/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/licenses/publicdomain
 */

package java.util.concurrent.atomic;
import sun.misc.Unsafe;

/**
 * An object reference that may be updated atomically. See the {@link
 * java.util.concurrent.atomic} package specification for description
 * of the properties of atomic variables.
 * @since 1.5
 * @author Doug Lea
 * @param <V> The type of object referred to by this reference
 */
public class AtomicReference<V>  implements java.io.Serializable { 
    private static final long serialVersionUID = -1848883965231344442L;

    // BEGIN android-changed
    private static final Unsafe unsafe = UnsafeAccess.THE_ONE;
    // END android-changed
    private static final long valueOffset;

    static {
      try {
        valueOffset = unsafe.objectFieldOffset
            (AtomicReference.class.getDeclaredField("value"));
      } catch(Exception ex) { throw new Error(ex); }
    }

    private volatile V value;

    /**
     * Create a new AtomicReference with the given initial value.
     *
     * @param initialValue the initial value
     */
    public AtomicReference(V initialValue) {
        value = initialValue;
    }

    /**
     * Create a new AtomicReference with null initial value.
     */
    public AtomicReference() {
    }
  
    /**
     * Get the current value.
     *
     * @return the current value
     */
    public final V get() {
        return value;
    }
  
    /**
     * Set to the given value.
     *
     * @param newValue the new value
     */
    public final void set(V newValue) {
        value = newValue;
    }
  
    /**
     * Atomically set the value to the given updated value
     * if the current value <tt>==</tt> the expected value.
     * @param expect the expected value
     * @param update the new value
     * @return true if successful. False return indicates that
     * the actual value was not equal to the expected value.
     */
    public final boolean compareAndSet(V expect, V update) {
      return unsafe.compareAndSwapObject(this, valueOffset, expect, update);
    }

    /**
     * Atomically set the value to the given updated value
     * if the current value <tt>==</tt> the expected value.
     * May fail spuriously.
     * @param expect the expected value
     * @param update the new value
     * @return true if successful.
     */
    public final boolean weakCompareAndSet(V expect, V update) {
      return unsafe.compareAndSwapObject(this, valueOffset, expect, update);
    }

    /**
     * Set to the given value and return the old value.
     *
     * @param newValue the new value
     * @return the previous value
     */
    public final V getAndSet(V newValue) {
        while (true) {
            V x = get();
            if (compareAndSet(x, newValue))
                return x;
        }
    }

    /**
     * Returns the String representation of the current value.
     * @return the String representation of the current value.
     */
    public String toString() {
        return String.valueOf(get());
    }

}
