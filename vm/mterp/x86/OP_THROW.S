%verify "executed"
%verify "exception for null object"
    /*
     * Throw an exception object in the current thread.
     */
    /* throw vAA */
    movl     rGLUE,%ecx
    EXPORT_PC
    GET_VREG_R %eax rINST              # eax<- exception object
    movl     offGlue_self(%ecx),%ecx   # ecx<- glue->self
    testl    %eax,%eax                 # null object?
    je       common_errNullObject
    movl     %eax,offThread_exception(%ecx) # thread->exception<- obj
    jmp      common_exceptionThrown
