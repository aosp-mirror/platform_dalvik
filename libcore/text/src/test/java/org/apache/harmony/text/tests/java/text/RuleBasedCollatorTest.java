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

import dalvik.annotation.TestInfo;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTarget;
import dalvik.annotation.TestTargetClass;

import junit.framework.TestCase;

import java.text.CharacterIterator;
import java.text.CollationElementIterator;
import java.text.CollationKey;
import java.text.Collator;
import java.text.ParseException;
import java.text.RuleBasedCollator;
import java.text.StringCharacterIterator;
import java.util.Locale;

@TestTargetClass(RuleBasedCollator.class) 
public class RuleBasedCollatorTest extends TestCase {

    /**
     * @tests java.text.RuleBasedCollator#RuleBasedCollator(String)
     */
    @TestInfo(
      level = TestLevel.COMPLETE,
      purpose = "",
      targets = {
        @TestTarget(
          methodName = "RuleBasedCollator",
          methodArgs = {java.lang.String.class}
        )
    })
    public void test_constrLRuleBasedCollatorLjava_lang_String() {
        RuleBasedCollator rbc;
        try {
            rbc = new RuleBasedCollator("<a< b< c< d");
            assertNotSame("RuleBasedCollator object is null", null, rbc);
        } catch (java.text.ParseException pe) {
            fail("java.text.ParseException is thrown for correct string");
        }

        try {
            rbc = new RuleBasedCollator("<a< '&'b< \u0301< d");
            assertNotSame("RuleBasedCollator object is null", null, rbc);
        } catch (java.text.ParseException pe) {
            fail("java.text.ParseException is thrown for correct string");
        }

        try {
            new RuleBasedCollator(null);
            fail("No Exception is thrown for correct string");
        } catch (java.text.ParseException pe) {
            fail("java.lang.NullPointerException is not thrown for correct string");
        } catch (java.lang.NullPointerException npe) {

        }

        try {
            new RuleBasedCollator("");
            fail("java.text.ParseException is not thrown for empty string");
        } catch (java.text.ParseException pe) {
        }

        try {
            new RuleBasedCollator("1234567%$#845");
            fail("java.text.ParseException is not thrown for wrong rules");
        } catch (java.text.ParseException pe) {
        }
    }
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "Regression test. Doesn't verify positive functionality.",
      targets = {
        @TestTarget(
          methodName = "getCollationKey",
          methodArgs = {java.lang.String.class}
        )
    })
    public void test_getCollationKeyLjava_lang_String() {
        // Regression test for HARMONY-28
        String source = null;
        RuleBasedCollator rbc = null;
        try {
            String Simple = "< a< b< c< d";
            rbc = new RuleBasedCollator(Simple);
        } catch (ParseException e) {
            fail("Assert 0: Unexpected format exception " + e);
        }
        CollationKey ck = rbc.getCollationKey(source);
        assertNull("Assert 1: getCollationKey (null) does not return null", ck);
    }
    @TestInfo(
      level = TestLevel.COMPLETE,
      purpose = "",
      targets = {
        @TestTarget(
          methodName = "hashCode",
          methodArgs = {}
        )
    })
    public void testHashCode() throws ParseException {
        {
            String rule = "< a < b < c < d";
            RuleBasedCollator coll = new RuleBasedCollator(rule);
            assertEquals(rule.hashCode(), coll.hashCode());
        }

        {
            String rule = "< a < b < c < d < e";
            RuleBasedCollator coll = new RuleBasedCollator(rule);
            assertEquals(rule.hashCode(), coll.hashCode());
        }

    }
    @TestInfo(
      level = TestLevel.COMPLETE,
      purpose = "",
      targets = {
        @TestTarget(
          methodName = "clone",
          methodArgs = {}
        )
    })
    public void testClone() throws ParseException {
        RuleBasedCollator coll = (RuleBasedCollator) Collator
                .getInstance(Locale.US);
        RuleBasedCollator clone = (RuleBasedCollator) coll.clone();
        assertNotSame(coll, clone);
        assertEquals(coll.getRules(), clone.getRules());
        assertEquals(coll.getDecomposition(), clone.getDecomposition());
        assertEquals(coll.getStrength(), clone.getStrength());
    }

    /*
     * Class under test for boolean equals(java.lang.Object)
     */
    @TestInfo(
      level = TestLevel.COMPLETE,
      purpose = "",
      targets = {
        @TestTarget(
          methodName = "equals",
          methodArgs = {java.lang.Object.class}
        )
    })
    public void testEqualsObject() throws ParseException {
        String rule = "< a < b < c < d < e";
        RuleBasedCollator coll = new RuleBasedCollator(rule);

        assertEquals(Collator.TERTIARY, coll.getStrength());
        assertEquals(Collator.NO_DECOMPOSITION, coll.getDecomposition());
        RuleBasedCollator other = new RuleBasedCollator(rule);
        assertTrue(coll.equals(other));

        coll.setStrength(Collator.PRIMARY);
        assertFalse(coll.equals(other));

        coll.setStrength(Collator.TERTIARY);
        coll.setDecomposition(Collator.CANONICAL_DECOMPOSITION);
        assertFalse(coll.equals(other));
    }

    /*
     * Class under test for int compare(java.lang.String, java.lang.String)
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "Doesn't verify positive case.",
      targets = {
        @TestTarget(
          methodName = "compare",
          methodArgs = {java.lang.String.class, java.lang.String.class}
        )
    })
    public void testCompareStringString() throws ParseException {
        String rule = "< c < b < a";
        RuleBasedCollator coll = new RuleBasedCollator(rule);
        assertEquals(-1, coll.compare("c", "a"));
    }
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "Doesn't verify null as a parameter.",
      targets = {
        @TestTarget(
          methodName = "getCollationKey",
          methodArgs = {java.lang.String.class}
        )
    })
    public void testGetCollationKey() {
        RuleBasedCollator coll = (RuleBasedCollator) Collator
                .getInstance(Locale.GERMAN);
        String source = "abc";
        CollationKey key1 = coll.getCollationKey(source);
        assertEquals(source, key1.getSourceString());
        String source2 = "abb";
        CollationKey key2 = coll.getCollationKey(source2);
        assertEquals(source2, key2.getSourceString());
        assertTrue(key1.compareTo(key2) > 0);
        assertTrue(coll.compare(source, source2) > 0);

    }
    @TestInfo(
      level = TestLevel.COMPLETE,
      purpose = "",
      targets = {
        @TestTarget(
          methodName = "getRules",
          methodArgs = {}
        )
    })
    public void testGetRules() throws ParseException {
        String rule = "< a = b < c";
        RuleBasedCollator coll = new RuleBasedCollator(rule);
        assertEquals(rule, coll.getRules());
    }

    /*
     * Class under test for java.text.CollationElementIterator
     * getCollationElementIterator(java.lang.String)
     */
    @TestInfo(
      level = TestLevel.COMPLETE,
      purpose = "",
      targets = {
        @TestTarget(
          methodName = "getCollationElementIterator",
          methodArgs = {java.lang.String.class}
        )
    })
    public void _testGetCollationElementIteratorString() throws Exception {
        {
            Locale locale = new Locale("es", "", "TRADITIONAL");
            RuleBasedCollator coll = (RuleBasedCollator) Collator
                    .getInstance(locale);
            String source = "cha";
            CollationElementIterator iterator = coll
                    .getCollationElementIterator(source);
            int[] e_offset = { 0, 2, 3 };
            int offset = iterator.getOffset();
            int i = 0;
            assertEquals(e_offset[i++], offset);
            while (offset != source.length()) {
                iterator.next();
                offset = iterator.getOffset();
                assertEquals(e_offset[i++], offset);
            }
        }

        {
            Locale locale = new Locale("de", "DE");
            RuleBasedCollator coll = (RuleBasedCollator) Collator
                    .getInstance(locale);
            String source = "\u00E6b";
            CollationElementIterator iterator = coll
                    .getCollationElementIterator(source);
            int[] e_offset = { 0, 1, 1, 2 };
            int offset = iterator.getOffset();
            int i = 0;
            assertEquals(e_offset[i++], offset);
            while (offset != source.length()) {
                iterator.next();
                offset = iterator.getOffset();
                assertEquals(e_offset[i++], offset);
            }
        }
        // Regression for HARMONY-1352
        try {
            new RuleBasedCollator("< a< b< c< d").getCollationElementIterator((String)null);
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
            // expected
        }
    }

    /*
     * Class under test for java.text.CollationElementIterator
     * getCollationElementIterator(java.text.CharacterIterator)
     */
    @TestInfo(
      level = TestLevel.COMPLETE,
      purpose = "",
      targets = {
        @TestTarget(
          methodName = "getCollationElementIterator",
          methodArgs = {java.text.CharacterIterator.class}
        )
    })
    public void _testGetCollationElementIteratorCharacterIterator() throws Exception {
        {
            Locale locale = new Locale("es", "", "TRADITIONAL");
            RuleBasedCollator coll = (RuleBasedCollator) Collator
                    .getInstance(locale);
            String text = "cha";
            StringCharacterIterator source = new StringCharacterIterator(text);
            CollationElementIterator iterator = coll
                    .getCollationElementIterator(source);
            int[] e_offset = { 0, 2, 3 };
            int offset = iterator.getOffset();
            int i = 0;
            assertEquals(e_offset[i++], offset);
            while (offset != text.length()) {
                iterator.next();
                offset = iterator.getOffset();
                // System.out.println(offset);
                assertEquals(e_offset[i++], offset);
            }
        }

        {
            Locale locale = new Locale("de", "DE");
            RuleBasedCollator coll = (RuleBasedCollator) Collator
                    .getInstance(locale);
            String text = "\u00E6b";
            StringCharacterIterator source = new StringCharacterIterator(text);
            CollationElementIterator iterator = coll
                    .getCollationElementIterator(source);
            int[] e_offset = { 0, 1, 1, 2 };
            int offset = iterator.getOffset();
            int i = 0;
            assertEquals(e_offset[i++], offset);
            while (offset != text.length()) {
                iterator.next();
                offset = iterator.getOffset();
                assertEquals(e_offset[i++], offset);
            }
        }
        // Regression for HARMONY-1352
        try {
            new RuleBasedCollator("< a< b< c< d").getCollationElementIterator((CharacterIterator)null);
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
            // expected
        }
    }
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "Doesn't verify setStrength method with PRIMARY, SECONDARY, " +
            "TERTIARY or IDENTICAL values as a parameter; doesn't verify that" +
            "setStrength method can throw IllegalArgumentException.",
      targets = {
        @TestTarget(
          methodName = "setStrength",
          methodArgs = {int.class}
        ),
        @TestTarget(
          methodName = "getStrength",
          methodArgs = {}
        )        
    })
    public void testStrength() {
        RuleBasedCollator coll = (RuleBasedCollator) Collator
                .getInstance(Locale.US);
        for (int i = 0; i < 4; i++) {
            coll.setStrength(i);
            assertEquals(i, coll.getStrength());
        }

    }
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "Doesn't verify NO_DECOMPOSITION, CANONICAL_DECOMPOSITION, " +
            "FULL_DECOMPOSITION.",
      targets = {
        @TestTarget(
          methodName = "setDecomposition",
          methodArgs = {int.class}
        ),
        @TestTarget(
          methodName = "getDecomposition",
          methodArgs = {}
        )
    })
    public void testDecomposition() {
        RuleBasedCollator coll = (RuleBasedCollator) Collator
                .getInstance(Locale.US);
        for (int i = 0; i < 2; i++) {
            coll.setDecomposition(i);
            assertEquals(i, coll.getDecomposition());
        }
    }
    @TestInfo(
      level = TestLevel.COMPLETE,
      purpose = "",
      targets = {
        @TestTarget(
          methodName = "getInstance",
          methodArgs = {}
        )
    })
    public void testCollator_GetInstance() {
        Collator coll = Collator.getInstance();
        Object obj1 = "a";
        Object obj2 = "b";
        assertEquals(-1, coll.compare(obj1, obj2));

        Collator.getInstance();
        assertFalse(coll.equals("A", "\uFF21"));
    }
    @TestInfo(
      level = TestLevel.TODO,
      purpose = "Empty test.",
      targets = {
        @TestTarget(
          methodName = "getAvailableLocales",
          methodArgs = {}
        )
    })
    public void testGetAvaiableLocales() {
        // Locale[] locales = Collator.getAvailableLocales();
        // for (int i = 0; i < locales.length; i++) {
        // Locale locale = locales[i];
        // }
    }

    // Test CollationKey
    @TestInfo(
      level = TestLevel.COMPLETE,
      purpose = "",
      targets = {
        @TestTarget(
          methodName = "getCollationKey",
          methodArgs = {java.lang.String.class}
        )
    })
    public void testCollationKey() {
        Collator coll = Collator.getInstance(Locale.US);
        String text = "abc";
        CollationKey key = coll.getCollationKey(text);
        key.hashCode();

        CollationKey key2 = coll.getCollationKey("abc");

        assertEquals(0, key.compareTo(key2));
    }

    /**
     * @tests java.text.RuleBasedCollator.RuleBasedCollator(java.lang.String)
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "Verifies RuleBasedCollator(java.lang.String) constructor with " +
            "null as a parameter.",
      targets = {
        @TestTarget(
          methodName = "RuleBasedCollator",
          methodArgs = {java.lang.String.class}
        )
    })
    public void testNullPointerException() throws Exception {
        // Regression for HARMONY-241
        try {
            new RuleBasedCollator(null);
            fail("Constructor RuleBasedCollator(null) "
                    + "should throw NullPointerException");
        } catch (NullPointerException e) {}
    }

    /**
     * @tests java.text.RuleBasedCollator.compare(java.lang.String, java.lang.String)
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "Verifies null as parameters.",
      targets = {
        @TestTarget(
          methodName = "compare",
          methodArgs = {java.lang.String.class, java.lang.String.class}
        )
    })
    public void testCompareNull() throws Exception {
        // Regression for HARMONY-836
        try {
            new RuleBasedCollator("< a").compare(null, null);
            fail("RuleBasedCollator.compare(null, null) "
                    + "should throw NullPointerException");
        } catch (NullPointerException e) {}
    }

    /**
     * @tests java.text.RuleBasedCollator.RuleBasedCollator(java.lang.String)
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "Verifies empty string as a parameter.",
      targets = {
        @TestTarget(
          methodName = "RuleBasedCollator",
          methodArgs = {java.lang.String.class}
        )
    })
    public void testEmptyStringException() {
        // Regression for HARMONY-241
        try {
            new RuleBasedCollator("");
            fail("Constructor RuleBasedCollator(\"\") "
                    + "should throw ParseException");
        } catch (ParseException e) {
            assertEquals("java.text.ParseException", e.getClass().getName());
            assertEquals(0, e.getErrorOffset());
        }
    }

}
