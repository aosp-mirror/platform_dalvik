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

#include <math.h>  // for double sqrt(double)


/*
 * This file is included by Codegen-armv5te-vfp.c, and implements architecture
 * variant-specific code.
 */

#define USE_IN_CACHE_HANDLER 1

/*
 * Determine the initial instruction set to be used for this trace.
 * Later components may decide to change this.
 */
JitInstructionSetType dvmCompilerInstructionSet(CompilationUnit *cUnit)
{
    return DALVIK_JIT_THUMB2;
}

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
    newLIR2(cUnit, THUMB_BLX_1,
            (int) gDvmJit.codeCache + templateEntryOffsets[opCode],
            (int) gDvmJit.codeCache + templateEntryOffsets[opCode]);
    newLIR2(cUnit, THUMB_BLX_2,
            (int) gDvmJit.codeCache + templateEntryOffsets[opCode],
            (int) gDvmJit.codeCache + templateEntryOffsets[opCode]);
#else
    /*
     * In case we want to access the statically compiled handlers for
     * debugging purposes, define USE_IN_CACHE_HANDLER to 0
     */
    void *templatePtr;

#define JIT_TEMPLATE(X) extern void dvmCompiler_TEMPLATE_##X();
#include "../../../template/armv5te-vfp/TemplateOpList.h"
#undef JIT_TEMPLATE
    switch (opCode) {
#define JIT_TEMPLATE(X) \
        case TEMPLATE_##X: { templatePtr = dvmCompiler_TEMPLATE_##X; break; }
#include "../../../template/armv5te-vfp/TemplateOpList.h"
#undef JIT_TEMPLATE
        default: templatePtr = NULL;
    }
    loadConstant(cUnit, r7, (int) templatePtr);
    newLIR1(cUnit, THUMB_BLX_R, r7);
#endif
}

/* Architecture-specific initializations and checks go here */
static bool compilerArchVariantInit(void)
{
    /* First, declare dvmCompiler_TEMPLATE_XXX for each template */
#define JIT_TEMPLATE(X) extern void dvmCompiler_TEMPLATE_##X();
#include "../../../template/armv5te-vfp/TemplateOpList.h"
#undef JIT_TEMPLATE

    int i = 0;
    extern void dvmCompilerTemplateStart(void);

    /*
     * Then, populate the templateEntryOffsets array with the offsets from the
     * the dvmCompilerTemplateStart symbol for each template.
     */
#define JIT_TEMPLATE(X) templateEntryOffsets[i++] = \
    (intptr_t) dvmCompiler_TEMPLATE_##X - (intptr_t) dvmCompilerTemplateStart;
#include "../../../template/armv5te-vfp/TemplateOpList.h"
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

static bool genInlineSqrt(CompilationUnit *cUnit, MIR *mir)
{
    int offset = offsetof(InterpState, retval);
    int vSrc = mir->dalvikInsn.arg[0];
    int vDest = inlinedTarget(mir);
    ArmLIR *branch;
    ArmLIR *target;

    loadDouble(cUnit, vSrc, dr1);
    newLIR2(cUnit, THUMB2_VSQRTD, dr0, dr1);
    newLIR2(cUnit, THUMB2_VCMPD, dr0, dr0);
    newLIR0(cUnit, THUMB2_FMSTAT);
    branch = newLIR2(cUnit, THUMB_B_COND, 0, ARM_COND_EQ);
    loadConstant(cUnit, r2, (int)sqrt);
    newLIR3(cUnit, THUMB2_FMRRD, r0, r1, dr1);
    newLIR1(cUnit, THUMB_BLX_R, r2);
    newLIR3(cUnit, THUMB2_FMDRR, dr0, r0, r1);
    ArmLIR *label = newLIR0(cUnit, ARM_PSEUDO_TARGET_LABEL);
    label->defMask = ENCODE_ALL;
    branch->generic.target = (LIR *)label;
    if (vDest >= 0)
        storeDouble(cUnit, dr0, vDest);
    else
        newLIR3(cUnit, THUMB2_VSTRD, dr0, rGLUE, offset >> 2);
    resetRegisterScoreboard(cUnit);
    return true;
}

static bool genArithOpFloat(CompilationUnit *cUnit, MIR *mir, int vDest,
                                int vSrc1, int vSrc2)
{
    int op = THUMB_BKPT;

    /*
     * Don't attempt to optimize register usage since these opcodes call out to
     * the handlers.
     */
    switch (mir->dalvikInsn.opCode) {
        case OP_ADD_FLOAT_2ADDR:
        case OP_ADD_FLOAT:
            op = THUMB2_VADDS;
            break;
        case OP_SUB_FLOAT_2ADDR:
        case OP_SUB_FLOAT:
            op = THUMB2_VSUBS;
            break;
        case OP_DIV_FLOAT_2ADDR:
        case OP_DIV_FLOAT:
            op = THUMB2_VDIVS;
            break;
        case OP_MUL_FLOAT_2ADDR:
        case OP_MUL_FLOAT:
            op = THUMB2_VMULS;
            break;
        case OP_REM_FLOAT_2ADDR:
        case OP_REM_FLOAT:
        case OP_NEG_FLOAT: {
            return genArithOpFloatPortable(cUnit, mir, vDest, vSrc1, vSrc2);
        }
        default:
            return true;
    }
    int reg0, reg1, reg2;
    reg1 = selectSFPReg(cUnit, vSrc1);
    reg2 = selectSFPReg(cUnit, vSrc2);
    /*
     * The register mapping is overly optimistic and lazily updated so we
     * need to detect false sharing here.
     */
    if (reg1 == reg2 && vSrc1 != vSrc2) {
        reg2 = nextFPReg(cUnit, vSrc2, false /* isDouble */);
    }
    loadFloat(cUnit, vSrc1, reg1);
    loadFloat(cUnit, vSrc2, reg2);
    reg0 = selectSFPReg(cUnit, vDest);
    newLIR3(cUnit, op, reg0, reg1, reg2);
    storeFloat(cUnit, reg0, vDest);
    return false;
}

static bool genArithOpDouble(CompilationUnit *cUnit, MIR *mir, int vDest,
                             int vSrc1, int vSrc2)
{
    int op = THUMB_BKPT;

    switch (mir->dalvikInsn.opCode) {
        case OP_ADD_DOUBLE_2ADDR:
        case OP_ADD_DOUBLE:
            op = THUMB2_VADDD;
            break;
        case OP_SUB_DOUBLE_2ADDR:
        case OP_SUB_DOUBLE:
            op = THUMB2_VSUBD;
            break;
        case OP_DIV_DOUBLE_2ADDR:
        case OP_DIV_DOUBLE:
            op = THUMB2_VDIVD;
            break;
        case OP_MUL_DOUBLE_2ADDR:
        case OP_MUL_DOUBLE:
            op = THUMB2_VMULD;
            break;
        case OP_REM_DOUBLE_2ADDR:
        case OP_REM_DOUBLE:
        case OP_NEG_DOUBLE: {
            return genArithOpDoublePortable(cUnit, mir, vDest, vSrc1, vSrc2);
        }
        default:
            return true;
    }

    int reg0, reg1, reg2;
    reg1 = selectDFPReg(cUnit, vSrc1);
    reg2 = selectDFPReg(cUnit, vSrc2);
    if (reg1 == reg2 && vSrc1 != vSrc2) {
        reg2 = nextFPReg(cUnit, vSrc2, true /* isDouble */);
    }
    loadDouble(cUnit, vSrc1, reg1);
    loadDouble(cUnit, vSrc2, reg2);
    /* Rename the new vDest to a new register */
    reg0 = selectDFPReg(cUnit, vNone);
    newLIR3(cUnit, op, reg0, reg1, reg2);
    storeDouble(cUnit, reg0, vDest);
    return false;
}

static bool genConversion(CompilationUnit *cUnit, MIR *mir)
{
    OpCode opCode = mir->dalvikInsn.opCode;
    int vSrc1Dest = mir->dalvikInsn.vA;
    int vSrc2 = mir->dalvikInsn.vB;
    int op = THUMB_BKPT;
    bool longSrc = false;
    bool longDest = false;
    int srcReg;
    int tgtReg;

    switch (opCode) {
        case OP_INT_TO_FLOAT:
            longSrc = false;
            longDest = false;
            op = THUMB2_VCVTIF;
            break;
        case OP_FLOAT_TO_INT:
            longSrc = false;
            longDest = false;
            op = THUMB2_VCVTFI;
            break;
        case OP_DOUBLE_TO_FLOAT:
            longSrc = true;
            longDest = false;
            op = THUMB2_VCVTDF;
            break;
        case OP_FLOAT_TO_DOUBLE:
            longSrc = false;
            longDest = true;
            op = THUMB2_VCVTFD;
            break;
        case OP_INT_TO_DOUBLE:
            longSrc = false;
            longDest = true;
            op = THUMB2_VCVTID;
            break;
        case OP_DOUBLE_TO_INT:
            longSrc = true;
            longDest = false;
            op = THUMB2_VCVTDI;
            break;
        case OP_FLOAT_TO_LONG:
        case OP_LONG_TO_FLOAT:
        case OP_DOUBLE_TO_LONG:
        case OP_LONG_TO_DOUBLE:
            return genConversionPortable(cUnit, mir);
        default:
            return true;
    }
    if (longSrc) {
        srcReg = selectDFPReg(cUnit, vSrc2);
        loadDouble(cUnit, vSrc2, srcReg);
    } else {
        srcReg = selectSFPReg(cUnit, vSrc2);
        loadFloat(cUnit, vSrc2, srcReg);
    }
    if (longDest) {
        int destReg = selectDFPReg(cUnit, vNone);
        newLIR2(cUnit, op, destReg, srcReg);
        storeDouble(cUnit, destReg, vSrc1Dest);
    } else {
        int destReg = selectSFPReg(cUnit, vNone);
        newLIR2(cUnit, op, destReg, srcReg);
        storeFloat(cUnit, destReg, vSrc1Dest);
    }
    return false;
}

static bool genCmpX(CompilationUnit *cUnit, MIR *mir, int vDest, int vSrc1,
                    int vSrc2)
{
    bool isDouble;
    int defaultResult;
    bool ltNaNBias;
    int fpReg1, fpReg2;

    switch(mir->dalvikInsn.opCode) {
        case OP_CMPL_FLOAT:
            isDouble = false;
            defaultResult = -1;
            break;
        case OP_CMPG_FLOAT:
            isDouble = false;
            defaultResult = 1;
            break;
        case OP_CMPL_DOUBLE:
            isDouble = true;
            defaultResult = -1;
            break;
        case OP_CMPG_DOUBLE:
            isDouble = true;
            defaultResult = 1;
            break;
        default:
            return true;
    }
    if (isDouble) {
        fpReg1 = selectDFPReg(cUnit, vSrc1);
        fpReg2 = selectDFPReg(cUnit, vSrc2);
        if (fpReg1 == fpReg2 && vSrc1 != vSrc2) {
            fpReg2 = nextFPReg(cUnit, vSrc2, true /* isDouble */);
        }
        loadDouble(cUnit, vSrc1, fpReg1);
        loadDouble(cUnit, vSrc2, fpReg2);
        // Hard-coded use of r7 as temp.  Revisit
        loadConstant(cUnit, r7, defaultResult);
        newLIR2(cUnit, THUMB2_VCMPD, fpReg1, fpReg2);
    } else {
        fpReg1 = selectSFPReg(cUnit, vSrc1);
        fpReg2 = selectSFPReg(cUnit, vSrc2);
        if (fpReg1 == fpReg2 && vSrc1 != vSrc2) {
            fpReg2 = nextFPReg(cUnit, vSrc2, false /* isDouble */);
        }
        loadFloat(cUnit, vSrc1, fpReg1);
        loadFloat(cUnit, vSrc2, fpReg2);
        // Hard-coded use of r7 as temp.  Revisit
        loadConstant(cUnit, r7, defaultResult);
        newLIR2(cUnit, THUMB2_VCMPS, fpReg1, fpReg2);
    }
    newLIR0(cUnit, THUMB2_FMSTAT);
    genIT(cUnit, (defaultResult == -1) ? ARM_COND_GT : ARM_COND_MI, "");
    newLIR2(cUnit, THUMB2_MOV_IMM_SHIFT, r7,
            modifiedImmediate(-defaultResult)); // Must not alter ccodes
    genIT(cUnit, ARM_COND_EQ, "");
    loadConstant(cUnit, r7, 0);
    // Hard-coded use of r4PC as temp.  Revisit
    storeValue(cUnit, r7, vDest, r4PC);
    return false;
}
