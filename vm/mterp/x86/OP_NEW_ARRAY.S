%verify "executed"
%verify "negative array length"
%verify "allocation fails"
    /*
     * Allocate an array of objects, specified with the array class
     * and a count.
     *
     * The verifier guarantees that this is an array class, so we don't
     * check for it here.
     */
    /* new-array vA, vB, class@CCCC */
    GET_GLUE(%ecx)
    EXPORT_PC()
    movl    offGlue_methodClassDex(%ecx),%ecx # ecx<- pDvmDex
    movzwl  2(rPC),%eax                       # eax<- CCCC
    movl    offDvmDex_pResClasses(%ecx),%ecx  # ecx<- pDvmDex->pResClasses
    movl    (%ecx,%eax,4),%ecx                # ecx<- resolved class
    movzbl  rINST_HI,%eax
    sarl    $$4,%eax                          # eax<- B
    GET_VREG(%eax,%eax)                       # eax<- vB (array length)
    movzbl  rINST_HI,rINST_FULL
    andb    $$0xf,rINST_LO                    # rINST_FULL<- A
    testl   %eax,%eax
    js      common_errNegativeArraySize       # bail
    testl   %ecx,%ecx                         # already resolved?
    jne     .L${opcode}_finish                # yes, fast path
    jmp     .L${opcode}_resolve               # resolve now
%break

    /*
     * Resolve class.  (This is an uncommon case.)
     *  ecx holds class (null here)
     *  eax holds array length (vB)
     */
.L${opcode}_resolve:
    GET_GLUE(%ecx)
    SPILL_TMP(%eax)                    # save array length
    movl    offGlue_method(%ecx),%ecx  # ecx<- glue->method
    movzwl  2(rPC),%eax                # eax<- CCCC
    movl    offMethod_clazz(%ecx),%ecx # ecx<- method->clazz
    movl    %eax,OUT_ARG1(%esp)
    movl    $$0,OUT_ARG2(%esp)
    movl    %ecx,OUT_ARG0(%esp)
    SPILL(rPC)
    call    dvmResolveClass            # eax<- call(clazz,ref,flag)
    UNSPILL(rPC)
    movl    %eax,%ecx
    UNSPILL_TMP(%eax)
    testl   %ecx,%ecx                  # successful resolution?
    je      common_exceptionThrown     # no, bail.
# fall through to ${opcode}_finish

    /*
     * Finish allocation
     *
     * ecx holds class
     * eax holds array length (vB)
     */
.L${opcode}_finish:
    movl    %ecx,OUT_ARG0(%esp)
    movl    %eax,OUT_ARG1(%esp)
    movl    $$ALLOC_DONT_TRACK,OUT_ARG2(%esp)
    SPILL(rPC)
    call    dvmAllocArrayByClass    # eax<- call(clazz,length,flags)
    UNSPILL(rPC)
    testl   %eax,%eax               # failed?
    je      common_exceptionThrown  # yup - go handle
    movl    rINST_FULL,%ecx
    FETCH_INST_WORD(2)
    SET_VREG(%eax,%ecx)
    ADVANCE_PC(2)
    GOTO_NEXT
