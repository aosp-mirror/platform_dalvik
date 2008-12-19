    /* (stub) */
    GET_GLUE(%ecx)
    SAVE_PC_TO_GLUE(%ecx)            # only need to export these two
    SAVE_FP_TO_GLUE(%ecx)            # only need to export these two
    movl %ecx,OUT_ARG0(%esp)         # glue is first arg to function
    call      dvmMterp_${opcode}     # do the real work
    GET_GLUE(%ecx)
    LOAD_PC_FROM_GLUE(%ecx)          # retrieve updated values
    LOAD_FP_FROM_GLUE(%ecx)          # retrieve updated values
    FETCH_INST()
    GOTO_NEXT
