%verify "branch taken"
%verify "branch not taken"
    /*
     * Generic one-operand compare-and-branch operation.  Provide a "revcmp"
     * fragment that specifies the *reverse* comparison to perform, e.g.
     * for "if-le" you would use "gt".
     *
     * for: if-eqz, if-nez, if-ltz, if-gez, if-gtz, if-lez
     */
    /* if-cmp vAA, +BBBB */
    mov     r0, rINST, lsr #8           @ r0<- AA
    GET_VREG(r2, r0)                    @ r2<- vAA
    mov     r9, #4                      @ r0<- BYTE branch dist for not-taken
    cmp     r2, #0                      @ compare (vA, 0)
    b${revcmp}  1f                      @ branch to 1 if comparison failed
    FETCH_S(r9, 1)                      @ r9<- branch offset, in code units
    movs    r9, r9, asl #1              @ convert to bytes, check sign
    bmi     common_backwardBranch       @ backward branch, do periodic checks
1:
#if defined(WITH_JIT)
    GET_JIT_PROF_TABLE(r0)
    FETCH_ADVANCE_INST_RB(r9)           @ update rPC, load rINST
    cmp     r0,#0
    bne     common_updateProfile
    GET_INST_OPCODE(ip)                 @ extract opcode from rINST
    GOTO_OPCODE(ip)                     @ jump to next instruction
#else
    FETCH_ADVANCE_INST_RB(r9)           @ update rPC, load rINST
    GET_INST_OPCODE(ip)                 @ extract opcode from rINST
    GOTO_OPCODE(ip)                     @ jump to next instruction
#endif
