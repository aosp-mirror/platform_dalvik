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

/**
* @author Alexey V. Varlamov
* @version $Revision$
*/

package org.apache.harmony.security.tests.java.security;
import java.security.Permission;

import junit.framework.TestCase;

/**
 * Tests for <code>Permission</code>
 */

public class PermissionTest extends TestCase {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(PermissionTest.class);
    }

    /**
     * Constructor for PermissionTest.
     * 
     * @param arg0
     */
    public PermissionTest(String arg0) {
        super(arg0);
    }

    // Bare extension to instantiate abstract Permission class
    static final class RealPermission extends Permission {

        public RealPermission(String name) {
            super(name);
        }

        public boolean equals(Object obj) {
            return false;
        }

        public String getActions() {
            return null;
        }

        public int hashCode() {
            return 0;
        }

        public boolean implies(Permission permission) {
            return false;
        }
    }

    /**
     * Test that a permission object is created with the specified name and is
     * properly converted to String
     */
    public void testCtor() {
        String name = "testCtor123^%$#&^ &^$";
        Permission test = new RealPermission(name);
        assertEquals(name, test.getName());
        assertEquals("(" + test.getClass().getName() + " " + name + ")", test
            .toString());
    }

    /**
     * Test checkGuard() realization: if SecurityManager is installed, it's
     * checkPermission() should be called with this permission, otherwise
     * nothing happens
     */
    public void testCheckGuard() {
        final Permission test = new RealPermission("234234");
        SecurityManager old = System.getSecurityManager();
        try {
            System.setSecurityManager(null);
            test.checkGuard(this);
            final boolean[] callFlag = new boolean[] { false };
            System.setSecurityManager(new SecurityManager() {

                public void checkPermission(Permission p) {
                    if (p == test) {
                        callFlag[0] = true;
                    }
                }
            });
            test.checkGuard(null);
            assertTrue(callFlag[0]);
        } finally {
            System.setSecurityManager(old);
        }
    }

    /** newPermissionCollection() should return null */
    public void testCollection() {
        assertNull(new RealPermission("123").newPermissionCollection());
    }
}
