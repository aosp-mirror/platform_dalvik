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
#include <cutils/ashmem.h>

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
    if (gDvmJit.compilerQueueLength == 0) {
        int cc = pthread_cond_signal(&gDvmJit.compilerQueueEmpty);
    }

    /* Remember the high water mark of the queue length */
    if (gDvmJit.compilerQueueLength > gDvmJit.compilerMaxQueued)
        gDvmJit.compilerMaxQueued = gDvmJit.compilerQueueLength;

    return work;
}

/*
 * Attempt to enqueue a work order, returning true if successful.
 * This routine will not block, but simply return if it couldn't
 * aquire the lock or if the queue is full.
 *
 * NOTE: Make sure that the caller frees the info pointer if the return value
 * is false.
 */
bool dvmCompilerWorkEnqueue(const u2 *pc, WorkOrderKind kind, void* info)
{
    int cc;
    int i;
    int numWork;
    bool result = true;

    if (dvmTryLockMutex(&gDvmJit.compilerLock)) {
        return false;  // Couldn't acquire the lock
    }

    /*
     * Return if queue or code cache is full.
     */
    if (gDvmJit.compilerQueueLength == COMPILER_WORK_QUEUE_SIZE ||
        gDvmJit.codeCacheFull == true) {
        result = false;
        goto unlockAndExit;
    }

    for (numWork = gDvmJit.compilerQueueLength,
           i = gDvmJit.compilerWorkDequeueIndex;
         numWork > 0;
         numWork--) {
        /* Already enqueued */
        if (gDvmJit.compilerWorkQueue[i++].pc == pc)
            goto unlockAndExit;
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
    newOrder->result.requestingThread = dvmThreadSelf();

    gDvmJit.compilerWorkEnqueueIndex++;
    if (gDvmJit.compilerWorkEnqueueIndex == COMPILER_WORK_QUEUE_SIZE)
        gDvmJit.compilerWorkEnqueueIndex = 0;
    gDvmJit.compilerQueueLength++;
    cc = pthread_cond_signal(&gDvmJit.compilerQueueActivity);
    assert(cc == 0);

unlockAndExit:
    dvmUnlockMutex(&gDvmJit.compilerLock);
    return result;
}

/* Block until queue length is 0 */
void dvmCompilerDrainQueue(void)
{
    int oldStatus = dvmChangeStatus(NULL, THREAD_VMWAIT);
    dvmLockMutex(&gDvmJit.compilerLock);
    while (workQueueLength() != 0 && !gDvmJit.haltCompilerThread) {
        pthread_cond_wait(&gDvmJit.compilerQueueEmpty, &gDvmJit.compilerLock);
    }
    dvmUnlockMutex(&gDvmJit.compilerLock);
    dvmChangeStatus(NULL, oldStatus);
}

bool dvmCompilerSetupCodeCache(void)
{
    extern void dvmCompilerTemplateStart(void);
    extern void dmvCompilerTemplateEnd(void);
    int fd;

    /* Allocate the code cache */
    fd = ashmem_create_region("dalvik-jit-code-cache", gDvmJit.codeCacheSize);
    if (fd < 0) {
        LOGE("Could not create %u-byte ashmem region for the JIT code cache",
             gDvmJit.codeCacheSize);
        return false;
    }
    gDvmJit.codeCache = mmap(NULL, gDvmJit.codeCacheSize,
                             PROT_READ | PROT_WRITE | PROT_EXEC,
                             MAP_PRIVATE , fd, 0);
    close(fd);
    if (gDvmJit.codeCache == MAP_FAILED) {
        LOGE("Failed to mmap the JIT code cache: %s\n", strerror(errno));
        return false;
    }

    /* This can be found through "dalvik-jit-code-cache" in /proc/<pid>/maps */
    // LOGD("Code cache starts at %p", gDvmJit.codeCache);

    /* Copy the template code into the beginning of the code cache */
    int templateSize = (intptr_t) dmvCompilerTemplateEnd -
                       (intptr_t) dvmCompilerTemplateStart;
    memcpy((void *) gDvmJit.codeCache,
           (void *) dvmCompilerTemplateStart,
           templateSize);

    /*
     * Work around a CPU bug by keeping the 32-bit ARM handler code in its own
     * page.
     */
    if (dvmCompilerInstructionSet() == DALVIK_JIT_THUMB2) {
        templateSize = (templateSize + 4095) & ~4095;
    }

    gDvmJit.templateSize = templateSize;
    gDvmJit.codeCacheByteUsed = templateSize;

    /* Only flush the part in the code cache that is being used now */
    cacheflush((intptr_t) gDvmJit.codeCache,
               (intptr_t) gDvmJit.codeCache + templateSize, 0);
    return true;
}

static void crawlDalvikStack(Thread *thread, bool print)
{
    void *fp = thread->curFrame;
    StackSaveArea* saveArea = NULL;
    int stackLevel = 0;

    if (print) {
        LOGD("Crawling tid %d (%s / %p %s)", thread->systemTid,
             dvmGetThreadStatusStr(thread->status),
             thread->inJitCodeCache,
             thread->inJitCodeCache ? "jit" : "interp");
    }
    /* Crawl the Dalvik stack frames to clear the returnAddr field */
    while (fp != NULL) {
        saveArea = SAVEAREA_FROM_FP(fp);

        if (print) {
            if (dvmIsBreakFrame(fp)) {
                LOGD("  #%d: break frame (%p)",
                     stackLevel, saveArea->returnAddr);
            }
            else {
                LOGD("  #%d: %s.%s%s (%p)",
                     stackLevel,
                     saveArea->method->clazz->descriptor,
                     saveArea->method->name,
                     dvmIsNativeMethod(saveArea->method) ?
                         " (native)" : "",
                     saveArea->returnAddr);
            }
        }
        stackLevel++;
        saveArea->returnAddr = NULL;
        assert(fp != saveArea->prevFrame);
        fp = saveArea->prevFrame;
    }
    /* Make sure the stack is fully unwound to the bottom */
    assert(saveArea == NULL ||
           (u1 *) (saveArea+1) == thread->interpStackStart);
}

static void resetCodeCache(void)
{
    Thread* thread;
    u8 startTime = dvmGetRelativeTimeUsec();
    int inJit = 0;
    int byteUsed = gDvmJit.codeCacheByteUsed;

    /* If any thread is found stuck in the JIT state, don't reset the cache */
    for (thread = gDvm.threadList; thread != NULL; thread = thread->next) {
        /*
         * Crawl the stack to wipe out the returnAddr field so that
         * 1) the soon-to-be-deleted code in the JIT cache won't be used
         * 2) or the thread stuck in the JIT land will soon return
         *    to the interpreter land
         */
        crawlDalvikStack(thread, false);
        if (thread->inJitCodeCache) {
            inJit++;
        }
    }

    if (inJit) {
        LOGD("JIT code cache reset delayed (%d bytes %d/%d)",
             gDvmJit.codeCacheByteUsed, gDvmJit.numCodeCacheReset,
             ++gDvmJit.numCodeCacheResetDelayed);
        return;
    }

    /* Lock the mutex to clean up the work queue */
    dvmLockMutex(&gDvmJit.compilerLock);

    /* Drain the work queue to free the work orders */
    while (workQueueLength()) {
        CompilerWorkOrder work = workDequeue();
        free(work.info);
    }

    /* Reset the JitEntry table contents to the initial unpopulated state */
    dvmJitResetTable();

    /*
     * Wipe out the code cache content to force immediate crashes if
     * stale JIT'ed code is invoked.
     */
    memset((char *) gDvmJit.codeCache + gDvmJit.templateSize,
           0,
           gDvmJit.codeCacheByteUsed - gDvmJit.templateSize);
    cacheflush((intptr_t) gDvmJit.codeCache,
               (intptr_t) gDvmJit.codeCache + gDvmJit.codeCacheByteUsed, 0);

    /* Reset the current mark of used bytes to the end of template code */
    gDvmJit.codeCacheByteUsed = gDvmJit.templateSize;
    gDvmJit.numCompilations = 0;

    /* Reset the work queue */
    memset(gDvmJit.compilerWorkQueue, 0,
           sizeof(CompilerWorkOrder) * COMPILER_WORK_QUEUE_SIZE);
    gDvmJit.compilerWorkEnqueueIndex = gDvmJit.compilerWorkDequeueIndex = 0;
    gDvmJit.compilerQueueLength = 0;

    /* Reset the IC patch work queue */
    dvmLockMutex(&gDvmJit.compilerICPatchLock);
    gDvmJit.compilerICPatchIndex = 0;
    dvmUnlockMutex(&gDvmJit.compilerICPatchLock);

    /* All clear now */
    gDvmJit.codeCacheFull = false;

    dvmUnlockMutex(&gDvmJit.compilerLock);

    LOGD("JIT code cache reset in %lld ms (%d bytes %d/%d)",
         (dvmGetRelativeTimeUsec() - startTime) / 1000,
         byteUsed, ++gDvmJit.numCodeCacheReset,
         gDvmJit.numCodeCacheResetDelayed);
}

/*
 * Perform actions that are only safe when all threads are suspended. Currently
 * we do:
 * 1) Check if the code cache is full. If so reset it and restart populating it
 *    from scratch.
 * 2) Patch predicted chaining cells by consuming recorded work orders.
 */
void dvmCompilerPerformSafePointChecks(void)
{
    if (gDvmJit.codeCacheFull) {
        resetCodeCache();
    }
    dvmCompilerPatchInlineCache();
}

bool compilerThreadStartup(void)
{
    JitEntry *pJitTable = NULL;
    unsigned char *pJitProfTable = NULL;
    unsigned int i;

    if (!dvmCompilerArchInit())
        goto fail;

    /*
     * Setup the code cache if we have not inherited a valid code cache
     * from the zygote.
     */
    if (gDvmJit.codeCache == NULL) {
        if (!dvmCompilerSetupCodeCache())
            goto fail;
    }

    /* Allocate the initial arena block */
    if (dvmCompilerHeapInit() == false) {
        goto fail;
    }

    dvmLockMutex(&gDvmJit.compilerLock);

#if defined(WITH_JIT_TUNING)
    /* Track method-level compilation statistics */
    gDvmJit.methodStatsTable =  dvmHashTableCreate(32, NULL);
#endif

    dvmUnlockMutex(&gDvmJit.compilerLock);

    /* Set up the JitTable */

    /* Power of 2? */
    assert(gDvmJit.jitTableSize &&
           !(gDvmJit.jitTableSize & (gDvmJit.jitTableSize - 1)));

    dvmInitMutex(&gDvmJit.tableLock);
    dvmLockMutex(&gDvmJit.tableLock);
    pJitTable = (JitEntry*)
                calloc(gDvmJit.jitTableSize, sizeof(*pJitTable));
    if (!pJitTable) {
        LOGE("jit table allocation failed\n");
        dvmUnlockMutex(&gDvmJit.tableLock);
        goto fail;
    }
    /*
     * NOTE: the profile table must only be allocated once, globally.
     * Profiling is turned on and off by nulling out gDvm.pJitProfTable
     * and then restoring its original value.  However, this action
     * is not syncronized for speed so threads may continue to hold
     * and update the profile table after profiling has been turned
     * off by null'ng the global pointer.  Be aware.
     */
    pJitProfTable = (unsigned char *)malloc(JIT_PROF_SIZE);
    if (!pJitProfTable) {
        LOGE("jit prof table allocation failed\n");
        dvmUnlockMutex(&gDvmJit.tableLock);
        goto fail;
    }
    memset(pJitProfTable, gDvmJit.threshold, JIT_PROF_SIZE);
    for (i=0; i < gDvmJit.jitTableSize; i++) {
       pJitTable[i].u.info.chain = gDvmJit.jitTableSize;
    }
    /* Is chain field wide enough for termination pattern? */
    assert(pJitTable[0].u.info.chain == gDvmJit.jitTableSize);

    gDvmJit.pJitEntryTable = pJitTable;
    gDvmJit.jitTableMask = gDvmJit.jitTableSize - 1;
    gDvmJit.jitTableEntriesUsed = 0;
    gDvmJit.compilerHighWater =
        COMPILER_WORK_QUEUE_SIZE - (COMPILER_WORK_QUEUE_SIZE/4);
    gDvmJit.pProfTable = pJitProfTable;
    gDvmJit.pProfTableCopy = pJitProfTable;
    dvmUnlockMutex(&gDvmJit.tableLock);

    /* Signal running threads to refresh their cached pJitTable pointers */
    dvmSuspendAllThreads(SUSPEND_FOR_REFRESH);
    dvmResumeAllThreads(SUSPEND_FOR_REFRESH);
    return true;

fail:
    return false;

}

static void *compilerThreadStart(void *arg)
{
    int ret;
    struct timespec ts;

    dvmChangeStatus(NULL, THREAD_VMWAIT);

    /*
     * If we're not running stand-alone, wait a little before
     * recieving translation requests on the assumption that process start
     * up code isn't worth compiling.  We'll resume when the framework
     * signals us that the first screen draw has happened, or the timer
     * below expires (to catch daemons).
     *
     * There is a theoretical race between the callback to
     * VMRuntime.startJitCompiation and when the compiler thread reaches this
     * point. In case the callback happens earlier, in order not to permanently
     * hold the system_server (which is not using the timed wait) in
     * interpreter-only mode we bypass the delay here.
     */
    if (gDvmJit.runningInAndroidFramework &&
        !gDvmJit.alreadyEnabledViaFramework) {
        /*
         * If the current VM instance is the system server (detected by having
         * 0 in gDvm.systemServerPid), we will use the indefinite wait on the
         * conditional variable to determine whether to start the JIT or not.
         * If the system server detects that the whole system is booted in
         * safe mode, the conditional variable will never be signaled and the
         * system server will remain in the interpreter-only mode. All
         * subsequent apps will be started with the --enable-safemode flag
         * explicitly appended.
         */
        if (gDvm.systemServerPid == 0) {
            dvmLockMutex(&gDvmJit.compilerLock);
            pthread_cond_wait(&gDvmJit.compilerQueueActivity,
                              &gDvmJit.compilerLock);
            dvmUnlockMutex(&gDvmJit.compilerLock);
            LOGD("JIT started for system_server");
        } else {
            dvmLockMutex(&gDvmJit.compilerLock);
            /*
             * TUNING: experiment with the delay & perhaps make it
             * target-specific
             */
            dvmRelativeCondWait(&gDvmJit.compilerQueueActivity,
                                 &gDvmJit.compilerLock, 3000, 0);
            dvmUnlockMutex(&gDvmJit.compilerLock);
        }
        if (gDvmJit.haltCompilerThread) {
             return NULL;
        }
    }

    compilerThreadStartup();

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
#if defined(JIT_STATS)
                u8 startTime = dvmGetRelativeTimeUsec();
#endif
                /*
                 * Check whether there is a suspend request on me.  This
                 * is necessary to allow a clean shutdown.
                 */
                dvmCheckSuspendPending(NULL);
                /* Is JitTable filling up? */
                if (gDvmJit.jitTableEntriesUsed >
                    (gDvmJit.jitTableSize - gDvmJit.jitTableSize/4)) {
                    bool resizeFail =
                        dvmJitResizeJitTable(gDvmJit.jitTableSize * 2);
                    /*
                     * If the jit table is full, consider it's time to reset
                     * the code cache too.
                     */
                    gDvmJit.codeCacheFull |= resizeFail;
                }
                if (gDvmJit.haltCompilerThread) {
                    LOGD("Compiler shutdown in progress - discarding request");
                } else if (!gDvmJit.codeCacheFull) {
                    bool compileOK = false;
                    jmp_buf jmpBuf;
                    work.bailPtr = &jmpBuf;
                    bool aborted = setjmp(jmpBuf);
                    if (!aborted) {
                        compileOK = dvmCompilerDoWork(&work);
                    }
                    if (aborted || !compileOK) {
                        dvmCompilerArenaReset();
                        work.result.codeAddress = gDvmJit.interpretTemplate;
                    } else if (!work.result.discardResult) {
                        dvmJitSetCodeAddr(work.pc, work.result.codeAddress,
                                          work.result.instructionSet);
                    }
                }
                free(work.info);
#if defined(JIT_STATS)
                gDvmJit.jitTime += dvmGetRelativeTimeUsec() - startTime;
#endif
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

    if (gDvm.verboseShutdown)
        LOGD("Compiler thread shutting down\n");
    return NULL;
}

bool dvmCompilerStartup(void)
{

    dvmInitMutex(&gDvmJit.compilerLock);
    dvmInitMutex(&gDvmJit.compilerICPatchLock);
    dvmLockMutex(&gDvmJit.compilerLock);
    pthread_cond_init(&gDvmJit.compilerQueueActivity, NULL);
    pthread_cond_init(&gDvmJit.compilerQueueEmpty, NULL);

    /* Reset the work queue */
    gDvmJit.compilerWorkEnqueueIndex = gDvmJit.compilerWorkDequeueIndex = 0;
    gDvmJit.compilerQueueLength = 0;
    dvmUnlockMutex(&gDvmJit.compilerLock);

    /*
     * Defer rest of initialization until we're sure JIT'ng makes sense. Launch
     * the compiler thread, which will do the real initialization if and
     * when it is signalled to do so.
     */
    return dvmCreateInternalThread(&gDvmJit.compilerHandle, "Compiler",
                                   compilerThreadStart, NULL);
}

void dvmCompilerShutdown(void)
{
    void *threadReturn;

    /* Disable new translation requests */
    gDvmJit.pProfTable = NULL;
    gDvmJit.pProfTableCopy = NULL;

    if (gDvm.verboseShutdown) {
        dvmCompilerDumpStats();
        while (gDvmJit.compilerQueueLength)
          sleep(5);
    }

    if (gDvmJit.compilerHandle) {

        gDvmJit.haltCompilerThread = true;

        dvmLockMutex(&gDvmJit.compilerLock);
        pthread_cond_signal(&gDvmJit.compilerQueueActivity);
        dvmUnlockMutex(&gDvmJit.compilerLock);

        if (pthread_join(gDvmJit.compilerHandle, &threadReturn) != 0)
            LOGW("Compiler thread join failed\n");
        else if (gDvm.verboseShutdown)
            LOGD("Compiler thread has shut down\n");
    }

    /* Break loops within the translation cache */
    dvmJitUnchainAll();

    /*
     * NOTE: our current implementatation doesn't allow for the compiler
     * thread to be restarted after it exits here.  We aren't freeing
     * the JitTable or the ProfTable because threads which still may be
     * running or in the process of shutting down may hold references to
     * them.
     */
}

void dvmCompilerStateRefresh()
{
    bool jitActive;
    bool jitActivate;
    bool needUnchain = false;

    dvmLockMutex(&gDvmJit.tableLock);
    jitActive = gDvmJit.pProfTable != NULL;
    jitActivate = !(gDvm.debuggerActive || (gDvm.activeProfilers > 0));

    if (jitActivate && !jitActive) {
        gDvmJit.pProfTable = gDvmJit.pProfTableCopy;
    } else if (!jitActivate && jitActive) {
        gDvmJit.pProfTable = NULL;
        needUnchain = true;
    }
    dvmUnlockMutex(&gDvmJit.tableLock);
    if (needUnchain)
        dvmJitUnchainAll();
}
