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

#include "Dalvik.h"
#include "interp/InterpDefs.h"
#include "libdex/OpCode.h"
#include "dexdump/OpCodeNames.h"
#include "vm/compiler/CompilerInternals.h"
#include "Armv5teLIR.h"
#include "vm/mterp/common/FindInterface.h"

/* Create the TemplateOpcode enum */
#define JIT_TEMPLATE(X) TEMPLATE_##X,
typedef enum {
#include "../../template/armv5te/TemplateOpList.h"
/*
 * For example,
 *     TEMPLATE_CMP_LONG,
 *     TEMPLATE_RETURN,
 *     ...
 */
    TEMPLATE_LAST_MARK,
} TemplateOpCode;
#undef JIT_TEMPLATE

/* Array holding the entry offset of each template relative to the first one */
static intptr_t templateEntryOffsets[TEMPLATE_LAST_MARK];

/* Track exercised opcodes */
static int opcodeCoverage[256];

/*****************************************************************************/

/*
 * The following are building blocks to construct low-level IRs with 0 - 3
 * operands.
 */
static Armv5teLIR *newLIR0(CompilationUnit *cUnit, Armv5teOpCode opCode)
{
    Armv5teLIR *insn = dvmCompilerNew(sizeof(Armv5teLIR), true);
    assert(isPseudoOpCode(opCode) || EncodingMap[opCode].operands == 0);
    insn->opCode = opCode;
    dvmCompilerAppendLIR(cUnit, (LIR *) insn);
    return insn;
}

static Armv5teLIR *newLIR1(CompilationUnit *cUnit, Armv5teOpCode opCode,
                           int dest)
{
    Armv5teLIR *insn = dvmCompilerNew(sizeof(Armv5teLIR), true);
    assert(isPseudoOpCode(opCode) || EncodingMap[opCode].operands == 1);
    insn->opCode = opCode;
    insn->operands[0] = dest;
    dvmCompilerAppendLIR(cUnit, (LIR *) insn);
    return insn;
}

static Armv5teLIR *newLIR2(CompilationUnit *cUnit, Armv5teOpCode opCode,
                           int dest, int src1)
{
    Armv5teLIR *insn = dvmCompilerNew(sizeof(Armv5teLIR), true);
    assert(isPseudoOpCode(opCode) || EncodingMap[opCode].operands == 2);
    insn->opCode = opCode;
    insn->operands[0] = dest;
    insn->operands[1] = src1;
    dvmCompilerAppendLIR(cUnit, (LIR *) insn);
    return insn;
}

static Armv5teLIR *newLIR3(CompilationUnit *cUnit, Armv5teOpCode opCode,
                           int dest, int src1, int src2)
{
    Armv5teLIR *insn = dvmCompilerNew(sizeof(Armv5teLIR), true);
    assert(isPseudoOpCode(opCode) || EncodingMap[opCode].operands == 3);
    insn->opCode = opCode;
    insn->operands[0] = dest;
    insn->operands[1] = src1;
    insn->operands[2] = src2;
    dvmCompilerAppendLIR(cUnit, (LIR *) insn);
    return insn;
}

static Armv5teLIR *newLIR23(CompilationUnit *cUnit, Armv5teOpCode opCode,
                            int srcdest, int src2)
{
    assert(!isPseudoOpCode(opCode));
    if (EncodingMap[opCode].operands==2)
        return newLIR2(cUnit, opCode, srcdest, src2);
    else
        return newLIR3(cUnit, opCode, srcdest, srcdest, src2);
}

/*****************************************************************************/

/*
 * The following are building blocks to insert constants into the pool or
 * instruction streams.
 */

/* Add a 32-bit constant either in the constant pool or mixed with code */
static Armv5teLIR *addWordData(CompilationUnit *cUnit, int value, bool inPlace)
{
    /* Add the constant to the literal pool */
    if (!inPlace) {
        Armv5teLIR *newValue = dvmCompilerNew(sizeof(Armv5teLIR), true);
        newValue->operands[0] = value;
        newValue->generic.next = cUnit->wordList;
        cUnit->wordList = (LIR *) newValue;
        return newValue;
    } else {
        /* Add the constant in the middle of code stream */
        newLIR1(cUnit, ARMV5TE_16BIT_DATA, (value & 0xffff));
        newLIR1(cUnit, ARMV5TE_16BIT_DATA, (value >> 16));
    }
    return NULL;
}

/*
 * Search the existing constants in the literal pool for an exact or close match
 * within specified delta (greater or equal to 0).
 */
static Armv5teLIR *scanLiteralPool(CompilationUnit *cUnit, int value,
                                   unsigned int delta)
{
    LIR *dataTarget = cUnit->wordList;
    while (dataTarget) {
        if (((unsigned) (value - ((Armv5teLIR *) dataTarget)->operands[0])) <=
            delta)
            return (Armv5teLIR *) dataTarget;
        dataTarget = dataTarget->next;
    }
    return NULL;
}

/*
 * Load a immediate using a shortcut if possible; otherwise
 * grab from the per-translation literal pool
 */
void loadConstant(CompilationUnit *cUnit, int rDest, int value)
{
    /* See if the value can be constructed cheaply */
    if ((value >= 0) && (value <= 255)) {
        newLIR2(cUnit, ARMV5TE_MOV_IMM, rDest, value);
        return;
    } else if ((value & 0xFFFFFF00) == 0xFFFFFF00) {
        newLIR2(cUnit, ARMV5TE_MOV_IMM, rDest, ~value);
        newLIR2(cUnit, ARMV5TE_MVN, rDest, rDest);
        return;
    }
    /* No shortcut - go ahead and use literal pool */
    Armv5teLIR *dataTarget = scanLiteralPool(cUnit, value, 255);
    if (dataTarget == NULL) {
        dataTarget = addWordData(cUnit, value, false);
    }
    Armv5teLIR *loadPcRel = dvmCompilerNew(sizeof(Armv5teLIR), true);
    loadPcRel->opCode = ARMV5TE_LDR_PC_REL;
    loadPcRel->generic.target = (LIR *) dataTarget;
    loadPcRel->operands[0] = rDest;
    dvmCompilerAppendLIR(cUnit, (LIR *) loadPcRel);

    /*
     * To save space in the constant pool, we use the ADD_RRI8 instruction to
     * add up to 255 to an existing constant value.
     */
    if (dataTarget->operands[0] != value) {
        newLIR2(cUnit, ARMV5TE_ADD_RI8, rDest, value - dataTarget->operands[0]);
    }
}

/* Export the Dalvik PC assicated with an instruction to the StackSave area */
static void genExportPC(CompilationUnit *cUnit, MIR *mir, int rDPC, int rAddr)
{
    int offset = offsetof(StackSaveArea, xtra.currentPc);
    loadConstant(cUnit, rDPC, (int) (cUnit->method->insns + mir->offset));
    newLIR2(cUnit, ARMV5TE_MOV_RR, rAddr, rFP);
    newLIR2(cUnit, ARMV5TE_SUB_RI8, rAddr, sizeof(StackSaveArea) - offset);
    newLIR3(cUnit, ARMV5TE_STR_RRI5, rDPC, rAddr, 0);
}

/* Generate conditional branch instructions */
static void genConditionalBranch(CompilationUnit *cUnit,
                                 Armv5teConditionCode cond,
                                 Armv5teLIR *target)
{
    Armv5teLIR *branch = newLIR2(cUnit, ARMV5TE_B_COND, 0, cond);
    branch->generic.target = (LIR *) target;
}

/* Generate unconditional branch instructions */
static void genUnconditionalBranch(CompilationUnit *cUnit, Armv5teLIR *target)
{
    Armv5teLIR *branch = newLIR0(cUnit, ARMV5TE_B_UNCOND);
    branch->generic.target = (LIR *) target;
}

#define USE_IN_CACHE_HANDLER 1

/*
 * Jump to the out-of-line handler in ARM mode to finish executing the
 * remaining of more complex instructions.
 */
static void genDispatchToHandler(CompilationUnit *cUnit, TemplateOpCode opCode)
{
#if USE_IN_CACHE_HANDLER
    /*
     * NOTE - In practice BLX only needs one operand, but since the assembler
     * may abort itself and retry due to other out-of-range conditions we
     * cannot really use operand[0] to store the absolute target address since
     * it may get clobbered by the final relative offset. Therefore,
     * we fake BLX_1 is a two operand instruction and the absolute target
     * address is stored in operand[1].
     */
    newLIR2(cUnit, ARMV5TE_BLX_1,
            (int) gDvmJit.codeCache + templateEntryOffsets[opCode],
            (int) gDvmJit.codeCache + templateEntryOffsets[opCode]);
    newLIR2(cUnit, ARMV5TE_BLX_2,
            (int) gDvmJit.codeCache + templateEntryOffsets[opCode],
            (int) gDvmJit.codeCache + templateEntryOffsets[opCode]);
#else
    /*
     * In case we want to access the statically compiled handlers for
     * debugging purposes, define USE_IN_CACHE_HANDLER to 0
     */
    void *templatePtr;

#define JIT_TEMPLATE(X) extern void dvmCompiler_TEMPLATE_##X();
#include "../../template/armv5te/TemplateOpList.h"
#undef JIT_TEMPLATE
    switch (opCode) {
#define JIT_TEMPLATE(X) \
        case TEMPLATE_##X: { templatePtr = dvmCompiler_TEMPLATE_##X; break; }
#include "../../template/armv5te/TemplateOpList.h"
#undef JIT_TEMPLATE
        default: templatePtr = NULL;
    }
    loadConstant(cUnit, r7, (int) templatePtr);
    newLIR1(cUnit, ARMV5TE_BLX_R, r7);
#endif
}

/* Perform the actual operation for OP_RETURN_* */
static void genReturnCommon(CompilationUnit *cUnit, MIR *mir)
{
    genDispatchToHandler(cUnit, TEMPLATE_RETURN);
#if defined(INVOKE_STATS)
    gDvmJit.jitReturn++;
#endif
    int dPC = (int) (cUnit->method->insns + mir->offset);
    Armv5teLIR *branch = newLIR0(cUnit, ARMV5TE_B_UNCOND);
    /* Set up the place holder to reconstruct this Dalvik PC */
    Armv5teLIR *pcrLabel = dvmCompilerNew(sizeof(Armv5teLIR), true);
    pcrLabel->opCode = ARMV5TE_PSEUDO_PC_RECONSTRUCTION_CELL;
    pcrLabel->operands[0] = dPC;
    pcrLabel->operands[1] = mir->offset;
    /* Insert the place holder to the growable list */
    dvmInsertGrowableList(&cUnit->pcReconstructionList, pcrLabel);
    /* Branch to the PC reconstruction code */
    branch->generic.target = (LIR *) pcrLabel;
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
        newLIR3(cUnit, ARMV5TE_LDR_RRI5, rDestLo, rFP, vSrc);
        newLIR3(cUnit, ARMV5TE_LDR_RRI5, rDestHi, rFP, vSrc+1);
    } else {
        if (vSrc <= 64) {
            /* Sneak 4 into the base address first */
            newLIR3(cUnit, ARMV5TE_ADD_RRI3, rDestLo, rFP, 4);
            newLIR2(cUnit, ARMV5TE_ADD_RI8, rDestHi, (vSrc-1)*4);
        } else {
            /* Offset too far from rFP */
            loadConstant(cUnit, rDestLo, vSrc*4);
            newLIR3(cUnit, ARMV5TE_ADD_RRR, rDestLo, rFP, rDestLo);
        }
        assert(rDestLo != rDestHi);
        newLIR2(cUnit, ARMV5TE_LDMIA, rDestLo, (1<<rDestLo) | (1<<(rDestHi)));
    }
}

/*
 * Store a pair of values of rSrc and rSrc+1 and store them into vDest and
 * vDest+1
 */
static void storeValuePair(CompilationUnit *cUnit, int rSrcLo, int rSrcHi,
                           int vDest, int rScratch)
{
    /* Use reg + imm5*4 to store the values if possible */
    if (vDest <= 30) {
        newLIR3(cUnit, ARMV5TE_STR_RRI5, rSrcLo, rFP, vDest);
        newLIR3(cUnit, ARMV5TE_STR_RRI5, rSrcHi, rFP, vDest+1);
    } else {
        if (vDest <= 64) {
            /* Sneak 4 into the base address first */
            newLIR3(cUnit, ARMV5TE_ADD_RRI3, rScratch, rFP, 4);
            newLIR2(cUnit, ARMV5TE_ADD_RI8, rScratch, (vDest-1)*4);
        } else {
            /* Offset too far from rFP */
            loadConstant(cUnit, rScratch, vDest*4);
            newLIR3(cUnit, ARMV5TE_ADD_RRR, rScratch, rFP, rScratch);
        }
        assert(rSrcLo != rSrcHi);
        newLIR2(cUnit, ARMV5TE_STMIA, rScratch, (1<<rSrcLo) | (1 << (rSrcHi)));
    }
}

/* Load the address of a Dalvik register on the frame */
static void loadValueAddress(CompilationUnit *cUnit, int vSrc, int rDest)
{
    /* RRI3 can add up to 7 */
    if (vSrc <= 1) {
        newLIR3(cUnit, ARMV5TE_ADD_RRI3, rDest, rFP, vSrc*4);
    } else if (vSrc <= 64) {
        /* Sneak 4 into the base address first */
        newLIR3(cUnit, ARMV5TE_ADD_RRI3, rDest, rFP, 4);
        newLIR2(cUnit, ARMV5TE_ADD_RI8, rDest, (vSrc-1)*4);
    } else {
        loadConstant(cUnit, rDest, vSrc*4);
        newLIR3(cUnit, ARMV5TE_ADD_RRR, rDest, rFP, rDest);
    }
}


/* Load a single value from rFP[src] and store them into rDest */
static void loadValue(CompilationUnit *cUnit, int vSrc, int rDest)
{
    /* Use reg + imm5*4 to load the value if possible */
    if (vSrc <= 31) {
        newLIR3(cUnit, ARMV5TE_LDR_RRI5, rDest, rFP, vSrc);
    } else {
        loadConstant(cUnit, rDest, vSrc*4);
        newLIR3(cUnit, ARMV5TE_LDR_RRR, rDest, rFP, rDest);
    }
}

/* Store a value from rSrc to vDest */
static void storeValue(CompilationUnit *cUnit, int rSrc, int vDest,
                       int rScratch)
{
    /* Use reg + imm5*4 to store the value if possible */
    if (vDest <= 31) {
        newLIR3(cUnit, ARMV5TE_STR_RRI5, rSrc, rFP, vDest);
    } else {
        loadConstant(cUnit, rScratch, vDest*4);
        newLIR3(cUnit, ARMV5TE_STR_RRR, rSrc, rFP, rScratch);
    }
}

/* Calculate the address of rFP+vSrc*4 */
static void calculateValueAddress(CompilationUnit *cUnit, int vSrc, int rDest)
{
    /* Use add rd, rs, imm_3 */
    if (vSrc <= 1) {
        newLIR3(cUnit, ARMV5TE_ADD_RRI3, rDest, rFP, vSrc*4);
    } else if (vSrc <= 64) {
        /* Use add rd, imm_8 */
        /* Sneak in 4 above rFP to cover one more register offset (ie v64) */
        newLIR3(cUnit, ARMV5TE_ADD_RRI3, rDest, rFP, 4);
        newLIR2(cUnit, ARMV5TE_ADD_RI8, rDest, (vSrc-1)*4);
    } else {
        /* Load offset from the constant pool */
        loadConstant(cUnit, rDest, vSrc*4);
        newLIR3(cUnit, ARMV5TE_ADD_RRR, rDest, rFP, rDest);
    }
}

/*
 * Perform a binary operation on 64-bit operands and leave the results in the
 * r0/r1 pair.
 */
static void genBinaryOpWide(CompilationUnit *cUnit, int vDest,
                            Armv5teOpCode preinst, Armv5teOpCode inst)
{
    newLIR23(cUnit, preinst, r0, r2);
    newLIR23(cUnit, inst, r1, r3);
    storeValuePair(cUnit, r0, r1, vDest, r2);
}

/* Perform a binary operation on 32-bit operands and leave the results in r0. */
static void genBinaryOp(CompilationUnit *cUnit, int vDest, Armv5teOpCode inst)
{
    newLIR23(cUnit, inst, r0, r1);
    storeValue(cUnit, r0, vDest, r1);
}

/* Create the PC reconstruction slot if not already done */
static inline Armv5teLIR *genCheckCommon(CompilationUnit *cUnit, int dOffset,
                                         Armv5teLIR *branch,
                                         Armv5teLIR *pcrLabel)
{
    /* Set up the place holder to reconstruct this Dalvik PC */
    if (pcrLabel == NULL) {
        int dPC = (int) (cUnit->method->insns + dOffset);
        pcrLabel = dvmCompilerNew(sizeof(Armv5teLIR), true);
        pcrLabel->opCode = ARMV5TE_PSEUDO_PC_RECONSTRUCTION_CELL;
        pcrLabel->operands[0] = dPC;
        pcrLabel->operands[1] = dOffset;
        /* Insert the place holder to the growable list */
        dvmInsertGrowableList(&cUnit->pcReconstructionList, pcrLabel);
    }
    /* Branch to the PC reconstruction code */
    branch->generic.target = (LIR *) pcrLabel;
    return pcrLabel;
}

/*
 * Perform a "reg cmp imm" operation and jump to the PCR region if condition
 * satisfies.
 */
static inline Armv5teLIR *genRegImmCheck(CompilationUnit *cUnit,
                                         Armv5teConditionCode cond, int reg,
                                         int checkValue, int dOffset,
                                         Armv5teLIR *pcrLabel)
{
    newLIR2(cUnit, ARMV5TE_CMP_RI8, reg, checkValue);
    Armv5teLIR *branch = newLIR2(cUnit, ARMV5TE_B_COND, 0, cond);
    return genCheckCommon(cUnit, dOffset, branch, pcrLabel);
}

/*
 * Perform a "reg cmp reg" operation and jump to the PCR region if condition
 * satisfies.
 */
static inline Armv5teLIR *inertRegRegCheck(CompilationUnit *cUnit,
                                           Armv5teConditionCode cond,
                                           int reg1, int reg2, int dOffset,
                                           Armv5teLIR *pcrLabel)
{
    newLIR2(cUnit, ARMV5TE_CMP_RR, reg1, reg2);
    Armv5teLIR *branch = newLIR2(cUnit, ARMV5TE_B_COND, 0, cond);
    return genCheckCommon(cUnit, dOffset, branch, pcrLabel);
}

/* Perform null-check on a register */
static Armv5teLIR *genNullCheck(CompilationUnit *cUnit, int reg, int dOffset,
                                Armv5teLIR *pcrLabel)
{
    return genRegImmCheck(cUnit, ARM_COND_EQ, reg, 0, dOffset, pcrLabel);
}

/* Perform bound check on two registers */
static Armv5teLIR *genBoundsCheck(CompilationUnit *cUnit, int rIndex,
                                  int rBound, int dOffset, Armv5teLIR *pcrLabel)
{
    return inertRegRegCheck(cUnit, ARM_COND_CS, rIndex, rBound, dOffset,
                            pcrLabel);
}

/* Generate a unconditional branch to go to the interpreter */
static inline Armv5teLIR *genTrap(CompilationUnit *cUnit, int dOffset,
                                  Armv5teLIR *pcrLabel)
{
    Armv5teLIR *branch = newLIR0(cUnit, ARMV5TE_B_UNCOND);
    return genCheckCommon(cUnit, dOffset, branch, pcrLabel);
}

/* Load a wide field from an object instance */
static void genIGetWide(CompilationUnit *cUnit, MIR *mir, int fieldOffset)
{
    DecodedInstruction *dInsn = &mir->dalvikInsn;

    loadValue(cUnit, dInsn->vB, r2);
    loadConstant(cUnit, r3, fieldOffset);
    genNullCheck(cUnit, r2, mir->offset, NULL); /* null object? */
    newLIR3(cUnit, ARMV5TE_ADD_RRR, r2, r2, r3);
    newLIR2(cUnit, ARMV5TE_LDMIA, r2, (1<<r0 | 1<<r1));
    storeValuePair(cUnit, r0, r1, dInsn->vA, r3);
}

/* Store a wide field to an object instance */
static void genIPutWide(CompilationUnit *cUnit, MIR *mir, int fieldOffset)
{
    DecodedInstruction *dInsn = &mir->dalvikInsn;

    loadValue(cUnit, dInsn->vB, r2);
    loadValuePair(cUnit, dInsn->vA, r0, r1);
    loadConstant(cUnit, r3, fieldOffset);
    genNullCheck(cUnit, r2, mir->offset, NULL); /* null object? */
    newLIR3(cUnit, ARMV5TE_ADD_RRR, r2, r2, r3);
    newLIR2(cUnit, ARMV5TE_STMIA, r2, (1<<r0 | 1<<r1));
}

/*
 * Load a field from an object instance
 *
 * Inst should be one of:
 *      ARMV5TE_LDR_RRR
 *      ARMV5TE_LDRB_RRR
 *      ARMV5TE_LDRH_RRR
 *      ARMV5TE_LDRSB_RRR
 *      ARMV5TE_LDRSH_RRR
 */
static void genIGet(CompilationUnit *cUnit, MIR *mir, Armv5teOpCode inst,
                    int fieldOffset)
{
    DecodedInstruction *dInsn = &mir->dalvikInsn;

    /* TUNING: write a utility routine to load via base + constant offset */
    loadValue(cUnit, dInsn->vB, r0);
    loadConstant(cUnit, r1, fieldOffset);
    genNullCheck(cUnit, r0, mir->offset, NULL); /* null object? */
    newLIR3(cUnit, inst, r0, r0, r1);
    storeValue(cUnit, r0, dInsn->vA, r1);
}

/*
 * Store a field to an object instance
 *
 * Inst should be one of:
 *      ARMV5TE_STR_RRR
 *      ARMV5TE_STRB_RRR
 *      ARMV5TE_STRH_RRR
 */
static void genIPut(CompilationUnit *cUnit, MIR *mir, Armv5teOpCode inst,
                    int fieldOffset)
{
    DecodedInstruction *dInsn = &mir->dalvikInsn;

    /* TUNING: write a utility routine to load via base + constant offset */
    loadValue(cUnit, dInsn->vB, r2);
    loadConstant(cUnit, r1, fieldOffset);
    loadValue(cUnit, dInsn->vA, r0);
    genNullCheck(cUnit, r2, mir->offset, NULL); /* null object? */
    newLIR3(cUnit, inst, r0, r2, r1);
}


/* TODO: This should probably be done as an out-of-line instruction handler. */

/*
 * Generate array load
 *
 * Inst should be one of:
 *      ARMV5TE_LDR_RRR
 *      ARMV5TE_LDRB_RRR
 *      ARMV5TE_LDRH_RRR
 *      ARMV5TE_LDRSB_RRR
 *      ARMV5TE_LDRSH_RRR
 */
static void genArrayGet(CompilationUnit *cUnit, MIR *mir, Armv5teOpCode inst,
                        int vArray, int vIndex, int vDest, int scale)
{
    int lenOffset = offsetof(ArrayObject, length);
    int dataOffset = offsetof(ArrayObject, contents);

    loadValue(cUnit, vArray, r2);
    loadValue(cUnit, vIndex, r3);

    /* null object? */
    Armv5teLIR * pcrLabel = genNullCheck(cUnit, r2, mir->offset, NULL);
    newLIR3(cUnit, ARMV5TE_LDR_RRI5, r0, r2, lenOffset >> 2);  /* Get len */
    newLIR2(cUnit, ARMV5TE_ADD_RI8, r2, dataOffset); /* r2 -> array data */
    genBoundsCheck(cUnit, r3, r0, mir->offset, pcrLabel);
    if (scale) {
        newLIR3(cUnit, ARMV5TE_LSL, r3, r3, scale);
    }
    if (scale==3) {
        newLIR3(cUnit, inst, r0, r2, r3);
        newLIR2(cUnit, ARMV5TE_ADD_RI8, r2, 4);
        newLIR3(cUnit, inst, r1, r2, r3);
        storeValuePair(cUnit, r0, r1, vDest, r3);
    } else {
        newLIR3(cUnit, inst, r0, r2, r3);
        storeValue(cUnit, r0, vDest, r3);
    }
}

/* TODO: This should probably be done as an out-of-line instruction handler. */

/*
 * Generate array store
 *
 * Inst should be one of:
 *      ARMV5TE_STR_RRR
 *      ARMV5TE_STRB_RRR
 *      ARMV5TE_STRH_RRR
 */
static void genArrayPut(CompilationUnit *cUnit, MIR *mir, Armv5teOpCode inst,
                        int vArray, int vIndex, int vSrc, int scale)
{
    int lenOffset = offsetof(ArrayObject, length);
    int dataOffset = offsetof(ArrayObject, contents);

    loadValue(cUnit, vArray, r2);
    loadValue(cUnit, vIndex, r3);
    genNullCheck(cUnit, r2, mir->offset, NULL); /* null object? */
    newLIR3(cUnit, ARMV5TE_LDR_RRI5, r0, r2, lenOffset >> 2);  /* Get len */
    newLIR2(cUnit, ARMV5TE_ADD_RI8, r2, dataOffset); /* r2 -> array data */
    genBoundsCheck(cUnit, r3, r0, mir->offset, NULL);
    /* at this point, r2 points to array, r3 is unscaled index */
    if (scale==3) {
        loadValuePair(cUnit, vSrc, r0, r1);
    } else {
        loadValue(cUnit, vSrc, r0);
    }
    if (scale) {
        newLIR3(cUnit, ARMV5TE_LSL, r3, r3, scale);
    }
    /*
     * at this point, r2 points to array, r3 is scaled index, and r0[r1] is
     * data
     */
    if (scale==3) {
        newLIR3(cUnit, inst, r0, r2, r3);
        newLIR2(cUnit, ARMV5TE_ADD_RI8, r2, 4);
        newLIR3(cUnit, inst, r1, r2, r3);
    } else {
        newLIR3(cUnit, inst, r0, r2, r3);
    }
}

static bool genShiftOpLong(CompilationUnit *cUnit, MIR *mir, int vDest,
                           int vSrc1, int vShift)
{
     loadValuePair(cUnit, vSrc1, r0, r1);
     loadValue(cUnit, vShift, r2);
     switch( mir->dalvikInsn.opCode) {
         case OP_SHL_LONG:
         case OP_SHL_LONG_2ADDR:
             genDispatchToHandler(cUnit, TEMPLATE_SHL_LONG);
             break;
         case OP_SHR_LONG:
         case OP_SHR_LONG_2ADDR:
             genDispatchToHandler(cUnit, TEMPLATE_SHR_LONG);
             break;
         case OP_USHR_LONG:
         case OP_USHR_LONG_2ADDR:
             genDispatchToHandler(cUnit, TEMPLATE_USHR_LONG);
             break;
         default:
             return true;
     }
     storeValuePair(cUnit, r0, r1, vDest, r2);
     return false;
}

static bool genArithOpFloat(CompilationUnit *cUnit, MIR *mir, int vDest,
                            int vSrc1, int vSrc2)
{
    void* funct;
    /* TODO: use a proper include file to define these */
    float __aeabi_fadd(float a, float b);
    float __aeabi_fsub(float a, float b);
    float __aeabi_fdiv(float a, float b);
    float __aeabi_fmul(float a, float b);
    float fmodf(float a, float b);

    switch (mir->dalvikInsn.opCode) {
        case OP_ADD_FLOAT_2ADDR:
        case OP_ADD_FLOAT:
            funct = (void*) __aeabi_fadd;
            break;
        case OP_SUB_FLOAT_2ADDR:
        case OP_SUB_FLOAT:
            funct = (void*) __aeabi_fsub;
            break;
        case OP_DIV_FLOAT_2ADDR:
        case OP_DIV_FLOAT:
            funct = (void*) __aeabi_fdiv;
            break;
        case OP_MUL_FLOAT_2ADDR:
        case OP_MUL_FLOAT:
            funct = (void*) __aeabi_fmul;
            break;
        case OP_REM_FLOAT_2ADDR:
        case OP_REM_FLOAT:
            funct = (void*) fmodf;
            break;
        case OP_NEG_FLOAT: {
            loadValue(cUnit, vSrc2, r0);
            loadConstant(cUnit, r1, 0x80000000);
            newLIR3(cUnit, ARMV5TE_ADD_RRR, r0, r0, r1);
            storeValue(cUnit, r0, vDest, r1);
            return false;
        }
        default:
            return true;
    }
    loadConstant(cUnit, r2, (int)funct);
    loadValue(cUnit, vSrc1, r0);
    loadValue(cUnit, vSrc2, r1);
    newLIR1(cUnit, ARMV5TE_BLX_R, r2);
    storeValue(cUnit, r0, vDest, r1);
    return false;
}

static bool genArithOpDouble(CompilationUnit *cUnit, MIR *mir, int vDest,
                             int vSrc1, int vSrc2)
{
    void* funct;
    /* TODO: use a proper include file to define these */
    double __aeabi_dadd(double a, double b);
    double __aeabi_dsub(double a, double b);
    double __aeabi_ddiv(double a, double b);
    double __aeabi_dmul(double a, double b);
    double fmod(double a, double b);

    switch (mir->dalvikInsn.opCode) {
        case OP_ADD_DOUBLE_2ADDR:
        case OP_ADD_DOUBLE:
            funct = (void*) __aeabi_dadd;
            break;
        case OP_SUB_DOUBLE_2ADDR:
        case OP_SUB_DOUBLE:
            funct = (void*) __aeabi_dsub;
            break;
        case OP_DIV_DOUBLE_2ADDR:
        case OP_DIV_DOUBLE:
            funct = (void*) __aeabi_ddiv;
            break;
        case OP_MUL_DOUBLE_2ADDR:
        case OP_MUL_DOUBLE:
            funct = (void*) __aeabi_dmul;
            break;
        case OP_REM_DOUBLE_2ADDR:
        case OP_REM_DOUBLE:
            funct = (void*) fmod;
            break;
        case OP_NEG_DOUBLE: {
            loadValuePair(cUnit, vSrc2, r0, r1);
            loadConstant(cUnit, r2, 0x80000000);
            newLIR3(cUnit, ARMV5TE_ADD_RRR, r1, r1, r2);
            storeValuePair(cUnit, r0, r1, vDest, r2);
            return false;
        }
        default:
            return true;
    }
    loadConstant(cUnit, r4PC, (int)funct);
    loadValuePair(cUnit, vSrc1, r0, r1);
    loadValuePair(cUnit, vSrc2, r2, r3);
    newLIR1(cUnit, ARMV5TE_BLX_R, r4PC);
    storeValuePair(cUnit, r0, r1, vDest, r2);
    return false;
}

static bool genArithOpLong(CompilationUnit *cUnit, MIR *mir, int vDest,
                           int vSrc1, int vSrc2)
{
    int firstOp = ARMV5TE_BKPT;
    int secondOp = ARMV5TE_BKPT;
    bool callOut = false;
    void *callTgt;
    int retReg = r0;
    /* TODO - find proper .h file to declare these */
    long long __aeabi_ldivmod(long long op1, long long op2);

    switch (mir->dalvikInsn.opCode) {
        case OP_NOT_LONG:
            firstOp = ARMV5TE_MVN;
            secondOp = ARMV5TE_MVN;
            break;
        case OP_ADD_LONG:
        case OP_ADD_LONG_2ADDR:
            firstOp = ARMV5TE_ADD_RRR;
            secondOp = ARMV5TE_ADC;
            break;
        case OP_SUB_LONG:
        case OP_SUB_LONG_2ADDR:
            firstOp = ARMV5TE_SUB_RRR;
            secondOp = ARMV5TE_SBC;
            break;
        case OP_MUL_LONG:
        case OP_MUL_LONG_2ADDR:
            loadValuePair(cUnit, vSrc1, r0, r1);
            loadValuePair(cUnit, vSrc2, r2, r3);
            genDispatchToHandler(cUnit, TEMPLATE_MUL_LONG);
            storeValuePair(cUnit, r0, r1, vDest, r2);
            return false;
            break;
        case OP_DIV_LONG:
        case OP_DIV_LONG_2ADDR:
            callOut = true;
            retReg = r0;
            callTgt = (void*)__aeabi_ldivmod;
            break;
        /* NOTE - result is in r2/r3 instead of r0/r1 */
        case OP_REM_LONG:
        case OP_REM_LONG_2ADDR:
            callOut = true;
            callTgt = (void*)__aeabi_ldivmod;
            retReg = r2;
            break;
        case OP_AND_LONG:
        case OP_AND_LONG_2ADDR:
            firstOp = ARMV5TE_AND_RR;
            secondOp = ARMV5TE_AND_RR;
            break;
        case OP_OR_LONG:
        case OP_OR_LONG_2ADDR:
            firstOp = ARMV5TE_ORR;
            secondOp = ARMV5TE_ORR;
            break;
        case OP_XOR_LONG:
        case OP_XOR_LONG_2ADDR:
            firstOp = ARMV5TE_EOR;
            secondOp = ARMV5TE_EOR;
            break;
        case OP_NEG_LONG:
            loadValuePair(cUnit, vSrc2, r2, r3);
            loadConstant(cUnit, r1, 0);
            newLIR3(cUnit, ARMV5TE_SUB_RRR, r0, r1, r2);
            newLIR2(cUnit, ARMV5TE_SBC, r1, r3);
            storeValuePair(cUnit, r0, r1, vDest, r2);
            return false;
        default:
            LOGE("Invalid long arith op");
            dvmAbort();
    }
    if (!callOut) {
        loadValuePair(cUnit, vSrc1, r0, r1);
        loadValuePair(cUnit, vSrc2, r2, r3);
        genBinaryOpWide(cUnit, vDest, firstOp, secondOp);
    } else {
        loadValuePair(cUnit, vSrc2, r2, r3);
        loadConstant(cUnit, r4PC, (int) callTgt);
        loadValuePair(cUnit, vSrc1, r0, r1);
        newLIR1(cUnit, ARMV5TE_BLX_R, r4PC);
        storeValuePair(cUnit, retReg, retReg+1, vDest, r4PC);
    }
    return false;
}

static bool genArithOpInt(CompilationUnit *cUnit, MIR *mir, int vDest,
                          int vSrc1, int vSrc2)
{
    int armOp = ARMV5TE_BKPT;
    bool callOut = false;
    bool checkZero = false;
    int retReg = r0;
    void *callTgt;

    /* TODO - find proper .h file to declare these */
    int __aeabi_idivmod(int op1, int op2);
    int __aeabi_idiv(int op1, int op2);

    switch (mir->dalvikInsn.opCode) {
        case OP_NEG_INT:
            armOp = ARMV5TE_NEG;
            break;
        case OP_NOT_INT:
            armOp = ARMV5TE_MVN;
            break;
        case OP_ADD_INT:
        case OP_ADD_INT_2ADDR:
            armOp = ARMV5TE_ADD_RRR;
            break;
        case OP_SUB_INT:
        case OP_SUB_INT_2ADDR:
            armOp = ARMV5TE_SUB_RRR;
            break;
        case OP_MUL_INT:
        case OP_MUL_INT_2ADDR:
            armOp = ARMV5TE_MUL;
            break;
        case OP_DIV_INT:
        case OP_DIV_INT_2ADDR:
            callOut = true;
            checkZero = true;
            callTgt = __aeabi_idiv;
            retReg = r0;
            break;
        /* NOTE: returns in r1 */
        case OP_REM_INT:
        case OP_REM_INT_2ADDR:
            callOut = true;
            checkZero = true;
            callTgt = __aeabi_idivmod;
            retReg = r1;
            break;
        case OP_AND_INT:
        case OP_AND_INT_2ADDR:
            armOp = ARMV5TE_AND_RR;
            break;
        case OP_OR_INT:
        case OP_OR_INT_2ADDR:
            armOp = ARMV5TE_ORR;
            break;
        case OP_XOR_INT:
        case OP_XOR_INT_2ADDR:
            armOp = ARMV5TE_EOR;
            break;
        case OP_SHL_INT:
        case OP_SHL_INT_2ADDR:
            armOp = ARMV5TE_LSLV;
            break;
        case OP_SHR_INT:
        case OP_SHR_INT_2ADDR:
            armOp = ARMV5TE_ASRV;
            break;
        case OP_USHR_INT:
        case OP_USHR_INT_2ADDR:
            armOp = ARMV5TE_LSRV;
            break;
        default:
            LOGE("Invalid word arith op: 0x%x(%d)",
                 mir->dalvikInsn.opCode, mir->dalvikInsn.opCode);
            dvmAbort();
    }
    if (!callOut) {
        loadValue(cUnit, vSrc1, r0);
        loadValue(cUnit, vSrc2, r1);
        genBinaryOp(cUnit, vDest, armOp);
    } else {
        loadValue(cUnit, vSrc2, r1);
        loadConstant(cUnit, r2, (int) callTgt);
        loadValue(cUnit, vSrc1, r0);
        if (checkZero) {
            genNullCheck(cUnit, r1, mir->offset, NULL);
        }
        newLIR1(cUnit, ARMV5TE_BLX_R, r2);
        storeValue(cUnit, retReg, vDest, r2);
    }
    return false;
}

static bool genArithOp(CompilationUnit *cUnit, MIR *mir)
{
    OpCode opCode = mir->dalvikInsn.opCode;
    int vA = mir->dalvikInsn.vA;
    int vB = mir->dalvikInsn.vB;
    int vC = mir->dalvikInsn.vC;

    if ((opCode >= OP_ADD_LONG_2ADDR) && (opCode <= OP_XOR_LONG_2ADDR)) {
        return genArithOpLong(cUnit,mir, vA, vA, vB);
    }
    if ((opCode >= OP_ADD_LONG) && (opCode <= OP_XOR_LONG)) {
        return genArithOpLong(cUnit,mir, vA, vB, vC);
    }
    if ((opCode >= OP_SHL_LONG_2ADDR) && (opCode <= OP_USHR_LONG_2ADDR)) {
        return genShiftOpLong(cUnit,mir, vA, vA, vB);
    }
    if ((opCode >= OP_SHL_LONG) && (opCode <= OP_USHR_LONG)) {
        return genShiftOpLong(cUnit,mir, vA, vB, vC);
    }
    if ((opCode >= OP_ADD_INT_2ADDR) && (opCode <= OP_USHR_INT_2ADDR)) {
        return genArithOpInt(cUnit,mir, vA, vA, vB);
    }
    if ((opCode >= OP_ADD_INT) && (opCode <= OP_USHR_INT)) {
        return genArithOpInt(cUnit,mir, vA, vB, vC);
    }
    if ((opCode >= OP_ADD_FLOAT_2ADDR) && (opCode <= OP_REM_FLOAT_2ADDR)) {
        return genArithOpFloat(cUnit,mir, vA, vA, vB);
    }
    if ((opCode >= OP_ADD_FLOAT) && (opCode <= OP_REM_FLOAT)) {
        return genArithOpFloat(cUnit,mir, vA, vB, vC);
    }
    if ((opCode >= OP_ADD_DOUBLE_2ADDR) && (opCode <= OP_REM_DOUBLE_2ADDR)) {
        return genArithOpDouble(cUnit,mir, vA, vA, vB);
    }
    if ((opCode >= OP_ADD_DOUBLE) && (opCode <= OP_REM_DOUBLE)) {
        return genArithOpDouble(cUnit,mir, vA, vB, vC);
    }
    return true;
}

static bool genConversion(CompilationUnit *cUnit, MIR *mir, void *funct,
                          int srcSize, int tgtSize)
{
    loadConstant(cUnit, r2, (int)funct);
    if (srcSize == 1) {
        loadValue(cUnit, mir->dalvikInsn.vB, r0);
    } else {
        loadValuePair(cUnit, mir->dalvikInsn.vB, r0, r1);
    }
    newLIR1(cUnit, ARMV5TE_BLX_R, r2);
    if (tgtSize == 1) {
        storeValue(cUnit, r0, mir->dalvikInsn.vA, r1);
    } else {
        storeValuePair(cUnit, r0, r1, mir->dalvikInsn.vA, r2);
    }
    return false;
}

/* Experimental example of completely inlining a native replacement */
static bool genInlinedStringLength(CompilationUnit *cUnit, MIR *mir)
{
    int offset = (int) &((InterpState *) NULL)->retval;
    DecodedInstruction *dInsn = &mir->dalvikInsn;
    assert(dInsn->vA == 1);
    loadValue(cUnit, dInsn->arg[0], r0);
    loadConstant(cUnit, r1, gDvm.offJavaLangString_count);
    genNullCheck(cUnit, r0, mir->offset, NULL);
    newLIR3(cUnit, ARMV5TE_LDR_RRR, r0, r0, r1);
    newLIR3(cUnit, ARMV5TE_STR_RRI5, r0, rGLUE, offset >> 2);
    return false;
}

static void genProcessArgsNoRange(CompilationUnit *cUnit, MIR *mir,
                                  DecodedInstruction *dInsn,
                                  Armv5teLIR **pcrLabel)
{
    unsigned int i;
    unsigned int regMask = 0;

    /* Load arguments to r0..r4 */
    for (i = 0; i < dInsn->vA; i++) {
        regMask |= 1 << i;
        loadValue(cUnit, dInsn->arg[i], i);
    }
    if (regMask) {
        /* Up to 5 args are pushed on top of FP - sizeofStackSaveArea */
        newLIR2(cUnit, ARMV5TE_MOV_RR, r7, rFP);
        newLIR2(cUnit, ARMV5TE_SUB_RI8, r7,
                sizeof(StackSaveArea) + (dInsn->vA << 2));
        /* generate null check */
        if (pcrLabel) {
            *pcrLabel = genNullCheck(cUnit, r0, mir->offset, NULL);
        }
        newLIR2(cUnit, ARMV5TE_STMIA, r7, regMask);
    }
}

static void genProcessArgsRange(CompilationUnit *cUnit, MIR *mir,
                                DecodedInstruction *dInsn,
                                Armv5teLIR **pcrLabel)
{
    int srcOffset = dInsn->vC << 2;
    int numArgs = dInsn->vA;
    int regMask;
    /*
     * r4PC     : &rFP[vC]
     * r7: &newFP[0]
     */
    if (srcOffset < 8) {
        newLIR3(cUnit, ARMV5TE_ADD_RRI3, r4PC, rFP, srcOffset);
    } else {
        loadConstant(cUnit, r4PC, srcOffset);
        newLIR3(cUnit, ARMV5TE_ADD_RRR, r4PC, rFP, r4PC);
    }
    /* load [r0 .. min(numArgs,4)] */
    regMask = (1 << ((numArgs < 4) ? numArgs : 4)) - 1;
    newLIR2(cUnit, ARMV5TE_LDMIA, r4PC, regMask);

    if (sizeof(StackSaveArea) + (numArgs << 2) < 256) {
        newLIR2(cUnit, ARMV5TE_MOV_RR, r7, rFP);
        newLIR2(cUnit, ARMV5TE_SUB_RI8, r7,
                sizeof(StackSaveArea) + (numArgs << 2));
    } else {
        loadConstant(cUnit, r7, sizeof(StackSaveArea) + (numArgs << 2));
        newLIR3(cUnit, ARMV5TE_SUB_RRR, r7, rFP, r7);
    }

    /* generate null check */
    if (pcrLabel) {
        *pcrLabel = genNullCheck(cUnit, r0, mir->offset, NULL);
    }

    /*
     * Handle remaining 4n arguments:
     * store previously loaded 4 values and load the next 4 values
     */
    if (numArgs >= 8) {
        Armv5teLIR *loopLabel = NULL;
        /*
         * r0 contains "this" and it will be used later, so push it to the stack
         * first. Pushing r5 is just for stack alignment purposes.
         */
        newLIR1(cUnit, ARMV5TE_PUSH, 1 << r0 | 1 << 5);
        /* No need to generate the loop structure if numArgs <= 11 */
        if (numArgs > 11) {
            loadConstant(cUnit, 5, ((numArgs - 4) >> 2) << 2);
            loopLabel = newLIR0(cUnit, ARMV5TE_PSEUDO_TARGET_LABEL);
        }
        newLIR2(cUnit, ARMV5TE_STMIA, r7, regMask);
        newLIR2(cUnit, ARMV5TE_LDMIA, r4PC, regMask);
        /* No need to generate the loop structure if numArgs <= 11 */
        if (numArgs > 11) {
            newLIR2(cUnit, ARMV5TE_SUB_RI8, 5, 4);
            genConditionalBranch(cUnit, ARM_COND_NE, loopLabel);
        }
    }

    /* Save the last batch of loaded values */
    newLIR2(cUnit, ARMV5TE_STMIA, r7, regMask);

    /* Generate the loop epilogue - don't use r0 */
    if ((numArgs > 4) && (numArgs % 4)) {
        regMask = ((1 << (numArgs & 0x3)) - 1) << 1;
        newLIR2(cUnit, ARMV5TE_LDMIA, r4PC, regMask);
    }
    if (numArgs >= 8)
        newLIR1(cUnit, ARMV5TE_POP, 1 << r0 | 1 << 5);

    /* Save the modulo 4 arguments */
    if ((numArgs > 4) && (numArgs % 4)) {
        newLIR2(cUnit, ARMV5TE_STMIA, r7, regMask);
    }
}

static void genInvokeCommon(CompilationUnit *cUnit, MIR *mir, BasicBlock *bb,
                            Armv5teLIR *labelList, Armv5teLIR *pcrLabel,
                            const Method *calleeMethod)
{
    Armv5teLIR *retChainingCell = &labelList[bb->fallThrough->id];

    /* r1 = &retChainingCell */
    Armv5teLIR *addrRetChain = newLIR2(cUnit, ARMV5TE_ADD_PC_REL,
                                           r1, 0);
    /* r4PC = dalvikCallsite */
    loadConstant(cUnit, r4PC,
                 (int) (cUnit->method->insns + mir->offset));
    addrRetChain->generic.target = (LIR *) retChainingCell;
    /*
     * r0 = calleeMethod (loaded upon calling genInvokeCommon)
     * r1 = &ChainingCell
     * r4PC = callsiteDPC
     */
    if (dvmIsNativeMethod(calleeMethod)) {
        genDispatchToHandler(cUnit, TEMPLATE_INVOKE_METHOD_NO_OPT);
#if defined(INVOKE_STATS)
        gDvmJit.invokeNoOpt++;
#endif
    } else {
        genDispatchToHandler(cUnit, TEMPLATE_INVOKE_METHOD_CHAIN);
#if defined(INVOKE_STATS)
        gDvmJit.invokeChain++;
#endif
        genUnconditionalBranch(cUnit, &labelList[bb->taken->id]);
    }
    /* Handle exceptions using the interpreter */
    genTrap(cUnit, mir->offset, pcrLabel);
}

/* Geneate a branch to go back to the interpreter */
static void genPuntToInterp(CompilationUnit *cUnit, unsigned int offset)
{
    /* r0 = dalvik pc */
    loadConstant(cUnit, r0, (int) (cUnit->method->insns + offset));
    newLIR3(cUnit, ARMV5TE_LDR_RRI5, r1, rGLUE,
            offsetof(InterpState, jitToInterpEntries.dvmJitToInterpPunt) >> 2);
    newLIR1(cUnit, ARMV5TE_BLX_R, r1);
}

/*
 * Attempt to single step one instruction using the interpreter and return
 * to the compiled code for the next Dalvik instruction
 */
static void genInterpSingleStep(CompilationUnit *cUnit, MIR *mir)
{
    int flags = dexGetInstrFlags(gDvm.instrFlags, mir->dalvikInsn.opCode);
    int flagsToCheck = kInstrCanBranch | kInstrCanSwitch | kInstrCanReturn |
                       kInstrCanThrow;
    if ((mir->next == NULL) || (flags & flagsToCheck)) {
       genPuntToInterp(cUnit, mir->offset);
       return;
    }
    int entryAddr = offsetof(InterpState,
                             jitToInterpEntries.dvmJitToInterpSingleStep);
    newLIR3(cUnit, ARMV5TE_LDR_RRI5, r2, rGLUE, entryAddr >> 2);
    /* r0 = dalvik pc */
    loadConstant(cUnit, r0, (int) (cUnit->method->insns + mir->offset));
    /* r1 = dalvik pc of following instruction */
    loadConstant(cUnit, r1, (int) (cUnit->method->insns + mir->next->offset));
    newLIR1(cUnit, ARMV5TE_BLX_R, r2);
}


/*****************************************************************************/
/*
 * The following are the first-level codegen routines that analyze the format
 * of each bytecode then either dispatch special purpose codegen routines
 * or produce corresponding Thumb instructions directly.
 */

static bool handleFmt10t_Fmt20t_Fmt30t(CompilationUnit *cUnit, MIR *mir,
                                       BasicBlock *bb, Armv5teLIR *labelList)
{
    /* For OP_GOTO, OP_GOTO_16, and OP_GOTO_32 */
    genUnconditionalBranch(cUnit, &labelList[bb->taken->id]);
    return false;
}

static bool handleFmt10x(CompilationUnit *cUnit, MIR *mir)
{
    OpCode dalvikOpCode = mir->dalvikInsn.opCode;
    if (((dalvikOpCode >= OP_UNUSED_3E) && (dalvikOpCode <= OP_UNUSED_43)) ||
        ((dalvikOpCode >= OP_UNUSED_E3) && (dalvikOpCode <= OP_UNUSED_EC))) {
        LOGE("Codegen: got unused opcode 0x%x\n",dalvikOpCode);
        return true;
    }
    switch (dalvikOpCode) {
        case OP_RETURN_VOID:
            genReturnCommon(cUnit,mir);
            break;
        case OP_UNUSED_73:
        case OP_UNUSED_79:
        case OP_UNUSED_7A:
            LOGE("Codegen: got unused opcode 0x%x\n",dalvikOpCode);
            return true;
        case OP_NOP:
            break;
        default:
            return true;
    }
    return false;
}

static bool handleFmt11n_Fmt31i(CompilationUnit *cUnit, MIR *mir)
{
    switch (mir->dalvikInsn.opCode) {
        case OP_CONST:
        case OP_CONST_4:
            loadConstant(cUnit, r0, mir->dalvikInsn.vB);
            storeValue(cUnit, r0, mir->dalvikInsn.vA, r1);
            break;
        case OP_CONST_WIDE_32:
            loadConstant(cUnit, r0, mir->dalvikInsn.vB);
            newLIR3(cUnit, ARMV5TE_ASR, r1, r0, 31);
            storeValuePair(cUnit, r0, r1, mir->dalvikInsn.vA, r2);
            break;
        default:
            return true;
    }
    return false;
}

static bool handleFmt21h(CompilationUnit *cUnit, MIR *mir)
{
    switch (mir->dalvikInsn.opCode) {
        case OP_CONST_HIGH16:
            loadConstant(cUnit, r0, mir->dalvikInsn.vB << 16);
            storeValue(cUnit, r0, mir->dalvikInsn.vA, r1);
            break;
        case OP_CONST_WIDE_HIGH16:
            loadConstant(cUnit, r1, mir->dalvikInsn.vB << 16);
            loadConstant(cUnit, r0, 0);
            storeValuePair(cUnit, r0, r1, mir->dalvikInsn.vA, r2);
            break;
        default:
            return true;
    }
    return false;
}

static bool handleFmt20bc(CompilationUnit *cUnit, MIR *mir)
{
    /* For OP_THROW_VERIFICATION_ERROR */
    genInterpSingleStep(cUnit, mir);
    return false;
}

static bool handleFmt21c_Fmt31c(CompilationUnit *cUnit, MIR *mir)
{
    switch (mir->dalvikInsn.opCode) {
        /*
         * TODO: Verify that we can ignore the resolution check here because
         * it will have already successfully been interpreted once
         */
        case OP_CONST_STRING_JUMBO:
        case OP_CONST_STRING: {
            void *strPtr = (void*)
              (cUnit->method->clazz->pDvmDex->pResStrings[mir->dalvikInsn.vB]);
            assert(strPtr != NULL);
            loadConstant(cUnit, r0, (int) strPtr );
            storeValue(cUnit, r0, mir->dalvikInsn.vA, r1);
            break;
        }
        /*
         * TODO: Verify that we can ignore the resolution check here because
         * it will have already successfully been interpreted once
         */
        case OP_CONST_CLASS: {
            void *classPtr = (void*)
              (cUnit->method->clazz->pDvmDex->pResClasses[mir->dalvikInsn.vB]);
            assert(classPtr != NULL);
            loadConstant(cUnit, r0, (int) classPtr );
            storeValue(cUnit, r0, mir->dalvikInsn.vA, r1);
            break;
        }
        case OP_SGET_OBJECT:
        case OP_SGET_BOOLEAN:
        case OP_SGET_CHAR:
        case OP_SGET_BYTE:
        case OP_SGET_SHORT:
        case OP_SGET: {
            int valOffset = (int)&((struct StaticField*)NULL)->value;
            void *fieldPtr = (void*)
              (cUnit->method->clazz->pDvmDex->pResFields[mir->dalvikInsn.vB]);
            assert(fieldPtr != NULL);
            loadConstant(cUnit, r0,  (int) fieldPtr + valOffset);
            newLIR3(cUnit, ARMV5TE_LDR_RRI5, r0, r0, 0);
            storeValue(cUnit, r0, mir->dalvikInsn.vA, r2);
            break;
        }
        case OP_SGET_WIDE: {
            int valOffset = (int)&((struct StaticField*)NULL)->value;
            void *fieldPtr = (void*)
              (cUnit->method->clazz->pDvmDex->pResFields[mir->dalvikInsn.vB]);
            assert(fieldPtr != NULL);
            loadConstant(cUnit, r2,  (int) fieldPtr + valOffset);
            newLIR2(cUnit, ARMV5TE_LDMIA, r2, (1<<r0 | 1<<r1));
            storeValuePair(cUnit, r0, r1, mir->dalvikInsn.vA, r2);
            break;
        }
        case OP_SPUT_OBJECT:
        case OP_SPUT_BOOLEAN:
        case OP_SPUT_CHAR:
        case OP_SPUT_BYTE:
        case OP_SPUT_SHORT:
        case OP_SPUT: {
            int valOffset = (int)&((struct StaticField*)NULL)->value;
            void *fieldPtr = (void*)
              (cUnit->method->clazz->pDvmDex->pResFields[mir->dalvikInsn.vB]);
            assert(fieldPtr != NULL);
            loadValue(cUnit, mir->dalvikInsn.vA, r0);
            loadConstant(cUnit, r1,  (int) fieldPtr + valOffset);
            newLIR3(cUnit, ARMV5TE_STR_RRI5, r0, r1, 0);
            break;
        }
        case OP_SPUT_WIDE: {
            int valOffset = (int)&((struct StaticField*)NULL)->value;
            void *fieldPtr = (void*)
              (cUnit->method->clazz->pDvmDex->pResFields[mir->dalvikInsn.vB]);
            assert(fieldPtr != NULL);
            loadValuePair(cUnit, mir->dalvikInsn.vA, r0, r1);
            loadConstant(cUnit, r2,  (int) fieldPtr + valOffset);
            newLIR2(cUnit, ARMV5TE_STMIA, r2, (1<<r0 | 1<<r1));
            break;
        }
        case OP_NEW_INSTANCE: {
            ClassObject *classPtr = (void*)
              (cUnit->method->clazz->pDvmDex->pResClasses[mir->dalvikInsn.vB]);
            assert(classPtr != NULL);
            assert(classPtr->status & CLASS_INITIALIZED);
            if ((classPtr->accessFlags & (ACC_INTERFACE|ACC_ABSTRACT)) != 0) {
                /* It's going to throw, just let the interp. deal with it. */
                genInterpSingleStep(cUnit, mir);
                return false;
            }
            loadConstant(cUnit, r0, (int) classPtr);
            loadConstant(cUnit, r4PC, (int)dvmAllocObject);
            genExportPC(cUnit, mir, r2, r3 );
            loadConstant(cUnit, r1, ALLOC_DONT_TRACK);
            newLIR1(cUnit, ARMV5TE_BLX_R, r4PC);
            /*
             * TODO: As coded, we'll bail and reinterpret on alloc failure.
             * Need a general mechanism to bail to thrown exception code.
             */
            genNullCheck(cUnit, r0, mir->offset, NULL);
            storeValue(cUnit, r0, mir->dalvikInsn.vA, r1);
            break;
        }
        case OP_CHECK_CAST: {
            ClassObject *classPtr =
              (cUnit->method->clazz->pDvmDex->pResClasses[mir->dalvikInsn.vB]);
            loadConstant(cUnit, r1, (int) classPtr );
            loadValue(cUnit, mir->dalvikInsn.vA, r0);  /* Ref */
            /*
             * TODO - in theory classPtr should be resoved by the time this
             * instruction made into a trace, but we are seeing NULL at runtime
             * so this check is temporarily used as a workaround.
             */
            Armv5teLIR * pcrLabel = genNullCheck(cUnit, r1, mir->offset, NULL);
            newLIR2(cUnit, ARMV5TE_CMP_RI8, r0, 0);    /* Null? */
            Armv5teLIR *branch1 =
                newLIR2(cUnit, ARMV5TE_B_COND, 4, ARM_COND_EQ);
            /* r0 now contains object->clazz */
            newLIR3(cUnit, ARMV5TE_LDR_RRI5, r0, r0,
                    offsetof(Object, clazz) >> 2);
            loadConstant(cUnit, r4PC, (int)dvmInstanceofNonTrivial);
            newLIR2(cUnit, ARMV5TE_CMP_RR, r0, r1);
            Armv5teLIR *branch2 =
                newLIR2(cUnit, ARMV5TE_B_COND, 2, ARM_COND_EQ);
            newLIR1(cUnit, ARMV5TE_BLX_R, r4PC);
            /* check cast failed - punt to the interpreter */
            genNullCheck(cUnit, r0, mir->offset, pcrLabel);
            /* check cast passed - branch target here */
            Armv5teLIR *target = newLIR0(cUnit, ARMV5TE_PSEUDO_TARGET_LABEL);
            branch1->generic.target = (LIR *)target;
            branch2->generic.target = (LIR *)target;
            break;
        }
        default:
            return true;
    }
    return false;
}

static bool handleFmt11x(CompilationUnit *cUnit, MIR *mir)
{
    OpCode dalvikOpCode = mir->dalvikInsn.opCode;
    switch (dalvikOpCode) {
        case OP_MOVE_EXCEPTION: {
            int offset = offsetof(InterpState, self);
            int exOffset = offsetof(Thread, exception);
            newLIR3(cUnit, ARMV5TE_LDR_RRI5, r0, rGLUE, offset >> 2);
            newLIR3(cUnit, ARMV5TE_LDR_RRI5, r1, r0, exOffset >> 2);
            storeValue(cUnit, r1, mir->dalvikInsn.vA, r0);
           break;
        }
        case OP_MOVE_RESULT:
        case OP_MOVE_RESULT_OBJECT: {
            int offset = offsetof(InterpState, retval);
            newLIR3(cUnit, ARMV5TE_LDR_RRI5, r0, rGLUE, offset >> 2);
            storeValue(cUnit, r0, mir->dalvikInsn.vA, r1);
            break;
        }
        case OP_MOVE_RESULT_WIDE: {
            int offset = offsetof(InterpState, retval);
            newLIR3(cUnit, ARMV5TE_LDR_RRI5, r0, rGLUE, offset >> 2);
            newLIR3(cUnit, ARMV5TE_LDR_RRI5, r1, rGLUE, (offset >> 2)+1);
            storeValuePair(cUnit, r0, r1, mir->dalvikInsn.vA, r2);
            break;
        }
        case OP_RETURN_WIDE: {
            loadValuePair(cUnit, mir->dalvikInsn.vA, r0, r1);
            int offset = offsetof(InterpState, retval);
            newLIR3(cUnit, ARMV5TE_STR_RRI5, r0, rGLUE, offset >> 2);
            newLIR3(cUnit, ARMV5TE_STR_RRI5, r1, rGLUE, (offset >> 2)+1);
            genReturnCommon(cUnit,mir);
            break;
        }
        case OP_RETURN:
        case OP_RETURN_OBJECT: {
            loadValue(cUnit, mir->dalvikInsn.vA, r0);
            int offset = offsetof(InterpState, retval);
            newLIR3(cUnit, ARMV5TE_STR_RRI5, r0, rGLUE, offset >> 2);
            genReturnCommon(cUnit,mir);
            break;
        }
        /*
         * TODO-VERIFY: May be playing a bit fast and loose here.  As coded,
         * a failure on lock/unlock will cause us to revert to the interpeter
         * to try again. This means we essentially ignore the first failure on
         * the assumption that the interpreter will correctly handle the 2nd.
         */
        case OP_MONITOR_ENTER:
        case OP_MONITOR_EXIT: {
            int offset = offsetof(InterpState, self);
            loadValue(cUnit, mir->dalvikInsn.vA, r1);
            newLIR3(cUnit, ARMV5TE_LDR_RRI5, r0, rGLUE, offset >> 2);
            if (dalvikOpCode == OP_MONITOR_ENTER) {
                loadConstant(cUnit, r2, (int)dvmLockObject);
            } else {
                loadConstant(cUnit, r2, (int)dvmUnlockObject);
            }
          /*
           * TODO-VERIFY: Note that we're not doing an EXPORT_PC, as
           * Lock/unlock won't throw, and this code does not support
           * DEADLOCK_PREDICTION or MONITOR_TRACKING.  Should it?
           */
            genNullCheck(cUnit, r1, mir->offset, NULL);
            /* Do the call */
            newLIR1(cUnit, ARMV5TE_BLX_R, r2);
            break;
        }
        case OP_THROW: {
            genInterpSingleStep(cUnit, mir);
            break;
        }
        default:
            return true;
    }
    return false;
}

static bool handleFmt12x(CompilationUnit *cUnit, MIR *mir)
{
    OpCode opCode = mir->dalvikInsn.opCode;
    int vSrc1Dest = mir->dalvikInsn.vA;
    int vSrc2 = mir->dalvikInsn.vB;

    /* TODO - find the proper include file to declare these */
    float  __aeabi_i2f(  int op1 );
    int    __aeabi_f2iz( float op1 );
    float  __aeabi_d2f(  double op1 );
    double __aeabi_f2d(  float op1 );
    double __aeabi_i2d(  int op1 );
    int    __aeabi_d2iz( double op1 );
    long   __aeabi_f2lz( float op1 );
    float  __aeabi_l2f(  long op1 );
    long   __aeabi_d2lz( double op1 );
    double __aeabi_l2d(  long op1 );

    if ( (opCode >= OP_ADD_INT_2ADDR) && (opCode <= OP_REM_DOUBLE_2ADDR)) {
        return genArithOp( cUnit, mir );
    }

    switch (opCode) {
        case OP_INT_TO_FLOAT:
            return genConversion(cUnit, mir, (void*)__aeabi_i2f, 1, 1);
        case OP_FLOAT_TO_INT:
            return genConversion(cUnit, mir, (void*)__aeabi_f2iz, 1, 1);
        case OP_DOUBLE_TO_FLOAT:
            return genConversion(cUnit, mir, (void*)__aeabi_d2f, 2, 1);
        case OP_FLOAT_TO_DOUBLE:
            return genConversion(cUnit, mir, (void*)__aeabi_f2d, 1, 2);
        case OP_INT_TO_DOUBLE:
            return genConversion(cUnit, mir, (void*)__aeabi_i2d, 1, 2);
        case OP_DOUBLE_TO_INT:
            return genConversion(cUnit, mir, (void*)__aeabi_d2iz, 2, 1);
        case OP_FLOAT_TO_LONG:
            return genConversion(cUnit, mir, (void*)__aeabi_f2lz, 1, 2);
        case OP_LONG_TO_FLOAT:
            return genConversion(cUnit, mir, (void*)__aeabi_l2f, 2, 1);
        case OP_DOUBLE_TO_LONG:
            return genConversion(cUnit, mir, (void*)__aeabi_d2lz, 2, 2);
        case OP_LONG_TO_DOUBLE:
            return genConversion(cUnit, mir, (void*)__aeabi_l2d, 2, 2);
        case OP_NEG_INT:
        case OP_NOT_INT:
            return genArithOpInt(cUnit, mir, vSrc1Dest, vSrc1Dest, vSrc2);
        case OP_NEG_LONG:
        case OP_NOT_LONG:
            return genArithOpLong(cUnit,mir, vSrc1Dest, vSrc1Dest, vSrc2);
        case OP_NEG_FLOAT:
            return genArithOpFloat(cUnit,mir,vSrc1Dest,vSrc1Dest,vSrc2);
        case OP_NEG_DOUBLE:
            return genArithOpDouble(cUnit,mir,vSrc1Dest,vSrc1Dest,vSrc2);
        case OP_MOVE_WIDE:
            loadValuePair(cUnit, mir->dalvikInsn.vB, r0, r1);
            storeValuePair(cUnit, r0, r1, mir->dalvikInsn.vA, r2);
            break;
        case OP_INT_TO_LONG:
            loadValue(cUnit, mir->dalvikInsn.vB, r0);
            newLIR3(cUnit, ARMV5TE_ASR, r1, r0, 31);
            storeValuePair(cUnit, r0, r1, mir->dalvikInsn.vA, r2);
            break;
        case OP_MOVE:
        case OP_MOVE_OBJECT:
        case OP_LONG_TO_INT:
            loadValue(cUnit, vSrc2, r0);
            storeValue(cUnit, r0, vSrc1Dest, r1);
            break;
        case OP_INT_TO_BYTE:
            loadValue(cUnit, vSrc2, r0);
            newLIR3(cUnit, ARMV5TE_LSL, r0, r0, 24);
            newLIR3(cUnit, ARMV5TE_ASR, r0, r0, 24);
            storeValue(cUnit, r0, vSrc1Dest, r1);
            break;
        case OP_INT_TO_SHORT:
            loadValue(cUnit, vSrc2, r0);
            newLIR3(cUnit, ARMV5TE_LSL, r0, r0, 16);
            newLIR3(cUnit, ARMV5TE_ASR, r0, r0, 16);
            storeValue(cUnit, r0, vSrc1Dest, r1);
            break;
        case OP_INT_TO_CHAR:
            loadValue(cUnit, vSrc2, r0);
            newLIR3(cUnit, ARMV5TE_LSL, r0, r0, 16);
            newLIR3(cUnit, ARMV5TE_LSR, r0, r0, 16);
            storeValue(cUnit, r0, vSrc1Dest, r1);
            break;
        case OP_ARRAY_LENGTH: {
            int lenOffset = offsetof(ArrayObject, length);
            loadValue(cUnit, vSrc2, r0);
            genNullCheck(cUnit, r0, mir->offset, NULL);
            newLIR3(cUnit, ARMV5TE_LDR_RRI5, r0, r0, lenOffset >> 2);
            storeValue(cUnit, r0, vSrc1Dest, r1);
            break;
        }
        default:
            return true;
    }
    return false;
}

static bool handleFmt21s(CompilationUnit *cUnit, MIR *mir)
{
    OpCode dalvikOpCode = mir->dalvikInsn.opCode;
    /* It takes few instructions to handle OP_CONST_WIDE_16 inline */
    if (dalvikOpCode == OP_CONST_WIDE_16) {
        int rDest = mir->dalvikInsn.vA;
        int BBBB = mir->dalvikInsn.vB;
        int rLow = r0, rHigh = r1;
        if (BBBB == 0) {
            newLIR2(cUnit, ARMV5TE_MOV_IMM, rLow, 0);
            rHigh = rLow;
        } else if (BBBB > 0 && BBBB <= 255) {
            /* rLow = ssssBBBB */
            newLIR2(cUnit, ARMV5TE_MOV_IMM, rLow, BBBB);
            /* rHigh = 0 */
            newLIR2(cUnit, ARMV5TE_MOV_IMM, rHigh, 0);
        } else {
            loadConstant(cUnit, rLow, BBBB);
            /*
             * arithmetic-shift-right 32 bits to get the high half of long
             * [63..32]
             */
            newLIR3(cUnit, ARMV5TE_ASR, rHigh, rLow, 0);
        }

        /* Save the long values to the specified Dalvik register pair */
        /*
         * If rDest is no greater than 30, use two "str rd, [rFP + immed_5]"
         * instructions to store the results. Effective address is
         * rFP + immed_5 << 2.
         */
        if (rDest < 31) {
            newLIR3(cUnit, ARMV5TE_STR_RRI5, rLow, rFP, rDest);
            newLIR3(cUnit, ARMV5TE_STR_RRI5, rHigh, rFP, rDest+1);
        } else {
          /*
           * Otherwise just load the frame offset from the constant pool and add
           * it to rFP. Then use stmia to store the results to the specified
           * register pair.
           */
            /* Need to replicate the content in r0 to r1 */
            if (rLow == rHigh) {
                newLIR3(cUnit, ARMV5TE_ADD_RRI3, rLow+1, rLow, 0);
            }
            /* load the rFP offset into r2 */
            loadConstant(cUnit, r2, rDest*4);
            newLIR3(cUnit, ARMV5TE_ADD_RRR, r2, rFP, r2);
            newLIR2(cUnit, ARMV5TE_STMIA, r2, (1<<r0 | 1 << r1));
        }
    } else if (dalvikOpCode == OP_CONST_16) {
        int rDest = mir->dalvikInsn.vA;
        int BBBB = mir->dalvikInsn.vB;
        if (BBBB >= 0 && BBBB <= 255) {
            /* r0 = BBBB */
            newLIR2(cUnit, ARMV5TE_MOV_IMM, r0, BBBB);
        } else {
            loadConstant(cUnit, r0, BBBB);
        }

        /* Save the constant to the specified Dalvik register */
        /*
         * If rDest is no greater than 31, effective address is
         * rFP + immed_5 << 2.
         */
        if (rDest < 32) {
            newLIR3(cUnit, ARMV5TE_STR_RRI5, r0, rFP, rDest);
        } else {
          /*
           * Otherwise just load the frame offset from the constant pool and add
           * it to rFP. Then use stmia to store the results to the specified
           * register pair.
           */
            /* load the rFP offset into r2 */
            loadConstant(cUnit, r2, rDest*4);
            newLIR3(cUnit, ARMV5TE_ADD_RRR, r2, rFP, r2);
            newLIR3(cUnit, ARMV5TE_STR_RRI5, r0, r2, 0);
        }
    } else {
        return true;
    }
    return false;
}

/* Compare agaist zero */
static bool handleFmt21t(CompilationUnit *cUnit, MIR *mir, BasicBlock *bb,
                         Armv5teLIR *labelList)
{
    OpCode dalvikOpCode = mir->dalvikInsn.opCode;
    Armv5teConditionCode cond;

    loadValue(cUnit, mir->dalvikInsn.vA, r0);
    newLIR2(cUnit, ARMV5TE_CMP_RI8, r0, 0);

    switch (dalvikOpCode) {
        case OP_IF_EQZ:
            cond = ARM_COND_EQ;
            break;
        case OP_IF_NEZ:
            cond = ARM_COND_NE;
            break;
        case OP_IF_LTZ:
            cond = ARM_COND_LT;
            break;
        case OP_IF_GEZ:
            cond = ARM_COND_GE;
            break;
        case OP_IF_GTZ:
            cond = ARM_COND_GT;
            break;
        case OP_IF_LEZ:
            cond = ARM_COND_LE;
            break;
        default:
            cond = 0;
            LOGE("Unexpected opcode (%d) for Fmt21t\n", dalvikOpCode);
            dvmAbort();
    }
    genConditionalBranch(cUnit, cond, &labelList[bb->taken->id]);
    /* This mostly likely will be optimized away in a later phase */
    genUnconditionalBranch(cUnit, &labelList[bb->fallThrough->id]);
    return false;
}

static bool handleFmt22b_Fmt22s(CompilationUnit *cUnit, MIR *mir)
{
    OpCode dalvikOpCode = mir->dalvikInsn.opCode;
    int vSrc = mir->dalvikInsn.vB;
    int vDest = mir->dalvikInsn.vA;
    int lit = mir->dalvikInsn.vC;
    int armOp;

    /* TODO: find the proper .h file to declare these */
    int __aeabi_idivmod(int op1, int op2);
    int __aeabi_idiv(int op1, int op2);

    switch (dalvikOpCode) {
        case OP_ADD_INT_LIT8:
        case OP_ADD_INT_LIT16:
            loadValue(cUnit, vSrc, r0);
            if (lit <= 255 && lit >= 0) {
                newLIR2(cUnit, ARMV5TE_ADD_RI8, r0, lit);
                storeValue(cUnit, r0, vDest, r1);
            } else if (lit >= -255 && lit <= 0) {
                /* Convert to a small constant subtraction */
                newLIR2(cUnit, ARMV5TE_SUB_RI8, r0, -lit);
                storeValue(cUnit, r0, vDest, r1);
            } else {
                loadConstant(cUnit, r1, lit);
                genBinaryOp(cUnit, vDest, ARMV5TE_ADD_RRR);
            }
            break;

        case OP_RSUB_INT_LIT8:
        case OP_RSUB_INT:
            loadValue(cUnit, vSrc, r1);
            loadConstant(cUnit, r0, lit);
            genBinaryOp(cUnit, vDest, ARMV5TE_SUB_RRR);
            break;

        case OP_MUL_INT_LIT8:
        case OP_MUL_INT_LIT16:
        case OP_AND_INT_LIT8:
        case OP_AND_INT_LIT16:
        case OP_OR_INT_LIT8:
        case OP_OR_INT_LIT16:
        case OP_XOR_INT_LIT8:
        case OP_XOR_INT_LIT16:
            loadValue(cUnit, vSrc, r0);
            loadConstant(cUnit, r1, lit);
            switch (dalvikOpCode) {
                case OP_MUL_INT_LIT8:
                case OP_MUL_INT_LIT16:
                    armOp = ARMV5TE_MUL;
                    break;
                case OP_AND_INT_LIT8:
                case OP_AND_INT_LIT16:
                    armOp = ARMV5TE_AND_RR;
                    break;
                case OP_OR_INT_LIT8:
                case OP_OR_INT_LIT16:
                    armOp = ARMV5TE_ORR;
                    break;
                case OP_XOR_INT_LIT8:
                case OP_XOR_INT_LIT16:
                    armOp = ARMV5TE_EOR;
                    break;
                default:
                    dvmAbort();
            }
            genBinaryOp(cUnit, vDest, armOp);
            break;

        case OP_SHL_INT_LIT8:
        case OP_SHR_INT_LIT8:
        case OP_USHR_INT_LIT8:
            loadValue(cUnit, vSrc, r0);
            switch (dalvikOpCode) {
                case OP_SHL_INT_LIT8:
                    armOp = ARMV5TE_LSL;
                    break;
                case OP_SHR_INT_LIT8:
                    armOp = ARMV5TE_ASR;
                    break;
                case OP_USHR_INT_LIT8:
                    armOp = ARMV5TE_LSR;
                    break;
                default: dvmAbort();
            }
            newLIR3(cUnit, armOp, r0, r0, lit);
            storeValue(cUnit, r0, vDest, r1);
            break;

        case OP_DIV_INT_LIT8:
        case OP_DIV_INT_LIT16:
            if (lit == 0) {
                /* Let the interpreter deal with div by 0 */
                genInterpSingleStep(cUnit, mir);
                return false;
            }
            loadConstant(cUnit, r2, (int)__aeabi_idiv);
            loadConstant(cUnit, r1, lit);
            loadValue(cUnit, vSrc, r0);
            newLIR1(cUnit, ARMV5TE_BLX_R, r2);
            storeValue(cUnit, r0, vDest, r2);
            break;

        case OP_REM_INT_LIT8:
        case OP_REM_INT_LIT16:
            if (lit == 0) {
                /* Let the interpreter deal with div by 0 */
                genInterpSingleStep(cUnit, mir);
                return false;
            }
            loadConstant(cUnit, r2, (int)__aeabi_idivmod);
            loadConstant(cUnit, r1, lit);
            loadValue(cUnit, vSrc, r0);
            newLIR1(cUnit, ARMV5TE_BLX_R, r2);
            storeValue(cUnit, r1, vDest, r2);
            break;
        default:
            return true;
    }
    return false;
}

static bool handleFmt22c(CompilationUnit *cUnit, MIR *mir)
{
    OpCode dalvikOpCode = mir->dalvikInsn.opCode;
    int fieldOffset;

    if (dalvikOpCode >= OP_IGET && dalvikOpCode <= OP_IPUT_SHORT) {
        InstField *pInstField = (InstField *)
            cUnit->method->clazz->pDvmDex->pResFields[mir->dalvikInsn.vC];
        int fieldOffset;

        assert(pInstField != NULL);
        fieldOffset = pInstField->byteOffset;
    } else {
        /* To make the compiler happy */
        fieldOffset = 0;
    }
    switch (dalvikOpCode) {
        /*
         * TODO: I may be assuming too much here.
         * Verify what is known at JIT time.
         */
        case OP_NEW_ARRAY: {
            void *classPtr = (void*)
              (cUnit->method->clazz->pDvmDex->pResClasses[mir->dalvikInsn.vC]);
            assert(classPtr != NULL);
            loadValue(cUnit, mir->dalvikInsn.vB, r1);  /* Len */
            loadConstant(cUnit, r0, (int) classPtr );
            loadConstant(cUnit, r4PC, (int)dvmAllocArrayByClass);
            Armv5teLIR *pcrLabel =
                genRegImmCheck(cUnit, ARM_COND_MI, r1, 0, mir->offset, NULL);
            genExportPC(cUnit, mir, r2, r3 );
            newLIR2(cUnit, ARMV5TE_MOV_IMM,r2,ALLOC_DONT_TRACK);
            newLIR1(cUnit, ARMV5TE_BLX_R, r4PC);
            /*
             * TODO: As coded, we'll bail and reinterpret on alloc failure.
             * Need a general mechanism to bail to thrown exception code.
             */
            genNullCheck(cUnit, r0, mir->offset, pcrLabel);
            storeValue(cUnit, r0, mir->dalvikInsn.vA, r1);
            break;
        }
        /*
         * TODO: I may be assuming too much here.
         * Verify what is known at JIT time.
         */
        case OP_INSTANCE_OF: {
            ClassObject *classPtr =
              (cUnit->method->clazz->pDvmDex->pResClasses[mir->dalvikInsn.vC]);
            assert(classPtr != NULL);
            loadValue(cUnit, mir->dalvikInsn.vB, r1);  /* Ref */
            loadConstant(cUnit, r2, (int) classPtr );
            loadConstant(cUnit, r0, 1);                /* Assume true */
            newLIR2(cUnit, ARMV5TE_CMP_RI8, r1, 0);    /* Null? */
            Armv5teLIR *branch1 = newLIR2(cUnit, ARMV5TE_B_COND, 4,
                                          ARM_COND_EQ);
            /* r1 now contains object->clazz */
            newLIR3(cUnit, ARMV5TE_LDR_RRI5, r1, r1,
                    offsetof(Object, clazz) >> 2);
            loadConstant(cUnit, r4PC, (int)dvmInstanceofNonTrivial);
            newLIR2(cUnit, ARMV5TE_CMP_RR, r1, r2);
            Armv5teLIR *branch2 = newLIR2(cUnit, ARMV5TE_B_COND, 2,
                                          ARM_COND_EQ);
            newLIR2(cUnit, ARMV5TE_MOV_RR, r0, r1);
            newLIR2(cUnit, ARMV5TE_MOV_RR, r1, r2);
            newLIR1(cUnit, ARMV5TE_BLX_R, r4PC);
            /* branch target here */
            Armv5teLIR *target = newLIR0(cUnit, ARMV5TE_PSEUDO_TARGET_LABEL);
            storeValue(cUnit, r0, mir->dalvikInsn.vA, r1);
            branch1->generic.target = (LIR *)target;
            branch2->generic.target = (LIR *)target;
            break;
        }
        case OP_IGET_WIDE:
            genIGetWide(cUnit, mir, fieldOffset);
            break;
        case OP_IGET:
        case OP_IGET_OBJECT:
            genIGet(cUnit, mir, ARMV5TE_LDR_RRR, fieldOffset);
            break;
        case OP_IGET_BOOLEAN:
            genIGet(cUnit, mir, ARMV5TE_LDRB_RRR, fieldOffset);
            break;
        case OP_IGET_BYTE:
            genIGet(cUnit, mir, ARMV5TE_LDRSB_RRR, fieldOffset);
            break;
        case OP_IGET_CHAR:
            genIGet(cUnit, mir, ARMV5TE_LDRH_RRR, fieldOffset);
            break;
        case OP_IGET_SHORT:
            genIGet(cUnit, mir, ARMV5TE_LDRSH_RRR, fieldOffset);
            break;
        case OP_IPUT_WIDE:
            genIPutWide(cUnit, mir, fieldOffset);
            break;
        case OP_IPUT:
        case OP_IPUT_OBJECT:
            genIPut(cUnit, mir, ARMV5TE_STR_RRR, fieldOffset);
            break;
        case OP_IPUT_SHORT:
        case OP_IPUT_CHAR:
            genIPut(cUnit, mir, ARMV5TE_STRH_RRR, fieldOffset);
            break;
        case OP_IPUT_BYTE:
        case OP_IPUT_BOOLEAN:
            genIPut(cUnit, mir, ARMV5TE_STRB_RRR, fieldOffset);
            break;
        default:
            return true;
    }
    return false;
}

static bool handleFmt22cs(CompilationUnit *cUnit, MIR *mir)
{
    OpCode dalvikOpCode = mir->dalvikInsn.opCode;
    int fieldOffset =  mir->dalvikInsn.vC;
    switch (dalvikOpCode) {
        case OP_IGET_QUICK:
        case OP_IGET_OBJECT_QUICK:
            genIGet(cUnit, mir, ARMV5TE_LDR_RRR, fieldOffset);
            break;
        case OP_IPUT_QUICK:
        case OP_IPUT_OBJECT_QUICK:
            genIPut(cUnit, mir, ARMV5TE_STR_RRR, fieldOffset);
            break;
        case OP_IGET_WIDE_QUICK:
            genIGetWide(cUnit, mir, fieldOffset);
            break;
        case OP_IPUT_WIDE_QUICK:
            genIPutWide(cUnit, mir, fieldOffset);
            break;
        default:
            return true;
    }
    return false;

}

/* Compare agaist zero */
static bool handleFmt22t(CompilationUnit *cUnit, MIR *mir, BasicBlock *bb,
                         Armv5teLIR *labelList)
{
    OpCode dalvikOpCode = mir->dalvikInsn.opCode;
    Armv5teConditionCode cond;

    loadValue(cUnit, mir->dalvikInsn.vA, r0);
    loadValue(cUnit, mir->dalvikInsn.vB, r1);
    newLIR2(cUnit, ARMV5TE_CMP_RR, r0, r1);

    switch (dalvikOpCode) {
        case OP_IF_EQ:
            cond = ARM_COND_EQ;
            break;
        case OP_IF_NE:
            cond = ARM_COND_NE;
            break;
        case OP_IF_LT:
            cond = ARM_COND_LT;
            break;
        case OP_IF_GE:
            cond = ARM_COND_GE;
            break;
        case OP_IF_GT:
            cond = ARM_COND_GT;
            break;
        case OP_IF_LE:
            cond = ARM_COND_LE;
            break;
        default:
            cond = 0;
            LOGE("Unexpected opcode (%d) for Fmt22t\n", dalvikOpCode);
            dvmAbort();
    }
    genConditionalBranch(cUnit, cond, &labelList[bb->taken->id]);
    /* This mostly likely will be optimized away in a later phase */
    genUnconditionalBranch(cUnit, &labelList[bb->fallThrough->id]);
    return false;
}

static bool handleFmt22x_Fmt32x(CompilationUnit *cUnit, MIR *mir)
{
    OpCode opCode = mir->dalvikInsn.opCode;
    int vSrc1Dest = mir->dalvikInsn.vA;
    int vSrc2 = mir->dalvikInsn.vB;

    switch (opCode) {
        case OP_MOVE_16:
        case OP_MOVE_OBJECT_16:
        case OP_MOVE_FROM16:
        case OP_MOVE_OBJECT_FROM16:
            loadValue(cUnit, vSrc2, r0);
            storeValue(cUnit, r0, vSrc1Dest, r1);
            break;
        case OP_MOVE_WIDE_16:
        case OP_MOVE_WIDE_FROM16:
            loadValuePair(cUnit, vSrc2, r0, r1);
            storeValuePair(cUnit, r0, r1, vSrc1Dest, r2);
            break;
        default:
            return true;
    }
    return false;
}

static bool handleFmt23x(CompilationUnit *cUnit, MIR *mir)
{
    OpCode opCode = mir->dalvikInsn.opCode;
    int vA = mir->dalvikInsn.vA;
    int vB = mir->dalvikInsn.vB;
    int vC = mir->dalvikInsn.vC;

    if ( (opCode >= OP_ADD_INT) && (opCode <= OP_REM_DOUBLE)) {
        return genArithOp( cUnit, mir );
    }

    switch (opCode) {
        case OP_CMP_LONG:
            loadValuePair(cUnit,vB, r0, r1);
            loadValuePair(cUnit, vC, r2, r3);
            genDispatchToHandler(cUnit, TEMPLATE_CMP_LONG);
            storeValue(cUnit, r0, vA, r1);
            break;
        case OP_CMPL_FLOAT:
            loadValue(cUnit, vB, r0);
            loadValue(cUnit, vC, r1);
            genDispatchToHandler(cUnit, TEMPLATE_CMPL_FLOAT);
            storeValue(cUnit, r0, vA, r1);
            break;
        case OP_CMPG_FLOAT:
            loadValue(cUnit, vB, r0);
            loadValue(cUnit, vC, r1);
            genDispatchToHandler(cUnit, TEMPLATE_CMPG_FLOAT);
            storeValue(cUnit, r0, vA, r1);
            break;
        case OP_CMPL_DOUBLE:
            loadValueAddress(cUnit, vB, r0);
            loadValueAddress(cUnit, vC, r1);
            genDispatchToHandler(cUnit, TEMPLATE_CMPL_DOUBLE);
            storeValue(cUnit, r0, vA, r1);
            break;
        case OP_CMPG_DOUBLE:
            loadValueAddress(cUnit, vB, r0);
            loadValueAddress(cUnit, vC, r1);
            genDispatchToHandler(cUnit, TEMPLATE_CMPG_DOUBLE);
            storeValue(cUnit, r0, vA, r1);
            break;
        case OP_AGET_WIDE:
            genArrayGet(cUnit, mir, ARMV5TE_LDR_RRR, vB, vC, vA, 3);
            break;
        case OP_AGET:
        case OP_AGET_OBJECT:
            genArrayGet(cUnit, mir, ARMV5TE_LDR_RRR, vB, vC, vA, 2);
            break;
        case OP_AGET_BOOLEAN:
            genArrayGet(cUnit, mir, ARMV5TE_LDRB_RRR, vB, vC, vA, 0);
            break;
        case OP_AGET_BYTE:
            genArrayGet(cUnit, mir, ARMV5TE_LDRSB_RRR, vB, vC, vA, 0);
            break;
        case OP_AGET_CHAR:
            genArrayGet(cUnit, mir, ARMV5TE_LDRH_RRR, vB, vC, vA, 1);
            break;
        case OP_AGET_SHORT:
            genArrayGet(cUnit, mir, ARMV5TE_LDRSH_RRR, vB, vC, vA, 1);
            break;
        case OP_APUT_WIDE:
            genArrayPut(cUnit, mir, ARMV5TE_STR_RRR, vB, vC, vA, 3);
            break;
        case OP_APUT:
        case OP_APUT_OBJECT:
            genArrayPut(cUnit, mir, ARMV5TE_STR_RRR, vB, vC, vA, 2);
            break;
        case OP_APUT_SHORT:
        case OP_APUT_CHAR:
            genArrayPut(cUnit, mir, ARMV5TE_STRH_RRR, vB, vC, vA, 1);
            break;
        case OP_APUT_BYTE:
        case OP_APUT_BOOLEAN:
            genArrayPut(cUnit, mir, ARMV5TE_STRB_RRR, vB, vC, vA, 0);
            break;
        default:
            return true;
    }
    return false;
}

static bool handleFmt31t(CompilationUnit *cUnit, MIR *mir)
{
    OpCode dalvikOpCode = mir->dalvikInsn.opCode;
    switch (dalvikOpCode) {
        case OP_FILL_ARRAY_DATA: {
            loadConstant(cUnit, r4PC, (int)dvmInterpHandleFillArrayData);
            loadValue(cUnit, mir->dalvikInsn.vA, r0);
            loadConstant(cUnit, r1, (mir->dalvikInsn.vB << 1) +
                 (int) (cUnit->method->insns + mir->offset));
            genExportPC(cUnit, mir, r2, r3 );
            newLIR1(cUnit, ARMV5TE_BLX_R, r4PC);
            genNullCheck(cUnit, r0, mir->offset, NULL);
            break;
        }
        /*
         * TODO
         * - Add a 1 to 3-entry per-location cache here to completely
         *   bypass the dvmInterpHandle[Packed/Sparse]Switch call w/ chaining
         * - Use out-of-line handlers for both of these
         */
        case OP_PACKED_SWITCH:
        case OP_SPARSE_SWITCH: {
            if (dalvikOpCode == OP_PACKED_SWITCH) {
                loadConstant(cUnit, r4PC, (int)dvmInterpHandlePackedSwitch);
            } else {
                loadConstant(cUnit, r4PC, (int)dvmInterpHandleSparseSwitch);
            }
            loadValue(cUnit, mir->dalvikInsn.vA, r1);
            loadConstant(cUnit, r0, (mir->dalvikInsn.vB << 1) +
                 (int) (cUnit->method->insns + mir->offset));
            newLIR1(cUnit, ARMV5TE_BLX_R, r4PC);
            loadConstant(cUnit, r1, (int)(cUnit->method->insns + mir->offset));
            newLIR3(cUnit, ARMV5TE_LDR_RRI5, r2, rGLUE,
                offsetof(InterpState, jitToInterpEntries.dvmJitToInterpNoChain)
                    >> 2);
            newLIR3(cUnit, ARMV5TE_ADD_RRR, r0, r0, r0);
            newLIR3(cUnit, ARMV5TE_ADD_RRR, r4PC, r0, r1);
            newLIR1(cUnit, ARMV5TE_BLX_R, r2);
            break;
        }
        default:
            return true;
    }
    return false;
}

static bool handleFmt35c_3rc(CompilationUnit *cUnit, MIR *mir, BasicBlock *bb,
                             Armv5teLIR *labelList)
{
    Armv5teLIR *retChainingCell = &labelList[bb->fallThrough->id];
    Armv5teLIR *pcrLabel = NULL;

    DecodedInstruction *dInsn = &mir->dalvikInsn;
    switch (mir->dalvikInsn.opCode) {
        /*
         * calleeMethod = this->clazz->vtable[
         *     method->clazz->pDvmDex->pResMethods[BBBB]->methodIndex
         * ]
         */
        case OP_INVOKE_VIRTUAL:
        case OP_INVOKE_VIRTUAL_RANGE: {
            int methodIndex =
                cUnit->method->clazz->pDvmDex->pResMethods[dInsn->vB]->
                methodIndex;

            if (mir->dalvikInsn.opCode == OP_INVOKE_VIRTUAL)
                genProcessArgsNoRange(cUnit, mir, dInsn, &pcrLabel);
            else
                genProcessArgsRange(cUnit, mir, dInsn, &pcrLabel);

            /* r0 now contains this->clazz */
            newLIR3(cUnit, ARMV5TE_LDR_RRI5, r0, r0,
                    offsetof(Object, clazz) >> 2);
            /* r1 = &retChainingCell */
            Armv5teLIR *addrRetChain = newLIR2(cUnit, ARMV5TE_ADD_PC_REL,
                                                   r1, 0);
            /* r4PC = dalvikCallsite */
            loadConstant(cUnit, r4PC,
                         (int) (cUnit->method->insns + mir->offset));

            /* r0 now contains this->clazz->vtable */
            newLIR3(cUnit, ARMV5TE_LDR_RRI5, r0, r0,
                    offsetof(ClassObject, vtable) >> 2);
            addrRetChain->generic.target = (LIR *) retChainingCell;

            if (methodIndex < 32) {
                newLIR3(cUnit, ARMV5TE_LDR_RRI5, r0, r0, methodIndex);
            } else {
                loadConstant(cUnit, r7, methodIndex<<2);
                newLIR3(cUnit, ARMV5TE_LDR_RRR, r0, r0, r7);
            }

            /*
             * r0 = calleeMethod,
             * r1 = &ChainingCell,
             * r4PC = callsiteDPC,
             */
            genDispatchToHandler(cUnit, TEMPLATE_INVOKE_METHOD_NO_OPT);
#if defined(INVOKE_STATS)
            gDvmJit.invokeNoOpt++;
#endif
            /* Handle exceptions using the interpreter */
            genTrap(cUnit, mir->offset, pcrLabel);
            break;
        }
        /*
         * calleeMethod = method->clazz->super->vtable[method->clazz->pDvmDex
         *                ->pResMethods[BBBB]->methodIndex]
         */
        /* TODO - not excersized in RunPerf.jar */
        case OP_INVOKE_SUPER:
        case OP_INVOKE_SUPER_RANGE: {
            int mIndex = cUnit->method->clazz->pDvmDex->
                pResMethods[dInsn->vB]->methodIndex;
            const Method *calleeMethod =
                cUnit->method->clazz->super->vtable[mIndex];

            if (mir->dalvikInsn.opCode == OP_INVOKE_SUPER)
                genProcessArgsNoRange(cUnit, mir, dInsn, &pcrLabel);
            else
                genProcessArgsRange(cUnit, mir, dInsn, &pcrLabel);

            /* r0 = calleeMethod */
            loadConstant(cUnit, r0, (int) calleeMethod);

            genInvokeCommon(cUnit, mir, bb, labelList, pcrLabel,
                            calleeMethod);
            break;
        }
        /* calleeMethod = method->clazz->pDvmDex->pResMethods[BBBB] */
        case OP_INVOKE_DIRECT:
        case OP_INVOKE_DIRECT_RANGE: {
            const Method *calleeMethod =
                cUnit->method->clazz->pDvmDex->pResMethods[dInsn->vB];

            if (mir->dalvikInsn.opCode == OP_INVOKE_DIRECT)
                genProcessArgsNoRange(cUnit, mir, dInsn, &pcrLabel);
            else
                genProcessArgsRange(cUnit, mir, dInsn, &pcrLabel);

            /* r0 = calleeMethod */
            loadConstant(cUnit, r0, (int) calleeMethod);

            genInvokeCommon(cUnit, mir, bb, labelList, pcrLabel,
                            calleeMethod);
            break;
        }
        /* calleeMethod = method->clazz->pDvmDex->pResMethods[BBBB] */
        case OP_INVOKE_STATIC:
        case OP_INVOKE_STATIC_RANGE: {
            const Method *calleeMethod =
                cUnit->method->clazz->pDvmDex->pResMethods[dInsn->vB];

            if (mir->dalvikInsn.opCode == OP_INVOKE_STATIC)
                genProcessArgsNoRange(cUnit, mir, dInsn,
                                      NULL /* no null check */);
            else
                genProcessArgsRange(cUnit, mir, dInsn,
                                    NULL /* no null check */);

            /* r0 = calleeMethod */
            loadConstant(cUnit, r0, (int) calleeMethod);

            genInvokeCommon(cUnit, mir, bb, labelList, pcrLabel,
                            calleeMethod);
            break;
        }
        /*
         * calleeMethod = dvmFindInterfaceMethodInCache(this->clazz,
         *                    BBBB, method, method->clazz->pDvmDex)
         */
        case OP_INVOKE_INTERFACE:
        case OP_INVOKE_INTERFACE_RANGE: {
            int methodIndex = dInsn->vB;

            if (mir->dalvikInsn.opCode == OP_INVOKE_INTERFACE)
                genProcessArgsNoRange(cUnit, mir, dInsn, &pcrLabel);
            else
                genProcessArgsRange(cUnit, mir, dInsn, &pcrLabel);

            /* r0 now contains this->clazz */
            newLIR3(cUnit, ARMV5TE_LDR_RRI5, r0, r0,
                    offsetof(Object, clazz) >> 2);

            /* r1 = BBBB */
            loadConstant(cUnit, r1, dInsn->vB);

            /* r2 = method (caller) */
            loadConstant(cUnit, r2, (int) cUnit->method);

            /* r3 = pDvmDex */
            loadConstant(cUnit, r3, (int) cUnit->method->clazz->pDvmDex);

            loadConstant(cUnit, r7,
                         (intptr_t) dvmFindInterfaceMethodInCache);
            newLIR1(cUnit, ARMV5TE_BLX_R, r7);

            /* r0 = calleeMethod (returned from dvmFindInterfaceMethodInCache */

            /* r1 = &retChainingCell */
            Armv5teLIR *addrRetChain = newLIR2(cUnit, ARMV5TE_ADD_PC_REL,
                                               r1, 0);
            /* r4PC = dalvikCallsite */
            loadConstant(cUnit, r4PC,
                         (int) (cUnit->method->insns + mir->offset));

            addrRetChain->generic.target = (LIR *) retChainingCell;
            /*
             * r0 = this, r1 = calleeMethod,
             * r1 = &ChainingCell,
             * r4PC = callsiteDPC,
             */
            genDispatchToHandler(cUnit, TEMPLATE_INVOKE_METHOD_NO_OPT);
#if defined(INVOKE_STATS)
            gDvmJit.invokeNoOpt++;
#endif
            /* Handle exceptions using the interpreter */
            genTrap(cUnit, mir->offset, pcrLabel);
            break;
        }
        /* NOP */
        case OP_INVOKE_DIRECT_EMPTY: {
            return false;
        }
        case OP_FILLED_NEW_ARRAY:
        case OP_FILLED_NEW_ARRAY_RANGE: {
            /* Just let the interpreter deal with these */
            genInterpSingleStep(cUnit, mir);
            break;
        }
        default:
            return true;
    }
    return false;
}

static bool handleFmt35ms_3rms(CompilationUnit *cUnit, MIR *mir,
                               BasicBlock *bb, Armv5teLIR *labelList)
{
    Armv5teLIR *retChainingCell = &labelList[bb->fallThrough->id];
    Armv5teLIR *pcrLabel = NULL;

    DecodedInstruction *dInsn = &mir->dalvikInsn;
    switch (mir->dalvikInsn.opCode) {
        /* calleeMethod = this->clazz->vtable[BBBB] */
        case OP_INVOKE_VIRTUAL_QUICK_RANGE:
        case OP_INVOKE_VIRTUAL_QUICK: {
            int methodIndex = dInsn->vB;
            if (mir->dalvikInsn.opCode == OP_INVOKE_VIRTUAL_QUICK)
                genProcessArgsNoRange(cUnit, mir, dInsn, &pcrLabel);
            else
                genProcessArgsRange(cUnit, mir, dInsn, &pcrLabel);

            /* r0 now contains this->clazz */
            newLIR3(cUnit, ARMV5TE_LDR_RRI5, r0, r0,
                    offsetof(Object, clazz) >> 2);
            /* r1 = &retChainingCell */
            Armv5teLIR *addrRetChain = newLIR2(cUnit, ARMV5TE_ADD_PC_REL,
                                               r1, 0);
            /* r4PC = dalvikCallsite */
            loadConstant(cUnit, r4PC,
                         (int) (cUnit->method->insns + mir->offset));

            /* r0 now contains this->clazz->vtable */
            newLIR3(cUnit, ARMV5TE_LDR_RRI5, r0, r0,
                    offsetof(ClassObject, vtable) >> 2);
            addrRetChain->generic.target = (LIR *) retChainingCell;

            if (methodIndex < 32) {
                newLIR3(cUnit, ARMV5TE_LDR_RRI5, r0, r0, methodIndex);
            } else {
                loadConstant(cUnit, r7, methodIndex<<2);
                newLIR3(cUnit, ARMV5TE_LDR_RRR, r0, r0, r7);
            }

            /*
             * r0 = calleeMethod,
             * r1 = &ChainingCell,
             * r4PC = callsiteDPC,
             */
            genDispatchToHandler(cUnit, TEMPLATE_INVOKE_METHOD_NO_OPT);
#if defined(INVOKE_STATS)
            gDvmJit.invokeNoOpt++;
#endif
            break;
        }
        /* calleeMethod = method->clazz->super->vtable[BBBB] */
        case OP_INVOKE_SUPER_QUICK:
        case OP_INVOKE_SUPER_QUICK_RANGE: {
            const Method *calleeMethod =
                cUnit->method->clazz->super->vtable[dInsn->vB];

            if (mir->dalvikInsn.opCode == OP_INVOKE_SUPER_QUICK)
                genProcessArgsNoRange(cUnit, mir, dInsn, &pcrLabel);
            else
                genProcessArgsRange(cUnit, mir, dInsn, &pcrLabel);

            /* r0 = calleeMethod */
            loadConstant(cUnit, r0, (int) calleeMethod);

            genInvokeCommon(cUnit, mir, bb, labelList, pcrLabel,
                            calleeMethod);
            break;
        }
        /* calleeMethod = method->clazz->super->vtable[BBBB] */
        default:
            return true;
    }
    /* Handle exceptions using the interpreter */
    genTrap(cUnit, mir->offset, pcrLabel);
    return false;
}

/*
 * NOTE: We assume here that the special native inline routines
 * are side-effect free.  By making this assumption, we can safely
 * re-execute the routine from the interpreter if it decides it
 * wants to throw an exception. We still need to EXPORT_PC(), though.
 */
static bool handleFmt3inline(CompilationUnit *cUnit, MIR *mir)
{
    DecodedInstruction *dInsn = &mir->dalvikInsn;
    switch( mir->dalvikInsn.opCode) {
        case OP_EXECUTE_INLINE: {
            unsigned int i;
            const InlineOperation* inLineTable = dvmGetInlineOpsTable();
            int offset = (int) &((InterpState *) NULL)->retval;
            int operation = dInsn->vB;

            if (!strcmp(inLineTable[operation].classDescriptor,
                        "Ljava/lang/String;") &&
                !strcmp(inLineTable[operation].methodName,
                        "length") &&
                !strcmp(inLineTable[operation].methodSignature,
                        "()I")) {
                return genInlinedStringLength(cUnit,mir);
            }

            /* Materialize pointer to retval & push */
            newLIR2(cUnit, ARMV5TE_MOV_RR, r4PC, rGLUE);
            newLIR2(cUnit, ARMV5TE_ADD_RI8, r4PC, offset);
            /* Push r4 and (just to take up space) r5) */
            newLIR1(cUnit, ARMV5TE_PUSH, (1<<r4PC | 1<<rFP));

            /* Get code pointer to inline routine */
            loadConstant(cUnit, r4PC, (int)inLineTable[operation].func);

            /* Export PC */
            genExportPC(cUnit, mir, r0, r1 );

            /* Load arguments to r0 through r3 as applicable */
            for (i=0; i < dInsn->vA; i++) {
                loadValue(cUnit, dInsn->arg[i], i);
            }
            /* Call inline routine */
            newLIR1(cUnit, ARMV5TE_BLX_R, r4PC);

            /* Strip frame */
            newLIR1(cUnit, ARMV5TE_ADD_SPI7, 2);

            /* Did we throw? If so, redo under interpreter*/
            genNullCheck(cUnit, r0, mir->offset, NULL);

            break;
        }
        default:
            return true;
    }
    return false;
}

static bool handleFmt51l(CompilationUnit *cUnit, MIR *mir)
{
    loadConstant(cUnit, r0, mir->dalvikInsn.vB_wide & 0xFFFFFFFFUL);
    loadConstant(cUnit, r1, (mir->dalvikInsn.vB_wide>>32) & 0xFFFFFFFFUL);
    storeValuePair(cUnit, r0, r1, mir->dalvikInsn.vA, r2);
    return false;
}

/*****************************************************************************/
/*
 * The following are special processing routines that handle transfer of
 * controls between compiled code and the interpreter. Certain VM states like
 * Dalvik PC and special-purpose registers are reconstructed here.
 */

/* Chaining cell for normal-ending compiles (eg branches) */
static void handleGenericChainingCell(CompilationUnit *cUnit,
                                      unsigned int offset)
{
    newLIR3(cUnit, ARMV5TE_LDR_RRI5, r0, rGLUE,
        offsetof(InterpState, jitToInterpEntries.dvmJitToInterpNormal) >> 2);
    newLIR1(cUnit, ARMV5TE_BLX_R, r0);
    addWordData(cUnit, (int) (cUnit->method->insns + offset), true);
}

/*
 * Chaining cell for instructions that immediately following a method
 * invocation.
 */
static void handlePostInvokeChainingCell(CompilationUnit *cUnit,
                                         unsigned int offset)
{
    newLIR3(cUnit, ARMV5TE_LDR_RRI5, r0, rGLUE,
        offsetof(InterpState, jitToInterpEntries.dvmJitToTraceSelect) >> 2);
    newLIR1(cUnit, ARMV5TE_BLX_R, r0);
    addWordData(cUnit, (int) (cUnit->method->insns + offset), true);
}

/* Chaining cell for monomorphic method invocations. */
static void handleInvokeChainingCell(CompilationUnit *cUnit,
                                     const Method *callee)
{
    newLIR3(cUnit, ARMV5TE_LDR_RRI5, r0, rGLUE,
        offsetof(InterpState, jitToInterpEntries.dvmJitToTraceSelect) >> 2);
    newLIR1(cUnit, ARMV5TE_BLX_R, r0);
    addWordData(cUnit, (int) (callee->insns), true);
}

/* Load the Dalvik PC into r0 and jump to the specified target */
static void handlePCReconstruction(CompilationUnit *cUnit,
                                   Armv5teLIR *targetLabel)
{
    Armv5teLIR **pcrLabel =
        (Armv5teLIR **) cUnit->pcReconstructionList.elemList;
    int numElems = cUnit->pcReconstructionList.numUsed;
    int i;
    for (i = 0; i < numElems; i++) {
        dvmCompilerAppendLIR(cUnit, (LIR *) pcrLabel[i]);
        /* r0 = dalvik PC */
        loadConstant(cUnit, r0, pcrLabel[i]->operands[0]);
        genUnconditionalBranch(cUnit, targetLabel);
    }
}

/* Entry function to invoke the backend of the JIT compiler */
void dvmCompilerMIR2LIR(CompilationUnit *cUnit)
{
    /* Used to hold the labels of each block */
    Armv5teLIR *labelList =
        dvmCompilerNew(sizeof(Armv5teLIR) * cUnit->numBlocks, true);
    GrowableList chainingListByType[CHAINING_CELL_LAST];
    int i;

    /*
     * Initialize the three chaining lists for generic, post-invoke, and invoke
     * chains.
     */
    for (i = 0; i < CHAINING_CELL_LAST; i++) {
        dvmInitGrowableList(&chainingListByType[i], 2);
    }

    BasicBlock **blockList = cUnit->blockList;

    /* Handle the content in each basic block */
    for (i = 0; i < cUnit->numBlocks; i++) {
        blockList[i]->visited = true;
        MIR *mir;

        labelList[i].operands[0] = blockList[i]->startOffset;

        if (blockList[i]->blockType >= CHAINING_CELL_LAST) {
            /*
             * Append the label pseudo LIR first. Chaining cells will be handled
             * separately afterwards.
             */
            dvmCompilerAppendLIR(cUnit, (LIR *) &labelList[i]);
        }

        if (blockList[i]->blockType == DALVIK_BYTECODE) {
            labelList[i].opCode = ARMV5TE_PSEUDO_NORMAL_BLOCK_LABEL;
        } else {
            switch (blockList[i]->blockType) {
                case CHAINING_CELL_GENERIC:
                    labelList[i].opCode = ARMV5TE_PSEUDO_CHAINING_CELL_GENERIC;
                    /* handle the codegen later */
                    dvmInsertGrowableList(
                        &chainingListByType[CHAINING_CELL_GENERIC], (void *) i);
                    break;
                case CHAINING_CELL_INVOKE:
                    labelList[i].opCode = ARMV5TE_PSEUDO_CHAINING_CELL_INVOKE;
                    labelList[i].operands[0] =
                        (int) blockList[i]->containingMethod;
                    /* handle the codegen later */
                    dvmInsertGrowableList(
                        &chainingListByType[CHAINING_CELL_INVOKE], (void *) i);
                    break;
                case CHAINING_CELL_POST_INVOKE:
                    labelList[i].opCode =
                        ARMV5TE_PSEUDO_CHAINING_CELL_POST_INVOKE;
                    /* handle the codegen later */
                    dvmInsertGrowableList(
                        &chainingListByType[CHAINING_CELL_POST_INVOKE],
                        (void *) i);
                    break;
                case PC_RECONSTRUCTION:
                    /* Make sure exception handling block is next */
                    labelList[i].opCode =
                        ARMV5TE_PSEUDO_PC_RECONSTRUCTION_BLOCK_LABEL;
                    assert (i == cUnit->numBlocks - 2);
                    handlePCReconstruction(cUnit, &labelList[i+1]);
                    break;
                case EXCEPTION_HANDLING:
                    labelList[i].opCode = ARMV5TE_PSEUDO_EH_BLOCK_LABEL;
                    if (cUnit->pcReconstructionList.numUsed) {
                        newLIR3(cUnit, ARMV5TE_LDR_RRI5, r1, rGLUE,
                            offsetof(InterpState,
                                     jitToInterpEntries.dvmJitToInterpPunt)
                            >> 2);
                        newLIR1(cUnit, ARMV5TE_BLX_R, r1);
                    }
                    break;
                default:
                    break;
            }
            continue;
        }
        for (mir = blockList[i]->firstMIRInsn; mir; mir = mir->next) {
            OpCode dalvikOpCode = mir->dalvikInsn.opCode;
            InstructionFormat dalvikFormat =
                dexGetInstrFormat(gDvm.instrFormat, dalvikOpCode);
            newLIR2(cUnit, ARMV5TE_PSEUDO_DALVIK_BYTECODE_BOUNDARY,
                    mir->offset,dalvikOpCode);
            bool notHandled;
            /*
             * Debugging: screen the opcode first to see if it is in the
             * do[-not]-compile list
             */
            bool singleStepMe =
                gDvmJit.includeSelectedOp !=
                ((gDvmJit.opList[dalvikOpCode >> 3] &
                  (1 << (dalvikOpCode & 0x7))) !=
                 0);
            if (singleStepMe || cUnit->allSingleStep) {
                notHandled = false;
                genInterpSingleStep(cUnit, mir);
            } else {
                opcodeCoverage[dalvikOpCode]++;
                switch (dalvikFormat) {
                    case kFmt10t:
                    case kFmt20t:
                    case kFmt30t:
                        notHandled = handleFmt10t_Fmt20t_Fmt30t(cUnit,
                                  mir, blockList[i], labelList);
                        break;
                    case kFmt10x:
                        notHandled = handleFmt10x(cUnit, mir);
                        break;
                    case kFmt11n:
                    case kFmt31i:
                        notHandled = handleFmt11n_Fmt31i(cUnit, mir);
                        break;
                    case kFmt11x:
                        notHandled = handleFmt11x(cUnit, mir);
                        break;
                    case kFmt12x:
                        notHandled = handleFmt12x(cUnit, mir);
                        break;
                    case kFmt20bc:
                        notHandled = handleFmt20bc(cUnit, mir);
                        break;
                    case kFmt21c:
                    case kFmt31c:
                        notHandled = handleFmt21c_Fmt31c(cUnit, mir);
                        break;
                    case kFmt21h:
                        notHandled = handleFmt21h(cUnit, mir);
                        break;
                    case kFmt21s:
                        notHandled = handleFmt21s(cUnit, mir);
                        break;
                    case kFmt21t:
                        notHandled = handleFmt21t(cUnit, mir, blockList[i],
                                                  labelList);
                        break;
                    case kFmt22b:
                    case kFmt22s:
                        notHandled = handleFmt22b_Fmt22s(cUnit, mir);
                        break;
                    case kFmt22c:
                        notHandled = handleFmt22c(cUnit, mir);
                        break;
                    case kFmt22cs:
                        notHandled = handleFmt22cs(cUnit, mir);
                        break;
                    case kFmt22t:
                        notHandled = handleFmt22t(cUnit, mir, blockList[i],
                                                  labelList);
                        break;
                    case kFmt22x:
                    case kFmt32x:
                        notHandled = handleFmt22x_Fmt32x(cUnit, mir);
                        break;
                    case kFmt23x:
                        notHandled = handleFmt23x(cUnit, mir);
                        break;
                    case kFmt31t:
                        notHandled = handleFmt31t(cUnit, mir);
                        break;
                    case kFmt3rc:
                    case kFmt35c:
                        notHandled = handleFmt35c_3rc(cUnit, mir, blockList[i],
                                                      labelList);
                        break;
                    case kFmt3rms:
                    case kFmt35ms:
                        notHandled = handleFmt35ms_3rms(cUnit, mir,blockList[i],
                                                        labelList);
                        break;
                    case kFmt3inline:
                        notHandled = handleFmt3inline(cUnit, mir);
                        break;
                    case kFmt51l:
                        notHandled = handleFmt51l(cUnit, mir);
                        break;
                    default:
                        notHandled = true;
                        break;
                }
            }
            if (notHandled) {
                LOGE("%#06x: Opcode 0x%x (%s) / Fmt %d not handled\n",
                     mir->offset,
                     dalvikOpCode, getOpcodeName(dalvikOpCode),
                     dalvikFormat);
                dvmAbort();
                break;
            } else {
              gDvmJit.opHistogram[dalvikOpCode]++;
            }
        }
    }

    /* Handle the codegen in predefined order */
    for (i = 0; i < CHAINING_CELL_LAST; i++) {
        size_t j;
        int *blockIdList = (int *) chainingListByType[i].elemList;

        cUnit->numChainingCells[i] = chainingListByType[i].numUsed;

        /* No chaining cells of this type */
        if (cUnit->numChainingCells[i] == 0)
            continue;

        /* Record the first LIR for a new type of chaining cell */
        cUnit->firstChainingLIR[i] = (LIR *) &labelList[blockIdList[0]];

        for (j = 0; j < chainingListByType[i].numUsed; j++) {
            int blockId = blockIdList[j];

            /* Align this chaining cell first */
            newLIR0(cUnit, ARMV5TE_PSEUDO_ALIGN4);

            /* Insert the pseudo chaining instruction */
            dvmCompilerAppendLIR(cUnit, (LIR *) &labelList[blockId]);


            switch (blockList[blockId]->blockType) {
                case CHAINING_CELL_GENERIC:
                    handleGenericChainingCell(cUnit,
                      blockList[blockId]->startOffset);
                    break;
                case CHAINING_CELL_INVOKE:
                    handleInvokeChainingCell(cUnit,
                        blockList[blockId]->containingMethod);
                    break;
                case CHAINING_CELL_POST_INVOKE:
                    handlePostInvokeChainingCell(cUnit,
                        blockList[blockId]->startOffset);
                    break;
                default:
                    dvmAbort();
                    break;
            }
        }
    }
}

/* Accept the work and start compiling */
void *dvmCompilerDoWork(CompilerWorkOrder *work)
{
   void *res;

   if (gDvmJit.codeCacheFull) {
       return NULL;
   }

   switch (work->kind) {
       case kWorkOrderMethod:
           res = dvmCompileMethod(work->info);
           break;
       case kWorkOrderTrace:
           res = dvmCompileTrace(work->info);
           break;
       default:
           res = NULL;
           dvmAbort();
   }
   return res;
}

/* Architecture-specific initializations and checks go here */
bool dvmCompilerArchInit(void)
{
    /* First, declare dvmCompiler_TEMPLATE_XXX for each template */
#define JIT_TEMPLATE(X) extern void dvmCompiler_TEMPLATE_##X();
#include "../../template/armv5te/TemplateOpList.h"
#undef JIT_TEMPLATE

    int i = 0;
    extern void dvmCompilerTemplateStart(void);

    /*
     * Then, populate the templateEntryOffsets array with the offsets from the
     * the dvmCompilerTemplateStart symbol for each template.
     */
#define JIT_TEMPLATE(X) templateEntryOffsets[i++] = \
    (intptr_t) dvmCompiler_TEMPLATE_##X - (intptr_t) dvmCompilerTemplateStart;
#include "../../template/armv5te/TemplateOpList.h"
#undef JIT_TEMPLATE

    /* Codegen-specific assumptions */
    assert(offsetof(ClassObject, vtable) < 128 &&
           (offsetof(ClassObject, vtable) & 0x3) == 0);
    assert(offsetof(ArrayObject, length) < 128 &&
           (offsetof(ArrayObject, length) & 0x3) == 0);
    assert(offsetof(ArrayObject, contents) < 256);

    /* Up to 5 args are pushed on top of FP - sizeofStackSaveArea */
    assert(sizeof(StackSaveArea) < 236);

    /*
     * EA is calculated by doing "Rn + imm5 << 2", and there are 5 entry points
     * that codegen may access, make sure that the offset from the top of the
     * struct is less than 108.
     */
    assert(offsetof(InterpState, jitToInterpEntries) < 108);
    return true;
}

/* Architectural-specific debugging helpers go here */
void dvmCompilerArchDump(void)
{
    /* Print compiled opcode in this VM instance */
    int i, start, streak;
    char buf[1024];

    streak = i = 0;
    buf[0] = 0;
    while (opcodeCoverage[i] == 0 && i < 256) {
        i++;
    }
    if (i == 256) {
        return;
    }
    for (start = i++, streak = 1; i < 256; i++) {
        if (opcodeCoverage[i]) {
            streak++;
        } else {
            if (streak == 1) {
                sprintf(buf+strlen(buf), "%x,", start);
            } else {
                sprintf(buf+strlen(buf), "%x-%x,", start, start + streak - 1);
            }
            streak = 0;
            while (opcodeCoverage[i] == 0 && i < 256) {
                i++;
            }
            if (i < 256) {
                streak = 1;
                start = i;
            }
        }
    }
    if (streak) {
        if (streak == 1) {
            sprintf(buf+strlen(buf), "%x", start);
        } else {
            sprintf(buf+strlen(buf), "%x-%x", start, start + streak - 1);
        }
    }
    if (strlen(buf)) {
        LOGD("dalvik.vm.jitop = %s", buf);
    }
}
