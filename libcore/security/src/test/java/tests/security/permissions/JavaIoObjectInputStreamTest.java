/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.NotActiveException;
import java.io.ObjectInputStream;
import java.io.SerializablePermission;
import java.io.StreamCorruptedException;
import java.security.Permission;

/*
 * This class tests the secrity permissions which are documented in
 * http://java.sun.com/j2se/1.5.0/docs/guide/security/permissions.html#PermsAndMethods
 * for classes
 *    java.io.ObjectInputStream
 *    java.io.ObjectOutputStream
 */
@TestTargetClass(SecurityManager.class)
public class JavaIoObjectInputStreamTest extends TestCase {
    
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
      purpose = "Verifies that ObjectInputStream.enableResolveObject method " +
            "calls checkPermission of security manager.",
      targets = {
        @TestTarget(
          methodName = "checkPermission",
          methodArgs = {java.security.Permission.class}
        )
    })
    public void test_ObjectInputStream() throws IOException {
        class TestSecurityManager extends SecurityManager {
            boolean called;
            Permission permission;
            void reset(){
                called = false;
                permission = null;
            }
            @Override
            public void checkPermission(Permission permission){
                if(permission instanceof SerializablePermission){
                    called = true;
                    this.permission = permission;
                }
                super.checkPermission(permission);
            }
        }
        
        // TestObjectInputStream is necessary in order to call enableResolveObject
        class TestObjectInputStream extends ObjectInputStream  {
            TestObjectInputStream(InputStream s) throws StreamCorruptedException, IOException {
                super(s);
            }
            @Override
            public boolean enableResolveObject(boolean enable) throws SecurityException {
                return super.enableResolveObject(enable);
            }
        }

        long id = new java.util.Date().getTime();
        String filename  = "SecurityPermissionsTest_"+id;
        File f = File.createTempFile(filename, null);
        f.deleteOnExit();

        TestObjectInputStream ois = new TestObjectInputStream(new FileInputStream(f));

        TestSecurityManager s = new TestSecurityManager();
        System.setSecurityManager(s);
        
        s.reset();
        ois.enableResolveObject(true);
        assertTrue("ObjectInputStream.enableResolveObject(boolean) must call checkPermission on security manager", s.called);
        assertEquals("Name of SerializablePermission is not correct", "enableSubstitution", s.permission.getName());
    }
    
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "Verifies that ObjectInputStream constructor calls " +
            "checkPermission method of security manager.",
      targets = {
        @TestTarget(
          methodName = "checkPermission",
          methodArgs = {java.security.Permission.class}
        )
    })
    public void test_ObjectInputOutputStream() throws IOException {
        class TestSecurityManager extends SecurityManager {
            boolean called;
            Permission permission;
            void reset(){
                called = false;
                permission = null;
            }
            @Override
            public void checkPermission(Permission permission){
                if(permission instanceof SerializablePermission){
                    called = true;
                    this.permission = permission;
                }
                super.checkPermission(permission);
            }
        }
        
        // Beginning with J2SE 1.4.0, ObjectOutputStream's public one-argument constructor
        // requires the "enableSubclassImplementation" SerializablePermission when invoked
        // (either directly or indirectly) by a subclass which overrides 
        // ObjectOutputStream.putFields or ObjectOutputStream.writeUnshared.
        //
        // Also beginning with J2SE 1.4.0, ObjectInputStream's public one-argument 
        // constructor requires the "enableSubclassImplementation" SerializablePermission 
        // when invoked (either directly or indirectly) by a subclass which overrides 
        // ObjectInputStream.readFields or ObjectInputStream.readUnshared.

        
        class TestObjectInputStream extends ObjectInputStream  {
            TestObjectInputStream(InputStream s) throws StreamCorruptedException, IOException {
                super(s);
            }
        }
        
        class TestObjectInputStream_readFields extends ObjectInputStream  {
            TestObjectInputStream_readFields(InputStream s) throws StreamCorruptedException, IOException {
                super(s);
            }
            @Override
            public GetField readFields() throws IOException, ClassNotFoundException, NotActiveException {
                return super.readFields();
            }
        }
        
        class TestObjectInputStream_readUnshared extends ObjectInputStream  {
            TestObjectInputStream_readUnshared(InputStream s) throws StreamCorruptedException, IOException {
                super(s);
            }
            @Override
            public Object readUnshared() throws IOException, ClassNotFoundException {
                return super.readUnshared();
            }
        }
        
        
        long id = new java.util.Date().getTime();
        String filename  = "SecurityPermissionsTest_"+id;
        File f = File.createTempFile(filename, null);
        f.deleteOnExit();
        
        TestSecurityManager s = new TestSecurityManager();
        System.setSecurityManager(s);
        
        s.reset();
        new ObjectInputStream(new FileInputStream(f));
        assertTrue("ObjectInputStream(InputStream) ctor must not call checkPermission on security manager on a class which neither overwrites methods readFields nor readUnshared", !s.called);
        
        s.reset();
        new TestObjectInputStream(new FileInputStream(f));
        assertTrue("ObjectInputStream(InputStream) ctor must not call checkPermission on security manager on a class which neither overwrites methods readFields nor readUnshared", !s.called);
        
        s.reset();
        new TestObjectInputStream_readFields(new FileInputStream(f));
        assertTrue("ObjectInputStream(InputStream) ctor must call checkPermission on security manager on a class which overwrites method readFields", s.called);
        assertEquals("Name of SerializablePermission is not correct", "enableSubclassImplementation", s.permission.getName());
        
        s.reset();
        new TestObjectInputStream_readUnshared(new FileInputStream(f));
        assertTrue("ObjectInputStream(InputStream) ctor must call checkPermission on security manager on a class which overwrites method readUnshared", s.called);
        assertEquals("Name of SerializablePermission is not correct", "enableSubclassImplementation", s.permission.getName());
    }
    
}
