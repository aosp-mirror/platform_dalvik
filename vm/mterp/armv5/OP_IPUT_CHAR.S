%verify "executed"
@include "armv5/OP_IPUT.S" { "store":"strh", "sqnum":"3" }
%include "armv5/OP_IPUT.S" { "store":"str", "sqnum":"3" }
