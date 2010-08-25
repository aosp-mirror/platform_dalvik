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
#include "alloc/clz.h"
#include "alloc/HeapInternal.h"
#include "alloc/Visit.h"
#include "alloc/VisitInlines.h"

/*
 * Visits all of the reference locations in an object.
 */
void dvmVisitObject(Visitor *visitor, Object *obj, void *arg)
{
    assert(visitor != NULL);
    assert(obj != NULL);
    assert(obj->clazz != NULL);
    visitObject(visitor, obj, arg);
}

/*
 * Applies a verification function to all present values in the hash table.
 */
static void visitHashTable(Visitor *visitor, HashTable *table, void *arg)
{
    int i;

    assert(visitor != NULL);
    assert(table != NULL);
    dvmHashTableLock(table);
    for (i = 0; i < table->tableSize; ++i) {
        HashEntry *entry = &table->pEntries[i];
        if (entry->data != NULL && entry->data != HASH_TOMBSTONE) {
            (*visitor)(&entry->data, arg);
        }
    }
    dvmHashTableUnlock(table);
}

/*
 * Visits all entries in the reference table.
 */
static void visitReferenceTable(Visitor *visitor, const ReferenceTable *table,
                                void *arg)
{
    Object **entry;

    assert(visitor != NULL);
    assert(table != NULL);
    for (entry = table->table; entry < table->nextEntry; ++entry) {
        assert(entry != NULL);
        (*visitor)(entry, arg);
    }
}

/*
 * Visits a large heap reference table.  These objects are list heads.
 * As such, it is valid for table to be NULL.
 */
static void visitLargeHeapRefTable(Visitor *visitor, LargeHeapRefTable *table,
                                   void *arg)
{
    assert(visitor != NULL);
    for (; table != NULL; table = table->next) {
        visitReferenceTable(visitor, &table->refs, arg);
    }
}

/*
 * Visits all stack slots. TODO: visit native methods.
 */
static void visitThreadStack(Visitor *visitor, Thread *thread, void *arg)
{
    const StackSaveArea *saveArea;
    u4 *framePtr;

    assert(visitor != NULL);
    assert(thread != NULL);
    framePtr = (u4 *)thread->curFrame;
    for (; framePtr != NULL; framePtr = saveArea->prevFrame) {
        Method *method;
        saveArea = SAVEAREA_FROM_FP(framePtr);
        method = (Method *)saveArea->method;
        if (method != NULL && !dvmIsNativeMethod(method)) {
            const RegisterMap* pMap = dvmGetExpandedRegisterMap(method);
            const u1* regVector = NULL;
            size_t i;

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
                        (*visitor)(&framePtr[i], arg);
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
                        (*visitor)(&framePtr[i], arg);
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
 * Visits all roots associated with a thread.
 */
static void visitThread(Visitor *visitor, Thread *thread, void *arg)
{
    assert(visitor != NULL);
    assert(thread != NULL);
    (*visitor)(&thread->threadObj, arg);
    (*visitor)(&thread->exception, arg);
    visitReferenceTable(visitor, &thread->internalLocalRefTable, arg);
    visitReferenceTable(visitor, &thread->jniLocalRefTable, arg);
    if (thread->jniMonitorRefTable.table) {
        visitReferenceTable(visitor, &thread->jniMonitorRefTable, arg);
    }
    visitThreadStack(visitor, thread, arg);
}

/*
 * Visits all threads on the thread list.
 */
static void visitThreads(Visitor *visitor, void *arg)
{
    Thread *thread;

    assert(visitor != NULL);
    dvmLockThreadList(dvmThreadSelf());
    thread = gDvm.threadList;
    while (thread) {
        visitThread(visitor, thread, arg);
        thread = thread->next;
    }
    dvmUnlockThreadList();
}

/*
 * Visits roots.  TODO: visit all roots.
 */
void dvmVisitRoots(Visitor *visitor, void *arg)
{
    assert(visitor != NULL);
    visitHashTable(visitor, gDvm.loadedClasses, arg);
    visitHashTable(visitor, gDvm.dbgRegistry, arg);
    visitHashTable(visitor, gDvm.internedStrings, arg);
    visitHashTable(visitor, gDvm.literalStrings, arg);
    visitReferenceTable(visitor, &gDvm.jniGlobalRefTable, arg);
    visitReferenceTable(visitor, &gDvm.jniPinRefTable, arg);
    visitLargeHeapRefTable(visitor, gDvm.gcHeap->referenceOperations, arg);
    visitLargeHeapRefTable(visitor, gDvm.gcHeap->pendingFinalizationRefs, arg);
    visitThreads(visitor, arg);
    (*visitor)(&gDvm.outOfMemoryObj, arg);
    (*visitor)(&gDvm.internalErrorObj, arg);
    (*visitor)(&gDvm.noClassDefFoundErrorObj, arg);
    /* TODO: visit cached global references. */
}
