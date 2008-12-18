%verify "executed"
    /*
     * Return a 64-bit value.  Copies the return value into the "glue"
     * structure, then jumps to the return handler.
     */
    /* return-wide vAA */
    GET_GLUE(%ecx)
    movzbl  rINST_HI,rINST_FULL            # rINST_FULL<- AA
    GET_VREG_WORD(%eax,rINST_FULL,0)       # eax<- v[AA+0]
    GET_VREG_WORD(rINST_FULL,rINST_FULL,1) # rINST_FULL<- v[AA+1]
    movl    %eax,offGlue_retval(%ecx)
    movl    rINST_FULL,4+offGlue_retval(%ecx)
    jmp     common_returnFromMethod
