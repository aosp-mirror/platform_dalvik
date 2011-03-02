%verify "executed"
%verify "forward and backward"
    /*
     * Unconditional branch, 16-bit offset.
     *
     * The branch distance is a signed code-unit offset
     */
    /* goto/16 +AAAA */
    movl    rSELF,%ecx
    movswl  2(rPC),%eax            # eax<- ssssAAAA
    movl    offThread_curHandlerTable(%ecx),rIBASE
    FETCH_INST_INDEXED %eax
    ADVANCE_PC_INDEXED %eax
    GOTO_NEXT
