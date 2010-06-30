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
 * This code generate "register maps" for Dalvik bytecode.  In a stack-based
 * VM we might call these "stack maps".  They are used to increase the
 * precision in the garbage collector when scanning references in the
 * interpreter thread stacks.
 */
#include "Dalvik.h"
#include "analysis/CodeVerify.h"
#include "analysis/RegisterMap.h"
#include "libdex/DexCatch.h"
#include "libdex/InstrUtils.h"
#include "libdex/Leb128.h"

#include <stddef.h>

/* double-check the compression */
#define REGISTER_MAP_VERIFY     false

/* verbose logging */
#define REGISTER_MAP_VERBOSE    false

//#define REGISTER_MAP_STATS

// fwd
static void outputTypeVector(const RegType* regs, int insnRegCount, u1* data);
static bool verifyMap(VerifierData* vdata, const RegisterMap* pMap);
static int compareMaps(const RegisterMap* pMap1, const RegisterMap* pMap2);

#ifdef REGISTER_MAP_STATS
static void computeMapStats(RegisterMap* pMap, const Method* method);
#endif
static RegisterMap* compressMapDifferential(const RegisterMap* pMap,\
    const Method* meth);
static RegisterMap* uncompressMapDifferential(const RegisterMap* pMap);

#ifdef REGISTER_MAP_STATS
/*
 * Generate some statistics on the register maps we create and use.
 */
#define kMaxGcPointGap      50
#define kUpdatePosnMinRegs  24
#define kNumUpdatePosns     8
#define kMaxDiffBits        20
typedef struct MapStats {
    /*
     * Buckets measuring the distance between GC points.  This tells us how
     * many bits we need to encode the advancing program counter.  We ignore
     * some of the "long tail" entries.
     */
    int gcPointGap[kMaxGcPointGap];

    /*
     * Number of gaps.  Equal to (number of gcPoints - number of methods),
     * since the computation isn't including the initial gap.
     */
    int gcGapCount;

    /*
     * Number of gaps.
     */
    int totalGcPointCount;

    /*
     * For larger methods (>= 24 registers), measure in which octant register
     * updates occur.  This should help us understand whether register
     * changes tend to cluster in the low regs even for large methods.
     */
    int updatePosn[kNumUpdatePosns];

    /*
     * For all methods, count up the number of changes to registers < 16
     * and >= 16.
     */
    int updateLT16;
    int updateGE16;

    /*
     * Histogram of the number of bits that differ between adjacent entries.
     */
    int numDiffBits[kMaxDiffBits];


    /*
     * Track the number of expanded maps, and the heap space required to
     * hold them.
     */
    int numExpandedMaps;
    int totalExpandedMapSize;
} MapStats;
#endif

/*
 * Prepare some things.
 */
bool dvmRegisterMapStartup(void)
{
#ifdef REGISTER_MAP_STATS
    MapStats* pStats = calloc(1, sizeof(MapStats));
    gDvm.registerMapStats = pStats;
#endif
    return true;
}

/*
 * Clean up.
 */
void dvmRegisterMapShutdown(void)
{
#ifdef REGISTER_MAP_STATS
    free(gDvm.registerMapStats);
#endif
}

/*
 * Write stats to log file.
 */
void dvmRegisterMapDumpStats(void)
{
#ifdef REGISTER_MAP_STATS
    MapStats* pStats = (MapStats*) gDvm.registerMapStats;
    int i, end;

    for (end = kMaxGcPointGap-1; end >= 0; end--) {
        if (pStats->gcPointGap[end] != 0)
            break;
    }

    LOGI("Register Map gcPointGap stats (diff count=%d, total=%d):\n",
        pStats->gcGapCount, pStats->totalGcPointCount);
    assert(pStats->gcPointGap[0] == 0);
    for (i = 1; i <= end; i++) {
        LOGI(" %2d %d\n", i, pStats->gcPointGap[i]);
    }


    for (end = kMaxDiffBits-1; end >= 0; end--) {
        if (pStats->numDiffBits[end] != 0)
            break;
    }

    LOGI("Register Map bit difference stats:\n");
    for (i = 0; i <= end; i++) {
        LOGI(" %2d %d\n", i, pStats->numDiffBits[i]);
    }


    LOGI("Register Map update position stats (lt16=%d ge16=%d):\n",
        pStats->updateLT16, pStats->updateGE16);
    for (i = 0; i < kNumUpdatePosns; i++) {
        LOGI(" %2d %d\n", i, pStats->updatePosn[i]);
    }
#endif
}


/*
 * ===========================================================================
 *      Map generation
 * ===========================================================================
 */

/*
 * Generate the register map for a method that has just been verified
 * (i.e. we're doing this as part of verification).
 *
 * For type-precise determination we have all the data we need, so we
 * just need to encode it in some clever fashion.
 *
 * Returns a pointer to a newly-allocated RegisterMap, or NULL on failure.
 */
RegisterMap* dvmGenerateRegisterMapV(VerifierData* vdata)
{
    static const int kHeaderSize = offsetof(RegisterMap, data);
    RegisterMap* pMap = NULL;
    RegisterMap* pResult = NULL;
    RegisterMapFormat format;
    u1 regWidth;
    u1* mapData;
    int i, bytesForAddr, gcPointCount;
    int bufSize;

    if (vdata->method->registersSize >= 2048) {
        LOGE("ERROR: register map can't handle %d registers\n",
            vdata->method->registersSize);
        goto bail;
    }
    regWidth = (vdata->method->registersSize + 7) / 8;

    /*
     * Decide if we need 8 or 16 bits to hold the address.  Strictly speaking
     * we only need 16 bits if we actually encode an address >= 256 -- if
     * the method has a section at the end without GC points (e.g. array
     * data) we don't need to count it.  The situation is unusual, and
     * detecting it requires scanning the entire method, so we don't bother.
     */
    if (vdata->insnsSize < 256) {
        format = kRegMapFormatCompact8;
        bytesForAddr = 1;
    } else {
        format = kRegMapFormatCompact16;
        bytesForAddr = 2;
    }

    /*
     * Count up the number of GC point instructions.
     *
     * NOTE: this does not automatically include the first instruction,
     * since we don't count method entry as a GC point.
     */
    gcPointCount = 0;
    for (i = 0; i < (int) vdata->insnsSize; i++) {
        if (dvmInsnIsGcPoint(vdata->insnFlags, i))
            gcPointCount++;
    }
    if (gcPointCount >= 65536) {
        /* we could handle this, but in practice we don't get near this */
        LOGE("ERROR: register map can't handle %d gc points in one method\n",
            gcPointCount);
        goto bail;
    }

    /*
     * Allocate a buffer to hold the map data.
     */
    bufSize = kHeaderSize + gcPointCount * (bytesForAddr + regWidth);

    LOGV("+++ grm: %s.%s (adr=%d gpc=%d rwd=%d bsz=%d)\n",
        vdata->method->clazz->descriptor, vdata->method->name,
        bytesForAddr, gcPointCount, regWidth, bufSize);

    pMap = (RegisterMap*) malloc(bufSize);
    dvmRegisterMapSetFormat(pMap, format);
    dvmRegisterMapSetOnHeap(pMap, true);
    dvmRegisterMapSetRegWidth(pMap, regWidth);
    dvmRegisterMapSetNumEntries(pMap, gcPointCount);

    /*
     * Populate it.
     */
    mapData = pMap->data;
    for (i = 0; i < (int) vdata->insnsSize; i++) {
        if (dvmInsnIsGcPoint(vdata->insnFlags, i)) {
            assert(vdata->addrRegs[i] != NULL);
            if (format == kRegMapFormatCompact8) {
                *mapData++ = i;
            } else /*kRegMapFormatCompact16*/ {
                *mapData++ = i & 0xff;
                *mapData++ = i >> 8;
            }
            outputTypeVector(vdata->addrRegs[i], vdata->insnRegCount, mapData);
            mapData += regWidth;
        }
    }

    LOGV("mapData=%p pMap=%p bufSize=%d\n", mapData, pMap, bufSize);
    assert(mapData - (const u1*) pMap == bufSize);

    if (REGISTER_MAP_VERIFY && !verifyMap(vdata, pMap))
        goto bail;
#ifdef REGISTER_MAP_STATS
    computeMapStats(pMap, vdata->method);
#endif

    /*
     * Try to compress the map.
     */
    RegisterMap* pCompMap;

    pCompMap = compressMapDifferential(pMap, vdata->method);
    if (pCompMap != NULL) {
        if (REGISTER_MAP_VERIFY) {
            /*
             * Expand the compressed map we just created, and compare it
             * to the original.  Abort the VM if it doesn't match up.
             */
            RegisterMap* pUncompMap;
            pUncompMap = uncompressMapDifferential(pCompMap);
            if (pUncompMap == NULL) {
                LOGE("Map failed to uncompress - %s.%s\n",
                    vdata->method->clazz->descriptor,
                    vdata->method->name);
                free(pCompMap);
                /* bad - compression is broken or we're out of memory */
                dvmAbort();
            } else {
                if (compareMaps(pMap, pUncompMap) != 0) {
                    LOGE("Map comparison failed - %s.%s\n",
                        vdata->method->clazz->descriptor,
                        vdata->method->name);
                    free(pCompMap);
                    /* bad - compression is broken */
                    dvmAbort();
                }

                /* verify succeeded */
                free(pUncompMap);
            }
        }

        if (REGISTER_MAP_VERBOSE) {
            LOGD("Good compress on %s.%s\n",
                vdata->method->clazz->descriptor,
                vdata->method->name);
        }
        free(pMap);
        pMap = pCompMap;
    } else {
        if (REGISTER_MAP_VERBOSE) {
            LOGD("Unable to compress %s.%s (ent=%d rw=%d)\n",
                vdata->method->clazz->descriptor,
                vdata->method->name,
                dvmRegisterMapGetNumEntries(pMap),
                dvmRegisterMapGetRegWidth(pMap));
        }
    }

    pResult = pMap;

bail:
    return pResult;
}

/*
 * Release the storage held by a RegisterMap.
 */
void dvmFreeRegisterMap(RegisterMap* pMap)
{
    if (pMap == NULL)
        return;

    assert(dvmRegisterMapGetOnHeap(pMap));
    free(pMap);
}

/*
 * Determine if the RegType value is a reference type.
 *
 * Ordinarily we include kRegTypeZero in the "is it a reference"
 * check.  There's no value in doing so here, because we know
 * the register can't hold anything but zero.
 */
static inline bool isReferenceType(RegType type)
{
    return (type > kRegTypeMAX || type == kRegTypeUninit);
}

/*
 * Given a line of registers, output a bit vector that indicates whether
 * or not the register holds a reference type (which could be null).
 *
 * We use '1' to indicate it's a reference, '0' for anything else (numeric
 * value, uninitialized data, merge conflict).  Register 0 will be found
 * in the low bit of the first byte.
 */
static void outputTypeVector(const RegType* regs, int insnRegCount, u1* data)
{
    u1 val = 0;
    int i;

    for (i = 0; i < insnRegCount; i++) {
        RegType type = *regs++;
        val >>= 1;
        if (isReferenceType(type))
            val |= 0x80;        /* set hi bit */

        if ((i & 0x07) == 7)
            *data++ = val;
    }
    if ((i & 0x07) != 0) {
        /* flush bits from last byte */
        val >>= 8 - (i & 0x07);
        *data++ = val;
    }
}

/*
 * Print the map as a series of binary strings.
 *
 * Pass in method->registersSize if known, or -1 if not.
 */
static void dumpRegisterMap(const RegisterMap* pMap, int registersSize)
{
    const u1* rawMap = pMap->data;
    const RegisterMapFormat format = dvmRegisterMapGetFormat(pMap);
    const int numEntries = dvmRegisterMapGetNumEntries(pMap);
    const int regWidth = dvmRegisterMapGetRegWidth(pMap);
    int addrWidth;

    switch (format) {
    case kRegMapFormatCompact8:
        addrWidth = 1;
        break;
    case kRegMapFormatCompact16:
        addrWidth = 2;
        break;
    default:
        /* can't happen */
        LOGE("Can only dump Compact8 / Compact16 maps (not %d)\n", format);
        return;
    }

    if (registersSize < 0)
        registersSize = 8 * regWidth;
    assert(registersSize <= regWidth * 8);

    int ent;
    for (ent = 0; ent < numEntries; ent++) {
        int i, addr;

        addr = *rawMap++;
        if (addrWidth > 1)
            addr |= (*rawMap++) << 8;

        const u1* dataStart = rawMap;
        u1 val = 0;

        /* create binary string */
        char outBuf[registersSize +1];
        for (i = 0; i < registersSize; i++) {
            val >>= 1;
            if ((i & 0x07) == 0)
                val = *rawMap++;

            outBuf[i] = '0' + (val & 0x01);
        }
        outBuf[i] = '\0';

        /* back up and create hex dump */
        char hexBuf[regWidth * 3 +1];
        char* cp = hexBuf;
        rawMap = dataStart;
        for (i = 0; i < regWidth; i++) {
            sprintf(cp, " %02x", *rawMap++);
            cp += 3;
        }
        hexBuf[i * 3] = '\0';

        LOGD("  %04x %s %s\n", addr, outBuf, hexBuf);
    }
}

/*
 * Double-check the map.
 *
 * We run through all of the data in the map, and compare it to the original.
 * Only works on uncompressed data.
 */
static bool verifyMap(VerifierData* vdata, const RegisterMap* pMap)
{
    const u1* rawMap = pMap->data;
    const RegisterMapFormat format = dvmRegisterMapGetFormat(pMap);
    const int numEntries = dvmRegisterMapGetNumEntries(pMap);
    int ent;
    bool dumpMap = false;

    if (false) {
        const char* cd = "Landroid/net/http/Request;";
        const char* mn = "readResponse";
        if (strcmp(vdata->method->clazz->descriptor, cd) == 0 &&
            strcmp(vdata->method->name, mn) == 0)
        {
            char* desc;
            desc = dexProtoCopyMethodDescriptor(&vdata->method->prototype);
            LOGI("Map for %s.%s %s\n", vdata->method->clazz->descriptor,
                vdata->method->name, desc);
            free(desc);

            dumpMap = true;
        }
    }

    if ((vdata->method->registersSize + 7) / 8 != pMap->regWidth) {
        LOGE("GLITCH: registersSize=%d, regWidth=%d\n",
            vdata->method->registersSize, pMap->regWidth);
        return false;
    }

    for (ent = 0; ent < numEntries; ent++) {
        int addr;

        switch (format) {
        case kRegMapFormatCompact8:
            addr = *rawMap++;
            break;
        case kRegMapFormatCompact16:
            addr = *rawMap++;
            addr |= (*rawMap++) << 8;
            break;
        default:
            /* shouldn't happen */
            LOGE("GLITCH: bad format (%d)", format);
            dvmAbort();
        }

        const RegType* regs = vdata->addrRegs[addr];
        if (regs == NULL) {
            LOGE("GLITCH: addr %d has no data\n", addr);
            return false;
        }

        u1 val = 0;
        int i;

        for (i = 0; i < vdata->method->registersSize; i++) {
            bool bitIsRef, regIsRef;

            val >>= 1;
            if ((i & 0x07) == 0) {
                /* load next byte of data */
                val = *rawMap++;
            }

            bitIsRef = val & 0x01;

            RegType type = regs[i];
            regIsRef = isReferenceType(type);

            if (bitIsRef != regIsRef) {
                LOGE("GLITCH: addr %d reg %d: bit=%d reg=%d(%d)\n",
                    addr, i, bitIsRef, regIsRef, type);
                return false;
            }
        }

        /* rawMap now points to the address field of the next entry */
    }

    if (dumpMap)
        dumpRegisterMap(pMap, vdata->method->registersSize);

    return true;
}


/*
 * ===========================================================================
 *      DEX generation & parsing
 * ===========================================================================
 */

/*
 * Advance "ptr" to ensure 32-bit alignment.
 */
static inline u1* align32(u1* ptr)
{
    return (u1*) (((int) ptr + 3) & ~0x03);
}

/*
 * Compute the size, in bytes, of a register map.
 */
static size_t computeRegisterMapSize(const RegisterMap* pMap)
{
    static const int kHeaderSize = offsetof(RegisterMap, data);
    u1 format = dvmRegisterMapGetFormat(pMap);
    u2 numEntries = dvmRegisterMapGetNumEntries(pMap);

    assert(pMap != NULL);

    switch (format) {
    case kRegMapFormatNone:
        return 1;
    case kRegMapFormatCompact8:
        return kHeaderSize + (1 + pMap->regWidth) * numEntries;
    case kRegMapFormatCompact16:
        return kHeaderSize + (2 + pMap->regWidth) * numEntries;
    case kRegMapFormatDifferential:
        {
            /* kHeaderSize + decoded ULEB128 length */
            const u1* ptr = pMap->data;
            int len = readUnsignedLeb128(&ptr);
            return len + (ptr - (u1*) pMap);
        }
    default:
        LOGE("Bad register map format %d\n", format);
        dvmAbort();
        return 0;
    }
}

/*
 * Output the map for a single method, if it has one.
 *
 * Abstract and native methods have no map.  All others are expected to
 * have one, since we know the class verified successfully.
 *
 * This strips the "allocated on heap" flag from the format byte, so that
 * direct-mapped maps are correctly identified as such.
 */
static bool writeMapForMethod(const Method* meth, u1** pPtr)
{
    if (meth->registerMap == NULL) {
        if (!dvmIsAbstractMethod(meth) && !dvmIsNativeMethod(meth)) {
            LOGW("Warning: no map available for %s.%s\n",
                meth->clazz->descriptor, meth->name);
            /* weird, but keep going */
        }
        *(*pPtr)++ = kRegMapFormatNone;
        return true;
    }

    /* serialize map into the buffer */
    size_t mapSize = computeRegisterMapSize(meth->registerMap);
    memcpy(*pPtr, meth->registerMap, mapSize);

    /* strip the "on heap" flag out of the format byte, which is always first */
    assert(**pPtr == meth->registerMap->format);
    **pPtr &= ~(kRegMapFormatOnHeap);

    *pPtr += mapSize;

    return true;
}

/*
 * Write maps for all methods in the specified class to the buffer, which
 * can hold at most "length" bytes.  "*pPtr" will be advanced past the end
 * of the data we write.
 */
static bool writeMapsAllMethods(DvmDex* pDvmDex, const ClassObject* clazz,
    u1** pPtr, size_t length)
{
    RegisterMapMethodPool* pMethodPool;
    u1* ptr = *pPtr;
    int i, methodCount;

    /* artificial limit */
    if (clazz->virtualMethodCount + clazz->directMethodCount >= 65536) {
        LOGE("Too many methods in %s\n", clazz->descriptor);
        return false;
    }

    pMethodPool = (RegisterMapMethodPool*) ptr;
    ptr += offsetof(RegisterMapMethodPool, methodData);
    methodCount = 0;

    /*
     * Run through all methods, direct then virtual.  The class loader will
     * traverse them in the same order.  (We could split them into two
     * distinct pieces, but there doesn't appear to be any value in doing
     * so other than that it makes class loading slightly less fragile.)
     *
     * The class loader won't know about miranda methods at the point
     * where it parses this, so we omit those.
     *
     * TODO: consider omitting all native/abstract definitions.  Should be
     * safe, though we lose the ability to sanity-check against the
     * method counts in the DEX file.
     */
    for (i = 0; i < clazz->directMethodCount; i++) {
        const Method* meth = &clazz->directMethods[i];
        if (dvmIsMirandaMethod(meth))
            continue;
        if (!writeMapForMethod(&clazz->directMethods[i], &ptr)) {
            return false;
        }
        methodCount++;
        //ptr = align32(ptr);
    }

    for (i = 0; i < clazz->virtualMethodCount; i++) {
        const Method* meth = &clazz->virtualMethods[i];
        if (dvmIsMirandaMethod(meth))
            continue;
        if (!writeMapForMethod(&clazz->virtualMethods[i], &ptr)) {
            return false;
        }
        methodCount++;
        //ptr = align32(ptr);
    }

    pMethodPool->methodCount = methodCount;

    *pPtr = ptr;
    return true;
}

/*
 * Write maps for all classes to the specified buffer, which can hold at
 * most "length" bytes.
 *
 * Returns the actual length used, or 0 on failure.
 */
static size_t writeMapsAllClasses(DvmDex* pDvmDex, u1* basePtr, size_t length)
{
    DexFile* pDexFile = pDvmDex->pDexFile;
    u4 count = pDexFile->pHeader->classDefsSize;
    RegisterMapClassPool* pClassPool;
    u4* offsetTable;
    u1* ptr = basePtr;
    u4 idx;

    assert(gDvm.optimizing);

    pClassPool = (RegisterMapClassPool*) ptr;
    ptr += offsetof(RegisterMapClassPool, classDataOffset);
    offsetTable = (u4*) ptr;
    ptr += count * sizeof(u4);

    pClassPool->numClasses = count;

    /*
     * We want an entry for every class, loaded or not.
     */
    for (idx = 0; idx < count; idx++) {
        const DexClassDef* pClassDef;
        const char* classDescriptor;
        ClassObject* clazz;

        pClassDef = dexGetClassDef(pDexFile, idx);
        classDescriptor = dexStringByTypeIdx(pDexFile, pClassDef->classIdx);

        /*
         * All classes have been loaded into the bootstrap class loader.
         * If we can find it, and it was successfully pre-verified, we
         * run through its methods and add the register maps.
         *
         * If it wasn't pre-verified then we know it can't have any
         * register maps.  Classes that can't be loaded or failed
         * verification get an empty slot in the index.
         */
        clazz = NULL;
        if ((pClassDef->accessFlags & CLASS_ISPREVERIFIED) != 0)
            clazz = dvmLookupClass(classDescriptor, NULL, false);

        if (clazz != NULL) {
            offsetTable[idx] = ptr - basePtr;
            LOGVV("%d -> offset %d (%p-%p)\n",
                idx, offsetTable[idx], ptr, basePtr);

            if (!writeMapsAllMethods(pDvmDex, clazz, &ptr,
                    length - (ptr - basePtr)))
            {
                return 0;
            }

            ptr = align32(ptr);
            LOGVV("Size %s (%d+%d methods): %d\n", clazz->descriptor,
                clazz->directMethodCount, clazz->virtualMethodCount,
                (ptr - basePtr) - offsetTable[idx]);
        } else {
            LOGV("%4d NOT mapadding '%s'\n", idx, classDescriptor);
            assert(offsetTable[idx] == 0);
        }
    }

    if (ptr - basePtr >= (int)length) {
        /* a bit late */
        LOGE("Buffer overrun\n");
        dvmAbort();
    }

    return ptr - basePtr;
}

/*
 * Generate a register map set for all verified classes in "pDvmDex".
 */
RegisterMapBuilder* dvmGenerateRegisterMaps(DvmDex* pDvmDex)
{
    RegisterMapBuilder* pBuilder;

    pBuilder = (RegisterMapBuilder*) calloc(1, sizeof(RegisterMapBuilder));
    if (pBuilder == NULL)
        return NULL;

    /*
     * We have a couple of options here:
     *  (1) Compute the size of the output, and malloc a buffer.
     *  (2) Create a "large-enough" anonymous mmap region.
     *
     * The nice thing about option #2 is that we don't have to traverse
     * all of the classes and methods twice.  The risk is that we might
     * not make the region large enough.  Since the pages aren't mapped
     * until used we can allocate a semi-absurd amount of memory without
     * worrying about the effect on the rest of the system.
     *
     * The basic encoding on the largest jar file requires about 1MB of
     * storage.  We map out 4MB here.  (TODO: guarantee that the last
     * page of the mapping is marked invalid, so we reliably fail if
     * we overrun.)
     */
    if (sysCreatePrivateMap(4 * 1024 * 1024, &pBuilder->memMap) != 0) {
        free(pBuilder);
        return NULL;
    }

    /*
     * Create the maps.
     */
    size_t actual = writeMapsAllClasses(pDvmDex, (u1*)pBuilder->memMap.addr,
                                        pBuilder->memMap.length);
    if (actual == 0) {
        dvmFreeRegisterMapBuilder(pBuilder);
        return NULL;
    }

    LOGV("TOTAL size of register maps: %d\n", actual);

    pBuilder->data = pBuilder->memMap.addr;
    pBuilder->size = actual;
    return pBuilder;
}

/*
 * Free the builder.
 */
void dvmFreeRegisterMapBuilder(RegisterMapBuilder* pBuilder)
{
    if (pBuilder == NULL)
        return;

    sysReleaseShmem(&pBuilder->memMap);
    free(pBuilder);
}


/*
 * Find the data for the specified class.
 *
 * If there's no register map data, or none for this class, we return NULL.
 */
const void* dvmRegisterMapGetClassData(const DexFile* pDexFile, u4 classIdx,
    u4* pNumMaps)
{
    const RegisterMapClassPool* pClassPool;
    const RegisterMapMethodPool* pMethodPool;

    pClassPool = (const RegisterMapClassPool*) pDexFile->pRegisterMapPool;
    if (pClassPool == NULL)
        return NULL;

    if (classIdx >= pClassPool->numClasses) {
        LOGE("bad class index (%d vs %d)\n", classIdx, pClassPool->numClasses);
        dvmAbort();
    }

    u4 classOffset = pClassPool->classDataOffset[classIdx];
    if (classOffset == 0) {
        LOGV("+++ no map for classIdx=%d\n", classIdx);
        return NULL;
    }

    pMethodPool =
        (const RegisterMapMethodPool*) (((u1*) pClassPool) + classOffset);
    if (pNumMaps != NULL)
        *pNumMaps = pMethodPool->methodCount;
    return pMethodPool->methodData;
}

/*
 * This advances "*pPtr" and returns its original value.
 */
const RegisterMap* dvmRegisterMapGetNext(const void** pPtr)
{
    const RegisterMap* pMap = *pPtr;

    *pPtr = /*align32*/(((u1*) pMap) + computeRegisterMapSize(pMap));
    LOGVV("getNext: %p -> %p (f=0x%x w=%d e=%d)\n",
        pMap, *pPtr, pMap->format, pMap->regWidth,
        dvmRegisterMapGetNumEntries(pMap));
    return pMap;
}


/*
 * ===========================================================================
 *      Utility functions
 * ===========================================================================
 */

/*
 * Return the data for the specified address, or NULL if not found.
 *
 * The result must be released with dvmReleaseRegisterMapLine().
 */
const u1* dvmRegisterMapGetLine(const RegisterMap* pMap, int addr)
{
    int addrWidth, lineWidth;
    u1 format = dvmRegisterMapGetFormat(pMap);
    u2 numEntries = dvmRegisterMapGetNumEntries(pMap);

    assert(numEntries > 0);

    switch (format) {
    case kRegMapFormatNone:
        return NULL;
    case kRegMapFormatCompact8:
        addrWidth = 1;
        break;
    case kRegMapFormatCompact16:
        addrWidth = 2;
        break;
    default:
        LOGE("Unknown format %d\n", format);
        dvmAbort();
        return NULL;
    }

    lineWidth = addrWidth + pMap->regWidth;

    /*
     * Find the appropriate entry.  Many maps are very small, some are very
     * large.
     */
    static const int kSearchThreshold = 8;
    const u1* data = NULL;
    int lineAddr;

    if (numEntries < kSearchThreshold) {
        int i;
        data = pMap->data;
        for (i = numEntries; i > 0; i--) {
            lineAddr = data[0];
            if (addrWidth > 1)
                lineAddr |= data[1] << 8;
            if (lineAddr == addr)
                return data + addrWidth;

            data += lineWidth;
        }
        assert(data == pMap->data + lineWidth * numEntries);
    } else {
        int hi, lo, mid;

        lo = 0;
        hi = numEntries -1;

        while (hi >= lo) {
            mid = (hi + lo) / 2;
            data = pMap->data + lineWidth * mid;

            lineAddr = data[0];
            if (addrWidth > 1)
                lineAddr |= data[1] << 8;

            if (addr > lineAddr) {
                lo = mid + 1;
            } else if (addr < lineAddr) {
                hi = mid - 1;
            } else {
                return data + addrWidth;
            }
        }
    }

    return NULL;
}

/*
 * Compare two register maps.
 *
 * Returns 0 if they're equal, nonzero if not.
 */
static int compareMaps(const RegisterMap* pMap1, const RegisterMap* pMap2)
{
    size_t size1, size2;

    size1 = computeRegisterMapSize(pMap1);
    size2 = computeRegisterMapSize(pMap2);
    if (size1 != size2) {
        LOGI("compareMaps: size mismatch (%zd vs %zd)\n", size1, size2);
        return -1;
    }

    if (memcmp(pMap1, pMap2, size1) != 0) {
        LOGI("compareMaps: content mismatch\n");
        return -1;
    }

    return 0;
}


/*
 * Get the expanded form of the register map associated with the method.
 *
 * If the map is already in one of the uncompressed formats, we return
 * immediately.  Otherwise, we expand the map and replace method's register
 * map pointer, freeing it if it was allocated on the heap.
 *
 * NOTE: this function is not synchronized; external locking is mandatory
 * (unless we're in the zygote, where single-threaded access is guaranteed).
 */
const RegisterMap* dvmGetExpandedRegisterMap0(Method* method)
{
    const RegisterMap* curMap = method->registerMap;
    RegisterMap* newMap;

    if (curMap == NULL)
        return NULL;

    /* sanity check to ensure this isn't called w/o external locking */
    /* (if we use this at a time other than during GC, fix/remove this test) */
    if (true) {
        if (!gDvm.zygote && dvmTryLockMutex(&gDvm.gcHeapLock) == 0) {
            LOGE("GLITCH: dvmGetExpandedRegisterMap not called at GC time\n");
            dvmAbort();
        }
    }

    RegisterMapFormat format = dvmRegisterMapGetFormat(curMap);
    switch (format) {
    case kRegMapFormatCompact8:
    case kRegMapFormatCompact16:
        if (REGISTER_MAP_VERBOSE) {
            if (dvmRegisterMapGetOnHeap(curMap)) {
                LOGD("RegMap: already expanded: %s.%s\n",
                    method->clazz->descriptor, method->name);
            } else {
                LOGD("RegMap: stored w/o compression: %s.%s\n",
                    method->clazz->descriptor, method->name);
            }
        }
        return curMap;
    case kRegMapFormatDifferential:
        newMap = uncompressMapDifferential(curMap);
        break;
    default:
        LOGE("Unknown format %d in dvmGetExpandedRegisterMap\n", format);
        dvmAbort();
        newMap = NULL;      // make gcc happy
    }

    if (newMap == NULL) {
        LOGE("Map failed to uncompress (fmt=%d) %s.%s\n",
            format, method->clazz->descriptor, method->name);
        return NULL;
    }

#ifdef REGISTER_MAP_STATS
    /*
     * Gather and display some stats.
     */
    {
        MapStats* pStats = (MapStats*) gDvm.registerMapStats;
        pStats->numExpandedMaps++;
        pStats->totalExpandedMapSize += computeRegisterMapSize(newMap);
        LOGD("RMAP: count=%d size=%d\n",
            pStats->numExpandedMaps, pStats->totalExpandedMapSize);
    }
#endif

    IF_LOGV() {
        char* desc = dexProtoCopyMethodDescriptor(&method->prototype);
        LOGV("Expanding map -> %s.%s:%s\n",
            method->clazz->descriptor, method->name, desc);
        free(desc);
    }

    /*
     * Update method, and free compressed map if it was sitting on the heap.
     */
    dvmSetRegisterMap(method, newMap);

    if (dvmRegisterMapGetOnHeap(curMap))
        dvmFreeRegisterMap((RegisterMap*) curMap);

    return newMap;
}


/*
 * ===========================================================================
 *      Map compression
 * ===========================================================================
 */

/*
Notes on map compression

The idea is to create a compressed form that will be uncompressed before
use, with the output possibly saved in a cache.  This means we can use an
approach that is unsuited for random access if we choose.

In the event that a map simply does not work with our compression scheme,
it's reasonable to store the map without compression.  In the future we
may want to have more than one compression scheme, and try each in turn,
retaining the best.  (We certainly want to keep the uncompressed form if it
turns out to be smaller or even slightly larger than the compressed form.)

Each entry consists of an address and a bit vector.  Adjacent entries are
strongly correlated, suggesting differential encoding.


Ideally we would avoid outputting adjacent entries with identical
bit vectors.  However, the register values at a given address do not
imply anything about the set of valid registers at subsequent addresses.
We therefore cannot omit an entry.

  If the thread stack has a PC at an address without a corresponding
  entry in the register map, we must conservatively scan the registers in
  that thread.  This can happen when single-stepping in the debugger,
  because the debugger is allowed to invoke arbitrary methods when
  a thread is stopped at a breakpoint.  If we can guarantee that a GC
  thread scan will never happen while the debugger has that thread stopped,
  then we can lift this restriction and simply omit entries that don't
  change the bit vector from its previous state.

Each entry advances the address value by at least 1 (measured in 16-bit
"code units").  Looking at the bootclasspath entries, advancing by 2 units
is most common.  Advances by 1 unit are far less common than advances by
2 units, but more common than 5, and things fall off rapidly.  Gaps of
up to 220 code units appear in some computationally intensive bits of code,
but are exceedingly rare.

If we sum up the number of transitions in a couple of ranges in framework.jar:
  [1,4]: 188998 of 218922 gaps (86.3%)
  [1,7]: 211647 of 218922 gaps (96.7%)
Using a 3-bit delta, with one value reserved as an escape code, should
yield good results for the address.

These results would change dramatically if we reduced the set of GC
points by e.g. removing instructions like integer divide that are only
present because they can throw and cause an allocation.

We also need to include an "initial gap", because the first few instructions
in a method may not be GC points.


By observation, many entries simply repeat the previous bit vector, or
change only one or two bits.  (This is with type-precise information;
the rate of change of bits will be different if live-precise information
is factored in).

Looking again at adjacent entries in framework.jar:
  0 bits changed: 63.0%
  1 bit changed: 32.2%
After that it falls off rapidly, e.g. the number of entries with 2 bits
changed is usually less than 1/10th of the number of entries with 1 bit
changed.  A solution that allows us to encode 0- or 1- bit changes
efficiently will do well.

We still need to handle cases where a large number of bits change.  We
probably want a way to drop in a full copy of the bit vector when it's
smaller than the representation of multiple bit changes.


The bit-change information can be encoded as an index that tells the
decoder to toggle the state.  We want to encode the index in as few bits
as possible, but we need to allow for fairly wide vectors (e.g. we have a
method with 175 registers).  We can deal with this in a couple of ways:
(1) use an encoding that assumes few registers and has an escape code
for larger numbers of registers; or (2) use different encodings based
on how many total registers the method has.  The choice depends to some
extent on whether methods with large numbers of registers tend to modify
the first 16 regs more often than the others.

The last N registers hold method arguments.  If the bytecode is expected
to be examined in a debugger, "dx" ensures that the contents of these
registers won't change.  Depending upon the encoding format, we may be
able to take advantage of this.  We still have to encode the initial
state, but we know we'll never have to output a bit change for the last
N registers.

Considering only methods with 16 or more registers, the "target octant"
for register changes looks like this:
  [ 43.1%, 16.4%, 6.5%, 6.2%, 7.4%, 8.8%, 9.7%, 1.8% ]
As expected, there are fewer changes at the end of the list where the
arguments are kept, and more changes at the start of the list because
register values smaller than 16 can be used in compact Dalvik instructions
and hence are favored for frequently-used values.  In general, the first
octant is considerably more active than later entries, the last octant
is much less active, and the rest are all about the same.

Looking at all bit changes in all methods, 94% are to registers 0-15.  The
encoding will benefit greatly by favoring the low-numbered registers.


Some of the smaller methods have identical maps, and space could be
saved by simply including a pointer to an earlier definition.  This would
be best accomplished by specifying a "pointer" format value, followed by
a 3-byte (or ULEB128) offset.  Implementing this would probably involve
generating a hash value for each register map and maintaining a hash table.

In some cases there are repeating patterns in the bit vector that aren't
adjacent.  These could benefit from a dictionary encoding.  This doesn't
really become useful until the methods reach a certain size though,
and managing the dictionary may incur more overhead than we want.

Large maps can be compressed significantly.  The trouble is that, when
we need to use them, we have to uncompress them onto the heap.  We may
get a better trade-off between storage size and heap usage by refusing to
compress large maps, so that they can be memory mapped and used directly.
(OTOH, only about 2% of the maps will ever actually be used.)


----- differential format -----

// common header
+00 1B format
+01 1B regWidth
+02 2B numEntries (little-endian)
+04 nB length in bytes of the data that follows, in ULEB128 format
       (not strictly necessary; allows determination of size w/o full parse)
+05+ 1B initial address (0-127), high bit set if max addr >= 256
+06+ nB initial value for bit vector

// for each entry
+00: CCCCBAAA

  AAA: address difference.  Values from 0 to 6 indicate an increment of 1
  to 7.  A value of 7 indicates that the address difference is large,
  and the next byte is a ULEB128-encoded difference value.

  B: determines the meaning of CCCC.

  CCCC: if B is 0, this is the number of the bit to toggle (0-15).
  If B is 1, this is a count of the number of changed bits (1-14).  A value
  of 0 means that no bits were changed, and a value of 15 indicates
  that enough bits were changed that it required less space to output
  the entire bit vector.

+01: (optional) ULEB128-encoded address difference

+01+: (optional) one or more ULEB128-encoded bit numbers, OR the entire
  bit vector.

The most common situation is an entry whose address has changed by 2-4
code units, has no changes or just a single bit change, and the changed
register is less than 16.  We should therefore be able to encode a large
number of entries with a single byte, which is half the size of the
Compact8 encoding method.
*/

/*
 * Compute some stats on an uncompressed register map.
 */
#ifdef REGISTER_MAP_STATS
static void computeMapStats(RegisterMap* pMap, const Method* method)
{
    MapStats* pStats = (MapStats*) gDvm.registerMapStats;
    const u1 format = dvmRegisterMapGetFormat(pMap);
    const u2 numEntries = dvmRegisterMapGetNumEntries(pMap);
    const u1* rawMap = pMap->data;
    const u1* prevData = NULL;
    int ent, addr, prevAddr = -1;

    for (ent = 0; ent < numEntries; ent++) {
        switch (format) {
        case kRegMapFormatCompact8:
            addr = *rawMap++;
            break;
        case kRegMapFormatCompact16:
            addr = *rawMap++;
            addr |= (*rawMap++) << 8;
            break;
        default:
            /* shouldn't happen */
            LOGE("GLITCH: bad format (%d)", format);
            dvmAbort();
        }

        const u1* dataStart = rawMap;

        pStats->totalGcPointCount++;

        /*
         * Gather "gap size" stats, i.e. the difference in addresses between
         * successive GC points.
         */
        if (prevData != NULL) {
            assert(prevAddr >= 0);
            int addrDiff = addr - prevAddr;

            if (addrDiff < 0) {
                LOGE("GLITCH: address went backward (0x%04x->0x%04x, %s.%s)\n",
                    prevAddr, addr, method->clazz->descriptor, method->name);
            } else if (addrDiff > kMaxGcPointGap) {
                if (REGISTER_MAP_VERBOSE) {
                    LOGI("HEY: addrDiff is %d, max %d (0x%04x->0x%04x %s.%s)\n",
                        addrDiff, kMaxGcPointGap, prevAddr, addr,
                        method->clazz->descriptor, method->name);
                }
                /* skip this one */
            } else {
                pStats->gcPointGap[addrDiff]++;
            }
            pStats->gcGapCount++;


            /*
             * Compare bit vectors in adjacent entries.  We want to count
             * up the number of bits that differ (to see if we frequently
             * change 0 or 1 bits) and get a sense for which part of the
             * vector changes the most often (near the start, middle, end).
             *
             * We only do the vector position quantization if we have at
             * least 16 registers in the method.
             */
            int numDiff = 0;
            float div = (float) kNumUpdatePosns / method->registersSize;
            int regByte;
            for (regByte = 0; regByte < pMap->regWidth; regByte++) {
                int prev, cur, bit;

                prev = prevData[regByte];
                cur = dataStart[regByte];

                for (bit = 0; bit < 8; bit++) {
                    if (((prev >> bit) & 1) != ((cur >> bit) & 1)) {
                        numDiff++;

                        int bitNum = regByte * 8 + bit;

                        if (bitNum < 16)
                            pStats->updateLT16++;
                        else
                            pStats->updateGE16++;

                        if (method->registersSize < 16)
                            continue;

                        if (bitNum >= method->registersSize) {
                            /* stuff off the end should be zero in both */
                            LOGE("WEIRD: bit=%d (%d/%d), prev=%02x cur=%02x\n",
                                bit, regByte, method->registersSize,
                                prev, cur);
                            assert(false);
                        }
                        int idx = (int) (bitNum * div);
                        if (!(idx >= 0 && idx < kNumUpdatePosns)) {
                            LOGE("FAIL: bitNum=%d (of %d) div=%.3f idx=%d\n",
                                bitNum, method->registersSize, div, idx);
                            assert(false);
                        }
                        pStats->updatePosn[idx]++;
                    }
                }
            }

            if (numDiff > kMaxDiffBits) {
                if (REGISTER_MAP_VERBOSE) {
                    LOGI("WOW: numDiff is %d, max %d\n", numDiff, kMaxDiffBits);
                }
            } else {
                pStats->numDiffBits[numDiff]++;
            }
        }

        /* advance to start of next line */
        rawMap += pMap->regWidth;

        prevAddr = addr;
        prevData = dataStart;
    }
}
#endif

/*
 * Compute the difference between two bit vectors.
 *
 * If "lebOutBuf" is non-NULL, we output the bit indices in ULEB128 format
 * as we go.  Otherwise, we just generate the various counts.
 *
 * The bit vectors are compared byte-by-byte, so any unused bits at the
 * end must be zero.
 *
 * Returns the number of bytes required to hold the ULEB128 output.
 *
 * If "pFirstBitChanged" or "pNumBitsChanged" are non-NULL, they will
 * receive the index of the first changed bit and the number of changed
 * bits, respectively.
 */
static int computeBitDiff(const u1* bits1, const u1* bits2, int byteWidth,
    int* pFirstBitChanged, int* pNumBitsChanged, u1* lebOutBuf)
{
    int numBitsChanged = 0;
    int firstBitChanged = -1;
    int lebSize = 0;
    int byteNum;

    /*
     * Run through the vectors, first comparing them at the byte level.  This
     * will yield a fairly quick result if nothing has changed between them.
     */
    for (byteNum = 0; byteNum < byteWidth; byteNum++) {
        u1 byte1 = *bits1++;
        u1 byte2 = *bits2++;
        if (byte1 != byte2) {
            /*
             * Walk through the byte, identifying the changed bits.
             */
            int bitNum;
            for (bitNum = 0; bitNum < 8; bitNum++) {
                if (((byte1 >> bitNum) & 0x01) != ((byte2 >> bitNum) & 0x01)) {
                    int bitOffset = (byteNum << 3) + bitNum;

                    if (firstBitChanged < 0)
                        firstBitChanged = bitOffset;
                    numBitsChanged++;

                    if (lebOutBuf == NULL) {
                        lebSize += unsignedLeb128Size(bitOffset);
                    } else {
                        u1* curBuf = lebOutBuf;
                        lebOutBuf = writeUnsignedLeb128(lebOutBuf, bitOffset);
                        lebSize += lebOutBuf - curBuf;
                    }
                }
            }
        }
    }

    if (numBitsChanged > 0)
        assert(firstBitChanged >= 0);

    if (pFirstBitChanged != NULL)
        *pFirstBitChanged = firstBitChanged;
    if (pNumBitsChanged != NULL)
        *pNumBitsChanged = numBitsChanged;

    return lebSize;
}

/*
 * Compress the register map with differential encoding.
 *
 * "meth" is only needed for debug output.
 *
 * On success, returns a newly-allocated RegisterMap.  If the map is not
 * compatible for some reason, or fails to get smaller, this will return NULL.
 */
static RegisterMap* compressMapDifferential(const RegisterMap* pMap,
    const Method* meth)
{
    RegisterMap* pNewMap = NULL;
    int origSize = computeRegisterMapSize(pMap);
    u1* tmpBuf = NULL;
    u1* tmpPtr;
    int addrWidth, regWidth, numEntries;
    bool debug = false;

    if (false &&
        strcmp(meth->clazz->descriptor, "Landroid/text/StaticLayout;") == 0 &&
        strcmp(meth->name, "generate") == 0)
    {
        debug = true;
    }

    u1 format = dvmRegisterMapGetFormat(pMap);
    switch (format) {
    case kRegMapFormatCompact8:
        addrWidth = 1;
        break;
    case kRegMapFormatCompact16:
        addrWidth = 2;
        break;
    default:
        LOGE("ERROR: can't compress map with format=%d\n", format);
        goto bail;
    }

    regWidth = dvmRegisterMapGetRegWidth(pMap);
    numEntries = dvmRegisterMapGetNumEntries(pMap);

    if (debug) {
        LOGI("COMPRESS: %s.%s aw=%d rw=%d ne=%d\n",
            meth->clazz->descriptor, meth->name,
            addrWidth, regWidth, numEntries);
        dumpRegisterMap(pMap, -1);
    }

    if (numEntries <= 1) {
        LOGV("Can't compress map with 0 or 1 entries\n");
        goto bail;
    }

    /*
     * We don't know how large the compressed data will be.  It's possible
     * for it to expand and become larger than the original.  The header
     * itself is variable-sized, so we generate everything into a temporary
     * buffer and then copy it to form-fitting storage once we know how big
     * it will be (and that it's smaller than the original).
     *
     * If we use a size that is equal to the size of the input map plus
     * a value longer than a single entry can possibly expand to, we need
     * only check for overflow at the end of each entry.  The worst case
     * for a single line is (1 + <ULEB8 address> + <full copy of vector>).
     * Addresses are 16 bits, so that's (1 + 3 + regWidth).
     *
     * The initial address offset and bit vector will take up less than
     * or equal to the amount of space required when uncompressed -- large
     * initial offsets are rejected.
     */
    tmpBuf = (u1*) malloc(origSize + (1 + 3 + regWidth));
    if (tmpBuf == NULL)
        goto bail;

    tmpPtr = tmpBuf;

    const u1* mapData = pMap->data;
    const u1* prevBits;
    u2 addr, prevAddr;

    addr = *mapData++;
    if (addrWidth > 1)
        addr |= (*mapData++) << 8;

    if (addr >= 128) {
        LOGV("Can't compress map with starting address >= 128\n");
        goto bail;
    }

    /*
     * Start by writing the initial address and bit vector data.  The high
     * bit of the initial address is used to indicate the required address
     * width (which the decoder can't otherwise determine without parsing
     * the compressed data).
     */
    *tmpPtr++ = addr | (addrWidth > 1 ? 0x80 : 0x00);
    memcpy(tmpPtr, mapData, regWidth);

    prevBits = mapData;
    prevAddr = addr;

    tmpPtr += regWidth;
    mapData += regWidth;

    /*
     * Loop over all following entries.
     */
    int entry;
    for (entry = 1; entry < numEntries; entry++) {
        int addrDiff;
        u1 key;

        /*
         * Pull out the address and figure out how to encode it.
         */
        addr = *mapData++;
        if (addrWidth > 1)
            addr |= (*mapData++) << 8;

        if (debug)
            LOGI(" addr=0x%04x ent=%d (aw=%d)\n", addr, entry, addrWidth);

        addrDiff = addr - prevAddr;
        assert(addrDiff > 0);
        if (addrDiff < 8) {
            /* small difference, encode in 3 bits */
            key = addrDiff -1;          /* set 00000AAA */
            if (debug)
                LOGI(" : small %d, key=0x%02x\n", addrDiff, key);
        } else {
            /* large difference, output escape code */
            key = 0x07;                 /* escape code for AAA */
            if (debug)
                LOGI(" : large %d, key=0x%02x\n", addrDiff, key);
        }

        int numBitsChanged, firstBitChanged, lebSize;

        lebSize = computeBitDiff(prevBits, mapData, regWidth,
            &firstBitChanged, &numBitsChanged, NULL);

        if (debug) {
            LOGI(" : diff fbc=%d nbc=%d ls=%d (rw=%d)\n",
                firstBitChanged, numBitsChanged, lebSize, regWidth);
        }

        if (numBitsChanged == 0) {
            /* set B to 1 and CCCC to zero to indicate no bits were changed */
            key |= 0x08;
            if (debug) LOGI(" : no bits changed\n");
        } else if (numBitsChanged == 1 && firstBitChanged < 16) {
            /* set B to 0 and CCCC to the index of the changed bit */
            key |= firstBitChanged << 4;
            if (debug) LOGI(" : 1 low bit changed\n");
        } else if (numBitsChanged < 15 && lebSize < regWidth) {
            /* set B to 1 and CCCC to the number of bits */
            key |= 0x08 | (numBitsChanged << 4);
            if (debug) LOGI(" : some bits changed\n");
        } else {
            /* set B to 1 and CCCC to 0x0f so we store the entire vector */
            key |= 0x08 | 0xf0;
            if (debug) LOGI(" : encode original\n");
        }

        /*
         * Encode output.  Start with the key, follow with the address
         * diff (if it didn't fit in 3 bits), then the changed bit info.
         */
        *tmpPtr++ = key;
        if ((key & 0x07) == 0x07)
            tmpPtr = writeUnsignedLeb128(tmpPtr, addrDiff);

        if ((key & 0x08) != 0) {
            int bitCount = key >> 4;
            if (bitCount == 0) {
                /* nothing changed, no additional output required */
            } else if (bitCount == 15) {
                /* full vector is most compact representation */
                memcpy(tmpPtr, mapData, regWidth);
                tmpPtr += regWidth;
            } else {
                /* write bit indices in LEB128 format */
                (void) computeBitDiff(prevBits, mapData, regWidth,
                    NULL, NULL, tmpPtr);
                tmpPtr += lebSize;
            }
        } else {
            /* single-bit changed, value encoded in key byte */
        }

        prevBits = mapData;
        prevAddr = addr;
        mapData += regWidth;

        /*
         * See if we've run past the original size.
         */
        if (tmpPtr - tmpBuf >= origSize) {
            if (debug) {
                LOGD("Compressed size >= original (%d vs %d): %s.%s\n",
                    tmpPtr - tmpBuf, origSize,
                    meth->clazz->descriptor, meth->name);
            }
            goto bail;
        }
    }

    /*
     * Create a RegisterMap with the contents.
     *
     * TODO: consider using a threshold other than merely ">=".  We would
     * get poorer compression but potentially use less native heap space.
     */
    static const int kHeaderSize = offsetof(RegisterMap, data);
    int newDataSize = tmpPtr - tmpBuf;
    int newMapSize;

    newMapSize = kHeaderSize + unsignedLeb128Size(newDataSize) + newDataSize;
    if (newMapSize >= origSize) {
        if (debug) {
            LOGD("Final comp size >= original (%d vs %d): %s.%s\n",
                newMapSize, origSize, meth->clazz->descriptor, meth->name);
        }
        goto bail;
    }

    pNewMap = (RegisterMap*) malloc(newMapSize);
    if (pNewMap == NULL)
        goto bail;
    dvmRegisterMapSetFormat(pNewMap, kRegMapFormatDifferential);
    dvmRegisterMapSetOnHeap(pNewMap, true);
    dvmRegisterMapSetRegWidth(pNewMap, regWidth);
    dvmRegisterMapSetNumEntries(pNewMap, numEntries);

    tmpPtr = pNewMap->data;
    tmpPtr = writeUnsignedLeb128(tmpPtr, newDataSize);
    memcpy(tmpPtr, tmpBuf, newDataSize);

    if (REGISTER_MAP_VERBOSE) {
        LOGD("Compression successful (%d -> %d) from aw=%d rw=%d ne=%d\n",
            computeRegisterMapSize(pMap), computeRegisterMapSize(pNewMap),
            addrWidth, regWidth, numEntries);
    }

bail:
    free(tmpBuf);
    return pNewMap;
}

/*
 * Toggle the value of the "idx"th bit in "ptr".
 */
static inline void toggleBit(u1* ptr, int idx)
{
    ptr[idx >> 3] ^= 1 << (idx & 0x07);
}

/*
 * Expand a compressed map to an uncompressed form.
 *
 * Returns a newly-allocated RegisterMap on success, or NULL on failure.
 *
 * TODO: consider using the linear allocator or a custom allocator with
 * LRU replacement for these instead of the native heap.
 */
static RegisterMap* uncompressMapDifferential(const RegisterMap* pMap)
{
    RegisterMap* pNewMap = NULL;
    static const int kHeaderSize = offsetof(RegisterMap, data);
    u1 format = dvmRegisterMapGetFormat(pMap);
    RegisterMapFormat newFormat;
    int regWidth, numEntries, newAddrWidth, newMapSize;

    if (format != kRegMapFormatDifferential) {
        LOGE("Not differential (%d)\n", format);
        goto bail;
    }

    regWidth = dvmRegisterMapGetRegWidth(pMap);
    numEntries = dvmRegisterMapGetNumEntries(pMap);

    /* get the data size; we can check this at the end */
    const u1* srcPtr = pMap->data;
    int expectedSrcLen = readUnsignedLeb128(&srcPtr);
    const u1* srcStart = srcPtr;

    /* get the initial address and the 16-bit address flag */
    int addr = *srcPtr & 0x7f;
    if ((*srcPtr & 0x80) == 0) {
        newFormat = kRegMapFormatCompact8;
        newAddrWidth = 1;
    } else {
        newFormat = kRegMapFormatCompact16;
        newAddrWidth = 2;
    }
    srcPtr++;

    /* now we know enough to allocate the new map */
    if (REGISTER_MAP_VERBOSE) {
        LOGI("Expanding to map aw=%d rw=%d ne=%d\n",
            newAddrWidth, regWidth, numEntries);
    }
    newMapSize = kHeaderSize + (newAddrWidth + regWidth) * numEntries;
    pNewMap = (RegisterMap*) malloc(newMapSize);
    if (pNewMap == NULL)
        goto bail;

    dvmRegisterMapSetFormat(pNewMap, newFormat);
    dvmRegisterMapSetOnHeap(pNewMap, true);
    dvmRegisterMapSetRegWidth(pNewMap, regWidth);
    dvmRegisterMapSetNumEntries(pNewMap, numEntries);

    /*
     * Write the start address and initial bits to the new map.
     */
    u1* dstPtr = pNewMap->data;

    *dstPtr++ = addr & 0xff;
    if (newAddrWidth > 1)
        *dstPtr++ = (u1) (addr >> 8);

    memcpy(dstPtr, srcPtr, regWidth);

    int prevAddr = addr;
    const u1* prevBits = dstPtr;    /* point at uncompressed data */

    dstPtr += regWidth;
    srcPtr += regWidth;

    /*
     * Walk through, uncompressing one line at a time.
     */
    int entry;
    for (entry = 1; entry < numEntries; entry++) {
        int addrDiff;
        u1 key;

        key = *srcPtr++;

        /* get the address */
        if ((key & 0x07) == 7) {
            /* address diff follows in ULEB128 */
            addrDiff = readUnsignedLeb128(&srcPtr);
        } else {
            addrDiff = (key & 0x07) +1;
        }

        addr = prevAddr + addrDiff;
        *dstPtr++ = addr & 0xff;
        if (newAddrWidth > 1)
            *dstPtr++ = (u1) (addr >> 8);

        /* unpack the bits */
        if ((key & 0x08) != 0) {
            int bitCount = (key >> 4);
            if (bitCount == 0) {
                /* no bits changed, just copy previous */
                memcpy(dstPtr, prevBits, regWidth);
            } else if (bitCount == 15) {
                /* full copy of bit vector is present; ignore prevBits */
                memcpy(dstPtr, srcPtr, regWidth);
                srcPtr += regWidth;
            } else {
                /* copy previous bits and modify listed indices */
                memcpy(dstPtr, prevBits, regWidth);
                while (bitCount--) {
                    int bitIndex = readUnsignedLeb128(&srcPtr);
                    toggleBit(dstPtr, bitIndex);
                }
            }
        } else {
            /* copy previous bits and modify the specified one */
            memcpy(dstPtr, prevBits, regWidth);

            /* one bit, from 0-15 inclusive, was changed */
            toggleBit(dstPtr, key >> 4);
        }

        prevAddr = addr;
        prevBits = dstPtr;
        dstPtr += regWidth;
    }

    if (dstPtr - (u1*) pNewMap != newMapSize) {
        LOGE("ERROR: output %d bytes, expected %d\n",
            dstPtr - (u1*) pNewMap, newMapSize);
        goto bail;
    }

    if (srcPtr - srcStart != expectedSrcLen) {
        LOGE("ERROR: consumed %d bytes, expected %d\n",
            srcPtr - srcStart, expectedSrcLen);
        goto bail;
    }

    if (REGISTER_MAP_VERBOSE) {
        LOGD("Expansion successful (%d -> %d)\n",
            computeRegisterMapSize(pMap), computeRegisterMapSize(pNewMap));
    }

    return pNewMap;

bail:
    free(pNewMap);
    return NULL;
}


/*
 * ===========================================================================
 *      Just-in-time generation
 * ===========================================================================
 */

#if 0   /* incomplete implementation; may be removed entirely in the future */

/*
Notes on just-in-time RegisterMap generation

Generating RegisterMap tables as part of verification is convenient because
we generate most of what we need to know as part of doing the verify.
The negative aspect of doing it this way is that we must store the
result in the DEX file (if we're verifying ahead of time) or in memory
(if verifying during class load) for every concrete non-native method,
even if we never actually need the map during a GC.

A simple but compact encoding of register map data increases the size of
optimized DEX files by about 25%, so size considerations are important.

We can instead generate the RegisterMap at the point where it is needed.
In a typical application we only need to convert about 2% of the loaded
methods, and we can generate type-precise roots reasonably quickly because
(a) we know the method has already been verified and hence can make a
lot of assumptions, and (b) we don't care what type of object a register
holds, just whether or not it holds a reference, and hence can skip a
lot of class resolution gymnastics.

There are a couple of problems with this approach however.  First, to
get good performance we really want an implementation that is largely
independent from the verifier, which means some duplication of effort.
Second, we're dealing with post-dexopt code, which contains "quickened"
instructions.  We can't process those without either tracking type
information (which slows us down) or storing additional data in the DEX
file that allows us to reconstruct the original instructions (adds ~5%
to the size of the ODEX).


Implementation notes...

Both type-precise and live-precise information can be generated knowing
only whether or not a register holds a reference.  We don't need to
know what kind of reference or whether the object has been initialized.
Not only can we skip many of the fancy steps in the verifier, we can
initialize from simpler sources, e.g. the initial registers and return
type are set from the "shorty" signature rather than the full signature.

The short-term storage needs for just-in-time register map generation can
be much lower because we can use a 1-byte SRegType instead of a 4-byte
RegType.  On the other hand, if we're not doing type-precise analysis
in the verifier we only need to store register contents at every branch
target, rather than every GC point (which are much more frequent).

Whether it happens in the verifier or independently, because this is done
with native heap allocations that may be difficult to return to the system,
an effort should be made to minimize memory use.
*/

/*
 * This is like RegType in the verifier, but simplified.  It holds a value
 * from the reg type enum, or kRegTypeReference.
 */
typedef u1 SRegType;
#define kRegTypeReference kRegTypeMAX

/*
 * We need an extra "pseudo register" to hold the return type briefly.  It
 * can be category 1 or 2, so we need two slots.
 */
#define kExtraRegs  2
#define RESULT_REGISTER(_insnRegCountPlus)  (_insnRegCountPlus - kExtraRegs)

/*
 * Working state.
 */
typedef struct WorkState {
    /*
     * The method we're working on.
     */
    const Method* method;

    /*
     * Number of instructions in the method.
     */
    int         insnsSize;

    /*
     * Number of registers we track for each instruction.  This is equal
     * to the method's declared "registersSize" plus kExtraRegs.
     */
    int         insnRegCountPlus;

    /*
     * Instruction widths and flags, one entry per code unit.
     */
    InsnFlags*  insnFlags;

    /*
     * Array of SRegType arrays, one entry per code unit.  We only need
     * to create an entry when an instruction starts at this address.
     * We can further reduce this to instructions that are GC points.
     *
     * We could just go ahead and allocate one per code unit, but for
     * larger methods that can represent a significant bit of short-term
     * storage.
     */
    SRegType**  addrRegs;

    /*
     * A single large alloc, with all of the storage needed for addrRegs.
     */
    SRegType*   regAlloc;
} WorkState;

// fwd
static bool generateMap(WorkState* pState, RegisterMap* pMap);
static bool analyzeMethod(WorkState* pState);
static bool handleInstruction(WorkState* pState, SRegType* workRegs,\
    int insnIdx, int* pStartGuess);
static void updateRegisters(WorkState* pState, int nextInsn,\
    const SRegType* workRegs);


/*
 * Set instruction flags.
 */
static bool setInsnFlags(WorkState* pState, int* pGcPointCount)
{
    const Method* meth = pState->method;
    InsnFlags* insnFlags = pState->insnFlags;
    int insnsSize = pState->insnsSize;
    const u2* insns = meth->insns;
    int gcPointCount = 0;
    int offset;

    /* set the widths */
    if (!dvmComputeCodeWidths(meth, pState->insnFlags, NULL))
        return false;

    /* mark "try" regions and exception handler branch targets */
    if (!dvmSetTryFlags(meth, pState->insnFlags))
        return false;

    /* the start of the method is a "branch target" */
    dvmInsnSetBranchTarget(insnFlags, 0, true);

    /*
     * Run through the instructions, looking for switches and branches.
     * Mark their targets.
     *
     * We don't really need to "check" these instructions -- the verifier
     * already did that -- but the additional overhead isn't significant
     * enough to warrant making a second copy of the "Check" function.
     *
     * Mark and count GC points while we're at it.
     */
    for (offset = 0; offset < insnsSize; offset++) {
        static int gcMask = kInstrCanBranch | kInstrCanSwitch |
            kInstrCanThrow | kInstrCanReturn;
        u1 opcode = insns[offset] & 0xff;
        InstructionFlags opFlags = dexGetInstrFlags(gDvm.instrFlags, opcode);

        if (opFlags & kInstrCanBranch) {
            if (!dvmCheckBranchTarget(meth, insnFlags, offset, true))
                return false;
        }
        if (opFlags & kInstrCanSwitch) {
            if (!dvmCheckSwitchTargets(meth, insnFlags, offset))
                return false;
        }

        if ((opFlags & gcMask) != 0) {
            dvmInsnSetGcPoint(pState->insnFlags, offset, true);
            gcPointCount++;
        }
    }

    *pGcPointCount = gcPointCount;
    return true;
}

/*
 * Generate the register map for a method.
 *
 * Returns a pointer to newly-allocated storage.
 */
RegisterMap* dvmGenerateRegisterMap(const Method* meth)
{
    WorkState* pState = NULL;
    RegisterMap* pMap = NULL;
    RegisterMap* result = NULL;
    SRegType* regPtr;

    pState = (WorkState*) calloc(1, sizeof(WorkState));
    if (pState == NULL)
        goto bail;

    pMap = (RegisterMap*) calloc(1, sizeof(RegisterMap));
    if (pMap == NULL)
        goto bail;

    pState->method = meth;
    pState->insnsSize = dvmGetMethodInsnsSize(meth);
    pState->insnRegCountPlus = meth->registersSize + kExtraRegs;

    pState->insnFlags = calloc(sizeof(InsnFlags), pState->insnsSize);
    pState->addrRegs = calloc(sizeof(SRegType*), pState->insnsSize);

    /*
     * Set flags on instructions, and calculate the number of code units
     * that happen to be GC points.
     */
    int gcPointCount;
    if (!setInsnFlags(pState, &gcPointCount))
        goto bail;

    if (gcPointCount == 0) {
        /* the method doesn't allocate or call, and never returns? unlikely */
        LOG_VFY_METH(meth, "Found do-nothing method\n");
        goto bail;
    }

    pState->regAlloc = (SRegType*)
        calloc(sizeof(SRegType), pState->insnsSize * gcPointCount);
    regPtr = pState->regAlloc;

    /*
     * For each instruction that is a GC point, set a pointer into the
     * regAlloc buffer.
     */
    int offset;
    for (offset = 0; offset < pState->insnsSize; offset++) {
        if (dvmInsnIsGcPoint(pState->insnFlags, offset)) {
            pState->addrRegs[offset] = regPtr;
            regPtr += pState->insnRegCountPlus;
        }
    }
    assert(regPtr - pState->regAlloc == pState->insnsSize * gcPointCount);
    assert(pState->addrRegs[0] != NULL);

    /*
     * Compute the register map.
     */
    if (!generateMap(pState, pMap))
        goto bail;

    /* success */
    result = pMap;
    pMap = NULL;

bail:
    if (pState != NULL) {
        free(pState->insnFlags);
        free(pState->addrRegs);
        free(pState->regAlloc);
        free(pState);
    }
    if (pMap != NULL)
        dvmFreeRegisterMap(pMap);
    return result;
}

/*
 * Release the storage associated with a RegisterMap.
 */
void dvmFreeRegisterMap(RegisterMap* pMap)
{
    if (pMap == NULL)
        return;
}


/*
 * Create the RegisterMap using the provided state.
 */
static bool generateMap(WorkState* pState, RegisterMap* pMap)
{
    bool result = false;

    /*
     * Analyze the method and store the results in WorkState.
     */
    if (!analyzeMethod(pState))
        goto bail;

    /*
     * Convert the analyzed data into a RegisterMap.
     */
    // TODO

    result = true;

bail:
    return result;
}

/*
 * Set the register types for the method arguments.  We can pull the values
 * out of the "shorty" signature.
 */
static bool setTypesFromSignature(WorkState* pState)
{
    const Method* meth = pState->method;
    int argReg = meth->registersSize - meth->insSize;   /* first arg */
    SRegType* pRegs = pState->addrRegs[0];
    SRegType* pCurReg = &pRegs[argReg];
    const char* ccp;

    /*
     * Include "this" pointer, if appropriate.
     */
    if (!dvmIsStaticMethod(meth)) {
        *pCurReg++ = kRegTypeReference;
    }

    ccp = meth->shorty +1;      /* skip first byte, which holds return type */
    while (*ccp != 0) {
        switch (*ccp) {
        case 'L':
        //case '[':
            *pCurReg++ = kRegTypeReference;
            break;
        case 'Z':
            *pCurReg++ = kRegTypeBoolean;
            break;
        case 'C':
            *pCurReg++ = kRegTypeChar;
            break;
        case 'B':
            *pCurReg++ = kRegTypeByte;
            break;
        case 'I':
            *pCurReg++ = kRegTypeInteger;
            break;
        case 'S':
            *pCurReg++ = kRegTypeShort;
            break;
        case 'F':
            *pCurReg++ = kRegTypeFloat;
            break;
        case 'D':
            *pCurReg++ = kRegTypeDoubleLo;
            *pCurReg++ = kRegTypeDoubleHi;
            break;
        case 'J':
            *pCurReg++ = kRegTypeLongLo;
            *pCurReg++ = kRegTypeLongHi;
            break;
        default:
            assert(false);
            return false;
        }
    }

    assert(pCurReg - pRegs == meth->insSize);
    return true;
}

/*
 * Find the start of the register set for the specified instruction in
 * the current method.
 */
static inline SRegType* getRegisterLine(const WorkState* pState, int insnIdx)
{
    return pState->addrRegs[insnIdx];
}

/*
 * Copy a set of registers.
 */
static inline void copyRegisters(SRegType* dst, const SRegType* src,
    int numRegs)
{
    memcpy(dst, src, numRegs * sizeof(SRegType));
}

/*
 * Compare a set of registers.  Returns 0 if they match.
 */
static inline int compareRegisters(const SRegType* src1, const SRegType* src2,
    int numRegs)
{
    return memcmp(src1, src2, numRegs * sizeof(SRegType));
}

/*
 * Run through the instructions repeatedly until we have exercised all
 * possible paths.
 */
static bool analyzeMethod(WorkState* pState)
{
    const Method* meth = pState->method;
    SRegType workRegs[pState->insnRegCountPlus];
    InsnFlags* insnFlags = pState->insnFlags;
    int insnsSize = pState->insnsSize;
    int insnIdx, startGuess;
    bool result = false;

    /*
     * Initialize the types of the registers that correspond to method
     * arguments.
     */
    if (!setTypesFromSignature(pState))
        goto bail;

    /*
     * Mark the first instruction as "changed".
     */
    dvmInsnSetChanged(insnFlags, 0, true);
    startGuess = 0;

    if (true) {
        IF_LOGI() {
            char* desc = dexProtoCopyMethodDescriptor(&meth->prototype);
            LOGI("Now mapping: %s.%s %s (ins=%d regs=%d)\n",
                meth->clazz->descriptor, meth->name, desc,
                meth->insSize, meth->registersSize);
            LOGI(" ------ [0    4    8    12   16   20   24   28   32   36\n");
            free(desc);
        }
    }

    /*
     * Continue until no instructions are marked "changed".
     */
    while (true) {
        /*
         * Find the first marked one.  Use "startGuess" as a way to find
         * one quickly.
         */
        for (insnIdx = startGuess; insnIdx < insnsSize; insnIdx++) {
            if (dvmInsnIsChanged(insnFlags, insnIdx))
                break;
        }

        if (insnIdx == insnsSize) {
            if (startGuess != 0) {
                /* try again, starting from the top */
                startGuess = 0;
                continue;
            } else {
                /* all flags are clear */
                break;
            }
        }

        /*
         * We carry the working set of registers from instruction to
         * instruction.  If this address can be the target of a branch
         * (or throw) instruction, or if we're skipping around chasing
         * "changed" flags, we need to load the set of registers from
         * the table.
         *
         * Because we always prefer to continue on to the next instruction,
         * we should never have a situation where we have a stray
         * "changed" flag set on an instruction that isn't a branch target.
         */
        if (dvmInsnIsBranchTarget(insnFlags, insnIdx)) {
            SRegType* insnRegs = getRegisterLine(pState, insnIdx);
            assert(insnRegs != NULL);
            copyRegisters(workRegs, insnRegs, pState->insnRegCountPlus);

        } else {
#ifndef NDEBUG
            /*
             * Sanity check: retrieve the stored register line (assuming
             * a full table) and make sure it actually matches.
             */
            SRegType* insnRegs = getRegisterLine(pState, insnIdx);
            if (insnRegs != NULL &&
                compareRegisters(workRegs, insnRegs,
                                 pState->insnRegCountPlus) != 0)
            {
                char* desc = dexProtoCopyMethodDescriptor(&meth->prototype);
                LOG_VFY("HUH? workRegs diverged in %s.%s %s\n",
                        meth->clazz->descriptor, meth->name, desc);
                free(desc);
            }
#endif
        }

        /*
         * Update the register sets altered by this instruction.
         */
        if (!handleInstruction(pState, workRegs, insnIdx, &startGuess)) {
            goto bail;
        }

        dvmInsnSetVisited(insnFlags, insnIdx, true);
        dvmInsnSetChanged(insnFlags, insnIdx, false);
    }

    // TODO - add dead code scan to help validate this code?

    result = true;

bail:
    return result;
}

/*
 * Get a pointer to the method being invoked.
 *
 * Returns NULL on failure.
 */
static Method* getInvokedMethod(const Method* meth,
    const DecodedInstruction* pDecInsn, MethodType methodType)
{
    Method* resMethod;
    char* sigOriginal = NULL;

    /*
     * Resolve the method.  This could be an abstract or concrete method
     * depending on what sort of call we're making.
     */
    if (methodType == METHOD_INTERFACE) {
        resMethod = dvmOptResolveInterfaceMethod(meth->clazz, pDecInsn->vB);
    } else {
        resMethod = dvmOptResolveMethod(meth->clazz, pDecInsn->vB, methodType);
    }
    if (resMethod == NULL) {
        /* failed; print a meaningful failure message */
        DexFile* pDexFile = meth->clazz->pDvmDex->pDexFile;
        const DexMethodId* pMethodId;
        const char* methodName;
        char* methodDesc;
        const char* classDescriptor;

        pMethodId = dexGetMethodId(pDexFile, pDecInsn->vB);
        methodName = dexStringById(pDexFile, pMethodId->nameIdx);
        methodDesc = dexCopyDescriptorFromMethodId(pDexFile, pMethodId);
        classDescriptor = dexStringByTypeIdx(pDexFile, pMethodId->classIdx);

        LOG_VFY("VFY: unable to resolve %s method %u: %s.%s %s\n",
            dvmMethodTypeStr(methodType), pDecInsn->vB,
            classDescriptor, methodName, methodDesc);
        free(methodDesc);
        return NULL;
    }

    return resMethod;
}

/*
 * Return the register type for the method.  Since we don't care about
 * the actual type, we can just look at the "shorty" signature.
 *
 * Returns kRegTypeUnknown for "void".
 */
static SRegType getMethodReturnType(const Method* meth)
{
    SRegType type;

    switch (meth->shorty[0]) {
    case 'I':
        type = kRegTypeInteger;
        break;
    case 'C':
        type = kRegTypeChar;
        break;
    case 'S':
        type = kRegTypeShort;
        break;
    case 'B':
        type = kRegTypeByte;
        break;
    case 'Z':
        type = kRegTypeBoolean;
        break;
    case 'V':
        type = kRegTypeUnknown;
        break;
    case 'F':
        type = kRegTypeFloat;
        break;
    case 'D':
        type = kRegTypeDoubleLo;
        break;
    case 'J':
        type = kRegTypeLongLo;
        break;
    case 'L':
    //case '[':
        type = kRegTypeReference;
        break;
    default:
        /* we verified signature return type earlier, so this is impossible */
        assert(false);
        type = kRegTypeConflict;
        break;
    }

    return type;
}

/*
 * Copy a category 1 register.
 */
static inline void copyRegister1(SRegType* insnRegs, u4 vdst, u4 vsrc)
{
    insnRegs[vdst] = insnRegs[vsrc];
}

/*
 * Copy a category 2 register.  Note the source and destination may overlap.
 */
static inline void copyRegister2(SRegType* insnRegs, u4 vdst, u4 vsrc)
{
    //memmove(&insnRegs[vdst], &insnRegs[vsrc], sizeof(SRegType) * 2);
    SRegType r1 = insnRegs[vsrc];
    SRegType r2 = insnRegs[vsrc+1];
    insnRegs[vdst] = r1;
    insnRegs[vdst+1] = r2;
}

/*
 * Set the type of a category 1 register.
 */
static inline void setRegisterType(SRegType* insnRegs, u4 vdst, SRegType type)
{
    insnRegs[vdst] = type;
}

/*
 * Decode the specified instruction and update the register info.
 */
static bool handleInstruction(WorkState* pState, SRegType* workRegs,
    int insnIdx, int* pStartGuess)
{
    const Method* meth = pState->method;
    const u2* insns = meth->insns + insnIdx;
    InsnFlags* insnFlags = pState->insnFlags;
    bool result = false;

    /*
     * Once we finish decoding the instruction, we need to figure out where
     * we can go from here.  There are three possible ways to transfer
     * control to another statement:
     *
     * (1) Continue to the next instruction.  Applies to all but
     *     unconditional branches, method returns, and exception throws.
     * (2) Branch to one or more possible locations.  Applies to branches
     *     and switch statements.
     * (3) Exception handlers.  Applies to any instruction that can
     *     throw an exception that is handled by an encompassing "try"
     *     block.  (We simplify this to be any instruction that can
     *     throw any exception.)
     *
     * We can also return, in which case there is no successor instruction
     * from this point.
     *
     * The behavior can be determined from the InstrFlags.
     */
    DecodedInstruction decInsn;
    SRegType entryRegs[pState->insnRegCountPlus];
    const int insnRegCountPlus = pState->insnRegCountPlus;
    bool justSetResult = false;
    int branchTarget = 0;
    SRegType tmpType;

    dexDecodeInstruction(gDvm.instrFormat, insns, &decInsn);
    const int nextFlags = dexGetInstrFlags(gDvm.instrFlags, decInsn.opCode);

    /*
     * Make a copy of the previous register state.  If the instruction
     * throws an exception, we merge *this* into the destination rather
     * than workRegs, because we don't want the result from the "successful"
     * code path (e.g. a check-cast that "improves" a type) to be visible
     * to the exception handler.
     */
    if ((nextFlags & kInstrCanThrow) != 0 && dvmInsnIsInTry(insnFlags, insnIdx))
    {
        copyRegisters(entryRegs, workRegs, insnRegCountPlus);
    }

    switch (decInsn.opCode) {
    case OP_NOP:
        break;

    case OP_MOVE:
    case OP_MOVE_FROM16:
    case OP_MOVE_16:
    case OP_MOVE_OBJECT:
    case OP_MOVE_OBJECT_FROM16:
    case OP_MOVE_OBJECT_16:
        copyRegister1(workRegs, decInsn.vA, decInsn.vB);
        break;
    case OP_MOVE_WIDE:
    case OP_MOVE_WIDE_FROM16:
    case OP_MOVE_WIDE_16:
        copyRegister2(workRegs, decInsn.vA, decInsn.vB);
        break;

    /*
     * The move-result instructions copy data out of a "pseudo-register"
     * with the results from the last method invocation.  In practice we
     * might want to hold the result in an actual CPU register, so the
     * Dalvik spec requires that these only appear immediately after an
     * invoke or filled-new-array.
     *
     * These calls invalidate the "result" register.  (This is now
     * redundant with the reset done below, but it can make the debug info
     * easier to read in some cases.)
     */
    case OP_MOVE_RESULT:
    case OP_MOVE_RESULT_OBJECT:
        copyRegister1(workRegs, decInsn.vA, RESULT_REGISTER(insnRegCountPlus));
        break;
    case OP_MOVE_RESULT_WIDE:
        copyRegister2(workRegs, decInsn.vA, RESULT_REGISTER(insnRegCountPlus));
        break;

    case OP_MOVE_EXCEPTION:
        /*
         * This statement can only appear as the first instruction in an
         * exception handler (though not all exception handlers need to
         * have one of these).  We verify that as part of extracting the
         * exception type from the catch block list.
         */
        setRegisterType(workRegs, decInsn.vA, kRegTypeReference);
        break;

    case OP_RETURN_VOID:
    case OP_RETURN:
    case OP_RETURN_WIDE:
    case OP_RETURN_OBJECT:
        break;

    case OP_CONST_4:
    case OP_CONST_16:
    case OP_CONST:
        /* could be boolean, int, float, or a null reference */
        setRegisterType(workRegs, decInsn.vA,
            dvmDetermineCat1Const((s4)decInsn.vB));
        break;
    case OP_CONST_HIGH16:
        /* could be boolean, int, float, or a null reference */
        setRegisterType(workRegs, decInsn.vA,
            dvmDetermineCat1Const((s4) decInsn.vB << 16));
        break;
    case OP_CONST_WIDE_16:
    case OP_CONST_WIDE_32:
    case OP_CONST_WIDE:
    case OP_CONST_WIDE_HIGH16:
        /* could be long or double; default to long and allow conversion */
        setRegisterType(workRegs, decInsn.vA, kRegTypeLongLo);
        break;
    case OP_CONST_STRING:
    case OP_CONST_STRING_JUMBO:
    case OP_CONST_CLASS:
        setRegisterType(workRegs, decInsn.vA, kRegTypeReference);
        break;

    case OP_MONITOR_ENTER:
    case OP_MONITOR_EXIT:
        break;

    case OP_CHECK_CAST:
        setRegisterType(workRegs, decInsn.vA, kRegTypeReference);
        break;
    case OP_INSTANCE_OF:
        /* result is boolean */
        setRegisterType(workRegs, decInsn.vA, kRegTypeBoolean);
        break;

    case OP_ARRAY_LENGTH:
        setRegisterType(workRegs, decInsn.vA, kRegTypeInteger);
        break;

    case OP_NEW_INSTANCE:
    case OP_NEW_ARRAY:
        /* add the new uninitialized reference to the register ste */
        setRegisterType(workRegs, decInsn.vA, kRegTypeReference);
        break;
    case OP_FILLED_NEW_ARRAY:
    case OP_FILLED_NEW_ARRAY_RANGE:
        setRegisterType(workRegs, RESULT_REGISTER(insnRegCountPlus),
            kRegTypeReference);
        justSetResult = true;
        break;

    case OP_CMPL_FLOAT:
    case OP_CMPG_FLOAT:
        setRegisterType(workRegs, decInsn.vA, kRegTypeBoolean);
        break;
    case OP_CMPL_DOUBLE:
    case OP_CMPG_DOUBLE:
        setRegisterType(workRegs, decInsn.vA, kRegTypeBoolean);
        break;
    case OP_CMP_LONG:
        setRegisterType(workRegs, decInsn.vA, kRegTypeBoolean);
        break;

    case OP_THROW:
    case OP_GOTO:
    case OP_GOTO_16:
    case OP_GOTO_32:
    case OP_PACKED_SWITCH:
    case OP_SPARSE_SWITCH:
        break;

    case OP_FILL_ARRAY_DATA:
        break;

    case OP_IF_EQ:
    case OP_IF_NE:
    case OP_IF_LT:
    case OP_IF_GE:
    case OP_IF_GT:
    case OP_IF_LE:
    case OP_IF_EQZ:
    case OP_IF_NEZ:
    case OP_IF_LTZ:
    case OP_IF_GEZ:
    case OP_IF_GTZ:
    case OP_IF_LEZ:
        break;

    case OP_AGET:
        tmpType = kRegTypeInteger;
        goto aget_1nr_common;
    case OP_AGET_BOOLEAN:
        tmpType = kRegTypeBoolean;
        goto aget_1nr_common;
    case OP_AGET_BYTE:
        tmpType = kRegTypeByte;
        goto aget_1nr_common;
    case OP_AGET_CHAR:
        tmpType = kRegTypeChar;
        goto aget_1nr_common;
    case OP_AGET_SHORT:
        tmpType = kRegTypeShort;
        goto aget_1nr_common;
aget_1nr_common:
        setRegisterType(workRegs, decInsn.vA, tmpType);
        break;

    case OP_AGET_WIDE:
        /*
         * We know this is either long or double, and we don't really
         * discriminate between those during verification, so we
         * call it a long.
         */
        setRegisterType(workRegs, decInsn.vA, kRegTypeLongLo);
        break;

    case OP_AGET_OBJECT:
        setRegisterType(workRegs, decInsn.vA, kRegTypeReference);
        break;

    case OP_APUT:
    case OP_APUT_BOOLEAN:
    case OP_APUT_BYTE:
    case OP_APUT_CHAR:
    case OP_APUT_SHORT:
    case OP_APUT_WIDE:
    case OP_APUT_OBJECT:
        break;

    case OP_IGET:
        tmpType = kRegTypeInteger;
        goto iget_1nr_common;
    case OP_IGET_BOOLEAN:
        tmpType = kRegTypeBoolean;
        goto iget_1nr_common;
    case OP_IGET_BYTE:
        tmpType = kRegTypeByte;
        goto iget_1nr_common;
    case OP_IGET_CHAR:
        tmpType = kRegTypeChar;
        goto iget_1nr_common;
    case OP_IGET_SHORT:
        tmpType = kRegTypeShort;
        goto iget_1nr_common;
iget_1nr_common:
        setRegisterType(workRegs, decInsn.vA, tmpType);
        break;

    case OP_IGET_WIDE:
        setRegisterType(workRegs, decInsn.vA, kRegTypeLongLo);
        break;

    case OP_IGET_OBJECT:
        setRegisterType(workRegs, decInsn.vA, kRegTypeReference);
        break;

    case OP_IPUT:
    case OP_IPUT_BOOLEAN:
    case OP_IPUT_BYTE:
    case OP_IPUT_CHAR:
    case OP_IPUT_SHORT:
    case OP_IPUT_WIDE:
    case OP_IPUT_OBJECT:
        break;

    case OP_SGET:
        tmpType = kRegTypeInteger;
        goto sget_1nr_common;
    case OP_SGET_BOOLEAN:
        tmpType = kRegTypeBoolean;
        goto sget_1nr_common;
    case OP_SGET_BYTE:
        tmpType = kRegTypeByte;
        goto sget_1nr_common;
    case OP_SGET_CHAR:
        tmpType = kRegTypeChar;
        goto sget_1nr_common;
    case OP_SGET_SHORT:
        tmpType = kRegTypeShort;
        goto sget_1nr_common;
sget_1nr_common:
        setRegisterType(workRegs, decInsn.vA, tmpType);
        break;

    case OP_SGET_WIDE:
        setRegisterType(workRegs, decInsn.vA, kRegTypeLongLo);
        break;

    case OP_SGET_OBJECT:
        setRegisterType(workRegs, decInsn.vA, kRegTypeReference);
        break;

    case OP_SPUT:
    case OP_SPUT_BOOLEAN:
    case OP_SPUT_BYTE:
    case OP_SPUT_CHAR:
    case OP_SPUT_SHORT:
    case OP_SPUT_WIDE:
    case OP_SPUT_OBJECT:
        break;

    case OP_INVOKE_VIRTUAL:
    case OP_INVOKE_VIRTUAL_RANGE:
    case OP_INVOKE_SUPER:
    case OP_INVOKE_SUPER_RANGE:
        {
            Method* calledMethod;

            calledMethod = getInvokedMethod(meth, &decInsn, METHOD_VIRTUAL);
            if (calledMethod == NULL)
                goto bail;
            setRegisterType(workRegs, RESULT_REGISTER(insnRegCountPlus),
                getMethodReturnType(calledMethod));
            justSetResult = true;
        }
        break;
    case OP_INVOKE_DIRECT:
    case OP_INVOKE_DIRECT_RANGE:
        {
            Method* calledMethod;

            calledMethod = getInvokedMethod(meth, &decInsn, METHOD_DIRECT);
            if (calledMethod == NULL)
                goto bail;
            setRegisterType(workRegs, RESULT_REGISTER(insnRegCountPlus),
                getMethodReturnType(calledMethod));
            justSetResult = true;
        }
        break;
    case OP_INVOKE_STATIC:
    case OP_INVOKE_STATIC_RANGE:
        {
            Method* calledMethod;

            calledMethod = getInvokedMethod(meth, &decInsn, METHOD_STATIC);
            if (calledMethod == NULL)
                goto bail;
            setRegisterType(workRegs, RESULT_REGISTER(insnRegCountPlus),
                getMethodReturnType(calledMethod));
            justSetResult = true;
        }
        break;
    case OP_INVOKE_INTERFACE:
    case OP_INVOKE_INTERFACE_RANGE:
        {
            Method* absMethod;

            absMethod = getInvokedMethod(meth, &decInsn, METHOD_INTERFACE);
            if (absMethod == NULL)
                goto bail;
            setRegisterType(workRegs, RESULT_REGISTER(insnRegCountPlus),
                getMethodReturnType(absMethod));
            justSetResult = true;
        }
        break;

    case OP_NEG_INT:
    case OP_NOT_INT:
        setRegisterType(workRegs, decInsn.vA, kRegTypeInteger);
        break;
    case OP_NEG_LONG:
    case OP_NOT_LONG:
        setRegisterType(workRegs, decInsn.vA, kRegTypeLongLo);
        break;
    case OP_NEG_FLOAT:
        setRegisterType(workRegs, decInsn.vA, kRegTypeFloat);
        break;
    case OP_NEG_DOUBLE:
        setRegisterType(workRegs, decInsn.vA, kRegTypeDoubleLo);
        break;
    case OP_INT_TO_LONG:
        setRegisterType(workRegs, decInsn.vA, kRegTypeLongLo);
        break;
    case OP_INT_TO_FLOAT:
        setRegisterType(workRegs, decInsn.vA, kRegTypeFloat);
        break;
    case OP_INT_TO_DOUBLE:
        setRegisterType(workRegs, decInsn.vA, kRegTypeDoubleLo);
        break;
    case OP_LONG_TO_INT:
        setRegisterType(workRegs, decInsn.vA, kRegTypeInteger);
        break;
    case OP_LONG_TO_FLOAT:
        setRegisterType(workRegs, decInsn.vA, kRegTypeFloat);
        break;
    case OP_LONG_TO_DOUBLE:
        setRegisterType(workRegs, decInsn.vA, kRegTypeDoubleLo);
        break;
    case OP_FLOAT_TO_INT:
        setRegisterType(workRegs, decInsn.vA, kRegTypeInteger);
        break;
    case OP_FLOAT_TO_LONG:
        setRegisterType(workRegs, decInsn.vA, kRegTypeLongLo);
        break;
    case OP_FLOAT_TO_DOUBLE:
        setRegisterType(workRegs, decInsn.vA, kRegTypeDoubleLo);
        break;
    case OP_DOUBLE_TO_INT:
        setRegisterType(workRegs, decInsn.vA, kRegTypeInteger);
        break;
    case OP_DOUBLE_TO_LONG:
        setRegisterType(workRegs, decInsn.vA, kRegTypeLongLo);
        break;
    case OP_DOUBLE_TO_FLOAT:
        setRegisterType(workRegs, decInsn.vA, kRegTypeFloat);
        break;
    case OP_INT_TO_BYTE:
        setRegisterType(workRegs, decInsn.vA, kRegTypeByte);
        break;
    case OP_INT_TO_CHAR:
        setRegisterType(workRegs, decInsn.vA, kRegTypeChar);
        break;
    case OP_INT_TO_SHORT:
        setRegisterType(workRegs, decInsn.vA, kRegTypeShort);
        break;

    case OP_ADD_INT:
    case OP_SUB_INT:
    case OP_MUL_INT:
    case OP_REM_INT:
    case OP_DIV_INT:
    case OP_SHL_INT:
    case OP_SHR_INT:
    case OP_USHR_INT:
    case OP_AND_INT:
    case OP_OR_INT:
    case OP_XOR_INT:
        setRegisterType(workRegs, decInsn.vA, kRegTypeInteger);
        break;
    case OP_ADD_LONG:
    case OP_SUB_LONG:
    case OP_MUL_LONG:
    case OP_DIV_LONG:
    case OP_REM_LONG:
    case OP_AND_LONG:
    case OP_OR_LONG:
    case OP_XOR_LONG:
    case OP_SHL_LONG:
    case OP_SHR_LONG:
    case OP_USHR_LONG:
        setRegisterType(workRegs, decInsn.vA, kRegTypeLongLo);
        break;
    case OP_ADD_FLOAT:
    case OP_SUB_FLOAT:
    case OP_MUL_FLOAT:
    case OP_DIV_FLOAT:
    case OP_REM_FLOAT:
        setRegisterType(workRegs, decInsn.vA, kRegTypeFloat);
        break;
    case OP_ADD_DOUBLE:
    case OP_SUB_DOUBLE:
    case OP_MUL_DOUBLE:
    case OP_DIV_DOUBLE:
    case OP_REM_DOUBLE:
        setRegisterType(workRegs, decInsn.vA, kRegTypeDoubleLo);
        break;
    case OP_ADD_INT_2ADDR:
    case OP_SUB_INT_2ADDR:
    case OP_MUL_INT_2ADDR:
    case OP_REM_INT_2ADDR:
    case OP_SHL_INT_2ADDR:
    case OP_SHR_INT_2ADDR:
    case OP_USHR_INT_2ADDR:
    case OP_AND_INT_2ADDR:
    case OP_OR_INT_2ADDR:
    case OP_XOR_INT_2ADDR:
    case OP_DIV_INT_2ADDR:
        setRegisterType(workRegs, decInsn.vA, kRegTypeInteger);
        break;
    case OP_ADD_LONG_2ADDR:
    case OP_SUB_LONG_2ADDR:
    case OP_MUL_LONG_2ADDR:
    case OP_DIV_LONG_2ADDR:
    case OP_REM_LONG_2ADDR:
    case OP_AND_LONG_2ADDR:
    case OP_OR_LONG_2ADDR:
    case OP_XOR_LONG_2ADDR:
    case OP_SHL_LONG_2ADDR:
    case OP_SHR_LONG_2ADDR:
    case OP_USHR_LONG_2ADDR:
        setRegisterType(workRegs, decInsn.vA, kRegTypeLongLo);
        break;
    case OP_ADD_FLOAT_2ADDR:
    case OP_SUB_FLOAT_2ADDR:
    case OP_MUL_FLOAT_2ADDR:
    case OP_DIV_FLOAT_2ADDR:
    case OP_REM_FLOAT_2ADDR:
        setRegisterType(workRegs, decInsn.vA, kRegTypeFloat);
        break;
    case OP_ADD_DOUBLE_2ADDR:
    case OP_SUB_DOUBLE_2ADDR:
    case OP_MUL_DOUBLE_2ADDR:
    case OP_DIV_DOUBLE_2ADDR:
    case OP_REM_DOUBLE_2ADDR:
        setRegisterType(workRegs, decInsn.vA, kRegTypeDoubleLo);
        break;
    case OP_ADD_INT_LIT16:
    case OP_RSUB_INT:
    case OP_MUL_INT_LIT16:
    case OP_DIV_INT_LIT16:
    case OP_REM_INT_LIT16:
    case OP_AND_INT_LIT16:
    case OP_OR_INT_LIT16:
    case OP_XOR_INT_LIT16:
    case OP_ADD_INT_LIT8:
    case OP_RSUB_INT_LIT8:
    case OP_MUL_INT_LIT8:
    case OP_DIV_INT_LIT8:
    case OP_REM_INT_LIT8:
    case OP_SHL_INT_LIT8:
    case OP_SHR_INT_LIT8:
    case OP_USHR_INT_LIT8:
    case OP_AND_INT_LIT8:
    case OP_OR_INT_LIT8:
    case OP_XOR_INT_LIT8:
        setRegisterType(workRegs, decInsn.vA, kRegTypeInteger);
        break;


    /*
     * See comments in analysis/CodeVerify.c re: why some of these are
     * annoying to deal with.  It's worse in this implementation, because
     * we're not keeping any information about the classes held in each
     * reference register.
     *
     * Handling most of these would require retaining the field/method
     * reference info that we discarded when the instructions were
     * quickened.  This is feasible but not currently supported.
     */
    case OP_EXECUTE_INLINE:
    case OP_EXECUTE_INLINE_RANGE:
    case OP_INVOKE_DIRECT_EMPTY:
    case OP_IGET_QUICK:
    case OP_IGET_WIDE_QUICK:
    case OP_IGET_OBJECT_QUICK:
    case OP_IPUT_QUICK:
    case OP_IPUT_WIDE_QUICK:
    case OP_IPUT_OBJECT_QUICK:
    case OP_IGET_WIDE_VOLATILE:
    case OP_IPUT_WIDE_VOLATILE:
    case OP_SGET_WIDE_VOLATILE:
    case OP_SPUT_WIDE_VOLATILE:
    case OP_INVOKE_VIRTUAL_QUICK:
    case OP_INVOKE_VIRTUAL_QUICK_RANGE:
    case OP_INVOKE_SUPER_QUICK:
    case OP_INVOKE_SUPER_QUICK_RANGE:
        dvmAbort();     // not implemented, shouldn't be here
        break;


    /* these should never appear */
    case OP_UNUSED_3E:
    case OP_UNUSED_3F:
    case OP_UNUSED_40:
    case OP_UNUSED_41:
    case OP_UNUSED_42:
    case OP_UNUSED_43:
    case OP_UNUSED_73:
    case OP_UNUSED_79:
    case OP_UNUSED_7A:
    case OP_UNUSED_E3:
    case OP_UNUSED_E4:
    case OP_UNUSED_E5:
    case OP_UNUSED_E6:
    case OP_UNUSED_E7:
    case OP_BREAKPOINT:
    case OP_UNUSED_ED:
    case OP_UNUSED_F1:
    case OP_UNUSED_FC:
    case OP_UNUSED_FD:
    case OP_UNUSED_FE:
    case OP_UNUSED_FF:
        dvmAbort();
        break;

    /*
     * DO NOT add a "default" clause here.  Without it the compiler will
     * complain if an instruction is missing (which is desirable).
     */
    }


    /*
     * If we didn't just set the result register, clear it out.  This
     * isn't so important here, but does help ensure that our output matches
     * the verifier.
     */
    if (!justSetResult) {
        int reg = RESULT_REGISTER(pState->insnRegCountPlus);
        workRegs[reg] = workRegs[reg+1] = kRegTypeUnknown;
    }

    /*
     * Handle "continue".  Tag the next consecutive instruction.
     */
    if ((nextFlags & kInstrCanContinue) != 0) {
        int insnWidth = dvmInsnGetWidth(insnFlags, insnIdx);

        /*
         * We want to update the registers and set the "changed" flag on the
         * next instruction (if necessary).  We aren't storing register
         * changes for all addresses, so for non-GC-point targets we just
         * compare "entry" vs. "work" to see if we've changed anything.
         */
        if (getRegisterLine(pState, insnIdx+insnWidth) != NULL) {
            updateRegisters(pState, insnIdx+insnWidth, workRegs);
        } else {
            /* if not yet visited, or regs were updated, set "changed" */
            if (!dvmInsnIsVisited(insnFlags, insnIdx+insnWidth) ||
                compareRegisters(workRegs, entryRegs,
                    pState->insnRegCountPlus) != 0)
            {
                dvmInsnSetChanged(insnFlags, insnIdx+insnWidth, true);
            }
        }
    }

    /*
     * Handle "branch".  Tag the branch target.
     */
    if ((nextFlags & kInstrCanBranch) != 0) {
        bool isConditional;

        dvmGetBranchTarget(meth, insnFlags, insnIdx, &branchTarget,
                &isConditional);
        assert(isConditional || (nextFlags & kInstrCanContinue) == 0);
        assert(!isConditional || (nextFlags & kInstrCanContinue) != 0);

        updateRegisters(pState, insnIdx+branchTarget, workRegs);
    }

    /*
     * Handle "switch".  Tag all possible branch targets.
     */
    if ((nextFlags & kInstrCanSwitch) != 0) {
        int offsetToSwitch = insns[1] | (((s4)insns[2]) << 16);
        const u2* switchInsns = insns + offsetToSwitch;
        int switchCount = switchInsns[1];
        int offsetToTargets, targ;

        if ((*insns & 0xff) == OP_PACKED_SWITCH) {
            /* 0=sig, 1=count, 2/3=firstKey */
            offsetToTargets = 4;
        } else {
            /* 0=sig, 1=count, 2..count*2 = keys */
            assert((*insns & 0xff) == OP_SPARSE_SWITCH);
            offsetToTargets = 2 + 2*switchCount;
        }

        /* verify each switch target */
        for (targ = 0; targ < switchCount; targ++) {
            int offset, absOffset;

            /* offsets are 32-bit, and only partly endian-swapped */
            offset = switchInsns[offsetToTargets + targ*2] |
                     (((s4) switchInsns[offsetToTargets + targ*2 +1]) << 16);
            absOffset = insnIdx + offset;
            assert(absOffset >= 0 && absOffset < pState->insnsSize);

            updateRegisters(pState, absOffset, workRegs);
        }
    }

    /*
     * Handle instructions that can throw and that are sitting in a
     * "try" block.  (If they're not in a "try" block when they throw,
     * control transfers out of the method.)
     */
    if ((nextFlags & kInstrCanThrow) != 0 && dvmInsnIsInTry(insnFlags, insnIdx))
    {
        DexFile* pDexFile = meth->clazz->pDvmDex->pDexFile;
        const DexCode* pCode = dvmGetMethodCode(meth);
        DexCatchIterator iterator;

        if (dexFindCatchHandler(&iterator, pCode, insnIdx)) {
            while (true) {
                DexCatchHandler* handler = dexCatchIteratorNext(&iterator);
                if (handler == NULL)
                    break;

                /* note we use entryRegs, not workRegs */
                updateRegisters(pState, handler->address, entryRegs);
            }
        }
    }

    /*
     * Update startGuess.  Advance to the next instruction of that's
     * possible, otherwise use the branch target if one was found.  If
     * neither of those exists we're in a return or throw; leave startGuess
     * alone and let the caller sort it out.
     */
    if ((nextFlags & kInstrCanContinue) != 0) {
        *pStartGuess = insnIdx + dvmInsnGetWidth(insnFlags, insnIdx);
    } else if ((nextFlags & kInstrCanBranch) != 0) {
        /* we're still okay if branchTarget is zero */
        *pStartGuess = insnIdx + branchTarget;
    }

    assert(*pStartGuess >= 0 && *pStartGuess < pState->insnsSize &&
        dvmInsnGetWidth(insnFlags, *pStartGuess) != 0);

    result = true;

bail:
    return result;
}


/*
 * Merge two SRegType values.
 *
 * Sets "*pChanged" to "true" if the result doesn't match "type1".
 */
static SRegType mergeTypes(SRegType type1, SRegType type2, bool* pChanged)
{
    SRegType result;

    /*
     * Check for trivial case so we don't have to hit memory.
     */
    if (type1 == type2)
        return type1;

    /*
     * Use the table if we can, and reject any attempts to merge something
     * from the table with a reference type.
     *
     * The uninitialized table entry at index zero *will* show up as a
     * simple kRegTypeUninit value.  Since this cannot be merged with
     * anything but itself, the rules do the right thing.
     */
    if (type1 < kRegTypeMAX) {
        if (type2 < kRegTypeMAX) {
            result = gDvmMergeTab[type1][type2];
        } else {
            /* simple + reference == conflict, usually */
            if (type1 == kRegTypeZero)
                result = type2;
            else
                result = kRegTypeConflict;
        }
    } else {
        if (type2 < kRegTypeMAX) {
            /* reference + simple == conflict, usually */
            if (type2 == kRegTypeZero)
                result = type1;
            else
                result = kRegTypeConflict;
        } else {
            /* merging two references */
            assert(type1 == type2);
            result = type1;
        }
    }

    if (result != type1)
        *pChanged = true;
    return result;
}

/*
 * Control can transfer to "nextInsn".
 *
 * Merge the registers from "workRegs" into "addrRegs" at "nextInsn", and
 * set the "changed" flag on the target address if the registers have changed.
 */
static void updateRegisters(WorkState* pState, int nextInsn,
    const SRegType* workRegs)
{
    const Method* meth = pState->method;
    InsnFlags* insnFlags = pState->insnFlags;
    const int insnRegCountPlus = pState->insnRegCountPlus;
    SRegType* targetRegs = getRegisterLine(pState, nextInsn);

    if (!dvmInsnIsVisitedOrChanged(insnFlags, nextInsn)) {
        /*
         * We haven't processed this instruction before, and we haven't
         * touched the registers here, so there's nothing to "merge".  Copy
         * the registers over and mark it as changed.  (This is the only
         * way a register can transition out of "unknown", so this is not
         * just an optimization.)
         */
        LOGVV("COPY into 0x%04x\n", nextInsn);
        copyRegisters(targetRegs, workRegs, insnRegCountPlus);
        dvmInsnSetChanged(insnFlags, nextInsn, true);
    } else {
        /* merge registers, set Changed only if different */
        LOGVV("MERGE into 0x%04x\n", nextInsn);
        bool changed = false;
        int i;

        for (i = 0; i < insnRegCountPlus; i++) {
            targetRegs[i] = mergeTypes(targetRegs[i], workRegs[i], &changed);
        }

        if (changed)
            dvmInsnSetChanged(insnFlags, nextInsn, true);
    }
}

#endif /*#if 0*/
