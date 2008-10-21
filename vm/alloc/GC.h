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
 * Mark an object and schedule it to be scanned for
 * references to other objects.
 *
 * @param obj must be a valid object
 */
void dvmMarkObjectNonNull(const Object *obj);

/*
 * Mark an object and schedule it to be scanned for
 * references to other objects.
 *
 * @param obj must be a valid object or NULL
 */
#define dvmMarkObject(obj) \
    do { \
        Object *DMO_obj_ = (Object *)(obj); \
        if (DMO_obj_ != NULL) { \
            dvmMarkObjectNonNull(DMO_obj_); \
        } \
    } while (false)

/*
 * If obj points to a valid object, mark it and
 * schedule it to be scanned for references to other
 * objects.
 *
 * @param obj any pointer that may be an Object, or NULL
TODO: check for alignment, too (would require knowledge of heap chunks)
 */
#define dvmMarkIfObject(obj) \
    do { \
        Object *DMIO_obj_ = (Object *)(obj); \
        if (DMIO_obj_ != NULL && dvmIsValidObject(DMIO_obj_)) { \
            dvmMarkObjectNonNull(DMIO_obj_); \
        } \
    } while (false)

/*
 * Functions that handle scanning various objects for references.
 */

/*
 * Mark all class objects loaded by the root class loader;
 * most of these are the java.* classes.
 *
 * Currently implemented in Class.c.
 */
void dvmGcScanRootClassLoader(void);

/*
 * Mark all root ThreadGroup objects, guaranteeing that
 * all live Thread objects will eventually be scanned.
 *
 * NOTE: this is a misnomer, because the current implementation
 * actually only scans the internal list of VM threads, which
 * will mark all VM-reachable Thread objects.  Someone else
 * must scan the root class loader, which will mark java/lang/ThreadGroup.
 * The ThreadGroup class object has static members pointing to
 * the root ThreadGroups, and these will be marked as a side-effect
 * of marking the class object.
 *
 * Currently implemented in Thread.c.
 */
void dvmGcScanRootThreadGroups(void);

/*
 * Mark all interned string objects.
 *
 * Currently implemented in Intern.c.
 */
void dvmGcScanInternedStrings(void);

/*
 * Remove any unmarked interned string objects from the table.
 *
 * Currently implemented in Intern.c.
 */
void dvmGcDetachDeadInternedStrings(int (*isUnmarkedObject)(void *));

/*
 * Mark all primitive class objects.
 *
 * Currently implemented in Array.c.
 */
void dvmGcScanPrimitiveClasses(void);

/*
 * Mark all JNI global references.
 *
 * Currently implemented in JNI.c.
 */
void dvmGcMarkJniGlobalRefs(void);

/*
 * Mark all debugger references.
 *
 * Currently implemented in Debugger.c.
 */
void dvmGcMarkDebuggerRefs(void);

/*
 * Optional heap profiling.
 */
#if WITH_HPROF && !defined(_DALVIK_HPROF_HPROF)
#include "hprof/Hprof.h"
#define HPROF_SET_GC_SCAN_STATE(tag_, thread_) \
    dvmHeapSetHprofGcScanState((tag_), (thread_))
#define HPROF_CLEAR_GC_SCAN_STATE() \
    dvmHeapSetHprofGcScanState(0, 0)
#else
#define HPROF_SET_GC_SCAN_STATE(tag_, thread_)  do {} while (false)
#define HPROF_CLEAR_GC_SCAN_STATE()  do {} while (false)
#endif

#endif  // _DALVIK_ALLOC_GC
