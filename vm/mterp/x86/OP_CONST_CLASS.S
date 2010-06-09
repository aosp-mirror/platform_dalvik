
%verify "Class already resolved"
%verify "Class not yet resolved"
%verify "Class cannot be resolved"
    /* const/class vAA, Class@BBBB */
    GET_GLUE(%ecx)
    movzwl    2(rPC),%eax              # eax<- BBBB
    movl      offGlue_methodClassDex(%ecx),%ecx# ecx<- glue->methodClassDex
    movzbl    rINST_HI,rINST_FULL      # rINST_FULL<- AA
    movl      offDvmDex_pResClasses(%ecx),%ecx # ecx<- dvmDex->pResClasses
    movl      (%ecx,%eax,4),%eax       # eax<- rResClasses[BBBB]
    movl      rINST_FULL,%ecx
    FETCH_INST_WORD(2)
    testl     %eax,%eax                # resolved yet?
    je        .L${opcode}_resolve
    SET_VREG(%eax,%ecx)                # vAA<- rResClasses[BBBB]
    ADVANCE_PC(2)
    GOTO_NEXT
%break

/* This is the less common path, so we'll redo some work
   here rather than force spills on the common path */
.L${opcode}_resolve:
    GET_GLUE(%eax)
    movl     %ecx,rINST_FULL           # rINST_FULL<- AA
    EXPORT_PC()
    movl     offGlue_method(%eax),%eax # eax<- glue->method
    movl     $$1,OUT_ARG2(%esp)        # true
    movzwl   2(rPC),%ecx               # ecx<- BBBB
    movl     offMethod_clazz(%eax),%eax
    SPILL(rPC)
    movl     %ecx,OUT_ARG1(%esp)
    movl     %eax,OUT_ARG0(%esp)
    call     dvmResolveClass           # go resolve
    UNSPILL(rPC)
    testl    %eax,%eax                 # failed?
    je       common_exceptionThrown
    SET_VREG(%eax,rINST_FULL)
    FETCH_INST_WORD(2)
    ADVANCE_PC(2)
    GOTO_NEXT
