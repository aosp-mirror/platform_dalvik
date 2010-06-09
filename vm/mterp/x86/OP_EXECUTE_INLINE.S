%verify "executed"
%verify "exception handled"
    /*
     * Execute a "native inline" instruction.
     *
     * We will be calling through a function table:
     *
     * (*gDvmInlineOpsTable[opIndex].func)(arg0, arg1, arg2, arg3, pResult)
     *
     */
    /* [opt] execute-inline vAA, {vC, vD, vE, vF}, inline@BBBB */
    GET_GLUE(%ecx)
    EXPORT_PC()
    movzwl    2(rPC),%eax               # eax<- BBBB
    leal      offGlue_retval(%ecx),%ecx # ecx<- & glue->retval
    movl      %ecx,OUT_ARG4(%esp)
    sarl      $$12,rINST_FULL           # rINST_FULL<- arg count (0-4)
    SPILL(rPC)
    call      .L${opcode}_continue      # make call; will return after
    UNSPILL(rPC)
    testl     %eax,%eax                 # successful?
    FETCH_INST_WORD(3)
    je        common_exceptionThrown    # no, handle exception
    ADVANCE_PC(3)
    GOTO_NEXT
%break

.L${opcode}_continue:
    /*
     * Extract args, call function.
     *  ecx = #of args (0-4)
     *  eax = call index
     *  @esp = return addr
     *  esp is -4 from normal
     *
     *  Go ahead and load all 4 args, even if not used.
     */
    movzwl    4(rPC),rPC

    movl      $$0xf,%ecx
    andl      rPC,%ecx
    GET_VREG(%ecx,%ecx)
    sarl      $$4,rPC
    movl      %ecx,4+OUT_ARG0(%esp)

    movl      $$0xf,%ecx
    andl      rPC,%ecx
    GET_VREG(%ecx,%ecx)
    sarl      $$4,rPC
    movl      %ecx,4+OUT_ARG1(%esp)

    movl      $$0xf,%ecx
    andl      rPC,%ecx
    GET_VREG(%ecx,%ecx)
    sarl      $$4,rPC
    movl      %ecx,4+OUT_ARG2(%esp)

    movl      $$0xf,%ecx
    andl      rPC,%ecx
    GET_VREG(%ecx,%ecx)
    sarl      $$4,rPC
    movl      %ecx,4+OUT_ARG3(%esp)

    sall      $$4,%eax      # index *= sizeof(table entry)
    jmp       *gDvmInlineOpsTable(%eax)
    # will return to caller of .L${opcode}_continue
