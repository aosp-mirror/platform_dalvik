%default { "routine":"NoRange","isrange":"0" }
%verify "executed"
%verify "unknown method"
    /*
     * Handle a static method call.
     *
     * for: invoke-static, invoke-static/range
     */
    /* op vB, {vD, vE, vF, vG, vA}, class@CCCC */
    /* op {vCCCC..v(CCCC+AA-1)}, meth@BBBB */
    movl      rSELF,%ecx
    movzwl    2(rPC),%eax               # eax<- BBBB
    movl      offThread_methodClassDex(%ecx),%ecx # ecx<- pDvmDex
    EXPORT_PC
    movl      offDvmDex_pResMethods(%ecx),%ecx  # ecx<- pDvmDex->pResMethods
    movl      (%ecx,%eax,4),%eax        # eax<- resolved methodToCall
    testl     %eax,%eax
    jne       common_invokeMethod${routine}
    movl      rSELF,%ecx
    movl      offThread_method(%ecx),%ecx # ecx<- self->method
    movzwl    2(rPC),%eax
    movl      offMethod_clazz(%ecx),%ecx# ecx<- method->clazz
    movl      %eax,OUT_ARG1(%esp)       # arg1<- BBBB
    movl      %ecx,OUT_ARG0(%esp)       # arg0<- clazz
    movl      $$METHOD_STATIC,%eax
    movl      %eax,OUT_ARG2(%esp)       # arg2<- flags
    call      dvmResolveMethod          # call(clazz,ref,flags)
    testl     %eax,%eax                 # got null?
    jne       common_invokeMethod${routine}
    jmp       common_exceptionThrown
