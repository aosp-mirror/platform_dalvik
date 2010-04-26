/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/licenses/publicdomain
 * Other contributors include Andrew Wright, Jeffrey Hayes,
 * Pat Fisher, Mike Judd.
 */

package tests.api.java.util.concurrent; // android-added

import java.util.concurrent.*;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import java.util.concurrent.locks.*;

import junit.framework.*;
import java.util.*;

public class ThreadPoolExecutorSubclassTest extends JSR166TestCase {
    public static Test suite() {
        return new TestSuite(ThreadPoolExecutorSubclassTest.class);
    }

    static class CustomTask<V> implements RunnableFuture<V> {
        final Callable<V> callable;
        final ReentrantLock lock = new ReentrantLock();
        final Condition cond = lock.newCondition();
        boolean done;
        boolean cancelled;
        V result;
        Thread thread;
        Exception exception;
        CustomTask(Callable<V> c) {
            if (c == null) throw new NullPointerException();
            callable = c;
        }
        CustomTask(final Runnable r, final V res) {
            if (r == null) throw new NullPointerException();
            callable = new Callable<V>() {
            public V call() throws Exception { r.run(); return res; }};
        }
        public boolean isDone() {
            lock.lock(); try { return done; } finally { lock.unlock() ; }
        }
        public boolean isCancelled() {
            lock.lock(); try { return cancelled; } finally { lock.unlock() ; }
        }
        public boolean cancel(boolean mayInterrupt) {
            lock.lock();
            try {
                if (!done) {
                    cancelled = true;
                    done = true;
                    if (mayInterrupt && thread != null)
                        thread.interrupt();
                    return true;
                }
                return false;
            }
            finally { lock.unlock() ; }
        }
        public void run() {
            boolean runme;
            lock.lock();
            try {
                runme = !done;
                if (!runme)
                    thread = Thread.currentThread();
            }
            finally { lock.unlock() ; }
            if (!runme) return;
            V v = null;
            Exception e = null;
            try {
                v = callable.call();
            }
            catch (Exception ex) {
                e = ex;
            }
            lock.lock();
            try {
                result = v;
                exception = e;
                done = true;
                thread = null;
                cond.signalAll();
            }
            finally { lock.unlock(); }
        }
        public V get() throws InterruptedException, ExecutionException {
            lock.lock();
            try {
                while (!done)
                    cond.await();
                if (exception != null)
                    throw new ExecutionException(exception);
                return result;
            }
            finally { lock.unlock(); }
        }
        public V get(long timeout, TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException {
            long nanos = unit.toNanos(timeout);
            lock.lock();
            try {
                for (;;) {
                    if (done) break;
                    if (nanos < 0)
                        throw new TimeoutException();
                    nanos = cond.awaitNanos(nanos);
                }
                if (exception != null)
                    throw new ExecutionException(exception);
                return result;
            }
            finally { lock.unlock(); }
        }
    }


    static class CustomTPE extends ThreadPoolExecutor {
        protected <V> RunnableFuture<V> newTaskFor(Callable<V> c) {
            return new CustomTask<V>(c);
        }
        protected <V> RunnableFuture<V> newTaskFor(Runnable r, V v) {
            return new CustomTask<V>(r, v);
        }

        CustomTPE(int corePoolSize,
                  int maximumPoolSize,
                  long keepAliveTime,
                  TimeUnit unit,
                  BlockingQueue<Runnable> workQueue) {
            super(corePoolSize, maximumPoolSize, keepAliveTime, unit,
                  workQueue);
        }
        CustomTPE(int corePoolSize,
                  int maximumPoolSize,
                  long keepAliveTime,
                  TimeUnit unit,
                  BlockingQueue<Runnable> workQueue,
                  ThreadFactory threadFactory) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue,
             threadFactory);
        }

        CustomTPE(int corePoolSize,
                  int maximumPoolSize,
                  long keepAliveTime,
                  TimeUnit unit,
                  BlockingQueue<Runnable> workQueue,
                  RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue,
              handler);
        }
        CustomTPE(int corePoolSize,
                  int maximumPoolSize,
                  long keepAliveTime,
                  TimeUnit unit,
                  BlockingQueue<Runnable> workQueue,
                  ThreadFactory threadFactory,
                  RejectedExecutionHandler handler) {
            super(corePoolSize, maximumPoolSize, keepAliveTime, unit,
              workQueue, threadFactory, handler);
        }

        volatile boolean beforeCalled = false;
        volatile boolean afterCalled = false;
        volatile boolean terminatedCalled = false;
        public CustomTPE() {
            super(1, 1, LONG_DELAY_MS, MILLISECONDS, new SynchronousQueue<Runnable>());
        }
        protected void beforeExecute(Thread t, Runnable r) {
            beforeCalled = true;
        }
        protected void afterExecute(Runnable r, Throwable t) {
            afterCalled = true;
        }
        protected void terminated() {
            terminatedCalled = true;
        }

    }

    static class FailingThreadFactory implements ThreadFactory {
        int calls = 0;
        public Thread newThread(Runnable r) {
            if (++calls > 1) return null;
            return new Thread(r);
        }
    }


    /**
     *  execute successfully executes a runnable
     */
    public void testExecute() throws InterruptedException {
        ThreadPoolExecutor p1 = new CustomTPE(1, 1, LONG_DELAY_MS, MILLISECONDS, new ArrayBlockingQueue<Runnable>(10));
        try {
            p1.execute(new ShortRunnable());
            Thread.sleep(SMALL_DELAY_MS);
        } finally {
            joinPool(p1);
        }
    }

    /**
     *  getActiveCount increases but doesn't overestimate, when a
     *  thread becomes active
     */
    public void testGetActiveCount() throws InterruptedException {
        ThreadPoolExecutor p2 = new CustomTPE(2, 2, LONG_DELAY_MS, MILLISECONDS, new ArrayBlockingQueue<Runnable>(10));
        assertEquals(0, p2.getActiveCount());
        p2.execute(new MediumRunnable());
        Thread.sleep(SHORT_DELAY_MS);
        assertEquals(1, p2.getActiveCount());
        joinPool(p2);
    }

    /**
     *  prestartCoreThread starts a thread if under corePoolSize, else doesn't
     */
    public void testPrestartCoreThread() {
        ThreadPoolExecutor p2 = new CustomTPE(2, 2, LONG_DELAY_MS, MILLISECONDS, new ArrayBlockingQueue<Runnable>(10));
        assertEquals(0, p2.getPoolSize());
        assertTrue(p2.prestartCoreThread());
        assertEquals(1, p2.getPoolSize());
        assertTrue(p2.prestartCoreThread());
        assertEquals(2, p2.getPoolSize());
        assertFalse(p2.prestartCoreThread());
        assertEquals(2, p2.getPoolSize());
        joinPool(p2);
    }

    /**
     *  prestartAllCoreThreads starts all corePoolSize threads
     */
    public void testPrestartAllCoreThreads() {
        ThreadPoolExecutor p2 = new CustomTPE(2, 2, LONG_DELAY_MS, MILLISECONDS, new ArrayBlockingQueue<Runnable>(10));
        assertEquals(0, p2.getPoolSize());
        p2.prestartAllCoreThreads();
        assertEquals(2, p2.getPoolSize());
        p2.prestartAllCoreThreads();
        assertEquals(2, p2.getPoolSize());
        joinPool(p2);
    }

    /**
     *   getCompletedTaskCount increases, but doesn't overestimate,
     *   when tasks complete
     */
    public void testGetCompletedTaskCount() throws InterruptedException {
        ThreadPoolExecutor p2 = new CustomTPE(2, 2, LONG_DELAY_MS, MILLISECONDS, new ArrayBlockingQueue<Runnable>(10));
        assertEquals(0, p2.getCompletedTaskCount());
        p2.execute(new ShortRunnable());
        Thread.sleep(SMALL_DELAY_MS);
        assertEquals(1, p2.getCompletedTaskCount());
        try { p2.shutdown(); } catch (SecurityException ok) { return; }
        joinPool(p2);
    }

    /**
     *   getCorePoolSize returns size given in constructor if not otherwise set
     */
    public void testGetCorePoolSize() {
        ThreadPoolExecutor p1 = new CustomTPE(1, 1, LONG_DELAY_MS, MILLISECONDS, new ArrayBlockingQueue<Runnable>(10));
        assertEquals(1, p1.getCorePoolSize());
        joinPool(p1);
    }

    /**
     *   getKeepAliveTime returns value given in constructor if not otherwise set
     */
    public void testGetKeepAliveTime() {
        ThreadPoolExecutor p2 = new CustomTPE(2, 2, 1000, MILLISECONDS, new ArrayBlockingQueue<Runnable>(10));
        assertEquals(1, p2.getKeepAliveTime(TimeUnit.SECONDS));
        joinPool(p2);
    }


    /**
     * getThreadFactory returns factory in constructor if not set
     */
    public void testGetThreadFactory() {
        ThreadFactory tf = new SimpleThreadFactory();
        ThreadPoolExecutor p = new CustomTPE(1,2,LONG_DELAY_MS, MILLISECONDS, new ArrayBlockingQueue<Runnable>(10), tf, new NoOpREHandler());
        assertSame(tf, p.getThreadFactory());
        joinPool(p);
    }

    /**
     * setThreadFactory sets the thread factory returned by getThreadFactory
     */
    public void testSetThreadFactory() {
        ThreadPoolExecutor p = new CustomTPE(1,2,LONG_DELAY_MS, MILLISECONDS, new ArrayBlockingQueue<Runnable>(10));
        ThreadFactory tf = new SimpleThreadFactory();
        p.setThreadFactory(tf);
        assertSame(tf, p.getThreadFactory());
        joinPool(p);
    }


    /**
     * setThreadFactory(null) throws NPE
     */
    public void testSetThreadFactoryNull() {
        ThreadPoolExecutor p = new CustomTPE(1,2,LONG_DELAY_MS, MILLISECONDS, new ArrayBlockingQueue<Runnable>(10));
        try {
            p.setThreadFactory(null);
            shouldThrow();
        } catch (NullPointerException success) {
        } finally {
            joinPool(p);
        }
    }

    /**
     * getRejectedExecutionHandler returns handler in constructor if not set
     */
    public void testGetRejectedExecutionHandler() {
        RejectedExecutionHandler h = new NoOpREHandler();
        ThreadPoolExecutor p = new CustomTPE(1,2,LONG_DELAY_MS, MILLISECONDS, new ArrayBlockingQueue<Runnable>(10), h);
        assertSame(h, p.getRejectedExecutionHandler());
        joinPool(p);
    }

    /**
     * setRejectedExecutionHandler sets the handler returned by
     * getRejectedExecutionHandler
     */
    public void testSetRejectedExecutionHandler() {
        ThreadPoolExecutor p = new CustomTPE(1,2,LONG_DELAY_MS, MILLISECONDS, new ArrayBlockingQueue<Runnable>(10));
        RejectedExecutionHandler h = new NoOpREHandler();
        p.setRejectedExecutionHandler(h);
        assertSame(h, p.getRejectedExecutionHandler());
        joinPool(p);
    }


    /**
     * setRejectedExecutionHandler(null) throws NPE
     */
    public void testSetRejectedExecutionHandlerNull() {
        ThreadPoolExecutor p = new CustomTPE(1,2,LONG_DELAY_MS, MILLISECONDS, new ArrayBlockingQueue<Runnable>(10));
        try {
            p.setRejectedExecutionHandler(null);
            shouldThrow();
        } catch (NullPointerException success) {
        } finally {
            joinPool(p);
        }
    }


    /**
     *   getLargestPoolSize increases, but doesn't overestimate, when
     *   multiple threads active
     */
    public void testGetLargestPoolSize() throws InterruptedException {
        ThreadPoolExecutor p2 = new CustomTPE(2, 2, LONG_DELAY_MS, MILLISECONDS, new ArrayBlockingQueue<Runnable>(10));
        assertEquals(0, p2.getLargestPoolSize());
        p2.execute(new MediumRunnable());
        p2.execute(new MediumRunnable());
        Thread.sleep(SHORT_DELAY_MS);
        assertEquals(2, p2.getLargestPoolSize());
        joinPool(p2);
    }

    /**
     *   getMaximumPoolSize returns value given in constructor if not
     *   otherwise set
     */
    public void testGetMaximumPoolSize() {
        ThreadPoolExecutor p2 = new CustomTPE(2, 2, LONG_DELAY_MS, MILLISECONDS, new ArrayBlockingQueue<Runnable>(10));
        assertEquals(2, p2.getMaximumPoolSize());
        joinPool(p2);
    }

    /**
     *   getPoolSize increases, but doesn't overestimate, when threads
     *   become active
     */
    public void testGetPoolSize() {
        ThreadPoolExecutor p1 = new CustomTPE(1, 1, LONG_DELAY_MS, MILLISECONDS, new ArrayBlockingQueue<Runnable>(10));
        assertEquals(0, p1.getPoolSize());
        p1.execute(new MediumRunnable());
        assertEquals(1, p1.getPoolSize());
        joinPool(p1);
    }

    /**
     *  getTaskCount increases, but doesn't overestimate, when tasks submitted
     */
    public void testGetTaskCount() throws InterruptedException {
        ThreadPoolExecutor p1 = new CustomTPE(1, 1, LONG_DELAY_MS, MILLISECONDS, new ArrayBlockingQueue<Runnable>(10));
        assertEquals(0, p1.getTaskCount());
        p1.execute(new MediumRunnable());
        Thread.sleep(SHORT_DELAY_MS);
        assertEquals(1, p1.getTaskCount());
        joinPool(p1);
    }

    /**
     *   isShutDown is false before shutdown, true after
     */
    public void testIsShutdown() {

        ThreadPoolExecutor p1 = new CustomTPE(1, 1, LONG_DELAY_MS, MILLISECONDS, new ArrayBlockingQueue<Runnable>(10));
        assertFalse(p1.isShutdown());
        try { p1.shutdown(); } catch (SecurityException ok) { return; }
        assertTrue(p1.isShutdown());
        joinPool(p1);
    }


    /**
     *  isTerminated is false before termination, true after
     */
    public void testIsTerminated() throws InterruptedException {
        ThreadPoolExecutor p1 = new CustomTPE(1, 1, LONG_DELAY_MS, MILLISECONDS, new ArrayBlockingQueue<Runnable>(10));
        assertFalse(p1.isTerminated());
        try {
            p1.execute(new MediumRunnable());
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
        ThreadPoolExecutor p1 = new CustomTPE(1, 1, LONG_DELAY_MS, MILLISECONDS, new ArrayBlockingQueue<Runnable>(10));
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
        BlockingQueue<Runnable> q = new ArrayBlockingQueue<Runnable>(10);
        ThreadPoolExecutor p1 = new CustomTPE(1, 1, LONG_DELAY_MS, MILLISECONDS, q);
        FutureTask[] tasks = new FutureTask[5];
        for (int i = 0; i < 5; i++) {
            tasks[i] = new FutureTask(new MediumPossiblyInterruptedRunnable(), Boolean.TRUE);
            p1.execute(tasks[i]);
        }
        try {
            Thread.sleep(SHORT_DELAY_MS);
            BlockingQueue<Runnable> wq = p1.getQueue();
            assertSame(q, wq);
            assertFalse(wq.contains(tasks[0]));
            assertTrue(wq.contains(tasks[4]));
            for (int i = 1; i < 5; ++i)
                tasks[i].cancel(true);
            p1.shutdownNow();
        } finally {
            joinPool(p1);
        }
    }

    /**
     * remove(task) removes queued task, and fails to remove active task
     */
    public void testRemove() throws InterruptedException {
        BlockingQueue<Runnable> q = new ArrayBlockingQueue<Runnable>(10);
        ThreadPoolExecutor p1 = new CustomTPE(1, 1, LONG_DELAY_MS, MILLISECONDS, q);
        FutureTask[] tasks = new FutureTask[5];
        for (int i = 0; i < 5; i++) {
            tasks[i] = new FutureTask(new MediumPossiblyInterruptedRunnable(), Boolean.TRUE);
            p1.execute(tasks[i]);
        }
        try {
            Thread.sleep(SHORT_DELAY_MS);
            assertFalse(p1.remove(tasks[0]));
            assertTrue(q.contains(tasks[4]));
            assertTrue(q.contains(tasks[3]));
            assertTrue(p1.remove(tasks[4]));
            assertFalse(p1.remove(tasks[4]));
            assertFalse(q.contains(tasks[4]));
            assertTrue(q.contains(tasks[3]));
            assertTrue(p1.remove(tasks[3]));
            assertFalse(q.contains(tasks[3]));
        } finally {
            joinPool(p1);
        }
    }

    /**
     *   purge removes cancelled tasks from the queue
     */
    public void testPurge() {
        ThreadPoolExecutor p1 = new CustomTPE(1, 1, LONG_DELAY_MS, MILLISECONDS, new ArrayBlockingQueue<Runnable>(10));
        FutureTask[] tasks = new FutureTask[5];
        for (int i = 0; i < 5; i++) {
            tasks[i] = new FutureTask(new MediumPossiblyInterruptedRunnable(), Boolean.TRUE);
            p1.execute(tasks[i]);
        }
        tasks[4].cancel(true);
        tasks[3].cancel(true);
        p1.purge();
        long count = p1.getTaskCount();
        assertTrue(count >= 2 && count < 5);
        joinPool(p1);
    }

    /**
     *  shutDownNow returns a list containing tasks that were not run
     */
    public void testShutDownNow() {
        ThreadPoolExecutor p1 = new CustomTPE(1, 1, LONG_DELAY_MS, MILLISECONDS, new ArrayBlockingQueue<Runnable>(10));
        List l;
        try {
            for (int i = 0; i < 5; i++)
                p1.execute(new MediumPossiblyInterruptedRunnable());
        }
        finally {
            try {
                l = p1.shutdownNow();
            } catch (SecurityException ok) { return; }
        }
        assertTrue(p1.isShutdown());
        assertTrue(l.size() <= 4);
    }

    // Exception Tests


    /**
     * Constructor throws if corePoolSize argument is less than zero
     */
    public void testConstructor1() {
        try {
            new CustomTPE(-1,1,LONG_DELAY_MS, MILLISECONDS, new ArrayBlockingQueue<Runnable>(10));
            shouldThrow();
        } catch (IllegalArgumentException success) {}
    }

    /**
     * Constructor throws if maximumPoolSize is less than zero
     */
    public void testConstructor2() {
        try {
            new CustomTPE(1,-1,LONG_DELAY_MS, MILLISECONDS, new ArrayBlockingQueue<Runnable>(10));
            shouldThrow();
        } catch (IllegalArgumentException success) {}
    }

    /**
     * Constructor throws if maximumPoolSize is equal to zero
     */
    public void testConstructor3() {
        try {
            new CustomTPE(1,0,LONG_DELAY_MS, MILLISECONDS, new ArrayBlockingQueue<Runnable>(10));
            shouldThrow();
        } catch (IllegalArgumentException success) {}
    }

    /**
     * Constructor throws if keepAliveTime is less than zero
     */
    public void testConstructor4() {
        try {
            new CustomTPE(1,2,-1L,MILLISECONDS, new ArrayBlockingQueue<Runnable>(10));
            shouldThrow();
        } catch (IllegalArgumentException success) {}
    }

    /**
     * Constructor throws if corePoolSize is greater than the maximumPoolSize
     */
    public void testConstructor5() {
        try {
            new CustomTPE(2,1,LONG_DELAY_MS, MILLISECONDS, new ArrayBlockingQueue<Runnable>(10));
            shouldThrow();
        } catch (IllegalArgumentException success) {}
    }

    /**
     * Constructor throws if workQueue is set to null
     */
    public void testConstructorNullPointerException() {
        try {
            new CustomTPE(1,2,LONG_DELAY_MS, MILLISECONDS,null);
            shouldThrow();
        } catch (NullPointerException success) {}
    }



    /**
     * Constructor throws if corePoolSize argument is less than zero
     */
    public void testConstructor6() {
        try {
            new CustomTPE(-1,1,LONG_DELAY_MS, MILLISECONDS, new ArrayBlockingQueue<Runnable>(10),new SimpleThreadFactory());
            shouldThrow();
        } catch (IllegalArgumentException success) {}
    }

    /**
     * Constructor throws if maximumPoolSize is less than zero
     */
    public void testConstructor7() {
        try {
            new CustomTPE(1,-1,LONG_DELAY_MS, MILLISECONDS, new ArrayBlockingQueue<Runnable>(10),new SimpleThreadFactory());
            shouldThrow();
        } catch (IllegalArgumentException success) {}
    }

    /**
     * Constructor throws if maximumPoolSize is equal to zero
     */
    public void testConstructor8() {
        try {
            new CustomTPE(1,0,LONG_DELAY_MS, MILLISECONDS, new ArrayBlockingQueue<Runnable>(10),new SimpleThreadFactory());
            shouldThrow();
        } catch (IllegalArgumentException success) {}
    }

    /**
     * Constructor throws if keepAliveTime is less than zero
     */
    public void testConstructor9() {
        try {
            new CustomTPE(1,2,-1L,MILLISECONDS, new ArrayBlockingQueue<Runnable>(10),new SimpleThreadFactory());
            shouldThrow();
        } catch (IllegalArgumentException success) {}
    }

    /**
     * Constructor throws if corePoolSize is greater than the maximumPoolSize
     */
    public void testConstructor10() {
        try {
            new CustomTPE(2,1,LONG_DELAY_MS, MILLISECONDS, new ArrayBlockingQueue<Runnable>(10),new SimpleThreadFactory());
            shouldThrow();
        } catch (IllegalArgumentException success) {}
    }

    /**
     * Constructor throws if workQueue is set to null
     */
    public void testConstructorNullPointerException2() {
        try {
            new CustomTPE(1,2,LONG_DELAY_MS, MILLISECONDS,null,new SimpleThreadFactory());
            shouldThrow();
        } catch (NullPointerException success) {}
    }

    /**
     * Constructor throws if threadFactory is set to null
     */
    public void testConstructorNullPointerException3() {
        try {
            ThreadFactory f = null;
            new CustomTPE(1,2,LONG_DELAY_MS, MILLISECONDS,new ArrayBlockingQueue<Runnable>(10),f);
            shouldThrow();
        } catch (NullPointerException success) {}
    }


    /**
     * Constructor throws if corePoolSize argument is less than zero
     */
    public void testConstructor11() {
        try {
            new CustomTPE(-1,1,LONG_DELAY_MS, MILLISECONDS, new ArrayBlockingQueue<Runnable>(10),new NoOpREHandler());
            shouldThrow();
        } catch (IllegalArgumentException success) {}
    }

    /**
     * Constructor throws if maximumPoolSize is less than zero
     */
    public void testConstructor12() {
        try {
            new CustomTPE(1,-1,LONG_DELAY_MS, MILLISECONDS, new ArrayBlockingQueue<Runnable>(10),new NoOpREHandler());
            shouldThrow();
        } catch (IllegalArgumentException success) {}
    }

    /**
     * Constructor throws if maximumPoolSize is equal to zero
     */
    public void testConstructor13() {
        try {
            new CustomTPE(1,0,LONG_DELAY_MS, MILLISECONDS, new ArrayBlockingQueue<Runnable>(10),new NoOpREHandler());
            shouldThrow();
        } catch (IllegalArgumentException success) {}
    }

    /**
     * Constructor throws if keepAliveTime is less than zero
     */
    public void testConstructor14() {
        try {
            new CustomTPE(1,2,-1L,MILLISECONDS, new ArrayBlockingQueue<Runnable>(10),new NoOpREHandler());
            shouldThrow();
        } catch (IllegalArgumentException success) {}
    }

    /**
     * Constructor throws if corePoolSize is greater than the maximumPoolSize
     */
    public void testConstructor15() {
        try {
            new CustomTPE(2,1,LONG_DELAY_MS, MILLISECONDS, new ArrayBlockingQueue<Runnable>(10),new NoOpREHandler());
            shouldThrow();
        } catch (IllegalArgumentException success) {}
    }

    /**
     * Constructor throws if workQueue is set to null
     */
    public void testConstructorNullPointerException4() {
        try {
            new CustomTPE(1,2,LONG_DELAY_MS, MILLISECONDS,null,new NoOpREHandler());
            shouldThrow();
        } catch (NullPointerException success) {}
    }

    /**
     * Constructor throws if handler is set to null
     */
    public void testConstructorNullPointerException5() {
        try {
            RejectedExecutionHandler r = null;
            new CustomTPE(1,2,LONG_DELAY_MS, MILLISECONDS,new ArrayBlockingQueue<Runnable>(10),r);
            shouldThrow();
        } catch (NullPointerException success) {}
    }


    /**
     * Constructor throws if corePoolSize argument is less than zero
     */
    public void testConstructor16() {
        try {
            new CustomTPE(-1,1,LONG_DELAY_MS, MILLISECONDS, new ArrayBlockingQueue<Runnable>(10),new SimpleThreadFactory(),new NoOpREHandler());
            shouldThrow();
        } catch (IllegalArgumentException success) {}
    }

    /**
     * Constructor throws if maximumPoolSize is less than zero
     */
    public void testConstructor17() {
        try {
            new CustomTPE(1,-1,LONG_DELAY_MS, MILLISECONDS, new ArrayBlockingQueue<Runnable>(10),new SimpleThreadFactory(),new NoOpREHandler());
            shouldThrow();
        } catch (IllegalArgumentException success) {}
    }

    /**
     * Constructor throws if maximumPoolSize is equal to zero
     */
    public void testConstructor18() {
        try {
            new CustomTPE(1,0,LONG_DELAY_MS, MILLISECONDS, new ArrayBlockingQueue<Runnable>(10),new SimpleThreadFactory(),new NoOpREHandler());
            shouldThrow();
        } catch (IllegalArgumentException success) {}
    }

    /**
     * Constructor throws if keepAliveTime is less than zero
     */
    public void testConstructor19() {
        try {
            new CustomTPE(1,2,-1L,MILLISECONDS, new ArrayBlockingQueue<Runnable>(10),new SimpleThreadFactory(),new NoOpREHandler());
            shouldThrow();
        } catch (IllegalArgumentException success) {}
    }

    /**
     * Constructor throws if corePoolSize is greater than the maximumPoolSize
     */
    public void testConstructor20() {
        try {
            new CustomTPE(2,1,LONG_DELAY_MS, MILLISECONDS, new ArrayBlockingQueue<Runnable>(10),new SimpleThreadFactory(),new NoOpREHandler());
            shouldThrow();
        } catch (IllegalArgumentException success) {}
    }

    /**
     * Constructor throws if workQueue is set to null
     */
    public void testConstructorNullPointerException6() {
        try {
            new CustomTPE(1,2,LONG_DELAY_MS, MILLISECONDS,null,new SimpleThreadFactory(),new NoOpREHandler());
            shouldThrow();
        } catch (NullPointerException success) {}
    }

    /**
     * Constructor throws if handler is set to null
     */
    public void testConstructorNullPointerException7() {
        try {
            RejectedExecutionHandler r = null;
            new CustomTPE(1,2,LONG_DELAY_MS, MILLISECONDS,new ArrayBlockingQueue<Runnable>(10),new SimpleThreadFactory(),r);
            shouldThrow();
        } catch (NullPointerException success) {}
    }

    /**
     * Constructor throws if ThreadFactory is set top null
     */
    public void testConstructorNullPointerException8() {
        try {
            ThreadFactory f = null;
            new CustomTPE(1,2,LONG_DELAY_MS, MILLISECONDS,new ArrayBlockingQueue<Runnable>(10),f,new NoOpREHandler());
            shouldThrow();
        } catch (NullPointerException success) {}
    }


    /**
     *  execute throws RejectedExecutionException
     *  if saturated.
     */
    public void testSaturatedExecute() {
        ThreadPoolExecutor p = new CustomTPE(1,1, LONG_DELAY_MS, MILLISECONDS, new ArrayBlockingQueue<Runnable>(1));
        try {

            for (int i = 0; i < 5; ++i) {
                p.execute(new MediumRunnable());
            }
            shouldThrow();
        } catch (RejectedExecutionException success) {}
        joinPool(p);
    }

    /**
     *  executor using CallerRunsPolicy runs task if saturated.
     */
    public void testSaturatedExecute2() {
        RejectedExecutionHandler h = new CustomTPE.CallerRunsPolicy();
        ThreadPoolExecutor p = new CustomTPE(1,1, LONG_DELAY_MS, MILLISECONDS, new ArrayBlockingQueue<Runnable>(1), h);
        try {

            TrackedNoOpRunnable[] tasks = new TrackedNoOpRunnable[5];
            for (int i = 0; i < 5; ++i) {
                tasks[i] = new TrackedNoOpRunnable();
            }
            TrackedLongRunnable mr = new TrackedLongRunnable();
            p.execute(mr);
            for (int i = 0; i < 5; ++i) {
                p.execute(tasks[i]);
            }
            for (int i = 1; i < 5; ++i) {
                assertTrue(tasks[i].done);
            }
            try { p.shutdownNow(); } catch (SecurityException ok) { return; }
        } finally {
            joinPool(p);
        }
    }

    /**
     *  executor using DiscardPolicy drops task if saturated.
     */
    public void testSaturatedExecute3() {
        RejectedExecutionHandler h = new CustomTPE.DiscardPolicy();
        ThreadPoolExecutor p = new CustomTPE(1,1, LONG_DELAY_MS, MILLISECONDS, new ArrayBlockingQueue<Runnable>(1), h);
        try {

            TrackedNoOpRunnable[] tasks = new TrackedNoOpRunnable[5];
            for (int i = 0; i < 5; ++i) {
                tasks[i] = new TrackedNoOpRunnable();
            }
            p.execute(new TrackedLongRunnable());
            for (int i = 0; i < 5; ++i) {
                p.execute(tasks[i]);
            }
            for (int i = 0; i < 5; ++i) {
                assertFalse(tasks[i].done);
            }
            try { p.shutdownNow(); } catch (SecurityException ok) { return; }
        } finally {
            joinPool(p);
        }
    }

    /**
     *  executor using DiscardOldestPolicy drops oldest task if saturated.
     */
    public void testSaturatedExecute4() {
        RejectedExecutionHandler h = new CustomTPE.DiscardOldestPolicy();
        ThreadPoolExecutor p = new CustomTPE(1,1, LONG_DELAY_MS, MILLISECONDS, new ArrayBlockingQueue<Runnable>(1), h);
        try {
            p.execute(new TrackedLongRunnable());
            TrackedLongRunnable r2 = new TrackedLongRunnable();
            p.execute(r2);
            assertTrue(p.getQueue().contains(r2));
            TrackedNoOpRunnable r3 = new TrackedNoOpRunnable();
            p.execute(r3);
            assertFalse(p.getQueue().contains(r2));
            assertTrue(p.getQueue().contains(r3));
            try { p.shutdownNow(); } catch (SecurityException ok) { return; }
        } finally {
            joinPool(p);
        }
    }

    /**
     *  execute throws RejectedExecutionException if shutdown
     */
    public void testRejectedExecutionExceptionOnShutdown() {
        ThreadPoolExecutor tpe =
            new CustomTPE(1,1,LONG_DELAY_MS, MILLISECONDS,new ArrayBlockingQueue<Runnable>(1));
        try { tpe.shutdown(); } catch (SecurityException ok) { return; }
        try {
            tpe.execute(new NoOpRunnable());
            shouldThrow();
        } catch (RejectedExecutionException success) {}

        joinPool(tpe);
    }

    /**
     *  execute using CallerRunsPolicy drops task on shutdown
     */
    public void testCallerRunsOnShutdown() {
        RejectedExecutionHandler h = new CustomTPE.CallerRunsPolicy();
        ThreadPoolExecutor p = new CustomTPE(1,1, LONG_DELAY_MS, MILLISECONDS, new ArrayBlockingQueue<Runnable>(1), h);

        try { p.shutdown(); } catch (SecurityException ok) { return; }
        try {
            TrackedNoOpRunnable r = new TrackedNoOpRunnable();
            p.execute(r);
            assertFalse(r.done);
        } finally {
            joinPool(p);
        }
    }

    /**
     *  execute using DiscardPolicy drops task on shutdown
     */
    public void testDiscardOnShutdown() {
        RejectedExecutionHandler h = new CustomTPE.DiscardPolicy();
        ThreadPoolExecutor p = new CustomTPE(1,1, LONG_DELAY_MS, MILLISECONDS, new ArrayBlockingQueue<Runnable>(1), h);

        try { p.shutdown(); } catch (SecurityException ok) { return; }
        try {
            TrackedNoOpRunnable r = new TrackedNoOpRunnable();
            p.execute(r);
            assertFalse(r.done);
        } finally {
            joinPool(p);
        }
    }


    /**
     *  execute using DiscardOldestPolicy drops task on shutdown
     */
    public void testDiscardOldestOnShutdown() {
        RejectedExecutionHandler h = new CustomTPE.DiscardOldestPolicy();
        ThreadPoolExecutor p = new CustomTPE(1,1, LONG_DELAY_MS, MILLISECONDS, new ArrayBlockingQueue<Runnable>(1), h);

        try { p.shutdown(); } catch (SecurityException ok) { return; }
        try {
            TrackedNoOpRunnable r = new TrackedNoOpRunnable();
            p.execute(r);
            assertFalse(r.done);
        } finally {
            joinPool(p);
        }
    }


    /**
     *  execute (null) throws NPE
     */
    public void testExecuteNull() {
        ThreadPoolExecutor tpe = null;
        try {
            tpe = new CustomTPE(1,2,LONG_DELAY_MS, MILLISECONDS,new ArrayBlockingQueue<Runnable>(10));
            tpe.execute(null);
            shouldThrow();
        } catch (NullPointerException success) {}

        joinPool(tpe);
    }

    /**
     *  setCorePoolSize of negative value throws IllegalArgumentException
     */
    public void testCorePoolSizeIllegalArgumentException() {
        ThreadPoolExecutor tpe =
            new CustomTPE(1,2,LONG_DELAY_MS, MILLISECONDS,new ArrayBlockingQueue<Runnable>(10));
        try {
            tpe.setCorePoolSize(-1);
            shouldThrow();
        } catch (IllegalArgumentException success) {
        } finally {
            try { tpe.shutdown(); } catch (SecurityException ok) { return; }
        }
        joinPool(tpe);
    }

    /**
     *  setMaximumPoolSize(int) throws IllegalArgumentException if
     *  given a value less the core pool size
     */
    public void testMaximumPoolSizeIllegalArgumentException() {
        ThreadPoolExecutor tpe =
            new CustomTPE(2,3,LONG_DELAY_MS, MILLISECONDS,new ArrayBlockingQueue<Runnable>(10));
        try {
            tpe.setMaximumPoolSize(1);
            shouldThrow();
        } catch (IllegalArgumentException success) {
        } finally {
            try { tpe.shutdown(); } catch (SecurityException ok) { return; }
        }
        joinPool(tpe);
    }

    /**
     *  setMaximumPoolSize throws IllegalArgumentException
     *  if given a negative value
     */
    public void testMaximumPoolSizeIllegalArgumentException2() {
        ThreadPoolExecutor tpe =
            new CustomTPE(2,3,LONG_DELAY_MS, MILLISECONDS,new ArrayBlockingQueue<Runnable>(10));
        try {
            tpe.setMaximumPoolSize(-1);
            shouldThrow();
        } catch (IllegalArgumentException success) {
        } finally {
            try { tpe.shutdown(); } catch (SecurityException ok) { return; }
        }
        joinPool(tpe);
    }


    /**
     *  setKeepAliveTime  throws IllegalArgumentException
     *  when given a negative value
     */
    public void testKeepAliveTimeIllegalArgumentException() {
        ThreadPoolExecutor tpe =
            new CustomTPE(2,3,LONG_DELAY_MS, MILLISECONDS,new ArrayBlockingQueue<Runnable>(10));

        try {
            tpe.setKeepAliveTime(-1,MILLISECONDS);
            shouldThrow();
        } catch (IllegalArgumentException success) {
        } finally {
            try { tpe.shutdown(); } catch (SecurityException ok) { return; }
        }
        joinPool(tpe);
    }

    /**
     * terminated() is called on termination
     */
    public void testTerminated() {
        CustomTPE tpe = new CustomTPE();
        try { tpe.shutdown(); } catch (SecurityException ok) { return; }
        assertTrue(tpe.terminatedCalled);
        joinPool(tpe);
    }

    /**
     * beforeExecute and afterExecute are called when executing task
     */
    public void testBeforeAfter() throws InterruptedException {
        CustomTPE tpe = new CustomTPE();
        try {
            TrackedNoOpRunnable r = new TrackedNoOpRunnable();
            tpe.execute(r);
            Thread.sleep(SHORT_DELAY_MS);
            assertTrue(r.done);
            assertTrue(tpe.beforeCalled);
            assertTrue(tpe.afterCalled);
            try { tpe.shutdown(); } catch (SecurityException ok) { return; }
        } finally {
            joinPool(tpe);
        }
    }

    /**
     * completed submit of callable returns result
     */
    public void testSubmitCallable() throws Exception {
        ExecutorService e = new CustomTPE(2, 2, LONG_DELAY_MS, MILLISECONDS, new ArrayBlockingQueue<Runnable>(10));
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
        ExecutorService e = new CustomTPE(2, 2, LONG_DELAY_MS, MILLISECONDS, new ArrayBlockingQueue<Runnable>(10));
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
        ExecutorService e = new CustomTPE(2, 2, LONG_DELAY_MS, MILLISECONDS, new ArrayBlockingQueue<Runnable>(10));
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
        ExecutorService e = new CustomTPE(2, 2, LONG_DELAY_MS, MILLISECONDS, new ArrayBlockingQueue<Runnable>(10));
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
        ExecutorService e = new CustomTPE(2, 2, LONG_DELAY_MS, MILLISECONDS, new ArrayBlockingQueue<Runnable>(10));
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
        ExecutorService e = new CustomTPE(2, 2, LONG_DELAY_MS, MILLISECONDS, new ArrayBlockingQueue<Runnable>(10));
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
        ExecutorService e = new CustomTPE(2, 2, LONG_DELAY_MS, MILLISECONDS, new ArrayBlockingQueue<Runnable>(10));
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
        ExecutorService e = new CustomTPE(2, 2, LONG_DELAY_MS, MILLISECONDS, new ArrayBlockingQueue<Runnable>(10));
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
        ExecutorService e = new CustomTPE(2, 2, LONG_DELAY_MS, MILLISECONDS, new ArrayBlockingQueue<Runnable>(10));
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
        ExecutorService e = new CustomTPE(2, 2, LONG_DELAY_MS, MILLISECONDS, new ArrayBlockingQueue<Runnable>(10));
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
        ExecutorService e = new CustomTPE(2, 2, LONG_DELAY_MS, MILLISECONDS, new ArrayBlockingQueue<Runnable>(10));
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
     * get of element of invokeAll(c) throws exception on failed task
     */
    public void testInvokeAll4() throws Exception {
        ExecutorService e = new CustomTPE(2, 2, LONG_DELAY_MS, MILLISECONDS, new ArrayBlockingQueue<Runnable>(10));
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
        ExecutorService e = new CustomTPE(2, 2, LONG_DELAY_MS, MILLISECONDS, new ArrayBlockingQueue<Runnable>(10));
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
        ExecutorService e = new CustomTPE(2, 2, LONG_DELAY_MS, MILLISECONDS, new ArrayBlockingQueue<Runnable>(10));
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
        ExecutorService e = new CustomTPE(2, 2, LONG_DELAY_MS, MILLISECONDS, new ArrayBlockingQueue<Runnable>(10));
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
        ExecutorService e = new CustomTPE(2, 2, LONG_DELAY_MS, MILLISECONDS, new ArrayBlockingQueue<Runnable>(10));
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
        ExecutorService e = new CustomTPE(2, 2, LONG_DELAY_MS, MILLISECONDS, new ArrayBlockingQueue<Runnable>(10));
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
        ExecutorService e = new CustomTPE(2, 2, LONG_DELAY_MS, MILLISECONDS, new ArrayBlockingQueue<Runnable>(10));
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
        ExecutorService e = new CustomTPE(2, 2, LONG_DELAY_MS, MILLISECONDS, new ArrayBlockingQueue<Runnable>(10));
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
        ExecutorService e = new CustomTPE(2, 2, LONG_DELAY_MS, MILLISECONDS, new ArrayBlockingQueue<Runnable>(10));
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
        ExecutorService e = new CustomTPE(2, 2, LONG_DELAY_MS, MILLISECONDS, new ArrayBlockingQueue<Runnable>(10));
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
        ExecutorService e = new CustomTPE(2, 2, LONG_DELAY_MS, MILLISECONDS, new ArrayBlockingQueue<Runnable>(10));
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
        ExecutorService e = new CustomTPE(2, 2, LONG_DELAY_MS, MILLISECONDS, new ArrayBlockingQueue<Runnable>(10));
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
        ExecutorService e = new CustomTPE(2, 2, LONG_DELAY_MS, MILLISECONDS, new ArrayBlockingQueue<Runnable>(10));
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
        ExecutorService e = new CustomTPE(2, 2, LONG_DELAY_MS, MILLISECONDS, new ArrayBlockingQueue<Runnable>(10));
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
        ExecutorService e = new CustomTPE(2, 2, LONG_DELAY_MS, MILLISECONDS, new ArrayBlockingQueue<Runnable>(10));
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

    /**
     * Execution continues if there is at least one thread even if
     * thread factory fails to create more
     */
    public void testFailingThreadFactory() throws InterruptedException {
        ExecutorService e = new CustomTPE(100, 100, LONG_DELAY_MS, MILLISECONDS, new LinkedBlockingQueue<Runnable>(), new FailingThreadFactory());
        try {
            for (int k = 0; k < 100; ++k) {
                e.execute(new NoOpRunnable());
            }
            Thread.sleep(LONG_DELAY_MS);
        } finally {
            joinPool(e);
        }
    }

    /**
     * allowsCoreThreadTimeOut is by default false.
     */
    public void testAllowsCoreThreadTimeOut() {
        ThreadPoolExecutor tpe = new CustomTPE(2, 2, 1000, MILLISECONDS, new ArrayBlockingQueue<Runnable>(10));
        assertFalse(tpe.allowsCoreThreadTimeOut());
        joinPool(tpe);
    }

    /**
     * allowCoreThreadTimeOut(true) causes idle threads to time out
     */
    public void testAllowCoreThreadTimeOut_true() throws InterruptedException {
        ThreadPoolExecutor tpe = new CustomTPE(2, 10, 10, MILLISECONDS, new ArrayBlockingQueue<Runnable>(10));
        tpe.allowCoreThreadTimeOut(true);
        tpe.execute(new NoOpRunnable());
        try {
            Thread.sleep(MEDIUM_DELAY_MS);
            assertEquals(0, tpe.getPoolSize());
        } finally {
            joinPool(tpe);
        }
    }

    /**
     * allowCoreThreadTimeOut(false) causes idle threads not to time out
     */
    public void testAllowCoreThreadTimeOut_false() throws InterruptedException {
        ThreadPoolExecutor tpe = new CustomTPE(2, 10, 10, MILLISECONDS, new ArrayBlockingQueue<Runnable>(10));
        tpe.allowCoreThreadTimeOut(false);
        tpe.execute(new NoOpRunnable());
        try {
            Thread.sleep(MEDIUM_DELAY_MS);
            assertTrue(tpe.getPoolSize() >= 1);
        } finally {
            joinPool(tpe);
        }
    }

}
