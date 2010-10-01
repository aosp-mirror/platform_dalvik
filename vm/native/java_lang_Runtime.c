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
 * java.lang.Runtime
 */
#include "Dalvik.h"
#include "native/InternalNativePriv.h"
#include <unistd.h>
#include <limits.h>

/*
 * public void gc()
 *
 * Initiate a gc.
 */
static void Dalvik_java_lang_Runtime_gc(const u4* args, JValue* pResult)
{
    UNUSED_PARAMETER(args);

    dvmCollectGarbage(false);
    RETURN_VOID();
}

/*
 * private static void nativeExit(int code, boolean isExit)
 *
 * Runtime.exit() calls this after doing shutdown processing.  Runtime.halt()
 * uses this as well.
 */
static void Dalvik_java_lang_Runtime_nativeExit(const u4* args,
    JValue* pResult)
{
    int status = args[0];
    bool isExit = (args[1] != 0);

    if (isExit && gDvm.exitHook != NULL) {
        dvmChangeStatus(NULL, THREAD_NATIVE);
        (*gDvm.exitHook)(status);     // not expected to return
        dvmChangeStatus(NULL, THREAD_RUNNING);
        LOGW("JNI exit hook returned\n");
    }
    LOGD("Calling exit(%d)\n", status);
#if defined(WITH_JIT) && defined(WITH_JIT_TUNING)
    dvmCompilerDumpStats();
#endif
    exit(status);
}

/*
 * static String nativeLoad(String filename, ClassLoader loader)
 *
 * Load the specified full path as a dynamic library filled with
 * JNI-compatible methods. Returns null on success, or a failure
 * message on failure.
 */
static void Dalvik_java_lang_Runtime_nativeLoad(const u4* args,
    JValue* pResult)
{
    StringObject* fileNameObj = (StringObject*) args[0];
    Object* classLoader = (Object*) args[1];
    char* fileName = NULL;
    StringObject* result = NULL;
    char* reason = NULL;
    bool success;

    assert(fileNameObj != NULL);
    fileName = dvmCreateCstrFromString(fileNameObj);

    success = dvmLoadNativeCode(fileName, classLoader, &reason);
    if (!success) {
        const char* msg = (reason != NULL) ? reason : "unknown failure";
        result = dvmCreateStringFromCstr(msg);
        dvmReleaseTrackedAlloc((Object*) result, NULL);
    }

    free(reason);
    free(fileName);
    RETURN_PTR(result);
}

/*
 * public void runFinalization(boolean forced)
 *
 * Requests that the VM runs finalizers for objects on the heap. If the
 * parameter forced is true, then the VM needs to ensure finalization.
 * Otherwise this only inspires the VM to make a best-effort attempt to
 * run finalizers before returning, but it's not guaranteed to actually
 * do anything.
 */
static void Dalvik_java_lang_Runtime_runFinalization(const u4* args,
    JValue* pResult)
{
    bool forced = (args[0] != 0);

    dvmWaitForHeapWorkerIdle();
    if (forced) {
        // TODO(Google) Need to explicitly implement this,
        //              although dvmWaitForHeapWorkerIdle()
        //              should usually provide the "forced"
        //              behavior already.
    }

    RETURN_VOID();
}

/*
 * public int availableProcessors()
 *
 * Returns the number of online processors, at least one.
 *
 */
static void Dalvik_java_lang_Runtime_availableProcessors(const u4* args,
    JValue* pResult)
{
    long result = 1;
#ifdef _SC_NPROCESSORS_ONLN
    result = sysconf(_SC_NPROCESSORS_ONLN);
    if (result > INT_MAX) {
        result = INT_MAX;
    } else if (result < 1 ) {
        result = 1;
    }
#endif
    RETURN_INT((int)result);
}
/*
 * public void maxMemory()
 *
 * Returns GC heap max memory in bytes.
 */
static void Dalvik_java_lang_Runtime_maxMemory(const u4* args, JValue* pResult)
{
    unsigned int result = gDvm.heapSizeMax;
    RETURN_LONG(result);
}

/*
 * public void totalMemory()
 *
 * Returns GC heap total memory in bytes.
 */
static void Dalvik_java_lang_Runtime_totalMemory(const u4* args,
    JValue* pResult)
{
    int result = dvmGetHeapDebugInfo(kVirtualHeapSize);
    RETURN_LONG(result);
}

/*
 * public void freeMemory()
 *
 * Returns GC heap free memory in bytes.
 */
static void Dalvik_java_lang_Runtime_freeMemory(const u4* args,
    JValue* pResult)
{
    int result = dvmGetHeapDebugInfo(kVirtualHeapSize)
                 - dvmGetHeapDebugInfo(kVirtualHeapAllocated);
    if (result < 0) {
        result = 0;
    }
    RETURN_LONG(result);
}

const DalvikNativeMethod dvm_java_lang_Runtime[] = {
    { "freeMemory",          "()J",
        Dalvik_java_lang_Runtime_freeMemory },
    { "gc",                 "()V",
        Dalvik_java_lang_Runtime_gc },
    { "availableProcessors", "()I",
        Dalvik_java_lang_Runtime_availableProcessors },
    { "maxMemory",          "()J",
        Dalvik_java_lang_Runtime_maxMemory },
    { "nativeExit",         "(IZ)V",
        Dalvik_java_lang_Runtime_nativeExit },
    { "nativeLoad",         "(Ljava/lang/String;Ljava/lang/ClassLoader;)Ljava/lang/String;",
        Dalvik_java_lang_Runtime_nativeLoad },
    { "runFinalization",    "(Z)V",
        Dalvik_java_lang_Runtime_runFinalization },
    { "totalMemory",          "()J",
        Dalvik_java_lang_Runtime_totalMemory },
    { NULL, NULL, NULL },
};
