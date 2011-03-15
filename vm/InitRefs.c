/*
 * Copyright (C) 2011 The Android Open Source Project
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
 * Code to initialize references to classes and members for use by
 * lower-level VM facilities
 */

#include "Dalvik.h"

/*
 * Helper for dvmInitRequiredClassesAndMembers(), which looks up
 * classes and stores them to the indicated pointer, returning a
 * failure code (false == failure).
 */
static bool initClassReference(ClassObject** pClass, const char* name) {
    ClassObject* result;

    if (*pClass != NULL) {
        /*
         * There are a couple cases where it's legit to call this
         * function with an already-initialized reference, so just
         * silently tolerate this instead of complaining loudly.
         */
        return true;
    }

    if (name[0] == '[') {
        result = dvmFindArrayClass(name, NULL);
    } else {
        result = dvmFindSystemClassNoInit(name);
    }

    if (result == NULL) {
        LOGE("Could not find essential class %s\n", name);
        return false;
    }

    *pClass = result;
    return true;
}

static bool initClassReferences(void) {
    static struct { ClassObject** ref; const char* name; } classes[] = {

        /* The corest of the core classes */
        { &gDvm.classJavaLangClass,  "Ljava/lang/Class;" },
        { &gDvm.classJavaLangObject, "Ljava/lang/Object;" },
        { &gDvm.exThrowable,         "Ljava/lang/Throwable;" },

        /* Slightly less core, but still down there, classes */
        { &gDvm.classJavaLangClassArray,             "[Ljava/lang/Class;" },
        { &gDvm.classJavaLangObjectArray,            "[Ljava/lang/Object;"},
        { &gDvm.classJavaLangStackTraceElement,      "Ljava/lang/StackTraceElement;" },
        { &gDvm.classJavaLangStackTraceElementArray, "[Ljava/lang/StackTraceElement;" },
        { &gDvm.classJavaLangString,                 "Ljava/lang/String;" },
        { &gDvm.classJavaLangThread,                 "Ljava/lang/Thread;" },
        { &gDvm.classJavaLangThreadGroup,            "Ljava/lang/ThreadGroup;" },
        { &gDvm.classJavaLangVMThread,               "Ljava/lang/VMThread;" },

        /* Arrays of primitive types */
        { &gDvm.classArrayBoolean, "[Z" },
        { &gDvm.classArrayByte,    "[B" },
        { &gDvm.classArrayShort,   "[S" },
        { &gDvm.classArrayChar,    "[C" },
        { &gDvm.classArrayInt,     "[I" },
        { &gDvm.classArrayLong,    "[J" },
        { &gDvm.classArrayFloat,   "[F" },
        { &gDvm.classArrayDouble,  "[D" },

        /* Exception classes */
        { &gDvm.exAbstractMethodError,             "Ljava/lang/AbstractMethodError;" },
        { &gDvm.exArithmeticException,             "Ljava/lang/ArithmeticException;" },
        { &gDvm.exArrayIndexOutOfBoundsException,  "Ljava/lang/ArrayIndexOutOfBoundsException;" },
        { &gDvm.exArrayStoreException,             "Ljava/lang/ArrayStoreException;" },
        { &gDvm.exClassCastException,              "Ljava/lang/ClassCastException;" },
        { &gDvm.exClassCircularityError,           "Ljava/lang/ClassCircularityError;" },
        { &gDvm.exClassNotFoundException,          "Ljava/lang/ClassNotFoundException;" },
        { &gDvm.exClassFormatError,                "Ljava/lang/ClassFormatError;" },
        { &gDvm.exError,                           "Ljava/lang/Error;" },
        { &gDvm.exExceptionInInitializerError,     "Ljava/lang/ExceptionInInitializerError;" },
        { &gDvm.exFileNotFoundException,           "Ljava/io/FileNotFoundException;" },
        { &gDvm.exIOException,                     "Ljava/io/IOException;" },
        { &gDvm.exIllegalAccessError,              "Ljava/lang/IllegalAccessError;" },
        { &gDvm.exIllegalAccessException,          "Ljava/lang/IllegalAccessException;" },
        { &gDvm.exIllegalArgumentException,        "Ljava/lang/IllegalArgumentException;" },
        { &gDvm.exIllegalMonitorStateException,    "Ljava/lang/IllegalMonitorStateException;" },
        { &gDvm.exIllegalStateException,           "Ljava/lang/IllegalStateException;" },
        { &gDvm.exIllegalThreadStateException,     "Ljava/lang/IllegalThreadStateException;" },
        { &gDvm.exIncompatibleClassChangeError,    "Ljava/lang/IncompatibleClassChangeError;" },
        { &gDvm.exInstantiationError,              "Ljava/lang/InstantiationError;" },
        { &gDvm.exInstantiationException,          "Ljava/lang/InstantiationException;" },
        { &gDvm.exInternalError,                   "Ljava/lang/InternalError;" },
        { &gDvm.exInterruptedException,            "Ljava/lang/InterruptedException;" },
        { &gDvm.exLinkageError,                    "Ljava/lang/LinkageError;" },
        { &gDvm.exNegativeArraySizeException,      "Ljava/lang/NegativeArraySizeException;" },
        { &gDvm.exNoClassDefFoundError,            "Ljava/lang/NoClassDefFoundError;" },
        { &gDvm.exNoSuchFieldError,                "Ljava/lang/NoSuchFieldError;" },
        { &gDvm.exNoSuchFieldException,            "Ljava/lang/NoSuchFieldException;" },
        { &gDvm.exNoSuchMethodError,               "Ljava/lang/NoSuchMethodError;" },
        { &gDvm.exNullPointerException,            "Ljava/lang/NullPointerException;" },
        { &gDvm.exOutOfMemoryError,                "Ljava/lang/OutOfMemoryError;" },
        { &gDvm.exRuntimeException,                "Ljava/lang/RuntimeException;" },
        { &gDvm.exStackOverflowError,              "Ljava/lang/StackOverflowError;" },
        { &gDvm.exStaleDexCacheError,              "Ldalvik/system/StaleDexCacheError;" },
        { &gDvm.exStringIndexOutOfBoundsException, "Ljava/lang/StringIndexOutOfBoundsException;" },
        { &gDvm.exTypeNotPresentException,         "Ljava/lang/TypeNotPresentException;" },
        { &gDvm.exUnsatisfiedLinkError,            "Ljava/lang/UnsatisfiedLinkError;" },
        { &gDvm.exUnsupportedOperationException,   "Ljava/lang/UnsupportedOperationException;" },
        { &gDvm.exVerifyError,                     "Ljava/lang/VerifyError;" },
        { &gDvm.exVirtualMachineError,             "Ljava/lang/VirtualMachineError;" },

        /* Other classes */
        { &gDvm.classJavaLangAnnotationAnnotationArray, "[Ljava/lang/annotation/Annotation;" },
        { &gDvm.classJavaLangAnnotationAnnotationArrayArray,
          "[[Ljava/lang/annotation/Annotation;" },
        { &gDvm.classJavaLangReflectAccessibleObject,   "Ljava/lang/reflect/AccessibleObject;" },
        { &gDvm.classJavaLangReflectConstructor,        "Ljava/lang/reflect/Constructor;" },
        { &gDvm.classJavaLangReflectConstructorArray,   "[Ljava/lang/reflect/Constructor;" },
        { &gDvm.classJavaLangReflectField,              "Ljava/lang/reflect/Field;" },
        { &gDvm.classJavaLangReflectFieldArray,         "[Ljava/lang/reflect/Field;" },
        { &gDvm.classJavaLangReflectMethod,             "Ljava/lang/reflect/Method;" },
        { &gDvm.classJavaLangReflectMethodArray,        "[Ljava/lang/reflect/Method;"},
        { &gDvm.classJavaLangReflectProxy,              "Ljava/lang/reflect/Proxy;" },
        { &gDvm.classJavaNioReadWriteDirectByteBuffer,  "Ljava/nio/ReadWriteDirectByteBuffer;" },
        { &gDvm.classOrgApacheHarmonyLangAnnotationAnnotationFactory,
          "Lorg/apache/harmony/lang/annotation/AnnotationFactory;" },
        { &gDvm.classOrgApacheHarmonyLangAnnotationAnnotationMember,
          "Lorg/apache/harmony/lang/annotation/AnnotationMember;" },
        { &gDvm.classOrgApacheHarmonyLangAnnotationAnnotationMemberArray,
          "[Lorg/apache/harmony/lang/annotation/AnnotationMember;" },

        { NULL, NULL }
    };

    bool ok = true;
    int i;

    for (i = 0; classes[i].ref != NULL; i++) {
        ok &= initClassReference(classes[i].ref, classes[i].name);
    }

    return ok;
}

static bool initFieldOffset(ClassObject* clazz, int *pOffset,
        const char* name, const char* type) {
    int offset = dvmFindFieldOffset(clazz, name, type);
    if (offset < 0) {
        LOGE("Could not find essential field %s.%s of type %s\n", clazz->descriptor, name, type);
        return false;
    }

    *pOffset = offset;
    return true;
}

static bool initFieldOffsets(void) {
    struct FieldInfo {
        int* offset;
        const char* name;
        const char* type;
    };

    static struct FieldInfo infoString[] = {
        { &gDvm.offJavaLangString_value,    "value",    "[C" },
        { &gDvm.offJavaLangString_count,    "count",    "I" },
        { &gDvm.offJavaLangString_offset,   "offset",   "I" },
        { &gDvm.offJavaLangString_hashCode, "hashCode", "I" },
        { NULL, NULL, NULL }
    };

    static struct FieldInfo infoThread[] = {
        { &gDvm.offJavaLangThread_vmThread, "vmThread", "Ljava/lang/VMThread;" },
        { &gDvm.offJavaLangThread_group,    "group",    "Ljava/lang/ThreadGroup;" },
        { &gDvm.offJavaLangThread_daemon,   "daemon",   "Z" },
        { &gDvm.offJavaLangThread_name,     "name",     "Ljava/lang/String;" },
        { &gDvm.offJavaLangThread_priority, "priority", "I" },
        { NULL, NULL, NULL }
    };

    static struct FieldInfo infoThrowable[] = {
        { &gDvm.offJavaLangThrowable_stackState, "stackState", "Ljava/lang/Object;" },
        { &gDvm.offJavaLangThrowable_cause,      "cause",      "Ljava/lang/Throwable;" },
        { NULL, NULL, NULL }
    };

    static struct FieldInfo infoVMThread[] = {
        { &gDvm.offJavaLangVMThread_thread, "thread", "Ljava/lang/Thread;" },
        { &gDvm.offJavaLangVMThread_vmData, "vmData", "I" },
        { NULL, NULL, NULL }
    };

    static struct FieldInfo infoAccessibleObject[] = {
        { &gDvm.offJavaLangReflectAccessibleObject_flag, "flag", "Z" },
        { NULL, NULL, NULL }
    };

    static struct FieldInfo infoConstructor[] = {
        { &gDvm.offJavaLangReflectConstructor_slot,      "slot",           "I" },
        { &gDvm.offJavaLangReflectConstructor_declClass, "declaringClass", "Ljava/lang/Class;" },
        { NULL, NULL, NULL }
    };

    static struct FieldInfo infoField[] = {
        { &gDvm.offJavaLangReflectField_slot,      "slot",           "I" },
        { &gDvm.offJavaLangReflectField_declClass, "declaringClass", "Ljava/lang/Class;" },
        { NULL, NULL, NULL }
    };

    static struct FieldInfo infoMethod[] = {
        { &gDvm.offJavaLangReflectMethod_slot,      "slot",           "I" },
        { &gDvm.offJavaLangReflectMethod_declClass, "declaringClass", "Ljava/lang/Class;" },
        { NULL, NULL, NULL }
    };

    static struct FieldInfo infoProxy[] = {
        { &gDvm.offJavaLangReflectProxy_h, "h", "Ljava/lang/reflect/InvocationHandler;" },
        { NULL, NULL, NULL }
    };

    static struct FieldInfo infoBuffer[] = {
        { &gDvm.offJavaNioBuffer_capacity,               "capacity",               "I" },
        { &gDvm.offJavaNioBuffer_effectiveDirectAddress, "effectiveDirectAddress", "I" },
        { NULL, NULL, NULL }
    };

    static struct { const char* name; const struct FieldInfo* fields; } classes[] = {
        { "Ljava/lang/String;",                   infoString },
        { "Ljava/lang/Thread;",                   infoThread },
        { "Ljava/lang/Throwable;",                infoThrowable },
        { "Ljava/lang/VMThread;",                 infoVMThread },
        { "Ljava/lang/reflect/AccessibleObject;", infoAccessibleObject },
        { "Ljava/lang/reflect/Constructor;",      infoConstructor },
        { "Ljava/lang/reflect/Field;",            infoField },
        { "Ljava/lang/reflect/Method;",           infoMethod },
        { "Ljava/lang/reflect/Proxy;",            infoProxy },
        { "Ljava/nio/Buffer;",                    infoBuffer },
        { NULL, NULL }
    };

    bool ok = true;
    int i;

    for (i = 0; classes[i].name != NULL; i++) {
        const char* className = classes[i].name;
        ClassObject* clazz = dvmFindSystemClassNoInit(className);
        const struct FieldInfo* fields = classes[i].fields;

        if (clazz == NULL) {
            LOGE("Could not find essential class %s for field lookup\n", className);
        }

        int j;
        for (j = 0; fields[j].offset != NULL; j++) {
            ok &= initFieldOffset(clazz, fields[j].offset, fields[j].name, fields[j].type);
        }
    }

    return ok;
}

static bool initConstructorReference(Method** pMethod, const char* name, const char* descriptor) {
    ClassObject* clazz = dvmFindSystemClassNoInit(name);

    if (clazz == NULL) {
        LOGE("Could not find essential class %s for constructor lookup\n", name);
    }

    /*
     * Constructors are direct methods and don't have vtable offsets, which
     * is why we resolve constructors to a Method*.
     */

    Method* method = dvmFindDirectMethodByDescriptor(clazz, "<init>", descriptor);

    if (method == NULL) {
        LOGE("Could not find essential constructor for class %s with descriptor %s\n",
                clazz->descriptor, descriptor);
        return false;
    }

    *pMethod = method;
    return true;
}

static bool initConstructorReferences(void) {
    static struct { Method** method; const char* name; const char* descriptor; } constructors[] = {
        { &gDvm.methJavaLangStackTraceElement_init, "Ljava/lang/StackTraceElement;",
          "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;I)V" },
        { &gDvm.methJavaLangReflectConstructor_init, "Ljava/lang/reflect/Constructor;",
          "(Ljava/lang/Class;[Ljava/lang/Class;[Ljava/lang/Class;I)V" },
        { &gDvm.methJavaLangReflectField_init, "Ljava/lang/reflect/Field;",
          "(Ljava/lang/Class;Ljava/lang/Class;Ljava/lang/String;I)V" },
        { &gDvm.methJavaLangReflectMethod_init, "Ljava/lang/reflect/Method;",
          "(Ljava/lang/Class;[Ljava/lang/Class;[Ljava/lang/Class;Ljava/lang/Class;"
          "Ljava/lang/String;I)V" },
        { &gDvm.methJavaNioReadWriteDirectByteBuffer_init, "Ljava/nio/ReadWriteDirectByteBuffer;",
          "(II)V" },
        { &gDvm.methOrgApacheHarmonyLangAnnotationAnnotationMember_init,
          "Lorg/apache/harmony/lang/annotation/AnnotationMember;",
          "(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Class;Ljava/lang/reflect/Method;)V" },

        { NULL, NULL, NULL }
    };

    bool ok = true;
    int i;

    for (i = 0; constructors[i].method != NULL; i++) {
        ok &= initConstructorReference(constructors[i].method, constructors[i].name,
                constructors[i].descriptor);
    }

    return ok;
}

static bool find2(void) {
    ClassObject* clClass = dvmFindSystemClassNoInit("Ljava/lang/ClassLoader;");
    Method* meth = dvmFindVirtualMethodByDescriptor(clClass, "loadClass",
            "(Ljava/lang/String;)Ljava/lang/Class;");
    if (meth == NULL) {
        LOGE("Unable to find loadClass() in java.lang.ClassLoader\n");
        return false;
    }
    gDvm.voffJavaLangClassLoader_loadClass = meth->methodIndex;

    return true;
}

static bool find3(void) {
    assert(gDvm.classJavaLangThread != NULL);
    assert(gDvm.classJavaLangThreadGroup != NULL);
    assert(gDvm.classJavaLangVMThread != NULL);

    /*
     * Cache the vtable offset for "run()".
     *
     * We don't want to keep the Method* because then we won't find see
     * methods defined in subclasses.
     */
    Method* meth;
    meth = dvmFindVirtualMethodByDescriptor(gDvm.classJavaLangThread, "run", "()V");
    if (meth == NULL) {
        LOGE("Unable to find run() in java.lang.Thread\n");
        return false;
    }
    gDvm.voffJavaLangThread_run = meth->methodIndex;

    /*
     * Cache vtable offsets for ThreadGroup methods.
     */
    meth = dvmFindVirtualMethodByDescriptor(gDvm.classJavaLangThreadGroup,
        "removeThread", "(Ljava/lang/Thread;)V");
    if (meth == NULL) {
        LOGE("Unable to find removeThread(Thread) in java.lang.ThreadGroup\n");
        return false;
    }
    gDvm.voffJavaLangThreadGroup_removeThread = meth->methodIndex;

    return true;
}

static bool find6()
{
    /*
     * Standard methods we must provide in our proxy.
     */
    Method* methE;
    Method* methH;
    Method* methT;
    Method* methF;
    methE = dvmFindVirtualMethodByDescriptor(gDvm.classJavaLangObject,
                "equals", "(Ljava/lang/Object;)Z");
    methH = dvmFindVirtualMethodByDescriptor(gDvm.classJavaLangObject,
                "hashCode", "()I");
    methT = dvmFindVirtualMethodByDescriptor(gDvm.classJavaLangObject,
                "toString", "()Ljava/lang/String;");
    methF = dvmFindVirtualMethodByDescriptor(gDvm.classJavaLangObject,
                "finalize", "()V");
    if (methE == NULL || methH == NULL || methT == NULL || methF == NULL) {
        LOGE("Could not find equals/hashCode/toString/finalize in Object\n");
        return false;
    }
    gDvm.voffJavaLangObject_equals = methE->methodIndex;
    gDvm.voffJavaLangObject_hashCode = methH->methodIndex;
    gDvm.voffJavaLangObject_toString = methT->methodIndex;
    gDvm.voffJavaLangObject_finalize = methF->methodIndex;

    /*
     * The prototype signature needs to be cloned from a method in a
     * "real" DEX file.  We declared this otherwise unused method just
     * for this purpose.
     */
    Method* meth;

    meth = dvmFindDirectMethodByDescriptor(gDvm.classJavaLangReflectProxy, "constructorPrototype",
                "(Ljava/lang/reflect/InvocationHandler;)V");
    if (meth == NULL) {
        LOGE("Could not find java.lang.Proxy.constructorPrototype()\n");
        return false;
    }
    gDvm.methJavaLangReflectProxy_constructorPrototype = meth;

    return true;
}

/*
 * Perform Annotation setup.
 */
static bool find7(void)
{
    Method* meth;

    meth = dvmFindDirectMethodByDescriptor(gDvm.classOrgApacheHarmonyLangAnnotationAnnotationFactory,
            "createAnnotation",
            "(Ljava/lang/Class;[Lorg/apache/harmony/lang/annotation/AnnotationMember;)Ljava/lang/annotation/Annotation;");
    if (meth == NULL) {
        LOGE("Unable to find createAnnotation() in android AnnotationFactory\n");
        return false;
    }
    gDvm.methOrgApacheHarmonyLangAnnotationAnnotationFactory_createAnnotation = meth;


    return true;
}

static bool find8(void) {
    ClassObject* clazz =
        dvmFindClassNoInit("Ldalvik/system/VMDebug;", NULL);
    assert(clazz != NULL);
    gDvm.methodTraceGcMethod =
        dvmFindDirectMethodByDescriptor(clazz, "startGC", "()V");
    gDvm.methodTraceClassPrepMethod =
        dvmFindDirectMethodByDescriptor(clazz, "startClassPrep", "()V");
    if (gDvm.methodTraceGcMethod == NULL ||
        gDvm.methodTraceClassPrepMethod == NULL)
    {
        LOGE("Unable to find startGC or startClassPrep\n");
        return false;
    }

    return true;
}

static bool find9(void) {
    bool badValue = false;
    if (gDvm.offJavaLangString_value != STRING_FIELDOFF_VALUE) {
        LOGE("InlineNative: String.value offset = %d, expected %d\n",
            gDvm.offJavaLangString_value, STRING_FIELDOFF_VALUE);
        badValue = true;
    }
    if (gDvm.offJavaLangString_count != STRING_FIELDOFF_COUNT) {
        LOGE("InlineNative: String.count offset = %d, expected %d\n",
            gDvm.offJavaLangString_count, STRING_FIELDOFF_COUNT);
        badValue = true;
    }
    if (gDvm.offJavaLangString_offset != STRING_FIELDOFF_OFFSET) {
        LOGE("InlineNative: String.offset offset = %d, expected %d\n",
            gDvm.offJavaLangString_offset, STRING_FIELDOFF_OFFSET);
        badValue = true;
    }
    if (gDvm.offJavaLangString_hashCode != STRING_FIELDOFF_HASHCODE) {
        LOGE("InlineNative: String.hashCode offset = %d, expected %d\n",
            gDvm.offJavaLangString_hashCode, STRING_FIELDOFF_HASHCODE);
        badValue = true;
    }
    if (badValue)
        return false;

    return true;
}

/* (documented in header) */
bool dvmFindRequiredClassesAndMembers(void) {
    /*
     * Note: Under normal VM use, this is called by dvmStartup()
     * in Init.c. For dex optimization, this is called as well, but in
     * that case, the call is made from DexPrepare.c.
     */

    bool ok = true;

    ok &= initClassReferences();
    ok &= initFieldOffsets();
    ok &= initConstructorReferences();
    ok &= find2();
    ok &= find3();
    ok &= find6();
    ok &= find7();
    ok &= find8();
    ok &= find9();

    return ok;
}

/* (documented in header) */
bool dvmFindReferenceMembers(ClassObject* classReference) {
    if (gDvm.methJavaLangRefReference_enqueueInternal != NULL) {
        LOGE("Attempt to set up class Reference more than once\n");
        return false;
    }

    if (strcmp(classReference->descriptor, "Ljava/lang/ref/Reference;") != 0) {
        LOGE("Attempt to set up the wrong class as Reference\n");
        return false;
    }

    bool ok = true;

    gDvm.offJavaLangRefReference_referent =
        dvmFindFieldOffset(classReference, "referent", "Ljava/lang/Object;");
    ok &= (gDvm.offJavaLangRefReference_referent >= 0);

    gDvm.offJavaLangRefReference_queue =
        dvmFindFieldOffset(classReference, "queue", "Ljava/lang/ref/ReferenceQueue;");
    ok &= (gDvm.offJavaLangRefReference_queue >= 0);

    gDvm.offJavaLangRefReference_queueNext =
        dvmFindFieldOffset(classReference, "queueNext", "Ljava/lang/ref/Reference;");
    ok &= (gDvm.offJavaLangRefReference_queueNext >= 0);

    gDvm.offJavaLangRefReference_pendingNext =
        dvmFindFieldOffset(classReference, "pendingNext", "Ljava/lang/ref/Reference;");
    ok &= (gDvm.offJavaLangRefReference_pendingNext >= 0);

    /* enqueueInternal() is private and thus a direct method. */
    Method *meth = dvmFindDirectMethodByDescriptor(classReference, "enqueueInternal", "()Z");
    ok &= (meth != NULL);
    gDvm.methJavaLangRefReference_enqueueInternal = meth;

    return ok;
}
