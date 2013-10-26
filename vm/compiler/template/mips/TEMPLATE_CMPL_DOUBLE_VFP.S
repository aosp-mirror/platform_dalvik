%default { "naninst":"li     rTEMP, -1" }
%verify "executed"
%verify "basic lt, gt, eq */
%verify "left arg NaN"
%verify "right arg NaN"
    /*
     * Compare two double precision floating-point values.  Puts 0, 1, or -1 into the
     * destination register based on the results of the comparison.
     *
     * Provide a "naninst" instruction that puts 1 or -1 into a1 depending
     * on what value we'd like to return when one of the operands is NaN.
     *
     * The operation we're implementing is:
     *   if (x == y)
     *     return 0;
     *   else if (x < y)
     *     return -1;
     *   else if (x > y)
     *     return 1;
     *   else
     *     return {-1,1};  // one or both operands was NaN
     *
     * On entry:
     *    a0 = &op1 [vBB]
     *    a1 = &op2 [vCC]
     *
     * for: cmpl-double, cmpg-double
     */
    /* op vAA, vBB, vCC */

    /* "clasic" form */
#ifdef  SOFT_FLOAT
    move rOBJ, a0                       # save a0
    move rBIX, a1                       # save a1
    LOAD64(rARG0, rARG1, rOBJ)          # a0/a1<- vBB/vBB+1
    LOAD64(rARG2, rARG3, rBIX)          # a2/a3<- vCC/vCC+1
    JAL(__eqdf2)                        # v0<- (vBB == vCC)
    li       rTEMP, 0                   # vAA<- 0
    beqz     v0, ${opcode}_finish
    LOAD64(rARG0, rARG1, rOBJ)          # a0/a1<- vBB/vBB+1
    LOAD64(rARG2, rARG3, rBIX)          # a2/a3<- vCC/vCC+1
    JAL(__ltdf2)                        # a0<- (vBB < vCC)
    li       rTEMP, -1                  # vAA<- -1
    bltz     v0, ${opcode}_finish
    LOAD64(rARG0, rARG1, rOBJ)          # a0/a1<- vBB/vBB+1
    LOAD64(rARG2, rARG3, rBIX)          # a2/a3<- vCC/vCC+1
    JAL(__gtdf2)                        # v0<- (vBB > vCC)
    li      rTEMP, 1                    # vAA<- 1
    bgtz    v0, ${opcode}_finish
#else
    LOAD64_F(ft0, ft0f, a0)             # ft0<- vBB
    LOAD64_F(ft1, ft1f, a1)             # ft1<- vCC
    c.olt.d     fcc0, ft0, ft1          # Is ft0 < ft1
    li          rTEMP, -1
    bc1t        fcc0, ${opcode}_finish
    c.olt.d     fcc0, ft1, ft0
    li          rTEMP, 1
    bc1t        fcc0, ${opcode}_finish
    c.eq.d      fcc0, ft0, ft1
    li          rTEMP, 0
    bc1t        fcc0, ${opcode}_finish
#endif

    $naninst

${opcode}_finish:
    move     v0, rTEMP                  # v0<- vAA
    RETURN
