%verify "executed"
%verify "null object"
    /* For: iput-wide-quick */
    /* op vA, vB, offset@CCCC */
    movzbl    rINST_HI,%ecx             # ecx<- BA
    sarl      $$4,%ecx                  # ecx<- B
    GET_VREG(%ecx,%ecx)                 # vB (object we're operating on)
    movzwl    2(rPC),%eax               # eax<- field byte offset
    testl      %ecx,%ecx                # is object null?
    je        common_errNullObject
    leal      (%ecx,%eax,1),%ecx        # ecx<- Address of 64-bit target
    movzbl    rINST_HI,rINST_FULL
    andb      $$0xf,rINST_LO            # rINST_FULL<- A
    GET_VREG_WORD(%eax,rINST_FULL,0)    # eax<- lsw
    GET_VREG_WORD(rINST_FULL,rINST_FULL,1) # rINST_FULL<- msw
    movl      %eax,(%ecx)
    movl      rINST_FULL,4(%ecx)
    FETCH_INST_WORD(2)
    ADVANCE_PC(2)
    GOTO_NEXT
