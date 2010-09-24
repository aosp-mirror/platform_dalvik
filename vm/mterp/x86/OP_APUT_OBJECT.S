%verify "executed"
    /*
     * Array put, 32 bits or less.  vBB[vCC] <- vAA
     *
     * for: aput, aput-object, aput-boolean, aput-byte, aput-char, aput-short
     */
    /* op vAA, vBB, vCC */
    movzbl    2(rPC),%eax               # eax<- BB
    movzbl    3(rPC),%ecx               # ecx<- CC
    GET_VREG_R  %eax %eax               # eax<- vBB (array object)
    GET_VREG_R  %ecx %ecx               # ecs<- vCC (requested index)
    GET_VREG_R  rINST rINST             # rINST<- vAA
    testl     %eax,%eax                 # null array object?
    je        common_errNullObject      # bail if so
    cmpl      offArrayObject_length(%eax),%ecx
    jb        .L${opcode}_continue
    jmp       common_errArrayIndex      # index >= length, bail
%break

    /* On entry:
     *   eax<- array object
     *   ecx<- index
     *   rINST<- vAA
     */
.L${opcode}_continue:
    leal      offArrayObject_contents(%eax,%ecx,4),%ecx
    testl     rINST,rINST     # storing null reference?
    je        .L${opcode}_skip_check
    SPILL_TMP1(%ecx)
    movl      offObject_clazz(%eax),%eax # eax<- arrayObj->clazz
    movl      offObject_clazz(rINST),%ecx # ecx<- obj->clazz
    movl      %eax,OUT_ARG1(%esp)
    movl      %ecx,OUT_ARG0(%esp)
    call      dvmCanPutArrayElement     # test object type vs. array type
    UNSPILL_TMP1(%ecx)
    testl     %eax,%eax
    movl      rGLUE,%eax
    je        common_errArrayStore
    movl      offGlue_cardTable(%eax),%eax   # get card table base
    movl      rINST,(%ecx)
    FETCH_INST_OPCODE 2 %edx
    shrl      $$GC_CARD_SHIFT,%ecx           # convert addr to card number
    movb      %al,(%eax,%ecx)                # mark card
    ADVANCE_PC 2
    GOTO_NEXT_R %edx

.L${opcode}_skip_check:
    movl      rINST,(%ecx)
    FETCH_INST_OPCODE 2 %edx
    ADVANCE_PC 2
    GOTO_NEXT_R %edx
