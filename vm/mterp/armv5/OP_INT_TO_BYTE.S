%verify "executed"
%include "armv5/unop.S" {"preinstr":"mov     r0, r0, asl #24", "instr":"mov     r0, r0, asr #24"}
