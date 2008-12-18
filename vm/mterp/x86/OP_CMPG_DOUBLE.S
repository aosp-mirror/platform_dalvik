%default {"is_double":"1","nanval":"1"}
%verify "executed"
%verify "basic lt, gt, eq"
%verify "left arg NaN"
%verify "right arg NaN"
    /* float/double_cmp[gl] vAA, vBB, vCC */
    movzbl    3(rPC),%eax             # eax<- CC
    movzbl    2(rPC),%ecx             # ecx<- BB
    .if $is_double
    fldl     (rFP,%eax,4)
    fldl     (rFP,%ecx,4)
    .else
    flds     (rFP,%eax,4)
    flds     (rFP,%ecx,4)
    .endif
    movzbl   rINST_HI,rINST_FULL
    xorl     %ecx,%ecx
    fucompp     # z if equal, p set if NaN, c set if st0 < st1
    fnstsw   %ax
    sahf
    movl      rINST_FULL,%eax
    FETCH_INST_WORD(2)
    jp       .L${opcode}_isNaN
    je       .L${opcode}_finish
    sbbl     %ecx,%ecx
    jb       .L${opcode}_finish
    incl     %ecx
.L${opcode}_finish:
    SET_VREG(%ecx,%eax)
    ADVANCE_PC(2)
    GOTO_NEXT
%break

.L${opcode}_isNaN:
    movl      $$$nanval,%ecx
    jmp       .L${opcode}_finish
