%verify "executed"
    /*
     * Long integer shift, 2addr version.  vA is 64-bit value/result, vB is
     * 32-bit shift distance.
     */
    /* shl-long/2addr vA, vB */
    /* ecx gets shift count */
    /* Need to spill edx */
    /* rINSTw gets AA */
    movzbl    rINSTbl,%ecx             # ecx<- BA
    andb      $$0xf,rINSTbl            # rINST<- A
    GET_VREG_WORD %eax rINST 0         # eax<- v[AA+0]
    sarl      $$4,%ecx                 # ecx<- B
    GET_VREG_WORD %edx rINST 1         # edx<- v[AA+1]
    GET_VREG_R  %ecx %ecx              # ecx<- vBB
    shldl     %eax,%edx
    sall      %cl,%eax
    testb     $$32,%cl
    je        2f
    movl      %eax,%edx
    xorl      %eax,%eax
2:
    SET_VREG_WORD %edx rINST 1         # v[AA+1]<- edx
    jmp       .L${opcode}_finish
%break


.L${opcode}_finish:
    FETCH_INST_OPCODE 1 %edx
    SET_VREG_WORD %eax rINST 0         # v[AA+0]<- eax
    ADVANCE_PC 1
    GOTO_NEXT_R %edx
