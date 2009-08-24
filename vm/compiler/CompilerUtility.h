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

#ifndef _DALVIK_VM_COMPILER_UTILITY
#define _DALVIK_VM_COMPILER_UTILITY

#define ARENA_DEFAULT_SIZE 4096

/* Allocate the initial memory block for arena-based allocation */
bool dvmCompilerHeapInit(void);

typedef struct ArenaMemBlock {
    size_t bytesAllocated;
    struct ArenaMemBlock *next;
    char ptr[0];
} ArenaMemBlock;

void *dvmCompilerNew(size_t size, bool zero);

void dvmCompilerArenaReset(void);

typedef struct GrowableList {
    size_t numAllocated;
    size_t numUsed;
    void **elemList;
} GrowableList;

#define GET_ELEM_N(LIST, TYPE, N) (((TYPE*) LIST->elemList)[N])

void dvmInitGrowableList(GrowableList *gList, size_t initLength);
void dvmInsertGrowableList(GrowableList *gList, void *elem);

BitVector* dvmCompilerAllocBitVector(int startBits, bool expandable);
bool dvmCompilerSetBit(BitVector* pBits, int num);
void dvmDebugBitVector(char *msg, const BitVector *bv, int length);

#endif /* _DALVIK_COMPILER_UTILITY */
