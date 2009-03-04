%default {"pre0":"","pre1":""}
    /*
     * Generic 32-bit unary operation.  Provide an "instr" line that
     * specifies an instruction that performs "result = op eax".
     */
    /* unop vA, vB */
    movzbl   rINST_HI,%ecx           # ecx<- A+
    sarl     $$12,rINST_FULL         # rINST_FULL<- B
    GET_VREG(%eax,rINST_FULL)        # eax<- vB
    andb     $$0xf,%cl               # ecx<- A
    FETCH_INST_WORD(1)
    ADVANCE_PC(1)
    $pre0
    $pre1
    $instr
    SET_VREG(%eax,%ecx)
    GOTO_NEXT
