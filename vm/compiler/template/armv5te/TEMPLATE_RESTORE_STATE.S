    /*
     * This handler restores state following a selfVerification memory access.
     * On entry:
     *    r0 - offset from rGLUE to the 1st element of the coreRegs save array.
     */
    add     r0, r0, rGLUE               @ pointer to heapArgSpace.coreRegs[0]
    ldmia   r0, {r0-r12}
    bx      lr
