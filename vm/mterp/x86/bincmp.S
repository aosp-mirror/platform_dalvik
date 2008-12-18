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
    movzx    rINST_HI,%ecx              # ecx <- A+
    andb     $$0xf,%cl                  # ecx <- A
    GET_VREG(%eax,%ecx)                 # eax <- vA
    sarl     $$12,rINST_FULL            # rINST_FULL<- B
    cmpl     (rFP,rINST_FULL,4),%eax    # compare (vA, vB)
    movswl   2(rPC),rINST_FULL          # Get signed branch offset
    movl     $$2,%eax                   # assume not taken
    j${revcmp}   1f
    testl    rINST_FULL,rINST_FULL
    js       common_backwardBranch
    movl     rINST_FULL,%eax
1:
    FETCH_INST_INDEXED(%eax)
    ADVANCE_PC_INDEXED(%eax)
    GOTO_NEXT
    GOTO_NEXT
