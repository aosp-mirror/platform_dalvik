%verify "executed"
%verify "forward and backward"
    /*
     * Unconditional branch, 16-bit offset.
     *
     * The branch distance is a signed code-unit offset, which we need to
     * double to get a byte offset.
     */
    /* goto/16 +AAAA */
    FETCH_S(r0, 1)                      @ r0<- ssssAAAA (sign-extended)
    movs    r9, r0, asl #1              @ r9<- byte offset, check sign
    bmi     common_backwardBranch       @ backward branch, do periodic checks
#if defined(WITH_JIT)
    GET_JIT_PROF_TABLE(r0)
    FETCH_ADVANCE_INST_RB(r9)           @ update rPC, load rINST
    cmp     r0,#0
    bne     common_updateProfile
    GET_INST_OPCODE(ip)                 @ extract opcode from rINST
    GOTO_OPCODE(ip)                     @ jump to next instruction
#else
    FETCH_ADVANCE_INST_RB(r9)           @ update rPC, load rINST
    GET_INST_OPCODE(ip)                 @ extract opcode from rINST
    GOTO_OPCODE(ip)                     @ jump to next instruction
#endif
