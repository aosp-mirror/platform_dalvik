    /*
     * String's compareTo.
     *
     * Requires r0/r1 to have been previously checked for null.  Will
     * return negative if this's string is < comp, 0 if they are the
     * same and positive if >.
     *
     * IMPORTANT NOTE:
     *
     * This code relies on hard-coded offsets for string objects, and must be
     * kept in sync with definitions in UtfString.h.  See asm-constants.h
     *
     * On entry:
     *    r0:   this object pointer
     *    r1:   comp object pointer
     *
     */

    mov    r2, r0         @ this to r2, opening up r0 for return value
    subs   r0, r2, r1     @ Same?
    bxeq   lr

    ldr    r4, [r2, #STRING_FIELDOFF_OFFSET]
    ldr    r9, [r1, #STRING_FIELDOFF_OFFSET]
    ldr    r7, [r2, #STRING_FIELDOFF_COUNT]
    ldr    r10, [r1, #STRING_FIELDOFF_COUNT]
    ldr    r2, [r2, #STRING_FIELDOFF_VALUE]
    ldr    r1, [r1, #STRING_FIELDOFF_VALUE]

    /*
     * At this point, we have:
     *    value:  r2/r1
     *    offset: r4/r9
     *    count:  r7/r10
     * We're going to compute
     *    r11 <- countDiff
     *    r10 <- minCount
     */
     subs  r11, r7, r10
     movls r10, r7

     /* Now, build pointers to the string data */
     add   r2, r2, r4, lsl #1
     add   r1, r1, r9, lsl #1
     /*
      * Note: data pointers point to previous element so we can use pre-index
      * mode with base writeback.
      */
     add   r2, #16-2   @ offset to contents[-1]
     add   r1, #16-2   @ offset to contents[-1]

     /*
      * At this point we have:
      *   r2: *this string data
      *   r1: *comp string data
      *   r10: iteration count for comparison
      *   r11: value to return if the first part of the string is equal
      *   r0: reserved for result
      *   r3, r4, r7, r8, r9, r12 available for loading string data
      */

    sub   r10, #3
    blt   do_remainder
loopback_triple:
    ldrh  r3, [r2, #2]!
    ldrh  r4, [r1, #2]!
    ldrh  r7, [r2, #2]!
    ldrh  r8, [r1, #2]!
    ldrh  r9, [r2, #2]!
    ldrh  r12,[r1, #2]!
    subs  r0, r3, r4
    subeqs  r0, r7, r8
    subeqs  r0, r9, r12
    bxne  lr
    subs  r10, #3
    bge   loopback_triple

do_remainder:
    adds  r10, #3
    beq   returnDiff

loopback_single:
    ldrh  r3, [r2, #2]!
    ldrh  r4, [r1, #2]!
    subs  r0, r3, r4
    bxne  lr
    subs  r10, #1
    bne     loopback_single

returnDiff:
    mov   r0, r11
    bx    lr

