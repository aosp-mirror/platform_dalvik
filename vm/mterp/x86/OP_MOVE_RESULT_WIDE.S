%verify "executed"
    /* move-result-wide vAA */
    GET_GLUE(%ecx)
    movzbl  rINST_HI,rINST_FULL         # rINST_FULL<- AA
    movl    offGlue_retval(%ecx),%eax
    movl    4+offGlue_retval(%ecx),%ecx
    SET_VREG_WORD(%eax,rINST_FULL,0)    # v[AA+0] <- eax
    SET_VREG_WORD(%ecx,rINST_FULL,1)    # v[AA+1] <- ecx
    FETCH_INST_WORD(1)
    ADVANCE_PC(1)
    GOTO_NEXT
