/*
 * Copyright (C) 2010 The Android Open Source Project
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

#include "Dalvik.h"
#include "alloc/HeapSource.h"
#include "alloc/Verify.h"
#include "alloc/HeapBitmap.h"

/*
 * Helper routine for verifyRefernce that masks low-tag bits before
 * applying verification checks.  TODO: eliminate the use of low-tag
 * bits and move this code into verfiyReference.
 */
static void verifyReferenceUnmask(const void *addr, uintptr_t mask)
{
    const Object *obj;
    uintptr_t tmp;
    bool isValid;

    tmp = (uintptr_t)*(const Object **)addr;
    obj = (const Object *)(tmp & ~mask);
    if (obj == NULL) {
        isValid = true;
    } else {
        isValid = dvmIsValidObject(obj);
    }
    if (!isValid) {
        LOGE("Verify of object %p @ %p failed", obj, addr);
        dvmAbort();
    }
}

/*
 * Assertion that the given reference points to a valid object.
 */
static void verifyReference(const void *addr)
{
    verifyReferenceUnmask(addr, 0);
}

/*
 * Verifies instance fields.
 */
static void verifyInstanceFields(const Object *obj)
{
    ClassObject *clazz;
    int i;

    assert(obj != NULL);
    assert(obj->clazz != NULL);
    LOGV("Entering verifyInstanceFields(obj=%p)", obj);
    /* TODO(cshapiro): check reference offsets bitmap for agreement. */
    for (clazz = obj->clazz; clazz != NULL; clazz = clazz->super) {
        InstField *field = clazz->ifields;
        for (i = 0; i < clazz->ifieldRefCount; ++i, ++field) {
            void *addr = BYTE_OFFSET((Object *)obj, field->byteOffset);
            verifyReference(&((JValue *)addr)->l);
        }
    }
    LOGV("Exiting verifyInstanceFields(obj=%p)", obj);
}

/*
 * Verifies the header, static field references, and interface
 * pointers of a class object.
 */
static void verifyClassObject(const ClassObject *obj)
{
    int i;

    LOGV("Entering verifyClassObject(obj=%p)", obj);
    if (obj == gDvm.unlinkedJavaLangClass) {
        assert(obj->obj.clazz == NULL);
        goto exit;
    }
    verifyReference(&obj->obj.clazz);
    assert(!strcmp(obj->obj.clazz->descriptor, "Ljava/lang/Class;"));
    if (IS_CLASS_FLAG_SET(obj, CLASS_ISARRAY)) {
        verifyReference(&obj->elementClass);
    }
    verifyReference(&obj->super);
    verifyReference(&obj->classLoader);
    /* Verify static field references. */
    for (i = 0; i < obj->sfieldCount; ++i) {
        char ch = obj->sfields[i].field.signature[0];
        if (ch == '[' || ch == 'L') {
            verifyReference(&obj->sfields[i].value.l);
        }
    }
    /* Verify the instance fields. */
    verifyInstanceFields((const Object *)obj);
    /* Verify interface references. */
    for (i = 0; i < obj->interfaceCount; ++i) {
        verifyReference(&obj->interfaces[i]);
    }
exit:
    LOGV("Exiting verifyClassObject(obj=%p)", obj);
}

/*
 * Verifies the header of all array objects.  If the array object is
 * specialized to a reference type, verifies the array data as well.
 */
static void verifyArrayObject(const ArrayObject *array)
{
    size_t i;

    LOGV("Entering verifyArrayObject(array=%p)", array);
    /* Verify the class object reference. */
    assert(array->obj.clazz != NULL);
    verifyReference(&array->obj.clazz);
    if (IS_CLASS_FLAG_SET(array->obj.clazz, CLASS_ISOBJECTARRAY)) {
        /* Verify the array contents. */
        Object **contents = (Object **)array->contents;
        for (i = 0; i < array->length; ++i) {
            verifyReference(&contents[i]);
        }
    }
    LOGV("Exiting verifyArrayObject(array=%p)", array);
}

/*
 * Verifies the header and field references of a data object.
 */
static void verifyDataObject(const DataObject *obj)
{
    LOGV("Entering verifyDataObject(obj=%p)", obj);
    /* Verify the class object. */
    assert(obj->obj.clazz != NULL);
    verifyReference(&obj->obj.clazz);
    /* Verify the instance fields. */
    verifyInstanceFields((const Object *)obj);
    if (IS_CLASS_FLAG_SET(obj->obj.clazz, CLASS_ISREFERENCE)) {
        /* Verify the hidden Reference.referent field. */
        size_t offset = gDvm.offJavaLangRefReference_referent;
        void *addr = BYTE_OFFSET((Object *)obj, offset);
        verifyReference(&((JValue *)addr)->l);
    }
    LOGV("Exiting verifyDataObject(obj=%p)", obj);
}

/*
 * Verifies an object reference.  Determines the type of the reference
 * and dispatches to a specialized verification routine.
 */
void dvmVerifyObject(const Object *obj)
{
    ClassObject *clazz;

    LOGV("Entering dvmVerifyObject(obj=%p)", obj);
    assert(obj != NULL);
    /* Check that the object is aligned. */
    assert(((uintptr_t)obj & 7) == 0);
    clazz = obj->clazz;
    /* Check that the class object is aligned. */
    assert(((uintptr_t)clazz & 7) == 0);
    /* Dispatch a type-specific verification routine. */
    if (clazz == gDvm.classJavaLangClass ||
        obj == (Object *)gDvm.unlinkedJavaLangClass) {
        verifyClassObject((ClassObject *)obj);
    } else {
        assert(clazz != NULL);
        if (IS_CLASS_FLAG_SET(clazz, CLASS_ISARRAY)) {
            verifyArrayObject((ArrayObject *)obj);
        } else {
            verifyDataObject((DataObject *)obj);
        }
    }
    LOGV("Exiting dvmVerifyObject(obj=%p)", obj);
}

/*
 * Helper function to call dvmVerifyObject from a bitmap walker.
 */
static bool verifyBitmapCallback(size_t numPtrs, void **ptrs,
                                 const void *finger, void *arg)
{
    size_t i;

    for (i = 0; i < numPtrs; i++) {
        dvmVerifyObject(*ptrs++);
    }
    return true;
}

/*
 * Verifies the object references in a heap bitmap. Assumes the VM is
 * suspended.
 */
void dvmVerifyBitmap(const HeapBitmap *bitmap)
{
    /* TODO: check that locks are held and the VM is suspended. */
    dvmHeapBitmapWalk(bitmap, verifyBitmapCallback, NULL);
}

/*
 * Applies a verification function to all present values in the hash table.
 */
static void verifyHashTable(HashTable *table,
                            void (*callback)(const void *arg))
{
    int i;

    assert(table != NULL);
    assert(callback != NULL);
    dvmHashTableLock(table);
    for (i = 0; i < table->tableSize; ++i) {
        const HashEntry *entry = &table->pEntries[i];
        if (entry->data != NULL && entry->data != HASH_TOMBSTONE) {
            (*callback)(&entry->data);
        }
    }
    dvmHashTableUnlock(table);
}

/*
 * Applies the verify routine to the given object.
 */
static void verifyStringReference(const void *arg)
{
    assert(arg != NULL);
    verifyReferenceUnmask(arg, 0x1);
}

/*
 * Verifies all entries in the reference table.
 */
static void verifyReferenceTable(const ReferenceTable *table)
{
    Object **entry;

    assert(table != NULL);
    for (entry = table->table; entry < table->nextEntry; ++entry) {
        assert(entry != NULL);
        verifyReference(entry);
    }
}

/*
 * Verifies roots.  TODO: verify all roots.
 */
void dvmVerifyRoots(void)
{
    verifyHashTable(gDvm.loadedClasses, verifyReference);
    verifyHashTable(gDvm.dbgRegistry, verifyReference);
    verifyHashTable(gDvm.internedStrings, verifyStringReference);
    verifyReferenceTable(&gDvm.jniGlobalRefTable);
    verifyReferenceTable(&gDvm.jniPinRefTable);
    verifyReferenceTable(&gDvm.gcHeap->nonCollectableRefs);
    /* TODO: verify cached global references. */
    /* TODO: verify threads and stacks. */
    /* TODO: verify finalizer and reference operation queues. */
}
