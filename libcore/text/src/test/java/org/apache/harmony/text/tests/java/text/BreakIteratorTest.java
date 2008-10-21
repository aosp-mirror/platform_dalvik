/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.apache.harmony.text.tests.java.text;

import java.text.BreakIterator;
import java.text.CharacterIterator;
import java.util.Collection;
import java.util.Collections;
import java.text.StringCharacterIterator;
import java.util.Locale;

import junit.framework.Test;
import junit.framework.TestCase;

public class BreakIteratorTest extends TestCase {

    private static final String TEXT = "a\u0308abc def, gh-12i?jkl.mno?";

    BreakIterator iterator;

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        iterator = BreakIterator.getCharacterInstance(Locale.US);
    }

    public void testConsts() {
        assertEquals(-1, BreakIterator.DONE);
    }

    public void testCache() {
        BreakIterator newOne = BreakIterator.getCharacterInstance(Locale.US);
        assertNotSame(newOne, iterator);
        assertEquals(newOne, iterator);

        newOne = BreakIterator.getCharacterInstance();
        assertEquals(newOne, iterator);

        newOne = BreakIterator.getCharacterInstance(Locale.CHINA);
        assertEquals(newOne, iterator);

        BreakIterator wordIterator = BreakIterator.getWordInstance();
        assertFalse(wordIterator.equals(iterator));

        BreakIterator lineIterator = BreakIterator.getLineInstance();
        assertFalse(lineIterator.equals(iterator));

        BreakIterator senteIterator = BreakIterator.getSentenceInstance();
        assertFalse(senteIterator.equals(iterator));
    }

    public void testClone() {
        BreakIterator cloned = (BreakIterator) iterator.clone();
        assertNotSame(cloned, iterator);
        assertEquals(cloned, iterator);
    }

    public void testCurrent() {
        assertEquals(0, iterator.current());
        iterator.setText(TEXT);
        assertEquals(iterator.first(), iterator.current());
    }

    /**
     * @tests java.text.BreakIterator#BreakIterator() Test of method
     *        java.text.BreakIterator#BreakIterator().
     */
    public void testConstructor() {
        try {
            new MockBreakIterator();
        } catch (Exception e) {
            fail("Unexpected exception " + e.toString());
        }
    }

    public void testFirst() {
        assertEquals(0, iterator.first());
        iterator.setText(TEXT);
        assertEquals(0, iterator.first());
    }

    public void testFollowing() {
        try {
            iterator.following(1);
            fail("should throw illegal argument exception");
        } catch (IllegalArgumentException e) {
        }
        iterator.setText(TEXT);
        assertEquals(2, iterator.following(1));
        try {
            assertEquals(0, iterator.following(-1));
            fail("should throw illegal argument exception");
        } catch (IllegalArgumentException e) {
        }
        try {
            iterator.following(TEXT.length());
            fail("should throw illegal argument exception");
        } catch (IllegalArgumentException e) {
        }
    }

    public void testIsBoundary() {
        try {
            iterator.isBoundary(2);
            fail("should throw illegal argument exception");
        } catch (IllegalArgumentException e) {
        }
        iterator.setText(TEXT);
        assertTrue(iterator.isBoundary(2));
        assertFalse(iterator.isBoundary(1));
        assertTrue(iterator.isBoundary(0));
        try {
            iterator.isBoundary(-1);
            fail("should throw illegal argument exception");
        } catch (IllegalArgumentException e) {
        }
        try {
            iterator.isBoundary(TEXT.length());
            fail("should throw illegal argument exception");
        } catch (IllegalArgumentException e) {
        }
    }

    public void testLast() {
        assertEquals(0, iterator.last());
        iterator.setText(TEXT);
        assertEquals(TEXT.length(), iterator.last());
    }

    /*
     * Class under test for int next(int)
     */
    public void testNextint() {
        assertEquals(BreakIterator.DONE, iterator.next(3));
        iterator.setText(TEXT);
        assertEquals(4, iterator.next(3));
        assertEquals(24, iterator.next(20));
        assertEquals(23, iterator.next(-1));
        assertEquals(-1, iterator.next(TEXT.length()));
    }

    public void testPreceding() {
        try {
            iterator.preceding(2);
            fail("should throw illegal argument exception");
        } catch (IllegalArgumentException e) {
        }
        iterator.setText(TEXT);
        assertEquals(0, iterator.preceding(2));
        assertEquals(2, iterator.preceding(3));
        assertEquals(16, iterator.preceding(17));
        assertEquals(17, iterator.preceding(18));
        assertEquals(18, iterator.preceding(19));
        try {
            iterator.preceding(-1);
            fail("should throw illegal argument exception");
        } catch (IllegalArgumentException e) {
        }
        try {
            iterator.preceding(TEXT.length());
            fail("should throw illegal argument exception");
        } catch (IllegalArgumentException e) {
        }
    }

    public void testPrevious() {
        assertEquals(-1, iterator.previous());
        iterator.setText(TEXT);
        assertEquals(-1, iterator.previous());
        iterator.last();
        assertEquals(TEXT.length() - 1, iterator.previous());
    }

    /**
     * @tests java.text.BreakIterator#getAvailableLocales(). Test of method
     *        java.text.BreakIterator#getAvailableLocales().
     */
    public void testGetAvailableLocales() {
        try {
            Locale[] locales = BreakIterator.getAvailableLocales();
            assertTrue("Array available locales is null", locales != null);
            assertTrue("Array available locales is 0-length",
                    (locales != null && locales.length != 0));
            boolean found = false;
            for (Locale l : locales) {
                if (l.equals(Locale.US)) {
                    // expected
                    found = true;
                }
            }
            assertTrue("At least locale " + Locale.US + " must be presented",
                    found);
        } catch (Exception e) {
            fail("Unexpected exception " + e.toString());
        }
    }

    /*
     * Class under test for BreakIterator getCharacterInstance()
     */
    public void testGetCharacterInstance() {
        BreakIterator.getCharacterInstance();
    }

    /*
     * Class under test for BreakIterator getCharacterInstance(Locale)
     */
    public void testGetCharacterInstanceLocale() {
        BreakIterator it = BreakIterator.getCharacterInstance(Locale.US);
        BreakIterator it2 = BreakIterator.getCharacterInstance(Locale.CHINA);
        assertEquals(it, it2);
    }

    /*
     * Class under test for BreakIterator getLineInstance()
     */
    public void testGetLineInstance() {
        BreakIterator it = BreakIterator.getLineInstance();
        assertNotNull(it);
    }

    /*
     * @tests java.text.BreakIterator#getLineInstance(Locale) Class under test
     * for BreakIterator getLineInstance(Locale)
     */
    public void testGetLineInstanceLocale() {
        try {
            BreakIterator it1 = BreakIterator
                    .getLineInstance(Locale.CANADA_FRENCH);
            assertTrue("Incorrect BreakIterator", it1 != BreakIterator
                    .getLineInstance());
            BreakIterator it2 = BreakIterator.getLineInstance(new Locale(
                    "bad locale"));
            assertTrue("Incorrect BreakIterator", it2 != BreakIterator
                    .getLineInstance());
        } catch (Exception e) {
            fail("Unexpected exception " + e.toString());
        }
    }

    /*
     * Class under test for BreakIterator getSentenceInstance()
     */
    public void testGetSentenceInstance() {
        BreakIterator it = BreakIterator.getSentenceInstance();
        assertNotNull(it);
    }

    /*
     * Class under test for BreakIterator getSentenceInstance(Locale)
     */
    public void testGetSentenceInstanceLocale() {
        BreakIterator it = BreakIterator.getSentenceInstance(Locale.US);
        assertNotNull(it);
    }

    public void testGetText() {
        assertEquals(new StringCharacterIterator(""), iterator.getText());
        iterator.setText(TEXT);
        assertEquals(new StringCharacterIterator(TEXT), iterator.getText());
    }

    /*
     * Class under test for BreakIterator getWordInstance()
     */
    public void testGetWordInstance() {
        BreakIterator it = BreakIterator.getWordInstance();
        assertNotNull(it);
    }

    /*
     * @tests java.text.BreakIterator#getWordInstance(Locale) Class under test
     * for BreakIterator getWordInstance(Locale)
     */
    public void testGetWordInstanceLocale() {
        try {
            BreakIterator it1 = BreakIterator
                    .getWordInstance(Locale.CANADA_FRENCH);
            assertTrue("Incorrect BreakIterator", it1 != BreakIterator
                    .getWordInstance());
            BreakIterator it2 = BreakIterator.getWordInstance(new Locale(
                    "bad locale"));
            assertTrue("Incorrect BreakIterator", it2 != BreakIterator
                    .getWordInstance());
        } catch (Exception e) {
            fail("Unexpected exception " + e.toString());
        }
    }

    /*
     * Class under test for void setText(CharacterIterator)
     */
    public void testSetTextCharacterIterator() {
        try {
            iterator.setText((CharacterIterator) null);
            fail();
        } catch (NullPointerException e) {
        }
        CharacterIterator it = new StringCharacterIterator("abc");
        iterator.setText(it);
        assertSame(it, iterator.getText());
    }

    /*
     * Class under test for void setText(String)
     */
    public void testSetTextString() {
        try {
            iterator.setText((String) null);
            fail();
        } catch (NullPointerException e) {
        }
        iterator.setText("abc");
        CharacterIterator it = new StringCharacterIterator("abc");
        assertEquals(it, iterator.getText());
    }

    public void test_next() {
        // Regression test for HARMONY-30
        BreakIterator bi = BreakIterator.getWordInstance(Locale.US);
        bi.setText("This is the test, WordInstance");
        int n = bi.first();
        n = bi.next();
        assertEquals("Assert 0: next() returns incorrect value ", 4, n);

        assertEquals(BreakIterator.DONE, iterator.next());
        iterator.setText(TEXT);
        assertEquals(2, iterator.next());
    }

    /**
     * @tests java.text.BreakIterator.getShort(byte[], int)
     * 
     */
    public void test_getShort() {
        try {
            MockBreakIterator.publicGetShort(null, 0);
            fail("should throw NPE.");
        } catch (NullPointerException e) {
        }
        try {
            MockBreakIterator.publicGetShort(null, -1);
            fail("should throw NPE.");
        } catch (NullPointerException e) {
        }
        try {
            MockBreakIterator.publicGetShort(new byte[] { 0, 0, 0, 1, 1 }, -1);
            fail("should throw ArrayIndexOutOfBoundsException.");
        } catch (ArrayIndexOutOfBoundsException e) {
        }
        try {
            MockBreakIterator.publicGetShort(new byte[] { 0, 0 }, 1);
            fail("should throw ArrayIndexOutOfBoundsException.");
        } catch (ArrayIndexOutOfBoundsException e) {
        }
        assertEquals(0, MockBreakIterator
                .publicGetShort(new byte[] { 0, 0 }, 0));
        assertEquals(1, MockBreakIterator
                .publicGetShort(new byte[] { 0, 1 }, 0));
        assertEquals(-1, MockBreakIterator.publicGetShort(new byte[] {
                (byte) 0xff, (byte) 0xff }, 0));
        assertEquals(1, MockBreakIterator.publicGetShort(new byte[] { 1, 0, 1,
                0 }, 1));
        assertEquals(1, MockBreakIterator.publicGetShort(new byte[] { 0, 0, 1,
                0 }, 1));
        assertEquals(1, MockBreakIterator.publicGetShort(new byte[] { 0, 0, 1,
                1 }, 1));
        assertEquals(257, MockBreakIterator.publicGetShort(
                new byte[] { 0, 1, 1 }, 1));

        // regression for Harmony-944
        try {
            MockBreakIterator.publicGetShort(new byte[] { 0, 0 },
                    Integer.MAX_VALUE);
            fail("should throw ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException e) {
            // expected
        }

        try {
            MockBreakIterator.publicGetShort(new byte[] { 0, 0 },
                    Short.MAX_VALUE);
            fail("should throw ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException e) {
            // expected
        }
    }

    /**
     * @tests java.text.BreakIterator.getInt(byte[], int)
     * 
     */
    public void test_getInt() {
        try {
            MockBreakIterator.publicGetInt(null, 0);
            fail("should throw NPE.");
        } catch (NullPointerException e) {
        }
        try {
            MockBreakIterator.publicGetInt(null, -1);
            fail("should throw NPE.");
        } catch (NullPointerException e) {
        }
        try {
            MockBreakIterator.publicGetInt(new byte[] { 0, 0, 0, 1, 1 }, -1);
            fail("should throw ArrayIndexOutOfBoundsException.");
        } catch (ArrayIndexOutOfBoundsException e) {
        }
        try {
            MockBreakIterator.publicGetInt(new byte[] { 0, 0, 0, 0 }, 1);
            fail("should throw ArrayIndexOutOfBoundsException.");
        } catch (ArrayIndexOutOfBoundsException e) {
        }
        assertEquals(0, MockBreakIterator.publicGetInt(
                new byte[] { 0, 0, 0, 0 }, 0));
        assertEquals(1, MockBreakIterator.publicGetInt(
                new byte[] { 0, 0, 0, 1 }, 0));
        assertEquals(-1, MockBreakIterator.publicGetInt(new byte[] {
                (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff }, 0));
        assertEquals(1, MockBreakIterator.publicGetInt(new byte[] { 1, 0, 0, 0,
                1, 0 }, 1));
        assertEquals(1, MockBreakIterator.publicGetInt(new byte[] { 0, 0, 0, 0,
                1, 0 }, 1));
        assertEquals(1, MockBreakIterator.publicGetInt(new byte[] { 0, 0, 0, 0,
                1, 1 }, 1));
        assertEquals(257, MockBreakIterator.publicGetInt(new byte[] { 0, 0, 0,
                1, 1 }, 1));

        // regression for Harmony-944
        try {
            MockBreakIterator.publicGetInt(new byte[] { 0, 0 },
                    Integer.MAX_VALUE);
            fail("should throw ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException e) {
            // expected
        }
    }

    /**
     * @tests java.text.BreakIterator.getLong(byte[], int)
     * 
     */
    public void test_getLong() {
        try {
            MockBreakIterator.publicGetLong(null, 0);
            fail("should throw NPE.");
        } catch (NullPointerException e) {
        }
        try {
            MockBreakIterator.publicGetLong(null, -1);
            fail("should throw NPE.");
        } catch (NullPointerException e) {
        }
        try {
            MockBreakIterator.publicGetLong(
                    new byte[] { 0, 0, 0, 0, 0, 0, 1, 1 }, -1);
            fail("should throw ArrayIndexOutOfBoundsException.");
        } catch (ArrayIndexOutOfBoundsException e) {
        }
        try {
            MockBreakIterator.publicGetLong(
                    new byte[] { 0, 0, 0, 0, 0, 0, 1, 1 }, 1);
            fail("should throw ArrayIndexOutOfBoundsException.");
        } catch (ArrayIndexOutOfBoundsException e) {
        }
        assertEquals(0, MockBreakIterator.publicGetLong(new byte[] { 0, 0, 0,
                0, 0, 0, 0, 0 }, 0));
        assertEquals(1, MockBreakIterator.publicGetLong(new byte[] { 0, 0, 0,
                0, 0, 0, 0, 1 }, 0));
        assertEquals(-1, MockBreakIterator.publicGetLong(new byte[] {
                (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
                (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff }, 0));
        assertEquals(1, MockBreakIterator.publicGetLong(new byte[] { 1, 0, 0,
                0, 0, 0, 0, 0, 1, 0 }, 1));
        assertEquals(1, MockBreakIterator.publicGetLong(new byte[] { 0, 0, 0,
                0, 0, 0, 0, 0, 1, 0 }, 1));
        assertEquals(1, MockBreakIterator.publicGetLong(new byte[] { 0, 0, 0,
                0, 0, 0, 0, 0, 1, 1 }, 1));
        assertEquals(257, MockBreakIterator.publicGetLong(new byte[] { 0, 0, 0,
                0, 0, 0, 0, 1, 1 }, 1));

        // regression for Harmony-944
        try {
            MockBreakIterator.publicGetLong(new byte[] { 0, 1 },
                    Integer.MAX_VALUE);
            fail("should throw ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException e) {
            // expected
        }
    }

    /**
     * @tests java.text.BreakIterator#getCharacterInstance(Locale)
     */
    public void testGetCharacterInstanceLocale_NPE() {
        // Regression for HARMONY-265
        try {
            BreakIterator.getCharacterInstance(null);
            fail("BreakIterator.getCharacterInstance(null); should throw NullPointerException");
        } catch (NullPointerException e) {
        }
    }

    public void testGetLineInstanceLocale_NPE() {
        try {
            BreakIterator.getLineInstance(null);
            fail("BreakIterator.getLineInstance(null); should throw NullPointerException");
        } catch (NullPointerException e) {
        }
    }

    public void testGetSentenceInstanceLocale_NPE() {
        try {
            BreakIterator.getSentenceInstance(null);
            fail("BreakIterator.getSentenceInstance(null); should throw NullPointerException");
        } catch (NullPointerException e) {
        }
    }

    public void testGetWordInstanceLocale_NPE() {
        try {
            BreakIterator.getWordInstance(null);
            fail("BreakIterator.getWordInstance(null); should throw NullPointerException");
        } catch (NullPointerException e) {
        }
    }

    private static class MockBreakIterator extends BreakIterator {
        public MockBreakIterator() {
            super();
        }

        public static int publicGetInt(byte[] buf, int offset) {
            return BreakIterator.getInt(buf, offset);
        }

        public static long publicGetLong(byte[] buf, int offset) {
            return BreakIterator.getLong(buf, offset);
        }

        public static short publicGetShort(byte[] buf, int offset) {
            return BreakIterator.getShort(buf, offset);
        }

        public int current() {
            return 0;
        }

        public int first() {
            return 0;
        }

        public int following(int offset) {
            return 0;
        }

        public CharacterIterator getText() {
            return null;
        }

        public int last() {
            return 0;
        }

        public int next() {
            return 0;
        }

        public int next(int n) {
            return 0;
        }

        public int previous() {
            return 0;
        }

        public void setText(CharacterIterator newText) {
        }
    }
}
