%verify "executed"
    /* for: move-result, move-result-object */
    /* op vAA */
    GET_GLUE(%eax)                         # eax<- rGLUE
    movzx    rINST_HI,%ecx                 # ecx<- AA
    movl     offGlue_retval(%eax),%eax     # eax<- glue->retval.l
    FETCH_INST_WORD(1)
    ADVANCE_PC(1)
    SET_VREG (%eax,%ecx)                   # fp[AA]<- retval.l
    GOTO_NEXT
