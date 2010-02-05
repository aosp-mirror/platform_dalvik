    /*
     * thumb2 specific.
     *
     * In this variant of the MONITOR_ENTER handler, we assume that
     * the test for the simple thin lock case has already been done.
     * So, we'll just call dvmLockObject(), refresh the
     * jit's on/off switch on return and then bail out to the interpreter.
     * We have to bail to the interpreter because the translation cache
     * may have been cleared while we were blocked on a monitor in
     * dvmLockObject.
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
    blx     r2                           @ dvmLockObject(self, obj)
    @ refresh Jit's on/off status
    ldr     r0, [rGLUE, #offGlue_ppJitProfTable]
    ldr     r0, [r0]
    ldr     r2, .LdvmJitToInterpNoChain
    str     r0, [rGLUE, #offGlue_pJitProfTable]
    @ Bail to interpreter - no chain [note - r4 still contains rPC]
#if defined(EXIT_STATS)
    mov     r0, #kHeavyweightMonitor
#endif
    bx      r2

