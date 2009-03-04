/*
 * Copyright (C) 2006 The Android Open Source Project
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
 * Provides access to the Dalvik "zygote" feature, which allows a VM instance to
 * be partially initialized and then fork()'d from the partially initialized
 * state.
 * 
 * @since Android 1.0
 */
public class Zygote {
    /*
     * Bit values for "debugFlags" argument.  The definitions are duplicated
     * in the native code.
     */
    /** enable debugging over JDWP */
    public static final int DEBUG_ENABLE_DEBUGGER   = 1;
    /** enable JNI checks */
    public static final int DEBUG_ENABLE_CHECKJNI   = 1 << 1;
    /** enable Java programming language "assert" statements */
    public static final int DEBUG_ENABLE_ASSERT     = 1 << 2;

    private Zygote() {}

    /**
     * Forks a new Zygote instance, but does not leave the zygote mode.
     * The current VM must have been started with the -Xzygote flag. The
     * new child is expected to eventually call forkAndSpecialize()
     *
     * @return 0 if this is the child, pid of the child
     * if this is the parent, or -1 on error
     */
    native public static int fork();

    /**
     * Forks a new VM instance.  The current VM must have been started
     * with the -Xzygote flag. <b>NOTE: new instance keeps all
     * root capabilities. The new process is expected to call capset()</b>.
     *
     * @param uid the UNIX uid that the new process should setuid() to after
     * fork()ing and and before spawning any threads.
     * @param gid the UNIX gid that the new process should setgid() to after
     * fork()ing and and before spawning any threads.
     * @param gids null-ok; a list of UNIX gids that the new process should
     * setgroups() to after fork and before spawning any threads.
     * @param debugFlags bit flags that enable debugging features.
     * @param rlimits null-ok an array of rlimit tuples, with the second
     * dimension having a length of 3 and representing
     * (resource, rlim_cur, rlim_max). These are set via the posix
     * setrlimit(2) call.
     *
     * @return 0 if this is the child, pid of the child
     * if this is the parent, or -1 on error.
     */
    native public static int forkAndSpecialize(int uid, int gid, int[] gids,
            int debugFlags, int[][] rlimits);

    /**
     * Forks a new VM instance.
     * @deprecated use {@link Zygote#forkAndSpecialize(int, int, int[], int, int[][])}
     */
    @Deprecated
    public static int forkAndSpecialize(int uid, int gid, int[] gids,
            boolean enableDebugger, int[][] rlimits) {
        int debugFlags = enableDebugger ? DEBUG_ENABLE_DEBUGGER : 0;
        return forkAndSpecialize(uid, gid, gids, debugFlags, rlimits);
    }

    /**
     * Special method to start the system server process. In addition to the
     * common actions performed in forkAndSpecialize, the pid of the child
     * process is recorded such that the death of the child process will cause 
     * zygote to exit.
     *
     * @param uid the UNIX uid that the new process should setuid() to after
     * fork()ing and and before spawning any threads.
     * @param gid the UNIX gid that the new process should setgid() to after
     * fork()ing and and before spawning any threads.
     * @param gids null-ok; a list of UNIX gids that the new process should
     * setgroups() to after fork and before spawning any threads.
     * @param debugFlags bit flags that enable debugging features.
     * @param rlimits null-ok an array of rlimit tuples, with the second
     * dimension having a length of 3 and representing
     * (resource, rlim_cur, rlim_max). These are set via the posix
     * setrlimit(2) call.
     *
     * @return 0 if this is the child, pid of the child
     * if this is the parent, or -1 on error.
     */
    native public static int forkSystemServer(int uid, int gid, 
            int[] gids, int debugFlags, int[][] rlimits);

    /**
     * Special method to start the system server process.
     * @deprecated use {@link Zygote#forkSystemServer(int, int, int[], int, int[][])}
     */
    @Deprecated
    public static int forkSystemServer(int uid, int gid, int[] gids,
            boolean enableDebugger, int[][] rlimits) {
        int debugFlags = enableDebugger ? DEBUG_ENABLE_DEBUGGER : 0;
        return forkAndSpecialize(uid, gid, gids, debugFlags, rlimits);
    }
}

