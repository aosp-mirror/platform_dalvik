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
 * java.lang.reflect.Array
 */
#include "Dalvik.h"
#include "native/InternalNativePriv.h"


/*
 * private static Object createObjectArray(Class<?> componentType,
 *     int length) throws NegativeArraySizeException;
 *
 * Create a one-dimensional array of Objects.
 */
static void Dalvik_java_lang_reflect_Array_createObjectArray(const u4* args,
    JValue* pResult)
{
    ClassObject* elementClass = (ClassObject*) args[0];
    int length = args[1];
    ArrayObject* newArray;

    assert(elementClass != NULL);       // tested by caller
    if (length < 0) {
        dvmThrowException("Ljava/lang/NegativeArraySizeException;", NULL);
        RETURN_VOID();
    }

    newArray = dvmAllocObjectArray(elementClass, length, ALLOC_DEFAULT);
    if (newArray == NULL) {
        assert(dvmCheckException(dvmThreadSelf()));
        RETURN_VOID();
    }
    dvmReleaseTrackedAlloc((Object*) newArray, NULL);

    RETURN_PTR(newArray);
}

/*
 * private static Object createMultiArray(Class<?> componentType,
 *     int[] dimensions) throws NegativeArraySizeException;
 *
 * Create a multi-dimensional array of Objects or primitive types.
 *
 * We have to generate the names for X[], X[][], X[][][], and so on.  The
 * easiest way to deal with that is to create the full name once and then
 * subtract pieces off.  Besides, we want to start with the outermost
 * piece and work our way in.
 */
static void Dalvik_java_lang_reflect_Array_createMultiArray(const u4* args,
    JValue* pResult)
{
    static const char kPrimLetter[] = PRIM_TYPE_TO_LETTER;
    ClassObject* elementClass = (ClassObject*) args[0];
    ArrayObject* dimArray = (ArrayObject*) args[1];
    ClassObject* arrayClass;
    ArrayObject* newArray;
    char* acDescriptor;
    int numDim, i;
    int* dimensions;

    LOGV("createMultiArray: '%s' [%d]\n",
        elementClass->descriptor, dimArray->length);

    assert(elementClass != NULL);       // verified by caller

    /*
     * Verify dimensions.
     *
     * The caller is responsible for verifying that "dimArray" is non-null
     * and has a length > 0 and <= 255.
     */
    assert(dimArray != NULL);           // verified by caller
    numDim = dimArray->length;
    assert(numDim > 0 && numDim <= 255);

    dimensions = (int*) dimArray->contents;
    for (i = 0; i < numDim; i++) {
        if (dimensions[i] < 0) {
            dvmThrowException("Ljava/lang/NegativeArraySizeException;", NULL);
            RETURN_VOID();
        }
        LOGVV("DIM %d: %d\n", i, dimensions[i]);
    }

    /*
     * Generate the full name of the array class.
     */
    acDescriptor =
        (char*) malloc(strlen(elementClass->descriptor) + numDim + 1);
    memset(acDescriptor, '[', numDim);

    LOGVV("#### element name = '%s'\n", elementClass->descriptor);
    if (dvmIsPrimitiveClass(elementClass)) {
        assert(elementClass->primitiveType >= 0);
        acDescriptor[numDim] = kPrimLetter[elementClass->primitiveType];
        acDescriptor[numDim+1] = '\0';
    } else {
        strcpy(acDescriptor+numDim, elementClass->descriptor);
    }
    LOGVV("#### array name = '%s'\n", acDescriptor);

    /*
     * Find/generate the array class.
     */
    arrayClass = dvmFindArrayClass(acDescriptor, elementClass->classLoader);
    if (arrayClass == NULL) {
        LOGW("Unable to find or generate array class '%s'\n", acDescriptor);
        assert(dvmCheckException(dvmThreadSelf()));
        free(acDescriptor);
        RETURN_VOID();
    }
    free(acDescriptor);

    /* create the array */
    newArray = dvmAllocMultiArray(arrayClass, numDim-1, dimensions);
    if (newArray == NULL) {
        assert(dvmCheckException(dvmThreadSelf()));
        RETURN_VOID();
    }

    dvmReleaseTrackedAlloc((Object*) newArray, NULL);
    RETURN_PTR(newArray);
}

const DalvikNativeMethod dvm_java_lang_reflect_Array[] = {
    { "createObjectArray",  "(Ljava/lang/Class;I)Ljava/lang/Object;",
        Dalvik_java_lang_reflect_Array_createObjectArray },
    { "createMultiArray",   "(Ljava/lang/Class;[I)Ljava/lang/Object;",
        Dalvik_java_lang_reflect_Array_createMultiArray },
    { NULL, NULL, NULL },
};
