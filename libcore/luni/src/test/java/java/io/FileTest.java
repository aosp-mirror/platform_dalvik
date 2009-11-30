/*
 * Copyright (C) 2009 The Android Open Source Project
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

package java.io;

import java.util.UUID;

import junit.framework.Test;
import junit.framework.TestSuite;

public class FileTest extends junit.framework.TestCase {
    private static File createTemporaryDirectory() throws Exception {
        String base = System.getProperty("java.io.tmpdir");
        File directory = new File(base, UUID.randomUUID().toString());
        assertTrue(directory.mkdirs());
        return directory;
    }
    
    private static String longString(int n) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < n; ++i) {
            result.append('x');
        }
        return result.toString();
    }
    
    private static File createDeepStructure(File base) throws Exception {
        // ext has a limit of around 256 characters for each path entry.
        // 128 characters should be safe for everything but FAT.
        String longString = longString(128);
        // Keep creating subdirectories until the path length is greater than 1KiB.
        // Ubuntu 8.04's kernel is happy up to about 4KiB.
        File f = base;
        for (int i = 0; f.toString().length() <= 1024; ++i) {
            f = new File(f, longString);
            assertTrue(f.mkdir());
        }
        return f;
    }
    
    // Rather than test all methods, assume that if createTempFile creates a long path and
    // exists can see it, the code for coping with long paths (shared by all methods) works.
    public void test_longPath() throws Exception {
        File base = createTemporaryDirectory();
        assertTrue(createDeepStructure(base).exists());
    }
    
    // readlink(2) is a special case,.
    public void test_longReadlink() throws Exception {
        File base = createTemporaryDirectory();
        File target = createDeepStructure(base);
        File source = new File(base, "source");
        assertFalse(source.exists());
        assertTrue(target.exists());
        assertTrue(target.getCanonicalPath().length() > 1024);
        Runtime.getRuntime().exec(new String[] { "ln", "-s", target.toString(), source.toString() }).waitFor();
        assertTrue(source.exists());
        assertEquals(target.getCanonicalPath(), source.getCanonicalPath());
    }
    
    // TODO: File.list is a special case too, but I haven't fixed it yet, and the new code,
    // like the old code, will die of a native buffer overrun if we exercise it.

    public void test_emptyFilename() throws Exception {
        // The behavior of the empty filename is an odd mixture.
        File f = new File("");
        // Mostly it behaves like an invalid path...
        assertFalse(f.canRead());
        assertFalse(f.canWrite());
        try {
            f.createNewFile();
            fail("expected IOException");
        } catch (IOException expected) {
        }
        assertFalse(f.delete());
        f.deleteOnExit();
        assertFalse(f.exists());
        assertEquals("", f.getName());
        assertEquals(null, f.getParent());
        assertEquals(null, f.getParentFile());
        assertEquals("", f.getPath());
        assertFalse(f.isAbsolute());
        assertFalse(f.isDirectory());
        assertFalse(f.isFile());
        assertFalse(f.isHidden());
        assertEquals(0, f.lastModified());
        assertEquals(0, f.length());
        assertEquals(null, f.list());
        assertEquals(null, f.list(null));
        assertEquals(null, f.listFiles());
        assertEquals(null, f.listFiles((FileFilter) null));
        assertEquals(null, f.listFiles((FilenameFilter) null));
        assertFalse(f.mkdir());
        assertFalse(f.mkdirs());
        assertFalse(f.renameTo(f));
        assertFalse(f.setLastModified(123));
        assertFalse(f.setReadOnly());
        // ...but sometimes it behaves like "user.dir".
        String cwd = System.getProperty("user.dir");
        assertEquals(new File(cwd), f.getAbsoluteFile());
        assertEquals(cwd, f.getAbsolutePath());
        assertEquals(new File(cwd), f.getCanonicalFile());
        assertEquals(cwd, f.getCanonicalPath());
    }
}
