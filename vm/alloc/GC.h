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
 * Garbage collector
 */
#ifndef _DALVIK_ALLOC_GC
#define _DALVIK_ALLOC_GC

/*
 * Initiate garbage collection.
 *
 * This usually happens automatically, but can also be caused by Runtime.gc().
 */
void dvmCollectGarbage(bool collectSoftRefs);

/****
 **** NOTE: The functions after this point will (should) only be called
 ****       during GC.
 ****/

/*
 * Functions that mark an object.
 *
 * Currently implemented in Heap.c.
 */

/*
 * Remove any unmarked interned string objects from the table.
 *
 * Currently implemented in Intern.c.
 */
void dvmGcDetachDeadInternedStrings(int (*isUnmarkedObject)(void *));

#endif  // _DALVIK_ALLOC_GC
