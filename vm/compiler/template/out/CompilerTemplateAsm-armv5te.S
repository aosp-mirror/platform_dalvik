/*
 * This file was generated automatically by gen-template.py for 'armv5te'.
 *
 * --> DO NOT EDIT <--
 */

/* File: armv5te/header.S */
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

#if defined(WITH_JIT)

/*
 * ARMv5 definitions and declarations.
 */

/*
ARM EABI general notes:

r0-r3 hold first 4 args to a method; they are not preserved across method calls
r4-r8 are available for general use
r9 is given special treatment in some situations, but not for us
r10 (sl) seems to be generally available
r11 (fp) is used by gcc (unless -fomit-frame-pointer is set)
r12 (ip) is scratch -- not preserved across method calls
r13 (sp) should be managed carefully in case a signal arrives
r14 (lr) must be preserved
r15 (pc) can be tinkered with directly

r0 holds returns of <= 4 bytes
r0-r1 hold returns of 8 bytes, low word in r0

Callee must save/restore r4+ (except r12) if it modifies them.

Stack is "full descending".  Only the arguments that don't fit in the first 4
registers are placed on the stack.  "sp" points at the first stacked argument
(i.e. the 5th arg).

VFP: single-precision results in s0, double-precision results in d0.

In the EABI, "sp" must be 64-bit aligned on entry to a function, and any
64-bit quantities (long long, double) must be 64-bit aligned.
*/

/*
JIT and ARM notes:

The following registers have fixed assignments:

  reg nick      purpose
  r5  rFP       interpreted frame pointer, used for accessing locals and args
  r6  rGLUE     MterpGlue pointer

The following registers have fixed assignments in mterp but are scratch
registers in compiled code

  reg nick      purpose
  r4  rPC       interpreted program counter, used for fetching instructions
  r7  rINST     first 16-bit code unit of current instruction
  r8  rIBASE    interpreted instruction base pointer, used for computed goto

Macros are provided for common operations.  Each macro MUST emit only
one instruction to make instruction-counting easier.  They MUST NOT alter
unspecified registers or condition codes.
*/

/* single-purpose registers, given names for clarity */
#define rPC     r4
#define rFP     r5
#define rGLUE   r6
#define rINST   r7
#define rIBASE  r8

/*
 * Given a frame pointer, find the stack save area.
 *
 * In C this is "((StackSaveArea*)(_fp) -1)".
 */
#define SAVEAREA_FROM_FP(_reg, _fpreg) \
    sub     _reg, _fpreg, #sizeofStackSaveArea

/*
 * This is a #include, not a %include, because we want the C pre-processor
 * to expand the macros into assembler assignment statements.
 */
#include "../../../mterp/common/asm-constants.h"


/* File: armv5te/platform.S */
/*
 * ===========================================================================
 *  CPU-version-specific defines
 * ===========================================================================
 */

/*
 * Macro for "MOV LR,PC / LDR PC,xxx", which is not allowed pre-ARMv5.
 * Jump to subroutine.
 *
 * May modify IP and LR.
 */
.macro  LDR_PC_LR source
    mov     lr, pc
    ldr     pc, \source
.endm


    .global dvmCompilerTemplateStart
    .type   dvmCompilerTemplateStart, %function
    .text

dvmCompilerTemplateStart:

/* ------------------------------ */
    .balign 4
    .global dvmCompiler_TEMPLATE_CMP_LONG
dvmCompiler_TEMPLATE_CMP_LONG:
/* File: armv5te/TEMPLATE_CMP_LONG.S */
    /*
     * Compare two 64-bit values.  Puts 0, 1, or -1 into the destination
     * register based on the results of the comparison.
     *
     * We load the full values with LDM, but in practice many values could
     * be resolved by only looking at the high word.  This could be made
     * faster or slower by splitting the LDM into a pair of LDRs.
     *
     * If we just wanted to set condition flags, we could do this:
     *  subs    ip, r0, r2
     *  sbcs    ip, r1, r3
     *  subeqs  ip, r0, r2
     * Leaving { <0, 0, >0 } in ip.  However, we have to set it to a specific
     * integer value, which we can do with 2 conditional mov/mvn instructions
     * (set 1, set -1; if they're equal we already have 0 in ip), giving
     * us a constant 5-cycle path plus a branch at the end to the
     * instruction epilogue code.  The multi-compare approach below needs
     * 2 or 3 cycles + branch if the high word doesn't match, 6 + branch
     * in the worst case (the 64-bit values are equal).
     */
    /* cmp-long vAA, vBB, vCC */
    cmp     r1, r3                      @ compare (vBB+1, vCC+1)
    blt     .LTEMPLATE_CMP_LONG_less            @ signed compare on high part
    bgt     .LTEMPLATE_CMP_LONG_greater
    subs    r0, r0, r2                  @ r0<- r0 - r2
    bxeq     lr
    bhi     .LTEMPLATE_CMP_LONG_greater         @ unsigned compare on low part
.LTEMPLATE_CMP_LONG_less:
    mvn     r0, #0                      @ r0<- -1
    bx      lr
.LTEMPLATE_CMP_LONG_greater:
    mov     r0, #1                      @ r0<- 1
    bx      lr


/* ------------------------------ */
    .balign 4
    .global dvmCompiler_TEMPLATE_RETURN
dvmCompiler_TEMPLATE_RETURN:
/* File: armv5te/TEMPLATE_RETURN.S */
    /*
     * Unwind a frame from the Dalvik stack for compiled OP_RETURN_XXX.
     * If the stored value in returnAddr
     * is non-zero, the caller is compiled by the JIT thus return to the
     * address in the code cache following the invoke instruction. Otherwise
     * return to the special dvmJitToInterpNoChain entry point.
     */
    SAVEAREA_FROM_FP(r0, rFP)           @ r0<- saveArea (old)
    ldr     r10, [r0, #offStackSaveArea_prevFrame] @ r10<- saveArea->prevFrame
    ldr     r8, [rGLUE, #offGlue_pSelfSuspendCount] @ r8<- &suspendCount
    ldr     rPC, [r0, #offStackSaveArea_savedPc] @ rPC<- saveArea->savedPc
#if !defined(WITH_SELF_VERIFICATION)
    ldr     r9,  [r0, #offStackSaveArea_returnAddr] @ r9<- chaining cell ret
#else
    mov     r9, #0                      @ disable chaining
#endif
    ldr     r2, [r10, #(offStackSaveArea_method - sizeofStackSaveArea)]
                                        @ r2<- method we're returning to
    ldr     r3, [rGLUE, #offGlue_self]  @ r3<- glue->self
    cmp     r2, #0                      @ break frame?
#if !defined(WITH_SELF_VERIFICATION)
    beq     1f                          @ bail to interpreter
#else
    blxeq   lr                          @ punt to interpreter and compare state
#endif
    ldr     r0, .LdvmJitToInterpNoChain @ defined in footer.S
    mov     rFP, r10                    @ publish new FP
    ldrne   r10, [r2, #offMethod_clazz] @ r10<- method->clazz
    ldr     r8, [r8]                    @ r8<- suspendCount

    str     r2, [rGLUE, #offGlue_method]@ glue->method = newSave->method
    ldr     r1, [r10, #offClassObject_pDvmDex] @ r1<- method->clazz->pDvmDex
    str     rFP, [r3, #offThread_curFrame] @ self->curFrame = fp
    add     rPC, rPC, #6                @ publish new rPC (advance 6 bytes)
    str     r1, [rGLUE, #offGlue_methodClassDex]
    cmp     r8, #0                      @ check the suspendCount
    movne   r9, #0                      @ clear the chaining cell address
    cmp     r9, #0                      @ chaining cell exists?
    blxne   r9                          @ jump to the chaining cell
    mov     pc, r0                      @ callsite is interpreted
1:
    stmia   rGLUE, {rPC, rFP}           @ SAVE_PC_FP_TO_GLUE()
    ldr     r2, .LdvmMterpStdBail       @ defined in footer.S
    mov     r1, #0                      @ changeInterp = false
    mov     r0, rGLUE                   @ Expecting rGLUE in r0
    blx     r2                          @ exit the interpreter

/* ------------------------------ */
    .balign 4
    .global dvmCompiler_TEMPLATE_INVOKE_METHOD_NO_OPT
dvmCompiler_TEMPLATE_INVOKE_METHOD_NO_OPT:
/* File: armv5te/TEMPLATE_INVOKE_METHOD_NO_OPT.S */
    /*
     * For polymorphic callsites - setup the Dalvik frame and load Dalvik PC
     * into rPC then jump to dvmJitToInterpNoChain to dispatch the
     * runtime-resolved callee.
     */
    @ r0 = methodToCall, r1 = returnCell, rPC = dalvikCallsite
    ldrh    r7, [r0, #offMethod_registersSize]  @ r7<- methodToCall->regsSize
    ldrh    r2, [r0, #offMethod_outsSize]  @ r2<- methodToCall->outsSize
    ldr     r9, [rGLUE, #offGlue_interpStackEnd]    @ r9<- interpStackEnd
    ldr     r8, [rGLUE, #offGlue_pSelfSuspendCount] @ r8<- &suspendCount
    add     r3, r1, #1  @ Thumb addr is odd
    SAVEAREA_FROM_FP(r1, rFP)           @ r1<- stack save area
    sub     r1, r1, r7, lsl #2          @ r1<- newFp (old savearea - regsSize)
    SAVEAREA_FROM_FP(r10, r1)           @ r10<- stack save area
    sub     r10, r10, r2, lsl #2        @ r10<- bottom (newsave - outsSize)
    ldr     r8, [r8]                    @ r3<- suspendCount (int)
    cmp     r10, r9                     @ bottom < interpStackEnd?
    bxlt    lr                          @ return to raise stack overflow excep.
    @ r1 = newFP, r0 = methodToCall, r3 = returnCell, rPC = dalvikCallsite
    ldr     r9, [r0, #offMethod_clazz]      @ r9<- method->clazz
    ldr     r10, [r0, #offMethod_accessFlags] @ r10<- methodToCall->accessFlags
    str     rPC, [rFP, #(offStackSaveArea_currentPc - sizeofStackSaveArea)]
    str     rPC, [r1, #(offStackSaveArea_savedPc - sizeofStackSaveArea)]
    ldr     rPC, [r0, #offMethod_insns]     @ rPC<- methodToCall->insns


    @ set up newSaveArea
    str     rFP, [r1, #(offStackSaveArea_prevFrame - sizeofStackSaveArea)]
    str     r3, [r1, #(offStackSaveArea_returnAddr - sizeofStackSaveArea)]
    str     r0, [r1, #(offStackSaveArea_method - sizeofStackSaveArea)]
    cmp     r8, #0                      @ suspendCount != 0
    bxne    lr                          @ bail to the interpreter
    tst     r10, #ACC_NATIVE
#if !defined(WITH_SELF_VERIFICATION)
    bne     .LinvokeNative
#else
    bxne    lr                          @ bail to the interpreter
#endif

    ldr     r10, .LdvmJitToInterpNoChain
    ldr     r3, [r9, #offClassObject_pDvmDex] @ r3<- method->clazz->pDvmDex
    ldr     r2, [rGLUE, #offGlue_self]      @ r2<- glue->self

    @ Update "glue" values for the new method
    str     r0, [rGLUE, #offGlue_method]    @ glue->method = methodToCall
    str     r3, [rGLUE, #offGlue_methodClassDex] @ glue->methodClassDex = ...
    mov     rFP, r1                         @ fp = newFp
    str     rFP, [r2, #offThread_curFrame]  @ self->curFrame = newFp

    @ Start executing the callee
    mov     pc, r10                         @ dvmJitToInterpNoChain

/* ------------------------------ */
    .balign 4
    .global dvmCompiler_TEMPLATE_INVOKE_METHOD_CHAIN
dvmCompiler_TEMPLATE_INVOKE_METHOD_CHAIN:
/* File: armv5te/TEMPLATE_INVOKE_METHOD_CHAIN.S */
    /*
     * For monomorphic callsite, setup the Dalvik frame and return to the
     * Thumb code through the link register to transfer control to the callee
     * method through a dedicated chaining cell.
     */
    @ r0 = methodToCall, r1 = returnCell, rPC = dalvikCallsite
    @ methodToCall is guaranteed to be non-native
.LinvokeChain:
    ldrh    r7, [r0, #offMethod_registersSize]  @ r7<- methodToCall->regsSize
    ldrh    r2, [r0, #offMethod_outsSize]  @ r2<- methodToCall->outsSize
    ldr     r9, [rGLUE, #offGlue_interpStackEnd]    @ r9<- interpStackEnd
    ldr     r8, [rGLUE, #offGlue_pSelfSuspendCount] @ r8<- &suspendCount
    add     r3, r1, #1  @ Thumb addr is odd
    SAVEAREA_FROM_FP(r1, rFP)           @ r1<- stack save area
    sub     r1, r1, r7, lsl #2          @ r1<- newFp (old savearea - regsSize)
    SAVEAREA_FROM_FP(r10, r1)           @ r10<- stack save area
    add     r12, lr, #2                 @ setup the punt-to-interp address
    sub     r10, r10, r2, lsl #2        @ r10<- bottom (newsave - outsSize)
    ldr     r8, [r8]                    @ r3<- suspendCount (int)
    cmp     r10, r9                     @ bottom < interpStackEnd?
    bxlt    r12                         @ return to raise stack overflow excep.
    @ r1 = newFP, r0 = methodToCall, r3 = returnCell, rPC = dalvikCallsite
    ldr     r9, [r0, #offMethod_clazz]      @ r9<- method->clazz
    str     rPC, [rFP, #(offStackSaveArea_currentPc - sizeofStackSaveArea)]
    str     rPC, [r1, #(offStackSaveArea_savedPc - sizeofStackSaveArea)]
    ldr     rPC, [r0, #offMethod_insns]     @ rPC<- methodToCall->insns


    @ set up newSaveArea
    str     rFP, [r1, #(offStackSaveArea_prevFrame - sizeofStackSaveArea)]
    str     r3, [r1, #(offStackSaveArea_returnAddr - sizeofStackSaveArea)]
    str     r0, [r1, #(offStackSaveArea_method - sizeofStackSaveArea)]
    cmp     r8, #0                      @ suspendCount != 0
    bxne    r12                         @ bail to the interpreter

    ldr     r3, [r9, #offClassObject_pDvmDex] @ r3<- method->clazz->pDvmDex
    ldr     r2, [rGLUE, #offGlue_self]      @ r2<- glue->self

    @ Update "glue" values for the new method
    str     r0, [rGLUE, #offGlue_method]    @ glue->method = methodToCall
    str     r3, [rGLUE, #offGlue_methodClassDex] @ glue->methodClassDex = ...
    mov     rFP, r1                         @ fp = newFp
    str     rFP, [r2, #offThread_curFrame]  @ self->curFrame = newFp

    bx      lr                              @ return to the callee-chaining cell



/* ------------------------------ */
    .balign 4
    .global dvmCompiler_TEMPLATE_INVOKE_METHOD_PREDICTED_CHAIN
dvmCompiler_TEMPLATE_INVOKE_METHOD_PREDICTED_CHAIN:
/* File: armv5te/TEMPLATE_INVOKE_METHOD_PREDICTED_CHAIN.S */
    /*
     * For polymorphic callsite, check whether the cached class pointer matches
     * the current one. If so setup the Dalvik frame and return to the
     * Thumb code through the link register to transfer control to the callee
     * method through a dedicated chaining cell.
     *
     * The predicted chaining cell is declared in ArmLIR.h with the
     * following layout:
     *
     *  typedef struct PredictedChainingCell {
     *      u4 branch;
     *      const ClassObject *clazz;
     *      const Method *method;
     *      u4 counter;
     *  } PredictedChainingCell;
     *
     * Upon returning to the callsite:
     *    - lr  : to branch to the chaining cell
     *    - lr+2: to punt to the interpreter
     *    - lr+4: to fully resolve the callee and may rechain.
     *            r3 <- class
     *            r9 <- counter
     */
    @ r0 = this, r1 = returnCell, r2 = predictedChainCell, rPC = dalvikCallsite
    ldr     r3, [r0, #offObject_clazz]  @ r3 <- this->class
    ldr     r8, [r2, #4]    @ r8 <- predictedChainCell->clazz
    ldr     r0, [r2, #8]    @ r0 <- predictedChainCell->method
    ldr     r9, [r2, #12]   @ r9 <- predictedChainCell->counter
    cmp     r3, r8          @ predicted class == actual class?
    beq     .LinvokeChain   @ predicted chain is valid
    ldr     r7, [r3, #offClassObject_vtable] @ r7 <- this->class->vtable
    sub     r1, r9, #1      @ count--
    str     r1, [r2, #12]   @ write back to PredictedChainingCell->counter
    add     lr, lr, #4      @ return to fully-resolve landing pad
    /*
     * r1 <- count
     * r2 <- &predictedChainCell
     * r3 <- this->class
     * r4 <- dPC
     * r7 <- this->class->vtable
     */
    bx      lr

/* ------------------------------ */
    .balign 4
    .global dvmCompiler_TEMPLATE_INVOKE_METHOD_NATIVE
dvmCompiler_TEMPLATE_INVOKE_METHOD_NATIVE:
/* File: armv5te/TEMPLATE_INVOKE_METHOD_NATIVE.S */
    @ r0 = methodToCall, r1 = returnCell, rPC = dalvikCallsite
    ldrh    r7, [r0, #offMethod_registersSize]  @ r7<- methodToCall->regsSize
    ldr     r9, [rGLUE, #offGlue_interpStackEnd]    @ r9<- interpStackEnd
    ldr     r8, [rGLUE, #offGlue_pSelfSuspendCount] @ r8<- &suspendCount
    add     r3, r1, #1  @ Thumb addr is odd
    SAVEAREA_FROM_FP(r1, rFP)           @ r1<- stack save area
    sub     r1, r1, r7, lsl #2          @ r1<- newFp (old savearea - regsSize)
    SAVEAREA_FROM_FP(r10, r1)           @ r10<- stack save area
    ldr     r8, [r8]                    @ r3<- suspendCount (int)
    cmp     r10, r9                     @ bottom < interpStackEnd?
    bxlt    lr                          @ return to raise stack overflow excep.
    @ r1 = newFP, r0 = methodToCall, r3 = returnCell, rPC = dalvikCallsite
    str     rPC, [rFP, #(offStackSaveArea_currentPc - sizeofStackSaveArea)]
    str     rPC, [r1, #(offStackSaveArea_savedPc - sizeofStackSaveArea)]
    ldr     rPC, [r0, #offMethod_insns]     @ rPC<- methodToCall->insns


    @ set up newSaveArea
    str     rFP, [r1, #(offStackSaveArea_prevFrame - sizeofStackSaveArea)]
    str     r3, [r1, #(offStackSaveArea_returnAddr - sizeofStackSaveArea)]
    ldr     r3, [rGLUE, #offGlue_self]      @ r3<- glue->self
    str     r0, [r1, #(offStackSaveArea_method - sizeofStackSaveArea)]
    cmp     r8, #0                      @ suspendCount != 0
    ldr     r8, [r0, #offMethod_nativeFunc] @ r8<- method->nativeFunc
#if !defined(WITH_SELF_VERIFICATION)
    bxne    lr                          @ bail to the interpreter
#else
    bx      lr                          @ bail to interpreter unconditionally
#endif

    @ go ahead and transfer control to the native code
    ldr     r9, [r3, #offThread_jniLocal_topCookie] @ r9<- thread->localRef->...
    str     r1, [r3, #offThread_curFrame]   @ self->curFrame = newFp
    str     r9, [r1, #(offStackSaveArea_localRefCookie - sizeofStackSaveArea)]
                                        @ newFp->localRefCookie=top
    mov     r9, r3                      @ r9<- glue->self (preserve)
    SAVEAREA_FROM_FP(r10, r1)           @ r10<- new stack save area

    mov     r2, r0                      @ r2<- methodToCall
    mov     r0, r1                      @ r0<- newFP
    add     r1, rGLUE, #offGlue_retval  @ r1<- &retval

    blx     r8                          @ off to the native code

    @ native return; r9=self, r10=newSaveArea
    @ equivalent to dvmPopJniLocals
    ldr     r2, [r10, #offStackSaveArea_returnAddr] @ r2 = chaining cell ret
    ldr     r0, [r10, #offStackSaveArea_localRefCookie] @ r0<- saved->top
    ldr     r1, [r9, #offThread_exception] @ check for exception
    str     rFP, [r9, #offThread_curFrame]  @ self->curFrame = fp
    cmp     r1, #0                      @ null?
    str     r0, [r9, #offThread_jniLocal_topCookie] @ new top <- old top
    bne     .LhandleException             @ no, handle exception
    bx      r2


/* ------------------------------ */
    .balign 4
    .global dvmCompiler_TEMPLATE_CMPG_DOUBLE
dvmCompiler_TEMPLATE_CMPG_DOUBLE:
/* File: armv5te/TEMPLATE_CMPG_DOUBLE.S */
/* File: armv5te/TEMPLATE_CMPL_DOUBLE.S */
    /*
     * For the JIT: incoming arguments are pointers to the arguments in r0/r1
     *              result in r0
     *
     * Compare two floating-point values.  Puts 0, 1, or -1 into the
     * destination register based on the results of the comparison.
     *
     * Provide a "naninst" instruction that puts 1 or -1 into r1 depending
     * on what value we'd like to return when one of the operands is NaN.
     *
     * See OP_CMPL_FLOAT for an explanation.
     *
     * For: cmpl-double, cmpg-double
     */
    /* op vAA, vBB, vCC */
    mov     r4, lr                      @ save return address
    mov     r9, r0                      @ save copy of &arg1
    mov     r10, r1                     @ save copy of &arg2
    ldmia   r9, {r0-r1}                 @ r0/r1<- vBB/vBB+1
    ldmia   r10, {r2-r3}                @ r2/r3<- vCC/vCC+1
    LDR_PC_LR ".L__aeabi_cdcmple"       @ PIC way of "bl __aeabi_cdcmple"
    bhi     .LTEMPLATE_CMPG_DOUBLE_gt_or_nan       @ C set and Z clear, disambiguate
    mvncc   r0, #0                      @ (less than) r1<- -1
    moveq   r0, #0                      @ (equal) r1<- 0, trumps less than
    bx      r4

    @ Test for NaN with a second comparison.  EABI forbids testing bit
    @ patterns, and we can't represent 0x7fc00000 in immediate form, so
    @ make the library call.
.LTEMPLATE_CMPG_DOUBLE_gt_or_nan:
    ldmia   r10, {r0-r1}                @ reverse order
    ldmia   r9, {r2-r3}
    LDR_PC_LR ".L__aeabi_cdcmple"       @ r0<- Z set if eq, C clear if <
    movcc   r0, #1                      @ (greater than) r1<- 1
    bxcc    r4
    mov     r0, #1                            @ r1<- 1 or -1 for NaN
    bx      r4



/* ------------------------------ */
    .balign 4
    .global dvmCompiler_TEMPLATE_CMPL_DOUBLE
dvmCompiler_TEMPLATE_CMPL_DOUBLE:
/* File: armv5te/TEMPLATE_CMPL_DOUBLE.S */
    /*
     * For the JIT: incoming arguments are pointers to the arguments in r0/r1
     *              result in r0
     *
     * Compare two floating-point values.  Puts 0, 1, or -1 into the
     * destination register based on the results of the comparison.
     *
     * Provide a "naninst" instruction that puts 1 or -1 into r1 depending
     * on what value we'd like to return when one of the operands is NaN.
     *
     * See OP_CMPL_FLOAT for an explanation.
     *
     * For: cmpl-double, cmpg-double
     */
    /* op vAA, vBB, vCC */
    mov     r4, lr                      @ save return address
    mov     r9, r0                      @ save copy of &arg1
    mov     r10, r1                     @ save copy of &arg2
    ldmia   r9, {r0-r1}                 @ r0/r1<- vBB/vBB+1
    ldmia   r10, {r2-r3}                @ r2/r3<- vCC/vCC+1
    LDR_PC_LR ".L__aeabi_cdcmple"       @ PIC way of "bl __aeabi_cdcmple"
    bhi     .LTEMPLATE_CMPL_DOUBLE_gt_or_nan       @ C set and Z clear, disambiguate
    mvncc   r0, #0                      @ (less than) r1<- -1
    moveq   r0, #0                      @ (equal) r1<- 0, trumps less than
    bx      r4

    @ Test for NaN with a second comparison.  EABI forbids testing bit
    @ patterns, and we can't represent 0x7fc00000 in immediate form, so
    @ make the library call.
.LTEMPLATE_CMPL_DOUBLE_gt_or_nan:
    ldmia   r10, {r0-r1}                @ reverse order
    ldmia   r9, {r2-r3}
    LDR_PC_LR ".L__aeabi_cdcmple"       @ r0<- Z set if eq, C clear if <
    movcc   r0, #1                      @ (greater than) r1<- 1
    bxcc    r4
    mvn     r0, #0                            @ r1<- 1 or -1 for NaN
    bx      r4


/* ------------------------------ */
    .balign 4
    .global dvmCompiler_TEMPLATE_CMPG_FLOAT
dvmCompiler_TEMPLATE_CMPG_FLOAT:
/* File: armv5te/TEMPLATE_CMPG_FLOAT.S */
/* File: armv5te/TEMPLATE_CMPL_FLOAT.S */
    /*
     * For the JIT: incoming arguments in r0, r1
     *              result in r0
     *
     * Compare two floating-point values.  Puts 0, 1, or -1 into the
     * destination register based on the results of the comparison.
     *
     * Provide a "naninst" instruction that puts 1 or -1 into r1 depending
     * on what value we'd like to return when one of the operands is NaN.
     *
     * The operation we're implementing is:
     *   if (x == y)
     *     return 0;
     *   else if (x < y)
     *     return -1;
     *   else if (x > y)
     *     return 1;
     *   else
     *     return {-1,1};  // one or both operands was NaN
     *
     * The straightforward implementation requires 3 calls to functions
     * that return a result in r0.  We can do it with two calls if our
     * EABI library supports __aeabi_cfcmple (only one if we want to check
     * for NaN directly):
     *   check x <= y
     *     if <, return -1
     *     if ==, return 0
     *   check y <= x
     *     if <, return 1
     *   return {-1,1}
     *
     * for: cmpl-float, cmpg-float
     */
    /* op vAA, vBB, vCC */
    mov     r4, lr                      @ save return address
    mov     r9, r0                      @ Save copies - we may need to redo
    mov     r10, r1
    LDR_PC_LR ".L__aeabi_cfcmple"       @ cmp <=: C clear if <, Z set if eq
    bhi     .LTEMPLATE_CMPG_FLOAT_gt_or_nan       @ C set and Z clear, disambiguate
    mvncc   r0, #0                      @ (less than) r0<- -1
    moveq   r0, #0                      @ (equal) r0<- 0, trumps less than
    bx      r4
    @ Test for NaN with a second comparison.  EABI forbids testing bit
    @ patterns, and we can't represent 0x7fc00000 in immediate form, so
    @ make the library call.
.LTEMPLATE_CMPG_FLOAT_gt_or_nan:
    mov     r1, r9                      @ reverse order
    mov     r0, r10
    LDR_PC_LR ".L__aeabi_cfcmple"       @ r0<- Z set if eq, C clear if <
    movcc   r0, #1                      @ (greater than) r1<- 1
    bxcc    r4
    mov     r0, #1                            @ r1<- 1 or -1 for NaN
    bx      r4




/* ------------------------------ */
    .balign 4
    .global dvmCompiler_TEMPLATE_CMPL_FLOAT
dvmCompiler_TEMPLATE_CMPL_FLOAT:
/* File: armv5te/TEMPLATE_CMPL_FLOAT.S */
    /*
     * For the JIT: incoming arguments in r0, r1
     *              result in r0
     *
     * Compare two floating-point values.  Puts 0, 1, or -1 into the
     * destination register based on the results of the comparison.
     *
     * Provide a "naninst" instruction that puts 1 or -1 into r1 depending
     * on what value we'd like to return when one of the operands is NaN.
     *
     * The operation we're implementing is:
     *   if (x == y)
     *     return 0;
     *   else if (x < y)
     *     return -1;
     *   else if (x > y)
     *     return 1;
     *   else
     *     return {-1,1};  // one or both operands was NaN
     *
     * The straightforward implementation requires 3 calls to functions
     * that return a result in r0.  We can do it with two calls if our
     * EABI library supports __aeabi_cfcmple (only one if we want to check
     * for NaN directly):
     *   check x <= y
     *     if <, return -1
     *     if ==, return 0
     *   check y <= x
     *     if <, return 1
     *   return {-1,1}
     *
     * for: cmpl-float, cmpg-float
     */
    /* op vAA, vBB, vCC */
    mov     r4, lr                      @ save return address
    mov     r9, r0                      @ Save copies - we may need to redo
    mov     r10, r1
    LDR_PC_LR ".L__aeabi_cfcmple"       @ cmp <=: C clear if <, Z set if eq
    bhi     .LTEMPLATE_CMPL_FLOAT_gt_or_nan       @ C set and Z clear, disambiguate
    mvncc   r0, #0                      @ (less than) r0<- -1
    moveq   r0, #0                      @ (equal) r0<- 0, trumps less than
    bx      r4
    @ Test for NaN with a second comparison.  EABI forbids testing bit
    @ patterns, and we can't represent 0x7fc00000 in immediate form, so
    @ make the library call.
.LTEMPLATE_CMPL_FLOAT_gt_or_nan:
    mov     r1, r9                      @ reverse order
    mov     r0, r10
    LDR_PC_LR ".L__aeabi_cfcmple"       @ r0<- Z set if eq, C clear if <
    movcc   r0, #1                      @ (greater than) r1<- 1
    bxcc    r4
    mvn     r0, #0                            @ r1<- 1 or -1 for NaN
    bx      r4



/* ------------------------------ */
    .balign 4
    .global dvmCompiler_TEMPLATE_MUL_LONG
dvmCompiler_TEMPLATE_MUL_LONG:
/* File: armv5te/TEMPLATE_MUL_LONG.S */
    /*
     * Signed 64-bit integer multiply.
     *
     * For JIT: op1 in r0/r1, op2 in r2/r3, return in r0/r1
     *
     * Consider WXxYZ (r1r0 x r3r2) with a long multiply:
     *        WX
     *      x YZ
     *  --------
     *     ZW ZX
     *  YW YX
     *
     * The low word of the result holds ZX, the high word holds
     * (ZW+YX) + (the high overflow from ZX).  YW doesn't matter because
     * it doesn't fit in the low 64 bits.
     *
     * Unlike most ARM math operations, multiply instructions have
     * restrictions on using the same register more than once (Rd and Rm
     * cannot be the same).
     */
    /* mul-long vAA, vBB, vCC */
    mul     ip, r2, r1                  @  ip<- ZxW
    umull   r9, r10, r2, r0             @  r9/r10 <- ZxX
    mla     r2, r0, r3, ip              @  r2<- YxX + (ZxW)
    add     r10, r2, r10                @  r10<- r10 + low(ZxW + (YxX))
    mov     r0,r9
    mov     r1,r10
    bx      lr

/* ------------------------------ */
    .balign 4
    .global dvmCompiler_TEMPLATE_SHL_LONG
dvmCompiler_TEMPLATE_SHL_LONG:
/* File: armv5te/TEMPLATE_SHL_LONG.S */
    /*
     * Long integer shift.  This is different from the generic 32/64-bit
     * binary operations because vAA/vBB are 64-bit but vCC (the shift
     * distance) is 32-bit.  Also, Dalvik requires us to ignore all but the low
     * 6 bits.
     */
    /* shl-long vAA, vBB, vCC */
    and     r2, r2, #63                 @ r2<- r2 & 0x3f
    mov     r1, r1, asl r2              @  r1<- r1 << r2
    rsb     r3, r2, #32                 @  r3<- 32 - r2
    orr     r1, r1, r0, lsr r3          @  r1<- r1 | (r0 << (32-r2))
    subs    ip, r2, #32                 @  ip<- r2 - 32
    movpl   r1, r0, asl ip              @  if r2 >= 32, r1<- r0 << (r2-32)
    mov     r0, r0, asl r2              @  r0<- r0 << r2
    bx      lr

/* ------------------------------ */
    .balign 4
    .global dvmCompiler_TEMPLATE_SHR_LONG
dvmCompiler_TEMPLATE_SHR_LONG:
/* File: armv5te/TEMPLATE_SHR_LONG.S */
    /*
     * Long integer shift.  This is different from the generic 32/64-bit
     * binary operations because vAA/vBB are 64-bit but vCC (the shift
     * distance) is 32-bit.  Also, Dalvik requires us to ignore all but the low
     * 6 bits.
     */
    /* shr-long vAA, vBB, vCC */
    and     r2, r2, #63                 @ r0<- r0 & 0x3f
    mov     r0, r0, lsr r2              @  r0<- r2 >> r2
    rsb     r3, r2, #32                 @  r3<- 32 - r2
    orr     r0, r0, r1, asl r3          @  r0<- r0 | (r1 << (32-r2))
    subs    ip, r2, #32                 @  ip<- r2 - 32
    movpl   r0, r1, asr ip              @  if r2 >= 32, r0<-r1 >> (r2-32)
    mov     r1, r1, asr r2              @  r1<- r1 >> r2
    bx      lr


/* ------------------------------ */
    .balign 4
    .global dvmCompiler_TEMPLATE_USHR_LONG
dvmCompiler_TEMPLATE_USHR_LONG:
/* File: armv5te/TEMPLATE_USHR_LONG.S */
    /*
     * Long integer shift.  This is different from the generic 32/64-bit
     * binary operations because vAA/vBB are 64-bit but vCC (the shift
     * distance) is 32-bit.  Also, Dalvik requires us to ignore all but the low
     * 6 bits.
     */
    /* ushr-long vAA, vBB, vCC */
    and     r2, r2, #63                 @ r0<- r0 & 0x3f
    mov     r0, r0, lsr r2              @  r0<- r2 >> r2
    rsb     r3, r2, #32                 @  r3<- 32 - r2
    orr     r0, r0, r1, asl r3          @  r0<- r0 | (r1 << (32-r2))
    subs    ip, r2, #32                 @  ip<- r2 - 32
    movpl   r0, r1, lsr ip              @  if r2 >= 32, r0<-r1 >>> (r2-32)
    mov     r1, r1, lsr r2              @  r1<- r1 >>> r2
    bx      lr


/* ------------------------------ */
    .balign 4
    .global dvmCompiler_TEMPLATE_THROW_EXCEPTION_COMMON
dvmCompiler_TEMPLATE_THROW_EXCEPTION_COMMON:
/* File: armv5te/TEMPLATE_THROW_EXCEPTION_COMMON.S */
    /*
     * Throw an exception from JIT'ed code.
     * On entry:
     *    r0    Dalvik PC that raises the exception
     */
    b       .LhandleException

    .size   dvmCompilerTemplateStart, .-dvmCompilerTemplateStart
/* File: armv5te/footer.S */
/*
 * ===========================================================================
 *  Common subroutines and data
 * ===========================================================================
 */

    .text
    .align  2
.LinvokeNative:
    @ Prep for the native call
    @ r1 = newFP, r0 = methodToCall
    ldr     r3, [rGLUE, #offGlue_self]      @ r3<- glue->self
    ldr     r9, [r3, #offThread_jniLocal_topCookie] @ r9<- thread->localRef->...
    str     r1, [r3, #offThread_curFrame]   @ self->curFrame = newFp
    str     r9, [r1, #(offStackSaveArea_localRefCookie - sizeofStackSaveArea)]
                                        @ newFp->localRefCookie=top
    mov     r9, r3                      @ r9<- glue->self (preserve)
    SAVEAREA_FROM_FP(r10, r1)           @ r10<- new stack save area

    mov     r2, r0                      @ r2<- methodToCall
    mov     r0, r1                      @ r0<- newFP
    add     r1, rGLUE, #offGlue_retval  @ r1<- &retval

    LDR_PC_LR "[r2, #offMethod_nativeFunc]"

    @ native return; r9=self, r10=newSaveArea
    @ equivalent to dvmPopJniLocals
    ldr     r2, [r10, #offStackSaveArea_returnAddr] @ r2 = chaining cell ret
    ldr     r0, [r10, #offStackSaveArea_localRefCookie] @ r0<- saved->top
    ldr     r1, [r9, #offThread_exception] @ check for exception
    str     rFP, [r9, #offThread_curFrame]  @ self->curFrame = fp
    cmp     r1, #0                      @ null?
    str     r0, [r9, #offThread_jniLocal_topCookie] @ new top <- old top
    ldr     r0, [r10, #offStackSaveArea_savedPc] @ reload rPC
    bne     .LhandleException             @ no, handle exception
    bx      r2

/*
 * On entry:
 * r0  Faulting Dalvik PC
 */
.LhandleException:
    ldr     r1, .LdvmMterpCommonExceptionThrown @ PIC way of getting &func
    ldr     rIBASE, .LdvmAsmInstructionStart    @ same as above
    mov     rPC, r0                 @ reload the faulting Dalvik address
    mov     pc, r1                  @ branch to dvmMterpCommonExceptionThrown

    .align  2
.LdvmAsmInstructionStart:
    .word   dvmAsmInstructionStart
.LdvmJitToInterpNoChain:
    .word   dvmJitToInterpNoChain
.LdvmMterpStdBail:
    .word   dvmMterpStdBail
.LdvmMterpCommonExceptionThrown:
    .word   dvmMterpCommonExceptionThrown
.L__aeabi_cdcmple:
    .word   __aeabi_cdcmple
.L__aeabi_cfcmple:
    .word   __aeabi_cfcmple

    .global dmvCompilerTemplateEnd
dmvCompilerTemplateEnd:

#endif /* WITH_JIT */

