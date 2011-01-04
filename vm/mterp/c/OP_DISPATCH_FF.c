HANDLE_OPCODE(OP_DISPATCH_FF)
    /*
     * Indicates extended opcode.  Use next 8 bits to choose where to branch.
     */
    DISPATCH_EXTENDED(INST_AA(inst));
OP_END
