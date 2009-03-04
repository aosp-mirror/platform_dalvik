%verify "executed"
%include "x86/OP_APUT.S" { "reg":"%cx", "store":"movw", "shift":"2" }
