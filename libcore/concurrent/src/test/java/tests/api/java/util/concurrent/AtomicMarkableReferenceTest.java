/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/licenses/publicdomain
 * Other contributors include Andrew Wright, Jeffrey Hayes, 
 * Pat Fisher, Mike Judd. 
 */

package tests.api.java.util.concurrent;

import junit.framework.*;
import java.util.concurrent.atomic.*;

public class AtomicMarkableReferenceTest extends JSR166TestCase{
    public static Test suite() {
        return new TestSuite(AtomicMarkableReferenceTest.class);
    }
    
    /**
     *  constructor initializes to given reference and mark
     */
    public void testConstructor(){
        AtomicMarkableReference ai = new AtomicMarkableReference(one, false);
        assertEquals(one,ai.getReference());
        assertFalse(ai.isMarked());
        AtomicMarkableReference a2 = new AtomicMarkableReference(null, true);
        assertNull(a2.getReference());
        assertTrue(a2.isMarked());

    }

    /**
     *  get returns the last values of reference and mark set
     */
    public void testGetSet(){
        boolean[] mark = new boolean[1];
        AtomicMarkableReference ai = new AtomicMarkableReference(one, false);
        assertEquals(one,ai.getReference());
        assertFalse(ai.isMarked());
        assertEquals(one, ai.get(mark));
        assertFalse(mark[0]);
        ai.set(two, false);
        assertEquals(two,ai.getReference());
        assertFalse(ai.isMarked());
        assertEquals(two, ai.get(mark));
        assertFalse(mark[0]);
        ai.set(one, true);
        assertEquals(one,ai.getReference());
        assertTrue(ai.isMarked());
        assertEquals(one, ai.get(mark));
        assertTrue(mark[0]);
    }

    /**
     * attemptMark succeeds in single thread
     */
    public void testAttemptMark(){
        boolean[] mark = new boolean[1];
        AtomicMarkableReference ai = new AtomicMarkableReference(one, false);
        assertFalse(ai.isMarked());
        assertTrue(ai.attemptMark(one, true));
        assertTrue(ai.isMarked());
        assertEquals(one, ai.get(mark));
        assertTrue(mark[0]);
    }

    /**
     * compareAndSet succeeds in changing values if equal to expected reference
     * and mark else fails
     */
    public void testCompareAndSet(){
        boolean[] mark = new boolean[1];
        AtomicMarkableReference ai = new AtomicMarkableReference(one, false);
        assertEquals(one, ai.get(mark));
        assertFalse(ai.isMarked());
        assertFalse(mark[0]);

        assertTrue(ai.compareAndSet(one, two, false, false));
        assertEquals(two, ai.get(mark));
        assertFalse(mark[0]);

        assertTrue(ai.compareAndSet(two, m3, false, true));
        assertEquals(m3, ai.get(mark));
        assertTrue(mark[0]);

        assertFalse(ai.compareAndSet(two, m3, true, true));
        assertEquals(m3, ai.get(mark));
        assertTrue(mark[0]);
    }

    /**
     * compareAndSet in one thread enables another waiting for reference value
     * to succeed
     */
    public void testCompareAndSetInMultipleThreads() {
        final AtomicMarkableReference ai = new AtomicMarkableReference(one, false);
        Thread t = new Thread(new Runnable() {
                public void run() {
                    while(!ai.compareAndSet(two, three, false, false)) Thread.yield();
                }});
        try {
            t.start();
            assertTrue(ai.compareAndSet(one, two, false, false));
            t.join(LONG_DELAY_MS);
            assertFalse(t.isAlive());
            assertEquals(ai.getReference(), three);
            assertFalse(ai.isMarked());
        }
        catch(Exception e) {
            unexpectedException();
        }
    }

    /**
     * compareAndSet in one thread enables another waiting for mark value
     * to succeed
     */
    public void testCompareAndSetInMultipleThreads2() {
        final AtomicMarkableReference ai = new AtomicMarkableReference(one, false);
        Thread t = new Thread(new Runnable() {
                public void run() {
                    while(!ai.compareAndSet(one, one, true, false)) Thread.yield();
                }});
        try {
            t.start();
            assertTrue(ai.compareAndSet(one, one, false, true));
            t.join(LONG_DELAY_MS);
            assertFalse(t.isAlive());
            assertEquals(ai.getReference(), one);
            assertFalse(ai.isMarked());
        }
        catch(Exception e) {
            unexpectedException();
        }
    }

    /**
     * repeated weakCompareAndSet succeeds in changing values when equal
     * to expected 
     */
    public void testWeakCompareAndSet(){
        boolean[] mark = new boolean[1];
        AtomicMarkableReference ai = new AtomicMarkableReference(one, false);
        assertEquals(one, ai.get(mark));
        assertFalse(ai.isMarked());
        assertFalse(mark[0]);

        while(!ai.weakCompareAndSet(one, two, false, false));
        assertEquals(two, ai.get(mark));
        assertFalse(mark[0]);

        while(!ai.weakCompareAndSet(two, m3, false, true));
        assertEquals(m3, ai.get(mark));
        assertTrue(mark[0]);
    }

}
