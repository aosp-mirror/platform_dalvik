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
static void visitHashTable(RootVisitor *visitor, HashTable *table,
                           RootType type, void *arg)
{
    int i;

    assert(visitor != NULL);
    assert(table != NULL);
    dvmHashTableLock(table);
    for (i = 0; i < table->tableSize; ++i) {
        HashEntry *entry = &table->pEntries[i];
        if (entry->data != NULL && entry->data != HASH_TOMBSTONE) {
            (*visitor)(&entry->data, 0, type, arg);
        }
    }
    dvmHashTableUnlock(table);
}

/*
 * Visits all entries in the reference table.
 */
static void visitReferenceTable(RootVisitor *visitor, ReferenceTable *table,
                                u4 threadId, RootType type, void *arg)
{
    Object **entry;

    assert(visitor != NULL);
    assert(table != NULL);
    for (entry = table->table; entry < table->nextEntry; ++entry) {
        assert(entry != NULL);
        (*visitor)(entry, threadId, type, arg);
    }
}

/*
 * Visits all entries in the indirect reference table.
 */
static void visitIndirectRefTable(RootVisitor *visitor, IndirectRefTable *table,
                                  u4 threadId, RootType type, void *arg)
{
    assert(visitor != NULL);
    assert(table != NULL);
    Object **entry = table->table;
    int numEntries = dvmIndirectRefTableEntries(table);
    int i;
    for (i = 0; i < numEntries; ++i) {
        (*visitor)(&entry[i], threadId, type, arg);
    }
}

/*
 * Visits a large heap reference table.  These objects are list heads.
 * As such, it is valid for table to be NULL.
 */
static void visitLargeHeapRefTable(RootVisitor *visitor,
                                   LargeHeapRefTable *table,
                                   RootType type, void *arg)
{
    assert(visitor != NULL);
    for (; table != NULL; table = table->next) {
        visitReferenceTable(visitor, &table->refs, 0, type, arg);
    }
}

/*
 * Visits all stack slots except those belonging to native method
 * arguments.
 */
static void visitThreadStack(RootVisitor *visitor, Thread *thread, void *arg)
{
    const StackSaveArea *saveArea;
    u4 *fp;
    u4 threadId;

    assert(visitor != NULL);
    assert(thread != NULL);
    threadId = thread->threadId;
    fp = (u4 *)thread->curFrame;
    for (; fp != NULL; fp = (u4 *)saveArea->prevFrame) {
        Method *method;
        saveArea = SAVEAREA_FROM_FP(fp);
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
                    if (dvmIsValidObject((Object *)fp[i])) {
                        (*visitor)(&fp[i], threadId, ROOT_JAVA_FRAME, arg);
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
#if WITH_EXTRA_GC_CHECKS
                        if (fp[i] != 0 && !dvmIsValidObject((Object *)fp[i])) {
                            /* this is very bad */
                            LOGE("PGC: invalid ref in reg %d: %#x",
                                 method->registersSize - 1 - i, fp[i]);
                            LOGE("PGC: %s.%s addr %#x",
                                 method->clazz->descriptor, method->name,
                                 saveArea->xtra.currentPc - method->insns);
                            continue;
                        }
#endif
                        (*visitor)(&fp[i], threadId, ROOT_JAVA_FRAME, arg);
                    }
                }
                dvmReleaseRegisterMapLine(pMap, regVector);
            }
        }
        /*
         * Don't fall into an infinite loop if things get corrupted.
         */
        assert((uintptr_t)saveArea->prevFrame > (uintptr_t)fp ||
               saveArea->prevFrame == NULL);
    }
}

/*
 * Visits all roots associated with a thread.
 */
static void visitThread(RootVisitor *visitor, Thread *thread, void *arg)
{
    u4 threadId;

    assert(visitor != NULL);
    assert(thread != NULL);
    threadId = thread->threadId;
    (*visitor)(&thread->threadObj, threadId, ROOT_THREAD_OBJECT, arg);
    (*visitor)(&thread->exception, threadId, ROOT_NATIVE_STACK, arg);
    visitReferenceTable(visitor, &thread->internalLocalRefTable, threadId, ROOT_NATIVE_STACK, arg);
    visitIndirectRefTable(visitor, &thread->jniLocalRefTable, threadId, ROOT_JNI_LOCAL, arg);
    if (thread->jniMonitorRefTable.table != NULL) {
        visitReferenceTable(visitor, &thread->jniMonitorRefTable, threadId, ROOT_JNI_MONITOR, arg);
    }
    visitThreadStack(visitor, thread, arg);
}

/*
 * Visits all threads on the thread list.
 */
static void visitThreads(RootVisitor *visitor, void *arg)
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

static void visitPrimitiveTypes(RootVisitor *visitor, void *arg)
{
    (*visitor)(&gDvm.typeVoid, 0, ROOT_STICKY_CLASS, arg);
    (*visitor)(&gDvm.typeBoolean, 0, ROOT_STICKY_CLASS, arg);
    (*visitor)(&gDvm.typeByte, 0, ROOT_STICKY_CLASS, arg);
    (*visitor)(&gDvm.typeShort, 0, ROOT_STICKY_CLASS, arg);
    (*visitor)(&gDvm.typeChar, 0, ROOT_STICKY_CLASS, arg);
    (*visitor)(&gDvm.typeInt, 0, ROOT_STICKY_CLASS, arg);
    (*visitor)(&gDvm.typeLong, 0, ROOT_STICKY_CLASS, arg);
    (*visitor)(&gDvm.typeFloat, 0, ROOT_STICKY_CLASS, arg);
    (*visitor)(&gDvm.typeDouble, 0, ROOT_STICKY_CLASS, arg);
}

/*
 * Visits roots.  TODO: visit cached global references.
 */
void dvmVisitRoots(RootVisitor *visitor, void *arg)
{
    assert(visitor != NULL);
    visitHashTable(visitor, gDvm.loadedClasses, ROOT_STICKY_CLASS, arg);
    visitPrimitiveTypes(visitor, arg);
    if (gDvm.dbgRegistry != NULL) {
        visitHashTable(visitor, gDvm.dbgRegistry, ROOT_DEBUGGER, arg);
    }
    if (gDvm.literalStrings != NULL) {
        visitHashTable(visitor, gDvm.literalStrings, ROOT_INTERNED_STRING, arg);
    }
    dvmLockMutex(&gDvm.jniGlobalRefLock);
    visitIndirectRefTable(visitor, &gDvm.jniGlobalRefTable, 0, ROOT_JNI_GLOBAL, arg);
    dvmUnlockMutex(&gDvm.jniGlobalRefLock);
    dvmLockMutex(&gDvm.jniPinRefLock);
    visitReferenceTable(visitor, &gDvm.jniPinRefTable, 0, ROOT_VM_INTERNAL, arg);
    dvmUnlockMutex(&gDvm.jniPinRefLock);
    visitLargeHeapRefTable(visitor, gDvm.gcHeap->referenceOperations, ROOT_REFERENCE_CLEANUP, arg);
    visitThreads(visitor, arg);
    (*visitor)(&gDvm.outOfMemoryObj, 0, ROOT_VM_INTERNAL, arg);
    (*visitor)(&gDvm.internalErrorObj, 0, ROOT_VM_INTERNAL, arg);
    (*visitor)(&gDvm.noClassDefFoundErrorObj, 0, ROOT_VM_INTERNAL, arg);
}
