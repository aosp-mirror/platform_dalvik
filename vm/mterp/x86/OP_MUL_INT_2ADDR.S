%verify "executed"
    /* mul vA, vB */
    movzx   rINSTbl,%ecx               # ecx<- A+
    sarl    $$4,rINST                 # rINST<- B
    GET_VREG_R %eax rINST              # eax<- vB
    andb    $$0xf,%cl                  # ecx<- A
    imull   (rFP,%ecx,4),%eax
    FETCH_INST_OPCODE 1 %edx
    SET_VREG %eax %ecx
    ADVANCE_PC 1
    GOTO_NEXT_R %edx
