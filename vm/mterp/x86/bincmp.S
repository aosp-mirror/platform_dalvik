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
    movzx    rINSTbl,%ecx          # ecx <- A+
    andb     $$0xf,%cl             # ecx <- A
    GET_VREG_R %eax %ecx           # eax <- vA
    sarl     $$4,rINST            # rINST<- B
    cmpl     (rFP,rINST,4),%eax    # compare (vA, vB)
    movswl   2(rPC),rINST          # Get signed branch offset
    movl     $$2,%eax              # assume not taken
    j${revcmp}   1f
    testl    rINST,rINST
    js       common_backwardBranch
    movl     rINST,%eax
1:
    FETCH_INST_INDEXED %eax
    ADVANCE_PC_INDEXED %eax
    GOTO_NEXT
