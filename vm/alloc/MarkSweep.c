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
#include "alloc/clz.h"
#include "alloc/HeapBitmap.h"
#include "alloc/HeapInternal.h"
#include "alloc/HeapSource.h"
#include "alloc/MarkSweep.h"
#include "alloc/Visit.h"
#include <limits.h>     // for ULONG_MAX
#include <sys/mman.h>   // for madvise(), mmap()
#include <errno.h>

#define GC_LOG_TAG      LOG_TAG "-gc"

#if LOG_NDEBUG
#define LOGV_GC(...)    ((void)0)
#define LOGD_GC(...)    ((void)0)
#else
#define LOGV_GC(...)    LOG(LOG_VERBOSE, GC_LOG_TAG, __VA_ARGS__)
#define LOGD_GC(...)    LOG(LOG_DEBUG, GC_LOG_TAG, __VA_ARGS__)
#endif

#define LOGI_GC(...)    LOG(LOG_INFO, GC_LOG_TAG, __VA_ARGS__)
#define LOGW_GC(...)    LOG(LOG_WARN, GC_LOG_TAG, __VA_ARGS__)
#define LOGE_GC(...)    LOG(LOG_ERROR, GC_LOG_TAG, __VA_ARGS__)

#define LOG_SCAN(...)   LOGV_GC("SCAN: " __VA_ARGS__)

#define ALIGN_UP_TO_PAGE_SIZE(p) \
    (((size_t)(p) + (SYSTEM_PAGE_SIZE - 1)) & ~(SYSTEM_PAGE_SIZE - 1))

/* Do not cast the result of this to a boolean; the only set bit
 * may be > 1<<8.
 */
static inline long isMarked(const void *obj, const GcMarkContext *ctx)
{
    return dvmHeapBitmapIsObjectBitSet(ctx->bitmap, obj);
}

static bool
createMarkStack(GcMarkStack *stack)
{
    const Object **limit;
    const char *name;
    size_t size;

    /* Create a stack big enough for the worst possible case,
     * where the heap is perfectly full of the smallest object.
     * TODO: be better about memory usage; use a smaller stack with
     *       overflow detection and recovery.
     */
    size = dvmHeapSourceGetIdealFootprint() * sizeof(Object*) /
            (sizeof(Object) + HEAP_SOURCE_CHUNK_OVERHEAD);
    size = ALIGN_UP_TO_PAGE_SIZE(size);
    name = "dalvik-mark-stack";
    limit = dvmAllocRegion(size, PROT_READ | PROT_WRITE, name);
    if (limit == NULL) {
        LOGE_GC("Could not mmap %zd-byte ashmem region '%s'", size, name);
        return false;
    }
    stack->limit = limit;
    stack->base = (const Object **)((uintptr_t)limit + size);
    stack->top = stack->base;
    return true;
}

static void
destroyMarkStack(GcMarkStack *stack)
{
    munmap((char *)stack->limit,
            (uintptr_t)stack->base - (uintptr_t)stack->limit);
    memset(stack, 0, sizeof(*stack));
}

#define MARK_STACK_PUSH(stack, obj) \
    do { \
        *--(stack).top = (obj); \
    } while (false)

bool
dvmHeapBeginMarkStep(GcMode mode)
{
    GcMarkContext *mc = &gDvm.gcHeap->markContext;

    if (!createMarkStack(&mc->stack)) {
        return false;
    }
    mc->finger = NULL;
    mc->immuneLimit = dvmHeapSourceGetImmuneLimit(mode);
    return true;
}

static long
setAndReturnMarkBit(GcMarkContext *ctx, const void *obj)
{
    return dvmHeapBitmapSetAndReturnObjectBit(ctx->bitmap, obj);
}

static void
markObjectNonNull(const Object *obj, GcMarkContext *ctx,
        bool checkFinger, bool forceStack)
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
        if (forceStack || (checkFinger && (void *)obj < ctx->finger)) {
            /* This object will need to go on the mark stack.
             */
            MARK_STACK_PUSH(ctx->stack, obj);
        }

#if WITH_HPROF
        if (gDvm.gcHeap->hprofContext != NULL) {
            hprofMarkRootObject(gDvm.gcHeap->hprofContext, obj, 0);
        }
#endif
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
        markObjectNonNull(obj, ctx, true, false);
    }
}

/* If the object hasn't already been marked, mark it and
 * schedule it to be scanned for references.
 *
 * obj may not be NULL.  The macro dvmMarkObject() should
 * be used in situations where a reference may be NULL.
 *
 * This function may only be called when marking the root
 * set.  When recursing, use the internal markObject().
 */
void
dvmMarkObjectNonNull(const Object *obj)
{
    assert(obj != NULL);
    markObjectNonNull(obj, &gDvm.gcHeap->markContext, false, false);
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

    HPROF_SET_GC_SCAN_STATE(HPROF_ROOT_STICKY_CLASS, 0);

    LOG_SCAN("immune objects");
    dvmMarkImmuneObjects(gcHeap->markContext.immuneLimit);

    LOG_SCAN("root class loader\n");
    dvmGcScanRootClassLoader();
    LOG_SCAN("primitive classes\n");
    dvmGcScanPrimitiveClasses();

    /* dvmGcScanRootThreadGroups() sets a bunch of
     * different scan states internally.
     */
    HPROF_CLEAR_GC_SCAN_STATE();

    LOG_SCAN("root thread groups\n");
    dvmGcScanRootThreadGroups();

    HPROF_SET_GC_SCAN_STATE(HPROF_ROOT_INTERNED_STRING, 0);

    LOG_SCAN("interned strings\n");
    dvmGcScanInternedStrings();

    HPROF_SET_GC_SCAN_STATE(HPROF_ROOT_JNI_GLOBAL, 0);

    LOG_SCAN("JNI global refs\n");
    dvmGcMarkJniGlobalRefs();

    HPROF_SET_GC_SCAN_STATE(HPROF_ROOT_REFERENCE_CLEANUP, 0);

    LOG_SCAN("pending reference operations\n");
    dvmHeapMarkLargeTableRefs(gcHeap->referenceOperations);

    HPROF_SET_GC_SCAN_STATE(HPROF_ROOT_FINALIZING, 0);

    LOG_SCAN("pending finalizations\n");
    dvmHeapMarkLargeTableRefs(gcHeap->pendingFinalizationRefs);

    HPROF_SET_GC_SCAN_STATE(HPROF_ROOT_DEBUGGER, 0);

    LOG_SCAN("debugger refs\n");
    dvmGcMarkDebuggerRefs();

    HPROF_SET_GC_SCAN_STATE(HPROF_ROOT_VM_INTERNAL, 0);

    /* Mark any special objects we have sitting around.
     */
    LOG_SCAN("special objects\n");
    dvmMarkObjectNonNull(gDvm.outOfMemoryObj);
    dvmMarkObjectNonNull(gDvm.internalErrorObj);
    dvmMarkObjectNonNull(gDvm.noClassDefFoundErrorObj);
//TODO: scan object references sitting in gDvm;  use pointer begin & end

    HPROF_CLEAR_GC_SCAN_STATE();
}

/*
 * Nothing past this point is allowed to use dvmMarkObject() or
 * dvmMarkObjectNonNull(), which are for root-marking only.
 * Scanning/recursion must use markObject(), which takes the finger
 * into account.
 */
#undef dvmMarkObject
#define dvmMarkObject __dont_use_dvmMarkObject__
#define dvmMarkObjectNonNull __dont_use_dvmMarkObjectNonNull__

/*
 * Scans instance fields.
 */
static void scanInstanceFields(const Object *obj, GcMarkContext *ctx)
{
    assert(obj != NULL);
    assert(obj->clazz != NULL);
    assert(ctx != NULL);

    if (obj->clazz->refOffsets != CLASS_WALK_SUPER) {
        unsigned int refOffsets = obj->clazz->refOffsets;
        while (refOffsets != 0) {
            const int rshift = CLZ(refOffsets);
            refOffsets &= ~(CLASS_HIGH_BIT >> rshift);
            markObject(dvmGetFieldObject((Object*)obj,
                                          CLASS_OFFSET_FROM_CLZ(rshift)), ctx);
        }
    } else {
        ClassObject *clazz;
        int i;
        for (clazz = obj->clazz; clazz != NULL; clazz = clazz->super) {
            InstField *field = clazz->ifields;
            for (i = 0; i < clazz->ifieldRefCount; ++i, ++field) {
                void *addr = BYTE_OFFSET((Object *)obj, field->byteOffset);
                markObject(((JValue *)addr)->l, ctx);
            }
        }
    }
}

/*
 * Scans the header, static field references, and interface
 * pointers of a class object.
 */
static void scanClassObject(const ClassObject *obj, GcMarkContext *ctx)
{
    int i;

    assert(obj != NULL);
    assert(obj->obj.clazz == gDvm.classJavaLangClass);
    assert(ctx != NULL);

    markObject((Object *)obj->obj.clazz, ctx);
    if (IS_CLASS_FLAG_SET(obj, CLASS_ISARRAY)) {
        markObject((Object *)obj->elementClass, ctx);
    }
    /* Do super and the interfaces contain Objects and not dex idx values? */
    if (obj->status > CLASS_IDX) {
        markObject((Object *)obj->super, ctx);
    }
    markObject(obj->classLoader, ctx);
    /* Scan static field references. */
    for (i = 0; i < obj->sfieldCount; ++i) {
        char ch = obj->sfields[i].field.signature[0];
        if (ch == '[' || ch == 'L') {
            markObject(obj->sfields[i].value.l, ctx);
        }
    }
    /* Scan the instance fields. */
    scanInstanceFields((const Object *)obj, ctx);
    /* Scan interface references. */
    if (obj->status > CLASS_IDX) {
        for (i = 0; i < obj->interfaceCount; ++i) {
            markObject((Object *)obj->interfaces[i], ctx);
        }
    }
}

/*
 * Scans the header of all array objects.  If the array object is
 * specialized to a reference type, scans the array data as well.
 */
static void scanArrayObject(const ArrayObject *obj, GcMarkContext *ctx)
{
    size_t i;

    assert(obj != NULL);
    assert(obj->obj.clazz != NULL);
    assert(ctx != NULL);
    /* Scan the class object reference. */
    markObject((Object *)obj->obj.clazz, ctx);
    if (IS_CLASS_FLAG_SET(obj->obj.clazz, CLASS_ISOBJECTARRAY)) {
        /* Scan the array contents. */
        Object **contents = (Object **)obj->contents;
        for (i = 0; i < obj->length; ++i) {
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
static void scanDataObject(DataObject *obj, GcMarkContext *ctx)
{
    assert(obj != NULL);
    assert(obj->obj.clazz != NULL);
    assert(ctx != NULL);
    /* Scan the class object. */
    markObject((Object *)obj->obj.clazz, ctx);
    /* Scan the instance fields. */
    scanInstanceFields((const Object *)obj, ctx);
    if (IS_CLASS_FLAG_SET(obj->obj.clazz, CLASS_ISREFERENCE)) {
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
#if WITH_HPROF
    if (gDvm.gcHeap->hprofContext != NULL) {
        hprofDumpHeapObject(gDvm.gcHeap->hprofContext, obj);
    }
#endif
    /* Dispatch a type-specific scan routine. */
    if (obj->clazz == gDvm.classJavaLangClass) {
        scanClassObject((ClassObject *)obj, ctx);
    } else if (IS_CLASS_FLAG_SET(obj->clazz, CLASS_ISARRAY)) {
        scanArrayObject((ArrayObject *)obj, ctx);
    } else {
        scanDataObject((DataObject *)obj, ctx);
    }
}

/*
 * Variants for partial GC. Scan immune objects, and rebuild the card
 * table.
 */

/*
 * Mark an object which was found in an immune object.
 */
static void scanImmuneReference(const Object *obj, GcMarkContext *ctx)
{
    if (obj != NULL) {
        if (obj < (Object *)ctx->immuneLimit) {
            assert(isMarked(obj, ctx));
        } else {
            ctx->crossGen = true;
            markObjectNonNull(obj, ctx, true, false);
        }
    }
}

/*
 * Scans instance fields.
 */
static void scanImmuneInstanceFields(const Object *obj, GcMarkContext *ctx)
{
    assert(obj != NULL);
    assert(obj->clazz != NULL);
    assert(ctx != NULL);

    if (obj->clazz->refOffsets != CLASS_WALK_SUPER) {
        unsigned int refOffsets = obj->clazz->refOffsets;
        while (refOffsets != 0) {
            const int rshift = CLZ(refOffsets);
            refOffsets &= ~(CLASS_HIGH_BIT >> rshift);
            scanImmuneReference(
                dvmGetFieldObject((Object*)obj, CLASS_OFFSET_FROM_CLZ(rshift)),
                ctx);
        }
    } else {
        ClassObject *clazz;
        int i;
        for (clazz = obj->clazz; clazz != NULL; clazz = clazz->super) {
            InstField *field = clazz->ifields;
            for (i = 0; i < clazz->ifieldRefCount; ++i, ++field) {
                void *addr = BYTE_OFFSET((Object *)obj, field->byteOffset);
                scanImmuneReference(((JValue *)addr)->l, ctx);
            }
        }
    }
}

/*
 * Scans the header, static field references, and interface
 * pointers of a class object.
 */
static void scanImmuneClassObject(const ClassObject *obj, GcMarkContext *ctx)
{
    int i;

    assert(obj != NULL);
    assert(obj->obj.clazz == gDvm.classJavaLangClass);
    assert(ctx != NULL);

    scanImmuneReference((Object *)obj->obj.clazz, ctx);
    if (IS_CLASS_FLAG_SET(obj, CLASS_ISARRAY)) {
        scanImmuneReference((Object *)obj->elementClass, ctx);
    }
    /* Do super and the interfaces contain Objects and not dex idx values? */
    if (obj->status > CLASS_IDX) {
        scanImmuneReference((Object *)obj->super, ctx);
    }
    scanImmuneReference(obj->classLoader, ctx);
    /* Scan static field references. */
    for (i = 0; i < obj->sfieldCount; ++i) {
        char ch = obj->sfields[i].field.signature[0];
        if (ch == '[' || ch == 'L') {
            scanImmuneReference(obj->sfields[i].value.l, ctx);
        }
    }
    /* Scan the instance fields. */
    scanImmuneInstanceFields((const Object *)obj, ctx);
    /* Scan interface references. */
    if (obj->status > CLASS_IDX) {
        for (i = 0; i < obj->interfaceCount; ++i) {
            scanImmuneReference((Object *)obj->interfaces[i], ctx);
        }
    }
}

/*
 * Scans the header of all array objects.  If the array object is
 * specialized to a reference type, scans the array data as well.
 */
static void scanImmuneArrayObject(const ArrayObject *obj, GcMarkContext *ctx)
{
    size_t i;

    assert(obj != NULL);
    assert(obj->obj.clazz != NULL);
    assert(ctx != NULL);
    /* Scan the class object reference. */
    scanImmuneReference((Object *)obj->obj.clazz, ctx);
    if (IS_CLASS_FLAG_SET(obj->obj.clazz, CLASS_ISOBJECTARRAY)) {
        /* Scan the array contents. */
        Object **contents = (Object **)obj->contents;
        for (i = 0; i < obj->length; ++i) {
            scanImmuneReference(contents[i], ctx);
        }
    }
}

/*
 * Scans the header and field references of a data object.
 */
static void scanImmuneDataObject(DataObject *obj, GcMarkContext *ctx)
{
    assert(obj != NULL);
    assert(obj->obj.clazz != NULL);
    assert(ctx != NULL);
    /* Scan the class object. */
    scanImmuneReference((Object *)obj->obj.clazz, ctx);
    /* Scan the instance fields. */
    scanImmuneInstanceFields((const Object *)obj, ctx);
    if (IS_CLASS_FLAG_SET(obj->obj.clazz, CLASS_ISREFERENCE)) {
        scanImmuneReference((Object *)obj, ctx);
    }
}

/*
 * Scans an object reference.  Determines the type of the reference
 * and dispatches to a specialized scanning routine.
 */
static void scanImmuneObject(const Object *obj, GcMarkContext *ctx)
{
    assert(obj != NULL);
    assert(obj->clazz != NULL);
    assert(ctx != NULL);
    assert(obj < (Object *)ctx->immuneLimit);

#if WITH_HPROF
    if (gDvm.gcHeap->hprofContext != NULL) {
        hprofDumpHeapObject(gDvm.gcHeap->hprofContext, obj);
    }
#endif
    /* Dispatch a type-specific scan routine. */
    if (obj->clazz == gDvm.classJavaLangClass) {
        scanImmuneClassObject((ClassObject *)obj, ctx);
    } else if (IS_CLASS_FLAG_SET(obj->clazz, CLASS_ISARRAY)) {
        scanImmuneArrayObject((ArrayObject *)obj, ctx);
    } else {
        scanImmuneDataObject((DataObject *)obj, ctx);
    }
}

static void
processMarkStack(GcMarkContext *ctx)
{
    const Object **const base = ctx->stack.base;

    /* Scan anything that's on the mark stack.
     * We can't use the bitmaps anymore, so use
     * a finger that points past the end of them.
     */
    ctx->finger = (void *)ULONG_MAX;
    while (ctx->stack.top != base) {
        scanObject(*ctx->stack.top++, ctx);
    }
}

#ifndef NDEBUG
static uintptr_t gLastFinger = 0;
#endif

static bool
scanBitmapCallback(size_t numPtrs, void **ptrs, const void *finger, void *arg)
{
    GcMarkContext *ctx = (GcMarkContext *)arg;
    size_t i;

#ifndef NDEBUG
    assert((uintptr_t)finger >= gLastFinger);
    gLastFinger = (uintptr_t)finger;
#endif

    ctx->finger = finger;
    for (i = 0; i < numPtrs; i++) {
        scanObject(*ptrs++, ctx);
    }

    return true;
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
#ifndef NDEBUG
    gLastFinger = 0;
#endif
    if (gDvm.executionMode == kExecutionModeInterpPortable) {
        /* The portable interpreter dirties cards on write; other
         * modes do not yet do so.
         * TODO: Bring the fast interpreter and JIT into the fold.
         */
        HeapBitmap markBits[HEAP_SOURCE_MAX_HEAP_COUNT];
        HeapBitmap liveBits[HEAP_SOURCE_MAX_HEAP_COUNT];
        size_t numBitmaps, i;
        numBitmaps = dvmHeapSourceGetNumHeaps();
        dvmHeapSourceGetObjectBitmaps(liveBits, markBits, numBitmaps);
        for (i = 0; i < numBitmaps; i++) {
            /* The use of finger to tell visited from unvisited objects
             * requires we walk the bitmaps from low to high
             * addresses. This code assumes [and asserts] that the order
             * of the heaps returned is the reverse of that.
             */
            size_t j = numBitmaps-1-i;
            assert(j == 0 || (markBits[j].base < markBits[j-1].base));
            if (markBits[j].base < (uintptr_t)ctx->immuneLimit) {
                uintptr_t minAddr = markBits[j].base;
                uintptr_t maxAddr = markBits[j].base +
                    HB_MAX_OFFSET(&markBits[j]);
                u1 *minCard = dvmCardFromAddr((void *)minAddr);
                u1 *maxCard = dvmCardFromAddr((void *)maxAddr);

                u1 *card;
                /* TODO: This double-loop should be made faster. In
                 * particular the inner loop could get in bed with the
                 * bitmap scanning routines.
                 */
                for (card = minCard; card <= maxCard; card++) {
                    if (*card == GC_CARD_DIRTY) {
                        uintptr_t addr = (uintptr_t)dvmAddrFromCard(card);
                        uintptr_t endAddr = addr + GC_CARD_SIZE;
                        ctx->crossGen  = false;
                        for ( ; addr < endAddr; addr += 8) {
                            if (dvmIsValidObject((void *)addr)) {
                                scanImmuneObject((void *)addr, ctx);
                            }
                        }
                        if (! ctx->crossGen) {
                            *card = GC_CARD_CLEAN;
                        }
                    }
                }
            } else {
                dvmHeapBitmapWalk(&markBits[j], scanBitmapCallback, ctx);
            }
        }
    } else {
        dvmHeapBitmapWalk(ctx->bitmap, scanBitmapCallback, ctx);
    }
    /* We've walked the mark bitmaps.  Scan anything that's
     * left on the mark stack.
     */
    processMarkStack(ctx);

    LOG_SCAN("done with marked objects\n");
}

/*
 * Callback applied to each gray object to blacken it.
 */
static bool dirtyObjectCallback(size_t numPtrs, void **ptrs,
                                const void *finger, void *arg)
{
    size_t i;

    for (i = 0; i < numPtrs; ++i) {
        scanObject(ptrs[i], arg);
    }
    return true;
}

/*
 * Re-mark dirtied objects.  Iterates through all blackened objects
 * looking for references to white objects.
 */
void dvmMarkDirtyObjects(void)
{
    HeapBitmap markBits[HEAP_SOURCE_MAX_HEAP_COUNT];
    HeapBitmap liveBits[HEAP_SOURCE_MAX_HEAP_COUNT];
    GcMarkContext *ctx;
    size_t numBitmaps;
    size_t i;

    ctx = &gDvm.gcHeap->markContext;
    /*
     * The finger must have been set to the maximum value to ensure
     * that gray objects will be pushed onto the mark stack.
     */
    assert(ctx->finger == (void *)ULONG_MAX);
    numBitmaps = dvmHeapSourceGetNumHeaps();
    dvmHeapSourceGetObjectBitmaps(liveBits, markBits, numBitmaps);
    for (i = 0; i < numBitmaps; i++) {
        dvmHeapBitmapWalk(&markBits[i], dirtyObjectCallback, ctx);
    }
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
void dvmHandleSoftRefs(Object **list)
{
    GcMarkContext *markContext;
    Object *ref, *referent;
    Object *clear;
    size_t referentOffset;
    size_t counter;
    bool marked;

    markContext = &gDvm.gcHeap->markContext;
    referentOffset = gDvm.offJavaLangRefReference_referent;
    clear = NULL;
    counter = 0;
    while (*list != NULL) {
        ref = dequeuePendingReference(list);
        referent = dvmGetFieldObject(ref, referentOffset);
        assert(referent != NULL);
        marked = isMarked(referent, markContext);
        if (!marked && ((++counter) & 1)) {
            /* Referent is white and biased toward saving, mark it. */
            markObject(referent, markContext);
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
    processMarkStack(markContext);
}

/*
 * Unlink the reference list clearing references objects with white
 * referents.  Cleared references registered to a reference queue are
 * scheduled for appending by the heap worker thread.
 */
void dvmClearWhiteRefs(Object **list)
{
    GcMarkContext *markContext;
    Object *ref, *referent;
    size_t referentOffset;
    bool doSignal;

    markContext = &gDvm.gcHeap->markContext;
    referentOffset = gDvm.offJavaLangRefReference_referent;
    doSignal = false;
    while (*list != NULL) {
        ref = dequeuePendingReference(list);
        referent = dvmGetFieldObject(ref, referentOffset);
        assert(referent != NULL);
        if (!isMarked(referent, markContext)) {
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
void dvmHeapScheduleFinalizations()
{
    HeapRefTable newPendingRefs;
    LargeHeapRefTable *finRefs = gDvm.gcHeap->finalizableRefs;
    Object **ref;
    Object **lastRef;
    size_t totalPendCount;
    GcMarkContext *markContext = &gDvm.gcHeap->markContext;

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
        LOGE_GC("dvmHeapScheduleFinalizations(): no room for "
                "pending finalizations\n");
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
            if (!isMarked(*ref, markContext)) {
                if (!dvmHeapAddToHeapRefTable(&newPendingRefs, *ref)) {
                    //TODO: add the current table and allocate
                    //      a new, smaller one.
                    LOGE_GC("dvmHeapScheduleFinalizations(): "
                            "no room for any more pending finalizations: %zd\n",
                            dvmHeapNumHeapRefTableEntries(&newPendingRefs));
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
    LOGD_GC("dvmHeapScheduleFinalizations(): %zd finalizers triggered.\n",
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
        LOGE_GC("dvmHeapScheduleFinalizations(): can't insert new "
                "pending finalizations\n");
        dvmAbort();
    }

    //TODO: try compacting the main list with a memcpy loop

    /* Mark the refs we just moved;  we don't want them or their
     * children to get swept yet.
     */
    ref = newPendingRefs.table;
    lastRef = newPendingRefs.nextEntry;
    assert(ref < lastRef);
    HPROF_SET_GC_SCAN_STATE(HPROF_ROOT_FINALIZING, 0);
    while (ref < lastRef) {
        assert(*ref != NULL);
        markObject(*ref, markContext);
        ref++;
    }
    HPROF_CLEAR_GC_SCAN_STATE();
    processMarkStack(markContext);
    dvmSignalHeapWorker(false);
}

void dvmHeapFinishMarkStep()
{
    GcMarkContext *markContext;

    markContext = &gDvm.gcHeap->markContext;

    /* The sweep step freed every object that appeared in the
     * HeapSource bitmaps that didn't appear in the mark bitmaps.
     * The new state of the HeapSource is exactly the final
     * mark bitmaps, so swap them in.
     */
    dvmHeapSourceSwapBitmaps();

    /* Clean up everything else associated with the marking process.
     */
    destroyMarkStack(&markContext->stack);

    markContext->finger = NULL;
}

static bool
sweepBitmapCallback(size_t numPtrs, void **ptrs, const void *finger, void *arg)
{
    const ClassObject *const classJavaLangClass = gDvm.classJavaLangClass;
    const bool overwriteFree = gDvm.overwriteFree;
    size_t i;
    void **origPtrs = ptrs;

    for (i = 0; i < numPtrs; i++) {
        Object *obj;

        obj = (Object *)*ptrs++;

        /* This assumes that java.lang.Class will never go away.
         * If it can, and we were the last reference to it, it
         * could have already been swept.  However, even in that case,
         * gDvm.classJavaLangClass should still have a useful
         * value.
         */
        if (obj->clazz == classJavaLangClass) {
            /* dvmFreeClassInnards() may have already been called,
             * but it's safe to call on the same ClassObject twice.
             */
            dvmFreeClassInnards((ClassObject *)obj);
        }

        /* Overwrite the to-be-freed object to make stale references
         * more obvious.
         */
        if (overwriteFree) {
            int objlen;
            ClassObject *clazz = obj->clazz;
            objlen = dvmHeapSourceChunkSize(obj);
            memset(obj, 0xa5, objlen);
            obj->clazz = (ClassObject *)((uintptr_t)clazz ^ 0xffffffff);
        }
    }
    // TODO: dvmHeapSourceFreeList has a loop, just like the above
    // does. Consider collapsing the two loops to save overhead.
    dvmHeapSourceFreeList(numPtrs, origPtrs);

    return true;
}

/* Returns true if the given object is unmarked.  Ignores the low bits
 * of the pointer because the intern table may set them.
 */
static int isUnmarkedObject(void *object)
{
    return !isMarked((void *)((uintptr_t)object & ~(HB_OBJECT_ALIGNMENT-1)),
            &gDvm.gcHeap->markContext);
}

/* Walk through the list of objects that haven't been
 * marked and free them.
 */
void
dvmHeapSweepUnmarkedObjects(GcMode mode, int *numFreed, size_t *sizeFreed)
{
    HeapBitmap markBits[HEAP_SOURCE_MAX_HEAP_COUNT];
    HeapBitmap liveBits[HEAP_SOURCE_MAX_HEAP_COUNT];
    size_t origObjectsAllocated;
    size_t origBytesAllocated;
    size_t numBitmaps, numSweepBitmaps;
    size_t i;

    /* All reachable objects have been marked.
     * Detach any unreachable interned strings before
     * we sweep.
     */
    dvmGcDetachDeadInternedStrings(isUnmarkedObject);

    /* Free any known objects that are not marked.
     */
    origObjectsAllocated = dvmHeapSourceGetValue(HS_OBJECTS_ALLOCATED, NULL, 0);
    origBytesAllocated = dvmHeapSourceGetValue(HS_BYTES_ALLOCATED, NULL, 0);

    dvmSweepMonitorList(&gDvm.monitorList, isUnmarkedObject);

    numBitmaps = dvmHeapSourceGetNumHeaps();
    dvmHeapSourceGetObjectBitmaps(liveBits, markBits, numBitmaps);
    if (mode == GC_PARTIAL) {
        numSweepBitmaps = 1;
        assert((uintptr_t)gDvm.gcHeap->markContext.immuneLimit == liveBits[0].base);
    } else {
        numSweepBitmaps = numBitmaps;
    }
    for (i = 0; i < numSweepBitmaps; i++) {
        dvmHeapBitmapXorWalk(&markBits[i], &liveBits[i],
                             sweepBitmapCallback, NULL);
    }

    *numFreed = origObjectsAllocated -
            dvmHeapSourceGetValue(HS_OBJECTS_ALLOCATED, NULL, 0);
    *sizeFreed = origBytesAllocated -
            dvmHeapSourceGetValue(HS_BYTES_ALLOCATED, NULL, 0);

#ifdef WITH_PROFILER
    if (gDvm.allocProf.enabled) {
        gDvm.allocProf.freeCount += *numFreed;
        gDvm.allocProf.freeSize += *sizeFreed;
    }
#endif
}
