%verify "executed"
%include "x86/binopWide.S" {"instr1":"orl (rFP,%ecx,4),rPC", "instr2":"orl 4(rFP,%ecx,4),%eax"}
