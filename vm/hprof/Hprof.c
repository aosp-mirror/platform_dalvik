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
#include <errno.h>
#include <sys/time.h>
#include <time.h>


#define kHeadSuffix "-hptemp"

hprof_context_t *
hprofStartup(const char *outputFileName, bool directToDdms)
{
    FILE* fp = NULL;

    if (!directToDdms) {
        int len = strlen(outputFileName);
        char fileName[len + sizeof(kHeadSuffix)];

        /* Construct the temp file name.  This wasn't handed to us by the
         * application, so we need to be careful about stomping on it.
         */
        sprintf(fileName, "%s" kHeadSuffix, outputFileName);
        if (access(fileName, F_OK) == 0) {
            LOGE("hprof: temp file %s exists, bailing\n", fileName);
            return NULL;
        }

        fp = fopen(fileName, "w+");
        if (fp == NULL) {
            LOGE("hprof: can't open %s: %s.\n", fileName, strerror(errno));
            return NULL;
        }
        if (unlink(fileName) != 0) {
            LOGW("hprof: WARNING: unable to remove temp file %s\n", fileName);
            /* keep going */
        }
        LOGI("hprof: dumping VM heap to \"%s\".\n", fileName);
    }

    hprofStartup_String();
    hprofStartup_Class();
#if WITH_HPROF_STACK
    hprofStartup_StackFrame();
    hprofStartup_Stack();
#endif

    hprof_context_t *ctx = malloc(sizeof(*ctx));
    if (ctx == NULL) {
        LOGE("hprof: can't allocate context.\n");
        if (fp != NULL)
            fclose(fp);
        return NULL;
    }

    /* pass in "fp" for the temp file, and the name of the output file */
    hprofContextInit(ctx, strdup(outputFileName), fp, false, directToDdms);

    assert(ctx->fp != NULL);

    return ctx;
}

/*
 * Copy the entire contents of "srcFp" to "dstFp".
 *
 * Returns "true" on success.
 */
static bool
copyFileToFile(FILE *dstFp, FILE *srcFp)
{
    char buf[65536];
    size_t dataRead, dataWritten;

    while (true) {
        dataRead = fread(buf, 1, sizeof(buf), srcFp);
        if (dataRead > 0) {
            dataWritten = fwrite(buf, 1, dataRead, dstFp);
            if (dataWritten != dataRead) {
                LOGE("hprof: failed writing data (%d of %d): %s\n",
                    dataWritten, dataRead, strerror(errno));
                return false;
            }
        } else {
            if (feof(srcFp))
                return true;
            LOGE("hprof: failed reading data (res=%d): %s\n",
                dataRead, strerror(errno));
            return false;
        }
    }
}

/*
 * Finish up the hprof dump.  Returns true on success.
 */
bool
hprofShutdown(hprof_context_t *tailCtx)
{
    FILE *fp = NULL;

    /* flush output to the temp file, then prepare the output file */
    hprofFlushCurrentRecord(tailCtx);

    LOGI("hprof: dumping heap strings to \"%s\".\n", tailCtx->fileName);
    if (!tailCtx->directToDdms) {
        fp = fopen(tailCtx->fileName, "w");
        if (fp == NULL) {
            LOGE("can't open %s: %s\n", tailCtx->fileName, strerror(errno));
            hprofFreeContext(tailCtx);
            return false;
        }
    }

    /*
     * Create a new context struct for the start of the file.  We
     * heap-allocate it so we can share the "free" function.
     */
    hprof_context_t *headCtx = malloc(sizeof(*headCtx));
    if (headCtx == NULL) {
        LOGE("hprof: can't allocate context.\n");
        if (fp != NULL)
            fclose(fp);
        hprofFreeContext(tailCtx);
        return NULL;
    }
    hprofContextInit(headCtx, strdup(tailCtx->fileName), fp, true,
        tailCtx->directToDdms);

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

    if (tailCtx->directToDdms) {
        /* flush to ensure memstream pointer and size are updated */
        fflush(headCtx->fp);
        fflush(tailCtx->fp);

        /* send the data off to DDMS */
        struct iovec iov[2];
        iov[0].iov_base = headCtx->fileDataPtr;
        iov[0].iov_len = headCtx->fileDataSize;
        iov[1].iov_base = tailCtx->fileDataPtr;
        iov[1].iov_len = tailCtx->fileDataSize;
        dvmDbgDdmSendChunkV(CHUNK_TYPE("HPDS"), iov, 2);
    } else {
        /*
         * Append the contents of the temp file to the output file.  The temp
         * file was removed immediately after being opened, so it will vanish
         * when we close it.
         */
        rewind(tailCtx->fp);
        if (!copyFileToFile(headCtx->fp, tailCtx->fp)) {
            LOGW("hprof: file copy failed, hprof data may be incomplete\n");
            /* finish up anyway */
        }
    }

    hprofFreeContext(headCtx);
    hprofFreeContext(tailCtx);

    /* throw out a log message for the benefit of "runhat" */
    LOGI("hprof: heap dump completed, temp file removed\n");
    return true;
}

/*
 * Free any heap-allocated items in "ctx", and then free "ctx" itself.
 */
void
hprofFreeContext(hprof_context_t *ctx)
{
    assert(ctx != NULL);

    if (ctx->fp != NULL)
        fclose(ctx->fp);
    free(ctx->curRec.body);
    free(ctx->fileName);
    free(ctx->fileDataPtr);
    free(ctx);
}

