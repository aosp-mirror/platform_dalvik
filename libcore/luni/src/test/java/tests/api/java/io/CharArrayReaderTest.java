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
import java.io.IOException;

@TestTargetClass(CharArrayReader.class) 
public class CharArrayReaderTest extends junit.framework.TestCase {

    char[] hw = { 'H', 'e', 'l', 'l', 'o', 'W', 'o', 'r', 'l', 'd' };

    CharArrayReader cr;

    /**
     * @tests java.io.CharArrayReader#CharArrayReader(char[])
     */
    @TestInfo(
            level = TestLevel.COMPLETE,
            purpose = "Verifies CharArrayReader(char[] buf) constructor.",
            targets = { @TestTarget(methodName = "CharArrayReader", 
                                    methodArgs = {char[].class})                         
            }
    )         
    public void test_Constructor$C() {
        // Test for method java.io.CharArrayReader(char [])

        try {
            cr = new CharArrayReader(hw);
            assertTrue("Failed to create reader", cr.ready());
        } catch (IOException e) {
            fail("Exception determining ready state : " + e.getMessage());
        }
    }

    /**
     * @tests java.io.CharArrayReader#CharArrayReader(char[], int, int)
     */
    @TestInfo(
            level = TestLevel.PARTIAL,
            purpose = "IllegalArgumentException checking missed.",
            targets = { @TestTarget(methodName = "CharArrayReader", 
                                    methodArgs = {char[].class, int.class, int.class})                         
            }
    )         
    public void test_Constructor$CII() {
        // Test for method java.io.CharArrayReader(char [], int, int)
        try {
            cr = new CharArrayReader(hw, 5, 5);
            assertTrue("Failed to create reader", cr.ready());
        } catch (IOException e) {
            fail("Exception determining ready state : " + e.getMessage());
        }
        try {
            int c = cr.read();
            assertTrue("Created incorrect reader--returned '" + (char) c
                    + "' intsead of 'W'", c == 'W');
        } catch (IOException e) {
            fail("Exception reading from new reader : " + e.getMessage());
        }
    }

    /**
     * @tests java.io.CharArrayReader#close()
     */
    @TestInfo(
            level = TestLevel.COMPLETE,
            purpose = "Verifies close() method.",
            targets = { @TestTarget(methodName = "close", 
                                    methodArgs = {})                         
            }
    )     
    public void test_close() {
        // Test for method void java.io.CharArrayReader.close()
        cr = new CharArrayReader(hw);
        cr.close();
        try {
            cr.read();
            fail("Failed to throw exception on reqad from closed stream");            
        } catch (IOException e) { // Correct
        }

    }

    /**
     * @tests java.io.CharArrayReader#mark(int)
     */
    @TestInfo(
            level = TestLevel.PARTIAL,
            purpose = "IOException checking missed.",
            targets = { @TestTarget(methodName = "mark", 
                                    methodArgs = { int.class })                         
            }
    )     
    public void test_markI() {
        // Test for method void java.io.CharArrayReader.mark(int)
        try {
            cr = new CharArrayReader(hw);
            cr.skip(5L);
            cr.mark(100);
            cr.read();
            cr.reset();
            assertEquals("Failed to mark correct position", 'W', cr.read());
        } catch (IOException e) {
            fail("Exception during mark test: " + e.getMessage());
        }
    }

    /**
     * @tests java.io.CharArrayReader#markSupported()
     */
    @TestInfo(
            level = TestLevel.COMPLETE,
            purpose = "Verifies markSupported() method.",
            targets = { @TestTarget(methodName = "markSupported", 
                                    methodArgs = {})                         
            }
    )       
    public void test_markSupported() {
        // Test for method boolean java.io.CharArrayReader.markSupported()
        cr = new CharArrayReader(hw);
        assertTrue("markSupported returned false", cr.markSupported());
    }

    /**
     * @tests java.io.CharArrayReader#read()
     */
    @TestInfo(
            level = TestLevel.PARTIAL,
            purpose = "IOException checking missed.",
            targets = { @TestTarget(methodName = "read", 
                                    methodArgs = {})                         
            }
    )        
    public void test_read() {
        // Test for method int java.io.CharArrayReader.read()
        try {
            cr = new CharArrayReader(hw);
            assertEquals("Read returned incorrect char", 'H', cr.read());
            cr = new CharArrayReader(new char[] { '\u8765' });
            assertTrue("Incorrect double byte char", cr.read() == '\u8765');
        } catch (IOException e) {
            fail("Exception during read test: " + e.getMessage());
        }
    }

    /**
     * @tests java.io.CharArrayReader#read(char[], int, int)
     */
    @TestInfo(
            level = TestLevel.PARTIAL,
            purpose = "IOException checking missed.",
            targets = { @TestTarget(methodName = "read", 
                                    methodArgs = {char[].class, int.class, int.class})                         
            }
    )        
    public void test_read$CII() {
        // Test for method int java.io.CharArrayReader.read(char [], int, int)
        char[] c = new char[11];
        try {
            cr = new CharArrayReader(hw);
            cr.read(c, 1, 10);
            assertTrue("Read returned incorrect chars", new String(c, 1, 10)
                    .equals(new String(hw, 0, 10)));
        } catch (IOException e) {
            fail("Exception during read test : " + e.getMessage());
        }
    }

    /**
     * @tests java.io.CharArrayReader#ready()
     */
    @TestInfo(
            level = TestLevel.COMPLETE,
            purpose = "Verifies ready() method.",
            targets = { @TestTarget(methodName = "ready", 
                                    methodArgs = {})                         
            }
    )         
    public void test_ready() {
        // Test for method boolean java.io.CharArrayReader.ready()
        cr = new CharArrayReader(hw);
        boolean expectException = false;
        try {
            assertTrue("ready returned false", cr.ready());
            cr.skip(1000);
            assertTrue("ready returned true", !cr.ready());
            cr.close();
            expectException = true;
            cr.ready();
            fail("No exception 1");
        } catch (IOException e) {
            if (!expectException)
                fail("Unexpected: " + e);
        }
        try {
            cr = new CharArrayReader(hw);
            cr.close();
            cr.ready();
            fail("No exception 2");
        } catch (IOException e) {
        }

    }

    /**
     * @tests java.io.CharArrayReader#reset()
     */
    @TestInfo(
            level = TestLevel.PARTIAL,
            purpose = "IOException checking missed.",
            targets = { @TestTarget(methodName = "reset", 
                                    methodArgs = {})                         
            }
    )        
    public void test_reset() {
        // Test for method void java.io.CharArrayReader.reset()
        try {
            cr = new CharArrayReader(hw);
            cr.skip(5L);
            cr.mark(100);
            cr.read();
            cr.reset();
            assertEquals("Reset failed to return to marker position",
                    'W', cr.read());
        } catch (IOException e) {
            fail("Exception during reset test : " + e.getMessage());
        }
    }

    /**
     * @tests java.io.CharArrayReader#skip(long)
     */
    @TestInfo(
            level = TestLevel.PARTIAL,
            purpose = "IOException checking missed.",
            targets = { @TestTarget(methodName = "skip", 
                                    methodArgs = {long.class})                         
            }
    )         
    public void test_skipJ() {
        // Test for method long java.io.CharArrayReader.skip(long)
        long skipped = 0;
        try {
            cr = new CharArrayReader(hw);
            skipped = cr.skip(5L);
        } catch (IOException e) {
            fail("Exception during skip test : " + e.getMessage());
        }
        assertEquals("Failed to skip correct number of chars", 5L, skipped);
        try {
            assertEquals("Skip skipped wrong chars", 'W', cr.read());
        } catch (IOException e) {
            fail("read exception during skip test : " + e.getMessage());
        }
    }

    /**
     * Sets up the fixture, for example, open a network connection. This method
     * is called before a test is executed.
     */
    protected void setUp() {
    }

    /**
     * Tears down the fixture, for example, close a network connection. This
     * method is called after a test is executed.
     */
    protected void tearDown() {
        if (cr != null)
            cr.close();
    }
}
