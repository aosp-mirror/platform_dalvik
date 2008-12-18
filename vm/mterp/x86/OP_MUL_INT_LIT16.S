%verify "executed"
    /* mul/lit16 vA, vB, #+CCCC */
    /* Need A in rINST_FULL, ssssCCCC in ecx, vB in eax */
    movzbl   rINST_HI,%eax              # eax<- 000000BA
    sarl     $$4,%eax                   # eax<- B
    GET_VREG(%eax,%eax)                 # eax<- vB
    movswl   2(rPC),%ecx                # ecx<- ssssCCCC
    SPILL(rPC)
    movzbl   rINST_HI,rINST_FULL        # rINST_FULL<- BA
    andb     $$0xf,rINST_LO             # rINST_FULL<- A
    imull     %ecx,%eax                 # trashes rPC
    UNSPILL(rPC)
    SET_VREG(%eax,rINST_FULL)
    FETCH_INST_WORD(2)
    ADVANCE_PC(2)
    GOTO_NEXT
