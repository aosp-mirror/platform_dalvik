HANDLE_OPCODE(OP_INSTANCE_OF_JUMBO /*vBBBB, vCCCC, class@AAAAAAAA*/)
    {
        ClassObject* clazz;
        Object* obj;

        ref = FETCH(1) | (u4)FETCH(2) << 16;     /* class to check against */
        vdst = FETCH(3);
        vsrc1 = FETCH(4);   /* object to check */
        ILOGV("|instance-of/jumbo v%d,v%d,class@0x%08x", vdst, vsrc1, ref);

        obj = (Object*)GET_REGISTER(vsrc1);
        if (obj == NULL) {
            SET_REGISTER(vdst, 0);
        } else {
#if defined(WITH_EXTRA_OBJECT_VALIDATION)
            if (!checkForNullExportPC(obj, fp, pc))
                GOTO_exceptionThrown();
#endif
            clazz = dvmDexGetResolvedClass(methodClassDex, ref);
            if (clazz == NULL) {
                EXPORT_PC();
                clazz = dvmResolveClass(curMethod->clazz, ref, true);
                if (clazz == NULL)
                    GOTO_exceptionThrown();
            }
            SET_REGISTER(vdst, dvmInstanceof(obj->clazz, clazz));
        }
    }
    FINISH(5);
OP_END
