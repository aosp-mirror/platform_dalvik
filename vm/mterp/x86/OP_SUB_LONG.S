%verify "executed"
%include "x86/binopWide.S" {"instr1":"subl (rFP,%ecx,4),rPC", "instr2":"sbbl 4(rFP,%ecx,4),%eax"}
