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
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;

/*
 * This class tests the secrity permissions which are documented in
 * http://java.sun.com/j2se/1.5.0/docs/guide/security/permissions.html#PermsAndMethods
 * for class java.io.FileInputStream and java.io.FileOutputStream
 */
@TestTargetClass(SecurityManager.class)
public class JavaIoFileOutputStreamTest extends TestCase {
    
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
      purpose = "Verifies that FileOutputStream constructor calls checkRead " +
            "method of security manager.",
      targets = {
        @TestTarget(
          methodName = "checkRead",
          methodArgs = {java.lang.String.class}
        ),
        @TestTarget(
          methodName = "checkRead",
          methodArgs = {java.lang.String.class}
        )
    })    
    public void test_FileOutputStream1() throws IOException {
        class TestSecurityManager extends SecurityManager {
            boolean called;
            String file;
            FileDescriptor fd;
            void reset(){
                called = false;
                file = null;
                fd = null;
            }
            @Override
            public void checkWrite(FileDescriptor fd) {
                called = true;
                this.fd = fd;
                super.checkWrite(fd);
            }
            @Override
            public void checkWrite(String file){
                called = true;
                this.file = file;
                super.checkWrite(file);
            }
        }

        long id = new java.util.Date().getTime();
        String filename = "SecurityPermissionsTest_"+id;
        File f = File.createTempFile(filename, null);
        f.deleteOnExit();
        filename = f.getCanonicalPath();

        TestSecurityManager s = new TestSecurityManager();
        System.setSecurityManager(s);
        
        s.reset();
        FileDescriptor fd = new FileDescriptor();
        new FileOutputStream(fd);
        assertTrue("FileOutputStream(FileDescriptor) ctor must call checkWrite on security manager", s.called);
        assertEquals("Argument of checkWrite is not correct", fd, s.fd);
        
        s.reset();
        new FileOutputStream(f);
        assertTrue("FileOutputStream(File) ctor must call checkWrite on security manager", s.called);
        assertEquals("Argument of checkWrite is not correct", filename, s.file); 
        
        s.reset();
        new FileOutputStream(filename);
        assertTrue("FileOutputStream(String) ctor must call checkWrite on security manager", s.called);
        assertEquals("Argument of checkWrite is not correct", filename, s.file);
        
        s.reset();
        new FileOutputStream(filename, true);
        assertTrue("FileOutputStream(String,boolean) ctor must call checkWrite on security manager", s.called);
        assertEquals("Argument of checkWrite is not correct", filename, s.file); 
    }
    
}
