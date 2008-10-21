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

import java.lang.reflect.ReflectPermission;

public class ReflectPermissionTest extends junit.framework.TestCase {

    /**
     * @tests java.lang.reflect.ReflectPermission#ReflectPermission(java.lang.String)
     */
    public void test_ConstructorLjava_lang_String() {
        // Test for method java.lang.reflect.ReflectPermission(java.lang.String)
        String permString = new ReflectPermission("Blah").toString();
        assertTrue("Incorrect permission constructed", permString.indexOf(
                "java.lang.reflect.ReflectPermission Blah") >= 0);
    }

    /**
     * @tests java.lang.reflect.ReflectPermission#ReflectPermission(java.lang.String,
     *        java.lang.String)
     */
    public void test_ConstructorLjava_lang_StringLjava_lang_String() {
        // Test for method java.lang.reflect.ReflectPermission(java.lang.String,
        // java.lang.String)
        assertTrue("Incorrect permission constructed", new ReflectPermission(
                "Blah", "suppressAccessChecks").toString().indexOf(
                "java.lang.reflect.ReflectPermission Blah") >= 0);
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
