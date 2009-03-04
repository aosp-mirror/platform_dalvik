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

package org.apache.harmony.luni.tests.java.lang;

import dalvik.annotation.TestTargets;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargetClass;

import junit.framework.TestCase;

@TestTargetClass(Character.Subset.class) 
public class Character_SubsetTest extends TestCase {

    /**
     * @tests java.lang.Character.Subset#Character.Subset(java.lang.String)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "Subset",
        args = {java.lang.String.class}
    )
    public void test_Ctor() {

        try {
            // Regression for HARMONY-888
            new Character.Subset(null) {
            };
            fail("No expected NullPointerException");
        } catch (NullPointerException e) {
        }
    }

    /**
     * @tests java.lang.Character.Subset#toString()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "toString",
        args = {}
    )
    public void test_toString() {

        String name = "name";
        Character.Subset subset = new Character.Subset(name) {
        };
        assertSame(name, subset.toString());
    }

    
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "equals",
        args = {java.lang.Object.class}
    )
    public void test_equals() {
      Character.Subset subset1 = new Character.Subset("name") { };
      assertTrue(subset1.equals(subset1));
      assertFalse(subset1.equals(new Character.Subset("name") {}));      
      assertFalse(subset1.equals(new Character.Subset("name1") {}));
      assertFalse(subset1.equals(new Integer(0)));     
    }
    
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "hashCode",
        args = {}
    )
    public void test_hashCode() {
      Character.Subset subset1 = new Character.Subset("name") {};
      Character.Subset subset2 = new Character.Subset("name") {};
      Character.Subset subset3 = new Character.Subset("name1") {};
      assertFalse(subset1.hashCode() == subset2.hashCode());      
      assertFalse(subset1.hashCode() == subset3.hashCode());
    }
    
    public static void main(String[] args) {
        junit.textui.TestRunner.run(Character_SubsetTest.class);
    }
}
