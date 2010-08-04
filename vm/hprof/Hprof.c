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
 * Preparation and completion of hprof data generation.  The output is
 * written into two files and then combined.  This is necessary because
 * we generate some of the data (strings and classes) while we dump the
 * heap, and some analysis tools require that the class and string data
 * appear first.
 */
#include "Hprof.h"

#include <string.h>
#include <unistd.h>
#include <fcntl.h>
#include <errno.h>
#include <sys/time.h>
#include <time.h>


#define kHeadSuffix "-hptemp"

hprof_context_t *
hprofStartup(const char *outputFileName, int fd, bool directToDdms)
{
    hprofStartup_String();
    hprofStartup_Class();
#if WITH_HPROF_STACK
    hprofStartup_StackFrame();
    hprofStartup_Stack();
#endif

    hprof_context_t *ctx = malloc(sizeof(*ctx));
    if (ctx == NULL) {
        LOGE("hprof: can't allocate context.\n");
        return NULL;
    }

    /* pass in name or descriptor of the output file */
    hprofContextInit(ctx, strdup(outputFileName), fd, false, directToDdms);

    assert(ctx->memFp != NULL);

    return ctx;
}

/*
 * Finish up the hprof dump.  Returns true on success.
 */
bool
hprofShutdown(hprof_context_t *tailCtx)
{
    /* flush the "tail" portion of the output */
    hprofFlushCurrentRecord(tailCtx);

    /*
     * Create a new context struct for the start of the file.  We
     * heap-allocate it so we can share the "free" function.
     */
    hprof_context_t *headCtx = malloc(sizeof(*headCtx));
    if (headCtx == NULL) {
        LOGE("hprof: can't allocate context.\n");
        hprofFreeContext(tailCtx);
        return false;
    }
    hprofContextInit(headCtx, strdup(tailCtx->fileName), tailCtx->fd, true,
        tailCtx->directToDdms);

    LOGI("hprof: dumping heap strings to \"%s\".\n", tailCtx->fileName);
    hprofDumpStrings(headCtx);
    hprofDumpClasses(headCtx);

    /* Write a dummy stack trace record so the analysis
     * tools don't freak out.
     */
    hprofStartNewRecord(headCtx, HPROF_TAG_STACK_TRACE, HPROF_TIME);
    hprofAddU4ToRecord(&headCtx->curRec, HPROF_NULL_STACK_TRACE);
    hprofAddU4ToRecord(&headCtx->curRec, HPROF_NULL_THREAD);
    hprofAddU4ToRecord(&headCtx->curRec, 0);    // no frames

#if WITH_HPROF_STACK
    hprofDumpStackFrames(headCtx);
    hprofDumpStacks(headCtx);
#endif

    hprofFlushCurrentRecord(headCtx);

    hprofShutdown_Class();
    hprofShutdown_String();
#if WITH_HPROF_STACK
    hprofShutdown_Stack();
    hprofShutdown_StackFrame();
#endif

    /* flush to ensure memstream pointer and size are updated */
    fflush(headCtx->memFp);
    fflush(tailCtx->memFp);

    if (tailCtx->directToDdms) {
        /* send the data off to DDMS */
        struct iovec iov[2];
        iov[0].iov_base = headCtx->fileDataPtr;
        iov[0].iov_len = headCtx->fileDataSize;
        iov[1].iov_base = tailCtx->fileDataPtr;
        iov[1].iov_len = tailCtx->fileDataSize;
        dvmDbgDdmSendChunkV(CHUNK_TYPE("HPDS"), iov, 2);
    } else {
        /*
         * Open the output file, and copy the head and tail to it.
         */
        assert(headCtx->fd == tailCtx->fd);

        int outFd;
        if (headCtx->fd >= 0) {
            outFd = dup(headCtx->fd);
            if (outFd < 0) {
                LOGE("dup(%d) failed: %s\n", headCtx->fd, strerror(errno));
                /* continue to fail-handler below */
            }
        } else {
            outFd = open(tailCtx->fileName, O_WRONLY|O_CREAT, 0644);
            if (outFd < 0) {
                LOGE("can't open %s: %s\n", headCtx->fileName, strerror(errno));
                /* continue to fail-handler below */
            }
        }
        if (outFd < 0) {
            hprofFreeContext(headCtx);
            hprofFreeContext(tailCtx);
            return false;
        }

        int result;
        result = sysWriteFully(outFd, headCtx->fileDataPtr,
            headCtx->fileDataSize, "hprof-head");
        result |= sysWriteFully(outFd, tailCtx->fileDataPtr,
            tailCtx->fileDataSize, "hprof-tail");
        close(outFd);
        if (result != 0) {
            hprofFreeContext(headCtx);
            hprofFreeContext(tailCtx);
            return false;
        }
    }

    /* throw out a log message for the benefit of "runhat" */
    LOGI("hprof: heap dump completed (%dKB)\n",
        (headCtx->fileDataSize + tailCtx->fileDataSize + 1023) / 1024);

    hprofFreeContext(headCtx);
    hprofFreeContext(tailCtx);

    return true;
}

/*
 * Free any heap-allocated items in "ctx", and then free "ctx" itself.
 */
void
hprofFreeContext(hprof_context_t *ctx)
{
    assert(ctx != NULL);

    /* we don't own ctx->fd, do not close */

    if (ctx->memFp != NULL)
        fclose(ctx->memFp);
    free(ctx->curRec.body);
    free(ctx->fileName);
    free(ctx->fileDataPtr);
    free(ctx);
}
