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
 * This file contains codegen and support common to all supported
 * ARM variants.  It is included by:
 *
 *        Codegen-$(TARGET_ARCH_VARIANT).c
 *
 * which combines this common code with specific support found in the
 * applicable directory below this one.
 */

#include "compiler/Loop.h"

/* Array holding the entry offset of each template relative to the first one */
static intptr_t templateEntryOffsets[TEMPLATE_LAST_MARK];

/* Track exercised opcodes */
static int opcodeCoverage[256];

static void setMemRefType(ArmLIR *lir, bool isLoad, int memType)
{
    u8 *maskPtr;
    u8 mask;
    assert( EncodingMap[lir->opCode].flags & (IS_LOAD | IS_STORE));
    if (isLoad) {
        maskPtr = &lir->useMask;
        mask = ENCODE_MEM_USE;
    } else {
        maskPtr = &lir->defMask;
        mask = ENCODE_MEM_DEF;
    }
    /* Clear out the memref flags */
    *maskPtr &= ~mask;
    /* ..and then add back the one we need */
    switch(memType) {
        case kLiteral:
            assert(isLoad);
            *maskPtr |= (ENCODE_LITERAL | ENCODE_LITPOOL_REF);
            break;
        case kDalvikReg:
            *maskPtr |= (ENCODE_DALVIK_REG | ENCODE_FRAME_REF);
            break;
        case kHeapRef:
            *maskPtr |= ENCODE_HEAP_REF;
            break;
        default:
            LOGE("Jit: invalid memref kind - %d", memType);
            assert(0);  // Bail if debug build, set worst-case in the field
            *maskPtr |= ENCODE_ALL;
    }
}

/*
 * Mark load/store instructions that access Dalvik registers through rFP +
 * offset.
 */
static void annotateDalvikRegAccess(ArmLIR *lir, int regId, bool isLoad)
{
    setMemRefType(lir, isLoad, kDalvikReg);

    /*
     * Store the Dalvik register id in aliasInfo. Mark he MSB if it is a 64-bit
     * access.
     */
    lir->aliasInfo = regId;
    if (DOUBLEREG(lir->operands[0])) {
        lir->aliasInfo |= 0x80000000;
    }
}

/*
 * Decode the register id and mark the corresponding bit(s).
 */
static inline void setupRegMask(u8 *mask, int reg)
{
    u8 seed;
    int shift;
    int regId = reg & 0x1f;

    /*
     * Each double register is equal to a pair of single-precision FP registers
     */
    seed = DOUBLEREG(reg) ? 3 : 1;
    /* FP register starts at bit position 16 */
    shift = FPREG(reg) ? kFPReg0 : 0;
    /* Expand the double register id into single offset */
    shift += regId;
    *mask |= seed << shift;
}

/*
 * Set up the proper fields in the resource mask
 */
static void setupResourceMasks(ArmLIR *lir)
{
    int opCode = lir->opCode;
    int flags;

    if (opCode <= 0) {
        lir->useMask = lir->defMask = 0;
        return;
    }

    flags = EncodingMap[lir->opCode].flags;

    /* Set up the mask for resources that are updated */
    if (flags & (IS_LOAD | IS_STORE)) {
        /* Default to heap - will catch specialized classes later */
        setMemRefType(lir, flags & IS_LOAD, kHeapRef);
    }

    if (flags & IS_BRANCH) {
        lir->defMask |= ENCODE_REG_PC;
        lir->useMask |= ENCODE_REG_PC;
    }

    if (flags & REG_DEF0) {
        setupRegMask(&lir->defMask, lir->operands[0]);
    }

    if (flags & REG_DEF1) {
        setupRegMask(&lir->defMask, lir->operands[1]);
    }

    if (flags & REG_DEF_SP) {
        lir->defMask |= ENCODE_REG_SP;
    }

    if (flags & REG_DEF_LR) {
        lir->defMask |= ENCODE_REG_LR;
    }

    if (flags & REG_DEF_LIST0) {
        lir->defMask |= ENCODE_REG_LIST(lir->operands[0]);
    }

    if (flags & REG_DEF_LIST1) {
        lir->defMask |= ENCODE_REG_LIST(lir->operands[1]);
    }

    if (flags & SETS_CCODES) {
        lir->defMask |= ENCODE_CCODE;
    }

    /* Conservatively treat the IT block */
    if (flags & IS_IT) {
        lir->defMask = ENCODE_ALL;
    }

    /* Set up the mask for resources that are used */
    if (flags & IS_BRANCH) {
        lir->useMask |= ENCODE_REG_PC;
    }

    if (flags & (REG_USE0 | REG_USE1 | REG_USE2 | REG_USE3)) {
        int i;

        for (i = 0; i < 4; i++) {
            if (flags & (1 << (kRegUse0 + i))) {
                setupRegMask(&lir->useMask, lir->operands[i]);
            }
        }
    }

    if (flags & REG_USE_PC) {
        lir->useMask |= ENCODE_REG_PC;
    }

    if (flags & REG_USE_SP) {
        lir->useMask |= ENCODE_REG_SP;
    }

    if (flags & REG_USE_LIST0) {
        lir->useMask |= ENCODE_REG_LIST(lir->operands[0]);
    }

    if (flags & REG_USE_LIST1) {
        lir->useMask |= ENCODE_REG_LIST(lir->operands[1]);
    }

    if (flags & USES_CCODES) {
        lir->useMask |= ENCODE_CCODE;
    }
}

/*
 * The following are building blocks to construct low-level IRs with 0 - 4
 * operands.
 */
static ArmLIR *newLIR0(CompilationUnit *cUnit, ArmOpCode opCode)
{
    ArmLIR *insn = dvmCompilerNew(sizeof(ArmLIR), true);
    assert(isPseudoOpCode(opCode) || (EncodingMap[opCode].flags & NO_OPERAND));
    insn->opCode = opCode;
    setupResourceMasks(insn);
    dvmCompilerAppendLIR(cUnit, (LIR *) insn);
    return insn;
}

static ArmLIR *newLIR1(CompilationUnit *cUnit, ArmOpCode opCode,
                           int dest)
{
    ArmLIR *insn = dvmCompilerNew(sizeof(ArmLIR), true);
    assert(isPseudoOpCode(opCode) || (EncodingMap[opCode].flags & IS_UNARY_OP));
    insn->opCode = opCode;
    insn->operands[0] = dest;
    setupResourceMasks(insn);
    dvmCompilerAppendLIR(cUnit, (LIR *) insn);
    return insn;
}

static ArmLIR *newLIR2(CompilationUnit *cUnit, ArmOpCode opCode,
                           int dest, int src1)
{
    ArmLIR *insn = dvmCompilerNew(sizeof(ArmLIR), true);
    assert(isPseudoOpCode(opCode) ||
           (EncodingMap[opCode].flags & IS_BINARY_OP));
    insn->opCode = opCode;
    insn->operands[0] = dest;
    insn->operands[1] = src1;
    setupResourceMasks(insn);
    dvmCompilerAppendLIR(cUnit, (LIR *) insn);
    return insn;
}

static ArmLIR *newLIR3(CompilationUnit *cUnit, ArmOpCode opCode,
                           int dest, int src1, int src2)
{
    ArmLIR *insn = dvmCompilerNew(sizeof(ArmLIR), true);
    if (!(EncodingMap[opCode].flags & IS_TERTIARY_OP)) {
        LOGE("Bad LIR3: %s[%d]",EncodingMap[opCode].name,opCode);
    }
    assert(isPseudoOpCode(opCode) ||
           (EncodingMap[opCode].flags & IS_TERTIARY_OP));
    insn->opCode = opCode;
    insn->operands[0] = dest;
    insn->operands[1] = src1;
    insn->operands[2] = src2;
    setupResourceMasks(insn);
    dvmCompilerAppendLIR(cUnit, (LIR *) insn);
    return insn;
}

#if defined(_ARMV7_A) || defined(_ARMV7_A_NEON)
static ArmLIR *newLIR4(CompilationUnit *cUnit, ArmOpCode opCode,
                           int dest, int src1, int src2, int info)
{
    ArmLIR *insn = dvmCompilerNew(sizeof(ArmLIR), true);
    assert(isPseudoOpCode(opCode) ||
           (EncodingMap[opCode].flags & IS_QUAD_OP));
    insn->opCode = opCode;
    insn->operands[0] = dest;
    insn->operands[1] = src1;
    insn->operands[2] = src2;
    insn->operands[3] = info;
    setupResourceMasks(insn);
    dvmCompilerAppendLIR(cUnit, (LIR *) insn);
    return insn;
}
#endif

/*
 * If the next instruction is a move-result or move-result-long,
 * return the target Dalvik sReg[s] and convert the next to a
 * nop.  Otherwise, return INVALID_SREG.  Used to optimize method inlining.
 */
static RegLocation inlinedTarget(CompilationUnit *cUnit, MIR *mir,
                                  bool fpHint)
{
    if (mir->next &&
        ((mir->next->dalvikInsn.opCode == OP_MOVE_RESULT) ||
         (mir->next->dalvikInsn.opCode == OP_MOVE_RESULT_OBJECT))) {
        mir->next->dalvikInsn.opCode = OP_NOP;
        return dvmCompilerGetDest(cUnit, mir->next, 0);
    } else {
        RegLocation res = LOC_DALVIK_RETURN_VAL;
        res.fp = fpHint;
        return res;
    }
}

/*
 * Search the existing constants in the literal pool for an exact or close match
 * within specified delta (greater or equal to 0).
 */
static ArmLIR *scanLiteralPool(CompilationUnit *cUnit, int value,
                                   unsigned int delta)
{
    LIR *dataTarget = cUnit->wordList;
    while (dataTarget) {
        if (((unsigned) (value - ((ArmLIR *) dataTarget)->operands[0])) <=
            delta)
            return (ArmLIR *) dataTarget;
        dataTarget = dataTarget->next;
    }
    return NULL;
}

/*
 * The following are building blocks to insert constants into the pool or
 * instruction streams.
 */

/* Add a 32-bit constant either in the constant pool or mixed with code */
static ArmLIR *addWordData(CompilationUnit *cUnit, int value, bool inPlace)
{
    /* Add the constant to the literal pool */
    if (!inPlace) {
        ArmLIR *newValue = dvmCompilerNew(sizeof(ArmLIR), true);
        newValue->operands[0] = value;
        newValue->generic.next = cUnit->wordList;
        cUnit->wordList = (LIR *) newValue;
        return newValue;
    } else {
        /* Add the constant in the middle of code stream */
        newLIR1(cUnit, kArm16BitData, (value & 0xffff));
        newLIR1(cUnit, kArm16BitData, (value >> 16));
    }
    return NULL;
}

static RegLocation inlinedTargetWide(CompilationUnit *cUnit, MIR *mir,
                                      bool fpHint)
{
    if (mir->next &&
        (mir->next->dalvikInsn.opCode == OP_MOVE_RESULT_WIDE)) {
        mir->next->dalvikInsn.opCode = OP_NOP;
        return dvmCompilerGetDestWide(cUnit, mir->next, 0, 1);
    } else {
        RegLocation res = LOC_DALVIK_RETURN_VAL_WIDE;
        res.fp = fpHint;
        return res;
    }
}


/*
 * Generate an kArmPseudoBarrier marker to indicate the boundary of special
 * blocks.
 */
static void genBarrier(CompilationUnit *cUnit)
{
    ArmLIR *barrier = newLIR0(cUnit, kArmPseudoBarrier);
    /* Mark all resources as being clobbered */
    barrier->defMask = -1;
}

/* Create the PC reconstruction slot if not already done */
extern ArmLIR *genCheckCommon(CompilationUnit *cUnit, int dOffset,
                              ArmLIR *branch,
                              ArmLIR *pcrLabel)
{
    /* Forget all def info (because we might rollback here.  Bug #2367397 */
    dvmCompilerResetDefTracking(cUnit);

    /* Set up the place holder to reconstruct this Dalvik PC */
    if (pcrLabel == NULL) {
        int dPC = (int) (cUnit->method->insns + dOffset);
        pcrLabel = dvmCompilerNew(sizeof(ArmLIR), true);
        pcrLabel->opCode = kArmPseudoPCReconstructionCell;
        pcrLabel->operands[0] = dPC;
        pcrLabel->operands[1] = dOffset;
        /* Insert the place holder to the growable list */
        dvmInsertGrowableList(&cUnit->pcReconstructionList, pcrLabel);
    }
    /* Branch to the PC reconstruction code */
    branch->generic.target = (LIR *) pcrLabel;
    return pcrLabel;
}
