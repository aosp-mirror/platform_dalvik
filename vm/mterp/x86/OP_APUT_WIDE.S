%verify "executed"
    /*
     * Array put, 64 bits.  vBB[vCC]<-vAA.
     *
     */
    /* op vAA, vBB, vCC */
    movzbl    2(rPC),%eax               # eax<- BB
    movzbl    3(rPC),%ecx               # ecx<- CC
    GET_VREG_R  %eax %eax               # eax<- vBB (array object)
    GET_VREG_R  %ecx %ecx               # ecs<- vCC (requested index)
    testl     %eax,%eax                 # null array object?
    je        common_errNullObject      # bail if so
    cmpl      offArrayObject_length(%eax),%ecx
    jb        .L${opcode}_finish        # index < length, OK
    jmp       common_errArrayIndex      # index >= length, bail
%break

.L${opcode}_finish:
    leal      offArrayObject_contents(%eax,%ecx,8),%eax
    GET_VREG_WORD %ecx rINST 0
    GET_VREG_WORD rINST rINST 1
    movl      rINST,4(%eax)
    FETCH_INST_OPCODE 2 %edx
    movl      %ecx,(%eax)
    ADVANCE_PC 2
    GOTO_NEXT_R %edx
