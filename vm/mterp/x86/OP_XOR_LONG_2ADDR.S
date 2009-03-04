%verify "executed"
%include "x86/binopWide2addr.S" {"instr1":"xorl %eax,(rFP,rINST_FULL,4)","instr2":"xorl %ecx,4(rFP,rINST_FULL,4)"}
