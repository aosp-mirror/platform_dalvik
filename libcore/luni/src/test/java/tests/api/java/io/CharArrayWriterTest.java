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

import dalvik.annotation.TestInfo;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTarget;
import dalvik.annotation.TestTargetClass; 

import java.io.CharArrayReader;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.StringWriter;

@TestTargetClass(CharArrayWriter.class) 
public class CharArrayWriterTest extends junit.framework.TestCase {

    char[] hw = { 'H', 'e', 'l', 'l', 'o', 'W', 'o', 'r', 'l', 'd' };

    CharArrayWriter cw;

    CharArrayReader cr;

    /**
     * @tests java.io.CharArrayWriter#CharArrayWriter(int)
     */
    @TestInfo(
            level = TestLevel.PARTIAL,
            purpose = "IllegalArgumentException checking missed.",
            targets = { @TestTarget(methodName = "CharArrayWriter", 
                                    methodArgs = {int.class})                         
            }
    )    
    public void test_ConstructorI() {
        // Test for method java.io.CharArrayWriter(int)
        cw = new CharArrayWriter(90);
        assertEquals("Created incorrect writer", 0, cw.size());
    }

    /**
     * @tests java.io.CharArrayWriter#CharArrayWriter()
     */
    @TestInfo(
            level = TestLevel.COMPLETE,
            purpose = "Verifies CharArrayWriter() method.",
            targets = { @TestTarget(methodName = "CharArrayWriter", 
                                    methodArgs = {})                         
            }
    )       
    public void test_Constructor() {
        // Test for method java.io.CharArrayWriter()
        cw = new CharArrayWriter();
        assertEquals("Created incorrect writer", 0, cw.size());
    }

    /**
     * @tests java.io.CharArrayWriter#close()
     */
    @TestInfo(
            level = TestLevel.COMPLETE,
            purpose = "Verifies close() method.",
            targets = { @TestTarget(methodName = "close", 
                                    methodArgs = {})                         
            }
    )        
    public void test_close() {
        // Test for method void java.io.CharArrayWriter.close()
        cw.close();
    }

    /**
     * @tests java.io.CharArrayWriter#flush()
     */
    @TestInfo(
            level = TestLevel.COMPLETE,
            purpose = "Verifies flush() method.",
            targets = { @TestTarget(methodName = "flush", 
                                    methodArgs = {})                         
            }
    )    
    public void test_flush() {
        // Test for method void java.io.CharArrayWriter.flush()
        cw.flush();
    }

    /**
     * @tests java.io.CharArrayWriter#reset()
     */
    @TestInfo(
            level = TestLevel.COMPLETE,
            purpose = "Verifies reset() method.",
            targets = { @TestTarget(methodName = "reset", 
                                    methodArgs = {})                         
            }
    )      
    public void test_reset() {
        // Test for method void java.io.CharArrayWriter.reset()
        cw.write("HelloWorld", 5, 5);
        cw.reset();
        cw.write("HelloWorld", 0, 5);
        cr = new CharArrayReader(cw.toCharArray());
        try {
            char[] c = new char[100];
            cr.read(c, 0, 5);
            assertEquals("Reset failed to reset buffer",
                         "Hello", new String(c, 0, 5));
        } catch (IOException e) {
            fail("Exception during reset test : " + e.getMessage());
        }
    }

    /**
     * @tests java.io.CharArrayWriter#size()
     */
    @TestInfo(
            level = TestLevel.COMPLETE,
            purpose = "Verifies size() method.",
            targets = { @TestTarget(methodName = "size", 
                                    methodArgs = {})                         
            }
    )    
    public void test_size() {
        // Test for method int java.io.CharArrayWriter.size()
        assertEquals("Returned incorrect size", 0, cw.size());
        cw.write(hw, 5, 5);
        assertEquals("Returned incorrect size", 5, cw.size());
    }

    /**
     * @tests java.io.CharArrayWriter#toCharArray()
     */
    @TestInfo(
            level = TestLevel.COMPLETE,
            purpose = "Verifies toCharArray() method.",
            targets = { @TestTarget(methodName = "toCharArray", 
                                    methodArgs = {})                         
            }
    )     
    public void test_toCharArray() {
        // Test for method char [] java.io.CharArrayWriter.toCharArray()
        cw.write("HelloWorld", 0, 10);
        cr = new CharArrayReader(cw.toCharArray());
        try {
            char[] c = new char[100];
            cr.read(c, 0, 10);
            assertEquals("toCharArray failed to return correct array",
                         "HelloWorld", new String(c, 0, 10));
        } catch (IOException e) {
            fail("Exception during toCharArray test : " + e.getMessage());
        }
    }

    /**
     * @tests java.io.CharArrayWriter#toString()
     */
    @TestInfo(
            level = TestLevel.COMPLETE,
            purpose = "Verifies toString() method.",
            targets = { @TestTarget(methodName = "toString", 
                                    methodArgs = {})                         
            }
    )       
    public void test_toString() {
        // Test for method java.lang.String java.io.CharArrayWriter.toString()
        cw.write("HelloWorld", 5, 5);
        cr = new CharArrayReader(cw.toCharArray());
        assertEquals("Returned incorrect string",
                     "World", cw.toString());
    }

    /**
     * @tests java.io.CharArrayWriter#write(char[], int, int)
     */
    @TestInfo(
            level = TestLevel.COMPLETE,
            purpose = "Verifies write(char[] c, int off, int len) method.",
            targets = { @TestTarget(methodName = "write", 
                                    methodArgs = {char[].class, int.class, int.class})                         
            }
    )     
    public void test_write$CII() {
        // Test for method void java.io.CharArrayWriter.write(char [], int, int)
        cw.write(hw, 5, 5);
        cr = new CharArrayReader(cw.toCharArray());
        try {
            char[] c = new char[100];
            cr.read(c, 0, 5);
            assertEquals("Writer failed to write correct chars",
                         "World", new String(c, 0, 5));
        } catch (IOException e) {
            fail("Exception during write test : " + e.getMessage());
        }
    }

    /**
     * @tests java.io.CharArrayWriter#write(char[], int, int)
     * Regression for HARMONY-387
     */
    @TestInfo(
            level = TestLevel.PARTIAL,
            purpose = "Regression for write(char[] c, int off, int len) method.",
            targets = { @TestTarget(methodName = "write", 
                                    methodArgs = {char[].class, int.class, int.class})                         
            }
    )         
    public void test_write$CII_2() {
        CharArrayWriter obj = new CharArrayWriter();
        try {
            obj.write(new char []{'0'}, 0, -1);
            fail("IndexOutOfBoundsException expected");
        } catch (IndexOutOfBoundsException t) {
            assertEquals(
                    "IndexOutOfBoundsException rather than a subclass expected",
                    IndexOutOfBoundsException.class, t.getClass());
        }
    }

    /**
     * @tests java.io.CharArrayWriter#write(int)
     */
    @TestInfo(
            level = TestLevel.COMPLETE,
            purpose = "Verifies write(int) method.",
            targets = { @TestTarget(methodName = "write", 
                                    methodArgs = {int.class})                         
            }
    )    
    public void test_writeI() {
        // Test for method void java.io.CharArrayWriter.write(int)
        cw.write('T');
        cr = new CharArrayReader(cw.toCharArray());
        try {
            assertEquals("Writer failed to write char", 'T', cr.read());
        } catch (IOException e) {
            fail("Exception during write test : " + e.getMessage());
        }
    }

    /**
     * @tests java.io.CharArrayWriter#write(java.lang.String, int, int)
     */
    @TestInfo(
            level = TestLevel.COMPLETE,
            purpose = "Verifies write(java.lang.String, int, int) method. " +
                    "[Need to check different strings?]",
            targets = { @TestTarget(methodName = "write", 
                                    methodArgs = {java.lang.String.class, int.class, int.class})                         
            }
    )     
    public void test_writeLjava_lang_StringII() {
        // Test for method void java.io.CharArrayWriter.write(java.lang.String,
        // int, int)
        cw.write("HelloWorld", 5, 5);
        cr = new CharArrayReader(cw.toCharArray());
        try {
            char[] c = new char[100];
            cr.read(c, 0, 5);
            assertEquals("Writer failed to write correct chars",
                         "World", new String(c, 0, 5));
        } catch (IOException e) {
            fail("Exception during write test : " + e.getMessage());
        }
    }

    /**
     * @tests java.io.CharArrayWriter#write(java.lang.String, int, int)
     * Regression for HARMONY-387
     */
    @TestInfo(
            level = TestLevel.PARTIAL,
            purpose = "Regression for write(java.lang.String, int, int) method.",
            targets = { @TestTarget(methodName = "write", 
                                    methodArgs = {java.lang.String.class, int.class, int.class})                         
            }
    )         
    public void test_writeLjava_lang_StringII_2() throws StringIndexOutOfBoundsException {
        CharArrayWriter obj = new CharArrayWriter();
        try {
            obj.write((String) null, -1, 0);
            fail("NullPointerException expected");
        } catch (NullPointerException t) {
        }
    }

    /**
     * @tests java.io.CharArrayWriter#writeTo(java.io.Writer)
     */
    @TestInfo(
            level = TestLevel.PARTIAL,
            purpose = "IOException checking missed.",
            targets = { @TestTarget(methodName = "writeTo", 
                                    methodArgs = {java.io.Writer.class})                         
            }
    )         
    public void test_writeToLjava_io_Writer() {
        // Test for method void java.io.CharArrayWriter.writeTo(java.io.Writer)
        cw.write("HelloWorld", 0, 10);
        StringWriter sw = new StringWriter();
        try {
            cw.writeTo(sw);
            assertEquals("Writer failed to write correct chars",
                         "HelloWorld", sw.toString());
        } catch (IOException e) {
            fail("Exception during writeTo test : " + e.getMessage());
        }
    }

    /**
     * Sets up the fixture, for example, open a network connection. This method
     * is called before a test is executed.
     */
    protected void setUp() {
        cw = new CharArrayWriter();
    }

    /**
     * Tears down the fixture, for example, close a network connection. This
     * method is called after a test is executed.
     */
    protected void tearDown() {
        if (cr != null)
            cr.close();
        cw.close();
    }

    /**
     * @tests java.io.CharArrayWriter#append(char)
     */
    @TestInfo(
            level = TestLevel.COMPLETE,
            purpose = "Verifies append(char c) method.",
            targets = { @TestTarget(methodName = "append", 
                                    methodArgs = {char.class})                         
            }
    )      
    public void test_appendChar() throws IOException {
        char testChar = ' ';
        CharArrayWriter writer = new CharArrayWriter(10);
        writer.append(testChar);
        writer.flush();
        assertEquals(String.valueOf(testChar), writer.toString());
        writer.close();
    }

    /**
     * @tests java.io.CharArrayWriter#append(CharSequence)
     */
    @TestInfo(
            level = TestLevel.COMPLETE,
            purpose = "Verifies append(CharSequence csq) method.",
            targets = { @TestTarget(methodName = "append", 
                                    methodArgs = {java.lang.CharSequence.class})                         
            }
    )      
    public void test_appendCharSequence() {

        String testString = "My Test String";
        CharArrayWriter writer = new CharArrayWriter(10);
        writer.append(testString);
        writer.flush();
        assertEquals(testString, writer.toString());
        writer.close();
    }

    /**
     * @tests java.io.CharArrayWriter#append(CharSequence, int, int)
     */
    @TestInfo(
            level = TestLevel.PARTIAL,
            purpose = "IndexOutOfBoundsException checking missed.",
            targets = { @TestTarget(methodName = "append", 
                                    methodArgs = {java.lang.CharSequence.class, int.class, int.class})                         
            }
    )    
    public void test_appendCharSequenceIntInt() {
        String testString = "My Test String";
        CharArrayWriter writer = new CharArrayWriter(10);
        writer.append(testString, 1, 3);
        writer.flush();
        assertEquals(testString.substring(1, 3), writer.toString());
        writer.close();

    }

}
