/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/licenses/publicdomain
 */

package tests.api.java.util.concurrent; // android-added

import junit.framework.*;
import java.util.*;
import java.util.concurrent.*;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import java.util.concurrent.atomic.*;

public class ScheduledExecutorSubclassTest extends JSR166TestCase {
    public static Test suite() {
        return new TestSuite(ScheduledExecutorSubclassTest.class);
    }

    static class CustomTask<V> implements RunnableScheduledFuture<V> {
        RunnableScheduledFuture<V> task;
        volatile boolean ran;
        CustomTask(RunnableScheduledFuture<V> t) { task = t; }
        public boolean isPeriodic() { return task.isPeriodic(); }
        public void run() {
            ran = true;
            task.run();
        }
        public long getDelay(TimeUnit unit) { return task.getDelay(unit); }
        public int compareTo(Delayed t) {
            return task.compareTo(((CustomTask)t).task);
        }
        public boolean cancel(boolean mayInterruptIfRunning) {
            return task.cancel(mayInterruptIfRunning);
        }
        public boolean isCancelled() { return task.isCancelled(); }
        public boolean isDone() { return task.isDone(); }
        public V get() throws InterruptedException,  ExecutionException {
            V v = task.get();
            assertTrue(ran);
            return v;
        }
        public V get(long time, TimeUnit unit) throws InterruptedException,  ExecutionException, TimeoutException {
            V v = task.get(time, unit);
            assertTrue(ran);
            return v;
        }
    }


    public class CustomExecutor extends ScheduledThreadPoolExecutor {

        protected <V> RunnableScheduledFuture<V> decorateTask(Runnable r, RunnableScheduledFuture<V> task) {
            return new CustomTask<V>(task);
        }

        protected <V> RunnableScheduledFuture<V> decorateTask(Callable<V> c, RunnableScheduledFuture<V> task) {
            return new CustomTask<V>(task);
        }
        CustomExecutor(int corePoolSize) { super(corePoolSize);}
        CustomExecutor(int corePoolSize, RejectedExecutionHandler handler) {
            super(corePoolSize, handler);
        }

        CustomExecutor(int corePoolSize, ThreadFactory threadFactory) {
            super(corePoolSize, threadFactory);
        }
        CustomExecutor(int corePoolSize, ThreadFactory threadFactory,
                       RejectedExecutionHandler handler) {
            super(corePoolSize, threadFactory, handler);
        }

    }


    /**
     * execute successfully executes a runnable
     */
    public void testExecute() throws InterruptedException {
        TrackedShortRunnable runnable =new TrackedShortRunnable();
        CustomExecutor p1 = new CustomExecutor(1);
        p1.execute(runnable);
        assertFalse(runnable.done);
        Thread.sleep(SHORT_DELAY_MS);
        try { p1.shutdown(); } catch (SecurityException ok) { return; }
        Thread.sleep(MEDIUM_DELAY_MS);
        assertTrue(runnable.done);
        try { p1.shutdown(); } catch (SecurityException ok) { return; }
        joinPool(p1);
    }


    /**
     * delayed schedule of callable successfully executes after delay
     */
    public void testSchedule1() throws Exception {
        TrackedCallable callable = new TrackedCallable();
        CustomExecutor p1 = new CustomExecutor(1);
        Future f = p1.schedule(callable, SHORT_DELAY_MS, MILLISECONDS);
        assertFalse(callable.done);
        Thread.sleep(MEDIUM_DELAY_MS);
        assertTrue(callable.done);
        assertEquals(Boolean.TRUE, f.get());
        try { p1.shutdown(); } catch (SecurityException ok) { return; }
        joinPool(p1);
    }

    /**
     *  delayed schedule of runnable successfully executes after delay
     */
    public void testSchedule3() throws InterruptedException {
        TrackedShortRunnable runnable = new TrackedShortRunnable();
        CustomExecutor p1 = new CustomExecutor(1);
        p1.schedule(runnable, SMALL_DELAY_MS, MILLISECONDS);
        Thread.sleep(SHORT_DELAY_MS);
        assertFalse(runnable.done);
        Thread.sleep(MEDIUM_DELAY_MS);
        assertTrue(runnable.done);
        try { p1.shutdown(); } catch (SecurityException ok) { return; }
        joinPool(p1);
    }

    /**
     * scheduleAtFixedRate executes runnable after given initial delay
     */
    public void testSchedule4() throws InterruptedException {
        TrackedShortRunnable runnable = new TrackedShortRunnable();
        CustomExecutor p1 = new CustomExecutor(1);
        ScheduledFuture h = p1.scheduleAtFixedRate(runnable, SHORT_DELAY_MS, SHORT_DELAY_MS, MILLISECONDS);
        assertFalse(runnable.done);
        Thread.sleep(MEDIUM_DELAY_MS);
        assertTrue(runnable.done);
        h.cancel(true);
        joinPool(p1);
    }

    static class RunnableCounter implements Runnable {
        AtomicInteger count = new AtomicInteger(0);
        public void run() { count.getAndIncrement(); }
    }

    /**
     * scheduleWithFixedDelay executes runnable after given initial delay
     */
    public void testSchedule5() throws InterruptedException {
        TrackedShortRunnable runnable = new TrackedShortRunnable();
        CustomExecutor p1 = new CustomExecutor(1);
        ScheduledFuture h = p1.scheduleWithFixedDelay(runnable, SHORT_DELAY_MS, SHORT_DELAY_MS, MILLISECONDS);
        assertFalse(runnable.done);
        Thread.sleep(MEDIUM_DELAY_MS);
        assertTrue(runnable.done);
        h.cancel(true);
        joinPool(p1);
    }

    /**
     * scheduleAtFixedRate executes series of tasks at given rate
     */
    public void testFixedRateSequence() throws InterruptedException {
        CustomExecutor p1 = new CustomExecutor(1);
        RunnableCounter counter = new RunnableCounter();
        ScheduledFuture h =
            p1.scheduleAtFixedRate(counter, 0, 1, MILLISECONDS);
        Thread.sleep(SMALL_DELAY_MS);
        h.cancel(true);
        int c = counter.count.get();
        // By time scaling conventions, we must have at least
        // an execution per SHORT delay, but no more than one SHORT more
        assertTrue(c >= SMALL_DELAY_MS / SHORT_DELAY_MS);
        assertTrue(c <= SMALL_DELAY_MS + SHORT_DELAY_MS);
        joinPool(p1);
    }

    /**
     * scheduleWithFixedDelay executes series of tasks with given period
     */
    public void testFixedDelaySequence() throws InterruptedException {
        CustomExecutor p1 = new CustomExecutor(1);
        RunnableCounter counter = new RunnableCounter();
        ScheduledFuture h =
            p1.scheduleWithFixedDelay(counter, 0, 1, MILLISECONDS);
        Thread.sleep(SMALL_DELAY_MS);
        h.cancel(true);
        int c = counter.count.get();
        assertTrue(c >= SMALL_DELAY_MS / SHORT_DELAY_MS);
        assertTrue(c <= SMALL_DELAY_MS + SHORT_DELAY_MS);
        joinPool(p1);
    }


    /**
     *  execute (null) throws NPE
     */
    public void testExecuteNull() throws InterruptedException {
        CustomExecutor se = new CustomExecutor(1);
        try {
            se.execute(null);
            shouldThrow();
        } catch (NullPointerException success) {}
        joinPool(se);
    }

    /**
     * schedule (null) throws NPE
     */
    public void testScheduleNull() throws InterruptedException {
        CustomExecutor se = new CustomExecutor(1);
        try {
            TrackedCallable callable = null;
            Future f = se.schedule(callable, SHORT_DELAY_MS, MILLISECONDS);
            shouldThrow();
        } catch (NullPointerException success) {}
        joinPool(se);
    }

    /**
     * execute throws RejectedExecutionException if shutdown
     */
    public void testSchedule1_RejectedExecutionException() {
        CustomExecutor se = new CustomExecutor(1);
        try {
            se.shutdown();
            se.schedule(new NoOpRunnable(),
                        MEDIUM_DELAY_MS, MILLISECONDS);
            shouldThrow();
        } catch (RejectedExecutionException success) {
        } catch (SecurityException ok) {
        }

        joinPool(se);
    }

    /**
     * schedule throws RejectedExecutionException if shutdown
     */
    public void testSchedule2_RejectedExecutionException() {
        CustomExecutor se = new CustomExecutor(1);
        try {
            se.shutdown();
            se.schedule(new NoOpCallable(),
                        MEDIUM_DELAY_MS, MILLISECONDS);
            shouldThrow();
        } catch (RejectedExecutionException success) {
        } catch (SecurityException ok) {
        }
        joinPool(se);
    }

    /**
     * schedule callable throws RejectedExecutionException if shutdown
     */
     public void testSchedule3_RejectedExecutionException() {
         CustomExecutor se = new CustomExecutor(1);
         try {
             se.shutdown();
             se.schedule(new NoOpCallable(),
                         MEDIUM_DELAY_MS, MILLISECONDS);
             shouldThrow();
         } catch (RejectedExecutionException success) {
         } catch (SecurityException ok) {
         }
         joinPool(se);
    }

    /**
     *  scheduleAtFixedRate throws RejectedExecutionException if shutdown
     */
    public void testScheduleAtFixedRate1_RejectedExecutionException() {
        CustomExecutor se = new CustomExecutor(1);
        try {
            se.shutdown();
            se.scheduleAtFixedRate(new NoOpRunnable(),
                                   MEDIUM_DELAY_MS, MEDIUM_DELAY_MS, MILLISECONDS);
            shouldThrow();
        } catch (RejectedExecutionException success) {
        } catch (SecurityException ok) {
        }
        joinPool(se);
    }

    /**
     * scheduleWithFixedDelay throws RejectedExecutionException if shutdown
     */
    public void testScheduleWithFixedDelay1_RejectedExecutionException() {
        CustomExecutor se = new CustomExecutor(1);
        try {
            se.shutdown();
            se.scheduleWithFixedDelay(new NoOpRunnable(),
                                      MEDIUM_DELAY_MS, MEDIUM_DELAY_MS, MILLISECONDS);
            shouldThrow();
        } catch (RejectedExecutionException success) {
        } catch (SecurityException ok) {
        }
        joinPool(se);
    }

    /**
     *  getActiveCount increases but doesn't overestimate, when a
     *  thread becomes active
     */
    public void testGetActiveCount() throws InterruptedException {
        CustomExecutor p2 = new CustomExecutor(2);
        assertEquals(0, p2.getActiveCount());
        p2.execute(new SmallRunnable());
        Thread.sleep(SHORT_DELAY_MS);
        assertEquals(1, p2.getActiveCount());
        joinPool(p2);
    }

    /**
     *    getCompletedTaskCount increases, but doesn't overestimate,
     *   when tasks complete
     */
    public void testGetCompletedTaskCount() throws InterruptedException {
        CustomExecutor p2 = new CustomExecutor(2);
        assertEquals(0, p2.getCompletedTaskCount());
        p2.execute(new SmallRunnable());
        Thread.sleep(MEDIUM_DELAY_MS);
        assertEquals(1, p2.getCompletedTaskCount());
        joinPool(p2);
    }

    /**
     *  getCorePoolSize returns size given in constructor if not otherwise set
     */
    public void testGetCorePoolSize() {
        CustomExecutor p1 = new CustomExecutor(1);
        assertEquals(1, p1.getCorePoolSize());
        joinPool(p1);
    }

    /**
     *    getLargestPoolSize increases, but doesn't overestimate, when
     *   multiple threads active
     */
    public void testGetLargestPoolSize() throws InterruptedException {
        CustomExecutor p2 = new CustomExecutor(2);
        assertEquals(0, p2.getLargestPoolSize());
        p2.execute(new SmallRunnable());
        p2.execute(new SmallRunnable());
        Thread.sleep(SHORT_DELAY_MS);
        assertEquals(2, p2.getLargestPoolSize());
        joinPool(p2);
    }

    /**
     *   getPoolSize increases, but doesn't overestimate, when threads
     *   become active
     */
    public void testGetPoolSize() {
        CustomExecutor p1 = new CustomExecutor(1);
        assertEquals(0, p1.getPoolSize());
        p1.execute(new SmallRunnable());
        assertEquals(1, p1.getPoolSize());
        joinPool(p1);
    }

    /**
     *    getTaskCount increases, but doesn't overestimate, when tasks
     *    submitted
     */
    public void testGetTaskCount() throws InterruptedException {
        CustomExecutor p1 = new CustomExecutor(1);
        assertEquals(0, p1.getTaskCount());
        for (int i = 0; i < 5; i++)
            p1.execute(new SmallRunnable());
        Thread.sleep(SHORT_DELAY_MS);
        assertEquals(5, p1.getTaskCount());
        joinPool(p1);
    }

    /**
     * getThreadFactory returns factory in constructor if not set
     */
    public void testGetThreadFactory() {
        ThreadFactory tf = new SimpleThreadFactory();
        CustomExecutor p = new CustomExecutor(1, tf);
        assertSame(tf, p.getThreadFactory());
        joinPool(p);
    }

    /**
     * setThreadFactory sets the thread factory returned by getThreadFactory
     */
    public void testSetThreadFactory() {
        ThreadFactory tf = new SimpleThreadFactory();
        CustomExecutor p = new CustomExecutor(1);
        p.setThreadFactory(tf);
        assertSame(tf, p.getThreadFactory());
        joinPool(p);
    }

    /**
     * setThreadFactory(null) throws NPE
     */
    public void testSetThreadFactoryNull() {
        CustomExecutor p = new CustomExecutor(1);
        try {
            p.setThreadFactory(null);
            shouldThrow();
        } catch (NullPointerException success) {
        } finally {
            joinPool(p);
        }
    }

    /**
     *   is isShutDown is false before shutdown, true after
     */
    public void testIsShutdown() {
        CustomExecutor p1 = new CustomExecutor(1);
        try {
            assertFalse(p1.isShutdown());
        }
        finally {
            try { p1.shutdown(); } catch (SecurityException ok) { return; }
        }
        assertTrue(p1.isShutdown());
    }


    /**
     *  isTerminated is false before termination, true after
     */
    public void testIsTerminated() throws InterruptedException {
        CustomExecutor p1 = new CustomExecutor(1);
        try {
            p1.execute(new SmallRunnable());
        } finally {
            try { p1.shutdown(); } catch (SecurityException ok) { return; }
        }
        assertTrue(p1.awaitTermination(LONG_DELAY_MS, MILLISECONDS));
        assertTrue(p1.isTerminated());
    }

    /**
     *  isTerminating is not true when running or when terminated
     */
    public void testIsTerminating() throws InterruptedException {
        CustomExecutor p1 = new CustomExecutor(1);
        assertFalse(p1.isTerminating());
        try {
            p1.execute(new SmallRunnable());
            assertFalse(p1.isTerminating());
        } finally {
            try { p1.shutdown(); } catch (SecurityException ok) { return; }
        }
        assertTrue(p1.awaitTermination(LONG_DELAY_MS, MILLISECONDS));
        assertTrue(p1.isTerminated());
        assertFalse(p1.isTerminating());
    }

    /**
     * getQueue returns the work queue, which contains queued tasks
     */
    public void testGetQueue() throws InterruptedException {
        CustomExecutor p1 = new CustomExecutor(1);
        ScheduledFuture[] tasks = new ScheduledFuture[5];
        for (int i = 0; i < 5; i++) {
            tasks[i] = p1.schedule(new SmallPossiblyInterruptedRunnable(), 1, MILLISECONDS);
        }
        try {
            Thread.sleep(SHORT_DELAY_MS);
            BlockingQueue<Runnable> q = p1.getQueue();
            assertTrue(q.contains(tasks[4]));
            assertFalse(q.contains(tasks[0]));
        } finally {
            joinPool(p1);
        }
    }

    /**
     * remove(task) removes queued task, and fails to remove active task
     */
    public void testRemove() throws InterruptedException {
        CustomExecutor p1 = new CustomExecutor(1);
        ScheduledFuture[] tasks = new ScheduledFuture[5];
        for (int i = 0; i < 5; i++) {
            tasks[i] = p1.schedule(new SmallPossiblyInterruptedRunnable(), 1, MILLISECONDS);
        }
        try {
            Thread.sleep(SHORT_DELAY_MS);
            BlockingQueue<Runnable> q = p1.getQueue();
            assertFalse(p1.remove((Runnable)tasks[0]));
            assertTrue(q.contains((Runnable)tasks[4]));
            assertTrue(q.contains((Runnable)tasks[3]));
            assertTrue(p1.remove((Runnable)tasks[4]));
            assertFalse(p1.remove((Runnable)tasks[4]));
            assertFalse(q.contains((Runnable)tasks[4]));
            assertTrue(q.contains((Runnable)tasks[3]));
            assertTrue(p1.remove((Runnable)tasks[3]));
            assertFalse(q.contains((Runnable)tasks[3]));
        } finally {
            joinPool(p1);
        }
    }

    /**
     *  purge removes cancelled tasks from the queue
     */
    public void testPurge() throws InterruptedException {
        CustomExecutor p1 = new CustomExecutor(1);
        ScheduledFuture[] tasks = new ScheduledFuture[5];
        for (int i = 0; i < 5; i++) {
            tasks[i] = p1.schedule(new SmallPossiblyInterruptedRunnable(), SHORT_DELAY_MS, MILLISECONDS);
        }
        try {
            int max = 5;
            if (tasks[4].cancel(true)) --max;
            if (tasks[3].cancel(true)) --max;
            // There must eventually be an interference-free point at
            // which purge will not fail. (At worst, when queue is empty.)
            int k;
            for (k = 0; k < SMALL_DELAY_MS; ++k) {
                p1.purge();
                long count = p1.getTaskCount();
                if (count >= 0 && count <= max)
                    break;
                Thread.sleep(1);
            }
            assertTrue(k < SMALL_DELAY_MS);
        } finally {
            joinPool(p1);
        }
    }

    /**
     *  shutDownNow returns a list containing tasks that were not run
     */
    public void testShutDownNow() {
        CustomExecutor p1 = new CustomExecutor(1);
        for (int i = 0; i < 5; i++)
            p1.schedule(new SmallPossiblyInterruptedRunnable(), SHORT_DELAY_MS, MILLISECONDS);
        List l;
        try {
            l = p1.shutdownNow();
        } catch (SecurityException ok) {
            return;
        }
        assertTrue(p1.isShutdown());
        assertTrue(l.size() > 0 && l.size() <= 5);
        joinPool(p1);
    }

    /**
     * In default setting, shutdown cancels periodic but not delayed
     * tasks at shutdown
     */
    public void testShutDown1() throws InterruptedException {
        CustomExecutor p1 = new CustomExecutor(1);
        assertTrue(p1.getExecuteExistingDelayedTasksAfterShutdownPolicy());
        assertFalse(p1.getContinueExistingPeriodicTasksAfterShutdownPolicy());

        ScheduledFuture[] tasks = new ScheduledFuture[5];
        for (int i = 0; i < 5; i++)
            tasks[i] = p1.schedule(new NoOpRunnable(), SHORT_DELAY_MS, MILLISECONDS);
        try { p1.shutdown(); } catch (SecurityException ok) { return; }
        BlockingQueue q = p1.getQueue();
        for (Iterator it = q.iterator(); it.hasNext();) {
            ScheduledFuture t = (ScheduledFuture)it.next();
            assertFalse(t.isCancelled());
        }
        assertTrue(p1.isShutdown());
        Thread.sleep(SMALL_DELAY_MS);
        for (int i = 0; i < 5; ++i) {
            assertTrue(tasks[i].isDone());
            assertFalse(tasks[i].isCancelled());
        }
    }


    /**
     * If setExecuteExistingDelayedTasksAfterShutdownPolicy is false,
     * delayed tasks are cancelled at shutdown
     */
    public void testShutDown2() throws InterruptedException {
        CustomExecutor p1 = new CustomExecutor(1);
        p1.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
        ScheduledFuture[] tasks = new ScheduledFuture[5];
        for (int i = 0; i < 5; i++)
            tasks[i] = p1.schedule(new NoOpRunnable(), SHORT_DELAY_MS, MILLISECONDS);
        try { p1.shutdown(); } catch (SecurityException ok) { return; }
        assertTrue(p1.isShutdown());
        BlockingQueue q = p1.getQueue();
        assertTrue(q.isEmpty());
        Thread.sleep(SMALL_DELAY_MS);
        assertTrue(p1.isTerminated());
    }


    /**
     * If setContinueExistingPeriodicTasksAfterShutdownPolicy is set false,
     * periodic tasks are not cancelled at shutdown
     */
    public void testShutDown3() throws InterruptedException {
        CustomExecutor p1 = new CustomExecutor(1);
        p1.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);
        ScheduledFuture task =
            p1.scheduleAtFixedRate(new NoOpRunnable(), 5, 5, MILLISECONDS);
        try { p1.shutdown(); } catch (SecurityException ok) { return; }
        assertTrue(p1.isShutdown());
        BlockingQueue q = p1.getQueue();
        assertTrue(q.isEmpty());
        Thread.sleep(SHORT_DELAY_MS);
        assertTrue(p1.isTerminated());
    }

    /**
     * if setContinueExistingPeriodicTasksAfterShutdownPolicy is true,
     * periodic tasks are cancelled at shutdown
     */
    public void testShutDown4() throws InterruptedException {
        CustomExecutor p1 = new CustomExecutor(1);
        try {
            p1.setContinueExistingPeriodicTasksAfterShutdownPolicy(true);
            ScheduledFuture task =
                p1.scheduleAtFixedRate(new NoOpRunnable(), 1, 1, MILLISECONDS);
            assertFalse(task.isCancelled());
            try { p1.shutdown(); } catch (SecurityException ok) { return; }
            assertFalse(task.isCancelled());
            assertFalse(p1.isTerminated());
            assertTrue(p1.isShutdown());
            Thread.sleep(SHORT_DELAY_MS);
            assertFalse(task.isCancelled());
            assertTrue(task.cancel(true));
            assertTrue(task.isDone());
            Thread.sleep(SHORT_DELAY_MS);
            assertTrue(p1.isTerminated());
        }
        finally {
            joinPool(p1);
        }
    }

    /**
     * completed submit of callable returns result
     */
    public void testSubmitCallable() throws Exception {
        ExecutorService e = new CustomExecutor(2);
        try {
            Future<String> future = e.submit(new StringTask());
            String result = future.get();
            assertSame(TEST_STRING, result);
        } finally {
            joinPool(e);
        }
    }

    /**
     * completed submit of runnable returns successfully
     */
    public void testSubmitRunnable() throws Exception {
        ExecutorService e = new CustomExecutor(2);
        try {
            Future<?> future = e.submit(new NoOpRunnable());
            future.get();
            assertTrue(future.isDone());
        } finally {
            joinPool(e);
        }
    }

    /**
     * completed submit of (runnable, result) returns result
     */
    public void testSubmitRunnable2() throws Exception {
        ExecutorService e = new CustomExecutor(2);
        try {
            Future<String> future = e.submit(new NoOpRunnable(), TEST_STRING);
            String result = future.get();
            assertSame(TEST_STRING, result);
        } finally {
            joinPool(e);
        }
    }

    /**
     * invokeAny(null) throws NPE
     */
    public void testInvokeAny1() throws Exception {
        ExecutorService e = new CustomExecutor(2);
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
    public void testInvokeAny2() throws Exception {
        ExecutorService e = new CustomExecutor(2);
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
        CountDownLatch latch = new CountDownLatch(1);
        ExecutorService e = new CustomExecutor(2);
        List<Callable<String>> l = new ArrayList<Callable<String>>();
        l.add(latchAwaitingStringTask(latch));
        l.add(null);
        try {
            e.invokeAny(l);
            shouldThrow();
        } catch (NullPointerException success) {
        } finally {
            latch.countDown();
            joinPool(e);
        }
    }

    /**
     * invokeAny(c) throws ExecutionException if no task completes
     */
    public void testInvokeAny4() throws Exception {
        ExecutorService e = new CustomExecutor(2);
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
     * invokeAny(c) returns result of some task
     */
    public void testInvokeAny5() throws Exception {
        ExecutorService e = new CustomExecutor(2);
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
    public void testInvokeAll1() throws Exception {
        ExecutorService e = new CustomExecutor(2);
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
    public void testInvokeAll2() throws Exception {
        ExecutorService e = new CustomExecutor(2);
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
    public void testInvokeAll3() throws Exception {
        ExecutorService e = new CustomExecutor(2);
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
     * get of invokeAll(c) throws exception on failed task
     */
    public void testInvokeAll4() throws Exception {
        ExecutorService e = new CustomExecutor(2);
        List<Callable<String>> l = new ArrayList<Callable<String>>();
        l.add(new NPETask());
        List<Future<String>> futures = e.invokeAll(l);
        assertEquals(1, futures.size());
        try {
            futures.get(0).get();
            shouldThrow();
        } catch (ExecutionException success) {
            assertTrue(success.getCause() instanceof NullPointerException);
        } finally {
            joinPool(e);
        }
    }

    /**
     * invokeAll(c) returns results of all completed tasks
     */
    public void testInvokeAll5() throws Exception {
        ExecutorService e = new CustomExecutor(2);
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
        ExecutorService e = new CustomExecutor(2);
        try {
            e.invokeAny(null, MEDIUM_DELAY_MS, MILLISECONDS);
            shouldThrow();
        } catch (NullPointerException success) {
        } finally {
            joinPool(e);
        }
    }

    /**
     * timed invokeAny(,,null) throws NPE
     */
    public void testTimedInvokeAnyNullTimeUnit() throws Exception {
        ExecutorService e = new CustomExecutor(2);
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
        ExecutorService e = new CustomExecutor(2);
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
        CountDownLatch latch = new CountDownLatch(1);
        ExecutorService e = new CustomExecutor(2);
        List<Callable<String>> l = new ArrayList<Callable<String>>();
        l.add(latchAwaitingStringTask(latch));
        l.add(null);
        try {
            e.invokeAny(l, MEDIUM_DELAY_MS, MILLISECONDS);
            shouldThrow();
        } catch (NullPointerException success) {
        } finally {
            latch.countDown();
            joinPool(e);
        }
    }

    /**
     * timed invokeAny(c) throws ExecutionException if no task completes
     */
    public void testTimedInvokeAny4() throws Exception {
        ExecutorService e = new CustomExecutor(2);
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
     * timed invokeAny(c) returns result of some task
     */
    public void testTimedInvokeAny5() throws Exception {
        ExecutorService e = new CustomExecutor(2);
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
    public void testTimedInvokeAll1() throws Exception {
        ExecutorService e = new CustomExecutor(2);
        try {
            e.invokeAll(null, MEDIUM_DELAY_MS, MILLISECONDS);
            shouldThrow();
        } catch (NullPointerException success) {
        } finally {
            joinPool(e);
        }
    }

    /**
     * timed invokeAll(,,null) throws NPE
     */
    public void testTimedInvokeAllNullTimeUnit() throws Exception {
        ExecutorService e = new CustomExecutor(2);
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
    public void testTimedInvokeAll2() throws Exception {
        ExecutorService e = new CustomExecutor(2);
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
    public void testTimedInvokeAll3() throws Exception {
        ExecutorService e = new CustomExecutor(2);
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
     * get of element of invokeAll(c) throws exception on failed task
     */
    public void testTimedInvokeAll4() throws Exception {
        ExecutorService e = new CustomExecutor(2);
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
        } finally {
            joinPool(e);
        }
    }

    /**
     * timed invokeAll(c) returns results of all completed tasks
     */
    public void testTimedInvokeAll5() throws Exception {
        ExecutorService e = new CustomExecutor(2);
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
     * timed invokeAll(c) cancels tasks not completed by timeout
     */
    public void testTimedInvokeAll6() throws Exception {
        ExecutorService e = new CustomExecutor(2);
        try {
            List<Callable<String>> l = new ArrayList<Callable<String>>();
            l.add(new StringTask());
            l.add(Executors.callable(new MediumPossiblyInterruptedRunnable(), TEST_STRING));
            l.add(new StringTask());
            List<Future<String>> futures =
                e.invokeAll(l, SHORT_DELAY_MS, MILLISECONDS);
            assertEquals(3, futures.size());
            Iterator<Future<String>> it = futures.iterator();
            Future<String> f1 = it.next();
            Future<String> f2 = it.next();
            Future<String> f3 = it.next();
            assertTrue(f1.isDone());
            assertTrue(f2.isDone());
            assertTrue(f3.isDone());
            assertFalse(f1.isCancelled());
            assertTrue(f2.isCancelled());
        } finally {
            joinPool(e);
        }
    }

}
