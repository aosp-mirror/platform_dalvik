%verify executed
    /*
     * Handle a throw-verification-error instruction.  This throws an
     * exception for an error discovered during verification.  The
     * exception is indicated by AA, with some detail provided by BBBB.
     */
    /* op AA, ref@BBBB */
    ldr     r0, [rGLUE, #offGlue_methodClassDex]    @ r0<- glue->methodClassDex
    FETCH(r2, 1)                        @ r2<- BBBB
    mov     r1, rINST, lsr #8           @ r1<- AA
    bl      dvmThrowVerificationError   @ always throws
    b       common_exceptionThrown      @ handle exception

