    /*
     * Generic 32-bit binary float operation.
     *
     * For: add-fp, sub-fp, mul-fp, div-fp
     */

    /* binop/2addr vA, vB */
    movzx   rINST_HI,%ecx               # ecx<- A+
    andb    $$0xf,%cl                   # ecx<- A
    $load    (rFP,%ecx,4)               # vAA to fp stack
    sarl    $$12,rINST_FULL             # rINST_FULL<- B
    $instr   (rFP,rINST_FULL,4)           # ex: faddp
    FETCH_INST_WORD(1)
    ADVANCE_PC(1)
    $store    (rFP,%ecx,4)              # %st to vA
    GOTO_NEXT
