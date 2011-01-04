%verify "executed"
%include "x86/OP_IPUT_JUMBO.S" { "store":"movw", "reg":"rINSTw", "sqnum":"3" }
