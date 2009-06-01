%verify "executed"
%verify "forward, backward, self"
    /*
     * Unconditional branch, 32-bit offset.
     *
     * The branch distance is a signed code-unit offset, which we need to
     * double to get a byte offset.
     *
     * Unlike most opcodes, this one is allowed to branch to itself, so
     * our "backward branch" test must be "<=0" instead of "<0".  The ORRS
     * instruction doesn't affect the V flag, so we need to clear it
     * explicitly.
     */
    /* goto/32 +AAAAAAAA */
    FETCH(r0, 1)                        @ r0<- aaaa (lo)
    FETCH(r1, 2)                        @ r1<- AAAA (hi)
    cmp     ip, ip                      @ (clear V flag during stall)
    orrs    r0, r0, r1, lsl #16         @ r0<- AAAAaaaa, check sign
    mov     r9, r0, asl #1              @ r9<- byte offset
    ble     common_backwardBranch       @ backward branch, do periodic checks
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
