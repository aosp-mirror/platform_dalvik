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

import junit.framework.TestCase;

/*
 * This class tests the secrity permissions which are documented in
 * http://java.sun.com/j2se/1.5.0/docs/guide/security/permissions.html#PermsAndMethods
 * for class java.lang.Class
 */
public class JavaLangClassTest extends TestCase {
    
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
    
    
    public void test_getProtectionDomain () {
        class TestSecurityManager extends SecurityManager {
            boolean called;
            void reset(){
                called = false;
            }
            @Override
            public void checkPermission(Permission permission){
                if(permission instanceof RuntimePermission && "getProtectionDomain".equals(permission.getName())){
                    called = true;
                }
                super.checkPermission(permission);
            }
        }

        Class c = java.lang.String.class;
        assertTrue("java.lang.String.class not assigned", c != null);

        TestSecurityManager s = new TestSecurityManager();
        System.setSecurityManager(s);
        
        s.reset();
        c.getProtectionDomain();
        assertTrue("Class.getProtectionDomain() must check RuntimePermission(\"getProtectionDomain\") on security manager", s.called);
    }

    public void test_Class() throws ClassNotFoundException {
        class TestSecurityManager extends SecurityManager {
            boolean called;
            void reset(){
                called = false;
            }
            @Override
            public void checkPermission(Permission permission){
                if(permission instanceof RuntimePermission && "getClassLoader".equals(permission.getName())){
                    called = true;
                }
                super.checkPermission(permission);
            }
        }

        TestSecurityManager s = new TestSecurityManager();
        System.setSecurityManager(s);
        
        s.reset();
        Class.forName("java.lang.String", true, null);
        assertTrue("Class.forName(String,boolean,Classloader) must check RuntimePermission(getClassLoader) on security manager", s.called);
    }
    
}
