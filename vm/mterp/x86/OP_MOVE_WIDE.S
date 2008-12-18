%verify "executed"
    /* move-wide vA, vB */
    /* NOTE: regs can overlap, e.g. "move v6,v7" or "move v7,v6" */
    movzbl    rINST_HI,%ecx                # ecx <- BA
    sarl      $$12,rINST_FULL              # rinst_FULL<- B
    GET_VREG_WORD(%eax,rINST_FULL,0)       # eax<- v[B+0]
    GET_VREG_WORD(rINST_FULL,rINST_FULL,1) # rINST_FULL<- v[B+1]
    andb      $$0xf,%cl                    # ecx <- A
    SET_VREG_WORD(rINST_FULL,%ecx,1)       # v[A+1]<- rINST_FULL
    FETCH_INST_WORD(1)
    ADVANCE_PC(1)
    SET_VREG_WORD(%eax,%ecx,0)             # v[A+0]<- eax
    GOTO_NEXT
