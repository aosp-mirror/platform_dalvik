%verify "executed"
%verify "exception for null object"
    /*
     * Synchronize on an object.
     */
    /* monitor-enter vAA */
    GET_GLUE(%ecx)
    movzbl  rINST_HI,rINST_FULL         # rINST_FULL<- AA
    GET_VREG(%eax,rINST_FULL)           # eax<- vAA
    movl    offGlue_self(%ecx),%ecx     # ecx<- glue->self
    FETCH_INST_WORD(1)
    testl   %eax,%eax                   # null object?
    EXPORT_PC()                         # need for precise GC, MONITOR_TRACKING
    jne     .L${opcode}_continue
    jmp     common_errNullObject
%break

.L${opcode}_continue:
    SPILL(rPC)                          # have to - caller save
    movl    %ecx,OUT_ARG0(%esp)
    movl    %eax,OUT_ARG1(%esp)
    call    dvmLockObject               # dvmLockObject(self,object)
    UNSPILL(rPC)
#ifdef WITH_DEADLOCK_PREDICTION
    GET_GLUE(%ecx)
    movl    offGlueSelf(%ecx),%ecx      # ecx<- glue->self
    movl    offThread_exception(%ecx),%eax
    testl   %eax,%eax
    jne     common_exceptionThrown
#endif
    ADVANCE_PC(1)
    GOTO_NEXT
