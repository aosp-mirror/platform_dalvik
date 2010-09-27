%default { "sqnum":"0" }
%verify "executed"
%verify "null object"
%verify "field already resolved"
%verify "field not yet resolved"
%verify "field cannot be resolved"
    /*
     * Object field put.
     *
     * for: iput-object
     */
    /* op vA, vB, field@CCCC */
    GET_GLUE(%ecx)
    SPILL(rIBASE)                                 # need another reg
    movzwl  2(rPC),rIBASE                         # rIBASE<- 0000CCCC
    movl    offGlue_methodClassDex(%ecx),%eax     # eax<- DvmDex
    movzbl  rINST_HI,%ecx                         # ecx<- BA
    sarl    $$4,%ecx                              # ecx<- B
    movl    offDvmDex_pResFields(%eax),%eax       # eax<- pDvmDex->pResFields
    movzbl  rINST_HI,rINST_FULL                   # rINST_FULL<- BA
    andb    $$0xf,rINST_LO                        # rINST_FULL<- A
    GET_VREG(%ecx,%ecx)                           # ecx<- fp[B], the object ptr
    movl    (%eax,rIBASE,4),%eax                  # resolved entry
    testl   %eax,%eax                             # is resolved entry null?
    jne     .L${opcode}_finish                    # no, already resolved
    movl    rIBASE,OUT_ARG1(%esp)
    GET_GLUE(rIBASE)
    jmp     .L${opcode}_resolve
%break


.L${opcode}_resolve:
    EXPORT_PC()
    SPILL(rPC)
    movl    offGlue_method(rIBASE),rPC            # rPC<- current method
    UNSPILL(rIBASE)
    movl    offMethod_clazz(rPC),rPC              # rPC<- method->clazz
    SPILL_TMP(%ecx)                               # save object pointer across call
    movl    rPC,OUT_ARG0(%esp)                    # pass in method->clazz
    call    dvmResolveInstField                   #  ... to dvmResolveInstField
    UNSPILL_TMP(%ecx)
    UNSPILL(rPC)
    testl   %eax,%eax                             #  ... which returns InstrField ptr
    jne     .L${opcode}_finish
    jmp     common_exceptionThrown

.L${opcode}_finish:
    /*
     * Currently:
     *   eax holds resolved field
     *   ecx holds object
     *   rIBASE is scratch, but needs to be unspilled
     *   rINST_FULL holds A
     */
    GET_VREG(rINST_FULL,rINST_FULL)              # rINST_FULL<- v[A]
    movl    offInstField_byteOffset(%eax),%eax   # eax<- byte offset of field
    UNSPILL(rIBASE)
    testl   %ecx,%ecx                            # object null?
    je      common_errNullObject                 # object was null
    movl    rINST_FULL,(%ecx,%eax)          # obj.field <- v[A](8/16/32 bits)
    GET_GLUE(%eax)
    testl   rINST_FULL,rINST_FULL                # stored a NULL?
    movl    offGlue_cardTable(%eax),%eax         # get card table base
    FETCH_INST_WORD(2)
    je      1f                                   # skip card mark if null store
    shrl    $$GC_CARD_SHIFT,%ecx                 # object head to card number
    movb    %al,(%eax,%ecx)                      # mark card using object head
1:
    ADVANCE_PC(2)
    GOTO_NEXT
