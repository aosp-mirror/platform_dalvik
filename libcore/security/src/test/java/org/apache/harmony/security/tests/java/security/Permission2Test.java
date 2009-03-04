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

import java.security.Permission;
import java.security.PermissionCollection;
import java.security.SecurityPermission;

@TestTargetClass(Permission.class)
public class Permission2Test extends junit.framework.TestCase {
    static class ConcretePermission extends Permission {
        public ConcretePermission() {
            super("noname");
        }

        public boolean equals(Object obj) {
            return true;
        }

        public String getActions() {
            return "none";
        }

        public int hashCode() {
            return 1;
        }

        public boolean implies(Permission p) {
            return true;
        }
    }

    /**
     * @tests java.security.Permission#Permission(java.lang.String)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "Permission",
        args = {java.lang.String.class}
    )
    public void test_ConstructorLjava_lang_String() {
        // test method java.security.permission.Permission(string)
        try {
        SecurityPermission permi = new SecurityPermission(
                "Testing the permission abstract class");
        String name = permi.getName();
        assertEquals("Permission Constructor failed",
                "Testing the permission abstract class", name);
        } catch (Exception e) {
            fail("Unexpected excpetion");
        }
        
        try {
            SecurityPermission permi = new SecurityPermission(null);
            fail("NullPointerException was not thrown for NULL parameter");
        } catch (NullPointerException e) {
            //expected
        }
        
        try {
            SecurityPermission permi = new SecurityPermission("");
            fail("IllegalArgumentException was not thrown for empty parameter");
        } catch (IllegalArgumentException e) {
            //expected
        }
    }

    /**
     * @tests java.security.Permission#checkGuard(java.lang.Object)
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "checkGuard",
        args = {java.lang.Object.class}
    )
    public void test_checkGuardLjava_lang_Object() {
        // test method java.security.permission.checkGuard(object)
        SecurityPermission permi = new SecurityPermission(
                "Testing the permission abstract class");
        String name = permi.getName();
        try {
            permi.checkGuard(name);
        } catch (SecurityException e) {
            fail("security not granted when it is suppose to be : " + e);
        }
    }

    /**
     * @tests java.security.Permission#getName()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getName",
        args = {}
    )
    public void test_getName() {
        // test method java.security.permission.getName()
        SecurityPermission permi = new SecurityPermission("testing getName()");
        String name = permi.getName();
        assertEquals("getName failed to obtain the correct name",
                "testing getName()", name);

        SecurityPermission permi2 = new SecurityPermission("93048Helloworld");
        assertEquals("getName failed to obtain correct name",
                "93048Helloworld", permi2.getName());
    }

    /**
     * @tests java.security.Permission#newPermissionCollection()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "newPermissionCollection",
        args = {}
    )
    public void test_newPermissionCollection() {
        // test method java.security.permission.newPermissionCollection
        Permission permi = new ConcretePermission();
        PermissionCollection permiCollect = permi.newPermissionCollection();
        assertNull("newPermissionCollector of the abstract class "
                + "permission did not return a null instance "
                + "of permissionCollection", permiCollect);
    }

    /**
     * @tests java.security.Permission#toString()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "toString",
        args = {}
    )
    public void test_toString() {
        // test method java.security.permission.toString
        // test for permission with no action
        SecurityPermission permi = new SecurityPermission("testing toString");
        String toString = permi.toString();
        assertNotNull("toString should have returned a string of elements",
                toString);
    }
}