/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/licenses/publicdomain
 */

package java.util.concurrent.atomic;
import sun.misc.Unsafe;
import java.util.*;

/**
 * A <tt>long</tt> array in which elements may be updated atomically.
 * See the {@link java.util.concurrent.atomic} package specification
 * for description of the properties of atomic variables.
 * @since 1.5
 * @author Doug Lea
 */
public class AtomicLongArray implements java.io.Serializable { 
    private static final long serialVersionUID = -2308431214976778248L;

    // setup to use Unsafe.compareAndSwapInt for updates
    // BEGIN android-changed
    private static final Unsafe unsafe = UnsafeAccess.THE_ONE;
    // END android-changed
    private static final int base = unsafe.arrayBaseOffset(long[].class);
    private static final int scale = unsafe.arrayIndexScale(long[].class);
    private final long[] array;

    private long rawIndex(int i) {
        if (i < 0 || i >= array.length)
            throw new IndexOutOfBoundsException("index " + i);
        return base + i * scale;
    }

    /**
     * Create a new AtomicLongArray of given length.
     * @param length the length of the array
     */
    public AtomicLongArray(int length) {
        array = new long[length];
        // must perform at least one volatile write to conform to JMM
        if (length > 0) 
            unsafe.putLongVolatile(array, rawIndex(0), 0);
    }

    /**
     * Create a new AtomicLongArray with the same length as, and
     * all elements copied from, the given array.
     *
     * @param array the array to copy elements from
     * @throws NullPointerException if array is null
     */
    public AtomicLongArray(long[] array) {
        if (array == null) 
            throw new NullPointerException();
        int length = array.length;
        this.array = new long[length];
        if (length > 0) {
            int last = length-1;
            for (int i = 0; i < last; ++i)
                this.array[i] = array[i];
            // Do the last write as volatile
            unsafe.putLongVolatile(this.array, rawIndex(last), array[last]);
        }
    }

    /**
     * Returns the length of the array.
     *
     * @return the length of the array
     */
    public final int length() {
        return array.length;
    }

    /**
     * Get the current value at position <tt>i</tt>.
     *
     * @param i the index
     * @return the current value
     */
    public final long get(int i) {
        return unsafe.getLongVolatile(array, rawIndex(i));
    }
 
    /**
     * Set the element at position <tt>i</tt> to the given value.
     *
     * @param i the index
     * @param newValue the new value
     */
    public final void set(int i, long newValue) {
        unsafe.putLongVolatile(array, rawIndex(i), newValue);
    }
  
    /**
     * Set the element at position <tt>i</tt> to the given value and return the
     * old value.
     *
     * @param i the index
     * @param newValue the new value
     * @return the previous value
     */
    public final long getAndSet(int i, long newValue) {
        while (true) {
            long current = get(i);
            if (compareAndSet(i, current, newValue))
                return current;
        }
    }
  
    /**
     * Atomically set the value to the given updated value
     * if the current value <tt>==</tt> the expected value.
     * @param i the index
     * @param expect the expected value
     * @param update the new value
     * @return true if successful. False return indicates that
     * the actual value was not equal to the expected value.
     */
    public final boolean compareAndSet(int i, long expect, long update) {
        return unsafe.compareAndSwapLong(array, rawIndex(i), 
                                         expect, update);
    }

    /**
     * Atomically set the value to the given updated value
     * if the current value <tt>==</tt> the expected value.
     * May fail spuriously.
     * @param i the index
     * @param expect the expected value
     * @param update the new value
     * @return true if successful.
     */
    public final boolean weakCompareAndSet(int i, long expect, long update) {
        return compareAndSet(i, expect, update);
    }

    /**
     * Atomically increment by one the element at index <tt>i</tt>.
     *
     * @param i the index
     * @return the previous value;
     */
    public final long getAndIncrement(int i) {
        while (true) {
            long current = get(i);
            long next = current + 1;
            if (compareAndSet(i, current, next))
                return current;
        }
    }
  
    /**
     * Atomically decrement by one the element at index <tt>i</tt>.
     *
     * @param i the index
     * @return the previous value;
     */
    public final long getAndDecrement(int i) {
        while (true) {
            long current = get(i);
            long next = current - 1;
            if (compareAndSet(i, current, next))
                return current;
        }
    }
  
    /**
     * Atomically add the given value to element at index <tt>i</tt>.
     *
     * @param i the index
     * @param delta the value to add
     * @return the previous value;
     */
    public final long getAndAdd(int i, long delta) {
        while (true) {
            long current = get(i);
            long next = current + delta;
            if (compareAndSet(i, current, next))
                return current;
        }
    }
  

    /**
     * Atomically increment the element at index <tt>i</tt>.
     *
     * @param i the index
     * @return the updated value;
     */
    public final long incrementAndGet(int i) {
        while (true) {
            long current = get(i);
            long next = current + 1;
            if (compareAndSet(i, current, next))
                return next;
        }
    }
  
    /**
     * Atomically decrement the element at index <tt>i</tt>.
     *
     * @param i the index
     * @return the updated value;
     */
    public final long decrementAndGet(int i) {
        while (true) {
            long current = get(i);
            long next = current - 1;
            if (compareAndSet(i, current, next))
                return next;
        }
    }
  
    /**
     * Atomically add the given value to element at index <tt>i</tt>.
     *
     * @param i the index
     * @param delta the value to add
     * @return the updated value;
     */
    public long addAndGet(int i, long delta) {
        while (true) {
            long current = get(i);
            long next = current + delta;
            if (compareAndSet(i, current, next))
                return next;
        }
    }

    /**
     * Returns the String representation of the current values of array.
     * @return the String representation of the current values of array.
     */
    public String toString() {
        if (array.length > 0) // force volatile read
            get(0);
        return Arrays.toString(array);
    }
  
}
