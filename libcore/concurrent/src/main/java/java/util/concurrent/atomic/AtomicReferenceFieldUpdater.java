/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/licenses/publicdomain
 */

package java.util.concurrent.atomic;
import sun.misc.Unsafe;
import java.lang.reflect.*;

/**
 * A reflection-based utility that enables atomic updates to
 * designated <tt>volatile</tt> reference fields of designated
 * classes.  This class is designed for use in atomic data structures
 * in which several reference fields of the same node are
 * independently subject to atomic updates. For example, a tree node
 * might be declared as
 *
 * <pre>
 * class Node {
 *   private volatile Node left, right;
 *
 *   private static final AtomicReferenceFieldUpdater&lt;Node, Node&gt; leftUpdater =
 *     AtomicReferenceFieldUpdater.newUpdater(Node.class, Node.class, "left");
 *   private static AtomicReferenceFieldUpdater&lt;Node, Node&gt; rightUpdater =
 *     AtomicReferenceFieldUpdater.newUpdater(Node.class, Node.class, "right");
 *
 *   Node getLeft() { return left;  }
 *   boolean compareAndSetLeft(Node expect, Node update) {
 *     return leftUpdater.compareAndSet(this, expect, update);
 *   }
 *   // ... and so on
 * }
 * </pre>
 *
 * <p> Note that the guarantees of the <tt>compareAndSet</tt>
 * method in this class are weaker than in other atomic classes. Because this
 * class cannot ensure that all uses of the field are appropriate for
 * purposes of atomic access, it can guarantee atomicity and volatile
 * semantics only with respect to other invocations of
 * <tt>compareAndSet</tt> and <tt>set</tt>.
 * @since 1.5
 * @author Doug Lea
 * @param <T> The type of the object holding the updatable field
 * @param <V> The type of the field
 */
public abstract class AtomicReferenceFieldUpdater<T, V>  {

    /**
     * Creates an updater for objects with the given field.  The Class
     * arguments are needed to check that reflective types and generic
     * types match.
     * @param tclass the class of the objects holding the field.
     * @param vclass the class of the field
     * @param fieldName the name of the field to be updated.
     * @return the updater
     * @throws IllegalArgumentException if the field is not a volatile reference type.
     * @throws RuntimeException with a nested reflection-based
     * exception if the class does not hold field or is the wrong type.
     */
    public static <U, W> AtomicReferenceFieldUpdater<U,W> newUpdater(Class<U> tclass, Class<W> vclass, String fieldName) {
        // Currently rely on standard intrinsics implementation
        return new AtomicReferenceFieldUpdaterImpl<U,W>(tclass, 
                                                        vclass, 
                                                        fieldName);
    }

    /**
     * Protected do-nothing constructor for use by subclasses.
     */
    protected AtomicReferenceFieldUpdater() {
    }

    /**
     * Atomically set the value of the field of the given object managed
     * by this Updater to the given updated value if the current value
     * <tt>==</tt> the expected value. This method is guaranteed to be
     * atomic with respect to other calls to <tt>compareAndSet</tt> and
     * <tt>set</tt>, but not necessarily with respect to other
     * changes in the field.
     * @param obj An object whose field to conditionally set
     * @param expect the expected value
     * @param update the new value
     * @return true if successful.
     */

    public abstract boolean compareAndSet(T obj, V expect, V update);

    /**
     * Atomically set the value of the field of the given object managed
     * by this Updater to the given updated value if the current value
     * <tt>==</tt> the expected value. This method is guaranteed to be
     * atomic with respect to other calls to <tt>compareAndSet</tt> and
     * <tt>set</tt>, but not necessarily with respect to other
     * changes in the field, and may fail spuriously.
     * @param obj An object whose field to conditionally set
     * @param expect the expected value
     * @param update the new value
     * @return true if successful.
     */
    public abstract boolean weakCompareAndSet(T obj, V expect, V update);

    /**
     * Set the field of the given object managed by this updater. This
     * operation is guaranteed to act as a volatile store with respect
     * to subsequent invocations of <tt>compareAndSet</tt>.
     * @param obj An object whose field to set
     * @param newValue the new value
     */
    public abstract void set(T obj, V newValue);

    /**
     * Get the current value held in the field by the given object.
     * @param obj An object whose field to get
     * @return the current value
     */
    public abstract V get(T obj);

    /**
     * Set to the given value and return the old value.
     *
     * @param obj An object whose field to get and set
     * @param newValue the new value
     * @return the previous value
     */
    public V getAndSet(T obj, V newValue) {
        for (;;) {
            V current = get(obj);
            if (compareAndSet(obj, current, newValue))
                return current;
        }
    }

    /**
     * Standard hotspot implementation using intrinsics
     */
    private static class AtomicReferenceFieldUpdaterImpl<T,V> extends AtomicReferenceFieldUpdater<T,V> {
        // BEGIN android-changed
        private static final Unsafe unsafe = UnsafeAccess.THE_ONE;
        // END android-changed
        private final long offset;
        private final Class<T> tclass;
        private final Class<V> vclass;

        AtomicReferenceFieldUpdaterImpl(Class<T> tclass, Class<V> vclass, String fieldName) {
            Field field = null;
            Class fieldClass = null;
            try {
                field = tclass.getDeclaredField(fieldName);
                fieldClass = field.getType();
            } catch(Exception ex) {
                throw new RuntimeException(ex);
            }
            
            if (vclass != fieldClass)
                throw new ClassCastException();
            
            if (!Modifier.isVolatile(field.getModifiers()))
                throw new IllegalArgumentException("Must be volatile type");

            this.tclass = tclass;
            this.vclass = vclass;
            offset = unsafe.objectFieldOffset(field);
        }
        

        public boolean compareAndSet(T obj, V expect, V update) {
            if (!tclass.isInstance(obj) ||
                (update != null && !vclass.isInstance(update)))
                throw new ClassCastException();
            return unsafe.compareAndSwapObject(obj, offset, expect, update);
        }

        public boolean weakCompareAndSet(T obj, V expect, V update) {
            // same implementation as strong form for now
            if (!tclass.isInstance(obj) ||
                (update != null && !vclass.isInstance(update)))
                throw new ClassCastException();
            return unsafe.compareAndSwapObject(obj, offset, expect, update);
        }


        public void set(T obj, V newValue) {
            if (!tclass.isInstance(obj) ||
                (newValue != null && !vclass.isInstance(newValue)))
                throw new ClassCastException();
            unsafe.putObjectVolatile(obj, offset, newValue);
        }

        public V get(T obj) {
            if (!tclass.isInstance(obj))
                throw new ClassCastException();
            return (V)unsafe.getObjectVolatile(obj, offset);
        }
    }
}

