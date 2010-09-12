    /*
     * Generic 64-bit binary operation.
     */
    /* binop vAA, vBB, vCC */

    movzbl    2(rPC),%eax               # eax<- BB
    movzbl    3(rPC),%ecx               # ecx<- CC
    GET_VREG_WORD %edx %eax 0           # edx<- v[BB+0]
    GET_VREG_WORD %eax %eax 1           # eax<- v[BB+1]
    $instr1         # ex: addl   (rFP,%ecx,4),%edx
    $instr2         # ex: adcl   4(rFP,%ecx,4),%eax
    SET_VREG_WORD %edx rINST 0          # v[AA+0] <- edx
    FETCH_INST_OPCODE 2 %edx
    SET_VREG_WORD %eax rINST 1          # v[AA+1] <- eax
    ADVANCE_PC 2
    GOTO_NEXT_R %edx
