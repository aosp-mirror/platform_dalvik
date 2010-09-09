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
 * Variables with library scope.
 *
 * Prefer this over scattered static and global variables -- it's easier to
 * view the state in a debugger, it makes clean shutdown simpler, we can
 * trivially dump the state into a crash log, and it dodges most naming
 * collisions that will arise when we are embedded in a larger program.
 *
 * If we want multiple VMs per process, this can get stuffed into TLS (or
 * accessed through a Thread field).  May need to pass it around for some
 * of the early initialization functions.
 */
#ifndef _DALVIK_GLOBALS
#define _DALVIK_GLOBALS

#include <stdarg.h>
#include <pthread.h>

#define MAX_BREAKPOINTS 20      /* used for a debugger optimization */

/* private structures */
typedef struct GcHeap GcHeap;
typedef struct BreakpointSet BreakpointSet;
typedef struct InlineSub InlineSub;

/*
 * One of these for each -ea/-da/-esa/-dsa on the command line.
 */
typedef struct AssertionControl {
    char*   pkgOrClass;         /* package/class string, or NULL for esa/dsa */
    int     pkgOrClassLen;      /* string length, for quick compare */
    bool    enable;             /* enable or disable */
    bool    isPackage;          /* string ended with "..."? */
} AssertionControl;

/*
 * Execution mode, e.g. interpreter vs. JIT.
 */
typedef enum ExecutionMode {
    kExecutionModeUnknown = 0,
    kExecutionModeInterpPortable,
    kExecutionModeInterpFast,
#if defined(WITH_JIT)
    kExecutionModeJit,
#endif
} ExecutionMode;

/*
 * All fields are initialized to zero.
 *
 * Storage allocated here must be freed by a subsystem shutdown function or
 * from within freeGlobals().
 */
struct DvmGlobals {
    /*
     * Some options from the command line or environment.
     */
    char*       bootClassPathStr;
    char*       classPathStr;

    unsigned int    heapSizeStart;
    unsigned int    heapSizeMax;
    unsigned int    stackSize;

    bool        verboseGc;
    bool        verboseJni;
    bool        verboseClass;
    bool        verboseShutdown;

    bool        jdwpAllowed;        // debugging allowed for this process?
    bool        jdwpConfigured;     // has debugging info been provided?
    int         jdwpTransport;
    bool        jdwpServer;
    char*       jdwpHost;
    int         jdwpPort;
    bool        jdwpSuspend;

    /* use wall clock as method profiler clock source? */
    bool        profilerWallClock;

    /*
     * Lock profiling threshold value in milliseconds.  Acquires that
     * exceed threshold are logged.  Acquires within the threshold are
     * logged with a probability of $\frac{time}{threshold}$ .  If the
     * threshold is unset no additional logging occurs.
     */
    u4          lockProfThreshold;

    int         (*vfprintfHook)(FILE*, const char*, va_list);
    void        (*exitHook)(int);
    void        (*abortHook)(void);

    int         jniGrefLimit;       // 0 means no limit
    char*       jniTrace;
    bool        reduceSignals;
    bool        noQuitHandler;
    bool        verifyDexChecksum;
    char*       stackTraceFile;     // for SIGQUIT-inspired output

    bool        logStdio;

    DexOptimizerMode    dexOptMode;
    DexClassVerifyMode  classVerifyMode;

    bool        dexOptForSmp;

    /*
     * GC option flags.
     */
    bool        preciseGc;
    bool        preVerify;
    bool        postVerify;
    bool        generateRegisterMaps;
    bool        concurrentMarkSweep;
    bool        verifyCardTable;

    int         assertionCtrlCount;
    AssertionControl*   assertionCtrl;

    ExecutionMode   executionMode;

    /*
     * VM init management.
     */
    bool        initializing;
    int         initExceptionCount;
    bool        optimizing;

    /*
     * java.lang.System properties set from the command line.
     */
    int         numProps;
    int         maxProps;
    char**      propList;

    /*
     * Where the VM goes to find system classes.
     */
    ClassPathEntry* bootClassPath;
    /* used by the DEX optimizer to load classes from an unfinished DEX */
    DvmDex*     bootClassPathOptExtra;
    bool        optimizingBootstrapClass;

    /*
     * Loaded classes, hashed by class name.  Each entry is a ClassObject*,
     * allocated in GC space.
     */
    HashTable*  loadedClasses;

    /*
     * Value for the next class serial number to be assigned.  This is
     * incremented as we load classes.  Failed loads and races may result
     * in some numbers being skipped, and the serial number is not
     * guaranteed to start at 1, so the current value should not be used
     * as a count of loaded classes.
     */
    volatile int classSerialNumber;

    /*
     * Classes with a low classSerialNumber are probably in the zygote, and
     * their InitiatingLoaderList is not used, to promote sharing. The list is
     * kept here instead.
     */
    InitiatingLoaderList* initiatingLoaderList;

    /*
     * Interned strings.
     */

    /* A mutex that guards access to the interned string tables. */
    pthread_mutex_t internLock;

    /* Hash table of strings interned by the user. */
    HashTable*  internedStrings;

    /* Hash table of strings interned by the class loader. */
    HashTable*  literalStrings;

    /*
     * Quick lookups for popular classes used internally.
     */
    ClassObject* classJavaLangClass;
    ClassObject* classJavaLangClassArray;
    ClassObject* classJavaLangError;
    ClassObject* classJavaLangObject;
    ClassObject* classJavaLangObjectArray;
    ClassObject* classJavaLangRuntimeException;
    ClassObject* classJavaLangString;
    ClassObject* classJavaLangThread;
    ClassObject* classJavaLangVMThread;
    ClassObject* classJavaLangThreadGroup;
    ClassObject* classJavaLangThrowable;
    ClassObject* classJavaLangStackOverflowError;
    ClassObject* classJavaLangStackTraceElement;
    ClassObject* classJavaLangStackTraceElementArray;
    ClassObject* classJavaLangAnnotationAnnotationArray;
    ClassObject* classJavaLangAnnotationAnnotationArrayArray;
    ClassObject* classJavaLangReflectAccessibleObject;
    ClassObject* classJavaLangReflectConstructor;
    ClassObject* classJavaLangReflectConstructorArray;
    ClassObject* classJavaLangReflectField;
    ClassObject* classJavaLangReflectFieldArray;
    ClassObject* classJavaLangReflectMethod;
    ClassObject* classJavaLangReflectMethodArray;
    ClassObject* classJavaLangReflectProxy;
    ClassObject* classJavaLangExceptionInInitializerError;
    ClassObject* classJavaLangRefPhantomReference;
    ClassObject* classJavaLangRefReference;
    ClassObject* classJavaNioReadWriteDirectByteBuffer;
    ClassObject* classJavaSecurityAccessController;
    ClassObject* classOrgApacheHarmonyLangAnnotationAnnotationFactory;
    ClassObject* classOrgApacheHarmonyLangAnnotationAnnotationMember;
    ClassObject* classOrgApacheHarmonyLangAnnotationAnnotationMemberArray;
    ClassObject* classOrgApacheHarmonyNioInternalDirectBuffer;
    jclass      jclassOrgApacheHarmonyNioInternalDirectBuffer;

    /* synthetic classes for arrays of primitives */
    ClassObject* classArrayBoolean;
    ClassObject* classArrayChar;
    ClassObject* classArrayFloat;
    ClassObject* classArrayDouble;
    ClassObject* classArrayByte;
    ClassObject* classArrayShort;
    ClassObject* classArrayInt;
    ClassObject* classArrayLong;

    /* method offsets - Object */
    int         voffJavaLangObject_equals;
    int         voffJavaLangObject_hashCode;
    int         voffJavaLangObject_toString;
    int         voffJavaLangObject_finalize;

    /* field offsets - Class */
    int         offJavaLangClass_pd;

    /* field offsets - String */
    int         javaLangStringReady;    /* 0=not init, 1=ready, -1=initing */
    int         offJavaLangString_value;
    int         offJavaLangString_count;
    int         offJavaLangString_offset;
    int         offJavaLangString_hashCode;

    /* field offsets - Thread */
    int         offJavaLangThread_vmThread;
    int         offJavaLangThread_group;
    int         offJavaLangThread_daemon;
    int         offJavaLangThread_name;
    int         offJavaLangThread_priority;

    /* method offsets - Thread */
    int         voffJavaLangThread_run;

    /* field offsets - VMThread */
    int         offJavaLangVMThread_thread;
    int         offJavaLangVMThread_vmData;

    /* method offsets - ThreadGroup */
    int         voffJavaLangThreadGroup_removeThread;

    /* field offsets - Throwable */
    int         offJavaLangThrowable_stackState;
    int         offJavaLangThrowable_message;
    int         offJavaLangThrowable_cause;

    /* field offsets - java.lang.reflect.* */
    int         offJavaLangReflectAccessibleObject_flag;
    int         offJavaLangReflectConstructor_slot;
    int         offJavaLangReflectConstructor_declClass;
    int         offJavaLangReflectField_slot;
    int         offJavaLangReflectField_declClass;
    int         offJavaLangReflectMethod_slot;
    int         offJavaLangReflectMethod_declClass;

    /* field offsets - java.lang.ref.Reference */
    int         offJavaLangRefReference_referent;
    int         offJavaLangRefReference_queue;
    int         offJavaLangRefReference_queueNext;
    int         offJavaLangRefReference_pendingNext;

    /* method pointers - java.lang.ref.Reference */
    Method*     methJavaLangRefReference_enqueueInternal;

    /* field offsets - java.nio.Buffer and java.nio.DirectByteBufferImpl */
    //int         offJavaNioBuffer_capacity;
    //int         offJavaNioDirectByteBufferImpl_pointer;

    /* method pointers - java.security.AccessController */
    volatile int javaSecurityAccessControllerReady;
    Method*     methJavaSecurityAccessController_doPrivileged[4];

    /* constructor method pointers; no vtable involved, so use Method* */
    Method*     methJavaLangStackTraceElement_init;
    Method*     methJavaLangExceptionInInitializerError_init;
    Method*     methJavaLangRefPhantomReference_init;
    Method*     methJavaLangReflectConstructor_init;
    Method*     methJavaLangReflectField_init;
    Method*     methJavaLangReflectMethod_init;
    Method*     methOrgApacheHarmonyLangAnnotationAnnotationMember_init;

    /* static method pointers - android.lang.annotation.* */
    Method*
        methOrgApacheHarmonyLangAnnotationAnnotationFactory_createAnnotation;

    /* direct method pointers - java.lang.reflect.Proxy */
    Method*     methJavaLangReflectProxy_constructorPrototype;

    /* field offsets - java.lang.reflect.Proxy */
    int         offJavaLangReflectProxy_h;

    /* fake native entry point method */
    Method*     methFakeNativeEntry;

    /* assorted direct buffer helpers */
    Method*     methJavaNioReadWriteDirectByteBuffer_init;
    Method*     methOrgApacheHarmonyLuniPlatformPlatformAddress_on;
    Method*     methOrgApacheHarmonyNioInternalDirectBuffer_getEffectiveAddress;
    int         offJavaNioBuffer_capacity;
    int         offJavaNioBuffer_effectiveDirectAddress;
    int         offOrgApacheHarmonyLuniPlatformPlatformAddress_osaddr;
    int         voffOrgApacheHarmonyLuniPlatformPlatformAddress_toLong;

    /*
     * VM-synthesized primitive classes, for arrays.
     */
    ClassObject* volatile primitiveClass[PRIM_MAX];

    /*
     * Thread list.  This always has at least one element in it (main),
     * and main is always the first entry.
     *
     * The threadListLock is used for several things, including the thread
     * start condition variable.  Generally speaking, you must hold the
     * threadListLock when:
     *  - adding/removing items from the list
     *  - waiting on or signaling threadStartCond
     *  - examining the Thread struct for another thread (this is to avoid
     *    one thread freeing the Thread struct while another thread is
     *    perusing it)
     */
    Thread*     threadList;
    pthread_mutex_t threadListLock;

    pthread_cond_t threadStartCond;

    /*
     * The thread code grabs this before suspending all threads.  There
     * are a few things that can cause a "suspend all":
     *  (1) the GC is starting;
     *  (2) the debugger has sent a "suspend all" request;
     *  (3) a thread has hit a breakpoint or exception that the debugger
     *      has marked as a "suspend all" event;
     *  (4) the SignalCatcher caught a signal that requires suspension.
     *  (5) (if implemented) the JIT needs to perform a heavyweight
     *      rearrangement of the translation cache or JitTable.
     *
     * Because we use "safe point" self-suspension, it is never safe to
     * do a blocking "lock" call on this mutex -- if it has been acquired,
     * somebody is probably trying to put you to sleep.  The leading '_' is
     * intended as a reminder that this lock is special.
     */
    pthread_mutex_t _threadSuspendLock;

    /*
     * Guards Thread->suspendCount for all threads, and provides the lock
     * for the condition variable that all suspended threads sleep on
     * (threadSuspendCountCond).
     *
     * This has to be separate from threadListLock because of the way
     * threads put themselves to sleep.
     */
    pthread_mutex_t threadSuspendCountLock;

    /*
     * Suspended threads sleep on this.  They should sleep on the condition
     * variable until their "suspend count" is zero.
     *
     * Paired with "threadSuspendCountLock".
     */
    pthread_cond_t  threadSuspendCountCond;

    /*
     * Sum of all threads' suspendCount fields.  The JIT needs to know if any
     * thread is suspended.  Guarded by threadSuspendCountLock.
     */
    int  sumThreadSuspendCount;

    /*
     * MUTEX ORDERING: when locking multiple mutexes, always grab them in
     * this order to avoid deadlock:
     *
     *  (1) _threadSuspendLock      (use lockThreadSuspend())
     *  (2) threadListLock          (use dvmLockThreadList())
     *  (3) threadSuspendCountLock  (use lockThreadSuspendCount())
     */


    /*
     * Thread ID bitmap.  We want threads to have small integer IDs so
     * we can use them in "thin locks".
     */
    BitVector*  threadIdMap;

    /*
     * Manage exit conditions.  The VM exits when all non-daemon threads
     * have exited.  If the main thread returns early, we need to sleep
     * on a condition variable.
     */
    int         nonDaemonThreadCount;   /* must hold threadListLock to access */
    //pthread_mutex_t vmExitLock;
    pthread_cond_t  vmExitCond;

    /*
     * The set of DEX files loaded by custom class loaders.
     */
    HashTable*  userDexFiles;

    /*
     * JNI global reference table.
     */
#ifdef USE_INDIRECT_REF
    IndirectRefTable jniGlobalRefTable;
#else
    ReferenceTable  jniGlobalRefTable;
#endif
    pthread_mutex_t jniGlobalRefLock;
    int         jniGlobalRefHiMark;
    int         jniGlobalRefLoMark;

    /*
     * JNI pinned object table (used for primitive arrays).
     */
    ReferenceTable  jniPinRefTable;
    pthread_mutex_t jniPinRefLock;

    /*
     * Native shared library table.
     */
    HashTable*  nativeLibs;

    /*
     * GC heap lock.  Functions like gcMalloc() acquire this before making
     * any changes to the heap.  It is held throughout garbage collection.
     */
    pthread_mutex_t gcHeapLock;

    /*
     * Condition variable to queue threads waiting to retry an
     * allocation.  Signaled after a concurrent GC is completed.
     */
    pthread_cond_t gcHeapCond;

    /* Opaque pointer representing the heap. */
    GcHeap*     gcHeap;

    /* The card table base, modified as needed for marking cards. */
    u1*         biasedCardTableBase;

    /*
     * Pre-allocated throwables.
     */
    Object*     outOfMemoryObj;
    Object*     internalErrorObj;
    Object*     noClassDefFoundErrorObj;

    /* Monitor list, so we can free them */
    /*volatile*/ Monitor* monitorList;

    /* Monitor for Thread.sleep() implementation */
    Monitor*    threadSleepMon;

    /* set when we create a second heap inside the zygote */
    bool        newZygoteHeapAllocated;

    /*
     * TLS keys.
     */
    pthread_key_t pthreadKeySelf;       /* Thread*, for dvmThreadSelf */

    /*
     * JNI allows you to have multiple VMs, but we limit ourselves to 1,
     * so "vmList" is really just a pointer to the one and only VM.
     */
    JavaVM*     vmList;

    /*
     * Cache results of "A instanceof B".
     */
    AtomicCache* instanceofCache;

    /* instruction width table, used for optimization and verification */
    InstructionWidth*   instrWidth;
    /* instruction flags table, used for verification */
    InstructionFlags*   instrFlags;
    /* instruction format table, used for verification */
    InstructionFormat*  instrFormat;

    /* inline substitution table, used during optimization */
    InlineSub*          inlineSubs;

    /*
     * Bootstrap class loader linear allocator.
     */
    LinearAllocHdr* pBootLoaderAlloc;


    /*
     * Heap worker thread.
     */
    bool            heapWorkerInitialized;
    bool            heapWorkerReady;
    bool            haltHeapWorker;
    pthread_t       heapWorkerHandle;
    pthread_mutex_t heapWorkerLock;
    pthread_cond_t  heapWorkerCond;
    pthread_cond_t  heapWorkerIdleCond;
    pthread_mutex_t heapWorkerListLock;

    /*
     * Compute some stats on loaded classes.
     */
    int         numLoadedClasses;
    int         numDeclaredMethods;
    int         numDeclaredInstFields;
    int         numDeclaredStaticFields;

    /* when using a native debugger, set this to suppress watchdog timers */
    bool        nativeDebuggerActive;

    /*
     * JDWP debugger support.
     *
     * Note "debuggerActive" is accessed from mterp, so its storage size and
     * meaning must not be changed without updating the assembly sources.
     */
    bool        debuggerConnected;      /* debugger or DDMS is connected */
    u1          debuggerActive;         /* debugger is making requests */
    JdwpState*  jdwpState;

    /*
     * Registry of objects known to the debugger.
     */
    HashTable*  dbgRegistry;

    /*
     * Debugger breakpoint table.
     */
    BreakpointSet*  breakpointSet;

    /*
     * Single-step control struct.  We currently only allow one thread to
     * be single-stepping at a time, which is all that really makes sense,
     * but it's possible we may need to expand this to be per-thread.
     */
    StepControl stepControl;

    /*
     * DDM features embedded in the VM.
     */
    bool        ddmThreadNotification;

    /*
     * Zygote (partially-started process) support
     */
    bool        zygote;

    /*
     * Used for tracking allocations that we report to DDMS.  When the feature
     * is enabled (through a DDMS request) the "allocRecords" pointer becomes
     * non-NULL.
     */
    pthread_mutex_t allocTrackerLock;
    AllocRecord*    allocRecords;
    int             allocRecordHead;        /* most-recently-added entry */
    int             allocRecordCount;       /* #of valid entries */

#ifdef WITH_ALLOC_LIMITS
    /* set on first use of an alloc limit, never cleared */
    bool        checkAllocLimits;
    /* allocation limit, for setGlobalAllocationLimit() regression testing */
    int         allocationLimit;
#endif

#ifdef WITH_DEADLOCK_PREDICTION
    /* global lock on history tree accesses */
    pthread_mutex_t deadlockHistoryLock;

    enum { kDPOff=0, kDPWarn, kDPErr, kDPAbort } deadlockPredictMode;
#endif

    /*
     * When a profiler is enabled, this is incremented.  Distinct profilers
     * include "dmtrace" method tracing, emulator method tracing, and
     * possibly instruction counting.
     *
     * The purpose of this is to have a single value that the interpreter
     * can check to see if any profiling activity is enabled.
     */
    volatile int activeProfilers;

    /*
     * State for method-trace profiling.
     */
    MethodTraceState methodTrace;

    /*
     * State for emulator tracing.
     */
    void*       emulatorTracePage;
    int         emulatorTraceEnableCount;

    /*
     * Global state for memory allocation profiling.
     */
    AllocProfState allocProf;

    /*
     * Pointers to the original methods for things that have been inlined.
     * This makes it easy for us to output method entry/exit records for
     * the method calls we're not actually making.  (Used by method
     * profiling.)
     */
    Method**    inlinedMethods;

    /*
     * Dalvik instruction counts (256 entries).
     */
    int*        executedInstrCounts;
    bool        instructionCountEnableCount;

    /*
     * Signal catcher thread (for SIGQUIT).
     */
    pthread_t   signalCatcherHandle;
    bool        haltSignalCatcher;

    /*
     * Stdout/stderr conversion thread.
     */
    bool            haltStdioConverter;
    bool            stdioConverterReady;
    pthread_t       stdioConverterHandle;
    pthread_mutex_t stdioConverterLock;
    pthread_cond_t  stdioConverterCond;

    /*
     * pid of the system_server process. We track it so that when system server
     * crashes the Zygote process will be killed and restarted.
     */
    pid_t systemServerPid;

    int kernelGroupScheduling;

//#define COUNT_PRECISE_METHODS
#ifdef COUNT_PRECISE_METHODS
    PointerSet* preciseMethods;
#endif

    /* some RegisterMap statistics, useful during development */
    void*       registerMapStats;
};

extern struct DvmGlobals gDvm;

#if defined(WITH_JIT)

/*
 * Exiting the compiled code w/o chaining will incur overhead to look up the
 * target in the code cache which is extra work only when JIT is enabled. So
 * we want to monitor it closely to make sure we don't have performance bugs.
 */
typedef enum NoChainExits {
    kInlineCacheMiss = 0,
    kCallsiteInterpreted,
    kSwitchOverflow,
    kHeavyweightMonitor,
    kNoChainExitLast,
} NoChainExits;

/*
 * JIT-specific global state
 */
struct DvmJitGlobals {
    /*
     * Guards writes to Dalvik PC (dPC), translated code address (codeAddr) and
     * chain fields within the JIT hash table.  Note carefully the access
     * mechanism.
     * Only writes are guarded, and the guarded fields must be updated in a
     * specific order using atomic operations.  Further, once a field is
     * written it cannot be changed without halting all threads.
     *
     * The write order is:
     *    1) codeAddr
     *    2) dPC
     *    3) chain [if necessary]
     *
     * This mutex also guards both read and write of curJitTableEntries.
     */
    pthread_mutex_t tableLock;

    /* The JIT hash table.  Note that for access speed, copies of this pointer
     * are stored in each thread. */
    struct JitEntry *pJitEntryTable;

    /* Array of profile threshold counters */
    unsigned char *pProfTable;

    /* Copy of pProfTable used for temporarily disabling the Jit */
    unsigned char *pProfTableCopy;

    /* Size of JIT hash table in entries.  Must be a power of 2 */
    unsigned int jitTableSize;

    /* Mask used in hash function for JitTable.  Should be jitTableSize-1 */
    unsigned int jitTableMask;

    /* How many entries in the JitEntryTable are in use */
    unsigned int jitTableEntriesUsed;

    /* Bytes allocated for the code cache */
    unsigned int codeCacheSize;

    /* Trigger for trace selection */
    unsigned short threshold;

    /* JIT Compiler Control */
    bool               haltCompilerThread;
    bool               blockingMode;
    pthread_t          compilerHandle;
    pthread_mutex_t    compilerLock;
    pthread_mutex_t    compilerICPatchLock;
    pthread_cond_t     compilerQueueActivity;
    pthread_cond_t     compilerQueueEmpty;
    volatile int       compilerQueueLength;
    int                compilerHighWater;
    int                compilerWorkEnqueueIndex;
    int                compilerWorkDequeueIndex;
    int                compilerICPatchIndex;

    /* JIT internal stats */
    int                compilerMaxQueued;
    int                translationChains;

    /* Compiled code cache */
    void* codeCache;

    /* Bytes used by the code templates */
    unsigned int templateSize;

    /* Bytes already used in the code cache */
    unsigned int codeCacheByteUsed;

    /* Number of installed compilations in the cache */
    unsigned int numCompilations;

    /* Flag to indicate that the code cache is full */
    bool codeCacheFull;

    /* Page size  - 1 */
    unsigned int pageSizeMask;

    /* Lock to change the protection type of the code cache */
    pthread_mutex_t    codeCacheProtectionLock;

    /* Number of times that the code cache has been reset */
    int numCodeCacheReset;

    /* Number of times that the code cache reset request has been delayed */
    int numCodeCacheResetDelayed;

    /* true/false: compile/reject opcodes specified in the -Xjitop list */
    bool includeSelectedOp;

    /* true/false: compile/reject methods specified in the -Xjitmethod list */
    bool includeSelectedMethod;

    /* Disable JIT for selected opcodes - one bit for each opcode */
    char opList[32];

    /* Disable JIT for selected methods */
    HashTable *methodTable;

    /* Flag to dump all compiled code */
    bool printMe;

    /* Flag to count trace execution */
    bool profile;

    /* Vector to disable selected optimizations */
    int disableOpt;

    /* Table to track the overall and trace statistics of hot methods */
    HashTable*  methodStatsTable;

    /* Filter method compilation blacklist with call-graph information */
    bool checkCallGraph;

    /* New translation chain has been set up */
    volatile bool hasNewChain;

#if defined(WITH_SELF_VERIFICATION)
    /* Spin when error is detected, volatile so GDB can reset it */
    volatile bool selfVerificationSpin;
#endif

    /* Framework or stand-alone? */
    bool runningInAndroidFramework;

    /* Framework callback happened? */
    bool alreadyEnabledViaFramework;

    /* Framework requests to disable the JIT for good */
    bool disableJit;

#if defined(SIGNATURE_BREAKPOINT)
    /* Signature breakpoint */
    u4 signatureBreakpointSize;         // # of words
    u4 *signatureBreakpoint;            // Signature content
#endif

#if defined(WITH_JIT_TUNING)
    /* Performance tuning counters */
    int                addrLookupsFound;
    int                addrLookupsNotFound;
    int                noChainExit[kNoChainExitLast];
    int                normalExit;
    int                puntExit;
    int                invokeMonomorphic;
    int                invokePolymorphic;
    int                invokeNative;
    int                invokeMonoGetterInlined;
    int                invokeMonoSetterInlined;
    int                invokePolyGetterInlined;
    int                invokePolySetterInlined;
    int                returnOp;
    int                icPatchInit;
    int                icPatchLockFree;
    int                icPatchQueued;
    int                icPatchRejected;
    int                icPatchDropped;
    u8                 jitTime;
    int                codeCachePatches;
#endif

    /* Place arrays at the end to ease the display in gdb sessions */

    /* Work order queue for compilations */
    CompilerWorkOrder compilerWorkQueue[COMPILER_WORK_QUEUE_SIZE];

    /* Work order queue for predicted chain patching */
    ICPatchWorkOrder compilerICPatchQueue[COMPILER_IC_PATCH_QUEUE_SIZE];
};

extern struct DvmJitGlobals gDvmJit;

#if defined(WITH_JIT_TUNING)
extern int gDvmICHitCount;
#endif

#endif

#endif /*_DALVIK_GLOBALS*/
