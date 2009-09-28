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
