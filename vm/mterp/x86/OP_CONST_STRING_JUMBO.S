
%verify "String already resolved"
%verify "String not yet resolved"
%verify "String cannot be resolved"
    /* const/string vAA, String@BBBBBBBB */
    movl      rSELF,%ecx
    movl      2(rPC),%eax              # eax<- BBBBBBBB
    movl      offThread_methodClassDex(%ecx),%ecx# ecx<- self->methodClassDex
    movl      offDvmDex_pResStrings(%ecx),%ecx # ecx<- dvmDex->pResStrings
    movl      (%ecx,%eax,4),%eax       # eax<- rResString[BBBB]
    movl      rINST,%ecx
    FETCH_INST_OPCODE 3 %edx
    testl     %eax,%eax                # resolved yet?
    je        .L${opcode}_resolve
    SET_VREG  %eax %ecx                # vAA<- rResString[BBBB]
    ADVANCE_PC 3
    GOTO_NEXT_R %edx
%break

/* This is the less common path, so we'll redo some work
   here rather than force spills on the common path */
.L${opcode}_resolve:
    movl     rSELF,%eax
    movl     %ecx,rINST                # rINST<- AA
    EXPORT_PC
    movl     offThread_method(%eax),%eax # eax<- self->method
    movl     2(rPC),%ecx               # ecx<- BBBBBBBB
    movl     offMethod_clazz(%eax),%eax
    movl     %ecx,OUT_ARG1(%esp)
    movl     %eax,OUT_ARG0(%esp)
    call     dvmResolveString          # go resolve
    testl    %eax,%eax                 # failed?
    je       common_exceptionThrown
    SET_VREG %eax rINST
    FETCH_INST_OPCODE 3 %edx
    ADVANCE_PC 3
    GOTO_NEXT_R %edx
