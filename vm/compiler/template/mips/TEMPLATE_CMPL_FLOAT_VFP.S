%default { "naninst":"li     rTEMP, -1" }
%verify "executed"
%verify "basic lt, gt, eq */
%verify "left arg NaN"
%verify "right arg NaN"
    /*
     * Compare two floating-point values.  Puts 0, 1, or -1 into the
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
     * for: cmpl-float, cmpg-float
     */
    /* op vAA, vBB, vCC */

    /* "clasic" form */
#ifdef  SOFT_FLOAT
    LOAD(rOBJ, a0)                      # rOBJ<- vBB
    LOAD(rBIX, a1)                      # rBIX<- vCC
    move     a0, rOBJ                   # a0<- vBB
    move     a1, rBIX                   # a1<- vCC
    JAL(__eqsf2)                        # v0<- (vBB == vCC)
    li       rTEMP, 0                   # vAA<- 0
    beqz     v0, ${opcode}_finish
    move     a0, rOBJ                   # a0<- vBB
    move     a1, rBIX                   # a1<- vCC
    JAL(__ltsf2)                        # a0<- (vBB < vCC)
    li       rTEMP, -1                  # vAA<- -1
    bltz     v0, ${opcode}_finish
    move     a0, rOBJ                   # a0<- vBB
    move     a1, rBIX                   # a1<- vCC
    JAL(__gtsf2)                        # v0<- (vBB > vCC)
    li      rTEMP, 1                    # vAA<- 1
    bgtz    v0, ${opcode}_finish
#else
    LOAD_F(ft0, a0)                     # ft0<- vBB
    LOAD_F(ft1, a1)                     # ft1<- vCC
    c.olt.s     fcc0, ft0, ft1          #Is ft0 < ft1
    li          rTEMP, -1
    bc1t        fcc0, ${opcode}_finish
    c.olt.s     fcc0, ft1, ft0
    li          rTEMP, 1
    bc1t        fcc0, ${opcode}_finish
    c.eq.s      fcc0, ft0, ft1
    li          rTEMP, 0
    bc1t        fcc0, ${opcode}_finish
#endif

    $naninst

${opcode}_finish:
    move     v0, rTEMP                  # v0<- vAA
    RETURN
