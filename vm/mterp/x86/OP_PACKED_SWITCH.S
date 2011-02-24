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
    movl    2(rPC),%ecx           # ecx<- BBBBbbbb
    GET_VREG_R %eax rINST         # eax<- vAA
    leal    (rPC,%ecx,2),%ecx     # ecx<- PC + BBBBbbbb*2
    movl    %eax,OUT_ARG1(%esp)   # ARG1<- vAA
    movl    %ecx,OUT_ARG0(%esp)   # ARG0<- switchData
    SPILL(rIBASE)
    call    $func
    UNSPILL(rIBASE)
    testl   %eax,%eax
    movl    %eax,rINST            # set up word offset
    jle     common_backwardBranch # check on special actions
    ADVANCE_PC_INDEXED rINST
    FETCH_INST
    GOTO_NEXT
