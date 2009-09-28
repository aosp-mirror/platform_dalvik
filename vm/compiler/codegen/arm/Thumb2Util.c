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
ArmLIR* dvmCompilerRegCopy(CompilationUnit *cUnit, int rDest, int rSrc);
static ArmLIR *loadMultiple(CompilationUnit *cUnit, int rBase, int rMask);
static ArmLIR *storeMultiple(CompilationUnit *cUnit, int rBase, int rMask);

static ArmLIR *opNone(CompilationUnit *cUnit, OpKind op);
static ArmLIR *opImm(CompilationUnit *cUnit, OpKind op, int value);
static ArmLIR *opCondBranch(CompilationUnit *cUnit, ArmConditionCode cc);
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
    int i;

    dvmClearAllBits(registerScoreboard->nullCheckedRegs);
    registerScoreboard->liveDalvikReg = vNone;
    registerScoreboard->nativeReg = vNone;
    registerScoreboard->nativeRegHi = vNone;
    for (i = 0; i < 32; i++) {
        registerScoreboard->fp[i] = vNone;
    }
    registerScoreboard->nextFP = 0;
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
    return newLIR2(cUnit, THUMB2_IT, code, mask);
}


static ArmLIR *fpRegCopy(CompilationUnit *cUnit, int rDest, int rSrc)
{
    ArmLIR* res = dvmCompilerNew(sizeof(ArmLIR), true);
    res->operands[0] = rDest;
    res->operands[1] = rSrc;
    if (rDest == rSrc) {
        res->isNop = true;
    } else {
        // TODO: support copy between FP and gen regs
        if (DOUBLEREG(rDest)) {
            assert(DOUBLEREG(rSrc));
            res->opCode = THUMB2_VMOVD;
        } else {
            assert(SINGLEREG(rSrc));
            res->opCode = THUMB2_VMOVS;
        }
        res->operands[0] = rDest;
        res->operands[1] = rSrc;
    }
    setupResourceMasks(res);
    return res;
}

ArmLIR* dvmCompilerRegCopy(CompilationUnit *cUnit, int rDest, int rSrc)
{
    ArmLIR* res;
    ArmOpCode opCode;
    if (FPREG(rDest) || FPREG(rSrc))
        return fpRegCopy(cUnit, rDest, rSrc);
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

static int leadingZeros(u4 val)
{
    u4 alt;
    int n;
    int count;

    count = 16;
    n = 32;
    do {
        alt = val >> count;
        if (alt != 0) {
            n = n - count;
            val = alt;
        }
        count >>= 1;
    } while (count);
    return n - val;
}

/*
 * Determine whether value can be encoded as a Thumb2 modified
 * immediate.  If not, return -1.  If so, return i:imm3:a:bcdefgh form.
 */
static int modifiedImmediate(u4 value)
{
   int zLeading;
   int zTrailing;
   u4 b0 = value & 0xff;

   /* Note: case of value==0 must use 0:000:0:0000000 encoding */
   if (value <= 0xFF)
       return b0;  // 0:000:a:bcdefgh
   if (value == ((b0 << 16) | b0))
       return (0x1 << 8) | b0; /* 0:001:a:bcdefgh */
   if (value == ((b0 << 24) | (b0 << 16) | (b0 << 8) | b0))
       return (0x3 << 8) | b0; /* 0:011:a:bcdefgh */
   b0 = (value >> 8) & 0xff;
   if (value == ((b0 << 24) | (b0 << 8)))
       return (0x2 << 8) | b0; /* 0:010:a:bcdefgh */
   /* Can we do it with rotation? */
   zLeading = leadingZeros(value);
   zTrailing = 32 - leadingZeros(~value & (value - 1));
   /* A run of eight or fewer active bits? */
   if ((zLeading + zTrailing) < 24)
       return -1;  /* No - bail */
   /* left-justify the constant, discarding msb (known to be 1) */
   value <<= zLeading + 1;
   /* Create bcdefgh */
   value >>= 25;
   /* Put it all together */
   return value | ((0x8 + zLeading) << 7); /* [01000..11111]:bcdefgh */
}

/*
 * Load a immediate using a shortcut if possible; otherwise
 * grab from the per-translation literal pool
 */
static ArmLIR *loadConstant(CompilationUnit *cUnit, int rDest, int value)
{
    ArmLIR *res;
    int modImm;
    /* See if the value can be constructed cheaply */
    if ((value >= 0) && (value <= 255)) {
        return newLIR2(cUnit, THUMB_MOV_IMM, rDest, value);
    }
    /* Check Modified immediate special cases */
    modImm = modifiedImmediate(value);
    if (modImm >= 0) {
        res = newLIR2(cUnit, THUMB2_MOV_IMM_SHIFT, rDest, modImm);
        return res;
    }
    modImm = modifiedImmediate(~value);
    if (modImm >= 0) {
        res = newLIR2(cUnit, THUMB2_MVN_IMM_SHIFT, rDest, modImm);
        return res;
    }
    /* 16-bit immediate? */
    if ((value & 0xffff) == value) {
        res = newLIR2(cUnit, THUMB2_MOV_IMM16, rDest, value);
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
    newLIR3(cUnit, THUMB2_STR_RRI8_PREDEC, rDPC, rFP,
            sizeof(StackSaveArea) - offset);
    return res;
}

/* Load value from base + scaled index. Note: index reg killed */
static ArmLIR *loadBaseIndexed(CompilationUnit *cUnit, int rBase,
                               int rIndex, int rDest, int scale, OpSize size)
{
    bool allLowRegs = LOWREG(rBase) && LOWREG(rIndex) && LOWREG(rDest);
    ArmOpCode opCode = THUMB_BKPT;
    bool thumbForm = (allLowRegs && (scale == 0));
    switch (size) {
        case WORD:
            opCode = (thumbForm) ? THUMB_LDR_RRR : THUMB2_LDR_RRR;
            break;
        case UNSIGNED_HALF:
            opCode = (thumbForm) ? THUMB_LDRH_RRR : THUMB2_LDRH_RRR;
            break;
        case SIGNED_HALF:
            opCode = (thumbForm) ? THUMB_LDRSH_RRR : THUMB2_LDRSH_RRR;
            break;
        case UNSIGNED_BYTE:
            opCode = (thumbForm) ? THUMB_LDRB_RRR : THUMB2_LDRB_RRR;
            break;
        case SIGNED_BYTE:
            opCode = (thumbForm) ? THUMB_LDRSB_RRR : THUMB2_LDRSB_RRR;
            break;
        default:
            assert(0);
    }
    if (thumbForm)
        return newLIR3(cUnit, opCode, rDest, rBase, rIndex);
    else
        return newLIR4(cUnit, opCode, rDest, rBase, rIndex, scale);
}

/* store value base base + scaled index. Note: index reg killed */
static ArmLIR *storeBaseIndexed(CompilationUnit *cUnit, int rBase,
                                int rIndex, int rSrc, int scale, OpSize size)
{
    bool allLowRegs = LOWREG(rBase) && LOWREG(rIndex) && LOWREG(rSrc);
    ArmOpCode opCode = THUMB_BKPT;
    bool thumbForm = (allLowRegs && (scale == 0));
    switch (size) {
        case WORD:
            opCode = (thumbForm) ? THUMB_STR_RRR : THUMB2_STR_RRR;
            break;
        case UNSIGNED_HALF:
        case SIGNED_HALF:
            opCode = (thumbForm) ? THUMB_STRH_RRR : THUMB2_STRH_RRR;
            break;
        case UNSIGNED_BYTE:
        case SIGNED_BYTE:
            opCode = (thumbForm) ? THUMB_STRB_RRR : THUMB2_STRB_RRR;
            break;
        default:
            assert(0);
    }
    if (thumbForm)
        return newLIR3(cUnit, opCode, rSrc, rBase, rIndex);
    else
        return newLIR4(cUnit, opCode, rSrc, rBase, rIndex, scale);
}

/*
 * Load a float from a Dalvik register.  Note: using fixed r7 here
 * when operation is out of range.  Revisit this when registor allocation
 * strategy changes.
 */
static ArmLIR *fpVarAccess(CompilationUnit *cUnit, int vSrcDest,
                           int rSrcDest, ArmOpCode opCode)
{
    ArmLIR *res;
    if (vSrcDest > 255) {
        opRegRegImm(cUnit, OP_ADD, r7, rFP, vSrcDest * 4, rNone);
        res = newLIR3(cUnit, opCode, rSrcDest, r7, 0);
    } else {
        res = newLIR3(cUnit, opCode, rSrcDest, rFP, vSrcDest);
    }
    return res;
}

static int nextFPReg(CompilationUnit *cUnit, int dalvikReg, bool isDouble)
{
    RegisterScoreboard *registerScoreboard = &cUnit->registerScoreboard;
    int reg;

    if (isDouble) {
        reg = ((registerScoreboard->nextFP + 1) & ~1) % 32;
        registerScoreboard->nextFP = reg + 2;
        registerScoreboard->nextFP %= 32;
        registerScoreboard->fp[reg] = dalvikReg;
        return dr0 + reg;
    }
    else {
        reg = registerScoreboard->nextFP++;
        registerScoreboard->nextFP %= 32;
        registerScoreboard->fp[reg] = dalvikReg;
        return fr0 + reg;
    }
}

/*
 * Select a SFP register for the dalvikReg
 */
static int selectSFPReg(CompilationUnit *cUnit, int dalvikReg)
{
    RegisterScoreboard *registerScoreboard = &cUnit->registerScoreboard;
    int i;

    if (dalvikReg == vNone) {
        return nextFPReg(cUnit, dalvikReg, false);;
    }

    for (i = 0; i < 32; i++) {
        if (registerScoreboard->fp[i] == dalvikReg) {
            return fr0 + i;
        }
    }
    return nextFPReg(cUnit, dalvikReg, false);;
}

/*
 * Select a DFP register for the dalvikReg
 */
static int selectDFPReg(CompilationUnit *cUnit, int dalvikReg)
{
    RegisterScoreboard *registerScoreboard = &cUnit->registerScoreboard;
    int i;

    if (dalvikReg == vNone) {
        return nextFPReg(cUnit, dalvikReg, true);;
    }

    for (i = 0; i < 32; i += 2) {
        if (registerScoreboard->fp[i] == dalvikReg) {
            return dr0 + i;
        }
    }
    return nextFPReg(cUnit, dalvikReg, true);
}

static ArmLIR *loadFloat(CompilationUnit *cUnit, int vSrc, int rDest)
{
    assert(SINGLEREG(rDest));
    ArmLIR *lir = fpVarAccess(cUnit, vSrc, rDest, THUMB2_VLDRS);
    annotateDalvikRegAccess(lir, vSrc, true /* isLoad */);
    return lir;
}

/* Store a float to a Dalvik register */
static ArmLIR *storeFloat(CompilationUnit *cUnit, int rSrc, int vDest)
{
    RegisterScoreboard *registerScoreboard = &cUnit->registerScoreboard;

    assert(SINGLEREG(rSrc));
    registerScoreboard->fp[rSrc % 32] = vDest;

    ArmLIR *lir = fpVarAccess(cUnit, vDest, rSrc, THUMB2_VSTRS);
    annotateDalvikRegAccess(lir, vDest, false /* isLoad */);
    return lir;
}

/* Load a double from a Dalvik register */
static ArmLIR *loadDouble(CompilationUnit *cUnit, int vSrc, int rDest)
{
    assert(DOUBLEREG(rDest));
    ArmLIR *lir = fpVarAccess(cUnit, vSrc, rDest, THUMB2_VLDRD);
    annotateDalvikRegAccess(lir, vSrc, true /* isLoad */);
    return lir;
}

/* Store a double to a Dalvik register */
static ArmLIR *storeDouble(CompilationUnit *cUnit, int rSrc, int vDest)
{
    RegisterScoreboard *registerScoreboard = &cUnit->registerScoreboard;

    assert(DOUBLEREG(rSrc));
    registerScoreboard->fp[rSrc % 32] = vDest;

    ArmLIR *lir = fpVarAccess(cUnit, vDest, rSrc, THUMB2_VSTRD);
    annotateDalvikRegAccess(lir, vDest, false /* isLoad */);
    return lir;
}

/*
 * Load value from base + displacement.  Optionally perform null check
 * on base (which must have an associated vReg and MIR).  If not
 * performing null check, incoming MIR can be null. Note: base and
 * dest must not be the same if there is any chance that the long
 * form must be used.
 * TODO: revisit, perhaps use hot temp reg in (base == dest) case.
 */
static ArmLIR *loadBaseDisp(CompilationUnit *cUnit, MIR *mir, int rBase,
                            int displacement, int rDest, OpSize size,
                            bool nullCheck, int vReg)
{
    ArmLIR *first = NULL;
    ArmLIR *res, *load;
    ArmOpCode opCode = THUMB_BKPT;
    bool shortForm = false;
    bool thumb2Form = (displacement < 4092 && displacement >= 0);
    int shortMax = 128;
    bool allLowRegs = (LOWREG(rBase) && LOWREG(rDest));
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
            } else if (allLowRegs && displacement < 128 && displacement >= 0) {
                assert((displacement & 0x3) == 0);
                shortForm = true;
                encodedDisp >>= 2;
                opCode = THUMB_LDR_RRI5;
            } else if (thumb2Form) {
                shortForm = true;
                opCode = THUMB2_LDR_RRI12;
            }
            break;
        case UNSIGNED_HALF:
            if (allLowRegs && displacement < 64 && displacement >= 0) {
                assert((displacement & 0x1) == 0);
                shortForm = true;
                encodedDisp >>= 1;
                opCode = THUMB_LDRH_RRI5;
            } else if (displacement < 4092 && displacement >= 0) {
                shortForm = true;
                opCode = THUMB2_LDRH_RRI12;
            }
            break;
        case SIGNED_HALF:
            if (thumb2Form) {
                shortForm = true;
                opCode = THUMB2_LDRSH_RRI12;
            }
            break;
        case UNSIGNED_BYTE:
            if (allLowRegs && displacement < 32 && displacement >= 0) {
                shortForm = true;
                opCode = THUMB_LDRB_RRI5;
            } else if (thumb2Form) {
                shortForm = true;
                opCode = THUMB2_LDRB_RRI12;
            }
            break;
        case SIGNED_BYTE:
            if (thumb2Form) {
                shortForm = true;
                opCode = THUMB2_LDRSB_RRI12;
            }
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
        load = loadBaseIndexed(cUnit, rBase, rDest, rDest, 0, size);
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
    bool thumb2Form = (displacement < 4092 && displacement >= 0);
    int shortMax = 128;
    bool allLowRegs = (LOWREG(rBase) && LOWREG(rSrc));
    int encodedDisp = displacement;

    if (rScratch != -1)
        allLowRegs &= LOWREG(rScratch);
    switch (size) {
        case WORD:
            if (allLowRegs && displacement < 128 && displacement >= 0) {
                assert((displacement & 0x3) == 0);
                shortForm = true;
                encodedDisp >>= 2;
                opCode = THUMB_STR_RRI5;
            } else if (thumb2Form) {
                shortForm = true;
                opCode = THUMB2_STR_RRI12;
            }
            break;
        case UNSIGNED_HALF:
        case SIGNED_HALF:
            if (displacement < 64 && displacement >= 0) {
                assert((displacement & 0x1) == 0);
                shortForm = true;
                encodedDisp >>= 1;
                opCode = THUMB_STRH_RRI5;
            } else if (thumb2Form) {
                shortForm = true;
                opCode = THUMB2_STRH_RRI12;
            }
            break;
        case UNSIGNED_BYTE:
        case SIGNED_BYTE:
            if (displacement < 32 && displacement >= 0) {
                shortForm = true;
                opCode = THUMB_STRB_RRI5;
            } else if (thumb2Form) {
                shortForm = true;
                opCode = THUMB2_STRH_RRI12;
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
        store = storeBaseIndexed(cUnit, rBase, rScratch, rSrc, 0, size);
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
static ArmLIR *genRegImmCheck(CompilationUnit *cUnit,
                                         ArmConditionCode cond, int reg,
                                         int checkValue, int dOffset,
                                         ArmLIR *pcrLabel)
{
    ArmLIR *branch;
    int modImm;
    /*
     * TODO: re-enable usage of THUMB2_CBZ & THUMB2_CBNZ once assembler is
     * enhanced to allow us to replace code patterns when instructions don't
     * reach.  Currently, CB[N]Z is causing too many assembler aborts.
     * What we want to do is emit the short forms, and then replace them with
     * longer versions when needed.
     */
    if (0 && (LOWREG(reg)) && (checkValue == 0) &&
       ((cond == ARM_COND_EQ) || (cond == ARM_COND_NE))) {
        branch = newLIR2(cUnit,
                         (cond == ARM_COND_EQ) ? THUMB2_CBZ : THUMB2_CBNZ,
                         reg, 0);
    } else {
        modImm = modifiedImmediate(checkValue);
        if ((checkValue & 0xff) == checkValue) {
            newLIR2(cUnit, THUMB_CMP_RI8, reg, checkValue);
        } else if (modImm >= 0) {
            newLIR2(cUnit, THUMB2_CMP_RI8, reg, modImm);
        } else {
            /* Note: direct use of hot temp r7 here. Revisit. */
            loadConstant(cUnit, r7, checkValue);
            newLIR2(cUnit, THUMB_CMP_RR, reg, r7);
        }
        branch = newLIR2(cUnit, THUMB_B_COND, 0, cond);
    }
    return genCheckCommon(cUnit, dOffset, branch, pcrLabel);
}

static ArmLIR *loadMultiple(CompilationUnit *cUnit, int rBase, int rMask)
{
    ArmLIR *res;
    if (LOWREG(rBase) && ((rMask & 0xff)==rMask)) {
        res = newLIR2(cUnit, THUMB_LDMIA, rBase, rMask);
    } else {
        res = newLIR2(cUnit, THUMB2_LDMIA, rBase, rMask);
    }
    return res;
}

static ArmLIR *storeMultiple(CompilationUnit *cUnit, int rBase, int rMask)
{
    ArmLIR *res;
    if (LOWREG(rBase) && ((rMask & 0xff)==rMask)) {
        res = newLIR2(cUnit, THUMB_STMIA, rBase, rMask);
    } else {
        res = newLIR2(cUnit, THUMB2_STMIA, rBase, rMask);
    }
    return res;
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

static ArmLIR *opCondBranch(CompilationUnit *cUnit, ArmConditionCode cc)
{
    return newLIR2(cUnit, THUMB_B_COND, 0 /* offset to be patched */, cc);
}

static ArmLIR *opImm(CompilationUnit *cUnit, OpKind op, int value)
{
    ArmOpCode opCode = THUMB_BKPT;
    switch (op) {
        case OP_PUSH:
            opCode = ((value & 0xff00) != 0) ? THUMB2_PUSH : THUMB_PUSH;
            break;
        case OP_POP:
            opCode = ((value & 0xff00) != 0) ? THUMB2_POP : THUMB_POP;
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

static ArmLIR *opRegRegShift(CompilationUnit *cUnit, OpKind op, int rDestSrc1,
                        int rSrc2, int shift)
{
    bool thumbForm = ((shift == 0) && LOWREG(rDestSrc1) && LOWREG(rSrc2));
    ArmOpCode opCode = THUMB_BKPT;
    switch (op) {
        case OP_ADC:
            opCode = (thumbForm) ? THUMB_ADC_RR : THUMB2_ADC_RRR;
            break;
        case OP_AND:
            opCode = (thumbForm) ? THUMB_AND_RR : THUMB2_AND_RRR;
            break;
        case OP_BIC:
            opCode = (thumbForm) ? THUMB_BIC_RR : THUMB2_BIC_RRR;
            break;
        case OP_CMN:
            assert(shift == 0);
            opCode = (thumbForm) ? THUMB_CMN_RR : THUMB2_CMN_RR;
            break;
        case OP_CMP:
            if (thumbForm)
                opCode = THUMB_CMP_RR;
            else if ((shift == 0) && !LOWREG(rDestSrc1) && !LOWREG(rSrc2))
                opCode = THUMB_CMP_HH;
            else if ((shift == 0) && LOWREG(rDestSrc1))
                opCode = THUMB_CMP_LH;
            else if (shift == 0)
                opCode = THUMB_CMP_HL;
            else
                opCode = THUMB2_CMP_RR;
            break;
        case OP_XOR:
            opCode = (thumbForm) ? THUMB_EOR_RR : THUMB2_EOR_RRR;
            break;
        case OP_MOV:
            assert(shift == 0);
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
            assert(shift == 0);
            opCode = (thumbForm) ? THUMB_MUL : THUMB2_MUL_RRR;
            break;
        case OP_MVN:
            opCode = (thumbForm) ? THUMB_MVN : THUMB2_MVN_RR;
            break;
        case OP_NEG:
            assert(shift == 0);
            opCode = (thumbForm) ? THUMB_NEG : THUMB2_NEG_RR;
            break;
        case OP_OR:
            opCode = (thumbForm) ? THUMB_ORR : THUMB2_ORR_RRR;
            break;
        case OP_SBC:
            opCode = (thumbForm) ? THUMB_SBC : THUMB2_SBC_RRR;
            break;
        case OP_TST:
            opCode = (thumbForm) ? THUMB_TST : THUMB2_TST_RR;
            break;
        case OP_LSL:
            assert(shift == 0);
            opCode = (thumbForm) ? THUMB_LSL_RR : THUMB2_LSL_RRR;
            break;
        case OP_LSR:
            assert(shift == 0);
            opCode = (thumbForm) ? THUMB_LSR_RR : THUMB2_LSR_RRR;
            break;
        case OP_ASR:
            assert(shift == 0);
            opCode = (thumbForm) ? THUMB_ASR_RR : THUMB2_ASR_RRR;
            break;
        case OP_ROR:
            assert(shift == 0);
            opCode = (thumbForm) ? THUMB_ROR_RR : THUMB2_ROR_RRR;
            break;
        case OP_ADD:
            opCode = (thumbForm) ? THUMB_ADD_RRR : THUMB2_ADD_RRR;
            break;
        case OP_SUB:
            opCode = (thumbForm) ? THUMB_SUB_RRR : THUMB2_SUB_RRR;
            break;
        case OP_2BYTE:
            assert(shift == 0);
            return newLIR4(cUnit, THUMB2_SBFX, rDestSrc1, rSrc2, 0, 8);
        case OP_2SHORT:
            assert(shift == 0);
            return newLIR4(cUnit, THUMB2_SBFX, rDestSrc1, rSrc2, 0, 16);
        case OP_2CHAR:
            assert(shift == 0);
            return newLIR4(cUnit, THUMB2_UBFX, rDestSrc1, rSrc2, 0, 16);
        default:
            assert(0);
            break;
    }
    assert(opCode >= 0);
    if (EncodingMap[opCode].flags & IS_BINARY_OP)
        return newLIR2(cUnit, opCode, rDestSrc1, rSrc2);
    else if (EncodingMap[opCode].flags & IS_TERTIARY_OP) {
        if (EncodingMap[opCode].fieldLoc[2].kind == SHIFT)
            return newLIR3(cUnit, opCode, rDestSrc1, rSrc2, shift);
        else
            return newLIR3(cUnit, opCode, rDestSrc1, rDestSrc1, rSrc2);
    } else if (EncodingMap[opCode].flags & IS_QUAD_OP)
        return newLIR4(cUnit, opCode, rDestSrc1, rDestSrc1, rSrc2, shift);
    else {
        assert(0);
        return NULL;
    }
}

static ArmLIR *opRegReg(CompilationUnit *cUnit, OpKind op, int rDestSrc1,
                        int rSrc2)
{
    return opRegRegShift(cUnit, op, rDestSrc1, rSrc2, 0);
}

/* Handle Thumb-only variants here - otherwise punt to opRegRegImm */
static ArmLIR *opRegImm(CompilationUnit *cUnit, OpKind op, int rDestSrc1,
                        int value, int rScratch)
{
    ArmLIR *res;
    bool neg = (value < 0);
    int absValue = (neg) ? -value : value;
    bool shortForm = (((absValue & 0xff) == absValue) && LOWREG(rDestSrc1));
    ArmOpCode opCode = THUMB_BKPT;
    switch (op) {
        case OP_ADD:
            if ( !neg && (rDestSrc1 == 13) && (value <= 508)) { /* sp */
                assert((value & 0x3) == 0);
                return newLIR1(cUnit, THUMB_ADD_SPI7, value >> 2);
            } else if (shortForm) {
                opCode = (neg) ? THUMB_SUB_RI8 : THUMB_ADD_RI8;
            }
            break;
        case OP_SUB:
            if (!neg && (rDestSrc1 == 13) && (value <= 508)) { /* sp */
                assert((value & 0x3) == 0);
                return newLIR1(cUnit, THUMB_SUB_SPI7, value >> 2);
            } else if (shortForm) {
                opCode = (neg) ? THUMB_ADD_RI8 : THUMB_SUB_RI8;
            }
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
            /* Punt to opRegRegImm - if bad case catch it there */
            shortForm = false;
            break;
    }
    if (shortForm)
        return newLIR2(cUnit, opCode, rDestSrc1, absValue);
    else
        return opRegRegImm(cUnit, op, rDestSrc1, rDestSrc1, value, rScratch);
}

static ArmLIR *opRegRegRegShift(CompilationUnit *cUnit, OpKind op,
                                int rDest, int rSrc1, int rSrc2, int shift)
{
    ArmOpCode opCode = THUMB_BKPT;
    bool thumbForm = (shift == 0) && LOWREG(rDest) && LOWREG(rSrc1) &&
                      LOWREG(rSrc2);
    switch (op) {
        case OP_ADD:
            opCode = (thumbForm) ? THUMB_ADD_RRR : THUMB2_ADD_RRR;
            break;
        case OP_SUB:
            opCode = (thumbForm) ? THUMB_SUB_RRR : THUMB2_SUB_RRR;
            break;
        case OP_ADC:
            opCode = THUMB2_ADC_RRR;
            break;
        case OP_AND:
            opCode = THUMB2_AND_RRR;
            break;
        case OP_BIC:
            opCode = THUMB2_BIC_RRR;
            break;
        case OP_XOR:
            opCode = THUMB2_EOR_RRR;
            break;
        case OP_MUL:
            assert(shift == 0);
            opCode = THUMB2_MUL_RRR;
            break;
        case OP_OR:
            opCode = THUMB2_ORR_RRR;
            break;
        case OP_SBC:
            opCode = THUMB2_SBC_RRR;
            break;
        case OP_LSL:
            assert(shift == 0);
            opCode = THUMB2_LSL_RRR;
            break;
        case OP_LSR:
            assert(shift == 0);
            opCode = THUMB2_LSR_RRR;
            break;
        case OP_ASR:
            assert(shift == 0);
            opCode = THUMB2_ASR_RRR;
            break;
        case OP_ROR:
            assert(shift == 0);
            opCode = THUMB2_ROR_RRR;
            break;
        default:
            assert(0);
            break;
    }
    assert(opCode >= 0);
    if (EncodingMap[opCode].flags & IS_QUAD_OP)
        return newLIR4(cUnit, opCode, rDest, rSrc1, rSrc2, shift);
    else {
        assert(EncodingMap[opCode].flags & IS_TERTIARY_OP);
        return newLIR3(cUnit, opCode, rDest, rSrc1, rSrc2);
    }
}

static ArmLIR *opRegRegReg(CompilationUnit *cUnit, OpKind op, int rDest,
                           int rSrc1, int rSrc2)
{
    return opRegRegRegShift(cUnit, op, rDest, rSrc1, rSrc2, 0);
}

static ArmLIR *opRegRegImm(CompilationUnit *cUnit, OpKind op, int rDest,
                           int rSrc1, int value, int rScratch)
{
    ArmLIR *res;
    bool neg = (value < 0);
    int absValue = (neg) ? -value : value;
    ArmOpCode opCode = THUMB_BKPT;
    ArmOpCode altOpCode = THUMB_BKPT;
    bool allLowRegs = (LOWREG(rDest) && LOWREG(rSrc1));
    int modImm = modifiedImmediate(value);
    int modImmNeg = modifiedImmediate(-value);

    switch(op) {
        case OP_LSL:
            if (allLowRegs)
                return newLIR3(cUnit, THUMB_LSL_RRI5, rDest, rSrc1, value);
            else
                return newLIR3(cUnit, THUMB2_LSL_RRI5, rDest, rSrc1, value);
        case OP_LSR:
            if (allLowRegs)
                return newLIR3(cUnit, THUMB_LSR_RRI5, rDest, rSrc1, value);
            else
                return newLIR3(cUnit, THUMB2_LSR_RRI5, rDest, rSrc1, value);
        case OP_ASR:
            if (allLowRegs)
                return newLIR3(cUnit, THUMB_ASR_RRI5, rDest, rSrc1, value);
            else
                return newLIR3(cUnit, THUMB2_ASR_RRI5, rDest, rSrc1, value);
        case OP_ROR:
            return newLIR3(cUnit, THUMB2_ROR_RRI5, rDest, rSrc1, value);
        case OP_ADD:
            if (LOWREG(rDest) && (rSrc1 == 13) &&
                (value <= 1020) && ((value & 0x3)==0)) {
                return newLIR3(cUnit, THUMB_ADD_SP_REL, rDest, rSrc1,
                               value >> 2);
            } else if (LOWREG(rDest) && (rSrc1 == rpc) &&
                       (value <= 1020) && ((value & 0x3)==0)) {
                return newLIR3(cUnit, THUMB_ADD_PC_REL, rDest, rSrc1,
                               value >> 2);
            }
            opCode = THUMB2_ADD_RRI8;
            altOpCode = THUMB2_ADD_RRR;
            // Note: intentional fallthrough
        case OP_SUB:
            if (allLowRegs && ((absValue & 0x7) == absValue)) {
                if (op == OP_ADD)
                    opCode = (neg) ? THUMB_SUB_RRI3 : THUMB_ADD_RRI3;
                else
                    opCode = (neg) ? THUMB_ADD_RRI3 : THUMB_SUB_RRI3;
                return newLIR3(cUnit, opCode, rDest, rSrc1, absValue);
            } else if ((absValue & 0xff) == absValue) {
                if (op == OP_ADD)
                    opCode = (neg) ? THUMB2_SUB_RRI12 : THUMB2_ADD_RRI12;
                else
                    opCode = (neg) ? THUMB2_ADD_RRI12 : THUMB2_SUB_RRI12;
                return newLIR3(cUnit, opCode, rDest, rSrc1, absValue);
            }
            if (modImmNeg >= 0) {
                op = (op == OP_ADD) ? OP_SUB : OP_ADD;
                modImm = modImmNeg;
            }
            if (op == OP_SUB) {
                opCode = THUMB2_SUB_RRI8;
                altOpCode = THUMB2_SUB_RRR;
            }
            break;
        case OP_ADC:
            opCode = THUMB2_ADC_RRI8;
            altOpCode = THUMB2_ADC_RRR;
            break;
        case OP_SBC:
            opCode = THUMB2_SBC_RRI8;
            altOpCode = THUMB2_SBC_RRR;
            break;
        case OP_OR:
            opCode = THUMB2_ORR_RRI8;
            altOpCode = THUMB2_ORR_RRR;
            break;
        case OP_AND:
            opCode = THUMB2_AND_RRI8;
            altOpCode = THUMB2_AND_RRR;
            break;
        case OP_XOR:
            opCode = THUMB2_EOR_RRI8;
            altOpCode = THUMB2_EOR_RRR;
            break;
        case OP_MUL:
            //TUNING: power of 2, shift & add
            modImm = -1;
            altOpCode = THUMB2_MUL_RRR;
            break;
        default:
            assert(0);
    }

    if (modImm >= 0) {
        return newLIR3(cUnit, opCode, rDest, rSrc1, modImm);
    } else {
        loadConstant(cUnit, rScratch, value);
        if (EncodingMap[altOpCode].flags & IS_QUAD_OP)
            return newLIR4(cUnit, altOpCode, rDest, rSrc1, rScratch, 0);
        else
            return newLIR3(cUnit, altOpCode, rDest, rSrc1, rScratch);
    }
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
                               int vDest, int vSrc1, int vSrc2)
{
    int op1lo = selectFirstRegister(cUnit, vSrc1, true);
    int op1hi = NEXT_REG(op1lo);
    int op2lo = NEXT_REG(op1hi);
    int op2hi = NEXT_REG(op2lo);
    loadValuePair(cUnit, vSrc1, op1lo, op1hi);
    loadValuePair(cUnit, vSrc2, op2lo, op2hi);
    /* Note: using hardcoded r7 & r4PC for now.  revisit */
    loadConstant(cUnit, r7, -1);
    opRegReg(cUnit, OP_CMP, op1hi, op2hi);
    ArmLIR *branch1 = opCondBranch(cUnit, ARM_COND_LT);
    ArmLIR *branch2 = opCondBranch(cUnit, ARM_COND_GT);
    opRegRegReg(cUnit, OP_SUB, r7, op1lo, op2lo);
    ArmLIR *branch3 = opCondBranch(cUnit, ARM_COND_EQ);

    // TODO: need assert mechanism to verify IT block size
    branch1->generic.target = (LIR *) genIT(cUnit, ARM_COND_HI, "E");
    newLIR2(cUnit, THUMB2_MOV_IMM_SHIFT, r7, modifiedImmediate(-1));
    newLIR2(cUnit, THUMB_MOV_IMM, r7, 1);
    genBarrier(cUnit);

    branch2->generic.target = (LIR *) opRegReg(cUnit, OP_NEG, r7, r7);
    branch1->generic.target = (LIR *) storeValue(cUnit, r7, vDest, r4PC);
    branch3->generic.target = branch1->generic.target;
}

static bool genInlinedStringLength(CompilationUnit *cUnit, MIR *mir)
{
    DecodedInstruction *dInsn = &mir->dalvikInsn;
    int offset = offsetof(InterpState, retval);
    int regObj = selectFirstRegister(cUnit, dInsn->arg[0], false);
    int reg1 = NEXT_REG(regObj);
    int vDest = inlinedTarget(mir);
    loadValue(cUnit, dInsn->arg[0], regObj);
    genNullCheck(cUnit, dInsn->arg[0], regObj, mir->offset, NULL);
    loadWordDisp(cUnit, regObj, gDvm.offJavaLangString_count, reg1);
    if (vDest >= 0)
        storeValue(cUnit, reg1, vDest, regObj);
    else
        storeWordDisp(cUnit, rGLUE, offset, reg1, rNone);
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
    int vDest = inlinedTarget(mir);
    loadValue(cUnit, dInsn->arg[0], regObj);
    loadValue(cUnit, dInsn->arg[1], regIdx);
    ArmLIR * pcrLabel = genNullCheck(cUnit, dInsn->arg[0], regObj,
                                         mir->offset, NULL);
    loadWordDisp(cUnit, regObj, gDvm.offJavaLangString_count, regMax);
    loadWordDisp(cUnit, regObj, gDvm.offJavaLangString_offset, regOff);
    loadWordDisp(cUnit, regObj, gDvm.offJavaLangString_value, regObj);
    genBoundsCheck(cUnit, regIdx, regMax, mir->offset, pcrLabel);
    opRegImm(cUnit, OP_ADD, regObj, contents, rNone);
    opRegReg(cUnit, OP_ADD, regIdx, regOff);
    loadBaseIndexed(cUnit, regObj, regIdx, regMax, 1, UNSIGNED_HALF);
    if (vDest >= 0)
        storeValue(cUnit, regMax, vDest, regObj);
    else
        storeWordDisp(cUnit, rGLUE, offset, regMax, rNone);
    return false;
}

static bool genInlinedAbsInt(CompilationUnit *cUnit, MIR *mir)
{
    int offset = offsetof(InterpState, retval);
    DecodedInstruction *dInsn = &mir->dalvikInsn;
    int reg0 = selectFirstRegister(cUnit, dInsn->arg[0], false);
    int sign = NEXT_REG(reg0);
    int vDest = inlinedTarget(mir);
    /* abs(x) = y<=x>>31, (x+y)^y.  */
    loadValue(cUnit, dInsn->arg[0], reg0);
    /*
     * Thumb2's IT block also yields 3 instructions, but imposes
     * scheduling constraints.
     */
    opRegRegImm(cUnit, OP_ASR, sign, reg0, 31, rNone);
    opRegReg(cUnit, OP_ADD, reg0, sign);
    opRegReg(cUnit, OP_XOR, reg0, sign);
    if (vDest >= 0)
        storeValue(cUnit, reg0, vDest, sign);
    else
        storeWordDisp(cUnit, rGLUE, offset, reg0, rNone);
    return false;
}

static bool genInlinedAbsFloat(CompilationUnit *cUnit, MIR *mir)
{
    int offset = offsetof(InterpState, retval);
    DecodedInstruction *dInsn = &mir->dalvikInsn;
    int reg0 = selectFirstRegister(cUnit, dInsn->arg[0], false);
    int signMask = NEXT_REG(reg0);
    int vDest = inlinedTarget(mir);
    // TUNING: handle case of src already in FP reg
    loadValue(cUnit, dInsn->arg[0], reg0);
    loadConstant(cUnit, signMask, 0x7fffffff);
    newLIR2(cUnit, THUMB_AND_RR, reg0, signMask);
    if (vDest >= 0)
        storeValue(cUnit, reg0, vDest, signMask);
    else
        storeWordDisp(cUnit, rGLUE, offset, reg0, rNone);
    return false;
}

static bool genInlinedAbsDouble(CompilationUnit *cUnit, MIR *mir)
{
    int offset = offsetof(InterpState, retval);
    DecodedInstruction *dInsn = &mir->dalvikInsn;
    int oplo = selectFirstRegister(cUnit, dInsn->arg[0], true);
    int ophi = NEXT_REG(oplo);
    int signMask = NEXT_REG(ophi);
    int vSrc = dInsn->arg[0];
    int vDest = inlinedTarget(mir);
    // TUNING: handle case of src already in FP reg
    if (vDest >= 0) {
        /*
         * FIXME: disable this case to to work around bug until after
         * new schedule/ralloc mechanisms are done.
         */
        if (0 && (vDest == vSrc)) {
            loadValue(cUnit, vSrc+1, ophi);
            opRegRegImm(cUnit, OP_AND, ophi, ophi, 0x7fffffff, signMask);
            storeValue(cUnit, ophi, vDest + 1, signMask);
        } else {
            loadValuePair(cUnit, dInsn->arg[0], oplo, ophi);
            opRegRegImm(cUnit, OP_AND, ophi, ophi, 0x7fffffff, signMask);
            storeValuePair(cUnit, oplo, ophi, vDest, signMask);
        }
    } else {
        loadValuePair(cUnit, dInsn->arg[0], oplo, ophi);
        loadConstant(cUnit, signMask, 0x7fffffff);
        storeWordDisp(cUnit, rGLUE, offset, oplo, rNone);
        opRegReg(cUnit, OP_AND, ophi, signMask);
        storeWordDisp(cUnit, rGLUE, offset + 4, ophi, rNone);
    }
    return false;
}

static bool genInlinedMinMaxInt(CompilationUnit *cUnit, MIR *mir, bool isMin)
{
    int offset = offsetof(InterpState, retval);
    DecodedInstruction *dInsn = &mir->dalvikInsn;
    int reg0 = selectFirstRegister(cUnit, dInsn->arg[0], false);
    int reg1 = NEXT_REG(reg0);
    int vDest = inlinedTarget(mir);
    loadValue(cUnit, dInsn->arg[0], reg0);
    loadValue(cUnit, dInsn->arg[1], reg1);
    opRegReg(cUnit, OP_CMP, reg0, reg1);
    //TODO: need assertion mechanism to validate IT region size
    genIT(cUnit, (isMin) ? ARM_COND_GT : ARM_COND_LT, "");
    opRegReg(cUnit, OP_MOV, reg0, reg1);
    genBarrier(cUnit);
    if (vDest >= 0)
        storeValue(cUnit, reg0, vDest, reg1);
    else
        storeWordDisp(cUnit, rGLUE, offset, reg0, rNone);
    return false;
}

static bool genInlinedAbsLong(CompilationUnit *cUnit, MIR *mir)
{
    int offset = offsetof(InterpState, retval);
    DecodedInstruction *dInsn = &mir->dalvikInsn;
    int oplo = selectFirstRegister(cUnit, dInsn->arg[0], true);
    int ophi = NEXT_REG(oplo);
    int sign = NEXT_REG(ophi);
    int vDest = inlinedTarget(mir);
    /* abs(x) = y<=x>>31, (x+y)^y. */
    loadValuePair(cUnit, dInsn->arg[0], oplo, ophi);
    /*
     * Thumb2 IT block allows slightly shorter sequence,
     * but introduces a scheduling barrier.  Stick with this
     * mechanism for now.
     */
    opRegRegImm(cUnit, OP_ASR, sign, ophi, 31, rNone);
    opRegReg(cUnit, OP_ADD, oplo, sign);
    opRegReg(cUnit, OP_ADC, ophi, sign);
    opRegReg(cUnit, OP_XOR, oplo, sign);
    opRegReg(cUnit, OP_XOR, ophi, sign);
    if (vDest >= 0) {
        storeValuePair(cUnit, oplo, ophi, vDest, sign);
    } else {
        storeWordDisp(cUnit, rGLUE, offset, oplo, rNone);
        storeWordDisp(cUnit, rGLUE, offset + 4, ophi, rNone);
    }
    return false;
}
