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

package java.lang;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Manages child processes.
 *
 * <p>Harmony's native implementation (for comparison purposes):
 * http://tinyurl.com/3ytwuq
 */
final class ProcessManager {

    /**
     * constant communicated from native code indicating that a
     * child died, but it was unable to determine the status
     */
    private static final int WAIT_STATUS_UNKNOWN = -1;

    /**
     * constant communicated from native code indicating that there
     * are currently no children to wait for
     */
    private static final int WAIT_STATUS_NO_CHILDREN = -2;

    /**
     * constant communicated from native code indicating that a wait()
     * call returned -1 and set an undocumented (and hence unexpected) errno
     */
    private static final int WAIT_STATUS_STRANGE_ERRNO = -3;

    /**
     * Initializes native static state.
     */
    static native void staticInitialize();
    static {
        staticInitialize();
    }

    /**
     * Map from pid to Process. We keep weak references to the Process objects
     * and clean up the entries when no more external references are left. The
     * process objects themselves don't require much memory, but file
     * descriptors (associated with stdin/out/err in this case) can be
     * a scarce resource.
     */
    private final Map<Integer, ProcessReference> processReferences
            = new HashMap<Integer, ProcessReference>();

    /** Keeps track of garbage-collected Processes. */
    private final ProcessReferenceQueue referenceQueue
            = new ProcessReferenceQueue();

    private ProcessManager() {
        // Spawn a thread to listen for signals from child processes.
        Thread processThread = new Thread(ProcessManager.class.getName()) {
            @Override
            public void run() {
                watchChildren();
            }
        };
        processThread.setDaemon(true);
        processThread.start();
    }

    /**
     * Kills the process with the given ID.
     *
     * @parm pid ID of process to kill
     */
    private static native void kill(int pid) throws IOException;

    /**
     * Cleans up after garbage collected processes. Requires the lock on the
     * map.
     */
    void cleanUp() {
        ProcessReference reference;
        while ((reference = referenceQueue.poll()) != null) {
            synchronized (processReferences) {
                processReferences.remove(reference.processId);
            }
        }
    }

    /**
     * Listens for signals from processes and calls back to
     * {@link #onExit(int,int)}.
     */
    native void watchChildren();

    /**
     * Called by {@link #watchChildren()} when a child process exits.
     *
     * @param pid ID of process that exited
     * @param exitValue value the process returned upon exit
     */
    void onExit(int pid, int exitValue) {
        ProcessReference processReference = null;

        synchronized (processReferences) {
            cleanUp();
            if (pid >= 0) {
                processReference = processReferences.remove(pid);
            } else if (exitValue == WAIT_STATUS_NO_CHILDREN) {
                if (processReferences.isEmpty()) {
                    /*
                     * There are no eligible children; wait for one to be
                     * added. The wait() will return due to the
                     * notifyAll() call below.
                     */
                    try {
                        processReferences.wait();
                    } catch (InterruptedException ex) {
                        // This should never happen.
                        throw new AssertionError("unexpected interrupt");
                    }
                } else {
                    /*
                     * A new child was spawned just before we entered
                     * the synchronized block. We can just fall through
                     * without doing anything special and land back in
                     * the native wait().
                     */
                }
            } else {
                // Something weird is happening; abort!
                throw new AssertionError("unexpected wait() behavior");
            }
        }

        if (processReference != null) {
            ProcessImpl process = processReference.get();
            if (process != null) {
                process.setExitValue(exitValue);
            }
        }
    }

    /**
     * Executes a native process. Fills in in, out, and err and returns the
     * new process ID upon success.
     */
    static native int exec(String[] command, String[] environment,
            String workingDirectory, FileDescriptor in, FileDescriptor out,
            FileDescriptor err, boolean redirectErrorStream) throws IOException;

    /**
     * Executes a process and returns an object representing it.
     */
    Process exec(String[] taintedCommand, String[] taintedEnvironment, File workingDirectory,
            boolean redirectErrorStream) throws IOException {
        // Make sure we throw the same exceptions as the RI.
        if (taintedCommand == null) {
            throw new NullPointerException();
        }
        if (taintedCommand.length == 0) {
            throw new IndexOutOfBoundsException();
        }

        // Handle security and safety by copying mutable inputs and checking them.
        String[] command = taintedCommand.clone();
        String[] environment = taintedEnvironment != null ? taintedEnvironment.clone() : null;
        SecurityManager securityManager = System.getSecurityManager();
        if (securityManager != null) {
            securityManager.checkExec(command[0]);
        }
        // Check we're not passing null Strings to the native exec.
        for (String arg : command) {
            if (arg == null) {
                throw new NullPointerException();
            }
        }
        // The environment is allowed to be null or empty, but no element may be null.
        if (environment != null) {
            for (String env : environment) {
                if (env == null) {
                    throw new NullPointerException();
                }
            }
        }

        FileDescriptor in = new FileDescriptor();
        FileDescriptor out = new FileDescriptor();
        FileDescriptor err = new FileDescriptor();

        String workingPath = (workingDirectory == null)
                ? null
                : workingDirectory.getPath();

        // Ensure onExit() doesn't access the process map before we add our
        // entry.
        synchronized (processReferences) {
            int pid;
            try {
                pid = exec(command, environment, workingPath, in, out, err, redirectErrorStream);
            } catch (IOException e) {
                IOException wrapper = new IOException("Error running exec()." 
                        + " Command: " + Arrays.toString(command)
                        + " Working Directory: " + workingDirectory
                        + " Environment: " + Arrays.toString(environment));
                wrapper.initCause(e);
                throw wrapper;
            }
            ProcessImpl process = new ProcessImpl(pid, in, out, err);
            ProcessReference processReference
                    = new ProcessReference(process, referenceQueue);
            processReferences.put(pid, processReference);

            /*
             * This will wake up the child monitor thread in case there
             * weren't previously any children to wait on.
             */
            processReferences.notifyAll();

            return process;
        }
    }

    static class ProcessImpl extends Process {

        /** Process ID. */
        final int id;

        final InputStream errorStream;

        /** Reads output from process. */
        final InputStream inputStream;

        /** Sends output to process. */
        final OutputStream outputStream;

        /** The process's exit value. */
        Integer exitValue = null;
        final Object exitValueMutex = new Object();

        ProcessImpl(int id, FileDescriptor in, FileDescriptor out,
                FileDescriptor err) {
            this.id = id;

            this.errorStream = new ProcessInputStream(err);
            this.inputStream = new ProcessInputStream(in);
            this.outputStream = new ProcessOutputStream(out);
        }

        public void destroy() {
            try {
                kill(this.id);
            } catch (IOException e) {
                Logger.getLogger(Runtime.class.getName()).log(Level.FINE,
                        "Failed to destroy process " + id + ".", e);
            }
        }

        public int exitValue() {
            synchronized (exitValueMutex) {
                if (exitValue == null) {
                    throw new IllegalThreadStateException(
                            "Process has not yet terminated.");
                }

                return exitValue;
            }
        }

        public InputStream getErrorStream() {
            return this.errorStream;
        }

        public InputStream getInputStream() {
            return this.inputStream;
        }

        public OutputStream getOutputStream() {
            return this.outputStream;
        }

        public int waitFor() throws InterruptedException {
            synchronized (exitValueMutex) {
                while (exitValue == null) {
                    exitValueMutex.wait();
                }
                return exitValue;
            }
        }

        void setExitValue(int exitValue) {
            synchronized (exitValueMutex) {
                this.exitValue = exitValue;
                exitValueMutex.notifyAll();
            }
        }

        @Override
        public String toString() {
            return "Process[id=" + id + "]";  
        }
    }

    static class ProcessReference extends WeakReference<ProcessImpl> {

        final int processId;

        public ProcessReference(ProcessImpl referent,
                ProcessReferenceQueue referenceQueue) {
            super(referent, referenceQueue);
            this.processId = referent.id;
        }
    }

    static class ProcessReferenceQueue extends ReferenceQueue<ProcessImpl> {

        @Override
        public ProcessReference poll() {
            // Why couldn't they get the generics right on ReferenceQueue? :(
            Object reference = super.poll();
            return (ProcessReference) reference;
        }
    }

    static final ProcessManager instance = new ProcessManager();

    /** Gets the process manager. */
    static ProcessManager getInstance() {
        return instance;
    }

    /** Automatically closes fd when collected. */
    private static class ProcessInputStream extends FileInputStream {

        private FileDescriptor fd;

        private ProcessInputStream(FileDescriptor fd) {
            super(fd);
            this.fd = fd;
        }

        @Override
        public void close() throws IOException {
            try {
                super.close();
            } finally {
                synchronized (this) {
                    if (fd != null && fd.valid()) {
                        try {
                            ProcessManager.close(fd);
                        } finally {
                            fd = null;
                        }
                    }
                }
            }
        }
    }

    /** Automatically closes fd when collected. */
    private static class ProcessOutputStream extends FileOutputStream {

        private FileDescriptor fd;

        private ProcessOutputStream(FileDescriptor fd) {
            super(fd);
            this.fd = fd;
        }

        @Override
        public void close() throws IOException {
            try {
                super.close();
            } finally {
                synchronized (this) {
                    if (fd != null && fd.valid()) {
                        try {
                            ProcessManager.close(fd);
                        } finally {
                            fd = null;
                        }
                    }
                }
            }
        }
    }

    /** Closes the given file descriptor. */
    private static native void close(FileDescriptor fd) throws IOException;
}
