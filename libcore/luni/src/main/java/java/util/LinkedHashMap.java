/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements. See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

// BEGIN android-note
// Completely different implementation from harmony.  Runs much faster.
// BEGIN android-note

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
 */
public class LinkedHashMap<K, V> extends HashMap<K, V> {

    /**
     * A dummy entry in the circular linked list of entries in the map.
     * The first real entry is header.nxt, and the last is header.prv.
     * If the map is empty, header.nxt == header && header.prv == header.
     */
    private transient LinkedEntry<K, V> header;

    /**
     * True if access ordered, false if insertion ordered.
     */
    private final boolean accessOrder;

    /**
     * Constructs a new empty {@code LinkedHashMap} instance.
     */
    public LinkedHashMap() {
        super();
        init();
        accessOrder = false;
    }

    /**
     * Constructs a new {@code LinkedHashMap} instance with the specified
     * capacity.
     *
     * @param initialCapacity
     *            the initial capacity of this map.
     * @exception IllegalArgumentException
     *                when the capacity is less than zero.
     */
    public LinkedHashMap(int initialCapacity) {
        this(initialCapacity, DEFAULT_LOAD_FACTOR);
    }

    /**
     * Constructs a new {@code LinkedHashMap} instance with the specified
     * capacity and load factor.
     *
     * @param initialCapacity
     *            the initial capacity of this map.
     * @param loadFactor
     *            the initial load factor.
     * @throws IllegalArgumentException
     *             when the capacity is less than zero or the load factor is
     *             less or equal to zero.
     */
    public LinkedHashMap(int initialCapacity, float loadFactor) {
        this(initialCapacity, loadFactor, false);
    }

    /**
     * Constructs a new {@code LinkedHashMap} instance with the specified
     * capacity, load factor and a flag specifying the ordering behavior.
     *
     * @param initialCapacity
     *            the initial capacity of this hash map.
     * @param loadFactor
     *            the initial load factor.
     * @param accessOrder
     *            {@code true} if the ordering should be done based on the last
     *            access (from least-recently accessed to most-recently
     *            accessed), and {@code false} if the ordering should be the
     *            order in which the entries were inserted.
     * @throws IllegalArgumentException
     *             when the capacity is less than zero or the load factor is
     *             less or equal to zero.
     */
    public LinkedHashMap(
            int initialCapacity, float loadFactor, boolean accessOrder) {
        super(initialCapacity, loadFactor);
        init();
        this.accessOrder = accessOrder;
    }

    /**
     * Constructs a new {@code LinkedHashMap} instance containing the mappings
     * from the specified map. The order of the elements is preserved.
     *
     * @param map
     *            the mappings to add.
     */
    public LinkedHashMap(Map<? extends K, ? extends V> map) {
        this(capacityForInitSize(map.size()));
        constructorPutAll(map);
    }

    @Override void init(){
        header = new LinkedEntry<K, V>(null, null, 0, null, null, null);
        header.nxt = header.prv = header;
    }

    /**
     * LinkedEntry adds nxt/prv double-links to plain HashMapEntry.
     */
    static class LinkedEntry<K, V> extends HashMapEntry<K, V> {
        LinkedEntry<K, V> nxt;
        LinkedEntry<K, V> prv;

        LinkedEntry(K key, V value, int hash, HashMapEntry<K, V> next,
                    LinkedEntry<K, V> nxt, LinkedEntry<K, V> prv) {
            super(key, value, hash, next);
            this.nxt = nxt;
            this.prv = prv;
        }
    }

    /**
     * Evicts eldest entry if instructed, creates a new entry and links it in
     * as head of linked list. This method should call constructorNewEntry
     * (instead of duplicating code) if the performance of your VM permits.
     */
    @Override LinkedEntry<K, V> newEntry(
            K key, V value, int hash, HashMapEntry<K, V> next) {
        // Remove eldest entry if instructed to do so.
        LinkedEntry<K, V> eldest = header.nxt;
        if (eldest != header && removeEldestEntry(eldest))
            remove(eldest.key);

        // Create new entry and link it on to list
        LinkedEntry<K, V> header = this.header;
        LinkedEntry<K, V> oldTail = header.prv;
        LinkedEntry<K, V> newTail
                = new LinkedEntry<K,V>(key, value, hash, next, header, oldTail);
        return oldTail.nxt = header.prv = newTail;
    }

    /**
     * As above, but without eviction.
     */
    @Override HashMapEntry<K, V> constructorNewEntry(
            K key, V value, int hash, HashMapEntry<K, V> next) {
        LinkedEntry<K, V> header = this.header;
        LinkedEntry<K, V> oldTail = header.prv;
        LinkedEntry<K, V> newTail
                = new LinkedEntry<K,V>(key, value, hash, next, header, oldTail);
        return oldTail.nxt = header.prv = newTail;
    }

    /**
     * Returns the value of the mapping with the specified key.
     *
     * @param key
     *            the key.
     * @return the value of the mapping with the specified key, or {@code null}
     *         if no mapping for the specified key is found.
     */
    @Override public V get(Object key) {
        /*
         * This method is overridden to eliminate the need for a polymorphic
         * invocation in superclass at the expense of code duplication.
         */
        if (key == null) {
            HashMapEntry<K, V> e = entryForNullKey;
            if (e == null)
                return null;
            if (accessOrder)
                makeTail((LinkedEntry<K, V>) e);
            return e.value;
        }

        // Doug Lea's supplemental secondaryHash function (inlined)
        int hash = key.hashCode();
        hash ^= (hash >>> 20) ^ (hash >>> 12);
        hash ^= (hash >>> 7) ^ (hash >>> 4);

        HashMapEntry<K, V>[] tab = table;
        for (HashMapEntry<K, V> e = tab[hash & (tab.length - 1)];
                e != null; e = e.next) {
            K eKey = e.key;
            if (eKey == key || (e.hash == hash && key.equals(eKey))) {
                if (accessOrder)
                    makeTail((LinkedEntry<K, V>) e);
                return e.value;
            }
        }
        return null;
    }

    /**
     * Relinks the given entry to the tail of the list. Under access ordering,
     * this method is invoked whenever the value of a  pre-existing entry is
     * read by Map.get or modified by Map.put.
     */
    private void makeTail(LinkedEntry<K, V> e) {
        // Unlink e
        e.prv.nxt = e.nxt;
        e.nxt.prv = e.prv;

        // Relink e as tail
        LinkedEntry<K, V> header = this.header;
        LinkedEntry<K, V> oldTail = header.prv;
        e.nxt = header;
        e.prv = oldTail;
        oldTail.nxt = header.prv = e;
        modCount++;
    }

    @Override void preModify(HashMapEntry<K, V> e) {
        if (accessOrder) {
            makeTail((LinkedEntry<K, V>) e);
        }
    }

    @Override void postRemove(HashMapEntry<K, V> e) {
        LinkedEntry<K, V> le = (LinkedEntry<K, V>) e;
        le.prv.nxt = le.nxt;
        le.nxt.prv = le.prv;
        le.nxt = le.prv = null; // Help the GC (for performance)
    }

    /**
     * This override is done for LinkedHashMap performance: iteration is cheaper
     * via LinkedHashMap nxt links.
     */
    @Override public boolean containsValue(Object value) {
        if (value == null) {
            for (LinkedEntry<K, V> header = this.header, e = header.nxt;
                    e != header; e = e.nxt) {
                if (e.value == null) {
                    return true;
                }
            }
            return entryForNullKey != null && entryForNullKey.value == null;
        }

        // value is non-null
        for (LinkedEntry<K, V> header = this.header, e = header.nxt;
                e != header; e = e.nxt) {
            if (value.equals(e.value)) {
                return true;
            }
        }
        return entryForNullKey != null && value.equals(entryForNullKey.value);
    }
    
    public void clear() {
        super.clear();

        // Clear all links to help GC
        LinkedEntry<K, V> header = this.header;
        LinkedEntry<K, V> e = header;
        do {
            LinkedEntry<K, V> nxt = e.nxt;
            e.nxt = e.prv = null;
            e = nxt;
        } while(e != header);

        header.nxt = header.prv = header;
    }

    private abstract class LinkedHashIterator<T> implements Iterator<T> {
        LinkedEntry<K, V> next = header.nxt;
        LinkedEntry<K, V> lastReturned = null;
        int expectedModCount = modCount;

        public final boolean hasNext() {
            return next != header;
        }

        final LinkedEntry<K, V> nextEntry() {
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
            LinkedEntry<K, V> e = next;
            if (e == header)
                throw new NoSuchElementException();
            next = e.nxt;
            return lastReturned = e;
        }

        public final void remove() {
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
            if (lastReturned == null)
                throw new IllegalStateException();
            LinkedHashMap.this.remove(lastReturned.key);
            lastReturned = null;
            expectedModCount = modCount;
        }
    }

    private final class KeyIterator extends LinkedHashIterator<K> {
        public final K next() { return nextEntry().key; }
    }

    private final class ValueIterator extends LinkedHashIterator<V> {
        public final V next() { return nextEntry().value; }
    }

    private final class EntryIterator
            extends LinkedHashIterator<Map.Entry<K, V>> {
        public final Map.Entry<K, V> next() { return nextEntry(); }
    }

    // Override view iterator methods to generate correct iteration order
    @Override Iterator<K> newKeyIterator() {
        return new KeyIterator();
    }
    @Override Iterator<V> newValueIterator() {
        return new ValueIterator();
    }
    @Override Iterator<Map.Entry<K, V>> newEntryIterator() {
        return new EntryIterator();
    }

    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return false;
    }

    private static final long serialVersionUID = 3801124242820219131L;
}
