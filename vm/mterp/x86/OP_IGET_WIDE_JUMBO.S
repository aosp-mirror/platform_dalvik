%verify "executed"
%verify "null object"
%verify "field already resolved"
%verify "field not yet resolved"
%verify "field cannot be resolved"
    /*
     * Jumbo 64-bit instance field get.
     */
    /* iget-wide/jumbo vBBBB, vCCCC, field@AAAA */
    movl    rSELF,%ecx
    SPILL(rIBASE)                               # preserve rIBASE
    movl    2(rPC),rIBASE                       # rIBASE<- AAAAAAAA
    movl    offThread_methodClassDex(%ecx),%eax # eax<- DvmDex
    movzwl  8(rPC),%ecx                         # ecx<- CCCC
    movl    offDvmDex_pResFields(%eax),%eax     # eax<- pDvmDex->pResFields
    GET_VREG_R %ecx %ecx                        # ecx<- fp[CCCC], the object ptr
    movl    (%eax,rIBASE,4),%eax                # resolved entry
    testl   %eax,%eax                           # is resolved entry null?
    jne     .L${opcode}_finish                  # no, already resolved
    movl    rIBASE,OUT_ARG1(%esp)               # for dvmResolveInstField
    movl    rSELF,rIBASE
    EXPORT_PC
    movl    offThread_method(rIBASE),rIBASE     # rIBASE<- current method
    movl    offMethod_clazz(rIBASE),rIBASE      # rIBASE<- method->clazz
    SPILL_TMP1(%ecx)                            # save objpointer across call
    movl    rIBASE,OUT_ARG0(%esp)               # pass in method->clazz
    call    dvmResolveInstField                 #  ... to dvmResolveInstField
    UNSPILL_TMP1(%ecx)
    testl   %eax,%eax                           # returns InstrField ptr
    jne     .L${opcode}_finish
    jmp     common_exceptionThrown

.L${opcode}_finish:
    /*
     * Currently:
     *   eax holds resolved field
     *   ecx holds object
     *   rINST holds BBBB
     */
    movl    offInstField_byteOffset(%eax),%eax  # eax<- byte offset of field
    testl   %ecx,%ecx                           # object null?
    je      common_errNullObject                # object was null
    leal    (%ecx,%eax,1),%eax                  # eax<- address of field
    movl    (%eax),%ecx                         # ecx<- lsw
    movl    4(%eax),%eax                        # eax<- msw
    SET_VREG_WORD %ecx rINST 0
    FETCH_INST_OPCODE 5 %ecx
    UNSPILL(rIBASE)                             # restore rIBASE
    SET_VREG_WORD %eax rINST 1
    ADVANCE_PC 5
    GOTO_NEXT_R %ecx
