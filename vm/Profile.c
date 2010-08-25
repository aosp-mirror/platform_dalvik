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
 * Android's method call profiling goodies.
 */
#include "Dalvik.h"

#include <stdlib.h>
#include <stddef.h>
#include <string.h>
#include <sys/time.h>
#include <time.h>
#include <sys/mman.h>
#include <sched.h>
#include <errno.h>
#include <fcntl.h>

#include <cutils/open_memstream.h>

#ifdef HAVE_ANDROID_OS
# define UPDATE_MAGIC_PAGE      1
#endif

/*
 * File format:
 *  header
 *  record 0
 *  record 1
 *  ...
 *
 * Header format:
 *  u4  magic ('SLOW')
 *  u2  version
 *  u2  offset to data
 *  u8  start date/time in usec
 *
 * Record format:
 *  u1  thread ID
 *  u4  method ID | method action
 *  u4  time delta since start, in usec
 *
 * 32 bits of microseconds is 70 minutes.
 *
 * All values are stored in little-endian order.
 */
#define TRACE_REC_SIZE      9
#define TRACE_MAGIC         0x574f4c53
#define TRACE_HEADER_LEN    32

#define FILL_PATTERN        0xeeeeeeee


/*
 * Get the wall-clock date/time, in usec.
 */
static inline u8 getTimeInUsec()
{
    struct timeval tv;

    gettimeofday(&tv, NULL);
    return tv.tv_sec * 1000000LL + tv.tv_usec;
}

/*
 * Get the current time, in microseconds.
 *
 * This can mean one of two things.  In "global clock" mode, we get the
 * same time across all threads.  If we use CLOCK_THREAD_CPUTIME_ID, we
 * get a per-thread CPU usage timer.  The latter is better, but a bit
 * more complicated to implement.
 */
static inline u8 getClock()
{
#if defined(HAVE_POSIX_CLOCKS)
    if (!gDvm.profilerWallClock) {
        struct timespec tm;

        clock_gettime(CLOCK_THREAD_CPUTIME_ID, &tm);
        if (!(tm.tv_nsec >= 0 && tm.tv_nsec < 1*1000*1000*1000)) {
            LOGE("bad nsec: %ld\n", tm.tv_nsec);
            dvmAbort();
        }

        return tm.tv_sec * 1000000LL + tm.tv_nsec / 1000;
    } else
#endif
    {
        struct timeval tv;

        gettimeofday(&tv, NULL);
        return tv.tv_sec * 1000000LL + tv.tv_usec;
    }
}

/*
 * Write little-endian data.
 */
static inline void storeShortLE(u1* buf, u2 val)
{
    *buf++ = (u1) val;
    *buf++ = (u1) (val >> 8);
}
static inline void storeIntLE(u1* buf, u4 val)
{
    *buf++ = (u1) val;
    *buf++ = (u1) (val >> 8);
    *buf++ = (u1) (val >> 16);
    *buf++ = (u1) (val >> 24);
}
static inline void storeLongLE(u1* buf, u8 val)
{
    *buf++ = (u1) val;
    *buf++ = (u1) (val >> 8);
    *buf++ = (u1) (val >> 16);
    *buf++ = (u1) (val >> 24);
    *buf++ = (u1) (val >> 32);
    *buf++ = (u1) (val >> 40);
    *buf++ = (u1) (val >> 48);
    *buf++ = (u1) (val >> 56);
}

/*
 * Boot-time init.
 */
bool dvmProfilingStartup(void)
{
    /*
     * Initialize "dmtrace" method profiling.
     */
    memset(&gDvm.methodTrace, 0, sizeof(gDvm.methodTrace));
    dvmInitMutex(&gDvm.methodTrace.startStopLock);
    pthread_cond_init(&gDvm.methodTrace.threadExitCond, NULL);

    ClassObject* clazz =
        dvmFindClassNoInit("Ldalvik/system/VMDebug;", NULL);
    assert(clazz != NULL);
    gDvm.methodTrace.gcMethod =
        dvmFindDirectMethodByDescriptor(clazz, "startGC", "()V");
    gDvm.methodTrace.classPrepMethod =
        dvmFindDirectMethodByDescriptor(clazz, "startClassPrep", "()V");
    if (gDvm.methodTrace.gcMethod == NULL ||
        gDvm.methodTrace.classPrepMethod == NULL)
    {
        LOGE("Unable to find startGC or startClassPrep\n");
        return false;
    }

    assert(!dvmCheckException(dvmThreadSelf()));

    /*
     * Allocate storage for instruction counters.
     */
    gDvm.executedInstrCounts = (int*) malloc(kNumDalvikInstructions * sizeof(int));
    if (gDvm.executedInstrCounts == NULL)
        return false;
    memset(gDvm.executedInstrCounts, 0, kNumDalvikInstructions * sizeof(int));

#ifdef UPDATE_MAGIC_PAGE
    /*
     * If we're running on the emulator, there's a magic page into which
     * we can put interpreted method information.  This allows interpreted
     * methods to show up in the emulator's code traces.
     *
     * We could key this off of the "ro.kernel.qemu" property, but there's
     * no real harm in doing this on a real device.
     */
    int fd = open("/dev/qemu_trace", O_RDWR);
    if (fd < 0) {
        LOGV("Unable to open /dev/qemu_trace\n");
    } else {
        gDvm.emulatorTracePage = mmap(0, SYSTEM_PAGE_SIZE, PROT_READ|PROT_WRITE,
                                      MAP_SHARED, fd, 0);
        close(fd);
        if (gDvm.emulatorTracePage == MAP_FAILED) {
            LOGE("Unable to mmap /dev/qemu_trace\n");
            gDvm.emulatorTracePage = NULL;
        } else {
            *(u4*) gDvm.emulatorTracePage = 0;
        }
    }
#else
    assert(gDvm.emulatorTracePage == NULL);
#endif

    return true;
}

/*
 * Free up profiling resources.
 */
void dvmProfilingShutdown(void)
{
#ifdef UPDATE_MAGIC_PAGE
    if (gDvm.emulatorTracePage != NULL)
        munmap(gDvm.emulatorTracePage, SYSTEM_PAGE_SIZE);
#endif
    free(gDvm.executedInstrCounts);
}

/*
 * Update the "active profilers" count.
 *
 * "count" should be +1 or -1.
 */
static void updateActiveProfilers(int count)
{
    int oldValue, newValue;

    do {
        oldValue = gDvm.activeProfilers;
        newValue = oldValue + count;
        if (newValue < 0) {
            LOGE("Can't have %d active profilers\n", newValue);
            dvmAbort();
        }
    } while (android_atomic_release_cas(oldValue, newValue,
            &gDvm.activeProfilers) != 0);

    LOGD("+++ active profiler count now %d\n", newValue);
#if defined(WITH_JIT)
    dvmCompilerStateRefresh();
#endif
}


/*
 * Reset the "cpuClockBase" field in all threads.
 */
static void resetCpuClockBase(void)
{
    Thread* thread;

    dvmLockThreadList(NULL);
    for (thread = gDvm.threadList; thread != NULL; thread = thread->next) {
        thread->cpuClockBaseSet = false;
        thread->cpuClockBase = 0;
    }
    dvmUnlockThreadList();
}

/*
 * Dump the thread list to the specified file.
 */
static void dumpThreadList(FILE* fp)
{
    Thread* thread;

    dvmLockThreadList(NULL);
    for (thread = gDvm.threadList; thread != NULL; thread = thread->next) {
        char* name = dvmGetThreadName(thread);

        fprintf(fp, "%d\t%s\n", thread->threadId, name);
        free(name);
    }
    dvmUnlockThreadList();
}

/*
 * This is a dvmHashForeach callback.
 */
static int dumpMarkedMethods(void* vclazz, void* vfp)
{
    DexStringCache stringCache;
    ClassObject* clazz = (ClassObject*) vclazz;
    FILE* fp = (FILE*) vfp;
    Method* meth;
    char* name;
    int i;

    dexStringCacheInit(&stringCache);

    for (i = 0; i < clazz->virtualMethodCount; i++) {
        meth = &clazz->virtualMethods[i];
        if (meth->inProfile) {
            name = dvmDescriptorToName(meth->clazz->descriptor);
            fprintf(fp, "0x%08x\t%s\t%s\t%s\t%s\t%d\n", (int) meth,
                name, meth->name,
                dexProtoGetMethodDescriptor(&meth->prototype, &stringCache),
                dvmGetMethodSourceFile(meth), dvmLineNumFromPC(meth, 0));
            meth->inProfile = false;
            free(name);
        }
    }

    for (i = 0; i < clazz->directMethodCount; i++) {
        meth = &clazz->directMethods[i];
        if (meth->inProfile) {
            name = dvmDescriptorToName(meth->clazz->descriptor);
            fprintf(fp, "0x%08x\t%s\t%s\t%s\t%s\t%d\n", (int) meth,
                name, meth->name,
                dexProtoGetMethodDescriptor(&meth->prototype, &stringCache),
                dvmGetMethodSourceFile(meth), dvmLineNumFromPC(meth, 0));
            meth->inProfile = false;
            free(name);
        }
    }

    dexStringCacheRelease(&stringCache);

    return 0;
}

/*
 * Dump the list of "marked" methods to the specified file.
 */
static void dumpMethodList(FILE* fp)
{
    dvmHashTableLock(gDvm.loadedClasses);
    dvmHashForeach(gDvm.loadedClasses, dumpMarkedMethods, (void*) fp);
    dvmHashTableUnlock(gDvm.loadedClasses);
}

/*
 * Start method tracing.  Method tracing is global to the VM (i.e. we
 * trace all threads).
 *
 * This opens the output file (if an already open fd has not been supplied,
 * and we're not going direct to DDMS) and allocates the data buffer.  This
 * takes ownership of the file descriptor, closing it on completion.
 *
 * On failure, we throw an exception and return.
 */
void dvmMethodTraceStart(const char* traceFileName, int traceFd, int bufferSize,
    int flags, bool directToDdms)
{
    MethodTraceState* state = &gDvm.methodTrace;

    assert(bufferSize > 0);

    dvmLockMutex(&state->startStopLock);
    while (state->traceEnabled != 0) {
        LOGI("TRACE start requested, but already in progress; stopping\n");
        dvmUnlockMutex(&state->startStopLock);
        dvmMethodTraceStop();
        dvmLockMutex(&state->startStopLock);
    }
    updateActiveProfilers(1);
    LOGI("TRACE STARTED: '%s' %dKB\n", traceFileName, bufferSize / 1024);

    /*
     * Allocate storage and open files.
     *
     * We don't need to initialize the buffer, but doing so might remove
     * some fault overhead if the pages aren't mapped until touched.
     */
    state->buf = (u1*) malloc(bufferSize);
    if (state->buf == NULL) {
        dvmThrowException("Ljava/lang/InternalError;", "buffer alloc failed");
        goto fail;
    }
    if (!directToDdms) {
        if (traceFd < 0) {
            state->traceFile = fopen(traceFileName, "w");
        } else {
            state->traceFile = fdopen(traceFd, "w");
        }
        if (state->traceFile == NULL) {
            int err = errno;
            LOGE("Unable to open trace file '%s': %s\n",
                traceFileName, strerror(err));
            dvmThrowExceptionFmt("Ljava/lang/RuntimeException;",
                "Unable to open trace file '%s': %s",
                traceFileName, strerror(err));
            goto fail;
        }
    }
    traceFd = -1;
    memset(state->buf, (char)FILL_PATTERN, bufferSize);

    state->directToDdms = directToDdms;
    state->bufferSize = bufferSize;
    state->overflow = false;

    /*
     * Enable alloc counts if we've been requested to do so.
     */
    state->flags = flags;
    if ((flags & TRACE_ALLOC_COUNTS) != 0)
        dvmStartAllocCounting();

    /* reset our notion of the start time for all CPU threads */
    resetCpuClockBase();

    state->startWhen = getTimeInUsec();

    /*
     * Output the header.
     */
    memset(state->buf, 0, TRACE_HEADER_LEN);
    storeIntLE(state->buf + 0, TRACE_MAGIC);
    storeShortLE(state->buf + 4, TRACE_VERSION);
    storeShortLE(state->buf + 6, TRACE_HEADER_LEN);
    storeLongLE(state->buf + 8, state->startWhen);
    state->curOffset = TRACE_HEADER_LEN;

    /*
     * Set the "enabled" flag.  Once we do this, threads will wait to be
     * signaled before exiting, so we have to make sure we wake them up.
     */
    android_atomic_release_store(true, &state->traceEnabled);
    dvmUnlockMutex(&state->startStopLock);
    return;

fail:
    updateActiveProfilers(-1);
    if (state->traceFile != NULL) {
        fclose(state->traceFile);
        state->traceFile = NULL;
    }
    if (state->buf != NULL) {
        free(state->buf);
        state->buf = NULL;
    }
    if (traceFd >= 0)
        close(traceFd);
    dvmUnlockMutex(&state->startStopLock);
}

/*
 * Run through the data buffer and pull out the methods that were visited.
 * Set a mark so that we know which ones to output.
 */
static void markTouchedMethods(int endOffset)
{
    u1* ptr = gDvm.methodTrace.buf + TRACE_HEADER_LEN;
    u1* end = gDvm.methodTrace.buf + endOffset;
    unsigned int methodVal;
    Method* method;

    while (ptr < end) {
        methodVal = *(ptr+1) | (*(ptr+2) << 8) | (*(ptr+3) << 16)
                    | (*(ptr+4) << 24);
        method = (Method*) METHOD_ID(methodVal);

        method->inProfile = true;
        ptr += TRACE_REC_SIZE;
    }
}

/*
 * Compute the amount of overhead in a clock call, in nsec.
 *
 * This value is going to vary depending on what else is going on in the
 * system.  When examined across several runs a pattern should emerge.
 */
static u4 getClockOverhead(void)
{
    u8 calStart, calElapsed;
    int i;

    calStart = getClock();
    for (i = 1000 * 4; i > 0; i--) {
        getClock();
        getClock();
        getClock();
        getClock();
        getClock();
        getClock();
        getClock();
        getClock();
    }

    calElapsed = getClock() - calStart;
    return (int) (calElapsed / (8*4));
}

/*
 * Returns "true" if method tracing is currently active.
 */
bool dvmIsMethodTraceActive(void)
{
    const MethodTraceState* state = &gDvm.methodTrace;
    return state->traceEnabled;
}

/*
 * Stop method tracing.  We write the buffer to disk and generate a key
 * file so we can interpret it.
 */
void dvmMethodTraceStop(void)
{
    MethodTraceState* state = &gDvm.methodTrace;
    u8 elapsed;

    /*
     * We need this to prevent somebody from starting a new trace while
     * we're in the process of stopping the old.
     */
    dvmLockMutex(&state->startStopLock);

    if (!state->traceEnabled) {
        /* somebody already stopped it, or it was never started */
        LOGD("TRACE stop requested, but not running\n");
        dvmUnlockMutex(&state->startStopLock);
        return;
    } else {
        updateActiveProfilers(-1);
    }

    /* compute elapsed time */
    elapsed = getTimeInUsec() - state->startWhen;

    /*
     * Globally disable it, and allow other threads to notice.  We want
     * to stall here for at least as long as dvmMethodTraceAdd needs
     * to finish.  There's no real risk though -- it will take a while to
     * write the data to disk, and we don't clear the buffer pointer until
     * after that completes.
     */
    state->traceEnabled = false;
    ANDROID_MEMBAR_FULL();
    sched_yield();
    usleep(250 * 1000);

    if ((state->flags & TRACE_ALLOC_COUNTS) != 0)
        dvmStopAllocCounting();

    /*
     * It's possible under some circumstances for a thread to have advanced
     * the data pointer but not written the method value.  It's possible
     * (though less likely) for the data pointer to be advanced, or partial
     * data written, while we're doing work here.
     *
     * To avoid seeing partially-written data, we grab state->curOffset here,
     * and use our local copy from here on.  We then scan through what's
     * already written.  If we see the fill pattern in what should be the
     * method pointer, we cut things off early.  (If we don't, we'll fail
     * when we dereference the pointer.)
     *
     * There's a theoretical possibility of interrupting another thread
     * after it has partially written the method pointer, in which case
     * we'll likely crash when we dereference it.  The possibility of
     * this actually happening should be at or near zero.  Fixing it
     * completely could be done by writing the thread number last and
     * using a sentinel value to indicate a partially-written record,
     * but that requires memory barriers.
     */
    int finalCurOffset = state->curOffset;

    if (finalCurOffset > TRACE_HEADER_LEN) {
        u4 fillVal = METHOD_ID(FILL_PATTERN);
        u1* scanPtr = state->buf + TRACE_HEADER_LEN;

        while (scanPtr < state->buf + finalCurOffset) {
            u4 methodVal = scanPtr[1] | (scanPtr[2] << 8) | (scanPtr[3] << 16)
                        | (scanPtr[4] << 24);
            if (METHOD_ID(methodVal) == fillVal) {
                u1* scanBase = state->buf + TRACE_HEADER_LEN;
                LOGW("Found unfilled record at %d (of %d)\n",
                    (scanPtr - scanBase) / TRACE_REC_SIZE,
                    (finalCurOffset - TRACE_HEADER_LEN) / TRACE_REC_SIZE);
                finalCurOffset = scanPtr - state->buf;
                break;
            }

            scanPtr += TRACE_REC_SIZE;
        }
    }

    LOGI("TRACE STOPPED%s: writing %d records\n",
        state->overflow ? " (NOTE: overflowed buffer)" : "",
        (finalCurOffset - TRACE_HEADER_LEN) / TRACE_REC_SIZE);
    if (gDvm.debuggerActive) {
        LOGW("WARNING: a debugger is active; method-tracing results "
             "will be skewed\n");
    }

    /*
     * Do a quick calibration test to see how expensive our clock call is.
     */
    u4 clockNsec = getClockOverhead();

    markTouchedMethods(finalCurOffset);

    char* memStreamPtr;
    size_t memStreamSize;
    if (state->directToDdms) {
        assert(state->traceFile == NULL);
        state->traceFile = open_memstream(&memStreamPtr, &memStreamSize);
        if (state->traceFile == NULL) {
            /* not expected */
            LOGE("Unable to open memstream\n");
            dvmAbort();
        }
    }
    assert(state->traceFile != NULL);

    fprintf(state->traceFile, "%cversion\n", TOKEN_CHAR);
    fprintf(state->traceFile, "%d\n", TRACE_VERSION);
    fprintf(state->traceFile, "data-file-overflow=%s\n",
        state->overflow ? "true" : "false");
#if defined(HAVE_POSIX_CLOCKS)
    if (!gDvm.profilerWallClock) {
        fprintf(state->traceFile, "clock=thread-cpu\n");
    } else
#endif
    {
        fprintf(state->traceFile, "clock=wall\n");
    }
    fprintf(state->traceFile, "elapsed-time-usec=%llu\n", elapsed);
    fprintf(state->traceFile, "num-method-calls=%d\n",
        (finalCurOffset - TRACE_HEADER_LEN) / TRACE_REC_SIZE);
    fprintf(state->traceFile, "clock-call-overhead-nsec=%d\n", clockNsec);
    fprintf(state->traceFile, "vm=dalvik\n");
    if ((state->flags & TRACE_ALLOC_COUNTS) != 0) {
        fprintf(state->traceFile, "alloc-count=%d\n",
            gDvm.allocProf.allocCount);
        fprintf(state->traceFile, "alloc-size=%d\n",
            gDvm.allocProf.allocSize);
        fprintf(state->traceFile, "gc-count=%d\n",
            gDvm.allocProf.gcCount);
    }
    fprintf(state->traceFile, "%cthreads\n", TOKEN_CHAR);
    dumpThreadList(state->traceFile);
    fprintf(state->traceFile, "%cmethods\n", TOKEN_CHAR);
    dumpMethodList(state->traceFile);
    fprintf(state->traceFile, "%cend\n", TOKEN_CHAR);

    if (state->directToDdms) {
        /*
         * Data is in two places: memStreamPtr and state->buf.  Send
         * the whole thing to DDMS, wrapped in an MPSE packet.
         */
        fflush(state->traceFile);

        struct iovec iov[2];
        iov[0].iov_base = memStreamPtr;
        iov[0].iov_len = memStreamSize;
        iov[1].iov_base = state->buf;
        iov[1].iov_len = finalCurOffset;
        dvmDbgDdmSendChunkV(CHUNK_TYPE("MPSE"), iov, 2);
    } else {
        /* append the profiling data */
        if (fwrite(state->buf, finalCurOffset, 1, state->traceFile) != 1) {
            int err = errno;
            LOGE("trace fwrite(%d) failed: %s\n",
                finalCurOffset, strerror(err));
            dvmThrowExceptionFmt("Ljava/lang/RuntimeException;",
                "Trace data write failed: %s", strerror(err));
        }
    }

    /* done! */
    free(state->buf);
    state->buf = NULL;
    fclose(state->traceFile);
    state->traceFile = NULL;

    /* wake any threads that were waiting for profiling to complete */
    dvmBroadcastCond(&state->threadExitCond);
    dvmUnlockMutex(&state->startStopLock);
}


/*
 * We just did something with a method.  Emit a record.
 *
 * Multiple threads may be banging on this all at once.  We use atomic ops
 * rather than mutexes for speed.
 */
void dvmMethodTraceAdd(Thread* self, const Method* method, int action)
{
    MethodTraceState* state = &gDvm.methodTrace;
    u4 clockDiff, methodVal;
    int oldOffset, newOffset;
    u1* ptr;

    /*
     * We can only access the per-thread CPU clock from within the
     * thread, so we have to initialize the base time on the first use.
     * (Looks like pthread_getcpuclockid(thread, &id) will do what we
     * want, but it doesn't appear to be defined on the device.)
     */
    if (!self->cpuClockBaseSet) {
        self->cpuClockBase = getClock();
        self->cpuClockBaseSet = true;
        //LOGI("thread base id=%d 0x%llx\n",
        //    self->threadId, self->cpuClockBase);
    }

    /*
     * Advance "curOffset" atomically.
     */
    do {
        oldOffset = state->curOffset;
        newOffset = oldOffset + TRACE_REC_SIZE;
        if (newOffset > state->bufferSize) {
            state->overflow = true;
            return;
        }
    } while (android_atomic_release_cas(oldOffset, newOffset,
            &state->curOffset) != 0);

    //assert(METHOD_ACTION((u4) method) == 0);

    u8 now = getClock();
    clockDiff = (u4) (now - self->cpuClockBase);

    methodVal = METHOD_COMBINE((u4) method, action);

    /*
     * Write data into "oldOffset".
     */
    ptr = state->buf + oldOffset;
    *ptr++ = self->threadId;
    *ptr++ = (u1) methodVal;
    *ptr++ = (u1) (methodVal >> 8);
    *ptr++ = (u1) (methodVal >> 16);
    *ptr++ = (u1) (methodVal >> 24);
    *ptr++ = (u1) clockDiff;
    *ptr++ = (u1) (clockDiff >> 8);
    *ptr++ = (u1) (clockDiff >> 16);
    *ptr++ = (u1) (clockDiff >> 24);
}

/*
 * We just did something with a method.  Emit a record by setting a value
 * in a magic memory location.
 */
void dvmEmitEmulatorTrace(const Method* method, int action)
{
#ifdef UPDATE_MAGIC_PAGE
    /*
     * We store the address of the Dalvik bytecodes to the memory-mapped
     * trace page for normal Java methods.  We also trace calls to native
     * functions by storing the address of the native function to the
     * trace page.
     * Abstract methods don't have any bytecodes, so we don't trace them.
     * (Abstract methods are never called, but in Dalvik they can be
     * because we do a "late trap" to a native method to generate the
     * abstract method exception.)
     */
    if (dvmIsAbstractMethod(method))
        return;

    u4* pMagic = (u4*) gDvm.emulatorTracePage;
    u4 addr;

    if (dvmIsNativeMethod(method)) {
        /*
         * The "action" parameter is one of:
         *   0 = ENTER
         *   1 = EXIT
         *   2 = UNROLL
         * To help the trace tools reconstruct the runtime stack containing
         * a mix of Java plus native methods, we add 4 to the action if this
         * is a native method.
         */
        action += 4;

        /*
         * Get the address of the native function.
         * This isn't the right address -- how do I get it?
         * Fortunately, the trace tools can get by without the address, but
         * it would be nice to fix this.
         */
         addr = (u4) method->nativeFunc;
    } else {
        /*
         * The dexlist output shows the &DexCode.insns offset value, which
         * is offset from the start of the base DEX header. Method.insns
         * is the absolute address, effectively offset from the start of
         * the optimized DEX header. We either need to return the
         * optimized DEX base file address offset by the right amount, or
         * take the "real" address and subtract off the size of the
         * optimized DEX header.
         *
         * Would be nice to factor this out at dexlist time, but we can't count
         * on having access to the correct optimized DEX file.
         */
        assert(method->insns != NULL);
        const DexOptHeader* pOptHdr = method->clazz->pDvmDex->pDexFile->pOptHeader;
        addr = (u4) method->insns - pOptHdr->dexOffset;
    }

    *(pMagic+action) = addr;
    LOGVV("Set %p = 0x%08x (%s.%s)\n",
        pMagic+action, addr, method->clazz->descriptor, method->name);
#endif
}

/*
 * The GC calls this when it's about to start.  We add a marker to the
 * trace output so the tool can exclude the GC cost from the results.
 */
void dvmMethodTraceGCBegin(void)
{
    TRACE_METHOD_ENTER(dvmThreadSelf(), gDvm.methodTrace.gcMethod);
}
void dvmMethodTraceGCEnd(void)
{
    TRACE_METHOD_EXIT(dvmThreadSelf(), gDvm.methodTrace.gcMethod);
}

/*
 * The class loader calls this when it's loading or initializing a class.
 */
void dvmMethodTraceClassPrepBegin(void)
{
    TRACE_METHOD_ENTER(dvmThreadSelf(), gDvm.methodTrace.classPrepMethod);
}
void dvmMethodTraceClassPrepEnd(void)
{
    TRACE_METHOD_EXIT(dvmThreadSelf(), gDvm.methodTrace.classPrepMethod);
}


/*
 * Enable emulator trace info.
 */
void dvmEmulatorTraceStart(void)
{
    /* If we could not map the emulator trace page, then do not enable tracing */
    if (gDvm.emulatorTracePage == NULL)
        return;

    updateActiveProfilers(1);

    /* in theory we should make this an atomic inc; in practice not important */
    gDvm.emulatorTraceEnableCount++;
    if (gDvm.emulatorTraceEnableCount == 1)
        LOGD("--- emulator method traces enabled\n");
}

/*
 * Disable emulator trace info.
 */
void dvmEmulatorTraceStop(void)
{
    if (gDvm.emulatorTraceEnableCount == 0) {
        LOGE("ERROR: emulator tracing not enabled\n");
        return;
    }
    updateActiveProfilers(-1);
    /* in theory we should make this an atomic inc; in practice not important */
    gDvm.emulatorTraceEnableCount--;
    if (gDvm.emulatorTraceEnableCount == 0)
        LOGD("--- emulator method traces disabled\n");
}


/*
 * Start instruction counting.
 */
void dvmStartInstructionCounting()
{
    updateActiveProfilers(1);
    /* in theory we should make this an atomic inc; in practice not important */
    gDvm.instructionCountEnableCount++;
}

/*
 * Start instruction counting.
 */
void dvmStopInstructionCounting()
{
    if (gDvm.instructionCountEnableCount == 0) {
        LOGE("ERROR: instruction counting not enabled\n");
        dvmAbort();
    }
    updateActiveProfilers(-1);
    gDvm.instructionCountEnableCount--;
}


/*
 * Start alloc counting.  Note this doesn't affect the "active profilers"
 * count, since the interpreter loop is not involved.
 */
void dvmStartAllocCounting(void)
{
    gDvm.allocProf.enabled = true;
}

/*
 * Stop alloc counting.
 */
void dvmStopAllocCounting(void)
{
    gDvm.allocProf.enabled = false;
}
