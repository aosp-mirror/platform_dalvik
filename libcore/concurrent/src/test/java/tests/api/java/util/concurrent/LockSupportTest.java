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

public class LockSupportTest extends JSR166TestCase {
    public static Test suite() {
        return new TestSuite(LockSupportTest.class);
    }

    /**
     * park is released by unpark occurring after park
     */
    public void testPark() throws InterruptedException {
        Thread t = new Thread(new CheckedRunnable() {
            public void realRun() {
                LockSupport.park();
            }});

        t.start();
        Thread.sleep(SHORT_DELAY_MS);
        LockSupport.unpark(t);
        t.join();
    }

    /**
     * park is released by unpark occurring before park
     */
    public void testPark2() throws InterruptedException {
        Thread t = new Thread(new CheckedRunnable() {
            public void realRun() throws InterruptedException {
                Thread.sleep(SHORT_DELAY_MS);
                LockSupport.park();
            }});

        t.start();
        LockSupport.unpark(t);
        t.join();
    }

    /**
     * park is released by interrupt
     */
    public void testPark3() throws InterruptedException {
        Thread t = new Thread(new CheckedRunnable() {
            public void realRun() {
                LockSupport.park();
            }});

        t.start();
        Thread.sleep(SHORT_DELAY_MS);
        t.interrupt();
        t.join();
    }

    /**
     * park returns if interrupted before park
     */
    public void testPark4() throws InterruptedException {
        final ReentrantLock lock = new ReentrantLock();
        lock.lock();
        Thread t = new Thread(new CheckedRunnable() {
            public void realRun() {
                lock.lock();
                LockSupport.park();
            }});

        t.start();
        Thread.sleep(SHORT_DELAY_MS);
        t.interrupt();
        lock.unlock();
        t.join();
    }

    /**
     * parkNanos times out if not unparked
     */
    public void testParkNanos() throws InterruptedException {
        Thread t = new Thread(new CheckedRunnable() {
            public void realRun() {
                LockSupport.parkNanos(1000);
            }});

        t.start();
        t.join();
    }


    /**
     * parkUntil times out if not unparked
     */
    public void testParkUntil() throws InterruptedException {
        Thread t = new Thread(new CheckedRunnable() {
            public void realRun() {
                long d = new Date().getTime() + 100;
                LockSupport.parkUntil(d);
            }});

        t.start();
        t.join();
    }
}
