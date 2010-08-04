    /* (stub) */
    SAVE_PC_FP_TO_GLUE()            @ only need to export these two
    mov     r0, rGLUE               @ glue is first arg to function
    bl      dvmMterp_${opcode}      @ call
    LOAD_PC_FP_FROM_GLUE()          @ retrieve updated values
    FETCH_INST()                    @ load next instruction from rPC
    GET_INST_OPCODE(ip)             @ ...trim down to just the opcode
    GOTO_OPCODE(ip)                 @ ...and jump to the handler
