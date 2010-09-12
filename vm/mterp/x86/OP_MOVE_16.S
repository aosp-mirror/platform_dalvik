%verify "executed"
    /* for: move/16, move-object/16 */
    /* op vAAAA, vBBBB */
    movzwl    4(rPC),%ecx              # ecx<- BBBB
    movzwl    2(rPC),%eax              # eax<- AAAA
    GET_VREG_R  %ecx %ecx
    FETCH_INST_OPCODE 3 %edx
    ADVANCE_PC 3
    SET_VREG  %ecx %eax
    GOTO_NEXT_R %edx
