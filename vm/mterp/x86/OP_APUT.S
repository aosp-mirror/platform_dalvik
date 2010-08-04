%default { "reg":"%ecx", "store":"movl", "shift":"4" }
%verify "executed"
    /*
     * Array put, 32 bits or less.  vBB[vCC] <- vAA
     *
     * for: aput, aput-object, aput-boolean, aput-byte, aput-char, aput-short
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
    leal      offArrayObject_contents(%eax,%ecx,$shift),%eax
    GET_VREG(%ecx,rINST_FULL)
    FETCH_INST_WORD(2)
    $store     $reg,(%eax)
    ADVANCE_PC(2)
    GOTO_NEXT
