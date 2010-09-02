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
    hb->bits = bits;
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
 * The callback is permitted to increase the bitmap's max; the walk
 * will use the updated max as a terminating condition,
 */
void dvmHeapBitmapSweepWalk(const HeapBitmap *liveHb, const HeapBitmap *markHb,
                            BitmapSweepCallback *callback, void *callbackArg)
{
    static const size_t kPointerBufSize = 128;
    void *pointerBuf[kPointerBufSize];
    void **pb = pointerBuf;
    size_t index;
    size_t i;

#define FLUSH_POINTERBUF() \
    do { \
        (*callback)(pb - pointerBuf, (void **)pointerBuf, \
                    callbackArg); \
        pb = pointerBuf; \
    } while (false)

#define DECODE_BITS(hb_, bits_, update_index_) \
    do { \
        if (UNLIKELY(bits_ != 0)) { \
            static const unsigned long kHighBit = \
                    (unsigned long)1 << (HB_BITS_PER_WORD - 1); \
            const uintptr_t ptrBase = HB_INDEX_TO_OFFSET(i) + hb_->base; \
/*TODO: hold onto ptrBase so we can shrink max later if possible */ \
/*TODO: see if this is likely or unlikely */ \
            while (bits_ != 0) { \
                const int rshift = CLZ(bits_); \
                bits_ &= ~(kHighBit >> rshift); \
                *pb++ = (void *)(ptrBase + rshift * HB_OBJECT_ALIGNMENT); \
            } \
            /* Make sure that there are always enough slots available */ \
            /* for an entire word of 1s. */ \
            if (kPointerBufSize - (pb - pointerBuf) < HB_BITS_PER_WORD) { \
                FLUSH_POINTERBUF(); \
                if (update_index_) { \
                    /* The callback may have caused hb_->max to grow. */ \
                    index = HB_OFFSET_TO_INDEX(hb_->max - hb_->base); \
                } \
            } \
        } \
    } while (false)

    assert(liveHb != NULL);
    assert(liveHb->bits != NULL);
    assert(markHb != NULL);
    assert(markHb->bits != NULL);
    assert(callback != NULL);

    if (liveHb->base != markHb->base) {
        LOGW("dvmHeapBitmapSweepWalk: bitmaps cover different heaps (%zd != %zd)",
             liveHb->base, markHb->base);
        return;
    }
    if (liveHb->bitsLen != markHb->bitsLen) {
        LOGW("dvmHeapBitmapSweepWalk: size of bitmaps differ (%zd != %zd)",
             liveHb->bitsLen, markHb->bitsLen);
        return;
    }
    if (liveHb->max < liveHb->base && markHb->max < markHb->base) {
        /* Easy case; both are obviously empty.
         */
        return;
    }

    /* First, walk along the section of the bitmaps that may be the same.
     */
    if (liveHb->max >= liveHb->base && markHb->max >= markHb->base) {
        unsigned long *live, *mark;
        uintptr_t offset;

        offset = ((liveHb->max < markHb->max) ? liveHb->max : markHb->max) - liveHb->base;
//TODO: keep track of which (and whether) one is longer for later
        index = HB_OFFSET_TO_INDEX(offset);

        live = liveHb->bits;
        mark = markHb->bits;
        for (i = 0; i <= index; i++) {
//TODO: unroll this. pile up a few in locals?
            unsigned long garbage = live[i] & ~mark[i];
            DECODE_BITS(liveHb, garbage, false);
//BUG: if the callback was called, either max could have changed.
        }
        /* The next index to look at.
         */
        index++;
    } else {
        /* One of the bitmaps is empty.
         */
        index = 0;
    }

    /* If one bitmap's max is larger, walk through the rest of the
     * set bits.
     */
const HeapBitmap *longHb;
unsigned long *p;
//TODO: may be the same size, in which case this is wasted work
    longHb = (liveHb->max > markHb->max) ? liveHb : markHb;
    i = index;
    index = HB_OFFSET_TO_INDEX(longHb->max - longHb->base);
    p = longHb->bits + i;
    for (/* i = i */; i <= index; i++) {
//TODO: unroll this
        unsigned long bits = *p++;
        DECODE_BITS(longHb, bits, true);
    }

    if (pb > pointerBuf) {
        FLUSH_POINTERBUF();
    }
#undef FLUSH_POINTERBUF
#undef DECODE_BITS
}
