%verify "executed"
%include "armv5te/OP_IPUT_JUMBO.S"  {"prebarrier":"SMP_DMB_ST", "postbarrier":"SMP_DMB"}
