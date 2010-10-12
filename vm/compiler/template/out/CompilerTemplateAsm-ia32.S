/*
 * This file was generated automatically by gen-template.py for 'ia32'.
 *
 * --> DO NOT EDIT <--
 */

/* File: ia32/header.S */
/*
 * Copyright (C) 2010 The Android Open Source Project
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

#if defined(WITH_JIT)

/*
 * This is a #include, not a %include, because we want the C pre-processor
 * to expand the macros into assembler assignment statements.
 */
#include "../../../mterp/common/asm-constants.h"

/* File: ia32/platform.S */
/*
 * ===========================================================================
 *  CPU-version-specific defines and utility
 * ===========================================================================
 */




    .global dvmCompilerTemplateStart
    .type   dvmCompilerTemplateStart, %function
    .text

dvmCompilerTemplateStart:

/* ------------------------------ */
    .balign 4
    .global dvmCompiler_TEMPLATE_INTERPRET
dvmCompiler_TEMPLATE_INTERPRET:
/* File: ia32/TEMPLATE_INTERPRET.S */
    /*
     * TODO: figure out how best to do this on x86, as we don't have
     * an lr equivalent and probably don't want to push.
     *
     * This handler transfers control to the interpeter without performing
     * any lookups.  It may be called either as part of a normal chaining
     * operation, or from the transition code in header.S.  We distinquish
     * the two cases by looking at the link register.  If called from a
     * translation chain, it will point to the chaining Dalvik PC -3.
     * On entry:
     *    lr - if NULL:
     *        r1 - the Dalvik PC to begin interpretation.
     *    else
     *        [lr, #3] contains Dalvik PC to begin interpretation
     *    rGLUE - pointer to interpState
     *    rFP - Dalvik frame pointer
     *
     *cmp     lr, #0
     *ldrne   r1,[lr, #3]
     *ldr     r2, .LinterpPunt
     *mov     r0, r1                       @ set Dalvik PC
     *bx      r2
     *@ doesn't return
     */

.LinterpPunt:
    .long   dvmJitToInterpPunt

    .size   dvmCompilerTemplateStart, .-dvmCompilerTemplateStart
/* File: ia32/footer.S */
/*
 * ===========================================================================
 *  Common subroutines and data
 * ===========================================================================
 */

    .text
    .align  4
/*
 * FIXME - need a cacheflush for x86
 */
    .global cacheflush
cacheflush:
    movl  $0xdeadf0f0, %eax
    call *%eax


    .global dmvCompilerTemplateEnd
dmvCompilerTemplateEnd:

#endif /* WITH_JIT */

