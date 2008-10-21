%verify "executed"
%include "armv5/binopLit8.S" {"instr":"bl     __aeabi_idiv","chkzero":"1"}
