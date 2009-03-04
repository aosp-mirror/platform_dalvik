%verify "executed"
%verify "basic lt, gt, eq"
%verify "hi equal, lo <=>"
%verify "lo equal, hi <=>"
    /*
     * Compare two 64-bit values.  Puts 0, 1, or -1 into the destination
     * register based on the results of the comparison.
     */
    /* cmp-long vAA, vBB, vCC */
    movzbl    2(rPC),%ecx              # ecx<- BB
    SPILL(rPC)
    movzbl    3(rPC),rPC               # rPC<- CC
    GET_VREG_WORD(%eax,%ecx,1)         # eax<- v[BB+1]
    GET_VREG_WORD(%ecx,%ecx,0)         # ecx<- v[BB+0]
    movzbl    rINST_HI,rINST_FULL      # rINST_FULL<- AA
    cmpl      4(rFP,rPC,4),%eax
    jl        .L${opcode}_smaller
    jg        .L${opcode}_bigger
    sub       (rFP,rPC,4),%ecx
    ja        .L${opcode}_bigger
    jb        .L${opcode}_smaller
    UNSPILL(rPC)
    jmp       .L${opcode}_finish
%break

.L${opcode}_bigger:
    UNSPILL(rPC)
    movl      $$1,%ecx
    jmp       .L${opcode}_finish
.L${opcode}_smaller:
    UNSPILL(rPC)
    movl      $$-1,%ecx
.L${opcode}_finish:
    SET_VREG(%ecx,rINST_FULL)
    FETCH_INST_WORD(2)
    ADVANCE_PC(2)
    GOTO_NEXT
