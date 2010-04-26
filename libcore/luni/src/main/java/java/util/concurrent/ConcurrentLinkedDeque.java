/*
 * Written by Doug Lea and Martin Buchholz with assistance from members of
 * JCP JSR-166 Expert Group and released to the public domain, as explained
 * at http://creativecommons.org/licenses/publicdomain
 */

package java.util.concurrent;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.ConcurrentModificationException;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A concurrent linked-list implementation of a {@link Deque}
 * (double-ended queue).  Concurrent insertion, removal, and access
 * operations execute safely across multiple threads. Iterators are
 * <i>weakly consistent</i>, returning elements reflecting the state
 * of the deque at some point at or since the creation of the
 * iterator.  They do <em>not</em> throw {@link
 * ConcurrentModificationException}, and may proceed concurrently with
 * other operations.
 *
 * <p>This class and its iterators implement all of the
 * <em>optional</em> methods of the {@link Collection} and {@link
 * Iterator} interfaces. Like most other concurrent collection
 * implementations, this class does not permit the use of
 * {@code null} elements.  because some null arguments and return
 * values cannot be reliably distinguished from the absence of
 * elements. Arbitrarily, the {@link Collection#remove} method is
 * mapped to {@code removeFirstOccurrence}, and {@link
 * Collection#add} is mapped to {@code addLast}.
 *
 * <p>Beware that, unlike in most collections, the {@code size}
 * method is <em>NOT</em> a constant-time operation. Because of the
 * asynchronous nature of these deques, determining the current number
 * of elements requires a traversal of the elements.
 *
 * <p>This class is {@code Serializable}, but relies on default
 * serialization mechanisms.  Usually, it is a better idea for any
 * serializable class using a {@code ConcurrentLinkedDeque} to instead
 * serialize a snapshot of the elements obtained by method
 * {@code toArray}.
 *
 * @author  Doug Lea
 * @author  Martin Buchholz
 * @param <E> the type of elements held in this collection
 */

public class ConcurrentLinkedDeque<E>
    extends AbstractCollection<E>
    implements Deque<E>, java.io.Serializable {

    /*
     * This is an implementation of a concurrent lock-free deque
     * supporting interior removes but not interior insertions, as
     * required to fully support the Deque interface.
     *
     * We extend the techniques developed for
     * ConcurrentLinkedQueue and LinkedTransferQueue
     * (see the internal docs for those classes).
     *
     * At any time, there is precisely one "first" active node with a
     * null prev pointer.  Similarly there is one "last" active node
     * with a null next pointer.  New nodes are simply enqueued by
     * null-CASing.
     *
     * A node p is considered "active" if it either contains an
     * element, or is an end node and neither next nor prev pointers
     * are self-links:
     *
     * p.item != null ||
     * (p.prev == null && p.next != p) ||
     * (p.next == null && p.prev != p)
     *
     * The head and tail pointers are only approximations to the start
     * and end of the deque.  The first node can always be found by
     * following prev pointers from head; likewise for tail.  However,
     * head and tail may be pointing at deleted nodes that have been
     * unlinked and so may not be reachable from any live node.
     *
     * There are 3 levels of node deletion:
     * - logical deletion atomically removes the element
     * - "unlinking" makes a deleted node unreachable from active
     *   nodes, and thus eventually reclaimable by GC
     * - "gc-unlinking" further does the reverse of making active
     *   nodes unreachable from deleted nodes, making it easier for
     *   the GC to reclaim future deleted nodes
     *
     * TODO: find a better name for "gc-unlinked"
     *
     * Logical deletion of a node simply involves CASing its element
     * to null.  Physical deletion is merely an optimization (albeit a
     * critical one), and can be performed at our convenience.  At any
     * time, the set of non-logically-deleted nodes maintained by prev
     * and next links are identical, that is the live elements found
     * via next links from the first node is equal to the elements
     * found via prev links from the last node.  However, this is not
     * true for nodes that have already been logically deleted - such
     * nodes may only be reachable in one direction.
     *
     * When a node is dequeued at either end, e.g. via poll(), we
     * would like to break any references from the node to live nodes,
     * to stop old garbage from causing retention of new garbage with
     * a generational or conservative GC.  We develop further the
     * self-linking trick that was very effective in other concurrent
     * collection classes.  The idea is to replace prev and next
     * pointers to active nodes with special values that are
     * interpreted to mean off-the-list-at-one-end.  These are
     * approximations, but good enough to preserve the properties we
     * want in our traversals, e.g. we guarantee that a traversal will
     * never hit the same element twice, but we don't guarantee
     * whether a traversal that runs out of elements will be able to
     * see more elements later after more elements are added at that
     * end.  Doing gc-unlinking safely is particularly tricky, since
     * any node can be in use indefinitely (for example by an
     * iterator).  We must make sure that the nodes pointed at by
     * head/tail do not get gc-unlinked, since head/tail are needed to
     * get "back on track" by other nodes that are gc-unlinked.
     * gc-unlinking accounts for much of the implementation complexity.
     *
     * Since neither unlinking nor gc-unlinking are necessary for
     * correctness, there are many implementation choices regarding
     * frequency (eagerness) of these operations.  Since volatile
     * reads are likely to be much cheaper than CASes, saving CASes by
     * unlinking multiple adjacent nodes at a time may be a win.
     * gc-unlinking can be performed rarely and still be effective,
     * since it is most important that long chains of deleted nodes
     * are occasionally broken.
     *
     * The actual representation we use is that p.next == p means to
     * goto the first node, and p.next == null && p.prev == p means
     * that the iteration is at an end and that p is a (final static)
     * dummy node, NEXT_TERMINATOR, and not the last active node.
     * Finishing the iteration when encountering such a TERMINATOR is
     * good enough for read-only traversals.  When the last active
     * node is desired, for example when enqueueing, goto tail and
     * continue traversal.
     *
     * The implementation is completely directionally symmetrical,
     * except that most public methods that iterate through the list
     * follow next pointers ("forward" direction).
     *
     * There is one desirable property we would like to have, but
     * don't: it is possible, when an addFirst(A) is racing with
     * pollFirst() removing B, for an iterating observer to see A B C
     * and subsequently see A C, even though no interior removes are
     * ever performed.  I believe this wart can only be removed at
     * significant runtime cost.
     *
     * Empirically, microbenchmarks suggest that this class adds about
     * 40% overhead relative to ConcurrentLinkedQueue, which feels as
     * good as we can hope for.
     */

    /**
     * A node from which the first node on list (that is, the unique
     * node with node.prev == null) can be reached in O(1) time.
     * Invariants:
     * - the first node is always O(1) reachable from head via prev links
     * - all live nodes are reachable from the first node via succ()
     * - head != null
     * - (tmp = head).next != tmp || tmp != head
     * Non-invariants:
     * - head.item may or may not be null
     * - head may not be reachable from the first or last node, or from tail
     */
    private transient volatile Node<E> head = new Node<E>(null);

    private final static Node<Object> PREV_TERMINATOR, NEXT_TERMINATOR;

    static {
        PREV_TERMINATOR = new Node<Object>(null);
        PREV_TERMINATOR.next = PREV_TERMINATOR;
        NEXT_TERMINATOR = new Node<Object>(null);
        NEXT_TERMINATOR.prev = NEXT_TERMINATOR;
    }

    @SuppressWarnings("unchecked")
    Node<E> prevTerminator() {
        return (Node<E>) PREV_TERMINATOR;
    }

    @SuppressWarnings("unchecked")
    Node<E> nextTerminator() {
        return (Node<E>) NEXT_TERMINATOR;
    }

    /**
     * A node from which the last node on list (that is, the unique
     * node with node.next == null) can be reached in O(1) time.
     * Invariants:
     * - the last node is always O(1) reachable from tail via next links
     * - all live nodes are reachable from the last node via pred()
     * - tail != null
     * Non-invariants:
     * - tail.item may or may not be null
     * - tail may not be reachable from the first or last node, or from head
     */
    private transient volatile Node<E> tail = head;

    static final class Node<E> {
        volatile Node<E> prev;
        volatile E item;
        volatile Node<E> next;

        Node(E item) {
            // Piggyback on imminent casNext() or casPrev()
            lazySetItem(item);
        }

        boolean casItem(E cmp, E val) {
            return UNSAFE.compareAndSwapObject(this, itemOffset, cmp, val);
        }

        void lazySetItem(E val) {
            UNSAFE.putOrderedObject(this, itemOffset, val);
        }

        void lazySetNext(Node<E> val) {
            UNSAFE.putOrderedObject(this, nextOffset, val);
        }

        boolean casNext(Node<E> cmp, Node<E> val) {
            return UNSAFE.compareAndSwapObject(this, nextOffset, cmp, val);
        }

        void lazySetPrev(Node<E> val) {
            UNSAFE.putOrderedObject(this, prevOffset, val);
        }

        boolean casPrev(Node<E> cmp, Node<E> val) {
            return UNSAFE.compareAndSwapObject(this, prevOffset, cmp, val);
        }

        // Unsafe mechanics

        private static final sun.misc.Unsafe UNSAFE =
            sun.misc.Unsafe.getUnsafe();
        private static final long prevOffset =
            objectFieldOffset(UNSAFE, "prev", Node.class);
        private static final long itemOffset =
            objectFieldOffset(UNSAFE, "item", Node.class);
        private static final long nextOffset =
            objectFieldOffset(UNSAFE, "next", Node.class);
    }

    /**
     * Links e as first element.
     */
    private void linkFirst(E e) {
        checkNotNull(e);
        final Node<E> newNode = new Node<E>(e);

        retry:
        for (;;) {
            for (Node<E> h = head, p = h;;) {
                Node<E> q = p.prev;
                if (q == null) {
                    if (p.next == p)
                        continue retry;
                    newNode.lazySetNext(p); // CAS piggyback
                    if (p.casPrev(null, newNode)) {
                        if (p != h) // hop two nodes at a time
                            casHead(h, newNode);
                        return;
                    } else {
                        p = p.prev; // lost CAS race to another thread
                    }
                }
                else if (p == q)
                    continue retry;
                else
                    p = q;
            }
        }
    }

    /**
     * Links e as last element.
     */
    private void linkLast(E e) {
        checkNotNull(e);
        final Node<E> newNode = new Node<E>(e);

        retry:
        for (;;) {
            for (Node<E> t = tail, p = t;;) {
                Node<E> q = p.next;
                if (q == null) {
                    if (p.prev == p)
                        continue retry;
                    newNode.lazySetPrev(p); // CAS piggyback
                    if (p.casNext(null, newNode)) {
                        if (p != t) // hop two nodes at a time
                            casTail(t, newNode);
                        return;
                    } else {
                        p = p.next; // lost CAS race to another thread
                    }
                }
                else if (p == q)
                    continue retry;
                else
                    p = q;
            }
        }
    }

    // TODO: Is there a better cheap way of performing some cleanup
    // operation "occasionally"?
    static class Count {
        int count = 0;
    }
    private final static ThreadLocal<Count> tlc =
        new ThreadLocal<Count>() {
        protected Count initialValue() { return new Count(); }
    };
    private static boolean shouldGCUnlinkOccasionally() {
        return (tlc.get().count++ & 0x3) == 0;
    }

    private final static int HOPS = 2;

    /**
     * Unlinks non-null node x.
     */
    void unlink(Node<E> x) {
        assert x != null;
        assert x.item == null;
        assert x != PREV_TERMINATOR;
        assert x != NEXT_TERMINATOR;

        final Node<E> prev = x.prev;
        final Node<E> next = x.next;
        if (prev == null) {
            unlinkFirst(x, next);
        } else if (next == null) {
            unlinkLast(x, prev);
        } else {
            // Unlink interior node.
            //
            // This is the common case, since a series of polls at the
            // same end will be "interior" removes, except perhaps for
            // the first one, since end nodes cannot be physically removed.
            //
            // At any time, all active nodes are mutually reachable by
            // following a sequence of either next or prev pointers.
            //
            // Our strategy is to find the unique active predecessor
            // and successor of x.  Try to fix up their links so that
            // they point to each other, leaving x unreachable from
            // active nodes.  If successful, and if x has no live
            // predecessor/successor, we additionally try to leave
            // active nodes unreachable from x, by rechecking that
            // the status of predecessor and successor are unchanged
            // and ensuring that x is not reachable from tail/head,
            // before setting x's prev/next links to their logical
            // approximate replacements, self/TERMINATOR.
            Node<E> activePred, activeSucc;
            boolean isFirst, isLast;
            int hops = 1;

            // Find active predecessor
            for (Node<E> p = prev;; ++hops) {
                if (p.item != null) {
                    activePred = p;
                    isFirst = false;
                    break;
                }
                Node<E> q = p.prev;
                if (q == null) {
                    if (p == p.next)
                        return;
                    activePred = p;
                    isFirst = true;
                    break;
                }
                else if (p == q)
                    return;
                else
                    p = q;
            }

            // Find active successor
            for (Node<E> p = next;; ++hops) {
                if (p.item != null) {
                    activeSucc = p;
                    isLast = false;
                    break;
                }
                Node<E> q = p.next;
                if (q == null) {
                    if (p == p.prev)
                        return;
                    activeSucc = p;
                    isLast = true;
                    break;
                }
                else if (p == q)
                    return;
                else
                    p = q;
            }

            // TODO: better HOP heuristics
            if (hops < HOPS
                // always squeeze out interior deleted nodes
                && (isFirst | isLast))
                return;

            // Squeeze out deleted nodes between activePred and
            // activeSucc, including x.
            skipDeletedSuccessors(activePred);
            skipDeletedPredecessors(activeSucc);

            // Try to gc-unlink, if possible
            if ((isFirst | isLast) &&
                //shouldGCUnlinkOccasionally() &&

                // Recheck expected state of predecessor and successor
                (activePred.next == activeSucc) &&
                (activeSucc.prev == activePred) &&
                (isFirst ? activePred.prev == null : activePred.item != null) &&
                (isLast  ? activeSucc.next == null : activeSucc.item != null)) {

                // Ensure x is not reachable from head or tail
                updateHead();
                updateTail();
                x.lazySetPrev(isFirst ? prevTerminator() : x);
                x.lazySetNext(isLast  ? nextTerminator() : x);
            }
        }
    }

    /**
     * Unlinks non-null first node.
     */
    private void unlinkFirst(Node<E> first, Node<E> next) {
        assert first != null && next != null && first.item == null;
        Node<E> o = null, p = next;
        for (int hops = 0;; ++hops) {
            Node<E> q;
            if (p.item != null || (q = p.next) == null) {
                if (hops >= HOPS) {
                    if (p == p.prev)
                        return;
                    if (first.casNext(next, p)) {
                        skipDeletedPredecessors(p);
                        if (//shouldGCUnlinkOccasionally() &&
                            first.prev == null &&
                            (p.next == null || p.item != null) &&
                            p.prev == first) {

                            updateHead();
                            updateTail();
                            o.lazySetNext(o);
                            o.lazySetPrev(prevTerminator());
                        }
                    }
                }
                return;
            }
            else if (p == q)
                return;
            else {
                o = p;
                p = q;
            }
        }
    }

    /**
     * Unlinks non-null last node.
     */
    private void unlinkLast(Node<E> last, Node<E> prev) {
        assert last != null && prev != null && last.item == null;
        Node<E> o = null, p = prev;
        for (int hops = 0;; ++hops) {
            Node<E> q;
            if (p.item != null || (q = p.prev) == null) {
                if (hops >= HOPS) {
                    if (p == p.next)
                        return;
                    if (last.casPrev(prev, p)) {
                        skipDeletedSuccessors(p);
                        if (//shouldGCUnlinkOccasionally() &&
                            last.next == null &&
                            (p.prev == null || p.item != null) &&
                            p.next == last) {

                            updateHead();
                            updateTail();
                            o.lazySetPrev(o);
                            o.lazySetNext(nextTerminator());
                        }
                    }
                }
                return;
            }
            else if (p == q)
                return;
            else {
                o = p;
                p = q;
            }
        }
    }

    private final void updateHead() {
        first();
    }

    private final void updateTail() {
        last();
    }

    private void skipDeletedPredecessors(Node<E> x) {
        whileActive:
        do {
            Node<E> prev = x.prev;
            assert prev != null;
            assert x != NEXT_TERMINATOR;
            assert x != PREV_TERMINATOR;
            Node<E> p = prev;
            findActive:
            for (;;) {
                if (p.item != null)
                    break findActive;
                Node<E> q = p.prev;
                if (q == null) {
                    if (p.next == p)
                        continue whileActive;
                    break findActive;
                }
                else if (p == q)
                    continue whileActive;
                else
                    p = q;
            }

            // found active CAS target
            if (prev == p || x.casPrev(prev, p))
                return;

        } while (x.item != null || x.next == null);
    }

    private void skipDeletedSuccessors(Node<E> x) {
        whileActive:
        do {
            Node<E> next = x.next;
            assert next != null;
            assert x != NEXT_TERMINATOR;
            assert x != PREV_TERMINATOR;
            Node<E> p = next;
            findActive:
            for (;;) {
                if (p.item != null)
                    break findActive;
                Node<E> q = p.next;
                if (q == null) {
                    if (p.prev == p)
                        continue whileActive;
                    break findActive;
                }
                else if (p == q)
                    continue whileActive;
                else
                    p = q;
            }

            // found active CAS target
            if (next == p || x.casNext(next, p))
                return;

        } while (x.item != null || x.prev == null);
    }

    /**
     * Returns the successor of p, or the first node if p.next has been
     * linked to self, which will only be true if traversing with a
     * stale pointer that is now off the list.
     */
    final Node<E> succ(Node<E> p) {
        // TODO: should we skip deleted nodes here?
        Node<E> q = p.next;
        return (p == q) ? first() : q;
    }

    /**
     * Returns the predecessor of p, or the last node if p.prev has been
     * linked to self, which will only be true if traversing with a
     * stale pointer that is now off the list.
     */
    final Node<E> pred(Node<E> p) {
        Node<E> q = p.prev;
        return (p == q) ? last() : q;
    }

    /**
     * Returns the first node, the unique node which has a null prev link.
     * The returned node may or may not be logically deleted.
     * Guarantees that head is set to the returned node.
     */
    Node<E> first() {
        retry:
        for (;;) {
            for (Node<E> h = head, p = h;;) {
                Node<E> q = p.prev;
                if (q == null) {
                    if (p == h
                        // It is possible that p is PREV_TERMINATOR,
                        // but if so, the CAS will fail.
                        || casHead(h, p))
                        return p;
                    else
                        continue retry;
                } else if (p == q) {
                    continue retry;
                } else {
                    p = q;
                }
            }
        }
    }

    /**
     * Returns the last node, the unique node which has a null next link.
     * The returned node may or may not be logically deleted.
     * Guarantees that tail is set to the returned node.
     */
    Node<E> last() {
        retry:
        for (;;) {
            for (Node<E> t = tail, p = t;;) {
                Node<E> q = p.next;
                if (q == null) {
                    if (p == t
                        // It is possible that p is NEXT_TERMINATOR,
                        // but if so, the CAS will fail.
                        || casTail(t, p))
                        return p;
                    else
                        continue retry;
                } else if (p == q) {
                    continue retry;
                } else {
                    p = q;
                }
            }
        }
    }

    // Minor convenience utilities

    /**
     * Throws NullPointerException if argument is null.
     *
     * @param v the element
     */
    private static void checkNotNull(Object v) {
        if (v == null)
            throw new NullPointerException();
    }

    /**
     * Returns element unless it is null, in which case throws
     * NoSuchElementException.
     *
     * @param v the element
     * @return the element
     */
    private E screenNullResult(E v) {
        if (v == null)
            throw new NoSuchElementException();
        return v;
    }

    /**
     * Creates an array list and fills it with elements of this list.
     * Used by toArray.
     *
     * @return the arrayList
     */
    private ArrayList<E> toArrayList() {
        ArrayList<E> c = new ArrayList<E>();
        for (Node<E> p = first(); p != null; p = succ(p)) {
            E item = p.item;
            if (item != null)
                c.add(item);
        }
        return c;
    }

    // Fields and constructors

    private static final long serialVersionUID = 876323262645176354L;

    /**
     * Constructs an empty deque.
     */
    public ConcurrentLinkedDeque() {}

    /**
     * Constructs a deque initially containing the elements of
     * the given collection, added in traversal order of the
     * collection's iterator.
     *
     * @param c the collection of elements to initially contain
     * @throws NullPointerException if the specified collection or any
     *         of its elements are null
     */
     public ConcurrentLinkedDeque(Collection<? extends E> c) {
         this();
         addAll(c);
     }

    /**
     * Inserts the specified element at the front of this deque.
     *
     * @throws NullPointerException {@inheritDoc}
     */
    public void addFirst(E e) {
        linkFirst(e);
    }

    /**
     * Inserts the specified element at the end of this deque.
     * This is identical in function to the {@code add} method.
     *
     * @throws NullPointerException {@inheritDoc}
     */
    public void addLast(E e) {
        linkLast(e);
    }

    /**
     * Inserts the specified element at the front of this deque.
     *
     * @return {@code true} always
     * @throws NullPointerException {@inheritDoc}
     */
    public boolean offerFirst(E e) {
        linkFirst(e);
        return true;
    }

    /**
     * Inserts the specified element at the end of this deque.
     *
     * <p>This method is equivalent to {@link #add}.
     *
     * @return {@code true} always
     * @throws NullPointerException {@inheritDoc}
     */
    public boolean offerLast(E e) {
        linkLast(e);
        return true;
    }

    public E peekFirst() {
        for (Node<E> p = first(); p != null; p = succ(p)) {
            E item = p.item;
            if (item != null)
                return item;
        }
        return null;
    }

    public E peekLast() {
        for (Node<E> p = last(); p != null; p = pred(p)) {
            E item = p.item;
            if (item != null)
                return item;
        }
        return null;
    }

    /**
     * @throws NoSuchElementException {@inheritDoc}
     */
    public E getFirst() {
        return screenNullResult(peekFirst());
    }

    /**
     * @throws NoSuchElementException {@inheritDoc}
     */
    public E getLast()  {
        return screenNullResult(peekLast());
    }

    public E pollFirst() {
        for (Node<E> p = first(); p != null; p = succ(p)) {
            E item = p.item;
            if (item != null && p.casItem(item, null)) {
                unlink(p);
                return item;
            }
        }
        return null;
    }

    public E pollLast() {
        for (Node<E> p = last(); p != null; p = pred(p)) {
            E item = p.item;
            if (item != null && p.casItem(item, null)) {
                unlink(p);
                return item;
            }
        }
        return null;
    }

    /**
     * @throws NoSuchElementException {@inheritDoc}
     */
    public E removeFirst() {
        return screenNullResult(pollFirst());
    }

    /**
     * @throws NoSuchElementException {@inheritDoc}
     */
    public E removeLast() {
        return screenNullResult(pollLast());
    }

    // *** Queue and stack methods ***

    /**
     * Inserts the specified element at the tail of this deque.
     *
     * @return {@code true} (as specified by {@link Queue#offer})
     * @throws NullPointerException if the specified element is null
     */
    public boolean offer(E e) {
        return offerLast(e);
    }

    /**
     * Inserts the specified element at the tail of this deque.
     *
     * @return {@code true} (as specified by {@link Collection#add})
     * @throws NullPointerException if the specified element is null
     */
    public boolean add(E e) {
        return offerLast(e);
    }

    public E poll()           { return pollFirst(); }
    public E remove()         { return removeFirst(); }
    public E peek()           { return peekFirst(); }
    public E element()        { return getFirst(); }
    public void push(E e)     { addFirst(e); }
    public E pop()            { return removeFirst(); }

    /**
     * Removes the first element {@code e} such that
     * {@code o.equals(e)}, if such an element exists in this deque.
     * If the deque does not contain the element, it is unchanged.
     *
     * @param o element to be removed from this deque, if present
     * @return {@code true} if the deque contained the specified element
     * @throws NullPointerException if the specified element is {@code null}
     */
    public boolean removeFirstOccurrence(Object o) {
        checkNotNull(o);
        for (Node<E> p = first(); p != null; p = succ(p)) {
            E item = p.item;
            if (item != null && o.equals(item) && p.casItem(item, null)) {
                unlink(p);
                return true;
            }
        }
        return false;
    }

    /**
     * Removes the last element {@code e} such that
     * {@code o.equals(e)}, if such an element exists in this deque.
     * If the deque does not contain the element, it is unchanged.
     *
     * @param o element to be removed from this deque, if present
     * @return {@code true} if the deque contained the specified element
     * @throws NullPointerException if the specified element is {@code null}
     */
    public boolean removeLastOccurrence(Object o) {
        checkNotNull(o);
        for (Node<E> p = last(); p != null; p = pred(p)) {
            E item = p.item;
            if (item != null && o.equals(item) && p.casItem(item, null)) {
                unlink(p);
                return true;
            }
        }
        return false;
    }

    /**
     * Returns {@code true} if this deque contains at least one
     * element {@code e} such that {@code o.equals(e)}.
     *
     * @param o element whose presence in this deque is to be tested
     * @return {@code true} if this deque contains the specified element
     */
    public boolean contains(Object o) {
        if (o == null) return false;
        for (Node<E> p = first(); p != null; p = succ(p)) {
            E item = p.item;
            if (item != null && o.equals(item))
                return true;
        }
        return false;
    }

    /**
     * Returns {@code true} if this collection contains no elements.
     *
     * @return {@code true} if this collection contains no elements
     */
    public boolean isEmpty() {
        return peekFirst() == null;
    }

    /**
     * Returns the number of elements in this deque.  If this deque
     * contains more than {@code Integer.MAX_VALUE} elements, it
     * returns {@code Integer.MAX_VALUE}.
     *
     * <p>Beware that, unlike in most collections, this method is
     * <em>NOT</em> a constant-time operation. Because of the
     * asynchronous nature of these deques, determining the current
     * number of elements requires traversing them all to count them.
     * Additionally, it is possible for the size to change during
     * execution of this method, in which case the returned result
     * will be inaccurate. Thus, this method is typically not very
     * useful in concurrent applications.
     *
     * @return the number of elements in this deque
     */
    public int size() {
        long count = 0;
        for (Node<E> p = first(); p != null; p = succ(p))
            if (p.item != null)
                ++count;
        return (count >= Integer.MAX_VALUE) ? Integer.MAX_VALUE : (int) count;
    }

    /**
     * Removes the first element {@code e} such that
     * {@code o.equals(e)}, if such an element exists in this deque.
     * If the deque does not contain the element, it is unchanged.
     *
     * @param o element to be removed from this deque, if present
     * @return {@code true} if the deque contained the specified element
     * @throws NullPointerException if the specified element is {@code null}
     */
    public boolean remove(Object o) {
        return removeFirstOccurrence(o);
    }

    /**
     * Appends all of the elements in the specified collection to the end of
     * this deque, in the order that they are returned by the specified
     * collection's iterator.  The behavior of this operation is undefined if
     * the specified collection is modified while the operation is in
     * progress.  (This implies that the behavior of this call is undefined if
     * the specified Collection is this deque, and this deque is nonempty.)
     *
     * @param c the elements to be inserted into this deque
     * @return {@code true} if this deque changed as a result of the call
     * @throws NullPointerException if {@code c} or any element within it
     * is {@code null}
     */
    public boolean addAll(Collection<? extends E> c) {
        Iterator<? extends E> it = c.iterator();
        if (!it.hasNext())
            return false;
        do {
            addLast(it.next());
        } while (it.hasNext());
        return true;
    }

    /**
     * Removes all of the elements from this deque.
     */
    public void clear() {
        while (pollFirst() != null)
            ;
    }

    /**
     * Returns an array containing all of the elements in this deque, in
     * proper sequence (from first to last element).
     *
     * <p>The returned array will be "safe" in that no references to it are
     * maintained by this deque.  (In other words, this method must allocate
     * a new array).  The caller is thus free to modify the returned array.
     *
     * <p>This method acts as bridge between array-based and collection-based
     * APIs.
     *
     * @return an array containing all of the elements in this deque
     */
    public Object[] toArray() {
        return toArrayList().toArray();
    }

    /**
     * Returns an array containing all of the elements in this deque,
     * in proper sequence (from first to last element); the runtime
     * type of the returned array is that of the specified array.  If
     * the deque fits in the specified array, it is returned therein.
     * Otherwise, a new array is allocated with the runtime type of
     * the specified array and the size of this deque.
     *
     * <p>If this deque fits in the specified array with room to spare
     * (i.e., the array has more elements than this deque), the element in
     * the array immediately following the end of the deque is set to
     * {@code null}.
     *
     * <p>Like the {@link #toArray()} method, this method acts as bridge between
     * array-based and collection-based APIs.  Further, this method allows
     * precise control over the runtime type of the output array, and may,
     * under certain circumstances, be used to save allocation costs.
     *
     * <p>Suppose {@code x} is a deque known to contain only strings.
     * The following code can be used to dump the deque into a newly
     * allocated array of {@code String}:
     *
     * <pre>
     *     String[] y = x.toArray(new String[0]);</pre>
     *
     * Note that {@code toArray(new Object[0])} is identical in function to
     * {@code toArray()}.
     *
     * @param a the array into which the elements of the deque are to
     *          be stored, if it is big enough; otherwise, a new array of the
     *          same runtime type is allocated for this purpose
     * @return an array containing all of the elements in this deque
     * @throws ArrayStoreException if the runtime type of the specified array
     *         is not a supertype of the runtime type of every element in
     *         this deque
     * @throws NullPointerException if the specified array is null
     */
    public <T> T[] toArray(T[] a) {
        return toArrayList().toArray(a);
    }

    /**
     * Returns an iterator over the elements in this deque in proper sequence.
     * The elements will be returned in order from first (head) to last (tail).
     *
     * <p>The returned {@code Iterator} is a "weakly consistent" iterator that
     * will never throw {@link java.util.ConcurrentModificationException
     * ConcurrentModificationException},
     * and guarantees to traverse elements as they existed upon
     * construction of the iterator, and may (but is not guaranteed to)
     * reflect any modifications subsequent to construction.
     *
     * @return an iterator over the elements in this deque in proper sequence
     */
    public Iterator<E> iterator() {
        return new Itr();
    }

    /**
     * Returns an iterator over the elements in this deque in reverse
     * sequential order.  The elements will be returned in order from
     * last (tail) to first (head).
     *
     * <p>The returned {@code Iterator} is a "weakly consistent" iterator that
     * will never throw {@link java.util.ConcurrentModificationException
     * ConcurrentModificationException},
     * and guarantees to traverse elements as they existed upon
     * construction of the iterator, and may (but is not guaranteed to)
     * reflect any modifications subsequent to construction.
     */
    public Iterator<E> descendingIterator() {
        return new DescendingItr();
    }

    private abstract class AbstractItr implements Iterator<E> {
        /**
         * Next node to return item for.
         */
        private Node<E> nextNode;

        /**
         * nextItem holds on to item fields because once we claim
         * that an element exists in hasNext(), we must return it in
         * the following next() call even if it was in the process of
         * being removed when hasNext() was called.
         */
        private E nextItem;

        /**
         * Node returned by most recent call to next. Needed by remove.
         * Reset to null if this element is deleted by a call to remove.
         */
        private Node<E> lastRet;

        abstract Node<E> startNode();
        abstract Node<E> nextNode(Node<E> p);

        AbstractItr() {
            advance();
        }

        /**
         * Sets nextNode and nextItem to next valid node, or to null
         * if no such.
         */
        private void advance() {
            lastRet = nextNode;

            Node<E> p = (nextNode == null) ? startNode() : nextNode(nextNode);
            for (;; p = nextNode(p)) {
                if (p == null) {
                    // p might be active end or TERMINATOR node; both are OK
                    nextNode = null;
                    nextItem = null;
                    break;
                }
                E item = p.item;
                if (item != null) {
                    nextNode = p;
                    nextItem = item;
                    break;
                }
            }
        }

        public boolean hasNext() {
            return nextItem != null;
        }

        public E next() {
            E item = nextItem;
            if (item == null) throw new NoSuchElementException();
            advance();
            return item;
        }

        public void remove() {
            Node<E> l = lastRet;
            if (l == null) throw new IllegalStateException();
            l.item = null;
            unlink(l);
            lastRet = null;
        }
    }

    /** Forward iterator */
    private class Itr extends AbstractItr {
        Node<E> startNode() { return first(); }
        Node<E> nextNode(Node<E> p) { return succ(p); }
    }

    /** Descending iterator */
    private class DescendingItr extends AbstractItr {
        Node<E> startNode() { return last(); }
        Node<E> nextNode(Node<E> p) { return pred(p); }
    }

    /**
     * Save the state to a stream (that is, serialize it).
     *
     * @serialData All of the elements (each an {@code E}) in
     * the proper order, followed by a null
     * @param s the stream
     */
    private void writeObject(java.io.ObjectOutputStream s)
        throws java.io.IOException {

        // Write out any hidden stuff
        s.defaultWriteObject();

        // Write out all elements in the proper order.
        for (Node<E> p = first(); p != null; p = succ(p)) {
            Object item = p.item;
            if (item != null)
                s.writeObject(item);
        }

        // Use trailing null as sentinel
        s.writeObject(null);
    }

    /**
     * Reconstitute the Queue instance from a stream (that is,
     * deserialize it).
     * @param s the stream
     */
    private void readObject(java.io.ObjectInputStream s)
        throws java.io.IOException, ClassNotFoundException {
        // Read in capacity, and any hidden stuff
        s.defaultReadObject();
        tail = head = new Node<E>(null);
        // Read in all elements and place in queue
        for (;;) {
            @SuppressWarnings("unchecked")
            E item = (E)s.readObject();
            if (item == null)
                break;
            else
                offer(item);
        }
    }

    // Unsafe mechanics

    private static final sun.misc.Unsafe UNSAFE =
        sun.misc.Unsafe.getUnsafe();
    private static final long headOffset =
        objectFieldOffset(UNSAFE, "head", ConcurrentLinkedDeque.class);
    private static final long tailOffset =
        objectFieldOffset(UNSAFE, "tail", ConcurrentLinkedDeque.class);

    private boolean casHead(Node<E> cmp, Node<E> val) {
        return UNSAFE.compareAndSwapObject(this, headOffset, cmp, val);
    }

    private boolean casTail(Node<E> cmp, Node<E> val) {
        return UNSAFE.compareAndSwapObject(this, tailOffset, cmp, val);
    }

    static long objectFieldOffset(sun.misc.Unsafe UNSAFE,
                                  String field, Class<?> klazz) {
        try {
            return UNSAFE.objectFieldOffset(klazz.getDeclaredField(field));
        } catch (NoSuchFieldException e) {
            // Convert Exception to corresponding Error
            NoSuchFieldError error = new NoSuchFieldError(field);
            error.initCause(e);
            throw error;
        }
    }
}
