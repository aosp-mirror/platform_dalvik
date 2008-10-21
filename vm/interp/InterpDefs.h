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
 * Dalvik interpreter definitions.  These are internal to the interpreter.
 *
 * This includes defines, types, function declarations, and inline functions
 * that are common to all interpreter implementations.
 *
 * Functions and globals declared here are defined in Interp.c.
 */
#ifndef _DALVIK_INTERP_DEFS
#define _DALVIK_INTERP_DEFS


/*
 * Specify the starting point when switching between interpreters.
 */
typedef enum InterpEntry {
    kInterpEntryInstr = 0,      // continue to next instruction
    kInterpEntryReturn = 1,     // jump to method return
    kInterpEntryThrow = 2,      // jump to exception throw
} InterpEntry;

/*
 * Interpreter context, used when switching from one interpreter to
 * another.  We also tuck "mterp" state in here.
 */
typedef struct InterpState {
    /*
     * To make some mterp state updates easier, "pc" and "fp" MUST come
     * first and MUST appear in this order.
     */
    const u2*   pc;                     // program counter
    u4*         fp;                     // frame pointer

    JValue      retval;                 // return value -- "out" only
    const Method* method;               // method being executed


    /* ----------------------------------------------------------------------
     * Mterp-only state
     */
    DvmDex*         methodClassDex;
    Thread*         self;

    /* housekeeping */
    void*           bailPtr;

    /*
     * These are available globally, from gDvm, or from another glue field
     * (self/method).  They're included for speed.
     */
    const u1*       interpStackEnd;
    volatile int*   pSelfSuspendCount;
#if defined(WITH_DEBUGGER)
    volatile bool*  pDebuggerActive;
#endif
#if defined(WITH_PROFILER)
    volatile int*   pActiveProfilers;
#endif
    /* ----------------------------------------------------------------------
     */

    /*
     * Interpreter switching.
     */
    InterpEntry entryPoint;             // what to do when we start
    int         nextMode;               // INTERP_STD or INTERP_DBG


#if defined(WITH_PROFILER) || defined(WITH_DEBUGGER)
    bool        debugIsMethodEntry;     // used for method entry event triggers
#endif
#if defined(WITH_TRACKREF_CHECKS)
    int         debugTrackedRefStart;   // tracked refs from prior invocations
#endif

} InterpState;

/*
 * These are generated from InterpCore.h.
 */
extern bool dvmInterpretDbg(Thread* self, InterpState* interpState);
extern bool dvmInterpretStd(Thread* self, InterpState* interpState);
#define INTERP_STD 0
#define INTERP_DBG 1

/*
 * "mterp" interpreter.
 */
extern bool dvmMterpStd(Thread* self, InterpState* interpState);

/*
 * Get the "this" pointer from the current frame.
 */
Object* dvmGetThisPtr(const Method* method, const u4* fp);

/*
 * Verify that our tracked local references are valid.
 */
void dvmInterpCheckTrackedRefs(Thread* self, const Method* method,
    int debugTrackedRefStart);

/*
 * Process switch statement.
 */
s4 dvmInterpHandlePackedSwitch(const u2* switchData, s4 testVal);
s4 dvmInterpHandleSparseSwitch(const u2* switchData, s4 testVal);

/*
 * Process fill-array-data.
 */
bool dvmInterpHandleFillArrayData(ArrayObject* arrayObject, 
                                  const u2* arrayData);

/*
 * Find an interface method.
 */
Method* dvmInterpFindInterfaceMethod(ClassObject* thisClass, u4 methodIdx,
    const Method* method, DvmDex* methodClassDex);

/*
 * Configuration defines.
 *
 * Some defines are controlled by the Makefile, e.g.:
 *   WITH_PROFILER
 *   WITH_DEBUGGER
 *   WITH_INSTR_CHECKS
 *   WITH_TRACKREF_CHECKS
 *   EASY_GDB
 *   NDEBUG
 *
 * If THREADED_INTERP is not defined, we use a classic "while true / switch"
 * interpreter.  If it is defined, then the tail end of each instruction
 * handler fetches the next instruction and jumps directly to the handler.
 * This increases the size of the "Std" interpreter by about 10%, but
 * provides a speedup of about the same magnitude.
 *
 * There's a "hybrid" approach that uses a goto table instead of a switch
 * statement, avoiding the "is the opcode in range" tests required for switch.
 * The performance is close to the threaded version, and without the 10%
 * size increase, but the benchmark results are off enough that it's not
 * worth adding as a third option.
 */
#define THREADED_INTERP             /* threaded vs. while-loop interpreter */

#ifdef WITH_INSTR_CHECKS            /* instruction-level paranoia */
# define CHECK_BRANCH_OFFSETS
# define CHECK_REGISTER_INDICES
#endif

/*
 * ARM EABI requires 64-bit alignment for access to 64-bit data types.  We
 * can't just use pointers to copy 64-bit values out of our interpreted
 * register set, because gcc will generate ldrd/strd.
 *
 * The __UNION version copies data in and out of a union.  The __MEMCPY
 * version uses a memcpy() call to do the transfer; gcc is smart enough to
 * not actually call memcpy().  The __UNION version is very bad on ARM;
 * it only uses one more instruction than __MEMCPY, but for some reason
 * gcc thinks it needs separate storage for every instance of the union.
 * On top of that, it feels the need to zero them out at the start of the
 * method.  Net result is we zero out ~700 bytes of stack space at the top
 * of the interpreter using ARM STM instructions.
 */
#if defined(__ARM_EABI__)
//# define NO_UNALIGN_64__UNION
# define NO_UNALIGN_64__MEMCPY
#endif

//#define LOG_INSTR                   /* verbose debugging */
/* set and adjust ANDROID_LOG_TAGS='*:i jdwp:i dalvikvm:i dalvikvmi:i' */

/*
 * Keep a tally of accesses to fields.  Currently only works if full DEX
 * optimization is disabled.
 */
#ifdef PROFILE_FIELD_ACCESS
# define UPDATE_FIELD_GET(_field) { (_field)->gets++; }
# define UPDATE_FIELD_PUT(_field) { (_field)->puts++; }
#else
# define UPDATE_FIELD_GET(_field) ((void)0)
# define UPDATE_FIELD_PUT(_field) ((void)0)
#endif

/*
 * Adjust the program counter.  "_offset" is a signed int, in 16-bit units.
 *
 * Assumes the existence of "const u2* pc" and "const u2* method->insns".
 *
 * We don't advance the program counter until we finish an instruction or
 * branch, because we do want to have to unroll the PC if there's an
 * exception.
 */
#ifdef CHECK_BRANCH_OFFSETS
# define ADJUST_PC(_offset) do {                                            \
        int myoff = _offset;        /* deref only once */                   \
        if (pc + myoff < method->insns ||                                   \
            pc + myoff >= method->insns + dvmGetMethodInsnsSize(method))    \
        {                                                                   \
            char* desc = dexProtoCopyMethodDescriptor(&method->prototype);  \
            LOGE("Invalid branch %d at 0x%04x in %s.%s %s\n",               \
                myoff, (int) (pc - method->insns),                          \
                method->clazz->descriptor, method->name, desc);             \
            free(desc);                                                     \
            dvmAbort();                                                     \
        }                                                                   \
        pc += myoff;                                                        \
    } while (false)
#else
# define ADJUST_PC(_offset) (pc += _offset)
#endif

/*
 * Instruction framing.  For a switch-oriented implementation this is
 * case/break, for a threaded implementation it's a goto label and an
 * instruction fetch/computed goto.
 *
 * Assumes the existence of "const u2* pc" and (for threaded operation)
 * "u2 inst".
 */
#ifdef THREADED_INTERP
# define H(_op)             &&op_##_op
# define HANDLE_OPCODE(_op) op_##_op:
# define FINISH(_offset) {                                                  \
        ADJUST_PC(_offset);                                                 \
        inst = FETCH(0);                                                    \
        CHECK_DEBUG_AND_PROF();                                             \
        CHECK_TRACKED_REFS();                                               \
        goto *handlerTable[INST_INST(inst)];                                \
    }
#else
# define HANDLE_OPCODE(_op) case _op:
# define FINISH(_offset)    { ADJUST_PC(_offset); break; }
#endif

#if INTERP_TYPE == INTERP_DBG
# define CHECK_DEBUG_AND_PROF() \
    checkDebugAndProf(pc, fp, self, method, &debugIsMethodEntry)
#else
# define CHECK_DEBUG_AND_PROF() ((void)0)
#endif

#if defined(WITH_TRACKREF_CHECKS)
# define CHECK_TRACKED_REFS() \
    dvmInterpCheckTrackedRefs(self, method, debugTrackedRefStart)
#else
# define CHECK_TRACKED_REFS() ((void)0)
#endif

/*
 * If enabled, log instructions as we execute them.
 */
#ifdef LOG_INSTR
# define ILOGD(...) ILOG(LOG_DEBUG, __VA_ARGS__)
# define ILOGV(...) ILOG(LOG_VERBOSE, __VA_ARGS__)
# define ILOG(_level, ...) do {                                             \
        char debugStrBuf[128];                                              \
        snprintf(debugStrBuf, sizeof(debugStrBuf), __VA_ARGS__);            \
        if (method != NULL)                                                 \
            LOG(_level, LOG_TAG"i", "%-2d|%04x%s\n",                        \
                self->threadId, (int) (pc - method->insns), debugStrBuf);   \
        else                                                                \
            LOG(_level, LOG_TAG"i", "%-2d|####%s\n",                        \
                self->threadId, debugStrBuf);                               \
    } while(false)
void dvmDumpRegs(const Method* method, const u4* framePtr, bool inOnly);
# define DUMP_REGS(_meth, _frame, _inOnly) dvmDumpRegs(_meth, _frame, _inOnly)
static const char kSpacing[] = "            ";
#else
# define ILOGD(...) ((void)0)
# define ILOGV(...) ((void)0)
# define DUMP_REGS(_meth, _frame, _inOnly) ((void)0)
#endif

/* get a long from an array of u4 */
static inline s8 getLongFromArray(const u4* ptr, int idx)
{
#if defined(NO_UNALIGN_64__UNION)
    union { s8 ll; u4 parts[2]; } conv;

    ptr += idx;
    conv.parts[0] = ptr[0];
    conv.parts[1] = ptr[1];
    return conv.ll;
#elif defined(NO_UNALIGN_64__MEMCPY)
    s8 val;
    memcpy(&val, &ptr[idx], 8);
    return val;
#else
    return *((s8*) &ptr[idx]);
#endif
}

/* store a long into an array of u4 */
static inline void putLongToArray(u4* ptr, int idx, s8 val)
{
#if defined(NO_UNALIGN_64__UNION)
    union { s8 ll; u4 parts[2]; } conv;

    ptr += idx;
    conv.ll = val;
    ptr[0] = conv.parts[0];
    ptr[1] = conv.parts[1];
#elif defined(NO_UNALIGN_64__MEMCPY)
    memcpy(&ptr[idx], &val, 8);
#else
    *((s8*) &ptr[idx]) = val;
#endif
}

/* get a double from an array of u4 */
static inline double getDoubleFromArray(const u4* ptr, int idx)
{
#if defined(NO_UNALIGN_64__UNION)
    union { double d; u4 parts[2]; } conv;

    ptr += idx;
    conv.parts[0] = ptr[0];
    conv.parts[1] = ptr[1];
    return conv.d;
#elif defined(NO_UNALIGN_64__MEMCPY)
    double dval;
    memcpy(&dval, &ptr[idx], 8);
    return dval;
#else
    return *((double*) &ptr[idx]);
#endif
}

/* store a double into an array of u4 */
static inline void putDoubleToArray(u4* ptr, int idx, double dval)
{
#if defined(NO_UNALIGN_64__UNION)
    union { double d; u4 parts[2]; } conv;

    ptr += idx;
    conv.d = dval;
    ptr[0] = conv.parts[0];
    ptr[1] = conv.parts[1];
#elif defined(NO_UNALIGN_64__MEMCPY)
    memcpy(&ptr[idx], &dval, 8);
#else
    *((double*) &ptr[idx]) = dval;
#endif
}

/*
 * If enabled, validate the register number on every access.  Otherwise,
 * just do an array access.
 *
 * Assumes the existence of "u4* fp".
 *
 * "_idx" may be referenced more than once.
 */
#ifdef CHECK_REGISTER_INDICES
# define GET_REGISTER(_idx) \
    ( (_idx) < method->registersSize ? \
        (fp[(_idx)]) : (assert(!"bad reg"),1969) )
# define SET_REGISTER(_idx, _val) \
    ( (_idx) < method->registersSize ? \
        (fp[(_idx)] = (u4)(_val)) : (assert(!"bad reg"),1969) )
# define GET_REGISTER_AS_OBJECT(_idx)       ((Object *)GET_REGISTER(_idx))
# define SET_REGISTER_AS_OBJECT(_idx, _val) SET_REGISTER(_idx, (s4)_val)
# define GET_REGISTER_INT(_idx) ((s4) GET_REGISTER(_idx))
# define SET_REGISTER_INT(_idx, _val) SET_REGISTER(_idx, (s4)_val)
# define GET_REGISTER_WIDE(_idx) \
    ( (_idx) < method->registersSize-1 ? \
        getLongFromArray(fp, (_idx)) : (assert(!"bad reg"),1969) )
# define SET_REGISTER_WIDE(_idx, _val) \
    ( (_idx) < method->registersSize-1 ? \
        putLongToArray(fp, (_idx), (_val)) : (assert(!"bad reg"),1969) )
# define GET_REGISTER_FLOAT(_idx) \
    ( (_idx) < method->registersSize ? \
        (*((float*) &fp[(_idx)])) : (assert(!"bad reg"),1969.0f) )
# define SET_REGISTER_FLOAT(_idx, _val) \
    ( (_idx) < method->registersSize ? \
        (*((float*) &fp[(_idx)]) = (_val)) : (assert(!"bad reg"),1969.0f) )
# define GET_REGISTER_DOUBLE(_idx) \
    ( (_idx) < method->registersSize-1 ? \
        getDoubleFromArray(fp, (_idx)) : (assert(!"bad reg"),1969.0) )
# define SET_REGISTER_DOUBLE(_idx, _val) \
    ( (_idx) < method->registersSize-1 ? \
        putDoubleToArray(fp, (_idx), (_val)) : (assert(!"bad reg"),1969.0) )
#else
# define GET_REGISTER(_idx)                 (fp[(_idx)])
# define SET_REGISTER(_idx, _val)           (fp[(_idx)] = (_val))
# define GET_REGISTER_AS_OBJECT(_idx)       ((Object*) fp[(_idx)])
# define SET_REGISTER_AS_OBJECT(_idx, _val) (fp[(_idx)] = (u4)(_val))
# define GET_REGISTER_INT(_idx)             ((s4)GET_REGISTER(_idx))
# define SET_REGISTER_INT(_idx, _val)       SET_REGISTER(_idx, (s4)_val)
# define GET_REGISTER_WIDE(_idx)            getLongFromArray(fp, (_idx))
# define SET_REGISTER_WIDE(_idx, _val)      putLongToArray(fp, (_idx), (_val))
# define GET_REGISTER_FLOAT(_idx)           (*((float*) &fp[(_idx)]))
# define SET_REGISTER_FLOAT(_idx, _val)     (*((float*) &fp[(_idx)]) = (_val))
# define GET_REGISTER_DOUBLE(_idx)          getDoubleFromArray(fp, (_idx))
# define SET_REGISTER_DOUBLE(_idx, _val)    putDoubleToArray(fp, (_idx), (_val))
#endif

/*
 * Get 16 bits from the specified offset of the program counter.  We always
 * want to load 16 bits at a time from the instruction stream -- it's more
 * efficient than 8 and won't have the alignment problems that 32 might.
 *
 * Assumes existence of "const u2* pc".
 */
#define FETCH(_offset)     (pc[(_offset)])

/*
 * Extract instruction byte from 16-bit fetch (_inst is a u2).
 */
#define INST_INST(_inst)    ((_inst) & 0xff)

/*
 * Extract the "vA, vB" 4-bit registers from the instruction word (_inst is u2).
 */
#define INST_A(_inst)       (((_inst) >> 8) & 0x0f)
#define INST_B(_inst)       ((_inst) >> 12)

/*
 * Get the 8-bit "vAA" 8-bit register index from the instruction word.
 * (_inst is u2)
 */
#define INST_AA(_inst)      ((_inst) >> 8)

/*
 * The current PC must be available to Throwable constructors, e.g.
 * those created by dvmThrowException(), so that the exception stack
 * trace can be generated correctly.  If we don't do this, the offset
 * within the current method won't be shown correctly.  See the notes
 * in Exception.c.
 *
 * Assumes existence of "u4* fp" and "const u2* pc".
 */
#define EXPORT_PC()         (SAVEAREA_FROM_FP(fp)->xtra.currentPc = pc)

/*
 * Determine if we need to switch to a different interpreter.  "_current"
 * is either INTERP_STD or INTERP_DBG.  It should be fixed for a given
 * interpreter generation file, which should remove the outer conditional
 * from the following.
 *
 * If we're building without debug and profiling support, we never switch.
 */
#if defined(WITH_PROFILER) || defined(WITH_DEBUGGER)
# define NEED_INTERP_SWITCH(_current) (                                     \
    (_current == INTERP_STD) ?                                              \
        dvmDebuggerOrProfilerActive() : !dvmDebuggerOrProfilerActive() )
#else
# define NEED_INTERP_SWITCH(_current) (false)
#endif

/*
 * Determine if the debugger or profiler is currently active.  Used when
 * selecting which interpreter to start or switch to.
 */
static inline bool dvmDebuggerOrProfilerActive(void)
{
    return gDvm.debuggerActive
#if defined(WITH_PROFILER)
        || gDvm.activeProfilers != 0
#endif
        ;
}

/*
 * Look up an interface on a class using the cache.
 */
INLINE Method* dvmFindInterfaceMethodInCache(ClassObject* thisClass,
    u4 methodIdx, const Method* method, DvmDex* methodClassDex)
{
#define ATOMIC_CACHE_CALC \
    dvmInterpFindInterfaceMethod(thisClass, methodIdx, method, methodClassDex)

    return (Method*) ATOMIC_CACHE_LOOKUP(methodClassDex->pInterfaceCache,
                DEX_INTERFACE_CACHE_SIZE, thisClass, methodIdx);

#undef ATOMIC_CACHE_CALC
}

/*
 * Check to see if "obj" is NULL.  If so, throw an exception.  Assumes the
 * pc has already been exported to the stack.
 *
 * Perform additional checks on debug builds.
 *
 * Use this to check for NULL when the instruction handler calls into
 * something that could throw an exception (so we have already called
 * EXPORT_PC at the top).
 */
static inline bool checkForNull(Object* obj)
{
    if (obj == NULL) {
        dvmThrowException("Ljava/lang/NullPointerException;", NULL);
        return false;
    }
#ifdef WITH_EXTRA_OBJECT_VALIDATION
    if (!dvmIsValidObject(obj)) {
        LOGE("Invalid object %p\n", obj);
        dvmAbort();
    }
#endif
#ifndef NDEBUG
    if (obj->clazz == NULL || ((u4) obj->clazz) <= 65536) {
        /* probable heap corruption */
        LOGE("Invalid object class %p (in %p)\n", obj->clazz, obj);
        dvmAbort();
    }
#endif
    return true;
}

/*
 * Check to see if "obj" is NULL.  If so, export the PC into the stack
 * frame and throw an exception.
 *
 * Perform additional checks on debug builds.
 *
 * Use this to check for NULL when the instruction handler doesn't do
 * anything else that can throw an exception.
 */
static inline bool checkForNullExportPC(Object* obj, u4* fp, const u2* pc)
{
    if (obj == NULL) {
        EXPORT_PC();
        dvmThrowException("Ljava/lang/NullPointerException;", NULL);
        return false;
    }
#ifdef WITH_EXTRA_OBJECT_VALIDATION
    if (!dvmIsValidObject(obj)) {
        LOGE("Invalid object %p\n", obj);
        dvmAbort();
    }
#endif
#ifndef NDEBUG
    if (obj->clazz == NULL || ((u4) obj->clazz) <= 65536) {
        /* probable heap corruption */
        LOGE("Invalid object class %p (in %p)\n", obj->clazz, obj);
        dvmAbort();
    }
#endif
    return true;
}

#endif /*_DALVIK_INTERP_DEFS*/
