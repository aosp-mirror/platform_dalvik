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

#ifndef _DALVIK_VM_COMPILER
#define _DALVIK_VM_COMPILER

#define CODE_CACHE_SIZE                 1024*1024
#define MAX_JIT_RUN_LEN                 64
#define COMPILER_WORK_QUEUE_SIZE        100

#define COMPILER_TRACED(X)
#define COMPILER_TRACEE(X)
#define COMPILER_TRACE_CHAINING(X)

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
    void     *codeAddress;
    JitInstructionSetType instructionSet;
} JitTranslationInfo;

typedef enum WorkOrderKind {
    kWorkOrderInvalid = 0,      // Should never see by the backend
    kWorkOrderMethod = 1,       // Work is to compile a whole method
    kWorkOrderTrace = 2,        // Work is to compile code fragment(s)
} WorkOrderKind;

typedef struct CompilerWorkOrder {
    const u2* pc;
    WorkOrderKind kind;
    void* info;
    JitTranslationInfo result;
} CompilerWorkOrder;

typedef enum JitState {
    kJitOff = 0,
    kJitNormal = 1,            // Profiling in mterp or running native
    kJitTSelectRequest = 2,    // Transition state - start trace selection
    kJitTSelect = 3,           // Actively selecting trace in dbg interp
    kJitTSelectAbort = 4,      // Something threw during selection - abort
    kJitTSelectEnd = 5,        // Done with the trace - wrap it up
    kJitSingleStep = 6,        // Single step interpretation
    kJitSingleStepEnd = 7,     // Done with single step, return to mterp
} JitState;

typedef enum JitHint {
   kJitHintNone = 0,
   kJitHintTaken = 1,         // Last inst in run was taken branch
   kJitHintNotTaken = 2,      // Last inst in run was not taken branch
   kJitHintNoBias = 3,        // Last inst in run was unbiased branch
} jitHint;

/*
 * Element of a Jit trace description.  Describes a contiguous
 * sequence of Dalvik byte codes, the last of which can be
 * associated with a hint.
 * Dalvik byte code
 */
typedef struct {
    u2    startOffset;       // Starting offset for trace run
    unsigned numInsts:8;     // Number of Byte codes in run
    unsigned runEnd:1;       // Run ends with last byte code
    jitHint  hint:7;         // Hint to apply to final code of run
} JitCodeDesc;

typedef union {
    JitCodeDesc frag;
    void*       hint;
} JitTraceRun;

/*
 * Trace description as will appear in the translation cache.  Note
 * flexible array at end, as these will be of variable size.  To
 * conserve space in the translation cache, total length of JitTraceRun
 * array must be recomputed via seqential scan if needed.
 */
typedef struct {
    const Method* method;
    JitTraceRun trace[];
} JitTraceDescription;

typedef struct CompilerMethodStats {
    const Method *method;       // Used as hash entry signature
    int dalvikSize;             // # of bytes for dalvik bytecodes
    int compiledDalvikSize;     // # of compiled dalvik bytecodes
    int nativeSize;             // # of bytes for produced native code
} CompilerMethodStats;

bool dvmCompilerSetupCodeCache(void);
bool dvmCompilerArchInit(void);
void dvmCompilerArchDump(void);
bool dvmCompilerStartup(void);
void dvmCompilerShutdown(void);
bool dvmCompilerWorkEnqueue(const u2* pc, WorkOrderKind kind, void* info);
void *dvmCheckCodeCache(void *method);
bool dvmCompileMethod(const Method *method, JitTranslationInfo *info);
bool dvmCompileTrace(JitTraceDescription *trace, int numMaxInsts,
                     JitTranslationInfo *info);
void dvmCompilerDumpStats(void);
void dvmCompilerDrainQueue(void);
void dvmJitUnchainAll(void);
void dvmCompilerSortAndPrintTraceProfiles(void);

#endif /* _DALVIK_VM_COMPILER */
