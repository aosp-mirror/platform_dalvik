%verify "executed"
@include "armv5te/OP_IPUT_JUMBO.S" { "store":"strh", "sqnum":"3" }
%include "armv5te/OP_IPUT_JUMBO.S" { "store":"str", "sqnum":"3" }
