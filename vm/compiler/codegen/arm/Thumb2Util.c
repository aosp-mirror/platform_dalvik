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
 * includes by:and support common to all supported
 *
 *        Codegen-$(TARGET_ARCH_VARIANT).c
 *
 */

#include "Codegen.h"

/* Routines which must be supplied here */
static void loadConstant(CompilationUnit *cUnit, int rDest, int value);
static void genExportPC(CompilationUnit *cUnit, MIR *mir, int rDPC, int rAddr);
static void genConditionalBranch(CompilationUnit *cUnit,
                                 ArmConditionCode cond,
                                 ArmLIR *target);
static ArmLIR *genUnconditionalBranch(CompilationUnit *cUnit, ArmLIR *target);
static void loadValuePair(CompilationUnit *cUnit, int vSrc, int rDestLo,
                          int rDestHi);
static void storeValuePair(CompilationUnit *cUnit, int rSrcLo, int rSrcHi,
                           int vDest, int rScratch);
static void loadValueAddress(CompilationUnit *cUnit, int vSrc, int vDest);
static void loadValue(CompilationUnit *cUnit, int vSrc, int rDest);
static void loadWordDisp(CompilationUnit *cUnit, int rBase, int displacement,
                         int rDest);
static void storeValue(CompilationUnit *cUnit, int rSrc, int vDest,
                       int rScratch);
static inline ArmLIR *genRegImmCheck(CompilationUnit *cUnit,
                                         ArmConditionCode cond, int reg,
                                         int checkValue, int dOffset,
                                         ArmLIR *pcrLabel);
ArmLIR* dvmCompilerRegCopy(CompilationUnit *cUnit, int rDest, int rSrc);

/*****************************************************************************/

/*
 * Support for register allocation
 */

/* non-existent register */
#define vNone   (-1)

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

/*****************************************************************************/

ArmLIR* dvmCompilerRegCopy(CompilationUnit *cUnit, int rDest, int rSrc)
{
    ArmLIR* res = dvmCompilerNew(sizeof(ArmLIR), true);
    res->operands[0] = rDest;
    res->operands[1] = rSrc;
    if (rDest == rSrc) {
        res->isNop = true;
    } else {
        if (LOWREG(rDest) && LOWREG(rSrc)) {
            res->opCode = THUMB_MOV_RR;
        } else if (FPREG(rDest) && FPREG(rSrc)) {
            if (DOUBLEREG(rDest)) {
                assert(DOUBLEREG(rSrc));
                res->opCode = THUMB2_VMOVD;
            } else {
                assert(SINGLEREG(rSrc));
                res->opCode = THUMB2_VMOVS;
            }
        } else {
            // TODO: support copy between FP and gen regs.
            assert(!FPREG(rDest));
            assert(!FPREG(rSrc));
            res->opCode = THUMB2_MOV_RR;
        }
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
 * Determine whether value can be encoded as a Thumb modified
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
static void loadConstant(CompilationUnit *cUnit, int rDest, int value)
{
    int modImm;
    /* See if the value can be constructed cheaply */
    if ((value & 0xff) == value) {
        newLIR2(cUnit, THUMB_MOV_IMM, rDest, value);
        return;
    } else if ((value & 0xFFFFFF00) == 0xFFFFFF00) {
        newLIR2(cUnit, THUMB_MOV_IMM, rDest, ~value);
        newLIR2(cUnit, THUMB_MVN, rDest, rDest);
        return;
    }
    /* Check Modified immediate special cases */
    modImm = modifiedImmediate(value);
    if (modImm >= 0) {
        newLIR2(cUnit, THUMB2_MOV_IMM_SHIFT, rDest, modImm);
        return;
    }
    /* 16-bit immediate? */
    if ((value & 0xffff) == value) {
        newLIR2(cUnit, THUMB2_MOV_IMM16, rDest, value);
        return;
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
    dvmCompilerAppendLIR(cUnit, (LIR *) loadPcRel);

    /*
     * To save space in the constant pool, we use the ADD_RRI8 instruction to
     * add up to 255 to an existing constant value.
     */
    if (dataTarget->operands[0] != value) {
        newLIR2(cUnit, THUMB_ADD_RI8, rDest, value - dataTarget->operands[0]);
    }
}

/* Export the Dalvik PC assicated with an instruction to the StackSave area */
static void genExportPC(CompilationUnit *cUnit, MIR *mir, int rDPC, int rAddr)
{
    int offset = offsetof(StackSaveArea, xtra.currentPc);
    loadConstant(cUnit, rDPC, (int) (cUnit->method->insns + mir->offset));
    newLIR3(cUnit, THUMB2_STR_RRI8_PREDEC, rDPC, rFP,
            sizeof(StackSaveArea) - offset);
}

/* Generate conditional branch instructions */
static void genConditionalBranch(CompilationUnit *cUnit,
                                 ArmConditionCode cond,
                                 ArmLIR *target)
{
    ArmLIR *branch = newLIR2(cUnit, THUMB_B_COND, 0, cond);
    branch->generic.target = (LIR *) target;
}

/* Generate unconditional branch instructions */
static ArmLIR *genUnconditionalBranch(CompilationUnit *cUnit, ArmLIR *target)
{
    ArmLIR *branch = newLIR0(cUnit, THUMB_B_UNCOND);
    branch->generic.target = (LIR *) target;
    return branch;
}

/*
 * Load a pair of values of rFP[src..src+1] and store them into rDestLo and
 * rDestHi
 */
static void loadValuePair(CompilationUnit *cUnit, int vSrc, int rDestLo,
                          int rDestHi)
{
    bool allLowRegs = (LOWREG(rDestLo) && LOWREG(rDestHi));

    /* Use reg + imm5*4 to load the values if possible */
    if (allLowRegs && vSrc <= 30) {
        newLIR3(cUnit, THUMB_LDR_RRI5, rDestLo, rFP, vSrc);
        newLIR3(cUnit, THUMB_LDR_RRI5, rDestHi, rFP, vSrc+1);
    } else {
        assert(rDestLo < rDestHi);
        loadValueAddress(cUnit, vSrc, rDestLo);
        if (allLowRegs) {
            newLIR2(cUnit, THUMB_LDMIA, rDestLo, (1<<rDestLo) | (1<<(rDestHi)));
        } else {
            assert(0); // Unimp - need Thumb2 ldmia
        }
    }
}

/*
 * Store a pair of values of rSrc and rSrc+1 and store them into vDest and
 * vDest+1
 */
static void storeValuePair(CompilationUnit *cUnit, int rSrcLo, int rSrcHi,
                           int vDest, int rScratch)
{
    bool allLowRegs = (LOWREG(rSrcLo) && LOWREG(rSrcHi));
    killNullCheckedRegister(cUnit, vDest);
    killNullCheckedRegister(cUnit, vDest+1);
    updateLiveRegisterPair(cUnit, vDest, rSrcLo, rSrcHi);

    /* Use reg + imm5*4 to store the values if possible */
    if (allLowRegs && vDest <= 30) {
        newLIR3(cUnit, THUMB_STR_RRI5, rSrcLo, rFP, vDest);
        newLIR3(cUnit, THUMB_STR_RRI5, rSrcHi, rFP, vDest+1);
    } else {
        assert(rSrcLo < rSrcHi);
        loadValueAddress(cUnit, vDest, rScratch);
        if (allLowRegs) {
            newLIR2(cUnit, THUMB_STMIA, rScratch,
                    (1<<rSrcLo) | (1 << (rSrcHi)));
        } else {
            assert(0); // Unimp - need Thumb2 stmia
        }
    }
}

static void addRegisterRegister(CompilationUnit *cUnit, int rDest,
                                int rSrc1, int rSrc2)
{
    if (!LOWREG(rDest) || !LOWREG(rSrc1) || !LOWREG(rSrc2)) {
        assert(0); // Unimp
        //newLIR3(cUnit, THUMB2_ADD_RRR, rDest, rFP, rDest);
    } else {
        newLIR3(cUnit, THUMB_ADD_RRR, rDest, rFP, rDest);
    }
}

/* Add in immediate to a register. */
static void addRegisterImmediate(CompilationUnit *cUnit, int rDest, int rSrc,
                                 int value)
{
// TODO: check for modified immediate form
    if (LOWREG(rDest) && LOWREG(rSrc) && (value <= 7)) {
        newLIR3(cUnit, THUMB_ADD_RRI3, rDest, rSrc, value);
    } else if (LOWREG(rDest) && (rDest == rSrc) && ((value & 0xff) == 0xff)) {
        newLIR2(cUnit, THUMB_ADD_RI8, rDest, value);
    } else if (value <= 4095) {
        newLIR3(cUnit, THUMB2_ADD_RRI12, rDest, rSrc, value);
    } else {
        loadConstant(cUnit, rDest, value);
        addRegisterRegister(cUnit, rDest, rDest, rFP);
    }
}

/* Load the address of a Dalvik register on the frame */
static void loadValueAddress(CompilationUnit *cUnit, int vSrc, int rDest)
{
    addRegisterImmediate(cUnit, rDest, rFP, vSrc*4);
}

/*
 * FIXME: We need a general register temp for all of these coprocessor
 * operations in case we can't reach in 1 shot.  Might just want to
 * designate a hot temp that all codegen routines could use in their
 * scope.  Alternately, callers will need to allocate a temp and
 * pass it in to each of these.
 */

/* Load a float from a Dalvik register */
static void loadFloat(CompilationUnit *cUnit, int vSrc, int rDest)
{
    assert(vSrc <= 255); // FIXME - temp limit to 1st 256
    assert(SINGLEREG(rDest));
    newLIR3(cUnit, THUMB2_VLDRS, rDest, rFP, vSrc);
}

/* Store a float to a Dalvik register */
static void storeFloat(CompilationUnit *cUnit, int rSrc, int vDest,
                       int rScratch)
{
    assert(vSrc <= 255); // FIXME - temp limit to 1st 256
    assert(SINGLEREG(rSrc));
    newLIR3(cUnit, THUMB2_VSTRS, rSrc, rFP, vDest);
}

/* Load a double from a Dalvik register */
static void loadDouble(CompilationUnit *cUnit, int vSrc, int rDest)
{
    assert(vSrc <= 255); // FIXME - temp limit to 1st 256
    assert(DOUBLEREG(rDest));
    newLIR3(cUnit, THUMB2_VLDRD, rDest, rFP, vSrc);
}

/* Store a double to a Dalvik register */
static void storeDouble(CompilationUnit *cUnit, int rSrc, int vDest,
                       int rScratch)
{
    assert(vSrc <= 255); // FIXME - temp limit to 1st 256
    assert(DOUBLEREG(rSrc));
    newLIR3(cUnit, THUMB2_VSTRD, rSrc, rFP, vDest);
}


/* Load a single value from rFP[src] and store them into rDest */
static void loadValue(CompilationUnit *cUnit, int vSrc, int rDest)
{
    loadWordDisp(cUnit, rFP, vSrc * 4, rDest);
}

/* Load a word at base + displacement.  Displacement must be word multiple */
static void loadWordDisp(CompilationUnit *cUnit, int rBase, int displacement,
                         int rDest)
{
    bool allLowRegs = (LOWREG(rBase) && LOWREG(rDest));
    assert((displacement & 0x3) == 0);
    /* Can it fit in a RRI5? */
    if (allLowRegs && displacement < 128) {
        newLIR3(cUnit, THUMB_LDR_RRI5, rDest, rBase, displacement >> 2);
    } else if (displacement < 4092) {
        newLIR3(cUnit, THUMB2_LDR_RRI12, rDest, rFP, displacement);
    } else {
        loadConstant(cUnit, rDest, displacement);
        if (allLowRegs) {
            newLIR3(cUnit, THUMB_LDR_RRR, rDest, rBase, rDest);
        } else {
            assert(0); // Unimp - need Thumb2 ldr_rrr
        }
    }
}

/* Store a value from rSrc to vDest */
static void storeValue(CompilationUnit *cUnit, int rSrc, int vDest,
                       int rScratch)
{
    killNullCheckedRegister(cUnit, vDest);
    updateLiveRegister(cUnit, vDest, rSrc);

    /* Use reg + imm5*4 to store the value if possible */
    if (LOWREG(rSrc) && vDest <= 31) {
        newLIR3(cUnit, THUMB_STR_RRI5, rSrc, rFP, vDest);
    } else if (vDest <= 1023) {
        newLIR3(cUnit, THUMB2_STR_RRI12, rSrc, rFP, vDest*4);
    } else {
        loadConstant(cUnit, rScratch, vDest*4);
        if (LOWREG(rSrc)) {
            newLIR3(cUnit, THUMB_STR_RRR, rSrc, rFP, rScratch);
        } else {
            assert(0); // Unimp: Need generic str_rrr routine
        }
    }
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
    if ((LOWREG(reg)) && (checkValue == 0) &&
       ((cond == ARM_COND_EQ) || (cond == ARM_COND_NE))) {
        branch = newLIR2(cUnit,
                         (cond == ARM_COND_EQ) ? THUMB2_CBZ : THUMB2_CBNZ,
                         reg, 0);
    } else {
        newLIR2(cUnit, THUMB_CMP_RI8, reg, checkValue);
        branch = newLIR2(cUnit, THUMB_B_COND, 0, cond);
    }
    return genCheckCommon(cUnit, dOffset, branch, pcrLabel);
}
