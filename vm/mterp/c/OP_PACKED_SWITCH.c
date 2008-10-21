HANDLE_OPCODE(OP_PACKED_SWITCH /*vAA, +BBBB*/)
    {
        const u2* switchData;
        u4 testVal;
        s4 offset;

        vsrc1 = INST_AA(inst);
        offset = FETCH(1) | (((s4) FETCH(2)) << 16);
        ILOGV("|packed-switch v%d +0x%04x", vsrc1, vsrc2);
        switchData = pc + offset;       // offset in 16-bit units
#ifndef NDEBUG
        if (switchData < method->insns ||
            switchData >= method->insns + dvmGetMethodInsnsSize(method))
        {
            /* should have been caught in verifier */
            EXPORT_PC();
            dvmThrowException("Ljava/lang/InternalError;", "bad packed switch");
            GOTO(exceptionThrown);
        }
#endif
        testVal = GET_REGISTER(vsrc1);

        offset = dvmInterpHandlePackedSwitch(switchData, testVal);
        ILOGV("> branch taken (0x%04x)\n", offset);
        if (offset <= 0)  /* uncommon */
            PERIODIC_CHECKS(kInterpEntryInstr, offset);
        FINISH(offset);
    }
OP_END
