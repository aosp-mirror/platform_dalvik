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
#include "vm/compiler/CompilerInternals.h"
#include "ArmLIR.h"

ArmLIR* dvmCompilerGenCopy(CompilationUnit *cUnit, int rDest, int rSrc);

/* Is this a Dalvik register access? */
static inline bool isDalvikLoad(ArmLIR *lir)
{
    return ((lir->operands[1] == rFP) &&
            ((lir->opCode == THUMB_LDR_RRI5) ||
             (lir->opCode == THUMB2_LDR_RRI12) ||
             (lir->opCode == THUMB2_VLDRS) ||
             (lir->opCode == THUMB2_VLDRD)));
}

static inline bool isDalvikStore(ArmLIR *lir)
{
    return ((lir->operands[1] == rFP) &&
            ((lir->opCode == THUMB_STR_RRI5) ||
             (lir->opCode == THUMB2_STR_RRI12) ||
             (lir->opCode == THUMB2_VSTRS) ||
             (lir->opCode == THUMB2_VSTRD)));
}

/* Double regs overlap float regs.  Return true if collision  */
static bool regClobber(int reg1, int reg2)
{
    int reg1a, reg1b;
    int reg2a, reg2b;
    if (!FPREG(reg1) || !FPREG(reg2))
        return (reg1 == reg2);
    if (DOUBLEREG(reg1)) {
        reg1a = reg1 & FP_REG_MASK;
        reg1b = reg1a + 1;
    } else {
        reg1a = reg1b = reg1 & FP_REG_MASK;
    }
    if (DOUBLEREG(reg2)) {
        reg2a = reg2 & FP_REG_MASK;
        reg2b = reg2a + 1;
    } else {
        reg2a = reg2b = reg2 & FP_REG_MASK;
    }
    return (reg1a == reg2a) || (reg1a == reg2b) ||
           (reg1b == reg2a) || (reg1b == reg2b);
}
/*
 * Perform a pass of top-down walk to
 * 1) Eliminate redundant loads and stores
 * 2) Sink stores to latest possible slot
 */
static void applyLoadStoreElimination(CompilationUnit *cUnit,
                                      ArmLIR *headLIR,
                                      ArmLIR *tailLIR)
{
    ArmLIR *thisLIR;

    cUnit->optRound++;
    for (thisLIR = headLIR;
         thisLIR != tailLIR;
         thisLIR = NEXT_LIR(thisLIR)) {
        /* Skip newly added instructions */
        if (thisLIR->age >= cUnit->optRound) {
            continue;
        }
        if (isDalvikStore(thisLIR)) {
            int dRegId = thisLIR->operands[2];
            int nativeRegId = thisLIR->operands[0];
            ArmLIR *checkLIR;
            int sinkDistance = 0;
            /*
             * Add r15 (pc) to the mask to prevent this instruction
             * from sinking past branch instructions.
             */
            u8 stopMask = ENCODE_GP_REG(rpc) | thisLIR->useMask;

            for (checkLIR = NEXT_LIR(thisLIR);
                 checkLIR != tailLIR;
                 checkLIR = NEXT_LIR(checkLIR)) {

                /* Check if a Dalvik register load is redundant */
                if (isDalvikLoad(checkLIR) &&
                    checkLIR->operands[2] == dRegId ) {
                    if (FPREG(nativeRegId) != FPREG(checkLIR->operands[0])) {
                        break;  // TODO: handle gen<=>float copies
                    }
                    /* Insert a move to replace the load */
                    if (checkLIR->operands[0] != nativeRegId) {
                        ArmLIR *moveLIR;
                        moveLIR = dvmCompilerRegCopy(cUnit,
                                                    checkLIR->operands[0],
                                                    nativeRegId);
                        /*
                         * Insertion is guaranteed to succeed since checkLIR
                         * is never the first LIR on the list
                         */
                        dvmCompilerInsertLIRBefore((LIR *) checkLIR,
                                                   (LIR *) moveLIR);
                    }
                    checkLIR->isNop = true;
                    continue;

                /* Found a true output dependency - nuke the previous store */
                } else if (isDalvikStore(checkLIR) &&
                           checkLIR->operands[2] == dRegId) {
                    thisLIR->isNop = true;
                    break;
                /* Find out the latest slot that the store can be sunk into */
                } else {
                    bool stopHere = false;

                    /* Last instruction reached */
                    stopHere |= NEXT_LIR(checkLIR) == tailLIR;

                    /*
                     * Conservatively assume there is a memory dependency
                     * for st/ld multiples and reg+reg address mode
                     */
                    stopHere |= checkLIR->opCode == THUMB_STMIA ||
                                checkLIR->opCode == THUMB_LDMIA ||
                                checkLIR->opCode == THUMB_STR_RRR ||
                                checkLIR->opCode == THUMB_LDR_RRR ||
                                checkLIR->opCode == THUMB2_STR_RRR ||
                                checkLIR->opCode == THUMB2_LDR_RRR ||
                                checkLIR->opCode == THUMB2_STMIA ||
                                checkLIR->opCode == THUMB2_LDMIA ||
                                checkLIR->opCode == THUMB2_VLDRD ||
                                checkLIR->opCode == THUMB2_VSTRD;


                    /* Store data is clobbered */
                    stopHere |= (stopMask & checkLIR->defMask) != 0;

                    /* Found a new place to put the store - move it here */
                    if (stopHere == true) {
                        /* The store can be sunk for at least one cycle */
                        if (sinkDistance != 0) {
                            ArmLIR *newStoreLIR =
                                dvmCompilerNew(sizeof(ArmLIR), true);
                            *newStoreLIR = *thisLIR;
                            newStoreLIR->age = cUnit->optRound;
                            /*
                             * Insertion is guaranteed to succeed since checkLIR
                             * is never the first LIR on the list
                             */
                            dvmCompilerInsertLIRBefore((LIR *) checkLIR,
                                                       (LIR *) newStoreLIR);
                            thisLIR->isNop = true;
                        }
                        break;
                    }

                    /*
                     * Saw a real instruction that the store can be sunk after
                     */
                    if (!isPseudoOpCode(checkLIR->opCode)) {
                        sinkDistance++;
                    }
                }
            }
        }
    }
}

static void applyLoadHoisting(CompilationUnit *cUnit,
                              ArmLIR *headLIR,
                              ArmLIR *tailLIR)
{
    ArmLIR *thisLIR;

    cUnit->optRound++;
    for (thisLIR = headLIR;
         thisLIR != tailLIR;
         thisLIR = NEXT_LIR(thisLIR)) {
        /* Skip newly added instructions */
        if (thisLIR->age >= cUnit->optRound ||
            thisLIR->isNop == true) {
            continue;
        }
        if (isDalvikLoad(thisLIR)) {
            int dRegId = thisLIR->operands[2];
            int nativeRegId = thisLIR->operands[0];
            ArmLIR *checkLIR;
            int hoistDistance = 0;
            u8 stopUseMask = ENCODE_GP_REG(rpc) | thisLIR->useMask;
            u8 stopDefMask = thisLIR->defMask;

            for (checkLIR = PREV_LIR(thisLIR);
                 checkLIR != headLIR;
                 checkLIR = PREV_LIR(checkLIR)) {

                if (checkLIR->isNop) continue;

                /* Check if the current load is redundant */
                if ((isDalvikLoad(checkLIR) || isDalvikStore(checkLIR)) &&
                    checkLIR->operands[2] == dRegId ) {
                    if (FPREG(nativeRegId) != FPREG(checkLIR->operands[0])) {
                        break;  // TODO: handle gen<=>float copies
                    }
                    /* Insert a move to replace the load */
                    if (checkLIR->operands[0] != nativeRegId) {
                        ArmLIR *moveLIR;
                        moveLIR = dvmCompilerRegCopy(cUnit,
                                                    nativeRegId,
                                                    checkLIR->operands[0]);
                        /*
                         * Convert *thisLIR* load into a move
                         */
                        dvmCompilerInsertLIRAfter((LIR *) checkLIR,
                                                  (LIR *) moveLIR);
                    }
                    cUnit->printMe = true;
                    thisLIR->isNop = true;
                    break;

                /* Find out if the load can be yanked past the checkLIR */
                } else {
                    bool stopHere = false;

                    /* Last instruction reached */
                    stopHere |= PREV_LIR(checkLIR) == headLIR;

                    /*
                     * Conservatively assume there is a memory dependency
                     * for st/ld multiples and reg+reg address mode
                     */
                    stopHere |= checkLIR->opCode == THUMB_STMIA ||
                                checkLIR->opCode == THUMB_LDMIA ||
                                checkLIR->opCode == THUMB_STR_RRR ||
                                checkLIR->opCode == THUMB_LDR_RRR ||
                                checkLIR->opCode == THUMB2_STR_RRR ||
                                checkLIR->opCode == THUMB2_LDR_RRR ||
                                checkLIR->opCode == THUMB2_STMIA ||
                                checkLIR->opCode == THUMB2_LDMIA ||
                                checkLIR->opCode == THUMB2_VLDRD ||
                                checkLIR->opCode == THUMB2_VSTRD;

                    /* Base address is clobbered by checkLIR */
                    stopHere |= (stopUseMask & checkLIR->defMask) != 0;

                    /* Load target clobbers use/def in checkLIR */
                    stopHere |= (stopDefMask &
                                 (checkLIR->useMask | checkLIR->defMask)) != 0;

                    /* Found a new place to put the load - move it here */
                    if (stopHere == true) {
                        /* The store can be hoisted for at least one cycle */
                        if (hoistDistance != 0) {
                            ArmLIR *newLoadLIR =
                                dvmCompilerNew(sizeof(ArmLIR), true);
                            *newLoadLIR = *thisLIR;
                            newLoadLIR->age = cUnit->optRound;
                            /*
                             * Insertion is guaranteed to succeed since checkLIR
                             * is never the first LIR on the list
                             */
                            dvmCompilerInsertLIRAfter((LIR *) checkLIR,
                                                      (LIR *) newLoadLIR);
                            thisLIR->isNop = true;
                            cUnit->printMe = true;
                        }
                        break;
                    }

                    /*
                     * Saw a real instruction that the store can be sunk after
                     */
                    if (!isPseudoOpCode(checkLIR->opCode)) {
                        hoistDistance++;
                    }
                }
            }
        }
    }
}

void dvmCompilerApplyLocalOptimizations(CompilationUnit *cUnit, LIR *headLIR,
                                        LIR *tailLIR)
{
    if (!(gDvmJit.disableOpt & (1 << kLoadStoreElimination))) {
        applyLoadStoreElimination(cUnit, (ArmLIR *) headLIR,
                                  (ArmLIR *) tailLIR);
    }
    if (!(gDvmJit.disableOpt & (1 << kLoadHoisting))) {
        applyLoadHoisting(cUnit, (ArmLIR *) headLIR, (ArmLIR *) tailLIR);
    }
}
