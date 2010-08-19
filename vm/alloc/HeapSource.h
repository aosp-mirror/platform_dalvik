/*
 * Copyright (C) 2008 The Android Open Source Project
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
#ifndef _DALVIK_HEAP_SOURCE
#define _DALVIK_HEAP_SOURCE

#include "alloc/Heap.h"
#include "alloc/HeapInternal.h" // for GcHeap

/* dlmalloc uses one size_t per allocated chunk.
 */
#define HEAP_SOURCE_CHUNK_OVERHEAD         (1 * sizeof (size_t))
#define HEAP_SOURCE_WORST_CHUNK_OVERHEAD   (32 * sizeof (size_t))

/* The largest number of separate heaps we can handle.
 */
#define HEAP_SOURCE_MAX_HEAP_COUNT 2

/*
 * Initializes the heap source; must be called before any other
 * dvmHeapSource*() functions.
 */
GcHeap *dvmHeapSourceStartup(size_t startSize, size_t absoluteMaxSize);

/*
 * If the HeapSource was created while in zygote mode, this
 * will create a new heap for post-zygote allocations.
 * Having a separate heap should maximize the number of pages
 * that a given app_process shares with the zygote process.
 */
bool dvmHeapSourceStartupAfterZygote(void);

/*
 * If the HeapSource was created while in zygote mode, this
 * will create an additional zygote heap before the first fork().
 * Having a separate heap should reduce the number of shared
 * pages subsequently touched by the zygote process.
 */
bool dvmHeapSourceStartupBeforeFork(void);

/*
 * Shutdown any threads internal to the heap source.  This should be
 * called before the heap source itself is shutdown.
 */
void dvmHeapSourceThreadShutdown(void);

/*
 * Tears down the heap source and frees any resources associated with it.
 */
void dvmHeapSourceShutdown(GcHeap **gcHeap);

/*
 * Initializes a vector of object and mark bits to the object and mark
 * bits of each heap.
 */
void dvmHeapSourceGetObjectBitmaps(HeapBitmap liveBits[], HeapBitmap markBits[],
                                   size_t numHeaps);

/*
 * Get the bitmap representing all live objects.
 */
HeapBitmap *dvmHeapSourceGetLiveBits(void);

/*
 * Gets the begining of the allocation for the HeapSource.
 */
void *dvmHeapSourceGetBase(void);

/*
 * Returns the requested value. If the per-heap stats are requested, fill
 * them as well.
 */
enum HeapSourceValueSpec {
    HS_FOOTPRINT,
    HS_ALLOWED_FOOTPRINT,
    HS_BYTES_ALLOCATED,
    HS_OBJECTS_ALLOCATED,
    HS_EXTERNAL_BYTES_ALLOCATED,
    HS_EXTERNAL_LIMIT
};
size_t dvmHeapSourceGetValue(enum HeapSourceValueSpec spec,
                             size_t perHeapStats[], size_t arrayLen);

/*
 * Allocates <n> bytes of zeroed data.
 */
void *dvmHeapSourceAlloc(size_t n);

/*
 * Allocates <n> bytes of zeroed data, growing up to absoluteMaxSize
 * if necessary.
 */
void *dvmHeapSourceAllocAndGrow(size_t n);

/*
 * Frees the first numPtrs objects in the ptrs list and returns the
 * amount of reclaimed storage.  The list must contain addresses all
 * in the same mspace, and must be in increasing order. This implies
 * that there are no duplicates, and no entries are NULL.
 */
size_t dvmHeapSourceFreeList(size_t numPtrs, void **ptrs);

/*
 * Returns true iff <ptr> was allocated from the heap source.
 */
bool dvmHeapSourceContains(const void *ptr);

/*
 * Returns true iff <ptr> is within the address space managed by heap source.
 */
bool dvmHeapSourceContainsAddress(const void *ptr);

/*
 * Returns the value of the requested flag.
 */
enum HeapSourcePtrFlag {
    HS_CONTAINS,    // identical to dvmHeapSourceContains()
    HS_ALLOCATED_IN_ZYGOTE
};
bool dvmHeapSourceGetPtrFlag(const void *ptr, enum HeapSourcePtrFlag flag);

/*
 * Returns the number of usable bytes in an allocated chunk; the size
 * may be larger than the size passed to dvmHeapSourceAlloc().
 */
size_t dvmHeapSourceChunkSize(const void *ptr);

/*
 * Returns the number of bytes that the heap source has allocated
 * from the system using sbrk/mmap, etc.
 */
size_t dvmHeapSourceFootprint(void);

/*
 * Gets the maximum number of bytes that the heap source is allowed
 * to allocate from the system.
 */
size_t dvmHeapSourceGetIdealFootprint(void);

/*
 * Given the current contents of the heap, increase the allowed
 * heap footprint to match the target utilization ratio.  This
 * should only be called immediately after a full mark/sweep.
 */
void dvmHeapSourceGrowForUtilization(void);

/*
 * Return unused memory to the system if possible.  If <bytesTrimmed>
 * is non-NULL, the number of bytes returned to the system is written to it.
 */
void dvmHeapSourceTrim(size_t bytesTrimmed[], size_t arrayLen);

/*
 * Walks over the heap source and passes every allocated and
 * free chunk to the callback.
 */
void dvmHeapSourceWalk(void(*callback)(const void *chunkptr, size_t chunklen,
                                      const void *userptr, size_t userlen,
                                      void *arg),
                       void *arg);
/*
 * Gets the number of heaps available in the heap source.
 */
size_t dvmHeapSourceGetNumHeaps(void);

/*
 * Exchanges the mark and object bitmaps.
 */
void dvmHeapSourceSwapBitmaps(void);

/*
 * Zeroes the mark bitmap.
 */
void dvmHeapSourceZeroMarkBitmap(void);

/*
 * Marks all objects inside the immune region of the heap. Addresses
 * at or above this pointer are threatened, addresses below this
 * pointer are immune.
 */
void dvmMarkImmuneObjects(const char *immuneLimit);

/*
 * Returns a pointer that demarcates the threatened region of the
 * heap.  Addresses at or above this pointer are threatened, addresses
 * below this pointer are immune.
 */
void *dvmHeapSourceGetImmuneLimit(GcMode mode);

#endif  // _DALVIK_HEAP_SOURCE
