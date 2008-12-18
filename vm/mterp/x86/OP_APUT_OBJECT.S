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
    GET_VREG(rINST_FULL,rINST_FULL)     # rINST_FULL<- vAA
    testl     %eax,%eax                 # null array object?
    je        common_errNullObject      # bail if so
    cmpl      offArrayObject_length(%eax),%ecx
    jb        .L${opcode}_continue
    jmp       common_errArrayIndex      # index >= length, bail
%break

    /* On entry:
     *   eax<- array object
     *   ecx<- index
     *   rINST_FULL<- vAA
     */
.L${opcode}_continue:
    leal      offArrayObject_contents(%eax,%ecx,4),%ecx
    testl     rINST_FULL,rINST_FULL     # storing null reference?
    je        .L${opcode}_skip_check
    SPILL(rPC)
    SPILL_TMP(%ecx)
    movl      offObject_clazz(%eax),%eax # eax<- arrayObj->clazz
    movl      offObject_clazz(rINST_FULL),%ecx # ecx<- obj->clazz
    movl      %eax,OUT_ARG1(%esp)
    movl      %ecx,OUT_ARG0(%esp)
    call      dvmCanPutArrayElement     # test object type vs. array type
    UNSPILL(rPC)
    UNSPILL_TMP(%ecx)
    testl     %eax,%eax
    je        common_errArrayStore

.L${opcode}_skip_check:
    movl      rINST_FULL,(%ecx)
    FETCH_INST_WORD(2)
    ADVANCE_PC(2)
    GOTO_NEXT

