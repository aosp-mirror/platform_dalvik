%verify "branch taken"
%verify "branch not taken"
    /*
     * Generic two-operand compare-and-branch operation.  Provide a "revcmp"
     * fragment that specifies the *reverse* comparison to perform, e.g.
     * for "if-le" you would use "gt".
     *
     * For: if-eq, if-ne, if-lt, if-ge, if-gt, if-le
     */
    /* if-cmp vA, vB, +CCCC */
    mov     r0, rINST, lsr #8           @ r0<- A+
    mov     r1, rINST, lsr #12          @ r1<- B
    and     r0, r0, #15
    GET_VREG(r3, r1)                    @ r3<- vB
    GET_VREG(r2, r0)                    @ r2<- vA
    mov     r9, #4                      @ r0<- BYTE branch dist for not-taken
    cmp     r2, r3                      @ compare (vA, vB)
    b${revcmp}  1f                      @ branch to 1 if comparison failed
    FETCH_S(r9, 1)                      @ r9<- branch offset, in code units
    movs    r9, r9, asl #1              @ convert to bytes, check sign
    bmi     common_backwardBranch       @ yes, do periodic checks
1:
#if defined(WITH_JIT)
    GET_JIT_PROF_TABLE(r0)
    FETCH_ADVANCE_INST_RB(r9)           @ update rPC, load rINST
    b        common_testUpdateProfile
#else
    FETCH_ADVANCE_INST_RB(r9)           @ update rPC, load rINST
    GET_INST_OPCODE(ip)                 @ extract opcode from rINST
    GOTO_OPCODE(ip)                     @ jump to next instruction
#endif
