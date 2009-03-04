%verify "executed"
    /* move-wide/from16 vAA, vBBBB */
    /* NOTE: regs can overlap, e.g. "move v6,v7" or "move v7,v6" */
    movzwl    2(rPC),%ecx              # ecx<- BBBB
    movzbl    rINST_HI,%eax            # eax<- AAAA
    GET_VREG_WORD(rINST_FULL,%ecx,0)   # rINST_FULL<- v[BBBB+0]
    GET_VREG_WORD(%ecx,%ecx,1)         # ecx<- v[BBBB+1]
    SET_VREG_WORD(rINST_FULL,%eax,0)   # v[AAAA+0]<- rINST_FULL
    FETCH_INST_WORD(2)
    ADVANCE_PC(2)
    SET_VREG_WORD(%ecx,%eax,1)         # v[AAAA+1]<- eax
    GOTO_NEXT
