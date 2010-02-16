%verify "executed"
%verify "exception for null object (impossible in javac)"
%verify "dvmUnlockObject fails"
    /*
     * Unlock an object.
     *
     * Exceptions that occur when unlocking a monitor need to appear as
     * if they happened at the following instruction.  See the Dalvik
     * instruction spec.
     */
    /* monitor-exit vAA */
    movzbl  rINST_HI,rINST_FULL         # rINST_FULL<- AA
    GET_VREG(%eax,rINST_FULL)
    GET_GLUE(%ecx)
    EXPORT_PC()
    testl   %eax,%eax                   # null object?
    je      .L${opcode}_errNullObject   # go if so
    movl    offGlue_self(%ecx),%ecx     # ecx<- glue->self
    movl    %eax,OUT_ARG1(%esp)
    SPILL(rPC)
    movl    %ecx,OUT_ARG0(%esp)
    jmp     .L${opcode}_continue
%break

.L${opcode}_continue:
    call    dvmUnlockObject             # unlock(self,obj)
    UNSPILL(rPC)
    FETCH_INST_WORD(1)
    testl   %eax,%eax                   # success?
    ADVANCE_PC(1)
    je      common_exceptionThrown      # no, exception pending
    GOTO_NEXT
.L${opcode}_errNullObject:
    ADVANCE_PC(1)                       # advance before throw
    jmp     common_errNullObject
