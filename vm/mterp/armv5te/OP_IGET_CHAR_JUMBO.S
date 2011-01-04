%verify "executed"
%verify "large values are not sign-extended"
@include "armv5te/OP_IGET_JUMBO.S" { "load":"ldrh", "sqnum":"3" }
%include "armv5te/OP_IGET_JUMBO.S" { "load":"ldr", "sqnum":"3" }
