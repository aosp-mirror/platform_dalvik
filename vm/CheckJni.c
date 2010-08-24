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
 * Support for -Xcheck:jni (the "careful" version of the JNI interfaces).
 *
 * We want to verify types, make sure class and field IDs are valid, and
 * ensure that JNI's semantic expectations are being met.  JNI seems to
 * be relatively lax when it comes to requirements for permission checks,
 * e.g. access to private methods is generally allowed from anywhere.
 *
 * TODO: keep a counter on global Get/Release.  Report a warning if some Gets
 * were not Released.  Do not count explicit Add/DeleteGlobalRef calls (or
 * count them separately, so we can complain if they exceed a certain
 * threshold).
 *
 * TODO: verify that the methodID passed into the Call functions is for
 * a method in the specified class.
 */
#include "Dalvik.h"
#include "JniInternal.h"

#include <zlib.h>

static void abortMaybe(void);       // fwd


/*
 * ===========================================================================
 *      JNI call bridge wrapper
 * ===========================================================================
 */

/*
 * Check the result of a native method call that returns an object reference.
 *
 * The primary goal here is to verify that native code is returning the
 * correct type of object.  If it's declared to return a String but actually
 * returns a byte array, things will fail in strange ways later on.
 *
 * This can be a fairly expensive operation, since we have to look up the
 * return type class by name in method->clazz' class loader.  We take a
 * shortcut here and allow the call to succeed if the descriptor strings
 * match.  This will allow some false-positives when a class is redefined
 * by a class loader, but that's rare enough that it doesn't seem worth
 * testing for.
 *
 * At this point, pResult->l has already been converted to an object pointer.
 */
static void checkCallResultCommon(const u4* args, JValue* pResult,
    const Method* method, Thread* self)
{
    assert(pResult->l != NULL);
    Object* resultObj = (Object*) pResult->l;
    ClassObject* objClazz = resultObj->clazz;

    /*
     * Make sure that pResult->l is an instance of the type this
     * method was expected to return.
     */
    const char* declType = dexProtoGetReturnType(&method->prototype);
    const char* objType = objClazz->descriptor;
    if (strcmp(declType, objType) == 0) {
        /* names match; ignore class loader issues and allow it */
        LOGV("Check %s.%s: %s io %s (FAST-OK)\n",
            method->clazz->descriptor, method->name, objType, declType);
    } else {
        /*
         * Names didn't match.  We need to resolve declType in the context
         * of method->clazz->classLoader, and compare the class objects
         * for equality.
         *
         * Since we're returning an instance of declType, it's safe to
         * assume that it has been loaded and initialized (or, for the case
         * of an array, generated).  However, the current class loader may
         * not be listed as an initiating loader, so we can't just look for
         * it in the loaded-classes list.
         */
        ClassObject* declClazz;

        declClazz = dvmFindClassNoInit(declType, method->clazz->classLoader);
        if (declClazz == NULL) {
            LOGW("JNI WARNING: method declared to return '%s' returned '%s'\n",
                declType, objType);
            LOGW("             failed in %s.%s ('%s' not found)\n",
                method->clazz->descriptor, method->name, declType);
            abortMaybe();
            return;
        }
        if (!dvmInstanceof(objClazz, declClazz)) {
            LOGW("JNI WARNING: method declared to return '%s' returned '%s'\n",
                declType, objType);
            LOGW("             failed in %s.%s\n",
                method->clazz->descriptor, method->name);
            abortMaybe();
            return;
        } else {
            LOGV("Check %s.%s: %s io %s (SLOW-OK)\n",
                method->clazz->descriptor, method->name, objType, declType);
        }
    }
}

/*
 * Determine if we need to check the return type coming out of the call.
 *
 * (We don't do this at the top of checkCallResultCommon() because this is on
 * the critical path for native method calls.)
 */
static inline bool callNeedsCheck(const u4* args, JValue* pResult,
    const Method* method, Thread* self)
{
    return (method->shorty[0] == 'L' && !dvmCheckException(self) &&
            pResult->l != NULL);
}

/*
 * Check a call into native code.
 */
void dvmCheckCallJNIMethod_general(const u4* args, JValue* pResult,
    const Method* method, Thread* self)
{
    dvmCallJNIMethod_general(args, pResult, method, self);
    if (callNeedsCheck(args, pResult, method, self))
        checkCallResultCommon(args, pResult, method, self);
}

/*
 * Check a synchronized call into native code.
 */
void dvmCheckCallJNIMethod_synchronized(const u4* args, JValue* pResult,
    const Method* method, Thread* self)
{
    dvmCallJNIMethod_synchronized(args, pResult, method, self);
    if (callNeedsCheck(args, pResult, method, self))
        checkCallResultCommon(args, pResult, method, self);
}

/*
 * Check a virtual call with no reference arguments (other than "this").
 */
void dvmCheckCallJNIMethod_virtualNoRef(const u4* args, JValue* pResult,
    const Method* method, Thread* self)
{
    dvmCallJNIMethod_virtualNoRef(args, pResult, method, self);
    if (callNeedsCheck(args, pResult, method, self))
        checkCallResultCommon(args, pResult, method, self);
}

/*
 * Check a static call with no reference arguments (other than "clazz").
 */
void dvmCheckCallJNIMethod_staticNoRef(const u4* args, JValue* pResult,
    const Method* method, Thread* self)
{
    dvmCallJNIMethod_staticNoRef(args, pResult, method, self);
    if (callNeedsCheck(args, pResult, method, self))
        checkCallResultCommon(args, pResult, method, self);
}


/*
 * ===========================================================================
 *      JNI function helpers
 * ===========================================================================
 */

#define JNI_ENTER()     dvmChangeStatus(NULL, THREAD_RUNNING)
#define JNI_EXIT()      dvmChangeStatus(NULL, THREAD_NATIVE)

#define BASE_ENV(_env)  (((JNIEnvExt*)_env)->baseFuncTable)
#define BASE_VM(_vm)    (((JavaVMExt*)_vm)->baseFuncTable)

#define kRedundantDirectBufferTest false

/*
 * Flags passed into checkThread().
 */
#define kFlag_Default       0x0000

#define kFlag_CritBad       0x0000      /* calling while in critical is bad */
#define kFlag_CritOkay      0x0001      /* ...okay */
#define kFlag_CritGet       0x0002      /* this is a critical "get" */
#define kFlag_CritRelease   0x0003      /* this is a critical "release" */
#define kFlag_CritMask      0x0003      /* bit mask to get "crit" value */

#define kFlag_ExcepBad      0x0000      /* raised exceptions are bad */
#define kFlag_ExcepOkay     0x0004      /* ...okay */

/*
 * Enter/exit macros for JNI env "check" functions.  These do not change
 * the thread state within the VM.
 */
#define CHECK_ENTER(_env, _flags)                                           \
    do {                                                                    \
        JNI_TRACE(true, true);                                              \
        checkThread(_env, _flags, __FUNCTION__);                            \
    } while(false)

#define CHECK_EXIT(_env)                                                    \
    do { JNI_TRACE(false, true); } while(false)


/*
 * Enter/exit macros for JNI invocation interface "check" functions.  These
 * do not change the thread state within the VM.
 *
 * Set "_hasmeth" to true if we have a valid thread with a method pointer.
 * We won't have one before attaching a thread, after detaching a thread, or
 * after destroying the VM.
 */
#define CHECK_VMENTER(_vm, _hasmeth)                                        \
    do { JNI_TRACE(true, _hasmeth); } while(false)
#define CHECK_VMEXIT(_vm, _hasmeth)                                         \
    do { JNI_TRACE(false, _hasmeth); } while(false)

#define CHECK_FIELD_TYPE(_env, _obj, _fieldid, _prim, _isstatic)            \
    checkFieldType(_env, _obj, _fieldid, _prim, _isstatic, __FUNCTION__)
#define CHECK_INST_FIELD_ID(_env, _obj, _fieldid)                           \
    checkInstanceFieldID(_env, _obj, _fieldid, __FUNCTION__)
#define CHECK_CLASS(_env, _clazz)                                           \
    checkClass(_env, _clazz, __FUNCTION__)
#define CHECK_STRING(_env, _str)                                            \
    checkString(_env, _str, __FUNCTION__)
#define CHECK_UTF_STRING(_env, _str, _nullok)                               \
    checkUtfString(_env, _str, _nullok, __FUNCTION__)
#define CHECK_CLASS_NAME(_env, _str)                                        \
    checkClassName(_env, _str, __FUNCTION__)
#define CHECK_OBJECT(_env, _obj)                                            \
    checkObject(_env, _obj, __FUNCTION__)
#define CHECK_ARRAY(_env, _array)                                           \
    checkArray(_env, _array, __FUNCTION__)
#define CHECK_RELEASE_MODE(_env, _mode)                                     \
    checkReleaseMode(_env, _mode, __FUNCTION__)
#define CHECK_LENGTH_POSITIVE(_env, _length)                                \
    checkLengthPositive(_env, _length, __FUNCTION__)
#define CHECK_NON_NULL(_env, _ptr)                                          \
    checkNonNull(_env, _ptr, __FUNCTION__)
#define CHECK_SIG(_env, _methid, _sigbyte, _isstatic)                       \
    checkSig(_env, _methid, _sigbyte, _isstatic, __FUNCTION__)
#define CHECK_VIRTUAL_METHOD(_env, _obj, _methid)                           \
    checkVirtualMethod(_env, _obj, _methid, __FUNCTION__)
#define CHECK_STATIC_METHOD(_env, _clazz, _methid)                          \
    checkStaticMethod(_env, _clazz, _methid, __FUNCTION__)
#define CHECK_METHOD_ARGS_A(_env, _methid, _args)                           \
    checkMethodArgsA(_env, _methid, _args, __FUNCTION__)
#define CHECK_METHOD_ARGS_V(_env, _methid, _args)                           \
    checkMethodArgsV(_env, _methid, _args, __FUNCTION__)

/*
 * Prints trace messages when a native method calls a JNI function such as
 * NewByteArray. Enabled if both "-Xcheck:jni" and "-verbose:jni" are enabled.
 */
#define JNI_TRACE(_entry, _hasmeth)                                         \
    do {                                                                    \
        if (gDvm.verboseJni && (_entry)) {                                  \
            static const char* classDescriptor = "???";                     \
            static const char* methodName = "???";                          \
            if (_hasmeth) {                                                 \
                const Method* meth = dvmGetCurrentJNIMethod();              \
                classDescriptor = meth->clazz->descriptor;                  \
                methodName = meth->name;                                    \
            }                                                               \
            /* use +6 to drop the leading "Check_" */                       \
            LOGI("JNI: %s (from %s.%s)",                                    \
                (__FUNCTION__)+6, classDescriptor, methodName);             \
        }                                                                   \
    } while(false)

/*
 * Log the current location.
 *
 * "func" looks like "Check_DeleteLocalRef"; we drop the "Check_".
 */
static void showLocation(const Method* meth, const char* func)
{
    char* desc = dexProtoCopyMethodDescriptor(&meth->prototype);
    LOGW("             in %s.%s %s (%s)\n",
        meth->clazz->descriptor, meth->name, desc, func + 6);
    free(desc);
}

/*
 * Abort if we are configured to bail out on JNI warnings.
 */
static void abortMaybe(void)
{
    JavaVMExt* vm = (JavaVMExt*) gDvm.vmList;
    if (vm->warnError) {
        dvmDumpThread(dvmThreadSelf(), false);
        dvmAbort();
    }
}

/*
 * Verify that the current thread is (a) attached and (b) associated with
 * this particular instance of JNIEnv.
 *
 * Verify that, if this thread previously made a critical "get" call, we
 * do the corresponding "release" call before we try anything else.
 *
 * Verify that, if an exception has been raised, the native code doesn't
 * make any JNI calls other than the Exception* methods.
 *
 * TODO? if we add support for non-JNI native calls, make sure that the
 * method at the top of the interpreted stack is a JNI method call.  (Or
 * set a flag in the Thread/JNIEnv when the call is made and clear it on
 * return?)
 *
 * NOTE: we are still in THREAD_NATIVE mode.  A GC could happen at any time.
 */
static void checkThread(JNIEnv* env, int flags, const char* func)
{
    JNIEnvExt* threadEnv;
    bool printWarn = false;
    bool printException = false;

    /* get the *correct* JNIEnv by going through our TLS pointer */
    threadEnv = dvmGetJNIEnvForThread();

    /*
     * Verify that the JNIEnv we've been handed matches what we expected
     * to receive.
     */
    if (threadEnv == NULL) {
        LOGE("JNI ERROR: non-VM thread making JNI calls\n");
        // don't set printWarn -- it'll try to call showLocation()
        dvmAbort();
    } else if ((JNIEnvExt*) env != threadEnv) {
        if (dvmThreadSelf()->threadId != threadEnv->envThreadId) {
            LOGE("JNI: threadEnv != thread->env?\n");
            dvmAbort();
        }

        LOGW("JNI WARNING: threadid=%d using env from threadid=%d\n",
            threadEnv->envThreadId, ((JNIEnvExt*)env)->envThreadId);
        printWarn = true;

        /* this is a bad idea -- need to throw as we exit, or abort func */
        //dvmThrowException("Ljava/lang/RuntimeException;",
        //    "invalid use of JNI env ptr");
    } else if (((JNIEnvExt*) env)->self != dvmThreadSelf()) {
        /* correct JNIEnv*; make sure the "self" pointer is correct */
        LOGE("JNI ERROR: env->self != thread-self (%p vs. %p)\n",
            ((JNIEnvExt*) env)->self, dvmThreadSelf());
        dvmAbort();
    }

    /*
     * Check for critical resource misuse.
     */
    switch (flags & kFlag_CritMask) {
    case kFlag_CritOkay:    // okay to call this method
        break;
    case kFlag_CritBad:     // not okay to call
        if (threadEnv->critical) {
            LOGW("JNI WARNING: threadid=%d using JNI after critical get\n",
                threadEnv->envThreadId);
            printWarn = true;
        }
        break;
    case kFlag_CritGet:     // this is a "get" call
        /* don't check here; we allow nested gets */
        threadEnv->critical++;
        break;
    case kFlag_CritRelease: // this is a "release" call
        threadEnv->critical--;
        if (threadEnv->critical < 0) {
            LOGW("JNI WARNING: threadid=%d called too many crit releases\n",
                threadEnv->envThreadId);
            printWarn = true;
        }
        break;
    default:
        assert(false);
    }

    /*
     * Check for raised exceptions.
     */
    if ((flags & kFlag_ExcepOkay) == 0 && dvmCheckException(dvmThreadSelf())) {
        LOGW("JNI WARNING: JNI method called with exception raised\n");
        printWarn = true;
        printException = true;
    }

    if (printWarn)
        showLocation(dvmGetCurrentJNIMethod(), func);
    if (printException) {
        LOGW("Pending exception is:\n");
        dvmLogExceptionStackTrace();
    }
    if (printWarn)
        abortMaybe();
}

/*
 * Verify that the field is of the appropriate type.  If the field has an
 * object type, "obj" is the object we're trying to assign into it.
 *
 * Works for both static and instance fields.
 */
static void checkFieldType(JNIEnv* env, jobject jobj, jfieldID fieldID,
    PrimitiveType prim, bool isStatic, const char* func)
{
    static const char* primNameList[] = {
        "Object/Array", "boolean", "char", "float", "double",
        "byte", "short", "int", "long", "void"
    };
    const char** primNames = &primNameList[1];      // shift up for PRIM_NOT
    Field* field = (Field*) fieldID;
    bool printWarn = false;

    if (fieldID == NULL) {
        LOGE("JNI ERROR: null field ID\n");
        abortMaybe();
    }

    if (field->signature[0] == 'L' || field->signature[0] == '[') {
        Object* obj = dvmDecodeIndirectRef(env, jobj);
        if (obj != NULL) {
            ClassObject* fieldClass =
                dvmFindLoadedClass(field->signature);
            ClassObject* objClass = obj->clazz;

            assert(fieldClass != NULL);
            assert(objClass != NULL);

            if (!dvmInstanceof(objClass, fieldClass)) {
                LOGW("JNI WARNING: field '%s' with type '%s' set with wrong type (%s)\n",
                    field->name, field->signature, objClass->descriptor);
                printWarn = true;
            }
        }
    } else if (field->signature[0] != PRIM_TYPE_TO_LETTER[prim]) {
        LOGW("JNI WARNING: field '%s' with type '%s' set with wrong type (%s)\n",
            field->name, field->signature, primNames[prim]);
        printWarn = true;
    } else if (isStatic && !dvmIsStaticField(field)) {
        if (isStatic)
            LOGW("JNI WARNING: accessing non-static field %s as static\n",
                field->name);
        else
            LOGW("JNI WARNING: accessing static field %s as non-static\n",
                field->name);
        printWarn = true;
    }

    if (printWarn) {
        showLocation(dvmGetCurrentJNIMethod(), func);
        abortMaybe();
    }
}

/*
 * Verify that "jobj" is a valid object, and that it's an object that JNI
 * is allowed to know about.  We allow NULL references.
 *
 * Must be in "running" mode before calling here.
 */
static void checkObject0(JNIEnv* env, jobject jobj, const char* func)
{
    UNUSED_PARAMETER(env);
    bool printWarn = false;

    if (jobj == NULL)
        return;

    if (dvmIsWeakGlobalRef(jobj)) {
        /*
         * Normalize and continue.  This will tell us if the PhantomReference
         * object is valid.
         */
        jobj = dvmNormalizeWeakGlobalRef((jweak) jobj);
    }

    if (dvmGetJNIRefType(env, jobj) == JNIInvalidRefType) {
        LOGW("JNI WARNING: %p is not a valid JNI reference\n", jobj);
        printWarn = true;
    } else {
        Object* obj = dvmDecodeIndirectRef(env, jobj);

        if (obj == NULL || !dvmIsValidObject(obj)) {
            LOGW("JNI WARNING: native code passing in bad object %p %p (%s)\n",
                jobj, obj, func);
            printWarn = true;
        }
    }

    if (printWarn) {
        showLocation(dvmGetCurrentJNIMethod(), func);
        abortMaybe();
    }
}

/*
 * Verify that "jobj" is a valid object, and that it's an object that JNI
 * is allowed to know about.  We allow NULL references.
 *
 * Switches to "running" mode before performing checks.
 */
static void checkObject(JNIEnv* env, jobject jobj, const char* func)
{
    JNI_ENTER();
    checkObject0(env, jobj, func);
    JNI_EXIT();
}

/*
 * Verify that "clazz" actually points to a class object.  (Also performs
 * checkObject.)
 *
 * We probably don't need to identify where we're being called from,
 * because the VM is most likely about to crash and leave a core dump
 * if something is wrong.
 *
 * Because we're looking at an object on the GC heap, we have to switch
 * to "running" mode before doing the checks.
 */
static void checkClass(JNIEnv* env, jclass jclazz, const char* func)
{
    JNI_ENTER();
    bool printWarn = false;

    Object* obj = dvmDecodeIndirectRef(env, jclazz);

    if (obj == NULL) {
        LOGW("JNI WARNING: received null jclass\n");
        printWarn = true;
    } else if (!dvmIsValidObject(obj)) {
        LOGW("JNI WARNING: jclass points to invalid object %p\n", obj);
        printWarn = true;
    } else if (obj->clazz != gDvm.classJavaLangClass) {
        LOGW("JNI WARNING: jclass arg is not a Class reference "
             "(%p is instance of %s)\n",
            jclazz, obj->clazz->descriptor);
        printWarn = true;
    }
    JNI_EXIT();

    if (printWarn)
        abortMaybe();
    else
        checkObject(env, jclazz, func);
}

/*
 * Verify that "str" is non-NULL and points to a String object.
 *
 * Since we're dealing with objects, switch to "running" mode.
 */
static void checkString(JNIEnv* env, jstring jstr, const char* func)
{
    JNI_ENTER();
    bool printWarn = false;

    Object* obj = dvmDecodeIndirectRef(env, jstr);

    if (obj == NULL) {
        LOGW("JNI WARNING: received null jstring (%s)\n", func);
        printWarn = true;
    } else if (obj->clazz != gDvm.classJavaLangString) {
        /*
         * TODO: we probably should test dvmIsValidObject first, because
         * this will crash if "obj" is non-null but pointing to an invalid
         * memory region.  However, the "is valid" test is a little slow,
         * we're doing it again over in checkObject().
         */
        if (dvmIsValidObject(obj))
            LOGW("JNI WARNING: jstring %p points to non-string object (%s)\n",
                jstr, func);
        else
            LOGW("JNI WARNING: jstring %p is bogus (%s)\n", jstr, func);
        printWarn = true;
    }
    JNI_EXIT();

    if (printWarn)
        abortMaybe();
    else
        checkObject(env, jstr, func);
}

/*
 * Verify that "bytes" points to valid "modified UTF-8" data.
 */
static void checkUtfString(JNIEnv* env, const char* bytes, bool nullOk,
    const char* func)
{
    const char* origBytes = bytes;

    if (bytes == NULL) {
        if (!nullOk) {
            LOGW("JNI WARNING: unexpectedly null UTF string\n");
            goto fail;
        }

        return;
    }

    while (*bytes != '\0') {
        u1 utf8 = *(bytes++);
        // Switch on the high four bits.
        switch (utf8 >> 4) {
            case 0x00:
            case 0x01:
            case 0x02:
            case 0x03:
            case 0x04:
            case 0x05:
            case 0x06:
            case 0x07: {
                // Bit pattern 0xxx. No need for any extra bytes.
                break;
            }
            case 0x08:
            case 0x09:
            case 0x0a:
            case 0x0b:
            case 0x0f: {
                /*
                 * Bit pattern 10xx or 1111, which are illegal start bytes.
                 * Note: 1111 is valid for normal UTF-8, but not the
                 * modified UTF-8 used here.
                 */
                LOGW("JNI WARNING: illegal start byte 0x%x\n", utf8);
                goto fail;
            }
            case 0x0e: {
                // Bit pattern 1110, so there are two additional bytes.
                utf8 = *(bytes++);
                if ((utf8 & 0xc0) != 0x80) {
                    LOGW("JNI WARNING: illegal continuation byte 0x%x\n", utf8);
                    goto fail;
                }
                // Fall through to take care of the final byte.
            }
            case 0x0c:
            case 0x0d: {
                // Bit pattern 110x, so there is one additional byte.
                utf8 = *(bytes++);
                if ((utf8 & 0xc0) != 0x80) {
                    LOGW("JNI WARNING: illegal continuation byte 0x%x\n", utf8);
                    goto fail;
                }
                break;
            }
        }
    }

    return;

fail:
    LOGW("             string: '%s'\n", origBytes);
    showLocation(dvmGetCurrentJNIMethod(), func);
    abortMaybe();
}

/*
 * In some circumstances the VM will screen class names, but it doesn't
 * for class lookup.  When things get bounced through a class loader, they
 * can actually get normalized a couple of times; as a result, passing in
 * a class name like "java.lang.Thread" instead of "java/lang/Thread" will
 * work in some circumstances.
 *
 * This is incorrect and could cause strange behavior or compatibility
 * problems, so we want to screen that out here.
 *
 * We expect "full-qualified" class names, like "java/lang/Thread" or
 * "[Ljava/lang/Object;".
 */
static void checkClassName(JNIEnv* env, const char* className, const char* func)
{
    const char* cp;

    /* quick check for illegal chars */
    cp = className;
    while (*cp != '\0') {
        if (*cp == '.')     /* catch "java.lang.String" */
            goto fail;
        cp++;
    }
    if (*(cp-1) == ';' && *className == 'L')
        goto fail;         /* catch "Ljava/lang/String;" */

    // TODO: need a more rigorous check here

    return;

fail:
    LOGW("JNI WARNING: illegal class name '%s' (%s)\n", className, func);
    LOGW("             (should be formed like 'java/lang/String')\n");
    abortMaybe();
}

/*
 * Verify that "array" is non-NULL and points to an Array object.
 *
 * Since we're dealing with objects, switch to "running" mode.
 */
static void checkArray(JNIEnv* env, jarray jarr, const char* func)
{
    JNI_ENTER();
    bool printWarn = false;

    Object* obj = dvmDecodeIndirectRef(env, jarr);

    if (obj == NULL) {
        LOGW("JNI WARNING: received null array (%s)\n", func);
        printWarn = true;
    } else if (obj->clazz->descriptor[0] != '[') {
        if (dvmIsValidObject(obj))
            LOGW("JNI WARNING: jarray %p points to non-array object (%s)\n",
                jarr, obj->clazz->descriptor);
        else
            LOGW("JNI WARNING: jarray %p is bogus\n", jarr);
        printWarn = true;
    }

    JNI_EXIT();

    if (printWarn)
        abortMaybe();
    else
        checkObject(env, jarr, func);
}

/*
 * Verify that the "mode" argument passed to a primitive array Release
 * function is one of the valid values.
 */
static void checkReleaseMode(JNIEnv* env, jint mode, const char* func)
{
    if (mode != 0 && mode != JNI_COMMIT && mode != JNI_ABORT) {
        LOGW("JNI WARNING: bad value for mode (%d) (%s)\n", mode, func);
        abortMaybe();
    }
}

/*
 * Verify that the length argument to array-creation calls is >= 0.
 */
static void checkLengthPositive(JNIEnv* env, jsize length, const char* func)
{
    if (length < 0) {
        LOGW("JNI WARNING: negative length for array allocation (%s)\n", func);
        abortMaybe();
    }
}

/*
 * Verify that the pointer value is non-NULL.
 */
static void checkNonNull(JNIEnv* env, const void* ptr, const char* func)
{
    if (ptr == NULL) {
        LOGW("JNI WARNING: invalid null pointer (%s)\n", func);
        abortMaybe();
    }
}

/*
 * Verify that the method's return type matches the type of call.
 *
 * "expectedSigByte" will be 'L' for all objects, including arrays.
 */
static void checkSig(JNIEnv* env, jmethodID methodID, char expectedSigByte,
    bool isStatic, const char* func)
{
    const Method* meth = (const Method*) methodID;
    bool printWarn = false;

    if (expectedSigByte != meth->shorty[0]) {
        LOGW("JNI WARNING: expected return type '%c'\n", expectedSigByte);
        printWarn = true;
    } else if (isStatic && !dvmIsStaticMethod(meth)) {
        if (isStatic)
            LOGW("JNI WARNING: calling non-static method with static call\n");
        else
            LOGW("JNI WARNING: calling static method with non-static call\n");
        printWarn = true;
    }

    if (printWarn) {
        char* desc = dexProtoCopyMethodDescriptor(&meth->prototype);
        LOGW("             calling %s.%s %s\n",
            meth->clazz->descriptor, meth->name, desc);
        free(desc);
        showLocation(dvmGetCurrentJNIMethod(), func);
        abortMaybe();
    }
}

/*
 * Verify that this static field ID is valid for this class.
 */
static void checkStaticFieldID(JNIEnv* env, jclass jclazz, jfieldID fieldID)
{
    ClassObject* clazz = (ClassObject*) dvmDecodeIndirectRef(env, jclazz);
    StaticField* base = &clazz->sfields[0];
    int fieldCount = clazz->sfieldCount;

    if ((StaticField*) fieldID < base ||
        (StaticField*) fieldID >= base + fieldCount)
    {
        LOGW("JNI WARNING: static fieldID %p not valid for class %s\n",
            fieldID, clazz->descriptor);
        LOGW("             base=%p count=%d\n", base, fieldCount);
        abortMaybe();
    }
}

/*
 * Verify that this instance field ID is valid for this object.
 */
static void checkInstanceFieldID(JNIEnv* env, jobject jobj, jfieldID fieldID,
    const char* func)
{
    JNI_ENTER();

    if (jobj == NULL) {
        LOGW("JNI WARNING: invalid null object (%s)\n", func);
        abortMaybe();
        goto bail;
    }

    Object* obj = dvmDecodeIndirectRef(env, jobj);
    ClassObject* clazz = obj->clazz;

    /*
     * Check this class and all of its superclasses for a matching field.
     * Don't need to scan interfaces.
     */
    while (clazz != NULL) {
        if ((InstField*) fieldID >= clazz->ifields &&
            (InstField*) fieldID < clazz->ifields + clazz->ifieldCount)
        {
            goto bail;
        }

        clazz = clazz->super;
    }

    LOGW("JNI WARNING: inst fieldID %p not valid for class %s\n",
        fieldID, obj->clazz->descriptor);
    abortMaybe();

bail:
    JNI_EXIT();
}

/*
 * Verify that "methodID" is appropriate for "jobj".
 *
 * Make sure the object is an instance of the method's declaring class.
 * (Note the methodID might point to a declaration in an interface; this
 * will be handled automatically by the instanceof check.)
 */
static void checkVirtualMethod(JNIEnv* env, jobject jobj, jmethodID methodID,
    const char* func)
{
    JNI_ENTER();

    Object* obj = dvmDecodeIndirectRef(env, jobj);
    const Method* meth = (const Method*) methodID;

    if (!dvmInstanceof(obj->clazz, meth->clazz)) {
        LOGW("JNI WARNING: can't call %s.%s on instance of %s\n",
            meth->clazz->descriptor, meth->name, obj->clazz->descriptor);
        abortMaybe();
    }

    JNI_EXIT();
}

/*
 * Verify that "methodID" is appropriate for "clazz".
 *
 * A mismatch isn't dangerous, because the method defines the class.  In
 * fact, jclazz is unused in the implementation.  It's best if we don't
 * allow bad code in the system though.
 *
 * Instances of "jclazz" must be instances of the method's declaring class.
 */
static void checkStaticMethod(JNIEnv* env, jclass jclazz, jmethodID methodID,
    const char* func)
{
    JNI_ENTER();

    ClassObject* clazz = (ClassObject*) dvmDecodeIndirectRef(env, jclazz);
    const Method* meth = (const Method*) methodID;

    if (!dvmInstanceof(clazz, meth->clazz)) {
        LOGW("JNI WARNING: can't call static %s.%s on class %s\n",
            meth->clazz->descriptor, meth->name, clazz->descriptor);
        // no abort
    }

    JNI_EXIT();
}

/*
 * Verify that the reference arguments being passed in are appropriate for
 * this method.
 *
 * At a minimum we want to make sure that the argument is a valid
 * reference.  We can also do a class lookup on the method signature
 * and verify that the object is an instance of the appropriate class,
 * but that's more expensive.
 *
 * The basic tests are redundant when indirect references are enabled,
 * since reference arguments must always be converted explicitly.  An
 * instanceof test would not be redundant, but we're not doing that at
 * this time.
 */
static void checkMethodArgsV(JNIEnv* env, jmethodID methodID, va_list args,
    const char* func)
{
#ifndef USE_INDIRECT_REF
    JNI_ENTER();

    const Method* meth = (const Method*) methodID;
    const char* desc = meth->shorty;

    LOGV("V-checking %s.%s:%s...\n", meth->clazz->descriptor, meth->name, desc);

    while (*++desc != '\0') {       /* pre-incr to skip return type */
        switch (*desc) {
        case 'L':
            {     /* 'shorty' descr uses L for all refs, incl array */
                jobject argObj = va_arg(args, jobject);
                checkObject0(env, argObj, func);
            }
            break;
        case 'D':       /* 8-byte double */
        case 'J':       /* 8-byte long */
        case 'F':       /* floats normalized to doubles */
            (void) va_arg(args, u8);
            break;
        default:        /* Z B C S I -- all passed as 32-bit integers */
            (void) va_arg(args, u4);
            break;
        }
    }

    JNI_EXIT();
#endif
}

/*
 * Same purpose as checkMethodArgsV, but with arguments in an array of
 * jvalue structs.
 */
static void checkMethodArgsA(JNIEnv* env, jmethodID methodID, jvalue* args,
    const char* func)
{
#ifndef USE_INDIRECT_REF
    JNI_ENTER();

    const Method* meth = (const Method*) methodID;
    const char* desc = meth->shorty;
    int idx = 0;

    LOGV("A-checking %s.%s:%s...\n", meth->clazz->descriptor, meth->name, desc);

    while (*++desc != '\0') {       /* pre-incr to skip return type */
        if (*desc == 'L') {
            jobject argObj = args[idx].l;
            checkObject0(env, argObj, func);
        }

        idx++;
    }

    JNI_EXIT();
#endif
}


/*
 * ===========================================================================
 *      Guarded arrays
 * ===========================================================================
 */

#define kGuardLen       512         /* must be multiple of 2 */
#define kGuardPattern   0xd5e3      /* uncommon values; d5e3d5e3 invalid addr */
#define kGuardMagic     0xffd5aa96
#define kGuardExtra     sizeof(GuardExtra)

/* this gets tucked in at the start of the buffer; struct size must be even */
typedef struct GuardExtra {
    u4          magic;
    uLong       adler;
    size_t      originalLen;
    const void* originalPtr;
} GuardExtra;

/* find the GuardExtra given the pointer into the "live" data */
inline static GuardExtra* getGuardExtra(const void* dataBuf)
{
    u1* fullBuf = ((u1*) dataBuf) - kGuardLen / 2;
    return (GuardExtra*) fullBuf;
}

/*
 * Create an oversized buffer to hold the contents of "buf".  Copy it in,
 * filling in the area around it with guard data.
 *
 * We use a 16-bit pattern to make a rogue memset less likely to elude us.
 */
static void* createGuardedCopy(const void* buf, size_t len, bool modOkay)
{
    GuardExtra* pExtra;
    size_t newLen = (len + kGuardLen +1) & ~0x01;
    u1* newBuf;
    u2* pat;
    int i;

    newBuf = (u1*)malloc(newLen);
    if (newBuf == NULL) {
        LOGE("createGuardedCopy failed on alloc of %d bytes\n", newLen);
        dvmAbort();
    }

    /* fill it in with a pattern */
    pat = (u2*) newBuf;
    for (i = 0; i < (int)newLen / 2; i++)
        *pat++ = kGuardPattern;

    /* copy the data in; note "len" could be zero */
    memcpy(newBuf + kGuardLen / 2, buf, len);

    /* if modification is not expected, grab a checksum */
    uLong adler = 0;
    if (!modOkay) {
        adler = adler32(0L, Z_NULL, 0);
        adler = adler32(adler, buf, len);
        *(uLong*)newBuf = adler;
    }

    pExtra = (GuardExtra*) newBuf;
    pExtra->magic = kGuardMagic;
    pExtra->adler = adler;
    pExtra->originalPtr = buf;
    pExtra->originalLen = len;

    return newBuf + kGuardLen / 2;
}

/*
 * Verify the guard area and, if "modOkay" is false, that the data itself
 * has not been altered.
 *
 * The caller has already checked that "dataBuf" is non-NULL.
 */
static bool checkGuardedCopy(const void* dataBuf, bool modOkay)
{
    static const u4 kMagicCmp = kGuardMagic;
    const u1* fullBuf = ((const u1*) dataBuf) - kGuardLen / 2;
    const GuardExtra* pExtra = getGuardExtra(dataBuf);
    size_t len;
    const u2* pat;
    int i;

    /*
     * Before we do anything with "pExtra", check the magic number.  We
     * do the check with memcmp rather than "==" in case the pointer is
     * unaligned.  If it points to completely bogus memory we're going
     * to crash, but there's no easy way around that.
     */
    if (memcmp(&pExtra->magic, &kMagicCmp, 4) != 0) {
        u1 buf[4];
        memcpy(buf, &pExtra->magic, 4);
        LOGE("JNI: guard magic does not match (found 0x%02x%02x%02x%02x) "
             "-- incorrect data pointer %p?\n",
            buf[3], buf[2], buf[1], buf[0], dataBuf); /* assume little endian */
        return false;
    }

    len = pExtra->originalLen;

    /* check bottom half of guard; skip over optional checksum storage */
    pat = (u2*) fullBuf;
    for (i = kGuardExtra / 2; i < (int) (kGuardLen / 2 - kGuardExtra) / 2; i++)
    {
        if (pat[i] != kGuardPattern) {
            LOGE("JNI: guard pattern(1) disturbed at %p + %d\n",
                fullBuf, i*2);
            return false;
        }
    }

    int offset = kGuardLen / 2 + len;
    if (offset & 0x01) {
        /* odd byte; expected value depends on endian-ness of host */
        const u2 patSample = kGuardPattern;
        if (fullBuf[offset] != ((const u1*) &patSample)[1]) {
            LOGE("JNI: guard pattern disturbed in odd byte after %p "
                 "(+%d) 0x%02x 0x%02x\n",
                fullBuf, offset, fullBuf[offset], ((const u1*) &patSample)[1]);
            return false;
        }
        offset++;
    }

    /* check top half of guard */
    pat = (u2*) (fullBuf + offset);
    for (i = 0; i < kGuardLen / 4; i++) {
        if (pat[i] != kGuardPattern) {
            LOGE("JNI: guard pattern(2) disturbed at %p + %d\n",
                fullBuf, offset + i*2);
            return false;
        }
    }

    /*
     * If modification is not expected, verify checksum.  Strictly speaking
     * this is wrong: if we told the client that we made a copy, there's no
     * reason they can't alter the buffer.
     */
    if (!modOkay) {
        uLong adler = adler32(0L, Z_NULL, 0);
        adler = adler32(adler, dataBuf, len);
        if (pExtra->adler != adler) {
            LOGE("JNI: buffer modified (0x%08lx vs 0x%08lx) at addr %p\n",
                pExtra->adler, adler, dataBuf);
            return false;
        }
    }

    return true;
}

/*
 * Free up the guard buffer, scrub it, and return the original pointer.
 */
static void* freeGuardedCopy(void* dataBuf)
{
    u1* fullBuf = ((u1*) dataBuf) - kGuardLen / 2;
    const GuardExtra* pExtra = getGuardExtra(dataBuf);
    void* originalPtr = (void*) pExtra->originalPtr;
    size_t len = pExtra->originalLen;

    memset(dataBuf, 0xdd, len);
    free(fullBuf);
    return originalPtr;
}

/*
 * Just pull out the original pointer.
 */
static void* getGuardedCopyOriginalPtr(const void* dataBuf)
{
    const GuardExtra* pExtra = getGuardExtra(dataBuf);
    return (void*) pExtra->originalPtr;
}

/*
 * Grab the data length.
 */
static size_t getGuardedCopyOriginalLen(const void* dataBuf)
{
    const GuardExtra* pExtra = getGuardExtra(dataBuf);
    return pExtra->originalLen;
}

/*
 * Return the width, in bytes, of a primitive type.
 */
static int dvmPrimitiveTypeWidth(PrimitiveType primType)
{
    static const int lengths[PRIM_MAX] = {
        1,      // boolean
        2,      // char
        4,      // float
        8,      // double
        1,      // byte
        2,      // short
        4,      // int
        8,      // long
        -1,     // void
    };
    assert(primType >= 0 && primType < PRIM_MAX);
    return lengths[primType];
}

/*
 * Create a guarded copy of a primitive array.  Modifications to the copied
 * data are allowed.  Returns a pointer to the copied data.
 */
static void* createGuardedPACopy(JNIEnv* env, const jarray jarr,
    jboolean* isCopy)
{
    ArrayObject* arrObj = (ArrayObject*) dvmDecodeIndirectRef(env, jarr);
    PrimitiveType primType = arrObj->obj.clazz->elementClass->primitiveType;
    int len = arrObj->length * dvmPrimitiveTypeWidth(primType);
    void* result;

    result = createGuardedCopy(arrObj->contents, len, true);

    if (isCopy != NULL)
        *isCopy = JNI_TRUE;

    return result;
}

/*
 * Perform the array "release" operation, which may or may not copy data
 * back into the VM, and may or may not release the underlying storage.
 */
static void* releaseGuardedPACopy(JNIEnv* env, jarray jarr, void* dataBuf,
    int mode)
{
    ArrayObject* arrObj = (ArrayObject*) dvmDecodeIndirectRef(env, jarr);
    bool release, copyBack;
    u1* result;

    if (!checkGuardedCopy(dataBuf, true)) {
        LOGE("JNI: failed guarded copy check in releaseGuardedPACopy\n");
        abortMaybe();
        return NULL;
    }

    switch (mode) {
    case 0:
        release = copyBack = true;
        break;
    case JNI_ABORT:
        release = true;
        copyBack = false;
        break;
    case JNI_COMMIT:
        release = false;
        copyBack = true;
        break;
    default:
        LOGE("JNI: bad release mode %d\n", mode);
        dvmAbort();
        return NULL;
    }

    if (copyBack) {
        size_t len = getGuardedCopyOriginalLen(dataBuf);
        memcpy(arrObj->contents, dataBuf, len);
    }

    if (release) {
        result = (u1*) freeGuardedCopy(dataBuf);
    } else {
        result = (u1*) getGuardedCopyOriginalPtr(dataBuf);
    }

    /* pointer is to the array contents; back up to the array object */
    result -= offsetof(ArrayObject, contents);

    return result;
}


/*
 * ===========================================================================
 *      JNI functions
 * ===========================================================================
 */

static jint Check_GetVersion(JNIEnv* env)
{
    CHECK_ENTER(env, kFlag_Default);
    jint result;
    result = BASE_ENV(env)->GetVersion(env);
    CHECK_EXIT(env);
    return result;
}

static jclass Check_DefineClass(JNIEnv* env, const char* name, jobject loader,
    const jbyte* buf, jsize bufLen)
{
    CHECK_ENTER(env, kFlag_Default);
    CHECK_OBJECT(env, loader);
    CHECK_UTF_STRING(env, name, false);
    CHECK_CLASS_NAME(env, name);
    jclass result;
    result = BASE_ENV(env)->DefineClass(env, name, loader, buf, bufLen);
    CHECK_EXIT(env);
    return result;
}

static jclass Check_FindClass(JNIEnv* env, const char* name)
{
    CHECK_ENTER(env, kFlag_Default);
    CHECK_UTF_STRING(env, name, false);
    CHECK_CLASS_NAME(env, name);
    jclass result;
    result = BASE_ENV(env)->FindClass(env, name);
    CHECK_EXIT(env);
    return result;
}

static jclass Check_GetSuperclass(JNIEnv* env, jclass clazz)
{
    CHECK_ENTER(env, kFlag_Default);
    CHECK_CLASS(env, clazz);
    jclass result;
    result = BASE_ENV(env)->GetSuperclass(env, clazz);
    CHECK_EXIT(env);
    return result;
}

static jboolean Check_IsAssignableFrom(JNIEnv* env, jclass clazz1,
    jclass clazz2)
{
    CHECK_ENTER(env, kFlag_Default);
    CHECK_CLASS(env, clazz1);
    CHECK_CLASS(env, clazz2);
    jboolean result;
    result = BASE_ENV(env)->IsAssignableFrom(env, clazz1, clazz2);
    CHECK_EXIT(env);
    return result;
}

static jmethodID Check_FromReflectedMethod(JNIEnv* env, jobject method)
{
    CHECK_ENTER(env, kFlag_Default);
    CHECK_OBJECT(env, method);
    jmethodID result;
    result = BASE_ENV(env)->FromReflectedMethod(env, method);
    CHECK_EXIT(env);
    return result;
}

static jfieldID Check_FromReflectedField(JNIEnv* env, jobject field)
{
    CHECK_ENTER(env, kFlag_Default);
    CHECK_OBJECT(env, field);
    jfieldID result;
    result = BASE_ENV(env)->FromReflectedField(env, field);
    CHECK_EXIT(env);
    return result;
}

static jobject Check_ToReflectedMethod(JNIEnv* env, jclass cls,
    jmethodID methodID, jboolean isStatic)
{
    CHECK_ENTER(env, kFlag_Default);
    CHECK_CLASS(env, cls);
    jobject result;
    result = BASE_ENV(env)->ToReflectedMethod(env, cls, methodID, isStatic);
    CHECK_EXIT(env);
    return result;
}

static jobject Check_ToReflectedField(JNIEnv* env, jclass cls, jfieldID fieldID,
    jboolean isStatic)
{
    CHECK_ENTER(env, kFlag_Default);
    CHECK_CLASS(env, cls);
    jobject result;
    result = BASE_ENV(env)->ToReflectedField(env, cls, fieldID, isStatic);
    CHECK_EXIT(env);
    return result;
}

static jint Check_Throw(JNIEnv* env, jthrowable obj)
{
    CHECK_ENTER(env, kFlag_Default);
    CHECK_OBJECT(env, obj);
    jint result;
    result = BASE_ENV(env)->Throw(env, obj);
    CHECK_EXIT(env);
    return result;
}

static jint Check_ThrowNew(JNIEnv* env, jclass clazz, const char* message)
{
    CHECK_ENTER(env, kFlag_Default);
    CHECK_CLASS(env, clazz);
    CHECK_UTF_STRING(env, message, true);
    jint result;
    result = BASE_ENV(env)->ThrowNew(env, clazz, message);
    CHECK_EXIT(env);
    return result;
}

static jthrowable Check_ExceptionOccurred(JNIEnv* env)
{
    CHECK_ENTER(env, kFlag_ExcepOkay);
    jthrowable result;
    result = BASE_ENV(env)->ExceptionOccurred(env);
    CHECK_EXIT(env);
    return result;
}

static void Check_ExceptionDescribe(JNIEnv* env)
{
    CHECK_ENTER(env, kFlag_ExcepOkay);
    BASE_ENV(env)->ExceptionDescribe(env);
    CHECK_EXIT(env);
}

static void Check_ExceptionClear(JNIEnv* env)
{
    CHECK_ENTER(env, kFlag_ExcepOkay);
    BASE_ENV(env)->ExceptionClear(env);
    CHECK_EXIT(env);
}

static void Check_FatalError(JNIEnv* env, const char* msg)
{
    CHECK_ENTER(env, kFlag_Default);
    CHECK_UTF_STRING(env, msg, true);
    BASE_ENV(env)->FatalError(env, msg);
    CHECK_EXIT(env);
}

static jint Check_PushLocalFrame(JNIEnv* env, jint capacity)
{
    CHECK_ENTER(env, kFlag_Default | kFlag_ExcepOkay);
    jint result;
    result = BASE_ENV(env)->PushLocalFrame(env, capacity);
    CHECK_EXIT(env);
    return result;
}

static jobject Check_PopLocalFrame(JNIEnv* env, jobject res)
{
    CHECK_ENTER(env, kFlag_Default | kFlag_ExcepOkay);
    CHECK_OBJECT(env, res);
    jobject result;
    result = BASE_ENV(env)->PopLocalFrame(env, res);
    CHECK_EXIT(env);
    return result;
}

static jobject Check_NewGlobalRef(JNIEnv* env, jobject obj)
{
    CHECK_ENTER(env, kFlag_Default);
    CHECK_OBJECT(env, obj);
    jobject result;
    result = BASE_ENV(env)->NewGlobalRef(env, obj);
    CHECK_EXIT(env);
    return result;
}

static void Check_DeleteGlobalRef(JNIEnv* env, jobject globalRef)
{
    CHECK_ENTER(env, kFlag_Default | kFlag_ExcepOkay);
    CHECK_OBJECT(env, globalRef);
#ifdef USE_INDIRECT_REF
    if (globalRef != NULL &&
        dvmGetJNIRefType(env, globalRef) != JNIGlobalRefType)
    {
        LOGW("JNI WARNING: DeleteGlobalRef on non-global %p (type=%d)\n",
            globalRef, dvmGetJNIRefType(env, globalRef));
        abortMaybe();
    } else
#endif
    {
        BASE_ENV(env)->DeleteGlobalRef(env, globalRef);
    }
    CHECK_EXIT(env);
}

static jobject Check_NewLocalRef(JNIEnv* env, jobject ref)
{
    CHECK_ENTER(env, kFlag_Default);
    CHECK_OBJECT(env, ref);
    jobject result;
    result = BASE_ENV(env)->NewLocalRef(env, ref);
    CHECK_EXIT(env);
    return result;
}

static void Check_DeleteLocalRef(JNIEnv* env, jobject localRef)
{
    CHECK_ENTER(env, kFlag_Default | kFlag_ExcepOkay);
    CHECK_OBJECT(env, localRef);
#ifdef USE_INDIRECT_REF
    if (localRef != NULL &&
        dvmGetJNIRefType(env, localRef) != JNILocalRefType)
    {
        LOGW("JNI WARNING: DeleteLocalRef on non-local %p (type=%d)\n",
            localRef, dvmGetJNIRefType(env, localRef));
        abortMaybe();
    } else
#endif
    {
        BASE_ENV(env)->DeleteLocalRef(env, localRef);
    }
    CHECK_EXIT(env);
}

static jint Check_EnsureLocalCapacity(JNIEnv *env, jint capacity)
{
    CHECK_ENTER(env, kFlag_Default);
    jint result;
    result = BASE_ENV(env)->EnsureLocalCapacity(env, capacity);
    CHECK_EXIT(env);
    return result;
}

static jboolean Check_IsSameObject(JNIEnv* env, jobject ref1, jobject ref2)
{
    CHECK_ENTER(env, kFlag_Default);
    CHECK_OBJECT(env, ref1);
    CHECK_OBJECT(env, ref2);
    jboolean result;
    result = BASE_ENV(env)->IsSameObject(env, ref1, ref2);
    CHECK_EXIT(env);
    return result;
}

static jobject Check_AllocObject(JNIEnv* env, jclass clazz)
{
    CHECK_ENTER(env, kFlag_Default);
    CHECK_CLASS(env, clazz);
    jobject result;
    result = BASE_ENV(env)->AllocObject(env, clazz);
    CHECK_EXIT(env);
    return result;
}

static jobject Check_NewObject(JNIEnv* env, jclass clazz, jmethodID methodID,
    ...)
{
    CHECK_ENTER(env, kFlag_Default);
    CHECK_CLASS(env, clazz);
    jobject result;
    va_list args, tmpArgs;

    va_start(args, methodID);

    va_copy(tmpArgs, args);
    CHECK_METHOD_ARGS_V(env, methodID, tmpArgs);
    va_end(tmpArgs);

    result = BASE_ENV(env)->NewObjectV(env, clazz, methodID, args);
    va_end(args);

    CHECK_EXIT(env);
    return result;
}
static jobject Check_NewObjectV(JNIEnv* env, jclass clazz, jmethodID methodID,
    va_list args)
{
    CHECK_ENTER(env, kFlag_Default);
    CHECK_CLASS(env, clazz);
    jobject result;

    va_list tmpArgs;
    va_copy(tmpArgs, args);
    CHECK_METHOD_ARGS_V(env, methodID, tmpArgs);
    va_end(tmpArgs);

    result = BASE_ENV(env)->NewObjectV(env, clazz, methodID, args);
    CHECK_EXIT(env);
    return result;
}
static jobject Check_NewObjectA(JNIEnv* env, jclass clazz, jmethodID methodID,
    jvalue* args)
{
    CHECK_ENTER(env, kFlag_Default);
    CHECK_CLASS(env, clazz);
    jobject result;

    CHECK_METHOD_ARGS_A(env, methodID, args);
    result = BASE_ENV(env)->NewObjectA(env, clazz, methodID, args);
    CHECK_EXIT(env);
    return result;
}

static jclass Check_GetObjectClass(JNIEnv* env, jobject obj)
{
    CHECK_ENTER(env, kFlag_Default);
    CHECK_OBJECT(env, obj);
    jclass result;
    result = BASE_ENV(env)->GetObjectClass(env, obj);
    CHECK_EXIT(env);
    return result;
}

static jboolean Check_IsInstanceOf(JNIEnv* env, jobject obj, jclass clazz)
{
    CHECK_ENTER(env, kFlag_Default);
    CHECK_OBJECT(env, obj);
    CHECK_CLASS(env, clazz);
    jboolean result;
    result = BASE_ENV(env)->IsInstanceOf(env, obj, clazz);
    CHECK_EXIT(env);
    return result;
}

static jmethodID Check_GetMethodID(JNIEnv* env, jclass clazz, const char* name,
    const char* sig)
{
    CHECK_ENTER(env, kFlag_Default);
    CHECK_CLASS(env, clazz);
    CHECK_UTF_STRING(env, name, false);
    CHECK_UTF_STRING(env, sig, false);
    jmethodID result;
    result = BASE_ENV(env)->GetMethodID(env, clazz, name, sig);
    CHECK_EXIT(env);
    return result;
}

static jfieldID Check_GetFieldID(JNIEnv* env, jclass clazz,
    const char* name, const char* sig)
{
    CHECK_ENTER(env, kFlag_Default);
    CHECK_CLASS(env, clazz);
    CHECK_UTF_STRING(env, name, false);
    CHECK_UTF_STRING(env, sig, false);
    jfieldID result;
    result = BASE_ENV(env)->GetFieldID(env, clazz, name, sig);
    CHECK_EXIT(env);
    return result;
}

static jmethodID Check_GetStaticMethodID(JNIEnv* env, jclass clazz,
    const char* name, const char* sig)
{
    CHECK_ENTER(env, kFlag_Default);
    CHECK_CLASS(env, clazz);
    CHECK_UTF_STRING(env, name, false);
    CHECK_UTF_STRING(env, sig, false);
    jmethodID result;
    result = BASE_ENV(env)->GetStaticMethodID(env, clazz, name, sig);
    CHECK_EXIT(env);
    return result;
}

static jfieldID Check_GetStaticFieldID(JNIEnv* env, jclass clazz,
    const char* name, const char* sig)
{
    CHECK_ENTER(env, kFlag_Default);
    CHECK_CLASS(env, clazz);
    CHECK_UTF_STRING(env, name, false);
    CHECK_UTF_STRING(env, sig, false);
    jfieldID result;
    result = BASE_ENV(env)->GetStaticFieldID(env, clazz, name, sig);
    CHECK_EXIT(env);
    return result;
}

#define GET_STATIC_TYPE_FIELD(_ctype, _jname)                               \
    static _ctype Check_GetStatic##_jname##Field(JNIEnv* env, jclass clazz, \
        jfieldID fieldID)                                                   \
    {                                                                       \
        CHECK_ENTER(env, kFlag_Default);                                    \
        CHECK_CLASS(env, clazz);                                            \
        _ctype result;                                                      \
        checkStaticFieldID(env, clazz, fieldID);                            \
        result = BASE_ENV(env)->GetStatic##_jname##Field(env, clazz,        \
            fieldID);                                                       \
        CHECK_EXIT(env);                                                    \
        return result;                                                      \
    }
GET_STATIC_TYPE_FIELD(jobject, Object);
GET_STATIC_TYPE_FIELD(jboolean, Boolean);
GET_STATIC_TYPE_FIELD(jbyte, Byte);
GET_STATIC_TYPE_FIELD(jchar, Char);
GET_STATIC_TYPE_FIELD(jshort, Short);
GET_STATIC_TYPE_FIELD(jint, Int);
GET_STATIC_TYPE_FIELD(jlong, Long);
GET_STATIC_TYPE_FIELD(jfloat, Float);
GET_STATIC_TYPE_FIELD(jdouble, Double);

#define SET_STATIC_TYPE_FIELD(_ctype, _jname, _ftype)                       \
    static void Check_SetStatic##_jname##Field(JNIEnv* env, jclass clazz,   \
        jfieldID fieldID, _ctype value)                                     \
    {                                                                       \
        CHECK_ENTER(env, kFlag_Default);                                    \
        CHECK_CLASS(env, clazz);                                            \
        checkStaticFieldID(env, clazz, fieldID);                            \
        /* "value" arg only used when type == ref */                        \
        CHECK_FIELD_TYPE(env, (jobject)(u4)value, fieldID, _ftype, true);   \
        BASE_ENV(env)->SetStatic##_jname##Field(env, clazz, fieldID,        \
            value);                                                         \
        CHECK_EXIT(env);                                                    \
    }
SET_STATIC_TYPE_FIELD(jobject, Object, PRIM_NOT);
SET_STATIC_TYPE_FIELD(jboolean, Boolean, PRIM_BOOLEAN);
SET_STATIC_TYPE_FIELD(jbyte, Byte, PRIM_BYTE);
SET_STATIC_TYPE_FIELD(jchar, Char, PRIM_CHAR);
SET_STATIC_TYPE_FIELD(jshort, Short, PRIM_SHORT);
SET_STATIC_TYPE_FIELD(jint, Int, PRIM_INT);
SET_STATIC_TYPE_FIELD(jlong, Long, PRIM_LONG);
SET_STATIC_TYPE_FIELD(jfloat, Float, PRIM_FLOAT);
SET_STATIC_TYPE_FIELD(jdouble, Double, PRIM_DOUBLE);

#define GET_TYPE_FIELD(_ctype, _jname)                                      \
    static _ctype Check_Get##_jname##Field(JNIEnv* env, jobject obj,        \
        jfieldID fieldID)                                                   \
    {                                                                       \
        CHECK_ENTER(env, kFlag_Default);                                    \
        CHECK_OBJECT(env, obj);                                             \
        _ctype result;                                                      \
        CHECK_INST_FIELD_ID(env, obj, fieldID);                             \
        result = BASE_ENV(env)->Get##_jname##Field(env, obj, fieldID);      \
        CHECK_EXIT(env);                                                    \
        return result;                                                      \
    }
GET_TYPE_FIELD(jobject, Object);
GET_TYPE_FIELD(jboolean, Boolean);
GET_TYPE_FIELD(jbyte, Byte);
GET_TYPE_FIELD(jchar, Char);
GET_TYPE_FIELD(jshort, Short);
GET_TYPE_FIELD(jint, Int);
GET_TYPE_FIELD(jlong, Long);
GET_TYPE_FIELD(jfloat, Float);
GET_TYPE_FIELD(jdouble, Double);

#define SET_TYPE_FIELD(_ctype, _jname, _ftype)                              \
    static void Check_Set##_jname##Field(JNIEnv* env, jobject obj,          \
        jfieldID fieldID, _ctype value)                                     \
    {                                                                       \
        CHECK_ENTER(env, kFlag_Default);                                    \
        CHECK_OBJECT(env, obj);                                             \
        CHECK_INST_FIELD_ID(env, obj, fieldID);                             \
        /* "value" arg only used when type == ref */                        \
        CHECK_FIELD_TYPE(env, (jobject)(u4) value, fieldID, _ftype, false); \
        BASE_ENV(env)->Set##_jname##Field(env, obj, fieldID, value);        \
        CHECK_EXIT(env);                                                    \
    }
SET_TYPE_FIELD(jobject, Object, PRIM_NOT);
SET_TYPE_FIELD(jboolean, Boolean, PRIM_BOOLEAN);
SET_TYPE_FIELD(jbyte, Byte, PRIM_BYTE);
SET_TYPE_FIELD(jchar, Char, PRIM_CHAR);
SET_TYPE_FIELD(jshort, Short, PRIM_SHORT);
SET_TYPE_FIELD(jint, Int, PRIM_INT);
SET_TYPE_FIELD(jlong, Long, PRIM_LONG);
SET_TYPE_FIELD(jfloat, Float, PRIM_FLOAT);
SET_TYPE_FIELD(jdouble, Double, PRIM_DOUBLE);

#define CALL_VIRTUAL(_ctype, _jname, _retdecl, _retasgn, _retok, _retsig)   \
    static _ctype Check_Call##_jname##Method(JNIEnv* env, jobject obj,      \
        jmethodID methodID, ...)                                            \
    {                                                                       \
        CHECK_ENTER(env, kFlag_Default);                                    \
        CHECK_OBJECT(env, obj);                                             \
        CHECK_SIG(env, methodID, _retsig, false);                           \
        CHECK_VIRTUAL_METHOD(env, obj, methodID);                           \
        _retdecl;                                                           \
        va_list args, tmpArgs;                                              \
        va_start(args, methodID);                                           \
        va_copy(tmpArgs, args);                                             \
        CHECK_METHOD_ARGS_V(env, methodID, tmpArgs);                        \
        va_end(tmpArgs);                                                    \
        _retasgn BASE_ENV(env)->Call##_jname##MethodV(env, obj, methodID,   \
            args);                                                          \
        va_end(args);                                                       \
        CHECK_EXIT(env);                                                    \
        return _retok;                                                      \
    }                                                                       \
    static _ctype Check_Call##_jname##MethodV(JNIEnv* env, jobject obj,     \
        jmethodID methodID, va_list args)                                   \
    {                                                                       \
        CHECK_ENTER(env, kFlag_Default);                                    \
        CHECK_OBJECT(env, obj);                                             \
        CHECK_SIG(env, methodID, _retsig, false);                           \
        CHECK_VIRTUAL_METHOD(env, obj, methodID);                           \
        _retdecl;                                                           \
        va_list tmpArgs;                                                    \
        va_copy(tmpArgs, args);                                             \
        CHECK_METHOD_ARGS_V(env, methodID, tmpArgs);                        \
        va_end(tmpArgs);                                                    \
        _retasgn BASE_ENV(env)->Call##_jname##MethodV(env, obj, methodID,   \
            args);                                                          \
        CHECK_EXIT(env);                                                    \
        return _retok;                                                      \
    }                                                                       \
    static _ctype Check_Call##_jname##MethodA(JNIEnv* env, jobject obj,     \
        jmethodID methodID, jvalue* args)                                   \
    {                                                                       \
        CHECK_ENTER(env, kFlag_Default);                                    \
        CHECK_OBJECT(env, obj);                                             \
        CHECK_SIG(env, methodID, _retsig, false);                           \
        CHECK_VIRTUAL_METHOD(env, obj, methodID);                           \
        _retdecl;                                                           \
        CHECK_METHOD_ARGS_A(env, methodID, args);                           \
        _retasgn BASE_ENV(env)->Call##_jname##MethodA(env, obj, methodID,   \
            args);                                                          \
        CHECK_EXIT(env);                                                    \
        return _retok;                                                      \
    }
CALL_VIRTUAL(jobject, Object, Object* result, result=, result, 'L');
CALL_VIRTUAL(jboolean, Boolean, jboolean result, result=, result, 'Z');
CALL_VIRTUAL(jbyte, Byte, jbyte result, result=, result, 'B');
CALL_VIRTUAL(jchar, Char, jchar result, result=, result, 'C');
CALL_VIRTUAL(jshort, Short, jshort result, result=, result, 'S');
CALL_VIRTUAL(jint, Int, jint result, result=, result, 'I');
CALL_VIRTUAL(jlong, Long, jlong result, result=, result, 'J');
CALL_VIRTUAL(jfloat, Float, jfloat result, result=, result, 'F');
CALL_VIRTUAL(jdouble, Double, jdouble result, result=, result, 'D');
CALL_VIRTUAL(void, Void, , , , 'V');

#define CALL_NONVIRTUAL(_ctype, _jname, _retdecl, _retasgn, _retok,         \
        _retsig)                                                            \
    static _ctype Check_CallNonvirtual##_jname##Method(JNIEnv* env,         \
        jobject obj, jclass clazz, jmethodID methodID, ...)                 \
    {                                                                       \
        CHECK_ENTER(env, kFlag_Default);                                    \
        CHECK_CLASS(env, clazz);                                            \
        CHECK_OBJECT(env, obj);                                             \
        CHECK_SIG(env, methodID, _retsig, false);                           \
        CHECK_VIRTUAL_METHOD(env, obj, methodID);                           \
        _retdecl;                                                           \
        va_list args, tmpArgs;                                              \
        va_start(args, methodID);                                           \
        va_copy(tmpArgs, args);                                             \
        CHECK_METHOD_ARGS_V(env, methodID, tmpArgs);                        \
        va_end(tmpArgs);                                                    \
        _retasgn BASE_ENV(env)->CallNonvirtual##_jname##MethodV(env, obj,   \
            clazz, methodID, args);                                         \
        va_end(args);                                                       \
        CHECK_EXIT(env);                                                    \
        return _retok;                                                      \
    }                                                                       \
    static _ctype Check_CallNonvirtual##_jname##MethodV(JNIEnv* env,        \
        jobject obj, jclass clazz, jmethodID methodID, va_list args)        \
    {                                                                       \
        CHECK_ENTER(env, kFlag_Default);                                    \
        CHECK_CLASS(env, clazz);                                            \
        CHECK_OBJECT(env, obj);                                             \
        CHECK_SIG(env, methodID, _retsig, false);                           \
        CHECK_VIRTUAL_METHOD(env, obj, methodID);                           \
        _retdecl;                                                           \
        va_list tmpArgs;                                                    \
        va_copy(tmpArgs, args);                                             \
        CHECK_METHOD_ARGS_V(env, methodID, tmpArgs);                        \
        va_end(tmpArgs);                                                    \
        _retasgn BASE_ENV(env)->CallNonvirtual##_jname##MethodV(env, obj,   \
            clazz, methodID, args);                                         \
        CHECK_EXIT(env);                                                    \
        return _retok;                                                      \
    }                                                                       \
    static _ctype Check_CallNonvirtual##_jname##MethodA(JNIEnv* env,        \
        jobject obj, jclass clazz, jmethodID methodID, jvalue* args)        \
    {                                                                       \
        CHECK_ENTER(env, kFlag_Default);                                    \
        CHECK_CLASS(env, clazz);                                            \
        CHECK_OBJECT(env, obj);                                             \
        CHECK_SIG(env, methodID, _retsig, false);                           \
        CHECK_VIRTUAL_METHOD(env, obj, methodID);                           \
        _retdecl;                                                           \
        CHECK_METHOD_ARGS_A(env, methodID, args);                           \
        _retasgn BASE_ENV(env)->CallNonvirtual##_jname##MethodA(env, obj,   \
            clazz, methodID, args);                                         \
        CHECK_EXIT(env);                                                    \
        return _retok;                                                      \
    }
CALL_NONVIRTUAL(jobject, Object, Object* result, result=, result, 'L');
CALL_NONVIRTUAL(jboolean, Boolean, jboolean result, result=, result, 'Z');
CALL_NONVIRTUAL(jbyte, Byte, jbyte result, result=, result, 'B');
CALL_NONVIRTUAL(jchar, Char, jchar result, result=, result, 'C');
CALL_NONVIRTUAL(jshort, Short, jshort result, result=, result, 'S');
CALL_NONVIRTUAL(jint, Int, jint result, result=, result, 'I');
CALL_NONVIRTUAL(jlong, Long, jlong result, result=, result, 'J');
CALL_NONVIRTUAL(jfloat, Float, jfloat result, result=, result, 'F');
CALL_NONVIRTUAL(jdouble, Double, jdouble result, result=, result, 'D');
CALL_NONVIRTUAL(void, Void, , , , 'V');


#define CALL_STATIC(_ctype, _jname, _retdecl, _retasgn, _retok, _retsig)    \
    static _ctype Check_CallStatic##_jname##Method(JNIEnv* env,             \
        jclass clazz, jmethodID methodID, ...)                              \
    {                                                                       \
        CHECK_ENTER(env, kFlag_Default);                                    \
        CHECK_CLASS(env, clazz);                                            \
        CHECK_SIG(env, methodID, _retsig, true);                            \
        CHECK_STATIC_METHOD(env, clazz, methodID);                          \
        _retdecl;                                                           \
        va_list args, tmpArgs;                                              \
        va_start(args, methodID);                                           \
        va_copy(tmpArgs, args);                                             \
        CHECK_METHOD_ARGS_V(env, methodID, tmpArgs);                        \
        va_end(tmpArgs);                                                    \
        _retasgn BASE_ENV(env)->CallStatic##_jname##MethodV(env, clazz,     \
            methodID, args);                                                \
        va_end(args);                                                       \
        CHECK_EXIT(env);                                                    \
        return _retok;                                                      \
    }                                                                       \
    static _ctype Check_CallStatic##_jname##MethodV(JNIEnv* env,            \
        jclass clazz, jmethodID methodID, va_list args)                     \
    {                                                                       \
        CHECK_ENTER(env, kFlag_Default);                                    \
        CHECK_CLASS(env, clazz);                                            \
        CHECK_SIG(env, methodID, _retsig, true);                            \
        CHECK_STATIC_METHOD(env, clazz, methodID);                          \
        _retdecl;                                                           \
        va_list tmpArgs;                                                    \
        va_copy(tmpArgs, args);                                             \
        CHECK_METHOD_ARGS_V(env, methodID, tmpArgs);                        \
        va_end(tmpArgs);                                                    \
        _retasgn BASE_ENV(env)->CallStatic##_jname##MethodV(env, clazz,     \
            methodID, args);                                                \
        CHECK_EXIT(env);                                                    \
        return _retok;                                                      \
    }                                                                       \
    static _ctype Check_CallStatic##_jname##MethodA(JNIEnv* env,            \
        jclass clazz, jmethodID methodID, jvalue* args)                     \
    {                                                                       \
        CHECK_ENTER(env, kFlag_Default);                                    \
        CHECK_CLASS(env, clazz);                                            \
        CHECK_SIG(env, methodID, _retsig, true);                            \
        CHECK_STATIC_METHOD(env, clazz, methodID);                          \
        _retdecl;                                                           \
        CHECK_METHOD_ARGS_A(env, methodID, args);                           \
        _retasgn BASE_ENV(env)->CallStatic##_jname##MethodA(env, clazz,     \
            methodID, args);                                                \
        CHECK_EXIT(env);                                                    \
        return _retok;                                                      \
    }
CALL_STATIC(jobject, Object, Object* result, result=, result, 'L');
CALL_STATIC(jboolean, Boolean, jboolean result, result=, result, 'Z');
CALL_STATIC(jbyte, Byte, jbyte result, result=, result, 'B');
CALL_STATIC(jchar, Char, jchar result, result=, result, 'C');
CALL_STATIC(jshort, Short, jshort result, result=, result, 'S');
CALL_STATIC(jint, Int, jint result, result=, result, 'I');
CALL_STATIC(jlong, Long, jlong result, result=, result, 'J');
CALL_STATIC(jfloat, Float, jfloat result, result=, result, 'F');
CALL_STATIC(jdouble, Double, jdouble result, result=, result, 'D');
CALL_STATIC(void, Void, , , , 'V');

static jstring Check_NewString(JNIEnv* env, const jchar* unicodeChars,
    jsize len)
{
    CHECK_ENTER(env, kFlag_Default);
    jstring result;
    result = BASE_ENV(env)->NewString(env, unicodeChars, len);
    CHECK_EXIT(env);
    return result;
}

static jsize Check_GetStringLength(JNIEnv* env, jstring string)
{
    CHECK_ENTER(env, kFlag_CritOkay);
    CHECK_STRING(env, string);
    jsize result;
    result = BASE_ENV(env)->GetStringLength(env, string);
    CHECK_EXIT(env);
    return result;
}

static const jchar* Check_GetStringChars(JNIEnv* env, jstring string,
    jboolean* isCopy)
{
    CHECK_ENTER(env, kFlag_CritOkay);
    CHECK_STRING(env, string);
    const jchar* result;
    result = BASE_ENV(env)->GetStringChars(env, string, isCopy);
    if (((JNIEnvExt*)env)->forceDataCopy && result != NULL) {
        // TODO: fix for indirect
        int len = dvmStringLen(string) * 2;
        result = (const jchar*) createGuardedCopy(result, len, false);
        if (isCopy != NULL)
            *isCopy = JNI_TRUE;
    }
    CHECK_EXIT(env);
    return result;
}

static void Check_ReleaseStringChars(JNIEnv* env, jstring string,
    const jchar* chars)
{
    CHECK_ENTER(env, kFlag_Default | kFlag_ExcepOkay);
    CHECK_STRING(env, string);
    CHECK_NON_NULL(env, chars);
    if (((JNIEnvExt*)env)->forceDataCopy) {
        if (!checkGuardedCopy(chars, false)) {
            LOGE("JNI: failed guarded copy check in ReleaseStringChars\n");
            abortMaybe();
            return;
        }
        chars = (const jchar*) freeGuardedCopy((jchar*)chars);
    }
    BASE_ENV(env)->ReleaseStringChars(env, string, chars);
    CHECK_EXIT(env);
}

static jstring Check_NewStringUTF(JNIEnv* env, const char* bytes)
{
    CHECK_ENTER(env, kFlag_Default);
    CHECK_UTF_STRING(env, bytes, true);
    jstring result;
    result = BASE_ENV(env)->NewStringUTF(env, bytes);
    CHECK_EXIT(env);
    return result;
}

static jsize Check_GetStringUTFLength(JNIEnv* env, jstring string)
{
    CHECK_ENTER(env, kFlag_CritOkay);
    CHECK_STRING(env, string);
    jsize result;
    result = BASE_ENV(env)->GetStringUTFLength(env, string);
    CHECK_EXIT(env);
    return result;
}

static const char* Check_GetStringUTFChars(JNIEnv* env, jstring string,
    jboolean* isCopy)
{
    CHECK_ENTER(env, kFlag_CritOkay);
    CHECK_STRING(env, string);
    const char* result;
    result = BASE_ENV(env)->GetStringUTFChars(env, string, isCopy);
    if (((JNIEnvExt*)env)->forceDataCopy && result != NULL) {
        // TODO: fix for indirect
        int len = dvmStringUtf8ByteLen(string) + 1;
        result = (const char*) createGuardedCopy(result, len, false);
        if (isCopy != NULL)
            *isCopy = JNI_TRUE;
    }
    CHECK_EXIT(env);
    return result;
}

static void Check_ReleaseStringUTFChars(JNIEnv* env, jstring string,
    const char* utf)
{
    CHECK_ENTER(env, kFlag_ExcepOkay);
    CHECK_STRING(env, string);
    CHECK_NON_NULL(env, utf);
    if (((JNIEnvExt*)env)->forceDataCopy) {
        //int len = dvmStringUtf8ByteLen(string) + 1;
        if (!checkGuardedCopy(utf, false)) {
            LOGE("JNI: failed guarded copy check in ReleaseStringUTFChars\n");
            abortMaybe();
            return;
        }
        utf = (const char*) freeGuardedCopy((char*)utf);
    }
    BASE_ENV(env)->ReleaseStringUTFChars(env, string, utf);
    CHECK_EXIT(env);
}

static jsize Check_GetArrayLength(JNIEnv* env, jarray array)
{
    CHECK_ENTER(env, kFlag_CritOkay);
    CHECK_ARRAY(env, array);
    jsize result;
    result = BASE_ENV(env)->GetArrayLength(env, array);
    CHECK_EXIT(env);
    return result;
}

static jobjectArray Check_NewObjectArray(JNIEnv* env, jsize length,
    jclass elementClass, jobject initialElement)
{
    CHECK_ENTER(env, kFlag_Default);
    CHECK_CLASS(env, elementClass);
    CHECK_OBJECT(env, initialElement);
    CHECK_LENGTH_POSITIVE(env, length);
    jobjectArray result;
    result = BASE_ENV(env)->NewObjectArray(env, length, elementClass,
                                            initialElement);
    CHECK_EXIT(env);
    return result;
}

static jobject Check_GetObjectArrayElement(JNIEnv* env, jobjectArray array,
    jsize index)
{
    CHECK_ENTER(env, kFlag_Default);
    CHECK_ARRAY(env, array);
    jobject result;
    result = BASE_ENV(env)->GetObjectArrayElement(env, array, index);
    CHECK_EXIT(env);
    return result;
}

static void Check_SetObjectArrayElement(JNIEnv* env, jobjectArray array,
    jsize index, jobject value)
{
    CHECK_ENTER(env, kFlag_Default);
    CHECK_ARRAY(env, array);
    BASE_ENV(env)->SetObjectArrayElement(env, array, index, value);
    CHECK_EXIT(env);
}

#define NEW_PRIMITIVE_ARRAY(_artype, _jname)                                \
    static _artype Check_New##_jname##Array(JNIEnv* env, jsize length)      \
    {                                                                       \
        CHECK_ENTER(env, kFlag_Default);                                    \
        CHECK_LENGTH_POSITIVE(env, length);                                 \
        _artype result;                                                     \
        result = BASE_ENV(env)->New##_jname##Array(env, length);            \
        CHECK_EXIT(env);                                                    \
        return result;                                                      \
    }
NEW_PRIMITIVE_ARRAY(jbooleanArray, Boolean);
NEW_PRIMITIVE_ARRAY(jbyteArray, Byte);
NEW_PRIMITIVE_ARRAY(jcharArray, Char);
NEW_PRIMITIVE_ARRAY(jshortArray, Short);
NEW_PRIMITIVE_ARRAY(jintArray, Int);
NEW_PRIMITIVE_ARRAY(jlongArray, Long);
NEW_PRIMITIVE_ARRAY(jfloatArray, Float);
NEW_PRIMITIVE_ARRAY(jdoubleArray, Double);


#define GET_PRIMITIVE_ARRAY_ELEMENTS(_ctype, _jname)                        \
    static _ctype* Check_Get##_jname##ArrayElements(JNIEnv* env,            \
        _ctype##Array array, jboolean* isCopy)                              \
    {                                                                       \
        CHECK_ENTER(env, kFlag_Default);                                    \
        CHECK_ARRAY(env, array);                                            \
        _ctype* result;                                                     \
        result = BASE_ENV(env)->Get##_jname##ArrayElements(env,             \
            array, isCopy);                                                 \
        if (((JNIEnvExt*)env)->forceDataCopy && result != NULL) {           \
            result = (_ctype*) createGuardedPACopy(env, array, isCopy);     \
        }                                                                   \
        CHECK_EXIT(env);                                                    \
        return result;                                                      \
    }

#define RELEASE_PRIMITIVE_ARRAY_ELEMENTS(_ctype, _jname)                    \
    static void Check_Release##_jname##ArrayElements(JNIEnv* env,           \
        _ctype##Array array, _ctype* elems, jint mode)                      \
    {                                                                       \
        CHECK_ENTER(env, kFlag_Default | kFlag_ExcepOkay);                  \
        CHECK_ARRAY(env, array);                                            \
        CHECK_NON_NULL(env, elems);                                         \
        CHECK_RELEASE_MODE(env, mode);                                      \
        if (((JNIEnvExt*)env)->forceDataCopy) {                             \
            elems = (_ctype*) releaseGuardedPACopy(env, array, elems, mode);\
        }                                                                   \
        BASE_ENV(env)->Release##_jname##ArrayElements(env,                  \
            array, elems, mode);                                            \
        CHECK_EXIT(env);                                                    \
    }

#define GET_PRIMITIVE_ARRAY_REGION(_ctype, _jname)                          \
    static void Check_Get##_jname##ArrayRegion(JNIEnv* env,                 \
        _ctype##Array array, jsize start, jsize len, _ctype* buf)           \
    {                                                                       \
        CHECK_ENTER(env, kFlag_Default);                                    \
        CHECK_ARRAY(env, array);                                            \
        BASE_ENV(env)->Get##_jname##ArrayRegion(env, array, start,          \
            len, buf);                                                      \
        CHECK_EXIT(env);                                                    \
    }

#define SET_PRIMITIVE_ARRAY_REGION(_ctype, _jname)                          \
    static void Check_Set##_jname##ArrayRegion(JNIEnv* env,                 \
        _ctype##Array array, jsize start, jsize len, const _ctype* buf)     \
    {                                                                       \
        CHECK_ENTER(env, kFlag_Default);                                    \
        CHECK_ARRAY(env, array);                                            \
        BASE_ENV(env)->Set##_jname##ArrayRegion(env, array, start,          \
            len, buf);                                                      \
        CHECK_EXIT(env);                                                    \
    }

#define PRIMITIVE_ARRAY_FUNCTIONS(_ctype, _jname, _typechar)                \
    GET_PRIMITIVE_ARRAY_ELEMENTS(_ctype, _jname);                           \
    RELEASE_PRIMITIVE_ARRAY_ELEMENTS(_ctype, _jname);                       \
    GET_PRIMITIVE_ARRAY_REGION(_ctype, _jname);                             \
    SET_PRIMITIVE_ARRAY_REGION(_ctype, _jname);

/* TODO: verify primitive array type matches call type */
PRIMITIVE_ARRAY_FUNCTIONS(jboolean, Boolean, 'Z');
PRIMITIVE_ARRAY_FUNCTIONS(jbyte, Byte, 'B');
PRIMITIVE_ARRAY_FUNCTIONS(jchar, Char, 'C');
PRIMITIVE_ARRAY_FUNCTIONS(jshort, Short, 'S');
PRIMITIVE_ARRAY_FUNCTIONS(jint, Int, 'I');
PRIMITIVE_ARRAY_FUNCTIONS(jlong, Long, 'J');
PRIMITIVE_ARRAY_FUNCTIONS(jfloat, Float, 'F');
PRIMITIVE_ARRAY_FUNCTIONS(jdouble, Double, 'D');

static jint Check_RegisterNatives(JNIEnv* env, jclass clazz,
    const JNINativeMethod* methods, jint nMethods)
{
    CHECK_ENTER(env, kFlag_Default);
    CHECK_CLASS(env, clazz);
    jint result;
    result = BASE_ENV(env)->RegisterNatives(env, clazz, methods, nMethods);
    CHECK_EXIT(env);
    return result;
}

static jint Check_UnregisterNatives(JNIEnv* env, jclass clazz)
{
    CHECK_ENTER(env, kFlag_Default);
    CHECK_CLASS(env, clazz);
    jint result;
    result = BASE_ENV(env)->UnregisterNatives(env, clazz);
    CHECK_EXIT(env);
    return result;
}

static jint Check_MonitorEnter(JNIEnv* env, jobject obj)
{
    CHECK_ENTER(env, kFlag_Default);
    CHECK_OBJECT(env, obj);
    jint result;
    result = BASE_ENV(env)->MonitorEnter(env, obj);
    CHECK_EXIT(env);
    return result;
}

static jint Check_MonitorExit(JNIEnv* env, jobject obj)
{
    CHECK_ENTER(env, kFlag_Default | kFlag_ExcepOkay);
    CHECK_OBJECT(env, obj);
    jint result;
    result = BASE_ENV(env)->MonitorExit(env, obj);
    CHECK_EXIT(env);
    return result;
}

static jint Check_GetJavaVM(JNIEnv *env, JavaVM **vm)
{
    CHECK_ENTER(env, kFlag_Default);
    jint result;
    result = BASE_ENV(env)->GetJavaVM(env, vm);
    CHECK_EXIT(env);
    return result;
}

static void Check_GetStringRegion(JNIEnv* env, jstring str, jsize start,
    jsize len, jchar* buf)
{
    CHECK_ENTER(env, kFlag_CritOkay);
    CHECK_STRING(env, str);
    BASE_ENV(env)->GetStringRegion(env, str, start, len, buf);
    CHECK_EXIT(env);
}

static void Check_GetStringUTFRegion(JNIEnv* env, jstring str, jsize start,
    jsize len, char* buf)
{
    CHECK_ENTER(env, kFlag_CritOkay);
    CHECK_STRING(env, str);
    BASE_ENV(env)->GetStringUTFRegion(env, str, start, len, buf);
    CHECK_EXIT(env);
}

static void* Check_GetPrimitiveArrayCritical(JNIEnv* env, jarray array,
    jboolean* isCopy)
{
    CHECK_ENTER(env, kFlag_CritGet);
    CHECK_ARRAY(env, array);
    void* result;
    result = BASE_ENV(env)->GetPrimitiveArrayCritical(env, array, isCopy);
    if (((JNIEnvExt*)env)->forceDataCopy && result != NULL) {
        result = createGuardedPACopy(env, array, isCopy);
    }
    CHECK_EXIT(env);
    return result;
}

static void Check_ReleasePrimitiveArrayCritical(JNIEnv* env, jarray array,
    void* carray, jint mode)
{
    CHECK_ENTER(env, kFlag_CritRelease | kFlag_ExcepOkay);
    CHECK_ARRAY(env, array);
    CHECK_NON_NULL(env, carray);
    CHECK_RELEASE_MODE(env, mode);
    if (((JNIEnvExt*)env)->forceDataCopy) {
        carray = releaseGuardedPACopy(env, array, carray, mode);
    }
    BASE_ENV(env)->ReleasePrimitiveArrayCritical(env, array, carray, mode);
    CHECK_EXIT(env);
}

static const jchar* Check_GetStringCritical(JNIEnv* env, jstring string,
    jboolean* isCopy)
{
    CHECK_ENTER(env, kFlag_CritGet);
    CHECK_STRING(env, string);
    const jchar* result;
    result = BASE_ENV(env)->GetStringCritical(env, string, isCopy);
    if (((JNIEnvExt*)env)->forceDataCopy && result != NULL) {
        // TODO: fix for indirect
        int len = dvmStringLen(string) * 2;
        result = (const jchar*) createGuardedCopy(result, len, false);
        if (isCopy != NULL)
            *isCopy = JNI_TRUE;
    }
    CHECK_EXIT(env);
    return result;
}

static void Check_ReleaseStringCritical(JNIEnv* env, jstring string,
    const jchar* carray)
{
    CHECK_ENTER(env, kFlag_CritRelease | kFlag_ExcepOkay);
    CHECK_STRING(env, string);
    CHECK_NON_NULL(env, carray);
    if (((JNIEnvExt*)env)->forceDataCopy) {
        if (!checkGuardedCopy(carray, false)) {
            LOGE("JNI: failed guarded copy check in ReleaseStringCritical\n");
            abortMaybe();
            return;
        }
        carray = (const jchar*) freeGuardedCopy((jchar*)carray);
    }
    BASE_ENV(env)->ReleaseStringCritical(env, string, carray);
    CHECK_EXIT(env);
}

static jweak Check_NewWeakGlobalRef(JNIEnv* env, jobject obj)
{
    CHECK_ENTER(env, kFlag_Default);
    CHECK_OBJECT(env, obj);
    jweak result;
    result = BASE_ENV(env)->NewWeakGlobalRef(env, obj);
    CHECK_EXIT(env);
    return result;
}

static void Check_DeleteWeakGlobalRef(JNIEnv* env, jweak obj)
{
    CHECK_ENTER(env, kFlag_Default | kFlag_ExcepOkay);
    CHECK_OBJECT(env, obj);
    BASE_ENV(env)->DeleteWeakGlobalRef(env, obj);
    CHECK_EXIT(env);
}

static jboolean Check_ExceptionCheck(JNIEnv* env)
{
    CHECK_ENTER(env, kFlag_CritOkay | kFlag_ExcepOkay);
    jboolean result;
    result = BASE_ENV(env)->ExceptionCheck(env);
    CHECK_EXIT(env);
    return result;
}

static jobjectRefType Check_GetObjectRefType(JNIEnv* env, jobject obj)
{
    CHECK_ENTER(env, kFlag_Default);
    CHECK_OBJECT(env, obj);
    jobjectRefType result;
    result = BASE_ENV(env)->GetObjectRefType(env, obj);
    CHECK_EXIT(env);
    return result;
}

static jobject Check_NewDirectByteBuffer(JNIEnv* env, void* address,
    jlong capacity)
{
    CHECK_ENTER(env, kFlag_Default);
    jobject result;
    if (address == NULL || capacity < 0) {
        LOGW("JNI WARNING: invalid values for address (%p) or capacity (%ld)\n",
            address, (long) capacity);
        abortMaybe();
        return NULL;
    }
    result = BASE_ENV(env)->NewDirectByteBuffer(env, address, capacity);
    CHECK_EXIT(env);
    return result;
}

static void* Check_GetDirectBufferAddress(JNIEnv* env, jobject buf)
{
    CHECK_ENTER(env, kFlag_Default);
    CHECK_OBJECT(env, buf);
    void* result = BASE_ENV(env)->GetDirectBufferAddress(env, buf);
    CHECK_EXIT(env);

    /* optional - check result vs. "safe" implementation */
    if (kRedundantDirectBufferTest) {
        jobject platformAddr = NULL;
        void* checkResult = NULL;

        /*
         * Start by determining if the object supports the DirectBuffer
         * interfaces.  Note this does not guarantee that it's a direct buffer.
         */
        if (JNI_FALSE == (*env)->IsInstanceOf(env, buf,
                gDvm.jclassOrgApacheHarmonyNioInternalDirectBuffer))
        {
            goto bail;
        }

        /*
         * Get the PlatformAddress object.
         *
         * If this isn't a direct buffer, platformAddr will be NULL and/or an
         * exception will have been thrown.
         */
        platformAddr = (*env)->CallObjectMethod(env, buf,
            (jmethodID) gDvm.methOrgApacheHarmonyNioInternalDirectBuffer_getEffectiveAddress);

        if ((*env)->ExceptionCheck(env)) {
            (*env)->ExceptionClear(env);
            platformAddr = NULL;
        }
        if (platformAddr == NULL) {
            LOGV("Got request for address of non-direct buffer\n");
            goto bail;
        }

        jclass platformAddrClass = (*env)->FindClass(env,
            "org/apache/harmony/luni/platform/PlatformAddress");
        jmethodID toLongMethod = (*env)->GetMethodID(env, platformAddrClass,
            "toLong", "()J");
        checkResult = (void*)(u4)(*env)->CallLongMethod(env, platformAddr,
                toLongMethod);

    bail:
        if (platformAddr != NULL)
            (*env)->DeleteLocalRef(env, platformAddr);

        if (result != checkResult) {
            LOGW("JNI WARNING: direct buffer result mismatch (%p vs %p)\n",
                result, checkResult);
            abortMaybe();
            /* keep going */
        }
    }

    return result;
}

static jlong Check_GetDirectBufferCapacity(JNIEnv* env, jobject buf)
{
    CHECK_ENTER(env, kFlag_Default);
    CHECK_OBJECT(env, buf);
    /* TODO: verify "buf" is an instance of java.nio.Buffer */
    jlong result = BASE_ENV(env)->GetDirectBufferCapacity(env, buf);
    CHECK_EXIT(env);
    return result;
}


/*
 * ===========================================================================
 *      JNI invocation functions
 * ===========================================================================
 */

static jint Check_DestroyJavaVM(JavaVM* vm)
{
    CHECK_VMENTER(vm, false);
    jint result;
    result = BASE_VM(vm)->DestroyJavaVM(vm);
    CHECK_VMEXIT(vm, false);
    return result;
}

static jint Check_AttachCurrentThread(JavaVM* vm, JNIEnv** p_env,
    void* thr_args)
{
    CHECK_VMENTER(vm, false);
    jint result;
    result = BASE_VM(vm)->AttachCurrentThread(vm, p_env, thr_args);
    CHECK_VMEXIT(vm, true);
    return result;
}

static jint Check_AttachCurrentThreadAsDaemon(JavaVM* vm, JNIEnv** p_env,
    void* thr_args)
{
    CHECK_VMENTER(vm, false);
    jint result;
    result = BASE_VM(vm)->AttachCurrentThreadAsDaemon(vm, p_env, thr_args);
    CHECK_VMEXIT(vm, true);
    return result;
}

static jint Check_DetachCurrentThread(JavaVM* vm)
{
    CHECK_VMENTER(vm, true);
    jint result;
    result = BASE_VM(vm)->DetachCurrentThread(vm);
    CHECK_VMEXIT(vm, false);
    return result;
}

static jint Check_GetEnv(JavaVM* vm, void** env, jint version)
{
    CHECK_VMENTER(vm, true);
    jint result;
    result = BASE_VM(vm)->GetEnv(vm, env, version);
    CHECK_VMEXIT(vm, true);
    return result;
}


/*
 * ===========================================================================
 *      Function tables
 * ===========================================================================
 */

static const struct JNINativeInterface gCheckNativeInterface = {
    NULL,
    NULL,
    NULL,
    NULL,

    Check_GetVersion,

    Check_DefineClass,
    Check_FindClass,

    Check_FromReflectedMethod,
    Check_FromReflectedField,
    Check_ToReflectedMethod,

    Check_GetSuperclass,
    Check_IsAssignableFrom,

    Check_ToReflectedField,

    Check_Throw,
    Check_ThrowNew,
    Check_ExceptionOccurred,
    Check_ExceptionDescribe,
    Check_ExceptionClear,
    Check_FatalError,

    Check_PushLocalFrame,
    Check_PopLocalFrame,

    Check_NewGlobalRef,
    Check_DeleteGlobalRef,
    Check_DeleteLocalRef,
    Check_IsSameObject,
    Check_NewLocalRef,
    Check_EnsureLocalCapacity,

    Check_AllocObject,
    Check_NewObject,
    Check_NewObjectV,
    Check_NewObjectA,

    Check_GetObjectClass,
    Check_IsInstanceOf,

    Check_GetMethodID,

    Check_CallObjectMethod,
    Check_CallObjectMethodV,
    Check_CallObjectMethodA,
    Check_CallBooleanMethod,
    Check_CallBooleanMethodV,
    Check_CallBooleanMethodA,
    Check_CallByteMethod,
    Check_CallByteMethodV,
    Check_CallByteMethodA,
    Check_CallCharMethod,
    Check_CallCharMethodV,
    Check_CallCharMethodA,
    Check_CallShortMethod,
    Check_CallShortMethodV,
    Check_CallShortMethodA,
    Check_CallIntMethod,
    Check_CallIntMethodV,
    Check_CallIntMethodA,
    Check_CallLongMethod,
    Check_CallLongMethodV,
    Check_CallLongMethodA,
    Check_CallFloatMethod,
    Check_CallFloatMethodV,
    Check_CallFloatMethodA,
    Check_CallDoubleMethod,
    Check_CallDoubleMethodV,
    Check_CallDoubleMethodA,
    Check_CallVoidMethod,
    Check_CallVoidMethodV,
    Check_CallVoidMethodA,

    Check_CallNonvirtualObjectMethod,
    Check_CallNonvirtualObjectMethodV,
    Check_CallNonvirtualObjectMethodA,
    Check_CallNonvirtualBooleanMethod,
    Check_CallNonvirtualBooleanMethodV,
    Check_CallNonvirtualBooleanMethodA,
    Check_CallNonvirtualByteMethod,
    Check_CallNonvirtualByteMethodV,
    Check_CallNonvirtualByteMethodA,
    Check_CallNonvirtualCharMethod,
    Check_CallNonvirtualCharMethodV,
    Check_CallNonvirtualCharMethodA,
    Check_CallNonvirtualShortMethod,
    Check_CallNonvirtualShortMethodV,
    Check_CallNonvirtualShortMethodA,
    Check_CallNonvirtualIntMethod,
    Check_CallNonvirtualIntMethodV,
    Check_CallNonvirtualIntMethodA,
    Check_CallNonvirtualLongMethod,
    Check_CallNonvirtualLongMethodV,
    Check_CallNonvirtualLongMethodA,
    Check_CallNonvirtualFloatMethod,
    Check_CallNonvirtualFloatMethodV,
    Check_CallNonvirtualFloatMethodA,
    Check_CallNonvirtualDoubleMethod,
    Check_CallNonvirtualDoubleMethodV,
    Check_CallNonvirtualDoubleMethodA,
    Check_CallNonvirtualVoidMethod,
    Check_CallNonvirtualVoidMethodV,
    Check_CallNonvirtualVoidMethodA,

    Check_GetFieldID,

    Check_GetObjectField,
    Check_GetBooleanField,
    Check_GetByteField,
    Check_GetCharField,
    Check_GetShortField,
    Check_GetIntField,
    Check_GetLongField,
    Check_GetFloatField,
    Check_GetDoubleField,
    Check_SetObjectField,
    Check_SetBooleanField,
    Check_SetByteField,
    Check_SetCharField,
    Check_SetShortField,
    Check_SetIntField,
    Check_SetLongField,
    Check_SetFloatField,
    Check_SetDoubleField,

    Check_GetStaticMethodID,

    Check_CallStaticObjectMethod,
    Check_CallStaticObjectMethodV,
    Check_CallStaticObjectMethodA,
    Check_CallStaticBooleanMethod,
    Check_CallStaticBooleanMethodV,
    Check_CallStaticBooleanMethodA,
    Check_CallStaticByteMethod,
    Check_CallStaticByteMethodV,
    Check_CallStaticByteMethodA,
    Check_CallStaticCharMethod,
    Check_CallStaticCharMethodV,
    Check_CallStaticCharMethodA,
    Check_CallStaticShortMethod,
    Check_CallStaticShortMethodV,
    Check_CallStaticShortMethodA,
    Check_CallStaticIntMethod,
    Check_CallStaticIntMethodV,
    Check_CallStaticIntMethodA,
    Check_CallStaticLongMethod,
    Check_CallStaticLongMethodV,
    Check_CallStaticLongMethodA,
    Check_CallStaticFloatMethod,
    Check_CallStaticFloatMethodV,
    Check_CallStaticFloatMethodA,
    Check_CallStaticDoubleMethod,
    Check_CallStaticDoubleMethodV,
    Check_CallStaticDoubleMethodA,
    Check_CallStaticVoidMethod,
    Check_CallStaticVoidMethodV,
    Check_CallStaticVoidMethodA,

    Check_GetStaticFieldID,

    Check_GetStaticObjectField,
    Check_GetStaticBooleanField,
    Check_GetStaticByteField,
    Check_GetStaticCharField,
    Check_GetStaticShortField,
    Check_GetStaticIntField,
    Check_GetStaticLongField,
    Check_GetStaticFloatField,
    Check_GetStaticDoubleField,

    Check_SetStaticObjectField,
    Check_SetStaticBooleanField,
    Check_SetStaticByteField,
    Check_SetStaticCharField,
    Check_SetStaticShortField,
    Check_SetStaticIntField,
    Check_SetStaticLongField,
    Check_SetStaticFloatField,
    Check_SetStaticDoubleField,

    Check_NewString,

    Check_GetStringLength,
    Check_GetStringChars,
    Check_ReleaseStringChars,

    Check_NewStringUTF,
    Check_GetStringUTFLength,
    Check_GetStringUTFChars,
    Check_ReleaseStringUTFChars,

    Check_GetArrayLength,
    Check_NewObjectArray,
    Check_GetObjectArrayElement,
    Check_SetObjectArrayElement,

    Check_NewBooleanArray,
    Check_NewByteArray,
    Check_NewCharArray,
    Check_NewShortArray,
    Check_NewIntArray,
    Check_NewLongArray,
    Check_NewFloatArray,
    Check_NewDoubleArray,

    Check_GetBooleanArrayElements,
    Check_GetByteArrayElements,
    Check_GetCharArrayElements,
    Check_GetShortArrayElements,
    Check_GetIntArrayElements,
    Check_GetLongArrayElements,
    Check_GetFloatArrayElements,
    Check_GetDoubleArrayElements,

    Check_ReleaseBooleanArrayElements,
    Check_ReleaseByteArrayElements,
    Check_ReleaseCharArrayElements,
    Check_ReleaseShortArrayElements,
    Check_ReleaseIntArrayElements,
    Check_ReleaseLongArrayElements,
    Check_ReleaseFloatArrayElements,
    Check_ReleaseDoubleArrayElements,

    Check_GetBooleanArrayRegion,
    Check_GetByteArrayRegion,
    Check_GetCharArrayRegion,
    Check_GetShortArrayRegion,
    Check_GetIntArrayRegion,
    Check_GetLongArrayRegion,
    Check_GetFloatArrayRegion,
    Check_GetDoubleArrayRegion,
    Check_SetBooleanArrayRegion,
    Check_SetByteArrayRegion,
    Check_SetCharArrayRegion,
    Check_SetShortArrayRegion,
    Check_SetIntArrayRegion,
    Check_SetLongArrayRegion,
    Check_SetFloatArrayRegion,
    Check_SetDoubleArrayRegion,

    Check_RegisterNatives,
    Check_UnregisterNatives,

    Check_MonitorEnter,
    Check_MonitorExit,

    Check_GetJavaVM,

    Check_GetStringRegion,
    Check_GetStringUTFRegion,

    Check_GetPrimitiveArrayCritical,
    Check_ReleasePrimitiveArrayCritical,

    Check_GetStringCritical,
    Check_ReleaseStringCritical,

    Check_NewWeakGlobalRef,
    Check_DeleteWeakGlobalRef,

    Check_ExceptionCheck,

    Check_NewDirectByteBuffer,
    Check_GetDirectBufferAddress,
    Check_GetDirectBufferCapacity,

    Check_GetObjectRefType
};
static const struct JNIInvokeInterface gCheckInvokeInterface = {
    NULL,
    NULL,
    NULL,

    Check_DestroyJavaVM,
    Check_AttachCurrentThread,
    Check_DetachCurrentThread,

    Check_GetEnv,

    Check_AttachCurrentThreadAsDaemon,
};


/*
 * Replace the normal table with the checked table.
 */
void dvmUseCheckedJniEnv(JNIEnvExt* pEnv)
{
    assert(pEnv->funcTable != &gCheckNativeInterface);
    pEnv->baseFuncTable = pEnv->funcTable;
    pEnv->funcTable = &gCheckNativeInterface;
}

/*
 * Replace the normal table with the checked table.
 */
void dvmUseCheckedJniVm(JavaVMExt* pVm)
{
    assert(pVm->funcTable != &gCheckInvokeInterface);
    pVm->baseFuncTable = pVm->funcTable;
    pVm->funcTable = &gCheckInvokeInterface;
}
