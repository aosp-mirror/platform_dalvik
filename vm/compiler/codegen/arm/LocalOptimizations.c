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
#include "Codegen.h"

#define DEBUG_OPT(X)

ArmLIR* dvmCompilerGenCopy(CompilationUnit *cUnit, int rDest, int rSrc);

/* Is this a Dalvik register access? */
static inline bool isDalvikLoad(ArmLIR *lir)
{
    return (lir->useMask != ~0ULL) && (lir->useMask & ENCODE_DALVIK_REG);
}

static inline bool isDalvikStore(ArmLIR *lir)
{
    return (lir->defMask != ~0ULL) && (lir->defMask & ENCODE_DALVIK_REG);
}

static inline bool isDalvikRegisterClobbered(ArmLIR *lir1, ArmLIR *lir2)
{
  int reg1Lo = DECODE_ALIAS_INFO_REG(lir1->aliasInfo);
  int reg1Hi = reg1Lo + DECODE_ALIAS_INFO_WIDE(lir1->aliasInfo);
  int reg2Lo = DECODE_ALIAS_INFO_REG(lir2->aliasInfo);
  int reg2Hi = reg2Lo + DECODE_ALIAS_INFO_WIDE(lir2->aliasInfo);

  return (reg1Lo == reg2Lo) || (reg1Lo == reg2Hi) || (reg1Hi == reg2Lo);
}

static void dumpDependentInsnPair(ArmLIR *thisLIR, ArmLIR *checkLIR,
                                  const char *optimization)
{
    LOGD("************ %s ************", optimization);
    dvmDumpLIRInsn((LIR *) thisLIR, 0);
    dvmDumpLIRInsn((LIR *) checkLIR, 0);
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
            int dRegId = DECODE_ALIAS_INFO_REG(thisLIR->aliasInfo);
            int dRegIdHi = dRegId + DECODE_ALIAS_INFO_WIDE(thisLIR->aliasInfo);
            int nativeRegId = thisLIR->operands[0];
            ArmLIR *checkLIR;
            int sinkDistance = 0;
            /*
             * Add r15 (pc) to the mask to prevent this instruction
             * from sinking past branch instructions. Unset the Dalvik register
             * bit when checking with native resource constraints.
             */
            u8 stopMask = (ENCODE_REG_PC | thisLIR->useMask) &
                          ~ENCODE_DALVIK_REG;

            for (checkLIR = NEXT_LIR(thisLIR);
                 checkLIR != tailLIR;
                 checkLIR = NEXT_LIR(checkLIR)) {

                /* Check if a Dalvik register load is redundant */
                if (isDalvikLoad(checkLIR) &&
                    (checkLIR->aliasInfo == thisLIR->aliasInfo) &&
                    (REGTYPE(checkLIR->operands[0]) == REGTYPE(nativeRegId))) {
                    /* Insert a move to replace the load */
                    if (checkLIR->operands[0] != nativeRegId) {
                        ArmLIR *moveLIR;
                        moveLIR = dvmCompilerRegCopyNoInsert(
                                    cUnit, checkLIR->operands[0], nativeRegId);
                        /*
                         * Insertion is guaranteed to succeed since checkLIR
                         * is never the first LIR on the list
                         */
                        dvmCompilerInsertLIRBefore((LIR *) checkLIR,
                                                   (LIR *) moveLIR);
                    }
                    checkLIR->isNop = true;
                    continue;

                /*
                 * Found a true output dependency - nuke the previous store.
                 * The register type doesn't matter here.
                 */
                } else if (isDalvikStore(checkLIR) &&
                           (checkLIR->aliasInfo == thisLIR->aliasInfo)) {
                    thisLIR->isNop = true;
                    break;
                /* Find out the latest slot that the store can be sunk into */
                } else {
                    /* Last instruction reached */
                    bool stopHere = (NEXT_LIR(checkLIR) == tailLIR);

                    /* Store data is clobbered */
                    stopHere |= ((stopMask & checkLIR->defMask) != 0);

                    /* Store data partially clobbers the Dalvik register */
                    if (stopHere == false &&
                        ((checkLIR->useMask | checkLIR->defMask) &
                         ENCODE_DALVIK_REG)) {
                        stopHere = isDalvikRegisterClobbered(thisLIR, checkLIR);
                    }

                    /* Found a new place to put the store - move it here */
                    if (stopHere == true) {
                        DEBUG_OPT(dumpDependentInsnPair(thisLIR, checkLIR,
                                                        "SINK STORE"));
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
            int dRegId = DECODE_ALIAS_INFO_REG(thisLIR->aliasInfo);
            int dRegIdHi = dRegId + DECODE_ALIAS_INFO_WIDE(thisLIR->aliasInfo);
            int nativeRegId = thisLIR->operands[0];
            ArmLIR *checkLIR;
            int hoistDistance = 0;
            u8 stopUseMask = (ENCODE_REG_PC | thisLIR->useMask) &
                             ~ENCODE_DALVIK_REG;
            u8 stopDefMask = thisLIR->defMask & ~ENCODE_DALVIK_REG;

            /* First check if the load can be completely elinimated */
            for (checkLIR = PREV_LIR(thisLIR);
                 checkLIR != headLIR;
                 checkLIR = PREV_LIR(checkLIR)) {

                if (checkLIR->isNop) continue;

                /*
                 * Check if the Dalvik register is previously accessed
                 * with exactly the same type.
                 */
                if ((isDalvikLoad(checkLIR) || isDalvikStore(checkLIR)) &&
                    (checkLIR->aliasInfo == thisLIR->aliasInfo) &&
                    (checkLIR->operands[0] == nativeRegId)) {
                    /*
                     * If it is previously accessed but with a different type,
                     * the search will terminate later at the point checking
                     * for partially overlapping stores.
                     */
                    thisLIR->isNop = true;
                    break;
                }

                /*
                 * No earlier use/def can reach this load if:
                 * 1) Head instruction is reached
                 * 2) load target register is clobbered
                 * 3) A branch is seen (stopUseMask has the PC bit set).
                 */
                if ((checkLIR == headLIR) ||
                    (stopUseMask | stopDefMask) & checkLIR->defMask) {
                    break;
                }

                /* Store data partially clobbers the Dalvik register */
                if (isDalvikStore(checkLIR) &&
                    isDalvikRegisterClobbered(thisLIR, checkLIR)) {
                    break;
                }
            }

            /* The load has been eliminated */
            if (thisLIR->isNop) continue;

            /*
             * The load cannot be eliminated. See if it can be hoisted to an
             * earlier spot.
             */
            for (checkLIR = PREV_LIR(thisLIR);
                 /* empty by intention */;
                 checkLIR = PREV_LIR(checkLIR)) {

                if (checkLIR->isNop) continue;

                /* Check if the current load is redundant */
                if ((isDalvikLoad(checkLIR) || isDalvikStore(checkLIR)) &&
                    (checkLIR->aliasInfo == thisLIR->aliasInfo) &&
                    (REGTYPE(checkLIR->operands[0]) == REGTYPE(nativeRegId))) {
                    /* Insert a move to replace the load */
                    if (checkLIR->operands[0] != nativeRegId) {
                        ArmLIR *moveLIR;
                        moveLIR = dvmCompilerRegCopyNoInsert(
                                    cUnit, nativeRegId, checkLIR->operands[0]);
                        /*
                         * Convert *thisLIR* load into a move
                         */
                        dvmCompilerInsertLIRAfter((LIR *) checkLIR,
                                                  (LIR *) moveLIR);
                    }
                    thisLIR->isNop = true;
                    break;

                /* Find out if the load can be yanked past the checkLIR */
                } else {
                    /* Last instruction reached */
                    bool stopHere = (checkLIR == headLIR);

                    /* Base address is clobbered by checkLIR */
                    stopHere |= ((stopUseMask & checkLIR->defMask) != 0);

                    /* Load target clobbers use/def in checkLIR */
                    stopHere |= ((stopDefMask &
                                 (checkLIR->useMask | checkLIR->defMask)) != 0);

                    /* Store data partially clobbers the Dalvik register */
                    if (stopHere == false &&
                        (checkLIR->defMask & ENCODE_DALVIK_REG)) {
                        stopHere = isDalvikRegisterClobbered(thisLIR, checkLIR);
                    }

                    /*
                     * Stop at an earlier Dalvik load if the offset of checkLIR
                     * is not less than thisLIR
                     *
                     * Experiments show that doing
                     *
                     * ldr     r1, [r5, #16]
                     * ldr     r0, [r5, #20]
                     *
                     * is much faster than
                     *
                     * ldr     r0, [r5, #20]
                     * ldr     r1, [r5, #16]
                     */
                    if (isDalvikLoad(checkLIR)) {
                        int dRegId2 =
                            DECODE_ALIAS_INFO_REG(checkLIR->aliasInfo);
                        if (dRegId2 <= dRegId) {
                            stopHere = true;
                        }
                    }

                    /* Found a new place to put the load - move it here */
                    if (stopHere == true) {
                        DEBUG_OPT(dumpDependentInsnPair(thisLIR, checkLIR,
                                                        "HOIST LOAD"));
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
                        }
                        break;
                    }

                    /*
                     * Saw a real instruction that hosting the load is
                     * beneficial
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
