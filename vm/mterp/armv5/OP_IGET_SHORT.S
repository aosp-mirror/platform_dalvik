%verify "executed"
%verify "negative value is sign-extended"
@include "armv5/OP_IGET.S" { "load":"ldrsh", "sqnum":"4" }
%include "armv5/OP_IGET.S" { "load":"ldr", "sqnum":"4" }
