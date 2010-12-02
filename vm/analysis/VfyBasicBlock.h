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

/*
 * Basic block functions, as used by the verifier.  (The names were chosen
 * to avoid conflicts with similar structures used by the compiler.)
 */
#ifndef _DALVIK_VFYBASICBLOCK
#define _DALVIK_VFYBASICBLOCK

#include "libdex/InstrUtils.h"
#include "PointerSet.h"

struct VerifierData;


/*
 * Structure representing a basic block.
 *
 * This is used for liveness analysis, which is a reverse-flow algorithm,
 * so we need to mantain a list of predecessors for each block.
 */
typedef struct {
    u4              firstAddr;      /* address of first instruction */
    u4              lastAddr;       /* address of last instruction */
    PointerSet*     predecessors;   /* set of basic blocks that can flow here */
} VfyBasicBlock;

/*
 * Generate a list of basic blocks.
 */
bool dvmComputeVfyBasicBlocks(struct VerifierData* vdata);

/*
 * Free storage allocated by dvmComputeVfyBasicBlocks.
 */
void dvmFreeVfyBasicBlocks(struct VerifierData* vdata);

#endif /*_DALVIK_VFYBASICBLOCK*/
