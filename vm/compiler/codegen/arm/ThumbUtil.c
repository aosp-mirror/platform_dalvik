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

/*
 * Load a immediate using a shortcut if possible; otherwise
 * grab from the per-translation literal pool
 */
static void loadConstant(CompilationUnit *cUnit, int rDest, int value)
{
    /* See if the value can be constructed cheaply */
    if ((value >= 0) && (value <= 255)) {
        newLIR2(cUnit, THUMB_MOV_IMM, rDest, value);
        return;
    } else if ((value & 0xFFFFFF00) == 0xFFFFFF00) {
        newLIR2(cUnit, THUMB_MOV_IMM, rDest, ~value);
        newLIR2(cUnit, THUMB_MVN, rDest, rDest);
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
    newLIR2(cUnit, THUMB_MOV_RR, rAddr, rFP);
    newLIR2(cUnit, THUMB_SUB_RI8, rAddr, sizeof(StackSaveArea) - offset);
    newLIR3(cUnit, THUMB_STR_RRI5, rDPC, rAddr, 0);
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
    /* Use reg + imm5*4 to load the values if possible */
    if (vSrc <= 30) {
        newLIR3(cUnit, THUMB_LDR_RRI5, rDestLo, rFP, vSrc);
        newLIR3(cUnit, THUMB_LDR_RRI5, rDestHi, rFP, vSrc+1);
    } else {
        if (vSrc <= 64) {
            /* Sneak 4 into the base address first */
            newLIR3(cUnit, THUMB_ADD_RRI3, rDestLo, rFP, 4);
            newLIR2(cUnit, THUMB_ADD_RI8, rDestLo, (vSrc-1)*4);
        } else {
            /* Offset too far from rFP */
            loadConstant(cUnit, rDestLo, vSrc*4);
            newLIR3(cUnit, THUMB_ADD_RRR, rDestLo, rFP, rDestLo);
        }
        assert(rDestLo < rDestHi);
        newLIR2(cUnit, THUMB_LDMIA, rDestLo, (1<<rDestLo) | (1<<(rDestHi)));
    }
}

/*
 * Store a pair of values of rSrc and rSrc+1 and store them into vDest and
 * vDest+1
 */
static void storeValuePair(CompilationUnit *cUnit, int rSrcLo, int rSrcHi,
                           int vDest, int rScratch)
{
    killNullCheckedRegister(cUnit, vDest);
    killNullCheckedRegister(cUnit, vDest+1);
    updateLiveRegisterPair(cUnit, vDest, rSrcLo, rSrcHi);

    /* Use reg + imm5*4 to store the values if possible */
    if (vDest <= 30) {
        newLIR3(cUnit, THUMB_STR_RRI5, rSrcLo, rFP, vDest);
        newLIR3(cUnit, THUMB_STR_RRI5, rSrcHi, rFP, vDest+1);
    } else {
        if (vDest <= 64) {
            /* Sneak 4 into the base address first */
            newLIR3(cUnit, THUMB_ADD_RRI3, rScratch, rFP, 4);
            newLIR2(cUnit, THUMB_ADD_RI8, rScratch, (vDest-1)*4);
        } else {
            /* Offset too far from rFP */
            loadConstant(cUnit, rScratch, vDest*4);
            newLIR3(cUnit, THUMB_ADD_RRR, rScratch, rFP, rScratch);
        }
        assert(rSrcLo < rSrcHi);
        newLIR2(cUnit, THUMB_STMIA, rScratch, (1<<rSrcLo) | (1 << (rSrcHi)));
    }
}

/* Load the address of a Dalvik register on the frame */
static void loadValueAddress(CompilationUnit *cUnit, int vSrc, int rDest)
{
    /* RRI3 can add up to 7 */
    if (vSrc <= 1) {
        newLIR3(cUnit, THUMB_ADD_RRI3, rDest, rFP, vSrc*4);
    } else if (vSrc <= 64) {
        /* Sneak 4 into the base address first */
        newLIR3(cUnit, THUMB_ADD_RRI3, rDest, rFP, 4);
        newLIR2(cUnit, THUMB_ADD_RI8, rDest, (vSrc-1)*4);
    } else {
        loadConstant(cUnit, rDest, vSrc*4);
        newLIR3(cUnit, THUMB_ADD_RRR, rDest, rFP, rDest);
    }
}

/* Load a single value from rFP[src] and store them into rDest */
static void loadValue(CompilationUnit *cUnit, int vSrc, int rDest)
{
    /* Use reg + imm5*4 to load the value if possible */
    if (vSrc <= 31) {
        newLIR3(cUnit, THUMB_LDR_RRI5, rDest, rFP, vSrc);
    } else {
        loadConstant(cUnit, rDest, vSrc*4);
        newLIR3(cUnit, THUMB_LDR_RRR, rDest, rFP, rDest);
    }
}

/* Load a word at base + displacement.  Displacement must be word multiple */
static void loadWordDisp(CompilationUnit *cUnit, int rBase, int displacement,
                         int rDest)
{
    assert((displacement & 0x3) == 0);
    /* Can it fit in a RRI5? */
    if (displacement < 128) {
        newLIR3(cUnit, THUMB_LDR_RRI5, rDest, rBase, displacement >> 2);
    } else {
        loadConstant(cUnit, rDest, displacement);
        newLIR3(cUnit, THUMB_LDR_RRR, rDest, rBase, rDest);
    }
}

/* Store a value from rSrc to vDest */
static void storeValue(CompilationUnit *cUnit, int rSrc, int vDest,
                       int rScratch)
{
    killNullCheckedRegister(cUnit, vDest);
    updateLiveRegister(cUnit, vDest, rSrc);

    /* Use reg + imm5*4 to store the value if possible */
    if (vDest <= 31) {
        newLIR3(cUnit, THUMB_STR_RRI5, rSrc, rFP, vDest);
    } else {
        loadConstant(cUnit, rScratch, vDest*4);
        newLIR3(cUnit, THUMB_STR_RRR, rSrc, rFP, rScratch);
    }
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
    newLIR2(cUnit, THUMB_CMP_RI8, reg, checkValue);
    ArmLIR *branch = newLIR2(cUnit, THUMB_B_COND, 0, cond);
    return genCheckCommon(cUnit, dOffset, branch, pcrLabel);
}
