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
#include "alloc/VisitInlines.h"
#include <limits.h>     // for ULONG_MAX
#include <sys/mman.h>   // for madvise(), mmap()
#include <errno.h>

typedef unsigned long Word;
const size_t kWordSize = sizeof(Word);

/*
 * Returns true if the given object is marked.
 */
static bool isMarked(const Object *obj, const GcMarkContext *ctx)
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

/*
 * Visits all objects that start on the given card.
 */
static void visitCard(Visitor *visitor, u1 *card, void *arg)
{
    assert(visitor != NULL);
    assert(card != NULL);
    assert(dvmIsValidCard(card));
    u1 *addr= (u1*)dvmAddrFromCard(card);
    u1 *limit = addr + GC_CARD_SIZE;
    for (; addr < limit; addr += HB_OBJECT_ALIGNMENT) {
        Object *obj = (Object *)addr;
        GcMarkContext *ctx = &gDvm.gcHeap->markContext;
        if (isMarked(obj, ctx)) {
            (*visitor)(obj, arg);
        }
    }
}

/*
 * Visits objects on dirty cards marked the mod union table.
 */
static void visitModUnionTable(Visitor *visitor, u1 *base, u1 *limit, void *arg)
{
    assert(visitor != NULL);
    assert(base != NULL);
    assert(limit != NULL);
    assert(base <= limit);
    u1 *heapBase = (u1*)dvmHeapSourceGetBase();
    /* compute the start address in the bit table */
    assert(base >= heapBase);
    u4 *bits = (u4*)gDvm.gcHeap->modUnionTableBase;
    /* compute the end address in the bit table */
    size_t byteLength = (limit - base) / GC_CARD_SIZE / CHAR_BIT;
    assert(byteLength <= gDvm.gcHeap->modUnionTableLength);
    assert(byteLength % sizeof(*bits) == 0);
    size_t wordLength = byteLength / sizeof(*bits);
    size_t i;
    for (i = 0; i < wordLength; ++i) {
        if (bits[i] == 0) {
            continue;
        }
        u4 word = bits[i];
        bits[i] = 0;
        size_t j = 0;
        for (j = 0; j < sizeof(u4)*CHAR_BIT; ++j) {
            if (word & (1 << j)) {
                /* compute the base of the card */
                size_t offset = (i*sizeof(u4)*CHAR_BIT + j) * GC_CARD_SIZE;
                u1* addr = heapBase + offset;
                u1* card = dvmCardFromAddr(addr);
                /* visit all objects on the card */
                visitCard(visitor, card, arg);
            }
        }
    }
}

/*
 * Visits objects on dirty cards marked in the card table.
 */
static void visitCardTable(Visitor *visitor, u1 *base, u1 *limit, void *arg)
{
    assert(visitor != NULL);
    assert(base != NULL);
    assert(limit != NULL);
    u1 *start = dvmCardFromAddr(base);
    u1 *end = dvmCardFromAddr(limit);
    while (start < end) {
        u1 *dirty = (u1 *)memchr(start, GC_CARD_DIRTY, end - start);
        if (dirty == NULL) {
            break;
        }
        assert(dirty >= start);
        assert(dirty <= end);
        assert(dvmIsValidCard(dirty));
        visitCard(visitor, dirty, arg);
        start = dirty + 1;
    }
}

typedef struct {
    Object *threatenBoundary;
    Object *currObject;
} ScanImmuneObjectContext;

/*
 * Marks the referent of an immune object it is threatened.
 */
static void scanImmuneObjectReferent(void *addr, void *arg)
{
    assert(addr != NULL);
    assert(arg != NULL);
    Object *obj = *(Object **)addr;
    ScanImmuneObjectContext *ctx = (ScanImmuneObjectContext *)arg;
    if (obj == NULL) {
        return;
    }
    if (obj >= ctx->threatenBoundary) {
        /* TODO: set a bit in the mod union table instead. */
        dvmMarkCard(ctx->currObject);
        markObjectNonNull(obj, &gDvm.gcHeap->markContext, false);
   }
}

/*
 * This function is poorly named, as is its callee.
 */
static void scanImmuneObject(void *addr, void *arg)
{
    ScanImmuneObjectContext *ctx = (ScanImmuneObjectContext *)arg;
    Object *obj = (Object *)addr;
    ctx->currObject = obj;
    visitObject(scanImmuneObjectReferent, obj, arg);
}

/*
 * Verifies that immune objects have their referents marked.
 */
static void verifyImmuneObjectsVisitor(void *addr, void *arg)
{
    assert(addr != NULL);
    assert(arg != NULL);
    Object *obj = *(Object **)addr;
    GcMarkContext *ctx = (GcMarkContext *)arg;
    if (obj == NULL || obj < (Object *)ctx->immuneLimit) {
        return;
    }
    assert(dvmIsValidObject(obj));
    if (!isMarked(obj, ctx)) {
        LOGE("Immune reference %p points to a white threatened object %p",
             addr, obj);
        dvmAbort();
    }
}

/*
 * Visitor that searches for immune objects and verifies that all
 * threatened referents are marked.
 */
static void verifyImmuneObjectsCallback(void *addr, void *arg)
{
    assert(addr != NULL);
    assert(arg != NULL);
    Object *obj = (Object *)addr;
    GcMarkContext *ctx = (GcMarkContext *)arg;
    if (obj->clazz == NULL) {
        LOGI("uninitialized object @ %p (has null clazz pointer)", obj);
        return;
    }
    if (obj < (Object *)ctx->immuneLimit) {
        visitObject(verifyImmuneObjectsVisitor, obj, ctx);
    }
}

/*
 * Verify that immune objects refer to marked objects.
 */
static void verifyImmuneObjects()
{
    const HeapBitmap *bitmap = dvmHeapSourceGetLiveBits();
    GcMarkContext *ctx = &gDvm.gcHeap->markContext;
    dvmHeapBitmapWalk(bitmap, verifyImmuneObjectsCallback, ctx);
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
 * - Objects in debugger object registry
 *
 * Don't need:
 * - Native stack (for in-progress stuff in the VM)
 *   - The TrackedAlloc stuff watches all native VM references.
 */

/*
 * Blackens the root set.
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
                CLASS_ISFINALIZERREFERENCE |
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
 * Returns true if the object derives from FinalizerReference.
 */
static bool isFinalizerReference(const Object *obj)
{
    return referenceClassFlags(obj) & CLASS_ISFINALIZERREFERENCE;
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
        } else if (isFinalizerReference(obj)) {
            list = &gcHeap->finalizerReferences;
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
    assert(isMarked(obj, ctx));
    assert(obj->clazz != NULL);
    assert(isMarked(obj, ctx));
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

/*
 * Blackens gray objects found on dirty cards.
 */
static void scanGrayObjects(GcMarkContext *ctx)
{
    HeapBitmap *bitmap = ctx->bitmap;
    u1 *base = (u1 *)bitmap->base;
    u1 *limit = (u1 *)ALIGN_UP(bitmap->max, GC_CARD_SIZE);
    visitCardTable((Visitor *)scanObject, base, limit, ctx);
}

/*
 * Iterate through the immune objects and mark their referents.  Uses
 * the mod union table to save scanning time.
 */
void dvmHeapScanImmuneObjects(const GcMarkContext *ctx)
{
    ScanImmuneObjectContext scanCtx;
    memset(&scanCtx, 0, sizeof(scanCtx));
    scanCtx.threatenBoundary = (Object*)ctx->immuneLimit;
    visitModUnionTable(scanImmuneObject,
                       (u1*)ctx->bitmap->base, (u1*)ctx->immuneLimit,
                       (void *)&scanCtx);
    if (gDvm.verifyCardTable) {
        verifyImmuneObjects();
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
void dvmHeapScanMarkedObjects(bool isPartial)
{
    GcMarkContext *ctx = &gDvm.gcHeap->markContext;

    assert(ctx != NULL);
    assert(ctx->finger == NULL);

    u1 *start;
    if (isPartial && dvmHeapSourceGetNumHeaps() > 1) {
        dvmHeapScanImmuneObjects(ctx);
        start = (u1 *)ctx->immuneLimit;
    } else {
        start = (u1*)ctx->bitmap->base;
    }
    /*
     * All objects reachable from the root set have a bit set in the
     * mark bitmap.  Walk the mark bitmap and blacken these objects.
     */
    dvmHeapBitmapScanWalk(ctx->bitmap,
                          (uintptr_t)start, ctx->bitmap->max,
                          scanBitmapCallback,
                          ctx);

    ctx->finger = (void *)ULONG_MAX;

    /* Process gray objects until the mark stack it is empty. */
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
    enqueuePendingReference(ref, &gDvm.gcHeap->clearedReferences);
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

    ctx = &gDvm.gcHeap->markContext;
    referentOffset = gDvm.offJavaLangRefReference_referent;
    while (*list != NULL) {
        ref = dequeuePendingReference(list);
        referent = dvmGetFieldObject(ref, referentOffset);
        if (referent != NULL && !isMarked(referent, ctx)) {
            /* Referent is white, clear it. */
            clearReference(ref);
            if (isEnqueuable(ref)) {
                enqueueReference(ref);
            }
        }
    }
    assert(*list == NULL);
}

/*
 * Enqueues finalizer references with white referents.  White
 * referents are blackened, moved to the zombie field, and the
 * referent field is cleared.
 */
static void enqueueFinalizerReferences(Object **list)
{
    GcMarkContext *ctx = &gDvm.gcHeap->markContext;
    size_t referentOffset = gDvm.offJavaLangRefReference_referent;
    size_t zombieOffset = gDvm.offJavaLangRefFinalizerReference_zombie;
    bool hasEnqueued = false;
    while (*list != NULL) {
        Object *ref = dequeuePendingReference(list);
        Object *referent = dvmGetFieldObject(ref, referentOffset);
        if (referent != NULL && !isMarked(referent, ctx)) {
            markObject(referent, ctx);
            /* If the referent is non-null the reference must queuable. */
            assert(isEnqueuable(ref));
            dvmSetFieldObject(ref, zombieOffset, referent);
            clearReference(ref);
            enqueueReference(ref);
            hasEnqueued = true;
        }
    }
    if (hasEnqueued) {
        processMarkStack(ctx);
    }
    assert(*list == NULL);
}

/*
 * This object is an instance of a class that overrides finalize().  Mark
 * it as finalizable.
 *
 * This is called when Object.<init> completes normally.  It's also
 * called for clones of finalizable objects.
 */
void dvmSetFinalizable(Object *obj)
{
    Thread *self = dvmThreadSelf();
    assert(self != NULL);
    Method *meth = gDvm.methJavaLangRefFinalizerReferenceAdd;
    assert(meth != NULL);
    JValue unusedResult;
    dvmCallMethod(self, meth, NULL, &unusedResult, obj);
}

/*
 * Process reference class instances and schedule finalizations.
 */
void dvmHeapProcessReferences(Object **softReferences, bool clearSoftRefs,
                              Object **weakReferences,
                              Object **finalizerReferences,
                              Object **phantomReferences)
{
    assert(softReferences != NULL);
    assert(weakReferences != NULL);
    assert(finalizerReferences != NULL);
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
    enqueueFinalizerReferences(finalizerReferences);
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
    assert(*finalizerReferences == NULL);
    assert(*phantomReferences == NULL);
}

/*
 * Pushes a list of cleared references out to the managed heap.
 */
void dvmEnqueueClearedReferences(Object **cleared)
{
    assert(cleared != NULL);
    if (*cleared != NULL) {
        Thread *self = dvmThreadSelf();
        assert(self != NULL);
        Method *meth = gDvm.methJavaLangRefReferenceQueueAdd;
        assert(meth != NULL);
        JValue unused;
        Object *reference = *cleared;
        dvmCallMethod(self, meth, NULL, &unused, reference);
        *cleared = NULL;
    }
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
static int isUnmarkedObject(void *obj)
{
    return !isMarked((Object *)obj, &gDvm.gcHeap->markContext);
}

void sweepWeakJniGlobals(void)
{
    IndirectRefTable *table = &gDvm.jniWeakGlobalRefTable;
    Object **entry = table->table;
    GcMarkContext *ctx = &gDvm.gcHeap->markContext;
    int numEntries = dvmIndirectRefTableEntries(table);
    int i;
    for (i = 0; i < numEntries; ++i) {
        if (entry[i] != NULL && !isMarked(entry[i], ctx)) {
            entry[i] = NULL;
        }
    }
}

/*
 * Process all the internal system structures that behave like
 * weakly-held objects.
 */
void dvmHeapSweepSystemWeaks(void)
{
    dvmGcDetachDeadInternedStrings(isUnmarkedObject);
    dvmSweepMonitorList(&gDvm.monitorList, isUnmarkedObject);
    sweepWeakJniGlobals();
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
    dvmHeapSourceGetRegions(base, max, NULL, numHeaps);
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
