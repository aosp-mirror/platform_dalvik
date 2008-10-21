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
 * Dictionary is a abstract class which is the superclass of all classes that
 * associate keys with values, such as Hashtable.
 * 
 * @see Hashtable
 * @since 1.0
 */
public abstract class Dictionary<K,V> {
    /**
     * Constructs a new instance of this class.
     * 
     */
    public Dictionary() {
        super();
    }

    /**
     * Returns an Enumeration on the elements of this Dictionary.
     * 
     * @return an Enumeration of the values of this Dictionary
     * 
     * @see #keys
     * @see #size
     * @see Enumeration
     */
    public abstract Enumeration<V> elements();

    /**
     * Returns the value associated with <code>key</code>.
     * 
     * @param key
     *            the key of the value returned
     * @return the value associated with <code>key</code> or <code>null</code>
     *         if the specified key does not exist
     * 
     * @see #put
     */
    public abstract V get(Object key);

    /**
     * Returns if this Dictionary has no key/value pairs, a size of zero.
     * 
     * @return true if this Dictionary has no key/value pairs, false otherwise
     * 
     * @see #size
     */
    public abstract boolean isEmpty();

    /**
     * Returns an Enumeration on the keys of this Dictionary.
     * 
     * @return an Enumeration of the keys of this Dictionary
     * 
     * @see #elements
     * @see #size
     * @see Enumeration
     */
    public abstract Enumeration<K> keys();

    /**
     * Associate <code>key</code> with <code>value</code> in this
     * <code>Dictionary</code>. If <code>key</code> exists in the
     * <code>Dictionary</code> prior to this call being made, the old value is
     * replaced.
     * 
     * @param key
     *            the key to add
     * @param value
     *            the value to add
     * @return the old value previously associated with <code>key</code> or
     *         <code>null</code> if <code>key</code> is new to the
     *         <code>Dictionary</code>.
     * 
     * @see #elements
     * @see #get
     * @see #keys
     */
    public abstract V put(K key, V value);

    /**
     * Remove the key/value pair with the specified <code>key</code> from this
     * <code>Dictionary</code>.
     * 
     * @param key
     *            the key to remove
     * @return the associated value or else <code>null</code> if
     *         <code>key</code> is not known to this <code>Dictionary</code>
     * 
     * @see #get
     * @see #put
     */
    public abstract V remove(Object key);

    /**
     * Returns the number of key/value pairs in this Dictionary.
     * 
     * @return the number of key/value pairs in this Dictionary
     * 
     * @see #elements
     * @see #keys
     */
    public abstract int size();
}
