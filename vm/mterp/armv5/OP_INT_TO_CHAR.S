%verify "executed"
%include "armv5/unop.S" {"preinstr":"mov     r0, r0, asl #16", "instr":"mov     r0, r0, lsr #16"}
