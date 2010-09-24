%default {"instr":"","load":"","store":""}
    /*
     * Generic 32-bit FP conversion operation.
     */
    /* unop vA, vB */
    movzbl   rINSTbl,%ecx       # ecx<- A+
    sarl     $$4,rINST         # rINST<- B
    $load    (rFP,rINST,4)      # %st0<- vB
    andb     $$0xf,%cl          # ecx<- A
    FETCH_INST_OPCODE 1 %edx
    ADVANCE_PC 1
    $instr
    $store  (rFP,%ecx,4)        # vA<- %st0
    GOTO_NEXT_R %edx
