%verify "executed"
%verify "exception handled"
    /*
     * Execute a "native inline" instruction.
     *
     * We will be calling through a function table:
     *
     * (*gDvmInlineOpsTable[opIndex].func)(arg0, arg1, arg2, arg3, pResult)
     *
     * Ignores argument count - always loads 4.
     *
     */
    /* [opt] execute-inline vAA, {vC, vD, vE, vF}, inline@BBBB */
    movl      rSELF,%ecx
    EXPORT_PC
    movzwl    2(rPC),%eax               # eax<- BBBB
    leal      offThread_retval(%ecx),%ecx # ecx<- & self->retval
    movl      %ecx,OUT_ARG4(%esp)
    call      .L${opcode}_continue      # make call; will return after
    testl     %eax,%eax                 # successful?
    FETCH_INST_OPCODE 3 %edx
    je        common_exceptionThrown    # no, handle exception
    ADVANCE_PC 3
    GOTO_NEXT_R %edx
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
    movzwl    4(rPC),%edx

    movl      $$0xf,%ecx
    andl      %edx,%ecx
    GET_VREG_R  %ecx %ecx
    sarl      $$4,%edx
    movl      %ecx,4+OUT_ARG0(%esp)

    movl      $$0xf,%ecx
    andl      %edx,%ecx
    GET_VREG_R  %ecx %ecx
    sarl      $$4,%edx
    movl      %ecx,4+OUT_ARG1(%esp)

    movl      $$0xf,%ecx
    andl      %edx,%ecx
    GET_VREG_R  %ecx %ecx
    sarl      $$4,%edx
    movl      %ecx,4+OUT_ARG2(%esp)

    movl      $$0xf,%ecx
    andl      %edx,%ecx
    GET_VREG_R  %ecx %ecx
    sarl      $$4,%edx
    movl      %ecx,4+OUT_ARG3(%esp)

    sall      $$4,%eax      # index *= sizeof(table entry)
    jmp       *gDvmInlineOpsTable(%eax)
    # will return to caller of .L${opcode}_continue
