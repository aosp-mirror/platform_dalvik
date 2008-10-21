/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * Main interpreter loop.  This is in an include file so that we can
 * generate multiple versions (debug, nondebug).
 *
 * For thread suspension, the plan is to check for thread suspension on
 * backward branches, exceptions, and method returns, the theory being that
 * a given code path will do one of these in short order.
 *
 * The INTERP_TYPE preprocessing variable will be set to INTERP_STD or
 * INTERP_DBG.  If it's set to INTERP_DBG, we know that either WITH_DEBUGGER
 * or WITH_PROFILING is set; if neither is set, the debug version of the
 * interpreter is simply not built.
 *
 * TODO: now that we have "mterp", this should be generated from the mterp
 * sources, so that we don't have the two copies of the same code.  This
 * requires concatenating the various pieces together (as we would for the
 * C-stub-only form of mterp), but combining everything into a single large
 * function.  We also need to merge some debugger support functions in,
 * which requires some additional work because the mterp C code doesn't
 * presently have that.
 */
#include "Dalvik.h"
#include "interp/InterpDefs.h"

#include <stdlib.h>
#include <math.h>


/*
 * Periodically check for thread suspension.
 *
 * While we're at it, see if a debugger has attached or the profiler has
 * started.  If so, switch to a different "goto" table.
 */
#define PERIODIC_CHECKS(_entryPoint, _pcadj) {                              \
        dvmCheckSuspendQuick(self);                                         \
        if (NEED_INTERP_SWITCH(INTERP_TYPE)) {                              \
            ADJUST_PC(_pcadj);                                              \
            interpState->entryPoint = _entryPoint;                          \
            LOGVV("threadid=%d: switch to %s ep=%d adj=%d\n",               \
                self->threadId,                                             \
                (interpState->nextMode == INTERP_STD) ? "STD" : "DBG",      \
                (_entryPoint), (_pcadj));                                   \
            goto bail_switch;                                               \
        }                                                                   \
    }



#if INTERP_TYPE == INTERP_DBG
/* code in here is only included if profiling or debugging is enabled */

/*
 * Determine if an address is "interesting" to the debugger.  This allows
 * us to avoid scanning the entire event list before every instruction.
 *
 * The "debugBreakAddr" table is global and not synchronized.
 */
static bool isInterestingAddr(const u2* pc)
{
    const u2** ptr = gDvm.debugBreakAddr;
    int i;

    for (i = 0; i < MAX_BREAKPOINTS; i++, ptr++) {
        if (*ptr == pc) {
            LOGV("BKP: hit on %p\n", pc);
            return true;
        }
    }
    return false;
}

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
     *
     * Checking method->debugBreakpointCount is slower on the device than
     * just scanning the table (!).  We could probably work something out
     * where we just check it on method entry/exit and remember the result,
     * but that's more fragile and requires passing more stuff around.
     */
#ifdef WITH_DEBUGGER
    if (method->debugBreakpointCount > 0 && isInterestingAddr(pc)) {
        eventFlags |= DBG_BREAKPOINT;
    }
#endif

    /*
     * If the debugger is single-stepping one of our threads, check to
     * see if we're that thread and we've reached a step point.
     */
    const StepControl* pCtrl = &gDvm.stepControl;
    if (pCtrl->active && pCtrl->thread == self) {
        int line, frameDepth;
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
            strcmp(method->signature, sg) == 0)
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
     * If the debugger is attached, check for events.
     *
     * This doesn't work quite right for "*pIsMethodEntry" if we're profiling
     * while the debugger is attached, but that's not very useful anyway.
     */
    if (gDvm.debuggerActive) {
        updateDebugger(method, pc, fp, *pIsMethodEntry, self);
        *pIsMethodEntry = false;
    }
#ifdef WITH_PROFILER
    else {
        if (*pIsMethodEntry) {
            TRACE_METHOD_ENTER(self, method);
            *pIsMethodEntry = false;
        }
    }
    if (gDvm.instructionCountEnableCount != 0) {
        /*
         * Count up the #of executed instructions.  This isn't synchronized
         * for thread-safety; if we need that we should make this
         * thread-local and merge counts into the global area when threads
         * exit (suspending all threads GC-style).
         */
        int inst = *pc & 0xff;
        gDvm.executedInstrCounts[inst]++;
    }
#endif
}

#endif /*INTERP_TYPE == INTERP_DBG*/



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
    DvmDex* methodClassDex;     // method->clazz->pDvmDex
    JValue retval;

    /* core state */
    const Method* method;       // method we're interpreting
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

    /* copy state in */
    method = interpState->method;
    pc = interpState->pc;
    fp = interpState->fp;
    retval = interpState->retval;   /* only need for kInterpEntryReturn? */

    methodClassDex = method->clazz->pDvmDex;

    LOGVV("threadid=%d: entry(%s) %s.%s pc=0x%x fp=%p ep=%d\n",
        self->threadId, (interpState->nextMode == INTERP_STD) ? "STD" : "DBG",
        method->clazz->descriptor, method->name, pc - method->insns, fp,
        interpState->entryPoint);

    /*
     * DEBUG: scramble this to ensure we're not relying on it.
     */
    methodToCall = (const Method*) -1;

#if INTERP_TYPE == INTERP_DBG
    if (debugIsMethodEntry) {
        ILOGD("|-- Now interpreting %s.%s", method->clazz->descriptor,
                method->name);
        DUMP_REGS(method, interpState->fp, false);
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


HANDLE_OPCODE(OP_NOP)
    FINISH(1);

HANDLE_OPCODE(OP_GOTO /*+AA*/)
    vdst = INST_AA(inst);
    if ((s1)vdst < 0)
        ILOGV("|goto -0x%02x", -((s1)vdst));
    else
        ILOGV("|goto +0x%02x", ((s1)vdst));
    ILOGV("> branch taken");
    if ((s1)vdst < 0)
        PERIODIC_CHECKS(kInterpEntryInstr, (s1)vdst);
    FINISH((s1)vdst);

HANDLE_OPCODE(OP_GOTO_16 /*+AAAA*/)
    {
        s4 offset = (s2) FETCH(1);          /* sign-extend next code unit */

        if (offset < 0)
            ILOGV("|goto/16 -0x%04x", -offset);
        else
            ILOGV("|goto/16 +0x%04x", offset);
        ILOGV("> branch taken");
        if (offset < 0)
            PERIODIC_CHECKS(kInterpEntryInstr, offset);
        FINISH(offset);
    }

HANDLE_OPCODE(OP_GOTO_32 /*+AAAAAAAA*/)
    {
        s4 offset = FETCH(1);               /* low-order 16 bits */
        offset |= ((s4) FETCH(2)) << 16;    /* high-order 16 bits */

        if (offset < 0)
            ILOGV("|goto/32 -0x%08x", -offset);
        else
            ILOGV("|goto/32 +0x%08x", offset);
        ILOGV("> branch taken");
        if (offset <= 0)    /* allowed to branch to self */
            PERIODIC_CHECKS(kInterpEntryInstr, offset);
        FINISH(offset);
    }

#define HANDLE_NUMCONV(_opcode, _opname, _fromtype, _totype)                \
    HANDLE_OPCODE(_opcode /*vA, vB*/)                                       \
        vdst = INST_A(inst);                                                \
        vsrc1 = INST_B(inst);                                               \
        ILOGV("|%s v%d,v%d", (_opname), vdst, vsrc1);                       \
        SET_REGISTER##_totype(vdst,                                         \
            GET_REGISTER##_fromtype(vsrc1));                                \
        FINISH(1);
HANDLE_NUMCONV(OP_INT_TO_LONG,          "int-to-long", _INT, _WIDE)
HANDLE_NUMCONV(OP_INT_TO_FLOAT,         "int-to-float", _INT, _FLOAT)
HANDLE_NUMCONV(OP_INT_TO_DOUBLE,        "int-to-double", _INT, _DOUBLE)
HANDLE_NUMCONV(OP_LONG_TO_INT,          "long-to-int", _WIDE, _INT)
HANDLE_NUMCONV(OP_LONG_TO_FLOAT,        "long-to-float", _WIDE, _FLOAT)
HANDLE_NUMCONV(OP_LONG_TO_DOUBLE,       "long-to-double", _WIDE, _DOUBLE)
HANDLE_NUMCONV(OP_FLOAT_TO_DOUBLE,      "float-to-double", _FLOAT, _DOUBLE)
HANDLE_NUMCONV(OP_DOUBLE_TO_FLOAT,      "double-to-float", _DOUBLE, _FLOAT)


#define HANDLE_FLOAT_TO_INT(_opcode, _opname, _fromvtype, _fromrtype,       \
        _tovtype, _tortype)                                                 \
    HANDLE_OPCODE(_opcode /*vA, vB*/)                                       \
    {                                                                       \
        /* spec defines specific handling for +/- inf and NaN values */     \
        _fromvtype val;                                                     \
        _tovtype intMin, intMax, result;                                    \
        vdst = INST_A(inst);                                                \
        vsrc1 = INST_B(inst);                                               \
        ILOGV("|%s v%d,v%d", (_opname), vdst, vsrc1);                       \
        val = GET_REGISTER##_fromrtype(vsrc1);                              \
        intMin = (_tovtype) 1 << (sizeof(_tovtype) * 8 -1);                 \
        intMax = ~intMin;                                                   \
        if (val >= intMax)          /* +inf */                              \
            result = intMax;                                                \
        else if (val <= intMin)     /* -inf */                              \
            result = intMin;                                                \
        else if (val != val)        /* NaN */                               \
            result = 0;                                                     \
        else                                                                \
            result = (_tovtype) val;                                        \
        SET_REGISTER##_tortype(vdst, result);                               \
    }                                                                       \
    FINISH(1);
HANDLE_FLOAT_TO_INT(OP_FLOAT_TO_INT,    "float-to-int",
    float, _FLOAT, s4, _INT)
HANDLE_FLOAT_TO_INT(OP_FLOAT_TO_LONG,   "float-to-long",
    float, _FLOAT, s8, _WIDE)
HANDLE_FLOAT_TO_INT(OP_DOUBLE_TO_INT,   "double-to-int",
    double, _DOUBLE, s4, _INT)
HANDLE_FLOAT_TO_INT(OP_DOUBLE_TO_LONG,  "double-to-long",
    double, _DOUBLE, s8, _WIDE)


#define HANDLE_INT_TO_SMALL(_opcode, _opname, _type)                        \
    HANDLE_OPCODE(_opcode /*vA, vB*/)                                       \
        vdst = INST_A(inst);                                                \
        vsrc1 = INST_B(inst);                                               \
        ILOGV("|int-to-%s v%d,v%d", (_opname), vdst, vsrc1);                \
        SET_REGISTER(vdst, (_type) GET_REGISTER(vsrc1));                    \
        FINISH(1);
HANDLE_INT_TO_SMALL(OP_INT_TO_BYTE,     "byte", s1)
HANDLE_INT_TO_SMALL(OP_INT_TO_CHAR,     "char", u2)
HANDLE_INT_TO_SMALL(OP_INT_TO_SHORT,    "short", s2)    /* want sign bit */


HANDLE_OPCODE(OP_MOVE_FROM16 /*vAA, vBBBB*/)
HANDLE_OPCODE(OP_MOVE_OBJECT_FROM16 /*vAA, vBBBB*/)
    vdst = INST_AA(inst);
    vsrc1 = FETCH(1);
    ILOGV("|move%s/from16 v%d,v%d %s(v%d=0x%08x)",
        (INST_INST(inst) == OP_MOVE_FROM16) ? "" : "-object", vdst, vsrc1,
        kSpacing, vdst, GET_REGISTER(vsrc1));
    SET_REGISTER(vdst, GET_REGISTER(vsrc1));
    FINISH(2);

HANDLE_OPCODE(OP_MOVE_16 /*vAAAA, vBBBB*/)
HANDLE_OPCODE(OP_MOVE_OBJECT_16 /*vAAAA, vBBBB*/)
    vdst = FETCH(1);
    vsrc1 = FETCH(2);
    ILOGV("|move%s/16 v%d,v%d %s(v%d=0x%08x)",
        (INST_INST(inst) == OP_MOVE_16) ? "" : "-object", vdst, vsrc1,
        kSpacing, vdst, GET_REGISTER(vsrc1));
    SET_REGISTER(vdst, GET_REGISTER(vsrc1));
    FINISH(3);

HANDLE_OPCODE(OP_MOVE_WIDE /*vA, vB*/)
    /* IMPORTANT: must correctly handle overlapping registers, e.g. both
     * "move-wide v6, v7" and "move-wide v7, v6" */
    vdst = INST_A(inst);
    vsrc1 = INST_B(inst);
    ILOGV("|move-wide v%d,v%d %s(v%d=0x%08llx)", vdst, vsrc1,
        kSpacing+5, vdst, GET_REGISTER_WIDE(vsrc1));
    SET_REGISTER_WIDE(vdst, GET_REGISTER_WIDE(vsrc1));
    FINISH(1);

HANDLE_OPCODE(OP_MOVE_WIDE_FROM16 /*vAA, vBBBB*/)
    vdst = INST_AA(inst);
    vsrc1 = FETCH(1);
    ILOGV("|move-wide/from16 v%d,v%d  (v%d=0x%08llx)", vdst, vsrc1,
        vdst, GET_REGISTER_WIDE(vsrc1));
    SET_REGISTER_WIDE(vdst, GET_REGISTER_WIDE(vsrc1));
    FINISH(2);

HANDLE_OPCODE(OP_MOVE_WIDE_16 /*vAAAA, vBBBB*/)
    vdst = FETCH(1);
    vsrc1 = FETCH(2);
    ILOGV("|move-wide/16 v%d,v%d %s(v%d=0x%08llx)", vdst, vsrc1,
        kSpacing+8, vdst, GET_REGISTER_WIDE(vsrc1));
    SET_REGISTER_WIDE(vdst, GET_REGISTER_WIDE(vsrc1));
    FINISH(3);

HANDLE_OPCODE(OP_MOVE_RESULT /*vAA*/)
HANDLE_OPCODE(OP_MOVE_RESULT_OBJECT /*vAA*/)
    vdst = INST_AA(inst);
    ILOGV("|move-result%s v%d %s(v%d=0x%08x)",
         (INST_INST(inst) == OP_MOVE_RESULT) ? "" : "-object",
         vdst, kSpacing+4, vdst,retval.i);
    SET_REGISTER(vdst, retval.i);
    FINISH(1);

HANDLE_OPCODE(OP_MOVE_RESULT_WIDE /*vAA*/)
    vdst = INST_AA(inst);
    ILOGV("|move-result-wide v%d %s(0x%08llx)", vdst, kSpacing, retval.j);
    SET_REGISTER_WIDE(vdst, retval.j);
    FINISH(1);

HANDLE_OPCODE(OP_MOVE_EXCEPTION /*vAA*/)
    vdst = INST_AA(inst);
    ILOGV("|move-exception v%d", vdst);
    assert(self->exception != NULL);
    SET_REGISTER(vdst, (u4)self->exception);
    dvmClearException(self);
    FINISH(1);

HANDLE_OPCODE(OP_MOVE /*vA, vB*/)
HANDLE_OPCODE(OP_MOVE_OBJECT /*vA, vB*/)
    vdst = INST_A(inst);
    vsrc1 = INST_B(inst);
    ILOGV("|move%s v%d,v%d %s(v%d=0x%08x)",
        (INST_INST(inst) == OP_MOVE) ? "" : "-object", vdst, vsrc1,
        kSpacing, vdst, GET_REGISTER(vsrc1));
    SET_REGISTER(vdst, GET_REGISTER(vsrc1));
    FINISH(1);

/* NOTE: the comparison result is always a signed 4-byte integer */
#define HANDLE_OP_CMPX(_opcode, _opname, _varType, _type, _nanVal)          \
    HANDLE_OPCODE(_opcode /*vAA, vBB, vCC*/)                                \
    {                                                                       \
        int result;                                                         \
        u2 regs;                                                            \
        _varType val1, val2;                                                \
        vdst = INST_AA(inst);                                               \
        regs = FETCH(1);                                                    \
        vsrc1 = regs & 0xff;                                                \
        vsrc2 = regs >> 8;                                                  \
        ILOGV("|cmp%s v%d,v%d,v%d", (_opname), vdst, vsrc1, vsrc2);         \
        val1 = GET_REGISTER##_type(vsrc1);                                  \
        val2 = GET_REGISTER##_type(vsrc2);                                  \
        if (val1 == val2)                                                   \
            result = 0;                                                     \
        else if (val1 < val2)                                               \
            result = -1;                                                    \
        else if (val1 > val2)                                               \
            result = 1;                                                     \
        else                                                                \
            result = (_nanVal);                                             \
        ILOGV("+ result=%d\n", result);                                     \
        SET_REGISTER(vdst, result);                                         \
    }                                                                       \
    FINISH(2);
HANDLE_OP_CMPX(OP_CMPL_FLOAT, "l-float", float, _FLOAT, -1)
HANDLE_OP_CMPX(OP_CMPG_FLOAT, "g-float", float, _FLOAT, 1)
HANDLE_OP_CMPX(OP_CMPL_DOUBLE, "l-double", double, _DOUBLE, -1)
HANDLE_OP_CMPX(OP_CMPG_DOUBLE, "g-double", double, _DOUBLE, 1)
HANDLE_OP_CMPX(OP_CMP_LONG, "-long", s8, _WIDE, 0)


#define HANDLE_OP_IF_XX(_opcode, _opname, _cmp)                             \
    HANDLE_OPCODE(_opcode /*vA, vB, +CCCC*/)                                \
        vsrc1 = INST_A(inst);                                               \
        vsrc2 = INST_B(inst);                                               \
        if ((s4) GET_REGISTER(vsrc1) _cmp (s4) GET_REGISTER(vsrc2)) {       \
            int branchOffset = (s2)FETCH(1);    /* sign-extended */         \
            ILOGV("|if-%s v%d,v%d,+0x%04x", (_opname), vsrc1, vsrc2,        \
                branchOffset);                                              \
            ILOGV("> branch taken");                                        \
            if (branchOffset < 0)                                           \
                PERIODIC_CHECKS(kInterpEntryInstr, branchOffset);           \
            FINISH(branchOffset);                                           \
        } else {                                                            \
            ILOGV("|if-%s v%d,v%d,-", (_opname), vsrc1, vsrc2);             \
            FINISH(2);                                                      \
        }
HANDLE_OP_IF_XX(OP_IF_EQ, "eq", ==)
HANDLE_OP_IF_XX(OP_IF_NE, "ne", !=)
HANDLE_OP_IF_XX(OP_IF_LT, "lt", <)
HANDLE_OP_IF_XX(OP_IF_GE, "ge", >=)
HANDLE_OP_IF_XX(OP_IF_GT, "gt", >)
HANDLE_OP_IF_XX(OP_IF_LE, "le", <=)


#define HANDLE_OP_IF_XXZ(_opcode, _opname, _cmp)                            \
    HANDLE_OPCODE(_opcode /*vAA, +BBBB*/)                                   \
        vsrc1 = INST_AA(inst);                                              \
        if ((s4) GET_REGISTER(vsrc1) _cmp 0) {                              \
            int branchOffset = (s2)FETCH(1);    /* sign-extended */         \
            ILOGV("|if-%s v%d,+0x%04x", (_opname), vsrc1, branchOffset);    \
            ILOGV("> branch taken");                                        \
            if (branchOffset < 0)                                           \
                PERIODIC_CHECKS(kInterpEntryInstr, branchOffset);           \
            FINISH(branchOffset);                                           \
        } else {                                                            \
            ILOGV("|if-%s v%d,-", (_opname), vsrc1);                        \
            FINISH(2);                                                      \
        }
HANDLE_OP_IF_XXZ(OP_IF_EQZ, "eqz", ==)
HANDLE_OP_IF_XXZ(OP_IF_NEZ, "nez", !=)
HANDLE_OP_IF_XXZ(OP_IF_LTZ, "ltz", <)
HANDLE_OP_IF_XXZ(OP_IF_GEZ, "gez", >=)
HANDLE_OP_IF_XXZ(OP_IF_GTZ, "gtz", >)
HANDLE_OP_IF_XXZ(OP_IF_LEZ, "lez", <=)


#define HANDLE_UNOP(_opcode, _opname, _pfx, _sfx, _type)                    \
    HANDLE_OPCODE(_opcode /*vA, vB*/)                                       \
        vdst = INST_A(inst);                                                \
        vsrc1 = INST_B(inst);                                               \
        ILOGV("|%s v%d,v%d", (_opname), vdst, vsrc1);                       \
        SET_REGISTER##_type(vdst, _pfx GET_REGISTER##_type(vsrc1) _sfx);    \
        FINISH(1);
HANDLE_UNOP(OP_NEG_INT, "neg-int", -, , )
HANDLE_UNOP(OP_NOT_INT, "not-int", , ^ 0xffffffff, )
HANDLE_UNOP(OP_NEG_LONG, "neg-long", -, , _WIDE)
HANDLE_UNOP(OP_NOT_LONG, "not-long", , ^ 0xffffffffffffffffULL, _WIDE)
HANDLE_UNOP(OP_NEG_FLOAT, "neg-float", -, , _FLOAT)
HANDLE_UNOP(OP_NEG_DOUBLE, "neg-double", -, , _DOUBLE)


#define HANDLE_OP_X_INT(_opcode, _opname, _op, _chkdiv)                     \
    HANDLE_OPCODE(_opcode /*vAA, vBB, vCC*/)                                \
    {                                                                       \
        u2 srcRegs;                                                         \
        vdst = INST_AA(inst);                                               \
        srcRegs = FETCH(1);                                                 \
        vsrc1 = srcRegs & 0xff;                                             \
        vsrc2 = srcRegs >> 8;                                               \
        ILOGV("|%s-int v%d,v%d", (_opname), vdst, vsrc1);                   \
        if (_chkdiv) {                                                      \
            if (GET_REGISTER(vsrc2) == 0) {                                 \
                EXPORT_PC();                                                \
                dvmThrowException("Ljava/lang/ArithmeticException;",        \
                    "divide by zero");                                      \
                goto exceptionThrown;                                       \
            }                                                               \
        }                                                                   \
        SET_REGISTER(vdst,                                                  \
            (s4) GET_REGISTER(vsrc1) _op (s4) GET_REGISTER(vsrc2));         \
    }                                                                       \
    FINISH(2);
HANDLE_OP_X_INT(OP_ADD_INT, "add", +, false)
HANDLE_OP_X_INT(OP_SUB_INT, "sub", -, false)
HANDLE_OP_X_INT(OP_MUL_INT, "mul", *, false)
HANDLE_OP_X_INT(OP_DIV_INT, "div", /, true)
HANDLE_OP_X_INT(OP_REM_INT, "rem", %, true)
HANDLE_OP_X_INT(OP_AND_INT, "and", &, false)
HANDLE_OP_X_INT(OP_OR_INT,  "or",  |, false)
HANDLE_OP_X_INT(OP_XOR_INT, "xor", ^, false)


#define HANDLE_OP_SHX_INT(_opcode, _opname, _cast, _op)                     \
    HANDLE_OPCODE(_opcode /*vAA, vBB, vCC*/)                                \
    {                                                                       \
        u2 srcRegs;                                                         \
        vdst = INST_AA(inst);                                               \
        srcRegs = FETCH(1);                                                 \
        vsrc1 = srcRegs & 0xff;                                             \
        vsrc2 = srcRegs >> 8;                                               \
        ILOGV("|%s-int v%d,v%d", (_opname), vdst, vsrc1);                   \
        SET_REGISTER(vdst,                                                  \
            _cast GET_REGISTER(vsrc1) _op (GET_REGISTER(vsrc2) & 0x1f));    \
    }                                                                       \
    FINISH(2);
HANDLE_OP_SHX_INT(OP_SHL_INT, "shl", (s4), <<)
HANDLE_OP_SHX_INT(OP_SHR_INT, "shr", (s4), >>)
HANDLE_OP_SHX_INT(OP_USHR_INT, "ushr", (u4), >>)


#define HANDLE_OP_X_INT_LIT16(_opcode, _opname, _cast, _op, _chkdiv)        \
    HANDLE_OPCODE(_opcode /*vA, vB, #+CCCC*/)                               \
        vdst = INST_A(inst);                                                \
        vsrc1 = INST_B(inst);                                               \
        vsrc2 = FETCH(1);                                                   \
        ILOGV("|%s-int/lit16 v%d,v%d,#+0x%04x",                             \
            (_opname), vdst, vsrc1, vsrc2);                                 \
        if (_chkdiv) {                                                      \
            if ((s2) vsrc2 == 0) {                                          \
                EXPORT_PC();                                                \
                dvmThrowException("Ljava/lang/ArithmeticException;",        \
                    "divide by zero");                                      \
                goto exceptionThrown;                                       \
            }                                                               \
        }                                                                   \
        SET_REGISTER(vdst,                                                  \
            _cast GET_REGISTER(vsrc1) _op (s2) vsrc2);                      \
        FINISH(2);
HANDLE_OP_X_INT_LIT16(OP_ADD_INT_LIT16, "add", (s4), +, false)
HANDLE_OP_X_INT_LIT16(OP_MUL_INT_LIT16, "mul", (s4), *, false)
HANDLE_OP_X_INT_LIT16(OP_DIV_INT_LIT16, "div", (s4), /, true)
HANDLE_OP_X_INT_LIT16(OP_REM_INT_LIT16, "rem", (s4), %, true)
HANDLE_OP_X_INT_LIT16(OP_AND_INT_LIT16, "and", (s4), &, false)
HANDLE_OP_X_INT_LIT16(OP_OR_INT_LIT16,  "or",  (s4), |, false)
HANDLE_OP_X_INT_LIT16(OP_XOR_INT_LIT16, "xor", (s4), ^, false)


HANDLE_OPCODE(OP_RSUB_INT /*vA, vB, #+CCCC*/)
    {
        vdst = INST_A(inst);
        vsrc1 = INST_B(inst);
        vsrc2 = FETCH(1);
        ILOGV("|rsub-int v%d,v%d,#+0x%04x", vdst, vsrc1, vsrc2);
        SET_REGISTER(vdst, (s2) vsrc2 - (s4) GET_REGISTER(vsrc1));
    }
    FINISH(2);


#define HANDLE_OP_X_INT_LIT8(_opcode, _opname, _op, _chkdiv)                \
    HANDLE_OPCODE(_opcode /*vAA, vBB, #+CC*/)                               \
    {                                                                       \
        u2 litInfo;                                                         \
        vdst = INST_AA(inst);                                               \
        litInfo = FETCH(1);                                                 \
        vsrc1 = litInfo & 0xff;                                             \
        vsrc2 = litInfo >> 8;       /* constant */                          \
        ILOGV("|%s-int/lit8 v%d,v%d,#+0x%02x",                              \
            (_opname), vdst, vsrc1, vsrc2);                                 \
        if (_chkdiv) {                                                      \
            if ((s1) vsrc2 == 0) {                                          \
                EXPORT_PC();                                                \
                dvmThrowException("Ljava/lang/ArithmeticException;",        \
                    "divide by zero");                                      \
                goto exceptionThrown;                                       \
            }                                                               \
        }                                                                   \
        SET_REGISTER(vdst,                                                  \
            (s4) GET_REGISTER(vsrc1) _op (s1) vsrc2);                       \
    }                                                                       \
    FINISH(2);
HANDLE_OP_X_INT_LIT8(OP_ADD_INT_LIT8,   "add", +, false)
HANDLE_OP_X_INT_LIT8(OP_MUL_INT_LIT8,   "mul", *, false)
HANDLE_OP_X_INT_LIT8(OP_DIV_INT_LIT8,   "div", /, true)
HANDLE_OP_X_INT_LIT8(OP_REM_INT_LIT8,   "rem", %, true)
HANDLE_OP_X_INT_LIT8(OP_AND_INT_LIT8,   "and", &, false)
HANDLE_OP_X_INT_LIT8(OP_OR_INT_LIT8,    "or",  |, false)
HANDLE_OP_X_INT_LIT8(OP_XOR_INT_LIT8,   "xor", ^, false)

#define HANDLE_OP_SHX_INT_LIT8(_opcode, _opname, _cast, _op)                \
    HANDLE_OPCODE(_opcode /*vAA, vBB, #+CC*/)                               \
    {                                                                       \
        u2 litInfo;                                                         \
        vdst = INST_AA(inst);                                               \
        litInfo = FETCH(1);                                                 \
        vsrc1 = litInfo & 0xff;                                             \
        vsrc2 = litInfo >> 8;       /* constant */                          \
        ILOGV("|%s-int/lit8 v%d,v%d,#+0x%02x",                              \
            (_opname), vdst, vsrc1, vsrc2);                                 \
        SET_REGISTER(vdst,                                                  \
            _cast GET_REGISTER(vsrc1) _op (vsrc2 & 0x1f));                  \
    }                                                                       \
    FINISH(2);

HANDLE_OP_SHX_INT_LIT8(OP_SHL_INT_LIT8,   "shl", (s4), <<)
HANDLE_OP_SHX_INT_LIT8(OP_SHR_INT_LIT8,   "shr", (s4), >>)
HANDLE_OP_SHX_INT_LIT8(OP_USHR_INT_LIT8,  "ushr", (u4), >>)

HANDLE_OPCODE(OP_RSUB_INT_LIT8 /*vAA, vBB, #+CC*/)
    {
        u2 litInfo;
        vdst = INST_AA(inst);
        litInfo = FETCH(1);
        vsrc1 = litInfo & 0xff;
        vsrc2 = litInfo >> 8;
        ILOGV("|%s-int/lit8 v%d,v%d,#+0x%02x", "rsub", vdst, vsrc1, vsrc2);
        SET_REGISTER(vdst, (s1) vsrc2 - (s4) GET_REGISTER(vsrc1));
    }
    FINISH(2);

#define HANDLE_OP_X_INT_2ADDR(_opcode, _opname, _op, _chkdiv)               \
    HANDLE_OPCODE(_opcode /*vA, vB*/)                                       \
        vdst = INST_A(inst);                                                \
        vsrc1 = INST_B(inst);                                               \
        ILOGV("|%s-int-2addr v%d,v%d", (_opname), vdst, vsrc1);             \
        if (_chkdiv) {                                                      \
            if (GET_REGISTER(vsrc1) == 0) {                                 \
                EXPORT_PC();                                                \
                dvmThrowException("Ljava/lang/ArithmeticException;",        \
                    "divide by zero");                                      \
                goto exceptionThrown;                                       \
            }                                                               \
        }                                                                   \
        SET_REGISTER(vdst,                                                  \
            (s4) GET_REGISTER(vdst) _op (s4) GET_REGISTER(vsrc1));          \
        FINISH(1);
HANDLE_OP_X_INT_2ADDR(OP_ADD_INT_2ADDR, "add", +, false)
HANDLE_OP_X_INT_2ADDR(OP_SUB_INT_2ADDR, "sub", -, false)
HANDLE_OP_X_INT_2ADDR(OP_MUL_INT_2ADDR, "mul", *, false)
HANDLE_OP_X_INT_2ADDR(OP_DIV_INT_2ADDR, "div", /, true)
HANDLE_OP_X_INT_2ADDR(OP_REM_INT_2ADDR, "rem", %, true)
HANDLE_OP_X_INT_2ADDR(OP_AND_INT_2ADDR, "and", &, false)
HANDLE_OP_X_INT_2ADDR(OP_OR_INT_2ADDR,  "or", |, false)
HANDLE_OP_X_INT_2ADDR(OP_XOR_INT_2ADDR, "xor", ^, false)


#define HANDLE_OP_SHX_INT_2ADDR(_opcode, _opname, _cast, _op)               \
    HANDLE_OPCODE(_opcode /*vA, vB*/)                                       \
        vdst = INST_A(inst);                                                \
        vsrc1 = INST_B(inst);                                               \
        ILOGV("|%s-int-2addr v%d,v%d", (_opname), vdst, vsrc1);             \
        SET_REGISTER(vdst,                                                  \
            _cast GET_REGISTER(vdst) _op (GET_REGISTER(vsrc1) & 0x1f));     \
        FINISH(1);

HANDLE_OP_SHX_INT_2ADDR(OP_SHL_INT_2ADDR, "shl", (s4), <<)
HANDLE_OP_SHX_INT_2ADDR(OP_SHR_INT_2ADDR, "shr", (s4), >>)
HANDLE_OP_SHX_INT_2ADDR(OP_USHR_INT_2ADDR, "ushr", (u4), >>)


#define HANDLE_OP_X_LONG(_opcode, _opname, _op, _chkdiv)                    \
    HANDLE_OPCODE(_opcode /*vAA, vBB, vCC*/)                                \
    {                                                                       \
        u2 srcRegs;                                                         \
        vdst = INST_AA(inst);                                               \
        srcRegs = FETCH(1);                                                 \
        vsrc1 = srcRegs & 0xff;                                             \
        vsrc2 = srcRegs >> 8;                                               \
        ILOGV("|%s-long v%d,v%d,v%d", (_opname), vdst, vsrc1, vsrc2);       \
        if (_chkdiv) {                                                      \
            if (GET_REGISTER_WIDE(vsrc2) == 0) {                            \
                EXPORT_PC();                                                \
                dvmThrowException("Ljava/lang/ArithmeticException;",        \
                    "divide by zero");                                      \
                goto exceptionThrown;                                       \
            }                                                               \
        }                                                                   \
        SET_REGISTER_WIDE(vdst,                                             \
            (s8) GET_REGISTER_WIDE(vsrc1) _op (s8) GET_REGISTER_WIDE(vsrc2)); \
    }                                                                       \
    FINISH(2);
HANDLE_OP_X_LONG(OP_ADD_LONG, "add", +, false)
HANDLE_OP_X_LONG(OP_SUB_LONG, "sub", -, false)
HANDLE_OP_X_LONG(OP_MUL_LONG, "mul", *, false)
HANDLE_OP_X_LONG(OP_DIV_LONG, "div", /, true)
HANDLE_OP_X_LONG(OP_REM_LONG, "rem", %, true)
HANDLE_OP_X_LONG(OP_AND_LONG, "and", &, false)
HANDLE_OP_X_LONG(OP_OR_LONG,  "or", |, false)
HANDLE_OP_X_LONG(OP_XOR_LONG, "xor", ^, false)

#define HANDLE_OP_SHX_LONG(_opcode, _opname, _cast, _op)                    \
    HANDLE_OPCODE(_opcode /*vAA, vBB, vCC*/)                                \
    {                                                                       \
        u2 srcRegs;                                                         \
        vdst = INST_AA(inst);                                               \
        srcRegs = FETCH(1);                                                 \
        vsrc1 = srcRegs & 0xff;                                             \
        vsrc2 = srcRegs >> 8;                                               \
        ILOGV("|%s-long v%d,v%d,v%d", (_opname), vdst, vsrc1, vsrc2);       \
        SET_REGISTER_WIDE(vdst,                                             \
            _cast GET_REGISTER_WIDE(vsrc1) _op (GET_REGISTER(vsrc2) & 0x3f)); \
    }                                                                       \
    FINISH(2);
HANDLE_OP_SHX_LONG(OP_SHL_LONG, "shl", (s8), <<)
HANDLE_OP_SHX_LONG(OP_SHR_LONG, "shr", (s8), >>)
HANDLE_OP_SHX_LONG(OP_USHR_LONG, "ushr", (u8), >>)


#define HANDLE_OP_X_LONG_2ADDR(_opcode, _opname, _op, _chkdiv)              \
    HANDLE_OPCODE(_opcode /*vA, vB*/)                                       \
        vdst = INST_A(inst);                                                \
        vsrc1 = INST_B(inst);                                               \
        ILOGV("|%s-long-2addr v%d,v%d", (_opname), vdst, vsrc1);            \
        if (_chkdiv) {                                                      \
            if (GET_REGISTER_WIDE(vsrc1) == 0) {                            \
                EXPORT_PC();                                                \
                dvmThrowException("Ljava/lang/ArithmeticException;",        \
                    "divide by zero");                                      \
                goto exceptionThrown;                                       \
            }                                                               \
        }                                                                   \
        SET_REGISTER_WIDE(vdst,                                             \
            (s8) GET_REGISTER_WIDE(vdst) _op (s8)GET_REGISTER_WIDE(vsrc1)); \
        FINISH(1);
HANDLE_OP_X_LONG_2ADDR(OP_ADD_LONG_2ADDR, "add", +, false)
HANDLE_OP_X_LONG_2ADDR(OP_SUB_LONG_2ADDR, "sub", -, false)
HANDLE_OP_X_LONG_2ADDR(OP_MUL_LONG_2ADDR, "mul", *, false)
HANDLE_OP_X_LONG_2ADDR(OP_DIV_LONG_2ADDR, "div", /, true)
HANDLE_OP_X_LONG_2ADDR(OP_REM_LONG_2ADDR, "rem", %, true)
HANDLE_OP_X_LONG_2ADDR(OP_AND_LONG_2ADDR, "and", &, false)
HANDLE_OP_X_LONG_2ADDR(OP_OR_LONG_2ADDR,  "or", |, false)
HANDLE_OP_X_LONG_2ADDR(OP_XOR_LONG_2ADDR, "xor", ^, false)


#define HANDLE_OP_SHX_LONG_2ADDR(_opcode, _opname, _cast, _op)              \
    HANDLE_OPCODE(_opcode /*vA, vB*/)                                       \
        vdst = INST_A(inst);                                                \
        vsrc1 = INST_B(inst);                                               \
        ILOGV("|%s-long-2addr v%d,v%d", (_opname), vdst, vsrc1);            \
        SET_REGISTER_WIDE(vdst,                                             \
            _cast GET_REGISTER_WIDE(vdst) _op (GET_REGISTER(vsrc1) & 0x3f)); \
        FINISH(1);

HANDLE_OP_SHX_LONG_2ADDR(OP_SHL_LONG_2ADDR, "shl", (s8), <<)
HANDLE_OP_SHX_LONG_2ADDR(OP_SHR_LONG_2ADDR, "shr", (s8), >>)
HANDLE_OP_SHX_LONG_2ADDR(OP_USHR_LONG_2ADDR, "ushr", (u8), >>)


#define HANDLE_OP_X_FLOAT(_opcode, _opname, _op)                            \
    HANDLE_OPCODE(_opcode /*vAA, vBB, vCC*/)                                \
    {                                                                       \
        u2 srcRegs;                                                         \
        vdst = INST_AA(inst);                                               \
        srcRegs = FETCH(1);                                                 \
        vsrc1 = srcRegs & 0xff;                                             \
        vsrc2 = srcRegs >> 8;                                               \
        ILOGV("|%s-float v%d,v%d,v%d", (_opname), vdst, vsrc1, vsrc2);      \
        SET_REGISTER_FLOAT(vdst,                                            \
            GET_REGISTER_FLOAT(vsrc1) _op GET_REGISTER_FLOAT(vsrc2));       \
    }                                                                       \
    FINISH(2);
HANDLE_OP_X_FLOAT(OP_ADD_FLOAT, "add", +)
HANDLE_OP_X_FLOAT(OP_SUB_FLOAT, "sub", -)
HANDLE_OP_X_FLOAT(OP_MUL_FLOAT, "mul", *)
HANDLE_OP_X_FLOAT(OP_DIV_FLOAT, "div", /)
HANDLE_OPCODE(OP_REM_FLOAT /*vAA, vBB, vCC*/)
    {
        u2 srcRegs;
        vdst = INST_AA(inst);
        srcRegs = FETCH(1);
        vsrc1 = srcRegs & 0xff;
        vsrc2 = srcRegs >> 8;
        ILOGV("|%s-float v%d,v%d,v%d", "rem", vdst, vsrc1, vsrc2);
        SET_REGISTER_FLOAT(vdst,
            fmodf(GET_REGISTER_FLOAT(vsrc1), GET_REGISTER_FLOAT(vsrc2)));
    }
    FINISH(2);


#define HANDLE_OP_X_DOUBLE(_opcode, _opname, _op)                           \
    HANDLE_OPCODE(_opcode /*vAA, vBB, vCC*/)                                \
    {                                                                       \
        u2 srcRegs;                                                         \
        vdst = INST_AA(inst);                                               \
        srcRegs = FETCH(1);                                                 \
        vsrc1 = srcRegs & 0xff;                                             \
        vsrc2 = srcRegs >> 8;                                               \
        ILOGV("|%s-double v%d,v%d,v%d", (_opname), vdst, vsrc1, vsrc2);     \
        SET_REGISTER_DOUBLE(vdst,                                           \
            GET_REGISTER_DOUBLE(vsrc1) _op GET_REGISTER_DOUBLE(vsrc2));     \
    }                                                                       \
    FINISH(2);
HANDLE_OP_X_DOUBLE(OP_ADD_DOUBLE, "add", +)
HANDLE_OP_X_DOUBLE(OP_SUB_DOUBLE, "sub", -)
HANDLE_OP_X_DOUBLE(OP_MUL_DOUBLE, "mul", *)
HANDLE_OP_X_DOUBLE(OP_DIV_DOUBLE, "div", /)
HANDLE_OPCODE(OP_REM_DOUBLE /*vAA, vBB, vCC*/)
    {
        u2 srcRegs;
        vdst = INST_AA(inst);
        srcRegs = FETCH(1);
        vsrc1 = srcRegs & 0xff;
        vsrc2 = srcRegs >> 8;
        ILOGV("|%s-double v%d,v%d,v%d", "rem", vdst, vsrc1, vsrc2);
        SET_REGISTER_DOUBLE(vdst,
            fmod(GET_REGISTER_DOUBLE(vsrc1), GET_REGISTER_DOUBLE(vsrc2)));
    }
    FINISH(2);

#define HANDLE_OP_X_FLOAT_2ADDR(_opcode, _opname, _op)                      \
    HANDLE_OPCODE(_opcode /*vA, vB*/)                                       \
        vdst = INST_A(inst);                                                \
        vsrc1 = INST_B(inst);                                               \
        ILOGV("|%s-float-2addr v%d,v%d", (_opname), vdst, vsrc1);           \
        SET_REGISTER_FLOAT(vdst,                                            \
            GET_REGISTER_FLOAT(vdst) _op GET_REGISTER_FLOAT(vsrc1));        \
        FINISH(1);
HANDLE_OP_X_FLOAT_2ADDR(OP_ADD_FLOAT_2ADDR, "add", +)
HANDLE_OP_X_FLOAT_2ADDR(OP_SUB_FLOAT_2ADDR, "sub", -)
HANDLE_OP_X_FLOAT_2ADDR(OP_MUL_FLOAT_2ADDR, "mul", *)
HANDLE_OP_X_FLOAT_2ADDR(OP_DIV_FLOAT_2ADDR, "div", /)

HANDLE_OPCODE(OP_REM_FLOAT_2ADDR /*vA, vB*/)
    vdst = INST_A(inst);
    vsrc1 = INST_B(inst);
    ILOGV("|%s-float-2addr v%d,v%d", "rem", vdst, vsrc1);
    SET_REGISTER_FLOAT(vdst,
        fmodf(GET_REGISTER_FLOAT(vdst), GET_REGISTER_FLOAT(vsrc1)));
    FINISH(1);


#define HANDLE_OP_X_DOUBLE_2ADDR(_opcode, _opname, _op)                     \
    HANDLE_OPCODE(_opcode /*vA, vB*/)                                       \
        vdst = INST_A(inst);                                                \
        vsrc1 = INST_B(inst);                                               \
        ILOGV("|%s-double-2addr v%d,v%d", (_opname), vdst, vsrc1);          \
        SET_REGISTER_DOUBLE(vdst,                                           \
            GET_REGISTER_DOUBLE(vdst) _op GET_REGISTER_DOUBLE(vsrc1));      \
        FINISH(1);

HANDLE_OP_X_DOUBLE_2ADDR(OP_ADD_DOUBLE_2ADDR, "add", +)
HANDLE_OP_X_DOUBLE_2ADDR(OP_SUB_DOUBLE_2ADDR, "sub", -)
HANDLE_OP_X_DOUBLE_2ADDR(OP_MUL_DOUBLE_2ADDR, "mul", *)
HANDLE_OP_X_DOUBLE_2ADDR(OP_DIV_DOUBLE_2ADDR, "div", /)

HANDLE_OPCODE(OP_REM_DOUBLE_2ADDR /*vA, vB*/)
    vdst = INST_A(inst);
    vsrc1 = INST_B(inst);
    ILOGV("|%s-double-2addr v%d,v%d", "rem", vdst, vsrc1);
    SET_REGISTER_DOUBLE(vdst,
        fmod(GET_REGISTER_DOUBLE(vdst), GET_REGISTER_DOUBLE(vsrc1)));
    FINISH(1);



#define HANDLE_OP_AGET(_opcode, _opname, _type, _regsize)                   \
    HANDLE_OPCODE(_opcode /*vAA, vBB, vCC*/)                                \
    {                                                                       \
        ArrayObject* arrayObj;                                              \
        u2 arrayInfo;                                                       \
        EXPORT_PC();                                                        \
        vdst = INST_AA(inst);                                               \
        arrayInfo = FETCH(1);                                               \
        vsrc1 = arrayInfo & 0xff;    /* array ptr */                        \
        vsrc2 = arrayInfo >> 8;      /* index */                            \
        ILOGV("|aget%s v%d,v%d,v%d", (_opname), vdst, vsrc1, vsrc2);        \
        arrayObj = (ArrayObject*) GET_REGISTER(vsrc1);                      \
        if (!checkForNull((Object*) arrayObj))                              \
            goto exceptionThrown;                                           \
        if (GET_REGISTER(vsrc2) >= arrayObj->length) {                      \
            LOGV("Invalid array access: %p %d (len=%d)\n",                  \
                arrayObj, vsrc2, arrayObj->length);                         \
            dvmThrowException("Ljava/lang/ArrayIndexOutOfBoundsException;", \
                NULL);                                                      \
            goto exceptionThrown;                                           \
        }                                                                   \
        SET_REGISTER##_regsize(vdst,                                        \
            ((_type*) arrayObj->contents)[GET_REGISTER(vsrc2)]);            \
        ILOGV("+ AGET[%d]=0x%x", GET_REGISTER(vsrc2), GET_REGISTER(vdst));  \
    }                                                                       \
    FINISH(2);
HANDLE_OP_AGET(OP_AGET, "", u4, )
HANDLE_OP_AGET(OP_AGET_OBJECT, "-object", u4, )
HANDLE_OP_AGET(OP_AGET_WIDE, "-wide", s8, _WIDE)
HANDLE_OP_AGET(OP_AGET_BOOLEAN, "-boolean", u1, )
HANDLE_OP_AGET(OP_AGET_BYTE, "-byte", s1, )
HANDLE_OP_AGET(OP_AGET_CHAR, "-char", u2, )
HANDLE_OP_AGET(OP_AGET_SHORT, "-short", s2, )


#define HANDLE_OP_APUT(_opcode, _opname, _type, _regsize)                   \
    HANDLE_OPCODE(_opcode /*vAA, vBB, vCC*/)                                \
    {                                                                       \
        ArrayObject* arrayObj;                                              \
        u2 arrayInfo;                                                       \
        EXPORT_PC();                                                        \
        vdst = INST_AA(inst);       /* AA: source value */                  \
        arrayInfo = FETCH(1);                                               \
        vsrc1 = arrayInfo & 0xff;   /* BB: array ptr */                     \
        vsrc2 = arrayInfo >> 8;     /* CC: index */                         \
        ILOGV("|aput%s v%d,v%d,v%d", (_opname), vdst, vsrc1, vsrc2);        \
        arrayObj = (ArrayObject*) GET_REGISTER(vsrc1);                      \
        if (!checkForNull((Object*) arrayObj))                              \
            goto exceptionThrown;                                           \
        if (GET_REGISTER(vsrc2) >= arrayObj->length) {                      \
            dvmThrowException("Ljava/lang/ArrayIndexOutOfBoundsException;", \
                NULL);                                                      \
            goto exceptionThrown;                                           \
        }                                                                   \
        ILOGV("+ APUT[%d]=0x%08x", GET_REGISTER(vsrc2), GET_REGISTER(vdst));\
        ((_type*) arrayObj->contents)[GET_REGISTER(vsrc2)] =                \
            GET_REGISTER##_regsize(vdst);                                   \
    }                                                                       \
    FINISH(2);
HANDLE_OP_APUT(OP_APUT, "", u4, )
HANDLE_OP_APUT(OP_APUT_WIDE, "-wide", s8, _WIDE)
HANDLE_OP_APUT(OP_APUT_BOOLEAN, "-boolean", u1, )
HANDLE_OP_APUT(OP_APUT_BYTE, "-byte", s1, )
HANDLE_OP_APUT(OP_APUT_CHAR, "-char", u2, )
HANDLE_OP_APUT(OP_APUT_SHORT, "-short", s2, )

HANDLE_OPCODE(OP_APUT_OBJECT /*vAA, vBB, vCC*/)
    {
        ArrayObject* arrayObj;
        Object* obj;
        u2 arrayInfo;
        EXPORT_PC();
        vdst = INST_AA(inst);       /* AA: source value */
        arrayInfo = FETCH(1);
        vsrc1 = arrayInfo & 0xff;   /* BB: array ptr */
        vsrc2 = arrayInfo >> 8;     /* CC: index */
        ILOGV("|aput%s v%d,v%d,v%d", "-object", vdst, vsrc1, vsrc2);
        arrayObj = (ArrayObject*) GET_REGISTER(vsrc1);
        if (!checkForNull((Object*) arrayObj))
            goto exceptionThrown;
        if (GET_REGISTER(vsrc2) >= arrayObj->length) {
            dvmThrowException("Ljava/lang/ArrayIndexOutOfBoundsException;",
                NULL);
            goto exceptionThrown;
        }
        obj = (Object*) GET_REGISTER(vdst);
        if (obj != NULL) {
            if (!checkForNull(obj))
                goto exceptionThrown;
            if (!dvmCanPutArrayElement(obj->clazz, arrayObj->obj.clazz)) {
                LOGV("Can't put a '%s'(%p) into array type='%s'(%p)\n",
                    obj->clazz->descriptor, obj,
                    arrayObj->obj.clazz->descriptor, arrayObj);
                //dvmDumpClass(obj->clazz);
                //dvmDumpClass(arrayObj->obj.clazz);
                dvmThrowException("Ljava/lang/ArrayStoreException;", NULL);
                goto exceptionThrown;
            }
        }
        ILOGV("+ APUT[%d]=0x%08x", GET_REGISTER(vsrc2), GET_REGISTER(vdst));
        ((u4*) arrayObj->contents)[GET_REGISTER(vsrc2)] =
            GET_REGISTER(vdst);
    }
    FINISH(2);

/*
 * It's possible to get a bad value out of a field with sub-32-bit stores
 * because the -quick versions always operate on 32 bits.  Consider:
 *   short foo = -1  (sets a 32-bit register to 0xffffffff)
 *   iput-quick foo  (writes all 32 bits to the field)
 *   short bar = 1   (sets a 32-bit register to 0x00000001)
 *   iput-short      (writes the low 16 bits to the field)
 *   iget-quick foo  (reads all 32 bits from the field, yielding 0xffff0001)
 * This can only happen when optimized and non-optimized code has interleaved
 * access to the same field.  This is unlikely but possible.
 *
 * The easiest way to fix this is to always read/write 32 bits at a time.  On
 * a device with a 16-bit data bus this is sub-optimal.  (The alternative
 * approach is to have sub-int versions of iget-quick, but now we're wasting
 * Dalvik instruction space and making it less likely that handler code will
 * already be in the CPU i-cache.)
 */
#define HANDLE_IGET_X(_opcode, _opname, _ftype, _regsize)                   \
    HANDLE_OPCODE(_opcode /*vA, vB, field@CCCC*/)                           \
    {                                                                       \
        InstField* ifield;                                                  \
        Object* obj;                                                        \
        EXPORT_PC();                                                        \
        vdst = INST_A(inst);                                                \
        vsrc1 = INST_B(inst);   /* object ptr */                            \
        ref = FETCH(1);         /* field ref */                             \
        ILOGV("|iget%s v%d,v%d,field@0x%04x", (_opname), vdst, vsrc1, ref); \
        obj = (Object*) GET_REGISTER(vsrc1);                                \
        if (!checkForNull(obj))                                             \
            goto exceptionThrown;                                           \
        ifield = (InstField*) dvmDexGetResolvedField(methodClassDex, ref);  \
        if (ifield == NULL) {                                               \
            ifield = dvmResolveInstField(method->clazz, ref);               \
            if (ifield == NULL)                                             \
                goto exceptionThrown;                                       \
        }                                                                   \
        SET_REGISTER##_regsize(vdst,                                        \
            dvmGetField##_ftype(obj, ifield->byteOffset));                  \
        ILOGV("+ IGET '%s'=0x%08llx", ifield->field.name,                   \
            (u8) GET_REGISTER##_regsize(vdst));                             \
        UPDATE_FIELD_GET(&ifield->field);                                   \
    }                                                                       \
    FINISH(2);
HANDLE_IGET_X(OP_IGET_WIDE,             "-wide", Long, _WIDE)
HANDLE_IGET_X(OP_IGET_OBJECT,           "-object", Object, _AS_OBJECT)
HANDLE_OPCODE(OP_IGET_BOOLEAN)
HANDLE_OPCODE(OP_IGET_BYTE)
HANDLE_OPCODE(OP_IGET_CHAR)
HANDLE_OPCODE(OP_IGET_SHORT)
HANDLE_IGET_X(OP_IGET,                  "", Int, )


#define HANDLE_IGET_X_QUICK(_opcode, _opname, _ftype, _regsize)             \
    HANDLE_OPCODE(_opcode /*vA, vB, field@CCCC*/)                           \
    {                                                                       \
        Object* obj;                                                        \
        vdst = INST_A(inst);                                                \
        vsrc1 = INST_B(inst);   /* object ptr */                            \
        ref = FETCH(1);         /* field offset */                          \
        ILOGV("|iget%s-quick v%d,v%d,field@+%u",                            \
            (_opname), vdst, vsrc1, ref);                                   \
        obj = (Object*) GET_REGISTER(vsrc1);                                \
        if (!checkForNullExportPC(obj, fp, pc))                             \
            goto exceptionThrown;                                           \
        SET_REGISTER##_regsize(vdst, dvmGetField##_ftype(obj, ref));        \
        ILOGV("+ IGETQ %d=0x%08llx", ref,                                   \
            (u8) GET_REGISTER##_regsize(vdst));                             \
    }                                                                       \
    FINISH(2);
HANDLE_IGET_X_QUICK(OP_IGET_QUICK,          "", Int, )
HANDLE_IGET_X_QUICK(OP_IGET_OBJECT_QUICK,   "-object", Object, _AS_OBJECT)
HANDLE_IGET_X_QUICK(OP_IGET_WIDE_QUICK,     "-wide", Long, _WIDE)


#define HANDLE_IPUT_X(_opcode, _opname, _ftype, _regsize)                   \
    HANDLE_OPCODE(_opcode /*vA, vB, field@CCCC*/)                           \
    {                                                                       \
        InstField* ifield;                                                  \
        Object* obj;                                                        \
        EXPORT_PC();                                                        \
        vdst = INST_A(inst);                                                \
        vsrc1 = INST_B(inst);   /* object ptr */                            \
        ref = FETCH(1);         /* field ref */                             \
        ILOGV("|iput%s v%d,v%d,field@0x%04x", (_opname), vdst, vsrc1, ref); \
        obj = (Object*) GET_REGISTER(vsrc1);                                \
        if (!checkForNull(obj))                                             \
            goto exceptionThrown;                                           \
        ifield = (InstField*) dvmDexGetResolvedField(methodClassDex, ref);  \
        if (ifield == NULL) {                                               \
            ifield = dvmResolveInstField(method->clazz, ref);               \
            if (ifield == NULL)                                             \
                goto exceptionThrown;                                       \
        }                                                                   \
        dvmSetField##_ftype(obj, ifield->byteOffset,                        \
            GET_REGISTER##_regsize(vdst));                                  \
        ILOGV("+ IPUT '%s'=0x%08llx", ifield->field.name,                   \
            (u8) GET_REGISTER##_regsize(vdst));                             \
        UPDATE_FIELD_PUT(&ifield->field);                                   \
    }                                                                       \
    FINISH(2);
HANDLE_IPUT_X(OP_IPUT_WIDE,             "-wide", Long, _WIDE)
HANDLE_IPUT_X(OP_IPUT_OBJECT,           "-object", Object, _AS_OBJECT)
HANDLE_OPCODE(OP_IPUT_BOOLEAN)
HANDLE_OPCODE(OP_IPUT_BYTE)
HANDLE_OPCODE(OP_IPUT_CHAR)
HANDLE_OPCODE(OP_IPUT_SHORT)
HANDLE_IPUT_X(OP_IPUT,                  "", Int, )


#define HANDLE_IPUT_X_QUICK(_opcode, _opname, _ftype, _regsize)             \
    HANDLE_OPCODE(_opcode /*vA, vB, field@CCCC*/)                           \
    {                                                                       \
        Object* obj;                                                        \
        vdst = INST_A(inst);                                                \
        vsrc1 = INST_B(inst);   /* object ptr */                            \
        ref = FETCH(1);         /* field offset */                          \
        ILOGV("|iput%s-quick v%d,v%d,field@0x%04x",                         \
            (_opname), vdst, vsrc1, ref);                                   \
        obj = (Object*) GET_REGISTER(vsrc1);                                \
        if (!checkForNullExportPC(obj, fp, pc))                             \
            goto exceptionThrown;                                           \
        dvmSetField##_ftype(obj, ref, GET_REGISTER##_regsize(vdst));        \
        ILOGV("+ IPUTQ %d=0x%08llx", ref,                                   \
            (u8) GET_REGISTER##_regsize(vdst));                             \
    }                                                                       \
    FINISH(2);
HANDLE_IPUT_X_QUICK(OP_IPUT_QUICK,          "", Int, )
HANDLE_IPUT_X_QUICK(OP_IPUT_OBJECT_QUICK,   "-object", Object, _AS_OBJECT)
HANDLE_IPUT_X_QUICK(OP_IPUT_WIDE_QUICK,     "-wide", Long, _WIDE)


#define HANDLE_SGET_X(_opcode, _opname, _ftype, _regsize)                   \
    HANDLE_OPCODE(_opcode /*vAA, field@BBBB*/)                              \
    {                                                                       \
        StaticField* sfield;                                                \
        vdst = INST_AA(inst);                                               \
        ref = FETCH(1);         /* field ref */                             \
        ILOGV("|sget%s v%d,sfield@0x%04x", (_opname), vdst, ref);           \
        sfield = (StaticField*)dvmDexGetResolvedField(methodClassDex, ref); \
        if (sfield == NULL) {                                               \
            EXPORT_PC();                                                    \
            sfield = dvmResolveStaticField(method->clazz, ref);             \
            if (sfield == NULL)                                             \
                goto exceptionThrown;                                       \
        }                                                                   \
        SET_REGISTER##_regsize(vdst, dvmGetStaticField##_ftype(sfield));    \
        ILOGV("+ SGET '%s'=0x%08llx",                                       \
            sfield->field.name, (u8)GET_REGISTER##_regsize(vdst));          \
        UPDATE_FIELD_GET(&sfield->field);                                   \
    }                                                                       \
    FINISH(2);
HANDLE_SGET_X(OP_SGET_WIDE,             "-wide", Long, _WIDE)
HANDLE_SGET_X(OP_SGET_OBJECT,           "-object", Object, _AS_OBJECT)
//HANDLE_SGET_X(OP_SGET_BOOLEAN,          "-boolean", Boolean, )
//HANDLE_SGET_X(OP_SGET_BYTE,             "-byte", Int, )
//HANDLE_SGET_X(OP_SGET_CHAR,             "-char", Int, )
//HANDLE_SGET_X(OP_SGET_SHORT,            "-short", Int, )
HANDLE_OPCODE(OP_SGET_BOOLEAN)
HANDLE_OPCODE(OP_SGET_BYTE)
HANDLE_OPCODE(OP_SGET_CHAR)
HANDLE_OPCODE(OP_SGET_SHORT)
HANDLE_SGET_X(OP_SGET,                  "", Int, )


#define HANDLE_SPUT_X(_opcode, _opname, _ftype, _regsize)                   \
    HANDLE_OPCODE(_opcode /*vAA, field@BBBB*/)                              \
    {                                                                       \
        StaticField* sfield;                                                \
        vdst = INST_AA(inst);                                               \
        ref = FETCH(1);         /* field ref */                             \
        ILOGV("|sput%s v%d,sfield@0x%04x", (_opname), vdst, ref);           \
        sfield = (StaticField*)dvmDexGetResolvedField(methodClassDex, ref); \
        if (sfield == NULL) {                                               \
            EXPORT_PC();                                                    \
            sfield = dvmResolveStaticField(method->clazz, ref);             \
            if (sfield == NULL)                                             \
                goto exceptionThrown;                                       \
        }                                                                   \
        dvmSetStaticField##_ftype(sfield, GET_REGISTER##_regsize(vdst));    \
        ILOGV("+ SPUT '%s'=0x%08llx",                                       \
            sfield->field.name, (u8)GET_REGISTER##_regsize(vdst));          \
        UPDATE_FIELD_PUT(&sfield->field);                                   \
    }                                                                       \
    FINISH(2);
HANDLE_SPUT_X(OP_SPUT_WIDE,             "-wide", Long, _WIDE)
HANDLE_SPUT_X(OP_SPUT_OBJECT,           "-object", Object, _AS_OBJECT)
//HANDLE_SPUT_X(OP_SPUT_BOOLEAN,          "-boolean", Boolean, )
//HANDLE_SPUT_X(OP_SPUT_BYTE,             "-byte", Int, )
//HANDLE_SPUT_X(OP_SPUT_CHAR,             "-char", Int, )
//HANDLE_SPUT_X(OP_SPUT_SHORT,            "-short", Int, )
HANDLE_OPCODE(OP_SPUT_BOOLEAN)
HANDLE_OPCODE(OP_SPUT_BYTE)
HANDLE_OPCODE(OP_SPUT_CHAR)
HANDLE_OPCODE(OP_SPUT_SHORT)
HANDLE_SPUT_X(OP_SPUT,                  "", Int, )


HANDLE_OPCODE(OP_CONST_4 /*vA, #+B*/)
    {
        s4 tmp;

        vdst = INST_A(inst);
        tmp = (s4) (INST_B(inst) << 28) >> 28;  // sign extend 4-bit value
        ILOGV("|const/4 v%d,#0x%02x", vdst, (s4)tmp);
        SET_REGISTER(vdst, tmp);
    }
    FINISH(1);

HANDLE_OPCODE(OP_CONST_16 /*vAA, #+BBBB*/)
    vdst = INST_AA(inst);
    vsrc1 = FETCH(1);
    ILOGV("|const/16 v%d,#0x%04x", vdst, (s2)vsrc1);
    SET_REGISTER(vdst, (s2) vsrc1);
    FINISH(2);

HANDLE_OPCODE(OP_CONST /*vAA, #+BBBBBBBB*/)
    {
        u4 tmp;

        vdst = INST_AA(inst);
        tmp = FETCH(1);
        tmp |= (u4)FETCH(2) << 16;
        ILOGV("|const v%d,#0x%08x", vdst, tmp);
        SET_REGISTER(vdst, tmp);
    }
    FINISH(3);

HANDLE_OPCODE(OP_CONST_HIGH16 /*vAA, #+BBBB0000*/)
    vdst = INST_AA(inst);
    vsrc1 = FETCH(1);
    ILOGV("|const/high16 v%d,#0x%04x0000", vdst, vsrc1);
    SET_REGISTER(vdst, vsrc1 << 16);
    FINISH(2);

HANDLE_OPCODE(OP_CONST_WIDE_16 /*vAA, #+BBBB*/)
    vdst = INST_AA(inst);
    vsrc1 = FETCH(1);
    ILOGV("|const-wide/16 v%d,#0x%04x", vdst, (s2)vsrc1);
    SET_REGISTER_WIDE(vdst, (s2)vsrc1);
    FINISH(2);

HANDLE_OPCODE(OP_CONST_WIDE_32 /*vAA, #+BBBBBBBB*/)
    {
        u4 tmp;

        vdst = INST_AA(inst);
        tmp = FETCH(1);
        tmp |= (u4)FETCH(2) << 16;
        ILOGV("|const-wide/32 v%d,#0x%08x", vdst, tmp);
        SET_REGISTER_WIDE(vdst, (s4) tmp);
    }
    FINISH(3);

HANDLE_OPCODE(OP_CONST_WIDE /*vAA, #+BBBBBBBBBBBBBBBB*/)
    {
        u8 tmp;

        vdst = INST_AA(inst);
        tmp = FETCH(1);
        tmp |= (u8)FETCH(2) << 16;
        tmp |= (u8)FETCH(3) << 32;
        tmp |= (u8)FETCH(4) << 48;
        ILOGV("|const-wide v%d,#0x%08llx", vdst, tmp);
        SET_REGISTER_WIDE(vdst, tmp);
    }
    FINISH(5);

HANDLE_OPCODE(OP_CONST_WIDE_HIGH16 /*vAA, #+BBBB000000000000*/)
    vdst = INST_AA(inst);
    vsrc1 = FETCH(1);
    ILOGV("|const-wide/high16 v%d,#0x%04x000000000000", vdst, vsrc1);
    SET_REGISTER_WIDE(vdst, ((u8) vsrc1) << 48);
    FINISH(2);

HANDLE_OPCODE(OP_CONST_STRING /*vAA, string@BBBB*/)
    {
        StringObject* strObj;

        vdst = INST_AA(inst);
        ref = FETCH(1);
        ILOGV("|const-string v%d string@0x%04x", vdst, ref);
        strObj = dvmDexGetResolvedString(methodClassDex, ref);
        if (strObj == NULL) {
            EXPORT_PC();
            strObj = dvmResolveString(method->clazz, ref);
            if (strObj == NULL)
                goto exceptionThrown;
        }
        SET_REGISTER(vdst, (u4) strObj);
    }
    FINISH(2);

HANDLE_OPCODE(OP_CONST_STRING_JUMBO /*vAA, string@BBBBBBBB*/)
    {
        StringObject* strObj;
        u4 tmp;

        vdst = INST_AA(inst);
        tmp = FETCH(1);
        tmp |= (u4)FETCH(2) << 16;
        ILOGV("|const-string/jumbo v%d string@0x%08x", vdst, tmp);
        strObj = dvmDexGetResolvedString(methodClassDex, tmp);
        if (strObj == NULL) {
            EXPORT_PC();
            strObj = dvmResolveString(method->clazz, tmp);
            if (strObj == NULL)
                goto exceptionThrown;
        }
        SET_REGISTER(vdst, (u4) strObj);
    }
    FINISH(3);

HANDLE_OPCODE(OP_CONST_CLASS /*vAA, class@BBBB*/)
    {
        ClassObject* clazz;

        vdst = INST_AA(inst);
        ref = FETCH(1);
        ILOGV("|const-class v%d class@0x%04x", vdst, ref);
        clazz = dvmDexGetResolvedClass(methodClassDex, ref);
        if (clazz == NULL) {
            EXPORT_PC();
            clazz = dvmResolveClass(method->clazz, ref, true);
            if (clazz == NULL)
                goto exceptionThrown;
        }
        SET_REGISTER(vdst, (u4) clazz);
    }
    FINISH(2);

HANDLE_OPCODE(OP_MONITOR_ENTER /*vAA*/)
    {
        Object* obj;

        vsrc1 = INST_AA(inst);
        ILOGV("|monitor-enter v%d %s(0x%08x)",
            vsrc1, kSpacing+6, GET_REGISTER(vsrc1));
        obj = (Object*)GET_REGISTER(vsrc1);
        if (!checkForNullExportPC(obj, fp, pc))
            goto exceptionThrown;
        ILOGV("+ locking %p %s\n", obj, obj->clazz->descriptor);
#ifdef WITH_MONITOR_TRACKING
        EXPORT_PC();        /* need for stack trace */
#endif
        dvmLockObject(self, obj);
#ifdef WITH_DEADLOCK_PREDICTION
        if (dvmCheckException(self))
            goto exceptionThrown;
#endif
    }
    FINISH(1);

HANDLE_OPCODE(OP_MONITOR_EXIT /*vAA*/)
    {
        Object* obj;

        EXPORT_PC();

        vsrc1 = INST_AA(inst);
        ILOGV("|monitor-exit v%d %s(0x%08x)",
            vsrc1, kSpacing+5, GET_REGISTER(vsrc1));
        obj = (Object*)GET_REGISTER(vsrc1);
        if (!checkForNull(obj)) {
            /*
             * The exception needs to be processed at the *following*
             * instruction, not the current instruction (see the Dalvik
             * spec).  Because we're jumping to an exception handler,
             * we're not actually at risk of skipping an instruction
             * by doing so.
             */
            ADJUST_PC(1);           /* monitor-exit width is 1 */
            goto exceptionThrown;
        }
        ILOGV("+ unlocking %p %s\n", obj, obj->clazz->descriptor);
        if (!dvmUnlockObject(self, obj)) {
            assert(dvmCheckException(self));
            ADJUST_PC(1);
            goto exceptionThrown;
        }
    }
    FINISH(1);

HANDLE_OPCODE(OP_CHECK_CAST /*vAA, class@BBBB*/)
    {
        ClassObject* clazz;
        Object* obj;

        EXPORT_PC();

        vsrc1 = INST_AA(inst);
        ref = FETCH(1);         /* class to check against */
        ILOGV("|check-cast v%d,class@0x%04x", vsrc1, ref);

        obj = (Object*)GET_REGISTER(vsrc1);
        if (obj != NULL) {
#if defined(WITH_EXTRA_OBJECT_VALIDATION)
            if (!checkForNull(obj))     /* do additional checks */
                goto exceptionThrown;
#endif
            clazz = dvmDexGetResolvedClass(methodClassDex, ref);
            if (clazz == NULL) {
                clazz = dvmResolveClass(method->clazz, ref, false);
                if (clazz == NULL)
                    goto exceptionThrown;
            }
            if (!dvmInstanceof(obj->clazz, clazz)) {
                dvmThrowExceptionWithClassMessage(
                    "Ljava/lang/ClassCastException;", obj->clazz->descriptor);
                goto exceptionThrown;
            }
        }
    }
    FINISH(2);

HANDLE_OPCODE(OP_INSTANCE_OF /*vA, vB, class@CCCC*/)
    {
        ClassObject* clazz;
        Object* obj;

        vdst = INST_A(inst);
        vsrc1 = INST_B(inst);   /* object to check */
        ref = FETCH(1);         /* class to check against */
        ILOGV("|instance-of v%d,v%d,class@0x%04x", vdst, vsrc1, ref);

        obj = (Object*)GET_REGISTER(vsrc1);
        if (obj == NULL) {
            SET_REGISTER(vdst, 0);
        } else {
#if defined(WITH_EXTRA_OBJECT_VALIDATION)
            if (!checkForNullExportPC(obj, fp, pc)) /* do additional checks */
                goto exceptionThrown;
#endif
            clazz = dvmDexGetResolvedClass(methodClassDex, ref);
            if (clazz == NULL) {
                EXPORT_PC();
                clazz = dvmResolveClass(method->clazz, ref, true);
                if (clazz == NULL)
                    goto exceptionThrown;
            }
            SET_REGISTER(vdst, dvmInstanceof(obj->clazz, clazz));
        }
    }
    FINISH(2);

HANDLE_OPCODE(OP_ARRAY_LENGTH /*vA, vB*/)
    {
        ArrayObject* arrayObj;

        vdst = INST_A(inst);
        vsrc1 = INST_B(inst);
        arrayObj = (ArrayObject*) GET_REGISTER(vsrc1);
        ILOGV("|array-length v%d,v%d  (%p)", vdst, vsrc1, arrayObj);
        if (!checkForNullExportPC((Object*) arrayObj, fp, pc))
            goto exceptionThrown;
        /* verifier guarantees this is an array reference */
        SET_REGISTER(vdst, arrayObj->length);
    }
    FINISH(1);

HANDLE_OPCODE(OP_NEW_INSTANCE /*vAA, class@BBBB*/)
    {
        ClassObject* clazz;
        Object* newObj;

        EXPORT_PC();

        vdst = INST_AA(inst);
        ref = FETCH(1);
        ILOGV("|new-instance v%d,class@0x%04x", vdst, ref);
        clazz = dvmDexGetResolvedClass(methodClassDex, ref);
        if (clazz == NULL) {
            clazz = dvmResolveClass(method->clazz, ref, false);
            if (clazz == NULL)
                goto exceptionThrown;
        }

        if (!dvmIsClassInitialized(clazz) && !dvmInitClass(clazz))
            goto exceptionThrown;

        /*
         * Note: the verifier can ensure that this never happens, allowing us
         * to remove the check.  However, the spec requires we throw the
         * exception at runtime, not verify time, so the verifier would
         * need to replace the new-instance call with a magic "throw
         * InstantiationError" instruction.
         *
         * Since this relies on the verifier, which is optional, we would
         * also need a "new-instance-quick" instruction to identify instances
         * that don't require the check.
         */
        if (dvmIsInterfaceClass(clazz) || dvmIsAbstractClass(clazz)) {
            dvmThrowExceptionWithClassMessage("Ljava/lang/InstantiationError;",
                clazz->descriptor);
            goto exceptionThrown;
        }
        newObj = dvmAllocObject(clazz, ALLOC_DONT_TRACK);
        if (newObj == NULL)
            goto exceptionThrown;
        SET_REGISTER(vdst, (u4) newObj);
    }
    FINISH(2);

HANDLE_OPCODE(OP_NEW_ARRAY /*vA, vB, class@CCCC*/)
    {
        ClassObject* arrayClass;
        ArrayObject* newArray;
        s4 length;

        EXPORT_PC();

        vdst = INST_A(inst);
        vsrc1 = INST_B(inst);       /* length reg */
        ref = FETCH(1);
        ILOGV("|new-array v%d,v%d,class@0x%04x  (%d elements)",
            vdst, vsrc1, ref, (s4) GET_REGISTER(vsrc1));
        length = (s4) GET_REGISTER(vsrc1);
        if (length < 0) {
            dvmThrowException("Ljava/lang/NegativeArraySizeException;", NULL);
            goto exceptionThrown;
        }
        arrayClass = dvmDexGetResolvedClass(methodClassDex, ref);
        if (arrayClass == NULL) {
            arrayClass = dvmResolveClass(method->clazz, ref, false);
            if (arrayClass == NULL)
                goto exceptionThrown;
        }
        /* verifier guarantees this is an array class */
        assert(dvmIsArrayClass(arrayClass));
        assert(dvmIsClassInitialized(arrayClass));

        newArray = dvmAllocArrayByClass(arrayClass, length, ALLOC_DONT_TRACK);
        if (newArray == NULL)
            goto exceptionThrown;
        SET_REGISTER(vdst, (u4) newArray);
    }
    FINISH(2);

HANDLE_OPCODE(OP_FILLED_NEW_ARRAY_RANGE /*{vCCCC..v(CCCC+AA-1)}, class@BBBB*/)
    methodCallRange = true;     // work this like the method call variants
    goto filledNewArray;
HANDLE_OPCODE(OP_FILLED_NEW_ARRAY /*vB, {vD, vE, vF, vG, vA}, class@CCCC*/)
    methodCallRange = false;
filledNewArray:
    {
        ClassObject* arrayClass;
        ArrayObject* newArray;
        int* contents;
        char typeCh;
        int i;
        u4 arg5;

        EXPORT_PC();

        ref = FETCH(1);             /* class ref */
        vdst = FETCH(2);            /* first 4 regs -or- range base */

        if (methodCallRange) {
            vsrc1 = INST_AA(inst);  /* #of elements */
            arg5 = -1;              /* silence compiler warning */
            ILOGV("|filled-new-array-range args=%d @0x%04x {regs=v%d-v%d}",
                vsrc1, ref, vdst, vdst+vsrc1-1);
        } else {
            arg5 = INST_A(inst);
            vsrc1 = INST_B(inst);   /* #of elements */
            ILOGV("|filled-new-array args=%d @0x%04x {regs=0x%04x %x}",
                vsrc1, ref, vdst, arg5);
        }

        /*
         * Resolve the array class.
         */
        arrayClass = dvmDexGetResolvedClass(methodClassDex, ref);
        if (arrayClass == NULL) {
            arrayClass = dvmResolveClass(method->clazz, ref, false);
            if (arrayClass == NULL)
                goto exceptionThrown;
        }
        /*
        if (!dvmIsArrayClass(arrayClass)) {
            dvmThrowException("Ljava/lang/RuntimeError;",
                "filled-new-array needs array class");
            goto exceptionThrown;
        }
        */
        /* verifier guarantees this is an array class */
        assert(dvmIsArrayClass(arrayClass));
        assert(dvmIsClassInitialized(arrayClass));

        /*
         * Create an array of the specified type.
         */
        LOGVV("+++ filled-new-array type is '%s'\n", arrayClass->descriptor);
        typeCh = arrayClass->descriptor[1];
        if (typeCh == 'D' || typeCh == 'J') {
            /* category 2 primitives not allowed */
            dvmThrowException("Ljava/lang/RuntimeError;",
                "bad filled array req");
            goto exceptionThrown;
        } else if (typeCh == 'L' || typeCh == '[') {
            /* create array of objects or array of arrays */
            /* TODO: need some work in the verifier before we allow this */
            LOGE("fnao not implemented\n");
            dvmThrowException("Ljava/lang/InternalError;",
                "filled-new-array not implemented for reference types");
            goto exceptionThrown;
        } else if (typeCh != 'I') {
            /* TODO: requires multiple "fill in" loops with different widths */
            LOGE("non-int not implemented\n");
            dvmThrowException("Ljava/lang/InternalError;",
                "filled-new-array not implemented for anything but 'int'");
            goto exceptionThrown;
        }

        assert(strchr("BCIFZ", typeCh) != NULL);
        newArray = dvmAllocPrimitiveArray(arrayClass->descriptor[1], vsrc1,
                    ALLOC_DONT_TRACK);
        if (newArray == NULL)
            goto exceptionThrown;

        /*
         * Fill in the elements.  It's legal for vsrc1 to be zero.
         */
        contents = (int*) newArray->contents;
        if (methodCallRange) {
            for (i = 0; i < vsrc1; i++)
                contents[i] = GET_REGISTER(vdst+i);
        } else {
            assert(vsrc1 <= 5);
            if (vsrc1 == 5) {
                contents[4] = GET_REGISTER(arg5);
                vsrc1--;
            }
            for (i = 0; i < vsrc1; i++) {
                contents[i] = GET_REGISTER(vdst & 0x0f);
                vdst >>= 4;
            }
        }

        retval.l = newArray;
    }
    FINISH(3);

HANDLE_OPCODE(OP_FILL_ARRAY_DATA /*vAA, +BBBBBBBB*/)
    {
        const u2* arrayData;
        s4 offset;
        ArrayObject* arrayObj;

        EXPORT_PC();
        vsrc1 = INST_AA(inst);
        offset = FETCH(1) | (((s4) FETCH(2)) << 16);
        ILOGV("|fill-array-data v%d +0x%04x", vsrc1, offset);
        arrayData = pc + offset;       // offset in 16-bit units
#ifndef NDEBUG
        if (arrayData < method->insns ||
            arrayData >= method->insns + dvmGetMethodInsnsSize(method))
        {
            /* should have been caught in verifier */
            dvmThrowException("Ljava/lang/InternalError;", 
                              "bad fill array data");
            goto exceptionThrown;
        }
#endif
        arrayObj = (ArrayObject*) GET_REGISTER(vsrc1);
        if (!dvmInterpHandleFillArrayData(arrayObj, arrayData)) {
            goto exceptionThrown;
        }
        FINISH(3);
    }

HANDLE_OPCODE(OP_THROW /*vAA*/)
    {
        Object* obj;

        vsrc1 = INST_AA(inst);
        ILOGV("|throw v%d  (%p)", vsrc1, (void*)GET_REGISTER(vsrc1));
        obj = (Object*) GET_REGISTER(vsrc1);
        if (!checkForNullExportPC(obj, fp, pc)) {
            /* will throw a null pointer exception */
            LOGVV("Bad exception\n");
        } else {
            /* use the requested exception */
            dvmSetException(self, obj);
        }
        goto exceptionThrown;
    }

HANDLE_OPCODE(OP_PACKED_SWITCH /*vAA, +BBBBBBBB*/)
    {
        const u2* switchData;
        u4 testVal;
        s4 offset;

        vsrc1 = INST_AA(inst);
        offset = FETCH(1) | (((s4) FETCH(2)) << 16);
        ILOGV("|packed-switch v%d +0x%04x", vsrc1, offset);
        switchData = pc + offset;       // offset in 16-bit units
#ifndef NDEBUG
        if (switchData < method->insns ||
            switchData >= method->insns + dvmGetMethodInsnsSize(method))
        {
            /* should have been caught in verifier */
            EXPORT_PC();
            dvmThrowException("Ljava/lang/InternalError;", "bad packed switch");
            goto exceptionThrown;
        }
#endif
        testVal = GET_REGISTER(vsrc1);

        offset = dvmInterpHandlePackedSwitch(switchData, testVal);
        ILOGV("> branch taken (0x%04x)\n", offset);
        if (offset <= 0)    /* uncommon */
            PERIODIC_CHECKS(kInterpEntryInstr, offset);
        FINISH(offset);
    }

HANDLE_OPCODE(OP_SPARSE_SWITCH /*vAA, +BBBBBBBB*/)
    {
        const u2* switchData;
        u4 testVal;
        s4 offset;

        vsrc1 = INST_AA(inst);
        offset = FETCH(1) | (((s4) FETCH(2)) << 16);
        ILOGV("|sparse-switch v%d +0x%04x", vsrc1, offset);
        switchData = pc + offset;       // offset in 16-bit units
#ifndef NDEBUG
        if (switchData < method->insns ||
            switchData >= method->insns + dvmGetMethodInsnsSize(method))
        {
            /* should have been caught in verifier */
            EXPORT_PC();
            dvmThrowException("Ljava/lang/InternalError;", "bad sparse switch");
            goto exceptionThrown;
        }
#endif
        testVal = GET_REGISTER(vsrc1);

        offset = dvmInterpHandleSparseSwitch(switchData, testVal);
        ILOGV("> branch taken (0x%04x)\n", offset);
        if (offset <= 0)  /* uncommon */
            PERIODIC_CHECKS(kInterpEntryInstr, offset);
        FINISH(offset);
    }

HANDLE_OPCODE(OP_EXECUTE_INLINE /*vB, {vD, vE, vF, vG}, inline@CCCC*/)
    {
        /*
         * This has the same form as other method calls, but we ignore
         * the 5th argument (vA).  This is chiefly because the first four
         * arguments to a function on ARM are in registers.
         *
         * We only set the arguments that are actually used, leaving
         * the rest uninitialized.  We're assuming that, if the method
         * needs them, they'll be specified in the call.
         *
         * This annoys gcc when optimizations are enabled, causing a
         * "may be used uninitialized" warning.  We can quiet the warnings
         * for a slight penalty (5%: 373ns vs. 393ns on empty method).  Note
         * that valgrind is perfectly happy with this arrangement, because
         * the uninitialized values are never actually used.
         */
        u4 arg0, arg1, arg2, arg3;
        //arg0 = arg1 = arg2 = arg3 = 0;

        EXPORT_PC();

        vsrc1 = INST_B(inst);       /* #of args */
        ref = FETCH(1);             /* inline call "ref" */
        vdst = FETCH(2);            /* 0-4 register indices */
        ILOGV("|execute-inline args=%d @%d {regs=0x%04x}",
            vsrc1, ref, vdst);

        assert((vdst >> 16) == 0);  // 16-bit type -or- high 16 bits clear
        assert(vsrc1 <= 4);

        switch (vsrc1) {
        case 4:
            arg3 = GET_REGISTER(vdst >> 12);
            /* fall through */
        case 3:
            arg2 = GET_REGISTER((vdst & 0x0f00) >> 8);
            /* fall through */
        case 2:
            arg1 = GET_REGISTER((vdst & 0x00f0) >> 4);
            /* fall through */
        case 1:
            arg0 = GET_REGISTER(vdst & 0x0f);
            /* fall through */
        default:        // case 0
            ;
        }

#if INTERP_TYPE == INTERP_DBG
        if (!dvmPerformInlineOp4Dbg(arg0, arg1, arg2, arg3, &retval, ref))
            goto exceptionThrown;
#else
        if (!dvmPerformInlineOp4Std(arg0, arg1, arg2, arg3, &retval, ref))
            goto exceptionThrown;
#endif
    }
    FINISH(3);


HANDLE_OPCODE(OP_INVOKE_VIRTUAL_RANGE /*{vCCCC..v(CCCC+AA-1)}, meth@BBBB*/)
    methodCallRange = true;
    goto invokeVirtual;
HANDLE_OPCODE(OP_INVOKE_VIRTUAL /*vB, {vD, vE, vF, vG, vA}, meth@CCCC*/)
    methodCallRange = false;
invokeVirtual:
    {
        Method* baseMethod;
        Object* thisPtr;

        EXPORT_PC();

        vsrc1 = INST_AA(inst);      /* AA (count) or BA (count + arg 5) */
        ref = FETCH(1);             /* method ref */
        vdst = FETCH(2);            /* 4 regs -or- first reg */

        /*
         * The object against which we are executing a method is always
         * in the first argument.
         */
        assert(vsrc1 > 0);
        if (methodCallRange) {
            ILOGV("|invoke-virtual-range args=%d @0x%04x {regs=v%d-v%d}",
                vsrc1, ref, vdst, vdst+vsrc1-1);
            thisPtr = (Object*) GET_REGISTER(vdst);
        } else {
            ILOGV("|invoke-virtual args=%d @0x%04x {regs=0x%04x %x}",
                vsrc1 >> 4, ref, vdst, vsrc1 & 0x0f);
            thisPtr = (Object*) GET_REGISTER(vdst & 0x0f);
        }

        if (!checkForNull(thisPtr))
            goto exceptionThrown;

        /*
         * Resolve the method.  This is the correct method for the static
         * type of the object.  We also verify access permissions here.
         */
        baseMethod = dvmDexGetResolvedMethod(methodClassDex, ref);
        if (baseMethod == NULL) {
            baseMethod = dvmResolveMethod(method->clazz, ref, METHOD_VIRTUAL);
            if (baseMethod == NULL) {
                ILOGV("+ unknown method or access denied\n");
                goto exceptionThrown;
            }
        }

        /*
         * Combine the object we found with the vtable offset in the
         * method.
         */
        assert(baseMethod->methodIndex < thisPtr->clazz->vtableCount);
        methodToCall = thisPtr->clazz->vtable[baseMethod->methodIndex];

#if 0
        if (dvmIsAbstractMethod(methodToCall)) {
            /*
             * This can happen if you create two classes, Base and Sub, where
             * Sub is a sub-class of Base.  Declare a protected abstract
             * method foo() in Base, and invoke foo() from a method in Base.
             * Base is an "abstract base class" and is never instantiated
             * directly.  Now, Override foo() in Sub, and use Sub.  This
             * Works fine unless Sub stops providing an implementation of
             * the method.
             */
            dvmThrowException("Ljava/lang/AbstractMethodError;",
                "abstract method not implemented");
            goto exceptionThrown;
        }
#else
        assert(!dvmIsAbstractMethod(methodToCall) ||
            methodToCall->nativeFunc != NULL);
#endif

        LOGVV("+++ base=%s.%s virtual[%d]=%s.%s\n",
            baseMethod->clazz->descriptor, baseMethod->name,
            (u4) baseMethod->methodIndex,
            methodToCall->clazz->descriptor, methodToCall->name);
        assert(methodToCall != NULL);

#if 0
        if (vsrc1 != methodToCall->insSize) {
            LOGW("WRONG METHOD: base=%s.%s virtual[%d]=%s.%s\n",
                baseMethod->clazz->descriptor, baseMethod->name,
                (u4) baseMethod->methodIndex,
                methodToCall->clazz->descriptor, methodToCall->name);
            //dvmDumpClass(baseMethod->clazz);
            //dvmDumpClass(methodToCall->clazz);
            dvmDumpAllClasses(0);
        }
#endif

        goto invokeMethod;
    }

HANDLE_OPCODE(OP_INVOKE_SUPER_RANGE /*{vCCCC..v(CCCC+AA-1)}, meth@BBBB*/)
    methodCallRange = true;
    goto invokeSuper;
HANDLE_OPCODE(OP_INVOKE_SUPER /*vB, {vD, vE, vF, vG, vA}, meth@CCCC*/)
    methodCallRange = false;
invokeSuper:
    {
        Method* baseMethod;
        u2 thisReg;

        EXPORT_PC();

        vsrc1 = INST_AA(inst);      /* AA (count) or BA (count + arg 5) */
        ref = FETCH(1);             /* method ref */
        vdst = FETCH(2);            /* 4 regs -or- first reg */

        if (methodCallRange) {
            ILOGV("|invoke-super-range args=%d @0x%04x {regs=v%d-v%d}",
                vsrc1, ref, vdst, vdst+vsrc1-1);
            thisReg = vdst;
        } else {
            ILOGV("|invoke-super args=%d @0x%04x {regs=0x%04x %x}",
                vsrc1 >> 4, ref, vdst, vsrc1 & 0x0f);
            thisReg = vdst & 0x0f;
        }
        /* impossible in well-formed code, but we must check nevertheless */
        if (!checkForNull((Object*) GET_REGISTER(thisReg)))
            goto exceptionThrown;

        /*
         * Resolve the method.  This is the correct method for the static
         * type of the object.  We also verify access permissions here.
         * The first arg to dvmResolveMethod() is just the referring class
         * (used for class loaders and such), so we don't want to pass
         * the superclass into the resolution call.
         */
        baseMethod = dvmDexGetResolvedMethod(methodClassDex, ref);
        if (baseMethod == NULL) {
            baseMethod = dvmResolveMethod(method->clazz, ref, METHOD_VIRTUAL);
            if (baseMethod == NULL) {
                ILOGV("+ unknown method or access denied\n");
                goto exceptionThrown;
            }
        }

        /*
         * Combine the object we found with the vtable offset in the
         * method's class.
         *
         * We're using the current method's class' superclass, not the
         * superclass of "this".  This is because we might be executing
         * in a method inherited from a superclass, and we want to run
         * in that class' superclass.
         */
        if (baseMethod->methodIndex >= method->clazz->super->vtableCount) {
            /*
             * Method does not exist in the superclass.  Could happen if
             * superclass gets updated.
             */
            dvmThrowException("Ljava/lang/NoSuchMethodError;",
                baseMethod->name);
            goto exceptionThrown;
        }
        methodToCall = method->clazz->super->vtable[baseMethod->methodIndex];
#if 0
        if (dvmIsAbstractMethod(methodToCall)) {
            dvmThrowException("Ljava/lang/AbstractMethodError;",
                "abstract method not implemented");
            goto exceptionThrown;
        }
#else
        assert(!dvmIsAbstractMethod(methodToCall) ||
            methodToCall->nativeFunc != NULL);
#endif
        LOGVV("+++ base=%s.%s super-virtual=%s.%s\n",
            baseMethod->clazz->descriptor, baseMethod->name,
            methodToCall->clazz->descriptor, methodToCall->name);
        assert(methodToCall != NULL);

        goto invokeMethod;
    }

HANDLE_OPCODE(OP_INVOKE_INTERFACE_RANGE /*{vCCCC..v(CCCC+AA-1)}, meth@BBBB*/)
    methodCallRange = true;
    goto invokeInterface;
HANDLE_OPCODE(OP_INVOKE_INTERFACE /*vB, {vD, vE, vF, vG, vA}, meth@CCCC*/)
    methodCallRange = false;
invokeInterface:
    {
        Object* thisPtr;
        ClassObject* thisClass;

        EXPORT_PC();

        vsrc1 = INST_AA(inst);      /* AA (count) or BA (count + arg 5) */
        ref = FETCH(1);             /* method ref */
        vdst = FETCH(2);            /* 4 regs -or- first reg */

        /*
         * The object against which we are executing a method is always
         * in the first argument.
         */
        assert(vsrc1 > 0);
        if (methodCallRange) {
            ILOGV("|invoke-interface-range args=%d @0x%04x {regs=v%d-v%d}",
                vsrc1, ref, vdst, vdst+vsrc1-1);
            thisPtr = (Object*) GET_REGISTER(vdst);
        } else {
            ILOGV("|invoke-interface args=%d @0x%04x {regs=0x%04x %x}",
                vsrc1 >> 4, ref, vdst, vsrc1 & 0x0f);
            thisPtr = (Object*) GET_REGISTER(vdst & 0x0f);
        }
        if (!checkForNull(thisPtr))
            goto exceptionThrown;

        thisClass = thisPtr->clazz;

        /*
         * Given a class and a method index, find the Method* with the
         * actual code we want to execute.
         */
        methodToCall = dvmFindInterfaceMethodInCache(thisClass, ref, method,
                        methodClassDex);
        if (methodToCall == NULL) {
            assert(dvmCheckException(self));
            goto exceptionThrown;
        }

        goto invokeMethod;
    }


HANDLE_OPCODE(OP_INVOKE_DIRECT_RANGE /*{vCCCC..v(CCCC+AA-1)}, meth@BBBB*/)
    methodCallRange = true;
    goto invokeDirect;
HANDLE_OPCODE(OP_INVOKE_DIRECT_EMPTY /*vB, {vD, vE, vF, vG, vA}, meth@CCCC*/)
#if INTERP_TYPE != INTERP_DBG
    //LOGI("Ignoring empty\n");
    FINISH(3);
#else
    if (!gDvm.debuggerActive) {
        //LOGI("Skipping empty\n");
        FINISH(3);      // don't want it to show up in profiler output
    } else {
        //LOGI("Running empty\n");
        /* fall through to OP_INVOKE_DIRECT */
    }
#endif
HANDLE_OPCODE(OP_INVOKE_DIRECT /*vB, {vD, vE, vF, vG, vA}, meth@CCCC*/)
    methodCallRange = false;
invokeDirect:
    {
        u2 thisReg;

        vsrc1 = INST_AA(inst);      /* AA (count) or BA (count + arg 5) */
        ref = FETCH(1);             /* method ref */
        vdst = FETCH(2);            /* 4 regs -or- first reg */

        EXPORT_PC();

        if (methodCallRange) {
            ILOGV("|invoke-direct args=%d @0x%04x {regs=0x%04x %x}",
                vsrc1 >> 4, ref, vdst, vsrc1 & 0x0f);
            thisReg = vdst;
        } else {
            ILOGV("|invoke-direct-range args=%d @0x%04x {regs=v%d-v%d}",
                vsrc1, ref, vdst, vdst+vsrc1-1);
            thisReg = vdst & 0x0f;
        }
        if (!checkForNull((Object*) GET_REGISTER(thisReg)))
            goto exceptionThrown;

        methodToCall = dvmDexGetResolvedMethod(methodClassDex, ref);
        if (methodToCall == NULL) {
            methodToCall = dvmResolveMethod(method->clazz, ref, METHOD_DIRECT);
            if (methodToCall == NULL) {
                ILOGV("+ unknown direct method\n");     // should be impossible
                goto exceptionThrown;
            }
        }
        goto invokeMethod;
    }

HANDLE_OPCODE(OP_INVOKE_STATIC_RANGE /*{vCCCC..v(CCCC+AA-1)}, meth@BBBB*/)
    methodCallRange = true;
    goto invokeStatic;
HANDLE_OPCODE(OP_INVOKE_STATIC /*vB, {vD, vE, vF, vG, vA}, meth@CCCC*/)
    methodCallRange = false;
invokeStatic:
    vsrc1 = INST_AA(inst);      /* AA (count) or BA (count + arg 5) */
    ref = FETCH(1);             /* method ref */
    vdst = FETCH(2);            /* 4 regs -or- first reg */

    EXPORT_PC();

    if (methodCallRange)
        ILOGV("|invoke-static-range args=%d @0x%04x {regs=v%d-v%d}",
            vsrc1, ref, vdst, vdst+vsrc1-1);
    else
        ILOGV("|invoke-static args=%d @0x%04x {regs=0x%04x %x}",
            vsrc1 >> 4, ref, vdst, vsrc1 & 0x0f);

    methodToCall = dvmDexGetResolvedMethod(methodClassDex, ref);
    if (methodToCall == NULL) {
        methodToCall = dvmResolveMethod(method->clazz, ref, METHOD_STATIC);
        if (methodToCall == NULL) {
            ILOGV("+ unknown method\n");
            goto exceptionThrown;
        }
    }
    goto invokeMethod;


HANDLE_OPCODE(OP_INVOKE_VIRTUAL_QUICK_RANGE/*{vCCCC..v(CCCC+AA-1)}, meth@BBBB*/)
    methodCallRange = true;
    goto invokeVirtualQuick;
HANDLE_OPCODE(OP_INVOKE_VIRTUAL_QUICK /*vB, {vD, vE, vF, vG, vA}, meth@CCCC*/)
    methodCallRange = false;
invokeVirtualQuick:
    {
        Object* thisPtr;

        EXPORT_PC();

        vsrc1 = INST_AA(inst);      /* AA (count) or BA (count + arg 5) */
        ref = FETCH(1);             /* vtable index */
        vdst = FETCH(2);            /* 4 regs -or- first reg */

        /*
         * The object against which we are executing a method is always
         * in the first argument.
         */
        assert(vsrc1 > 0);
        if (methodCallRange) {
            ILOGV("|invoke-virtual-quick-range args=%d @0x%04x {regs=v%d-v%d}",
                vsrc1, ref, vdst, vdst+vsrc1-1);
            thisPtr = (Object*) GET_REGISTER(vdst);
        } else {
            ILOGV("|invoke-virtual-quick args=%d @0x%04x {regs=0x%04x %x}",
                vsrc1 >> 4, ref, vdst, vsrc1 & 0x0f);
            thisPtr = (Object*) GET_REGISTER(vdst & 0x0f);
        }

        if (!checkForNull(thisPtr))
            goto exceptionThrown;

        /*
         * Combine the object we found with the vtable offset in the
         * method.
         */
        assert(ref < thisPtr->clazz->vtableCount);
        methodToCall = thisPtr->clazz->vtable[ref];

#if 0
        if (dvmIsAbstractMethod(methodToCall)) {
            dvmThrowException("Ljava/lang/AbstractMethodError;",
                "abstract method not implemented");
            goto exceptionThrown;
        }
#else
        assert(!dvmIsAbstractMethod(methodToCall) ||
            methodToCall->nativeFunc != NULL);
#endif

        LOGVV("+++ virtual[%d]=%s.%s\n",
            ref, methodToCall->clazz->descriptor, methodToCall->name);
        assert(methodToCall != NULL);

        goto invokeMethod;
    }
HANDLE_OPCODE(OP_INVOKE_SUPER_QUICK_RANGE /*{vCCCC..v(CCCC+AA-1)}, meth@BBBB*/)
    methodCallRange = true;
    goto invokeSuperQuick;
HANDLE_OPCODE(OP_INVOKE_SUPER_QUICK /*vB, {vD, vE, vF, vG, vA}, meth@CCCC*/)
    methodCallRange = false;
invokeSuperQuick:
    {
        u2 thisReg;

        EXPORT_PC();

        vsrc1 = INST_AA(inst);      /* AA (count) or BA (count + arg 5) */
        ref = FETCH(1);             /* vtable index */
        vdst = FETCH(2);            /* 4 regs -or- first reg */

        if (methodCallRange) {
            ILOGV("|invoke-super-quick-range args=%d @0x%04x {regs=v%d-v%d}",
                vsrc1, ref, vdst, vdst+vsrc1-1);
            thisReg = vdst;
        } else {
            ILOGV("|invoke-super-quick args=%d @0x%04x {regs=0x%04x %x}",
                vsrc1 >> 4, ref, vdst, vsrc1 & 0x0f);
            thisReg = vdst & 0x0f;
        }
        /* impossible in well-formed code, but we must check nevertheless */
        if (!checkForNull((Object*) GET_REGISTER(thisReg)))
            goto exceptionThrown;

#if 0   /* impossible in optimized + verified code */
        if (ref >= method->clazz->super->vtableCount) {
            dvmThrowException("Ljava/lang/NoSuchMethodError;", NULL);
            goto exceptionThrown;
        }
#else
        assert(ref < method->clazz->super->vtableCount);
#endif

        /*
         * Combine the object we found with the vtable offset in the
         * method's class.
         *
         * We're using the current method's class' superclass, not the
         * superclass of "this".  This is because we might be executing
         * in a method inherited from a superclass, and we want to run
         * in the method's class' superclass.
         */
        methodToCall = method->clazz->super->vtable[ref];

#if 0
        if (dvmIsAbstractMethod(methodToCall)) {
            dvmThrowException("Ljava/lang/AbstractMethodError;",
                "abstract method not implemented");
            goto exceptionThrown;
        }
#else
        assert(!dvmIsAbstractMethod(methodToCall) ||
            methodToCall->nativeFunc != NULL);
#endif
        LOGVV("+++ super-virtual[%d]=%s.%s\n",
            ref, methodToCall->clazz->descriptor, methodToCall->name);
        assert(methodToCall != NULL);

        goto invokeMethod;
    }


    /*
     * General handling for invoke-{virtual,super,direct,static,interface},
     * including "quick" variants.
     *
     * Set "methodToCall" to the Method we're calling, and "methodCallRange"
     * depending on whether this is a "/range" instruction.
     *
     * For a range call:
     *  "vsrc1" holds the argument count (8 bits)
     *  "vdst" holds the first argument in the range
     * For a non-range call:
     *  "vsrc1" holds the argument count (4 bits) and the 5th argument index
     *  "vdst" holds four 4-bit register indices
     *
     * The caller must EXPORT_PC before jumping here, because any method
     * call can throw a stack overflow exception.
     */
invokeMethod:
    {
        u4* outs;
        int i;

        /*
         * Copy args.  This may corrupt vsrc1/vdst.
         */
        if (methodCallRange) {
            // could use memcpy or a "Duff's device"; most functions have
            // so few args it won't matter much
            assert(vsrc1 <= method->outsSize);
            assert(vsrc1 == methodToCall->insSize);
            outs = OUTS_FROM_FP(fp, vsrc1);
            for (i = 0; i < vsrc1; i++)
                outs[i] = GET_REGISTER(vdst+i);
        } else {
            u4 count = vsrc1 >> 4;

            assert(count <= method->outsSize);
            assert(count == methodToCall->insSize);
            assert(count <= 5);

            outs = OUTS_FROM_FP(fp, count);
#if 0
            if (count == 5) {
                outs[4] = GET_REGISTER(vsrc1 & 0x0f);
                count--;
            }
            for (i = 0; i < (int) count; i++) {
                outs[i] = GET_REGISTER(vdst & 0x0f);
                vdst >>= 4;
            }
#else
            // This version executes fewer instructions but is larger
            // overall.  Seems to be a teensy bit faster.
            assert((vdst >> 16) == 0);  // 16 bits -or- high 16 bits clear
            switch (count) {
            case 5:
                outs[4] = GET_REGISTER(vsrc1 & 0x0f);
            case 4:
                outs[3] = GET_REGISTER(vdst >> 12);
            case 3:
                outs[2] = GET_REGISTER((vdst & 0x0f00) >> 8);
            case 2:
                outs[1] = GET_REGISTER((vdst & 0x00f0) >> 4);
            case 1:
                outs[0] = GET_REGISTER(vdst & 0x0f);
            default:
                ;
            }
#endif
        }
    }

    /*
     * (This was originally a "goto" target; I've kept it separate from the
     * stuff above in case we want to refactor things again.)
     *
     * At this point, we have the arguments stored in the "outs" area of
     * the current method's stack frame, and the method to call in
     * "methodToCall".  Push a new stack frame.
     */
    {
        StackSaveArea* newSaveArea;
        u4* newFp;

        ILOGV("> %s%s.%s %s",
            dvmIsNativeMethod(methodToCall) ? "(NATIVE) " : "",
            methodToCall->clazz->descriptor, methodToCall->name,
            methodToCall->signature);

        newFp = (u4*) SAVEAREA_FROM_FP(fp) - methodToCall->registersSize;
        newSaveArea = SAVEAREA_FROM_FP(newFp);

        /* verify that we have enough space */
        if (true) {
            u1* bottom;
            bottom = (u1*) newSaveArea - methodToCall->outsSize * sizeof(u4);
            if (bottom < self->interpStackEnd) {
                /* stack overflow */
                LOGV("Stack overflow on method call (top=%p end=%p newBot=%p size=%d '%s')\n",
                    self->interpStackStart, self->interpStackEnd, bottom,
                    self->interpStackSize, methodToCall->name);
                dvmHandleStackOverflow(self);
                assert(dvmCheckException(self));
                goto exceptionThrown;
            }
            //LOGD("+++ fp=%p newFp=%p newSave=%p bottom=%p\n",
            //    fp, newFp, newSaveArea, bottom);
        }

#ifdef LOG_INSTR
        if (methodToCall->registersSize > methodToCall->insSize) {
            /*
             * This makes valgrind quiet when we print registers that
             * haven't been initialized.  Turn it off when the debug
             * messages are disabled -- we want valgrind to report any
             * used-before-initialized issues.
             */
            memset(newFp, 0xcc,
                (methodToCall->registersSize - methodToCall->insSize) * 4);
        }
#endif

#ifdef EASY_GDB
        newSaveArea->prevSave = SAVEAREA_FROM_FP(fp);
#endif
        newSaveArea->prevFrame = fp;
        newSaveArea->savedPc = pc;
        newSaveArea->method = methodToCall;

        if (!dvmIsNativeMethod(methodToCall)) {
            /*
             * "Call" interpreted code.  Reposition the PC, update the
             * frame pointer and other local state, and continue.
             */
            method = methodToCall;
            methodClassDex = method->clazz->pDvmDex;
            pc = methodToCall->insns;
            fp = self->curFrame = newFp;
#ifdef EASY_GDB
            debugSaveArea = SAVEAREA_FROM_FP(newFp);
#endif
#if INTERP_TYPE == INTERP_DBG
            debugIsMethodEntry = true;              // profiling, debugging
#endif
            ILOGD("> pc <-- %s.%s %s", method->clazz->descriptor, method->name,
                method->signature);
            DUMP_REGS(method, fp, true);            // show input args
            FINISH(0);                              // jump to method start
        } else {
            /* set this up for JNI locals, even if not a JNI native */
            newSaveArea->xtra.localRefTop = self->jniLocalRefTable.nextEntry;

            self->curFrame = newFp;

            DUMP_REGS(methodToCall, newFp, true);   // show input args

#if (INTERP_TYPE == INTERP_DBG) && defined(WITH_DEBUGGER)
            if (gDvm.debuggerActive) {
                dvmDbgPostLocationEvent(methodToCall, -1,
                    dvmGetThisPtr(method, fp), DBG_METHOD_ENTRY);
            }
#endif
#if (INTERP_TYPE == INTERP_DBG) && defined(WITH_PROFILER)
            TRACE_METHOD_ENTER(self, methodToCall);
#endif

            ILOGD("> native <-- %s.%s %s", methodToCall->clazz->descriptor,
                methodToCall->name, methodToCall->signature);

            /*
             * Jump through native call bridge.  Because we leave no
             * space for locals on native calls, "newFp" points directly
             * to the method arguments.
             */
            (*methodToCall->nativeFunc)(newFp, &retval, methodToCall, self);

#ifdef WITH_EXTRA_OBJECT_VALIDATION
            if (methodToCall->shorty[0] == 'L' && !dvmCheckException(self)) {
                assert(retval.l == NULL || dvmIsValidObject(retval.l));
            }
#endif
#if (INTERP_TYPE == INTERP_DBG) && defined(WITH_DEBUGGER)
            if (gDvm.debuggerActive) {
                dvmDbgPostLocationEvent(methodToCall, -1,
                    dvmGetThisPtr(method, fp), DBG_METHOD_EXIT);
            }
#endif
#if (INTERP_TYPE == INTERP_DBG) && defined(WITH_PROFILER)
            TRACE_METHOD_EXIT(self, methodToCall);
#endif

            /* pop frame off */
            dvmPopJniLocals(self, newSaveArea);
            self->curFrame = fp;

            /*
             * If the native code threw an exception, or interpreted code
             * invoked by the native call threw one and nobody has cleared
             * it, jump to our local exception handling.
             */
            if (dvmCheckException(self)) {
                LOGV("Exception thrown by/below native code\n");
                goto exceptionThrown;
            }

            ILOGD("> retval=0x%llx (leaving native)", retval.j);
            ILOGD("> (return from native %s.%s to %s.%s %s)",
                methodToCall->clazz->descriptor, methodToCall->name,
                method->clazz->descriptor, method->name,
                method->signature);

            //u2 invokeInstr = INST_INST(FETCH(0));
            if (true /*invokeInstr >= OP_INVOKE_VIRTUAL &&
                invokeInstr <= OP_INVOKE_INTERFACE*/)
            {
                FINISH(3);
            } else {
                //LOGE("Unknown invoke instr %02x at %d\n",
                //    invokeInstr, (int) (pc - method->insns));
                assert(false);
            }
        }
    }
    assert(false);      // should not get here


HANDLE_OPCODE(OP_RETURN_WIDE /*vAA*/)
    vsrc1 = INST_AA(inst);
    ILOGV("|return-wide v%d", vsrc1);
    retval.j = GET_REGISTER_WIDE(vsrc1);
    goto returnFromMethod;

HANDLE_OPCODE(OP_RETURN_VOID /**/)
    ILOGV("|return-void");
#ifndef NDEBUG
    retval.j = 0xababababULL;    // placate valgrind
#endif
    goto returnFromMethod;

HANDLE_OPCODE(OP_RETURN /*vAA*/)
HANDLE_OPCODE(OP_RETURN_OBJECT /*vAA*/)
    vsrc1 = INST_AA(inst);
    ILOGV("|return%s v%d",
        (INST_INST(inst) == OP_RETURN) ? "" : "-object", vsrc1);
    retval.i = GET_REGISTER(vsrc1);
    goto returnFromMethod;
    /* gcc removes the goto -- these are 2x more common than return-void */

    /*
     * General handling for return-void, return, and return-wide.  Put the
     * return value in "retval" before jumping here.
     */
returnFromMethod:
    {
        StackSaveArea* saveArea;

        /*
         * We must do this BEFORE we pop the previous stack frame off, so
         * that the GC can see the return value (if any) in the local vars.
         *
         * Since this is now an interpreter switch point, we must do it before
         * we do anything at all.
         */
        PERIODIC_CHECKS(kInterpEntryReturn, 0);

        ILOGV("> retval=0x%llx (leaving %s.%s %s)",
            retval.j, method->clazz->descriptor, method->name,
            method->signature);
        //DUMP_REGS(method, fp);

        saveArea = SAVEAREA_FROM_FP(fp);

#ifdef EASY_GDB
        debugSaveArea = saveArea;
#endif
#if (INTERP_TYPE == INTERP_DBG) && defined(WITH_PROFILER)
        TRACE_METHOD_EXIT(self, method);
#endif

        /* back up to previous frame and see if we hit a break */
        fp = saveArea->prevFrame;
        assert(fp != NULL);
        if (dvmIsBreakFrame(fp)) {
            /* bail without popping the method frame from stack */
            LOGVV("+++ returned into break frame\n");
            goto bail;
        }

        /* update thread FP, and reset local variables */
        self->curFrame = fp;
        method = SAVEAREA_FROM_FP(fp)->method;
        //methodClass = method->clazz;
        methodClassDex = method->clazz->pDvmDex;
        pc = saveArea->savedPc;
        ILOGD("> (return to %s.%s %s)", method->clazz->descriptor,
            method->name, method->signature);

        /* use FINISH on the caller's invoke instruction */
        //u2 invokeInstr = INST_INST(FETCH(0));
        if (true /*invokeInstr >= OP_INVOKE_VIRTUAL &&
            invokeInstr <= OP_INVOKE_INTERFACE*/)
        {
            FINISH(3);
        } else {
            //LOGE("Unknown invoke instr %02x at %d\n",
            //    invokeInstr, (int) (pc - method->insns));
            assert(false);
        }
    }


    /*
     * Jump here when the code throws an exception.
     *
     * By the time we get here, the Throwable has been created and the stack
     * trace has been saved off.
     */
exceptionThrown:
    {
        Object* exception;
        int catchRelPc;

        /*
         * Since this is now an interpreter switch point, we must do it before
         * we do anything at all.
         */
        PERIODIC_CHECKS(kInterpEntryThrow, 0);

        /*
         * We save off the exception and clear the exception status.  While
         * processing the exception we might need to load some Throwable
         * classes, and we don't want class loader exceptions to get
         * confused with this one.
         */
        assert(dvmCheckException(self));
        exception = dvmGetException(self);
        dvmAddTrackedAlloc(exception, self);
        dvmClearException(self);

        LOGV("Handling exception %s at %s:%d\n",
            exception->clazz->descriptor, method->name,
            dvmLineNumFromPC(method, pc - method->insns));

#if (INTERP_TYPE == INTERP_DBG) && defined(WITH_DEBUGGER)
        /*
         * Tell the debugger about it.
         *
         * TODO: if the exception was thrown by interpreted code, control
         * fell through native, and then back to us, we will report the
         * exception at the point of the throw and again here.  We can avoid
         * this by not reporting exceptions when we jump here directly from
         * the native call code above, but then we won't report exceptions
         * that were thrown *from* the JNI code (as opposed to *through* it).
         *
         * The correct solution is probably to ignore from-native exceptions
         * here, and have the JNI exception code do the reporting to the
         * debugger.
         */
        if (gDvm.debuggerActive) {
            void* catchFrame;
            catchRelPc = dvmFindCatchBlock(self, pc - method->insns,
                        exception, true, &catchFrame);
            dvmDbgPostException(fp, pc - method->insns, catchFrame, catchRelPc,
                exception);
        }
#endif

        /*
         * We need to unroll to the catch block or the nearest "break"
         * frame.
         *
         * A break frame could indicate that we have reached an intermediate
         * native call, or have gone off the top of the stack and the thread
         * needs to exit.  Either way, we return from here, leaving the
         * exception raised.
         *
         * If we do find a catch block, we want to transfer execution to
         * that point.
         */
        catchRelPc = dvmFindCatchBlock(self, pc - method->insns,
                    exception, false, (void*)&fp);

        /*
         * Restore the stack bounds after an overflow.  This isn't going to
         * be correct in all circumstances, e.g. if JNI code devours the
         * exception this won't happen until some other exception gets
         * thrown.  If the code keeps pushing the stack bounds we'll end
         * up aborting the VM.
         */
        if (self->stackOverflowed)
            dvmCleanupStackOverflow(self);

        if (catchRelPc < 0) {
            /* falling through to JNI code or off the bottom of the stack */
#if DVM_SHOW_EXCEPTION >= 2
            LOGD("Exception %s from %s:%d not caught locally\n",
                exception->clazz->descriptor, dvmGetMethodSourceFile(method),
                dvmLineNumFromPC(method, pc - method->insns));
#endif
            dvmSetException(self, exception);
            dvmReleaseTrackedAlloc(exception, self);
            goto bail;
        }

#if DVM_SHOW_EXCEPTION >= 3
        {
            const Method* catchMethod = SAVEAREA_FROM_FP(fp)->method;
            LOGD("Exception %s thrown from %s:%d to %s:%d\n",
                exception->clazz->descriptor, dvmGetMethodSourceFile(method),
                dvmLineNumFromPC(method, pc - method->insns),
                dvmGetMethodSourceFile(catchMethod),
                dvmLineNumFromPC(catchMethod, catchRelPc));
        }
#endif

        /*
         * Adjust local variables to match self->curFrame and the
         * updated PC.
         */
        //fp = (u4*) self->curFrame;
        method = SAVEAREA_FROM_FP(fp)->method;
        //methodClass = method->clazz;
        methodClassDex = method->clazz->pDvmDex;
        pc = method->insns + catchRelPc;
        ILOGV("> pc <-- %s.%s %s", method->clazz->descriptor, method->name,
            method->signature);
        DUMP_REGS(method, fp, false);               // show all regs

        /*
         * Restore the exception if the handler wants it.
         *
         * The Dalvik spec mandates that, if an exception handler wants to
         * do something with the exception, the first instruction executed
         * must be "move-exception".  We can pass the exception along
         * through the thread struct, and let the move-exception instruction
         * clear it for us.
         *
         * If the handler doesn't call move-exception, we don't want to
         * finish here with an exception still pending.
         */
        if (INST_INST(FETCH(0)) == OP_MOVE_EXCEPTION)
            dvmSetException(self, exception);

        dvmReleaseTrackedAlloc(exception, self);
        FINISH(0);
    }

#ifndef THREADED_INTERP
        // ---------- bottom of instruction switch statement ----------
        default:
            /* fall into OP_UNUSED code */
#endif

HANDLE_OPCODE(OP_UNUSED_3E)
HANDLE_OPCODE(OP_UNUSED_3F)
HANDLE_OPCODE(OP_UNUSED_40)
HANDLE_OPCODE(OP_UNUSED_41)
HANDLE_OPCODE(OP_UNUSED_42)
HANDLE_OPCODE(OP_UNUSED_43)
HANDLE_OPCODE(OP_UNUSED_73)
HANDLE_OPCODE(OP_UNUSED_79)
HANDLE_OPCODE(OP_UNUSED_7A)
HANDLE_OPCODE(OP_UNUSED_E3)
HANDLE_OPCODE(OP_UNUSED_E4)
HANDLE_OPCODE(OP_UNUSED_E5)
HANDLE_OPCODE(OP_UNUSED_E6)
HANDLE_OPCODE(OP_UNUSED_E7)
HANDLE_OPCODE(OP_UNUSED_E8)
HANDLE_OPCODE(OP_UNUSED_E9)
HANDLE_OPCODE(OP_UNUSED_EA)
HANDLE_OPCODE(OP_UNUSED_EB)
HANDLE_OPCODE(OP_UNUSED_EC)
HANDLE_OPCODE(OP_UNUSED_ED)
HANDLE_OPCODE(OP_UNUSED_EF)
HANDLE_OPCODE(OP_UNUSED_F1)
HANDLE_OPCODE(OP_UNUSED_FC)
HANDLE_OPCODE(OP_UNUSED_FD)
HANDLE_OPCODE(OP_UNUSED_FE)
HANDLE_OPCODE(OP_UNUSED_FF)
    LOGE("unknown opcode 0x%02x\n", INST_INST(inst));
    assert(false);
    EXPORT_PC();
    dvmThrowException("Ljava/lang/InternalError;", "unknown opcode");
    goto exceptionThrown;

#ifndef THREADED_INTERP
        } // end of "switch"
    } // end of "while"
#endif

bail:
    ILOGD("|-- Leaving interpreter loop");      // note "method" may be NULL

    interpState->retval = retval;
    return false;

bail_switch:
    /*
     * The standard interpreter currently doesn't set or care about the
     * "debugIsMethodEntry" value, so setting this is only of use if we're
     * switching between two "debug" interpreters, which we never do.
     *
     * TODO: figure out if preserving this makes any sense.
     */
#if defined(WITH_PROFILER) || defined(WITH_DEBUGGER)
# if INTERP_TYPE == INTERP_DBG
    interpState->debugIsMethodEntry = debugIsMethodEntry;
# else
    interpState->debugIsMethodEntry = false;
# endif
#endif

    /* export state changes */
    interpState->method = method;
    interpState->pc = pc;
    interpState->fp = fp;
    /* debugTrackedRefStart doesn't change */
    interpState->retval = retval;   /* need for _entryPoint=ret */
    interpState->nextMode =
        (INTERP_TYPE == INTERP_STD) ? INTERP_DBG : INTERP_STD;
    LOGVV(" meth='%s.%s' pc=0x%x fp=%p\n",
        method->clazz->descriptor, method->name,
        pc - method->insns, fp);
    return true;
}
