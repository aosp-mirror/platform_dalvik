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
 * Internal-native initialization and some common utility functions.
 */
#include "Dalvik.h"
#include "native/InternalNativePriv.h"

/*
 * Set of classes for which we provide methods.
 *
 * The last field, classNameHash, is filled in at startup.
 */
static DalvikNativeClass gDvmNativeMethodSet[] = {
    { "Ljava/lang/Object;",               dvm_java_lang_Object, 0 },
    { "Ljava/lang/Class;",                dvm_java_lang_Class, 0 },
    { "Ljava/lang/Runtime;",              dvm_java_lang_Runtime, 0 },
    { "Ljava/lang/String;",               dvm_java_lang_String, 0 },
    { "Ljava/lang/System;",               dvm_java_lang_System, 0 },
    { "Ljava/lang/SystemProperties;",     dvm_java_lang_SystemProperties, 0 },
    { "Ljava/lang/Throwable;",            dvm_java_lang_Throwable, 0 },
    { "Ljava/lang/VMClassLoader;",        dvm_java_lang_VMClassLoader, 0 },
    { "Ljava/lang/VMThread;",             dvm_java_lang_VMThread, 0 },
    { "Ljava/lang/reflect/AccessibleObject;",
            dvm_java_lang_reflect_AccessibleObject, 0 },
    { "Ljava/lang/reflect/Array;",        dvm_java_lang_reflect_Array, 0 },
    { "Ljava/lang/reflect/Constructor;",
            dvm_java_lang_reflect_Constructor, 0 },
    { "Ljava/lang/reflect/Field;",        dvm_java_lang_reflect_Field, 0 },
    { "Ljava/lang/reflect/Method;",       dvm_java_lang_reflect_Method, 0 },
    { "Ljava/lang/reflect/Proxy;",        dvm_java_lang_reflect_Proxy, 0 },
    { "Ljava/security/AccessController;",
            dvm_java_security_AccessController, 0 },
    { "Ljava/util/concurrent/atomic/AtomicLong;",
            dvm_java_util_concurrent_atomic_AtomicLong, 0 },
    { "Ldalvik/system/VMDebug;",          dvm_dalvik_system_VMDebug, 0 },
    { "Ldalvik/system/DexFile;",          dvm_dalvik_system_DexFile, 0 },
    { "Ldalvik/system/VMRuntime;",        dvm_dalvik_system_VMRuntime, 0 },
    { "Ldalvik/system/Zygote;",           dvm_dalvik_system_Zygote, 0 },
    { "Ldalvik/system/VMStack;",          dvm_dalvik_system_VMStack, 0 },
    { "Lorg/apache/harmony/dalvik/ddmc/DdmServer;",
            dvm_org_apache_harmony_dalvik_ddmc_DdmServer, 0 },
    { "Lorg/apache/harmony/dalvik/ddmc/DdmVmInternal;",
            dvm_org_apache_harmony_dalvik_ddmc_DdmVmInternal, 0 },
    { "Lorg/apache/harmony/dalvik/NativeTestTarget;",
            dvm_org_apache_harmony_dalvik_NativeTestTarget, 0 },
    { "Lsun/misc/Unsafe;",                dvm_sun_misc_Unsafe, 0 },
    { NULL, NULL, 0 },
};


/*
 * Set up hash values on the class names.
 */
bool dvmInternalNativeStartup(void)
{
    DalvikNativeClass* classPtr = gDvmNativeMethodSet;

    while (classPtr->classDescriptor != NULL) {
        classPtr->classDescriptorHash =
            dvmComputeUtf8Hash(classPtr->classDescriptor);
        classPtr++;
    }

    gDvm.userDexFiles = dvmHashTableCreate(2, dvmFreeDexOrJar);
    if (gDvm.userDexFiles == NULL)
        return false;

    return true;
}

/*
 * Clean up.
 */
void dvmInternalNativeShutdown(void)
{
    dvmHashTableFree(gDvm.userDexFiles);
}

/*
 * Search the internal native set for a match.
 */
DalvikNativeFunc dvmLookupInternalNativeMethod(const Method* method)
{
    const char* classDescriptor = method->clazz->descriptor;
    const DalvikNativeClass* pClass;
    u4 hash;

    hash = dvmComputeUtf8Hash(classDescriptor);
    pClass = gDvmNativeMethodSet;
    while (true) {
        if (pClass->classDescriptor == NULL)
            break;
        if (pClass->classDescriptorHash == hash &&
            strcmp(pClass->classDescriptor, classDescriptor) == 0)
        {
            const DalvikNativeMethod* pMeth = pClass->methodInfo;
            while (true) {
                if (pMeth->name == NULL)
                    break;

                if (dvmCompareNameDescriptorAndMethod(pMeth->name,
                    pMeth->signature, method) == 0)
                {
                    /* match */
                    //LOGV("+++  match on %s.%s %s at %p\n",
                    //    className, methodName, methodSignature, pMeth->fnPtr);
                    return pMeth->fnPtr;
                }

                pMeth++;
            }
        }

        pClass++;
    }

    return NULL;
}


/*
 * Magic "internal native" code stub, inserted into abstract method
 * definitions when a class is first loaded.  This throws the expected
 * exception so we don't have to explicitly check for it in the interpreter.
 */
void dvmAbstractMethodStub(const u4* args, JValue* pResult)
{
    LOGD("--- called into dvmAbstractMethodStub\n");
    dvmThrowException("Ljava/lang/AbstractMethodError;",
        "abstract method not implemented");
}


/*
 * Verify that "obj" is non-null and is an instance of "clazz".
 *
 * Returns "false" and throws an exception if not.
 */
bool dvmVerifyObjectInClass(Object* obj, ClassObject* clazz)
{
    if (obj == NULL) {
        dvmThrowException("Ljava/lang/NullPointerException;", NULL);
        return false;
    }
    if (!dvmInstanceof(obj->clazz, clazz)) {
        dvmThrowException("Ljava/lang/IllegalArgumentException;",
            "object is not an instance of the class");
        return false;
    }

    return true;
}

/*
 * Validate a "binary" class name, e.g. "java.lang.String" or "[I".
 */
static bool validateClassName(const char* name)
{
    int len = strlen(name);
    int i = 0;

    /* check for reasonable array types */
    if (name[0] == '[') {
        while (name[i] == '[')
            i++;

        if (name[i] == 'L') {
            /* array of objects, make sure it ends well */
            if (name[len-1] != ';')
                return false;
        } else if (strchr(PRIM_TYPE_TO_LETTER, name[i]) != NULL) {
            if (i != len-1)
                return false;
        } else {
            return false;
        }
    }

    /* quick check for illegal chars */
    for ( ; i < len; i++) {
        if (name[i] == '/')
            return false;
    }

    return true;
}

/*
 * Find a class by name, initializing it if requested.
 */
ClassObject* dvmFindClassByName(StringObject* nameObj, Object* loader,
    bool doInit)
{
    ClassObject* clazz = NULL;
    char* name = NULL;
    char* descriptor = NULL;

    if (nameObj == NULL) {
        dvmThrowException("Ljava/lang/NullPointerException;", NULL);
        goto bail;
    }
    name = dvmCreateCstrFromString(nameObj);

    /*
     * We need to validate and convert the name (from x.y.z to x/y/z).  This
     * is especially handy for array types, since we want to avoid
     * auto-generating bogus array classes.
     */
    if (!validateClassName(name)) {
        LOGW("dvmFindClassByName rejecting '%s'\n", name);
        dvmThrowException("Ljava/lang/ClassNotFoundException;", name);
        goto bail;
    }

    descriptor = dvmDotToDescriptor(name);
    if (descriptor == NULL) {
        goto bail;
    }

    if (doInit)
        clazz = dvmFindClass(descriptor, loader);
    else
        clazz = dvmFindClassNoInit(descriptor, loader);

    if (clazz == NULL) {
        LOGVV("FAIL: load %s (%d)\n", descriptor, doInit);
        Thread* self = dvmThreadSelf();
        Object* oldExcep = dvmGetException(self);
        dvmAddTrackedAlloc(oldExcep, self);     /* don't let this be GCed */
        dvmClearException(self);
        dvmThrowChainedException("Ljava/lang/ClassNotFoundException;",
            name, oldExcep);
        dvmReleaseTrackedAlloc(oldExcep, self);
    } else {
        LOGVV("GOOD: load %s (%d) --> %p ldr=%p\n",
            descriptor, doInit, clazz, clazz->classLoader);
    }

bail:
    free(name);
    free(descriptor);
    return clazz;
}

/*
 * We insert native method stubs for abstract methods so we don't have to
 * check the access flags at the time of the method call.  This results in
 * "native abstract" methods, which can't exist.  If we see the "abstract"
 * flag set, clear the "native" flag.
 *
 * We also move the DECLARED_SYNCHRONIZED flag into the SYNCHRONIZED
 * position, because the callers of this function are trying to convey
 * the "traditional" meaning of the flags to their callers.
 */
u4 dvmFixMethodFlags(u4 flags)
{
    if ((flags & ACC_ABSTRACT) != 0) {
        flags &= ~ACC_NATIVE;
    }

    flags &= ~ACC_SYNCHRONIZED;

    if ((flags & ACC_DECLARED_SYNCHRONIZED) != 0) {
        flags |= ACC_SYNCHRONIZED;
    }

    return flags & JAVA_FLAGS_MASK;
}


#define NUM_DOPRIV_FUNCS    4

/*
 * Determine if "method" is a "privileged" invocation, i.e. is it one
 * of the variations of AccessController.doPrivileged().
 *
 * Because the security stuff pulls in a pile of stuff that we may not
 * want or need, we don't do the class/method lookups at init time, but
 * instead on first use.
 */
bool dvmIsPrivilegedMethod(const Method* method)
{
    int i;

    assert(method != NULL);

    if (!gDvm.javaSecurityAccessControllerReady) {
        /*
         * Populate on first use.  No concurrency risk since we're just
         * finding pointers to fixed structures.
         */
        static const char* kSignatures[NUM_DOPRIV_FUNCS] = {
            "(Ljava/security/PrivilegedAction;)Ljava/lang/Object;",
            "(Ljava/security/PrivilegedExceptionAction;)Ljava/lang/Object;",
            "(Ljava/security/PrivilegedAction;Ljava/security/AccessControlContext;)Ljava/lang/Object;",
            "(Ljava/security/PrivilegedExceptionAction;Ljava/security/AccessControlContext;)Ljava/lang/Object;",
        };
        ClassObject* clazz;

        clazz = dvmFindClassNoInit("Ljava/security/AccessController;", NULL);
        if (clazz == NULL) {
            LOGW("Couldn't find java/security/AccessController\n");
            return false;
        }

        assert(NELEM(gDvm.methJavaSecurityAccessController_doPrivileged) ==
               NELEM(kSignatures));

        /* verify init */
        for (i = 0; i < NUM_DOPRIV_FUNCS; i++) {
            gDvm.methJavaSecurityAccessController_doPrivileged[i] =
                dvmFindDirectMethodByDescriptor(clazz, "doPrivileged", kSignatures[i]);
            if (gDvm.methJavaSecurityAccessController_doPrivileged[i] == NULL) {
                LOGW("Warning: couldn't find java/security/AccessController"
                    ".doPrivileged %s\n", kSignatures[i]);
                return false;
            }
        }

        /* all good, raise volatile readiness flag */
        android_atomic_release_store(true,
            &gDvm.javaSecurityAccessControllerReady);
    }

    for (i = 0; i < NUM_DOPRIV_FUNCS; i++) {
        if (gDvm.methJavaSecurityAccessController_doPrivileged[i] == method) {
            //LOGI("+++ doPriv match\n");
            return true;
        }
    }
    return false;
}
