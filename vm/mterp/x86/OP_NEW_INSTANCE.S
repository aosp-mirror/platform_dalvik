%verify "executed"
%verify "class not resolved"
%verify "class cannot be resolved"
%verify "class not initialized"
%verify "class fails to initialize"
%verify "class already resolved/initialized"
%verify "class is abstract or interface"
%verify "allocation fails"
    /*
     * Create a new instance of a class.
     */
    /* new-instance vAA, class@BBBB */
    movl      rSELF,%ecx
    movzwl    2(rPC),%eax               # eax<- BBBB
    movl      offThread_methodClassDex(%ecx),%ecx  # ecx<- pDvmDex
    movl      offDvmDex_pResClasses(%ecx),%ecx # ecx<- pDvmDex->pResClasses
    EXPORT_PC
    movl      (%ecx,%eax,4),%ecx        # ecx<- resolved class
    testl     %ecx,%ecx                 # resolved?
    je        .L${opcode}_resolve       # no, go do it
.L${opcode}_resolved:  # on entry, ecx<- class
    cmpb      $$CLASS_INITIALIZED,offClassObject_status(%ecx)
    je        .L${opcode}_initialized
    jmp       .L${opcode}_needinit
%break

.L${opcode}_initialized:  # on entry, ecx<- class
    /* TODO: remove test for interface/abstract, now done in verifier */
    testl     $$(ACC_INTERFACE|ACC_ABSTRACT),offClassObject_accessFlags(%ecx)
    movl      $$ALLOC_DONT_TRACK,OUT_ARG1(%esp)
    jne       .L${opcode}_abstract
.L${opcode}_finish: # ecx=class
    movl     %ecx,OUT_ARG0(%esp)
    call     dvmAllocObject             # eax<- new object
    FETCH_INST_OPCODE 2 %edx
    testl    %eax,%eax                  # success?
    je       common_exceptionThrown     # no, bail out
    SET_VREG %eax rINST
    ADVANCE_PC 2
    GOTO_NEXT_R %edx

    /*
     * Class initialization required.
     *
     *  ecx holds class object
     */
.L${opcode}_needinit:
    SPILL_TMP1(%ecx)                    # save object
    movl    %ecx,OUT_ARG0(%esp)
    call    dvmInitClass                # initialize class
    UNSPILL_TMP1(%ecx)                  # restore object
    testl   %eax,%eax                   # success?
    jne     .L${opcode}_initialized     # success, continue
    jmp     common_exceptionThrown      # go deal with init exception

    /*
     * Resolution required.  This is the least-likely path.
     *
     */
.L${opcode}_resolve:
    movl    rSELF,%ecx
    movzwl  2(rPC),%eax
    movl    offThread_method(%ecx),%ecx   # ecx<- self->method
    movl    %eax,OUT_ARG1(%esp)
    movl    offMethod_clazz(%ecx),%ecx  # ecx<- method->clazz
    movl    $$0,OUT_ARG2(%esp)
    movl    %ecx,OUT_ARG0(%esp)
    call    dvmResolveClass             # call(clazz,off,flags)
    movl    %eax,%ecx                   # ecx<- resolved ClassObject ptr
    testl   %ecx,%ecx                   # success?
    jne     .L${opcode}_resolved        # good to go
    jmp     common_exceptionThrown      # no, handle exception

    /*
     * TODO: remove this
     * We can't instantiate an abstract class or interface, so throw an
     * InstantiationError with the class descriptor as the message.
     *
     *  ecx holds class object
     */
.L${opcode}_abstract:
    movl    offClassObject_descriptor(%ecx),%eax
    movl    $$.LstrInstantiationError,OUT_ARG0(%esp)
    movl    %eax,OUT_ARG1(%esp)
    call    dvmThrowExceptionWithClassMessage
    jmp     common_exceptionThrown
