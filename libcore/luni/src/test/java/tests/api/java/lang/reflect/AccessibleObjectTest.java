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

package tests.api.java.lang.reflect;

import dalvik.annotation.TestInfo;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTarget;
import dalvik.annotation.TestTargetClass;

import java.lang.reflect.AccessibleObject;

@TestTargetClass(AccessibleObject.class) 
public class AccessibleObjectTest extends junit.framework.TestCase {

    public class TestClass {
        public Object aField;
    }

    /**
     * @tests java.lang.reflect.AccessibleObject#isAccessible()
     */
    @TestInfo(
      level = TestLevel.COMPLETE,
      purpose = "",
      targets = {
        @TestTarget(
          methodName = "isAccessible",
          methodArgs = {}
        )
    })
    public void test_isAccessible() {
        // Test for method boolean
        // java.lang.reflect.AccessibleObject.isAccessible()
        try {
            AccessibleObject ao = TestClass.class.getField("aField");
            ao.setAccessible(true);
            assertTrue("Returned false to isAccessible", ao.isAccessible());
            ao.setAccessible(false);
            assertTrue("Returned true to isAccessible", !ao.isAccessible());
        } catch (Exception e) {
            fail("Exception during test : " + e.getMessage());
        }
    }

    /**
     * @tests java.lang.reflect.AccessibleObject#setAccessible(java.lang.reflect.AccessibleObject[],
     *        boolean)
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "Doesn't verify SecurityException.",
      targets = {
        @TestTarget(
          methodName = "setAccessible",
          methodArgs = {java.lang.reflect.AccessibleObject[].class, boolean.class}
        )
    })
    public void test_setAccessible$Ljava_lang_reflect_AccessibleObjectZ() {
        // Test for method void
        // java.lang.reflect.AccessibleObject.setAccessible(java.lang.reflect.AccessibleObject
        // [], boolean)
        try {
            AccessibleObject ao = TestClass.class.getField("aField");
            AccessibleObject[] aoa = new AccessibleObject[] { ao };
            AccessibleObject.setAccessible(aoa, true);
            assertTrue("Returned false to isAccessible", ao.isAccessible());
            AccessibleObject.setAccessible(aoa, false);
            assertTrue("Returned true to isAccessible", !ao.isAccessible());
        } catch (Exception e) {
            fail("Exception during test : " + e.getMessage());
        }
    }

    /**
     * @tests java.lang.reflect.AccessibleObject#setAccessible(boolean)
     */
    @TestInfo(
      level = TestLevel.TODO,
      purpose = "Empty test, setAccessible(boolean) method is not " +
            "verified.",
      targets = {
        @TestTarget(
          methodName = "setAccessible",
          methodArgs = {boolean.class}
        )
    })
    public void test_setAccessibleZ() {
        // Test for method void
        // java.lang.reflect.AccessibleObject.setAccessible(boolean)
        assertTrue("Used to test", true);
    }

    /**
     * Sets up the fixture, for example, open a network connection. This method
     * is called before a test is executed.
     */
    protected void setUp() {
    }

    /**
     * Tears down the fixture, for example, close a network connection. This
     * method is called after a test is executed.
     */
    protected void tearDown() {
    }
}
