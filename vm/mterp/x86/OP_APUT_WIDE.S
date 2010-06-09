%verify "executed"
    /*
     * Array put, 64 bits.  vBB[vCC]<-vAA.
     *
     */
    /* op vAA, vBB, vCC */
    movzbl    2(rPC),%eax               # eax<- BB
    movzbl    3(rPC),%ecx               # ecx<- CC
    movzbl    rINST_HI,rINST_FULL       # rINST_FULL<- AA
    GET_VREG(%eax,%eax)                 # eax<- vBB (array object)
    GET_VREG(%ecx,%ecx)                 # ecs<- vCC (requested index)
    testl     %eax,%eax                 # null array object?
    je        common_errNullObject      # bail if so
    cmpl      offArrayObject_length(%eax),%ecx
    jb        .L${opcode}_finish        # index < length, OK
    jmp       common_errArrayIndex      # index >= length, bail
%break

.L${opcode}_finish:
    leal      offArrayObject_contents(%eax,%ecx,8),%eax
    GET_VREG_WORD(%ecx,rINST_FULL,0)
    GET_VREG_WORD(rINST_FULL,rINST_FULL,1)
    movl      rINST_FULL,4(%eax)
    FETCH_INST_WORD(2)
    movl      %ecx,(%eax)
    ADVANCE_PC(2)
    GOTO_NEXT
