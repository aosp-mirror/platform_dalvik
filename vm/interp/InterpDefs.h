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
     * (self/method).  They're copied in here for speed.
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

#endif /*_DALVIK_INTERP_DEFS*/
