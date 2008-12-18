HANDLE_OPCODE(OP_MONITOR_ENTER /*vAA*/)
    {
        Object* obj;

        vsrc1 = INST_AA(inst);
        ILOGV("|monitor-enter v%d %s(0x%08x)",
            vsrc1, kSpacing+6, GET_REGISTER(vsrc1));
        obj = (Object*)GET_REGISTER(vsrc1);
        if (!checkForNullExportPC(obj, fp, pc))
            GOTO_exceptionThrown();
        ILOGV("+ locking %p %s\n", obj, obj->clazz->descriptor);
#ifdef WITH_MONITOR_TRACKING
        EXPORT_PC();        /* need for stack trace */
#endif
        dvmLockObject(self, obj);
#ifdef WITH_DEADLOCK_PREDICTION
        if (dvmCheckException(self))
            GOTO_exceptionThrown();
#endif
    }
    FINISH(1);
OP_END
