HANDLE_OPCODE(OP_CHECK_CAST_JUMBO /*vBBBB, class@AAAAAAAA*/)
    {
        ClassObject* clazz;
        Object* obj;

        EXPORT_PC();

        ref = FETCH(1) | (u4)FETCH(2) << 16;     /* class to check against */
        vsrc1 = FETCH(3);
        ILOGV("|check-cast/jumbo v%d,class@0x%08x", vsrc1, ref);

        obj = (Object*)GET_REGISTER(vsrc1);
        if (obj != NULL) {
#if defined(WITH_EXTRA_OBJECT_VALIDATION)
            if (!checkForNull(obj))
                GOTO_exceptionThrown();
#endif
            clazz = dvmDexGetResolvedClass(methodClassDex, ref);
            if (clazz == NULL) {
                clazz = dvmResolveClass(curMethod->clazz, ref, false);
                if (clazz == NULL)
                    GOTO_exceptionThrown();
            }
            if (!dvmInstanceof(obj->clazz, clazz)) {
                dvmThrowClassCastException(obj->clazz, clazz);
                GOTO_exceptionThrown();
            }
        }
    }
    FINISH(4);
OP_END
