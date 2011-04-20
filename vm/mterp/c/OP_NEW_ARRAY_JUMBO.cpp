HANDLE_OPCODE(OP_NEW_ARRAY_JUMBO /*vBBBB, vCCCC, class@AAAAAAAA*/)
    {
        ClassObject* arrayClass;
        ArrayObject* newArray;
        s4 length;

        EXPORT_PC();

        ref = FETCH(1) | (u4)FETCH(2) << 16;
        vdst = FETCH(3);
        vsrc1 = FETCH(4);       /* length reg */
        ILOGV("|new-array/jumbo v%d,v%d,class@0x%08x  (%d elements)",
            vdst, vsrc1, ref, (s4) GET_REGISTER(vsrc1));
        length = (s4) GET_REGISTER(vsrc1);
        if (length < 0) {
            dvmThrowNegativeArraySizeException(length);
            GOTO_exceptionThrown();
        }
        arrayClass = dvmDexGetResolvedClass(methodClassDex, ref);
        if (arrayClass == NULL) {
            arrayClass = dvmResolveClass(curMethod->clazz, ref, false);
            if (arrayClass == NULL)
                GOTO_exceptionThrown();
        }
        /* verifier guarantees this is an array class */
        assert(dvmIsArrayClass(arrayClass));
        assert(dvmIsClassInitialized(arrayClass));

        newArray = dvmAllocArrayByClass(arrayClass, length, ALLOC_DONT_TRACK);
        if (newArray == NULL)
            GOTO_exceptionThrown();
        SET_REGISTER(vdst, (u4) newArray);
    }
    FINISH(5);
OP_END
