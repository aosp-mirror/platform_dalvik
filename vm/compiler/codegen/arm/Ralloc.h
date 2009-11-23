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

/*
 * This file contains register alloction support and is intended to be
 * included by:
 *
 *        Codegen-$(TARGET_ARCH_VARIANT).c
 *
 */

#include "compiler/CompilerUtility.h"
#include "compiler/CompilerIR.h"
#include "compiler/Dataflow.h"
#include "compiler/codegen/arm/ArmLIR.h"

/*
 * The following are register allocation routines exposed to the code generator
 * FIXME - dvmCompiler prefixes are not added yet
 */

static inline int sReg2vReg(CompilationUnit *cUnit, int sReg)
{
    assert(sReg != INVALID_SREG);
    return DECODE_REG(dvmConvertSSARegToDalvik(cUnit, sReg));
}

/* Reset the tracker to unknown state */
static inline void resetNullCheckTracker(CompilationUnit *cUnit)
{
    dvmClearAllBits(cUnit->regPool->nullCheckedRegs);
}

/*
 * Get the "real" sreg number associated with an sReg slot.  In general,
 * sReg values passed through codegen are the SSA names created by
 * dataflow analysis and refer to slot numbers in the cUnit->regLocation
 * array.  However, renaming is accomplished by simply replacing RegLocation
 * entries in the cUnit->reglocation[] array.  Therefore, when location
 * records for operands are first created, we need to ask the locRecord
 * identified by the dataflow pass what it's new name is.
 */

static inline int hiSReg(int lowSreg) {
    return (lowSreg == INVALID_SREG) ? INVALID_SREG : lowSreg + 1;
}


static inline bool liveOut(CompilationUnit *cUnit, int sReg)
{
    //TODO: fully implement
    return true;
}

static inline int getSrcSSAName(MIR *mir, int num)
{
    assert(mir->ssaRep->numUses > num);
    return mir->ssaRep->uses[num];
}

extern RegLocation evalLoc(CompilationUnit *cUnit, RegLocation loc,
                               int regClass, bool update);
/* Mark a temp register as dead.  Does not affect allocation state. */
extern void clobberReg(CompilationUnit *cUnit, int reg);

extern RegLocation updateLoc(CompilationUnit *cUnit, RegLocation loc);

/* see comments for updateLoc */
extern RegLocation updateLocWide(CompilationUnit *cUnit, RegLocation loc);

/* Clobber all of the temps that might be used by a handler. */
extern void clobberHandlerRegs(CompilationUnit *cUnit);

extern void markRegLive(CompilationUnit *cUnit, int reg, int sReg);

extern void markRegDirty(CompilationUnit *cUnit, int reg);

extern void markRegPair(CompilationUnit *cUnit, int lowReg, int highReg);

extern void markRegClean(CompilationUnit *cUnit, int reg);

extern void resetDef(CompilationUnit *cUnit, int reg);

extern void resetDefLoc(CompilationUnit *cUnit, RegLocation rl);

/* Set up temp & preserved register pools specialized by target */
extern void initPool(RegisterInfo *regs, int *regNums, int num);

/*
 * Mark the beginning and end LIR of a def sequence.  Note that
 * on entry start points to the LIR prior to the beginning of the
 * sequence.
 */
extern void markDef(CompilationUnit *cUnit, RegLocation rl,
                    LIR *start, LIR *finish);
/*
 * Mark the beginning and end LIR of a def sequence.  Note that
 * on entry start points to the LIR prior to the beginning of the
 * sequence.
 */
extern void markDefWide(CompilationUnit *cUnit, RegLocation rl,
                        LIR *start, LIR *finish);

extern RegLocation getSrcLocWide(CompilationUnit *cUnit, MIR *mir,
                                         int low, int high);

extern RegLocation getDestLocWide(CompilationUnit *cUnit, MIR *mir,
                                         int low, int high);
// Get the LocRecord associated with an SSA name use.
extern RegLocation getSrcLoc(CompilationUnit *cUnit, MIR *mir, int num);

// Get the LocRecord associated with an SSA name def.
extern RegLocation getDestLoc(CompilationUnit *cUnit, MIR *mir, int num);

extern RegLocation getReturnLocWide(CompilationUnit *cUnit);

/* Clobber all regs that might be used by an external C call */
extern void clobberCallRegs(CompilationUnit *cUnit);

extern RegisterInfo *isTemp(CompilationUnit *cUnit, int reg);

extern void markRegInUse(CompilationUnit *cUnit, int reg);

extern int allocTemp(CompilationUnit *cUnit);

extern int allocTempFloat(CompilationUnit *cUnit);

//REDO: too many assumptions.
extern int allocTempDouble(CompilationUnit *cUnit);

extern void freeTemp(CompilationUnit *cUnit, int reg);

extern void resetDefLocWide(CompilationUnit *cUnit, RegLocation rl);

extern void resetDefTracking(CompilationUnit *cUnit);

/* Kill the corresponding bit in the null-checked register list */
extern void killNullCheckedLocation(CompilationUnit *cUnit, RegLocation loc);

//FIXME - this needs to also check the preserved pool.
extern RegisterInfo *isLive(CompilationUnit *cUnit, int reg);

/* To be used when explicitly managing register use */
extern void lockAllTemps(CompilationUnit *cUnit);

extern void flushAllRegs(CompilationUnit *cUnit);

extern RegLocation getReturnLocWideAlt(CompilationUnit *cUnit);

extern RegLocation getReturnLoc(CompilationUnit *cUnit);

extern RegLocation getReturnLocAlt(CompilationUnit *cUnit);

/* Clobber any temp associated with an sReg.  Could be in either class */
extern void clobberSReg(CompilationUnit *cUnit, int sReg);

/* Return a temp if one is available, -1 otherwise */
extern int allocFreeTemp(CompilationUnit *cUnit);

/*
 * Similar to allocTemp(), but forces the allocation of a specific
 * register.  No check is made to see if the register was previously
 * allocated.  Use with caution.
 */
extern void lockTemp(CompilationUnit *cUnit, int reg);

extern RegLocation wideToNarrowLoc(CompilationUnit *cUnit, RegLocation rl);

/*
 * Free all allocated temps in the temp pools.  Note that this does
 * not affect the "liveness" of a temp register, which will stay
 * live until it is either explicitly killed or reallocated.
 */
extern void resetRegPool(CompilationUnit *cUnit);

extern void clobberAllRegs(CompilationUnit *cUnit);

extern void resetDefTracking(CompilationUnit *cUnit);
