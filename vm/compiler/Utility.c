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

#include "Dalvik.h"
#include "CompilerInternals.h"

static ArenaMemBlock *arenaHead, *currentArena;
static int numArenaBlocks;

/* Allocate the initial memory block for arena-based allocation */
bool dvmCompilerHeapInit(void)
{
    assert(arenaHead == NULL);
    arenaHead =
        (ArenaMemBlock *) malloc(sizeof(ArenaMemBlock) + ARENA_DEFAULT_SIZE);
    if (arenaHead == NULL) {
        LOGE("No memory left to create compiler heap memory\n");
        return false;
    }
    currentArena = arenaHead;
    currentArena->bytesAllocated = 0;
    currentArena->next = NULL;
    numArenaBlocks = 1;

    return true;
}

/* Arena-based malloc for compilation tasks */
void * dvmCompilerNew(size_t size, bool zero)
{
    size = (size + 3) & ~3;
retry:
    /* Normal case - space is available in the current page */
    if (size + currentArena->bytesAllocated <= ARENA_DEFAULT_SIZE) {
        void *ptr;
        ptr = &currentArena->ptr[currentArena->bytesAllocated];
        currentArena->bytesAllocated += size;
        if (zero) {
            memset(ptr, 0, size);
        }
        return ptr;
    } else {
        /*
         * See if there are previously allocated arena blocks before the last
         * reset
         */
        if (currentArena->next) {
            currentArena = currentArena->next;
            goto retry;
        }
        /*
         * If we allocate really large variable-sized data structures that
         * could go above the limit we need to enhance the allocation
         * mechanism.
         */
        if (size > ARENA_DEFAULT_SIZE) {
            LOGE("Requesting %d bytes which exceed the maximal size allowed\n",
                 size);
            return NULL;
        }
        /* Time to allocate a new arena */
        ArenaMemBlock *newArena = (ArenaMemBlock *)
            malloc(sizeof(ArenaMemBlock) + ARENA_DEFAULT_SIZE);
        newArena->bytesAllocated = 0;
        newArena->next = NULL;
        currentArena->next = newArena;
        currentArena = newArena;
        numArenaBlocks++;
        LOGD("Total arena pages for JIT: %d", numArenaBlocks);
        goto retry;
    }
    return NULL;
}

/* Reclaim all the arena blocks allocated so far */
void dvmCompilerArenaReset(void)
{
    ArenaMemBlock *block;

    for (block = arenaHead; block; block = block->next) {
        block->bytesAllocated = 0;
    }
    currentArena = arenaHead;
}

/* Growable List initialization */
void dvmInitGrowableList(GrowableList *gList, size_t initLength)
{
    gList->numAllocated = initLength;
    gList->numUsed = 0;
    gList->elemList = (void **) dvmCompilerNew(sizeof(void *) * initLength,
                                               true);
}

/* Expand the capacity of a growable list */
static void expandGrowableList(GrowableList *gList)
{
    int newLength = gList->numAllocated;
    if (newLength < 128) {
        newLength <<= 1;
    } else {
        newLength += 128;
    }
    void *newArray = dvmCompilerNew(sizeof(void *) * newLength, true);
    memcpy(newArray, gList->elemList, sizeof(void *) * gList->numAllocated);
    gList->numAllocated = newLength;
    gList->elemList = newArray;
}

/* Insert a new element into the growable list */
void dvmInsertGrowableList(GrowableList *gList, void *elem)
{
    if (gList->numUsed == gList->numAllocated) {
        expandGrowableList(gList);
    }
    gList->elemList[gList->numUsed++] = elem;
}

/* Debug Utility - dump a compilation unit */
void dvmCompilerDumpCompilationUnit(CompilationUnit *cUnit)
{
    int i;
    BasicBlock *bb;
    LOGD("%d blocks in total\n", cUnit->numBlocks);

    for (i = 0; i < cUnit->numBlocks; i++) {
        bb = cUnit->blockList[i];
        LOGD("Block %d (insn %04x - %04x%s)\n",
             bb->id, bb->startOffset,
             bb->lastMIRInsn ? bb->lastMIRInsn->offset : bb->startOffset,
             bb->lastMIRInsn ? "" : " empty");
        if (bb->taken) {
            LOGD("  Taken branch: block %d (%04x)\n",
                 bb->taken->id, bb->taken->startOffset);
        }
        if (bb->fallThrough) {
            LOGD("  Fallthrough : block %d (%04x)\n",
                 bb->fallThrough->id, bb->fallThrough->startOffset);
        }
    }
}

/*
 * dvmHashForeach callback.
 */
static int dumpMethodStats(void *compilerMethodStats, void *totalMethodStats)
{
    CompilerMethodStats *methodStats =
        (CompilerMethodStats *) compilerMethodStats;
    CompilerMethodStats *totalStats =
        (CompilerMethodStats *) totalMethodStats;
    const Method *method = methodStats->method;

    totalStats->dalvikSize += methodStats->dalvikSize;
    totalStats->compiledDalvikSize += methodStats->compiledDalvikSize;
    totalStats->nativeSize += methodStats->nativeSize;

    /* Enable the following when fine-tuning the JIT performance */
#if 0
    int limit = (methodStats->dalvikSize >> 2) * 3;

    /* If over 3/4 of the Dalvik code is compiled, print something */
    if (methodStats->compiledDalvikSize >= limit) {
        LOGD("Method stats: %s%s, %d/%d (compiled/total Dalvik), %d (native)",
             method->clazz->descriptor, method->name,
             methodStats->compiledDalvikSize,
             methodStats->dalvikSize,
             methodStats->nativeSize);
    }
#endif
    return 0;
}

/*
 * Dump the current stats of the compiler, including number of bytes used in
 * the code cache, arena size, and work queue length, and various JIT stats.
 */
void dvmCompilerDumpStats(void)
{
    CompilerMethodStats totalMethodStats;

    memset(&totalMethodStats, 0, sizeof(CompilerMethodStats));
    LOGD("%d compilations using %d + %d bytes",
         gDvmJit.numCompilations,
         gDvmJit.templateSize,
         gDvmJit.codeCacheByteUsed - gDvmJit.templateSize);
    LOGD("Compiler arena uses %d blocks (%d bytes each)",
         numArenaBlocks, ARENA_DEFAULT_SIZE);
    LOGD("Compiler work queue length is %d/%d", gDvmJit.compilerQueueLength,
         gDvmJit.compilerMaxQueued);
    dvmJitStats();
    dvmCompilerArchDump();
    dvmHashForeach(gDvmJit.methodStatsTable, dumpMethodStats,
                   &totalMethodStats);
    LOGD("Code size stats: %d/%d (compiled/total Dalvik), %d (native)",
         totalMethodStats.compiledDalvikSize,
         totalMethodStats.dalvikSize,
         totalMethodStats.nativeSize);
}

/*
 * Allocate a bit vector with enough space to hold at least the specified
 * number of bits.
 *
 * NOTE: this is the sister implementation of dvmAllocBitVector. In this version
 * memory is allocated from the compiler arena.
 */
BitVector* dvmCompilerAllocBitVector(int startBits, bool expandable)
{
    BitVector* bv;
    int count;

    assert(sizeof(bv->storage[0]) == 4);        /* assuming 32-bit units */
    assert(startBits >= 0);

    bv = (BitVector*) dvmCompilerNew(sizeof(BitVector), false);

    count = (startBits + 31) >> 5;

    bv->storageSize = count;
    bv->expandable = expandable;
    bv->storage = (u4*) dvmCompilerNew(count * sizeof(u4), true);
    return bv;
}

/*
 * Mark the specified bit as "set".
 *
 * Returns "false" if the bit is outside the range of the vector and we're
 * not allowed to expand.
 *
 * NOTE: this is the sister implementation of dvmSetBit. In this version
 * memory is allocated from the compiler arena.
 */
bool dvmCompilerSetBit(BitVector *pBits, int num)
{
    assert(num >= 0);
    if (num >= pBits->storageSize * (int)sizeof(u4) * 8) {
        if (!pBits->expandable)
            return false;

        int newSize = (num + 31) >> 5;
        assert(newSize > pBits->storageSize);
        u4 *newStorage = dvmCompilerNew(newSize * sizeof(u4), false);
        memcpy(newStorage, pBits->storage, pBits->storageSize * sizeof(u4));
        memset(&newStorage[pBits->storageSize], 0,
               (newSize - pBits->storageSize) * sizeof(u4));
        pBits->storage = newStorage;
        pBits->storageSize = newSize;
    }

    pBits->storage[num >> 5] |= 1 << (num & 0x1f);
    return true;
}

/* FIXME */
void dvmDebugBitVector(char *msg, const BitVector *bv, int length)
{
    int i;

    LOGE("%s", msg);
    for (i = 0; i < length; i++) {
        if (dvmIsBitSet(bv, i)) {
            LOGE("Bit %d is set", i);
        }
    }
}
