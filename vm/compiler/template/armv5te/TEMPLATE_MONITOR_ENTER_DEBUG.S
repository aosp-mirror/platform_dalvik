    /*
     * To support deadlock prediction, this version of MONITOR_ENTER
     * will always call the heavyweight dvmLockObject, check for an
     * exception and then bail out to the interpreter.
     *
     * On entry:
     *    r0 - self pointer
     *    r1 - the object (which has already been null-checked by the caller
     *    r4 - the Dalvik PC of the following instruction.
     *
     */
    ldr     r2, .LdvmLockObject
    mov     r3, #0                       @ Record that we're not returning
    str     r3, [r0, #offThread_inJitCodeCache]
    blx     r2             @ dvmLockObject(self, obj)
    @ refresh Jit's on/off status & test for exception
    ldr     r0, [rGLUE, #offGlue_ppJitProfTable]
    ldr     r1, [rGLUE, #offGlue_self]
    ldr     r0, [r0]
    ldr     r1, [r1, #offThread_exception]
    str     r0, [rGLUE, #offGlue_pJitProfTable]
    cmp     r1, #0
    beq     1f
    ldr     r2, .LhandleException
    sub     r0, r4, #2     @ roll dPC back to this monitor instruction
    bx      r2
1:
    @ Bail to interpreter - no chain [note - r4 still contains rPC]
#if defined(WITH_JIT_TUNING)
    mov     r0, #kHeavyweightMonitor
#endif
    ldr     pc, .LdvmJitToInterpNoChain
