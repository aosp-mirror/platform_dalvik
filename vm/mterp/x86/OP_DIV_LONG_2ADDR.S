%verify "executed"
%default {"routine":"__divdi3","special":"$0x80000000"}
    /* div/2addr vA, vB */
    movzbl    rINST_HI,%eax
    shrl      $$4,%eax                  # eax<- B
    movzbl    rINST_HI,rINST_FULL
    andb      $$0xf,rINST_LO            # rINST_FULL<- A
    SPILL(rPC)
    GET_VREG_WORD(rPC,%eax,0)
    GET_VREG_WORD(%eax,%eax,1)
    movl     rPC,OUT_ARG2(%esp)
    testl    %eax,%eax
    je       .L${opcode}_check_zero
    cmpl     $$-1,%eax
    je       .L${opcode}_check_neg1
.L${opcode}_notSpecial:
    GET_VREG_WORD(rPC,rINST_FULL,0)
    GET_VREG_WORD(%ecx,rINST_FULL,1)
.L${opcode}_notSpecial1:
    jmp      .L${opcode}_continue
%break

.L${opcode}_continue:
    movl     %eax,OUT_ARG3(%esp)
    movl     rPC,OUT_ARG0(%esp)
    movl     %ecx,OUT_ARG1(%esp)
    call     $routine
.L${opcode}_finish:
    movl     rINST_FULL,%ecx
    SET_VREG_WORD(rPC,%ecx,1)
    UNSPILL(rPC)
    SET_VREG_WORD(%eax,%ecx,0)
    FETCH_INST_WORD(1)
    ADVANCE_PC(1)
    GOTO_NEXT

.L${opcode}_check_zero:
    testl   rPC,rPC
    jne     .L${opcode}_notSpecial
    UNSPILL(rPC)
    jmp     common_errDivideByZero
.L${opcode}_check_neg1:
    testl   rPC,%eax
    jne     .L${opcode}_notSpecial
    GET_VREG_WORD(rPC,rINST_FULL,0)
    GET_VREG_WORD(%ecx,rINST_FULL,1)
    testl    rPC,rPC
    jne      .L${opcode}_notSpecial1
    cmpl     $$0x80000000,%ecx
    jne      .L${opcode}_notSpecial1
    /* minint / -1, return minint on div, 0 on rem */
    xorl     %eax,%eax
    movl     $special,%edx
    jmp      .L${opcode}_finish
