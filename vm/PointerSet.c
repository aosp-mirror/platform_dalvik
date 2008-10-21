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
 * Maintain an expanding set of unique pointer values.
 */
#include "Dalvik.h"

/*
 * Sorted, expanding list of pointers.
 */
struct PointerSet {
    u2          alloc;
    u2          count;
    const void** list;
};

/*
 * Verify that the set is in sorted order.
 */
static bool verifySorted(PointerSet* pSet)
{
    const void* last = NULL;
    int i;

    for (i = 0; i < pSet->count; i++) {
        const void* cur = pSet->list[i];
        if (cur < last)
            return false;
        last = cur;
    }

    return true;
}


/*
 * Allocate a new PointerSet.
 *
 * Returns NULL on failure.
 */
PointerSet* dvmPointerSetAlloc(int initialSize)
{
    PointerSet* pSet = calloc(1, sizeof(PointerSet));
    if (pSet != NULL) {
        if (initialSize > 0) {
            pSet->list = malloc(sizeof(const void*) * initialSize);
            if (pSet->list == NULL) {
                free(pSet);
                return NULL;
            }
            pSet->alloc = initialSize;
        }
    }

    return pSet;
}

/*
 * Free up a PointerSet.
 */
void dvmPointerSetFree(PointerSet* pSet)
{
    if (pSet->list != NULL) {
        free(pSet->list);
        pSet->list = NULL;
    }
    free(pSet);
}

/*
 * Get the number of pointers currently stored in the list.
 */
int dvmPointerSetGetCount(const PointerSet* pSet)
{
    return pSet->count;
}

/*
 * Get the Nth entry from the list.
 */
const void* dvmPointerSetGetEntry(const PointerSet* pSet, int i)
{
    return pSet->list[i];
}

/*
 * Insert a new entry into the list.  If it already exists, this returns
 * without doing anything.
 */
void dvmPointerSetAddEntry(PointerSet* pSet, const void* ptr)
{
    int nearby;

    if (dvmPointerSetHas(pSet, ptr, &nearby))
        return;

    /* ensure we have space to add one more */
    if (pSet->count == pSet->alloc) {
        /* time to expand */
        const void** newList;

        if (pSet->alloc == 0)
            pSet->alloc = 4;
        else
            pSet->alloc *= 2;
        LOGVV("expanding %p to %d\n", pSet, pSet->alloc);
        newList = realloc(pSet->list, pSet->alloc * sizeof(const void*));
        if (newList == NULL) {
            LOGE("Failed expanding ptr set (alloc=%d)\n", pSet->alloc);
            dvmAbort();
        }
        pSet->list = newList;
    }

    if (pSet->count == 0) {
        /* empty list */
        assert(nearby == 0);
    } else {
        /*
         * Determine the insertion index.  The binary search might have
         * terminated "above" or "below" the value.
         */
        if (nearby != 0 && ptr < pSet->list[nearby-1]) {
            //LOGD("nearby-1=%d %p, inserting %p at -1\n",
            //    nearby-1, pSet->list[nearby-1], ptr);
            nearby--;
        } else if (ptr < pSet->list[nearby]) {
            //LOGD("nearby=%d %p, inserting %p at +0\n",
            //    nearby, pSet->list[nearby], ptr);
        } else {
            //LOGD("nearby+1=%d %p, inserting %p at +1\n",
            //    nearby+1, pSet->list[nearby+1], ptr);
            nearby++;
        }

        /*
         * Move existing values, if necessary.
         */
        if (nearby != pSet->count) {
            /* shift up */
            memmove(&pSet->list[nearby+1], &pSet->list[nearby],
                (pSet->count - nearby) * sizeof(pSet->list[0]));
        }
    }

    pSet->list[nearby] = ptr;
    pSet->count++;

    assert(verifySorted(pSet));
}

/*
 * Returns "true" if the element was successfully removed.
 */
bool dvmPointerSetRemoveEntry(PointerSet* pSet, const void* ptr)
{
    int i, where;

    if (!dvmPointerSetHas(pSet, ptr, &where))
        return false;

    if (where != pSet->count-1) {
        /* shift down */
        memmove(&pSet->list[where], &pSet->list[where+1],
            (pSet->count-1 - where) * sizeof(pSet->list[0]));
    }

    pSet->count--;
    pSet->list[pSet->count] = (const void*) 0xdecadead;
    return true;
}

/*
 * Returns the index if "ptr" appears in the list.  If it doesn't appear,
 * this returns a negative index for a nearby element.
 */
bool dvmPointerSetHas(const PointerSet* pSet, const void* ptr, int* pIndex)
{
    int hi, lo, mid;

    lo = mid = 0;
    hi = pSet->count-1;

    /* array is sorted, use a binary search */
    while (lo <= hi) {
        mid = (lo + hi) / 2;
        const void* listVal = pSet->list[mid];

        if (ptr > listVal) {
            lo = mid + 1;
        } else if (ptr < listVal) {
            hi = mid - 1;
        } else /* listVal == ptr */ {
            if (pIndex != NULL)
                *pIndex = mid;
            return true;
        }
    }

    if (pIndex != NULL)
        *pIndex = mid;
    return false;
}

/*
 * Print the list contents to stdout.  For debugging.
 */
void dvmPointerSetDump(const PointerSet* pSet)
{
    int i;
    for (i = 0; i < pSet->count; i++)
        printf(" %p", pSet->list[i]);
}

