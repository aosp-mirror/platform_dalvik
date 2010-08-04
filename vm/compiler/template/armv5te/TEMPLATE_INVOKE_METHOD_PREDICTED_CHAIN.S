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
    ldr     r9, [rGLUE, #offGlue_icRechainCount]   @ r1 <- shared rechainCount
    cmp     r3, r8          @ predicted class == actual class?
#if defined(WITH_JIT_TUNING)
    ldr     r7, .LdvmICHitCount
    ldreq   r10, [r7, #0]
    add     r10, r10, #1
    streq   r10, [r7, #0]
#endif
    beq     .LinvokeChain   @ predicted chain is valid
    ldr     r7, [r3, #offClassObject_vtable] @ r7 <- this->class->vtable
    cmp     r8, #0          @ initialized class or not
    moveq   r1, #0
    subne   r1, r9, #1      @ count--
    strne   r1, [rGLUE, #offGlue_icRechainCount]   @ write back to InterpState
    add     lr, lr, #4      @ return to fully-resolve landing pad
    /*
     * r1 <- count
     * r2 <- &predictedChainCell
     * r3 <- this->class
     * r4 <- dPC
     * r7 <- this->class->vtable
     */
    bx      lr
