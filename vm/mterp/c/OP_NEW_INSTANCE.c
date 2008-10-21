HANDLE_OPCODE(OP_NEW_INSTANCE /*vAA, class@BBBB*/)
    {
        ClassObject* clazz;
        Object* newObj;

        EXPORT_PC();

        vdst = INST_AA(inst);
        ref = FETCH(1);
        ILOGV("|new-instance v%d,class@0x%04x", vdst, ref);
        clazz = dvmDexGetResolvedClass(methodClassDex, ref);
        if (clazz == NULL) {
            clazz = dvmResolveClass(method->clazz, ref, false);
            if (clazz == NULL)
                GOTO(exceptionThrown);
        }

        if (!dvmIsClassInitialized(clazz) && !dvmInitClass(clazz))
            GOTO(exceptionThrown);

        /*
         * Note: the verifier can ensure that this never happens, allowing us
         * to remove the check.  However, the spec requires we throw the
         * exception at runtime, not verify time, so the verifier would
         * need to replace the new-instance call with a magic "throw
         * InstantiationError" instruction.
         *
         * Since this relies on the verifier, which is optional, we would
         * also need a "new-instance-quick" instruction to identify instances
         * that don't require the check.
         */
        if (dvmIsInterfaceClass(clazz) || dvmIsAbstractClass(clazz)) {
            dvmThrowExceptionWithClassMessage("Ljava/lang/InstantiationError;",
                clazz->descriptor);
            GOTO(exceptionThrown);
        }
        newObj = dvmAllocObject(clazz, ALLOC_DONT_TRACK);
        if (newObj == NULL)
            GOTO(exceptionThrown);
        SET_REGISTER(vdst, (u4) newObj);
    }
    FINISH(2);
OP_END
