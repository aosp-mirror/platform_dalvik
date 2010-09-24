%verify "executed"
    FETCH_INST_OPCODE 1 %edx
    ADVANCE_PC 1
    GOTO_NEXT_R %edx
