%verify "executed"
%include "x86/binflop.S" {"instr":"fsubl","load":"fldl","store":"fstpl"}
