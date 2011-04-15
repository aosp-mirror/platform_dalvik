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
 */

#include "Dalvik.h"
#include "JniInternal.h"

#include <zlib.h>

/*
 * Abort if we are configured to bail out on JNI warnings.
 */
static void abortMaybe() {
    JavaVMExt* vm = (JavaVMExt*) gDvm.vmList;
    if (vm->warnError) {
        dvmDumpThread(dvmThreadSelf(), false);
        dvmAbort();
    }
}

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
static void checkCallResultCommon(const u4* args, const JValue* pResult,
        const Method* method, Thread* self)
{
    assert(pResult->l != NULL);
    const Object* resultObj = (const Object*) pResult->l;

    if (resultObj == kInvalidIndirectRefObject) {
        LOGW("JNI WARNING: invalid reference returned from native code");
        const Method* method = dvmGetCurrentJNIMethod();
        char* desc = dexProtoCopyMethodDescriptor(&method->prototype);
        LOGW("             in %s.%s:%s", method->clazz->descriptor, method->name, desc);
        free(desc);
        abortMaybe();
        return;
    }

    ClassObject* objClazz = resultObj->clazz;

    /*
     * Make sure that pResult->l is an instance of the type this
     * method was expected to return.
     */
    const char* declType = dexProtoGetReturnType(&method->prototype);
    const char* objType = objClazz->descriptor;
    if (strcmp(declType, objType) == 0) {
        /* names match; ignore class loader issues and allow it */
        LOGV("Check %s.%s: %s io %s (FAST-OK)",
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
        ClassObject* declClazz = dvmFindClassNoInit(declType, method->clazz->classLoader);
        if (declClazz == NULL) {
            LOGW("JNI WARNING: method declared to return '%s' returned '%s'",
                declType, objType);
            LOGW("             failed in %s.%s ('%s' not found)",
                method->clazz->descriptor, method->name, declType);
            abortMaybe();
            return;
        }
        if (!dvmInstanceof(objClazz, declClazz)) {
            LOGW("JNI WARNING: method declared to return '%s' returned '%s'",
                declType, objType);
            LOGW("             failed in %s.%s",
                method->clazz->descriptor, method->name);
            abortMaybe();
            return;
        } else {
            LOGV("Check %s.%s: %s io %s (SLOW-OK)",
                method->clazz->descriptor, method->name, objType, declType);
        }
    }
}

/*
 * Determine if we need to check the return type coming out of the call.
 *
 * (We don't simply do this at the top of checkCallResultCommon() because
 * this is on the critical path for native method calls.)
 */
static inline bool callNeedsCheck(const u4* args, JValue* pResult,
    const Method* method, Thread* self)
{
    return (method->shorty[0] == 'L' && !dvmCheckException(self) && pResult->l != NULL);
}

/*
 * Check a call into native code.
 */
void dvmCheckCallJNIMethod_general(const u4* args, JValue* pResult,
    const Method* method, Thread* self)
{
    dvmCallJNIMethod_general(args, pResult, method, self);
    if (callNeedsCheck(args, pResult, method, self)) {
        checkCallResultCommon(args, pResult, method, self);
    }
}

/*
 * Check a synchronized call into native code.
 */
void dvmCheckCallJNIMethod_synchronized(const u4* args, JValue* pResult,
    const Method* method, Thread* self)
{
    dvmCallJNIMethod_synchronized(args, pResult, method, self);
    if (callNeedsCheck(args, pResult, method, self)) {
        checkCallResultCommon(args, pResult, method, self);
    }
}

/*
 * Check a virtual call with no reference arguments (other than "this").
 */
void dvmCheckCallJNIMethod_virtualNoRef(const u4* args, JValue* pResult,
    const Method* method, Thread* self)
{
    dvmCallJNIMethod_virtualNoRef(args, pResult, method, self);
    if (callNeedsCheck(args, pResult, method, self)) {
        checkCallResultCommon(args, pResult, method, self);
    }
}

/*
 * Check a static call with no reference arguments (other than "clazz").
 */
void dvmCheckCallJNIMethod_staticNoRef(const u4* args, JValue* pResult,
    const Method* method, Thread* self)
{
    dvmCallJNIMethod_staticNoRef(args, pResult, method, self);
    if (callNeedsCheck(args, pResult, method, self)) {
        checkCallResultCommon(args, pResult, method, self);
    }
}


/*
 * ===========================================================================
 *      JNI function helpers
 * ===========================================================================
 */

static inline const JNINativeInterface* baseEnv(JNIEnv* env) {
    return ((JNIEnvExt*) env)->baseFuncTable;
}

static inline const JNIInvokeInterface* baseVm(JavaVM* vm) {
    return ((JavaVMExt*) vm)->baseFuncTable;
}

/*
 * Prints trace messages when a native method calls a JNI function such as
 * NewByteArray. Enabled if both "-Xcheck:jni" and "-verbose:jni" are enabled.
 */
static inline void jniTrace(bool hasMethod, const char* functionName) {
    static const char* classDescriptor = "???";
    static const char* methodName = "???";
    if (hasMethod) {
        const Method* method = dvmGetCurrentJNIMethod();
        classDescriptor = method->clazz->descriptor;
        methodName = method->name;
    }
    /* use +6 to drop the leading "Check_" */
    LOGI("JNI: %s (from %s.%s)", functionName + 6, classDescriptor, methodName);
}

class ScopedJniThreadState {
public:
    explicit ScopedJniThreadState(JNIEnv* env) {
        dvmChangeStatus(NULL, THREAD_RUNNING);
    }

    ~ScopedJniThreadState() {
        dvmChangeStatus(NULL, THREAD_NATIVE);
    }

private:
    // Disallow copy and assignment.
    ScopedJniThreadState(const ScopedJniThreadState&);
    void operator=(const ScopedJniThreadState&);
};

class ScopedVmCheck {
public:
    /*
     * Set "hasMethod" to true if we have a valid thread with a method pointer.
     * We won't have one before attaching a thread, after detaching a thread, or
     * after destroying the VM.
     */
    ScopedVmCheck(bool hasMethod, const char* functionName) {
        if (gDvm.verboseJni) {
            jniTrace(hasMethod, functionName);
        }
    }

private:
    // Disallow copy and assignment.
    ScopedVmCheck(const ScopedVmCheck&);
    void operator=(const ScopedVmCheck&);
};

/*
 * Flags passed into ScopedCheck.
 */
#define kFlag_Default       0x0000

#define kFlag_CritBad       0x0000      /* calling while in critical is bad */
#define kFlag_CritOkay      0x0001      /* ...okay */
#define kFlag_CritGet       0x0002      /* this is a critical "get" */
#define kFlag_CritRelease   0x0003      /* this is a critical "release" */
#define kFlag_CritMask      0x0003      /* bit mask to get "crit" value */

#define kFlag_ExcepBad      0x0000      /* raised exceptions are bad */
#define kFlag_ExcepOkay     0x0004      /* ...okay */

class ScopedCheck {
public:
    explicit ScopedCheck(JNIEnv* env, int flags, const char* functionName)
    : mEnv(env), mFunctionName(functionName)
    {
        if (gDvm.verboseJni) {
            jniTrace(true, mFunctionName);
        }
        checkThread(flags);
    }

    /*
     * Verify that "array" is non-NULL and points to an Array object.
     *
     * Since we're dealing with objects, switch to "running" mode.
     */
    void checkArray(jarray jarr) {
        if (jarr == NULL) {
            LOGW("JNI WARNING: received null array");
            showLocation();
            abortMaybe();
            return;
        }

        ScopedJniThreadState ts(mEnv);
        bool printWarn = false;

        Object* obj = dvmDecodeIndirectRef(mEnv, jarr);

        if (!dvmIsValidObject(obj)) {
            LOGW("JNI WARNING: jarray is invalid %s ref (%p)", dvmIndirectRefTypeName(jarr), jarr);
            printWarn = true;
        } else if (obj->clazz->descriptor[0] != '[') {
            LOGW("JNI WARNING: jarray arg has wrong type (expected array, got %s)",
                    obj->clazz->descriptor);
            printWarn = true;
        }

        if (printWarn) {
            showLocation();
            abortMaybe();
        }
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
    void checkClassName(const char* className) {
        if (!dexIsValidClassName(className, false)) {
            LOGW("JNI WARNING: illegal class name '%s' (%s)", className, mFunctionName);
            LOGW("             (should be formed like 'dalvik/system/DexFile')");
            LOGW("             or '[Ldalvik/system/DexFile;' or '[[B')");
            abortMaybe();
        }
    }

    /*
     * Verify that the field is of the appropriate type.  If the field has an
     * object type, "jobj" is the object we're trying to assign into it.
     *
     * Works for both static and instance fields.
     */
    void checkFieldType(jobject jobj, jfieldID fieldID, PrimitiveType prim, bool isStatic) {
        if (fieldID == NULL) {
            LOGW("JNI WARNING: null field ID");
            showLocation();
            abortMaybe();
        }

        bool printWarn = false;
        Field* field = (Field*) fieldID;
        if ((field->signature[0] == 'L' || field->signature[0] == '[') && jobj != NULL) {
            ScopedJniThreadState ts(mEnv);
            Object* obj = dvmDecodeIndirectRef(mEnv, jobj);
            /*
             * If jobj is a weak global ref whose referent has been cleared,
             * obj will be NULL.  Otherwise, obj should always be non-NULL
             * and valid.
             */
            if (obj != NULL && !dvmIsValidObject(obj)) {
                LOGW("JNI WARNING: field operation on invalid %s ref (%p)",
                        dvmIndirectRefTypeName(jobj), jobj);
                printWarn = true;
            } else {
                ClassObject* fieldClass = dvmFindLoadedClass(field->signature);
                ClassObject* objClass = obj->clazz;

                assert(fieldClass != NULL);
                assert(objClass != NULL);

                if (!dvmInstanceof(objClass, fieldClass)) {
                    LOGW("JNI WARNING: set field '%s' expected type %s, got %s",
                            field->name, field->signature, objClass->descriptor);
                    printWarn = true;
                }
            }
        } else if (dexGetPrimitiveTypeFromDescriptorChar(field->signature[0]) != prim) {
            LOGW("JNI WARNING: set field '%s' expected type %s, got %s",
                    field->name, field->signature, primitiveTypeToName(prim));
            printWarn = true;
        } else if (isStatic && !dvmIsStaticField(field)) {
            if (isStatic) {
                LOGW("JNI WARNING: accessing non-static field %s as static", field->name);
            } else {
                LOGW("JNI WARNING: accessing static field %s as non-static", field->name);
            }
            printWarn = true;
        }

        if (printWarn) {
            showLocation();
            abortMaybe();
        }
    }

    /*
     * Verify that this instance field ID is valid for this object.
     *
     * Assumes "jobj" has already been validated.
     */
    void checkInstanceFieldID(jobject jobj, jfieldID fieldID) {
        ScopedJniThreadState ts(mEnv);

        Object* obj = dvmDecodeIndirectRef(mEnv, jobj);
        ClassObject* clazz = obj->clazz;

        /*
         * Check this class and all of its superclasses for a matching field.
         * Don't need to scan interfaces.
         */
        while (clazz != NULL) {
            if ((InstField*) fieldID >= clazz->ifields &&
                    (InstField*) fieldID < clazz->ifields + clazz->ifieldCount) {
                return;
            }

            clazz = clazz->super;
        }

        LOGW("JNI WARNING: inst fieldID %p not valid for class %s",
                fieldID, obj->clazz->descriptor);
        showLocation();
        abortMaybe();
    }

    /*
     * Verify that the length argument to array-creation calls is >= 0.
     */
    void checkLengthPositive(jsize length) {
        if (length < 0) {
            LOGW("JNI WARNING: negative length for array allocation (%s)", mFunctionName);
            abortMaybe();
        }
    }

    /*
     * Verify that the pointer value is non-NULL.
     */
    void checkNonNull(const void* ptr) {
        if (ptr == NULL) {
            LOGW("JNI WARNING: invalid null pointer (%s)", mFunctionName);
            abortMaybe();
        }
    }

    /*
     * Verify that "jobj" is a valid object, and that it's an object that JNI
     * is allowed to know about.  We allow NULL references.
     *
     * Switches to "running" mode before performing checks.
     */
    void checkObject(jobject jobj) {
        if (jobj == NULL) {
            return;
        }

        ScopedJniThreadState ts(mEnv);

        bool printWarn = false;
        if (dvmGetJNIRefType(mEnv, jobj) == JNIInvalidRefType) {
            LOGW("JNI WARNING: %p is not a valid JNI reference (type=%s)",
            jobj, dvmIndirectRefTypeName(jobj));
            printWarn = true;
        } else {
            Object* obj = dvmDecodeIndirectRef(mEnv, jobj);

            /*
             * The decoded object will be NULL if this is a weak global ref
             * with a cleared referent.
             */
            if (obj == kInvalidIndirectRefObject || (obj != NULL && !dvmIsValidObject(obj))) {
                LOGW("JNI WARNING: native code passing in bad object %p %p", jobj, obj);
                printWarn = true;
            }
        }

        if (printWarn) {
            showLocation();
            abortMaybe();
        }
    }

    /*
     * Verify that the "mode" argument passed to a primitive array Release
     * function is one of the valid values.
     */
    void checkReleaseMode(jint mode) {
        if (mode != 0 && mode != JNI_COMMIT && mode != JNI_ABORT) {
            LOGW("JNI WARNING: bad value for mode (%d) (%s)", mode, mFunctionName);
            abortMaybe();
        }
    }

    /*
     * Verify that the method's return type matches the type of call.
     *
     * "expectedSigByte" will be 'L' for all objects, including arrays.
     */
    void checkSig(jmethodID methodID, char expectedSigByte, bool isStatic) {
        const Method* method = (const Method*) methodID;
        bool printWarn = false;

        if (expectedSigByte != method->shorty[0]) {
            LOGW("JNI WARNING: expected return type '%c'", expectedSigByte);
            printWarn = true;
        } else if (isStatic && !dvmIsStaticMethod(method)) {
            if (isStatic) {
                LOGW("JNI WARNING: calling non-static method with static call");
            } else {
                LOGW("JNI WARNING: calling static method with non-static call");
            }
            printWarn = true;
        }

        if (printWarn) {
            char* desc = dexProtoCopyMethodDescriptor(&method->prototype);
            LOGW("             calling %s.%s %s", method->clazz->descriptor, method->name, desc);
            free(desc);
            showLocation();
            abortMaybe();
        }
    }

    /*
     * Verify that this static field ID is valid for this class.
     *
     * Assumes "jclazz" has already been validated.
     */
    void checkStaticFieldID(jclass jclazz, jfieldID fieldID) {
        ScopedJniThreadState ts(mEnv);
        ClassObject* clazz = (ClassObject*) dvmDecodeIndirectRef(mEnv, jclazz);
        StaticField* base = &clazz->sfields[0];
        int fieldCount = clazz->sfieldCount;
        if ((StaticField*) fieldID < base || (StaticField*) fieldID >= base + fieldCount) {
            LOGW("JNI WARNING: static fieldID %p not valid for class %s",
                    fieldID, clazz->descriptor);
            LOGW("             base=%p count=%d", base, fieldCount);
            showLocation();
            abortMaybe();
        }
    }

    /*
     * Verify that "methodID" is appropriate for "clazz".
     *
     * A mismatch isn't dangerous, because the jmethodID defines the class.  In
     * fact, jclazz is unused in the implementation.  It's best if we don't
     * allow bad code in the system though.
     *
     * Instances of "jclazz" must be instances of the method's declaring class.
     */
    void checkStaticMethod(jclass jclazz, jmethodID methodID) {
        ScopedJniThreadState ts(mEnv);

        ClassObject* clazz = (ClassObject*) dvmDecodeIndirectRef(mEnv, jclazz);
        const Method* method = (const Method*) methodID;

        if (!dvmInstanceof(clazz, method->clazz)) {
            LOGW("JNI WARNING: can't call static %s.%s on class %s",
                    method->clazz->descriptor, method->name, clazz->descriptor);
            showLocation();
            // no abort?
        }
    }

    void checkString(jstring s) {
        checkInstance(s, gDvm.classJavaLangString, "jstring");
    }

    void checkClass(jclass c) {
        checkInstance(c, gDvm.classJavaLangClass, "jclass");
    }

    /*
     * Verify that "bytes" points to valid "modified UTF-8" data.
     * If "identifier" is NULL, "bytes" is allowed to be NULL; otherwise,
     * "identifier" is the name to use when reporting the null pointer.
     */
    void checkUtfString(const char* bytes, const char* identifier) {
        if (bytes == NULL) {
            if (identifier != NULL) {
                LOGW("JNI WARNING: %s == NULL", identifier);
                showLocation();
                abortMaybe();
            }
            return;
        }

        const char* errorKind = NULL;
        u1 utf8 = checkUtfBytes(bytes, &errorKind);
        if (errorKind != NULL) {
            LOGW("JNI WARNING: input is not valid UTF-8: illegal %s byte 0x%x", errorKind, utf8);
            LOGW("             string: '%s'", bytes);
            showLocation();
            abortMaybe();
        }
    }

    /*
     * Verify that "methodID" is appropriate for "jobj".
     *
     * Make sure the object is an instance of the method's declaring class.
     * (Note the methodID might point to a declaration in an interface; this
     * will be handled automatically by the instanceof check.)
     */
    void checkVirtualMethod(jobject jobj, jmethodID methodID) {
        ScopedJniThreadState ts(mEnv);

        Object* obj = dvmDecodeIndirectRef(mEnv, jobj);
        const Method* method = (const Method*) methodID;

        if (!dvmInstanceof(obj->clazz, method->clazz)) {
            LOGW("JNI WARNING: can't call %s.%s on instance of %s",
                    method->clazz->descriptor, method->name, obj->clazz->descriptor);
            showLocation();
            abortMaybe();
        }
    }

private:
    JNIEnv* mEnv;
    const char* mFunctionName;

    void checkThread(int flags) {
        // Get the *correct* JNIEnv by going through our TLS pointer.
        JNIEnvExt* threadEnv = dvmGetJNIEnvForThread();

        /*
         * Verify that the current thread is (a) attached and (b) associated with
         * this particular instance of JNIEnv.
         */
        bool printWarn = false;
        if (threadEnv == NULL) {
            LOGE("JNI ERROR: non-VM thread making JNI calls");
            // don't set printWarn -- it'll try to call showLocation()
            dvmAbort();
        } else if ((JNIEnvExt*) mEnv != threadEnv) {
            if (dvmThreadSelf()->threadId != threadEnv->envThreadId) {
                LOGE("JNI: threadEnv != thread->env?");
                dvmAbort();
            }

            LOGW("JNI WARNING: threadid=%d using env from threadid=%d",
                    threadEnv->envThreadId, ((JNIEnvExt*) mEnv)->envThreadId);
            printWarn = true;

            /* this is a bad idea -- need to throw as we exit, or abort func */
            //dvmThrowRuntimeException("invalid use of JNI env ptr");
        } else if (((JNIEnvExt*) mEnv)->self != dvmThreadSelf()) {
            /* correct JNIEnv*; make sure the "self" pointer is correct */
            LOGE("JNI ERROR: env->self != thread-self (%p vs. %p)",
                    ((JNIEnvExt*) mEnv)->self, dvmThreadSelf());
            dvmAbort();
        }

        /*
         * Verify that, if this thread previously made a critical "get" call, we
         * do the corresponding "release" call before we try anything else.
         */
        switch (flags & kFlag_CritMask) {
        case kFlag_CritOkay:    // okay to call this method
            break;
        case kFlag_CritBad:     // not okay to call
            if (threadEnv->critical) {
                LOGW("JNI WARNING: threadid=%d using JNI after critical get",
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
                LOGW("JNI WARNING: threadid=%d called too many crit releases",
                        threadEnv->envThreadId);
                printWarn = true;
            }
            break;
        default:
            assert(false);
        }

        /*
         * Verify that, if an exception has been raised, the native code doesn't
         * make any JNI calls other than the Exception* methods.
         */
        bool printException = false;
        if ((flags & kFlag_ExcepOkay) == 0 && dvmCheckException(dvmThreadSelf())) {
            LOGW("JNI WARNING: JNI method called with exception raised");
            printWarn = true;
            printException = true;
        }

        if (printWarn) {
            showLocation();
        }
        if (printException) {
            LOGW("Pending exception is:");
            dvmLogExceptionStackTrace();
        }
        if (printWarn) {
            abortMaybe();
        }
    }

    /*
     * Verify that "jobj" is a valid non-NULL object reference, and points to
     * an instance of expectedClass.
     *
     * Because we're looking at an object on the GC heap, we have to switch
     * to "running" mode before doing the checks.
     */
    void checkInstance(jobject jobj, ClassObject* expectedClass, const char* argName) {
        if (jobj == NULL) {
            LOGW("JNI WARNING: received null %s", argName);
            showLocation();
            abortMaybe();
            return;
        }

        ScopedJniThreadState ts(mEnv);
        bool printWarn = false;

        Object* obj = dvmDecodeIndirectRef(mEnv, jobj);

        if (!dvmIsValidObject(obj)) {
            LOGW("JNI WARNING: %s is invalid %s ref (%p)",
                    argName, dvmIndirectRefTypeName(jobj), jobj);
            printWarn = true;
        } else if (obj->clazz != expectedClass) {
            LOGW("JNI WARNING: %s arg has wrong type (expected %s, got %s)",
                    argName, expectedClass->descriptor, obj->clazz->descriptor);
            printWarn = true;
        }

        if (printWarn) {
            showLocation();
            abortMaybe();
        }
    }

    static u1 checkUtfBytes(const char* bytes, const char** errorKind) {
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
            case 0x07:
                // Bit pattern 0xxx. No need for any extra bytes.
                break;
            case 0x08:
            case 0x09:
            case 0x0a:
            case 0x0b:
            case 0x0f:
                /*
                 * Bit pattern 10xx or 1111, which are illegal start bytes.
                 * Note: 1111 is valid for normal UTF-8, but not the
                 * modified UTF-8 used here.
                 */
                *errorKind = "start";
                return utf8;
            case 0x0e:
                // Bit pattern 1110, so there are two additional bytes.
                utf8 = *(bytes++);
                if ((utf8 & 0xc0) != 0x80) {
                    *errorKind = "continuation";
                    return utf8;
                }
                // Fall through to take care of the final byte.
            case 0x0c:
            case 0x0d:
                // Bit pattern 110x, so there is one additional byte.
                utf8 = *(bytes++);
                if ((utf8 & 0xc0) != 0x80) {
                    *errorKind = "continuation";
                    return utf8;
                }
                break;
            }
        }
        return 0;
    }

    /**
     * Returns a human-readable name for the given primitive type.
     */
    static const char* primitiveTypeToName(PrimitiveType primType) {
        switch (primType) {
        case PRIM_VOID:    return "void";
        case PRIM_BOOLEAN: return "boolean";
        case PRIM_BYTE:    return "byte";
        case PRIM_SHORT:   return "short";
        case PRIM_CHAR:    return "char";
        case PRIM_INT:     return "int";
        case PRIM_LONG:    return "long";
        case PRIM_FLOAT:   return "float";
        case PRIM_DOUBLE:  return "double";
        case PRIM_NOT:     return "Object/array";
        default:           return "???";
        }
    }

    void showLocation() {
        // mFunctionName looks like "Check_DeleteLocalRef"; we drop the "Check_".
        const char* name = mFunctionName + 6;
        const Method* method = dvmGetCurrentJNIMethod();
        char* desc = dexProtoCopyMethodDescriptor(&method->prototype);
        LOGW("             in %s.%s:%s (%s)", method->clazz->descriptor, method->name, desc, name);
        free(desc);
    }

    // Disallow copy and assignment.
    ScopedCheck(const ScopedCheck&);
    void operator=(const ScopedCheck&);
};

/*
 * ===========================================================================
 *      Guarded arrays
 * ===========================================================================
 */

#define kGuardLen       512         /* must be multiple of 2 */
#define kGuardPattern   0xd5e3      /* uncommon values; d5e3d5e3 invalid addr */
#define kGuardMagic     0xffd5aa96

/* this gets tucked in at the start of the buffer; struct size must be even */
struct GuardedCopy {
    u4          magic;
    uLong       adler;
    size_t      originalLen;
    const void* originalPtr;

    /* find the GuardedCopy given the pointer into the "live" data */
    static inline GuardedCopy* fromData(const void* dataBuf) {
        u1* fullBuf = ((u1*) dataBuf) - kGuardLen / 2;
        return reinterpret_cast<GuardedCopy*>(fullBuf);
    }

    /*
     * Create an over-sized buffer to hold the contents of "buf".  Copy it in,
     * filling in the area around it with guard data.
     *
     * We use a 16-bit pattern to make a rogue memset less likely to elude us.
     */
    static void* create(const void* buf, size_t len, bool modOkay) {
        size_t newLen = (len + kGuardLen +1) & ~0x01;
        u1* newBuf = (u1*)malloc(newLen);
        if (newBuf == NULL) {
            LOGE("GuardedCopy::create failed on alloc of %d bytes", newLen);
            dvmAbort();
        }

        /* fill it in with a pattern */
        u2* pat = (u2*) newBuf;
        for (size_t i = 0; i < newLen / 2; i++) {
            *pat++ = kGuardPattern;
        }

        /* copy the data in; note "len" could be zero */
        memcpy(newBuf + kGuardLen / 2, buf, len);

        /* if modification is not expected, grab a checksum */
        uLong adler = 0;
        if (!modOkay) {
            adler = adler32(0L, Z_NULL, 0);
            adler = adler32(adler, (const Bytef*)buf, len);
            *(uLong*)newBuf = adler;
        }

        GuardedCopy* pExtra = reinterpret_cast<GuardedCopy*>(newBuf);
        pExtra->magic = kGuardMagic;
        pExtra->adler = adler;
        pExtra->originalPtr = buf;
        pExtra->originalLen = len;

        return newBuf + kGuardLen / 2;
    }

    /*
     * Free up the guard buffer, scrub it, and return the original pointer.
     */
    static void* free(void* dataBuf) {
        u1* fullBuf = ((u1*) dataBuf) - kGuardLen / 2;
        const GuardedCopy* pExtra = GuardedCopy::fromData(dataBuf);
        void* originalPtr = (void*) pExtra->originalPtr;
        size_t len = pExtra->originalLen;
        memset(dataBuf, 0xdd, len);
        free(fullBuf);
        return originalPtr;
    }

    /*
     * Verify the guard area and, if "modOkay" is false, that the data itself
     * has not been altered.
     *
     * The caller has already checked that "dataBuf" is non-NULL.
     */
    static bool check(const void* dataBuf, bool modOkay) {
        static const u4 kMagicCmp = kGuardMagic;
        const u1* fullBuf = ((const u1*) dataBuf) - kGuardLen / 2;
        const GuardedCopy* pExtra = GuardedCopy::fromData(dataBuf);

        /*
         * Before we do anything with "pExtra", check the magic number.  We
         * do the check with memcmp rather than "==" in case the pointer is
         * unaligned.  If it points to completely bogus memory we're going
         * to crash, but there's no easy way around that.
         */
        if (memcmp(&pExtra->magic, &kMagicCmp, 4) != 0) {
            u1 buf[4];
            memcpy(buf, &pExtra->magic, 4);
            LOGE("JNI: guard magic does not match (found 0x%02x%02x%02x%02x) -- incorrect data pointer %p?",
                    buf[3], buf[2], buf[1], buf[0], dataBuf); /* assume little endian */
            return false;
        }

        size_t len = pExtra->originalLen;

        /* check bottom half of guard; skip over optional checksum storage */
        const u2* pat = (u2*) fullBuf;
        for (size_t i = sizeof(GuardedCopy) / 2; i < (kGuardLen / 2 - sizeof(GuardedCopy)) / 2; i++) {
            if (pat[i] != kGuardPattern) {
                LOGE("JNI: guard pattern(1) disturbed at %p + %d", fullBuf, i*2);
                return false;
            }
        }

        int offset = kGuardLen / 2 + len;
        if (offset & 0x01) {
            /* odd byte; expected value depends on endian-ness of host */
            const u2 patSample = kGuardPattern;
            if (fullBuf[offset] != ((const u1*) &patSample)[1]) {
                LOGE("JNI: guard pattern disturbed in odd byte after %p (+%d) 0x%02x 0x%02x",
                        fullBuf, offset, fullBuf[offset], ((const u1*) &patSample)[1]);
                return false;
            }
            offset++;
        }

        /* check top half of guard */
        pat = (u2*) (fullBuf + offset);
        for (size_t i = 0; i < kGuardLen / 4; i++) {
            if (pat[i] != kGuardPattern) {
                LOGE("JNI: guard pattern(2) disturbed at %p + %d", fullBuf, offset + i*2);
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
            adler = adler32(adler, (const Bytef*)dataBuf, len);
            if (pExtra->adler != adler) {
                LOGE("JNI: buffer modified (0x%08lx vs 0x%08lx) at addr %p",
                        pExtra->adler, adler, dataBuf);
                return false;
            }
        }

        return true;
    }
};

/*
 * Return the width, in bytes, of a primitive type.
 */
static int dvmPrimitiveTypeWidth(PrimitiveType primType) {
    switch (primType) {
        case PRIM_BOOLEAN: return 1;
        case PRIM_BYTE:    return 1;
        case PRIM_SHORT:   return 2;
        case PRIM_CHAR:    return 2;
        case PRIM_INT:     return 4;
        case PRIM_LONG:    return 8;
        case PRIM_FLOAT:   return 4;
        case PRIM_DOUBLE:  return 8;
        case PRIM_VOID:
        default: {
            assert(false);
            return -1;
        }
    }
}

/*
 * Create a guarded copy of a primitive array.  Modifications to the copied
 * data are allowed.  Returns a pointer to the copied data.
 */
static void* createGuardedPACopy(JNIEnv* env, const jarray jarr, jboolean* isCopy) {
    ScopedJniThreadState ts(env);

    ArrayObject* arrObj = (ArrayObject*) dvmDecodeIndirectRef(env, jarr);
    PrimitiveType primType = arrObj->obj.clazz->elementClass->primitiveType;
    int len = arrObj->length * dvmPrimitiveTypeWidth(primType);
    void* result = GuardedCopy::create(arrObj->contents, len, true);
    if (isCopy != NULL) {
        *isCopy = JNI_TRUE;
    }
    return result;
}

/*
 * Perform the array "release" operation, which may or may not copy data
 * back into the VM, and may or may not release the underlying storage.
 */
static void* releaseGuardedPACopy(JNIEnv* env, jarray jarr, void* dataBuf, int mode) {
    ScopedJniThreadState ts(env);
    ArrayObject* arrObj = (ArrayObject*) dvmDecodeIndirectRef(env, jarr);
    bool release, copyBack;
    u1* result = NULL;

    if (!GuardedCopy::check(dataBuf, true)) {
        LOGE("JNI: failed guarded copy check in releaseGuardedPACopy");
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
        LOGE("JNI: bad release mode %d", mode);
        dvmAbort();
        return NULL;
    }

    if (copyBack) {
        size_t len = GuardedCopy::fromData(dataBuf)->originalLen;
        memcpy(arrObj->contents, dataBuf, len);
    }

    if (release) {
        result = (u1*) GuardedCopy::free(dataBuf);
    } else {
        result = (u1*) (void*) GuardedCopy::fromData(dataBuf)->originalPtr;
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

static jint Check_GetVersion(JNIEnv* env) {
    ScopedCheck sc(env, kFlag_Default, __FUNCTION__);
    return baseEnv(env)->GetVersion(env);
}

static jclass Check_DefineClass(JNIEnv* env, const char* name, jobject loader,
    const jbyte* buf, jsize bufLen)
{
    ScopedCheck sc(env, kFlag_Default, __FUNCTION__);
    sc.checkObject(loader);
    sc.checkUtfString(name, "name");
    sc.checkClassName(name);
    return baseEnv(env)->DefineClass(env, name, loader, buf, bufLen);
}

static jclass Check_FindClass(JNIEnv* env, const char* name) {
    ScopedCheck sc(env, kFlag_Default, __FUNCTION__);
    sc.checkUtfString(name, "name");
    sc.checkClassName(name);
    return baseEnv(env)->FindClass(env, name);
}

static jclass Check_GetSuperclass(JNIEnv* env, jclass clazz) {
    ScopedCheck sc(env, kFlag_Default, __FUNCTION__);
    sc.checkClass(clazz);
    return baseEnv(env)->GetSuperclass(env, clazz);
}

static jboolean Check_IsAssignableFrom(JNIEnv* env, jclass clazz1, jclass clazz2) {
    ScopedCheck sc(env, kFlag_Default, __FUNCTION__);
    sc.checkClass(clazz1);
    sc.checkClass(clazz2);
    return baseEnv(env)->IsAssignableFrom(env, clazz1, clazz2);
}

static jmethodID Check_FromReflectedMethod(JNIEnv* env, jobject method) {
    ScopedCheck sc(env, kFlag_Default, __FUNCTION__);
    sc.checkObject(method);
    return baseEnv(env)->FromReflectedMethod(env, method);
}

static jfieldID Check_FromReflectedField(JNIEnv* env, jobject field) {
    ScopedCheck sc(env, kFlag_Default, __FUNCTION__);
    sc.checkObject(field);
    return baseEnv(env)->FromReflectedField(env, field);
}

static jobject Check_ToReflectedMethod(JNIEnv* env, jclass cls,
        jmethodID methodID, jboolean isStatic)
{
    ScopedCheck sc(env, kFlag_Default, __FUNCTION__);
    sc.checkClass(cls);
    return baseEnv(env)->ToReflectedMethod(env, cls, methodID, isStatic);
}

static jobject Check_ToReflectedField(JNIEnv* env, jclass cls,
        jfieldID fieldID, jboolean isStatic)
{
    ScopedCheck sc(env, kFlag_Default, __FUNCTION__);
    sc.checkClass(cls);
    return baseEnv(env)->ToReflectedField(env, cls, fieldID, isStatic);
}

static jint Check_Throw(JNIEnv* env, jthrowable obj) {
    ScopedCheck sc(env, kFlag_Default, __FUNCTION__);
    sc.checkObject(obj);
    /* TODO: verify that "obj" is an instance of Throwable */
    return baseEnv(env)->Throw(env, obj);
}

static jint Check_ThrowNew(JNIEnv* env, jclass clazz, const char* message) {
    ScopedCheck sc(env, kFlag_Default, __FUNCTION__);
    sc.checkClass(clazz);
    sc.checkUtfString(message, NULL);
    return baseEnv(env)->ThrowNew(env, clazz, message);
}

static jthrowable Check_ExceptionOccurred(JNIEnv* env) {
    ScopedCheck sc(env, kFlag_ExcepOkay, __FUNCTION__);
    return baseEnv(env)->ExceptionOccurred(env);
}

static void Check_ExceptionDescribe(JNIEnv* env) {
    ScopedCheck sc(env, kFlag_ExcepOkay, __FUNCTION__);
    baseEnv(env)->ExceptionDescribe(env);
}

static void Check_ExceptionClear(JNIEnv* env) {
    ScopedCheck sc(env, kFlag_ExcepOkay, __FUNCTION__);
    baseEnv(env)->ExceptionClear(env);
}

static void Check_FatalError(JNIEnv* env, const char* msg) {
    ScopedCheck sc(env, kFlag_Default, __FUNCTION__);
    sc.checkUtfString(msg, NULL);
    baseEnv(env)->FatalError(env, msg);
}

static jint Check_PushLocalFrame(JNIEnv* env, jint capacity) {
    ScopedCheck sc(env, kFlag_Default | kFlag_ExcepOkay, __FUNCTION__);
    return baseEnv(env)->PushLocalFrame(env, capacity);
}

static jobject Check_PopLocalFrame(JNIEnv* env, jobject res) {
    ScopedCheck sc(env, kFlag_Default | kFlag_ExcepOkay, __FUNCTION__);
    sc.checkObject(res);
    return baseEnv(env)->PopLocalFrame(env, res);
}

static jobject Check_NewGlobalRef(JNIEnv* env, jobject obj) {
    ScopedCheck sc(env, kFlag_Default, __FUNCTION__);
    sc.checkObject(obj);
    return baseEnv(env)->NewGlobalRef(env, obj);
}

static void Check_DeleteGlobalRef(JNIEnv* env, jobject globalRef) {
    ScopedCheck sc(env, kFlag_Default | kFlag_ExcepOkay, __FUNCTION__);
    sc.checkObject(globalRef);
    if (globalRef != NULL && dvmGetJNIRefType(env, globalRef) != JNIGlobalRefType) {
        LOGW("JNI WARNING: DeleteGlobalRef on non-global %p (type=%d)",
            globalRef, dvmGetJNIRefType(env, globalRef));
        abortMaybe();
    } else {
        baseEnv(env)->DeleteGlobalRef(env, globalRef);
    }
}

static jobject Check_NewLocalRef(JNIEnv* env, jobject ref) {
    ScopedCheck sc(env, kFlag_Default, __FUNCTION__);
    sc.checkObject(ref);
    return baseEnv(env)->NewLocalRef(env, ref);
}

static void Check_DeleteLocalRef(JNIEnv* env, jobject localRef) {
    ScopedCheck sc(env, kFlag_Default | kFlag_ExcepOkay, __FUNCTION__);
    sc.checkObject(localRef);
    if (localRef != NULL && dvmGetJNIRefType(env, localRef) != JNILocalRefType) {
        LOGW("JNI WARNING: DeleteLocalRef on non-local %p (type=%d)",
            localRef, dvmGetJNIRefType(env, localRef));
        abortMaybe();
    } else {
        baseEnv(env)->DeleteLocalRef(env, localRef);
    }
}

static jint Check_EnsureLocalCapacity(JNIEnv *env, jint capacity) {
    ScopedCheck sc(env, kFlag_Default, __FUNCTION__);
    return baseEnv(env)->EnsureLocalCapacity(env, capacity);
}

static jboolean Check_IsSameObject(JNIEnv* env, jobject ref1, jobject ref2) {
    ScopedCheck sc(env, kFlag_Default, __FUNCTION__);
    sc.checkObject(ref1);
    sc.checkObject(ref2);
    return baseEnv(env)->IsSameObject(env, ref1, ref2);
}

static jobject Check_AllocObject(JNIEnv* env, jclass clazz) {
    ScopedCheck sc(env, kFlag_Default, __FUNCTION__);
    sc.checkClass(clazz);
    return baseEnv(env)->AllocObject(env, clazz);
}

static jobject Check_NewObject(JNIEnv* env, jclass clazz, jmethodID methodID, ...) {
    ScopedCheck sc(env, kFlag_Default, __FUNCTION__);
    sc.checkClass(clazz);
    va_list args;

    va_start(args, methodID);
    jobject result = baseEnv(env)->NewObjectV(env, clazz, methodID, args);
    va_end(args);

    return result;
}

static jobject Check_NewObjectV(JNIEnv* env, jclass clazz, jmethodID methodID, va_list args) {
    ScopedCheck sc(env, kFlag_Default, __FUNCTION__);
    sc.checkClass(clazz);
    return baseEnv(env)->NewObjectV(env, clazz, methodID, args);
}

static jobject Check_NewObjectA(JNIEnv* env, jclass clazz, jmethodID methodID, jvalue* args) {
    ScopedCheck sc(env, kFlag_Default, __FUNCTION__);
    sc.checkClass(clazz);
    return baseEnv(env)->NewObjectA(env, clazz, methodID, args);
}

static jclass Check_GetObjectClass(JNIEnv* env, jobject obj) {
    ScopedCheck sc(env, kFlag_Default, __FUNCTION__);
    sc.checkObject(obj);
    return baseEnv(env)->GetObjectClass(env, obj);
}

static jboolean Check_IsInstanceOf(JNIEnv* env, jobject obj, jclass clazz) {
    ScopedCheck sc(env, kFlag_Default, __FUNCTION__);
    sc.checkObject(obj);
    sc.checkClass(clazz);
    return baseEnv(env)->IsInstanceOf(env, obj, clazz);
}

static jmethodID Check_GetMethodID(JNIEnv* env, jclass clazz, const char* name, const char* sig) {
    ScopedCheck sc(env, kFlag_Default, __FUNCTION__);
    sc.checkClass(clazz);
    sc.checkUtfString(name, "name");
    sc.checkUtfString(sig, "sig");
    return baseEnv(env)->GetMethodID(env, clazz, name, sig);
}

static jfieldID Check_GetFieldID(JNIEnv* env, jclass clazz, const char* name, const char* sig) {
    ScopedCheck sc(env, kFlag_Default, __FUNCTION__);
    sc.checkClass(clazz);
    sc.checkUtfString(name, "name");
    sc.checkUtfString(sig, "sig");
    return baseEnv(env)->GetFieldID(env, clazz, name, sig);
}

static jmethodID Check_GetStaticMethodID(JNIEnv* env, jclass clazz,
        const char* name, const char* sig)
{
    ScopedCheck sc(env, kFlag_Default, __FUNCTION__);
    sc.checkClass(clazz);
    sc.checkUtfString(name, "name");
    sc.checkUtfString(sig, "sig");
    return baseEnv(env)->GetStaticMethodID(env, clazz, name, sig);
}

static jfieldID Check_GetStaticFieldID(JNIEnv* env, jclass clazz,
        const char* name, const char* sig)
{
    ScopedCheck sc(env, kFlag_Default, __FUNCTION__);
    sc.checkClass(clazz);
    sc.checkUtfString(name, "name");
    sc.checkUtfString(sig, "sig");
    return baseEnv(env)->GetStaticFieldID(env, clazz, name, sig);
}

#define GET_STATIC_TYPE_FIELD(_ctype, _jname) \
    static _ctype Check_GetStatic##_jname##Field(JNIEnv* env, jclass clazz, jfieldID fieldID) \
    { \
        ScopedCheck sc(env, kFlag_Default, __FUNCTION__); \
        sc.checkClass(clazz); \
        sc.checkStaticFieldID(clazz, fieldID); \
        return baseEnv(env)->GetStatic##_jname##Field(env, clazz, fieldID); \
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

#define SET_STATIC_TYPE_FIELD(_ctype, _jname, _ftype) \
    static void Check_SetStatic##_jname##Field(JNIEnv* env, jclass clazz, \
        jfieldID fieldID, _ctype value) { \
        ScopedCheck sc(env, kFlag_Default, __FUNCTION__); \
        sc.checkClass(clazz); \
        sc.checkStaticFieldID(clazz, fieldID); \
        /* "value" arg only used when type == ref */ \
        sc.checkFieldType((jobject)(u4)value, fieldID, _ftype, true); \
        baseEnv(env)->SetStatic##_jname##Field(env, clazz, fieldID, value); \
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
    static _ctype Check_Get##_jname##Field(JNIEnv* env, jobject obj, jfieldID fieldID) { \
        ScopedCheck sc(env, kFlag_Default, __FUNCTION__); \
        sc.checkObject(obj); \
        sc.checkInstanceFieldID(obj, fieldID); \
        return baseEnv(env)->Get##_jname##Field(env, obj, fieldID);      \
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

#define SET_TYPE_FIELD(_ctype, _jname, _ftype) \
    static void Check_Set##_jname##Field(JNIEnv* env, jobject obj, jfieldID fieldID, _ctype value) \
    { \
        ScopedCheck sc(env, kFlag_Default, __FUNCTION__); \
        sc.checkObject(obj); \
        sc.checkInstanceFieldID(obj, fieldID); \
        /* "value" arg only used when type == ref */ \
        sc.checkFieldType((jobject)(u4) value, fieldID, _ftype, false); \
        baseEnv(env)->Set##_jname##Field(env, obj, fieldID, value); \
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
        ScopedCheck sc(env, kFlag_Default, __FUNCTION__); \
        sc.checkObject(obj); \
        sc.checkSig(methodID, _retsig, false); \
        sc.checkVirtualMethod(obj, methodID); \
        _retdecl;                                                           \
        va_list args;                                                       \
        va_start(args, methodID);                                           \
        _retasgn baseEnv(env)->Call##_jname##MethodV(env, obj, methodID,   \
            args);                                                          \
        va_end(args);                                                       \
        return _retok;                                                      \
    }                                                                       \
    static _ctype Check_Call##_jname##MethodV(JNIEnv* env, jobject obj,     \
        jmethodID methodID, va_list args)                                   \
    {                                                                       \
        ScopedCheck sc(env, kFlag_Default, __FUNCTION__); \
        sc.checkObject(obj); \
        sc.checkSig(methodID, _retsig, false); \
        sc.checkVirtualMethod(obj, methodID); \
        _retdecl;                                                           \
        _retasgn baseEnv(env)->Call##_jname##MethodV(env, obj, methodID,   \
            args);                                                          \
        return _retok;                                                      \
    }                                                                       \
    static _ctype Check_Call##_jname##MethodA(JNIEnv* env, jobject obj,     \
        jmethodID methodID, jvalue* args)                                   \
    {                                                                       \
        ScopedCheck sc(env, kFlag_Default, __FUNCTION__); \
        sc.checkObject(obj); \
        sc.checkSig(methodID, _retsig, false); \
        sc.checkVirtualMethod(obj, methodID); \
        _retdecl;                                                           \
        _retasgn baseEnv(env)->Call##_jname##MethodA(env, obj, methodID,   \
            args);                                                          \
        return _retok;                                                      \
    }
CALL_VIRTUAL(jobject, Object, Object* result, result=(Object*), (jobject) result, 'L');
CALL_VIRTUAL(jboolean, Boolean, jboolean result, result=, (jboolean) result, 'Z');
CALL_VIRTUAL(jbyte, Byte, jbyte result, result=, (jbyte) result, 'B');
CALL_VIRTUAL(jchar, Char, jchar result, result=, (jchar) result, 'C');
CALL_VIRTUAL(jshort, Short, jshort result, result=, (jshort) result, 'S');
CALL_VIRTUAL(jint, Int, jint result, result=, (jint) result, 'I');
CALL_VIRTUAL(jlong, Long, jlong result, result=, (jlong) result, 'J');
CALL_VIRTUAL(jfloat, Float, jfloat result, result=, (jfloat) result, 'F');
CALL_VIRTUAL(jdouble, Double, jdouble result, result=, (jdouble) result, 'D');
CALL_VIRTUAL(void, Void, , , , 'V');

#define CALL_NONVIRTUAL(_ctype, _jname, _retdecl, _retasgn, _retok,         \
        _retsig)                                                            \
    static _ctype Check_CallNonvirtual##_jname##Method(JNIEnv* env,         \
        jobject obj, jclass clazz, jmethodID methodID, ...)                 \
    {                                                                       \
        ScopedCheck sc(env, kFlag_Default, __FUNCTION__); \
        sc.checkClass(clazz); \
        sc.checkObject(obj); \
        sc.checkSig(methodID, _retsig, false); \
        sc.checkVirtualMethod(obj, methodID); \
        _retdecl;                                                           \
        va_list args;                                                       \
        va_start(args, methodID);                                           \
        _retasgn baseEnv(env)->CallNonvirtual##_jname##MethodV(env, obj,   \
            clazz, methodID, args);                                         \
        va_end(args);                                                       \
        return _retok;                                                      \
    }                                                                       \
    static _ctype Check_CallNonvirtual##_jname##MethodV(JNIEnv* env,        \
        jobject obj, jclass clazz, jmethodID methodID, va_list args)        \
    {                                                                       \
        ScopedCheck sc(env, kFlag_Default, __FUNCTION__); \
        sc.checkClass(clazz); \
        sc.checkObject(obj); \
        sc.checkSig(methodID, _retsig, false); \
        sc.checkVirtualMethod(obj, methodID); \
        _retdecl;                                                           \
        _retasgn baseEnv(env)->CallNonvirtual##_jname##MethodV(env, obj,   \
            clazz, methodID, args);                                         \
        return _retok;                                                      \
    }                                                                       \
    static _ctype Check_CallNonvirtual##_jname##MethodA(JNIEnv* env,        \
        jobject obj, jclass clazz, jmethodID methodID, jvalue* args)        \
    {                                                                       \
        ScopedCheck sc(env, kFlag_Default, __FUNCTION__); \
        sc.checkClass(clazz); \
        sc.checkObject(obj); \
        sc.checkSig(methodID, _retsig, false); \
        sc.checkVirtualMethod(obj, methodID); \
        _retdecl;                                                           \
        _retasgn baseEnv(env)->CallNonvirtual##_jname##MethodA(env, obj,   \
            clazz, methodID, args);                                         \
        return _retok;                                                      \
    }
CALL_NONVIRTUAL(jobject, Object, Object* result, result=(Object*), (jobject) result, 'L');
CALL_NONVIRTUAL(jboolean, Boolean, jboolean result, result=, (jboolean) result, 'Z');
CALL_NONVIRTUAL(jbyte, Byte, jbyte result, result=, (jbyte) result, 'B');
CALL_NONVIRTUAL(jchar, Char, jchar result, result=, (jchar) result, 'C');
CALL_NONVIRTUAL(jshort, Short, jshort result, result=, (jshort) result, 'S');
CALL_NONVIRTUAL(jint, Int, jint result, result=, (jint) result, 'I');
CALL_NONVIRTUAL(jlong, Long, jlong result, result=, (jlong) result, 'J');
CALL_NONVIRTUAL(jfloat, Float, jfloat result, result=, (jfloat) result, 'F');
CALL_NONVIRTUAL(jdouble, Double, jdouble result, result=, (jdouble) result, 'D');
CALL_NONVIRTUAL(void, Void, , , , 'V');


#define CALL_STATIC(_ctype, _jname, _retdecl, _retasgn, _retok, _retsig)    \
    static _ctype Check_CallStatic##_jname##Method(JNIEnv* env,             \
        jclass clazz, jmethodID methodID, ...)                              \
    {                                                                       \
        ScopedCheck sc(env, kFlag_Default, __FUNCTION__); \
        sc.checkClass(clazz); \
        sc.checkSig(methodID, _retsig, true); \
        sc.checkStaticMethod(clazz, methodID); \
        _retdecl;                                                           \
        va_list args;                                                       \
        va_start(args, methodID);                                           \
        _retasgn baseEnv(env)->CallStatic##_jname##MethodV(env, clazz,     \
            methodID, args);                                                \
        va_end(args);                                                       \
        return _retok;                                                      \
    }                                                                       \
    static _ctype Check_CallStatic##_jname##MethodV(JNIEnv* env,            \
        jclass clazz, jmethodID methodID, va_list args)                     \
    {                                                                       \
        ScopedCheck sc(env, kFlag_Default, __FUNCTION__); \
        sc.checkClass(clazz); \
        sc.checkSig(methodID, _retsig, true); \
        sc.checkStaticMethod(clazz, methodID); \
        _retdecl;                                                           \
        _retasgn baseEnv(env)->CallStatic##_jname##MethodV(env, clazz,     \
            methodID, args);                                                \
        return _retok;                                                      \
    }                                                                       \
    static _ctype Check_CallStatic##_jname##MethodA(JNIEnv* env,            \
        jclass clazz, jmethodID methodID, jvalue* args)                     \
    {                                                                       \
        ScopedCheck sc(env, kFlag_Default, __FUNCTION__); \
        sc.checkClass(clazz); \
        sc.checkSig(methodID, _retsig, true); \
        sc.checkStaticMethod(clazz, methodID); \
        _retdecl;                                                           \
        _retasgn baseEnv(env)->CallStatic##_jname##MethodA(env, clazz,     \
            methodID, args);                                                \
        return _retok;                                                      \
    }
CALL_STATIC(jobject, Object, Object* result, result=(Object*), (jobject) result, 'L');
CALL_STATIC(jboolean, Boolean, jboolean result, result=, (jboolean) result, 'Z');
CALL_STATIC(jbyte, Byte, jbyte result, result=, (jbyte) result, 'B');
CALL_STATIC(jchar, Char, jchar result, result=, (jchar) result, 'C');
CALL_STATIC(jshort, Short, jshort result, result=, (jshort) result, 'S');
CALL_STATIC(jint, Int, jint result, result=, (jint) result, 'I');
CALL_STATIC(jlong, Long, jlong result, result=, (jlong) result, 'J');
CALL_STATIC(jfloat, Float, jfloat result, result=, (jfloat) result, 'F');
CALL_STATIC(jdouble, Double, jdouble result, result=, (jdouble) result, 'D');
CALL_STATIC(void, Void, , , , 'V');

static jstring Check_NewString(JNIEnv* env, const jchar* unicodeChars, jsize len) {
    ScopedCheck sc(env, kFlag_Default, __FUNCTION__);
    return baseEnv(env)->NewString(env, unicodeChars, len);
}

static jsize Check_GetStringLength(JNIEnv* env, jstring string) {
    ScopedCheck sc(env, kFlag_CritOkay, __FUNCTION__);
    sc.checkString(string);
    return baseEnv(env)->GetStringLength(env, string);
}

static const jchar* Check_GetStringChars(JNIEnv* env, jstring string, jboolean* isCopy) {
    ScopedCheck sc(env, kFlag_CritOkay, __FUNCTION__);
    sc.checkString(string);
    const jchar* result = baseEnv(env)->GetStringChars(env, string, isCopy);
    if (((JNIEnvExt*)env)->forceDataCopy && result != NULL) {
        ScopedJniThreadState ts(env);
        StringObject* strObj = (StringObject*) dvmDecodeIndirectRef(env, string);
        int byteCount = dvmStringLen(strObj) * 2;
        result = (const jchar*) GuardedCopy::create(result, byteCount, false);
        if (isCopy != NULL) {
            *isCopy = JNI_TRUE;
        }
    }
    return result;
}

static void Check_ReleaseStringChars(JNIEnv* env, jstring string, const jchar* chars) {
    ScopedCheck sc(env, kFlag_Default | kFlag_ExcepOkay, __FUNCTION__);
    sc.checkString(string);
    sc.checkNonNull(chars);
    if (((JNIEnvExt*)env)->forceDataCopy) {
        if (!GuardedCopy::check(chars, false)) {
            LOGE("JNI: failed guarded copy check in ReleaseStringChars");
            abortMaybe();
            return;
        }
        chars = (const jchar*) GuardedCopy::free((jchar*)chars);
    }
    baseEnv(env)->ReleaseStringChars(env, string, chars);
}

static jstring Check_NewStringUTF(JNIEnv* env, const char* bytes) {
    ScopedCheck sc(env, kFlag_Default, __FUNCTION__);
    sc.checkUtfString(bytes, NULL);
    return baseEnv(env)->NewStringUTF(env, bytes);
}

static jsize Check_GetStringUTFLength(JNIEnv* env, jstring string) {
    ScopedCheck sc(env, kFlag_CritOkay, __FUNCTION__);
    sc.checkString(string);
    return baseEnv(env)->GetStringUTFLength(env, string);
}

static const char* Check_GetStringUTFChars(JNIEnv* env, jstring string, jboolean* isCopy) {
    ScopedCheck sc(env, kFlag_CritOkay, __FUNCTION__);
    sc.checkString(string);
    const char* result;
    result = baseEnv(env)->GetStringUTFChars(env, string, isCopy);
    if (((JNIEnvExt*)env)->forceDataCopy && result != NULL) {
        result = (const char*) GuardedCopy::create(result, strlen(result)+1, false);
        if (isCopy != NULL) {
            *isCopy = JNI_TRUE;
        }
    }
    return result;
}

static void Check_ReleaseStringUTFChars(JNIEnv* env, jstring string, const char* utf) {
    ScopedCheck sc(env, kFlag_ExcepOkay, __FUNCTION__);
    sc.checkString(string);
    sc.checkNonNull(utf);
    if (((JNIEnvExt*)env)->forceDataCopy) {
        //int len = dvmStringUtf8ByteLen(string) + 1;
        if (!GuardedCopy::check(utf, false)) {
            LOGE("JNI: failed guarded copy check in ReleaseStringUTFChars");
            abortMaybe();
            return;
        }
        utf = (const char*) GuardedCopy::free((char*)utf);
    }
    baseEnv(env)->ReleaseStringUTFChars(env, string, utf);
}

static jsize Check_GetArrayLength(JNIEnv* env, jarray array) {
    ScopedCheck sc(env, kFlag_CritOkay, __FUNCTION__);
    sc.checkArray(array);
    return baseEnv(env)->GetArrayLength(env, array);
}

static jobjectArray Check_NewObjectArray(JNIEnv* env, jsize length,
    jclass elementClass, jobject initialElement)
{
    ScopedCheck sc(env, kFlag_Default, __FUNCTION__);
    sc.checkClass(elementClass);
    sc.checkObject(initialElement);
    sc.checkLengthPositive(length);
    return baseEnv(env)->NewObjectArray(env, length, elementClass, initialElement);
}

static jobject Check_GetObjectArrayElement(JNIEnv* env, jobjectArray array, jsize index) {
    ScopedCheck sc(env, kFlag_Default, __FUNCTION__);
    sc.checkArray(array);
    return baseEnv(env)->GetObjectArrayElement(env, array, index);
}

static void Check_SetObjectArrayElement(JNIEnv* env, jobjectArray array, jsize index, jobject value)
{
    ScopedCheck sc(env, kFlag_Default, __FUNCTION__);
    sc.checkArray(array);
    baseEnv(env)->SetObjectArrayElement(env, array, index, value);
}

#define NEW_PRIMITIVE_ARRAY(_artype, _jname) \
    static _artype Check_New##_jname##Array(JNIEnv* env, jsize length) { \
        ScopedCheck sc(env, kFlag_Default, __FUNCTION__); \
        sc.checkLengthPositive(length); \
        return baseEnv(env)->New##_jname##Array(env, length); \
    }
NEW_PRIMITIVE_ARRAY(jbooleanArray, Boolean);
NEW_PRIMITIVE_ARRAY(jbyteArray, Byte);
NEW_PRIMITIVE_ARRAY(jcharArray, Char);
NEW_PRIMITIVE_ARRAY(jshortArray, Short);
NEW_PRIMITIVE_ARRAY(jintArray, Int);
NEW_PRIMITIVE_ARRAY(jlongArray, Long);
NEW_PRIMITIVE_ARRAY(jfloatArray, Float);
NEW_PRIMITIVE_ARRAY(jdoubleArray, Double);


/*
 * Hack to allow forcecopy to work with jniGetNonMovableArrayElements.
 * The code deliberately uses an invalid sequence of operations, so we
 * need to pass it through unmodified.  Review that code before making
 * any changes here.
 */
#define kNoCopyMagic    0xd5aab57f

#define GET_PRIMITIVE_ARRAY_ELEMENTS(_ctype, _jname)                        \
    static _ctype* Check_Get##_jname##ArrayElements(JNIEnv* env,            \
        _ctype##Array array, jboolean* isCopy)                              \
    {                                                                       \
        ScopedCheck sc(env, kFlag_Default, __FUNCTION__); \
        sc.checkArray(array); \
        _ctype* result;                                                     \
        u4 noCopy = 0;                                                      \
        if (((JNIEnvExt*)env)->forceDataCopy && isCopy != NULL) {           \
            /* capture this before the base call tramples on it */          \
            noCopy = *(u4*) isCopy;                                         \
        }                                                                   \
        result = baseEnv(env)->Get##_jname##ArrayElements(env, array, isCopy); \
        if (((JNIEnvExt*)env)->forceDataCopy && result != NULL) {           \
            if (noCopy == kNoCopyMagic) {                                   \
                LOGV("FC: not copying %p %x", array, noCopy); \
            } else {                                                        \
                result = (_ctype*) createGuardedPACopy(env, array, isCopy); \
            }                                                               \
        }                                                                   \
        return result;                                                      \
    }

#define RELEASE_PRIMITIVE_ARRAY_ELEMENTS(_ctype, _jname)                    \
    static void Check_Release##_jname##ArrayElements(JNIEnv* env,           \
        _ctype##Array array, _ctype* elems, jint mode)                      \
    {                                                                       \
        ScopedCheck sc(env, kFlag_Default | kFlag_ExcepOkay, __FUNCTION__); \
        sc.checkArray(array);                                            \
        sc.checkNonNull(elems); \
        sc.checkReleaseMode(mode); \
        if (((JNIEnvExt*)env)->forceDataCopy) {                             \
            if ((uintptr_t)elems == kNoCopyMagic) {                         \
                LOGV("FC: not freeing %p", array); \
                elems = NULL;   /* base JNI call doesn't currently need */  \
            } else {                                                        \
                elems = (_ctype*) releaseGuardedPACopy(env, array, elems,   \
                        mode);                                              \
            }                                                               \
        }                                                                   \
        baseEnv(env)->Release##_jname##ArrayElements(env, array, elems, mode); \
    }

#define GET_PRIMITIVE_ARRAY_REGION(_ctype, _jname) \
    static void Check_Get##_jname##ArrayRegion(JNIEnv* env, \
        _ctype##Array array, jsize start, jsize len, _ctype* buf) { \
        ScopedCheck sc(env, kFlag_Default, __FUNCTION__); \
        sc.checkArray(array); \
        baseEnv(env)->Get##_jname##ArrayRegion(env, array, start, len, buf); \
    }

#define SET_PRIMITIVE_ARRAY_REGION(_ctype, _jname) \
    static void Check_Set##_jname##ArrayRegion(JNIEnv* env, \
        _ctype##Array array, jsize start, jsize len, const _ctype* buf) { \
        ScopedCheck sc(env, kFlag_Default, __FUNCTION__); \
        sc.checkArray(array); \
        baseEnv(env)->Set##_jname##ArrayRegion(env, array, start, len, buf); \
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

static jint Check_RegisterNatives(JNIEnv* env, jclass clazz, const JNINativeMethod* methods,
        jint nMethods)
{
    ScopedCheck sc(env, kFlag_Default, __FUNCTION__);
    sc.checkClass(clazz);
    return baseEnv(env)->RegisterNatives(env, clazz, methods, nMethods);
}

static jint Check_UnregisterNatives(JNIEnv* env, jclass clazz) {
    ScopedCheck sc(env, kFlag_Default, __FUNCTION__);
    sc.checkClass(clazz);
    return baseEnv(env)->UnregisterNatives(env, clazz);
}

static jint Check_MonitorEnter(JNIEnv* env, jobject obj) {
    ScopedCheck sc(env, kFlag_Default, __FUNCTION__);
    sc.checkObject(obj);
    return baseEnv(env)->MonitorEnter(env, obj);
}

static jint Check_MonitorExit(JNIEnv* env, jobject obj) {
    ScopedCheck sc(env, kFlag_Default | kFlag_ExcepOkay, __FUNCTION__);
    sc.checkObject(obj);
    return baseEnv(env)->MonitorExit(env, obj);
}

static jint Check_GetJavaVM(JNIEnv *env, JavaVM **vm) {
    ScopedCheck sc(env, kFlag_Default, __FUNCTION__);
    return baseEnv(env)->GetJavaVM(env, vm);
}

static void Check_GetStringRegion(JNIEnv* env, jstring str, jsize start, jsize len, jchar* buf) {
    ScopedCheck sc(env, kFlag_CritOkay, __FUNCTION__);
    sc.checkString(str);
    baseEnv(env)->GetStringRegion(env, str, start, len, buf);
}

static void Check_GetStringUTFRegion(JNIEnv* env, jstring str, jsize start, jsize len, char* buf) {
    ScopedCheck sc(env, kFlag_CritOkay, __FUNCTION__);
    sc.checkString(str);
    baseEnv(env)->GetStringUTFRegion(env, str, start, len, buf);
}

static void* Check_GetPrimitiveArrayCritical(JNIEnv* env, jarray array, jboolean* isCopy) {
    ScopedCheck sc(env, kFlag_CritGet, __FUNCTION__);
    sc.checkArray(array);
    void* result;
    result = baseEnv(env)->GetPrimitiveArrayCritical(env, array, isCopy);
    if (((JNIEnvExt*)env)->forceDataCopy && result != NULL) {
        result = createGuardedPACopy(env, array, isCopy);
    }
    return result;
}

static void Check_ReleasePrimitiveArrayCritical(JNIEnv* env, jarray array, void* carray, jint mode)
{
    ScopedCheck sc(env, kFlag_CritRelease | kFlag_ExcepOkay, __FUNCTION__);
    sc.checkArray(array);
    sc.checkNonNull(carray);
    sc.checkReleaseMode(mode);
    if (((JNIEnvExt*)env)->forceDataCopy) {
        carray = releaseGuardedPACopy(env, array, carray, mode);
    }
    baseEnv(env)->ReleasePrimitiveArrayCritical(env, array, carray, mode);
}

static const jchar* Check_GetStringCritical(JNIEnv* env, jstring string, jboolean* isCopy) {
    ScopedCheck sc(env, kFlag_CritGet, __FUNCTION__);
    sc.checkString(string);
    const jchar* result;
    result = baseEnv(env)->GetStringCritical(env, string, isCopy);
    if (((JNIEnvExt*)env)->forceDataCopy && result != NULL) {
        ScopedJniThreadState ts(env);
        StringObject* strObj = (StringObject*) dvmDecodeIndirectRef(env, string);
        int byteCount = dvmStringLen(strObj) * 2;
        result = (const jchar*) GuardedCopy::create(result, byteCount, false);
        if (isCopy != NULL) {
            *isCopy = JNI_TRUE;
        }
    }
    return result;
}

static void Check_ReleaseStringCritical(JNIEnv* env, jstring string, const jchar* carray) {
    ScopedCheck sc(env, kFlag_CritRelease | kFlag_ExcepOkay, __FUNCTION__);
    sc.checkString(string);
    sc.checkNonNull(carray);
    if (((JNIEnvExt*)env)->forceDataCopy) {
        if (!GuardedCopy::check(carray, false)) {
            LOGE("JNI: failed guarded copy check in ReleaseStringCritical");
            abortMaybe();
            return;
        }
        carray = (const jchar*) GuardedCopy::free((jchar*)carray);
    }
    baseEnv(env)->ReleaseStringCritical(env, string, carray);
}

static jweak Check_NewWeakGlobalRef(JNIEnv* env, jobject obj) {
    ScopedCheck sc(env, kFlag_Default, __FUNCTION__);
    sc.checkObject(obj);
    return baseEnv(env)->NewWeakGlobalRef(env, obj);
}

static void Check_DeleteWeakGlobalRef(JNIEnv* env, jweak obj) {
    ScopedCheck sc(env, kFlag_Default | kFlag_ExcepOkay, __FUNCTION__);
    sc.checkObject(obj);
    baseEnv(env)->DeleteWeakGlobalRef(env, obj);
}

static jboolean Check_ExceptionCheck(JNIEnv* env) {
    ScopedCheck sc(env, kFlag_CritOkay | kFlag_ExcepOkay, __FUNCTION__);
    return baseEnv(env)->ExceptionCheck(env);
}

static jobjectRefType Check_GetObjectRefType(JNIEnv* env, jobject obj) {
    ScopedCheck sc(env, kFlag_Default, __FUNCTION__);
    sc.checkObject(obj);
    return baseEnv(env)->GetObjectRefType(env, obj);
}

static jobject Check_NewDirectByteBuffer(JNIEnv* env, void* address, jlong capacity) {
    ScopedCheck sc(env, kFlag_Default, __FUNCTION__);
    if (address == NULL || capacity < 0) {
        LOGW("JNI WARNING: invalid values for address (%p) or capacity (%ld)",
            address, (long) capacity);
        abortMaybe();
        return NULL;
    }
    return baseEnv(env)->NewDirectByteBuffer(env, address, capacity);
}

static void* Check_GetDirectBufferAddress(JNIEnv* env, jobject buf) {
    ScopedCheck sc(env, kFlag_Default, __FUNCTION__);
    sc.checkObject(buf);
    return baseEnv(env)->GetDirectBufferAddress(env, buf);
}

static jlong Check_GetDirectBufferCapacity(JNIEnv* env, jobject buf) {
    ScopedCheck sc(env, kFlag_Default, __FUNCTION__);
    sc.checkObject(buf);
    /* TODO: verify "buf" is an instance of java.nio.Buffer */
    return baseEnv(env)->GetDirectBufferCapacity(env, buf);
}


/*
 * ===========================================================================
 *      JNI invocation functions
 * ===========================================================================
 */

static jint Check_DestroyJavaVM(JavaVM* vm) {
    ScopedVmCheck svc(false, __FUNCTION__);
    return baseVm(vm)->DestroyJavaVM(vm);
}

static jint Check_AttachCurrentThread(JavaVM* vm, JNIEnv** p_env, void* thr_args) {
    ScopedVmCheck svc(false, __FUNCTION__);
    return baseVm(vm)->AttachCurrentThread(vm, p_env, thr_args);
}

static jint Check_AttachCurrentThreadAsDaemon(JavaVM* vm, JNIEnv** p_env, void* thr_args) {
    ScopedVmCheck svc(false, __FUNCTION__);
    return baseVm(vm)->AttachCurrentThreadAsDaemon(vm, p_env, thr_args);
}

static jint Check_DetachCurrentThread(JavaVM* vm) {
    ScopedVmCheck svc(true, __FUNCTION__);
    return baseVm(vm)->DetachCurrentThread(vm);
}

static jint Check_GetEnv(JavaVM* vm, void** env, jint version) {
    ScopedVmCheck svc(true, __FUNCTION__);
    return baseVm(vm)->GetEnv(vm, env, version);
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
void dvmUseCheckedJniEnv(JNIEnvExt* pEnv) {
    assert(pEnv->funcTable != &gCheckNativeInterface);
    pEnv->baseFuncTable = pEnv->funcTable;
    pEnv->funcTable = &gCheckNativeInterface;
}

/*
 * Replace the normal table with the checked table.
 */
void dvmUseCheckedJniVm(JavaVMExt* pVm) {
    assert(pVm->funcTable != &gCheckInvokeInterface);
    pVm->baseFuncTable = pVm->funcTable;
    pVm->funcTable = &gCheckInvokeInterface;
}
