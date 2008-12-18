/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tests.security.permissions;

import dalvik.annotation.TestInfo;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTarget;
import dalvik.annotation.TestTargetClass;

import junit.framework.TestCase;

import java.security.Permission;
import java.security.Policy;
import java.security.SecurityPermission;
/*
 * This class tests the secrity permissions which are documented in
 * http://java.sun.com/j2se/1.5.0/docs/guide/security/permissions.html#PermsAndMethods
 * for class java.security.Policy
 */
@TestTargetClass(SecurityManager.class)
public class JavaSecurityPolicyTest extends TestCase {
    
    SecurityManager old;

    @Override
    protected void setUp() throws Exception {
        old = System.getSecurityManager();
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        System.setSecurityManager(old);
        super.tearDown();
    }
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "Verifies that java.security.Policy.getPolicy() method calls " +
            "checkPermission of security permissions.",
      targets = {
        @TestTarget(
          methodName = "checkPermission",
          methodArgs = {java.security.Permission.class}
        )
    })
    public void test_getPolicy() {
        class TestSecurityManager extends SecurityManager {
            boolean called = false;
            void reset(){
                called = false;
            }
            @Override
            public void checkPermission(Permission permission) {
                if(permission instanceof SecurityPermission && "getPolicy".equals(permission.getName())){
                    called = true;              
                }
                super.checkPermission(permission);
            }
            
        }
        TestSecurityManager s = new TestSecurityManager();
        System.setSecurityManager(s);
        
        s.reset();
        Policy.getPolicy();
        assertTrue("java.security.Policy.getPolicy() must call checkPermission on security permissions", s.called);
    }
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "Verifies that java.security.Policy.setPolicy() method calls " +
            "checkPermission on security permissions.",
      targets = {
        @TestTarget(
          methodName = "checkPermission",
          methodArgs = {java.security.Permission.class}
        )
    })
    public void test_setPolicy() {
        class TestSecurityManager extends SecurityManager {
            boolean called = false;
            void reset(){
                called = false;
            }
            @Override
            public void checkPermission(Permission permission) {
                if(permission instanceof SecurityPermission && "setPolicy".equals(permission.getName())){
                    called = true;              
                }
                super.checkPermission(permission);
            }
            
        }
        
        Policy p = Policy.getPolicy();
        
        TestSecurityManager s = new TestSecurityManager();
        System.setSecurityManager(s);
        
        s.reset();
        Policy.setPolicy(p);
        assertTrue("java.security.Policy.setPolicy() must call checkPermission on security permissions", s.called);
    }
}
