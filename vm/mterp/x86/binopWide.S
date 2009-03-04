    /*
     * Generic 64-bit binary operation.
     */
    /* binop vAA, vBB, vCC */

    movzbl    2(rPC),%eax               # eax<- BB
    movzbl    3(rPC),%ecx               # ecx<- CC
    SPILL(rPC)
    GET_VREG_WORD(rPC,%eax,0)           # rPC<- v[BB+0]
    GET_VREG_WORD(%eax,%eax,1)          # eax<- v[BB+1]
    $instr1         # ex: addl   (rFP,%ecx,4),rPC
    $instr2         # ex: adcl   4(rFP,%ecx,4),%eax
    movzbl    rINST_HI,rINST_FULL       # rINST_FULL<- AA
    SET_VREG_WORD(rPC,rINST_FULL,0)     # v[AA+0] <- rPC
    UNSPILL(rPC)
    SET_VREG_WORD(%eax,rINST_FULL,1)    # v[AA+1] <- eax
    FETCH_INST_WORD(2)
    ADVANCE_PC(2)
    GOTO_NEXT
