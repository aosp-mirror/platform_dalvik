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
 * Basic reflection calls and utility functions.
 */
#include "Dalvik.h"

#include <stdlib.h>

/*
 * Cache some classes.
 */
bool dvmReflectStartup(void)
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

    if (!dvmReflectProxyStartup())
        return false;
    if (!dvmReflectAnnotationStartup())
        return false;

    return true;
}

/*
 * Clean up.
 */
void dvmReflectShutdown(void)
{
    // nothing to do
}

/*
 * For some of the reflection stuff we need to un-box primitives, e.g.
 * convert a java/lang/Integer to int or even a float.  We assume that
 * the first instance field holds the value.
 *
 * To verify this, we either need to ensure that the class has only one
 * instance field, or we need to look up the field by name and verify
 * that it comes first.  The former is simpler, and should work.
 */
bool dvmValidateBoxClasses()
{
    static const char* classes[] = {
        "Ljava/lang/Boolean;",
        "Ljava/lang/Character;",
        "Ljava/lang/Float;",
        "Ljava/lang/Double;",
        "Ljava/lang/Byte;",
        "Ljava/lang/Short;",
        "Ljava/lang/Integer;",
        "Ljava/lang/Long;",
        NULL
    };
    const char** ccp;

    for (ccp = classes; *ccp != NULL; ccp++) {
        ClassObject* clazz;

        clazz = dvmFindClassNoInit(*ccp, NULL);
        if (clazz == NULL) {
            LOGE("Couldn't find '%s'\n", *ccp);
            return false;
        }

        if (clazz->ifieldCount != 1) {
            LOGE("Found %d instance fields in '%s'\n",
                clazz->ifieldCount, *ccp);
            return false;
        }
    }

    return true;
}


/*
 * Find the named class object.  We have to trim "*pSignature" down to just
 * the first token, do the lookup, and then restore anything important
 * that we've stomped on.
 *
 * "pSig" will be advanced to the start of the next token.
 */
static ClassObject* convertSignaturePartToClass(char** pSignature,
    const ClassObject* defClass)
{
    ClassObject* clazz = NULL;
    char* signature = *pSignature;

    if (*signature == '[') {
        /* looks like "[[[Landroid/debug/Stuff;"; we want the whole thing */
        char savedChar;

        while (*++signature == '[')
            ;
        if (*signature == 'L') {
            while (*++signature != ';')
                ;
        }

        /* advance past ';', and stomp on whatever comes next */
        savedChar = *++signature;
        *signature = '\0';
        clazz = dvmFindArrayClass(*pSignature, defClass->classLoader);
        *signature = savedChar;
    } else if (*signature == 'L') {
        /* looks like 'Landroid/debug/Stuff;"; we want the whole thing */
        char savedChar;
        while (*++signature != ';')
            ;
        savedChar = *++signature;
        *signature = '\0';
        clazz = dvmFindClassNoInit(*pSignature, defClass->classLoader);
        *signature = savedChar;
    } else {
        clazz = dvmFindPrimitiveClass(*signature++);
    }

    if (clazz == NULL) {
        LOGW("Unable to match class for part: '%s'\n", *pSignature);
        dvmClearException(dvmThreadSelf());
        dvmThrowException("Ljava/lang/NoSuchMethodException;", NULL);
    }
    *pSignature = signature;
    return clazz;
}

/*
 * Convert the method signature to an array of classes.
 *
 * The tokenization process may mangle "*pSignature".  On return, it will
 * be pointing at the closing ')'.
 *
 * "defClass" is the method's class, which is needed to make class loaders
 * happy.
 */
static ArrayObject* convertSignatureToClassArray(char** pSignature,
    ClassObject* defClass)
{
    ArrayObject* classArray;
    char* signature = *pSignature;
    char* cp;
    int i, count;

    assert(*signature == '(');
    signature++;

    /* count up the number of parameters */
    count = 0;
    cp = signature;
    while (*cp != ')') {
        count++;

        if (*cp == '[') {
            while (*++cp == '[')
                ;
        }
        if (*cp == 'L') {
            while (*++cp != ';')
                ;
        }
        cp++;
    }
    LOGVV("REFLECT found %d parameters in '%s'\n", count, *pSignature);

    /* create an array to hold them */
    classArray = dvmAllocArray(gDvm.classJavaLangClassArray, count,
                    kObjectArrayRefWidth, ALLOC_DEFAULT);
    if (classArray == NULL)
        return NULL;

    /* fill it in */
    cp = signature;
    for (i = 0; i < count; i++) {
        ClassObject* clazz;

        clazz = convertSignaturePartToClass(&cp, defClass);
        if (clazz == NULL) {
            assert(dvmCheckException(dvmThreadSelf()));
            return NULL;
        }
        LOGVV("REFLECT  %d: '%s'\n", i, clazz->descriptor);
        dvmSetObjectArrayElement(classArray, i, (Object *)clazz);
    }

    *pSignature = cp;

    /* caller must call dvmReleaseTrackedAlloc */
    return classArray;
}


/*
 * Convert a field pointer to a slot number.
 *
 * We use positive values starting from 0 for instance fields, negative
 * values starting from -1 for static fields.
 */
static int fieldToSlot(const Field* field, const ClassObject* clazz)
{
    int slot;

    if (dvmIsStaticField(field)) {
        slot = (StaticField*)field - &clazz->sfields[0];
        assert(slot >= 0 && slot < clazz->sfieldCount);
        slot = -(slot+1);
    } else {
        slot = (InstField*)field - clazz->ifields;
        assert(slot >= 0 && slot < clazz->ifieldCount);
    }

    return slot;
}

/*
 * Convert a slot number to a field pointer.
 */
Field* dvmSlotToField(ClassObject* clazz, int slot)
{
    if (slot < 0) {
        slot = -(slot+1);
        assert(slot < clazz->sfieldCount);
        return (Field*) &clazz->sfields[slot];
    } else {
        assert(slot < clazz->ifieldCount);
        return (Field*) &clazz->ifields[slot];
    }
}

/*
 * Create a new java.lang.reflect.Field object from "field".
 *
 * The Field spec doesn't specify the constructor.  We're going to use the
 * one from our existing class libs:
 *
 *  private Field(Class declaringClass, Class type, String name, int slot)
 */
static Object* createFieldObject(Field* field, const ClassObject* clazz)
{
    Object* result = NULL;
    Object* fieldObj = NULL;
    StringObject* nameObj = NULL;
    ClassObject* type;
    char* mangle;
    char* cp;
    int slot;

    assert(dvmIsClassInitialized(gDvm.classJavaLangReflectField));

    fieldObj = dvmAllocObject(gDvm.classJavaLangReflectField, ALLOC_DEFAULT);
    if (fieldObj == NULL)
        goto bail;

    cp = mangle = strdup(field->signature);
    type = convertSignaturePartToClass(&cp, clazz);
    free(mangle);
    if (type == NULL)
        goto bail;

    nameObj = dvmCreateStringFromCstr(field->name);
    if (nameObj == NULL)
        goto bail;

    slot = fieldToSlot(field, clazz);

    JValue unused;
    dvmCallMethod(dvmThreadSelf(), gDvm.methJavaLangReflectField_init,
        fieldObj, &unused, clazz, type, nameObj, slot);
    if (dvmCheckException(dvmThreadSelf())) {
        LOGD("Field class init threw exception\n");
        goto bail;
    }

    result = fieldObj;

bail:
    dvmReleaseTrackedAlloc((Object*) nameObj, NULL);
    if (result == NULL)
        dvmReleaseTrackedAlloc((Object*) fieldObj, NULL);
    /* caller must dvmReleaseTrackedAlloc(result) */
    return result;
}

/*
 *
 * Get an array with all fields declared by a class.
 *
 * This includes both static and instance fields.
 */
ArrayObject* dvmGetDeclaredFields(ClassObject* clazz, bool publicOnly)
{
    ArrayObject* fieldArray = NULL;
    int i, count;

    if (!dvmIsClassInitialized(gDvm.classJavaLangReflectField))
        dvmInitClass(gDvm.classJavaLangReflectField);

    /* count #of fields */
    if (!publicOnly)
        count = clazz->sfieldCount + clazz->ifieldCount;
    else {
        count = 0;
        for (i = 0; i < clazz->sfieldCount; i++) {
            if ((clazz->sfields[i].field.accessFlags & ACC_PUBLIC) != 0)
                count++;
        }
        for (i = 0; i < clazz->ifieldCount; i++) {
            if ((clazz->ifields[i].field.accessFlags & ACC_PUBLIC) != 0)
                count++;
        }
    }

    /* create the Field[] array */
    fieldArray = dvmAllocArray(gDvm.classJavaLangReflectFieldArray, count,
                    kObjectArrayRefWidth, ALLOC_DEFAULT);
    if (fieldArray == NULL)
        return NULL;

    /* populate */
    size_t fieldCount = 0;
    for (i = 0; i < clazz->sfieldCount; i++) {
        if (!publicOnly ||
            (clazz->sfields[i].field.accessFlags & ACC_PUBLIC) != 0)
        {
            Object* field = createFieldObject(&clazz->sfields[i].field, clazz);
            if (field == NULL) {
                goto fail;
            }
            dvmSetObjectArrayElement(fieldArray, fieldCount, field);
            dvmReleaseTrackedAlloc(field, NULL);
            ++fieldCount;
        }
    }
    for (i = 0; i < clazz->ifieldCount; i++) {
        if (!publicOnly ||
            (clazz->ifields[i].field.accessFlags & ACC_PUBLIC) != 0)
        {
            Object* field = createFieldObject(&clazz->ifields[i].field, clazz);
            if (field == NULL) {
                goto fail;
            }
            dvmSetObjectArrayElement(fieldArray, fieldCount, field);
            dvmReleaseTrackedAlloc(field, NULL);
            ++fieldCount;
        }
    }

    assert(fieldCount == fieldArray->length);

    /* caller must call dvmReleaseTrackedAlloc */
    return fieldArray;

fail:
    dvmReleaseTrackedAlloc((Object*) fieldArray, NULL);
    return NULL;
}


/*
 * Convert a method pointer to a slot number.
 *
 * We use positive values starting from 0 for virtual methods, negative
 * values starting from -1 for static methods.
 */
static int methodToSlot(const Method* meth)
{
    ClassObject* clazz = meth->clazz;
    int slot;

    if (dvmIsDirectMethod(meth)) {
        slot = meth - clazz->directMethods;
        assert(slot >= 0 && slot < clazz->directMethodCount);
        slot = -(slot+1);
    } else {
        slot = meth - clazz->virtualMethods;
        assert(slot >= 0 && slot < clazz->virtualMethodCount);
    }

    return slot;
}

/*
 * Convert a slot number to a method pointer.
 */
Method* dvmSlotToMethod(ClassObject* clazz, int slot)
{
    if (slot < 0) {
        slot = -(slot+1);
        assert(slot < clazz->directMethodCount);
        return &clazz->directMethods[slot];
    } else {
        assert(slot < clazz->virtualMethodCount);
        return &clazz->virtualMethods[slot];
    }
}

/*
 * Create a new java/lang/reflect/Constructor object, using the contents of
 * "meth" to construct it.
 *
 * The spec doesn't specify the constructor.  We're going to use the
 * one from our existing class libs:
 *
 *  private Constructor (Class declaringClass, Class[] ptypes, Class[] extypes,
 *      int slot)
 */
static Object* createConstructorObject(Method* meth)
{
    Object* result = NULL;
    ArrayObject* params = NULL;
    ArrayObject* exceptions = NULL;
    Object* consObj;
    DexStringCache mangle;
    char* cp;
    int slot;

    dexStringCacheInit(&mangle);

    /* parent should guarantee init so we don't have to check on every call */
    assert(dvmIsClassInitialized(gDvm.classJavaLangReflectConstructor));

    consObj = dvmAllocObject(gDvm.classJavaLangReflectConstructor,
                ALLOC_DEFAULT);
    if (consObj == NULL)
        goto bail;

    /*
     * Convert the signature string into an array of classes representing
     * the arguments.
     */
    cp = dvmCopyDescriptorStringFromMethod(meth, &mangle);
    params = convertSignatureToClassArray(&cp, meth->clazz);
    if (params == NULL)
        goto bail;
    assert(*cp == ')');
    assert(*(cp+1) == 'V');

    /*
     * Create an array with one entry for every exception that the class
     * is declared to throw.
     */
    exceptions = dvmGetMethodThrows(meth);
    if (dvmCheckException(dvmThreadSelf()))
        goto bail;

    slot = methodToSlot(meth);

    JValue unused;
    dvmCallMethod(dvmThreadSelf(), gDvm.methJavaLangReflectConstructor_init,
        consObj, &unused, meth->clazz, params, exceptions, slot);
    if (dvmCheckException(dvmThreadSelf())) {
        LOGD("Constructor class init threw exception\n");
        goto bail;
    }

    result = consObj;

bail:
    dexStringCacheRelease(&mangle);
    dvmReleaseTrackedAlloc((Object*) params, NULL);
    dvmReleaseTrackedAlloc((Object*) exceptions, NULL);
    if (result == NULL) {
        assert(dvmCheckException(dvmThreadSelf()));
        dvmReleaseTrackedAlloc(consObj, NULL);
    }
    /* caller must dvmReleaseTrackedAlloc(result) */
    return result;
}

/*
 * Get an array with all constructors declared by a class.
 */
ArrayObject* dvmGetDeclaredConstructors(ClassObject* clazz, bool publicOnly)
{
    ArrayObject* consArray;
    Method* meth;
    int i, count;

    if (!dvmIsClassInitialized(gDvm.classJavaLangReflectConstructor))
        dvmInitClass(gDvm.classJavaLangReflectConstructor);

    /*
     * Ordinarily we init the class the first time we resolve a method.
     * We're bypassing the normal resolution mechanism, so we init it here.
     */
    if (!dvmIsClassInitialized(clazz))
        dvmInitClass(clazz);

    /*
     * Count up the #of relevant methods.
     */
    count = 0;
    meth = clazz->directMethods;
    for (i = 0; i < clazz->directMethodCount; i++, meth++) {
        if ((!publicOnly || dvmIsPublicMethod(meth)) &&
            dvmIsConstructorMethod(meth) && !dvmIsStaticMethod(meth))
        {
            count++;
        }
    }

    /*
     * Create an array of Constructor objects.
     */
    consArray = dvmAllocArray(gDvm.classJavaLangReflectConstructorArray, count,
                kObjectArrayRefWidth, ALLOC_DEFAULT);
    if (consArray == NULL)
        return NULL;

    /*
     * Fill out the array.
     */
    meth = clazz->directMethods;
    size_t consObjCount = 0;
    for (i = 0; i < clazz->directMethodCount; i++, meth++) {
        if ((!publicOnly || dvmIsPublicMethod(meth)) &&
            dvmIsConstructorMethod(meth) && !dvmIsStaticMethod(meth))
        {
            Object* consObj = createConstructorObject(meth);
            if (consObj == NULL)
                goto fail;
            dvmSetObjectArrayElement(consArray, consObjCount, consObj);
            ++consObjCount;
            dvmReleaseTrackedAlloc(consObj, NULL);
        }
    }

    assert(consObjCount == consArray->length);

    /* caller must call dvmReleaseTrackedAlloc */
    return consArray;

fail:
    dvmReleaseTrackedAlloc((Object*) consArray, NULL);
    return NULL;
}

/*
 * Create a new java/lang/reflect/Method object, using the contents of
 * "meth" to construct it.
 *
 * The spec doesn't specify the constructor.  We're going to use the
 * one from our existing class libs:
 *
 *  private Method(Class declaring, Class[] paramTypes, Class[] exceptTypes,
 *      Class returnType, String name, int slot)
 *
 * The caller must call dvmReleaseTrackedAlloc() on the result.
 */
Object* dvmCreateReflectMethodObject(const Method* meth)
{
    Object* result = NULL;
    ArrayObject* params = NULL;
    ArrayObject* exceptions = NULL;
    StringObject* nameObj = NULL;
    Object* methObj;
    ClassObject* returnType;
    DexStringCache mangle;
    char* cp;
    int slot;

    if (dvmCheckException(dvmThreadSelf())) {
        LOGW("WARNING: dvmCreateReflectMethodObject called with "
             "exception pending\n");
        return NULL;
    }

    dexStringCacheInit(&mangle);

    /* parent should guarantee init so we don't have to check on every call */
    assert(dvmIsClassInitialized(gDvm.classJavaLangReflectMethod));

    methObj = dvmAllocObject(gDvm.classJavaLangReflectMethod, ALLOC_DEFAULT);
    if (methObj == NULL)
        goto bail;

    /*
     * Convert the signature string into an array of classes representing
     * the arguments, and a class for the return type.
     */
    cp = dvmCopyDescriptorStringFromMethod(meth, &mangle);
    params = convertSignatureToClassArray(&cp, meth->clazz);
    if (params == NULL)
        goto bail;
    assert(*cp == ')');
    cp++;
    returnType = convertSignaturePartToClass(&cp, meth->clazz);
    if (returnType == NULL)
        goto bail;

    /*
     * Create an array with one entry for every exception that the class
     * is declared to throw.
     */
    exceptions = dvmGetMethodThrows(meth);
    if (dvmCheckException(dvmThreadSelf()))
        goto bail;

    /* method name */
    nameObj = dvmCreateStringFromCstr(meth->name);
    if (nameObj == NULL)
        goto bail;

    slot = methodToSlot(meth);

    JValue unused;
    dvmCallMethod(dvmThreadSelf(), gDvm.methJavaLangReflectMethod_init,
        methObj, &unused, meth->clazz, params, exceptions, returnType,
        nameObj, slot);
    if (dvmCheckException(dvmThreadSelf())) {
        LOGD("Method class init threw exception\n");
        goto bail;
    }

    result = methObj;

bail:
    dexStringCacheRelease(&mangle);
    if (result == NULL) {
        assert(dvmCheckException(dvmThreadSelf()));
    }
    dvmReleaseTrackedAlloc((Object*) nameObj, NULL);
    dvmReleaseTrackedAlloc((Object*) params, NULL);
    dvmReleaseTrackedAlloc((Object*) exceptions, NULL);
    if (result == NULL)
        dvmReleaseTrackedAlloc(methObj, NULL);
    return result;
}

/*
 * Get an array with all methods declared by a class.
 *
 * This includes both static and virtual methods, and can include private
 * members if "publicOnly" is false.  It does not include Miranda methods,
 * since those weren't declared in the class, or constructors.
 */
ArrayObject* dvmGetDeclaredMethods(ClassObject* clazz, bool publicOnly)
{
    ArrayObject* methodArray;
    Method* meth;
    int i, count;

    if (!dvmIsClassInitialized(gDvm.classJavaLangReflectMethod))
        dvmInitClass(gDvm.classJavaLangReflectMethod);

    /*
     * Count up the #of relevant methods.
     *
     * Ignore virtual Miranda methods and direct class/object constructors.
     */
    count = 0;
    meth = clazz->virtualMethods;
    for (i = 0; i < clazz->virtualMethodCount; i++, meth++) {
        if ((!publicOnly || dvmIsPublicMethod(meth)) &&
            !dvmIsMirandaMethod(meth))
        {
            count++;
        }
    }
    meth = clazz->directMethods;
    for (i = 0; i < clazz->directMethodCount; i++, meth++) {
        if ((!publicOnly || dvmIsPublicMethod(meth)) &&
            meth->name[0] != '<')
        {
            count++;
        }
    }

    /*
     * Create an array of Method objects.
     */
    methodArray = dvmAllocArray(gDvm.classJavaLangReflectMethodArray, count,
                kObjectArrayRefWidth, ALLOC_DEFAULT);
    if (methodArray == NULL)
        return NULL;


    /*
     * Fill out the array.
     */
    meth = clazz->virtualMethods;
    size_t methObjCount = 0;
    for (i = 0; i < clazz->virtualMethodCount; i++, meth++) {
        if ((!publicOnly || dvmIsPublicMethod(meth)) &&
            !dvmIsMirandaMethod(meth))
        {
            Object* methObj = dvmCreateReflectMethodObject(meth);
            if (methObj == NULL)
                goto fail;
            dvmSetObjectArrayElement(methodArray, methObjCount, methObj);
            ++methObjCount;
            dvmReleaseTrackedAlloc(methObj, NULL);
        }
    }
    meth = clazz->directMethods;
    for (i = 0; i < clazz->directMethodCount; i++, meth++) {
        if ((!publicOnly || dvmIsPublicMethod(meth)) &&
            meth->name[0] != '<')
        {
            Object* methObj = dvmCreateReflectMethodObject(meth);
            if (methObj == NULL)
                goto fail;
            dvmSetObjectArrayElement(methodArray, methObjCount, methObj);
            ++methObjCount;
            dvmReleaseTrackedAlloc(methObj, NULL);
        }
    }

    assert(methObjCount == methodArray->length);

    /* caller must call dvmReleaseTrackedAlloc */
    return methodArray;

fail:
    dvmReleaseTrackedAlloc((Object*) methodArray, NULL);
    return NULL;
}

/*
 * Get all interfaces a class implements. If this is unable to allocate
 * the result array, this raises an OutOfMemoryError and returns NULL.
 */
ArrayObject* dvmGetInterfaces(ClassObject* clazz)
{
    ArrayObject* interfaceArray;

    if (!dvmIsClassInitialized(gDvm.classJavaLangReflectMethod))
        dvmInitClass(gDvm.classJavaLangReflectMethod);

    /*
     * Create an array of Class objects.
     */
    int count = clazz->interfaceCount;
    interfaceArray = dvmAllocArray(gDvm.classJavaLangClassArray, count,
                kObjectArrayRefWidth, ALLOC_DEFAULT);
    if (interfaceArray == NULL)
        return NULL;

    /*
     * Fill out the array.
     */
    memcpy(interfaceArray->contents, clazz->interfaces,
           count * sizeof(Object *));
    dvmWriteBarrierArray(interfaceArray, 0, count);

    /* caller must call dvmReleaseTrackedAlloc */
    return interfaceArray;
}

/*
 * Given a boxed primitive type, such as java/lang/Integer, return the
 * primitive type index.
 *
 * Returns PRIM_NOT for void, since we never "box" that.
 */
static PrimitiveType getBoxedType(DataObject* arg)
{
    static const int kJavaLangLen = 11;     // strlen("Ljava/lang/")
    const char* name;

    if (arg == NULL)
        return PRIM_NOT;

    name = arg->obj.clazz->descriptor;

    if (strncmp(name, "Ljava/lang/", kJavaLangLen) != 0)
        return PRIM_NOT;

    if (strcmp(name + kJavaLangLen, "Boolean;") == 0)
        return PRIM_BOOLEAN;
    if (strcmp(name + kJavaLangLen, "Character;") == 0)
        return PRIM_CHAR;
    if (strcmp(name + kJavaLangLen, "Float;") == 0)
        return PRIM_FLOAT;
    if (strcmp(name + kJavaLangLen, "Double;") == 0)
        return PRIM_DOUBLE;
    if (strcmp(name + kJavaLangLen, "Byte;") == 0)
        return PRIM_BYTE;
    if (strcmp(name + kJavaLangLen, "Short;") == 0)
        return PRIM_SHORT;
    if (strcmp(name + kJavaLangLen, "Integer;") == 0)
        return PRIM_INT;
    if (strcmp(name + kJavaLangLen, "Long;") == 0)
        return PRIM_LONG;
    return PRIM_NOT;
}

/*
 * Convert primitive, boxed data from "srcPtr" to "dstPtr".
 *
 * Section v2 2.6 lists the various conversions and promotions.  We
 * allow the "widening" and "identity" conversions, but don't allow the
 * "narrowing" conversions.
 *
 * Allowed:
 *  byte to short, int, long, float, double
 *  short to int, long, float double
 *  char to int, long, float, double
 *  int to long, float, double
 *  long to float, double
 *  float to double
 * Values of types byte, char, and short are "internally" widened to int.
 *
 * Returns the width in bytes of the destination primitive, or -1 if the
 * conversion is not allowed.
 *
 * TODO? use JValue rather than u4 pointers
 */
int dvmConvertPrimitiveValue(PrimitiveType srcType,
    PrimitiveType dstType, const s4* srcPtr, s4* dstPtr)
{
    enum {
        OK4, OK8, ItoJ,
        ItoD, JtoD, FtoD,
        ItoF, JtoF,
        bad, kMax
    };
    /* [src][dst] */
    static const int kConvMode[kMax][kMax] = {
    /*FROM *TO: bool    char    float   double  byte    short   int     long */
    /*bool */ { OK4,    bad,    bad,    bad,    bad,    bad,    bad,    bad  },
    /*char */ { bad,    OK4,    ItoF,   ItoD,   bad,    bad,    OK4,    ItoJ },
    /*float*/ { bad,    bad,    OK4,    FtoD,   bad,    bad,    bad,    bad  },
    /*doubl*/ { bad,    bad,    bad,    OK8,    bad,    bad,    bad,    bad  },
    /*byte */ { bad,    bad,    ItoF,   ItoD,   OK4,    OK4,    OK4,    ItoJ },
    /*short*/ { bad,    bad,    ItoF,   ItoD,   bad,    OK4,    OK4,    ItoJ },
    /*int  */ { bad,    bad,    ItoF,   ItoD,   bad,    bad,    OK4,    ItoJ },
    /*long */ { bad,    bad,    JtoF,   JtoD,   bad,    bad,    bad,    OK8  },
    };
    int result;

    assert(srcType != PRIM_NOT && dstType != PRIM_NOT &&
           srcType != PRIM_VOID && dstType != PRIM_VOID);
    result = kConvMode[srcType][dstType];

    //LOGV("+++ convprim: src=%d dst=%d result=%d\n", srcType, dstType, result);

    switch (result) {
    case OK4:
        *dstPtr = *srcPtr;
        return 1;
    case OK8:
        *(s8*)dstPtr = *(s8*)srcPtr;
        return 2;
    case ItoJ:
        *(s8*)dstPtr = (s8) (*(s4*) srcPtr);
        return 2;
    case ItoD:
        *(double*)dstPtr = (double) (*(s4*) srcPtr);
        return 2;
    case JtoD:
        *(double*)dstPtr = (double) (*(long long*) srcPtr);
        return 2;
    case FtoD:
        *(double*)dstPtr = (double) (*(float*) srcPtr);
        return 2;
    case ItoF:
        *(float*)dstPtr = (float) (*(int*) srcPtr);
        return 1;
    case JtoF:
        *(float*)dstPtr = (float) (*(long long*) srcPtr);
        return 1;
    case bad:
        LOGV("convert primitive: prim %d to %d not allowed\n",
            srcType, dstType);
        return -1;
    default:
        assert(false);
        return -1;
    }
}

/*
 * Convert types and widen primitives.  Puts the value of "arg" into
 * "destPtr".
 *
 * Returns the width of the argument in 32-bit words (1 or 2), or -1 on error.
 */
int dvmConvertArgument(DataObject* arg, ClassObject* type, s4* destPtr)
{
    int retVal;

    if (dvmIsPrimitiveClass(type)) {
        /* e.g.: "arg" is java/lang/Float instance, "type" is VM float class */
        PrimitiveType srcType;
        s4* valuePtr;

        srcType = getBoxedType(arg);
        if (srcType < 0) {     // didn't pass a boxed primitive in
            LOGVV("conv arg: type '%s' not boxed primitive\n",
                arg->obj.clazz->descriptor);
            return -1;
        }

        /* assumes value is stored in first instance field */
        valuePtr = (s4*) arg->instanceData;

        retVal = dvmConvertPrimitiveValue(srcType, type->primitiveType,
                    valuePtr, destPtr);
    } else {
        /* verify object is compatible */
        if ((arg == NULL) || dvmInstanceof(arg->obj.clazz, type)) {
            *destPtr = (s4) arg;
            retVal = 1;
        } else {
            LOGVV("Arg %p (%s) not compatible with %s\n",
                arg, arg->obj.clazz->descriptor, type->descriptor);
            retVal = -1;
        }
    }

    return retVal;
}

/*
 * Create a wrapper object for a primitive data type.  If "returnType" is
 * not primitive, this just casts "value" to an object and returns it.
 *
 * We could invoke the "toValue" method on the box types to take
 * advantage of pre-created values, but running that through the
 * interpreter is probably less efficient than just allocating storage here.
 *
 * The caller must call dvmReleaseTrackedAlloc on the result.
 */
DataObject* dvmWrapPrimitive(JValue value, ClassObject* returnType)
{
    static const char* boxTypes[] = {       // order from enum PrimitiveType
        "Ljava/lang/Boolean;",
        "Ljava/lang/Character;",
        "Ljava/lang/Float;",
        "Ljava/lang/Double;",
        "Ljava/lang/Byte;",
        "Ljava/lang/Short;",
        "Ljava/lang/Integer;",
        "Ljava/lang/Long;"
    };
    ClassObject* wrapperClass;
    DataObject* wrapperObj;
    s4* dataPtr;
    PrimitiveType typeIndex = returnType->primitiveType;
    const char* classDescriptor;

    if (typeIndex == PRIM_NOT) {
        /* add to tracking table so return value is always in table */
        if (value.l != NULL)
            dvmAddTrackedAlloc(value.l, NULL);
        return (DataObject*) value.l;
    }

    assert(typeIndex >= 0 && typeIndex < PRIM_MAX);
    if (typeIndex == PRIM_VOID)
        return NULL;

    classDescriptor = boxTypes[typeIndex];

    wrapperClass = dvmFindSystemClass(classDescriptor);
    if (wrapperClass == NULL) {
        LOGW("Unable to find '%s'\n", classDescriptor);
        assert(dvmCheckException(dvmThreadSelf()));
        return NULL;
    }

    wrapperObj = (DataObject*) dvmAllocObject(wrapperClass, ALLOC_DEFAULT);
    if (wrapperObj == NULL)
        return NULL;
    dataPtr = (s4*) wrapperObj->instanceData;

    /* assumes value is stored in first instance field */
    /* (see dvmValidateBoxClasses) */
    if (typeIndex == PRIM_LONG || typeIndex == PRIM_DOUBLE)
        *(s8*)dataPtr = value.j;
    else
        *dataPtr = value.i;

    return wrapperObj;
}

/*
 * Unwrap a primitive data type, if necessary.
 *
 * If "returnType" is not primitive, we just tuck "value" into JValue and
 * return it after verifying that it's the right type of object.
 *
 * Fails if the field is primitive and "value" is either not a boxed
 * primitive or is of a type that cannot be converted.
 *
 * Returns "true" on success, "false" on failure.
 */
bool dvmUnwrapPrimitive(Object* value, ClassObject* returnType,
    JValue* pResult)
{
    PrimitiveType typeIndex = returnType->primitiveType;
    PrimitiveType valueIndex;

    if (typeIndex == PRIM_NOT) {
        if (value != NULL && !dvmInstanceof(value->clazz, returnType)) {
            LOGD("wrong object type: %s %s\n",
                value->clazz->descriptor, returnType->descriptor);
            return false;
        }
        pResult->l = value;
        return true;
    } else if (typeIndex == PRIM_VOID) {
        /* can't put anything into a void */
        return false;
    }

    valueIndex = getBoxedType((DataObject*)value);
    if (valueIndex == PRIM_NOT)
        return false;

    /* assumes value is stored in first instance field of "value" */
    /* (see dvmValidateBoxClasses) */
    if (dvmConvertPrimitiveValue(valueIndex, typeIndex,
            (s4*) ((DataObject*)value)->instanceData, (s4*)pResult) < 0)
    {
        LOGV("Prim conversion failed\n");
        return false;
    }

    return true;
}


/*
 * Find the return type in the signature, and convert it to a class
 * object.  For primitive types we use a boxed class, for reference types
 * we do a name lookup.
 *
 * On failure, we return NULL with an exception raised.
 */
ClassObject* dvmGetBoxedReturnType(const Method* meth)
{
    const char* sig = dexProtoGetReturnType(&meth->prototype);

    switch (*sig) {
    case 'Z':
    case 'C':
    case 'F':
    case 'D':
    case 'B':
    case 'S':
    case 'I':
    case 'J':
    case 'V':
        return dvmFindPrimitiveClass(*sig);
    case '[':
    case 'L':
        return dvmFindClass(sig, meth->clazz->classLoader);
    default: {
        /* should not have passed verification */
        char* desc = dexProtoCopyMethodDescriptor(&meth->prototype);
        LOGE("Bad return type in signature '%s'\n", desc);
        free(desc);
        dvmThrowException("Ljava/lang/InternalError;", NULL);
        return NULL;
    }
    }
}


/*
 * JNI reflection support: convert reflection object to Field ptr.
 */
Field* dvmGetFieldFromReflectObj(Object* obj)
{
    ClassObject* clazz;
    int slot;

    assert(obj->clazz == gDvm.classJavaLangReflectField);
    clazz = (ClassObject*)dvmGetFieldObject(obj,
                                gDvm.offJavaLangReflectField_declClass);
    slot = dvmGetFieldInt(obj, gDvm.offJavaLangReflectField_slot);

    /* must initialize the class before returning a field ID */
    if (!dvmInitClass(clazz))
        return NULL;

    return dvmSlotToField(clazz, slot);
}

/*
 * JNI reflection support: convert reflection object to Method ptr.
 */
Method* dvmGetMethodFromReflectObj(Object* obj)
{
    ClassObject* clazz;
    int slot;

    if (obj->clazz == gDvm.classJavaLangReflectConstructor) {
        clazz = (ClassObject*)dvmGetFieldObject(obj,
                                gDvm.offJavaLangReflectConstructor_declClass);
        slot = dvmGetFieldInt(obj, gDvm.offJavaLangReflectConstructor_slot);
    } else if (obj->clazz == gDvm.classJavaLangReflectMethod) {
        clazz = (ClassObject*)dvmGetFieldObject(obj,
                                gDvm.offJavaLangReflectMethod_declClass);
        slot = dvmGetFieldInt(obj, gDvm.offJavaLangReflectMethod_slot);
    } else {
        assert(false);
        return NULL;
    }

    /* must initialize the class before returning a method ID */
    if (!dvmInitClass(clazz))
        return NULL;

    return dvmSlotToMethod(clazz, slot);
}

/*
 * JNI reflection support: convert Field to reflection object.
 *
 * The return value is a java.lang.reflect.Field.
 *
 * Caller must call dvmReleaseTrackedAlloc().
 */
Object* dvmCreateReflectObjForField(const ClassObject* clazz, Field* field)
{
    if (!dvmIsClassInitialized(gDvm.classJavaLangReflectField))
        dvmInitClass(gDvm.classJavaLangReflectField);

    /* caller must dvmReleaseTrackedAlloc(result) */
    return createFieldObject(field, clazz);
}

/*
 * JNI reflection support: convert Method to reflection object.
 *
 * The returned object will be either a java.lang.reflect.Method or
 * .Constructor, depending on whether "method" is a constructor.
 *
 * This is also used for certain "system" annotations.
 *
 * Caller must call dvmReleaseTrackedAlloc().
 */
Object* dvmCreateReflectObjForMethod(const ClassObject* clazz, Method* method)
{
    UNUSED_PARAMETER(clazz);

    if (strcmp(method->name, "<init>") == 0) {
        if (!dvmIsClassInitialized(gDvm.classJavaLangReflectConstructor))
            dvmInitClass(gDvm.classJavaLangReflectConstructor);

        return createConstructorObject(method);
    } else {
        if (!dvmIsClassInitialized(gDvm.classJavaLangReflectMethod))
            dvmInitClass(gDvm.classJavaLangReflectMethod);

        return dvmCreateReflectMethodObject(method);
    }
}
