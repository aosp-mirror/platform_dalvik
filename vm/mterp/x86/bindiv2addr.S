%default {"result":"","special":""}
    /*
     * 32-bit binary div/rem operation.  Handles special case of op0=minint and
     * op1=-1.
     */
    /* div/rem/2addr vA, vB */
    movzx    rINSTbl,%ecx          # eax<- BA
    sarl     $$4,%ecx              # ecx<- B
    GET_VREG_R %ecx %ecx           # eax<- vBB
    andb     $$0xf,rINSTbl         # rINST<- A
    GET_VREG_R %eax rINST          # eax<- vBB
    cmpl     $$0,%ecx
    je       common_errDivideByZero
    cmpl     $$-1,%ecx
    jne      .L${opcode}_continue_div2addr
    cmpl     $$0x80000000,%eax
    jne      .L${opcode}_continue_div2addr
    movl     $special,$result
    jmp      .L${opcode}_finish_div2addr

%break
.L${opcode}_continue_div2addr:
    cltd
    idivl   %ecx
.L${opcode}_finish_div2addr:
    SET_VREG $result rINST
    FETCH_INST_OPCODE 1 %edx
    ADVANCE_PC 1
    GOTO_NEXT_R %edx
