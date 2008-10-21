%verify "executed"
%include "armv5/binopLit16.S" {"instr":"bl     __aeabi_idiv","chkzero":"1"}
