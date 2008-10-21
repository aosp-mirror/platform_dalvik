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


import java.lang.reflect.Array;

/**
 * AbstractCollection is an abstract implementation of the Collection interface.
 * This implementation does not support adding. A subclass must implement the
 * abstract methods iterator() and size().
 * @since 1.2
 */
public abstract class AbstractCollection<E> implements Collection<E> {

    /**
     * Constructs a new instance of this AbstractCollection.
     */
    protected AbstractCollection() {
        super();
    }

    /**
     * If the specified element is not contained within this collection, and
     * addition of this element succeeds, then true will be returned. If the
     * specified element is already contained within this collection, or
     * duplication is not permitted, false will be returned. Different
     * implementations may add specific limitations on this method to filter
     * permitted elements. For example, in some implementation, null element may
     * be denied, and NullPointerException will be thrown out. These limitations
     * should be explicitly documented by specific collection implementation.
     * 
     * Add operation is not supported in this implementation, and
     * UnsupportedOperationException will always be thrown out.
     * 
     * @param object
     *            the element to be added.
     * @return true if the collection is changed successfully after invoking
     *         this method. Otherwise, false.
     * @throws UnsupportedOperationException
     *                if add operation is not supported by this class.
     * @throws NullPointerException
     *                if null is used to invoke this method, and null is not
     *                permitted by this collection.
     * @throws ClassCastException
     *                if the class type of the specified element is not
     *                compatible with the permitted class type.
     * @throws IllegalArgumentException
     *                if limitations of this collection prevent the specified
     *                element from being added
     */
    public boolean add(E object) {
        throw new UnsupportedOperationException();
    }

    /**
     * Adds the objects in the specified Collection to this Collection.
     * 
     * @param collection
     *            the Collection of objects
     * @return true if this Collection is modified, false otherwise
     * 
     * @throws UnsupportedOperationException
     *                when adding to this Collection is not supported
     * @throws NullPointerException
     *                if null is used to invoke this method
     */
    public boolean addAll(Collection<? extends E> collection) {
        boolean result = false;
        Iterator<? extends E> it = collection.iterator();
        while (it.hasNext()) {
            if (add(it.next())) {
                result = true;
            }
        }
        return result;
    }

    /**
     * Removes all the elements in this collection. This collection will be 
     * cleared up after this operation. The operation iterates over the 
     * collection, removes every element using Iterator.remove method.
     * 
     * UnsupportedOperationException will be thrown out if the iterator returned
     * by this collection does not implement the remove method and the collection
     * is not zero length.
     * 
     * @throws UnsupportedOperationException 
     *                  if this operation is not implemented.
     */
    public void clear() {
        Iterator<E> it = iterator();
        while (it.hasNext()) {
            it.next();
            it.remove();
        }
    }

    /**
     * Searches this Collection for the specified object.
     * 
     * @param object
     *            the object to search for
     * @return true if <code>object</code> is an element of this Collection,
     *         false otherwise
     */
    public boolean contains(Object object) {
        Iterator<E> it = iterator();
        if (object != null) {
            while (it.hasNext()) {
                if (object.equals(it.next())) {
                    return true;
                }
            }
        } else {
            while (it.hasNext()) {
                if (it.next() == null) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Searches this Collection for all objects in the specified Collection.
     * 
     * @param collection
     *            the Collection of objects
     * @return true if all objects in the specified Collection are elements of
     *         this Collection, false otherwise
     * @throws NullPointerException
     *                if null is used to invoke this method
     */
    public boolean containsAll(Collection<?> collection) {
        Iterator<?> it = collection.iterator();
        while (it.hasNext()) {
            if (!contains(it.next())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns true if the collection has no element, otherwise false.
     * 
     * @return true if the collection has no element.
     */
    public boolean isEmpty() {
        return size() == 0;
    }

    /**
     * Returns an Iterator on the elements of this Collection. A subclass must
     * implement the abstract methods iterator() and size().
     * 
     * @return an Iterator on the elements of this Collection
     * 
     * @see Iterator
     */
    public abstract Iterator<E> iterator();

    /**
     * Removes the first occurrence of the specified object from this
     * Collection. This operation traverses over the collection, looking
     * for the specified object. Once the object is found, the object will
     * be removed from the collection using the iterator's remove method. 
     * 
     * This collection will throw an UnsupportedOperationException if the 
     * iterator returned does not implement remove method, and the specified
     * object is in this collection.
     * 
     * @param object
     *            the object to remove
     * @return true if this Collection is modified, false otherwise
     * 
     * @throws UnsupportedOperationException
     *                when removing from this Collection is not supported
     */
    public boolean remove(Object object) {
        Iterator<?> it = iterator();
        if (object != null) {
            while (it.hasNext()) {
                if (object.equals(it.next())) {
                    it.remove();
                    return true;
                }
            }
        } else {
            while (it.hasNext()) {
                if (it.next() == null) {
                    it.remove();
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Removes all occurrences in this Collection of each object in the
     * specified Collection. This operation traverses over the collection
     * itself, to verify whether each element is contained in the specified 
     * collection. The object will be removed from the collection itself using 
     * the iterator's remove method if it is contained in the specified 
     * collection. 
     * 
     * This collection will throw an UnsupportedOperationException if the 
     * iterator returned does not implement remove method, and the element 
     * in the specified collection is contained in this collection.
     * 
     * @param collection
     *            the Collection of objects to remove
     * @return true if this Collection is modified, false otherwise
     * 
     * @throws UnsupportedOperationException
     *                when removing from this Collection is not supported
     * @throws NullPointerException
     *                if null is used to invoke this method
     */
    public boolean removeAll(Collection<?> collection) {
        boolean result = false;
        Iterator<?> it = iterator();
        while (it.hasNext()) {
            if (collection.contains(it.next())) {
                it.remove();
                result = true;
            }
        }
        return result;
    }

    /**
     * Removes all objects from this Collection that are not contained in the
     * specified Collection. This operation traverses over the collection
     * itself, to verify whether any element is contained in the specified 
     * collection. The object will be removed from the collection itself using 
     * the iterator's remove method if it is not contained in the specified 
     * collection. 
     * 
     * This collection will throw an UnsupportedOperationException if the 
     * iterator returned does not implement remove method, and the collection
     * itself does contain elements which do not exist in the specified collection.
     * 
     * @param collection
     *            the Collection of objects to retain
     * @return true if this Collection is modified, false otherwise
     * 
     * @throws UnsupportedOperationException
     *                when removing from this Collection is not supported
     * @throws NullPointerException
     *                if null is used to invoke this method
     */
    public boolean retainAll(Collection<?> collection) {
        boolean result = false;
        Iterator<?> it = iterator();
        while (it.hasNext()) {
            if (!collection.contains(it.next())) {
                it.remove();
                result = true;
            }
        }
        return result;
    }

    /**
     * Returns the number of elements in this Collection.
     * 
     * @return the number of elements in this Collection
     */
    public abstract int size();

    /**
     * Returns a new array containing all elements contained in this Collection.
     * All the elements in the array will not be referenced by the collection.
     * The elements in the returned array will be sorted to the same order as 
     * those returned by the iterator of this collection itself if the collection  
     * guarantees the order. 
     * 
     * @return an array of the elements from this Collection
     */
    public Object[] toArray() {
        int size = size(), index = 0;
        Iterator<?> it = iterator();
        Object[] array = new Object[size];
        while (index < size) {
            array[index++] = it.next();
        }
        return array;
    }

    /**
     * Returns an array containing all elements contained in this Collection. If
     * the specified array is large enough to hold the elements, the specified
     * array is used, otherwise an array of the same type is created. If the
     * specified array is used and is larger than this Collection, the array
     * element following the collection elements is set to null.
     * 
     * @param contents
     *            the array
     * @return an array of the elements from this Collection
     * 
     * @throws ArrayStoreException
     *                when the type of an element in this Collection cannot be
     *                stored in the type of the specified array
     * @throws NullPointerException
     *                if null is used to invoke this method
     */
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] contents) {
        int size = size(), index = 0;
        if (size > contents.length) {
            Class<?> ct = contents.getClass().getComponentType();
            contents = (T[])Array.newInstance(ct, size);
        }
        for (E entry: this) {
            contents[index++] = (T)entry;
        }
        if (index < contents.length) {
            contents[index] = null;
        }
        return contents;
    }

    /**
     * Returns the string representation of this Collection. The presentation
     * has a specific format. It is enclosed by square brackets ("[]"). Elements
     * are separated by ', ' (comma and space).
     * 
     * @return the string representation of this Collection
     */
    @Override
    public String toString() {
        if (isEmpty()) {
            return "[]"; //$NON-NLS-1$
        }

        StringBuilder buffer = new StringBuilder(size() * 16);
        buffer.append('[');
        Iterator<?> it = iterator();
        while (it.hasNext()) {
            Object next = it.next();
            if (next != this) {
                buffer.append(next);
            } else {
                buffer.append("(this Collection)"); //$NON-NLS-1$
            }
            if(it.hasNext()) {
                buffer.append(", "); //$NON-NLS-1$
            }
        }
        buffer.append(']');
        return buffer.toString();
    }
}
