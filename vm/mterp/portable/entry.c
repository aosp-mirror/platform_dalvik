/*
 * Main interpreter loop.
 *
 * This was written with an ARM implementation in mind.
 */
bool INTERP_FUNC_NAME(Thread* self, InterpState* interpState)
{
#if defined(EASY_GDB)
    StackSaveArea* debugSaveArea = SAVEAREA_FROM_FP(self->curFrame);
#endif
#if INTERP_TYPE == INTERP_DBG
    bool debugIsMethodEntry = interpState->debugIsMethodEntry;
#endif
#if defined(WITH_TRACKREF_CHECKS)
    int debugTrackedRefStart = interpState->debugTrackedRefStart;
#endif
    DvmDex* methodClassDex;     // curMethod->clazz->pDvmDex
    JValue retval;

    /* core state */
    const Method* curMethod;    // method we're interpreting
    const u2* pc;               // program counter
    u4* fp;                     // frame pointer
    u2 inst;                    // current instruction
    /* instruction decoding */
    u2 ref;                     // 16-bit quantity fetched directly
    u2 vsrc1, vsrc2, vdst;      // usually used for register indexes
    /* method call setup */
    const Method* methodToCall;
    bool methodCallRange;


#if defined(THREADED_INTERP)
    /* static computed goto table */
    DEFINE_GOTO_TABLE(handlerTable);
#endif

#if defined(WITH_JIT)
#if 0
    LOGD("*DebugInterp - entrypoint is %d, tgt is 0x%x, %s\n",
         interpState->entryPoint,
         interpState->pc,
         interpState->method->name);
#endif

#if INTERP_TYPE == INTERP_DBG
    /* Check to see if we've got a trace selection request.  If we do,
     * but something is amiss, revert to the fast interpreter.
     */
    if (dvmJitCheckTraceRequest(self,interpState)) {
        interpState->nextMode = INTERP_STD;
        //LOGD("** something wrong, exiting\n");
        return true;
    }
#endif
#endif

    /* copy state in */
    curMethod = interpState->method;
    pc = interpState->pc;
    fp = interpState->fp;
    retval = interpState->retval;   /* only need for kInterpEntryReturn? */

    methodClassDex = curMethod->clazz->pDvmDex;

    LOGVV("threadid=%d: entry(%s) %s.%s pc=0x%x fp=%p ep=%d\n",
        self->threadId, (interpState->nextMode == INTERP_STD) ? "STD" : "DBG",
        curMethod->clazz->descriptor, curMethod->name, pc - curMethod->insns,
        fp, interpState->entryPoint);

    /*
     * DEBUG: scramble this to ensure we're not relying on it.
     */
    methodToCall = (const Method*) -1;

#if INTERP_TYPE == INTERP_DBG
    if (debugIsMethodEntry) {
        ILOGD("|-- Now interpreting %s.%s", curMethod->clazz->descriptor,
                curMethod->name);
        DUMP_REGS(curMethod, interpState->fp, false);
    }
#endif

    switch (interpState->entryPoint) {
    case kInterpEntryInstr:
        /* just fall through to instruction loop or threaded kickstart */
        break;
    case kInterpEntryReturn:
        goto returnFromMethod;
    case kInterpEntryThrow:
        goto exceptionThrown;
    default:
        dvmAbort();
    }

#ifdef THREADED_INTERP
    FINISH(0);                  /* fetch and execute first instruction */
#else
    while (1) {
        CHECK_DEBUG_AND_PROF(); /* service debugger and profiling */
        CHECK_TRACKED_REFS();   /* check local reference tracking */

        /* fetch the next 16 bits from the instruction stream */
        inst = FETCH(0);

        switch (INST_INST(inst)) {
#endif

/*--- start of opcodes ---*/
