%verify "executed"
%verify "exception for null object"
    /*
     * Throw an exception object in the current thread.
     */
    /* throw vAA */
    GET_GLUE(%ecx)
    EXPORT_PC()
    movzbl   rINST_HI,rINST_FULL       # rINST_FULL<- AA
    GET_VREG(%eax,rINST_FULL)          # eax<- exception object
    movl     offGlue_self(%ecx),%ecx   # ecx<- glue->self
    testl    %eax,%eax                 # null object?
    je       common_errNullObject
    movl     %eax,offThread_exception(%ecx) # thread->exception<- obj
    jmp      common_exceptionThrown
