%verify "executed"
    /* const/high16 vAA, #+BBBB0000 */
    movzwl     2(rPC),%eax                # eax<- 0000BBBB
    movzbl     rINST_HI,%ecx              # ecx<- AA
    FETCH_INST_WORD(2)
    ADVANCE_PC(2)
    sall       $$16,%eax                  # eax<- BBBB0000
    SET_VREG(%eax,%ecx)                   # vAA<- eax
    GOTO_NEXT
