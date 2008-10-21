%verify "executed"
%include "armv5/binopWide.S" {"preinstr":"and     r0, r0, r2", "instr":"and     r1, r1, r3"}
