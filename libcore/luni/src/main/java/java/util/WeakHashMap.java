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

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

/**
 * WeakHashMap is an implementation of Map with keys which are WeakReferences.
 * The key/value mapping is removed when the key is no longer referenced. All
 * optional operations are supported, adding and removing. Keys and values can
 * be any objects.
 * 
 * @since 1.2
 * @see HashMap
 * @see WeakReference
 */
public class WeakHashMap<K, V> extends AbstractMap<K, V> implements Map<K, V> {

    private static final int DEFAULT_SIZE = 16;

    private final ReferenceQueue<K> referenceQueue;

    int elementCount;

    Entry<K, V>[] elementData;

    private final int loadFactor;

    private int threshold;

    volatile int modCount;

    // Simple utility method to isolate unchecked cast for array creation
    @SuppressWarnings("unchecked")
    private static <K, V> Entry<K, V>[] newEntryArray(int size) {
        return new Entry[size];
    }

    private static final class Entry<K, V> extends WeakReference<K> implements
            Map.Entry<K, V> {
        int hash;

        boolean isNull;

        V value;

        Entry<K, V> next;

        interface Type<R, K, V> {
            R get(Map.Entry<K, V> entry);
        }

        Entry(K key, V object, ReferenceQueue<K> queue) {
            super(key, queue);
            isNull = key == null;
            hash = isNull ? 0 : key.hashCode();
            value = object;
        }

        public K getKey() {
            return super.get();
        }

        public V getValue() {
            return value;
        }

        public V setValue(V object) {
            V result = value;
            value = object;
            return result;
        }

        @Override
        public boolean equals(Object other) {
            if (!(other instanceof Map.Entry)) {
                return false;
            }
            Map.Entry<?, ?> entry = (Map.Entry<?, ?>) other;
            Object key = super.get();
            return (key == null ? key == entry.getKey() : key.equals(entry
                    .getKey()))
                    && (value == null ? value == entry.getValue() : value
                            .equals(entry.getValue()));
        }

        @Override
        public int hashCode() {
            return hash + (value == null ? 0 : value.hashCode());
        }

        @Override
        public String toString() {
            return super.get() + "=" + value; //$NON-NLS-1$
        }
    }

    class HashIterator<R> implements Iterator<R> {
        private int position = 0, expectedModCount;

        private Entry<K, V> currentEntry, nextEntry;

        private K nextKey;

        final Entry.Type<R, K, V> type;

        HashIterator(Entry.Type<R, K, V> type) {
            this.type = type;
            expectedModCount = modCount;
        }

        public boolean hasNext() {
            if (nextEntry != null) {
                return true;
            }
            while (true) {
                if (nextEntry == null) {
                    while (position < elementData.length) {
                        if ((nextEntry = elementData[position++]) != null) {
                            break;
                        }
                    }
                    if (nextEntry == null) {
                        return false;
                    }
                }
                // ensure key of next entry is not gc'ed
                nextKey = nextEntry.get();
                if (nextKey != null || nextEntry.isNull) {
                    return true;
                }
                nextEntry = nextEntry.next;
            }
        }

        public R next() {
            if (expectedModCount == modCount) {
                if (hasNext()) {
                    currentEntry = nextEntry;
                    nextEntry = currentEntry.next;
                    R result = type.get(currentEntry);
                    // free the key
                    nextKey = null;
                    return result;
                }
                throw new NoSuchElementException();
            }
            throw new ConcurrentModificationException();
        }

        public void remove() {
            if (expectedModCount == modCount) {
                if (currentEntry != null) {
                    removeEntry(currentEntry);
                    currentEntry = null;
                    expectedModCount++;
                    // cannot poll() as that would change the expectedModCount
                } else {
                    throw new IllegalStateException();
                }
            } else {
                throw new ConcurrentModificationException();
            }
        }
    }

    /**
     * Constructs a new empty instance of WeakHashMap.
     */
    public WeakHashMap() {
        this(DEFAULT_SIZE);
    }

    /**
     * Constructs a new instance of WeakHashMap with the specified capacity.
     * 
     * @param capacity
     *            the initial capacity of this WeakHashMap
     * 
     * @exception IllegalArgumentException
     *                when the capacity is less than zero
     */
    public WeakHashMap(int capacity) {
        if (capacity >= 0) {
            elementCount = 0;
            elementData = newEntryArray(capacity == 0 ? 1 : capacity);
            loadFactor = 7500; // Default load factor of 0.75
            computeMaxSize();
            referenceQueue = new ReferenceQueue<K>();
        } else {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Constructs a new instance of WeakHashMap with the specified capacity and
     * load factor.
     * 
     * @param capacity
     *            the initial capacity
     * @param loadFactor
     *            the initial load factor
     * 
     * @exception IllegalArgumentException
     *                when the capacity is less than zero or the load factor is
     *                less or equal to zero
     */
    public WeakHashMap(int capacity, float loadFactor) {
        if (capacity >= 0 && loadFactor > 0) {
            elementCount = 0;
            elementData = newEntryArray(capacity == 0 ? 1 : capacity);
            this.loadFactor = (int) (loadFactor * 10000);
            computeMaxSize();
            referenceQueue = new ReferenceQueue<K>();
        } else {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Constructs a new instance of HashMap containing the mappings from the
     * specified Map.
     * 
     * @param map
     *            the mappings to add
     */
    public WeakHashMap(Map<? extends K, ? extends V> map) {
        this(map.size() < 6 ? 11 : map.size() * 2);
        putAllImpl(map);
    }

    /**
     * Removes all mappings from this WeakHashMap, leaving it empty.
     * 
     * @see #isEmpty
     * @see #size
     */
    @Override
    public void clear() {
        if (elementCount > 0) {
            elementCount = 0;
            Arrays.fill(elementData, null);
            modCount++;
            while (referenceQueue.poll() != null) {
                // do nothing
            }
        }
    }

    private void computeMaxSize() {
        threshold = (int) ((long) elementData.length * loadFactor / 10000);
    }

    /**
     * Searches this WeakHashMap for the specified key.
     * 
     * @param key
     *            the object to search for
     * @return true if <code>key</code> is a key of this WeakHashMap, false
     *         otherwise
     */
    @Override
    public boolean containsKey(Object key) {
        return getEntry(key) != null;
    }

    /**
     * Returns a Set of the mappings contained in this WeakHashMap. Each element
     * in the set is a Map.Entry. The set is backed by this WeakHashMap so
     * changes to one are reflected by the other. The set does not support
     * adding.
     * 
     * @return a Set of the mappings
     */
    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        poll();
        return new AbstractSet<Map.Entry<K, V>>() {
            @Override
            public int size() {
                return WeakHashMap.this.size();
            }

            @Override
            public void clear() {
                WeakHashMap.this.clear();
            }

            @Override
            public boolean remove(Object object) {
                if (contains(object)) {
                    WeakHashMap.this
                            .remove(((Map.Entry<?, ?>) object).getKey());
                    return true;
                }
                return false;
            }

            @Override
            public boolean contains(Object object) {
                if (object instanceof Map.Entry) {
                    Entry<?, ?> entry = getEntry(((Map.Entry<?, ?>) object)
                            .getKey());
                    if (entry != null) {
                        Object key = entry.get();
                        if (key != null || entry.isNull) {
                            return object.equals(entry);
                        }
                    }
                }
                return false;
            }

            @Override
            public Iterator<Map.Entry<K, V>> iterator() {
                return new HashIterator<Map.Entry<K, V>>(
                        new Entry.Type<Map.Entry<K, V>, K, V>() {
                            public Map.Entry<K, V> get(Map.Entry<K, V> entry) {
                                return entry;
                            }
                        });
            }
        };
    }

    /**
     * Returns a Set of the keys contained in this WeakHashMap. The set is
     * backed by this WeakHashMap so changes to one are reflected by the other.
     * The set does not support adding.
     * 
     * @return a Set of the keys
     */
    @Override
    public Set<K> keySet() {
        poll();
        if (keySet == null) {
            keySet = new AbstractSet<K>() {
                @Override
                public boolean contains(Object object) {
                    return containsKey(object);
                }

                @Override
                public int size() {
                    return WeakHashMap.this.size();
                }

                @Override
                public void clear() {
                    WeakHashMap.this.clear();
                }

                @Override
                public boolean remove(Object key) {
                    if (containsKey(key)) {
                        WeakHashMap.this.remove(key);
                        return true;
                    }
                    return false;
                }

                @Override
                public Iterator<K> iterator() {
                    return new HashIterator<K>(new Entry.Type<K, K, V>() {
                        public K get(Map.Entry<K, V> entry) {
                            return entry.getKey();
                        }
                    });
                }
            };
        }
        return keySet;
    }

    /**
     * Returns a Collection of the values contained in this WeakHashMap. The
     * collection is backed by this WeakHashMap so changes to one are reflected
     * by the other. The collection does not support adding.
     * 
     * @return a Collection of the values
     */
    @Override
    public Collection<V> values() {
        poll();
        if (valuesCollection == null) {
            valuesCollection = new AbstractCollection<V>() {
                @Override
                public int size() {
                    return WeakHashMap.this.size();
                }

                @Override
                public void clear() {
                    WeakHashMap.this.clear();
                }

                @Override
                public boolean contains(Object object) {
                    return containsValue(object);
                }

                @Override
                public Iterator<V> iterator() {
                    return new HashIterator<V>(new Entry.Type<V, K, V>() {
                        public V get(Map.Entry<K, V> entry) {
                            return entry.getValue();
                        }
                    });
                }
            };
        }
        return valuesCollection;
    }

    /**
     * Returns the value of the mapping with the specified key.
     * 
     * @param key
     *            the key
     * @return the value of the mapping with the specified key
     */
    @Override
    public V get(Object key) {
        poll();
        if (key != null) {
            int index = (key.hashCode() & 0x7FFFFFFF) % elementData.length;
            Entry<K, V> entry = elementData[index];
            while (entry != null) {
                if (key.equals(entry.get())) {
                    return entry.value;
                }
                entry = entry.next;
            }
            return null;
        }
        Entry<K, V> entry = elementData[0];
        while (entry != null) {
            if (entry.isNull) {
                return entry.value;
            }
            entry = entry.next;
        }
        return null;
    }

    Entry<K, V> getEntry(Object key) {
        poll();
        if (key != null) {
            int index = (key.hashCode() & 0x7FFFFFFF) % elementData.length;
            Entry<K, V> entry = elementData[index];
            while (entry != null) {
                if (key.equals(entry.get())) {
                    return entry;
                }
                entry = entry.next;
            }
            return null;
        }
        Entry<K, V> entry = elementData[0];
        while (entry != null) {
            if (entry.isNull) {
                return entry;
            }
            entry = entry.next;
        }
        return null;
    }

    /**
     * Searches this WeakHashMap for the specified value, and returns true, if
     * at least one entry has this object as its value.
     * 
     * @param value
     *            the object to search for
     * @return true if <code>value</code> is a value in this WeakHashMap,
     *         false otherwise
     */
    @Override
    public boolean containsValue(Object value) {
        poll();
        if (value != null) {
            for (int i = elementData.length; --i >= 0;) {
                Entry<K, V> entry = elementData[i];
                while (entry != null) {
                    K key = entry.get();
                    if ((key != null || entry.isNull)
                            && value.equals(entry.value)) {
                        return true;
                    }
                    entry = entry.next;
                }
            }
        } else {
            for (int i = elementData.length; --i >= 0;) {
                Entry<K, V> entry = elementData[i];
                while (entry != null) {
                    K key = entry.get();
                    if ((key != null || entry.isNull) && entry.value == null) {
                        return true;
                    }
                    entry = entry.next;
                }
            }
        }
        return false;
    }

    /**
     * Returns if this WeakHashMap has no elements, a size of zero.
     * 
     * @return true if this HashMap has no elements, false otherwise
     * 
     * @see #size
     */
    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @SuppressWarnings("unchecked")
    void poll() {
        Entry<K, V> toRemove;
        while ((toRemove = (Entry<K, V>) referenceQueue.poll()) != null) {
            removeEntry(toRemove);
        }
    }

    void removeEntry(Entry<K, V> toRemove) {
        Entry<K, V> entry, last = null;
        int index = (toRemove.hash & 0x7FFFFFFF) % elementData.length;
        entry = elementData[index];
        // Ignore queued entries which cannot be found, the user could
        // have removed them before they were queued, i.e. using clear()
        while (entry != null) {
            if (toRemove == entry) {
                modCount++;
                if (last == null) {
                    elementData[index] = entry.next;
                } else {
                    last.next = entry.next;
                }
                elementCount--;
                break;
            }
            last = entry;
            entry = entry.next;
        }
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
     */
    @Override
    public V put(K key, V value) {
        poll();
        int index = 0;
        Entry<K, V> entry;
        if (key != null) {
            index = (key.hashCode() & 0x7FFFFFFF) % elementData.length;
            entry = elementData[index];
            while (entry != null && !key.equals(entry.get())) {
                entry = entry.next;
            }
        } else {
            entry = elementData[0];
            while (entry != null && !entry.isNull) {
                entry = entry.next;
            }
        }
        if (entry == null) {
            modCount++;
            if (++elementCount > threshold) {
                rehash();
                index = key == null ? 0 : (key.hashCode() & 0x7FFFFFFF)
                        % elementData.length;
            }
            entry = new Entry<K, V>(key, value, referenceQueue);
            entry.next = elementData[index];
            elementData[index] = entry;
            return null;
        }
        V result = entry.value;
        entry.value = value;
        return result;
    }

    private void rehash() {
        int length = elementData.length << 1;
        if (length == 0) {
            length = 1;
        }
        Entry<K, V>[] newData = newEntryArray(length);
        for (int i = 0; i < elementData.length; i++) {
            Entry<K, V> entry = elementData[i];
            while (entry != null) {
                int index = entry.isNull ? 0 : (entry.hash & 0x7FFFFFFF)
                        % length;
                Entry<K, V> next = entry.next;
                entry.next = newData[index];
                newData[index] = entry;
                entry = next;
            }
        }
        elementData = newData;
        computeMaxSize();
    }

    /**
     * Copies all the mappings in the given map to this map. These mappings will
     * replace all mappings that this map had for any of the keys currently in
     * the given map.
     * 
     * @param map
     *            the Map to copy mappings from
     * @throws NullPointerException
     *             if the given map is null
     */
    @Override
    public void putAll(Map<? extends K, ? extends V> map) {
        putAllImpl(map);
    }

    /**
     * Removes a mapping with the specified key from this WeakHashMap.
     * 
     * @param key
     *            the key of the mapping to remove
     * @return the value of the removed mapping or null if key is not a key in
     *         this WeakHashMap
     */
    @Override
    public V remove(Object key) {
        poll();
        int index = 0;
        Entry<K, V> entry, last = null;
        if (key != null) {
            index = (key.hashCode() & 0x7FFFFFFF) % elementData.length;
            entry = elementData[index];
            while (entry != null && !key.equals(entry.get())) {
                last = entry;
                entry = entry.next;
            }
        } else {
            entry = elementData[0];
            while (entry != null && !entry.isNull) {
                last = entry;
                entry = entry.next;
            }
        }
        if (entry != null) {
            modCount++;
            if (last == null) {
                elementData[index] = entry.next;
            } else {
                last.next = entry.next;
            }
            elementCount--;
            return entry.value;
        }
        return null;
    }

    /**
     * Returns the number of mappings in this WeakHashMap.
     * 
     * @return the number of mappings in this WeakHashMap
     */
    @Override
    public int size() {
        poll();
        return elementCount;
    }

    private void putAllImpl(Map<? extends K, ? extends V> map) {
        if (map.entrySet() != null) {
            super.putAll(map);
        }
    }
}
