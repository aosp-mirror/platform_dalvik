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
#include <limits.h>     // for ULONG_MAX
#include <sys/mman.h>   // for madvise(), mmap()

#define LIKELY(exp)     (__builtin_expect((exp) != 0, true))
#define UNLIKELY(exp)   (__builtin_expect((exp) != 0, false))

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
 * object pointers that correspond to places where the bitmaps differ.
 * Call <callback> zero or more times with lists of these object pointers.
 *
 * The <finger> argument to the callback indicates the next-highest
 * address that hasn't been visited yet; setting bits for objects whose
 * addresses are less than <finger> are not guaranteed to be seen by
 * the current XorWalk.  <finger> will be set to ULONG_MAX when the
 * end of the bitmap is reached.
 */
bool
dvmHeapBitmapXorWalk(const HeapBitmap *hb1, const HeapBitmap *hb2,
        bool (*callback)(size_t numPtrs, void **ptrs,
                         const void *finger, void *arg),
        void *callbackArg)
{
    static const size_t kPointerBufSize = 128;
    void *pointerBuf[kPointerBufSize];
    void **pb = pointerBuf;
    size_t index;
    size_t i;

#define FLUSH_POINTERBUF(finger_) \
    do { \
        if (!callback(pb - pointerBuf, (void **)pointerBuf, \
                (void *)(finger_), callbackArg)) \
        { \
            LOGW("dvmHeapBitmapXorWalk: callback failed\n"); \
            return false; \
        } \
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
                FLUSH_POINTERBUF(ptrBase + \
                        HB_BITS_PER_WORD * HB_OBJECT_ALIGNMENT); \
                if (update_index_) { \
                    /* The callback may have caused hb_->max to grow. */ \
                    index = HB_OFFSET_TO_INDEX(hb_->max - hb_->base); \
                } \
            } \
        } \
    } while (false)

    assert(hb1 != NULL);
    assert(hb1->bits != NULL);
    assert(hb2 != NULL);
    assert(hb2->bits != NULL);
    assert(callback != NULL);

    if (hb1->base != hb2->base) {
        LOGW("dvmHeapBitmapXorWalk: bitmaps cover different heaps "
                "(0x%08x != 0x%08x)\n",
                (uintptr_t)hb1->base, (uintptr_t)hb2->base);
        return false;
    }
    if (hb1->bitsLen != hb2->bitsLen) {
        LOGW("dvmHeapBitmapXorWalk: size of bitmaps differ (%zd != %zd)\n",
                hb1->bitsLen, hb2->bitsLen);
        return false;
    }
    if (hb1->max < hb1->base && hb2->max < hb2->base) {
        /* Easy case; both are obviously empty.
         */
        return true;
    }

    /* First, walk along the section of the bitmaps that may be the same.
     */
    if (hb1->max >= hb1->base && hb2->max >= hb2->base) {
        unsigned long *p1, *p2;
        uintptr_t offset;

        offset = ((hb1->max < hb2->max) ? hb1->max : hb2->max) - hb1->base;
//TODO: keep track of which (and whether) one is longer for later
        index = HB_OFFSET_TO_INDEX(offset);

        p1 = hb1->bits;
        p2 = hb2->bits;
        for (i = 0; i <= index; i++) {
//TODO: unroll this. pile up a few in locals?
            unsigned long diff = *p1++ ^ *p2++;
            DECODE_BITS(hb1, diff, false);
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
    longHb = (hb1->max > hb2->max) ? hb1 : hb2;
    i = index;
    index = HB_OFFSET_TO_INDEX(longHb->max - longHb->base);
    p = longHb->bits + i;
    for (/* i = i */; i <= index; i++) {
//TODO: unroll this
        unsigned long bits = *p++;
        DECODE_BITS(longHb, bits, true);
    }

    if (pb > pointerBuf) {
        /* Set the finger to the end of the heap (rather than
         * longHb->max) so that the callback doesn't expect to be
         * called again if it happens to change the current max.
         */
        uintptr_t finalFinger = longHb->base + HB_MAX_OFFSET(longHb);
        FLUSH_POINTERBUF(finalFinger);
        assert(finalFinger > longHb->max);
    }

    return true;

#undef FLUSH_POINTERBUF
#undef DECODE_BITS
}

/*
 * Similar to dvmHeapBitmapXorWalk(), but visit the set bits
 * in a single bitmap.
 */
bool
dvmHeapBitmapWalk(const HeapBitmap *hb,
        bool (*callback)(size_t numPtrs, void **ptrs,
                         const void *finger, void *arg),
        void *callbackArg)
{
    /* Create an empty bitmap with the same extent as <hb>.
     * Don't actually allocate any memory.
     */
    HeapBitmap emptyHb = *hb;
    emptyHb.max = emptyHb.base - 1; // empty
    emptyHb.bits = (void *)1;       // non-NULL but intentionally bad

    return dvmHeapBitmapXorWalk(hb, &emptyHb, callback, callbackArg);
}
