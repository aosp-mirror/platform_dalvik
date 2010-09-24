%verify "executed"
    /* const-wide vAA, #+HHHHhhhhBBBBbbbb */
    movl      2(rPC),%eax         # eax<- lsw
    movzbl    rINSTbl,%ecx        # ecx<- AA
    movl      6(rPC),rINST        # rINST<- msw
    leal      (rFP,%ecx,4),%ecx   # dst addr
    movl      rINST,4(%ecx)
    FETCH_INST_OPCODE 5 %edx
    movl      %eax,(%ecx)
    ADVANCE_PC 5
    GOTO_NEXT_R %edx
