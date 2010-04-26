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

import dalvik.annotation.TestTargets;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargetClass;

import junit.framework.TestCase;

import java.text.BreakIterator;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.Locale;

@TestTargetClass(BreakIterator.class) 
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
    @TestTargetNew(
        level = TestLevel.TODO,
        notes = "Verifies constant.",
        method = "!Constants",
        args = {}
    )
    public void testConsts() {
        assertEquals(-1, BreakIterator.DONE);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getCharacterInstance",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getCharacterInstance",
            args = {java.util.Locale.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getWordInstance",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getLineInstance",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getSentenceInstance",
            args = {}
        )
    })
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
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "clone",
        args = {}
    )
    public void testClone() {
        BreakIterator cloned = (BreakIterator) iterator.clone();
        assertNotSame(cloned, iterator);
        assertEquals(cloned, iterator);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "current",
        args = {}
    )
    public void testCurrent() {
        assertEquals(0, iterator.current());
        iterator.setText(TEXT);
        assertEquals(iterator.first(), iterator.current());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "first",
        args = {}
    )
    public void testFirst() {
        assertEquals(0, iterator.first());
        iterator.setText(TEXT);
        assertEquals(0, iterator.first());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "following",
        args = {int.class}
    )
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
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "isBoundary",
        args = {int.class}
    )
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
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "last",
        args = {}
    )
    public void testLast() {
        assertEquals(0, iterator.last());
        iterator.setText(TEXT);
        assertEquals(TEXT.length(), iterator.last());
    }

    /*
     * Class under test for int next(int)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "next",
        args = {int.class}
    )
    public void testNextint() {
        assertEquals(BreakIterator.DONE, iterator.next(3));
        iterator.setText(TEXT);
        assertEquals(4, iterator.next(3));
        assertEquals(24, iterator.next(20));
        assertEquals(23, iterator.next(-1));
        assertEquals(-1, iterator.next(TEXT.length()));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "preceding",
        args = {int.class}
    )
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
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "previous",
        args = {}
    )
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
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getAvailableLocales",
        args = {}
    )
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
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getCharacterInstance",
        args = {}
    )
    public void testGetCharacterInstance() {
        BreakIterator.getCharacterInstance();
    }

    /*
     * Class under test for BreakIterator getCharacterInstance(Locale)
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Doesn't verify exception.",
        method = "getCharacterInstance",
        args = {java.util.Locale.class}
    )
    public void testGetCharacterInstanceLocale() {
        BreakIterator it = BreakIterator.getCharacterInstance(Locale.US);
        BreakIterator it2 = BreakIterator.getCharacterInstance(Locale.CHINA);
        assertEquals(it, it2);
    }

    /*
     * Class under test for BreakIterator getLineInstance()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getLineInstance",
        args = {}
    )
    public void testGetLineInstance() {
        BreakIterator it = BreakIterator.getLineInstance();
        assertNotNull(it);
    }

    /*
     * @tests java.text.BreakIterator#getLineInstance(Locale) Class under test
     * for BreakIterator getLineInstance(Locale)
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Doesn't verify exception.",
        method = "getLineInstance",
        args = {java.util.Locale.class}
    )
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
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getSentenceInstance",
        args = {}
    )
    public void testGetSentenceInstance() {
        BreakIterator it = BreakIterator.getSentenceInstance();
        assertNotNull(it);
    }

    /*
     * Class under test for BreakIterator getSentenceInstance(Locale)
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Doesn't verify exception.",
        method = "getSentenceInstance",
        args = {java.util.Locale.class}
    )
    public void testGetSentenceInstanceLocale() {
        BreakIterator it = BreakIterator.getSentenceInstance(Locale.US);
        assertNotNull(it);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getText",
        args = {}
    )
    public void testGetText() {
        assertEquals(new StringCharacterIterator(""), iterator.getText());
        iterator.setText(TEXT);
        assertEquals(new StringCharacterIterator(TEXT), iterator.getText());
    }

    /*
     * Class under test for BreakIterator getWordInstance()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getWordInstance",
        args = {}
    )
    public void testGetWordInstance() {
        BreakIterator it = BreakIterator.getWordInstance();
        assertNotNull(it);
    }

    /*
     * @tests java.text.BreakIterator#getWordInstance(Locale) Class under test
     * for BreakIterator getWordInstance(Locale)
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Doesn't verify exception.",
        method = "getWordInstance",
        args = {java.util.Locale.class}
    )
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
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setText",
        args = {java.text.CharacterIterator.class}
    )
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
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setText",
        args = {java.lang.String.class}
    )
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
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "next",
        args = {}
    )
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
     * @tests java.text.BreakIterator#getCharacterInstance(Locale)
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Verifies exception.",
        method = "getCharacterInstance",
        args = {java.util.Locale.class}
    )
    public void testGetCharacterInstanceLocale_NPE() {
        // Regression for HARMONY-265
        try {
            BreakIterator.getCharacterInstance(null);
            fail("BreakIterator.getCharacterInstance(null); should throw NullPointerException");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies exception.",
        method = "getLineInstance",
        args = {java.util.Locale.class}
    )
    public void testGetLineInstanceLocale_NPE() {
        try {
            BreakIterator.getLineInstance(null);
            fail("BreakIterator.getLineInstance(null); should throw NullPointerException");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies exception.",
        method = "getSentenceInstance",
        args = {java.util.Locale.class}
    )
    public void testGetSentenceInstanceLocale_NPE() {
        try {
            BreakIterator.getSentenceInstance(null);
            fail("BreakIterator.getSentenceInstance(null); should throw NullPointerException");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies exception.",
        method = "getWordInstance",
        args = {java.util.Locale.class}
    )
    public void testGetWordInstanceLocale_NPE() {
        try {
            BreakIterator.getWordInstance(null);
            fail("BreakIterator.getWordInstance(null); should throw NullPointerException");
        } catch (NullPointerException e) {
        }
    }
}
