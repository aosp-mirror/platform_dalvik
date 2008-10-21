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

import java.io.FilePermission;
import java.security.AccessControlException;

public class AccessControlException2Test extends junit.framework.TestCase {
    FilePermission filePermission;

    AccessControlException acException;

    AccessControlException acException1;

    /**
     * @tests java.security.AccessControlException#AccessControlException(java.lang.String)
     */
    public void test_ConstructorLjava_lang_String() {
        // Test for method
        // java.security.AccessControlException(java.lang.String)
        assertTrue("AccessControlException's toString() should have returned "
                + "'java.security.AccessControlException: test message' but "
                + "returned: " + acException.toString(), acException.toString()
                .equals("java.security.AccessControlException: test message"));
    }

    /**
     * @tests java.security.AccessControlException#AccessControlException(java.lang.String,
     *        java.security.Permission)
     */
    public void test_ConstructorLjava_lang_StringLjava_security_Permission() {
        // Test for method
        // java.security.AccessControlException(java.lang.String,
        // java.security.Permission)
        assertTrue("AccessControlException's toString() should have returned "
                + "'java.security.AccessControlException: test message "
                + "(java.io.FilePermission /* read)' but returned: "
                + acException1.toString(), acException1.toString().equals(
                "java.security.AccessControlException: test message"));
    }

    /**
     * @tests java.security.AccessControlException#getPermission()
     */
    public void test_getPermission() {
        // Test for method java.security.Permission
        // java.security.AccessControlException.getPermission()
        // make sure getPermission returns null when it's not set
        assertNull(
                "getPermission should have returned null if no permission was set",
                acException.getPermission());
        assertTrue(
                "getPermission should have returned the permission we assigned to it",
                acException1.getPermission() == filePermission);
    }

    /**
     * Sets up the fixture, for example, open a network connection. This method
     * is called before a test is executed.
     */
    protected void setUp() {
        filePermission = new FilePermission("/*", "read");
        acException = new AccessControlException("test message");
        acException1 = new AccessControlException("test message",
                filePermission);
    }
}