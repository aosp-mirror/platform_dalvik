HANDLE_OPCODE(OP_THROW_VERIFICATION_ERROR)
    vsrc1 = INST_AA(inst);
    ref = FETCH(1);             /* class/field/method ref */
    dvmThrowVerificationError(methodClassDex, vsrc1, ref);
    GOTO_exceptionThrown();
OP_END
