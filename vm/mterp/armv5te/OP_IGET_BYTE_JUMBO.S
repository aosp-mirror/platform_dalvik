%verify "executed"
%verify "negative value is sign-extended"
@include "armv5te/OP_IGET_JUMBO.S" { "load":"ldrsb", "sqnum":"2" }
%include "armv5te/OP_IGET_JUMBO.S" { "load":"ldr", "sqnum":"2" }
