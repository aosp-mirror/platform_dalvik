// Copyright 2006 The Android Open Source Project 

package dalvik.system;

/**
 * Interfaces for supporting the Dalvik "zygote" feature, which allows
 * a VM instance to be partially initialized and then fork()'d from
 * the partially initialized state.
 */

public class Zygote {
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
     * root capabilities. The new process is expected to call capset()<b>.
     *
     * @param uid the UNIX uid that the new process should setuid() to after
     * fork()ing and and before spawning any threads.
     * @param gid the UNIX gid that the new process should setgid() to after
     * fork()ing and and before spawning any threads.
     * @param gids null-ok; a list of UNIX gids that the new process should
     * setgroups() to after fork and before spawning any threads.
     * @param enableDebugger true if JDWP should be enabled.
     * @param rlimits null-ok an array of rlimit tuples, with the second
     * dimension having a length of 3 and representing
     * (resource, rlim_cur, rlim_max). These are set via the posix
     * setrlimit(2) call.
     *
     * @return 0 if this is the child, pid of the child
     * if this is the parent, or -1 on error.
     */
    native public static int forkAndSpecialize(int uid, int gid, int[] gids,
            boolean enableDebugger, int[][] rlimits);

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
     * @param enableDebugger true if JDWP should be enabled.
     * @param rlimits null-ok an array of rlimit tuples, with the second
     * dimension having a length of 3 and representing
     * (resource, rlim_cur, rlim_max). These are set via the posix
     * setrlimit(2) call.
     *
     * @return 0 if this is the child, pid of the child
     * if this is the parent, or -1 on error.
     */
    native public static int forkSystemServer(int uid, int gid, 
            int[] gids, boolean enableDebugger, int[][] rlimits);
}
