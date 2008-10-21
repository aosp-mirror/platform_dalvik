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
#include <errno.h>
#include <sys/time.h>
#include <time.h>

#define TWO_FILES 1

hprof_context_t *
hprofStartup(const char *outputDir)
{
    hprof_context_t *ctx;

    ctx = malloc(sizeof(*ctx));
    if (ctx != NULL) {
        FILE *fp;
        struct timeval tv;

        /* Construct the output file name.
         */
        int len = strlen(outputDir);
        len += 64;  // hprofShutdown assumes that there's some slack
        gettimeofday(&tv, NULL);
        char *fileName = malloc(len);
        if (fileName == NULL) {
            LOGE("hprof: can't malloc %d bytes.\n", len);
            free(ctx);
            return NULL;
        }
        snprintf(fileName, len, "%s/heap-dump-tm%d-pid%d.hprof",
                outputDir, (int)tv.tv_sec, getpid());
        fileName[len-1] = '\0';

        fp = fopen(fileName, "w");
        if (fp == NULL) {
            LOGE("hprof: can't open %s: %s.\n", fileName, strerror(errno));
            free(ctx);
            return NULL;
        }
        LOGI("hprof: dumping VM heap to \"%s\".\n", fileName);

        hprofStartup_String();
        hprofStartup_Class();
#if WITH_HPROF_STACK
        hprofStartup_StackFrame();
        hprofStartup_Stack();
#endif
#if TWO_FILES
        hprofContextInit(ctx, fileName, fp, false);
#else
        hprofContextInit(ctx, fileName, fp, true);
#endif
    } else {
        LOGE("hprof: can't allocate context.\n");
    }

    return ctx;
}

void
hprofShutdown(hprof_context_t *ctx)
{
#if TWO_FILES
    FILE *fp;

    /* hprofStartup allocated some slack, so the strcat() should be ok.
     */
    char *fileName = strcat(ctx->fileName, "-head");

    hprofFlushCurrentRecord(ctx);
    fclose(ctx->fp);
    free(ctx->curRec.body);
    ctx->curRec.allocLen = 0;

    LOGI("hprof: dumping heap strings to \"%s\".\n", fileName);
    fp = fopen(fileName, "w");
    if (fp == NULL) {
        LOGE("can't open %s: %s\n", fileName, strerror(errno));
        free(ctx->fileName);
        free(ctx);
        return;
    }
    hprofContextInit(ctx, ctx->fileName, fp, true);
#endif

    hprofDumpStrings(ctx);
    hprofDumpClasses(ctx);

    /* Write a dummy stack trace record so the analysis
     * tools don't freak out.
     */
    hprofStartNewRecord(ctx, HPROF_TAG_STACK_TRACE, HPROF_TIME);
    hprofAddU4ToRecord(&ctx->curRec, HPROF_NULL_STACK_TRACE);
    hprofAddU4ToRecord(&ctx->curRec, HPROF_NULL_THREAD);
    hprofAddU4ToRecord(&ctx->curRec, 0);    // no frames

#if WITH_HPROF_STACK
    hprofDumpStackFrames(ctx);
    hprofDumpStacks(ctx);
#endif

    hprofFlushCurrentRecord(ctx);

    hprofShutdown_Class();
    hprofShutdown_String();
#if WITH_HPROF_STACK
    hprofShutdown_Stack();
    hprofShutdown_StackFrame();
#endif

    fclose(ctx->fp);
    free(ctx->fileName);
    free(ctx);
}
