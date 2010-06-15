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
#include "alloc/HeapBitmap.h"
#include "alloc/HeapSource.h"
#include "alloc/Verify.h"
#include "alloc/Visit.h"

/*
 * Helper routine for verifyRefernce that masks low-tag bits before
 * applying verification checks.  TODO: eliminate the use of low-tag
 * bits and move this code into verifyReference.
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

static void visitorCallback(void *addr, void *arg)
{
    verifyReference(addr);
}

/*
 * Verifies an object reference.  Determines the type of the reference
 * and dispatches to a specialized verification routine.
 */
void dvmVerifyObject(const Object *obj)
{
    LOGV("Entering dvmVerifyObject(obj=%p)", obj);
    dvmVisitObject(visitorCallback, (Object *)obj, NULL);
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
 * Verifies a large heap reference table.  These objects are list
 * heads.  As such, it is valid for table to be NULL.
 */
static void verifyLargeHeapRefTable(const LargeHeapRefTable *table)
{
    for (; table != NULL; table = table->next) {
        verifyReferenceTable(&table->refs);
    }
}

/*
 * Verifies all stack slots. TODO: verify native methods.
 */
static void verifyThreadStack(const Thread *thread)
{
    const StackSaveArea *saveArea;
    const u4 *framePtr;

    assert(thread != NULL);
    framePtr = (const u4 *)thread->curFrame;
    for (; framePtr != NULL; framePtr = saveArea->prevFrame) {
        Method *method;
        saveArea = SAVEAREA_FROM_FP(framePtr);
        method = (Method *)saveArea->method;
        if (method != NULL && !dvmIsNativeMethod(method)) {
            const RegisterMap* pMap = dvmGetExpandedRegisterMap(method);
            const u1* regVector = NULL;
            int i;

            if (pMap != NULL) {
                /* found map, get registers for this address */
                int addr = saveArea->xtra.currentPc - method->insns;
                regVector = dvmRegisterMapGetLine(pMap, addr);
            }
            if (regVector == NULL) {
                /*
                 * Either there was no register map or there is no
                 * info for the current PC.  Perform a conservative
                 * scan.
                 */
                for (i = 0; i < method->registersSize; ++i) {
                    if (dvmIsValidObject((Object *)framePtr[i])) {
                        verifyReference(&framePtr[i]);
                    }
                }
            } else {
                /*
                 * Precise scan.  v0 is at the lowest address on the
                 * interpreted stack, and is the first bit in the
                 * register vector, so we can walk through the
                 * register map and memory in the same direction.
                 *
                 * A '1' bit indicates a live reference.
                 */
                u2 bits = 1 << 1;
                for (i = 0; i < method->registersSize; ++i) {
                    bits >>= 1;
                    if (bits == 1) {
                        /* set bit 9 so we can tell when we're empty */
                        bits = *regVector++ | 0x0100;
                    }
                    if ((bits & 0x1) != 0) {
                        /*
                         * Register is marked as live, it's a valid root.
                         */
                        verifyReference(&framePtr[i]);
                    }
                }
                dvmReleaseRegisterMapLine(pMap, regVector);
            }
        }
        /*
         * Don't fall into an infinite loop if things get corrupted.
         */
        assert((uintptr_t)saveArea->prevFrame > (uintptr_t)framePtr ||
               saveArea->prevFrame == NULL);
    }
}

/*
 * Verifies all roots associated with a thread.
 */
static void verifyThread(const Thread *thread)
{
    assert(thread != NULL);
    assert(thread->status != THREAD_RUNNING ||
           thread->isSuspended ||
           thread == dvmThreadSelf());
    LOGV("Entering verifyThread(thread=%p)", thread);
    verifyReference(&thread->threadObj);
    verifyReference(&thread->exception);
    verifyReferenceTable(&thread->internalLocalRefTable);
    verifyReferenceTable(&thread->jniLocalRefTable);
    if (thread->jniMonitorRefTable.table) {
        verifyReferenceTable(&thread->jniMonitorRefTable);
    }
    verifyThreadStack(thread);
    LOGV("Exiting verifyThread(thread=%p)", thread);
}

/*
 * Verifies all threads on the thread list.
 */
static void verifyThreads(void)
{
    Thread *thread;

    dvmLockThreadList(dvmThreadSelf());
    thread = gDvm.threadList;
    while (thread) {
        verifyThread(thread);
        thread = thread->next;
    }
    dvmUnlockThreadList();
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
    verifyLargeHeapRefTable(gDvm.gcHeap->referenceOperations);
    verifyLargeHeapRefTable(gDvm.gcHeap->pendingFinalizationRefs);
    verifyThreads();
    /* TODO: verify cached global references. */
}
