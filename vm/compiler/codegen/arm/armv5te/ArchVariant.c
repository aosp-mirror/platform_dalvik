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
 * This file is included by Codegen-armv5te.c, and implements architecture
 * variant-specific code.
 */

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

/* Architecture-specific initializations and checks go here */
static bool compilerArchVariantInit(void)
{
    /* First, declare dvmCompiler_TEMPLATE_XXX for each template */
#define JIT_TEMPLATE(X) extern void dvmCompiler_TEMPLATE_##X();
#include "../../../template/armv5te/TemplateOpList.h"
#undef JIT_TEMPLATE

    int i = 0;
    extern void dvmCompilerTemplateStart(void);

    /*
     * Then, populate the templateEntryOffsets array with the offsets from the
     * the dvmCompilerTemplateStart symbol for each template.
     */
#define JIT_TEMPLATE(X) templateEntryOffsets[i++] = \
    (intptr_t) dvmCompiler_TEMPLATE_##X - (intptr_t) dvmCompilerTemplateStart;
#include "../../../template/armv5te/TemplateOpList.h"
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
    return false;   /* punt to C handler */
}

static bool handleConversion(CompilationUnit *cUnit, MIR *mir)
{
    return handleConversionPortable(cUnit, mir);
}

static bool handleArithOpFloat(CompilationUnit *cUnit, MIR *mir,
                               RegLocation rlDest, RegLocation rlSrc1,
                               RegLocation rlSrc2)
{
    return handleArithOpFloatPortable(cUnit, mir, rlDest, rlSrc1, rlSrc2);
}

static bool handleArithOpDouble(CompilationUnit *cUnit, MIR *mir,
                                RegLocation rlDest, RegLocation rlSrc1,
                                RegLocation rlSrc2)
{
    return handleArithOpDoublePortable(cUnit, mir, rlDest, rlSrc1, rlSrc2);
}

static bool handleCmpFP(CompilationUnit *cUnit, MIR *mir, RegLocation rlDest,
                        RegLocation rlSrc1, RegLocation rlSrc2)
{
    RegLocation rlResult = LOC_C_RETURN;
    /*
     * Don't attempt to optimize register usage since these opcodes call out to
     * the handlers.
     */
    switch (mir->dalvikInsn.opCode) {
        case OP_CMPL_FLOAT:
            loadValueDirectFixed(cUnit, rlSrc1, r0);
            loadValueDirectFixed(cUnit, rlSrc2, r1);
            genDispatchToHandler(cUnit, TEMPLATE_CMPL_FLOAT);
            storeValue(cUnit, rlDest, rlResult);
            break;
        case OP_CMPG_FLOAT:
            loadValueDirectFixed(cUnit, rlSrc1, r0);
            loadValueDirectFixed(cUnit, rlSrc2, r1);
            genDispatchToHandler(cUnit, TEMPLATE_CMPG_FLOAT);
            storeValue(cUnit, rlDest, rlResult);
            break;
        case OP_CMPL_DOUBLE:
            loadValueDirectWideFixed(cUnit, rlSrc1, r0, r1);
            loadValueDirectWideFixed(cUnit, rlSrc2, r2, r3);
            genDispatchToHandler(cUnit, TEMPLATE_CMPL_DOUBLE);
            storeValue(cUnit, rlDest, rlResult);
            break;
        case OP_CMPG_DOUBLE:
            loadValueDirectWideFixed(cUnit, rlSrc1, r0, r1);
            loadValueDirectWideFixed(cUnit, rlSrc2, r2, r3);
            genDispatchToHandler(cUnit, TEMPLATE_CMPG_DOUBLE);
            storeValue(cUnit, rlDest, rlResult);
            break;
        default:
            return true;
    }
    return false;
}
