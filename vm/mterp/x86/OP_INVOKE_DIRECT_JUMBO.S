%verify "executed"
%verify "unknown method"
    /*
     * Handle a jumbo direct method call.
     *
     * (We could defer the "is 'this' pointer null" test to the common
     * method invocation code, and use a flag to indicate that static
     * calls don't count.  If we do this as part of copying the arguments
     * out we could avoiding loading the first arg twice.)
     */
    /* invoke-direct/jumbo {vCCCC..v(CCCC+BBBB-1)}, meth@AAAAAAAA */
    movl      rSELF,%ecx
    movl      2(rPC),%eax              # eax<- AAAAAAAA
    movl      offThread_methodClassDex(%ecx),%ecx # ecx<- pDvmDex
    EXPORT_PC
    movl      offDvmDex_pResMethods(%ecx),%ecx  # ecx<- pDvmDex->pResMethods
    movzwl    8(rPC),rIBASE            # rIBASE<- CCCC
    movl      (%ecx,%eax,4),%eax       # eax<- resolved methodToCall
    testl     %eax,%eax                # already resolved?
    GET_VREG_R  %ecx rIBASE            # ecx<- "this" ptr
    je        .L${opcode}_resolve      # not resolved, do it now
.L${opcode}_finish:
    testl     %ecx,%ecx                # null "this"?
    jne       common_invokeMethodJumbo # no, continue on
    jmp       common_errNullObject

    /*
     * On entry:
     *   TMP_SPILL  <- "this" register
     * Things a bit ugly on this path, but it's the less
     * frequent one.  We'll have to do some reloading.
     */
.L${opcode}_resolve:
     SPILL_TMP1(%ecx)
     movl     rSELF,%ecx
     movl     offThread_method(%ecx),%ecx  # ecx<- self->method
     movl     2(rPC),%eax      # reference AAAAAAAA
     movl     offMethod_clazz(%ecx),%ecx # ecx<- method->clazz
     movl     $$METHOD_DIRECT,OUT_ARG2(%esp)
     movl     %eax,OUT_ARG1(%esp)
     movl     %ecx,OUT_ARG0(%esp)
     call     dvmResolveMethod # eax<- call(clazz, ref, flags)
     UNSPILL_TMP1(%ecx)
     testl    %eax,%eax
     jne      .L${opcode}_finish
     jmp      common_exceptionThrown
