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
* @author Alexander V. Astapchuk
* @version $Revision$
*/

package org.apache.harmony.security.tests.java.security;
import java.net.URL;
import java.net.MalformedURLException;
import java.net.URLClassLoader;
import java.security.AllPermission;
import java.security.CodeSource;
import java.security.Permission;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.security.Policy;
import java.security.Principal;
import java.security.ProtectionDomain;

import junit.framework.TestCase;


/**
 * Unit tests for java.security.ProtectionDomain.
 * 
 */

public class ProtectionDomainTest extends TestCase {

    /**
     * Entry point for standalone runs.
     * @param args command line arguments
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(ProtectionDomainTest.class);
    }

    private final AllPermission allperm = new AllPermission();

    private URL url = null;

    private CodeSource cs = null;

    private PermissionCollection perms = null;

    private ClassLoader classldr = null;

    private Principal[] principals = null; // changed in setUp()

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        try {
            url = new URL("http://localhost");
        } catch (MalformedURLException ex) {
            throw new Error(ex);
        }
        cs = new CodeSource(url, (java.security.cert.Certificate[]) null);
        perms = allperm.newPermissionCollection();
        perms.add(allperm);
        classldr = URLClassLoader.newInstance(new URL[] { url });
        principals = new Principal[] { new TestPrincipal("0"),
                new TestPrincipal("1"), new TestPrincipal("2"),
                new TestPrincipal("3"), new TestPrincipal("4"), };
    }

    /**
     * Class under test for void ProtectionDomain(CodeSource,
     * PermissionCollection)
     */
    public void testProtectionDomainCodeSourcePermissionCollection_00() {
        new ProtectionDomain(null, null);
        new ProtectionDomain(cs, null);

        new ProtectionDomain(cs, perms);
    }

    /**
     * the ctor must set the PermissionCollection read-only
     */
    public void testProtectionDomainCodeSourcePermissionCollection_01() {
        assertFalse(perms.isReadOnly());
        new ProtectionDomain(null, perms);
        assertTrue(perms.isReadOnly());
    }

    /**
     * Test for ProtectionDomain(CodeSource, PermissionCollection, ClassLoader, Principal[])
     */
    public void testProtectionDomainCodeSourcePermissionCollectionClassLoaderPrincipalArray() {
        new ProtectionDomain(null, null, null, null);

        new ProtectionDomain(cs, null, null, null);
        new ProtectionDomain(null, perms, null, null);
        new ProtectionDomain(null, null, classldr, null);
        new ProtectionDomain(null, null, null, principals);

        new ProtectionDomain(cs, perms, classldr, principals);
    }

    /**
     * Tests for ProtectionDomain.getClassLoader()
     */
    public void testGetClassLoader() {
        assertNull(new ProtectionDomain(null, null).getClassLoader());
        assertSame(new ProtectionDomain(null, null, classldr, null)
                .getClassLoader(), classldr);
    }

    /**
     * Tests for ProtectionDomain.getCodeSource()
     */
    public void testGetCodeSource() {
        assertNull(new ProtectionDomain(null, null).getCodeSource());
        assertSame(new ProtectionDomain(cs, null).getCodeSource(), cs);
    }

    /**
     * Tests for ProtectionDomain.getPermissions()
     */
    public void testGetPermissions() {
        assertNull(new ProtectionDomain(null, null).getPermissions());
        assertSame(new ProtectionDomain(null, perms).getPermissions(), perms);
    }

    /**
     * getPrincipals() always returns non null array
     */
    public void testGetPrincipals_00() {
        assertNotNull(new ProtectionDomain(null, null).getPrincipals());
    }

    /**
     * getPrincipals() returns new array each time it's called
     */
    public void testGetPrincipals_01() {
        ProtectionDomain pd = new ProtectionDomain(null, null, null, principals);
        Principal[] got = pd.getPrincipals();
        assertNotNull(got);
        assertNotSame(got, principals);
        assertNotSame(got, pd.getPrincipals());
        assertTrue(got.length == principals.length);
    }

    /**
     * ProtectionDomain with null Permissions must not imply() permissions.
     */
    public void testImplies_00() {
        assertFalse(new ProtectionDomain(null, null).implies(allperm));
    }

    /**
     * ProtectionDomain with PermissionCollection which contains AllPermission
     * must imply() AllPermission.
     */
    public void testImplies_01() {
        assertTrue(new ProtectionDomain(null, perms).implies(allperm));
    }

    /**
     * ProtectionDomain created with a static set of permissions must not query 
     * policy. 
     */
    public void testImplies_02() {
        TestPolicy policy = new TestPolicy();
        // null set of permissions [must] force the PD to use Policy - for 
        // dynamic permissions
        ProtectionDomain pd = new ProtectionDomain(cs, null);
        policy.setTrackPD(pd);
        try {
            Policy.setPolicy(policy);
            pd.implies(allperm);
        } finally {
            Policy.setPolicy(null);
        }
        assertFalse(policy.getPdTracked());
    }

    /**
     * ProtectionDomain created with dynamic set of permissions must query 
     * policy. 
     */
    public void testImplies_03() {
        TestPolicy policy = new TestPolicy();
        ProtectionDomain pd = new ProtectionDomain(cs, null, ClassLoader
                .getSystemClassLoader(), principals);
        policy.setTrackPD(pd);
        try {
            Policy.setPolicy(policy);
            pd.implies(allperm);
        } finally {
            Policy.setPolicy(null);
        }
        assertTrue(policy.getPdTracked());
    }

    /**
     * Simply checks that it's working somehow
     */
    public void testToString() {
        new ProtectionDomain(null, null).toString();
        new ProtectionDomain(cs, perms).toString();
        new ProtectionDomain(null, null, null, null).toString();
        new ProtectionDomain(cs, perms, classldr, principals).toString();
    }

    /**
     * Test principal used during the testing. Does nothing.
     */

    private static class TestPrincipal implements Principal {
        private String name;

        TestPrincipal(String name) {
            this.name = name;
        }

        public String getName() {
            return "TestPrincipal: " + name;
        }
    }

    private static class TestPolicy extends Policy {
        ProtectionDomain trackPD = null;

        boolean pdTracked = false;

        ProtectionDomain setTrackPD(ProtectionDomain pd) {
            ProtectionDomain tmp = trackPD;
            trackPD = pd;
            pdTracked = false;
            return tmp;
        }

        boolean getPdTracked() {
            return pdTracked;
        }

        public PermissionCollection getPermissions(CodeSource cs) {
            return new Permissions();
        }

        //        public PermissionCollection getPermissions(ProtectionDomain domain) {
        //            return super.getPermissions(domain);
        //        }
        public boolean implies(ProtectionDomain domain, Permission permission) {
            if (trackPD != null && trackPD == domain) {
                pdTracked = true;
            }
            return super.implies(domain, permission);
        }

        public void refresh() {
            // do nothing
        }
    }

}
