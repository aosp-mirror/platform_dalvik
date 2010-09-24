%default {"result":"","special":""}
    /*
     * 32-bit div/rem "lit8" binary operation.  Handles special case of
     * op0=minint & op1=-1
     */
    /* div/rem/lit8 vAA, vBB, #+CC */
    movzbl    2(rPC),%eax        # eax<- BB
    movsbl    3(rPC),%ecx        # ecx<- ssssssCC
    GET_VREG_R  %eax %eax        # eax<- rBB
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
    SET_VREG $result rINST
    FETCH_INST_OPCODE 2 %edx
    ADVANCE_PC 2
    GOTO_NEXT_R %edx
