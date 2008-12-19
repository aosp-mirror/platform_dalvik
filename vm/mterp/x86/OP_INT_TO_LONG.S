%verify "executed"
    /* int to long vA, vB */
    movzbl  rINST_HI,%ecx               # ecx<- +A
    sarl    $$12,rINST_FULL             # rINST_FULL<- B
    GET_VREG(%eax,rINST_FULL)           # eax<- vB
    SPILL(rPC)                          # will step on edx later
    andb    $$0xf,%cl                   # ecx<- A
    cltd                                # edx:eax<- sssssssBBBBBBBB
    SET_VREG_WORD(%edx,%ecx,1)          # v[A+1]<- edx/rPC
    UNSPILL(rPC)
    SET_VREG_WORD(%eax,%ecx,0)          # v[A+0]<- %eax
    FETCH_INST_WORD(1)
    ADVANCE_PC(1)
    GOTO_NEXT
