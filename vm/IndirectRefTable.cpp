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

static void abortMaybe() {
    // If CheckJNI is on, it'll give a more detailed error before aborting.
    // Otherwise, we want to abort rather than hand back a bad reference.
    if (!gDvmJni.useCheckJni) {
        dvmAbort();
    }
}

bool IndirectRefTable::init(size_t initialCount,
        size_t maxCount, IndirectRefKind desiredKind)
{
    assert(initialCount > 0);
    assert(initialCount <= maxCount);
    assert(desiredKind != kIndirectKindInvalid);

    table_ = (Object**) malloc(initialCount * sizeof(Object*));
    if (table_ == NULL) {
        return false;
    }
#ifndef NDEBUG
    memset(table_, 0xd1, initialCount * sizeof(Object*));
#endif

    slot_data_ = (IndirectRefSlot*) calloc(initialCount, sizeof(IndirectRefSlot));
    if (slot_data_ == NULL) {
        return false;
    }

    segmentState.all = IRT_FIRST_SEGMENT;
    alloc_entries_ = initialCount;
    max_entries_ = maxCount;
    kind_ = desiredKind;

    return true;
}

/*
 * Clears out the contents of a IndirectRefTable, freeing allocated storage.
 */
void IndirectRefTable::destroy()
{
    free(table_);
    free(slot_data_);
    table_ = NULL;
    slot_data_ = NULL;
    alloc_entries_ = max_entries_ = -1;
}

/*
 * Make sure that the entry at "idx" is correctly paired with "iref".
 */
bool IndirectRefTable::checkEntry(const char* what, IndirectRef iref, int idx) const
{
    Object* obj = table_[idx];
    IndirectRef checkRef = toIndirectRef(obj, idx);
    if (checkRef != iref) {
        LOGE("JNI ERROR (app bug): attempt to %s stale %s reference %p (should be %p)",
                what, indirectRefKindToString(kind_), iref, checkRef);
        abortMaybe();
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
    assert(dvmIsHeapAddress(obj));
    assert(table_ != NULL);
    assert(alloc_entries_ <= max_entries_);
    assert(segmentState.parts.numHoles >= prevState.parts.numHoles);

    if (topIndex == alloc_entries_) {
        /* reached end of allocated space; did we hit buffer max? */
        if (topIndex == max_entries_) {
            LOGE("JNI ERROR (app bug): %s reference table overflow (max=%d)",
                    indirectRefKindToString(kind_), max_entries_);
            dump(indirectRefKindToString(kind_));
            dvmAbort();
        }

        size_t newSize = alloc_entries_ * 2;
        if (newSize > max_entries_) {
            newSize = max_entries_;
        }
        assert(newSize > alloc_entries_);

        table_ = (Object**) realloc(table_, newSize * sizeof(Object*));
        slot_data_ = (IndirectRefSlot*) realloc(slot_data_, newSize * sizeof(IndirectRefSlot));
        if (table_ == NULL || slot_data_ == NULL) {
            LOGE("JNI ERROR (app bug): unable to expand %s reference table (from %d to %d, max=%d)",
                    indirectRefKindToString(kind_),
                    alloc_entries_, newSize, max_entries_);
            dump(indirectRefKindToString(kind_));
            dvmAbort();
        }

        // Clear the newly-allocated slot_data_ elements.
        memset(slot_data_ + alloc_entries_, 0, (newSize - alloc_entries_) * sizeof(IndirectRefSlot));

        alloc_entries_ = newSize;
    }

    /*
     * We know there's enough room in the table.  Now we just need to find
     * the right spot.  If there's a hole, find it and fill it; otherwise,
     * add to the end of the list.
     */
    IndirectRef result;
    int numHoles = segmentState.parts.numHoles - prevState.parts.numHoles;
    if (numHoles > 0) {
        assert(topIndex > 1);
        /* find the first hole; likely to be near the end of the list */
        Object** pScan = &table_[topIndex - 1];
        assert(*pScan != NULL);
        while (*--pScan != NULL) {
            assert(pScan >= table_ + prevState.parts.topIndex);
        }
        updateSlotAdd(obj, pScan - table_);
        result = toIndirectRef(obj, pScan - table_);
        *pScan = obj;
        segmentState.parts.numHoles--;
    } else {
        /* add to the end */
        updateSlotAdd(obj, topIndex);
        result = toIndirectRef(obj, topIndex);
        table_[topIndex++] = obj;
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
        LOGW("Attempt to look up NULL %s reference", indirectRefKindToString(kind_));
        return false;
    }
    if (indirectRefKind(iref) == kIndirectKindInvalid) {
        LOGE("JNI ERROR (app bug): invalid %s reference %p",
                indirectRefKindToString(kind_), iref);
        abortMaybe();
        return false;
    }

    int topIndex = segmentState.parts.topIndex;
    int idx = extractIndex(iref);
    if (idx >= topIndex) {
        /* bad -- stale reference? */
        LOGE("JNI ERROR (app bug): accessed stale %s reference %p (index %d in a table of size %d)",
                indirectRefKindToString(kind_), iref, idx, topIndex);
        abortMaybe();
        return false;
    }

    if (table_[idx] == NULL) {
        LOGI("JNI ERROR (app bug): accessed deleted %s reference %p",
                indirectRefKindToString(kind_), iref);
        abortMaybe();
        return false;
    }

    if (!checkEntry("use", iref, idx)) {
        return false;
    }

    return true;
}

static int linearScan(IndirectRef iref, int bottomIndex, int topIndex, Object** table) {
    for (int i = bottomIndex; i < topIndex; ++i) {
        if (table[i] == reinterpret_cast<Object*>(iref)) {
            return i;
        }
    }
    return -1;
}

bool IndirectRefTable::contains(IndirectRef iref) const {
    return linearScan(iref, 0, segmentState.parts.topIndex, table_) != -1;
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
 * for explicit single removals.
 *
 * Returns "false" if nothing was removed.
 */
bool IndirectRefTable::remove(u4 cookie, IndirectRef iref)
{
    IRTSegmentState prevState;
    prevState.all = cookie;
    int topIndex = segmentState.parts.topIndex;
    int bottomIndex = prevState.parts.topIndex;

    assert(table_ != NULL);
    assert(alloc_entries_ <= max_entries_);
    assert(segmentState.parts.numHoles >= prevState.parts.numHoles);

    int idx = extractIndex(iref);
    bool workAroundAppJniBugs = false;

    if (indirectRefKind(iref) == kIndirectKindInvalid && gDvmJni.workAroundAppJniBugs) {
        idx = linearScan(iref, bottomIndex, topIndex, table_);
        workAroundAppJniBugs = true;
        if (idx == -1) {
            LOGW("trying to work around app JNI bugs, but didn't find %p in table!", iref);
            return false;
        }
    }

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
        // Top-most entry.  Scan up and consume holes.

        if (workAroundAppJniBugs == false && !checkEntry("remove", iref, idx)) {
            return false;
        }

        table_[idx] = NULL;
        int numHoles = segmentState.parts.numHoles - prevState.parts.numHoles;
        if (numHoles != 0) {
            while (--topIndex > bottomIndex && numHoles != 0) {
                LOGV("+++ checking for hole at %d (cookie=0x%08x) val=%p",
                    topIndex-1, cookie, table_[topIndex-1]);
                if (table_[topIndex-1] != NULL) {
                    break;
                }
                LOGV("+++ ate hole at %d", topIndex-1);
                numHoles--;
            }
            segmentState.parts.numHoles = numHoles + prevState.parts.numHoles;
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
        if (table_[idx] == NULL) {
            LOGV("--- WEIRD: removing null entry %d", idx);
            return false;
        }
        if (workAroundAppJniBugs == false && !checkEntry("remove", iref, idx)) {
            return false;
        }

        table_[idx] = NULL;
        segmentState.parts.numHoles++;
        LOGV("+++ left hole at %d, holes=%d", idx, segmentState.parts.numHoles);
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
    dvmDumpReferenceTableContents(table_, capacity(), descr);
}
