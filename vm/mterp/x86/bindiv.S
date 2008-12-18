
%default {"result":"","special":""}
    /*
     * 32-bit binary div/rem operation.  Handles special case of op0=minint and
     * op1=-1.
     */
    /* binop vAA, vBB, vCC */
    movzbl   2(rPC),%eax            # eax<- BB
    movzbl   3(rPC),%ecx            # ecx<- CC
    GET_VREG(%eax,%eax)             # eax<- vBB
    GET_VREG(%ecx,%ecx)             # eax<- vBB
    SPILL(rPC)
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
    movzbl   rINST_HI,%ecx         # ecl<- AA
    SET_VREG($result,%ecx)
    UNSPILL(rPC)
    FETCH_INST_WORD(2)
    ADVANCE_PC(2)
    GOTO_NEXT
