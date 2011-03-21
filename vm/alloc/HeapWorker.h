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
 * Manage async heap tasks.
 */
#ifndef _DALVIK_ALLOC_HEAP_WORKER
#define _DALVIK_ALLOC_HEAP_WORKER

/*
 * Initialize any HeapWorker state that Heap.c
 * cares about.  This lets the GC start before the
 * HeapWorker thread is initialized.
 */
void dvmInitializeHeapWorkerState(void);

/*
 * Initialization.  Starts/stops the worker thread.
 */
bool dvmHeapWorkerStartup(void);
void dvmHeapWorkerShutdown(void);

/*
 * Tell the worker thread to wake up and do work.
 * If shouldLock is false, the caller must have already
 * acquired gDvm.heapWorkerLock.
 */
void dvmSignalHeapWorker(bool shouldLock);

/*
 * Requests that dvmHeapSourceTrim() be called no sooner
 * than timeoutSec seconds from now.  If timeoutSec
 * is zero, any pending trim is cancelled.
 *
 * Caller must hold heapWorkerLock.
 */
void dvmScheduleHeapSourceTrim(size_t timeoutSec);

/* Make sure that the HeapWorker thread hasn't spent an inordinate
 * amount of time inside interpreted code.
 *
 * Aborts the VM if the thread appears to be wedged.
 *
 * The caller must hold the heapWorkerLock.
 */
void dvmAssertHeapWorkerThreadRunning();

/*
 * Called by the worker thread to get the next object
 * to finalize/enqueue/clear.  Implemented in Heap.c.
 *
 * @param op The operation to perform on the returned object.
 *           Must be non-NULL.
 * @return The object to operate on, or NULL.
 */
Object *dvmGetNextHeapWorkerObject();

#endif /*_DALVIK_ALLOC_HEAP_WORKER*/
