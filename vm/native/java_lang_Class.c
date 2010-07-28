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
 * java.lang.Class
 */
#include "Dalvik.h"
#include "native/InternalNativePriv.h"


/*
 * native public boolean desiredAssertionStatus()
 *
 * Determine the class-init-time assertion status of a class.  This is
 * called from <clinit> in javac-generated classes that use the Java
 * programming language "assert" keyword.
 */
static void Dalvik_java_lang_Class_desiredAssertionStatus(const u4* args,
    JValue* pResult)
{
    ClassObject* thisPtr = (ClassObject*) args[0];
    char* className = dvmDescriptorToName(thisPtr->descriptor);
    int i;
    bool enable = false;

    /*
     * Run through the list of arguments specified on the command line.  The
     * last matching argument takes precedence.
     */
    for (i = 0; i < gDvm.assertionCtrlCount; i++) {
        const AssertionControl* pCtrl = &gDvm.assertionCtrl[i];

        if (pCtrl->isPackage) {
            /*
             * Given "dalvik/system/Debug" or "MyStuff", compute the
             * length of the package portion of the class name string.
             *
             * Unlike most package operations, we allow matching on
             * "sub-packages", so "dalvik..." will match "dalvik.Foo"
             * and "dalvik.system.Foo".
             *
             * The pkgOrClass string looks like "dalvik/system/", i.e. it still
             * has the terminating slash, so we can be sure we're comparing
             * against full package component names.
             */
            const char* lastSlash;
            int pkgLen;

            lastSlash = strrchr(className, '/');
            if (lastSlash == NULL) {
                pkgLen = 0;
            } else {
                pkgLen = lastSlash - className +1;
            }

            if (pCtrl->pkgOrClassLen > pkgLen ||
                memcmp(pCtrl->pkgOrClass, className, pCtrl->pkgOrClassLen) != 0)
            {
                LOGV("ASRT: pkg no match: '%s'(%d) vs '%s'\n",
                    className, pkgLen, pCtrl->pkgOrClass);
            } else {
                LOGV("ASRT: pkg match: '%s'(%d) vs '%s' --> %d\n",
                    className, pkgLen, pCtrl->pkgOrClass, pCtrl->enable);
                enable = pCtrl->enable;
            }
        } else {
            /*
             * "pkgOrClass" holds a fully-qualified class name, converted from
             * dot-form to slash-form.  An empty string means all classes.
             */
            if (pCtrl->pkgOrClass == NULL) {
                /* -esa/-dsa; see if class is a "system" class */
                if (strncmp(className, "java/", 5) != 0) {
                    LOGV("ASRT: sys no match: '%s'\n", className);
                } else {
                    LOGV("ASRT: sys match: '%s' --> %d\n",
                        className, pCtrl->enable);
                    enable = pCtrl->enable;
                }
            } else if (*pCtrl->pkgOrClass == '\0') {
                LOGV("ASRT: class all: '%s' --> %d\n",
                    className, pCtrl->enable);
                enable = pCtrl->enable;
            } else {
                if (strcmp(pCtrl->pkgOrClass, className) != 0) {
                    LOGV("ASRT: cls no match: '%s' vs '%s'\n",
                        className, pCtrl->pkgOrClass);
                } else {
                    LOGV("ASRT: cls match: '%s' vs '%s' --> %d\n",
                        className, pCtrl->pkgOrClass, pCtrl->enable);
                    enable = pCtrl->enable;
                }
            }
        }
    }

    free(className);
    RETURN_INT(enable);
}

/*
 * static public Class<?> classForName(String name, boolean initialize,
 *     ClassLoader loader)
 *
 * Return the Class object associated with the class or interface with
 * the specified name.
 *
 * "name" is in "binary name" format, e.g. "dalvik.system.Debug$1".
 */
static void Dalvik_java_lang_Class_classForName(const u4* args, JValue* pResult)
{
    StringObject* nameObj = (StringObject*) args[0];
    bool initialize = (args[1] != 0);
    Object* loader = (Object*) args[2];

    RETURN_PTR(dvmFindClassByName(nameObj, loader, initialize));
}

/*
 * static private ClassLoader getClassLoader(Class clazz)
 *
 * Return the class' defining class loader.
 */
static void Dalvik_java_lang_Class_getClassLoader(const u4* args,
    JValue* pResult)
{
    ClassObject* clazz = (ClassObject*) args[0];

    RETURN_PTR(clazz->classLoader);
}

/*
 * public Class<?> getComponentType()
 *
 * If this is an array type, return the class of the elements; otherwise
 * return NULL.
 */
static void Dalvik_java_lang_Class_getComponentType(const u4* args,
    JValue* pResult)
{
    ClassObject* thisPtr = (ClassObject*) args[0];

    if (!dvmIsArrayClass(thisPtr))
        RETURN_PTR(NULL);

    /*
     * We can't just return thisPtr->elementClass, because that gives
     * us the base type (e.g. X[][][] returns X).  If this is a multi-
     * dimensional array, we have to do the lookup by name.
     */
    if (thisPtr->descriptor[1] == '[')
        RETURN_PTR(dvmFindArrayClass(&thisPtr->descriptor[1],
                   thisPtr->classLoader));
    else
        RETURN_PTR(thisPtr->elementClass);
}

/*
 * private static Class<?>[] getDeclaredClasses(Class<?> clazz,
 *     boolean publicOnly)
 *
 * Return an array with the classes that are declared by the specified class.
 * If "publicOnly" is set, we strip out any classes that don't have "public"
 * access.
 */
static void Dalvik_java_lang_Class_getDeclaredClasses(const u4* args,
    JValue* pResult)
{
    ClassObject* clazz = (ClassObject*) args[0];
    bool publicOnly = (args[1] != 0);
    ArrayObject* classes;

    classes = dvmGetDeclaredClasses(clazz);
    if (classes == NULL) {
        if (!dvmCheckException(dvmThreadSelf())) {
            /* empty list, so create a zero-length array */
            classes = dvmAllocArrayByClass(gDvm.classJavaLangClassArray,
                        0, ALLOC_DEFAULT);
        }
    } else if (publicOnly) {
        u4 count, newIdx, publicCount = 0;
        ClassObject** pSource = (ClassObject**) classes->contents;
        u4 length = classes->length;

        /* count up public classes */
        for (count = 0; count < length; count++) {
            if (dvmIsPublicClass(pSource[count]))
                publicCount++;
        }

        /* create a new array to hold them */
        ArrayObject* newClasses;
        newClasses = dvmAllocArrayByClass(gDvm.classJavaLangClassArray,
                        publicCount, ALLOC_DEFAULT);

        /* copy them over */
        for (count = newIdx = 0; count < length; count++) {
            if (dvmIsPublicClass(pSource[count])) {
                dvmSetObjectArrayElement(newClasses, newIdx,
                                         (Object *)pSource[count]);
                newIdx++;
            }
        }
        assert(newIdx == publicCount);
        dvmReleaseTrackedAlloc((Object*) classes, NULL);
        classes = newClasses;
    }

    dvmReleaseTrackedAlloc((Object*) classes, NULL);
    RETURN_PTR(classes);
}

/*
 * static Constructor[] getDeclaredConstructors(Class clazz, boolean publicOnly)
 *     throws SecurityException
 */
static void Dalvik_java_lang_Class_getDeclaredConstructors(const u4* args,
    JValue* pResult)
{
    ClassObject* clazz = (ClassObject*) args[0];
    bool publicOnly = (args[1] != 0);
    ArrayObject* constructors;

    constructors = dvmGetDeclaredConstructors(clazz, publicOnly);
    dvmReleaseTrackedAlloc((Object*) constructors, NULL);

    RETURN_PTR(constructors);
}

/*
 * static Field[] getDeclaredFields(Class klass, boolean publicOnly)
 *     throws SecurityException
 */
static void Dalvik_java_lang_Class_getDeclaredFields(const u4* args,
    JValue* pResult)
{
    ClassObject* clazz = (ClassObject*) args[0];
    bool publicOnly = (args[1] != 0);
    ArrayObject* fields;

    fields = dvmGetDeclaredFields(clazz, publicOnly);
    dvmReleaseTrackedAlloc((Object*) fields, NULL);

    RETURN_PTR(fields);
}

/*
 * static Method[] getDeclaredMethods(Class clazz, boolean publicOnly)
 *     throws SecurityException
 */
static void Dalvik_java_lang_Class_getDeclaredMethods(const u4* args,
    JValue* pResult)
{
    ClassObject* clazz = (ClassObject*) args[0];
    bool publicOnly = (args[1] != 0);
    ArrayObject* methods;

    methods = dvmGetDeclaredMethods(clazz, publicOnly);
    dvmReleaseTrackedAlloc((Object*) methods, NULL);

    RETURN_PTR(methods);
}

/*
 * Class[] getInterfaces()
 */
static void Dalvik_java_lang_Class_getInterfaces(const u4* args,
    JValue* pResult)
{
    ClassObject* clazz = (ClassObject*) args[0];
    ArrayObject* interfaces;

    interfaces = dvmGetInterfaces(clazz);
    dvmReleaseTrackedAlloc((Object*) interfaces, NULL);

    RETURN_PTR(interfaces);
}

/*
 * private static int getModifiers(Class klass, boolean
 *     ignoreInnerClassesAttrib)
 *
 * Return the class' modifier flags.  If "ignoreInnerClassesAttrib" is false,
 * and this is an inner class, we return the access flags from the inner class
 * attribute.
 */
static void Dalvik_java_lang_Class_getModifiers(const u4* args, JValue* pResult)
{
    ClassObject* clazz = (ClassObject*) args[0];
    bool ignoreInner = args[1];
    u4 accessFlags;

    accessFlags = clazz->accessFlags & JAVA_FLAGS_MASK;

    if (!ignoreInner) {
        /* see if we have an InnerClass annotation with flags in it */
        StringObject* className = NULL;
        int innerFlags;

        if (dvmGetInnerClass(clazz, &className, &innerFlags))
            accessFlags = innerFlags & JAVA_FLAGS_MASK;

        dvmReleaseTrackedAlloc((Object*) className, NULL);
    }

    RETURN_INT(accessFlags);
}

/*
 * private native String getNameNative()
 *
 * Return the class' name.
 */
static void Dalvik_java_lang_Class_getNameNative(const u4* args, JValue* pResult)
{
    ClassObject* clazz = (ClassObject*) args[0];
    const char* descriptor = clazz->descriptor;
    StringObject* nameObj;

    if ((descriptor[0] != 'L') && (descriptor[0] != '[')) {
        /*
         * The descriptor indicates that this is the class for
         * a primitive type; special-case the return value.
         */
        const char* name;
        switch (descriptor[0]) {
            case 'Z': name = "boolean"; break;
            case 'B': name = "byte";    break;
            case 'C': name = "char";    break;
            case 'S': name = "short";   break;
            case 'I': name = "int";     break;
            case 'J': name = "long";    break;
            case 'F': name = "float";   break;
            case 'D': name = "double";  break;
            case 'V': name = "void";    break;
            default: {
                LOGE("Unknown primitive type '%c'\n", descriptor[0]);
                assert(false);
                RETURN_PTR(NULL);
            }
        }

        nameObj = dvmCreateStringFromCstr(name);
    } else {
        /*
         * Convert the UTF-8 name to a java.lang.String. The
         * name must use '.' to separate package components.
         *
         * TODO: this could be more efficient. Consider a custom
         * conversion function here that walks the string once and
         * avoids the allocation for the common case (name less than,
         * say, 128 bytes).
         */
        char* dotName = dvmDescriptorToDot(clazz->descriptor);
        nameObj = dvmCreateStringFromCstr(dotName);
        free(dotName);
    }

    dvmReleaseTrackedAlloc((Object*) nameObj, NULL);

#if 0
    /* doesn't work -- need "java.lang.String" not "java/lang/String" */
    {
        /*
         * Find the string in the DEX file and use the copy in the intern
         * table if it already exists (else put one there).  Only works
         * for strings in the DEX file, e.g. not arrays.
         *
         * We have to do the class lookup by name in the DEX file because
         * we don't have a DexClassDef pointer in the ClassObject, and it's
         * not worth adding one there just for this.  Should be cheaper
         * to do this than the string-creation above.
         */
        const DexFile* pDexFile = clazz->pDexFile;
        const DexClassDef* pClassDef;
        const DexClassId* pClassId;

        pDexFile = clazz->pDexFile;
        pClassDef = dvmDexFindClass(pDexFile, clazz->descriptor);
        pClassId = dvmDexGetClassId(pDexFile, pClassDef->classIdx);
        nameObj = dvmDexGetResolvedString(pDexFile, pClassId->nameIdx);
        if (nameObj == NULL) {
            nameObj = dvmResolveString(clazz, pClassId->nameIdx);
            if (nameObj == NULL)
                LOGW("WARNING: couldn't find string %u for '%s'\n",
                    pClassId->nameIdx, clazz->name);
        }
    }
#endif

    RETURN_PTR(nameObj);
}

/*
 * Return the superclass for instances of this class.
 *
 * If the class represents a java/lang/Object, an interface, a primitive
 * type, or void (which *is* a primitive type??), return NULL.
 *
 * For an array, return the java/lang/Object ClassObject.
 */
static void Dalvik_java_lang_Class_getSuperclass(const u4* args,
    JValue* pResult)
{
    ClassObject* clazz = (ClassObject*) args[0];

    if (dvmIsPrimitiveClass(clazz) || dvmIsInterfaceClass(clazz))
        RETURN_PTR(NULL);
    else
        RETURN_PTR(clazz->super);
}

/*
 * public boolean isAssignableFrom(Class<?> cls)
 *
 * Determine if this class is either the same as, or is a superclass or
 * superinterface of, the class specified in the "cls" parameter.
 */
static void Dalvik_java_lang_Class_isAssignableFrom(const u4* args,
    JValue* pResult)
{
    ClassObject* thisPtr = (ClassObject*) args[0];
    ClassObject* testClass = (ClassObject*) args[1];

    if (testClass == NULL) {
        dvmThrowException("Ljava/lang/NullPointerException;", NULL);
        RETURN_INT(false);
    }
    RETURN_INT(dvmInstanceof(testClass, thisPtr));
}

/*
 * public boolean isInstance(Object o)
 *
 * Dynamic equivalent of Java programming language "instanceof".
 */
static void Dalvik_java_lang_Class_isInstance(const u4* args,
    JValue* pResult)
{
    ClassObject* thisPtr = (ClassObject*) args[0];
    Object* testObj = (Object*) args[1];

    if (testObj == NULL)
        RETURN_INT(false);
    RETURN_INT(dvmInstanceof(testObj->clazz, thisPtr));
}

/*
 * public boolean isInterface()
 */
static void Dalvik_java_lang_Class_isInterface(const u4* args,
    JValue* pResult)
{
    ClassObject* thisPtr = (ClassObject*) args[0];

    RETURN_INT(dvmIsInterfaceClass(thisPtr));
}

/*
 * public boolean isPrimitive()
 */
static void Dalvik_java_lang_Class_isPrimitive(const u4* args,
    JValue* pResult)
{
    ClassObject* thisPtr = (ClassObject*) args[0];

    RETURN_INT(dvmIsPrimitiveClass(thisPtr));
}

/*
 * public T newInstance() throws InstantiationException, IllegalAccessException
 *
 * Create a new instance of this class.
 */
static void Dalvik_java_lang_Class_newInstance(const u4* args, JValue* pResult)
{
    Thread* self = dvmThreadSelf();
    ClassObject* clazz = (ClassObject*) args[0];
    Method* init;
    Object* newObj;

    /* can't instantiate these */
    if (dvmIsPrimitiveClass(clazz) || dvmIsInterfaceClass(clazz)
        || dvmIsArrayClass(clazz) || dvmIsAbstractClass(clazz))
    {
        LOGD("newInstance failed: p%d i%d [%d a%d\n",
            dvmIsPrimitiveClass(clazz), dvmIsInterfaceClass(clazz),
            dvmIsArrayClass(clazz), dvmIsAbstractClass(clazz));
        dvmThrowExceptionWithClassMessage("Ljava/lang/InstantiationException;",
            clazz->descriptor);
        RETURN_VOID();
    }

    /* initialize the class if it hasn't been already */
    if (!dvmIsClassInitialized(clazz)) {
        if (!dvmInitClass(clazz)) {
            LOGW("Class init failed in newInstance call (%s)\n",
                clazz->descriptor);
            assert(dvmCheckException(self));
            RETURN_VOID();
        }
    }

    /* find the "nullary" constructor */
    init = dvmFindDirectMethodByDescriptor(clazz, "<init>", "()V");
    if (init == NULL) {
        /* common cause: secret "this" arg on non-static inner class ctor */
        LOGD("newInstance failed: no <init>()\n");
        dvmThrowExceptionWithClassMessage("Ljava/lang/InstantiationException;",
            clazz->descriptor);
        RETURN_VOID();
    }

    /*
     * Verify access from the call site.
     *
     * First, make sure the method invoking Class.newInstance() has permission
     * to access the class.
     *
     * Second, make sure it has permission to invoke the constructor.  The
     * constructor must be public or, if the caller is in the same package,
     * have package scope.
     */
    ClassObject* callerClass = dvmGetCaller2Class(self->curFrame);

    if (!dvmCheckClassAccess(callerClass, clazz)) {
        LOGD("newInstance failed: %s not accessible to %s\n",
            clazz->descriptor, callerClass->descriptor);
        dvmThrowException("Ljava/lang/IllegalAccessException;",
            "access to class not allowed");
        RETURN_VOID();
    }
    if (!dvmCheckMethodAccess(callerClass, init)) {
        LOGD("newInstance failed: %s.<init>() not accessible to %s\n",
            clazz->descriptor, callerClass->descriptor);
        dvmThrowException("Ljava/lang/IllegalAccessException;",
            "access to constructor not allowed");
        RETURN_VOID();
    }

    newObj = dvmAllocObject(clazz, ALLOC_DEFAULT);
    JValue unused;

    /* invoke constructor; unlike reflection calls, we don't wrap exceptions */
    dvmCallMethod(self, init, newObj, &unused);
    dvmReleaseTrackedAlloc(newObj, NULL);

    RETURN_PTR(newObj);
}

/*
 * private Object[] getSignatureAnnotation()
 *
 * Returns the signature annotation array.
 */
static void Dalvik_java_lang_Class_getSignatureAnnotation(const u4* args,
    JValue* pResult)
{
    ClassObject* clazz = (ClassObject*) args[0];
    ArrayObject* arr = dvmGetClassSignatureAnnotation(clazz);

    dvmReleaseTrackedAlloc((Object*) arr, NULL);
    RETURN_PTR(arr);
}

/*
 * public Class getDeclaringClass()
 *
 * Get the class that encloses this class (if any).
 */
static void Dalvik_java_lang_Class_getDeclaringClass(const u4* args,
    JValue* pResult)
{
    ClassObject* clazz = (ClassObject*) args[0];

    ClassObject* enclosing = dvmGetDeclaringClass(clazz);
    dvmReleaseTrackedAlloc((Object*) enclosing, NULL);
    RETURN_PTR(enclosing);
}

/*
 * public Class getEnclosingClass()
 *
 * Get the class that encloses this class (if any).
 */
static void Dalvik_java_lang_Class_getEnclosingClass(const u4* args,
    JValue* pResult)
{
    ClassObject* clazz = (ClassObject*) args[0];

    ClassObject* enclosing = dvmGetEnclosingClass(clazz);
    dvmReleaseTrackedAlloc((Object*) enclosing, NULL);
    RETURN_PTR(enclosing);
}

/*
 * public Constructor getEnclosingConstructor()
 *
 * Get the constructor that encloses this class (if any).
 */
static void Dalvik_java_lang_Class_getEnclosingConstructor(const u4* args,
    JValue* pResult)
{
    ClassObject* clazz = (ClassObject*) args[0];

    Object* enclosing = dvmGetEnclosingMethod(clazz);
    if (enclosing != NULL) {
        dvmReleaseTrackedAlloc(enclosing, NULL);
        if (enclosing->clazz == gDvm.classJavaLangReflectConstructor) {
            RETURN_PTR(enclosing);
        }
        assert(enclosing->clazz == gDvm.classJavaLangReflectMethod);
    }
    RETURN_PTR(NULL);
}

/*
 * public Method getEnclosingMethod()
 *
 * Get the method that encloses this class (if any).
 */
static void Dalvik_java_lang_Class_getEnclosingMethod(const u4* args,
    JValue* pResult)
{
    ClassObject* clazz = (ClassObject*) args[0];

    Object* enclosing = dvmGetEnclosingMethod(clazz);
    if (enclosing != NULL) {
        dvmReleaseTrackedAlloc(enclosing, NULL);
        if (enclosing->clazz == gDvm.classJavaLangReflectMethod) {
            RETURN_PTR(enclosing);
        }
        assert(enclosing->clazz == gDvm.classJavaLangReflectConstructor);
    }
    RETURN_PTR(NULL);
}

#if 0
static void Dalvik_java_lang_Class_getGenericInterfaces(const u4* args,
    JValue* pResult)
{
    dvmThrowException("Ljava/lang/UnsupportedOperationException;",
        "native method not implemented");

    RETURN_PTR(NULL);
}

static void Dalvik_java_lang_Class_getGenericSuperclass(const u4* args,
    JValue* pResult)
{
    dvmThrowException("Ljava/lang/UnsupportedOperationException;",
        "native method not implemented");

    RETURN_PTR(NULL);
}

static void Dalvik_java_lang_Class_getTypeParameters(const u4* args,
    JValue* pResult)
{
    dvmThrowException("Ljava/lang/UnsupportedOperationException;",
        "native method not implemented");

    RETURN_PTR(NULL);
}
#endif

/*
 * public boolean isAnonymousClass()
 *
 * Returns true if this is an "anonymous" class.
 */
static void Dalvik_java_lang_Class_isAnonymousClass(const u4* args,
    JValue* pResult)
{
    ClassObject* clazz = (ClassObject*) args[0];
    StringObject* className = NULL;
    int accessFlags;

    /*
     * If this has an InnerClass annotation, pull it out.  Lack of the
     * annotation, or an annotation with a NULL class name, indicates
     * that this is an anonymous inner class.
     */
    if (!dvmGetInnerClass(clazz, &className, &accessFlags))
        RETURN_BOOLEAN(false);

    dvmReleaseTrackedAlloc((Object*) className, NULL);
    RETURN_BOOLEAN(className == NULL);
}

/*
 * private Annotation[] getDeclaredAnnotations()
 *
 * Return the annotations declared on this class.
 */
static void Dalvik_java_lang_Class_getDeclaredAnnotations(const u4* args,
    JValue* pResult)
{
    ClassObject* clazz = (ClassObject*) args[0];

    ArrayObject* annos = dvmGetClassAnnotations(clazz);
    dvmReleaseTrackedAlloc((Object*) annos, NULL);
    RETURN_PTR(annos);
}

/*
 * public String getInnerClassName()
 *
 * Returns the simple name of a member class or local class, or null otherwise.
 */
static void Dalvik_java_lang_Class_getInnerClassName(const u4* args,
    JValue* pResult)
{
    ClassObject* clazz = (ClassObject*) args[0];
    StringObject* nameObj;
    int flags;

    if (dvmGetInnerClass(clazz, &nameObj, &flags)) {
        dvmReleaseTrackedAlloc((Object*) nameObj, NULL);
        RETURN_PTR(nameObj);
    } else {
        RETURN_PTR(NULL);
    }
}

/*
 * static native void setAccessibleNoCheck(AccessibleObject ao, boolean flag);
 */
static void Dalvik_java_lang_Class_setAccessibleNoCheck(const u4* args,
    JValue* pResult)
{
    Object* target = (Object*) args[0];
    u4 flag = (u4) args[1];

    dvmSetFieldBoolean(target, gDvm.offJavaLangReflectAccessibleObject_flag,
            flag);
}

const DalvikNativeMethod dvm_java_lang_Class[] = {
    { "desiredAssertionStatus", "()Z",
        Dalvik_java_lang_Class_desiredAssertionStatus },
    { "classForName",           "(Ljava/lang/String;ZLjava/lang/ClassLoader;)Ljava/lang/Class;",
        Dalvik_java_lang_Class_classForName },
    { "getClassLoader",         "(Ljava/lang/Class;)Ljava/lang/ClassLoader;",
        Dalvik_java_lang_Class_getClassLoader },
    { "getComponentType",       "()Ljava/lang/Class;",
        Dalvik_java_lang_Class_getComponentType },
    { "getSignatureAnnotation",  "()[Ljava/lang/Object;",
        Dalvik_java_lang_Class_getSignatureAnnotation },
    { "getDeclaredClasses",     "(Ljava/lang/Class;Z)[Ljava/lang/Class;",
        Dalvik_java_lang_Class_getDeclaredClasses },
    { "getDeclaredConstructors", "(Ljava/lang/Class;Z)[Ljava/lang/reflect/Constructor;",
        Dalvik_java_lang_Class_getDeclaredConstructors },
    { "getDeclaredFields",      "(Ljava/lang/Class;Z)[Ljava/lang/reflect/Field;",
        Dalvik_java_lang_Class_getDeclaredFields },
    { "getDeclaredMethods",     "(Ljava/lang/Class;Z)[Ljava/lang/reflect/Method;",
        Dalvik_java_lang_Class_getDeclaredMethods },
    { "getInterfaces",          "()[Ljava/lang/Class;",
        Dalvik_java_lang_Class_getInterfaces },
    { "getModifiers",           "(Ljava/lang/Class;Z)I",
        Dalvik_java_lang_Class_getModifiers },
    { "getNameNative",                "()Ljava/lang/String;",
        Dalvik_java_lang_Class_getNameNative },
    { "getSuperclass",          "()Ljava/lang/Class;",
        Dalvik_java_lang_Class_getSuperclass },
    { "isAssignableFrom",       "(Ljava/lang/Class;)Z",
        Dalvik_java_lang_Class_isAssignableFrom },
    { "isInstance",             "(Ljava/lang/Object;)Z",
        Dalvik_java_lang_Class_isInstance },
    { "isInterface",            "()Z",
        Dalvik_java_lang_Class_isInterface },
    { "isPrimitive",            "()Z",
        Dalvik_java_lang_Class_isPrimitive },
    { "newInstanceImpl",        "()Ljava/lang/Object;",
        Dalvik_java_lang_Class_newInstance },
    { "getDeclaringClass",      "()Ljava/lang/Class;",
        Dalvik_java_lang_Class_getDeclaringClass },
    { "getEnclosingClass",      "()Ljava/lang/Class;",
        Dalvik_java_lang_Class_getEnclosingClass },
    { "getEnclosingConstructor", "()Ljava/lang/reflect/Constructor;",
        Dalvik_java_lang_Class_getEnclosingConstructor },
    { "getEnclosingMethod",     "()Ljava/lang/reflect/Method;",
        Dalvik_java_lang_Class_getEnclosingMethod },
#if 0
    { "getGenericInterfaces",   "()[Ljava/lang/reflect/Type;",
        Dalvik_java_lang_Class_getGenericInterfaces },
    { "getGenericSuperclass",   "()Ljava/lang/reflect/Type;",
        Dalvik_java_lang_Class_getGenericSuperclass },
    { "getTypeParameters",      "()Ljava/lang/reflect/TypeVariable;",
        Dalvik_java_lang_Class_getTypeParameters },
#endif
    { "isAnonymousClass",       "()Z",
        Dalvik_java_lang_Class_isAnonymousClass },
    { "getDeclaredAnnotations", "()[Ljava/lang/annotation/Annotation;",
        Dalvik_java_lang_Class_getDeclaredAnnotations },
    { "getInnerClassName",       "()Ljava/lang/String;",
        Dalvik_java_lang_Class_getInnerClassName },
    { "setAccessibleNoCheck",   "(Ljava/lang/reflect/AccessibleObject;Z)V",
        Dalvik_java_lang_Class_setAccessibleNoCheck },
    { NULL, NULL, NULL },
};
