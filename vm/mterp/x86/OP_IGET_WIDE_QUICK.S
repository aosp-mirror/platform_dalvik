%verify "executed"
%verify "null object"
    /* For: iget-wide-quick */
    /* op vA, vB, offset@CCCC */
    movzbl    rINST_HI,%ecx             # ecx<- BA
    sarl      $$4,%ecx                  # ecx<- B
    GET_VREG(%ecx,%ecx)                 # vB (object we're operating on)
    movzwl    2(rPC),%eax               # eax<- field byte offset
    cmpl      $$0,%ecx                  # is object null?
    je        common_errNullObject
    leal      (%ecx,%eax,1),%eax        # eax<- address of 64-bit source
    movl      (%eax),%ecx               # ecx<- lsw
    movl      4(%eax),%eax               # eax<- msw
    movzbl    rINST_HI,rINST_FULL
    andb      $$0xf,rINST_LO            # rINST_FULL<- A
    SET_VREG_WORD(%ecx,rINST_FULL,0)    # v[A+0]<- lsw
    SET_VREG_WORD(%eax,rINST_FULL,1)    # v[A+1]<- msw
    FETCH_INST_WORD(2)
    ADVANCE_PC(2)
    GOTO_NEXT
