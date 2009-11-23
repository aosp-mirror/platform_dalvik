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
    rlResult = evalLoc(cUnit, rlDest, kCoreReg, true);
    opRegRegImm(cUnit, kOpAdd, rlResult.lowReg,
                rlSrc.lowReg, 0x80000000);
    storeValue(cUnit, rlDest, rlResult);
}

static void genNegDouble(CompilationUnit *cUnit, RegLocation rlDest,
                        RegLocation rlSrc)
{
    RegLocation rlResult;
    rlSrc = loadValueWide(cUnit, rlSrc, kCoreReg);
    rlResult = evalLoc(cUnit, rlDest, kCoreReg, true);
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
    rlResult = getReturnLocWide(cUnit);
    storeValueWide(cUnit, rlDest, rlResult);
}

static void genLong3Addr(CompilationUnit *cUnit, OpKind firstOp,
                         OpKind secondOp, RegLocation rlDest,
                         RegLocation rlSrc1, RegLocation rlSrc2)
{
    RegLocation rlResult;
   if (rlDest.sRegLow == rlSrc1.sRegLow) {
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
        clobberReg(cUnit, rlResult.lowReg);
        clobberReg(cUnit, rlResult.highReg);
        clobberReg(cUnit, rlSrc1.lowReg);
        clobberReg(cUnit, rlSrc1.highReg);
        rlDest.location = kLocDalvikFrame;
        assert(rlSrc1.location == kLocPhysReg);
        // Reassign registers - rlDest will now get rlSrc1's old regs
        storeValueWide(cUnit, rlDest, rlSrc1);
    } else {
        // Copy Src1 to Dest
        rlSrc2 = loadValueWide(cUnit, rlSrc2, kCoreReg);
        rlResult = evalLoc(cUnit, rlDest, kCoreReg, false);
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
    int i;
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
    initPool(pool->coreTemps, coreTemps, pool->numCoreTemps);
    initPool(pool->FPTemps, NULL, 0);
    initPool(pool->coreRegs, NULL, 0);
    initPool(pool->FPRegs, NULL, 0);
    pool->nullCheckedRegs =
        dvmCompilerAllocBitVector(cUnit->numSSARegs, false);
}

/* Export the Dalvik PC assicated with an instruction to the StackSave area */
static ArmLIR *genExportPC(CompilationUnit *cUnit, MIR *mir)
{
    ArmLIR *res;
    int rDPC = allocTemp(cUnit);
    int rAddr = allocTemp(cUnit);
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
    rlResult = getReturnLoc(cUnit);
    storeValue(cUnit, rlDest, rlResult);
}

static bool genInlinedStringLength(CompilationUnit *cUnit, MIR *mir)
{
    DecodedInstruction *dInsn = &mir->dalvikInsn;
    int offset = offsetof(InterpState, retval);
    RegLocation rlObj = getSrcLoc(cUnit, mir, 0);
    int regObj = loadValue(cUnit, rlObj, kCoreReg).lowReg;
    int reg1 = allocTemp(cUnit);
    genNullCheck(cUnit, getSrcSSAName(mir, 0), regObj, mir->offset, NULL);
    loadWordDisp(cUnit, regObj, gDvm.offJavaLangString_count, reg1);
    storeWordDisp(cUnit, rGLUE, offset, reg1);
    return false;
}

static bool genInlinedStringCharAt(CompilationUnit *cUnit, MIR *mir)
{
    DecodedInstruction *dInsn = &mir->dalvikInsn;
    int offset = offsetof(InterpState, retval);
    int contents = offsetof(ArrayObject, contents);
    RegLocation rlObj = getSrcLoc(cUnit, mir, 0);
    RegLocation rlIdx = getSrcLoc(cUnit, mir, 1);
    int regObj = loadValue(cUnit, rlObj, kCoreReg).lowReg;
    int regIdx = loadValue(cUnit, rlIdx, kCoreReg).lowReg;
    int regMax = allocTemp(cUnit);
    int regOff = allocTemp(cUnit);
    ArmLIR * pcrLabel = genNullCheck(cUnit, getSrcSSAName(mir, 0),
                                     regObj, mir->offset, NULL);
    loadWordDisp(cUnit, regObj, gDvm.offJavaLangString_count, regMax);
    loadWordDisp(cUnit, regObj, gDvm.offJavaLangString_offset, regOff);
    loadWordDisp(cUnit, regObj, gDvm.offJavaLangString_value, regObj);
    genBoundsCheck(cUnit, regIdx, regMax, mir->offset, pcrLabel);

    newLIR2(cUnit, kThumbAddRI8, regObj, contents);
    newLIR3(cUnit, kThumbAddRRR, regIdx, regIdx, regOff);
    newLIR3(cUnit, kThumbAddRRR, regIdx, regIdx, regIdx);
    newLIR3(cUnit, kThumbLdrhRRR, regMax, regObj, regIdx);
    freeTemp(cUnit, regOff);
    storeWordDisp(cUnit, rGLUE, offset, regMax);
//FIXME: rewrite this to not clobber
    clobberReg(cUnit, regObj);
    clobberReg(cUnit, regIdx);
    return false;
}

static bool genInlinedAbsInt(CompilationUnit *cUnit, MIR *mir)
{
    int offset = offsetof(InterpState, retval);
    RegLocation rlSrc = getSrcLoc(cUnit, mir, 0);
    int reg0 = loadValue(cUnit, rlSrc, kCoreReg).lowReg;
    int sign = allocTemp(cUnit);
    /* abs(x) = y<=x>>31, (x+y)^y.  Shorter in ARM/THUMB2, no skip in THUMB */
    newLIR3(cUnit, kThumbAsrRRI5, sign, reg0, 31);
    newLIR3(cUnit, kThumbAddRRR, reg0, reg0, sign);
    newLIR2(cUnit, kThumbEorRR, reg0, sign);
    freeTemp(cUnit, sign);
    storeWordDisp(cUnit, rGLUE, offset, reg0);
//FIXME: rewrite this to not clobber
    clobberReg(cUnit, reg0);
    return false;
}

static bool genInlinedAbsFloat(CompilationUnit *cUnit, MIR *mir)
{
    int offset = offsetof(InterpState, retval);
    RegLocation rlSrc = getSrcLoc(cUnit, mir, 0);
    int reg0 = loadValue(cUnit, rlSrc, kCoreReg).lowReg;
    int signMask = allocTemp(cUnit);
    loadConstant(cUnit, signMask, 0x7fffffff);
    newLIR2(cUnit, kThumbAndRR, reg0, signMask);
    freeTemp(cUnit, signMask);
    storeWordDisp(cUnit, rGLUE, offset, reg0);
//FIXME: rewrite this to not clobber
    clobberReg(cUnit, reg0);
    return true;
}

static bool genInlinedAbsDouble(CompilationUnit *cUnit, MIR *mir)
{
    int offset = offsetof(InterpState, retval);
    RegLocation rlSrc = getSrcLocWide(cUnit, mir, 0, 1);
    RegLocation regSrc = loadValueWide(cUnit, rlSrc, kCoreReg);
    int reglo = regSrc.lowReg;
    int reghi = regSrc.highReg;
    int signMask = allocTemp(cUnit);
    loadConstant(cUnit, signMask, 0x7fffffff);
    storeWordDisp(cUnit, rGLUE, offset, reglo);
    newLIR2(cUnit, kThumbAndRR, reghi, signMask);
    freeTemp(cUnit, signMask);
    storeWordDisp(cUnit, rGLUE, offset + 4, reghi);
//FIXME: rewrite this to not clobber
    clobberReg(cUnit, reghi);
    return true;
}

/* No select in thumb, so we need to branch.  Thumb2 will do better */
static bool genInlinedMinMaxInt(CompilationUnit *cUnit, MIR *mir, bool isMin)
{
    int offset = offsetof(InterpState, retval);
    RegLocation rlSrc1 = getSrcLoc(cUnit, mir, 0);
    RegLocation rlSrc2 = getSrcLoc(cUnit, mir, 1);
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
//FIXME: rewrite this to not clobber
    clobberReg(cUnit,reg0);
    return false;
}

static bool genInlinedAbsLong(CompilationUnit *cUnit, MIR *mir)
{
    int offset = offsetof(InterpState, retval);
    RegLocation rlSrc = getSrcLocWide(cUnit, mir, 0, 1);
    RegLocation regSrc = loadValueWide(cUnit, rlSrc, kCoreReg);
    int oplo = regSrc.lowReg;
    int ophi = regSrc.highReg;
    int sign = allocTemp(cUnit);
    /* abs(x) = y<=x>>31, (x+y)^y.  Shorter in ARM/THUMB2, no skip in THUMB */
    newLIR3(cUnit, kThumbAsrRRI5, sign, ophi, 31);
    newLIR3(cUnit, kThumbAddRRR, oplo, oplo, sign);
    newLIR2(cUnit, kThumbAdcRR, ophi, sign);
    newLIR2(cUnit, kThumbEorRR, oplo, sign);
    newLIR2(cUnit, kThumbEorRR, ophi, sign);
    freeTemp(cUnit, sign);
    storeWordDisp(cUnit, rGLUE, offset, oplo);
    storeWordDisp(cUnit, rGLUE, offset + 4, ophi);
//FIXME: rewrite this to not clobber
    clobberReg(cUnit, oplo);
    clobberReg(cUnit, ophi);
    return false;
}
