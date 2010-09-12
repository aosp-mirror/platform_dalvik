%verify "executed"
    /* for: move/from16, move-object/from16 */
    /* op vAA, vBBBB */
    movzx    rINSTbl,%eax              # eax <= AA
    movw     2(rPC),rINSTw             # rINSTw <= BBBB
    GET_VREG_R %ecx rINST              # ecx<- fp[BBBB]
    FETCH_INST_OPCODE 2 %edx
    ADVANCE_PC 2
    SET_VREG %ecx %eax                # fp[AA]<- ecx]
    GOTO_NEXT_R %edx
