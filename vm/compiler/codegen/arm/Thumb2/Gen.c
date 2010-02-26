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

static void genNegFloat(CompilationUnit *cUnit, RegLocation rlDest,
                        RegLocation rlSrc)
{
    RegLocation rlResult;
    rlSrc = loadValue(cUnit, rlSrc, kFPReg);
    rlResult = dvmCompilerEvalLoc(cUnit, rlDest, kFPReg, true);
    newLIR2(cUnit, kThumb2Vnegs, rlResult.lowReg, rlSrc.lowReg);
    storeValue(cUnit, rlDest, rlResult);
}

static void genNegDouble(CompilationUnit *cUnit, RegLocation rlDest,
                         RegLocation rlSrc)
{
    RegLocation rlResult;
    rlSrc = loadValueWide(cUnit, rlSrc, kFPReg);
    rlResult = dvmCompilerEvalLoc(cUnit, rlDest, kFPReg, true);
    newLIR2(cUnit, kThumb2Vnegd, S2D(rlResult.lowReg, rlResult.highReg),
            S2D(rlSrc.lowReg, rlSrc.highReg));
    storeValueWide(cUnit, rlDest, rlResult);
}

/*
 * To avoid possible conflicts, we use a lot of temps here.  Note that
 * our usage of Thumb2 instruction forms avoids the problems with register
 * reuse for multiply instructions prior to arm6.
 */
static void genMulLong(CompilationUnit *cUnit, RegLocation rlDest,
                       RegLocation rlSrc1, RegLocation rlSrc2)
{
    RegLocation rlResult;
    int resLo = dvmCompilerAllocTemp(cUnit);
    int resHi = dvmCompilerAllocTemp(cUnit);
    int tmp1 = dvmCompilerAllocTemp(cUnit);

    rlSrc1 = loadValueWide(cUnit, rlSrc1, kCoreReg);
    rlSrc2 = loadValueWide(cUnit, rlSrc2, kCoreReg);

    newLIR3(cUnit, kThumb2MulRRR, tmp1, rlSrc2.lowReg, rlSrc1.highReg);
    newLIR4(cUnit, kThumb2Umull, resLo, resHi, rlSrc2.lowReg, rlSrc1.lowReg);
    newLIR4(cUnit, kThumb2Mla, tmp1, rlSrc1.lowReg, rlSrc2.highReg, tmp1);
    newLIR4(cUnit, kThumb2AddRRR, resHi, tmp1, resHi, 0);
    dvmCompilerFreeTemp(cUnit, tmp1);

    rlResult = dvmCompilerGetReturnWide(cUnit);  // Just as a template, will patch
    rlResult.lowReg = resLo;
    rlResult.highReg = resHi;
    storeValueWide(cUnit, rlDest, rlResult);
}

static void genLong3Addr(CompilationUnit *cUnit, OpKind firstOp,
                         OpKind secondOp, RegLocation rlDest,
                         RegLocation rlSrc1, RegLocation rlSrc2)
{
    RegLocation rlResult;
    rlSrc1 = loadValueWide(cUnit, rlSrc1, kCoreReg);
    rlSrc2 = loadValueWide(cUnit, rlSrc2, kCoreReg);
    rlResult = dvmCompilerEvalLoc(cUnit, rlDest, kCoreReg, true);
    opRegRegReg(cUnit, firstOp, rlResult.lowReg, rlSrc1.lowReg, rlSrc2.lowReg);
    opRegRegReg(cUnit, secondOp, rlResult.highReg, rlSrc1.highReg,
                rlSrc2.highReg);
    storeValueWide(cUnit, rlDest, rlResult);
}

void dvmCompilerInitializeRegAlloc(CompilationUnit *cUnit)
{
    int i;
    int numTemps = sizeof(coreTemps)/sizeof(int);
    int numFPTemps = sizeof(fpTemps)/sizeof(int);
    RegisterPool *pool = dvmCompilerNew(sizeof(*pool), true);
    cUnit->regPool = pool;
    pool->numCoreTemps = numTemps;
    pool->coreTemps =
            dvmCompilerNew(numTemps * sizeof(*cUnit->regPool->coreTemps), true);
    pool->numFPTemps = numFPTemps;
    pool->FPTemps =
            dvmCompilerNew(numFPTemps * sizeof(*cUnit->regPool->FPTemps), true);
    pool->numCoreRegs = 0;
    pool->coreRegs = NULL;
    pool->numFPRegs = 0;
    pool->FPRegs = NULL;
    dvmCompilerInitPool(pool->coreTemps, coreTemps, pool->numCoreTemps);
    dvmCompilerInitPool(pool->FPTemps, fpTemps, pool->numFPTemps);
    dvmCompilerInitPool(pool->coreRegs, NULL, 0);
    dvmCompilerInitPool(pool->FPRegs, NULL, 0);
    pool->nullCheckedRegs =
        dvmCompilerAllocBitVector(cUnit->numSSARegs, false);
}

/*
 * Generate a Thumb2 IT instruction, which can nullify up to
 * four subsequent instructions based on a condition and its
 * inverse.  The condition applies to the first instruction, which
 * is executed if the condition is met.  The string "guide" consists
 * of 0 to 3 chars, and applies to the 2nd through 4th instruction.
 * A "T" means the instruction is executed if the condition is
 * met, and an "E" means the instruction is executed if the condition
 * is not met.
 */
static ArmLIR *genIT(CompilationUnit *cUnit, ArmConditionCode code,
                     char *guide)
{
    int mask;
    int condBit = code & 1;
    int altBit = condBit ^ 1;
    int mask3 = 0;
    int mask2 = 0;
    int mask1 = 0;

    //Note: case fallthroughs intentional
    switch(strlen(guide)) {
        case 3:
            mask1 = (guide[2] == 'T') ? condBit : altBit;
        case 2:
            mask2 = (guide[1] == 'T') ? condBit : altBit;
        case 1:
            mask3 = (guide[0] == 'T') ? condBit : altBit;
            break;
        case 0:
            break;
        default:
            assert(0);
            dvmAbort();
    }
    mask = (mask3 << 3) | (mask2 << 2) | (mask1 << 1) |
           (1 << (3 - strlen(guide)));
    return newLIR2(cUnit, kThumb2It, code, mask);
}

/* Export the Dalvik PC assicated with an instruction to the StackSave area */
static ArmLIR *genExportPC(CompilationUnit *cUnit, MIR *mir)
{
    ArmLIR *res;
    int offset = offsetof(StackSaveArea, xtra.currentPc);
    int rDPC = dvmCompilerAllocTemp(cUnit);
    res = loadConstant(cUnit, rDPC, (int) (cUnit->method->insns + mir->offset));
    newLIR3(cUnit, kThumb2StrRRI8Predec, rDPC, rFP,
            sizeof(StackSaveArea) - offset);
    dvmCompilerFreeTemp(cUnit, rDPC);
    return res;
}

/*
 * Handle simple case (thin lock) inline.  If it's complicated, bail
 * out to the heavyweight lock/unlock routines.  We'll use dedicated
 * registers here in order to be in the right position in case we
 * to bail to dvm[Lock/Unlock]Object(self, object)
 *
 * r0 -> self pointer [arg0 for dvm[Lock/Unlock]Object
 * r1 -> object [arg1 for dvm[Lock/Unlock]Object
 * r2 -> intial contents of object->lock, later result of strex
 * r3 -> self->threadId
 * r7 -> temp to hold new lock value [unlock only]
 * r4 -> allow to be used by utilities as general temp
 *
 * The result of the strex is 0 if we acquire the lock.
 *
 * See comments in Sync.c for the layout of the lock word.
 * Of particular interest to this code is the test for the
 * simple case - which we handle inline.  For monitor enter, the
 * simple case is thin lock, held by no-one.  For monitor exit,
 * the simple case is thin lock, held by the unlocking thread with
 * a recurse count of 0.
 *
 * A minor complication is that there is a field in the lock word
 * unrelated to locking: the hash state.  This field must be ignored, but
 * preserved.
 *
 */
static void genMonitor(CompilationUnit *cUnit, MIR *mir)
{
    RegLocation rlSrc = dvmCompilerGetSrc(cUnit, mir, 0);
    bool enter = (mir->dalvikInsn.opCode == OP_MONITOR_ENTER);
    ArmLIR *target;
    ArmLIR *branch;

    assert(LW_SHAPE_THIN == 0);
    loadValueDirectFixed(cUnit, rlSrc, r1);  // Get obj
    dvmCompilerLockAllTemps(cUnit);  // Prepare for explicit register usage
    dvmCompilerFreeTemp(cUnit, r4PC);  // Free up r4 for general use
    loadWordDisp(cUnit, rGLUE, offsetof(InterpState, self), r0); // Get self
    genNullCheck(cUnit, rlSrc.sRegLow, r1, mir->offset, NULL);
    loadWordDisp(cUnit, r0, offsetof(Thread, threadId), r3); // Get threadId
    newLIR3(cUnit, kThumb2Ldrex, r2, r1,
            offsetof(Object, lock) >> 2); // Get object->lock
    opRegImm(cUnit, kOpLsl, r3, LW_LOCK_OWNER_SHIFT); // Align owner
    // Is lock unheld on lock or held by us (==threadId) on unlock?
    if (enter) {
        newLIR4(cUnit, kThumb2Bfi, r3, r2, 0, LW_LOCK_OWNER_SHIFT - 1);
        newLIR3(cUnit, kThumb2Bfc, r2, LW_HASH_STATE_SHIFT,
                 LW_LOCK_OWNER_SHIFT - 1);
        opRegImm(cUnit, kOpCmp, r2, 0);
    } else {
        opRegRegImm(cUnit, kOpAnd, r7, r2,
                (LW_HASH_STATE_MASK << LW_HASH_STATE_SHIFT));
        newLIR3(cUnit, kThumb2Bfc, r2, LW_HASH_STATE_SHIFT,
                 LW_LOCK_OWNER_SHIFT - 1);
        opRegReg(cUnit, kOpSub, r2, r3);
    }
    // Note: start of IT block.  If last sub result != clear, else strex
    genIT(cUnit, kArmCondNe, "E");
    newLIR0(cUnit, kThumb2Clrex);
    if (enter) {
        newLIR4(cUnit, kThumb2Strex, r2, r3, r1,
                offsetof(Object, lock) >> 2);
    } else {
        newLIR4(cUnit, kThumb2Strex, r2, r7, r1,
                offsetof(Object, lock) >> 2);
    }
    // Note: end of IT block
    branch = newLIR2(cUnit, kThumb2Cbz, r2, 0);

    // Export PC (part 1)
    loadConstant(cUnit, r3, (int) (cUnit->method->insns + mir->offset));

    if (enter) {
        /* Get dPC of next insn */
        loadConstant(cUnit, r4PC, (int)(cUnit->method->insns + mir->offset +
                 dexGetInstrWidthAbs(gDvm.instrWidth, OP_MONITOR_ENTER)));
        // Export PC (part 2)
        newLIR3(cUnit, kThumb2StrRRI8Predec, r3, rFP,
                sizeof(StackSaveArea) -
                offsetof(StackSaveArea, xtra.currentPc));
        /* Call template, and don't return */
        genDispatchToHandler(cUnit, TEMPLATE_MONITOR_ENTER);
    } else {
        loadConstant(cUnit, r7, (int)dvmUnlockObject);
        // Export PC (part 2)
        newLIR3(cUnit, kThumb2StrRRI8Predec, r3, rFP,
                sizeof(StackSaveArea) -
                offsetof(StackSaveArea, xtra.currentPc));
        opReg(cUnit, kOpBlx, r7);
        opRegImm(cUnit, kOpCmp, r0, 0); /* Did we throw? */
        ArmLIR *branchOver = opCondBranch(cUnit, kArmCondNe);
        loadConstant(cUnit, r0,
                     (int) (cUnit->method->insns + mir->offset +
                     dexGetInstrWidthAbs(gDvm.instrWidth, OP_MONITOR_EXIT)));
        genDispatchToHandler(cUnit, TEMPLATE_THROW_EXCEPTION_COMMON);
        ArmLIR *target = newLIR0(cUnit, kArmPseudoTargetLabel);
        target->defMask = ENCODE_ALL;
        branchOver->generic.target = (LIR *) target;
        dvmCompilerClobberCallRegs(cUnit);
    }

    // Resume here
    target = newLIR0(cUnit, kArmPseudoTargetLabel);
    target->defMask = ENCODE_ALL;
    branch->generic.target = (LIR *)target;
}

/*
 * 64-bit 3way compare function.
 *     mov   r7, #-1
 *     cmp   op1hi, op2hi
 *     blt   done
 *     bgt   flip
 *     sub   r7, op1lo, op2lo (treat as unsigned)
 *     beq   done
 *     ite   hi
 *     mov(hi)   r7, #-1
 *     mov(!hi)  r7, #1
 * flip:
 *     neg   r7
 * done:
 */
static void genCmpLong(CompilationUnit *cUnit, MIR *mir,
                       RegLocation rlDest, RegLocation rlSrc1,
                       RegLocation rlSrc2)
{
    RegLocation rlTemp = LOC_C_RETURN; // Just using as template, will change
    ArmLIR *target1;
    ArmLIR *target2;
    rlSrc1 = loadValueWide(cUnit, rlSrc1, kCoreReg);
    rlSrc2 = loadValueWide(cUnit, rlSrc2, kCoreReg);
    rlTemp.lowReg = dvmCompilerAllocTemp(cUnit);
    loadConstant(cUnit, rlTemp.lowReg, -1);
    opRegReg(cUnit, kOpCmp, rlSrc1.highReg, rlSrc2.highReg);
    ArmLIR *branch1 = opCondBranch(cUnit, kArmCondLt);
    ArmLIR *branch2 = opCondBranch(cUnit, kArmCondGt);
    opRegRegReg(cUnit, kOpSub, rlTemp.lowReg, rlSrc1.lowReg, rlSrc2.lowReg);
    ArmLIR *branch3 = opCondBranch(cUnit, kArmCondEq);

    genIT(cUnit, kArmCondHi, "E");
    newLIR2(cUnit, kThumb2MovImmShift, rlTemp.lowReg, modifiedImmediate(-1));
    loadConstant(cUnit, rlTemp.lowReg, 1);
    genBarrier(cUnit);

    target2 = newLIR0(cUnit, kArmPseudoTargetLabel);
    target2->defMask = -1;
    opRegReg(cUnit, kOpNeg, rlTemp.lowReg, rlTemp.lowReg);

    target1 = newLIR0(cUnit, kArmPseudoTargetLabel);
    target1->defMask = -1;

    storeValue(cUnit, rlDest, rlTemp);

    branch1->generic.target = (LIR *)target1;
    branch2->generic.target = (LIR *)target2;
    branch3->generic.target = branch1->generic.target;
}

static bool genInlinedStringLength(CompilationUnit *cUnit, MIR *mir)
{
    RegLocation rlObj = dvmCompilerGetSrc(cUnit, mir, 0);
    RegLocation rlDest = inlinedTarget(cUnit, mir, false);
    rlObj = loadValue(cUnit, rlObj, kCoreReg);
    RegLocation rlResult = dvmCompilerEvalLoc(cUnit, rlDest, kCoreReg, true);
    genNullCheck(cUnit, rlObj.sRegLow, rlObj.lowReg, mir->offset, NULL);
    loadWordDisp(cUnit, rlObj.lowReg, gDvm.offJavaLangString_count,
                 rlResult.lowReg);
    storeValue(cUnit, rlDest, rlResult);
    return false;
}

static bool genInlinedStringCharAt(CompilationUnit *cUnit, MIR *mir)
{
    int contents = offsetof(ArrayObject, contents);
    RegLocation rlObj = dvmCompilerGetSrc(cUnit, mir, 0);
    RegLocation rlIdx = dvmCompilerGetSrc(cUnit, mir, 1);
    RegLocation rlDest = inlinedTarget(cUnit, mir, false);
    RegLocation rlResult;
    rlObj = loadValue(cUnit, rlObj, kCoreReg);
    rlIdx = loadValue(cUnit, rlIdx, kCoreReg);
    int regMax = dvmCompilerAllocTemp(cUnit);
    int regOff = dvmCompilerAllocTemp(cUnit);
    int regPtr = dvmCompilerAllocTemp(cUnit);
    ArmLIR *pcrLabel = genNullCheck(cUnit, rlObj.sRegLow, rlObj.lowReg,
                                    mir->offset, NULL);
    loadWordDisp(cUnit, rlObj.lowReg, gDvm.offJavaLangString_count, regMax);
    loadWordDisp(cUnit, rlObj.lowReg, gDvm.offJavaLangString_offset, regOff);
    loadWordDisp(cUnit, rlObj.lowReg, gDvm.offJavaLangString_value, regPtr);
    genBoundsCheck(cUnit, rlIdx.lowReg, regMax, mir->offset, pcrLabel);
    dvmCompilerFreeTemp(cUnit, regMax);
    opRegImm(cUnit, kOpAdd, regPtr, contents);
    opRegReg(cUnit, kOpAdd, regOff, rlIdx.lowReg);
    rlResult = dvmCompilerEvalLoc(cUnit, rlDest, kCoreReg, true);
    loadBaseIndexed(cUnit, regPtr, regOff, rlResult.lowReg, 1, kUnsignedHalf);
    storeValue(cUnit, rlDest, rlResult);
    return false;
}

static bool genInlinedAbsInt(CompilationUnit *cUnit, MIR *mir)
{
    RegLocation rlSrc = dvmCompilerGetSrc(cUnit, mir, 0);
    rlSrc = loadValue(cUnit, rlSrc, kCoreReg);
    RegLocation rlDest = inlinedTarget(cUnit, mir, false);;
    RegLocation rlResult = dvmCompilerEvalLoc(cUnit, rlDest, kCoreReg, true);
    int signReg = dvmCompilerAllocTemp(cUnit);
    /*
     * abs(x) = y<=x>>31, (x+y)^y.
     * Thumb2's IT block also yields 3 instructions, but imposes
     * scheduling constraints.
     */
    opRegRegImm(cUnit, kOpAsr, signReg, rlSrc.lowReg, 31);
    opRegRegReg(cUnit, kOpAdd, rlResult.lowReg, rlSrc.lowReg, signReg);
    opRegReg(cUnit, kOpXor, rlResult.lowReg, signReg);
    storeValue(cUnit, rlDest, rlResult);
    return false;
}

static bool genInlinedAbsFloat(CompilationUnit *cUnit, MIR *mir)
{
    RegLocation rlSrc = dvmCompilerGetSrc(cUnit, mir, 0);
    RegLocation rlDest = inlinedTarget(cUnit, mir, true);
    rlSrc = loadValue(cUnit, rlSrc, kFPReg);
    RegLocation rlResult = dvmCompilerEvalLoc(cUnit, rlDest, kFPReg, true);
    newLIR2(cUnit, kThumb2Vabss, rlResult.lowReg, rlSrc.lowReg);
    storeValue(cUnit, rlDest, rlResult);
    return true;
}

static bool genInlinedAbsDouble(CompilationUnit *cUnit, MIR *mir)
{
    RegLocation rlSrc = dvmCompilerGetSrcWide(cUnit, mir, 0, 1);
    RegLocation rlDest = inlinedTargetWide(cUnit, mir, true);
    rlSrc = loadValueWide(cUnit, rlSrc, kFPReg);
    RegLocation rlResult = dvmCompilerEvalLoc(cUnit, rlDest, kFPReg, true);
    newLIR2(cUnit, kThumb2Vabsd, S2D(rlResult.lowReg, rlResult.highReg),
            S2D(rlSrc.lowReg, rlSrc.highReg));
    storeValueWide(cUnit, rlDest, rlResult);
    return true;
}

static bool genInlinedMinMaxInt(CompilationUnit *cUnit, MIR *mir, bool isMin)
{
    RegLocation rlSrc1 = dvmCompilerGetSrc(cUnit, mir, 0);
    RegLocation rlSrc2 = dvmCompilerGetSrc(cUnit, mir, 1);
    rlSrc1 = loadValue(cUnit, rlSrc1, kCoreReg);
    rlSrc2 = loadValue(cUnit, rlSrc2, kCoreReg);
    RegLocation rlDest = inlinedTarget(cUnit, mir, false);
    RegLocation rlResult = dvmCompilerEvalLoc(cUnit, rlDest, kCoreReg, true);
    opRegReg(cUnit, kOpCmp, rlSrc1.lowReg, rlSrc2.lowReg);
    genIT(cUnit, (isMin) ? kArmCondGt : kArmCondLt, "E");
    opRegReg(cUnit, kOpMov, rlResult.lowReg, rlSrc2.lowReg);
    opRegReg(cUnit, kOpMov, rlResult.lowReg, rlSrc1.lowReg);
    genBarrier(cUnit);
    storeValue(cUnit, rlDest, rlResult);
    return false;
}

static bool genInlinedAbsLong(CompilationUnit *cUnit, MIR *mir)
{
    RegLocation rlSrc = dvmCompilerGetSrcWide(cUnit, mir, 0, 1);
    RegLocation rlDest = inlinedTargetWide(cUnit, mir, false);
    rlSrc = loadValueWide(cUnit, rlSrc, kCoreReg);
    RegLocation rlResult = dvmCompilerEvalLoc(cUnit, rlDest, kCoreReg, true);
    int signReg = dvmCompilerAllocTemp(cUnit);
    /*
     * abs(x) = y<=x>>31, (x+y)^y.
     * Thumb2 IT block allows slightly shorter sequence,
     * but introduces a scheduling barrier.  Stick with this
     * mechanism for now.
     */
    opRegRegImm(cUnit, kOpAsr, signReg, rlSrc.highReg, 31);
    opRegRegReg(cUnit, kOpAdd, rlResult.lowReg, rlSrc.lowReg, signReg);
    opRegRegReg(cUnit, kOpAdc, rlResult.highReg, rlSrc.highReg, signReg);
    opRegReg(cUnit, kOpXor, rlResult.lowReg, signReg);
    opRegReg(cUnit, kOpXor, rlResult.highReg, signReg);
    storeValueWide(cUnit, rlDest, rlResult);
    return false;
}

static void genMultiplyByTwoBitMultiplier(CompilationUnit *cUnit,
        RegLocation rlSrc, RegLocation rlResult, int lit,
        int firstBit, int secondBit)
{
    opRegRegRegShift(cUnit, kOpAdd, rlResult.lowReg, rlSrc.lowReg, rlSrc.lowReg,
                     encodeShift(kArmLsl, secondBit - firstBit));
    if (firstBit != 0) {
        opRegRegImm(cUnit, kOpLsl, rlResult.lowReg, rlResult.lowReg, firstBit);
    }
}
