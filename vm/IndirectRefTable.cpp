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

/*
 * Indirect reference table management.
 */
#include "Dalvik.h"

/*
 * Initialize an IndirectRefTable structure.
 */
bool dvmInitIndirectRefTable(IndirectRefTable* pRef, int initialCount,
    int maxCount, IndirectRefKind kind)
{
    assert(initialCount > 0);
    assert(initialCount <= maxCount);
    assert(kind != kIndirectKindInvalid);

    pRef->table = (Object**) malloc(initialCount * sizeof(Object*));
    if (pRef->table == NULL)
        return false;
#ifndef NDEBUG
    memset(pRef->table, 0xd1, initialCount * sizeof(Object*));
#endif

    pRef->slotData =
        (IndirectRefSlot*) calloc(maxCount, sizeof(IndirectRefSlot));
    if (pRef->slotData == NULL)
        return false;

    pRef->segmentState.all = IRT_FIRST_SEGMENT;
    pRef->allocEntries = initialCount;
    pRef->maxEntries = maxCount;
    pRef->kind = kind;

    return true;
}

/*
 * Clears out the contents of a IndirectRefTable, freeing allocated storage.
 */
void dvmClearIndirectRefTable(IndirectRefTable* pRef)
{
    free(pRef->table);
    free(pRef->slotData);
    pRef->table = NULL;
    pRef->allocEntries = pRef->maxEntries = -1;
}

/*
 * Remove one or more segments from the top.  The table entry identified
 * by "cookie" becomes the new top-most entry.
 *
 * Returns false if "cookie" is invalid or the table has only one segment.
 */
bool dvmPopIndirectRefTableSegmentCheck(IndirectRefTable* pRef, u4 cookie)
{
    IRTSegmentState sst;

    /*
     * The new value for "top" must be <= the current value.  Otherwise
     * this would represent an expansion of the table.
     */
    sst.all = cookie;
    if (sst.parts.topIndex > pRef->segmentState.parts.topIndex) {
        LOGE("Attempt to expand table with segment pop (%d to %d)\n",
            pRef->segmentState.parts.topIndex, sst.parts.topIndex);
        return false;
    }
    if (sst.parts.numHoles >= sst.parts.topIndex) {
        LOGE("Absurd numHoles in cookie (%d bi=%d)\n",
            sst.parts.numHoles, sst.parts.topIndex);
        return false;
    }

    LOGV("IRT %p[%d]: pop, top=%d holes=%d\n",
        pRef, pRef->kind, sst.parts.topIndex, sst.parts.numHoles);

    return true;
}

/*
 * Make sure that the entry at "idx" is correctly paired with "iref".
 */
static bool checkEntry(IndirectRefTable* pRef, IndirectRef iref, int idx)
{
    Object* obj = pRef->table[idx];
    IndirectRef checkRef = dvmObjectToIndirectRef(pRef, obj, idx, pRef->kind);
    if (checkRef != iref) {
        LOGW("IRT %p[%d]: iref mismatch (req=%p vs cur=%p)\n",
            pRef, pRef->kind, iref, checkRef);
        return false;
    }
    return true;
}

/*
 * Update extended debug info when an entry is added.
 *
 * We advance the serial number, invalidating any outstanding references to
 * this slot.
 */
static inline void updateSlotAdd(IndirectRefTable* pRef, Object* obj, int slot)
{
    if (pRef->slotData != NULL) {
        IndirectRefSlot* pSlot = &pRef->slotData[slot];
        pSlot->serial++;
        //LOGI("+++ add [%d] slot %d (%p->%p), serial=%d\n",
        //    pRef->kind, slot, obj, iref, pSlot->serial);
        pSlot->previous[pSlot->serial % kIRTPrevCount] = obj;
    }
}

/*
 * Update extended debug info when an entry is removed.
 */
static inline void updateSlotRemove(IndirectRefTable* pRef, int slot)
{
    if (pRef->slotData != NULL) {
        //IndirectRefSlot* pSlot = &pRef->slotData[slot];
        //LOGI("+++ remove [%d] slot %d, serial now %d\n",
        //    pRef->kind, slot, pSlot->serial);
    }
}

/*
 * Add "obj" to "pRef".
 */
IndirectRef dvmAddToIndirectRefTable(IndirectRefTable* pRef, u4 cookie,
    Object* obj)
{
    IRTSegmentState prevState;
    prevState.all = cookie;
    int topIndex = pRef->segmentState.parts.topIndex;

    assert(obj != NULL);
    assert(dvmIsValidObject(obj));
    assert(pRef->table != NULL);
    assert(pRef->allocEntries <= pRef->maxEntries);
    assert(pRef->segmentState.parts.numHoles >= prevState.parts.numHoles);

    if (topIndex == pRef->allocEntries) {
        /* reached end of allocated space; did we hit buffer max? */
        if (topIndex == pRef->maxEntries) {
            LOGW("IndirectRefTable overflow (max=%d)\n", pRef->maxEntries);
            return NULL;
        }

        Object** newTable;
        int newSize;

        newSize = pRef->allocEntries * 2;
        if (newSize > pRef->maxEntries)
            newSize = pRef->maxEntries;
        assert(newSize > pRef->allocEntries);

        newTable = (Object**) realloc(pRef->table, newSize * sizeof(Object*));
        if (newTable == NULL) {
            LOGE("Unable to expand iref table (from %d to %d, max=%d)\n",
                pRef->allocEntries, newSize, pRef->maxEntries);
            return false;
        }
        LOGV("Growing ireftab %p from %d to %d (max=%d)\n",
            pRef, pRef->allocEntries, newSize, pRef->maxEntries);

        /* update entries; adjust "nextEntry" in case memory moved */
        pRef->table = newTable;
        pRef->allocEntries = newSize;
    }

    IndirectRef result;

    /*
     * We know there's enough room in the table.  Now we just need to find
     * the right spot.  If there's a hole, find it and fill it; otherwise,
     * add to the end of the list.
     */
    int numHoles = pRef->segmentState.parts.numHoles - prevState.parts.numHoles;
    if (numHoles > 0) {
        assert(topIndex > 1);
        /* find the first hole; likely to be near the end of the list */
        Object** pScan = &pRef->table[topIndex - 1];
        assert(*pScan != NULL);
        while (*--pScan != NULL) {
            assert(pScan >= pRef->table + prevState.parts.topIndex);
        }
        updateSlotAdd(pRef, obj, pScan - pRef->table);
        result = dvmObjectToIndirectRef(pRef, obj, pScan - pRef->table,
            pRef->kind);
        *pScan = obj;
        pRef->segmentState.parts.numHoles--;
    } else {
        /* add to the end */
        updateSlotAdd(pRef, obj, topIndex);
        result = dvmObjectToIndirectRef(pRef, obj, topIndex, pRef->kind);
        pRef->table[topIndex++] = obj;
        pRef->segmentState.parts.topIndex = topIndex;
    }

    assert(result != NULL);
    return result;
}

/*
 * Verify that the indirect table lookup is valid.
 *
 * Returns "false" if something looks bad.
 */
bool dvmGetFromIndirectRefTableCheck(IndirectRefTable* pRef, IndirectRef iref)
{
    if (dvmGetIndirectRefType(iref) == kIndirectKindInvalid) {
        LOGW("Invalid indirect reference 0x%08x\n", (u4) iref);
        return false;
    }

    int topIndex = pRef->segmentState.parts.topIndex;
    int idx = dvmIndirectRefToIndex(iref);

    if (iref == NULL) {
        LOGD("Attempt to look up NULL iref\n");
        return false;
    }
    if (idx >= topIndex) {
        /* bad -- stale reference? */
        LOGD("Attempt to access invalid index %d (top=%d)\n",
            idx, topIndex);
        return false;
    }

    Object* obj = pRef->table[idx];
    if (obj == NULL) {
        LOGD("Attempt to read from hole, iref=%p\n", iref);
        return false;
    }
    if (!checkEntry(pRef, iref, idx))
        return false;

    return true;
}

/*
 * Remove "obj" from "pRef".  We extract the table offset bits from "iref"
 * and zap the corresponding entry, leaving a hole if it's not at the top.
 *
 * If the entry is not between the current top index and the bottom index
 * specified by the cookie, we don't remove anything.  This is the behavior
 * required by JNI's DeleteLocalRef function.
 *
 * Note this is NOT called when a local frame is popped.  This is only used
 * for explict single removals.
 *
 * Returns "false" if nothing was removed.
 */
bool dvmRemoveFromIndirectRefTable(IndirectRefTable* pRef, u4 cookie,
    IndirectRef iref)
{
    IRTSegmentState prevState;
    prevState.all = cookie;
    int topIndex = pRef->segmentState.parts.topIndex;
    int bottomIndex = prevState.parts.topIndex;

    assert(pRef->table != NULL);
    assert(pRef->allocEntries <= pRef->maxEntries);
    assert(pRef->segmentState.parts.numHoles >= prevState.parts.numHoles);

    int idx = dvmIndirectRefToIndex(iref);
    if (idx < bottomIndex) {
        /* wrong segment */
        LOGV("Attempt to remove index outside index area (%d vs %d-%d)\n",
            idx, bottomIndex, topIndex);
        return false;
    }
    if (idx >= topIndex) {
        /* bad -- stale reference? */
        LOGD("Attempt to remove invalid index %d (bottom=%d top=%d)\n",
            idx, bottomIndex, topIndex);
        return false;
    }

    if (idx == topIndex-1) {
        /*
         * Top-most entry.  Scan up and consume holes.  No need to NULL
         * out the entry, since the test vs. topIndex will catch it.
         */
        if (!checkEntry(pRef, iref, idx))
            return false;
        updateSlotRemove(pRef, idx);

#ifndef NDEBUG
        pRef->table[idx] = (Object*)0xd3d3d3d3;
#endif

        int numHoles =
            pRef->segmentState.parts.numHoles - prevState.parts.numHoles;
        if (numHoles != 0) {
            while (--topIndex > bottomIndex && numHoles != 0) {
                LOGV("+++ checking for hole at %d (cookie=0x%08x) val=%p\n",
                    topIndex-1, cookie, pRef->table[topIndex-1]);
                if (pRef->table[topIndex-1] != NULL)
                    break;
                LOGV("+++ ate hole at %d\n", topIndex-1);
                numHoles--;
            }
            pRef->segmentState.parts.numHoles =
                numHoles + prevState.parts.numHoles;
            pRef->segmentState.parts.topIndex = topIndex;
        } else {
            pRef->segmentState.parts.topIndex = topIndex-1;
            LOGV("+++ ate last entry %d\n", topIndex-1);
        }
    } else {
        /*
         * Not the top-most entry.  This creates a hole.  We NULL out the
         * entry to prevent somebody from deleting it twice and screwing up
         * the hole count.
         */
        if (pRef->table[idx] == NULL) {
            LOGV("--- WEIRD: removing null entry %d\n", idx);
            return false;
        }
        if (!checkEntry(pRef, iref, idx))
            return false;
        updateSlotRemove(pRef, idx);

        pRef->table[idx] = NULL;
        pRef->segmentState.parts.numHoles++;
        LOGV("+++ left hole at %d, holes=%d\n",
            idx, pRef->segmentState.parts.numHoles);
    }

    return true;
}

/*
 * Return a type name, useful for debugging.
 */
const char* dvmIndirectRefTypeName(IndirectRef iref)
{
    switch (dvmGetIndirectRefType(iref)) {
    case kIndirectKindInvalid:      return "invalid";
    case kIndirectKindLocal:        return "local";
    case kIndirectKindGlobal:       return "global";
    case kIndirectKindWeakGlobal:   return "weak global";
    default:                        return "UNKNOWN";
    }
}

/*
 * Dump the contents of a IndirectRefTable to the log.
 */
void dvmDumpIndirectRefTable(const IndirectRefTable* pRef, const char* descr)
{
    dvmDumpReferenceTableContents(pRef->table, dvmIndirectRefTableEntries(pRef),
        descr);
}
