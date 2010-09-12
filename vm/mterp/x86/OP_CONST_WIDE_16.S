%verify "executed"
    /* const-wide/16 vAA, #+BBBB */
    movswl    2(rPC),%eax               # eax<- ssssBBBB
    cltd                                # rPC:eax<- ssssssssssssBBBB
    SET_VREG_WORD %edx rINST 1          # store msw
    FETCH_INST_OPCODE 2 %edx
    SET_VREG_WORD %eax rINST 0          # store lsw
    ADVANCE_PC 2
    GOTO_NEXT_R %edx
