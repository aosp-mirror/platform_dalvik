%verify "executed"
    /* for move, move-object, long-to-int */
    /* op vA, vB */
    movzbl rINST_HI,%eax         # eax<- BA
    andb   $$0xf,%al             # eax<- A
    shrl   $$12,rINST_FULL       # rINST_FULL<- B
    GET_VREG(%ecx,rINST_FULL)
    FETCH_INST_WORD(1)
    ADVANCE_PC(1)
    SET_VREG(%ecx,%eax)          # fp[A]<-fp[B]
    GOTO_NEXT
