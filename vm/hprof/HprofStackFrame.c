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

#include "Hprof.h"
#include "HprofStack.h"

#include "alloc/HeapInternal.h"

static HashTable *gStackFrameHashTable;

static u4 computeStackFrameHash(const StackFrameEntry *stackFrameEntry);

int
hprofStartup_StackFrame()
{
    HashIter iter;

    /* Cache the string "<unknown>" for use when the source file is
     * unknown.
     */
    hprofLookupStringId("<unknown>");

    /* This will be called when a GC begins. */
    for (dvmHashIterBegin(gStackFrameHashTable, &iter);
         !dvmHashIterDone(&iter);
         dvmHashIterNext(&iter)) {
        StackFrameEntry *stackFrameEntry;
        const Method *method;

        /* Clear the 'live' bit at the start of the GC pass. */
        stackFrameEntry = (StackFrameEntry *) dvmHashIterData(&iter);
        stackFrameEntry->live = 0;

        method = stackFrameEntry->frame.method;
        if (method == NULL) {
            continue;
        }

        /* Make sure the method name, descriptor, and source file are in
         * the string table, and that the method class is in the class
         * table. This is needed because strings and classes will be dumped
         * before stack frames.
         */

        if (method->name) {
            hprofLookupStringId(method->name);
        }

        DexStringCache cache;
        const char* descriptor;

        dexStringCacheInit(&cache);
        descriptor = dexProtoGetMethodDescriptor(&method->prototype, &cache);
        hprofLookupStringId(descriptor);
        dexStringCacheRelease(&cache);

        const char* sourceFile = dvmGetMethodSourceFile(method);
        if (sourceFile) {
            hprofLookupStringId(sourceFile);
        }

        if (method->clazz) {
            hprofLookupClassId(method->clazz);
        }
    }

    return 0;
}

int
hprofShutdown_StackFrame()
{
    HashIter iter;

    /* This will be called when a GC has completed. */
    for (dvmHashIterBegin(gStackFrameHashTable, &iter);
         !dvmHashIterDone(&iter);
         dvmHashIterNext(&iter)) {
        const StackFrameEntry *stackFrameEntry;

        /*
         * If the 'live' bit is 0, the frame is not in use by any current
         * heap object and may be destroyed.
         */
        stackFrameEntry = (const StackFrameEntry *) dvmHashIterData(&iter);
        if (!stackFrameEntry->live) {
            dvmHashTableRemove(gStackFrameHashTable,
                    computeStackFrameHash(stackFrameEntry),
                    (void*) stackFrameEntry);
            free((void*) stackFrameEntry);
        }
    }

    return 0;
}

/* Only hash the 'frame' portion of the StackFrameEntry. */
static u4
computeStackFrameHash(const StackFrameEntry *stackFrameEntry)
{
    u4 hash = 0;
    const char *cp = (char *) &stackFrameEntry->frame;
    int i;

    for (i = 0; i < (int) sizeof(StackFrame); i++) {
        hash = 31 * hash + cp[i];
    }
    return hash;
}

/* Only compare the 'frame' portion of the StackFrameEntry. */
static int
stackFrameCmp(const void *tableItem, const void *looseItem)
{
    return memcmp(&((StackFrameEntry *)tableItem)->frame,
            &((StackFrameEntry *) looseItem)->frame, sizeof(StackFrame));
}

static StackFrameEntry *
stackFrameDup(const StackFrameEntry *stackFrameEntry)
{
    StackFrameEntry *newStackFrameEntry = malloc(sizeof(StackFrameEntry));
    memcpy(newStackFrameEntry, stackFrameEntry, sizeof(StackFrameEntry));
    return newStackFrameEntry;
}

hprof_stack_frame_id
hprofLookupStackFrameId(const StackFrameEntry *stackFrameEntry)
{
    StackFrameEntry *val;
    u4 hashValue;

    /*
     * Create the hash table on first contact.  We can't do this in
     * hprofStartupStackFrame, because we have to compute stack trace
     * serial numbers and place them into object headers before the
     * rest of hprof is triggered by a GC event.
     */
    if (gStackFrameHashTable == NULL) {
        gStackFrameHashTable = dvmHashTableCreate(512, free);
    }
    dvmHashTableLock(gStackFrameHashTable);

    hashValue = computeStackFrameHash(stackFrameEntry);
    val = dvmHashTableLookup(gStackFrameHashTable, hashValue,
        (void *)stackFrameEntry, (HashCompareFunc)stackFrameCmp, false);
    if (val == NULL) {
        const StackFrameEntry *newStackFrameEntry;

        newStackFrameEntry = stackFrameDup(stackFrameEntry);
        val = dvmHashTableLookup(gStackFrameHashTable, hashValue,
            (void *)newStackFrameEntry, (HashCompareFunc)stackFrameCmp, true);
        assert(val != NULL);
    }

    /* Mark the frame as live (in use by an object in the current heap). */
    val->live = 1;

    dvmHashTableUnlock(gStackFrameHashTable);

    return (hprof_stack_frame_id) val;
}

int
hprofDumpStackFrames(hprof_context_t *ctx)
{
    HashIter iter;
    hprof_record_t *rec = &ctx->curRec;

    dvmHashTableLock(gStackFrameHashTable);

    for (dvmHashIterBegin(gStackFrameHashTable, &iter);
         !dvmHashIterDone(&iter);
         dvmHashIterNext(&iter))
    {
        const StackFrameEntry *stackFrameEntry;
        const Method *method;
        int pc;
        const char *sourceFile;
        ClassObject *clazz;
        int lineNum;

        hprofStartNewRecord(ctx, HPROF_TAG_STACK_FRAME, HPROF_TIME);

        stackFrameEntry = (const StackFrameEntry *) dvmHashIterData(&iter);
        assert(stackFrameEntry != NULL);

        method = stackFrameEntry->frame.method;
        pc = stackFrameEntry->frame.pc;
        sourceFile = dvmGetMethodSourceFile(method);
        if (sourceFile == NULL) {
            sourceFile = "<unknown>";
            lineNum = 0;
        } else {
            lineNum = dvmLineNumFromPC(method, pc);
        }
        clazz = (ClassObject *) hprofLookupClassId(method->clazz);

        /* STACK FRAME format:
         *
         * ID:     ID for this stack frame
         * ID:     ID for the method name
         * ID:     ID for the method descriptor
         * ID:     ID for the source file name
         * u4:     class serial number
         * u4:     line number, 0 = no line information
         *
         * We use the address of the stack frame as its ID.
         */

        DexStringCache cache;
        const char* descriptor;

        dexStringCacheInit(&cache);
        descriptor = dexProtoGetMethodDescriptor(&method->prototype, &cache);

        hprofAddIdToRecord(rec, (u4) stackFrameEntry);
        hprofAddIdToRecord(rec, hprofLookupStringId(method->name));
        hprofAddIdToRecord(rec, hprofLookupStringId(descriptor));
        hprofAddIdToRecord(rec, hprofLookupStringId(sourceFile));
        hprofAddU4ToRecord(rec, (u4) clazz->serialNumber);
        hprofAddU4ToRecord(rec, (u4) lineNum);

        dexStringCacheRelease(&cache);
    }

    dvmHashTableUnlock(gStackFrameHashTable);
    return 0;
}
