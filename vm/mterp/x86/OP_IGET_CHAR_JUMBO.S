%verify "executed"
%verify "large values are not sign-extended"
%include "x86/OP_IGET_JUMBO.S" { "load":"movzwl", "sqnum":"3" }
