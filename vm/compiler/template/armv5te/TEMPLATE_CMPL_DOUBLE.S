%default { "naninst":"mvn     r0, #0" }
    /*
     * For the JIT: incoming arguments are pointers to the arguments in r0/r1
     *              result in r0
     *
     * Compare two floating-point values.  Puts 0, 1, or -1 into the
     * destination register based on the results of the comparison.
     *
     * Provide a "naninst" instruction that puts 1 or -1 into r1 depending
     * on what value we'd like to return when one of the operands is NaN.
     *
     * See OP_CMPL_FLOAT for an explanation.
     *
     * For: cmpl-double, cmpg-double
     */
    /* op vAA, vBB, vCC */
    mov     r4, lr                      @ save return address
    mov     r9, r0                      @ save copy of &arg1
    mov     r10, r1                     @ save copy of &arg2
    ldmia   r9, {r0-r1}                 @ r0/r1<- vBB/vBB+1
    ldmia   r10, {r2-r3}                @ r2/r3<- vCC/vCC+1
    LDR_PC_LR ".L__aeabi_cdcmple"       @ PIC way of "bl __aeabi_cdcmple"
    bhi     .L${opcode}_gt_or_nan       @ C set and Z clear, disambiguate
    mvncc   r0, #0                      @ (less than) r1<- -1
    moveq   r0, #0                      @ (equal) r1<- 0, trumps less than
    bx      r4

    @ Test for NaN with a second comparison.  EABI forbids testing bit
    @ patterns, and we can't represent 0x7fc00000 in immediate form, so
    @ make the library call.
.L${opcode}_gt_or_nan:
    ldmia   r10, {r0-r1}                @ reverse order
    ldmia   r9, {r2-r3}
    LDR_PC_LR ".L__aeabi_cdcmple"       @ r0<- Z set if eq, C clear if <
    movcc   r0, #1                      @ (greater than) r1<- 1
    bxcc    r4
    $naninst                            @ r1<- 1 or -1 for NaN
    bx      r4

