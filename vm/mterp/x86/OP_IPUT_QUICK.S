%verify "executed"
%verify "null object"
    /* For: iput-quick */
    /* op vA, vB, offset@CCCC */
    movzbl    rINST_HI,%ecx             # ecx<- BA
    sarl      $$4,%ecx                  # ecx<- B
    GET_VREG(%ecx,%ecx)                 # vB (object we're operating on)
    movzbl    rINST_HI,rINST_FULL
    andb      $$0xf,rINST_LO            # rINST_FULL<- A
    GET_VREG(rINST_FULL,rINST_FULL)     # rINST_FULL<- v[A]
    movzwl    2(rPC),%eax               # eax<- field byte offset
    testl     %ecx,%ecx                  # is object null?
    je        common_errNullObject
    movl      rINST_FULL,(%ecx,%eax,1)
    FETCH_INST_WORD(2)
    ADVANCE_PC(2)
    GOTO_NEXT
