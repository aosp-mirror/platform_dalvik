%verify "executed"
    /* const-wide/high16 vAA, #+BBBB000000000000 */
    movzwl     2(rPC),%eax                # eax<- 0000BBBB
    movzbl     rINST_HI,%ecx              # ecx<- AA
    FETCH_INST_WORD(2)
    ADVANCE_PC(2)
    sall       $$16,%eax                  # eax<- BBBB0000
    SET_VREG_WORD(%eax,%ecx,1)            # v[AA+1]<- eax
    xorl       %eax,%eax
    SET_VREG_WORD(%eax,%ecx,0)            # v[AA+0]<- eax
    GOTO_NEXT
