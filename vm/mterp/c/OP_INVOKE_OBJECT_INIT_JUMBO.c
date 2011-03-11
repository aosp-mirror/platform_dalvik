HANDLE_OPCODE(OP_INVOKE_OBJECT_INIT_JUMBO /*{vCCCC..vNNNN}, meth@AAAAAAAA*/)
    {
        Object* obj;

        vsrc1 = FETCH(4);               /* reg number of "this" pointer */
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
        if (DEBUGGER_ACTIVE) {
            /* behave like OP_INVOKE_DIRECT_RANGE */
            GOTO_invoke(invokeDirect, true, true);
        }
#endif
        FINISH(5);
    }
OP_END
