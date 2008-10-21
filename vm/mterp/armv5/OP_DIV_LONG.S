%verify "executed"
%include "armv5/binopWide.S" {"instr":"bl      __aeabi_ldivmod", "chkzero":"1"}
