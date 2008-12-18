%default { "func":"dvmInterpHandlePackedSwitch" }
%verify executed
    /*
     * Handle a packed-switch or sparse-switch instruction.  In both cases
     * we decode it and hand it off to a helper function.
     *
     * We don't really expect backward branches in a switch statement, but
     * they're perfectly legal, so we check for them here.
     *
     * for: packed-switch, sparse-switch
     */
    /* op vAA, +BBBB */
    movzbl  rINST_HI,rINST_FULL         # rINST_FULL<- AA
    movl    2(rPC),%ecx                 # ecx<- BBBBbbbb
    GET_VREG(%eax,rINST_FULL)           # eax<- vAA
    leal    (rPC,%ecx,2),%ecx           # ecx<- PC + BBBBbbbb*2
    movl    %eax,OUT_ARG1(%esp)         # ARG1<- vAA
    movl    %ecx,OUT_ARG0(%esp)         # ARG0<- switchData
    SPILL(rPC)
    call    $func
    UNSPILL(rPC)
    testl   %eax,%eax
    movl    %eax,rINST_FULL             # set up word offset
    jle     common_backwardBranch       # check on special actions
    ADVANCE_PC_INDEXED(rINST_FULL)
    FETCH_INST()
    GOTO_NEXT
