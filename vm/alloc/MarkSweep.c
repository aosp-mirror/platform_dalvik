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
#include "alloc/CardTable.h"
#include "alloc/HeapBitmap.h"
#include "alloc/HeapBitmapInlines.h"
#include "alloc/HeapInternal.h"
#include "alloc/HeapSource.h"
#include "alloc/MarkSweep.h"
#include "alloc/Visit.h"
#include <limits.h>     // for ULONG_MAX
#include <sys/mman.h>   // for madvise(), mmap()
#include <errno.h>

#define GC_LOG_TAG      LOG_TAG "-gc"

#if LOG_NDEBUG
#define LOGD_GC(...)    ((void)0)
#else
#define LOGD_GC(...)    LOG(LOG_DEBUG, GC_LOG_TAG, __VA_ARGS__)
#endif

#define LOGE_GC(...)    LOG(LOG_ERROR, GC_LOG_TAG, __VA_ARGS__)

typedef unsigned long Word;
const size_t kWordSize = sizeof(Word);

/* Do not cast the result of this to a boolean; the only set bit
 * may be > 1<<8.
 */
static long isMarked(const void *obj, const GcMarkContext *ctx)
{
    return dvmHeapBitmapIsObjectBitSet(ctx->bitmap, obj);
}

/*
 * Initializes the stack top and advises the mark stack pages as needed.
 */
static bool createMarkStack(GcMarkStack *stack)
{
    assert(stack != NULL);
    size_t length = dvmHeapSourceGetIdealFootprint() * sizeof(Object*) /
        (sizeof(Object) + HEAP_SOURCE_CHUNK_OVERHEAD);
    madvise(stack->base, length, MADV_NORMAL);
    stack->top = stack->base;
    return true;
}

/*
 * Assigns NULL to the stack top and advises the mark stack pages as
 * not needed.
 */
static void destroyMarkStack(GcMarkStack *stack)
{
    assert(stack != NULL);
    madvise(stack->base, stack->length, MADV_DONTNEED);
    stack->top = NULL;
}

/*
 * Pops an object from the mark stack.
 */
static void markStackPush(GcMarkStack *stack, const Object *obj)
{
    assert(stack != NULL);
    assert(stack->base <= stack->top);
    assert(stack->limit > stack->top);
    assert(obj != NULL);
    *stack->top = obj;
    ++stack->top;
}

/*
 * Pushes an object on the mark stack.
 */
static const Object *markStackPop(GcMarkStack *stack)
{
    assert(stack != NULL);
    assert(stack->base < stack->top);
    assert(stack->limit > stack->top);
    --stack->top;
    return *stack->top;
}

bool dvmHeapBeginMarkStep(bool isPartial)
{
    GcMarkContext *ctx = &gDvm.gcHeap->markContext;

    if (!createMarkStack(&ctx->stack)) {
        return false;
    }
    ctx->finger = NULL;
    ctx->immuneLimit = (char*)dvmHeapSourceGetImmuneLimit(isPartial);
    return true;
}

static long setAndReturnMarkBit(GcMarkContext *ctx, const void *obj)
{
    return dvmHeapBitmapSetAndReturnObjectBit(ctx->bitmap, obj);
}

static void markObjectNonNull(const Object *obj, GcMarkContext *ctx,
                              bool checkFinger)
{
    assert(ctx != NULL);
    assert(obj != NULL);
    assert(dvmIsValidObject(obj));

    if (obj < (Object *)ctx->immuneLimit) {
        assert(isMarked(obj, ctx));
        return;
    }
    if (!setAndReturnMarkBit(ctx, obj)) {
        /* This object was not previously marked.
         */
        if (checkFinger && (void *)obj < ctx->finger) {
            /* This object will need to go on the mark stack.
             */
            markStackPush(&ctx->stack, obj);
        }
    }
}

/* Used to mark objects when recursing.  Recursion is done by moving
 * the finger across the bitmaps in address order and marking child
 * objects.  Any newly-marked objects whose addresses are lower than
 * the finger won't be visited by the bitmap scan, so those objects
 * need to be added to the mark stack.
 */
static void markObject(const Object *obj, GcMarkContext *ctx)
{
    if (obj != NULL) {
        markObjectNonNull(obj, ctx, true);
    }
}

/*
 * Callback applied to root references during the initial root
 * marking.  Marks white objects but does not push them on the mark
 * stack.
 */
static void rootMarkObjectVisitor(void *addr, RootType type, u4 thread,
                                  void *arg)
{
    Object *obj;
    GcMarkContext *ctx;

    assert(addr != NULL);
    assert(arg != NULL);
    obj = *(Object **)addr;
    ctx = (GcMarkContext *)arg;
    if (obj != NULL) {
        markObjectNonNull(obj, ctx, false);
    }
}

/* Mark the set of root objects.
 *
 * Things we need to scan:
 * - System classes defined by root classloader
 * - For each thread:
 *   - Interpreted stack, from top to "curFrame"
 *     - Dalvik registers (args + local vars)
 *   - JNI local references
 *   - Automatic VM local references (TrackedAlloc)
 *   - Associated Thread/VMThread object
 *   - ThreadGroups (could track & start with these instead of working
 *     upward from Threads)
 *   - Exception currently being thrown, if present
 * - JNI global references
 * - Interned string table
 * - Primitive classes
 * - Special objects
 *   - gDvm.outOfMemoryObj
 * - Objects allocated with ALLOC_NO_GC
 * - Objects pending finalization (but not yet finalized)
 * - Objects in debugger object registry
 *
 * Don't need:
 * - Native stack (for in-progress stuff in the VM)
 *   - The TrackedAlloc stuff watches all native VM references.
 */
void dvmHeapMarkRootSet()
{
    GcHeap *gcHeap = gDvm.gcHeap;
    dvmMarkImmuneObjects(gcHeap->markContext.immuneLimit);
    dvmVisitRoots(rootMarkObjectVisitor, &gcHeap->markContext);
}

/*
 * Callback applied to root references during root remarking.  Marks
 * white objects and pushes them on the mark stack.
 */
static void rootReMarkObjectVisitor(void *addr, RootType type, u4 thread,
                                    void *arg)
{
    Object *obj;
    GcMarkContext *ctx;

    assert(addr != NULL);
    assert(arg != NULL);
    obj = *(Object **)addr;
    ctx = (GcMarkContext *)arg;
    if (obj != NULL) {
        markObjectNonNull(obj, ctx, true);
    }
}

/*
 * Grays all references in the roots.
 */
void dvmHeapReMarkRootSet(void)
{
    GcMarkContext *ctx = &gDvm.gcHeap->markContext;
    assert(ctx->finger == (void *)ULONG_MAX);
    dvmVisitRoots(rootReMarkObjectVisitor, ctx);
}

/*
 * Scans instance fields.
 */
static void scanFields(const Object *obj, GcMarkContext *ctx)
{
    assert(obj != NULL);
    assert(obj->clazz != NULL);
    assert(ctx != NULL);

    if (obj->clazz->refOffsets != CLASS_WALK_SUPER) {
        unsigned int refOffsets = obj->clazz->refOffsets;
        while (refOffsets != 0) {
            size_t rshift = CLZ(refOffsets);
            size_t offset = CLASS_OFFSET_FROM_CLZ(rshift);
            Object *ref = dvmGetFieldObject((Object*)obj, offset);
            markObject(ref, ctx);
            refOffsets &= ~(CLASS_HIGH_BIT >> rshift);
        }
    } else {
        ClassObject *clazz;
        for (clazz = obj->clazz; clazz != NULL; clazz = clazz->super) {
            InstField *field = clazz->ifields;
            int i;
            for (i = 0; i < clazz->ifieldRefCount; ++i, ++field) {
                void *addr = BYTE_OFFSET((Object *)obj, field->byteOffset);
                Object *ref = (Object *)((JValue *)addr)->l;
                markObject(ref, ctx);
            }
        }
    }
}

/*
 * Scans the static fields of a class object.
 */
static void scanStaticFields(const ClassObject *clazz, GcMarkContext *ctx)
{
    int i;

    assert(clazz != NULL);
    assert(ctx != NULL);
    for (i = 0; i < clazz->sfieldCount; ++i) {
        char ch = clazz->sfields[i].field.signature[0];
        if (ch == '[' || ch == 'L') {
            Object *obj = (Object *)clazz->sfields[i].value.l;
            markObject(obj, ctx);
        }
    }
}

/*
 * Visit the interfaces of a class object.
 */
static void scanInterfaces(const ClassObject *clazz, GcMarkContext *ctx)
{
    int i;

    assert(clazz != NULL);
    assert(ctx != NULL);
    for (i = 0; i < clazz->interfaceCount; ++i) {
        markObject((const Object *)clazz->interfaces[i], ctx);
    }
}

/*
 * Scans the header, static field references, and interface
 * pointers of a class object.
 */
static void scanClassObject(const Object *obj, GcMarkContext *ctx)
{
    const ClassObject *asClass;

    assert(obj != NULL);
    assert(obj->clazz == gDvm.classJavaLangClass);
    assert(ctx != NULL);
    markObject((const Object *)obj->clazz, ctx);
    asClass = (const ClassObject *)obj;
    if (IS_CLASS_FLAG_SET(asClass, CLASS_ISARRAY)) {
        markObject((const Object *)asClass->elementClass, ctx);
    }
    /* Do super and the interfaces contain Objects and not dex idx values? */
    if (asClass->status > CLASS_IDX) {
        markObject((const Object *)asClass->super, ctx);
    }
    markObject((const Object *)asClass->classLoader, ctx);
    scanFields(obj, ctx);
    scanStaticFields(asClass, ctx);
    if (asClass->status > CLASS_IDX) {
        scanInterfaces(asClass, ctx);
    }
}

/*
 * Scans the header of all array objects.  If the array object is
 * specialized to a reference type, scans the array data as well.
 */
static void scanArrayObject(const Object *obj, GcMarkContext *ctx)
{
    assert(obj != NULL);
    assert(obj->clazz != NULL);
    assert(ctx != NULL);
    markObject((const Object *)obj->clazz, ctx);
    if (IS_CLASS_FLAG_SET(obj->clazz, CLASS_ISOBJECTARRAY)) {
        const ArrayObject *array = (const ArrayObject *)obj;
        const Object **contents = (const Object **)array->contents;
        size_t i;
        for (i = 0; i < array->length; ++i) {
            markObject(contents[i], ctx);
        }
    }
}

/*
 * Returns class flags relating to Reference subclasses.
 */
static int referenceClassFlags(const Object *obj)
{
    int flags = CLASS_ISREFERENCE |
                CLASS_ISWEAKREFERENCE |
                CLASS_ISPHANTOMREFERENCE;
    return GET_CLASS_FLAG_GROUP(obj->clazz, flags);
}

/*
 * Returns true if the object derives from SoftReference.
 */
static bool isSoftReference(const Object *obj)
{
    return referenceClassFlags(obj) == CLASS_ISREFERENCE;
}

/*
 * Returns true if the object derives from WeakReference.
 */
static bool isWeakReference(const Object *obj)
{
    return referenceClassFlags(obj) & CLASS_ISWEAKREFERENCE;
}

/*
 * Returns true if the object derives from PhantomReference.
 */
static bool isPhantomReference(const Object *obj)
{
    return referenceClassFlags(obj) & CLASS_ISPHANTOMREFERENCE;
}

/*
 * Adds a reference to the tail of a circular queue of references.
 */
static void enqueuePendingReference(Object *ref, Object **list)
{
    size_t offset;

    assert(ref != NULL);
    assert(list != NULL);
    offset = gDvm.offJavaLangRefReference_pendingNext;
    if (*list == NULL) {
        dvmSetFieldObject(ref, offset, ref);
        *list = ref;
    } else {
        Object *head = dvmGetFieldObject(*list, offset);
        dvmSetFieldObject(ref, offset, head);
        dvmSetFieldObject(*list, offset, ref);
    }
}

/*
 * Removes the reference at the head of a circular queue of
 * references.
 */
static Object *dequeuePendingReference(Object **list)
{
    Object *ref, *head;
    size_t offset;

    assert(list != NULL);
    assert(*list != NULL);
    offset = gDvm.offJavaLangRefReference_pendingNext;
    head = dvmGetFieldObject(*list, offset);
    if (*list == head) {
        ref = *list;
        *list = NULL;
    } else {
        Object *next = dvmGetFieldObject(head, offset);
        dvmSetFieldObject(*list, offset, next);
        ref = head;
    }
    dvmSetFieldObject(ref, offset, NULL);
    return ref;
}

/*
 * Process the "referent" field in a java.lang.ref.Reference.  If the
 * referent has not yet been marked, put it on the appropriate list in
 * the gcHeap for later processing.
 */
static void delayReferenceReferent(Object *obj, GcMarkContext *ctx)
{
    GcHeap *gcHeap = gDvm.gcHeap;
    Object *pending, *referent;
    size_t pendingNextOffset, referentOffset;

    assert(obj != NULL);
    assert(obj->clazz != NULL);
    assert(IS_CLASS_FLAG_SET(obj->clazz, CLASS_ISREFERENCE));
    assert(ctx != NULL);
    pendingNextOffset = gDvm.offJavaLangRefReference_pendingNext;
    referentOffset = gDvm.offJavaLangRefReference_referent;
    pending = dvmGetFieldObject(obj, pendingNextOffset);
    referent = dvmGetFieldObject(obj, referentOffset);
    if (pending == NULL && referent != NULL && !isMarked(referent, ctx)) {
        Object **list = NULL;
        if (isSoftReference(obj)) {
            list = &gcHeap->softReferences;
        } else if (isWeakReference(obj)) {
            list = &gcHeap->weakReferences;
        } else if (isPhantomReference(obj)) {
            list = &gcHeap->phantomReferences;
        }
        assert(list != NULL);
        enqueuePendingReference(obj, list);
    }
}

/*
 * Scans the header and field references of a data object.
 */
static void scanDataObject(const Object *obj, GcMarkContext *ctx)
{
    assert(obj != NULL);
    assert(obj->clazz != NULL);
    assert(ctx != NULL);
    markObject((const Object *)obj->clazz, ctx);
    scanFields(obj, ctx);
    if (IS_CLASS_FLAG_SET(obj->clazz, CLASS_ISREFERENCE)) {
        delayReferenceReferent((Object *)obj, ctx);
    }
}

/*
 * Scans an object reference.  Determines the type of the reference
 * and dispatches to a specialized scanning routine.
 */
static void scanObject(const Object *obj, GcMarkContext *ctx)
{
    assert(obj != NULL);
    assert(ctx != NULL);
    assert(obj->clazz != NULL);
    if (obj->clazz == gDvm.classJavaLangClass) {
        scanClassObject(obj, ctx);
    } else if (IS_CLASS_FLAG_SET(obj->clazz, CLASS_ISARRAY)) {
        scanArrayObject(obj, ctx);
    } else {
        scanDataObject(obj, ctx);
    }
}

/*
 * Scan anything that's on the mark stack.  We can't use the bitmaps
 * anymore, so use a finger that points past the end of them.
 */
static void processMarkStack(GcMarkContext *ctx)
{
    GcMarkStack *stack;

    assert(ctx != NULL);
    assert(ctx->finger == (void *)ULONG_MAX);
    stack = &ctx->stack;
    assert(stack->top >= stack->base);
    while (stack->top > stack->base) {
        const Object *obj = markStackPop(stack);
        scanObject(obj, ctx);
    }
}

static size_t objectSize(const Object *obj)
{
    assert(dvmIsValidObject(obj));
    assert(dvmIsValidObject((Object *)obj->clazz));
    if (IS_CLASS_FLAG_SET(obj->clazz, CLASS_ISARRAY)) {
        return dvmArrayObjectSize((ArrayObject *)obj);
    } else if (obj->clazz == gDvm.classJavaLangClass) {
        return dvmClassObjectSize((ClassObject *)obj);
    } else {
        return obj->clazz->objectSize;
    }
}

/*
 * Scans forward to the header of the next marked object between start
 * and limit.  Returns NULL if no marked objects are in that region.
 */
static Object *nextGrayObject(const u1 *base, const u1 *limit,
                              const HeapBitmap *markBits)
{
    const u1 *ptr;

    assert(base < limit);
    assert(limit - base <= GC_CARD_SIZE);
    for (ptr = base; ptr < limit; ptr += HB_OBJECT_ALIGNMENT) {
        if (dvmHeapBitmapIsObjectBitSet(markBits, ptr))
            return (Object *)ptr;
    }
    return NULL;
}

/*
 * Scans range of dirty cards between start and end.  A range of dirty
 * cards is composed consecutively dirty cards or dirty cards spanned
 * by a gray object.  Returns the address of a clean card if the scan
 * reached a clean card or NULL if the scan reached the end.
 */
const u1 *scanDirtyCards(const u1 *start, const u1 *end,
                         GcMarkContext *ctx)
{
    const HeapBitmap *markBits = ctx->bitmap;
    const u1 *card = start, *prevAddr = NULL;
    while (card < end) {
        if (*card != GC_CARD_DIRTY) {
            return card;
        }
        const u1 *ptr = prevAddr ? prevAddr : (u1*)dvmAddrFromCard(card);
        const u1 *limit = ptr + GC_CARD_SIZE;
        while (ptr < limit) {
            Object *obj = nextGrayObject(ptr, limit, markBits);
            if (obj == NULL) {
                break;
            }
            scanObject(obj, ctx);
            ptr = (u1*)obj + ALIGN_UP(objectSize(obj), HB_OBJECT_ALIGNMENT);
        }
        if (ptr < limit) {
            /* Ended within the current card, advance to the next card. */
            ++card;
            prevAddr = NULL;
        } else {
            /* Ended past the current card, skip ahead. */
            card = dvmCardFromAddr(ptr);
            prevAddr = ptr;
        }
    }
    return NULL;
}

/*
 * Blackens gray objects found on dirty cards.
 */
static void scanGrayObjects(GcMarkContext *ctx)
{
    GcHeap *h = gDvm.gcHeap;
    const u1 *base, *limit, *ptr, *dirty;
    size_t footprint;

    footprint = dvmHeapSourceGetValue(HS_FOOTPRINT, NULL, 0);
    base = &h->cardTableBase[0];
    limit = dvmCardFromAddr((u1 *)dvmHeapSourceGetBase() + footprint);
    assert(limit <= &h->cardTableBase[h->cardTableLength]);

    ptr = base;
    for (;;) {
        dirty = (const u1 *)memchr(ptr, GC_CARD_DIRTY, limit - ptr);
        if (dirty == NULL) {
            break;
        }
        assert((dirty > ptr) && (dirty < limit));
        ptr = scanDirtyCards(dirty, limit, ctx);
        if (ptr == NULL) {
            break;
        }
        assert((ptr > dirty) && (ptr < limit));
    }
}

/*
 * Callback for scanning each object in the bitmap.  The finger is set
 * to the address corresponding to the lowest address in the next word
 * of bits in the bitmap.
 */
static void scanBitmapCallback(void *addr, void *finger, void *arg)
{
    GcMarkContext *ctx = (GcMarkContext *)arg;
    ctx->finger = (void *)finger;
    scanObject((Object *)addr, ctx);
}

/* Given bitmaps with the root set marked, find and mark all
 * reachable objects.  When this returns, the entire set of
 * live objects will be marked and the mark stack will be empty.
 */
void dvmHeapScanMarkedObjects(void)
{
    GcMarkContext *ctx = &gDvm.gcHeap->markContext;

    assert(ctx->finger == NULL);

    /* The bitmaps currently have bits set for the root set.
     * Walk across the bitmaps and scan each object.
     */
    dvmHeapBitmapScanWalk(ctx->bitmap, scanBitmapCallback, ctx);

    ctx->finger = (void *)ULONG_MAX;

    /* We've walked the mark bitmaps.  Scan anything that's
     * left on the mark stack.
     */
    processMarkStack(ctx);
}

void dvmHeapReScanMarkedObjects(void)
{
    GcMarkContext *ctx = &gDvm.gcHeap->markContext;

    /*
     * The finger must have been set to the maximum value to ensure
     * that gray objects will be pushed onto the mark stack.
     */
    assert(ctx->finger == (void *)ULONG_MAX);
    scanGrayObjects(ctx);
    processMarkStack(ctx);
}

/*
 * Clear the referent field.
 */
static void clearReference(Object *reference)
{
    size_t offset = gDvm.offJavaLangRefReference_referent;
    dvmSetFieldObject(reference, offset, NULL);
}

/*
 * Returns true if the reference was registered with a reference queue
 * and has not yet been enqueued.
 */
static bool isEnqueuable(const Object *reference)
{
    Object *queue = dvmGetFieldObject(reference,
            gDvm.offJavaLangRefReference_queue);
    Object *queueNext = dvmGetFieldObject(reference,
            gDvm.offJavaLangRefReference_queueNext);
    return queue != NULL && queueNext == NULL;
}

/*
 * Schedules a reference to be appended to its reference queue.
 */
static void enqueueReference(Object *ref)
{
    assert(ref != NULL);
    assert(dvmGetFieldObject(ref, gDvm.offJavaLangRefReference_queue) != NULL);
    assert(dvmGetFieldObject(ref, gDvm.offJavaLangRefReference_queueNext) == NULL);
    if (!dvmHeapAddRefToLargeTable(&gDvm.gcHeap->referenceOperations, ref)) {
        LOGE_HEAP("enqueueReference(): no room for any more "
                  "reference operations\n");
        dvmAbort();
    }
}

/*
 * Walks the reference list marking any references subject to the
 * reference clearing policy.  References with a black referent are
 * removed from the list.  References with white referents biased
 * toward saving are blackened and also removed from the list.
 */
static void preserveSomeSoftReferences(Object **list)
{
    GcMarkContext *ctx;
    Object *ref, *referent;
    Object *clear;
    size_t referentOffset;
    size_t counter;
    bool marked;

    ctx = &gDvm.gcHeap->markContext;
    referentOffset = gDvm.offJavaLangRefReference_referent;
    clear = NULL;
    counter = 0;
    while (*list != NULL) {
        ref = dequeuePendingReference(list);
        referent = dvmGetFieldObject(ref, referentOffset);
        if (referent == NULL) {
            /* Referent was cleared by the user during marking. */
            continue;
        }
        marked = isMarked(referent, ctx);
        if (!marked && ((++counter) & 1)) {
            /* Referent is white and biased toward saving, mark it. */
            markObject(referent, ctx);
            marked = true;
        }
        if (!marked) {
            /* Referent is white, queue it for clearing. */
            enqueuePendingReference(ref, &clear);
        }
    }
    *list = clear;
    /*
     * Restart the mark with the newly black references added to the
     * root set.
     */
    processMarkStack(ctx);
}

/*
 * Unlink the reference list clearing references objects with white
 * referents.  Cleared references registered to a reference queue are
 * scheduled for appending by the heap worker thread.
 */
static void clearWhiteReferences(Object **list)
{
    GcMarkContext *ctx;
    Object *ref, *referent;
    size_t referentOffset;
    bool doSignal;

    ctx = &gDvm.gcHeap->markContext;
    referentOffset = gDvm.offJavaLangRefReference_referent;
    doSignal = false;
    while (*list != NULL) {
        ref = dequeuePendingReference(list);
        referent = dvmGetFieldObject(ref, referentOffset);
        if (referent != NULL && !isMarked(referent, ctx)) {
            /* Referent is white, clear it. */
            clearReference(ref);
            if (isEnqueuable(ref)) {
                enqueueReference(ref);
                doSignal = true;
            }
        }
    }
    /*
     * If we cleared a reference with a reference queue we must notify
     * the heap worker to append the reference.
     */
    if (doSignal) {
        dvmSignalHeapWorker(false);
    }
    assert(*list == NULL);
}

/* Find unreachable objects that need to be finalized,
 * and schedule them for finalization.
 */
static void scheduleFinalizations(void)
{
    ReferenceTable newPendingRefs;
    LargeHeapRefTable *finRefs = gDvm.gcHeap->finalizableRefs;
    Object **ref;
    Object **lastRef;
    size_t totalPendCount;
    GcMarkContext *ctx = &gDvm.gcHeap->markContext;

    /*
     * All reachable objects have been marked.
     * Any unmarked finalizable objects need to be finalized.
     */

    /* Create a table that the new pending refs will
     * be added to.
     */
    if (!dvmHeapInitHeapRefTable(&newPendingRefs)) {
        //TODO: mark all finalizable refs and hope that
        //      we can schedule them next time.  Watch out,
        //      because we may be expecting to free up space
        //      by calling finalizers.
        LOGE_GC("scheduleFinalizations(): no room for "
                "pending finalizations");
        dvmAbort();
    }

    /* Walk through finalizableRefs and move any unmarked references
     * to the list of new pending refs.
     */
    totalPendCount = 0;
    while (finRefs != NULL) {
        Object **gapRef;
        size_t newPendCount = 0;

        gapRef = ref = finRefs->refs.table;
        lastRef = finRefs->refs.nextEntry;
        while (ref < lastRef) {
            if (!isMarked(*ref, ctx)) {
                if (!dvmAddToReferenceTable(&newPendingRefs, *ref)) {
                    //TODO: add the current table and allocate
                    //      a new, smaller one.
                    LOGE_GC("scheduleFinalizations(): "
                            "no room for any more pending finalizations: %zd",
                            dvmReferenceTableEntries(&newPendingRefs));
                    dvmAbort();
                }
                newPendCount++;
            } else {
                /* This ref is marked, so will remain on finalizableRefs.
                 */
                if (newPendCount > 0) {
                    /* Copy it up to fill the holes.
                     */
                    *gapRef++ = *ref;
                } else {
                    /* No holes yet; don't bother copying.
                     */
                    gapRef++;
                }
            }
            ref++;
        }
        finRefs->refs.nextEntry = gapRef;
        //TODO: if the table is empty when we're done, free it.
        totalPendCount += newPendCount;
        finRefs = finRefs->next;
    }
    LOGD_GC("scheduleFinalizations(): %zd finalizers triggered.",
            totalPendCount);
    if (totalPendCount == 0) {
        /* No objects required finalization.
         * Free the empty temporary table.
         */
        dvmClearReferenceTable(&newPendingRefs);
        return;
    }

    /* Add the new pending refs to the main list.
     */
    if (!dvmHeapAddTableToLargeTable(&gDvm.gcHeap->pendingFinalizationRefs,
                &newPendingRefs))
    {
        LOGE_GC("scheduleFinalizations(): can't insert new "
                "pending finalizations");
        dvmAbort();
    }

    //TODO: try compacting the main list with a memcpy loop

    /* Mark the refs we just moved;  we don't want them or their
     * children to get swept yet.
     */
    ref = newPendingRefs.table;
    lastRef = newPendingRefs.nextEntry;
    assert(ref < lastRef);
    while (ref < lastRef) {
        assert(*ref != NULL);
        markObject(*ref, ctx);
        ref++;
    }
    processMarkStack(ctx);
    dvmSignalHeapWorker(false);
}

/*
 * Process reference class instances and schedule finalizations.
 */
void dvmHeapProcessReferences(Object **softReferences, bool clearSoftRefs,
                              Object **weakReferences,
                              Object **phantomReferences)
{
    assert(softReferences != NULL);
    assert(weakReferences != NULL);
    assert(phantomReferences != NULL);
    /*
     * Unless we are in the zygote or required to clear soft
     * references with white references, preserve some white
     * referents.
     */
    if (!gDvm.zygote && !clearSoftRefs) {
        preserveSomeSoftReferences(softReferences);
    }
    /*
     * Clear all remaining soft and weak references with white
     * referents.
     */
    clearWhiteReferences(softReferences);
    clearWhiteReferences(weakReferences);
    /*
     * Preserve all white objects with finalize methods and schedule
     * them for finalization.
     */
    scheduleFinalizations();
    /*
     * Clear all f-reachable soft and weak references with white
     * referents.
     */
    clearWhiteReferences(softReferences);
    clearWhiteReferences(weakReferences);
    /*
     * Clear all phantom references with white referents.
     */
    clearWhiteReferences(phantomReferences);
    /*
     * At this point all reference lists should be empty.
     */
    assert(*softReferences == NULL);
    assert(*weakReferences == NULL);
    assert(*phantomReferences == NULL);
}

void dvmHeapFinishMarkStep()
{
    GcMarkContext *ctx;

    ctx = &gDvm.gcHeap->markContext;

    /* The mark bits are now not needed.
     */
    dvmHeapSourceZeroMarkBitmap();

    /* Clean up everything else associated with the marking process.
     */
    destroyMarkStack(&ctx->stack);

    ctx->finger = NULL;
}

typedef struct {
    size_t numObjects;
    size_t numBytes;
    bool isConcurrent;
} SweepContext;

static void sweepBitmapCallback(size_t numPtrs, void **ptrs, void *arg)
{
    SweepContext *ctx = (SweepContext *)arg;

    if (ctx->isConcurrent) {
        dvmLockHeap();
    }
    ctx->numBytes += dvmHeapSourceFreeList(numPtrs, ptrs);
    ctx->numObjects += numPtrs;
    if (ctx->isConcurrent) {
        dvmUnlockHeap();
    }
}

/*
 * Returns true if the given object is unmarked.  This assumes that
 * the bitmaps have not yet been swapped.
 */
static int isUnmarkedObject(void *object)
{
    return !isMarked((void *)((uintptr_t)object & ~(HB_OBJECT_ALIGNMENT-1)),
            &gDvm.gcHeap->markContext);
}

/*
 * Process all the internal system structures that behave like
 * weakly-held objects.
 */
void dvmHeapSweepSystemWeaks(void)
{
    dvmGcDetachDeadInternedStrings(isUnmarkedObject);
    dvmSweepMonitorList(&gDvm.monitorList, isUnmarkedObject);
}

/*
 * Walk through the list of objects that haven't been marked and free
 * them.  Assumes the bitmaps have been swapped.
 */
void dvmHeapSweepUnmarkedObjects(bool isPartial, bool isConcurrent,
                                 size_t *numObjects, size_t *numBytes)
{
    uintptr_t base[HEAP_SOURCE_MAX_HEAP_COUNT];
    uintptr_t max[HEAP_SOURCE_MAX_HEAP_COUNT];
    SweepContext ctx;
    HeapBitmap *prevLive, *prevMark;
    size_t numHeaps, numSweepHeaps;
    size_t i;

    numHeaps = dvmHeapSourceGetNumHeaps();
    dvmHeapSourceGetRegions(base, max, numHeaps);
    if (isPartial) {
        assert((uintptr_t)gDvm.gcHeap->markContext.immuneLimit == base[0]);
        numSweepHeaps = 1;
    } else {
        numSweepHeaps = numHeaps;
    }
    ctx.numObjects = ctx.numBytes = 0;
    ctx.isConcurrent = isConcurrent;
    prevLive = dvmHeapSourceGetMarkBits();
    prevMark = dvmHeapSourceGetLiveBits();
    for (i = 0; i < numSweepHeaps; ++i) {
        dvmHeapBitmapSweepWalk(prevLive, prevMark, base[i], max[i],
                               sweepBitmapCallback, &ctx);
    }
    *numObjects = ctx.numObjects;
    *numBytes = ctx.numBytes;
    if (gDvm.allocProf.enabled) {
        gDvm.allocProf.freeCount += ctx.numObjects;
        gDvm.allocProf.freeSize += ctx.numBytes;
    }
}
