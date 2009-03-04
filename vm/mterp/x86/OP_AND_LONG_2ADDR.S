%verify "executed"
%include "x86/binopWide2addr.S" {"instr1":"andl %eax,(rFP,rINST_FULL,4)","instr2":"andl %ecx,4(rFP,rINST_FULL,4)"}
