/*
 * Copyright (C) 2007 The Android Open Source Project
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

package dalvik.system;

/**
 * VM-specific debug features. Though this class and many of its members
 * are public, this class is meant to be wrapped in a more friendly way
 * for use by application developers. On the Android platform, the
 * recommended way to access this functionality is through the class
 * <code>android.os.Debug</code>.
 */
public final class VMDebug {
    /** default method trace data file name */
    static public final String DEFAULT_METHOD_TRACE_FILE_NAME = "/sdcard/dmtrace.trace";

    /**
     * flag for startMethodTracing(), which adds the results from
     * startAllocCounting to the trace key file.
     */
    public static final int TRACE_COUNT_ALLOCS = 1;

    /* constants for getAllocCount */
    private static final int KIND_ALLOCATED_OBJECTS = 1<<0;
    private static final int KIND_ALLOCATED_BYTES   = 1<<1;
    private static final int KIND_FREED_OBJECTS     = 1<<2;
    private static final int KIND_FREED_BYTES       = 1<<3;
    private static final int KIND_GC_INVOCATIONS    = 1<<4;
    private static final int KIND_EXT_ALLOCATED_OBJECTS = 1<<12;
    private static final int KIND_EXT_ALLOCATED_BYTES   = 1<<13;
    private static final int KIND_EXT_FREED_OBJECTS     = 1<<14;
    private static final int KIND_EXT_FREED_BYTES       = 1<<15;

    public static final int KIND_GLOBAL_ALLOCATED_OBJECTS =
        KIND_ALLOCATED_OBJECTS;
    public static final int KIND_GLOBAL_ALLOCATED_BYTES =
        KIND_ALLOCATED_BYTES;
    public static final int KIND_GLOBAL_FREED_OBJECTS =
        KIND_FREED_OBJECTS;
    public static final int KIND_GLOBAL_FREED_BYTES =
        KIND_FREED_BYTES;
    public static final int KIND_GLOBAL_GC_INVOCATIONS =
        KIND_GC_INVOCATIONS;
    public static final int KIND_GLOBAL_EXT_ALLOCATED_OBJECTS =
        KIND_EXT_ALLOCATED_OBJECTS;
    public static final int KIND_GLOBAL_EXT_ALLOCATED_BYTES =
        KIND_EXT_ALLOCATED_BYTES;
    public static final int KIND_GLOBAL_EXT_FREED_OBJECTS =
        KIND_EXT_FREED_OBJECTS;
    public static final int KIND_GLOBAL_EXT_FREED_BYTES =
        KIND_EXT_FREED_BYTES;

    public static final int KIND_THREAD_ALLOCATED_OBJECTS =
        KIND_ALLOCATED_OBJECTS << 16;
    public static final int KIND_THREAD_ALLOCATED_BYTES =
        KIND_ALLOCATED_BYTES << 16;
    public static final int KIND_THREAD_FREED_OBJECTS =
        KIND_FREED_OBJECTS << 16;
    public static final int KIND_THREAD_FREED_BYTES =
        KIND_FREED_BYTES << 16;
    public static final int KIND_THREAD_GC_INVOCATIONS =
        KIND_GC_INVOCATIONS << 16;
    public static final int KIND_THREAD_EXT_ALLOCATED_OBJECTS =
        KIND_EXT_ALLOCATED_OBJECTS << 16;
    public static final int KIND_THREAD_EXT_ALLOCATED_BYTES =
        KIND_EXT_ALLOCATED_BYTES << 16;
    public static final int KIND_THREAD_EXT_FREED_OBJECTS =
        KIND_EXT_FREED_OBJECTS << 16;
    public static final int KIND_THREAD_EXT_FREED_BYTES =
        KIND_EXT_FREED_BYTES << 16;

    public static final int KIND_ALL_COUNTS = 0xffffffff;

    /* all methods are static */
    private VMDebug() {}

    /**
     * Time, in msec, since the last debugger activity.  -1 if debugger is
     * not connected.
     */
    public static native long lastDebuggerActivity();

    /**
     * Determine if a debugger is currently attached.
     */
    public static native boolean isDebuggerConnected();

    /**
     * Enable object allocation count logging and reporting.  Call with
     * a depth of zero to disable.  This produces "top N" lists on every GC.
     */
    //public static native void enableTopAllocCounts(int depth);
    
    /**
     * Start method tracing with default name, size, and with <code>0</code>
     * flags.
     */
    public static void startMethodTracing() {
        startMethodTracing(DEFAULT_METHOD_TRACE_FILE_NAME, 0, 0);
    }

    /**
     * Start method tracing, specifying a file name as well as a default
     * buffer size. See <a
     * href="{@docRoot}reference/traceview.html"> Running the
     * Traceview Debugging Program</a> for information about reading
     * trace files.
     * 
     * <p>You can use either a fully qualified path and
     * name, or just a name. If only a name is specified, the file will
     * be created under the /sdcard/ directory. If a name is not given,
     * the default is /sdcard/dmtrace.trace.</p>
     * 
     * @param traceFileName name to give the trace file
     * @param bufferSize the maximum size of both files combined. If passed
     * as <code>0</code>, it defaults to 8MB.
     * @param flags flags to control method tracing. The only one that
     * is currently defined is {@link #TRACE_COUNT_ALLOCS}.
     */
    public static native void startMethodTracing(String traceFileName,
        int bufferSize, int flags);

    /**
     * Stop method tracing.
     */
    public static native void stopMethodTracing();

    /**
     * Start sending Dalvik method trace info to the emulator.
     */
    public static native void startEmulatorTracing();

    /**
     * Stop sending Dalvik method trace info to the emulator.
     */
    public static native void stopEmulatorTracing();

    /**
     * Get an indication of thread CPU usage.  The value returned
     * indicates the amount of time that the current thread has spent
     * executing code or waiting for certain types of I/O.
     *
     * The time is expressed in nanoseconds, and is only meaningful
     * when compared to the result from an earlier call.  Note that
     * nanosecond resolution does not imply nanosecond accuracy.
     *
     * On system which don't support this operation, the call returns -1.
     */
    public static native long threadCpuTimeNanos();

    /**
     * Count the number and aggregate size of memory allocations between
     * two points.
     */
    public static native void startAllocCounting();
    public static native void stopAllocCounting();
    public static native int getAllocCount(int kind);
    public static native void resetAllocCount(int kinds);

    /**
     * Establish an object allocation limit in the current thread.  Useful
     * for catching regressions in code that is expected to operate
     * without causing any allocations.
     *
     * Use -1 to disable the limit.
     *
     * Returns the previous limit.
     */
    public static native int setAllocationLimit(int limit);

    /**
     * Establish an object allocation limit for the entire VM.  Very much
     * like setAllocationLimit().
     *
     * Use -1 to disable the limit.
     *
     * Returns the previous limit.
     */
    public static native int setGlobalAllocationLimit(int limit);

    /**
     * Count the number of instructions executed between two points.
     */
    public static native void startInstructionCounting();
    public static native void stopInstructionCounting();
    public static native void getInstructionCount(int[] counts);
    public static native void resetInstructionCount();

    /**
     * Dump a list of loaded class to the log file.
     */
    public static native void printLoadedClasses(int flags);

    /**
     * Get the number of loaded classes.
     */
    public static native int getLoadedClassCount();

    /* don't ask */
    static native void printThis(Object thisThing, int count, int thing);

    /*
     * Fake method, inserted into dmtrace output when the garbage collector
     * runs.  Not actually called.
     */
    private static void startGC() {}

    /*
     * Fake method, inserted into dmtrace output during class preparation
     * (loading and linking, but not verification or initialization).  Not
     * actually called.
     */
    private static void startClassPrep() {}
}
