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
 * SortedSet is a Set which iterates its elements in sorted order.
 */
public interface SortedSet<E> extends Set<E> {
    
    /**
     * Returns the Comparator used to compare elements in this SortedSet.
     * 
     * @return a Comparator or null if the natural order is used
     */
    public Comparator<? super E> comparator();

    /**
     * Answer the first sorted element in this SortedSet.
     * 
     * @return the first sorted element
     * 
     * @exception NoSuchElementException
     *                when this SortedSet is empty
     */
    public E first();

    /**
     * Returns a SortedSet of the specified portion of this SortedSet which
     * contains elements less than the end element. The returned SortedSet is
     * backed by this SortedSet so changes to one are reflected by the other.
     * 
     * @param end
     *            the end element
     * @return a subset where the elements are less than <code>end</code>
     * 
     * @exception ClassCastException
     *                when the class of the end element is inappropriate for
     *                this SubSet
     * @exception NullPointerException
     *                when the end element is null and this SortedSet does not
     *                support null elements
     */
    public SortedSet<E> headSet(E end);

    /**
     * Answer the last sorted element in this SortedSet.
     * 
     * @return the last sorted element
     * 
     * @exception NoSuchElementException
     *                when this SortedSet is empty
     */
    public E last();

    /**
     * Returns a SortedSet of the specified portion of this SortedSet which
     * contains elements greater or equal to the start element but less than the
     * end element. The returned SortedSet is backed by this SortedMap so
     * changes to one are reflected by the other.
     * 
     * @param start
     *            the start element
     * @param end
     *            the end element
     * @return a subset where the elements are greater or equal to
     *         <code>start</code> and less than <code>end</code>
     * 
     * @exception ClassCastException
     *                when the class of the start or end element is
     *                inappropriate for this SubSet
     * @exception NullPointerException
     *                when the start or end element is null and this SortedSet
     *                does not support null elements
     * @exception IllegalArgumentException
     *                when the start element is greater than the end element
     */
    public SortedSet<E> subSet(E start, E end);

    /**
     * Returns a SortedSet of the specified portion of this SortedSet which
     * contains elements greater or equal to the start element. The returned
     * SortedSet is backed by this SortedSet so changes to one are reflected by
     * the other.
     * 
     * @param start
     *            the start element
     * @return a subset where the elements are greater or equal to
     *         <code>start</code>
     * 
     * @exception ClassCastException
     *                when the class of the start element is inappropriate for
     *                this SubSet
     * @exception NullPointerException
     *                when the start element is null and this SortedSet does not
     *                support null elements
     */
    public SortedSet<E> tailSet(E start);
}
