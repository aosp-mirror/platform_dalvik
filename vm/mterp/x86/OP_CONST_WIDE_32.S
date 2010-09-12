%verify "executed"
    /* const-wide/32 vAA, #+BBBBbbbb */
    movl     2(rPC),%eax                # eax<- BBBBbbbb
    cltd                                # rPC:eax<- ssssssssssssBBBB
    SET_VREG_WORD %edx rINST,1          # store msw
    FETCH_INST_OPCODE 3 %edx
    SET_VREG_WORD %eax rINST 0          # store lsw
    ADVANCE_PC 3
    GOTO_NEXT_R %edx
