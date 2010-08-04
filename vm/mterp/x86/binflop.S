    /*
     * Generic 32-bit binary float operation.
     *
     * For: add-fp, sub-fp, mul-fp, div-fp
     */
    /* binop vAA, vBB, vCC */
    movzbl   2(rPC),%eax            # eax<- CC
    movzbl   3(rPC),%ecx            # ecx<- BB
    $load    (rFP,%eax,4)           # vCC to fp stack
    $instr   (rFP,%ecx,4)           # ex: faddp
    movzbl   rINST_HI,%ecx         # ecx<- AA
    FETCH_INST_WORD(2)
    ADVANCE_PC(2)
    $store   (rFP,%ecx,4)           # %st to vAA
    GOTO_NEXT
