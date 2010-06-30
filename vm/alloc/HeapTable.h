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
#ifndef _DALVIK_ALLOC_HEAP_TABLE
#define _DALVIK_ALLOC_HEAP_TABLE

#include "ReferenceTable.h"

typedef ReferenceTable HeapRefTable;
typedef struct LargeHeapRefTable LargeHeapRefTable;
typedef struct HeapSource HeapSource;

struct LargeHeapRefTable {
    LargeHeapRefTable *next;
    HeapRefTable refs;
};

bool dvmHeapInitHeapRefTable(HeapRefTable *refs);
void dvmHeapFreeHeapRefTable(HeapRefTable *refs);
void dvmHeapFreeLargeTable(LargeHeapRefTable *table);
void dvmHeapHeapTableFree(void *ptr);
bool dvmHeapAddRefToLargeTable(LargeHeapRefTable **tableP, Object *ref);
void dvmHeapMarkLargeTableRefs(LargeHeapRefTable *table);
bool dvmHeapAddTableToLargeTable(LargeHeapRefTable **tableP,
        HeapRefTable *refs);
Object *dvmHeapGetNextObjectFromLargeTable(LargeHeapRefTable **pTable);

#define dvmHeapAddToHeapRefTable(refs, ptr) \
            dvmAddToReferenceTable((refs), (ptr))

#define dvmHeapNumHeapRefTableEntries(refs) \
            dvmReferenceTableEntries(refs)

#define dvmHeapRemoveFromHeapRefTable(refs, ptr) \
            dvmRemoveFromReferenceTable((refs), (refs)->table, (ptr))

#endif  // _DALVIK_ALLOC_HEAP_TABLE
