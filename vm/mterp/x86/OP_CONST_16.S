%verify "executed"
    /* const/16 vAA, #+BBBB */
    movswl  2(rPC),%ecx                # ecx<- ssssBBBB
    movl    rINST,%eax                 # eax<- AA
    FETCH_INST_OPCODE 2 %edx
    ADVANCE_PC 2
    SET_VREG %ecx %eax                 # vAA<- ssssBBBB
    GOTO_NEXT_R %edx
