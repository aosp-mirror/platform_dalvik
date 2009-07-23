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
#ifdef WITH_JIT

/*
 * Target independent portion of Android's Jit
 */

#include "Dalvik.h"
#include "Jit.h"


#include "dexdump/OpCodeNames.h"
#include <unistd.h>
#include <pthread.h>
#include <sys/time.h>
#include <signal.h>
#include "compiler/Compiler.h"
#include "compiler/CompilerUtility.h"
#include "compiler/CompilerIR.h"
#include <errno.h>

int dvmJitStartup(void)
{
    unsigned int i;
    bool res = true;  /* Assume success */

    // Create the compiler thread and setup miscellaneous chores */
    res &= dvmCompilerStartup();

    dvmInitMutex(&gDvmJit.tableLock);
    if (res && gDvm.executionMode == kExecutionModeJit) {
        JitEntry *pJitTable = NULL;
        unsigned char *pJitProfTable = NULL;
        assert(gDvm.jitTableSize &&
            !(gDvm.jitTableSize & (gDvmJit.jitTableSize - 1))); // Power of 2?
        dvmLockMutex(&gDvmJit.tableLock);
        pJitTable = (JitEntry*)
                    calloc(gDvmJit.jitTableSize, sizeof(*pJitTable));
        if (!pJitTable) {
            LOGE("jit table allocation failed\n");
            res = false;
            goto done;
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
            res = false;
            goto done;
        }
        memset(pJitProfTable,0,JIT_PROF_SIZE);
        for (i=0; i < gDvmJit.jitTableSize; i++) {
           pJitTable[i].u.info.chain = gDvmJit.jitTableSize;
        }
        /* Is chain field wide enough for termination pattern? */
        assert(pJitTable[0].u.info.chain == gDvm.maxJitTableEntries);

done:
        gDvmJit.pJitEntryTable = pJitTable;
        gDvmJit.jitTableMask = gDvmJit.jitTableSize - 1;
        gDvmJit.jitTableEntriesUsed = 0;
        gDvmJit.pProfTableCopy = gDvmJit.pProfTable = pJitProfTable;
        dvmUnlockMutex(&gDvmJit.tableLock);
    }
    return res;
}

/*
 * If one of our fixed tables or the translation buffer fills up,
 * call this routine to avoid wasting cycles on future translation requests.
 */
void dvmJitStopTranslationRequests()
{
    /*
     * Note 1: This won't necessarily stop all translation requests, and
     * operates on a delayed mechanism.  Running threads look to the copy
     * of this value in their private InterpState structures and won't see
     * this change until it is refreshed (which happens on interpreter
     * entry).
     * Note 2: This is a one-shot memory leak on this table. Because this is a
     * permanent off switch for Jit profiling, it is a one-time leak of 1K
     * bytes, and no further attempt will be made to re-allocate it.  Can't
     * free it because some thread may be holding a reference.
     */
    gDvmJit.pProfTable = gDvmJit.pProfTableCopy = NULL;
}

#if defined(EXIT_STATS)
/* Convenience function to increment counter from assembly code */
void dvmBumpNoChain()
{
    gDvm.jitNoChainExit++;
}

/* Convenience function to increment counter from assembly code */
void dvmBumpNormal()
{
    gDvm.jitNormalExit++;
}

/* Convenience function to increment counter from assembly code */
void dvmBumpPunt(int from)
{
    gDvm.jitPuntExit++;
}
#endif

/* Dumps debugging & tuning stats to the log */
void dvmJitStats()
{
    int i;
    int hit;
    int not_hit;
    int chains;
    if (gDvmJit.pJitEntryTable) {
        for (i=0, chains=hit=not_hit=0;
             i < (int) gDvmJit.jitTableSize;
             i++) {
            if (gDvmJit.pJitEntryTable[i].dPC != 0)
                hit++;
            else
                not_hit++;
            if (gDvmJit.pJitEntryTable[i].u.info.chain != gDvmJit.jitTableSize)
                chains++;
        }
        LOGD(
         "JIT: %d traces, %d slots, %d chains, %d maxQ, %d thresh, %s",
         hit, not_hit + hit, chains, gDvmJit.compilerMaxQueued,
         gDvmJit.threshold, gDvmJit.blockingMode ? "Blocking" : "Non-blocking");
#if defined(EXIT_STATS)
        LOGD(
         "JIT: Lookups: %d hits, %d misses; %d NoChain, %d normal, %d punt",
         gDvmJit.addrLookupsFound, gDvmJit.addrLookupsNotFound,
         gDvmJit.noChainExit, gDvmJit.normalExit, gDvmJit.puntExit);
#endif
        LOGD("JIT: %d Translation chains", gDvmJit.translationChains);
#if defined(INVOKE_STATS)
        LOGD("JIT: Invoke: %d chainable, %d pred. chain, %d native, "
             "%d return",
             gDvmJit.invokeChain, gDvmJit.invokePredictedChain,
             gDvmJit.invokeNative, gDvmJit.returnOp);
#endif
        if (gDvmJit.profile) {
            dvmCompilerSortAndPrintTraceProfiles();
        }
    }
}


/*
 * Final JIT shutdown.  Only do this once, and do not attempt to restart
 * the JIT later.
 */
void dvmJitShutdown(void)
{
    /* Shutdown the compiler thread */
    dvmCompilerShutdown();

    dvmCompilerDumpStats();

    dvmDestroyMutex(&gDvmJit.tableLock);

    if (gDvmJit.pJitEntryTable) {
        free(gDvmJit.pJitEntryTable);
        gDvmJit.pJitEntryTable = NULL;
    }

    if (gDvmJit.pProfTable) {
        free(gDvmJit.pProfTable);
        gDvmJit.pProfTable = NULL;
    }
}

/*
 * Adds to the current trace request one instruction at a time, just
 * before that instruction is interpreted.  This is the primary trace
 * selection function.  NOTE: return instruction are handled a little
 * differently.  In general, instructions are "proposed" to be added
 * to the current trace prior to interpretation.  If the interpreter
 * then successfully completes the instruction, is will be considered
 * part of the request.  This allows us to examine machine state prior
 * to interpretation, and also abort the trace request if the instruction
 * throws or does something unexpected.  However, return instructions
 * will cause an immediate end to the translation request - which will
 * be passed to the compiler before the return completes.  This is done
 * in response to special handling of returns by the interpreter (and
 * because returns cannot throw in a way that causes problems for the
 * translated code.
 */
int dvmCheckJit(const u2* pc, Thread* self, InterpState* interpState)
{
    int flags,i,len;
    int switchInterp = false;
    int debugOrProfile = (gDvm.debuggerActive || self->suspendCount
#if defined(WITH_PROFILER)
                          || gDvm.activeProfilers
#endif
            );

    switch (interpState->jitState) {
        char* nopStr;
        int target;
        int offset;
        DecodedInstruction decInsn;
        case kJitTSelect:
            dexDecodeInstruction(gDvm.instrFormat, pc, &decInsn);
#if defined(SHOW_TRACE)
            LOGD("TraceGen: adding %s",getOpcodeName(decInsn.opCode));
#endif
            flags = dexGetInstrFlags(gDvm.instrFlags, decInsn.opCode);
            len = dexGetInstrOrTableWidthAbs(gDvm.instrWidth, pc);
            offset = pc - interpState->method->insns;
            if (pc != interpState->currRunHead + interpState->currRunLen) {
                int currTraceRun;
                /* We need to start a new trace run */
                currTraceRun = ++interpState->currTraceRun;
                interpState->currRunLen = 0;
                interpState->currRunHead = (u2*)pc;
                interpState->trace[currTraceRun].frag.startOffset = offset;
                interpState->trace[currTraceRun].frag.numInsts = 0;
                interpState->trace[currTraceRun].frag.runEnd = false;
                interpState->trace[currTraceRun].frag.hint = kJitHintNone;
            }
            interpState->trace[interpState->currTraceRun].frag.numInsts++;
            interpState->totalTraceLen++;
            interpState->currRunLen += len;
            if (  ((flags & kInstrUnconditional) == 0) &&
                  ((flags & (kInstrCanBranch |
                             kInstrCanSwitch |
                             kInstrCanReturn |
                             kInstrInvoke)) != 0)) {
                    interpState->jitState = kJitTSelectEnd;
#if defined(SHOW_TRACE)
            LOGD("TraceGen: ending on %s, basic block end",
                 getOpcodeName(decInsn.opCode));
#endif
            }
            if (decInsn.opCode == OP_THROW) {
                interpState->jitState = kJitTSelectEnd;
            }
            if (interpState->totalTraceLen >= JIT_MAX_TRACE_LEN) {
                interpState->jitState = kJitTSelectEnd;
            }
            if (debugOrProfile) {
                interpState->jitState = kJitTSelectAbort;
                switchInterp = !debugOrProfile;
                break;
            }
            if ((flags & kInstrCanReturn) != kInstrCanReturn) {
                break;
            }
            /* NOTE: intentional fallthrough for returns */
        case kJitTSelectEnd:
            {
                if (interpState->totalTraceLen == 0) {
                    switchInterp = !debugOrProfile;
                    break;
                }
                JitTraceDescription* desc =
                   (JitTraceDescription*)malloc(sizeof(JitTraceDescription) +
                     sizeof(JitTraceRun) * (interpState->currTraceRun+1));
                if (desc == NULL) {
                    LOGE("Out of memory in trace selection");
                    dvmJitStopTranslationRequests();
                    interpState->jitState = kJitTSelectAbort;
                    switchInterp = !debugOrProfile;
                    break;
                }
                interpState->trace[interpState->currTraceRun].frag.runEnd =
                     true;
                interpState->jitState = kJitNormal;
                desc->method = interpState->method;
                memcpy((char*)&(desc->trace[0]),
                    (char*)&(interpState->trace[0]),
                    sizeof(JitTraceRun) * (interpState->currTraceRun+1));
#if defined(SHOW_TRACE)
                LOGD("TraceGen:  trace done, adding to queue");
#endif
                dvmCompilerWorkEnqueue(
                       interpState->currTraceHead,kWorkOrderTrace,desc);
                if (gDvmJit.blockingMode) {
                    dvmCompilerDrainQueue();
                }
                switchInterp = !debugOrProfile;
            }
            break;
        case kJitSingleStep:
            interpState->jitState = kJitSingleStepEnd;
            break;
        case kJitSingleStepEnd:
            interpState->entryPoint = kInterpEntryResume;
            switchInterp = !debugOrProfile;
            break;
        case kJitTSelectAbort:
#if defined(SHOW_TRACE)
            LOGD("TraceGen:  trace abort");
#endif
            interpState->jitState = kJitNormal;
            switchInterp = !debugOrProfile;
            break;
        case kJitNormal:
            switchInterp = !debugOrProfile;
            break;
        default:
            dvmAbort();
    }
    return switchInterp;
}

static inline JitEntry *findJitEntry(const u2* pc)
{
    int idx = dvmJitHash(pc);

    /* Expect a high hit rate on 1st shot */
    if (gDvmJit.pJitEntryTable[idx].dPC == pc)
        return &gDvmJit.pJitEntryTable[idx];
    else {
        int chainEndMarker = gDvmJit.jitTableSize;
        while (gDvmJit.pJitEntryTable[idx].u.info.chain != chainEndMarker) {
            idx = gDvmJit.pJitEntryTable[idx].u.info.chain;
            if (gDvmJit.pJitEntryTable[idx].dPC == pc)
                return &gDvmJit.pJitEntryTable[idx];
        }
    }
    return NULL;
}

JitEntry *dvmFindJitEntry(const u2* pc)
{
    return findJitEntry(pc);
}

/*
 * If a translated code address exists for the davik byte code
 * pointer return it.  This routine needs to be fast.
 */
void* dvmJitGetCodeAddr(const u2* dPC)
{
    int idx = dvmJitHash(dPC);

    /* If anything is suspended, don't re-enter the code cache */
    if (gDvm.sumThreadSuspendCount > 0) {
        return NULL;
    }

    /* Expect a high hit rate on 1st shot */
    if (gDvmJit.pJitEntryTable[idx].dPC == dPC) {
#if defined(EXIT_STATS)
        gDvmJit.addrLookupsFound++;
#endif
        return gDvmJit.pJitEntryTable[idx].codeAddress;
    } else {
        int chainEndMarker = gDvmJit.jitTableSize;
        while (gDvmJit.pJitEntryTable[idx].u.info.chain != chainEndMarker) {
            idx = gDvmJit.pJitEntryTable[idx].u.info.chain;
            if (gDvmJit.pJitEntryTable[idx].dPC == dPC) {
#if defined(EXIT_STATS)
                gDvmJit.addrLookupsFound++;
#endif
                return gDvmJit.pJitEntryTable[idx].codeAddress;
            }
        }
    }
#if defined(EXIT_STATS)
    gDvmJit.addrLookupsNotFound++;
#endif
    return NULL;
}

/*
 * Find an entry in the JitTable, creating if necessary.
 * Returns null if table is full.
 */
JitEntry *dvmJitLookupAndAdd(const u2* dPC)
{
    u4 chainEndMarker = gDvmJit.jitTableSize;
    u4 idx = dvmJitHash(dPC);

    /* Walk the bucket chain to find an exact match for our PC */
    while ((gDvmJit.pJitEntryTable[idx].u.info.chain != chainEndMarker) &&
           (gDvmJit.pJitEntryTable[idx].dPC != dPC)) {
        idx = gDvmJit.pJitEntryTable[idx].u.info.chain;
    }

    if (gDvmJit.pJitEntryTable[idx].dPC != dPC) {
        /*
         * No match.  Aquire jitTableLock and find the last
         * slot in the chain. Possibly continue the chain walk in case
         * some other thread allocated the slot we were looking
         * at previuosly (perhaps even the dPC we're trying to enter).
         */
        dvmLockMutex(&gDvmJit.tableLock);
        /*
         * At this point, if .dPC is NULL, then the slot we're
         * looking at is the target slot from the primary hash
         * (the simple, and common case).  Otherwise we're going
         * to have to find a free slot and chain it.
         */
        MEM_BARRIER(); /* Make sure we reload [].dPC after lock */
        if (gDvmJit.pJitEntryTable[idx].dPC != NULL) {
            u4 prev;
            while (gDvmJit.pJitEntryTable[idx].u.info.chain != chainEndMarker) {
                if (gDvmJit.pJitEntryTable[idx].dPC == dPC) {
                    /* Another thread got there first for this dPC */
                    dvmUnlockMutex(&gDvmJit.tableLock);
                    return &gDvmJit.pJitEntryTable[idx];
                }
                idx = gDvmJit.pJitEntryTable[idx].u.info.chain;
            }
            /* Here, idx should be pointing to the last cell of an
             * active chain whose last member contains a valid dPC */
            assert(gDvmJit.pJitEntryTable[idx].dPC != NULL);
            /* Linear walk to find a free cell and add it to the end */
            prev = idx;
            while (true) {
                idx++;
                if (idx == chainEndMarker)
                    idx = 0;  /* Wraparound */
                if ((gDvmJit.pJitEntryTable[idx].dPC == NULL) ||
                    (idx == prev))
                    break;
            }
            if (idx != prev) {
                JitEntryInfoUnion oldValue;
                JitEntryInfoUnion newValue;
                /*
                 * Although we hold the lock so that noone else will
                 * be trying to update a chain field, the other fields
                 * packed into the word may be in use by other threads.
                 */
                do {
                    oldValue = gDvmJit.pJitEntryTable[prev].u;
                    newValue = oldValue;
                    newValue.info.chain = idx;
                } while (!ATOMIC_CMP_SWAP(
                         &gDvmJit.pJitEntryTable[prev].u.infoWord,
                         oldValue.infoWord, newValue.infoWord));
            }
        }
        if (gDvmJit.pJitEntryTable[idx].dPC == NULL) {
           /* Allocate the slot */
            gDvmJit.pJitEntryTable[idx].dPC = dPC;
            gDvmJit.jitTableEntriesUsed++;
        } else {
            /* Table is full */
            idx = chainEndMarker;
        }
        dvmUnlockMutex(&gDvmJit.tableLock);
    }
    return (idx == chainEndMarker) ? NULL : &gDvmJit.pJitEntryTable[idx];
}
/*
 * Register the translated code pointer into the JitTable.
 * NOTE: Once a codeAddress field transitions from NULL to
 * JIT'd code, it must not be altered without first halting all
 * threads.  This routine should only be called by the compiler
 * thread.
 */
void dvmJitSetCodeAddr(const u2* dPC, void *nPC, JitInstructionSetType set) {
    JitEntryInfoUnion oldValue;
    JitEntryInfoUnion newValue;
    JitEntry *jitEntry = dvmJitLookupAndAdd(dPC);
    assert(jitEntry);
    /* Note: order of update is important */
    do {
        oldValue = jitEntry->u;
        newValue = oldValue;
        newValue.info.instructionSet = set;
    } while (!ATOMIC_CMP_SWAP(
             &jitEntry->u.infoWord,
             oldValue.infoWord, newValue.infoWord));
    jitEntry->codeAddress = nPC;
}

/*
 * Determine if valid trace-bulding request is active.  Return true
 * if we need to abort and switch back to the fast interpreter, false
 * otherwise.  NOTE: may be called even when trace selection is not being
 * requested
 */

bool dvmJitCheckTraceRequest(Thread* self, InterpState* interpState)
{
    bool res = false;         /* Assume success */
    int i;
    if (gDvmJit.pJitEntryTable != NULL) {
        /* Two-level filtering scheme */
        for (i=0; i< JIT_TRACE_THRESH_FILTER_SIZE; i++) {
            if (interpState->pc == interpState->threshFilter[i]) {
                break;
            }
        }
        if (i == JIT_TRACE_THRESH_FILTER_SIZE) {
            /*
             * Use random replacement policy - otherwise we could miss a large
             * loop that contains more traces than the size of our filter array.
             */
            i = rand() % JIT_TRACE_THRESH_FILTER_SIZE;
            interpState->threshFilter[i] = interpState->pc;
            res = true;
        }
        /*
         * If the compiler is backlogged, or if a debugger or profiler is
         * active, cancel any JIT actions
         */
        if ( res || (gDvmJit.compilerQueueLength >= gDvmJit.compilerHighWater) ||
              gDvm.debuggerActive || self->suspendCount
#if defined(WITH_PROFILER)
                 || gDvm.activeProfilers
#endif
                                             ) {
            if (interpState->jitState != kJitOff) {
                interpState->jitState = kJitNormal;
            }
        } else if (interpState->jitState == kJitTSelectRequest) {
            JitEntry *slot = dvmJitLookupAndAdd(interpState->pc);
            if (slot == NULL) {
                /*
                 * Table is full.  This should have been
                 * detected by the compiler thread and the table
                 * resized before we run into it here.  Assume bad things
                 * are afoot and disable profiling.
                 */
                interpState->jitState = kJitTSelectAbort;
                LOGD("JIT: JitTable full, disabling profiling");
                dvmJitStopTranslationRequests();
            } else if (slot->u.info.traceRequested) {
                /* Trace already requested - revert to interpreter */
                interpState->jitState = kJitTSelectAbort;
            } else {
                /* Mark request */
                JitEntryInfoUnion oldValue;
                JitEntryInfoUnion newValue;
                do {
                    oldValue = slot->u;
                    newValue = oldValue;
                    newValue.info.traceRequested = true;
                } while (!ATOMIC_CMP_SWAP( &slot->u.infoWord,
                         oldValue.infoWord, newValue.infoWord));
            }
        }
        switch (interpState->jitState) {
            case kJitTSelectRequest:
                 interpState->jitState = kJitTSelect;
                 interpState->currTraceHead = interpState->pc;
                 interpState->currTraceRun = 0;
                 interpState->totalTraceLen = 0;
                 interpState->currRunHead = interpState->pc;
                 interpState->currRunLen = 0;
                 interpState->trace[0].frag.startOffset =
                       interpState->pc - interpState->method->insns;
                 interpState->trace[0].frag.numInsts = 0;
                 interpState->trace[0].frag.runEnd = false;
                 interpState->trace[0].frag.hint = kJitHintNone;
                 break;
            case kJitTSelect:
            case kJitTSelectAbort:
                 res = true;
            case kJitSingleStep:
            case kJitSingleStepEnd:
            case kJitOff:
            case kJitNormal:
                break;
            default:
                dvmAbort();
        }
    }
    return res;
}

/*
 * Resizes the JitTable.  Must be a power of 2, and returns true on failure.
 * Stops all threads, and thus is a heavyweight operation.
 */
bool dvmJitResizeJitTable( unsigned int size )
{
    JitEntry *pNewTable;
    JitEntry *pOldTable;
    u4 newMask;
    unsigned int oldSize;
    unsigned int i;

    assert(gDvm.pJitEntryTable != NULL);
    assert(size && !(size & (size - 1)));   /* Is power of 2? */

    LOGD("Jit: resizing JitTable from %d to %d", gDvmJit.jitTableSize, size);

    newMask = size - 1;

    if (size <= gDvmJit.jitTableSize) {
        return true;
    }

    pNewTable = (JitEntry*)calloc(size, sizeof(*pNewTable));
    if (pNewTable == NULL) {
        return true;
    }
    for (i=0; i< size; i++) {
        pNewTable[i].u.info.chain = size;  /* Initialize chain termination */
    }

    /* Stop all other interpreting/jit'ng threads */
    dvmSuspendAllThreads(SUSPEND_FOR_JIT);

    pOldTable = gDvmJit.pJitEntryTable;
    oldSize = gDvmJit.jitTableSize;

    dvmLockMutex(&gDvmJit.tableLock);
    gDvmJit.pJitEntryTable = pNewTable;
    gDvmJit.jitTableSize = size;
    gDvmJit.jitTableMask = size - 1;
    gDvmJit.jitTableEntriesUsed = 0;
    dvmUnlockMutex(&gDvmJit.tableLock);

    for (i=0; i < oldSize; i++) {
        if (pOldTable[i].dPC) {
            JitEntry *p;
            u2 chain;
            p = dvmJitLookupAndAdd(pOldTable[i].dPC);
            p->dPC = pOldTable[i].dPC;
            /*
             * Compiler thread may have just updated the new entry's
             * code address field, so don't blindly copy null.
             */
            if (pOldTable[i].codeAddress != NULL) {
                p->codeAddress = pOldTable[i].codeAddress;
            }
            /* We need to preserve the new chain field, but copy the rest */
            dvmLockMutex(&gDvmJit.tableLock);
            chain = p->u.info.chain;
            p->u = pOldTable[i].u;
            p->u.info.chain = chain;
            dvmUnlockMutex(&gDvmJit.tableLock);
        }
    }

    free(pOldTable);

    /* Restart the world */
    dvmResumeAllThreads(SUSPEND_FOR_JIT);

    return false;
}

/*
 * Float/double conversion requires clamping to min and max of integer form.  If
 * target doesn't support this normally, use these.
 */
s8 dvmJitd2l(double d)
{
    static const double kMaxLong = (double)0x7fffffffffffffffULL;
    static const double kMinLong = (double)0x8000000000000000ULL;
    if (d >= kMaxLong)
        return 0x7fffffffffffffffULL;
    else if (d <= kMinLong)
        return 0x8000000000000000ULL;
    else if (d != d) // NaN case
        return 0;
    else
        return (s8)d;
}

s8 dvmJitf2l(float f)
{
    static const float kMaxLong = (float)0x7fffffffffffffffULL;
    static const float kMinLong = (float)0x8000000000000000ULL;
    if (f >= kMaxLong)
        return 0x7fffffffffffffffULL;
    else if (f <= kMinLong)
        return 0x8000000000000000ULL;
    else if (f != f) // NaN case
        return 0;
    else
        return (s8)f;
}


#endif /* WITH_JIT */
