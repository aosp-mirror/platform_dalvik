%verify "executed"
@include "armv5/OP_IGET.S" { "load":"ldrb", "sqnum":"1" }
%include "armv5/OP_IGET.S" { "load":"ldr", "sqnum":"1" }
