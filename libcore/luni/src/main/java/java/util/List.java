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


/**
 * List is a collection which maintains an ordering for its elements. Every
 * element in the list has an index.
 */
public interface List<E> extends Collection<E> {
    /**
     * Inserts the specified object into this Vector at the specified location.
     * The object is inserted before any previous element at the specified
     * location. If the location is equal to the size of this List, the object
     * is added at the end.
     * 
     * @param location
     *            the index at which to insert
     * @param object
     *            the object to add
     * 
     * @exception UnsupportedOperationException
     *                when adding to this List is not supported
     * @exception ClassCastException
     *                when the class of the object is inappropriate for this
     *                List
     * @exception IllegalArgumentException
     *                when the object cannot be added to this List
     * @exception IndexOutOfBoundsException
     *                when <code>location < 0 || >= size()</code>
     */
    public void add(int location, E object);

    /**
     * Adds the specified object at the end of this List.
     * 
     * @param object
     *            the object to add
     * @return true
     * 
     * @exception UnsupportedOperationException
     *                when adding to this List is not supported
     * @exception ClassCastException
     *                when the class of the object is inappropriate for this
     *                List
     * @exception IllegalArgumentException
     *                when the object cannot be added to this List
     */
    public boolean add(E object);

    /**
     * Inserts the objects in the specified Collection at the specified location
     * in this List. The objects are added in the order they are returned from
     * the Collection iterator.
     * 
     * @param location
     *            the index at which to insert
     * @param collection
     *            the Collection of objects
     * @return true if this List is modified, false otherwise
     * 
     * @exception UnsupportedOperationException
     *                when adding to this List is not supported
     * @exception ClassCastException
     *                when the class of an object is inappropriate for this List
     * @exception IllegalArgumentException
     *                when an object cannot be added to this List
     * @exception IndexOutOfBoundsException
     *                when <code>location < 0 || >= size()</code>
     */
    public boolean addAll(int location, Collection<? extends E> collection);

    /**
     * Adds the objects in the specified Collection to the end of this List. The
     * objects are added in the order they are returned from the Collection
     * iterator.
     * 
     * @param collection
     *            the Collection of objects
     * @return true if this List is modified, false otherwise
     * 
     * @exception UnsupportedOperationException
     *                when adding to this List is not supported
     * @exception ClassCastException
     *                when the class of an object is inappropriate for this List
     * @exception IllegalArgumentException
     *                when an object cannot be added to this List
     */
    public boolean addAll(Collection<? extends E> collection);

    /**
     * Removes all elements from this List, leaving it empty.
     * 
     * @exception UnsupportedOperationException
     *                when removing from this List is not supported
     * 
     * @see #isEmpty
     * @see #size
     */
    public void clear();

    /**
     * Searches this List for the specified object.
     * 
     * @param object
     *            the object to search for
     * @return true if object is an element of this List, false otherwise
     */
    public boolean contains(Object object);

    /**
     * Searches this List for all objects in the specified Collection.
     * 
     * @param collection
     *            the Collection of objects
     * @return true if all objects in the specified Collection are elements of
     *         this List, false otherwise
     */
    public boolean containsAll(Collection<?> collection);

    /**
     * Compares the argument to the receiver, and returns true if they represent
     * the <em>same</em> object using a class specific comparison.
     * 
     * @param object
     *            Object the object to compare with this object.
     * @return boolean <code>true</code> if the object is the same as this
     *         object <code>false</code> if it is different from this object.
     * @see #hashCode
     */
    public boolean equals(Object object);

    /**
     * Returns the element at the specified location in this List.
     * 
     * @param location
     *            the index of the element to return
     * @return the element at the specified location
     * 
     * @exception IndexOutOfBoundsException
     *                when <code>location < 0 || >= size()</code>
     */
    public E get(int location);

    /**
     * Returns an integer hash code for the receiver. Objects which are equal
     * answer the same value for this method.
     * 
     * @return the receiver's hash
     * 
     * @see #equals
     */
    public int hashCode();

    /**
     * Searches this List for the specified object and returns the index of the
     * first occurrence.
     * 
     * @param object
     *            the object to search for
     * @return the index of the first occurrence of the object
     */
    public int indexOf(Object object);

    /**
     * Returns if this List has no elements, a size of zero.
     * 
     * @return true if this List has no elements, false otherwise
     * 
     * @see #size
     */
    public boolean isEmpty();

    /**
     * Returns an Iterator on the elements of this List. The elements are
     * iterated in the same order that they occur in the List.
     * 
     * @return an Iterator on the elements of this List
     * 
     * @see Iterator
     */
    public Iterator<E> iterator();

    /**
     * Searches this List for the specified object and returns the index of the
     * last occurrence.
     * 
     * @param object
     *            the object to search for
     * @return the index of the last occurrence of the object
     */
    public int lastIndexOf(Object object);

    /**
     * Returns a ListIterator on the elements of this List. The elements are
     * iterated in the same order that they occur in the List.
     * 
     * @return a ListIterator on the elements of this List
     * 
     * @see ListIterator
     */
    public ListIterator<E> listIterator();

    /**
     * Returns a ListIterator on the elements of this List. The elements are
     * iterated in the same order that they occur in the List. The iteration
     * starts at the specified location.
     * 
     * @param location
     *            the index at which to start the iteration
     * @return a ListIterator on the elements of this List
     * 
     * @exception IndexOutOfBoundsException
     *                when <code>location < 0 || >= size()</code>
     * 
     * @see ListIterator
     */
    public ListIterator<E> listIterator(int location);

    /**
     * Removes the object at the specified location from this List.
     * 
     * @param location
     *            the index of the object to remove
     * @return the removed object
     * 
     * @exception UnsupportedOperationException
     *                when removing from this List is not supported
     * @exception IndexOutOfBoundsException
     *                when <code>location < 0 || >= size()</code>
     */
    public E remove(int location);

    /**
     * Removes the first occurrence of the specified object from this List.
     * 
     * @param object
     *            the object to remove
     * @return true if this List is modified, false otherwise
     * 
     * @exception UnsupportedOperationException
     *                when removing from this List is not supported
     */
    public boolean remove(Object object);

    /**
     * Removes all occurrences in this List of each object in the specified
     * Collection.
     * 
     * @param collection
     *            the Collection of objects to remove
     * @return true if this List is modified, false otherwise
     * 
     * @exception UnsupportedOperationException
     *                when removing from this List is not supported
     */
    public boolean removeAll(Collection<?> collection);

    /**
     * Removes all objects from this List that are not contained in the
     * specified Collection.
     * 
     * @param collection
     *            the Collection of objects to retain
     * @return true if this List is modified, false otherwise
     * 
     * @exception UnsupportedOperationException
     *                when removing from this List is not supported
     */
    public boolean retainAll(Collection<?> collection);

    /**
     * Replaces the element at the specified location in this List with the
     * specified object.
     * 
     * @param location
     *            the index at which to put the specified object
     * @param object
     *            the object to add
     * @return the previous element at the index
     * 
     * @exception UnsupportedOperationException
     *                when replacing elements in this List is not supported
     * @exception ClassCastException
     *                when the class of an object is inappropriate for this List
     * @exception IllegalArgumentException
     *                when an object cannot be added to this List
     * @exception IndexOutOfBoundsException
     *                when <code>location < 0 || >= size()</code>
     */
    public E set(int location, E object);

    /**
     * Returns the number of elements in this List.
     * 
     * @return the number of elements in this List
     */
    public int size();

    /**
     * Returns a List of the specified portion of this List from the start index
     * to one less than the end index. The returned List is backed by this list
     * so changes to one are reflected by the other.
     * 
     * @param start
     *            the index at which to start the sublist
     * @param end
     *            the index one past the end of the sublist
     * @return a List of a portion of this List
     * 
     * @exception IndexOutOfBoundsException
     *                when <code>start < 0, start > end</code> or
     *                <code>end > size()</code>
     */
    public List<E> subList(int start, int end);

    /**
     * Returns an array containing all elements contained in this List.
     * 
     * @return an array of the elements from this List
     */
    public Object[] toArray();

    /**
     * Returns an array containing all elements contained in this List. If the
     * specified array is large enough to hold the elements, the specified array
     * is used, otherwise an array of the same type is created. If the specified
     * array is used and is larger than this List, the array element following
     * the collection elements is set to null.
     * 
     * @param array
     *            the array
     * @return an array of the elements from this List
     * 
     * @exception ArrayStoreException
     *                when the type of an element in this List cannot be stored
     *                in the type of the specified array
     */
    public <T> T[] toArray(T[] array);
}
