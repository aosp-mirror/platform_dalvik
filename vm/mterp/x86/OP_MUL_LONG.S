%verify "executed"
    /*
     * Signed 64-bit integer multiply.
     *
     * We could definately use more free registers for
     * this code.  We must spill rPC (edx) because it
     * is used by imul.  We'll also spill rINST (ebx),
     * giving us eax, ebc, ecx and edx as computational
     * temps.  On top of that, we'll spill rIBASE (edi)
     * for use as the vB pointer and rFP (esi) for use
     * as the vC pointer.  Yuck.
     */
    /* mul-long vAA, vBB, vCC */
    movzbl    2(rPC),%eax              # eax<- B
    movzbl    3(rPC),%ecx              # ecx<- C
    SPILL(rPC)
    SPILL(rIBASE)
    SPILL(rFP)
    SPILL(rINST_FULL)
    leal      (rFP,%eax,4),rIBASE      # rIBASE<- &v[B]
    leal      (rFP,%ecx,4),rFP         # rFP<- &v[C]
    movl      4(rIBASE),%ecx      # ecx<- Bmsw
    imull     (rFP),%ecx          # ecx<- (Bmsw*Clsw)
    movl      4(rFP),%eax         # eax<- Cmsw
    imull     (rIBASE),%eax       # eax<- (Cmsw*Blsw)
    addl      %eax,%ecx           # ecx<- (Bmsw*Clsw)+(Cmsw*Blsw)
    movl      (rFP),%eax          # eax<- Clsw
    mull      (rIBASE)            # eax<- (Clsw*Alsw)
    UNSPILL(rINST_FULL)
    UNSPILL(rFP)
    jmp       .L${opcode}_continue
%break

.L${opcode}_continue:
    leal      (%ecx,%edx),%edx    # full result now in %edx:%eax
    movzbl    rINST_HI,%ecx       # ecx<- A
    movl      %edx,4(rFP,%ecx,4)  # v[B+1]<- %edx
    UNSPILL(rPC)                  # restore rPC/%edx
    FETCH_INST_WORD(2)
    UNSPILL(rIBASE)
    movl      %eax,(rFP,%ecx,4)   # v[B]<- %eax
    ADVANCE_PC(2)
    GOTO_NEXT
