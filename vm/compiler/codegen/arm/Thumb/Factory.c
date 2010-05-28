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

static int coreTemps[] = {r0, r1, r2, r3, r4PC, r7};

static void storePair(CompilationUnit *cUnit, int base, int lowReg,
                      int highReg);
static void loadPair(CompilationUnit *cUnit, int base, int lowReg, int highReg);
static ArmLIR *loadWordDisp(CompilationUnit *cUnit, int rBase, int displacement,
                            int rDest);
static ArmLIR *storeWordDisp(CompilationUnit *cUnit, int rBase,
                             int displacement, int rSrc);
static ArmLIR *genRegRegCheck(CompilationUnit *cUnit,
                              ArmConditionCode cond,
                              int reg1, int reg2, int dOffset,
                              ArmLIR *pcrLabel);


/*
 * Load a immediate using a shortcut if possible; otherwise
 * grab from the per-translation literal pool.  If target is
 * a high register, build constant into a low register and copy.
 *
 * No additional register clobbering operation performed. Use this version when
 * 1) rDest is freshly returned from dvmCompilerAllocTemp or
 * 2) The codegen is under fixed register usage
 */
static ArmLIR *loadConstantNoClobber(CompilationUnit *cUnit, int rDest,
                                     int value)
{
    ArmLIR *res;
    int tDest = LOWREG(rDest) ? rDest : dvmCompilerAllocTemp(cUnit);
    /* See if the value can be constructed cheaply */
    if ((value >= 0) && (value <= 255)) {
        res = newLIR2(cUnit, kThumbMovImm, tDest, value);
        if (rDest != tDest) {
           opRegReg(cUnit, kOpMov, rDest, tDest);
           dvmCompilerFreeTemp(cUnit, tDest);
        }
        return res;
    } else if ((value & 0xFFFFFF00) == 0xFFFFFF00) {
        res = newLIR2(cUnit, kThumbMovImm, tDest, ~value);
        newLIR2(cUnit, kThumbMvn, tDest, tDest);
        if (rDest != tDest) {
           opRegReg(cUnit, kOpMov, rDest, tDest);
           dvmCompilerFreeTemp(cUnit, tDest);
        }
        return res;
    }
    /* No shortcut - go ahead and use literal pool */
    ArmLIR *dataTarget = scanLiteralPool(cUnit, value, 255);
    if (dataTarget == NULL) {
        dataTarget = addWordData(cUnit, value, false);
    }
    ArmLIR *loadPcRel = dvmCompilerNew(sizeof(ArmLIR), true);
    loadPcRel->opCode = kThumbLdrPcRel;
    loadPcRel->generic.target = (LIR *) dataTarget;
    loadPcRel->operands[0] = tDest;
    setupResourceMasks(loadPcRel);
    /*
     * Special case for literal loads with a link register target.
     * Self-cosim mode will insert calls prior to heap references
     * after optimization, and those will destroy r14.  The easy
     * workaround is to treat literal loads into r14 as heap references
     * to prevent them from being hoisted.  Use of r14 in this manner
     * is currently rare.  Revist if that changes.
     */
    if (rDest != rlr)
        setMemRefType(loadPcRel, true, kLiteral);
    loadPcRel->aliasInfo = dataTarget->operands[0];
    res = loadPcRel;
    dvmCompilerAppendLIR(cUnit, (LIR *) loadPcRel);

    /*
     * To save space in the constant pool, we use the ADD_RRI8 instruction to
     * add up to 255 to an existing constant value.
     */
    if (dataTarget->operands[0] != value) {
        newLIR2(cUnit, kThumbAddRI8, tDest, value - dataTarget->operands[0]);
    }
    if (rDest != tDest) {
       opRegReg(cUnit, kOpMov, rDest, tDest);
       dvmCompilerFreeTemp(cUnit, tDest);
    }
    return res;
}

/*
 * Load an immediate value into a fixed or temp register.  Target
 * register is clobbered, and marked inUse.
 */
static ArmLIR *loadConstant(CompilationUnit *cUnit, int rDest, int value)
{
    if (dvmCompilerIsTemp(cUnit, rDest)) {
        dvmCompilerClobber(cUnit, rDest);
        dvmCompilerMarkInUse(cUnit, rDest);
    }
    return loadConstantNoClobber(cUnit, rDest, value);
}

static ArmLIR *opNone(CompilationUnit *cUnit, OpKind op)
{
    ArmOpCode opCode = kThumbBkpt;
    switch (op) {
        case kOpUncondBr:
            opCode = kThumbBUncond;
            break;
        default:
            LOGE("Jit: bad case in opNone");
            dvmCompilerAbort(cUnit);
    }
    return newLIR0(cUnit, opCode);
}

static ArmLIR *opCondBranch(CompilationUnit *cUnit, ArmConditionCode cc)
{
    return newLIR2(cUnit, kThumbBCond, 0 /* offset to be patched */, cc);
}

static ArmLIR *opImm(CompilationUnit *cUnit, OpKind op, int value)
{
    ArmOpCode opCode = kThumbBkpt;
    switch (op) {
        case kOpPush:
            opCode = kThumbPush;
            break;
        case kOpPop:
            opCode = kThumbPop;
            break;
        default:
            LOGE("Jit: bad case in opCondBranch");
            dvmCompilerAbort(cUnit);
    }
    return newLIR1(cUnit, opCode, value);
}

static ArmLIR *opReg(CompilationUnit *cUnit, OpKind op, int rDestSrc)
{
    ArmOpCode opCode = kThumbBkpt;
    switch (op) {
        case kOpBlx:
            opCode = kThumbBlxR;
            break;
        default:
            LOGE("Jit: bad case in opReg");
            dvmCompilerAbort(cUnit);
    }
    return newLIR1(cUnit, opCode, rDestSrc);
}

static ArmLIR *opRegImm(CompilationUnit *cUnit, OpKind op, int rDestSrc1,
                        int value)
{
    ArmLIR *res;
    bool neg = (value < 0);
    int absValue = (neg) ? -value : value;
    bool shortForm = (absValue & 0xff) == absValue;
    ArmOpCode opCode = kThumbBkpt;
    switch (op) {
        case kOpAdd:
            if ( !neg && (rDestSrc1 == 13) && (value <= 508)) { /* sp */
                assert((value & 0x3) == 0);
                return newLIR1(cUnit, kThumbAddSpI7, value >> 2);
            } else if (shortForm) {
                opCode = (neg) ? kThumbSubRI8 : kThumbAddRI8;
            } else
                opCode = kThumbAddRRR;
            break;
        case kOpSub:
            if (!neg && (rDestSrc1 == 13) && (value <= 508)) { /* sp */
                assert((value & 0x3) == 0);
                return newLIR1(cUnit, kThumbSubSpI7, value >> 2);
            } else if (shortForm) {
                opCode = (neg) ? kThumbAddRI8 : kThumbSubRI8;
            } else
                opCode = kThumbSubRRR;
            break;
        case kOpCmp:
            if (neg)
               shortForm = false;
            if (LOWREG(rDestSrc1) && shortForm) {
                opCode = kThumbCmpRI8;
            } else if (LOWREG(rDestSrc1)) {
                opCode = kThumbCmpRR;
            } else {
                shortForm = false;
                opCode = kThumbCmpHL;
            }
            break;
        default:
            LOGE("Jit: bad case in opRegImm");
            dvmCompilerAbort(cUnit);
            break;
    }
    if (shortForm)
        res = newLIR2(cUnit, opCode, rDestSrc1, absValue);
    else {
        int rScratch = dvmCompilerAllocTemp(cUnit);
        res = loadConstant(cUnit, rScratch, value);
        if (op == kOpCmp)
            newLIR2(cUnit, opCode, rDestSrc1, rScratch);
        else
            newLIR3(cUnit, opCode, rDestSrc1, rDestSrc1, rScratch);
    }
    return res;
}

static ArmLIR *opRegRegReg(CompilationUnit *cUnit, OpKind op, int rDest,
                           int rSrc1, int rSrc2)
{
    ArmOpCode opCode = kThumbBkpt;
    switch (op) {
        case kOpAdd:
            opCode = kThumbAddRRR;
            break;
        case kOpSub:
            opCode = kThumbSubRRR;
            break;
        default:
            if (rDest == rSrc1) {
                return opRegReg(cUnit, op, rDest, rSrc2);
            } else if (rDest == rSrc2) {
                assert(dvmCompilerIsTemp(cUnit, rSrc1));
                dvmCompilerClobber(cUnit, rSrc1);
                opRegReg(cUnit, op, rSrc1, rSrc2);
                return opRegReg(cUnit, kOpMov, rDest, rSrc1);
            } else {
                opRegReg(cUnit, kOpMov, rDest, rSrc1);
                return opRegReg(cUnit, op, rDest, rSrc2);
            }
            break;
    }
    return newLIR3(cUnit, opCode, rDest, rSrc1, rSrc2);
}

static ArmLIR *opRegRegImm(CompilationUnit *cUnit, OpKind op, int rDest,
                           int rSrc1, int value)
{
    ArmLIR *res;
    bool neg = (value < 0);
    int absValue = (neg) ? -value : value;
    ArmOpCode opCode = kThumbBkpt;
    bool shortForm = (absValue & 0x7) == absValue;
    switch(op) {
        case kOpAdd:
            if (rDest == rSrc1)
                return opRegImm(cUnit, op, rDest, value);
            if ((rSrc1 == 13) && (value <= 1020)) { /* sp */
                assert((value & 0x3) == 0);
                shortForm = true;
                opCode = kThumbAddSpRel;
                value >>= 2;
            } else if ((rSrc1 == 15) && (value <= 1020)) { /* pc */
                assert((value & 0x3) == 0);
                shortForm = true;
                opCode = kThumbAddPcRel;
                value >>= 2;
            } else if (shortForm) {
                opCode = (neg) ? kThumbSubRRI3 : kThumbAddRRI3;
            } else if ((absValue > 0) && (absValue <= (255 + 7))) {
                /* Two shots - 1st handle the 7 */
                opCode = (neg) ? kThumbSubRRI3 : kThumbAddRRI3;
                res = newLIR3(cUnit, opCode, rDest, rSrc1, 7);
                opCode = (neg) ? kThumbSubRI8 : kThumbAddRI8;
                newLIR2(cUnit, opCode, rDest, absValue - 7);
                return res;
            } else
                opCode = kThumbAddRRR;
            break;

        case kOpSub:
            if (rDest == rSrc1)
                return opRegImm(cUnit, op, rDest, value);
            if (shortForm) {
                opCode = (neg) ? kThumbAddRRI3 : kThumbSubRRI3;
            } else if ((absValue > 0) && (absValue <= (255 + 7))) {
                /* Two shots - 1st handle the 7 */
                opCode = (neg) ? kThumbAddRRI3 : kThumbSubRRI3;
                res = newLIR3(cUnit, opCode, rDest, rSrc1, 7);
                opCode = (neg) ? kThumbAddRI8 : kThumbSubRI8;
                newLIR2(cUnit, opCode, rDest, absValue - 7);
                return res;
            } else
                opCode = kThumbSubRRR;
            break;
        case kOpLsl:
                shortForm = (!neg && value <= 31);
                opCode = kThumbLslRRI5;
                break;
        case kOpLsr:
                shortForm = (!neg && value <= 31);
                opCode = kThumbLsrRRI5;
                break;
        case kOpAsr:
                shortForm = (!neg && value <= 31);
                opCode = kThumbAsrRRI5;
                break;
        case kOpMul:
        case kOpAnd:
        case kOpOr:
        case kOpXor:
                if (rDest == rSrc1) {
                    int rScratch = dvmCompilerAllocTemp(cUnit);
                    res = loadConstant(cUnit, rScratch, value);
                    opRegReg(cUnit, op, rDest, rScratch);
                } else {
                    res = loadConstant(cUnit, rDest, value);
                    opRegReg(cUnit, op, rDest, rSrc1);
                }
                return res;
        default:
            LOGE("Jit: bad case in opRegRegImm");
            dvmCompilerAbort(cUnit);
            break;
    }
    if (shortForm)
        res = newLIR3(cUnit, opCode, rDest, rSrc1, absValue);
    else {
        if (rDest != rSrc1) {
            res = loadConstant(cUnit, rDest, value);
            newLIR3(cUnit, opCode, rDest, rSrc1, rDest);
        } else {
            int rScratch = dvmCompilerAllocTemp(cUnit);
            res = loadConstant(cUnit, rScratch, value);
            newLIR3(cUnit, opCode, rDest, rSrc1, rScratch);
        }
    }
    return res;
}

static ArmLIR *opRegReg(CompilationUnit *cUnit, OpKind op, int rDestSrc1,
                        int rSrc2)
{
    ArmLIR *res;
    ArmOpCode opCode = kThumbBkpt;
    switch (op) {
        case kOpAdc:
            opCode = kThumbAdcRR;
            break;
        case kOpAnd:
            opCode = kThumbAndRR;
            break;
        case kOpBic:
            opCode = kThumbBicRR;
            break;
        case kOpCmn:
            opCode = kThumbCmnRR;
            break;
        case kOpCmp:
            opCode = kThumbCmpRR;
            break;
        case kOpXor:
            opCode = kThumbEorRR;
            break;
        case kOpMov:
            if (LOWREG(rDestSrc1) && LOWREG(rSrc2))
                opCode = kThumbMovRR;
            else if (!LOWREG(rDestSrc1) && !LOWREG(rSrc2))
                opCode = kThumbMovRR_H2H;
            else if (LOWREG(rDestSrc1))
                opCode = kThumbMovRR_H2L;
            else
                opCode = kThumbMovRR_L2H;
            break;
        case kOpMul:
            opCode = kThumbMul;
            break;
        case kOpMvn:
            opCode = kThumbMvn;
            break;
        case kOpNeg:
            opCode = kThumbNeg;
            break;
        case kOpOr:
            opCode = kThumbOrr;
            break;
        case kOpSbc:
            opCode = kThumbSbc;
            break;
        case kOpTst:
            opCode = kThumbTst;
            break;
        case kOpLsl:
            opCode = kThumbLslRR;
            break;
        case kOpLsr:
            opCode = kThumbLsrRR;
            break;
        case kOpAsr:
            opCode = kThumbAsrRR;
            break;
        case kOpRor:
            opCode = kThumbRorRR;
        case kOpAdd:
        case kOpSub:
            return opRegRegReg(cUnit, op, rDestSrc1, rDestSrc1, rSrc2);
        case kOp2Byte:
             res = opRegRegImm(cUnit, kOpLsl, rDestSrc1, rSrc2, 24);
             opRegRegImm(cUnit, kOpAsr, rDestSrc1, rDestSrc1, 24);
             return res;
        case kOp2Short:
             res = opRegRegImm(cUnit, kOpLsl, rDestSrc1, rSrc2, 16);
             opRegRegImm(cUnit, kOpAsr, rDestSrc1, rDestSrc1, 16);
             return res;
        case kOp2Char:
             res = opRegRegImm(cUnit, kOpLsl, rDestSrc1, rSrc2, 16);
             opRegRegImm(cUnit, kOpLsr, rDestSrc1, rDestSrc1, 16);
             return res;
        default:
            LOGE("Jit: bad case in opRegReg");
            dvmCompilerAbort(cUnit);
            break;
    }
    return newLIR2(cUnit, opCode, rDestSrc1, rSrc2);
}

static ArmLIR *loadConstantValueWide(CompilationUnit *cUnit, int rDestLo,
                                     int rDestHi, int valLo, int valHi)
{
    ArmLIR *res;
    res = loadConstantNoClobber(cUnit, rDestLo, valLo);
    loadConstantNoClobber(cUnit, rDestHi, valHi);
    return res;
}

/* Load value from base + scaled index. */
static ArmLIR *loadBaseIndexed(CompilationUnit *cUnit, int rBase,
                               int rIndex, int rDest, int scale, OpSize size)
{
    ArmLIR *first = NULL;
    ArmLIR *res;
    ArmOpCode opCode = kThumbBkpt;
    int rNewIndex = rIndex;
    if (scale) {
        // Scale the index, but can't trash the original.
        rNewIndex = dvmCompilerAllocTemp(cUnit);
        first = opRegRegImm(cUnit, kOpLsl, rNewIndex, rIndex, scale);
    }
    switch (size) {
        case kWord:
            opCode = kThumbLdrRRR;
            break;
        case kUnsignedHalf:
            opCode = kThumbLdrhRRR;
            break;
        case kSignedHalf:
            opCode = kThumbLdrshRRR;
            break;
        case kUnsignedByte:
            opCode = kThumbLdrbRRR;
            break;
        case kSignedByte:
            opCode = kThumbLdrsbRRR;
            break;
        default:
            LOGE("Jit: bad case in loadBaseIndexed");
            dvmCompilerAbort(cUnit);
    }
    res = newLIR3(cUnit, opCode, rDest, rBase, rNewIndex);
#if defined(WITH_SELF_VERIFICATION)
    if (cUnit->heapMemOp)
        res->branchInsertSV = true;
#endif
    if (scale)
        dvmCompilerFreeTemp(cUnit, rNewIndex);
    return (first) ? first : res;
}

/* store value base base + scaled index. */
static ArmLIR *storeBaseIndexed(CompilationUnit *cUnit, int rBase,
                                int rIndex, int rSrc, int scale, OpSize size)
{
    ArmLIR *first = NULL;
    ArmLIR *res;
    ArmOpCode opCode = kThumbBkpt;
    int rNewIndex = rIndex;
    if (scale) {
        rNewIndex = dvmCompilerAllocTemp(cUnit);
        first = opRegRegImm(cUnit, kOpLsl, rNewIndex, rIndex, scale);
    }
    switch (size) {
        case kWord:
            opCode = kThumbStrRRR;
            break;
        case kUnsignedHalf:
        case kSignedHalf:
            opCode = kThumbStrhRRR;
            break;
        case kUnsignedByte:
        case kSignedByte:
            opCode = kThumbStrbRRR;
            break;
        default:
            LOGE("Jit: bad case in storeBaseIndexed");
            dvmCompilerAbort(cUnit);
    }
    res = newLIR3(cUnit, opCode, rSrc, rBase, rNewIndex);
#if defined(WITH_SELF_VERIFICATION)
    if (cUnit->heapMemOp)
        res->branchInsertSV = true;
#endif
    if (scale)
        dvmCompilerFreeTemp(cUnit, rNewIndex);
    return (first) ? first : res;
}

static ArmLIR *loadMultiple(CompilationUnit *cUnit, int rBase, int rMask)
{
    ArmLIR *res;
    genBarrier(cUnit);
    res = newLIR2(cUnit, kThumbLdmia, rBase, rMask);
#if defined(WITH_SELF_VERIFICATION)
    if (cUnit->heapMemOp)
        res->branchInsertSV = true;
#endif
    genBarrier(cUnit);
    return res;
}

static ArmLIR *storeMultiple(CompilationUnit *cUnit, int rBase, int rMask)
{
    ArmLIR *res;
    genBarrier(cUnit);
    res = newLIR2(cUnit, kThumbStmia, rBase, rMask);
#if defined(WITH_SELF_VERIFICATION)
    if (cUnit->heapMemOp)
        res->branchInsertSV = true;
#endif
    genBarrier(cUnit);
    return res;
}

static ArmLIR *loadBaseDispBody(CompilationUnit *cUnit, MIR *mir, int rBase,
                                int displacement, int rDest, int rDestHi,
                                OpSize size, int sReg)
/*
 * Load value from base + displacement.  Optionally perform null check
 * on base (which must have an associated sReg and MIR).  If not
 * performing null check, incoming MIR can be null. IMPORTANT: this
 * code must not allocate any new temps.  If a new register is needed
 * and base and dest are the same, spill some other register to
 * rlp and then restore.
 */
{
    ArmLIR *res;
    ArmLIR *load = NULL;
    ArmLIR *load2 = NULL;
    ArmOpCode opCode = kThumbBkpt;
    bool shortForm = false;
    int encodedDisp = displacement;
    bool pair = false;

    switch (size) {
        case kLong:
        case kDouble:
            pair = true;
            if ((displacement < 124) && (displacement >= 0)) {
                assert((displacement & 0x3) == 0);
                shortForm = true;
                encodedDisp >>= 2;
                opCode = kThumbLdrRRI5;
            } else {
                opCode = kThumbLdrRRR;
            }
            break;
        case kWord:
            if (LOWREG(rDest) && (rBase == rpc) &&
                (displacement <= 1020) && (displacement >= 0)) {
                shortForm = true;
                encodedDisp >>= 2;
                opCode = kThumbLdrPcRel;
            } else if (LOWREG(rDest) && (rBase == r13) &&
                      (displacement <= 1020) && (displacement >= 0)) {
                shortForm = true;
                encodedDisp >>= 2;
                opCode = kThumbLdrSpRel;
            } else if (displacement < 128 && displacement >= 0) {
                assert((displacement & 0x3) == 0);
                shortForm = true;
                encodedDisp >>= 2;
                opCode = kThumbLdrRRI5;
            } else {
                opCode = kThumbLdrRRR;
            }
            break;
        case kUnsignedHalf:
            if (displacement < 64 && displacement >= 0) {
                assert((displacement & 0x1) == 0);
                shortForm = true;
                encodedDisp >>= 1;
                opCode = kThumbLdrhRRI5;
            } else {
                opCode = kThumbLdrhRRR;
            }
            break;
        case kSignedHalf:
            opCode = kThumbLdrshRRR;
            break;
        case kUnsignedByte:
            if (displacement < 32 && displacement >= 0) {
                shortForm = true;
                opCode = kThumbLdrbRRI5;
            } else {
                opCode = kThumbLdrbRRR;
            }
            break;
        case kSignedByte:
            opCode = kThumbLdrsbRRR;
            break;
        default:
            LOGE("Jit: bad case in loadBaseIndexedBody");
            dvmCompilerAbort(cUnit);
    }
    if (shortForm) {
        load = res = newLIR3(cUnit, opCode, rDest, rBase, encodedDisp);
        if (pair) {
            load2 = newLIR3(cUnit, opCode, rDestHi, rBase, encodedDisp+1);
        }
    } else {
        if (pair) {
            int rTmp = dvmCompilerAllocFreeTemp(cUnit);
            res = opRegRegImm(cUnit, kOpAdd, rTmp, rBase, displacement);
            load = newLIR3(cUnit, kThumbLdrRRI5, rDest, rTmp, 0);
            load2 = newLIR3(cUnit, kThumbLdrRRI5, rDestHi, rTmp, 1);
            dvmCompilerFreeTemp(cUnit, rTmp);
        } else {
            int rTmp = (rBase == rDest) ? dvmCompilerAllocFreeTemp(cUnit)
                                        : rDest;
            res = loadConstant(cUnit, rTmp, displacement);
            load = newLIR3(cUnit, opCode, rDest, rBase, rTmp);
            if (rBase == rFP)
                annotateDalvikRegAccess(load, displacement >> 2,
                                        true /* isLoad */);
            if (rTmp != rDest)
                dvmCompilerFreeTemp(cUnit, rTmp);
        }
    }
    if (rBase == rFP) {
        if (load != NULL)
            annotateDalvikRegAccess(load, displacement >> 2,
                                    true /* isLoad */);
        if (load2 != NULL)
            annotateDalvikRegAccess(load2, (displacement >> 2) + 1,
                                    true /* isLoad */);
    }
#if defined(WITH_SELF_VERIFICATION)
    if (load != NULL && cUnit->heapMemOp)
        load->branchInsertSV = true;
    if (load2 != NULL && cUnit->heapMemOp)
        load2->branchInsertSV = true;
#endif
    return res;
}

static ArmLIR *loadBaseDisp(CompilationUnit *cUnit, MIR *mir, int rBase,
                            int displacement, int rDest, OpSize size,
                            int sReg)
{
    return loadBaseDispBody(cUnit, mir, rBase, displacement, rDest, -1,
                            size, sReg);
}

static ArmLIR *loadBaseDispWide(CompilationUnit *cUnit, MIR *mir, int rBase,
                                int displacement, int rDestLo, int rDestHi,
                                int sReg)
{
    return loadBaseDispBody(cUnit, mir, rBase, displacement, rDestLo, rDestHi,
                            kLong, sReg);
}

static ArmLIR *storeBaseDispBody(CompilationUnit *cUnit, int rBase,
                                 int displacement, int rSrc, int rSrcHi,
                                 OpSize size)
{
    ArmLIR *res;
    ArmLIR *store = NULL;
    ArmLIR *store2 = NULL;
    ArmOpCode opCode = kThumbBkpt;
    bool shortForm = false;
    int encodedDisp = displacement;
    bool pair = false;

    switch (size) {
        case kLong:
        case kDouble:
            pair = true;
            if ((displacement < 124) && (displacement >= 0)) {
                assert((displacement & 0x3) == 0);
                pair = true;
                shortForm = true;
                encodedDisp >>= 2;
                opCode = kThumbStrRRI5;
            } else {
                opCode = kThumbStrRRR;
            }
            break;
        case kWord:
            if (displacement < 128 && displacement >= 0) {
                assert((displacement & 0x3) == 0);
                shortForm = true;
                encodedDisp >>= 2;
                opCode = kThumbStrRRI5;
            } else {
                opCode = kThumbStrRRR;
            }
            break;
        case kUnsignedHalf:
        case kSignedHalf:
            if (displacement < 64 && displacement >= 0) {
                assert((displacement & 0x1) == 0);
                shortForm = true;
                encodedDisp >>= 1;
                opCode = kThumbStrhRRI5;
            } else {
                opCode = kThumbStrhRRR;
            }
            break;
        case kUnsignedByte:
        case kSignedByte:
            if (displacement < 32 && displacement >= 0) {
                shortForm = true;
                opCode = kThumbStrbRRI5;
            } else {
                opCode = kThumbStrbRRR;
            }
            break;
        default:
            LOGE("Jit: bad case in storeBaseIndexedBody");
            dvmCompilerAbort(cUnit);
    }
    if (shortForm) {
        store = res = newLIR3(cUnit, opCode, rSrc, rBase, encodedDisp);
        if (pair) {
            store2 = newLIR3(cUnit, opCode, rSrcHi, rBase, encodedDisp + 1);
        }
    } else {
        int rScratch = dvmCompilerAllocTemp(cUnit);
        if (pair) {
            res = opRegRegImm(cUnit, kOpAdd, rScratch, rBase, displacement);
            store =  newLIR3(cUnit, kThumbStrRRI5, rSrc, rScratch, 0);
            store2 = newLIR3(cUnit, kThumbStrRRI5, rSrcHi, rScratch, 1);
        } else {
            res = loadConstant(cUnit, rScratch, displacement);
            store = newLIR3(cUnit, opCode, rSrc, rBase, rScratch);
        }
        dvmCompilerFreeTemp(cUnit, rScratch);
    }
    if (rBase == rFP) {
        if (store != NULL)
            annotateDalvikRegAccess(store, displacement >> 2,
                                    false /* isLoad */);
        if (store2 != NULL)
            annotateDalvikRegAccess(store2, (displacement >> 2) + 1,
                                    false /* isLoad */);
    }
#if defined(WITH_SELF_VERIFICATION)
    if (store != NULL && cUnit->heapMemOp)
        store->branchInsertSV = true;
    if (store2 != NULL && cUnit->heapMemOp)
        store2->branchInsertSV = true;
#endif
    return res;
}

static ArmLIR *storeBaseDisp(CompilationUnit *cUnit, int rBase,
                             int displacement, int rSrc, OpSize size)
{
    return storeBaseDispBody(cUnit, rBase, displacement, rSrc, -1, size);
}

static ArmLIR *storeBaseDispWide(CompilationUnit *cUnit, int rBase,
                                 int displacement, int rSrcLo, int rSrcHi)
{
    return storeBaseDispBody(cUnit, rBase, displacement, rSrcLo, rSrcHi, kLong);
}

static void storePair(CompilationUnit *cUnit, int base, int lowReg, int highReg)
{
    if (lowReg < highReg) {
        storeMultiple(cUnit, base, (1 << lowReg) | (1 << highReg));
    } else {
        storeWordDisp(cUnit, base, 0, lowReg);
        storeWordDisp(cUnit, base, 4, highReg);
    }
}

static void loadPair(CompilationUnit *cUnit, int base, int lowReg, int highReg)
{
    if (lowReg < highReg) {
        loadMultiple(cUnit, base, (1 << lowReg) | (1 << highReg));
    } else {
        loadWordDisp(cUnit, base, 0 , lowReg);
        loadWordDisp(cUnit, base, 4 , highReg);
    }
}

static ArmLIR* genRegCopyNoInsert(CompilationUnit *cUnit, int rDest, int rSrc)
{
    ArmLIR* res;
    ArmOpCode opCode;
    res = dvmCompilerNew(sizeof(ArmLIR), true);
    if (LOWREG(rDest) && LOWREG(rSrc))
        opCode = kThumbMovRR;
    else if (!LOWREG(rDest) && !LOWREG(rSrc))
         opCode = kThumbMovRR_H2H;
    else if (LOWREG(rDest))
         opCode = kThumbMovRR_H2L;
    else
         opCode = kThumbMovRR_L2H;

    res->operands[0] = rDest;
    res->operands[1] = rSrc;
    res->opCode = opCode;
    setupResourceMasks(res);
    if (rDest == rSrc) {
        res->isNop = true;
    }
    return res;
}

static ArmLIR* genRegCopy(CompilationUnit *cUnit, int rDest, int rSrc)
{
    ArmLIR *res = genRegCopyNoInsert(cUnit, rDest, rSrc);
    dvmCompilerAppendLIR(cUnit, (LIR*)res);
    return res;
}

static void genRegCopyWide(CompilationUnit *cUnit, int destLo, int destHi,
                           int srcLo, int srcHi)
{
    // Handle overlap
    if (srcHi == destLo) {
        genRegCopy(cUnit, destHi, srcHi);
        genRegCopy(cUnit, destLo, srcLo);
    } else {
        genRegCopy(cUnit, destLo, srcLo);
        genRegCopy(cUnit, destHi, srcHi);
    }
}

static inline ArmLIR *genRegImmCheck(CompilationUnit *cUnit,
                                     ArmConditionCode cond, int reg,
                                     int checkValue, int dOffset,
                                     ArmLIR *pcrLabel)
{
    int tReg;
    ArmLIR *res;
    if ((checkValue & 0xff) != checkValue) {
        tReg = dvmCompilerAllocTemp(cUnit);
        loadConstant(cUnit, tReg, checkValue);
        res = genRegRegCheck(cUnit, cond, reg, tReg, dOffset, pcrLabel);
        dvmCompilerFreeTemp(cUnit, tReg);
        return res;
    }
    newLIR2(cUnit, kThumbCmpRI8, reg, checkValue);
    ArmLIR *branch = newLIR2(cUnit, kThumbBCond, 0, cond);
    return genCheckCommon(cUnit, dOffset, branch, pcrLabel);
}
