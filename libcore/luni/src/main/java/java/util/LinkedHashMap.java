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
 * LinkedHashMap is a variant on HashMap. Its entries are kept in a doubly-linked list.
 * The iteration order is, by default, the order in which keys were inserted.
 * <p> 
 * If the three argument constructor is used, and <code>order</code> is specified as <code>true</code>, 
 * the iteration would be in the order that entries were accessed. The access order gets 
 * affected by put(), get(), putAll() operations, but not by operations on the collection views.
 * <p>
 * Null elements are allowed, and all the optional Map operations are supported.
 * <p>
 * @since 1.4
 */
public class LinkedHashMap<K, V> extends HashMap<K, V> {

    private static final long serialVersionUID = 3801124242820219131L;

    private final boolean accessOrder;

    transient private LinkedHashMapEntry<K, V> head, tail;

    /**
     * Constructs a new empty instance of LinkedHashMap.
     */
    public LinkedHashMap() {
        super();
        accessOrder = false;
        head = null;
    }

    /**
     * Constructor with specified size.
     * 
     * @param s
     *            Size of LinkedHashMap required
     */
    public LinkedHashMap(int s) {
        super(s);
        accessOrder = false;
        head = null;
    }

    /**
     * Constructor with specified size and load factor.
     * 
     * @param s
     *            Size of LinkedHashMap required
     * @param lf
     *            Load factor
     */
    public LinkedHashMap(int s, float lf) {
        super(s, lf);
        accessOrder = false;
        head = null;
        tail = null;
    }

    /**
     * Constructor with specified size, load factor and access order
     * 
     * @param s
     *            Size of LinkedHashmap required
     * @param lf
     *            Load factor
     * @param order
     *            If true indicates that traversal order should begin with most
     *            recently accessed
     */
    public LinkedHashMap(int s, float lf, boolean order) {
        super(s, lf);
        accessOrder = order;
        head = null;
        tail = null;
    }

    /**
     * Constructor with input map
     * 
     * @param m
     *            Input map
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

    /**
     * Create a new element array
     * 
     * @param s
     * @return Reference to the element array
     */
    @Override
    @SuppressWarnings("unchecked")
    Entry<K, V>[] newElementArray(int s) {
        return new LinkedHashMapEntry[s];
    }

    /**
     * Retrieve the map value corresponding to the given key.
     * 
     * @param key
     *            Key value
     * @return mapped value or null if the key is not in the map
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

    /**
     * Set the mapped value for the given key to the given value.
     * 
     * @param key
     *            Key value
     * @param value
     *            New mapped value
     * @return The old value if the key was already in the map or null
     *         otherwise.
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
                    m = (LinkedHashMapEntry<K, V>) createHashedEntry(key, 0, 0);
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
     * Returns a Set of the mappings contained in this HashMap. Each element in
     * the set is a Map.Entry. The set is backed by this HashMap so changes to
     * one are reflected by the other. The set does not support adding.
     * 
     * @return a Set of the mappings
     */
    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        return new LinkedHashMapEntrySet<K, V>(this);
    }

    /**
     * Returns a Set of the keys contained in this HashMap. The set is backed by
     * this HashMap so changes to one are reflected by the other. The set does
     * not support adding.
     * 
     * @return a Set of the keys
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
     * Returns a Collection of the values contained in this HashMap. The
     * collection is backed by this HashMap so changes to one are reflected by
     * the other. The collection does not support adding.
     * 
     * @return a Collection of the values
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
     * Remove the entry corresponding to the given key.
     * 
     * @param key
     *            the key
     * @return the value associated with the key or null if the key was no in
     *         the map
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
     * removeEldesrEntry is assumed to be false.
     * 
     * @param eldest
     * @return true if the eldest member should be removed
     */
    protected boolean removeEldestEntry(Map.Entry<K,V> eldest) {
        return false;
    }

    /**
     * Removes all mappings from this HashMap, leaving it empty.
     * 
     * @see #isEmpty()
     * @see #size()
     */
    @Override
    public void clear() {
        super.clear();
        head = tail = null;
    }
}
