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
    ldr     r9,  [r0, #offStackSaveArea_returnAddr] @ r9<- chaining cell ret
    ldr     r2, [r10, #(offStackSaveArea_method - sizeofStackSaveArea)]
                                        @ r2<- method we're returning to
    ldr     r3, [rGLUE, #offGlue_self]  @ r3<- glue->self
    cmp     r2, #0                      @ break frame?
    beq     1f                          @ bail to interpreter
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
