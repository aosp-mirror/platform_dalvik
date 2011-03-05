/*
 * Copyright (C) 2011 The Android Open Source Project
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
#ifndef _DALVIK_INTERP_STATE
#define _DALVIK_INTERP_STATE

/*
 * Specify the starting point when switching between interpreters.
 */
typedef enum InterpEntry {
    kInterpEntryInstr = 0,      // continue to next instruction
    kInterpEntryReturn = 1,     // jump to method return
    kInterpEntryThrow = 2,      // jump to exception throw
#if defined(WITH_JIT)
    kInterpEntryResume = 3,     // Resume after single-step
#endif
} InterpEntry;

typedef struct InterpSaveState {
    const u2*       pc;         // Dalvik PC
    u4*             fp;         // Dalvik frame pointer
    const Method    *method;    // Method being executed
    DvmDex*         methodClassDex;
    void*           bailPtr;
    volatile int*   pInterpBreak;  // FIXME - use directly
#if defined(WITH_TRACKREF_CHECKS)
    int             debugTrackedRefStart;
#else
    int             unused;        // Keep struct size constant
#endif
    struct InterpSaveState* prev;  // To follow nested activations
} InterpSaveState;

#ifdef WITH_JIT
/*
 * NOTE: Only entry points dispatched via [self + #offset] are put
 * in this struct, and there are six of them:
 * 1) dvmJitToInterpNormal: find if there is a corresponding compilation for
 *    the new dalvik PC. If so, chain the originating compilation with the
 *    target then jump to it. If the destination trace doesn't exist, update
 *    the profile count for that Dalvik PC.
 * 2) dvmJitToInterpNoChain: similar to dvmJitToInterpNormal but chaining is
 *    not performed.
 * 3) dvmJitToInterpPunt: use the fast interpreter to execute the next
 *    instruction(s) and stay there as long as it is appropriate to return
 *    to the compiled land. This is used when the jit'ed code is about to
 *    throw an exception.
 * 4) dvmJitToInterpSingleStep: use the portable interpreter to execute the
 *    next instruction only and return to pre-specified location in the
 *    compiled code to resume execution. This is mainly used as debugging
 *    feature to bypass problematic opcode implementations without
 *    disturbing the trace formation.
 * 5) dvmJitToTraceSelect: Similar to dvmJitToInterpNormal except for the
 *    profiling operation. If the new Dalvik PC is dominated by an already
 *    translated trace, directly request a new translation if the destinaion
 *    trace doesn't exist.
 * 6) dvmJitToBackwardBranch: special case for SELF_VERIFICATION when the
 *    destination Dalvik PC is included by the trace itself.
 */
struct JitToInterpEntries {
    void *dvmJitToInterpNormal;
    void *dvmJitToInterpNoChain;
    void *dvmJitToInterpPunt;
    void *dvmJitToInterpSingleStep;
    void *dvmJitToInterpTraceSelect;
#if defined(WITH_SELF_VERIFICATION)
    void *dvmJitToInterpBackwardBranch;
#else
    void *unused;  // Keep structure size constant
#endif
};

/* States of the dbg interpreter when serving a JIT-related request */
typedef enum JitState {
    /* Entering states in the debug interpreter */
    kJitNot = 0,               // Non-JIT related reasons */
    kJitTSelectRequest = 1,    // Request a trace (subject to filtering)
    kJitTSelectRequestHot = 2, // Request a hot trace (bypass the filter)
    kJitSelfVerification = 3,  // Self Verification Mode

    /* Operational states in the debug interpreter */
    kJitTSelect = 4,           // Actively selecting a trace
    kJitTSelectEnd = 5,        // Done with the trace - wrap it up
    kJitSingleStep = 6,        // Single step interpretation
    kJitSingleStepEnd = 7,     // Done with single step, ready return to mterp
    kJitDone = 8,              // Ready to leave the debug interpreter
} JitState;

#if defined(WITH_SELF_VERIFICATION)
typedef enum SelfVerificationState {
    kSVSIdle = 0,           // Idle
    kSVSStart = 1,          // Shadow space set up, running compiled code
    kSVSPunt = 2,           // Exiting compiled code by punting
    kSVSSingleStep = 3,     // Exiting compiled code by single stepping
    kSVSNoProfile = 4,      // Exiting compiled code and don't collect profiles
    kSVSTraceSelect = 5,    // Exiting compiled code and compile the next pc
    kSVSNormal = 6,         // Exiting compiled code normally
    kSVSNoChain = 7,        // Exiting compiled code by no chain
    kSVSBackwardBranch = 8, // Exiting compiled code with backward branch trace
    kSVSDebugInterp = 9,    // Normal state restored, running debug interpreter
} SelfVerificationState;
#endif

/* Number of entries in the 2nd level JIT profiler filter cache */
#define JIT_TRACE_THRESH_FILTER_SIZE 32
/* Number of low dalvik pc address bits to include in 2nd level filter key */
#define JIT_TRACE_THRESH_FILTER_PC_BITS 4
#define MAX_JIT_RUN_LEN 64

typedef enum JitHint {
   kJitHintNone = 0,
   kJitHintTaken = 1,         // Last inst in run was taken branch
   kJitHintNotTaken = 2,      // Last inst in run was not taken branch
   kJitHintNoBias = 3,        // Last inst in run was unbiased branch
} jitHint;

/*
 * Element of a Jit trace description. If the isCode bit is set, it describes
 * a contiguous sequence of Dalvik byte codes.
 */
typedef struct {
    unsigned numInsts:8;     // Number of Byte codes in run
    unsigned runEnd:1;       // Run ends with last byte code
    jitHint  hint:7;         // Hint to apply to final code of run
    u2    startOffset;       // Starting offset for trace run
} JitCodeDesc;

/*
 * A complete list of trace runs passed to the compiler looks like the
 * following:
 *   frag1
 *   frag2
 *   frag3
 *   meta1
 *     :
 *   metan
 *   frag4
 *
 * frags 1-4 have the "isCode" field set and describe the location/length of
 * real code traces, while metas 1-n are misc information.
 * The meaning of the meta content is loosely defined. It is usually the code
 * fragment right before the first meta field (frag3 in this case) to
 * understand and parse them. Frag4 could be a dummy one with 0 "numInsts" but
 * the "runEnd" field set.
 *
 * For example, if a trace run contains a method inlining target, the class
 * descriptor/loader of "this" and the currently resolved method pointer are
 * three instances of meta information stored there.
 */
typedef struct {
    union {
        JitCodeDesc frag;
        void*       meta;
    } info;
    u4 isCode:1;
    u4 unused:31;
} JitTraceRun;

#endif

#endif /*_DALVIK_INTERP_STATE*/
