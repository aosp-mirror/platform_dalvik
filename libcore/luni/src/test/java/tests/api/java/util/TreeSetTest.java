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

package tests.api.java.util;

import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargets;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetClass; 

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

@TestTargetClass(TreeSet.class) 
public class TreeSetTest extends junit.framework.TestCase {

    public static class ReversedIntegerComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            return -(((Integer) o1).compareTo((Integer) o2));
        }

        public boolean equals(Object o1, Object o2) {
            return ((Integer) o1).compareTo((Integer) o2) == 0;
        }
    }

    TreeSet ts;

    Object objArray[] = new Object[1000];

    /**
     * @tests java.util.TreeSet#TreeSet()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "TreeSet",
        args = {}
    )
    public void test_Constructor() {
        // Test for method java.util.TreeSet()
        assertTrue("Did not construct correct TreeSet", new TreeSet().isEmpty());
    }

    /**
     * @tests java.util.TreeSet#TreeSet(java.util.Collection)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "TreeSet",
        args = {java.util.Collection.class}
    )
    public void test_ConstructorLjava_util_Collection() {
        // Test for method java.util.TreeSet(java.util.Collection)
        TreeSet myTreeSet = new TreeSet(Arrays.asList(objArray));
        assertTrue("TreeSet incorrect size",
                myTreeSet.size() == objArray.length);
        for (int counter = 0; counter < objArray.length; counter++)
            assertTrue("TreeSet does not contain correct elements", myTreeSet
                    .contains(objArray[counter]));
        
        HashMap hm = new HashMap();
        hm.put("First", new Integer(1));
        hm.put(new Integer(2), "two");
        
        try {
            new TreeSet(hm.values());
            fail("ClassCastException expected");
        } catch (ClassCastException e) {
            //expected
        }
        
        try {   
            new TreeSet((Collection)null);
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
            //expected
        }
    }

    /**
     * @tests java.util.TreeSet#TreeSet(java.util.Comparator)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "TreeSet",
        args = {java.util.Comparator.class}
    )
    public void test_ConstructorLjava_util_Comparator() {
        // Test for method java.util.TreeSet(java.util.Comparator)
        TreeSet myTreeSet = new TreeSet(new ReversedIntegerComparator());
        assertTrue("Did not construct correct TreeSet", myTreeSet.isEmpty());
        myTreeSet.add(new Integer(1));
        myTreeSet.add(new Integer(2));
        assertTrue(
                "Answered incorrect first element--did not use custom comparator ",
                myTreeSet.first().equals(new Integer(2)));
        assertTrue(
                "Answered incorrect last element--did not use custom comparator ",
                myTreeSet.last().equals(new Integer(1)));
    }

    /**
     * @tests java.util.TreeSet#TreeSet(java.util.SortedSet)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "TreeSet",
        args = {java.util.SortedSet.class}
    )
    public void test_ConstructorLjava_util_SortedSet() {
        // Test for method java.util.TreeSet(java.util.SortedSet)
        ReversedIntegerComparator comp = new ReversedIntegerComparator();
        TreeSet myTreeSet = new TreeSet(comp);
        for (int i = 0; i < objArray.length; i++)
            myTreeSet.add(objArray[i]);
        TreeSet anotherTreeSet = new TreeSet(myTreeSet);
        assertTrue("TreeSet is not correct size",
                anotherTreeSet.size() == objArray.length);
        for (int counter = 0; counter < objArray.length; counter++)
            assertTrue("TreeSet does not contain correct elements",
                    anotherTreeSet.contains(objArray[counter]));
        assertTrue("TreeSet does not answer correct comparator", anotherTreeSet
                .comparator() == comp);
        assertTrue("TreeSet does not use comparator",
                anotherTreeSet.first() == objArray[objArray.length - 1]);

        try {   
            new TreeSet((SortedSet)null);
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
            //expected
        }
    }

    /**
     * @tests java.util.TreeSet#add(java.lang.Object)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "add",
        args = {java.lang.Object.class}
    )
    public void test_addLjava_lang_Object() {
        // Test for method boolean java.util.TreeSet.add(java.lang.Object)
        ts.add(new Integer(-8));
        assertTrue("Failed to add Object", ts.contains(new Integer(-8)));
        ts.add(objArray[0]);
        assertTrue("Added existing element", ts.size() == objArray.length + 1);

        HashMap hm = new HashMap();
        hm.put("First", new Integer(1));
        hm.put(new Integer(2), "two");
        
        try {
            ts.add("Wrong element");
            fail("ClassCastException expected");
        } catch (ClassCastException e) {
            //expected
        }
    }

    /**
     * @tests java.util.TreeSet#addAll(java.util.Collection)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "addAll",
        args = {java.util.Collection.class}
    )
    public void test_addAllLjava_util_Collection() {
        // Test for method boolean
        // java.util.TreeSet.addAll(java.util.Collection)
        TreeSet s = new TreeSet();
        s.addAll(ts);
        assertTrue("Incorrect size after add", s.size() == ts.size());
        Iterator i = ts.iterator();
        while (i.hasNext())
            assertTrue("Returned incorrect set", s.contains(i.next()));

        HashMap hm = new HashMap();
        hm.put("First", new Integer(1));
        hm.put(new Integer(2), "two");
        
        try {
            s.addAll(hm.values());
            fail("ClassCastException expected");
        } catch (ClassCastException e) {
            //expected
        }
        
        try {
            s.addAll(null);
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
            //expected
        }
    }

    /**
     * @tests java.util.TreeSet#clear()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "clear",
        args = {}
    )
    public void test_clear() {
        // Test for method void java.util.TreeSet.clear()
        ts.clear();
        assertEquals("Returned non-zero size after clear", 0, ts.size());
        assertTrue("Found element in cleared set", !ts.contains(objArray[0]));
    }

    /**
     * @tests java.util.TreeSet#clone()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "clone",
        args = {}
    )
    public void test_clone() {
        // Test for method java.lang.Object java.util.TreeSet.clone()
        TreeSet s = (TreeSet) ts.clone();
        Iterator i = ts.iterator();
        while (i.hasNext())
            assertTrue("Clone failed to copy all elements", s
                    .contains(i.next()));
    }

    /**
     * @tests java.util.TreeSet#comparator()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "comparator",
        args = {}
    )
    public void test_comparator() {
        // Test for method java.util.Comparator java.util.TreeSet.comparator()
        ReversedIntegerComparator comp = new ReversedIntegerComparator();
        TreeSet myTreeSet = new TreeSet(comp);
        assertTrue("Answered incorrect comparator",
                myTreeSet.comparator() == comp);
    }

    /**
     * @tests java.util.TreeSet#contains(java.lang.Object)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "contains",
        args = {java.lang.Object.class}
    )
    public void test_containsLjava_lang_Object() {
        // Test for method boolean java.util.TreeSet.contains(java.lang.Object)
        assertTrue("Returned false for valid Object", ts
                .contains(objArray[objArray.length / 2]));
        assertTrue("Returned true for invalid Object", !ts
                .contains(new Integer(-9)));
        try {
            ts.contains(new Object());
        } catch (ClassCastException e) {
            // Correct
            return;
        }
        fail("Failed to throw exception when passed invalid element");

    }

    /**
     * @tests java.util.TreeSet#first()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "first",
        args = {}
    )
    public void test_first() {
        // Test for method java.lang.Object java.util.TreeSet.first()
        assertTrue("Returned incorrect first element",
                ts.first() == objArray[0]);
        
        ts = new TreeSet();
        try {
            ts.first();
            fail("NoSuchElementException expected");
        } catch (NoSuchElementException e) {
            //expected
        }
    }

    /**
     * @tests java.util.TreeSet#headSet(java.lang.Object)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "headSet",
        args = {java.lang.Object.class}
    )
    public void test_headSetLjava_lang_Object() {
        // Test for method java.util.SortedSet
        // java.util.TreeSet.headSet(java.lang.Object)
        Set s = ts.headSet(new Integer(100));
        assertEquals("Returned set of incorrect size", 100, s.size());
        for (int i = 0; i < 100; i++)
            assertTrue("Returned incorrect set", s.contains(objArray[i]));

        SortedSet sort = ts.headSet(new Integer(100));
        try {
            sort.headSet(new Integer(101));
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
            //expected
        }
        
        try {
            ts.headSet(this);
            fail("ClassCastException expected");
        } catch (ClassCastException e) {
            //expected
        }
        
        try {
            ts.headSet(null);
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
            //expected
        }
    }

    /**
     * @tests java.util.TreeSet#isEmpty()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "isEmpty",
        args = {}
    )
    public void test_isEmpty() {
        // Test for method boolean java.util.TreeSet.isEmpty()
        assertTrue("Empty set returned false", new TreeSet().isEmpty());
        assertTrue("Non-Empty returned true", !ts.isEmpty());
    }

    /**
     * @tests java.util.TreeSet#iterator()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "iterator",
        args = {}
    )
    public void test_iterator() {
        // Test for method java.util.Iterator java.util.TreeSet.iterator()
        TreeSet s = new TreeSet();
        s.addAll(ts);
        Iterator i = ts.iterator();
        Set as = new HashSet(Arrays.asList(objArray));
        while (i.hasNext())
            as.remove(i.next());
        assertEquals("Returned incorrect iterator", 0, as.size());

    }

    /**
     * @tests java.util.TreeSet#last()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "last",
        args = {}
    )
    public void test_last() {
        // Test for method java.lang.Object java.util.TreeSet.last()
        assertTrue("Returned incorrect last element",
                ts.last() == objArray[objArray.length - 1]);
        
        ts = new TreeSet();
        try {
            ts.last();
            fail("NoSuchElementException expected");
        } catch (NoSuchElementException e) {
            //expected
        }
    }

    /**
     * @tests java.util.TreeSet#remove(java.lang.Object)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "remove",
        args = {java.lang.Object.class}
    )
    public void test_removeLjava_lang_Object() {
        // Test for method boolean java.util.TreeSet.remove(java.lang.Object)
        ts.remove(objArray[0]);
        assertTrue("Failed to remove object", !ts.contains(objArray[0]));
        assertTrue("Failed to change size after remove",
                ts.size() == objArray.length - 1);
        try {
            ts.remove(new Object());
        } catch (ClassCastException e) {
            // Correct
            return;
        }
        fail("Failed to throw exception when past uncomparable value");
    }

    /**
     * @tests java.util.TreeSet#size()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "size",
        args = {}
    )
    public void test_size() {
        // Test for method int java.util.TreeSet.size()
        assertTrue("Returned incorrect size", ts.size() == objArray.length);
    }

    /**
     * @tests java.util.TreeSet#subSet(java.lang.Object, java.lang.Object)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "subSet",
        args = {java.lang.Object.class, java.lang.Object.class}
    )
    public void test_subSetLjava_lang_ObjectLjava_lang_Object() {
        // Test for method java.util.SortedSet
        // java.util.TreeSet.subSet(java.lang.Object, java.lang.Object)
        final int startPos = objArray.length / 4;
        final int endPos = 3 * objArray.length / 4;
        SortedSet aSubSet = ts.subSet(objArray[startPos], objArray[endPos]);
        assertTrue("Subset has wrong number of elements",
                aSubSet.size() == (endPos - startPos));
        for (int counter = startPos; counter < endPos; counter++)
            assertTrue("Subset does not contain all the elements it should",
                    aSubSet.contains(objArray[counter]));

        try {
            ts.subSet(objArray[3], objArray[0]);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
            //expected
        }
        
        try {
            ts.subSet(null, objArray[3]);
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
            //expected
        }
        
        try {
            ts.subSet(objArray[3], null);
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
            //expected
        }
        
        try {
            ts.subSet(objArray[3], this);
            fail("ClassCastException expected");
        } catch (ClassCastException e) {
            //expected
        }
    }

    /**
     * @tests java.util.TreeSet#tailSet(java.lang.Object)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "tailSet",
        args = {java.lang.Object.class}
    )
    public void test_tailSetLjava_lang_Object() {
        // Test for method java.util.SortedSet
        // java.util.TreeSet.tailSet(java.lang.Object)
        Set s = ts.tailSet(new Integer(900));
        assertEquals("Returned set of incorrect size", 100, s.size());
        for (int i = 900; i < objArray.length; i++)
            assertTrue("Returned incorrect set", s.contains(objArray[i]));

        SortedSet sort = ts.tailSet(new Integer(101));
        
        try {
            sort.tailSet(new Integer(100));
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
            //expected
        }
        
        try {
            ts.tailSet(this);
            fail("ClassCastException expected");
        } catch (ClassCastException e) {
            //expected
        }
        
        try {
            ts.tailSet(null);
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
            //expected
        }
    }

    /**
     * Tests equals() method.
     * Tests that no ClassCastException will be thrown in all cases.
     * Regression test for HARMONY-1639.
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "equals",
        args = {java.lang.Object.class}
    )
    public void test_equals() throws Exception {
        // comparing TreeSets with different object types
        Set s1 = new TreeSet();
        Set s2 = new TreeSet();
        s1.add("key1");
        s1.add("key2");
        s2.add(new Integer(1));
        s2.add(new Integer(2));
        assertFalse("Sets should not be equal 1", s1.equals(s2));
        assertFalse("Sets should not be equal 2", s2.equals(s1));

        // comparing TreeSet with HashSet
        s1 = new TreeSet();
        s2 = new HashSet();
        s1.add("key");
        s2.add(new Object());
        assertFalse("Sets should not be equal 3", s1.equals(s2));
        assertFalse("Sets should not be equal 4", s2.equals(s1));

        // comparing TreeSets with not-comparable objects inside
        s1 = new TreeSet();
        s2 = new TreeSet();
        s1.add(new Object());
        s2.add(new Object());
        assertFalse("Sets should not be equal 5", s1.equals(s2));
        assertFalse("Sets should not be equal 6", s2.equals(s1));
    }

    /**
     * Sets up the fixture, for example, open a network connection. This method
     * is called before a test is executed.
     */
    protected void setUp() {
        ts = new TreeSet();
        for (int i = 0; i < objArray.length; i++) {
            Object x = objArray[i] = new Integer(i);
            ts.add(x);
        }
    }

    /**
     * Tears down the fixture, for example, close a network connection. This
     * method is called after a test is executed.
     */
    protected void tearDown() {
    }
}
