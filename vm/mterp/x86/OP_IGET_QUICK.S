%verify "executed"
%verify "null object"
    /* For: iget-quick, iget-object-quick */
    /* op vA, vB, offset@CCCC */
    movzbl    rINST_HI,%ecx             # ecx<- BA
    sarl      $$4,%ecx                  # ecx<- B
    GET_VREG(%ecx,%ecx)                 # vB (object we're operating on)
    movzwl    2(rPC),%eax               # eax<- field byte offset
    cmpl      $$0,%ecx                  # is object null?
    je        common_errNullObject
    movl      (%ecx,%eax,1),%eax
    movzbl    rINST_HI,%ecx
    FETCH_INST_WORD(2)
    ADVANCE_PC(2)
    andb      $$0xf,%cl                 # rINST_FULL<- A
    SET_VREG  (%eax,%ecx)               # fp[A]<- result
    GOTO_NEXT
