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
 * bool dvmMterpStdRun(Thread* self)
 *
 * Interpreter entry point.  Returns changeInterp.
 *
 */
dvmMterpStdRun:
    movl    4(%esp), %ecx        # get incoming rSELF
    push    %ebp                 # save caller base pointer
    push    %ecx                 # save rSELF at (%ebp)
    movl    %esp, %ebp           # set our %ebp
/*
 * At this point we've allocated two slots on the stack
 * via push and stack is 8-byte aligned.  Allocate space
 * for 8 spill slots, 3 local slots, 5 arg slots + 2 slots for
 * padding to bring us to 16-byte alignment
 */
    subl    $$(FRAME_SIZE-8), %esp

/* Spill callee save regs */
    movl    %edi,EDI_SPILL(%ebp)
    movl    %esi,ESI_SPILL(%ebp)
    movl    %ebx,EBX_SPILL(%ebp)

/* Set up "named" registers */
    movl    offThread_pc(%ecx),rPC
    movl    offThread_fp(%ecx),rFP
    movl    offThread_curHandlerTable(%ecx),rIBASE

/* Remember %esp for future "longjmp" */
    movl    %esp,offThread_bailPtr(%ecx)

/* How to start? */
    movb    offThread_entryPoint(%ecx),%al

/* Normal start? */
    cmpb    $$kInterpEntryInstr,%al
    jne     .Lnot_instr

   /* Normal case: start executing the instruction at rPC */
    FETCH_INST
    GOTO_NEXT

.Lnot_instr:
    /* Reset to normal case */
    movb   $$kInterpEntryInstr,offThread_entryPoint(%ecx)
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
 * void dvmMterpStdBail(Thread* self, bool changeInterp)
 *
 * Restore the stack pointer and PC from the save point established on entry.
 * This is essentially the same as a longjmp, but should be cheaper.  The
 * last instruction causes us to return to whoever called dvmMterpStdRun.
 *
 * We're not going to build a standard frame here, so the arg accesses will
 * look a little strange.
 *
 * On entry:
 *  esp+4 (arg0)  Thread* self
 *  esp+8 (arg1)  bool changeInterp
 */
dvmMterpStdBail:
    movl    4(%esp),%ecx                 # grab self
    movl    8(%esp),%eax                 # changeInterp to return reg
    movl    offThread_bailPtr(%ecx),%esp   # Restore "setjmp" esp
    movl    %esp,%ebp
    addl    $$(FRAME_SIZE-8), %ebp       # Restore %ebp at point of setjmp
    movl    EDI_SPILL(%ebp),%edi
    movl    ESI_SPILL(%ebp),%esi
    movl    EBX_SPILL(%ebp),%ebx
    movl    PREV_FP(%ebp),%ebp           # restore caller's ebp
    addl    $$FRAME_SIZE,%esp                    # strip frame
    ret                                  # return to dvmMterpStdRun's caller


/*
 * Strings
 */
    .section    .rodata
.LstrBadEntryPoint:
    .asciz  "Bad entry point %d\n"

