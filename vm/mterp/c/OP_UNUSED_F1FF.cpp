HANDLE_OPCODE(OP_UNUSED_F1FF)
    /*
     * In portable interp, most unused opcodes will fall through to here.
     */
    LOGE("unknown opcode 0x%04x", inst);
    dvmAbort();
    FINISH(1);
OP_END
