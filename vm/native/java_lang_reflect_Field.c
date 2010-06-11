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
 * java.lang.reflect.Field
 */
#include "Dalvik.h"
#include "native/InternalNativePriv.h"


/*
 * Get the address of a field from an object.  This can be used with "get"
 * or "set".
 *
 * "declaringClass" is the class in which the field was declared.  For an
 * instance field, "obj" is the object that holds the field data; for a
 * static field its value is ignored.
 *
 * "If the underlying field is static, the class that declared the
 * field is initialized if it has not already been initialized."
 *
 * On failure, throws an exception and returns NULL.
 *
 * The documentation lists exceptional conditions and the exceptions that
 * should be thrown, but doesn't say which exception previals when two or
 * more exceptional conditions exist at the same time.  For example,
 * attempting to set a protected field from an unrelated class causes an
 * IllegalAccessException, while passing in a data type that doesn't match
 * the field causes an IllegalArgumentException.  If code does both at the
 * same time, we have to choose one or othe other.
 *
 * The expected order is:
 *  (1) Check for illegal access. Throw IllegalAccessException.
 *  (2) Make sure the object actually has the field.  Throw
 *      IllegalArgumentException.
 *  (3) Make sure the field matches the expected type, e.g. if we issued
 *      a "getInteger" call make sure the field is an integer or can be
 *      converted to an int with a widening conversion.  Throw
 *      IllegalArgumentException.
 *  (4) Make sure "obj" is not null.  Throw NullPointerException.
 *
 * TODO: we're currently handling #3 after #4, because we don't check the
 * widening conversion until we're actually extracting the value from the
 * object (which won't work well if it's a null reference).
 */
static JValue* getFieldDataAddr(Object* obj, ClassObject* declaringClass,
    int slot, bool isSetOperation, bool noAccessCheck)
{
    Field* field;
    JValue* result;

    field = dvmSlotToField(declaringClass, slot);
    assert(field != NULL);

    /* verify access */
    if (!noAccessCheck) {
        if (isSetOperation && dvmIsFinalField(field)) {
            dvmThrowException("Ljava/lang/IllegalAccessException;",
                "field is marked 'final'");
            return NULL;
        }

        ClassObject* callerClass =
            dvmGetCaller2Class(dvmThreadSelf()->curFrame);

        /*
         * We need to check two things:
         *  (1) Would an instance of the calling class have access to the field?
         *  (2) If the field is "protected", is the object an instance of the
         *      calling class, or is the field's declaring class in the same
         *      package as the calling class?
         *
         * #1 is basic access control.  #2 ensures that, just because
         * you're a subclass of Foo, you can't mess with protected fields
         * in arbitrary Foo objects from other packages.
         */
        if (!dvmCheckFieldAccess(callerClass, field)) {
            dvmThrowException("Ljava/lang/IllegalAccessException;",
                "access to field not allowed");
            return NULL;
        }
        if (dvmIsProtectedField(field)) {
            bool isInstance, samePackage;

            if (obj != NULL)
                isInstance = dvmInstanceof(obj->clazz, callerClass);
            else
                isInstance = false;
            samePackage = dvmInSamePackage(declaringClass, callerClass);

            if (!isInstance && !samePackage) {
                dvmThrowException("Ljava/lang/IllegalAccessException;",
                    "access to protected field not allowed");
                return NULL;
            }
        }
    }

    if (dvmIsStaticField(field)) {
        /* init class if necessary, then return ptr to storage in "field" */
        if (!dvmIsClassInitialized(declaringClass)) {
            if (!dvmInitClass(declaringClass)) {
                assert(dvmCheckException(dvmThreadSelf()));
                return NULL;
            }
        }

        result = dvmStaticFieldPtr((StaticField*) field);
    } else {
        /*
         * Verify object is of correct type (i.e. it actually has the
         * expected field in it), then grab a pointer to obj storage.
         * The call to dvmVerifyObjectInClass throws an NPE if "obj" is NULL.
         */
        if (!dvmVerifyObjectInClass(obj, declaringClass)) {
            assert(dvmCheckException(dvmThreadSelf()));
            if (obj != NULL) {
                LOGD("Wrong type of object for field lookup: %s %s\n",
                    obj->clazz->descriptor, declaringClass->descriptor);
            }
            return NULL;
        }
        result = dvmFieldPtr(obj, ((InstField*) field)->byteOffset);
    }

    return result;
}

/*
 * public int getFieldModifiers(Class declaringClass, int slot)
 */
static void Dalvik_java_lang_reflect_Field_getFieldModifiers(
    const u4* args, JValue* pResult)
{
    // ignore thisPtr in args[0]
    ClassObject* declaringClass = (ClassObject*) args[1];
    int slot = args[2];
    Field* field;

    field = dvmSlotToField(declaringClass, slot);
    RETURN_INT(field->accessFlags & JAVA_FLAGS_MASK);
}

/*
 * private Object getField(Object o, Class declaringClass, Class type,
 *     int slot, boolean noAccessCheck)
 *
 * Primitive types need to be boxed.
 */
static void Dalvik_java_lang_reflect_Field_getField(const u4* args,
    JValue* pResult)
{
    // ignore thisPtr in args[0]
    Object* obj = (Object*) args[1];
    ClassObject* declaringClass = (ClassObject*) args[2];
    ClassObject* fieldType = (ClassObject*) args[3];
    int slot = args[4];
    bool noAccessCheck = (args[5] != 0);
    JValue value;
    const JValue* fieldPtr;
    DataObject* result;

    //dvmDumpClass(obj->clazz, kDumpClassFullDetail);

    /* get a pointer to the field's data; performs access checks */
    fieldPtr = getFieldDataAddr(obj, declaringClass, slot, false,noAccessCheck);
    if (fieldPtr == NULL)
        RETURN_VOID();

    /* copy 4 or 8 bytes out */
    if (fieldType->primitiveType == PRIM_LONG ||
        fieldType->primitiveType == PRIM_DOUBLE)
    {
        value.j = fieldPtr->j;
    } else {
        value.i = fieldPtr->i;
    }

    result = dvmWrapPrimitive(value, fieldType);
    dvmReleaseTrackedAlloc((Object*) result, NULL);
    RETURN_PTR(result);
}

/*
 * private void setField(Object o, Class declaringClass, Class type,
 *     int slot, boolean noAccessCheck, Object value)
 *
 * When assigning into a primitive field we will automatically extract
 * the value from box types.
 */
static void Dalvik_java_lang_reflect_Field_setField(const u4* args,
    JValue* pResult)
{
    // ignore thisPtr in args[0]
    Object* obj = (Object*) args[1];
    ClassObject* declaringClass = (ClassObject*) args[2];
    ClassObject* fieldType = (ClassObject*) args[3];
    int slot = args[4];
    bool noAccessCheck = (args[5] != 0);
    Object* valueObj = (Object*) args[6];
    JValue* fieldPtr;
    JValue value;

    /* unwrap primitive, or verify object type */
    if (!dvmUnwrapPrimitive(valueObj, fieldType, &value)) {
        dvmThrowException("Ljava/lang/IllegalArgumentException;",
            "invalid value for field");
        RETURN_VOID();
    }

    /* get a pointer to the field's data; performs access checks */
    fieldPtr = getFieldDataAddr(obj, declaringClass, slot, true, noAccessCheck);
    if (fieldPtr == NULL)
        RETURN_VOID();

    /* store 4 or 8 bytes */
    if (fieldType->primitiveType == PRIM_LONG ||
        fieldType->primitiveType == PRIM_DOUBLE)
    {
        fieldPtr->j = value.j;
    } else if (fieldType->primitiveType == PRIM_NOT) {
        if (slot < 0) {
            StaticField *sfield;
            sfield = (StaticField *)dvmSlotToField(declaringClass, slot);
            assert(fieldPtr == &sfield->value);
            dvmSetStaticFieldObject(sfield, value.l);
        } else {
            int offset = declaringClass->ifields[slot].byteOffset;
            assert(fieldPtr == (JValue *)BYTE_OFFSET(obj, offset));
            dvmSetFieldObject(obj, offset, value.l);
        }
    } else {
        fieldPtr->i = value.i;
    }

    RETURN_VOID();
}

/*
 * Convert a reflection primitive type ordinal (inherited from the previous
 * VM's reflection classes) to our value.
 */
static PrimitiveType convPrimType(int typeNum)
{
    static const PrimitiveType conv[PRIM_MAX] = {
        PRIM_NOT, PRIM_BOOLEAN, PRIM_BYTE, PRIM_CHAR, PRIM_SHORT,
        PRIM_INT, PRIM_FLOAT, PRIM_LONG, PRIM_DOUBLE
    };
    if (typeNum <= 0 || typeNum > 8)
        return PRIM_NOT;
    return conv[typeNum];
}

/*
 * Primitive field getters, e.g.:
 * private double getIField(Object o, Class declaringClass,
 *     Class type, int slot, boolean noAccessCheck, int type_no)
 *
 * The "type_no" is defined by the java.lang.reflect.Field class.
 */
static void Dalvik_java_lang_reflect_Field_getPrimitiveField(const u4* args,
    JValue* pResult)
{
    // ignore thisPtr in args[0]
    Object* obj = (Object*) args[1];
    ClassObject* declaringClass = (ClassObject*) args[2];
    ClassObject* fieldType = (ClassObject*) args[3];
    int slot = args[4];
    bool noAccessCheck = (args[5] != 0);
    int typeNum = args[6];
    PrimitiveType targetType = convPrimType(typeNum);
    const JValue* fieldPtr;
    JValue value;

    if (!dvmIsPrimitiveClass(fieldType)) {
        dvmThrowException("Ljava/lang/IllegalArgumentException;",
            "not a primitive field");
        RETURN_VOID();
    }

    /* get a pointer to the field's data; performs access checks */
    fieldPtr = getFieldDataAddr(obj, declaringClass, slot, false,noAccessCheck);
    if (fieldPtr == NULL)
        RETURN_VOID();

    /* copy 4 or 8 bytes out */
    if (fieldType->primitiveType == PRIM_LONG ||
        fieldType->primitiveType == PRIM_DOUBLE)
    {
        value.j = fieldPtr->j;
    } else {
        value.i = fieldPtr->i;
    }

    /* retrieve value, performing a widening conversion if necessary */
    if (dvmConvertPrimitiveValue(fieldType->primitiveType, targetType,
        &(value.i), &(pResult->i)) < 0)
    {
        dvmThrowException("Ljava/lang/IllegalArgumentException;",
            "invalid primitive conversion");
        RETURN_VOID();
    }
}

/*
 * Primitive field setters, e.g.:
 * private void setIField(Object o, Class declaringClass,
 *     Class type, int slot, boolean noAccessCheck, int type_no, int value)
 *
 * The "type_no" is defined by the java.lang.reflect.Field class.
 */
static void Dalvik_java_lang_reflect_Field_setPrimitiveField(const u4* args,
    JValue* pResult)
{
    // ignore thisPtr in args[0]
    Object* obj = (Object*) args[1];
    ClassObject* declaringClass = (ClassObject*) args[2];
    ClassObject* fieldType = (ClassObject*) args[3];
    int slot = args[4];
    bool noAccessCheck = (args[5] != 0);
    int typeNum = args[6];
    const s4* valuePtr = (s4*) &args[7];
    PrimitiveType srcType = convPrimType(typeNum);
    JValue* fieldPtr;
    JValue value;

    if (!dvmIsPrimitiveClass(fieldType)) {
        dvmThrowException("Ljava/lang/IllegalArgumentException;",
            "not a primitive field");
        RETURN_VOID();
    }

    /* convert the 32/64-bit arg to a JValue matching the field type */
    if (dvmConvertPrimitiveValue(srcType, fieldType->primitiveType,
        valuePtr, &(value.i)) < 0)
    {
        dvmThrowException("Ljava/lang/IllegalArgumentException;",
            "invalid primitive conversion");
        RETURN_VOID();
    }

    /* get a pointer to the field's data; performs access checks */
    fieldPtr = getFieldDataAddr(obj, declaringClass, slot, true, noAccessCheck);
    if (fieldPtr == NULL)
        RETURN_VOID();

    /* store 4 or 8 bytes */
    if (fieldType->primitiveType == PRIM_LONG ||
        fieldType->primitiveType == PRIM_DOUBLE)
    {
        fieldPtr->j = value.j;
    } else {
        fieldPtr->i = value.i;
    }

    RETURN_VOID();
}

/*
 * public Annotation[] getDeclaredAnnotations(Class declaringClass, int slot)
 *
 * Return the annotations declared for this field.
 */
static void Dalvik_java_lang_reflect_Field_getDeclaredAnnotations(
    const u4* args, JValue* pResult)
{
    // ignore thisPtr in args[0]
    ClassObject* declaringClass = (ClassObject*) args[1];
    int slot = args[2];
    Field* field;

    field = dvmSlotToField(declaringClass, slot);
    assert(field != NULL);

    ArrayObject* annos = dvmGetFieldAnnotations(field);
    dvmReleaseTrackedAlloc((Object*) annos, NULL);
    RETURN_PTR(annos);
}

/*
 * private Object[] getSignatureAnnotation()
 *
 * Returns the signature annotation.
 */
static void Dalvik_java_lang_reflect_Field_getSignatureAnnotation(const u4* args,
    JValue* pResult)
{
    // ignore thisPtr in args[0]
    ClassObject* declaringClass = (ClassObject*) args[1];
    int slot = args[2];
    Field* field;

    field = dvmSlotToField(declaringClass, slot);
    assert(field != NULL);

    ArrayObject* arr = dvmGetFieldSignatureAnnotation(field);
    dvmReleaseTrackedAlloc((Object*) arr, NULL);
    RETURN_PTR(arr);
}

const DalvikNativeMethod dvm_java_lang_reflect_Field[] = {
    { "getFieldModifiers",  "(Ljava/lang/Class;I)I",
        Dalvik_java_lang_reflect_Field_getFieldModifiers },
    { "getField",           "(Ljava/lang/Object;Ljava/lang/Class;Ljava/lang/Class;IZ)Ljava/lang/Object;",
        Dalvik_java_lang_reflect_Field_getField },
    { "getBField",          "(Ljava/lang/Object;Ljava/lang/Class;Ljava/lang/Class;IZI)B",
        Dalvik_java_lang_reflect_Field_getPrimitiveField },
    { "getCField",          "(Ljava/lang/Object;Ljava/lang/Class;Ljava/lang/Class;IZI)C",
        Dalvik_java_lang_reflect_Field_getPrimitiveField },
    { "getDField",          "(Ljava/lang/Object;Ljava/lang/Class;Ljava/lang/Class;IZI)D",
        Dalvik_java_lang_reflect_Field_getPrimitiveField },
    { "getFField",          "(Ljava/lang/Object;Ljava/lang/Class;Ljava/lang/Class;IZI)F",
        Dalvik_java_lang_reflect_Field_getPrimitiveField },
    { "getIField",          "(Ljava/lang/Object;Ljava/lang/Class;Ljava/lang/Class;IZI)I",
        Dalvik_java_lang_reflect_Field_getPrimitiveField },
    { "getJField",          "(Ljava/lang/Object;Ljava/lang/Class;Ljava/lang/Class;IZI)J",
        Dalvik_java_lang_reflect_Field_getPrimitiveField },
    { "getSField",          "(Ljava/lang/Object;Ljava/lang/Class;Ljava/lang/Class;IZI)S",
        Dalvik_java_lang_reflect_Field_getPrimitiveField },
    { "getZField",          "(Ljava/lang/Object;Ljava/lang/Class;Ljava/lang/Class;IZI)Z",
        Dalvik_java_lang_reflect_Field_getPrimitiveField },
    { "setField",           "(Ljava/lang/Object;Ljava/lang/Class;Ljava/lang/Class;IZLjava/lang/Object;)V",
        Dalvik_java_lang_reflect_Field_setField },
    { "setBField",          "(Ljava/lang/Object;Ljava/lang/Class;Ljava/lang/Class;IZIB)V",
        Dalvik_java_lang_reflect_Field_setPrimitiveField },
    { "setCField",          "(Ljava/lang/Object;Ljava/lang/Class;Ljava/lang/Class;IZIC)V",
        Dalvik_java_lang_reflect_Field_setPrimitiveField },
    { "setDField",          "(Ljava/lang/Object;Ljava/lang/Class;Ljava/lang/Class;IZID)V",
        Dalvik_java_lang_reflect_Field_setPrimitiveField },
    { "setFField",          "(Ljava/lang/Object;Ljava/lang/Class;Ljava/lang/Class;IZIF)V",
        Dalvik_java_lang_reflect_Field_setPrimitiveField },
    { "setIField",          "(Ljava/lang/Object;Ljava/lang/Class;Ljava/lang/Class;IZII)V",
        Dalvik_java_lang_reflect_Field_setPrimitiveField },
    { "setJField",          "(Ljava/lang/Object;Ljava/lang/Class;Ljava/lang/Class;IZIJ)V",
        Dalvik_java_lang_reflect_Field_setPrimitiveField },
    { "setSField",          "(Ljava/lang/Object;Ljava/lang/Class;Ljava/lang/Class;IZIS)V",
        Dalvik_java_lang_reflect_Field_setPrimitiveField },
    { "setZField",          "(Ljava/lang/Object;Ljava/lang/Class;Ljava/lang/Class;IZIZ)V",
        Dalvik_java_lang_reflect_Field_setPrimitiveField },
    { "getDeclaredAnnotations", "(Ljava/lang/Class;I)[Ljava/lang/annotation/Annotation;",
        Dalvik_java_lang_reflect_Field_getDeclaredAnnotations },
    { "getSignatureAnnotation",  "(Ljava/lang/Class;I)[Ljava/lang/Object;",
        Dalvik_java_lang_reflect_Field_getSignatureAnnotation },
    { NULL, NULL, NULL },
};
