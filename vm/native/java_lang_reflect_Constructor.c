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
 * java.lang.reflect.Constructor
 */
#include "Dalvik.h"
#include "native/InternalNativePriv.h"


/*
 * public int getConstructorModifiers(Class declaringClass, int slot)
 */
static void Dalvik_java_lang_reflect_Constructor_getConstructorModifiers(
    const u4* args, JValue* pResult)
{
    // ignore thisPtr in args[0]
    ClassObject* declaringClass = (ClassObject*) args[1];
    int slot = args[2];
    Method* meth;

    meth = dvmSlotToMethod(declaringClass, slot);
    RETURN_INT(dvmFixMethodFlags(meth->accessFlags));
}

/*
 * public int constructNative(Object[] args, Class declaringClass,
 *     Class[] parameterTypes, int slot, boolean noAccessCheck)
 *
 * We get here through Constructor.newInstance().  The Constructor object
 * would not be available if the constructor weren't public (per the
 * definition of Class.getConstructor), so we can skip the method access
 * check.  We can also safely assume the constructor isn't associated
 * with an interface, array, or primitive class.
 */
static void Dalvik_java_lang_reflect_Constructor_constructNative(
    const u4* args, JValue* pResult)
{
    // ignore thisPtr in args[0]
    ArrayObject* argList = (ArrayObject*) args[1];
    ClassObject* declaringClass = (ClassObject*) args[2];
    ArrayObject* params = (ArrayObject*) args[3];
    int slot = args[4];
    bool noAccessCheck = (args[5] != 0);
    Object* newObj;
    Method* meth;

    if (dvmIsAbstractClass(declaringClass)) {
        dvmThrowExceptionWithClassMessage("Ljava/lang/InstantiationException;",
            declaringClass->descriptor);
        RETURN_VOID();
    }

    /* initialize the class if it hasn't been already */
    if (!dvmIsClassInitialized(declaringClass)) {
        if (!dvmInitClass(declaringClass)) {
            LOGW("Class init failed in Constructor.constructNative (%s)\n",
                declaringClass->descriptor);
            assert(dvmCheckException(dvmThreadSelf()));
            RETURN_VOID();
        }
    }

    newObj = dvmAllocObject(declaringClass, ALLOC_DEFAULT);
    if (newObj == NULL)
        RETURN_PTR(NULL);

    meth = dvmSlotToMethod(declaringClass, slot);
    assert(meth != NULL);

    (void) dvmInvokeMethod(newObj, meth, argList, params, NULL, noAccessCheck);
    dvmReleaseTrackedAlloc(newObj, NULL);
    RETURN_PTR(newObj);
}

/*
 * public Annotation[] getDeclaredAnnotations(Class declaringClass, int slot)
 *
 * Return the annotations declared for this constructor.
 */
static void Dalvik_java_lang_reflect_Constructor_getDeclaredAnnotations(
    const u4* args, JValue* pResult)
{
    // ignore thisPtr in args[0]
    ClassObject* declaringClass = (ClassObject*) args[1];
    int slot = args[2];
    Method* meth;

    meth = dvmSlotToMethod(declaringClass, slot);
    assert(meth != NULL);

    ArrayObject* annos = dvmGetMethodAnnotations(meth);
    dvmReleaseTrackedAlloc((Object*)annos, NULL);
    RETURN_PTR(annos);
}

/*
 * public Annotation[][] getParameterAnnotations(Class declaringClass, int slot)
 *
 * Return the annotations declared for this constructor's parameters.
 */
static void Dalvik_java_lang_reflect_Constructor_getParameterAnnotations(
    const u4* args, JValue* pResult)
{
    // ignore thisPtr in args[0]
    ClassObject* declaringClass = (ClassObject*) args[1];
    int slot = args[2];
    Method* meth;

    meth = dvmSlotToMethod(declaringClass, slot);
    assert(meth != NULL);

    ArrayObject* annos = dvmGetParameterAnnotations(meth);
    dvmReleaseTrackedAlloc((Object*)annos, NULL);
    RETURN_PTR(annos);
}

/*
 * private Object[] getSignatureAnnotation()
 *
 * Returns the signature annotation.
 */
static void Dalvik_java_lang_reflect_Constructor_getSignatureAnnotation(
    const u4* args, JValue* pResult)
{
    // ignore thisPtr in args[0]
    ClassObject* declaringClass = (ClassObject*) args[1];
    int slot = args[2];
    Method* meth;

    meth = dvmSlotToMethod(declaringClass, slot);
    assert(meth != NULL);

    ArrayObject* arr = dvmGetMethodSignatureAnnotation(meth);
    dvmReleaseTrackedAlloc((Object*) arr, NULL);
    RETURN_PTR(arr);
}

const DalvikNativeMethod dvm_java_lang_reflect_Constructor[] = {
    { "constructNative",    "([Ljava/lang/Object;Ljava/lang/Class;[Ljava/lang/Class;IZ)Ljava/lang/Object;",
        Dalvik_java_lang_reflect_Constructor_constructNative },
    { "getConstructorModifiers", "(Ljava/lang/Class;I)I",
        Dalvik_java_lang_reflect_Constructor_getConstructorModifiers },
    { "getDeclaredAnnotations", "(Ljava/lang/Class;I)[Ljava/lang/annotation/Annotation;",
        Dalvik_java_lang_reflect_Constructor_getDeclaredAnnotations },
    { "getParameterAnnotations", "(Ljava/lang/Class;I)[[Ljava/lang/annotation/Annotation;",
        Dalvik_java_lang_reflect_Constructor_getParameterAnnotations },
    { "getSignatureAnnotation",  "(Ljava/lang/Class;I)[Ljava/lang/Object;",
        Dalvik_java_lang_reflect_Constructor_getSignatureAnnotation },
    { NULL, NULL, NULL },
};
