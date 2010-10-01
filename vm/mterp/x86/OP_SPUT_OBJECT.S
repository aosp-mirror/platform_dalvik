%verify "executed"
%verify "field already resolved"
%verify "field not yet resolved"
%verify "field cannot be resolved"
    /*
     * SPUT object handler.
     */
    /* op vAA, field@BBBB */
    GET_GLUE(%ecx)
    movzwl    2(rPC),%eax                        # eax<- field ref BBBB
    movl      offGlue_methodClassDex(%ecx),%ecx  # ecx<- DvmDex
    movl      offDvmDex_pResFields(%ecx),%ecx    # ecx<- dvmDex->pResFields
    movl      (%ecx,%eax,4),%eax                 # eax<- resolved StaticField
    testl     %eax,%eax                          # resolved entry null?
    je        .L${opcode}_resolve                # if not, make it so
.L${opcode}_finish:     # field ptr in eax
    movzbl    rINST_HI,%ecx                      # ecx<- AA
    GET_VREG(%ecx,%ecx)
    jmp       .L${opcode}_continue
%break


.L${opcode}_continue:
    movl      %ecx,offStaticField_value(%eax)    # do the store
    testl     %ecx,%ecx                          # stored null object ptr?
    FETCH_INST_WORD(2)
    je        1f                                 # skip card mark if null
    GET_GLUE(%ecx)
    movl      offField_clazz(%eax),%eax          # eax<- field->clazz
    movl      offGlue_cardTable(%ecx),%ecx       # get card table base
    shrl      $$GC_CARD_SHIFT,%eax               # head to card number
    movb      %cl,(%ecx,%eax)                    # mark card
1:
    ADVANCE_PC(2)
    GOTO_NEXT

.L${opcode}_resolve:
    GET_GLUE(%ecx)
    movzwl   2(rPC),%eax                        # eax<- field ref BBBB
    movl     offGlue_method(%ecx),%ecx          # ecx<- current method
    EXPORT_PC()                                 # could throw, need to export
    movl     offMethod_clazz(%ecx),%ecx         # ecx<- method->clazz
    SPILL(rPC)
    movl     %eax,OUT_ARG1(%esp)
    movl     %ecx,OUT_ARG0(%esp)
    call     dvmResolveStaticField              # eax<- resolved StaticField ptr
    UNSPILL(rPC)
    testl    %eax,%eax
    jne      .L${opcode}_finish                 # success, continue
    jmp      common_exceptionThrown             # no, handle exception
