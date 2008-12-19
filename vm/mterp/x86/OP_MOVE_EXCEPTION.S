%verify "executed"
    /* move-exception vAA */
    GET_GLUE(%ecx)
    movzbl  rINST_HI,rINST_FULL        # rINST_FULL<- AA
    movl    offGlue_self(%ecx),%ecx    # ecx<- glue->self
    movl    offThread_exception(%ecx),%eax # eax<- dvmGetException bypass
    SET_VREG(%eax,rINST_FULL)          # fp[AA]<- exception object
    FETCH_INST_WORD(1)
    ADVANCE_PC(1)
    movl    $$0,offThread_exception(%ecx) # dvmClearException bypass
    GOTO_NEXT
