/* 
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.harmony.text.tests.java.text;

import java.text.CollationKey;
import java.text.Collator;
import java.text.ParseException;
import java.text.RuleBasedCollator;
import java.util.Arrays;

public class CollationKeyTest extends junit.framework.TestCase {

    /**
     * @tests java.text.CollationKey#compareTo(java.text.CollationKey)
     */
    public void test_compareToLjava_text_CollationKey() {
        Collator collator = Collator.getInstance();
        collator.setStrength(Collator.PRIMARY);
        CollationKey key1 = collator.getCollationKey("abc");
        CollationKey key2 = collator.getCollationKey("ABC");
        assertEquals("Should be equal", 0, key1.compareTo(key2));
    }

    /**
     * @tests java.text.CollationKey#compareTo(java.lang.Object)
     */
    public void test_compareToLjava_lang_Object() {
        // Test for method int
        // java.text.CollationKey.compareTo(java.lang.Object)
        Collator collator = Collator.getInstance();
        collator.setStrength(Collator.PRIMARY);
        CollationKey key1 = collator.getCollationKey("abc");
        CollationKey key2 = collator.getCollationKey("ABC");
        assertEquals("Should be equal", 0, key1.compareTo(key2));
    }

    /**
     * @tests java.text.CollationKey#equals(java.lang.Object)
     */
    public void test_equalsLjava_lang_Object() {
        Collator collator = Collator.getInstance();
        collator.setStrength(Collator.PRIMARY);
        CollationKey key1 = collator.getCollationKey("abc");
        CollationKey key2 = collator.getCollationKey("ABC");
        assertTrue("Should be equal", key1.equals(key2));
    }

    /**
     * @tests java.text.CollationKey#getSourceString()
     */
    public void test_getSourceString() {
        Collator collator = Collator.getInstance();
        collator.setStrength(Collator.PRIMARY);
        assertTrue("Wrong source string1", collator.getCollationKey("abc")
                .getSourceString() == "abc");
        assertTrue("Wrong source string2", collator.getCollationKey("ABC")
                .getSourceString() == "ABC");
    }

    /**
     * @tests java.text.CollationKey#hashCode()
     */
    public void test_hashCode() {
        Collator collator = Collator.getInstance();
        collator.setStrength(Collator.PRIMARY);
        CollationKey key1 = collator.getCollationKey("abc");
        CollationKey key2 = collator.getCollationKey("ABC");
        assertTrue("Should be equal", key1.hashCode() == key2.hashCode());
    }

    /**
     * @tests java.text.CollationKey#toByteArray()
     */
    // FIXME This test fails on Harmony ClassLibrary
    public void failing_test_toByteArray() {
        // Test for method byte [] java.text.CollationKey.toByteArray()
        Collator collator = Collator.getInstance();
        collator.setStrength(Collator.PRIMARY);
        CollationKey key1 = collator.getCollationKey("abc");
        byte[] bytes = key1.toByteArray();
        assertTrue("Not enough bytes", bytes.length >= 3);

        try {
            collator = new RuleBasedCollator("= 1 , 2 ; 3 , 4 < 5 ; 6 , 7");
        } catch (ParseException e) {
            fail("ParseException");
            return;
        }
        bytes = collator.getCollationKey("1234567").toByteArray();
        /*
         * CollationElementIterator it =
         * ((RuleBasedCollator)collator).getCollationElementIterator("1234567");
         * int order; while ((order = it.next()) !=
         * CollationElementIterator.NULLORDER) {
         * System.out.println(Integer.toHexString(order)); } for (int i=0; i<bytes.length;
         * i+=2) { System.out.print(Integer.toHexString(bytes[i]) +
         * Integer.toHexString(bytes[i+1]) + " "); } System.out.println();
         */
        byte[] result = new byte[] { 0, 2, 0, 2, 0, 2, 0, 0, 0, 3, 0, 3, 0, 1,
                0, 2, 0, 2, 0, 0, 0, 4, 0, 4, 0, 1, 0, 1, 0, 2 };
        assertTrue("Wrong bytes", Arrays.equals(bytes, result));
    }
}
