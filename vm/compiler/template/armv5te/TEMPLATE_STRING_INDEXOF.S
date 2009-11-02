    /*
     * String's indexOf.
     *
     * Requires r0 to have been previously checked for null.  Will
     * return index of match of r1 in r0.
     *
     * IMPORTANT NOTE:
     *
     * This code relies on hard-coded offsets for string objects, and must be
     * kept in sync wth definitions in UtfString.h  See asm-constants.h
     *
     * On entry:
     *    r0:   string object pointer
     *    r1:   char to match
     *    r2:   Starting offset in string data
     */

    ldr    r7, [r0, #STRING_FIELDOFF_OFFSET]
    ldr    r8, [r0, #STRING_FIELDOFF_COUNT]
    ldr    r0, [r0, #STRING_FIELDOFF_VALUE]

    /*
     * At this point, we have:
     *    value:  r0
     *    offset: r7
     *    count:  r8
     */

     /* Clamp start to [0..count] */
     cmp   r2, #0
     movlt r2, #0
     cmp   r2, r8
     movgt r2, r0

     /* Fold start & offset, and set data pointer to contents[-1] */
     add   r2, r7
     add   r0, r0, r2, lsl #1
     add   r0, #16-2   @ offset to contents[-1]
     add   r2, r0, #2  @ remember true start of data

     /*
      * At this point we have:
      *   r0: *next[-1] char to test
      *   r2: *start
      *   r1: char to compare
      *   r8: max count
      *   r3, r4, r7, r9, r12 available for loading string data
      */

    /* Unroll x 4 */

    cmp   r8, #4
    blt   do_rest
loopback_quad:
    ldrh  r3, [r0, #2]!
    ldrh  r4, [r0, #2]!
    ldrh  r7, [r0, #2]!
    ldrh  r9, [r0, #2]!
    cmp   r3, r1
    beq   match_0
    cmp   r4, r1
    beq   match_1
    cmp   r7, r1
    beq   match_2
    cmp   r9, r1
    beq   match_3
    subs  r8, #4
    bgt   loopback_quad

do_rest:
    cmp   r8, #0
    beq   no_match

loopback_indexof:
    ldrh  r3, [r0, #2]!
    cmp   r3, r1
    beq   match_3
    subs  r8, #1
    bne     loopback_indexof

no_match:
    mov   r0, #-1
    bx    lr

match_0:
    sub   r0, #6
    sub   r0, r2
    bx    lr
match_1:
    sub   r0, #4
    sub   r0, r2
    bx    lr
match_2:
    sub   r0, #2
    sub   r0, r2
    bx    lr
match_3:
    sub   r0, r2
    bx    lr

