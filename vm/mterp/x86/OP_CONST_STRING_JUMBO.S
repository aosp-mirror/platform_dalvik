
%verify "String already resolved"
%verify "String not yet resolved"
%verify "String cannot be resolved"
    /* const/string vAA, String@BBBBBBBB */
    GET_GLUE(%ecx)
    movl      2(rPC),%eax              # eax<- BBBBBBBB
    movl      offGlue_methodClassDex(%ecx),%ecx# ecx<- glue->methodClassDex
    movzbl    rINST_HI,rINST_FULL      # rINST_FULL<- AA
    movl      offDvmDex_pResStrings(%ecx),%ecx # ecx<- dvmDex->pResStrings
    movl      (%ecx,%eax,4),%eax       # eax<- rResString[BBBB]
    movl      rINST_FULL,%ecx
    FETCH_INST_WORD(3)
    testl     %eax,%eax                # resolved yet?
    je        .L${opcode}_resolve
    SET_VREG(%eax,%ecx)                # vAA<- rResString[BBBB]
    ADVANCE_PC(3)
    GOTO_NEXT
%break

/* This is the less common path, so we'll redo some work
   here rather than force spills on the common path */
.L${opcode}_resolve:
    GET_GLUE(%eax)
    movl     %ecx,rINST_FULL           # rINST_FULL<- AA
    EXPORT_PC()
    movl     offGlue_method(%eax),%eax # eax<- glue->method
    movl     2(rPC),%ecx               # ecx<- BBBBBBBB
    movl     offMethod_clazz(%eax),%eax
    SPILL(rPC)
    movl     %ecx,OUT_ARG1(%esp)
    movl     %eax,OUT_ARG0(%esp)
    call     dvmResolveString          # go resolve
    UNSPILL(rPC)
    testl    %eax,%eax                 # failed?
    je       common_exceptionThrown
    SET_VREG(%eax,rINST_FULL)
    FETCH_INST_WORD(3)
    ADVANCE_PC(3)
    GOTO_NEXT
