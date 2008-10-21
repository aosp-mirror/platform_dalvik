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
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Array;

/**
 * Vector is a variable size contiguous indexable array of Objects. The size of
 * the Vector is the number of Objects it contains. The capacity of the Vector
 * is the number of Objects it can hold.
 * <p>
 * Objects may be inserted at any position up to the size of the Vector,
 * increasing the size of the Vector. Objects at any position in the Vector may
 * be removed, shrinking the size of the Vector. Objects at any position in the
 * Vector may be replaced, which does not affect the Vector size.
 * <p>
 * The capacity of a Vector may be specified when the Vector is created. If the
 * capacity of the Vector is exceeded, the capacity is increased, doubling by
 * default.
 * 
 * @see java.lang.StringBuffer
 */
public class Vector<E> extends AbstractList<E> implements List<E>, RandomAccess,
        Cloneable, Serializable {
    
    private static final long serialVersionUID = -2767605614048989439L;

    /**
     * The number of elements or the size of the vector.
     */
    protected int elementCount;

    /**
     * The elements of the vector.
     */
    protected Object[] elementData;

    /**
     * How many elements should be added to the vector when it is detected that
     * it needs to grow to accommodate extra entries.
     */
    protected int capacityIncrement;

    private static final int DEFAULT_SIZE = 10;

    /**
     * Constructs a new Vector using the default capacity.
     */
    public Vector() {
        this(DEFAULT_SIZE, 0);
    }

    /**
     * Constructs a new Vector using the specified capacity.
     * 
     * @param capacity
     *            the initial capacity of the new vector
     */
    public Vector(int capacity) {
        this(capacity, 0);
    }

    /**
     * Constructs a new Vector using the specified capacity and capacity
     * increment.
     * 
     * @param capacity
     *            the initial capacity of the new Vector
     * @param capacityIncrement
     *            the amount to increase the capacity when this Vector is full
     */
    public Vector(int capacity, int capacityIncrement) {
        elementCount = 0;
        try {
            elementData = newElementArray(capacity);
        } catch (NegativeArraySizeException e) {
            throw new IllegalArgumentException();
        }
        this.capacityIncrement = capacityIncrement;
    }

    /**
     * Constructs a new instance of <code>Vector</code> containing the
     * elements in <code>collection</code>. The order of the elements in the
     * new <code>Vector</code> is dependent on the iteration order of the seed
     * collection.
     * 
     * @param collection
     *            the collection of elements to add
     */
    public Vector(Collection<? extends E> collection) {
        this(collection.size(), 0);
        Iterator<? extends E> it = collection.iterator();
        while (it.hasNext()) {
            elementData[elementCount++] = it.next();
        }
    }
    
    @SuppressWarnings("unchecked")
    private E[] newElementArray(int size) {
        return (E[])new Object[size];
    }

    /**
     * Adds the specified object into this Vector at the specified location. The
     * object is inserted before any previous element at the specified location.
     * If the location is equal to the size of this Vector, the object is added
     * at the end.
     * 
     * @param location
     *            the index at which to insert the element
     * @param object
     *            the object to insert in this Vector
     * 
     * @exception ArrayIndexOutOfBoundsException
     *                when <code>location < 0 || > size()</code>
     * 
     * @see #addElement
     * @see #size
     */
    @Override
    public void add(int location, E object) {
        insertElementAt(object, location);
    }

    /**
     * Adds the specified object at the end of this Vector.
     * 
     * @param object
     *            the object to add to the Vector
     * @return true
     */
    @Override
    public boolean add(E object) {
        addElement(object);
        return true;
    }

    /**
     * Inserts the objects in the specified Collection at the specified location
     * in this Vector. The objects are inserted in the order in which they are
     * returned from the Collection iterator.
     * 
     * @param location
     *            the location to insert the objects
     * @param collection
     *            the Collection of objects
     * @return true if this Vector is modified, false otherwise
     * 
     * @exception ArrayIndexOutOfBoundsException
     *                when <code>location < 0</code> or
     *                <code>location > size()</code>
     */
    @Override
    public synchronized boolean addAll(int location, Collection<? extends E> collection) {
        if (0 <= location && location <= elementCount) {
            int size = collection.size();
            if (size == 0) {
                return false;
            }
            int required = size - (elementData.length - elementCount);
            if (required > 0) {
                growBy(required);
            }
            int count = elementCount - location;
            if (count > 0) {
                System.arraycopy(elementData, location, elementData, location
                        + size, count);
            }
            Iterator<? extends E> it = collection.iterator();
            while (it.hasNext()) {
                elementData[location++] = it.next();
            }
            elementCount += size;
            modCount++;
            return true;
        }
        throw new ArrayIndexOutOfBoundsException(location);
    }

    /**
     * Adds the objects in the specified Collection to the end of this Vector.
     * 
     * @param collection
     *            the Collection of objects
     * @return true if this Vector is modified, false otherwise
     */
    @Override
    public synchronized boolean addAll(Collection<? extends E> collection) {
        return addAll(elementCount, collection);
    }

    /**
     * Adds the specified object at the end of this Vector.
     * 
     * @param object
     *            the object to add to the Vector
     */
    public synchronized void addElement(E object) {
        if (elementCount == elementData.length) {
            growByOne();
        }
        elementData[elementCount++] = object;
        modCount++;
    }

    /**
     * Returns the number of elements this Vector can hold without growing.
     * 
     * @return the capacity of this Vector
     * 
     * @see #ensureCapacity
     * @see #size
     */
    public synchronized int capacity() {
        return elementData.length;
    }

    /**
     * Removes all elements from this Vector, leaving it empty.
     * 
     * @see #isEmpty
     * @see #size
     */
    @Override
    public void clear() {
        removeAllElements();
    }

    /**
     * Returns a new Vector with the same elements, size, capacity and capacity
     * increment as this Vector.
     * 
     * @return a shallow copy of this Vector
     * 
     * @see java.lang.Cloneable
     */
    @Override
    @SuppressWarnings("unchecked")
    public synchronized Object clone() {
        try {
            Vector<E> vector = (Vector<E>) super.clone();
            vector.elementData = elementData.clone();
            return vector;
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    /**
     * Searches this Vector for the specified object.
     * 
     * @param object
     *            the object to look for in this Vector
     * @return true if object is an element of this Vector, false otherwise
     * 
     * @see #indexOf(Object)
     * @see #indexOf(Object, int)
     * @see java.lang.Object#equals
     */
    @Override
    public boolean contains(Object object) {
        return indexOf(object, 0) != -1;
    }

    /**
     * Searches this Vector for all objects in the specified Collection.
     * 
     * @param collection
     *            the Collection of objects
     * @return true if all objects in the specified Collection are elements of
     *         this Vector, false otherwise
     */
    @Override
    public synchronized boolean containsAll(Collection<?> collection) {
        return super.containsAll(collection);
    }

    /**
     * Attempts to copy elements contained by this <code>Vector</code> into
     * the corresponding elements of the supplied <code>Object</code> array.
     * 
     * @param elements
     *            the <code>Object</code> array into which the elements of
     *            this Vector are copied
     * 
     * @see #clone
     */
    public synchronized void copyInto(Object[] elements) {
        System.arraycopy(elementData, 0, elements, 0, elementCount);
    }

    /**
     * Returns the element at the specified location in this Vector.
     * 
     * @param location
     *            the index of the element to return in this Vector
     * @return the element at the specified location
     * 
     * @exception ArrayIndexOutOfBoundsException
     *                when <code>location < 0 || >= size()</code>
     * 
     * @see #size
     */
    public synchronized E elementAt(int location) {
        if (location < elementCount) {
            return (E)elementData[location];
        }
        throw new ArrayIndexOutOfBoundsException(location);
    }

    /**
     * Returns an Enumeration on the elements of this Vector. The results of the
     * Enumeration may be affected if the contents of this Vector are modified.
     * 
     * @return an Enumeration of the elements of this Vector
     * 
     * @see #elementAt
     * @see Enumeration
     */
    public Enumeration<E> elements() {
        return new Enumeration<E>() {
            int pos = 0;

            public boolean hasMoreElements() {
                synchronized (Vector.this) {
                    return pos < elementCount;
                }
            }

            public E nextElement() {
                synchronized (Vector.this) {
                    if (pos < elementCount) {
                        return (E)elementData[pos++];
                    }
                }
                throw new NoSuchElementException();
            }
        };
    }

    /**
     * Ensures that this Vector can hold the specified number of elements
     * without growing.
     * 
     * @param minimumCapacity
     *            the minimum number of elements that this vector will hold
     *            before growing
     * 
     * @see #capacity
     */
    public synchronized void ensureCapacity(int minimumCapacity) {
        if (elementData.length < minimumCapacity) {
            int next = (capacityIncrement <= 0 ? elementData.length
                    : capacityIncrement)
                    + elementData.length;
            grow(minimumCapacity > next ? minimumCapacity : next);
        }
    }

    /**
     * Compares the specified object to this Vector and answer if they are
     * equal. The object must be a List which contains the same objects in the
     * same order.
     * 
     * @param object
     *            the object to compare with this object
     * @return true if the specified object is equal to this Vector, false
     *         otherwise
     * 
     * @see #hashCode
     */
    @Override
    public synchronized boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object instanceof List) {
            List<?> list = (List) object;
            if (list.size() != size()) {
                return false;
            }

            int index = 0;
            Iterator<?> it = list.iterator();
            while (it.hasNext()) {
                Object e1 = elementData[index++], e2 = it.next();
                if (!(e1 == null ? e2 == null : e1.equals(e2))) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Returns the first element in this Vector.
     * 
     * @return the element at the first position
     * 
     * @exception NoSuchElementException
     *                when this vector is empty
     * 
     * @see #elementAt
     * @see #lastElement
     * @see #size
     */
    public synchronized E firstElement() {
        if (elementCount > 0) {
            return (E)elementData[0];
        }
        throw new NoSuchElementException();
    }

    /**
     * Returns the element at the specified location in this Vector.
     * 
     * @param location
     *            the index of the element to return in this Vector
     * @return the element at the specified location
     * 
     * @exception ArrayIndexOutOfBoundsException
     *                when <code>location < 0 || >= size()</code>
     * 
     * @see #size
     */
    @Override
    public synchronized E get(int location) {
        return elementAt(location);
    }

    private void grow(int newCapacity) {
        E[] newData = newElementArray(newCapacity);
        // Assumes elementCount is <= newCapacity
        assert elementCount <= newCapacity;
        System.arraycopy(elementData, 0, newData, 0, elementCount); 
        elementData = newData;
    }

    /**
     * JIT optimization
     */
    private void growByOne() {
        int adding = 0;
        if (capacityIncrement <= 0) {
            if ((adding = elementData.length) == 0) {
                adding = 1;
            }
        } else {
            adding = capacityIncrement;
        }

        E[] newData = newElementArray(elementData.length + adding);
        System.arraycopy(elementData, 0, newData, 0, elementCount);
        elementData = newData;
    }

    private void growBy(int required) {
        int adding = 0;
        if (capacityIncrement <= 0) {
            if ((adding = elementData.length) == 0) {
                adding = required;
            }
            while (adding < required) {
                adding += adding;
            }
        } else {
            adding = (required / capacityIncrement) * capacityIncrement;
            if (adding < required) {
                adding += capacityIncrement;
            }
        }
        E[] newData = newElementArray(elementData.length + adding);
        System.arraycopy(elementData, 0, newData, 0, elementCount);
        elementData = newData;
    }

    /**
     * Returns an integer hash code for the receiver. Objects which are equal
     * answer the same value for this method.
     * 
     * @return the receiver's hash
     * 
     * @see #equals
     */
    @Override
    public synchronized int hashCode() {
        int result = 1;
        for (int i = 0; i < elementCount; i++) {
            result = (31 * result)
                    + (elementData[i] == null ? 0 : elementData[i].hashCode());
        }
        return result;
    }

    /**
     * Searches in this Vector for the index of the specified object. The search
     * for the object starts at the beginning and moves towards the end of this
     * Vector.
     * 
     * @param object
     *            the object to find in this Vector
     * @return the index in this Vector of the specified element, -1 if the
     *         element isn't found
     * 
     * @see #contains
     * @see #lastIndexOf(Object)
     * @see #lastIndexOf(Object, int)
     */
    @Override
    public int indexOf(Object object) {
        return indexOf(object, 0);
    }

    /**
     * Searches in this Vector for the index of the specified object. The search
     * for the object starts at the specified location and moves towards the end
     * of this Vector.
     * 
     * @param object
     *            the object to find in this Vector
     * @param location
     *            the index at which to start searching
     * @return the index in this Vector of the specified element, -1 if the
     *         element isn't found
     * 
     * @exception ArrayIndexOutOfBoundsException
     *                when <code>location < 0</code>
     * 
     * @see #contains
     * @see #lastIndexOf(Object)
     * @see #lastIndexOf(Object, int)
     */
    public synchronized int indexOf(Object object, int location) {
        if (object != null) {
            for (int i = location; i < elementCount; i++) {
                if (object.equals(elementData[i])) {
                    return i;
                }
            }
        } else {
            for (int i = location; i < elementCount; i++) {
                if (elementData[i] == null) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * Inserts the specified object into this Vector at the specified location.
     * This object is inserted before any previous element at the specified
     * location. If the location is equal to the size of this Vector, the object
     * is added at the end.
     * 
     * @param object
     *            the object to insert in this Vector
     * @param location
     *            the index at which to insert the element
     * 
     * @exception ArrayIndexOutOfBoundsException
     *                when <code>location < 0 || > size()</code>
     * 
     * @see #addElement
     * @see #size
     */
    public synchronized void insertElementAt(E object, int location) {
        if (0 <= location && location <= elementCount) {
            if (elementCount == elementData.length) {
                growByOne();
            }
            int count = elementCount - location;
            if (count > 0) {
                System.arraycopy(elementData, location, elementData,
                        location + 1, count);
            }
            elementData[location] = object;
            elementCount++;
            modCount++;
        } else {
            throw new ArrayIndexOutOfBoundsException(location);
        }
    }

    /**
     * Returns if this Vector has no elements, a size of zero.
     * 
     * @return true if this Vector has no elements, false otherwise
     * 
     * @see #size
     */
    @Override
    public synchronized boolean isEmpty() {
        return elementCount == 0;
    }

    /**
     * Returns the last element in this Vector.
     * 
     * @return the element at the last position
     * 
     * @exception NoSuchElementException
     *                when this vector is empty
     * 
     * @see #elementAt
     * @see #firstElement
     * @see #size
     */
    public synchronized E lastElement() {
        try {
            return (E)elementData[elementCount - 1];
        } catch (IndexOutOfBoundsException e) {
            throw new NoSuchElementException();
        }
    }

    /**
     * Searches in this Vector for the index of the specified object. The search
     * for the object starts at the end and moves towards the start of this
     * Vector.
     * 
     * @param object
     *            the object to find in this Vector
     * @return the index in this Vector of the specified element, -1 if the
     *         element isn't found
     * 
     * @see #contains
     * @see #indexOf(Object)
     * @see #indexOf(Object, int)
     */
    @Override
    public synchronized int lastIndexOf(Object object) {
        return lastIndexOf(object, elementCount - 1);
    }

    /**
     * Searches in this Vector for the index of the specified object. The search
     * for the object starts at the specified location and moves towards the
     * start of this Vector.
     * 
     * @param object
     *            the object to find in this Vector
     * @param location
     *            the index at which to start searching
     * @return the index in this Vector of the specified element, -1 if the
     *         element isn't found
     * 
     * @exception ArrayIndexOutOfBoundsException
     *                when <code>location >= size()</code>
     * 
     * @see #contains
     * @see #indexOf(Object)
     * @see #indexOf(Object, int)
     */
    public synchronized int lastIndexOf(Object object, int location) {
        if (location < elementCount) {
            if (object != null) {
                for (int i = location; i >= 0; i--) {
                    if (object.equals(elementData[i])) {
                        return i;
                    }
                }
            } else {
                for (int i = location; i >= 0; i--) {
                    if (elementData[i] == null) {
                        return i;
                    }
                }
            }
            return -1;
        }
        throw new ArrayIndexOutOfBoundsException(location);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.List#remove(int)
     */
    @Override
    public synchronized E remove(int location) {
        if (location < elementCount) {
            E result = (E)elementData[location];
            elementCount--;
            int size = elementCount - location;
            if (size > 0) {
                System.arraycopy(elementData, location + 1, elementData,
                        location, size);
            }
            elementData[elementCount] = null;
            modCount++;
            return result;
        }
        throw new ArrayIndexOutOfBoundsException(location);
    }

    /**
     * Removes the first occurrence, starting at the beginning and moving
     * towards the end, of the specified object from this Vector.
     * 
     * @param object
     *            the object to remove from this Vector
     * @return true if the specified object was found, false otherwise
     * 
     * @see #removeAllElements
     * @see #removeElementAt
     * @see #size
     */
    @Override
    public boolean remove(Object object) {
        return removeElement(object);
    }

    /**
     * Removes all occurrences in this Vector of each object in the specified
     * Collection.
     * 
     * @param collection
     *            the Collection of objects to remove
     * @return true if this Vector is modified, false otherwise
     */
    @Override
    public synchronized boolean removeAll(Collection<?> collection) {
        return super.removeAll(collection);
    }

    /**
     * Removes all elements from this Vector, leaving the size zero and the
     * capacity unchanged.
     * 
     * @see #isEmpty
     * @see #size
     */
    public synchronized void removeAllElements() {
        Arrays.fill(elementData, 0, elementCount, null);
        modCount++;
        elementCount = 0;
    }

    /**
     * Removes the first occurrence, starting at the beginning and moving
     * towards the end, of the specified object from this Vector.
     * 
     * @param object
     *            the object to remove from this Vector
     * @return true if the specified object was found, false otherwise
     * 
     * @see #removeAllElements
     * @see #removeElementAt
     * @see #size
     */
    public synchronized boolean removeElement(Object object) {
        int index;
        if ((index = indexOf(object, 0)) == -1) {
            return false;
        }
        removeElementAt(index);
        return true;
    }

    /**
     * Removes the element found at index position <code>location</code> from
     * this <code>Vector</code> and decrements the size accordingly.
     * 
     * @param location
     *            the index of the element to remove
     * 
     * @exception ArrayIndexOutOfBoundsException
     *                when <code>location < 0 || >= size()</code>
     * 
     * @see #removeElement
     * @see #removeAllElements
     * @see #size
     */
    public synchronized void removeElementAt(int location) {
        if (0 <= location && location < elementCount) {
            elementCount--;
            int size = elementCount - location;
            if (size > 0) {
                System.arraycopy(elementData, location + 1, elementData,
                        location, size);
            }
            elementData[elementCount] = null;
            modCount++;
        } else {
            throw new ArrayIndexOutOfBoundsException(location);
        }
    }

    /**
     * Removes the objects in the specified range from the start to the, but not
     * including, end index.
     * 
     * @param start
     *            the index at which to start removing
     * @param end
     *            the index one past the end of the range to remove
     * 
     * @exception IndexOutOfBoundsException
     *                when <code>start < 0, start > end</code> or
     *                <code>end > size()</code>
     */
    @Override
    protected void removeRange(int start, int end) {
        if (start >= 0 && start <= end && end <= size()) {
            if (start == end) {
                return;
            }
            if (end != elementCount) {
                System.arraycopy(elementData, end, elementData, start,
                        elementCount - end);
                int newCount = elementCount - (end - start);
                Arrays.fill(elementData, newCount, elementCount, null);
                elementCount = newCount;
            } else {
                Arrays.fill(elementData, start, elementCount, null);
                elementCount = start;
            }
            modCount++;
        } else {
            throw new IndexOutOfBoundsException();
        }
    }

    /**
     * Removes all objects from this Vector that are not contained in the
     * specified Collection.
     * 
     * @param collection
     *            the Collection of objects to retain
     * @return true if this Vector is modified, false otherwise
     */
    @Override
    public synchronized boolean retainAll(Collection<?> collection) {
        return super.retainAll(collection);
    }

    /**
     * Replaces the element at the specified location in this Vector with the
     * specified object.
     * 
     * @param location
     *            the index at which to put the specified object
     * @param object
     *            the object to add to this Vector
     * @return the previous element at the location
     * 
     * @exception ArrayIndexOutOfBoundsException
     *                when <code>location < 0 || >= size()</code>
     * 
     * @see #size
     */
    @Override
    public synchronized E set(int location, E object) {
        if (location < elementCount) {
            E result = (E)elementData[location];
            elementData[location] = object;
            return result;
        }
        throw new ArrayIndexOutOfBoundsException(location);
    }

    /**
     * Replaces the element at the specified location in this Vector with the
     * specified object.
     * 
     * @param object
     *            the object to add to this Vector
     * @param location
     *            the index at which to put the specified object
     * 
     * @exception ArrayIndexOutOfBoundsException
     *                when <code>location < 0 || >= size()</code>
     * 
     * @see #size
     */
    public synchronized void setElementAt(E object, int location) {
        if (location < elementCount) {
            elementData[location] = object;
        } else {
            throw new ArrayIndexOutOfBoundsException(location);
        }
    }

    /**
     * Sets the size of this Vector to the specified size. If there are more
     * than length elements in this Vector, the elements at end are lost. If
     * there are less than length elements in the Vector, the additional
     * elements contain null.
     * 
     * @param length
     *            the new size of this Vector
     * 
     * @see #size
     */
    public synchronized void setSize(int length) {
        if (length == elementCount) {
            return;
        }
        ensureCapacity(length);
        if (elementCount > length) {
            Arrays.fill(elementData, length, elementCount, null);
        }
        elementCount = length;
        modCount++;
    }

    /**
     * Returns the number of elements in this Vector.
     * 
     * @return the number of elements in this Vector
     * 
     * @see #elementCount
     * @see #lastElement
     */
    @Override
    public synchronized int size() {
        return elementCount;
    }

    /**
     * Returns a List of the specified portion of this Vector from the start
     * index to one less than the end index. The returned List is backed by this
     * Vector so changes to one are reflected by the other.
     * 
     * @param start
     *            the index at which to start the sublist
     * @param end
     *            the index one past the end of the sublist
     * @return a List of a portion of this Vector
     * 
     * @exception IndexOutOfBoundsException
     *                when <code>start < 0 or <code>end > size()</code>
     * @exception    IllegalArgumentException when <code>start > end</code>
     */
    @Override
    public synchronized List<E> subList(int start, int end) {
        return new Collections.SynchronizedRandomAccessList<E>(
                super.subList(start, end), this);
    }

    /**
     * Returns a new array containing all elements contained in this Vector.
     * 
     * @return an array of the elements from this Vector
     */
    @Override
    public synchronized Object[] toArray() {
        Object[] result = new Object[elementCount];
        System.arraycopy(elementData, 0, result, 0, elementCount);
        return result;
    }

    /**
     * Returns an array containing all elements contained in this Vector. If the
     * specified array is large enough to hold the elements, the specified array
     * is used, otherwise an array of the same type is created. If the specified
     * array is used and is larger than this Vector, the array element following
     * the collection elements is set to null.
     * 
     * @param contents
     *            the array
     * @return an array of the elements from this Vector
     * 
     * @exception ArrayStoreException
     *                when the type of an element in this Vector cannot be
     *                stored in the type of the specified array
     */
    @Override
    @SuppressWarnings("unchecked")
    public synchronized <T> T[] toArray(T[] contents) {
        if (elementCount > contents.length) {
            Class<?> ct = contents.getClass().getComponentType();
            contents = (T[]) Array.newInstance(ct, elementCount);
        }
        System.arraycopy(elementData, 0, contents, 0, elementCount);
        if (elementCount < contents.length) {
            contents[elementCount] = null;
        }
        return contents;
    }

    /**
     * Returns the string representation of this Vector.
     * 
     * @return the string representation of this Vector
     * 
     * @see #elements
     */
    @Override
    public synchronized String toString() {
        if (elementCount == 0) {
            return "[]";
        }
        int length = elementCount - 1;
        StringBuffer buffer = new StringBuffer(size() * 16);
        buffer.append('[');
        for (int i = 0; i < length; i++) {
            if (elementData[i] == this) {
                buffer.append("(this Collection)");
            } else {
                buffer.append(elementData[i]);
            }
            buffer.append(", ");
        }
        if (elementData[length] == this) {
            buffer.append("(this Collection)");
        } else {
            buffer.append(elementData[length]);
        }
        buffer.append(']');
        return buffer.toString();
    }

    /**
     * Sets the capacity of this Vector to be the same as the size.
     * 
     * @see #capacity
     * @see #ensureCapacity
     * @see #size
     */
    public synchronized void trimToSize() {
        if (elementData.length != elementCount) {
            grow(elementCount);
        }
    }

    private synchronized void writeObject(ObjectOutputStream stream)
            throws IOException {
        stream.defaultWriteObject();
    }
}
