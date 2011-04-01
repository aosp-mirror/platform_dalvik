HANDLE_OPCODE(OP_THROW_VERIFICATION_ERROR_JUMBO)
    EXPORT_PC();
    vsrc1 = FETCH(3);
    ref = FETCH(1) | (u4)FETCH(2) << 16;      /* class/field/method ref */
    dvmThrowVerificationError(curMethod, vsrc1, ref);
    GOTO_exceptionThrown();
OP_END
