%verify "executed"
    /* move-wide/16 vAAAA, vBBBB */
    /* NOTE: regs can overlap, e.g. "move v6,v7" or "move v7,v6" */
    movzwl    4(rPC),%ecx            # ecx<- BBBB
    movzwl    2(rPC),%eax            # eax<- AAAA
    GET_VREG_WORD rINST %ecx 0       # rINSTw_WORD<- v[BBBB+0]
    GET_VREG_WORD %ecx %ecx 1        # ecx<- v[BBBB+1]
    FETCH_INST_OPCODE 3 %edx
    SET_VREG_WORD rINST %eax 0       # v[AAAA+0]<- rINST
    ADVANCE_PC 3
    SET_VREG_WORD %ecx %eax 1        # v[AAAA+1]<- ecx
    GOTO_NEXT_R %edx
