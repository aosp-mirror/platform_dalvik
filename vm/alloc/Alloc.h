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
    ALLOC_FINALIZABLE   = 0x02,     /* call finalize() before freeing */
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
 * The new object will be added to the "tracked alloc" table.
 */
Object* dvmCloneObject(Object* obj);

/*
 * Validate the object pointer.  Returns "false" and throws an exception if
 * "obj" is null or invalid.
 *
 * This may be used in performance critical areas as a null-pointer check;
 * anything else here should be for debug builds only.  In particular, for
 * "release" builds we want to skip the call to dvmIsValidObject() -- the
 * classfile validation will screen out code that puts invalid data into
 * object reference registers.
 */
INLINE int dvmValidateObject(Object* obj)
{
    if (obj == NULL) {
        dvmThrowException("Ljava/lang/NullPointerException;", NULL);
        return false;
    }
#ifdef WITH_EXTRA_OBJECT_VALIDATION
    if (!dvmIsValidObject(obj)) {
        dvmAbort();
        dvmThrowException("Ljava/lang/InternalError;",
            "VM detected invalid object ptr");
        return false;
    }
#endif
#ifndef NDEBUG
    /* check for heap corruption */
    if (obj->clazz == NULL || ((u4) obj->clazz) <= 65536) {
        dvmAbort();
        dvmThrowException("Ljava/lang/InternalError;",
            "VM detected invalid object class ptr");
        return false;
    }
#endif
    return true;
}

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
 * Runtime.gc().  If clearSoftReferences is true, the garbage
 * collector will not attempt to preserve any softly-reachable
 * SoftReference referents.
 */
void dvmCollectGarbage(bool clearSoftReferences);

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
