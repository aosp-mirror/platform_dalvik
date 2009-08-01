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


/* Array holding the entry offset of each template relative to the first one */
static intptr_t templateEntryOffsets[TEMPLATE_LAST_MARK];

/* Track exercised opcodes */
static int opcodeCoverage[256];

/*****************************************************************************/

/*
 * The following are building blocks to construct low-level IRs with 0 - 3
 * operands.
 */
static ArmLIR *newLIR0(CompilationUnit *cUnit, ArmOpCode opCode)
{
    ArmLIR *insn = dvmCompilerNew(sizeof(ArmLIR), true);
    assert(isPseudoOpCode(opCode) || (EncodingMap[opCode].flags & NO_OPERAND));
    insn->opCode = opCode;
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
    dvmCompilerAppendLIR(cUnit, (LIR *) insn);
    return insn;
}

static ArmLIR *newLIR3(CompilationUnit *cUnit, ArmOpCode opCode,
                           int dest, int src1, int src2)
{
    ArmLIR *insn = dvmCompilerNew(sizeof(ArmLIR), true);
    assert(isPseudoOpCode(opCode) ||
           (EncodingMap[opCode].flags & IS_TERTIARY_OP));
    insn->opCode = opCode;
    insn->operands[0] = dest;
    insn->operands[1] = src1;
    insn->operands[2] = src2;
    dvmCompilerAppendLIR(cUnit, (LIR *) insn);
    return insn;
}

static ArmLIR *newLIR23(CompilationUnit *cUnit, ArmOpCode opCode,
                            int srcdest, int src2)
{
    assert(!isPseudoOpCode(opCode));
    if (EncodingMap[opCode].flags & IS_BINARY_OP)
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
        newLIR1(cUnit, ARM_16BIT_DATA, (value & 0xffff));
        newLIR1(cUnit, ARM_16BIT_DATA, (value >> 16));
    }
    return NULL;
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

/* Perform the actual operation for OP_RETURN_* */
static void genReturnCommon(CompilationUnit *cUnit, MIR *mir)
{
    genDispatchToHandler(cUnit, TEMPLATE_RETURN);
#if defined(INVOKE_STATS)
    gDvmJit.returnOp++;
#endif
    int dPC = (int) (cUnit->method->insns + mir->offset);
    /* Insert branch, but defer setting of target */
    ArmLIR *branch = genUnconditionalBranch(cUnit, NULL);
    /* Set up the place holder to reconstruct this Dalvik PC */
    ArmLIR *pcrLabel = dvmCompilerNew(sizeof(ArmLIR), true);
    pcrLabel->opCode = ARM_PSEUDO_PC_RECONSTRUCTION_CELL;
    pcrLabel->operands[0] = dPC;
    pcrLabel->operands[1] = mir->offset;
    /* Insert the place holder to the growable list */
    dvmInsertGrowableList(&cUnit->pcReconstructionList, pcrLabel);
    /* Branch to the PC reconstruction code */
    branch->generic.target = (LIR *) pcrLabel;
}

/*
 * Perform a binary operation on 64-bit operands and leave the results in the
 * r0/r1 pair.
 */
static void genBinaryOpWide(CompilationUnit *cUnit, int vDest,
                            ArmOpCode preinst, ArmOpCode inst,
                            int reg0, int reg2)
{
    int reg1 = NEXT_REG(reg0);
    int reg3 = NEXT_REG(reg2);
    newLIR23(cUnit, preinst, reg0, reg2);
    newLIR23(cUnit, inst, reg1, reg3);
    storeValuePair(cUnit, reg0, reg1, vDest, reg2);
}

/* Perform a binary operation on 32-bit operands and leave the results in r0. */
static void genBinaryOp(CompilationUnit *cUnit, int vDest, ArmOpCode inst,
                        int reg0, int reg1, int regDest)
{
    if (EncodingMap[inst].flags & IS_BINARY_OP) {
        newLIR2(cUnit, inst, reg0, reg1);
        storeValue(cUnit, reg0, vDest, reg1);
    } else {
        newLIR3(cUnit, inst, regDest, reg0, reg1);
        storeValue(cUnit, regDest, vDest, reg1);
    }
}

/* Create the PC reconstruction slot if not already done */
static inline ArmLIR *genCheckCommon(CompilationUnit *cUnit, int dOffset,
                                         ArmLIR *branch,
                                         ArmLIR *pcrLabel)
{
    /* Set up the place holder to reconstruct this Dalvik PC */
    if (pcrLabel == NULL) {
        int dPC = (int) (cUnit->method->insns + dOffset);
        pcrLabel = dvmCompilerNew(sizeof(ArmLIR), true);
        pcrLabel->opCode = ARM_PSEUDO_PC_RECONSTRUCTION_CELL;
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
 * Perform a "reg cmp reg" operation and jump to the PCR region if condition
 * satisfies.
 */
static inline ArmLIR *inertRegRegCheck(CompilationUnit *cUnit,
                                           ArmConditionCode cond,
                                           int reg1, int reg2, int dOffset,
                                           ArmLIR *pcrLabel)
{
    newLIR2(cUnit, THUMB_CMP_RR, reg1, reg2);
    ArmLIR *branch = newLIR2(cUnit, THUMB_B_COND, 0, cond);
    return genCheckCommon(cUnit, dOffset, branch, pcrLabel);
}

/*
 * Perform null-check on a register. vReg is the Dalvik register being checked,
 * and mReg is the machine register holding the actual value. If internal state
 * indicates that vReg has been checked before the check request is ignored.
 */
static ArmLIR *genNullCheck(CompilationUnit *cUnit, int vReg, int mReg,
                                int dOffset, ArmLIR *pcrLabel)
{
    /* This particular Dalvik register has been null-checked */
    if (dvmIsBitSet(cUnit->registerScoreboard.nullCheckedRegs, vReg)) {
        return pcrLabel;
    }
    dvmSetBit(cUnit->registerScoreboard.nullCheckedRegs, vReg);
    return genRegImmCheck(cUnit, ARM_COND_EQ, mReg, 0, dOffset, pcrLabel);
}

/*
 * Perform zero-check on a register. Similar to genNullCheck but the value being
 * checked does not have a corresponding Dalvik register.
 */
static ArmLIR *genZeroCheck(CompilationUnit *cUnit, int mReg,
                                int dOffset, ArmLIR *pcrLabel)
{
    return genRegImmCheck(cUnit, ARM_COND_EQ, mReg, 0, dOffset, pcrLabel);
}

/* Perform bound check on two registers */
static ArmLIR *genBoundsCheck(CompilationUnit *cUnit, int rIndex,
                                  int rBound, int dOffset, ArmLIR *pcrLabel)
{
    return inertRegRegCheck(cUnit, ARM_COND_CS, rIndex, rBound, dOffset,
                            pcrLabel);
}

/* Generate a unconditional branch to go to the interpreter */
static inline ArmLIR *genTrap(CompilationUnit *cUnit, int dOffset,
                                  ArmLIR *pcrLabel)
{
    ArmLIR *branch = newLIR0(cUnit, THUMB_B_UNCOND);
    return genCheckCommon(cUnit, dOffset, branch, pcrLabel);
}

/* Load a wide field from an object instance */
static void genIGetWide(CompilationUnit *cUnit, MIR *mir, int fieldOffset)
{
    DecodedInstruction *dInsn = &mir->dalvikInsn;
    int reg0, reg1, reg2, reg3;

    /* Allocate reg0..reg3 into physical registers r0..r3 */

    /* See if vB is in a native register. If so, reuse it. */
    reg2 = selectFirstRegister(cUnit, dInsn->vB, false);
    /* Ping reg3 to the other register of the same pair containing reg2 */
    reg3 = reg2 ^ 0x1;
    /*
     * Ping reg0 to the first register of the alternate register pair
     */
    reg0 = (reg2 + 2) & 0x2;
    reg1 = NEXT_REG(reg0);

    loadValue(cUnit, dInsn->vB, reg2);
    loadConstant(cUnit, reg3, fieldOffset);
    genNullCheck(cUnit, dInsn->vB, reg2, mir->offset, NULL); /* null object? */
    newLIR3(cUnit, THUMB_ADD_RRR, reg2, reg2, reg3);
    newLIR2(cUnit, THUMB_LDMIA, reg2, (1<<reg0 | 1<<reg1));
    storeValuePair(cUnit, reg0, reg1, dInsn->vA, reg3);
}

/* Store a wide field to an object instance */
static void genIPutWide(CompilationUnit *cUnit, MIR *mir, int fieldOffset)
{
    DecodedInstruction *dInsn = &mir->dalvikInsn;
    int reg0, reg1, reg2, reg3;

    /* Allocate reg0..reg3 into physical registers r0..r3 */

    /* See if vB is in a native register. If so, reuse it. */
    reg2 = selectFirstRegister(cUnit, dInsn->vB, false);
    /* Ping reg3 to the other register of the same pair containing reg2 */
    reg3 = reg2 ^ 0x1;
    /*
     * Ping reg0 to the first register of the alternate register pair
     */
    reg0 = (reg2 + 2) & 0x2;
    reg1 = NEXT_REG(reg0);


    loadValue(cUnit, dInsn->vB, reg2);
    loadValuePair(cUnit, dInsn->vA, reg0, reg1);
    updateLiveRegisterPair(cUnit, dInsn->vA, reg0, reg1);
    loadConstant(cUnit, reg3, fieldOffset);
    genNullCheck(cUnit, dInsn->vB, reg2, mir->offset, NULL); /* null object? */
    newLIR3(cUnit, THUMB_ADD_RRR, reg2, reg2, reg3);
    newLIR2(cUnit, THUMB_STMIA, reg2, (1<<reg0 | 1<<reg1));
}

/*
 * Load a field from an object instance
 *
 * Inst should be one of:
 *      THUMB_LDR_RRR
 *      THUMB_LDRB_RRR
 *      THUMB_LDRH_RRR
 *      THUMB_LDRSB_RRR
 *      THUMB_LDRSH_RRR
 */
static void genIGet(CompilationUnit *cUnit, MIR *mir, ArmOpCode inst,
                    int fieldOffset)
{
    DecodedInstruction *dInsn = &mir->dalvikInsn;
    int reg0, reg1;

    reg0 = selectFirstRegister(cUnit, dInsn->vB, false);
    reg1 = NEXT_REG(reg0);
    /* TUNING: write a utility routine to load via base + constant offset */
    loadValue(cUnit, dInsn->vB, reg0);
    loadConstant(cUnit, reg1, fieldOffset);
    genNullCheck(cUnit, dInsn->vB, reg0, mir->offset, NULL); /* null object? */
    newLIR3(cUnit, inst, reg0, reg0, reg1);
    storeValue(cUnit, reg0, dInsn->vA, reg1);
}

/*
 * Store a field to an object instance
 *
 * Inst should be one of:
 *      THUMB_STR_RRR
 *      THUMB_STRB_RRR
 *      THUMB_STRH_RRR
 */
static void genIPut(CompilationUnit *cUnit, MIR *mir, ArmOpCode inst,
                    int fieldOffset)
{
    DecodedInstruction *dInsn = &mir->dalvikInsn;
    int reg0, reg1, reg2;

    reg0 = selectFirstRegister(cUnit, dInsn->vB, false);
    reg1 = NEXT_REG(reg0);
    reg2 = NEXT_REG(reg1);

    /* TUNING: write a utility routine to load via base + constant offset */
    loadValue(cUnit, dInsn->vB, reg0);
    loadConstant(cUnit, reg1, fieldOffset);
    loadValue(cUnit, dInsn->vA, reg2);
    updateLiveRegister(cUnit, dInsn->vA, reg2);
    genNullCheck(cUnit, dInsn->vB, reg0, mir->offset, NULL); /* null object? */
    newLIR3(cUnit, inst, reg2, reg0, reg1);
}


/* TODO: This should probably be done as an out-of-line instruction handler. */

/*
 * Generate array load
 *
 * Inst should be one of:
 *      THUMB_LDR_RRR
 *      THUMB_LDRB_RRR
 *      THUMB_LDRH_RRR
 *      THUMB_LDRSB_RRR
 *      THUMB_LDRSH_RRR
 */
static void genArrayGet(CompilationUnit *cUnit, MIR *mir, ArmOpCode inst,
                        int vArray, int vIndex, int vDest, int scale)
{
    int lenOffset = offsetof(ArrayObject, length);
    int dataOffset = offsetof(ArrayObject, contents);
    int reg0, reg1, reg2, reg3;

    reg0 = selectFirstRegister(cUnit, vArray, false);
    reg1 = NEXT_REG(reg0);
    reg2 = NEXT_REG(reg1);
    reg3 = NEXT_REG(reg2);

    loadValue(cUnit, vArray, reg2);
    loadValue(cUnit, vIndex, reg3);

    /* null object? */
    ArmLIR * pcrLabel = genNullCheck(cUnit, vArray, reg2, mir->offset,
                                         NULL);
    newLIR3(cUnit, THUMB_LDR_RRI5, reg0, reg2, lenOffset >> 2);  /* Get len */
    newLIR2(cUnit, THUMB_ADD_RI8, reg2, dataOffset); /* reg2 -> array data */
    genBoundsCheck(cUnit, reg3, reg0, mir->offset, pcrLabel);
    if (scale) {
        newLIR3(cUnit, THUMB_LSL, reg3, reg3, scale);
    }
    if (scale==3) {
        newLIR3(cUnit, inst, reg0, reg2, reg3);
        newLIR2(cUnit, THUMB_ADD_RI8, reg2, 4);
        newLIR3(cUnit, inst, reg1, reg2, reg3);
        storeValuePair(cUnit, reg0, reg1, vDest, reg3);
    } else {
        newLIR3(cUnit, inst, reg0, reg2, reg3);
        storeValue(cUnit, reg0, vDest, reg3);
    }
}

/* TODO: This should probably be done as an out-of-line instruction handler. */

/*
 * Generate array store
 *
 * Inst should be one of:
 *      THUMB_STR_RRR
 *      THUMB_STRB_RRR
 *      THUMB_STRH_RRR
 */
static void genArrayPut(CompilationUnit *cUnit, MIR *mir, ArmOpCode inst,
                        int vArray, int vIndex, int vSrc, int scale)
{
    int lenOffset = offsetof(ArrayObject, length);
    int dataOffset = offsetof(ArrayObject, contents);
    int reg0, reg1, reg2, reg3;

    reg0 = selectFirstRegister(cUnit, vArray, false);
    reg1 = NEXT_REG(reg0);
    reg2 = NEXT_REG(reg1);
    reg3 = NEXT_REG(reg2);

    loadValue(cUnit, vArray, reg2);
    loadValue(cUnit, vIndex, reg3);

    /* null object? */
    ArmLIR * pcrLabel = genNullCheck(cUnit, vArray, reg2, mir->offset,
                                         NULL);
    newLIR3(cUnit, THUMB_LDR_RRI5, reg0, reg2, lenOffset >> 2);  /* Get len */
    newLIR2(cUnit, THUMB_ADD_RI8, reg2, dataOffset); /* reg2 -> array data */
    genBoundsCheck(cUnit, reg3, reg0, mir->offset, pcrLabel);
    /* at this point, reg2 points to array, reg3 is unscaled index */
    if (scale==3) {
        loadValuePair(cUnit, vSrc, reg0, reg1);
        updateLiveRegisterPair(cUnit, vSrc, reg0, reg1);
    } else {
        loadValue(cUnit, vSrc, reg0);
        updateLiveRegister(cUnit, vSrc, reg0);
    }
    if (scale) {
        newLIR3(cUnit, THUMB_LSL, reg3, reg3, scale);
    }
    /*
     * at this point, reg2 points to array, reg3 is scaled index, and
     * reg0[reg1] is data
     */
    if (scale==3) {
        newLIR3(cUnit, inst, reg0, reg2, reg3);
        newLIR2(cUnit, THUMB_ADD_RI8, reg2, 4);
        newLIR3(cUnit, inst, reg1, reg2, reg3);
    } else {
        newLIR3(cUnit, inst, reg0, reg2, reg3);
    }
}

static bool genShiftOpLong(CompilationUnit *cUnit, MIR *mir, int vDest,
                           int vSrc1, int vShift)
{
    /*
     * Don't mess with the regsiters here as there is a particular calling
     * convention to the out-of-line handler.
     */
    loadValue(cUnit, vShift, r2);
    loadValuePair(cUnit, vSrc1, r0, r1);
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
bool genArithOpFloatPortable(CompilationUnit *cUnit, MIR *mir,
                             int vDest, int vSrc1, int vSrc2)
{
    /*
     * Don't optimize the regsiter usage here as they are governed by the EABI
     * calling convention.
     */
    void* funct;
    int reg0, reg1;

    /* TODO: use a proper include file to define these */
    float __aeabi_fadd(float a, float b);
    float __aeabi_fsub(float a, float b);
    float __aeabi_fdiv(float a, float b);
    float __aeabi_fmul(float a, float b);
    float fmodf(float a, float b);

    reg0 = selectFirstRegister(cUnit, vSrc2, false);
    reg1 = NEXT_REG(reg0);

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
            loadValue(cUnit, vSrc2, reg0);
            loadConstant(cUnit, reg1, 0x80000000);
            newLIR3(cUnit, THUMB_ADD_RRR, reg0, reg0, reg1);
            storeValue(cUnit, reg0, vDest, reg1);
            return false;
        }
        default:
            return true;
    }
    loadConstant(cUnit, r2, (int)funct);
    loadValue(cUnit, vSrc1, r0);
    loadValue(cUnit, vSrc2, r1);
    newLIR1(cUnit, THUMB_BLX_R, r2);
    storeValue(cUnit, r0, vDest, r1);
    return false;
}

bool genArithOpDoublePortable(CompilationUnit *cUnit, MIR *mir,
                              int vDest, int vSrc1, int vSrc2)
{
    void* funct;
    int reg0, reg1, reg2;

    /* TODO: use a proper include file to define these */
    double __aeabi_dadd(double a, double b);
    double __aeabi_dsub(double a, double b);
    double __aeabi_ddiv(double a, double b);
    double __aeabi_dmul(double a, double b);
    double fmod(double a, double b);

    reg0 = selectFirstRegister(cUnit, vSrc2, true);
    reg1 = NEXT_REG(reg0);
    reg2 = NEXT_REG(reg1);

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
            loadValuePair(cUnit, vSrc2, reg0, reg1);
            loadConstant(cUnit, reg2, 0x80000000);
            newLIR3(cUnit, THUMB_ADD_RRR, reg1, reg1, reg2);
            storeValuePair(cUnit, reg0, reg1, vDest, reg2);
            return false;
        }
        default:
            return true;
    }
    /*
     * Don't optimize the regsiter usage here as they are governed by the EABI
     * calling convention.
     */
    loadConstant(cUnit, r4PC, (int)funct);
    loadValuePair(cUnit, vSrc1, r0, r1);
    loadValuePair(cUnit, vSrc2, r2, r3);
    newLIR1(cUnit, THUMB_BLX_R, r4PC);
    storeValuePair(cUnit, r0, r1, vDest, r2);
    return false;
}

static bool genArithOpLong(CompilationUnit *cUnit, MIR *mir, int vDest,
                           int vSrc1, int vSrc2)
{
    int firstOp = THUMB_BKPT;
    int secondOp = THUMB_BKPT;
    bool callOut = false;
    void *callTgt;
    int retReg = r0;
    int reg0, reg1, reg2, reg3;
    /* TODO - find proper .h file to declare these */
    long long __aeabi_ldivmod(long long op1, long long op2);

    switch (mir->dalvikInsn.opCode) {
        case OP_NOT_LONG:
            firstOp = THUMB_MVN;
            secondOp = THUMB_MVN;
            break;
        case OP_ADD_LONG:
        case OP_ADD_LONG_2ADDR:
            firstOp = THUMB_ADD_RRR;
            secondOp = THUMB_ADC;
            break;
        case OP_SUB_LONG:
        case OP_SUB_LONG_2ADDR:
            firstOp = THUMB_SUB_RRR;
            secondOp = THUMB_SBC;
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
            firstOp = THUMB_AND_RR;
            secondOp = THUMB_AND_RR;
            break;
        case OP_OR_LONG:
        case OP_OR_LONG_2ADDR:
            firstOp = THUMB_ORR;
            secondOp = THUMB_ORR;
            break;
        case OP_XOR_LONG:
        case OP_XOR_LONG_2ADDR:
            firstOp = THUMB_EOR;
            secondOp = THUMB_EOR;
            break;
        case OP_NEG_LONG: {
            reg0 = selectFirstRegister(cUnit, vSrc2, true);
            reg1 = NEXT_REG(reg0);
            reg2 = NEXT_REG(reg1);
            reg3 = NEXT_REG(reg2);

            loadValuePair(cUnit, vSrc2, reg0, reg1);
            loadConstant(cUnit, reg3, 0);
            newLIR3(cUnit, THUMB_SUB_RRR, reg2, reg3, reg0);
            newLIR2(cUnit, THUMB_SBC, reg3, reg1);
            storeValuePair(cUnit, reg2, reg3, vDest, reg0);
            return false;
        }
        default:
            LOGE("Invalid long arith op");
            dvmAbort();
    }
    if (!callOut) {
        reg0 = selectFirstRegister(cUnit, vSrc1, true);
        reg1 = NEXT_REG(reg0);
        reg2 = NEXT_REG(reg1);
        reg3 = NEXT_REG(reg2);

        loadValuePair(cUnit, vSrc1, reg0, reg1);
        loadValuePair(cUnit, vSrc2, reg2, reg3);
        genBinaryOpWide(cUnit, vDest, firstOp, secondOp, reg0, reg2);
    /*
     * Don't optimize the regsiter usage here as they are governed by the EABI
     * calling convention.
     */
    } else {
        loadValuePair(cUnit, vSrc2, r2, r3);
        loadConstant(cUnit, r4PC, (int) callTgt);
        loadValuePair(cUnit, vSrc1, r0, r1);
        newLIR1(cUnit, THUMB_BLX_R, r4PC);
        storeValuePair(cUnit, retReg, retReg+1, vDest, r4PC);
    }
    return false;
}

static bool genArithOpInt(CompilationUnit *cUnit, MIR *mir, int vDest,
                          int vSrc1, int vSrc2)
{
    int armOp = THUMB_BKPT;
    bool callOut = false;
    bool checkZero = false;
    int retReg = r0;
    void *callTgt;
    int reg0, reg1, regDest;

    /* TODO - find proper .h file to declare these */
    int __aeabi_idivmod(int op1, int op2);
    int __aeabi_idiv(int op1, int op2);

    switch (mir->dalvikInsn.opCode) {
        case OP_NEG_INT:
            armOp = THUMB_NEG;
            break;
        case OP_NOT_INT:
            armOp = THUMB_MVN;
            break;
        case OP_ADD_INT:
        case OP_ADD_INT_2ADDR:
            armOp = THUMB_ADD_RRR;
            break;
        case OP_SUB_INT:
        case OP_SUB_INT_2ADDR:
            armOp = THUMB_SUB_RRR;
            break;
        case OP_MUL_INT:
        case OP_MUL_INT_2ADDR:
            armOp = THUMB_MUL;
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
            armOp = THUMB_AND_RR;
            break;
        case OP_OR_INT:
        case OP_OR_INT_2ADDR:
            armOp = THUMB_ORR;
            break;
        case OP_XOR_INT:
        case OP_XOR_INT_2ADDR:
            armOp = THUMB_EOR;
            break;
        case OP_SHL_INT:
        case OP_SHL_INT_2ADDR:
            armOp = THUMB_LSLV;
            break;
        case OP_SHR_INT:
        case OP_SHR_INT_2ADDR:
            armOp = THUMB_ASRV;
            break;
        case OP_USHR_INT:
        case OP_USHR_INT_2ADDR:
            armOp = THUMB_LSRV;
            break;
        default:
            LOGE("Invalid word arith op: 0x%x(%d)",
                 mir->dalvikInsn.opCode, mir->dalvikInsn.opCode);
            dvmAbort();
    }
    if (!callOut) {
         /* Try to allocate reg0 to the currently cached source operand  */
        if (cUnit->registerScoreboard.liveDalvikReg == vSrc1) {
            reg0 = selectFirstRegister(cUnit, vSrc1, false);
            reg1 = NEXT_REG(reg0);
            regDest = NEXT_REG(reg1);

            loadValue(cUnit, vSrc1, reg0); /* Should be optimized away */
            loadValue(cUnit, vSrc2, reg1);
            genBinaryOp(cUnit, vDest, armOp, reg0, reg1, regDest);
        } else {
            reg0 = selectFirstRegister(cUnit, vSrc2, false);
            reg1 = NEXT_REG(reg0);
            regDest = NEXT_REG(reg1);

            loadValue(cUnit, vSrc1, reg1); /* Load this value first */
            loadValue(cUnit, vSrc2, reg0); /* May be optimized away */
            genBinaryOp(cUnit, vDest, armOp, reg1, reg0, regDest);
        }
    } else {
        /*
         * Load the callout target first since it will never be eliminated
         * and its value will be used first.
         */
        loadConstant(cUnit, r2, (int) callTgt);
        /*
         * Load vSrc2 first if it is not cached in a native register or it
         * is in r0 which will be clobbered if vSrc1 is loaded first.
         */
        if (cUnit->registerScoreboard.liveDalvikReg != vSrc2 ||
            cUnit->registerScoreboard.nativeReg == r0) {
            /* Cannot be optimized and won't clobber r0 */
            loadValue(cUnit, vSrc2, r1);
            /* May be optimized if vSrc1 is cached */
            loadValue(cUnit, vSrc1, r0);
        } else {
            loadValue(cUnit, vSrc1, r0);
            loadValue(cUnit, vSrc2, r1);
        }
        if (checkZero) {
            genNullCheck(cUnit, vSrc2, r1, mir->offset, NULL);
        }
        newLIR1(cUnit, THUMB_BLX_R, r2);
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
        return genArithOpFloat(cUnit, mir, vA, vB, vC);
    }
    if ((opCode >= OP_ADD_DOUBLE_2ADDR) && (opCode <= OP_REM_DOUBLE_2ADDR)) {
        return genArithOpDouble(cUnit,mir, vA, vA, vB);
    }
    if ((opCode >= OP_ADD_DOUBLE) && (opCode <= OP_REM_DOUBLE)) {
        return genArithOpDouble(cUnit,mir, vA, vB, vC);
    }
    return true;
}

static bool genConversionCall(CompilationUnit *cUnit, MIR *mir, void *funct,
                                     int srcSize, int tgtSize)
{
    /*
     * Don't optimize the register usage since it calls out to template
     * functions
     */
    loadConstant(cUnit, r2, (int)funct);
    if (srcSize == 1) {
        loadValue(cUnit, mir->dalvikInsn.vB, r0);
    } else {
        loadValuePair(cUnit, mir->dalvikInsn.vB, r0, r1);
    }
    newLIR1(cUnit, THUMB_BLX_R, r2);
    if (tgtSize == 1) {
        storeValue(cUnit, r0, mir->dalvikInsn.vA, r1);
    } else {
        storeValuePair(cUnit, r0, r1, mir->dalvikInsn.vA, r2);
    }
    return false;
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
    newLIR3(cUnit, THUMB_STR_RRI5, reg1, rGLUE, offset >> 2);
    return false;
}

/*
 * NOTE: The amount of code for this body suggests it ought to
 * be handled in a template (and could also be coded quite a bit
 * more efficiently in ARM).  However, the code is dependent on the
 * internal structure layout of string objects which are most safely
 * known at run time.
 * TUNING:  One possibility (which could also be used for StringCompareTo
 * and StringEquals) is to generate string access helper subroutines on
 * Jit startup, and then call them from the translated inline-executes.
 */
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
    newLIR3(cUnit, THUMB_STR_RRI5, regMax, rGLUE, offset >> 2);
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
    newLIR3(cUnit, THUMB_ASR, sign, reg0, 31);
    newLIR3(cUnit, THUMB_ADD_RRR, reg0, reg0, sign);
    newLIR2(cUnit, THUMB_EOR, reg0, sign);
    newLIR3(cUnit, THUMB_STR_RRI5, reg0, rGLUE, offset >> 2);
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
    newLIR3(cUnit, THUMB_STR_RRI5, reg0, rGLUE, offset >> 2);
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
    newLIR3(cUnit, THUMB_STR_RRI5, oplo, rGLUE, offset >> 2);
    newLIR2(cUnit, THUMB_AND_RR, ophi, signMask);
    newLIR3(cUnit, THUMB_STR_RRI5, ophi, rGLUE, (offset >> 2)+1);
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
    newLIR3(cUnit, THUMB_ASR, sign, ophi, 31);
    newLIR3(cUnit, THUMB_ADD_RRR, oplo, oplo, sign);
    newLIR2(cUnit, THUMB_ADC, ophi, sign);
    newLIR2(cUnit, THUMB_EOR, oplo, sign);
    newLIR2(cUnit, THUMB_EOR, ophi, sign);
    newLIR3(cUnit, THUMB_STR_RRI5, oplo, rGLUE, offset >> 2);
    newLIR3(cUnit, THUMB_STR_RRI5, ophi, rGLUE, (offset >> 2)+1);
    return false;
}

static void genProcessArgsNoRange(CompilationUnit *cUnit, MIR *mir,
                                  DecodedInstruction *dInsn,
                                  ArmLIR **pcrLabel)
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
        newLIR2(cUnit, THUMB_MOV_RR, r7, rFP);
        newLIR2(cUnit, THUMB_SUB_RI8, r7,
                sizeof(StackSaveArea) + (dInsn->vA << 2));
        /* generate null check */
        if (pcrLabel) {
            *pcrLabel = genNullCheck(cUnit, dInsn->arg[0], r0, mir->offset,
                                     NULL);
        }
        newLIR2(cUnit, THUMB_STMIA, r7, regMask);
    }
}

static void genProcessArgsRange(CompilationUnit *cUnit, MIR *mir,
                                DecodedInstruction *dInsn,
                                ArmLIR **pcrLabel)
{
    int srcOffset = dInsn->vC << 2;
    int numArgs = dInsn->vA;
    int regMask;
    /*
     * r4PC     : &rFP[vC]
     * r7: &newFP[0]
     */
    if (srcOffset < 8) {
        newLIR3(cUnit, THUMB_ADD_RRI3, r4PC, rFP, srcOffset);
    } else {
        loadConstant(cUnit, r4PC, srcOffset);
        newLIR3(cUnit, THUMB_ADD_RRR, r4PC, rFP, r4PC);
    }
    /* load [r0 .. min(numArgs,4)] */
    regMask = (1 << ((numArgs < 4) ? numArgs : 4)) - 1;
    newLIR2(cUnit, THUMB_LDMIA, r4PC, regMask);

    if (sizeof(StackSaveArea) + (numArgs << 2) < 256) {
        newLIR2(cUnit, THUMB_MOV_RR, r7, rFP);
        newLIR2(cUnit, THUMB_SUB_RI8, r7,
                sizeof(StackSaveArea) + (numArgs << 2));
    } else {
        loadConstant(cUnit, r7, sizeof(StackSaveArea) + (numArgs << 2));
        newLIR3(cUnit, THUMB_SUB_RRR, r7, rFP, r7);
    }

    /* generate null check */
    if (pcrLabel) {
        *pcrLabel = genNullCheck(cUnit, dInsn->vC, r0, mir->offset, NULL);
    }

    /*
     * Handle remaining 4n arguments:
     * store previously loaded 4 values and load the next 4 values
     */
    if (numArgs >= 8) {
        ArmLIR *loopLabel = NULL;
        /*
         * r0 contains "this" and it will be used later, so push it to the stack
         * first. Pushing r5 is just for stack alignment purposes.
         */
        newLIR1(cUnit, THUMB_PUSH, 1 << r0 | 1 << 5);
        /* No need to generate the loop structure if numArgs <= 11 */
        if (numArgs > 11) {
            loadConstant(cUnit, 5, ((numArgs - 4) >> 2) << 2);
            loopLabel = newLIR0(cUnit, ARM_PSEUDO_TARGET_LABEL);
        }
        newLIR2(cUnit, THUMB_STMIA, r7, regMask);
        newLIR2(cUnit, THUMB_LDMIA, r4PC, regMask);
        /* No need to generate the loop structure if numArgs <= 11 */
        if (numArgs > 11) {
            newLIR2(cUnit, THUMB_SUB_RI8, 5, 4);
            genConditionalBranch(cUnit, ARM_COND_NE, loopLabel);
        }
    }

    /* Save the last batch of loaded values */
    newLIR2(cUnit, THUMB_STMIA, r7, regMask);

    /* Generate the loop epilogue - don't use r0 */
    if ((numArgs > 4) && (numArgs % 4)) {
        regMask = ((1 << (numArgs & 0x3)) - 1) << 1;
        newLIR2(cUnit, THUMB_LDMIA, r4PC, regMask);
    }
    if (numArgs >= 8)
        newLIR1(cUnit, THUMB_POP, 1 << r0 | 1 << 5);

    /* Save the modulo 4 arguments */
    if ((numArgs > 4) && (numArgs % 4)) {
        newLIR2(cUnit, THUMB_STMIA, r7, regMask);
    }
}

/*
 * Generate code to setup the call stack then jump to the chaining cell if it
 * is not a native method.
 */
static void genInvokeSingletonCommon(CompilationUnit *cUnit, MIR *mir,
                                     BasicBlock *bb, ArmLIR *labelList,
                                     ArmLIR *pcrLabel,
                                     const Method *calleeMethod)
{
    ArmLIR *retChainingCell = &labelList[bb->fallThrough->id];

    /* r1 = &retChainingCell */
    ArmLIR *addrRetChain = newLIR3(cUnit, THUMB_ADD_PC_REL,
                                           r1, 0, 0);
    /* r4PC = dalvikCallsite */
    loadConstant(cUnit, r4PC,
                 (int) (cUnit->method->insns + mir->offset));
    addrRetChain->generic.target = (LIR *) retChainingCell;
    /*
     * r0 = calleeMethod (loaded upon calling genInvokeSingletonCommon)
     * r1 = &ChainingCell
     * r4PC = callsiteDPC
     */
    if (dvmIsNativeMethod(calleeMethod)) {
        genDispatchToHandler(cUnit, TEMPLATE_INVOKE_METHOD_NATIVE);
#if defined(INVOKE_STATS)
        gDvmJit.invokeNative++;
#endif
    } else {
        genDispatchToHandler(cUnit, TEMPLATE_INVOKE_METHOD_CHAIN);
#if defined(INVOKE_STATS)
        gDvmJit.invokeChain++;
#endif
        /* Branch to the chaining cell */
        genUnconditionalBranch(cUnit, &labelList[bb->taken->id]);
    }
    /* Handle exceptions using the interpreter */
    genTrap(cUnit, mir->offset, pcrLabel);
}

/*
 * Generate code to check the validity of a predicted chain and take actions
 * based on the result.
 *
 * 0x426a99aa : ldr     r4, [pc, #72] --> r4 <- dalvikPC of this invoke
 * 0x426a99ac : add     r1, pc, #32   --> r1 <- &retChainingCell
 * 0x426a99ae : add     r2, pc, #40   --> r2 <- &predictedChainingCell
 * 0x426a99b0 : blx_1   0x426a918c    --+ TEMPLATE_INVOKE_METHOD_PREDICTED_CHAIN
 * 0x426a99b2 : blx_2   see above     --+
 * 0x426a99b4 : b       0x426a99d8    --> off to the predicted chain
 * 0x426a99b6 : b       0x426a99c8    --> punt to the interpreter
 * 0x426a99b8 : ldr     r0, [r7, #44] --> r0 <- this->class->vtable[methodIdx]
 * 0x426a99ba : cmp     r1, #0        --> compare r1 (rechain count) against 0
 * 0x426a99bc : bgt     0x426a99c2    --> >=0? don't rechain
 * 0x426a99be : ldr     r7, [r6, #96] --+ dvmJitToPatchPredictedChain
 * 0x426a99c0 : blx     r7            --+
 * 0x426a99c2 : add     r1, pc, #12   --> r1 <- &retChainingCell
 * 0x426a99c4 : blx_1   0x426a9098    --+ TEMPLATE_INVOKE_METHOD_NO_OPT
 * 0x426a99c6 : blx_2   see above     --+
 */
static void genInvokeVirtualCommon(CompilationUnit *cUnit, MIR *mir,
                                   int methodIndex,
                                   ArmLIR *retChainingCell,
                                   ArmLIR *predChainingCell,
                                   ArmLIR *pcrLabel)
{
    /* "this" is already left in r0 by genProcessArgs* */

    /* r4PC = dalvikCallsite */
    loadConstant(cUnit, r4PC,
                 (int) (cUnit->method->insns + mir->offset));

    /* r1 = &retChainingCell */
    ArmLIR *addrRetChain = newLIR2(cUnit, THUMB_ADD_PC_REL,
                                       r1, 0);
    addrRetChain->generic.target = (LIR *) retChainingCell;

    /* r2 = &predictedChainingCell */
    ArmLIR *predictedChainingCell =
        newLIR2(cUnit, THUMB_ADD_PC_REL, r2, 0);
    predictedChainingCell->generic.target = (LIR *) predChainingCell;

    genDispatchToHandler(cUnit, TEMPLATE_INVOKE_METHOD_PREDICTED_CHAIN);

    /* return through lr - jump to the chaining cell */
    genUnconditionalBranch(cUnit, predChainingCell);

    /*
     * null-check on "this" may have been eliminated, but we still need a PC-
     * reconstruction label for stack overflow bailout.
     */
    if (pcrLabel == NULL) {
        int dPC = (int) (cUnit->method->insns + mir->offset);
        pcrLabel = dvmCompilerNew(sizeof(ArmLIR), true);
        pcrLabel->opCode = ARM_PSEUDO_PC_RECONSTRUCTION_CELL;
        pcrLabel->operands[0] = dPC;
        pcrLabel->operands[1] = mir->offset;
        /* Insert the place holder to the growable list */
        dvmInsertGrowableList(&cUnit->pcReconstructionList, pcrLabel);
    }

    /* return through lr+2 - punt to the interpreter */
    genUnconditionalBranch(cUnit, pcrLabel);

    /*
     * return through lr+4 - fully resolve the callee method.
     * r1 <- count
     * r2 <- &predictedChainCell
     * r3 <- this->class
     * r4 <- dPC
     * r7 <- this->class->vtable
     */

    /* r0 <- calleeMethod */
    if (methodIndex < 32) {
        newLIR3(cUnit, THUMB_LDR_RRI5, r0, r7, methodIndex);
    } else {
        loadConstant(cUnit, r0, methodIndex<<2);
        newLIR3(cUnit, THUMB_LDR_RRR, r0, r7, r0);
    }

    /* Check if rechain limit is reached */
    newLIR2(cUnit, THUMB_CMP_RI8, r1, 0);

    ArmLIR *bypassRechaining =
        newLIR2(cUnit, THUMB_B_COND, 0, ARM_COND_GT);

    newLIR3(cUnit, THUMB_LDR_RRI5, r7, rGLUE,
            offsetof(InterpState,
                     jitToInterpEntries.dvmJitToPatchPredictedChain)
            >> 2);

    /*
     * r0 = calleeMethod
     * r2 = &predictedChainingCell
     * r3 = class
     *
     * &returnChainingCell has been loaded into r1 but is not needed
     * when patching the chaining cell and will be clobbered upon
     * returning so it will be reconstructed again.
     */
    newLIR1(cUnit, THUMB_BLX_R, r7);

    /* r1 = &retChainingCell */
    addrRetChain = newLIR3(cUnit, THUMB_ADD_PC_REL, r1, 0, 0);
    addrRetChain->generic.target = (LIR *) retChainingCell;

    bypassRechaining->generic.target = (LIR *) addrRetChain;
    /*
     * r0 = calleeMethod,
     * r1 = &ChainingCell,
     * r4PC = callsiteDPC,
     */
    genDispatchToHandler(cUnit, TEMPLATE_INVOKE_METHOD_NO_OPT);
#if defined(INVOKE_STATS)
    gDvmJit.invokePredictedChain++;
#endif
    /* Handle exceptions using the interpreter */
    genTrap(cUnit, mir->offset, pcrLabel);
}

/*
 * Up calling this function, "this" is stored in r0. The actual class will be
 * chased down off r0 and the predicted one will be retrieved through
 * predictedChainingCell then a comparison is performed to see whether the
 * previously established chaining is still valid.
 *
 * The return LIR is a branch based on the comparison result. The actual branch
 * target will be setup in the caller.
 */
static ArmLIR *genCheckPredictedChain(CompilationUnit *cUnit,
                                          ArmLIR *predChainingCell,
                                          ArmLIR *retChainingCell,
                                          MIR *mir)
{
    /* r3 now contains this->clazz */
    newLIR3(cUnit, THUMB_LDR_RRI5, r3, r0,
            offsetof(Object, clazz) >> 2);

    /*
     * r2 now contains predicted class. The starting offset of the
     * cached value is 4 bytes into the chaining cell.
     */
    ArmLIR *getPredictedClass =
        newLIR3(cUnit, THUMB_LDR_PC_REL, r2, 0,
                offsetof(PredictedChainingCell, clazz));
    getPredictedClass->generic.target = (LIR *) predChainingCell;

    /*
     * r0 now contains predicted method. The starting offset of the
     * cached value is 8 bytes into the chaining cell.
     */
    ArmLIR *getPredictedMethod =
        newLIR3(cUnit, THUMB_LDR_PC_REL, r0, 0,
                offsetof(PredictedChainingCell, method));
    getPredictedMethod->generic.target = (LIR *) predChainingCell;

    /* Load the stats counter to see if it is time to unchain and refresh */
    ArmLIR *getRechainingRequestCount =
        newLIR3(cUnit, THUMB_LDR_PC_REL, r7, 0,
                offsetof(PredictedChainingCell, counter));
    getRechainingRequestCount->generic.target =
        (LIR *) predChainingCell;

    /* r4PC = dalvikCallsite */
    loadConstant(cUnit, r4PC,
                 (int) (cUnit->method->insns + mir->offset));

    /* r1 = &retChainingCell */
    ArmLIR *addrRetChain = newLIR3(cUnit, THUMB_ADD_PC_REL,
                                       r1, 0, 0);
    addrRetChain->generic.target = (LIR *) retChainingCell;

    /* Check if r2 (predicted class) == r3 (actual class) */
    newLIR2(cUnit, THUMB_CMP_RR, r2, r3);

    return newLIR2(cUnit, THUMB_B_COND, 0, ARM_COND_EQ);
}

/* Geneate a branch to go back to the interpreter */
static void genPuntToInterp(CompilationUnit *cUnit, unsigned int offset)
{
    /* r0 = dalvik pc */
    loadConstant(cUnit, r0, (int) (cUnit->method->insns + offset));
    newLIR3(cUnit, THUMB_LDR_RRI5, r1, rGLUE,
            offsetof(InterpState, jitToInterpEntries.dvmJitToInterpPunt) >> 2);
    newLIR1(cUnit, THUMB_BLX_R, r1);
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
    newLIR3(cUnit, THUMB_LDR_RRI5, r2, rGLUE, entryAddr >> 2);
    /* r0 = dalvik pc */
    loadConstant(cUnit, r0, (int) (cUnit->method->insns + mir->offset));
    /* r1 = dalvik pc of following instruction */
    loadConstant(cUnit, r1, (int) (cUnit->method->insns + mir->next->offset));
    newLIR1(cUnit, THUMB_BLX_R, r2);
}


/*****************************************************************************/
/*
 * The following are the first-level codegen routines that analyze the format
 * of each bytecode then either dispatch special purpose codegen routines
 * or produce corresponding Thumb instructions directly.
 */

static bool handleFmt10t_Fmt20t_Fmt30t(CompilationUnit *cUnit, MIR *mir,
                                       BasicBlock *bb, ArmLIR *labelList)
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
    int reg0, reg1, reg2;

    switch (mir->dalvikInsn.opCode) {
        case OP_CONST:
        case OP_CONST_4: {
            /* Avoid using the previously used register */
            reg0 = selectFirstRegister(cUnit, vNone, false);
            reg1 = NEXT_REG(reg0);
            loadConstant(cUnit, reg0, mir->dalvikInsn.vB);
            storeValue(cUnit, reg0, mir->dalvikInsn.vA, reg1);
            break;
        }
        case OP_CONST_WIDE_32: {
            /* Avoid using the previously used register */
            reg0 = selectFirstRegister(cUnit, vNone, true);
            reg1 = NEXT_REG(reg0);
            reg2 = NEXT_REG(reg1);
            loadConstant(cUnit, reg0, mir->dalvikInsn.vB);
            newLIR3(cUnit, THUMB_ASR, reg1, reg0, 31);
            storeValuePair(cUnit, reg0, reg1, mir->dalvikInsn.vA, reg2);
            break;
        }
        default:
            return true;
    }
    return false;
}

static bool handleFmt21h(CompilationUnit *cUnit, MIR *mir)
{
    int reg0, reg1, reg2;

    /* Avoid using the previously used register */
    switch (mir->dalvikInsn.opCode) {
        case OP_CONST_HIGH16: {
            reg0 = selectFirstRegister(cUnit, vNone, false);
            reg1 = NEXT_REG(reg0);
            loadConstant(cUnit, reg0, mir->dalvikInsn.vB << 16);
            storeValue(cUnit, reg0, mir->dalvikInsn.vA, reg1);
            break;
        }
        case OP_CONST_WIDE_HIGH16: {
            reg0 = selectFirstRegister(cUnit, vNone, true);
            reg1 = NEXT_REG(reg0);
            reg2 = NEXT_REG(reg1);
            loadConstant(cUnit, reg1, mir->dalvikInsn.vB << 16);
            loadConstant(cUnit, reg0, 0);
            storeValuePair(cUnit, reg0, reg1, mir->dalvikInsn.vA, reg2);
            break;
        }
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
    /* Native register to use if the interested value is vA */
    int regvA = selectFirstRegister(cUnit, mir->dalvikInsn.vA, false);
    /* Native register to use if source is not from Dalvik registers */
    int regvNone = selectFirstRegister(cUnit, vNone, false);
    /* Similar to regvA but for 64-bit values */
    int regvAWide = selectFirstRegister(cUnit, mir->dalvikInsn.vA, true);
    /* Similar to regvNone but for 64-bit values */
    int regvNoneWide = selectFirstRegister(cUnit, vNone, true);

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
            loadConstant(cUnit, regvNone, (int) strPtr );
            storeValue(cUnit, regvNone, mir->dalvikInsn.vA, NEXT_REG(regvNone));
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
            loadConstant(cUnit, regvNone, (int) classPtr );
            storeValue(cUnit, regvNone, mir->dalvikInsn.vA, NEXT_REG(regvNone));
            break;
        }
        case OP_SGET_OBJECT:
        case OP_SGET_BOOLEAN:
        case OP_SGET_CHAR:
        case OP_SGET_BYTE:
        case OP_SGET_SHORT:
        case OP_SGET: {
            int valOffset = offsetof(StaticField, value);
            void *fieldPtr = (void*)
              (cUnit->method->clazz->pDvmDex->pResFields[mir->dalvikInsn.vB]);
            assert(fieldPtr != NULL);
            loadConstant(cUnit, regvNone,  (int) fieldPtr + valOffset);
            newLIR3(cUnit, THUMB_LDR_RRI5, regvNone, regvNone, 0);
            storeValue(cUnit, regvNone, mir->dalvikInsn.vA, NEXT_REG(regvNone));
            break;
        }
        case OP_SGET_WIDE: {
            int valOffset = offsetof(StaticField, value);
            void *fieldPtr = (void*)
              (cUnit->method->clazz->pDvmDex->pResFields[mir->dalvikInsn.vB]);
            int reg0, reg1, reg2;

            assert(fieldPtr != NULL);
            reg0 = regvNoneWide;
            reg1 = NEXT_REG(reg0);
            reg2 = NEXT_REG(reg1);
            loadConstant(cUnit, reg2,  (int) fieldPtr + valOffset);
            newLIR2(cUnit, THUMB_LDMIA, reg2, (1<<reg0 | 1<<reg1));
            storeValuePair(cUnit, reg0, reg1, mir->dalvikInsn.vA, reg2);
            break;
        }
        case OP_SPUT_OBJECT:
        case OP_SPUT_BOOLEAN:
        case OP_SPUT_CHAR:
        case OP_SPUT_BYTE:
        case OP_SPUT_SHORT:
        case OP_SPUT: {
            int valOffset = offsetof(StaticField, value);
            void *fieldPtr = (void*)
              (cUnit->method->clazz->pDvmDex->pResFields[mir->dalvikInsn.vB]);

            assert(fieldPtr != NULL);
            loadValue(cUnit, mir->dalvikInsn.vA, regvA);
            updateLiveRegister(cUnit, mir->dalvikInsn.vA, regvA);
            loadConstant(cUnit, NEXT_REG(regvA),  (int) fieldPtr + valOffset);
            newLIR3(cUnit, THUMB_STR_RRI5, regvA, NEXT_REG(regvA), 0);
            break;
        }
        case OP_SPUT_WIDE: {
            int reg0, reg1, reg2;
            int valOffset = offsetof(StaticField, value);
            void *fieldPtr = (void*)
              (cUnit->method->clazz->pDvmDex->pResFields[mir->dalvikInsn.vB]);

            assert(fieldPtr != NULL);
            reg0 = regvAWide;
            reg1 = NEXT_REG(reg0);
            reg2 = NEXT_REG(reg1);
            loadValuePair(cUnit, mir->dalvikInsn.vA, reg0, reg1);
            updateLiveRegisterPair(cUnit, mir->dalvikInsn.vA, reg0, reg1);
            loadConstant(cUnit, reg2,  (int) fieldPtr + valOffset);
            newLIR2(cUnit, THUMB_STMIA, reg2, (1<<reg0 | 1<<reg1));
            break;
        }
        case OP_NEW_INSTANCE: {
            /*
             * Obey the calling convention and don't mess with the register
             * usage.
             */
            ClassObject *classPtr = (void*)
              (cUnit->method->clazz->pDvmDex->pResClasses[mir->dalvikInsn.vB]);
            assert(classPtr != NULL);
            assert(classPtr->status & CLASS_INITIALIZED);
            if ((classPtr->accessFlags & (ACC_INTERFACE|ACC_ABSTRACT)) != 0) {
                /* It's going to throw, just let the interp. deal with it. */
                genInterpSingleStep(cUnit, mir);
                return false;
            }
            loadConstant(cUnit, r4PC, (int)dvmAllocObject);
            loadConstant(cUnit, r0, (int) classPtr);
            genExportPC(cUnit, mir, r2, r3 );
            loadConstant(cUnit, r1, ALLOC_DONT_TRACK);
            newLIR1(cUnit, THUMB_BLX_R, r4PC);
            /*
             * TODO: As coded, we'll bail and reinterpret on alloc failure.
             * Need a general mechanism to bail to thrown exception code.
             */
            genZeroCheck(cUnit, r0, mir->offset, NULL);
            storeValue(cUnit, r0, mir->dalvikInsn.vA, r1);
            break;
        }
        case OP_CHECK_CAST: {
            /*
             * Obey the calling convention and don't mess with the register
             * usage.
             */
            ClassObject *classPtr =
              (cUnit->method->clazz->pDvmDex->pResClasses[mir->dalvikInsn.vB]);
            loadConstant(cUnit, r1, (int) classPtr );
            loadValue(cUnit, mir->dalvikInsn.vA, r0);  /* Ref */
            /*
             * TODO - in theory classPtr should be resoved by the time this
             * instruction made into a trace, but we are seeing NULL at runtime
             * so this check is temporarily used as a workaround.
             */
            ArmLIR * pcrLabel = genZeroCheck(cUnit, r1, mir->offset, NULL);
            newLIR2(cUnit, THUMB_CMP_RI8, r0, 0);    /* Null? */
            ArmLIR *branch1 =
                newLIR2(cUnit, THUMB_B_COND, 4, ARM_COND_EQ);
            /* r0 now contains object->clazz */
            newLIR3(cUnit, THUMB_LDR_RRI5, r0, r0,
                    offsetof(Object, clazz) >> 2);
            loadConstant(cUnit, r4PC, (int)dvmInstanceofNonTrivial);
            newLIR2(cUnit, THUMB_CMP_RR, r0, r1);
            ArmLIR *branch2 =
                newLIR2(cUnit, THUMB_B_COND, 2, ARM_COND_EQ);
            newLIR1(cUnit, THUMB_BLX_R, r4PC);
            /* check cast failed - punt to the interpreter */
            genZeroCheck(cUnit, r0, mir->offset, pcrLabel);
            /* check cast passed - branch target here */
            ArmLIR *target = newLIR0(cUnit, ARM_PSEUDO_TARGET_LABEL);
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
            newLIR3(cUnit, THUMB_LDR_RRI5, r1, rGLUE, offset >> 2);
            newLIR3(cUnit, THUMB_LDR_RRI5, r0, r1, exOffset >> 2);
            storeValue(cUnit, r0, mir->dalvikInsn.vA, r1);
           break;
        }
        case OP_MOVE_RESULT:
        case OP_MOVE_RESULT_OBJECT: {
            int offset = offsetof(InterpState, retval);
            newLIR3(cUnit, THUMB_LDR_RRI5, r0, rGLUE, offset >> 2);
            storeValue(cUnit, r0, mir->dalvikInsn.vA, r1);
            break;
        }
        case OP_MOVE_RESULT_WIDE: {
            int offset = offsetof(InterpState, retval);
            newLIR3(cUnit, THUMB_LDR_RRI5, r0, rGLUE, offset >> 2);
            newLIR3(cUnit, THUMB_LDR_RRI5, r1, rGLUE, (offset >> 2)+1);
            storeValuePair(cUnit, r0, r1, mir->dalvikInsn.vA, r2);
            break;
        }
        case OP_RETURN_WIDE: {
            loadValuePair(cUnit, mir->dalvikInsn.vA, r0, r1);
            int offset = offsetof(InterpState, retval);
            newLIR3(cUnit, THUMB_STR_RRI5, r0, rGLUE, offset >> 2);
            newLIR3(cUnit, THUMB_STR_RRI5, r1, rGLUE, (offset >> 2)+1);
            genReturnCommon(cUnit,mir);
            break;
        }
        case OP_RETURN:
        case OP_RETURN_OBJECT: {
            loadValue(cUnit, mir->dalvikInsn.vA, r0);
            int offset = offsetof(InterpState, retval);
            newLIR3(cUnit, THUMB_STR_RRI5, r0, rGLUE, offset >> 2);
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
            newLIR3(cUnit, THUMB_LDR_RRI5, r0, rGLUE, offset >> 2);
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
            genNullCheck(cUnit, mir->dalvikInsn.vA, r1, mir->offset, NULL);
            /* Do the call */
            newLIR1(cUnit, THUMB_BLX_R, r2);
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

static bool genConversionPortable(CompilationUnit *cUnit, MIR *mir)
{
    OpCode opCode = mir->dalvikInsn.opCode;

    float  __aeabi_i2f(  int op1 );
    int    __aeabi_f2iz( float op1 );
    float  __aeabi_d2f(  double op1 );
    double __aeabi_f2d(  float op1 );
    double __aeabi_i2d(  int op1 );
    int    __aeabi_d2iz( double op1 );
    float  __aeabi_l2f(  long op1 );
    double __aeabi_l2d(  long op1 );

    switch (opCode) {
        case OP_INT_TO_FLOAT:
            return genConversionCall(cUnit, mir, (void*)__aeabi_i2f, 1, 1);
        case OP_FLOAT_TO_INT:
            return genConversionCall(cUnit, mir, (void*)__aeabi_f2iz, 1, 1);
        case OP_DOUBLE_TO_FLOAT:
            return genConversionCall(cUnit, mir, (void*)__aeabi_d2f, 2, 1);
        case OP_FLOAT_TO_DOUBLE:
            return genConversionCall(cUnit, mir, (void*)__aeabi_f2d, 1, 2);
        case OP_INT_TO_DOUBLE:
            return genConversionCall(cUnit, mir, (void*)__aeabi_i2d, 1, 2);
        case OP_DOUBLE_TO_INT:
            return genConversionCall(cUnit, mir, (void*)__aeabi_d2iz, 2, 1);
        case OP_FLOAT_TO_LONG:
            return genConversionCall(cUnit, mir, (void*)dvmJitf2l, 1, 2);
        case OP_LONG_TO_FLOAT:
            return genConversionCall(cUnit, mir, (void*)__aeabi_l2f, 2, 1);
        case OP_DOUBLE_TO_LONG:
            return genConversionCall(cUnit, mir, (void*)dvmJitd2l, 2, 2);
        case OP_LONG_TO_DOUBLE:
            return genConversionCall(cUnit, mir, (void*)__aeabi_l2d, 2, 2);
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
    int reg0, reg1, reg2;

    /* TODO - find the proper include file to declare these */

    if ( (opCode >= OP_ADD_INT_2ADDR) && (opCode <= OP_REM_DOUBLE_2ADDR)) {
        return genArithOp( cUnit, mir );
    }

    /*
     * If data type is 64-bit, re-calculate the register numbers in the
     * corresponding cases.
     */
    reg0 = selectFirstRegister(cUnit, vSrc2, false);
    reg1 = NEXT_REG(reg0);
    reg2 = NEXT_REG(reg1);

    switch (opCode) {
        case OP_INT_TO_FLOAT:
        case OP_FLOAT_TO_INT:
        case OP_DOUBLE_TO_FLOAT:
        case OP_FLOAT_TO_DOUBLE:
        case OP_INT_TO_DOUBLE:
        case OP_DOUBLE_TO_INT:
        case OP_FLOAT_TO_LONG:
        case OP_LONG_TO_FLOAT:
        case OP_DOUBLE_TO_LONG:
        case OP_LONG_TO_DOUBLE:
            return genConversion(cUnit, mir);
        case OP_NEG_INT:
        case OP_NOT_INT:
            return genArithOpInt(cUnit, mir, vSrc1Dest, vSrc1Dest, vSrc2);
        case OP_NEG_LONG:
        case OP_NOT_LONG:
            return genArithOpLong(cUnit,mir, vSrc1Dest, vSrc1Dest, vSrc2);
        case OP_NEG_FLOAT:
            return genArithOpFloat(cUnit, mir, vSrc1Dest, vSrc1Dest, vSrc2);
        case OP_NEG_DOUBLE:
            return genArithOpDouble(cUnit, mir, vSrc1Dest, vSrc1Dest, vSrc2);
        case OP_MOVE_WIDE: {
            reg0 = selectFirstRegister(cUnit, vSrc2, true);
            reg1 = NEXT_REG(reg0);
            reg2 = NEXT_REG(reg1);

            loadValuePair(cUnit, vSrc2, reg0, reg1);
            storeValuePair(cUnit, reg0, reg1, vSrc1Dest, reg2);
            break;
        }
        case OP_INT_TO_LONG: {
            reg0 = selectFirstRegister(cUnit, vSrc2, true);
            reg1 = NEXT_REG(reg0);
            reg2 = NEXT_REG(reg1);

            loadValue(cUnit, vSrc2, reg0);
            newLIR3(cUnit, THUMB_ASR, reg1, reg0, 31);
            storeValuePair(cUnit, reg0, reg1, vSrc1Dest, reg2);
            break;
        }
        case OP_MOVE:
        case OP_MOVE_OBJECT:
        case OP_LONG_TO_INT:
            loadValue(cUnit, vSrc2, reg0);
            storeValue(cUnit, reg0, vSrc1Dest, reg1);
            break;
        case OP_INT_TO_BYTE:
            loadValue(cUnit, vSrc2, reg0);
            newLIR3(cUnit, THUMB_LSL, reg0, reg0, 24);
            newLIR3(cUnit, THUMB_ASR, reg0, reg0, 24);
            storeValue(cUnit, reg0, vSrc1Dest, reg1);
            break;
        case OP_INT_TO_SHORT:
            loadValue(cUnit, vSrc2, reg0);
            newLIR3(cUnit, THUMB_LSL, reg0, reg0, 16);
            newLIR3(cUnit, THUMB_ASR, reg0, reg0, 16);
            storeValue(cUnit, reg0, vSrc1Dest, reg1);
            break;
        case OP_INT_TO_CHAR:
            loadValue(cUnit, vSrc2, reg0);
            newLIR3(cUnit, THUMB_LSL, reg0, reg0, 16);
            newLIR3(cUnit, THUMB_LSR, reg0, reg0, 16);
            storeValue(cUnit, reg0, vSrc1Dest, reg1);
            break;
        case OP_ARRAY_LENGTH: {
            int lenOffset = offsetof(ArrayObject, length);
            loadValue(cUnit, vSrc2, reg0);
            genNullCheck(cUnit, vSrc2, reg0, mir->offset, NULL);
            newLIR3(cUnit, THUMB_LDR_RRI5, reg0, reg0, lenOffset >> 2);
            storeValue(cUnit, reg0, vSrc1Dest, reg1);
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
    int reg0, reg1, reg2;

    /* It takes few instructions to handle OP_CONST_WIDE_16 inline */
    if (dalvikOpCode == OP_CONST_WIDE_16) {
        int vDest = mir->dalvikInsn.vA;
        int BBBB = mir->dalvikInsn.vB;

        reg0 = selectFirstRegister(cUnit, vNone, true);
        reg1 = NEXT_REG(reg0);
        reg2 = NEXT_REG(reg1);

        loadConstant(cUnit, reg0, BBBB);
        newLIR3(cUnit, THUMB_ASR, reg1, reg0, 31);

        /* Save the long values to the specified Dalvik register pair */
        storeValuePair(cUnit, reg0, reg1, vDest, reg2);
    } else if (dalvikOpCode == OP_CONST_16) {
        int vDest = mir->dalvikInsn.vA;
        int BBBB = mir->dalvikInsn.vB;

        reg0 = selectFirstRegister(cUnit, vNone, false);
        reg1 = NEXT_REG(reg0);

        loadConstant(cUnit, reg0, BBBB);
        storeValue(cUnit, reg0, vDest, reg1);
    } else {
        return true;
    }
    return false;
}

/* Compare agaist zero */
static bool handleFmt21t(CompilationUnit *cUnit, MIR *mir, BasicBlock *bb,
                         ArmLIR *labelList)
{
    OpCode dalvikOpCode = mir->dalvikInsn.opCode;
    ArmConditionCode cond;
    int reg0 = selectFirstRegister(cUnit, mir->dalvikInsn.vA, false);

    loadValue(cUnit, mir->dalvikInsn.vA, reg0);
    newLIR2(cUnit, THUMB_CMP_RI8, reg0, 0);

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
    int reg0, reg1, regDest;

    reg0 = selectFirstRegister(cUnit, vSrc, false);
    reg1 = NEXT_REG(reg0);
    regDest = NEXT_REG(reg1);

    /* TODO: find the proper .h file to declare these */
    int __aeabi_idivmod(int op1, int op2);
    int __aeabi_idiv(int op1, int op2);

    switch (dalvikOpCode) {
        case OP_ADD_INT_LIT8:
        case OP_ADD_INT_LIT16:
            loadValue(cUnit, vSrc, reg0);
            if (lit <= 7 && lit >= 0) {
                newLIR3(cUnit, THUMB_ADD_RRI3, regDest, reg0, lit);
                storeValue(cUnit, regDest, vDest, reg1);
            } else if (lit <= 255 && lit >= 0) {
                newLIR2(cUnit, THUMB_ADD_RI8, reg0, lit);
                storeValue(cUnit, reg0, vDest, reg1);
            } else if (lit >= -7 && lit <= 0) {
                /* Convert to a small constant subtraction */
                newLIR3(cUnit, THUMB_SUB_RRI3, regDest, reg0, -lit);
                storeValue(cUnit, regDest, vDest, reg1);
            } else if (lit >= -255 && lit <= 0) {
                /* Convert to a small constant subtraction */
                newLIR2(cUnit, THUMB_SUB_RI8, reg0, -lit);
                storeValue(cUnit, reg0, vDest, reg1);
            } else {
                loadConstant(cUnit, reg1, lit);
                genBinaryOp(cUnit, vDest, THUMB_ADD_RRR, reg0, reg1, regDest);
            }
            break;

        case OP_RSUB_INT_LIT8:
        case OP_RSUB_INT:
            loadValue(cUnit, vSrc, reg1);
            loadConstant(cUnit, reg0, lit);
            genBinaryOp(cUnit, vDest, THUMB_SUB_RRR, reg0, reg1, regDest);
            break;

        case OP_MUL_INT_LIT8:
        case OP_MUL_INT_LIT16:
        case OP_AND_INT_LIT8:
        case OP_AND_INT_LIT16:
        case OP_OR_INT_LIT8:
        case OP_OR_INT_LIT16:
        case OP_XOR_INT_LIT8:
        case OP_XOR_INT_LIT16:
            loadValue(cUnit, vSrc, reg0);
            loadConstant(cUnit, reg1, lit);
            switch (dalvikOpCode) {
                case OP_MUL_INT_LIT8:
                case OP_MUL_INT_LIT16:
                    armOp = THUMB_MUL;
                    break;
                case OP_AND_INT_LIT8:
                case OP_AND_INT_LIT16:
                    armOp = THUMB_AND_RR;
                    break;
                case OP_OR_INT_LIT8:
                case OP_OR_INT_LIT16:
                    armOp = THUMB_ORR;
                    break;
                case OP_XOR_INT_LIT8:
                case OP_XOR_INT_LIT16:
                    armOp = THUMB_EOR;
                    break;
                default:
                    dvmAbort();
            }
            genBinaryOp(cUnit, vDest, armOp, reg0, reg1, regDest);
            break;

        case OP_SHL_INT_LIT8:
        case OP_SHR_INT_LIT8:
        case OP_USHR_INT_LIT8:
            loadValue(cUnit, vSrc, reg0);
            switch (dalvikOpCode) {
                case OP_SHL_INT_LIT8:
                    armOp = THUMB_LSL;
                    break;
                case OP_SHR_INT_LIT8:
                    armOp = THUMB_ASR;
                    break;
                case OP_USHR_INT_LIT8:
                    armOp = THUMB_LSR;
                    break;
                default: dvmAbort();
            }
            newLIR3(cUnit, armOp, reg0, reg0, lit);
            storeValue(cUnit, reg0, vDest, reg1);
            break;

        case OP_DIV_INT_LIT8:
        case OP_DIV_INT_LIT16:
            /* Register usage based on the calling convention */
            if (lit == 0) {
                /* Let the interpreter deal with div by 0 */
                genInterpSingleStep(cUnit, mir);
                return false;
            }
            loadConstant(cUnit, r2, (int)__aeabi_idiv);
            loadConstant(cUnit, r1, lit);
            loadValue(cUnit, vSrc, r0);
            newLIR1(cUnit, THUMB_BLX_R, r2);
            storeValue(cUnit, r0, vDest, r2);
            break;

        case OP_REM_INT_LIT8:
        case OP_REM_INT_LIT16:
            /* Register usage based on the calling convention */
            if (lit == 0) {
                /* Let the interpreter deal with div by 0 */
                genInterpSingleStep(cUnit, mir);
                return false;
            }
            loadConstant(cUnit, r2, (int)__aeabi_idivmod);
            loadConstant(cUnit, r1, lit);
            loadValue(cUnit, vSrc, r0);
            newLIR1(cUnit, THUMB_BLX_R, r2);
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
            ArmLIR *pcrLabel =
                genRegImmCheck(cUnit, ARM_COND_MI, r1, 0, mir->offset, NULL);
            genExportPC(cUnit, mir, r2, r3 );
            newLIR2(cUnit, THUMB_MOV_IMM,r2,ALLOC_DONT_TRACK);
            newLIR1(cUnit, THUMB_BLX_R, r4PC);
            /*
             * TODO: As coded, we'll bail and reinterpret on alloc failure.
             * Need a general mechanism to bail to thrown exception code.
             */
            genZeroCheck(cUnit, r0, mir->offset, pcrLabel);
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
            loadValue(cUnit, mir->dalvikInsn.vB, r0);  /* Ref */
            loadConstant(cUnit, r2, (int) classPtr );
            newLIR2(cUnit, THUMB_CMP_RI8, r0, 0);    /* Null? */
            /* When taken r0 has NULL which can be used for store directly */
            ArmLIR *branch1 = newLIR2(cUnit, THUMB_B_COND, 4,
                                          ARM_COND_EQ);
            /* r1 now contains object->clazz */
            newLIR3(cUnit, THUMB_LDR_RRI5, r1, r0,
                    offsetof(Object, clazz) >> 2);
            loadConstant(cUnit, r4PC, (int)dvmInstanceofNonTrivial);
            loadConstant(cUnit, r0, 1);                /* Assume true */
            newLIR2(cUnit, THUMB_CMP_RR, r1, r2);
            ArmLIR *branch2 = newLIR2(cUnit, THUMB_B_COND, 2,
                                          ARM_COND_EQ);
            newLIR2(cUnit, THUMB_MOV_RR, r0, r1);
            newLIR2(cUnit, THUMB_MOV_RR, r1, r2);
            newLIR1(cUnit, THUMB_BLX_R, r4PC);
            /* branch target here */
            ArmLIR *target = newLIR0(cUnit, ARM_PSEUDO_TARGET_LABEL);
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
            genIGet(cUnit, mir, THUMB_LDR_RRR, fieldOffset);
            break;
        case OP_IGET_BOOLEAN:
            genIGet(cUnit, mir, THUMB_LDRB_RRR, fieldOffset);
            break;
        case OP_IGET_BYTE:
            genIGet(cUnit, mir, THUMB_LDRSB_RRR, fieldOffset);
            break;
        case OP_IGET_CHAR:
            genIGet(cUnit, mir, THUMB_LDRH_RRR, fieldOffset);
            break;
        case OP_IGET_SHORT:
            genIGet(cUnit, mir, THUMB_LDRSH_RRR, fieldOffset);
            break;
        case OP_IPUT_WIDE:
            genIPutWide(cUnit, mir, fieldOffset);
            break;
        case OP_IPUT:
        case OP_IPUT_OBJECT:
            genIPut(cUnit, mir, THUMB_STR_RRR, fieldOffset);
            break;
        case OP_IPUT_SHORT:
        case OP_IPUT_CHAR:
            genIPut(cUnit, mir, THUMB_STRH_RRR, fieldOffset);
            break;
        case OP_IPUT_BYTE:
        case OP_IPUT_BOOLEAN:
            genIPut(cUnit, mir, THUMB_STRB_RRR, fieldOffset);
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
            genIGet(cUnit, mir, THUMB_LDR_RRR, fieldOffset);
            break;
        case OP_IPUT_QUICK:
        case OP_IPUT_OBJECT_QUICK:
            genIPut(cUnit, mir, THUMB_STR_RRR, fieldOffset);
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
                         ArmLIR *labelList)
{
    OpCode dalvikOpCode = mir->dalvikInsn.opCode;
    ArmConditionCode cond;
    int reg0, reg1;

    if (cUnit->registerScoreboard.liveDalvikReg == (int) mir->dalvikInsn.vA) {
        reg0 = selectFirstRegister(cUnit, mir->dalvikInsn.vA, false);
        reg1 = NEXT_REG(reg0);
        /* Load vB first since vA can be fetched via a move */
        loadValue(cUnit, mir->dalvikInsn.vB, reg1);
        loadValue(cUnit, mir->dalvikInsn.vA, reg0);
    } else {
        reg0 = selectFirstRegister(cUnit, mir->dalvikInsn.vB, false);
        reg1 = NEXT_REG(reg0);
        /* Load vA first since vB can be fetched via a move */
        loadValue(cUnit, mir->dalvikInsn.vA, reg0);
        loadValue(cUnit, mir->dalvikInsn.vB, reg1);
    }
    newLIR2(cUnit, THUMB_CMP_RR, reg0, reg1);

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
    int reg0, reg1, reg2;

    switch (opCode) {
        case OP_MOVE_16:
        case OP_MOVE_OBJECT_16:
        case OP_MOVE_FROM16:
        case OP_MOVE_OBJECT_FROM16: {
            reg0 = selectFirstRegister(cUnit, vSrc2, false);
            reg1 = NEXT_REG(reg0);
            loadValue(cUnit, vSrc2, reg0);
            storeValue(cUnit, reg0, vSrc1Dest, reg1);
            break;
        }
        case OP_MOVE_WIDE_16:
        case OP_MOVE_WIDE_FROM16: {
            reg0 = selectFirstRegister(cUnit, vSrc2, true);
            reg1 = NEXT_REG(reg0);
            reg2 = NEXT_REG(reg1);
            loadValuePair(cUnit, vSrc2, reg0, reg1);
            storeValuePair(cUnit, reg0, reg1, vSrc1Dest, reg2);
            break;
        }
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

    /* Don't optimize for register usage since out-of-line handlers are used */
    if ( (opCode >= OP_ADD_INT) && (opCode <= OP_REM_DOUBLE)) {
        return genArithOp( cUnit, mir );
    }

    switch (opCode) {
        case OP_CMPL_FLOAT:
        case OP_CMPG_FLOAT:
        case OP_CMPL_DOUBLE:
        case OP_CMPG_DOUBLE:
            return genCmpX(cUnit, mir, vA, vB, vC);
        case OP_CMP_LONG:
            loadValuePair(cUnit,vB, r0, r1);
            loadValuePair(cUnit, vC, r2, r3);
            genDispatchToHandler(cUnit, TEMPLATE_CMP_LONG);
            storeValue(cUnit, r0, vA, r1);
            break;
        case OP_AGET_WIDE:
            genArrayGet(cUnit, mir, THUMB_LDR_RRR, vB, vC, vA, 3);
            break;
        case OP_AGET:
        case OP_AGET_OBJECT:
            genArrayGet(cUnit, mir, THUMB_LDR_RRR, vB, vC, vA, 2);
            break;
        case OP_AGET_BOOLEAN:
            genArrayGet(cUnit, mir, THUMB_LDRB_RRR, vB, vC, vA, 0);
            break;
        case OP_AGET_BYTE:
            genArrayGet(cUnit, mir, THUMB_LDRSB_RRR, vB, vC, vA, 0);
            break;
        case OP_AGET_CHAR:
            genArrayGet(cUnit, mir, THUMB_LDRH_RRR, vB, vC, vA, 1);
            break;
        case OP_AGET_SHORT:
            genArrayGet(cUnit, mir, THUMB_LDRSH_RRR, vB, vC, vA, 1);
            break;
        case OP_APUT_WIDE:
            genArrayPut(cUnit, mir, THUMB_STR_RRR, vB, vC, vA, 3);
            break;
        case OP_APUT:
        case OP_APUT_OBJECT:
            genArrayPut(cUnit, mir, THUMB_STR_RRR, vB, vC, vA, 2);
            break;
        case OP_APUT_SHORT:
        case OP_APUT_CHAR:
            genArrayPut(cUnit, mir, THUMB_STRH_RRR, vB, vC, vA, 1);
            break;
        case OP_APUT_BYTE:
        case OP_APUT_BOOLEAN:
            genArrayPut(cUnit, mir, THUMB_STRB_RRR, vB, vC, vA, 0);
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
            newLIR1(cUnit, THUMB_BLX_R, r4PC);
            genZeroCheck(cUnit, r0, mir->offset, NULL);
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
            newLIR1(cUnit, THUMB_BLX_R, r4PC);
            loadConstant(cUnit, r1, (int)(cUnit->method->insns + mir->offset));
            newLIR3(cUnit, THUMB_LDR_RRI5, r2, rGLUE,
                offsetof(InterpState, jitToInterpEntries.dvmJitToInterpNoChain)
                    >> 2);
            newLIR3(cUnit, THUMB_ADD_RRR, r0, r0, r0);
            newLIR3(cUnit, THUMB_ADD_RRR, r4PC, r0, r1);
            newLIR1(cUnit, THUMB_BLX_R, r2);
            break;
        }
        default:
            return true;
    }
    return false;
}

static bool handleFmt35c_3rc(CompilationUnit *cUnit, MIR *mir, BasicBlock *bb,
                             ArmLIR *labelList)
{
    ArmLIR *retChainingCell = NULL;
    ArmLIR *pcrLabel = NULL;

    if (bb->fallThrough != NULL)
        retChainingCell = &labelList[bb->fallThrough->id];

    DecodedInstruction *dInsn = &mir->dalvikInsn;
    switch (mir->dalvikInsn.opCode) {
        /*
         * calleeMethod = this->clazz->vtable[
         *     method->clazz->pDvmDex->pResMethods[BBBB]->methodIndex
         * ]
         */
        case OP_INVOKE_VIRTUAL:
        case OP_INVOKE_VIRTUAL_RANGE: {
            ArmLIR *predChainingCell = &labelList[bb->taken->id];
            int methodIndex =
                cUnit->method->clazz->pDvmDex->pResMethods[dInsn->vB]->
                methodIndex;

            if (mir->dalvikInsn.opCode == OP_INVOKE_VIRTUAL)
                genProcessArgsNoRange(cUnit, mir, dInsn, &pcrLabel);
            else
                genProcessArgsRange(cUnit, mir, dInsn, &pcrLabel);

            genInvokeVirtualCommon(cUnit, mir, methodIndex,
                                   retChainingCell,
                                   predChainingCell,
                                   pcrLabel);
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

            genInvokeSingletonCommon(cUnit, mir, bb, labelList, pcrLabel,
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

            genInvokeSingletonCommon(cUnit, mir, bb, labelList, pcrLabel,
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

            genInvokeSingletonCommon(cUnit, mir, bb, labelList, pcrLabel,
                                     calleeMethod);
            break;
        }
        /*
         * calleeMethod = dvmFindInterfaceMethodInCache(this->clazz,
         *                    BBBB, method, method->clazz->pDvmDex)
         *
         *  Given "invoke-interface {v0}", the following is the generated code:
         *
         * 0x426a9abe : ldr     r0, [r5, #0]   --+
         * 0x426a9ac0 : mov     r7, r5           |
         * 0x426a9ac2 : sub     r7, #24          |
         * 0x426a9ac4 : cmp     r0, #0           | genProcessArgsNoRange
         * 0x426a9ac6 : beq     0x426a9afe       |
         * 0x426a9ac8 : stmia   r7, <r0>       --+
         * 0x426a9aca : ldr     r4, [pc, #104] --> r4 <- dalvikPC of this invoke
         * 0x426a9acc : add     r1, pc, #52    --> r1 <- &retChainingCell
         * 0x426a9ace : add     r2, pc, #60    --> r2 <- &predictedChainingCell
         * 0x426a9ad0 : blx_1   0x426a918c     --+ TEMPLATE_INVOKE_METHOD_
         * 0x426a9ad2 : blx_2   see above      --+     PREDICTED_CHAIN
         * 0x426a9ad4 : b       0x426a9b0c     --> off to the predicted chain
         * 0x426a9ad6 : b       0x426a9afe     --> punt to the interpreter
         * 0x426a9ad8 : mov     r9, r1         --+
         * 0x426a9ada : mov     r10, r2          |
         * 0x426a9adc : mov     r12, r3          |
         * 0x426a9ade : mov     r0, r3           |
         * 0x426a9ae0 : mov     r1, #74          | dvmFindInterfaceMethodInCache
         * 0x426a9ae2 : ldr     r2, [pc, #76]    |
         * 0x426a9ae4 : ldr     r3, [pc, #68]    |
         * 0x426a9ae6 : ldr     r7, [pc, #64]    |
         * 0x426a9ae8 : blx     r7             --+
         * 0x426a9aea : mov     r1, r9         --> r1 <- rechain count
         * 0x426a9aec : cmp     r1, #0         --> compare against 0
         * 0x426a9aee : bgt     0x426a9af8     --> >=0? don't rechain
         * 0x426a9af0 : ldr     r7, [r6, #96]  --+
         * 0x426a9af2 : mov     r2, r10          | dvmJitToPatchPredictedChain
         * 0x426a9af4 : mov     r3, r12          |
         * 0x426a9af6 : blx     r7             --+
         * 0x426a9af8 : add     r1, pc, #8     --> r1 <- &retChainingCell
         * 0x426a9afa : blx_1   0x426a9098     --+ TEMPLATE_INVOKE_METHOD_NO_OPT
         * 0x426a9afc : blx_2   see above      --+
         * -------- reconstruct dalvik PC : 0x428b786c @ +0x001e
         * 0x426a9afe (0042): ldr     r0, [pc, #52]
         * Exception_Handling:
         * 0x426a9b00 (0044): ldr     r1, [r6, #84]
         * 0x426a9b02 (0046): blx     r1
         * 0x426a9b04 (0048): .align4
         * -------- chaining cell (hot): 0x0021
         * 0x426a9b04 (0048): ldr     r0, [r6, #92]
         * 0x426a9b06 (004a): blx     r0
         * 0x426a9b08 (004c): data    0x7872(30834)
         * 0x426a9b0a (004e): data    0x428b(17035)
         * 0x426a9b0c (0050): .align4
         * -------- chaining cell (predicted)
         * 0x426a9b0c (0050): data    0x0000(0) --> will be patched into bx
         * 0x426a9b0e (0052): data    0x0000(0)
         * 0x426a9b10 (0054): data    0x0000(0) --> class
         * 0x426a9b12 (0056): data    0x0000(0)
         * 0x426a9b14 (0058): data    0x0000(0) --> method
         * 0x426a9b16 (005a): data    0x0000(0)
         * 0x426a9b18 (005c): data    0x0000(0) --> reset count
         * 0x426a9b1a (005e): data    0x0000(0)
         * 0x426a9b28 (006c): .word (0xad0392a5)
         * 0x426a9b2c (0070): .word (0x6e750)
         * 0x426a9b30 (0074): .word (0x4109a618)
         * 0x426a9b34 (0078): .word (0x428b786c)
         */
        case OP_INVOKE_INTERFACE:
        case OP_INVOKE_INTERFACE_RANGE: {
            ArmLIR *predChainingCell = &labelList[bb->taken->id];
            int methodIndex = dInsn->vB;

            if (mir->dalvikInsn.opCode == OP_INVOKE_INTERFACE)
                genProcessArgsNoRange(cUnit, mir, dInsn, &pcrLabel);
            else
                genProcessArgsRange(cUnit, mir, dInsn, &pcrLabel);

            /* "this" is already left in r0 by genProcessArgs* */

            /* r4PC = dalvikCallsite */
            loadConstant(cUnit, r4PC,
                         (int) (cUnit->method->insns + mir->offset));

            /* r1 = &retChainingCell */
            ArmLIR *addrRetChain = newLIR2(cUnit, THUMB_ADD_PC_REL,
                                               r1, 0);
            addrRetChain->generic.target = (LIR *) retChainingCell;

            /* r2 = &predictedChainingCell */
            ArmLIR *predictedChainingCell =
                newLIR2(cUnit, THUMB_ADD_PC_REL, r2, 0);
            predictedChainingCell->generic.target = (LIR *) predChainingCell;

            genDispatchToHandler(cUnit, TEMPLATE_INVOKE_METHOD_PREDICTED_CHAIN);

            /* return through lr - jump to the chaining cell */
            genUnconditionalBranch(cUnit, predChainingCell);

            /*
             * null-check on "this" may have been eliminated, but we still need
             * a PC-reconstruction label for stack overflow bailout.
             */
            if (pcrLabel == NULL) {
                int dPC = (int) (cUnit->method->insns + mir->offset);
                pcrLabel = dvmCompilerNew(sizeof(ArmLIR), true);
                pcrLabel->opCode = ARM_PSEUDO_PC_RECONSTRUCTION_CELL;
                pcrLabel->operands[0] = dPC;
                pcrLabel->operands[1] = mir->offset;
                /* Insert the place holder to the growable list */
                dvmInsertGrowableList(&cUnit->pcReconstructionList, pcrLabel);
            }

            /* return through lr+2 - punt to the interpreter */
            genUnconditionalBranch(cUnit, pcrLabel);

            /*
             * return through lr+4 - fully resolve the callee method.
             * r1 <- count
             * r2 <- &predictedChainCell
             * r3 <- this->class
             * r4 <- dPC
             * r7 <- this->class->vtable
             */

            /* Save count, &predictedChainCell, and class to high regs first */
            newLIR2(cUnit, THUMB_MOV_RR_L2H, r9 & THUMB_REG_MASK, r1);
            newLIR2(cUnit, THUMB_MOV_RR_L2H, r10 & THUMB_REG_MASK, r2);
            newLIR2(cUnit, THUMB_MOV_RR_L2H, r12 & THUMB_REG_MASK, r3);

            /* r0 now contains this->clazz */
            newLIR2(cUnit, THUMB_MOV_RR, r0, r3);

            /* r1 = BBBB */
            loadConstant(cUnit, r1, dInsn->vB);

            /* r2 = method (caller) */
            loadConstant(cUnit, r2, (int) cUnit->method);

            /* r3 = pDvmDex */
            loadConstant(cUnit, r3, (int) cUnit->method->clazz->pDvmDex);

            loadConstant(cUnit, r7,
                         (intptr_t) dvmFindInterfaceMethodInCache);
            newLIR1(cUnit, THUMB_BLX_R, r7);

            /* r0 = calleeMethod (returned from dvmFindInterfaceMethodInCache */

            newLIR2(cUnit, THUMB_MOV_RR_H2L, r1, r9 & THUMB_REG_MASK);

            /* Check if rechain limit is reached */
            newLIR2(cUnit, THUMB_CMP_RI8, r1, 0);

            ArmLIR *bypassRechaining =
                newLIR2(cUnit, THUMB_B_COND, 0, ARM_COND_GT);

            newLIR3(cUnit, THUMB_LDR_RRI5, r7, rGLUE,
                    offsetof(InterpState,
                             jitToInterpEntries.dvmJitToPatchPredictedChain)
                    >> 2);

            newLIR2(cUnit, THUMB_MOV_RR_H2L, r2, r10 & THUMB_REG_MASK);
            newLIR2(cUnit, THUMB_MOV_RR_H2L, r3, r12 & THUMB_REG_MASK);

            /*
             * r0 = calleeMethod
             * r2 = &predictedChainingCell
             * r3 = class
             *
             * &returnChainingCell has been loaded into r1 but is not needed
             * when patching the chaining cell and will be clobbered upon
             * returning so it will be reconstructed again.
             */
            newLIR1(cUnit, THUMB_BLX_R, r7);

            /* r1 = &retChainingCell */
            addrRetChain = newLIR3(cUnit, THUMB_ADD_PC_REL,
                                               r1, 0, 0);
            addrRetChain->generic.target = (LIR *) retChainingCell;

            bypassRechaining->generic.target = (LIR *) addrRetChain;

            /*
             * r0 = this, r1 = calleeMethod,
             * r1 = &ChainingCell,
             * r4PC = callsiteDPC,
             */
            genDispatchToHandler(cUnit, TEMPLATE_INVOKE_METHOD_NO_OPT);
#if defined(INVOKE_STATS)
            gDvmJit.invokePredictedChain++;
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
                               BasicBlock *bb, ArmLIR *labelList)
{
    ArmLIR *retChainingCell = &labelList[bb->fallThrough->id];
    ArmLIR *predChainingCell = &labelList[bb->taken->id];
    ArmLIR *pcrLabel = NULL;

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

            genInvokeVirtualCommon(cUnit, mir, methodIndex,
                                   retChainingCell,
                                   predChainingCell,
                                   pcrLabel);
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

            genInvokeSingletonCommon(cUnit, mir, bb, labelList, pcrLabel,
                                     calleeMethod);
            /* Handle exceptions using the interpreter */
            genTrap(cUnit, mir->offset, pcrLabel);
            break;
        }
        default:
            return true;
    }
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
            int offset = offsetof(InterpState, retval);
            int operation = dInsn->vB;

            switch (operation) {
                case INLINE_EMPTYINLINEMETHOD:
                    return false;  /* Nop */
                case INLINE_STRING_LENGTH:
                    return genInlinedStringLength(cUnit, mir);
                case INLINE_MATH_ABS_INT:
                    return genInlinedAbsInt(cUnit, mir);
                case INLINE_MATH_ABS_LONG:
                    return genInlinedAbsLong(cUnit, mir);
                case INLINE_MATH_MIN_INT:
                    return genInlinedMinMaxInt(cUnit, mir, true);
                case INLINE_MATH_MAX_INT:
                    return genInlinedMinMaxInt(cUnit, mir, false);
                case INLINE_STRING_CHARAT:
                    return genInlinedStringCharAt(cUnit, mir);
                case INLINE_MATH_SQRT:
                    if (genInlineSqrt(cUnit, mir))
                        return false;
                    else
                        break;   /* Handle with C routine */
                case INLINE_MATH_COS:
                    if (genInlineCos(cUnit, mir))
                        return false;
                    else
                        break;   /* Handle with C routine */
                case INLINE_MATH_SIN:
                    if (genInlineSin(cUnit, mir))
                        return false;
                    else
                        break;   /* Handle with C routine */
                case INLINE_MATH_ABS_FLOAT:
                    return genInlinedAbsFloat(cUnit, mir);
                case INLINE_MATH_ABS_DOUBLE:
                    return genInlinedAbsDouble(cUnit, mir);
                case INLINE_STRING_COMPARETO:
                case INLINE_STRING_EQUALS:
                    break;
                default:
                    dvmAbort();
            }

            /* Materialize pointer to retval & push */
            newLIR2(cUnit, THUMB_MOV_RR, r4PC, rGLUE);
            newLIR2(cUnit, THUMB_ADD_RI8, r4PC, offset);
            /* Push r4 and (just to take up space) r5) */
            newLIR1(cUnit, THUMB_PUSH, (1<<r4PC | 1<<rFP));

            /* Get code pointer to inline routine */
            loadConstant(cUnit, r4PC, (int)inLineTable[operation].func);

            /* Export PC */
            genExportPC(cUnit, mir, r0, r1 );

            /* Load arguments to r0 through r3 as applicable */
            for (i=0; i < dInsn->vA; i++) {
                loadValue(cUnit, dInsn->arg[i], i);
            }
            /* Call inline routine */
            newLIR1(cUnit, THUMB_BLX_R, r4PC);

            /* Strip frame */
            newLIR1(cUnit, THUMB_ADD_SPI7, 2);

            /* Did we throw? If so, redo under interpreter*/
            genZeroCheck(cUnit, r0, mir->offset, NULL);

            resetRegisterScoreboard(cUnit);
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

/* Chaining cell for code that may need warmup. */
static void handleNormalChainingCell(CompilationUnit *cUnit,
                                     unsigned int offset)
{
    newLIR3(cUnit, THUMB_LDR_RRI5, r0, rGLUE,
        offsetof(InterpState, jitToInterpEntries.dvmJitToInterpNormal) >> 2);
    newLIR1(cUnit, THUMB_BLX_R, r0);
    addWordData(cUnit, (int) (cUnit->method->insns + offset), true);
}

/*
 * Chaining cell for instructions that immediately following already translated
 * code.
 */
static void handleHotChainingCell(CompilationUnit *cUnit,
                                  unsigned int offset)
{
    newLIR3(cUnit, THUMB_LDR_RRI5, r0, rGLUE,
        offsetof(InterpState, jitToInterpEntries.dvmJitToTraceSelect) >> 2);
    newLIR1(cUnit, THUMB_BLX_R, r0);
    addWordData(cUnit, (int) (cUnit->method->insns + offset), true);
}

/* Chaining cell for monomorphic method invocations. */
static void handleInvokeSingletonChainingCell(CompilationUnit *cUnit,
                                              const Method *callee)
{
    newLIR3(cUnit, THUMB_LDR_RRI5, r0, rGLUE,
        offsetof(InterpState, jitToInterpEntries.dvmJitToTraceSelect) >> 2);
    newLIR1(cUnit, THUMB_BLX_R, r0);
    addWordData(cUnit, (int) (callee->insns), true);
}

/* Chaining cell for monomorphic method invocations. */
static void handleInvokePredictedChainingCell(CompilationUnit *cUnit)
{

    /* Should not be executed in the initial state */
    addWordData(cUnit, PREDICTED_CHAIN_BX_PAIR_INIT, true);
    /* To be filled: class */
    addWordData(cUnit, PREDICTED_CHAIN_CLAZZ_INIT, true);
    /* To be filled: method */
    addWordData(cUnit, PREDICTED_CHAIN_METHOD_INIT, true);
    /*
     * Rechain count. The initial value of 0 here will trigger chaining upon
     * the first invocation of this callsite.
     */
    addWordData(cUnit, PREDICTED_CHAIN_COUNTER_INIT, true);
}

/* Load the Dalvik PC into r0 and jump to the specified target */
static void handlePCReconstruction(CompilationUnit *cUnit,
                                   ArmLIR *targetLabel)
{
    ArmLIR **pcrLabel =
        (ArmLIR **) cUnit->pcReconstructionList.elemList;
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
    ArmLIR *labelList =
        dvmCompilerNew(sizeof(ArmLIR) * cUnit->numBlocks, true);
    GrowableList chainingListByType[CHAINING_CELL_LAST];
    int i;

    /*
     * Initialize various types chaining lists.
     */
    for (i = 0; i < CHAINING_CELL_LAST; i++) {
        dvmInitGrowableList(&chainingListByType[i], 2);
    }

    BasicBlock **blockList = cUnit->blockList;

    if (cUnit->executionCount) {
        /*
         * Reserve 6 bytes at the beginning of the trace
         *        +----------------------------+
         *        | execution count (4 bytes)  |
         *        +----------------------------+
         *        | chain cell offset (2 bytes)|
         *        +----------------------------+
         * ...and then code to increment the execution
         * count:
         *       mov   r0, pc       @ move adr of "mov r0,pc" + 4 to r0
         *       sub   r0, #10      @ back up to addr of executionCount
         *       ldr   r1, [r0]
         *       add   r1, #1
         *       str   r1, [r0]
         */
        newLIR1(cUnit, ARM_16BIT_DATA, 0);
        newLIR1(cUnit, ARM_16BIT_DATA, 0);
        cUnit->chainCellOffsetLIR =
            (LIR *) newLIR1(cUnit, ARM_16BIT_DATA, CHAIN_CELL_OFFSET_TAG);
        cUnit->headerSize = 6;
        newLIR2(cUnit, THUMB_MOV_RR_H2L, r0, rpc & THUMB_REG_MASK);
        newLIR2(cUnit, THUMB_SUB_RI8, r0, 10);
        newLIR3(cUnit, THUMB_LDR_RRI5, r1, r0, 0);
        newLIR2(cUnit, THUMB_ADD_RI8, r1, 1);
        newLIR3(cUnit, THUMB_STR_RRI5, r1, r0, 0);
    } else {
         /* Just reserve 2 bytes for the chain cell offset */
        cUnit->chainCellOffsetLIR =
            (LIR *) newLIR1(cUnit, ARM_16BIT_DATA, CHAIN_CELL_OFFSET_TAG);
        cUnit->headerSize = 2;
    }

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
            labelList[i].opCode = ARM_PSEUDO_NORMAL_BLOCK_LABEL;
            /* Reset the register state */
            resetRegisterScoreboard(cUnit);
        } else {
            switch (blockList[i]->blockType) {
                case CHAINING_CELL_NORMAL:
                    labelList[i].opCode = ARM_PSEUDO_CHAINING_CELL_NORMAL;
                    /* handle the codegen later */
                    dvmInsertGrowableList(
                        &chainingListByType[CHAINING_CELL_NORMAL], (void *) i);
                    break;
                case CHAINING_CELL_INVOKE_SINGLETON:
                    labelList[i].opCode =
                        ARM_PSEUDO_CHAINING_CELL_INVOKE_SINGLETON;
                    labelList[i].operands[0] =
                        (int) blockList[i]->containingMethod;
                    /* handle the codegen later */
                    dvmInsertGrowableList(
                        &chainingListByType[CHAINING_CELL_INVOKE_SINGLETON],
                        (void *) i);
                    break;
                case CHAINING_CELL_INVOKE_PREDICTED:
                    labelList[i].opCode =
                        ARM_PSEUDO_CHAINING_CELL_INVOKE_PREDICTED;
                    /* handle the codegen later */
                    dvmInsertGrowableList(
                        &chainingListByType[CHAINING_CELL_INVOKE_PREDICTED],
                        (void *) i);
                    break;
                case CHAINING_CELL_HOT:
                    labelList[i].opCode =
                        ARM_PSEUDO_CHAINING_CELL_HOT;
                    /* handle the codegen later */
                    dvmInsertGrowableList(
                        &chainingListByType[CHAINING_CELL_HOT],
                        (void *) i);
                    break;
                case PC_RECONSTRUCTION:
                    /* Make sure exception handling block is next */
                    labelList[i].opCode =
                        ARM_PSEUDO_PC_RECONSTRUCTION_BLOCK_LABEL;
                    assert (i == cUnit->numBlocks - 2);
                    handlePCReconstruction(cUnit, &labelList[i+1]);
                    break;
                case EXCEPTION_HANDLING:
                    labelList[i].opCode = ARM_PSEUDO_EH_BLOCK_LABEL;
                    if (cUnit->pcReconstructionList.numUsed) {
                        newLIR3(cUnit, THUMB_LDR_RRI5, r1, rGLUE,
                            offsetof(InterpState,
                                     jitToInterpEntries.dvmJitToInterpPunt)
                            >> 2);
                        newLIR1(cUnit, THUMB_BLX_R, r1);
                    }
                    break;
                default:
                    break;
            }
            continue;
        }

        ArmLIR *headLIR = NULL;

        for (mir = blockList[i]->firstMIRInsn; mir; mir = mir->next) {
            OpCode dalvikOpCode = mir->dalvikInsn.opCode;
            InstructionFormat dalvikFormat =
                dexGetInstrFormat(gDvm.instrFormat, dalvikOpCode);
            ArmLIR *boundaryLIR =
                newLIR2(cUnit, ARM_PSEUDO_DALVIK_BYTECODE_BOUNDARY,
                        mir->offset,dalvikOpCode);
            /* Remember the first LIR for this block */
            if (headLIR == NULL) {
                headLIR = boundaryLIR;
            }
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
            }
        }
        /* Eliminate redundant loads/stores and delay stores into later slots */
        dvmCompilerApplyLocalOptimizations(cUnit, (LIR *) headLIR,
                                           cUnit->lastLIRInsn);
        /*
         * Check if the block is terminated due to trace length constraint -
         * insert an unconditional branch to the chaining cell.
         */
        if (blockList[i]->needFallThroughBranch) {
            genUnconditionalBranch(cUnit,
                                   &labelList[blockList[i]->fallThrough->id]);
        }

    }

    /* Handle the chaining cells in predefined order */
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
            newLIR0(cUnit, ARM_PSEUDO_ALIGN4);

            /* Insert the pseudo chaining instruction */
            dvmCompilerAppendLIR(cUnit, (LIR *) &labelList[blockId]);


            switch (blockList[blockId]->blockType) {
                case CHAINING_CELL_NORMAL:
                    handleNormalChainingCell(cUnit,
                      blockList[blockId]->startOffset);
                    break;
                case CHAINING_CELL_INVOKE_SINGLETON:
                    handleInvokeSingletonChainingCell(cUnit,
                        blockList[blockId]->containingMethod);
                    break;
                case CHAINING_CELL_INVOKE_PREDICTED:
                    handleInvokePredictedChainingCell(cUnit);
                    break;
                case CHAINING_CELL_HOT:
                    handleHotChainingCell(cUnit,
                        blockList[blockId]->startOffset);
                    break;
                default:
                    dvmAbort();
                    break;
            }
        }
    }

    dvmCompilerApplyGlobalOptimizations(cUnit);
}

/* Accept the work and start compiling */
bool dvmCompilerDoWork(CompilerWorkOrder *work)
{
   bool res;

   if (gDvmJit.codeCacheFull) {
       return false;
   }

   switch (work->kind) {
       case kWorkOrderMethod:
           res = dvmCompileMethod(work->info, &work->result);
           break;
       case kWorkOrderTrace:
           /* Start compilation with maximally allowed trace length */
           res = dvmCompileTrace(work->info, JIT_MAX_TRACE_LEN, &work->result);
           break;
       default:
           res = false;
           dvmAbort();
   }
   return res;
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
        LOGD("dalvik.vm.jit.op = %s", buf);
    }
}
