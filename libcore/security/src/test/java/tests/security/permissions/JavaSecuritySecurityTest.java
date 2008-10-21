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

import java.security.Permission;
import java.security.Provider;
import java.security.Security;
import java.security.SecurityPermission;
import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

/*
 * This class tests the secrity permissions which are documented in
 * http://java.sun.com/j2se/1.5.0/docs/guide/security/permissions.html#PermsAndMethods
 * for class java.security.Security
 */
public class JavaSecuritySecurityTest extends TestCase {
    
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
    
    public void test_getProperty() {
        class TestSecurityManager extends SecurityManager {
            boolean called = false;
            void reset(){
                called = false;
            }
            @Override
            public void checkPermission(Permission permission) {
                if(permission instanceof SecurityPermission && "getProperty.key".equals(permission.getName())){
                    called = true;              
                }
                super.checkPermission(permission);
            }
            
        }
        TestSecurityManager s = new TestSecurityManager();
        System.setSecurityManager(s);
        
        s.reset();
        Security.getProperty("key");
        assertTrue("java.security.Security.getProperty() must call checkPermission on security permissions", s.called);
    }
    
    public void test_setProperty() {
        class TestSecurityManager extends SecurityManager {
            boolean called = false;
            String target = null;
            void reset(){
                called = false;
                target = null;
            }
            @Override
            public void checkSecurityAccess(String target) {
                called = true;       
                this.target = target;
                super.checkSecurityAccess(target);
            }
            
        }
        TestSecurityManager s = new TestSecurityManager();
        System.setSecurityManager(s);
        
        s.reset();
        Security.setProperty("key", "value");
        assertTrue("java.security.Security.setProperty() must call checkSecurityAccess on security manager", s.called);
        assertEquals("Argument of checkSecurityAccess is not correct", "setProperty.key", s.target);
    }
    
    public void test_Provider() {
        class TestSecurityManager extends SecurityManager {
            boolean called = false;
            Set<String> targets = new HashSet<String>();
            void reset(){
                called = false;
                targets.clear();
            }
            @Override
            public void checkSecurityAccess(String target) {
                called = true;       
                this.targets.add(target);
                super.checkSecurityAccess(target);
            }
            
        }
        
        class MyProvider extends Provider {
            private static final long serialVersionUID = 1L;
            MyProvider(){
                super("DummyProvider", 1.0, "Provider for test purposes only");
            }
        }
        
        Provider p = new MyProvider();
        
        TestSecurityManager s = new TestSecurityManager();
        System.setSecurityManager(s);
        
        s.reset();        
        Security.addProvider(p);
        assertTrue("java.security.Security.addProvider() must call checkSecurityAccess on security manager", s.called);
        assertTrue("Argument of checkSecurityAccess is not correct", s.targets.contains("insertProvider.DummyProvider"));
        
        s.reset();        
        Security.removeProvider(p.getName());
        assertTrue("java.security.Security.removeProvider() must call checkSecurityAccess on security manager", s.called);
        assertTrue("Argument of checkSecurityAccess is not correct", s.targets.contains("removeProvider.DummyProvider"));
    }
}
