%verify executed
    /*
     * Handle a jumbo throw-verification-error instruction.  This throws an
     * exception for an error discovered during verification.  The
     * exception is indicated by BBBB, with some detail provided by AAAAAAAA.
     */
    /* exop BBBB, Class@AAAAAAAA */
    FETCH(r1, 1)                        @ r1<- aaaa (lo)
    FETCH(r2, 2)                        @ r2<- AAAA (hi)
    ldr     r0, [rSELF, #offThread_method]    @ r0<- self->method
    orr     r2, r1, r2, lsl #16         @ r2<- AAAAaaaa
    EXPORT_PC()                         @ export the PC
    FETCH(r1, 3)                        @ r1<- BBBB
    bl      dvmThrowVerificationError   @ always throws
    b       common_exceptionThrown      @ handle exception
