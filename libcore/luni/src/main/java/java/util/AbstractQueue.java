/* Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package java.util;

/**
 * 
 * An abstract class which gives out skeletal implementations for some methods
 * in Queue which include add, remove, and element that are based on offer,
 * poll, and peek except that they throw exception to indicate the occurrence of
 * some error instead of the return value of false or null.
 * 
 * @param <E>
 *            the type of the element in the collection.
 */
public abstract class AbstractQueue<E> extends AbstractCollection<E> implements
        Queue<E> {

    /**
     * Constructor for the sub classes.
     * 
     */
    protected AbstractQueue() {
        super();
    }

    /**
     * Adds an element to the queue.
     * 
     * @param o the element added to the queue.
     * @return true if the operation succeeds.
     * @throws NullPointerException if the element is null.
     * @throws IllegalStateException if the element is not allowed to be added
     *         to the queue.
     */
    @Override
    public boolean add(E o) {
        if (null == o) {
            throw new NullPointerException();
        }
        if (offer(o)) {
            return true;
        }
        throw new IllegalStateException();
    }

    /**
     * Adds all the elements of a collection to the queue. If the collection is
     * the queue itself, then an IllegalArgumentException will be thrown out. If
     * during the process, some runtime exception is thrown out, then part of
     * the elements in the collection that have successfully added will remain
     * in the queue.
     * 
     * The result of the method is undefined if the collection is modified
     * during the process of the method.
     * 
     * @param c the collection to be added to the queue.
     * @return true if the operation succeeds.
     * @throws NullPointerException if the collection or any element of it is
     *         null.
     * @throws IllegalArgumentException If the collection to be added to the
     *         queue is the queue itself.
     */
    @Override
    public boolean addAll(Collection<? extends E> c) {
        if (null == c) {
            throw new NullPointerException();
        }
        if (this == c) {
            throw new IllegalArgumentException();
        }
        return super.addAll(c);
    }

    /**
     * Gets and removes the element in the head of the queue.
     * 
     * @return the element in the head of the queue.
     * @throws NoSuchElementException if the queue is empty.
     */
    public E remove() {
        E o = poll();
        if (null == o) {
            throw new NoSuchElementException();
        }
        return o;
    }

    /**
     * Gets but not removes the element in the head of the queue.
     * 
     * @return the element in the head of the queue.
     * @throws NoSuchElementException if the queue is empty.
     */
    public E element() {
        E o = peek();
        if (null == o) {
            throw new NoSuchElementException();
        }
        return o;
    }

    /**
     * Removes all elements of the queue.
     */
    @Override
    public void clear() {
        E o;
        do {
            o = poll();
        } while (null != o);
    }
}
