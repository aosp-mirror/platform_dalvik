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
#ifndef _DALVIK_ALLOC_MARK_SWEEP
#define _DALVIK_ALLOC_MARK_SWEEP

#include "alloc/HeapBitmap.h"
#include "alloc/HeapSource.h"

/* Downward-growing stack for better cache read behavior.
 */
typedef struct {
    /* Lowest address (inclusive)
     */
    const Object **limit;

    /* Current top of the stack (inclusive)
     */
    const Object **top;

    /* Highest address (exclusive)
     */
    const Object **base;
} GcMarkStack;

/* This is declared publicly so that it can be included in gDvm.gcHeap.
 */
typedef struct {
    HeapBitmap *bitmap;
    GcMarkStack stack;
    const char *immuneLimit;
    const void *finger;   // only used while scanning/recursing.
} GcMarkContext;

bool dvmHeapBeginMarkStep(GcMode mode);
void dvmHeapMarkRootSet(void);
void dvmHeapReMarkRootSet(void);
void dvmHeapScanMarkedObjects(void);
void dvmHeapReScanMarkedObjects(void);
void dvmHandleSoftRefs(Object **list);
void dvmClearWhiteRefs(Object **list);
void dvmHeapScheduleFinalizations(void);
void dvmHeapFinishMarkStep(void);
void dvmHeapSweepSystemWeaks(void);
void dvmHeapSweepUnmarkedObjects(GcMode mode, bool isConcurrent,
                                 size_t *numObjects, size_t *numBytes);

#endif  // _DALVIK_ALLOC_MARK_SWEEP
