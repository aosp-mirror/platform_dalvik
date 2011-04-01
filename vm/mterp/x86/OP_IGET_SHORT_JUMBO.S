%verify "executed"
%verify "negative value is sign-extended"
%include "x86/OP_IGET_JUMBO.S" { "load":"movswl", "sqnum":"4" }
