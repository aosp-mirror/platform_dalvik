%default {"result":"%eax"}
    /*
     * Generic 32-bit "lit16" binary operation.  Provide an "instr" line
     * that specifies an instruction that performs "result = eax op ecx".
     * This could be an x86 instruction or a function call.  (If the result
     * comes back in a register other than eax, you can override "result".)
     *
     * For: add-int/lit16, rsub-int,
     *      and-int/lit16, or-int/lit16, xor-int/lit16
     */
    /* binop/lit16 vA, vB, #+CCCC */
    movzbl   rINST_HI,%eax              # eax<- 000000BA
    sarl     $$4,%eax                   # eax<- B
    GET_VREG(%eax,%eax)                 # eax<- vB
    movswl   2(rPC),%ecx                # ecx<- ssssCCCC
    movzbl   rINST_HI,rINST_FULL        # rINST_FULL<- BA
    andb     $$0xf,rINST_LO             # rINST_FULL<- A
    $instr                              # for example: addl %ecx, %eax
    SET_VREG($result,rINST_FULL)
    FETCH_INST_WORD(2)
    ADVANCE_PC(2)
    GOTO_NEXT
