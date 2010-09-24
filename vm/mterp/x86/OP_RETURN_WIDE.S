%verify "executed"
    /*
     * Return a 64-bit value.  Copies the return value into the "glue"
     * structure, then jumps to the return handler.
     */
    /* return-wide vAA */
    movl    rGLUE,%ecx
    GET_VREG_WORD %eax rINST 0       # eax<- v[AA+0]
    GET_VREG_WORD rINST rINST 1      # rINST<- v[AA+1]
    movl    %eax,offGlue_retval(%ecx)
    movl    rINST,4+offGlue_retval(%ecx)
    jmp     common_returnFromMethod
