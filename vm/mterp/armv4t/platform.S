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
    ldr     ip, \source
    bx      ip
.endm

/*
 * Macro for "MOV LR,PC / LDR PC,xxx", which is not allowed pre-ARMv5.
 * Jump to subroutine.
 *
 * May modify IP and LR.
 */
.macro  LDR_PC_LR source
    ldr     ip, \source
    mov     lr, pc
    bx      ip
.endm

/*
 * Macro for "LDMFD SP!,{...regs...,PC}".
 *
 * May modify IP and LR.
 */
.macro  LDMFD_PC regs
    ldmfd   sp!, {\regs,lr}
    bx      lr
.endm

/*
 * Macro for data memory barrier; not meaningful pre-ARMv6K.
 */
.macro  SMP_DMB
.endm
