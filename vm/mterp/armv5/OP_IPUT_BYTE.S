%verify "executed"
@include "armv5/OP_IPUT.S" { "store":"strb", "sqnum":"2" }
%include "armv5/OP_IPUT.S" { "store":"str", "sqnum":"2" }
