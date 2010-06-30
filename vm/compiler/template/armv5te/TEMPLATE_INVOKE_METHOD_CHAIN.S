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
    ldr     r8, [r8]                    @ r8<- suspendCount (int)
    cmp     r10, r9                     @ bottom < interpStackEnd?
    bxlo    r12                         @ return to raise stack overflow excep.
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
