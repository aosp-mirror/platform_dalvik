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
    movzbl    3(rPC),%edx              # edx<- CC
    GET_VREG_WORD %eax %ecx,1          # eax<- v[BB+1]
    GET_VREG_WORD %ecx %ecx 0          # ecx<- v[BB+0]
    cmpl      4(rFP,%edx,4),%eax
    jl        .L${opcode}_smaller
    jg        .L${opcode}_bigger
    sub       (rFP,%edx,4),%ecx
    ja        .L${opcode}_bigger
    jb        .L${opcode}_smaller
    jmp       .L${opcode}_finish
%break

.L${opcode}_bigger:
    movl      $$1,%ecx
    jmp       .L${opcode}_finish
.L${opcode}_smaller:
    movl      $$-1,%ecx
.L${opcode}_finish:
    SET_VREG %ecx rINST
    FETCH_INST_OPCODE 2 %edx
    ADVANCE_PC 2
    GOTO_NEXT_R %edx
