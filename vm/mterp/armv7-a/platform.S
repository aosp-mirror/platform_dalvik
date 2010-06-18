/*
 * ===========================================================================
 *  CPU-version-specific defines
 * ===========================================================================
 */

/*
 * Macro for "LDR PC,xxx", which is not allowed pre-ARMv5.  Essentially a
 * one-way branch.
 *
 * May modify IP.  Does not modify LR.
 */
.macro  LDR_PC source
    ldr     pc, \source
.endm

/*
 * Macro for "MOV LR,PC / LDR PC,xxx", which is not allowed pre-ARMv5.
 * Jump to subroutine.
 *
 * May modify IP and LR.
 */
.macro  LDR_PC_LR source
    mov     lr, pc
    ldr     pc, \source
.endm

/*
 * Macro for "LDMFD SP!, {...regs...,PC}".
 *
 * May modify IP and LR.
 */
.macro  LDMFD_PC regs
    ldmfd   sp!, {\regs,pc}
.endm

#if !defined(ANDROID_SMP)
# error "Must define ANDROID_SMP"
#endif

/*
 * Macro for data memory barrier; not meaningful pre-ARMv6K.
 * If the argument is nonzero, emit barrier; otherwise, emit nothing.
 */
.macro  SMP_DMB
#if ANDROID_SMP != 0
    dmb
#else
    /* not SMP */
#endif
.endm
