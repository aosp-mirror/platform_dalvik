%verify "executed"
    /*
     * Array get, 64 bits.  vAA <- vBB[vCC].
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
    movl      (%eax),%ecx
    movl      4(%eax),%eax
    SET_VREG_WORD(%ecx,rINST_FULL,0)
    SET_VREG_WORD(%eax,rINST_FULL,1)
    FETCH_INST_WORD(2)
    ADVANCE_PC(2)
    GOTO_NEXT
