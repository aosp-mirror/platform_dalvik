%verify "executed"
%verify "forward and backward"
    /*
     * Unconditional branch, 8-bit offset.
     *
     * The branch distance is a signed code-unit offset, which we need to
     * double to get a byte offset.
     */
    /* goto +AA */
    movsbl  rINST_HI,rINST_FULL         # ebx<- ssssssAA
    testl   rINST_FULL,rINST_FULL       # test for <0
    js      common_backwardBranch
    movl    rINST_FULL,%eax
    FETCH_INST_INDEXED(%eax)
    ADVANCE_PC_INDEXED(%eax)
    GOTO_NEXT
