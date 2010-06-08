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

static HashTable *gStackTraceHashTable = NULL;
static int gSerialNumber = 0;

/* Number of stack frames to cache */
#define STACK_DEPTH 8

typedef struct {
    int serialNumber;
    int threadSerialNumber;
    int frameIds[STACK_DEPTH];
} StackTrace;

typedef struct {
    StackTrace trace;
    u1 live;
} StackTraceEntry;

static u4 computeStackTraceHash(const StackTraceEntry *stackTraceEntry);

int
hprofStartup_Stack()
{
    HashIter iter;

    /* This will be called when a GC begins. */
    for (dvmHashIterBegin(gStackTraceHashTable, &iter);
         !dvmHashIterDone(&iter);
         dvmHashIterNext(&iter)) {
        StackTraceEntry *stackTraceEntry;

        /* Clear the 'live' bit at the start of the GC pass. */
        stackTraceEntry = (StackTraceEntry *) dvmHashIterData(&iter);
        stackTraceEntry->live = 0;
    }

    return 0;
}

int
hprofShutdown_Stack()
{
    HashIter iter;

    /* This will be called when a GC has completed. */
    for (dvmHashIterBegin(gStackTraceHashTable, &iter);
         !dvmHashIterDone(&iter);
         dvmHashIterNext(&iter)) {
        StackTraceEntry *stackTraceEntry;

        /*
         * If the 'live' bit is 0, the trace is not in use by any current
         * heap object and may be destroyed.
         */
        stackTraceEntry = (StackTraceEntry *) dvmHashIterData(&iter);
        if (!stackTraceEntry->live) {
            dvmHashTableRemove(gStackTraceHashTable,
                    computeStackTraceHash(stackTraceEntry), stackTraceEntry);
            free(stackTraceEntry);
        }
    }

    return 0;
}

static u4
computeStackTraceHash(const StackTraceEntry *stackTraceEntry)
{
    u4 hash = 0;
    const char *cp = (const char *) &stackTraceEntry->trace;
    int i;

    for (i = 0; i < (int) sizeof(StackTrace); i++) {
        hash = hash * 31 + cp[i];
    }

    return hash;
}

/* Only compare the 'trace' portion of the StackTraceEntry. */
static int
stackCmp(const void *tableItem, const void *looseItem)
{
    return memcmp(&((StackTraceEntry *) tableItem)->trace,
            &((StackTraceEntry *) looseItem)->trace, sizeof(StackTrace));
}

static StackTraceEntry *
stackDup(const StackTraceEntry *stackTrace)
{
    StackTraceEntry *newStackTrace = malloc(sizeof(StackTraceEntry));
    memcpy(newStackTrace, stackTrace, sizeof(StackTraceEntry));
    return newStackTrace;
}

static u4
hprofLookupStackSerialNumber(const StackTraceEntry *stackTrace)
{
    StackTraceEntry *val;
    u4 hashValue;
    int serial;

    /*
     * Create the hash table on first contact.  We can't do this in
     * hprofStartupStack, because we have to compute stack trace
     * serial numbers and place them into object headers before the
     * rest of hprof is triggered by a GC event.
     */
    if (gStackTraceHashTable == NULL) {
        gStackTraceHashTable = dvmHashTableCreate(512, free);
    }
    dvmHashTableLock(gStackTraceHashTable);

    hashValue = computeStackTraceHash(stackTrace);
    val = dvmHashTableLookup(gStackTraceHashTable, hashValue, (void *)stackTrace,
            (HashCompareFunc)stackCmp, false);
    if (val == NULL) {
        StackTraceEntry *newStackTrace;

        newStackTrace = stackDup(stackTrace);
        newStackTrace->trace.serialNumber = ++gSerialNumber;
        val = dvmHashTableLookup(gStackTraceHashTable, hashValue,
                (void *)newStackTrace, (HashCompareFunc)stackCmp, true);
        assert(val != NULL);
    }

    /* Mark the trace as live (in use by an object in the current heap). */
    val->live = 1;

    /* Grab the serial number before unlocking the table. */
    serial = val->trace.serialNumber;

    dvmHashTableUnlock(gStackTraceHashTable);

    return serial;
}

int
hprofDumpStacks(hprof_context_t *ctx)
{
    HashIter iter;
    hprof_record_t *rec = &ctx->curRec;

    dvmHashTableLock(gStackTraceHashTable);

    for (dvmHashIterBegin(gStackTraceHashTable, &iter);
         !dvmHashIterDone(&iter);
         dvmHashIterNext(&iter))
    {
        const StackTraceEntry *stackTraceEntry;
        int count;
        int i;

        hprofStartNewRecord(ctx, HPROF_TAG_STACK_TRACE, HPROF_TIME);

        stackTraceEntry = (const StackTraceEntry *) dvmHashIterData(&iter);
        assert(stackTraceEntry != NULL);

        /* STACK TRACE format:
         *
         * u4:     serial number for this stack
         * u4:     serial number for the running thread
         * u4:     number of frames
         * [ID]*:  ID for the stack frame
         */
        hprofAddU4ToRecord(rec, stackTraceEntry->trace.serialNumber);
        hprofAddU4ToRecord(rec, stackTraceEntry->trace.threadSerialNumber);

        count = 0;
        while ((count < STACK_DEPTH) &&
               (stackTraceEntry->trace.frameIds[count] != 0)) {
            count++;
        }
        hprofAddU4ToRecord(rec, count);
        for (i = 0; i < count; i++) {
            hprofAddU4ToRecord(rec, stackTraceEntry->trace.frameIds[i]);
        }
    }

    dvmHashTableUnlock(gStackTraceHashTable);

    return 0;
}

void
hprofFillInStackTrace(void *objectPtr)

{
    DvmHeapChunk *chunk;
    StackTraceEntry stackTraceEntry;
    Thread* self;
    void* fp;
    int i;

    if (objectPtr == NULL) {
        return;
    }
    self = dvmThreadSelf();
    if (self == NULL) {
        return;
    }
    fp = self->curFrame;

    /* Serial number to be filled in later. */
    stackTraceEntry.trace.serialNumber = -1;

    /*
     * TODO - The HAT tool doesn't care about thread data, so we can defer
     * actually emitting thread records and assigning thread serial numbers.
     */
    stackTraceEntry.trace.threadSerialNumber = (int) self;

    memset(&stackTraceEntry.trace.frameIds, 0,
            sizeof(stackTraceEntry.trace.frameIds));

    i = 0;
    while ((fp != NULL) && (i < STACK_DEPTH)) {
        const StackSaveArea* saveArea = SAVEAREA_FROM_FP(fp);
        const Method* method = saveArea->method;
        StackFrameEntry frame;

        if (!dvmIsBreakFrame(fp)) {
            frame.frame.method = method;
            if (dvmIsNativeMethod(method)) {
                frame.frame.pc = 0; /* no saved PC for native methods */
            } else {
                assert(saveArea->xtra.currentPc >= method->insns &&
                        saveArea->xtra.currentPc <
                        method->insns + dvmGetMethodInsnsSize(method));
                frame.frame.pc = (int) (saveArea->xtra.currentPc -
                        method->insns);
            }

            // Canonicalize the frame and cache it in the hprof context
            stackTraceEntry.trace.frameIds[i++] =
                hprofLookupStackFrameId(&frame);
        }

        assert(fp != saveArea->prevFrame);
        fp = saveArea->prevFrame;
    }

    /* Store the stack trace serial number in the object header */
    chunk = ptr2chunk(objectPtr);
    chunk->stackTraceSerialNumber =
            hprofLookupStackSerialNumber(&stackTraceEntry);
}
