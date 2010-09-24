%verify "executed"
    /* const/4 vA, #+B */
    movsx   rINSTbl,%eax              # eax<-ssssssBx
    movl    $$0xf,%ecx
    andl    %eax,%ecx                 # ecx<- A
    FETCH_INST_OPCODE 1 %edx
    ADVANCE_PC 1
    sarl    $$4,%eax
    SET_VREG %eax %ecx
    GOTO_NEXT_R %edx
