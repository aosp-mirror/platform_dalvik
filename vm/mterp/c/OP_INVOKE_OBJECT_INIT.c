HANDLE_OPCODE(OP_INVOKE_OBJECT_INIT /*vB, {vD, vE, vF, vG, vA}, meth@CCCC*/)
    {
        Object* obj;

        vsrc1 = FETCH(2) & 0x0f;        /* reg number of "this" pointer */
        obj = GET_REGISTER_AS_OBJECT(vsrc1);

        if (!checkForNullExportPC(obj, fp, pc))
            GOTO_exceptionThrown();

        /*
         * The object should be marked "finalizable" when Object.<init>
         * completes normally.  We're going to assume it does complete
         * (by virtue of being nothing but a return-void) and set it now.
         */
        if (IS_CLASS_FLAG_SET(obj->clazz, CLASS_ISFINALIZABLE)) {
            dvmSetFinalizable(obj);
        }

#if INTERP_TYPE == INTERP_DBG
        if (!DEBUGGER_ACTIVE) {
            /* skip method invocation */
            FINISH(3);
        } else {
            /* behave like OP_INVOKE_DIRECT */
            GOTO_invoke(invokeDirect, false, false);
        }
#else
        /* debugger can't be attached, skip method invocation */
        FINISH(3);
#endif
    }
OP_END
