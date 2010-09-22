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
 * Array objects.
 */
#include "Dalvik.h"

#include <stdlib.h>
#include <stddef.h>

#if WITH_HPROF && WITH_HPROF_STACK
#include "hprof/Hprof.h"
#endif

static ClassObject* createArrayClass(const char* descriptor, Object* loader);
static ClassObject* createPrimitiveClass(int idx);

static const char gPrimLetter[] = PRIM_TYPE_TO_LETTER;

/*
 * Allocate space for a new array object.  This is the lowest-level array
 * allocation function.
 *
 * Pass in the array class and the width of each element.
 *
 * On failure, returns NULL with an exception raised.
 */
ArrayObject* dvmAllocArray(ClassObject* arrayClass, size_t length,
    size_t elemWidth, int allocFlags)
{
    ArrayObject* newArray;
    size_t size;

    assert(arrayClass->descriptor[0] == '[');

    if (length > 0x0fffffff) {
        /* too large and (length * elemWidth) will overflow 32 bits */
        LOGE("Rejecting allocation of %u-element array\n", length);
        dvmThrowBadAllocException("array size too large");
        return NULL;
    }

    size = offsetof(ArrayObject, contents);
    size += length * elemWidth;

    /* Note that we assume that the Array class does not
     * override finalize().
     */
    newArray = dvmMalloc(size, allocFlags);
    if (newArray != NULL) {
        DVM_OBJECT_INIT(&newArray->obj, arrayClass);
        newArray->length = length;
        LOGVV("AllocArray: %s [%d] (%d)\n",
            arrayClass->descriptor, (int) length, (int) size);
#if WITH_HPROF && WITH_HPROF_STACK
        hprofFillInStackTrace(&newArray->obj);
#endif
        dvmTrackAllocation(arrayClass, size);
    }
    /* the caller must call dvmReleaseTrackedAlloc */
    return newArray;
}

/*
 * Create a new array, given an array class.  The class may represent an
 * array of references or primitives.
 */
ArrayObject* dvmAllocArrayByClass(ClassObject* arrayClass,
    size_t length, int allocFlags)
{
    const char* descriptor = arrayClass->descriptor;

    assert(descriptor[0] == '[');       /* must be array class */
    if (descriptor[1] != '[' && descriptor[1] != 'L') {
        /* primitive array */
        assert(descriptor[2] == '\0');
        return dvmAllocPrimitiveArray(descriptor[1], length, allocFlags);
    } else {
        return dvmAllocArray(arrayClass, length, kObjectArrayRefWidth,
            allocFlags);
    }
}

/*
 * Find the array class for "elemClassObj", which could itself be an
 * array class.
 */
ClassObject* dvmFindArrayClassForElement(ClassObject* elemClassObj)
{
    ClassObject* arrayClass;

    assert(elemClassObj != NULL);

    /* Simply prepend "[" to the descriptor. */
    int nameLen = strlen(elemClassObj->descriptor);
    char className[nameLen + 2];

    className[0] = '[';
    memcpy(className+1, elemClassObj->descriptor, nameLen+1);
    arrayClass = dvmFindArrayClass(className, elemClassObj->classLoader);

    return arrayClass;
}

/*
 * Create a new array that holds references to members of the specified class.
 *
 * "elemClassObj" is the element type, and may itself be an array class.  It
 * may not be a primitive class.
 *
 * "allocFlags" determines whether the new object will be added to the
 * "tracked alloc" table.
 *
 * This is less efficient than dvmAllocArray(), but occasionally convenient.
 */
ArrayObject* dvmAllocObjectArray(ClassObject* elemClassObj, size_t length,
    int allocFlags)
{
    ClassObject* arrayClass;
    ArrayObject* newArray = NULL;

    LOGVV("dvmAllocObjectArray: '%s' len=%d\n",
        elemClassObj->descriptor, (int)length);

    arrayClass = dvmFindArrayClassForElement(elemClassObj);
    if (arrayClass != NULL) {
        newArray = dvmAllocArray(arrayClass, length, kObjectArrayRefWidth,
            allocFlags);
    }

    /* the caller must call dvmReleaseTrackedAlloc */
    return newArray;
}

/*
 * Create a new array that holds primitive types.
 *
 * "type" is the primitive type letter, e.g. 'I' for int or 'J' for long.
 * If the array class doesn't exist, it will be created.
 */
ArrayObject* dvmAllocPrimitiveArray(char type, size_t length, int allocFlags)
{
    ArrayObject* newArray;
    ClassObject** pTypeClass;
    int width;

    switch (type) {
    case 'I':
        pTypeClass = &gDvm.classArrayInt;
        width = 4;
        break;
    case 'C':
        pTypeClass = &gDvm.classArrayChar;
        width = 2;
        break;
    case 'B':
        pTypeClass = &gDvm.classArrayByte;
        width = 1;
        break;
    case 'Z':
        pTypeClass = &gDvm.classArrayBoolean;
        width = 1; /* special-case this? */
        break;
    case 'F':
        pTypeClass = &gDvm.classArrayFloat;
        width = 4;
        break;
    case 'D':
        pTypeClass = &gDvm.classArrayDouble;
        width = 8;
        break;
    case 'S':
        pTypeClass = &gDvm.classArrayShort;
        width = 2;
        break;
    case 'J':
        pTypeClass = &gDvm.classArrayLong;
        width = 8;
        break;
    default:
        LOGE("Unknown type '%c'\n", type);
        assert(false);
        return NULL;
    }

    if (*pTypeClass == NULL) {
        char typeClassName[3] = "[x";

        typeClassName[1] = type;

        *pTypeClass = dvmFindArrayClass(typeClassName, NULL);
        if (*pTypeClass == NULL) {
            LOGE("ERROR: failed to generate array class for '%s'\n",
                typeClassName);
            return NULL;
        }
    }

    newArray = dvmAllocArray(*pTypeClass, length, width, allocFlags);

    /* the caller must dvmReleaseTrackedAlloc if allocFlags==ALLOC_DEFAULT */
    return newArray;
}

/*
 * Recursively create an array with multiple dimensions.  Elements may be
 * Objects or primitive types.
 *
 * The dimension we're creating is in dimensions[0], so when we recurse
 * we advance the pointer.
 */
ArrayObject* dvmAllocMultiArray(ClassObject* arrayClass, int curDim,
    const int* dimensions)
{
    ArrayObject* newArray;
    const char* elemName = arrayClass->descriptor + 1; // Advance past one '['.

    LOGVV("dvmAllocMultiArray: class='%s' curDim=%d *dimensions=%d\n",
        arrayClass->descriptor, curDim, *dimensions);

    if (curDim == 0) {
        if (*elemName == 'L' || *elemName == '[') {
            LOGVV("  end: array class (obj) is '%s'\n",
                arrayClass->descriptor);
            newArray = dvmAllocArray(arrayClass, *dimensions,
                        kObjectArrayRefWidth, ALLOC_DEFAULT);
        } else {
            LOGVV("  end: array class (prim) is '%s'\n",
                arrayClass->descriptor);
            newArray = dvmAllocPrimitiveArray(
                    gPrimLetter[arrayClass->elementClass->primitiveType],
                    *dimensions, ALLOC_DEFAULT);
        }
    } else {
        ClassObject* subArrayClass;
        int i;

        /* if we have X[][], find X[] */
        subArrayClass = dvmFindArrayClass(elemName, arrayClass->classLoader);
        if (subArrayClass == NULL) {
            /* not enough '['s on the initial class? */
            assert(dvmCheckException(dvmThreadSelf()));
            return NULL;
        }
        assert(dvmIsArrayClass(subArrayClass));

        /* allocate the array that holds the sub-arrays */
        newArray = dvmAllocArray(arrayClass, *dimensions, kObjectArrayRefWidth,
                        ALLOC_DEFAULT);
        if (newArray == NULL) {
            assert(dvmCheckException(dvmThreadSelf()));
            return NULL;
        }

        /*
         * Create a new sub-array in every element of the array.
         */
        for (i = 0; i < *dimensions; i++) {
          ArrayObject* newSubArray;
          newSubArray = dvmAllocMultiArray(subArrayClass, curDim-1,
                          dimensions+1);
            if (newSubArray == NULL) {
                dvmReleaseTrackedAlloc((Object*) newArray, NULL);
                assert(dvmCheckException(dvmThreadSelf()));
                return NULL;
            }
            dvmSetObjectArrayElement(newArray, i, (Object *)newSubArray);
            dvmReleaseTrackedAlloc((Object*) newSubArray, NULL);
        }
    }

    /* caller must call dvmReleaseTrackedAlloc */
    return newArray;
}


/*
 * Find an array class, by name (e.g. "[I").
 *
 * If the array class doesn't exist, we generate it.
 *
 * If the element class doesn't exist, we return NULL (no exception raised).
 */
ClassObject* dvmFindArrayClass(const char* descriptor, Object* loader)
{
    ClassObject* clazz;

    assert(descriptor[0] == '[');
    //LOGV("dvmFindArrayClass: '%s' %p\n", descriptor, loader);

    clazz = dvmLookupClass(descriptor, loader, false);
    if (clazz == NULL) {
        LOGV("Array class '%s' %p not found; creating\n", descriptor, loader);
        clazz = createArrayClass(descriptor, loader);
        if (clazz != NULL)
            dvmAddInitiatingLoader(clazz, loader);
    }

    return clazz;
}

/*
 * Create an array class (i.e. the class object for the array, not the
 * array itself).  "descriptor" looks like "[C" or "[Ljava/lang/String;".
 *
 * If "descriptor" refers to an array of primitives, look up the
 * primitive type's internally-generated class object.
 *
 * "loader" is the class loader of the class that's referring to us.  It's
 * used to ensure that we're looking for the element type in the right
 * context.  It does NOT become the class loader for the array class; that
 * always comes from the base element class.
 *
 * Returns NULL with an exception raised on failure.
 */
static ClassObject* createArrayClass(const char* descriptor, Object* loader)
{
    ClassObject* newClass = NULL;
    ClassObject* elementClass = NULL;
    int arrayDim;
    u4 extraFlags;

    assert(descriptor[0] == '[');
    assert(gDvm.classJavaLangClass != NULL);
    assert(gDvm.classJavaLangObject != NULL);

    /*
     * Identify the underlying element class and the array dimension depth.
     */
    extraFlags = CLASS_ISARRAY;
    if (descriptor[1] == '[') {
        /* array of arrays; keep descriptor and grab stuff from parent */
        ClassObject* outer;

        outer = dvmFindClassNoInit(&descriptor[1], loader);
        if (outer != NULL) {
            /* want the base class, not "outer", in our elementClass */
            elementClass = outer->elementClass;
            arrayDim = outer->arrayDim + 1;
            extraFlags |= CLASS_ISOBJECTARRAY;
        } else {
            assert(elementClass == NULL);     /* make sure we fail */
        }
    } else {
        arrayDim = 1;
        if (descriptor[1] == 'L') {
            /* array of objects; strip off "[" and look up descriptor. */
            const char* subDescriptor = &descriptor[1];
            LOGVV("searching for element class '%s'\n", subDescriptor);
            elementClass = dvmFindClassNoInit(subDescriptor, loader);
            extraFlags |= CLASS_ISOBJECTARRAY;
        } else {
            /* array of a primitive type */
            elementClass = dvmFindPrimitiveClass(descriptor[1]);
        }
    }

    if (elementClass == NULL) {
        /* failed */
        assert(dvmCheckException(dvmThreadSelf()));
        dvmFreeClassInnards(newClass);
        dvmReleaseTrackedAlloc((Object*) newClass, NULL);
        return NULL;
    }

    /*
     * See if it's already loaded.  Array classes are always associated
     * with the class loader of their underlying element type -- an array
     * of Strings goes with the loader for java/lang/String -- so we need
     * to look for it there.  (The caller should have checked for the
     * existence of the class before calling here, but they did so with
     * *their* class loader, not the element class' loader.)
     *
     * If we find it, the caller adds "loader" to the class' initiating
     * loader list, which should prevent us from going through this again.
     *
     * This call is unnecessary if "loader" and "elementClass->classLoader"
     * are the same, because our caller (dvmFindArrayClass) just did the
     * lookup.  (Even if we get this wrong we still have correct behavior,
     * because we effectively do this lookup again when we add the new
     * class to the hash table -- necessary because of possible races with
     * other threads.)
     */
    if (loader != elementClass->classLoader) {
        LOGVV("--- checking for '%s' in %p vs. elem %p\n",
            descriptor, loader, elementClass->classLoader);
        newClass = dvmLookupClass(descriptor, elementClass->classLoader, false);
        if (newClass != NULL) {
            LOGV("--- we already have %s in %p, don't need in %p\n",
                descriptor, elementClass->classLoader, loader);
            return newClass;
        }
    }


    /*
     * Fill out the fields in the ClassObject.
     *
     * It is possible to execute some methods against arrays, because all
     * arrays are instances of Object, so we need to set up a vtable.  We
     * can just point at the one in Object.
     *
     * Array classes are simple enough that we don't need to do a full
     * link step.
     */
    newClass = (ClassObject*) dvmMalloc(sizeof(*newClass), ALLOC_DEFAULT);
    if (newClass == NULL)
        return NULL;
    DVM_OBJECT_INIT(&newClass->obj, gDvm.classJavaLangClass);
    dvmSetClassSerialNumber(newClass);
    newClass->descriptorAlloc = strdup(descriptor);
    newClass->descriptor = newClass->descriptorAlloc;
    dvmSetFieldObject((Object *)newClass,
                      offsetof(ClassObject, super),
                      (Object *)gDvm.classJavaLangObject);
    newClass->vtableCount = gDvm.classJavaLangObject->vtableCount;
    newClass->vtable = gDvm.classJavaLangObject->vtable;
    newClass->primitiveType = PRIM_NOT;
    dvmSetFieldObject((Object *)newClass,
                      offsetof(ClassObject, elementClass),
                      (Object *)elementClass);
    dvmSetFieldObject((Object *)newClass,
                      offsetof(ClassObject, classLoader),
                      (Object *)elementClass->classLoader);
    newClass->arrayDim = arrayDim;
    newClass->status = CLASS_INITIALIZED;
#if WITH_HPROF && WITH_HPROF_STACK
    hprofFillInStackTrace(newClass);
#endif

    /* don't need to set newClass->objectSize */

    /*
     * All arrays have java/lang/Cloneable and java/io/Serializable as
     * interfaces.  We need to set that up here, so that stuff like
     * "instanceof" works right.
     *
     * Note: The GC could run during the call to dvmFindSystemClassNoInit(),
     * so we need to make sure the class object is GC-valid while we're in
     * there.  Do this by clearing the interface list so the GC will just
     * think that the entries are null.
     *
     * TODO?
     * We may want to cache these two classes to avoid the lookup, though
     * it's not vital -- we only do it when creating an array class, not
     * every time we create an array.  Better yet, create a single, global
     * copy of "interfaces" and "iftable" somewhere near the start and
     * just point to those (and remember not to free them for arrays).
     */
    newClass->interfaceCount = 2;
    newClass->interfaces = (ClassObject**)dvmLinearAlloc(newClass->classLoader,
                                sizeof(ClassObject*) * 2);
    memset(newClass->interfaces, 0, sizeof(ClassObject*) * 2);
    newClass->interfaces[0] =
        dvmFindSystemClassNoInit("Ljava/lang/Cloneable;");
    newClass->interfaces[1] =
        dvmFindSystemClassNoInit("Ljava/io/Serializable;");
    dvmLinearReadOnly(newClass->classLoader, newClass->interfaces);
    if (newClass->interfaces[0] == NULL || newClass->interfaces[1] == NULL) {
        LOGE("Unable to create array class '%s': missing interfaces\n",
            descriptor);
        dvmFreeClassInnards(newClass);
        dvmThrowException("Ljava/lang/InternalError;", "missing array ifaces");
        dvmReleaseTrackedAlloc((Object*) newClass, NULL);
        return NULL;
    }
    /*
     * We assume that Cloneable/Serializable don't have superinterfaces --
     * normally we'd have to crawl up and explicitly list all of the
     * supers as well.  These interfaces don't have any methods, so we
     * don't have to worry about the ifviPool either.
     */
    newClass->iftableCount = 2;
    newClass->iftable = (InterfaceEntry*) dvmLinearAlloc(newClass->classLoader,
                                sizeof(InterfaceEntry) * 2);
    memset(newClass->iftable, 0, sizeof(InterfaceEntry) * 2);
    newClass->iftable[0].clazz = newClass->interfaces[0];
    newClass->iftable[1].clazz = newClass->interfaces[1];
    dvmLinearReadOnly(newClass->classLoader, newClass->iftable);

    /*
     * Inherit access flags from the element.  Arrays can't be used as a
     * superclass or interface, so we want to add "final" and remove
     * "interface".
     *
     * Don't inherit any non-standard flags (e.g., CLASS_FINALIZABLE)
     * from elementClass.  We assume that the array class does not
     * override finalize().
     */
    newClass->accessFlags = ((newClass->elementClass->accessFlags &
                             ~ACC_INTERFACE) | ACC_FINAL) & JAVA_FLAGS_MASK;

    /* Set the flags we determined above.
     * This must happen after accessFlags is set.
     */
    SET_CLASS_FLAG(newClass, extraFlags);

    if (!dvmAddClassToHash(newClass)) {
        /*
         * Another thread must have loaded the class after we
         * started but before we finished.  Discard what we've
         * done and leave some hints for the GC.
         */
        LOGI("WOW: somebody generated %s simultaneously\n",
            newClass->descriptor);

        /* Clean up the class before letting the
         * GC get its hands on it.
         */
        dvmFreeClassInnards(newClass);

        /* Let the GC free the class.
         */
        dvmReleaseTrackedAlloc((Object*) newClass, NULL);

        /* Grab the winning class.
         */
        newClass = dvmLookupClass(descriptor, elementClass->classLoader, false);
        assert(newClass != NULL);
        return newClass;
    }
    dvmReleaseTrackedAlloc((Object*) newClass, NULL);

    LOGV("Created array class '%s' %p (access=0x%04x.%04x)\n",
        descriptor, newClass->classLoader,
        newClass->accessFlags >> 16,
        newClass->accessFlags & JAVA_FLAGS_MASK);

    return newClass;
}

/*
 * Get a class we generated for the primitive types.
 *
 * These correspond to e.g. Integer.TYPE, and are used as the element
 * class in arrays of primitives.
 *
 * "type" should be 'I', 'J', 'Z', etc.
 *
 * Returns NULL if the type doesn't correspond to a known primitive type.
 */
ClassObject* dvmFindPrimitiveClass(char type)
{
    int idx;

    switch (type) {
    case 'Z':
        idx = PRIM_BOOLEAN;
        break;
    case 'C':
        idx = PRIM_CHAR;
        break;
    case 'F':
        idx = PRIM_FLOAT;
        break;
    case 'D':
        idx = PRIM_DOUBLE;
        break;
    case 'B':
        idx = PRIM_BYTE;
        break;
    case 'S':
        idx = PRIM_SHORT;
        break;
    case 'I':
        idx = PRIM_INT;
        break;
    case 'J':
        idx = PRIM_LONG;
        break;
    case 'V':
        idx = PRIM_VOID;
        break;
    default:
        LOGW("Unknown primitive type '%c'\n", type);
        return NULL;
    }

    /*
     * Create the primitive class if it hasn't already been, and add it
     * to the table.
     */
    if (gDvm.primitiveClass[idx] == NULL) {
        ClassObject* primClass = createPrimitiveClass(idx);
        dvmReleaseTrackedAlloc((Object*) primClass, NULL);

        if (android_atomic_release_cas(0, (int) primClass,
                (int*) &gDvm.primitiveClass[idx]) != 0)
        {
            /*
             * Looks like somebody beat us to it.  Free up the one we
             * just created and use the other one.
             */
            dvmFreeClassInnards(primClass);
        }
    }

    return gDvm.primitiveClass[idx];
}

/*
 * Synthesize a primitive class.
 *
 * Just creates the class and returns it (does not add it to the class list).
 */
static ClassObject* createPrimitiveClass(int idx)
{
    ClassObject* newClass;
    static const char* kClassDescriptors[PRIM_MAX] = {
        "Z", "C", "F", "D", "B", "S", "I", "J", "V"
    };

    assert(gDvm.classJavaLangClass != NULL);
    assert(idx >= 0 && idx < PRIM_MAX);

    /*
     * Fill out a few fields in the ClassObject.
     *
     * Note that primitive classes do not sub-class java/lang/Object.  This
     * matters for "instanceof" checks.  Also, we assume that the primitive
     * class does not override finalize().
     */
    newClass = (ClassObject*) dvmMalloc(sizeof(*newClass), ALLOC_DEFAULT);
    if (newClass == NULL)
        return NULL;
    DVM_OBJECT_INIT(&newClass->obj, gDvm.classJavaLangClass);
    dvmSetClassSerialNumber(newClass);
    newClass->accessFlags = ACC_PUBLIC | ACC_FINAL | ACC_ABSTRACT;
    newClass->primitiveType = idx;
    newClass->descriptorAlloc = NULL;
    newClass->descriptor = kClassDescriptors[idx];
    //newClass->super = gDvm.classJavaLangObject;
    newClass->status = CLASS_INITIALIZED;
#if WITH_HPROF && WITH_HPROF_STACK
    hprofFillInStackTrace(newClass);
#endif

    /* don't need to set newClass->objectSize */

    LOGVV("Created primitive class '%s'\n", kClassDescriptors[idx]);

    return newClass;
}

/*
 * Copy the entire contents of one array of objects to another.  If the copy
 * is impossible because of a type clash, we fail and return "false".
 */
bool dvmCopyObjectArray(ArrayObject* dstArray, const ArrayObject* srcArray,
    ClassObject* dstElemClass)
{
    Object** src = (Object**)srcArray->contents;
    u4 length, count;

    assert(srcArray->length == dstArray->length);
    assert(dstArray->obj.clazz->elementClass == dstElemClass ||
        (dstArray->obj.clazz->elementClass == dstElemClass->elementClass &&
         dstArray->obj.clazz->arrayDim == dstElemClass->arrayDim+1));

    length = dstArray->length;
    for (count = 0; count < length; count++) {
        if (!dvmInstanceof(src[count]->clazz, dstElemClass)) {
            LOGW("dvmCopyObjectArray: can't store %s in %s\n",
                src[count]->clazz->descriptor, dstElemClass->descriptor);
            return false;
        }
        dvmSetObjectArrayElement(dstArray, count, src[count]);
    }

    return true;
}

/*
 * Copy the entire contents of an array of boxed primitives into an
 * array of primitives.  The boxed value must fit in the primitive (i.e.
 * narrowing conversions are not allowed).
 */
bool dvmUnboxObjectArray(ArrayObject* dstArray, const ArrayObject* srcArray,
    ClassObject* dstElemClass)
{
    Object** src = (Object**)srcArray->contents;
    void* dst = (void*)dstArray->contents;
    u4 count = dstArray->length;
    PrimitiveType typeIndex = dstElemClass->primitiveType;

    assert(typeIndex != PRIM_NOT);
    assert(srcArray->length == dstArray->length);

    while (count--) {
        JValue result;

        /*
         * This will perform widening conversions as appropriate.  It
         * might make sense to be more restrictive and require that the
         * primitive type exactly matches the box class, but it's not
         * necessary for correctness.
         */
        if (!dvmUnwrapPrimitive(*src, dstElemClass, &result)) {
            LOGW("dvmCopyObjectArray: can't store %s in %s\n",
                (*src)->clazz->descriptor, dstElemClass->descriptor);
            return false;
        }

        /* would be faster with 4 loops, but speed not crucial here */
        switch (typeIndex) {
        case PRIM_BOOLEAN:
        case PRIM_BYTE:
            {
                u1* tmp = dst;
                *tmp++ = result.b;
                dst = tmp;
            }
            break;
        case PRIM_CHAR:
        case PRIM_SHORT:
            {
                u2* tmp = dst;
                *tmp++ = result.s;
                dst = tmp;
            }
            break;
        case PRIM_FLOAT:
        case PRIM_INT:
            {
                u4* tmp = dst;
                *tmp++ = result.i;
                dst = tmp;
            }
            break;
        case PRIM_DOUBLE:
        case PRIM_LONG:
            {
                u8* tmp = dst;
                *tmp++ = result.j;
                dst = tmp;
            }
            break;
        default:
            /* should not be possible to get here */
            dvmAbort();
        }

        src++;
    }

    return true;
}

/*
 * Returns the width, in bytes, required by elements in instances of
 * the array class.
 */
size_t dvmArrayClassElementWidth(const ClassObject* arrayClass)
{
    const char *descriptor;

    assert(dvmIsArrayClass(arrayClass));

    if (dvmIsObjectArrayClass(arrayClass)) {
        return sizeof(Object *);
    } else {
        descriptor = arrayClass->descriptor;
        switch (descriptor[1]) {
        case 'B': return 1;  /* byte */
        case 'C': return 2;  /* char */
        case 'D': return 8;  /* double */
        case 'F': return 4;  /* float */
        case 'I': return 4;  /* int */
        case 'J': return 8;  /* long */
        case 'S': return 2;  /* short */
        case 'Z': return 1;  /* boolean */
        }
    }
    LOGE("class %p has an unhandled descriptor '%s'", arrayClass, descriptor);
    dvmDumpThread(dvmThreadSelf(), false);
    dvmAbort();
    return 0;  /* Quiet the compiler. */
}

size_t dvmArrayObjectSize(const ArrayObject *array)
{
    size_t size;

    assert(array != NULL);
    size = offsetof(ArrayObject, contents);
    size += array->length * dvmArrayClassElementWidth(array->obj.clazz);
    return size;
}

/*
 * Add all primitive classes to the root set of objects.
TODO: do these belong to the root class loader?
 */
void dvmGcScanPrimitiveClasses()
{
    int i;

    for (i = 0; i < PRIM_MAX; i++) {
        dvmMarkObject((Object *)gDvm.primitiveClass[i]);    // may be NULL
    }
}
