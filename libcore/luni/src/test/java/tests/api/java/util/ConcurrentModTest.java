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

import dalvik.annotation.TestTarget;
import dalvik.annotation.TestInfo;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetClass; 

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Vector;

import junit.framework.TestCase;

@TestTargetClass(AbstractList.class) 
public class ConcurrentModTest extends TestCase {

    /*
     * Test method for 'java.util.AbstractList.subList(int, int)'
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "Doesn't verify IndexOutOfBoundsException.",
      targets = {
        @TestTarget(
          methodName = "get",
          methodArgs = {int.class}
        )
    })
    public void testGet() {
        AbstractList al = new ArrayList();
        Double one = new Double(1.0);
        Double two = new Double(2.0);
        Double three = new Double(3.0);
        Double four = new Double(4.0);
        al.add(one);
        al.add(two);
        al.add(three);
        al.add(four);
        List sub = al.subList(1, 3);
        assertEquals(2, sub.size());
        // the sub.get(1) is 3.0
        assertTrue(((Double) sub.get(1)).doubleValue() <= 3.0);
        assertTrue(((Double) sub.get(1)).doubleValue() > 2.0);

        al.remove(1); // remove the 2.0

        try {
            // illegal call the subList's method get(int).
            sub.get(1);
            fail("It should throws ConcurrentModificationException.");
        } catch (ConcurrentModificationException e) {
            return;
        }
    }

    /*
     * Test method for 'java.util.AbstractList.subList(int, int)'
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "Doesn't verify UnsupportedOperationException, " + 
            "ClassCastException, IllegalArgumentException, " + 
            "IndexOutOfBoundsException.",
      targets = {
        @TestTarget(
          methodName = "set",
          methodArgs = {int.class, Object.class}
        )
    })
    public void testSet() {
        AbstractList al = new ArrayList();
        Double one = new Double(1.0);
        Double two = new Double(2.0);
        Double three = new Double(3.0);
        Double four = new Double(4.0);
        al.add(one);
        al.add(two);
        al.add(three);
        al.add(four);
        List sub = al.subList(1, 3);
        assertEquals(2, sub.size());
        // the sub.get(1) is 3.0
        assertTrue(((Double) sub.get(1)).doubleValue() <= 3.0);
        assertTrue(((Double) sub.get(1)).doubleValue() > 2.0);

        al.remove(1); // remove the 2.0

        try {
            // illegal call the subList's method set(int,Object).
            sub.set(1, two);
            fail("It should throws ConcurrentModificationException.");
        } catch (ConcurrentModificationException e) {
            return;
        }
    }

    /*
     * Test method for 'java.util.AbstractList.subList(int, int)'
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "Doesn't verify UnsupportedOperationException, " + 
            "ClassCastException, " + 
            "IllegalArgumentException, " + 
            "IndexOutOfBoundsException.",
      targets = {
        @TestTarget(
          methodName = "add",
          methodArgs = {int.class, Object.class}
        )
    })
    public void testAdd() {
        AbstractList al = new ArrayList();
        Double one = new Double(1.0);
        Double two = new Double(2.0);
        Double three = new Double(3.0);
        Double four = new Double(4.0);
        al.add(one);
        al.add(two);
        al.add(three);
        al.add(four);
        List sub = al.subList(1, 3);
        assertEquals(2, sub.size());
        // the sub.get(1) is 3.0
        assertTrue(((Double) sub.get(1)).doubleValue() <= 3.0);
        assertTrue(((Double) sub.get(1)).doubleValue() > 2.0);

        al.remove(1); // remove the 2.0

        try {
            // illegal call the subList's method Add(int,Object).
            sub.add(1, two);
            fail("It should throws ConcurrentModificationException.");
        } catch (ConcurrentModificationException e) {
            return;
        }
    }

    /*
     * Test method for 'java.util.AbstractList.subList(int, int)'
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "Doesn't verify UnsupportedOperationException, " + 
            "IndexOutOfBoundsException.",
      targets = {
        @TestTarget(
          methodName = "remove",
          methodArgs = {int.class}
        )
    })
    public void testRemove() {
        AbstractList al = new ArrayList();
        Double one = new Double(1.0);
        Double two = new Double(2.0);
        Double three = new Double(3.0);
        Double four = new Double(4.0);
        al.add(one);
        al.add(two);
        al.add(three);
        al.add(four);
        List sub = al.subList(1, 3);
        assertEquals(2, sub.size());
        // the sub.get(1) is 3.0
        assertTrue(((Double) sub.get(1)).doubleValue() <= 3.0);
        assertTrue(((Double) sub.get(1)).doubleValue() > 2.0);

        al.remove(1); // remove the 2.0

        try {
            // illegal call the subList's method remove(int).
            sub.remove(1);
            fail("It should throws ConcurrentModificationException.");
        } catch (ConcurrentModificationException e) {
            return;
        }
    }

    /*
     * Test method for 'java.util.AbstractList.subList(int, int)'
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "Doesn't verify UnsupportedOperationException, " + 
            "ClassCastException, IllegalArgumentException, " + 
            "IndexOutOfBoundsException, NullPointerException.",
      targets = {
        @TestTarget(
          methodName = "addAll",
          methodArgs = {int.class, java.util.Collection.class}
        )
    })
    public void testAddAll() {
        AbstractList al = new ArrayList();
        Double one = new Double(1.0);
        Double two = new Double(2.0);
        Double three = new Double(3.0);
        Double four = new Double(4.0);
        al.add(one);
        al.add(two);
        al.add(three);
        al.add(four);
        List sub = al.subList(1, 3);
        assertEquals(2, sub.size());
        // the sub.get(1) is 3.0
        assertTrue(((Double) sub.get(1)).doubleValue() <= 3.0);
        assertTrue(((Double) sub.get(1)).doubleValue() > 2.0);

        al.remove(1); // remove the 2.0

        try {
            // illegal call the subList's method addAll(int,Collection).
            Collection c = new Vector();
            Double five = new Double(5.0);
            c.add(five);
            sub.addAll(1, c);
            fail("It should throws ConcurrentModificationException.");
        } catch (ConcurrentModificationException e) {
            return;
        }
    }
}
