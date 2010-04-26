/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/licenses/publicdomain
 * Other contributors include Andrew Wright, Jeffrey Hayes,
 * Pat Fisher, Mike Judd.
 */

package tests.api.java.util.concurrent; // android-added

import junit.framework.*;
import java.util.concurrent.*;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import java.util.*;

public class FutureTaskTest extends JSR166TestCase {
    public static Test suite() {
        return new TestSuite(FutureTaskTest.class);
    }

    /**
     * Subclass to expose protected methods
     */
    static class PublicFutureTask extends FutureTask {
        public PublicFutureTask(Callable r) { super(r); }
        public boolean runAndReset() { return super.runAndReset(); }
        public void set(Object x) { super.set(x); }
        public void setException(Throwable t) { super.setException(t); }
    }

    /**
     * Creating a future with a null callable throws NPE
     */
    public void testConstructor() {
        try {
            FutureTask task = new FutureTask(null);
            shouldThrow();
        } catch (NullPointerException success) {}
    }

    /**
     * creating a future with null runnable fails
     */
    public void testConstructor2() {
        try {
            FutureTask task = new FutureTask(null, Boolean.TRUE);
            shouldThrow();
        } catch (NullPointerException success) {}
    }

    /**
     * isDone is true when a task completes
     */
    public void testIsDone() {
        FutureTask task = new FutureTask(new NoOpCallable());
        task.run();
        assertTrue(task.isDone());
        assertFalse(task.isCancelled());
    }

    /**
     * runAndReset of a non-cancelled task succeeds
     */
    public void testRunAndReset() {
        PublicFutureTask task = new PublicFutureTask(new NoOpCallable());
        assertTrue(task.runAndReset());
        assertFalse(task.isDone());
    }

    /**
     * runAndReset after cancellation fails
     */
    public void testResetAfterCancel() {
        PublicFutureTask task = new PublicFutureTask(new NoOpCallable());
        assertTrue(task.cancel(false));
        assertFalse(task.runAndReset());
        assertTrue(task.isDone());
        assertTrue(task.isCancelled());
    }



    /**
     * setting value causes get to return it
     */
    public void testSet() throws Exception {
        PublicFutureTask task = new PublicFutureTask(new NoOpCallable());
        task.set(one);
        assertSame(task.get(), one);
    }

    /**
     * setException causes get to throw ExecutionException
     */
    public void testSetException() throws Exception {
        Exception nse = new NoSuchElementException();
        PublicFutureTask task = new PublicFutureTask(new NoOpCallable());
        task.setException(nse);
        try {
            Object x = task.get();
            shouldThrow();
        } catch (ExecutionException success) {
            assertSame(success.getCause(), nse);
        }
    }

    /**
     *  Cancelling before running succeeds
     */
    public void testCancelBeforeRun() {
        FutureTask task = new FutureTask(new NoOpCallable());
        assertTrue(task.cancel(false));
        task.run();
        assertTrue(task.isDone());
        assertTrue(task.isCancelled());
    }

    /**
     * Cancel(true) before run succeeds
     */
    public void testCancelBeforeRun2() {
        FutureTask task = new FutureTask(new NoOpCallable());
        assertTrue(task.cancel(true));
        task.run();
        assertTrue(task.isDone());
        assertTrue(task.isCancelled());
    }

    /**
     * cancel of a completed task fails
     */
    public void testCancelAfterRun() {
        FutureTask task = new FutureTask(new NoOpCallable());
        task.run();
        assertFalse(task.cancel(false));
        assertTrue(task.isDone());
        assertFalse(task.isCancelled());
    }

    /**
     * cancel(true) interrupts a running task
     */
    public void testCancelInterrupt() throws InterruptedException {
        final FutureTask task =
            new FutureTask(new CheckedInterruptedCallable<Object>() {
                public Object realCall() throws InterruptedException {
                    Thread.sleep(SMALL_DELAY_MS);
                    return Boolean.TRUE;
                }});

        Thread t = new Thread(task);
        t.start();
        Thread.sleep(SHORT_DELAY_MS);
        assertTrue(task.cancel(true));
        t.join();
        assertTrue(task.isDone());
        assertTrue(task.isCancelled());
    }


    /**
     * cancel(false) does not interrupt a running task
     */
    public void testCancelNoInterrupt() throws InterruptedException {
        final FutureTask task =
            new FutureTask(new CheckedCallable<Object>() {
                public Object realCall() throws InterruptedException {
                    Thread.sleep(MEDIUM_DELAY_MS);
                    return Boolean.TRUE;
                }});

        Thread t = new Thread(task);
        t.start();
        Thread.sleep(SHORT_DELAY_MS);
        assertTrue(task.cancel(false));
        t.join();
        assertTrue(task.isDone());
        assertTrue(task.isCancelled());
    }

    /**
     * set in one thread causes get in another thread to retrieve value
     */
    public void testGet1() throws InterruptedException {
        final FutureTask ft =
            new FutureTask(new CheckedCallable<Object>() {
                public Object realCall() throws InterruptedException {
                    return Boolean.TRUE;
                }});
        Thread t = new Thread(new CheckedRunnable() {
            public void realRun() throws Exception {
                assertSame(Boolean.TRUE, ft.get());
            }});

        assertFalse(ft.isDone());
        assertFalse(ft.isCancelled());
        t.start();
        Thread.sleep(SHORT_DELAY_MS);
        ft.run();
        t.join();
        assertTrue(ft.isDone());
        assertFalse(ft.isCancelled());
    }

    /**
     * set in one thread causes timed get in another thread to retrieve value
     */
    public void testTimedGet1() throws InterruptedException {
        final FutureTask ft =
            new FutureTask(new CheckedCallable<Object>() {
                public Object realCall() throws InterruptedException {
                    return Boolean.TRUE;
                }});
        Thread t = new Thread(new CheckedRunnable() {
            public void realRun() throws Exception {
                assertSame(Boolean.TRUE, ft.get(SMALL_DELAY_MS, MILLISECONDS));
            }});

        assertFalse(ft.isDone());
        assertFalse(ft.isCancelled());
        t.start();
        Thread.sleep(SHORT_DELAY_MS);
        ft.run();
        t.join();
        assertTrue(ft.isDone());
        assertFalse(ft.isCancelled());
    }

    /**
     *  Cancelling a task causes timed get in another thread to throw CancellationException
     */
    public void testTimedGet_Cancellation() throws InterruptedException {
        final FutureTask ft =
            new FutureTask(new CheckedInterruptedCallable<Object>() {
                public Object realCall() throws InterruptedException {
                    Thread.sleep(SMALL_DELAY_MS);
                    return Boolean.TRUE;
                }});

        Thread t1 = new ThreadShouldThrow(CancellationException.class) {
            public void realRun() throws Exception {
                ft.get(MEDIUM_DELAY_MS, MILLISECONDS);
            }};
        Thread t2 = new Thread(ft);
        t1.start();
        t2.start();
        Thread.sleep(SHORT_DELAY_MS);
        ft.cancel(true);
        t1.join();
        t2.join();
    }

    /**
     * Cancelling a task causes get in another thread to throw CancellationException
     */
    public void testGet_Cancellation() throws InterruptedException {
        final FutureTask ft =
            new FutureTask(new CheckedInterruptedCallable<Object>() {
                public Object realCall() throws InterruptedException {
                    Thread.sleep(SMALL_DELAY_MS);
                    return Boolean.TRUE;
                }});
        Thread t1 = new ThreadShouldThrow(CancellationException.class) {
            public void realRun() throws Exception {
                ft.get();
            }};

        Thread t2 = new Thread(ft);
        t1.start();
        t2.start();
        Thread.sleep(SHORT_DELAY_MS);
        ft.cancel(true);
        t1.join();
        t2.join();
    }


    /**
     * A runtime exception in task causes get to throw ExecutionException
     */
    public void testGet_ExecutionException() throws InterruptedException {
        final FutureTask ft = new FutureTask(new Callable() {
            public Object call() {
                return 5/0;
            }});

        ft.run();
        try {
            ft.get();
            shouldThrow();
        } catch (ExecutionException success) {
            assertTrue(success.getCause() instanceof ArithmeticException);
        }
    }

    /**
     *  A runtime exception in task causes timed get to throw ExecutionException
     */
    public void testTimedGet_ExecutionException2() throws Exception {
        final FutureTask ft = new FutureTask(new Callable() {
            public Object call() {
                return 5/0;
            }});

        ft.run();
        try {
            ft.get(SHORT_DELAY_MS, MILLISECONDS);
            shouldThrow();
        } catch (ExecutionException success) {
            assertTrue(success.getCause() instanceof ArithmeticException);
        }
    }


    /**
     * Interrupting a waiting get causes it to throw InterruptedException
     */
    public void testGet_InterruptedException() throws InterruptedException {
        final FutureTask ft = new FutureTask(new NoOpCallable());
        Thread t = new Thread(new CheckedInterruptedRunnable() {
            public void realRun() throws Exception {
                ft.get();
            }});

        t.start();
        Thread.sleep(SHORT_DELAY_MS);
        t.interrupt();
        t.join();
    }

    /**
     *  Interrupting a waiting timed get causes it to throw InterruptedException
     */
    public void testTimedGet_InterruptedException2() throws InterruptedException {
        final FutureTask ft = new FutureTask(new NoOpCallable());
        Thread t = new Thread(new CheckedInterruptedRunnable() {
            public void realRun() throws Exception {
                ft.get(LONG_DELAY_MS,MILLISECONDS);
            }});

        t.start();
        Thread.sleep(SHORT_DELAY_MS);
        t.interrupt();
        t.join();
    }

    /**
     * A timed out timed get throws TimeoutException
     */
    public void testGet_TimeoutException() throws Exception {
        try {
            FutureTask ft = new FutureTask(new NoOpCallable());
            ft.get(1,MILLISECONDS);
            shouldThrow();
        } catch (TimeoutException success) {}
    }

}
