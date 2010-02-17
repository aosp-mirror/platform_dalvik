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
 * dalvik.system.VMRuntime
 */
#include "Dalvik.h"
#include "native/InternalNativePriv.h"

#include <limits.h>


/*
 * public native float getTargetHeapUtilization()
 *
 * Gets the current ideal heap utilization, represented as a number
 * between zero and one.
 */
static void Dalvik_dalvik_system_VMRuntime_getTargetHeapUtilization(
    const u4* args, JValue* pResult)
{
    UNUSED_PARAMETER(args);

    RETURN_FLOAT(dvmGetTargetHeapUtilization());
}

/*
 * native float nativeSetTargetHeapUtilization()
 *
 * Sets the current ideal heap utilization, represented as a number
 * between zero and one.  Returns the old utilization.
 *
 * Note that this is NOT static.
 */
static void Dalvik_dalvik_system_VMRuntime_nativeSetTargetHeapUtilization(
    const u4* args, JValue* pResult)
{
    dvmSetTargetHeapUtilization(dvmU4ToFloat(args[1]));

    RETURN_VOID();
}

/*
 * native long nativeMinimumHeapSize(long size, boolean set)
 *
 * If set is true, sets the new minimum heap size to size; always
 * returns the current (or previous) size.  If size is negative or
 * zero, removes the current minimum constraint (if present).
 */
static void Dalvik_dalvik_system_VMRuntime_nativeMinimumHeapSize(
    const u4* args, JValue* pResult)
{
    s8 longSize = GET_ARG_LONG(args, 1);
    size_t size;
    bool set = (args[3] != 0);

    /* Fit in 32 bits. */
    if (longSize < 0) {
        size = 0;
    } else if (longSize > INT_MAX) {
        size = INT_MAX;
    } else {
        size = (size_t)longSize;
    }

    size = dvmMinimumHeapSize(size, set);

    RETURN_LONG(size);
}

/*
 * public native void gcSoftReferences()
 *
 * Does a GC and forces collection of SoftReferences that are
 * not strongly-reachable.
 */
static void Dalvik_dalvik_system_VMRuntime_gcSoftReferences(const u4* args,
    JValue* pResult)
{
    dvmCollectGarbage(true);

    RETURN_VOID();
}

/*
 * public native void runFinalizationSync()
 *
 * Does not return until any pending finalizers have been called.
 * This may or may not happen in the context of the calling thread.
 * No exceptions will escape.
 *
 * Used by zygote, which doesn't have a HeapWorker thread.
 */
static void Dalvik_dalvik_system_VMRuntime_runFinalizationSync(const u4* args,
    JValue* pResult)
{
    dvmRunFinalizationSync();

    RETURN_VOID();
}

/*
 * public native boolean trackExternalAllocation(long size)
 *
 * Asks the VM if <size> bytes can be allocated in an external heap.
 * This information may be used to limit the amount of memory available
 * to Dalvik threads.  Returns false if the VM would rather that the caller
 * did not allocate that much memory.  If the call returns false, the VM
 * will not update its internal counts.
 */
static void Dalvik_dalvik_system_VMRuntime_trackExternalAllocation(
    const u4* args, JValue* pResult)
{
    s8 longSize = GET_ARG_LONG(args, 1);

    /* Fit in 32 bits. */
    if (longSize < 0) {
        dvmThrowException("Ljava/lang/IllegalArgumentException;",
            "size must be positive");
        RETURN_VOID();
    } else if (longSize > INT_MAX) {
        dvmThrowException("Ljava/lang/UnsupportedOperationException;",
            "size must fit in 32 bits");
        RETURN_VOID();
    }
    RETURN_BOOLEAN(dvmTrackExternalAllocation((size_t)longSize));
}

/*
 * public native void trackExternalFree(long size)
 *
 * Tells the VM that <size> bytes have been freed in an external
 * heap.  This information may be used to control the amount of memory
 * available to Dalvik threads.
 */
static void Dalvik_dalvik_system_VMRuntime_trackExternalFree(
    const u4* args, JValue* pResult)
{
    s8 longSize = GET_ARG_LONG(args, 1);

    /* Fit in 32 bits. */
    if (longSize < 0) {
        dvmThrowException("Ljava/lang/IllegalArgumentException;",
            "size must be positive");
        RETURN_VOID();
    } else if (longSize > INT_MAX) {
        dvmThrowException("Ljava/lang/UnsupportedOperationException;",
            "size must fit in 32 bits");
        RETURN_VOID();
    }
    dvmTrackExternalFree((size_t)longSize);

    RETURN_VOID();
}

/*
 * public native long getExternalBytesAllocated()
 *
 * Returns the number of externally-allocated bytes being tracked by
 * trackExternalAllocation/Free().
 */
static void Dalvik_dalvik_system_VMRuntime_getExternalBytesAllocated(
    const u4* args, JValue* pResult)
{
    RETURN_LONG((s8)dvmGetExternalBytesAllocated());
}

/*
 * public native void startJitCompilation()
 *
 * Callback function from the framework to indicate that an app has gone
 * through the startup phase and it is time to enable the JIT compiler.
 */
static void Dalvik_dalvik_system_VMRuntime_startJitCompilation(const u4* args,
    JValue* pResult)
{
#if defined(WITH_JIT)
    if (gDvm.executionMode == kExecutionModeJit &&
        gDvmJit.disableJit == false) {
        dvmLockMutex(&gDvmJit.compilerLock);
        gDvmJit.alreadyEnabledViaFramework = true;
        pthread_cond_signal(&gDvmJit.compilerQueueActivity);
        dvmUnlockMutex(&gDvmJit.compilerLock);
    }
#endif
    RETURN_VOID();
}

/*
 * public native void disableJitCompilation()
 *
 * Callback function from the framework to indicate that a VM instance wants to
 * permanently disable the JIT compiler. Currently only the system server uses
 * this interface when it detects system-wide safe mode is enabled.
 */
static void Dalvik_dalvik_system_VMRuntime_disableJitCompilation(const u4* args,
    JValue* pResult)
{
#if defined(WITH_JIT)
    if (gDvm.executionMode == kExecutionModeJit) {
        gDvmJit.disableJit = true;
    }
#endif
    RETURN_VOID();
}

const DalvikNativeMethod dvm_dalvik_system_VMRuntime[] = {
    { "getTargetHeapUtilization", "()F",
        Dalvik_dalvik_system_VMRuntime_getTargetHeapUtilization },
    { "nativeSetTargetHeapUtilization", "(F)V",
        Dalvik_dalvik_system_VMRuntime_nativeSetTargetHeapUtilization },
    { "nativeMinimumHeapSize", "(JZ)J",
        Dalvik_dalvik_system_VMRuntime_nativeMinimumHeapSize },
    { "gcSoftReferences", "()V",
        Dalvik_dalvik_system_VMRuntime_gcSoftReferences },
    { "runFinalizationSync", "()V",
        Dalvik_dalvik_system_VMRuntime_runFinalizationSync },
    { "trackExternalAllocation", "(J)Z",
        Dalvik_dalvik_system_VMRuntime_trackExternalAllocation },
    { "trackExternalFree", "(J)V",
        Dalvik_dalvik_system_VMRuntime_trackExternalFree },
    { "getExternalBytesAllocated", "()J",
        Dalvik_dalvik_system_VMRuntime_getExternalBytesAllocated },
    { "startJitCompilation", "()V",
        Dalvik_dalvik_system_VMRuntime_startJitCompilation },
    { "disableJitCompilation", "()V",
        Dalvik_dalvik_system_VMRuntime_disableJitCompilation },
    { NULL, NULL, NULL },
};
