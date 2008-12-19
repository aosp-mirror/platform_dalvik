%verify "executed"
    /* for: move/16, move-object/16 */
    /* op vAAAA, vBBBB */
    movzwl    4(rPC),%ecx              # ecx<- BBBB
    movzwl    2(rPC),%eax              # eax<- AAAA
    GET_VREG(%ecx,%ecx)
    FETCH_INST_WORD(3)
    ADVANCE_PC(3)
    SET_VREG(%ecx,%eax)
    GOTO_NEXT
