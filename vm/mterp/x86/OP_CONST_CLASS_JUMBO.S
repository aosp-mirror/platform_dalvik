%verify "Class already resolved"
%verify "Class not yet resolved"
%verify "Class cannot be resolved"
    /* const-class/jumbo vBBBB, Class@AAAAAAAA */
    movl      rSELF,%ecx
    movl      2(rPC),%eax              # eax<- AAAAAAAA
    movl      offThread_methodClassDex(%ecx),%ecx# ecx<- self->methodClassDex
    movl      offDvmDex_pResClasses(%ecx),%ecx # ecx<- dvmDex->pResClasses
    movl      (%ecx,%eax,4),%eax       # eax<- rResClasses[AAAAAAAA]
    FETCH_INST_OPCODE 4 %ecx
    testl     %eax,%eax                # resolved yet?
    je        .L${opcode}_resolve
    SET_VREG  %eax rINST               # vBBBB<- rResClasses[AAAAAAAA]
    ADVANCE_PC 4
    GOTO_NEXT_R %ecx

/* This is the less common path, so we'll redo some work
   here rather than force spills on the common path */
.L${opcode}_resolve:
    movl     rSELF,%eax
    EXPORT_PC
    movl     offThread_method(%eax),%eax # eax<- self->method
    movl     $$1,OUT_ARG2(%esp)        # true
    movl     2(rPC),%ecx               # ecx<- AAAAAAAA
    movl     offMethod_clazz(%eax),%eax
    movl     %ecx,OUT_ARG1(%esp)
    movl     %eax,OUT_ARG0(%esp)
    SPILL(rIBASE)
    call     dvmResolveClass           # go resolve
    UNSPILL(rIBASE)
    testl    %eax,%eax                 # failed?
    je       common_exceptionThrown
    FETCH_INST_OPCODE 4 %ecx
    SET_VREG %eax rINST
    ADVANCE_PC 4
    GOTO_NEXT_R %ecx
