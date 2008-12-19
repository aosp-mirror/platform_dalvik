/* 
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

package java.lang;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.Reader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import java.util.StringTokenizer;
import java.util.List;
import java.util.ArrayList;

import dalvik.system.VMDebug;
import dalvik.system.VMStack;

/**
 * Allows Java applications to interface with the environment in which they are
 * running. Applications can not create an instance of this class, but they can
 * get a singleton instance by invoking {@link #getRuntime()}.
 * 
 * @see System
 * 
 * @since Android 1.0
 */
public class Runtime {
    
    /**
     * Holds the Singleton global instance of Runtime.
     */
    private static final Runtime mRuntime = new Runtime();

    /**
     * Holds the library paths, used for native library lookup.
     */
    private final String[] mLibPaths;
    
    /**
     * Holds the list of threads to run when the VM terminates
     */
    private List<Thread> shutdownHooks = new ArrayList<Thread>();

    /**
     * Reflects whether finalization should be run for all objects
     * when the VM terminates.
     */
    private static boolean finalizeOnExit;
    
    /**
     * Reflects whether we are already shutting down the VM.
     */ 
    private boolean shuttingDown;

    /**
     * Reflects whether we are tracing method calls.
     */
    private boolean tracingMethods;
    
    /**
     * Prevent this class from being instantiated.
     */
    private Runtime(){
        String pathList = System.getProperty("java.library.path", ".");
        String pathSep = System.getProperty("path.separator", ":");
        String fileSep = System.getProperty("file.separator", "/");

        mLibPaths = pathList.split(pathSep);

        int i;

        if (false)
            System.out.println("Runtime paths:");

        // Add a '/' to the end so we don't have to do the property lookup
        // and concatenation later.
        for (i = 0; i < mLibPaths.length; i++) {
            if (!mLibPaths[i].endsWith(fileSep))
                mLibPaths[i] += fileSep;
            if (false)
                System.out.println("  " + mLibPaths[i]);
        }
    }
    
    /**
     * Executes the specified command and its arguments in a separate native
     * process. The new process inherits the environment of the caller. Calling
     * this method is equivalent to calling {@code exec(progArray, null, null)}.
     * 
     * @param progArray
     *            the array containing the program to execute as well as any
     *            arguments to the program.
     * @return the new {@code Process} object that represents the native
     *         process.
     * @throws IOException
     *             if the requested program can not be executed.
     * @throws SecurityException
     *             if the current {@code SecurityManager} disallows program
     *             execution.
     * @see SecurityManager#checkExec
     * @since Android 1.0
     */
    public Process exec(String[] progArray) throws java.io.IOException {
        return exec(progArray, null, null);
    }

    /**
     * Executes the specified command and its arguments in a separate native
     * process. The new process uses the environment provided in {@code envp}.
     * Calling this method is equivalent to calling
     * {@code exec(progArray, envp, null)}.
     * 
     * @param progArray
     *            the array containing the program to execute as well as any
     *            arguments to the program.
     * @param envp
     *            the array containing the environment to start the new process
     *            in.
     * @return the new {@code Process} object that represents the native
     *         process.
     * @throws IOException
     *             if the requested program can not be executed.
     * @throws SecurityException
     *             if the current {@code SecurityManager} disallows program
     *             execution.
     * @see SecurityManager#checkExec
     * @since Android 1.0
     */    
    public Process exec(String[] progArray, String[] envp) throws java.io.IOException {
        return exec(progArray, envp, null);
    }

    /**
     * Executes the specified command and its arguments in a separate native
     * process. The new process uses the environment provided in {@code envp}
     * and the working directory specified by {@code directory}.
     * 
     * @param progArray
     *            the array containing the program to execute as well as any
     *            arguments to the program.
     * @param envp
     *            the array containing the environment to start the new process
     *            in.
     * @param directory
     *            the directory in which to execute the program. If {@code null},
     *            execute if in the same directory as the parent process.
     * @return the new {@code Process} object that represents the native
     *         process.
     * @throws IOException
     *             if the requested program can not be executed.
     * @throws SecurityException
     *             if the current {@code SecurityManager} disallows program
     *             execution.
     * @see SecurityManager#checkExec
     * @since Android 1.0
     */    
    public Process exec(String[] progArray, String[] envp, File directory)
            throws java.io.IOException {
        
        // Sanity checks
        if (progArray == null) {
            throw new NullPointerException();
        } else if (progArray.length == 0) {
            throw new IndexOutOfBoundsException();
        } else {
            for (int i = 0; i < progArray.length; i++) {
                if (progArray[i] == null) {
                    throw new NullPointerException();
                }
            }
        }
        
        if (envp != null) {
            for (int i = 0; i < envp.length; i++) {
                if (envp[i] == null) {
                    throw new NullPointerException();
                }
            }
        }
        
        // Security checks
        SecurityManager smgr = System.getSecurityManager();
        if (smgr != null) {
            smgr.checkExec(progArray[0]);
        }
        
        // Delegate the execution
        return ProcessManager.getInstance().exec(progArray, envp, directory);
    }

    /**
     * Executes the specified program in a separate native process. The new
     * process inherits the environment of the caller. Calling this method is
     * equivalent to calling {@code exec(prog, null, null)}.
     * 
     * @param prog
     *            the name of the program to execute.
     * @return the new {@code Process} object that represents the native
     *         process.
     * @throws IOException
     *             if the requested program can not be executed.
     * @throws SecurityException
     *             if the current {@code SecurityManager} disallows program
     *             execution.
     * @see SecurityManager#checkExec
     * @since Android 1.0
     */
    public Process exec(String prog) throws java.io.IOException {
        return exec(prog, null, null);
    }

    /**
     * Executes the specified program in a separate native process. The new
     * process uses the environment provided in {@code envp}. Calling this
     * method is equivalent to calling {@code exec(prog, envp, null)}.
     * 
     * @param prog
     *            the name of the program to execute.
     * @param envp
     *            the array containing the environment to start the new process
     *            in.
     * @return the new {@code Process} object that represents the native
     *         process.
     * @throws IOException
     *             if the requested program can not be executed.
     * @throws SecurityException
     *             if the current {@code SecurityManager} disallows program
     *             execution.
     * @see SecurityManager#checkExec
     * @since Android 1.0
     */
    public Process exec(String prog, String[] envp) throws java.io.IOException {
        return exec(prog, envp, null);
    }

    /**
     * Executes the specified program in a separate native process. The new
     * process uses the environment provided in {@code envp} and the working
     * directory specified by {@code directory}.
     * 
     * @param prog
     *            the name of the program to execute.
     * @param envp
     *            the array containing the environment to start the new process
     *            in.
     * @param directory
     *            the directory in which to execute the program. If {@code null},
     *            execute if in the same directory as the parent process.
     * @return the new {@code Process} object that represents the native
     *         process.
     * @throws IOException
     *             if the requested program can not be executed.
     * @throws SecurityException
     *             if the current {@code SecurityManager} disallows program
     *             execution.
     * @see SecurityManager#checkExec
     * @since Android 1.0
     */
    public Process exec(String prog, String[] envp, File directory) throws java.io.IOException {
        // Sanity checks
        if (prog == null) {
            throw new NullPointerException();
        } else if (prog.length() == 0) {
            throw new IllegalArgumentException();
        }
        
        // Break down into tokens, as described in Java docs
        StringTokenizer tokenizer = new StringTokenizer(prog);
        int length = tokenizer.countTokens();
        String[] progArray = new String[length];
        for (int i = 0; i < length; i++) {
            progArray[i] = tokenizer.nextToken();
        }
        
        // Delegate
        return exec(progArray, envp, directory);
    }

    /**
     * Causes the virtual machine to stop running and the program to exit. If
     * {@link #runFinalizersOnExit(boolean)} has been previously invoked with a
     * {@code true} argument, then all all objects will be properly
     * garbage-collected and finalized first.
     * 
     * @param code
     *            the return code. By convention, non-zero return codes indicate
     *            abnormal terminations.
     * @throws SecurityException
     *             if the current {@code SecurityManager} does not allow the
     *             running thread to terminate the virtual machine.
     * @see SecurityManager#checkExit
     * @since Android 1.0
     */
    public void exit(int code) {
        // Security checks
        SecurityManager smgr = System.getSecurityManager();
        if (smgr != null) {
            smgr.checkExit(code);
        }

        // Make sure we don't try this several times
        synchronized(this) {
            if (!shuttingDown) {
                shuttingDown = true;

                Thread[] hooks;
                synchronized (shutdownHooks) {
                    // create a copy of the hooks
                    hooks = new Thread[shutdownHooks.size()];
                    shutdownHooks.toArray(hooks);
                }

                // Start all shutdown hooks concurrently
                for (int i = 0; i < hooks.length; i++) {
                    hooks[i].start();
                }

                // Wait for all shutdown hooks to finish
                for (Thread hook : hooks) {
                    try {
                        hook.join();
                    } catch (InterruptedException ex) {
                        // Ignore, since we are at VM shutdown.
                    }
                }

                // Ensure finalization on exit, if requested
                if (finalizeOnExit) {
                    runFinalization(true);
                }

                // Get out of here finally...
                nativeExit(code, true);
            }
        }
    }

    /**
     * Returns the amount of free memory resources which are available to the
     * running program.
     * 
     * @return the approximate amount of free memory, measured in bytes.
     * @since Android 1.0
     */
    public native long freeMemory();

    /**
     * Indicates to the virtual machine that it would be a good time to run the
     * garbage collector. Note that this is a hint only. There is no guarantee
     * that the garbage collector will actually be run.
     * 
     * @since Android 1.0
     */
    public native void gc();

    /**
     * Returns the single {@code Runtime} instance.
     * 
     * @return the {@code Runtime} object for the current application.
     * @since Android 1.0
     */
    public static Runtime getRuntime() {
        return mRuntime;
    }

    /**
     * Loads and links the dynamic library that is identified through the
     * specified path. This method is similar to {@link #loadLibrary(String)},
     * but it accepts a full path specification whereas {@code loadLibrary} just
     * accepts the name of the library to load.
     * 
     * @param pathName
     *            the absolute (platform dependent) path to the library to load.
     * @throws UnsatisfiedLinkError
     *             if the library can not be loaded.
     * @throws SecurityException
     *             if the current {@code SecurityManager} does not allow to load
     *             the library.
     * @see SecurityManager#checkLink
     * @since Android 1.0
     */
    public void load(String pathName) {
        // Security checks
        SecurityManager smgr = System.getSecurityManager();
        if (smgr != null) {
            smgr.checkLink(pathName);
        }

        load(pathName, VMStack.getCallingClassLoader());
    }

    /*
     * Loads and links a library without security checks.
     */
    void load(String filename, ClassLoader loader) {
        if (filename == null) {
            throw new NullPointerException("library path was null.");
        }
        if (!nativeLoad(filename, loader)) {
            throw new UnsatisfiedLinkError(
                    "Library " + filename + " not found");
        }
    }
    
    /**
     * Loads and links the library with the specified name. The mapping of the
     * specified library name to the full path for loading the library is
     * implementation-dependent.
     * 
     * @param libName
     *            the name of the library to load.
     * @throws UnsatisfiedLinkError
     *             if the library can not be loaded.
     * @throws SecurityException
     *             if the current {@code SecurityManager} does not allow to load
     *             the library.
     * @see SecurityManager#checkLink
     * @since Android 1.0
     */
    public void loadLibrary(String libName) {
        // Security checks
        SecurityManager smgr = System.getSecurityManager();
        if (smgr != null) {
            smgr.checkLink(libName);
        }

        loadLibrary(libName, VMStack.getCallingClassLoader());
    }

    /*
     * Loads and links a library without security checks.
     */
    void loadLibrary(String libname, ClassLoader loader) {
        String filename;
        int i;

        if (loader != null) {
            filename = loader.findLibrary(libname);
            if (filename != null && nativeLoad(filename, loader))
                return;
            // else fall through to exception
        } else {
            filename = System.mapLibraryName(libname);
            for (i = 0; i < mLibPaths.length; i++) {
                if (false)
                    System.out.println("Trying " + mLibPaths[i] + filename);
                if (nativeLoad(mLibPaths[i] + filename, loader))
                    return;
            }
        }

        throw new UnsatisfiedLinkError("Library " + libname + " not found");
    }
    
    private static native void nativeExit(int code, boolean isExit);
    
    private static native boolean nativeLoad(String filename,
            ClassLoader loader);
    
    /**
     * Requests proper finalization for all Objects on the heap.
     * 
     * @param forced Decides whether the VM really needs to do this (true)
     *               or if this is just a suggestion that can safely be ignored
     *               (false).
     */
    private native void runFinalization(boolean forced);

    /**
     * Provides a hint to the virtual machine that it would be useful to attempt
     * to perform any outstanding object finalizations.
     * 
     * @since Android 1.0
     */
    public void runFinalization() {
        runFinalization(false);
    }

    /**
     * Sets the flag that indicates whether all objects are finalized when the
     * virtual machine is about to exit. Note that all finalization which occurs
     * when the system is exiting is performed after all running threads have
     * been terminated.
     * 
     * @param run
     *            {@code true} to enable finalization on exit, {@code false} to
     *            disable it.
     * @deprecated This method is unsafe.
     * @since Android 1.0
     */
    @Deprecated
    public static void runFinalizersOnExit(boolean run) {
        SecurityManager smgr = System.getSecurityManager();
        if (smgr != null) {
            smgr.checkExit(0);
        }
        finalizeOnExit = run;
    }

    /**
     * Returns the total amount of memory which is available to the running
     * program.
     * 
     * @return the total amount of memory, measured in bytes.
     * @since Android 1.0
     */
    public native long totalMemory();

    /**
     * Switches the output of debug information for instructions on or off.
     * For the Android 1.0 reference implementation, this method does nothing.
     * 
     * @param enable
     *            {@code true} to switch tracing on, {@code false} to switch it
     *            off.
     * @since Android 1.0
     */
    public void traceInstructions(boolean enable) {
        // TODO(Google) Provide some implementation for this.
        return;
    }

    /**
     * Switches the output of debug information for methods on or off.
     * 
     * @param enable
     *            {@code true} to switch tracing on, {@code false} to switch it
     *            off.
     * @since Android 1.0
     */
    public void traceMethodCalls(boolean enable) {
        if (enable != tracingMethods) {
            if (enable) {
                VMDebug.startMethodTracing();
            } else {
                VMDebug.stopMethodTracing();
            }
            tracingMethods = enable;
        }
    }

    /**
     * Returns the localized version of the specified input stream. The input
     * stream that is returned automatically converts all characters from the
     * local character set to Unicode after reading them from the underlying
     * stream.
     * 
     * @param stream
     *            the input stream to localize.
     * @return the localized input stream.
     * @deprecated Use {@link InputStreamReader}.
     * @since Android 1.0
     */
    @Deprecated
    public InputStream getLocalizedInputStream(InputStream stream) {
        if (System.getProperty("file.encoding", "UTF-8").equals("UTF-8")) {
            return stream;
        }
        return new ReaderInputStream(stream);
    }

    /**
     * Returns the localized version of the specified output stream. The output
     * stream that is returned automatically converts all characters from
     * Unicode to the local character set before writing them to the underlying
     * stream.
     * 
     * @param stream
     *            the output stream to localize.
     * @return the localized output stream.
     * @deprecated Use {@link OutputStreamWriter}.
     * @since Android 1.0
     */    
    @Deprecated
    public OutputStream getLocalizedOutputStream(OutputStream stream) {
        if (System.getProperty("file.encoding", "UTF-8").equals("UTF-8")) {
            return stream;
        }
        return new WriterOutputStream(stream );
    }

    /**
     * Registers a virtual-machine shutdown hook. A shutdown hook is a
     * {@code Thread} that is ready to run, but has not yet been started. All
     * registered shutdown hooks will be executed once the virtual machine shuts
     * down properly. A proper shutdown happens when either the
     * {@link #exit(int)} method is called or the surrounding system decides to
     * terminate the application, for example in response to a {@code CTRL-C} or
     * a system-wide shutdown. A termination of the virtual machine due to the
     * {@link #halt(int)} method, an {@link Error} or a {@code SIGKILL}, in
     * contrast, is not considered a proper shutdown. In these cases the
     * shutdown hooks will not be run.
     * <p>
     * Shutdown hooks are run concurrently and in an unspecified order. Hooks
     * failing due to an unhandled exception are not a problem, but the stack
     * trace might be printed to the console. Once initiated, the whole shutdown
     * process can only be terminated by calling {@code halt()}.
     * <p>
     * If {@link #runFinalizersOnExit(boolean)} has been called with a {@code
     * true} argument, garbage collection and finalization will take place after
     * all hooks are either finished or have failed. Then the virtual machine
     * terminates.
     * <p>
     * It is recommended that shutdown hooks do not do any time-consuming
     * activities, in order to not hold up the shutdown process longer than
     * necessary.
     * 
     * @param hook
     *            the shutdown hook to register.
     * @throws IllegalArgumentException
     *             if the hook has already been started or if it has already
     *             been registered.
     * @throws IllegalStateException
     *             if the virtual machine is already shutting down.
     * @throws SecurityException
     *             if a SecurityManager is registered and the calling code
     *             doesn't have the RuntimePermission("shutdownHooks").
     */
    public void addShutdownHook(Thread hook) {
        // Sanity checks
        if (hook == null) {
            throw new NullPointerException("Hook may not be null.");
        }

        if (shuttingDown) {
            throw new IllegalStateException("VM already shutting down");
        }

        if (hook.hasBeenStarted) {
            throw new IllegalArgumentException("Hook has already been started");
        }

        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new RuntimePermission("shutdownHooks"));
        }
        
        synchronized (shutdownHooks) {
            if (shutdownHooks.contains(hook)) {
                throw new IllegalArgumentException("Hook already registered.");
            }
    
            shutdownHooks.add(hook);
        }
    }

    /**
     * Unregisters a previously registered virtual machine shutdown hook.
     * 
     * @param hook
     *            the shutdown hook to remove.
     * @return {@code true} if the hook has been removed successfully; {@code
     *         false} otherwise.
     * @throws IllegalStateException
     *             if the virtual machine is already shutting down.
     * @throws SecurityException
     *             if a SecurityManager is registered and the calling code
     *             doesn't have the RuntimePermission("shutdownHooks").
     */
    public boolean removeShutdownHook(Thread hook) {
        // Sanity checks
        if (hook == null) {
            throw new NullPointerException("Hook may not be null.");
        }
        
        if (shuttingDown) {
            throw new IllegalStateException("VM already shutting down");
        }

        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new RuntimePermission("shutdownHooks"));
        }

        synchronized (shutdownHooks) {
            return shutdownHooks.remove(hook);
        }
    }

    /**
     * Causes the virtual machine to stop running, and the program to exit.
     * Neither shutdown hooks nor finalizers are run before.
     * 
     * @param code
     *            the return code. By convention, non-zero return codes indicate
     *            abnormal terminations.
     * @throws SecurityException
     *             if the current {@code SecurityManager} does not allow the
     *             running thread to terminate the virtual machine.
     * @see SecurityManager#checkExit
     * @see #addShutdownHook(Thread)
     * @see #removeShutdownHook(Thread)
     * @see #runFinalizersOnExit(boolean)
     * @since Android 1.0
     */
    public void halt(int code) {
        // Security checks
        SecurityManager smgr = System.getSecurityManager();
        if (smgr != null) {
            smgr.checkExit(code);
        }
        
        // Get out of here...
        nativeExit(code, false);
    }

    /**
     * Returns the number of processors available to the virtual machine. The
     * Android reference implementation (currently) always returns 1.
     * 
     * @return the number of available processors, at least 1.
     * @since Android 1.0
     */
    public int availableProcessors() {
        return 1;
    }

    /**
     * Returns the maximum amount of memory that may be used by the virtual
     * machine, or {@code Long.MAX_VALUE} if there is no such limit.
     * 
     * @return the maximum amount of memory that the virtual machine will try to
     *         allocate, measured in bytes.
     * @since Android 1.0
     */
    public native long maxMemory();

}

/*
 * Internal helper class for creating a localized InputStream. A reader
 * wrapped in an InputStream.
 */
class ReaderInputStream extends InputStream {

    private Reader reader;
    
    private Writer writer;

    ByteArrayOutputStream out = new ByteArrayOutputStream(256);
    
    private byte[] bytes;
    
    private int nextByte;
    
    private int numBytes;
    
    String encoding = System.getProperty("file.encoding", "UTF-8");
    
    public ReaderInputStream(InputStream stream) {
        try {
            reader = new InputStreamReader(stream, "UTF-8");
            writer = new OutputStreamWriter(out, encoding);
        } catch (UnsupportedEncodingException e) {
            // Should never happen, since UTF-8 and platform encoding must be
            // supported.
            throw new RuntimeException(e);
        }
    }

    @Override
    public int read() throws IOException {
        if (nextByte >= numBytes) {
            readBuffer();
        }

        return (numBytes < 0) ? -1 : bytes[nextByte++];
    }

    private void readBuffer() throws IOException {
        char[] chars = new char[128];
        int read = reader.read(chars);
        if (read < 0) {
            numBytes = read;
            return;
        }

        writer.write(chars, 0, read);
        writer.flush();
        bytes = out.toByteArray();
        numBytes = bytes.length;
        nextByte = 0;
    }        
    
}

/*
 * Internal helper class for creating a localized OutputStream. A writer
 * wrapped in an OutputStream. Bytes are written to characters in big-endian
 * fashion.
 */
class WriterOutputStream extends OutputStream {

    private Reader reader;

    private Writer writer;

    private PipedOutputStream out;

    private PipedInputStream pipe;

    private int numBytes;

    private String enc = System.getProperty("file.encoding", "UTF-8");

    public WriterOutputStream(OutputStream stream) {
        try {
            // sink
            this.writer = new OutputStreamWriter(stream, enc);

            // transcriber
            out = new PipedOutputStream();
            pipe = new PipedInputStream(out);
            this.reader = new InputStreamReader(pipe, "UTF-8");

        } catch (UnsupportedEncodingException e) {
            // Should never happen, since platform encoding must be supported.
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void write(int b) throws IOException {
        out.write(b);
        if( ++numBytes > 256) {
            flush();
            numBytes = 0;
        }
    }

    @Override
    public void flush() throws IOException {
        out.flush();
        char[] chars = new char[128];
        if (pipe.available() > 0) {
            int read = reader.read(chars);
            if (read > 0) {
                writer.write(chars, 0, read);
            }
        }
        writer.flush();
    }

    @Override
    public void close() throws IOException {
        out.close();
        flush();
        writer.close();
    }
}
