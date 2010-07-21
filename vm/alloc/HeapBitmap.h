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

#define HB_OBJECT_ALIGNMENT 8
#define HB_BITS_PER_WORD    (sizeof (unsigned long) * 8)

/* <offset> is the difference from .base to a pointer address.
 * <index> is the index of .bits that contains the bit representing
 *         <offset>.
 */
#define HB_OFFSET_TO_INDEX(offset_) \
    ((uintptr_t)(offset_) / HB_OBJECT_ALIGNMENT / HB_BITS_PER_WORD)
#define HB_INDEX_TO_OFFSET(index_) \
    ((uintptr_t)(index_) * HB_OBJECT_ALIGNMENT * HB_BITS_PER_WORD)

#define HB_OFFSET_TO_BYTE_INDEX(offset_) \
  (HB_OFFSET_TO_INDEX(offset_) * sizeof(*((HeapBitmap *)0)->bits))

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


struct HeapBitmap {
    /* The bitmap data, which points to an mmap()ed area of zeroed
     * anonymous memory.
     */
    unsigned long *bits;

    /* The size of the used memory pointed to by bits, in bytes.  This
     * value changes when the bitmap is shrunk.
     */
    size_t bitsLen;

    /* The real size of the memory pointed to by bits.  This is the
     * number of bytes we requested from the allocator and does not
     * change.
     */
    size_t allocLen;

    /* The base address, which corresponds to the first bit in
     * the bitmap.
     */
    uintptr_t base;

    /* The highest pointer value ever returned by an allocation
     * from this heap.  I.e., the highest address that may correspond
     * to a set bit.  If there are no bits set, (max < base).
     */
    uintptr_t max;
};
typedef struct HeapBitmap HeapBitmap;

typedef void BitmapCallback(size_t numPtrs, void **ptrs,
                            const void *finger, void *arg);

/*
 * Initialize a HeapBitmap so that it points to a bitmap large
 * enough to cover a heap at <base> of <maxSize> bytes, where
 * objects are guaranteed to be HB_OBJECT_ALIGNMENT-aligned.
 */
bool dvmHeapBitmapInit(HeapBitmap *hb, const void *base, size_t maxSize,
        const char *name);

/*
 * Clean up any resources associated with the bitmap.
 */
void dvmHeapBitmapDelete(HeapBitmap *hb);

/*
 * Fill the bitmap with zeroes.  Returns the bitmap's memory to
 * the system as a side-effect.
 */
void dvmHeapBitmapZero(HeapBitmap *hb);

/*
 * Walk through the bitmaps in increasing address order, and find the
 * object pointers that correspond to garbage objects.  Call
 * <callback> zero or more times with lists of these object pointers.
 *
 * The <finger> argument to the callback indicates the next-highest
 * address that hasn't been visited yet; setting bits for objects whose
 * addresses are less than <finger> are not guaranteed to be seen by
 * the current walk.
 *
 * The callback is permitted to increase the bitmap's max; the walk
 * will use the updated max as a terminating condition,
 *
 * <finger> will be set to some value beyond the bitmap max when the
 * end of the bitmap is reached.
 */
void dvmHeapBitmapSweepWalk(const HeapBitmap *liveHb, const HeapBitmap *markHb,
                            BitmapCallback *callback, void *callbackArg);

/*
 * Similar to dvmHeapBitmapSweepWalk(), but visit the set bits
 * in a single bitmap.
 */
void dvmHeapBitmapWalk(const HeapBitmap *hb,
                       BitmapCallback *callback, void *callbackArg);

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
    unsigned long
    _heapBitmapModifyObjectBit(HeapBitmap *hb, const void *obj,
            bool setBit, bool returnOld)
)
{
    const uintptr_t offset = (uintptr_t)obj - hb->base;
    const size_t index = HB_OFFSET_TO_INDEX(offset);
    const unsigned long mask = HB_OFFSET_TO_MASK(offset);

    assert(hb->bits != NULL);
    assert((uintptr_t)obj >= hb->base);
    assert(index < hb->bitsLen / sizeof(*hb->bits));

    if (setBit) {
        if ((uintptr_t)obj > hb->max) {
            hb->max = (uintptr_t)obj;
        }
        if (returnOld) {
            unsigned long *p = hb->bits + index;
            const unsigned long word = *p;
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
    unsigned long
    dvmHeapBitmapSetAndReturnObjectBit(HeapBitmap *hb, const void *obj)
)
{
    return _heapBitmapModifyObjectBit(hb, obj, true, true);
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
    unsigned long
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

#undef HB_INLINE_PROTO

#endif  // _DALVIK_HEAP_BITMAP
