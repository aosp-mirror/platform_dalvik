/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package tests.api.java.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;

import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestTargetNew;

@TestTargetClass(FileReader.class) 
public class FileReaderTest extends junit.framework.TestCase {

    FileReader br;

    BufferedWriter bw;

    FileInputStream fis;

    File f;

    /**
     * @tests java.io.FileReader#FileReader(java.io.File)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "FileReader",
        args = {java.io.File.class}
    )     
    public void test_ConstructorLjava_io_File() {
        // Test for method java.io.FileReader(java.io.File)
        try {
            bw = new BufferedWriter(new FileWriter(f.getPath()));
            bw.write(" After test string", 0, 18);
            bw.close();
            br = new FileReader(f);
            char[] buf = new char[100];
            int r = br.read(buf);
            br.close();
            assertEquals("Test 1: Failed to read correct chars", 
                    " After test string", new String(buf, 0, r));
        } catch (Exception e) {
            fail("Exception during Constructor test " + e.toString());
        }
        
        File noFile = new File(System.getProperty("java.io.tmpdir"), "noreader.tst");
        try {
            br = new FileReader(noFile);
            fail("Test 2: FileNotFoundException expected.");
        } catch (FileNotFoundException e) {
            // Expected.
        }
    }

    /**
     * @tests java.io.FileReader#FileReader(java.io.FileDescriptor)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies FileReader(java.io.FileDescriptor) constructor.",
        method = "FileReader",
        args = {java.io.FileDescriptor.class}
    )     
    public void test_ConstructorLjava_io_FileDescriptor() {
        // Test for method java.io.FileReader(java.io.FileDescriptor)
        try {
            bw = new BufferedWriter(new FileWriter(f.getPath()));
            bw.write(" After test string", 0, 18);
            bw.close();
            FileInputStream fis = new FileInputStream(f.getPath());
            br = new FileReader(fis.getFD());
            char[] buf = new char[100];
            int r = br.read(buf);
            br.close();
            fis.close();
            assertEquals("Failed to read correct chars", 
                    " After test string", new String(buf, 0, r));
        } catch (Exception e) {
            fail("Exception during Constructor test " + e.toString());
        }
    }

    /**
     * @tests java.io.FileReader#FileReader(java.lang.String)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "FileReader",
        args = {java.lang.String.class}
    )         
    public void test_ConstructorLjava_lang_String() {
        // Test for method java.io.FileReader(java.lang.String)
        try {
            bw = new BufferedWriter(new FileWriter(f.getPath()));
            bw.write(" After test string", 0, 18);
            bw.close();
            br = new FileReader(f.getPath());
            char[] buf = new char[100];
            int r = br.read(buf);
            br.close();
            assertEquals("Test 1: Failed to read correct chars", 
                    " After test string", new String(buf, 0, r));
        } catch (Exception e) {
            fail("Exception during Constructor test " + e.toString());
        }
 
        try {
            br = new FileReader(System.getProperty("java.io.tmpdir") + "/noreader.tst");
            fail("Test 2: FileNotFoundException expected.");
        } catch (FileNotFoundException e) {
            // Expected.
        }
      }

    /**
     * Sets up the fixture, for example, open a network connection. This method
     * is called before a test is executed.
     */
    protected void setUp() {

        f = new File(System.getProperty("java.io.tmpdir"), "reader.tst");

        if (f.exists()) {
            if (!f.delete()) {
                fail("Unable to delete test file");
            }
        }
    }

    /**
     * Tears down the fixture, for example, close a network connection. This
     * method is called after a test is executed.
     */
    protected void tearDown() {

        try {
            bw.close();
            br.close();
        } catch (Exception e) {
        }

        try {
            if (fis != null)
                fis.close();
        } catch (Exception e) {
        }
        f.delete();
    }
}
