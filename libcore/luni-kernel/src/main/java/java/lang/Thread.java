// BEGIN android-note
// Reworded description of Thread
// END android-note

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

// BEGIN android-added
import dalvik.system.VMStack;
// END android-added

import java.util.Map;
import java.util.HashMap;
// BEGIN android-changed
//import java.util.WeakHashMap;
// END android-changed

import java.security.AccessController;

import org.apache.harmony.security.fortress.SecurityUtils;

/**
 * A Thread is a unit of concurrent execution. It has its own
 * call stack for methods being called and their parameters. Threads in the same
 * VM interact and synchronize by the use of shared Objects and monitors
 * associated with these objects. Synchronized methods and part of the API in
 * Object also allow Threads to cooperate.
 *
 * @see java.lang.Object
 * @see java.lang.ThreadGroup
 */
public class Thread implements Runnable {

    // BEGIN android-added

    /** Park states */
    private static class ParkState {
        /** park state indicating unparked */
        private static final int UNPARKED = 1;
    
        /** park state indicating preemptively unparked */
        private static final int PREEMPTIVELY_UNPARKED = 2;
    
        /** park state indicating parked */
        private static final int PARKED = 3;
    };

    // END android-added

    /**
     * A representation of a thread's state. A given thread may only be in one
     * state at a time.
     *
     * @since 1.5
     */
    public enum State {
        /**
         * The thread is blocked and waiting for a lock.
         */
        BLOCKED,
        /**
         * The thread has been created, but has never been started.
         */
        NEW,
        /**
         * The thread may be run.
         */
        RUNNABLE,
        /**
         * The thread has been terminated.
         */
        TERMINATED,
        /**
         * The thread is waiting for a specified amount of time.
         */
        TIMED_WAITING,
        /**
         * The thread is waiting.
         */
        WAITING
    }

    /**
     * <p>
     * The maximum priority value allowed for a thread.
     * </p>
     */
    public final static int MAX_PRIORITY = 10;

    /**
     * <p>
     * The minimum priority value allowed for a thread.
     * </p>
     */
    public final static int MIN_PRIORITY = 1;

    /**
     * <p>
     * The normal (default) priority value assigned to threads.
     * </p>
     */
    public final static int NORM_PRIORITY = 5;

    /* some of these are accessed directly by the VM; do not rename them */
    volatile VMThread vmThread;
    volatile ThreadGroup group;
    volatile boolean daemon;
    volatile String name;
    volatile int priority;
    volatile long stackSize;
    Runnable target;
    private static int count = 0;

    /**
     * Holds the thread's ID. We simply count upwards, so
     * each Thread has a unique ID.
     */
    private long id;

// BEGIN android-chnged
    /**
     * Normal thread local values.
     */
    ThreadLocal.Values localValues;

    /**
     * Inheritable thread local values.
     */
    ThreadLocal.Values inheritableValues;
// END android-changed

    /**
     * Holds the interrupt action for this Thread, if any.
     * <p>
     * This is required internally by NIO, so even if it looks like it's
     * useless, don't delete it!
     */
    private Runnable interruptAction;

    /**
     * Holds the class loader for this Thread, in case there is one.
     */
    private ClassLoader contextClassLoader;

    /**
     * Holds the handler for uncaught exceptions in this Thread,
     * in case there is one.
     */
    private UncaughtExceptionHandler uncaughtHandler;

    /**
     * Holds the default handler for uncaught exceptions, in case there is one.
     */
    private static UncaughtExceptionHandler defaultUncaughtHandler;

    /**
     * Reflects whether this Thread has already been started. A Thread
     * can only be started once (no recycling). Also, we need it to deduce
     * the proper Thread status.
     */
    boolean hasBeenStarted = false;

    // BEGIN android-added
    /** the park state of the thread */
    private int parkState = ParkState.UNPARKED;
    // END android-added
        
    /**
     * Constructs a new Thread with no runnable object and a newly generated
     * name. The new Thread will belong to the same ThreadGroup as the Thread
     * calling this constructor.
     *
     * @see java.lang.ThreadGroup
     */
    public Thread() {
        create(null, null, null, 0);
    }

    /**
     * Constructs a new Thread with a runnable object and a newly generated
     * name. The new Thread will belong to the same ThreadGroup as the Thread
     * calling this constructor.
     *
     * @param runnable a java.lang.Runnable whose method <code>run</code> will
     *        be executed by the new Thread
     * @see java.lang.ThreadGroup
     * @see java.lang.Runnable
     */
    public Thread(Runnable runnable) {
        create(null, runnable, null, 0);
    }

    /**
     * Constructs a new Thread with a runnable object and name provided. The new
     * Thread will belong to the same ThreadGroup as the Thread calling this
     * constructor.
     *
     * @param runnable a java.lang.Runnable whose method <code>run</code> will
     *        be executed by the new Thread
     * @param threadName Name for the Thread being created
     * @see java.lang.ThreadGroup
     * @see java.lang.Runnable
     */
    public Thread(Runnable runnable, String threadName) {
        if (threadName == null) {
            throw new NullPointerException();
        }

        create(null, runnable, threadName, 0);
    }

    /**
     * Constructs a new Thread with no runnable object and the name provided.
     * The new Thread will belong to the same ThreadGroup as the Thread calling
     * this constructor.
     *
     * @param threadName Name for the Thread being created
     * @see java.lang.ThreadGroup
     * @see java.lang.Runnable
     */
    public Thread(String threadName) {
        if (threadName == null) {
            throw new NullPointerException();
        }

        create(null, null, threadName, 0);
    }

    /**
     * Constructs a new Thread with a runnable object and a newly generated
     * name. The new Thread will belong to the ThreadGroup passed as parameter.
     *
     * @param group ThreadGroup to which the new Thread will belong
     * @param runnable a java.lang.Runnable whose method <code>run</code> will
     *        be executed by the new Thread
     * @throws SecurityException if <code>group.checkAccess()</code> fails
     *         with a SecurityException
     * @throws IllegalThreadStateException if <code>group.destroy()</code> has
     *         already been done
     * @see java.lang.ThreadGroup
     * @see java.lang.Runnable
     * @see java.lang.SecurityException
     * @see java.lang.SecurityManager
     */
    public Thread(ThreadGroup group, Runnable runnable) {
        create(group, runnable, null, 0);
    }

    /**
     * Constructs a new Thread with a runnable object, the given name and
     * belonging to the ThreadGroup passed as parameter.
     *
     * @param group ThreadGroup to which the new Thread will belong
     * @param runnable a java.lang.Runnable whose method <code>run</code> will
     *        be executed by the new Thread
     * @param threadName Name for the Thread being created
     * @throws SecurityException if <code>group.checkAccess()</code> fails
     *         with a SecurityException
     * @throws IllegalThreadStateException if <code>group.destroy()</code> has
     *         already been done
     * @see java.lang.ThreadGroup
     * @see java.lang.Runnable
     * @see java.lang.SecurityException
     * @see java.lang.SecurityManager
     */
    public Thread(ThreadGroup group, Runnable runnable, String threadName) {
        if (threadName == null) {
            throw new NullPointerException();
        }

        create(group, runnable, threadName, 0);
    }

    /**
     * Constructs a new Thread with no runnable object, the given name and
     * belonging to the ThreadGroup passed as parameter.
     *
     * @param group ThreadGroup to which the new Thread will belong
     * @param threadName Name for the Thread being created
     * @throws SecurityException if <code>group.checkAccess()</code> fails
     *         with a SecurityException
     * @throws IllegalThreadStateException if <code>group.destroy()</code> has
     *         already been done
     * @see java.lang.ThreadGroup
     * @see java.lang.SecurityException
     * @see java.lang.SecurityManager
     */
    public Thread(ThreadGroup group, String threadName) {
        if (threadName == null) {
            throw new NullPointerException();
        }

        create(group, null, threadName, 0);
    }

    /**
     * Constructs a new Thread with a runnable object, the given name and
     * belonging to the ThreadGroup passed as parameter.
     *
     * @param group ThreadGroup to which the new Thread will belong
     * @param runnable a java.lang.Runnable whose method <code>run</code> will
     *        be executed by the new Thread
     * @param threadName Name for the Thread being created
     * @param stackSize Platform dependent stack size
     * @throws SecurityException if <code>group.checkAccess()</code> fails
     *         with a SecurityException
     * @throws IllegalThreadStateException if <code>group.destroy()</code> has
     *         already been done
     * @see java.lang.ThreadGroup
     * @see java.lang.Runnable
     * @see java.lang.SecurityException
     * @see java.lang.SecurityManager
     */
    public Thread(ThreadGroup group, Runnable runnable, String threadName, long stackSize) {
        if (threadName == null) {
            throw new NullPointerException();
        }
        create(group, runnable, threadName, stackSize);
    }

    /**
     * Package-scope method invoked by Dalvik VM to create "internal"
     * threads or attach threads created externally.
     *
     * Don't call Thread.currentThread(), since there may not be such
     * a thing (e.g. for Main).
     */
    Thread(ThreadGroup group, String name, int priority, boolean daemon) {
        synchronized (Thread.class) {
            id = ++Thread.count;
        }

        if (name == null) {
            this.name = "Thread-" + id;
        } else
            this.name = name;

        if (group == null) {
            throw new InternalError("group not specified");
        }

        this.group = group;

        this.target = null;
        this.stackSize = 0;
        this.priority = priority;
        this.daemon = daemon;

        /* add ourselves to our ThreadGroup of choice */
        this.group.addThread(this);
    }

    /**
     * Initializes a new, existing Thread object with a runnable object,
     * the given name and belonging to the ThreadGroup passed as parameter.
     * This is the method that the several public constructors delegate their
     * work to.
     *
     * @param group ThreadGroup to which the new Thread will belong
     * @param runnable a java.lang.Runnable whose method <code>run</code> will
     *        be executed by the new Thread
     * @param threadName Name for the Thread being created
     * @param stackSize Platform dependent stack size
     * @throws SecurityException if <code>group.checkAccess()</code> fails
     *         with a SecurityException
     * @throws IllegalThreadStateException if <code>group.destroy()</code> has
     *         already been done
     * @see java.lang.ThreadGroup
     * @see java.lang.Runnable
     * @see java.lang.SecurityException
     * @see java.lang.SecurityManager
     */
    private void create(ThreadGroup group, Runnable runnable, String threadName, long stackSize) {
        SecurityManager smgr = System.getSecurityManager();
        if (smgr != null) {
            if (group == null) {
                group = smgr.getThreadGroup();
            }

            /*
             * Freaky security requirement: If the Thread's class is actually
             * a subclass of Thread and it tries to override either
             * getContextClassLoader() or setContextClassLoader(), the
             * SecurityManager has to allow this.
             */
            if (getClass() != Thread.class) {
                Class[] signature = new Class[] { ClassLoader.class };

                try {
                    getClass().getDeclaredMethod("getContextClassLoader", signature);
                    smgr.checkPermission(new RuntimePermission("enableContextClassLoaderOverride"));
                } catch (NoSuchMethodException ex) {
                    // Ignore. Just interested in the method's existence.
                }

                try {
                    getClass().getDeclaredMethod("setContextClassLoader", signature);
                    smgr.checkPermission(new RuntimePermission("enableContextClassLoaderOverride"));
                } catch (NoSuchMethodException ex) {
                    // Ignore. Just interested in the method's existence.
                }
            }
        }

// BEGIN android-changed
        Thread currentThread = Thread.currentThread();
        if (group == null) {
            group = currentThread.getThreadGroup();
        }

        group.checkAccess();
        if (group.isDestroyed()) {
            throw new IllegalThreadStateException("Group already destroyed");
        }

        this.group = group;

        synchronized (Thread.class) {
            id = ++Thread.count;
        }

        if (threadName == null) {
            this.name = "Thread-" + id;
        } else {
            this.name = threadName;
        }

        this.target = runnable;
        this.stackSize = stackSize;

        this.priority = currentThread.getPriority();

        // Transfer over InheritableThreadLocals.
        if (currentThread.inheritableValues != null) {
            inheritableValues
                    = new ThreadLocal.Values(currentThread.inheritableValues);
        }
// END android-changed

        // store current AccessControlContext as inherited context for this thread
        SecurityUtils.putContext(this, AccessController.getContext());

        // add ourselves to our ThreadGroup of choice
        this.group.addThread(this);
    }

    /**
     * Returns the number of active threads in the running thread's ThreadGroup
     * and its subgroups.
     *
     * @return Number of Threads
     */
    public static int activeCount() {
        return currentThread().getThreadGroup().activeCount();
    }

    /**
     * This method is used for operations that require approval from a
     * SecurityManager. If there's none installed, this method is a no-op. If
     * there's a SecurityManager installed ,
     * {@link SecurityManager#checkAccess(Thread)} is called for that
     * SecurityManager.
     *
     * @see java.lang.SecurityException
     * @see java.lang.SecurityManager
     */
    public final void checkAccess() {
        // Forwards the message to the SecurityManager (if there's one) passing
        // the receiver as parameter

        SecurityManager currentManager = System.getSecurityManager();
        if (currentManager != null) {
            currentManager.checkAccess(this);
        }
    }

    /**
     * Returns the number of stack frames in this thread.
     *
     * @return Number of stack frames
     * @deprecated The results of this call were never well defined. To make
     *             things worse, it would depend if the Thread was suspended or
     *             not, and suspend was deprecated too.
     */
    @Deprecated
    public int countStackFrames() {
        return getStackTrace().length;
    }

    /**
     * Returns the instance of Thread that corresponds to the running Thread
     * which calls this method.
     *
     * @return a java.lang.Thread corresponding to the code that called
     *         <code>currentThread()</code>
     */
    public static Thread currentThread() {
        return VMThread.currentThread();
    }

    /**
     * Destroys the receiver without any monitor cleanup.
     *
     * @deprecated Not implemented.
     */
    @Deprecated
    public void destroy() {
        throw new NoSuchMethodError("Thread.destroy()"); // TODO Externalize???
    }

    /**
     * Prints a text representation of the stack for this Thread.
     *
     */
    public static void dumpStack() {
        new Throwable("stack dump").printStackTrace();
    }

    /**
     * Copies an array with all Threads which are in the same ThreadGroup as the
     * receiver - and subgroups - into the array <code>threads</code> passed
     * as parameter. If the array passed as parameter is too small no exception
     * is thrown - the extra elements are simply not copied.
     *
     * @param threads array into which the Threads will be copied
     * @return How many Threads were copied over
     * @throws SecurityException if the installed SecurityManager fails
     *         {@link SecurityManager#checkAccess(Thread)}
     * @see java.lang.SecurityException
     * @see java.lang.SecurityManager
     */
    public static int enumerate(Thread[] threads) {
        Thread thread = Thread.currentThread();
        thread.checkAccess();
        return thread.getThreadGroup().enumerate(threads);
    }

    /**
     * <p>
     * Returns the stack traces of all the currently live threads and puts them into
     * the given map.
     * </p>
     *
     * @return A Map of current Threads to StackTraceElement arrays.
     * @throws SecurityException if the current SecurityManager fails the
     *         {@link SecurityManager#checkPermission(java.security.Permission)}
     *         call.
     * @since 1.5
     */
    public static Map<Thread, StackTraceElement[]> getAllStackTraces() {
        SecurityManager securityManager = System.getSecurityManager();
        if (securityManager != null) {
            securityManager.checkPermission(new RuntimePermission("getStackTrace"));
            securityManager.checkPermission(new RuntimePermission("modifyThreadGroup"));
        }

        Map<Thread, StackTraceElement[]> map = new HashMap<Thread, StackTraceElement[]>();

        // Find out how many live threads we have. Allocate a bit more
        // space than needed, in case new ones are just being created.
        int count = ThreadGroup.mSystem.activeCount();
        Thread[] threads = new Thread[count + count / 2];

        // Enumerate the threads and collect the stacktraces.
        count = ThreadGroup.mSystem.enumerate(threads);
        for (int i = 0; i < count; i++) {
            map.put(threads[i], threads[i].getStackTrace());
        }

        return map;
    }

    /**
     * Returns the context ClassLoader for the receiver.
     * <p>
     * If the conditions
     * <ol>
     *   <li> there is a security manager
     *   <li> the caller's class loader is not null
     *   <li> the caller's class loader is not the same as or an
     *        ancestor of contextClassLoader
     * </ol>
     * are satisfied, a security check for
     * <code>RuntimePermission("getClassLoader")</code> is performed first.
     *
     * @return ClassLoader The context ClassLoader
     * @see java.lang.ClassLoader
     * @see #getContextClassLoader()
     *
     * @throws SecurityException If aforementioned security check fails.
     */
    public ClassLoader getContextClassLoader() {
        // First, if the conditions
        //    1) there is a security manager
        //    2) the caller's class loader is not null
        //    3) the caller's class loader is not the same as or an
        //    ancestor of contextClassLoader
        // are satisfied we should perform a security check.
        SecurityManager securityManager = System.getSecurityManager();
        if (securityManager != null) {
            ClassLoader callerClassLoader =
                    VMStack.getCallingClassLoader();
            if (callerClassLoader != null) {
                ClassLoader classLoader = contextClassLoader;
                while (classLoader != null) {
                    if (classLoader == callerClassLoader) {
                        return contextClassLoader;
                    }
                    classLoader = classLoader.getParent();
                }
                securityManager.checkPermission(new RuntimePermission(
                        "getClassLoader"));
            }
        }

        return contextClassLoader;
    }

    /**
     * <p>
     * Returns the default exception handler that's executed when uncaught
     * exception terminates a thread.
     * </p>
     *
     * @return An {@link UncaughtExceptionHandler} or <code>null</code> if
     *         none exists.
     * @since 1.5
     */
    public static UncaughtExceptionHandler getDefaultUncaughtExceptionHandler() {
        return defaultUncaughtHandler;
    }

    /**
     * <p>
     * Returns the thread's identifier. The ID is a positive <code>long</code>
     * generated on thread creation, is unique to the thread and doesn't change
     * during the life of the thread; the ID may be reused after the thread has
     * been terminated.
     * </p>
     *
     * @return The thread's ID.
     * @since 1.5
     */
    public long getId() {
        return id;
    }

    /**
     * Returns the name of the receiver.
     *
     * @return the receiver's name (a java.lang.String)
     */
    public final String getName() {
        return name;
    }

    /**
     * Returns the priority of the receiver.
     *
     * @return the receiver's priority (an <code>int</code>)
     * @see Thread#setPriority
     */
    public final int getPriority() {
        return priority;
    }

    /**
     * <p>
     * Returns the current stack trace of the thread.
     * </p>
     * <p>
     * The <code>RuntimePermission("getStackTrace")</code> is checked before
     * returning a result.
     * </p>
     *
     * @return An array of StackTraceElements.
     * @throws SecurityException if the current SecurityManager fails the
     *         {@link SecurityManager#checkPermission(java.security.Permission)}
     *         call.
     * @since 1.5
     */
    public StackTraceElement[] getStackTrace() {
        SecurityManager securityManager = System.getSecurityManager();
        if (securityManager != null) {
            securityManager.checkPermission(new RuntimePermission("getStackTrace"));
        }

        StackTraceElement ste[] = VMStack.getThreadStackTrace(this);
        return ste != null ? ste : new StackTraceElement[0];
    }

    /**
     * <p>
     * Returns the current state of the thread for monitoring purposes.
     * </p>
     *
     * @return A State value.
     * @since 1.5
     */
    public State getState() {
        // TODO Ticket 164: This is ugly and should be implemented better.
        VMThread vmt = this.vmThread;

        // Make sure we have a valid reference to an object. If native code
        // deletes the reference we won't run into a null reference later.
        VMThread thread = vmThread;
        if (thread != null) {
            // If the Thread Object became invalid or was not yet started,  
            // getStatus() will return -1.
            int state = thread.getStatus();
            if(state != -1) {
                return VMThread.STATE_MAP[state];
            }
        }
        return hasBeenStarted ? Thread.State.TERMINATED : Thread.State.NEW;
    }
    
    /**
     * Returns the ThreadGroup to which the receiver belongs
     *
     * @return the receiver's ThreadGroup
     */
    public final ThreadGroup getThreadGroup() {
        // TODO Ticket 164: This should actually be done at native termination.
        if (getState() == Thread.State.TERMINATED) {
            return null;
        } else {
            return group;
        }
    }

    /**
     * <p>
     * Returns the thread's uncaught exception handler. If not explicitly set,
     * then the ThreadGroup's handler is returned. If the thread is terminated,
     * then <code>null</code> is returned.
     * </p>
     *
     * @return An UncaughtExceptionHandler instance or <code>null</code>.
     * @since 1.5
     */
    public UncaughtExceptionHandler getUncaughtExceptionHandler() {
        if (uncaughtHandler != null)
            return uncaughtHandler;
        else
            return group;           // ThreadGroup is instance of UEH
    }

    /**
     * Posts an interrupt request to the receiver.
     *
     * @throws SecurityException if <code>checkAccess()</code> fails
     *         with a SecurityException
     * @see java.lang.SecurityException
     * @see java.lang.SecurityManager
     * @see Thread#interrupted
     * @see Thread#isInterrupted
     */
    public void interrupt() {
        checkAccess();

        if (interruptAction != null) {
            interruptAction.run();
        }

        VMThread vmt = this.vmThread;
        if (vmt != null) {
            vmt.interrupt();
        }
    }

    /**
     * Returns a <code>boolean</code> indicating whether the current Thread (
     * <code>currentThread()</code>) has a pending interrupt request (
     * <code>true</code>) or not (<code>false</code>). It also has the
     * side-effect of clearing the flag.
     *
     * @return a <code>boolean</code>
     * @see Thread#currentThread
     * @see Thread#interrupt
     * @see Thread#isInterrupted
     */
    public static boolean interrupted() {
        return VMThread.interrupted();
    }

    /**
     * Returns <code>true</code> if the receiver has already been started and
     * still runs code (hasn't died yet). Returns <code>false</code> either if
     * the receiver hasn't been started yet or if it has already started and run
     * to completion and died.
     *
     * @return a <code>boolean</code>
     * @see Thread#start
     */
    public final boolean isAlive() {
        Thread.State state = getState();

        return (state != Thread.State.TERMINATED && state != Thread.State.NEW);
    }

    /**
     * Returns a <code>boolean</code> indicating whether the receiver is a
     * daemon Thread (<code>true</code>) or not (<code>false</code>) A
     * daemon Thread only runs as long as there are non-daemon Threads running.
     * When the last non-daemon Thread ends, the whole program ends no matter if
     * it had daemon Threads still running or not.
     *
     * @return a <code>boolean</code>
     * @see Thread#setDaemon
     */
    public final boolean isDaemon() {
        return daemon;
    }

    /**
     * Returns a <code>boolean</code> indicating whether the receiver has a
     * pending interrupt request (<code>true</code>) or not (
     * <code>false</code>)
     *
     * @return a <code>boolean</code>
     * @see Thread#interrupt
     * @see Thread#interrupted
     */
    public boolean isInterrupted() {
        VMThread vmt = this.vmThread;
        if (vmt != null) {
            return vmt.isInterrupted();
        }

        return false;
    }

    /**
     * Blocks the current Thread (<code>Thread.currentThread()</code>) until
     * the receiver finishes its execution and dies.
     *
     * @throws InterruptedException if <code>interrupt()</code> was called for
     *         the receiver while it was in the <code>join()</code> call
     * @see Object#notifyAll
     * @see java.lang.ThreadDeath
     */
    public final void join() throws InterruptedException {
        join(0, 0);
    }

    /**
     * Blocks the current Thread (<code>Thread.currentThread()</code>) until
     * the receiver finishes its execution and dies or the specified timeout
     * expires, whatever happens first.
     *
     * @param millis The maximum time to wait (in milliseconds).
     * @throws InterruptedException if <code>interrupt()</code> was called for
     *         the receiver while it was in the <code>join()</code> call
     * @see Object#notifyAll
     * @see java.lang.ThreadDeath
     */
    public final void join(long millis) throws InterruptedException {
        join(millis, 0);
    }

    /**
     * Blocks the current Thread (<code>Thread.currentThread()</code>) until
     * the receiver finishes its execution and dies or the specified timeout
     * expires, whatever happens first.
     *
     * @param millis The maximum time to wait (in milliseconds).
     * @param nanos Extra nanosecond precision
     * @throws InterruptedException if <code>interrupt()</code> was called for
     *         the receiver while it was in the <code>join()</code> call
     * @see Object#notifyAll
     * @see java.lang.ThreadDeath
     */
    public final void join(long millis, int nanos) throws InterruptedException {
        if (millis < 0 || nanos < 0 || nanos > 999999) {
            throw new IllegalArgumentException();
        }

        VMThread t;

        t = this.vmThread;

        if (t != null) {
            synchronized (t) {
                if (isAlive())
                    t.wait(millis, nanos);
            }
        }
    }

    /**
     * This is a no-op if the receiver was never suspended, or suspended and
     * already resumed. If the receiver is suspended, however, makes it resume
     * to the point where it was when it was suspended.
     *
     * @throws SecurityException if <code>checkAccess()</code> fails with a
     *         SecurityException
     * @see Thread#suspend()
     * @deprecated Used with deprecated method {@link Thread#suspend}
     */
    @Deprecated
    public final void resume() {
        checkAccess();

        VMThread vmt = this.vmThread;
        if (vmt != null) {
            vmt.resume();
        }
    }

    /**
     * Calls the <code>run()</code> method of the Runnable object the receiver
     * holds. If no Runnable is set, does nothing.
     *
     * @see Thread#start
     */
    public void run() {
        if (target != null) {
            target.run();
        }
    }

    /**
     * Set the context ClassLoader for the receiver.
     * <p>
     * The <code>RuntimePermission("setContextClassLoader")</code>
     * is checked prior to setting the handler.
     * </p>
     *
     * @param cl The context ClassLoader
     * @throws SecurityException if the current SecurityManager fails the
     *         checkPermission call.
     * @see java.lang.ClassLoader
     * @see #getContextClassLoader()
     */
    public void setContextClassLoader(ClassLoader cl) {
        SecurityManager securityManager = System.getSecurityManager();
        if (securityManager != null) {
            securityManager.checkPermission(new RuntimePermission("setContextClassLoader"));
        }

        contextClassLoader = cl;
    }

    /**
     * Set if the receiver is a daemon Thread or not. This can only be done
     * before the Thread starts running.
     *
     * @param isDaemon A boolean indicating if the Thread should be daemon or
     *        not
     * @throws SecurityException if <code>checkAccess()</code> fails with a
     *         SecurityException
     * @see Thread#isDaemon
     */
    public final void setDaemon(boolean isDaemon) {
        checkAccess();

        if (hasBeenStarted) {
            throw new IllegalThreadStateException("Thread already started."); // TODO Externalize?
        }

        if (vmThread == null) {
            daemon = isDaemon;
        }
    }

    /**
     * <p>
     * Sets the default uncaught exception handler.
     * </p>
     * <p>
     * The <code>RuntimePermission("setDefaultUncaughtExceptionHandler")</code>
     * is checked prior to setting the handler.
     * </p>
     *
     * @param handler The handler to set or <code>null</code>.
     * @throws SecurityException if the current SecurityManager fails the
     *         checkPermission call.
     * @since 1.5
     */
    public static void setDefaultUncaughtExceptionHandler(UncaughtExceptionHandler handler) {
        SecurityManager securityManager = System.getSecurityManager();
        if (securityManager != null) {
            securityManager.checkPermission(new RuntimePermission ("setDefaultUncaughtExceptionHandler"));
        }

        Thread.defaultUncaughtHandler = handler;
    }

    /**
     * Set the action to be executed when interruption, which is probably be
     * used to implement the interruptible channel. The action is null by
     * default. And if this method is invoked by passing in a non-null value,
     * this action's run() method will be invoked in <code>interrupt()</code>.
     * <p>
     * This is required internally by NIO, so even if it looks like it's
     * useless, don't delete it!
     *
     * @param action the action to be executed when interruption
     */
    @SuppressWarnings("unused")
    private void setInterruptAction(Runnable action) {
        this.interruptAction = action;
    }

    /**
     * Sets the name of the receiver.
     *
     * @param threadName new name for the Thread
     * @throws SecurityException if <code>checkAccess()</code> fails with a
     *         SecurityException
     * @see Thread#getName
     */
    public final void setName(String threadName) {
        if (threadName == null) {
            throw new NullPointerException();
        }

        checkAccess();

        name = threadName;
        VMThread vmt = this.vmThread;
        if (vmt != null) {
            /* notify the VM that the thread name has changed */
            vmt.nameChanged(threadName);
        }
    }

    /**
     * Sets the priority of the receiver. Note that the final priority set may
     * not be the parameter that was passed - it will depend on the receiver's
     * ThreadGroup. The priority cannot be set to be higher than the receiver's
     * ThreadGroup's maxPriority().
     *
     * @param priority new priority for the Thread
     * @throws SecurityException if <code>checkAccess()</code> fails with a
     *         SecurityException
     * @throws IllegalArgumentException if the new priority is greater than
     *         Thread.MAX_PRIORITY or less than Thread.MIN_PRIORITY
     * @see Thread#getPriority
     */
    public final void setPriority(int priority) {
        checkAccess();

        if (priority < Thread.MIN_PRIORITY || priority > Thread.MAX_PRIORITY) {
            throw new IllegalArgumentException("Prioritiy out of range"); // TODO Externalize?
        }

        if (priority > group.getMaxPriority()) {
            priority = group.getMaxPriority();
        }

        this.priority = priority;

        VMThread vmt = this.vmThread;
        if (vmt != null) {
            vmt.setPriority(priority);
        }
    }

    /**
     * <p>
     * Sets the default uncaught exception handler.
     * </p>
     *
     * @param handler The handler to set or <code>null</code>.
     * @throws SecurityException if the current SecurityManager fails the
     *         checkAccess call.
     * @since 1.5
     */
    public void setUncaughtExceptionHandler(UncaughtExceptionHandler handler) {
        checkAccess();

        uncaughtHandler = handler;
    }

    /**
     * Causes the thread which sent this message to sleep an interval of time
     * (given in milliseconds). The precision is not guaranteed - the Thread may
     * sleep more or less than requested.
     *
     * @param time The time to sleep in milliseconds.
     * @throws InterruptedException if <code>interrupt()</code> was called for
     *         this Thread while it was sleeping
     * @see Thread#interrupt()
     */
    public static void sleep(long time) throws InterruptedException {
        Thread.sleep(time, 0);
    }

    /**
     * Causes the thread which sent this message to sleep an interval of time
     * (given in milliseconds). The precision is not guaranteed - the Thread may
     * sleep more or less than requested.
     *
     * @param time The time to sleep in milliseconds.
     * @param nanos Extra nanosecond precision
     * @throws InterruptedException if <code>interrupt()</code> was called for
     *         this Thread while it was sleeping
     * @see Thread#interrupt()
     */
    public static void sleep(long time, int nanos) throws InterruptedException {
        VMThread.sleep(time, nanos);
    }

    /**
     * Starts the new Thread of execution. The <code>run()</code> method of
     * the receiver will be called by the receiver Thread itself (and not the
     * Thread calling <code>start()</code>).
     *
     * @throws IllegalThreadStateException Unspecified in the Java language
     *         specification
     * @see Thread#run
     */
    public synchronized void start() {
        if (hasBeenStarted) {
            throw new IllegalThreadStateException("Thread already started."); // TODO Externalize?
        }

        hasBeenStarted = true;

        VMThread.create(this, stackSize);
    }

    /**
     * Requests the receiver Thread to stop and throw ThreadDeath. The Thread is
     * resumed if it was suspended and awakened if it was sleeping, so that it
     * can proceed to throw ThreadDeath.
     *
     * @throws SecurityException if <code>checkAccess()</code> fails with a
     *         SecurityException
     * @deprecated
     */
    @Deprecated
    public final void stop() {
        stop(new ThreadDeath());
    }

    /**
     * Requests the receiver Thread to stop and throw the
     * <code>throwable()</code>. The Thread is resumed if it was suspended
     * and awakened if it was sleeping, so that it can proceed to throw the
     * <code>throwable()</code>.
     *
     * @param throwable Throwable object to be thrown by the Thread
     * @throws SecurityException if <code>checkAccess()</code> fails with a
     *         SecurityException
     * @throws NullPointerException if <code>throwable()</code> is
     *         <code>null</code>
     * @deprecated
     */
    @Deprecated
    public final synchronized void stop(Throwable throwable) {
        SecurityManager securityManager = System.getSecurityManager();
        if (securityManager != null) {
            securityManager.checkAccess(this);
            if (Thread.currentThread() != this) {
                securityManager.checkPermission(new RuntimePermission("stopThread"));
            }
        }

        if (throwable == null) {
            throw new NullPointerException();
        }

        VMThread vmt = this.vmThread;
        if (vmt != null) {
            vmt.stop(throwable);
        }
    }

    /**
     * This is a no-op if the receiver is suspended. If the receiver
     * <code>isAlive()</code> however, suspended it until
     * <code>resume()</code> is sent to it. Suspend requests are not queued,
     * which means that N requests are equivalent to just one - only one resume
     * request is needed in this case.
     *
     * @throws SecurityException if <code>checkAccess()</code> fails with a
     *         SecurityException
     * @see Thread#resume()
     * @deprecated May cause deadlocks.
     */
    @Deprecated
    public final void suspend() {
        checkAccess();

        VMThread vmt = this.vmThread;
        if (vmt != null) {
            vmt.suspend();
        }
    }

    /**
     * Returns a string containing a concise, human-readable description of the
     * receiver.
     *
     * @return a printable representation for the receiver.
     */
    @Override
    public String toString() {
        return "Thread[" + name + "," + priority + "," + group.getName() + "]";
    }

    /**
     * Causes the thread which sent this message to yield execution to another
     * Thread that is ready to run. The actual scheduling is
     * implementation-dependent.
     *
     */
    public static void yield() {
        VMThread.yield();
    }

    /**
     * Returns whether the current thread has a monitor lock on the specified
     * object.
     *
     * @param object the object to test for the monitor lock
     * @return true when the current thread has a monitor lock on the specified
     *         object
     */
    public static boolean holdsLock(Object object) {
        return currentThread().vmThread.holdsLock(object);
    }

    /**
     * Implemented by objects that want to handle cases where a thread is being
     * terminated by an uncaught exception. Upon such termination, the handler
     * is notified of the terminating thread and causal exception. If there is
     * no explicit handler set then the thread's group is the default handler.
     */
    public static interface UncaughtExceptionHandler {
        /**
         * The thread is being terminated by an uncaught exception. Further
         * exceptions thrown in this method are prevent the remainder of the
         * method from executing, but are otherwise ignored.
         *
         * @param thread the thread that has an uncaught exception
         * @param ex the exception that was thrown
         */
        void uncaughtException(Thread thread, Throwable ex);
    }

    // BEGIN android-added

    /**
     * Implementation of <code>unpark()</code>. See {@link LangAccessImpl}.
     */
    /*package*/ void unpark() {
        VMThread vmt = vmThread;

        if (vmt == null) {
            /*
             * vmThread is null before the thread is start()ed. In
             * this case, we just go ahead and set the state to
             * PREEMPTIVELY_UNPARKED. Since this happens before the
             * thread is started, we don't have to worry about
             * synchronizing with it.
             */
            parkState = ParkState.PREEMPTIVELY_UNPARKED;
            return;
        }
        
        synchronized (vmt) {
            switch (parkState) {
                case ParkState.PREEMPTIVELY_UNPARKED: {
                    /*
                     * Nothing to do in this case: By definition, a
                     * preemptively unparked thread is to remain in
                     * the preemptively unparked state if it is told
                     * to unpark.
                     */
                    break;
                }
                case ParkState.UNPARKED: {
                    parkState = ParkState.PREEMPTIVELY_UNPARKED;
                    break;
                }
                default /*parked*/: {
                    parkState = ParkState.UNPARKED;
                    vmt.notifyAll();
                    break;
                }
            }
        }
    }
    
    /**
     * Implementation of <code>parkFor()</code>. See {@link LangAccessImpl}.
     * This method must only be called when <code>this</code> is the current
     * thread.
     * 
     * @param nanos number of nanoseconds to park for
     */
    /*package*/ void parkFor(long nanos) {
        VMThread vmt = vmThread;

        if (vmt == null) {
            // Running threads should always have an associated vmThread.
            throw new AssertionError();
        }
        
        synchronized (vmt) {
            switch (parkState) {
                case ParkState.PREEMPTIVELY_UNPARKED: {
                    parkState = ParkState.UNPARKED;
                    break;
                }
                case ParkState.UNPARKED: {
                    long millis = nanos / 1000000;
                    nanos %= 1000000;

                    parkState = ParkState.PARKED;
                    try {
                        vmt.wait(millis, (int) nanos);
                    } catch (InterruptedException ex) {
                        interrupt();
                    } finally {
                        /*
                         * Note: If parkState manages to become
                         * PREEMPTIVELY_UNPARKED before hitting this
                         * code, it should left in that state.
                         */
                        if (parkState == ParkState.PARKED) {
                            parkState = ParkState.UNPARKED;
                        }                            
                    }
                    break;
                }
                default /*parked*/: {
                    throw new AssertionError(
                            "shouldn't happen: attempt to repark");
                }
            }       
        }
    }

    /**
     * Implementation of <code>parkUntil()</code>. See {@link LangAccessImpl}.
     * This method must only be called when <code>this</code> is the current
     * thread.
     * 
     * @param time absolute milliseconds since the epoch to park until
     */
    /*package*/ void parkUntil(long time) {
        VMThread vmt = vmThread;

        if (vmt == null) {
            // Running threads should always have an associated vmThread.
            throw new AssertionError();
        }
        
        synchronized (vmt) {
            /*
             * Note: This conflates the two time bases of "wall clock"
             * time and "monotonic uptime" time. However, given that
             * the underlying system can only wait on monotonic time,
             * it is unclear if there is any way to avoid the
             * conflation. The downside here is that if, having
             * calculated the delay, the wall clock gets moved ahead,
             * this method may not return until well after the wall
             * clock has reached the originally designated time. The
             * reverse problem (the wall clock being turned back)
             * isn't a big deal, since this method is allowed to
             * spuriously return for any reason, and this situation
             * can safely be construed as just such a spurious return.
             */
            long delayMillis = time - System.currentTimeMillis();

            if (delayMillis <= 0) {
                parkState = ParkState.UNPARKED;
            } else {
                parkFor(delayMillis * 1000000);
            }
        }
    }

    // END android-added
}
