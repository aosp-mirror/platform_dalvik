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

#include <errno.h>
#include <limits.h>
#include <sys/mman.h>

#include "Dalvik.h"
#include "alloc/Heap.h"
#include "alloc/HeapBitmap.h"
#include "alloc/HeapInternal.h"
#include "alloc/HeapSource.h"
#include "alloc/Verify.h"
#include "alloc/clz.h"

/*
 * A "mostly copying", generational, garbage collector.
 *
 * TODO: we allocate our own contiguous tract of page frames to back
 * object allocations.  To cooperate with other heaps active in the
 * virtual machine we need to move the responsibility of allocating
 * pages someplace outside of this code.
 *
 * The other major data structures that maintain the state of the heap
 * are the block space table and the block queue.
 *
 * The block space table records the state of a block.  We must track
 * whether a block is:
 *
 * - Free or allocated in some space.
 *
 * - If the block holds part of a large object allocation, whether the
 *   block is the initial or a continued block of the allocation.
 *
 * - Whether the block is pinned, that is to say whether at least one
 *   object in the block must remain stationary.  Only needed during a
 *   GC.
 *
 * - Which space the object belongs to.  At present this means
 *   from-space or to-space.
 *
 * The block queue is used during garbage collection.  Unlike Cheney's
 * algorithm, from-space and to-space are not contiguous.  Therefore,
 * one cannot maintain the state of the copy with just two pointers.
 * The block queue exists to thread lists of blocks from the various
 * spaces together.
 *
 * Additionally, we record the free space frontier of the heap, as
 * well as the address of the first object within a block, which is
 * required to copy objects following a large object (not currently
 * implemented).  This is stored in the heap source structure.  This
 * should be moved elsewhere to support in-line allocations from Java
 * threads.
 *
 * Allocation requests are satisfied by reserving storage from one or
 * more contiguous blocks.  Objects that are small enough to fit
 * inside a block are packed together within a block.  Objects that
 * are larger than a block are allocated from contiguous sequences of
 * blocks.  When half the available blocks are filled, a garbage
 * collection occurs.  We "flip" spaces (exchange from- and to-space),
 * copy live objects into to space, and perform pointer adjustment.
 *
 * Copying is made more complicated by the requirement that some
 * objects must not be moved.  This property is known as "pinning".
 * These objects must be dealt with specially.  We use Bartlett's
 * scheme; blocks containing such objects are grayed (promoted) at the
 * start of a garbage collection.  By virtue of this trick, marking
 * from the roots proceeds as usual but all objects on those pages are
 * considered promoted and therefore not moved.
 *
 * TODO: there is sufficient information within the garbage collector
 * to implement Attardi's scheme for evacuating unpinned objects from
 * a page that is otherwise pinned.  This would eliminate false
 * retention caused by the large pinning granularity.
 *
 * We need a scheme for medium and large objects.  Ignore that for
 * now, we can return to this later.
 *
 * Eventually we need to worry about promoting objects out of the
 * copy-collected heap (tenuring) into a less volatile space.  Copying
 * may not always be the best policy for such spaces.  We should
 * consider a variant of mark, sweep, compact.
 *
 * The block scheme allows us to use VM page faults to maintain a
 * write barrier.  Consider having a special leaf state for a page.
 *
 * Bibliography:
 *
 * C. J. Cheney. 1970. A non-recursive list compacting
 * algorithm. CACM. 13-11 pp677--678.
 *
 * Joel F. Bartlett. 1988. Compacting Garbage Collection with
 * Ambiguous Roots. Digital Equipment Corporation.
 *
 * Joel F. Bartlett. 1989. Mostly-Copying Garbage Collection Picks Up
 * Generations and C++. Digital Equipment Corporation.
 *
 * G. May Yip. 1991. Incremental, Generational Mostly-Copying Garbage
 * Collection in Uncooperative Environments. Digital Equipment
 * Corporation.
 *
 * Giuseppe Attardi, Tito Flagella. 1994. A Customisable Memory
 * Management Framework. TR-94-010
 *
 * Giuseppe Attardi, Tito Flagella, Pietro Iglio. 1998. A customisable
 * memory management framework for C++. Software -- Practice and
 * Experience. 28(11), 1143-1183.
 *
 */

#define ARRAYSIZE(x) (sizeof(x) / sizeof(x[0]))

#if 1
#define LOG_ALLOC LOGI
#define LOG_SCAVENGE LOGI
#define LOG_TRANSPORT LOGI
#define LOG_PROMOTE LOGI
#define LOG_VERIFY LOGI
#else
#define LOG_ALLOC(...) ((void *)0)
#define LOG_SCAVENGE(...) ((void *)0)
#define LOG_TRANSPORT(...) ((void *)0)
#define LOG_PROMOTE(...) ((void *)0)
#define LOG_VERIFY(...) ((void *)0)
#endif

static void enqueueBlock(HeapSource *heapSource, size_t block);
static size_t scavengeReference(Object **obj);
static void verifyReference(const void *obj);
static void printHeapBitmap(const HeapBitmap *bitmap);
static void printHeapBitmapSxS(const HeapBitmap *b1, const HeapBitmap *b2);
static bool isToSpace(const void *addr);
static bool isFromSpace(const void *addr);
static size_t sumHeapBitmap(const HeapBitmap *bitmap);
static size_t scavengeDataObject(DataObject *obj);
static DataObject *transportDataObject(const DataObject *fromObj);

/*
 * We use 512-byte blocks.
 */
enum { BLOCK_SHIFT = 9 };
enum { BLOCK_SIZE = 1 << BLOCK_SHIFT };

/*
 * Space identifiers, stored into the blockSpace array.
 */
enum {
    BLOCK_FREE = 0,
    BLOCK_FROM_SPACE = 1,
    BLOCK_TO_SPACE = 2,
    BLOCK_CONTINUED = 7
};

/*
 * Alignment for all allocations, in bytes.
 */
enum { ALLOC_ALIGNMENT = 8 };

/*
 * Sentinel value for the queue end.
 */
#define QUEUE_TAIL (~(size_t)0)

struct HeapSource {

    /* The base address of backing store. */
    u1 *blockBase;

    /* Total number of blocks available for allocation. */
    size_t totalBlocks;
    size_t allocBlocks;

    /*
     * The scavenger work queue.  Implemented as an array of index
     * values into the queue.
     */
    size_t *blockQueue;

    /*
     * Base and limit blocks.  Basically the shifted start address of
     * the block.  We convert blocks to a relative number when
     * indexing in the block queue.  TODO: make the block queue base
     * relative rather than the index into the block queue.
     */
    size_t baseBlock, limitBlock;

    size_t queueHead;
    size_t queueTail;
    size_t queueSize;

    /* The space of the current block 0 (free), 1 or 2. */
    char *blockSpace;

    /* Start of free space in the current block. */
    u1 *allocPtr;
    /* Exclusive limit of free space in the current block. */
    u1 *allocLimit;

    HeapBitmap allocBits;

    /*
     * Singly-linked lists of live Reference objects.  Built when
     * scavenging, threaded through the Reference.vmData field.
     */
    DataObject *softReferenceList;
    DataObject *weakReferenceList;
    DataObject *phantomReferenceList;

    /*
     * The starting size of the heap.  This value is the same as the
     * value provided to the -Xms flag.
     */
    size_t minimumSize;

    /*
     * The maximum size of the heap.  This value is the same as the
     * -Xmx flag.
     */
    size_t maximumSize;

    /*
     * The current, committed size of the heap.  At present, this is
     * equivalent to the maximumSize.
     */
    size_t currentSize;

    size_t bytesAllocated;
};

static unsigned long alignDown(unsigned long x, unsigned long n)
{
    return x & -n;
}

static unsigned long alignUp(unsigned long x, unsigned long n)
{
    return alignDown(x + (n - 1), n);
}

static void describeBlocks(const HeapSource *heapSource)
{
    size_t i;

    for (i = 0; i < heapSource->totalBlocks; ++i) {
        if ((i % 32) == 0) putchar('\n');
        printf("%d ", heapSource->blockSpace[i]);
    }
    putchar('\n');
}

/*
 * Virtual memory interface.
 */

static void *virtualAlloc(size_t length)
{
    void *addr;
    int flags, prot;

    flags = MAP_PRIVATE | MAP_ANONYMOUS;
    prot = PROT_READ | PROT_WRITE;
    addr = mmap(NULL, length, prot, flags, -1, 0);
    if (addr == MAP_FAILED) {
        LOGE_HEAP("mmap: %s", strerror(errno));
        addr = NULL;
    }
    return addr;
}

static void virtualFree(void *addr, size_t length)
{
    int res;

    assert(addr != NULL);
    assert((uintptr_t)addr % SYSTEM_PAGE_SIZE == 0);
    res = munmap(addr, length);
    if (res == -1) {
        LOGE_HEAP("munmap: %s", strerror(errno));
    }
}

static int isValidAddress(const HeapSource *heapSource, const u1 *addr)
{
    size_t block;

    block = (uintptr_t)addr >> BLOCK_SHIFT;
    return heapSource->baseBlock <= block &&
           heapSource->limitBlock > block;
}

/*
 * Iterate over the block map looking for a contiguous run of free
 * blocks.
 */
static void *allocateBlocks(HeapSource *heapSource, size_t blocks)
{
    void *addr;
    size_t allocBlocks, totalBlocks;
    size_t i, j;

    allocBlocks = heapSource->allocBlocks;
    totalBlocks = heapSource->totalBlocks;
    /* Check underflow. */
    assert(blocks != 0);
    /* Check overflow. */
    if (allocBlocks + blocks > totalBlocks / 2) {
        return NULL;
    }
    /* Scan block map. */
    for (i = 0; i < totalBlocks; ++i) {
        /* Check fit. */
        for (j = 0; j < blocks; ++j) { /* runs over totalBlocks */
            if (heapSource->blockSpace[i+j] != BLOCK_FREE) {
                break;
            }
        }
        /* No fit? */
        if (j != blocks) {
            i += j;
            continue;
        }
        /* Fit, allocate. */
        heapSource->blockSpace[i] = BLOCK_TO_SPACE; /* why to-space? */
        for (j = 1; j < blocks; ++j) {
            heapSource->blockSpace[i+j] = BLOCK_CONTINUED;
        }
        heapSource->allocBlocks += blocks;
        addr = &heapSource->blockBase[i*BLOCK_SIZE];
        memset(addr, 0, blocks*BLOCK_SIZE);
        /* Collecting? */
        if (heapSource->queueHead != QUEUE_TAIL) {
            LOG_ALLOC("allocateBlocks allocBlocks=%zu,block#=%zu", heapSource->allocBlocks, i);
            /*
             * This allocated was on behalf of the transporter when it
             * shaded a white object gray.  We enqueue the block so
             * the scavenger can further shade the gray objects black.
             */
            enqueueBlock(heapSource, i);
        }

        return addr;
    }
    /* Insufficient space, fail. */
    LOGE("Insufficient space, %zu blocks, %zu blocks allocated and %zu bytes allocated",
         heapSource->totalBlocks,
         heapSource->allocBlocks,
         heapSource->bytesAllocated);
    return NULL;
}

/* Converts an absolute address to a relative block number. */
static size_t addressToBlock(const HeapSource *heapSource, const void *addr)
{
    assert(heapSource != NULL);
    assert(isValidAddress(heapSource, addr));
    return (((uintptr_t)addr) >> BLOCK_SHIFT) - heapSource->baseBlock;
}

/* Converts a relative block number to an absolute address. */
static u1 *blockToAddress(const HeapSource *heapSource, size_t block)
{
    u1 *addr;

    addr = (u1 *) (((uintptr_t) heapSource->baseBlock + block) * BLOCK_SIZE);
    assert(isValidAddress(heapSource, addr));
    return addr;
}

static void clearBlock(HeapSource *heapSource, size_t block)
{
    u1 *addr;
    size_t i;

    assert(heapSource != NULL);
    assert(block < heapSource->totalBlocks);
    addr = heapSource->blockBase + block*BLOCK_SIZE;
    memset(addr, 0xCC, BLOCK_SIZE);
    for (i = 0; i < BLOCK_SIZE; i += 8) {
        dvmHeapBitmapClearObjectBit(&heapSource->allocBits, addr + i);
    }
}

static void clearFromSpace(HeapSource *heapSource)
{
    size_t i, count;

    assert(heapSource != NULL);
    i = count = 0;
    while (i < heapSource->totalBlocks) {
        if (heapSource->blockSpace[i] != BLOCK_FROM_SPACE) {
            ++i;
            continue;
        }
        heapSource->blockSpace[i] = BLOCK_FREE;
        clearBlock(heapSource, i);
        ++i;
        ++count;
        while (i < heapSource->totalBlocks &&
               heapSource->blockSpace[i] == BLOCK_CONTINUED) {
            heapSource->blockSpace[i] = BLOCK_FREE;
            clearBlock(heapSource, i);
            ++i;
            ++count;
        }
    }
    LOGI("freed %zu blocks (%zu bytes)", count, count*BLOCK_SIZE);
}

/*
 * Appends the given block to the block queue.  The block queue is
 * processed in-order by the scavenger.
 */
static void enqueueBlock(HeapSource *heapSource, size_t block)
{
    assert(heapSource != NULL);
    assert(block < heapSource->totalBlocks);
    if (heapSource->queueHead != QUEUE_TAIL) {
        heapSource->blockQueue[heapSource->queueTail] = block;
    } else {
        heapSource->queueHead = block;
    }
    heapSource->blockQueue[block] = QUEUE_TAIL;
    heapSource->queueTail = block;
    ++heapSource->queueSize;
}

/*
 * Grays all objects within the block corresponding to the given
 * address.
 */
static void promoteBlockByAddr(HeapSource *heapSource, const void *addr)
{
    size_t block;

    block = addressToBlock(heapSource, (const u1 *)addr);
    if (heapSource->blockSpace[block] != BLOCK_TO_SPACE) {
        // LOGI("promoting block %zu %d @ %p", block, heapSource->blockSpace[block], obj);
        heapSource->blockSpace[block] = BLOCK_TO_SPACE;
        enqueueBlock(heapSource, block);
        /* TODO(cshapiro): count continued blocks?*/
        heapSource->allocBlocks += 1;
    } else {
        // LOGI("NOT promoting block %zu %d @ %p", block, heapSource->blockSpace[block], obj);
    }
}

GcHeap *dvmHeapSourceStartup(size_t startSize, size_t absoluteMaxSize)
{
    GcHeap* gcHeap;
    HeapSource *heapSource;

    assert(startSize <= absoluteMaxSize);

    heapSource = malloc(sizeof(*heapSource));
    assert(heapSource != NULL);
    memset(heapSource, 0, sizeof(*heapSource));

    heapSource->minimumSize = alignUp(startSize, BLOCK_SIZE);
    heapSource->maximumSize = alignUp(absoluteMaxSize, BLOCK_SIZE);

    heapSource->currentSize = heapSource->maximumSize;

    /* Allocate underlying storage for blocks. */
    heapSource->blockBase = virtualAlloc(heapSource->maximumSize);
    assert(heapSource->blockBase != NULL);
    heapSource->baseBlock = (uintptr_t) heapSource->blockBase >> BLOCK_SHIFT;
    heapSource->limitBlock = ((uintptr_t) heapSource->blockBase + heapSource->maximumSize) >> BLOCK_SHIFT;

    heapSource->allocBlocks = 0;
    heapSource->totalBlocks = (heapSource->limitBlock - heapSource->baseBlock);

    assert(heapSource->totalBlocks = heapSource->maximumSize / BLOCK_SIZE);

    {
        size_t size = sizeof(heapSource->blockQueue[0]);
        heapSource->blockQueue = malloc(heapSource->totalBlocks*size);
        assert(heapSource->blockQueue != NULL);
        memset(heapSource->blockQueue, 0xCC, heapSource->totalBlocks*size);
        heapSource->queueHead = QUEUE_TAIL;
    }

    /* Byte indicating space residence or free status of block. */
    {
        size_t size = sizeof(heapSource->blockSpace[0]);
        heapSource->blockSpace = malloc(heapSource->totalBlocks*size);
        assert(heapSource->blockSpace != NULL);
        memset(heapSource->blockSpace, 0, heapSource->totalBlocks*size);
    }

    dvmHeapBitmapInit(&heapSource->allocBits,
                      heapSource->blockBase,
                      heapSource->maximumSize,
                      "blockBase");

    /* Initialize allocation pointers. */
    heapSource->allocPtr = allocateBlocks(heapSource, 1);
    heapSource->allocLimit = heapSource->allocPtr + BLOCK_SIZE;

    gcHeap = malloc(sizeof(*gcHeap));
    assert(gcHeap != NULL);
    memset(gcHeap, 0, sizeof(*gcHeap));
    gcHeap->heapSource = heapSource;

    return gcHeap;
}

/*
 * Perform any required heap initializations after forking from the
 * zygote process.  This is a no-op for the time being.  Eventually
 * this will demarcate the shared region of the heap.
 */
bool dvmHeapSourceStartupAfterZygote(void)
{
    return true;
}

bool dvmHeapSourceStartupBeforeFork(void)
{
    assert(!"implemented");
    return false;
}

void dvmHeapSourceShutdown(GcHeap **gcHeap)
{
    if (*gcHeap == NULL || (*gcHeap)->heapSource == NULL)
        return;
    virtualFree((*gcHeap)->heapSource->blockBase,
                (*gcHeap)->heapSource->maximumSize);
    free((*gcHeap)->heapSource);
    (*gcHeap)->heapSource = NULL;
    free(*gcHeap);
    *gcHeap = NULL;
}

size_t dvmHeapSourceGetValue(enum HeapSourceValueSpec spec,
                             size_t perHeapStats[],
                             size_t arrayLen)
{
    HeapSource *heapSource;
    size_t value;

    heapSource = gDvm.gcHeap->heapSource;
    switch (spec) {
    case HS_EXTERNAL_BYTES_ALLOCATED:
        value = 0;
        break;
    case HS_EXTERNAL_LIMIT:
        value = 0;
        break;
    case HS_FOOTPRINT:
        value = heapSource->maximumSize;
        break;
    case HS_ALLOWED_FOOTPRINT:
        value = heapSource->maximumSize;
        break;
    case HS_BYTES_ALLOCATED:
        value = heapSource->bytesAllocated;
        break;
    case HS_OBJECTS_ALLOCATED:
        value = sumHeapBitmap(&heapSource->allocBits);
        break;
    default:
        assert(!"implemented");
        value = 0;
    }
    if (perHeapStats) {
        *perHeapStats = value;
    }
    return value;
}

/*
 * Performs a shallow copy of the allocation bitmap into the given
 * vector of heap bitmaps.
 */
void dvmHeapSourceGetObjectBitmaps(HeapBitmap objBits[], HeapBitmap markBits[],
                                   size_t numHeaps)
{
    assert(!"implemented");
}

HeapBitmap *dvmHeapSourceGetLiveBits(void)
{
    assert(!"implemented");
    return NULL;
}

/*
 * Allocate the specified number of bytes from the heap.  The
 * allocation cursor points into a block of free storage.  If the
 * given allocation fits in the remaining space of the block, we
 * advance the cursor and return a pointer to the free storage.  If
 * the allocation cannot fit in the current block but is smaller than
 * a block we request a new block and allocate from it instead.  If
 * the allocation is larger than a block we must allocate from a span
 * of contiguous blocks.
 */
void *dvmHeapSourceAlloc(size_t length)
{
    HeapSource *heapSource;
    unsigned char *addr;
    size_t aligned, available, blocks;

    heapSource = gDvm.gcHeap->heapSource;
    assert(heapSource != NULL);
    assert(heapSource->allocPtr != NULL);
    assert(heapSource->allocLimit != NULL);

    aligned = alignUp(length, ALLOC_ALIGNMENT);
    available = heapSource->allocLimit - heapSource->allocPtr;

    /* Try allocating inside the current block. */
    if (aligned <= available) {
        addr = heapSource->allocPtr;
        heapSource->allocPtr += aligned;
        heapSource->bytesAllocated += aligned;
        dvmHeapBitmapSetObjectBit(&heapSource->allocBits, addr);
        return addr;
    }

    /* Try allocating in a new block. */
    if (aligned <= BLOCK_SIZE) {
        addr =  allocateBlocks(heapSource, 1);
        if (addr != NULL) {
            heapSource->allocLimit = addr + BLOCK_SIZE;
            heapSource->allocPtr = addr + aligned;
            heapSource->bytesAllocated += aligned;
            dvmHeapBitmapSetObjectBit(&heapSource->allocBits, addr);
            /* TODO(cshapiro): pad out the current block. */
        }
        return addr;
    }

    /* Try allocating in a span of blocks. */
    blocks = alignUp(aligned, BLOCK_SIZE) / BLOCK_SIZE;

    addr = allocateBlocks(heapSource, blocks);
    /* Propagate failure upward. */
    if (addr != NULL) {
        heapSource->bytesAllocated += aligned;
        dvmHeapBitmapSetObjectBit(&heapSource->allocBits, addr);
        /* TODO(cshapiro): pad out free space in the last block. */
    }
    return addr;
}

void *dvmHeapSourceAllocAndGrow(size_t size)
{
    return dvmHeapSourceAlloc(size);
}

/* TODO: refactor along with dvmHeapSourceAlloc */
void *allocateGray(size_t size)
{
    assert(gDvm.gcHeap->heapSource->queueHead != QUEUE_TAIL);
    return dvmHeapSourceAlloc(size);
}

/*
 * Returns true if the given address is within the heap and points to
 * the header of a live object.
 */
bool dvmHeapSourceContains(const void *addr)
{
    HeapSource *heapSource;
    HeapBitmap *bitmap;

    heapSource = gDvm.gcHeap->heapSource;
    bitmap = &heapSource->allocBits;
    return dvmHeapBitmapIsObjectBitSet(bitmap, addr);
}

bool dvmHeapSourceGetPtrFlag(const void *ptr, enum HeapSourcePtrFlag flag)
{
    assert(!"implemented");
    return false;
}

size_t dvmHeapSourceChunkSize(const void *ptr)
{
    assert(!"implemented");
    return 0;
}

size_t dvmHeapSourceFootprint(void)
{
    assert(!"implemented");
    return 0;
}

/*
 * Returns the "ideal footprint" which appears to be the number of
 * bytes currently committed to the heap.  This starts out at the
 * start size of the heap and grows toward the maximum size.
 */
size_t dvmHeapSourceGetIdealFootprint(void)
{
    return gDvm.gcHeap->heapSource->currentSize;
}

float dvmGetTargetHeapUtilization(void)
{
    assert(!"implemented");
    return 0.0f;
}

void dvmSetTargetHeapUtilization(float newTarget)
{
    assert(!"implemented");
}

size_t dvmMinimumHeapSize(size_t size, bool set)
{
    return gDvm.gcHeap->heapSource->minimumSize;
}

/*
 * Expands the size of the heap after a collection.  At present we
 * commit the pages for maximum size of the heap so this routine is
 * just a no-op.  Eventually, we will either allocate or commit pages
 * on an as-need basis.
 */
void dvmHeapSourceGrowForUtilization(void)
{
    /* do nothing */
}

void dvmHeapSourceTrim(size_t bytesTrimmed[], size_t arrayLen)
{
    /* do nothing */
}

void dvmHeapSourceWalk(void (*callback)(const void *chunkptr, size_t chunklen,
                                        const void *userptr, size_t userlen,
                                        void *arg),
                       void *arg)
{
    assert(!"implemented");
}

size_t dvmHeapSourceGetNumHeaps(void)
{
    return 1;
}

bool dvmTrackExternalAllocation(size_t n)
{
    assert(!"implemented");
    return false;
}

void dvmTrackExternalFree(size_t n)
{
    assert(!"implemented");
}

size_t dvmGetExternalBytesAllocated(void)
{
    assert(!"implemented");
    return 0;
}

void dvmHeapSourceFlip(void)
{
    HeapSource *heapSource;
    size_t i;

    heapSource = gDvm.gcHeap->heapSource;

    /* Reset the block queue. */
    heapSource->allocBlocks = 0;
    heapSource->queueSize = 0;
    heapSource->queueHead = QUEUE_TAIL;

    /* Hack: reset the reference lists. */
    /* TODO(cshapiro): implement reference object processing. */
    heapSource->softReferenceList = NULL;
    heapSource->weakReferenceList = NULL;
    heapSource->phantomReferenceList = NULL;

    /* TODO(cshapiro): pad the current (prev) block. */

    heapSource->allocPtr = NULL;
    heapSource->allocLimit = NULL;

    /* Whiten all allocated blocks. */
    for (i = 0; i < heapSource->totalBlocks; ++i) {
        if (heapSource->blockSpace[i] == BLOCK_TO_SPACE) {
            heapSource->blockSpace[i] = BLOCK_FROM_SPACE;
        }
    }
}

static void room(size_t *alloc, size_t *avail, size_t *total)
{
    HeapSource *heapSource;
    size_t i;

    heapSource = gDvm.gcHeap->heapSource;
    *total = heapSource->totalBlocks*BLOCK_SIZE;
    *alloc = heapSource->allocBlocks*BLOCK_SIZE;
    *avail = *total - *alloc;
}

static bool isSpaceInternal(u1 *addr, int space)
{
    HeapSource *heapSource;
    u1 *base, *limit;
    size_t offset;
    char space2;

    heapSource = gDvm.gcHeap->heapSource;
    base = heapSource->blockBase;
    assert(addr >= base);
    limit = heapSource->blockBase + heapSource->maximumSize;
    assert(addr < limit);
    offset = addr - base;
    space2 = heapSource->blockSpace[offset >> BLOCK_SHIFT];
    return space == space2;
}

static bool isFromSpace(const void *addr)
{
    return isSpaceInternal((u1 *)addr, BLOCK_FROM_SPACE);
}

static bool isToSpace(const void *addr)
{
    return isSpaceInternal((u1 *)addr, BLOCK_TO_SPACE);
}

/*
 * Notifies the collector that the object at the given address must
 * remain stationary during the current collection.
 */
static void pinObject(const Object *obj)
{
    promoteBlockByAddr(gDvm.gcHeap->heapSource, obj);
}

static void printHeapBitmap(const HeapBitmap *bitmap)
{
    const char *cp;
    size_t i, length;

    length = bitmap->bitsLen >> 2;
    fprintf(stderr, "%p", bitmap->bits);
    for (i = 0; i < length; ++i) {
        fprintf(stderr, " %lx", bitmap->bits[i]);
        fputc('\n', stderr);
    }
}

static void printHeapBitmapSxS(const HeapBitmap *b1, const HeapBitmap *b2)
{
    uintptr_t addr;
    size_t i, length;

    assert(b1->base == b2->base);
    assert(b1->bitsLen == b2->bitsLen);
    addr = b1->base;
    length = b1->bitsLen >> 2;
    for (i = 0; i < length; ++i) {
        int diff = b1->bits[i] == b2->bits[i];
        fprintf(stderr, "%08x %08lx %08lx %d\n",
                addr, b1->bits[i], b2->bits[i], diff);
        addr += sizeof(*b1->bits)*CHAR_BIT;
    }
}

static size_t sumHeapBitmap(const HeapBitmap *bitmap)
{
    const char *cp;
    size_t i, sum;

    sum = 0;
    for (i = 0; i < bitmap->bitsLen >> 2; ++i) {
        sum += dvmClzImpl(bitmap->bits[i]);
    }
    return sum;
}

/*
 * Miscellaneous functionality.
 */

static int isForward(const void *addr)
{
    return (uintptr_t)addr & 0x1;
}

static void setForward(const void *toObj, void *fromObj)
{
    *(unsigned long *)fromObj = (uintptr_t)toObj | 0x1;
}

static void* getForward(const void *fromObj)
{
    return (void *)((uintptr_t)fromObj & ~0x1);
}

/* Beware, uses the same encoding as a forwarding pointers! */
static int isPermanentString(const StringObject *obj) {
    return (uintptr_t)obj & 0x1;
}

static void* getPermanentString(const StringObject *obj)
{
    return (void *)((uintptr_t)obj & ~0x1);
}


/*
 * Scavenging and transporting routines follow.  A transporter grays
 * an object.  A scavenger blackens an object.  We define these
 * routines for each fundamental object type.  Dispatch is performed
 * in scavengeObject.
 */

/*
 * Class object scavenging and transporting.
 */

static ClassObject *transportClassObject(const ClassObject *fromObj)
{
    ClassObject *toObj;
    size_t length;

    LOG_TRANSPORT("transportClassObject(fromObj=%p)", fromObj);
    length = dvmClassObjectSize(fromObj);  /* TODO: hash code */
    assert(length != 0);
    toObj = allocateGray(length);
    assert(toObj != NULL);
    memcpy(toObj, fromObj, length);
    LOG_TRANSPORT("transportClassObject: from %p to %p (%zu)", fromObj, toObj, length);
    return toObj;
}

static size_t scavengeClassObject(ClassObject *obj)
{
    size_t size;
    int i;

    assert(obj != NULL);
    LOG_SCAVENGE("scavengeClassObject(obj=%p)", obj);
    /* Scavenge our class object. */
    assert(obj->obj.clazz != NULL);
    assert(obj->obj.clazz->descriptor != NULL);
    assert(!strcmp(obj->obj.clazz->descriptor, "Ljava/lang/Class;"));
    assert(obj->descriptor != NULL);
    LOG_SCAVENGE("scavengeClassObject: descriptor='%s',vtableCount=%zu",
                 obj->descriptor, obj->vtableCount);
    scavengeReference((Object **) obj);
    /* Scavenge the array element class object. */
    if (IS_CLASS_FLAG_SET(obj, CLASS_ISARRAY)) {
        scavengeReference((Object **)(void *)&obj->elementClass);
    }
    /* Scavenge the superclass. */
    scavengeReference((Object **)(void *)&obj->super);
    /* Scavenge the class loader. */
    scavengeReference(&obj->classLoader);
    /* Scavenge static fields. */
    for (i = 0; i < obj->sfieldCount; ++i) {
        char ch = obj->sfields[i].field.signature[0];
        if (ch == '[' || ch == 'L') {
            scavengeReference((Object **)(void *)&obj->sfields[i].value.l);
        }
    }
    /* Scavenge interface class objects. */
    for (i = 0; i < obj->interfaceCount; ++i) {
        scavengeReference((Object **) &obj->interfaces[i]);
    }
    size = dvmClassObjectSize(obj);
    return size;
}

/*
 * Array object scavenging.
 */

static ArrayObject *transportArrayObject(const ArrayObject *fromObj)
{
    ArrayObject *toObj;
    size_t length;

    LOG_TRANSPORT("transportArrayObject(fromObj=%p)", fromObj);
    length = dvmArrayObjectSize(fromObj);
    assert(length != 0);
    if (length >= BLOCK_SIZE) {
        LOGI("WARNING: LARGE ARRAY OBJECT %s", fromObj->obj.clazz->descriptor);
    }
    toObj = allocateGray(length);
    LOG_TRANSPORT("transportArrayObject: from %p to %p (%zu)", fromObj, toObj, length);
    assert(toObj != NULL);
    memcpy(toObj, fromObj, length);
    return toObj;
}

static size_t scavengeArrayObject(ArrayObject *array)
{
    size_t i, length;

    LOG_SCAVENGE("scavengeArrayObject(array=%p)", array);
    /* Scavenge the class object. */
    assert(isToSpace(array));
    assert(array != NULL);
    assert(array->obj.clazz != NULL);
    scavengeReference((Object **) array);
    length = dvmArrayObjectSize(array);
    /* Scavenge the array contents. */
    if (IS_CLASS_FLAG_SET(array->obj.clazz, CLASS_ISOBJECTARRAY)) {
        Object **contents = (Object **)array->contents;
        for (i = 0; i < array->length; ++i) {
            scavengeReference(&contents[i]);
        }
    }
    return length;
}

/*
 * Reference object scavenging.
 */

static int getReferenceFlags(const DataObject *obj)
{
    int flags;

    flags = CLASS_ISREFERENCE |
            CLASS_ISWEAKREFERENCE |
            CLASS_ISPHANTOMREFERENCE;
    return GET_CLASS_FLAG_GROUP(obj->obj.clazz, flags);
}

static int isReference(const DataObject *obj)
{
    return getReferenceFlags(obj) != 0;
}

static int isSoftReference(const DataObject *obj)
{
    return getReferenceFlags(obj) == CLASS_ISREFERENCE;
}

static int isWeakReference(const DataObject *obj)
{
    return getReferenceFlags(obj) & CLASS_ISWEAKREFERENCE;
}

static bool isPhantomReference(const DataObject *obj)
{
    return getReferenceFlags(obj) & CLASS_ISPHANTOMREFERENCE;
}

static void clearReference(DataObject *reference)
{
    size_t offset;

    assert(isSoftReference(reference) || isWeakReference(reference));
    offset = gDvm.offJavaLangRefReference_referent;
    dvmSetFieldObject((Object *)reference, offset, NULL);
}

static bool isReferentGray(const DataObject *reference)
{
    Object *obj;
    size_t offset;

    assert(reference != NULL);
    assert(isSoftReference(reference) || isWeakReference(reference));
    offset = gDvm.offJavaLangRefReference_referent;
    obj = dvmGetFieldObject((Object *)reference, offset);
    return obj == NULL || isToSpace(obj);
}

static void enqueueReference(HeapSource *heapSource, DataObject *reference)
{
    DataObject **queue;
    size_t offset;

    LOGI("enqueueReference(heapSource=%p,reference=%p)", heapSource, reference);
    assert(heapSource != NULL);
    assert(reference != NULL);
    assert(isToSpace(reference));
    assert(isReference(reference));
    if (isSoftReference(reference)) {
        queue = &heapSource->softReferenceList;
    } else if (isWeakReference(reference)) {
        queue = &heapSource->weakReferenceList;
    } else if (isPhantomReference(reference)) {
        queue = &heapSource->phantomReferenceList;
    } else {
        assert(!"reached");
        queue = NULL;
    }
    offset = gDvm.offJavaLangRefReference_vmData;
    dvmSetFieldObject((Object *)reference, offset, (Object *)*queue);
    *queue = reference;
}

static DataObject *transportReferenceObject(const DataObject *fromObj)
{
    assert(fromObj != NULL);
    LOG_TRANSPORT("transportReferenceObject(fromObj=%p)", fromObj);
    return transportDataObject(fromObj);
}

/*
 * If a reference points to from-space and has been forwarded, we snap
 * the pointer to its new to-space address.  If the reference points
 * to an unforwarded from-space address we must enqueue the reference
 * for later processing.  TODO: implement proper reference processing
 * and move the referent scavenging elsewhere.
 */
static size_t scavengeReferenceObject(DataObject *obj)
{
    size_t length;

    assert(obj != NULL);
    LOG_SCAVENGE("scavengeReferenceObject(obj=%p),'%s'", obj, obj->obj.clazz->descriptor);
    {
        /* Always scavenge the hidden Reference.referent field. */
        size_t offset = gDvm.offJavaLangRefReference_referent;
        void *addr = BYTE_OFFSET((Object *)obj, offset);
        Object **ref = (Object **)(void *)&((JValue *)addr)->l;
        scavengeReference(ref);
    }
    length = scavengeDataObject(obj);
    if (!isReferentGray(obj)) {
        assert(!"reached");  /* TODO(cshapiro): remove this */
        LOG_SCAVENGE("scavengeReferenceObject: enqueueing %p", obj);
        enqueueReference(gDvm.gcHeap->heapSource, obj);
        length = obj->obj.clazz->objectSize;
    }
    return length;
}

/*
 * Data object scavenging.
 */

static DataObject *transportDataObject(const DataObject *fromObj)
{
    DataObject *toObj;
    ClassObject *clazz;
    const char *name;
    size_t length;
    int flags;

    assert(fromObj != NULL);
    assert(isFromSpace(fromObj));
    LOG_TRANSPORT("transportDataObject(fromObj=%p) allocBlocks=%zu", fromObj, gDvm.gcHeap->heapSource->allocBlocks);
    clazz = fromObj->obj.clazz;
    assert(clazz != NULL);
    length = clazz->objectSize;
    assert(length != 0);
    /* TODO(cshapiro): don't copy, re-map large data objects. */
    toObj = allocateGray(length);
    assert(toObj != NULL);
    assert(isToSpace(toObj));
    memcpy(toObj, fromObj, length);
    LOG_TRANSPORT("transportDataObject: from %p/%zu to %p/%zu (%zu)", fromObj, addressToBlock(gDvm.gcHeap->heapSource,fromObj), toObj, addressToBlock(gDvm.gcHeap->heapSource,toObj), length);
    return toObj;
}

static size_t scavengeDataObject(DataObject *obj)
{
    ClassObject *clazz;
    size_t length;
    int i;

    // LOGI("scavengeDataObject(obj=%p)", obj);
    assert(obj != NULL);
    assert(obj->obj.clazz != NULL);
    assert(obj->obj.clazz->objectSize != 0);
    assert(isToSpace(obj));
    /* Scavenge the class object. */
    clazz = obj->obj.clazz;
    scavengeReference((Object **) obj);
    length = obj->obj.clazz->objectSize;
    /* Scavenge instance fields. */
    if (clazz->refOffsets != CLASS_WALK_SUPER) {
        size_t refOffsets = clazz->refOffsets;
        while (refOffsets != 0) {
            size_t rshift = CLZ(refOffsets);
            size_t offset = CLASS_OFFSET_FROM_CLZ(rshift);
            Object **ref = (Object **)((u1 *)obj + offset);
            scavengeReference(ref);
            refOffsets &= ~(CLASS_HIGH_BIT >> rshift);
        }
    } else {
        for (; clazz != NULL; clazz = clazz->super) {
            InstField *field = clazz->ifields;
            for (i = 0; i < clazz->ifieldRefCount; ++i, ++field) {
                size_t offset = field->byteOffset;
                Object **ref = (Object **)((u1 *)obj + offset);
                scavengeReference(ref);
            }
        }
    }
    return length;
}

/*
 * Generic reference scavenging.
 */

/*
 * Given a reference to an object, the scavenge routine will gray the
 * reference.  Any objects pointed to by the scavenger object will be
 * transported to new space and a forwarding pointer will be installed
 * in the header of the object.
 */

/*
 * Blacken the given pointer.  If the pointer is in from space, it is
 * transported to new space.  If the object has a forwarding pointer
 * installed it has already been transported and the referent is
 * snapped to the new address.
 */
static size_t scavengeReference(Object **obj)
{
    ClassObject *clazz;
    uintptr_t word;

    assert(obj);

    if (*obj == NULL) goto exit;

    assert(dvmIsValidObject(*obj));

    /* The entire block is black. */
    if (isToSpace(*obj)) {
        LOG_SCAVENGE("scavengeReference skipping pinned object @ %p", *obj);
        goto exit;
    }
    LOG_SCAVENGE("scavengeReference(*obj=%p)", *obj);

    assert(isFromSpace(*obj));

    clazz = (*obj)->clazz;

    if (isForward(clazz)) {
        // LOGI("forwarding %p @ %p to %p", *obj, obj, (void *)((uintptr_t)clazz & ~0x1));
        *obj = (Object *)getForward(clazz);
    } else if (clazz == NULL) {
        // LOGI("scavangeReference %p has a NULL class object", *obj);
        assert(!"implemented");
    } else if (clazz == gDvm.unlinkedJavaLangClass) {
        // LOGI("scavangeReference %p is an unlinked class object", *obj);
        assert(!"implemented");
    } else if (clazz == gDvm.classJavaLangClass) {
        ClassObject *toObj;

        toObj = transportClassObject((ClassObject *)*obj);
        setForward(toObj, *obj);
        *obj = (Object *)toObj;
    } else if (IS_CLASS_FLAG_SET(clazz, CLASS_ISARRAY)) {
        ArrayObject *toObj;

        toObj = transportArrayObject((ArrayObject *)*obj);
        setForward(toObj, *obj);
        *obj = (Object *)toObj;
    } else if (IS_CLASS_FLAG_SET(clazz, CLASS_ISREFERENCE)) {
        DataObject *toObj;

        toObj = transportReferenceObject((DataObject *)*obj);
        setForward(toObj, *obj);
        *obj = (Object *)toObj;
    } else {
        DataObject *toObj;

        toObj = transportDataObject((DataObject *)*obj);
        setForward(toObj, *obj);
        *obj = (Object *)toObj;
    }
exit:
    return sizeof(Object *);
}

static void verifyReference(const void *obj)
{
    HeapSource *heapSource;
    size_t block;
    char space;

    if (obj == NULL) {
        LOG_VERIFY("verifyReference(obj=%p)", obj);
        return;
    }
    heapSource = gDvm.gcHeap->heapSource;
    block = addressToBlock(heapSource, obj);
    space = heapSource->blockSpace[block];
    LOG_VERIFY("verifyReference(obj=%p),block=%zu,space=%d", obj, block, space);
    assert(!((uintptr_t)obj & 7));
    assert(isToSpace(obj));
    assert(dvmIsValidObject(obj));
}

/*
 * Generic object scavenging.
 */

static size_t scavengeObject(Object *obj)
{
    ClassObject *clazz;
    size_t length;

    assert(obj != NULL);
    clazz = obj->clazz;
    assert(clazz != NULL);
    assert(!((uintptr_t)clazz & 0x1));
    assert(clazz != gDvm.unlinkedJavaLangClass);
    if (clazz == gDvm.classJavaLangClass) {
        length = scavengeClassObject((ClassObject *)obj);
    } else if (IS_CLASS_FLAG_SET(clazz, CLASS_ISARRAY)) {
        length = scavengeArrayObject((ArrayObject *)obj);
    } else if (IS_CLASS_FLAG_SET(clazz, CLASS_ISREFERENCE)) {
        length = scavengeReferenceObject((DataObject *)obj);
    } else {
        length = scavengeDataObject((DataObject *)obj);
    }
    return length;
}

/*
 * External root scavenging routines.
 */

static void scavengeHashTable(HashTable *table)
{
    HashEntry *entry;
    void *obj;
    int i;

    if (table == NULL) {
        return;
    }
    dvmHashTableLock(table);
    for (i = 0; i < table->tableSize; ++i) {
        entry = &table->pEntries[i];
        obj = entry->data;
        if (obj == NULL || obj == HASH_TOMBSTONE) {
            continue;
        }
        scavengeReference((Object **)(void *)&entry->data);
    }
    dvmHashTableUnlock(table);
}

static void pinHashTableEntries(HashTable *table)
{
    HashEntry *entry;
    void *obj;
    int i;

    LOGI(">>> pinHashTableEntries(table=%p)", table);
    if (table == NULL) {
        return;
    }
    dvmHashTableLock(table);
    for (i = 0; i < table->tableSize; ++i) {
        entry = &table->pEntries[i];
        obj = entry->data;
        if (obj == NULL || obj == HASH_TOMBSTONE) {
            continue;
        }
        pinObject(entry->data);
    }
    dvmHashTableUnlock(table);
    LOGI("<<< pinHashTableEntries(table=%p)", table);
}

static void pinPrimitiveClasses(void)
{
    size_t length;
    size_t i;

    length = ARRAYSIZE(gDvm.primitiveClass);
    for (i = 0; i < length; i++) {
        if (gDvm.primitiveClass[i] != NULL) {
            pinObject((Object *)gDvm.primitiveClass[i]);
        }
    }
}

/*
 * Scavenge interned strings.  Permanent interned strings will have
 * been pinned and are therefore ignored.  Non-permanent strings that
 * have been forwarded are snapped.  All other entries are removed.
 */
static void scavengeInternedStrings(void)
{
    HashTable *table;
    HashEntry *entry;
    Object *obj;
    int i;

    table = gDvm.internedStrings;
    if (table == NULL) {
        return;
    }
    dvmHashTableLock(table);
    for (i = 0; i < table->tableSize; ++i) {
        entry = &table->pEntries[i];
        obj = (Object *)entry->data;
        if (obj == NULL || obj == HASH_TOMBSTONE) {
            continue;
        } else if (!isPermanentString((StringObject *)obj)) {
            // LOGI("entry->data=%p", entry->data);
            LOG_SCAVENGE(">>> string obj=%p", entry->data);
            /* TODO(cshapiro): detach white string objects */
            scavengeReference((Object **)(void *)&entry->data);
            LOG_SCAVENGE("<<< string obj=%p", entry->data);
        }
    }
    dvmHashTableUnlock(table);
}

static void pinInternedStrings(void)
{
    HashTable *table;
    HashEntry *entry;
    Object *obj;
    int i;

    table = gDvm.internedStrings;
    if (table == NULL) {
        return;
    }
    dvmHashTableLock(table);
    for (i = 0; i < table->tableSize; ++i) {
        entry = &table->pEntries[i];
        obj = (Object *)entry->data;
        if (obj == NULL || obj == HASH_TOMBSTONE) {
            continue;
        } else if (isPermanentString((StringObject *)obj)) {
            obj = (Object *)getPermanentString((StringObject*)obj);
            LOG_PROMOTE(">>> pin string obj=%p", obj);
            pinObject(obj);
            LOG_PROMOTE("<<< pin string obj=%p", obj);
        }
     }
    dvmHashTableUnlock(table);
}

static void verifyInternedStrings(void)
{
    HashTable *table;
    HashEntry *entry;
    Object *fwd, *obj;
    int i;

    table = gDvm.internedStrings;
    if (table == NULL) {
        return;
    }
    dvmHashTableLock(table);
    for (i = 0; i < table->tableSize; ++i) {
        entry = &table->pEntries[i];
        obj = (Object *)entry->data;
        if (obj == NULL || obj == HASH_TOMBSTONE) {
            continue;
        } else if (isPermanentString((StringObject *)obj)) {
            fwd = (Object *)getForward(obj);
            LOG_VERIFY(">>> verify string fwd=%p obj=%p", fwd, obj);
            verifyReference(fwd);
            LOG_VERIFY(">>> verify string fwd=%p obj=%p", fwd, obj);
        } else {
            LOG_SCAVENGE(">>> verify string obj=%p %p", obj, entry->data);
            verifyReference(obj);
            LOG_SCAVENGE("<<< verify string obj=%p %p", obj, entry->data);
        }
    }
    dvmHashTableUnlock(table);
}

/*
 * At present, reference tables contain references that must not be
 * moved by the collector.  Instead of scavenging each reference in
 * the table we pin each referenced object.
 */
static void pinReferenceTable(ReferenceTable *table)
{
    Object **entry;
    int i;

    assert(table != NULL);
    assert(table->table != NULL);
    assert(table->nextEntry != NULL);
    for (entry = table->table; entry < table->nextEntry; ++entry) {
        assert(entry != NULL);
        assert(!isForward(*entry));
        pinObject(*entry);
    }
}

static void verifyReferenceTable(const ReferenceTable *table)
{
    Object **entry;
    int i;

    LOGI(">>> verifyReferenceTable(table=%p)", table);
    for (entry = table->table; entry < table->nextEntry; ++entry) {
        assert(entry != NULL);
        assert(!isForward(*entry));
        verifyReference(*entry);
    }
    LOGI("<<< verifyReferenceTable(table=%p)", table);
}

static void scavengeLargeHeapRefTable(LargeHeapRefTable *table)
{
    Object **entry;

    for (; table != NULL; table = table->next) {
        for (entry = table->refs.table; entry < table->refs.nextEntry; ++entry) {
            if ((uintptr_t)*entry & ~0x3) {
                /* It's a pending reference operation. */
                assert(!"implemented");
            }
            scavengeReference(entry);
        }
    }
}

/* This code was copied from Thread.c */
static void scavengeThreadStack(Thread *thread)
{
    const u4 *framePtr;
#if WITH_EXTRA_GC_CHECKS > 1
    bool first = true;
#endif

    framePtr = (const u4 *)thread->curFrame;
    while (framePtr != NULL) {
        const StackSaveArea *saveArea;
        const Method *method;

        saveArea = SAVEAREA_FROM_FP(framePtr);
        method = saveArea->method;
        if (method != NULL && !dvmIsNativeMethod(method)) {
#ifdef COUNT_PRECISE_METHODS
            /* the GC is running, so no lock required */
            if (dvmPointerSetAddEntry(gDvm.preciseMethods, method))
                LOGI("PGC: added %s.%s %p\n",
                     method->clazz->descriptor, method->name, method);
#endif
#if WITH_EXTRA_GC_CHECKS > 1
            /*
             * May also want to enable the memset() in the "invokeMethod"
             * goto target in the portable interpreter.  That sets the stack
             * to a pattern that makes referring to uninitialized data
             * very obvious.
             */

            if (first) {
                /*
                 * First frame, isn't native, check the "alternate" saved PC
                 * as a sanity check.
                 *
                 * It seems like we could check the second frame if the first
                 * is native, since the PCs should be the same.  It turns out
                 * this doesn't always work.  The problem is that we could
                 * have calls in the sequence:
                 *   interp method #2
                 *   native method
                 *   interp method #1
                 *
                 * and then GC while in the native method after returning
                 * from interp method #2.  The currentPc on the stack is
                 * for interp method #1, but thread->currentPc2 is still
                 * set for the last thing interp method #2 did.
                 *
                 * This can also happen in normal execution:
                 * - sget-object on not-yet-loaded class
                 * - class init updates currentPc2
                 * - static field init is handled by parsing annotations;
                 *   static String init requires creation of a String object,
                 *   which can cause a GC
                 *
                 * Essentially, any pattern that involves executing
                 * interpreted code and then causes an allocation without
                 * executing instructions in the original method will hit
                 * this.  These are rare enough that the test still has
                 * some value.
                 */
                if (saveArea->xtra.currentPc != thread->currentPc2) {
                    LOGW("PGC: savedPC(%p) != current PC(%p), %s.%s ins=%p\n",
                        saveArea->xtra.currentPc, thread->currentPc2,
                        method->clazz->descriptor, method->name, method->insns);
                    if (saveArea->xtra.currentPc != NULL)
                        LOGE("  pc inst = 0x%04x\n", *saveArea->xtra.currentPc);
                    if (thread->currentPc2 != NULL)
                        LOGE("  pc2 inst = 0x%04x\n", *thread->currentPc2);
                    dvmDumpThread(thread, false);
                }
            } else {
                /*
                 * It's unusual, but not impossible, for a non-first frame
                 * to be at something other than a method invocation.  For
                 * example, if we do a new-instance on a nonexistent class,
                 * we'll have a lot of class loader activity on the stack
                 * above the frame with the "new" operation.  Could also
                 * happen while we initialize a Throwable when an instruction
                 * fails.
                 *
                 * So there's not much we can do here to verify the PC,
                 * except to verify that it's a GC point.
                 */
            }
            assert(saveArea->xtra.currentPc != NULL);
#endif

            const RegisterMap* pMap;
            const u1* regVector;
            int i;

            Method* nonConstMethod = (Method*) method;  // quiet gcc
            pMap = dvmGetExpandedRegisterMap(nonConstMethod);

            /* assert(pMap != NULL); */

            //LOGI("PGC: %s.%s\n", method->clazz->descriptor, method->name);

            if (pMap != NULL) {
                /* found map, get registers for this address */
                int addr = saveArea->xtra.currentPc - method->insns;
                regVector = dvmRegisterMapGetLine(pMap, addr);
                /*
                if (regVector == NULL) {
                    LOGI("PGC: map but no entry for %s.%s addr=0x%04x\n",
                         method->clazz->descriptor, method->name, addr);
                } else {
                    LOGI("PGC: found map for %s.%s 0x%04x (t=%d)\n",
                         method->clazz->descriptor, method->name, addr,
                         thread->threadId);
                }
                */
            } else {
                /*
                 * No map found.  If precise GC is disabled this is
                 * expected -- we don't create pointers to the map data even
                 * if it's present -- but if it's enabled it means we're
                 * unexpectedly falling back on a conservative scan, so it's
                 * worth yelling a little.
                 */
                if (gDvm.preciseGc) {
                    LOGI("PGC: no map for %s.%s\n", method->clazz->descriptor, method->name);
                }
                regVector = NULL;
            }

            /* assert(regVector != NULL); */

            if (regVector == NULL) {
                /* conservative scan */
                for (i = method->registersSize - 1; i >= 0; i--) {
                    u4 rval = *framePtr++;
                    if (rval != 0 && (rval & 0x3) == 0) {
                        abort();
                        /* dvmMarkIfObject((Object *)rval); */
                    }
                }
            } else {
                /*
                 * Precise scan.  v0 is at the lowest address on the
                 * interpreted stack, and is the first bit in the register
                 * vector, so we can walk through the register map and
                 * memory in the same direction.
                 *
                 * A '1' bit indicates a live reference.
                 */
                u2 bits = 1 << 1;
                for (i = method->registersSize - 1; i >= 0; i--) {
                    /* u4 rval = *framePtr++; */
                    u4 rval = *framePtr;

                    bits >>= 1;
                    if (bits == 1) {
                        /* set bit 9 so we can tell when we're empty */
                        bits = *regVector++ | 0x0100;
                        LOGVV("loaded bits: 0x%02x\n", bits & 0xff);
                    }

                    if (rval != 0 && (bits & 0x01) != 0) {
                        /*
                         * Non-null, register marked as live reference.  This
                         * should always be a valid object.
                         */
#if WITH_EXTRA_GC_CHECKS > 0
                        if ((rval & 0x3) != 0 || !dvmIsValidObject((Object*) rval)) {
                            /* this is very bad */
                            LOGE("PGC: invalid ref in reg %d: 0x%08x\n",
                                method->registersSize-1 - i, rval);
                        } else
#endif
                        {

                            // LOGI("stack reference %u@%p", *framePtr, framePtr);
                            /* dvmMarkObjectNonNull((Object *)rval); */
                            scavengeReference((Object **) framePtr);
                        }
                    } else {
                        /*
                         * Null or non-reference, do nothing at all.
                         */
#if WITH_EXTRA_GC_CHECKS > 1
                        if (dvmIsValidObject((Object*) rval)) {
                            /* this is normal, but we feel chatty */
                            LOGD("PGC: ignoring valid ref in reg %d: 0x%08x\n",
                                 method->registersSize-1 - i, rval);
                        }
#endif
                    }
                    ++framePtr;
                }
                dvmReleaseRegisterMapLine(pMap, regVector);
            }
        }
        /* else this is a break frame and there is nothing to mark, or
         * this is a native method and the registers are just the "ins",
         * copied from various registers in the caller's set.
         */

#if WITH_EXTRA_GC_CHECKS > 1
        first = false;
#endif

        /* Don't fall into an infinite loop if things get corrupted.
         */
        assert((uintptr_t)saveArea->prevFrame > (uintptr_t)framePtr ||
               saveArea->prevFrame == NULL);
        framePtr = saveArea->prevFrame;
    }
}

static void scavengeThread(Thread *thread)
{
    assert(thread->status != THREAD_RUNNING ||
           thread->isSuspended ||
           thread == dvmThreadSelf());

    // LOGI("scavengeThread(thread=%p)", thread);

    // LOGI("Scavenging threadObj=%p", thread->threadObj);
    scavengeReference(&thread->threadObj);

    // LOGI("Scavenging exception=%p", thread->exception);
    scavengeReference(&thread->exception);

    scavengeThreadStack(thread);
}

static void scavengeThreadList(void)
{
    Thread *thread;

    dvmLockThreadList(dvmThreadSelf());
    thread = gDvm.threadList;
    while (thread) {
        scavengeThread(thread);
        thread = thread->next;
    }
    dvmUnlockThreadList();
}

static void verifyThreadStack(const Thread *thread)
{
    const u4 *framePtr;

    assert(thread != NULL);
    framePtr = (const u4 *)thread->curFrame;
    while (framePtr != NULL) {
        const StackSaveArea *saveArea;
        const Method *method;

        saveArea = SAVEAREA_FROM_FP(framePtr);
        method = saveArea->method;
        if (method != NULL && !dvmIsNativeMethod(method)) {
            const RegisterMap* pMap;
            const u1* regVector;
            int i;

            Method* nonConstMethod = (Method*) method;  // quiet gcc
            pMap = dvmGetExpandedRegisterMap(nonConstMethod);

            /* assert(pMap != NULL); */

            // LOGI("PGC: %s.%s\n", method->clazz->descriptor, method->name);

            if (pMap != NULL) {
                /* found map, get registers for this address */
                int addr = saveArea->xtra.currentPc - method->insns;
                regVector = dvmRegisterMapGetLine(pMap, addr);
                if (regVector == NULL) {
                    LOGI("PGC: map but no entry for %s.%s addr=0x%04x\n",
                         method->clazz->descriptor, method->name, addr);
                } else {
                    //LOGI("PGC: found map for %s.%s 0x%04x (t=%d)\n", method->clazz->descriptor, method->name, addr, thread->threadId);
                }
            } else {
                /*
                 * No map found.  If precise GC is disabled this is
                 * expected -- we don't create pointers to the map data even
                 * if it's present -- but if it's enabled it means we're
                 * unexpectedly falling back on a conservative scan, so it's
                 * worth yelling a little.
                 */
                if (gDvm.preciseGc) {
                    LOGI("PGC: no map for %s.%s\n",
                        method->clazz->descriptor, method->name);
                }
                regVector = NULL;
            }

            /* assert(regVector != NULL); */

            if (regVector == NULL) {
                /* conservative scan */
                for (i = method->registersSize - 1; i >= 0; i--) {
                    u4 rval = *framePtr++;
                    if (rval != 0 && (rval & 0x3) == 0) {
                        abort();
                        /* dvmMarkIfObject((Object *)rval); */
                    }
                }
            } else {
                /*
                 * Precise scan.  v0 is at the lowest address on the
                 * interpreted stack, and is the first bit in the register
                 * vector, so we can walk through the register map and
                 * memory in the same direction.
                 *
                 * A '1' bit indicates a live reference.
                 */
                u2 bits = 1 << 1;
                for (i = method->registersSize - 1; i >= 0; i--) {
                    u4 rval = *framePtr;

                    bits >>= 1;
                    if (bits == 1) {
                        /* set bit 9 so we can tell when we're empty */
                        bits = *regVector++ | 0x0100;
                        LOGVV("loaded bits: 0x%02x\n", bits & 0xff);
                    }

                    if (rval != 0 && (bits & 0x01) != 0) {
                        /*
                         * Non-null, register marked as live reference.  This
                         * should always be a valid object.
                         */
                        //LOGI("verify stack reference %p", (Object *)*framePtr);
                        verifyReference((Object *)*framePtr);
                    } else {
                        /*
                         * Null or non-reference, do nothing at all.
                         */
                    }
                    ++framePtr;
                }
                dvmReleaseRegisterMapLine(pMap, regVector);
            }
        }
        /* else this is a break frame and there is nothing to mark, or
         * this is a native method and the registers are just the "ins",
         * copied from various registers in the caller's set.
         */

        /* Don't fall into an infinite loop if things get corrupted.
         */
        assert((uintptr_t)saveArea->prevFrame > (uintptr_t)framePtr ||
               saveArea->prevFrame == NULL);
        framePtr = saveArea->prevFrame;
    }
}

static void verifyThread(const Thread *thread)
{
    assert(thread->status != THREAD_RUNNING ||
           thread->isSuspended ||
           thread == dvmThreadSelf());

    LOGI("verifyThread(thread=%p)", thread);

    LOGI("verify threadObj=%p", thread->threadObj);
    verifyReference(thread->threadObj);

    LOGI("verify exception=%p", thread->exception);
    verifyReference(thread->exception);

    LOGI("verify thread->internalLocalRefTable");
    verifyReferenceTable(&thread->internalLocalRefTable);

    LOGI("verify thread->jniLocalRefTable");
    verifyReferenceTable(&thread->jniLocalRefTable);

    /* Can the check be pushed into the promote routine? */
    if (thread->jniMonitorRefTable.table) {
        LOGI("verify thread->jniMonitorRefTable");
        verifyReferenceTable(&thread->jniMonitorRefTable);
    }

    verifyThreadStack(thread);
}

static void verifyThreadList(void)
{
    Thread *thread;

    dvmLockThreadList(dvmThreadSelf());
    thread = gDvm.threadList;
    while (thread) {
        verifyThread(thread);
        thread = thread->next;
    }
    dvmUnlockThreadList();
}

static void pinThread(Thread *thread)
{
    assert(thread != NULL);
    assert(thread->status != THREAD_RUNNING ||
           thread->isSuspended ||
           thread == dvmThreadSelf());
    LOGI("pinThread(thread=%p)", thread);

    LOGI("Pin internalLocalRefTable");
    pinReferenceTable(&thread->internalLocalRefTable);

    LOGI("Pin jniLocalRefTable");
    pinReferenceTable(&thread->jniLocalRefTable);

    /* Can the check be pushed into the promote routine? */
    if (thread->jniMonitorRefTable.table) {
        LOGI("Pin jniMonitorRefTable");
        pinReferenceTable(&thread->jniMonitorRefTable);
    }
}

static void pinThreadList(void)
{
    Thread *thread;

    dvmLockThreadList(dvmThreadSelf());
    thread = gDvm.threadList;
    while (thread) {
        pinThread(thread);
        thread = thread->next;
    }
    dvmUnlockThreadList();
}

/*
 * Heap block scavenging.
 */

/*
 * Scavenge objects in the current block.  Scavenging terminates when
 * the pointer reaches the highest address in the block or when a run
 * of zero words that continues to the highest address is reached.
 */
static void scavengeBlock(HeapSource *heapSource, size_t block)
{
    u1 *cursor;
    u1 *end;
    size_t size;

    LOG_SCAVENGE("scavengeBlock(heapSource=%p,block=%zu)", heapSource, block);

    assert(heapSource != NULL);
    assert(block < heapSource->totalBlocks);
    assert(heapSource->blockSpace[block] == BLOCK_TO_SPACE);

    cursor = blockToAddress(heapSource, block);
    end = cursor + BLOCK_SIZE;
    LOG_SCAVENGE("scavengeBlock start=%p, end=%p", cursor, end);

    /* Parse and scavenge the current block. */
    size = 0;
    while (cursor < end) {
        u4 word = *(u4 *)cursor;
        if (word != 0) {
            size = scavengeObject((Object *)cursor);
            size = alignUp(size, ALLOC_ALIGNMENT);
            cursor += size;
        } else if (word == 0 && cursor == (u1 *)gDvm.unlinkedJavaLangClass) {
            size = sizeof(ClassObject);
            cursor += size;
        } else {
            /* Check for padding. */
            while (*(u4 *)cursor == 0) {
                cursor += 4;
                if (cursor == end) break;
            }
            /* Punt if something went wrong. */
            assert(cursor == end);
        }
    }
}

static size_t objectSize(Object *obj)
{
    size_t size;

    if (obj->clazz == gDvm.classJavaLangClass ||
        obj->clazz == gDvm.unlinkedJavaLangClass) {
        size = dvmClassObjectSize((ClassObject *)obj);
    } else if (IS_CLASS_FLAG_SET(obj->clazz, CLASS_ISARRAY)) {
        size = dvmArrayObjectSize((ArrayObject *)obj);
    } else {
        size = obj->clazz->objectSize;
    }
    return size;
}

static void verifyBlock(HeapSource *heapSource, size_t block)
{
    u1 *cursor;
    u1 *end;
    size_t size;

    // LOGI("verifyBlock(heapSource=%p,block=%zu)", heapSource, block);

    assert(heapSource != NULL);
    assert(block < heapSource->totalBlocks);
    assert(heapSource->blockSpace[block] == BLOCK_TO_SPACE);

    cursor = blockToAddress(heapSource, block);
    end = cursor + BLOCK_SIZE;
    // LOGI("verifyBlock start=%p, end=%p", cursor, end);

    /* Parse and scavenge the current block. */
    size = 0;
    while (cursor < end) {
        u4 word = *(u4 *)cursor;
        if (word != 0) {
            dvmVerifyObject((Object *)cursor);
            size = objectSize((Object *)cursor);
            size = alignUp(size, ALLOC_ALIGNMENT);
            cursor += size;
        } else if (word == 0 && cursor == (u1 *)gDvm.unlinkedJavaLangClass) {
            size = sizeof(ClassObject);
            cursor += size;
        } else {
            /* Check for padding. */
            while (*(unsigned long *)cursor == 0) {
                cursor += 4;
                if (cursor == end) break;
            }
            /* Punt if something went wrong. */
            assert(cursor == end);
        }
    }
}

static void describeBlockQueue(const HeapSource *heapSource)
{
    size_t block, count;
    char space;

    block = heapSource->queueHead;
    count = 0;
    LOG_SCAVENGE(">>> describeBlockQueue(heapSource=%p)", heapSource);
    /* Count the number of blocks enqueued. */
    while (block != QUEUE_TAIL) {
        block = heapSource->blockQueue[block];
        ++count;
    }
    LOG_SCAVENGE("blockQueue %zu elements, enqueued %zu",
                 count, heapSource->queueSize);
    block = heapSource->queueHead;
    while (block != QUEUE_TAIL) {
        space = heapSource->blockSpace[block];
        LOG_SCAVENGE("block=%zu@%p,space=%zu", block, blockToAddress(heapSource,block), space);
        block = heapSource->blockQueue[block];
    }

    LOG_SCAVENGE("<<< describeBlockQueue(heapSource=%p)", heapSource);
}

/*
 * Blackens promoted objects.
 */
static void scavengeBlockQueue(void)
{
    HeapSource *heapSource;
    size_t block;

    LOG_SCAVENGE(">>> scavengeBlockQueue()");
    heapSource = gDvm.gcHeap->heapSource;
    describeBlockQueue(heapSource);
    while (heapSource->queueHead != QUEUE_TAIL) {
        block = heapSource->queueHead;
        LOG_SCAVENGE("Dequeueing block %zu\n", block);
        scavengeBlock(heapSource, block);
        heapSource->queueHead = heapSource->blockQueue[block];
        LOGI("New queue head is %zu\n", heapSource->queueHead);
    }
    LOG_SCAVENGE("<<< scavengeBlockQueue()");
}

/*
 * Scan the block list and verify all blocks that are marked as being
 * in new space.  This should be parametrized so we can invoke this
 * routine outside of the context of a collection.
 */
static void verifyNewSpace(void)
{
    HeapSource *heapSource;
    size_t i;
    size_t c0, c1, c2, c7;

    c0 = c1 = c2 = c7 = 0;
    heapSource = gDvm.gcHeap->heapSource;
    for (i = 0; i < heapSource->totalBlocks; ++i) {
        switch (heapSource->blockSpace[i]) {
        case BLOCK_FREE: ++c0; break;
        case BLOCK_TO_SPACE: ++c1; break;
        case BLOCK_FROM_SPACE: ++c2; break;
        case BLOCK_CONTINUED: ++c7; break;
        default: assert(!"reached");
        }
    }
    LOG_VERIFY("Block Demographics: "
               "Free=%zu,ToSpace=%zu,FromSpace=%zu,Continued=%zu",
               c0, c1, c2, c7);
    for (i = 0; i < heapSource->totalBlocks; ++i) {
        if (heapSource->blockSpace[i] != BLOCK_TO_SPACE) {
            continue;
        }
        verifyBlock(heapSource, i);
    }
}

static void scavengeGlobals(void)
{
    scavengeReference((Object **)(void *)&gDvm.classJavaLangClass);
    scavengeReference((Object **)(void *)&gDvm.classJavaLangClassArray);
    scavengeReference((Object **)(void *)&gDvm.classJavaLangError);
    scavengeReference((Object **)(void *)&gDvm.classJavaLangObject);
    scavengeReference((Object **)(void *)&gDvm.classJavaLangObjectArray);
    scavengeReference((Object **)(void *)&gDvm.classJavaLangRuntimeException);
    scavengeReference((Object **)(void *)&gDvm.classJavaLangString);
    scavengeReference((Object **)(void *)&gDvm.classJavaLangThread);
    scavengeReference((Object **)(void *)&gDvm.classJavaLangVMThread);
    scavengeReference((Object **)(void *)&gDvm.classJavaLangThreadGroup);
    scavengeReference((Object **)(void *)&gDvm.classJavaLangThrowable);
    scavengeReference((Object **)(void *)&gDvm.classJavaLangStackTraceElement);
    scavengeReference((Object **)(void *)&gDvm.classJavaLangStackTraceElementArray);
    scavengeReference((Object **)(void *)&gDvm.classJavaLangAnnotationAnnotationArray);
    scavengeReference((Object **)(void *)&gDvm.classJavaLangAnnotationAnnotationArrayArray);
    scavengeReference((Object **)(void *)&gDvm.classJavaLangReflectAccessibleObject);
    scavengeReference((Object **)(void *)&gDvm.classJavaLangReflectConstructor);
    scavengeReference((Object **)(void *)&gDvm.classJavaLangReflectConstructorArray);
    scavengeReference((Object **)(void *)&gDvm.classJavaLangReflectField);
    scavengeReference((Object **)(void *)&gDvm.classJavaLangReflectFieldArray);
    scavengeReference((Object **)(void *)&gDvm.classJavaLangReflectMethod);
    scavengeReference((Object **)(void *)&gDvm.classJavaLangReflectMethodArray);
    scavengeReference((Object **)(void *)&gDvm.classJavaLangReflectProxy);
    scavengeReference((Object **)(void *)&gDvm.classJavaLangExceptionInInitializerError);
    scavengeReference((Object **)(void *)&gDvm.classJavaLangRefReference);
    scavengeReference((Object **)(void *)&gDvm.classJavaNioReadWriteDirectByteBuffer);
    scavengeReference((Object **)(void *)&gDvm.classJavaSecurityAccessController);
    scavengeReference((Object **)(void *)&gDvm.classOrgApacheHarmonyLangAnnotationAnnotationFactory);
    scavengeReference((Object **)(void *)&gDvm.classOrgApacheHarmonyLangAnnotationAnnotationMember);
    scavengeReference((Object **)(void *)&gDvm.classOrgApacheHarmonyLangAnnotationAnnotationMemberArray);
    scavengeReference((Object **)(void *)&gDvm.classOrgApacheHarmonyNioInternalDirectBuffer);
    scavengeReference((Object **)(void *)&gDvm.classArrayBoolean);
    scavengeReference((Object **)(void *)&gDvm.classArrayChar);
    scavengeReference((Object **)(void *)&gDvm.classArrayFloat);
    scavengeReference((Object **)(void *)&gDvm.classArrayDouble);
    scavengeReference((Object **)(void *)&gDvm.classArrayByte);
    scavengeReference((Object **)(void *)&gDvm.classArrayShort);
    scavengeReference((Object **)(void *)&gDvm.classArrayInt);
    scavengeReference((Object **)(void *)&gDvm.classArrayLong);
}

void describeHeap(void)
{
    HeapSource *heapSource;

    heapSource = gDvm.gcHeap->heapSource;
    describeBlocks(heapSource);
}

/*
 * The collection interface.  Collection has a few distinct phases.
 * The first is flipping AKA condemning AKA whitening the heap.  The
 * second is to promote all objects which are pointed to by pinned or
 * ambiguous references.  The third phase is tracing from the stacks,
 * registers and various globals.  Lastly, a verification of the heap
 * is performed.  The last phase should be optional.
 */
void dvmScavengeRoots(void)  /* Needs a new name badly */
{
    HeapRefTable *refs;
    GcHeap *gcHeap;

    {
        size_t alloc, unused, total;

        room(&alloc, &unused, &total);
        LOGI("BEFORE GC: %zu alloc, %zu free, %zu total.",
             alloc, unused, total);
    }

    gcHeap = gDvm.gcHeap;
    dvmHeapSourceFlip();

    /*
     * Promote blocks with stationary objects.
     */

    // LOGI("Pinning gDvm.threadList");
    pinThreadList();

    // LOGI("Pinning gDvm.jniGlobalRefTable");
    pinReferenceTable(&gDvm.jniGlobalRefTable);

    // LOGI("Pinning gDvm.jniPinRefTable");
    pinReferenceTable(&gDvm.jniPinRefTable);

    // LOGI("Pinning gDvm.gcHeap->nonCollectableRefs");
    pinReferenceTable(&gcHeap->nonCollectableRefs);

    // LOGI("Pinning gDvm.loadedClasses");
    pinHashTableEntries(gDvm.loadedClasses);

    // LOGI("Pinning gDvm.primitiveClass");
    pinPrimitiveClasses();

    // LOGI("Pinning gDvm.internedStrings");
    pinInternedStrings();

    // describeBlocks(gcHeap->heapSource);

    /*
     * Create first, open new-space page right here.
     */

    /* Reset allocation to an unallocated block. */
    gDvm.gcHeap->heapSource->allocPtr = allocateBlocks(gDvm.gcHeap->heapSource, 1);
    gDvm.gcHeap->heapSource->allocLimit = gDvm.gcHeap->heapSource->allocPtr + BLOCK_SIZE;
    /*
     * Hack: promote the empty block allocated above.  If the
     * promotions that occurred above did not actually gray any
     * objects, the block queue may be empty.  We must force a
     * promotion to be safe.
     */
    promoteBlockByAddr(gDvm.gcHeap->heapSource, gDvm.gcHeap->heapSource->allocPtr);

    /*
     * Scavenge blocks and relocate movable objects.
     */

    LOGI("Scavenging gDvm.threadList");
    scavengeThreadList();

    LOGI("Scavenging gDvm.gcHeap->referenceOperations");
    scavengeLargeHeapRefTable(gcHeap->referenceOperations);

    LOGI("Scavenging gDvm.gcHeap->pendingFinalizationRefs");
    scavengeLargeHeapRefTable(gcHeap->pendingFinalizationRefs);

    LOGI("Scavenging random global stuff");
    scavengeReference(&gDvm.outOfMemoryObj);
    scavengeReference(&gDvm.internalErrorObj);
    scavengeReference(&gDvm.noClassDefFoundErrorObj);

    LOGI("Scavenging gDvm.dbgRegistry");
    scavengeHashTable(gDvm.dbgRegistry);

    // LOGI("Scavenging gDvm.internedString");
    scavengeInternedStrings();

    LOGI("Root scavenge has completed.");

    scavengeBlockQueue();

    LOGI("Re-snap global class pointers.");
    scavengeGlobals();

    LOGI("New space scavenge has completed.");

    /*
     * Verify the stack and heap.
     */

    // LOGI("Validating new space.");

    verifyInternedStrings();

    verifyThreadList();

    verifyNewSpace();

    // LOGI("New space verify has completed.");

    //describeBlocks(gcHeap->heapSource);

    clearFromSpace(gcHeap->heapSource);

    {
        size_t alloc, rem, total;

        room(&alloc, &rem, &total);
        LOGI("AFTER GC: %zu alloc, %zu free, %zu total.", alloc, rem, total);
    }
}

/*
 * Interface compatibility routines.
 */

void dvmClearWhiteRefs(Object **list)
{
    /* TODO */
    assert(*list == NULL);
}

void dvmHandleSoftRefs(Object **list)
{
    /* TODO */
    assert(*list == NULL);
}

bool dvmHeapBeginMarkStep(GcMode mode)
{
    /* do nothing */
    return true;
}

void dvmHeapFinishMarkStep(void)
{
    /* do nothing */
}

void dvmHeapMarkRootSet(void)
{
    /* do nothing */
}

void dvmHeapScanMarkedObjects(void)
{
    dvmScavengeRoots();
}

void dvmHeapScheduleFinalizations(void)
{
    /* do nothing */
}

void dvmHeapSweepUnmarkedObjects(GcMode mode, int *numFreed, size_t *sizeFreed)
{
    /* do nothing */
}

void dvmMarkObjectNonNull(const Object *obj)
{
    assert(!"implemented");
}
