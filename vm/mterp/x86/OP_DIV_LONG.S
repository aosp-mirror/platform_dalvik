%verify "executed"
%default {"routine":"__divdi3","special":"$0x80000000"}
    /* div vAA, vBB, vCC */
    movzbl    3(rPC),%eax              # eax<- CC
    movzbl    2(rPC),%ecx              # ecx<- BB
    GET_VREG_WORD %edx %eax 0
    GET_VREG_WORD %eax %eax 1
    movl     %edx,OUT_ARG2(%esp)
    testl    %eax,%eax
    je       .L${opcode}_check_zero
    cmpl     $$-1,%eax
    je       .L${opcode}_check_neg1
.L${opcode}_notSpecial:
    GET_VREG_WORD %edx %ecx 0
    GET_VREG_WORD %ecx %ecx 1
.L${opcode}_notSpecial1:
    movl     %eax,OUT_ARG3(%esp)
    movl     %edx,OUT_ARG0(%esp)
    movl     %ecx,OUT_ARG1(%esp)
    jmp      .L${opcode}_continue
%break

.L${opcode}_continue:
    call     $routine
.L${opcode}_finish:
    SET_VREG_WORD %edx rINST 1
    SET_VREG_WORD %eax rINST 0
    FETCH_INST_OPCODE 2 %edx
    ADVANCE_PC 2
    GOTO_NEXT_R %edx

.L${opcode}_check_zero:
    testl   %edx,%edx
    jne     .L${opcode}_notSpecial
    jmp     common_errDivideByZero
.L${opcode}_check_neg1:
    testl   %edx,%eax
    jne     .L${opcode}_notSpecial
    GET_VREG_WORD %edx %ecx 0
    GET_VREG_WORD %ecx %ecx 1
    testl    %edx,%edx
    jne      .L${opcode}_notSpecial1
    cmpl     $$0x80000000,%ecx
    jne      .L${opcode}_notSpecial1
    /* minint / -1, return minint on div, 0 on rem */
    xorl     %eax,%eax
    movl     $special,%edx
    jmp      .L${opcode}_finish
