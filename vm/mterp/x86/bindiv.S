
%default {"result":"","special":""}
    /*
     * 32-bit binary div/rem operation.  Handles special case of op0=minint and
     * op1=-1.
     */
    /* binop vAA, vBB, vCC */
    movzbl   2(rPC),%eax            # eax<- BB
    movzbl   3(rPC),%ecx            # ecx<- CC
    GET_VREG_R %eax %eax            # eax<- vBB
    GET_VREG_R %ecx %ecx            # eax<- vBB
    cmpl     $$0,%ecx
    je       common_errDivideByZero
    cmpl     $$-1,%ecx
    jne      .L${opcode}_continue_div
    cmpl     $$0x80000000,%eax
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
