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
import java.math.BigInteger;
import java.security.*;

public class AbstractExecutorServiceTest extends JSR166TestCase {
    public static Test suite() {
        return new TestSuite(AbstractExecutorServiceTest.class);
    }

    /**
     * A no-frills implementation of AbstractExecutorService, designed
     * to test the submit methods only.
     */
    static class DirectExecutorService extends AbstractExecutorService {
        public void execute(Runnable r) { r.run(); }
        public void shutdown() { shutdown = true; }
        public List<Runnable> shutdownNow() { shutdown = true; return Collections.EMPTY_LIST; }
        public boolean isShutdown() { return shutdown; }
        public boolean isTerminated() { return isShutdown(); }
        public boolean awaitTermination(long timeout, TimeUnit unit) { return isShutdown(); }
        private volatile boolean shutdown = false;
    }

    /**
     * execute(runnable) runs it to completion
     */
    public void testExecuteRunnable() throws Exception {
        ExecutorService e = new DirectExecutorService();
        TrackedShortRunnable task = new TrackedShortRunnable();
        assertFalse(task.done);
        Future<?> future = e.submit(task);
        future.get();
        assertTrue(task.done);
    }


    /**
     * Completed submit(callable) returns result
     */
    public void testSubmitCallable() throws Exception {
        ExecutorService e = new DirectExecutorService();
        Future<String> future = e.submit(new StringTask());
        String result = future.get();
        assertSame(TEST_STRING, result);
    }

    /**
     * Completed submit(runnable) returns successfully
     */
    public void testSubmitRunnable() throws Exception {
        ExecutorService e = new DirectExecutorService();
        Future<?> future = e.submit(new NoOpRunnable());
        future.get();
        assertTrue(future.isDone());
    }

    /**
     * Completed submit(runnable, result) returns result
     */
    public void testSubmitRunnable2() throws Exception {
        ExecutorService e = new DirectExecutorService();
        Future<String> future = e.submit(new NoOpRunnable(), TEST_STRING);
        String result = future.get();
        assertSame(TEST_STRING, result);
    }


    /**
     * A submitted privileged action runs to completion
     */
    public void testSubmitPrivilegedAction() throws Exception {
        Runnable r = new CheckedRunnable() {
            public void realRun() throws Exception {
                ExecutorService e = new DirectExecutorService();
                Future future = e.submit(Executors.callable(new PrivilegedAction() {
                    public Object run() {
                        return TEST_STRING;
                    }}));

                assertSame(TEST_STRING, future.get());
            }};

        runWithPermissions(r,
                           new RuntimePermission("getClassLoader"),
                           new RuntimePermission("setContextClassLoader"),
                           new RuntimePermission("modifyThread"));
    }

    /**
     * A submitted privileged exception action runs to completion
     */
    public void testSubmitPrivilegedExceptionAction() throws Exception {
        Runnable r = new CheckedRunnable() {
            public void realRun() throws Exception {
                ExecutorService e = new DirectExecutorService();
                Future future = e.submit(Executors.callable(new PrivilegedExceptionAction() {
                    public Object run() {
                        return TEST_STRING;
                    }}));

                assertSame(TEST_STRING, future.get());
            }};

        runWithPermissions(r);
    }

    /**
     * A submitted failed privileged exception action reports exception
     */
    public void testSubmitFailedPrivilegedExceptionAction() throws Exception {
        Runnable r = new CheckedRunnable() {
            public void realRun() throws Exception {
                ExecutorService e = new DirectExecutorService();
                Future future = e.submit(Executors.callable(new PrivilegedExceptionAction() {
                    public Object run() throws Exception {
                        throw new IndexOutOfBoundsException();
                    }}));

                try {
                    future.get();
                    shouldThrow();
                } catch (ExecutionException success) {
                    assertTrue(success.getCause() instanceof IndexOutOfBoundsException);
                }}};

        runWithPermissions(r);
    }

    /**
     * execute(null runnable) throws NPE
     */
    public void testExecuteNullRunnable() {
        try {
            ExecutorService e = new DirectExecutorService();
            e.submit((Runnable) null);
            shouldThrow();
        } catch (NullPointerException success) {}
    }


    /**
     * submit(null callable) throws NPE
     */
    public void testSubmitNullCallable() {
        try {
            ExecutorService e = new DirectExecutorService();
            e.submit((Callable) null);
            shouldThrow();
        } catch (NullPointerException success) {}
    }

    /**
     * submit(runnable) throws RejectedExecutionException if
     * executor is saturated.
     */
    public void testExecute1() {
        ThreadPoolExecutor p =
            new ThreadPoolExecutor(1, 1,
                                   60, TimeUnit.SECONDS,
                                   new ArrayBlockingQueue<Runnable>(1));
        try {
            for (int i = 0; i < 2; ++i)
                p.submit(new MediumRunnable());
            for (int i = 0; i < 2; ++i) {
                try {
                    p.submit(new MediumRunnable());
                    shouldThrow();
                } catch (RejectedExecutionException success) {}
            }
        } finally {
            joinPool(p);
        }
    }

    /**
     * submit(callable) throws RejectedExecutionException
     * if executor is saturated.
     */
    public void testExecute2() {
        ThreadPoolExecutor p =
            new ThreadPoolExecutor(1, 1,
                                   60, TimeUnit.SECONDS,
                                   new ArrayBlockingQueue<Runnable>(1));
        try {
            for (int i = 0; i < 2; ++i)
                p.submit(new MediumRunnable());
            for (int i = 0; i < 2; ++i) {
                try {
                    p.submit(new SmallCallable());
                    shouldThrow();
                } catch (RejectedExecutionException success) {}
            }
        } finally {
            joinPool(p);
        }
    }


    /**
     *  Blocking on submit(callable) throws InterruptedException if
     *  caller interrupted.
     */
    public void testInterruptedSubmit() throws InterruptedException {
        final ThreadPoolExecutor p = new ThreadPoolExecutor(1,1,60, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(10));
        Thread t = new Thread(new CheckedInterruptedRunnable() {
            public void realRun() throws Exception {
                p.submit(new CheckedCallable<Object>() {
                             public Object realCall()
                                 throws InterruptedException {
                                 Thread.sleep(SMALL_DELAY_MS);
                                 return null;
                             }}).get();
            }});

        t.start();
        Thread.sleep(SHORT_DELAY_MS);
        t.interrupt();
        joinPool(p);
    }

    /**
     *  get of submitted callable throws InterruptedException if callable
     *  interrupted
     */
    public void testSubmitIE() throws InterruptedException {
        final ThreadPoolExecutor p =
            new ThreadPoolExecutor(1, 1,
                                   60, TimeUnit.SECONDS,
                                   new ArrayBlockingQueue<Runnable>(10));

        Thread t = new Thread(new CheckedInterruptedRunnable() {
            public void realRun() throws Exception {
                p.submit(new SmallCallable()).get();
            }});

        t.start();
        Thread.sleep(SHORT_DELAY_MS);
        t.interrupt();
        t.join();
        joinPool(p);
    }

    /**
     *  get of submit(callable) throws ExecutionException if callable
     *  throws exception
     */
    public void testSubmitEE() throws InterruptedException {
        ThreadPoolExecutor p =
            new ThreadPoolExecutor(1, 1,
                                   60, TimeUnit.SECONDS,
                                   new ArrayBlockingQueue<Runnable>(10));

        Callable c = new Callable() {
            public Object call() { return 5/0; }};

        try {
            p.submit(c).get();
            shouldThrow();
        } catch (ExecutionException success) {
            assertTrue(success.getCause() instanceof ArithmeticException);
        }
        joinPool(p);
    }

    /**
     * invokeAny(null) throws NPE
     */
    public void testInvokeAny1()
        throws InterruptedException, ExecutionException {
        ExecutorService e = new DirectExecutorService();
        try {
            e.invokeAny(null);
            shouldThrow();
        } catch (NullPointerException success) {
        } finally {
            joinPool(e);
        }
    }

    /**
     * invokeAny(empty collection) throws IAE
     */
    public void testInvokeAny2()
        throws InterruptedException, ExecutionException {
        ExecutorService e = new DirectExecutorService();
        try {
            e.invokeAny(new ArrayList<Callable<String>>());
            shouldThrow();
        } catch (IllegalArgumentException success) {
        } finally {
            joinPool(e);
        }
    }

    /**
     * invokeAny(c) throws NPE if c has null elements
     */
    public void testInvokeAny3() throws Exception {
        ExecutorService e = new DirectExecutorService();
        List<Callable<Integer>> l = new ArrayList<Callable<Integer>>();
        l.add(new Callable<Integer>() {
                  public Integer call() { return 5/0; }});
        l.add(null);
        try {
            e.invokeAny(l);
            shouldThrow();
        } catch (NullPointerException success) {
        } finally {
            joinPool(e);
        }
    }

    /**
     * invokeAny(c) throws ExecutionException if no task in c completes
     */
    public void testInvokeAny4() throws InterruptedException {
        ExecutorService e = new DirectExecutorService();
        List<Callable<String>> l = new ArrayList<Callable<String>>();
        l.add(new NPETask());
        try {
            e.invokeAny(l);
            shouldThrow();
        } catch (ExecutionException success) {
            assertTrue(success.getCause() instanceof NullPointerException);
        } finally {
            joinPool(e);
        }
    }

    /**
     * invokeAny(c) returns result of some task in c if at least one completes
     */
    public void testInvokeAny5() throws Exception {
        ExecutorService e = new DirectExecutorService();
        try {
            List<Callable<String>> l = new ArrayList<Callable<String>>();
            l.add(new StringTask());
            l.add(new StringTask());
            String result = e.invokeAny(l);
            assertSame(TEST_STRING, result);
        } finally {
            joinPool(e);
        }
    }

    /**
     * invokeAll(null) throws NPE
     */
    public void testInvokeAll1() throws InterruptedException {
        ExecutorService e = new DirectExecutorService();
        try {
            e.invokeAll(null);
            shouldThrow();
        } catch (NullPointerException success) {
        } finally {
            joinPool(e);
        }
    }

    /**
     * invokeAll(empty collection) returns empty collection
     */
    public void testInvokeAll2() throws InterruptedException {
        ExecutorService e = new DirectExecutorService();
        try {
            List<Future<String>> r = e.invokeAll(new ArrayList<Callable<String>>());
            assertTrue(r.isEmpty());
        } finally {
            joinPool(e);
        }
    }

    /**
     * invokeAll(c) throws NPE if c has null elements
     */
    public void testInvokeAll3() throws InterruptedException {
        ExecutorService e = new DirectExecutorService();
        List<Callable<String>> l = new ArrayList<Callable<String>>();
        l.add(new StringTask());
        l.add(null);
        try {
            e.invokeAll(l);
            shouldThrow();
        } catch (NullPointerException success) {
        } finally {
            joinPool(e);
        }
    }

    /**
     * get of returned element of invokeAll(c) throws exception on failed task
     */
    public void testInvokeAll4() throws Exception {
        ExecutorService e = new DirectExecutorService();
        try {
            List<Callable<String>> l = new ArrayList<Callable<String>>();
            l.add(new NPETask());
            List<Future<String>> futures = e.invokeAll(l);
            assertEquals(1, futures.size());
            try {
                futures.get(0).get();
                shouldThrow();
            } catch (ExecutionException success) {
                assertTrue(success.getCause() instanceof NullPointerException);
            }
        } finally {
            joinPool(e);
        }
    }

    /**
     * invokeAll(c) returns results of all completed tasks in c
     */
    public void testInvokeAll5() throws Exception {
        ExecutorService e = new DirectExecutorService();
        try {
            List<Callable<String>> l = new ArrayList<Callable<String>>();
            l.add(new StringTask());
            l.add(new StringTask());
            List<Future<String>> futures = e.invokeAll(l);
            assertEquals(2, futures.size());
            for (Future<String> future : futures)
                assertSame(TEST_STRING, future.get());
        } finally {
            joinPool(e);
        }
    }


    /**
     * timed invokeAny(null) throws NPE
     */
    public void testTimedInvokeAny1() throws Exception {
        ExecutorService e = new DirectExecutorService();
        try {
            e.invokeAny(null, MEDIUM_DELAY_MS, MILLISECONDS);
            shouldThrow();
        } catch (NullPointerException success) {
        } finally {
            joinPool(e);
        }
    }

    /**
     * timed invokeAny(null time unit) throws NPE
     */
    public void testTimedInvokeAnyNullTimeUnit() throws Exception {
        ExecutorService e = new DirectExecutorService();
        List<Callable<String>> l = new ArrayList<Callable<String>>();
        l.add(new StringTask());
        try {
            e.invokeAny(l, MEDIUM_DELAY_MS, null);
            shouldThrow();
        } catch (NullPointerException success) {
        } finally {
            joinPool(e);
        }
    }

    /**
     * timed invokeAny(empty collection) throws IAE
     */
    public void testTimedInvokeAny2() throws Exception {
        ExecutorService e = new DirectExecutorService();
        try {
            e.invokeAny(new ArrayList<Callable<String>>(), MEDIUM_DELAY_MS, MILLISECONDS);
            shouldThrow();
        } catch (IllegalArgumentException success) {
        } finally {
            joinPool(e);
        }
    }

    /**
     * timed invokeAny(c) throws NPE if c has null elements
     */
    public void testTimedInvokeAny3() throws Exception {
        ExecutorService e = new DirectExecutorService();
        List<Callable<Integer>> l = new ArrayList<Callable<Integer>>();
        l.add(new Callable<Integer>() {
                  public Integer call() { return 5/0; }});
        l.add(null);
        try {
            e.invokeAny(l, MEDIUM_DELAY_MS, MILLISECONDS);
            shouldThrow();
        } catch (NullPointerException success) {
        } finally {
            joinPool(e);
        }
    }

    /**
     * timed invokeAny(c) throws ExecutionException if no task completes
     */
    public void testTimedInvokeAny4() throws Exception {
        ExecutorService e = new DirectExecutorService();
        List<Callable<String>> l = new ArrayList<Callable<String>>();
        l.add(new NPETask());
        try {
            e.invokeAny(l, MEDIUM_DELAY_MS, MILLISECONDS);
            shouldThrow();
        } catch (ExecutionException success) {
            assertTrue(success.getCause() instanceof NullPointerException);
        } finally {
            joinPool(e);
        }
    }

    /**
     * timed invokeAny(c) returns result of some task in c
     */
    public void testTimedInvokeAny5() throws Exception {
        ExecutorService e = new DirectExecutorService();
        try {
            List<Callable<String>> l = new ArrayList<Callable<String>>();
            l.add(new StringTask());
            l.add(new StringTask());
            String result = e.invokeAny(l, MEDIUM_DELAY_MS, MILLISECONDS);
            assertSame(TEST_STRING, result);
        } finally {
            joinPool(e);
        }
    }

    /**
     * timed invokeAll(null) throws NPE
     */
    public void testTimedInvokeAll1() throws InterruptedException {
        ExecutorService e = new DirectExecutorService();
        try {
            e.invokeAll(null, MEDIUM_DELAY_MS, MILLISECONDS);
            shouldThrow();
        } catch (NullPointerException success) {
        } finally {
            joinPool(e);
        }
    }

    /**
     * timed invokeAll(null time unit) throws NPE
     */
    public void testTimedInvokeAllNullTimeUnit() throws InterruptedException {
        ExecutorService e = new DirectExecutorService();
        List<Callable<String>> l = new ArrayList<Callable<String>>();
        l.add(new StringTask());
        try {
            e.invokeAll(l, MEDIUM_DELAY_MS, null);
            shouldThrow();
        } catch (NullPointerException success) {
        } finally {
            joinPool(e);
        }
    }

    /**
     * timed invokeAll(empty collection) returns empty collection
     */
    public void testTimedInvokeAll2() throws InterruptedException {
        ExecutorService e = new DirectExecutorService();
        try {
            List<Future<String>> r = e.invokeAll(new ArrayList<Callable<String>>(), MEDIUM_DELAY_MS, MILLISECONDS);
            assertTrue(r.isEmpty());
        } finally {
            joinPool(e);
        }
    }

    /**
     * timed invokeAll(c) throws NPE if c has null elements
     */
    public void testTimedInvokeAll3() throws InterruptedException {
        ExecutorService e = new DirectExecutorService();
        List<Callable<String>> l = new ArrayList<Callable<String>>();
        l.add(new StringTask());
        l.add(null);
        try {
            e.invokeAll(l, MEDIUM_DELAY_MS, MILLISECONDS);
            shouldThrow();
        } catch (NullPointerException success) {
        } finally {
            joinPool(e);
        }
    }

    /**
     * get of returned element of invokeAll(c) throws exception on failed task
     */
    public void testTimedInvokeAll4() throws Exception {
        ExecutorService e = new DirectExecutorService();
        try {
            List<Callable<String>> l = new ArrayList<Callable<String>>();
            l.add(new NPETask());
            List<Future<String>> futures =
                e.invokeAll(l, MEDIUM_DELAY_MS, MILLISECONDS);
            assertEquals(1, futures.size());
            try {
                futures.get(0).get();
                shouldThrow();
            } catch (ExecutionException success) {
                assertTrue(success.getCause() instanceof NullPointerException);
            }
        } finally {
            joinPool(e);
        }
    }

    /**
     * timed invokeAll(c) returns results of all completed tasks in c
     */
    public void testTimedInvokeAll5() throws Exception {
        ExecutorService e = new DirectExecutorService();
        try {
            List<Callable<String>> l = new ArrayList<Callable<String>>();
            l.add(new StringTask());
            l.add(new StringTask());
            List<Future<String>> futures =
                e.invokeAll(l, MEDIUM_DELAY_MS, MILLISECONDS);
            assertEquals(2, futures.size());
            for (Future<String> future : futures)
                assertSame(TEST_STRING, future.get());
        } finally {
            joinPool(e);
        }
    }

    /**
     * timed invokeAll cancels tasks not completed by timeout
     */
    public void testTimedInvokeAll6() throws InterruptedException {
        ExecutorService e = new DirectExecutorService();
        try {
            List<Callable<String>> l = new ArrayList<Callable<String>>();
            l.add(new StringTask());
            l.add(Executors.callable(new MediumPossiblyInterruptedRunnable(), TEST_STRING));
            l.add(new StringTask());
            List<Future<String>> futures =
                e.invokeAll(l, SMALL_DELAY_MS, MILLISECONDS);
            assertEquals(3, futures.size());
            Iterator<Future<String>> it = futures.iterator();
            Future<String> f1 = it.next();
            Future<String> f2 = it.next();
            Future<String> f3 = it.next();
            assertTrue(f1.isDone());
            assertFalse(f1.isCancelled());
            assertTrue(f2.isDone());
            assertTrue(f3.isDone());
            assertTrue(f3.isCancelled());
        } finally {
            joinPool(e);
        }
    }

}
