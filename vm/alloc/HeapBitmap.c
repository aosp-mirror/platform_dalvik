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
#include <cutils/ashmem.h>

#define HB_ASHMEM_NAME "dalvik-heap-bitmap"

#define ALIGN_UP_TO_PAGE_SIZE(p) \
    (((size_t)(p) + (SYSTEM_PAGE_SIZE - 1)) & ~(SYSTEM_PAGE_SIZE - 1))

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
    size_t allocLen;
    int fd;
    char nameBuf[ASHMEM_NAME_LEN] = HB_ASHMEM_NAME;

    assert(hb != NULL);

    bitsLen = HB_OFFSET_TO_INDEX(maxSize) * sizeof(*hb->bits);
    allocLen = ALIGN_UP_TO_PAGE_SIZE(bitsLen);   // required by ashmem

    if (name != NULL) {
        snprintf(nameBuf, sizeof(nameBuf), HB_ASHMEM_NAME "/%s", name);
    }
    fd = ashmem_create_region(nameBuf, allocLen);
    if (fd < 0) {
        LOGE("Could not create %zu-byte ashmem region \"%s\" to cover "
                "%zu-byte heap (%d)\n",
                allocLen, nameBuf, maxSize, fd);
        return false;
    }

    bits = mmap(NULL, bitsLen, PROT_READ | PROT_WRITE, MAP_PRIVATE, fd, 0);
    close(fd);
    if (bits == MAP_FAILED) {
        LOGE("Could not mmap %d-byte ashmem region \"%s\"\n",
                bitsLen, nameBuf);
        return false;
    }

    memset(hb, 0, sizeof(*hb));
    hb->bits = bits;
    hb->bitsLen = bitsLen;
    hb->base = (uintptr_t)base;
    hb->max = hb->base - 1;

    return true;
}

/*
 * Initialize <hb> so that it covers the same extent as <templateBitmap>.
 */
bool
dvmHeapBitmapInitFromTemplate(HeapBitmap *hb, const HeapBitmap *templateBitmap,
        const char *name)
{
    return dvmHeapBitmapInit(hb,
            (void *)templateBitmap->base, HB_MAX_OFFSET(templateBitmap), name);
}

/*
 * Initialize the bitmaps in <out> so that they cover the same extent as
 * the corresponding bitmaps in <templates>.
 */
bool
dvmHeapBitmapInitListFromTemplates(HeapBitmap out[], HeapBitmap templates[],
    size_t numBitmaps, const char *name)
{
    size_t i;
    char fullName[PATH_MAX];

    fullName[sizeof(fullName)-1] = '\0';
    for (i = 0; i < numBitmaps; i++) {
        bool ok;

        /* If two ashmem regions have the same name, only one gets
         * the name when looking at the maps.
         */
        snprintf(fullName, sizeof(fullName)-1, "%s/%zd", name, i);
        
        ok = dvmHeapBitmapInitFromTemplate(&out[i], &templates[i], fullName);
        if (!ok) {
            dvmHeapBitmapDeleteList(out, i);
            return false;
        }
    }
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
        // Re-calculate the size we passed to mmap().
        size_t allocLen = ALIGN_UP_TO_PAGE_SIZE(hb->bitsLen);
        munmap((char *)hb->bits, allocLen);
    }
    memset(hb, 0, sizeof(*hb));
}

/*
 * Clean up any resources associated with the bitmaps.
 */
void
dvmHeapBitmapDeleteList(HeapBitmap hbs[], size_t numBitmaps)
{
    size_t i;

    for (i = 0; i < numBitmaps; i++) {
        dvmHeapBitmapDelete(&hbs[i]);
    }
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
        unsigned long int *p1, *p2;
        uintptr_t offset;

        offset = ((hb1->max < hb2->max) ? hb1->max : hb2->max) - hb1->base;
//TODO: keep track of which (and whether) one is longer for later
        index = HB_OFFSET_TO_INDEX(offset);

        p1 = hb1->bits;
        p2 = hb2->bits;
        for (i = 0; i <= index; i++) {
//TODO: unroll this. pile up a few in locals?
            unsigned long int diff = *p1++ ^ *p2++;
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
unsigned long int *p;
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
        /* Set the finger to the end of the heap (rather than longHb->max)
         * so that the callback doesn't expect to be called again
         * if it happens to change the current max.
         */
        FLUSH_POINTERBUF(longHb->base + HB_MAX_OFFSET(longHb));
    }

    return true;

#undef FLUSH_POINTERBUF
#undef DECODE_BITS
}

/*
 * Fills outIndexList with indices so that for all i:
 *
 *   hb[outIndexList[i]].base < hb[outIndexList[i+1]].base
 */
static void
createSortedBitmapIndexList(const HeapBitmap hbs[], size_t numBitmaps,
        size_t outIndexList[])
{
    int i, j;

    /* numBitmaps is usually 2 or 3, so use a simple sort */
    for (i = 0; i < (int) numBitmaps; i++) {
        outIndexList[i] = i;
        for (j = 0; j < i; j++) {
            if (hbs[j].base > hbs[i].base) {
                int tmp = outIndexList[i];
                outIndexList[i] = outIndexList[j];
                outIndexList[j] = tmp;
            }
        }
    }
}

/*
 * Similar to dvmHeapBitmapXorWalk(), but compare multiple bitmaps.
 * Regardless of the order of the arrays, the bitmaps will be visited
 * in address order, so that finger will increase monotonically.
 */
bool
dvmHeapBitmapXorWalkLists(const HeapBitmap hbs1[], const HeapBitmap hbs2[],
        size_t numBitmaps,
        bool (*callback)(size_t numPtrs, void **ptrs,
                         const void *finger, void *arg),
        void *callbackArg)
{
    size_t indexList[numBitmaps];
    size_t i;

    /* Sort the bitmaps by address.  Assume that the two lists contain
     * congruent bitmaps.
     */
    createSortedBitmapIndexList(hbs1, numBitmaps, indexList);

    /* Walk each pair of bitmaps, lowest address first.
     */
    for (i = 0; i < numBitmaps; i++) {
        bool ok;

        ok = dvmHeapBitmapXorWalk(&hbs1[indexList[i]], &hbs2[indexList[i]],
                callback, callbackArg);
        if (!ok) {
            return false;
        }
    }

    return true;
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

/*
 * Similar to dvmHeapBitmapXorWalkList(), but visit the set bits
 * in a single list of bitmaps.  Regardless of the order of the array,
 * the bitmaps will be visited in address order, so that finger will
 * increase monotonically.
 */
bool dvmHeapBitmapWalkList(const HeapBitmap hbs[], size_t numBitmaps,
        bool (*callback)(size_t numPtrs, void **ptrs,
                         const void *finger, void *arg),
        void *callbackArg)
{
    size_t indexList[numBitmaps];
    size_t i;

    /* Sort the bitmaps by address.
     */
    createSortedBitmapIndexList(hbs, numBitmaps, indexList);

    /* Walk each bitmap, lowest address first.
     */
    for (i = 0; i < numBitmaps; i++) {
        bool ok;

        ok = dvmHeapBitmapWalk(&hbs[indexList[i]], callback, callbackArg);
        if (!ok) {
            return false;
        }
    }

    return true;
}
