%verify "executed"
@include "armv5te/OP_IGET_JUMBO.S" { "load":"ldrb", "sqnum":"1" }
%include "armv5te/OP_IGET_JUMBO.S" { "load":"ldr", "sqnum":"1" }
