%verify "executed"
@include "armv5te/OP_IPUT_JUMBO.S" { "store":"strb", "sqnum":"1" }
%include "armv5te/OP_IPUT_JUMBO.S" { "store":"str", "sqnum":"1" }
