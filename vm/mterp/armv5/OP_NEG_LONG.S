%verify "executed"
%include "armv5/unopWide.S" {"preinstr":"rsbs    r0, r0, #0", "instr":"rsc     r1, r1, #0"}
