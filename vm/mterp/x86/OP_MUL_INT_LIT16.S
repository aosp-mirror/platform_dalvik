%verify "executed"
    /* mul/lit16 vA, vB, #+CCCC */
    /* Need A in rINST, ssssCCCC in ecx, vB in eax */
    movzbl   rINSTbl,%eax               # eax<- 000000BA
    sarl     $$4,%eax                   # eax<- B
    GET_VREG_R %eax %eax                # eax<- vB
    movswl   2(rPC),%ecx                # ecx<- ssssCCCC
    andb     $$0xf,rINSTbl              # rINST<- A
    imull     %ecx,%eax                 # trashes edx
    FETCH_INST_OPCODE 2 %edx
    ADVANCE_PC 2
    SET_VREG %eax rINST
    GOTO_NEXT_R %edx
