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

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.security.Permission;

import junit.framework.TestCase;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargets;

/*
 * This class tests the security permissions which are documented in
 * http://java.sun.com/j2se/1.5.0/docs/guide/security/permissions.html#PermsAndMethods
 * for class java.io.File.
 */
@TestTargetClass(java.io.File.class)
public class JavaIoFileTest extends TestCase {
    
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
    
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies that File.delete and File.deleteOnExit methods call checkDelete on security manager.",
            method = "delete",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies that File.delete and File.deleteOnExit methods call checkDelete on security manager.",
            method = "deleteOnExit",
            args = {}
        )
    })
    public void test_File1() throws IOException {
        class TestSecurityManager extends SecurityManager {
            boolean called;
            String filename;
            
            void reset(){
                called = false;
                filename = null;
            }
            
            @Override
            public void checkDelete(String file) {
                called = true;
                this.filename = file;
            }
            @Override
            public void checkPermission(Permission p) {
                
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
        f.delete();
        assertTrue("File.delete must call checkDelete on security manager", s.called);
        assertEquals("Argument of checkDelete is not correct", filename, s.filename);
        
        s.reset();
        f.deleteOnExit();
        assertTrue("File.deleteOnExit must call checkDelete on security manager", s.called);        
        assertEquals("Argument of checkDelete is not correct", filename, s.filename);
    }
    
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies that File.exists(), File.canRead(), File.isFile(), File.isDirectory(), File.isHidden(), File.lastModified(), File.length(), File.list(...), File.listFiles(...) methods call checkRead method of security manager.",
            method = "exists",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies that File.exists(), File.canRead(), File.isFile(), File.isDirectory(), File.isHidden(), File.lastModified(), File.length(), File.list(...), File.listFiles(...) methods call checkRead method of security manager.",
            method = "canRead",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies that File.exists(), File.canRead(), File.isFile(), File.isDirectory(), File.isHidden(), File.lastModified(), File.length(), File.list(...), File.listFiles(...) methods call checkRead method of security manager.",
            method = "isFile",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies that File.exists(), File.canRead(), File.isFile(), File.isDirectory(), File.isHidden(), File.lastModified(), File.length(), File.list(...), File.listFiles(...) methods call checkRead method of security manager.",
            method = "isDirectory",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies that File.exists(), File.canRead(), File.isFile(), File.isDirectory(), File.isHidden(), File.lastModified(), File.length(), File.list(...), File.listFiles(...) methods call checkRead method of security manager.",
            method = "isHidden",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies that File.exists(), File.canRead(), File.isFile(), File.isDirectory(), File.isHidden(), File.lastModified(), File.length(), File.list(...), File.listFiles(...) methods call checkRead method of security manager.",
            method = "lastModified",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies that File.exists(), File.canRead(), File.isFile(), File.isDirectory(), File.isHidden(), File.lastModified(), File.length(), File.list(...), File.listFiles(...) methods call checkRead method of security manager.",
            method = "length",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies that File.exists(), File.canRead(), File.isFile(), File.isDirectory(), File.isHidden(), File.lastModified(), File.length(), File.list(...), File.listFiles(...) methods call checkRead method of security manager.",
            method = "list",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies that File.exists(), File.canRead(), File.isFile(), File.isDirectory(), File.isHidden(), File.lastModified(), File.length(), File.list(...), File.listFiles(...) methods call checkRead method of security manager.",
            method = "list",
            args = {java.io.FilenameFilter.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies that File.exists(), File.canRead(), File.isFile(), File.isDirectory(), File.isHidden(), File.lastModified(), File.length(), File.list(...), File.listFiles(...) methods call checkRead method of security manager.",
            method = "listFiles",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies that File.exists(), File.canRead(), File.isFile(), File.isDirectory(), File.isHidden(), File.lastModified(), File.length(), File.list(...), File.listFiles(...) methods call checkRead method of security manager.",
            method = "listFiles",
            args = {java.io.FilenameFilter.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies that File.exists(), File.canRead(), File.isFile(), File.isDirectory(), File.isHidden(), File.lastModified(), File.length(), File.list(...), File.listFiles(...) methods call checkRead method of security manager.",
            method = "listFiles",
            args = {java.io.FileFilter.class}
        )
    })
    public void test_File2() throws IOException {
        class TestSecurityManager extends SecurityManager {
            boolean called;
            String file;
            void reset(){
                called = false;
                file = null;
            }
            @Override
            public void checkRead(String file){
                called = true;
                this.file = file;
            }
            @Override
            public void checkPermission(Permission p) {
                
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
        f.exists();
        assertTrue("File.exists() must call checkRead on security manager", s.called);
        assertEquals("Argument of checkRead is not correct", filename, s.file);
        
        s.reset();
        f.canRead();
        assertTrue("File.canRead() must call checkRead on security manager", s.called);
        assertEquals("Argument of checkRead is not correct", filename, s.file);
        
        s.reset();
        f.isFile();
        assertTrue("File.isFile() must call checkRead on security manager", s.called);
        assertEquals("Argument of checkRead is not correct", filename, s.file);
        
        s.reset();
        f.isDirectory();
        assertTrue("File.isDirectory() must call checkRead on security manager", s.called);
        assertEquals("Argument of checkRead is not correct", filename, s.file);
        
        s.reset();
        f.isHidden();
        assertTrue("File.isHidden() must call checkRead on security manager", s.called);
        assertEquals("Argument of checkRead is not correct", filename, s.file);
        
        s.reset();
        f.lastModified();
        assertTrue("File.lastModified() must call checkRead on security manager", s.called);
        assertEquals("Argument of checkRead is not correct", filename, s.file);
        
        s.reset();
        f.length();
        assertTrue("File.length() must call checkRead on security manager", s.called);
        assertEquals("Argument of checkRead is not correct", filename, s.file);
        
        s.reset();
        f.list();
        assertTrue("File.list() must call checkRead on security manager", s.called);
        assertEquals("Argument of checkRead is not correct", filename, s.file);
        
        s.reset();
        f.list((FilenameFilter)null);
        assertTrue("File.list(FilenameFilter) must call checkRead on security manager", s.called);
        assertEquals("Argument of checkRead is not correct", filename, s.file);
        
        s.reset();
        f.listFiles();
        assertTrue("File.listFiles() must call checkRead on security manager", s.called);
        assertEquals("Argument of checkRead is not correct", filename, s.file);
        
        s.reset();
        f.listFiles((FilenameFilter)null);
        assertTrue("File.listFiles(FilenameFilter) must call checkRead on security manager", s.called);
        assertEquals("Argument of checkRead is not correct", filename, s.file);
        
        s.reset();
        f.listFiles((FileFilter)null);
        assertTrue("File.listFiles(FileFilter) must call checkRead on security manager", s.called);
        assertEquals("Argument of checkRead is not correct", filename, s.file);
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies that write/create methods of File class call checkWrite on security manager.",
            method = "canWrite",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies that write/create methods of File class call checkWrite on security manager.",
            method = "createNewFile",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies that write/create methods of File class call checkWrite on security manager.",
            method = "createTempFile",
            args = {java.lang.String.class, java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies that write/create methods of File class call checkWrite on security manager.",
            method = "createTempFile",
            args = {java.lang.String.class, java.lang.String.class, java.io.File.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies that write/create methods of File class call checkWrite on security manager.",
            method = "mkdir",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies that write/create methods of File class call checkWrite on security manager.",
            method = "mkdirs",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies that write/create methods of File class call checkWrite on security manager.",
            method = "renameTo",
            args = {java.io.File.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies that write/create methods of File class call checkWrite on security manager.",
            method = "setLastModified",
            args = {long.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies that write/create methods of File class call checkWrite on security manager.",
            method = "setReadOnly",
            args = {}
        )
    })
    public void test_File3() throws IOException {
        class TestSecurityManager extends SecurityManager {
            boolean called;
            String file;
            void reset(){
                called = false;
                file = null;
            }
            @Override
            public void checkWrite(String file){
                called = true;
                this.file = file;
            }
            @Override
            public void checkPermission(Permission p) {
                
            }
        }

        String tmpPath = System.getProperty("java.io.tmpdir");
        if(!tmpPath.endsWith("/")) {
            tmpPath += "/";
        }
        long id = new java.util.Date().getTime();
        String filename  = "SecurityPermissionsTest_"+id;
        String filename2 = "SecurityPermissionsTest_"+(id+1);
        File f = File.createTempFile(filename, null);
        f.deleteOnExit();
        filename = f.getCanonicalPath();

        File f2 = File.createTempFile(filename2, null);
        f2.deleteOnExit();
        filename2 = f2.getCanonicalPath();

        TestSecurityManager s = new TestSecurityManager();
        System.setSecurityManager(s);
        
        s.reset();
        f.canWrite();
        assertTrue("File.canWrite() must call checkWrite on security manager", s.called);
        assertEquals("Argument of checkWrite is not correct", filename, s.file);

        s.reset();
        f.createNewFile();
        assertTrue("File.createNewFile() must call checkWrite on security manager", s.called);
        assertEquals("Argument of checkWrite is not correct", filename, s.file);

        s.reset();
        File tmp = new File(tmpPath + "dir"+id);
        tmp.mkdir();
        assertTrue("File.canWrite() must call checkWrite on security manager", s.called);
        assertEquals("Argument of checkWrite is not correct", tmpPath + "dir"+id, s.file);

        s.reset();
        tmp = new File(tmpPath + "a"+id+"/b/c");
        tmp.mkdirs();
        assertTrue("File.mkdirs() must call checkWrite on security manager", s.called);
        assertEquals("Argument of checkWrite is not correct", tmpPath +"a"+id+"/b/c", s.file);
        
        s.reset();
        f.renameTo(f2);
        assertTrue("File.renameTo(File) must call checkWrite on security manager", s.called);
        assertEquals("Argument of checkWrite is not correct", filename2, s.file);
        
        s.reset();
        f.setLastModified(id);
        assertTrue("File.setLastModified() must call checkWrite on security manager", s.called);
        assertEquals("Argument of checkWrite is not correct", filename, s.file);
        
        s.reset();
        f.setReadOnly();
        assertTrue("File.setReadOnly() must call checkWrite on security manager", s.called);
        assertEquals("Argument of checkWrite is not correct", filename, s.file);
        
        s.reset();
        tmp = File.createTempFile("xxx", "yyy");
        tmp.deleteOnExit();
        filename = tmp.getCanonicalPath();
        assertTrue("File.createTempFile(String,String) must call checkWrite on security manager", s.called);
        assertEquals("Argument of checkWrite is not correct", filename, s.file);

        s.reset();
        tmp = File.createTempFile("xxx", "yyy", (File)null);
        tmp.deleteOnExit();
        filename = tmp.getCanonicalPath();
        assertTrue("File.createTempFile(String,String,File) must call checkWrite on security manager", s.called);
        assertEquals("Argument of checkWrite is not correct", filename, s.file);
    }
    
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies that checkRead(java.io.FileDescriptor) " +
                    "and checkPropertyAccess(java.lang.String) on " +
                    "security manager are called.",
            method = "getCanonicalFile",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies that checkRead(java.io.FileDescriptor) " +
                    "and checkPropertyAccess(java.lang.String) on " +
                    "security manager are called.",
            method = "getCanonicalPath",
            args = {}
        )
    })
    public void test_File4() throws IOException {
        class TestSecurityManager extends SecurityManager {
            boolean checkPropertyAccessCalled;
            
            void reset(){
                checkPropertyAccessCalled = false;
            }
            @Override
            public void checkPropertyAccess(String key){
                checkPropertyAccessCalled = true;
            }
            @Override
            public void checkPermission(Permission p) {
                
            }
        }

        long id = new java.util.Date().getTime();
        TestSecurityManager s = new TestSecurityManager();
        System.setSecurityManager(s);

        String filename = "SecurityPermissionsTest_" + id;
        File f = new File(filename);

        try {
            s.reset();
            f.getCanonicalFile();
            assertTrue("File.getCanonicalFile() must call checkPropertyAccess " +
                    "on security manager", s.checkPropertyAccessCalled);
            
            s.reset();
            f = new File(filename);
            filename = f.getCanonicalPath();
            assertTrue("File.getCanonicalPath() must call checkPropertyAccess " +
                    " on security manager", s.checkPropertyAccessCalled);
        } finally {
            f.delete();
        }
    }
    
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies that checkPropertyAccess(java.lang.String) on " +
                    "security manager is called.",
            method = "getAbsoluteFile",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies that checkPropertyAccess(java.lang.String) on " +
                    "security manager is called.",
            method = "getAbsolutePath",
            args = {}
        )
    })
    public void test_File5() throws IOException {
        class TestSecurityManager extends SecurityManager {
            boolean checkPropertyAccessCalled;
            
            void reset(){
                checkPropertyAccessCalled = false;
            }
            @Override
            public void checkPropertyAccess(String key){
                checkPropertyAccessCalled = true;
            }
            @Override
            public void checkPermission(Permission p) {
                
            }
        }

        long id = new java.util.Date().getTime();
        TestSecurityManager s = new TestSecurityManager();
        System.setSecurityManager(s);

        String filename = "SecurityPermissionsTest_" + id;
        File f = new File(filename);

        try {
            s.reset();
            f.getAbsoluteFile();
            assertTrue("File.getAbsoluteFile() must call checkPropertyAccess " +
                    "on security manager", s.checkPropertyAccessCalled);
            
            s.reset();
            f = new File(filename);
            filename = f.getAbsolutePath();
            assertTrue("File.getAbsolutePath() must call checkPropertyAccess " +
                    " on security manager", s.checkPropertyAccessCalled);
        } finally {
            f.delete();
        }
    }
}

