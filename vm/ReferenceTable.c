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

/*
 * Reference table management.
 */
#include "Dalvik.h"

/*
 * Initialize a ReferenceTable structure.
 */
bool dvmInitReferenceTable(ReferenceTable* pRef, int initialCount,
    int maxCount)
{
    assert(initialCount > 0);
    assert(initialCount <= maxCount);

    pRef->table = (Object**) malloc(initialCount * sizeof(Object*));
    if (pRef->table == NULL)
        return false;
#ifndef NDEBUG
    memset(pRef->table, 0xdd, initialCount * sizeof(Object*));
#endif
    pRef->nextEntry = pRef->table;
    pRef->allocEntries = initialCount;
    pRef->maxEntries = maxCount;

    return true;
}

/*
 * Clears out the contents of a ReferenceTable, freeing allocated storage.
 */
void dvmClearReferenceTable(ReferenceTable* pRef)
{
    free(pRef->table);
    pRef->table = pRef->nextEntry = NULL;
    pRef->allocEntries = pRef->maxEntries = -1;
}

/*
 * Add "obj" to "pRef".
 */
bool dvmAddToReferenceTable(ReferenceTable* pRef, Object* obj)
{
    assert(dvmIsValidObject(obj));
    assert(obj != NULL);
    assert(pRef->table != NULL);
    assert(pRef->allocEntries <= pRef->maxEntries);

    if (pRef->nextEntry == pRef->table + pRef->allocEntries) {
        /* reached end of allocated space; did we hit buffer max? */
        if (pRef->nextEntry == pRef->table + pRef->maxEntries) {
            LOGW("ReferenceTable overflow (max=%d)\n", pRef->maxEntries);
            return false;
        }

        Object** newTable;
        int newSize;

        newSize = pRef->allocEntries * 2;
        if (newSize > pRef->maxEntries)
            newSize = pRef->maxEntries;
        assert(newSize > pRef->allocEntries);

        newTable = (Object**) realloc(pRef->table, newSize * sizeof(Object*));
        if (newTable == NULL) {
            LOGE("Unable to expand ref table (from %d to %d %d-byte entries)\n",
                pRef->allocEntries, newSize, sizeof(Object*));
            return false;
        }
        LOGVV("Growing %p from %d to %d\n", pRef, pRef->allocEntries, newSize);

        /* update entries; adjust "nextEntry" in case memory moved */
        pRef->nextEntry = newTable + (pRef->nextEntry - pRef->table);
        pRef->table = newTable;
        pRef->allocEntries = newSize;
    }

    *pRef->nextEntry++ = obj;
    return true;
}

/*
 * Returns NULL if not found.
 */
Object** dvmFindInReferenceTable(const ReferenceTable* pRef, Object** bottom,
    Object* obj)
{
    Object** ptr;

    ptr = pRef->nextEntry;
    while (--ptr >= bottom) {
        if (*ptr == obj)
            return ptr;
    }
    return NULL;
}

/*
 * Remove "obj" from "pRef".  We start at the end of the list (where the
 * most-recently-added element is), and stop searching for a match after
 * examining the element at "bottom".
 *
 * Most of the time "obj" is at or near the end of the list.  If not, we
 * compact it down.
 */
bool dvmRemoveFromReferenceTable(ReferenceTable* pRef, Object** bottom,
    Object* obj)
{
    Object** ptr;

    assert(pRef->table != NULL);

    /*
     * Scan from the most-recently-added entry up to the bottom entry for
     * this frame.
     */
    ptr = dvmFindInReferenceTable(pRef, bottom, obj);
    if (ptr == NULL)
        return false;

    /*
     * Delete the entry.
     */
    pRef->nextEntry--;
    int moveCount = pRef->nextEntry - ptr;
    if (moveCount != 0) {
        /* remove from middle, slide the rest down */
        memmove(ptr, ptr+1, moveCount * sizeof(Object*));
        //LOGV("LREF delete %p, shift %d down\n", obj, moveCount);
    } else {
        /* last entry, falls off the end */
        //LOGV("LREF delete %p from end\n", obj);
    }

    return true;
}

/*
 * This is a qsort() callback.  We sort Object* by class, allocation size,
 * and then by the Object* itself.
 */
static int compareObject(const void* vobj1, const void* vobj2)
{
    Object* obj1 = *((Object**) vobj1);
    Object* obj2 = *((Object**) vobj2);

    if (obj1 == NULL || obj2 == NULL)
        return (u1*)obj1 - (u1*)obj2;

    if (obj1->clazz != obj2->clazz) {
        return (u1*)obj1->clazz - (u1*)obj2->clazz;
    } else {
        int size1 = dvmObjectSizeInHeap(obj1);
        int size2 = dvmObjectSizeInHeap(obj2);
        if (size1 != size2) {
            return size1 - size2;
        } else {
            return (u1*)obj1 - (u1*)obj2;
        }
    }
}

/*
 * Log an object with some additional info.
 *
 * Pass in the number of additional elements that are identical to or
 * equivalent to the original.
 */
static void logObject(Object* obj, int size, int identical, int equiv)
{
    if (obj == NULL) {
        LOGW("  NULL reference (count=%d)\n", equiv);
        return;
    }

    /* handle "raw" dvmMalloc case */
    const char* descriptor =
        (obj->clazz != NULL) ? obj->clazz->descriptor : "(raw)";

    if (identical + equiv != 0) {
        LOGW("%5d of %s %dB (%d unique)\n", identical + equiv +1,
            descriptor, size, equiv +1);
    } else {
        LOGW("%5d of %s %dB\n", identical + equiv +1, descriptor, size);
    }
}

/*
 * Dump the contents of a ReferenceTable to the log.
 *
 * The caller should lock any external sync before calling.
 *
 * (This was originally written to be tolerant of null entries in the table.
 * I don't think that can happen anymore.)
 */
void dvmDumpReferenceTable(const ReferenceTable* pRef, const char* descr)
{
    const int kLast = 10;
    int count = dvmReferenceTableEntries(pRef);
    Object** refs;
    int i;

    if (count == 0) {
        LOGW("%s reference table has no entries\n", descr);
        return;
    }
    assert(count > 0);

    /*
     * Dump the most recent N entries.
     */
    LOGW("Last %d entries in %s reference table:\n", kLast, descr);
    refs = pRef->table;         // use unsorted list
    int size;
    int start = count - kLast;
    if (start < 0)
        start = 0;

    for (i = start; i < count; i++) {
        size = (refs[i] == NULL) ? 0 : dvmObjectSizeInHeap(refs[i]);
        Object* ref = refs[i];
        if (ref->clazz == gDvm.classJavaLangClass) {
            ClassObject* clazz = (ClassObject*) ref;
            LOGW("%5d: %p cls=%s '%s' (%d bytes)\n", i, ref,
                (refs[i] == NULL) ? "-" : ref->clazz->descriptor,
                clazz->descriptor, size);
        } else if (ref->clazz == NULL) {
            /* should only be possible right after a plain dvmMalloc() */
            LOGW("%5d: %p cls=(raw) (%d bytes)\n", i, ref, size);
        } else {
            LOGW("%5d: %p cls=%s (%d bytes)\n", i, ref,
                (refs[i] == NULL) ? "-" : ref->clazz->descriptor, size);
        }
    }

    /*
     * Make a copy of the table, and sort it.
     */
    Object** tableCopy = (Object**)malloc(sizeof(Object*) * count);
    memcpy(tableCopy, pRef->table, sizeof(Object*) * count);
    qsort(tableCopy, count, sizeof(Object*), compareObject);
    refs = tableCopy;       // use sorted list

    /*
     * Dump uniquified table summary.  While we're at it, generate a
     * cumulative total amount of pinned memory based on the unique entries.
     */
    LOGW("%s reference table summary (%d entries):\n", descr, count);
    int equiv, identical, total;
    total = equiv = identical = 0;
    for (i = 1; i < count; i++) {
        size = (refs[i-1] == NULL) ? 0 : dvmObjectSizeInHeap(refs[i-1]);

        if (refs[i] == refs[i-1]) {
            /* same reference, added more than once */
            identical++;
        } else if (refs[i]->clazz == refs[i-1]->clazz &&
            (int) dvmObjectSizeInHeap(refs[i]) == size)
        {
            /* same class / size, different object */
            total += size;
            equiv++;
        } else {
            /* different class */
            total += size;
            logObject(refs[i-1], size, identical, equiv);
            equiv = identical = 0;
        }
    }

    /* handle the last entry (everything above outputs refs[i-1]) */
    size = (refs[count-1] == NULL) ? 0 : dvmObjectSizeInHeap(refs[count-1]);
    total += size;
    logObject(refs[count-1], size, identical, equiv);

    LOGW("Memory held directly by tracked refs is %d bytes\n", total);
    free(tableCopy);
}
