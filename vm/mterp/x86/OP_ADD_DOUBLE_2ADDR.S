%verify "executed"
%include "x86/binflop2addr.S" {"instr":"faddl","load":"fldl","store":"fstpl"}
