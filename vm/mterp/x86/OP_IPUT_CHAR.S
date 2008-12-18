%verify "executed"
%include "x86/OP_IPUT.S" { "store":"movw", "reg":"rINST", "sqnum":"3" }
