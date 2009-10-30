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

#include "Codegen.h"
#include "../../Dataflow.h"

/*
 * Register usage for 16-bit Thumb systems:
 *     r0-r3: Temp/argument
 *     lr(r14):      Temp for translations, return address for handlers
 *     rGLUE(r6):    Pointer to InterpState
 *     rFP(r5):      Dalvik frame pointer
 *     r4, r7:       Temp for translations
 *     r8, r9, r10:   Temp preserved across C calls
 *     r11, ip(r12):  Temp not preserved across C calls
 *
 * Register usage for 32-bit Thumb systems:
 *     r0-r3: Temp/argument
 *     lr(r14):      Temp for translations, return address for handlers
 *     rGLUE(r6):    Pointer to InterpState
 *     rFP(r5):      Dalvik frame pointer
 *     r4, r7:       Temp for translations
 *     r8, r9, r10   Temp preserved across C calls
 *     r11, ip(r12):      Temp not preserved across C calls
 *     fp0-fp15:     Hot temps, not preserved across C calls
 *     fp16-fp31:    Promotion pool
 *
 */

static ArmLIR *storeBaseDisp(CompilationUnit *cUnit, int rBase,
                             int displacement, int rSrc, OpSize size);
static ArmLIR *storeBaseDispWide(CompilationUnit *cUnit, int rBase,
                                 int displacement, int rSrcLo, int rSrcHi);
static int allocTypedTempPair(CompilationUnit *cUnit, bool fpHint,
                              int regClass);
static int allocTypedTemp(CompilationUnit *cUnit, bool fpHint, int regClass);
void genRegCopyWide(CompilationUnit *cUnit, int destLo, int destHi,
                    int srcLo, int srcHi);
static ArmLIR *genRegCopy(CompilationUnit *cUnit, int rDest, int rSrc);
static void clobberReg(CompilationUnit *cUnit, int reg);
static RegisterInfo *getRegInfo(CompilationUnit *cUnit, int reg);

#define SREG(c, s) ((c)->regLocation[(s)].sRegLow)
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

static int sReg2vReg(CompilationUnit *cUnit, int sReg)
{
    assert(sReg != INVALID_SREG);
    return DECODE_REG(dvmConvertSSARegToDalvik(cUnit, sReg));
}

static bool liveOut(CompilationUnit *cUnit, int sReg)
{
    //TODO: fully implement
    return true;
}

/*
 * Free all allocated temps in the temp pools.  Note that this does
 * not affect the "liveness" of a temp register, which will stay
 * live until it is either explicitly killed or reallocated.
 */
static void resetRegPool(CompilationUnit *cUnit)
{
    int i;
    for (i=0; i < cUnit->regPool->numCoreTemps; i++) {
        cUnit->regPool->coreTemps[i].inUse = false;
    }
    for (i=0; i < cUnit->regPool->numFPTemps; i++) {
        cUnit->regPool->FPTemps[i].inUse = false;
    }
}

 /* Set up temp & preserved register pools specialized by target */
static void initPool(RegisterInfo *regs, int *regNums, int num)
{
    int i;
    for (i=0; i < num; i++) {
        regs[i].reg = regNums[i];
        regs[i].inUse = false;
        regs[i].pair = false;
        regs[i].live = false;
        regs[i].dirty = false;
        regs[i].sReg = INVALID_SREG;
    }
}

static void dumpRegPool(RegisterInfo *p, int numRegs)
{
    int i;
    LOGE("================================================");
    for (i=0; i < numRegs; i++ ){
        LOGE("R[%d]: U:%d, P:%d, part:%d, LV:%d, D:%d, SR:%d, ST:%x, EN:%x",
           p[i].reg, p[i].inUse, p[i].pair, p[i].partner, p[i].live,
           p[i].dirty, p[i].sReg,(int)p[i].defStart, (int)p[i].defEnd);
    }
    LOGE("================================================");
}

static int allocTempBody(CompilationUnit *cUnit, RegisterInfo *p, int numTemps,
                         bool required)
{
    int i;
    //Tuning: redo this to widen the live window on freed temps
    for (i=0; i< numTemps; i++) {
        if (!p[i].inUse && !p[i].live) {
            clobberReg(cUnit, p[i].reg);
            p[i].inUse = true;
            p[i].pair = false;
            return p[i].reg;
        }
    }
    for (i=0; i< numTemps; i++) {
        if (!p[i].inUse) {
            clobberReg(cUnit, p[i].reg);
            p[i].inUse = true;
            p[i].pair = false;
            return p[i].reg;
        }
    }
    if (required) {
        LOGE("No free temp registers");
        assert(0);
    }
    return -1;  // No register available
}

//REDO: too many assumptions.
static int allocTempDouble(CompilationUnit *cUnit)
{
    RegisterInfo *p = cUnit->regPool->FPTemps;
    int numTemps = cUnit->regPool->numFPTemps;
    int i;

    for (i=0; i < numTemps; i+=2) {
        if ((!p[i].inUse && !p[i].live) &&
            (!p[i+1].inUse && !p[i+1].live)) {
            clobberReg(cUnit, p[i].reg);
            clobberReg(cUnit, p[i+1].reg);
            p[i].inUse = true;
            p[i+1].inUse = true;
            assert((p[i].reg+1) == p[i+1].reg);
            assert((p[i].reg & 0x1) == 0);
            return p[i].reg;
        }
    }
    for (i=0; i < numTemps; i+=2) {
        if (!p[i].inUse && !p[i+1].inUse) {
            clobberReg(cUnit, p[i].reg);
            clobberReg(cUnit, p[i+1].reg);
            p[i].inUse = true;
            p[i+1].inUse = true;
            assert((p[i].reg+1) == p[i+1].reg);
            assert((p[i].reg & 0x1) == 0);
            return p[i].reg;
        }
    }
    LOGE("No free temp registers");
    *((int*)0) = 0;  //For development, die instantly.  Later abort translation
    dvmAbort();
    return -1;
}

/* Return a temp if one is available, -1 otherwise */
static int allocFreeTemp(CompilationUnit *cUnit)
{
    return allocTempBody(cUnit, cUnit->regPool->coreTemps,
                         cUnit->regPool->numCoreTemps, true);
}

static int allocTemp(CompilationUnit *cUnit)
{
    return allocTempBody(cUnit, cUnit->regPool->coreTemps,
                         cUnit->regPool->numCoreTemps, true);
}

static int allocTempFloat(CompilationUnit *cUnit)
{
    return allocTempBody(cUnit, cUnit->regPool->FPTemps,
                         cUnit->regPool->numFPTemps, true);
}

static RegisterInfo *allocLiveBody(RegisterInfo *p, int numTemps, int sReg)
{
    int i;
    if (sReg == -1)
        return NULL;
    for (i=0; i < numTemps; i++) {
        if (p[i].live && (p[i].sReg == sReg)) {
            p[i].inUse = true;
            return &p[i];
        }
    }
    return NULL;
}

static RegisterInfo *allocLive(CompilationUnit *cUnit, int sReg,
                               int regClass)
{
    RegisterInfo *res = NULL;
    switch(regClass) {
        case kAnyReg:
            res = allocLiveBody(cUnit->regPool->FPTemps,
                                cUnit->regPool->numFPTemps, sReg);
            if (res)
                break;
            /* Intentional fallthrough */
        case kCoreReg:
            res = allocLiveBody(cUnit->regPool->coreTemps,
                                cUnit->regPool->numCoreTemps, sReg);
            break;
        case kFPReg:
            res = allocLiveBody(cUnit->regPool->FPTemps,
                                cUnit->regPool->numFPTemps, sReg);
            break;
        default:
            LOGE("Invalid register type");
            assert(0);
            dvmAbort();
    }
    return res;
}

static void freeTemp(CompilationUnit *cUnit, int reg)
{
    RegisterInfo *p = cUnit->regPool->coreTemps;
    int numTemps = cUnit->regPool->numCoreTemps;
    int i;
    for (i=0; i< numTemps; i++) {
        if (p[i].reg == reg) {
            p[i].inUse = false;
            p[i].pair = false;
            return;
        }
    }
    p = cUnit->regPool->FPTemps;
    numTemps = cUnit->regPool->numFPTemps;
    for (i=0; i< numTemps; i++) {
        if (p[i].reg == reg) {
            p[i].inUse = false;
            p[i].pair = false;
            return;
        }
    }
    LOGE("Tried to free a non-existant temp: r%d",reg);
    dvmAbort();
}

//FIXME - this needs to also check the preserved pool.
static RegisterInfo *isLive(CompilationUnit *cUnit, int reg)
{
    RegisterInfo *p = cUnit->regPool->coreTemps;
    int numTemps = cUnit->regPool->numCoreTemps;
    int i;
    for (i=0; i< numTemps; i++) {
        if (p[i].reg == reg) {
            return p[i].live ? &p[i] : NULL;
        }
    }
    p = cUnit->regPool->FPTemps;
    numTemps = cUnit->regPool->numFPTemps;
    for (i=0; i< numTemps; i++) {
        if (p[i].reg == reg) {
            return p[i].live ? &p[i] : NULL;
        }
    }
    return NULL;
}

static RegisterInfo *isTemp(CompilationUnit *cUnit, int reg)
{
    RegisterInfo *p = cUnit->regPool->coreTemps;
    int numTemps = cUnit->regPool->numCoreTemps;
    int i;
    for (i=0; i< numTemps; i++) {
        if (p[i].reg == reg) {
            return &p[i];
        }
    }
    p = cUnit->regPool->FPTemps;
    numTemps = cUnit->regPool->numFPTemps;
    for (i=0; i< numTemps; i++) {
        if (p[i].reg == reg) {
            return &p[i];
        }
    }
    return NULL;
}

static void flushRegWide(CompilationUnit *cUnit, int reg1, int reg2)
{
    RegisterInfo *info1 = getRegInfo(cUnit, reg1);
    RegisterInfo *info2 = getRegInfo(cUnit, reg2);
    assert(info1 && info2 && info1->pair && info2->pair &&
           (info1->partner == info2->reg) &&
           (info2->partner == info1->reg));
    if ((info1->live && info1->dirty) || (info2->live && info2->dirty)) {
        info1->dirty = false;
        info2->dirty = false;
        if (sReg2vReg(cUnit, info2->sReg) < sReg2vReg(cUnit, info1->sReg))
            info1 = info2;
        storeBaseDispWide(cUnit, rFP, sReg2vReg(cUnit, info1->sReg) << 2,
                          info1->reg, info1->partner);
    }
}

static void flushReg(CompilationUnit *cUnit, int reg)
{
    RegisterInfo *info = getRegInfo(cUnit, reg);
    if (info->live && info->dirty) {
        info->dirty = false;
        storeBaseDisp(cUnit, rFP, sReg2vReg(cUnit, info->sReg) << 2, reg,
                      kWord);
    }
}

/* return true if found reg to clobber */
static bool clobberRegBody(CompilationUnit *cUnit, RegisterInfo *p,
                           int numTemps, int reg)
{
    int i;
    for (i=0; i< numTemps; i++) {
        if (p[i].reg == reg) {
            if (p[i].live && p[i].dirty) {
                if (p[i].pair) {
                    flushRegWide(cUnit, p[i].reg, p[i].partner);
                } else {
                    flushReg(cUnit, p[i].reg);
                }
            }
            p[i].live = false;
            p[i].sReg = INVALID_SREG;
            p[i].defStart = NULL;
            p[i].defEnd = NULL;
            if (p[i].pair) {
                p[i].pair = false;
                clobberReg(cUnit, p[i].partner);
            }
            return true;
        }
    }
    return false;
}

/* Mark a temp register as dead.  Does not affect allocation state. */
static void clobberReg(CompilationUnit *cUnit, int reg)
{
    if (!clobberRegBody(cUnit, cUnit->regPool->coreTemps,
                        cUnit->regPool->numCoreTemps, reg)) {
        clobberRegBody(cUnit, cUnit->regPool->FPTemps,
                       cUnit->regPool->numFPTemps, reg);
    }
}

static void clobberSRegBody(RegisterInfo *p, int numTemps, int sReg)
{
    int i;
    for (i=0; i< numTemps; i++) {
        if (p[i].sReg == sReg) {
            p[i].live = false;
            p[i].defStart = NULL;
            p[i].defEnd = NULL;
        }
    }
}

/* Clobber any temp associated with an sReg.  Could be in either class */
static void clobberSReg(CompilationUnit *cUnit, int sReg)
{
    clobberSRegBody(cUnit->regPool->coreTemps, cUnit->regPool->numCoreTemps,
                    sReg);
    clobberSRegBody(cUnit->regPool->FPTemps, cUnit->regPool->numFPTemps,
                    sReg);
}

static RegisterInfo *getRegInfo(CompilationUnit *cUnit, int reg)
{
    int numTemps = cUnit->regPool->numCoreTemps;
    RegisterInfo *p = cUnit->regPool->coreTemps;
    int i;
    for (i=0; i< numTemps; i++) {
        if (p[i].reg == reg) {
            return &p[i];
        }
    }
    p = cUnit->regPool->FPTemps;
    numTemps = cUnit->regPool->numFPTemps;
    for (i=0; i< numTemps; i++) {
        if (p[i].reg == reg) {
            return &p[i];
        }
    }
    LOGE("Tried to get info on a non-existant temp: r%d",reg);
    dvmAbort();
    return NULL;
}

/*
 * Similar to allocTemp(), but forces the allocation of a specific
 * register.  No check is made to see if the register was previously
 * allocated.  Use with caution.
 */
static void lockTemp(CompilationUnit *cUnit, int reg)
{
    RegisterInfo *p = cUnit->regPool->coreTemps;
    int numTemps = cUnit->regPool->numCoreTemps;
    int i;
    for (i=0; i< numTemps; i++) {
        if (p[i].reg == reg) {
            p[i].inUse = true;
            p[i].live = false;
            return;
        }
    }
    p = cUnit->regPool->FPTemps;
    numTemps = cUnit->regPool->numFPTemps;
    for (i=0; i< numTemps; i++) {
        if (p[i].reg == reg) {
            p[i].inUse = true;
            p[i].live = false;
            return;
        }
    }
    LOGE("Tried to lock a non-existant temp: r%d",reg);
    dvmAbort();
}

static void lockArgRegs(CompilationUnit *cUnit)
{
    lockTemp(cUnit, r0);
    lockTemp(cUnit, r1);
    lockTemp(cUnit, r2);
    lockTemp(cUnit, r3);
}

/* Clobber all regs that might be used by an external C call */
static void clobberCallRegs(CompilationUnit *cUnit)
{
    clobberReg(cUnit, r0);
    clobberReg(cUnit, r1);
    clobberReg(cUnit, r2);
    clobberReg(cUnit, r3);
    clobberReg(cUnit, r9); // Not sure we need to do this, be convervative
    clobberReg(cUnit, r11);
    clobberReg(cUnit, r12);
    clobberReg(cUnit, rlr);
}

/* Clobber all of the temps that might be used by a handler. */
static void clobberHandlerRegs(CompilationUnit *cUnit)
{
    //TUNING: reduce the set of regs used by handlers.  Only a few need lots.
    clobberCallRegs(cUnit);
    clobberReg(cUnit, r9);
    clobberReg(cUnit, r10);
}

static void resetDef(CompilationUnit *cUnit, int reg)
{
    RegisterInfo *p = getRegInfo(cUnit, reg);
    p->defStart = NULL;
    p->defEnd = NULL;
}

static void nullifyRange(CompilationUnit *cUnit, LIR *start, LIR *finish,
                         int sReg1, int sReg2)
{
    if (start && finish) {
        LIR *p;
        assert(sReg1 == sReg2);
        for (p = start; ;p = p->next) {
            ((ArmLIR *)p)->isNop = true;
            if (p == finish)
                break;
        }
    }
}

/*
 * Mark the beginning and end LIR of a def sequence.  Note that
 * on entry start points to the LIR prior to the beginning of the
 * sequence.
 */
static void markDef(CompilationUnit *cUnit, RegLocation rl,
                    LIR *start, LIR *finish)
{
    assert(!rl.wide);
    assert(start && start->next);
    assert(finish);
    RegisterInfo *p = getRegInfo(cUnit, rl.lowReg);
    p->defStart = start->next;
    p->defEnd = finish;
}

/*
 * Mark the beginning and end LIR of a def sequence.  Note that
 * on entry start points to the LIR prior to the beginning of the
 * sequence.
 */
static void markDefWide(CompilationUnit *cUnit, RegLocation rl,
                        LIR *start, LIR *finish)
{
    assert(rl.wide);
    assert(start && start->next);
    assert(finish);
    RegisterInfo *p = getRegInfo(cUnit, rl.lowReg);
    resetDef(cUnit, rl.highReg);  // Only track low of pair
    p->defStart = start->next;
    p->defEnd = finish;
}

static RegLocation wideToNarrowLoc(CompilationUnit *cUnit, RegLocation rl)
{
    assert(rl.wide);
    if (rl.location == kLocPhysReg) {
        RegisterInfo *infoLo = getRegInfo(cUnit, rl.lowReg);
        RegisterInfo *infoHi = getRegInfo(cUnit, rl.highReg);
        if (!infoLo->pair) {
            dumpRegPool(cUnit->regPool->coreTemps, cUnit->regPool->numCoreTemps);
            assert(infoLo->pair);
        }
        if (!infoHi->pair) {
            dumpRegPool(cUnit->regPool->coreTemps, cUnit->regPool->numCoreTemps);
            assert(infoHi->pair);
        }
        assert(infoLo->pair);
        assert(infoHi->pair);
        assert(infoLo->partner == infoHi->reg);
        assert(infoHi->partner == infoLo->reg);
        infoLo->pair = false;
        infoHi->pair = false;
        infoLo->defStart = NULL;
        infoLo->defEnd = NULL;
        infoHi->defStart = NULL;
        infoHi->defEnd = NULL;
    }
    rl.wide = false;
    return rl;
}

static void resetDefLoc(CompilationUnit *cUnit, RegLocation rl)
{
    assert(!rl.wide);
    if (!(gDvmJit.disableOpt & (1 << kSuppressLoads))) {
        RegisterInfo *p = getRegInfo(cUnit, rl.lowReg);
        assert(!p->pair);
        nullifyRange(cUnit, p->defStart, p->defEnd,
                     p->sReg, rl.sRegLow);
    }
    resetDef(cUnit, rl.lowReg);
}

static void resetDefLocWide(CompilationUnit *cUnit, RegLocation rl)
{
    assert(rl.wide);
    if (!(gDvmJit.disableOpt & (1 << kSuppressLoads))) {
        RegisterInfo *p = getRegInfo(cUnit, rl.lowReg);
        assert(p->pair);
        nullifyRange(cUnit, p->defStart, p->defEnd,
                     p->sReg, rl.sRegLow);
    }
    resetDef(cUnit, rl.lowReg);
    resetDef(cUnit, rl.highReg);
}

static void resetDefTracking(CompilationUnit *cUnit)
{
    int i;
    for (i=0; i< cUnit->regPool->numCoreTemps; i++) {
        resetDef(cUnit, cUnit->regPool->coreTemps[i].reg);
    }
    for (i=0; i< cUnit->regPool->numFPTemps; i++) {
        resetDef(cUnit, cUnit->regPool->FPTemps[i].reg);
    }
}

static void clobberAllRegs(CompilationUnit *cUnit)
{
    int i;
    for (i=0; i< cUnit->regPool->numCoreTemps; i++) {
        clobberReg(cUnit, cUnit->regPool->coreTemps[i].reg);
    }
    for (i=0; i< cUnit->regPool->numFPTemps; i++) {
        clobberReg(cUnit, cUnit->regPool->FPTemps[i].reg);
    }
}

/* To be used when explicitly managing register use */
static void lockAllTemps(CompilationUnit *cUnit)
{
    int i;
    for (i=0; i< cUnit->regPool->numCoreTemps; i++) {
        lockTemp(cUnit, cUnit->regPool->coreTemps[i].reg);
    }
}

// Make sure nothing is live and dirty
static void flushAllRegsBody(CompilationUnit *cUnit, RegisterInfo *info,
                             int numRegs)
{
    int i;
    for (i=0; i < numRegs; i++) {
        if (info[i].live && info[i].dirty) {
            if (info[i].pair) {
                flushRegWide(cUnit, info[i].reg, info[i].partner);
            } else {
                flushReg(cUnit, info[i].reg);
            }
        }
    }
}

static void flushAllRegs(CompilationUnit *cUnit)
{
    flushAllRegsBody(cUnit, cUnit->regPool->coreTemps,
                     cUnit->regPool->numCoreTemps);
    flushAllRegsBody(cUnit, cUnit->regPool->FPTemps,
                     cUnit->regPool->numFPTemps);
    clobberAllRegs(cUnit);
}


//TUNING: rewrite all of this reg stuff.  Probably use an attribute table
static bool regClassMatches(int regClass, int reg)
{
    if (regClass == kAnyReg) {
        return true;
    } else if (regClass == kCoreReg) {
        return !FPREG(reg);
    } else {
        return FPREG(reg);
    }
}

static void markRegLive(CompilationUnit *cUnit, int reg, int sReg)
{
    RegisterInfo *info = getRegInfo(cUnit, reg);
    if ((info->reg == reg) && (info->sReg == sReg) && info->live) {
        return;  /* already live */
    } else if (sReg != INVALID_SREG) {
        clobberSReg(cUnit, sReg);
        info->live = true;
    } else {
        /* Can't be live if no associated sReg */
        info->live = false;
    }
    info->sReg = sReg;
}

static void markRegPair(CompilationUnit *cUnit, int lowReg, int highReg)
{
    RegisterInfo *infoLo = getRegInfo(cUnit, lowReg);
    RegisterInfo *infoHi = getRegInfo(cUnit, highReg);
    infoLo->pair = infoHi->pair = true;
    infoLo->partner = highReg;
    infoHi->partner = lowReg;
}

static void markRegSingle(CompilationUnit *cUnit, int reg)
{
    RegisterInfo *info = getRegInfo(cUnit, reg);
    info->pair = false;
}

static void markRegClean(CompilationUnit *cUnit, int reg)
{
    RegisterInfo *info = getRegInfo(cUnit, reg);
    info->dirty = false;
}

static void markRegDirty(CompilationUnit *cUnit, int reg)
{
    RegisterInfo *info = getRegInfo(cUnit, reg);
    info->dirty = true;
}

static void markRegInUse(CompilationUnit *cUnit, int reg)
{
    RegisterInfo *info = getRegInfo(cUnit, reg);
    info->inUse = true;
}

/* Return true if live & dirty */
static bool isDirty(CompilationUnit *cUnit, int reg)
{
    RegisterInfo *info = getRegInfo(cUnit, reg);
    return (info && info->live && info->dirty);
}

void copyRegInfo(CompilationUnit *cUnit, int newReg, int oldReg)
{
    RegisterInfo *newInfo = getRegInfo(cUnit, newReg);
    RegisterInfo *oldInfo = getRegInfo(cUnit, oldReg);
    *newInfo = *oldInfo;
    newInfo->reg = newReg;
}

/*
 * Return an updated location record with current in-register status.
 * If the value lives in live temps, reflect that fact.  No code
 * is generated.  The the live value is part of an older pair,
 * clobber both low and high.
 * TUNING: clobbering both is a bit heavy-handed, but the alternative
 * is a bit complex when dealing with FP regs.  Examine code to see
 * if it's worthwhile trying to be more clever here.
 */
static RegLocation updateLoc(CompilationUnit *cUnit, RegLocation loc)
{
    assert(!loc.wide);
    if (loc.location == kLocDalvikFrame) {
        RegisterInfo *infoLo = allocLive(cUnit, loc.sRegLow, kAnyReg);
        if (infoLo) {
            if (infoLo->pair) {
                clobberReg(cUnit, infoLo->reg);
                clobberReg(cUnit, infoLo->partner);
            } else {
                loc.lowReg = infoLo->reg;
                loc.location = kLocPhysReg;
            }
        }
    }

    return loc;
}

/* see comments for updateLoc */
static RegLocation updateLocWide(CompilationUnit *cUnit, RegLocation loc)
{
    assert(loc.wide);
    if (loc.location == kLocDalvikFrame) {
        RegisterInfo *infoLo = allocLive(cUnit, loc.sRegLow, kAnyReg);
        RegisterInfo *infoHi = allocLive(cUnit, hiSReg(loc.sRegLow), kAnyReg);
        bool match = true;
        match = match && (infoLo != NULL);
        match = match && (infoHi != NULL);
        match = match && (FPREG(infoLo->reg) == FPREG(infoHi->reg));
        if (match && FPREG(infoLo->reg)) {
            match &= ((infoLo->reg & 0x1) == 0);
            match &= ((infoHi->reg - infoLo->reg) == 1);
        }
        if (match) {
            loc.lowReg = infoLo->reg;
            loc.highReg = infoHi->reg;
            loc.location = kLocPhysReg;
            markRegPair(cUnit, loc.lowReg, loc.highReg);
            assert(!FPREG(loc.lowReg) || ((loc.lowReg & 0x1) == 0));
            return loc;
        }
        /* Can't easily reuse - just clobber any overlaps */
        if (infoLo) {
            clobberReg(cUnit, infoLo->reg);
            if (infoLo->pair)
                clobberReg(cUnit, infoLo->partner);
        }
        if (infoHi) {
            clobberReg(cUnit, infoHi->reg);
            if (infoHi->pair)
                clobberReg(cUnit, infoHi->partner);
        }
    }

    return loc;
}

static RegLocation evalLocWide(CompilationUnit *cUnit, RegLocation loc,
                               int regClass, bool update)
{
    assert(loc.wide);
    int newRegs;
    int lowReg;
    int highReg;

    loc = updateLocWide(cUnit, loc);

    /* If already in registers, we can assume proper form.  Right reg class? */
    if (loc.location == kLocPhysReg) {
        assert(FPREG(loc.lowReg) == FPREG(loc.highReg));
        assert(!FPREG(loc.lowReg) || ((loc.lowReg & 0x1) == 0));
        if (!regClassMatches(regClass, loc.lowReg)) {
            /* Wrong register class.  Reallocate and copy */
            newRegs = allocTypedTempPair(cUnit, loc.fp, regClass);
            lowReg = newRegs & 0xff;
            highReg = (newRegs >> 8) & 0xff;
            genRegCopyWide(cUnit, lowReg, highReg, loc.lowReg,
                           loc.highReg);
            copyRegInfo(cUnit, lowReg, loc.lowReg);
            copyRegInfo(cUnit, highReg, loc.highReg);
            clobberReg(cUnit, loc.lowReg);
            clobberReg(cUnit, loc.highReg);
            loc.lowReg = lowReg;
            loc.highReg = highReg;
            markRegPair(cUnit, loc.lowReg, loc.highReg);
            assert(!FPREG(loc.lowReg) || ((loc.lowReg & 0x1) == 0));
        }
        return loc;
    }

    assert((loc.location != kLocRetval) || (loc.sRegLow == INVALID_SREG));
    assert((loc.location != kLocRetval) || (hiSReg(loc.sRegLow) == INVALID_SREG));

    newRegs = allocTypedTempPair(cUnit, loc.fp, regClass);
    loc.lowReg = newRegs & 0xff;
    loc.highReg = (newRegs >> 8) & 0xff;

    markRegPair(cUnit, loc.lowReg, loc.highReg);
    if (update) {
        loc.location = kLocPhysReg;
        markRegLive(cUnit, loc.lowReg, loc.sRegLow);
        markRegLive(cUnit, loc.highReg, hiSReg(loc.sRegLow));
    }
    assert(!FPREG(loc.lowReg) || ((loc.lowReg & 0x1) == 0));
    return loc;
}

static RegLocation evalLoc(CompilationUnit *cUnit, RegLocation loc,
                               int regClass, bool update)
{
    RegisterInfo *infoLo = NULL;
    int newReg;
    if (loc.wide)
        return evalLocWide(cUnit, loc, regClass, update);
    loc = updateLoc(cUnit, loc);

    if (loc.location == kLocPhysReg) {
        if (!regClassMatches(regClass, loc.lowReg)) {
            /* Wrong register class.  Realloc, copy and transfer ownership */
            newReg = allocTypedTemp(cUnit, loc.fp, regClass);
            genRegCopy(cUnit, newReg, loc.lowReg);
            copyRegInfo(cUnit, newReg, loc.lowReg);
            clobberReg(cUnit, loc.lowReg);
            loc.lowReg = newReg;
        }
        return loc;
    }

    assert((loc.location != kLocRetval) || (loc.sRegLow == INVALID_SREG));

    newReg = allocTypedTemp(cUnit, loc.fp, regClass);
    loc.lowReg = newReg;

    if (update) {
        loc.location = kLocPhysReg;
        markRegLive(cUnit, loc.lowReg, loc.sRegLow);
    }
    return loc;
}

static inline int getSrcSSAName(MIR *mir, int num)
{
    assert(mir->ssaRep->numUses > num);
    return mir->ssaRep->uses[num];
}

static inline int getDestSSAName(MIR *mir, int num)
{
    assert(mir->ssaRep->numDefs > num);
    return mir->ssaRep->defs[num];
}

// Get the LocRecord associated with an SSA name use.
static inline RegLocation getSrcLoc(CompilationUnit *cUnit, MIR *mir, int num)
{
    RegLocation loc = cUnit->regLocation[SREG(cUnit, getSrcSSAName(mir, num))];
    loc.fp = cUnit->regLocation[getSrcSSAName(mir, num)].fp;
    loc.wide = false;
    return loc;
}

// Get the LocRecord associated with an SSA name def.
static inline RegLocation getDestLoc(CompilationUnit *cUnit, MIR *mir, int num)
{
    RegLocation loc = cUnit->regLocation[SREG(cUnit, getDestSSAName(mir, num))];
    loc.fp = cUnit->regLocation[getDestSSAName(mir, num)].fp;
    loc.wide = false;
    return loc;
}

static RegLocation getLocWide(CompilationUnit *cUnit, MIR *mir,
                                         int low, int high, bool isSrc)
{
    RegLocation lowLoc;
    RegLocation highLoc;
    /* Copy loc record for low word and patch in data from high word */
    if (isSrc) {
        lowLoc = getSrcLoc(cUnit, mir, low);
        highLoc = getSrcLoc(cUnit, mir, high);
    } else {
        lowLoc = getDestLoc(cUnit, mir, low);
        highLoc = getDestLoc(cUnit, mir, high);
    }
    /* Avoid this case by either promoting both or neither. */
    assert(lowLoc.location == highLoc.location);
    if (lowLoc.location == kLocPhysReg) {
        /* This case shouldn't happen if we've named correctly */
        assert(lowLoc.fp == highLoc.fp);
    }
    lowLoc.wide = true;
    lowLoc.highReg = highLoc.lowReg;
    return lowLoc;
}
static RegLocation getDestLocWide(CompilationUnit *cUnit, MIR *mir,
                                         int low, int high)
{
    return getLocWide(cUnit, mir, low, high, false);
}

static RegLocation getSrcLocWide(CompilationUnit *cUnit, MIR *mir,
                                         int low, int high)
{
    return getLocWide(cUnit, mir, low, high, true);
}

/* Reset the tracker to unknown state */
static inline void resetNullCheckTracker(CompilationUnit *cUnit)
{
    dvmClearAllBits(cUnit->regPool->nullCheckedRegs);
}

static RegLocation getReturnLocWide(CompilationUnit *cUnit)
{
    RegLocation res = LOC_C_RETURN_WIDE;
    clobberReg(cUnit, r0);
    clobberReg(cUnit, r1);
    markRegInUse(cUnit, r0);
    markRegInUse(cUnit, r1);
    markRegPair(cUnit, res.lowReg, res.highReg);
    return res;
}

static RegLocation getReturnLocWideAlt(CompilationUnit *cUnit)
{
    RegLocation res = LOC_C_RETURN_WIDE;
    res.lowReg = r2;
    res.highReg = r3;
    clobberReg(cUnit, r2);
    clobberReg(cUnit, r3);
    markRegInUse(cUnit, r2);
    markRegInUse(cUnit, r3);
    markRegPair(cUnit, res.lowReg, res.highReg);
    return res;
}

static RegLocation getReturnLoc(CompilationUnit *cUnit)
{
    RegLocation res = LOC_C_RETURN;
    clobberReg(cUnit, r0);
    markRegInUse(cUnit, r0);
    return res;
}

static RegLocation getReturnLocAlt(CompilationUnit *cUnit)
{
    RegLocation res = LOC_C_RETURN;
    res.lowReg = r1;
    clobberReg(cUnit, r1);
    markRegInUse(cUnit, r1);
    return res;
}

/* Kill the corresponding bit in the null-checked register list */
static inline void killNullCheckedLocation(CompilationUnit *cUnit,
                                           RegLocation loc)
{
    if (loc.location != kLocRetval) {
        assert(loc.sRegLow != INVALID_SREG);
        dvmClearBit(cUnit->regPool->nullCheckedRegs, loc.sRegLow);
        if (loc.wide) {
            assert(hiSReg(loc.sRegLow) != INVALID_SREG);
            dvmClearBit(cUnit->regPool->nullCheckedRegs, hiSReg(loc.sRegLow));
        }
    }
}
