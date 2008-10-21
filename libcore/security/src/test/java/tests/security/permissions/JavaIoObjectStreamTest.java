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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.NotActiveException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.SerializablePermission;
import java.io.StreamCorruptedException;
import java.security.Permission;

import junit.framework.TestCase;

/*
 * This class tests the secrity permissions which are documented in
 * http://java.sun.com/j2se/1.5.0/docs/guide/security/permissions.html#PermsAndMethods
 * for classes
 *    java.io.ObjectInputStream
 *    java.io.ObjectOutputStream
 */
public class JavaIoObjectStreamTest extends TestCase {
    
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
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(f));
        oos.close();
        TestObjectInputStream ois = new TestObjectInputStream(new FileInputStream(f));

        TestSecurityManager s = new TestSecurityManager();
        System.setSecurityManager(s);
        
        s.reset();
        ois.enableResolveObject(true);
        assertTrue("ObjectInputStream.enableResolveObject(boolean) must call checkPermission on security manager", s.called);
        assertEquals("Name of SerializablePermission is not correct", "enableSubstitution", s.permission.getName());
    }

    
    public void test_ObjectOutputStream() throws IOException {
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
        
        // TestObjectOutputStream is necessary in order to call enableReplaceObject
        class TestObjectOutputStream extends ObjectOutputStream  {
            TestObjectOutputStream(OutputStream s) throws StreamCorruptedException, IOException {
                super(s);
            }
            @Override
            public boolean enableReplaceObject(boolean enable) throws SecurityException {
                return super.enableReplaceObject(enable);
            }
        }

        long id = new java.util.Date().getTime();
        String filename  = "SecurityPermissionsTest_"+id;
        File f = File.createTempFile(filename, null);
        f.deleteOnExit();

        TestObjectOutputStream ois = new TestObjectOutputStream(new FileOutputStream(f));

        TestSecurityManager s = new TestSecurityManager();
        System.setSecurityManager(s);
        
        s.reset();
        ois.enableReplaceObject(true);
        assertTrue("ObjectOutputStream.enableReplaceObject(boolean) must call checkPermission on security manager", s.called);
        assertEquals("Name of SerializablePermission is not correct", "enableSubstitution", s.permission.getName());
    }

    
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

        class TestObjectOutputStream extends ObjectOutputStream  {
            TestObjectOutputStream(OutputStream s) throws StreamCorruptedException, IOException {
                super(s);
            }
        }
        
        class TestObjectOutputStream_putFields extends ObjectOutputStream  {
            TestObjectOutputStream_putFields(OutputStream s) throws StreamCorruptedException, IOException {
                super(s);
            }
            @Override 
            public PutField putFields() throws IOException {
                return super.putFields();
            }
        }
        
        class TestObjectOutputStream_writeUnshared extends ObjectOutputStream  {
            TestObjectOutputStream_writeUnshared(OutputStream s) throws StreamCorruptedException, IOException {
                super(s);
            }
            @Override 
            public void writeUnshared(Object object) throws IOException {
                super.writeUnshared(object);
            }

        }
        
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
        new ObjectOutputStream(new FileOutputStream(f));
        assertTrue("ObjectOutputStream(OutputStream) ctor must not call checkPermission on security manager on a class which neither overwrites writeUnshared nor putFields", !s.called);
        
        s.reset();
        new TestObjectOutputStream(new FileOutputStream(f));
        assertTrue("ObjectOutputStream(OutputStream) ctor must not call checkPermission on security manager on a class which neither overwrites writeUnshared nor putFields", !s.called);
        
        s.reset();
        new TestObjectOutputStream_writeUnshared(new FileOutputStream(f));
        assertTrue("ObjectOutputStream(OutputStream) ctor must call checkPermission on security manager on a class which overwrites method writeUnshared", s.called);
        assertEquals("Name of SerializablePermission is not correct", "enableSubclassImplementation", s.permission.getName());
        
        s.reset();
        new TestObjectOutputStream_putFields(new FileOutputStream(f));
        assertTrue("ObjectOutputStream(OutputStream) ctor must call checkPermission on security manager on a class which overwrites method putFields", s.called);
        assertEquals("Name of SerializablePermission is not correct", "enableSubclassImplementation", s.permission.getName());
        
        
        s.reset();
        new ObjectInputStream(new FileInputStream(f));
        assertTrue("ObjectOutputStream(OutputStream) ctor must not call checkPermission on security manager on a class which neither overwrites methods readFields nor readUnshared", !s.called);
        
        s.reset();
        new TestObjectInputStream(new FileInputStream(f));
        assertTrue("ObjectOutputStream(OutputStream) ctor must not call checkPermission on security manager on a class which neither overwrites methods readFields nor readUnshared", !s.called);
        
        s.reset();
        new TestObjectInputStream_readFields(new FileInputStream(f));
        assertTrue("ObjectOutputStream(OutputStream) ctor must call checkPermission on security manager on a class which overwrites method readFields", s.called);
        assertEquals("Name of SerializablePermission is not correct", "enableSubclassImplementation", s.permission.getName());
        
        s.reset();
        new TestObjectInputStream_readUnshared(new FileInputStream(f));
        assertTrue("ObjectOutputStream(OutputStream) ctor must call checkPermission on security manager on a class which overwrites method readUnshared", s.called);
        assertEquals("Name of SerializablePermission is not correct", "enableSubclassImplementation", s.permission.getName());
    }
    
}
