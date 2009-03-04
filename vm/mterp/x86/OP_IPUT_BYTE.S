%verify "executed"
%include "x86/OP_IPUT.S" { "store":"movb", "reg":"rINST_LO", "sqnum":"2" }
