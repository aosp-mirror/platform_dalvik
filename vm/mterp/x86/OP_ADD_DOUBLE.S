%verify "executed"
%include "x86/binflop.S" {"instr":"faddl","load":"fldl","store":"fstpl"}
