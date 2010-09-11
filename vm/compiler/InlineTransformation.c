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
#include "Dataflow.h"
#include "libdex/OpCodeNames.h"

/* Convert the reg id from the callee to the original id passed by the caller */
static inline u4 convertRegId(const DecodedInstruction *invoke,
                              const Method *calleeMethod,
                              int calleeRegId, bool isRange)
{
    /* The order in the original arg passing list */
    int rank = calleeRegId -
               (calleeMethod->registersSize - calleeMethod->insSize);
    assert(rank >= 0);
    if (!isRange) {
        return invoke->arg[rank];
    } else {
        return invoke->vC + rank;
    }
}

static void inlineGetter(CompilationUnit *cUnit,
                         const Method *calleeMethod,
                         MIR *invokeMIR,
                         BasicBlock *invokeBB,
                         bool isPredicted,
                         bool isRange)
{
    BasicBlock *moveResultBB = invokeBB->fallThrough;
    MIR *moveResultMIR = moveResultBB->firstMIRInsn;
    MIR *newGetterMIR = dvmCompilerNew(sizeof(MIR), true);
    DecodedInstruction getterInsn;

    dexDecodeInstruction(gDvm.instrFormat, calleeMethod->insns, &getterInsn);

    if (!dvmCompilerCanIncludeThisInstruction(calleeMethod, &getterInsn))
        return;

    /*
     * Some getters (especially invoked through interface) are not followed
     * by a move result.
     */
    if ((moveResultMIR == NULL) ||
        (moveResultMIR->dalvikInsn.opCode != OP_MOVE_RESULT &&
         moveResultMIR->dalvikInsn.opCode != OP_MOVE_RESULT_OBJECT &&
         moveResultMIR->dalvikInsn.opCode != OP_MOVE_RESULT_WIDE)) {
        return;
    }

    int dfFlags = dvmCompilerDataFlowAttributes[getterInsn.opCode];

    /* Expecting vA to be the destination register */
    if (dfFlags & (DF_UA | DF_UA_WIDE)) {
        LOGE("opcode %d has DF_UA set (not expected)", getterInsn.opCode);
        dvmAbort();
    }

    if (dfFlags & DF_UB) {
        getterInsn.vB = convertRegId(&invokeMIR->dalvikInsn, calleeMethod,
                                     getterInsn.vB, isRange);
    }

    if (dfFlags & DF_UC) {
        getterInsn.vC = convertRegId(&invokeMIR->dalvikInsn, calleeMethod,
                                     getterInsn.vC, isRange);
    }

    getterInsn.vA = moveResultMIR->dalvikInsn.vA;

    /* Now setup the Dalvik instruction with converted src/dst registers */
    newGetterMIR->dalvikInsn = getterInsn;

    newGetterMIR->width = gDvm.instrWidth[getterInsn.opCode];

    newGetterMIR->OptimizationFlags |= MIR_CALLEE;

    /*
     * If the getter instruction is about to raise any exception, punt to the
     * interpreter and re-execute the invoke.
     */
    newGetterMIR->offset = invokeMIR->offset;

    newGetterMIR->meta.calleeMethod = calleeMethod;

    dvmCompilerInsertMIRAfter(invokeBB, invokeMIR, newGetterMIR);

    if (isPredicted) {
        MIR *invokeMIRSlow = dvmCompilerNew(sizeof(MIR), true);
        *invokeMIRSlow = *invokeMIR;
        invokeMIR->dalvikInsn.opCode = kMirOpCheckInlinePrediction;

        /* Use vC to denote the first argument (ie this) */
        if (!isRange) {
            invokeMIR->dalvikInsn.vC = invokeMIRSlow->dalvikInsn.arg[0];
        }

        moveResultMIR->OptimizationFlags |= MIR_INLINED_PRED;

        dvmCompilerInsertMIRAfter(invokeBB, newGetterMIR, invokeMIRSlow);
        invokeMIRSlow->OptimizationFlags |= MIR_INLINED_PRED;
#if defined(WITH_JIT_TUNING)
        gDvmJit.invokePolyGetterInlined++;
#endif
    } else {
        invokeMIR->OptimizationFlags |= MIR_INLINED;
        moveResultMIR->OptimizationFlags |= MIR_INLINED;
#if defined(WITH_JIT_TUNING)
        gDvmJit.invokeMonoGetterInlined++;
#endif
    }

    return;
}

static void inlineSetter(CompilationUnit *cUnit,
                         const Method *calleeMethod,
                         MIR *invokeMIR,
                         BasicBlock *invokeBB,
                         bool isPredicted,
                         bool isRange)
{
    MIR *newSetterMIR = dvmCompilerNew(sizeof(MIR), true);
    DecodedInstruction setterInsn;

    dexDecodeInstruction(gDvm.instrFormat, calleeMethod->insns, &setterInsn);

    if (!dvmCompilerCanIncludeThisInstruction(calleeMethod, &setterInsn))
        return;

    int dfFlags = dvmCompilerDataFlowAttributes[setterInsn.opCode];

    if (dfFlags & (DF_UA | DF_UA_WIDE)) {
        setterInsn.vA = convertRegId(&invokeMIR->dalvikInsn, calleeMethod,
                                     setterInsn.vA, isRange);

    }

    if (dfFlags & DF_UB) {
        setterInsn.vB = convertRegId(&invokeMIR->dalvikInsn, calleeMethod,
                                     setterInsn.vB, isRange);

    }

    if (dfFlags & DF_UC) {
        setterInsn.vC = convertRegId(&invokeMIR->dalvikInsn, calleeMethod,
                                     setterInsn.vC, isRange);
    }

    /* Now setup the Dalvik instruction with converted src/dst registers */
    newSetterMIR->dalvikInsn = setterInsn;

    newSetterMIR->width = gDvm.instrWidth[setterInsn.opCode];

    newSetterMIR->OptimizationFlags |= MIR_CALLEE;

    /*
     * If the setter instruction is about to raise any exception, punt to the
     * interpreter and re-execute the invoke.
     */
    newSetterMIR->offset = invokeMIR->offset;

    newSetterMIR->meta.calleeMethod = calleeMethod;

    dvmCompilerInsertMIRAfter(invokeBB, invokeMIR, newSetterMIR);

    if (isPredicted) {
        MIR *invokeMIRSlow = dvmCompilerNew(sizeof(MIR), true);
        *invokeMIRSlow = *invokeMIR;
        invokeMIR->dalvikInsn.opCode = kMirOpCheckInlinePrediction;

        /* Use vC to denote the first argument (ie this) */
        if (!isRange) {
            invokeMIR->dalvikInsn.vC = invokeMIRSlow->dalvikInsn.arg[0];
        }

        dvmCompilerInsertMIRAfter(invokeBB, newSetterMIR, invokeMIRSlow);
        invokeMIRSlow->OptimizationFlags |= MIR_INLINED_PRED;
#if defined(WITH_JIT_TUNING)
        gDvmJit.invokePolySetterInlined++;
#endif
    } else {
        /*
         * The invoke becomes no-op so it needs an explicit branch to jump to
         * the chaining cell.
         */
        invokeBB->needFallThroughBranch = true;
        invokeMIR->OptimizationFlags |= MIR_INLINED;
#if defined(WITH_JIT_TUNING)
        gDvmJit.invokeMonoSetterInlined++;
#endif
    }

    return;
}

static void tryInlineSingletonCallsite(CompilationUnit *cUnit,
                                       const Method *calleeMethod,
                                       MIR *invokeMIR,
                                       BasicBlock *invokeBB,
                                       bool isRange)
{
    /* Not a Java method */
    if (dvmIsNativeMethod(calleeMethod)) return;

    CompilerMethodStats *methodStats =
        dvmCompilerAnalyzeMethodBody(calleeMethod, true);

    /* Empty callee - do nothing */
    if (methodStats->attributes & METHOD_IS_EMPTY) {
        /* The original invoke instruction is effectively turned into NOP */
        invokeMIR->OptimizationFlags |= MIR_INLINED;
        /*
         * Need to insert an explicit branch to catch the falling knife (into
         * the PC reconstruction or chaining cell).
         */
        invokeBB->needFallThroughBranch = true;
        return;
    }

    if (methodStats->attributes & METHOD_IS_GETTER) {
        inlineGetter(cUnit, calleeMethod, invokeMIR, invokeBB, false, isRange);
        return;
    } else if (methodStats->attributes & METHOD_IS_SETTER) {
        inlineSetter(cUnit, calleeMethod, invokeMIR, invokeBB, false, isRange);
        return;
    }
}

static void inlineEmptyVirtualCallee(CompilationUnit *cUnit,
                                     const Method *calleeMethod,
                                     MIR *invokeMIR,
                                     BasicBlock *invokeBB)
{
    MIR *invokeMIRSlow = dvmCompilerNew(sizeof(MIR), true);
    *invokeMIRSlow = *invokeMIR;
    invokeMIR->dalvikInsn.opCode = kMirOpCheckInlinePrediction;

    dvmCompilerInsertMIRAfter(invokeBB, invokeMIR, invokeMIRSlow);
    invokeMIRSlow->OptimizationFlags |= MIR_INLINED_PRED;
}

static void tryInlineVirtualCallsite(CompilationUnit *cUnit,
                                     const Method *calleeMethod,
                                     MIR *invokeMIR,
                                     BasicBlock *invokeBB,
                                     bool isRange)
{
    /* Not a Java method */
    if (dvmIsNativeMethod(calleeMethod)) return;

    CompilerMethodStats *methodStats =
        dvmCompilerAnalyzeMethodBody(calleeMethod, true);

    /* Empty callee - do nothing by checking the clazz pointer */
    if (methodStats->attributes & METHOD_IS_EMPTY) {
        inlineEmptyVirtualCallee(cUnit, calleeMethod, invokeMIR, invokeBB);
        return;
    }

    if (methodStats->attributes & METHOD_IS_GETTER) {
        inlineGetter(cUnit, calleeMethod, invokeMIR, invokeBB, true, isRange);
        return;
    } else if (methodStats->attributes & METHOD_IS_SETTER) {
        inlineSetter(cUnit, calleeMethod, invokeMIR, invokeBB, true, isRange);
        return;
    }
}


void dvmCompilerInlineMIR(CompilationUnit *cUnit)
{
    int i;
    bool isRange = false;

    /*
     * Analyze the basic block containing an invoke to see if it can be inlined
     */
    for (i = 0; i < cUnit->numBlocks; i++) {
        BasicBlock *bb = cUnit->blockList[i];
        if (bb->blockType != kDalvikByteCode)
            continue;
        MIR *lastMIRInsn = bb->lastMIRInsn;
        int opCode = lastMIRInsn->dalvikInsn.opCode;
        int flags = dexGetInstrFlags(gDvm.instrFlags, opCode);

        /* No invoke - continue */
        if ((flags & kInstrInvoke) == 0)
            continue;

        /* Not a real invoke - continue */
        if (opCode == OP_INVOKE_DIRECT_EMPTY)
            continue;

        /*
         * If the invoke itself is selected for single stepping, don't bother
         * to inline it.
         */
        if (SINGLE_STEP_OP(opCode))
            continue;

        const Method *calleeMethod;

        switch (opCode) {
            case OP_INVOKE_SUPER:
            case OP_INVOKE_DIRECT:
            case OP_INVOKE_STATIC:
            case OP_INVOKE_SUPER_QUICK:
                calleeMethod = lastMIRInsn->meta.callsiteInfo->method;
                break;
            case OP_INVOKE_SUPER_RANGE:
            case OP_INVOKE_DIRECT_RANGE:
            case OP_INVOKE_STATIC_RANGE:
            case OP_INVOKE_SUPER_QUICK_RANGE:
                isRange = true;
                calleeMethod = lastMIRInsn->meta.callsiteInfo->method;
                break;
            default:
                calleeMethod = NULL;
                break;
        }

        if (calleeMethod) {
            tryInlineSingletonCallsite(cUnit, calleeMethod, lastMIRInsn, bb,
                                       isRange);
            return;
        }

        switch (opCode) {
            case OP_INVOKE_VIRTUAL:
            case OP_INVOKE_VIRTUAL_QUICK:
            case OP_INVOKE_INTERFACE:
                isRange = false;
                calleeMethod = lastMIRInsn->meta.callsiteInfo->method;
                break;
            case OP_INVOKE_VIRTUAL_RANGE:
            case OP_INVOKE_VIRTUAL_QUICK_RANGE:
            case OP_INVOKE_INTERFACE_RANGE:
                isRange = true;
                calleeMethod = lastMIRInsn->meta.callsiteInfo->method;
                break;
            default:
                break;
        }

        if (calleeMethod) {
            tryInlineVirtualCallsite(cUnit, calleeMethod, lastMIRInsn, bb,
                                     isRange);
            return;
        }
    }
}
