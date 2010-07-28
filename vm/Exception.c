/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * Exception handling.
 */
#include "Dalvik.h"
#include "libdex/DexCatch.h"

#include <stdlib.h>

/*
Notes on Exception Handling

We have one fairly sticky issue to deal with: creating the exception stack
trace.  The trouble is that we need the current value of the program
counter for the method now being executed, but that's only held in a local
variable or hardware register in the main interpreter loop.

The exception mechanism requires that the current stack trace be associated
with a Throwable at the time the Throwable is constructed.  The construction
may or may not be associated with a throw.  We have three situations to
consider:

 (1) A Throwable is created with a "new Throwable" statement in the
     application code, for immediate or deferred use with a "throw" statement.
 (2) The VM throws an exception from within the interpreter core, e.g.
     after an integer divide-by-zero.
 (3) The VM throws an exception from somewhere deeper down, e.g. while
     trying to link a class.

We need to have the current value for the PC, which means that for
situation (3) the interpreter loop must copy it to an externally-accessible
location before handling any opcode that could cause the VM to throw
an exception.  We can't store it globally, because the various threads
would trample each other.  We can't store it in the Thread structure,
because it'll get overwritten as soon as the Throwable constructor starts
executing.  It needs to go on the stack, but our stack frames hold the
caller's *saved* PC, not the current PC.

Situation #1 doesn't require special handling.  Situation #2 could be dealt
with by passing the PC into the exception creation function.  The trick
is to solve situation #3 in a way that adds minimal overhead to common
operations.  Making it more costly to throw an exception is acceptable.

There are a few ways to deal with this:

 (a) Change "savedPc" to "currentPc" in the stack frame.  All of the
     stack logic gets offset by one frame.  The current PC is written
     to the current stack frame when necessary.
 (b) Write the current PC into the current stack frame, but without
     replacing "savedPc".  The JNI local refs pointer, which is only
     used for native code, can be overloaded to save space.
 (c) In dvmThrowException(), push an extra stack frame on, with the
     current PC in it.  The current PC is written into the Thread struct
     when necessary, and copied out when the VM throws.
 (d) Before doing something that might throw an exception, push a
     temporary frame on with the saved PC in it.

Solution (a) is the simplest, but breaks Dalvik's goal of mingling native
and interpreted stacks.

Solution (b) retains the simplicity of (a) without rearranging the stack,
but now in some cases we're storing the PC twice, which feels wrong.

Solution (c) usually works, because we push the saved PC onto the stack
before the Throwable construction can overwrite the copy in Thread.  One
way solution (c) could break is:
 - Interpreter saves the PC
 - Execute some bytecode, which runs successfully (and alters the saved PC)
 - Throw an exception before re-saving the PC (i.e in the same opcode)
This is a risk for anything that could cause <clinit> to execute, e.g.
executing a static method or accessing a static field.  Attemping to access
a field that doesn't exist in a class that does exist might cause this.
It may be possible to simply bracket the dvmCallMethod*() functions to
save/restore it.

Solution (d) incurs additional overhead, but may have other benefits (e.g.
it's easy to find the stack frames that should be removed before storage
in the Throwable).

Current plan is option (b), because it's simple, fast, and doesn't change
the way the stack works.
*/

/* fwd */
static bool initException(Object* exception, const char* msg, Object* cause,
    Thread* self);


/*
 * Cache pointers to some of the exception classes we use locally.
 *
 * Note this is NOT called during dexopt optimization.  Some of the fields
 * are initialized by the verifier (dvmVerifyCodeFlow).
 */
bool dvmExceptionStartup(void)
{
    gDvm.classJavaLangThrowable =
        dvmFindSystemClassNoInit("Ljava/lang/Throwable;");
    gDvm.classJavaLangRuntimeException =
        dvmFindSystemClassNoInit("Ljava/lang/RuntimeException;");
    gDvm.classJavaLangStackOverflowError =
        dvmFindSystemClassNoInit("Ljava/lang/StackOverflowError;");
    gDvm.classJavaLangError =
        dvmFindSystemClassNoInit("Ljava/lang/Error;");
    gDvm.classJavaLangStackTraceElement =
        dvmFindSystemClassNoInit("Ljava/lang/StackTraceElement;");
    gDvm.classJavaLangStackTraceElementArray =
        dvmFindArrayClass("[Ljava/lang/StackTraceElement;", NULL);
    if (gDvm.classJavaLangThrowable == NULL ||
        gDvm.classJavaLangStackTraceElement == NULL ||
        gDvm.classJavaLangStackTraceElementArray == NULL)
    {
        LOGE("Could not find one or more essential exception classes\n");
        return false;
    }

    /*
     * Find the constructor.  Note that, unlike other saved method lookups,
     * we're using a Method* instead of a vtable offset.  This is because
     * constructors don't have vtable offsets.  (Also, since we're creating
     * the object in question, it's impossible for anyone to sub-class it.)
     */
    Method* meth;
    meth = dvmFindDirectMethodByDescriptor(gDvm.classJavaLangStackTraceElement,
        "<init>", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;I)V");
    if (meth == NULL) {
        LOGE("Unable to find constructor for StackTraceElement\n");
        return false;
    }
    gDvm.methJavaLangStackTraceElement_init = meth;

    /* grab an offset for the stackData field */
    gDvm.offJavaLangThrowable_stackState =
        dvmFindFieldOffset(gDvm.classJavaLangThrowable,
            "stackState", "Ljava/lang/Object;");
    if (gDvm.offJavaLangThrowable_stackState < 0) {
        LOGE("Unable to find Throwable.stackState\n");
        return false;
    }

    /* and one for the message field, in case we want to show it */
    gDvm.offJavaLangThrowable_message =
        dvmFindFieldOffset(gDvm.classJavaLangThrowable,
            "detailMessage", "Ljava/lang/String;");
    if (gDvm.offJavaLangThrowable_message < 0) {
        LOGE("Unable to find Throwable.detailMessage\n");
        return false;
    }

    /* and one for the cause field, just 'cause */
    gDvm.offJavaLangThrowable_cause =
        dvmFindFieldOffset(gDvm.classJavaLangThrowable,
            "cause", "Ljava/lang/Throwable;");
    if (gDvm.offJavaLangThrowable_cause < 0) {
        LOGE("Unable to find Throwable.cause\n");
        return false;
    }

    return true;
}

/*
 * Clean up.
 */
void dvmExceptionShutdown(void)
{
    // nothing to do
}


/*
 * Format the message into a small buffer and pass it along.
 */
void dvmThrowExceptionFmtV(const char* exceptionDescriptor, const char* fmt,
    va_list args)
{
    char msgBuf[512];

    vsnprintf(msgBuf, sizeof(msgBuf), fmt, args);
    dvmThrowChainedException(exceptionDescriptor, msgBuf, NULL);
}

/*
 * Create a Throwable and throw an exception in the current thread (where
 * "throwing" just means "set the thread's exception pointer").
 *
 * "msg" and/or "cause" may be NULL.
 *
 * If we have a bad exception hierarchy -- something in Throwable.<init>
 * is missing -- then every attempt to throw an exception will result
 * in another exception.  Exceptions are generally allowed to "chain"
 * to other exceptions, so it's hard to auto-detect this problem.  It can
 * only happen if the system classes are broken, so it's probably not
 * worth spending cycles to detect it.
 *
 * We do have one case to worry about: if the classpath is completely
 * wrong, we'll go into a death spin during startup because we can't find
 * the initial class and then we can't find NoClassDefFoundError.  We have
 * to handle this case.
 *
 * [Do we want to cache pointers to common exception classes?]
 */
void dvmThrowChainedException(const char* exceptionDescriptor, const char* msg,
    Object* cause)
{
    ClassObject* excepClass;

    LOGV("THROW '%s' msg='%s' cause=%s\n",
        exceptionDescriptor, msg,
        (cause != NULL) ? cause->clazz->descriptor : "(none)");

    if (gDvm.initializing) {
        if (++gDvm.initExceptionCount >= 2) {
            LOGE("Too many exceptions during init (failed on '%s' '%s')\n",
                exceptionDescriptor, msg);
            dvmAbort();
        }
    }

    excepClass = dvmFindSystemClass(exceptionDescriptor);
    if (excepClass == NULL) {
        /*
         * We couldn't find the exception class.  The attempt to find a
         * nonexistent class should have raised an exception.  If no
         * exception is currently raised, then we're pretty clearly unable
         * to throw ANY sort of exception, and we need to pack it in.
         *
         * If we were able to throw the "class load failed" exception,
         * stick with that.  Ideally we'd stuff the original exception
         * into the "cause" field, but since we can't find it we can't
         * do that.  The exception class name should be in the "message"
         * field.
         */
        if (!dvmCheckException(dvmThreadSelf())) {
            LOGE("FATAL: unable to throw exception (failed on '%s' '%s')\n",
                exceptionDescriptor, msg);
            dvmAbort();
        }
        return;
    }

    dvmThrowChainedExceptionByClass(excepClass, msg, cause);
}

/*
 * Start/continue throwing process now that we have a class reference.
 */
void dvmThrowChainedExceptionByClass(ClassObject* excepClass, const char* msg,
    Object* cause)
{
    Thread* self = dvmThreadSelf();
    Object* exception;

    /* make sure the exception is initialized */
    if (!dvmIsClassInitialized(excepClass) && !dvmInitClass(excepClass)) {
        LOGE("ERROR: unable to initialize exception class '%s'\n",
            excepClass->descriptor);
        if (strcmp(excepClass->descriptor, "Ljava/lang/InternalError;") == 0)
            dvmAbort();
        dvmThrowChainedException("Ljava/lang/InternalError;",
            "failed to init original exception class", cause);
        return;
    }

    exception = dvmAllocObject(excepClass, ALLOC_DEFAULT);
    if (exception == NULL) {
        /*
         * We're in a lot of trouble.  We might be in the process of
         * throwing an out-of-memory exception, in which case the
         * pre-allocated object will have been thrown when our object alloc
         * failed.  So long as there's an exception raised, return and
         * allow the system to try to recover.  If not, something is broken
         * and we need to bail out.
         */
        if (dvmCheckException(self))
            goto bail;
        LOGE("FATAL: unable to allocate exception '%s' '%s'\n",
            excepClass->descriptor, msg != NULL ? msg : "(no msg)");
        dvmAbort();
    }

    /*
     * Init the exception.
     */
    if (gDvm.optimizing) {
        /* need the exception object, but can't invoke interpreted code */
        LOGV("Skipping init of exception %s '%s'\n",
            excepClass->descriptor, msg);
    } else {
        assert(excepClass == exception->clazz);
        if (!initException(exception, msg, cause, self)) {
            /*
             * Whoops.  If we can't initialize the exception, we can't use
             * it.  If there's an exception already set, the constructor
             * probably threw an OutOfMemoryError.
             */
            if (!dvmCheckException(self)) {
                /*
                 * We're required to throw something, so we just
                 * throw the pre-constructed internal error.
                 */
                self->exception = gDvm.internalErrorObj;
            }
            goto bail;
        }
    }

    self->exception = exception;

bail:
    dvmReleaseTrackedAlloc(exception, self);
}

/*
 * Throw the named exception using the dotted form of the class
 * descriptor as the exception message, and with the specified cause.
 */
void dvmThrowChainedExceptionWithClassMessage(const char* exceptionDescriptor,
    const char* messageDescriptor, Object* cause)
{
    char* message = dvmDescriptorToDot(messageDescriptor);

    dvmThrowChainedException(exceptionDescriptor, message, cause);
    free(message);
}

/*
 * Like dvmThrowExceptionWithMessageFromDescriptor, but take a
 * class object instead of a name.
 */
void dvmThrowExceptionByClassWithClassMessage(ClassObject* exceptionClass,
    const char* messageDescriptor)
{
    char* message = dvmDescriptorToName(messageDescriptor);

    dvmThrowExceptionByClass(exceptionClass, message);
    free(message);
}

/*
 * Find and return an exception constructor method that can take the
 * indicated parameters, or return NULL if no such constructor exists.
 */
static Method* findExceptionInitMethod(ClassObject* excepClass,
    bool hasMessage, bool hasCause)
{
    if (hasMessage) {
        Method* result;

        if (hasCause) {
            result = dvmFindDirectMethodByDescriptor(
                    excepClass, "<init>",
                    "(Ljava/lang/String;Ljava/lang/Throwable;)V");
        } else {
            result = dvmFindDirectMethodByDescriptor(
                    excepClass, "<init>", "(Ljava/lang/String;)V");
        }

        if (result != NULL) {
            return result;
        }

        if (hasCause) {
            return dvmFindDirectMethodByDescriptor(
                    excepClass, "<init>",
                    "(Ljava/lang/Object;Ljava/lang/Throwable;)V");
        } else {
            return dvmFindDirectMethodByDescriptor(
                    excepClass, "<init>", "(Ljava/lang/Object;)V");
        }
    } else if (hasCause) {
        return dvmFindDirectMethodByDescriptor(
                excepClass, "<init>", "(Ljava/lang/Throwable;)V");
    } else {
        return dvmFindDirectMethodByDescriptor(excepClass, "<init>", "()V");
    }
}

/*
 * Initialize an exception with an appropriate constructor.
 *
 * "exception" is the exception object to initialize.
 * Either or both of "msg" and "cause" may be null.
 * "self" is dvmThreadSelf(), passed in so we don't have to look it up again.
 *
 * If the process of initializing the exception causes another
 * exception (e.g., OutOfMemoryError) to be thrown, return an error
 * and leave self->exception intact.
 */
static bool initException(Object* exception, const char* msg, Object* cause,
    Thread* self)
{
    enum {
        kInitUnknown,
        kInitNoarg,
        kInitMsg,
        kInitMsgThrow,
        kInitThrow
    } initKind = kInitUnknown;
    Method* initMethod = NULL;
    ClassObject* excepClass = exception->clazz;
    StringObject* msgStr = NULL;
    bool result = false;
    bool needInitCause = false;

    assert(self != NULL);
    assert(self->exception == NULL);

    /* if we have a message, create a String */
    if (msg == NULL)
        msgStr = NULL;
    else {
        msgStr = dvmCreateStringFromCstr(msg);
        if (msgStr == NULL) {
            LOGW("Could not allocate message string \"%s\" while "
                    "throwing internal exception (%s)\n",
                    msg, excepClass->descriptor);
            goto bail;
        }
    }

    if (cause != NULL) {
        if (!dvmInstanceof(cause->clazz, gDvm.classJavaLangThrowable)) {
            LOGE("Tried to init exception with cause '%s'\n",
                cause->clazz->descriptor);
            dvmAbort();
        }
    }

    /*
     * The Throwable class has four public constructors:
     *  (1) Throwable()
     *  (2) Throwable(String message)
     *  (3) Throwable(String message, Throwable cause)  (added in 1.4)
     *  (4) Throwable(Throwable cause)                  (added in 1.4)
     *
     * The first two are part of the original design, and most exception
     * classes should support them.  The third prototype was used by
     * individual exceptions. e.g. ClassNotFoundException added it in 1.2.
     * The general "cause" mechanism was added in 1.4.  Some classes,
     * such as IllegalArgumentException, initially supported the first
     * two, but added the second two in a later release.
     *
     * Exceptions may be picky about how their "cause" field is initialized.
     * If you call ClassNotFoundException(String), it may choose to
     * initialize its "cause" field to null.  Doing so prevents future
     * calls to Throwable.initCause().
     *
     * So, if "cause" is not NULL, we need to look for a constructor that
     * takes a throwable.  If we can't find one, we fall back on calling
     * #1/#2 and making a separate call to initCause().  Passing a null ref
     * for "message" into Throwable(String, Throwable) is allowed, but we
     * prefer to use the Throwable-only version because it has different
     * behavior.
     *
     * java.lang.TypeNotPresentException is a strange case -- it has #3 but
     * not #2.  (Some might argue that the constructor is actually not #3,
     * because it doesn't take the message string as an argument, but it
     * has the same effect and we can work with it here.)
     *
     * java.lang.AssertionError is also a strange case -- it has a
     * constructor that takes an Object, but not one that takes a String.
     * There may be other cases like this, as well, so we generally look
     * for an Object-taking constructor if we can't find one that takes
     * a String.
     */
    if (cause == NULL) {
        if (msgStr == NULL) {
            initMethod = findExceptionInitMethod(excepClass, false, false);
            initKind = kInitNoarg;
        } else {
            initMethod = findExceptionInitMethod(excepClass, true, false);
            if (initMethod != NULL) {
                initKind = kInitMsg;
            } else {
                /* no #2, try #3 */
                initMethod = findExceptionInitMethod(excepClass, true, true);
                if (initMethod != NULL) {
                    initKind = kInitMsgThrow;
                }
            }
        }
    } else {
        if (msgStr == NULL) {
            initMethod = findExceptionInitMethod(excepClass, false, true);
            if (initMethod != NULL) {
                initKind = kInitThrow;
            } else {
                initMethod = findExceptionInitMethod(excepClass, false, false);
                initKind = kInitNoarg;
                needInitCause = true;
            }
        } else {
            initMethod = findExceptionInitMethod(excepClass, true, true);
            if (initMethod != NULL) {
                initKind = kInitMsgThrow;
            } else {
                initMethod = findExceptionInitMethod(excepClass, true, false);
                initKind = kInitMsg;
                needInitCause = true;
            }
        }
    }

    if (initMethod == NULL) {
        /*
         * We can't find the desired constructor.  This can happen if a
         * subclass of java/lang/Throwable doesn't define an expected
         * constructor, e.g. it doesn't provide one that takes a string
         * when a message has been provided.
         */
        LOGW("WARNING: exception class '%s' missing constructor "
            "(msg='%s' kind=%d)\n",
            excepClass->descriptor, msg, initKind);
        assert(strcmp(excepClass->descriptor,
                      "Ljava/lang/RuntimeException;") != 0);
        dvmThrowChainedException("Ljava/lang/RuntimeException;",
            "re-throw on exception class missing constructor", NULL);
        goto bail;
    }

    /*
     * Call the constructor with the appropriate arguments.
     */
    JValue unused;
    switch (initKind) {
    case kInitNoarg:
        LOGVV("+++ exc noarg (ic=%d)\n", needInitCause);
        dvmCallMethod(self, initMethod, exception, &unused);
        break;
    case kInitMsg:
        LOGVV("+++ exc msg (ic=%d)\n", needInitCause);
        dvmCallMethod(self, initMethod, exception, &unused, msgStr);
        break;
    case kInitThrow:
        LOGVV("+++ exc throw");
        assert(!needInitCause);
        dvmCallMethod(self, initMethod, exception, &unused, cause);
        break;
    case kInitMsgThrow:
        LOGVV("+++ exc msg+throw");
        assert(!needInitCause);
        dvmCallMethod(self, initMethod, exception, &unused, msgStr, cause);
        break;
    default:
        assert(false);
        goto bail;
    }

    /*
     * It's possible the constructor has thrown an exception.  If so, we
     * return an error and let our caller deal with it.
     */
    if (self->exception != NULL) {
        LOGW("Exception thrown (%s) while throwing internal exception (%s)\n",
            self->exception->clazz->descriptor, exception->clazz->descriptor);
        goto bail;
    }

    /*
     * If this exception was caused by another exception, and we weren't
     * able to find a cause-setting constructor, set the "cause" field
     * with an explicit call.
     */
    if (needInitCause) {
        Method* initCause;
        initCause = dvmFindVirtualMethodHierByDescriptor(excepClass, "initCause",
            "(Ljava/lang/Throwable;)Ljava/lang/Throwable;");
        if (initCause != NULL) {
            dvmCallMethod(self, initCause, exception, &unused, cause);
            if (self->exception != NULL) {
                /* initCause() threw an exception; return an error and
                 * let the caller deal with it.
                 */
                LOGW("Exception thrown (%s) during initCause() "
                        "of internal exception (%s)\n",
                        self->exception->clazz->descriptor,
                        exception->clazz->descriptor);
                goto bail;
            }
        } else {
            LOGW("WARNING: couldn't find initCause in '%s'\n",
                excepClass->descriptor);
        }
    }


    result = true;

bail:
    dvmReleaseTrackedAlloc((Object*) msgStr, self);     // NULL is ok
    return result;
}


/*
 * Clear the pending exception and the "initExceptionCount" counter.  This
 * is used by the optimization and verification code, which has to run with
 * "initializing" set to avoid going into a death-spin if the "class not
 * found" exception can't be found.
 *
 * This can also be called when the VM is in a "normal" state, e.g. when
 * verifying classes that couldn't be verified at optimization time.  The
 * reset of initExceptionCount should be harmless in that case.
 */
void dvmClearOptException(Thread* self)
{
    self->exception = NULL;
    gDvm.initExceptionCount = 0;
}

/*
 * Returns "true" if this is a "checked" exception, i.e. it's a subclass
 * of Throwable (assumed) but not a subclass of RuntimeException or Error.
 */
bool dvmIsCheckedException(const Object* exception)
{
    if (dvmInstanceof(exception->clazz, gDvm.classJavaLangError) ||
        dvmInstanceof(exception->clazz, gDvm.classJavaLangRuntimeException))
    {
        return false;
    } else {
        return true;
    }
}

/*
 * Wrap the now-pending exception in a different exception.  This is useful
 * for reflection stuff that wants to hand a checked exception back from a
 * method that doesn't declare it.
 *
 * If something fails, an (unchecked) exception related to that failure
 * will be pending instead.
 */
void dvmWrapException(const char* newExcepStr)
{
    Thread* self = dvmThreadSelf();
    Object* origExcep;
    ClassObject* iteClass;

    origExcep = dvmGetException(self);
    dvmAddTrackedAlloc(origExcep, self);    // don't let the GC free it

    dvmClearException(self);                // clear before class lookup
    iteClass = dvmFindSystemClass(newExcepStr);
    if (iteClass != NULL) {
        Object* iteExcep;
        Method* initMethod;

        iteExcep = dvmAllocObject(iteClass, ALLOC_DEFAULT);
        if (iteExcep != NULL) {
            initMethod = dvmFindDirectMethodByDescriptor(iteClass, "<init>",
                            "(Ljava/lang/Throwable;)V");
            if (initMethod != NULL) {
                JValue unused;
                dvmCallMethod(self, initMethod, iteExcep, &unused,
                    origExcep);

                /* if <init> succeeded, replace the old exception */
                if (!dvmCheckException(self))
                    dvmSetException(self, iteExcep);
            }
            dvmReleaseTrackedAlloc(iteExcep, NULL);

            /* if initMethod doesn't exist, or failed... */
            if (!dvmCheckException(self))
                dvmSetException(self, origExcep);
        } else {
            /* leave OutOfMemoryError pending */
        }
    } else {
        /* leave ClassNotFoundException pending */
    }

    assert(dvmCheckException(self));
    dvmReleaseTrackedAlloc(origExcep, self);
}

/*
 * Get the "cause" field from an exception.
 *
 * The Throwable class initializes the "cause" field to "this" to
 * differentiate between being initialized to null and never being
 * initialized.  We check for that here and convert it to NULL.
 */
Object* dvmGetExceptionCause(const Object* exception)
{
    if (!dvmInstanceof(exception->clazz, gDvm.classJavaLangThrowable)) {
        LOGE("Tried to get cause from object of type '%s'\n",
            exception->clazz->descriptor);
        dvmAbort();
    }
    Object* cause =
        dvmGetFieldObject(exception, gDvm.offJavaLangThrowable_cause);
    if (cause == exception)
        return NULL;
    else
        return cause;
}

/*
 * Print the stack trace of the current exception on stderr.  This is called
 * from the JNI ExceptionDescribe call.
 *
 * For consistency we just invoke the Throwable printStackTrace method,
 * which might be overridden in the exception object.
 *
 * Exceptions thrown during the course of printing the stack trace are
 * ignored.
 */
void dvmPrintExceptionStackTrace(void)
{
    Thread* self = dvmThreadSelf();
    Object* exception;
    Method* printMethod;

    exception = self->exception;
    if (exception == NULL)
        return;

    self->exception = NULL;
    printMethod = dvmFindVirtualMethodHierByDescriptor(exception->clazz,
                    "printStackTrace", "()V");
    if (printMethod != NULL) {
        JValue unused;
        dvmCallMethod(self, printMethod, exception, &unused);
    } else {
        LOGW("WARNING: could not find printStackTrace in %s\n",
            exception->clazz->descriptor);
    }

    if (self->exception != NULL) {
        LOGW("NOTE: exception thrown while printing stack trace: %s\n",
            self->exception->clazz->descriptor);
    }

    self->exception = exception;
}

/*
 * Search the method's list of exceptions for a match.
 *
 * Returns the offset of the catch block on success, or -1 on failure.
 */
static int findCatchInMethod(Thread* self, const Method* method, int relPc,
    ClassObject* excepClass)
{
    /*
     * Need to clear the exception before entry.  Otherwise, dvmResolveClass
     * might think somebody threw an exception while it was loading a class.
     */
    assert(!dvmCheckException(self));
    assert(!dvmIsNativeMethod(method));

    LOGVV("findCatchInMethod %s.%s excep=%s depth=%d\n",
        method->clazz->descriptor, method->name, excepClass->descriptor,
        dvmComputeExactFrameDepth(self->curFrame));

    DvmDex* pDvmDex = method->clazz->pDvmDex;
    const DexCode* pCode = dvmGetMethodCode(method);
    DexCatchIterator iterator;

    if (dexFindCatchHandler(&iterator, pCode, relPc)) {
        for (;;) {
            DexCatchHandler* handler = dexCatchIteratorNext(&iterator);

            if (handler == NULL) {
                break;
            }

            if (handler->typeIdx == kDexNoIndex) {
                /* catch-all */
                LOGV("Match on catch-all block at 0x%02x in %s.%s for %s\n",
                        relPc, method->clazz->descriptor,
                        method->name, excepClass->descriptor);
                return handler->address;
            }

            ClassObject* throwable =
                dvmDexGetResolvedClass(pDvmDex, handler->typeIdx);
            if (throwable == NULL) {
                /*
                 * TODO: this behaves badly if we run off the stack
                 * while trying to throw an exception.  The problem is
                 * that, if we're in a class loaded by a class loader,
                 * the call to dvmResolveClass has to ask the class
                 * loader for help resolving any previously-unresolved
                 * classes.  If this particular class loader hasn't
                 * resolved StackOverflowError, it will call into
                 * interpreted code, and blow up.
                 *
                 * We currently replace the previous exception with
                 * the StackOverflowError, which means they won't be
                 * catching it *unless* they explicitly catch
                 * StackOverflowError, in which case we'll be unable
                 * to resolve the class referred to by the "catch"
                 * block.
                 *
                 * We end up getting a huge pile of warnings if we do
                 * a simple synthetic test, because this method gets
                 * called on every stack frame up the tree, and it
                 * fails every time.
                 *
                 * This eventually bails out, effectively becoming an
                 * uncatchable exception, so other than the flurry of
                 * warnings it's not really a problem.  Still, we could
                 * probably handle this better.
                 */
                throwable = dvmResolveClass(method->clazz, handler->typeIdx,
                    true);
                if (throwable == NULL) {
                    /*
                     * We couldn't find the exception they wanted in
                     * our class files (or, perhaps, the stack blew up
                     * while we were querying a class loader). Cough
                     * up a warning, then move on to the next entry.
                     * Keep the exception status clear.
                     */
                    LOGW("Could not resolve class ref'ed in exception "
                            "catch list (class index %d, exception %s)\n",
                            handler->typeIdx,
                            (self->exception != NULL) ?
                            self->exception->clazz->descriptor : "(none)");
                    dvmClearException(self);
                    continue;
                }
            }

            //LOGD("ADDR MATCH, check %s instanceof %s\n",
            //    excepClass->descriptor, pEntry->excepClass->descriptor);

            if (dvmInstanceof(excepClass, throwable)) {
                LOGV("Match on catch block at 0x%02x in %s.%s for %s\n",
                        relPc, method->clazz->descriptor,
                        method->name, excepClass->descriptor);
                return handler->address;
            }
        }
    }

    LOGV("No matching catch block at 0x%02x in %s for %s\n",
        relPc, method->name, excepClass->descriptor);
    return -1;
}

/*
 * Find a matching "catch" block.  "pc" is the relative PC within the
 * current method, indicating the offset from the start in 16-bit units.
 *
 * Returns the offset to the catch block, or -1 if we run up against a
 * break frame without finding anything.
 *
 * The class resolution stuff we have to do while evaluating the "catch"
 * blocks could cause an exception.  The caller should clear the exception
 * before calling here and restore it after.
 *
 * Sets *newFrame to the frame pointer of the frame with the catch block.
 * If "scanOnly" is false, self->curFrame is also set to this value.
 */
int dvmFindCatchBlock(Thread* self, int relPc, Object* exception,
    bool scanOnly, void** newFrame)
{
    void* fp = self->curFrame;
    int catchAddr = -1;

    assert(!dvmCheckException(self));

    while (true) {
        StackSaveArea* saveArea = SAVEAREA_FROM_FP(fp);
        catchAddr = findCatchInMethod(self, saveArea->method, relPc,
                        exception->clazz);
        if (catchAddr >= 0)
            break;

        /*
         * Normally we'd check for ACC_SYNCHRONIZED methods and unlock
         * them as we unroll.  Dalvik uses what amount to generated
         * "finally" blocks to take care of this for us.
         */

        /* output method profiling info */
        if (!scanOnly) {
            TRACE_METHOD_UNROLL(self, saveArea->method);
        }

        /*
         * Move up one frame.  If the next thing up is a break frame,
         * break out now so we're left unrolled to the last method frame.
         * We need to point there so we can roll up the JNI local refs
         * if this was a native method.
         */
        assert(saveArea->prevFrame != NULL);
        if (dvmIsBreakFrame(saveArea->prevFrame)) {
            if (!scanOnly)
                break;      // bail with catchAddr == -1

            /*
             * We're scanning for the debugger.  It needs to know if this
             * exception is going to be caught or not, and we need to figure
             * out if it will be caught *ever* not just between the current
             * position and the next break frame.  We can't tell what native
             * code is going to do, so we assume it never catches exceptions.
             *
             * Start by finding an interpreted code frame.
             */
            fp = saveArea->prevFrame;           // this is the break frame
            saveArea = SAVEAREA_FROM_FP(fp);
            fp = saveArea->prevFrame;           // this may be a good one
            while (fp != NULL) {
                if (!dvmIsBreakFrame(fp)) {
                    saveArea = SAVEAREA_FROM_FP(fp);
                    if (!dvmIsNativeMethod(saveArea->method))
                        break;
                }

                fp = SAVEAREA_FROM_FP(fp)->prevFrame;
            }
            if (fp == NULL)
                break;      // bail with catchAddr == -1

            /*
             * Now fp points to the "good" frame.  When the interp code
             * invoked the native code, it saved a copy of its current PC
             * into xtra.currentPc.  Pull it out of there.
             */
            relPc =
                saveArea->xtra.currentPc - SAVEAREA_FROM_FP(fp)->method->insns;
        } else {
            fp = saveArea->prevFrame;

            /* savedPc in was-current frame goes with method in now-current */
            relPc = saveArea->savedPc - SAVEAREA_FROM_FP(fp)->method->insns;
        }
    }

    if (!scanOnly)
        self->curFrame = fp;

    /*
     * The class resolution in findCatchInMethod() could cause an exception.
     * Clear it to be safe.
     */
    self->exception = NULL;

    *newFrame = fp;
    return catchAddr;
}

/*
 * We have to carry the exception's stack trace around, but in many cases
 * it will never be examined.  It makes sense to keep it in a compact,
 * VM-specific object, rather than an array of Objects with strings.
 *
 * Pass in the thread whose stack we're interested in.  If "thread" is
 * not self, the thread must be suspended.  This implies that the thread
 * list lock is held, which means we can't allocate objects or we risk
 * jamming the GC.  So, we allow this function to return different formats.
 * (This shouldn't be called directly -- see the inline functions in the
 * header file.)
 *
 * If "wantObject" is true, this returns a newly-allocated Object, which is
 * presently an array of integers, but could become something else in the
 * future.  If "wantObject" is false, return plain malloc data.
 *
 * NOTE: if we support class unloading, we will need to scan the class
 * object references out of these arrays.
 */
void* dvmFillInStackTraceInternal(Thread* thread, bool wantObject, int* pCount)
{
    ArrayObject* stackData = NULL;
    int* simpleData = NULL;
    void* fp;
    void* startFp;
    int stackDepth;
    int* intPtr;

    if (pCount != NULL)
        *pCount = 0;
    fp = thread->curFrame;

    assert(thread == dvmThreadSelf() || dvmIsSuspended(thread));

    /*
     * We're looking at a stack frame for code running below a Throwable
     * constructor.  We want to remove the Throwable methods and the
     * superclass initializations so the user doesn't see them when they
     * read the stack dump.
     *
     * TODO: this just scrapes off the top layers of Throwable.  Might not do
     * the right thing if we create an exception object or cause a VM
     * exception while in a Throwable method.
     */
    while (fp != NULL) {
        const StackSaveArea* saveArea = SAVEAREA_FROM_FP(fp);
        const Method* method = saveArea->method;

        if (dvmIsBreakFrame(fp))
            break;
        if (!dvmInstanceof(method->clazz, gDvm.classJavaLangThrowable))
            break;
        //LOGD("EXCEP: ignoring %s.%s\n",
        //         method->clazz->descriptor, method->name);
        fp = saveArea->prevFrame;
    }
    startFp = fp;

    /*
     * Compute the stack depth.
     */
    stackDepth = 0;
    while (fp != NULL) {
        const StackSaveArea* saveArea = SAVEAREA_FROM_FP(fp);

        if (!dvmIsBreakFrame(fp))
            stackDepth++;

        assert(fp != saveArea->prevFrame);
        fp = saveArea->prevFrame;
    }
    //LOGD("EXCEP: stack depth is %d\n", stackDepth);

    if (!stackDepth)
        goto bail;

    /*
     * We need to store a pointer to the Method and the program counter.
     * We have 4-byte pointers, so we use '[I'.
     */
    if (wantObject) {
        assert(sizeof(Method*) == 4);
        stackData = dvmAllocPrimitiveArray('I', stackDepth*2, ALLOC_DEFAULT);
        if (stackData == NULL) {
            assert(dvmCheckException(dvmThreadSelf()));
            goto bail;
        }
        intPtr = (int*) stackData->contents;
    } else {
        /* array of ints; first entry is stack depth */
        assert(sizeof(Method*) == sizeof(int));
        simpleData = (int*) malloc(sizeof(int) * stackDepth*2);
        if (simpleData == NULL)
            goto bail;

        assert(pCount != NULL);
        intPtr = simpleData;
    }
    if (pCount != NULL)
        *pCount = stackDepth;

    fp = startFp;
    while (fp != NULL) {
        const StackSaveArea* saveArea = SAVEAREA_FROM_FP(fp);
        const Method* method = saveArea->method;

        if (!dvmIsBreakFrame(fp)) {
            //LOGD("EXCEP keeping %s.%s\n", method->clazz->descriptor,
            //         method->name);

            *intPtr++ = (int) method;
            if (dvmIsNativeMethod(method)) {
                *intPtr++ = 0;      /* no saved PC for native methods */
            } else {
                assert(saveArea->xtra.currentPc >= method->insns &&
                        saveArea->xtra.currentPc <
                        method->insns + dvmGetMethodInsnsSize(method));
                *intPtr++ = (int) (saveArea->xtra.currentPc - method->insns);
            }

            stackDepth--;       // for verification
        }

        assert(fp != saveArea->prevFrame);
        fp = saveArea->prevFrame;
    }
    assert(stackDepth == 0);

bail:
    if (wantObject) {
        dvmReleaseTrackedAlloc((Object*) stackData, dvmThreadSelf());
        return stackData;
    } else {
        return simpleData;
    }
}


/*
 * Given an Object previously created by dvmFillInStackTrace(), use the
 * contents of the saved stack trace to generate an array of
 * java/lang/StackTraceElement objects.
 *
 * The returned array is not added to the "local refs" list.
 */
ArrayObject* dvmGetStackTrace(const Object* ostackData)
{
    const ArrayObject* stackData = (const ArrayObject*) ostackData;
    const int* intVals;
    int stackSize;

    stackSize = stackData->length / 2;
    intVals = (const int*) stackData->contents;
    return dvmGetStackTraceRaw(intVals, stackSize);
}

/*
 * Generate an array of StackTraceElement objects from the raw integer
 * data encoded by dvmFillInStackTrace().
 *
 * "intVals" points to the first {method,pc} pair.
 *
 * The returned array is not added to the "local refs" list.
 */
ArrayObject* dvmGetStackTraceRaw(const int* intVals, int stackDepth)
{
    ArrayObject* steArray = NULL;
    int i;

    /* init this if we haven't yet */
    if (!dvmIsClassInitialized(gDvm.classJavaLangStackTraceElement))
        dvmInitClass(gDvm.classJavaLangStackTraceElement);

    /* allocate a StackTraceElement array */
    steArray = dvmAllocArray(gDvm.classJavaLangStackTraceElementArray,
                    stackDepth, kObjectArrayRefWidth, ALLOC_DEFAULT);
    if (steArray == NULL)
        goto bail;

    /*
     * Allocate and initialize a StackTraceElement for each stack frame.
     * We use the standard constructor to configure the object.
     */
    for (i = 0; i < stackDepth; i++) {
        Object* ste;
        Method* meth;
        StringObject* className;
        StringObject* methodName;
        StringObject* fileName;
        int lineNumber, pc;
        const char* sourceFile;
        char* dotName;

        ste = dvmAllocObject(gDvm.classJavaLangStackTraceElement,ALLOC_DEFAULT);
        if (ste == NULL)
            goto bail;

        meth = (Method*) *intVals++;
        pc = *intVals++;

        if (pc == -1)      // broken top frame?
            lineNumber = 0;
        else
            lineNumber = dvmLineNumFromPC(meth, pc);

        dotName = dvmDescriptorToDot(meth->clazz->descriptor);
        className = dvmCreateStringFromCstr(dotName);
        free(dotName);

        methodName = dvmCreateStringFromCstr(meth->name);
        sourceFile = dvmGetMethodSourceFile(meth);
        if (sourceFile != NULL)
            fileName = dvmCreateStringFromCstr(sourceFile);
        else
            fileName = NULL;

        /*
         * Invoke:
         *  public StackTraceElement(String declaringClass, String methodName,
         *      String fileName, int lineNumber)
         * (where lineNumber==-2 means "native")
         */
        JValue unused;
        dvmCallMethod(dvmThreadSelf(), gDvm.methJavaLangStackTraceElement_init,
            ste, &unused, className, methodName, fileName, lineNumber);

        dvmReleaseTrackedAlloc(ste, NULL);
        dvmReleaseTrackedAlloc((Object*) className, NULL);
        dvmReleaseTrackedAlloc((Object*) methodName, NULL);
        dvmReleaseTrackedAlloc((Object*) fileName, NULL);

        if (dvmCheckException(dvmThreadSelf()))
            goto bail;

        dvmSetObjectArrayElement(steArray, i, ste);
    }

bail:
    dvmReleaseTrackedAlloc((Object*) steArray, NULL);
    return steArray;
}

/*
 * Dump the contents of a raw stack trace to the log.
 */
void dvmLogRawStackTrace(const int* intVals, int stackDepth)
{
    int i;

    /*
     * Run through the array of stack frame data.
     */
    for (i = 0; i < stackDepth; i++) {
        Method* meth;
        int lineNumber, pc;
        const char* sourceFile;
        char* dotName;

        meth = (Method*) *intVals++;
        pc = *intVals++;

        if (pc == -1)      // broken top frame?
            lineNumber = 0;
        else
            lineNumber = dvmLineNumFromPC(meth, pc);

        // probably don't need to do this, but it looks nicer
        dotName = dvmDescriptorToDot(meth->clazz->descriptor);

        if (dvmIsNativeMethod(meth)) {
            LOGI("\tat %s.%s(Native Method)\n", dotName, meth->name);
        } else {
            LOGI("\tat %s.%s(%s:%d)\n",
                dotName, meth->name, dvmGetMethodSourceFile(meth),
                dvmLineNumFromPC(meth, pc));
        }

        free(dotName);

        sourceFile = dvmGetMethodSourceFile(meth);
    }
}

/*
 * Print the direct stack trace of the given exception to the log.
 */
static void logStackTraceOf(Object* exception)
{
    const ArrayObject* stackData;
    StringObject* messageStr;
    int stackSize;
    const int* intVals;

    messageStr = (StringObject*) dvmGetFieldObject(exception,
                    gDvm.offJavaLangThrowable_message);
    if (messageStr != NULL) {
        char* cp = dvmCreateCstrFromString(messageStr);
        LOGI("%s: %s\n", exception->clazz->descriptor, cp);
        free(cp);
    } else {
        LOGI("%s:\n", exception->clazz->descriptor);
    }

    stackData = (const ArrayObject*) dvmGetFieldObject(exception,
                    gDvm.offJavaLangThrowable_stackState);
    if (stackData == NULL) {
        LOGI("  (no stack trace data found)\n");
        return;
    }

    stackSize = stackData->length / 2;
    intVals = (const int*) stackData->contents;

    dvmLogRawStackTrace(intVals, stackSize);
}

/*
 * Print the stack trace of the current thread's exception, as well as
 * the stack traces of any chained exceptions, to the log. We extract
 * the stored stack trace and process it internally instead of calling
 * interpreted code.
 */
void dvmLogExceptionStackTrace(void)
{
    Object* exception = dvmThreadSelf()->exception;
    Object* cause;

    if (exception == NULL) {
        LOGW("tried to log a null exception?\n");
        return;
    }

    for (;;) {
        logStackTraceOf(exception);
        cause = dvmGetExceptionCause(exception);
        if (cause == NULL) {
            break;
        }
        LOGI("Caused by:\n");
        exception = cause;
    }
}
