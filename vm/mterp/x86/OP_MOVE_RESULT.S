%verify "executed"
    /* for: move-result, move-result-object */
    /* op vAA */
    movl     rSELF,%eax                    # eax<- rSELF
    movzx    rINSTbl,%ecx                  # ecx<- AA
    movl     offThread_retval(%eax),%eax     # eax<- self->retval.l
    FETCH_INST_OPCODE 1 %edx
    ADVANCE_PC 1
    SET_VREG  %eax %ecx                    # fp[AA]<- retval.l
    GOTO_NEXT_R %edx
