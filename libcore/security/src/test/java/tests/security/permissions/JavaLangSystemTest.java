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

import java.io.InputStream;
import java.io.PrintStream;
import java.security.Permission;
import java.util.Properties;
import java.util.PropertyPermission;
/*
 * This class tests the secrity permissions which are documented in
 * http://java.sun.com/j2se/1.5.0/docs/guide/security/permissions.html#PermsAndMethods
 * for class java.lang.System
 */
@TestTargetClass(java.lang.SecurityManager.class)
public class JavaLangSystemTest extends TestCase {
    
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
      purpose = "Verifies that getProperties calls checkPropertiesAccess " +
            "method of security manager.",
      targets = {
        @TestTarget(
          methodName = "checkPropertiesAccess",
          methodArgs = {}
        )
    })
    public void test_Properties() {
        class TestSecurityManager extends SecurityManager {
            boolean called;
            
            void reset(){
                called = false;
            }
            
            @Override
            public void checkPropertiesAccess() {
                called = true;
                super.checkPropertiesAccess();
            }
        }
        
        TestSecurityManager s = new TestSecurityManager();
        System.setSecurityManager(s);
        
        s.reset();
        Properties props = System.getProperties();
        assertTrue("System.getProperties must call checkPropertiesAccess on security manager", s.called);
        
        s.reset();
        System.setProperties(props);
        assertTrue("System.setProperties must call checkPropertiesAccess on security manager", s.called);
    }
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "Verifies that System.getProperty calls checkPropertyAccess on " +
            "security manager.",
      targets = {
        @TestTarget(
          methodName = "checkPropertyAccess",
          methodArgs = {java.lang.String.class}
        )
    })
    public void test_getProperty() {
        class TestSecurityManager extends SecurityManager {
            boolean called;
            String key;
            
            void reset(){
                called = false;
                key = null;
            }
            
            @Override
            public void checkPropertyAccess(String key) {
                called = true;
                this.key = key;
                super.checkPropertyAccess(key);
            }
        }
        
        TestSecurityManager s = new TestSecurityManager();
        System.setSecurityManager(s);
        
        s.reset();
        System.getProperty("key");
        assertTrue("System.getProperty must call checkPropertyAccess on security manager", s.called);
        assertEquals("Argument of checkPropertyAccess is not correct", "key", s.key);
        
        s.reset();
        System.getProperty("key", "value");
        assertTrue("System.getProperty must call checkPropertyAccess on security manager", s.called);
        assertEquals("Argument of checkPropertyAccess is not correct", "key", s.key);
    }
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "Verifies that System.setProperty method calls " +
            "checkPermission of security manager.",
      targets = {
        @TestTarget(
          methodName = "checkPermission",
          methodArgs = {java.security.Permission.class}
        )
    })
    public void test_setProperty() {
        class TestSecurityManager extends SecurityManager {
            boolean called;
            Permission p;
            
            void reset(){
                called = false;
                p = null;
            }
            
            @Override
            public void checkPermission(Permission p) {
                called = true;
                this.p = p;
                super.checkPermission(p);
            }
        }
        
        TestSecurityManager s = new TestSecurityManager();
        System.setSecurityManager(s);
        
        s.reset();
        System.setProperty("key", "value");
        assertTrue("System.setProperty must call checkPermission on security manager", s.called);
        assertEquals("Argument of checkPermission is not correct", new PropertyPermission("key", "write"), s.p);
    }
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "Verifies that System.setSecurityManager method checks " +
            "security permissions.",
      targets = {
        @TestTarget(
          methodName = "checkPermission",
          methodArgs = {java.security.Permission.class}
        )
    })
    public void test_setSecurityManager() {
        class TestSecurityManager extends SecurityManager {
            boolean called = false;
            @Override
            public void checkPermission(Permission permission) {
                if(permission instanceof RuntimePermission && "setSecurityManager".equals(permission.getName())){
                    called = true;              
                }
                super.checkPermission(permission);
            }
            
        }
        TestSecurityManager s = new TestSecurityManager();
        System.setSecurityManager(s);
        
        System.setSecurityManager(s);
        assertTrue("System.setSecurityManager must check security permissions", s.called);
    }
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "Verifies that setIn/Out/Err methods call checkPermission " +
            "method of security manager.",
      targets = {
        @TestTarget(
          methodName = "checkPermission",
          methodArgs = {java.security.Permission.class}
        )
    })
    public void test_setInOutErr() {
        class TestSecurityManager extends SecurityManager {
            boolean called;
            Permission p;
            
            void reset(){
                called = false;
                p = null;
            }
            
            @Override
            public void checkPermission(Permission p) {
                called = true;
                this.p = p;
                super.checkPermission(p);
            }
        }
        
        InputStream in = System.in;
        PrintStream out = System.out;
        PrintStream err = System.err;
        Permission p = new RuntimePermission("setIO");

        TestSecurityManager s = new TestSecurityManager();
        System.setSecurityManager(s);
        
        s.reset();
        System.setIn(in);
        assertTrue("System.setIn(Inputstream) must call checkPermission on security manager", s.called);
        assertEquals("Argument of checkPermission is not correct", p, s.p);

        System.setOut(err);
        assertTrue("System.setOut(PrintStream) must call checkPermission on security manager", s.called);
        assertEquals("Argument of checkPermission is not correct", p, s.p);

        System.setErr(out);
        assertTrue("System.setErr(PrintStream) must call checkPermission on security manager", s.called);
        assertEquals("Argument of checkPermission is not correct", p, s.p);
    }
}

