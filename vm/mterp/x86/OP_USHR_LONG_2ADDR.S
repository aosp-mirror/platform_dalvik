%verify "executed"
    /*
     * Long integer shift, 2addr version.  vA is 64-bit value/result, vB is
     * 32-bit shift distance.
     */
    /* shl-long/2addr vA, vB */
    /* ecx gets shift count */
    /* Need to spill edx */
    /* rINST gets AA */
    movzbl    rINST_HI,%ecx             # ecx<- BA
    movzbl    rINST_HI,rINST_FULL       # rINST_HI<- BA
    andb      $$0xf,rINST_LO            # rINST_FULL<- A
    GET_VREG_WORD(%eax,rINST_FULL,0)    # eax<- v[AA+0]
    sarl      $$4,%ecx                  # ecx<- B
    SPILL(rPC)
    GET_VREG_WORD(%edx,rINST_FULL,1)    # edx<- v[AA+1]
    GET_VREG(%ecx,%ecx)                 # ecx<- vBB
    shrdl     %edx,%eax
    shrl      %cl,%edx
    testb     $$32,%cl
    je        2f
    movl      %edx,%eax
    xorl      %edx,%edx
2:
    SET_VREG_WORD(%edx,rINST_FULL,1)   # v[AA+1]<- edx
    UNSPILL(rPC)
    jmp       .L${opcode}_finish
%break


.L${opcode}_finish:
    SET_VREG_WORD(%eax,rINST_FULL,0)   # v[AA+0]<- eax
    FETCH_INST_WORD(1)
    ADVANCE_PC(1)
    GOTO_NEXT
