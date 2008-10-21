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
 * Map has a set of keys, each key is mapped to a single value.
 */
public interface Map<K,V> {
    /**
     * Map.Entry is a key/value mapping which is contained in a Map.
     * 
     */
    public abstract static interface Entry<K,V> {
        /**
         * Compares the specified object to this Map.Entry and answer if they
         * are equal. The object must be an instance of Map.Entry and have the
         * same key and value.
         * 
         * @param object
         *            the object to compare with this object
         * @return true if the specified object is equal to this Map.Entry,
         *         false otherwise
         * 
         * @see #hashCode
         */
        public boolean equals(Object object);

        /**
         * Gets the key.
         * 
         * @return the key
         */
        public K getKey();

        /**
         * Gets the value.
         * 
         * @return the value
         */
        public V getValue();

        /**
         * Returns an integer hash code for the receiver. Objects which are
         * equal answer the same value for this method.
         * 
         * @return the receiver's hash
         * 
         * @see #equals
         */
        public int hashCode();

        /**
         * Sets the value.
         * 
         * @param object
         *            the new value
         * @return object
         */
        public V setValue(V object);
    };

    /**
     * Removes all elements from this Map, leaving it empty.
     * 
     * @exception UnsupportedOperationException
     *                when removing from this Map is not supported
     * 
     * @see #isEmpty
     * @see #size
     */
    public void clear();

    /**
     * Searches this Map for the specified key.
     * 
     * @param key
     *            the object to search for
     * @return true if <code>key</code> is a key of this Map, false otherwise
     */
    public boolean containsKey(Object key);

    /**
     * Searches this Map for the specified value.
     * 
     * @param value
     *            the object to search for
     * @return true if <code>value</code> is a value of this Map, false
     *         otherwise
     */
    public boolean containsValue(Object value);

    /**
     * Returns a <code>Set</code> whose elements comprise all of the mappings
     * that are to be found in this <code>Map</code>. Information on each of
     * the mappings is encapsulated in a separate {@link Map.Entry} instance. As
     * the <code>Set</code> is backed by this <code>Map</code>, users
     * should be aware that changes in one will be immediately visible in the
     * other.
     * 
     * @return a <code>Set</code> of the mappings
     */
    public Set<Map.Entry<K,V>> entrySet();

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
     * Returns the value of the mapping with the specified key.
     * 
     * @param key
     *            the key
     * @return the value of the mapping with the specified key
     */
    public V get(Object key);

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
     * Returns if this Map has no elements, a size of zero.
     * 
     * @return true if this Map has no elements, false otherwise
     * 
     * @see #size
     */
    public boolean isEmpty();

    /**
     * Returns a Set of the keys contained in this Map. The set is backed by
     * this Map so changes to one are relected by the other. The set does not
     * support adding.
     * 
     * @return a Set of the keys
     */
    public Set<K> keySet();

    /**
     * Maps the specified key to the specified value.
     * 
     * @param key
     *            the key
     * @param value
     *            the value
     * @return the value of any previous mapping with the specified key or null
     *         if there was no mapping
     * 
     * @exception UnsupportedOperationException
     *                when adding to this Map is not supported
     * @exception ClassCastException
     *                when the class of the key or value is inappropriate for
     *                this Map
     * @exception IllegalArgumentException
     *                when the key or value cannot be added to this Map
     * @exception NullPointerException
     *                when the key or value is null and this Map does not
     *                support null keys or values
     */
    public V put(K key, V value);

    /**
     * Copies every mapping in the specified Map to this Map.
     * 
     * @param map
     *            the Map to copy mappings from
     * 
     * @exception UnsupportedOperationException
     *                when adding to this Map is not supported
     * @exception ClassCastException
     *                when the class of a key or value is inappropriate for this
     *                Map
     * @exception IllegalArgumentException
     *                when a key or value cannot be added to this Map
     * @exception NullPointerException
     *                when a key or value is null and this Map does not support
     *                null keys or values
     */
    public void putAll(Map<? extends K,? extends V> map);

    /**
     * Removes a mapping with the specified key from this Map.
     * 
     * @param key
     *            the key of the mapping to remove
     * @return the value of the removed mapping or null if key is not a key in
     *         this Map
     * 
     * @exception UnsupportedOperationException
     *                when removing from this Map is not supported
     */
    public V remove(Object key);

    /**
     * Returns the number of elements in this Map.
     * 
     * @return the number of elements in this Map
     */
    public int size();

    /**
     * Returns all of the current <code>Map</code> values in a
     * <code>Collection</code>. As the returned <code>Collection</code> is
     * backed by this <code>Map</code>, users should be aware that changes in
     * one will be immediately visible in the other.
     * 
     * @return a Collection of the values
     */
    public Collection<V> values();
}
