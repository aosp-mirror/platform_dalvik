%verify "executed"
%verify "unknown method"
%verify "null object"
    /*
     * Handle a jumbo interface method call.
     */
    /* invoke-interface/jumbo {vCCCC..v(CCCC+BBBB-1)}, meth@AAAAAAAA */
    movzwl     8(rPC),%eax              # eax<- CCCC
    movl       rSELF,%ecx
    GET_VREG_R   %eax %eax              # eax<- "this"
    EXPORT_PC
    testl      %eax,%eax                # null this?
    je         common_errNullObject     # yes, fail
    movl       offObject_clazz(%eax),%eax# eax<- thisPtr->clazz
    movl       %eax,OUT_ARG0(%esp)                 # arg0<- class
    movl       offThread_methodClassDex(%ecx),%eax   # eax<- methodClassDex
    movl       offThread_method(%ecx),%ecx           # ecx<- method
    movl       %eax,OUT_ARG3(%esp)                 # arg3<- dex
    movl       2(rPC),%eax                         # eax<- AAAAAAAA
    movl       %ecx,OUT_ARG2(%esp)                 # arg2<- method
    movl       %eax,OUT_ARG1(%esp)                 # arg1<- AAAAAAAA
    call       dvmFindInterfaceMethodInCache # eax<- call(class, ref, method, dex)
    testl      %eax,%eax
    je         common_exceptionThrown
    jmp        common_invokeMethodJumbo
