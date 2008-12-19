%verify "executed"
%include "x86/binflop2addr.S" {"instr":"fsubl","load":"fldl","store":"fstpl"}
