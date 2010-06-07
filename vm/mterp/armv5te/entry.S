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
/*
 * Interpreter entry point.
 */

/*
 * We don't have formal stack frames, so gdb scans upward in the code
 * to find the start of the function (a label with the %function type),
 * and then looks at the next few instructions to figure out what
 * got pushed onto the stack.  From this it figures out how to restore
 * the registers, including PC, for the previous stack frame.  If gdb
 * sees a non-function label, it stops scanning, so either we need to
 * have nothing but assembler-local labels between the entry point and
 * the break, or we need to fake it out.
 *
 * When this is defined, we add some stuff to make gdb less confused.
 */
#define ASSIST_DEBUGGER 1

    .text
    .align  2
    .global dvmMterpStdRun
    .type   dvmMterpStdRun, %function

/*
 * On entry:
 *  r0  MterpGlue* glue
 *
 * This function returns a boolean "changeInterp" value.  The return comes
 * via a call to dvmMterpStdBail().
 */
dvmMterpStdRun:
#define MTERP_ENTRY1 \
    .save {r4-r10,fp,lr}; \
    stmfd   sp!, {r4-r10,fp,lr}         @ save 9 regs
#define MTERP_ENTRY2 \
    .pad    #4; \
    sub     sp, sp, #4                  @ align 64

    .fnstart
    MTERP_ENTRY1
    MTERP_ENTRY2

    /* save stack pointer, add magic word for debuggerd */
    str     sp, [r0, #offGlue_bailPtr]  @ save SP for eventual return

    /* set up "named" registers, figure out entry point */
    mov     rGLUE, r0                   @ set rGLUE
    ldr     r1, [r0, #offGlue_entryPoint]   @ enum is 4 bytes in aapcs-EABI
    LOAD_PC_FP_FROM_GLUE()              @ load rPC and rFP from "glue"
    adr     rIBASE, dvmAsmInstructionStart  @ set rIBASE
    cmp     r1, #kInterpEntryInstr      @ usual case?
    bne     .Lnot_instr                 @ no, handle it

#if defined(WITH_JIT)
.LentryInstr:
    ldr     r10, [rGLUE, #offGlue_self] @ callee saved r10 <- glue->self
    /* Entry is always a possible trace start */
    GET_JIT_PROF_TABLE(r0)
    FETCH_INST()
    mov     r1, #0                      @ prepare the value for the new state
    str     r1, [r10, #offThread_inJitCodeCache] @ back to the interp land
    cmp     r0,#0                       @ is profiling disabled?
#if !defined(WITH_SELF_VERIFICATION)
    bne     common_updateProfile        @ profiling is enabled
#else
    ldr     r2, [r10, #offThread_shadowSpace]   @ to find out the jit exit state
    beq     1f                          @ profiling is disabled
    ldr     r3, [r2, #offShadowSpace_jitExitState]  @ jit exit state
    cmp     r3, #kSVSTraceSelect        @ hot trace following?
    moveq   r2,#kJitTSelectRequestHot   @ ask for trace selection
    beq     common_selectTrace          @ go build the trace
    cmp     r3, #kSVSNoProfile          @ don't profile the next instruction?
    beq     1f                          @ intrepret the next instruction
    b       common_updateProfile        @ collect profiles
#endif
1:
    GET_INST_OPCODE(ip)
    GOTO_OPCODE(ip)
#else
    /* start executing the instruction at rPC */
    FETCH_INST()                        @ load rINST from rPC
    GET_INST_OPCODE(ip)                 @ extract opcode from rINST
    GOTO_OPCODE(ip)                     @ jump to next instruction
#endif

.Lnot_instr:
    cmp     r1, #kInterpEntryReturn     @ were we returning from a method?
    beq     common_returnFromMethod

.Lnot_return:
    cmp     r1, #kInterpEntryThrow      @ were we throwing an exception?
    beq     common_exceptionThrown

#if defined(WITH_JIT)
.Lnot_throw:
    ldr     r10,[rGLUE, #offGlue_jitResumeNPC]
    ldr     r2,[rGLUE, #offGlue_jitResumeDPC]
    cmp     r1, #kInterpEntryResume     @ resuming after Jit single-step?
    bne     .Lbad_arg
    cmp     rPC,r2
    bne     .LentryInstr                @ must have branched, don't resume
#if defined(WITH_SELF_VERIFICATION)
    @ glue->entryPoint will be set in dvmSelfVerificationSaveState
    b       jitSVShadowRunStart         @ re-enter the translation after the
                                        @ single-stepped instruction
    @noreturn
#endif
    mov     r1, #kInterpEntryInstr
    str     r1, [rGLUE, #offGlue_entryPoint]
    bx      r10                         @ re-enter the translation
#endif

.Lbad_arg:
    ldr     r0, strBadEntryPoint
    @ r1 holds value of entryPoint
    bl      printf
    bl      dvmAbort
    .fnend


    .global dvmMterpStdBail
    .type   dvmMterpStdBail, %function

/*
 * Restore the stack pointer and PC from the save point established on entry.
 * This is essentially the same as a longjmp, but should be cheaper.  The
 * last instruction causes us to return to whoever called dvmMterpStdRun.
 *
 * We pushed some registers on the stack in dvmMterpStdRun, then saved
 * SP and LR.  Here we restore SP, restore the registers, and then restore
 * LR to PC.
 *
 * On entry:
 *  r0  MterpGlue* glue
 *  r1  bool changeInterp
 */
dvmMterpStdBail:
    ldr     sp, [r0, #offGlue_bailPtr]      @ sp<- saved SP
    mov     r0, r1                          @ return the changeInterp value
    add     sp, sp, #4                      @ un-align 64
    LDMFD_PC "r4-r10,fp"                    @ restore 9 regs and return


/*
 * String references.
 */
strBadEntryPoint:
    .word   .LstrBadEntryPoint
