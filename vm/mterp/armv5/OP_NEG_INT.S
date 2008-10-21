%verify "executed"
%include "armv5/unop.S" {"instr":"rsb     r0, r0, #0"}
