%default {"result":"","special":""}
    /*
     * 32-bit binary div/rem operation.  Handles special case of op0=minint and
     * op1=-1.
     */
    /* div/rem/lit16 vA, vB, #+CCCC */
    /* Need A in rINST_FULL, ssssCCCC in ecx, vB in eax */
    movzbl   rINST_HI,%eax              # eax<- 000000BA
    sarl     $$4,%eax                   # eax<- B
    GET_VREG(%eax,%eax)                 # eax<- vB
    movswl   2(rPC),%ecx                # ecx<- ssssCCCC
    movzbl   rINST_HI,rINST_FULL        # rINST_FULL<- BA
    andb     $$0xf,rINST_LO             # rINST_FULL<- A
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
    SET_VREG($result,rINST_FULL)
    UNSPILL(rPC)
    FETCH_INST_WORD(2)
    ADVANCE_PC(2)
    GOTO_NEXT
