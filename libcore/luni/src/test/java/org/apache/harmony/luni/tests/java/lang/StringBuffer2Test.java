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

package org.apache.harmony.luni.tests.java.lang;

import dalvik.annotation.KnownFailure;
import dalvik.annotation.TestTargets;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargetClass;

@TestTargetClass(StringBuffer.class) 
public class StringBuffer2Test extends junit.framework.TestCase {

    StringBuffer testBuffer;

    /**
     * @tests java.lang.StringBuffer#StringBuffer()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "StringBuffer",
        args = {}
    )
    public void test_Constructor() {
        // Test for method java.lang.StringBuffer()
        new StringBuffer();
        assertTrue("Invalid buffer created", true);
    }

    /**
     * @tests java.lang.StringBuffer#StringBuffer(int)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "StringBuffer",
        args = {int.class}
    )
    public void test_ConstructorI() {
        // Test for method java.lang.StringBuffer(int)
        StringBuffer sb = new StringBuffer(8);
        assertEquals("Newly constructed buffer is of incorrect length", 0, sb
                .length());
    }

    /**
     * @tests java.lang.StringBuffer#StringBuffer(java.lang.String)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "StringBuffer",
        args = {java.lang.String.class}
    )
    public void test_ConstructorLjava_lang_String() {
        // Test for method java.lang.StringBuffer(java.lang.String)

        StringBuffer sb = new StringBuffer("HelloWorld");

        assertTrue("Invalid buffer created", sb.length() == 10
                && (sb.toString().equals("HelloWorld")));

        boolean pass = false;
        try {
            new StringBuffer(null);
        } catch (NullPointerException e) {
            pass = true;
        }
        assertTrue("Should throw NullPointerException", pass);
    }

    /**
     * @tests java.lang.StringBuffer#append(char[])
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "append",
        args = {char[].class}
    )
    public void test_append$C() {
        // Test for method java.lang.StringBuffer
        // java.lang.StringBuffer.append(char [])
        char buf[] = new char[4];
        "char".getChars(0, 4, buf, 0);
        testBuffer.append(buf);
        assertEquals("Append of char[] failed", 
                "This is a test bufferchar", testBuffer.toString());
    }

    /**
     * @tests java.lang.StringBuffer#append(char[], int, int)
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "append",
        args = {char[].class, int.class, int.class}
    )
    public void test_append$CII() {
        // Test for method java.lang.StringBuffer
        // java.lang.StringBuffer.append(char [], int, int)
        StringBuffer sb = new StringBuffer();
        char[] buf1 = { 'H', 'e', 'l', 'l', 'o' };
        char[] buf2 = { 'W', 'o', 'r', 'l', 'd' };
        sb.append(buf1, 0, buf1.length);
        assertEquals("Buffer is invalid length after append", 5, sb.length());
        sb.append(buf2, 0, buf2.length);
        assertEquals("Buffer is invalid length after append", 10, sb.length());
        assertTrue("Buffer contains invalid chars", (sb.toString()
                .equals("HelloWorld")));
    }

    /**
     * @tests java.lang.StringBuffer#append(char)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "append",
        args = {char.class}
    )
    public void test_appendC() {
        // Test for method java.lang.StringBuffer
        // java.lang.StringBuffer.append(char)
        StringBuffer sb = new StringBuffer();
        char buf1 = 'H';
        char buf2 = 'W';
        sb.append(buf1);
        assertEquals("Buffer is invalid length after append", 1, sb.length());
        sb.append(buf2);
        assertEquals("Buffer is invalid length after append", 2, sb.length());
        assertTrue("Buffer contains invalid chars",
                (sb.toString().equals("HW")));
    }

    /**
     * @tests java.lang.StringBuffer#append(double)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "append",
        args = {double.class}
    )
    public void test_appendD() {
        // Test for method java.lang.StringBuffer
        // java.lang.StringBuffer.append(double)
        StringBuffer sb = new StringBuffer();
        sb.append(Double.MAX_VALUE);
        assertEquals("Buffer is invalid length after append", 22, sb.length());
        assertEquals("Buffer contains invalid characters", 
                "1.7976931348623157E308", sb.toString());
    }

    /**
     * @tests java.lang.StringBuffer#append(float)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "append",
        args = {float.class}
    )
    public void test_appendF() {
        // Test for method java.lang.StringBuffer
        // java.lang.StringBuffer.append(float)
        StringBuffer sb = new StringBuffer();
        final float floatNum = 900.87654F;
        sb.append(floatNum);
        assertTrue("Buffer is invalid length after append: " + sb.length(), sb
                .length() == String.valueOf(floatNum).length());
        assertTrue("Buffer contains invalid characters", sb.toString().equals(
                String.valueOf(floatNum)));
    }

    /**
     * @tests java.lang.StringBuffer#append(int)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "append",
        args = {int.class}
    )
    public void test_appendI() {
        // Test for method java.lang.StringBuffer
        // java.lang.StringBuffer.append(int)
        StringBuffer sb = new StringBuffer();
        sb.append(9000);
        assertEquals("Buffer is invalid length after append", 4, sb.length());
        sb.append(1000);
        assertEquals("Buffer is invalid length after append", 8, sb.length());
        assertEquals("Buffer contains invalid characters", 
                "90001000", sb.toString());
    }

    /**
     * @tests java.lang.StringBuffer#append(long)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "append",
        args = {long.class}
    )
    public void test_appendJ() {
        // Test for method java.lang.StringBuffer
        // java.lang.StringBuffer.append(long)

        StringBuffer sb = new StringBuffer();
        long t = 927654321098L;
        sb.append(t);
        assertEquals("Buffer is of invlaid length", 12, sb.length());
        assertEquals("Buffer contains invalid characters", 
                "927654321098", sb.toString());
    }

    /**
     * @tests java.lang.StringBuffer#append(java.lang.Object)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "append",
        args = {java.lang.Object.class}
    )
    public void test_appendLjava_lang_Object() {
        // Test for method java.lang.StringBuffer
        // java.lang.StringBuffer.append(java.lang.Object)
        StringBuffer sb = new StringBuffer();
        Object obj1 = new Object();
        Object obj2 = new Object();
        sb.append(obj1);
        sb.append(obj2);
        assertTrue("Buffer contains invalid characters", sb.toString().equals(
                obj1.toString() + obj2.toString()));
    }

    /**
     * @tests java.lang.StringBuffer#append(java.lang.String)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "append",
        args = {java.lang.String.class}
    )
    public void test_appendLjava_lang_String() {
        // Test for method java.lang.StringBuffer
        // java.lang.StringBuffer.append(java.lang.String)
        StringBuffer sb = new StringBuffer();
        String buf1 = "Hello";
        String buf2 = "World";
        sb.append(buf1);
        assertEquals("Buffer is invalid length after append", 5, sb.length());
        sb.append(buf2);
        assertEquals("Buffer is invalid length after append", 10, sb.length());
        assertTrue("Buffer contains invalid chars", (sb.toString()
                .equals("HelloWorld")));
    }

    /**
     * @tests java.lang.StringBuffer#append(boolean)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "append",
        args = {boolean.class}
    )
    public void test_appendZ() {
        // Test for method java.lang.StringBuffer
        // java.lang.StringBuffer.append(boolean)
        StringBuffer sb = new StringBuffer();
        sb.append(false);
        assertEquals("Buffer is invalid length after append", 5, sb.length());
        sb.append(true);
        assertEquals("Buffer is invalid length after append", 9, sb.length());
        assertTrue("Buffer is invalid length after append", (sb.toString()
                .equals("falsetrue")));
    }

    /**
     * @tests java.lang.StringBuffer#capacity()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "capacity",
        args = {}
    )
    public void test_capacity() {
        // Test for method int java.lang.StringBuffer.capacity()
        StringBuffer sb = new StringBuffer(10);
        assertEquals("Returned incorrect capacity", 10, sb.capacity());
        sb.ensureCapacity(100);
        assertTrue("Returned incorrect capacity", sb.capacity() >= 100);
    }

    /**
     * @tests java.lang.StringBuffer#charAt(int)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "charAt",
        args = {int.class}
    )
    public void test_charAtI() {
        // Test for method char java.lang.StringBuffer.charAt(int)
        assertEquals("Returned incorrect char", 's', testBuffer.charAt(3));

        // Test for StringIndexOutOfBoundsException
        boolean exception = false;
        try {
            testBuffer.charAt(-1);
        } catch (StringIndexOutOfBoundsException e) {
            exception = true;
        } catch (ArrayIndexOutOfBoundsException e) {
        }
        assertTrue("Should throw StringIndexOutOfBoundsException", exception);
    }

    /**
     * @tests java.lang.StringBuffer#delete(int, int)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "delete",
        args = {int.class, int.class}
    )
    public void test_deleteII() {
        // Test for method java.lang.StringBuffer
        // java.lang.StringBuffer.delete(int, int)
        testBuffer.delete(7, 7);
        assertEquals("Deleted chars when start == end", "This is a test buffer", testBuffer.toString()
                );
        testBuffer.delete(4, 14);
        assertEquals("Deleted incorrect chars", 
                "This buffer", testBuffer.toString());

        testBuffer = new StringBuffer("This is a test buffer");
        String sharedStr = testBuffer.toString();
        testBuffer.delete(0, testBuffer.length());
        assertEquals("Didn't clone shared buffer", "This is a test buffer", sharedStr
                );
        assertTrue("Deleted incorrect chars", testBuffer.toString().equals(""));
        testBuffer.append("more stuff");
        assertEquals("Didn't clone shared buffer 2", "This is a test buffer", sharedStr
                );
        assertEquals("Wrong contents", "more stuff", testBuffer.toString());
        try {
            testBuffer.delete(-5, 2);
        } catch (IndexOutOfBoundsException e) {
        }
        assertEquals("Wrong contents 2", 
                "more stuff", testBuffer.toString());
    }

    /**
     * @tests java.lang.StringBuffer#deleteCharAt(int)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "deleteCharAt",
        args = {int.class}
    )
    public void test_deleteCharAtI() {
        // Test for method java.lang.StringBuffer
        // java.lang.StringBuffer.deleteCharAt(int)
        testBuffer.deleteCharAt(3);
        assertEquals("Deleted incorrect char", 
                "Thi is a test buffer", testBuffer.toString());
        try {
            testBuffer.deleteCharAt(testBuffer.length() + 1);
            fail("StringIndexOutOfBoundsException was not thrown.");
        } catch(StringIndexOutOfBoundsException sioobe) {
            //expected
        }
        
        try {
            testBuffer.deleteCharAt(-1);
            fail("StringIndexOutOfBoundsException was not thrown.");
        } catch(StringIndexOutOfBoundsException sioobe) {
            //expected
        }
    }

    /**
     * @tests java.lang.StringBuffer#ensureCapacity(int)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "ensureCapacity",
        args = {int.class}
    )
    public void test_ensureCapacityI() {
        // Test for method void java.lang.StringBuffer.ensureCapacity(int)
        StringBuffer sb = new StringBuffer(10);

        sb.ensureCapacity(-2);
        assertEquals("Failed to increase capacity.", 10, sb.capacity());
        
        sb.ensureCapacity(100);
        assertTrue("Failed to increase capacity", sb.capacity() >= 100);
        
        try {
            sb.ensureCapacity(Integer.MAX_VALUE);
            fail("OutOfMemoryError should be thrown.");
        } catch(java.lang.OutOfMemoryError oome) {
            //expected
        }
    }

    /**
     * @tests java.lang.StringBuffer#getChars(int, int, char[], int)
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Doesn't check exceptions.",
        method = "getChars",
        args = {int.class, int.class, char[].class, int.class}
    )
    public void test_getCharsII$CI() {
        // Test for method void java.lang.StringBuffer.getChars(int, int, char
        // [], int)

        char[] buf = new char[10];
        testBuffer.getChars(4, 8, buf, 2);
        assertTrue("Returned incorrect chars", new String(buf, 2, 4)
                .equals(testBuffer.toString().substring(4, 8)));

        StringBuffer buf2 = new StringBuffer("");
        try {
            buf2.getChars(-1, 0, new char[5], 2);
            fail("IndexOutOfBoundsException is not thrown.");
        } catch (IndexOutOfBoundsException e) {
            //expected
        }
        
        try {
            buf2.getChars(0, -1, new char[5], 2);
            fail("IndexOutOfBoundsException is not thrown.");
        } catch (IndexOutOfBoundsException e) {
            //expected
        }
        
        try {
            buf2.getChars(0, -1, new char[5], 2);
            fail("IndexOutOfBoundsException is not thrown.");
        } catch (IndexOutOfBoundsException e) {
            //expected
        }
        
        try {
            buf2.getChars(2, 1, new char[5], 2);
            fail("IndexOutOfBoundsException is not thrown.");
        } catch (IndexOutOfBoundsException e) {
            //expected
        }
        
        try {
            buf2.getChars(0, 6, new char[5], 2);
            fail("IndexOutOfBoundsException is not thrown.");
        } catch (IndexOutOfBoundsException e) {
            //expected
        }
        
        try {
            buf2.getChars(0, 6, new char[10], 5);
            fail("IndexOutOfBoundsException is not thrown.");
        } catch (IndexOutOfBoundsException e) {
            //expected
        }
    }

    /**
     * @tests java.lang.StringBuffer#insert(int, char[])
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "insert",
        args = {int.class, char[].class}
    )
    public void test_insertI$C() {
        // Test for method java.lang.StringBuffer
        // java.lang.StringBuffer.insert(int, char [])
        char buf[] = new char[4];
        "char".getChars(0, 4, buf, 0);
        testBuffer.insert(15, buf);
        assertEquals("Insert test failed", 
                "This is a test charbuffer", testBuffer.toString());

        boolean exception = false;
        StringBuffer buf1 = new StringBuffer("abcd");
        try {
            buf1.insert(-1, (char[]) null);
        } catch (StringIndexOutOfBoundsException e) {
            exception = true;
        } catch (NullPointerException e) {
        }
        assertTrue("Should throw StringIndexOutOfBoundsException", exception);
    }

    /**
     * @tests java.lang.StringBuffer#insert(int, char[], int, int)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "insert",
        args = {int.class, char[].class, int.class, int.class}
    )
    public void test_insertI$CII() {
        // Test for method java.lang.StringBuffer
        // java.lang.StringBuffer.insert(int, char [], int, int)
        char[] c = new char[] { 'n', 'o', 't', ' ' };
        testBuffer.insert(8, c, 0, 4);
        assertEquals("This is not a test buffer", testBuffer.toString());

        StringBuffer buf1 = new StringBuffer("abcd");
        try {
            buf1.insert(-1, (char[]) null, 0, 0);
            fail("Should throw StringIndexOutOfBoundsException");
        } catch (StringIndexOutOfBoundsException e) {
            //expected
        }
        
        try {
            testBuffer.insert(testBuffer.length() - 1, c, -1, 1);
        } catch (StringIndexOutOfBoundsException e) {
            //expected
        }
        
    }

    /**
     * @tests java.lang.StringBuffer#insert(int, char)
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "IndexOutOfBoundsException is not verified.",
        method = "insert",
        args = {int.class, char.class}
    )
    public void test_insertIC() {
        // Test for method java.lang.StringBuffer
        // java.lang.StringBuffer.insert(int, char)
        testBuffer.insert(15, 'T');
        assertEquals("Insert test failed", 
                "This is a test Tbuffer", testBuffer.toString());
    }

    /**
     * @tests java.lang.StringBuffer#insert(int, double)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "insert",
        args = {int.class, double.class}
    )
    public void test_insertID() {
        // Test for method java.lang.StringBuffer
        // java.lang.StringBuffer.insert(int, double)
        testBuffer.insert(15, Double.MAX_VALUE);
        assertTrue("Insert test failed", testBuffer.toString().equals(
                "This is a test " + Double.MAX_VALUE + "buffer"));
        try {
            testBuffer.insert(-1, Double.MAX_VALUE);
            fail("StringIndexOutOfBoundsException is not thrown.");
        } catch(StringIndexOutOfBoundsException sioobe) {
            //expected
        }
        
        try {
            testBuffer.insert(testBuffer.length() + 1, Double.MAX_VALUE);
            fail("StringIndexOutOfBoundsException is not thrown.");
        } catch(StringIndexOutOfBoundsException sioobe) {
            //expected
        }
    }

    /**
     * @tests java.lang.StringBuffer#insert(int, float)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "insert",
        args = {int.class, float.class}
    )
    public void test_insertIF() {
        // Test for method java.lang.StringBuffer
        // java.lang.StringBuffer.insert(int, float)
        testBuffer.insert(15, Float.MAX_VALUE);
        String testBufferString = testBuffer.toString();
        String expectedResult = "This is a test "
                + String.valueOf(Float.MAX_VALUE) + "buffer";
        assertTrue("Insert test failed, got: " + "\'" + testBufferString + "\'"
                + " but wanted: " + "\'" + expectedResult + "\'",
                testBufferString.equals(expectedResult));
        
        try {
            testBuffer.insert(-1, Float.MAX_VALUE);
            fail("StringIndexOutOfBoundsException is not thrown.");
        } catch(StringIndexOutOfBoundsException sioobe) {
            //expected
        }
        
        try {
            testBuffer.insert(testBuffer.length() + 1, Float.MAX_VALUE);
            fail("StringIndexOutOfBoundsException is not thrown.");
        } catch(StringIndexOutOfBoundsException sioobe) {
            //expected
        }
    }

    /**
     * @tests java.lang.StringBuffer#insert(int, int)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "insert",
        args = {int.class, int.class}
    )
    public void test_insertII() {
        // Test for method java.lang.StringBuffer
        // java.lang.StringBuffer.insert(int, int)
        testBuffer.insert(15, 100);
        assertEquals("Insert test failed", 
                "This is a test 100buffer", testBuffer.toString());
        
        try {
            testBuffer.insert(-1, Integer.MAX_VALUE);
            fail("StringIndexOutOfBoundsException is not thrown.");
        } catch(StringIndexOutOfBoundsException sioobe) {
            //expected
        }
        
        try {
            testBuffer.insert(testBuffer.length() + 1, Integer.MAX_VALUE);
            fail("StringIndexOutOfBoundsException is not thrown.");
        } catch(StringIndexOutOfBoundsException sioobe) {
            //expected
        }
    }

    /**
     * @tests java.lang.StringBuffer#insert(int, long)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "insert",
        args = {int.class, long.class}
    )
    public void test_insertIJ() {
        // Test for method java.lang.StringBuffer
        // java.lang.StringBuffer.insert(int, long)
        testBuffer.insert(15, 88888888888888888L);
        assertEquals("Insert test failed", 
                "This is a test 88888888888888888buffer", testBuffer.toString());
        
        try {
            testBuffer.insert(-1, Long.MAX_VALUE);
            fail("StringIndexOutOfBoundsException is not thrown.");
        } catch(StringIndexOutOfBoundsException sioobe) {
            //expected
        }
        
        try {
            testBuffer.insert(testBuffer.length() + 1, Long.MAX_VALUE);
            fail("StringIndexOutOfBoundsException is not thrown.");
        } catch(StringIndexOutOfBoundsException sioobe) {
            //expected
        }        
    }

    /**
     * @tests java.lang.StringBuffer#insert(int, java.lang.Object)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "insert",
        args = {int.class, java.lang.Object.class}
    )
    public void test_insertILjava_lang_Object() {
        // Test for method java.lang.StringBuffer
        // java.lang.StringBuffer.insert(int, java.lang.Object)
        Object obj1 = new Object();
        testBuffer.insert(15, obj1);
        assertTrue("Insert test failed", testBuffer.toString().equals(
                "This is a test " + obj1.toString() + "buffer"));
        
        try {
            testBuffer.insert(-1, obj1);
            fail("StringIndexOutOfBoundsException is not thrown.");
        } catch(StringIndexOutOfBoundsException sioobe) {
            //expected
        }
        
        try {
            testBuffer.insert(testBuffer.length() + 1, obj1);
            fail("StringIndexOutOfBoundsException is not thrown.");
        } catch(StringIndexOutOfBoundsException sioobe) {
            //expected
        }
    }

    /**
     * @tests java.lang.StringBuffer#insert(int, java.lang.String)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "insert",
        args = {int.class, java.lang.String.class}
    )
    public void test_insertILjava_lang_String() {
        // Test for method java.lang.StringBuffer
        // java.lang.StringBuffer.insert(int, java.lang.String)

        testBuffer.insert(15, "STRING ");
        assertEquals("Insert test failed", 
                "This is a test STRING buffer", testBuffer.toString());
        
        try {
            testBuffer.insert(-1, "");
            fail("StringIndexOutOfBoundsException is not thrown.");
        } catch(StringIndexOutOfBoundsException sioobe) {
            //expected
        }
        
        try {
            testBuffer.insert(testBuffer.length() + 1, "");
            fail("StringIndexOutOfBoundsException is not thrown.");
        } catch(StringIndexOutOfBoundsException sioobe) {
            //expected
        }        
    }

    /**
     * @tests java.lang.StringBuffer#insert(int, boolean)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "insert",
        args = {int.class, boolean.class}
    )
    public void test_insertIZ() {
        // Test for method java.lang.StringBuffer
        // java.lang.StringBuffer.insert(int, boolean)
        testBuffer.insert(15, true);
        assertEquals("Insert test failed", 
                "This is a test truebuffer", testBuffer.toString());
        try {
            testBuffer.insert(testBuffer.length() + 1, true);
            fail("StringIndexOutOfBoundsException is not thrown.");
        } catch(StringIndexOutOfBoundsException sioobe) {
            //expected
        }
        
        try {
            testBuffer.insert(-1, true);
            fail("StringIndexOutOfBoundsException is not thrown.");
        } catch(StringIndexOutOfBoundsException sioobe) {
            //expected
        }
    }

    /**
     * @tests java.lang.StringBuffer#length()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "length",
        args = {}
    )
    public void test_length() {
        // Test for method int java.lang.StringBuffer.length()
        assertEquals("Incorrect length returned", 21, testBuffer.length());
    }

    /**
     * @tests java.lang.StringBuffer#replace(int, int, java.lang.String)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "replace",
        args = {int.class, int.class, java.lang.String.class}
    )
    public void test_replaceIILjava_lang_String() {
        // Test for method java.lang.StringBuffer
        // java.lang.StringBuffer.replace(int, int, java.lang.String)
        testBuffer.replace(5, 9, "is a replaced");
        assertTrue("Replace failed, wanted: " + "\'"
                + "This is a replaced test buffer" + "\'" + " but got: " + "\'"
                + testBuffer.toString() + "\'", testBuffer.toString().equals(
                "This is a replaced test buffer"));
        assertEquals("insert1", "text", new StringBuffer().replace(0, 0, "text")
                .toString());
        assertEquals("insert2", "123text", new StringBuffer("123").replace(3, 3, "text")
                .toString());
        assertEquals("insert2", "1text23", new StringBuffer("123").replace(1, 1, "text")
                .toString());
        
        try {
            testBuffer.replace(-1, 0, "text");
            fail("StringIndexOutOfBoundsException is not thrown.");
        } catch(StringIndexOutOfBoundsException sioobe) {
            //expected
        }
        
        try {
            testBuffer.replace(0, -1, "text");
            fail("StringIndexOutOfBoundsException is not thrown.");
        } catch(StringIndexOutOfBoundsException sioobe) {
            //expected
        }
        
        try {
            testBuffer.replace(2, 1, "text");
            fail("StringIndexOutOfBoundsException is not thrown.");
        } catch(StringIndexOutOfBoundsException sioobe) {
            //expected
        }
        
        try {
            testBuffer.replace(testBuffer.length() + 1, testBuffer.length() + 1, 
                    "text");
            fail("StringIndexOutOfBoundsException is not thrown.");
        } catch(StringIndexOutOfBoundsException sioobe) {
            //expected
        }
    }

    private String writeString(String in) {
        StringBuffer result = new StringBuffer();
        result.append("\"");
        for (int i = 0; i < in.length(); i++) {
            result.append(" 0x" + Integer.toHexString(in.charAt(i)));
        }
        result.append("\"");
        return result.toString();
    }

    private void reverseTest(String id, String org, String rev, String back) {
        // create non-shared StringBuffer
        StringBuffer sb = new StringBuffer(org);
        sb.reverse();
        String reversed = sb.toString();
        assertTrue("reversed surrogate " + id + ": " + writeString(reversed),
                reversed.equals(rev));
        // create non-shared StringBuffer
        sb = new StringBuffer(reversed);
        sb.reverse();
        reversed = sb.toString();
        assertTrue("reversed surrogate " + id + "a: " + writeString(reversed),
                reversed.equals(back));

        // test algorithm when StringBuffer is shared
        sb = new StringBuffer(org);
        String copy = sb.toString();
        assertEquals(org, copy);
        sb.reverse();
        reversed = sb.toString();
        assertTrue("reversed surrogate " + id + ": " + writeString(reversed),
                reversed.equals(rev));
        sb = new StringBuffer(reversed);
        copy = sb.toString();
        assertEquals(rev, copy);
        sb.reverse();
        reversed = sb.toString();
        assertTrue("reversed surrogate " + id + "a: " + writeString(reversed),
                reversed.equals(back));

    }

    /**
     * @tests java.lang.StringBuffer#reverse()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "reverse",
        args = {}
    )
    public void test_reverse() {
        // Test for method java.lang.StringBuffer
        // java.lang.StringBuffer.reverse()
        String org;
        org = "a";
        reverseTest("0", org, org, org);

        org = "ab";
        reverseTest("1", org, "ba", org);

        org = "abcdef";
        reverseTest("2", org, "fedcba", org);

        org = "abcdefg";
        reverseTest("3", org, "gfedcba", org);

    }

    /**
     * @tests java.lang.StringBuffer#setCharAt(int, char)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setCharAt",
        args = {int.class, char.class}
    )
    public void test_setCharAtIC() {
        // Test for method void java.lang.StringBuffer.setCharAt(int, char)
        StringBuffer s = new StringBuffer("HelloWorld");
        s.setCharAt(4, 'Z');
        assertEquals("Returned incorrect char", 'Z', s.charAt(4));
        
        try {
            s.setCharAt(-1, 'Z');           
            fail("IndexOutOfBoundsException is not thrown.");
        } catch(IndexOutOfBoundsException ioobe) {
            //expected
        }
        try {
            s.setCharAt(s.length() + 1, 'Z');           
            fail("IndexOutOfBoundsException is not thrown.");
        } catch(IndexOutOfBoundsException ioobe) {
            //expected
        }
    }

    /**
     * @tests java.lang.StringBuffer#setLength(int)
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "IndexOutOfBoundsException is not verified.",
        method = "setLength",
        args = {int.class}
    )
    public void test_setLengthI() {
        // Test for method void java.lang.StringBuffer.setLength(int)
        testBuffer.setLength(1000);
        assertEquals("Failed to increase length", 1000, testBuffer.length());
        assertTrue("Increase in length trashed buffer", testBuffer.toString()
                .startsWith("This is a test buffer"));
        testBuffer.setLength(2);
        assertEquals("Failed to decrease length", 2, testBuffer.length());
        assertEquals("Decrease in length failed", 
                "Th", testBuffer.toString());
    }

    /**
     * @tests java.lang.StringBuffer#substring(int)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "substring",
        args = {int.class}
    )
    public void test_substringI() {
        // Test for method java.lang.String
        // java.lang.StringBuffer.substring(int)
        assertEquals("Returned incorrect substring", "is a test buffer",
                testBuffer.substring(5));
        
        try {
            testBuffer.substring(testBuffer.length() + 1);
            fail("StringIndexOutOfBoundsException is not thrown.");
        } catch(StringIndexOutOfBoundsException oobe) {
            //expected
        }
        
        try {
            testBuffer.substring(-1);
            fail("StringIndexOutOfBoundsException is not thrown.");
        } catch(StringIndexOutOfBoundsException oobe) {
            //expected
        }
    }

    /**
     * @tests java.lang.StringBuffer#substring(int, int)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "substring",
        args = {int.class, int.class}
    )
    public void test_substringII() {
        // Test for method java.lang.String
        // java.lang.StringBuffer.substring(int, int)
        assertEquals("Returned incorrect substring", "is", 
                testBuffer.substring(5, 7));
        
        try {
            testBuffer.substring(-1, testBuffer.length());
            fail("StringIndexOutOfBoundsException is not thrown.");
        } catch(StringIndexOutOfBoundsException oobe) {
            //expected
        }
        
        try {
            testBuffer.substring(0, -1);
            fail("StringIndexOutOfBoundsException is not thrown.");
        } catch(StringIndexOutOfBoundsException oobe) {
            //expected
        }
        
        try {
            testBuffer.substring(2, 1);
            fail("StringIndexOutOfBoundsException is not thrown.");
        } catch(StringIndexOutOfBoundsException oobe) {
            //expected
        }
    }

    /**
     * @tests java.lang.StringBuffer#toString()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "toString",
        args = {}
    )
    public void test_toString() {
        // Test for method java.lang.String java.lang.StringBuffer.toString()
        assertEquals("Incorrect string value returned", "This is a test buffer", testBuffer.toString()
                );
    }
    
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "subSequence",
        args = {int.class, int.class}
    )
          public void test_subSequence() {

              assertEquals("Incorrect substring returned", " is", 
                      testBuffer.subSequence(4, 7));
              assertEquals("Incorrect substring returned", "test buffer",
                      testBuffer.subSequence(10, 21));
              assertEquals("not identical", "This is a test buffer", 
                      testBuffer.subSequence(0, testBuffer.length()));
              
              try {
                  testBuffer.subSequence(0, Integer.MAX_VALUE);
                  fail("IndexOutOfBoundsException was not thrown.");
              } catch(IndexOutOfBoundsException ioobe) {
                  //expected            
              }   
              
              try {
                  testBuffer.subSequence(Integer.MAX_VALUE, testBuffer.length());
                  fail("IndexOutOfBoundsException was not thrown.");
              } catch(IndexOutOfBoundsException ioobe) {
                  //expected            
              }  
              
              try {
                  testBuffer.subSequence(-1, testBuffer.length());
                  fail("IndexOutOfBoundsException was not thrown.");
              } catch(IndexOutOfBoundsException ioobe) {
                  //expected            
              }
          }

    @Override
    protected void setUp() {
        testBuffer = new StringBuffer("This is a test buffer");
    }
}
