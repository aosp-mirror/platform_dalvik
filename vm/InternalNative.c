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
 * Internal native functions.  All of the functions defined here make
 * direct use of VM functions or data structures, so they can't be written
 * with JNI and shouldn't really be in a shared library.
 *
 * All functions here either complete quickly or are used to enter a wait
 * state, so we don't set the thread status to THREAD_NATIVE when executing
 * these methods.  This means that the GC will wait for these functions
 * to finish.  DO NOT perform long operations or blocking I/O in here.
 * These methods may not be declared "synchronized".
 *
 * We use "late" binding on these, rather than explicit registration,
 * because it's easier to handle the core system classes that way.
 *
 * The functions here use the DalvikNativeFunc prototype, but we can also
 * treat them as DalvikBridgeFunc, which takes two extra arguments.  The
 * former represents the API that we're most likely to expose should JNI
 * performance be deemed insufficient.  The Bridge version is used as an
 * optimization for a few high-volume Object calls, and should generally
 * not be used as we may drop support for it at some point.
 *
 * TODO: this is huge.  Consider splitting this into one file per class.
 */
#include "Dalvik.h"

#include <stdlib.h>
#include <stddef.h>
#include <sys/time.h>
#include <errno.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/wait.h>
#include <grp.h>
#include <limits.h>

#if defined(HAVE_PRCTL)
#include <sys/prctl.h>
#endif

#include "alloc/HeapDebug.h"

/*
 * Return macros.  Note we use "->i" instead of "->z" for boolean; this
 * is because the interpreter expects everything to be a 32-bit value.
 */
#ifdef NDEBUG
# define RETURN_VOID()           do { (void)(pResult); return; } while(0)
#else
# define RETURN_VOID()           do { pResult->i = 0xfefeabab; return; }while(0)
#endif
#define RETURN_BOOLEAN(_val)    do { pResult->i = (_val); return; } while(0)
#define RETURN_INT(_val)        do { pResult->i = (_val); return; } while(0)
#define RETURN_LONG(_val)       do { pResult->j = (_val); return; } while(0)
#define RETURN_FLOAT(_val)      do { pResult->f = (_val); return; } while(0)
#define RETURN_DOUBLE(_val)     do { pResult->d = (_val); return; } while(0)
#define RETURN_PTR(_val)        do { pResult->l = (_val); return; } while(0)


/*
 * Verify that "obj" is non-null and is an instance of "clazz".
 *
 * Returns "false" and throws an exception if not.
 */
static bool verifyObjectInClass(Object* obj, ClassObject* clazz)
{
    if (obj == NULL) {
        dvmThrowException("Ljava/lang/NullPointerException;", NULL);
        return false;
    }
    if (!dvmInstanceof(obj->clazz, clazz)) {
        dvmThrowException("Ljava/lang/IllegalArgumentException;",
            "object is not an instance of the class");
        return false;
    }

    return true;
}

/*
 * Validate a "fully qualified" class name, e.g. "Ljava/lang/String;" or "[I".
 */
static bool validateClassName(const char* name)
{
    int len = strlen(name);
    int i = 0;

    /* check for reasonable array types */
    if (name[0] == '[') {
        while (name[i] == '[')
            i++;

        if (name[i] == 'L') {
            /* array of objects, make sure it ends well */
            if (name[len-1] != ';')
                return false;
        } else if (strchr(PRIM_TYPE_TO_LETTER, name[i]) != NULL) {
            if (i != len-1)
                return false;
        } else {
            return false;
        }
    }

    /* quick check for illegal chars */
    for ( ; i < len; i++) {
        if (name[i] == '/')
            return false;
    }

    return true;
}

/*
 * Find a class by name, initializing it if requested.
 */
static ClassObject* findClassByName(StringObject* nameObj, Object* loader,
    bool doInit)
{
    ClassObject* clazz = NULL;
    char* name = NULL;
    char* descriptor = NULL;

    if (nameObj == NULL) {
        dvmThrowException("Ljava/lang/NullPointerException;", NULL);
        goto bail;
    }
    name = dvmCreateCstrFromString(nameObj);

    /*
     * We need to validate and convert the name (from x.y.z to x/y/z).  This
     * is especially handy for array types, since we want to avoid
     * auto-generating bogus array classes.
     */
    if (!validateClassName(name)) {
        LOGW("findClassByName rejecting '%s'\n", name);
        goto bail;
    }

    descriptor = dvmDotToDescriptor(name);
    if (descriptor == NULL) {
        goto bail;
    }

    if (doInit)
        clazz = dvmFindClass(descriptor, loader);
    else
        clazz = dvmFindClassNoInit(descriptor, loader);

    if (clazz == NULL) {
        LOGVV("FAIL: load %s (%d)\n", descriptor, doInit);
        Thread* self = dvmThreadSelf();
        Object* oldExcep = dvmGetException(self);
        dvmAddTrackedAlloc(oldExcep, self);     /* don't let this be GCed */
        dvmClearException(self);
        dvmThrowChainedException("Ljava/lang/ClassNotFoundException;",
            name, oldExcep);
        dvmReleaseTrackedAlloc(oldExcep, self);
    } else {
        LOGVV("GOOD: load %s (%d) --> %p ldr=%p\n",
            descriptor, doInit, clazz, clazz->classLoader);
    }

bail:
    free(name);
    free(descriptor);
    return clazz;
}

/*
 * We insert native method stubs for abstract methods so we don't have to
 * check the access flags at the time of the method call.  This results in
 * "native abstract" methods, which can't exist.  If we see the "abstract"
 * flag set, clear the "native" flag.
 * 
 * We also move the DECLARED_SYNCHRONIZED flag into the SYNCHRONIZED
 * position, because the callers of this function are trying to convey
 * the "traditional" meaning of the flags to their callers.
 */
static inline u4 fixMethodFlags(u4 flags)
{
    if ((flags & ACC_ABSTRACT) != 0) {
        flags &= ~ACC_NATIVE;
    }

    flags &= ~ACC_SYNCHRONIZED;
    
    if ((flags & ACC_DECLARED_SYNCHRONIZED) != 0) {
        flags |= ACC_SYNCHRONIZED;
    }
    
    return flags & JAVA_FLAGS_MASK;
}


/*
 * ===========================================================================
 *      dalvik.system.VMDebug
 * ===========================================================================
 */

#ifdef WITH_PROFILER
/* These must match the values in dalvik.system.VMDebug.
 */
enum {
    KIND_ALLOCATED_OBJECTS = 1<<0,
    KIND_ALLOCATED_BYTES   = 1<<1,
    KIND_FREED_OBJECTS     = 1<<2,
    KIND_FREED_BYTES       = 1<<3,
    KIND_GC_INVOCATIONS    = 1<<4,
#if PROFILE_EXTERNAL_ALLOCATIONS
    KIND_EXT_ALLOCATED_OBJECTS = 1<<12,
    KIND_EXT_ALLOCATED_BYTES   = 1<<13,
    KIND_EXT_FREED_OBJECTS     = 1<<14,
    KIND_EXT_FREED_BYTES       = 1<<15,
#endif // PROFILE_EXTERNAL_ALLOCATIONS

    KIND_GLOBAL_ALLOCATED_OBJECTS   = KIND_ALLOCATED_OBJECTS,
    KIND_GLOBAL_ALLOCATED_BYTES     = KIND_ALLOCATED_BYTES,
    KIND_GLOBAL_FREED_OBJECTS       = KIND_FREED_OBJECTS,
    KIND_GLOBAL_FREED_BYTES         = KIND_FREED_BYTES,
    KIND_GLOBAL_GC_INVOCATIONS      = KIND_GC_INVOCATIONS,
#if PROFILE_EXTERNAL_ALLOCATIONS
    KIND_GLOBAL_EXT_ALLOCATED_OBJECTS = KIND_EXT_ALLOCATED_OBJECTS,
    KIND_GLOBAL_EXT_ALLOCATED_BYTES = KIND_EXT_ALLOCATED_BYTES,
    KIND_GLOBAL_EXT_FREED_OBJECTS   = KIND_EXT_FREED_OBJECTS,
    KIND_GLOBAL_EXT_FREED_BYTES     = KIND_EXT_FREED_BYTES,
#endif // PROFILE_EXTERNAL_ALLOCATIONS

    KIND_THREAD_ALLOCATED_OBJECTS   = KIND_ALLOCATED_OBJECTS << 16,
    KIND_THREAD_ALLOCATED_BYTES     = KIND_ALLOCATED_BYTES << 16,
    KIND_THREAD_FREED_OBJECTS       = KIND_FREED_OBJECTS << 16,
    KIND_THREAD_FREED_BYTES         = KIND_FREED_BYTES << 16,
#if PROFILE_EXTERNAL_ALLOCATIONS
    KIND_THREAD_EXT_ALLOCATED_OBJECTS = KIND_EXT_ALLOCATED_OBJECTS << 16,
    KIND_THREAD_EXT_ALLOCATED_BYTES = KIND_EXT_ALLOCATED_BYTES << 16,
    KIND_THREAD_EXT_FREED_OBJECTS   = KIND_EXT_FREED_OBJECTS << 16,
    KIND_THREAD_EXT_FREED_BYTES     = KIND_EXT_FREED_BYTES << 16,
#endif // PROFILE_EXTERNAL_ALLOCATIONS
    KIND_THREAD_GC_INVOCATIONS      = KIND_GC_INVOCATIONS << 16,

    // TODO: failedAllocCount, failedAllocSize
};

#define KIND_ALL_COUNTS 0xffffffff

/*
 * Zero out the specified fields.
 */
static void clearAllocProfStateFields(AllocProfState *allocProf,
    unsigned int kinds)
{
    if (kinds & KIND_ALLOCATED_OBJECTS) {
        allocProf->allocCount = 0;
    }
    if (kinds & KIND_ALLOCATED_BYTES) {
        allocProf->allocSize = 0;
    }
    if (kinds & KIND_FREED_OBJECTS) {
        allocProf->freeCount = 0;
    }
    if (kinds & KIND_FREED_BYTES) {
        allocProf->freeSize = 0;
    }
    if (kinds & KIND_GC_INVOCATIONS) {
        allocProf->gcCount = 0;
    }
#if PROFILE_EXTERNAL_ALLOCATIONS
    if (kinds & KIND_EXT_ALLOCATED_OBJECTS) {
        allocProf->externalAllocCount = 0;
    }
    if (kinds & KIND_EXT_ALLOCATED_BYTES) {
        allocProf->externalAllocSize = 0;
    }
    if (kinds & KIND_EXT_FREED_OBJECTS) {
        allocProf->externalFreeCount = 0;
    }
    if (kinds & KIND_EXT_FREED_BYTES) {
        allocProf->externalFreeSize = 0;
    }
#endif // PROFILE_EXTERNAL_ALLOCATIONS
}
#endif

/*
 * static void startAllocCounting()
 *
 * Reset the counters and enable counting.
 *
 * TODO: this currently only resets the per-thread counters for the current
 * thread.  If we actually start using the per-thread counters we'll
 * probably want to fix this.
 */
static void Dalvik_dalvik_system_VMDebug_startAllocCounting(const u4* args,
    JValue* pResult)
{
    UNUSED_PARAMETER(args);

#ifdef WITH_PROFILER
    clearAllocProfStateFields(&gDvm.allocProf, KIND_ALL_COUNTS);
    clearAllocProfStateFields(&dvmThreadSelf()->allocProf, KIND_ALL_COUNTS);
    dvmStartAllocCounting();
#endif
    RETURN_VOID();
}

/*
 * public static void stopAllocCounting()
 */
static void Dalvik_dalvik_system_VMDebug_stopAllocCounting(const u4* args,
    JValue* pResult)
{
    UNUSED_PARAMETER(args);

#ifdef WITH_PROFILER
    dvmStopAllocCounting();
#endif
    RETURN_VOID();
}

/*
 * private static int getAllocCount(int kind)
 */
static void Dalvik_dalvik_system_VMDebug_getAllocCount(const u4* args,
    JValue* pResult)
{
#ifdef WITH_PROFILER
    AllocProfState *allocProf;
    unsigned int kind = args[0];
    if (kind < (1<<16)) {
        allocProf = &gDvm.allocProf;
    } else {
        allocProf = &dvmThreadSelf()->allocProf;
        kind >>= 16;
    }
    switch (kind) {
    case KIND_ALLOCATED_OBJECTS:
        pResult->i = allocProf->allocCount;
        break;
    case KIND_ALLOCATED_BYTES:
        pResult->i = allocProf->allocSize;
        break;
    case KIND_FREED_OBJECTS:
        pResult->i = allocProf->freeCount;
        break;
    case KIND_FREED_BYTES:
        pResult->i = allocProf->freeSize;
        break;
    case KIND_GC_INVOCATIONS:
        pResult->i = allocProf->gcCount;
        break;
#if PROFILE_EXTERNAL_ALLOCATIONS
    case KIND_EXT_ALLOCATED_OBJECTS:
        pResult->i = allocProf->externalAllocCount;
        break;
    case KIND_EXT_ALLOCATED_BYTES:
        pResult->i = allocProf->externalAllocSize;
        break;
    case KIND_EXT_FREED_OBJECTS:
        pResult->i = allocProf->externalFreeCount;
        break;
    case KIND_EXT_FREED_BYTES:
        pResult->i = allocProf->externalFreeSize;
        break;
#endif // PROFILE_EXTERNAL_ALLOCATIONS
    default:
        assert(false);
        pResult->i = -1;
    }
#else
    RETURN_INT(-1);
#endif
}

/*
 * public static void resetAllocCount(int kinds)
 */
static void Dalvik_dalvik_system_VMDebug_resetAllocCount(const u4* args,
    JValue* pResult)
{
#ifdef WITH_PROFILER
    unsigned int kinds = args[0];
    clearAllocProfStateFields(&gDvm.allocProf, kinds & 0xffff);
    clearAllocProfStateFields(&dvmThreadSelf()->allocProf, kinds >> 16);
#endif
    RETURN_VOID();
}

/*
 * static void startMethodTracing(String traceFileName,
 *     int bufferSize, int flags)
 *
 * Start method trace profiling.
 */
static void Dalvik_dalvik_system_VMDebug_startMethodTracing(const u4* args,
    JValue* pResult)
{
#ifdef WITH_PROFILER
    StringObject* traceFileStr = (StringObject*) args[0];
    int bufferSize = args[1];
    int flags = args[2];
    char* traceFileName;

    if (bufferSize == 0) {
        // Default to 8MB per the documentation.
        bufferSize = 8 * 1024 * 1024;
    }

    if (traceFileStr == NULL || bufferSize < 1024) {
        dvmThrowException("Ljava/lang/InvalidArgument;", NULL);
        RETURN_VOID();
    }

    traceFileName = dvmCreateCstrFromString(traceFileStr);

    dvmMethodTraceStart(traceFileName, bufferSize, flags);
    free(traceFileName);
#else
    // throw exception?
#endif
    RETURN_VOID();
}

/*
 * static void stopMethodTracing()
 *
 * Stop method tracing.
 */
static void Dalvik_dalvik_system_VMDebug_stopMethodTracing(const u4* args,
    JValue* pResult)
{
    UNUSED_PARAMETER(args);

#ifdef WITH_PROFILER
    dvmMethodTraceStop();
#else
    // throw exception?
#endif
    RETURN_VOID();
}

/*
 * static void startEmulatorTracing()
 *
 * Start sending method trace info to the emulator.
 */
static void Dalvik_dalvik_system_VMDebug_startEmulatorTracing(const u4* args,
    JValue* pResult)
{
    UNUSED_PARAMETER(args);

#ifdef WITH_PROFILER
    dvmEmulatorTraceStart();
#else
    // throw exception?
#endif
    RETURN_VOID();
}

/*
 * static void stopEmulatorTracing()
 *
 * Start sending method trace info to the emulator.
 */
static void Dalvik_dalvik_system_VMDebug_stopEmulatorTracing(const u4* args,
    JValue* pResult)
{
    UNUSED_PARAMETER(args);

#ifdef WITH_PROFILER
    dvmEmulatorTraceStop();
#else
    // throw exception?
#endif
    RETURN_VOID();
}

/*
 * static int setAllocationLimit(int limit)
 *
 * Set the current allocation limit in this thread.  Return the previous
 * value.
 */
static void Dalvik_dalvik_system_VMDebug_setAllocationLimit(const u4* args,
    JValue* pResult)
{
#if defined(WITH_ALLOC_LIMITS)
    gDvm.checkAllocLimits = true;

    Thread* self = dvmThreadSelf();
    int newLimit = args[0];
    int oldLimit = self->allocLimit;

    if (newLimit < -1) {
        LOGE("WARNING: bad limit request (%d)\n", newLimit);
        newLimit = -1;
    }
    self->allocLimit = newLimit;
    RETURN_INT(oldLimit);
#else
    UNUSED_PARAMETER(args);
    RETURN_INT(-1);
#endif
}

/*
 * static int setGlobalAllocationLimit(int limit)
 *
 * Set the allocation limit for this process.  Returns the previous value.
 */
static void Dalvik_dalvik_system_VMDebug_setGlobalAllocationLimit(const u4* args,
    JValue* pResult)
{
#if defined(WITH_ALLOC_LIMITS)
    gDvm.checkAllocLimits = true;

    int newLimit = args[0];
    int oldLimit = gDvm.allocationLimit;

    if (newLimit < -1 || newLimit > 0) {
        LOGE("WARNING: bad limit request (%d)\n", newLimit);
        newLimit = -1;
    }
    // TODO: should use an atomic swap here
    gDvm.allocationLimit = newLimit;
    RETURN_INT(oldLimit);
#else
    UNUSED_PARAMETER(args);
    RETURN_INT(-1);
#endif
}

/*
 * static boolean isDebuggerConnected()
 *
 * Returns "true" if a debugger is attached.
 */
static void Dalvik_dalvik_system_VMDebug_isDebuggerConnected(const u4* args,
    JValue* pResult)
{
    UNUSED_PARAMETER(args);

    RETURN_BOOLEAN(dvmDbgIsDebuggerConnected());
}

/*
 * static long lastDebuggerActivity()
 *
 * Returns the time, in msec, since we last had an interaction with the
 * debugger (send or receive).
 */
static void Dalvik_dalvik_system_VMDebug_lastDebuggerActivity(const u4* args,
    JValue* pResult)
{
    UNUSED_PARAMETER(args);

    RETURN_LONG(dvmDbgLastDebuggerActivity());
}

/*
 * static void startInstructionCounting()
 */
static void Dalvik_dalvik_system_VMDebug_startInstructionCounting(const u4* args,
    JValue* pResult)
{
#if defined(WITH_PROFILER)
    dvmStartInstructionCounting();
    RETURN_VOID();
#else
    dvmThrowException("Ljava/lang/UnsupportedOperationException;", NULL);
#endif
}

/*
 * static void stopInstructionCounting()
 */
static void Dalvik_dalvik_system_VMDebug_stopInstructionCounting(const u4* args,
    JValue* pResult)
{
#if defined(WITH_PROFILER)
    dvmStopInstructionCounting();
    RETURN_VOID();
#else
    dvmThrowException("Ljava/lang/UnsupportedOperationException;", NULL);
#endif
}

/*
 * static boolean getInstructionCount(int[] counts)
 *
 * Grab a copy of the global instruction count array.
 *
 * Since the instruction counts aren't synchronized, we use sched_yield
 * to improve our chances of finishing without contention.  (Only makes
 * sense on a uniprocessor.)
 */
static void Dalvik_dalvik_system_VMDebug_getInstructionCount(const u4* args,
    JValue* pResult)
{
#if defined(WITH_PROFILER)
    ArrayObject* countArray = (ArrayObject*) args[0];
    int* storage;

    storage = (int*) countArray->contents;
    sched_yield();
    memcpy(storage, gDvm.executedInstrCounts,
        kNumDalvikInstructions * sizeof(int));

    RETURN_VOID();
#else
    dvmThrowException("Ljava/lang/UnsupportedOperationException;", NULL);
#endif
}

/*
 * static boolean resetInstructionCount()
 *
 * Reset the instruction count array.
 */
static void Dalvik_dalvik_system_VMDebug_resetInstructionCount(const u4* args,
    JValue* pResult)
{
#if defined(WITH_PROFILER)
    sched_yield();
    memset(gDvm.executedInstrCounts, 0, kNumDalvikInstructions * sizeof(int));
    RETURN_VOID();
#else
    dvmThrowException("Ljava/lang/UnsupportedOperationException;", NULL);
#endif
}

/*
 * static void printLoadedClasses(int flags)
 *
 * Dump the list of loaded classes.
 */
static void Dalvik_dalvik_system_VMDebug_printLoadedClasses(const u4* args,
    JValue* pResult)
{
    int flags = args[0];

    dvmDumpAllClasses(flags);

    RETURN_VOID();
}

/*
 * static int getLoadedClassCount()
 *
 * Return the number of loaded classes
 */
static void Dalvik_dalvik_system_VMDebug_getLoadedClassCount(const u4* args,
    JValue* pResult)
{
    int count;

    UNUSED_PARAMETER(args);

    count = dvmGetNumLoadedClasses();

    RETURN_INT(count);
}

/*
 * Returns the thread-specific CPU-time clock value for the current thread,
 * or -1 if the feature isn't supported.
 */
static void Dalvik_dalvik_system_VMDebug_threadCpuTimeNanos(const u4* args,
        JValue* pResult)
{
    jlong result;
    
#ifdef HAVE_POSIX_CLOCKS
    struct timespec now;
    clock_gettime(CLOCK_THREAD_CPUTIME_ID, &now);
    result = (jlong) (now.tv_sec*1000000000LL + now.tv_nsec);
#else
    result = (jlong) -1;
#endif

    RETURN_LONG(result);
}

static const DalvikNativeMethod dalvik_system_VMDebug[] = {
    { "getAllocCount",          "(I)I",
        Dalvik_dalvik_system_VMDebug_getAllocCount },
    { "resetAllocCount",        "(I)V",
        Dalvik_dalvik_system_VMDebug_resetAllocCount },
    //{ "print",              "(Ljava/lang/String;)V",
    //    Dalvik_dalvik_system_VMDebug_print },
    { "startAllocCounting",     "()V",
        Dalvik_dalvik_system_VMDebug_startAllocCounting },
    { "stopAllocCounting",      "()V",
        Dalvik_dalvik_system_VMDebug_stopAllocCounting },
    { "startMethodTracing",     "(Ljava/lang/String;II)V",
        Dalvik_dalvik_system_VMDebug_startMethodTracing },
    { "stopMethodTracing",      "()V",
        Dalvik_dalvik_system_VMDebug_stopMethodTracing },
    { "startEmulatorTracing",   "()V",
        Dalvik_dalvik_system_VMDebug_startEmulatorTracing },
    { "stopEmulatorTracing",    "()V",
        Dalvik_dalvik_system_VMDebug_stopEmulatorTracing },
    { "setAllocationLimit",     "(I)I",
        Dalvik_dalvik_system_VMDebug_setAllocationLimit },
    { "setGlobalAllocationLimit", "(I)I",
        Dalvik_dalvik_system_VMDebug_setGlobalAllocationLimit },
    { "startInstructionCounting", "()V",
        Dalvik_dalvik_system_VMDebug_startInstructionCounting },
    { "stopInstructionCounting", "()V",
        Dalvik_dalvik_system_VMDebug_stopInstructionCounting },
    { "resetInstructionCount",  "()V",
        Dalvik_dalvik_system_VMDebug_resetInstructionCount },
    { "getInstructionCount",    "([I)V",
        Dalvik_dalvik_system_VMDebug_getInstructionCount },
    { "isDebuggerConnected",    "()Z",
        Dalvik_dalvik_system_VMDebug_isDebuggerConnected },
    { "lastDebuggerActivity",   "()J",
        Dalvik_dalvik_system_VMDebug_lastDebuggerActivity },
    { "printLoadedClasses",     "(I)V",
        Dalvik_dalvik_system_VMDebug_printLoadedClasses },
    { "getLoadedClassCount",     "()I",
        Dalvik_dalvik_system_VMDebug_getLoadedClassCount },
    { "threadCpuTimeNanos",     "()J",
        Dalvik_dalvik_system_VMDebug_threadCpuTimeNanos },
    { NULL, NULL, NULL },
};


/*
 * ===========================================================================
 *      org.apache.harmony.dalvik.NativeTestTarget
 * ===========================================================================
 */

/*
 * public static void emptyInternalStaticMethod()
 *
 * For benchmarks, a do-nothing internal method with no arguments.
 */
static void Dalvik_org_apache_harmony_dalvik_NativeTestTarget_emptyInternalMethod(
        const u4* args, JValue* pResult)
{
    UNUSED_PARAMETER(args);

    RETURN_VOID();
}

static const DalvikNativeMethod org_apache_harmony_dalvik_NativeTestTarget[] =
{
    { "emptyInternalStaticMethod", "()V",
      Dalvik_org_apache_harmony_dalvik_NativeTestTarget_emptyInternalMethod },
    { NULL, NULL, NULL },
};


/*
 * ===========================================================================
 *      dalvik.system.DexFile
 * ===========================================================================
 */

/*
 * Internal struct for managing DexFile.
 */
typedef struct DexOrJar {
    char*       fileName;
    bool        isDex;
    bool        okayToFree;
    RawDexFile* pRawDexFile;
    JarFile*    pJarFile;
} DexOrJar;

/*
 * (This is a dvmHashTableFree callback.)
 */
static void freeDexOrJar(void* vptr)
{
    DexOrJar* pDexOrJar = (DexOrJar*) vptr;

    LOGV("Freeing DexOrJar '%s'\n", pDexOrJar->fileName);

    if (pDexOrJar->isDex)
        dvmRawDexFileFree(pDexOrJar->pRawDexFile);
    else
        dvmJarFileFree(pDexOrJar->pJarFile);
    free(pDexOrJar->fileName);
    free(pDexOrJar);
}

/*
 * (This is a dvmHashTableLookup compare func.)
 *
 * Args are DexOrJar*.
 */
static int hashcmpDexOrJar(const void* tableVal, const void* newVal)
{
    return (int) newVal - (int) tableVal;
}

/*
 * Verify that the "cookie" is a DEX file we opened.
 */
static bool validateCookie(int cookie)
{
    DexOrJar* pDexOrJar = (DexOrJar*) cookie;

    LOGVV("+++ dex verifying cookie %p\n", pDexOrJar);

    if (pDexOrJar == NULL)
        return false;

    u4 hash = dvmComputeUtf8Hash(pDexOrJar->fileName);
    void* result = dvmHashTableLookup(gDvm.userDexFiles, hash, pDexOrJar,
                hashcmpDexOrJar, false);
    if (result == NULL)
        return false;

    return true;
}

/*
 * private static int openDexFile(String fileName) throws IOException
 *
 * Open a DEX file, returning a pointer to our internal data structure.
 *
 * The filename should point to the "source" jar or DEX file.  The DEX
 * code will automatically find the "optimized" version in the cache
 * directory, creating it if necessary.
 *
 * TODO: at present we will happily open the same file more than once.
 * To optimize this away we could search for existing entries in the hash
 * table and refCount them.  Requires atomic ops or adding "synchronized"
 * to the non-native code that calls here.
 */
static void Dalvik_dalvik_system_DexFile_openDexFile(const u4* args,
    JValue* pResult)
{
    StringObject* nameObj = (StringObject*) args[0];
    DexOrJar* pDexOrJar = NULL;
    JarFile* pJarFile;
    RawDexFile* pRawDexFile;
    char* name;

    name = dvmCreateCstrFromString(nameObj);

    /*
     * We have to deal with the possibility that somebody might try to
     * open one of our bootstrap class DEX files.  The set of dependencies
     * will be different, and hence the results of optimization might be
     * different, which means we'd actually need to have two versions of
     * the optimized DEX: one that only knows about part of the boot class
     * path, and one that knows about everything in it.  The latter might
     * optimize field/method accesses based on a class that appeared later
     * in the class path.
     *
     * We can't let the user-defined class loader open it and start using
     * the classes, since the optimized form of the code skips some of
     * the method and field resolution that we would ordinarily do, and
     * we'd have the wrong semantics.
     *
     * We have to reject attempts to manually open a DEX file from the boot
     * class path.  The easiest way to do this is by filename, which works
     * out because variations in name (e.g. "/system/framework/./ext.jar")
     * result in us hitting a different dalvik-cache entry.
     */
    if (dvmClassPathContains(gDvm.bootClassPath, name)) {
        LOGW("Refusing to reopen boot DEX '%s'\n", name);
        dvmThrowException("Ljava/io/IOException;",
            "Re-opening BOOTCLASSPATH DEX files is not allowed");
        free(name);
        RETURN_VOID();
    }

    /*
     * Try to open it directly as a DEX.  If that fails, try it as a Zip
     * with a "classes.dex" inside.
     */
    if (dvmRawDexFileOpen(name, &pRawDexFile, false) == 0) {
        LOGV("Opening DEX file '%s' (DEX)\n", name);

        pDexOrJar = (DexOrJar*) malloc(sizeof(DexOrJar));
        pDexOrJar->isDex = true;
        pDexOrJar->pRawDexFile = pRawDexFile;
    } else if (dvmJarFileOpen(name, &pJarFile, false) == 0) {
        LOGV("Opening DEX file '%s' (Jar)\n", name);

        pDexOrJar = (DexOrJar*) malloc(sizeof(DexOrJar));
        pDexOrJar->isDex = false;
        pDexOrJar->pJarFile = pJarFile;
    } else {
        LOGV("Unable to open DEX file '%s'\n", name);
        dvmThrowException("Ljava/io/IOException;", "unable to open DEX file");
    }

    if (pDexOrJar != NULL) {
        pDexOrJar->fileName = name;

        /* add to hash table */
        u4 hash = dvmComputeUtf8Hash(name);
        void* result;
        result = dvmHashTableLookup(gDvm.userDexFiles, hash, pDexOrJar,
                    hashcmpDexOrJar, true);
        if (result != pDexOrJar) {
            LOGE("Pointer has already been added?\n");
            dvmAbort();
        }

        pDexOrJar->okayToFree = true;
    } else
        free(name);

    RETURN_PTR(pDexOrJar);
}

/*
 * private static void closeDexFile(int cookie)
 *
 * Release resources associated with a user-loaded DEX file.
 */
static void Dalvik_dalvik_system_DexFile_closeDexFile(const u4* args,
    JValue* pResult)
{
    int cookie = args[0];
    DexOrJar* pDexOrJar = (DexOrJar*) cookie;

    if (pDexOrJar == NULL)
        RETURN_VOID();

    LOGV("Closing DEX file %p (%s)\n", pDexOrJar, pDexOrJar->fileName);

    if (!validateCookie(cookie))
        dvmAbort();

    /*
     * We can't just free arbitrary DEX files because they have bits and
     * pieces of loaded classes.  The only exception to this rule is if
     * they were never used to load classes.
     *
     * If we can't free them here, dvmInternalNativeShutdown() will free
     * them when the VM shuts down.
     */
    if (pDexOrJar->okayToFree) {
        u4 hash = dvmComputeUtf8Hash(pDexOrJar->fileName);
        if (!dvmHashTableRemove(gDvm.userDexFiles, hash, pDexOrJar)) {
            LOGW("WARNING: could not remove '%s' from DEX hash table\n",
                pDexOrJar->fileName);
        }
        LOGV("+++ freeing DexFile '%s' resources\n", pDexOrJar->fileName);
        freeDexOrJar(pDexOrJar);
    } else {
        LOGV("+++ NOT freeing DexFile '%s' resources\n", pDexOrJar->fileName);
    }

    RETURN_VOID();
}

/*
 * private static Class defineClass(String name, ClassLoader loader,
 *      int cookie, ProtectionDomain pd)
 *
 * Load a class from a DEX file.  This is roughly equivalent to defineClass()
 * in a regular VM -- it's invoked by the class loader to cause the
 * creation of a specific class.  The difference is that the search for and
 * reading of the bytes is done within the VM.
 *
 * Returns a null pointer with no exception if the class was not found.
 * Throws an exception on other failures.
 */
static void Dalvik_dalvik_system_DexFile_defineClass(const u4* args,
    JValue* pResult)
{
    StringObject* nameObj = (StringObject*) args[0];
    Object* loader = (Object*) args[1];
    int cookie = args[2];
    Object* pd = (Object*) args[3];
    ClassObject* clazz = NULL;
    DexOrJar* pDexOrJar = (DexOrJar*) cookie;
    DvmDex* pDvmDex;
    char* name;
    char* descriptor;

    name = dvmCreateCstrFromString(nameObj);
    descriptor = dvmNameToDescriptor(name);
    LOGV("--- Explicit class load '%s' 0x%08x\n", name, cookie);
    free(name);

    if (!validateCookie(cookie))
        dvmAbort();

    if (pDexOrJar->isDex)
        pDvmDex = dvmGetRawDexFileDex(pDexOrJar->pRawDexFile);
    else
        pDvmDex = dvmGetJarFileDex(pDexOrJar->pJarFile);

    /* once we load something, we can't unmap the storage */
    pDexOrJar->okayToFree = false;

    clazz = dvmDefineClass(pDvmDex, descriptor, loader);
    Thread* self = dvmThreadSelf();
    if (dvmCheckException(self)) {
        /*
         * If we threw a "class not found" exception, stifle it, since the
         * contract in the higher method says we simply return null if
         * the class is not found.
         */
        Object* excep = dvmGetException(self);
        if (strcmp(excep->clazz->descriptor,
                   "Ljava/lang/ClassNotFoundException;") == 0 ||
            strcmp(excep->clazz->descriptor,
                   "Ljava/lang/NoClassDefFoundError;") == 0)
        {
            dvmClearException(self);
        }
        clazz = NULL;
    }

    /*
     * Set the ProtectionDomain -- do we need this to happen before we
     * link the class and make it available? If so, we need to pass it
     * through dvmDefineClass (and figure out some other
     * stuff, like where it comes from for bootstrap classes).
     */
    if (clazz != NULL) {
        //LOGI("SETTING pd '%s' to %p\n", clazz->descriptor, pd);
        dvmSetFieldObject((Object*) clazz, gDvm.offJavaLangClass_pd, pd);
    }

    free(descriptor);
    RETURN_PTR(clazz);
}

/*
 * private static String[] getClassNameList(int cookie)
 *
 * Returns a String array that holds the names of all classes in the
 * specified DEX file.
 */
static void Dalvik_dalvik_system_DexFile_getClassNameList(const u4* args,
    JValue* pResult)
{
    int cookie = args[0];
    DexOrJar* pDexOrJar = (DexOrJar*) cookie;
    DvmDex* pDvmDex;
    DexFile* pDexFile;
    ArrayObject* stringArray;

    if (!validateCookie(cookie))
        dvmAbort();

    if (pDexOrJar->isDex)
        pDvmDex = dvmGetRawDexFileDex(pDexOrJar->pRawDexFile);
    else
        pDvmDex = dvmGetJarFileDex(pDexOrJar->pJarFile);
    assert(pDvmDex != NULL);
    pDexFile = pDvmDex->pDexFile;

    int count = pDexFile->pHeader->classDefsSize;
    stringArray = dvmAllocObjectArray(gDvm.classJavaLangString, count,
                    ALLOC_DEFAULT);
    if (stringArray == NULL)
        RETURN_VOID();          // should be an OOM pending

    StringObject** contents = (StringObject**) stringArray->contents;
    int i;
    for (i = 0; i < count; i++) {
        const DexClassDef* pClassDef = dexGetClassDef(pDexFile, i);
        const char* descriptor =
            dexStringByTypeIdx(pDexFile, pClassDef->classIdx);

        char* className = dvmDescriptorToDot(descriptor);
        contents[i] = dvmCreateStringFromCstr(className, ALLOC_DEFAULT);
        dvmReleaseTrackedAlloc((Object*) contents[i], NULL);
        free(className);
    }

    dvmReleaseTrackedAlloc((Object*)stringArray, NULL);
    RETURN_PTR(stringArray);
}

/*
 * public static boolean isDexOptNeeded(String apkName)
 *         throws FileNotFoundException, IOException
 *
 * Returns true if the VM believes that the apk/jar file is out of date
 * and should be passed through "dexopt" again.
 *
 * @param fileName the absolute path to the apk/jar file to examine.
 * @return true if dexopt should be called on the file, false otherwise.
 * @throws java.io.FileNotFoundException if fileName is not readable,
 *         not a file, or not present.
 * @throws java.io.IOException if fileName is not a valid apk/jar file or
 *         if problems occur while parsing it.
 * @throws java.lang.NullPointerException if fileName is null.
 * @throws dalvik.system.StaleDexCacheError if the optimized dex file
 *         is stale but exists on a read-only partition.
 */
static void Dalvik_dalvik_system_DexFile_isDexOptNeeded(const u4* args,
    JValue* pResult)
{
    StringObject* nameObj = (StringObject*) args[0];
    char* name;
    DexCacheStatus status;
    int result;

    name = dvmCreateCstrFromString(nameObj);
    if (name == NULL) {
        dvmThrowException("Ljava/lang/NullPointerException;", NULL);
        RETURN_VOID();
    }
    if (access(name, R_OK) != 0) {
        dvmThrowException("Ljava/io/FileNotFoundException;", name);
        free(name);
        RETURN_VOID();
    }
    status = dvmDexCacheStatus(name);
    LOGV("dvmDexCacheStatus(%s) returned %d\n", name, status);

    result = true;
    switch (status) {
    default: //FALLTHROUGH
    case DEX_CACHE_BAD_ARCHIVE:
        dvmThrowException("Ljava/io/IOException;", name);
        result = -1;
        break;
    case DEX_CACHE_OK:
        result = false;
        break;
    case DEX_CACHE_STALE:
        result = true;
        break;
    case DEX_CACHE_STALE_ODEX:
        dvmThrowException("Ldalvik/system/StaleDexCacheError;", name);
        result = -1;
        break;
    }
    free(name);

    if (result >= 0) {
        RETURN_BOOLEAN(result);
    } else {
        RETURN_VOID();
    }
}

static const DalvikNativeMethod dalvik_system_DexFile[] = {
    { "openDexFile",        "(Ljava/lang/String;)I",
        Dalvik_dalvik_system_DexFile_openDexFile },
    { "closeDexFile",       "(I)V",
        Dalvik_dalvik_system_DexFile_closeDexFile },
    { "defineClass",        "(Ljava/lang/String;Ljava/lang/ClassLoader;ILjava/security/ProtectionDomain;)Ljava/lang/Class;",
        Dalvik_dalvik_system_DexFile_defineClass },
    { "getClassNameList",   "(I)[Ljava/lang/String;",
        Dalvik_dalvik_system_DexFile_getClassNameList },
    { "isDexOptNeeded",     "(Ljava/lang/String;)Z",
        Dalvik_dalvik_system_DexFile_isDexOptNeeded },
    { NULL, NULL, NULL },
};


/*
 * ===========================================================================
 *      dalvik.system.VMRuntime
 * ===========================================================================
 */

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

static const DalvikNativeMethod dalvik_system_VMRuntime[] = {
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
    { NULL, NULL, NULL },
};


/*
 * ===========================================================================
 *      dalvik.system.Zygote
 * ===========================================================================
 */

#define ZYGOTE_LOG_TAG "Zygote"

/*
 * This signal handler is for zygote mode, since the zygote
 * must reap its children
 *
 */
static void sigchldHandler(int s)
{
    pid_t pid;
    int status;

    while ((pid = waitpid(-1, &status, WNOHANG)) > 0) {
        /* Log process-death status that we care about.  In general it is not
           safe to call LOG(...) from a signal handler because of possible
           reentrancy.  However, we know a priori that the current implementation
           of LOG() is safe to call from a SIGCHLD handler in the zygote process.
           If the LOG() implementation changes its locking strategy or its use
           of syscalls within the lazy-init critical section, its use here may
           become unsafe. */
        if (WIFEXITED(status)) {
            if (WEXITSTATUS(status)) {
                LOG(LOG_DEBUG, ZYGOTE_LOG_TAG, "Process %d exited cleanly (%d)\n",
                    (int) pid, WEXITSTATUS(status));
            } else {
                IF_LOGV(/*should use ZYGOTE_LOG_TAG*/) {
                    LOG(LOG_VERBOSE, ZYGOTE_LOG_TAG,
                        "Process %d exited cleanly (%d)\n",
                        (int) pid, WEXITSTATUS(status));
                }
            }
        } else if (WIFSIGNALED(status)) {
            if (WTERMSIG(status) != SIGKILL) {
                LOG(LOG_DEBUG, ZYGOTE_LOG_TAG,
                    "Process %d terminated by signal (%d)\n",
                    (int) pid, WTERMSIG(status));
            } else {
                IF_LOGV(/*should use ZYGOTE_LOG_TAG*/) {
                    LOG(LOG_VERBOSE, ZYGOTE_LOG_TAG,
                        "Process %d terminated by signal (%d)\n",
                        (int) pid, WTERMSIG(status));
                }
            }
#ifdef WCOREDUMP
            if (WCOREDUMP(status)) {
                LOG(LOG_INFO, ZYGOTE_LOG_TAG, "Process %d dumped core\n",
                    (int) pid);
            }
#endif /* ifdef WCOREDUMP */
        }

        /* 
         * If the just-crashed process is the system_server, bring down zygote
         * so that it is restarted by init and system server will be restarted
         * from there.
         */
        if (pid == gDvm.systemServerPid) {
            LOG(LOG_INFO, ZYGOTE_LOG_TAG,
                "Exit zygote because system server (%d) has terminated\n", 
                (int) pid);
            kill(getpid(), SIGKILL);
        }
    }

    if (pid < 0) {
        LOG(LOG_WARN, ZYGOTE_LOG_TAG,
            "Zygote SIGCHLD error (%d) in waitpid\n",errno);
    }
}

/*
 * configure sigchld handler for the zygote process
 * This is configured very late, because earlier in the dalvik lifecycle
 * we can fork() and exec() for the verifier/optimizer, and we
 * want to waitpid() for those rather than have them be harvested immediately.
 *
 * This ends up being called repeatedly before each fork(), but there's
 * no real harm in that.
 */
static void setSignalHandler() 
{
    int err;
    struct sigaction sa;

    memset(&sa, 0, sizeof(sa));

    sa.sa_handler = sigchldHandler;

    err = sigaction (SIGCHLD, &sa, NULL);
    
    if (err < 0) {
        LOGW("Error setting SIGCHLD handler errno: %d", errno);
    }
}

/*
 * Set the SIGCHLD handler back to default behavior in zygote children
 */
static void unsetSignalHandler()
{
    int err;
    struct sigaction sa;

    memset(&sa, 0, sizeof(sa));

    sa.sa_handler = SIG_DFL;

    err = sigaction (SIGCHLD, &sa, NULL);
    
    if (err < 0) {
        LOGW("Error unsetting SIGCHLD handler errno: %d", errno);
    }
}

/* 
 * Calls POSIX setgroups() using the int[] object as an argument.
 * A NULL argument is tolerated.
 */

static int setgroupsIntarray(ArrayObject* gidArray)
{
    gid_t *gids;
    u4 i;
    s4 *contents;

    if (gidArray == NULL) {
        return 0;
    }

    /* just in case gid_t and u4 are different... */
    gids = alloca(sizeof(gid_t) * gidArray->length);
    contents = (s4 *)gidArray->contents;

    for (i = 0 ; i < gidArray->length ; i++) {
        gids[i] = (gid_t) contents[i];
    }

    return setgroups((size_t) gidArray->length, gids);
}

/*
 * Sets the resource limits via setrlimit(2) for the values in the
 * two-dimensional array of integers that's passed in. The second dimension
 * contains a tuple of length 3: (resource, rlim_cur, rlim_max). NULL is
 * treated as an empty array.
 *
 * -1 is returned on error.
 */
static int setrlimitsFromArray(ArrayObject* rlimits)
{
    u4 i;
    struct rlimit rlim;

    if (rlimits == NULL) {
        return 0;
    }

    memset (&rlim, 0, sizeof(rlim));

    ArrayObject** tuples = (ArrayObject **)(rlimits->contents);

    for (i = 0; i < rlimits->length; i++) {
        ArrayObject * rlimit_tuple = tuples[i];
        s4* contents = (s4 *)rlimit_tuple->contents;
        int err;

        if (rlimit_tuple->length != 3) {
            LOGE("rlimits array must have a second dimension of size 3");
            return -1;
        }

        rlim.rlim_cur = contents[1];
        rlim.rlim_max = contents[2];

        err = setrlimit(contents[0], &rlim);

        if (err < 0) {
            return -1;
        }
    }
    
    return 0;
}

/* native public static int fork(); */
static void Dalvik_dalvik_system_Zygote_fork(const u4* args, JValue* pResult)
{
    pid_t pid;
    int err;

    if (!gDvm.zygote) {
        dvmThrowException("Ljava/lang/IllegalStateException;",
            "VM instance not started with -Xzygote");

        RETURN_VOID();
    }

    if (!dvmGcPreZygoteFork()) {
        LOGE("pre-fork heap failed\n");
        dvmAbort();
    }

    setSignalHandler();      

    dvmDumpLoaderStats("zygote");
    pid = fork();

#ifdef HAVE_ANDROID_OS
    if (pid == 0) {
        /* child process */
        extern int gMallocLeakZygoteChild;
        gMallocLeakZygoteChild = 1;
    }
#endif

    RETURN_INT(pid);
}

/* 
 * Utility routine to fork zygote and specialize the child process.
 */
static pid_t forkAndSpecializeCommon(const u4* args)
{
    pid_t pid;

    uid_t uid = (uid_t) args[0];
    gid_t gid = (gid_t) args[1];
    ArrayObject* gids = (ArrayObject *)args[2];
    u4 enableDebugger = args[3];
    ArrayObject *rlimits = (ArrayObject *)args[4];

    if (!gDvm.zygote) {
        dvmThrowException("Ljava/lang/IllegalStateException;",
            "VM instance not started with -Xzygote");

        return -1;
    }

    if (!dvmGcPreZygoteFork()) {
        LOGE("pre-fork heap failed\n");
        dvmAbort();
    }

    setSignalHandler();      

    dvmDumpLoaderStats("zygote");
    pid = fork();

    if (pid == 0) {
        int err;
        /* The child process */

#ifdef HAVE_ANDROID_OS
        extern int gMallocLeakZygoteChild;
        gMallocLeakZygoteChild = 1;

        /* keep caps across UID change, unless we're staying root */
        if (uid != 0) {
            err = prctl(PR_SET_KEEPCAPS, 1, 0, 0, 0);

            if (err < 0) {
                LOGW("cannot PR_SET_KEEPCAPS errno: %d", errno);
            }
        }

#endif /* HAVE_ANDROID_OS */

        err = setgroupsIntarray(gids);

        if (err < 0) {
            LOGW("cannot setgroups() errno: %d", errno);
        }

        err = setrlimitsFromArray(rlimits);

        if (err < 0) {
            LOGW("cannot setrlimit() errno: %d", errno);
        }

        err = setgid(gid);
        if (err < 0) {
            LOGW("cannot setgid(%d) errno: %d", gid, errno);
        }

        err = setuid(uid);
        if (err < 0) {
            LOGW("cannot setuid(%d) errno: %d", uid, errno);
        }

        /*
         * Our system thread ID has changed.  Get the new one.
         */
        Thread* thread = dvmThreadSelf();
        thread->systemTid = dvmGetSysThreadId();

        // jdwp not started until dvmInitAfterZygote()
        gDvm.jdwpAllowed = (enableDebugger != 0);

        unsetSignalHandler();      
        gDvm.zygote = false;
        if (!dvmInitAfterZygote()) {
            LOGE("error in post-zygote initialization\n");
            dvmAbort();
        }
    } else if (pid > 0) {
        /* the parent process */
    }

    return pid;
}

/* native public static int forkAndSpecialize(int uid, int gid, 
 * int[] gids, boolean enableDebugger); 
 */
static void Dalvik_dalvik_system_Zygote_forkAndSpecialize(const u4* args,
    JValue* pResult)
{
    pid_t pid;

    pid = forkAndSpecializeCommon(args);

    RETURN_INT(pid);
}

/* native public static int forkSystemServer(int uid, int gid, 
 * int[] gids, boolean enableDebugger); 
 */
static void Dalvik_dalvik_system_Zygote_forkSystemServer(
        const u4* args, JValue* pResult)
{
    pid_t pid;
    pid = forkAndSpecializeCommon(args);

    /* The zygote process checks whether the child process has died or not. */
    if (pid > 0) {
        int status;

        LOGI("System server process %d has been created", pid);
        gDvm.systemServerPid = pid;
        /* There is a slight window that the system server process has crashed
         * but it went unnoticed because we haven't published its pid yet. So
         * we recheck here just to make sure that all is well.
         */
        if (waitpid(pid, &status, WNOHANG) == pid) {
            LOGE("System server process %d has died. Restarting Zygote!", pid);
            kill(getpid(), SIGKILL);
        }
    }
    RETURN_INT(pid);
}

static const DalvikNativeMethod dalvik_system_Zygote[] = {
    { "fork",            "()I",
        Dalvik_dalvik_system_Zygote_fork },
    { "forkAndSpecialize",            "(II[IZ[[I)I",
        Dalvik_dalvik_system_Zygote_forkAndSpecialize },
    { "forkSystemServer",            "(II[IZ[[I)I",
        Dalvik_dalvik_system_Zygote_forkSystemServer },
    { NULL, NULL, NULL },
};


/*
 * ===========================================================================
 *      java.lang.VMClassLoader
 * ===========================================================================
 */

/*
 * static Class defineClass(ClassLoader cl, String name,
 *     byte[] data, int offset, int len, ProtectionDomain pd)
 *     throws ClassFormatError
 *
 * Convert an array of bytes to a Class object.
 */
static void Dalvik_java_lang_VMClassLoader_defineClass(const u4* args,
    JValue* pResult)
{
    Object* loader = (Object*) args[0];
    StringObject* nameObj = (StringObject*) args[1];
    const u1* data = (const u1*) args[2];
    int offset = args[3];
    int len = args[4];
    Object* pd = (Object*) args[5];
    char* name = NULL;

    name = dvmCreateCstrFromString(nameObj);
    LOGE("ERROR: defineClass(%p, %s, %p, %d, %d, %p)\n",
        loader, name, data, offset, len, pd);
    dvmThrowException("Ljava/lang/UnsupportedOperationException;",
        "can't load this type of class file");

    free(name);
    RETURN_VOID();
}

/*
 * static Class defineClass(ClassLoader cl, byte[] data, int offset,
 *     int len, ProtectionDomain pd)
 *     throws ClassFormatError
 *
 * Convert an array of bytes to a Class object. Deprecated version of
 * previous method, lacks name parameter.
 */
static void Dalvik_java_lang_VMClassLoader_defineClass2(const u4* args,
    JValue* pResult)
{
    Object* loader = (Object*) args[0];
    const u1* data = (const u1*) args[1];
    int offset = args[2];
    int len = args[3];
    Object* pd = (Object*) args[4];

    LOGE("ERROR: defineClass(%p, %p, %d, %d, %p)\n",
        loader, data, offset, len, pd);
    dvmThrowException("Ljava/lang/UnsupportedOperationException;",
        "can't load this type of class file");

    RETURN_VOID();
}

/*
 * static Class findLoadedClass(ClassLoader cl, String name)
 */
static void Dalvik_java_lang_VMClassLoader_findLoadedClass(const u4* args,
    JValue* pResult)
{
    Object* loader = (Object*) args[0];
    StringObject* nameObj = (StringObject*) args[1];
    ClassObject* clazz = NULL;
    char* name = NULL;
    char* descriptor = NULL;
    char* cp;

    if (nameObj == NULL) {
        dvmThrowException("Ljava/lang/NullPointerException;", NULL);
        goto bail;
    }

    /*
     * Get a UTF-8 copy of the string, and convert dots to slashes.
     */
    name = dvmCreateCstrFromString(nameObj);
    if (name == NULL)
        goto bail;

    descriptor = dvmDotToDescriptor(name);
    if (descriptor == NULL)
        goto bail;

    clazz = dvmLookupClass(descriptor, loader, false);
    LOGVV("look: %s ldr=%p --> %p\n", descriptor, loader, clazz);

bail:
    free(name);
    free(descriptor);
    RETURN_PTR(clazz);
}

/*
 * private static int getBootClassPathSize()
 *
 * Get the number of entries in the boot class path.
 */
static void Dalvik_java_lang_VMClassLoader_getBootClassPathSize(const u4* args,
    JValue* pResult)
{
    int count = dvmGetBootPathSize();
    RETURN_INT(count);
}

/*
 * private static String getBootClassPathResource(String name, int index)
 *
 * Find a resource with a matching name in a boot class path entry.
 *
 * This mimics the previous VM interface, since we're sharing class libraries.
 */
static void Dalvik_java_lang_VMClassLoader_getBootClassPathResource(
    const u4* args, JValue* pResult)
{
    StringObject* nameObj = (StringObject*) args[0];
    StringObject* result;
    int idx = args[1];
    char* name;

    name = dvmCreateCstrFromString(nameObj);
    if (name == NULL)
        RETURN_PTR(NULL);

    result = dvmGetBootPathResource(name, idx);
    free(name);
    dvmReleaseTrackedAlloc((Object*)result, NULL);
    RETURN_PTR(result);
}

/*
 * static final Class getPrimitiveClass(char prim_type)
 */
static void Dalvik_java_lang_VMClassLoader_getPrimitiveClass(const u4* args,
    JValue* pResult)
{
    int primType = args[0];

    pResult->l = dvmFindPrimitiveClass(primType);
}

/*
 * static Class loadClass(String name, boolean resolve)
 *     throws ClassNotFoundException
 *
 * Load class using bootstrap class loader.
 *
 * Return the Class object associated with the class or interface with
 * the specified name.
 *
 * "name" is in "binary name" format, e.g. "dalvik.system.Debug$1".
 */
static void Dalvik_java_lang_VMClassLoader_loadClass(const u4* args,
    JValue* pResult)
{
    StringObject* nameObj = (StringObject*) args[0];
    bool resolve = (args[1] != 0);
    ClassObject* clazz;

    clazz = findClassByName(nameObj, NULL, resolve);
    assert(clazz == NULL || dvmIsClassLinked(clazz));
    RETURN_PTR(clazz);
}

static const DalvikNativeMethod java_lang_VMClassLoader[] = {
    { "defineClass",        "(Ljava/lang/ClassLoader;Ljava/lang/String;[BIILjava/security/ProtectionDomain;)Ljava/lang/Class;",
        Dalvik_java_lang_VMClassLoader_defineClass },
    { "defineClass",        "(Ljava/lang/ClassLoader;[BIILjava/security/ProtectionDomain;)Ljava/lang/Class;",
        Dalvik_java_lang_VMClassLoader_defineClass2 },
    { "findLoadedClass",    "(Ljava/lang/ClassLoader;Ljava/lang/String;)Ljava/lang/Class;",
        Dalvik_java_lang_VMClassLoader_findLoadedClass },
    { "getBootClassPathSize", "()I",
        Dalvik_java_lang_VMClassLoader_getBootClassPathSize },
    { "getBootClassPathResource", "(Ljava/lang/String;I)Ljava/lang/String;",
        Dalvik_java_lang_VMClassLoader_getBootClassPathResource },
    { "getPrimitiveClass",  "(C)Ljava/lang/Class;",
        Dalvik_java_lang_VMClassLoader_getPrimitiveClass },
    { "loadClass",          "(Ljava/lang/String;Z)Ljava/lang/Class;",
        Dalvik_java_lang_VMClassLoader_loadClass },
    { NULL, NULL, NULL },
};


/*
 * ===========================================================================
 *      dalvik.system.VMStack
 * ===========================================================================
 */

#define NUM_DOPRIV_FUNCS    4

/*
 * Determine if "method" is a "privileged" invocation, i.e. is it one
 * of the variations of AccessController.doPrivileged().
 *
 * Because the security stuff pulls in a pile of stuff that we may not
 * want or need, we don't do the class/method lookups at init time, but
 * instead on first use.
 */
static bool dvmIsPrivilegedMethod(const Method* method)
{
    int i;

    assert(method != NULL);

    if (!gDvm.javaSecurityAccessControllerReady) {
        /*
         * Populate on first use.  No concurrency risk since we're just
         * finding pointers to fixed structures.
         */
        static const char* kSignatures[NUM_DOPRIV_FUNCS] = {
            "(Ljava/security/PrivilegedAction;)Ljava/lang/Object;",
            "(Ljava/security/PrivilegedExceptionAction;)Ljava/lang/Object;",
            "(Ljava/security/PrivilegedAction;Ljava/security/AccessControlContext;)Ljava/lang/Object;",
            "(Ljava/security/PrivilegedExceptionAction;Ljava/security/AccessControlContext;)Ljava/lang/Object;",
        };
        ClassObject* clazz;

        clazz = dvmFindClassNoInit("Ljava/security/AccessController;", NULL);
        if (clazz == NULL) {
            LOGW("Couldn't find java/security/AccessController\n");
            return false;
        }

        assert(NELEM(gDvm.methJavaSecurityAccessController_doPrivileged) ==
               NELEM(kSignatures));

        /* verify init */
        for (i = 0; i < NUM_DOPRIV_FUNCS; i++) {
            gDvm.methJavaSecurityAccessController_doPrivileged[i] =
                dvmFindDirectMethodByDescriptor(clazz, "doPrivileged", kSignatures[i]);
            if (gDvm.methJavaSecurityAccessController_doPrivileged[i] == NULL) {
                LOGW("Warning: couldn't find java/security/AccessController"
                    ".doPrivileged %s\n", kSignatures[i]);
                return false;
            }
        }

        /* all good, raise volatile readiness flag */
        gDvm.javaSecurityAccessControllerReady = true;
    }

    for (i = 0; i < NUM_DOPRIV_FUNCS; i++) {
        if (gDvm.methJavaSecurityAccessController_doPrivileged[i] == method) {
            //LOGI("+++ doPriv match\n");
            return true;
        }
    }
    return false;
}

/*
 * public static ClassLoader getCallingClassLoader()
 *
 * Return the defining class loader of the caller's caller.
 */
static void Dalvik_dalvik_system_VMStack_getCallingClassLoader(const u4* args,
    JValue* pResult)
{
    ClassObject* clazz = dvmGetCaller2Class(dvmThreadSelf()->curFrame);

    UNUSED_PARAMETER(args);

    if (clazz == NULL)
        RETURN_PTR(NULL);
    RETURN_PTR(clazz->classLoader);
}

/*
 * public static ClassLoader getCallingClassLoader2()
 *
 * Return the defining class loader of the caller's caller's caller.
 */
static void Dalvik_dalvik_system_VMStack_getCallingClassLoader2(const u4* args,
    JValue* pResult)
{
    ClassObject* clazz = dvmGetCaller3Class(dvmThreadSelf()->curFrame);

    UNUSED_PARAMETER(args);

    if (clazz == NULL)
        RETURN_PTR(NULL);
    RETURN_PTR(clazz->classLoader);
}

/*
 * public static Class<?>[] getClasses(int maxDepth, boolean stopAtPrivileged)
 *
 * Create an array of classes for the methods on the stack, skipping the
 * first two and all reflection methods.  If "stopAtPrivileged" is set,
 * stop shortly after we encounter a privileged class.
 */
static void Dalvik_dalvik_system_VMStack_getClasses(const u4* args,
    JValue* pResult)
{
    /* note "maxSize" is unsigned, so -1 turns into a very large value */
    unsigned int maxSize = args[0];
    bool stopAtPrivileged = args[1];
    unsigned int size = 0;
    const unsigned int kSkip = 2;
    const Method** methods = NULL;
    int methodCount;

    /*
     * Get an array with the stack trace in it.
     */
    if (!dvmCreateStackTraceArray(dvmThreadSelf()->curFrame, &methods,
            &methodCount))
    {
        LOGE("Failed to create stack trace array\n");
        dvmThrowException("Ljava/lang/InternalError;", NULL);
        RETURN_VOID();
    }

    //int i;
    //LOGI("dvmCreateStackTraceArray results:\n");
    //for (i = 0; i < methodCount; i++) {
    //    LOGI(" %2d: %s.%s\n",
    //        i, methods[i]->clazz->descriptor, methods[i]->name);
    //}

    /*
     * Run through the array and count up how many elements there are.
     */
    unsigned int idx;
    for (idx = kSkip; (int) idx < methodCount && size < maxSize; idx++) {
        const Method* meth = methods[idx];

        if (dvmIsReflectionMethod(meth))
            continue;

        if (stopAtPrivileged && dvmIsPrivilegedMethod(meth)) {
            /*
             * We want the last element of the array to be the caller of
             * the privileged method, so we want to include the privileged
             * method and the next one.
             */
            if (maxSize > size + 2)
                maxSize = size + 2;
        }

        size++;
    }

    /*
     * Create an array object to hold the classes.
     * TODO: can use gDvm.classJavaLangClassArray here?
     */
    ClassObject* classArrayClass = NULL;
    ArrayObject* classes = NULL;
    classArrayClass = dvmFindArrayClass("[Ljava/lang/Class;", NULL);
    if (classArrayClass == NULL) {
        LOGW("Unable to find java.lang.Class array class\n");
        goto bail;
    }
    classes = dvmAllocArray(classArrayClass, size, kObjectArrayRefWidth,
                ALLOC_DEFAULT);
    if (classes == NULL) {
        LOGW("Unable to allocate class array (%d elems)\n", size);
        goto bail;
    }

    /*
     * Fill in the array.
     */
    ClassObject** objects = (ClassObject**) classes->contents;

    unsigned int sidx = 0;
    for (idx = kSkip; (int) idx < methodCount && sidx < size; idx++) {
        const Method* meth = methods[idx];

        if (dvmIsReflectionMethod(meth))
            continue;

        *objects++ = meth->clazz;
        sidx++;
    }

bail:
    free(methods);
    dvmReleaseTrackedAlloc((Object*) classes, NULL);
    RETURN_PTR(classes);
}

/*
 * public static StackTraceElement[] getThreadStackTrace(Thread t)
 *
 * Retrieve the stack trace of the specified thread and return it as an
 * array of StackTraceElement.  Returns NULL on failure.
 */
static void Dalvik_dalvik_system_VMStack_getThreadStackTrace(const u4* args,
    JValue* pResult)
{
    Object* targetThreadObj = (Object*) args[0];
    Thread* self = dvmThreadSelf();
    Thread* thread;
    int* traceBuf;

    assert(targetThreadObj != NULL);

    dvmLockThreadList(self);

    /*
     * Make sure the thread is still alive and in the list.
     */
    for (thread = gDvm.threadList; thread != NULL; thread = thread->next) {
        if (thread->threadObj == targetThreadObj)
            break;
    }
    if (thread == NULL) {
        LOGI("VMStack.getThreadStackTrace: threadObj %p not active\n",
            targetThreadObj);
        dvmUnlockThreadList();
        RETURN_PTR(NULL);
    }

    /*
     * Suspend the thread, pull out the stack trace, then resume the thread
     * and release the thread list lock.  If we're being asked to examine
     * our own stack trace, skip the suspend/resume.
     */
    int stackDepth = -1;
    if (thread != self)
        dvmSuspendThread(thread);
    traceBuf = dvmFillInStackTraceRaw(thread, &stackDepth);
    if (thread != self)
        dvmResumeThread(thread);
    dvmUnlockThreadList();

    /*
     * Convert the raw buffer into an array of StackTraceElement.
     */
    ArrayObject* trace = dvmGetStackTraceRaw(traceBuf, stackDepth);
    free(traceBuf);
    RETURN_PTR(trace);
}

static const DalvikNativeMethod dalvik_system_VMStack[] = {
    { "getCallingClassLoader",  "()Ljava/lang/ClassLoader;",
        Dalvik_dalvik_system_VMStack_getCallingClassLoader },
    { "getCallingClassLoader2", "()Ljava/lang/ClassLoader;",
        Dalvik_dalvik_system_VMStack_getCallingClassLoader2 },
    { "getClasses",             "(IZ)[Ljava/lang/Class;",
        Dalvik_dalvik_system_VMStack_getClasses },
    { "getThreadStackTrace",    "(Ljava/lang/Thread;)[Ljava/lang/StackTraceElement;",
        Dalvik_dalvik_system_VMStack_getThreadStackTrace },
    { NULL, NULL, NULL },
};


/*
 * ===========================================================================
 *      java.lang.VMThread
 * ===========================================================================
 */

/*
 * static void create(Thread t, long stacksize)
 *
 * This is eventually called as a result of Thread.start().
 *
 * Throws an exception on failure.
 */
static void Dalvik_java_lang_VMThread_create(const u4* args, JValue* pResult)
{
    Object* threadObj = (Object*) args[0];
    s8 stackSize = GET_ARG_LONG(args, 1);

    dvmCreateInterpThread(threadObj, (int) stackSize);
    RETURN_VOID();
}

/*
 * static Thread currentThread()
 */
static void Dalvik_java_lang_VMThread_currentThread(const u4* args,
    JValue* pResult)
{
    UNUSED_PARAMETER(args);

    RETURN_PTR(dvmThreadSelf()->threadObj);
}

/*
 * void getStatus()
 *
 * Gets the Thread status. Result is in VM terms, has to be mapped to
 * Thread.State by interpreted code.
 */
static void Dalvik_java_lang_VMThread_getStatus(const u4* args, JValue* pResult)
{
    Object* thisPtr = (Object*) args[0];
    Thread* thread;
    int result;

    dvmLockThreadList(NULL);
    thread = dvmGetThreadFromThreadObject(thisPtr);
    if (thread != NULL)
        result = thread->status;
    else
        result = THREAD_ZOMBIE;     // assume it used to exist and is now gone
    dvmUnlockThreadList();
    
    RETURN_INT(result);
}

/*
 * boolean holdsLock(Object object)
 *
 * Returns whether the current thread has a monitor lock on the specific
 * object.
 */
static void Dalvik_java_lang_VMThread_holdsLock(const u4* args, JValue* pResult)
{
    Object* thisPtr = (Object*) args[0];
    Object* object = (Object*) args[1];
    Thread* thread;

    if (object == NULL) {
        dvmThrowException("Ljava/lang/NullPointerException;", NULL);
        RETURN_VOID();
    }

    dvmLockThreadList(NULL);
    thread = dvmGetThreadFromThreadObject(thisPtr);
    int result = dvmHoldsLock(thread, object);
    dvmUnlockThreadList();

    RETURN_BOOLEAN(result);
}

/*
 * void interrupt()
 *
 * Interrupt a thread that is waiting (or is about to wait) on a monitor.
 */
static void Dalvik_java_lang_VMThread_interrupt(const u4* args, JValue* pResult)
{
    Object* thisPtr = (Object*) args[0];
    Thread* thread;

    dvmLockThreadList(NULL);
    thread = dvmGetThreadFromThreadObject(thisPtr);
    if (thread != NULL)
        dvmThreadInterrupt(thread);
    dvmUnlockThreadList();
    RETURN_VOID();
}

/*
 * static boolean interrupted()
 *
 * Determine if the current thread has been interrupted.  Clears the flag.
 */
static void Dalvik_java_lang_VMThread_interrupted(const u4* args,
    JValue* pResult)
{
    Thread* self = dvmThreadSelf();
    bool interrupted;

    UNUSED_PARAMETER(args);

    interrupted = self->interrupted;
    self->interrupted = false;
    RETURN_BOOLEAN(interrupted);
}

/*
 * boolean isInterrupted()
 *
 * Determine if the specified thread has been interrupted.  Does not clear
 * the flag.
 */
static void Dalvik_java_lang_VMThread_isInterrupted(const u4* args,
    JValue* pResult)
{
    Object* thisPtr = (Object*) args[0];
    Thread* thread;
    bool interrupted;

    dvmLockThreadList(NULL);
    thread = dvmGetThreadFromThreadObject(thisPtr);
    if (thread != NULL)
        interrupted = thread->interrupted;
    else
        interrupted = false;
    dvmUnlockThreadList();

    RETURN_BOOLEAN(interrupted);
}

/*
 * void nameChanged(String newName)
 *
 * The name of the target thread has changed.  We may need to alert DDMS.
 */
static void Dalvik_java_lang_VMThread_nameChanged(const u4* args,
    JValue* pResult)
{
    Object* thisPtr = (Object*) args[0];
    StringObject* nameStr = (StringObject*) args[1];
    Thread* thread;
    int threadId = -1;

    /* get the thread's ID */
    dvmLockThreadList(NULL);
    thread = dvmGetThreadFromThreadObject(thisPtr);
    if (thread != NULL)
        threadId = thread->threadId;
    dvmUnlockThreadList();

    dvmDdmSendThreadNameChange(threadId, nameStr);
    //char* str = dvmCreateCstrFromString(nameStr);
    //LOGI("UPDATE: threadid=%d now '%s'\n", threadId, str);
    //free(str);

    RETURN_VOID();
}

/*
 * void setPriority(int newPriority)
 *
 * Alter the priority of the specified thread.  "newPriority" will range
 * from Thread.MIN_PRIORITY to Thread.MAX_PRIORITY (1-10), with "normal"
 * threads at Thread.NORM_PRIORITY (5).
 */
static void Dalvik_java_lang_VMThread_setPriority(const u4* args,
    JValue* pResult)
{
    Object* thisPtr = (Object*) args[0];
    int newPriority = args[1];
    Thread* thread;
    
    dvmLockThreadList(NULL);
    thread = dvmGetThreadFromThreadObject(thisPtr);
    if (thread != NULL)
        dvmChangeThreadPriority(thread, newPriority);
    //dvmDumpAllThreads(false);
    dvmUnlockThreadList();

    RETURN_VOID();
}

/*
 * static void sleep(long msec, int nsec)
 */
static void Dalvik_java_lang_VMThread_sleep(const u4* args, JValue* pResult)
{
    Thread* self = dvmThreadSelf();
    dvmThreadSleep(GET_ARG_LONG(args,0), args[2]);
    RETURN_VOID();
}

/*
 * public void yield()
 *
 * Causes the thread to temporarily pause and allow other threads to execute.
 *
 * The exact behavior is poorly defined.  Some discussion here:
 *   http://www.cs.umd.edu/~pugh/java/memoryModel/archive/0944.html
 */
static void Dalvik_java_lang_VMThread_yield(const u4* args, JValue* pResult)
{
    UNUSED_PARAMETER(args);

    sched_yield();

    RETURN_VOID();
}

static const DalvikNativeMethod java_lang_VMThread[] = {
    { "create",         "(Ljava/lang/Thread;J)V",
        Dalvik_java_lang_VMThread_create },
    { "currentThread",  "()Ljava/lang/Thread;",
        Dalvik_java_lang_VMThread_currentThread },
    { "getStatus",      "()I",
        Dalvik_java_lang_VMThread_getStatus },
    { "holdsLock",      "(Ljava/lang/Object;)Z",
        Dalvik_java_lang_VMThread_holdsLock },
    { "interrupt",      "()V",
        Dalvik_java_lang_VMThread_interrupt },
    { "interrupted",    "()Z",
        Dalvik_java_lang_VMThread_interrupted },
    { "isInterrupted",  "()Z",
        Dalvik_java_lang_VMThread_isInterrupted },
    { "nameChanged",    "(Ljava/lang/String;)V",
        Dalvik_java_lang_VMThread_nameChanged },
    { "setPriority",    "(I)V",
        Dalvik_java_lang_VMThread_setPriority },
    { "sleep",          "(JI)V",
        Dalvik_java_lang_VMThread_sleep },
    { "yield",          "()V",
        Dalvik_java_lang_VMThread_yield },
    { NULL, NULL, NULL },
};


/*
 * ===========================================================================
 *      java.lang.Throwable
 * ===========================================================================
 */

/*
 * private static Object nativeFillInStackTrace()
 */
static void Dalvik_java_lang_Throwable_nativeFillInStackTrace(const u4* args,
    JValue* pResult)
{
    Object* stackState = NULL;

    UNUSED_PARAMETER(args);

    stackState = dvmFillInStackTrace(dvmThreadSelf());
    RETURN_PTR(stackState);
}

/*
 * private static StackTraceElement[] nativeGetStackTrace(Object stackState)
 *
 * The "stackState" argument must be the value returned by an earlier call to
 * nativeFillInStackTrace().
 */
static void Dalvik_java_lang_Throwable_nativeGetStackTrace(const u4* args,
    JValue* pResult)
{
    Object* stackState = (Object*) args[0];
    ArrayObject* elements = NULL;

    elements = dvmGetStackTrace(stackState);
    RETURN_PTR(elements);
}

static const DalvikNativeMethod java_lang_Throwable[] = {
    { "nativeFillInStackTrace", "()Ljava/lang/Object;",
        Dalvik_java_lang_Throwable_nativeFillInStackTrace },
    { "nativeGetStackTrace",    "(Ljava/lang/Object;)[Ljava/lang/StackTraceElement;",
        Dalvik_java_lang_Throwable_nativeGetStackTrace },
    { NULL, NULL, NULL },
};


/*
 * ===========================================================================
 *      java.lang.Object
 * ===========================================================================
 */

/*
 * private Object internalClone()
 *
 * Implements most of Object.clone().
 */
static void Dalvik_java_lang_Object_internalClone(const u4* args,
    JValue* pResult)
{
    Object* thisPtr = (Object*) args[0];
    Object* clone = dvmCloneObject(thisPtr);

    dvmReleaseTrackedAlloc(clone, NULL);
    RETURN_PTR(clone);
}

/*
 * public int hashCode()
 */
static void Dalvik_java_lang_Object_hashCode(const u4* args, JValue* pResult)
{
    Object* thisPtr = (Object*) args[0];

    RETURN_PTR(thisPtr);    /* use the pointer as the hash code */
}

/*
 * public Class getClass()
 */
static void Dalvik_java_lang_Object_getClass(const u4* args, JValue* pResult)
{
    Object* thisPtr = (Object*) args[0];

    RETURN_PTR(thisPtr->clazz);
}

/*
 * public void notify()
 *
 * NOTE: we declare this as a full DalvikBridgeFunc, rather than a
 * DalvikNativeFunc, because we really want to avoid the "self" lookup.
 */
static void Dalvik_java_lang_Object_notify(const u4* args, JValue* pResult,
    const Method* method, Thread* self)
{
    Object* thisPtr = (Object*) args[0];

    dvmObjectNotify(self, thisPtr);
    RETURN_VOID();
}

/*
 * public void notifyAll()
 */
static void Dalvik_java_lang_Object_notifyAll(const u4* args, JValue* pResult,
    const Method* method, Thread* self)
{
    Object* thisPtr = (Object*) args[0];

    dvmObjectNotifyAll(self, thisPtr);
    RETURN_VOID();
}

/*
 * public void wait(long ms, int ns) throws InterruptedException
 */
static void Dalvik_java_lang_Object_wait(const u4* args, JValue* pResult,
    const Method* method, Thread* self)
{
    Object* thisPtr = (Object*) args[0];

    dvmObjectWait(self, thisPtr, GET_ARG_LONG(args,1), (s4)args[3], true);
    RETURN_VOID();
}

static const DalvikNativeMethod java_lang_Object[] = {
    { "internalClone",  "(Ljava/lang/Cloneable;)Ljava/lang/Object;",
        Dalvik_java_lang_Object_internalClone },
    { "hashCode",       "()I",
        Dalvik_java_lang_Object_hashCode },
    { "notify",         "()V",
        (DalvikNativeFunc) Dalvik_java_lang_Object_notify },
    { "notifyAll",      "()V",
        (DalvikNativeFunc) Dalvik_java_lang_Object_notifyAll },
    { "wait",           "(JI)V",
        (DalvikNativeFunc) Dalvik_java_lang_Object_wait },
    { "getClass",       "()Ljava/lang/Class;",
        Dalvik_java_lang_Object_getClass },
    { NULL, NULL, NULL },
};


/*
 * ===========================================================================
 *      java.lang.Class
 * ===========================================================================
 */

/*
 * native public boolean desiredAssertionStatus()
 *
 * Determine the class-init-time assertion status of a class.  This is
 * called from <clinit> in javac-generated classes that use the Java
 * programming language "assert" keyword.
 */
static void Dalvik_java_lang_Class_desiredAssertionStatus(const u4* args,
    JValue* pResult)
{
    ClassObject* thisPtr = (ClassObject*) args[0];
    char* className = dvmDescriptorToName(thisPtr->descriptor);
    int i;
    bool enable = false;

    /*
     * Run through the list of arguments specified on the command line.  The
     * last matching argument takes precedence.
     */
    for (i = 0; i < gDvm.assertionCtrlCount; i++) {
        const AssertionControl* pCtrl = &gDvm.assertionCtrl[i];

        if (pCtrl->isPackage) {
            /*
             * Given "dalvik/system/Debug" or "MyStuff", compute the
             * length of the package portion of the class name string.
             *
             * Unlike most package operations, we allow matching on
             * "sub-packages", so "dalvik..." will match "dalvik.Foo"
             * and "dalvik.system.Foo".
             *
             * The pkgOrClass string looks like "dalvik/system/", i.e. it still
             * has the terminating slash, so we can be sure we're comparing
             * against full package component names.
             */
            const char* lastSlash;
            int pkgLen;

            lastSlash = strrchr(className, '/');
            if (lastSlash == NULL) {
                pkgLen = 0;
            } else {
                pkgLen = lastSlash - className +1;
            }

            if (pCtrl->pkgOrClassLen > pkgLen ||
                memcmp(pCtrl->pkgOrClass, className, pCtrl->pkgOrClassLen) != 0)
            {
                LOGV("ASRT: pkg no match: '%s'(%d) vs '%s'\n",
                    className, pkgLen, pCtrl->pkgOrClass);
            } else {
                LOGV("ASRT: pkg match: '%s'(%d) vs '%s' --> %d\n",
                    className, pkgLen, pCtrl->pkgOrClass, pCtrl->enable);
                enable = pCtrl->enable;
            }
        } else {
            /*
             * "pkgOrClass" holds a fully-qualified class name, converted from
             * dot-form to slash-form.  An empty string means all classes.
             */
            if (pCtrl->pkgOrClass == NULL) {
                /* -esa/-dsa; see if class is a "system" class */
                if (strncmp(className, "java/", 5) != 0) {
                    LOGV("ASRT: sys no match: '%s'\n", className);
                } else {
                    LOGV("ASRT: sys match: '%s' --> %d\n",
                        className, pCtrl->enable);
                    enable = pCtrl->enable;
                }
            } else if (*pCtrl->pkgOrClass == '\0') {
                LOGV("ASRT: class all: '%s' --> %d\n",
                    className, pCtrl->enable);
                enable = pCtrl->enable;
            } else {
                if (strcmp(pCtrl->pkgOrClass, className) != 0) {
                    LOGV("ASRT: cls no match: '%s' vs '%s'\n",
                        className, pCtrl->pkgOrClass);
                } else {
                    LOGV("ASRT: cls match: '%s' vs '%s' --> %d\n",
                        className, pCtrl->pkgOrClass, pCtrl->enable);
                    enable = pCtrl->enable;
                }
            }
        }
    }

    free(className);
    RETURN_INT(enable);
}

/*
 * static public Class<?> classForName(String name, boolean initialize,
 *     ClassLoader loader)
 *
 * Return the Class object associated with the class or interface with
 * the specified name.
 *
 * "name" is in "binary name" format, e.g. "dalvik.system.Debug$1".
 */
static void Dalvik_java_lang_Class_classForName(const u4* args, JValue* pResult)
{
    StringObject* nameObj = (StringObject*) args[0];
    bool initialize = (args[1] != 0);
    Object* loader = (Object*) args[2];

    RETURN_PTR(findClassByName(nameObj, loader, initialize));
}

/*
 * static private ClassLoader getClassLoader(Class clazz)
 *
 * Return the class' defining class loader.
 */
static void Dalvik_java_lang_Class_getClassLoader(const u4* args,
    JValue* pResult)
{
    ClassObject* clazz = (ClassObject*) args[0];

    RETURN_PTR(clazz->classLoader);
}

/*
 * public Class<?> getComponentType()
 *
 * If this is an array type, return the class of the elements; otherwise
 * return NULL.
 */
static void Dalvik_java_lang_Class_getComponentType(const u4* args,
    JValue* pResult)
{
    ClassObject* thisPtr = (ClassObject*) args[0];

    if (!dvmIsArrayClass(thisPtr))
        RETURN_PTR(NULL);

    /*
     * We can't just return thisPtr->elementClass, because that gives
     * us the base type (e.g. X[][][] returns X).  If this is a multi-
     * dimensional array, we have to do the lookup by name.
     */
    if (thisPtr->descriptor[1] == '[')
        RETURN_PTR(dvmFindArrayClass(&thisPtr->descriptor[1],
                   thisPtr->classLoader));
    else
        RETURN_PTR(thisPtr->elementClass);
}

/*
 * private static Class<?>[] getDeclaredClasses(Class<?> clazz,
 *     boolean publicOnly)
 *
 * Return an array with the classes that are declared by the specified class.
 * If "publicOnly" is set, we strip out any classes that don't have "public"
 * access.
 */
static void Dalvik_java_lang_Class_getDeclaredClasses(const u4* args,
    JValue* pResult)
{
    ClassObject* clazz = (ClassObject*) args[0];
    bool publicOnly = (args[1] != 0);
    ArrayObject* classes;

    classes = dvmGetDeclaredClasses(clazz);
    if (classes == NULL) {
        if (!dvmCheckException(dvmThreadSelf())) {
            /* empty list, so create a zero-length array */
            classes = dvmAllocArrayByClass(gDvm.classJavaLangClassArray,
                        0, ALLOC_DEFAULT);
        }
    } else if (publicOnly) {
        int i, newIdx, publicCount = 0;
        ClassObject** pSource = (ClassObject**) classes->contents;

        /* count up public classes */
        for (i = 0; i < (int)classes->length; i++) {
            if (dvmIsPublicClass(pSource[i]))
                publicCount++;
        }

        /* create a new array to hold them */
        ArrayObject* newClasses;
        newClasses = dvmAllocArrayByClass(gDvm.classJavaLangClassArray,
                        publicCount, ALLOC_DEFAULT);

        /* copy them over */
        ClassObject** pDest = (ClassObject**) newClasses->contents;
        for (i = newIdx = 0; i < (int)classes->length; i++) {
            if (dvmIsPublicClass(pSource[i]))
                pDest[newIdx++] = pSource[i];
        }

        assert(newIdx == publicCount);
        dvmReleaseTrackedAlloc((Object*) classes, NULL);
        classes = newClasses;
    }

    dvmReleaseTrackedAlloc((Object*) classes, NULL);
    RETURN_PTR(classes);
}

/*
 * static Constructor[] getDeclaredConstructors(Class clazz, boolean publicOnly)
 *     throws SecurityException
 */
static void Dalvik_java_lang_Class_getDeclaredConstructors(const u4* args,
    JValue* pResult)
{
    ClassObject* clazz = (ClassObject*) args[0];
    bool publicOnly = (args[1] != 0);
    ArrayObject* constructors;

    constructors = dvmGetDeclaredConstructors(clazz, publicOnly);
    dvmReleaseTrackedAlloc((Object*) constructors, NULL);

    RETURN_PTR(constructors);
}

/*
 * static Field[] getDeclaredFields(Class klass, boolean publicOnly)
 *     throws SecurityException
 */
static void Dalvik_java_lang_Class_getDeclaredFields(const u4* args,
    JValue* pResult)
{
    ClassObject* clazz = (ClassObject*) args[0];
    bool publicOnly = (args[1] != 0);
    ArrayObject* fields;

    fields = dvmGetDeclaredFields(clazz, publicOnly);
    dvmReleaseTrackedAlloc((Object*) fields, NULL);

    RETURN_PTR(fields);
}

/*
 * static Method[] getDeclaredMethods(Class clazz, boolean publicOnly)
 *     throws SecurityException
 */
static void Dalvik_java_lang_Class_getDeclaredMethods(const u4* args,
    JValue* pResult)
{
    ClassObject* clazz = (ClassObject*) args[0];
    bool publicOnly = (args[1] != 0);
    ArrayObject* methods;

    methods = dvmGetDeclaredMethods(clazz, publicOnly);
    dvmReleaseTrackedAlloc((Object*) methods, NULL);

    RETURN_PTR(methods);
}

/*
 * Class[] getInterfaces()
 */
static void Dalvik_java_lang_Class_getInterfaces(const u4* args,
    JValue* pResult)
{
    ClassObject* clazz = (ClassObject*) args[0];
    ArrayObject* interfaces;

    interfaces = dvmGetInterfaces(clazz);
    dvmReleaseTrackedAlloc((Object*) interfaces, NULL);

    RETURN_PTR(interfaces);
}

/*
 * private static int getModifiers(Class klass, boolean
 *     ignoreInnerClassesAttrib)
 *
 * Return the class' modifier flags.  If "ignoreInnerClassesAttrib" is false,
 * and this is an inner class, we return the access flags from the inner class
 * attribute.
 */
static void Dalvik_java_lang_Class_getModifiers(const u4* args, JValue* pResult)
{
    ClassObject* clazz = (ClassObject*) args[0];
    bool ignoreInner = args[1];
    u4 accessFlags;

    accessFlags = clazz->accessFlags & JAVA_FLAGS_MASK;

    if (!ignoreInner) {
        /* see if we have an InnerClass annotation with flags in it */
        StringObject* className = NULL;
        int innerFlags;

        if (dvmGetInnerClass(clazz, &className, &innerFlags))
            accessFlags = innerFlags & JAVA_FLAGS_MASK;

        dvmReleaseTrackedAlloc((Object*) className, NULL);
    }

    RETURN_INT(accessFlags);
}

/*
 * public String getName()
 *
 * Return the class' name.
 */
static void Dalvik_java_lang_Class_getName(const u4* args, JValue* pResult)
{
    ClassObject* clazz = (ClassObject*) args[0];
    const char* descriptor = clazz->descriptor;
    StringObject* nameObj;

    if ((descriptor[0] != 'L') && (descriptor[0] != '[')) {
        /*
         * The descriptor indicates that this is the class for
         * a primitive type; special-case the return value.
         */
        const char* name;
        switch (descriptor[0]) {
            case 'Z': name = "boolean"; break;
            case 'B': name = "byte";    break;
            case 'C': name = "char";    break;
            case 'S': name = "short";   break;
            case 'I': name = "int";     break;
            case 'J': name = "long";    break;
            case 'F': name = "float";   break;
            case 'D': name = "double";  break;
            case 'V': name = "void";    break;
            default: {
                LOGE("Unknown primitive type '%c'\n", descriptor[0]);
                assert(false);
                RETURN_PTR(NULL);
            }
        }

        nameObj = dvmCreateStringFromCstr(name, ALLOC_DEFAULT);
    } else {
        /*
         * Convert the UTF-8 name to a java.lang.String. The
         * name must use '.' to separate package components.
         *
         * TODO: this could be more efficient. Consider a custom
         * conversion function here that walks the string once and
         * avoids the allocation for the common case (name less than,
         * say, 128 bytes).
         */
        char* dotName = dvmDescriptorToDot(clazz->descriptor);
        nameObj = dvmCreateStringFromCstr(dotName, ALLOC_DEFAULT);
        free(dotName);
    }

    dvmReleaseTrackedAlloc((Object*) nameObj, NULL);

#if 0
    /* doesn't work -- need "java.lang.String" not "java/lang/String" */
    {
        /*
         * Find the string in the DEX file and use the copy in the intern
         * table if it already exists (else put one there).  Only works
         * for strings in the DEX file, e.g. not arrays.
         *
         * We have to do the class lookup by name in the DEX file because
         * we don't have a DexClassDef pointer in the ClassObject, and it's
         * not worth adding one there just for this.  Should be cheaper
         * to do this than the string-creation above.
         */
        const DexFile* pDexFile = clazz->pDexFile;
        const DexClassDef* pClassDef;
        const DexClassId* pClassId;
        
        pDexFile = clazz->pDexFile;
        pClassDef = dvmDexFindClass(pDexFile, clazz->descriptor);
        pClassId = dvmDexGetClassId(pDexFile, pClassDef->classIdx);
        nameObj = dvmDexGetResolvedString(pDexFile, pClassId->nameIdx);
        if (nameObj == NULL) {
            nameObj = dvmResolveString(clazz, pClassId->nameIdx);
            if (nameObj == NULL)
                LOGW("WARNING: couldn't find string %u for '%s'\n",
                    pClassId->nameIdx, clazz->name);
        }
    }
#endif

    RETURN_PTR(nameObj);
}

/*
 * Return the superclass for instances of this class.
 *
 * If the class represents a java/lang/Object, an interface, a primitive
 * type, or void (which *is* a primitive type??), return NULL.
 *
 * For an array, return the java/lang/Object ClassObject.
 */
static void Dalvik_java_lang_Class_getSuperclass(const u4* args,
    JValue* pResult)
{
    ClassObject* clazz = (ClassObject*) args[0];

    if (dvmIsPrimitiveClass(clazz) || dvmIsInterfaceClass(clazz))
        RETURN_PTR(NULL);
    else
        RETURN_PTR(clazz->super);
}

/*
 * public boolean isAssignableFrom(Class<?> cls)
 *
 * Determine if this class is either the same as, or is a superclass or
 * superinterface of, the class specified in the "cls" parameter.
 */
static void Dalvik_java_lang_Class_isAssignableFrom(const u4* args,
    JValue* pResult)
{
    ClassObject* thisPtr = (ClassObject*) args[0];
    ClassObject* testClass = (ClassObject*) args[1];

    if (testClass == NULL) {
        dvmThrowException("Ljava/lang/NullPointerException;", NULL);
        RETURN_INT(false);
    }
    RETURN_INT(dvmInstanceof(testClass, thisPtr));
}

/*
 * public boolean isInstance(Object o)
 *
 * Dynamic equivalent of Java programming language "instanceof".
 */
static void Dalvik_java_lang_Class_isInstance(const u4* args,
    JValue* pResult)
{
    ClassObject* thisPtr = (ClassObject*) args[0];
    Object* testObj = (Object*) args[1];

    if (testObj == NULL)
        RETURN_INT(false);
    RETURN_INT(dvmInstanceof(testObj->clazz, thisPtr));
}

/*
 * public boolean isInterface()
 */
static void Dalvik_java_lang_Class_isInterface(const u4* args,
    JValue* pResult)
{
    ClassObject* thisPtr = (ClassObject*) args[0];

    RETURN_INT(dvmIsInterfaceClass(thisPtr));
}

/*
 * public boolean isPrimitive()
 */
static void Dalvik_java_lang_Class_isPrimitive(const u4* args,
    JValue* pResult)
{
    ClassObject* thisPtr = (ClassObject*) args[0];

    RETURN_INT(dvmIsPrimitiveClass(thisPtr));
}

/*
 * public T newInstance() throws InstantiationException, IllegalAccessException
 *
 * Create a new instance of this class.
 */
static void Dalvik_java_lang_Class_newInstance(const u4* args, JValue* pResult)
{
    Thread* self = dvmThreadSelf();
    ClassObject* clazz = (ClassObject*) args[0];
    Method* init;
    Object* newObj;

    /* can't instantiate these */
    if (dvmIsPrimitiveClass(clazz) || dvmIsInterfaceClass(clazz)
        || dvmIsArrayClass(clazz) || dvmIsAbstractClass(clazz))
    {
        LOGD("newInstance failed: p%d i%d [%d a%d\n",
            dvmIsPrimitiveClass(clazz), dvmIsInterfaceClass(clazz),
            dvmIsArrayClass(clazz), dvmIsAbstractClass(clazz));
        dvmThrowExceptionWithClassMessage("Ljava/lang/InstantiationException;",
            clazz->descriptor);
        RETURN_VOID();
    }

    /* initialize the class if it hasn't been already */
    if (!dvmIsClassInitialized(clazz)) {
        if (!dvmInitClass(clazz)) {
            LOGW("Class init failed in newInstance call (%s)\n",
                clazz->descriptor);
            assert(dvmCheckException(self));
            RETURN_VOID();
        }
    }

    /* find the "nullary" constructor */
    init = dvmFindDirectMethodByDescriptor(clazz, "<init>", "()V");
    if (init == NULL) {
        /* common cause: secret "this" arg on non-static inner class ctor */
        LOGD("newInstance failed: no <init>()\n");
        dvmThrowExceptionWithClassMessage("Ljava/lang/InstantiationException;",
            clazz->descriptor);
        RETURN_VOID();
    }

    /*
     * Verify access from the call site.
     *
     * First, make sure the method invoking Class.newInstance() has permission
     * to access the class.
     *
     * Second, make sure it has permission to invoke the constructor.  The
     * constructor must be public or, if the caller is in the same package,
     * have package scope.
     */
    ClassObject* callerClass = dvmGetCallerClass(self->curFrame);

    if (!dvmCheckClassAccess(callerClass, clazz)) {
        LOGD("newInstance failed: %s not accessible to %s\n",
            clazz->descriptor, callerClass->descriptor);
        dvmThrowException("Ljava/lang/IllegalAccessException;",
            "access to class not allowed");
        RETURN_VOID();
    }
    if (!dvmCheckMethodAccess(callerClass, init)) {
        LOGD("newInstance failed: %s.<init>() not accessible to %s\n",
            clazz->descriptor, callerClass->descriptor);
        dvmThrowException("Ljava/lang/IllegalAccessException;",
            "access to constructor not allowed");
        RETURN_VOID();
    }

    newObj = dvmAllocObject(clazz, ALLOC_DEFAULT);
    JValue unused;

    /* invoke constructor; unlike reflection calls, we don't wrap exceptions */
    dvmCallMethod(self, init, newObj, &unused);
    dvmReleaseTrackedAlloc(newObj, NULL);

    RETURN_PTR(newObj);
}

/*
 * private Object[] getSignatureAnnotation()
 *
 * Returns the signature annotation array.
 */
static void Dalvik_java_lang_Class_getSignatureAnnotation(const u4* args,
    JValue* pResult)
{
    ClassObject* clazz = (ClassObject*) args[0];
    ArrayObject* arr = dvmGetClassSignatureAnnotation(clazz);

    dvmReleaseTrackedAlloc((Object*) arr, NULL);
    RETURN_PTR(arr);
}

/*
 * public Class getDeclaringClass()
 *
 * Get the class that encloses this class (if any).
 */
static void Dalvik_java_lang_Class_getDeclaringClass(const u4* args,
    JValue* pResult)
{
    ClassObject* clazz = (ClassObject*) args[0];

    ClassObject* enclosing = dvmGetDeclaringClass(clazz);
    dvmReleaseTrackedAlloc((Object*) enclosing, NULL);
    RETURN_PTR(enclosing);
}

/*
 * public Class getEnclosingClass()
 *
 * Get the class that encloses this class (if any).
 */
static void Dalvik_java_lang_Class_getEnclosingClass(const u4* args,
    JValue* pResult)
{
    ClassObject* clazz = (ClassObject*) args[0];

    ClassObject* enclosing = dvmGetEnclosingClass(clazz);
    dvmReleaseTrackedAlloc((Object*) enclosing, NULL);
    RETURN_PTR(enclosing);
}

/*
 * public Constructor getEnclosingConstructor()
 *
 * Get the constructor that encloses this class (if any).
 */
static void Dalvik_java_lang_Class_getEnclosingConstructor(const u4* args,
    JValue* pResult)
{
    ClassObject* clazz = (ClassObject*) args[0];

    Object* enclosing = dvmGetEnclosingMethod(clazz);
    if (enclosing != NULL) {
        dvmReleaseTrackedAlloc(enclosing, NULL);
        if (enclosing->clazz == gDvm.classJavaLangReflectConstructor) {
            RETURN_PTR(enclosing);
        }
        assert(enclosing->clazz == gDvm.classJavaLangReflectMethod);
    }
    RETURN_PTR(NULL);
}

/*
 * public Method getEnclosingMethod()
 *
 * Get the method that encloses this class (if any).
 */
static void Dalvik_java_lang_Class_getEnclosingMethod(const u4* args,
    JValue* pResult)
{
    ClassObject* clazz = (ClassObject*) args[0];

    Object* enclosing = dvmGetEnclosingMethod(clazz);
    if (enclosing != NULL) {
        dvmReleaseTrackedAlloc(enclosing, NULL);
        if (enclosing->clazz == gDvm.classJavaLangReflectMethod) {
            RETURN_PTR(enclosing);
        }
        assert(enclosing->clazz == gDvm.classJavaLangReflectConstructor);
    }
    RETURN_PTR(NULL);
}

#if 0
static void Dalvik_java_lang_Class_getGenericInterfaces(const u4* args,
    JValue* pResult)
{
    dvmThrowException("Ljava/lang/UnsupportedOperationException;",
        "native method not implemented");

    RETURN_PTR(NULL);
}

static void Dalvik_java_lang_Class_getGenericSuperclass(const u4* args,
    JValue* pResult)
{
    dvmThrowException("Ljava/lang/UnsupportedOperationException;",
        "native method not implemented");

    RETURN_PTR(NULL);
}

static void Dalvik_java_lang_Class_getTypeParameters(const u4* args,
    JValue* pResult)
{
    dvmThrowException("Ljava/lang/UnsupportedOperationException;",
        "native method not implemented");

    RETURN_PTR(NULL);
}
#endif

/*
 * public boolean isAnonymousClass()
 *
 * Returns true if this is an "anonymous" class.
 */
static void Dalvik_java_lang_Class_isAnonymousClass(const u4* args,
    JValue* pResult)
{
    ClassObject* clazz = (ClassObject*) args[0];
    StringObject* className = NULL;
    int accessFlags;

    /*
     * If this has an InnerClass annotation, pull it out.  Lack of the
     * annotation, or an annotation with a NULL class name, indicates
     * that this is an anonymous inner class.
     */
    if (!dvmGetInnerClass(clazz, &className, &accessFlags))
        RETURN_BOOLEAN(false);

    dvmReleaseTrackedAlloc((Object*) className, NULL);
    RETURN_BOOLEAN(className == NULL);
}

/*
 * private Annotation[] getDeclaredAnnotations()
 *
 * Return the annotations declared on this class.
 */
static void Dalvik_java_lang_Class_getDeclaredAnnotations(const u4* args,
    JValue* pResult)
{
    ClassObject* clazz = (ClassObject*) args[0];

    ArrayObject* annos = dvmGetClassAnnotations(clazz);
    dvmReleaseTrackedAlloc((Object*) annos, NULL);
    RETURN_PTR(annos);
}

/*
 * public String getInnerClassName()
 *
 * Returns the simple name of a member class or local class, or null otherwise. 
 */
static void Dalvik_java_lang_Class_getInnerClassName(const u4* args,
    JValue* pResult)
{
    ClassObject* clazz = (ClassObject*) args[0];
    StringObject* nameObj;
    int flags;
    
    if (dvmGetInnerClass(clazz, &nameObj, &flags)) {
        dvmReleaseTrackedAlloc((Object*) nameObj, NULL);
        RETURN_PTR(nameObj);
    } else {
        RETURN_PTR(NULL);
    }
}

/*
 * static native void setAccessibleNoCheck(AccessibleObject ao, boolean flag);
 */
static void Dalvik_java_lang_Class_setAccessibleNoCheck(const u4* args,
    JValue* pResult)
{
    Object* target = (Object*) args[0];
    u4 flag = (u4) args[1];

    dvmSetFieldBoolean(target, gDvm.offJavaLangReflectAccessibleObject_flag,
            flag);
}

static const DalvikNativeMethod java_lang_Class[] = {
    { "desiredAssertionStatus", "()Z",
        Dalvik_java_lang_Class_desiredAssertionStatus },
    { "classForName",           "(Ljava/lang/String;ZLjava/lang/ClassLoader;)Ljava/lang/Class;",
        Dalvik_java_lang_Class_classForName },
    { "getClassLoader",         "(Ljava/lang/Class;)Ljava/lang/ClassLoader;",
        Dalvik_java_lang_Class_getClassLoader },
    { "getComponentType",       "()Ljava/lang/Class;",
        Dalvik_java_lang_Class_getComponentType },
    { "getSignatureAnnotation",  "()[Ljava/lang/Object;",
        Dalvik_java_lang_Class_getSignatureAnnotation },
    { "getDeclaredClasses",     "(Ljava/lang/Class;Z)[Ljava/lang/Class;",
        Dalvik_java_lang_Class_getDeclaredClasses },
    { "getDeclaredConstructors", "(Ljava/lang/Class;Z)[Ljava/lang/reflect/Constructor;",
        Dalvik_java_lang_Class_getDeclaredConstructors },
    { "getDeclaredFields",      "(Ljava/lang/Class;Z)[Ljava/lang/reflect/Field;",
        Dalvik_java_lang_Class_getDeclaredFields },
    { "getDeclaredMethods",     "(Ljava/lang/Class;Z)[Ljava/lang/reflect/Method;",
        Dalvik_java_lang_Class_getDeclaredMethods },
    { "getInterfaces",          "()[Ljava/lang/Class;",
        Dalvik_java_lang_Class_getInterfaces },
    { "getModifiers",           "(Ljava/lang/Class;Z)I",
        Dalvik_java_lang_Class_getModifiers },
    { "getName",                "()Ljava/lang/String;",
        Dalvik_java_lang_Class_getName },
    { "getSuperclass",          "()Ljava/lang/Class;",
        Dalvik_java_lang_Class_getSuperclass },
    { "isAssignableFrom",       "(Ljava/lang/Class;)Z",
        Dalvik_java_lang_Class_isAssignableFrom },
    { "isInstance",             "(Ljava/lang/Object;)Z",
        Dalvik_java_lang_Class_isInstance },
    { "isInterface",            "()Z",
        Dalvik_java_lang_Class_isInterface },
    { "isPrimitive",            "()Z",
        Dalvik_java_lang_Class_isPrimitive },
    { "newInstance",            "()Ljava/lang/Object;",
        Dalvik_java_lang_Class_newInstance },
    { "getDeclaringClass",      "()Ljava/lang/Class;",
        Dalvik_java_lang_Class_getDeclaringClass },
    { "getEnclosingClass",      "()Ljava/lang/Class;",
        Dalvik_java_lang_Class_getEnclosingClass },
    { "getEnclosingConstructor", "()Ljava/lang/reflect/Constructor;",
        Dalvik_java_lang_Class_getEnclosingConstructor },
    { "getEnclosingMethod",     "()Ljava/lang/reflect/Method;",
        Dalvik_java_lang_Class_getEnclosingMethod },
#if 0
    { "getGenericInterfaces",   "()[Ljava/lang/reflect/Type;",
        Dalvik_java_lang_Class_getGenericInterfaces },
    { "getGenericSuperclass",   "()Ljava/lang/reflect/Type;",
        Dalvik_java_lang_Class_getGenericSuperclass },
    { "getTypeParameters",      "()Ljava/lang/reflect/TypeVariable;",
        Dalvik_java_lang_Class_getTypeParameters },
#endif
    { "isAnonymousClass",       "()Z",
        Dalvik_java_lang_Class_isAnonymousClass },
    { "getDeclaredAnnotations", "()[Ljava/lang/annotation/Annotation;",
        Dalvik_java_lang_Class_getDeclaredAnnotations },
    { "getInnerClassName",       "()Ljava/lang/String;",
        Dalvik_java_lang_Class_getInnerClassName },
    { "setAccessibleNoCheck",   "(Ljava/lang/reflect/AccessibleObject;Z)V",
        Dalvik_java_lang_Class_setAccessibleNoCheck },
    { NULL, NULL, NULL },
};


/*
 * ===========================================================================
 *      java.lang.System
 * ===========================================================================
 */

/*
 * public static void arraycopy(Object src, int srcPos, Object dest,
 *      int destPos, int length)
 *
 * The description of this function is long, and describes a multitude
 * of checks and exceptions.
 */
static void Dalvik_java_lang_System_arraycopy(const u4* args, JValue* pResult)
{
    void* (*copyFunc)(void *dest, const void *src, size_t n);
    ArrayObject* srcArray;
    ArrayObject* dstArray;
    ClassObject* srcClass;
    ClassObject* dstClass;
    int srcPos, dstPos, length;
    char srcType, dstType;
    bool srcPrim, dstPrim;

    srcArray = (ArrayObject*) args[0];
    srcPos = args[1];
    dstArray = (ArrayObject*) args[2];
    dstPos = args[3];
    length = args[4];

    if (srcArray == dstArray)
        copyFunc = memmove;         /* might overlap */
    else
        copyFunc = memcpy;          /* can't overlap, use faster func */

    /* check for null or bad pointer */
    if (!dvmValidateObject((Object*)srcArray) ||
        !dvmValidateObject((Object*)dstArray))
    {
        assert(dvmCheckException(dvmThreadSelf()));
        RETURN_VOID();
    }
    /* make sure it's an array */
    if (!dvmIsArray(srcArray) || !dvmIsArray(dstArray)) {
        dvmThrowException("Ljava/lang/ArrayStoreException;", NULL);
        RETURN_VOID();
    }

    if (srcPos < 0 || dstPos < 0 || length < 0 ||
        srcPos + length > (int) srcArray->length ||
        dstPos + length > (int) dstArray->length)
    {
        dvmThrowException("Ljava/lang/IndexOutOfBoundsException;", NULL);
        RETURN_VOID();
    }

    srcClass = srcArray->obj.clazz;
    dstClass = dstArray->obj.clazz;
    srcType = srcClass->descriptor[1];
    dstType = dstClass->descriptor[1];

    /*
     * If one of the arrays holds a primitive type, the other array must
     * hold the same type.
     */
    srcPrim = (srcType != '[' && srcType != 'L');
    dstPrim = (dstType != '[' && dstType != 'L');
    if (srcPrim || dstPrim) {
        int width;

        if (srcPrim != dstPrim || srcType != dstType) {
            dvmThrowException("Ljava/lang/ArrayStoreException;", NULL);
            RETURN_VOID();
        }

        switch (srcClass->descriptor[1]) {
        case 'B':
        case 'Z':
            width = 1;
            break;
        case 'C':
        case 'S':
            width = 2;
            break;
        case 'F':
        case 'I':
            width = 4;
            break;
        case 'D':
        case 'J':
            width = 8;
            break;
        default:        /* 'V' or something weird */
            LOGE("Weird array type '%s'\n", srcClass->descriptor);
            assert(false);
            width = 0;
            break;
        }

        if (false) LOGVV("arraycopy prim dst=%p %d src=%p %d len=%d\n",
                dstArray->contents, dstPos * width,
                srcArray->contents, srcPos * width,
                length * width);
        (*copyFunc)((u1*)dstArray->contents + dstPos * width,
                (const u1*)srcArray->contents + srcPos * width,
                length * width);
    } else {
        /*
         * Neither class is primitive.  See if elements in "src" are instances
         * of elements in "dst" (e.g. copy String to String or String to
         * Object).
         */
        int width = sizeof(Object*);

        if (srcClass->arrayDim == dstClass->arrayDim &&
            dvmInstanceof(srcClass, dstClass))
        {
            /*
             * "dst" can hold "src"; copy the whole thing.
             */
            if (false) LOGVV("arraycopy ref dst=%p %d src=%p %d len=%d\n",
                dstArray->contents, dstPos * width,
                srcArray->contents, srcPos * width,
                length * width);
            (*copyFunc)((u1*)dstArray->contents + dstPos * width,
                    (const u1*)srcArray->contents + srcPos * width,
                    length * width);
        } else {
            /*
             * The arrays are not fundamentally compatible.  However, we may
             * still be able to do this if the destination object is compatible
             * (e.g. copy Object to String, but the Object being copied is
             * actually a String).  We need to copy elements one by one until
             * something goes wrong.
             *
             * Because of overlapping moves, what we really want to do is
             * compare the types and count up how many we can move, then call
             * memmove() to shift the actual data.  If we just start from the
             * front we could do a smear rather than a move.
             */
            Object** srcObj;
            Object** dstObj;
            int copyCount;
            ClassObject*   clazz = NULL;

            srcObj = ((Object**) srcArray->contents) + srcPos;
            dstObj = ((Object**) dstArray->contents) + dstPos;

            if (length > 0 && srcObj[0] != NULL)
            {
                clazz = srcObj[0]->clazz;
                if (!dvmCanPutArrayElement(clazz, dstClass))
                    clazz = NULL;
            }

            for (copyCount = 0; copyCount < length; copyCount++)
            {
                if (srcObj[copyCount] != NULL &&
                    srcObj[copyCount]->clazz != clazz &&
                    !dvmCanPutArrayElement(srcObj[copyCount]->clazz, dstClass))
                {
                    /* can't put this element into the array */
                    break;
                }
            }

            if (false) LOGVV("arraycopy iref dst=%p %d src=%p %d count=%d of %d\n",
                dstArray->contents, dstPos * width,
                srcArray->contents, srcPos * width,
                copyCount, length);
            (*copyFunc)((u1*)dstArray->contents + dstPos * width,
                    (const u1*)srcArray->contents + srcPos * width,
                    copyCount * width);

            if (copyCount != length) {
                dvmThrowException("Ljava/lang/ArrayStoreException;", NULL);
                RETURN_VOID();
            }
        }
    }

    RETURN_VOID();
}

/*
 * static long currentTimeMillis()
 *
 * Current time, in miliseconds.  This doesn't need to be internal to the
 * VM, but we're already handling java.lang.System here.
 */
static void Dalvik_java_lang_System_currentTimeMillis(const u4* args,
    JValue* pResult)
{
    struct timeval tv;

    UNUSED_PARAMETER(args);

    gettimeofday(&tv, (struct timezone *) NULL);
    long long when = tv.tv_sec * 1000LL + tv.tv_usec / 1000;

    RETURN_LONG(when);
}

/*
 * static long nanoTime()
 *
 * Current monotonically-increasing time, in nanoseconds.  This doesn't
 * need to be internal to the VM, but we're already handling
 * java.lang.System here.
 */
static void Dalvik_java_lang_System_nanoTime(const u4* args, JValue* pResult)
{
    UNUSED_PARAMETER(args);

    u8 when = dvmGetRelativeTimeNsec();
    RETURN_LONG(when);
}

/*
 * static int identityHashCode(Object x)
 *
 * Returns that hash code that the default hashCode()
 * method would return for "x", even if "x"s class
 * overrides hashCode().
 */
static void Dalvik_java_lang_System_identityHashCode(const u4* args,
    JValue* pResult)
{
    /* This is a static method, which means args[0] is the Object.
     * Passing the same args to Object.hashCode will work because
     * it treats the first arg as the "this" pointer.
     */
    Dalvik_java_lang_Object_hashCode(args, pResult);
}

/*
 * public static String mapLibraryName(String libname)
 */
static void Dalvik_java_lang_System_mapLibraryName(const u4* args,
    JValue* pResult)
{
    StringObject* nameObj = (StringObject*) args[0];
    StringObject* result = NULL;
    char* name;
    char* mappedName;

    if (nameObj == NULL) {
        dvmThrowException("Ljava/lang/NullPointerException;", NULL);
        RETURN_VOID();
    }

    name = dvmCreateCstrFromString(nameObj);
    mappedName = dvmCreateSystemLibraryName(name);
    if (mappedName != NULL) {
        result = dvmCreateStringFromCstr(mappedName, ALLOC_DEFAULT);
        dvmReleaseTrackedAlloc((Object*) result, NULL);
    }

    free(name);
    free(mappedName);
    RETURN_PTR(result);
}

static const DalvikNativeMethod java_lang_System[] = {
    { "arraycopy",          "(Ljava/lang/Object;ILjava/lang/Object;II)V",
        Dalvik_java_lang_System_arraycopy },
    { "currentTimeMillis",  "()J",
        Dalvik_java_lang_System_currentTimeMillis },
    { "nanoTime",  "()J",
        Dalvik_java_lang_System_nanoTime },
    { "identityHashCode",  "(Ljava/lang/Object;)I",
        Dalvik_java_lang_System_identityHashCode },
    { "mapLibraryName",     "(Ljava/lang/String;)Ljava/lang/String;",
        Dalvik_java_lang_System_mapLibraryName },
    { NULL, NULL, NULL },
};


/*
 * ===========================================================================
 *      java.lang.SystemProperties
 * ===========================================================================
 */

/*
 * Expected call sequence:
 *  (1) call SystemProperties.preInit() to get VM defaults
 *  (2) set any higher-level defaults
 *  (3) call SystemProperties.postInit() to get command-line overrides
 * This currently happens the first time somebody tries to access a property.
 *
 * SystemProperties is a Dalvik-specific package-scope class.
 */

/*
 * void preInit()
 *
 * Tells the VM to populate the properties table with VM defaults.
 */
static void Dalvik_java_lang_SystemProperties_preInit(const u4* args,
    JValue* pResult)
{
    dvmCreateDefaultProperties((Object*) args[0]);
    RETURN_VOID();
}

/*
 * void postInit()
 *
 * Tells the VM to update properties with values from the command line.
 */
static void Dalvik_java_lang_SystemProperties_postInit(const u4* args,
    JValue* pResult)
{
    dvmSetCommandLineProperties((Object*) args[0]);
    RETURN_VOID();
}

static const DalvikNativeMethod java_lang_SystemProperties[] = {
    { "preInit",            "()V",
        Dalvik_java_lang_SystemProperties_preInit },
    { "postInit",           "()V",
        Dalvik_java_lang_SystemProperties_postInit },
    { NULL, NULL, NULL },
};


/*
 * ===========================================================================
 *      java.lang.Runtime
 * ===========================================================================
 */

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
    exit(status);
}

/*
 * static boolean nativeLoad(String filename, ClassLoader loader)
 *
 * Load the specified full path as a dynamic library filled with
 * JNI-compatible methods.
 */
static void Dalvik_java_lang_Runtime_nativeLoad(const u4* args,
    JValue* pResult)
{
    StringObject* fileNameObj = (StringObject*) args[0];
    Object* classLoader = (Object*) args[1];
    char* fileName;
    int result;

    if (fileNameObj == NULL)
        RETURN_INT(false);
    fileName = dvmCreateCstrFromString(fileNameObj);

    result = dvmLoadNativeCode(fileName, classLoader);

    free(fileName);
    RETURN_INT(result);
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

static const DalvikNativeMethod java_lang_Runtime[] = {
    { "freeMemory",          "()J",
        Dalvik_java_lang_Runtime_freeMemory },
    { "gc",                 "()V",
        Dalvik_java_lang_Runtime_gc },
    { "maxMemory",          "()J",
        Dalvik_java_lang_Runtime_maxMemory },
    { "nativeExit",         "(IZ)V",
        Dalvik_java_lang_Runtime_nativeExit },
    { "nativeLoad",         "(Ljava/lang/String;Ljava/lang/ClassLoader;)Z",
        Dalvik_java_lang_Runtime_nativeLoad },
    { "runFinalization",    "(Z)V",
        Dalvik_java_lang_Runtime_runFinalization },
    { "totalMemory",          "()J",
        Dalvik_java_lang_Runtime_totalMemory },
    { NULL, NULL, NULL },
};


/*
 * ===========================================================================
 *      java.lang.String
 * ===========================================================================
 */

/*
 * public String intern()
 *
 * Intern a string in the VM string table.
 */
static void Dalvik_java_lang_String_intern(const u4* args, JValue* pResult)
{
    StringObject* str = (StringObject*) args[0];
    StringObject* interned;

    interned = dvmLookupInternedString(str);
    RETURN_PTR(interned);
}

static const DalvikNativeMethod java_lang_String[] = {
    { "intern",             "()Ljava/lang/String;",
        Dalvik_java_lang_String_intern },
    { NULL, NULL, NULL },
};


/*
 * ===========================================================================
 *      java.lang.reflect.AccessibleObject
 * ===========================================================================
 */

/*
 * private static Object[] getClassSignatureAnnotation(Class clazz)
 *
 * Return the Signature annotation for the specified class.  Equivalent to
 * Class.getSignatureAnnotation(), but available to java.lang.reflect.
 */
static void Dalvik_java_lang_reflect_AccessibleObject_getClassSignatureAnnotation(
    const u4* args, JValue* pResult)
{
    ClassObject* clazz = (ClassObject*) args[0];
    ArrayObject* arr = dvmGetClassSignatureAnnotation(clazz);

    dvmReleaseTrackedAlloc((Object*) arr, NULL);
    RETURN_PTR(arr);
}

static const DalvikNativeMethod java_lang_reflect_AccessibleObject[] = {
    { "getClassSignatureAnnotation", "(Ljava/lang/Class;)[Ljava/lang/Object;",
      Dalvik_java_lang_reflect_AccessibleObject_getClassSignatureAnnotation },
    { NULL, NULL, NULL },
};


/*
 * ===========================================================================
 *      java.lang.reflect.Array
 * ===========================================================================
 */

/*
 * private static Object createObjectArray(Class<?> componentType,
 *     int length) throws NegativeArraySizeException;
 *
 * Create a one-dimensional array of Objects.
 */
static void Dalvik_java_lang_reflect_Array_createObjectArray(const u4* args,
    JValue* pResult)
{
    ClassObject* elementClass = (ClassObject*) args[0];
    int length = args[1];
    ArrayObject* newArray;

    assert(elementClass != NULL);       // tested by caller
    if (length < 0) {
        dvmThrowException("Ljava/lang/NegativeArraySizeException;", NULL);
        RETURN_VOID();
    }

    newArray = dvmAllocObjectArray(elementClass, length, ALLOC_DEFAULT);
    if (newArray == NULL) {
        assert(dvmCheckException(dvmThreadSelf()));
        RETURN_VOID();
    }
    dvmReleaseTrackedAlloc((Object*) newArray, NULL);

    RETURN_PTR(newArray);
}

/*
 * private static Object createMultiArray(Class<?> componentType,
 *     int[] dimensions) throws NegativeArraySizeException;
 *
 * Create a multi-dimensional array of Objects or primitive types.
 *
 * We have to generate the names for X[], X[][], X[][][], and so on.  The
 * easiest way to deal with that is to create the full name once and then
 * subtract pieces off.  Besides, we want to start with the outermost
 * piece and work our way in.
 */
static void Dalvik_java_lang_reflect_Array_createMultiArray(const u4* args,
    JValue* pResult)
{
    static const char kPrimLetter[] = PRIM_TYPE_TO_LETTER;
    ClassObject* elementClass = (ClassObject*) args[0];
    ArrayObject* dimArray = (ArrayObject*) args[1];
    ClassObject* arrayClass;
    ArrayObject* newArray;
    char* acDescriptor;
    int numDim, i;
    int* dimensions;

    LOGV("createMultiArray: '%s' [%d]\n",
        elementClass->descriptor, dimArray->length);

    assert(elementClass != NULL);       // verified by caller

    /*
     * Verify dimensions.
     *
     * The caller is responsible for verifying that "dimArray" is non-null
     * and has a length > 0 and <= 255.
     */
    assert(dimArray != NULL);           // verified by caller
    numDim = dimArray->length;
    assert(numDim > 0 && numDim <= 255);

    dimensions = (int*) dimArray->contents;
    for (i = 0; i < numDim; i++) {
        if (dimensions[i] < 0) {
            dvmThrowException("Ljava/lang/NegativeArraySizeException;", NULL);
            RETURN_VOID();
        }
        LOGVV("DIM %d: %d\n", i, dimensions[i]);
    }

    /*
     * Generate the full name of the array class.
     */
    acDescriptor =
        (char*) malloc(strlen(elementClass->descriptor) + numDim + 1);
    memset(acDescriptor, '[', numDim);

    LOGVV("#### element name = '%s'\n", elementClass->descriptor);
    if (dvmIsPrimitiveClass(elementClass)) {
        assert(elementClass->primitiveType >= 0);
        acDescriptor[numDim] = kPrimLetter[elementClass->primitiveType];
        acDescriptor[numDim+1] = '\0';
    } else {
        strcpy(acDescriptor+numDim, elementClass->descriptor);
    }
    LOGVV("#### array name = '%s'\n", acDescriptor);

    /*
     * Find/generate the array class.
     */
    arrayClass = dvmFindArrayClass(acDescriptor, elementClass->classLoader);
    if (arrayClass == NULL) {
        LOGW("Unable to find or generate array class '%s'\n", acDescriptor);
        assert(dvmCheckException(dvmThreadSelf()));
        free(acDescriptor);
        RETURN_VOID();
    }
    free(acDescriptor);

    /* create the array */
    newArray = dvmAllocMultiArray(arrayClass, numDim-1, dimensions);
    if (newArray == NULL) {
        assert(dvmCheckException(dvmThreadSelf()));
        RETURN_VOID();
    }

    dvmReleaseTrackedAlloc((Object*) newArray, NULL);
    RETURN_PTR(newArray);
}

static const DalvikNativeMethod java_lang_reflect_Array[] = {
    { "createObjectArray",  "(Ljava/lang/Class;I)Ljava/lang/Object;",
        Dalvik_java_lang_reflect_Array_createObjectArray },
    { "createMultiArray",   "(Ljava/lang/Class;[I)Ljava/lang/Object;",
        Dalvik_java_lang_reflect_Array_createMultiArray },
    { NULL, NULL, NULL },
};


/*
 * ===========================================================================
 *      java.lang.reflect.Constructor
 * ===========================================================================
 */

/*
 * public int getConstructorModifiers(Class declaringClass, int slot)
 */
static void Dalvik_java_lang_reflect_Constructor_getConstructorModifiers(
    const u4* args, JValue* pResult)
{
    // ignore thisPtr in args[0]
    ClassObject* declaringClass = (ClassObject*) args[1];
    int slot = args[2];
    Method* meth;

    meth = dvmSlotToMethod(declaringClass, slot);
    RETURN_INT(fixMethodFlags(meth->accessFlags));
}

/*
 * public int constructNative(Object[] args, Class declaringClass,
 *     Class[] parameterTypes, int slot, boolean noAccessCheck)
 */
static void Dalvik_java_lang_reflect_Constructor_constructNative(
    const u4* args, JValue* pResult)
{
    // ignore thisPtr in args[0]
    ArrayObject* argList = (ArrayObject*) args[1];
    ClassObject* declaringClass = (ClassObject*) args[2];
    ArrayObject* params = (ArrayObject*) args[3];
    int slot = args[4];
    bool noAccessCheck = (args[5] != 0);
    Object* newObj;
    Method* meth;

    newObj = dvmAllocObject(declaringClass, ALLOC_DEFAULT);
    if (newObj == NULL)
        RETURN_PTR(NULL);

    meth = dvmSlotToMethod(declaringClass, slot);
    assert(meth != NULL);

    (void) dvmInvokeMethod(newObj, meth, argList, params, NULL, noAccessCheck);
    dvmReleaseTrackedAlloc(newObj, NULL);
    RETURN_PTR(newObj);
}

/*
 * public Annotation[] getDeclaredAnnotations(Class declaringClass, int slot)
 *
 * Return the annotations declared for this constructor.
 */
static void Dalvik_java_lang_reflect_Constructor_getDeclaredAnnotations(
    const u4* args, JValue* pResult)
{
    // ignore thisPtr in args[0]
    ClassObject* declaringClass = (ClassObject*) args[1];
    int slot = args[2];
    Method* meth;

    meth = dvmSlotToMethod(declaringClass, slot);
    assert(meth != NULL);

    ArrayObject* annos = dvmGetMethodAnnotations(meth);
    dvmReleaseTrackedAlloc((Object*)annos, NULL);
    RETURN_PTR(annos);
}

/*
 * public Annotation[][] getParameterAnnotations(Class declaringClass, int slot)
 *
 * Return the annotations declared for this constructor's parameters.
 */
static void Dalvik_java_lang_reflect_Constructor_getParameterAnnotations(
    const u4* args, JValue* pResult)
{
    // ignore thisPtr in args[0]
    ClassObject* declaringClass = (ClassObject*) args[1];
    int slot = args[2];
    Method* meth;

    meth = dvmSlotToMethod(declaringClass, slot);
    assert(meth != NULL);

    ArrayObject* annos = dvmGetParameterAnnotations(meth);
    dvmReleaseTrackedAlloc((Object*)annos, NULL);
    RETURN_PTR(annos);
}

/*
 * private Object[] getSignatureAnnotation()
 *
 * Returns the signature annotation.
 */
static void Dalvik_java_lang_reflect_Constructor_getSignatureAnnotation(
    const u4* args, JValue* pResult)
{
    // ignore thisPtr in args[0]
    ClassObject* declaringClass = (ClassObject*) args[1];
    int slot = args[2];
    Method* meth;

    meth = dvmSlotToMethod(declaringClass, slot);
    assert(meth != NULL);

    ArrayObject* arr = dvmGetMethodSignatureAnnotation(meth);
    dvmReleaseTrackedAlloc((Object*) arr, NULL);
    RETURN_PTR(arr);
}

static const DalvikNativeMethod java_lang_reflect_Constructor[] = {
    { "constructNative",    "([Ljava/lang/Object;Ljava/lang/Class;[Ljava/lang/Class;IZ)Ljava/lang/Object;",
        Dalvik_java_lang_reflect_Constructor_constructNative },
    { "getConstructorModifiers", "(Ljava/lang/Class;I)I",
        Dalvik_java_lang_reflect_Constructor_getConstructorModifiers },
    { "getDeclaredAnnotations", "(Ljava/lang/Class;I)[Ljava/lang/annotation/Annotation;",
        Dalvik_java_lang_reflect_Constructor_getDeclaredAnnotations },
    { "getParameterAnnotations", "(Ljava/lang/Class;I)[[Ljava/lang/annotation/Annotation;",
        Dalvik_java_lang_reflect_Constructor_getParameterAnnotations },
    { "getSignatureAnnotation",  "(Ljava/lang/Class;I)[Ljava/lang/Object;",
        Dalvik_java_lang_reflect_Constructor_getSignatureAnnotation },
    { NULL, NULL, NULL },
};


/*
 * ===========================================================================
 *      java.lang.reflect.Field
 * ===========================================================================
 */

/*
 * Get the address of a field from an object.  This can be used with "get"
 * or "set".
 *
 * "declaringClass" is the class in which the field was declared.  For an
 * instance field, "obj" is the object that holds the field data; for a
 * static field its value is ignored.
 *
 * "If the underlying field is static, the class that declared the
 * field is initialized if it has not already been initialized."
 *
 * On failure, throws an exception and returns NULL.
 *
 * The documentation lists exceptional conditions and the exceptions that
 * should be thrown, but doesn't say which exception previals when two or
 * more exceptional conditions exist at the same time.  For example,
 * attempting to set a protected field from an unrelated class causes an
 * IllegalAccessException, while passing in a data type that doesn't match
 * the field causes an IllegalArgumentException.  If code does both at the
 * same time, we have to choose one or othe other.
 *
 * The expected order is:
 *  (1) Check for illegal access. Throw IllegalAccessException.
 *  (2) Make sure the object actually has the field.  Throw
 *      IllegalArgumentException.
 *  (3) Make sure the field matches the expected type, e.g. if we issued
 *      a "getInteger" call make sure the field is an integer or can be
 *      converted to an int with a widening conversion.  Throw
 *      IllegalArgumentException.
 *  (4) Make sure "obj" is not null.  Throw NullPointerException.
 *
 * TODO: we're currently handling #3 after #4, because we don't check the
 * widening conversion until we're actually extracting the value from the
 * object (which won't work well if it's a null reference).
 */
static JValue* getFieldDataAddr(Object* obj, ClassObject* declaringClass,
    int slot, bool isSetOperation, bool noAccessCheck)
{
    Field* field;
    JValue* result;

    field = dvmSlotToField(declaringClass, slot);
    assert(field != NULL);

    /* verify access */
    if (!noAccessCheck) {
        if (isSetOperation && dvmIsFinalField(field)) {
            dvmThrowException("Ljava/lang/IllegalAccessException;",
                "field is marked 'final'");
            return NULL;
        }

        ClassObject* callerClass =
            dvmGetCaller2Class(dvmThreadSelf()->curFrame);

        /*
         * We need to check two things:
         *  (1) Would an instance of the calling class have access to the field?
         *  (2) If the field is "protected", is the object an instance of the
         *      calling class, or is the field's declaring class in the same
         *      package as the calling class?
         *
         * #1 is basic access control.  #2 ensures that, just because
         * you're a subclass of Foo, you can't mess with protected fields
         * in arbitrary Foo objects from other packages.
         */
        if (!dvmCheckFieldAccess(callerClass, field)) {
            dvmThrowException("Ljava/lang/IllegalAccessException;",
                "access to field not allowed");
            return NULL;
        }
        if (dvmIsProtectedField(field)) {
            bool isInstance, samePackage;

            if (obj != NULL)
                isInstance = dvmInstanceof(obj->clazz, callerClass);
            else
                isInstance = false;
            samePackage = dvmInSamePackage(declaringClass, callerClass);

            if (!isInstance && !samePackage) {
                dvmThrowException("Ljava/lang/IllegalAccessException;",
                    "access to protected field not allowed");
                return NULL;
            }
        }
    }

    if (dvmIsStaticField(field)) {
        /* init class if necessary, then return ptr to storage in "field" */
        if (!dvmIsClassInitialized(declaringClass)) {
            if (!dvmInitClass(declaringClass)) {
                assert(dvmCheckException(dvmThreadSelf()));
                return NULL;
            }
        }

        result = dvmStaticFieldPtr((StaticField*) field);
    } else {
        /*
         * Verify object is of correct type (i.e. it actually has the
         * expected field in it), then grab a pointer to obj storage.
         * The call to verifyObjectInClass throws an NPE if "obj" is NULL.
         */
        if (!verifyObjectInClass(obj, declaringClass)) {
            assert(dvmCheckException(dvmThreadSelf()));
            if (obj != NULL) {
                LOGD("Wrong type of object for field lookup: %s %s\n",
                    obj->clazz->descriptor, declaringClass->descriptor);
            }
            return NULL;
        }
        result = dvmFieldPtr(obj, ((InstField*) field)->byteOffset);
    }

    return result;
}

/*
 * public int getFieldModifiers(Class declaringClass, int slot)
 */
static void Dalvik_java_lang_reflect_Field_getFieldModifiers(
    const u4* args, JValue* pResult)
{
    // ignore thisPtr in args[0]
    ClassObject* declaringClass = (ClassObject*) args[1];
    int slot = args[2];
    Field* field;

    field = dvmSlotToField(declaringClass, slot);
    RETURN_INT(field->accessFlags & JAVA_FLAGS_MASK);
}

/*
 * private Object getField(Object o, Class declaringClass, Class type,
 *     int slot, boolean noAccessCheck)
 *
 * Primitive types need to be boxed.
 */
static void Dalvik_java_lang_reflect_Field_getField(const u4* args,
    JValue* pResult)
{
    // ignore thisPtr in args[0]
    Object* obj = (Object*) args[1];
    ClassObject* declaringClass = (ClassObject*) args[2];
    ClassObject* fieldType = (ClassObject*) args[3];
    int slot = args[4];
    bool noAccessCheck = (args[5] != 0);
    JValue value;
    const JValue* fieldPtr;
    DataObject* result;

    //dvmDumpClass(obj->clazz, kDumpClassFullDetail);

    /* get a pointer to the field's data; performs access checks */
    fieldPtr = getFieldDataAddr(obj, declaringClass, slot, false,noAccessCheck);
    if (fieldPtr == NULL)
        RETURN_VOID();

    /* copy 4 or 8 bytes out */
    if (fieldType->primitiveType == PRIM_LONG ||
        fieldType->primitiveType == PRIM_DOUBLE)
    {
        value.j = fieldPtr->j;
    } else {
        value.i = fieldPtr->i;
    }

    result = dvmWrapPrimitive(value, fieldType);
    dvmReleaseTrackedAlloc((Object*) result, NULL);
    RETURN_PTR(result);
}

/*
 * private void setField(Object o, Class declaringClass, Class type,
 *     int slot, boolean noAccessCheck, Object value)
 *
 * When assigning into a primitive field we will automatically extract
 * the value from box types.
 */
static void Dalvik_java_lang_reflect_Field_setField(const u4* args,
    JValue* pResult)
{
    // ignore thisPtr in args[0]
    Object* obj = (Object*) args[1];
    ClassObject* declaringClass = (ClassObject*) args[2];
    ClassObject* fieldType = (ClassObject*) args[3];
    int slot = args[4];
    bool noAccessCheck = (args[5] != 0);
    Object* valueObj = (Object*) args[6];
    JValue* fieldPtr;
    JValue value;

    /* unwrap primitive, or verify object type */
    if (!dvmUnwrapPrimitive(valueObj, fieldType, &value)) {
        dvmThrowException("Ljava/lang/IllegalArgumentException;",
            "invalid value for field");
        RETURN_VOID();
    }

    /* get a pointer to the field's data; performs access checks */
    fieldPtr = getFieldDataAddr(obj, declaringClass, slot, true, noAccessCheck);
    if (fieldPtr == NULL)
        RETURN_VOID();

    /* store 4 or 8 bytes */
    if (fieldType->primitiveType == PRIM_LONG ||
        fieldType->primitiveType == PRIM_DOUBLE)
    {
        fieldPtr->j = value.j;
    } else {
        fieldPtr->i = value.i;
    }

    RETURN_VOID();
}

/*
 * Convert a reflection primitive type ordinal (inherited from the previous
 * VM's reflection classes) to our value.
 */
static PrimitiveType convPrimType(int typeNum)
{
    static const PrimitiveType conv[PRIM_MAX] = {
        PRIM_NOT, PRIM_BOOLEAN, PRIM_BYTE, PRIM_CHAR, PRIM_SHORT,
        PRIM_INT, PRIM_FLOAT, PRIM_LONG, PRIM_DOUBLE
    };
    if (typeNum <= 0 || typeNum > 8)
        return PRIM_NOT;
    return conv[typeNum];
}

/*
 * Primitive field getters, e.g.:
 * private double getIField(Object o, Class declaringClass,
 *     Class type, int slot, boolean noAccessCheck, int type_no)
 *
 * The "type_no" is defined by the java.lang.reflect.Field class.
 */
static void Dalvik_java_lang_reflect_Field_getPrimitiveField(const u4* args,
    JValue* pResult)
{
    // ignore thisPtr in args[0]
    Object* obj = (Object*) args[1];
    ClassObject* declaringClass = (ClassObject*) args[2];
    ClassObject* fieldType = (ClassObject*) args[3];
    int slot = args[4];
    bool noAccessCheck = (args[5] != 0);
    int typeNum = args[6];
    PrimitiveType targetType = convPrimType(typeNum);
    const JValue* fieldPtr;
    JValue value;

    if (!dvmIsPrimitiveClass(fieldType)) {
        dvmThrowException("Ljava/lang/IllegalArgumentException;",
            "not a primitive field");
        RETURN_VOID();
    }

    /* get a pointer to the field's data; performs access checks */
    fieldPtr = getFieldDataAddr(obj, declaringClass, slot, false,noAccessCheck);
    if (fieldPtr == NULL)
        RETURN_VOID();

    /* copy 4 or 8 bytes out */
    if (fieldType->primitiveType == PRIM_LONG ||
        fieldType->primitiveType == PRIM_DOUBLE)
    {
        value.j = fieldPtr->j;
    } else {
        value.i = fieldPtr->i;
    }

    /* retrieve value, performing a widening conversion if necessary */
    if (dvmConvertPrimitiveValue(fieldType->primitiveType, targetType,
        &(value.i), &(pResult->i)) < 0)
    {
        dvmThrowException("Ljava/lang/IllegalArgumentException;",
            "invalid primitive conversion");
        RETURN_VOID();
    }
}

/*
 * Primitive field setters, e.g.:
 * private void setIField(Object o, Class declaringClass,
 *     Class type, int slot, boolean noAccessCheck, int type_no, int value)
 *
 * The "type_no" is defined by the java.lang.reflect.Field class.
 */
static void Dalvik_java_lang_reflect_Field_setPrimitiveField(const u4* args,
    JValue* pResult)
{
    // ignore thisPtr in args[0]
    Object* obj = (Object*) args[1];
    ClassObject* declaringClass = (ClassObject*) args[2];
    ClassObject* fieldType = (ClassObject*) args[3];
    int slot = args[4];
    bool noAccessCheck = (args[5] != 0);
    int typeNum = args[6];
    const s4* valuePtr = (s4*) &args[7];
    PrimitiveType srcType = convPrimType(typeNum);
    JValue* fieldPtr;
    JValue value;

    if (!dvmIsPrimitiveClass(fieldType)) {
        dvmThrowException("Ljava/lang/IllegalArgumentException;",
            "not a primitive field");
        RETURN_VOID();
    }

    /* convert the 32/64-bit arg to a JValue matching the field type */
    if (dvmConvertPrimitiveValue(srcType, fieldType->primitiveType,
        valuePtr, &(value.i)) < 0)
    {
        dvmThrowException("Ljava/lang/IllegalArgumentException;",
            "invalid primitive conversion");
        RETURN_VOID();
    }

    /* get a pointer to the field's data; performs access checks */
    fieldPtr = getFieldDataAddr(obj, declaringClass, slot, true, noAccessCheck);
    if (fieldPtr == NULL)
        RETURN_VOID();

    /* store 4 or 8 bytes */
    if (fieldType->primitiveType == PRIM_LONG ||
        fieldType->primitiveType == PRIM_DOUBLE)
    {
        fieldPtr->j = value.j;
    } else {
        fieldPtr->i = value.i;
    }

    RETURN_VOID();
}

/*
 * public Annotation[] getDeclaredAnnotations(Class declaringClass, int slot)
 *
 * Return the annotations declared for this field.
 */
static void Dalvik_java_lang_reflect_Field_getDeclaredAnnotations(
    const u4* args, JValue* pResult)
{
    // ignore thisPtr in args[0]
    ClassObject* declaringClass = (ClassObject*) args[1];
    int slot = args[2];
    Field* field;

    field = dvmSlotToField(declaringClass, slot);
    assert(field != NULL);

    ArrayObject* annos = dvmGetFieldAnnotations(field);
    dvmReleaseTrackedAlloc((Object*) annos, NULL);
    RETURN_PTR(annos);
}

/*
 * private Object[] getSignatureAnnotation()
 *
 * Returns the signature annotation.
 */
static void Dalvik_java_lang_reflect_Field_getSignatureAnnotation(const u4* args,
    JValue* pResult)
{
    // ignore thisPtr in args[0]
    ClassObject* declaringClass = (ClassObject*) args[1];
    int slot = args[2];
    Field* field;

    field = dvmSlotToField(declaringClass, slot);
    assert(field != NULL);

    ArrayObject* arr = dvmGetFieldSignatureAnnotation(field);
    dvmReleaseTrackedAlloc((Object*) arr, NULL);
    RETURN_PTR(arr);
}

static const DalvikNativeMethod java_lang_reflect_Field[] = {
    { "getFieldModifiers",  "(Ljava/lang/Class;I)I",
        Dalvik_java_lang_reflect_Field_getFieldModifiers },
    { "getField",           "(Ljava/lang/Object;Ljava/lang/Class;Ljava/lang/Class;IZ)Ljava/lang/Object;",
        Dalvik_java_lang_reflect_Field_getField },
    { "getBField",          "(Ljava/lang/Object;Ljava/lang/Class;Ljava/lang/Class;IZI)B",
        Dalvik_java_lang_reflect_Field_getPrimitiveField },
    { "getCField",          "(Ljava/lang/Object;Ljava/lang/Class;Ljava/lang/Class;IZI)C",
        Dalvik_java_lang_reflect_Field_getPrimitiveField },
    { "getDField",          "(Ljava/lang/Object;Ljava/lang/Class;Ljava/lang/Class;IZI)D",
        Dalvik_java_lang_reflect_Field_getPrimitiveField },
    { "getFField",          "(Ljava/lang/Object;Ljava/lang/Class;Ljava/lang/Class;IZI)F",
        Dalvik_java_lang_reflect_Field_getPrimitiveField },
    { "getIField",          "(Ljava/lang/Object;Ljava/lang/Class;Ljava/lang/Class;IZI)I",
        Dalvik_java_lang_reflect_Field_getPrimitiveField },
    { "getJField",          "(Ljava/lang/Object;Ljava/lang/Class;Ljava/lang/Class;IZI)J",
        Dalvik_java_lang_reflect_Field_getPrimitiveField },
    { "getSField",          "(Ljava/lang/Object;Ljava/lang/Class;Ljava/lang/Class;IZI)S",
        Dalvik_java_lang_reflect_Field_getPrimitiveField },
    { "getZField",          "(Ljava/lang/Object;Ljava/lang/Class;Ljava/lang/Class;IZI)Z",
        Dalvik_java_lang_reflect_Field_getPrimitiveField },
    { "setField",           "(Ljava/lang/Object;Ljava/lang/Class;Ljava/lang/Class;IZLjava/lang/Object;)V",
        Dalvik_java_lang_reflect_Field_setField },
    { "setBField",          "(Ljava/lang/Object;Ljava/lang/Class;Ljava/lang/Class;IZIB)V",
        Dalvik_java_lang_reflect_Field_setPrimitiveField },
    { "setCField",          "(Ljava/lang/Object;Ljava/lang/Class;Ljava/lang/Class;IZIC)V",
        Dalvik_java_lang_reflect_Field_setPrimitiveField },
    { "setDField",          "(Ljava/lang/Object;Ljava/lang/Class;Ljava/lang/Class;IZID)V",
        Dalvik_java_lang_reflect_Field_setPrimitiveField },
    { "setFField",          "(Ljava/lang/Object;Ljava/lang/Class;Ljava/lang/Class;IZIF)V",
        Dalvik_java_lang_reflect_Field_setPrimitiveField },
    { "setIField",          "(Ljava/lang/Object;Ljava/lang/Class;Ljava/lang/Class;IZII)V",
        Dalvik_java_lang_reflect_Field_setPrimitiveField },
    { "setJField",          "(Ljava/lang/Object;Ljava/lang/Class;Ljava/lang/Class;IZIJ)V",
        Dalvik_java_lang_reflect_Field_setPrimitiveField },
    { "setSField",          "(Ljava/lang/Object;Ljava/lang/Class;Ljava/lang/Class;IZIS)V",
        Dalvik_java_lang_reflect_Field_setPrimitiveField },
    { "setZField",          "(Ljava/lang/Object;Ljava/lang/Class;Ljava/lang/Class;IZIZ)V",
        Dalvik_java_lang_reflect_Field_setPrimitiveField },
    { "getDeclaredAnnotations", "(Ljava/lang/Class;I)[Ljava/lang/annotation/Annotation;",
        Dalvik_java_lang_reflect_Field_getDeclaredAnnotations },
    { "getSignatureAnnotation",  "(Ljava/lang/Class;I)[Ljava/lang/Object;",
        Dalvik_java_lang_reflect_Field_getSignatureAnnotation },
    { NULL, NULL, NULL },
};


/*
 * ===========================================================================
 *      java.lang.reflect.Method
 * ===========================================================================
 */

/*
 * private int getMethodModifiers(Class decl_class, int slot)
 *
 * (Not sure why the access flags weren't stored in the class along with
 * everything else.  Not sure why this isn't static.)
 */
static void Dalvik_java_lang_reflect_Method_getMethodModifiers(const u4* args,
    JValue* pResult)
{
    // ignore thisPtr in args[0]
    ClassObject* declaringClass = (ClassObject*) args[1];
    int slot = args[2];
    Method* meth;

    meth = dvmSlotToMethod(declaringClass, slot);
    RETURN_INT(fixMethodFlags(meth->accessFlags));
}

/*
 * private Object invokeNative(Object obj, Object[] args, Class declaringClass,
 *   Class[] parameterTypes, Class returnType, int slot, boolean noAccessCheck)
 *
 * Invoke a static or virtual method via reflection.
 */
static void Dalvik_java_lang_reflect_Method_invokeNative(const u4* args,
    JValue* pResult)
{
    // ignore thisPtr in args[0]
    Object* methObj = (Object*) args[1];        // null for static methods
    ArrayObject* argList = (ArrayObject*) args[2];
    ClassObject* declaringClass = (ClassObject*) args[3];
    ArrayObject* params = (ArrayObject*) args[4];
    ClassObject* returnType = (ClassObject*) args[5];
    int slot = args[6];
    bool noAccessCheck = (args[7] != 0);
    const Method* meth;
    Object* result;

    /*
     * "If the underlying method is static, the class that declared the
     * method is initialized if it has not already been initialized."
     */
    meth = dvmSlotToMethod(declaringClass, slot);
    assert(meth != NULL);

    if (dvmIsStaticMethod(meth)) {
        if (!dvmIsClassInitialized(declaringClass)) {
            if (!dvmInitClass(declaringClass))
                goto init_failed;
        }
    } else {
        /* looks like interfaces need this too? */
        if (dvmIsInterfaceClass(declaringClass) &&
            !dvmIsClassInitialized(declaringClass))
        {
            if (!dvmInitClass(declaringClass))
                goto init_failed;
        }

        /* make sure the object is an instance of the expected class */
        if (!verifyObjectInClass(methObj, declaringClass)) {
            assert(dvmCheckException(dvmThreadSelf()));
            RETURN_VOID();
        }

        /* do the virtual table lookup for the method */
        meth = dvmGetVirtualizedMethod(methObj->clazz, meth);
        if (meth == NULL) {
            assert(dvmCheckException(dvmThreadSelf()));
            RETURN_VOID();
        }
    }

    /*
     * If the method has a return value, "result" will be an object or
     * a boxed primitive.
     */
    result = dvmInvokeMethod(methObj, meth, argList, params, returnType,
                noAccessCheck);

    RETURN_PTR(result);

init_failed:
    /*
     * If initialization failed, an exception will be raised.
     */
    LOGD("Method.invoke() on bad class %s failed\n",
        declaringClass->descriptor);
    assert(dvmCheckException(dvmThreadSelf()));
    RETURN_VOID();
}

/*
 * public Annotation[] getDeclaredAnnotations(Class declaringClass, int slot)
 *
 * Return the annotations declared for this method.
 */
static void Dalvik_java_lang_reflect_Method_getDeclaredAnnotations(
    const u4* args, JValue* pResult)
{
    // ignore thisPtr in args[0]
    ClassObject* declaringClass = (ClassObject*) args[1];
    int slot = args[2];
    Method* meth;

    meth = dvmSlotToMethod(declaringClass, slot);
    assert(meth != NULL);

    ArrayObject* annos = dvmGetMethodAnnotations(meth);
    dvmReleaseTrackedAlloc((Object*)annos, NULL);
    RETURN_PTR(annos);
}

/*
 * public Annotation[] getParameterAnnotations(Class declaringClass, int slot)
 *
 * Return the annotations declared for this method's parameters.
 */
static void Dalvik_java_lang_reflect_Method_getParameterAnnotations(
    const u4* args, JValue* pResult)
{
    // ignore thisPtr in args[0]
    ClassObject* declaringClass = (ClassObject*) args[1];
    int slot = args[2];
    Method* meth;

    meth = dvmSlotToMethod(declaringClass, slot);
    assert(meth != NULL);

    ArrayObject* annos = dvmGetParameterAnnotations(meth);
    dvmReleaseTrackedAlloc((Object*)annos, NULL);
    RETURN_PTR(annos);
}

/*
 * private Object getDefaultValue(Class declaringClass, int slot)
 *
 * Return the default value for the annotation member represented by
 * this Method instance.  Returns NULL if none is defined.
 */
static void Dalvik_java_lang_reflect_Method_getDefaultValue(const u4* args,
    JValue* pResult)
{
    // ignore thisPtr in args[0]
    ClassObject* declaringClass = (ClassObject*) args[1];
    int slot = args[2];
    Method* meth;

    /* make sure this is an annotation class member */
    if (!dvmIsAnnotationClass(declaringClass))
        RETURN_PTR(NULL);

    meth = dvmSlotToMethod(declaringClass, slot);
    assert(meth != NULL);

    Object* def = dvmGetAnnotationDefaultValue(meth);
    dvmReleaseTrackedAlloc(def, NULL);
    RETURN_PTR(def);
}

/*
 * private Object[] getSignatureAnnotation()
 *
 * Returns the signature annotation.
 */
static void Dalvik_java_lang_reflect_Method_getSignatureAnnotation(
    const u4* args, JValue* pResult)
{
    // ignore thisPtr in args[0]
    ClassObject* declaringClass = (ClassObject*) args[1];
    int slot = args[2];
    Method* meth;

    meth = dvmSlotToMethod(declaringClass, slot);
    assert(meth != NULL);

    ArrayObject* arr = dvmGetMethodSignatureAnnotation(meth);
    dvmReleaseTrackedAlloc((Object*) arr, NULL);
    RETURN_PTR(arr);
}

static const DalvikNativeMethod java_lang_reflect_Method[] = {
    { "getMethodModifiers", "(Ljava/lang/Class;I)I",
        Dalvik_java_lang_reflect_Method_getMethodModifiers },
    { "invokeNative",       "(Ljava/lang/Object;[Ljava/lang/Object;Ljava/lang/Class;[Ljava/lang/Class;Ljava/lang/Class;IZ)Ljava/lang/Object;",
        Dalvik_java_lang_reflect_Method_invokeNative },
    { "getDeclaredAnnotations", "(Ljava/lang/Class;I)[Ljava/lang/annotation/Annotation;",
        Dalvik_java_lang_reflect_Method_getDeclaredAnnotations },
    { "getParameterAnnotations", "(Ljava/lang/Class;I)[[Ljava/lang/annotation/Annotation;",
        Dalvik_java_lang_reflect_Method_getParameterAnnotations },
    { "getDefaultValue",    "(Ljava/lang/Class;I)Ljava/lang/Object;",
        Dalvik_java_lang_reflect_Method_getDefaultValue },
    { "getSignatureAnnotation",  "(Ljava/lang/Class;I)[Ljava/lang/Object;",
        Dalvik_java_lang_reflect_Method_getSignatureAnnotation },
    { NULL, NULL, NULL },
};


/*
 * ===========================================================================
 *      java.lang.reflect.Proxy
 * ===========================================================================
 */

/*
 * static Class generateProxy(String name, Class[] interfaces,
 *      ClassLoader loader)
 *
 * Generate a proxy class with the specified characteristics.  Throws an
 * exception on error.
 */
static void Dalvik_java_lang_reflect_Proxy_generateProxy(const u4* args,
    JValue* pResult)
{
    StringObject* str = (StringObject*) args[0];
    ArrayObject* interfaces = (ArrayObject*) args[1];
    Object* loader = (Object*) args[2];
    ClassObject* result;

    result = dvmGenerateProxyClass(str, interfaces, loader);
    RETURN_PTR(result);
}

static const DalvikNativeMethod java_lang_reflect_Proxy[] = {
    { "generateProxy", "(Ljava/lang/String;[Ljava/lang/Class;Ljava/lang/ClassLoader;)Ljava/lang/Class;",
        Dalvik_java_lang_reflect_Proxy_generateProxy },
    { NULL, NULL, NULL },
};


/*
 * ===========================================================================
 *      java.security.AccessController
 * ===========================================================================
 */

/*
 * private static ProtectionDomain[] getStackDomains()
 *
 * Return an array of ProtectionDomain objects from the classes of the
 * methods on the stack.  Ignore reflection frames.  Stop at the first
 * privileged frame we see.
 */
static void Dalvik_java_security_AccessController_getStackDomains(
    const u4* args, JValue* pResult)
{
    UNUSED_PARAMETER(args);

    const Method** methods = NULL;
    int length;

    /*
     * Get an array with the stack trace in it.
     */
    if (!dvmCreateStackTraceArray(dvmThreadSelf()->curFrame, &methods, &length))
    {
        LOGE("Failed to create stack trace array\n");
        dvmThrowException("Ljava/lang/InternalError;", NULL);
        RETURN_VOID();
    }

    //int i;
    //LOGI("dvmCreateStackTraceArray results:\n");
    //for (i = 0; i < length; i++)
    //    LOGI(" %2d: %s.%s\n", i, methods[i]->clazz->name, methods[i]->name);

    /*
     * Generate a list of ProtectionDomain objects from the frames that
     * we're interested in.  Skip the first two methods (this method, and
     * the one that called us), and ignore reflection frames.  Stop on the
     * frame *after* the first privileged frame we see as we walk up.
     *
     * We create a new array, probably over-allocated, and fill in the
     * stuff we want.  We could also just run the list twice, but the
     * costs of the per-frame tests could be more expensive than the
     * second alloc.  (We could also allocate it on the stack using C99
     * array creation, but it's not guaranteed to fit.)
     *
     * The array we return doesn't include null ProtectionDomain objects,
     * so we skip those here.
     */
    Object** subSet = (Object**) malloc((length-2) * sizeof(Object*));
    if (subSet == NULL) {
        LOGE("Failed to allocate subSet (length=%d)\n", length);
        free(methods);
        dvmThrowException("Ljava/lang/InternalError;", NULL);
        RETURN_VOID();
    }
    int idx, subIdx = 0;
    for (idx = 2; idx < length; idx++) {
        const Method* meth = methods[idx];
        Object* pd;

        if (dvmIsReflectionMethod(meth))
            continue;

        if (dvmIsPrivilegedMethod(meth)) {
            /* find nearest non-reflection frame; note we skip priv frame */
            //LOGI("GSD priv frame at %s.%s\n", meth->clazz->name, meth->name);
            while (++idx < length && dvmIsReflectionMethod(methods[idx]))
                ;
            length = idx;       // stomp length to end loop
            meth = methods[idx];
        }

        /* get the pd object from the method's class */
        assert(gDvm.offJavaLangClass_pd != 0);
        pd = dvmGetFieldObject((Object*) meth->clazz,
                gDvm.offJavaLangClass_pd);
        //LOGI("FOUND '%s' pd=%p\n", meth->clazz->name, pd);
        if (pd != NULL)
            subSet[subIdx++] = pd;
    }

    //LOGI("subSet:\n");
    //for (i = 0; i < subIdx; i++)
    //    LOGI("  %2d: %s\n", i, subSet[i]->clazz->name);

    /*
     * Create an array object to contain "subSet".
     */
    ClassObject* pdArrayClass = NULL;
    ArrayObject* domains = NULL;
    pdArrayClass = dvmFindArrayClass("[Ljava/security/ProtectionDomain;", NULL);
    if (pdArrayClass == NULL) {
        LOGW("Unable to find ProtectionDomain class for array\n");
        goto bail;
    }
    domains = dvmAllocArray(pdArrayClass, subIdx, kObjectArrayRefWidth,
                ALLOC_DEFAULT);
    if (domains == NULL) {
        LOGW("Unable to allocate pd array (%d elems)\n", subIdx);
        goto bail;
    }

    /* copy the ProtectionDomain objects out */
    Object** objects = (Object**) domains->contents;
    for (idx = 0; idx < subIdx; idx++)
        *objects++ = subSet[idx];

bail:
    free(subSet);
    free(methods);
    dvmReleaseTrackedAlloc((Object*) domains, NULL);
    RETURN_PTR(domains);
}

static const DalvikNativeMethod java_security_AccessController[] = {
    { "getStackDomains",    "()[Ljava/security/ProtectionDomain;",
        Dalvik_java_security_AccessController_getStackDomains },
    { NULL, NULL, NULL },
};


/*
 * ===========================================================================
 *      java.util.concurrent.atomic.AtomicLong
 * ===========================================================================
 */

/*
 * private static native boolean VMSupportsCS8();
 */
static void Dalvik_java_util_concurrent_atomic_AtomicLong_VMSupportsCS8(
    const u4* args, JValue* pResult)
{
    UNUSED_PARAMETER(args);
    RETURN_BOOLEAN(1);
}

static const DalvikNativeMethod java_util_concurrent_atomic_AtomicLong[] = {
    { "VMSupportsCS8", "()Z",
      Dalvik_java_util_concurrent_atomic_AtomicLong_VMSupportsCS8 },
    { NULL, NULL, NULL },
};


/*
 * ===========================================================================
 *      org.apache.harmony.dalvik.ddmc.DdmServer
 * ===========================================================================
 */

/*
 * private static void nativeSendChunk(int type, byte[] data,
 *      int offset, int length)
 *
 * Send a DDM chunk to the server.
 */
static void Dalvik_org_apache_harmony_dalvik_ddmc_DdmServer_nativeSendChunk(
    const u4* args, JValue* pResult)
{
    int type = args[0];
    ArrayObject* data = (ArrayObject*) args[1];
    int offset = args[2];
    int length = args[3];

    assert(offset+length <= (int)data->length);

    dvmDbgDdmSendChunk(type, length, (const u1*)data->contents + offset);
    RETURN_VOID();
}

static const DalvikNativeMethod org_apache_harmony_dalvik_ddmc_DdmServer[] = {
    { "nativeSendChunk",    "(I[BII)V",
        Dalvik_org_apache_harmony_dalvik_ddmc_DdmServer_nativeSendChunk },
    { NULL, NULL, NULL },
};


/*
 * ===========================================================================
 *      org.apache.harmony.dalvik.ddmc.DdmVmInternal
 * ===========================================================================
 */

/*
 * public static void threadNotify(boolean enable)
 *
 * Enable DDM thread notifications.
 */
static void Dalvik_org_apache_harmony_dalvik_ddmc_DdmVmInternal_threadNotify(
    const u4* args, JValue* pResult)
{
    bool enable = (args[0] != 0);

    //LOGI("ddmThreadNotification: %d\n", enable);
    dvmDdmSetThreadNotification(enable);
    RETURN_VOID();
}

/*
 * public static byte[] getThreadStats()
 *
 * Get a buffer full of thread info.
 */
static void Dalvik_org_apache_harmony_dalvik_ddmc_DdmVmInternal_getThreadStats(
    const u4* args, JValue* pResult)
{
    UNUSED_PARAMETER(args);

    ArrayObject* result = dvmDdmGenerateThreadStats();
    dvmReleaseTrackedAlloc((Object*) result, NULL);
    RETURN_PTR(result);
}

/*
 * public static int heapInfoNotify(int what)
 *
 * Enable DDM heap notifications.
 */
static void Dalvik_org_apache_harmony_dalvik_ddmc_DdmVmInternal_heapInfoNotify(
    const u4* args, JValue* pResult)
{
    int when = args[0];
    bool ret;

    ret = dvmDdmHandleHpifChunk(when);
    RETURN_BOOLEAN(ret);
}

/*
 * public static boolean heapSegmentNotify(int when, int what, bool native)
 *
 * Enable DDM heap notifications.
 */
static void
    Dalvik_org_apache_harmony_dalvik_ddmc_DdmVmInternal_heapSegmentNotify(
    const u4* args, JValue* pResult)
{
    int  when   = args[0];        // 0=never (off), 1=during GC
    int  what   = args[1];        // 0=merged objects, 1=distinct objects
    bool native = (args[2] != 0); // false=virtual heap, true=native heap
    bool ret;

    ret = dvmDdmHandleHpsgNhsgChunk(when, what, native);
    RETURN_BOOLEAN(ret);
}

/*
 * public static StackTraceElement[] getStackTraceById(int threadId)
 *
 * Get a stack trace as an array of StackTraceElement objects.  Returns
 * NULL on failure, e.g. if the threadId couldn't be found.
 */
static void
    Dalvik_org_apache_harmony_dalvik_ddmc_DdmVmInternal_getStackTraceById(
    const u4* args, JValue* pResult)
{
    u4 threadId = args[0];
    ArrayObject* trace;

    trace = dvmDdmGetStackTraceById(threadId);
    RETURN_PTR(trace);
}

/*
 * public static void enableRecentAllocations(boolean enable)
 *
 * Enable or disable recent allocation tracking.
 */
static void
    Dalvik_org_apache_harmony_dalvik_ddmc_DdmVmInternal_enableRecentAllocations(
    const u4* args, JValue* pResult)
{
    bool enable = (args[0] != 0);

    if (enable)
        (void) dvmEnableAllocTracker();
    else
        (void) dvmDisableAllocTracker();
    RETURN_VOID();
}

/*
 * public static boolean getRecentAllocationStatus()
 *
 * Returns "true" if allocation tracking is enabled.
 */
static void
    Dalvik_org_apache_harmony_dalvik_ddmc_DdmVmInternal_getRecentAllocationStatus(
    const u4* args, JValue* pResult)
{
    UNUSED_PARAMETER(args);
    RETURN_BOOLEAN(gDvm.allocRecords != NULL);
}

/*
 * public static byte[] getRecentAllocations()
 *
 * Fill a buffer with data on recent heap allocations.
 */
static void
    Dalvik_org_apache_harmony_dalvik_ddmc_DdmVmInternal_getRecentAllocations(
    const u4* args, JValue* pResult)
{
    ArrayObject* data;

    data = dvmDdmGetRecentAllocations();
    dvmReleaseTrackedAlloc((Object*) data, NULL);
    RETURN_PTR(data);
}

static const DalvikNativeMethod
    org_apache_harmony_dalvik_ddmc_DdmVmInternal[] = {
    { "threadNotify",       "(Z)V",
      Dalvik_org_apache_harmony_dalvik_ddmc_DdmVmInternal_threadNotify },
    { "getThreadStats",     "()[B",
      Dalvik_org_apache_harmony_dalvik_ddmc_DdmVmInternal_getThreadStats },
    { "heapInfoNotify",     "(I)Z",
      Dalvik_org_apache_harmony_dalvik_ddmc_DdmVmInternal_heapInfoNotify },
    { "heapSegmentNotify",  "(IIZ)Z",
      Dalvik_org_apache_harmony_dalvik_ddmc_DdmVmInternal_heapSegmentNotify },
    { "getStackTraceById",  "(I)[Ljava/lang/StackTraceElement;",
      Dalvik_org_apache_harmony_dalvik_ddmc_DdmVmInternal_getStackTraceById },
    { "enableRecentAllocations", "(Z)V",
      Dalvik_org_apache_harmony_dalvik_ddmc_DdmVmInternal_enableRecentAllocations },
    { "getRecentAllocationStatus", "()Z",
      Dalvik_org_apache_harmony_dalvik_ddmc_DdmVmInternal_getRecentAllocationStatus },
    { "getRecentAllocations", "()[B",
      Dalvik_org_apache_harmony_dalvik_ddmc_DdmVmInternal_getRecentAllocations },
    { NULL, NULL, NULL },
};


/*
 * ===========================================================================
 *      sun.misc.Unsafe
 * ===========================================================================
 */

/*
 * private static native long objectFieldOffset0(Field field);
 */
static void Dalvik_sun_misc_Unsafe_objectFieldOffset0(const u4* args,
    JValue* pResult)
{
    Object* fieldObject = (Object*) args[0];
    InstField* field = (InstField*) dvmGetFieldFromReflectObj(fieldObject);
    s8 result = ((s8) field->byteOffset);

    RETURN_LONG(result);
}

/*
 * private static native int arrayBaseOffset0(Class clazz);
 */
static void Dalvik_sun_misc_Unsafe_arrayBaseOffset0(const u4* args,
    JValue* pResult)
{
    // The base offset is not type-dependent in this vm.
    UNUSED_PARAMETER(args);
    RETURN_INT(offsetof(ArrayObject, contents));
}

/*
 * private static native int arrayIndexScale0(Class clazz);
 */
static void Dalvik_sun_misc_Unsafe_arrayIndexScale0(const u4* args,
    JValue* pResult)
{
    ClassObject* clazz = (ClassObject*) args[0];
    int result;

    if ((clazz == gDvm.classArrayBoolean) ||
            (clazz == gDvm.classArrayByte)) {
        result = sizeof(u1);
    } else if ((clazz == gDvm.classArrayChar) ||
            (clazz == gDvm.classArrayShort)) {
        result = sizeof(u2);
    } else if ((clazz == gDvm.classArrayLong) ||
            (clazz == gDvm.classArrayDouble)) {
        result = sizeof(u8);
    } else {
        result = sizeof(u4);
    }

    RETURN_INT(result);
}

/*
 * public native boolean compareAndSwapInt(Object obj, long offset,
 *         int expectedValue, int newValue);
 */
static void Dalvik_sun_misc_Unsafe_compareAndSwapInt(const u4* args,
    JValue* pResult)
{
    // We ignore the this pointer in args[0].
    Object* obj = (Object*) args[1];
    s8 offset = GET_ARG_LONG(args, 2);
    s4 expectedValue = args[4];
    s4 newValue = args[5];
    volatile int32_t* address = (volatile int32_t*) (((u1*) obj) + offset);

    // Note: android_atomic_cmpxchg() returns 0 on success, not failure.
    int result = android_atomic_cmpxchg(expectedValue, newValue, address);

    RETURN_BOOLEAN(result == 0);
}

/*
 * public native boolean compareAndSwapLong(Object obj, long offset,
 *         long expectedValue, long newValue);
 */
static void Dalvik_sun_misc_Unsafe_compareAndSwapLong(const u4* args,
    JValue* pResult)
{
    // We ignore the this pointer in args[0].
    Object* obj = (Object*) args[1];
    s8 offset = GET_ARG_LONG(args, 2);
    s8 expectedValue = GET_ARG_LONG(args, 4);
    s8 newValue = GET_ARG_LONG(args, 6);
    volatile int64_t* address = (volatile int64_t*) (((u1*) obj) + offset);

    // Note: android_atomic_cmpxchg() returns 0 on success, not failure.
    int result =
        android_quasiatomic_cmpxchg_64(expectedValue, newValue, address);

    RETURN_BOOLEAN(result == 0);
}

/*
 * public native boolean compareAndSwapObject(Object obj, long offset,
 *         Object expectedValue, Object newValue);
 */
static void Dalvik_sun_misc_Unsafe_compareAndSwapObject(const u4* args,
    JValue* pResult)
{
    // We ignore the this pointer in args[0].
    Object* obj = (Object*) args[1];
    s8 offset = GET_ARG_LONG(args, 2);
    Object* expectedValue = (Object*) args[4];
    Object* newValue = (Object*) args[5];
    int32_t* address = (int32_t*) (((u1*) obj) + offset);

    // Note: android_atomic_cmpxchg() returns 0 on success, not failure.
    int result = android_atomic_cmpxchg((int32_t) expectedValue,
            (int32_t) newValue, address);
    
    RETURN_BOOLEAN(result == 0);
}

/*
 * public native int getIntVolatile(Object obj, long offset);
 */
static void Dalvik_sun_misc_Unsafe_getIntVolatile(const u4* args,
    JValue* pResult)
{
    // We ignore the this pointer in args[0].
    Object* obj = (Object*) args[1];
    s8 offset = GET_ARG_LONG(args, 2);
    volatile s4* address = (volatile s4*) (((u1*) obj) + offset);

    RETURN_INT(*address);
}

/*
 * public native void putIntVolatile(Object obj, long offset, int newValue);
 */
static void Dalvik_sun_misc_Unsafe_putIntVolatile(const u4* args,
    JValue* pResult)
{
    // We ignore the this pointer in args[0].
    Object* obj = (Object*) args[1];
    s8 offset = GET_ARG_LONG(args, 2);
    s4 value = (s4) args[4];
    volatile s4* address = (volatile s4*) (((u1*) obj) + offset);

    *address = value;
    RETURN_VOID();
}

/*
 * public native long getLongVolatile(Object obj, long offset);
 */
static void Dalvik_sun_misc_Unsafe_getLongVolatile(const u4* args,
    JValue* pResult)
{
    // We ignore the this pointer in args[0].
    Object* obj = (Object*) args[1];
    s8 offset = GET_ARG_LONG(args, 2);
    volatile s8* address = (volatile s8*) (((u1*) obj) + offset);

    RETURN_LONG(android_quasiatomic_read_64(address));
}

/*
 * public native void putLongVolatile(Object obj, long offset, long newValue);
 */
static void Dalvik_sun_misc_Unsafe_putLongVolatile(const u4* args,
    JValue* pResult)
{
    // We ignore the this pointer in args[0].
    Object* obj = (Object*) args[1];
    s8 offset = GET_ARG_LONG(args, 2);
    s8 value = GET_ARG_LONG(args, 4);
    volatile s8* address = (volatile s8*) (((u1*) obj) + offset);

    android_quasiatomic_swap_64(value, address);
    RETURN_VOID();
}

/*
 * public native Object getObjectVolatile(Object obj, long offset);
 */
static void Dalvik_sun_misc_Unsafe_getObjectVolatile(const u4* args,
    JValue* pResult)
{
    // We ignore the this pointer in args[0].
    Object* obj = (Object*) args[1];
    s8 offset = GET_ARG_LONG(args, 2);
    volatile Object** address = (volatile Object**) (((u1*) obj) + offset);

    RETURN_PTR((void*) *address);
}

/*
 * public native void putObjectVolatile(Object obj, long offset,
 *         Object newValue);
 */
static void Dalvik_sun_misc_Unsafe_putObjectVolatile(const u4* args,
    JValue* pResult)
{
    // We ignore the this pointer in args[0].
    Object* obj = (Object*) args[1];
    s8 offset = GET_ARG_LONG(args, 2);
    Object* value = (Object*) args[4];
    volatile Object** address = (volatile Object**) (((u1*) obj) + offset);

    *address = value;
    RETURN_VOID();
}
            
/*
 * public native int getInt(Object obj, long offset);
 */
static void Dalvik_sun_misc_Unsafe_getInt(const u4* args, JValue* pResult)
{
    // We ignore the this pointer in args[0].
    Object* obj = (Object*) args[1];
    s8 offset = GET_ARG_LONG(args, 2);
    s4* address = (s4*) (((u1*) obj) + offset);

    RETURN_INT(*address);
}

/*
 * public native void putInt(Object obj, long offset, int newValue);
 */
static void Dalvik_sun_misc_Unsafe_putInt(const u4* args, JValue* pResult)
{
    // We ignore the this pointer in args[0].
    Object* obj = (Object*) args[1];
    s8 offset = GET_ARG_LONG(args, 2);
    s4 value = (s4) args[4];
    s4* address = (s4*) (((u1*) obj) + offset);

    *address = value;
    RETURN_VOID();
}

/*
 * public native long getLong(Object obj, long offset);
 */
static void Dalvik_sun_misc_Unsafe_getLong(const u4* args, JValue* pResult)
{
    // We ignore the this pointer in args[0].
    Object* obj = (Object*) args[1];
    s8 offset = GET_ARG_LONG(args, 2);
    s8* address = (s8*) (((u1*) obj) + offset);

    RETURN_LONG(*address);
}

/*
 * public native void putLong(Object obj, long offset, long newValue);
 */
static void Dalvik_sun_misc_Unsafe_putLong(const u4* args, JValue* pResult)
{
    // We ignore the this pointer in args[0].
    Object* obj = (Object*) args[1];
    s8 offset = GET_ARG_LONG(args, 2);
    s8 value = GET_ARG_LONG(args, 4);
    s8* address = (s8*) (((u1*) obj) + offset);

    *address = value;
    RETURN_VOID();
}

/*
 * public native Object getObject(Object obj, long offset);
 */
static void Dalvik_sun_misc_Unsafe_getObject(const u4* args, JValue* pResult)
{
    // We ignore the this pointer in args[0].
    Object* obj = (Object*) args[1];
    s8 offset = GET_ARG_LONG(args, 2);
    Object** address = (Object**) (((u1*) obj) + offset);

    RETURN_PTR(*address);
}

/*
 * public native void putObject(Object obj, long offset, Object newValue);
 */
static void Dalvik_sun_misc_Unsafe_putObject(const u4* args, JValue* pResult)
{
    // We ignore the this pointer in args[0].
    Object* obj = (Object*) args[1];
    s8 offset = GET_ARG_LONG(args, 2);
    Object* value = (Object*) args[4];
    Object** address = (Object**) (((u1*) obj) + offset);

    *address = value;
    RETURN_VOID();
}

static const DalvikNativeMethod sun_misc_Unsafe[] = {
    { "objectFieldOffset0", "(Ljava/lang/reflect/Field;)J",
      Dalvik_sun_misc_Unsafe_objectFieldOffset0 },
    { "arrayBaseOffset0", "(Ljava/lang/Class;)I",
      Dalvik_sun_misc_Unsafe_arrayBaseOffset0 },
    { "arrayIndexScale0", "(Ljava/lang/Class;)I",
      Dalvik_sun_misc_Unsafe_arrayIndexScale0 },
    { "compareAndSwapInt", "(Ljava/lang/Object;JII)Z",
      Dalvik_sun_misc_Unsafe_compareAndSwapInt },
    { "compareAndSwapLong", "(Ljava/lang/Object;JJJ)Z",
      Dalvik_sun_misc_Unsafe_compareAndSwapLong },
    { "compareAndSwapObject",
      "(Ljava/lang/Object;JLjava/lang/Object;Ljava/lang/Object;)Z",
      Dalvik_sun_misc_Unsafe_compareAndSwapObject },
    { "getIntVolatile", "(Ljava/lang/Object;J)I",
      Dalvik_sun_misc_Unsafe_getIntVolatile },
    { "putIntVolatile", "(Ljava/lang/Object;JI)V",
      Dalvik_sun_misc_Unsafe_putIntVolatile },
    { "getLongVolatile", "(Ljava/lang/Object;J)J",
      Dalvik_sun_misc_Unsafe_getLongVolatile },
    { "putLongVolatile", "(Ljava/lang/Object;JJ)V",
      Dalvik_sun_misc_Unsafe_putLongVolatile },
    { "getObjectVolatile", "(Ljava/lang/Object;J)Ljava/lang/Object;",
      Dalvik_sun_misc_Unsafe_getObjectVolatile },
    { "putObjectVolatile", "(Ljava/lang/Object;JLjava/lang/Object;)V",
      Dalvik_sun_misc_Unsafe_putObjectVolatile },
    { "getInt", "(Ljava/lang/Object;J)I",
      Dalvik_sun_misc_Unsafe_getInt },
    { "putInt", "(Ljava/lang/Object;JI)V",
      Dalvik_sun_misc_Unsafe_putInt },
    { "getLong", "(Ljava/lang/Object;J)J",
      Dalvik_sun_misc_Unsafe_getLong },
    { "putLong", "(Ljava/lang/Object;JJ)V",
      Dalvik_sun_misc_Unsafe_putLong },
    { "getObject", "(Ljava/lang/Object;J)Ljava/lang/Object;",
      Dalvik_sun_misc_Unsafe_getObject },
    { "putObject", "(Ljava/lang/Object;JLjava/lang/Object;)V",
      Dalvik_sun_misc_Unsafe_putObject },
    { NULL, NULL, NULL },
};


/*
 * ===========================================================================
 *      General
 * ===========================================================================
 */

/*
 * Set of classes for which we provide methods.
 *
 * The last field, classNameHash, is filled in at startup.
 */
static DalvikNativeClass gDvmNativeMethodSet[] = {
    { "Ljava/lang/Object;",               java_lang_Object, 0 },
    { "Ljava/lang/Class;",                java_lang_Class, 0 },
    { "Ljava/lang/Runtime;",              java_lang_Runtime, 0 },
    { "Ljava/lang/String;",               java_lang_String, 0 },
    { "Ljava/lang/System;",               java_lang_System, 0 },
    { "Ljava/lang/SystemProperties;",     java_lang_SystemProperties, 0 },
    { "Ljava/lang/Throwable;",            java_lang_Throwable, 0 },
    { "Ljava/lang/VMClassLoader;",        java_lang_VMClassLoader, 0 },
    { "Ljava/lang/VMThread;",             java_lang_VMThread, 0 },
    { "Ljava/lang/reflect/AccessibleObject;",
                                java_lang_reflect_AccessibleObject, 0 },
    { "Ljava/lang/reflect/Array;",        java_lang_reflect_Array, 0 },
    { "Ljava/lang/reflect/Constructor;",  java_lang_reflect_Constructor, 0 },
    { "Ljava/lang/reflect/Field;",        java_lang_reflect_Field, 0 },
    { "Ljava/lang/reflect/Method;",       java_lang_reflect_Method, 0 },
    { "Ljava/lang/reflect/Proxy;",        java_lang_reflect_Proxy, 0 },
    { "Ljava/security/AccessController;", java_security_AccessController, 0 },
    { "Ljava/util/concurrent/atomic/AtomicLong;",
                                java_util_concurrent_atomic_AtomicLong, 0 },
    { "Ldalvik/system/VMDebug;",          dalvik_system_VMDebug, 0 },
    { "Ldalvik/system/DexFile;",          dalvik_system_DexFile, 0 },
    { "Ldalvik/system/VMRuntime;",        dalvik_system_VMRuntime, 0 },
    { "Ldalvik/system/Zygote;",           dalvik_system_Zygote, 0 },
    { "Ldalvik/system/VMStack;",          dalvik_system_VMStack, 0 },
    { "Lorg/apache/harmony/dalvik/ddmc/DdmServer;",
          org_apache_harmony_dalvik_ddmc_DdmServer, 0 },
    { "Lorg/apache/harmony/dalvik/ddmc/DdmVmInternal;", 
          org_apache_harmony_dalvik_ddmc_DdmVmInternal, 0 },
    { "Lorg/apache/harmony/dalvik/NativeTestTarget;",
          org_apache_harmony_dalvik_NativeTestTarget, 0 },
    { "Lsun/misc/Unsafe;",                sun_misc_Unsafe, 0 },
    { NULL, NULL, 0 },
};


/*
 * Set up hash values on the class names.
 */
bool dvmInternalNativeStartup(void)
{
    DalvikNativeClass* classPtr = gDvmNativeMethodSet;

    while (classPtr->classDescriptor != NULL) {
        classPtr->classDescriptorHash =
            dvmComputeUtf8Hash(classPtr->classDescriptor);
        classPtr++;
    }

    gDvm.userDexFiles = dvmHashTableCreate(2, freeDexOrJar);
    if (gDvm.userDexFiles == NULL)
        return false;

    return true;
}

/*
 * Clean up.
 */
void dvmInternalNativeShutdown(void)
{
    dvmHashTableFree(gDvm.userDexFiles);
}

/*
 * Search the internal native set for a match.
 */
DalvikNativeFunc dvmLookupInternalNativeMethod(const Method* method)
{
    const char* classDescriptor = method->clazz->descriptor;
    const DalvikNativeClass* pClass;
    u4 hash;

    hash = dvmComputeUtf8Hash(classDescriptor);
    pClass = gDvmNativeMethodSet;
    while (true) {
        if (pClass->classDescriptor == NULL)
            break;
        if (pClass->classDescriptorHash == hash &&
            strcmp(pClass->classDescriptor, classDescriptor) == 0)
        {
            const DalvikNativeMethod* pMeth = pClass->methodInfo;
            while (true) {
                if (pMeth->name == NULL)
                    break;

                if (dvmCompareNameDescriptorAndMethod(pMeth->name,
                    pMeth->signature, method) == 0)
                {
                    /* match */
                    //LOGV("+++  match on %s.%s %s at %p\n",
                    //    className, methodName, methodSignature, pMeth->fnPtr);
                    return pMeth->fnPtr;
                }

                pMeth++;
            }
        }

        pClass++;
    }

    return NULL;
}


/*
 * Magic "internal native" code stub, inserted into abstract method
 * definitions when a class is first loaded.  This throws the expected
 * exception so we don't have to explicitly check for it in the interpreter.
 */
void dvmAbstractMethodStub(const u4* args, JValue* pResult)
{
    LOGI("--- called into dvmAbstractMethodStub\n");
    dvmThrowException("Ljava/lang/AbstractMethodError;",
        "abstract method not implemented");
}
