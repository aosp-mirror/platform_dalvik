/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/licenses/publicdomain
 */

package java.util.concurrent;
import java.util.concurrent.locks.*;
import java.util.*;

// BEGIN android-note
// removed link to collections framework docs
// END android-note

/**
 * A {@linkplain BlockingQueue blocking queue} in which each
 * <tt>put</tt> must wait for a <tt>take</tt>, and vice versa.  A
 * synchronous queue does not have any internal capacity, not even a
 * capacity of one. You cannot <tt>peek</tt> at a synchronous queue
 * because an element is only present when you try to take it; you
 * cannot add an element (using any method) unless another thread is
 * trying to remove it; you cannot iterate as there is nothing to
 * iterate.  The <em>head</em> of the queue is the element that the
 * first queued thread is trying to add to the queue; if there are no
 * queued threads then no element is being added and the head is
 * <tt>null</tt>.  For purposes of other <tt>Collection</tt> methods
 * (for example <tt>contains</tt>), a <tt>SynchronousQueue</tt> acts
 * as an empty collection.  This queue does not permit <tt>null</tt>
 * elements.
 *
 * <p>Synchronous queues are similar to rendezvous channels used in
 * CSP and Ada. They are well suited for handoff designs, in which an
 * object running in one thread must sync up with an object running
 * in another thread in order to hand it some information, event, or
 * task.
 *
 * <p> This class supports an optional fairness policy for ordering
 * waiting producer and consumer threads.  By default, this ordering
 * is not guaranteed. However, a queue constructed with fairness set
 * to <tt>true</tt> grants threads access in FIFO order. Fairness
 * generally decreases throughput but reduces variability and avoids
 * starvation.
 *
 * <p>This class implements all of the <em>optional</em> methods
 * of the {@link Collection} and {@link Iterator} interfaces.
 *
 * @since 1.5
 * @author Doug Lea
 * @param <E> the type of elements held in this collection
 */
public class SynchronousQueue<E> extends AbstractQueue<E>
        implements BlockingQueue<E>, java.io.Serializable {
    private static final long serialVersionUID = -3223113410248163686L;

    /*
      This implementation divides actions into two cases for puts:

      * An arriving producer that does not already have a waiting consumer
      creates a node holding item, and then waits for a consumer to take it.
      * An arriving producer that does already have a waiting consumer fills
      the slot node created by the consumer, and notifies it to continue.

      And symmetrically, two for takes:

      * An arriving consumer that does not already have a waiting producer
      creates an empty slot node, and then waits for a producer to fill it.
      * An arriving consumer that does already have a waiting producer takes
      item from the node created by the producer, and notifies it to continue.

      When a put or take waiting for the actions of its counterpart
      aborts due to interruption or timeout, it marks the node
      it created as "CANCELLED", which causes its counterpart to retry
      the entire put or take sequence.

      This requires keeping two simple queues, waitingProducers and
      waitingConsumers. Each of these can be FIFO (preserves fairness)
      or LIFO (improves throughput).
    */

    /** Lock protecting both wait queues */
    private final ReentrantLock qlock;
    /** Queue holding waiting puts */
    private final WaitQueue waitingProducers;
    /** Queue holding waiting takes */
    private final WaitQueue waitingConsumers;

    /**
     * Creates a <tt>SynchronousQueue</tt> with nonfair access policy.
     */
    public SynchronousQueue() {
        this(false);
    }

    /**
     * Creates a <tt>SynchronousQueue</tt> with specified fairness policy.
     * @param fair if true, threads contend in FIFO order for access;
     * otherwise the order is unspecified.
     */
    public SynchronousQueue(boolean fair) {
        if (fair) {
            qlock = new ReentrantLock(true);
            waitingProducers = new FifoWaitQueue();
            waitingConsumers = new FifoWaitQueue();
        }
        else {
            qlock = new ReentrantLock();
            waitingProducers = new LifoWaitQueue();
            waitingConsumers = new LifoWaitQueue();
        }
    }

    /**
     * Queue to hold waiting puts/takes; specialized to Fifo/Lifo below.
     * These queues have all transient fields, but are serializable
     * in order to recover fairness settings when deserialized.
     */
    static abstract class WaitQueue implements java.io.Serializable {
        /** Create, add, and return node for x */
        abstract Node enq(Object x);
        /** Remove and return node, or null if empty */
        abstract Node deq();
    }

    /**
     * FIFO queue to hold waiting puts/takes.
     */
    static final class FifoWaitQueue extends WaitQueue implements java.io.Serializable {
        private static final long serialVersionUID = -3623113410248163686L;
        private transient Node head;
        private transient Node last;

        Node enq(Object x) {
            Node p = new Node(x);
            if (last == null)
                last = head = p;
            else
                last = last.next = p;
            return p;
        }

        Node deq() {
            Node p = head;
            if (p != null) {
                if ((head = p.next) == null)
                    last = null;
                p.next = null;
            }
            return p;
        }
    }

    /**
     * LIFO queue to hold waiting puts/takes.
     */
    static final class LifoWaitQueue extends WaitQueue implements java.io.Serializable {
        private static final long serialVersionUID = -3633113410248163686L;
        private transient Node head;

        Node enq(Object x) {
            return head = new Node(x, head);
        }

        Node deq() {
            Node p = head;
            if (p != null) {
                head = p.next;
                p.next = null;
            }
            return p;
        }
    }

    /**
     * Nodes each maintain an item and handle waits and signals for
     * getting and setting it. The class extends
     * AbstractQueuedSynchronizer to manage blocking, using AQS state
     *  0 for waiting, 1 for ack, -1 for cancelled.
     */
    static final class Node extends AbstractQueuedSynchronizer {
        /** Synchronization state value representing that node acked */
        private static final int ACK    =  1;
        /** Synchronization state value representing that node cancelled */
        private static final int CANCEL = -1;

        /** The item being transferred */
        Object item;
        /** Next node in wait queue */
        Node next;

        /** Creates a node with initial item */
        Node(Object x) { item = x; }

        /** Creates a node with initial item and next */
        Node(Object x, Node n) { item = x; next = n; }

        /**
         * Implements AQS base acquire to succeed if not in WAITING state
         */
        protected boolean tryAcquire(int ignore) {
            return getState() != 0;
        }

        /**
         * Implements AQS base release to signal if state changed
         */
        protected boolean tryRelease(int newState) {
            return compareAndSetState(0, newState);
        }

        /**
         * Takes item and nulls out field (for sake of GC)
         */
        private Object extract() {
            Object x = item;
            item = null;
            return x;
        }

        /**
         * Tries to cancel on interrupt; if so rethrowing,
         * else setting interrupt state
         */
        private void checkCancellationOnInterrupt(InterruptedException ie) 
            throws InterruptedException {
            if (release(CANCEL)) 
                throw ie;
            Thread.currentThread().interrupt();
        }

        /**
         * Fills in the slot created by the consumer and signal consumer to
         * continue.
         */
        boolean setItem(Object x) {
            item = x; // can place in slot even if cancelled
            return release(ACK);
        }

        /**
         * Removes item from slot created by producer and signal producer
         * to continue.
         */
        Object getItem() {
            return (release(ACK))? extract() : null;
        }

        /**
         * Waits for a consumer to take item placed by producer.
         */
        void waitForTake() throws InterruptedException {
            try {
                acquireInterruptibly(0);
            } catch (InterruptedException ie) {
                checkCancellationOnInterrupt(ie);
            }
        }

        /**
         * Waits for a producer to put item placed by consumer.
         */
        Object waitForPut() throws InterruptedException {
            try {
                acquireInterruptibly(0);
            } catch (InterruptedException ie) {
                checkCancellationOnInterrupt(ie);
            }
            return extract();
        }

        /**
         * Waits for a consumer to take item placed by producer or time out.
         */
        boolean waitForTake(long nanos) throws InterruptedException {
            try {
                if (!tryAcquireNanos(0, nanos) &&
                    release(CANCEL))
                    return false;
            } catch (InterruptedException ie) {
                checkCancellationOnInterrupt(ie);
            }
            return true;
        }

        /**
         * Waits for a producer to put item placed by consumer, or time out.
         */
        Object waitForPut(long nanos) throws InterruptedException {
            try {
                if (!tryAcquireNanos(0, nanos) &&
                    release(CANCEL))
                    return null;
            } catch (InterruptedException ie) {
                checkCancellationOnInterrupt(ie);
            }
            return extract();
        }
    }

    /**
     * Adds the specified element to this queue, waiting if necessary for
     * another thread to receive it.
     * @param o the element to add
     * @throws InterruptedException if interrupted while waiting.
     * @throws NullPointerException if the specified element is <tt>null</tt>.
     */
    public void put(E o) throws InterruptedException {
        if (o == null) throw new NullPointerException();
        final ReentrantLock qlock = this.qlock;

        for (;;) {
            Node node;
            boolean mustWait;
            if (Thread.interrupted()) throw new InterruptedException();
            qlock.lock();
            try {
                node = waitingConsumers.deq();
                if ( (mustWait = (node == null)) )
                    node = waitingProducers.enq(o);
            } finally {
                qlock.unlock();
            }

            if (mustWait) {
                node.waitForTake();
                return;
            }

            else if (node.setItem(o))
                return;

            // else consumer cancelled, so retry
        }
    }

    /**
     * Inserts the specified element into this queue, waiting if necessary
     * up to the specified wait time for another thread to receive it.
     * @param o the element to add
     * @param timeout how long to wait before giving up, in units of
     * <tt>unit</tt>
     * @param unit a <tt>TimeUnit</tt> determining how to interpret the
     * <tt>timeout</tt> parameter
     * @return <tt>true</tt> if successful, or <tt>false</tt> if
     * the specified waiting time elapses before a consumer appears.
     * @throws InterruptedException if interrupted while waiting.
     * @throws NullPointerException if the specified element is <tt>null</tt>.
     */
    public boolean offer(E o, long timeout, TimeUnit unit) throws InterruptedException {
        if (o == null) throw new NullPointerException();
        long nanos = unit.toNanos(timeout);
        final ReentrantLock qlock = this.qlock;
        for (;;) {
            Node node;
            boolean mustWait;
            if (Thread.interrupted()) throw new InterruptedException();
            qlock.lock();
            try {
                node = waitingConsumers.deq();
                if ( (mustWait = (node == null)) )
                    node = waitingProducers.enq(o);
            } finally {
                qlock.unlock();
            }

            if (mustWait) 
                return node.waitForTake(nanos);

            else if (node.setItem(o))
                return true;

            // else consumer cancelled, so retry
        }
    }

    /**
     * Retrieves and removes the head of this queue, waiting if necessary
     * for another thread to insert it.
     * @throws InterruptedException if interrupted while waiting.
     * @return the head of this queue
     */
    public E take() throws InterruptedException {
        final ReentrantLock qlock = this.qlock;
        for (;;) {
            Node node;
            boolean mustWait;

            if (Thread.interrupted()) throw new InterruptedException();
            qlock.lock();
            try {
                node = waitingProducers.deq();
                if ( (mustWait = (node == null)) )
                    node = waitingConsumers.enq(null);
            } finally {
                qlock.unlock();
            }

            if (mustWait) {
                Object x = node.waitForPut();
                return (E)x;
            }
            else {
                Object x = node.getItem();
                if (x != null)
                    return (E)x;
                // else cancelled, so retry
            }
        }
    }

    /**
     * Retrieves and removes the head of this queue, waiting
     * if necessary up to the specified wait time, for another thread
     * to insert it.
     * @param timeout how long to wait before giving up, in units of
     * <tt>unit</tt>
     * @param unit a <tt>TimeUnit</tt> determining how to interpret the
     * <tt>timeout</tt> parameter
     * @return the head of this queue, or <tt>null</tt> if the
     * specified waiting time elapses before an element is present.
     * @throws InterruptedException if interrupted while waiting.
     */
    public E poll(long timeout, TimeUnit unit) throws InterruptedException {
        long nanos = unit.toNanos(timeout);
        final ReentrantLock qlock = this.qlock;

        for (;;) {
            Node node;
            boolean mustWait;

            if (Thread.interrupted()) throw new InterruptedException();
            qlock.lock();
            try {
                node = waitingProducers.deq();
                if ( (mustWait = (node == null)) )
                    node = waitingConsumers.enq(null);
            } finally {
                qlock.unlock();
            }

            if (mustWait) {
                Object x = node.waitForPut(nanos);
                return (E)x;
            }
            else {
                Object x = node.getItem();
                if (x != null)
                    return (E)x;
                // else cancelled, so retry
            }
        }
    }

    // Untimed nonblocking versions

   /**
    * Inserts the specified element into this queue, if another thread is
    * waiting to receive it.
    *
    * @param o the element to add.
    * @return <tt>true</tt> if it was possible to add the element to
    *         this queue, else <tt>false</tt>
    * @throws NullPointerException if the specified element is <tt>null</tt>
    */
    public boolean offer(E o) {
        if (o == null) throw new NullPointerException();
        final ReentrantLock qlock = this.qlock;

        for (;;) {
            Node node;
            qlock.lock();
            try {
                node = waitingConsumers.deq();
            } finally {
                qlock.unlock();
            }
            if (node == null)
                return false;

            else if (node.setItem(o))
                return true;
            // else retry
        }
    }

    /**
     * Retrieves and removes the head of this queue, if another thread
     * is currently making an element available.
     *
     * @return the head of this queue, or <tt>null</tt> if no
     *         element is available.
     */
    public E poll() {
        final ReentrantLock qlock = this.qlock;
        for (;;) {
            Node node;
            qlock.lock();
            try {
                node = waitingProducers.deq();
            } finally {
                qlock.unlock();
            }
            if (node == null)
                return null;

            else {
                Object x = node.getItem();
                if (x != null)
                    return (E)x;
                // else retry
            }
        }
    }

    /**
     * Always returns <tt>true</tt>. 
     * A <tt>SynchronousQueue</tt> has no internal capacity.
     * @return <tt>true</tt>
     */
    public boolean isEmpty() {
        return true;
    }

    /**
     * Always returns zero.
     * A <tt>SynchronousQueue</tt> has no internal capacity.
     * @return zero.
     */
    public int size() {
        return 0;
    }

    /**
     * Always returns zero.
     * A <tt>SynchronousQueue</tt> has no internal capacity.
     * @return zero.
     */
    public int remainingCapacity() {
        return 0;
    }

    /**
     * Does nothing.
     * A <tt>SynchronousQueue</tt> has no internal capacity.
     */
    public void clear() {}

    /**
     * Always returns <tt>false</tt>.
     * A <tt>SynchronousQueue</tt> has no internal capacity.
     * @param o the element
     * @return <tt>false</tt>
     */
    public boolean contains(Object o) {
        return false;
    }

    /**
     * Always returns <tt>false</tt>.
     * A <tt>SynchronousQueue</tt> has no internal capacity.
     *
     * @param o the element to remove
     * @return <tt>false</tt>
     */
    public boolean remove(Object o) {
        return false;
    }

    /**
     * Returns <tt>false</tt> unless given collection is empty.
     * A <tt>SynchronousQueue</tt> has no internal capacity.
     * @param c the collection
     * @return <tt>false</tt> unless given collection is empty
     */
    public boolean containsAll(Collection<?> c) {
        return c.isEmpty();
    }

    /**
     * Always returns <tt>false</tt>.
     * A <tt>SynchronousQueue</tt> has no internal capacity.
     * @param c the collection
     * @return <tt>false</tt>
     */
    public boolean removeAll(Collection<?> c) {
        return false;
    }

    /**
     * Always returns <tt>false</tt>.
     * A <tt>SynchronousQueue</tt> has no internal capacity.
     * @param c the collection
     * @return <tt>false</tt>
     */
    public boolean retainAll(Collection<?> c) {
        return false;
    }

    /**
     * Always returns <tt>null</tt>. 
     * A <tt>SynchronousQueue</tt> does not return elements
     * unless actively waited on.
     * @return <tt>null</tt>
     */
    public E peek() {
        return null;
    }


    static class EmptyIterator<E> implements Iterator<E> {
        public boolean hasNext() {
            return false;
        }
        public E next() {
            throw new NoSuchElementException();
        }
        public void remove() {
            throw new IllegalStateException();
        }
    }

    /**
     * Returns an empty iterator in which <tt>hasNext</tt> always returns
     * <tt>false</tt>.
     *
     * @return an empty iterator
     */
    public Iterator<E> iterator() {
        return new EmptyIterator<E>();
    }


    /**
     * Returns a zero-length array.
     * @return a zero-length array
     */
    public Object[] toArray() {
        return new Object[0];
    }

    /**
     * Sets the zeroeth element of the specified array to <tt>null</tt>
     * (if the array has non-zero length) and returns it.
     * @param a the array
     * @return the specified array
     */
    public <T> T[] toArray(T[] a) {
        if (a.length > 0)
            a[0] = null;
        return a;
    }


    public int drainTo(Collection<? super E> c) {
        if (c == null)
            throw new NullPointerException();
        if (c == this)
            throw new IllegalArgumentException();
        int n = 0;
        E e;
        while ( (e = poll()) != null) {
            c.add(e);
            ++n;
        }
        return n;
    }

    public int drainTo(Collection<? super E> c, int maxElements) {
        if (c == null)
            throw new NullPointerException();
        if (c == this)
            throw new IllegalArgumentException();
        int n = 0;
        E e;
        while (n < maxElements && (e = poll()) != null) {
            c.add(e);
            ++n;
        }
        return n;
    }
}





