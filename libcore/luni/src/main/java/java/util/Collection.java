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
 * Collection is the root of the collection hierarchy.
 */
public interface Collection<E> extends Iterable<E> {

    /**
     * Attempts to add <code>object</code> to the contents of this
     * <code>Collection</code>.
     * 
     * @param object
     *            the object to add
     * @return <code>true</code> if this <code>Collection</code> is
     *         modified, <code>false</code> otherwise
     * 
     * @exception UnsupportedOperationException
     *                when adding to this Collection is not supported
     * @exception ClassCastException
     *                when the class of the object is inappropriate for this
     *                Collection
     * @exception IllegalArgumentException
     *                when the object cannot be added to this Collection
     */
    public boolean add(E object);

    /**
     * Attempts to add all of the objects contained in <code>collection</code>
     * to the contents of this collection.
     * 
     * @param collection
     *            the Collection of objects
     * @return true if this Collection is modified, false otherwise
     * 
     * @exception UnsupportedOperationException
     *                when adding to this Collection is not supported
     * @exception ClassCastException
     *                when the class of an object is inappropriate for this
     *                Collection
     * @exception IllegalArgumentException
     *                when an object cannot be added to this Collection
     */
    public boolean addAll(Collection<? extends E> collection);

    /**
     * Removes all elements from this Collection, leaving it empty.
     * 
     * @exception UnsupportedOperationException
     *                when removing from this Collection is not supported
     * 
     * @see #isEmpty
     * @see #size
     */
    public void clear();

    /**
     * Searches this Collection for the specified object.
     * 
     * @param object
     *            the object to search for
     * @return true if object is an element of this Collection, false otherwise
     */
    public boolean contains(Object object);

    /**
     * Searches this Collection for all objects in the specified Collection.
     * 
     * @param collection
     *            the Collection of objects
     * @return true if all objects in the specified Collection are elements of
     *         this Collection, false otherwise
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
     * Returns an integer hash code for the receiver. Objects which are equal
     * answer the same value for this method.
     * 
     * @return the receiver's hash
     * 
     * @see #equals
     */
    public int hashCode();

    /**
     * Returns if this Collection has no elements, a size of zero.
     * 
     * @return true if this Collection has no elements, false otherwise
     * 
     * @see #size
     */
    public boolean isEmpty();

    /**
     * Returns an instance of {@link Iterator} that may be used to access the
     * objects contained by this collection.
     * 
     * @return an iterator for accessing the collection contents
     */
    public Iterator<E> iterator();

    /**
     * Removes the first occurrence of the specified object from this
     * Collection.
     * 
     * @param object
     *            the object to remove
     * @return true if this Collection is modified, false otherwise
     * 
     * @exception UnsupportedOperationException
     *                when removing from this Collection is not supported
     */
    public boolean remove(Object object);

    /**
     * Removes all occurrences in this Collection of each object in the
     * specified Collection.
     * 
     * @param collection
     *            the Collection of objects to remove
     * @return true if this Collection is modified, false otherwise
     * 
     * @exception UnsupportedOperationException
     *                when removing from this Collection is not supported
     */
    public boolean removeAll(Collection<?> collection);

    /**
     * Removes all objects from this Collection that are not also found in the
     * contents of <code>collection</code>.
     * 
     * @param collection
     *            the Collection of objects to retain
     * @return true if this Collection is modified, false otherwise
     * 
     * @exception UnsupportedOperationException
     *                when removing from this Collection is not supported
     */
    public boolean retainAll(Collection<?> collection);

    /**
     * Returns a count of how many objects are contained by this collection.
     * 
     * @return how many objects are contained by this collection
     */
    public int size();

    /**
     * Returns a new array containing all elements contained in this Collection.
     * 
     * @return an array of the elements from this Collection
     */
    public Object[] toArray();

    /**
     * Returns an array containing all elements contained in this Collection. If
     * the specified array is large enough to hold the elements, the specified
     * array is used, otherwise an array of the same type is created. If the
     * specified array is used and is larger than this Collection, the array
     * element following the collection elements is set to null.
     * 
     * @param array
     *            the array
     * @return an array of the elements from this Collection
     * 
     * @exception ArrayStoreException
     *                when the type of an element in this Collection cannot be
     *                stored in the type of the specified array
     */
    public <T> T[] toArray(T[] array);
}
