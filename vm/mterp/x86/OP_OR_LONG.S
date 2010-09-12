%verify "executed"
%include "x86/binopWide.S" {"instr1":"orl (rFP,%ecx,4),%edx", "instr2":"orl 4(rFP,%ecx,4),%eax"}
