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
#include "alloc/HeapTable.h"
#include "alloc/HeapInternal.h"

#include <limits.h> // for INT_MAX

static const int kLargeHeapRefTableNElems = 1024;
static const int  kFinalizableRefDefault = 128;

bool dvmHeapInitHeapRefTable(ReferenceTable *refs)
{
    return dvmInitReferenceTable(refs, kFinalizableRefDefault, INT_MAX);
}

/*
 * Large, non-contiguous reference tables
 */

bool dvmHeapAddRefToLargeTable(LargeHeapRefTable **tableP, Object *ref)
{
    LargeHeapRefTable *table;

    assert(tableP != NULL);
    assert(ref != NULL);

    /* Make sure that a table with a free slot is
     * at the head of the list.
     */
    if (*tableP != NULL) {
        table = *tableP;
        LargeHeapRefTable *prevTable;

        /* Find an empty slot for this reference.
         */
        prevTable = NULL;
        while (table != NULL && dvmIsReferenceTableFull(&table->refs)) {
            prevTable = table;
            table = table->next;
        }
        if (table != NULL) {
            if (prevTable != NULL) {
                /* Move the table to the head of the list.
                 */
                prevTable->next = table->next;
                table->next = *tableP;
                *tableP = table;
            }
            /* else it's already at the head. */

            goto insert;
        }
        /* else all tables are already full;
         * fall through to the alloc case.
         */
    }

    /* Allocate a new table.
     */
    table = (LargeHeapRefTable *)calloc(1, sizeof(LargeHeapRefTable));
    if (table == NULL) {
        LOGE_HEAP("Can't allocate a new large ref table\n");
        return false;
    }
    if (!dvmInitReferenceTable(&table->refs,
                               kLargeHeapRefTableNElems,
                               INT_MAX)) {
        LOGE_HEAP("Can't initialize a new large ref table\n");
        free(table);
        return false;
    }

    /* Stick it at the head.
     */
    table->next = *tableP;
    *tableP = table;

insert:
    /* Insert the reference.
     */
    assert(table == *tableP);
    assert(table != NULL);
    assert(!dvmIsReferenceTableFull(&table->refs));
    *table->refs.nextEntry++ = ref;

    return true;
}

bool dvmHeapAddTableToLargeTable(LargeHeapRefTable **tableP, ReferenceTable *refs)
{
    LargeHeapRefTable *table;

    /* Allocate a node.
     */
    table = (LargeHeapRefTable *)calloc(1, sizeof(LargeHeapRefTable));
    if (table == NULL) {
        LOGE_HEAP("Can't allocate a new large ref table\n");
        return false;
    }
    table->refs = *refs;

    /* Insert the table into the list.
     */
    table->next = *tableP;
    *tableP = table;

    return true;
}

/* Frees everything associated with the LargeHeapRefTable.
 */
void dvmHeapFreeLargeTable(LargeHeapRefTable *table)
{
    while (table != NULL) {
        LargeHeapRefTable *next = table->next;
        dvmClearReferenceTable(&table->refs);
        free(table);
        table = next;
    }
}

Object *dvmHeapGetNextObjectFromLargeTable(LargeHeapRefTable **pTable)
{
    LargeHeapRefTable *table;
    Object *obj;

    assert(pTable != NULL);

    obj = NULL;
    table = *pTable;
    if (table != NULL) {
        ReferenceTable *refs = &table->refs;

        /* We should never have an empty table node in the list.
         */
        assert(dvmReferenceTableEntries(refs) != 0);

        /* Remove and return the last entry in the list.
         */
        obj = *--refs->nextEntry;

        /* If this was the last entry in the table node,
         * free it and patch up the list.
         */
        if (refs->nextEntry == refs->table) {
            *pTable = table->next;
            dvmClearReferenceTable(refs);
            free(table);
        }
    }

    return obj;
}
