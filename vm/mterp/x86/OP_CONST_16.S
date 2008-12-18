%verify "executed"
    /* const/16 vAA, #+BBBB */
    movswl  2(rPC),%ecx                # ecx<- ssssBBBB
    movzx   rINST_HI,%eax              # eax<- AA
    FETCH_INST_WORD(2)
    ADVANCE_PC(2)
    SET_VREG(%ecx,%eax)                # vAA<- ssssBBBB
    GOTO_NEXT
