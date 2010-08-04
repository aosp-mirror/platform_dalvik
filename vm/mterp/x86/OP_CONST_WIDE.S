%verify "executed"
    /* const-wide vAA, #+HHHHhhhhBBBBbbbb */
    movl      2(rPC),%eax         # eax<- lsw
    movzbl    rINST_HI,%ecx       # ecx <- AA
    movl      6(rPC),rINST_FULL   # rINST_FULL<- msw
    leal      (rFP,%ecx,4),%ecx   # dst addr
    movl      rINST_FULL,4(%ecx)
    FETCH_INST_WORD(5)
    movl      %eax,(%ecx)
    ADVANCE_PC(5)
    GOTO_NEXT
