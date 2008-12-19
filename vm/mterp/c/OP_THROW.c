HANDLE_OPCODE(OP_THROW /*vAA*/)
    {
        Object* obj;

        vsrc1 = INST_AA(inst);
        ILOGV("|throw v%d  (%p)", vsrc1, (void*)GET_REGISTER(vsrc1));
        obj = (Object*) GET_REGISTER(vsrc1);
        if (!checkForNullExportPC(obj, fp, pc)) {
            /* will throw a null pointer exception */
            LOGVV("Bad exception\n");
        } else {
            /* use the requested exception */
            dvmSetException(self, obj);
        }
        GOTO_exceptionThrown();
    }
OP_END
