%default {"result":"","special":""}
    /*
     * 32-bit div/rem "lit8" binary operation.  Handles special case of
     * op0=minint & op1=-1
     */
    /* div/rem/lit8 vAA, vBB, #+CC */
    movzbl    2(rPC),%eax              # eax<- BB
    movsbl    3(rPC),%ecx              # ecx<- ssssssCC
    GET_VREG  (%eax,%eax)              # eax<- rBB
    movzx     rINST_HI,rINST_FULL      # rINST_FULL<- AA
    SPILL(rPC)
    cmpl     $$0,%ecx
    je       common_errDivideByZero
    cmpl     $$0x80000000,%eax
    jne      .L${opcode}_continue_div
    cmpl     $$-1,%ecx
    jne      .L${opcode}_continue_div
    movl     $special,$result
    jmp      .L${opcode}_finish_div

%break
.L${opcode}_continue_div:
    cltd
    idivl   %ecx
.L${opcode}_finish_div:
    SET_VREG($result,rINST_FULL)
    UNSPILL(rPC)
    FETCH_INST_WORD(2)
    ADVANCE_PC(2)
    GOTO_NEXT
