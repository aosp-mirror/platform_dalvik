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

#ifndef _DALVIK_VM_COMPILER_OPTIMIZATION_H
#define _DALVIK_VM_COMPILER_OPTIMIZATION_H

#include "Dalvik.h"

/*
 * If the corresponding bit is set in gDvmJit.disableOpt, the selected
 * optimization will be suppressed.
 */
typedef enum optControlVector {
    kLoadStoreElimination = 0,
    kLoadHoisting,
} optControlVector;

/* Forward declarations */
struct CompilationUnit;
struct LIR;

/*
 * Data structure tracking the mapping between a Dalvik register (pair) and a
 * native register (pair). The idea is to reuse the previously loaded value
 * if possible, otherwise to keep the value in a native register as long as
 * possible.
 */
typedef struct RegisterScoreboard {
    BitVector *nullCheckedRegs; // Track which registers have been null-checked
    int liveDalvikReg;          // Track which Dalvik register is live
    int nativeReg;              // And the mapped native register
    int nativeRegHi;            // And the mapped native register
    bool isWide;                // Whether a pair of registers are alive
} RegisterScoreboard;

void dvmCompilerApplyLocalOptimizations(struct CompilationUnit *cUnit,
                                        struct LIR *head,
                                        struct LIR *tail);

void dvmCompilerApplyGlobalOptimizations(struct CompilationUnit *cUnit);

#endif /* _DALVIK_VM_COMPILER_OPTIMIZATION_H */
