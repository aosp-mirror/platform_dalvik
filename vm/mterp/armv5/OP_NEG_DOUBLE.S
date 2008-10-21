%verify "executed"
%include "armv5/unopWide.S" {"instr":"add     r1, r1, #0x80000000"}
