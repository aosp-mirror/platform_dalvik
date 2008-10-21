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

package java.lang;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.io.OutputStreamWriter;

import java.util.StringTokenizer;
import java.util.List;
import java.util.ArrayList;

// BEGIN android-added
import dalvik.system.VMDebug;
import dalvik.system.VMStack;
// END android-added

/**
 * This class, with the exception of the exec() APIs, must be implemented by the
 * VM vendor. The exec() APIs must first do any required security checks, and
 * then call org.apache.harmony.luni.internal.process.SystemProcess.create().
 * The Runtime interface.
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
     * Execute progArray[0] in a separate platform process The new process
     * inherits the environment of the caller.
     * 
     * @param progArray the array containing the program to execute as well as
     *        any arguments to the program.
     * @throws java.io.IOException if the program cannot be executed
     * @throws SecurityException if the current SecurityManager disallows
     *         program execution
     * @see SecurityManager#checkExec
     */
    public Process exec(String[] progArray) throws java.io.IOException {
        return exec(progArray, null, null);
    }

    /**
     * Execute progArray[0] in a separate platform process The new process uses
     * the environment provided in envp
     * 
     * @param progArray the array containing the program to execute a well as
     *        any arguments to the program.
     * @param envp the array containing the environment to start the new process
     *        in.
     * @throws java.io.IOException if the program cannot be executed
     * @throws SecurityException if the current SecurityManager disallows
     *         program execution
     * @see SecurityManager#checkExec
     */
    public Process exec(String[] progArray, String[] envp) throws java.io.IOException {
        return exec(progArray, envp, null);
    }

    /**
     * Execute progArray[0] in a separate platform process. The new process uses
     * the environment provided in envp
     * 
     * @param progArray the array containing the program to execute a well as
     *        any arguments to the program.
     * @param envp the array containing the environment to start the new process
     *        in.
     * @param directory the directory in which to execute progArray[0]. If null,
     *        execute in same directory as parent process.
     * @throws java.io.IOException if the program cannot be executed
     * @throws SecurityException if the current SecurityManager disallows
     *         program execution
     * @see SecurityManager#checkExec
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
     * Execute program in a separate platform process The new process inherits
     * the environment of the caller.
     * 
     * @param prog the name of the program to execute
     * @throws java.io.IOException if the program cannot be executed
     * @throws SecurityException if the current SecurityManager disallows
     *         program execution
     * @see SecurityManager#checkExec
     */
    public Process exec(String prog) throws java.io.IOException {
        return exec(prog, null, null);
    }

    /**
     * Execute prog in a separate platform process The new process uses the
     * environment provided in envp
     * 
     * @param prog the name of the program to execute
     * @param envp the array containing the environment to start the new process
     *        in.
     * @throws java.io.IOException if the program cannot be executed
     * @throws SecurityException if the current SecurityManager disallows
     *         program execution
     * @see SecurityManager#checkExec
     */
    public Process exec(String prog, String[] envp) throws java.io.IOException {
        return exec(prog, envp, null);
    }

    /**
     * Execute prog in a separate platform process The new process uses the
     * environment provided in envp
     * 
     * @param prog the name of the program to execute
     * @param envp the array containing the environment to start the new process
     *        in.
     * @param directory the initial directory for the subprocess, or null to use
     *        the directory of the current process
     * @throws java.io.IOException if the program cannot be executed
     * @throws SecurityException if the current SecurityManager disallows
     *         program execution
     * @see SecurityManager#checkExec
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
     * Causes the virtual machine to stop running, and the program to exit. If
     * runFinalizersOnExit(true) has been invoked, then all finalizers will be
     * run first.
     * 
     * @param code the return code.
     * @throws SecurityException if the running thread is not allowed to cause
     *         the vm to exit.
     * @see SecurityManager#checkExit
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
                
                // Start all shutdown hooks concurrently
                for (int i = 0; i < shutdownHooks.size(); i++) {
                    shutdownHooks.get(i).start();
                }
        
                // Wait for all shutdown hooks to finish
                for (int i = 0; i < shutdownHooks.size(); i++) {
                    try {
                        shutdownHooks.get(i).join();
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
     */
    public native long freeMemory();

    /**
     * Indicates to the virtual machine that it would be a good time to collect
     * available memory. Note that, this is a hint only.
     * 
     */
    public native void gc();

    /**
     * Return the single Runtime instance
     * 
     */
    public static Runtime getRuntime() {
        return mRuntime;
    }

    /**
     * Loads and links the library specified by the argument.
     * 
     * @param pathName the absolute (ie: platform dependent) path to the library
     *        to load
     * @throws UnsatisfiedLinkError if the library could not be loaded
     * @throws SecurityException if the library was not allowed to be loaded
     */
    public void load(String pathName) {
        // Security checks
        SecurityManager smgr = System.getSecurityManager();
        if (smgr != null) {
            smgr.checkLink(pathName);
        }

        // BEGIN android-changed
        load(pathName, VMStack.getCallingClassLoader());
        // END android-changed
    }

    /*
     * Loads and links a library without security checks.
     */
    void load(String filename, ClassLoader loader) {
        nativeLoad(filename, loader);
    }
    
    /**
     * Loads and links the library specified by the argument.
     * 
     * @param libName the name of the library to load
     * @throws UnsatisfiedLinkError if the library could not be loaded
     * @throws SecurityException if the library was not allowed to be loaded
     */
    public void loadLibrary(String libName) {
        // Security checks
        SecurityManager smgr = System.getSecurityManager();
        if (smgr != null) {
            smgr.checkLink(libName);
        }
        
        // BEGIN android-changed
        loadLibrary(libName, VMStack.getCallingClassLoader());
        // END android-changed
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
     */
    public void runFinalization() {
        runFinalization(false);
    }

    /**
     * Ensure that, when the virtual machine is about to exit, all objects are
     * finalized. Note that all finalization which occurs when the system is
     * exiting is performed after all running threads have been terminated.
     * 
     * @param run true means finalize all on exit.
     * @deprecated This method is unsafe.
     */
    @Deprecated
    public static void runFinalizersOnExit(boolean run) {
        finalizeOnExit = true;
    }

    /**
     * Returns the total amount of memory resources which is available to (or in
     * use by) the running program.
     * 
     */
    public native long totalMemory();

    /**
     * Turns the output of debug information for instructions on or off.
     * 
     * @param enable if true, turn trace on. false turns trace off.
     */
    public void traceInstructions(boolean enable) {
        // TODO(Google) Provide some implementation for this.
        return;
    }

    /**
     * Turns the output of debug information for methods on or off.
     * 
     * @param enable if true, turn trace on. false turns trace off.
     */
    public void traceMethodCalls(boolean enable) {
        // BEGIN android-changed
        if (enable != tracingMethods) {
            if (enable) {
                VMDebug.startMethodTracing();
            } else {
                VMDebug.stopMethodTracing();
            }
            tracingMethods = enable;
        }
        // END android-changed
    }

    /**
     * @deprecated Use {@link InputStreamReader}
     */
    @Deprecated
    public InputStream getLocalizedInputStream(InputStream stream) {
        try {
            return new ReaderInputStream(new InputStreamReader(stream, "UTF-8"));
        }
        catch (UnsupportedEncodingException ex) {
            // Should never happen, since UTF-8 is mandatory.
            throw new RuntimeException(ex);
        }
    }

    /**
     * @deprecated Use {@link OutputStreamWriter}
     */
    @Deprecated
    public OutputStream getLocalizedOutputStream(OutputStream stream) {
        try {
            return new WriterOutputStream(new OutputStreamWriter(stream, "UTF-8"));
        }
        catch (UnsupportedEncodingException ex) {
            // Should never happen, since UTF-8 is mandatory.
            throw new RuntimeException(ex);
        }
    }

    /**
     * Registers a new virtual-machine shutdown hook.
     * 
     * @param hook the hook (a Thread) to register
     */
    public void addShutdownHook(Thread hook) {
        // Sanity checks
        if (hook == null) {
            throw new NullPointerException("null is not allowed here");
        }

        if (shuttingDown) {
            throw new IllegalArgumentException("VM already shutting down");
        }
        
        if (!shutdownHooks.contains(hook)) {
            shutdownHooks.add(hook);
        }
    }

    /**
     * De-registers a previously-registered virtual-machine shutdown hook.
     * 
     * @param hook the hook (a Thread) to de-register
     * @return true if the hook could be de-registered
     */
    public boolean removeShutdownHook(Thread hook) {
        // Sanity checks
        if (hook == null) {
            throw new NullPointerException("null is not allowed here");
        }
        
        if (shuttingDown) {
            throw new IllegalArgumentException("VM already shutting down");
        }
        
        return shutdownHooks.remove(hook);
    }

    /**
     * Causes the virtual machine to stop running, and the program to exit.
     * Finalizers will not be run first. Shutdown hooks will not be run.
     * 
     * @param code
     *            the return code.
     * @throws SecurityException
     *                if the running thread is not allowed to cause the vm to
     *                exit.
     * @see SecurityManager#checkExit
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
     * Return the number of processors, always at least one.
     */
    public int availableProcessors() {
        return 1;
    }

    /**
     * Return the maximum memory that will be used by the virtual machine, or
     * Long.MAX_VALUE.
     */
    public native long maxMemory();

}

/*
 * Internal helper class for creating a localized InputStream. A reader
 * wrapped in an InputStream. Bytes are read from characters in big-endian
 * fashion.
 */
class ReaderInputStream extends InputStream {
    
    private Reader reader;
    
    private byte[] bytes = new byte[256];
    
    private int nextByte;
    
    private int numBytes;
    
    public ReaderInputStream(Reader reader) {
        this.reader = reader;
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
        int numChars = reader.read(chars);
        
        for (int i = 0; i < chars.length; i++) {
            bytes[2 * i    ] = (byte)(chars[i] >> 8);
            bytes[2 * i + 1] = (byte)(chars[i] & 0xFF);
        }
        
        numBytes = numChars * 2;
        nextByte = 0;
    }        
    
}

/*
 * Internal helper class for creating a localized OutputStream. A writer
 * wrapped in an OutputStream. Bytes are written to characters in big-endian
 * fashion.
 */
class WriterOutputStream extends OutputStream {
    
    private Writer writer;
    
    private byte[] bytes = new byte[256];
    
    private int numBytes;
    
    public WriterOutputStream(Writer writer) {
        this.writer = writer;
    }
    
    @Override
    public void write(int b) throws IOException {
        bytes[numBytes++] = (byte)b;
        
        if (numBytes >= bytes.length) {
            writeBuffer();
        }
    }
    
    @Override
    public void flush() throws IOException {
        writeBuffer();
        writer.flush();
    }
    
    private void writeBuffer() throws IOException {
        char[] chars = new char[128];
        for (int i = 0; i < chars.length; i++) {
            chars[i] = (char)(bytes[2 * i] << 8 | bytes[2 * i + 1]);
        }
        
        writer.write(chars);
    }    
}
