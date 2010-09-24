%verify "executed"
    /* mul/lit8 vAA, vBB, #+CC */
    movzbl    2(rPC),%eax              # eax<- BB
    movsbl    3(rPC),%ecx              # ecx<- ssssssCC
    GET_VREG_R   %eax %eax             # eax<- rBB
    imull     %ecx,%eax                # trashes edx
    FETCH_INST_OPCODE 2 %edx
    ADVANCE_PC 2
    SET_VREG  %eax rINST
    GOTO_NEXT_R %edx
