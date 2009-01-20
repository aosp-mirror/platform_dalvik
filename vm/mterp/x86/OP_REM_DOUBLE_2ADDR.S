%verify "executed"
    /* rem_float/2addr vA, vB */
    movzx   rINST_HI,%ecx               # ecx<- A+
    sarl    $$12,rINST_FULL             # rINST_FULL<- B
    fldl     (rFP,rINST_FULL,4)         # vBB to fp stack
    andb    $$0xf,%cl                   # ecx<- A
    fldl     (rFP,%ecx,4)               # vAA to fp stack
    FETCH_INST_WORD(1)
1:
    fprem
    fstsw     %ax
    sahf
    jp        1b
    fstp      %st(1)
    ADVANCE_PC(1)
    fstpl    (rFP,%ecx,4)               # %st to vA
    GOTO_NEXT
