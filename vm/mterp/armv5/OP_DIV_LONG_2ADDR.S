%verify "executed"
%include "armv5/binopWide2addr.S" {"instr":"bl      __aeabi_ldivmod", "chkzero":"1"}
