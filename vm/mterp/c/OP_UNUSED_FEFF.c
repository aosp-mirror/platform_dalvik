HANDLE_OPCODE(OP_UNUSED_FEFF)
  /*
   * In portable interp, most unused opcodes will fall through to here.
   */
  LOGE("unknown opcode 0x%04x\n", INST_INST(inst));
  dvmAbort();
  FINISH(1);
OP_END
