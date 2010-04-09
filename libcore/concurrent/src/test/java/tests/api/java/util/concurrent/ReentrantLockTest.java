/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/licenses/publicdomain
 * Other contributors include Andrew Wright, Jeffrey Hayes,
 * Pat Fisher, Mike Judd.
 */

package tests.api.java.util.concurrent; // android-added

import junit.framework.*;
import java.util.concurrent.locks.*;
import java.util.concurrent.*;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import java.util.*;
import java.io.*;

public class ReentrantLockTest extends JSR166TestCase {
    public static Test suite() {
        return new TestSuite(ReentrantLockTest.class);
    }

    /**
     * A runnable calling lockInterruptibly
     */
    class InterruptibleLockRunnable extends CheckedRunnable {
        final ReentrantLock lock;
        InterruptibleLockRunnable(ReentrantLock l) { lock = l; }
        public void realRun() throws InterruptedException {
            lock.lockInterruptibly();
        }
    }


    /**
     * A runnable calling lockInterruptibly that expects to be
     * interrupted
     */
    class InterruptedLockRunnable extends CheckedInterruptedRunnable {
        final ReentrantLock lock;
        InterruptedLockRunnable(ReentrantLock l) { lock = l; }
        public void realRun() throws InterruptedException {
            lock.lockInterruptibly();
        }
    }

    /**
     * Subclass to expose protected methods
     */
    static class PublicReentrantLock extends ReentrantLock {
        PublicReentrantLock() { super(); }
        public Collection<Thread> getQueuedThreads() {
            return super.getQueuedThreads();
        }
        public Collection<Thread> getWaitingThreads(Condition c) {
            return super.getWaitingThreads(c);
        }
    }

    /**
     * Constructor sets given fairness
     */
    public void testConstructor() {
        assertFalse(new ReentrantLock().isFair());
        assertFalse(new ReentrantLock(false).isFair());
        assertTrue(new ReentrantLock(true).isFair());
    }

    /**
     * locking an unlocked lock succeeds
     */
    public void testLock() {
        ReentrantLock rl = new ReentrantLock();
        rl.lock();
        assertTrue(rl.isLocked());
        rl.unlock();
        assertFalse(rl.isLocked());
    }

    /**
     * locking an unlocked fair lock succeeds
     */
    public void testFairLock() {
        ReentrantLock rl = new ReentrantLock(true);
        rl.lock();
        assertTrue(rl.isLocked());
        rl.unlock();
    }

    /**
     * Unlocking an unlocked lock throws IllegalMonitorStateException
     */
    public void testUnlock_IllegalMonitorStateException() {
        ReentrantLock rl = new ReentrantLock();
        try {
            rl.unlock();
            shouldThrow();
        } catch (IllegalMonitorStateException success) {}
    }

    /**
     * tryLock on an unlocked lock succeeds
     */
    public void testTryLock() {
        ReentrantLock rl = new ReentrantLock();
        assertTrue(rl.tryLock());
        assertTrue(rl.isLocked());
        rl.unlock();
    }


    /**
     * hasQueuedThreads reports whether there are waiting threads
     */
    public void testhasQueuedThreads() throws InterruptedException {
        final ReentrantLock lock = new ReentrantLock();
        Thread t1 = new Thread(new InterruptedLockRunnable(lock));
        Thread t2 = new Thread(new InterruptibleLockRunnable(lock));
        assertFalse(lock.hasQueuedThreads());
        lock.lock();
        t1.start();
        Thread.sleep(SHORT_DELAY_MS);
        assertTrue(lock.hasQueuedThreads());
        t2.start();
        Thread.sleep(SHORT_DELAY_MS);
        assertTrue(lock.hasQueuedThreads());
        t1.interrupt();
        Thread.sleep(SHORT_DELAY_MS);
        assertTrue(lock.hasQueuedThreads());
        lock.unlock();
        Thread.sleep(SHORT_DELAY_MS);
        assertFalse(lock.hasQueuedThreads());
        t1.join();
        t2.join();
    }

    /**
     * getQueueLength reports number of waiting threads
     */
    public void testGetQueueLength() throws InterruptedException {
        final ReentrantLock lock = new ReentrantLock();
        Thread t1 = new Thread(new InterruptedLockRunnable(lock));
        Thread t2 = new Thread(new InterruptibleLockRunnable(lock));
        assertEquals(0, lock.getQueueLength());
        lock.lock();
        t1.start();
        Thread.sleep(SHORT_DELAY_MS);
        assertEquals(1, lock.getQueueLength());
        t2.start();
        Thread.sleep(SHORT_DELAY_MS);
        assertEquals(2, lock.getQueueLength());
        t1.interrupt();
        Thread.sleep(SHORT_DELAY_MS);
        assertEquals(1, lock.getQueueLength());
        lock.unlock();
        Thread.sleep(SHORT_DELAY_MS);
        assertEquals(0, lock.getQueueLength());
        t1.join();
        t2.join();
    }

    /**
     * getQueueLength reports number of waiting threads
     */
    public void testGetQueueLength_fair() throws InterruptedException {
        final ReentrantLock lock = new ReentrantLock(true);
        Thread t1 = new Thread(new InterruptedLockRunnable(lock));
        Thread t2 = new Thread(new InterruptibleLockRunnable(lock));
        assertEquals(0, lock.getQueueLength());
        lock.lock();
        t1.start();
        Thread.sleep(SHORT_DELAY_MS);
        assertEquals(1, lock.getQueueLength());
        t2.start();
        Thread.sleep(SHORT_DELAY_MS);
        assertEquals(2, lock.getQueueLength());
        t1.interrupt();
        Thread.sleep(SHORT_DELAY_MS);
        assertEquals(1, lock.getQueueLength());
        lock.unlock();
        Thread.sleep(SHORT_DELAY_MS);
        assertEquals(0, lock.getQueueLength());
        t1.join();
        t2.join();
    }

    /**
     * hasQueuedThread(null) throws NPE
     */
    public void testHasQueuedThreadNPE() {
        final ReentrantLock sync = new ReentrantLock();
        try {
            sync.hasQueuedThread(null);
            shouldThrow();
        } catch (NullPointerException success) {}
    }

    /**
     * hasQueuedThread reports whether a thread is queued.
     */
    public void testHasQueuedThread() throws InterruptedException {
        final ReentrantLock sync = new ReentrantLock();
        Thread t1 = new Thread(new InterruptedLockRunnable(sync));
        Thread t2 = new Thread(new InterruptibleLockRunnable(sync));
        assertFalse(sync.hasQueuedThread(t1));
        assertFalse(sync.hasQueuedThread(t2));
        sync.lock();
        t1.start();
        Thread.sleep(SHORT_DELAY_MS);
        assertTrue(sync.hasQueuedThread(t1));
        t2.start();
        Thread.sleep(SHORT_DELAY_MS);
        assertTrue(sync.hasQueuedThread(t1));
        assertTrue(sync.hasQueuedThread(t2));
        t1.interrupt();
        Thread.sleep(SHORT_DELAY_MS);
        assertFalse(sync.hasQueuedThread(t1));
        assertTrue(sync.hasQueuedThread(t2));
        sync.unlock();
        Thread.sleep(SHORT_DELAY_MS);
        assertFalse(sync.hasQueuedThread(t1));
        Thread.sleep(SHORT_DELAY_MS);
        assertFalse(sync.hasQueuedThread(t2));
        t1.join();
        t2.join();
    }


    /**
     * getQueuedThreads includes waiting threads
     */
    public void testGetQueuedThreads() throws InterruptedException {
        final PublicReentrantLock lock = new PublicReentrantLock();
        Thread t1 = new Thread(new InterruptedLockRunnable(lock));
        Thread t2 = new Thread(new InterruptibleLockRunnable(lock));
        assertTrue(lock.getQueuedThreads().isEmpty());
        lock.lock();
        assertTrue(lock.getQueuedThreads().isEmpty());
        t1.start();
        Thread.sleep(SHORT_DELAY_MS);
        assertTrue(lock.getQueuedThreads().contains(t1));
        t2.start();
        Thread.sleep(SHORT_DELAY_MS);
        assertTrue(lock.getQueuedThreads().contains(t1));
        assertTrue(lock.getQueuedThreads().contains(t2));
        t1.interrupt();
        Thread.sleep(SHORT_DELAY_MS);
        assertFalse(lock.getQueuedThreads().contains(t1));
        assertTrue(lock.getQueuedThreads().contains(t2));
        lock.unlock();
        Thread.sleep(SHORT_DELAY_MS);
        assertTrue(lock.getQueuedThreads().isEmpty());
        t1.join();
        t2.join();
    }


    /**
     * timed tryLock is interruptible.
     */
    public void testInterruptedException2() throws InterruptedException {
        final ReentrantLock lock = new ReentrantLock();
        lock.lock();
        Thread t = new Thread(new CheckedInterruptedRunnable() {
            public void realRun() throws InterruptedException {
                lock.tryLock(MEDIUM_DELAY_MS,MILLISECONDS);
            }});

        t.start();
        Thread.sleep(SHORT_DELAY_MS);
        t.interrupt();
        t.join();
    }


    /**
     * TryLock on a locked lock fails
     */
    public void testTryLockWhenLocked() throws InterruptedException {
        final ReentrantLock lock = new ReentrantLock();
        lock.lock();
        Thread t = new Thread(new CheckedRunnable() {
            public void realRun() {
                threadAssertFalse(lock.tryLock());
            }});

        t.start();
        t.join();
        lock.unlock();
    }

    /**
     * Timed tryLock on a locked lock times out
     */
    public void testTryLock_Timeout() throws InterruptedException {
        final ReentrantLock lock = new ReentrantLock();
        lock.lock();
        Thread t = new Thread(new CheckedRunnable() {
            public void realRun() throws InterruptedException {
                threadAssertFalse(lock.tryLock(1, MILLISECONDS));
            }});

        t.start();
        t.join();
        lock.unlock();
    }

    /**
     * getHoldCount returns number of recursive holds
     */
    public void testGetHoldCount() {
        ReentrantLock lock = new ReentrantLock();
        for (int i = 1; i <= SIZE; i++) {
            lock.lock();
            assertEquals(i, lock.getHoldCount());
        }
        for (int i = SIZE; i > 0; i--) {
            lock.unlock();
            assertEquals(i-1, lock.getHoldCount());
        }
    }


    /**
     * isLocked is true when locked and false when not
     */
    public void testIsLocked() throws InterruptedException {
        final ReentrantLock lock = new ReentrantLock();
        lock.lock();
        assertTrue(lock.isLocked());
        lock.unlock();
        assertFalse(lock.isLocked());
        Thread t = new Thread(new CheckedRunnable() {
            public void realRun() throws InterruptedException {
                lock.lock();
                Thread.sleep(SMALL_DELAY_MS);
                lock.unlock();
            }});

        t.start();
        Thread.sleep(SHORT_DELAY_MS);
        assertTrue(lock.isLocked());
        t.join();
        assertFalse(lock.isLocked());
    }


    /**
     * lockInterruptibly is interruptible.
     */
    public void testLockInterruptibly1() throws InterruptedException {
        final ReentrantLock lock = new ReentrantLock();
        lock.lock();
        Thread t = new Thread(new InterruptedLockRunnable(lock));
        t.start();
        Thread.sleep(SHORT_DELAY_MS);
        t.interrupt();
        Thread.sleep(SHORT_DELAY_MS);
        lock.unlock();
        t.join();
    }

    /**
     * lockInterruptibly succeeds when unlocked, else is interruptible
     */
    public void testLockInterruptibly2() throws InterruptedException {
        final ReentrantLock lock = new ReentrantLock();
        lock.lockInterruptibly();
        Thread t = new Thread(new InterruptedLockRunnable(lock));
        t.start();
        Thread.sleep(SHORT_DELAY_MS);
        t.interrupt();
        assertTrue(lock.isLocked());
        assertTrue(lock.isHeldByCurrentThread());
        t.join();
    }

    /**
     * Calling await without holding lock throws IllegalMonitorStateException
     */
    public void testAwait_IllegalMonitor() throws InterruptedException {
        final ReentrantLock lock = new ReentrantLock();
        final Condition c = lock.newCondition();
        try {
            c.await();
            shouldThrow();
        } catch (IllegalMonitorStateException success) {}
    }

    /**
     * Calling signal without holding lock throws IllegalMonitorStateException
     */
    public void testSignal_IllegalMonitor() {
        final ReentrantLock lock = new ReentrantLock();
        final Condition c = lock.newCondition();
        try {
            c.signal();
            shouldThrow();
        } catch (IllegalMonitorStateException success) {}
    }

    /**
     * awaitNanos without a signal times out
     */
    public void testAwaitNanos_Timeout() throws InterruptedException {
        final ReentrantLock lock = new ReentrantLock();
        final Condition c = lock.newCondition();
        lock.lock();
        long t = c.awaitNanos(100);
        assertTrue(t <= 0);
        lock.unlock();
    }

    /**
     *  timed await without a signal times out
     */
    public void testAwait_Timeout() throws InterruptedException {
        final ReentrantLock lock = new ReentrantLock();
        final Condition c = lock.newCondition();
        lock.lock();
        assertFalse(c.await(SHORT_DELAY_MS, MILLISECONDS));
        lock.unlock();
    }

    /**
     * awaitUntil without a signal times out
     */
    public void testAwaitUntil_Timeout() throws InterruptedException {
        final ReentrantLock lock = new ReentrantLock();
        final Condition c = lock.newCondition();
        lock.lock();
        java.util.Date d = new java.util.Date();
        assertFalse(c.awaitUntil(new java.util.Date(d.getTime() + 10)));
        lock.unlock();
    }

    /**
     * await returns when signalled
     */
    public void testAwait() throws InterruptedException {
        final ReentrantLock lock = new ReentrantLock();
        final Condition c = lock.newCondition();
        Thread t = new Thread(new CheckedRunnable() {
            public void realRun() throws InterruptedException {
                lock.lock();
                c.await();
                lock.unlock();
            }});

        t.start();
        Thread.sleep(SHORT_DELAY_MS);
        lock.lock();
        c.signal();
        lock.unlock();
        t.join(SHORT_DELAY_MS);
        assertFalse(t.isAlive());
    }

    /**
     * hasWaiters throws NPE if null
     */
    public void testHasWaitersNPE() {
        final ReentrantLock lock = new ReentrantLock();
        try {
            lock.hasWaiters(null);
            shouldThrow();
        } catch (NullPointerException success) {}
    }

    /**
     * getWaitQueueLength throws NPE if null
     */
    public void testGetWaitQueueLengthNPE() {
        final ReentrantLock lock = new ReentrantLock();
        try {
            lock.getWaitQueueLength(null);
            shouldThrow();
        } catch (NullPointerException success) {}
    }


    /**
     * getWaitingThreads throws NPE if null
     */
    public void testGetWaitingThreadsNPE() {
        final PublicReentrantLock lock = new PublicReentrantLock();
        try {
            lock.getWaitingThreads(null);
            shouldThrow();
        } catch (NullPointerException success) {}
    }


    /**
     * hasWaiters throws IAE if not owned
     */
    public void testHasWaitersIAE() {
        final ReentrantLock lock = new ReentrantLock();
        final Condition c = lock.newCondition();
        final ReentrantLock lock2 = new ReentrantLock();
        try {
            lock2.hasWaiters(c);
            shouldThrow();
        } catch (IllegalArgumentException success) {}
    }

    /**
     * hasWaiters throws IMSE if not locked
     */
    public void testHasWaitersIMSE() {
        final ReentrantLock lock = new ReentrantLock();
        final Condition c = lock.newCondition();
        try {
            lock.hasWaiters(c);
            shouldThrow();
        } catch (IllegalMonitorStateException success) {}
    }


    /**
     * getWaitQueueLength throws IAE if not owned
     */
    public void testGetWaitQueueLengthIAE() {
        final ReentrantLock lock = new ReentrantLock();
        final Condition c = lock.newCondition();
        final ReentrantLock lock2 = new ReentrantLock();
        try {
            lock2.getWaitQueueLength(c);
            shouldThrow();
        } catch (IllegalArgumentException success) {}
    }

    /**
     * getWaitQueueLength throws IMSE if not locked
     */
    public void testGetWaitQueueLengthIMSE() {
        final ReentrantLock lock = new ReentrantLock();
        final Condition c = lock.newCondition();
        try {
            lock.getWaitQueueLength(c);
            shouldThrow();
        } catch (IllegalMonitorStateException success) {}
    }


    /**
     * getWaitingThreads throws IAE if not owned
     */
    public void testGetWaitingThreadsIAE() {
        final PublicReentrantLock lock = new PublicReentrantLock();
        final Condition c = lock.newCondition();
        final PublicReentrantLock lock2 = new PublicReentrantLock();
        try {
            lock2.getWaitingThreads(c);
            shouldThrow();
        } catch (IllegalArgumentException success) {}
    }

    /**
     * getWaitingThreads throws IMSE if not locked
     */
    public void testGetWaitingThreadsIMSE() {
        final PublicReentrantLock lock = new PublicReentrantLock();
        final Condition c = lock.newCondition();
        try {
            lock.getWaitingThreads(c);
            shouldThrow();
        } catch (IllegalMonitorStateException success) {}
    }


    /**
     * hasWaiters returns true when a thread is waiting, else false
     */
    public void testHasWaiters() throws InterruptedException {
        final ReentrantLock lock = new ReentrantLock();
        final Condition c = lock.newCondition();
        Thread t = new Thread(new CheckedRunnable() {
            public void realRun() throws InterruptedException {
                lock.lock();
                threadAssertFalse(lock.hasWaiters(c));
                threadAssertEquals(0, lock.getWaitQueueLength(c));
                c.await();
                lock.unlock();
            }});

        t.start();
        Thread.sleep(SHORT_DELAY_MS);
        lock.lock();
        assertTrue(lock.hasWaiters(c));
        assertEquals(1, lock.getWaitQueueLength(c));
        c.signal();
        lock.unlock();
        Thread.sleep(SHORT_DELAY_MS);
        lock.lock();
        assertFalse(lock.hasWaiters(c));
        assertEquals(0, lock.getWaitQueueLength(c));
        lock.unlock();
        t.join(SHORT_DELAY_MS);
        assertFalse(t.isAlive());
    }

    /**
     * getWaitQueueLength returns number of waiting threads
     */
    public void testGetWaitQueueLength() throws InterruptedException {
        final ReentrantLock lock = new ReentrantLock();
        final Condition c = lock.newCondition();
        Thread t1 = new Thread(new CheckedRunnable() {
            public void realRun() throws InterruptedException {
                lock.lock();
                threadAssertFalse(lock.hasWaiters(c));
                threadAssertEquals(0, lock.getWaitQueueLength(c));
                c.await();
                lock.unlock();
            }});

        Thread t2 = new Thread(new CheckedRunnable() {
            public void realRun() throws InterruptedException {
                lock.lock();
                threadAssertTrue(lock.hasWaiters(c));
                threadAssertEquals(1, lock.getWaitQueueLength(c));
                c.await();
                lock.unlock();
            }});

        t1.start();
        Thread.sleep(SHORT_DELAY_MS);
        t2.start();
        Thread.sleep(SHORT_DELAY_MS);
        lock.lock();
        assertTrue(lock.hasWaiters(c));
        assertEquals(2, lock.getWaitQueueLength(c));
        c.signalAll();
        lock.unlock();
        Thread.sleep(SHORT_DELAY_MS);
        lock.lock();
        assertFalse(lock.hasWaiters(c));
        assertEquals(0, lock.getWaitQueueLength(c));
        lock.unlock();
        t1.join(SHORT_DELAY_MS);
        t2.join(SHORT_DELAY_MS);
        assertFalse(t1.isAlive());
        assertFalse(t2.isAlive());
    }

    /**
     * getWaitingThreads returns only and all waiting threads
     */
    public void testGetWaitingThreads() throws InterruptedException {
        final PublicReentrantLock lock = new PublicReentrantLock();
        final Condition c = lock.newCondition();
        Thread t1 = new Thread(new CheckedRunnable() {
            public void realRun() throws InterruptedException {
                lock.lock();
                threadAssertTrue(lock.getWaitingThreads(c).isEmpty());
                c.await();
                lock.unlock();
            }});

        Thread t2 = new Thread(new CheckedRunnable() {
            public void realRun() throws InterruptedException {
                lock.lock();
                threadAssertFalse(lock.getWaitingThreads(c).isEmpty());
                c.await();
                lock.unlock();
            }});

        lock.lock();
        assertTrue(lock.getWaitingThreads(c).isEmpty());
        lock.unlock();
        t1.start();
        Thread.sleep(SHORT_DELAY_MS);
        t2.start();
        Thread.sleep(SHORT_DELAY_MS);
        lock.lock();
        assertTrue(lock.hasWaiters(c));
        assertTrue(lock.getWaitingThreads(c).contains(t1));
        assertTrue(lock.getWaitingThreads(c).contains(t2));
        c.signalAll();
        lock.unlock();
        Thread.sleep(SHORT_DELAY_MS);
        lock.lock();
        assertFalse(lock.hasWaiters(c));
        assertTrue(lock.getWaitingThreads(c).isEmpty());
        lock.unlock();
        t1.join(SHORT_DELAY_MS);
        t2.join(SHORT_DELAY_MS);
        assertFalse(t1.isAlive());
        assertFalse(t2.isAlive());
    }

    /** A helper class for uninterruptible wait tests */
    class UninterruptibleThread extends Thread {
        private ReentrantLock lock;
        private Condition c;

        public volatile boolean canAwake = false;
        public volatile boolean interrupted = false;
        public volatile boolean lockStarted = false;

        public UninterruptibleThread(ReentrantLock lock, Condition c) {
            this.lock = lock;
            this.c = c;
        }

        public synchronized void run() {
            lock.lock();
            lockStarted = true;

            while (!canAwake) {
                c.awaitUninterruptibly();
            }

            interrupted = isInterrupted();
            lock.unlock();
        }
    }

    /**
     * awaitUninterruptibly doesn't abort on interrupt
     */
    public void testAwaitUninterruptibly() throws InterruptedException {
        final ReentrantLock lock = new ReentrantLock();
        final Condition c = lock.newCondition();
        UninterruptibleThread thread = new UninterruptibleThread(lock, c);

        thread.start();

        while (!thread.lockStarted) {
            Thread.sleep(100);
        }

        lock.lock();
        try {
            thread.interrupt();
            thread.canAwake = true;
            c.signal();
        } finally {
            lock.unlock();
        }

        thread.join();
        assertTrue(thread.interrupted);
        assertFalse(thread.isAlive());
    }

    /**
     * await is interruptible
     */
    public void testAwait_Interrupt() throws InterruptedException {
        final ReentrantLock lock = new ReentrantLock();
        final Condition c = lock.newCondition();
        Thread t = new Thread(new CheckedInterruptedRunnable() {
            public void realRun() throws InterruptedException {
                lock.lock();
                c.await();
            }});

        t.start();
        Thread.sleep(SHORT_DELAY_MS);
        t.interrupt();
        t.join(SHORT_DELAY_MS);
        assertFalse(t.isAlive());
    }

    /**
     * awaitNanos is interruptible
     */
    public void testAwaitNanos_Interrupt() throws InterruptedException {
        final ReentrantLock lock = new ReentrantLock();
        final Condition c = lock.newCondition();
        Thread t = new Thread(new CheckedInterruptedRunnable() {
            public void realRun() throws InterruptedException {
                lock.lock();
                c.awaitNanos(MILLISECONDS.toNanos(LONG_DELAY_MS));
            }});

        t.start();
        Thread.sleep(SHORT_DELAY_MS);
        t.interrupt();
        t.join(SHORT_DELAY_MS);
        assertFalse(t.isAlive());
    }

    /**
     * awaitUntil is interruptible
     */
    public void testAwaitUntil_Interrupt() throws InterruptedException {
        final ReentrantLock lock = new ReentrantLock();
        final Condition c = lock.newCondition();
        Thread t = new Thread(new CheckedInterruptedRunnable() {
            public void realRun() throws InterruptedException {
                lock.lock();
                java.util.Date d = new java.util.Date();
                c.awaitUntil(new java.util.Date(d.getTime() + 10000));
            }});

        t.start();
        Thread.sleep(SHORT_DELAY_MS);
        t.interrupt();
        t.join(SHORT_DELAY_MS);
        assertFalse(t.isAlive());
    }

    /**
     * signalAll wakes up all threads
     */
    public void testSignalAll() throws InterruptedException {
        final ReentrantLock lock = new ReentrantLock();
        final Condition c = lock.newCondition();
        Thread t1 = new Thread(new CheckedRunnable() {
            public void realRun() throws InterruptedException {
                lock.lock();
                c.await();
                lock.unlock();
            }});

        Thread t2 = new Thread(new CheckedRunnable() {
            public void realRun() throws InterruptedException {
                lock.lock();
                c.await();
                lock.unlock();
            }});

        t1.start();
        t2.start();
        Thread.sleep(SHORT_DELAY_MS);
        lock.lock();
        c.signalAll();
        lock.unlock();
        t1.join(SHORT_DELAY_MS);
        t2.join(SHORT_DELAY_MS);
        assertFalse(t1.isAlive());
        assertFalse(t2.isAlive());
    }

    /**
     * await after multiple reentrant locking preserves lock count
     */
    public void testAwaitLockCount() throws InterruptedException {
        final ReentrantLock lock = new ReentrantLock();
        final Condition c = lock.newCondition();
        Thread t1 = new Thread(new CheckedRunnable() {
            public void realRun() throws InterruptedException {
                lock.lock();
                threadAssertEquals(1, lock.getHoldCount());
                c.await();
                threadAssertEquals(1, lock.getHoldCount());
                lock.unlock();
            }});

        Thread t2 = new Thread(new CheckedRunnable() {
            public void realRun() throws InterruptedException {
                lock.lock();
                lock.lock();
                threadAssertEquals(2, lock.getHoldCount());
                c.await();
                threadAssertEquals(2, lock.getHoldCount());
                lock.unlock();
                lock.unlock();
            }});

        t1.start();
        t2.start();
        Thread.sleep(SHORT_DELAY_MS);
        lock.lock();
        c.signalAll();
        lock.unlock();
        t1.join(SHORT_DELAY_MS);
        t2.join(SHORT_DELAY_MS);
        assertFalse(t1.isAlive());
        assertFalse(t2.isAlive());
    }

    /**
     * A serialized lock deserializes as unlocked
     */
    public void testSerialization() throws Exception {
        ReentrantLock l = new ReentrantLock();
        l.lock();
        l.unlock();

        ByteArrayOutputStream bout = new ByteArrayOutputStream(10000);
        ObjectOutputStream out =
            new ObjectOutputStream(new BufferedOutputStream(bout));
        out.writeObject(l);
        out.close();

        ByteArrayInputStream bin =
            new ByteArrayInputStream(bout.toByteArray());
        ObjectInputStream in =
            new ObjectInputStream(new BufferedInputStream(bin));
        ReentrantLock r = (ReentrantLock) in.readObject();
        r.lock();
        r.unlock();
    }

    /**
     * toString indicates current lock state
     */
    public void testToString() {
        ReentrantLock lock = new ReentrantLock();
        String us = lock.toString();
        assertTrue(us.indexOf("Unlocked") >= 0);
        lock.lock();
        String ls = lock.toString();
        assertTrue(ls.indexOf("Locked") >= 0);
    }

}
