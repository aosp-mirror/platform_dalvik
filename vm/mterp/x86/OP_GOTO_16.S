%verify "executed"
%verify "forward and backward"
    /*
     * Unconditional branch, 16-bit offset.
     *
     * The branch distance is a signed code-unit offset
     */
    /* goto/16 +AAAA */
    movswl  2(rPC),rINST_FULL           # rINST_FULL<- ssssAAAA
    testl   rINST_FULL,rINST_FULL       # test for <0
    js      common_backwardBranch
    movl    rINST_FULL,%eax
    FETCH_INST_INDEXED(%eax)
    ADVANCE_PC_INDEXED(%eax)
    GOTO_NEXT
