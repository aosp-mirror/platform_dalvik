HANDLE_OPCODE(OP_CHECK_CAST /*vAA, class@BBBB*/)
    {
        ClassObject* clazz;
        Object* obj;

        EXPORT_PC();

        vsrc1 = INST_AA(inst);
        ref = FETCH(1);         /* class to check against */
        ILOGV("|check-cast v%d,class@0x%04x", vsrc1, ref);

        obj = (Object*)GET_REGISTER(vsrc1);
        if (obj != NULL) {
#if defined(WITH_EXTRA_OBJECT_VALIDATION)
            if (!checkForNull(obj))
                GOTO(exceptionThrown);
#endif
            clazz = dvmDexGetResolvedClass(methodClassDex, ref);
            if (clazz == NULL) {
                clazz = dvmResolveClass(method->clazz, ref, false);
                if (clazz == NULL)
                    GOTO(exceptionThrown);
            }
            if (!dvmInstanceof(obj->clazz, clazz)) {
                dvmThrowExceptionWithClassMessage(
                    "Ljava/lang/ClassCastException;", obj->clazz->descriptor);
                GOTO(exceptionThrown);
            }
        }
    }
    FINISH(2);
OP_END
