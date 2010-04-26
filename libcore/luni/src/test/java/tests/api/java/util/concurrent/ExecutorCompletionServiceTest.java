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
import java.util.concurrent.atomic.*;
import java.math.BigInteger;
import java.security.*;

public class ExecutorCompletionServiceTest extends JSR166TestCase {
    public static Test suite() {
        return new TestSuite(ExecutorCompletionServiceTest.class);
    }


    /**
     * Creating a new ECS with null Executor throw NPE
     */
    public void testConstructorNPE() {
        try {
            ExecutorCompletionService ecs = new ExecutorCompletionService(null);
            shouldThrow();
        } catch (NullPointerException success) {}
    }

    /**
     * Creating a new ECS with null queue throw NPE
     */
    public void testConstructorNPE2() {
        try {
            ExecutorService e = Executors.newCachedThreadPool();
            ExecutorCompletionService ecs = new ExecutorCompletionService(e, null);
            shouldThrow();
        } catch (NullPointerException success) {}
    }

    /**
     * Submitting a null callable throws NPE
     */
    public void testSubmitNPE() {
        ExecutorService e = Executors.newCachedThreadPool();
        ExecutorCompletionService ecs = new ExecutorCompletionService(e);
        try {
            Callable c = null;
            ecs.submit(c);
            shouldThrow();
        } catch (NullPointerException success) {
        } finally {
            joinPool(e);
        }
    }

    /**
     * Submitting a null runnable throws NPE
     */
    public void testSubmitNPE2() {
        ExecutorService e = Executors.newCachedThreadPool();
        ExecutorCompletionService ecs = new ExecutorCompletionService(e);
        try {
            Runnable r = null;
            ecs.submit(r, Boolean.TRUE);
            shouldThrow();
        } catch (NullPointerException success) {
        } finally {
            joinPool(e);
        }
    }

    /**
     * A taken submitted task is completed
     */
    public void testTake() throws InterruptedException {
        ExecutorService e = Executors.newCachedThreadPool();
        ExecutorCompletionService ecs = new ExecutorCompletionService(e);
        try {
            Callable c = new StringTask();
            ecs.submit(c);
            Future f = ecs.take();
            assertTrue(f.isDone());
        } finally {
            joinPool(e);
        }
    }

    /**
     * Take returns the same future object returned by submit
     */
    public void testTake2() throws InterruptedException {
        ExecutorService e = Executors.newCachedThreadPool();
        ExecutorCompletionService ecs = new ExecutorCompletionService(e);
        try {
            Callable c = new StringTask();
            Future f1 = ecs.submit(c);
            Future f2 = ecs.take();
            assertSame(f1, f2);
        } finally {
            joinPool(e);
        }
    }

    /**
     * If poll returns non-null, the returned task is completed
     */
    public void testPoll1() throws InterruptedException {
        ExecutorService e = Executors.newCachedThreadPool();
        ExecutorCompletionService ecs = new ExecutorCompletionService(e);
        try {
            assertNull(ecs.poll());
            Callable c = new StringTask();
            ecs.submit(c);
            Thread.sleep(SHORT_DELAY_MS);
            for (;;) {
                Future f = ecs.poll();
                if (f != null) {
                    assertTrue(f.isDone());
                    break;
                }
            }
        } finally {
            joinPool(e);
        }
    }

    /**
     * If timed poll returns non-null, the returned task is completed
     */
    public void testPoll2() throws InterruptedException {
        ExecutorService e = Executors.newCachedThreadPool();
        ExecutorCompletionService ecs = new ExecutorCompletionService(e);
        try {
            assertNull(ecs.poll());
            Callable c = new StringTask();
            ecs.submit(c);
            Future f = ecs.poll(SHORT_DELAY_MS, MILLISECONDS);
            if (f != null)
                assertTrue(f.isDone());
        } finally {
            joinPool(e);
        }
    }
     /**
      * Submitting to underlying AES that overrides newTaskFor(Callable)
      * returns and eventually runs Future returned by newTaskFor.
      */
     public void testNewTaskForCallable() throws InterruptedException {
         final AtomicBoolean done = new AtomicBoolean(false);
         class MyCallableFuture<V> extends FutureTask<V> {
             MyCallableFuture(Callable<V> c) { super(c); }
             protected void done() { done.set(true); }
         }
         ExecutorService e = new ThreadPoolExecutor(
                                 1, 1, 30L, TimeUnit.SECONDS,
                                 new ArrayBlockingQueue<Runnable>(1)) {
             protected <T> RunnableFuture<T> newTaskFor(Callable<T> c) {
                 return new MyCallableFuture<T>(c);
             }
         };
         ExecutorCompletionService<String> ecs =
             new ExecutorCompletionService<String>(e);
         try {
             assertNull(ecs.poll());
             Callable<String> c = new StringTask();
             Future f1 = ecs.submit(c);
             assertTrue("submit must return MyCallableFuture",
                        f1 instanceof MyCallableFuture);
             Future f2 = ecs.take();
             assertSame("submit and take must return same objects", f1, f2);
             assertTrue("completed task must have set done", done.get());
         } finally {
             joinPool(e);
         }
     }

     /**
      * Submitting to underlying AES that overrides newTaskFor(Runnable,T)
      * returns and eventually runs Future returned by newTaskFor.
      */
     public void testNewTaskForRunnable() throws InterruptedException {
         final AtomicBoolean done = new AtomicBoolean(false);
         class MyRunnableFuture<V> extends FutureTask<V> {
             MyRunnableFuture(Runnable t, V r) { super(t, r); }
             protected void done() { done.set(true); }
         }
         ExecutorService e = new ThreadPoolExecutor(
                                 1, 1, 30L, TimeUnit.SECONDS,
                                 new ArrayBlockingQueue<Runnable>(1)) {
             protected <T> RunnableFuture<T> newTaskFor(Runnable t, T r) {
                 return new MyRunnableFuture<T>(t, r);
             }
         };
         ExecutorCompletionService<String> ecs =
             new ExecutorCompletionService<String>(e);
         try {
             assertNull(ecs.poll());
             Runnable r = new NoOpRunnable();
             Future f1 = ecs.submit(r, null);
             assertTrue("submit must return MyRunnableFuture",
                        f1 instanceof MyRunnableFuture);
             Future f2 = ecs.take();
             assertSame("submit and take must return same objects", f1, f2);
             assertTrue("completed task must have set done", done.get());
         } finally {
             joinPool(e);
         }
     }

}
