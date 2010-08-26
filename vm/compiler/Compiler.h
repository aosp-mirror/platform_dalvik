/*
 * Copyright (C) 2009 The Android Open Source Project
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

#include <Thread.h>
#include <setjmp.h>

#ifndef _DALVIK_VM_COMPILER
#define _DALVIK_VM_COMPILER

/*
 * Uncomment the following to enable JIT signature breakpoint
 * #define SIGNATURE_BREAKPOINT
 */

#define MAX_JIT_RUN_LEN                 64
#define COMPILER_WORK_QUEUE_SIZE        100
#define COMPILER_IC_PATCH_QUEUE_SIZE    64

/* Architectural-independent parameters for predicted chains */
#define PREDICTED_CHAIN_CLAZZ_INIT       0
#define PREDICTED_CHAIN_METHOD_INIT      0
#define PREDICTED_CHAIN_COUNTER_INIT     0
/* A fake value which will avoid initialization and won't match any class */
#define PREDICTED_CHAIN_FAKE_CLAZZ       0xdeadc001
/* Has to be positive */
#define PREDICTED_CHAIN_COUNTER_AVOID    0x7fffffff
/* Rechain after this many misses - shared globally and has to be positive */
#define PREDICTED_CHAIN_COUNTER_RECHAIN  8192

#define COMPILER_TRACED(X)
#define COMPILER_TRACEE(X)
#define COMPILER_TRACE_CHAINING(X)

/* Macro to change the permissions applied to a chunk of the code cache */
#if !defined(WITH_JIT_TUNING)
#define PROTECT_CODE_CACHE_ATTRS       (PROT_READ | PROT_EXEC)
#define UNPROTECT_CODE_CACHE_ATTRS     (PROT_READ | PROT_EXEC | PROT_WRITE)
#else
/* When doing JIT profiling always grant the write permission */
#define PROTECT_CODE_CACHE_ATTRS       (PROT_READ | PROT_EXEC |                \
                                  (gDvmJit.profile ? PROT_WRITE : 0))
#define UNPROTECT_CODE_CACHE_ATTRS     (PROT_READ | PROT_EXEC | PROT_WRITE)
#endif

/* Acquire the lock before removing PROT_WRITE from the specified mem region */
#define UNPROTECT_CODE_CACHE(addr, size)                                       \
    {                                                                          \
        dvmLockMutex(&gDvmJit.codeCacheProtectionLock);                        \
        mprotect((void *) (((intptr_t) (addr)) & ~gDvmJit.pageSizeMask),       \
                 (size) + (((intptr_t) (addr)) & gDvmJit.pageSizeMask),        \
                 (UNPROTECT_CODE_CACHE_ATTRS));                                \
    }

/* Add the PROT_WRITE to the specified memory region then release the lock */
#define PROTECT_CODE_CACHE(addr, size)                                         \
    {                                                                          \
        mprotect((void *) (((intptr_t) (addr)) & ~gDvmJit.pageSizeMask),       \
                 (size) + (((intptr_t) (addr)) & gDvmJit.pageSizeMask),        \
                 (PROTECT_CODE_CACHE_ATTRS));                                  \
        dvmUnlockMutex(&gDvmJit.codeCacheProtectionLock);                      \
    }

#define SINGLE_STEP_OP(opcode)                                                 \
    (gDvmJit.includeSelectedOp !=                                              \
     ((gDvmJit.opList[opcode >> 3] & (1 << (opcode & 0x7))) != 0))

typedef enum JitInstructionSetType {
    DALVIK_JIT_NONE = 0,
    DALVIK_JIT_ARM,
    DALVIK_JIT_THUMB,
    DALVIK_JIT_THUMB2,
    DALVIK_JIT_THUMB2EE,
    DALVIK_JIT_X86
} JitInstructionSetType;

/* Description of a compiled trace. */
typedef struct JitTranslationInfo {
    void *codeAddress;
    JitInstructionSetType instructionSet;
    bool discardResult;         // Used for debugging divergence and IC patching
    bool methodCompilationAborted;  // Cannot compile the whole method
    Thread *requestingThread;   // For debugging purpose
} JitTranslationInfo;

typedef enum WorkOrderKind {
    kWorkOrderInvalid = 0,      // Should never see by the backend
    kWorkOrderMethod = 1,       // Work is to compile a whole method
    kWorkOrderTrace = 2,        // Work is to compile code fragment(s)
    kWorkOrderTraceDebug = 3,   // Work is to compile/debug code fragment(s)
} WorkOrderKind;

typedef struct CompilerWorkOrder {
    const u2* pc;
    WorkOrderKind kind;
    void* info;
    JitTranslationInfo result;
    jmp_buf *bailPtr;
} CompilerWorkOrder;

/* Chain cell for predicted method invocation */
typedef struct PredictedChainingCell {
    u4 branch;                  /* Branch to chained destination */
    const ClassObject *clazz;   /* key for prediction */
    const Method *method;       /* to lookup native PC from dalvik PC */
    const ClassObject *stagedClazz;   /* possible next key for prediction */
} PredictedChainingCell;

/* Work order for inline cache patching */
typedef struct ICPatchWorkOrder {
    PredictedChainingCell *cellAddr;    /* Address to be patched */
    PredictedChainingCell cellContent;  /* content of the new cell */
} ICPatchWorkOrder;

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
    unsigned isCode:1;       // If set denotes code fragments
    unsigned numInsts:8;     // Number of Byte codes in run
    unsigned runEnd:1;       // Run ends with last byte code
    jitHint  hint:6;         // Hint to apply to final code of run
    u2    startOffset;       // Starting offset for trace run
} JitCodeDesc;

/*
 * A complete list of trace runs passed to the compiler looks like the
 * following:
 *   frag1
 *   frag2
 *   frag3
 *   meta1
 *   meta2
 *   frag4
 *
 * frags 1-4 have the "isCode" field set, and metas 1-2 are plain pointers or
 * pointers to auxiliary data structures as long as the LSB is null.
 * The meaning of the meta content is loosely defined. It is usually the code
 * fragment right before the first meta field (frag3 in this case) to
 * understand and parse them. Frag4 could be a dummy one with 0 "numInsts" but
 * the "runEnd" field set.
 *
 * For example, if a trace run contains a method inlining target, the class
 * type of "this" and the currently resolved method pointer are two instances
 * of meta information stored there.
 */
typedef union {
    JitCodeDesc frag;
    void*       meta;
} JitTraceRun;

/*
 * Trace description as will appear in the translation cache.  Note
 * flexible array at end, as these will be of variable size.  To
 * conserve space in the translation cache, total length of JitTraceRun
 * array must be recomputed via seqential scan if needed.
 */
typedef struct {
    const Method* method;
    JitTraceRun trace[0];       // Variable-length trace descriptors
} JitTraceDescription;

typedef enum JitMethodAttributes {
    kIsCallee = 0,      /* Code is part of a callee (invoked by a hot trace) */
    kIsHot,             /* Code is part of a hot trace */
    kIsLeaf,            /* Method is leaf */
    kIsEmpty,           /* Method is empty */
    kIsThrowFree,       /* Method doesn't throw */
    kIsGetter,          /* Method fits the getter pattern */
    kIsSetter,          /* Method fits the setter pattern */
} JitMethodAttributes;

#define METHOD_IS_CALLEE        (1 << kIsCallee)
#define METHOD_IS_HOT           (1 << kIsHot)
#define METHOD_IS_LEAF          (1 << kIsLeaf)
#define METHOD_IS_EMPTY         (1 << kIsEmpty)
#define METHOD_IS_THROW_FREE    (1 << kIsThrowFree)
#define METHOD_IS_GETTER        (1 << kIsGetter)
#define METHOD_IS_SETTER        (1 << kIsSetter)

/* Vectors to provide optimization hints */
typedef enum JitOptimizationHints {
    kJitOptNoLoop = 0,          // Disable loop formation/optimization
} JitOptimizationHints;

#define JIT_OPT_NO_LOOP         (1 << kJitOptNoLoop)

typedef struct CompilerMethodStats {
    const Method *method;       // Used as hash entry signature
    int dalvikSize;             // # of bytes for dalvik bytecodes
    int compiledDalvikSize;     // # of compiled dalvik bytecodes
    int nativeSize;             // # of bytes for produced native code
    int attributes;             // attribute vector
} CompilerMethodStats;

struct CompilationUnit;
struct BasicBlock;
struct SSARepresentation;
struct GrowableList;
struct JitEntry;
struct MIR;

bool dvmCompilerSetupCodeCache(void);
bool dvmCompilerArchInit(void);
void dvmCompilerArchDump(void);
bool dvmCompilerStartup(void);
void dvmCompilerShutdown(void);
bool dvmCompilerWorkEnqueue(const u2* pc, WorkOrderKind kind, void* info);
void *dvmCheckCodeCache(void *method);
CompilerMethodStats *dvmCompilerAnalyzeMethodBody(const Method *method,
                                                  bool isCallee);
bool dvmCompilerCanIncludeThisInstruction(const Method *method,
                                          const DecodedInstruction *insn);
bool dvmCompileMethod(struct CompilationUnit *cUnit, const Method *method,
                      JitTranslationInfo *info);
bool dvmCompileTrace(JitTraceDescription *trace, int numMaxInsts,
                     JitTranslationInfo *info, jmp_buf *bailPtr, int optHints);
void dvmCompilerDumpStats(void);
void dvmCompilerDrainQueue(void);
void dvmJitUnchainAll(void);
void dvmCompilerSortAndPrintTraceProfiles(void);
void dvmCompilerPerformSafePointChecks(void);
void dvmCompilerInlineMIR(struct CompilationUnit *cUnit);
void dvmInitializeSSAConversion(struct CompilationUnit *cUnit);
int dvmConvertSSARegToDalvik(struct CompilationUnit *cUnit, int ssaReg);
bool dvmCompilerLoopOpt(struct CompilationUnit *cUnit);
void dvmCompilerNonLoopAnalysis(struct CompilationUnit *cUnit);
void dvmCompilerFindLiveIn(struct CompilationUnit *cUnit,
                           struct BasicBlock *bb);
void dvmCompilerDoSSAConversion(struct CompilationUnit *cUnit,
                                struct BasicBlock *bb);
void dvmCompilerDoConstantPropagation(struct CompilationUnit *cUnit,
                                      struct BasicBlock *bb);
void dvmCompilerFindInductionVariables(struct CompilationUnit *cUnit,
                                       struct BasicBlock *bb);
char *dvmCompilerGetDalvikDisassembly(DecodedInstruction *insn, char *note);
char *dvmCompilerGetSSAString(struct CompilationUnit *cUnit,
                              struct SSARepresentation *ssaRep);
void dvmCompilerDataFlowAnalysisDispatcher(struct CompilationUnit *cUnit,
                void (*func)(struct CompilationUnit *, struct BasicBlock *));
void dvmCompilerStateRefresh(void);
JitTraceDescription *dvmCopyTraceDescriptor(const u2 *pc,
                                            const struct JitEntry *desc);
void *dvmCompilerGetInterpretTemplate();
#endif /* _DALVIK_VM_COMPILER */
