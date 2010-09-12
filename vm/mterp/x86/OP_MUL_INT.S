%verify "executed"
    /*
     * 32-bit binary multiplication.
     */
    /* mul vAA, vBB, vCC */
    movzbl   2(rPC),%eax            # eax<- BB
    movzbl   3(rPC),%ecx            # ecx<- CC
    GET_VREG_R %eax %eax            # eax<- vBB
    imull    (rFP,%ecx,4),%eax      # trashes edx
    FETCH_INST_OPCODE 2 %edx
    ADVANCE_PC 2
    SET_VREG %eax rINST
    GOTO_NEXT_R %edx
