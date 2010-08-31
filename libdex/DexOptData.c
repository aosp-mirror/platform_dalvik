/*
 * Copyright (C) 2010 The Android Open Source Project
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
 * Functions to parse and manipulate the additional data tables added
 * to optimized .dex files.
 */

#include <zlib.h>

#include "DexOptData.h"

/*
 * Check to see if a given data pointer is a valid double-word-aligned
 * pointer into the given memory range (from start inclusive to end
 * exclusive). Returns true if valid.
 */
static bool isValidPointer(const void* ptr, const void* start, const void* end)
{
    return (ptr >= start) && (ptr < end) && (((u4) ptr & 7) == 0);
}

/* (documented in header file) */
u4 dexComputeOptChecksum(const DexOptHeader* pOptHeader)
{
    const u1* start = (const u1*) pOptHeader + pOptHeader->depsOffset;
    const u1* end = (const u1*) pOptHeader +
        pOptHeader->optOffset + pOptHeader->optLength;

    uLong adler = adler32(0L, Z_NULL, 0);

    return (u4) adler32(adler, start, end - start);
}

/*
 * Parse out an index map entry, advancing "*pData" and reducing "*pSize".
 */
static bool parseIndexMapEntry(const u1** pData, u4* pSize, bool expanding,
    u4* pFullCount, u4* pReducedCount, const u2** pMap)
{
    const u4* wordPtr = (const u4*) *pData;
    u4 size = *pSize;
    u4 mapCount;

    if (expanding) {
        if (size < 4)
            return false;
        mapCount = *pReducedCount = *wordPtr++;
        *pFullCount = (u4) -1;
        size -= sizeof(u4);
    } else {
        if (size < 8)
            return false;
        mapCount = *pFullCount = *wordPtr++;
        *pReducedCount = *wordPtr++;
        size -= sizeof(u4) * 2;
    }

    u4 mapSize = mapCount * sizeof(u2);

    if (size < mapSize)
        return false;
    *pMap = (const u2*) wordPtr;
    size -= mapSize;

    /* advance the pointer */
    const u1* ptr = (const u1*) wordPtr;
    ptr += (mapSize + 3) & ~0x3;

    /* update pass-by-reference values */
    *pData = (const u1*) ptr;
    *pSize = size;

    return true;
}

/*
 * Set up some pointers into the mapped data.
 *
 * See analysis/ReduceConstants.c for the data layout description.
 */
static bool parseIndexMap(DexFile* pDexFile, const u1* data, u4 size,
    bool expanding)
{
    if (!parseIndexMapEntry(&data, &size, expanding,
            &pDexFile->indexMap.classFullCount,
            &pDexFile->indexMap.classReducedCount,
            &pDexFile->indexMap.classMap))
    {
        return false;
    }

    if (!parseIndexMapEntry(&data, &size, expanding,
            &pDexFile->indexMap.methodFullCount,
            &pDexFile->indexMap.methodReducedCount,
            &pDexFile->indexMap.methodMap))
    {
        return false;
    }

    if (!parseIndexMapEntry(&data, &size, expanding,
            &pDexFile->indexMap.fieldFullCount,
            &pDexFile->indexMap.fieldReducedCount,
            &pDexFile->indexMap.fieldMap))
    {
        return false;
    }

    if (!parseIndexMapEntry(&data, &size, expanding,
            &pDexFile->indexMap.stringFullCount,
            &pDexFile->indexMap.stringReducedCount,
            &pDexFile->indexMap.stringMap))
    {
        return false;
    }

    if (expanding) {
        /*
         * The map includes the "reduced" counts; pull the original counts
         * out of the DexFile so that code has a consistent source.
         */
        assert(pDexFile->indexMap.classFullCount == (u4) -1);
        assert(pDexFile->indexMap.methodFullCount == (u4) -1);
        assert(pDexFile->indexMap.fieldFullCount == (u4) -1);
        assert(pDexFile->indexMap.stringFullCount == (u4) -1);

#if 0   // TODO: not available yet -- do later or just skip this
        pDexFile->indexMap.classFullCount =
            pDexFile->pHeader->typeIdsSize;
        pDexFile->indexMap.methodFullCount =
            pDexFile->pHeader->methodIdsSize;
        pDexFile->indexMap.fieldFullCount =
            pDexFile->pHeader->fieldIdsSize;
        pDexFile->indexMap.stringFullCount =
            pDexFile->pHeader->stringIdsSize;
#endif
    }

    LOGI("Class : %u %u %u\n",
        pDexFile->indexMap.classFullCount,
        pDexFile->indexMap.classReducedCount,
        pDexFile->indexMap.classMap[0]);
    LOGI("Method: %u %u %u\n",
        pDexFile->indexMap.methodFullCount,
        pDexFile->indexMap.methodReducedCount,
        pDexFile->indexMap.methodMap[0]);
    LOGI("Field : %u %u %u\n",
        pDexFile->indexMap.fieldFullCount,
        pDexFile->indexMap.fieldReducedCount,
        pDexFile->indexMap.fieldMap[0]);
    LOGI("String: %u %u %u\n",
        pDexFile->indexMap.stringFullCount,
        pDexFile->indexMap.stringReducedCount,
        pDexFile->indexMap.stringMap[0]);

    return true;
}

/* (documented in header file) */
bool dexParseOptData(const u1* data, size_t length, DexFile* pDexFile)
{
    const void* pOptStart = data + pDexFile->pOptHeader->optOffset;
    const void* pOptEnd = data + length;
    const u4* pOpt = pOptStart;
    u4 optLength = (const u1*) pOptEnd - (const u1*) pOptStart;
    u4 indexMapType = 0;

    /*
     * Make sure the opt data start is in range and aligned. This may
     * seem like a superfluous check, but (a) if the file got
     * truncated, it might turn out that pOpt >= pOptEnd; and (b)
     * if the opt data header got corrupted, pOpt might not be
     * properly aligned. This test will catch both of these cases.
     */
    if (!isValidPointer(pOpt, pOptStart, pOptEnd)) {
        LOGE("Bogus opt data start pointer\n");
        return false;
    }

    /* Make sure that the opt data length is a whole number of words. */
    if ((optLength & 3) != 0) {
        LOGE("Unaligned opt data area end\n");
        return false;
    }

    /*
     * Make sure that the opt data area is large enough to have at least
     * one chunk header.
     */
    if (optLength < 8) {
        LOGE("Undersized opt data area (%u)\n", optLength);
        return false;
    }

    /* Process chunks until we see the end marker. */
    while (*pOpt != kDexChunkEnd) {
        if (!isValidPointer(pOpt + 2, pOptStart, pOptEnd)) {
            LOGE("Bogus opt data content pointer at offset %u\n",
                    ((const u1*) pOpt) - data);
            return false;
        }

        u4 size = *(pOpt + 1);
        const u1* pOptData = (const u1*) (pOpt + 2);

        /*
         * The rounded size is 64-bit aligned and includes +8 for the
         * type/size header (which was extracted immediately above).
         */
        u4 roundedSize = (size + 8 + 7) & ~7;
        const u4* pNextOpt = pOpt + (roundedSize / sizeof(u4));

        if (!isValidPointer(pNextOpt, pOptStart, pOptEnd)) {
            LOGE("Opt data area problem for chunk of size %u at offset %u\n",
                    size, ((const u1*) pOpt) - data);
            return false;
        }

        switch (*pOpt) {
        case kDexChunkClassLookup:
            pDexFile->pClassLookup = (const DexClassLookup*) pOptData;
            break;
        case kDexChunkReducingIndexMap:
            LOGI("+++ found reducing index map, size=%u\n", size);
            if (!parseIndexMap(pDexFile, pOptData, size, false)) {
                LOGE("Failed parsing reducing index map\n");
                return false;
            }
            indexMapType = *pOpt;
            break;
        case kDexChunkExpandingIndexMap:
            LOGI("+++ found expanding index map, size=%u\n", size);
            if (!parseIndexMap(pDexFile, pOptData, size, true)) {
                LOGE("Failed parsing expanding index map\n");
                return false;
            }
            indexMapType = *pOpt;
            break;
        case kDexChunkRegisterMaps:
            LOGV("+++ found register maps, size=%u\n", size);
            pDexFile->pRegisterMapPool = pOptData;
            break;
        default:
            LOGI("Unknown chunk 0x%08x (%c%c%c%c), size=%d in opt data area\n",
                *pOpt,
                (char) ((*pOpt) >> 24), (char) ((*pOpt) >> 16),
                (char) ((*pOpt) >> 8),  (char)  (*pOpt),
                size);
            break;
        }

        pOpt = pNextOpt;
    }

#if 0   // TODO: propagate expected map type from the VM through the API
    /*
     * If we're configured to expect an index map, and we don't find one,
     * reject this DEX so we'll regenerate it.  Also, if we found an
     * "expanding" map but we're not configured to use it, we have to fail
     * because the constants aren't usable without translation.
     */
    if (indexMapType != expectedIndexMapType) {
        LOGW("Incompatible index map configuration: found 0x%04x, need %d\n",
            indexMapType, DVM_REDUCE_CONSTANTS);
        return false;
    }
#endif

    return true;
}
