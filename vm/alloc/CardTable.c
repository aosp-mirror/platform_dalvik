/*
 * Copyright (C) 2010 The Android Open Source Project
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

/*
 * Needed for PROT_* definitions.
 */
#include <sys/mman.h>

#include "Dalvik.h"
#include "alloc/HeapSource.h"
#include "alloc/Visit.h"

/*
 * Maintain a card table from the the write barrier. All writes of
 * non-NULL values to heap addresses should go through an entry in
 * WriteBarrier, and from there to here.
 *
 * The heap is divided into "cards" of GC_CARD_SIZE bytes, as
 * determined by GC_CARD_SHIFT. The card table contains one byte of
 * data per card, to be used by the GC. The value of the byte will be
 * one of GC_CARD_CLEAN or GC_CARD_DIRTY.
 *
 * After any store of a non-NULL object pointer into a heap object,
 * code is obliged to mark the card dirty. The setters in
 * ObjectInlines.h [such as dvmSetFieldObject] do this for you. The
 * JIT and fast interpreters also contain code to mark cards as dirty.
 *
 * [TODO: Concurrent collection will have to expand on this, as it
 * uses the card table as well.]
 *
 * The card table is used to support partial collection, which at the
 * moment means "treat the zygote's heap as permanent, and only GC
 * objects in the application heap". In order to do this efficiently,
 * the GC need to find quickly references to objects in the
 * application heap from the zygote heap.  When an application creates
 * an object and stores it into an object on the zygote heap, it will
 * mark the corresponding card in the zygote heap as "dirty". When the
 * GC does a partial collection, it can efficiently find all the
 * cross-heap objects, since they are all on dirty cards. The GC also
 * takes the opportunity to mark as "clean" any cards which are dirty,
 * but no longer contain cross-heap pointers.
 *
 * The card table's base [the "biased card table"] gets set to a
 * rather strange value.  In order to keep the JIT from having to
 * fabricate or load GC_DIRTY_CARD to store into the card table,
 * biased base is within the mmap allocation at a point where it's low
 * byte is equal to GC_DIRTY_CARD. See dvmCardTableStartup for details.
 */

/*
 * Initializes the card table; must be called before any other
 * dvmCardTable*() functions.
 */
bool dvmCardTableStartup(GcHeap *gcHeap, void *heapBase)
{
    size_t length;
    void *allocBase;
    u1 *biasedBase;

    /* Set up the card table */
    length = gDvm.heapSizeMax / GC_CARD_SIZE;
    /* Allocate an extra 256 bytes to allow fixed low-byte of base */
    allocBase = dvmAllocRegion(length + 0x100, PROT_READ | PROT_WRITE,
                            "dalvik-card-table");
    if (allocBase == NULL) {
        return false;
    }
    gcHeap->cardTableBase = allocBase;
    gcHeap->cardTableLength = length;
    /* All zeros is the correct initial value; all clean. */
    assert(GC_CARD_CLEAN == 0);

    biasedBase = (u1 *)((uintptr_t)allocBase -
                        ((uintptr_t)heapBase >> GC_CARD_SHIFT));
    if (((uintptr_t)biasedBase & 0xff) != GC_CARD_DIRTY) {
        int offset;
        offset = GC_CARD_DIRTY - ((uintptr_t)biasedBase & 0xff);
        biasedBase += offset + (offset < 0 ? 0x100 : 0);
    }
    assert(((uintptr_t)biasedBase & 0xff) == GC_CARD_DIRTY);
    gcHeap->biasedCardTableBase = biasedBase;

    return true;
}

/*
 * Tears down the entire CardTable.
 */
void dvmCardTableShutdown()
{
    munmap(gDvm.gcHeap->cardTableBase, gDvm.gcHeap->cardTableLength);
}

/*
 * Returns the address of the relevent byte in the card table, given
 * an address on the heap.
 */
u1 *dvmCardFromAddr(const void *addr)
{
    GcHeap *h = gDvm.gcHeap;
    u1 *cardAddr = h->biasedCardTableBase + ((uintptr_t)addr >> GC_CARD_SHIFT);
    assert(cardAddr >= h->cardTableBase);
    assert(cardAddr < &h->cardTableBase[h->cardTableLength]);
    return cardAddr;
}

/*
 * Returns the first address in the heap which maps to this card.
 */
void *dvmAddrFromCard(const u1 *cardAddr)
{
    GcHeap *h = gDvm.gcHeap;
    assert(cardAddr >= h->cardTableBase);
    assert(cardAddr < &h->cardTableBase[h->cardTableLength]);
    uintptr_t offset = cardAddr - h->biasedCardTableBase;
    return (void *)(offset << GC_CARD_SHIFT);
}

/*
 * Dirties the card for the given address.
 */
void dvmMarkCard(const void *addr)
{
    u1 *cardAddr = dvmCardFromAddr(addr);
    *cardAddr = GC_CARD_DIRTY;
}

/*
 * Returns true iff all address within the Object are on unmarked cards.
 */
static bool objectIsClean(const Object *obj)
{
    assert(dvmIsValidObject(obj));
    size_t size = dvmHeapSourceChunkSize(obj);
    u1 *start = dvmCardFromAddr(obj);
    u1 *end = dvmCardFromAddr((char *)obj + size-1);
    u1 *index;

    for (index = start; index <= end; index++) {
        if (*index != GC_CARD_CLEAN) {
            return false;
        }
    }
    return true;
}

/*
 * A Visitor callback in support of checkCleanObjects. "arg" is
 * expected to be the immuneLimit.
 */
static void crossGenCheckVisitor(void *ptr, void *arg)
{
    Object *ref = *(Object **)ptr;
    Object *immuneLimit = (Object *)arg;

    if (ref >= immuneLimit) {
        LOGE("Clean obj contains threatened ref %p: %p", ptr, ref);
        dvmAbort();
    }
}

/*
 * A HeapBitmap callback in support of checkCleanObjects.
 */
static bool crossGenCheckCallback(size_t numPtrs, void **ptrs,
                      const void *finger, void *arg)
{
    size_t i;
    for (i = 0; i < numPtrs; i++) {
        Object *obj = ptrs[i];
        if (objectIsClean(obj)) {
            dvmVisitObject(crossGenCheckVisitor, obj, arg);
        }
    }

    return true;
}

/*
 * dvmAbort if a clean, immune Object in the bitmap contains a pointer
 * to a threatened Object.
 */
void dvmVerifyCardTable(HeapBitmap *bitmap, const char *immuneLimit)
{
    dvmHeapBitmapWalk(bitmap, crossGenCheckCallback, (void *)immuneLimit);
}

