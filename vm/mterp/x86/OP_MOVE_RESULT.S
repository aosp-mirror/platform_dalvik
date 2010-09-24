%verify "executed"
    /* for: move-result, move-result-object */
    /* op vAA */
    movl     rGLUE,%eax                    # eax<- rGLUE
    movzx    rINSTbl,%ecx                  # ecx<- AA
    movl     offGlue_retval(%eax),%eax     # eax<- glue->retval.l
    FETCH_INST_OPCODE 1 %edx
    ADVANCE_PC 1
    SET_VREG  %eax %ecx                    # fp[AA]<- retval.l
    GOTO_NEXT_R %edx
