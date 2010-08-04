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
 * Types and macros used internally by the heap.
 */
#ifndef _DALVIK_ALLOC_HEAP_INTERNAL
#define _DALVIK_ALLOC_HEAP_INTERNAL

#include <time.h>  // for struct timespec

#include "HeapTable.h"
#include "MarkSweep.h"

struct GcHeap {
    HeapSource      *heapSource;

    /* List of heap objects that will require finalization when
     * collected.  I.e., instance objects
     *
     *     a) whose class definitions override java.lang.Object.finalize()
     *
     * *** AND ***
     *
     *     b) that have never been finalized.
     *
     * Note that this does not exclude non-garbage objects;  this
     * is not the list of pending finalizations, but of objects that
     * potentially have finalization in their futures.
     */
    LargeHeapRefTable  *finalizableRefs;

    /* The list of objects that need to have finalize() called
     * on themselves.  These references are part of the root set.
     *
     * This table is protected by gDvm.heapWorkerListLock, which must
     * be acquired after the heap lock.
     */
    LargeHeapRefTable  *pendingFinalizationRefs;

    /* Linked lists of subclass instances of java/lang/ref/Reference
     * that we find while recursing.  The "next" pointers are hidden
     * in the objects' <code>int Reference.vmData</code> fields.
     * These lists are cleared and rebuilt each time the GC runs.
     */
    Object         *softReferences;
    Object         *weakReferences;
    Object         *phantomReferences;

    /* The list of Reference objects that need to be cleared and/or
     * enqueued.  The bottom two bits of the object pointers indicate
     * whether they should be cleared and/or enqueued.
     *
     * This table is protected by gDvm.heapWorkerListLock, which must
     * be acquired after the heap lock.
     */
    LargeHeapRefTable  *referenceOperations;

    /* If non-null, the method that the HeapWorker is currently
     * executing.
     */
    Object *heapWorkerCurrentObject;
    Method *heapWorkerCurrentMethod;

    /* If heapWorkerCurrentObject is non-null, this gives the time when
     * HeapWorker started executing that method.  The time value must come
     * from dvmGetRelativeTimeUsec().
     *
     * The "Cpu" entry tracks the per-thread CPU timer (when available).
     */
    u8 heapWorkerInterpStartTime;
    u8 heapWorkerInterpCpuStartTime;

    /* If any fields are non-zero, indicates the next (absolute) time that
     * the HeapWorker thread should call dvmHeapSourceTrim().
     */
    struct timespec heapWorkerNextTrim;

    /* The current state of the mark step.
     * Only valid during a GC.
     */
    GcMarkContext   markContext;

    /* GC's card table */
    u1*             cardTableBase;
    size_t          cardTableLength;

    /* Is the GC running?  Used to avoid recursive calls to GC.
     */
    bool            gcRunning;

    /*
     * Debug control values
     */

    int             ddmHpifWhen;
    int             ddmHpsgWhen;
    int             ddmHpsgWhat;
    int             ddmNhsgWhen;
    int             ddmNhsgWhat;

#if WITH_HPROF
    bool            hprofDumpOnGc;
    const char*     hprofFileName;
    int             hprofFd;
    hprof_context_t *hprofContext;
    int             hprofResult;
    bool            hprofDirectToDdms;
#endif
};

bool dvmLockHeap(void);
void dvmUnlockHeap(void);
void dvmLogGcStats(size_t numFreed, size_t sizeFreed, size_t gcTimeMs);
void dvmLogMadviseStats(size_t madvisedSizes[], size_t arrayLen);

/*
 * Logging helpers
 */

#define HEAP_LOG_TAG      LOG_TAG "-heap"

#if LOG_NDEBUG
#define LOGV_HEAP(...)    ((void)0)
#define LOGD_HEAP(...)    ((void)0)
#else
#define LOGV_HEAP(...)    LOG(LOG_VERBOSE, HEAP_LOG_TAG, __VA_ARGS__)
#define LOGD_HEAP(...)    LOG(LOG_DEBUG, HEAP_LOG_TAG, __VA_ARGS__)
#endif
#define LOGI_HEAP(...)    LOG(LOG_INFO, HEAP_LOG_TAG, __VA_ARGS__)
#define LOGW_HEAP(...)    LOG(LOG_WARN, HEAP_LOG_TAG, __VA_ARGS__)
#define LOGE_HEAP(...)    LOG(LOG_ERROR, HEAP_LOG_TAG, __VA_ARGS__)

#define QUIET_ZYGOTE_GC 1
#if QUIET_ZYGOTE_GC
#undef LOGI_HEAP
#define LOGI_HEAP(...) \
    do { \
        if (!gDvm.zygote) { \
            LOG(LOG_INFO, HEAP_LOG_TAG, __VA_ARGS__); \
        } \
    } while (false)
#endif

#define FRACTIONAL_MB(n)    (n) / (1024 * 1024), \
                            ((((n) % (1024 * 1024)) / 1024) * 1000) / 1024
#define FRACTIONAL_PCT(n,max)    ((n) * 100) / (max), \
                                 (((n) * 1000) / (max)) % 10

#endif  // _DALVIK_ALLOC_HEAP_INTERNAL
