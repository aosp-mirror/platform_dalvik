%verify "executed"
    /* const vAA, #+BBBBbbbb */
    movzbl    rINST_HI,%ecx           # ecx<- AA
    movl      2(rPC),%eax               # grab all 32 bits at once
    FETCH_INST_WORD(3)
    ADVANCE_PC(3)
    SET_VREG(%eax,%ecx)                 # vAA<- eax
    GOTO_NEXT
