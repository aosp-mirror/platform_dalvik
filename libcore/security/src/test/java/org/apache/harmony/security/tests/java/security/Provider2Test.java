/* 
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.harmony.security.tests.java.security;

import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestTargets;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetNew;

import java.security.Provider;

@TestTargetClass(Provider.class)
public class Provider2Test extends junit.framework.TestCase {

    class TestProvider extends Provider {
        TestProvider(String name, double version, String info) {
            super(name, version, info);
        }
    }

    class MyEntry implements java.util.Map.Entry {
         public Object getKey() {
             return null;  
         }

         public Object getValue() {
             return null;  
         }

         public Object setValue(Object value) {
             return null;  
         }
    }
    
    TestProvider provTest = new TestProvider("provTest", 1.2,
            "contains nothings, purely for testing the class");

    
    /**
     * @tests java.security.Provider#entrySet()
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "UnsupportedOperationException verification",
        method = "entrySet",
        args = {}
    )
    public void test_entrySet() {
        // test method of java.security.provider.entrySet
        provTest.put("test.prop", "this is a test property");
        try {
            //make it compilable on 1.5
            provTest.entrySet().add(new MyEntry());
            fail("was able to modify the entrySet");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    /**
     * @tests java.security.Provider#getInfo()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getInfo",
        args = {}
    )
    public void test_getInfo() {
        // test method of java.security.provider.getInfo
        assertEquals("the information of the provider is not stored properly",
                "contains nothings, purely for testing the class", provTest
                        .getInfo());
    }

    /**
     * @tests java.security.Provider#getName()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getName",
        args = {}
    )
    public void test_getName() {
        // test method of java.security.provider.getName
        assertEquals("the name of the provider is not stored properly",
                "provTest", provTest.getName());
    }

    /**
     * @tests java.security.Provider#getVersion()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getVersion",
        args = {}
    )
    public void test_getVersion() {
        // test method of java.security.provider.getVersion
        assertEquals("the version of the provider is not stored properly",
                1.2, provTest.getVersion(), 0);
    }

    /**
     * @tests java.security.Provider#keySet()
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "UnsupportedOperationException verification",
        method = "keySet",
        args = {}
    )
    public void test_keySet() {
        // test method of java.security.provider.keySet
        provTest.put("test.prop", "this is a test property");
        try {
            provTest.keySet().add("another property key");
            fail("was able to modify the keySet");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    /**
     * @tests java.security.Provider#values()
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "UnsupportedOperationException verification",
        method = "values",
        args = {}
    )
    public void test_values() {
        // test method of java.security.provider.values
        provTest.put("test.prop", "this is a test property");
        try {
            provTest.values().add("another property value");
            fail("was able to modify the values collection");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }
    
    
    /**
     * @tests java.security.Provider#toString()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Regression test",
        method = "toString",
        args = {}
    )
    public void test_toString() {
        // Regression for HARMONY-3734
        assertEquals("provTest version 1.2", provTest.toString());
    }
}
