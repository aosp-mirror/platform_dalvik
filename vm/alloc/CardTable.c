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

#include <sys/mman.h>  /* for PROT_* */

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
bool dvmCardTableStartup(void)
{
    size_t length;
    void *allocBase;
    u1 *biasedBase;
    GcHeap *gcHeap = gDvm.gcHeap;
    void *heapBase = dvmHeapSourceGetBase();
    assert(gcHeap != NULL);
    assert(heapBase != NULL);

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
        int offset = GC_CARD_DIRTY - ((uintptr_t)biasedBase & 0xff);
        biasedBase += offset + (offset < 0 ? 0x100 : 0);
    }
    assert(((uintptr_t)biasedBase & 0xff) == GC_CARD_DIRTY);
    gDvm.biasedCardTableBase = biasedBase;

    return true;
}

/*
 * Tears down the entire CardTable.
 */
void dvmCardTableShutdown()
{
    gDvm.biasedCardTableBase = NULL;
    munmap(gDvm.gcHeap->cardTableBase, gDvm.gcHeap->cardTableLength);
}

void dvmClearCardTable(void)
{
    assert(gDvm.gcHeap->cardTableBase != NULL);
    memset(gDvm.gcHeap->cardTableBase, GC_CARD_CLEAN, gDvm.gcHeap->cardTableLength);
}

/*
 * Returns true iff the address is within the bounds of the card table.
 */
bool dvmIsValidCard(const u1 *cardAddr)
{
    GcHeap *h = gDvm.gcHeap;
    return cardAddr >= h->cardTableBase &&
        cardAddr < &h->cardTableBase[h->cardTableLength];
}

/*
 * Returns the address of the relevent byte in the card table, given
 * an address on the heap.
 */
u1 *dvmCardFromAddr(const void *addr)
{
    u1 *biasedBase = gDvm.biasedCardTableBase;
    u1 *cardAddr = biasedBase + ((uintptr_t)addr >> GC_CARD_SHIFT);
    assert(dvmIsValidCard(cardAddr));
    return cardAddr;
}

/*
 * Returns the first address in the heap which maps to this card.
 */
void *dvmAddrFromCard(const u1 *cardAddr)
{
    assert(dvmIsValidCard(cardAddr));
    uintptr_t offset = cardAddr - gDvm.biasedCardTableBase;
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
 * Returns true if the object is on a dirty card.
 */
static bool isObjectDirty(const Object *obj)
{
    assert(obj != NULL);
    assert(dvmIsValidObject(obj));
    u1 *card = dvmCardFromAddr(obj);
    return *card == GC_CARD_DIRTY;
}

/*
 * Context structure for verifying the card table.
 */
typedef struct {
    HeapBitmap *markBits;
    size_t whiteRefs;
} WhiteReferenceCounter;

/*
 * Visitor that counts white referents.
 */
static void countWhiteReferenceVisitor(void *addr, void *arg)
{
    WhiteReferenceCounter *ctx;
    Object *obj;

    assert(addr != NULL);
    assert(arg != NULL);
    obj = *(Object **)addr;
    if (obj == NULL) {
        return;
    }
    assert(dvmIsValidObject(obj));
    ctx = arg;
    if (dvmHeapBitmapIsObjectBitSet(ctx->markBits, obj)) {
        return;
    }
    ctx->whiteRefs += 1;
}

/*
 * Returns true if the given object is a reference object and the
 * just the referent is unmarked.
 */
static bool isReferentUnmarked(const Object *obj,
                               const WhiteReferenceCounter* ctx)
{
    assert(obj != NULL);
    assert(obj->clazz != NULL);
    assert(ctx != NULL);
    if (ctx->whiteRefs != 1) {
        return false;
    } else if (IS_CLASS_FLAG_SET(obj->clazz, CLASS_ISREFERENCE)) {
        size_t offset = gDvm.offJavaLangRefReference_referent;
        const Object *referent = dvmGetFieldObject(obj, offset);
        return !dvmHeapBitmapIsObjectBitSet(ctx->markBits, referent);
    } else {
        return false;
    }
}

/*
 * Returns true if the given object is a string and has been interned
 * by the user.
 */
static bool isWeakInternedString(const Object *obj)
{
    assert(obj != NULL);
    if (obj->clazz == gDvm.classJavaLangString) {
        return dvmIsWeakInternedString((StringObject *)obj);
    } else {
        return false;
    }
}

/*
 * Returns true if the given object has been pushed on the mark stack
 * by root marking.
 */
static bool isPushedOnMarkStack(const Object *obj)
{
    GcMarkContext *ctx = &gDvm.gcHeap->markContext;
    const Object **ptr;

    for (ptr = ctx->stack.top; ptr != ctx->stack.base; ++ptr) {
        if (*ptr == obj) {
            return true;
        }
    }
    return false;
}

/*
 * Callback applied to marked objects.  If the object is gray and on
 * an unmarked card an error is logged and the VM is aborted.  Card
 * table verification occurs between root marking and weak reference
 * processing.  We treat objects marked from the roots and weak
 * references specially as it is permissible for these objects to be
 * gray and on an unmarked card.
 */
static void verifyCardTableCallback(void *ptr, void *arg)
{
    Object *obj = ptr;
    WhiteReferenceCounter ctx = { arg, 0 };

    dvmVisitObject(countWhiteReferenceVisitor, obj, &ctx);
    if (ctx.whiteRefs == 0) {
        return;
    } else if (isObjectDirty(obj)) {
        return;
    } else if (isReferentUnmarked(obj, &ctx)) {
        return;
    } else if (isWeakInternedString(obj)) {
        return;
    } else if (isPushedOnMarkStack(obj)) {
        return;
    } else {
        LOGE("Verify failed, object %p is gray and on an unmarked card", obj);
        dvmDumpObject(obj);
        dvmAbort();
    }
}

/*
 * Verifies that gray objects are on a dirty card.
 */
void dvmVerifyCardTable(void)
{
    HeapBitmap *markBits = gDvm.gcHeap->markContext.bitmap;
    dvmHeapBitmapWalk(markBits, verifyCardTableCallback, markBits);
}
