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

#ifndef _DALVIK_VM_COMPILER_IR
#define _DALVIK_VM_COMPILER_IR

#include "codegen/Optimizer.h"

typedef enum BBType {
    /* For coding convenience reasons chaining cell types should appear first */
    CHAINING_CELL_NORMAL = 0,
    CHAINING_CELL_HOT,
    CHAINING_CELL_INVOKE_SINGLETON,
    CHAINING_CELL_INVOKE_PREDICTED,
    CHAINING_CELL_BACKWARD_BRANCH,
    CHAINING_CELL_LAST,
    ENTRY_BLOCK,
    DALVIK_BYTECODE,
    EXIT_BLOCK,
    PC_RECONSTRUCTION,
    EXCEPTION_HANDLING,
} BBType;

typedef struct ChainCellCounts {
    union {
        u1 count[CHAINING_CELL_LAST];
        u4 dummyForAlignment;
    } u;
} ChainCellCounts;

typedef struct LIR {
    int offset;
    struct LIR *next;
    struct LIR *prev;
    struct LIR *target;
} LIR;

enum ExtendedMIROpcode {
    MIR_OP_FIRST = 256,
    MIR_OP_PHI = MIR_OP_FIRST,
    MIR_OP_NULL_N_RANGE_UP_CHECK,
    MIR_OP_NULL_N_RANGE_DOWN_CHECK,
    MIR_OP_LOWER_BOUND_CHECK,
    MIR_OP_PUNT,
    MIR_OP_LAST,
};

struct SSARepresentation;

typedef enum {
    kMIRIgnoreNullCheck = 0,
    kMIRNullCheckOnly,
    kMIRIgnoreRangeCheck,
    kMIRRangeCheckOnly,
} MIROptimizationFlagPositons;

#define MIR_IGNORE_NULL_CHECK           (1 << kMIRIgnoreNullCheck)
#define MIR_NULL_CHECK_ONLY             (1 << kMIRNullCheckOnly)
#define MIR_IGNORE_RANGE_CHECK          (1 << kMIRIgnoreRangeCheck)
#define MIR_RANGE_CHECK_ONLY            (1 << kMIRRangeCheckOnly)

typedef struct MIR {
    DecodedInstruction dalvikInsn;
    unsigned int width;
    unsigned int offset;
    struct MIR *prev;
    struct MIR *next;
    struct SSARepresentation *ssaRep;
    int OptimizationFlags;
} MIR;

struct BasicBlockDataFlow;

typedef struct BasicBlock {
    int id;
    int visited;
    unsigned int startOffset;
    const Method *containingMethod;     // For blocks from the callee
    BBType blockType;
    bool needFallThroughBranch;         // For blocks ended due to length limit
    MIR *firstMIRInsn;
    MIR *lastMIRInsn;
    struct BasicBlock *fallThrough;
    struct BasicBlock *taken;
    struct BasicBlock *next;            // Serial link for book keeping purposes
    struct BasicBlockDataFlow *dataFlowInfo;
} BasicBlock;

struct LoopAnalysis;

typedef struct CompilationUnit {
    int numInsts;
    int numBlocks;
    BasicBlock **blockList;
    const Method *method;
    const JitTraceDescription *traceDesc;
    LIR *firstLIRInsn;
    LIR *lastLIRInsn;
    LIR *wordList;
    LIR *chainCellOffsetLIR;
    GrowableList pcReconstructionList;
    int headerSize;                     // bytes before the first code ptr
    int dataOffset;                     // starting offset of literal pool
    int totalSize;                      // header + code size
    unsigned char *codeBuffer;
    void *baseAddr;
    bool printMe;
    bool allSingleStep;
    bool halveInstCount;
    bool executionCount;                // Add code to count trace executions
    bool hasLoop;
    int numChainingCells[CHAINING_CELL_LAST];
    LIR *firstChainingLIR[CHAINING_CELL_LAST];
    RegisterScoreboard registerScoreboard;      // Track register dependency
    int optRound;                       // round number to tell an LIR's age
    JitInstructionSetType instructionSet;
    /* Number of total regs used in the whole cUnit after SSA transformation */
    int numSSARegs;
    /* Map SSA reg i to the Dalvik[15..0]/Sub[31..16] pair. */
    GrowableList *ssaToDalvikMap;

    /* The following are new data structures to support SSA representations */
    /* Map original Dalvik reg i to the SSA[15..0]/Sub[31..16] pair */
    int *dalvikToSSAMap;                // length == method->registersSize
    BitVector *isConstantV;             // length == numSSAReg
    int *constantValues;                // length == numSSAReg

    /* Data structure for loop analysis and optimizations */
    struct LoopAnalysis *loopAnalysis;
} CompilationUnit;

BasicBlock *dvmCompilerNewBB(BBType blockType);

void dvmCompilerAppendMIR(BasicBlock *bb, MIR *mir);

void dvmCompilerPrependMIR(BasicBlock *bb, MIR *mir);

void dvmCompilerAppendLIR(CompilationUnit *cUnit, LIR *lir);

void dvmCompilerInsertLIRBefore(LIR *currentLIR, LIR *newLIR);

/* Debug Utilities */
void dvmCompilerDumpCompilationUnit(CompilationUnit *cUnit);

#endif /* _DALVIK_VM_COMPILER_IR */
