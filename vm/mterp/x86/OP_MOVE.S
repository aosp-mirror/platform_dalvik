%verify "executed"
    /* for move, move-object, long-to-int */
    /* op vA, vB */
    movzbl rINSTbl,%eax          # eax<- BA
    andb   $$0xf,%al             # eax<- A
    shrl   $$4,rINST            # rINST<- B
    GET_VREG_R %ecx rINST
    FETCH_INST_OPCODE 1 %edx
    ADVANCE_PC 1
    SET_VREG %ecx %eax           # fp[A]<-fp[B]
    GOTO_NEXT_R %edx
