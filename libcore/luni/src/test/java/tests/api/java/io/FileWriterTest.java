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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestTargetNew;

@TestTargetClass(FileWriter.class) 
public class FileWriterTest extends junit.framework.TestCase {

    FileWriter fw;

    FileInputStream fis;

    BufferedWriter bw;

    File f;

    FileOutputStream fos;

    BufferedReader br;

    /**
     * @tests java.io.FileWriter#FileWriter(java.io.File)
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        method = "FileWriter",
        args = {java.io.File.class}
    )      
    public void test_ConstructorLjava_io_File() {
        // Test for method java.io.FileWriter(java.io.File)
        try {
            fos = new FileOutputStream(f.getPath());
            fos.write("Test String".getBytes());
            fos.close();
            bw = new BufferedWriter(new FileWriter(f));
            bw.write(" After test string", 0, 18);
            bw.close();
            br = new BufferedReader(new FileReader(f.getPath()));
            char[] buf = new char[100];
            int r = br.read(buf);
            br.close();
            assertEquals("Failed to write correct chars", " After test string", new String(buf, 0, r)
                    );
        } catch (Exception e) {
            fail("Exception during Constructor test " + e.toString());
        }
    }
    
    /**
     * @tests java.io.FileWriter#FileWriter(java.io.File)
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Checks IOException.",
        method = "FileWriter",
        args = {java.io.File.class}
    )      
    public void test_ConstructorLjava_io_File_IOException() {
        File dir = new File(System.getProperty("java.io.tmpdir"));
        
        try {
            fw = new FileWriter(dir);
            fail("Test 1: IOException expected.");
        } catch (IOException e) {
            // Expected.
        }
    }

    /**
     * @tests java.io.FileWriter#FileWriter(java.io.File, boolean)
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies FileWriter(java.io.File, boolean) constructor.",
        method = "FileWriter",
        args = {java.io.File.class, boolean.class}
    )              
    public void test_ConstructorLjava_io_FileZ() throws Exception {
        // Test for method java.io.FileWriter(java.io.File)
        //append = true
        {
            FileWriter fileWriter = new FileWriter(f);

            String first = "The first string for testing. ";
            fileWriter.write(first);
            fileWriter.close();

            fileWriter = new FileWriter(f, true);
            String second = "The second String for testing.";
            fileWriter.write(second);
            fileWriter.close();

            FileReader fileReader = new FileReader(f);
            char[] out = new char[first.length() + second.length() + 10];
            int length = fileReader.read(out);
            fileReader.close();
            assertEquals(first + second, new String(out, 0, length));
        }
        //append = false
        {
            FileWriter fileWriter = new FileWriter(f);
            String first = "The first string for testing. ";
            fileWriter.write(first);
            fileWriter.close();

            fileWriter = new FileWriter(f, false);
            String second = "The second String for testing.";
            fileWriter.write(second);
            fileWriter.close();

            FileReader fileReader = new FileReader(f);
            char[] out = new char[first.length() + second.length() + 10];
            int length = fileReader.read(out);
            fileReader.close();
            assertEquals(second, new String(out, 0, length));
        }
    }
    
    /**
     * @tests java.io.FileWriter#FileWriter(java.io.File, boolean)
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Checks IOException.",
        method = "FileWriter",
        args = {java.io.File.class, boolean.class}
    )      
    public void test_ConstructorLjava_io_FileZ_IOException() {
        File dir = new File(System.getProperty("java.io.tmpdir"));
        
        try {
            fw = new FileWriter(dir, true);
            fail("Test 1: IOException expected.");
        } catch (IOException e) {
            // Expected.
        }
    }

    /**
     * @tests java.io.FileWriter#FileWriter(java.io.FileDescriptor)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies FileWriter(java.io.FileDescriptor) constructor.",
        method = "FileWriter",
        args = {java.io.FileDescriptor.class}
    )          
    public void test_ConstructorLjava_io_FileDescriptor() {
        // Test for method java.io.FileWriter(java.io.FileDescriptor)
        try {
            fos = new FileOutputStream(f.getPath());
            fos.write("Test String".getBytes());
            fos.close();
            fis = new FileInputStream(f.getPath());
            br = new BufferedReader(new FileReader(fis.getFD()));
            char[] buf = new char[100];
            int r = br.read(buf);
            br.close();
            fis.close();
            assertTrue("Failed to write correct chars: "
                    + new String(buf, 0, r), new String(buf, 0, r)
                    .equals("Test String"));
        } catch (Exception e) {
            fail("Exception during Constructor test " + e.toString());
        }
    }

    /**
     * @tests java.io.FileWriter#FileWriter(java.lang.String)
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        method = "FileWriter",
        args = {java.lang.String.class}
    )     
    public void test_ConstructorLjava_lang_String() {
        // Test for method java.io.FileWriter(java.lang.String)
        try {
            fos = new FileOutputStream(f.getPath());
            fos.write("Test String".getBytes());
            fos.close();
            bw = new BufferedWriter(new FileWriter(f.getPath()));
            bw.write(" After test string", 0, 18);
            bw.close();
            br = new BufferedReader(new FileReader(f.getPath()));
            char[] buf = new char[100];
            int r = br.read(buf);
            br.close();
            assertEquals("Failed to write correct chars", " After test string", new String(buf, 0, r)
                    );
        } catch (Exception e) {
            fail("Exception during Constructor test " + e.toString());
        }
    }

    /**
     * @tests java.io.FileWriter#FileWriter(java.lang.String)
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Checks IOException.",
        method = "FileWriter",
        args = {java.lang.String.class}
    )      
    public void test_ConstructorLjava_lang_String_IOException() {
        try {
            fw = new FileWriter(System.getProperty("java.io.tmpdir"));
            fail("Test 1: IOException expected.");
        } catch (IOException e) {
            // Expected.
        }
    }

    /**
     * @tests java.io.FileWriter#FileWriter(java.lang.String, boolean)
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        method = "FileWriter",
        args = {java.lang.String.class, boolean.class}
    )       
    public void test_ConstructorLjava_lang_StringZ() {
        // Test for method java.io.FileWriter(java.lang.String, boolean)

        try {
            fos = new FileOutputStream(f.getPath());
            fos.write("Test String".getBytes());
            fos.close();
            bw = new BufferedWriter(new FileWriter(f.getPath(), true));
            bw.write(" After test string", 0, 18);
            bw.close();
            br = new BufferedReader(new FileReader(f.getPath()));
            char[] buf = new char[100];
            int r = br.read(buf);
            br.close();
            assertEquals("Failed to append to file", "Test String After test string", new String(buf, 0, r)
                    );

            fos = new FileOutputStream(f.getPath());
            fos.write("Test String".getBytes());
            fos.close();
            bw = new BufferedWriter(new FileWriter(f.getPath(), false));
            bw.write(" After test string", 0, 18);
            bw.close();
            br = new BufferedReader(new FileReader(f.getPath()));
            buf = new char[100];
            r = br.read(buf);
            br.close();
            assertEquals("Failed to overwrite file", " After test string", new String(buf, 0, r)
                    );
        } catch (Exception e) {
            fail("Exception during Constructor test " + e.toString());
        }

    }

    /**
     * @tests java.io.FileWriter#FileWriter(java.lang.String, boolean)
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Checks IOException.",
        method = "FileWriter",
        args = {java.lang.String.class, boolean.class}
    )      
    public void test_ConstructorLjava_lang_StringZ_IOException() {
        try {
            fw = new FileWriter(System.getProperty("java.io.tmpdir"), false);
            fail("Test 1: IOException expected.");
        } catch (IOException e) {
            // Expected.
        }
    }

    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "write",
        args = {char[].class, int.class, int.class}
    )
    public void test_handleEarlyEOFChar_1() {
        String str = "All work and no play makes Jack a dull boy\n"; //$NON-NLS-1$
        int NUMBER = 2048;
        int j = 0;
        int len = str.length() * NUMBER;
        /* == 88064 *//* NUMBER compulsively written copies of the same string */
        char[] strChars = new char[len];
        for (int i = 0; i < NUMBER; ++i) {
            for (int k = 0; k < str.length(); ++k) {
                strChars[j++] = str.charAt(k);
            }
        }
        File f = null;
        FileWriter fw = null;
        try {
            f = File.createTempFile("ony", "by_one");
            fw = new FileWriter(f);
            fw.write(strChars);
            fw.close();
            InputStreamReader in = null;
            FileInputStream fis = new FileInputStream(f);
            in = new InputStreamReader(fis);
            int b;
            int errors = 0;
            for (int offset = 0; offset < strChars.length; ++offset) {
                b = in.read();
                if (b == -1) {
                    fail("Early EOF at offset " + offset + "\n");
                    return;
                }
            }
            assertEquals(0, errors);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
          
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "write",
        args = {char[].class, int.class, int.class}
    )
    public void test_handleEarlyEOFChar_2() throws IOException {
        int capacity = 65536;
        byte[] bytes = new byte[capacity];
        byte[] bs = {
                'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H'
        };
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = bs[i / 8192];
        }
        String inputStr = new String(bytes);
        int len = inputStr.length();
        File f = File.createTempFile("FileWriterBugTest ", null); //$NON-NLS-1$
        FileWriter writer = new FileWriter(f);
        writer.write(inputStr);
        writer.close();
        long flen = f.length();

        FileReader reader = new FileReader(f);
        char[] outChars = new char[capacity];
        int outCount = reader.read(outChars);
        String outStr = new String(outChars, 0, outCount);

        f.deleteOnExit();
        assertEquals(len, flen);
        assertEquals(inputStr, outStr);
    }
          

    /**
     * Sets up the fixture, for example, open a network connection. This method
     * is called before a test is executed.
     */
    protected void setUp() throws Exception {

        f = File.createTempFile("writer", ".tst");

        if (f.exists())
            if (!f.delete()) {
                fail("Unable to delete test file");
            }
    }

    /**
     * Tears down the fixture, for example, close a network connection. This
     * method is called after a test is executed.
     */
    protected void tearDown() {
        try {
            bw.close();
        } catch (Exception e) {
        }
        try {
            fis.close();
        } catch (Exception e) {
        }
        f.delete();
    }
}
