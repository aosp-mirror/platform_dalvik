%verify "executed"
%verify "forward, backward, self"
    /*
     * Unconditional branch, 32-bit offset.
     *
     * The branch distance is a signed code-unit offset.
     *
     * Unlike most opcodes, this one is allowed to branch to itself, so
     * our "backward branch" test must be "<=0" instead of "<0".
     */
    /* goto/32 AAAAAAAA */
    movl    2(rPC),rINST_FULL           # rINST_FULL<- AAAAAAAA
    cmpl    $$0,rINST_FULL              # test for <= 0
    jle     common_backwardBranch
    movl    rINST_FULL,%eax
    FETCH_INST_INDEXED(%eax)
    ADVANCE_PC_INDEXED(%eax)
    GOTO_NEXT
