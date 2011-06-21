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

bool IndirectRefTable::init(size_t initialCount,
        size_t maxCount, IndirectRefKind desiredKind)
{
    assert(initialCount > 0);
    assert(initialCount <= maxCount);
    assert(kind != kIndirectKindInvalid);

    table = (Object**) malloc(initialCount * sizeof(Object*));
    if (table == NULL) {
        return false;
    }
#ifndef NDEBUG
    memset(table, 0xd1, initialCount * sizeof(Object*));
#endif

    slotData =
        (IndirectRefSlot*) calloc(maxCount, sizeof(IndirectRefSlot));
    if (slotData == NULL) {
        return false;
    }

    segmentState.all = IRT_FIRST_SEGMENT;
    allocEntries = initialCount;
    maxEntries = maxCount;
    kind = desiredKind;

    return true;
}

/*
 * Clears out the contents of a IndirectRefTable, freeing allocated storage.
 */
void IndirectRefTable::destroy()
{
    free(table);
    free(slotData);
    table = NULL;
    allocEntries = maxEntries = -1;
}

/*
 * Make sure that the entry at "idx" is correctly paired with "iref".
 */
bool IndirectRefTable::checkEntry(IndirectRef iref, int idx) const
{
    Object* obj = table[idx];
    IndirectRef checkRef = toIndirectRef(obj, idx);
    if (checkRef != iref) {
        LOGE("Attempt to use stale %s reference (req=%p vs cur=%p; table=%p)",
                indirectRefKindToString(kind), iref, checkRef, this);
        return false;
    }
    return true;
}

IndirectRef IndirectRefTable::add(u4 cookie, Object* obj)
{
    IRTSegmentState prevState;
    prevState.all = cookie;
    size_t topIndex = segmentState.parts.topIndex;

    assert(obj != NULL);
    assert(dvmIsValidObject(obj));
    assert(table != NULL);
    assert(allocEntries <= maxEntries);
    assert(segmentState.parts.numHoles >= prevState.parts.numHoles);

    if (topIndex == allocEntries) {
        /* reached end of allocated space; did we hit buffer max? */
        if (topIndex == maxEntries) {
            LOGW("%s reference table overflow (max=%d)",
                    indirectRefKindToString(kind), maxEntries);
            return NULL;
        }

        size_t newSize = allocEntries * 2;
        if (newSize > maxEntries) {
            newSize = maxEntries;
        }
        assert(newSize > allocEntries);

        Object** newTable = (Object**) realloc(table, newSize * sizeof(Object*));
        if (newTable == NULL) {
            LOGE("Unable to expand %s reference table from %d to %d (max=%d)",
                    indirectRefKindToString(kind), allocEntries,
                    newSize, maxEntries);
            return false;
        }
        LOGV("Growing %s reference table %p from %d to %d (max=%d)",
                indirectRefKindToString(kind), this,
                allocEntries, newSize, maxEntries);

        /* update entries; adjust "nextEntry" in case memory moved */
        table = newTable;
        allocEntries = newSize;
    }

    IndirectRef result;

    /*
     * We know there's enough room in the table.  Now we just need to find
     * the right spot.  If there's a hole, find it and fill it; otherwise,
     * add to the end of the list.
     */
    int numHoles = segmentState.parts.numHoles - prevState.parts.numHoles;
    if (numHoles > 0) {
        assert(topIndex > 1);
        /* find the first hole; likely to be near the end of the list */
        Object** pScan = &table[topIndex - 1];
        assert(*pScan != NULL);
        while (*--pScan != NULL) {
            assert(pScan >= table + prevState.parts.topIndex);
        }
        updateSlotAdd(obj, pScan - table);
        result = toIndirectRef(obj, pScan - table);
        *pScan = obj;
        segmentState.parts.numHoles--;
    } else {
        /* add to the end */
        updateSlotAdd(obj, topIndex);
        result = toIndirectRef(obj, topIndex);
        table[topIndex++] = obj;
        segmentState.parts.topIndex = topIndex;
    }

    assert(result != NULL);
    return result;
}

/*
 * Verify that the indirect table lookup is valid.
 *
 * Returns "false" if something looks bad.
 */
bool IndirectRefTable::getChecked(IndirectRef iref) const
{
    if (iref == NULL) {
        LOGW("Attempt to look up NULL %s reference",
                indirectRefKindToString(kind));
        return false;
    }
    if (indirectRefKind(iref) == kIndirectKindInvalid) {
        LOGW("Invalid %s reference %p",
                indirectRefKindToString(kind), iref);
        return false;
    }

    int topIndex = segmentState.parts.topIndex;
    int idx = extractIndex(iref);
    if (idx >= topIndex) {
        /* bad -- stale reference? */
        LOGW("Attempt to access stale %s reference at index %d (top=%d)",
            indirectRefKindToString(kind), idx, topIndex);
        return false;
    }

    Object* obj = table[idx];
    if (obj == NULL) {
        LOGW("Attempt to access deleted %s reference (%p)",
                indirectRefKindToString(kind), iref);
        return false;
    }
    if (!checkEntry(iref, idx)) {
        return false;
    }

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
bool IndirectRefTable::remove(u4 cookie, IndirectRef iref)
{
    IRTSegmentState prevState;
    prevState.all = cookie;
    int topIndex = segmentState.parts.topIndex;
    int bottomIndex = prevState.parts.topIndex;

    assert(table != NULL);
    assert(allocEntries <= maxEntries);
    assert(segmentState.parts.numHoles >= prevState.parts.numHoles);

    int idx = extractIndex(iref);
    if (idx < bottomIndex) {
        /* wrong segment */
        LOGV("Attempt to remove index outside index area (%d vs %d-%d)",
            idx, bottomIndex, topIndex);
        return false;
    }
    if (idx >= topIndex) {
        /* bad -- stale reference? */
        LOGD("Attempt to remove invalid index %d (bottom=%d top=%d)",
            idx, bottomIndex, topIndex);
        return false;
    }

    if (idx == topIndex-1) {
        /*
         * Top-most entry.  Scan up and consume holes.  No need to NULL
         * out the entry, since the test vs. topIndex will catch it.
         */
        if (!checkEntry(iref, idx)) {
            return false;
        }
        updateSlotRemove(idx);

#ifndef NDEBUG
        table[idx] = (Object*)0xd3d3d3d3;
#endif

        int numHoles =
            segmentState.parts.numHoles - prevState.parts.numHoles;
        if (numHoles != 0) {
            while (--topIndex > bottomIndex && numHoles != 0) {
                LOGV("+++ checking for hole at %d (cookie=0x%08x) val=%p",
                    topIndex-1, cookie, table[topIndex-1]);
                if (table[topIndex-1] != NULL)
                    break;
                LOGV("+++ ate hole at %d", topIndex-1);
                numHoles--;
            }
            segmentState.parts.numHoles =
                numHoles + prevState.parts.numHoles;
            segmentState.parts.topIndex = topIndex;
        } else {
            segmentState.parts.topIndex = topIndex-1;
            LOGV("+++ ate last entry %d", topIndex-1);
        }
    } else {
        /*
         * Not the top-most entry.  This creates a hole.  We NULL out the
         * entry to prevent somebody from deleting it twice and screwing up
         * the hole count.
         */
        if (table[idx] == NULL) {
            LOGV("--- WEIRD: removing null entry %d", idx);
            return false;
        }
        if (!checkEntry(iref, idx)) {
            return false;
        }
        updateSlotRemove(idx);

        table[idx] = NULL;
        segmentState.parts.numHoles++;
        LOGV("+++ left hole at %d, holes=%d",
            idx, segmentState.parts.numHoles);
    }

    return true;
}

const char* indirectRefKindToString(IndirectRefKind kind)
{
    switch (kind) {
    case kIndirectKindInvalid:      return "invalid";
    case kIndirectKindLocal:        return "local";
    case kIndirectKindGlobal:       return "global";
    case kIndirectKindWeakGlobal:   return "weak global";
    default:                        return "UNKNOWN";
    }
}

void IndirectRefTable::dump(const char* descr) const
{
    dvmDumpReferenceTableContents(table, capacity(), descr);
}
