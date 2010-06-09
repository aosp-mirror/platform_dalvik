%verify "executed"
    /* mul/lit8 vAA, vBB, #+CC */
    movzbl    2(rPC),%eax              # eax<- BB
    movsbl    3(rPC),%ecx              # ecx<- ssssssCC
    SPILL(rPC)
    movzx     rINST_HI,rINST_FULL      # rINST_FULL<- AA
    GET_VREG  (%eax,%eax)              # eax<- rBB
    imull     %ecx,%eax                # trashes rPC
    UNSPILL(rPC)
    SET_VREG  (%eax,rINST_FULL)
    FETCH_INST_WORD(2)
    ADVANCE_PC(2)
    GOTO_NEXT
