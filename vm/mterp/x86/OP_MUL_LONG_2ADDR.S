%verify "executed"
    /*
     * Signed 64-bit integer multiply, 2-addr version
     *
     * We could definately use more free registers for
     * this code.  We must spill rPC (edx) because it
     * is used by imul.  We'll also spill rINST (ebx),
     * giving us eax, ebc, ecx and edx as computational
     * temps.  On top of that, we'll spill rIBASE (edi)
     * for use as the vA pointer and rFP (esi) for use
     * as the vB pointer.  Yuck.
     */
    /* mul-long/2addr vA, vB */
    movzbl    rINST_HI,%eax            # eax<- BA
    andb      $$0xf,%al                # eax<- A
    sarl      $$12,rINST_FULL          # rINST_FULL<- B
    SPILL(rPC)
    SPILL(rIBASE)
    SPILL(rFP)
    leal      (rFP,%eax,4),rIBASE      # rIBASE<- &v[A]
    leal      (rFP,rINST_FULL,4),rFP   # rFP<- &v[B]
    movl      4(rIBASE),%ecx      # ecx<- Amsw
    imull     (rFP),%ecx          # ecx<- (Amsw*Blsw)
    movl      4(rFP),%eax         # eax<- Bmsw
    imull     (rIBASE),%eax       # eax<- (Bmsw*Alsw)
    addl      %eax,%ecx           # ecx<- (Amsw*Blsw)+(Bmsw*Alsw)
    movl      (rFP),%eax          # eax<- Blsw
    mull      (rIBASE)            # eax<- (Blsw*Alsw)
    jmp       .L${opcode}_continue
%break

.L${opcode}_continue:
    leal      (%ecx,%edx),%edx    # full result now in %edx:%eax
    movl      %edx,4(rIBASE)      # v[A+1]<- %edx
    UNSPILL(rPC)                  # restore rPC/%edx
    FETCH_INST_WORD(1)
    movl      %eax,(rIBASE)       # v[A]<- %eax
    UNSPILL(rFP)
    UNSPILL(rIBASE)
    ADVANCE_PC(1)
    GOTO_NEXT
