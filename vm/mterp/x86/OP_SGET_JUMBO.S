%verify "executed"
%verify "field already resolved"
%verify "field not yet resolved"
%verify "field cannot be resolved"
    /*
     * Jumbo 32-bit SGET handler.
     *
     * for: sget/jumbo, sget-object/jumbo, sget-boolean/jumbo, sget-byte/jumbo,
     *      sget-char/jumbo, sget-short/jumbo
     */
    /* exop vBBBB, field@AAAAAAAA */
    movl      rSELF,%ecx
    movl      offThread_methodClassDex(%ecx),%ecx  # ecx<- DvmDex
    movl      2(rPC),%eax                        # eax<- field ref AAAAAAAA
    movl      offDvmDex_pResFields(%ecx),%ecx    # ecx<- dvmDex->pResFields
    movl      (%ecx,%eax,4),%eax                 # eax<- resolved StaticField ptr
    testl     %eax,%eax                          # resolved entry null?
    je        .L${opcode}_resolve                # if not, make it so
.L${opcode}_finish:     # field ptr in eax
    movl      offStaticField_value(%eax),%eax
    FETCH_INST_OPCODE 4 %ecx
    ADVANCE_PC 4
    SET_VREG %eax rINST
    GOTO_NEXT_R %ecx

    /*
     * Go resolve the field
     */
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
