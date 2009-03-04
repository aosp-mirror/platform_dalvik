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

import dalvik.annotation.TestTargets;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargetClass; 

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

import tests.support.Support_OutputStream;

import junit.framework.TestCase;

/**
 * 
 */
@TestTargetClass(OutputStreamWriter.class) 
public class OutputStreamWriterTest extends TestCase {

    private static final int UPPER = 0xd800;

    private static final int BUFFER_SIZE = 10000;

    static private final String[] MINIMAL_CHARSETS = new String[] { "US-ASCII",
            "ISO-8859-1", "UTF-16BE", "UTF-16LE", "UTF-16", "UTF-8" };

    OutputStreamWriter osw;

    InputStreamReader isr;

    private Support_OutputStream fos;

    public String testString = "This is a test message with Unicode characters. \u4e2d\u56fd is China's name in Chinese";

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        fos = new Support_OutputStream(500);
        osw = new OutputStreamWriter(fos, "UTF-8");
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        try {
            if (isr != null) isr.close();
            osw.close();
        } catch (Exception e) {
        }

        super.tearDown();
    }
    
    /**
     * @tests java.io.OutputStreamWriter#OutputStreamWriter(java.io.OutputStream)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "OutputStreamWriter",
        args = {java.io.OutputStream.class}
    )
    public void test_ConstructorLjava_io_OutputStream() throws IOException {
        OutputStreamWriter writer = null;
        
        try {
            writer = new OutputStreamWriter(null);
            fail("Test 1: NullPointerException expected.");
        } catch (NullPointerException e) {
            // Expected
        }
        
        try {
            writer = new OutputStreamWriter(new Support_OutputStream());
        } catch (Exception e) {
            fail("Test 2: Unexpected exception: " + e.getMessage());
        }
        
        // Test that the default encoding has been used.
        assertEquals("Test 3: Incorrect default encoding used.",
                     Charset.defaultCharset(),
                     Charset.forName(writer.getEncoding()));

        if (writer != null) writer.close();
    }

    /**
     * @tests java.io.OutputStreamWriter#OutputStreamWriter(java.io.OutputStream,
     *        java.lang.String)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "OutputStreamWriter",
        args = {java.io.OutputStream.class, java.lang.String.class}
    )
    public void test_ConstructorLjava_io_OutputStreamLjava_lang_String() 
            throws UnsupportedEncodingException {

        try {
            osw = new OutputStreamWriter(null, "utf-8");
            fail("Test 1: NullPointerException expected.");
        } catch (NullPointerException e) {
            // Expected
        }

        try {
            osw = new OutputStreamWriter(fos, (String) null);
            fail("Test 2: NullPointerException expected.");
        } catch (NullPointerException e) {
            // Expected
        }

        try {
            osw = new OutputStreamWriter(fos, "");
            fail("Test 3: UnsupportedEncodingException expected.");
        } catch (UnsupportedEncodingException e) {
            // Expected
        }

        try {
            osw = new OutputStreamWriter(fos, "Bogus");
            fail("Test 4: UnsupportedEncodingException expected.");
        } catch (UnsupportedEncodingException e) {
            // Expected
        }

        try {
            osw = new OutputStreamWriter(fos, "8859_1");
        } catch (UnsupportedEncodingException e) {
            fail("Test 5: Unexpected UnsupportedEncodingException.");
        }

        assertEquals("Test 6: Encoding not set correctly. ", 
                     Charset.forName("8859_1"), 
                     Charset.forName(osw.getEncoding()));
    }
    
    /**
     * @tests java.io.OutputStreamWriter#OutputStreamWriter(java.io.OutputStream,
     *        java.nio.charset.Charset)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "OutputStreamWriter",
        args = {java.io.OutputStream.class, java.nio.charset.Charset.class}
    )
    public void test_ConstructorLjava_io_OutputStreamLjava_nio_charset_Charset() 
            throws IOException {
        OutputStreamWriter writer;
        Support_OutputStream out = new Support_OutputStream();
        Charset cs = Charset.forName("ascii");
        
        try {
            writer = new OutputStreamWriter(null, cs);
            fail("Test 1: NullPointerException expected.");
        } catch (NullPointerException e) {
            // Expected
        }
        
        try {
            writer = new OutputStreamWriter(out, (Charset) null);
            fail("Test 2: NullPointerException expected.");
        } catch (NullPointerException e) {
            // Expected
        }
        
        writer = new OutputStreamWriter(out, cs);
        assertEquals("Test 3: Encoding not set correctly. ",
                     cs, Charset.forName(writer.getEncoding()));
        writer.close();
    }

    /**
     * @tests java.io.OutputStreamWriter#OutputStreamWriter(java.io.OutputStream,
     *        java.nio.charset.CharsetEncoder)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "OutputStreamWriter",
        args = {java.io.OutputStream.class, java.nio.charset.CharsetEncoder.class}
    )
    public void test_ConstructorLjava_io_OutputStreamLjava_nio_charset_CharsetEncoder() 
            throws IOException {
        OutputStreamWriter writer;
        Support_OutputStream out = new Support_OutputStream();
        Charset cs = Charset.forName("ascii");
        CharsetEncoder enc = cs.newEncoder();
        
        try {
            writer = new OutputStreamWriter(null, enc);
            fail("Test 1: NullPointerException expected.");
        } catch (NullPointerException e) {
            // Expected
        }
        
        try {
            writer = new OutputStreamWriter(out, (CharsetEncoder) null);
            fail("Test 2: NullPointerException expected.");
        } catch (NullPointerException e) {
            // Expected
        }
        
        writer = new OutputStreamWriter(out, cs);
        assertEquals("Test 3: CharacterEncoder not set correctly. ",
                     cs, Charset.forName(writer.getEncoding()));
        writer.close();
    }
    
    /**
     * @tests java.io.OutputStreamWriter#close()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "An issue in the API code has been identified (ticket #87). This test must be updated when the ticket is closed.",
        method = "close",
        args = {}
    )
    public void test_close() {
        
        fos.setThrowsException(true);
        try {
            osw.close();
            fail("Test 1: IOException expected.");
        } catch (IOException e) {
            // Expected.
        }
        
/* Test 2 does not work and has therefore been disabled (see Ticket #87).        
        // Test 2: Write should not fail since the closing
        // in test 1 has not been successful.
        try {
            osw.write("Lorem ipsum...");
        } catch (IOException e) {
            fail("Test 2: Unexpected IOException.");
        }
        
        // Test 3: Close should succeed.
        fos.setThrowsException(false);
        try {
            osw.close();
        } catch (IOException e) {
            fail("Test 3: Unexpected IOException.");
        }
*/ 
        
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        try {
            OutputStreamWriter writer = new OutputStreamWriter(bout,
                    "ISO2022JP");
            writer.write(new char[] { 'a' });
            writer.close();
            // The default is ASCII, there should not be any mode changes.
            String converted = new String(bout.toByteArray(), "ISO8859_1");
            assertTrue("Test 4: Invalid conversion: " + converted, 
                       converted.equals("a"));

            bout.reset();
            writer = new OutputStreamWriter(bout, "ISO2022JP");
            writer.write(new char[] { '\u3048' });
            writer.flush();
            // The byte sequence should not switch to ASCII mode until the
            // stream is closed.
            converted = new String(bout.toByteArray(), "ISO8859_1");
            assertTrue("Test 5: Invalid conversion: " + converted, 
                       converted.equals("\u001b$B$("));
            writer.close();
            converted = new String(bout.toByteArray(), "ISO8859_1");
            assertTrue("Test 6: Invalid conversion: " + converted, 
                       converted.equals("\u001b$B$(\u001b(B"));

            bout.reset();
            writer = new OutputStreamWriter(bout, "ISO2022JP");
            writer.write(new char[] { '\u3048' });
            writer.write(new char[] { '\u3048' });
            writer.close();
            // There should not be a mode switch between writes.
            assertEquals("Test 7: Invalid conversion. ", 
                         "\u001b$B$($(\u001b(B", 
                         new String(bout.toByteArray(), "ISO8859_1"));
        } catch (UnsupportedEncodingException e) {
            // Can't test missing converter.
            System.out.println(e);
        } catch (IOException e) {
            fail("Unexpected: " + e);
        }
    }

    /**
     * @tests java.io.OutputStreamWriter#flush()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "flush",
        args = {}
    )
    public void test_flush() {
        // Test for method void java.io.OutputStreamWriter.flush()
        try {
            char[] buf = new char[testString.length()];
            osw.write(testString, 0, testString.length());
            osw.flush();
            openInputStream();
            isr.read(buf, 0, buf.length);
            assertTrue("Test 1: Characters have not been flushed.", 
                       new String(buf, 0, buf.length).equals(testString));
        } catch (Exception e) {
            fail("Test 1: Unexpected exception: " + e.getMessage());
        }
        
        fos.setThrowsException(true);
        try {
            osw.flush();
            fail("Test 2: IOException expected.");
        } catch (IOException e) {
            // Expected
        }
        fos.setThrowsException(false);
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "write",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "read",
            args = {},
            clazz = InputStreamReader.class
        )
    }) 
    public void test_singleCharIO() throws Exception {
        int upper;
        OutputStreamWriter writer = null;
        ByteArrayOutputStream out;
        InputStreamReader isr = null;
        
        for (int i = 0; i < MINIMAL_CHARSETS.length; ++i) {
            try {
                out = new ByteArrayOutputStream();
                writer = new OutputStreamWriter(out, MINIMAL_CHARSETS[i]);

                switch (i) {
                case 0:
                    upper = 128;
                    break;
                case 1:
                    upper = 256;
                    break;
                default:
                    upper = UPPER;
                }

                for (int c = 0; c < upper; ++c) {
                    writer.write(c);
                }
                writer.flush();
                byte[] result = out.toByteArray();

                isr = new InputStreamReader(new ByteArrayInputStream(result),
                        MINIMAL_CHARSETS[i]);
                for (int expected = 0; expected < upper; ++expected) {
                    assertEquals("Error when reading bytes in "
                            + MINIMAL_CHARSETS[i], expected, isr.read());
                }
            } finally {
                try {
                    if (isr != null) isr.close();
                } catch (Exception e) {
                }
                try {
                    if (writer != null) writer.close();
                } catch (Exception e) {
                }
            }
        }
    }

    @TestTargets({
        @TestTargetNew(
                level = TestLevel.PARTIAL_COMPLETE,
                method = "write",
                args = {char[].class}
        ),
        @TestTargetNew(
                level = TestLevel.PARTIAL_COMPLETE,
                notes = "",
                method = "read",
                args = {},
                clazz = InputStreamReader.class
        )
    })    
    public void test_write$C() throws Exception {
        int upper;
        InputStreamReader isr = null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStreamWriter writer = null;
        
        char[] largeBuffer = new char[BUFFER_SIZE];
        for (int i = 0; i < MINIMAL_CHARSETS.length; ++i) {
            try {
                baos = new ByteArrayOutputStream();
                writer = new OutputStreamWriter(baos, MINIMAL_CHARSETS[i]);

                switch (i) {
                case 0:
                    upper = 128;
                    break;
                case 1:
                    upper = 256;
                    break;
                default:
                    upper = UPPER;
                }

                int m = 0;
                for (int c = 0; c < upper; ++c) {
                    largeBuffer[m++] = (char) c;
                    if (m == BUFFER_SIZE) {
                        writer.write(largeBuffer);
                        m = 0;
                    }
                }
                writer.write(largeBuffer, 0, m);
                writer.flush();
                byte[] result = baos.toByteArray();

                isr = new InputStreamReader(new ByteArrayInputStream(result),
                        MINIMAL_CHARSETS[i]);
                int expected = 0, read = 0, j = 0;
                while (expected < upper) {
                    if (j == read) {
                        read = isr.read(largeBuffer);
                        j = 0;
                    }
                    assertEquals("Error when reading bytes in "
                            + MINIMAL_CHARSETS[i], expected++, largeBuffer[j++]);
                }
            } finally {
                try {
                    if (isr != null) isr.close();
                } catch (Exception e) {
                }
                try {
                    if (writer != null) writer.close();
                } catch (Exception e) {
                }
            }
        }
    }

    /**
     * @tests java.io.OutputStreamWriter#getEncoding()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getEncoding",
        args = {}
    )
    public void test_getEncoding() throws IOException {
        OutputStreamWriter writer;
        writer = new OutputStreamWriter(new Support_OutputStream(), "utf-8");
        assertEquals("Test 1: Incorrect encoding returned.", 
                     Charset.forName("utf-8"), 
                     Charset.forName(writer.getEncoding()));
        
        writer.close();
        assertNull("Test 2: getEncoding() did not return null for a closed writer.",
                   writer.getEncoding());
    }
    
    /**
     * @tests java.io.OutputStreamWriter#write(char[], int, int)
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "write",
        args = {char[].class, int.class, int.class}
    )
    public void test_write$CII() throws IOException {
        char[] chars = testString.toCharArray();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Support_OutputStream out = new Support_OutputStream(500);
        OutputStreamWriter writer;

        writer = new OutputStreamWriter(out, "utf-8");
         
        try {
            writer.write(chars, -1, 1);
            fail("Test 1: IndexOutOfBoundsException expected.");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        }
        
        try {
            writer.write(chars, 0, -1);
            fail("Test 2: IndexOutOfBoundsException expected.");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        }
        
        try {
            writer.write(new char[0], 0, 1);
            fail("Test 3: IndexOutOfBoundsException expected.");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        }

        try {
            writer.write((char[]) null, 0, 1);
            fail("Test 4: NullPointerException expected.");
        } catch (NullPointerException e) {
            // Expected
        }

        try {
            writer.write(chars, 1, chars.length);
            fail("Test 5a: IndexOutOfBoundsException expected.");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        }
        try {
            writer.write(chars, 0, chars.length + 1);
            fail("Test 5b: IndexOutOfBoundsException expected.");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        }
        try {
            writer.write(chars, chars.length, 1);
            fail("Test 5c: IndexOutOfBoundsException expected.");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        }
        try {
            writer.write(chars, chars.length + 1, 0);
            fail("Test 5d: IndexOutOfBoundsException expected.");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        }
    
        out.setThrowsException(true);
        try {
            for (int i = 0; i < 200; i++) {
                writer.write(chars, 0, chars.length);
            }
            fail("Test 6: IOException expected.");
        } catch (IOException e) {
            // Expected
        }
        out.setThrowsException(false);

        writer.close();
        writer = new OutputStreamWriter(baos, "utf-8");
        writer.write(chars, 1, 2);
        writer.flush();
        assertEquals("Test 7: write(char[], int, int) has not produced the " + 
                     "expected content in the output stream.",
                     "hi", baos.toString("utf-8"));

        writer.write(chars, 0, chars.length);
        writer.flush();
        assertEquals("Test 8: write(char[], int, int) has not produced the " + 
                "expected content in the output stream.",
                "hi" + testString, baos.toString("utf-8"));
            
        writer.close();
        try {
            writer.write((char[]) null, -1, -1);
            fail("Test 9: IOException expected.");
        } catch (IOException e) {
            // Expected
        }
    }    
    
    /**
     * @tests java.io.OutputStreamWriter#write(int)
     */    
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "write",
        args = {int.class}
    )
    public void test_writeI() throws IOException {
        Support_OutputStream out = new Support_OutputStream(500);
        OutputStreamWriter writer;

        out.setThrowsException(true);
        writer = new OutputStreamWriter(out, "utf-8");
        try {
            // Since there is an internal buffer in the encoder, more than
            // one character needs to be written.
            for (int i = 0; i < 200; i++) {
                for (int j = 0; j < testString.length(); j++) {
                    writer.write(testString.charAt(j));
                }
            }   
            fail("Test 1: IOException expected.");
        } catch (IOException e) {
            // Expected
        }
        out.setThrowsException(false);
        writer.close();
        
        writer = new OutputStreamWriter(out, "utf-8");
        writer.write(1);
        writer.flush();
        String str = new String(out.toByteArray(), "utf-8");
        assertEquals("Test 2: ", "\u0001", str);

        writer.write(2);
        writer.flush();
        str = new String(out.toByteArray(), "utf-8");
        assertEquals("Test 3: ", "\u0001\u0002", str);

        writer.write(-1);
        writer.flush();
        str = new String(out.toByteArray(), "utf-8");
        assertEquals("Test 4: ", "\u0001\u0002\uffff", str);

        writer.write(0xfedcb);
        writer.flush();
        str = new String(out.toByteArray(), "utf-8");
        assertEquals("Test 5: ", "\u0001\u0002\uffff\uedcb", str);
        
        writer.close();
        try {
            writer.write(1);
            fail("Test 6: IOException expected.");
        } catch (IOException e) {
            // Expected
        }
    }

    /**
     * @tests java.io.OutputStreamWriter#write(java.lang.String, int, int)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "write",
        args = {java.lang.String.class, int.class, int.class}
    )
    public void test_writeLjava_lang_StringII() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Support_OutputStream out = new Support_OutputStream(500);
        OutputStreamWriter writer;

        writer = new OutputStreamWriter(out, "utf-8");

        try {
            writer.write("Lorem", -1, 0);
            fail("Test 1: IndexOutOfBoundsException expected.");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        }
        
        try {
            writer.write("Lorem", 0, -1);
            fail("Test 2: IndexOutOfBoundsException expected.");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        }
        
        try {
            writer.write("", 0, 1);
            fail("Test 3: IndexOutOfBoundsException expected.");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        }

        try {
            writer.write(testString, 1, testString.length());
            fail("Test 4a: IndexOutOfBoundsException expected.");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        }

        try {
            writer.write(testString, 0, testString.length() + 1);
            fail("Test 4b: IndexOutOfBoundsException expected.");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        }

        try {
            writer.write(testString, testString.length(), 1);
            fail("Test 4c: IndexOutOfBoundsException expected.");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        }

        try {
            writer.write(testString, testString.length() + 1, 0);
            fail("Test 4d: IndexOutOfBoundsException expected.");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        }

        try {
            writer.write((String) null, 0, 1);
            fail("Test 5: NullPointerException expected.");
        } catch (NullPointerException e) {
            // Expected
        }
    
        out.setThrowsException(true);
        try {
            for (int i = 0; i < 200; i++) {
                writer.write(testString, 0, testString.length());
            }
            fail("Test 6: IOException expected.");
        } catch (IOException e) {
            // Expected
        }
        out.setThrowsException(false);
        
        writer.close();
        writer = new OutputStreamWriter(baos, "utf-8");
        
        writer.write("abc", 1, 2);
        writer.flush();
        assertEquals("Test 7: write(String, int, int) has not produced the " + 
                     "expected content in the output stream.",
                     "bc", baos.toString("utf-8"));
        
        writer.write(testString, 0, testString.length());
        writer.flush();
        assertEquals("Test 7: write(String, int, int) has not produced the " + 
                     "expected content in the output stream.",
                     "bc" + testString, baos.toString("utf-8"));
        
        writer.close();
        try {
            writer.write("abc", 0, 1);
            fail("Test 8: IOException expected.");
        } catch (IOException e) {
            // Expected
        }
    }

    private void openInputStream() {
        try {
            isr = new InputStreamReader(new ByteArrayInputStream(fos.toByteArray()), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            fail("UTF-8 not supported");
        }
    }

}
