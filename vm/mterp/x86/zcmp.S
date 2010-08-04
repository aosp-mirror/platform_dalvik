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
    movzx    rINST_HI,%ecx             # ecx <- AA
    cmpl     $$0,(rFP,%ecx,4)          # compare (vA, 0)
    movswl   2(rPC),rINST_FULL         # fetch signed displacement
    movl     $$2,%eax                  # assume branch not taken
    j${revcmp}   1f
    testl    rINST_FULL,rINST_FULL
    js       common_backwardBranch
    movl     rINST_FULL,%eax
1:
    FETCH_INST_INDEXED(%eax)
    ADVANCE_PC_INDEXED(%eax)
    GOTO_NEXT
