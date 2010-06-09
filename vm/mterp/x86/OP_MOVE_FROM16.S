%verify "executed"
    /* for: move/from16, move-object/from16 */
    /* op vAA, vBBBB */
    movzx    rINST_HI,%eax            # eax <= AA
    movw     2(rPC),rINST             # rINST <= BBBB
    GET_VREG (%ecx,rINST_FULL)        # ecx<- fp[BBBB]
    FETCH_INST_WORD(2)
    ADVANCE_PC(2)
    SET_VREG (%ecx,%eax)              # fp[AA]<- ecx]
    GOTO_NEXT
