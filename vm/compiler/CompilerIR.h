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

typedef enum BBType {
    /* For coding convenience reasons chaining cell types should appear first */
    CHAINING_CELL_NORMAL = 0,
    CHAINING_CELL_HOT,
    CHAINING_CELL_INVOKE,
    CHAINING_CELL_LAST,
    DALVIK_BYTECODE,
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

typedef struct MIR {
    DecodedInstruction dalvikInsn;
    unsigned int width;
    unsigned int offset;
    struct MIR *prev;
    struct MIR *next;
} MIR;

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
} BasicBlock;

typedef struct CompilationUnit {
    int numInsts;
    int numBlocks;
    BasicBlock **blockList;
    const Method *method;
    const JitTraceDescription *traceDesc;
    LIR *firstLIRInsn;
    LIR *lastLIRInsn;
    LIR *wordList;
    GrowableList pcReconstructionList;
    int headerSize;                     // bytes before the first code ptr
    int dataOffset;                     // starting offset of literal pool
    int totalSize;                      // header + code size
    unsigned char *codeBuffer;
    void *baseAddr;
    bool printMe;
    bool allSingleStep;
    bool halveInstCount;
    int numChainingCells[CHAINING_CELL_LAST];
    LIR *firstChainingLIR[CHAINING_CELL_LAST];
} CompilationUnit;

BasicBlock *dvmCompilerNewBB(BBType blockType);

void dvmCompilerAppendMIR(BasicBlock *bb, MIR *mir);

void dvmCompilerAppendLIR(CompilationUnit *cUnit, LIR *lir);

/* Debug Utilities */
void dvmCompilerDumpCompilationUnit(CompilationUnit *cUnit);

#endif /* _DALVIK_VM_COMPILER_IR */
