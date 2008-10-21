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
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;

import junit.framework.TestCase;

/*
 * This class tests the secrity permissions which are documented in
 * http://java.sun.com/j2se/1.5.0/docs/guide/security/permissions.html#PermsAndMethods
 * for class java.io.File.
 */
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
                super.checkDelete(file);
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
                super.checkRead(file);
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
                super.checkWrite(file);
            }
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
        File tmp = new File("/tmp/dir"+id);
        tmp.mkdir();
        assertTrue("File.canWrite() must call checkWrite on security manager", s.called);
        assertEquals("Argument of checkWrite is not correct", "/tmp/dir"+id, s.file);

        s.reset();
        tmp = new File("/tmp/a"+id+"/b/c");
        tmp.mkdirs();
        assertTrue("File.mkdirs() must call checkWrite on security manager", s.called);
        assertEquals("Argument of checkWrite is not correct", "/tmp/a"+id+"/b/c", s.file);
        
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
}

