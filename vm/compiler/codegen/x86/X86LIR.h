/*
 * Copyright (C) 2010 The Android Open Source Project
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

#ifndef DALVIK_VM_COMPILER_CODEGEN_X86_X86LIR_H_
#define DALVIK_VM_COMPILER_CODEGEN_X86_X86LIR_H_

#include "Dalvik.h"
#include "compiler/CompilerInternals.h"

/*
 * For both JIT & interpreter:
 *     esi is Dalvik FP
 *     ebp is native FP
 *     esp is native SP
 *
 * For interpreter:
 *     edi is Dalvik PC (rPC)
 *     ebx is rINST
 *
 * For JIT:
 *     eax, edx, ecx are scratch & caller-save
 *     ebx, edi are scratch & callee-save
 *
 * Calling conventions:
 *     32-bit return in eax
 *     64-bit return in edx:eax
 *     fp on top of fp stack st(0)
 *     Parameters passed on stack, pushed left to right
 *     On entry to target, first parm is at 4(%esp).
 *     For performance, we'll maintain 16-byte stack alignment
 *
 * When transitioning from code cache to interp:
 *       materialize Dalvik PC of target in rPC/%edx
 *       Preload rINST/%ebx such that high 24 bits are zero and
 *           bl contains the non-opcode 8-bits of the 16-bit Dalvik
 *           instruction at (rPC)
 */

/* Keys for target-specific scheduling and other optimizations here */
typedef enum X86TargetOptHints {
   kMaxHoistDistance,
} X86TargetOptHints;

 /*
 * Data structure tracking the mapping between a Dalvik register (pair) and a
 * native register (pair). The idea is to reuse the previously loaded value
 * if possible, otherwise to keep the value in a native register as long as
 * possible.
 */
typedef struct RegisterInfo {
    int reg;                    // Reg number
    bool inUse;                 // Has it been allocated?
    bool pair;                  // Part of a register pair?
    int partner;                // If pair, other reg of pair
    bool live;                  // Is there an associated SSA name?
    bool dirty;                 // If live, is it dirty?
    int sReg;                   // Name of live value
    struct LIR *defStart;       // Starting inst in last def sequence
    struct LIR *defEnd;         // Ending inst in last def sequence
} RegisterInfo;

typedef struct RegisterPool {
    BitVector *nullCheckedRegs; // Track which registers have been null-checked
    int numCoreTemps;
    RegisterInfo *coreTemps;
    int nextCoreTemp;
    int numFPTemps;
    RegisterInfo *FPTemps;
    int nextFPTemp;
} RegisterPool;

typedef enum OpSize {
    kWord,
    kLong,
    kSingle,
    kDouble,
    kUnsignedHalf,
    kSignedHalf,
    kUnsignedByte,
    kSignedByte,
} OpSize;

typedef enum OpKind {
    kOpMov,
    kOpCmp,
    kOpLsl,
    kOpLsr,
    kOpAsr,
    kOpRor,
    kOpNot,
    kOpAnd,
    kOpOr,
    kOpXor,
    kOpNeg,
    kOpAdd,
    kOpAdc,
    kOpSub,
    kOpSbc,
    kOpMul,
    kOpDiv,
    kOpRem,
    kOpTst,
    kOpCall,
    kOpPush,
    kOpPop,
    kOp2Char,
    kOp2Short,
    kOp2Byte,
    kOpCondBr,
    kOpUncondBr,
} OpKind;

#define FP_REG_OFFSET 8

typedef enum NativeRegisterPool {
    rEAX = 0,
    rECX = 1,
    rEDX = 2,
    rEBX = 3,
    rESP = 4,
    rEBP = 5,
    rESI = 6,
    rEDI = 7,
    rXMM0 = 0 + FP_REG_OFFSET,
    rXMM1 = 1 + FP_REG_OFFSET,
    rXMM2 = 2 + FP_REG_OFFSET,
    rXMM3 = 3 + FP_REG_OFFSET,
    rXMM4 = 4 + FP_REG_OFFSET,
    rXMM5 = 5 + FP_REG_OFFSET,
    rXMM6 = 6 + FP_REG_OFFSET,
    rXMM7 = 7 + FP_REG_OFFSET,
} NativeRegisterPool;

#define rPC rEDI
#define rFP rESI
#define rINST rEBX

#define OUT_ARG0 0
#define OUT_ARG1 4
#define OUT_ARG2 8
#define OUT_ARG3 12
#define OUT_ARG4 16

typedef struct X86LIR {
    LIR generic;
    //X86Opcode opcode;
    int operands[4];    // [0..3] = [dest, src1, src2, extra]
    bool isNop;         // LIR is optimized away
    bool branchInsertSV;// mark for insertion of branch before this instruction,
                        // used to identify mem ops for self verification mode
    int age;            // default is 0, set lazily by the optimizer
    int aliasInfo;      // For Dalvik register access & litpool disambiguation
    u8 useMask;         // Resource mask for use
    u8 defMask;         // Resource mask for def
} X86LIR;

/* Utility macros to traverse the LIR/X86LIR list */
#define NEXT_LIR(lir) ((X86LIR *) lir->generic.next)
#define PREV_LIR(lir) ((X86LIR *) lir->generic.prev)

#define NEXT_LIR_LVALUE(lir) (lir)->generic.next
#define PREV_LIR_LVALUE(lir) (lir)->generic.prev

#define CHAIN_CELL_OFFSET_TAG   0xcdab

#define CHAIN_CELL_NORMAL_SIZE 12
#define CHAIN_CELL_PREDICTED_SIZE 16

#endif  // DALVIK_VM_COMPILER_CODEGEN_X86_X86LIR_H_
