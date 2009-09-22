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
/* Forward decls */
static ArmLIR *genNullCheck(CompilationUnit *cUnit, int vReg, int mReg,
                            int dOffset, ArmLIR *pcrLabel);
static ArmLIR *loadValueAddress(CompilationUnit *cUnit, int vSrc, int rDest);
static ArmLIR *loadValue(CompilationUnit *cUnit, int vSrc, int rDest);
static ArmLIR *loadWordDisp(CompilationUnit *cUnit, int rBase,
                            int displacement, int rDest);
static ArmLIR *storeWordDisp(CompilationUnit *cUnit, int rBase,
                             int displacement, int rSrc, int rScratch);
static ArmLIR *storeValue(CompilationUnit *cUnit, int rSrc, int vDest,
                          int rScratch);
static ArmLIR *genConditionalBranch(CompilationUnit *cUnit,
                                    ArmConditionCode cond,
                                    ArmLIR *target);
static ArmLIR *genUnconditionalBranch(CompilationUnit *cUnit, ArmLIR *target);
static ArmLIR *loadValuePair(CompilationUnit *cUnit, int vSrc, int rDestLo,
                             int rDestHi);
static ArmLIR *storeValuePair(CompilationUnit *cUnit, int rSrcLo, int rSrcHi,
                              int vDest, int rScratch);
static ArmLIR *genBoundsCheck(CompilationUnit *cUnit, int rIndex,
                              int rBound, int dOffset, ArmLIR *pcrLabel);
static ArmLIR *genRegCopy(CompilationUnit *cUnit, int rDest, int rSrc);
static int inlinedTarget(MIR *mir);


/* Routines which must be supplied here */
static ArmLIR *loadConstant(CompilationUnit *cUnit, int rDest, int value);
static ArmLIR *genExportPC(CompilationUnit *cUnit, MIR *mir, int rDPC,
                           int rAddr);
static ArmLIR *loadBaseDisp(CompilationUnit *cUnit, MIR *mir, int rBase,
                            int displacement, int rDest, OpSize size,
                            bool nullCheck, int vReg);
static ArmLIR *storeBaseDisp(CompilationUnit *cUnit, int rBase,
                             int displacement, int rSrc, OpSize size,
                             int rScratch);
static inline ArmLIR *genRegImmCheck(CompilationUnit *cUnit,
                                     ArmConditionCode cond, int reg,
                                     int checkValue, int dOffset,
                                     ArmLIR *pcrLabel);
static inline ArmLIR *genRegRegCheck(CompilationUnit *cUnit,
                                     ArmConditionCode cond,
                                     int reg1, int reg2, int dOffset,
                                     ArmLIR *pcrLabel);
ArmLIR* dvmCompilerRegCopy(CompilationUnit *cUnit, int rDest, int rSrc);
static ArmLIR *loadMultiple(CompilationUnit *cUnit, int rBase, int rMask);
static ArmLIR *storeMultiple(CompilationUnit *cUnit, int rBase, int rMask);

static ArmLIR *opNone(CompilationUnit *cUnit, OpKind op);
static ArmLIR *opImm(CompilationUnit *cUnit, OpKind op, int value);
static ArmLIR *opImmImm(CompilationUnit *cUnit, OpKind op, int value1,
                        int value2);
static ArmLIR *opReg(CompilationUnit *cUnit, OpKind op, int rDestSrc);
static ArmLIR *opRegReg(CompilationUnit *cUnit, OpKind op, int rDestSrc1,
                        int rSrc2);
static ArmLIR *opRegImm(CompilationUnit *cUnit, OpKind op, int rDestSrc1,
                        int value, int rScratch);
static ArmLIR *opRegRegImm(CompilationUnit *cUnit, OpKind op, int rDest,
                           int rSrc1, int value, int rScratch);
static ArmLIR *opRegRegReg(CompilationUnit *cUnit, OpKind op, int rDest,
                           int rSrc1, int rSrc2);
static ArmLIR *loadBaseIndexed(CompilationUnit *cUnit, int rBase,
                               int rIndex, int rDest, int scale, OpSize size);
static void genCmpLong(CompilationUnit *cUnit, MIR *mir, int vDest, int vSrc1,
                       int vSrc2);

static bool genInlinedStringLength(CompilationUnit *cUnit, MIR *mir);
static bool genInlinedStringCharAt(CompilationUnit *cUnit, MIR *mir);
static bool genInlinedAbsInt(CompilationUnit *cUnit, MIR *mir);
static bool genInlinedAbsFloat(CompilationUnit *cUnit, MIR *mir);
static bool genInlinedAbsDouble(CompilationUnit *cUnit, MIR *mir);
static bool genInlinedMinMaxInt(CompilationUnit *cUnit, MIR *mir, bool isMin);
static bool genInlinedAbsLong(CompilationUnit *cUnit, MIR *mir);

/*
 * Support for register allocation
 */

/* get the next register in r0..r3 in a round-robin fashion */
#define NEXT_REG(reg) ((reg + 1) & 3)
/*
 * The following are utility routines to help maintain the RegisterScoreboard
 * state to facilitate register renaming.
 */

/* Reset the tracker to unknown state */
static inline void resetRegisterScoreboard(CompilationUnit *cUnit)
{
    RegisterScoreboard *registerScoreboard = &cUnit->registerScoreboard;

    dvmClearAllBits(registerScoreboard->nullCheckedRegs);
    registerScoreboard->liveDalvikReg = vNone;
    registerScoreboard->nativeReg = vNone;
    registerScoreboard->nativeRegHi = vNone;
}

/* Kill the corresponding bit in the null-checked register list */
static inline void killNullCheckedRegister(CompilationUnit *cUnit, int vReg)
{
    dvmClearBit(cUnit->registerScoreboard.nullCheckedRegs, vReg);
}

/* The Dalvik register pair held in native registers have changed */
static inline void updateLiveRegisterPair(CompilationUnit *cUnit,
                                          int vReg, int mRegLo, int mRegHi)
{
    cUnit->registerScoreboard.liveDalvikReg = vReg;
    cUnit->registerScoreboard.nativeReg = mRegLo;
    cUnit->registerScoreboard.nativeRegHi = mRegHi;
    cUnit->registerScoreboard.isWide = true;
}

/* The Dalvik register held in a native register has changed */
static inline void updateLiveRegister(CompilationUnit *cUnit,
                                      int vReg, int mReg)
{
    cUnit->registerScoreboard.liveDalvikReg = vReg;
    cUnit->registerScoreboard.nativeReg = mReg;
    cUnit->registerScoreboard.isWide = false;
}

/*
 * Given a Dalvik register id vSrc, use a very simple algorithm to increase
 * the lifetime of cached Dalvik value in a native register.
 */
static inline int selectFirstRegister(CompilationUnit *cUnit, int vSrc,
                                      bool isWide)
{
    RegisterScoreboard *registerScoreboard = &cUnit->registerScoreboard;

    /* No live value - suggest to use r0 */
    if (registerScoreboard->liveDalvikReg == vNone)
        return r0;

    /* Reuse the previously used native reg */
    if (registerScoreboard->liveDalvikReg == vSrc) {
        if (isWide != true) {
            return registerScoreboard->nativeReg;
        } else {
            /* Return either r0 or r2 */
            return (registerScoreboard->nativeReg + 1) & 2;
        }
    }

    /* No reuse - choose the next one among r0..r3 in the round-robin fashion */
    if (isWide) {
        return (registerScoreboard->nativeReg + 2) & 2;
    } else {
        return (registerScoreboard->nativeReg + 1) & 3;
    }

}

ArmLIR* dvmCompilerRegCopy(CompilationUnit *cUnit, int rDest, int rSrc)
{
    ArmLIR* res;
    ArmOpCode opCode;
    res = dvmCompilerNew(sizeof(ArmLIR), true);
    if (LOWREG(rDest) && LOWREG(rSrc))
        opCode = THUMB_MOV_RR;
    else if (!LOWREG(rDest) && !LOWREG(rSrc))
         opCode = THUMB_MOV_RR_H2H;
    else if (LOWREG(rDest))
         opCode = THUMB_MOV_RR_H2L;
    else
         opCode = THUMB_MOV_RR_L2H;

    res->operands[0] = rDest;
    res->operands[1] = rSrc;
    res->opCode = opCode;
    setupResourceMasks(res);
    if (rDest == rSrc) {
        res->isNop = true;
    }
    return res;
}

/*
 * Load a immediate using a shortcut if possible; otherwise
 * grab from the per-translation literal pool
 */
static ArmLIR *loadConstant(CompilationUnit *cUnit, int rDest, int value)
{
    ArmLIR *res;
    /* See if the value can be constructed cheaply */
    if ((value >= 0) && (value <= 255)) {
        return newLIR2(cUnit, THUMB_MOV_IMM, rDest, value);
    } else if ((value & 0xFFFFFF00) == 0xFFFFFF00) {
        res = newLIR2(cUnit, THUMB_MOV_IMM, rDest, ~value);
        newLIR2(cUnit, THUMB_MVN, rDest, rDest);
        return res;
    }
    /* No shortcut - go ahead and use literal pool */
    ArmLIR *dataTarget = scanLiteralPool(cUnit, value, 255);
    if (dataTarget == NULL) {
        dataTarget = addWordData(cUnit, value, false);
    }
    ArmLIR *loadPcRel = dvmCompilerNew(sizeof(ArmLIR), true);
    loadPcRel->opCode = THUMB_LDR_PC_REL;
    loadPcRel->generic.target = (LIR *) dataTarget;
    loadPcRel->operands[0] = rDest;
    setupResourceMasks(loadPcRel);
    res = loadPcRel;
    dvmCompilerAppendLIR(cUnit, (LIR *) loadPcRel);

    /*
     * To save space in the constant pool, we use the ADD_RRI8 instruction to
     * add up to 255 to an existing constant value.
     */
    if (dataTarget->operands[0] != value) {
        newLIR2(cUnit, THUMB_ADD_RI8, rDest, value - dataTarget->operands[0]);
    }
    return res;
}

/* Export the Dalvik PC assicated with an instruction to the StackSave area */
static ArmLIR *genExportPC(CompilationUnit *cUnit, MIR *mir, int rDPC,
                           int rAddr)
{
    ArmLIR *res;
    int offset = offsetof(StackSaveArea, xtra.currentPc);
    res = loadConstant(cUnit, rDPC, (int) (cUnit->method->insns + mir->offset));
    newLIR2(cUnit, THUMB_MOV_RR, rAddr, rFP);
    newLIR2(cUnit, THUMB_SUB_RI8, rAddr, sizeof(StackSaveArea) - offset);
    storeWordDisp( cUnit, rAddr, 0, rDPC, -1);
    return res;
}

/* Load value from base + scaled index. Note: index reg killed */
static ArmLIR *loadBaseIndexed(CompilationUnit *cUnit, int rBase,
                               int rIndex, int rDest, int scale, OpSize size)
{
    ArmLIR *first = NULL;
    ArmLIR *res;
    ArmOpCode opCode = THUMB_BKPT;
    if (scale)
        first = opRegRegImm(cUnit, OP_LSL, rIndex, rIndex, scale, rNone);
    switch (size) {
        case WORD:
            opCode = THUMB_LDR_RRR;
            break;
        case UNSIGNED_HALF:
            opCode = THUMB_LDRH_RRR;
            break;
        case SIGNED_HALF:
            opCode = THUMB_LDRSH_RRR;
            break;
        case UNSIGNED_BYTE:
            opCode = THUMB_LDRB_RRR;
            break;
        case SIGNED_BYTE:
            opCode = THUMB_LDRSB_RRR;
            break;
        default:
            assert(0);
    }
    res = newLIR3(cUnit, opCode, rDest, rBase, rIndex);
    return (first) ? first : res;
}

/* store value base base + scaled index. Note: index reg killed */
static ArmLIR *storeBaseIndexed(CompilationUnit *cUnit, int rBase,
                                int rIndex, int rSrc, int scale, OpSize size)
{
    ArmLIR *first = NULL;
    ArmLIR *res;
    ArmOpCode opCode = THUMB_BKPT;
    if (scale)
        first = opRegRegImm(cUnit, OP_LSL, rIndex, rIndex, scale, rNone);
    switch (size) {
        case WORD:
            opCode = THUMB_STR_RRR;
            break;
        case UNSIGNED_HALF:
        case SIGNED_HALF:
            opCode = THUMB_STRH_RRR;
            break;
        case UNSIGNED_BYTE:
        case SIGNED_BYTE:
            opCode = THUMB_STRB_RRR;
            break;
        default:
            assert(0);
    }
    res = newLIR3(cUnit, opCode, rSrc, rBase, rIndex);
    return (first) ? first : res;
}

/*
 * Load value from base + displacement.  Optionally perform null check
 * on base (which must have an associated vReg and MIR).  If not
 * performing null check, incoming MIR can be null. Note: base and
 * dest must not be the same if there is any chance that the long
 * form must be used.
 */
static ArmLIR *loadBaseDisp(CompilationUnit *cUnit, MIR *mir, int rBase,
                            int displacement, int rDest, OpSize size,
                            bool nullCheck, int vReg)
{
    ArmLIR *first = NULL;
    ArmLIR *res, *load;
    ArmOpCode opCode = THUMB_BKPT;
    bool shortForm = false;
    int shortMax = 128;
    int encodedDisp = displacement;

    switch (size) {
        case WORD:
            if (LOWREG(rDest) && (rBase == rpc) &&
                (displacement <= 1020) && (displacement >= 0)) {
                shortForm = true;
                encodedDisp >>= 2;
                opCode = THUMB_LDR_PC_REL;
            } else if (LOWREG(rDest) && (rBase == r13) &&
                      (displacement <= 1020) && (displacement >= 0)) {
                shortForm = true;
                encodedDisp >>= 2;
                opCode = THUMB_LDR_SP_REL;
            } else if (displacement < 128 && displacement >= 0) {
                assert((displacement & 0x3) == 0);
                shortForm = true;
                encodedDisp >>= 2;
                opCode = THUMB_LDR_RRI5;
            } else {
                opCode = THUMB_LDR_RRR;
            }
            break;
        case UNSIGNED_HALF:
            if (displacement < 64 && displacement >= 0) {
                assert((displacement & 0x1) == 0);
                shortForm = true;
                encodedDisp >>= 1;
                opCode = THUMB_LDRH_RRI5;
            } else {
                opCode = THUMB_LDRH_RRR;
            }
            break;
        case SIGNED_HALF:
            opCode = THUMB_LDRSH_RRR;
            break;
        case UNSIGNED_BYTE:
            if (displacement < 32 && displacement >= 0) {
                shortForm = true;
                opCode = THUMB_LDRB_RRI5;
            } else {
                opCode = THUMB_LDRB_RRR;
            }
            break;
        case SIGNED_BYTE:
            opCode = THUMB_LDRSB_RRR;
            break;
        default:
            assert(0);
    }
    if (nullCheck)
        first = genNullCheck(cUnit, vReg, rBase, mir->offset, NULL);
    if (shortForm) {
        load = res = newLIR3(cUnit, opCode, rDest, rBase, encodedDisp);
    } else {
        assert(rBase != rDest);
        res = loadConstant(cUnit, rDest, encodedDisp);
        load = newLIR3(cUnit, opCode, rDest, rBase, rDest);
    }

    if (rBase == rFP) {
        annotateDalvikRegAccess(load, displacement >> 2, true /* isLoad */);
    }

    return (first) ? first : res;
}

static ArmLIR *storeBaseDisp(CompilationUnit *cUnit, int rBase,
                             int displacement, int rSrc, OpSize size,
                             int rScratch)
{
    ArmLIR *res, *store;
    ArmOpCode opCode = THUMB_BKPT;
    bool shortForm = false;
    int shortMax = 128;
    int encodedDisp = displacement;

    switch (size) {
        case WORD:
            if (displacement < 128 && displacement >= 0) {
                assert((displacement & 0x3) == 0);
                shortForm = true;
                encodedDisp >>= 2;
                opCode = THUMB_STR_RRI5;
            } else {
                opCode = THUMB_STR_RRR;
            }
            break;
        case UNSIGNED_HALF:
        case SIGNED_HALF:
            if (displacement < 64 && displacement >= 0) {
                assert((displacement & 0x1) == 0);
                shortForm = true;
                encodedDisp >>= 1;
                opCode = THUMB_STRH_RRI5;
            } else {
                opCode = THUMB_STRH_RRR;
            }
            break;
        case UNSIGNED_BYTE:
        case SIGNED_BYTE:
            if (displacement < 32 && displacement >= 0) {
                shortForm = true;
                opCode = THUMB_STRB_RRI5;
            } else {
                opCode = THUMB_STRB_RRR;
            }
            break;
        default:
            assert(0);
    }
    if (shortForm) {
        store = res = newLIR3(cUnit, opCode, rSrc, rBase, encodedDisp);
    } else {
        assert(rScratch != -1);
        res = loadConstant(cUnit, rScratch, encodedDisp);
        store = newLIR3(cUnit, opCode, rSrc, rBase, rScratch);
    }

    if (rBase == rFP) {
        annotateDalvikRegAccess(store, displacement >> 2, false /* isLoad */);
    }
    return res;
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
    if ((checkValue & 0xff) != checkValue) {
        /* NOTE: direct use of hot temp r7 here. Revisit. */
        loadConstant(cUnit, r7, checkValue);
        return genRegRegCheck(cUnit, cond, reg, r7, dOffset, pcrLabel);
    }
    newLIR2(cUnit, THUMB_CMP_RI8, reg, checkValue);
    ArmLIR *branch = newLIR2(cUnit, THUMB_B_COND, 0, cond);
    return genCheckCommon(cUnit, dOffset, branch, pcrLabel);
}

static ArmLIR *loadMultiple(CompilationUnit *cUnit, int rBase, int rMask)
{
    return newLIR2(cUnit, THUMB_LDMIA, rBase, rMask);
}

static ArmLIR *storeMultiple(CompilationUnit *cUnit, int rBase, int rMask)
{
    return newLIR2(cUnit, THUMB_STMIA, rBase, rMask);
}

static ArmLIR *opNone(CompilationUnit *cUnit, OpKind op)
{
    ArmOpCode opCode = THUMB_BKPT;
    switch (op) {
        case OP_UNCOND_BR:
            opCode = THUMB_B_UNCOND;
            break;
        default:
            assert(0);
    }
    return newLIR0(cUnit, opCode);
}

static ArmLIR *opImmImm(CompilationUnit *cUnit, OpKind op, int value1,
                        int value2)
{
    ArmOpCode opCode = THUMB_BKPT;
    switch (op) {
        case OP_COND_BR:
            opCode = THUMB_B_COND;
            break;
        default:
            assert(0);
    }
    return newLIR2(cUnit, opCode, value1, value2);
}

static ArmLIR *opImm(CompilationUnit *cUnit, OpKind op, int value)
{
    ArmOpCode opCode = THUMB_BKPT;
    switch (op) {
        case OP_PUSH:
            opCode = THUMB_PUSH;
            break;
        case OP_POP:
            opCode = THUMB_POP;
            break;
        default:
            assert(0);
    }
    return newLIR1(cUnit, opCode, value);
}

static ArmLIR *opReg(CompilationUnit *cUnit, OpKind op, int rDestSrc)
{
    ArmOpCode opCode = THUMB_BKPT;
    switch (op) {
        case OP_BLX:
            opCode = THUMB_BLX_R;
            break;
        default:
            assert(0);
    }
    return newLIR1(cUnit, opCode, rDestSrc);
}

static ArmLIR *opRegReg(CompilationUnit *cUnit, OpKind op, int rDestSrc1,
                        int rSrc2)
{
    ArmLIR *res;
    ArmOpCode opCode = THUMB_BKPT;
    switch (op) {
        case OP_ADC:
            opCode = THUMB_ADC_RR;
            break;
        case OP_AND:
            opCode = THUMB_AND_RR;
            break;
        case OP_BIC:
            opCode = THUMB_BIC_RR;
            break;
        case OP_CMN:
            opCode = THUMB_CMN_RR;
            break;
        case OP_CMP:
            opCode = THUMB_CMP_RR;
            break;
        case OP_XOR:
            opCode = THUMB_EOR_RR;
            break;
        case OP_MOV:
            if (LOWREG(rDestSrc1) && LOWREG(rSrc2))
                opCode = THUMB_MOV_RR;
            else if (!LOWREG(rDestSrc1) && !LOWREG(rSrc2))
                opCode = THUMB_MOV_RR_H2H;
            else if (LOWREG(rDestSrc1))
                opCode = THUMB_MOV_RR_H2L;
            else
                opCode = THUMB_MOV_RR_L2H;
            break;
        case OP_MUL:
            opCode = THUMB_MUL;
            break;
        case OP_MVN:
            opCode = THUMB_MVN;
            break;
        case OP_NEG:
            opCode = THUMB_NEG;
            break;
        case OP_OR:
            opCode = THUMB_ORR;
            break;
        case OP_SBC:
            opCode = THUMB_SBC;
            break;
        case OP_TST:
            opCode = THUMB_TST;
            break;
        case OP_LSL:
            opCode = THUMB_LSL_RR;
            break;
        case OP_LSR:
            opCode = THUMB_LSR_RR;
            break;
        case OP_ASR:
            opCode = THUMB_ASR_RR;
            break;
        case OP_ROR:
            opCode = THUMB_ROR_RR;
        case OP_ADD:
        case OP_SUB:
            return opRegRegReg(cUnit, op, rDestSrc1, rDestSrc1, rSrc2);
        case OP_2BYTE:
             res = opRegRegImm(cUnit, OP_LSL, rDestSrc1, rSrc2, 24, rNone);
             opRegRegImm(cUnit, OP_ASR, rDestSrc1, rDestSrc1, 24, rNone);
             return res;
        case OP_2SHORT:
             res = opRegRegImm(cUnit, OP_LSL, rDestSrc1, rSrc2, 16, rNone);
             opRegRegImm(cUnit, OP_ASR, rDestSrc1, rDestSrc1, 16, rNone);
             return res;
        case OP_2CHAR:
             res = opRegRegImm(cUnit, OP_LSL, rDestSrc1, rSrc2, 16, rNone);
             opRegRegImm(cUnit, OP_LSR, rDestSrc1, rDestSrc1, 16, rNone);
             return res;
        default:
            assert(0);
            break;
    }
    return newLIR2(cUnit, opCode, rDestSrc1, rSrc2);
}

static ArmLIR *opRegImm(CompilationUnit *cUnit, OpKind op, int rDestSrc1,
                        int value, int rScratch)
{
    ArmLIR *res;
    bool neg = (value < 0);
    int absValue = (neg) ? -value : value;
    bool shortForm = (absValue & 0xff) == absValue;
    ArmOpCode opCode = THUMB_BKPT;
    switch (op) {
        case OP_ADD:
            if ( !neg && (rDestSrc1 == 13) && (value <= 508)) { /* sp */
                assert((value & 0x3) == 0);
                return newLIR1(cUnit, THUMB_ADD_SPI7, value >> 2);
            } else if (shortForm) {
                opCode = (neg) ? THUMB_SUB_RI8 : THUMB_ADD_RI8;
            } else
                opCode = THUMB_ADD_RRR;
            break;
        case OP_SUB:
            if (!neg && (rDestSrc1 == 13) && (value <= 508)) { /* sp */
                assert((value & 0x3) == 0);
                return newLIR1(cUnit, THUMB_SUB_SPI7, value >> 2);
            } else if (shortForm) {
                opCode = (neg) ? THUMB_ADD_RI8 : THUMB_SUB_RI8;
            } else
                opCode = THUMB_SUB_RRR;
            break;
        case OP_CMP:
            if (LOWREG(rDestSrc1) && shortForm)
                opCode = (shortForm) ?  THUMB_CMP_RI8 : THUMB_CMP_RR;
            else if (LOWREG(rDestSrc1))
                opCode = THUMB_CMP_RR;
            else {
                shortForm = false;
                opCode = THUMB_CMP_HL;
            }
            break;
        default:
            assert(0);
            break;
    }
    if (shortForm)
        res = newLIR2(cUnit, opCode, rDestSrc1, absValue);
    else {
        assert(rScratch != rNone);
        res = loadConstant(cUnit, rScratch, value);
        newLIR3(cUnit, opCode, rDestSrc1, rDestSrc1, rScratch);
    }
    return res;
}

static ArmLIR *opRegRegReg(CompilationUnit *cUnit, OpKind op, int rDest,
                           int rSrc1, int rSrc2)
{
    ArmOpCode opCode = THUMB_BKPT;
    switch (op) {
        case OP_ADD:
            opCode = THUMB_ADD_RRR;
            break;
        case OP_SUB:
            opCode = THUMB_SUB_RRR;
            break;
        default:
            assert(0);
            break;
    }
    return newLIR3(cUnit, opCode, rDest, rSrc1, rSrc2);
}

static ArmLIR *opRegRegImm(CompilationUnit *cUnit, OpKind op, int rDest,
                           int rSrc1, int value, int rScratch)
{
    ArmLIR *res;
    bool neg = (value < 0);
    int absValue = (neg) ? -value : value;
    ArmOpCode opCode = THUMB_BKPT;
    bool shortForm = (absValue & 0x7) == absValue;
    switch(op) {
        case OP_ADD:
            if ((rSrc1 == 13) && (value <= 1020)) { /* sp */
                assert((value & 0x3) == 0);
                shortForm = true;
                opCode = THUMB_ADD_SP_REL;
                value >>= 2;
            } else if ((rSrc1 == 15) && (value <= 1020)) { /* pc */
                assert((value & 0x3) == 0);
                shortForm = true;
                opCode = THUMB_ADD_PC_REL;
                value >>= 2;
            } else if (shortForm) {
                opCode = (neg) ? THUMB_SUB_RRI3 : THUMB_ADD_RRI3;
            } else if ((absValue > 0) && (absValue <= (255 + 7))) {
                /* Two shots - 1st handle the 7 */
                opCode = (neg) ? THUMB_SUB_RRI3 : THUMB_ADD_RRI3;
                res = newLIR3(cUnit, opCode, rDest, rSrc1, 7);
                opCode = (neg) ? THUMB_SUB_RI8 : THUMB_ADD_RI8;
                newLIR2(cUnit, opCode, rDest, absValue - 7);
                return res;
            } else
                opCode = THUMB_ADD_RRR;
            break;

        case OP_SUB:
            if (shortForm) {
                opCode = (neg) ? THUMB_ADD_RRI3 : THUMB_SUB_RRI3;
            } else if ((absValue > 0) && (absValue <= (255 + 7))) {
                /* Two shots - 1st handle the 7 */
                opCode = (neg) ? THUMB_ADD_RRI3 : THUMB_SUB_RRI3;
                res = newLIR3(cUnit, opCode, rDest, rSrc1, 7);
                opCode = (neg) ? THUMB_ADD_RI8 : THUMB_SUB_RI8;
                newLIR2(cUnit, opCode, rDest, absValue - 7);
                return res;
            } else
                opCode = THUMB_SUB_RRR;
            break;
        case OP_LSL:
                shortForm = (!neg && value <= 31);
                opCode = THUMB_LSL_RRI5;
                break;
        case OP_LSR:
                shortForm = (!neg && value <= 31);
                opCode = THUMB_LSR_RRI5;
                break;
        case OP_ASR:
                shortForm = (!neg && value <= 31);
                opCode = THUMB_ASR_RRI5;
                break;
        case OP_MUL:
        case OP_AND:
        case OP_OR:
        case OP_XOR:
                if (rDest == rSrc1) {
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
            assert(rScratch != rNone);
            res = loadConstant(cUnit, rScratch, value);
            newLIR3(cUnit, opCode, rDest, rSrc1, rScratch);
        }
    }
    return res;
}

static void genCmpLong(CompilationUnit *cUnit, MIR *mir,
                               int vDest, int vSrc1, int vSrc2)
{
    loadValuePair(cUnit, vSrc1, r0, r1);
    loadValuePair(cUnit, vSrc2, r2, r3);
    genDispatchToHandler(cUnit, TEMPLATE_CMP_LONG);
    storeValue(cUnit, r0, vDest, r1);
}

static bool genInlinedStringLength(CompilationUnit *cUnit, MIR *mir)
{
    DecodedInstruction *dInsn = &mir->dalvikInsn;
    int offset = offsetof(InterpState, retval);
    int regObj = selectFirstRegister(cUnit, dInsn->arg[0], false);
    int reg1 = NEXT_REG(regObj);
    loadValue(cUnit, dInsn->arg[0], regObj);
    genNullCheck(cUnit, dInsn->arg[0], regObj, mir->offset, NULL);
    loadWordDisp(cUnit, regObj, gDvm.offJavaLangString_count, reg1);
    storeWordDisp(cUnit, rGLUE, offset, reg1, regObj);
    return false;
}

static bool genInlinedStringCharAt(CompilationUnit *cUnit, MIR *mir)
{
    DecodedInstruction *dInsn = &mir->dalvikInsn;
    int offset = offsetof(InterpState, retval);
    int contents = offsetof(ArrayObject, contents);
    int regObj = selectFirstRegister(cUnit, dInsn->arg[0], false);
    int regIdx = NEXT_REG(regObj);
    int regMax = NEXT_REG(regIdx);
    int regOff = NEXT_REG(regMax);
    loadValue(cUnit, dInsn->arg[0], regObj);
    loadValue(cUnit, dInsn->arg[1], regIdx);
    ArmLIR * pcrLabel = genNullCheck(cUnit, dInsn->arg[0], regObj,
                                         mir->offset, NULL);
    loadWordDisp(cUnit, regObj, gDvm.offJavaLangString_count, regMax);
    loadWordDisp(cUnit, regObj, gDvm.offJavaLangString_offset, regOff);
    loadWordDisp(cUnit, regObj, gDvm.offJavaLangString_value, regObj);
    genBoundsCheck(cUnit, regIdx, regMax, mir->offset, pcrLabel);

    newLIR2(cUnit, THUMB_ADD_RI8, regObj, contents);
    newLIR3(cUnit, THUMB_ADD_RRR, regIdx, regIdx, regOff);
    newLIR3(cUnit, THUMB_ADD_RRR, regIdx, regIdx, regIdx);
    newLIR3(cUnit, THUMB_LDRH_RRR, regMax, regObj, regIdx);
    storeWordDisp(cUnit, rGLUE, offset, regMax, regObj);
    return false;
}

static bool genInlinedAbsInt(CompilationUnit *cUnit, MIR *mir)
{
    int offset = offsetof(InterpState, retval);
    DecodedInstruction *dInsn = &mir->dalvikInsn;
    int reg0 = selectFirstRegister(cUnit, dInsn->arg[0], false);
    int sign = NEXT_REG(reg0);
    /* abs(x) = y<=x>>31, (x+y)^y.  Shorter in ARM/THUMB2, no skip in THUMB */
    loadValue(cUnit, dInsn->arg[0], reg0);
    newLIR3(cUnit, THUMB_ASR_RRI5, sign, reg0, 31);
    newLIR3(cUnit, THUMB_ADD_RRR, reg0, reg0, sign);
    newLIR2(cUnit, THUMB_EOR_RR, reg0, sign);
    storeWordDisp(cUnit, rGLUE, offset, reg0, sign);
    return false;
}

static bool genInlinedAbsFloat(CompilationUnit *cUnit, MIR *mir)
{
    int offset = offsetof(InterpState, retval);
    DecodedInstruction *dInsn = &mir->dalvikInsn;
    int reg0 = selectFirstRegister(cUnit, dInsn->arg[0], false);
    int signMask = NEXT_REG(reg0);
    loadValue(cUnit, dInsn->arg[0], reg0);
    loadConstant(cUnit, signMask, 0x7fffffff);
    newLIR2(cUnit, THUMB_AND_RR, reg0, signMask);
    storeWordDisp(cUnit, rGLUE, offset, reg0, signMask);
    return false;
}

static bool genInlinedAbsDouble(CompilationUnit *cUnit, MIR *mir)
{
    int offset = offsetof(InterpState, retval);
    DecodedInstruction *dInsn = &mir->dalvikInsn;
    int oplo = selectFirstRegister(cUnit, dInsn->arg[0], true);
    int ophi = NEXT_REG(oplo);
    int signMask = NEXT_REG(ophi);
    loadValuePair(cUnit, dInsn->arg[0], oplo, ophi);
    loadConstant(cUnit, signMask, 0x7fffffff);
    storeWordDisp(cUnit, rGLUE, offset, oplo, ophi);
    newLIR2(cUnit, THUMB_AND_RR, ophi, signMask);
    storeWordDisp(cUnit, rGLUE, offset + 4, ophi, oplo);
    return false;
}

 /* No select in thumb, so we need to branch.  Thumb2 will do better */
static bool genInlinedMinMaxInt(CompilationUnit *cUnit, MIR *mir, bool isMin)
{
    int offset = offsetof(InterpState, retval);
    DecodedInstruction *dInsn = &mir->dalvikInsn;
    int reg0 = selectFirstRegister(cUnit, dInsn->arg[0], false);
    int reg1 = NEXT_REG(reg0);
    loadValue(cUnit, dInsn->arg[0], reg0);
    loadValue(cUnit, dInsn->arg[1], reg1);
    newLIR2(cUnit, THUMB_CMP_RR, reg0, reg1);
    ArmLIR *branch1 = newLIR2(cUnit, THUMB_B_COND, 2,
           isMin ? ARM_COND_LT : ARM_COND_GT);
    newLIR2(cUnit, THUMB_MOV_RR, reg0, reg1);
    ArmLIR *target =
        newLIR3(cUnit, THUMB_STR_RRI5, reg0, rGLUE, offset >> 2);
    branch1->generic.target = (LIR *)target;
    return false;
}

static bool genInlinedAbsLong(CompilationUnit *cUnit, MIR *mir)
{
    int offset = offsetof(InterpState, retval);
    DecodedInstruction *dInsn = &mir->dalvikInsn;
    int oplo = selectFirstRegister(cUnit, dInsn->arg[0], true);
    int ophi = NEXT_REG(oplo);
    int sign = NEXT_REG(ophi);
    /* abs(x) = y<=x>>31, (x+y)^y.  Shorter in ARM/THUMB2, no skip in THUMB */
    loadValuePair(cUnit, dInsn->arg[0], oplo, ophi);
    newLIR3(cUnit, THUMB_ASR_RRI5, sign, ophi, 31);
    newLIR3(cUnit, THUMB_ADD_RRR, oplo, oplo, sign);
    newLIR2(cUnit, THUMB_ADC_RR, ophi, sign);
    newLIR2(cUnit, THUMB_EOR_RR, oplo, sign);
    newLIR2(cUnit, THUMB_EOR_RR, ophi, sign);
    storeWordDisp(cUnit, rGLUE, offset, oplo, sign);
    storeWordDisp(cUnit, rGLUE, offset + 4, ophi, sign);
    return false;
}
