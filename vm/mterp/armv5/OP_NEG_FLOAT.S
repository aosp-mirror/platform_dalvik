%verify "executed"
%include "armv5/unop.S" {"instr":"add     r0, r0, #0x80000000"}
