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

import dalvik.annotation.TestTargets;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargetClass;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.IllegalFormatException;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

@TestTargetClass(String.class) 
public class String2Test extends junit.framework.TestCase {

    String hw1 = "HelloWorld";

    String hw2 = "HelloWorld";

    String hwlc = "helloworld";

    String hwuc = "HELLOWORLD";

    String hello1 = "Hello";

    String world1 = "World";

    String comp11 = "Test String";

    Object obj = new Object();

    char[] buf = { 'W', 'o', 'r', 'l', 'd' };

    char[] rbuf = new char[5];

    /**
     * @tests java.lang.String#String()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "String",
        args = {java.lang.String.class}
    )
    public void test_Constructor() {
        // Test for method java.lang.String()
        assertTrue("Created incorrect string", new String().equals(""));
    }

    /**
     * @tests java.lang.String#String(byte[])
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "String",
        args = {byte[].class}
    )
    public void test_Constructor$B() {
        // Test for method java.lang.String(byte [])
        assertTrue("Failed to create string", new String(hw1.getBytes())
                .equals(hw1));
    }

    /**
     * @tests java.lang.String#String(byte[], int)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "String",
        args = {byte[].class, int.class}
    )
    @SuppressWarnings("deprecation")
    public void test_Constructor$BI() {
        // Test for method java.lang.String(byte [], int)
        String s = new String(new byte[] { 65, 66, 67, 68, 69 }, 0);
        assertTrue("Incorrect string returned: " + s, s.equals("ABCDE"));
        s = new String(new byte[] { 65, 66, 67, 68, 69 }, 1);
        assertTrue("Did not use nonzero hibyte", !s.equals("ABCDE"));
    }

    /**
     * @tests java.lang.String#String(byte[], int, int)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "String",
        args = {byte[].class, int.class, int.class}
    )
    public void test_Constructor$BII() {
        // Test for method java.lang.String(byte [], int, int)
        assertTrue("Failed to create string", new String(hw1.getBytes(), 0, hw1
                .getBytes().length).equals(hw1));

        boolean exception = false;
        try {
            new String(new byte[0], 0, Integer.MAX_VALUE);
        } catch (IndexOutOfBoundsException e) {
            exception = true;
        }
        assertTrue("Did not throw exception", exception);
    }

    /**
     * @tests java.lang.String#String(byte[], int, int, int)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "String",
        args = {byte[].class, int.class, int.class, int.class}
    )
    @SuppressWarnings("deprecation")
    public void test_Constructor$BIII() {
        // Test for method java.lang.String(byte [], int, int, int)
        String s = new String(new byte[] { 65, 66, 67, 68, 69 }, 0, 1, 3);
        assertTrue("Incorrect string returned: " + s, s.equals("BCD"));
        s = new String(new byte[] { 65, 66, 67, 68, 69 }, 1, 0, 5);
        assertTrue("Did not use nonzero hibyte", !s.equals("ABCDE"));
    }

    /**
     * @tests java.lang.String#String(byte[], int, int, java.lang.String)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "String",
        args = {byte[].class, int.class, int.class, java.lang.String.class}
    )
    public void test_Constructor$BIILjava_lang_String() throws Exception {
        // Test for method java.lang.String(byte [], int, int, java.lang.String)
        String s = null;
        try {
            s = new String(new byte[] { 65, 66, 67, 68, 69 }, 0, 5, "8859_1");
        } catch (Exception e) {
            fail("Threw exception : " + e.getMessage());
        }
        assertTrue("Incorrect string returned: " + s, s.equals("ABCDE"));
        //Regression for HARMONY-1111
        assertNotNull(new String(new byte[] {(byte)0xC0}, 0, 1, "UTF-8"));
    }

    /**
     * @tests java.lang.String#String(byte[], java.lang.String)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "String",
        args = {byte[].class, java.lang.String.class}
    )
    public void test_Constructor$BLjava_lang_String() {
        // Test for method java.lang.String(byte [], java.lang.String)
        String s = null;
        try {
            s = new String(new byte[] { 65, 66, 67, 68, 69 }, "8859_1");
        } catch (Exception e) {
            fail("Threw exception : " + e.getMessage());
        }
        assertTrue("Incorrect string returned: " + s, s.equals("ABCDE"));
    }

    /**
     * @tests java.lang.String#String(char[])
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "String",
        args = {char[].class}
    )
    public void test_Constructor$C() {
        // Test for method java.lang.String(char [])
        assertEquals("Failed Constructor test", "World", new String(buf));
    }

    /**
     * @tests java.lang.String#String(char[], int, int)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "String",
        args = {char[].class, int.class, int.class}
    )
    public void test_Constructor$CII() {
        // Test for method java.lang.String(char [], int, int)
        char[] buf = { 'H', 'e', 'l', 'l', 'o', 'W', 'o', 'r', 'l', 'd' };
        String s = new String(buf, 0, buf.length);
        assertTrue("Incorrect string created", hw1.equals(s));

        boolean exception = false;
        try {
            new String(new char[0], 0, Integer.MAX_VALUE);
        } catch (IndexOutOfBoundsException e) {
            exception = true;
        }
        assertTrue("Did not throw exception", exception);
    }

    /**
     * @tests java.lang.String#String(int[], int, int)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "String",
        args = {int[].class, int.class, int.class}
    )
    public void test_Constructor$III() {
        // Test for method java.lang.String(int [], int, int)
        try {
            new String(new int[0], 2, Integer.MAX_VALUE);
            fail("Did not throw exception");
        } catch (IndexOutOfBoundsException e) {
            //expected
        }
    }

    /**
     * @tests java.lang.String#String(java.lang.String)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "String",
        args = {java.lang.String.class}
    )
    public void test_ConstructorLjava_lang_String() {
        // Test for method java.lang.String(java.lang.String)
        String s = new String("Hello World");
        assertEquals("Failed to construct correct string", "Hello World", s
                );
    }

    /**
     * @tests java.lang.String#String(java.lang.StringBuffer)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "String",
        args = {java.lang.StringBuffer.class}
    )
    public void test_ConstructorLjava_lang_StringBuffer() {
        // Test for method java.lang.String(java.lang.StringBuffer)
        StringBuffer sb = new StringBuffer();
        sb.append("HelloWorld");
        assertEquals("Created incorrect string", "HelloWorld", new String(sb)
                );
    }

    /**
     * @tests java.lang.String#charAt(int)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "charAt",
        args = {int.class}
    )
    public void test_charAtI() {
        // Test for method char java.lang.String.charAt(int)
        assertTrue("Incorrect character returned", hw1.charAt(5) == 'W'
                && (hw1.charAt(1) != 'Z'));
        
        String testString = "Test String";
        try {
            testString.charAt(testString.length());
            fail("IndexOutOfBoundsException was not thrown.");
        } catch(IndexOutOfBoundsException iobe) {
            //expected
        }
        
        try {
            testString.charAt(Integer.MAX_VALUE);
            fail("IndexOutOfBoundsException was not thrown.");
        } catch(IndexOutOfBoundsException iobe) {
            //expected
        }
        
        try {
            testString.charAt(-1);
            fail("IndexOutOfBoundsException was not thrown.");
        } catch(IndexOutOfBoundsException iobe) {
            //expected
        }
    }

    /**
     * @tests java.lang.String#compareTo(java.lang.String)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "compareTo",
        args = {java.lang.String.class}
    )
    public void test_compareToLjava_lang_String() {
        // Test for method int java.lang.String.compareTo(java.lang.String)
        assertTrue("Returned incorrect value for first < second", "aaaaab"
                .compareTo("aaaaac") < 0);
        assertEquals("Returned incorrect value for first = second", 0, "aaaaac"
                .compareTo("aaaaac"));
        assertTrue("Returned incorrect value for first > second", "aaaaac"
                .compareTo("aaaaab") > 0);
        assertTrue("Considered case to not be of importance", !("A"
                .compareTo("a") == 0));
        
        try {
            "fixture".compareTo(null);
            fail("No NPE");
        } catch (NullPointerException e) {
        }
    }

    /**
     * @tests java.lang.String#compareToIgnoreCase(java.lang.String)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "compareToIgnoreCase",
        args = {java.lang.String.class}
    )
    public void test_compareToIgnoreCaseLjava_lang_String() {
        // Test for method int
        // java.lang.String.compareToIgnoreCase(java.lang.String)
        assertTrue("Returned incorrect value for first < second", "aaaaab"
                .compareToIgnoreCase("aaaaac") < 0);
        assertEquals("Returned incorrect value for first = second", 0, "aaaaac"
                .compareToIgnoreCase("aaaaac"));
        assertTrue("Returned incorrect value for first > second", "aaaaac"
                .compareToIgnoreCase("aaaaab") > 0);
        assertEquals("Considered case to not be of importance", 0, "A"
                .compareToIgnoreCase("a"));

        assertTrue("0xbf should not compare = to 'ss'", "\u00df"
                .compareToIgnoreCase("ss") != 0);
        assertEquals("0x130 should compare = to 'i'", 0, "\u0130"
                .compareToIgnoreCase("i"));
        assertEquals("0x131 should compare = to 'i'", 0, "\u0131"
                .compareToIgnoreCase("i"));

        Locale defLocale = Locale.getDefault();
        try {
            Locale.setDefault(new Locale("tr", ""));
            assertEquals("Locale tr: 0x130 should compare = to 'i'", 0, "\u0130"
                    .compareToIgnoreCase("i"));
            assertEquals("Locale tr: 0x131 should compare = to 'i'", 0, "\u0131"
                    .compareToIgnoreCase("i"));
        } finally {
            Locale.setDefault(defLocale);
        }
        
        try {
            "fixture".compareToIgnoreCase(null);
            fail("No NPE");
        } catch (NullPointerException e) {
        }
    }

    /**
     * @tests java.lang.String#concat(java.lang.String)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "concat",
        args = {java.lang.String.class}
    )
    public void test_concatLjava_lang_String() {
        // Test for method java.lang.String
        // java.lang.String.concat(java.lang.String)
        assertTrue("Concatenation failed to produce correct string", hello1
                .concat(world1).equals(hw1));
        boolean exception = false;
        try {
            String a = new String("test");
            String b = null;
            a.concat(b);
        } catch (NullPointerException e) {
            exception = true;
        }
        assertTrue("Concatenation failed to throw NP exception (1)", exception);
        exception = false;
        try {
            String a = new String("");
            String b = null;
            a.concat(b);
        } catch (NullPointerException e) {
            exception = true;
        }
        assertTrue("Concatenation failed to throw NP exception (2)", exception);

        String s1 = "";
        String s2 = "s2";
        String s3 = s1.concat(s2);
        assertEquals(s2, s3);
        
        s3 = s2.concat(s1);
        assertSame(s2, s3);
    }

    /**
     * @tests java.lang.String#copyValueOf(char[])
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "copyValueOf",
        args = {char[].class}
    )
    public void test_copyValueOf$C() {
        // Test for method java.lang.String java.lang.String.copyValueOf(char
        // [])
        char[] t = { 'H', 'e', 'l', 'l', 'o', 'W', 'o', 'r', 'l', 'd' };
        assertEquals("copyValueOf returned incorrect String", "HelloWorld", String.copyValueOf(
                t));
    }

    /**
     * @tests java.lang.String#copyValueOf(char[], int, int)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "copyValueOf",
        args = {char[].class, int.class, int.class}
    )
    public void test_copyValueOf$CII() {
        // Test for method java.lang.String java.lang.String.copyValueOf(char
        // [], int, int)
        char[] t = { 'H', 'e', 'l', 'l', 'o', 'W', 'o', 'r', 'l', 'd' };
        assertEquals("copyValueOf returned incorrect String", "World", String.copyValueOf(
                t, 5, 5));
    }

    /**
     * @tests java.lang.String#endsWith(java.lang.String)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "endsWith",
        args = {java.lang.String.class}
    )
    public void test_endsWithLjava_lang_String() {
        // Test for method boolean java.lang.String.endsWith(java.lang.String)
        assertTrue("Failed to fine ending string", hw1.endsWith("ld"));
        assertFalse("Doesn't return false value.", hw1.endsWith("ld "));
        assertFalse("Doesn't return false value.", hw1.endsWith(" "));
        assertTrue("Returned incorrect value for empty string.", hw1.endsWith(""));
        try {
            hw1.endsWith(null);
            fail("NullPointerException is not thrown.");
        } catch(NullPointerException npe) {
            //expected
        }
    }

    /**
     * @tests java.lang.String#equals(java.lang.Object)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "equals",
        args = {java.lang.Object.class}
    )
    public void test_equalsLjava_lang_Object() {
        // Test for method boolean java.lang.String.equals(java.lang.Object)
        assertTrue("String not equal", hw1.equals(hw2) && !(hw1.equals(comp11)));
    }

    /**
     * @tests java.lang.String#equalsIgnoreCase(java.lang.String)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "equalsIgnoreCase",
        args = {java.lang.String.class}
    )
    public void test_equalsIgnoreCaseLjava_lang_String() {
        // Test for method boolean
        // java.lang.String.equalsIgnoreCase(java.lang.String)
        assertTrue("lc version returned unequal to uc", hwlc
                .equalsIgnoreCase(hwuc));
        
        assertTrue("Returned false for equals strings.", hwlc
                .equalsIgnoreCase(hwlc));     
        
        assertFalse("Returned true for different strings.", hwlc
                .equalsIgnoreCase(hwuc + " "));
    }

    /**
     * @tests java.lang.String#getBytes()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getBytes",
        args = {}
    )
    public void test_getBytes() {
        // Test for method byte [] java.lang.String.getBytes()
        byte[] sbytes = hw1.getBytes();
        for (int i = 0; i < hw1.length(); i++)
            assertTrue("Returned incorrect bytes", sbytes[i] == (byte) hw1
                    .charAt(i));

        char[] chars = new char[1];
        for (int i = 0; i < 65536; i++) {
            // skip surrogates
            if (i == 0xd800)
                i = 0xe000;
            byte[] result = null;
            chars[0] = (char) i;
            String string = new String(chars);
            try {
                result = string.getBytes("8859_1");
                if (i < 256) {
                    assertEquals((byte)i, result[0]);
                } else {
                    /*
                     * Substitue character should be 0x1A [1], but may be
                     * '?' character.
                     * [1] http://en.wikipedia.org/wiki/Substitute_character
                     */
                    assertTrue(result[0] == '?' || result[0] == 0x1a);
                }
            } catch (java.io.UnsupportedEncodingException e) {
            }
            try {
                result = string.getBytes("UTF8");
                int length = i < 0x80 ? 1 : (i < 0x800 ? 2 : 3);
                assertTrue("Wrong length UTF8: " + Integer.toHexString(i),
                        result.length == length);
                assertTrue(
                        "Wrong bytes UTF8: " + Integer.toHexString(i),
                        (i < 0x80 && result[0] == i)
                                || (i >= 0x80
                                        && i < 0x800
                                        && result[0] == (byte) (0xc0 | ((i & 0x7c0) >> 6)) && result[1] == (byte) (0x80 | (i & 0x3f)))
                                || (i >= 0x800
                                        && result[0] == (byte) (0xe0 | (i >> 12))
                                        && result[1] == (byte) (0x80 | ((i & 0xfc0) >> 6)) && result[2] == (byte) (0x80 | (i & 0x3f))));
            } catch (java.io.UnsupportedEncodingException e) {
            }

            String bytes = null;
            try {
                bytes = new String(result, "UTF8");
                assertTrue("Wrong UTF8 byte length: " + bytes.length() + "("
                        + i + ")", bytes.length() == 1);
                assertTrue(
                        "Wrong char UTF8: "
                                + Integer.toHexString(bytes.charAt(0)) + " ("
                                + i + ")", bytes.charAt(0) == i);
            } catch (java.io.UnsupportedEncodingException e) {
            }
        }

        byte[] bytes = new byte[1];
        for (int i = 0; i < 256; i++) {
            bytes[0] = (byte) i;
            String result = null;
            try {
                result = new String(bytes, "8859_1");
                assertEquals("Wrong char length", 1, result.length());
                assertTrue("Wrong char value", result.charAt(0) == (char) i);
            } catch (java.io.UnsupportedEncodingException e) {
            }
        }
    }

    /**
     * @tests java.lang.String#getBytes(int, int, byte[], int)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getBytes",
        args = {int.class, int.class, byte[].class, int.class}
    )
    @SuppressWarnings("deprecation")
    public void test_getBytesII$BI() {
        // Test for method void java.lang.String.getBytes(int, int, byte [],
        // int)
        byte[] buf = new byte[5];
        "Hello World".getBytes(6, 11, buf, 0);
        assertEquals("Returned incorrect bytes", "World", new String(buf));

        try {
            "Hello World".getBytes(-1, 1, null, 0);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            //expected
        } catch (NullPointerException e) {
            fail("Threw wrong exception");
        }
        
        try {
            "Hello World".getBytes(6, 2, null, 0);
            fail("IndexOutOfBoundsException was not thrown.");
        } catch (IndexOutOfBoundsException e) {
            //expected
        }
        
        try {
            "Hello World".getBytes(2, 10, new byte[10], 4);
            fail("IndexOutOfBoundsException was not thrown.");
        } catch (IndexOutOfBoundsException e) {
            //expected
        }        
    }

    /**
     * @tests java.lang.String#getBytes(java.lang.String)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getBytes",
        args = {java.lang.String.class}
    )
    public void test_getBytesLjava_lang_String() throws Exception {
        // Test for method byte [] java.lang.String.getBytes(java.lang.String)
        byte[] buf = "Hello World".getBytes();
        assertEquals("Returned incorrect bytes", "Hello World", new String(buf));

        try {
            "string".getBytes("8849_1");
            fail("No UnsupportedEncodingException");
        } catch (UnsupportedEncodingException e) {
        }

        byte[] bytes = "\u3048".getBytes("UTF-8");
        byte[] expected = new byte[] {(byte)0xE3, (byte)0x81, (byte)0x88};
        assertEquals(expected[0], bytes[0]);
        assertEquals(expected[1], bytes[1]);
        assertEquals(expected[2], bytes[2]);

        // Regression for HARMONY-663
        try {
            // BEGIN android-changed
            // (replaced non-ASCII-printable characters with escapes)
            "string".getBytes(
                    "?Q?D??_??_6ffa?+vG?_??\u00ef\u00bf\u00bd??\u0015");
            // END android-changed
            fail("No UnsupportedEncodingException");
        } catch (UnsupportedEncodingException e) {
            //expected
        }
    }

    /**
     * @tests java.lang.String#getChars(int, int, char[], int)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getChars",
        args = {int.class, int.class, char[].class, int.class}
    )
    public void test_getCharsII$CI() {
        // Test for method void java.lang.String.getChars(int, int, char [],
        // int)
        hw1.getChars(5, hw1.length(), rbuf, 0);

        for (int i = 0; i < rbuf.length; i++)
            assertTrue("getChars returned incorrect char(s)", rbuf[i] == buf[i]);
        
        try {
            "Hello World".getChars(-1, 1, null, 0);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            //expected
        } catch (NullPointerException e) {
            fail("Threw wrong exception");
        }
        
        try {
            "Hello World".getChars(6, 2, null, 0);
            fail("IndexOutOfBoundsException was not thrown.");
        } catch (IndexOutOfBoundsException e) {
            //expected
        }
        
        try {
            "Hello World".getChars(2, 10, new char[10], 4);
            fail("IndexOutOfBoundsException was not thrown.");
        } catch (IndexOutOfBoundsException e) {
            //expected
        }            
    }

    /**
     * @tests java.lang.String#hashCode()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "hashCode",
        args = {}
    )
    public void test_hashCode() {
        // Test for method int java.lang.String.hashCode()
        int hwHashCode = 0;
        final int hwLength = hw1.length();
        int powerOfThirtyOne = 1;
        for (int counter = hwLength - 1; counter >= 0; counter--) {
            hwHashCode += hw1.charAt(counter) * powerOfThirtyOne;
            powerOfThirtyOne *= 31;
        }
        assertTrue("String did not hash to correct value--got: "
                + String.valueOf(hw1.hashCode()) + " but wanted: "
                + String.valueOf(hwHashCode), hw1.hashCode() == hwHashCode);
        assertTrue("The empty string \"\" did not hash to zero", 0 == ""
                .hashCode());
    }

    /**
     * @tests java.lang.String#indexOf(int)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "indexOf",
        args = {int.class}
    )
    public void test_indexOfI() {
        // Test for method int java.lang.String.indexOf(int)
        assertEquals("Invalid index returned", 1, hw1.indexOf('e'));

        assertEquals("Doesn't return -1 if there is no such character.", -1,
                hw1.indexOf('q'));
    }

    /**
     * @tests java.lang.String#indexOf(int, int)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "indexOf",
        args = {int.class, int.class}
    )
    public void test_indexOfII() {
        // Test for method int java.lang.String.indexOf(int, int)
        assertEquals("Invalid character index returned", 5, hw1.indexOf('W', 2));
        assertEquals("Doesn't return -1 if there is no such character.", -1, 
                hw1.indexOf('H', 2));

    }

    /**
     * @tests java.lang.String#indexOf(java.lang.String)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "indexOf",
        args = {java.lang.String.class}
    )
    public void test_indexOfLjava_lang_String() {
        // Test for method int java.lang.String.indexOf(java.lang.String)
        assertTrue("Failed to find string", hw1.indexOf("World") > 0);
        assertTrue("Failed to find string", !(hw1.indexOf("ZZ") > 0));
        assertEquals("Doesn't return -1 for unknown string.", 
                -1, hw1.indexOf("Heo"));
    }

    /**
     * @tests java.lang.String#indexOf(java.lang.String, int)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "indexOf",
        args = {java.lang.String.class, int.class}
    )
    public void test_indexOfLjava_lang_StringI() {
        // Test for method int java.lang.String.indexOf(java.lang.String, int)
        assertTrue("Failed to find string", hw1.indexOf("World", 0) > 0);
        assertTrue("Found string outside index", !(hw1.indexOf("Hello", 6) > 0));
        assertEquals("Did not accept valid negative starting position", 0, hello1
                .indexOf("", -5));
        assertEquals("Reported wrong error code", 5, hello1.indexOf("", 5));
        assertEquals("Wrong for empty in empty", 0, "".indexOf("", 0));
    }

    /**
     * @tests java.lang.String#intern()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "intern",
        args = {}
    )
    public void test_intern() {
        // Test for method java.lang.String java.lang.String.intern()
        assertTrue("Intern returned incorrect result", hw1.intern() == hw2
                .intern());
    }

    /**
     * @tests java.lang.String#lastIndexOf(int)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "lastIndexOf",
        args = {int.class}
    )
    public void test_lastIndexOfI() {
        // Test for method int java.lang.String.lastIndexOf(int)
        assertEquals("Failed to return correct index", 5, hw1.lastIndexOf('W'));
        assertEquals("Returned index for non-existent char",
                -1, hw1.lastIndexOf('Z'));

    }

    /**
     * @tests java.lang.String#lastIndexOf(int, int)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "lastIndexOf",
        args = {int.class, int.class}
    )
    public void test_lastIndexOfII() {
        // Test for method int java.lang.String.lastIndexOf(int, int)
        assertEquals("Failed to return correct index",
                5, hw1.lastIndexOf('W', 6));
        assertEquals("Returned index for char out of specified range", -1, hw1
                .lastIndexOf('W', 4));
        assertEquals("Returned index for non-existent char", -1, hw1.lastIndexOf('Z',
                9));

    }

    /**
     * @tests java.lang.String#lastIndexOf(java.lang.String)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "lastIndexOf",
        args = {java.lang.String.class}
    )
    public void test_lastIndexOfLjava_lang_String() {
        // Test for method int java.lang.String.lastIndexOf(java.lang.String)
        assertEquals("Returned incorrect index", 5, hw1.lastIndexOf("World"));
        assertEquals("Found String outside of index", -1, hw1
                .lastIndexOf("HeKKKKKKKK"));
    }

    /**
     * @tests java.lang.String#lastIndexOf(java.lang.String, int)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "lastIndexOf",
        args = {java.lang.String.class, int.class}
    )
    public void test_lastIndexOfLjava_lang_StringI() {
        // Test for method int java.lang.String.lastIndexOf(java.lang.String,
        // int)
        assertEquals("Returned incorrect index", 5, hw1.lastIndexOf("World", 9));
        int result = hw1.lastIndexOf("Hello", 2);
        assertTrue("Found String outside of index: " + result, result == 0);
        assertEquals("Reported wrong error code",
                -1, hello1.lastIndexOf("", -5));
        assertEquals("Did not accept valid large starting position", 5, hello1
                .lastIndexOf("", 5));
    }

    /**
     * @tests java.lang.String#length()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "length",
        args = {}
    )
    public void test_length() {
        // Test for method int java.lang.String.length()
        assertEquals("Invalid length returned", 11, comp11.length());
    }

    /**
     * @tests java.lang.String#regionMatches(int, java.lang.String, int, int)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "regionMatches",
        args = {int.class, java.lang.String.class, int.class, int.class}
    )
    public void test_regionMatchesILjava_lang_StringII() {
        // Test for method boolean java.lang.String.regionMatches(int,
        // java.lang.String, int, int)
        String bogusString = "xxcedkedkleiorem lvvwr e''' 3r3r 23r";

        assertTrue("identical regions failed comparison", hw1.regionMatches(2,
                hw2, 2, 5));
        assertTrue("Different regions returned true", !hw1.regionMatches(2,
                bogusString, 2, 5));
        assertFalse("Returned true for negative offset.", hw1.regionMatches(-1,
                hw2, 2, 5));
        assertFalse("Returned true for negative offset.", hw1.regionMatches(2,
                hw2, -1, 5));
        assertFalse("Returned true for toffset+len is greater than the length.", 
                hw1.regionMatches(5, hw2, 2, 6));
        assertFalse("Returned true for ooffset+len is greater than the length.", 
                hw1.regionMatches(2, hw2, 5, 6));        
    }

    /**
     * @tests java.lang.String#regionMatches(boolean, int, java.lang.String,
     *        int, int)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "regionMatches",
        args = {boolean.class, int.class, java.lang.String.class, int.class, int.class}
    )
    public void test_regionMatchesZILjava_lang_StringII() {
        // Test for method boolean java.lang.String.regionMatches(boolean, int,
        // java.lang.String, int, int)

        String bogusString = "xxcedkedkleiorem lvvwr e''' 3r3r 23r";

        assertTrue("identical regions failed comparison", hw1.regionMatches(
                false, 2, hw2, 2, 5));
        assertTrue("identical regions failed comparison with different cases",
                hw1.regionMatches(true, 2, hw2, 2, 5));
        assertTrue("Different regions returned true", !hw1.regionMatches(true,
                2, bogusString, 2, 5));
        assertTrue("identical regions failed comparison with different cases",
                hw1.regionMatches(false, 2, hw2, 2, 5));
        
        assertFalse("Returned true for negative offset.", hw1.regionMatches(true, 
                -1, hw2, 2, 5));
        assertFalse("Returned true for negative offset.", hw1.regionMatches(false, 
                2, hw2, -1, 5));
        assertFalse("Returned true for toffset+len is greater than the length.", 
                hw1.regionMatches(true, 5, hw2, 2, 6));
        assertFalse("Returned true for ooffset+len is greater than the length.", 
                hw1.regionMatches(false, 2, hw2, 5, 6));  
        
        assertTrue("identical regions failed comparison", hwuc.regionMatches(
                true, 0, hwlc, 0, hwuc.length()));
        assertFalse("non identical regions failed comparison", hwuc.regionMatches(
                false, 0, hwlc, 0, hwuc.length()));
    }

    /**
     * @tests java.lang.String#replace(char, char)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "replace",
        args = {char.class, char.class}
    )
    public void test_replaceCC() {
        // Test for method java.lang.String java.lang.String.replace(char, char)
        assertEquals("Failed replace", "HezzoWorzd", hw1.replace('l', 'z'));
        assertEquals("Returned incorrect string.", hw1, hw1.replace("!", "."));
    }
    
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "replaceAll",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void test_replaceAll() {
        String str = "!'123123.123HelloWorld!123123helloworld#";
        String [] patterns = {"[hw\\p{Upper}]", "(o|l){2,}", "([\'\"]?)(\\d+)",
                              "^!.*#$"};
        
        String [] results = {"!\'123123.123?ello?orld!123123?ello?orld#", 
                             "!\'123123.123He?World!123123he?world#",
                             "!?.?HelloWorld!?helloworld#", "?"};
        
        for(int i = 0; i < patterns.length; i++) {
            assertEquals("Returned incorrect string", 
                                  results[i], str.replaceAll(patterns[i], "?"));
        }
        
        try {
            str.replaceAll("[abc*", "?");
            fail("PatternSyntaxException is not thrown.");
        } catch(PatternSyntaxException pse) {
            //expected
        }
    }
    
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "replaceFirst",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void test_replaceFirst() {
        String str = "!'123123.123HelloWorld!123123helloworld#";
        String [] patterns = {"[hw\\p{Upper}]", "(o|l){2,}", "([\'\"]?)(\\d+)",
                              "^!.*#$"};
        
        String [] results = {"!'123123.123?elloWorld!123123helloworld#", 
                             "!'123123.123He?World!123123helloworld#",
                             "!?.123HelloWorld!123123helloworld#", "?"};
        
        for(int i = 0; i < patterns.length; i++) {
            assertEquals("Returned incorrect string", 
                                  results[i], str.replaceFirst(patterns[i], "?"));
        }
        
        try {
            str.replaceFirst("[abc*", "?");
            fail("PatternSyntaxException is not thrown.");
        } catch(PatternSyntaxException pse) {
            //expected
        }
    }
    
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "split",
        args = {java.lang.String.class}
    )
    public void test_splitLString() {
        String str = "!'123123.123HelloWorld!123123helloworld#";
        String [] patterns = {"[!.1]", "(\\d+).*e(l+)o.*orld"};
        String [][] results = {{"", "'","23", "23", "", "23HelloWorld", "", "23", 
                               "23helloworld#"}, 
                               {"!'", "#"}};

        for(int i = 0; i < patterns.length; i++) {
            assertTrue("Returned incorrect string array for pattern: " +
                patterns[i], Arrays.equals(results[i], str.split(patterns[i])));
        }
        
        try {
            str.split("[a}");
            fail("PatternSyntaxException is not thrown.");
        } catch(PatternSyntaxException pse) {
            //expected
        }
    }
    
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "split",
        args = {java.lang.String.class, int.class}
    )
    public void test_splitLStringLint() {
        String str = "!'123123.123HelloWorld!123123helloworld#";
        String pattern = "[!.1]";
        String [][] results = {{"", "'","23", "23.123HelloWorld!123123helloworld#"}, 
                               {"", "'","23", "23", "", "23HelloWorld", "", "23", 
                               "23helloworld#"}};

        assertTrue("Returned incorrect string array for limit 4", 
                Arrays.equals(results[0], str.split(pattern, 4)));
        assertTrue("Returned incorrect string array for limit 9", 
                Arrays.equals(results[1], str.split(pattern, 9)));   
        assertTrue("Returned incorrect string array for limit 0", 
                Arrays.equals(results[1], str.split(pattern, 0))); 
        assertTrue("Returned incorrect string array for limit -1", 
                Arrays.equals(results[1], str.split(pattern, -1)));   
        assertTrue("Returned incorrect string array for limit 10", 
                Arrays.equals(results[1], str.split(pattern, 10)));          
        assertTrue("Returned incorrect string array for limit Integer.MAX_VALUE", 
                Arrays.equals(results[1], str.split(pattern, Integer.MAX_VALUE))); 
        
        try {
            str.split("[a}", 0);
            fail("PatternSyntaxException is not thrown.");
        } catch(PatternSyntaxException pse) {
            //expected
        }
    }
    
    
    
    /**
     * @tests java.lang.String#replace(CharSequence, CharSequence)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "replace",
        args = {java.lang.CharSequence.class, java.lang.CharSequence.class}
    )
    public void test_replaceLjava_langCharSequenceLjava_langCharSequence() {            
        assertEquals("Failed replace", "aaccdd", "aabbdd".replace(
            new StringBuffer("bb"), "cc"));
        assertEquals("Failed replace by bigger seq", "cccbccc", "aba".replace(
            "a", "ccc"));
        assertEquals("Failed replace by smaller seq", "$bba^", 
            "$aaaaa^".replace(new StringBuilder("aa"), "b"));
        
        try {
            "".replace((CharSequence) null, "123".subSequence(0, 1));
            fail("NullPointerException is not thrown.");
        } catch(NullPointerException npe) {
            //expected
        }
        
        try {
            "".replace("123".subSequence(0, 1), (CharSequence) null);
            fail("NullPointerException is not thrown.");
        } catch(NullPointerException npe) {
            //expected
        }        
    }

    /**
     * @tests java.lang.String#startsWith(java.lang.String)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "startsWith",
        args = {java.lang.String.class}
    )
    public void test_startsWithLjava_lang_String() {
        // Test for method boolean java.lang.String.startsWith(java.lang.String)
        assertTrue("Failed to find string", hw1.startsWith("Hello"));
        assertTrue("Found incorrect string", !hw1.startsWith("T"));
    }

    /**
     * @tests java.lang.String#startsWith(java.lang.String, int)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "startsWith",
        args = {java.lang.String.class, int.class}
    )
    public void test_startsWithLjava_lang_StringI() {
        // Test for method boolean java.lang.String.startsWith(java.lang.String,
        // int)
        assertTrue("Failed to find string", hw1.startsWith("World", 5));
        assertTrue("Found incorrect string", !hw1.startsWith("Hello", 5));
    }

    /**
     * @tests java.lang.String#substring(int)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "substring",
        args = {int.class}
    )
    public void test_substringI() {
        // Test for method java.lang.String java.lang.String.substring(int)
        assertEquals("Incorrect substring returned", 
                "World", hw1.substring(5));
        assertTrue("not identical", hw1.substring(0) == hw1);
        
        try {
            hw1.substring(-1);
            fail("IndexOutOfBoundsException was not thrown.");
        } catch(IndexOutOfBoundsException ioobe) {
            //expected
        }
        
        try {
            hw1.substring(hw1.length() + 1);
            fail("IndexOutOfBoundsException was not thrown.");
        } catch(IndexOutOfBoundsException ioobe) {
            //expected
        }    
        
        try {
            hw1.substring(Integer.MAX_VALUE);
            fail("IndexOutOfBoundsException was not thrown.");
        } catch(IndexOutOfBoundsException ioobe) {
            //expected
        } 
    }

    /**
     * @tests java.lang.String#substring(int, int)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "substring",
        args = {int.class, int.class}
    )
    public void test_substringII() {
        // Test for method java.lang.String java.lang.String.substring(int, int)
        assertTrue("Incorrect substring returned", hw1.substring(0, 5).equals(
                "Hello")
                && (hw1.substring(5, 10).equals("World")));
        assertTrue("not identical", hw1.substring(0, hw1.length()) == hw1);
        
        try {
            hw1.substring(-1, hw1.length());
            fail("IndexOutOfBoundsException was not thrown.");
        } catch(IndexOutOfBoundsException ioobe) {
            //expected
        }
        
        try {
            hw1.substring(Integer.MAX_VALUE, hw1.length());
            fail("IndexOutOfBoundsException was not thrown.");
        } catch(IndexOutOfBoundsException ioobe) {
            //expected
        }
        
        try {
            hw1.substring(0, Integer.MAX_VALUE);
            fail("IndexOutOfBoundsException was not thrown.");
        } catch(IndexOutOfBoundsException ioobe) {
            //expected
        }
    }

    
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "subSequence",
        args = {int.class, int.class}
    )
    public void test_subSequence() {
        // Test for method java.lang.String java.lang.String.substring(int, int)
        assertTrue("Incorrect substring returned", hw1.subSequence(0, 5).equals(
                      "Hello") && (hw1.subSequence(5, 10).equals("World")));
        assertTrue("not identical", hw1.subSequence(0, hw1.length()) == hw1);
        
        try {
            hw1.subSequence(0, Integer.MAX_VALUE);
            fail("IndexOutOfBoundsException was not thrown.");
        } catch(IndexOutOfBoundsException ioobe) {
            //expected            
        }   
        
        try {
            hw1.subSequence(Integer.MAX_VALUE, hw1.length());
            fail("IndexOutOfBoundsException was not thrown.");
        } catch(IndexOutOfBoundsException ioobe) {
            //expected            
        }  
        
        try {
            hw1.subSequence(-1, hw1.length());
            fail("IndexOutOfBoundsException was not thrown.");
        } catch(IndexOutOfBoundsException ioobe) {
            //expected            
        }
    }
    
    /**
     * @tests java.lang.String#toCharArray()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "toCharArray",
        args = {}
    )
    public void test_toCharArray() {
        // Test for method char [] java.lang.String.toCharArray()

        String s = new String(buf, 0, buf.length);
        char[] schars = s.toCharArray();
        for (int i = 0; i < s.length(); i++)
            assertTrue("Returned incorrect char aray", buf[i] == schars[i]);
    }

    /**
     * @tests java.lang.String#toLowerCase()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "toLowerCase",
        args = {}
    )
    public void test_toLowerCase() {
        // Test for method java.lang.String java.lang.String.toLowerCase()
        assertTrue("toLowerCase case conversion did not succeed", hwuc
                .toLowerCase().equals(hwlc));

        assertEquals("a) Sigma has same lower case value at end of word with Unicode 3.0",
                "\u03c3", "\u03a3".toLowerCase());
        assertEquals("b) Sigma has same lower case value at end of word with Unicode 3.0",
                "a \u03c3", "a \u03a3".toLowerCase());
    }

    /**
     * @tests java.lang.String#toLowerCase(java.util.Locale)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "toLowerCase",
        args = {java.util.Locale.class}
    )
    public void test_toLowerCaseLjava_util_Locale() {
        // Test for method java.lang.String
        // java.lang.String.toLowerCase(java.util.Locale)
        assertTrue("toLowerCase case conversion did not succeed", hwuc
                .toLowerCase(java.util.Locale.getDefault()).equals(hwlc));
        assertEquals("Invalid \\u0049 for English", "\u0069", "\u0049".toLowerCase(
                Locale.ENGLISH));
        assertEquals("Invalid \\u0049 for Turkish", "\u0131", "\u0049".toLowerCase(
                new Locale("tr", "")));
    }

    /**
     * @tests java.lang.String#toString()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "toString",
        args = {}
    )
    public void test_toString() {
        // Test for method java.lang.String java.lang.String.toString()
        assertTrue("Incorrect string returned", hw1.toString().equals(hw1));
    }

    /**
     * @tests java.lang.String#toUpperCase()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "toUpperCase",
        args = {}
    )
    public void test_toUpperCase() {
        // Test for method java.lang.String java.lang.String.toUpperCase()
        assertTrue("Returned string is not UpperCase", hwlc.toUpperCase()
                .equals(hwuc));

        assertEquals("Wrong conversion", "SS", "\u00df".toUpperCase());

        String s = "a\u00df\u1f56";
        assertTrue("Invalid conversion", !s.toUpperCase().equals(s));

    }

    /**
     * @tests java.lang.String#toUpperCase(java.util.Locale)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "toUpperCase",
        args = {java.util.Locale.class}
    )
    public void test_toUpperCaseLjava_util_Locale() {
        // Test for method java.lang.String
        // java.lang.String.toUpperCase(java.util.Locale)
        assertTrue("Returned string is not UpperCase", hwlc.toUpperCase()
                .equals(hwuc));
        assertEquals("Invalid \\u0069 for English", "\u0049", "\u0069".toUpperCase(
                Locale.ENGLISH));
        assertEquals("Invalid \\u0069 for Turkish", "\u0130", "\u0069".toUpperCase(
                new Locale("tr", "")));
    }

    /**
     * @tests java.lang.String#trim()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "trim",
        args = {}
    )
    public void test_trim() {
        // Test for method java.lang.String java.lang.String.trim()
        assertTrue("Incorrect string returned", " HelloWorld ".trim().equals(
                hw1));
        assertEquals("Incorrect string returned", hw1, "  HelloWorld  ".trim());       
        assertTrue("Incorrect string returned", "   ".trim().equals(""));  
    }

    /**
     * @tests java.lang.String#valueOf(char[])
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "valueOf",
        args = {char[].class}
    )
    public void test_valueOf$C() {
        // Test for method java.lang.String java.lang.String.valueOf(char [])
        assertEquals("Returned incorrect String", 
                "World", String.valueOf(buf));
        assertEquals("Returned incorrect String", 
                "", String.valueOf(new char[]{}));
        try {
            String.valueOf(null);
            fail("NullPointerException was not thrown.");
        } catch(NullPointerException npe) {
            //expected
        }
    }

    /**
     * @tests java.lang.String#valueOf(char[], int, int)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "valueOf",
        args = {char[].class, int.class, int.class}
    )
    public void test_valueOf$CII() {
        // Test for method java.lang.String java.lang.String.valueOf(char [],
        // int, int)
        char[] t = { 'H', 'e', 'l', 'l', 'o', 'W', 'o', 'r', 'l', 'd' };
        assertEquals("copyValueOf returned incorrect String", "World", String.valueOf(t,
                5, 5));
        
        try {
            String.valueOf(t, 0, t.length + 1);
            fail("IndexOutOfBoundsException was not thrown.");
        } catch(IndexOutOfBoundsException ioobe) {
            //expected
        }
        
        try {
            String.valueOf(t, 0, -1);
            fail("IndexOutOfBoundsException was not thrown.");
        } catch(IndexOutOfBoundsException ioobe) {
            //expected
        }
        
        try {
            String.valueOf(t, 0, Integer.MAX_VALUE);
            fail("IndexOutOfBoundsException was not thrown.");
        } catch(IndexOutOfBoundsException ioobe) {
            //expected
        }        
    }

    /**
     * @tests java.lang.String#valueOf(char)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "valueOf",
        args = {char.class}
    )
    public void test_valueOfC() {
        // Test for method java.lang.String java.lang.String.valueOf(char)
        for (int i = 0; i < 65536; i++)
            assertTrue("Incorrect valueOf(char) returned: " + i, String
                    .valueOf((char) i).charAt(0) == (char) i);
    }

    /**
     * @tests java.lang.String#valueOf(double)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "valueOf",
        args = {double.class}
    )
    public void test_valueOfD() {
        // Test for method java.lang.String java.lang.String.valueOf(double)
        assertEquals("Incorrect double string returned", "1.7976931348623157E308", String.valueOf(
                Double.MAX_VALUE));
    }

    /**
     * @tests java.lang.String#valueOf(float)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "valueOf",
        args = {float.class}
    )
    public void test_valueOfF() {
        // Test for method java.lang.String java.lang.String.valueOf(float)
        assertTrue("incorrect float string returned--got: "
                + String.valueOf(1.0F) + " wanted: 1.0", String.valueOf(1.0F)
                .equals("1.0"));
        assertTrue("incorrect float string returned--got: "
                + String.valueOf(0.9F) + " wanted: 0.9", String.valueOf(0.9F)
                .equals("0.9"));
        assertTrue("incorrect float string returned--got: "
                + String.valueOf(109.567F) + " wanted: 109.567", String
                .valueOf(109.567F).equals("109.567"));
    }

    /**
     * @tests java.lang.String#valueOf(int)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "valueOf",
        args = {int.class}
    )
    public void test_valueOfI() {
        // Test for method java.lang.String java.lang.String.valueOf(int)
        assertEquals("returned invalid int string", "1", String.valueOf(1));
    }

    /**
     * @tests java.lang.String#valueOf(long)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "valueOf",
        args = {long.class}
    )
    public void test_valueOfJ() {
        // Test for method java.lang.String java.lang.String.valueOf(long)
        assertEquals("returned incorrect long string", "927654321098", String.valueOf(
                927654321098L));
    }

    /**
     * @tests java.lang.String#valueOf(java.lang.Object)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "valueOf",
        args = {java.lang.Object.class}
    )
    public void test_valueOfLjava_lang_Object() {
        // Test for method java.lang.String
        // java.lang.String.valueOf(java.lang.Object)
        assertTrue("Incorrect Object string returned", obj.toString().equals(
                String.valueOf(obj)));
        assertEquals("Incorrect value was returned for null.",
                 "null", String.valueOf((Object) null));
    }

    /**
     * @tests java.lang.String#valueOf(boolean)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "valueOf",
        args = {boolean.class}
    )
    public void test_valueOfZ() {
        // Test for method java.lang.String java.lang.String.valueOf(boolean)
        assertTrue("Incorrect boolean string returned", String.valueOf(false)
                .equals("false")
                && (String.valueOf(true).equals("true")));
    }

    /**
     * @tests java.lang.String#contentEquals(CharSequence cs)
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "NullPointerException is not verified.",
        method = "contentEquals",
        args = {java.lang.CharSequence.class}
    )
    public void test_contentEqualsLjava_lang_CharSequence() {
        // Test for method java.lang.String java.lang.String.contentEquals(CharSequence cs)
        assertFalse("Incorrect result of compare", "qwerty".contentEquals(""));
    }
    
    /**
     * @tests java.lang.String#format(Locale, String, Object[])
     */    
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "format",
        args = {java.lang.String.class, java.lang.Object[].class}
    )
    @SuppressWarnings("boxing")
    public void test_format() {
        assertEquals("13% of sum is 0x11", 
            String.format("%d%% of %s is 0x%x", 13, "sum", 17));
        
        assertEquals("3 2 1 4 3 2 1", String.format(
                "%3$d %2$d %1$d %4$d %3$d %2$d %1$d", 1, 2, 3, 4));
        
        assertEquals("empty format", "", String.format("", 123, this));
        try {
            String.format(null);
            fail("NPE is expected on null format");
        } catch (NullPointerException ok){}
        
        try {
            String.format("%d%% of %s is 0x%x", "123");
            fail("IllegalFormatException was not thrown.");
        } catch(IllegalFormatException ife) {
            //expected
        }
        
        try {
            String.format("%tu", "123");
            fail("IllegalFormatException was not thrown.");
        } catch(IllegalFormatException ife) {
            //expected
        }
  
    }
    
    /**
     * @tests java.lang.String#format(Locale, String, Object...)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "format",
        args = {java.util.Locale.class, java.lang.String.class, java.lang.Object[].class}
    )
    @SuppressWarnings("boxing")
    public void test_format_Locale() {
        Locale l = new Locale("UK");
        assertEquals("13% of sum is 0x11",
                String.format(l, "%d%% of %s is 0x%x", 13, "sum", 17));
        assertEquals("empty format", "", String.format("", 123, this));
        try {
            String.format(l, null, "");
            fail("NPE is expected on null format");
        } catch (NullPointerException ok){}
        
        try {
            String.format(l, "%d", "test");
            fail("IllegalFormatException wasn't thrown.");
        } catch(IllegalFormatException ife) {
            //expected
        }
    }
    
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "matches",
        args = {java.lang.String.class}
    )
    public void test_matches() {
        String[] patterns = {
                "(a|b)*abb",
                "(1*2*3*4*)*567",
                "(a|b|c|d)*aab",
                "(1|2|3|4|5|6|7|8|9|0)(1|2|3|4|5|6|7|8|9|0)*",
                "(abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ)*",
                "(a|b)*(a|b)*A(a|b)*lice.*",
                "(a|b|c|d|e|f|g|h|i|j|k|l|m|n|o|p|q|r|s|t|u|v|w|x|y|z)(a|b|c|d|e|f|g|h|"
                        + "i|j|k|l|m|n|o|p|q|r|s|t|u|v|w|x|y|z)*(1|2|3|4|5|6|7|8|9|0)*|while|for|struct|if|do",
        
       };

        String[] patternsInc = {
                "(ac)*bb",
                "(1)*567",
                "(c)*ab",
                "(|8|9|0)(1|2|7|8|9|0)*",
                "(z)",
                "(a)*A(b)*lice.",
                "(a|b|c|d|e)",
        
       };        
        
        String[][] strings = {
                { "abb", "ababb", "abababbababb", "abababbababbabababbbbbabb" },
                { "213567", "12324567", "1234567", "213213567",
                        "21312312312567", "444444567" },
                { "abcdaab", "aab", "abaab", "cdaab", "acbdadcbaab" },
                { "213234567", "3458", "0987654", "7689546432", "0398576",
                        "98432", "5" },
                {
                        "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ",
                        "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
                                + "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ" },
                { "ababbaAabababblice", "ababbaAliceababab", "ababbAabliceaaa",
                        "abbbAbbbliceaaa", "Alice" },
                { "a123", "bnxnvgds156", "for", "while", "if", "struct" },
                { "xy" }, { "xy" }, { "xcy" }

        };
        
        for (int i = 0; i < patterns.length; i++) {
            for (int j = 0; j < strings[i].length; j++) {
                assertTrue("Incorrect match: " + patterns[i] + " vs "
                        + strings[i][j], strings[i][j].matches(patterns[i]));
                assertFalse("" + i, strings[i][j].matches(patternsInc[i]));               
            }
        }
    }
        
}
