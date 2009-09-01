/*
 * Copyright (C) 2009 The Android Open Source Project
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

#ifndef _DALVIK_INDIRECTREFTABLE
#define _DALVIK_INDIRECTREFTABLE
/*
 * Maintain a table of indirect references.  Used for local/global JNI
 * references.
 *
 * The table contains object references that are part of the GC root set.
 * When an object is added we return an IndirectRef that is not a valid
 * pointer but can be used to find the original value in O(1) time.
 * Conversions to and from indirect refs are performed on JNI method calls
 * in and out of the VM, so they need to be very fast.
 *
 * To be efficient for JNI local variable storage, we need to provide
 * operations that allow us to operate on segments of the table, where
 * segments are pushed and popped as if on a stack.  For example, deletion
 * of an entry should only succeed if it appears in the current segment,
 * and we want to be able to strip off the current segment quickly when
 * a method returns.  Additions to the table must be made in the current
 * segment even if space is available in an earlier area.
 *
 * A new segment is created when we call into native code from interpreted
 * code, or when we handle the JNI PushLocalFrame function.
 *
 * The GC must be able to scan the entire table quickly.
 *
 * In summary, these must be very fast:
 *  - adding or removing a segment
 *  - adding references to a new segment
 *  - converting an indirect reference back to an Object
 * These can be a little slower, but must still be pretty quick:
 *  - adding references to a "mature" segment
 *  - removing individual references
 *  - scanning the entire table straight through
 *
 * If there's more than one segment, we don't guarantee that the table
 * will fill completely before we fail due to lack of space.  We do ensure
 * that the current segment will pack tightly, which should satisfy JNI
 * requirements (e.g. EnsureLocalCapacity).
 *
 * To make everything fit nicely in 32-bit integers, the maximum size of
 * the table is capped at 64K.
 *
 * None of the table functions are synchronized.
 */

/*
 * Indirect reference definition.  This must be interchangeable with JNI's
 * jobject, and it's convenient to let null be null, so we use void*.
 *
 * We need a 16-bit table index and a 2-bit reference type (global, local,
 * weak global).  Real object pointers will have zeroes in the low 2 or 3
 * bits (4- or 8-byte alignment), so it's useful to put the ref type
 * in the low bits and reserve zero as an invalid value.
 *
 * The remaining 14 bits can be used to detect stale indirect references.
 * For example, if objects don't move, we can use a hash of the original
 * Object* to make sure the entry hasn't been re-used.  (If the Object*
 * we find there doesn't match because of heap movement, we could do a
 * secondary check on the preserved hash value; this implies that creating
 * a global/local ref queries the hash value and forces it to be saved.)
 * This is only done when CheckJNI is enabled.
 *
 * A more rigorous approach would be to put a serial number in the extra
 * bits, and keep a copy of the serial number in a parallel table.  This is
 * easier when objects can move, but requires 2x the memory and additional
 * memory accesses on add/get.  It will catch additional problems, e.g.:
 * create iref1 for obj, delete iref1, create iref2 for same obj, lookup
 * iref1.  A pattern based on object bits will miss this.
 */
typedef void* IndirectRef;

/*
 * Indirect reference kind, used as the two low bits of IndirectRef.
 *
 * For convenience these match up with enum jobjectRefType from jni.h.
 */
typedef enum IndirectRefKind {
    kIndirectKindInvalid    = 0,
    kIndirectKindLocal      = 1,
    kIndirectKindGlobal     = 2,
    kIndirectKindWeakGlobal = 3
} IndirectRefKind;

/*
 * Extended debugging structure.  We keep a parallel array of these, one
 * per slot in the table.
 */
#define kIRTPrevCount   4
typedef struct IndirectRefSlot {
    u4          serial;         /* slot serial */
    Object*     previous[kIRTPrevCount];
} IndirectRefSlot;

/*
 * Table definition.
 *
 * For the global reference table, the expected common operations are
 * adding a new entry and removing a recently-added entry (usually the
 * most-recently-added entry).  For JNI local references, the common
 * operations are adding a new entry and removing an entire table segment.
 *
 * If "allocEntries" is not equal to "maxEntries", the table may expand
 * when entries are added, which means the memory may move.  If you want
 * to keep pointers into "table" rather than offsets, you must use a
 * fixed-size table.
 *
 * If we delete entries from the middle of the list, we will be left with
 * "holes".  We track the number of holes so that, when adding new elements,
 * we can quickly decide to do a trivial append or go slot-hunting.
 *
 * When the top-most entry is removed, any holes immediately below it are
 * also removed.  Thus, deletion of an entry may reduce "topIndex" by more
 * than one.
 *
 * To get the desired behavior for JNI locals, we need to know the bottom
 * and top of the current "segment".  The top is managed internally, and
 * the bottom is passed in as a function argument (the VM keeps it in a
 * slot in the interpreted stack frame).  When we call a native method or
 * push a local frame, the current top index gets pushed on, and serves
 * as the new bottom.  When we pop a frame off, the value from the stack
 * becomes the new top index, and the value stored in the previous frame
 * becomes the new bottom.
 *
 * To avoid having to re-scan the table after a pop, we want to push the
 * number of holes in the table onto the stack.  Because of our 64K-entry
 * cap, we can combine the two into a single unsigned 32-bit value.
 * Instead of a "bottom" argument we take a "cookie", which includes the
 * bottom index and the count of holes below the bottom.
 *
 * We need to minimize method call/return overhead.  If we store the
 * "cookie" externally, on the interpreted call stack, the VM can handle
 * pushes and pops with a single 4-byte load and store.  (We could also
 * store it internally in a public structure, but the local JNI refs are
 * logically tied to interpreted stack frames anyway.)
 *
 * Common alternative implementation: make IndirectRef a pointer to the
 * actual reference slot.  Instead of getting a table and doing a lookup,
 * the lookup can be done instantly.  Operations like determining the
 * type and deleting the reference are more expensive because the table
 * must be hunted for (i.e. you have to do a pointer comparison to see
 * which table it's in), you can't move the table when expanding it (so
 * realloc() is out), and tricks like serial number checking to detect
 * stale references aren't possible (though we may be able to get similar
 * benefits with other approaches).
 *
 * TODO: consider a "lastDeleteIndex" for quick hole-filling when an
 * add immediately follows a delete; must invalidate after segment pop
 * (which could increase the cost/complexity of method call/return).
 * Might be worth only using it for JNI globals.
 *
 * TODO: may want completely different add/remove algorithms for global
 * and local refs to improve performance.  A large circular buffer might
 * reduce the amortized cost of adding global references.
 *
 * TODO: if we can guarantee that the underlying storage doesn't move,
 * e.g. by using oversized mmap regions to handle expanding tables, we may
 * be able to avoid having to synchronize lookups.  Might make sense to
 * add a "synchronized lookup" call that takes the mutex as an argument,
 * and either locks or doesn't lock based on internal details.
 */
typedef union IRTSegmentState {
    u4          all;
    struct {
        u4      topIndex:16;            /* index of first unused entry */
        u4      numHoles:16;            /* #of holes in entire table */
    } parts;
} IRTSegmentState;
typedef struct IndirectRefTable {
    /* semi-public - read/write by interpreter in native call handler */
    IRTSegmentState segmentState;

    /* semi-public - read-only during GC scan; pointer must not be kept */
    Object**        table;              /* bottom of the stack */

    /* private */
    IndirectRefSlot* slotData;          /* extended debugging info */
    int             allocEntries;       /* #of entries we have space for */
    int             maxEntries;         /* max #of entries allowed */
    IndirectRefKind kind;               /* bit mask, ORed into all irefs */

    // TODO: want hole-filling stats (#of holes filled, total entries scanned)
    //       for performance evaluation.
} IndirectRefTable;

/* use as initial value for "cookie", and when table has only one segment */
#define IRT_FIRST_SEGMENT   0

/*
 * (This is PRIVATE, but we want it inside other inlines in this header.)
 *
 * Indirectify the object.
 *
 * The object pointer itself is subject to relocation in some GC
 * implementations, so we shouldn't really be using it here.
 */
INLINE IndirectRef dvmObjectToIndirectRef(IndirectRefTable* pRef,
    Object* obj, u4 tableIndex, IndirectRefKind kind)
{
    assert(tableIndex < 65536);
    //u4 objChunk = (((u4) obj >> 3) ^ ((u4) obj >> 19)) & 0x3fff;
    //u4 uref = objChunk << 18 | (tableIndex << 2) | kind;
    u4 serialChunk = pRef->slotData[tableIndex].serial;
    u4 uref = serialChunk << 20 | (tableIndex << 2) | kind;
    return (IndirectRef) uref;
}

/*
 * (This is PRIVATE, but we want it inside other inlines in this header.)
 *
 * Extract the table index from an indirect reference.
 */
INLINE u4 dvmIndirectRefToIndex(IndirectRef iref)
{
    u4 uref = (u4) iref;
    return (uref >> 2) & 0xffff;
}

/*
 * Determine what kind of indirect reference this is.
 */
INLINE IndirectRefKind dvmGetIndirectRefType(IndirectRef iref)
{
    return (u4) iref & 0x03;
}

/*
 * Initialize an IndirectRefTable.
 *
 * If "initialCount" != "maxCount", the table will expand as required.
 *
 * "kind" should be Local or Global.  The Global table may also hold
 * WeakGlobal refs.
 *
 * Returns "false" if table allocation fails.
 */
bool dvmInitIndirectRefTable(IndirectRefTable* pRef, int initialCount,
    int maxCount, IndirectRefKind kind);

/*
 * Clear out the contents, freeing allocated storage.  Does not free "pRef".
 *
 * You must call dvmInitReferenceTable() before you can re-use this table.
 */
void dvmClearIndirectRefTable(IndirectRefTable* pRef);

/*
 * Start a new segment at the top of the table.
 *
 * Returns an opaque 32-bit value that must be provided when the segment
 * is to be removed.
 *
 * IMPORTANT: this is implemented as a single instruction in mterp, rather
 * than a call here.  You can add debugging aids for the C-language
 * interpreters, but the basic implementation may not change.
 */
INLINE u4 dvmPushIndirectRefTableSegment(IndirectRefTable* pRef)
{
    return pRef->segmentState.all;
}

/* extra debugging checks */
bool dvmPopIndirectRefTableSegmentCheck(IndirectRefTable* pRef, u4 cookie);

/*
 * Remove one or more segments from the top.  The table entry identified
 * by "cookie" becomes the new top-most entry.
 *
 * IMPORTANT: this is implemented as a single instruction in mterp, rather
 * than a call here.  You can add debugging aids for the C-language
 * interpreters, but the basic implementation must not change.
 */
INLINE void dvmPopIndirectRefTableSegment(IndirectRefTable* pRef, u4 cookie)
{
    dvmPopIndirectRefTableSegmentCheck(pRef, cookie);
    pRef->segmentState.all = cookie;
}

/*
 * Return the #of entries in the entire table.  This includes holes, and
 * so may be larger than the actual number of "live" entries.
 */
INLINE size_t dvmIndirectRefTableEntries(const IndirectRefTable* pRef)
{
    return pRef->segmentState.parts.topIndex;
}

/*
 * Returns "true" if the table is full.  The table is considered full if
 * we would need to expand it to add another entry to the current segment.
 */
INLINE size_t dvmIsIndirectRefTableFull(const IndirectRefTable* pRef)
{
    return dvmIndirectRefTableEntries(pRef) == (size_t)pRef->allocEntries;
}

/*
 * Add a new entry.  "obj" must be a valid non-NULL object reference
 * (though it's okay if it's not fully-formed, e.g. the result from
 * dvmMalloc doesn't have obj->clazz set).
 *
 * Returns NULL if the table is full (max entries reached, or alloc
 * failed during expansion).
 */
IndirectRef dvmAddToIndirectRefTable(IndirectRefTable* pRef, u4 cookie,
    Object* obj);

/*
 * Add a new entry at the end.  Similar to Add but does not usually attempt
 * to fill in holes.  This is only appropriate to use right after a new
 * segment has been pushed.
 *
 * (This is intended for use when calling into a native JNI method, so
 * performance is critical.)
 */
INLINE IndirectRef dvmAppendToIndirectRefTable(IndirectRefTable* pRef,
    u4 cookie, Object* obj)
{
    int topIndex = pRef->segmentState.parts.topIndex;
    if (topIndex == pRef->allocEntries) {
        /* up against alloc or max limit, call the fancy version */
        return dvmAddToIndirectRefTable(pRef, cookie, obj);
    } else {
        IndirectRef result = dvmObjectToIndirectRef(pRef, obj, topIndex,
            pRef->kind);
        pRef->table[topIndex++] = obj;
        pRef->segmentState.parts.topIndex = topIndex;
        return result;
    }
}

/* extra debugging checks */
bool dvmGetFromIndirectRefTableCheck(IndirectRefTable* pRef, IndirectRef iref);

/*
 * Given an IndirectRef in the table, return the Object it refers to.
 *
 * Returns NULL if iref is invalid.
 */
INLINE Object* dvmGetFromIndirectRefTable(IndirectRefTable* pRef,
    IndirectRef iref)
{
    if (!dvmGetFromIndirectRefTableCheck(pRef, iref))
        return NULL;

    int idx = dvmIndirectRefToIndex(iref);
    return pRef->table[idx];
}

/*
 * Remove an existing entry.
 *
 * If the entry is not between the current top index and the bottom index
 * specified by the cookie, we don't remove anything.  This is the behavior
 * required by JNI's DeleteLocalRef function.
 *
 * Returns "false" if nothing was removed.
 */
bool dvmRemoveFromIndirectRefTable(IndirectRefTable* pRef, u4 cookie,
    IndirectRef iref);

/*
 * Dump the contents of a reference table to the log file.
 */
void dvmDumpIndirectRefTable(const IndirectRefTable* pRef, const char* descr);

#endif /*_DALVIK_INDIRECTREFTABLE*/
