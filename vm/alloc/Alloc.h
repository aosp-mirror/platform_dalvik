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
 * Garbage-collecting allocator.
 */
#ifndef _DALVIK_ALLOC_ALLOC
#define _DALVIK_ALLOC_ALLOC

#include <stddef.h>

/*
 * Initialization.
 */
bool dvmGcStartup(void);
bool dvmCreateStockExceptions(void);
bool dvmGcStartupAfterZygote(void);
void dvmGcShutdown(void);
void dvmGcThreadShutdown(void);

/*
 * Do any last-minute preparation before we call fork() for the first time.
 */
bool dvmGcPreZygoteFork(void);

/*
 * Basic allocation function.
 *
 * The new object will be added to the "tracked alloc" table unless
 * flags is ALLOC_DONT_TRACK.
 *
 * Returns NULL and throws an exception on failure.
 */
void* dvmMalloc(size_t size, int flags);

/*
 * Allocate a new object.
 *
 * The new object will be added to the "tracked alloc" table unless
 * flags is ALLOC_DONT_TRACK.
 *
 * Returns NULL and throws an exception on failure.
 */
Object* dvmAllocObject(ClassObject* clazz, int flags);

/* flags for dvmMalloc */
enum {
    ALLOC_DEFAULT       = 0x00,
    ALLOC_DONT_TRACK    = 0x01,     /* don't add to internal tracking list */
};

/*
 * Track an object reference that is currently only visible internally.
 * This is called automatically by dvmMalloc() unless ALLOC_DONT_TRACK
 * is set.
 *
 * The "self" argument is allowed as an optimization; it may be NULL.
 */
void dvmAddTrackedAlloc(Object* obj, Thread* self);

/*
 * Remove an object from the internal tracking list.
 *
 * Does nothing if "obj" is NULL.
 *
 * The "self" argument is allowed as an optimization; it may be NULL.
 */
void dvmReleaseTrackedAlloc(Object* obj, Thread* self);

/*
 * Returns true iff <obj> points to a valid allocated object.
 */
bool dvmIsValidObject(const Object* obj);

/*
 * Create a copy of an object.
 *
 * Returns NULL and throws an exception on failure.
 */
Object* dvmCloneObject(Object* obj, int flags);

/*
 * Make the object finalizable.
 */
void dvmSetFinalizable(Object* obj);

/*
 * Determine the exact number of GC heap bytes used by an object.  (Internal
 * to heap code except for debugging.)
 */
size_t dvmObjectSizeInHeap(const Object* obj);

/*
 * Gets the current ideal heap utilization, represented as a number
 * between zero and one.
 */
float dvmGetTargetHeapUtilization(void);

/*
 * Sets the new ideal heap utilization, represented as a number
 * between zero and one.
 */
void dvmSetTargetHeapUtilization(float newTarget);

/*
 * Initiate garbage collection.
 *
 * This usually happens automatically, but can also be caused by
 * Runtime.gc().
 */
void dvmCollectGarbage(void);

/*
 * Returns a count of the direct instances of a class.
 */
size_t dvmCountInstancesOfClass(const ClassObject *clazz);

/*
 * Returns a count of the instances of a class and its subclasses.
 */
size_t dvmCountAssignableInstancesOfClass(const ClassObject *clazz);

/*
 * Removes any growth limits from the heap.
 */
void dvmClearGrowthLimit(void);

#endif /*_DALVIK_ALLOC_ALLOC*/
