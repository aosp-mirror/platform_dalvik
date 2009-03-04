%verify "executed"
    /* unop vA, vB */
    movzbl    rINST_HI,%ecx            # ecx<- BA
    sarl      $$4,%ecx                 # ecx<- B
    movzbl    rINST_HI,rINST_FULL      # ecx<- BA
    andb      $$0xf,rINST_LO           # rINST_FULL<- A
    GET_VREG_WORD(%eax,%ecx,0)         # eax<- v[B+0]
    GET_VREG_WORD(%ecx,%ecx,1)         # ecx<- v[B+1]
    notl      %eax
    notl      %ecx
    SET_VREG_WORD(%eax,rINST_FULL,0)   # v[A+0]<- eax
    SET_VREG_WORD(%ecx,rINST_FULL,1)   # v[A+1]<- ecx
    FETCH_INST_WORD(1)
    ADVANCE_PC(1)
    GOTO_NEXT
