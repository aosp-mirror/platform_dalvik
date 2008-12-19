    /*
     * Generic 64-bit binary operation.
     */
    /* binop/2addr vA, vB */
    movzbl    rINST_HI,%ecx             # ecx<- BA
    sarl      $$4,%ecx                  # ecx<- B
    GET_VREG_WORD(%eax,%ecx,0)          # eax<- v[B+0]
    GET_VREG_WORD(%ecx,%ecx,1)          # eax<- v[B+1]
    movzbl    rINST_HI,rINST_FULL       # rINST_FULL<- BA
    andb      $$0xF,rINST_LO            # rINST_FULL<- A
    $instr1         # example: addl   %eax,(rFP,rINST_FULL,4)
    $instr2         # example: adcl   %ecx,4(rFP,rINST_FULL,4)
    FETCH_INST_WORD(1)
    ADVANCE_PC(1)
    GOTO_NEXT
