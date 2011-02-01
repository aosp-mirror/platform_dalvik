%verify "executed"
%verify "exception for null object"
    /*
     * Synchronize on an object.
     */
    /* monitor-enter vAA */
    movl    rGLUE,%ecx
    GET_VREG_R %eax rINST               # eax<- vAA
    movl    offGlue_self(%ecx),%ecx     # ecx<- glue->self
    FETCH_INST_WORD 1
    testl   %eax,%eax                   # null object?
    EXPORT_PC                           # need for precise GC
    jne     .L${opcode}_continue
    jmp     common_errNullObject
%break

.L${opcode}_continue:
    movl    %ecx,OUT_ARG0(%esp)
    movl    %eax,OUT_ARG1(%esp)
    call    dvmLockObject               # dvmLockObject(self,object)
    ADVANCE_PC 1
    GOTO_NEXT
