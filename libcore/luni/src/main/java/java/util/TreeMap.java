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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * TreeMap is an implementation of SortedMap. All optional operations are
 * supported, adding and removing. The values can be any objects. The keys can
 * be any objects which are comparable to each other either using their natural
 * order or a specified Comparator.
 * @since 1.2
 */
public class TreeMap<K, V> extends AbstractMap<K, V> implements SortedMap<K, V>, Cloneable,
        Serializable {
    private static final long serialVersionUID = 919286545866124006L;

    transient int size;

    transient Entry<K, V> root;

    private Comparator<? super K> comparator;

    transient int modCount;

    transient Set<Map.Entry<K, V>> entrySet;

    /**
     * Entry is an internal class which is used to hold the entries of a
     * TreeMap.
     */
    static class Entry<K, V> extends MapEntry<K, V> {
        Entry<K, V> parent, left, right;

        boolean color;

        Entry(K key) {
            super(key);
        }

        Entry(K key, V value) {
            super(key, value);
        }

        @SuppressWarnings("unchecked")
        Entry<K, V> clone(Entry<K, V> parent) {
            Entry<K, V> clone = (Entry<K, V>) super.clone();
            clone.parent = parent;
            if (left != null) {
                clone.left = left.clone(clone);
            }
            if (right != null) {
                clone.right = right.clone(clone);
            }
            return clone;
        }
    }
    
    @SuppressWarnings("unchecked")
    private static <T> Comparable<T> toComparable(T obj) {
        return (Comparable<T>)obj;
    }

    private static class AbstractMapIterator <K,V> {
        TreeMap<K, V> backingMap;
        int expectedModCount;
        TreeMap.Entry<K, V> node;
        TreeMap.Entry<K, V> lastNode;

        AbstractMapIterator(TreeMap<K, V> map, Entry<K, V> startNode) {
            backingMap = map;
            expectedModCount = map.modCount;
            node = startNode;
        }

        public boolean hasNext() {
            return node != null;
        }

        final public void remove() {
            if (expectedModCount == backingMap.modCount) {
                if (lastNode != null) {
                    backingMap.rbDelete(lastNode);
                    lastNode = null;
                    expectedModCount++;
                } else {
                    throw new IllegalStateException();
                }
            } else {
                throw new ConcurrentModificationException();
            }
        }

        final void makeNext() {
            if (expectedModCount != backingMap.modCount) {
                throw new ConcurrentModificationException();
            } else if (node == null) {
                throw new NoSuchElementException();
            }
            lastNode = node;
            node = TreeMap.successor(node);
            }
        }

        private static class UnboundedEntryIterator <K, V> extends AbstractMapIterator<K, V> implements Iterator<Map.Entry<K, V>> {

            UnboundedEntryIterator(TreeMap<K, V> map, Entry<K, V> startNode) {
                super(map, startNode);
            }

            UnboundedEntryIterator(TreeMap<K, V> map) {
                super(map, map.root == null ? null : TreeMap.minimum(map.root));
            }

            public Map.Entry<K, V> next() {
                makeNext();
                return lastNode;
            }
        }

        static class UnboundedKeyIterator <K, V> extends AbstractMapIterator<K, V> implements Iterator<K> {
            public UnboundedKeyIterator(TreeMap<K, V> treeMap, Entry<K, V> entry) {
                super(treeMap, entry);
            }

            public UnboundedKeyIterator(TreeMap<K, V> map) {
                super(map, map.root == null ? null : TreeMap.minimum(map.root));
            }

            public K next() {
                makeNext();
                return lastNode.key;
            }
        }

        static class UnboundedValueIterator <K, V> extends AbstractMapIterator<K, V> implements Iterator<V> {
     
            public UnboundedValueIterator(TreeMap<K, V> treeMap, Entry<K, V> startNode) {
                super(treeMap, startNode);
            }
     
            public UnboundedValueIterator(TreeMap<K, V> map) {
                super(map, map.root == null ? null : TreeMap.minimum(map.root));
            }
     
            public V next() {
                makeNext();
                return lastNode.value;
            }
        }

        private static class ComparatorBoundedIterator<K, V> extends AbstractMapIterator<K, V> {
            private final  K endKey;

            private final Comparator<? super K> cmp;

        ComparatorBoundedIterator(TreeMap<K, V> map, Entry<K, V> startNode, K end) {
            super(map, startNode);
            endKey = end;
            cmp = map.comparator();
        }

        final void cleanNext() {
            if (node != null && cmp.compare(endKey, node.key) <= 0) {
                node = null;
            }
        }

        @Override
        public boolean hasNext() {
            return (node != null && endKey != null) && (cmp.compare(node.key, endKey) < 0);
        }
    }

    private static class ComparatorBoundedEntryIterator<K, V> extends
            ComparatorBoundedIterator<K, V> implements Iterator<Map.Entry<K, V>> {

        ComparatorBoundedEntryIterator(TreeMap<K, V> map, Entry<K, V> startNode, K end) {
            super(map, startNode, end);
        }

        public Map.Entry<K, V> next() {
            makeNext();
            cleanNext();
            return lastNode;
        }
    }

    private static class ComparatorBoundedKeyIterator<K, V> extends
            ComparatorBoundedIterator<K, V> implements Iterator<K> {

        ComparatorBoundedKeyIterator(TreeMap<K, V> map, Entry<K, V> startNode, K end) {
            super(map, startNode, end);
        }

        public K next() {
            makeNext();
            cleanNext();
            return lastNode.key;
        }
    }

    private static class ComparatorBoundedValueIterator<K, V> extends
            ComparatorBoundedIterator<K, V> implements Iterator<V> {

        ComparatorBoundedValueIterator(TreeMap<K, V> map, Entry<K, V> startNode, K end) {
            super(map, startNode, end);
        }

        public V next() {
            makeNext();
            cleanNext();
            return lastNode.value;
        }
    }

    private static class ComparableBoundedIterator<K, V> extends AbstractMapIterator<K, V> {
        private final Comparable<K> endKey;

        public ComparableBoundedIterator(TreeMap<K, V> treeMap, Entry<K, V> entry,
                Comparable<K> endKey) {
            super(treeMap, entry);
            this.endKey = endKey;
        }

        final void cleanNext() {
            if ((node != null) && (endKey.compareTo(node.key) <= 0)) {
                node = null;
            }
        }

        @Override
        public boolean hasNext() {
            return (node != null) && (endKey.compareTo(node.key) > 0);
        }
    }

    private static class ComparableBoundedEntryIterator<K, V> extends
            ComparableBoundedIterator<K, V> implements Iterator<Map.Entry<K, V>> {

        ComparableBoundedEntryIterator(TreeMap<K, V> map, Entry<K, V> startNode,
                Comparable<K> end) {
            super(map, startNode, end);
        }

        public Map.Entry<K, V> next() {
            makeNext();
            cleanNext();
            return lastNode;
        }

    }

    private static class ComparableBoundedKeyIterator<K, V> extends
            ComparableBoundedIterator<K, V> implements Iterator<K> {

        ComparableBoundedKeyIterator(TreeMap<K, V> map, Entry<K, V> startNode, Comparable<K> end) {
            super(map, startNode, end);
        }

        public K next() {
            makeNext();
            cleanNext();
            return lastNode.key;
        }
    }

    private static class ComparableBoundedValueIterator<K, V> extends
            ComparableBoundedIterator<K, V> implements Iterator<V> {

        ComparableBoundedValueIterator(TreeMap<K, V> map, Entry<K, V> startNode,
                Comparable<K> end) {
            super(map, startNode, end);
        }

        public V next() {
            makeNext();
            cleanNext();
            return lastNode.value;
        }
    }

        static final class SubMap<K,V> extends AbstractMap<K,V> implements SortedMap<K,V>, Serializable {
            private static final long serialVersionUID = -6520786458950516097L;

            private TreeMap<K,V> backingMap;

            boolean hasStart, hasEnd;

            K startKey, endKey;

            transient Set<Map.Entry<K,V>> entrySet = null;

            SubMap(K start, TreeMap<K,V> map) {
                backingMap = map;
                hasStart = true;
                startKey = start;
            }

            SubMap(K start, TreeMap<K,V> map, K end) {
                backingMap = map;
                hasStart = hasEnd = true;
                startKey = start;
                endKey = end;
            }
            
            SubMap(TreeMap<K,V> map, K end) {
                backingMap = map;
                hasEnd = true;
                endKey = end;
            }

            private void checkRange(K key) {
                Comparator<? super K> cmp = backingMap.comparator;
                if (cmp == null) {
                    Comparable<K> object = toComparable(key);
                    if (hasStart && object.compareTo(startKey) < 0) {
                        throw new IllegalArgumentException();
                    }
                    if (hasEnd && object.compareTo(endKey) > 0) {
                        throw new IllegalArgumentException();
                    }
                } else {
                    if (hasStart
                            && backingMap.comparator().compare(key, startKey) < 0) {
                        throw new IllegalArgumentException();
                    }
                    if (hasEnd && backingMap.comparator().compare(key, endKey) > 0) {
                        throw new IllegalArgumentException();
                    }
                }
            }

            private boolean isInRange(K key) {
                Comparator<? super K> cmp = backingMap.comparator;
                if (cmp == null) {
                    Comparable<K> object = toComparable(key);
                    if (hasStart && object.compareTo(startKey) < 0) {
                        return false;
                    }
                    if (hasEnd && object.compareTo(endKey) >= 0) {
                        return false;
                    }
                } else {
                    if (hasStart && cmp.compare(key, startKey) < 0) {
                        return false;
                    }
                    if (hasEnd && cmp.compare(key, endKey) >= 0) {
                        return false;
                    }
                }
                return true;
            }

            private boolean checkUpperBound(K key) {
                if (hasEnd) {
                    Comparator<? super K> cmp = backingMap.comparator;
                    if (cmp == null) {
                        return (toComparable(key).compareTo(endKey) < 0);
                    }
                    return (cmp.compare(key, endKey) < 0);
                }
                return true;
            }

            private boolean checkLowerBound(K key) {
                if (hasStart) {
                    Comparator<? super K> cmp = backingMap.comparator;
                    if (cmp == null) {
                        return (toComparable(key).compareTo(startKey) >= 0);
                    }
                    return (cmp.compare(key, startKey) >= 0);
                }
                return true;
            }

            public Comparator<? super K> comparator() {
                return backingMap.comparator();
            }

            @SuppressWarnings("unchecked")
            @Override
            public boolean containsKey(Object key) {
                if (isInRange((K)key)) {
                    return backingMap.containsKey(key);
                }
                return false;
            }

            @Override
            public Set<Map.Entry<K,V>> entrySet() {
                if(entrySet==null) {
                    entrySet = new SubMapEntrySet<K,V>(this);
                }
                return entrySet;
            }

            public K firstKey() {
                TreeMap.Entry<K,V> node = firstEntry();
                if (node != null ) {
                    return node.key;
                }
                throw new NoSuchElementException();
            }

            TreeMap.Entry<K,V> firstEntry() {
                if (!hasStart) {
                    TreeMap.Entry<K,V> root = backingMap.root;
                    return (root == null) ? null : minimum(backingMap.root);
                }
                TreeMap.Entry<K,V> node = backingMap.findAfter(startKey);
                if (node != null && checkUpperBound(node.key)) {
                    return node;
                }
                return null;
            }

            @SuppressWarnings("unchecked")
            @Override
            public V get(Object key) {
                if (isInRange((K)key)) {
                    return backingMap.get(key);
                }
                return null;
            }

            public SortedMap<K,V> headMap(K endKey) {
                checkRange(endKey);
                if (hasStart) {
                    return new SubMap<K,V>(startKey, backingMap, endKey);
                }
                return new SubMap<K,V>(backingMap, endKey);
            }

            @Override
            public boolean isEmpty() {
                if (hasStart) {
                    TreeMap.Entry<K,V> node = backingMap.findAfter(startKey);
                    return node == null || !checkUpperBound(node.key);
                }
                return backingMap.findBefore(endKey) == null;
            }

            @Override
            public Set<K> keySet() {
                if (keySet == null) {
                    keySet = new SubMapKeySet<K,V>(this);
                }
                return keySet;
            }

            public K lastKey() {
                if (!hasEnd) {
                    return backingMap.lastKey();
                }
                TreeMap.Entry<K,V> node = backingMap.findBefore(endKey);
                if (node != null && checkLowerBound(node.key)) {
                    return node.key;
                }
                throw new NoSuchElementException();
            }

            @Override
            public V put(K key, V value) {
                if (isInRange(key)) {
                    return backingMap.put(key, value);
                }
                throw new IllegalArgumentException();
            }

            @SuppressWarnings("unchecked")
            @Override
            public V remove(Object key) {
                if (isInRange((K)key)) {
                    return backingMap.remove(key);
                }
                return null;
            }

            public SortedMap<K,V> subMap(K startKey, K endKey) {
                checkRange(startKey);
                checkRange(endKey);
                Comparator<? super K> c = backingMap.comparator();
                if (c == null) {
                    if (toComparable(startKey).compareTo(endKey) <= 0) {
                        return new SubMap<K,V>(startKey, backingMap, endKey);
                    }
                } else {
                    if (c.compare(startKey, endKey) <= 0) {
                        return new SubMap<K,V>(startKey, backingMap, endKey);
                    }
                }
                throw new IllegalArgumentException();
            }

            public SortedMap<K,V> tailMap(K startKey) {
                checkRange(startKey);
                if (hasEnd) {
                    return new SubMap<K,V>(startKey, backingMap, endKey);
                }
                return new SubMap<K,V>(startKey, backingMap);
            }

            @Override
            public Collection<V> values() {
                if(valuesCollection==null) {
                    valuesCollection = new SubMapValuesCollection<K,V>(this);
                }
                return valuesCollection;
            }
        }

        static class SubMapEntrySet<K,V> extends AbstractSet<Map.Entry<K,V>> implements Set<Map.Entry<K,V>> {
            SubMap<K,V> subMap;

            SubMapEntrySet(SubMap<K,V> map) {
                subMap = map;
            }

            @Override
            public boolean isEmpty() {
                return subMap.isEmpty();
            }

            @Override
            public Iterator<Map.Entry<K,V>> iterator() {
                TreeMap.Entry<K,V> startNode = subMap.firstEntry();
                if (subMap.hasEnd) {
                    Comparator<? super K> cmp = subMap.comparator();
                    if (cmp == null) {
                        return new ComparableBoundedEntryIterator<K,V>(subMap.backingMap, startNode, toComparable(subMap.endKey));
                    }
                    return new ComparatorBoundedEntryIterator<K,V>(subMap.backingMap, startNode, subMap.endKey);
                }
                return new UnboundedEntryIterator<K,V>(subMap.backingMap, startNode);
            }

            @Override
            public int size() {
                int size = 0;
                Iterator<Map.Entry<K,V>> it = iterator();
                while (it.hasNext()) {
                    size++;
                    it.next();
                }
                return size;
            }

            @SuppressWarnings("unchecked")
            @Override
            public boolean contains(Object object) {
                if (object instanceof Map.Entry) {
                    Map.Entry<K,V> entry = (Map.Entry<K,V>) object;
                    K key = entry.getKey();
                    if (subMap.isInRange(key)) {
                        V v1 = subMap.get(key), v2 = entry.getValue();
                        return v1 == null ? v2 == null : v1.equals(v2);
                    }
                }
                return false;
            }

        }

        static class SubMapKeySet<K,V> extends  AbstractSet<K> implements Set<K> {
            SubMap<K,V> subMap;

            SubMapKeySet(SubMap<K,V> map) {
                subMap = map;
            }

            @Override
            public boolean contains(Object object) {
                return subMap.containsKey(object);
            }

            @Override
            public boolean isEmpty() {
                return subMap.isEmpty();
            }

            @Override
            public int size() {
                int size = 0;
                Iterator<K> it = iterator();
                while (it.hasNext()) {
                    size++;
                    it.next();
                }
                return size;
            }

            @Override
            public Iterator<K> iterator() {
                TreeMap.Entry<K,V> startNode = subMap.firstEntry();
                if (subMap.hasEnd) {
                    Comparator<? super K> cmp = subMap.comparator();
                    if (cmp == null) {
                        return new ComparableBoundedKeyIterator<K,V>(subMap.backingMap, startNode, toComparable(subMap.endKey));
                    }
                    return new ComparatorBoundedKeyIterator<K,V>(subMap.backingMap, startNode, subMap.endKey);
                }
                return new UnboundedKeyIterator<K,V>(subMap.backingMap, startNode);
            }
        }

        static class SubMapValuesCollection<K,V> extends AbstractCollection<V> {
            SubMap<K,V> subMap;

            public SubMapValuesCollection(SubMap<K,V> subMap) {
                this.subMap = subMap;
            }

            @Override
            public boolean isEmpty() {
                return subMap.isEmpty();
            }

            @Override
            public Iterator<V> iterator() {
                TreeMap.Entry<K,V> startNode = subMap.firstEntry();
                if (subMap.hasEnd) {
                    Comparator<? super K> cmp = subMap.comparator();
                    if (cmp == null) {
                        return new ComparableBoundedValueIterator<K,V>(subMap.backingMap, startNode, toComparable(subMap.endKey));
                    }
                    return new ComparatorBoundedValueIterator<K,V>(subMap.backingMap, startNode, subMap.endKey);
                }
                return new UnboundedValueIterator<K,V>(subMap.backingMap, startNode);
            }

            @Override
            public int size() {
                int cnt = 0;
                for (Iterator<V> it = iterator(); it.hasNext();) {
                    it.next();
                    cnt++;
                }
                return cnt;
            }
        }

    /**
     * Constructs a new empty instance of TreeMap.
     * 
     */
    public TreeMap() {
        super();
    }

    /**
     * Constructs a new empty instance of TreeMap which uses the specified
     * Comparator.
     * 
     * @param comparator
     *            the Comparator
     */
    public TreeMap(Comparator<? super K> comparator) {
        this.comparator = comparator;
    }

    /**
     * Constructs a new instance of TreeMap containing the mappings from the
     * specified Map and using the natural ordering.
     * 
     * @param map
     *            the mappings to add
     * 
     * @exception ClassCastException
     *                when a key in the Map does not implement the Comparable
     *                interface, or they keys in the Map cannot be compared
     */
    public TreeMap(Map<? extends K,? extends V> map) {
        this();
        putAll(map);
    }

    /**
     * Constructs a new instance of TreeMap containing the mappings from the
     * specified SortedMap and using the same Comparator.
     * 
     * @param map
     *            the mappings to add
     */
    public TreeMap(SortedMap<K,? extends V> map) {
        this(map.comparator());
        Iterator<? extends Map.Entry<K, ? extends V>> it = map.entrySet().iterator();
        if (it.hasNext()) {
            Map.Entry<K, ? extends V> entry = it.next();
            Entry<K, V> last = new Entry<K, V>(entry.getKey(), entry.getValue());
            root = last;
            size = 1;
            while (it.hasNext()) {
                entry = it.next();
                Entry<K, V> x = new Entry<K, V>(entry.getKey(), entry.getValue());
                x.parent = last;
                last.right = x;
                size++;
                balance(x);
                last = x;
            }
        }
    }

    void balance(Entry<K, V> x) {
        Entry<K, V> y;
        x.color = true;
        while (x != root && x.parent.color) {
            if (x.parent == x.parent.parent.left) {
                y = x.parent.parent.right;
                if (y != null && y.color) {
                    x.parent.color = false;
                    y.color = false;
                    x.parent.parent.color = true;
                    x = x.parent.parent;
                } else {
                    if (x == x.parent.right) {
                        x = x.parent;
                        leftRotate(x);
                    }
                    x.parent.color = false;
                    x.parent.parent.color = true;
                    rightRotate(x.parent.parent);
                }
            } else {
                y = x.parent.parent.left;
                if (y != null && y.color) {
                    x.parent.color = false;
                    y.color = false;
                    x.parent.parent.color = true;
                    x = x.parent.parent;
                } else {
                    if (x == x.parent.left) {
                        x = x.parent;
                        rightRotate(x);
                    }
                    x.parent.color = false;
                    x.parent.parent.color = true;
                    leftRotate(x.parent.parent);
                }
            }
        }
        root.color = false;
    }

    /**
     * Removes all mappings from this TreeMap, leaving it empty.
     * 
     * @see Map#isEmpty
     * @see #size
     */
    @Override
    public void clear() {
        root = null;
        size = 0;
        modCount++;
    }

    /**
     * Returns a new TreeMap with the same mappings, size and comparator as this
     * TreeMap.
     * 
     * @return a shallow copy of this TreeMap
     * 
     * @see java.lang.Cloneable
     */
    @SuppressWarnings("unchecked")
    @Override
    public Object clone() {
        try {
            TreeMap<K, V> clone = (TreeMap<K, V>) super.clone();
            clone.entrySet = null;
            if (root != null) {
                clone.root = root.clone(null);
            }
            return clone;
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    /**
     * Returns the Comparator used to compare elements in this TreeMap.
     * 
     * @return a Comparator or null if the natural ordering is used
     */
    public Comparator<? super K> comparator() {
        return comparator;
    }

    /**
     * Searches this TreeMap for the specified key.
     * 
     * @param key
     *            the object to search for
     * @return true if <code>key</code> is a key of this TreeMap, false
     *         otherwise
     * 
     * @exception ClassCastException
     *                when the key cannot be compared with the keys in this
     *                TreeMap
     * @exception NullPointerException
     *                when the key is null and the comparator cannot handle null
     */
    @Override
    public boolean containsKey(Object key) {
        return find(key) != null;
    }

    /**
     * Searches this TreeMap for the specified value.
     * 
     * @param value
     *            the object to search for
     * @return true if <code>value</code> is a value of this TreeMap, false
     *         otherwise
     */
    @Override
    public boolean containsValue(Object value) {
        if (root != null) {
            return containsValue(root, value);
        }
        return false;
    }

    private boolean containsValue(Entry<K, V> node, Object value) {
        if (value == null ? node.value == null : value.equals(node.value)) {
            return true;
        }
        if (node.left != null) {
            if (containsValue(node.left, value)) {
                return true;
            }
        }
        if (node.right != null) {
            if (containsValue(node.right, value)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns a Set of the mappings contained in this TreeMap. Each element in
     * the set is a Map.Entry. The set is backed by this TreeMap so changes to
     * one are reflected by the other. The set does not support adding.
     * 
     * @return a Set of the mappings
     */
    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        if (entrySet == null) {
            entrySet = new AbstractSet<Map.Entry<K, V>>() {
                 @Override
                public int size() {
                    return size;
                }

                @Override
                public void clear() {
                    TreeMap.this.clear();
                }

                @SuppressWarnings("unchecked")
                @Override
                public boolean contains(Object object) {
                    if (object instanceof Map.Entry) {
                        Map.Entry<K, V> entry = (Map.Entry<K, V>) object;
                        Object v1 = get(entry.getKey()), v2 = entry.getValue();
                        return v1 == null ? v2 == null : v1.equals(v2);
                    }
                    return false;
                }

                @Override
                public Iterator<Map.Entry<K, V>> iterator() {
                    return new UnboundedEntryIterator<K, V>(TreeMap.this);
                }
            };
        }
        return entrySet;
    }

    @SuppressWarnings("unchecked")
    private Entry<K, V> find(Object keyObj) {
        int result;
        K key = (K)keyObj;
        Comparable<K> object = null;
        if (comparator == null) {
            object = toComparable(key);
        }
        Entry<K, V> x = root;
        while (x != null) {
            result = object != null ? object.compareTo(x.key) : comparator
                    .compare(key, x.key);
            if (result == 0) {
                return x;
            }
            x = result < 0 ? x.left : x.right;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    Entry<K, V> findAfter(Object keyObj) {
        K key = (K)keyObj;
        int result;
        Comparable<K> object = null;
        if (comparator == null) {
            object = toComparable(key);
        }
        Entry<K, V> x = root, last = null;
        while (x != null) {
            result = object != null ? object.compareTo(x.key) : comparator
                    .compare(key, x.key);
            if (result == 0) {
                return x;
            }
            if (result < 0) {
                last = x;
                x = x.left;
            } else {
                x = x.right;
            }
        }
        return last;
    }

    Entry<K, V> findBefore(K key) {
        int result;
        Comparable<K> object = null;
        if (comparator == null) {
            object = toComparable(key);
        }
        Entry<K, V> x = root, last = null;
        while (x != null) {
            result = object != null ? object.compareTo(x.key) : comparator
                    .compare(key, x.key);
            if (result <= 0) {
                x = x.left;
            } else {
                last = x;
                x = x.right;
            }
        }
        return last;
    }

    /**
     * Answer the first sorted key in this TreeMap.
     * 
     * @return the first sorted key
     * 
     * @exception NoSuchElementException
     *                when this TreeMap is empty
     */
    public K firstKey() {
        if (root != null) {
            return minimum(root).key;
        }
        throw new NoSuchElementException();
    }

    private void fixup(Entry<K, V> x) {
        Entry<K, V> w;
        while (x != root && !x.color) {
            if (x == x.parent.left) {
                w = x.parent.right;
                if (w == null) {
                    x = x.parent;
                    continue;
                }
                if (w.color) {
                    w.color = false;
                    x.parent.color = true;
                    leftRotate(x.parent);
                    w = x.parent.right;
                    if (w == null) {
                        x = x.parent;
                        continue;
                    }
                }
                if ((w.left == null || !w.left.color)
                        && (w.right == null || !w.right.color)) {
                    w.color = true;
                    x = x.parent;
                } else {
                    if (w.right == null || !w.right.color) {
                        w.left.color = false;
                        w.color = true;
                        rightRotate(w);
                        w = x.parent.right;
                    }
                    w.color = x.parent.color;
                    x.parent.color = false;
                    w.right.color = false;
                    leftRotate(x.parent);
                    x = root;
                }
            } else {
                w = x.parent.left;
                if (w == null) {
                    x = x.parent;
                    continue;
                }
                if (w.color) {
                    w.color = false;
                    x.parent.color = true;
                    rightRotate(x.parent);
                    w = x.parent.left;
                    if (w == null) {
                        x = x.parent;
                        continue;
                    }
                }
                if ((w.left == null || !w.left.color)
                        && (w.right == null || !w.right.color)) {
                    w.color = true;
                    x = x.parent;
                } else {
                    if (w.left == null || !w.left.color) {
                        w.right.color = false;
                        w.color = true;
                        leftRotate(w);
                        w = x.parent.left;
                    }
                    w.color = x.parent.color;
                    x.parent.color = false;
                    w.left.color = false;
                    rightRotate(x.parent);
                    x = root;
                }
            }
        }
        x.color = false;
    }

    /**
     * Returns the value of the mapping with the specified key.
     * 
     * @param key
     *            the key
     * @return the value of the mapping with the specified key
     * 
     * @exception ClassCastException
     *                when the key cannot be compared with the keys in this
     *                TreeMap
     * @exception NullPointerException
     *                when the key is null and the comparator cannot handle null
     */
    @Override
    public V get(Object key) {
        Entry<K, V> node = find(key);
        if (node != null) {
            return node.value;
        }
        return null;
    }

    /**
     * Returns a SortedMap of the specified portion of this TreeMap which
     * contains keys less than the end key. The returned SortedMap is backed by
     * this TreeMap so changes to one are reflected by the other.
     * 
     * @param endKey
     *            the end key
     * @return a sub-map where the keys are less than <code>endKey</code>
     * 
     * @exception ClassCastException
     *                when the end key cannot be compared with the keys in this
     *                TreeMap
     * @exception NullPointerException
     *                when the end key is null and the comparator cannot handle
     *                null
     */
    public SortedMap<K, V> headMap(K endKey) {
        // Check for errors
        if (comparator == null) {
            toComparable(endKey).compareTo(endKey);
        } else {
            comparator.compare(endKey, endKey);
        }
        return new SubMap<K, V>(this, endKey);
    }

    /**
     * Returns a Set of the keys contained in this TreeMap. The set is backed by
     * this TreeMap so changes to one are reflected by the other. The set does
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
                    return size;
                }

                @Override
                public void clear() {
                    TreeMap.this.clear();
                }

                @Override
                public Iterator<K> iterator() {
                    return new UnboundedKeyIterator<K,V> (TreeMap.this);
                }
            };
        }
        return keySet;
    }

    /**
     * Answer the last sorted key in this TreeMap.
     * 
     * @return the last sorted key
     * 
     * @exception NoSuchElementException
     *                when this TreeMap is empty
     */
    public K lastKey() {
        if (root != null) {
            return maximum(root).key;
        }
        throw new NoSuchElementException();
    }

    private void leftRotate(Entry<K, V> x) {
        Entry<K, V> y = x.right;
        x.right = y.left;
        if (y.left != null) {
            y.left.parent = x;
        }
        y.parent = x.parent;
        if (x.parent == null) {
            root = y;
        } else {
            if (x == x.parent.left) {
                x.parent.left = y;
            } else {
                x.parent.right = y;
            }
        }
        y.left = x;
        x.parent = y;
    }

    static <K, V> Entry<K, V> maximum(Entry<K, V> x) {
        while (x.right != null) {
            x = x.right;
        }
        return x;
    }

    static <K, V> Entry<K, V> minimum(Entry<K, V> x) {
        while (x.left != null) {
            x = x.left;
        }
        return x;
    }

    static <K, V> Entry<K, V> predecessor(Entry<K, V> x) {
        if (x.left != null) {
            return maximum(x.left);
        }
        Entry<K, V> y = x.parent;
        while (y != null && x == y.left) {
            x = y;
            y = y.parent;
        }
        return y;
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
     * @exception ClassCastException
     *                when the key cannot be compared with the keys in this
     *                TreeMap
     * @exception NullPointerException
     *                when the key is null and the comparator cannot handle null
     */
    @Override
    public V put(K key, V value) {
        MapEntry<K, V> entry = rbInsert(key);
        V result = entry.value;
        entry.value = value;
        return result;
    }

    /**
     * Copies every mapping in the specified Map to this TreeMap.
     * 
     * @param map
     *            the Map to copy mappings from
     * 
     * @exception ClassCastException
     *                when a key in the Map cannot be compared with the keys in
     *                this TreeMap
     * @exception NullPointerException
     *                when a key in the Map is null and the comparator cannot
     *                handle null
     */
    @Override
    public void putAll(Map<? extends K, ? extends V> map) {
        super.putAll(map);
    }

    void rbDelete(Entry<K, V> z) {
        Entry<K, V> y = z.left == null || z.right == null ? z : successor(z);
        Entry<K, V> x = y.left != null ? y.left : y.right;
        if (x != null) {
            x.parent = y.parent;
        }
        if (y.parent == null) {
            root = x;
        } else if (y == y.parent.left) {
            y.parent.left = x;
        } else {
            y.parent.right = x;
        }
        modCount++;
        if (y != z) {
            z.key = y.key;
            z.value = y.value;
        }
        if (!y.color && root != null) {
            if (x == null) {
                fixup(y.parent);
            } else {
                fixup(x);
            }
        }
        size--;
    }

    private Entry<K, V> rbInsert(K object) {
        int result = 0;
        Entry<K, V> y = null;
        if (size != 0) {
            Comparable<K> key = null;
            if (comparator == null) {
                key = toComparable(object);
            }
            Entry<K, V> x = root;
            while (x != null) {
                y = x;
                result = key != null ? key.compareTo(x.key) : comparator
                        .compare(object, x.key);
                if (result == 0) {
                    return x;
                }
                x = result < 0 ? x.left : x.right;
            }
        }

        size++;
        modCount++;
        Entry<K, V> z = new Entry<K, V>(object);
        if (y == null) {
            return root = z;
        }
        z.parent = y;
        if (result < 0) {
            y.left = z;
        } else {
            y.right = z;
        }
        balance(z);
        return z;
    }

    /**
     * Removes a mapping with the specified key from this TreeMap.
     * 
     * @param key
     *            the key of the mapping to remove
     * @return the value of the removed mapping or null if key is not a key in
     *         this TreeMap
     * 
     * @exception ClassCastException
     *                when the key cannot be compared with the keys in this
     *                TreeMap
     * @exception NullPointerException
     *                when the key is null and the comparator cannot handle null
     */
    @Override
    public V remove(Object key) {
        if (size == 0) {
            return null;
        }
        Entry<K, V> node = find(key);
        if (node == null) {
            return null;
        }
        V result = node.value;
        rbDelete(node);
        return result;
    }

    private void rightRotate(Entry<K, V> x) {
        Entry<K, V> y = x.left;
        x.left = y.right;
        if (y.right != null) {
            y.right.parent = x;
        }
        y.parent = x.parent;
        if (x.parent == null) {
            root = y;
        } else {
            if (x == x.parent.right) {
                x.parent.right = y;
            } else {
                x.parent.left = y;
            }
        }
        y.right = x;
        x.parent = y;
    }

    /**
     * Returns the number of mappings in this TreeMap.
     * 
     * @return the number of mappings in this TreeMap
     */
    @Override
    public int size() {
        return size;
    }

    /**
     * Returns a SortedMap of the specified portion of this TreeMap which
     * contains keys greater or equal to the start key but less than the end
     * key. The returned SortedMap is backed by this TreeMap so changes to one
     * are reflected by the other.
     * 
     * @param startKey
     *            the start key
     * @param endKey
     *            the end key
     * @return a sub-map where the keys are greater or equal to
     *         <code>startKey</code> and less than <code>endKey</code>
     * 
     * @exception ClassCastException
     *                when the start or end key cannot be compared with the keys
     *                in this TreeMap
     * @exception NullPointerException
     *                when the start or end key is null and the comparator
     *                cannot handle null
     */
    public SortedMap<K, V> subMap(K startKey, K endKey) {
        if (comparator == null) {
            if (toComparable(startKey).compareTo(endKey) <= 0) {
                return new SubMap<K, V>(startKey, this, endKey);
            }
        } else {
            if (comparator.compare(startKey, endKey) <= 0) {
                return new SubMap<K, V>(startKey, this, endKey);
            }
        }
        throw new IllegalArgumentException();
    }

    static <K, V> Entry<K, V> successor(Entry<K, V> x) {
        if (x.right != null) {
            return minimum(x.right);
        }
        Entry<K, V> y = x.parent;
        while (y != null && x == y.right) {
            x = y;
            y = y.parent;
        }
        return y;
    }

    /**
     * Returns a SortedMap of the specified portion of this TreeMap which
     * contains keys greater or equal to the start key. The returned SortedMap
     * is backed by this TreeMap so changes to one are reflected by the other.
     * 
     * @param startKey
     *            the start key
     * @return a sub-map where the keys are greater or equal to
     *         <code>startKey</code>
     * 
     * @exception ClassCastException
     *                when the start key cannot be compared with the keys in
     *                this TreeMap
     * @exception NullPointerException
     *                when the start key is null and the comparator cannot
     *                handle null
     */
    public SortedMap<K, V> tailMap(K startKey) {
        // Check for errors
        if (comparator == null) {
            toComparable(startKey).compareTo(startKey);
        } else {
            comparator.compare(startKey, startKey);
        }
        return new SubMap<K, V>(startKey, this);
    }

    /**
     * Returns a Collection of the values contained in this TreeMap. The
     * collection is backed by this TreeMap so changes to one are reflected by
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
                    return size;
                }

                @Override
                public void clear() {
                    TreeMap.this.clear();
                }

                @Override
                public Iterator<V> iterator() {
                    return new UnboundedValueIterator<K,V> (TreeMap.this);
                }
            };
        }
        return valuesCollection;
    }

    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.defaultWriteObject();
        stream.writeInt(size);
        if (size > 0) {
            Entry<K, V> node = minimum(root);
            while (node != null) {
                stream.writeObject(node.key);
                stream.writeObject(node.value);
                node = successor(node);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void readObject(ObjectInputStream stream) throws IOException,
            ClassNotFoundException {
        stream.defaultReadObject();
        size = stream.readInt();
        Entry<K, V> last = null;
        for (int i = size; --i >= 0;) {
            Entry<K, V> node = new Entry<K, V>((K)stream.readObject());
            node.value = (V)stream.readObject();
            if (last == null) {
                root = node;
            } else {
                node.parent = last;
                last.right = node;
                balance(node);
            }
            last = node;
        }
    }
}
