%default {"result":"","special":""}
    /*
     * 32-bit binary div/rem operation.  Handles special case of op0=minint and
     * op1=-1.
     */
    /* div/rem/2addr vA, vB */
    movzx    rINST_HI,%ecx          # eax<- BA
    sarl     $$4,%ecx               # ecx<- B
    GET_VREG(%ecx,%ecx)             # eax<- vBB
    movzbl   rINST_HI,rINST_FULL    # rINST_FULL<- BA
    andb     $$0xf,rINST_LO         # rINST_FULL<- A
    GET_VREG(%eax,rINST_FULL)       # eax<- vBB
    SPILL(rPC)
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
    SET_VREG($result,rINST_FULL)
    UNSPILL(rPC)
    FETCH_INST_WORD(1)
    ADVANCE_PC(1)
    GOTO_NEXT
