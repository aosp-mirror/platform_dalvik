%verify "executed"
%verify "forward and backward"
    /*
     * Unconditional branch, 8-bit offset.
     *
     * The branch distance is a signed code-unit offset, which we need to
     * double to get a byte offset.
     */
    /* goto +AA */
    movsbl  rINSTbl,rINST         # ebx<- ssssssAA
    testl   rINST,rINST           # test for <0
    js      common_backwardBranch
    movl    rINST,%eax
    FETCH_INST_INDEXED %eax
    ADVANCE_PC_INDEXED %eax
    GOTO_NEXT
