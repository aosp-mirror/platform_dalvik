%verify "executed"
%verify "null object"
%verify "field already resolved"
%verify "field not yet resolved"
%verify "field cannot be resolved"
    /*
     * Jumbo 64-bit instance field put.
     */
    /* iput-wide/jumbo vBBBB, vCCCC, field@AAAAAAAA */
    movl    rGLUE,%ecx
    movl    2(rPC),%edx                         # edx<- AAAAAAAA
    movl    offGlue_methodClassDex(%ecx),%eax   # eax<- DvmDex
    movzwl  8(rPC),%ecx                         # ecx<- CCCC
    movl    offDvmDex_pResFields(%eax),%eax     # eax<- pDvmDex->pResFields
    GET_VREG_R %ecx %ecx                        # ecx<- fp[CCCC], the object ptr
    movl    (%eax,%edx,4),%eax                  # resolved entry
    testl   %eax,%eax                           # is resolved entry null?
    jne     .L${opcode}_finish                  # no, already resolved
    movl    %edx,OUT_ARG1(%esp)
    movl    rGLUE,%edx
    jmp     .L${opcode}_resolve
%break


.L${opcode}_resolve:
    EXPORT_PC
    movl    offGlue_method(%edx),%edx           # edx<- current method
    movl    offMethod_clazz(%edx),%edx          # edx<- method->clazz
    SPILL_TMP1(%ecx)                            # save obj pointer across call
    movl    %edx,OUT_ARG0(%esp)                 # pass in method->clazz
    call    dvmResolveInstField                 #  ... to dvmResolveInstField
    UNSPILL_TMP1(%ecx)
    testl   %eax,%eax                           #  ... which returns InstrField ptr
    jne     .L${opcode}_finish
    jmp     common_exceptionThrown

.L${opcode}_finish:
    /*
     * Currently:
     *   eax holds resolved field
     *   ecx holds object
     *   %edx is scratch, but needs to be unspilled
     *   rINST holds BBBB
     */
    movl    offInstField_byteOffset(%eax),%eax  # eax<- byte offset of field
    testl   %ecx,%ecx                           # object null?
    je      common_errNullObject                # object was null
    leal    (%ecx,%eax,1),%eax                  # eax<- address of field
    GET_VREG_WORD %ecx rINST 0                  # ecx<- lsw
    GET_VREG_WORD rINST rINST 1                 # rINST<- msw
    FETCH_INST_OPCODE 5 %edx
    movl    rINST,4(%eax)
    movl    %ecx,(%eax)
    ADVANCE_PC 5
    GOTO_NEXT_R %edx
