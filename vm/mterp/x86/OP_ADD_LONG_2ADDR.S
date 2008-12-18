%verify "executed"
%include "x86/binopWide2addr.S" {"instr1":"addl %eax,(rFP,rINST_FULL,4)","instr2":"adcl %ecx,4(rFP,rINST_FULL,4)"}
