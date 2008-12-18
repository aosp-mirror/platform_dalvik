%verify "executed"
%include "x86/OP_APUT.S" { "reg":"%cl", "store":"movb", "shift":"1" }
