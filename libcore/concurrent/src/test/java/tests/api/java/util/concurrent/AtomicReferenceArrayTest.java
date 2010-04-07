/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/licenses/publicdomain
 * Other contributors include Andrew Wright, Jeffrey Hayes,
 * Pat Fisher, Mike Judd.
 */

package tests.api.java.util.concurrent; // android-added

import junit.framework.*;
import java.util.concurrent.atomic.*;
import java.io.*;
import java.util.*;

public class AtomicReferenceArrayTest extends JSR166TestCase {
    public static Test suite() {
        return new TestSuite(AtomicReferenceArrayTest.class);
    }

    /**
     * constructor creates array of given size with all elements null
     */
    public void testConstructor() {
        AtomicReferenceArray<Integer> ai = new AtomicReferenceArray<Integer>(SIZE);
        for (int i = 0; i < SIZE; ++i) {
            assertNull(ai.get(i));
        }
    }

    /**
     * constructor with null array throws NPE
     */
    public void testConstructor2NPE() {
        try {
            Integer[] a = null;
            AtomicReferenceArray<Integer> ai = new AtomicReferenceArray<Integer>(a);
            shouldThrow();
        } catch (NullPointerException success) {}
    }

    /**
     * constructor with array is of same size and has all elements
     */
    public void testConstructor2() {
        Integer[] a = { two, one, three, four, seven};
        AtomicReferenceArray<Integer> ai = new AtomicReferenceArray<Integer>(a);
        assertEquals(a.length, ai.length());
        for (int i = 0; i < a.length; ++i)
            assertEquals(a[i], ai.get(i));
    }


    /**
     * get and set for out of bound indices throw IndexOutOfBoundsException
     */
    public void testIndexing() {
        AtomicReferenceArray<Integer> ai = new AtomicReferenceArray<Integer>(SIZE);
        try {
            ai.get(SIZE);
            shouldThrow();
        } catch (IndexOutOfBoundsException success) {
        }
        try {
            ai.get(-1);
            shouldThrow();
        } catch (IndexOutOfBoundsException success) {
        }
        try {
            ai.set(SIZE, null);
            shouldThrow();
        } catch (IndexOutOfBoundsException success) {
        }
        try {
            ai.set(-1, null);
            shouldThrow();
        } catch (IndexOutOfBoundsException success) {
        }
    }

    /**
     * get returns the last value set at index
     */
    public void testGetSet() {
        AtomicReferenceArray ai = new AtomicReferenceArray(SIZE);
        for (int i = 0; i < SIZE; ++i) {
            ai.set(i, one);
            assertSame(one,ai.get(i));
            ai.set(i, two);
            assertSame(two,ai.get(i));
            ai.set(i, m3);
            assertSame(m3,ai.get(i));
        }
    }

    /**
     * get returns the last value lazySet at index by same thread
     */
    public void testGetLazySet() {
        AtomicReferenceArray ai = new AtomicReferenceArray(SIZE);
        for (int i = 0; i < SIZE; ++i) {
            ai.lazySet(i, one);
            assertSame(one,ai.get(i));
            ai.lazySet(i, two);
            assertSame(two,ai.get(i));
            ai.lazySet(i, m3);
            assertSame(m3,ai.get(i));
        }
    }

    /**
     * compareAndSet succeeds in changing value if equal to expected else fails
     */
    public void testCompareAndSet() {
        AtomicReferenceArray ai = new AtomicReferenceArray(SIZE);
        for (int i = 0; i < SIZE; ++i) {
            ai.set(i, one);
            assertTrue(ai.compareAndSet(i, one,two));
            assertTrue(ai.compareAndSet(i, two,m4));
            assertSame(m4,ai.get(i));
            assertFalse(ai.compareAndSet(i, m5,seven));
            assertSame(m4,ai.get(i));
            assertTrue(ai.compareAndSet(i, m4,seven));
            assertSame(seven,ai.get(i));
        }
    }

    /**
     * compareAndSet in one thread enables another waiting for value
     * to succeed
     */
    public void testCompareAndSetInMultipleThreads() throws InterruptedException {
        final AtomicReferenceArray a = new AtomicReferenceArray(1);
        a.set(0, one);
        Thread t = new Thread(new CheckedRunnable() {
            public void realRun() {
                while (!a.compareAndSet(0, two, three))
                    Thread.yield();
            }});

        t.start();
        assertTrue(a.compareAndSet(0, one, two));
        t.join(LONG_DELAY_MS);
        assertFalse(t.isAlive());
        assertSame(a.get(0), three);
    }

    /**
     * repeated weakCompareAndSet succeeds in changing value when equal
     * to expected
     */
    public void testWeakCompareAndSet() {
        AtomicReferenceArray ai = new AtomicReferenceArray(SIZE);
        for (int i = 0; i < SIZE; ++i) {
            ai.set(i, one);
            while (!ai.weakCompareAndSet(i, one,two));
            while (!ai.weakCompareAndSet(i, two,m4));
            assertSame(m4,ai.get(i));
            while (!ai.weakCompareAndSet(i, m4,seven));
            assertSame(seven,ai.get(i));
        }
    }

    /**
     * getAndSet returns previous value and sets to given value at given index
     */
    public void testGetAndSet() {
        AtomicReferenceArray ai = new AtomicReferenceArray(SIZE);
        for (int i = 0; i < SIZE; ++i) {
            ai.set(i, one);
            assertSame(one,ai.getAndSet(i,zero));
            assertSame(zero,ai.getAndSet(i,m10));
            assertSame(m10,ai.getAndSet(i,one));
        }
    }

    /**
     * a deserialized serialized array holds same values
     */
    public void testSerialization() throws Exception {
        AtomicReferenceArray l = new AtomicReferenceArray(SIZE);
        for (int i = 0; i < SIZE; ++i) {
            l.set(i, new Integer(-i));
        }

        ByteArrayOutputStream bout = new ByteArrayOutputStream(10000);
        ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(bout));
        out.writeObject(l);
        out.close();

        ByteArrayInputStream bin = new ByteArrayInputStream(bout.toByteArray());
        ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(bin));
        AtomicReferenceArray r = (AtomicReferenceArray) in.readObject();
        assertEquals(l.length(), r.length());
        for (int i = 0; i < SIZE; ++i) {
            assertEquals(r.get(i), l.get(i));
        }
    }


    /**
     * toString returns current value.
     */
    public void testToString() {
        Integer[] a = { two, one, three, four, seven};
        AtomicReferenceArray<Integer> ai = new AtomicReferenceArray<Integer>(a);
        assertEquals(Arrays.toString(a), ai.toString());
    }
}
