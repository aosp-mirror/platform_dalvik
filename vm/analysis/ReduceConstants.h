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
 * DEX constant-reduction declarations.
 */
#ifndef _DALVIK_REDUCECONSTANTS
#define _DALVIK_REDUCECONSTANTS

#define DVM_RC_DISABLED     0       /* no reduction, 1:1 map */
#define DVM_RC_REDUCING     1       /* normal constants, reduced lookup table */
#define DVM_RC_EXPANDING    2       /* reduced constants, expanded on resolve */
#define DVM_RC_NO_CACHE     3       /* disable the cache (reduce to zero) */

enum {
    kMapClasses     = 0,
    kMapMethods     = 1,
    kMapFields      = 2,
    kMapStrings     = 3,

    kNumIndexMaps
};

struct DvmDex;

#define kNoIndexMapping     ((u2) -1)

/*
 * Map indices back to the original.
 */
typedef struct IndexMap {
    int origCount;      /* original size; describes range of entries in map */
    int newCount;       /* reduced size */
    u2* mapToNew;       /* sparse map, from "orig" to "new" */
    u2* mapToOld;       /* dense map, from "new" back to "orig" */
} IndexMap;
typedef struct IndexMapSet {
    /* maps for the different sections */
    IndexMap    map[kNumIndexMaps];

    /* data stream that gets appended to the optimized DEX file */
    u4          chunkType;
    int         chunkDataLen;
    u1*         chunkData;
} IndexMapSet;

/*
 * Constant pool compaction.
 *
 * The caller is responsible for freeing the returned structure by
 * calling dvmFreeIndexMap().
 */
IndexMapSet* dvmRewriteConstants(struct DvmDex* pDvmDex);

/* free an index map set */
void dvmFreeIndexMapSet(IndexMapSet* indexMapSet);

#endif /*_DALVIK_REDUCECONSTANTS*/
