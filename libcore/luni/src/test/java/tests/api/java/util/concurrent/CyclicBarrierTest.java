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
import java.util.concurrent.locks.*;
import java.util.concurrent.atomic.*;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class CyclicBarrierTest extends JSR166TestCase {
    public static Test suite() {
        return new TestSuite(CyclicBarrierTest.class);
    }

    private volatile int countAction;
    private class MyAction implements Runnable {
        public void run() { ++countAction; }
    }

    /**
     * Creating with negative parties throws IAE
     */
    public void testConstructor1() {
        try {
            new CyclicBarrier(-1, (Runnable)null);
            shouldThrow();
        } catch (IllegalArgumentException success) {}
    }

    /**
     * Creating with negative parties and no action throws IAE
     */
    public void testConstructor2() {
        try {
            new CyclicBarrier(-1);
            shouldThrow();
        } catch (IllegalArgumentException success) {}
    }

    /**
     * getParties returns the number of parties given in constructor
     */
    public void testGetParties() {
        CyclicBarrier b = new CyclicBarrier(2);
        assertEquals(2, b.getParties());
        assertEquals(0, b.getNumberWaiting());
    }

    /**
     * A 1-party barrier triggers after single await
     */
    public void testSingleParty() throws Exception {
        CyclicBarrier b = new CyclicBarrier(1);
        assertEquals(1, b.getParties());
        assertEquals(0, b.getNumberWaiting());
        b.await();
        b.await();
        assertEquals(0, b.getNumberWaiting());
    }

    /**
     * The supplied barrier action is run at barrier
     */
    public void testBarrierAction() throws Exception {
        countAction = 0;
        CyclicBarrier b = new CyclicBarrier(1, new MyAction());
        assertEquals(1, b.getParties());
        assertEquals(0, b.getNumberWaiting());
        b.await();
        b.await();
        assertEquals(0, b.getNumberWaiting());
        assertEquals(countAction, 2);
    }

    /**
     * A 2-party/thread barrier triggers after both threads invoke await
     */
    public void testTwoParties() throws Exception {
        final CyclicBarrier b = new CyclicBarrier(2);
        Thread t = new Thread(new CheckedRunnable() {
            public void realRun() throws Exception {
                b.await();
                b.await();
                b.await();
                b.await();
            }});

        t.start();
        b.await();
        b.await();
        b.await();
        b.await();
        t.join();
    }


    /**
     * An interruption in one party causes others waiting in await to
     * throw BrokenBarrierException
     */
    public void testAwait1_Interrupted_BrokenBarrier() throws Exception {
        final CyclicBarrier c = new CyclicBarrier(3);
        Thread t1 = new ThreadShouldThrow(InterruptedException.class) {
            public void realRun() throws Exception {
                c.await();
            }};
        Thread t2 = new ThreadShouldThrow(BrokenBarrierException.class) {
            public void realRun() throws Exception {
                c.await();
            }};

        t1.start();
        t2.start();
        Thread.sleep(SHORT_DELAY_MS);
        t1.interrupt();
        t1.join();
        t2.join();
    }

    /**
     * An interruption in one party causes others waiting in timed await to
     * throw BrokenBarrierException
     */
    public void testAwait2_Interrupted_BrokenBarrier() throws Exception {
        final CyclicBarrier c = new CyclicBarrier(3);
        Thread t1 = new ThreadShouldThrow(InterruptedException.class) {
            public void realRun() throws Exception {
                c.await(LONG_DELAY_MS, MILLISECONDS);
            }};
        Thread t2 = new ThreadShouldThrow(BrokenBarrierException.class) {
            public void realRun() throws Exception {
                c.await(LONG_DELAY_MS, MILLISECONDS);
            }};

        t1.start();
        t2.start();
        Thread.sleep(SHORT_DELAY_MS);
        t1.interrupt();
        t1.join();
        t2.join();
    }

    /**
     * A timeout in timed await throws TimeoutException
     */
    public void testAwait3_TimeOutException() throws InterruptedException {
        final CyclicBarrier c = new CyclicBarrier(2);
        Thread t = new ThreadShouldThrow(TimeoutException.class) {
            public void realRun() throws Exception {
                c.await(SHORT_DELAY_MS, MILLISECONDS);
            }};

        t.start();
        t.join();
    }

    /**
     * A timeout in one party causes others waiting in timed await to
     * throw BrokenBarrierException
     */
    public void testAwait4_Timeout_BrokenBarrier() throws InterruptedException {
        final CyclicBarrier c = new CyclicBarrier(3);
        Thread t1 = new ThreadShouldThrow(TimeoutException.class) {
            public void realRun() throws Exception {
                c.await(SHORT_DELAY_MS, MILLISECONDS);
            }};
        Thread t2 = new ThreadShouldThrow(BrokenBarrierException.class) {
            public void realRun() throws Exception {
                c.await(MEDIUM_DELAY_MS, MILLISECONDS);
            }};

        t1.start();
        t2.start();
        t1.join();
        t2.join();
    }

    /**
     * A timeout in one party causes others waiting in await to
     * throw BrokenBarrierException
     */
    public void testAwait5_Timeout_BrokenBarrier() throws InterruptedException {
        final CyclicBarrier c = new CyclicBarrier(3);
        Thread t1 = new ThreadShouldThrow(TimeoutException.class) {
            public void realRun() throws Exception {
                c.await(SHORT_DELAY_MS, MILLISECONDS);
            }};
        Thread t2 = new ThreadShouldThrow(BrokenBarrierException.class) {
            public void realRun() throws Exception {
                c.await();
            }};

        t1.start();
        t2.start();
        t1.join();
        t2.join();
    }

    /**
     * A reset of an active barrier causes waiting threads to throw
     * BrokenBarrierException
     */
    public void testReset_BrokenBarrier() throws InterruptedException {
        final CyclicBarrier c = new CyclicBarrier(3);
        Thread t1 = new ThreadShouldThrow(BrokenBarrierException.class) {
            public void realRun() throws Exception {
                c.await();
            }};
        Thread t2 = new ThreadShouldThrow(BrokenBarrierException.class) {
            public void realRun() throws Exception {
                c.await();
            }};

        t1.start();
        t2.start();
        Thread.sleep(SHORT_DELAY_MS);
        c.reset();
        t1.join();
        t2.join();
    }

    /**
     * A reset before threads enter barrier does not throw
     * BrokenBarrierException
     */
    public void testReset_NoBrokenBarrier() throws Exception {
        final CyclicBarrier c = new CyclicBarrier(3);
        Thread t1 = new Thread(new CheckedRunnable() {
            public void realRun() throws Exception {
                c.await();
            }});
        Thread t2 = new Thread(new CheckedRunnable() {
            public void realRun() throws Exception {
                c.await();
            }});

        c.reset();
        t1.start();
        t2.start();
        c.await();
        t1.join();
        t2.join();
    }

    /**
     * All threads block while a barrier is broken.
     */
    public void testReset_Leakage() throws InterruptedException {
        final CyclicBarrier c = new CyclicBarrier(2);
        final AtomicBoolean done = new AtomicBoolean();
        Thread t = new Thread() {
                public void run() {
                    while (!done.get()) {
                        try {
                            while (c.isBroken())
                                c.reset();

                            c.await();
                            threadFail("await should not return");
                        }
                        catch (BrokenBarrierException e) {
                        }
                        catch (InterruptedException ie) {
                        }
                    }
                }
            };

        t.start();
        for (int i = 0; i < 4; i++) {
            Thread.sleep(SHORT_DELAY_MS);
            t.interrupt();
        }
        done.set(true);
        t.interrupt();
        t.join();
    }

    /**
     * Reset of a non-broken barrier does not break barrier
     */
    public void testResetWithoutBreakage() throws Exception {
        final CyclicBarrier start = new CyclicBarrier(3);
        final CyclicBarrier barrier = new CyclicBarrier(3);
        for (int i = 0; i < 3; i++) {
            Thread t1 = new Thread(new CheckedRunnable() {
                public void realRun() throws Exception {
                    start.await();
                    barrier.await();
                }});

            Thread t2 = new Thread(new CheckedRunnable() {
                public void realRun() throws Exception {
                    start.await();
                    barrier.await();
                }});

            t1.start();
            t2.start();
            start.await();
            barrier.await();
            t1.join();
            t2.join();
            assertFalse(barrier.isBroken());
            assertEquals(0, barrier.getNumberWaiting());
            if (i == 1) barrier.reset();
            assertFalse(barrier.isBroken());
            assertEquals(0, barrier.getNumberWaiting());
        }
    }

    /**
     * Reset of a barrier after interruption reinitializes it.
     */
    public void testResetAfterInterrupt() throws Exception {
        final CyclicBarrier start = new CyclicBarrier(3);
        final CyclicBarrier barrier = new CyclicBarrier(3);
        for (int i = 0; i < 2; i++) {
            Thread t1 = new ThreadShouldThrow(InterruptedException.class) {
                public void realRun() throws Exception {
                    start.await();
                    barrier.await();
                }};

            Thread t2 = new ThreadShouldThrow(BrokenBarrierException.class) {
                public void realRun() throws Exception {
                    start.await();
                    barrier.await();
                }};

            t1.start();
            t2.start();
            start.await();
            t1.interrupt();
            t1.join();
            t2.join();
            assertTrue(barrier.isBroken());
            assertEquals(0, barrier.getNumberWaiting());
            barrier.reset();
            assertFalse(barrier.isBroken());
            assertEquals(0, barrier.getNumberWaiting());
        }
    }

    /**
     * Reset of a barrier after timeout reinitializes it.
     */
    public void testResetAfterTimeout() throws Exception {
        final CyclicBarrier start = new CyclicBarrier(3);
        final CyclicBarrier barrier = new CyclicBarrier(3);
        for (int i = 0; i < 2; i++) {
            Thread t1 = new ThreadShouldThrow(TimeoutException.class) {
                    public void realRun() throws Exception {
                        start.await();
                        barrier.await(MEDIUM_DELAY_MS, MILLISECONDS);
                    }};

            Thread t2 = new ThreadShouldThrow(BrokenBarrierException.class) {
                public void realRun() throws Exception {
                    start.await();
                    barrier.await();
                }};

            t1.start();
            t2.start();
            start.await();
            t1.join();
            t2.join();
            assertTrue(barrier.isBroken());
            assertEquals(0, barrier.getNumberWaiting());
            barrier.reset();
            assertFalse(barrier.isBroken());
            assertEquals(0, barrier.getNumberWaiting());
        }
    }


    /**
     * Reset of a barrier after a failed command reinitializes it.
     */
    public void testResetAfterCommandException() throws Exception {
        final CyclicBarrier start = new CyclicBarrier(3);
        final CyclicBarrier barrier =
            new CyclicBarrier(3, new Runnable() {
                    public void run() {
                        throw new NullPointerException(); }});
        for (int i = 0; i < 2; i++) {
            Thread t1 = new ThreadShouldThrow(BrokenBarrierException.class) {
                public void realRun() throws Exception {
                    start.await();
                    barrier.await();
                }};

            Thread t2 = new ThreadShouldThrow(BrokenBarrierException.class) {
                public void realRun() throws Exception {
                    start.await();
                    barrier.await();
                }};

            t1.start();
            t2.start();
            start.await();
            while (barrier.getNumberWaiting() < 2) { Thread.yield(); }
            try {
                barrier.await();
                shouldThrow();
            } catch (NullPointerException success) {}
            t1.join();
            t2.join();
            assertTrue(barrier.isBroken());
            assertEquals(0, barrier.getNumberWaiting());
            barrier.reset();
            assertFalse(barrier.isBroken());
            assertEquals(0, barrier.getNumberWaiting());
        }
    }
}
