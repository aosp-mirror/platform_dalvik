/*
 * ===========================================================================
 *  CPU-version-specific defines and utility
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

/*
 * Save & restore for callee-save FP registers.
 * On entry:
 *    r0 : pointer to save area of JIT_CALLEE_SAVE_WORD_SIZE
 */
    .text
    .align 2
    .global dvmJitCalleeSave
    .type dvmJitCalleeSave, %function
dvmJitCalleeSave:
    vstmia r0, {d8-d15}
    bx     lr

    .global dvmJitCalleeRestore
    .type dvmJitCalleeRestore, %function
dvmJitCalleeRestore:
    vldmia r0, {d8-d15}
    bx     lr

