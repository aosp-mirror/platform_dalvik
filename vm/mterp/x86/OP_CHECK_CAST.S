%verify "executed"
%verify "null object"
%verify "class cast exception thrown, with correct class name"
%verify "class cast exception not thrown on same class"
%verify "class cast exception not thrown on subclass"
%verify "class not resolved"
%verify "class already resolved"
    /*
     * Check to see if a cast from one class to another is allowed.
     */
    /* check-cast vAA, class@BBBB */
    GET_GLUE(%ecx)
    movzbl    rINST_HI,rINST_FULL       # rINST_FULL<- AA
    GET_VREG(rINST_FULL,rINST_FULL)     # rINST_FULL<- vAA (object)
    movzwl    2(rPC),%eax               # eax<- BBBB
    movl      offGlue_methodClassDex(%ecx),%ecx # ecx<- pDvmDex
    testl     rINST_FULL,rINST_FULL     # is oject null?
    movl      offDvmDex_pResClasses(%ecx),%ecx # ecx<- pDvmDex->pResClasses
    je        .L${opcode}_okay          # null obj, cast always succeeds
    movl      (%ecx,%eax,4),%eax        # eax<- resolved class
    movl      offObject_clazz(rINST_FULL),%ecx # ecx<- obj->clazz
    testl     %eax,%eax                 # have we resolved this before?
    je        .L${opcode}_resolve       # no, go do it now
.L${opcode}_resolved:
    cmpl      %eax,%ecx                 # same class (trivial success)?
    jne       .L${opcode}_fullcheck     # no, do full check
.L${opcode}_okay:
    FETCH_INST_WORD(2)
    ADVANCE_PC(2)
    GOTO_NEXT
%break

    /*
     * Trivial test failed, need to perform full check.  This is common.
     *  ecx holds obj->clazz
     *  eax holds class resolved from BBBB
     *  rINST_FULL holds object
     */
.L${opcode}_fullcheck:
    movl    %eax,OUT_ARG1(%esp)
    movl    %ecx,OUT_ARG0(%esp)
    SPILL(rPC)
    call    dvmInstanceofNonTrivial     # eax<- boolean result
    UNSPILL(rPC)
    testl   %eax,%eax                   # failed?
    jne     .L${opcode}_okay            # no, success

    # A cast has failed.  We need to throw a ClassCastException with the
    # class of the object that failed to be cast.
    EXPORT_PC()
    movl    offObject_clazz(rINST_FULL),%ecx  # ecx<- obj->clazz
    movl    $$.LstrClassCastException,%eax
    movl    offClassObject_descriptor(%ecx),%ecx
    movl    %eax,OUT_ARG0(%esp)     # arg0<- message
    movl    %ecx,OUT_ARG1(%esp)     # arg1<- obj->clazz->descriptor
    SPILL(rPC)
    call    dvmThrowExceptionWithClassMessage
    UNSPILL(rPC)
    jmp     common_exceptionThrown

    /*
     * Resolution required.  This is the least-likely path, and we're
     * going to have to recreate some data.
     *
     *  rINST_FULL holds object
     */
.L${opcode}_resolve:
    GET_GLUE(%ecx)
    EXPORT_PC()
    movzwl  2(rPC),%eax                # eax<- BBBB
    movl    offGlue_method(%ecx),%ecx  # ecx<- glue->method
    movl    %eax,OUT_ARG1(%esp)        # arg1<- BBBB
    movl    offMethod_clazz(%ecx),%ecx # ecx<- metho->clazz
    movl    $$0,OUT_ARG2(%esp)         # arg2<- false
    movl    %ecx,OUT_ARG0(%esp)        # arg0<- method->clazz
    SPILL(rPC)
    call    dvmResolveClass            # eax<- resolved ClassObject ptr
    UNSPILL(rPC)
    testl   %eax,%eax                  # got null?
    je      common_exceptionThrown     # yes, handle exception
    movl    offObject_clazz(rINST_FULL),%ecx  # ecx<- obj->clazz
    jmp     .L${opcode}_resolved       # pick up where we left off
