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

#include <cutils/array.h>
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

static void Dalvik_dalvik_system_VMRuntime_newNonMovableArray(const u4* args,
    JValue* pResult)
{
    ClassObject* elementClass = (ClassObject*) args[1];
    int length = args[2];
    ArrayObject* newArray;

    if (elementClass == NULL) {
        dvmThrowException("Ljava/lang/NullPointerException;", NULL);
        RETURN_VOID();
    }
    if (length < 0) {
        dvmThrowException("Ljava/lang/NegativeArraySizeException;", NULL);
        RETURN_VOID();
    }

    // TODO: right now, we don't have a copying collector, so there's no need
    // to do anything special here, but we ought to pass the non-movability
    // through to the allocator.
    newArray = dvmAllocObjectArray(elementClass, length, ALLOC_DEFAULT);
    if (newArray == NULL) {
        assert(dvmCheckException(dvmThreadSelf()));
        RETURN_VOID();
    }
    dvmReleaseTrackedAlloc((Object*) newArray, NULL);

    RETURN_PTR(newArray);
}

static void Dalvik_dalvik_system_VMRuntime_addressOf(const u4* args,
    JValue* pResult)
{
    ArrayObject* array = (ArrayObject*) args[1];
    if (!dvmIsArray(array)) {
        dvmThrowException("Ljava/lang/IllegalArgumentException;", NULL);
        RETURN_VOID();
    }
    // TODO: we should also check that this is a non-movable array.
    s8 result = (uintptr_t) array->contents;
    RETURN_LONG(result);
}

static void Dalvik_dalvik_system_VMRuntime_clearGrowthLimit(const u4* args,
    JValue* pResult)
{
    dvmClearGrowthLimit();
    RETURN_VOID();
}

static void Dalvik_dalvik_system_VMRuntime_properties(const u4* args,
    JValue* pResult)
{
    char** strings = (char**) arrayUnwrap(gDvm.properties);
    int count = arraySize(gDvm.properties);
    ArrayObject* result = dvmCreateStringArray(strings, count);
    dvmReleaseTrackedAlloc((Object*) result, dvmThreadSelf());
    RETURN_PTR(result);
}

static void returnCString(JValue* pResult, const char* s)
{
    Object* result = (Object*) dvmCreateStringFromCstr(s);
    dvmReleaseTrackedAlloc(result, dvmThreadSelf());
    RETURN_PTR(result);
}

static void Dalvik_dalvik_system_VMRuntime_bootClassPath(const u4* args,
    JValue* pResult)
{
    returnCString(pResult, gDvm.bootClassPathStr);
}

static void Dalvik_dalvik_system_VMRuntime_classPath(const u4* args,
    JValue* pResult)
{
    returnCString(pResult, gDvm.classPathStr);
}

static void Dalvik_dalvik_system_VMRuntime_vmVersion(const u4* args,
    JValue* pResult)
{
    char buf[64];
    sprintf(buf, "%d.%d.%d",
            DALVIK_MAJOR_VERSION, DALVIK_MINOR_VERSION, DALVIK_BUG_VERSION);
    returnCString(pResult, buf);
}

const DalvikNativeMethod dvm_dalvik_system_VMRuntime[] = {
    { "addressOf", "(Ljava/lang/Object;)J",
        Dalvik_dalvik_system_VMRuntime_addressOf },
    { "bootClassPath", "()Ljava/lang/String;",
        Dalvik_dalvik_system_VMRuntime_bootClassPath },
    { "classPath", "()Ljava/lang/String;",
        Dalvik_dalvik_system_VMRuntime_classPath },
    { "clearGrowthLimit", "()V",
        Dalvik_dalvik_system_VMRuntime_clearGrowthLimit },
    { "disableJitCompilation", "()V",
        Dalvik_dalvik_system_VMRuntime_disableJitCompilation },
    { "getTargetHeapUtilization", "()F",
        Dalvik_dalvik_system_VMRuntime_getTargetHeapUtilization },
    { "nativeSetTargetHeapUtilization", "(F)V",
        Dalvik_dalvik_system_VMRuntime_nativeSetTargetHeapUtilization },
    { "newNonMovableArray", "(Ljava/lang/Class;I)Ljava/lang/Object;",
        Dalvik_dalvik_system_VMRuntime_newNonMovableArray },
    { "properties", "()[Ljava/lang/String;",
        Dalvik_dalvik_system_VMRuntime_properties },
    { "runFinalizationSync", "()V",
        Dalvik_dalvik_system_VMRuntime_runFinalizationSync },
    { "startJitCompilation", "()V",
        Dalvik_dalvik_system_VMRuntime_startJitCompilation },
    { "vmVersion", "()Ljava/lang/String;",
        Dalvik_dalvik_system_VMRuntime_vmVersion },
    { NULL, NULL, NULL },
};
