%verify "executed"
%verify "field already resolved"
%verify "field not yet resolved"
%verify "field cannot be resolved"
    /*
     * Jumbo 64-bit SGET handler.
     *
     */
    /* sget-wide/jumbo vBBBB, field@AAAAAAAA */
    movl      rGLUE,%ecx
    movl      offGlue_methodClassDex(%ecx),%ecx  # ecx<- DvmDex
    movl      2(rPC),%eax                        # eax<- field ref AAAAAAAA
    movl      offDvmDex_pResFields(%ecx),%ecx    # ecx<- dvmDex->pResFields
    movl      (%ecx,%eax,4),%eax                 # eax<- resolved StaticField ptr
    testl     %eax,%eax                          # resolved entry null?
    je        .L${opcode}_resolve                # if not, make it so
.L${opcode}_finish:     # field ptr in eax
    movl      offStaticField_value(%eax),%ecx    # ecx<- lsw
    movl      4+offStaticField_value(%eax),%eax  # eax<- msw
    FETCH_INST_OPCODE 2 %edx
    ADVANCE_PC 2
    SET_VREG_WORD %ecx rINST 0
    SET_VREG_WORD %eax rINST 1
    GOTO_NEXT_R %edx
%break

    /*
     * Go resolve the field
     */
.L${opcode}_resolve:
    movl     rGLUE,%ecx
    movl     2(rPC),%eax                        # eax<- field ref AAAAAAAA
    movl     offGlue_method(%ecx),%ecx          # ecx<- current method
    EXPORT_PC                                   # could throw, need to export
    movl     offMethod_clazz(%ecx),%ecx         # ecx<- method->clazz
    movl     %eax,OUT_ARG1(%esp)
    movl     %ecx,OUT_ARG0(%esp)
    call     dvmResolveStaticField              # eax<- resolved StaticField ptr
    testl    %eax,%eax
    jne      .L${opcode}_finish                 # success, continue
    jmp      common_exceptionThrown             # no, handle exception
