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
#ifndef _DALVIK_HPROF_STACK
#define _DALVIK_HPROF_STACK

#include "../alloc/HeapInternal.h"

typedef struct {
    const Method *method;
    int pc;
} StackFrame;

typedef struct {
    StackFrame frame;
    unsigned char live;
} StackFrameEntry;

int hprofStartupStack();
int hprofShutdown_Stack();
int hprofDumpStacks(hprof_context_t *ctx);
void hprofFillInStackTrace(void *objectPtr);

int hprofStartup_StackFrame();
int hprofShutdown_StackFrame();
hprof_stack_frame_id hprofLookupStackFrameId(const StackFrameEntry
    *stackFrameEntry);
int hprofDumpStackFrames(hprof_context_t *ctx);

#endif /* _DALVIK_HPROF_STACK */
