HANDLE_OPCODE(OP_FILL_ARRAY_DATA)   /*vAA, +BBBBBBBB*/
    {
        const u2* arrayData;
        s4 offset;
        ArrayObject* arrayObj;

        EXPORT_PC();
        vsrc1 = INST_AA(inst);
        offset = FETCH(1) | (((s4) FETCH(2)) << 16);
        ILOGV("|fill-array-data v%d +0x%04x", vsrc1, offset);
        arrayData = pc + offset;       // offset in 16-bit units
#ifndef NDEBUG
        if (arrayData < method->insns ||
            arrayData >= method->insns + dvmGetMethodInsnsSize(method))
        {
            /* should have been caught in verifier */
            dvmThrowException("Ljava/lang/InternalError;", 
                              "bad fill array data");
            GOTO(exceptionThrown);
        }
#endif
        arrayObj = (ArrayObject*) GET_REGISTER(vsrc1);
        if (!dvmInterpHandleFillArrayData(arrayObj, arrayData)) {
            GOTO(exceptionThrown);
        }
        FINISH(3);
    }
OP_END
