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
#include "../../../template/armv5te/TemplateOpList.h"
#undef JIT_TEMPLATE
    switch (opCode) {
#define JIT_TEMPLATE(X) \
        case TEMPLATE_##X: { templatePtr = dvmCompiler_TEMPLATE_##X; break; }
#include "../../../template/armv5te/TemplateOpList.h"
#undef JIT_TEMPLATE
        default: templatePtr = NULL;
    }
    loadConstant(cUnit, r7, (int) templatePtr);
    newLIR1(cUnit, ARMV5TE_BLX_R, r7);
#endif
}

/* Architecture-specific initializations and checks go here */
bool dvmCompilerArchInit(void)
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

static bool genInlineCos(CompilationUnit *cUnit, MIR *mir)
{
    return false;   /* punt to C handler */
}

static bool genInlineSin(CompilationUnit *cUnit, MIR *mir)
{
    return false;   /* punt to C handler */
}

static bool genConversion(CompilationUnit *cUnit, MIR *mir)
{
    return genConversionPortable(cUnit, mir);
}

static bool genArithOpFloat(CompilationUnit *cUnit, MIR *mir, int vDest,
                        int vSrc1, int vSrc2)
{
    return genArithOpFloatPortable(cUnit, mir, vDest, vSrc1, vSrc2);
}

static bool genArithOpDouble(CompilationUnit *cUnit, MIR *mir, int vDest,
                      int vSrc1, int vSrc2)
{
    return genArithOpDoublePortable(cUnit, mir, vDest, vSrc1, vSrc2);
}

static bool genCmpX(CompilationUnit *cUnit, MIR *mir, int vDest, int vSrc1,
                    int vSrc2)
{
    /*
     * Don't attempt to optimize register usage since these opcodes call out to
     * the handlers.
     */
    switch (mir->dalvikInsn.opCode) {
        case OP_CMPL_FLOAT:
            loadValue(cUnit, vSrc1, r0);
            loadValue(cUnit, vSrc2, r1);
            genDispatchToHandler(cUnit, TEMPLATE_CMPL_FLOAT);
            storeValue(cUnit, r0, vDest, r1);
            break;
        case OP_CMPG_FLOAT:
            loadValue(cUnit, vSrc1, r0);
            loadValue(cUnit, vSrc2, r1);
            genDispatchToHandler(cUnit, TEMPLATE_CMPG_FLOAT);
            storeValue(cUnit, r0, vDest, r1);
            break;
        case OP_CMPL_DOUBLE:
            loadValueAddress(cUnit, vSrc1, r0);
            loadValueAddress(cUnit, vSrc2, r1);
            genDispatchToHandler(cUnit, TEMPLATE_CMPL_DOUBLE);
            storeValue(cUnit, r0, vDest, r1);
            break;
        case OP_CMPG_DOUBLE:
            loadValueAddress(cUnit, vSrc1, r0);
            loadValueAddress(cUnit, vSrc2, r1);
            genDispatchToHandler(cUnit, TEMPLATE_CMPG_DOUBLE);
            storeValue(cUnit, r0, vDest, r1);
            break;
        default:
            return true;
    }
    return false;
}
