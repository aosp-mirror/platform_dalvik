HANDLE_OPCODE(OP_INVOKE_OBJECT_INIT /*vB, {vD, vE, vF, vG, vA}, meth@CCCC*/)
#if INTERP_TYPE != INTERP_DBG
    //LOGI("Ignoring empty\n");
    FINISH(3);
#else
    if (!DEBUGGER_ACTIVE) {
        //LOGI("Skipping empty\n");
        FINISH(3);      // don't want it to show up in profiler output
    } else {
        //LOGI("Running empty\n");
        /* fall through to OP_INVOKE_DIRECT */
        GOTO_invoke(invokeDirect, false, false);
    }
#endif
OP_END
