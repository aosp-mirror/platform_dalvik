%verify executed
    /*
     * Handle a throw-verification-error instruction.  This throws an
     * exception for an error discovered during verification.  The
     * exception is indicated by AA, with some detail provided by BBBB.
     */
    /* op AA, ref@BBBB */
    GET_GLUE(%ecx)
    movzwl   2(rPC),%eax                     # eax<- BBBB
    movl     offGlue_method(%ecx),%ecx       # ecx<- glue->method
    EXPORT_PC()
    movzbl   rINST_HI,rINST_FULL             # rINST_FULL<- AA
    movl     %eax,OUT_ARG2(%esp)             # arg2<- BBBB
    movl     rINST_FULL,OUT_ARG1(%esp)       # arg1<- AA
    movl     %ecx,OUT_ARG0(%esp)             # arg0<- method
    SPILL(rPC)
    call     dvmThrowVerificationError       # call(method, kind, ref)
    UNSPILL(rPC)
    jmp      common_exceptionThrown          # handle exception
