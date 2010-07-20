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
JitInstructionSetType dvmCompilerInstructionSet(void)
{
    return DALVIK_JIT_THUMB;
}

/* Architecture-specific initializations and checks go here */
bool dvmCompilerArchVariantInit(void)
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

    /* Target-specific configuration */
    gDvmJit.jitTableSize = 1 << 9; // 512
    gDvmJit.jitTableMask = gDvmJit.jitTableSize - 1;
    gDvmJit.threshold = 200;
    gDvmJit.codeCacheSize = 512*1024;

#if defined(WITH_SELF_VERIFICATION)
    /* Force into blocking mode */
    gDvmJit.blockingMode = true;
    gDvm.nativeDebuggerActive = true;
#endif

    /* Codegen-specific assumptions */
    assert(offsetof(ClassObject, vtable) < 128 &&
           (offsetof(ClassObject, vtable) & 0x3) == 0);
    assert(offsetof(ArrayObject, length) < 128 &&
           (offsetof(ArrayObject, length) & 0x3) == 0);
    assert(offsetof(ArrayObject, contents) < 256);

    /* Up to 5 args are pushed on top of FP - sizeofStackSaveArea */
    assert(sizeof(StackSaveArea) < 236);

    /*
     * EA is calculated by doing "Rn + imm5 << 2", make sure that the last
     * offset from the struct is less than 128.
     */
    assert((offsetof(InterpState, jitToInterpEntries) +
            sizeof(struct JitToInterpEntries)) <= 128);
    return true;
}

int dvmCompilerTargetOptHint(int key)
{
    int res;
    switch (key) {
        case kMaxHoistDistance:
            res = 2;
            break;
        default:
            LOGE("Unknown target optimization hint key: %d",key);
            res = 0;
    }
    return res;
}

void dvmCompilerGenMemBarrier(CompilationUnit *cUnit)
{
#if ANDROID_SMP != 0
#error armv5+smp not supported
#endif
}
