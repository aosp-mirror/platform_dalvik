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


#include "libdex/OpCodeNames.h"
#include <unistd.h>
#include <pthread.h>
#include <sys/time.h>
#include <signal.h>
#include "compiler/Compiler.h"
#include "compiler/CompilerUtility.h"
#include "compiler/CompilerIR.h"
#include <errno.h>

#if defined(WITH_SELF_VERIFICATION)
/* Allocate space for per-thread ShadowSpace data structures */
void* dvmSelfVerificationShadowSpaceAlloc(Thread* self)
{
    self->shadowSpace = (ShadowSpace*) calloc(1, sizeof(ShadowSpace));
    if (self->shadowSpace == NULL)
        return NULL;

    self->shadowSpace->registerSpaceSize = REG_SPACE;
    self->shadowSpace->registerSpace =
        (int*) calloc(self->shadowSpace->registerSpaceSize, sizeof(int));

    return self->shadowSpace->registerSpace;
}

/* Free per-thread ShadowSpace data structures */
void dvmSelfVerificationShadowSpaceFree(Thread* self)
{
    free(self->shadowSpace->registerSpace);
    free(self->shadowSpace);
}

/*
 * Save out PC, FP, InterpState, and registers to shadow space.
 * Return a pointer to the shadow space for JIT to use.
 */
void* dvmSelfVerificationSaveState(const u2* pc, const void* fp,
                                   InterpState* interpState, int targetTrace)
{
    Thread *self = dvmThreadSelf();
    ShadowSpace *shadowSpace = self->shadowSpace;
    unsigned preBytes = interpState->method->outsSize*4 + sizeof(StackSaveArea);
    unsigned postBytes = interpState->method->registersSize*4;

    //LOGD("### selfVerificationSaveState(%d) pc: 0x%x fp: 0x%x",
    //    self->threadId, (int)pc, (int)fp);

    if (shadowSpace->selfVerificationState != kSVSIdle) {
        LOGD("~~~ Save: INCORRECT PREVIOUS STATE(%d): %d",
            self->threadId, shadowSpace->selfVerificationState);
        LOGD("********** SHADOW STATE DUMP **********");
        LOGD("PC: 0x%x FP: 0x%x", (int)pc, (int)fp);
    }
    shadowSpace->selfVerificationState = kSVSStart;

    if (interpState->entryPoint == kInterpEntryResume) {
        interpState->entryPoint = kInterpEntryInstr;
#if 0
        /* Tracking the success rate of resume after single-stepping */
        if (interpState->jitResumeDPC == pc) {
            LOGD("SV single step resumed at %p", pc);
        }
        else {
            LOGD("real %p DPC %p NPC %p", pc, interpState->jitResumeDPC,
                 interpState->jitResumeNPC);
        }
#endif
    }

    // Dynamically grow shadow register space if necessary
    if (preBytes + postBytes > shadowSpace->registerSpaceSize * sizeof(u4)) {
        free(shadowSpace->registerSpace);
        shadowSpace->registerSpaceSize = (preBytes + postBytes) / sizeof(u4);
        shadowSpace->registerSpace =
            (int*) calloc(shadowSpace->registerSpaceSize, sizeof(u4));
    }

    // Remember original state
    shadowSpace->startPC = pc;
    shadowSpace->fp = fp;
    shadowSpace->glue = interpState;
    /*
     * Store the original method here in case the trace ends with a
     * return/invoke, the last method.
     */
    shadowSpace->method = interpState->method;
    shadowSpace->shadowFP = shadowSpace->registerSpace +
                            shadowSpace->registerSpaceSize - postBytes/4;

    // Create a copy of the InterpState
    memcpy(&(shadowSpace->interpState), interpState, sizeof(InterpState));
    shadowSpace->interpState.fp = shadowSpace->shadowFP;
    shadowSpace->interpState.interpStackEnd = (u1*)shadowSpace->registerSpace;

    // Create a copy of the stack
    memcpy(((char*)shadowSpace->shadowFP)-preBytes, ((char*)fp)-preBytes,
        preBytes+postBytes);

    // Setup the shadowed heap space
    shadowSpace->heapSpaceTail = shadowSpace->heapSpace;

    // Reset trace length
    shadowSpace->traceLength = 0;

    return shadowSpace;
}

/*
 * Save ending PC, FP and compiled code exit point to shadow space.
 * Return a pointer to the shadow space for JIT to restore state.
 */
void* dvmSelfVerificationRestoreState(const u2* pc, const void* fp,
                                      SelfVerificationState exitState)
{
    Thread *self = dvmThreadSelf();
    ShadowSpace *shadowSpace = self->shadowSpace;
    // Official InterpState structure
    InterpState *realGlue = shadowSpace->glue;
    shadowSpace->endPC = pc;
    shadowSpace->endShadowFP = fp;
    shadowSpace->jitExitState = exitState;

    //LOGD("### selfVerificationRestoreState(%d) pc: 0x%x fp: 0x%x endPC: 0x%x",
    //    self->threadId, (int)shadowSpace->startPC, (int)shadowSpace->fp,
    //    (int)pc);

    if (shadowSpace->selfVerificationState != kSVSStart) {
        LOGD("~~~ Restore: INCORRECT PREVIOUS STATE(%d): %d",
            self->threadId, shadowSpace->selfVerificationState);
        LOGD("********** SHADOW STATE DUMP **********");
        LOGD("Dalvik PC: 0x%x endPC: 0x%x", (int)shadowSpace->startPC,
            (int)shadowSpace->endPC);
        LOGD("Interp FP: 0x%x", (int)shadowSpace->fp);
        LOGD("Shadow FP: 0x%x endFP: 0x%x", (int)shadowSpace->shadowFP,
            (int)shadowSpace->endShadowFP);
    }

    // Move the resume [ND]PC from the shadow space to the real space so that
    // the debug interpreter can return to the translation
    if (exitState == kSVSSingleStep) {
        realGlue->jitResumeNPC = shadowSpace->interpState.jitResumeNPC;
        realGlue->jitResumeDPC = shadowSpace->interpState.jitResumeDPC;
    } else {
        realGlue->jitResumeNPC = NULL;
        realGlue->jitResumeDPC = NULL;
    }

    // Special case when punting after a single instruction
    if (exitState == kSVSPunt && pc == shadowSpace->startPC) {
        shadowSpace->selfVerificationState = kSVSIdle;
    } else if (exitState == kSVSBackwardBranch && pc < shadowSpace->startPC) {
        /*
         * Consider a trace with a backward branch:
         *   1: ..
         *   2: ..
         *   3: ..
         *   4: ..
         *   5: Goto {1 or 2 or 3 or 4}
         *
         * If there instruction 5 goes to 1 and there is no single-step
         * instruction in the loop, pc is equal to shadowSpace->startPC and
         * we will honor the backward branch condition.
         *
         * If the single-step instruction is outside the loop, then after
         * resuming in the trace the startPC will be less than pc so we will
         * also honor the backward branch condition.
         *
         * If the single-step is inside the loop, we won't hit the same endPC
         * twice when the interpreter is re-executing the trace so we want to
         * cancel the backward branch condition. In this case it can be
         * detected as the endPC (ie pc) will be less than startPC.
         */
        shadowSpace->selfVerificationState = kSVSNormal;
    } else {
        shadowSpace->selfVerificationState = exitState;
    }

    return shadowSpace;
}

/* Print contents of virtual registers */
static void selfVerificationPrintRegisters(int* addr, int* addrRef,
                                           int numWords)
{
    int i;
    for (i = 0; i < numWords; i++) {
        LOGD("(v%d) 0x%8x%s", i, addr[i], addr[i] != addrRef[i] ? " X" : "");
    }
}

/* Print values maintained in shadowSpace */
static void selfVerificationDumpState(const u2* pc, Thread* self)
{
    ShadowSpace* shadowSpace = self->shadowSpace;
    StackSaveArea* stackSave = SAVEAREA_FROM_FP(self->curFrame);
    int frameBytes = (int) shadowSpace->registerSpace +
                     shadowSpace->registerSpaceSize*4 -
                     (int) shadowSpace->shadowFP;
    int localRegs = 0;
    int frameBytes2 = 0;
    if (self->curFrame < shadowSpace->fp) {
        localRegs = (stackSave->method->registersSize -
                     stackSave->method->insSize)*4;
        frameBytes2 = (int) shadowSpace->fp - (int) self->curFrame - localRegs;
    }
    LOGD("********** SHADOW STATE DUMP **********");
    LOGD("CurrentPC: 0x%x, Offset: 0x%04x", (int)pc,
        (int)(pc - stackSave->method->insns));
    LOGD("Class: %s", shadowSpace->method->clazz->descriptor);
    LOGD("Method: %s", shadowSpace->method->name);
    LOGD("Dalvik PC: 0x%x endPC: 0x%x", (int)shadowSpace->startPC,
        (int)shadowSpace->endPC);
    LOGD("Interp FP: 0x%x endFP: 0x%x", (int)shadowSpace->fp,
        (int)self->curFrame);
    LOGD("Shadow FP: 0x%x endFP: 0x%x", (int)shadowSpace->shadowFP,
        (int)shadowSpace->endShadowFP);
    LOGD("Frame1 Bytes: %d Frame2 Local: %d Bytes: %d", frameBytes,
        localRegs, frameBytes2);
    LOGD("Trace length: %d State: %d", shadowSpace->traceLength,
        shadowSpace->selfVerificationState);
}

/* Print decoded instructions in the current trace */
static void selfVerificationDumpTrace(const u2* pc, Thread* self)
{
    ShadowSpace* shadowSpace = self->shadowSpace;
    StackSaveArea* stackSave = SAVEAREA_FROM_FP(self->curFrame);
    int i, addr, offset;
    DecodedInstruction *decInsn;

    LOGD("********** SHADOW TRACE DUMP **********");
    for (i = 0; i < shadowSpace->traceLength; i++) {
        addr = shadowSpace->trace[i].addr;
        offset =  (int)((u2*)addr - stackSave->method->insns);
        decInsn = &(shadowSpace->trace[i].decInsn);
        /* Not properly decoding instruction, some registers may be garbage */
        LOGD("0x%x: (0x%04x) %s",
            addr, offset, dexGetOpcodeName(decInsn->opCode));
    }
}

/* Code is forced into this spin loop when a divergence is detected */
static void selfVerificationSpinLoop(ShadowSpace *shadowSpace)
{
    const u2 *startPC = shadowSpace->startPC;
    JitTraceDescription* desc = dvmCopyTraceDescriptor(startPC, NULL);
    if (desc) {
        dvmCompilerWorkEnqueue(startPC, kWorkOrderTraceDebug, desc);
        /*
         * This function effectively terminates the VM right here, so not
         * freeing the desc pointer when the enqueuing fails is acceptable.
         */
    }
    gDvmJit.selfVerificationSpin = true;
    while(gDvmJit.selfVerificationSpin) sleep(10);
}

/* Manage self verification while in the debug interpreter */
static bool selfVerificationDebugInterp(const u2* pc, Thread* self,
                                        InterpState *interpState)
{
    ShadowSpace *shadowSpace = self->shadowSpace;
    SelfVerificationState state = shadowSpace->selfVerificationState;

    DecodedInstruction decInsn;
    dexDecodeInstruction(gDvm.instrFormat, pc, &decInsn);

    //LOGD("### DbgIntp(%d): PC: 0x%x endPC: 0x%x state: %d len: %d %s",
    //    self->threadId, (int)pc, (int)shadowSpace->endPC, state,
    //    shadowSpace->traceLength, dexGetOpcodeName(decInsn.opCode));

    if (state == kSVSIdle || state == kSVSStart) {
        LOGD("~~~ DbgIntrp: INCORRECT PREVIOUS STATE(%d): %d",
            self->threadId, state);
        selfVerificationDumpState(pc, self);
        selfVerificationDumpTrace(pc, self);
    }

    /*
     * Skip endPC once when trace has a backward branch. If the SV state is
     * single step, keep it that way.
     */
    if ((state == kSVSBackwardBranch && pc == shadowSpace->endPC) ||
        (state != kSVSBackwardBranch && state != kSVSSingleStep)) {
        shadowSpace->selfVerificationState = kSVSDebugInterp;
    }

    /* Check that the current pc is the end of the trace */
    if ((state == kSVSDebugInterp || state == kSVSSingleStep) &&
        pc == shadowSpace->endPC) {

        shadowSpace->selfVerificationState = kSVSIdle;

        /* Check register space */
        int frameBytes = (int) shadowSpace->registerSpace +
                         shadowSpace->registerSpaceSize*4 -
                         (int) shadowSpace->shadowFP;
        if (memcmp(shadowSpace->fp, shadowSpace->shadowFP, frameBytes)) {
            LOGD("~~~ DbgIntp(%d): REGISTERS DIVERGENCE!", self->threadId);
            selfVerificationDumpState(pc, self);
            selfVerificationDumpTrace(pc, self);
            LOGD("*** Interp Registers: addr: 0x%x bytes: %d",
                (int)shadowSpace->fp, frameBytes);
            selfVerificationPrintRegisters((int*)shadowSpace->fp,
                                           (int*)shadowSpace->shadowFP,
                                           frameBytes/4);
            LOGD("*** Shadow Registers: addr: 0x%x bytes: %d",
                (int)shadowSpace->shadowFP, frameBytes);
            selfVerificationPrintRegisters((int*)shadowSpace->shadowFP,
                                           (int*)shadowSpace->fp,
                                           frameBytes/4);
            selfVerificationSpinLoop(shadowSpace);
        }
        /* Check new frame if it exists (invokes only) */
        if (self->curFrame < shadowSpace->fp) {
            StackSaveArea* stackSave = SAVEAREA_FROM_FP(self->curFrame);
            int localRegs = (stackSave->method->registersSize -
                             stackSave->method->insSize)*4;
            int frameBytes2 = (int) shadowSpace->fp -
                              (int) self->curFrame - localRegs;
            if (memcmp(((char*)self->curFrame)+localRegs,
                ((char*)shadowSpace->endShadowFP)+localRegs, frameBytes2)) {
                LOGD("~~~ DbgIntp(%d): REGISTERS (FRAME2) DIVERGENCE!",
                    self->threadId);
                selfVerificationDumpState(pc, self);
                selfVerificationDumpTrace(pc, self);
                LOGD("*** Interp Registers: addr: 0x%x l: %d bytes: %d",
                    (int)self->curFrame, localRegs, frameBytes2);
                selfVerificationPrintRegisters((int*)self->curFrame,
                                               (int*)shadowSpace->endShadowFP,
                                               (frameBytes2+localRegs)/4);
                LOGD("*** Shadow Registers: addr: 0x%x l: %d bytes: %d",
                    (int)shadowSpace->endShadowFP, localRegs, frameBytes2);
                selfVerificationPrintRegisters((int*)shadowSpace->endShadowFP,
                                               (int*)self->curFrame,
                                               (frameBytes2+localRegs)/4);
                selfVerificationSpinLoop(shadowSpace);
            }
        }

        /* Check memory space */
        bool memDiff = false;
        ShadowHeap* heapSpacePtr;
        for (heapSpacePtr = shadowSpace->heapSpace;
             heapSpacePtr != shadowSpace->heapSpaceTail; heapSpacePtr++) {
            int memData = *((unsigned int*) heapSpacePtr->addr);
            if (heapSpacePtr->data != memData) {
                LOGD("~~~ DbgIntp(%d): MEMORY DIVERGENCE!", self->threadId);
                LOGD("Addr: 0x%x Intrp Data: 0x%x Jit Data: 0x%x",
                    heapSpacePtr->addr, memData, heapSpacePtr->data);
                selfVerificationDumpState(pc, self);
                selfVerificationDumpTrace(pc, self);
                memDiff = true;
            }
        }
        if (memDiff) selfVerificationSpinLoop(shadowSpace);

        /*
         * Switch to JIT single step mode to stay in the debug interpreter for
         * one more instruction
         */
        if (state == kSVSSingleStep) {
            interpState->jitState = kJitSingleStepEnd;
        }
        return true;

    /* If end not been reached, make sure max length not exceeded */
    } else if (shadowSpace->traceLength >= JIT_MAX_TRACE_LEN) {
        LOGD("~~~ DbgIntp(%d): CONTROL DIVERGENCE!", self->threadId);
        LOGD("startPC: 0x%x endPC: 0x%x currPC: 0x%x",
            (int)shadowSpace->startPC, (int)shadowSpace->endPC, (int)pc);
        selfVerificationDumpState(pc, self);
        selfVerificationDumpTrace(pc, self);
        selfVerificationSpinLoop(shadowSpace);

        return true;
    }
    /* Log the instruction address and decoded instruction for debug */
    shadowSpace->trace[shadowSpace->traceLength].addr = (int)pc;
    shadowSpace->trace[shadowSpace->traceLength].decInsn = decInsn;
    shadowSpace->traceLength++;

    return false;
}
#endif

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
    gDvmJit.pProfTable = NULL;
}

#if defined(WITH_JIT_TUNING)
/* Convenience function to increment counter from assembly code */
void dvmBumpNoChain(int from)
{
    gDvmJit.noChainExit[from]++;
}

/* Convenience function to increment counter from assembly code */
void dvmBumpNormal()
{
    gDvmJit.normalExit++;
}

/* Convenience function to increment counter from assembly code */
void dvmBumpPunt(int from)
{
    gDvmJit.puntExit++;
}
#endif

/* Dumps debugging & tuning stats to the log */
void dvmJitStats()
{
    int i;
    int hit;
    int not_hit;
    int chains;
    int stubs;
    if (gDvmJit.pJitEntryTable) {
        for (i=0, stubs=chains=hit=not_hit=0;
             i < (int) gDvmJit.jitTableSize;
             i++) {
            if (gDvmJit.pJitEntryTable[i].dPC != 0) {
                hit++;
                if (gDvmJit.pJitEntryTable[i].codeAddress ==
                      dvmCompilerGetInterpretTemplate())
                    stubs++;
            } else
                not_hit++;
            if (gDvmJit.pJitEntryTable[i].u.info.chain != gDvmJit.jitTableSize)
                chains++;
        }
        LOGD("JIT: table size is %d, entries used is %d",
             gDvmJit.jitTableSize,  gDvmJit.jitTableEntriesUsed);
        LOGD("JIT: %d traces, %d slots, %d chains, %d thresh, %s",
             hit, not_hit + hit, chains, gDvmJit.threshold,
             gDvmJit.blockingMode ? "Blocking" : "Non-blocking");

#if defined(WITH_JIT_TUNING)
        LOGD("JIT: Code cache patches: %d", gDvmJit.codeCachePatches);

        LOGD("JIT: Lookups: %d hits, %d misses; %d normal, %d punt",
             gDvmJit.addrLookupsFound, gDvmJit.addrLookupsNotFound,
             gDvmJit.normalExit, gDvmJit.puntExit);

        LOGD("JIT: ICHits: %d", gDvmICHitCount);

        LOGD("JIT: noChainExit: %d IC miss, %d interp callsite, "
             "%d switch overflow",
             gDvmJit.noChainExit[kInlineCacheMiss],
             gDvmJit.noChainExit[kCallsiteInterpreted],
             gDvmJit.noChainExit[kSwitchOverflow]);

        LOGD("JIT: ICPatch: %d init, %d rejected, %d lock-free, %d queued, "
             "%d dropped",
             gDvmJit.icPatchInit, gDvmJit.icPatchRejected,
             gDvmJit.icPatchLockFree, gDvmJit.icPatchQueued,
             gDvmJit.icPatchDropped);

        LOGD("JIT: Invoke: %d mono, %d poly, %d native, %d return",
             gDvmJit.invokeMonomorphic, gDvmJit.invokePolymorphic,
             gDvmJit.invokeNative, gDvmJit.returnOp);
        LOGD("JIT: Inline: %d mgetter, %d msetter, %d pgetter, %d psetter",
             gDvmJit.invokeMonoGetterInlined, gDvmJit.invokeMonoSetterInlined,
             gDvmJit.invokePolyGetterInlined, gDvmJit.invokePolySetterInlined);
        LOGD("JIT: Total compilation time: %llu ms", gDvmJit.jitTime / 1000);
        LOGD("JIT: Avg unit compilation time: %llu us",
             gDvmJit.jitTime / gDvmJit.numCompilations);
#endif

        LOGD("JIT: %d Translation chains, %d interp stubs",
             gDvmJit.translationChains, stubs);
        if (gDvmJit.profile) {
            dvmCompilerSortAndPrintTraceProfiles();
        }
    }
}


void setTraceConstruction(JitEntry *slot, bool value)
{

    JitEntryInfoUnion oldValue;
    JitEntryInfoUnion newValue;
    do {
        oldValue = slot->u;
        newValue = oldValue;
        newValue.info.traceConstruction = value;
    } while (android_atomic_release_cas(oldValue.infoWord, newValue.infoWord,
            &slot->u.infoWord) != 0);
}

void resetTracehead(InterpState* interpState, JitEntry *slot)
{
    slot->codeAddress = dvmCompilerGetInterpretTemplate();
    setTraceConstruction(slot, false);
}

/* Clean up any pending trace builds */
void dvmJitAbortTraceSelect(InterpState* interpState)
{
    if (interpState->jitState == kJitTSelect)
        interpState->jitState = kJitDone;
}

/*
 * Find an entry in the JitTable, creating if necessary.
 * Returns null if table is full.
 */
static JitEntry *lookupAndAdd(const u2* dPC, bool callerLocked)
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
        if (!callerLocked)
            dvmLockMutex(&gDvmJit.tableLock);
        /*
         * At this point, if .dPC is NULL, then the slot we're
         * looking at is the target slot from the primary hash
         * (the simple, and common case).  Otherwise we're going
         * to have to find a free slot and chain it.
         */
        ANDROID_MEMBAR_FULL(); /* Make sure we reload [].dPC after lock */
        if (gDvmJit.pJitEntryTable[idx].dPC != NULL) {
            u4 prev;
            while (gDvmJit.pJitEntryTable[idx].u.info.chain != chainEndMarker) {
                if (gDvmJit.pJitEntryTable[idx].dPC == dPC) {
                    /* Another thread got there first for this dPC */
                    if (!callerLocked)
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
                } while (android_atomic_release_cas(oldValue.infoWord,
                        newValue.infoWord,
                        &gDvmJit.pJitEntryTable[prev].u.infoWord) != 0);
            }
        }
        if (gDvmJit.pJitEntryTable[idx].dPC == NULL) {
            /*
             * Initialize codeAddress and allocate the slot.  Must
             * happen in this order (since dPC is set, the entry is live.
             */
            gDvmJit.pJitEntryTable[idx].dPC = dPC;
            gDvmJit.jitTableEntriesUsed++;
        } else {
            /* Table is full */
            idx = chainEndMarker;
        }
        if (!callerLocked)
            dvmUnlockMutex(&gDvmJit.tableLock);
    }
    return (idx == chainEndMarker) ? NULL : &gDvmJit.pJitEntryTable[idx];
}

/*
 * Append the class ptr of "this" and the current method ptr to the current
 * trace. That is, the trace runs will contain the following components:
 *  + trace run that ends with an invoke (existing entry)
 *  + thisClass (new)
 *  + calleeMethod (new)
 */
static void insertClassMethodInfo(InterpState* interpState,
                                  const ClassObject* thisClass,
                                  const Method* calleeMethod,
                                  const DecodedInstruction* insn)
{
    int currTraceRun = ++interpState->currTraceRun;
    interpState->trace[currTraceRun].meta = (void *) thisClass;
    currTraceRun = ++interpState->currTraceRun;
    interpState->trace[currTraceRun].meta = (void *) calleeMethod;
}

/*
 * Check if the next instruction following the invoke is a move-result and if
 * so add it to the trace. That is, this will add the trace run that includes
 * the move-result to the trace list.
 *
 *  + trace run that ends with an invoke (existing entry)
 *  + thisClass (existing entry)
 *  + calleeMethod (existing entry)
 *  + move result (new)
 *
 * lastPC, len, offset are all from the preceding invoke instruction
 */
static void insertMoveResult(const u2 *lastPC, int len, int offset,
                             InterpState *interpState)
{
    DecodedInstruction nextDecInsn;
    const u2 *moveResultPC = lastPC + len;

    dexDecodeInstruction(gDvm.instrFormat, moveResultPC, &nextDecInsn);
    if ((nextDecInsn.opCode != OP_MOVE_RESULT) &&
        (nextDecInsn.opCode != OP_MOVE_RESULT_WIDE) &&
        (nextDecInsn.opCode != OP_MOVE_RESULT_OBJECT))
        return;

    /* We need to start a new trace run */
    int currTraceRun = ++interpState->currTraceRun;
    interpState->currRunHead = moveResultPC;
    interpState->trace[currTraceRun].frag.startOffset = offset + len;
    interpState->trace[currTraceRun].frag.numInsts = 1;
    interpState->trace[currTraceRun].frag.runEnd = false;
    interpState->trace[currTraceRun].frag.hint = kJitHintNone;
    interpState->trace[currTraceRun].frag.isCode = true;
    interpState->totalTraceLen++;

    interpState->currRunLen = dexGetInstrOrTableWidthAbs(gDvm.instrWidth,
                                                         moveResultPC);
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
int dvmCheckJit(const u2* pc, Thread* self, InterpState* interpState,
                const ClassObject* thisClass, const Method* curMethod)
{
    int flags, len;
    int switchInterp = false;
    bool debugOrProfile = dvmDebuggerOrProfilerActive();
    /* Stay in the dbg interpreter for the next instruction */
    bool stayOneMoreInst = false;

    /*
     * Bug 2710533 - dalvik crash when disconnecting debugger
     *
     * Reset the entry point to the default value. If needed it will be set to a
     * specific value in the corresponding case statement (eg kJitSingleStepEnd)
     */
    interpState->entryPoint = kInterpEntryInstr;

    /* Prepare to handle last PC and stage the current PC */
    const u2 *lastPC = interpState->lastPC;
    interpState->lastPC = pc;

    switch (interpState->jitState) {
        int offset;
        DecodedInstruction decInsn;
        case kJitTSelect:
            /* First instruction - just remember the PC and exit */
            if (lastPC == NULL) break;
            /* Grow the trace around the last PC if jitState is kJitTSelect */
            dexDecodeInstruction(gDvm.instrFormat, lastPC, &decInsn);

            /*
             * Treat {PACKED,SPARSE}_SWITCH as trace-ending instructions due
             * to the amount of space it takes to generate the chaining
             * cells.
             */
            if (interpState->totalTraceLen != 0 &&
                (decInsn.opCode == OP_PACKED_SWITCH ||
                 decInsn.opCode == OP_SPARSE_SWITCH)) {
                interpState->jitState = kJitTSelectEnd;
                break;
            }


#if defined(SHOW_TRACE)
            LOGD("TraceGen: adding %s", dexGetOpcodeName(decInsn.opCode));
#endif
            flags = dexGetInstrFlags(gDvm.instrFlags, decInsn.opCode);
            len = dexGetInstrOrTableWidthAbs(gDvm.instrWidth, lastPC);
            offset = lastPC - interpState->method->insns;
            assert((unsigned) offset <
                   dvmGetMethodInsnsSize(interpState->method));
            if (lastPC != interpState->currRunHead + interpState->currRunLen) {
                int currTraceRun;
                /* We need to start a new trace run */
                currTraceRun = ++interpState->currTraceRun;
                interpState->currRunLen = 0;
                interpState->currRunHead = (u2*)lastPC;
                interpState->trace[currTraceRun].frag.startOffset = offset;
                interpState->trace[currTraceRun].frag.numInsts = 0;
                interpState->trace[currTraceRun].frag.runEnd = false;
                interpState->trace[currTraceRun].frag.hint = kJitHintNone;
                interpState->trace[currTraceRun].frag.isCode = true;
            }
            interpState->trace[interpState->currTraceRun].frag.numInsts++;
            interpState->totalTraceLen++;
            interpState->currRunLen += len;

            /*
             * If the last instruction is an invoke, we will try to sneak in
             * the move-result* (if existent) into a separate trace run.
             */
            int needReservedRun = (flags & kInstrInvoke) ? 1 : 0;

            /* Will probably never hit this with the current trace buildier */
            if (interpState->currTraceRun ==
                (MAX_JIT_RUN_LEN - 1 - needReservedRun)) {
                interpState->jitState = kJitTSelectEnd;
            }

            if (  ((flags & kInstrUnconditional) == 0) &&
                  /* don't end trace on INVOKE_DIRECT_EMPTY  */
                  (decInsn.opCode != OP_INVOKE_DIRECT_EMPTY) &&
                  ((flags & (kInstrCanBranch |
                             kInstrCanSwitch |
                             kInstrCanReturn |
                             kInstrInvoke)) != 0)) {
                    interpState->jitState = kJitTSelectEnd;
#if defined(SHOW_TRACE)
                LOGD("TraceGen: ending on %s, basic block end",
                     dexGetOpcodeName(decInsn.opCode));
#endif

                /*
                 * If the current invoke is a {virtual,interface}, get the
                 * current class/method pair into the trace as well.
                 * If the next instruction is a variant of move-result, insert
                 * it to the trace too.
                 */
                if (flags & kInstrInvoke) {
                    insertClassMethodInfo(interpState, thisClass, curMethod,
                                          &decInsn);
                    insertMoveResult(lastPC, len, offset, interpState);
                }
            }
            /* Break on throw or self-loop */
            if ((decInsn.opCode == OP_THROW) || (lastPC == pc)){
                interpState->jitState = kJitTSelectEnd;
            }
            if (interpState->totalTraceLen >= JIT_MAX_TRACE_LEN) {
                interpState->jitState = kJitTSelectEnd;
            }
             /* Abandon the trace request if debugger/profiler is attached */
            if (debugOrProfile) {
                interpState->jitState = kJitDone;
                break;
            }
            if ((flags & kInstrCanReturn) != kInstrCanReturn) {
                break;
            }
            else {
                /*
                 * Last instruction is a return - stay in the dbg interpreter
                 * for one more instruction if it is a non-void return, since
                 * we don't want to start a trace with move-result as the first
                 * instruction (which is already included in the trace
                 * containing the invoke.
                 */
                if (decInsn.opCode != OP_RETURN_VOID) {
                    stayOneMoreInst = true;
                }
            }
            /* NOTE: intentional fallthrough for returns */
        case kJitTSelectEnd:
            {
                /* Bad trace */
                if (interpState->totalTraceLen == 0) {
                    /* Bad trace - mark as untranslatable */
                    interpState->jitState = kJitDone;
                    switchInterp = true;
                    break;
                }

                int lastTraceDesc = interpState->currTraceRun;

                /* Extend a new empty desc if the last slot is meta info */
                if (!interpState->trace[lastTraceDesc].frag.isCode) {
                    lastTraceDesc = ++interpState->currTraceRun;
                    interpState->trace[lastTraceDesc].frag.startOffset = 0;
                    interpState->trace[lastTraceDesc].frag.numInsts = 0;
                    interpState->trace[lastTraceDesc].frag.hint = kJitHintNone;
                    interpState->trace[lastTraceDesc].frag.isCode = true;
                }

                /* Mark the end of the trace runs */
                interpState->trace[lastTraceDesc].frag.runEnd = true;

                JitTraceDescription* desc =
                   (JitTraceDescription*)malloc(sizeof(JitTraceDescription) +
                     sizeof(JitTraceRun) * (interpState->currTraceRun+1));

                if (desc == NULL) {
                    LOGE("Out of memory in trace selection");
                    dvmJitStopTranslationRequests();
                    interpState->jitState = kJitDone;
                    switchInterp = true;
                    break;
                }

                desc->method = interpState->method;
                memcpy((char*)&(desc->trace[0]),
                    (char*)&(interpState->trace[0]),
                    sizeof(JitTraceRun) * (interpState->currTraceRun+1));
#if defined(SHOW_TRACE)
                LOGD("TraceGen:  trace done, adding to queue");
#endif
                if (dvmCompilerWorkEnqueue(
                       interpState->currTraceHead,kWorkOrderTrace,desc)) {
                    /* Work order successfully enqueued */
                    if (gDvmJit.blockingMode) {
                        dvmCompilerDrainQueue();
                    }
                } else {
                    /*
                     * Make sure the descriptor for the abandoned work order is
                     * freed.
                     */
                    free(desc);
                }
                /*
                 * Reset "trace in progress" flag whether or not we
                 * successfully entered a work order.
                 */
                JitEntry *jitEntry =
                    lookupAndAdd(interpState->currTraceHead, false);
                if (jitEntry) {
                    setTraceConstruction(jitEntry, false);
                }
                interpState->jitState = kJitDone;
                switchInterp = true;
            }
            break;
        case kJitSingleStep:
            interpState->jitState = kJitSingleStepEnd;
            break;
        case kJitSingleStepEnd:
            /*
             * Clear the inJitCodeCache flag and abandon the resume attempt if
             * we cannot switch back to the translation due to corner-case
             * conditions. If the flag is not cleared and the code cache is full
             * we will be stuck in the debug interpreter as the code cache
             * cannot be reset.
             */
            if (dvmJitStayInPortableInterpreter()) {
                interpState->entryPoint = kInterpEntryInstr;
                self->inJitCodeCache = 0;
            } else {
                interpState->entryPoint = kInterpEntryResume;
            }
            interpState->jitState = kJitDone;
            switchInterp = true;
            break;
        case kJitDone:
            switchInterp = true;
            break;
#if defined(WITH_SELF_VERIFICATION)
        case kJitSelfVerification:
            if (selfVerificationDebugInterp(pc, self, interpState)) {
                /*
                 * If the next state is not single-step end, we can switch
                 * interpreter now.
                 */
                if (interpState->jitState != kJitSingleStepEnd) {
                    interpState->jitState = kJitDone;
                    switchInterp = true;
                }
            }
            break;
#endif
        case kJitNot:
            switchInterp = !debugOrProfile;
            break;
        default:
            LOGE("Unexpected JIT state: %d entry point: %d",
                 interpState->jitState, interpState->entryPoint);
            dvmAbort();
            break;
    }
    /*
     * Final check to see if we can really switch the interpreter. Make sure
     * the jitState is kJitDone or kJitNot when switchInterp is set to true.
     */
     assert(switchInterp == false || interpState->jitState == kJitDone ||
            interpState->jitState == kJitNot);
     return switchInterp && !debugOrProfile && !stayOneMoreInst &&
            !dvmJitStayInPortableInterpreter();
}

JitEntry *dvmFindJitEntry(const u2* pc)
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

/*
 * If a translated code address exists for the davik byte code
 * pointer return it.  This routine needs to be fast.
 */
void* dvmJitGetCodeAddr(const u2* dPC)
{
    int idx = dvmJitHash(dPC);
    const u2* npc = gDvmJit.pJitEntryTable[idx].dPC;
    if (npc != NULL) {
        bool hideTranslation = dvmJitHideTranslation();

        if (npc == dPC) {
#if defined(WITH_JIT_TUNING)
            gDvmJit.addrLookupsFound++;
#endif
            return hideTranslation ?
                NULL : gDvmJit.pJitEntryTable[idx].codeAddress;
        } else {
            int chainEndMarker = gDvmJit.jitTableSize;
            while (gDvmJit.pJitEntryTable[idx].u.info.chain != chainEndMarker) {
                idx = gDvmJit.pJitEntryTable[idx].u.info.chain;
                if (gDvmJit.pJitEntryTable[idx].dPC == dPC) {
#if defined(WITH_JIT_TUNING)
                    gDvmJit.addrLookupsFound++;
#endif
                    return hideTranslation ?
                        NULL : gDvmJit.pJitEntryTable[idx].codeAddress;
                }
            }
        }
    }
#if defined(WITH_JIT_TUNING)
    gDvmJit.addrLookupsNotFound++;
#endif
    return NULL;
}

/*
 * Register the translated code pointer into the JitTable.
 * NOTE: Once a codeAddress field transitions from initial state to
 * JIT'd code, it must not be altered without first halting all
 * threads.  This routine should only be called by the compiler
 * thread.
 */
void dvmJitSetCodeAddr(const u2* dPC, void *nPC, JitInstructionSetType set) {
    JitEntryInfoUnion oldValue;
    JitEntryInfoUnion newValue;
    JitEntry *jitEntry = lookupAndAdd(dPC, false);
    assert(jitEntry);
    /* Note: order of update is important */
    do {
        oldValue = jitEntry->u;
        newValue = oldValue;
        newValue.info.instructionSet = set;
    } while (android_atomic_release_cas(
             oldValue.infoWord, newValue.infoWord,
             &jitEntry->u.infoWord) != 0);
    jitEntry->codeAddress = nPC;
}

/*
 * Determine if valid trace-bulding request is active.  Return true
 * if we need to abort and switch back to the fast interpreter, false
 * otherwise.
 */
bool dvmJitCheckTraceRequest(Thread* self, InterpState* interpState)
{
    bool switchInterp = false;         /* Assume success */
    int i;
    /*
     * A note on trace "hotness" filtering:
     *
     * Our first level trigger is intentionally loose - we need it to
     * fire easily not just to identify potential traces to compile, but
     * also to allow re-entry into the code cache.
     *
     * The 2nd level filter (done here) exists to be selective about
     * what we actually compile.  It works by requiring the same
     * trace head "key" (defined as filterKey below) to appear twice in
     * a relatively short period of time.   The difficulty is defining the
     * shape of the filterKey.  Unfortunately, there is no "one size fits
     * all" approach.
     *
     * For spiky execution profiles dominated by a smallish
     * number of very hot loops, we would want the second-level filter
     * to be very selective.  A good selective filter is requiring an
     * exact match of the Dalvik PC.  In other words, defining filterKey as:
     *     intptr_t filterKey = (intptr_t)interpState->pc
     *
     * However, for flat execution profiles we do best when aggressively
     * translating.  A heuristically decent proxy for this is to use
     * the value of the method pointer containing the trace as the filterKey.
     * Intuitively, this is saying that once any trace in a method appears hot,
     * immediately translate any other trace from that same method that
     * survives the first-level filter.  Here, filterKey would be defined as:
     *     intptr_t filterKey = (intptr_t)interpState->method
     *
     * The problem is that we can't easily detect whether we're dealing
     * with a spiky or flat profile.  If we go with the "pc" match approach,
     * flat profiles perform poorly.  If we go with the loose "method" match,
     * we end up generating a lot of useless translations.  Probably the
     * best approach in the future will be to retain profile information
     * across runs of each application in order to determine it's profile,
     * and then choose once we have enough history.
     *
     * However, for now we've decided to chose a compromise filter scheme that
     * includes elements of both.  The high order bits of the filter key
     * are drawn from the enclosing method, and are combined with a slice
     * of the low-order bits of the Dalvik pc of the trace head.  The
     * looseness of the filter can be adjusted by changing with width of
     * the Dalvik pc slice (JIT_TRACE_THRESH_FILTER_PC_BITS).  The wider
     * the slice, the tighter the filter.
     *
     * Note: the fixed shifts in the function below reflect assumed word
     * alignment for method pointers, and half-word alignment of the Dalvik pc.
     * for method pointers and half-word alignment for dalvik pc.
     */
    u4 methodKey = (u4)interpState->method <<
                   (JIT_TRACE_THRESH_FILTER_PC_BITS - 2);
    u4 pcKey = ((u4)interpState->pc >> 1) &
               ((1 << JIT_TRACE_THRESH_FILTER_PC_BITS) - 1);
    intptr_t filterKey = (intptr_t)(methodKey | pcKey);
    bool debugOrProfile = dvmDebuggerOrProfilerActive();

    /* Check if the JIT request can be handled now */
    if (gDvmJit.pJitEntryTable != NULL && debugOrProfile == false) {
        /* Bypass the filter for hot trace requests or during stress mode */
        if (interpState->jitState == kJitTSelectRequest &&
            gDvmJit.threshold > 6) {
            /* Two-level filtering scheme */
            for (i=0; i< JIT_TRACE_THRESH_FILTER_SIZE; i++) {
                if (filterKey == interpState->threshFilter[i]) {
                    interpState->threshFilter[i] = 0; // Reset filter entry
                    break;
                }
            }
            if (i == JIT_TRACE_THRESH_FILTER_SIZE) {
                /*
                 * Use random replacement policy - otherwise we could miss a
                 * large loop that contains more traces than the size of our
                 * filter array.
                 */
                i = rand() % JIT_TRACE_THRESH_FILTER_SIZE;
                interpState->threshFilter[i] = filterKey;
                interpState->jitState = kJitDone;
            }
        }

        /* If the compiler is backlogged, cancel any JIT actions */
        if (gDvmJit.compilerQueueLength >= gDvmJit.compilerHighWater) {
            interpState->jitState = kJitDone;
        }

        /*
         * Check for additional reasons that might force the trace select
         * request to be dropped
         */
        if (interpState->jitState == kJitTSelectRequest ||
            interpState->jitState == kJitTSelectRequestHot) {
            JitEntry *slot = lookupAndAdd(interpState->pc, false);
            if (slot == NULL) {
                /*
                 * Table is full.  This should have been
                 * detected by the compiler thread and the table
                 * resized before we run into it here.  Assume bad things
                 * are afoot and disable profiling.
                 */
                interpState->jitState = kJitDone;
                LOGD("JIT: JitTable full, disabling profiling");
                dvmJitStopTranslationRequests();
            } else if (slot->u.info.traceConstruction) {
                /*
                 * Trace request already in progress, but most likely it
                 * aborted without cleaning up.  Assume the worst and
                 * mark trace head as untranslatable.  If we're wrong,
                 * the compiler thread will correct the entry when the
                 * translation is completed.  The downside here is that
                 * some existing translation may chain to the interpret-only
                 * template instead of the real translation during this
                 * window.  Performance, but not correctness, issue.
                 */
                interpState->jitState = kJitDone;
                resetTracehead(interpState, slot);
            } else if (slot->codeAddress) {
                 /* Nothing to do here - just return */
                interpState->jitState = kJitDone;
            } else {
                /*
                 * Mark request.  Note, we are not guaranteed exclusivity
                 * here.  A window exists for another thread to be
                 * attempting to build this same trace.  Rather than
                 * bear the cost of locking, we'll just allow that to
                 * happen.  The compiler thread, if it chooses, can
                 * discard redundant requests.
                 */
                setTraceConstruction(slot, true);
            }
        }

        switch (interpState->jitState) {
            case kJitTSelectRequest:
            case kJitTSelectRequestHot:
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
                interpState->trace[0].frag.isCode = true;
                interpState->lastPC = 0;
                break;
            /*
             * For JIT's perspective there is no need to stay in the debug
             * interpreter unless debugger/profiler is attached.
             */
            case kJitDone:
                switchInterp = true;
                break;
            default:
                LOGE("Unexpected JIT state: %d entry point: %d",
                     interpState->jitState, interpState->entryPoint);
                dvmAbort();
        }
    } else {
        /*
         * Cannot build trace this time - ready to leave the dbg interpreter
         */
        interpState->jitState = kJitDone;
        switchInterp = true;
    }

    /*
     * Final check to see if we can really switch the interpreter. Make sure
     * the jitState is kJitDone when switchInterp is set to true.
     */
    assert(switchInterp == false || interpState->jitState == kJitDone);
    return switchInterp && !debugOrProfile &&
           !dvmJitStayInPortableInterpreter();
}

/*
 * Resizes the JitTable.  Must be a power of 2, and returns true on failure.
 * Stops all threads, and thus is a heavyweight operation. May only be called
 * by the compiler thread.
 */
bool dvmJitResizeJitTable( unsigned int size )
{
    JitEntry *pNewTable;
    JitEntry *pOldTable;
    JitEntry tempEntry;
    u4 newMask;
    unsigned int oldSize;
    unsigned int i;

    assert(gDvmJit.pJitEntryTable != NULL);
    assert(size && !(size & (size - 1)));   /* Is power of 2? */

    LOGI("Jit: resizing JitTable from %d to %d", gDvmJit.jitTableSize, size);

    newMask = size - 1;

    if (size <= gDvmJit.jitTableSize) {
        return true;
    }

    /* Make sure requested size is compatible with chain field width */
    tempEntry.u.info.chain = size;
    if (tempEntry.u.info.chain != size) {
        LOGD("Jit: JitTable request of %d too big", size);
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
    dvmSuspendAllThreads(SUSPEND_FOR_TBL_RESIZE);

    pOldTable = gDvmJit.pJitEntryTable;
    oldSize = gDvmJit.jitTableSize;

    dvmLockMutex(&gDvmJit.tableLock);
    gDvmJit.pJitEntryTable = pNewTable;
    gDvmJit.jitTableSize = size;
    gDvmJit.jitTableMask = size - 1;
    gDvmJit.jitTableEntriesUsed = 0;

    for (i=0; i < oldSize; i++) {
        if (pOldTable[i].dPC) {
            JitEntry *p;
            u2 chain;
            p = lookupAndAdd(pOldTable[i].dPC, true /* holds tableLock*/ );
            p->codeAddress = pOldTable[i].codeAddress;
            /* We need to preserve the new chain field, but copy the rest */
            chain = p->u.info.chain;
            p->u = pOldTable[i].u;
            p->u.info.chain = chain;
        }
    }
    dvmUnlockMutex(&gDvmJit.tableLock);

    free(pOldTable);

    /* Restart the world */
    dvmResumeAllThreads(SUSPEND_FOR_TBL_RESIZE);

    return false;
}

/*
 * Reset the JitTable to the initial clean state.
 */
void dvmJitResetTable(void)
{
    JitEntry *jitEntry = gDvmJit.pJitEntryTable;
    unsigned int size = gDvmJit.jitTableSize;
    unsigned int i;

    dvmLockMutex(&gDvmJit.tableLock);
    memset((void *) jitEntry, 0, sizeof(JitEntry) * size);
    for (i=0; i< size; i++) {
        jitEntry[i].u.info.chain = size;  /* Initialize chain termination */
    }
    gDvmJit.jitTableEntriesUsed = 0;
    dvmUnlockMutex(&gDvmJit.tableLock);
}

/*
 * Float/double conversion requires clamping to min and max of integer form.  If
 * target doesn't support this normally, use these.
 */
s8 dvmJitd2l(double d)
{
    static const double kMaxLong = (double)(s8)0x7fffffffffffffffULL;
    static const double kMinLong = (double)(s8)0x8000000000000000ULL;
    if (d >= kMaxLong)
        return (s8)0x7fffffffffffffffULL;
    else if (d <= kMinLong)
        return (s8)0x8000000000000000ULL;
    else if (d != d) // NaN case
        return 0;
    else
        return (s8)d;
}

s8 dvmJitf2l(float f)
{
    static const float kMaxLong = (float)(s8)0x7fffffffffffffffULL;
    static const float kMinLong = (float)(s8)0x8000000000000000ULL;
    if (f >= kMaxLong)
        return (s8)0x7fffffffffffffffULL;
    else if (f <= kMinLong)
        return (s8)0x8000000000000000ULL;
    else if (f != f) // NaN case
        return 0;
    else
        return (s8)f;
}

#endif /* WITH_JIT */
