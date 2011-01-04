%verify "executed"
@include "armv5te/OP_IPUT_JUMBO.S" { "store":"strh", "sqnum":"4" }
%include "armv5te/OP_IPUT_JUMBO.S" { "store":"str", "sqnum":"4" }
