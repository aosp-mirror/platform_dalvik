/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.apache.harmony.luni.tests.java.util;

import dalvik.annotation.TestTargets;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargetClass;

import junit.framework.TestCase;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.RandomAccess;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

import org.apache.harmony.testframework.serialization.SerializationTest;
import org.apache.harmony.testframework.serialization.SerializationTest.SerializableAssert;

import tests.util.SerializationTester;

@TestTargetClass(Collections.class) 
public class CollectionsTest extends TestCase {

    private static final SerializableAssert comparator = new SerializableAssert() {
        public void assertDeserialized(Serializable reference, Serializable test) {
            assertSame(reference, test);
        }
    };
    
    /**
     * @tests java.util.Collections#binarySearch(java.util.List,
     *        java.lang.Object, java.util.Comparator)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "binarySearch",
        args = {java.util.List.class, java.lang.Object.class, java.util.Comparator.class}
    )
    public void test_binarySearchLjava_util_ListLjava_lang_ObjectLjava_util_Comparator() {
        // Regression for HARMONY-94
        LinkedList<Integer> lst = new LinkedList<Integer>();
        lst.add(new Integer(30));
        Collections.sort(lst, null);
        int index = Collections.binarySearch(lst, new Integer(2), null);
        assertEquals(-1, index);

        LinkedList<String> lls = new LinkedList<String>();
        lls.add("1");
        lls.add("2");
        lls.add("3");
        lls.add("4");
        lls.add("");
        LinkedList<String> ll = lls;

        try {
            Collections.binarySearch(ll, new Integer(10), null);
            fail("ClassCastException expected");
        } catch (ClassCastException e) {
            //expected
        }
    } 
    
    /**
     * @tests java.util.Collections#binarySearch(java.util.List,
     *        java.lang.Object)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "binarySearch",
        args = {java.util.List.class, java.lang.Object.class}
    )
    @SuppressWarnings("unchecked")
    public void test_binarySearchLjava_util_ListLjava_lang_Object() {
        // regression for Harmony-1367
        List localList = new LinkedList();
        assertEquals(-1, Collections.binarySearch(localList, new Object()));
        localList.add(new Object());
        try {
            Collections.binarySearch(localList, new Integer(1));
            fail("Should throw ClassCastException");
        } catch (ClassCastException e) {
            // expected
        }

        LinkedList<String> lls = new LinkedList<String>();
        lls.add("1");
        lls.add("2");
        lls.add("3");
        lls.add("4");
        lls.add("");
        LinkedList ll = lls;

        try {
            Collections.binarySearch(ll, new Integer(10));
            fail("ClassCastException expected");
        } catch (ClassCastException e) {
            //expected
        }
    }
       
    /**
     * @tests java.util.Collections#rotate(java.util.List, int)
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "UnsupportedOperationException is not tested.",
        method = "rotate",
        args = {java.util.List.class, int.class}
    )
    public void test_rotateLjava_util_ListI() {
        // Regression for HARMONY-19 Rotate an *empty* list
        Collections.rotate(new ArrayList<Object>(), 25);

        // Regression for HARMONY-20
        List<String> list = new ArrayList<String>();
        list.add(0, "zero");
        list.add(1, "one");
        list.add(2, "two");
        list.add(3, "three");
        list.add(4, "four");

        Collections.rotate(list, Integer.MIN_VALUE);
        assertEquals("Rotated incorrectly at position 0, ", "three",
                list.get(0));
        assertEquals("Rotated incorrectly at position 1, ", "four",
                list.get(1));
        assertEquals("Rotated incorrectly at position 2, ", "zero",
                list.get(2));
        assertEquals("Rotated incorrectly at position 3, ", "one",
                list.get(3));
        assertEquals("Rotated incorrectly at position 4, ", "two",
                list.get(4));
    }

    /**
     * @tests java.util.Collections#synchronizedCollection(java.util.Collection)
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies NullPointerException.",
        method = "synchronizedCollection",
        args = {java.util.Collection.class}
    )
    public void test_synchronizedCollectionLjava_util_Collection() {
        try {
            // Regression for HARMONY-93
            Collections.synchronizedCollection(null);
            fail("Assert 0: synchronizedCollection(null) must throw NPE");
        } catch (NullPointerException e) {
            // expected
        }
    }

    /**
     * @tests java.util.Collections#synchronizedSortedMap(java.util.SortedMap)
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies NullPointerException.",
        method = "synchronizedSortedMap",
        args = {java.util.SortedMap.class}
    )
    public void test_synchronizedSortedMapLjava_util_SortedMap() {
        try {
            // Regression for HARMONY-93
            Collections.synchronizedSortedMap(null);
            fail("Assert 0: synchronizedSortedMap(null) must throw NPE");
        } catch (NullPointerException e) {
            // expected
        }
    }

    /**
     * @tests java.util.Collections#synchronizedMap(java.util.Map)
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies NullPointerException.",
        method = "synchronizedMap",
        args = {java.util.Map.class}
    )
    public void test_synchronizedMapLjava_util_Map() {
        try {
            // Regression for HARMONY-93
            Collections.synchronizedMap(null);
            fail("Assert 0: synchronizedMap(map) must throw NPE");
        } catch (NullPointerException e) {
            // expected
        }
    }

    /**
     * @tests java.util.Collections#synchronizedSet(java.util.Set)
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies NullPointerException.",
        method = "synchronizedSet",
        args = {java.util.Set.class}
    )
    public void test_synchronizedSetLjava_util_Set() {
        try {
            // Regression for HARMONY-93
            Collections.synchronizedSet(null);
            fail("Assert 0: synchronizedSet(set) must throw NPE");
        } catch (NullPointerException e) {
            // expected
        }
    }

    /**
     * @tests java.util.Collections#synchronizedSortedSet(java.util.SortedSet)
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies NullPointerException.",
        method = "synchronizedSortedSet",
        args = {java.util.SortedSet.class}
    )
    public void test_synchronizedSortedSetLjava_util_SortedSet() {
        try {
            // Regression for HARMONY-93
            Collections.synchronizedSortedSet(null);
            fail("Assert 0: synchronizedSortedSet(null) must throw NPE");
        } catch (NullPointerException e) {
            // expected
        }
    }

    /**
     * @tests java.util.Collections#unmodifiableCollection(java.util.Collection)
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies NullPointerException.",
        method = "unmodifiableCollection",
        args = {java.util.Collection.class}
    )
    public void test_unmodifiableCollectionLjava_util_Collection() {
        try {
            // Regression for HARMONY-93
            Collections.unmodifiableCollection(null);
            fail("Assert 0: unmodifiableCollection(null) must throw NPE");
        } catch (NullPointerException e) {
            // expected
        }
    }

    /**
     * @tests java.util.Collections#unmodifiableMap(java.util.Map)
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies NullPointerException.",
        method = "unmodifiableMap",
        args = {java.util.Map.class}
    )
    public void test_unmodifiableMapLjava_util_Map() {
        try {
            // Regression for HARMONY-93
            Collections.unmodifiableMap(null);
            fail("Assert 0: unmodifiableMap(null) must throw NPE");
        } catch (NullPointerException e) {
            // expected
        }
    }

    /**
     * @tests java.util.Collections#unmodifiableSet(java.util.Set)
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies NullPointerException.",
        method = "unmodifiableSet",
        args = {java.util.Set.class}
    )
    public void test_unmodifiableSetLjava_util_Set() {
        try {
            // Regression for HARMONY-93
            Collections.unmodifiableSet(null);
            fail("Assert 0: unmodifiableSet(null) must throw NPE");
        } catch (NullPointerException e) {
            // expected
        }
    }

    /**
     * @tests java.util.Collections#unmodifiableSortedMap(java.util.SortedMap)
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies NullPointerException.",
        method = "unmodifiableSortedMap",
        args = {java.util.SortedMap.class}
    )
    public void test_unmodifiableSortedMapLjava_util_SortedMap() {
        try {
            // Regression for HARMONY-93
            Collections.unmodifiableSortedMap(null);
            fail("Assert 0: unmodifiableSortedMap(null) must throw NPE");
        } catch (NullPointerException e) {
            // expected
        }
    }

    /**
     * @tests java.util.Collections#unmodifiableSortedSet(java.util.SortedSet)
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies NullPointerException.",
        method = "unmodifiableSortedSet",
        args = {java.util.SortedSet.class}
    )
    public void test_unmodifiableSortedSetLjava_util_SortedSet() {
        try {
            // Regression for HARMONY-93
            Collections.unmodifiableSortedSet(null);
            fail("Assert 0: unmodifiableSortedSet(null) must throw NPE");
        } catch (NullPointerException e) {
            // expected
        }
    }
    
    /**
     * @tests java.util.Collections#frequency(java.util.Collection,Object)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "frequency",
        args = {java.util.Collection.class, java.lang.Object.class}
    )
    public void test_frequencyLjava_util_CollectionLint() {
        try {
            Collections.frequency(null, null);
            fail("Assert 0: frequency(null,<any>) must throw NPE");
        } catch (NullPointerException e) {}

        List<String> strings = Arrays.asList(new String[] { "1", "2", "3", "1", "1" });

        assertEquals("Assert 1: did not find three \"1\" strings", 3,
                Collections.frequency(strings, "1"));

        assertEquals("Assert 2: did not find one \"2\" strings", 1, Collections
                .frequency(strings, "2"));

        assertEquals("Assert 3: did not find three \"3\" strings", 1,
                Collections.frequency(strings, "3"));

        assertEquals("Assert 4: matched on null when there are none", 0,
                Collections.frequency(strings, null));

        List<Object> objects = Arrays.asList(new Object[] { new Integer(1), null, null,
                new Long(1) });

        assertEquals("Assert 5: did not find one Integer(1)", 1, Collections
                .frequency(objects, new Integer(1)));

        assertEquals("Assert 6: did not find one Long(1)", 1, Collections
                .frequency(objects, new Long(1)));

        assertEquals("Assert 7: did not find two null references", 2,
                Collections.frequency(objects, null));
    }

    /**
     * @tests java.util.Collections#reverseOrder()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "reverseOrder",
        args = {}
    )
    public void test_reverseOrder() {
        Comparator<String> roc = Collections.reverseOrder();
        assertNotNull("Assert 0: comparator must not be null", roc);

        assertTrue("Assert 1: comparator must implement Serializable",
                roc instanceof Serializable);

        String[] fixtureDesc = new String[] { "2", "1", "0" };
        String[] numbers = new String[] { "0", "1", "2" };
        Arrays.sort(numbers, roc);
        assertTrue("Assert 2: the arrays are not equal, the sort failed",
                Arrays.equals(fixtureDesc, numbers));
    }

    /**
     * @tests java.util.Collections#reverseOrder(java.util.Comparator)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "reverseOrder",
        args = {java.util.Comparator.class}
    )
    public void test_reverseOrderLjava_util_Comparator() {
        Comparator<String> roc = Collections
                .reverseOrder(String.CASE_INSENSITIVE_ORDER);
        assertNotNull("Assert 0: comparator must not be null", roc);

        assertTrue("Assert 1: comparator must implement Serializable",
                roc instanceof Serializable);

        String[] fixtureDesc = new String[] { "2", "1", "0" };
        String[] numbers = new String[] { "0", "1", "2" };
        Arrays.sort(numbers, roc);
        assertTrue("Assert 2: the arrays are not equal, the sort failed",
                Arrays.equals(fixtureDesc, numbers));

        roc = Collections.reverseOrder(null);
        assertNotNull("Assert 3: comparator must not be null", roc);

        assertTrue("Assert 4: comparator must implement Serializable",
                roc instanceof Serializable);

        numbers = new String[] { "0", "1", "2" };
        Arrays.sort(numbers, roc);
        assertTrue("Assert 5: the arrays are not equal, the sort failed",
                Arrays.equals(fixtureDesc, numbers));
    }

    class Mock_Collection implements Collection {
        public boolean add(Object o) {
            throw new UnsupportedOperationException();
        }

        public boolean addAll(Collection c) {
            return false;
        }

        public void clear() {
        }

        public boolean contains(Object o) {
            return false;
        }

        public boolean containsAll(Collection c) {
            return false;
        }

        public boolean isEmpty() {
            return false;
        }

        public Iterator iterator() {
            return null;
        }

        public boolean remove(Object o) {
            return false;
        }

        public boolean removeAll(Collection c) {
            return false;
        }

        public boolean retainAll(Collection c) {
            return false;
        }

        public int size() {
            return 0;
        }

        public Object[] toArray() {
            return null;
        }

        public Object[] toArray(Object[] a) {
            return null;
        }
    }

    class Mock_WrongCollection implements Collection {
        final String wrongElement = "Wrong element";
        public boolean add(Object o) {
            if (o.equals(wrongElement)) throw new IllegalArgumentException();
            if (o == null) throw new NullPointerException();
            return false;
        }

        public boolean addAll(Collection c) {
            return false;
        }

        public void clear() {
        }

        public boolean contains(Object o) {
            return false;
        }

        public boolean containsAll(Collection c) {
            return false;
        }

        public boolean isEmpty() {
            return false;
        }

        public Iterator iterator() {
            return null;
        }

        public boolean remove(Object o) {
            return false;
        }

        public boolean removeAll(Collection c) {
            return false;
        }

        public boolean retainAll(Collection c) {
            return false;
        }

        public int size() {
            return 0;
        }

        public Object[] toArray() {
            return null;
        }

        public Object[] toArray(Object[] a) {
            return null;
        }
    }
    
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "addAll",
        args = {java.util.Collection.class, java.lang.Object[].class}
    )
    public void test_AddAll() {
        List<Object> l = new ArrayList<Object>();
        assertFalse(Collections.addAll(l, new Object[] {}));
        assertTrue(l.isEmpty());
        assertTrue(Collections.addAll(l, new Object[] { new Integer(1),
                new Integer(2), new Integer(3) }));
        assertFalse(l.isEmpty());
        assertTrue(l.equals(Arrays.asList(new Object[] { new Integer(1),
                new Integer(2), new Integer(3) })));
        
        try {
            Collections.addAll(null,new Object[] { new Integer(1),
                    new Integer(2), new Integer(3) });
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
            //fail
        }
        
        Collection c = new Mock_Collection();
        try {
            Collections.addAll(c, new Object[] { new Integer(1),
                    new Integer(2), new Integer(3) });
            fail("UnsupportedOperationException expected");
        } catch (UnsupportedOperationException e) {
            //expected
        }
        
        c = new Mock_WrongCollection ();
        
        try {
            Collections.addAll(c, new String[] { "String",
                    "Correct element", null });
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
            //fail
        }
        
        try {
            Collections.addAll(c, new String[] { "String",
                    "Wrong element", "Correct element" });
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
            //fail
        }
        
        Collections.addAll(c, new String[] { "String",
                "", "Correct element" });
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "disjoint",
        args = {java.util.Collection.class, java.util.Collection.class}
    )
    public void test_Disjoint() {
        Object[] arr1 = new Object[10];
        for (int i = 0; i < arr1.length; i++) {
            arr1[i] = new Integer(i);
        }
        Object[] arr2 = new Object[20];
        for (int i = 0; i < arr2.length; i++) {
            arr2[i] = new Integer(100 + i);
        }
        Collection<Object> c1 = new ArrayList<Object>();
        Collection<Object> c2 = new ArrayList<Object>();
        Collections.addAll(c1, arr1);
        Collections.addAll(c2, arr2);
        assertTrue(Collections.disjoint(c1, c2));
        c1.add(arr2[10]);
        assertFalse(Collections.disjoint(c1, c2));

        c1 = new LinkedList<Object>();
        c2 = new LinkedList<Object>();
        Collections.addAll(c1, arr1);
        Collections.addAll(c2, arr2);
        assertTrue(Collections.disjoint(c1, c2));
        c1.add(arr2[10]);
        assertFalse(Collections.disjoint(c1, c2));

        c1 = new TreeSet<Object>();
        c2 = new TreeSet<Object>();
        Collections.addAll(c1, arr1);
        Collections.addAll(c2, arr2);
        assertTrue(Collections.disjoint(c1, c2));
        c1.add(arr2[10]);
        assertFalse(Collections.disjoint(c1, c2));

        c1 = new HashSet<Object>();
        c2 = new HashSet<Object>();
        Collections.addAll(c1, arr1);
        Collections.addAll(c2, arr2);
        assertTrue(Collections.disjoint(c1, c2));
        c1.add(arr2[10]);
        assertFalse(Collections.disjoint(c1, c2));

        c1 = new LinkedList<Object>();
        c2 = new TreeSet<Object>();
        Collections.addAll(c1, arr1);
        Collections.addAll(c2, arr2);
        assertTrue(Collections.disjoint(c1, c2));
        c1.add(arr2[10]);
        assertFalse(Collections.disjoint(c1, c2));

        c1 = new Vector<Object>();
        c2 = new HashSet<Object>();
        Collections.addAll(c1, arr1);
        Collections.addAll(c2, arr2);
        assertTrue(Collections.disjoint(c1, c2));
        c1.add(arr2[10]);
        assertFalse(Collections.disjoint(c1, c2));
        
        try {
            Collections.disjoint(c1, null);
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
            //expected
        }
        
        try {
            Collections.disjoint(null, c2);
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
            //expected
        }
    }
    
    /**
     * @tests java.util.Collections.EmptyList#readResolve()
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "!SerializationSelf",
        args = {}
    )
    public void test_EmptyList_readResolve() throws Exception {
        SerializationTest.verifySelf(Collections.EMPTY_LIST, comparator);
    }

    /**
     * @tests java.util.Collections.EmptyMap#readResolve()
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "!SerializationSelf",
        args = {}
    )
    public void test_EmptyMap_readResolve() throws Exception {
        SerializationTest.verifySelf(Collections.EMPTY_MAP, comparator);
    }

    /**
     * @tests java.util.Collections.EmptySet#readResolve()
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "!SerializationSelf",
        args = {}
    )
    public void test_EmptySet_readResolve() throws Exception {
        SerializationTest.verifySelf(Collections.EMPTY_SET, comparator);
    }

    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "!SerializationGolden",
        args = {}
    )
    public void test_checkedCollectionSerializationCompatability() throws Exception {
        Collection<String> c = Collections.emptySet();
        c = Collections.checkedCollection(c, String.class);
        SerializationTester.assertCompabilityEquals(c, "/serialization/org/apache/harmony/luni/tests/java/util/Collections_CheckedCollection.golden.ser");
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "!SerializationGolden",
        args = {}
    )
    public void test_checkedListRandomAccessSerializationCompatability() throws Exception {
        List<String> c = new ArrayList<String>();
        assertTrue(c instanceof RandomAccess);
        c = Collections.checkedList(c, String.class);
        SerializationTester.assertCompabilityEquals(c, "/serialization/org/apache/harmony/luni/tests/java/util/Collections_CheckedListRandomAccess.golden.ser");
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "!SerializationGolden",
        args = {}
    )
    public void test_checkedListSerializationCompatability() throws Exception {
        List<String> c = new LinkedList<String>();
        assertFalse(c instanceof RandomAccess);
        c = Collections.checkedList(c, String.class);
        SerializationTester.assertCompabilityEquals(c, "/serialization/org/apache/harmony/luni/tests/java/util/Collections_CheckedList.golden.ser");
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "!SerializationGolden",
        args = {}
    )
    public void test_checkedSetSerializationCompatability() throws Exception {
        Set<String> c = new HashSet<String>();
        assertFalse(c instanceof SortedSet);
        c = Collections.checkedSet(c, String.class);
        SerializationTester.assertCompabilityEquals(c, "/serialization/org/apache/harmony/luni/tests/java/util/Collections_CheckedSet.golden.ser");
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "!SerializationGolden",
        args = {}
    )
    public void test_checkedMapSerializationCompatability() throws Exception {
        Map<String, String> c = new HashMap<String, String>();
        assertFalse(c instanceof SortedMap);
        c = Collections.checkedMap(c, String.class, String.class);
        SerializationTester.assertCompabilityEquals(c, "/serialization/org/apache/harmony/luni/tests/java/util/Collections_CheckedMap.golden.ser");
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "!SerializationGolden",
        args = {}
    )
    public void test_checkedSortedSetSerializationCompatability() throws Exception {
        SortedSet<String> c = new TreeSet<String>();
        c = Collections.checkedSortedSet(c, String.class);
        SerializationTester.assertCompabilityEquals(c, "/serialization/org/apache/harmony/luni/tests/java/util/Collections_CheckedSortedSet.golden.ser");
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "!SerializationGolden",
        args = {}
    )
    public void test_checkedSortedMapSerializationCompatability() throws Exception {
        SortedMap<String, String> c = new TreeMap<String, String>();
        c = Collections.checkedSortedMap(c, String.class, String.class);
        SerializationTester.assertCompabilityEquals(c, "/serialization/org/apache/harmony/luni/tests/java/util/Collections_CheckedSortedMap.golden.ser");
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "checkedCollection",
        args = {java.util.Collection.class, java.lang.Class.class}
    )
    public void test_checkedCollectionLjava_util_CollectionLjava_lang_Class() {
        ArrayList al = new ArrayList<Integer>();
        
        Collection c = Collections.checkedCollection(al, Integer.class);
        
        c.add(new Integer(1));
        
        try {
            c.add(new Double(3.14));
            fail("ClassCastException expected");
        } catch (ClassCastException e) {
            //expected
        }
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "checkedList",
        args = {java.util.List.class, java.lang.Class.class}
    )
    public void test_checkedListLjava_util_ListLjava_lang_Class() {
        ArrayList al = new ArrayList<Integer>();
        
        List l = Collections.checkedList(al, Integer.class);
        
        l.add(new Integer(1));
        
        try {
            l.add(new Double(3.14));
            fail("ClassCastException expected");
        } catch (ClassCastException e) {
            //expected
        }
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "checkedMap",
        args = {java.util.Map.class, java.lang.Class.class, java.lang.Class.class}
    )
    public void test_checkedMapLjava_util_MapLjava_lang_ClassLjava_lang_Class() {
        HashMap hm = new HashMap<Integer, String>();
        
        Map m = Collections.checkedMap(hm, Integer.class, String.class);
        
        m.put(1, "one");
        m.put(2, "two");
        
        try {
            m.put("wron key", null);
            fail("ClassCastException expected");
        } catch (ClassCastException e) {
            //expected
        }

        try {
            m.put(3, new Double(3.14));
            fail("ClassCastException expected");
        } catch (ClassCastException e) {
            //expected
        }
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "checkedSet",
        args = {java.util.Set.class, java.lang.Class.class}
    )
    public void test_checkedSetLjava_util_SetLjava_lang_Class() {
        HashSet hs = new HashSet<Integer>();
        
        Set s = Collections.checkedSet(hs, Integer.class);
        
        s.add(new Integer(1));
        
        try {
            s.add(new Double(3.14));
            fail("ClassCastException expected");
        } catch (ClassCastException e) {
            //expected
        }
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "checkedSortedMap",
        args = {java.util.SortedMap.class, java.lang.Class.class, java.lang.Class.class}
    )
    public void test_checkedSortedMapLjava_util_SortedMapLjava_lang_ClassLjava_lang_Class() {
        TreeMap tm = new TreeMap<Integer, String>();
        
        SortedMap sm = Collections.checkedSortedMap(tm, Integer.class, String.class);
        
        sm.put(1, "one");
        sm.put(2, "two");
        
        try {
            sm.put("wron key", null);
            fail("ClassCastException expected");
        } catch (ClassCastException e) {
            //expected
        }

        try {
            sm.put(3, new Double(3.14));
            fail("ClassCastException expected");
        } catch (ClassCastException e) {
            //expected
        }
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "checkedSortedSet",
        args = {java.util.SortedSet.class, java.lang.Class.class}
    )
    public void test_checkedSortedSetLjava_util_SortedSetLjava_lang_Class() {
        TreeSet ts = new TreeSet<Integer>();
        
        SortedSet ss = Collections.checkedSortedSet(ts, Integer.class);
        
        ss.add(new Integer(1));
        
        try {
            ss.add(new Double(3.14));
            fail("ClassCastException expected");
        } catch (ClassCastException e) {
            //expected
        }
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "emptyList",
        args = {}
    )
    public void test_emptyList() {
        List<String> ls = Collections.emptyList();
        List<Integer> li = Collections.emptyList();

        assertTrue(ls.equals(li));
        assertTrue(li.equals(Collections.EMPTY_LIST));
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "emptyMap",
        args = {}
    )
    public void test_emptyMap() {
        Map<Integer, String> mis = Collections.emptyMap();
        Map<String, Integer> msi = Collections.emptyMap();
        
        assertTrue(mis.equals(msi));
        assertTrue(msi.equals(Collections.EMPTY_MAP));
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "emptySet",
        args = {}
    )
    public void test_emptySet() {
        Set<String> ss = Collections.emptySet();
        Set<Integer> si = Collections.emptySet();
        
        assertTrue(ss.equals(si));
        assertTrue(si.equals(Collections.EMPTY_SET));
    }
}
