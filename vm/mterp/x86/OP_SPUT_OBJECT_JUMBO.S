%verify "executed"
%verify "field already resolved"
%verify "field not yet resolved"
%verify "field cannot be resolved"
    /*
     * Jumbo SPUT object handler.
     */
    /* sput-object/jumbo vBBBB, field@AAAAAAAA */
    movl      rSELF,%ecx
    movl      2(rPC),%eax                        # eax<- field ref AAAAAAAA
    movl      offThread_methodClassDex(%ecx),%ecx  # ecx<- DvmDex
    movl      offDvmDex_pResFields(%ecx),%ecx    # ecx<- dvmDex->pResFields
    movl      (%ecx,%eax,4),%eax                 # eax<- resolved StaticField
    testl     %eax,%eax                          # resolved entry null?
    je        .L${opcode}_resolve                # if not, make it so
.L${opcode}_finish:                              # field ptr in eax
    GET_VREG_R  %ecx rINST
    movl      %ecx,offStaticField_value(%eax)    # do the store
    testl     %ecx,%ecx                          # stored null object ptr?
    je        1f                                 # skip card mark if null
    movl      rSELF,%ecx
    movl      offField_clazz(%eax),%eax          # eax<- method->clazz
    movl      offThread_cardTable(%ecx),%ecx       # get card table base
    shrl      $$GC_CARD_SHIFT,%eax               # head to card number
    movb      %cl,(%ecx,%eax)                    # mark card
1:
    FETCH_INST_OPCODE 4 %ecx
    ADVANCE_PC 4
    GOTO_NEXT_R %ecx

.L${opcode}_resolve:
    movl     rSELF,%ecx
    movl     2(rPC),%eax                        # eax<- field ref AAAAAAAA
    movl     offThread_method(%ecx),%ecx          # ecx<- current method
    EXPORT_PC                                   # could throw, need to export
    movl     offMethod_clazz(%ecx),%ecx         # ecx<- method->clazz
    movl     %eax,OUT_ARG1(%esp)
    movl     %ecx,OUT_ARG0(%esp)
    SPILL(rIBASE)
    call     dvmResolveStaticField              # eax<- resolved StaticField ptr
    UNSPILL(rIBASE)
    testl    %eax,%eax
    jne      .L${opcode}_finish                 # success, continue
    jmp      common_exceptionThrown             # no, handle exception
