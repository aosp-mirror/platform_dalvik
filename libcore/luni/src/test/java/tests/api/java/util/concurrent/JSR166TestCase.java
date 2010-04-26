/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/licenses/publicdomain
 * Other contributors include Andrew Wright, Jeffrey Hayes,
 * Pat Fisher, Mike Judd.
 */

package tests.api.java.util.concurrent; // android-added

import junit.framework.*;
import java.util.*;
import java.util.concurrent.*;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import java.io.*;
import java.security.*;

/**
 * Base class for JSR166 Junit TCK tests.  Defines some constants,
 * utility methods and classes, as well as a simple framework for
 * helping to make sure that assertions failing in generated threads
 * cause the associated test that generated them to itself fail (which
 * JUnit does not otherwise arrange).  The rules for creating such
 * tests are:
 *
 * <ol>
 *
 * <li> All assertions in code running in generated threads must use
 * the forms {@link #threadFail}, {@link #threadAssertTrue}, {@link
 * #threadAssertEquals}, or {@link #threadAssertNull}, (not
 * <tt>fail</tt>, <tt>assertTrue</tt>, etc.) It is OK (but not
 * particularly recommended) for other code to use these forms too.
 * Only the most typically used JUnit assertion methods are defined
 * this way, but enough to live with.</li>
 *
 * <li> If you override {@link #setUp} or {@link #tearDown}, make sure
 * to invoke <tt>super.setUp</tt> and <tt>super.tearDown</tt> within
 * them. These methods are used to clear and check for thread
 * assertion failures.</li>
 *
 * <li>All delays and timeouts must use one of the constants <tt>
 * SHORT_DELAY_MS</tt>, <tt> SMALL_DELAY_MS</tt>, <tt> MEDIUM_DELAY_MS</tt>,
 * <tt> LONG_DELAY_MS</tt>. The idea here is that a SHORT is always
 * discriminable from zero time, and always allows enough time for the
 * small amounts of computation (creating a thread, calling a few
 * methods, etc) needed to reach a timeout point. Similarly, a SMALL
 * is always discriminable as larger than SHORT and smaller than
 * MEDIUM.  And so on. These constants are set to conservative values,
 * but even so, if there is ever any doubt, they can all be increased
 * in one spot to rerun tests on slower platforms.</li>
 *
 * <li> All threads generated must be joined inside each test case
 * method (or <tt>fail</tt> to do so) before returning from the
 * method. The <tt> joinPool</tt> method can be used to do this when
 * using Executors.</li>
 *
 * </ol>
 *
 * <p> <b>Other notes</b>
 * <ul>
 *
 * <li> Usually, there is one testcase method per JSR166 method
 * covering "normal" operation, and then as many exception-testing
 * methods as there are exceptions the method can throw. Sometimes
 * there are multiple tests per JSR166 method when the different
 * "normal" behaviors differ significantly. And sometimes testcases
 * cover multiple methods when they cannot be tested in
 * isolation.</li>
 *
 * <li> The documentation style for testcases is to provide as javadoc
 * a simple sentence or two describing the property that the testcase
 * method purports to test. The javadocs do not say anything about how
 * the property is tested. To find out, read the code.</li>
 *
 * <li> These tests are "conformance tests", and do not attempt to
 * test throughput, latency, scalability or other performance factors
 * (see the separate "jtreg" tests for a set intended to check these
 * for the most central aspects of functionality.) So, most tests use
 * the smallest sensible numbers of threads, collection sizes, etc
 * needed to check basic conformance.</li>
 *
 * <li>The test classes currently do not declare inclusion in
 * any particular package to simplify things for people integrating
 * them in TCK test suites.</li>
 *
 * <li> As a convenience, the <tt>main</tt> of this class (JSR166TestCase)
 * runs all JSR166 unit tests.</li>
 *
 * </ul>
 */
public class JSR166TestCase extends TestCase {
    private static final boolean useSecurityManager =
        Boolean.getBoolean("jsr166.useSecurityManager");

    // BEGIN android-removed
    // /**
    //  * Runs all JSR166 unit tests using junit.textui.TestRunner
    //  */
    // public static void main(String[] args) {
    //     if (useSecurityManager) {
    //         System.err.println("Setting a permissive security manager");
    //         Policy.setPolicy(permissivePolicy());
    //         System.setSecurityManager(new SecurityManager());
    //     }
    //     int iters = 1;
    //     if (args.length > 0)
    //         iters = Integer.parseInt(args[0]);
    //     Test s = suite();
    //     for (int i = 0; i < iters; ++i) {
    //         junit.textui.TestRunner.run(s);
    //         System.gc();
    //         System.runFinalization();
    //     }
    //     System.exit(0);
    // }
    // END android-removed

    /**
     * Collects all JSR166 unit tests as one suite
     */
    public static Test suite() {
        TestSuite suite = new TestSuite("JSR166 Unit Tests");

        suite.addTest(new TestSuite(AbstractExecutorServiceTest.class));
        suite.addTest(new TestSuite(AbstractQueueTest.class));
        suite.addTest(new TestSuite(AbstractQueuedSynchronizerTest.class));
        suite.addTest(new TestSuite(AbstractQueuedLongSynchronizerTest.class));
        suite.addTest(new TestSuite(ArrayBlockingQueueTest.class));
        suite.addTest(new TestSuite(ArrayDequeTest.class));
        suite.addTest(new TestSuite(AtomicBooleanTest.class));
        suite.addTest(new TestSuite(AtomicIntegerArrayTest.class));
        suite.addTest(new TestSuite(AtomicIntegerFieldUpdaterTest.class));
        suite.addTest(new TestSuite(AtomicIntegerTest.class));
        suite.addTest(new TestSuite(AtomicLongArrayTest.class));
        suite.addTest(new TestSuite(AtomicLongFieldUpdaterTest.class));
        suite.addTest(new TestSuite(AtomicLongTest.class));
        suite.addTest(new TestSuite(AtomicMarkableReferenceTest.class));
        suite.addTest(new TestSuite(AtomicReferenceArrayTest.class));
        suite.addTest(new TestSuite(AtomicReferenceFieldUpdaterTest.class));
        suite.addTest(new TestSuite(AtomicReferenceTest.class));
        suite.addTest(new TestSuite(AtomicStampedReferenceTest.class));
        suite.addTest(new TestSuite(ConcurrentHashMapTest.class));
        suite.addTest(new TestSuite(ConcurrentLinkedQueueTest.class));
        suite.addTest(new TestSuite(ConcurrentSkipListMapTest.class));
        suite.addTest(new TestSuite(ConcurrentSkipListSubMapTest.class));
        suite.addTest(new TestSuite(ConcurrentSkipListSetTest.class));
        suite.addTest(new TestSuite(ConcurrentSkipListSubSetTest.class));
        suite.addTest(new TestSuite(CopyOnWriteArrayListTest.class));
        suite.addTest(new TestSuite(CopyOnWriteArraySetTest.class));
        suite.addTest(new TestSuite(CountDownLatchTest.class));
        suite.addTest(new TestSuite(CyclicBarrierTest.class));
        suite.addTest(new TestSuite(DelayQueueTest.class));
        suite.addTest(new TestSuite(EntryTest.class));
        suite.addTest(new TestSuite(ExchangerTest.class));
        suite.addTest(new TestSuite(ExecutorsTest.class));
        suite.addTest(new TestSuite(ExecutorCompletionServiceTest.class));
        suite.addTest(new TestSuite(FutureTaskTest.class));
        suite.addTest(new TestSuite(LinkedBlockingDequeTest.class));
        suite.addTest(new TestSuite(LinkedBlockingQueueTest.class));
        suite.addTest(new TestSuite(LinkedListTest.class));
        suite.addTest(new TestSuite(LockSupportTest.class));
        suite.addTest(new TestSuite(PriorityBlockingQueueTest.class));
        suite.addTest(new TestSuite(PriorityQueueTest.class));
        suite.addTest(new TestSuite(ReentrantLockTest.class));
        suite.addTest(new TestSuite(ReentrantReadWriteLockTest.class));
        suite.addTest(new TestSuite(ScheduledExecutorTest.class));
        suite.addTest(new TestSuite(ScheduledExecutorSubclassTest.class));
        suite.addTest(new TestSuite(SemaphoreTest.class));
        suite.addTest(new TestSuite(SynchronousQueueTest.class));
        suite.addTest(new TestSuite(SystemTest.class));
        suite.addTest(new TestSuite(ThreadLocalTest.class));
        suite.addTest(new TestSuite(ThreadPoolExecutorTest.class));
        suite.addTest(new TestSuite(ThreadPoolExecutorSubclassTest.class));
        suite.addTest(new TestSuite(ThreadTest.class));
        suite.addTest(new TestSuite(TimeUnitTest.class));

        return suite;
    }


    public static long SHORT_DELAY_MS;
    public static long SMALL_DELAY_MS;
    public static long MEDIUM_DELAY_MS;
    public static long LONG_DELAY_MS;


    /**
     * Returns the shortest timed delay. This could
     * be reimplemented to use for example a Property.
     */
    protected long getShortDelay() {
        // BEGIN android-changed
        // original value is 50
        return 250;
        // END android-changed
    }


    /**
     * Sets delays as multiples of SHORT_DELAY.
     */
    protected void setDelays() {
        SHORT_DELAY_MS = getShortDelay();
        SMALL_DELAY_MS = SHORT_DELAY_MS * 5;
        MEDIUM_DELAY_MS = SHORT_DELAY_MS * 10;
        LONG_DELAY_MS = SHORT_DELAY_MS * 50;
    }

    /**
     * Flag set true if any threadAssert methods fail
     */
    volatile boolean threadFailed;

    /**
     * Initializes test to indicate that no thread assertions have failed
     */
    public void setUp() {
        setDelays();
        threadFailed = false;
    }

    /**
     * Triggers test case failure if any thread assertions have failed
     */
    public void tearDown() {
        assertFalse(threadFailed);
    }

    /**
     * Fail, also setting status to indicate current testcase should fail
     */
    public void threadFail(String reason) {
        threadFailed = true;
        fail(reason);
    }

    /**
     * If expression not true, set status to indicate current testcase
     * should fail
     */
    public void threadAssertTrue(boolean b) {
        if (!b) {
            threadFailed = true;
            assertTrue(b);
        }
    }

    /**
     * If expression not false, set status to indicate current testcase
     * should fail
     */
    public void threadAssertFalse(boolean b) {
        if (b) {
            threadFailed = true;
            assertFalse(b);
        }
    }

    /**
     * If argument not null, set status to indicate current testcase
     * should fail
     */
    public void threadAssertNull(Object x) {
        if (x != null) {
            threadFailed = true;
            assertNull(x);
        }
    }

    /**
     * If arguments not equal, set status to indicate current testcase
     * should fail
     */
    public void threadAssertEquals(long x, long y) {
        if (x != y) {
            threadFailed = true;
            assertEquals(x, y);
        }
    }

    /**
     * If arguments not equal, set status to indicate current testcase
     * should fail
     */
    public void threadAssertEquals(Object x, Object y) {
        if (x != y && (x == null || !x.equals(y))) {
            threadFailed = true;
            assertEquals(x, y);
        }
    }

    /**
     * threadFail with message "should throw exception"
     */
    public void threadShouldThrow() {
        threadFailed = true;
        fail("should throw exception");
    }

    /**
     * threadFail with message "should throw" + exceptionName
     */
    public void threadShouldThrow(String exceptionName) {
        threadFailed = true;
        fail("should throw " + exceptionName);
    }

    /**
     * threadFail with message "Unexpected exception"
     */
    public void threadUnexpectedException() {
        threadFailed = true;
        fail("Unexpected exception");
    }

    /**
     * threadFail with message "Unexpected exception", with argument
     */
    public void threadUnexpectedException(Throwable ex) {
        threadFailed = true;
        ex.printStackTrace();
        fail("Unexpected exception: " + ex);
    }

    /**
     * Wait out termination of a thread pool or fail doing so
     */
    public void joinPool(ExecutorService exec) {
        try {
            exec.shutdown();
            assertTrue(exec.awaitTermination(LONG_DELAY_MS, MILLISECONDS));
        } catch (SecurityException ok) {
            // Allowed in case test doesn't have privs
        } catch (InterruptedException ie) {
            fail("Unexpected InterruptedException");
        }
    }


    /**
     * fail with message "should throw exception"
     */
    public void shouldThrow() {
        fail("Should throw exception");
    }

    /**
     * fail with message "should throw " + exceptionName
     */
    public void shouldThrow(String exceptionName) {
        fail("Should throw " + exceptionName);
    }

    /**
     * fail with message "Unexpected exception"
     */
    public void unexpectedException() {
        fail("Unexpected exception");
    }

    /**
     * fail with message "Unexpected exception", with argument
     */
    public void unexpectedException(Throwable ex) {
        ex.printStackTrace();
        fail("Unexpected exception: " + ex);
    }


    /**
     * The number of elements to place in collections, arrays, etc.
     */
    public static final int SIZE = 20;

    // Some convenient Integer constants

    public static final Integer zero  = new Integer(0);
    public static final Integer one   = new Integer(1);
    public static final Integer two   = new Integer(2);
    public static final Integer three = new Integer(3);
    public static final Integer four  = new Integer(4);
    public static final Integer five  = new Integer(5);
    public static final Integer six   = new Integer(6);
    public static final Integer seven = new Integer(7);
    public static final Integer eight = new Integer(8);
    public static final Integer nine  = new Integer(9);
    public static final Integer m1  = new Integer(-1);
    public static final Integer m2  = new Integer(-2);
    public static final Integer m3  = new Integer(-3);
    public static final Integer m4  = new Integer(-4);
    public static final Integer m5  = new Integer(-5);
    public static final Integer m6  = new Integer(-6);
    public static final Integer m10 = new Integer(-10);


    /**
     * Runs Runnable r with a security policy that permits precisely
     * the specified permissions.  If there is no current security
     * manager, the runnable is run twice, both with and without a
     * security manager.  We require that any security manager permit
     * getPolicy/setPolicy.
     */
    public void runWithPermissions(Runnable r, Permission... permissions) {
        SecurityManager sm = System.getSecurityManager();
        if (sm == null) {
            r.run();
            Policy savedPolicy = Policy.getPolicy();
            try {
                Policy.setPolicy(permissivePolicy());
                System.setSecurityManager(new SecurityManager());
                runWithPermissions(r, permissions);
            } finally {
                System.setSecurityManager(null);
                Policy.setPolicy(savedPolicy);
            }
        } else {
            Policy savedPolicy = Policy.getPolicy();
            AdjustablePolicy policy = new AdjustablePolicy(permissions);
            Policy.setPolicy(policy);

            try {
                r.run();
            } finally {
                policy.addPermission(new SecurityPermission("setPolicy"));
                Policy.setPolicy(savedPolicy);
            }
        }
    }

    /**
     * Runs a runnable without any permissions.
     */
    public void runWithoutPermissions(Runnable r) {
        runWithPermissions(r);
    }

    /**
     * A security policy where new permissions can be dynamically added
     * or all cleared.
     */
    public static class AdjustablePolicy extends java.security.Policy {
        Permissions perms = new Permissions();
        AdjustablePolicy(Permission... permissions) {
            for (Permission permission : permissions)
                perms.add(permission);
        }
        void addPermission(Permission perm) { perms.add(perm); }
        void clearPermissions() { perms = new Permissions(); }
        public PermissionCollection getPermissions(CodeSource cs) {
            return perms;
        }
        public PermissionCollection getPermissions(ProtectionDomain pd) {
            return perms;
        }
        public boolean implies(ProtectionDomain pd, Permission p) {
            return perms.implies(p);
        }
        public void refresh() {}
    }

    /**
     * Returns a policy containing all the permissions we ever need.
     */
    public static Policy permissivePolicy() {
        return new AdjustablePolicy
            // Permissions j.u.c. needs directly
            (new RuntimePermission("modifyThread"),
             new RuntimePermission("getClassLoader"),
             new RuntimePermission("setContextClassLoader"),
             // Permissions needed to change permissions!
             new SecurityPermission("getPolicy"),
             new SecurityPermission("setPolicy"),
             new RuntimePermission("setSecurityManager"),
             // Permissions needed by the junit test harness
             new RuntimePermission("accessDeclaredMembers"),
             new PropertyPermission("*", "read"),
             new java.io.FilePermission("<<ALL FILES>>", "read"));
    }

    /**
     * Sleep until the timeout has elapsed, or interrupted.
     * Does <em>NOT</em> throw InterruptedException.
     */
    void sleepTillInterrupted(long timeoutMillis) {
        try {
            Thread.sleep(timeoutMillis);
        } catch (InterruptedException wakeup) {}
    }

    /**
     * Returns a new started Thread running the given runnable.
     */
    Thread newStartedThread(Runnable runnable) {
        Thread t = new Thread(runnable);
        t.start();
        return t;
    }

    // Some convenient Runnable classes

    public abstract class CheckedRunnable implements Runnable {
        protected abstract void realRun() throws Throwable;

        public final void run() {
            try {
                realRun();
            } catch (Throwable t) {
                threadUnexpectedException(t);
            }
        }
    }

    public abstract class RunnableShouldThrow implements Runnable {
        protected abstract void realRun() throws Throwable;

        final Class<?> exceptionClass;

        <T extends Throwable> RunnableShouldThrow(Class<T> exceptionClass) {
            this.exceptionClass = exceptionClass;
        }

        public final void run() {
            try {
                realRun();
                threadShouldThrow(exceptionClass.getSimpleName());
            } catch (Throwable t) {
                if (! exceptionClass.isInstance(t))
                    threadUnexpectedException(t);
            }
        }
    }

    public abstract class ThreadShouldThrow extends Thread {
        protected abstract void realRun() throws Throwable;

        final Class<?> exceptionClass;

        <T extends Throwable> ThreadShouldThrow(Class<T> exceptionClass) {
            this.exceptionClass = exceptionClass;
        }

        public final void run() {
            try {
                realRun();
                threadShouldThrow(exceptionClass.getSimpleName());
            } catch (Throwable t) {
                if (! exceptionClass.isInstance(t))
                    threadUnexpectedException(t);
            }
        }
    }

    public abstract class CheckedInterruptedRunnable implements Runnable {
        protected abstract void realRun() throws Throwable;

        public final void run() {
            try {
                realRun();
                threadShouldThrow("InterruptedException");
            } catch (InterruptedException success) {
            } catch (Throwable t) {
                threadUnexpectedException(t);
            }
        }
    }

    public abstract class CheckedCallable<T> implements Callable<T> {
        protected abstract T realCall() throws Throwable;

        public final T call() {
            try {
                return realCall();
            } catch (Throwable t) {
                threadUnexpectedException(t);
            }
            return null;
        }
    }

    public abstract class CheckedInterruptedCallable<T> implements Callable<T> {
        protected abstract T realCall() throws Throwable;

        public final T call() {
            try {
                T result = realCall();
                threadShouldThrow("InterruptedException");
                return result;
            } catch (InterruptedException success) {
            } catch (Throwable t) {
                threadUnexpectedException(t);
            }
            return null;
        }
    }

    public static class NoOpRunnable implements Runnable {
        public void run() {}
    }

    public static class NoOpCallable implements Callable {
        public Object call() { return Boolean.TRUE; }
    }

    public static final String TEST_STRING = "a test string";

    public static class StringTask implements Callable<String> {
        public String call() { return TEST_STRING; }
    }

    public Callable<String> latchAwaitingStringTask(final CountDownLatch latch) {
        return new CheckedCallable<String>() {
            public String realCall() {
                try {
                    latch.await();
                } catch (InterruptedException quittingTime) {}
                return TEST_STRING;
            }};
    }

    public static class NPETask implements Callable<String> {
        public String call() { throw new NullPointerException(); }
    }

    public static class CallableOne implements Callable<Integer> {
        public Integer call() { return one; }
    }

    public class ShortRunnable extends CheckedRunnable {
        protected void realRun() throws Throwable {
            Thread.sleep(SHORT_DELAY_MS);
        }
    }

    public class ShortInterruptedRunnable extends CheckedInterruptedRunnable {
        protected void realRun() throws InterruptedException {
            Thread.sleep(SHORT_DELAY_MS);
        }
    }

    public class SmallRunnable extends CheckedRunnable {
        protected void realRun() throws Throwable {
            Thread.sleep(SMALL_DELAY_MS);
        }
    }

    public class SmallPossiblyInterruptedRunnable extends CheckedRunnable {
        protected void realRun() {
            try {
                Thread.sleep(SMALL_DELAY_MS);
            } catch (InterruptedException ok) {}
        }
    }

    public class SmallCallable extends CheckedCallable {
        protected Object realCall() throws InterruptedException {
            Thread.sleep(SMALL_DELAY_MS);
            return Boolean.TRUE;
        }
    }

    public class SmallInterruptedRunnable extends CheckedInterruptedRunnable {
        protected void realRun() throws InterruptedException {
            Thread.sleep(SMALL_DELAY_MS);
        }
    }

    public class MediumRunnable extends CheckedRunnable {
        protected void realRun() throws Throwable {
            Thread.sleep(MEDIUM_DELAY_MS);
        }
    }

    public class MediumInterruptedRunnable extends CheckedInterruptedRunnable {
        protected void realRun() throws InterruptedException {
            Thread.sleep(MEDIUM_DELAY_MS);
        }
    }

    public class MediumPossiblyInterruptedRunnable extends CheckedRunnable {
        protected void realRun() {
            try {
                Thread.sleep(MEDIUM_DELAY_MS);
            } catch (InterruptedException ok) {}
        }
    }

    public class LongPossiblyInterruptedRunnable extends CheckedRunnable {
        protected void realRun() {
            try {
                Thread.sleep(LONG_DELAY_MS);
            } catch (InterruptedException ok) {}
        }
    }

    /**
     * For use as ThreadFactory in constructors
     */
    public static class SimpleThreadFactory implements ThreadFactory {
        public Thread newThread(Runnable r) {
            return new Thread(r);
        }
    }

    public static class TrackedShortRunnable implements Runnable {
        public volatile boolean done = false;
        public void run() {
            try {
                Thread.sleep(SMALL_DELAY_MS);
                done = true;
            } catch (InterruptedException ok) {}
        }
    }

    public static class TrackedMediumRunnable implements Runnable {
        public volatile boolean done = false;
        public void run() {
            try {
                Thread.sleep(MEDIUM_DELAY_MS);
                done = true;
            } catch (InterruptedException ok) {}
        }
    }

    public static class TrackedLongRunnable implements Runnable {
        public volatile boolean done = false;
        public void run() {
            try {
                Thread.sleep(LONG_DELAY_MS);
                done = true;
            } catch (InterruptedException ok) {}
        }
    }

    public static class TrackedNoOpRunnable implements Runnable {
        public volatile boolean done = false;
        public void run() {
            done = true;
        }
    }

    public static class TrackedCallable implements Callable {
        public volatile boolean done = false;
        public Object call() {
            try {
                Thread.sleep(SMALL_DELAY_MS);
                done = true;
            } catch (InterruptedException ok) {}
            return Boolean.TRUE;
        }
    }


    /**
     * For use as RejectedExecutionHandler in constructors
     */
    public static class NoOpREHandler implements RejectedExecutionHandler {
        public void rejectedExecution(Runnable r,
                                      ThreadPoolExecutor executor) {}
    }

}
