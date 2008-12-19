%verify "executed"
%include "x86/binflop2addr.S" {"instr":"fmull","load":"fldl","store":"fstpl"}
