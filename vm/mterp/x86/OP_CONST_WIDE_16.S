%verify "executed"
    /* const-wide/16 vAA, #+BBBB */
    movswl    2(rPC),%eax               # eax<- ssssBBBB
    SPILL(rPC)
    movzbl    rINST_HI,%ecx             # ecx<- AA
    FETCH_INST_WORD(2)
    cltd                                # rPC:eax<- ssssssssssssBBBB
    SET_VREG_WORD(rPC,%ecx,1)           # store msw
    UNSPILL(rPC)
    SET_VREG_WORD(%eax,%ecx,0)          # store lsw
    ADVANCE_PC(2)
    GOTO_NEXT
