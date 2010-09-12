%verify "executed"
%default {"routine":"__divdi3","special":"$0x80000000"}
    /* div/2addr vA, vB */
    movzbl    rINSTbl,%eax
    shrl      $$4,%eax                  # eax<- B
    andb      $$0xf,rINSTbl             # rINST<- A
    GET_VREG_WORD %edx %eax 0
    GET_VREG_WORD %eax %eax 1
    movl     %edx,OUT_ARG2(%esp)
    testl    %eax,%eax
    je       .L${opcode}_check_zero
    cmpl     $$-1,%eax
    je       .L${opcode}_check_neg1
.L${opcode}_notSpecial:
    GET_VREG_WORD %edx rINST 0
    GET_VREG_WORD %ecx rINST 1
.L${opcode}_notSpecial1:
    jmp      .L${opcode}_continue
%break

.L${opcode}_continue:
    movl     %eax,OUT_ARG3(%esp)
    movl     %edx,OUT_ARG0(%esp)
    movl     %ecx,OUT_ARG1(%esp)
    call     $routine
.L${opcode}_finish:
    SET_VREG_WORD %edx rINST 1
    SET_VREG_WORD %eax rINST 0
    FETCH_INST_OPCODE 1 %edx
    ADVANCE_PC 1
    GOTO_NEXT_R %edx

.L${opcode}_check_zero:
    testl   %edx,%edx
    jne     .L${opcode}_notSpecial
    jmp     common_errDivideByZero
.L${opcode}_check_neg1:
    testl   %edx,%eax
    jne     .L${opcode}_notSpecial
    GET_VREG_WORD %edx rINST 0
    GET_VREG_WORD %ecx rINST 1
    testl    %edx,%edx
    jne      .L${opcode}_notSpecial1
    cmpl     $$0x80000000,%ecx
    jne      .L${opcode}_notSpecial1
    /* minint / -1, return minint on div, 0 on rem */
    xorl     %eax,%eax
    movl     $special,%edx
    jmp      .L${opcode}_finish
