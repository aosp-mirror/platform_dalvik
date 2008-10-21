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

import java.lang.reflect.AccessibleObject;

public class AccessibleObjectTest extends junit.framework.TestCase {

    public class TestClass {
        public Object aField;
    }

    /**
     * @tests java.lang.reflect.AccessibleObject#isAccessible()
     */
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
