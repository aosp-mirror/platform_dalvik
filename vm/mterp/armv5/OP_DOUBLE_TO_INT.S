%verify "executed"
/* EABI appears to have Java-style conversions of +inf/-inf/NaN */
%include "armv5/unopNarrower.S" {"instr":"bl      __aeabi_d2iz"}

#if 0
@include "armv5/unopNarrower.S" {"instr":"bl      d2i_doconv"}
@break
/*
 * Convert the double in r0/r1 to an int in r0.
 *
 * We have to clip values to int min/max per the specification.  The
 * expected common case is a "reasonable" value that converts directly
 * to modest integer.  The EABI convert function isn't doing this for us.
 */
d2i_doconv:
    stmfd   sp!, {r4, r5, lr}           @ save regs
    ldr     r2, .L${opcode}_maxlo       @ (double)maxint, lo
    ldr     r3, .L${opcode}_maxhi       @ (double)maxint, hi
    sub     sp, sp, #4                  @ align for EABI
    mov     r4, r0                      @ save r0
    mov     r5, r1                      @  and r1
    bl      __aeabi_dcmpge              @ is arg >= maxint?
    cmp     r0, #0                      @ nonzero == yes
    mvnne   r0, #0x80000000             @ return maxint (7fffffff)
    bne     1f

    mov     r0, r4                      @ recover arg
    mov     r1, r5
    ldr     r3, .L${opcode}_min         @ (double)minint, hi
    mov     r2, #0                      @ (double)minint, lo
    bl      __aeabi_dcmple              @ is arg <= minint?
    cmp     r0, #0                      @ nonzero == yes
    movne   r0, #0x80000000             @ return minint (80000000)
    bne     1f

    mov     r0, r4                      @ recover arg
    mov     r1, r5
    mov     r2, r4                      @ compare against self
    mov     r3, r5
    bl      __aeabi_dcmpeq              @ is arg == self?
    cmp     r0, #0                      @ zero == no
    beq     1f                          @ return zero for NaN

    mov     r0, r4                      @ recover arg
    mov     r1, r5
    bl      __aeabi_d2iz                @ convert double to int

1:
    add     sp, sp, #4
    ldmfd   sp!, {r4, r5, pc}

.L${opcode}_maxlo:
    .word   0xffc00000                  @ maxint, as a double (low word)
.L${opcode}_maxhi:
    .word   0x41dfffff                  @ maxint, as a double (high word)
.L${opcode}_min:
    .word   0xc1e00000                  @ minint, as a double (high word)
#endif

