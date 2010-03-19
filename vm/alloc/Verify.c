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

/* comment everything, of course! */
#define VERIFY_REFERENCE(x) do {                                \
        if (!verifyReference((x), &(x))) {                      \
            LOGE("Verify of %p at %p failed", (x), &(x));       \
            dvmAbort();                                         \
        }                                                       \
    } while (0)

/*
 * Verifies that a reference points to an object header.
 */
static bool verifyReference(const void *obj, const void *addr)
{
    if (obj == NULL) {
        return true;
    }
    return dvmIsValidObject(obj);
}

/*
 * Verifies the header, static fields references, and interface
 * pointers of a class object.
 */
static void verifyClassObject(const ClassObject *obj)
{
    int i;
    char ch;

    LOGV("Entering verifyClassObject(obj=%p)", obj);
    if (obj == gDvm.unlinkedJavaLangClass) {
        assert(obj->obj.clazz == NULL);
        goto exit;
    }
    VERIFY_REFERENCE(obj->obj.clazz);
    assert(!strcmp(obj->obj.clazz->descriptor, "Ljava/lang/Class;"));
    if (IS_CLASS_FLAG_SET(obj, CLASS_ISARRAY)) {
        VERIFY_REFERENCE(obj->elementClass);
    }
    VERIFY_REFERENCE(obj->super);
    VERIFY_REFERENCE(obj->classLoader);
    /* Verify static field references. */
    for (i = 0; i < obj->sfieldCount; ++i) {
        ch = obj->sfields[i].field.signature[0];
        if (ch == '[' || ch == 'L') {
            VERIFY_REFERENCE(obj->sfields[i].value.l);
        }
    }
    /* Verify interface references. */
    for (i = 0; i < obj->interfaceCount; ++i) {
        VERIFY_REFERENCE(obj->interfaces[i]);
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
    ClassObject *clazz;
    Object **contents;
    size_t i;

    LOGV("Entering verifyArrayObject(obj=%p)", obj);
    /* Verify the class object reference. */
    assert(array->obj.clazz != NULL);
    VERIFY_REFERENCE(array->obj.clazz);
    if (IS_CLASS_FLAG_SET(array->obj.clazz, CLASS_ISOBJECTARRAY)) {
        /* Verify the array contents. */
        contents = (Object **) array->contents;
        for (i = 0; i < array->length; ++i) {
            VERIFY_REFERENCE(contents[i]);
        }
    }
    LOGV("Exiting verifyArrayObject(obj=%p)", obj);
}

/*
 * Verifies the header and field references of a data object.
 */
static void verifyDataObject(const DataObject *obj)
{
    ClassObject *clazz;
    InstField *field;
    void *addr;
    size_t offset;
    int i, count;

    LOGV("Entering verifyDataObject(obj=%p)", obj);
    /* Verify the class object. */
    assert(obj->obj.clazz != NULL);
    VERIFY_REFERENCE(obj->obj.clazz);
    /* Verify the instance fields. */
    for (clazz = obj->obj.clazz; clazz != NULL; clazz = clazz->super) {
        field = clazz->ifields;
        count = clazz->ifieldRefCount;
        for (i = 0; i < count; ++i, ++field) {
            addr = BYTE_OFFSET((Object *)obj, field->byteOffset);
            VERIFY_REFERENCE(((JValue *)addr)->l);
        }
    }
    if (IS_CLASS_FLAG_SET(obj->obj.clazz, CLASS_ISREFERENCE)) {
        /*
         * Reference.referent is not included in the above loop. See
         * precacheReferenceOffsets in Class.c for details.
         */
        addr = BYTE_OFFSET((Object *)obj,
                           gDvm.offJavaLangRefReference_referent);
        VERIFY_REFERENCE(((JValue *)addr)->l);
    }
    LOGV("Exiting verifyDataObject(obj=%p) %zx", obj, length);
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
    clazz = obj->clazz;
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
 * Verifies the object references in a heap bitmap.  Assumes the heap
 * is locked.
 */
void dvmVerifyBitmapUnlocked(const HeapBitmap *bitmap)
{
    /* TODO: check that locks are held and the VM is suspended. */
    dvmHeapBitmapWalk(bitmap, verifyBitmapCallback, NULL);
}

/*
 * Verifies the object references in a heap bitmap.  Suspends the VM
 * for the duration of verification.
 */
void dvmVerifyBitmap(const HeapBitmap *bitmap)
{
    /* Suspend the VM. */
    dvmSuspendAllThreads(SUSPEND_FOR_VERIFY);
    dvmLockMutex(&gDvm.heapWorkerLock);
    dvmAssertHeapWorkerThreadRunning();
    dvmLockMutex(&gDvm.heapWorkerListLock);

    dvmVerifyBitmapUnlocked(bitmap);

    /* Resume the VM. */
    dvmUnlockMutex(&gDvm.heapWorkerListLock);
    dvmUnlockMutex(&gDvm.heapWorkerLock);
    dvmResumeAllThreads(SUSPEND_FOR_VERIFY);
}
