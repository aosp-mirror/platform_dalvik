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

#include "Codegen.h"

static int coreTemps[] = {r0, r1, r2, r3, r4PC, r7};
static int corePreserved[] = {};
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

/*
 * Alloc a pair of core registers, or a double.  Low reg in low byte,
 * high reg in next byte.
 */
static int allocTypedTempPair(CompilationUnit *cUnit, bool fpHint, int regClass)
{
    int highReg;
    int lowReg;
    int res = 0;
    lowReg = allocTemp(cUnit);
    highReg = allocTemp(cUnit);
    res = (lowReg & 0xff) | ((highReg & 0xff) << 8);
    return res;
}

static int allocTypedTemp(CompilationUnit *cUnit, bool fpHint, int regClass)
{
    return allocTemp(cUnit);
}

ArmLIR* dvmCompilerRegCopy(CompilationUnit *cUnit, int rDest, int rSrc)
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

void genRegCopyWide(CompilationUnit *cUnit, int destLo, int destHi,
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

static ArmLIR *opNone(CompilationUnit *cUnit, OpKind op)
{
    ArmOpCode opCode = kThumbBkpt;
    switch (op) {
        case kOpUncondBr:
            opCode = kThumbBUncond;
            break;
        default:
            assert(0);
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
            assert(0);
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
            assert(0);
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
            assert(0);
            break;
    }
    if (shortForm)
        res = newLIR2(cUnit, opCode, rDestSrc1, absValue);
    else {
        int rScratch = allocTemp(cUnit);
        res = loadConstant(cUnit, rScratch, value);
        if (op == kOpCmp)
            newLIR2(cUnit, opCode, rDestSrc1, rScratch);
        else
            newLIR3(cUnit, opCode, rDestSrc1, rDestSrc1, rScratch);
    }
    return res;
}

static ArmLIR *opRegReg(CompilationUnit *cUnit, OpKind op, int rDest,
                        int rSrc);

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
                assert(isTemp(cUnit, rSrc1));
                clobberReg(cUnit, rSrc1);
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
                    int rScratch = allocTemp(cUnit);
                    res = loadConstant(cUnit, rScratch, value);
                    opRegReg(cUnit, op, rDest, rScratch);
                } else {
                    res = loadConstant(cUnit, rDest, value);
                    opRegReg(cUnit, op, rDest, rSrc1);
                }
                return res;
        default:
            assert(0);
            break;
    }
    if (shortForm)
        res = newLIR3(cUnit, opCode, rDest, rSrc1, absValue);
    else {
        if (rDest != rSrc1) {
            res = loadConstant(cUnit, rDest, value);
            newLIR3(cUnit, opCode, rDest, rSrc1, rDest);
        } else {
            int rScratch = allocTemp(cUnit);
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
            assert(0);
            break;
    }
    return newLIR2(cUnit, opCode, rDestSrc1, rSrc2);
}


static void handleMonitor(CompilationUnit *cUnit, MIR *mir)
{
    handleMonitorPortable(cUnit, mir);
}

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
        rNewIndex = allocTemp(cUnit);
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
            assert(0);
    }
    res = newLIR3(cUnit, opCode, rDest, rBase, rNewIndex);
    if (scale)
        freeTemp(cUnit, rNewIndex);
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
        rNewIndex = allocTemp(cUnit);
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
            assert(0);
    }
    res = newLIR3(cUnit, opCode, rSrc, rBase, rNewIndex);
    if (scale)
        freeTemp(cUnit, rNewIndex);
    return (first) ? first : res;
}

static ArmLIR *loadMultiple(CompilationUnit *cUnit, int rBase, int rMask)
{
    ArmLIR *res;
    genBarrier(cUnit);
    res = newLIR2(cUnit, kThumbLdmia, rBase, rMask);
    genBarrier(cUnit);
    return res;
}

static ArmLIR *storeMultiple(CompilationUnit *cUnit, int rBase, int rMask)
{
    ArmLIR *res;
    genBarrier(cUnit);
    res = newLIR2(cUnit, kThumbStmia, rBase, rMask);
    genBarrier(cUnit);
    return res;
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

static ArmLIR *loadBaseDispBody(CompilationUnit *cUnit, MIR *mir, int rBase,
                                int displacement, int rDest, int rDestHi,
                                OpSize size, bool nullCheck, int sReg)
/*
 * Load value from base + displacement.  Optionally perform null check
 * on base (which must have an associated sReg and MIR).  If not
 * performing null check, incoming MIR can be null. IMPORTANT: this
 * code must not allocate any new temps.  If a new register is needed
 * and base and dest are the same, spill some other register to
 * rlp and then restore.
 */
{
    ArmLIR *first = NULL;
    ArmLIR *res;
    ArmLIR *load = NULL;
    ArmLIR *load2 = NULL;
    ArmOpCode opCode = kThumbBkpt;
    bool shortForm = false;
    int shortMax = 128;
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
            assert(0);
    }
    if (nullCheck)
        first = genNullCheck(cUnit, sReg, rBase, mir->offset, NULL);
    if (shortForm) {
        load = res = newLIR3(cUnit, opCode, rDest, rBase, encodedDisp);
        if (pair) {
            load2 = newLIR3(cUnit, opCode, rDestHi, rBase, encodedDisp+1);
        }
    } else {
        if (pair) {
            int rTmp = allocFreeTemp(cUnit);
            if (rTmp < 0) {
                //UNIMP: need to spill if no temps.
                assert(0);
            }
            res = opRegRegImm(cUnit, kOpAdd, rTmp, rBase, displacement);
            //TUNING: how to mark loadPair if Dalvik access?
            loadPair(cUnit, rTmp, rDest, rDestHi);
            freeTemp(cUnit, rTmp);
        } else {
            int rTmp = (rBase == rDest) ? allocFreeTemp(cUnit) : rDest;
            if (rTmp < 0) {
                //UNIMP: need to spill if no temps.
                assert(0);
            }
            res = loadConstant(cUnit, rTmp, displacement);
            load = newLIR3(cUnit, opCode, rDest, rBase, rTmp);
            if (rBase == rFP)
                annotateDalvikRegAccess(load, displacement >> 2,
                                        true /* isLoad */);
            if (rTmp != rDest)
                freeTemp(cUnit, rTmp);
        }
    }

    return (first) ? first : res;
}

static ArmLIR *loadBaseDisp(CompilationUnit *cUnit, MIR *mir, int rBase,
                            int displacement, int rDest, OpSize size,
                            bool nullCheck, int sReg)
{
    return loadBaseDispBody(cUnit, mir, rBase, displacement, rDest, -1,
                            size, nullCheck, sReg);
}

static ArmLIR *loadBaseDispWide(CompilationUnit *cUnit, MIR *mir, int rBase,
                                int displacement, int rDestLo, int rDestHi,
                                bool nullCheck, int sReg)
{
    return loadBaseDispBody(cUnit, mir, rBase, displacement, rDestLo, rDestHi,
                            kLong, nullCheck, sReg);
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


static ArmLIR *storeBaseDispBody(CompilationUnit *cUnit, int rBase,
                                 int displacement, int rSrc, int rSrcHi,
                                 OpSize size)
{
    ArmLIR *res;
    ArmLIR *store = NULL;
    ArmLIR *store2 = NULL;
    ArmOpCode opCode = kThumbBkpt;
    bool shortForm = false;
    int shortMax = 128;
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
            assert(0);
    }
    if (shortForm) {
        store = res = newLIR3(cUnit, opCode, rSrc, rBase, encodedDisp);
        if (pair) {
            store2 = newLIR3(cUnit, opCode, rSrcHi, rBase, encodedDisp + 1);
        }
    } else {
        int rScratch = allocTemp(cUnit);
        if (pair) {
            //TUNING: how to mark storePair as Dalvik access if it is?
            res = opRegRegImm(cUnit, kOpAdd, rScratch, rBase, displacement);
            storePair(cUnit, rScratch, rSrc, rSrcHi);
        } else {
            res = loadConstant(cUnit, rScratch, displacement);
            store = newLIR3(cUnit, opCode, rSrc, rBase, rScratch);
            if (rBase == rFP) {
                annotateDalvikRegAccess(store, displacement >> 2,
                                        false /* isLoad */);
            }
        }
        freeTemp(cUnit, rScratch);
    }
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


/*
 * Perform a "reg cmp imm" operation and jump to the PCR region if condition
 * satisfies.
 */
static inline ArmLIR *genRegImmCheck(CompilationUnit *cUnit,
                                         ArmConditionCode cond, int reg,
                                         int checkValue, int dOffset,
                                         ArmLIR *pcrLabel)
{
    int tReg;
    ArmLIR *res;
    if ((checkValue & 0xff) != checkValue) {
        tReg = allocTemp(cUnit);
        loadConstant(cUnit, tReg, checkValue);
        res = genRegRegCheck(cUnit, cond, reg, tReg, dOffset, pcrLabel);
        freeTemp(cUnit, tReg);
        return res;
    }
    newLIR2(cUnit, kThumbCmpRI8, reg, checkValue);
    ArmLIR *branch = newLIR2(cUnit, kThumbBCond, 0, cond);
    return genCheckCommon(cUnit, dOffset, branch, pcrLabel);
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


/*
 * Load a immediate using a shortcut if possible; otherwise
 * grab from the per-translation literal pool.  If target is
 * a high register, build constant into a low register and copy.
 */
static ArmLIR *loadConstantValue(CompilationUnit *cUnit, int rDest, int value)
{
    ArmLIR *res;
    int tDest = LOWREG(rDest) ? rDest : allocTemp(cUnit);
    /* See if the value can be constructed cheaply */
    if ((value >= 0) && (value <= 255)) {
        res = newLIR2(cUnit, kThumbMovImm, tDest, value);
        if (rDest != tDest) {
           opRegReg(cUnit, kOpMov, rDest, tDest);
           freeTemp(cUnit, tDest);
        }
        return res;
    } else if ((value & 0xFFFFFF00) == 0xFFFFFF00) {
        res = newLIR2(cUnit, kThumbMovImm, tDest, ~value);
        newLIR2(cUnit, kThumbMvn, tDest, tDest);
        if (rDest != tDest) {
           opRegReg(cUnit, kOpMov, rDest, tDest);
           freeTemp(cUnit, tDest);
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
       freeTemp(cUnit, tDest);
    }
    return res;
}

static ArmLIR *loadConstantValueWide(CompilationUnit *cUnit, int rDestLo,
                                     int rDestHi, int valLo, int valHi)
{
    ArmLIR *res;
    res = loadConstantValue(cUnit, rDestLo, valLo);
    loadConstantValue(cUnit, rDestHi, valHi);
    return res;
}
