%verify "executed"
/* EABI doesn't define a double remainder function, but libm does */
%include "armv5/binopWide2addr.S" {"instr":"bl      fmod"}
