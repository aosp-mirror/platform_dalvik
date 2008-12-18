/*
 * Copyright (C) 2008 The Android Open Source Project
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


    .text
    .global dvmMterpStdRun
    .type   dvmMterpStdRun, %function
/*
 * bool dvmMterpStdRun(MterpGlue* glue)
 *
 * Interpreter entry point.  Returns changeInterp.
 *
 */
dvmMterpStdRun:
    push    %ebp
    movl    %esp,%ebp
    push    %edi
    push    %esi
    push    %ebx

/* at this point, stack is misaligned by 1 word
   We're allocating spill space for 6 words, plus
   outgoing argument (5 words) and local variables
   (4 words) - 15 words or 60 bytes total. See
   diagram in header.S
*/
    subl   $$60,%esp

/* Set up "named" registers */
    movl    IN_ARG0(%ebp),%ecx
    movl    %ecx,rGLUE_SPILL(%ebp)
    LOAD_PC_FROM_GLUE(%ecx)
    LOAD_FP_FROM_GLUE(%ecx)
    movl    $$dvmAsmInstructionStart,rIBASE

/* Remember %esp for future "longjmp" */
    movl    %esp,offGlue_bailPtr(%ecx)

/* How to start? */
    movb    offGlue_entryPoint(%ecx),%al

/* Normal start? */
    cmpb    $$kInterpEntryInstr,%al
    jne     .Lnot_instr

   /* Normal case: start executing the instruction at rPC */
    FETCH_INST()
    GOTO_NEXT

.Lnot_instr:
    /* Reset to normal case */
    movb   $$kInterpEntryInstr,offGlue_entryPoint(%ecx)
    cmpb   $$kInterpEntryReturn,%al
    je     common_returnFromMethod
    cmpb   $$kInterpEntryThrow,%al
    je     common_exceptionThrown
    movzx  %al,%eax
    movl   %eax,OUT_ARG1(%esp)
    movl   $$.LstrBadEntryPoint,OUT_ARG0(%esp)
    call   printf
    call   dvmAbort
    /* Not reached */


    .global dvmMterpStdBail
    .type   dvmMterpStdBail, %function
/*
 * void dvmMterpStdBail(MterpGlue* glue, bool changeInterp)
 *
 * Restore the stack pointer and PC from the save point established on entry.
 * This is essentially the same as a longjmp, but should be cheaper.  The
 * last instruction causes us to return to whoever called dvmMterpStdRun.
 *
 * We're not going to build a standard frame here, so the arg accesses will
 * look a little strange.
 *
 * On entry:
 *  esp+4 (arg0)  MterpGlue* glue
 *  esp+8 (arg1)  bool changeInterp
 */
dvmMterpStdBail:
    movl    4(%esp),%ecx                 # grab glue
    movl    8(%esp),%eax                 # changeInterp to return reg
    movl    offGlue_bailPtr(%ecx),%esp   # Stack back to normal
    addl    $$60,%esp                    # Strip dvmMterpStdRun's frame
    pop     %ebx
    pop     %esi
    pop     %edi
    pop     %ebp
    ret                                  # return to dvmMterpStdRun's caller


/*
 * Strings
 */
    .section    .rodata
.LstrBadEntryPoint:
    .asciz  "Bad entry point %d\n"
