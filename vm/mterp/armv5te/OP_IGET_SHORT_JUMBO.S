%verify "executed"
%verify "negative value is sign-extended"
@include "armv5te/OP_IGET_JUMBO.S" { "load":"ldrsh", "sqnum":"4" }
%include "armv5te/OP_IGET_JUMBO.S" { "load":"ldr", "sqnum":"4" }
