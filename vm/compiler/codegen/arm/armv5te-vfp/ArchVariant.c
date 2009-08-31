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

#define USE_IN_CACHE_HANDLER 1

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
bool dvmCompilerArchInit(void)
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
    OpCode opCode = mir->dalvikInsn.opCode;
    int vSrc = mir->dalvikInsn.arg[0];
    loadValueAddress(cUnit, vSrc, r2);
    genDispatchToHandler(cUnit, TEMPLATE_SQRT_DOUBLE_VFP);
    newLIR3(cUnit, THUMB_STR_RRI5, r0, rGLUE, offset >> 2);
    newLIR3(cUnit, THUMB_STR_RRI5, r1, rGLUE, (offset >> 2) + 1);
    resetRegisterScoreboard(cUnit);
    return false;
}

static bool genArithOpFloat(CompilationUnit *cUnit, MIR *mir, int vDest,
                                int vSrc1, int vSrc2)
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
            return genArithOpFloatPortable(cUnit, mir, vDest,
                                                      vSrc1, vSrc2);
        }
        default:
            return true;
    }
    loadValueAddress(cUnit, vDest, r0);
    loadValueAddress(cUnit, vSrc1, r1);
    loadValueAddress(cUnit, vSrc2, r2);
    genDispatchToHandler(cUnit, opCode);
    return false;
}

static bool genArithOpDouble(CompilationUnit *cUnit, MIR *mir, int vDest,
                             int vSrc1, int vSrc2)
{
    TemplateOpCode opCode;

    /*
     * Don't attempt to optimize register usage since these opcodes call out to
     * the handlers.
     */
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
            return genArithOpDoublePortable(cUnit, mir, vDest,
                                                       vSrc1, vSrc2);
        }
        default:
            return true;
    }
    loadValueAddress(cUnit, vDest, r0);
    loadValueAddress(cUnit, vSrc1, r1);
    loadValueAddress(cUnit, vSrc2, r2);
    genDispatchToHandler(cUnit, opCode);
    return false;
}

static bool genConversion(CompilationUnit *cUnit, MIR *mir)
{
    OpCode opCode = mir->dalvikInsn.opCode;
    int vSrc1Dest = mir->dalvikInsn.vA;
    int vSrc2 = mir->dalvikInsn.vB;
    TemplateOpCode template;

    switch (opCode) {
        case OP_INT_TO_FLOAT:
            template = TEMPLATE_INT_TO_FLOAT_VFP;
            break;
        case OP_FLOAT_TO_INT:
            template = TEMPLATE_FLOAT_TO_INT_VFP;
            break;
        case OP_DOUBLE_TO_FLOAT:
            template = TEMPLATE_DOUBLE_TO_FLOAT_VFP;
            break;
        case OP_FLOAT_TO_DOUBLE:
            template = TEMPLATE_FLOAT_TO_DOUBLE_VFP;
            break;
        case OP_INT_TO_DOUBLE:
            template = TEMPLATE_INT_TO_DOUBLE_VFP;
            break;
        case OP_DOUBLE_TO_INT:
            template = TEMPLATE_DOUBLE_TO_INT_VFP;
            break;
        case OP_FLOAT_TO_LONG:
        case OP_LONG_TO_FLOAT:
        case OP_DOUBLE_TO_LONG:
        case OP_LONG_TO_DOUBLE:
            return genConversionPortable(cUnit, mir);
        default:
            return true;
    }
    loadValueAddress(cUnit, vSrc1Dest, r0);
    loadValueAddress(cUnit, vSrc2, r1);
    genDispatchToHandler(cUnit, template);
    return false;
}

static bool genCmpX(CompilationUnit *cUnit, MIR *mir, int vDest, int vSrc1,
                    int vSrc2)
{
    TemplateOpCode template;

    /*
     * Don't attempt to optimize register usage since these opcodes call out to
     * the handlers.
     */
    switch(mir->dalvikInsn.opCode) {
        case OP_CMPL_FLOAT:
            template = TEMPLATE_CMPL_FLOAT_VFP;
            break;
        case OP_CMPG_FLOAT:
            template = TEMPLATE_CMPG_FLOAT_VFP;
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
    loadValueAddress(cUnit, vSrc1, r0);
    loadValueAddress(cUnit, vSrc2, r1);
    genDispatchToHandler(cUnit, template);
    storeValue(cUnit, r0, vDest, r1);
    return false;
}
