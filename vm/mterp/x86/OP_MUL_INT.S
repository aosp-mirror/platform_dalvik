%verify "executed"
    /*
     * 32-bit binary multiplication.
     */
    /* mul vAA, vBB, vCC */
    movzbl   2(rPC),%eax            # eax<- BB
    movzbl   3(rPC),%ecx            # ecx<- CC
    SPILL(rPC)
    GET_VREG(%eax,%eax)             # eax<- vBB
    imull    (rFP,%ecx,4),%eax      # trashes rPC/edx
    UNSPILL(rPC)
    movzbl   rINST_HI,%ecx          # ecx<- AA
    FETCH_INST_WORD(2)
    ADVANCE_PC(2)
    SET_VREG(%eax,%ecx)
    GOTO_NEXT
