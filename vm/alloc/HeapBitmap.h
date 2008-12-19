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
#ifndef _DALVIK_HEAP_BITMAP
#define _DALVIK_HEAP_BITMAP

#include <stdint.h>
#include <assert.h>

#define HB_OBJECT_ALIGNMENT 8
#define HB_BITS_PER_WORD    (sizeof (unsigned long int) * 8)

/* <offset> is the difference from .base to a pointer address.
 * <index> is the index of .bits that contains the bit representing
 *         <offset>.
 */
#define HB_OFFSET_TO_INDEX(offset_) \
    ((uintptr_t)(offset_) / HB_OBJECT_ALIGNMENT / HB_BITS_PER_WORD)
#define HB_INDEX_TO_OFFSET(index_) \
    ((uintptr_t)(index_) * HB_OBJECT_ALIGNMENT * HB_BITS_PER_WORD)

/* Pack the bits in backwards so they come out in address order
 * when using CLZ.
 */
#define HB_OFFSET_TO_MASK(offset_) \
    (1 << \
        (31-(((uintptr_t)(offset_) / HB_OBJECT_ALIGNMENT) % HB_BITS_PER_WORD)))

/* Return the maximum offset (exclusive) that <hb> can represent.
 */
#define HB_MAX_OFFSET(hb_) \
    HB_INDEX_TO_OFFSET((hb_)->bitsLen / sizeof(*(hb_)->bits))

#define HB_INLINE_PROTO(p) \
    static inline p __attribute__((always_inline)); \
    static inline p


typedef struct {
    /* The bitmap data, which points to an mmap()ed area of zeroed
     * anonymous memory.
     */
    unsigned long int *bits;

    /* The size of the memory pointed to by bits, in bytes.
     */
    size_t bitsLen;

    /* The base address, which corresponds to the first bit in
     * the bitmap.
     */
    uintptr_t base;

    /* The highest pointer value ever returned by an allocation
     * from this heap.  I.e., the highest address that may correspond
     * to a set bit.  If there are no bits set, (max < base).
     */
    uintptr_t max;
} HeapBitmap;


/*
 * Initialize a HeapBitmap so that it points to a bitmap large
 * enough to cover a heap at <base> of <maxSize> bytes, where
 * objects are guaranteed to be HB_OBJECT_ALIGNMENT-aligned.
 */
bool dvmHeapBitmapInit(HeapBitmap *hb, const void *base, size_t maxSize,
        const char *name);

/*
 * Initialize <hb> so that it covers the same extent as <templateBitmap>.
 */
bool dvmHeapBitmapInitFromTemplate(HeapBitmap *hb,
        const HeapBitmap *templateBitmap, const char *name);

/*
 * Initialize the bitmaps in <out> so that they cover the same extent as
 * the corresponding bitmaps in <templates>.
 */
bool dvmHeapBitmapInitListFromTemplates(HeapBitmap out[],
    HeapBitmap templates[], size_t numBitmaps, const char *name);

/*
 * Clean up any resources associated with the bitmap.
 */
void dvmHeapBitmapDelete(HeapBitmap *hb);

/*
 * Clean up any resources associated with the bitmaps.
 */
void dvmHeapBitmapDeleteList(HeapBitmap hbs[], size_t numBitmaps);

/*
 * Fill the bitmap with zeroes.  Returns the bitmap's memory to
 * the system as a side-effect.
 */
void dvmHeapBitmapZero(HeapBitmap *hb);

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
bool dvmHeapBitmapXorWalk(const HeapBitmap *hb1, const HeapBitmap *hb2,
        bool (*callback)(size_t numPtrs, void **ptrs,
                         const void *finger, void *arg),
        void *callbackArg);

/*
 * Similar to dvmHeapBitmapXorWalk(), but compare multiple bitmaps.
 * Regardless of the order of the arrays, the bitmaps will be visited
 * in address order, so that finger will increase monotonically.
 */
bool dvmHeapBitmapXorWalkLists(const HeapBitmap hbs1[], const HeapBitmap hbs2[],
        size_t numBitmaps,
        bool (*callback)(size_t numPtrs, void **ptrs,
                         const void *finger, void *arg),
        void *callbackArg);

/*
 * Similar to dvmHeapBitmapXorWalk(), but visit the set bits
 * in a single bitmap.
 */
bool dvmHeapBitmapWalk(const HeapBitmap *hb,
        bool (*callback)(size_t numPtrs, void **ptrs,
                         const void *finger, void *arg),
        void *callbackArg);

/*
 * Similar to dvmHeapBitmapXorWalkList(), but visit the set bits
 * in a single list of bitmaps.  Regardless of the order of the array,
 * the bitmaps will be visited in address order, so that finger will
 * increase monotonically.
 */
bool dvmHeapBitmapWalkList(const HeapBitmap hbs[], size_t numBitmaps,
        bool (*callback)(size_t numPtrs, void **ptrs,
                         const void *finger, void *arg),
        void *callbackArg);
/*
 * Return true iff <obj> is within the range of pointers that
 * have had corresponding bits set in this bitmap.
 */
HB_INLINE_PROTO(
    bool
    dvmHeapBitmapMayContainObject(const HeapBitmap *hb,
            const void *obj)
)
{
    const uintptr_t p = (const uintptr_t)obj;

    assert((p & (HB_OBJECT_ALIGNMENT - 1)) == 0);

    return p >= hb->base && p <= hb->max;
}

/*
 * Return true iff <obj> is within the range of pointers that this
 * bitmap could potentially cover, even if a bit has not been set
 * for it.
 */
HB_INLINE_PROTO(
    bool
    dvmHeapBitmapCoversAddress(const HeapBitmap *hb, const void *obj)
)
{
    assert(hb != NULL);

    if (obj != NULL) {
        const uintptr_t offset = (uintptr_t)obj - hb->base;
        const size_t index = HB_OFFSET_TO_INDEX(offset);
        return index < hb->bitsLen / sizeof(*hb->bits);
    }
    return false;
}

/*
 * Internal function; do not call directly.
 */
HB_INLINE_PROTO(
    unsigned long int
    _heapBitmapModifyObjectBit(HeapBitmap *hb, const void *obj,
            bool setBit, bool returnOld)
)
{
    const uintptr_t offset = (uintptr_t)obj - hb->base;
    const size_t index = HB_OFFSET_TO_INDEX(offset);
    const unsigned long int mask = HB_OFFSET_TO_MASK(offset);

#ifndef NDEBUG
    assert(hb->bits != NULL);
    assert((uintptr_t)obj >= hb->base);
    assert(index < hb->bitsLen / sizeof(*hb->bits));
#endif

    if (setBit) {
        if ((uintptr_t)obj > hb->max) {
            hb->max = (uintptr_t)obj;
        }
        if (returnOld) {
            unsigned long int *p = hb->bits + index;
            const unsigned long int word = *p;
            *p |= mask;
            return word & mask;
        } else {
            hb->bits[index] |= mask;
        }
    } else {
        hb->bits[index] &= ~mask;
    }
    return false;
}

/*
 * Sets the bit corresponding to <obj>, and returns the previous value
 * of that bit (as zero or non-zero). Does no range checking to see if
 * <obj> is outside of the coverage of the bitmap.
 *
 * NOTE: casting this value to a bool is dangerous, because higher
 * set bits will be lost.
 */
HB_INLINE_PROTO(
    unsigned long int
    dvmHeapBitmapSetAndReturnObjectBit(HeapBitmap *hb, const void *obj)
)
{
    return _heapBitmapModifyObjectBit(hb, obj, true, true);
}

/*
 * Like dvmHeapBitmapSetAndReturnObjectBit(), but sets/returns the bit
 * in the appropriate bitmap.  Results are undefined if <obj> is not
 * covered by any bitmap.
 */
HB_INLINE_PROTO(
    unsigned long int
    dvmHeapBitmapSetAndReturnObjectBitInList(HeapBitmap hbs[],
            size_t numBitmaps, const void *obj)
)
{
    size_t i;

    for (i = 0; i < numBitmaps; i++) {
        if (dvmHeapBitmapCoversAddress(&hbs[i], obj)) {
            return dvmHeapBitmapSetAndReturnObjectBit(&hbs[i], obj);
        }
    }

    assert(!"object not covered by any bitmap");
    return false;
}

/*
 * Sets the bit corresponding to <obj>, and widens the range of seen
 * pointers if necessary.  Does no range checking.
 */
HB_INLINE_PROTO(
    void
    dvmHeapBitmapSetObjectBit(HeapBitmap *hb, const void *obj)
)
{
    (void)_heapBitmapModifyObjectBit(hb, obj, true, false);
}

/*
 * Clears the bit corresponding to <obj>.  Does no range checking.
 */
HB_INLINE_PROTO(
    void
    dvmHeapBitmapClearObjectBit(HeapBitmap *hb, const void *obj)
)
{
    (void)_heapBitmapModifyObjectBit(hb, obj, false, false);
}

/*
 * Returns the current value of the bit corresponding to <obj>,
 * as zero or non-zero.  Does no range checking.
 *
 * NOTE: casting this value to a bool is dangerous, because higher
 * set bits will be lost.
 */
HB_INLINE_PROTO(
    unsigned long int
    dvmHeapBitmapIsObjectBitSet(const HeapBitmap *hb, const void *obj)
)
{
    assert(dvmHeapBitmapCoversAddress(hb, obj));
    assert(hb->bits != NULL);
    assert((uintptr_t)obj >= hb->base);

    if ((uintptr_t)obj <= hb->max) {
        const uintptr_t offset = (uintptr_t)obj - hb->base;
        return hb->bits[HB_OFFSET_TO_INDEX(offset)] & HB_OFFSET_TO_MASK(offset);
    } else {
        return 0;
    }
}

/*
 * Looks through the list of bitmaps and returns the current value of the
 * bit corresponding to <obj>, which may be covered by any of the bitmaps.
 * Does no range checking.
 */
HB_INLINE_PROTO(
    long
    dvmHeapBitmapIsObjectBitSetInList(const HeapBitmap hbs[], size_t numBitmaps,
            const void *obj)
)
{
    size_t i;

    for (i = 0; i < numBitmaps; i++) {
        if (dvmHeapBitmapCoversAddress(&hbs[i], obj)) {
            return dvmHeapBitmapIsObjectBitSet(&hbs[i], obj);
        }
    }
    return false;
}

#undef HB_INLINE_PROTO

#endif  // _DALVIK_HEAP_BITMAP
