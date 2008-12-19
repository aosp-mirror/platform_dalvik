%verify "executed"
    /*
     * Return a 32-bit value.  Copies the return value into the "glue"
     * structure, then jumps to the return handler.
     *
     * for: return, return-object
     */
    /* op vAA */
    GET_GLUE(%ecx)
    movzbl  rINST_HI,rINST_FULL         # rINST_FULL<- AA
    GET_VREG(%eax,rINST_FULL)           # eax<- vAA
    movl    %eax,offGlue_retval(%ecx)   # retval.i <- AA
    jmp     common_returnFromMethod
