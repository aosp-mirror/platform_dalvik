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

#include <fcntl.h>
#include <stdlib.h>

#include "Dalvik.h"
#include "HeapInternal.h"
#include "HeapSource.h"
#include "Float12.h"

int dvmGetHeapDebugInfo(HeapDebugInfoType info)
{
    switch (info) {
    case kVirtualHeapSize:
        return (int)dvmHeapSourceGetValue(HS_FOOTPRINT, NULL, 0);
    case kVirtualHeapAllocated:
        return (int)dvmHeapSourceGetValue(HS_BYTES_ALLOCATED, NULL, 0);
    default:
        return -1;
    }
}

/* Looks up the cmdline for the process and tries to find
 * the most descriptive five characters, then inserts the
 * short name into the provided event value.
 */
#define PROC_NAME_LEN 5
static void insertProcessName(long long *ep)
{
    static bool foundRealName = false;
    static char name[PROC_NAME_LEN] = { 'X', 'X', 'X', 'X', 'X' };
    long long event = *ep;

    if (!foundRealName) {
        int fd = open("/proc/self/cmdline", O_RDONLY);
        if (fd > 0) {
            char buf[128];
            ssize_t n = read(fd, buf, sizeof(buf) - 1);
            close(fd);
            if (n > 0) {
                memset(name, 0, sizeof(name));
                if (n <= PROC_NAME_LEN) {
                    // The whole name fits.
                    memcpy(name, buf, n);
                } else {
                    /* We need to truncate.  The name will look something
                     * like "com.android.home".  Favor the characters
                     * immediately following the last dot.
                     */
                    buf[n] = '\0';
                    char *dot = strrchr(buf, '.');
                    if (dot == NULL) {
                        /* Or, look for a slash, in case it's something like
                         * "/system/bin/runtime".
                         */
                        dot = strrchr(buf, '/');
                    }
                    if (dot != NULL) {
                        dot++;  // Skip the dot
                        size_t dotlen = strlen(dot);
                        if (dotlen < PROC_NAME_LEN) {
                            /* Use all available characters.  We know that
                             * n > PROC_NAME_LEN from the check above.
                             */
                            dot -= PROC_NAME_LEN - dotlen;
                        }
                        strncpy(name, dot, PROC_NAME_LEN);
                    } else {
                        // No dot; just use the leading characters.
                        memcpy(name, buf, PROC_NAME_LEN);
                    }
                }
                if (strcmp(buf, "zygote") != 0) {
                    /* If the process is no longer called "zygote",
                     * cache this name.
                     */
                    foundRealName = true;
                }
            }
        }
    }

    event &= ~(0xffffffffffLL << 24);
    event |= (long long)name[0] << 56;
    event |= (long long)name[1] << 48;
    event |= (long long)name[2] << 40;
    event |= (long long)name[3] << 32;
    event |= (long long)name[4] << 24;

    *ep = event;
}

// See device/data/etc/event-log-tags
#define EVENT_LOG_TAG_dvm_gc_info 20001
#define EVENT_LOG_TAG_dvm_gc_madvise_info 20002

void dvmLogGcStats(size_t numFreed, size_t sizeFreed, size_t gcTimeMs)
{
    size_t perHeapActualSize[HEAP_SOURCE_MAX_HEAP_COUNT],
           perHeapAllowedSize[HEAP_SOURCE_MAX_HEAP_COUNT],
           perHeapNumAllocated[HEAP_SOURCE_MAX_HEAP_COUNT],
           perHeapSizeAllocated[HEAP_SOURCE_MAX_HEAP_COUNT];
    unsigned char eventBuf[1 + (1 + sizeof(long long)) * 4];
    size_t actualSize, allowedSize, numAllocated, sizeAllocated;
    size_t softLimit = dvmHeapSourceGetIdealFootprint();
    size_t nHeaps = dvmHeapSourceGetNumHeaps();

    /* Enough to quiet down gcc for unitialized variable check */
    perHeapActualSize[0] = perHeapAllowedSize[0] = perHeapNumAllocated[0] =
                           perHeapSizeAllocated[0] = 0;
    actualSize = dvmHeapSourceGetValue(HS_FOOTPRINT, perHeapActualSize,
                                       HEAP_SOURCE_MAX_HEAP_COUNT);
    allowedSize = dvmHeapSourceGetValue(HS_ALLOWED_FOOTPRINT,
                      perHeapAllowedSize, HEAP_SOURCE_MAX_HEAP_COUNT);
    numAllocated = dvmHeapSourceGetValue(HS_OBJECTS_ALLOCATED,
                      perHeapNumAllocated, HEAP_SOURCE_MAX_HEAP_COUNT);
    sizeAllocated = dvmHeapSourceGetValue(HS_BYTES_ALLOCATED,
                      perHeapSizeAllocated, HEAP_SOURCE_MAX_HEAP_COUNT);

    /*
     * Construct the the first 64-bit value to write to the log.
     * Global information:
     *
     * [63   ] Must be zero
     * [62-24] ASCII process identifier
     * [23-12] GC time in ms
     * [11- 0] Bytes freed
     *
     */
    long long event0;
    event0 = 0LL << 63 |
            (long long)intToFloat12(gcTimeMs) << 12 |
            (long long)intToFloat12(sizeFreed);
    insertProcessName(&event0);

    /*
     * Aggregated heap stats:
     *
     * [63-62] 10
     * [61-60] Reserved; must be zero
     * [59-48] Objects freed
     * [47-36] Actual size (current footprint)
     * [35-24] Allowed size (current hard max)
     * [23-12] Objects allocated
     * [11- 0] Bytes allocated
     */
    long long event1;
    event1 = 2LL << 62 |
            (long long)intToFloat12(numFreed) << 48 |
            (long long)intToFloat12(actualSize) << 36 |
            (long long)intToFloat12(allowedSize) << 24 |
            (long long)intToFloat12(numAllocated) << 12 |
            (long long)intToFloat12(sizeAllocated);

    /*
     * Report the current state of the zygote heap(s).
     *
     * The active heap is always heap[0].  We can be in one of three states
     * at present:
     *
     *  (1) Still in the zygote.  Zygote using heap[0].
     *  (2) In the zygote, when the first child is started.  We created a
     *      new heap just before the first fork() call, so the original
     *      "zygote heap" is now heap[1], and we have a small heap[0] for
     *      anything we do from here on.
     *  (3) In an app process.  The app gets a new heap[0], and can also
     *      see the two zygote heaps [1] and [2] (probably unwise to
     *      assume any specific ordering).
     *
     * So if nHeaps == 1, we want the stats from heap[0]; else we want
     * the sum of the values from heap[1] to heap[nHeaps-1].
     *
     *
     * Zygote heap stats (except for the soft limit, which belongs to the
     * active heap):
     *
     * [63-62] 11
     * [61-60] Reserved; must be zero
     * [59-48] Soft Limit (for the active heap)
     * [47-36] Actual size (current footprint)
     * [35-24] Allowed size (current hard max)
     * [23-12] Objects allocated
     * [11- 0] Bytes allocated
     */
    long long event2;
    size_t zActualSize, zAllowedSize, zNumAllocated, zSizeAllocated;
    int firstHeap = (nHeaps == 1) ? 0 : 1;
    size_t hh;

    zActualSize = zAllowedSize = zNumAllocated = zSizeAllocated = 0;
    for (hh = firstHeap; hh < nHeaps; hh++) {
        zActualSize += perHeapActualSize[hh];
        zAllowedSize += perHeapAllowedSize[hh];
        zNumAllocated += perHeapNumAllocated[hh];
        zSizeAllocated += perHeapSizeAllocated[hh];
    }
    event2 = 3LL << 62 |
            (long long)intToFloat12(softLimit) << 48 |
            (long long)intToFloat12(zActualSize) << 36 |
            (long long)intToFloat12(zAllowedSize) << 24 |
            (long long)intToFloat12(zNumAllocated) << 12 |
            (long long)intToFloat12(zSizeAllocated);

    /*
     * Report the current external allocation stats and the native heap
     * summary.
     *
     * [63-48] Reserved; must be zero (TODO: put new data in these slots)
     * [47-36] dlmalloc_footprint
     * [35-24] mallinfo: total allocated space
     * [23-12] External byte limit
     * [11- 0] External bytes allocated
     */
    long long event3;
    size_t externalLimit, externalBytesAllocated;
    size_t uordblks, footprint;

#if 0
    /*
     * This adds 2-5msec to the GC cost on a DVT, or about 2-3% of the cost
     * of a GC, so it's not horribly expensive but it's not free either.
     */
    extern size_t dlmalloc_footprint(void);
    struct mallinfo mi;
    //u8 start, end;

    //start = dvmGetRelativeTimeNsec();
    mi = mallinfo();
    uordblks = mi.uordblks;
    footprint = dlmalloc_footprint();
    //end = dvmGetRelativeTimeNsec();
    //LOGD("mallinfo+footprint took %dusec; used=%zd footprint=%zd\n",
    //    (int)((end - start) / 1000), mi.uordblks, footprint);
#else
    uordblks = footprint = 0;
#endif

    externalLimit =
            dvmHeapSourceGetValue(HS_EXTERNAL_LIMIT, NULL, 0);
    externalBytesAllocated =
            dvmHeapSourceGetValue(HS_EXTERNAL_BYTES_ALLOCATED, NULL, 0);
    event3 =
            (long long)intToFloat12(footprint) << 36 |
            (long long)intToFloat12(uordblks) << 24 |
            (long long)intToFloat12(externalLimit) << 12 |
            (long long)intToFloat12(externalBytesAllocated);

    /* Build the event data.
     * [ 0: 0] item count (4)
     * [ 1: 1] EVENT_TYPE_LONG
     * [ 2: 9] event0
     * [10:10] EVENT_TYPE_LONG
     * [11:18] event1
     * [19:19] EVENT_TYPE_LONG
     * [20:27] event2
     * [28:28] EVENT_TYPE_LONG
     * [29:36] event2
     */
    unsigned char *c = eventBuf;
    *c++ = 4;
    *c++ = EVENT_TYPE_LONG;
    memcpy(c, &event0, sizeof(event0));
    c += sizeof(event0);
    *c++ = EVENT_TYPE_LONG;
    memcpy(c, &event1, sizeof(event1));
    c += sizeof(event1);
    *c++ = EVENT_TYPE_LONG;
    memcpy(c, &event2, sizeof(event2));
    c += sizeof(event2);
    *c++ = EVENT_TYPE_LONG;
    memcpy(c, &event3, sizeof(event3));

    (void) android_btWriteLog(EVENT_LOG_TAG_dvm_gc_info, EVENT_TYPE_LIST,
            eventBuf, sizeof(eventBuf));
}

void dvmLogMadviseStats(size_t madvisedSizes[], size_t arrayLen)
{
    unsigned char eventBuf[1 + (1 + sizeof(int)) * 2];
    size_t total, zyg;
    size_t firstHeap, i;
    size_t nHeaps = dvmHeapSourceGetNumHeaps();

    assert(arrayLen >= nHeaps);

    firstHeap = nHeaps > 1 ? 1 : 0;
    total = 0;
    zyg = 0;
    for (i = 0; i < nHeaps; i++) {
        total += madvisedSizes[i];
        if (i >= firstHeap) {
            zyg += madvisedSizes[i];
        }
    }

    /* Build the event data.
     * [ 0: 0] item count (2)
     * [ 1: 1] EVENT_TYPE_INT
     * [ 2: 5] total madvise byte count
     * [ 6: 6] EVENT_TYPE_INT
     * [ 7:10] zygote heap madvise byte count
     */
    unsigned char *c = eventBuf;
    *c++ = 2;
    *c++ = EVENT_TYPE_INT;
    memcpy(c, &total, sizeof(total));
    c += sizeof(total);
    *c++ = EVENT_TYPE_INT;
    memcpy(c, &zyg, sizeof(zyg));
    c += sizeof(zyg);

    (void) android_btWriteLog(EVENT_LOG_TAG_dvm_gc_madvise_info,
            EVENT_TYPE_LIST, eventBuf, sizeof(eventBuf));
}

#if 0
#include <errno.h>
#include <stdio.h>

typedef struct HeapDumpContext {
    FILE *fp;
    void *chunkStart;
    size_t chunkLen;
    bool chunkFree;
} HeapDumpContext;

static void
dump_context(const HeapDumpContext *ctx)
{
    fprintf(ctx->fp, "0x%08x %12.12zd %s\n", (uintptr_t)ctx->chunkStart,
            ctx->chunkLen, ctx->chunkFree ? "FREE" : "USED");
}

static void
heap_chunk_callback(const void *chunkptr, size_t chunklen,
                    const void *userptr, size_t userlen, void *arg)
{
    HeapDumpContext *ctx = (HeapDumpContext *)arg;
    bool chunkFree = (userptr == NULL);

    if (chunkFree != ctx->chunkFree ||
            ((char *)ctx->chunkStart + ctx->chunkLen) != chunkptr)
    {
        /* The new chunk is of a different type or isn't
         * contiguous with the current chunk.  Dump the
         * old one and start a new one.
         */
        if (ctx->chunkStart != NULL) {
            /* It's not the first chunk. */
            dump_context(ctx);
        }
        ctx->chunkStart = (void *)chunkptr;
        ctx->chunkLen = chunklen;
        ctx->chunkFree = chunkFree;
    } else {
        /* Extend the current chunk.
         */
        ctx->chunkLen += chunklen;
    }
}

/* Dumps free and used ranges, as text, to the named file.
 */
void dvmDumpHeapToFile(const char *fileName)
{
    HeapDumpContext ctx;
    FILE *fp;

    fp = fopen(fileName, "w+");
    if (fp == NULL) {
        LOGE("Can't open %s for writing: %s\n", fileName, strerror(errno));
        return;
    }
    LOGW("Dumping heap to %s...\n", fileName);

    fprintf(fp, "==== Dalvik heap dump ====\n");
    memset(&ctx, 0, sizeof(ctx));
    ctx.fp = fp;
    dvmHeapSourceWalk(heap_chunk_callback, (void *)&ctx);
    dump_context(&ctx);
    fprintf(fp, "==== end heap dump ====\n");

    LOGW("Dumped heap to %s.\n", fileName);

    fclose(fp);
}
#endif
