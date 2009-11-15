/*
 * ===========================================================================
 *  CPU-version-specific defines
 * ===========================================================================
 */

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
