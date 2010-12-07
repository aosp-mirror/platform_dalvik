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

#include "Dalvik.h"
#include "HeapBitmap.h"
#include "clz.h"
#include <sys/mman.h>   /* for PROT_* */

/*
 * Initialize a HeapBitmap so that it points to a bitmap large
 * enough to cover a heap at <base> of <maxSize> bytes, where
 * objects are guaranteed to be HB_OBJECT_ALIGNMENT-aligned.
 */
bool
dvmHeapBitmapInit(HeapBitmap *hb, const void *base, size_t maxSize,
        const char *name)
{
    void *bits;
    size_t bitsLen;

    assert(hb != NULL);
    assert(name != NULL);
    bitsLen = HB_OFFSET_TO_INDEX(maxSize) * sizeof(*hb->bits);
    bits = dvmAllocRegion(bitsLen, PROT_READ | PROT_WRITE, name);
    if (bits == NULL) {
        LOGE("Could not mmap %zd-byte ashmem region '%s'", bitsLen, name);
        return false;
    }
    hb->bits = (unsigned long *)bits;
    hb->bitsLen = hb->allocLen = bitsLen;
    hb->base = (uintptr_t)base;
    hb->max = hb->base - 1;
    return true;
}

/*
 * Clean up any resources associated with the bitmap.
 */
void
dvmHeapBitmapDelete(HeapBitmap *hb)
{
    assert(hb != NULL);

    if (hb->bits != NULL) {
        munmap((char *)hb->bits, hb->allocLen);
    }
    memset(hb, 0, sizeof(*hb));
}

/*
 * Fill the bitmap with zeroes.  Returns the bitmap's memory to
 * the system as a side-effect.
 */
void
dvmHeapBitmapZero(HeapBitmap *hb)
{
    assert(hb != NULL);

    if (hb->bits != NULL) {
        /* This returns the memory to the system.
         * Successive page faults will return zeroed memory.
         */
        madvise(hb->bits, hb->bitsLen, MADV_DONTNEED);
        hb->max = hb->base - 1;
    }
}

/*
 * Walk through the bitmaps in increasing address order, and find the
 * object pointers that correspond to garbage objects.  Call
 * <callback> zero or more times with lists of these object pointers.
 *
 * The callback is not permitted to increase the max of either bitmap.
 */
void dvmHeapBitmapSweepWalk(const HeapBitmap *liveHb, const HeapBitmap *markHb,
                            BitmapSweepCallback *callback, void *callbackArg)
{
    static const size_t kPointerBufSize = 128;
    void *pointerBuf[kPointerBufSize];
    void **pb = pointerBuf;
    size_t index;
    size_t i;
    unsigned long *live, *mark;
    uintptr_t offset;

    assert(liveHb != NULL);
    assert(liveHb->bits != NULL);
    assert(markHb != NULL);
    assert(markHb->bits != NULL);
    assert(liveHb->base == markHb->base);
    assert(liveHb->bitsLen == markHb->bitsLen);
    assert(callback != NULL);
    if (liveHb->max < liveHb->base) {
        /* Easy case; both are obviously empty.
         */
        return;
    }
    offset = liveHb->max - liveHb->base;
    index = HB_OFFSET_TO_INDEX(offset);
    live = liveHb->bits;
    mark = markHb->bits;
    for (i = 0; i <= index; i++) {
        unsigned long garbage = live[i] & ~mark[i];
        if (UNLIKELY(garbage != 0)) {
            unsigned long highBit = 1 << (HB_BITS_PER_WORD - 1);
            uintptr_t ptrBase = HB_INDEX_TO_OFFSET(i) + liveHb->base;
            while (garbage != 0) {
                int shift = CLZ(garbage);
                garbage &= ~(highBit >> shift);
                *pb++ = (void *)(ptrBase + shift * HB_OBJECT_ALIGNMENT);
            }
            /* Make sure that there are always enough slots available */
            /* for an entire word of 1s. */
            if (kPointerBufSize - (pb - pointerBuf) < HB_BITS_PER_WORD) {
                (*callback)(pb - pointerBuf, pointerBuf, callbackArg);
                pb = pointerBuf;
            }
        }
    }
    if (pb > pointerBuf) {
        (*callback)(pb - pointerBuf, pointerBuf, callbackArg);
    }
}
