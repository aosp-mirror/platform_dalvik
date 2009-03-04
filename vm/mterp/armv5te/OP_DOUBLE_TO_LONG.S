%verify "executed"
@include "armv5te/unopWide.S" {"instr":"bl      __aeabi_d2lz"}
%include "armv5te/unopWide.S" {"instr":"bl      d2l_doconv"}

%break
/*
 * Convert the double in r0/r1 to a long in r0/r1.
 *
 * We have to clip values to long min/max per the specification.  The
 * expected common case is a "reasonable" value that converts directly
 * to modest integer.  The EABI convert function isn't doing this for us.
 */
d2l_doconv:
    stmfd   sp!, {r4, r5, lr}           @ save regs
    ldr     r3, .L${opcode}_max         @ (double)maxlong, hi
    sub     sp, sp, #4                  @ align for EABI
    mov     r2, #0                      @ (double)maxlong, lo
    mov     r4, r0                      @ save r0
    mov     r5, r1                      @  and r1
    bl      __aeabi_dcmpge              @ is arg >= maxlong?
    cmp     r0, #0                      @ nonzero == yes
    mvnne   r0, #0                      @ return maxlong (7fffffffffffffff)
    mvnne   r1, #0x80000000
    bne     1f

    mov     r0, r4                      @ recover arg
    mov     r1, r5
    ldr     r3, .L${opcode}_min         @ (double)minlong, hi
    mov     r2, #0                      @ (double)minlong, lo
    bl      __aeabi_dcmple              @ is arg <= minlong?
    cmp     r0, #0                      @ nonzero == yes
    movne   r0, #0                      @ return minlong (8000000000000000)
    movne   r1, #0x80000000
    bne     1f

    mov     r0, r4                      @ recover arg
    mov     r1, r5
    mov     r2, r4                      @ compare against self
    mov     r3, r5
    bl      __aeabi_dcmpeq              @ is arg == self?
    cmp     r0, #0                      @ zero == no
    moveq   r1, #0                      @ return zero for NaN
    beq     1f

    mov     r0, r4                      @ recover arg
    mov     r1, r5
    bl      __aeabi_d2lz                @ convert double to long

1:
    add     sp, sp, #4
    ldmfd   sp!, {r4, r5, pc}

.L${opcode}_max:
    .word   0x43e00000                  @ maxlong, as a double (high word)
.L${opcode}_min:
    .word   0xc3e00000                  @ minlong, as a double (high word)

