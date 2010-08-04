%verify "executed"
    /* move-result-wide vAA */
    mov     r2, rINST, lsr #8           @ r2<- AA
    add     r3, rGLUE, #offGlue_retval  @ r3<- &glue->retval
    add     r2, rFP, r2, lsl #2         @ r2<- &fp[AA]
    ldmia   r3, {r0-r1}                 @ r0/r1<- retval.j
    FETCH_ADVANCE_INST(1)               @ advance rPC, load rINST
    GET_INST_OPCODE(ip)                 @ extract opcode from rINST
    stmia   r2, {r0-r1}                 @ fp[AA]<- r0/r1
    GOTO_OPCODE(ip)                     @ jump to next instruction
