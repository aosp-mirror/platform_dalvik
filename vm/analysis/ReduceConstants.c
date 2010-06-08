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
 * Compress the range of "constant pool" indexes in instructions and
 * annotations to lower runtime RAM footprint.
 *
 * NOTE: this is an incomplete experimental feature.  Do not try to use it.
 */
#include "Dalvik.h"
#include "libdex/InstrUtils.h"
#include "libdex/OptInvocation.h"
#include "libdex/DexClass.h"

/*
Overview

When a class, method, field, or string constant is referred to from
Dalvik bytecode, the reference takes the form of an integer index value.
This value indexes into an array of type_id_item, method_id_item,
field_id_item, or string_id_item in the DEX file.  The first three
themselves contain (directly or indirectly) indexes to strings that the
resolver uses to convert the instruction stream index into a pointer to
the appropriate object or struct.

For example, an invoke-virtual instruction needs to specify which method
is to be invoked.  The method constant indexes into the method_id_item
array, each entry of which has indexes that specify the defining class
(type_id_item), method name (string_id_item), and method prototype
(proto_id_item).  The type_id_item just holds an index to a string_id_item,
which holds the file offset to the string with the class name.  The VM
finds the class by name, then searches through the class' table of virtual
methods to find one with a matching name and prototype.

This process is fairly expensive, so after the first time it completes
successfully, the VM records that the method index resolved to a specific
Method struct.  On subsequent execution, the VM just pulls the Method ptr
out of the resolved-methods array.  A similar approach is used with
the indexes for classes, fields, and string constants.

The problem with this approach is that we need to have a "resolved" entry
for every possible class, method, field, and string constant in every
DEX file, even if some of those aren't used from code.  The DEX string
constant table has entries for method prototypes and class names that are
never used by the code, and "public static final" fields often turn into
immediate constants.  The resolution table entries are only 4 bytes each,
but there are roughly 200,000 of them in the bootstrap classes alone.

DEX optimization removes many index references by replacing virtual method
indexes with vtable offsets and instance field indexes with byte offsets.
In the earlier example, the method would be resolved at "dexopt" time, and
the instruction rewritten as invoke-virtual-quick with the vtable offset.

(There are comparatively few classes compared to other constant pool
entries, and a much higher percentage (typically 60-70%) are used.  The
biggest gains come from the string pool.)

Using the resolved-entity tables provides a substantial performance
improvement, but results in applications allocating 1MB+ of tables that
are 70% unused.  The used and unused entries are freely intermixed,
preventing effective sharing with the zygote process, and resulting in
large numbers of private/dirty pages on the native heap as the tables
populate on first use.

The trick is to reduce the memory usage without decreasing performance.
Using smaller resolved-entity tables can actually give us a speed boost,
because we'll have a smaller "live" set of pages and make more effective
use of the data cache.


The approach we're going to use is to determine the set of indexes that
could potentially be resolved, generate a mapping from the minimal set to
the full set, and append the mapping to the DEX file.  This is done at
"dexopt" time, because we need to keep the changes in shared/read-only
pages or we'll lose the benefits of doing the work.

There are two ways to create and use the new mapping:

 (1) Write the entire full->minimal mapping to the ".odex" file.  On every
 instruction that uses an index, use the mapping to determine the
 "compressed" constant value, and then use that to index into the
 resolved-entity tables on the heap.  The instruction stream is unchanged,
 and the resolver can easily tell if a given index is cacheable.

 (2) Write the inverse miminal->full mapping to the ".odex" file, and
 rewrite the constants in the instruction stream.  The interpreter is
 unchanged, and the resolver code uses the mapping to find the original
 data in the DEX.

Approach #1 is easier and safer to implement, but it requires a table
lookup every time we execute an instruction that includes a constant
pool reference.  This causes an unacceptable performance hit, chiefly
because we're hitting semi-random memory pages and hosing the data cache.
This is mitigated somewhat by DEX optimizations that replace the constant
with a vtable index or field byte offset.  Approach #1 also requires
a larger map table, increasing the size of the DEX on disk.  One nice
property of approach #1 is that most of the DEX file is unmodified,
so use of the mapping is a runtime decision.

Approach #2 is preferred for performance reasons.


The class/method/field/string resolver code has to handle indices from
three sources: interpreted instructions, annotations, and exception
"catch" lists.  Sometimes these occur indirectly, e.g. we need to resolve
the declaring class associated with fields and methods when the latter
two are themselves resolved.  Parsing and rewriting instructions is fairly
straightforward, but annotations use a complex format with variable-width
index values.

We can safely rewrite index values in annotations if we guarantee that the
new value is smaller than the original.  This implies a two-pass approach:
the first determines the set of indexes actually used, the second does the
rewrite.  Doing the rewrite in a single pass would be much harder.

Instances of the "original" indices will still be found in the file; if
we try to be all-inclusive we will include some stuff that doesn't need
to be there (e.g. we don't generally need to cache the class name string
index result, since once we have the class resolved we don't need to look
it up by name through the resolver again).  There is some potential for
performance improvement by caching more than we strictly need, but we can
afford to give up a little performance during class loading if it allows
us to regain some memory.

For safety and debugging, it's useful to distinguish the "compressed"
constants in some way, e.g. setting the high bit when we rewrite them.
In practice we don't have any free bits: indexes are usually 16-bit
values, and we have more than 32,767 string constants in at least one of
our core DEX files.  Also, this does not work with constants embedded in
annotations, because of the variable-width encoding.

We should be safe if we can establish a clear distinction between sources
of "original" and "compressed" indices.  If the values get crossed up we
can end up with elusive bugs.  The easiest approach is to declare that
only indices pulled from certain locations (the instruction stream and/or
annotations) are compressed.  This prevents us from adding indices in
arbitrary locations to the compressed set, but should allow a reasonably
robust implementation.


Further implementation thoughts:

 - We don't have to do annotations in the first pass.  At heart the
   resolved entity cache is a performance optimization, not necessary for
   correctness, and we're not making annotation performance a priority
   at this stage.
 - The most important "fast path" is instruction processing.  Everything
   else can do additional work without having a measurable impact.
   However...
 - We need to keep an eye on uncached resolves to ensure that we haven't
   introduced noticeable performance losses.  In particular, the use of
   runtime annotations with string constants may suffer if we don't include
   annotation rewriting in the solution.
 - We can have separate resolver functions for "original" and "compressed"
   indices.  This way we don't have to add a flag argument to the resolver
   functions (which would require passing an additional parameter in from
   the interpreter).
 - The VM spec has some specific things to say about string constant
   equality and interning.  Index compression should have no effect on
   that; we just change how long it takes to find the interned string in
   certain circumstances.  The impact can be mitigated somewhat by
   improving the performance of the interned string table code.
 - This can make e.g. method resolution slower.  The method_id_item has
   an index to a method name string, and we will no longer cache the
   result of resolving that string.  This impacts resolution of any method
   with the same name as a previously-resolved method.
 - We may need to tweak the tools, particularly "dexdump", to show the
   translated values.
 - We can use 16-bit values in the mapping table, since we should have
   fewer than 2^16 remapped entries.  If we overflow we can skip the remap
   for that table or for the entire DEX file.  The resolver will need to
   check for the existence of the table to determine whether or not entries
   must be remapped.  The cost of the extra check is acceptable for
   approach #2, since it's only at resolve time, but may be undesirable
   for approach #1.
*/
/*
Output Formats

There are two possible output formats, from which we choose based on how
we plan to take advantage of the remapped constants.  At most one of these
will appear in the DEX.

NOTE: if EIXM appears in the DEX, the VM *must* be configured with
DVM_RESOLVER_CACHE=DVM_RC_EXPANDING (2).  Otherwise the constants we
pull from the instruction stream will be wrong and we will fail quickly.

For approach #1: map from original indices to the reduced set.

  This includes the four "mapToNew" tables.

  Format (RIXM):
   u4 classCount            // #of entries in classMap[]; == typeIdsSize
   u4 reducedClassCount     // #of entries in remapped table (for alloc)
   u2 classMap[]
   u4 methodCount
   u4 reducedMethodCount
   u2 methodMap[]
   u4 fieldCount
   u4 reducedFieldCount
   u2 fieldMap[]
   u4 stringCount
   u4 reducedStringCount
   u2 stringMap[]

For approach #2: map from the reduced set back to the originals.

  This includes the four "mapToOld" tables.

  Format (EIXM):
   u4 classCount            // #of entries in classMap[]; post-reduction
   u2 classMap[]
   u4 methodCount
   u2 methodMap[]
   u4 fieldCount
   u2 fieldMap[]
   u4 stringCount
   u2 stringMap[]

The arrays are padded so that the "count" values are always aligned on
32-bit boundaries.  All multi-byte values are in native host order.
*/


/*
 * Gather results from the post-optimization instruction scan.
 */
typedef struct ScanResults {
    /* output */
    BitVector*  usedClasses;
    BitVector*  usedMethods;
    BitVector*  usedFields;
    BitVector*  usedStrings;
} ScanResults;

/* prototype for the for-all-methods function */
typedef void (AllMethodsFunc)(DexFile* pDexFile, const char* classDescriptor,
    DexMethod* pDexMethod, void* arg);


/*
 * Free scan results.
 */
static void freeScanResults(ScanResults* pResults)
{
    if (pResults == NULL)
        return;

    dvmFreeBitVector(pResults->usedClasses);
    dvmFreeBitVector(pResults->usedMethods);
    dvmFreeBitVector(pResults->usedFields);
    dvmFreeBitVector(pResults->usedStrings);
    free(pResults);
}

/*
 * Allocate storage for the results of the instruction scan.
 */
static ScanResults* allocScanResults(const DexFile* pDexFile)
{
    ScanResults* pResults;
    const DexHeader* pHeader = pDexFile->pHeader;

    pResults = (ScanResults*) calloc(1, sizeof(ScanResults));
    if (pResults == NULL)
        return NULL;

    pResults->usedClasses = dvmAllocBitVector(pHeader->typeIdsSize, false);
    pResults->usedMethods = dvmAllocBitVector(pHeader->methodIdsSize, false);
    pResults->usedFields = dvmAllocBitVector(pHeader->fieldIdsSize, false);
    pResults->usedStrings = dvmAllocBitVector(pHeader->stringIdsSize, false);

    if (pResults->usedClasses == NULL ||
        pResults->usedMethods == NULL ||
        pResults->usedFields == NULL ||
        pResults->usedStrings == NULL)
    {
        freeScanResults(pResults);
        return NULL;
    }

    return pResults;
}

/*
 * Call "func(method, arg)" on all methods in the specified class.
 *
 * Pass in a pointer to the class_data_item, positioned at the start of
 * the field data (i.e. just past the class data header).
 *
 * "classDescriptor" is for debug messages.
 */
static void forAllMethodsInClass(DexFile* pDexFile, const u1** ppEncodedData,
    const DexClassDataHeader* pHeader, const char* classDescriptor,
    AllMethodsFunc func, void* arg)
{
    int i;

    /*
     * Consume field data.
     */
    if (pHeader->staticFieldsSize != 0) {
        int count = (int) pHeader->staticFieldsSize;
        u4 lastIndex = 0;
        DexField field;
        for (i = 0; i < count; i++) {
            dexReadClassDataField(ppEncodedData, &field, &lastIndex);
        }
    }
    if (pHeader->instanceFieldsSize != 0) {
        int count = (int) pHeader->instanceFieldsSize;
        u4 lastIndex = 0;
        DexField field;
        for (i = 0; i < count; i++) {
            dexReadClassDataField(ppEncodedData, &field, &lastIndex);
        }
    }

    /*
     * Run through all methods.
     */
    if (pHeader->directMethodsSize != 0) {
        int count = (int) pHeader->directMethodsSize;
        u4 lastIndex = 0;
        DexMethod method;

        for (i = 0; i < count; i++) {
            dexReadClassDataMethod(ppEncodedData, &method, &lastIndex);
            (func)(pDexFile, classDescriptor, &method, arg);
        }
    }
    if (pHeader->virtualMethodsSize != 0) {
        int count = (int) pHeader->virtualMethodsSize;
        u4 lastIndex = 0;
        DexMethod method;

        for (i = 0; i < count; i++) {
            dexReadClassDataMethod(ppEncodedData, &method, &lastIndex);
            (func)(pDexFile, classDescriptor, &method, arg);
        }
    }
}

/*
 * Call "func(method, arg)" on all methods in all classes in DexFile.
 */
static void forAllMethods(DexFile* pDexFile, AllMethodsFunc func, void* arg)
{
    u4 count = pDexFile->pHeader->classDefsSize;
    u4 idx;

    for (idx = 0; idx < count; idx++) {
        const DexClassDef* pClassDef;
        DexClassDataHeader header;
        const u1* pEncodedData;

        pClassDef = dexGetClassDef(pDexFile, idx);
        pEncodedData = dexGetClassData(pDexFile, pClassDef);

        const char* classDescriptor;
        classDescriptor = dexStringByTypeIdx(pDexFile, pClassDef->classIdx);

        if (pEncodedData != NULL) {
            dexReadClassDataHeader(&pEncodedData, &header);

            forAllMethodsInClass(pDexFile, &pEncodedData, &header,
                classDescriptor, func, arg);
        } else {
            //printf("%s: no class data\n", classDescriptor);
            /* no class data, e.g. "marker interface" */
        }
    }
}

/*
 * Mark a class ID as referenced.
 */
static void markClass(const u2* ptr, ScanResults* pResults)
{
    u2 classIdx = *ptr;
    if (!dvmSetBit(pResults->usedClasses, classIdx)) {
        LOGE("Unable to mark class %d as in-use\n", classIdx);
    }
}

/*
 * Mark a method ID as referenced.
 */
static void markMethod(const u2* ptr, ScanResults* pResults)
{
    u2 methodIdx = *ptr;
    if (!dvmSetBit(pResults->usedMethods, methodIdx)) {
        LOGE("Unable to mark method %d as in-use\n", methodIdx);
    }
}

/*
 * Mark a field ID as referenced.
 */
static void markField(const u2* ptr, ScanResults* pResults)
{
    u2 fieldIdx = *ptr;
    if (!dvmSetBit(pResults->usedFields, fieldIdx)) {
        LOGE("Unable to mark field %d as in-use\n", fieldIdx);
    }
}

/*
 * Mark a string constant as referenced.
 */
static void markString(const u2* ptr, ScanResults* pResults)
{
    u2 stringIdx = *ptr;
    if (!dvmSetBit(pResults->usedStrings, stringIdx)) {
        LOGE("Unable to mark string %d as in-use\n", stringIdx);
    }
}

/*
 * Mark a "jumbo" string constant as referenced.
 */
static void markJumboString(u2* ptr, ScanResults* pResults)
{
    u4 stringIdx;

    /* it's in native byte order, but might not be 32-bit aligned */
    memcpy(&stringIdx, ptr, sizeof(u4));
    if (!dvmSetBit(pResults->usedStrings, stringIdx)) {
        LOGE("Unable to mark string %d as in-use\n", stringIdx);
    }
}

/*
 * Remap a value in the instruction stream.
 */
static inline void updateValue(u2* ptr, const IndexMapSet* pIndexMapSet,
    int whichMap)
{
    const IndexMap* pMap = &pIndexMapSet->map[whichMap];
    if (pMap != NULL) {
        u2 newIdx = pMap->mapToNew[*ptr];
        assert(newIdx != kNoIndexMapping);
        *ptr = newIdx;
    }
}
static void updateClass(u2* ptr, const IndexMapSet* pIndexMapSet)
{
    updateValue(ptr, pIndexMapSet, kMapClasses);
}
static void updateMethod(u2* ptr, const IndexMapSet* pIndexMapSet)
{
    updateValue(ptr, pIndexMapSet, kMapMethods);
}
static void updateField(u2* ptr, const IndexMapSet* pIndexMapSet)
{
    updateValue(ptr, pIndexMapSet, kMapFields);
}
static void updateString(u2* ptr, const IndexMapSet* pIndexMapSet)
{
    updateValue(ptr, pIndexMapSet, kMapStrings);
}
static void updateJumboString(u2* ptr, const IndexMapSet* pIndexMapSet)
{
    u4 stringIdx;
    u4 newIdx;

    /* it's in native byte order, but might not be 32-bit aligned */
    memcpy(&stringIdx, ptr, sizeof(stringIdx));

    /* get new value */
    newIdx = pIndexMapSet->map[kMapStrings].mapToNew[*ptr];
    assert(newIdx != kNoIndexMapping);

    /* copy it out */
    memcpy(ptr, &newIdx, sizeof(newIdx));
}

/*
 * Run through an instructions stream, marking constants as we see them.
 *
 * If "pResults" is non-NULL, we populate "pResults" with what we find,
 * making no changes to the instruction stream.
 *
 * If "pIndexMapSet" is non-NULL, we rewrite the constants in the
 * instruction stream.
 */
static void markUsedConstantsFromInsns(u2* insns, u4 insnsSize,
    ScanResults* pResults, const IndexMapSet* pIndexMapSet)
{
    //printf(" %p %u units\n", insns, insnsSize);

    while (insnsSize > 0) {
        int width;
        u2* pConst = insns + 1;

        switch (*insns & 0xff) {
        case OP_IGET:
        case OP_IGET_WIDE:
        case OP_IGET_OBJECT:
        case OP_IGET_BOOLEAN:
        case OP_IGET_BYTE:
        case OP_IGET_CHAR:
        case OP_IGET_SHORT:
        case OP_IPUT:
        case OP_IPUT_WIDE:
        case OP_IPUT_OBJECT:
        case OP_IPUT_BOOLEAN:
        case OP_IPUT_BYTE:
        case OP_IPUT_CHAR:
        case OP_IPUT_SHORT:
        case OP_SGET:
        case OP_SGET_WIDE:
        case OP_SGET_OBJECT:
        case OP_SGET_BOOLEAN:
        case OP_SGET_BYTE:
        case OP_SGET_CHAR:
        case OP_SGET_SHORT:
        case OP_SPUT:
        case OP_SPUT_WIDE:
        case OP_SPUT_OBJECT:
        case OP_SPUT_BOOLEAN:
        case OP_SPUT_BYTE:
        case OP_SPUT_CHAR:
        case OP_SPUT_SHORT:
            /* instanceop vA, vB, field@CCCC */
            /* staticop vAA, field@BBBB */
            if (pResults != NULL)
                markField(pConst, pResults);
            else
                updateField(pConst, pIndexMapSet);
            break;

        case OP_CONST_STRING:
            /* const-string vAA, string@BBBB */
            if (pResults != NULL)
                markString(pConst, pResults);
            else
                updateString(pConst, pIndexMapSet);
            break;

        case OP_CONST_STRING_JUMBO:
            /* const-string/jumbo vAA, string@BBBBBBBB */
            if (pResults != NULL)
                markJumboString(pConst, pResults);
            else
                updateJumboString(pConst, pIndexMapSet);
            break;

        case OP_CONST_CLASS:
        case OP_CHECK_CAST:
        case OP_NEW_INSTANCE:
        case OP_FILLED_NEW_ARRAY_RANGE:
        case OP_INSTANCE_OF:
        case OP_NEW_ARRAY:
        case OP_FILLED_NEW_ARRAY:
            /* const-class vAA, type@BBBB */
            /* check-cast vAA, type@BBBB */
            /* new-instance vAA, type@BBBB */
            /* filled-new-array/range {vCCCC .. vNNNN}, type@BBBB */
            /* instance-of vA, vB, type@CCCC */
            /* new-array vA, vB, type@CCCC */
            /* filled-new-array {vD, vE, vF, vG, vA}, type@CCCC */
            if (pResults != NULL)
                markClass(pConst, pResults);
            else
                updateClass(pConst, pIndexMapSet);
            break;

        case OP_INVOKE_VIRTUAL:
        case OP_INVOKE_SUPER:
        case OP_INVOKE_DIRECT:
        case OP_INVOKE_STATIC:
        case OP_INVOKE_INTERFACE:
        case OP_INVOKE_VIRTUAL_RANGE:
        case OP_INVOKE_SUPER_RANGE:
        case OP_INVOKE_DIRECT_RANGE:
        case OP_INVOKE_STATIC_RANGE:
        case OP_INVOKE_INTERFACE_RANGE:
            /* invoke-kind {vD, vE, vF, vG, vA}, meth@CCCC */
            /* invoke-kind/range {vCCCC .. vNNNN}, meth@BBBB */
            if (pResults != NULL)
                markMethod(pConst, pResults);
            else
                updateMethod(pConst, pIndexMapSet);
            break;

        default:
            // ignore this instruction
            ;
        }

        width = dexGetInstrOrTableWidthAbs(gDvm.instrWidth, insns);
        assert(width > 0 && width <= (int)insnsSize);

        insns += width;
        insnsSize -= width;
    }
}

/*
 * This is an AllMethodsFunc implementation.
 *
 * Run through the instructions in this method, setting bits in the "pResults"
 * struct as we locate constants.
 */
static void markUsedConstants(DexFile* pDexFile, const char* classDescriptor,
    DexMethod* pDexMethod, void* arg)
{
    ScanResults* pResults = (ScanResults*) arg;
    const DexCode* pDexCode = dexGetCode(pDexFile, pDexMethod);

    if (false) {
        const DexMethodId* pMethodId;
        const char* methodName;
        pMethodId = dexGetMethodId(pDexFile, pDexMethod->methodIdx);
        methodName = dexStringById(pDexFile, pMethodId->nameIdx);
        printf(" %s.%s\n", classDescriptor, methodName);
    }

    if (pDexCode != NULL) {
        u2* insns = (u2*) pDexCode->insns;
        u4 insnsSize = pDexCode->insnsSize;
        markUsedConstantsFromInsns(insns, insnsSize, pResults, NULL);
    } else {
        //printf(" (no code)\n");
    }
}

/*
 * This is an AllMethodsFunc implementation.
 *
 * Run through the instructions in this method, altering the constants used.
 */
#if DVM_RESOLVER_CACHE == DVM_RC_EXPANDING
static void updateUsedConstants(DexFile* pDexFile, const char* classDescriptor,
    DexMethod* pDexMethod, void* arg)
{
    const IndexMapSet* pIndexMapSet = (const IndexMapSet*) arg;
    const DexCode* pDexCode = dexGetCode(pDexFile, pDexMethod);

    if (false) {
        const DexMethodId* pMethodId;
        const char* methodName;
        pMethodId = dexGetMethodId(pDexFile, pDexMethod->methodIdx);
        methodName = dexStringById(pDexFile, pMethodId->nameIdx);
        printf(" %s.%s\n", classDescriptor, methodName);
    }

    if (pDexCode != NULL) {
        u2* insns = (u2*) pDexCode->insns;
        u4 insnsSize = pDexCode->insnsSize;
        markUsedConstantsFromInsns(insns, insnsSize, NULL, pIndexMapSet);
    } else {
        //printf(" (no code)\n");
    }
}
#endif

/*
 * Count up the bits and show a count.
 */
static void showBitCount(const char* label, int setCount, int maxCount)
{
    printf("%s: %d of %d (%.1f%% unused)\n", label, setCount, maxCount,
        ((maxCount - setCount) * 100.0f) / maxCount);
}

/*
 * Print some debug info.
 */
static void summarizeResults(DvmDex* pDvmDex, ScanResults* pResults)
{
    DexFile* pDexFile = pDvmDex->pDexFile;
#if 0
    int i;

    for (i = 0; i < (int) pDvmDex->pDexFile->pHeader->typeIdsSize; i++) {
        const DexTypeId* pDexTypeId;
        const char* classDescr;

        pDexTypeId = dexGetTypeId(pDexFile, i);
        classDescr = dexStringById(pDexFile, pDexTypeId->descriptorIdx);

        if (dvmIsBitSet(pResults->usedStrings, i))
            printf("used  : %04x '%s'\n", i, classDescr);
        else
            printf("unused: %04x '%s'\n", i, classDescr);
    }
#endif
#if 0
    for (i = 0; i < (int) pDvmDex->pDexFile->pHeader->methodIdsSize; i++) {
        const DexMethodId* pDexMethodId;
        const DexTypeId* pDexTypeId;
        const char* classDescr;
        const char* methodName;

        pDexMethodId = dexGetMethodId(pDexFile, i);
        methodName = dexStringById(pDexFile, pDexMethodId->nameIdx);

        pDexTypeId = dexGetTypeId(pDexFile, pDexMethodId->classIdx);
        classDescr = dexStringById(pDexFile, pDexTypeId->descriptorIdx);
        if (dvmIsBitSet(pResults->usedMethods, i))
            printf("used  : %s.%s\n", classDescr, methodName);
        else
            printf("unused: %s.%s\n", classDescr, methodName);
    }
#endif
#if 0
    for (i = 0; i < (int) pDvmDex->pDexFile->pHeader->fieldIdsSize; i++) {
        const DexFieldId* pDexFieldId;
        const DexTypeId* pDexTypeId;
        const char* classDescr;
        const char* fieldName;

        pDexFieldId = dexGetFieldId(pDexFile, i);
        fieldName = dexStringById(pDexFile, pDexFieldId->nameIdx);

        pDexTypeId = dexGetTypeId(pDexFile, pDexFieldId->classIdx);
        classDescr = dexStringById(pDexFile, pDexTypeId->descriptorIdx);
        if (dvmIsBitSet(pResults->usedFields, i))
            printf("used  : %s.%s\n", classDescr, fieldName);
        else
            printf("unused: %s.%s\n", classDescr, fieldName);
    }
#endif
#if 0
    for (i = 0; i < (int) pDvmDex->pDexFile->pHeader->stringIdsSize; i++) {
        const char* str;

        str = dexStringById(pDexFile, i);

        if (dvmIsBitSet(pResults->usedStrings, i))
            printf("used  : %04x '%s'\n", i, str);
        else
            printf("unused: %04x '%s'\n", i, str);
    }
#endif

    int totalMax, totalSet;
    int setCount;

    totalMax = totalSet = 0;

    setCount = dvmCountSetBits(pResults->usedClasses);
    showBitCount("classes", setCount, pDexFile->pHeader->typeIdsSize);
    totalSet += setCount;
    totalMax += pDexFile->pHeader->typeIdsSize;

    setCount = dvmCountSetBits(pResults->usedMethods);
    showBitCount("methods", setCount, pDexFile->pHeader->methodIdsSize);
    totalSet += setCount;
    totalMax += pDexFile->pHeader->methodIdsSize;

    setCount = dvmCountSetBits(pResults->usedFields);
    showBitCount("fields",  setCount, pDexFile->pHeader->fieldIdsSize);
    totalSet += setCount;
    totalMax += pDexFile->pHeader->fieldIdsSize;

    setCount = dvmCountSetBits(pResults->usedStrings);
    showBitCount("strings", setCount, pDexFile->pHeader->stringIdsSize);
    totalSet += setCount;
    totalMax += pDexFile->pHeader->stringIdsSize;

    printf("TOTAL %d of %d (%.1f%% unused -- %.1fK)\n", totalSet, totalMax,
        ((totalMax - totalSet) * 100.0f) / totalMax,
        (totalMax - totalSet) / 256.0f);
}

/*
 * Fill out an index map set entry.
 *
 * If we can't fit the map into our base type, we don't create the map.
 *
 * Returns "false" if allocation fails.
 */
static bool constructIndexMap(int totalCount, const BitVector* pBits,
    IndexMap* pMap)
{
    const int kMaxIndex = 65534;        // 65535, a/k/a -1, is special
    int setCount;

    setCount = dvmCountSetBits(pBits);
    if (setCount < 0 || setCount > kMaxIndex)
        return true;

    u2* mapToOld = (u2*) malloc(setCount * sizeof(u2));
    u2* mapToNew = (u2*) malloc(totalCount * sizeof(u2));
    if (mapToOld == NULL || mapToNew == NULL) {
        free(mapToOld);
        free(mapToNew);
        return false;
    }

    /* fill in both arrays */
    int entry, idx = 0;
    for (entry = 0; entry < totalCount; entry++) {
        if (dvmIsBitSet(pBits, entry)) {
            mapToNew[entry] = idx;
            mapToOld[idx] = entry;
            idx++;
        } else {
            mapToNew[entry] = kNoIndexMapping;
        }
    }

    if (idx != setCount) {
        LOGE("GLITCH: idx=%d setCount=%d\n", idx, setCount);
        dvmAbort();
    }

    /* success */
    pMap->mapToOld = mapToOld;
    pMap->mapToNew = mapToNew;
    pMap->origCount = totalCount;
    pMap->newCount = setCount;

    return true;
}

/*
 * Construct a "reducing" chunk, with maps that convert the constants in
 * instructions to their reduced value for the cache lookup.
 */
static bool constructReducingDataChunk(IndexMapSet* pIndexMapSet)
{
    int chunkLen = 0;
    int i;

    pIndexMapSet->chunkType = kDexChunkReducingIndexMap;

    /*
     * Compute space requirements and allocate storage.
     */
    for (i = 0; i < kNumIndexMaps; i++) {
        /* space for the "original" count */
        chunkLen += sizeof(u4);

        /* space for the "reduced" count */
        chunkLen += sizeof(u4);

        /* add data length, round up to 32-bit boundary */
        chunkLen += pIndexMapSet->map[i].origCount * sizeof(u2);
        chunkLen = (chunkLen + 3) & ~3;
    }

    pIndexMapSet->chunkDataLen = chunkLen;
    pIndexMapSet->chunkData = (u1*) calloc(1, chunkLen);
    if (pIndexMapSet->chunkData == NULL)
        return false;

    /*
     * Copy the data in.
     */
    u1* ptr = pIndexMapSet->chunkData;
    for (i = 0; i < kNumIndexMaps; i++) {
        u4* wordPtr = (u4*) ptr;
        int dataLen = pIndexMapSet->map[i].origCount * sizeof(u2);

        *wordPtr++ = pIndexMapSet->map[i].origCount;
        *wordPtr++ = pIndexMapSet->map[i].newCount;
        if (dataLen != 0)
            memcpy(wordPtr, pIndexMapSet->map[i].mapToNew, dataLen);

        /* advance pointer, maintaining 32-bit alignment */
        ptr = ((u1*) wordPtr) + dataLen;
        ptr = (u1*) (((int) ptr + 3) & ~3);
    }

    if (ptr - (u1*) pIndexMapSet->chunkData != chunkLen) {
        LOGE("GLITCH: expected len=%d, actual=%d\n",
            chunkLen, ptr - (u1*) pIndexMapSet->chunkData);
        dvmAbort();
    }

    return true;
}

/*
 * Construct an "expanding" chunk, with maps that convert instructions
 * with reduced constants back to their full original values.
 */
#if DVM_RESOLVER_CACHE == DVM_RC_EXPANDING
static bool constructExpandingDataChunk(IndexMapSet* pIndexMapSet)
{
    int chunkLen = 0;
    int i;

    pIndexMapSet->chunkType = kDexChunkExpandingIndexMap;

    /*
     * Compute space requirements and allocate storage.
     */
    for (i = 0; i < kNumIndexMaps; i++) {
        /* space for the length word */
        chunkLen += sizeof(u4);

        /* add data length, round up to 32-bit boundary */
        chunkLen += pIndexMapSet->map[i].newCount * sizeof(u2);
        chunkLen = (chunkLen + 3) & ~3;
    }

    pIndexMapSet->chunkDataLen = chunkLen;
    pIndexMapSet->chunkData = (u1*) calloc(1, chunkLen);
    if (pIndexMapSet->chunkData == NULL)
        return false;

    /*
     * Copy the data in.
     */
    u1* ptr = pIndexMapSet->chunkData;
    for (i = 0; i < kNumIndexMaps; i++) {
        u4* wordPtr = (u4*) ptr;
        int dataLen = pIndexMapSet->map[i].newCount * sizeof(u2);

        *wordPtr++ = pIndexMapSet->map[i].newCount;
        if (dataLen != 0)
            memcpy(wordPtr, pIndexMapSet->map[i].mapToOld, dataLen);

        /* advance pointer, maintaining 32-bit alignment */
        ptr = ((u1*) wordPtr) + dataLen;
        ptr = (u1*) (((int) ptr + 3) & ~3);
    }

    if (ptr - (u1*) pIndexMapSet->chunkData != chunkLen) {
        LOGE("GLITCH: expected len=%d, actual=%d\n",
            chunkLen, ptr - (u1*) pIndexMapSet->chunkData);
        dvmAbort();
    }

    return true;
}
#endif

/*
 * Construct the "chunk" of data that will be appended to the optimized DEX
 * file.
 */
static bool constructDataChunk(IndexMapSet* pIndexMapSet)
{
    assert(sizeof(pIndexMapSet->map[0].mapToOld[0]) == sizeof(u2));
    assert(sizeof(pIndexMapSet->map[0].mapToNew[0]) == sizeof(u2));

#if DVM_RESOLVER_CACHE == DVM_RC_EXPANDING
    return constructExpandingDataChunk(pIndexMapSet);
#else
    return constructReducingDataChunk(pIndexMapSet);
#endif
}

/*
 * Allocate storage to hold the maps.
 */
static IndexMapSet* createIndexMapSet(const DexFile* pDexFile,
    ScanResults* pResults)
{
    IndexMapSet* pIndexMapSet;
    bool okay = true;

    pIndexMapSet = calloc(1, sizeof(*pIndexMapSet));
    if (pIndexMapSet == NULL)
        return NULL;

    okay = okay && constructIndexMap(pDexFile->pHeader->typeIdsSize,
            pResults->usedClasses, &pIndexMapSet->map[kMapClasses]);
    okay = okay && constructIndexMap(pDexFile->pHeader->methodIdsSize,
            pResults->usedMethods, &pIndexMapSet->map[kMapMethods]);
    okay = okay && constructIndexMap(pDexFile->pHeader->fieldIdsSize,
            pResults->usedFields, &pIndexMapSet->map[kMapFields]);
    okay = okay && constructIndexMap(pDexFile->pHeader->stringIdsSize,
            pResults->usedStrings, &pIndexMapSet->map[kMapStrings]);

    LOGVV("Constr: %d %d %d %d\n",
        pIndexMapSet->map[kMapClasses].mapToOld[0],
        pIndexMapSet->map[kMapMethods].mapToOld[0],
        pIndexMapSet->map[kMapFields].mapToOld[0],
        pIndexMapSet->map[kMapStrings].mapToOld[0]);

    okay = okay && constructDataChunk(pIndexMapSet);

    if (!okay) {
        dvmFreeIndexMapSet(pIndexMapSet);
        return NULL;
    }

    return pIndexMapSet;
}

/*
 * Free map storage.
 *
 * "pIndexMapSet" may be incomplete.
 */
void dvmFreeIndexMapSet(IndexMapSet* pIndexMapSet)
{
    int i;

    if (pIndexMapSet == NULL)
        return;

    for (i = 0; i < kNumIndexMaps; i++) {
        free(pIndexMapSet->map[i].mapToOld);
        free(pIndexMapSet->map[i].mapToNew);
    }
    free(pIndexMapSet->chunkData);
    free(pIndexMapSet);
}

/*
 * Rewrite constant indexes to reduce heap requirements.
 */
IndexMapSet* dvmRewriteConstants(DvmDex* pDvmDex)
{
#if (DVM_RESOLVER_CACHE != DVM_RC_REDUCING) && \
    (DVM_RESOLVER_CACHE != DVM_RC_EXPANDING)
    /* nothing to do */
    return NULL;
#endif

    /*
     * We're looking for instructions that use "constant pool" entries for
     * classes, methods, fields, and strings.  Many field and method entries
     * are optimized away, and many string constants are never accessed from
     * code or annotations.
     */
    ScanResults* pResults = allocScanResults(pDvmDex->pDexFile);
    forAllMethods(pDvmDex->pDexFile, markUsedConstants, pResults);

    summarizeResults(pDvmDex, pResults);

    /*
     * Allocate and populate the index maps.
     */
    IndexMapSet* pIndexMapSet = createIndexMapSet(pDvmDex->pDexFile, pResults);
#if DVM_RESOLVER_CACHE == DVM_RC_EXPANDING
    if (pIndexMapSet != NULL) {
        /*
         * Rewrite the constants to use the reduced set.
         */
        forAllMethods(pDvmDex->pDexFile, updateUsedConstants, pIndexMapSet);
    }
#endif

    freeScanResults(pResults);

    return pIndexMapSet;
}

