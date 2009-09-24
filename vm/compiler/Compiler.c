/*
 * Copyright (C) 2009 The Android Open Source Project
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

#include <sys/mman.h>
#include <errno.h>

#include "Dalvik.h"
#include "interp/Jit.h"
#include "CompilerInternals.h"

static inline bool workQueueLength(void)
{
    return gDvmJit.compilerQueueLength;
}

static CompilerWorkOrder workDequeue(void)
{
    assert(gDvmJit.compilerWorkQueue[gDvmJit.compilerWorkDequeueIndex].kind
           != kWorkOrderInvalid);
    CompilerWorkOrder work =
        gDvmJit.compilerWorkQueue[gDvmJit.compilerWorkDequeueIndex];
    gDvmJit.compilerWorkQueue[gDvmJit.compilerWorkDequeueIndex++].kind =
        kWorkOrderInvalid;
    if (gDvmJit.compilerWorkDequeueIndex == COMPILER_WORK_QUEUE_SIZE) {
        gDvmJit.compilerWorkDequeueIndex = 0;
    }
    gDvmJit.compilerQueueLength--;

    /* Remember the high water mark of the queue length */
    if (gDvmJit.compilerQueueLength > gDvmJit.compilerMaxQueued)
        gDvmJit.compilerMaxQueued = gDvmJit.compilerQueueLength;

    return work;
}

bool dvmCompilerWorkEnqueue(const u2 *pc, WorkOrderKind kind, void* info)
{
    int cc;
    int i;
    int numWork;

    dvmLockMutex(&gDvmJit.compilerLock);

    /* Queue full */
    if (gDvmJit.compilerQueueLength == COMPILER_WORK_QUEUE_SIZE ||
        gDvmJit.codeCacheFull == true) {
        dvmUnlockMutex(&gDvmJit.compilerLock);
        return false;
    }

    for (numWork = gDvmJit.compilerQueueLength,
           i = gDvmJit.compilerWorkDequeueIndex;
         numWork > 0;
         numWork--) {
        /* Already enqueued */
        if (gDvmJit.compilerWorkQueue[i++].pc == pc)
            goto done;
        /* Wrap around */
        if (i == COMPILER_WORK_QUEUE_SIZE)
            i = 0;
    }

    CompilerWorkOrder *newOrder =
        &gDvmJit.compilerWorkQueue[gDvmJit.compilerWorkEnqueueIndex];
    newOrder->pc = pc;
    newOrder->kind = kind;
    newOrder->info = info;
    newOrder->result.codeAddress = NULL;
    newOrder->result.discardResult =
        (kind == kWorkOrderTraceDebug) ? true : false;
    gDvmJit.compilerWorkEnqueueIndex++;
    if (gDvmJit.compilerWorkEnqueueIndex == COMPILER_WORK_QUEUE_SIZE)
        gDvmJit.compilerWorkEnqueueIndex = 0;
    gDvmJit.compilerQueueLength++;
    cc = pthread_cond_signal(&gDvmJit.compilerQueueActivity);
    assert(cc == 0);

done:
    dvmUnlockMutex(&gDvmJit.compilerLock);
    return true;
}

/* Block until queue length is 0 */
void dvmCompilerDrainQueue(void)
{
    dvmLockMutex(&gDvmJit.compilerLock);
    while (workQueueLength() != 0 && !gDvmJit.haltCompilerThread) {
        pthread_cond_wait(&gDvmJit.compilerQueueEmpty, &gDvmJit.compilerLock);
    }
    dvmUnlockMutex(&gDvmJit.compilerLock);
}

static void *compilerThreadStart(void *arg)
{
    dvmChangeStatus(NULL, THREAD_VMWAIT);

    dvmLockMutex(&gDvmJit.compilerLock);
    /*
     * Since the compiler thread will not touch any objects on the heap once
     * being created, we just fake its state as VMWAIT so that it can be a
     * bit late when there is suspend request pending.
     */
    while (!gDvmJit.haltCompilerThread) {
        if (workQueueLength() == 0) {
            int cc;
            cc = pthread_cond_signal(&gDvmJit.compilerQueueEmpty);
            assert(cc == 0);
            pthread_cond_wait(&gDvmJit.compilerQueueActivity,
                              &gDvmJit.compilerLock);
            continue;
        } else {
            do {
                CompilerWorkOrder work = workDequeue();
                dvmUnlockMutex(&gDvmJit.compilerLock);
                /* Check whether there is a suspend request on me */
                dvmCheckSuspendPending(NULL);
                /* Is JitTable filling up? */
                if (gDvmJit.jitTableEntriesUsed >
                    (gDvmJit.jitTableSize - gDvmJit.jitTableSize/4)) {
                    dvmJitResizeJitTable(gDvmJit.jitTableSize * 2);
                }
                if (gDvmJit.haltCompilerThread) {
                    LOGD("Compiler shutdown in progress - discarding request");
                } else {
                    /* Compilation is successful */
                    if (dvmCompilerDoWork(&work)) {
                        dvmJitSetCodeAddr(work.pc, work.result.codeAddress,
                                          work.result.instructionSet);
                    }
                }
                free(work.info);
                dvmLockMutex(&gDvmJit.compilerLock);
            } while (workQueueLength() != 0);
        }
    }
    pthread_cond_signal(&gDvmJit.compilerQueueEmpty);
    dvmUnlockMutex(&gDvmJit.compilerLock);

    /*
     * As part of detaching the thread we need to call into Java code to update
     * the ThreadGroup, and we should not be in VMWAIT state while executing
     * interpreted code.
     */
    dvmChangeStatus(NULL, THREAD_RUNNING);

    LOGD("Compiler thread shutting down\n");
    return NULL;
}

bool dvmCompilerSetupCodeCache(void)
{
    extern void dvmCompilerTemplateStart(void);
    extern void dmvCompilerTemplateEnd(void);

    /* Allocate the code cache */
    gDvmJit.codeCache = mmap(0, CODE_CACHE_SIZE,
                          PROT_READ | PROT_WRITE | PROT_EXEC,
                          MAP_PRIVATE | MAP_ANON, -1, 0);
    if (gDvmJit.codeCache == MAP_FAILED) {
        LOGE("Failed to create the code cache: %s\n", strerror(errno));
        return false;
    }

    /* Copy the template code into the beginning of the code cache */
    int templateSize = (intptr_t) dmvCompilerTemplateEnd -
                       (intptr_t) dvmCompilerTemplateStart;
    memcpy((void *) gDvmJit.codeCache,
           (void *) dvmCompilerTemplateStart,
           templateSize);

    gDvmJit.templateSize = templateSize;
    gDvmJit.codeCacheByteUsed = templateSize;

    /* Only flush the part in the code cache that is being used now */
    cacheflush((intptr_t) gDvmJit.codeCache,
               (intptr_t) gDvmJit.codeCache + templateSize, 0);
    return true;
}

bool dvmCompilerStartup(void)
{
    /* Make sure the BBType enum is in sane state */
    assert(kChainingCellNormal == 0);

    /* Architecture-specific chores to initialize */
    if (!dvmCompilerArchInit())
        goto fail;

    /*
     * Setup the code cache if it is not done so already. For apps it should be
     * done by the Zygote already, but for command-line dalvikvm invocation we
     * need to do it here.
     */
    if (gDvmJit.codeCache == NULL) {
        if (!dvmCompilerSetupCodeCache())
            goto fail;
    }

    /* Allocate the initial arena block */
    if (dvmCompilerHeapInit() == false) {
        goto fail;
    }

    dvmInitMutex(&gDvmJit.compilerLock);
    pthread_cond_init(&gDvmJit.compilerQueueActivity, NULL);
    pthread_cond_init(&gDvmJit.compilerQueueEmpty, NULL);

    dvmLockMutex(&gDvmJit.compilerLock);

    gDvmJit.haltCompilerThread = false;

    /* Reset the work queue */
    memset(gDvmJit.compilerWorkQueue, 0,
           sizeof(CompilerWorkOrder) * COMPILER_WORK_QUEUE_SIZE);
    gDvmJit.compilerWorkEnqueueIndex = gDvmJit.compilerWorkDequeueIndex = 0;
    gDvmJit.compilerQueueLength = 0;
    gDvmJit.compilerHighWater =
        COMPILER_WORK_QUEUE_SIZE - (COMPILER_WORK_QUEUE_SIZE/4);

    assert(gDvmJit.compilerHighWater < COMPILER_WORK_QUEUE_SIZE);
    if (!dvmCreateInternalThread(&gDvmJit.compilerHandle, "Compiler",
                                 compilerThreadStart, NULL)) {
        dvmUnlockMutex(&gDvmJit.compilerLock);
        goto fail;
    }

    /* Track method-level compilation statistics */
    gDvmJit.methodStatsTable =  dvmHashTableCreate(32, NULL);

    dvmUnlockMutex(&gDvmJit.compilerLock);

    return true;

fail:
    return false;
}

void dvmCompilerShutdown(void)
{
    void *threadReturn;

    if (gDvmJit.compilerHandle) {

        gDvmJit.haltCompilerThread = true;

        dvmLockMutex(&gDvmJit.compilerLock);
        pthread_cond_signal(&gDvmJit.compilerQueueActivity);
        dvmUnlockMutex(&gDvmJit.compilerLock);

        if (pthread_join(gDvmJit.compilerHandle, &threadReturn) != 0)
            LOGW("Compiler thread join failed\n");
        else
            LOGD("Compiler thread has shut down\n");
    }
}
