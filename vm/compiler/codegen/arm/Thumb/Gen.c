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
 * This file contains codegen for the Thumb ISA and is intended to be
 * includes by:
 *
 *        Codegen-$(TARGET_ARCH_VARIANT).c
 *
 */

/*
 * Perform a "reg cmp imm" operation and jump to the PCR region if condition
 * satisfies.
 */
static void genNegFloat(CompilationUnit *cUnit, RegLocation rlDest,
                        RegLocation rlSrc)
{
    RegLocation rlResult;
    rlSrc = loadValue(cUnit, rlSrc, kCoreReg);
    rlResult = dvmCompilerEvalLoc(cUnit, rlDest, kCoreReg, true);
    opRegRegImm(cUnit, kOpAdd, rlResult.lowReg,
                rlSrc.lowReg, 0x80000000);
    storeValue(cUnit, rlDest, rlResult);
}

static void genNegDouble(CompilationUnit *cUnit, RegLocation rlDest,
                         RegLocation rlSrc)
{
    RegLocation rlResult;
    rlSrc = loadValueWide(cUnit, rlSrc, kCoreReg);
    rlResult = dvmCompilerEvalLoc(cUnit, rlDest, kCoreReg, true);
    opRegRegImm(cUnit, kOpAdd, rlResult.highReg, rlSrc.highReg,
                        0x80000000);
    genRegCopy(cUnit, rlResult.lowReg, rlSrc.lowReg);
    storeValueWide(cUnit, rlDest, rlResult);
}

static void genMulLong(CompilationUnit *cUnit, RegLocation rlDest,
                       RegLocation rlSrc1, RegLocation rlSrc2)
{
    RegLocation rlResult;
    loadValueDirectWideFixed(cUnit, rlSrc1, r0, r1);
    loadValueDirectWideFixed(cUnit, rlSrc2, r2, r3);
    genDispatchToHandler(cUnit, TEMPLATE_MUL_LONG);
    rlResult = dvmCompilerGetReturnWide(cUnit);
    storeValueWide(cUnit, rlDest, rlResult);
}

static bool partialOverlap(int sreg1, int sreg2)
{
    return abs(sreg1 - sreg2) == 1;
}

static void genLong3Addr(CompilationUnit *cUnit, MIR *mir, OpKind firstOp,
                         OpKind secondOp, RegLocation rlDest,
                         RegLocation rlSrc1, RegLocation rlSrc2)
{
    RegLocation rlResult;
    if (partialOverlap(rlSrc1.sRegLow,rlSrc2.sRegLow) ||
        partialOverlap(rlSrc1.sRegLow,rlDest.sRegLow) ||
        partialOverlap(rlSrc2.sRegLow,rlDest.sRegLow)) {
        // Rare case - not enough registers to properly handle
        genInterpSingleStep(cUnit, mir);
    } else if (rlDest.sRegLow == rlSrc1.sRegLow) {
        // Already 2-operand
        rlResult = loadValueWide(cUnit, rlDest, kCoreReg);
        rlSrc2 = loadValueWide(cUnit, rlSrc2, kCoreReg);
        opRegReg(cUnit, firstOp, rlResult.lowReg, rlSrc2.lowReg);
        opRegReg(cUnit, secondOp, rlResult.highReg, rlSrc2.highReg);
        storeValueWide(cUnit, rlDest, rlResult);
    } else if (rlDest.sRegLow == rlSrc2.sRegLow) {
        // Bad case - must use/clobber Src1 and reassign Dest
        rlSrc1 = loadValueWide(cUnit, rlSrc1, kCoreReg);
        rlResult = loadValueWide(cUnit, rlDest, kCoreReg);
        opRegReg(cUnit, firstOp, rlSrc1.lowReg, rlResult.lowReg);
        opRegReg(cUnit, secondOp, rlSrc1.highReg, rlResult.highReg);
        // Old reg assignments are now invalid
        dvmCompilerClobber(cUnit, rlResult.lowReg);
        dvmCompilerClobber(cUnit, rlResult.highReg);
        dvmCompilerClobber(cUnit, rlSrc1.lowReg);
        dvmCompilerClobber(cUnit, rlSrc1.highReg);
        rlDest.location = kLocDalvikFrame;
        assert(rlSrc1.location == kLocPhysReg);
        // Reassign registers - rlDest will now get rlSrc1's old regs
        storeValueWide(cUnit, rlDest, rlSrc1);
    } else {
        // Copy Src1 to Dest
        rlSrc2 = loadValueWide(cUnit, rlSrc2, kCoreReg);
        rlResult = dvmCompilerEvalLoc(cUnit, rlDest, kCoreReg, false);
        loadValueDirectWide(cUnit, rlSrc1, rlResult.lowReg,
                            rlResult.highReg);
        rlResult.location = kLocPhysReg;
        opRegReg(cUnit, firstOp, rlResult.lowReg, rlSrc2.lowReg);
        opRegReg(cUnit, secondOp, rlResult.highReg, rlSrc2.highReg);
        storeValueWide(cUnit, rlDest, rlResult);
    }
}

void dvmCompilerInitializeRegAlloc(CompilationUnit *cUnit)
{
    int numTemps = sizeof(coreTemps)/sizeof(int);
    RegisterPool *pool = dvmCompilerNew(sizeof(*pool), true);
    cUnit->regPool = pool;
    pool->numCoreTemps = numTemps;
    pool->coreTemps =
            dvmCompilerNew(numTemps * sizeof(*pool->coreTemps), true);
    pool->numFPTemps = 0;
    pool->FPTemps = NULL;
    pool->numCoreRegs = 0;
    pool->coreRegs = NULL;
    pool->numFPRegs = 0;
    pool->FPRegs = NULL;
    dvmCompilerInitPool(pool->coreTemps, coreTemps, pool->numCoreTemps);
    dvmCompilerInitPool(pool->FPTemps, NULL, 0);
    dvmCompilerInitPool(pool->coreRegs, NULL, 0);
    dvmCompilerInitPool(pool->FPRegs, NULL, 0);
    pool->nullCheckedRegs =
        dvmCompilerAllocBitVector(cUnit->numSSARegs, false);
}

/* Export the Dalvik PC assicated with an instruction to the StackSave area */
static ArmLIR *genExportPC(CompilationUnit *cUnit, MIR *mir)
{
    ArmLIR *res;
    int rDPC = dvmCompilerAllocTemp(cUnit);
    int rAddr = dvmCompilerAllocTemp(cUnit);
    int offset = offsetof(StackSaveArea, xtra.currentPc);
    res = loadConstant(cUnit, rDPC, (int) (cUnit->method->insns + mir->offset));
    newLIR2(cUnit, kThumbMovRR, rAddr, rFP);
    newLIR2(cUnit, kThumbSubRI8, rAddr, sizeof(StackSaveArea) - offset);
    storeWordDisp( cUnit, rAddr, 0, rDPC);
    return res;
}

static void genMonitor(CompilationUnit *cUnit, MIR *mir)
{
    genMonitorPortable(cUnit, mir);
}

static void genCmpLong(CompilationUnit *cUnit, MIR *mir, RegLocation rlDest,
                       RegLocation rlSrc1, RegLocation rlSrc2)
{
    RegLocation rlResult;
    loadValueDirectWideFixed(cUnit, rlSrc1, r0, r1);
    loadValueDirectWideFixed(cUnit, rlSrc2, r2, r3);
    genDispatchToHandler(cUnit, TEMPLATE_CMP_LONG);
    rlResult = dvmCompilerGetReturn(cUnit);
    storeValue(cUnit, rlDest, rlResult);
}

static bool genInlinedAbsFloat(CompilationUnit *cUnit, MIR *mir)
{
    int offset = offsetof(InterpState, retval);
    RegLocation rlSrc = dvmCompilerGetSrc(cUnit, mir, 0);
    int reg0 = loadValue(cUnit, rlSrc, kCoreReg).lowReg;
    int signMask = dvmCompilerAllocTemp(cUnit);
    loadConstant(cUnit, signMask, 0x7fffffff);
    newLIR2(cUnit, kThumbAndRR, reg0, signMask);
    dvmCompilerFreeTemp(cUnit, signMask);
    storeWordDisp(cUnit, rGLUE, offset, reg0);
    //TUNING: rewrite this to not clobber
    dvmCompilerClobber(cUnit, reg0);
    return true;
}

static bool genInlinedAbsDouble(CompilationUnit *cUnit, MIR *mir)
{
    int offset = offsetof(InterpState, retval);
    RegLocation rlSrc = dvmCompilerGetSrcWide(cUnit, mir, 0, 1);
    RegLocation regSrc = loadValueWide(cUnit, rlSrc, kCoreReg);
    int reglo = regSrc.lowReg;
    int reghi = regSrc.highReg;
    int signMask = dvmCompilerAllocTemp(cUnit);
    loadConstant(cUnit, signMask, 0x7fffffff);
    storeWordDisp(cUnit, rGLUE, offset, reglo);
    newLIR2(cUnit, kThumbAndRR, reghi, signMask);
    dvmCompilerFreeTemp(cUnit, signMask);
    storeWordDisp(cUnit, rGLUE, offset + 4, reghi);
    //TUNING: rewrite this to not clobber
    dvmCompilerClobber(cUnit, reghi);
    return true;
}

/* No select in thumb, so we need to branch.  Thumb2 will do better */
static bool genInlinedMinMaxInt(CompilationUnit *cUnit, MIR *mir, bool isMin)
{
    int offset = offsetof(InterpState, retval);
    RegLocation rlSrc1 = dvmCompilerGetSrc(cUnit, mir, 0);
    RegLocation rlSrc2 = dvmCompilerGetSrc(cUnit, mir, 1);
    int reg0 = loadValue(cUnit, rlSrc1, kCoreReg).lowReg;
    int reg1 = loadValue(cUnit, rlSrc2, kCoreReg).lowReg;
    newLIR2(cUnit, kThumbCmpRR, reg0, reg1);
    ArmLIR *branch1 = newLIR2(cUnit, kThumbBCond, 2,
           isMin ? kArmCondLt : kArmCondGt);
    newLIR2(cUnit, kThumbMovRR, reg0, reg1);
    ArmLIR *target = newLIR0(cUnit, kArmPseudoTargetLabel);
    target->defMask = ENCODE_ALL;
    newLIR3(cUnit, kThumbStrRRI5, reg0, rGLUE, offset >> 2);
    branch1->generic.target = (LIR *)target;
    //TUNING: rewrite this to not clobber
    dvmCompilerClobber(cUnit,reg0);
    return false;
}

static void genMultiplyByTwoBitMultiplier(CompilationUnit *cUnit,
        RegLocation rlSrc, RegLocation rlResult, int lit,
        int firstBit, int secondBit)
{
    // We can't implement "add src, src, src, lsl#shift" on Thumb, so we have
    // to do a regular multiply.
    opRegRegImm(cUnit, kOpMul, rlResult.lowReg, rlSrc.lowReg, lit);
}
