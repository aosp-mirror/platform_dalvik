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

