%verify "executed"
    /* move-wide vA, vB */
    /* NOTE: regs can overlap, e.g. "move v6,v7" or "move v7,v6" */
    movzbl    rINSTbl,%ecx                # ecx <- BA
    sarl      $$4,rINST                   # rINST<- B
    GET_VREG_WORD %eax rINST 0            # eax<- v[B+0]
    GET_VREG_WORD rINST rINST 1           # rINST<- v[B+1]
    andb      $$0xf,%cl                   # ecx <- A
    FETCH_INST_OPCODE 1 %edx
    SET_VREG_WORD rINST %ecx 1            # v[A+1]<- rINST
    ADVANCE_PC 1
    SET_VREG_WORD %eax %ecx 0             # v[A+0]<- eax
    GOTO_NEXT_R %edx
