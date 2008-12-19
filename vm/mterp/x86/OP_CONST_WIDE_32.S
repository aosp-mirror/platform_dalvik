%verify "executed"
    /* const-wide/32 vAA, #+BBBBbbbb */
    movl     2(rPC),%eax               # eax<- BBBBbbbb
    SPILL(rPC)
    movzbl    rINST_HI,%ecx             # ecx<- AA
    FETCH_INST_WORD(3)
    cltd                                # rPC:eax<- ssssssssssssBBBB
    SET_VREG_WORD(rPC,%ecx,1)           # store msw
    UNSPILL(rPC)
    SET_VREG_WORD(%eax,%ecx,0)          # store lsw
    ADVANCE_PC(3)
    GOTO_NEXT
