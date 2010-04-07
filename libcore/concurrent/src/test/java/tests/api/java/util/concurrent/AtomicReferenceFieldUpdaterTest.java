/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/licenses/publicdomain
 * Other contributors include Andrew Wright, Jeffrey Hayes,
 * Pat Fisher, Mike Judd.
 */

package tests.api.java.util.concurrent; // android-added

import java.util.concurrent.atomic.*;
import junit.framework.*;
import java.util.*;

public class AtomicReferenceFieldUpdaterTest extends JSR166TestCase {
    volatile Integer x = null;
    Object z;
    Integer w;

    public static Test suite() {
        return new TestSuite(AtomicReferenceFieldUpdaterTest.class);
    }

    /**
     * Construction with non-existent field throws RuntimeException
     */
    public void testConstructor() {
        try {
            AtomicReferenceFieldUpdater<AtomicReferenceFieldUpdaterTest, Integer>
                a = AtomicReferenceFieldUpdater.newUpdater
                (AtomicReferenceFieldUpdaterTest.class, Integer.class, "y");
            shouldThrow();
        } catch (RuntimeException success) {}
    }


    /**
     * construction with field not of given type throws RuntimeException
     */
    public void testConstructor2() {
        try {
            AtomicReferenceFieldUpdater<AtomicReferenceFieldUpdaterTest, Integer>
                a = AtomicReferenceFieldUpdater.newUpdater
                (AtomicReferenceFieldUpdaterTest.class, Integer.class, "z");
            shouldThrow();
        } catch (RuntimeException success) {}
    }

    /**
     * Constructor with non-volatile field throws exception
     */
    public void testConstructor3() {
        try {
            AtomicReferenceFieldUpdater<AtomicReferenceFieldUpdaterTest, Integer>
                a = AtomicReferenceFieldUpdater.newUpdater
                (AtomicReferenceFieldUpdaterTest.class, Integer.class, "w");
            shouldThrow();
        } catch (RuntimeException success) {}
    }

    /**
     *  get returns the last value set or assigned
     */
    public void testGetSet() {
        AtomicReferenceFieldUpdater<AtomicReferenceFieldUpdaterTest, Integer>a;
        try {
            a = AtomicReferenceFieldUpdater.newUpdater(AtomicReferenceFieldUpdaterTest.class, Integer.class, "x");
        } catch (RuntimeException ok) {
            return;
        }
        x = one;
        assertSame(one,a.get(this));
        a.set(this,two);
        assertSame(two,a.get(this));
        a.set(this,m3);
        assertSame(m3,a.get(this));
    }

    /**
     *  get returns the last value lazySet by same thread
     */
    public void testGetLazySet() {
        AtomicReferenceFieldUpdater<AtomicReferenceFieldUpdaterTest, Integer>a;
        try {
            a = AtomicReferenceFieldUpdater.newUpdater(AtomicReferenceFieldUpdaterTest.class, Integer.class, "x");
        } catch (RuntimeException ok) {
            return;
        }
        x = one;
        assertSame(one,a.get(this));
        a.lazySet(this,two);
        assertSame(two,a.get(this));
        a.lazySet(this,m3);
        assertSame(m3,a.get(this));
    }

    /**
     * compareAndSet succeeds in changing value if equal to expected else fails
     */
    public void testCompareAndSet() {
        AtomicReferenceFieldUpdater<AtomicReferenceFieldUpdaterTest, Integer>a;
        try {
            a = AtomicReferenceFieldUpdater.newUpdater(AtomicReferenceFieldUpdaterTest.class, Integer.class, "x");
        } catch (RuntimeException ok) {
            return;
        }
        x = one;
        assertTrue(a.compareAndSet(this,one,two));
        assertTrue(a.compareAndSet(this,two,m4));
        assertSame(m4,a.get(this));
        assertFalse(a.compareAndSet(this,m5,seven));
        assertFalse(seven == a.get(this));
        assertTrue(a.compareAndSet(this,m4,seven));
        assertSame(seven,a.get(this));
    }

    /**
     * compareAndSet in one thread enables another waiting for value
     * to succeed
     */
    public void testCompareAndSetInMultipleThreads() throws Exception {
        x = one;
        final AtomicReferenceFieldUpdater<AtomicReferenceFieldUpdaterTest, Integer>a;
        try {
            a = AtomicReferenceFieldUpdater.newUpdater(AtomicReferenceFieldUpdaterTest.class, Integer.class, "x");
        } catch (RuntimeException ok) {
            return;
        }

        Thread t = new Thread(new CheckedRunnable() {
            public void realRun() {
                while (!a.compareAndSet(AtomicReferenceFieldUpdaterTest.this, two, three))
                    Thread.yield();
            }});

        t.start();
        assertTrue(a.compareAndSet(this, one, two));
        t.join(LONG_DELAY_MS);
        assertFalse(t.isAlive());
        assertSame(a.get(this), three);
    }

    /**
     * repeated weakCompareAndSet succeeds in changing value when equal
     * to expected
     */
    public void testWeakCompareAndSet() {
        AtomicReferenceFieldUpdater<AtomicReferenceFieldUpdaterTest, Integer>a;
        try {
            a = AtomicReferenceFieldUpdater.newUpdater(AtomicReferenceFieldUpdaterTest.class, Integer.class, "x");
        } catch (RuntimeException ok) {
            return;
        }
        x = one;
        while (!a.weakCompareAndSet(this,one,two));
        while (!a.weakCompareAndSet(this,two,m4));
        assertSame(m4,a.get(this));
        while (!a.weakCompareAndSet(this,m4,seven));
        assertSame(seven,a.get(this));
    }

    /**
     * getAndSet returns previous value and sets to given value
     */
    public void testGetAndSet() {
        AtomicReferenceFieldUpdater<AtomicReferenceFieldUpdaterTest, Integer>a;
        try {
            a = AtomicReferenceFieldUpdater.newUpdater(AtomicReferenceFieldUpdaterTest.class, Integer.class, "x");
        } catch (RuntimeException ok) {
            return;
        }
        x = one;
        assertSame(one,a.getAndSet(this, zero));
        assertSame(zero,a.getAndSet(this,m10));
        assertSame(m10,a.getAndSet(this,1));
    }

}
