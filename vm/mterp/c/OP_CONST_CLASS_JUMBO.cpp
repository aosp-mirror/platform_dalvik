HANDLE_OPCODE(OP_CONST_CLASS_JUMBO /*vBBBB, class@AAAAAAAA*/)
    {
        ClassObject* clazz;

        ref = FETCH(1) | (u4)FETCH(2) << 16;
        vdst = FETCH(3);
        ILOGV("|const-class/jumbo v%d class@0x%08x", vdst, ref);
        clazz = dvmDexGetResolvedClass(methodClassDex, ref);
        if (clazz == NULL) {
            EXPORT_PC();
            clazz = dvmResolveClass(curMethod->clazz, ref, true);
            if (clazz == NULL)
                GOTO_exceptionThrown();
        }
        SET_REGISTER(vdst, (u4) clazz);
    }
    FINISH(4);
OP_END
