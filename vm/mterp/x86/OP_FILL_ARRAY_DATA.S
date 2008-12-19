%verify "executed"
    /* fill-array-data vAA, +BBBBBBBB */
    movl    2(rPC),%ecx                # ecx<- BBBBbbbb
    movzbl  rINST_HI,rINST_FULL        # rINST_FULL<- AA
    leal    (rPC,%ecx,2),%ecx          # ecx<- PC + BBBBbbbb*2
    GET_VREG(%eax,rINST_FULL)
    SPILL(rPC)
    EXPORT_PC()
    movl    %eax,OUT_ARG0(%esp)
    movl    %ecx,OUT_ARG1(%esp)
    call    dvmInterpHandleFillArrayData
    UNSPILL(rPC)
    FETCH_INST_WORD(3)
    testl   %eax,%eax                   # exception thrown?
    je      common_exceptionThrown
    ADVANCE_PC(3)
    GOTO_NEXT
