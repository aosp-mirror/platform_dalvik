%verify "executed"
%include "x86/binflop.S" {"instr":"fmull","load":"fldl","store":"fstpl"}
