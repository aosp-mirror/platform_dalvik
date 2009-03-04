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
 * LinkedHashMap is a variant of HashMap. Its entries are kept in a
 * doubly-linked list. The iteration order is, by default, the order in which
 * keys were inserted. Reinserting an already existing key doesn't change the
 * order. A key is existing if a call to {@code containsKey} would return true.
 * <p>
 * If the three argument constructor is used, and {@code order} is specified as
 * {@code true}, the iteration will be in the order that entries were accessed.
 * The access order gets affected by put(), get(), putAll() operations, but not
 * by operations on the collection views.
 * <p>
 * Null elements are allowed, and all the optional map operations are supported.
 * <p>
 * <b>Note:</b> The implementation of {@code LinkedHashMap} is not synchronized.
 * If one thread of several threads accessing an instance modifies the map
 * structurally, access to the map needs to be synchronized. For
 * insertion-ordered instances a structural modification is an operation that
 * removes or adds an entry. Access-ordered instances also are structurally
 * modified by put(), get() and putAll() since these methods change the order of
 * the entries. Changes in the value of an entry are not structural changes.
 * <p>
 * The Iterator that can be created by calling the {@code iterator} method
 * throws a {@code ConcurrentModificationException} if the map is structurally
 * changed while an iterator is used to iterate over the elements. Only the
 * {@code remove} method that is provided by the iterator allows for removal of
 * elements during iteration. It is not possible to guarantee that this
 * mechanism works in all cases of unsynchronized concurrent modification. It
 * should only be used for debugging purposes.
 * 
 * @since Android 1.0
 */
public class LinkedHashMap<K, V> extends HashMap<K, V> {

    private static final long serialVersionUID = 3801124242820219131L;

    private final boolean accessOrder;

    transient private LinkedHashMapEntry<K, V> head, tail;

    /**
     * Constructs a new empty {@code LinkedHashMap} instance.
     * 
     * @since Android 1.0
     */
    public LinkedHashMap() {
        super();
        accessOrder = false;
        head = null;
    }

    /**
     * Constructs a new {@code LinkedHashMap} instance with the specified
     * capacity.
     * 
     * @param s
     *            the initial capacity of this map.
     * @exception IllegalArgumentException
     *                when the capacity is less than zero.
     * @since Android 1.0
     */
    public LinkedHashMap(int s) {
        super(s);
        accessOrder = false;
        head = null;
    }

    /**
     * Constructs a new {@code LinkedHashMap} instance with the specified
     * capacity and load factor.
     * 
     * @param s
     *            the initial capacity of this map.
     * @param lf
     *            the initial load factor.
     * @throws IllegalArgumentException
     *             when the capacity is less than zero or the load factor is
     *             less or equal to zero.
     * @since Android 1.0
     */
    public LinkedHashMap(int s, float lf) {
        super(s, lf);
        accessOrder = false;
        head = null;
        tail = null;
    }

    /**
     * Constructs a new {@code LinkedHashMap} instance with the specified
     * capacity, load factor and a flag specifying the ordering behavior.
     * 
     * @param s
     *            the initial capacity of this hash map.
     * @param lf
     *            the initial load factor.
     * @param order
     *            {@code true} if the ordering should be done based on the last
     *            access (from least-recently accessed to most-recently
     *            accessed), and {@code false} if the ordering should be the
     *            order in which the entries were inserted.
     * @throws IllegalArgumentException
     *             when the capacity is less than zero or the load factor is
     *             less or equal to zero.
     * @since Android 1.0
     */
    public LinkedHashMap(int s, float lf, boolean order) {
        super(s, lf);
        accessOrder = order;
        head = null;
        tail = null;
    }

    /**
     * Constructs a new {@code LinkedHashMap} instance containing the mappings
     * from the specified map. The order of the elements is preserved.
     * 
     * @param m
     *            the mappings to add.
     * @since Android 1.0
     */
    public LinkedHashMap(Map<? extends K, ? extends V> m) {
        accessOrder = false;
        head = null;
        tail = null;
        putAll(m);
    }

    static final class LinkedHashIterator<E, KT, VT> extends HashMapIterator<E, KT, VT> {
        LinkedHashIterator(MapEntry.Type<E, KT, VT> value, LinkedHashMap<KT, VT> hm) {
            super(value, hm);
            entry = hm.head;
        }

        @Override
        public boolean hasNext() {
            return (entry != null);
        }

        @Override
        public E next() {
            checkConcurrentMod();
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            E result = type.get(entry);
            lastEntry = entry;
            entry = ((LinkedHashMapEntry<KT, VT>)entry).chainForward;
            canRemove = true;
            return result;
        }

        @Override
        public void remove() {
            checkConcurrentMod();
            if (!canRemove) {
                throw new IllegalStateException();
            }

            canRemove = false;
            associatedMap.modCount++;

            int index = (lastEntry.key == null)? 0 : (lastEntry.key.hashCode() & 0x7FFFFFFF) % associatedMap.elementData.length;
            LinkedHashMapEntry<KT, VT> m = (LinkedHashMapEntry<KT, VT>) associatedMap.elementData[index];
            if (m == lastEntry) {
                associatedMap.elementData[index] = lastEntry.next;
            } else {
                while (m.next != null) {
                    if (m.next == lastEntry) {
                        break;
                    }
                    m = (LinkedHashMapEntry<KT, VT>) m.next;
                }
                // assert m.next == entry
                m.next = lastEntry.next;
            }
            LinkedHashMapEntry<KT, VT> lhme = (LinkedHashMapEntry<KT, VT>) lastEntry;
            LinkedHashMapEntry<KT, VT> p = lhme.chainBackward;
            LinkedHashMapEntry<KT, VT> n = lhme.chainForward;
            LinkedHashMap<KT, VT> lhm = (LinkedHashMap<KT, VT>) associatedMap;
            if (p != null) {
                p.chainForward = n;
                if (n != null) {
                    n.chainBackward = p;
                } else {
                    lhm.tail = p;
                }
            } else {
                lhm.head = n;
                if (n != null) {
                    n.chainBackward = null;
                } else {
                    lhm.tail = null;
                }
            }
            associatedMap.elementCount--;
            expectedModCount++;
        }
    }

    static final class LinkedHashMapEntrySet<KT, VT> extends HashMapEntrySet<KT, VT> {
        public LinkedHashMapEntrySet(LinkedHashMap<KT, VT> lhm) {
            super(lhm);
        }

        @Override
        public Iterator<Map.Entry<KT,VT>> iterator() {
            return new LinkedHashIterator<Map.Entry<KT,VT>,KT,VT>(new MapEntry.Type<Map.Entry<KT,VT>, KT, VT>() {
                public Map.Entry<KT,VT> get(MapEntry<KT,VT> entry) {
                    return entry;
                }
            }, (LinkedHashMap<KT, VT>) hashMap());
        }
    }

    static final class LinkedHashMapEntry<K, V> extends Entry<K, V> {
        LinkedHashMapEntry<K, V> chainForward, chainBackward;

        LinkedHashMapEntry(K theKey, V theValue) {
            super(theKey, theValue);
            chainForward = null;
            chainBackward = null;
        }

        LinkedHashMapEntry(K theKey, int hash) {
            super(theKey, hash);
            chainForward = null;
            chainBackward = null;
        }


        @Override
        @SuppressWarnings("unchecked")
        public Object clone() {
            LinkedHashMapEntry<K, V> entry = (LinkedHashMapEntry<K, V>) super.clone();
            entry.chainBackward = chainBackward;
            entry.chainForward = chainForward;
            LinkedHashMapEntry<K, V> lnext = (LinkedHashMapEntry<K, V>) entry.next;
            if (lnext != null) {
                entry.next = (LinkedHashMapEntry<K, V>) lnext.clone();
            }
            return entry;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    Entry<K, V>[] newElementArray(int s) {
        return new LinkedHashMapEntry[s];
    }

    /**
     * Returns the value of the mapping with the specified key.
     * 
     * @param key
     *            the key.
     * @return the value of the mapping with the specified key, or {@code null}
     *         if no mapping for the specified key is found.
     * @since Android 1.0
     */
    @Override
    public V get(Object key) {
        LinkedHashMapEntry<K, V> m;
        if (key == null) {
            m = (LinkedHashMapEntry<K, V>)findNullKeyEntry();
        } else {
            int hash = key.hashCode();
            int index = (hash & 0x7FFFFFFF) % elementData.length;
            m = (LinkedHashMapEntry<K, V>)findNonNullKeyEntry(key, index, hash);
        }
        if (m == null) {
            return null;
        }
        if (accessOrder && tail != m) {
            // BEGIN android-added
            modCount++;
            // END android-added
            LinkedHashMapEntry<K, V> p = m.chainBackward;
            LinkedHashMapEntry<K, V> n = m.chainForward;
            n.chainBackward = p;
            if (p != null) {
                p.chainForward = n;
            } else {
                head = n;
            }
            m.chainForward = null;
            m.chainBackward = tail;
            tail.chainForward = m;
            tail = m;
        }
        return m.value;
    }

    /*
     * @param key @param index @return Entry
     */
    @Override
    Entry<K, V> createEntry(K key, int index, V value) {
        LinkedHashMapEntry<K, V> m = new LinkedHashMapEntry<K, V>(key, value);
        m.next = elementData[index];
        elementData[index] = m;
        linkEntry(m);
        return m;
    }

    Entry<K,V> createHashedEntry(K key, int index, int hash) {
        LinkedHashMapEntry<K, V> m = new LinkedHashMapEntry<K, V>(key, hash);
        m.next = elementData[index];
        elementData[index] = m;
        linkEntry(m);
        return m;
    }

    // BEGIN android-changed
    // copied from newer version of harmony
    /**
     * Maps the specified key to the specified value.
     * 
     * @param key
     *            the key.
     * @param value
     *            the value.
     * @return the value of any previous mapping with the specified key or
     *         {@code null} if there was no such mapping.
     * @since Android 1.0
     */
    @Override
    public V put(K key, V value) {
        V result = putImpl(key,value);

        if (removeEldestEntry(head)) {
            remove(head.key);
        }

        return result;
    }
    
    V putImpl(K key, V value){
        LinkedHashMapEntry<K, V> m;
        if (elementCount == 0){
            head = tail = null;
        }
        if (key == null) {
            m = (LinkedHashMapEntry<K, V>)findNullKeyEntry();
            if (m == null) {
                modCount++;
                // Check if we need to remove the oldest entry
                // The check includes accessOrder since an accessOrder LinkedHashMap
                // does not record
                // the oldest member in 'head'.
                if (++elementCount > threshold) {
                    rehash();
                }
                    m = (LinkedHashMapEntry<K, V>) createHashedEntry(null, 0, 0);
            } else {
                linkEntry(m);
            }
        } else {
            int hash = key.hashCode();
            int index = (hash & 0x7FFFFFFF) % elementData.length;
            m = (LinkedHashMapEntry<K, V>)findNonNullKeyEntry(key, index, hash);
            if (m == null) {
                modCount++;
                if (++elementCount > threshold) {
                    rehash();
                    index = (hash & 0x7FFFFFFF) % elementData.length;
                }
                m = (LinkedHashMapEntry<K, V>) createHashedEntry(key, index, hash);
            } else {
                linkEntry(m);
            }
        }

        V result = m.value;
        m.value = value;
        return result;
    }
    // END android-changed

    /*
     * @param m
     */
    void linkEntry(LinkedHashMapEntry<K, V> m) {
        if (tail == m) {
            return;
        }

        if (head == null) {
            // Check if the map is empty
            head = tail = m;
            return;
        }

        // we need to link the new entry into either the head or tail
        // of the chain depending on if the LinkedHashMap is accessOrder or not
        LinkedHashMapEntry<K, V> p = m.chainBackward;
        LinkedHashMapEntry<K, V> n = m.chainForward;
        if (p == null) {
            if (n != null) {
                // The entry must be the head but not the tail
                if (accessOrder) {
                    head = n;
                    n.chainBackward = null;
                    m.chainBackward = tail;
                    m.chainForward = null;
                    tail.chainForward = m;
                    tail = m;
                }
            } else {
                // This is a new entry
                m.chainBackward = tail;
                m.chainForward = null;
                tail.chainForward = m;
                tail = m;
            }
            return;
        }

        if (n == null) {
            // The entry must be the tail so we can't get here
            return;
        }

        // The entry is neither the head nor tail
        if (accessOrder) {
            p.chainForward = n;
            n.chainBackward = p;
            m.chainForward = null;
            m.chainBackward = tail;
            tail.chainForward = m;
            tail = m;
        }

    }

    /**
     * Returns a set containing all of the mappings in this map. Each mapping is
     * an instance of {@link Map.Entry}. As the set is backed by this map,
     * changes in one will be reflected in the other.
     * 
     * @return a set of the mappings.
     * @since Android 1.0
     */
    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        return new LinkedHashMapEntrySet<K, V>(this);
    }

    /**
     * Returns a set of the keys contained in this map. The set is backed by
     * this map so changes to one are reflected by the other. The set does not
     * support adding.
     * 
     * @return a set of the keys.
     * @since Android 1.0
     */
    @Override
    public Set<K> keySet() {
        if (keySet == null) {
            keySet = new AbstractSet<K>() {
                @Override
                public boolean contains(Object object) {
                    return containsKey(object);
                }

                @Override
                public int size() {
                    return LinkedHashMap.this.size();
                }

                @Override
                public void clear() {
                    LinkedHashMap.this.clear();
                }

                @Override
                public boolean remove(Object key) {
                    if (containsKey(key)) {
                        LinkedHashMap.this.remove(key);
                        return true;
                    }
                    return false;
                }

                @Override
                public Iterator<K> iterator() {
                    return new LinkedHashIterator<K,K,V>(new MapEntry.Type<K,K,V>() {
                        public K get(MapEntry<K,V> entry) {
                            return entry.key;
                        }
                    }, LinkedHashMap.this);
                }
            };
        }
        return keySet;
    }

    /**
     * Returns a collection of the values contained in this map. The collection
     * is backed by this map so changes to one are reflected by the other. The
     * collection supports remove, removeAll, retainAll and clear operations,
     * and it does not support add or addAll operations.
     * <p>
     * This method returns a collection which is the subclass of
     * AbstractCollection. The iterator method of this subclass returns a
     * "wrapper object" over the iterator of map's entrySet(). The size method
     * wraps the map's size method and the contains method wraps the map's
     * containsValue method.
     * </p>
     * <p>
     * The collection is created when this method is called for the first time
     * and returned in response to all subsequent calls. This method may return
     * different collections when multiple concurrent calls occur, since no
     * synchronization is performed.
     * </p>
     * 
     * @return a collection of the values contained in this map.
     * @since Android 1.0
     */
    @Override
    public Collection<V> values() {
        if (valuesCollection == null) {
            valuesCollection = new AbstractCollection<V>() {
                @Override
                public boolean contains(Object object) {
                    return containsValue(object);
                }

                @Override
                public int size() {
                    return LinkedHashMap.this.size();
                }

                @Override
                public void clear() {
                    LinkedHashMap.this.clear();
                }

                @Override
                public Iterator<V> iterator() {
                    return new LinkedHashIterator<V,K,V>(new MapEntry.Type<V,K,V>() {
                        public V get(MapEntry<K,V> entry) {
                            return entry.value;
                        }
                    }, LinkedHashMap.this);
                }
            };
        }
        return valuesCollection;
    }

    /**
     * Removes the mapping with the specified key from this map.
     * 
     * @param key
     *            the key of the mapping to remove.
     * @return the value of the removed mapping or {@code null} if no mapping
     *         for the specified key was found.
     * @since Android 1.0
     */
    @Override
    public V remove(Object key) {
        LinkedHashMapEntry<K, V> m = (LinkedHashMapEntry<K, V>) removeEntry(key);
        if (m == null) {
            return null;
        }
        LinkedHashMapEntry<K, V> p = m.chainBackward;
        LinkedHashMapEntry<K, V> n = m.chainForward;
        if (p != null) {
            p.chainForward = n;
        } else {
            head = n;
        }
        if (n != null) {
            n.chainBackward = p;
        } else {
            tail = p;
        }
        return m.value;
    }

    /**
     * This method is queried from the put and putAll methods to check if the
     * eldest member of the map should be deleted before adding the new member.
     * If this map was created with accessOrder = true, then the result of
     * removeEldestEntry is assumed to be false.
     * 
     * @param eldest
     *            the entry to check if it should be removed.
     * @return {@code true} if the eldest member should be removed.
     * @since Android 1.0
     */
    protected boolean removeEldestEntry(Map.Entry<K,V> eldest) {
        return false;
    }

    // BEGIN android-changed
    /**
     * Removes all elements from this map, leaving it empty.
     * 
     * @see #isEmpty()
     * @see #size()
     * @since Android 1.0
     */
    @Override
    public void clear() {
        internalClear();
    }

    @Override
    void internalClear() {
        super.internalClear();
        head = tail = null;
    }
    // END android-changed

    // BEGIN android-removed
    // copied from newer version of harmony
    // /**
    //  * Answers a new HashMap with the same mappings and size as this HashMap.
    //  * 
    //  * @return a shallow copy of this HashMap
    //  * 
    //  * @see java.lang.Cloneable
    //  */
    // @Override
    // @SuppressWarnings("unchecked")
    // public Object clone() {
    //     LinkedHashMap<K, V> map = (LinkedHashMap<K, V>) super.clone();
    //     map.clear();
    //     for (Map.Entry<K, V> entry : entrySet()) {
    //         map.put(entry.getKey(), entry.getValue());
    //     }
    //     return map;
    // }
    // END android-removed
}
