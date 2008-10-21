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
 * AbstractMap is an abstract implementation of the Map interface. This
 * Implementation does not support adding. A subclass must implement the
 * abstract method entrySet().
 * 
 * @since 1.2
 */
public abstract class AbstractMap<K, V> implements Map<K, V> {

    // Lazily initialized key set.
    Set<K> keySet;

    Collection<V> valuesCollection;

    /**
     * Constructs a new instance of this AbstractMap.
     */
    protected AbstractMap() {
        super();
    }

    /**
     * Removes all elements from this Map, leaving it empty.
     * 
     * @exception UnsupportedOperationException
     *                when removing from this Map is not supported
     * 
     * @see #isEmpty
     * @see #size
     */
    public void clear() {
        entrySet().clear();
    }

    /**
     * Searches this Map for the specified key.
     * 
     * @param key
     *            the object to search for
     * @return true if <code>key</code> is a key of this Map, false otherwise
     */
    public boolean containsKey(Object key) {
        Iterator<Map.Entry<K, V>> it = entrySet().iterator();
        if (key != null) {
            while (it.hasNext()) {
                if (key.equals(it.next().getKey())) {
                    return true;
                }
            }
        } else {
            while (it.hasNext()) {
                if (it.next().getKey() == null) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Searches this Map for the specified value.
     * 
     * @param value
     *            the object to search for
     * @return true if <code>value</code> is a value of this Map, false
     *         otherwise
     */
    public boolean containsValue(Object value) {
        Iterator<Map.Entry<K, V>> it = entrySet().iterator();
        if (value != null) {
            while (it.hasNext()) {
                if (value.equals(it.next().getValue())) {
                    return true;
                }
            }
        } else {
            while (it.hasNext()) {
                if (it.next().getValue() == null) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns a set view of the mappings contained in this map. Each element in
     * this set is a Map.Entry. The set is backed by the map so changes to one
     * are reflected by the other. (If the map is modified while an iteration
     * over the set is in progress, the results of the iteration are undefined.)
     * The set supports remove, removeAll, retainAll and clear operations, and
     * it does not support add or addAll operations.
     * 
     * @return a set of the mappings contained in this map
     */
    public abstract Set<Map.Entry<K, V>> entrySet();

    /**
     * Compares the specified object to this Map and answer if they are equal.
     * The object must be an instance of Map and contain the same key/value
     * pairs.
     * 
     * @param object
     *            the object to compare with this object
     * @return true if the specified object is equal to this Map, false
     *         otherwise
     * 
     * @see #hashCode
     */
    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) object;
            if (size() != map.size()) {
                return false;
            }

            Set<?> objectSet = map.entrySet();
            Iterator<Map.Entry<K, V>> it = entrySet().iterator();
            while (it.hasNext()) {
                if (!objectSet.contains(it.next())) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Returns the value of the mapping with the specified key.
     * 
     * @param key
     *            the key
     * @return the value of the mapping with the specified key
     */
    public V get(Object key) {
        Iterator<Map.Entry<K, V>> it = entrySet().iterator();
        if (key != null) {
            while (it.hasNext()) {
                Map.Entry<K, V> entry = it.next();
                if (key.equals(entry.getKey())) {
                    return entry.getValue();
                }
            }
        } else {
            while (it.hasNext()) {
                Map.Entry<K, V> entry = it.next();
                if (entry.getKey() == null) {
                    return entry.getValue();
                }
            }
        }
        return null;
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
    public int hashCode() {
        int result = 0;
        Iterator<Map.Entry<K, V>> it = entrySet().iterator();
        while (it.hasNext()) {
            result += it.next().hashCode();
        }
        return result;
    }

    /**
     * Returns if this Map has no elements, a size of zero.
     * 
     * @return true if this Map has no elements, false otherwise
     * 
     * @see #size
     */
    public boolean isEmpty() {
        return size() == 0;
    }

    /**
     * Returns a Set of the keys contained in this Map. The set is backed by
     * this Map so changes to one are reflected by the other. The set does not
     * support adding.
     * 
     * @return a Set of the keys
     */
    public Set<K> keySet() {
        if (keySet == null) {
            keySet = new AbstractSet<K>() {
                @Override
                public boolean contains(Object object) {
                    return containsKey(object);
                }

                @Override
                public int size() {
                    return AbstractMap.this.size();
                }

                @Override
                public Iterator<K> iterator() {
                    return new Iterator<K>() {
                        Iterator<Map.Entry<K, V>> setIterator = entrySet()
                                .iterator();

                        public boolean hasNext() {
                            return setIterator.hasNext();
                        }

                        public K next() {
                            return setIterator.next().getKey();
                        }

                        public void remove() {
                            setIterator.remove();
                        }
                    };
                }
            };
        }
        return keySet;
    }

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
    public V put(K key, V value) {
        throw new UnsupportedOperationException();
    }

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
    public void putAll(Map<? extends K, ? extends V> map) {
        for (Map.Entry<? extends K, ? extends V> entry : map.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

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
    public V remove(Object key) {
        Iterator<Map.Entry<K, V>> it = entrySet().iterator();
        if (key != null) {
            while (it.hasNext()) {
                Map.Entry<K, V> entry = it.next();
                if (key.equals(entry.getKey())) {
                    it.remove();
                    return entry.getValue();
                }
            }
        } else {
            while (it.hasNext()) {
                Map.Entry<K, V> entry = it.next();
                if (entry.getKey() == null) {
                    it.remove();
                    return entry.getValue();
                }
            }
        }
        return null;
    }

    /**
     * Returns the number of elements in this Map.
     * 
     * @return the number of elements in this Map
     */
    public int size() {
        return entrySet().size();
    }

    /**
     * Returns the string representation of this Map.
     * 
     * @return the string representation of this Map
     */
    @Override
    public String toString() {
        if (isEmpty()) {
            return "{}"; //$NON-NLS-1$
        }

        StringBuilder buffer = new StringBuilder(size() * 28);
        buffer.append('{');
        Iterator<Map.Entry<K, V>> it = entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<K, V> entry = it.next();
            Object key = entry.getKey();
            if (key != this) {
                buffer.append(key);
            } else {
                buffer.append("(this Map)"); //$NON-NLS-1$
            }
            buffer.append('=');
            Object value = entry.getValue();
            if (value != this) {
                buffer.append(value);
            } else {
                buffer.append("(this Map)"); //$NON-NLS-1$
            }
            if (it.hasNext()) {
                buffer.append(", "); //$NON-NLS-1$
            }
        }
        buffer.append('}');
        return buffer.toString();
    }

    /**
     * Returns a collection of the values contained in this map. The collection
     * is backed by this map so changes to one are reflected by the other. The
     * collection supports remove, removeAll, retainAll and clear operations,
     * and it does not support add or addAll operations.
     * 
     * This method returns a collection which is the subclass of
     * AbstractCollection. The iterator method of this subclass returns a
     * "wrapper object" over the iterator of map's entrySet(). The size method
     * wraps the map's size method and the contains method wraps the map's
     * containsValue method.
     * 
     * The collection is created when this method is called at first time and
     * returned in response to all subsequent calls. This method may return
     * different Collection when multiple calls to this method, since it has no
     * synchronization performed.
     * 
     * @return a collection of the values contained in this map
     * 
     */
    public Collection<V> values() {
        if (valuesCollection == null) {
            valuesCollection = new AbstractCollection<V>() {
                @Override
                public int size() {
                    return AbstractMap.this.size();
                }

                @Override
                public boolean contains(Object object) {
                    return containsValue(object);
                }

                @Override
                public Iterator<V> iterator() {
                    return new Iterator<V>() {
                        Iterator<Map.Entry<K, V>> setIterator = entrySet()
                                .iterator();

                        public boolean hasNext() {
                            return setIterator.hasNext();
                        }

                        public V next() {
                            return setIterator.next().getValue();
                        }

                        public void remove() {
                            setIterator.remove();
                        }
                    };
                }
            };
        }
        return valuesCollection;
    }

    /**
     * Returns a new instance of the same class as the receiver, whose slots
     * have been filled in with the values in the slots of the receiver.
     * 
     * @return Object a shallow copy of this object.
     * @exception CloneNotSupportedException
     *                if the receiver's class does not implement the interface
     *                Cloneable.
     */
    @Override
    @SuppressWarnings("unchecked")
    protected Object clone() throws CloneNotSupportedException {
        AbstractMap<K, V> result = (AbstractMap<K, V>) super.clone();
        result.keySet = null;
        result.valuesCollection = null;
        return result;
    }
}
