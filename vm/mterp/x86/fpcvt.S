%default {"instr":"","load":"","store":""}
    /*
     * Generic 32-bit FP conversion operation.
     */
    /* unop vA, vB */
    movzbl   rINST_HI,%ecx           # ecx<- A+
    sarl     $$12,rINST_FULL         # rINST_FULL<- B
    $load    (rFP,rINST_FULL,4)      # %st0<- vB
    andb     $$0xf,%cl               # ecx<- A
    FETCH_INST_WORD(1)
    ADVANCE_PC(1)
    $instr
    $store  (rFP,%ecx,4)             # vA<- %st0
    GOTO_NEXT
