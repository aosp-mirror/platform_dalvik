%verify "executed"
    /* mul vA, vB */
    movzx   rINST_HI,%ecx               # ecx<- A+
    sarl    $$12,rINST_FULL             # rINST_FULL<- B
    GET_VREG(%eax,rINST_FULL)           # eax<- vB
    andb    $$0xf,%cl                   # ecx<- A
    SPILL(rPC)
    imull   (rFP,%ecx,4),%eax
    UNSPILL(rPC)
    SET_VREG(%eax,%ecx)
    FETCH_INST_WORD(1)
    ADVANCE_PC(1)
    GOTO_NEXT
