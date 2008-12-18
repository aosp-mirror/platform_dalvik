%verify "executed"
%include "x86/binopWide.S" {"instr1":"andl (rFP,%ecx,4),rPC", "instr2":"andl 4(rFP,%ecx,4),%eax"}
