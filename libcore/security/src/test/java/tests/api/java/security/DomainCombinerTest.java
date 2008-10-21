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

package tests.api.java.security;

import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.AllPermission;
import java.security.BasicPermission;
import java.security.CodeSource;
import java.security.DomainCombiner;
import java.security.Permission;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.security.PrivilegedAction;
import java.security.ProtectionDomain;
import java.security.SecurityPermission;
import java.security.cert.Certificate;

public class DomainCombinerTest extends junit.framework.TestCase {

    /**
     * @tests java.security.DomainCombiner#combine(java.security.ProtectionDomain[],
     *        java.security.ProtectionDomain[])
     */
    public void test_combine$Ljava_security_ProtectionDomain$Ljava_security_ProtectionDomain() {
        final boolean[] calledDomainCombiner = new boolean[] { false, false };

        class MyCombiner implements DomainCombiner {
            int i;

            MyCombiner(int i) {
                this.i = i;
            }

            public ProtectionDomain[] combine(
                    ProtectionDomain[] executionDomains,
                    ProtectionDomain[] parentDomains) {
                calledDomainCombiner[i] = true;
                PermissionCollection pc = new Permissions();
                pc.add(new AllPermission());
                ProtectionDomain pd;
                // if run with the system classloader then there will be no
                // execution domains 
                if (executionDomains.length > 0) {
                    pd = new ProtectionDomain(executionDomains[0]
                            .getCodeSource(), pc);
                } else {
                    pd = new ProtectionDomain(parentDomains[0].getCodeSource(),
                            pc);
                }
                return new ProtectionDomain[] { pd };
            }
        }

        ProtectionDomain[] domains = new ProtectionDomain[] { new ProtectionDomain(
                new CodeSource(null, (Certificate[]) null), new Permissions()) };

        AccessControlContext parent = new AccessControlContext(domains);
        AccessControlContext c0 = new AccessControlContext(parent,
                new MyCombiner(0));
        final AccessControlContext c1 = new AccessControlContext(parent,
                new MyCombiner(1));

                class TestPermission extends BasicPermission {
                    TestPermission(String s) {
                        super(s);
                    }
                }
        
        SecurityManager sm = new SecurityManager() {
            public void checkPermission(Permission p) {
                if( p instanceof TestPermission ) {
                    super.checkPermission(p);   
                }
            }
        };
        sm.checkPermission(new SecurityPermission("let it load"));
        
        System.setSecurityManager(sm);
        try {
            AccessController.doPrivileged(new PrivilegedAction<Object>() {
                public Object run() {
                    // AccessController.getContext();
                    AccessController.checkPermission(new TestPermission(
                            "MyTest"));

                    AccessController.doPrivileged(new PrivilegedAction<Object>() {
                        public Object run() {
                            AccessController
                                    .checkPermission(new TestPermission(
                                            "MyTest"));
                            return null;
                        }
                    }, c1);
                    return null;
                }
            }, c0);
            assertTrue("Failed to combine domains for security permission",
                    calledDomainCombiner[0]);
            assertTrue("Failed to combine domains for security permission",
                    calledDomainCombiner[1]);
        } finally {
            System.setSecurityManager(null);
        }
    }
}