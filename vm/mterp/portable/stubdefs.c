/*
 * In the C mterp stubs, "goto" is a function call followed immediately
 * by a return.
 */

#define GOTO_TARGET_DECL(_target, ...)

#define GOTO_TARGET(_target, ...) _target:

#define GOTO_TARGET_END

/* ugh */
#define STUB_HACK(x)

/*
 * Instruction framing.  For a switch-oriented implementation this is
 * case/break, for a threaded implementation it's a goto label and an
 * instruction fetch/computed goto.
 *
 * Assumes the existence of "const u2* pc" and (for threaded operation)
 * "u2 inst".
 *
 * TODO: remove "switch" version.
 */
#ifdef THREADED_INTERP
# define H(_op)             &&op_##_op
# define HANDLE_OPCODE(_op) op_##_op:
# define FINISH(_offset) {                                                  \
        ADJUST_PC(_offset);                                                 \
        inst = FETCH(0);                                                    \
        CHECK_DEBUG_AND_PROF();                                             \
        CHECK_TRACKED_REFS();                                               \
        if (CHECK_JIT_BOOL()) GOTO_bail_switch();                           \
        goto *handlerTable[INST_INST(inst)];                                \
    }
# define FINISH_BKPT(_opcode) {                                             \
        goto *handlerTable[_opcode];                                        \
    }
#else
# define HANDLE_OPCODE(_op) case _op:
# define FINISH(_offset)    { ADJUST_PC(_offset); break; }
# define FINISH_BKPT(opcode) { > not implemented < }
#endif

#define OP_END

#if defined(WITH_TRACKREF_CHECKS)
# define CHECK_TRACKED_REFS() \
    dvmInterpCheckTrackedRefs(self, curMethod, debugTrackedRefStart)
#else
# define CHECK_TRACKED_REFS() ((void)0)
#endif


/*
 * The "goto" targets just turn into goto statements.  The "arguments" are
 * passed through local variables.
 */

#define GOTO_exceptionThrown() goto exceptionThrown;

#define GOTO_returnFromMethod() goto returnFromMethod;

#define GOTO_invoke(_target, _methodCallRange)                              \
    do {                                                                    \
        methodCallRange = _methodCallRange;                                 \
        goto _target;                                                       \
    } while(false)

/* for this, the "args" are already in the locals */
#define GOTO_invokeMethod(_methodCallRange, _methodToCall, _vsrc1, _vdst) goto invokeMethod;

#define GOTO_bail() goto bail;
#define GOTO_bail_switch() goto bail_switch;

/*
 * Periodically check for thread suspension.
 *
 * While we're at it, see if a debugger has attached or the profiler has
 * started.  If so, switch to a different "goto" table.
 */
#define PERIODIC_CHECKS(_entryPoint, _pcadj) {                              \
        if (dvmCheckSuspendQuick(self)) {                                   \
            EXPORT_PC();  /* need for precise GC */                         \
            dvmCheckSuspendPending(self);                                   \
        }                                                                   \
        if (NEED_INTERP_SWITCH(INTERP_TYPE)) {                              \
            ADJUST_PC(_pcadj);                                              \
            interpState->entryPoint = _entryPoint;                          \
            LOGVV("threadid=%d: switch to %s ep=%d adj=%d\n",               \
                self->threadId,                                             \
                (interpState->nextMode == INTERP_STD) ? "STD" : "DBG",      \
                (_entryPoint), (_pcadj));                                   \
            GOTO_bail_switch();                                             \
        }                                                                   \
    }
