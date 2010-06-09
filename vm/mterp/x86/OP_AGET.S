%default { "load":"movl", "shift":"4" }
%verify "executed"
    /*
     * Array get, 32 bits or less.  vAA <- vBB[vCC].
     *
     * for: aget, aget-object, aget-boolean, aget-byte, aget-char, aget-short
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
    jae       common_errArrayIndex      # index >= length, bail
    $load     offArrayObject_contents(%eax,%ecx,$shift),%eax
    movl      rINST_FULL,%ecx
    FETCH_INST_WORD(2)
    SET_VREG(%eax,%ecx)
    ADVANCE_PC(2)
    GOTO_NEXT
