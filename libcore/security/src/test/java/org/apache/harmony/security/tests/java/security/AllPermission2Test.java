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

import java.security.AllPermission;
import java.security.Permission;
import java.security.PermissionCollection;
import java.security.SecurityPermission;
import java.util.Enumeration;

public class AllPermission2Test extends junit.framework.TestCase {

    /**
     * @tests java.security.AllPermission#AllPermission()
     */
    public void test_Constructor() {
        // Test for method java.security.AllPermission()
        AllPermission ap = new AllPermission();
        assertEquals("Bogus name for AllPermission \"" + ap.getName() + "\".",
                "<all permissions>", ap.getName());
    }

    /**
     * @tests java.security.AllPermission#AllPermission(java.lang.String,
     *        java.lang.String)
     */
    public void test_ConstructorLjava_lang_StringLjava_lang_String() {
        // Test for method java.security.AllPermission(java.lang.String,
        // java.lang.String)
        AllPermission ap = new AllPermission("Don't remember this stupid name",
                "or this action");
        assertEquals("Bogus name for AllPermission \"" + ap.getName() + "\".",
                "<all permissions>", ap.getName());
        assertEquals(
                "AllPermission constructed with actions didn't ignore them.",
                "<all actions>", ap.getActions());
    }

    /**
     * @tests java.security.AllPermission#equals(java.lang.Object)
     */
    public void test_equalsLjava_lang_Object() {
        // Test for method boolean
        // java.security.AllPermission.equals(java.lang.Object)
        assertTrue("Two AllPermissions not equal to each other.",
                new AllPermission().equals(new AllPermission()));
        assertTrue("AllPermission equals a SecurityPermission.",
                !(new AllPermission().equals(new SecurityPermission("ugh!"))));
    }

    /**
     * @tests java.security.AllPermission#getActions()
     */
    public void test_getActions() {
        AllPermission ap = new AllPermission();
        // Test for method java.lang.String
        // java.security.AllPermission.getActions()
        assertTrue("AllPermission has non-empty actions. (" + ap.getActions()
                + ")", ap.getActions().equals("<all actions>"));
    }

    /**
     * @tests java.security.AllPermission#hashCode()
     */
    public void test_hashCode() {
        final int ALLPERMISSION_HASH = 1;
        // Test for method int java.security.AllPermission.hashCode()
        AllPermission TestAllPermission = new AllPermission();
        assertTrue("AllPermission hashCode is wrong. Should have been "
                + ALLPERMISSION_HASH + " but was "
                + TestAllPermission.hashCode(),
                TestAllPermission.hashCode() == ALLPERMISSION_HASH);
    }

    /**
     * @tests java.security.AllPermission#implies(java.security.Permission)
     */
    public void test_impliesLjava_security_Permission() {
        // Test for method boolean
        // java.security.AllPermission.implies(java.security.Permission)
        assertTrue("AllPermission does not imply a AllPermission.",
                new AllPermission().implies(new AllPermission()));
        assertTrue("AllPermission does not imply a SecurityPermission.",
                new AllPermission().implies(new SecurityPermission("ugh!")));
        assertTrue("SecurityPermission implies AllPermission.",
                !(new SecurityPermission("ugh!").implies(new AllPermission())));
    }
    
    /**
     * @tests java.security.AllPermission#newPermissionCollection()
     */
    public void test_newPermissionCollection() {
        AllPermission ap1 = new AllPermission();
        AllPermission ap2 = new AllPermission("Don't remember this stupid name",
        "or this action");
        AllPermission ap3 = new AllPermission("Remember this cool name",
        "and this action");
        
        PermissionCollection pc1 = ap1.newPermissionCollection();
        assertFalse(pc1.isReadOnly());
        
        Enumeration<Permission> perm1 = pc1.elements();
        assertFalse(perm1.hasMoreElements());
        assertNotNull(perm1);
        
        pc1.add(ap1);
        pc1.add(ap2);
        assertTrue("Should imply", pc1.implies(ap1));
        assertTrue("Should imply", pc1.implies(ap2));
        assertTrue("Should imply", pc1.implies(ap3));
        perm1 = pc1.elements();
        assertTrue(perm1.hasMoreElements());
        
        PermissionCollection pc2 = ap2.newPermissionCollection();
        assertFalse(pc2.isReadOnly());
        
        Enumeration<Permission> perm2 = pc2.elements();
        assertFalse(perm2.hasMoreElements());
        assertNotNull(perm2);
        
        pc2.add(ap1);
        pc2.add(ap2);
        assertTrue("Should imply", pc2.implies(ap1));
        assertTrue("Should imply", pc2.implies(ap2));
        assertTrue("Should imply", pc2.implies(ap3));
        perm2 = pc2.elements();
        assertTrue(perm2.hasMoreElements());
    }

}