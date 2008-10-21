%verify "executed"
/* EABI doesn't define a float remainder function, but libm does */
%include "armv5/binop.S" {"instr":"bl      fmodf"}
