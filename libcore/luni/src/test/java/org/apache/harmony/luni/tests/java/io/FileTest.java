/* Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.harmony.luni.tests.java.io;

import java.io.File;

import junit.framework.TestCase;

import org.apache.harmony.testframework.serialization.SerializationTest;

import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestTargetNew;

@TestTargetClass(File.class)
public class FileTest extends TestCase {

    /**
     * @tests java.io.File#File(java.io.File, java.lang.String)
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "File",
        args = {java.io.File.class, java.lang.String.class}
    )
    public void test_ConstructorLjava_io_FileLjava_lang_String() {
        // Regression test for HARMONY-21
        File path = new File("/dir/file");
        File root = new File("/");
        File file = new File(root, "/dir/file");
        assertEquals("Assert 1: wrong path result ", path.getPath(), file
                .getPath());
        assertFalse("Assert 1.1: path absolute ", new File("\\\\\\a\b").isAbsolute());
        assertTrue("Assert 1.1: path absolute ", new File("///a/b").isAbsolute());

        // Test data used in a few places below
        String dirName = System.getProperty("java.io.tmpdir");
        String fileName = "input.tst";

        // change user.dir to a folder that's writeable on android.
        String oldUserDir = System.getProperty("user.dir");
        System.setProperty("user.dir", dirName);

        // Check filename is preserved correctly
        File d = new File(dirName);
        File f = new File(d, fileName);
        if (!dirName
                .regionMatches((dirName.length() - 1), File.separator, 0, 1)) {
            dirName += File.separator;
        }
        dirName += fileName;
        assertTrue("Assert 2: Created incorrect file " + f.getPath(), f
                .getPath().equals(dirName));

        // Check null argument is handled
        try {
            f = new File(d, null);
            fail("Assert 3: NullPointerException not thrown.");
        } catch (NullPointerException e) {
            // Expected.
        }

        f = new File((File) null, fileName);
        assertEquals("Assert 4: Created incorrect file " + f.getPath(), dirName,
                f.getAbsolutePath());

        // Regression for HARMONY-46
        File f1 = new File("a");
        File f2 = new File("a/");
        assertEquals("Assert 5: Trailing slash file name is incorrect", f1, f2);

        // reset user.dir
        System.setProperty("user.dir", oldUserDir);
    }
    
    /**
     * @tests java.io.File#hashCode()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "hashCode",
        args = {}
    )
    public void test_hashCode() {
        // Regression for HARMONY-53
        String mixedFname = "SoMe FiLeNaMe";
        File mfile = new File(mixedFname);
        File lfile = new File(mixedFname.toLowerCase());

        if (mfile.equals(lfile)) {
            assertTrue("Assert 0: wrong hashcode", mfile.hashCode() == lfile.hashCode());
        } else {
            assertFalse("Assert 1: wrong hashcode", mfile.hashCode() == lfile.hashCode());
        }
    }

    /**
     * @tests java.io.File#getPath()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getPath",
        args = {}
    )
    public void test_getPath() {
        // Regression for HARMONY-444
        File file;
        String separator = File.separator;

        file = new File((File) null, "x/y/z");
        assertEquals("x" + separator + "y" + separator + "z", file.getPath());

        file = new File((String) null, "x/y/z");
        assertEquals("x" + separator + "y" + separator + "z", file.getPath());
    }
    
    /**
     * @tests java.io.File#getPath()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getPath",
        args = {}
    )
    public void test_getPath_With_Empty_FileName() {
        // Regression for HARMONY-829
        String f1ParentName = "01";
        File f1 = new File(f1ParentName, "");
        assertEquals(f1ParentName, f1.getPath());
        
        String f2ParentName = "0";
        File f2 = new File(f2ParentName, "");

        assertEquals(-1, f2.compareTo(f1));
        assertEquals(1, f1.compareTo(f2));

        File parent = new File(System.getProperty("java.io.tmpdir"));
        File f3 = new File(parent, "");

        assertEquals(parent.getPath(), f3.getPath());
        
        
    }
    
    /**
     * @tests serialization/deserialization.
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies self serialization/deserialization.",
        method = "!SerializationSelf",
        args = {}
    )    
    public void test_serialization_self() throws Exception {
        File testFile = new File("test.ser");
        SerializationTest.verifySelf(testFile);
    }

    /**
     * @tests serialization/deserialization compatibility with RI.
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization/deserialization compatibility.",
        method = "!SerializationGolden",
        args = {}
    )
    public void test_serialization_compatibility() throws Exception {
        File file = new File("FileTest.golden.ser");
        SerializationTest.verifyGolden(this, file);
    }

}
