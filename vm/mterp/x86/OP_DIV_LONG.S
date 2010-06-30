%verify "executed"
%default {"routine":"__divdi3","special":"$0x80000000"}
    /* div vAA, vBB, vCC */
    movzbl    3(rPC),%eax              # eax<- CC
    movzbl    2(rPC),%ecx              # ecx<- BB
    SPILL(rPC)
    GET_VREG_WORD(rPC,%eax,0)
    GET_VREG_WORD(%eax,%eax,1)
    movl     rPC,OUT_ARG2(%esp)
    testl    %eax,%eax
    je       .L${opcode}_check_zero
    cmpl     $$-1,%eax
    je       .L${opcode}_check_neg1
.L${opcode}_notSpecial:
    GET_VREG_WORD(rPC,%ecx,0)
    GET_VREG_WORD(%ecx,%ecx,1)
.L${opcode}_notSpecial1:
    movl     %eax,OUT_ARG3(%esp)
    movl     rPC,OUT_ARG0(%esp)
    movl     %ecx,OUT_ARG1(%esp)
    jmp      .L${opcode}_continue
%break

.L${opcode}_continue:
    call     $routine
.L${opcode}_finish:
    movzbl   rINST_HI,%ecx
    SET_VREG_WORD(rPC,%ecx,1)
    UNSPILL(rPC)
    SET_VREG_WORD(%eax,%ecx,0)
    FETCH_INST_WORD(2)
    ADVANCE_PC(2)
    GOTO_NEXT

.L${opcode}_check_zero:
    testl   rPC,rPC
    jne     .L${opcode}_notSpecial
    UNSPILL(rPC)
    jmp     common_errDivideByZero
.L${opcode}_check_neg1:
    testl   rPC,%eax
    jne     .L${opcode}_notSpecial
    GET_VREG_WORD(rPC,%ecx,0)
    GET_VREG_WORD(%ecx,%ecx,1)
    testl    rPC,rPC
    jne      .L${opcode}_notSpecial1
    cmpl     $$0x80000000,%ecx
    jne      .L${opcode}_notSpecial1
    /* minint / -1, return minint on div, 0 on rem */
    xorl     %eax,%eax
    movl     $special,%edx
    jmp      .L${opcode}_finish
