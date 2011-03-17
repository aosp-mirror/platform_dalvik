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
    BasicBlock *entry =
        (BasicBlock *) dvmGrowableListGetElement(&cUnit->blockList, 0);
    BasicBlock *loopBody =
        (BasicBlock *) dvmGrowableListGetElement(&cUnit->blockList, 1);
    BasicBlock *loopBranch =
        (BasicBlock *) dvmGrowableListGetElement(&cUnit->blockList, 2);
    dvmCopyBitVector(entry->dataFlowInfo->defV,
                     loopBody->dataFlowInfo->liveInV);

    BitVector *phiV = dvmCompilerAllocBitVector(cUnit->method->registersSize,
                                                false);
    BitVector *phi2V = dvmCompilerAllocBitVector(cUnit->method->registersSize,
                                                 false);
    dvmIntersectBitVectors(phiV, entry->dataFlowInfo->defV,
                           loopBody->dataFlowInfo->defV);
    dvmIntersectBitVectors(phi2V, entry->dataFlowInfo->defV,
                           loopBranch->dataFlowInfo->defV);
    dvmUnifyBitVectors(phiV, phiV, phi2V);

    /* Insert the PHI MIRs */
    int i;
    for (i = 0; i < cUnit->method->registersSize; i++) {
        if (!dvmIsBitSet(phiV, i)) {
            continue;
        }
        MIR *phi = (MIR *)dvmCompilerNew(sizeof(MIR), true);
        phi->dalvikInsn.opcode = kMirOpPhi;
        phi->dalvikInsn.vA = i;
        dvmCompilerPrependMIR(loopBody, phi);
    }
}

static void fillPhiNodeContents(CompilationUnit *cUnit)
{
    BasicBlock *entry =
        (BasicBlock *) dvmGrowableListGetElement(&cUnit->blockList, 0);
    BasicBlock *loopBody =
        (BasicBlock *) dvmGrowableListGetElement(&cUnit->blockList, 1);
    BasicBlock *loopBranch =
        (BasicBlock *) dvmGrowableListGetElement(&cUnit->blockList, 2);
    MIR *mir;

    for (mir = loopBody->firstMIRInsn; mir; mir = mir->next) {
        if (mir->dalvikInsn.opcode != kMirOpPhi) break;
        int dalvikReg = mir->dalvikInsn.vA;

        mir->ssaRep->numUses = 2;
        mir->ssaRep->uses = (int *)dvmCompilerNew(sizeof(int) * 2, false);
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
    BasicBlock *loopBranch =
        (BasicBlock *) dvmGrowableListGetElement(&cUnit->blockList, 2);
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
    Opcode opcode = branch->dalvikInsn.opcode;

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
        if (opcode != OP_IF_GT && opcode != OP_IF_GE) {
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
        if (opcode == OP_IF_LT || opcode == OP_IF_LE) {
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
        } else if (opcode != OP_IF_LTZ && opcode != OP_IF_LEZ) {
            return false;
        }
    }
    loopAnalysis->loopBranchOpcode = opcode;
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
                    (ArrayAccessInfo *)dvmCompilerNew(sizeof(ArrayAccessInfo),
                                                      false);
                arrayAccessInfo->ivReg = ivInfo->basicSSAReg;
                arrayAccessInfo->arrayReg = arrayReg;
                arrayAccessInfo->maxC = (ivInfo->c > 0) ? ivInfo->c : 0;
                arrayAccessInfo->minC = (ivInfo->c < 0) ? ivInfo->c : 0;
                dvmInsertGrowableList(loopAnalysis->arrayAccessInfo,
                                      (intptr_t) arrayAccessInfo);
            }
            break;
        }
    }
}

/* Returns true if the loop body cannot throw any exceptions */
static bool doLoopBodyCodeMotion(CompilationUnit *cUnit)
{
    BasicBlock *loopBody =
        (BasicBlock *) dvmGrowableListGetElement(&cUnit->blockList, 1);
    MIR *mir;
    bool loopBodyCanThrow = false;

    for (mir = loopBody->firstMIRInsn; mir; mir = mir->next) {
        DecodedInstruction *dInsn = &mir->dalvikInsn;
        int dfAttributes =
            dvmCompilerDataFlowAttributes[mir->dalvikInsn.opcode];

        /* Skip extended MIR instructions */
        if (dInsn->opcode >= kNumPackedOpcodes) continue;

        int instrFlags = dexGetFlagsFromOpcode(dInsn->opcode);

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
    BasicBlock *entry =
        (BasicBlock *) dvmGrowableListGetElement(&cUnit->blockList, 0);
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

        MIR *rangeCheckMIR = (MIR *)dvmCompilerNew(sizeof(MIR), true);
        rangeCheckMIR->dalvikInsn.opcode = (loopAnalysis->isCountUpLoop) ?
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
            MIR *boundCheckMIR = (MIR *)dvmCompilerNew(sizeof(MIR), true);
            boundCheckMIR->dalvikInsn.opcode = kMirOpLowerBound;
            boundCheckMIR->dalvikInsn.vA = idxReg;
            boundCheckMIR->dalvikInsn.vB = globalMinC;
            dvmCompilerAppendMIR(entry, boundCheckMIR);
        } else {
            if (loopAnalysis->loopBranchOpcode == OP_IF_LT ||
                loopAnalysis->loopBranchOpcode == OP_IF_LE) {
                MIR *boundCheckMIR = (MIR *)dvmCompilerNew(sizeof(MIR), true);
                boundCheckMIR->dalvikInsn.opcode = kMirOpLowerBound;
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
                    MIR *boundCheckMIR = (MIR *)dvmCompilerNew(sizeof(MIR), true);
                    boundCheckMIR->dalvikInsn.opcode = kMirOpPunt;
                    dvmCompilerAppendMIR(entry, boundCheckMIR);
                }
            } else if (loopAnalysis->loopBranchOpcode == OP_IF_LEZ) {
                /* Array index will fall below 0 */
                if (globalMinC < -1) {
                    MIR *boundCheckMIR = (MIR *)dvmCompilerNew(sizeof(MIR), true);
                    boundCheckMIR->dalvikInsn.opcode = kMirOpPunt;
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
    LoopAnalysis *loopAnalysis =
        (LoopAnalysis *)dvmCompilerNew(sizeof(LoopAnalysis), true);

    assert(((BasicBlock *) dvmGrowableListGetElement(&cUnit->blockList, 0))
                               ->blockType == kTraceEntryBlock);
    assert(((BasicBlock *) dvmGrowableListGetElement(&cUnit->blockList, 2))
                               ->blockType == kDalvikByteCode);
    assert(((BasicBlock *) dvmGrowableListGetElement(&cUnit->blockList, 3))
                               ->blockType == kTraceExitBlock);

    cUnit->loopAnalysis = loopAnalysis;
    /*
     * Find live-in variables to the loop body so that we can fake their
     * definitions in the entry block.
     */
    dvmCompilerDataFlowAnalysisDispatcher(cUnit, dvmCompilerFindLocalLiveIn,
                                          kAllNodes,
                                          false /* isIterative */);

    /* Insert phi nodes to the loop body */
    handlePhiPlacement(cUnit);

    dvmCompilerDataFlowAnalysisDispatcher(cUnit, dvmCompilerDoSSAConversion,
                                          kAllNodes,
                                          false /* isIterative */);
    fillPhiNodeContents(cUnit);

    /* Constant propagation */
    cUnit->isConstantV = dvmAllocBitVector(cUnit->numSSARegs, false);
    cUnit->constantValues =
        (int *)dvmCompilerNew(sizeof(int) * cUnit->numSSARegs,
                              true);
    dvmCompilerDataFlowAnalysisDispatcher(cUnit,
                                          dvmCompilerDoConstantPropagation,
                                          kAllNodes,
                                          false /* isIterative */);
    DEBUG_LOOP(dumpConstants(cUnit);)

    /* Find induction variables - basic and dependent */
    loopAnalysis->ivList =
        (GrowableList *)dvmCompilerNew(sizeof(GrowableList), true);
    dvmInitGrowableList(loopAnalysis->ivList, 4);
    loopAnalysis->isIndVarV = dvmAllocBitVector(cUnit->numSSARegs, false);
    dvmCompilerDataFlowAnalysisDispatcher(cUnit,
                                          dvmCompilerFindInductionVariables,
                                          kAllNodes,
                                          false /* isIterative */);
    DEBUG_LOOP(dumpIVList(cUnit);)

    /* If the loop turns out to be non-optimizable, return early */
    if (!isLoopOptimizable(cUnit))
        return false;

    loopAnalysis->arrayAccessInfo =
        (GrowableList *)dvmCompilerNew(sizeof(GrowableList), true);
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

void resetBlockEdges(BasicBlock *bb)
{
    bb->taken = NULL;
    bb->fallThrough = NULL;
    bb->successorBlockList.blockListType = kNotUsed;
}

static bool clearPredecessorVector(struct CompilationUnit *cUnit,
                                   struct BasicBlock *bb)
{
    dvmClearAllBits(bb->predecessors);
    return false;
}

bool dvmCompilerFilterLoopBlocks(CompilationUnit *cUnit)
{
    BasicBlock *firstBB = cUnit->entryBlock->fallThrough;

    int numPred = dvmCountSetBits(firstBB->predecessors);
    /*
     * A loop body should have at least two incoming edges. Here we go with the
     * simple case and only form loops if numPred == 2.
     */
    if (numPred != 2) return false;

    BitVectorIterator bvIterator;
    GrowableList *blockList = &cUnit->blockList;
    BasicBlock *predBB = NULL;

    dvmBitVectorIteratorInit(firstBB->predecessors, &bvIterator);
    while (true) {
        int predIdx = dvmBitVectorIteratorNext(&bvIterator);
        if (predIdx == -1) break;
        predBB = (BasicBlock *) dvmGrowableListGetElement(blockList, predIdx);
        if (predBB != cUnit->entryBlock) break;
    }

    /* Used to record which block is in the loop */
    dvmClearAllBits(cUnit->tempBlockV);

    dvmCompilerSetBit(cUnit->tempBlockV, predBB->id);

    /* Form a loop by only including iDom block that is also a predecessor */
    while (predBB != firstBB) {
        BasicBlock *iDom = predBB->iDom;
        if (!dvmIsBitSet(predBB->predecessors, iDom->id)) {
            return false;
        /*
         * And don't form nested loops (ie by detecting if the branch target
         * of iDom dominates iDom).
         */
        } else if (iDom->taken &&
                   dvmIsBitSet(iDom->dominators, iDom->taken->id) &&
                   iDom != firstBB) {
            return false;
        }
        dvmCompilerSetBit(cUnit->tempBlockV, iDom->id);
        predBB = iDom;
    }

    /* Add the entry block and first block */
    dvmCompilerSetBit(cUnit->tempBlockV, firstBB->id);
    dvmCompilerSetBit(cUnit->tempBlockV, cUnit->entryBlock->id);

    /* Now mark blocks not included in the loop as hidden */
    GrowableListIterator iterator;
    dvmGrowableListIteratorInit(&cUnit->blockList, &iterator);
    while (true) {
        BasicBlock *bb = (BasicBlock *) dvmGrowableListIteratorNext(&iterator);
        if (bb == NULL) break;
        if (!dvmIsBitSet(cUnit->tempBlockV, bb->id)) {
            bb->hidden = true;
            /* Clear the insn list */
            bb->firstMIRInsn = bb->lastMIRInsn = NULL;
            resetBlockEdges(bb);
        }
    }

    dvmCompilerDataFlowAnalysisDispatcher(cUnit, clearPredecessorVector,
                                          kAllNodes, false /* isIterative */);

    dvmGrowableListIteratorInit(&cUnit->blockList, &iterator);
    while (true) {
        BasicBlock *bb = (BasicBlock *) dvmGrowableListIteratorNext(&iterator);
        if (bb == NULL) break;
        if (dvmIsBitSet(cUnit->tempBlockV, bb->id)) {
            if (bb->taken) {
                /*
                 * exit block means we run into control-flow that we don't want
                 * to handle.
                 */
                if (bb->taken == cUnit->exitBlock) {
                    return false;
                }
                if (bb->taken->hidden) {
                    bb->taken->blockType = kChainingCellNormal;
                    bb->taken->hidden = false;
                }
                dvmCompilerSetBit(bb->taken->predecessors, bb->id);
            }
            if (bb->fallThrough) {
                /*
                 * exit block means we run into control-flow that we don't want
                 * to handle.
                 */
                if (bb->fallThrough == cUnit->exitBlock) {
                    return false;
                }
                if (bb->fallThrough->hidden) {
                    bb->fallThrough->blockType = kChainingCellNormal;
                    bb->fallThrough->hidden = false;
                }
                dvmCompilerSetBit(bb->fallThrough->predecessors, bb->id);
            }
            /* Loop blocks shouldn't contain any successor blocks (yet) */
            assert(bb->successorBlockList.blockListType == kNotUsed);
        }
    }

    return true;
}
