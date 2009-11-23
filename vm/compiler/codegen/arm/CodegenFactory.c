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
 * This file contains codegen and support common to all supported
 * ARM variants.  It is included by:
 *
 *        Codegen-$(TARGET_ARCH_VARIANT).c
 *
 * which combines this common code with specific support found in the
 * applicable directory below this one.
 */


/* Load a word at base + displacement.  Displacement must be word multiple */
static ArmLIR *loadWordDisp(CompilationUnit *cUnit, int rBase, int displacement,
                            int rDest)
{
    return loadBaseDisp(cUnit, NULL, rBase, displacement, rDest, kWord,
                        INVALID_SREG);
}

static ArmLIR *storeWordDisp(CompilationUnit *cUnit, int rBase,
                             int displacement, int rSrc)
{
    return storeBaseDisp(cUnit, rBase, displacement, rSrc, kWord);
}

/*
 * Load a Dalvik register into a physical register.  Take care when
 * using this routine, as it doesn't perform any bookkeeping regarding
 * register liveness.  That is the responsibility of the caller.
 */
static void loadValueDirect(CompilationUnit *cUnit, RegLocation rlSrc,
                                int reg1)
{
    rlSrc = updateLoc(cUnit, rlSrc);  /* Is our value hiding in a live temp? */
    if (rlSrc.location == kLocPhysReg) {
        genRegCopy(cUnit, reg1, rlSrc.lowReg);
    } else  if (rlSrc.location == kLocRetval) {
        loadWordDisp(cUnit, rGLUE, offsetof(InterpState, retval), reg1);
    } else {
        assert(rlSrc.location == kLocDalvikFrame);
        loadWordDisp(cUnit, rFP, sReg2vReg(cUnit, rlSrc.sRegLow) << 2,
                     reg1);
    }
}

/*
 * Similar to loadValueDirect, but clobbers and allocates the target
 * register.  Should be used when loading to a fixed register (for example,
 * loading arguments to an out of line call.
 */
static void loadValueDirectFixed(CompilationUnit *cUnit, RegLocation rlSrc,
                                 int reg1)
{
    clobberReg(cUnit, reg1);
    markRegInUse(cUnit, reg1);
    loadValueDirect(cUnit, rlSrc, reg1);
}

/*
 * Load a Dalvik register pair into a physical register[s].  Take care when
 * using this routine, as it doesn't perform any bookkeeping regarding
 * register liveness.  That is the responsibility of the caller.
 */
static void loadValueDirectWide(CompilationUnit *cUnit, RegLocation rlSrc,
                                int regLo, int regHi)
{
    rlSrc = updateLocWide(cUnit, rlSrc);
    if (rlSrc.location == kLocPhysReg) {
        genRegCopyWide(cUnit, regLo, regHi, rlSrc.lowReg, rlSrc.highReg);
    } else if (rlSrc.location == kLocRetval) {
        loadBaseDispWide(cUnit, NULL, rGLUE, offsetof(InterpState, retval),
                         regLo, regHi, INVALID_SREG);
    } else {
        assert(rlSrc.location == kLocDalvikFrame);
            loadBaseDispWide(cUnit, NULL, rFP,
                             sReg2vReg(cUnit, rlSrc.sRegLow) << 2,
                             regLo, regHi, INVALID_SREG);
    }
}

/*
 * Similar to loadValueDirect, but clobbers and allocates the target
 * registers.  Should be used when loading to a fixed registers (for example,
 * loading arguments to an out of line call.
 */
static void loadValueDirectWideFixed(CompilationUnit *cUnit, RegLocation rlSrc,
                                     int regLo, int regHi)
{
    clobberReg(cUnit, regLo);
    clobberReg(cUnit, regHi);
    markRegInUse(cUnit, regLo);
    markRegInUse(cUnit, regHi);
    loadValueDirectWide(cUnit, rlSrc, regLo, regHi);
}

static RegLocation loadValue(CompilationUnit *cUnit, RegLocation rlSrc,
                             RegisterClass opKind)
{
    RegisterInfo *pReg;
    rlSrc = evalLoc(cUnit, rlSrc, opKind, false);
    if (rlSrc.location == kLocDalvikFrame) {
        loadValueDirect(cUnit, rlSrc, rlSrc.lowReg);
        rlSrc.location = kLocPhysReg;
        markRegLive(cUnit, rlSrc.lowReg, rlSrc.sRegLow);
    } else if (rlSrc.location == kLocRetval) {
        loadWordDisp(cUnit, rGLUE, offsetof(InterpState, retval), rlSrc.lowReg);
        rlSrc.location = kLocPhysReg;
        clobberReg(cUnit, rlSrc.lowReg);
    }
    return rlSrc;
}

static void storeValue(CompilationUnit *cUnit, RegLocation rlDest,
                       RegLocation rlSrc)
{
    RegisterInfo *pRegLo;
    LIR *defStart;
    LIR *defEnd;
    assert(!rlDest.wide);
    assert(!rlSrc.wide);
    killNullCheckedLocation(cUnit, rlDest);
    rlSrc = updateLoc(cUnit, rlSrc);
    rlDest = updateLoc(cUnit, rlDest);
    if (rlSrc.location == kLocPhysReg) {
        if (isLive(cUnit, rlSrc.lowReg) || (rlDest.location == kLocPhysReg)) {
            // Src is live or Dest has assigned reg.
            rlDest = evalLoc(cUnit, rlDest, kAnyReg, false);
            genRegCopy(cUnit, rlDest.lowReg, rlSrc.lowReg);
        } else {
            // Just re-assign the registers.  Dest gets Src's regs
            rlDest.lowReg = rlSrc.lowReg;
            clobberReg(cUnit, rlSrc.lowReg);
        }
    } else {
        // Load Src either into promoted Dest or temps allocated for Dest
        rlDest = evalLoc(cUnit, rlDest, kAnyReg, false);
        loadValueDirect(cUnit, rlSrc, rlDest.lowReg);
    }

    // Dest is now live and dirty (until/if we flush it to home location)
    markRegLive(cUnit, rlDest.lowReg, rlDest.sRegLow);
    markRegDirty(cUnit, rlDest.lowReg);


    if (rlDest.location == kLocRetval) {
        storeBaseDisp(cUnit, rGLUE, offsetof(InterpState, retval),
                      rlDest.lowReg, kWord);
        clobberReg(cUnit, rlDest.lowReg);
    } else {
        resetDefLoc(cUnit, rlDest);
        if (liveOut(cUnit, rlDest.sRegLow)) {
            defStart = (LIR *)cUnit->lastLIRInsn;
            int vReg = sReg2vReg(cUnit, rlDest.sRegLow);
            storeBaseDisp(cUnit, rFP, vReg << 2, rlDest.lowReg, kWord);
            markRegClean(cUnit, rlDest.lowReg);
            defEnd = (LIR *)cUnit->lastLIRInsn;
            markDef(cUnit, rlDest, defStart, defEnd);
        }
    }
}

static RegLocation loadValueWide(CompilationUnit *cUnit, RegLocation rlSrc,
                                 RegisterClass opKind)
{
    RegisterInfo *pRegLo;
    RegisterInfo *pRegHi;
    assert(rlSrc.wide);
    rlSrc = evalLoc(cUnit, rlSrc, opKind, false);
    if (rlSrc.location == kLocDalvikFrame) {
        loadValueDirectWide(cUnit, rlSrc, rlSrc.lowReg, rlSrc.highReg);
        rlSrc.location = kLocPhysReg;
        markRegLive(cUnit, rlSrc.lowReg, rlSrc.sRegLow);
        markRegLive(cUnit, rlSrc.highReg, hiSReg(rlSrc.sRegLow));
    } else if (rlSrc.location == kLocRetval) {
        loadBaseDispWide(cUnit, NULL, rGLUE, offsetof(InterpState, retval),
                         rlSrc.lowReg, rlSrc.highReg, INVALID_SREG);
        rlSrc.location = kLocPhysReg;
        clobberReg(cUnit, rlSrc.lowReg);
        clobberReg(cUnit, rlSrc.highReg);
    }
    return rlSrc;
}

static void storeValueWide(CompilationUnit *cUnit, RegLocation rlDest,
                       RegLocation rlSrc)
{
    RegisterInfo *pRegLo;
    RegisterInfo *pRegHi;
    LIR *defStart;
    LIR *defEnd;
    bool srcFP = FPREG(rlSrc.lowReg) && FPREG(rlSrc.highReg);
    assert(FPREG(rlSrc.lowReg)==FPREG(rlSrc.highReg));
    assert(rlDest.wide);
    assert(rlSrc.wide);
    killNullCheckedLocation(cUnit, rlDest);
    if (rlSrc.location == kLocPhysReg) {
        if (isLive(cUnit, rlSrc.lowReg) || isLive(cUnit, rlSrc.highReg) ||
            (rlDest.location == kLocPhysReg)) {
            // Src is live or Dest has assigned reg.
            rlDest = evalLoc(cUnit, rlDest, kAnyReg, false);
            genRegCopyWide(cUnit, rlDest.lowReg, rlDest.highReg,
                           rlSrc.lowReg, rlSrc.highReg);
        } else {
            // Just re-assign the registers.  Dest gets Src's regs
            rlDest.lowReg = rlSrc.lowReg;
            rlDest.highReg = rlSrc.highReg;
            clobberReg(cUnit, rlSrc.lowReg);
            clobberReg(cUnit, rlSrc.highReg);
        }
    } else {
        // Load Src either into promoted Dest or temps allocated for Dest
        rlDest = evalLoc(cUnit, rlDest, kAnyReg, false);
        loadValueDirectWide(cUnit, rlSrc, rlDest.lowReg,
                            rlDest.highReg);
    }

    // Dest is now live and dirty (until/if we flush it to home location)
    markRegLive(cUnit, rlDest.lowReg, rlDest.sRegLow);
    markRegLive(cUnit, rlDest.highReg, hiSReg(rlDest.sRegLow));
    markRegDirty(cUnit, rlDest.lowReg);
    markRegDirty(cUnit, rlDest.highReg);
    markRegPair(cUnit, rlDest.lowReg, rlDest.highReg);


    if (rlDest.location == kLocRetval) {
        storeBaseDispWide(cUnit, rGLUE, offsetof(InterpState, retval),
                          rlDest.lowReg, rlDest.highReg);
        clobberReg(cUnit, rlDest.lowReg);
        clobberReg(cUnit, rlDest.highReg);
    } else {
        resetDefLocWide(cUnit, rlDest);
        if (liveOut(cUnit, rlDest.sRegLow) ||
            liveOut(cUnit, hiSReg(rlDest.sRegLow))) {
            defStart = (LIR *)cUnit->lastLIRInsn;
            int vReg = sReg2vReg(cUnit, rlDest.sRegLow);
            assert((vReg+1) == sReg2vReg(cUnit, hiSReg(rlDest.sRegLow)));
            storeBaseDispWide(cUnit, rFP, vReg << 2, rlDest.lowReg,
                              rlDest.highReg);
            markRegClean(cUnit, rlDest.lowReg);
            markRegClean(cUnit, rlDest.highReg);
            defEnd = (LIR *)cUnit->lastLIRInsn;
            markDefWide(cUnit, rlDest, defStart, defEnd);
        }
    }
}
/*
 * Perform null-check on a register. sReg is the ssa register being checked,
 * and mReg is the machine register holding the actual value. If internal state
 * indicates that sReg has been checked before the check request is ignored.
 */
static ArmLIR *genNullCheck(CompilationUnit *cUnit, int sReg, int mReg,
                                int dOffset, ArmLIR *pcrLabel)
{
    /* This particular Dalvik register has been null-checked */
    if (dvmIsBitSet(cUnit->regPool->nullCheckedRegs, sReg)) {
        return pcrLabel;
    }
    dvmSetBit(cUnit->regPool->nullCheckedRegs, sReg);
    return genRegImmCheck(cUnit, kArmCondEq, mReg, 0, dOffset, pcrLabel);
}



/*
 * Perform a "reg cmp reg" operation and jump to the PCR region if condition
 * satisfies.
 */
static ArmLIR *genRegRegCheck(CompilationUnit *cUnit,
                              ArmConditionCode cond,
                              int reg1, int reg2, int dOffset,
                              ArmLIR *pcrLabel)
{
    ArmLIR *res;
    res = opRegReg(cUnit, kOpCmp, reg1, reg2);
    ArmLIR *branch = opCondBranch(cUnit, cond);
    genCheckCommon(cUnit, dOffset, branch, pcrLabel);
    return res;
}

/*
 * Perform zero-check on a register. Similar to genNullCheck but the value being
 * checked does not have a corresponding Dalvik register.
 */
static ArmLIR *genZeroCheck(CompilationUnit *cUnit, int mReg,
                                int dOffset, ArmLIR *pcrLabel)
{
    return genRegImmCheck(cUnit, kArmCondEq, mReg, 0, dOffset, pcrLabel);
}

/* Perform bound check on two registers */
static ArmLIR *genBoundsCheck(CompilationUnit *cUnit, int rIndex,
                                  int rBound, int dOffset, ArmLIR *pcrLabel)
{
    return genRegRegCheck(cUnit, kArmCondCs, rIndex, rBound, dOffset,
                            pcrLabel);
}

/*
 * Jump to the out-of-line handler in ARM mode to finish executing the
 * remaining of more complex instructions.
 */
static void genDispatchToHandler(CompilationUnit *cUnit, TemplateOpCode opCode)
{
    /*
     * NOTE - In practice BLX only needs one operand, but since the assembler
     * may abort itself and retry due to other out-of-range conditions we
     * cannot really use operand[0] to store the absolute target address since
     * it may get clobbered by the final relative offset. Therefore,
     * we fake BLX_1 is a two operand instruction and the absolute target
     * address is stored in operand[1].
     */
    clobberHandlerRegs(cUnit);
    newLIR2(cUnit, kThumbBlx1,
            (int) gDvmJit.codeCache + templateEntryOffsets[opCode],
            (int) gDvmJit.codeCache + templateEntryOffsets[opCode]);
    newLIR2(cUnit, kThumbBlx2,
            (int) gDvmJit.codeCache + templateEntryOffsets[opCode],
            (int) gDvmJit.codeCache + templateEntryOffsets[opCode]);
}
