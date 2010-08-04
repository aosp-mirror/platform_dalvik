/* this is a standard (no debug support) interpreter */
#define INTERP_TYPE INTERP_STD
#define CHECK_DEBUG_AND_PROF() ((void)0)
# define CHECK_TRACKED_REFS() ((void)0)
#define CHECK_JIT_BOOL() (false)
#define CHECK_JIT_VOID()
#define ABORT_JIT_TSELECT() ((void)0)

/*
 * In the C mterp stubs, "goto" is a function call followed immediately
 * by a return.
 */

#define GOTO_TARGET_DECL(_target, ...)                                      \
    void dvmMterp_##_target(MterpGlue* glue, ## __VA_ARGS__);

#define GOTO_TARGET(_target, ...)                                           \
    void dvmMterp_##_target(MterpGlue* glue, ## __VA_ARGS__) {              \
        u2 ref, vsrc1, vsrc2, vdst;                                         \
        u2 inst = FETCH(0);                                                 \
        const Method* methodToCall;                                         \
        StackSaveArea* debugSaveArea;

#define GOTO_TARGET_END }

/*
 * Redefine what used to be local variable accesses into MterpGlue struct
 * references.  (These are undefined down in "footer.c".)
 */
#define retval                  glue->retval
#define pc                      glue->pc
#define fp                      glue->fp
#define curMethod               glue->method
#define methodClassDex          glue->methodClassDex
#define self                    glue->self
#define debugTrackedRefStart    glue->debugTrackedRefStart

/* ugh */
#define STUB_HACK(x) x


/*
 * Opcode handler framing macros.  Here, each opcode is a separate function
 * that takes a "glue" argument and returns void.  We can't declare
 * these "static" because they may be called from an assembly stub.
 */
#define HANDLE_OPCODE(_op)                                                  \
    void dvmMterp_##_op(MterpGlue* glue) {                                  \
        u2 ref, vsrc1, vsrc2, vdst;                                         \
        u2 inst = FETCH(0);

#define OP_END }

/*
 * Like the "portable" FINISH, but don't reload "inst", and return to caller
 * when done.
 */
#define FINISH(_offset) {                                                   \
        ADJUST_PC(_offset);                                                 \
        CHECK_DEBUG_AND_PROF();                                             \
        CHECK_TRACKED_REFS();                                               \
        return;                                                             \
    }


/*
 * The "goto label" statements turn into function calls followed by
 * return statements.  Some of the functions take arguments, which in the
 * portable interpreter are handled by assigning values to globals.
 */

#define GOTO_exceptionThrown()                                              \
    do {                                                                    \
        dvmMterp_exceptionThrown(glue);                                     \
        return;                                                             \
    } while(false)

#define GOTO_returnFromMethod()                                             \
    do {                                                                    \
        dvmMterp_returnFromMethod(glue);                                    \
        return;                                                             \
    } while(false)

#define GOTO_invoke(_target, _methodCallRange)                              \
    do {                                                                    \
        dvmMterp_##_target(glue, _methodCallRange);                         \
        return;                                                             \
    } while(false)

#define GOTO_invokeMethod(_methodCallRange, _methodToCall, _vsrc1, _vdst)   \
    do {                                                                    \
        dvmMterp_invokeMethod(glue, _methodCallRange, _methodToCall,        \
            _vsrc1, _vdst);                                                 \
        return;                                                             \
    } while(false)

/*
 * As a special case, "goto bail" turns into a longjmp.  Use "bail_switch"
 * if we need to switch to the other interpreter upon our return.
 */
#define GOTO_bail()                                                         \
    dvmMterpStdBail(glue, false);
#define GOTO_bail_switch()                                                  \
    dvmMterpStdBail(glue, true);

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
            glue->entryPoint = _entryPoint;                                 \
            LOGVV("threadid=%d: switch to STD ep=%d adj=%d\n",              \
                self->threadId, (_entryPoint), (_pcadj));                   \
            GOTO_bail_switch();                                             \
        }                                                                   \
    }
