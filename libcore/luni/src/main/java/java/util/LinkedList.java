/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package java.util;


import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Array;

/**
 * LinkedList is an implementation of List, backed by a linked list. All
 * optional operations (adding, removing and replacing) are supported. The
 * elements can be any objects.
 * 
 * @since Android 1.0
 */
public class LinkedList<E> extends AbstractSequentialList<E> implements
        List<E>, Queue<E>, Cloneable, Serializable {
    
    private static final long serialVersionUID = 876323262645176354L;

    transient int size = 0;

    transient Link<E> voidLink;

    private static final class Link<ET> {
        ET data;

        Link<ET> previous, next;

        Link(ET o, Link<ET> p, Link<ET> n) {
            data = o;
            previous = p;
            next = n;
        }
    }

    private static final class LinkIterator<ET> implements ListIterator<ET> {
        int pos, expectedModCount;

        final LinkedList<ET> list;

        Link<ET> link, lastLink;

        LinkIterator(LinkedList<ET> object, int location) {
            list = object;
            expectedModCount = list.modCount;
            if (0 <= location && location <= list.size) {
                // pos ends up as -1 if list is empty, it ranges from -1 to
                // list.size - 1
                // if link == voidLink then pos must == -1
                link = list.voidLink;
                if (location < list.size / 2) {
                    for (pos = -1; pos + 1 < location; pos++) {
                        link = link.next;
                    }
                } else {
                    for (pos = list.size; pos >= location; pos--) {
                        link = link.previous;
                    }
                }
            } else {
                throw new IndexOutOfBoundsException();
            }
        }

        public void add(ET object) {
            if (expectedModCount == list.modCount) {
                Link<ET> next = link.next;
                Link<ET> newLink = new Link<ET>(object, link, next);
                link.next = newLink;
                next.previous = newLink;
                link = newLink;
                lastLink = null;
                pos++;
                expectedModCount++;
                list.size++;
                list.modCount++;
            } else {
                throw new ConcurrentModificationException();
            }
        }

        public boolean hasNext() {
            return link.next != list.voidLink;
        }

        public boolean hasPrevious() {
            return link != list.voidLink;
        }

        public ET next() {
            if (expectedModCount == list.modCount) {
                LinkedList.Link<ET> next = link.next;
                if (next != list.voidLink) {
                    lastLink = link = next;
                    pos++;
                    return link.data;
                }
                throw new NoSuchElementException();
            }
            throw new ConcurrentModificationException();
        }

        public int nextIndex() {
            return pos + 1;
        }

        public ET previous() {
            if (expectedModCount == list.modCount) {
                if (link != list.voidLink) {
                    lastLink = link;
                    link = link.previous;
                    pos--;
                    return lastLink.data;
                }
                throw new NoSuchElementException();
            }
            throw new ConcurrentModificationException();
        }

        public int previousIndex() {
            return pos;
        }

        public void remove() {
            if (expectedModCount == list.modCount) {
                if (lastLink != null) {
                    Link<ET> next = lastLink.next;
                    Link<ET> previous = lastLink.previous;
                    next.previous = previous;
                    previous.next = next;
                    if (lastLink == link) {
                        pos--;
                    }
                    link = previous;
                    lastLink = null;
                    expectedModCount++;
                    list.size--;
                    list.modCount++;
                } else {
                    throw new IllegalStateException();
                }
            } else {
                throw new ConcurrentModificationException();
            }
        }

        public void set(ET object) {
            if (expectedModCount == list.modCount) {
                if (lastLink != null) {
                    lastLink.data = object;
                } else {
                    throw new IllegalStateException();
                }
            } else {
                throw new ConcurrentModificationException();
            }
        }
    }

    /**
     * Constructs a new empty instance of {@code LinkedList}.
     * 
     * @since Android 1.0
     */
    public LinkedList() {
        voidLink = new Link<E>(null, null, null);
        voidLink.previous = voidLink;
        voidLink.next = voidLink;
    }

    /**
     * Constructs a new instance of {@code LinkedList} that holds all of the
     * elements contained in the specified {@code collection}. The order of the
     * elements in this new {@code LinkedList} will be determined by the
     * iteration order of {@code collection}.
     * 
     * @param collection
     *            the collection of elements to add.
     * @since Android 1.0
     */
    public LinkedList(Collection<? extends E> collection) {
        this();
        addAll(collection);
    }

    /**
     * Inserts the specified object into this {@code LinkedList} at the
     * specified location. The object is inserted before any previous element at
     * the specified location. If the location is equal to the size of this
     * {@code LinkedList}, the object is added at the end.
     * 
     * @param location
     *            the index at which to insert.
     * @param object
     *            the object to add.
     * @throws IndexOutOfBoundsException
     *             if {@code location < 0 || >= size()}
     * @since Android 1.0
     */
    @Override
    public void add(int location, E object) {
        if (0 <= location && location <= size) {
            Link<E> link = voidLink;
            if (location < (size / 2)) {
                for (int i = 0; i <= location; i++) {
                    link = link.next;
                }
            } else {
                for (int i = size; i > location; i--) {
                    link = link.previous;
                }
            }
            Link<E> previous = link.previous;
            Link<E> newLink = new Link<E>(object, previous, link);
            previous.next = newLink;
            link.previous = newLink;
            size++;
            modCount++;
        } else {
            throw new IndexOutOfBoundsException();
        }
    }

    /**
     * Adds the specified object at the end of this {@code LinkedList}.
     * 
     * @param object
     *            the object to add.
     * @return always true
     * @since Android 1.0
     */
    @Override
    public boolean add(E object) {
        // Cannot call addLast() as subclasses can override
        Link<E> oldLast = voidLink.previous;
        Link<E> newLink = new Link<E>(object, oldLast, voidLink);
        voidLink.previous = newLink;
        oldLast.next = newLink;
        size++;
        modCount++;
        return true;
    }

    /**
     * Inserts the objects in the specified collection at the specified location
     * in this {@code LinkedList}. The objects are added in the order they are
     * returned from the collection's iterator.
     * 
     * @param location
     *            the index at which to insert.
     * @param collection
     *            the collection of objects
     * @return {@code true} if this {@code LinkedList} is modified,
     *         {@code false} otherwise.
     * @throws ClassCastException
     *             if the class of an object is inappropriate for this list.
     * @throws IllegalArgumentException
     *             if an object cannot be added to this list.
     * @throws IndexOutOfBoundsException
     *             if {@code location < 0 || > size()}
     * @since Android 1.0
     */
    @Override
    public boolean addAll(int location, Collection<? extends E> collection) {
        if (location < 0 || location > size) {
            throw new IndexOutOfBoundsException();
        }
        int adding = collection.size();
        if (adding == 0) {
            return false;
        }
        Link<E> previous = voidLink;
        if (location < (size / 2)) {
            for (int i = 0; i < location; i++) {
                previous = previous.next;
            }
        } else {
            for (int i = size; i >= location; i--) {
                previous = previous.previous;
            }
        }
        Link<E> next = previous.next;
        for (E e : collection) {
            Link<E> newLink = new Link<E>(e, previous, null);
            previous.next = newLink;
            previous = newLink;
        }
        previous.next = next;
        next.previous = previous;
        size += adding;
        modCount++;
        return true;
    }


    /**
     * Adds the objects in the specified Collection to this {@code LinkedList}.
     * 
     * @param collection
     *            the collection of objects.
     * @return {@code true} if this {@code LinkedList} is modified,
     *         {@code false} otherwise.
     * @since Android 1.0
     */
    @Override
    public boolean addAll(Collection<? extends E> collection) {
        int adding = collection.size();
        if (adding == 0) {
            return false;
        }
        Link<E> previous = voidLink.previous;
        for (E e : collection) {
            Link<E> newLink = new Link<E>(e, previous, null);
            previous.next = newLink;
            previous = newLink;
        }
        previous.next = voidLink;
        voidLink.previous = previous;
        size += adding;
        modCount++;
        return true;
    }

    /**
     * Adds the specified object at the beginning of this {@code LinkedList}.
     * 
     * @param object
     *            the object to add.
     * @since Android 1.0
     */
    public void addFirst(E object) {
        Link<E> oldFirst = voidLink.next;
        Link<E> newLink = new Link<E>(object, voidLink, oldFirst);
        voidLink.next = newLink;
        oldFirst.previous = newLink;
        size++;
        modCount++;
    }

    /**
     * Adds the specified object at the end of this {@code LinkedList}.
     * 
     * @param object
     *            the object to add.
     * @since Android 1.0
     */
    public void addLast(E object) {
        Link<E> oldLast = voidLink.previous;
        Link<E> newLink = new Link<E>(object, oldLast, voidLink);
        voidLink.previous = newLink;
        oldLast.next = newLink;
        size++;
        modCount++;
    }

    /**
     * Removes all elements from this {@code LinkedList}, leaving it empty.
     * 
     * @see List#isEmpty
     * @see #size
     * @since Android 1.0
     */
    @Override
    public void clear() {
        if (size > 0) {
            size = 0;
            voidLink.next = voidLink;
            voidLink.previous = voidLink;
            modCount++;
        }
    }

    /**
     * Returns a new {@code LinkedList} with the same elements and size as this
     * {@code LinkedList}.
     * 
     * @return a shallow copy of this {@code LinkedList}.
     * @see java.lang.Cloneable
     * @since Android 1.0
     */
    @SuppressWarnings("unchecked")
    @Override
    public Object clone() {
        try {
            LinkedList<E> l = (LinkedList<E>) super.clone();
            l.size = 0;
            l.voidLink = new Link<E>(null, null, null);
            l.voidLink.previous = l.voidLink;
            l.voidLink.next = l.voidLink;
            l.addAll(this);
            return l;
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    /**
     * Searches this {@code LinkedList} for the specified object.
     * 
     * @param object
     *            the object to search for.
     * @return {@code true} if {@code object} is an element of this
     *         {@code LinkedList}, {@code false} otherwise
     * @since Android 1.0
     */
    @Override
    public boolean contains(Object object) {
        Link<E> link = voidLink.next;
        if (object != null) {
            while (link != voidLink) {
                if (object.equals(link.data)) {
                    return true;
                }
                link = link.next;
            }
        } else {
            while (link != voidLink) {
                if (link.data == null) {
                    return true;
                }
                link = link.next;
            }
        }
        return false;
    }

    @Override
    public E get(int location) {
        if (0 <= location && location < size) {
            Link<E> link = voidLink;
            if (location < (size / 2)) {
                for (int i = 0; i <= location; i++) {
                    link = link.next;
                }
            } else {
                for (int i = size; i > location; i--) {
                    link = link.previous;
                }
            }
            return link.data;
        }
        throw new IndexOutOfBoundsException();
    }

    /**
     * Returns the first element in this {@code LinkedList}.
     * 
     * @return the first element.
     * @throws NoSuchElementException
     *             if this {@code LinkedList} is empty.
     * @since Android 1.0
     */
    public E getFirst() {
        Link<E> first = voidLink.next;
        if (first != voidLink) {
            return first.data;
        }
        throw new NoSuchElementException();
    }

    /**
     * Returns the last element in this {@code LinkedList}.
     * 
     * @return the last element
     * @throws NoSuchElementException
     *             if this {@code LinkedList} is empty
     * @since Android 1.0
     */
    public E getLast() {
        Link<E> last = voidLink.previous;
        if (last != voidLink) {
            return last.data;
        }
        throw new NoSuchElementException();
    }

    @Override
    public int indexOf(Object object) {
        int pos = 0;
        Link<E> link = voidLink.next;
        if (object != null) {
            while (link != voidLink) {
                if (object.equals(link.data)) {
                    return pos;
                }
                link = link.next;
                pos++;
            }
        } else {
            while (link != voidLink) {
                if (link.data == null) {
                    return pos;
                }
                link = link.next;
                pos++;
            }
        }
        return -1;
    }

    /**
     * Searches this {@code LinkedList} for the specified object and returns the
     * index of the last occurrence.
     * 
     * @param object
     *            the object to search for
     * @return the index of the last occurrence of the object, or -1 if it was
     *         not found.
     * @since Android 1.0
     */
    @Override
    public int lastIndexOf(Object object) {
        int pos = size;
        Link<E> link = voidLink.previous;
        if (object != null) {
            while (link != voidLink) {
                pos--;
                if (object.equals(link.data)) {
                    return pos;
                }
                link = link.previous;
            }
        } else {
            while (link != voidLink) {
                pos--;
                if (link.data == null) {
                    return pos;
                }
                link = link.previous;
            }
        }
        return -1;
    }

    /**
     * Returns a ListIterator on the elements of this {@code LinkedList}. The
     * elements are iterated in the same order that they occur in the
     * {@code LinkedList}. The iteration starts at the specified location.
     * 
     * @param location
     *            the index at which to start the iteration
     * @return a ListIterator on the elements of this {@code LinkedList}
     * @throws IndexOutOfBoundsException
     *             if {@code location < 0 || >= size()}
     * @see ListIterator
     * @since Android 1.0
     */
    @Override
    public ListIterator<E> listIterator(int location) {
        return new LinkIterator<E>(this, location);
    }

    /**
     * Removes the object at the specified location from this {@code LinkedList}.
     * 
     * @param location
     *            the index of the object to remove
     * @return the removed object
     * @throws IndexOutOfBoundsException
     *             if {@code location < 0 || >= size()}
     * @since Android 1.0
     */
    @Override
    public E remove(int location) {
        if (0 <= location && location < size) {
            Link<E> link = voidLink;
            if (location < (size / 2)) {
                for (int i = 0; i <= location; i++) {
                    link = link.next;
                }
            } else {
                for (int i = size; i > location; i--) {
                    link = link.previous;
                }
            }
            Link<E> previous = link.previous;
            Link<E> next = link.next;
            previous.next = next;
            next.previous = previous;
            size--;
            modCount++;
            return link.data;
        }
        throw new IndexOutOfBoundsException();
    }

    @Override
    public boolean remove(Object object) {
        Link<E> link = voidLink.next;
        if (object != null) {
            while (link != voidLink && !object.equals(link.data)) {
                link = link.next;
            }
        } else {
            while (link != voidLink && link.data != null) {
                link = link.next;
            }
        }
        if (link == voidLink) {
            return false;
        }
        Link<E> next = link.next;
        Link<E> previous = link.previous;
        previous.next = next;
        next.previous = previous;
        size--;
        modCount++;
        return true;
    }

    /**
     * Removes the first object from this {@code LinkedList}.
     * 
     * @return the removed object.
     * @throws NoSuchElementException
     *             if this {@code LinkedList} is empty.
     * @since Android 1.0
     */
    public E removeFirst() {
        Link<E> first = voidLink.next;
        if (first != voidLink) {
            Link<E> next = first.next;
            voidLink.next = next;
            next.previous = voidLink;
            size--;
            modCount++;
            return first.data;
        }
        throw new NoSuchElementException();
    }

    /**
     * Removes the last object from this {@code LinkedList}.
     * 
     * @return the removed object.
     * @throws NoSuchElementException
     *             if this {@code LinkedList} is empty.
     * @since Android 1.0
     */
    public E removeLast() {
        Link<E> last = voidLink.previous;
        if (last != voidLink) {
            Link<E> previous = last.previous;
            voidLink.previous = previous;
            previous.next = voidLink;
            size--;
            modCount++;
            return last.data;
        }
        throw new NoSuchElementException();
    }

    /**
     * Replaces the element at the specified location in this {@code LinkedList}
     * with the specified object.
     * 
     * @param location
     *            the index at which to put the specified object.
     * @param object
     *            the object to add.
     * @return the previous element at the index.
     * @throws ClassCastException
     *             if the class of an object is inappropriate for this list.
     * @throws IllegalArgumentException
     *             if an object cannot be added to this list.
     * @throws IndexOutOfBoundsException
     *             if {@code location < 0 || >= size()}
     * @since Android 1.0
     */
    @Override
    public E set(int location, E object) {
        if (0 <= location && location < size) {
            Link<E> link = voidLink;
            if (location < (size / 2)) {
                for (int i = 0; i <= location; i++) {
                    link = link.next;
                }
            } else {
                for (int i = size; i > location; i--) {
                    link = link.previous;
                }
            }
            E result = link.data;
            link.data = object;
            return result;
        }
        throw new IndexOutOfBoundsException();
    }

    /**
     * Returns the number of elements in this {@code LinkedList}.
     * 
     * @return the number of elements in this {@code LinkedList}.
     * @since Android 1.0
     */
    @Override
    public int size() {
        return size;
    }
    
    public boolean offer(E o) {
        add(o);
        return true;
    }

    public E poll() {
        return size == 0 ? null : removeFirst();
    }

    public E remove() {
        return removeFirst();
    }

    public E peek() {
        Link<E> first = voidLink.next;
        return first == voidLink ? null : first.data;
    }

    public E element() {
        return getFirst();
    }

    /**
     * Returns a new array containing all elements contained in this
     * {@code LinkedList}.
     * 
     * @return an array of the elements from this {@code LinkedList}.
     * @since Android 1.0
     */
    @Override
    public Object[] toArray() {
        int index = 0;
        Object[] contents = new Object[size];
        Link<E> link = voidLink.next;
        while (link != voidLink) {
            contents[index++] = link.data;
            link = link.next;
        }
        return contents;
    }

    /**
     * Returns an array containing all elements contained in this
     * {@code LinkedList}. If the specified array is large enough to hold the
     * elements, the specified array is used, otherwise an array of the same
     * type is created. If the specified array is used and is larger than this
     * {@code LinkedList}, the array element following the collection elements
     * is set to null.
     * 
     * @param contents
     *            the array.
     * @return an array of the elements from this {@code LinkedList}.
     * @throws ArrayStoreException
     *             if the type of an element in this {@code LinkedList} cannot
     *             be stored in the type of the specified array.
     * @since Android 1.0
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] contents) {
        int index = 0;
        if (size > contents.length) {
            Class<?> ct = contents.getClass().getComponentType();
            contents = (T[]) Array.newInstance(ct, size);
        }
        Link<E> link = voidLink.next;
        while (link != voidLink) {
            contents[index++] = (T)link.data;
            link = link.next;
        }
        if (index < contents.length) {
            contents[index] = null;
        }
        return contents;
    }

    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.defaultWriteObject();
        stream.writeInt(size);
        Iterator<E> it = iterator();
        while (it.hasNext()) {
            stream.writeObject(it.next());
        }
    }

    @SuppressWarnings("unchecked")
    private void readObject(ObjectInputStream stream) throws IOException,
            ClassNotFoundException {
        stream.defaultReadObject();
        size = stream.readInt();
        voidLink = new Link<E>(null, null, null);
        Link<E> link = voidLink;
        for (int i = size; --i >= 0;) {
            Link<E> nextLink = new Link<E>((E)stream.readObject(), link, null);
            link.next = nextLink;
            link = nextLink;
        }
        link.next = voidLink;
        voidLink.previous = link;
    }
}
