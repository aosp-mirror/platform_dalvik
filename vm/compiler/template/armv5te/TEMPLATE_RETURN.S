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
    ldr     r1, .LdvmJitToInterpNoChainNoProfile @ defined in footer.S
    mov     rFP, r10                    @ publish new FP
    ldrne   r10, [r2, #offMethod_clazz] @ r10<- method->clazz
    ldr     r8, [r8]                    @ r8<- suspendCount

    str     r2, [rGLUE, #offGlue_method]@ glue->method = newSave->method
    ldr     r0, [r10, #offClassObject_pDvmDex] @ r0<- method->clazz->pDvmDex
    str     rFP, [r3, #offThread_curFrame] @ self->curFrame = fp
    add     rPC, rPC, #6                @ publish new rPC (advance 6 bytes)
    str     r0, [rGLUE, #offGlue_methodClassDex]
    cmp     r8, #0                      @ check the suspendCount
    movne   r9, #0                      @ clear the chaining cell address
    str     r9, [r3, #offThread_inJitCodeCache] @ in code cache or not
    cmp     r9, #0                      @ chaining cell exists?
    blxne   r9                          @ jump to the chaining cell
#if defined(WITH_JIT_TUNING)
    mov     r0, #kCallsiteInterpreted
#endif
    mov     pc, r1                      @ callsite is interpreted
1:
    stmia   rGLUE, {rPC, rFP}           @ SAVE_PC_FP_TO_GLUE()
    ldr     r2, .LdvmMterpStdBail       @ defined in footer.S
    mov     r1, #0                      @ changeInterp = false
    mov     r0, rGLUE                   @ Expecting rGLUE in r0
    blx     r2                          @ exit the interpreter
