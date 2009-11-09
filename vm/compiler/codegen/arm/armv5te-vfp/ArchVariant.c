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
 * This file is included by Codegen-armv5te-vfp.c, and implements architecture
 * variant-specific code.
 */

static void loadValueAddress(CompilationUnit *cUnit, RegLocation rlSrc,
                             int rDest);
/*
 * Determine the initial instruction set to be used for this trace.
 * Later components may decide to change this.
 */
JitInstructionSetType dvmCompilerInstructionSet(CompilationUnit *cUnit)
{
    return DALVIK_JIT_THUMB;
}

/*
 * Jump to the out-of-line handler in ARM mode to finish executing the
 * remaining of more complex instructions.
 */
static void genDispatchToHandler(CompilationUnit *cUnit, TemplateOpCode opCode)
{
    /*
     * NOTE - In practice BLX only needs one operand, but since the assembler
     * may abort itself and retry due to other out-of-range conditions we
     * cannot really use operand[0] to store the absolute target address since
     * it may get clobbered by the final relative offset. Therefore,
     * we fake BLX_1 is a two operand instruction and the absolute target
     * address is stored in operand[1].
     */
    clobberHandlerRegs(cUnit);
    newLIR2(cUnit, kThumbBlx1,
            (int) gDvmJit.codeCache + templateEntryOffsets[opCode],
            (int) gDvmJit.codeCache + templateEntryOffsets[opCode]);
    newLIR2(cUnit, kThumbBlx2,
            (int) gDvmJit.codeCache + templateEntryOffsets[opCode],
            (int) gDvmJit.codeCache + templateEntryOffsets[opCode]);
}

void *dvmCompilerGetInterpretTemplate()
{
    return (void*) ((int)gDvmJit.codeCache +
                    templateEntryOffsets[TEMPLATE_INTERPRET]);
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

/* First, flush any registers associated with this value */
static void loadValueAddress(CompilationUnit *cUnit, RegLocation rlSrc,
                             int rDest)
{
     rlSrc = rlSrc.wide ? updateLocWide(cUnit, rlSrc) : updateLoc(cUnit, rlSrc);
     if (rlSrc.location == kLocPhysReg) {
         if (rlSrc.wide) {
             flushRegWide(cUnit, rlSrc.lowReg, rlSrc.highReg);
         } else {
             flushReg(cUnit, rlSrc.lowReg);
         }
     }
     opRegRegImm(cUnit, kOpAdd, rDest, rFP,
                 sReg2vReg(cUnit, rlSrc.sRegLow) << 2);
}

static bool genInlineSqrt(CompilationUnit *cUnit, MIR *mir)
{
    RegLocation rlSrc = getSrcLocWide(cUnit, mir, 0, 1);
    RegLocation rlResult = LOC_C_RETURN_WIDE;
    RegLocation rlDest = LOC_DALVIK_RETURN_VAL_WIDE;
    loadValueAddress(cUnit, rlSrc, r2);
    genDispatchToHandler(cUnit, TEMPLATE_SQRT_DOUBLE_VFP);
    storeValueWide(cUnit, rlDest, rlResult);
    return false;
}

/*
 * TUNING: On some implementations, it is quicker to pass addresses
 * to the handlers rather than load the operands into core registers
 * and then move the values to FP regs in the handlers.  Other implementations
 * may prefer passing data in registers (and the latter approach would
 * yeild cleaner register handling - avoiding the requirement that operands
 * be flushed to memory prior to the call).
 */
static bool handleArithOpFloat(CompilationUnit *cUnit, MIR *mir,
                               RegLocation rlDest, RegLocation rlSrc1,
                               RegLocation rlSrc2)
{
    TemplateOpCode opCode;

    /*
     * Don't attempt to optimize register usage since these opcodes call out to
     * the handlers.
     */
    switch (mir->dalvikInsn.opCode) {
        case OP_ADD_FLOAT_2ADDR:
        case OP_ADD_FLOAT:
            opCode = TEMPLATE_ADD_FLOAT_VFP;
            break;
        case OP_SUB_FLOAT_2ADDR:
        case OP_SUB_FLOAT:
            opCode = TEMPLATE_SUB_FLOAT_VFP;
            break;
        case OP_DIV_FLOAT_2ADDR:
        case OP_DIV_FLOAT:
            opCode = TEMPLATE_DIV_FLOAT_VFP;
            break;
        case OP_MUL_FLOAT_2ADDR:
        case OP_MUL_FLOAT:
            opCode = TEMPLATE_MUL_FLOAT_VFP;
            break;
        case OP_REM_FLOAT_2ADDR:
        case OP_REM_FLOAT:
        case OP_NEG_FLOAT: {
            return handleArithOpFloatPortable(cUnit, mir, rlDest,
                                              rlSrc1, rlSrc2);
        }
        default:
            return true;
    }
    loadValueAddress(cUnit, rlDest, r0);
    clobberReg(cUnit, r0);
    loadValueAddress(cUnit, rlSrc1, r1);
    clobberReg(cUnit, r1);
    loadValueAddress(cUnit, rlSrc2, r2);
    genDispatchToHandler(cUnit, opCode);
    rlDest = updateLoc(cUnit, rlDest);
    if (rlDest.location == kLocPhysReg) {
        clobberReg(cUnit, rlDest.lowReg);
    }
    return false;
}

static bool handleArithOpDouble(CompilationUnit *cUnit, MIR *mir,
                                RegLocation rlDest, RegLocation rlSrc1,
                                RegLocation rlSrc2)
{
    TemplateOpCode opCode;

    switch (mir->dalvikInsn.opCode) {
        case OP_ADD_DOUBLE_2ADDR:
        case OP_ADD_DOUBLE:
            opCode = TEMPLATE_ADD_DOUBLE_VFP;
            break;
        case OP_SUB_DOUBLE_2ADDR:
        case OP_SUB_DOUBLE:
            opCode = TEMPLATE_SUB_DOUBLE_VFP;
            break;
        case OP_DIV_DOUBLE_2ADDR:
        case OP_DIV_DOUBLE:
            opCode = TEMPLATE_DIV_DOUBLE_VFP;
            break;
        case OP_MUL_DOUBLE_2ADDR:
        case OP_MUL_DOUBLE:
            opCode = TEMPLATE_MUL_DOUBLE_VFP;
            break;
        case OP_REM_DOUBLE_2ADDR:
        case OP_REM_DOUBLE:
        case OP_NEG_DOUBLE: {
            return handleArithOpDoublePortable(cUnit, mir, rlDest, rlSrc1,
                                               rlSrc2);
        }
        default:
            return true;
    }
    loadValueAddress(cUnit, rlDest, r0);
    clobberReg(cUnit, r0);
    loadValueAddress(cUnit, rlSrc1, r1);
    clobberReg(cUnit, r1);
    loadValueAddress(cUnit, rlSrc2, r2);
    genDispatchToHandler(cUnit, opCode);
    rlDest = updateLocWide(cUnit, rlDest);
    if (rlDest.location == kLocPhysReg) {
        clobberReg(cUnit, rlDest.lowReg);
        clobberReg(cUnit, rlDest.highReg);
    }
    return false;
}

static bool handleConversion(CompilationUnit *cUnit, MIR *mir)
{
    OpCode opCode = mir->dalvikInsn.opCode;
    bool longSrc = false;
    bool longDest = false;
    RegLocation rlSrc;
    RegLocation rlDest;
    TemplateOpCode template;
    switch (opCode) {
        case OP_INT_TO_FLOAT:
            longSrc = false;
            longDest = false;
            template = TEMPLATE_INT_TO_FLOAT_VFP;
            break;
        case OP_FLOAT_TO_INT:
            longSrc = false;
            longDest = false;
            template = TEMPLATE_FLOAT_TO_INT_VFP;
            break;
        case OP_DOUBLE_TO_FLOAT:
            longSrc = true;
            longDest = false;
            template = TEMPLATE_DOUBLE_TO_FLOAT_VFP;
            break;
        case OP_FLOAT_TO_DOUBLE:
            longSrc = false;
            longDest = true;
            template = TEMPLATE_FLOAT_TO_DOUBLE_VFP;
            break;
        case OP_INT_TO_DOUBLE:
            longSrc = false;
            longDest = true;
            template = TEMPLATE_INT_TO_DOUBLE_VFP;
            break;
        case OP_DOUBLE_TO_INT:
            longSrc = true;
            longDest = false;
            template = TEMPLATE_DOUBLE_TO_INT_VFP;
            break;
        case OP_LONG_TO_DOUBLE:
        case OP_FLOAT_TO_LONG:
        case OP_LONG_TO_FLOAT:
        case OP_DOUBLE_TO_LONG:
            return handleConversionPortable(cUnit, mir);
        default:
            return true;
    }

    if (longSrc) {
        rlSrc = getSrcLocWide(cUnit, mir, 0, 1);
    } else {
        rlSrc = getSrcLoc(cUnit, mir, 0);
    }

    if (longDest) {
        rlDest = getDestLocWide(cUnit, mir, 0, 1);
    } else {
        rlDest = getDestLoc(cUnit, mir, 0);
    }
    loadValueAddress(cUnit, rlDest, r0);
    clobberReg(cUnit, r0);
    loadValueAddress(cUnit, rlSrc, r1);
    genDispatchToHandler(cUnit, template);
    if (rlDest.wide) {
        rlDest = updateLocWide(cUnit, rlDest);
        clobberReg(cUnit, rlDest.highReg);
    } else {
        rlDest = updateLoc(cUnit, rlDest);
    }
    clobberReg(cUnit, rlDest.lowReg);
    return false;
}

static bool handleCmpFP(CompilationUnit *cUnit, MIR *mir, RegLocation rlDest,
                        RegLocation rlSrc1, RegLocation rlSrc2)
{
    TemplateOpCode template;
    RegLocation rlResult = getReturnLoc(cUnit);
    bool wide = true;

    switch(mir->dalvikInsn.opCode) {
        case OP_CMPL_FLOAT:
            template = TEMPLATE_CMPL_FLOAT_VFP;
            wide = false;
            break;
        case OP_CMPG_FLOAT:
            template = TEMPLATE_CMPG_FLOAT_VFP;
            wide = false;
            break;
        case OP_CMPL_DOUBLE:
            template = TEMPLATE_CMPL_DOUBLE_VFP;
            break;
        case OP_CMPG_DOUBLE:
            template = TEMPLATE_CMPG_DOUBLE_VFP;
            break;
        default:
            return true;
    }
    loadValueAddress(cUnit, rlSrc1, r0);
    clobberReg(cUnit, r0);
    loadValueAddress(cUnit, rlSrc2, r1);
    genDispatchToHandler(cUnit, template);
    storeValue(cUnit, rlDest, rlResult);
    return false;
}
