    /*
     * This handler transfers control to the interpeter without performing
     * any lookups.  It may be called either as part of a normal chaining
     * operation, or from the transition code in header.S.  We distinquish
     * the two cases by looking at the link register.  If called from a
     * translation chain, it will point to the chaining Dalvik PC -3.
     * On entry:
     *    lr - if NULL:
     *        r1 - the Dalvik PC to begin interpretation.
     *    else
     *        [lr, #3] contains Dalvik PC to begin interpretation
     *    rGLUE - pointer to interpState
     *    rFP - Dalvik frame pointer
     */
    cmp     lr, #0
    ldrne   r1,[lr, #3]
    ldr     r2, .LinterpPunt
    mov     r0, r1                       @ set Dalvik PC
    bx      r2
    @ doesn't return

.LinterpPunt:
    .word   dvmJitToInterpPunt
