%verify "executed"
%verify "exception for null object"
    /*
     * Synchronize on an object.
     */
    /* monitor-enter vAA */
    mov     r2, rINST, lsr #8           @ r2<- AA
    GET_VREG(r1, r2)                    @ r1<- vAA (object)
    ldr     r0, [rGLUE, #offGlue_self]  @ r0<- glue->self
    cmp     r1, #0                      @ null object?
    EXPORT_PC()                         @ need for precise GC, MONITOR_TRACKING
    beq     common_errNullObject        @ null object, throw an exception
    FETCH_ADVANCE_INST(1)               @ advance rPC, load rINST
    bl      dvmLockObject               @ call(self, obj)
#ifdef WITH_DEADLOCK_PREDICTION /* implies WITH_MONITOR_TRACKING */
    ldr     r0, [rGLUE, #offGlue_self]  @ r0<- glue->self
    ldr     r1, [r0, #offThread_exception] @ check for exception
    cmp     r1, #0
    bne     common_exceptionThrown      @ exception raised, bail out
#endif
    GET_INST_OPCODE(ip)                 @ extract opcode from rINST
    GOTO_OPCODE(ip)                     @ jump to next instruction
