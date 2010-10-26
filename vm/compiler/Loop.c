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

#include "Dalvik.h"
#include "CompilerInternals.h"
#include "Dataflow.h"
#include "Loop.h"

#define DEBUG_LOOP(X)

/*
 * Given the current simple natural loops, the phi node placement can be
 * determined in the following fashion:
 *                    entry (B0)
 *              +---v   v
 *              |  loop body (B1)
 *              |       v
 *              |  loop back (B2)
 *              +---+   v
 *                     exit (B3)
 *
 *  1) Add live-ins of B1 to B0 as defs
 *  2) The intersect of defs(B0)/defs(B1) and defs(B2)/def(B0) are the variables
 *     that need PHI nodes in B1.
 */
static void handlePhiPlacement(CompilationUnit *cUnit)
{
    BasicBlock *entry = cUnit->blockList[0];
    BasicBlock *loopBody = cUnit->blockList[1];
    BasicBlock *loopBranch = cUnit->blockList[2];
    dvmCopyBitVector(entry->dataFlowInfo->defV,
                     loopBody->dataFlowInfo->liveInV);

    BitVector *phiV = dvmCompilerAllocBitVector(cUnit->method->registersSize,
                                                false);
    dvmIntersectBitVectors(phiV, entry->dataFlowInfo->defV,
                           loopBody->dataFlowInfo->defV);
    dvmIntersectBitVectors(phiV, entry->dataFlowInfo->defV,
                           loopBranch->dataFlowInfo->defV);

    /* Insert the PHI MIRs */
    int i;
    for (i = 0; i < cUnit->method->registersSize; i++) {
        if (!dvmIsBitSet(phiV, i)) {
            continue;
        }
        MIR *phi = dvmCompilerNew(sizeof(MIR), true);
        phi->dalvikInsn.opCode = kMirOpPhi;
        phi->dalvikInsn.vA = i;
        dvmCompilerPrependMIR(loopBody, phi);
    }
}

static void fillPhiNodeContents(CompilationUnit *cUnit)
{
    BasicBlock *entry = cUnit->blockList[0];
    BasicBlock *loopBody = cUnit->blockList[1];
    BasicBlock *loopBranch = cUnit->blockList[2];
    MIR *mir;

    for (mir = loopBody->firstMIRInsn; mir; mir = mir->next) {
        if (mir->dalvikInsn.opCode != kMirOpPhi) break;
        int dalvikReg = mir->dalvikInsn.vA;

        mir->ssaRep->numUses = 2;
        mir->ssaRep->uses = dvmCompilerNew(sizeof(int) * 2, false);
        mir->ssaRep->uses[0] =
            DECODE_REG(entry->dataFlowInfo->dalvikToSSAMap[dalvikReg]);
        mir->ssaRep->uses[1] =
            DECODE_REG(loopBranch->dataFlowInfo->dalvikToSSAMap[dalvikReg]);
    }


}

#if 0
/* Debugging routines */
static void dumpConstants(CompilationUnit *cUnit)
{
    int i;
    for (i = 0; i < cUnit->numSSARegs; i++) {
        if (dvmIsBitSet(cUnit->isConstantV, i)) {
            int subNReg = dvmConvertSSARegToDalvik(cUnit, i);
            LOGE("s%d(v%d_%d) has %d", i,
                 DECODE_REG(subNReg), DECODE_SUB(subNReg),
                 cUnit->constantValues[i]);
        }
    }
}

static void dumpIVList(CompilationUnit *cUnit)
{
    unsigned int i;
    GrowableList *ivList = cUnit->loopAnalysis->ivList;
    int *ssaToDalvikMap = (int *) cUnit->ssaToDalvikMap->elemList;

    for (i = 0; i < ivList->numUsed; i++) {
        InductionVariableInfo *ivInfo = ivList->elemList[i];
        /* Basic IV */
        if (ivInfo->ssaReg == ivInfo->basicSSAReg) {
            LOGE("BIV %d: s%d(v%d) + %d", i,
                 ivInfo->ssaReg,
                 ssaToDalvikMap[ivInfo->ssaReg] & 0xffff,
                 ivInfo->inc);
        /* Dependent IV */
        } else {
            LOGE("DIV %d: s%d(v%d) = %d * s%d(v%d) + %d", i,
                 ivInfo->ssaReg,
                 ssaToDalvikMap[ivInfo->ssaReg] & 0xffff,
                 ivInfo->m,
                 ivInfo->basicSSAReg,
                 ssaToDalvikMap[ivInfo->basicSSAReg] & 0xffff,
                 ivInfo->c);
        }
    }
}

static void dumpHoistedChecks(CompilationUnit *cUnit)
{
    LoopAnalysis *loopAnalysis = cUnit->loopAnalysis;
    unsigned int i;

    for (i = 0; i < loopAnalysis->arrayAccessInfo->numUsed; i++) {
        ArrayAccessInfo *arrayAccessInfo =
            GET_ELEM_N(loopAnalysis->arrayAccessInfo,
                       ArrayAccessInfo*, i);
        int arrayReg = DECODE_REG(
            dvmConvertSSARegToDalvik(cUnit, arrayAccessInfo->arrayReg));
        int idxReg = DECODE_REG(
            dvmConvertSSARegToDalvik(cUnit, arrayAccessInfo->ivReg));
        LOGE("Array access %d", i);
        LOGE("  arrayReg %d", arrayReg);
        LOGE("  idxReg %d", idxReg);
        LOGE("  endReg %d", loopAnalysis->endConditionReg);
        LOGE("  maxC %d", arrayAccessInfo->maxC);
        LOGE("  minC %d", arrayAccessInfo->minC);
        LOGE("  opcode %d", loopAnalysis->loopBranchOpcode);
    }
}

#endif

/*
 * A loop is considered optimizable if:
 * 1) It has one basic induction variable
 * 2) The loop back branch compares the BIV with a constant
 * 3) If it is a count-up loop, the condition is GE/GT, or LE/LT/LEZ/LTZ for
 *    a count-down loop.
 *
 * Return false if the loop is not optimizable.
 */
static bool isLoopOptimizable(CompilationUnit *cUnit)
{
    unsigned int i;
    BasicBlock *loopBranch = cUnit->blockList[2];
    LoopAnalysis *loopAnalysis = cUnit->loopAnalysis;

    if (loopAnalysis->numBasicIV != 1) return false;
    for (i = 0; i < loopAnalysis->ivList->numUsed; i++) {
        InductionVariableInfo *ivInfo;

        ivInfo = GET_ELEM_N(loopAnalysis->ivList, InductionVariableInfo*, i);
        /* Count up or down loop? */
        if (ivInfo->ssaReg == ivInfo->basicSSAReg) {
            /* Infinite loop */
            if (ivInfo->inc == 0) {
                return false;
            }
            loopAnalysis->isCountUpLoop = ivInfo->inc > 0;
            break;
        }
    }

    MIR *branch = loopBranch->lastMIRInsn;
    OpCode opCode = branch->dalvikInsn.opCode;

    /*
     * If the instruction is not accessing the IV as the first operand, return
     * false.
     */
    if (branch->ssaRep->numUses == 0 || branch->ssaRep->numDefs != 0) {
        return false;
    }

    /*
     * If the first operand of the comparison is not the basic induction
     * variable, return false.
     */
    if (branch->ssaRep->uses[0] != loopAnalysis->ssaBIV) {
        return false;
    }

    if (loopAnalysis->isCountUpLoop) {
        /*
         * If the condition op is not > or >=, this is not an optimization
         * candidate.
         */
        if (opCode != OP_IF_GT && opCode != OP_IF_GE) {
            return false;
        }
        /*
         * If the comparison is not between the BIV and a loop invariant,
         * return false. endReg is loop invariant if one of the following is
         * true:
         * - It is not defined in the loop (ie DECODE_SUB returns 0)
         * - It is reloaded with a constant
         */
        int endReg = dvmConvertSSARegToDalvik(cUnit, branch->ssaRep->uses[1]);
        if (DECODE_SUB(endReg) != 0 &&
            !dvmIsBitSet(cUnit->isConstantV, branch->ssaRep->uses[1])) {
            return false;
        }
        loopAnalysis->endConditionReg = DECODE_REG(endReg);
    } else  {
        /*
         * If the condition op is not < or <=, this is not an optimization
         * candidate.
         */
        if (opCode == OP_IF_LT || opCode == OP_IF_LE) {
            /*
             * If the comparison is not between the BIV and a loop invariant,
             * return false.
             */
            int endReg = dvmConvertSSARegToDalvik(cUnit,
                                                  branch->ssaRep->uses[1]);

            if (DECODE_SUB(endReg) != 0) {
                return false;
            }
            loopAnalysis->endConditionReg = DECODE_REG(endReg);
        } else if (opCode != OP_IF_LTZ && opCode != OP_IF_LEZ) {
            return false;
        }
    }
    loopAnalysis->loopBranchOpcode = opCode;
    return true;
}

/*
 * Record the upper and lower bound information for range checks for each
 * induction variable. If array A is accessed by index "i+5", the upper and
 * lower bound will be len(A)-5 and -5, respectively.
 */
static void updateRangeCheckInfo(CompilationUnit *cUnit, int arrayReg,
                                 int idxReg)
{
    InductionVariableInfo *ivInfo;
    LoopAnalysis *loopAnalysis = cUnit->loopAnalysis;
    unsigned int i, j;

    for (i = 0; i < loopAnalysis->ivList->numUsed; i++) {
        ivInfo = GET_ELEM_N(loopAnalysis->ivList, InductionVariableInfo*, i);
        if (ivInfo->ssaReg == idxReg) {
            ArrayAccessInfo *arrayAccessInfo = NULL;
            for (j = 0; j < loopAnalysis->arrayAccessInfo->numUsed; j++) {
                ArrayAccessInfo *existingArrayAccessInfo =
                    GET_ELEM_N(loopAnalysis->arrayAccessInfo,
                               ArrayAccessInfo*,
                               j);
                if (existingArrayAccessInfo->arrayReg == arrayReg) {
                    if (ivInfo->c > existingArrayAccessInfo->maxC) {
                        existingArrayAccessInfo->maxC = ivInfo->c;
                    }
                    if (ivInfo->c < existingArrayAccessInfo->minC) {
                        existingArrayAccessInfo->minC = ivInfo->c;
                    }
                    arrayAccessInfo = existingArrayAccessInfo;
                    break;
                }
            }
            if (arrayAccessInfo == NULL) {
                arrayAccessInfo =
                    dvmCompilerNew(sizeof(ArrayAccessInfo), false);
                arrayAccessInfo->ivReg = ivInfo->basicSSAReg;
                arrayAccessInfo->arrayReg = arrayReg;
                arrayAccessInfo->maxC = (ivInfo->c > 0) ? ivInfo->c : 0;
                arrayAccessInfo->minC = (ivInfo->c < 0) ? ivInfo->c : 0;
                dvmInsertGrowableList(loopAnalysis->arrayAccessInfo,
                                      arrayAccessInfo);
            }
            break;
        }
    }
}

/* Returns true if the loop body cannot throw any exceptions */
static bool doLoopBodyCodeMotion(CompilationUnit *cUnit)
{
    BasicBlock *loopBody = cUnit->blockList[1];
    MIR *mir;
    bool loopBodyCanThrow = false;

    for (mir = loopBody->firstMIRInsn; mir; mir = mir->next) {
        DecodedInstruction *dInsn = &mir->dalvikInsn;
        int dfAttributes =
            dvmCompilerDataFlowAttributes[mir->dalvikInsn.opCode];

        /* Skip extended MIR instructions */
        if (dInsn->opCode > 255) continue;

        int instrFlags = dexGetInstrFlags(gDvm.instrFlags, dInsn->opCode);

        /* Instruction is clean */
        if ((instrFlags & kInstrCanThrow) == 0) continue;

        /*
         * Currently we can only optimize away null and range checks. Punt on
         * instructions that can throw due to other exceptions.
         */
        if (!(dfAttributes & DF_HAS_NR_CHECKS)) {
            loopBodyCanThrow = true;
            continue;
        }

        /*
         * This comparison is redundant now, but we will have more than one
         * group of flags to check soon.
         */
        if (dfAttributes & DF_HAS_NR_CHECKS) {
            /*
             * Check if the null check is applied on a loop invariant register?
             * If the register's SSA id is less than the number of Dalvik
             * registers, then it is loop invariant.
             */
            int refIdx;
            switch (dfAttributes & DF_HAS_NR_CHECKS) {
                case DF_NULL_N_RANGE_CHECK_0:
                    refIdx = 0;
                    break;
                case DF_NULL_N_RANGE_CHECK_1:
                    refIdx = 1;
                    break;
                case DF_NULL_N_RANGE_CHECK_2:
                    refIdx = 2;
                    break;
                default:
                    refIdx = 0;
                    LOGE("Jit: bad case in doLoopBodyCodeMotion");
                    dvmCompilerAbort(cUnit);
            }

            int useIdx = refIdx + 1;
            int subNRegArray =
                dvmConvertSSARegToDalvik(cUnit, mir->ssaRep->uses[refIdx]);
            int arraySub = DECODE_SUB(subNRegArray);

            /*
             * If the register is never updated in the loop (ie subscript == 0),
             * it is an optimization candidate.
             */
            if (arraySub != 0) {
                loopBodyCanThrow = true;
                continue;
            }

            /*
             * Then check if the range check can be hoisted out of the loop if
             * it is basic or dependent induction variable.
             */
            if (dvmIsBitSet(cUnit->loopAnalysis->isIndVarV,
                            mir->ssaRep->uses[useIdx])) {
                mir->OptimizationFlags |=
                    MIR_IGNORE_RANGE_CHECK |  MIR_IGNORE_NULL_CHECK;
                updateRangeCheckInfo(cUnit, mir->ssaRep->uses[refIdx],
                                     mir->ssaRep->uses[useIdx]);
            }
        }
    }

    return !loopBodyCanThrow;
}

static void genHoistedChecks(CompilationUnit *cUnit)
{
    unsigned int i;
    BasicBlock *entry = cUnit->blockList[0];
    LoopAnalysis *loopAnalysis = cUnit->loopAnalysis;
    int globalMaxC = 0;
    int globalMinC = 0;
    /* Should be loop invariant */
    int idxReg = 0;

    for (i = 0; i < loopAnalysis->arrayAccessInfo->numUsed; i++) {
        ArrayAccessInfo *arrayAccessInfo =
            GET_ELEM_N(loopAnalysis->arrayAccessInfo,
                       ArrayAccessInfo*, i);
        int arrayReg = DECODE_REG(
            dvmConvertSSARegToDalvik(cUnit, arrayAccessInfo->arrayReg));
        idxReg = DECODE_REG(
            dvmConvertSSARegToDalvik(cUnit, arrayAccessInfo->ivReg));

        MIR *rangeCheckMIR = dvmCompilerNew(sizeof(MIR), true);
        rangeCheckMIR->dalvikInsn.opCode = (loopAnalysis->isCountUpLoop) ?
            kMirOpNullNRangeUpCheck : kMirOpNullNRangeDownCheck;
        rangeCheckMIR->dalvikInsn.vA = arrayReg;
        rangeCheckMIR->dalvikInsn.vB = idxReg;
        rangeCheckMIR->dalvikInsn.vC = loopAnalysis->endConditionReg;
        rangeCheckMIR->dalvikInsn.arg[0] = arrayAccessInfo->maxC;
        rangeCheckMIR->dalvikInsn.arg[1] = arrayAccessInfo->minC;
        rangeCheckMIR->dalvikInsn.arg[2] = loopAnalysis->loopBranchOpcode;
        dvmCompilerAppendMIR(entry, rangeCheckMIR);
        if (arrayAccessInfo->maxC > globalMaxC) {
            globalMaxC = arrayAccessInfo->maxC;
        }
        if (arrayAccessInfo->minC < globalMinC) {
            globalMinC = arrayAccessInfo->minC;
        }
    }

    if (loopAnalysis->arrayAccessInfo->numUsed != 0) {
        if (loopAnalysis->isCountUpLoop) {
            MIR *boundCheckMIR = dvmCompilerNew(sizeof(MIR), true);
            boundCheckMIR->dalvikInsn.opCode = kMirOpLowerBound;
            boundCheckMIR->dalvikInsn.vA = idxReg;
            boundCheckMIR->dalvikInsn.vB = globalMinC;
            dvmCompilerAppendMIR(entry, boundCheckMIR);
        } else {
            if (loopAnalysis->loopBranchOpcode == OP_IF_LT ||
                loopAnalysis->loopBranchOpcode == OP_IF_LE) {
                MIR *boundCheckMIR = dvmCompilerNew(sizeof(MIR), true);
                boundCheckMIR->dalvikInsn.opCode = kMirOpLowerBound;
                boundCheckMIR->dalvikInsn.vA = loopAnalysis->endConditionReg;
                boundCheckMIR->dalvikInsn.vB = globalMinC;
                /*
                 * If the end condition is ">" in the source, the check in the
                 * Dalvik bytecode is OP_IF_LE. In this case add 1 back to the
                 * constant field to reflect the fact that the smallest index
                 * value is "endValue + constant + 1".
                 */
                if (loopAnalysis->loopBranchOpcode == OP_IF_LE) {
                    boundCheckMIR->dalvikInsn.vB++;
                }
                dvmCompilerAppendMIR(entry, boundCheckMIR);
            } else if (loopAnalysis->loopBranchOpcode == OP_IF_LTZ) {
                /* Array index will fall below 0 */
                if (globalMinC < 0) {
                    MIR *boundCheckMIR = dvmCompilerNew(sizeof(MIR), true);
                    boundCheckMIR->dalvikInsn.opCode = kMirOpPunt;
                    dvmCompilerAppendMIR(entry, boundCheckMIR);
                }
            } else if (loopAnalysis->loopBranchOpcode == OP_IF_LEZ) {
                /* Array index will fall below 0 */
                if (globalMinC < -1) {
                    MIR *boundCheckMIR = dvmCompilerNew(sizeof(MIR), true);
                    boundCheckMIR->dalvikInsn.opCode = kMirOpPunt;
                    dvmCompilerAppendMIR(entry, boundCheckMIR);
                }
            } else {
                LOGE("Jit: bad case in genHoistedChecks");
                dvmCompilerAbort(cUnit);
            }
        }

    }
}

/*
 * Main entry point to do loop optimization.
 * Return false if sanity checks for loop formation/optimization failed.
 */
bool dvmCompilerLoopOpt(CompilationUnit *cUnit)
{
    LoopAnalysis *loopAnalysis = dvmCompilerNew(sizeof(LoopAnalysis), true);

    assert(cUnit->blockList[0]->blockType == kTraceEntryBlock);
    assert(cUnit->blockList[2]->blockType == kDalvikByteCode);
    assert(cUnit->blockList[3]->blockType == kTraceExitBlock);

    cUnit->loopAnalysis = loopAnalysis;
    /*
     * Find live-in variables to the loop body so that we can fake their
     * definitions in the entry block.
     */
    dvmCompilerDataFlowAnalysisDispatcher(cUnit, dvmCompilerFindLiveIn);

    /* Insert phi nodes to the loop body */
    handlePhiPlacement(cUnit);

    dvmCompilerDataFlowAnalysisDispatcher(cUnit, dvmCompilerDoSSAConversion);
    fillPhiNodeContents(cUnit);

    /* Constant propagation */
    cUnit->isConstantV = dvmAllocBitVector(cUnit->numSSARegs, false);
    cUnit->constantValues = dvmCompilerNew(sizeof(int) * cUnit->numSSARegs,
                                           true);
    dvmCompilerDataFlowAnalysisDispatcher(cUnit,
                                          dvmCompilerDoConstantPropagation);
    DEBUG_LOOP(dumpConstants(cUnit);)

    /* Find induction variables - basic and dependent */
    loopAnalysis->ivList = dvmCompilerNew(sizeof(GrowableList), true);
    dvmInitGrowableList(loopAnalysis->ivList, 4);
    loopAnalysis->isIndVarV = dvmAllocBitVector(cUnit->numSSARegs, false);
    dvmCompilerDataFlowAnalysisDispatcher(cUnit,
                                          dvmCompilerFindInductionVariables);
    DEBUG_LOOP(dumpIVList(cUnit);)

    /* If the loop turns out to be non-optimizable, return early */
    if (!isLoopOptimizable(cUnit))
        return false;

    loopAnalysis->arrayAccessInfo = dvmCompilerNew(sizeof(GrowableList), true);
    dvmInitGrowableList(loopAnalysis->arrayAccessInfo, 4);
    loopAnalysis->bodyIsClean = doLoopBodyCodeMotion(cUnit);
    DEBUG_LOOP(dumpHoistedChecks(cUnit);)

    /*
     * Convert the array access information into extended MIR code in the loop
     * header.
     */
    genHoistedChecks(cUnit);
    return true;
}
