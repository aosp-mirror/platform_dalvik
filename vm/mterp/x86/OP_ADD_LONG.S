%verify "executed"
%include "x86/binopWide.S" {"instr1":"addl (rFP,%ecx,4),rPC", "instr2":"adcl 4(rFP,%ecx,4),%eax"}
