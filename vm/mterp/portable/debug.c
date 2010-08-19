/* code in here is only included in portable-debug interpreter */

/*
 * Update the debugger on interesting events, such as hitting a breakpoint
 * or a single-step point.  This is called from the top of the interpreter
 * loop, before the current instruction is processed.
 *
 * Set "methodEntry" if we've just entered the method.  This detects
 * method exit by checking to see if the next instruction is "return".
 *
 * This can't catch native method entry/exit, so we have to handle that
 * at the point of invocation.  We also need to catch it in dvmCallMethod
 * if we want to capture native->native calls made through JNI.
 *
 * Notes to self:
 * - Don't want to switch to VMWAIT while posting events to the debugger.
 *   Let the debugger code decide if we need to change state.
 * - We may want to check for debugger-induced thread suspensions on
 *   every instruction.  That would make a "suspend all" more responsive
 *   and reduce the chances of multiple simultaneous events occurring.
 *   However, it could change the behavior some.
 *
 * TODO: method entry/exit events are probably less common than location
 * breakpoints.  We may be able to speed things up a bit if we don't query
 * the event list unless we know there's at least one lurking within.
 */
static void updateDebugger(const Method* method, const u2* pc, const u4* fp,
    bool methodEntry, Thread* self)
{
    int eventFlags = 0;

    /*
     * Update xtra.currentPc on every instruction.  We need to do this if
     * there's a chance that we could get suspended.  This can happen if
     * eventFlags != 0 here, or somebody manually requests a suspend
     * (which gets handled at PERIOD_CHECKS time).  One place where this
     * needs to be correct is in dvmAddSingleStep().
     */
    EXPORT_PC();

    if (methodEntry)
        eventFlags |= DBG_METHOD_ENTRY;

    /*
     * See if we have a breakpoint here.
     *
     * Depending on the "mods" associated with event(s) on this address,
     * we may or may not actually send a message to the debugger.
     */
    if (INST_INST(*pc) == OP_BREAKPOINT) {
        LOGV("+++ breakpoint hit at %p\n", pc);
        eventFlags |= DBG_BREAKPOINT;
    }

    /*
     * If the debugger is single-stepping one of our threads, check to
     * see if we're that thread and we've reached a step point.
     */
    const StepControl* pCtrl = &gDvm.stepControl;
    if (pCtrl->active && pCtrl->thread == self) {
        int frameDepth;
        bool doStop = false;
        const char* msg = NULL;

        assert(!dvmIsNativeMethod(method));

        if (pCtrl->depth == SD_INTO) {
            /*
             * Step into method calls.  We break when the line number
             * or method pointer changes.  If we're in SS_MIN mode, we
             * always stop.
             */
            if (pCtrl->method != method) {
                doStop = true;
                msg = "new method";
            } else if (pCtrl->size == SS_MIN) {
                doStop = true;
                msg = "new instruction";
            } else if (!dvmAddressSetGet(
                    pCtrl->pAddressSet, pc - method->insns)) {
                doStop = true;
                msg = "new line";
            }
        } else if (pCtrl->depth == SD_OVER) {
            /*
             * Step over method calls.  We break when the line number is
             * different and the frame depth is <= the original frame
             * depth.  (We can't just compare on the method, because we
             * might get unrolled past it by an exception, and it's tricky
             * to identify recursion.)
             */
            frameDepth = dvmComputeVagueFrameDepth(self, fp);
            if (frameDepth < pCtrl->frameDepth) {
                /* popped up one or more frames, always trigger */
                doStop = true;
                msg = "method pop";
            } else if (frameDepth == pCtrl->frameDepth) {
                /* same depth, see if we moved */
                if (pCtrl->size == SS_MIN) {
                    doStop = true;
                    msg = "new instruction";
                } else if (!dvmAddressSetGet(pCtrl->pAddressSet,
                            pc - method->insns)) {
                    doStop = true;
                    msg = "new line";
                }
            }
        } else {
            assert(pCtrl->depth == SD_OUT);
            /*
             * Return from the current method.  We break when the frame
             * depth pops up.
             *
             * This differs from the "method exit" break in that it stops
             * with the PC at the next instruction in the returned-to
             * function, rather than the end of the returning function.
             */
            frameDepth = dvmComputeVagueFrameDepth(self, fp);
            if (frameDepth < pCtrl->frameDepth) {
                doStop = true;
                msg = "method pop";
            }
        }

        if (doStop) {
            LOGV("#####S %s\n", msg);
            eventFlags |= DBG_SINGLE_STEP;
        }
    }

    /*
     * Check to see if this is a "return" instruction.  JDWP says we should
     * send the event *after* the code has been executed, but it also says
     * the location we provide is the last instruction.  Since the "return"
     * instruction has no interesting side effects, we should be safe.
     * (We can't just move this down to the returnFromMethod label because
     * we potentially need to combine it with other events.)
     *
     * We're also not supposed to generate a method exit event if the method
     * terminates "with a thrown exception".
     */
    u2 inst = INST_INST(FETCH(0));
    if (inst == OP_RETURN_VOID || inst == OP_RETURN || inst == OP_RETURN_WIDE ||
        inst == OP_RETURN_OBJECT)
    {
        eventFlags |= DBG_METHOD_EXIT;
    }

    /*
     * If there's something interesting going on, see if it matches one
     * of the debugger filters.
     */
    if (eventFlags != 0) {
        Object* thisPtr = dvmGetThisPtr(method, fp);
        if (thisPtr != NULL && !dvmIsValidObject(thisPtr)) {
            /*
             * TODO: remove this check if we're confident that the "this"
             * pointer is where it should be -- slows us down, especially
             * during single-step.
             */
            char* desc = dexProtoCopyMethodDescriptor(&method->prototype);
            LOGE("HEY: invalid 'this' ptr %p (%s.%s %s)\n", thisPtr,
                method->clazz->descriptor, method->name, desc);
            free(desc);
            dvmAbort();
        }
        dvmDbgPostLocationEvent(method, pc - method->insns, thisPtr,
            eventFlags);
    }
}

/*
 * Perform some operations at the "top" of the interpreter loop.
 * This stuff is required to support debugging and profiling.
 *
 * Using" __attribute__((noinline))" seems to do more harm than good.  This
 * is best when inlined due to the large number of parameters, most of
 * which are local vars in the main interp loop.
 */
static void checkDebugAndProf(const u2* pc, const u4* fp, Thread* self,
    const Method* method, bool* pIsMethodEntry)
{
    /* check to see if we've run off end of method */
    assert(pc >= method->insns && pc <
            method->insns + dvmGetMethodInsnsSize(method));

#if 0
    /*
     * When we hit a specific method, enable verbose instruction logging.
     * Sometimes it's helpful to use the debugger attach as a trigger too.
     */
    if (*pIsMethodEntry) {
        static const char* cd = "Landroid/test/Arithmetic;";
        static const char* mn = "shiftTest2";
        static const char* sg = "()V";

        if (/*gDvm.debuggerActive &&*/
            strcmp(method->clazz->descriptor, cd) == 0 &&
            strcmp(method->name, mn) == 0 &&
            strcmp(method->shorty, sg) == 0)
        {
            LOGW("Reached %s.%s, enabling verbose mode\n",
                method->clazz->descriptor, method->name);
            android_setMinPriority(LOG_TAG"i", ANDROID_LOG_VERBOSE);
            dumpRegs(method, fp, true);
        }

        if (!gDvm.debuggerActive)
            *pIsMethodEntry = false;
    }
#endif

    /*
     * If the debugger is attached, check for events.  If the profiler is
     * enabled, update that too.
     *
     * This code is executed for every instruction we interpret, so for
     * performance we use a couple of #ifdef blocks instead of runtime tests.
     */
    bool isEntry = *pIsMethodEntry;
    if (isEntry) {
        *pIsMethodEntry = false;
        TRACE_METHOD_ENTER(self, method);
    }
    if (gDvm.debuggerActive) {
        updateDebugger(method, pc, fp, isEntry, self);
    }
    if (gDvm.instructionCountEnableCount != 0) {
        /*
         * Count up the #of executed instructions.  This isn't synchronized
         * for thread-safety; if we need that we should make this
         * thread-local and merge counts into the global area when threads
         * exit (perhaps suspending all other threads GC-style and pulling
         * the data out of them).
         */
        int inst = *pc & 0xff;
        gDvm.executedInstrCounts[inst]++;
    }
}
