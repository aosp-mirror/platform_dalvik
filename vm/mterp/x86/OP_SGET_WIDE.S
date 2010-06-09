%verify "executed"
%verify "field already resolved"
%verify "field not yet resolved"
%verify "field cannot be resolved"
    /*
     * 64-bit SGET handler.
     *
     */
    /* sget-wide vAA, field@BBBB */
    GET_GLUE(%ecx)
    movzwl    2(rPC),%eax                        # eax<- field ref BBBB
    movl      offGlue_methodClassDex(%ecx),%ecx  # ecx<- DvmDex
    movl      offDvmDex_pResFields(%ecx),%ecx    # ecx<- dvmDex->pResFields
    movl      (%ecx,%eax,4),%eax                 # eax<- resolved StaticField ptr
    testl     %eax,%eax                          # resolved entry null?
    je        .L${opcode}_resolve                # if not, make it so
.L${opcode}_finish:     # field ptr in eax
    movl      offStaticField_value(%eax),%ecx    # ecx<- lsw
    movl      4+offStaticField_value(%eax),%eax  # eax<- msw
    movzbl    rINST_HI,rINST_FULL                # rINST_FULL<- AA
    SET_VREG_WORD(%ecx,rINST_FULL,0)
    SET_VREG_WORD(%eax,rINST_FULL,1)
    FETCH_INST_WORD(2)
    ADVANCE_PC(2)
    GOTO_NEXT
%break

    /*
     * Go resolve the field
     */
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
