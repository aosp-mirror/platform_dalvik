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
static bool initRef(ClassObject** pClass, const char* name) {
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

static bool find1(void) {
    /*
     * Note: Under normal VM use, this is called by dvmStartup()
     * in Init.c. For dex optimization, this is called as well, but in
     * that case, the call is made from DexPrepare.c.
     */

    bool ok = true;

    /* The corest of the core classes */

    ok &= initRef(&gDvm.classJavaLangClass, "Ljava/lang/Class;");
    ok &= initRef(&gDvm.classJavaLangObject, "Ljava/lang/Object;");
    ok &= initRef(&gDvm.exThrowable, "Ljava/lang/Throwable;");

    ok &= initRef(&gDvm.classJavaLangString, "Ljava/lang/String;");
    ok &= initRef(&gDvm.classJavaLangThread, "Ljava/lang/Thread;");
    ok &= initRef(&gDvm.classJavaLangThreadGroup, "Ljava/lang/ThreadGroup;");
    ok &= initRef(&gDvm.classJavaLangVMThread, "Ljava/lang/VMThread;");

    /* Exception classes and related support classes */

    ok &= initRef(&gDvm.exAbstractMethodError,
            "Ljava/lang/AbstractMethodError;");
    ok &= initRef(&gDvm.exArithmeticException,
            "Ljava/lang/ArithmeticException;");
    ok &= initRef(&gDvm.exArrayIndexOutOfBoundsException,
            "Ljava/lang/ArrayIndexOutOfBoundsException;");
    ok &= initRef(&gDvm.exArrayStoreException,
            "Ljava/lang/ArrayStoreException;");
    ok &= initRef(&gDvm.exClassCastException,
            "Ljava/lang/ClassCastException;");
    ok &= initRef(&gDvm.exClassCircularityError,
            "Ljava/lang/ClassCircularityError;");
    ok &= initRef(&gDvm.exClassNotFoundException,
            "Ljava/lang/ClassNotFoundException;");
    ok &= initRef(&gDvm.exClassFormatError, "Ljava/lang/ClassFormatError;");
    ok &= initRef(&gDvm.exError, "Ljava/lang/Error;");
    ok &= initRef(&gDvm.exExceptionInInitializerError,
            "Ljava/lang/ExceptionInInitializerError;");
    ok &= initRef(&gDvm.exFileNotFoundException,
            "Ljava/io/FileNotFoundException;");
    ok &= initRef(&gDvm.exIOException, "Ljava/io/IOException;");
    ok &= initRef(&gDvm.exIllegalAccessError,
            "Ljava/lang/IllegalAccessError;");
    ok &= initRef(&gDvm.exIllegalAccessException,
            "Ljava/lang/IllegalAccessException;");
    ok &= initRef(&gDvm.exIllegalArgumentException,
            "Ljava/lang/IllegalArgumentException;");
    ok &= initRef(&gDvm.exIllegalMonitorStateException,
            "Ljava/lang/IllegalMonitorStateException;");
    ok &= initRef(&gDvm.exIllegalStateException,
            "Ljava/lang/IllegalStateException;");
    ok &= initRef(&gDvm.exIllegalThreadStateException,
            "Ljava/lang/IllegalThreadStateException;");
    ok &= initRef(&gDvm.exIncompatibleClassChangeError,
            "Ljava/lang/IncompatibleClassChangeError;");
    ok &= initRef(&gDvm.exInstantiationError,
            "Ljava/lang/InstantiationError;");
    ok &= initRef(&gDvm.exInstantiationException,
            "Ljava/lang/InstantiationException;");
    ok &= initRef(&gDvm.exInternalError,
            "Ljava/lang/InternalError;");
    ok &= initRef(&gDvm.exInterruptedException,
            "Ljava/lang/InterruptedException;");
    ok &= initRef(&gDvm.exLinkageError,
            "Ljava/lang/LinkageError;");
    ok &= initRef(&gDvm.exNegativeArraySizeException,
            "Ljava/lang/NegativeArraySizeException;");
    ok &= initRef(&gDvm.exNoClassDefFoundError,
            "Ljava/lang/NoClassDefFoundError;");
    ok &= initRef(&gDvm.exNoSuchFieldError,
            "Ljava/lang/NoSuchFieldError;");
    ok &= initRef(&gDvm.exNoSuchFieldException,
            "Ljava/lang/NoSuchFieldException;");
    ok &= initRef(&gDvm.exNoSuchMethodError,
            "Ljava/lang/NoSuchMethodError;");
    ok &= initRef(&gDvm.exNullPointerException,
            "Ljava/lang/NullPointerException;");
    ok &= initRef(&gDvm.exOutOfMemoryError,
            "Ljava/lang/OutOfMemoryError;");
    ok &= initRef(&gDvm.exRuntimeException, "Ljava/lang/RuntimeException;");
    ok &= initRef(&gDvm.exStackOverflowError,
            "Ljava/lang/StackOverflowError;");
    ok &= initRef(&gDvm.exStaleDexCacheError,
            "Ldalvik/system/StaleDexCacheError;");
    ok &= initRef(&gDvm.exStringIndexOutOfBoundsException,
            "Ljava/lang/StringIndexOutOfBoundsException;");
    ok &= initRef(&gDvm.exTypeNotPresentException,
            "Ljava/lang/TypeNotPresentException;");
    ok &= initRef(&gDvm.exUnsatisfiedLinkError,
            "Ljava/lang/UnsatisfiedLinkError;");
    ok &= initRef(&gDvm.exUnsupportedOperationException,
            "Ljava/lang/UnsupportedOperationException;");
    ok &= initRef(&gDvm.exVerifyError,
            "Ljava/lang/VerifyError;");
    ok &= initRef(&gDvm.exVirtualMachineError,
            "Ljava/lang/VirtualMachineError;");

    ok &= initRef(&gDvm.classJavaLangStackTraceElement,
            "Ljava/lang/StackTraceElement;");
    ok &= initRef(&gDvm.classJavaLangStackTraceElementArray,
            "[Ljava/lang/StackTraceElement;");

    if (!ok) {
        return false;
    }

    /*
     * Find the StackTraceElement constructor. Note that, unlike other
     * saved method lookups, we're using a Method* instead of a vtable
     * offset. This is because constructors don't have vtable offsets.
     * (Also, since we're creating the object in question, it's
     * impossible for anyone to sub-class it.)
     */
    Method* meth;
    meth = dvmFindDirectMethodByDescriptor(gDvm.classJavaLangStackTraceElement,
        "<init>",
        "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;I)V");
    if (meth == NULL) {
        LOGE("Unable to find constructor for StackTraceElement\n");
        return false;
    }
    gDvm.methJavaLangStackTraceElement_init = meth;

    /* grab an offset for the field Throwable.stackState */
    gDvm.offJavaLangThrowable_stackState =
        dvmFindFieldOffset(gDvm.exThrowable,
            "stackState", "Ljava/lang/Object;");
    if (gDvm.offJavaLangThrowable_stackState < 0) {
        LOGE("Unable to find Throwable.stackState\n");
        return false;
    }

    /* and one for the field Throwable.cause, just 'cause */
    gDvm.offJavaLangThrowable_cause =
        dvmFindFieldOffset(gDvm.exThrowable,
            "cause", "Ljava/lang/Throwable;");
    if (gDvm.offJavaLangThrowable_cause < 0) {
        LOGE("Unable to find Throwable.cause\n");
        return false;
    }

    return true;
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
     * Cache field offsets.  This makes things a little faster, at the
     * expense of hard-coding non-public field names into the VM.
     */
    gDvm.offJavaLangThread_vmThread =
        dvmFindFieldOffset(gDvm.classJavaLangThread,
            "vmThread", "Ljava/lang/VMThread;");
    gDvm.offJavaLangThread_group =
        dvmFindFieldOffset(gDvm.classJavaLangThread,
            "group", "Ljava/lang/ThreadGroup;");
    gDvm.offJavaLangThread_daemon =
        dvmFindFieldOffset(gDvm.classJavaLangThread, "daemon", "Z");
    gDvm.offJavaLangThread_name =
        dvmFindFieldOffset(gDvm.classJavaLangThread,
            "name", "Ljava/lang/String;");
    gDvm.offJavaLangThread_priority =
        dvmFindFieldOffset(gDvm.classJavaLangThread, "priority", "I");

    if (gDvm.offJavaLangThread_vmThread < 0 ||
        gDvm.offJavaLangThread_group < 0 ||
        gDvm.offJavaLangThread_daemon < 0 ||
        gDvm.offJavaLangThread_name < 0 ||
        gDvm.offJavaLangThread_priority < 0)
    {
        LOGE("Unable to find all fields in java.lang.Thread\n");
        return false;
    }

    gDvm.offJavaLangVMThread_thread =
        dvmFindFieldOffset(gDvm.classJavaLangVMThread,
            "thread", "Ljava/lang/Thread;");
    gDvm.offJavaLangVMThread_vmData =
        dvmFindFieldOffset(gDvm.classJavaLangVMThread, "vmData", "I");
    if (gDvm.offJavaLangVMThread_thread < 0 ||
        gDvm.offJavaLangVMThread_vmData < 0)
    {
        LOGE("Unable to find all fields in java.lang.VMThread\n");
        return false;
    }

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

static bool find4(void) {
    Method* meth;

    /*
     * Look up and cache pointers to some direct buffer classes, fields,
     * and methods.
     */
    ClassObject* readWriteBufferClass =
        dvmFindSystemClassNoInit("Ljava/nio/ReadWriteDirectByteBuffer;");
    ClassObject* bufferClass =
        dvmFindSystemClassNoInit("Ljava/nio/Buffer;");

    if (readWriteBufferClass == NULL || bufferClass == NULL) {
        LOGE("Unable to find internal direct buffer classes\n");
        return false;
    }
    gDvm.classJavaNioReadWriteDirectByteBuffer = readWriteBufferClass;

    meth = dvmFindDirectMethodByDescriptor(readWriteBufferClass,
                "<init>",
                "(II)V");
    if (meth == NULL) {
        LOGE("Unable to find ReadWriteDirectByteBuffer.<init>\n");
        return false;
    }
    gDvm.methJavaNioReadWriteDirectByteBuffer_init = meth;

    gDvm.offJavaNioBuffer_capacity =
        dvmFindFieldOffset(bufferClass, "capacity", "I");
    if (gDvm.offJavaNioBuffer_capacity < 0) {
        LOGE("Unable to find Buffer.capacity\n");
        return false;
    }

    gDvm.offJavaNioBuffer_effectiveDirectAddress =
        dvmFindFieldOffset(bufferClass, "effectiveDirectAddress", "I");
    if (gDvm.offJavaNioBuffer_effectiveDirectAddress < 0) {
        LOGE("Unable to find Buffer.effectiveDirectAddress\n");
        return false;
    }

    return true;
}

static bool find5(void)
{
    gDvm.classJavaLangReflectAccessibleObject =
        dvmFindSystemClassNoInit("Ljava/lang/reflect/AccessibleObject;");
    gDvm.classJavaLangReflectConstructor =
        dvmFindSystemClassNoInit("Ljava/lang/reflect/Constructor;");
    gDvm.classJavaLangReflectConstructorArray =
        dvmFindArrayClass("[Ljava/lang/reflect/Constructor;", NULL);
    gDvm.classJavaLangReflectField =
        dvmFindSystemClassNoInit("Ljava/lang/reflect/Field;");
    gDvm.classJavaLangReflectFieldArray =
        dvmFindArrayClass("[Ljava/lang/reflect/Field;", NULL);
    gDvm.classJavaLangReflectMethod =
        dvmFindSystemClassNoInit("Ljava/lang/reflect/Method;");
    gDvm.classJavaLangReflectMethodArray =
        dvmFindArrayClass("[Ljava/lang/reflect/Method;", NULL);
    gDvm.classJavaLangReflectProxy =
        dvmFindSystemClassNoInit("Ljava/lang/reflect/Proxy;");
    if (gDvm.classJavaLangReflectAccessibleObject == NULL ||
        gDvm.classJavaLangReflectConstructor == NULL ||
        gDvm.classJavaLangReflectConstructorArray == NULL ||
        gDvm.classJavaLangReflectField == NULL ||
        gDvm.classJavaLangReflectFieldArray == NULL ||
        gDvm.classJavaLangReflectMethod == NULL ||
        gDvm.classJavaLangReflectMethodArray == NULL ||
        gDvm.classJavaLangReflectProxy == NULL)
    {
        LOGE("Could not find one or more reflection classes\n");
        return false;
    }

    gDvm.methJavaLangReflectConstructor_init =
        dvmFindDirectMethodByDescriptor(gDvm.classJavaLangReflectConstructor, "<init>",
        "(Ljava/lang/Class;[Ljava/lang/Class;[Ljava/lang/Class;I)V");
    gDvm.methJavaLangReflectField_init =
        dvmFindDirectMethodByDescriptor(gDvm.classJavaLangReflectField, "<init>",
        "(Ljava/lang/Class;Ljava/lang/Class;Ljava/lang/String;I)V");
    gDvm.methJavaLangReflectMethod_init =
        dvmFindDirectMethodByDescriptor(gDvm.classJavaLangReflectMethod, "<init>",
        "(Ljava/lang/Class;[Ljava/lang/Class;[Ljava/lang/Class;Ljava/lang/Class;Ljava/lang/String;I)V");
    if (gDvm.methJavaLangReflectConstructor_init == NULL ||
        gDvm.methJavaLangReflectField_init == NULL ||
        gDvm.methJavaLangReflectMethod_init == NULL)
    {
        LOGE("Could not find reflection constructors\n");
        return false;
    }

    gDvm.classJavaLangClassArray =
        dvmFindArrayClass("[Ljava/lang/Class;", NULL);
    gDvm.classJavaLangObjectArray =
        dvmFindArrayClass("[Ljava/lang/Object;", NULL);
    if (gDvm.classJavaLangClassArray == NULL ||
        gDvm.classJavaLangObjectArray == NULL)
    {
        LOGE("Could not find class-array or object-array class\n");
        return false;
    }

    gDvm.offJavaLangReflectAccessibleObject_flag =
        dvmFindFieldOffset(gDvm.classJavaLangReflectAccessibleObject, "flag",
            "Z");

    gDvm.offJavaLangReflectConstructor_slot =
        dvmFindFieldOffset(gDvm.classJavaLangReflectConstructor, "slot", "I");
    gDvm.offJavaLangReflectConstructor_declClass =
        dvmFindFieldOffset(gDvm.classJavaLangReflectConstructor,
            "declaringClass", "Ljava/lang/Class;");

    gDvm.offJavaLangReflectField_slot =
        dvmFindFieldOffset(gDvm.classJavaLangReflectField, "slot", "I");
    gDvm.offJavaLangReflectField_declClass =
        dvmFindFieldOffset(gDvm.classJavaLangReflectField,
            "declaringClass", "Ljava/lang/Class;");

    gDvm.offJavaLangReflectMethod_slot =
        dvmFindFieldOffset(gDvm.classJavaLangReflectMethod, "slot", "I");
    gDvm.offJavaLangReflectMethod_declClass =
        dvmFindFieldOffset(gDvm.classJavaLangReflectMethod,
            "declaringClass", "Ljava/lang/Class;");

    if (gDvm.offJavaLangReflectAccessibleObject_flag < 0 ||
        gDvm.offJavaLangReflectConstructor_slot < 0 ||
        gDvm.offJavaLangReflectConstructor_declClass < 0 ||
        gDvm.offJavaLangReflectField_slot < 0 ||
        gDvm.offJavaLangReflectField_declClass < 0 ||
        gDvm.offJavaLangReflectMethod_slot < 0 ||
        gDvm.offJavaLangReflectMethod_declClass < 0)
    {
        LOGE("Could not find reflection fields\n");
        return false;
    }

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
    ClassObject* proxyClass;
    Method* meth;
    proxyClass = dvmFindSystemClassNoInit("Ljava/lang/reflect/Proxy;");
    if (proxyClass == NULL) {
        LOGE("No java.lang.reflect.Proxy\n");
        return false;
    }
    meth = dvmFindDirectMethodByDescriptor(proxyClass, "constructorPrototype",
                "(Ljava/lang/reflect/InvocationHandler;)V");
    if (meth == NULL) {
        LOGE("Could not find java.lang.Proxy.constructorPrototype()\n");
        return false;
    }
    gDvm.methJavaLangReflectProxy_constructorPrototype = meth;

    /*
     * Get the offset of the "h" field in Proxy.
     */
    gDvm.offJavaLangReflectProxy_h = dvmFindFieldOffset(proxyClass, "h",
        "Ljava/lang/reflect/InvocationHandler;");
    if (gDvm.offJavaLangReflectProxy_h < 0) {
        LOGE("Unable to find 'h' field in java.lang.Proxy\n");
        return false;
    }

    return true;
}

/*
 * Perform Annotation setup.
 */
static bool find7(void)
{
    Method* meth;

    /*
     * Find some standard Annotation classes.
     */
    gDvm.classJavaLangAnnotationAnnotationArray =
        dvmFindArrayClass("[Ljava/lang/annotation/Annotation;", NULL);
    gDvm.classJavaLangAnnotationAnnotationArrayArray =
        dvmFindArrayClass("[[Ljava/lang/annotation/Annotation;", NULL);
    if (gDvm.classJavaLangAnnotationAnnotationArray == NULL ||
        gDvm.classJavaLangAnnotationAnnotationArrayArray == NULL)
    {
        LOGE("Could not find Annotation-array classes\n");
        return false;
    }

    /*
     * VM-specific annotation classes.
     */
    gDvm.classOrgApacheHarmonyLangAnnotationAnnotationFactory =
        dvmFindSystemClassNoInit("Lorg/apache/harmony/lang/annotation/AnnotationFactory;");
    gDvm.classOrgApacheHarmonyLangAnnotationAnnotationMember =
        dvmFindSystemClassNoInit("Lorg/apache/harmony/lang/annotation/AnnotationMember;");
    gDvm.classOrgApacheHarmonyLangAnnotationAnnotationMemberArray =
        dvmFindArrayClass("[Lorg/apache/harmony/lang/annotation/AnnotationMember;", NULL);
    if (gDvm.classOrgApacheHarmonyLangAnnotationAnnotationFactory == NULL ||
        gDvm.classOrgApacheHarmonyLangAnnotationAnnotationMember == NULL ||
        gDvm.classOrgApacheHarmonyLangAnnotationAnnotationMemberArray == NULL)
    {
        LOGE("Could not find android.lang annotation classes\n");
        return false;
    }

    meth = dvmFindDirectMethodByDescriptor(gDvm.classOrgApacheHarmonyLangAnnotationAnnotationFactory,
            "createAnnotation",
            "(Ljava/lang/Class;[Lorg/apache/harmony/lang/annotation/AnnotationMember;)Ljava/lang/annotation/Annotation;");
    if (meth == NULL) {
        LOGE("Unable to find createAnnotation() in android AnnotationFactory\n");
        return false;
    }
    gDvm.methOrgApacheHarmonyLangAnnotationAnnotationFactory_createAnnotation = meth;

    meth = dvmFindDirectMethodByDescriptor(gDvm.classOrgApacheHarmonyLangAnnotationAnnotationMember,
            "<init>",
            "(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Class;Ljava/lang/reflect/Method;)V");
    if (meth == NULL) {
        LOGE("Unable to find 4-arg constructor in android AnnotationMember\n");
        return false;
    }

    gDvm.methOrgApacheHarmonyLangAnnotationAnnotationMember_init = meth;

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

/* (documented in header) */
bool dvmFindRequiredClassesAndMembers(void) {
    bool ok = true;

    ok &= find1();
    ok &= find2();
    ok &= find3();
    ok &= find4();
    ok &= find5();
    ok &= find6();
    ok &= find7();
    ok &= find8();

    return ok;
}
