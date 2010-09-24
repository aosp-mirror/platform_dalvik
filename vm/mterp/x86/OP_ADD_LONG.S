%verify "executed"
%include "x86/binopWide.S" {"instr1":"addl (rFP,%ecx,4),%edx", "instr2":"adcl 4(rFP,%ecx,4),%eax"}
