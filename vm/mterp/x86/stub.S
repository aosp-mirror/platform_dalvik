    /* (stub) */
    SAVE_PC_FP_TO_GLUE %ecx          # leaves rGLUE in %ecx
    movl %ecx,OUT_ARG0(%esp)         # glue is first arg to function
    call      dvmMterp_${opcode}     # do the real work
    mov       rGLUE,%ecx
    LOAD_PC_FP_FROM_GLUE             # retrieve updated values
    FETCH_INST
    GOTO_NEXT
