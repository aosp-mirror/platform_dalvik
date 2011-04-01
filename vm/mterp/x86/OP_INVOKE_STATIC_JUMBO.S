%verify "executed"
%verify "unknown method"
    /*
     * Handle a jumbo static method call.
     */
    /* invoke-static/jumbo {vCCCC..v(CCCC+BBBB-1)}, meth@AAAAAAAA */
    movl      rSELF,%ecx
    movl      2(rPC),%eax               # eax<- AAAAAAAA
    movl      offThread_methodClassDex(%ecx),%ecx # ecx<- pDvmDex
    EXPORT_PC
    movl      offDvmDex_pResMethods(%ecx),%ecx  # ecx<- pDvmDex->pResMethods
    movl      (%ecx,%eax,4),%eax        # eax<- resolved methodToCall
    testl     %eax,%eax
    jne       common_invokeMethodJumbo
    movl      rSELF,%ecx
    movl      offThread_method(%ecx),%ecx # ecx<- self->method
    movl      2(rPC),%eax               # eax<- AAAAAAAA
    movl      offMethod_clazz(%ecx),%ecx# ecx<- method->clazz
    movl      %eax,OUT_ARG1(%esp)       # arg1<- AAAAAAAA
    movl      %ecx,OUT_ARG0(%esp)       # arg0<- clazz
    movl      $$METHOD_STATIC,%eax
    movl      %eax,OUT_ARG2(%esp)       # arg2<- flags
    call      dvmResolveMethod          # call(clazz,ref,flags)
    testl     %eax,%eax                 # got null?
    jne       common_invokeMethodJumbo
    jmp       common_exceptionThrown
