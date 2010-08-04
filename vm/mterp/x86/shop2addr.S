%default {"result":"%eax"}
    /*
     * Generic 32-bit "shift/2addr" operation.
     */
    /* shift/2addr vA, vB */
    movzx    rINST_HI,%ecx          # eax<- BA
    sarl     $$4,%ecx               # ecx<- B
    GET_VREG(%ecx,%ecx)             # eax<- vBB
    movzbl   rINST_HI,rINST_FULL    # rINST_FULL<- BA
    andb     $$0xf,rINST_LO         # rINST_FULL<- A
    GET_VREG(%eax,rINST_FULL)       # eax<- vAA
    $instr                          # ex: sarl %cl,%eax
    SET_VREG($result,rINST_FULL)
    FETCH_INST_WORD(1)
    ADVANCE_PC(1)
    GOTO_NEXT
