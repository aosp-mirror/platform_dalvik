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

typedef struct LiveRange {
    int ssaName;
    bool active;
    int first;
    int last;
} LiveRange;

int computeLiveRange(LiveRange *list, BasicBlock *bb, int seqNum)
{
    MIR *mir;
    int i;

    if (bb->blockType != kDalvikByteCode &&
        bb->blockType != kTraceEntryBlock)
        return seqNum;

    for (mir = bb->firstMIRInsn; mir; mir = mir->next) {
        SSARepresentation *ssaRep = mir->ssaRep;
        mir->seqNum = seqNum;
        if (ssaRep) {
            for (i=0; i< ssaRep->numUses; i++) {
                int reg = ssaRep->uses[i];
                list[reg].first = MIN(list[reg].first, seqNum);
                list[reg].active = true;
            }
            for (i=0; i< ssaRep->numDefs; i++) {
                int reg = ssaRep->defs[i];
                list[reg].last = MAX(list[reg].last, seqNum + 1);
                list[reg].active = true;
            }
            seqNum += 2;
        }
    }
    return seqNum;
}

/*
 * Quick & dirty - make FP usage sticky.  This is strictly a hint - local
 * code generation will handle misses.  It might be worthwhile to collaborate
 * with dx/dexopt to avoid reusing the same Dalvik temp for values of
 * different types.
 */
static void inferTypes(CompilationUnit *cUnit, BasicBlock *bb)
{
    MIR *mir;
    if (bb->blockType != kDalvikByteCode &&
        bb->blockType != kTraceEntryBlock)
        return;

    for (mir = bb->firstMIRInsn; mir; mir = mir->next) {
        SSARepresentation *ssaRep = mir->ssaRep;
        if (ssaRep) {
            int i;
            for (i=0; ssaRep->fpUse && i< ssaRep->numUses; i++) {
                if (ssaRep->fpUse[i])
                    cUnit->regLocation[ssaRep->uses[i]].fp = true;
            }
            for (i=0; ssaRep->fpDef && i< ssaRep->numDefs; i++) {
                if (ssaRep->fpDef[i])
                    cUnit->regLocation[ssaRep->defs[i]].fp = true;
            }
        }
    }
}

/*
 * Determine whether to use simple or aggressive register allocation.  In
 * general, loops and full methods will get aggressive.
 */
static bool simpleTrace(CompilationUnit *cUnit)
{
    //TODO: flesh out
    return true;
}

/*
 * Target-independent register allocation.  Requires target-dependent
 * helper functions and assumes free list, temp list and spill region.
 * Uses a variant of linear scan and produces a mapping between SSA names
 * and location.  Location may be original Dalvik register, hardware
 * register or spill location.
 *
 * Method:
 *    0.  Allocate the structure to hold the SSA name life ranges
 *    1.  Number each MIR instruction, counting by 2.
 *        +0 -> The "read" of the operands
 *        +1 -> The definition of the target resource
 *    2.  Compute live ranges for all SSA names *not* including the
 *        subscript 0 original Dalvik names.  Phi functions ignored
 *        at this point.
 *    3.  Sort the live range list by lowest range start.
 *    4.  Process and remove all Phi functions.
 *        o If there is no live range collisions among all operands and
 *          the target of a Phi function, collapse operands and target
 *          and rewrite using target SSA name.
 *        o If there is a collision, introduce copies.
 *    5.  Allocate in order of increasing live range start.
 */
static const RegLocation freshLoc = {kLocDalvikFrame, 0, 0, INVALID_REG,
                                     INVALID_REG, INVALID_SREG};
void dvmCompilerRegAlloc(CompilationUnit *cUnit)
{
    int i;
    int seqNum = 0;
    LiveRange *ranges;
    RegLocation *loc;

    /* Allocate the location map */
    loc = (RegLocation*)dvmCompilerNew(cUnit->numSSARegs * sizeof(*loc), true);
    for (i=0; i< cUnit->numSSARegs; i++) {
        loc[i] = freshLoc;
        loc[i].sRegLow = i;
    }
    cUnit->regLocation = loc;

    /* Do type inference pass */
    for (i=0; i < cUnit->numBlocks; i++) {
        inferTypes(cUnit, cUnit->blockList[i]);
    }

    if (simpleTrace(cUnit)) {
        /*
         * Just rename everything back to subscript 0 names and don't do
         * any explicit promotion.  Local allocator will opportunistically
         * promote on the fly.
         */
        for (i=0; i < cUnit->numSSARegs; i++) {
            cUnit->regLocation[i].sRegLow =
                DECODE_REG(dvmConvertSSARegToDalvik(cUnit, loc[i].sRegLow));
        }
    } else {
        // Compute live ranges
        ranges = dvmCompilerNew(cUnit->numSSARegs * sizeof(*ranges), true);
        for (i=0; i < cUnit->numSSARegs; i++)
            ranges[i].active = false;
        seqNum = computeLiveRange(ranges, cUnit->blockList[i], seqNum);
        //TODO: phi squash & linear scan promotion
    }
}
