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
        if (thisLIR->opCode == THUMB_STR_RRI5 &&
            thisLIR->operands[1] == rFP) {
            int dRegId = thisLIR->operands[2];
            int nativeRegId = thisLIR->operands[0];
            ArmLIR *checkLIR;
            int sinkDistance = 0;

            for (checkLIR = NEXT_LIR(thisLIR);
                 checkLIR != tailLIR;
                 checkLIR = NEXT_LIR(checkLIR)) {

                /* Check if a Dalvik register load is redundant */
                if (checkLIR->opCode == THUMB_LDR_RRI5 &&
                    checkLIR->operands[1] == rFP &&
                    checkLIR->operands[2] == dRegId) {
                    /* Insert a move to replace the load */
                    if (checkLIR->operands[0] != nativeRegId) {
                        ArmLIR *moveLIR =
                            dvmCompilerNew(sizeof(ArmLIR), true);
                        moveLIR->opCode = THUMB_MOV_RR;
                        moveLIR->operands[0] = checkLIR->operands[0];
                        moveLIR->operands[1] = nativeRegId;
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
                } else if (checkLIR->opCode == THUMB_STR_RRI5 &&
                           checkLIR->operands[1] == rFP &&
                           checkLIR->operands[2] == dRegId) {
                    thisLIR->isNop = true;
                    break;
                /* Find out the latest slot that the store can be sunk into */
                } else {
                    bool stopHere = false;

                    /* Last instruction reached */
                    stopHere |= checkLIR->generic.next == NULL;

                    /* Store data is clobbered */
                    stopHere |= (EncodingMap[checkLIR->opCode].flags &
                                 CLOBBER_DEST) != 0 &&
                                checkLIR->operands[0] == nativeRegId;
                    /*
                     * Conservatively assume there is a memory dependency
                     * for st/ld multiples and reg+reg address mode
                     */
                    stopHere |= checkLIR->opCode == THUMB_STMIA ||
                                checkLIR->opCode == THUMB_LDMIA ||
                                checkLIR->opCode == THUMB_STR_RRR ||
                                checkLIR->opCode == THUMB_LDR_RRR;

// FIXME: need to enhance this code to sink & play well with coprocessor ld/str
                    stopHere |= checkLIR->opCode == THUMB2_VSTRS ||
                                checkLIR->opCode == THUMB2_VSTRD ||
                                checkLIR->opCode == THUMB2_VLDRS ||
                                checkLIR->opCode == THUMB2_VLDRD;

                    stopHere |= (EncodingMap[checkLIR->opCode].flags &
                                 IS_BRANCH) != 0;

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

void dvmCompilerApplyLocalOptimizations(CompilationUnit *cUnit, LIR *headLIR,
                                        LIR *tailLIR)
{
    applyLoadStoreElimination(cUnit,
                              (ArmLIR *) headLIR,
                              (ArmLIR *) tailLIR);
}
