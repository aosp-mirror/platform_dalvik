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

import java.text.Normalizer;
import java.text.Normalizer.Form;

import junit.framework.TestCase;

public class NormalizerTest extends TestCase {
    /**
     * @tests java.text.Normalizer.Form#values()
     */
    public void test_form_values() throws Exception {
        Form[] forms = Form.values();
        assertEquals(4, forms.length);
        assertEquals(Form.NFD, forms[0]);
        assertEquals(Form.NFC, forms[1]);
        assertEquals(Form.NFKD, forms[2]);
        assertEquals(Form.NFKC, forms[3]);
    }

    /**
     * @tests java.text.Normalizer.Form#valueOf(String)
     */
    public void test_form_valueOfLjava_lang_String() {
        try {
            Form.valueOf(null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }

        assertEquals(Form.NFC, Form.valueOf("NFC"));
        assertEquals(Form.NFD, Form.valueOf("NFD"));
        assertEquals(Form.NFKC, Form.valueOf("NFKC"));
        assertEquals(Form.NFKD, Form.valueOf("NFKD"));

        try {
            Form.valueOf("not exist");
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }

        try {
            Form.valueOf("nfc");
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }

        try {
            Form.valueOf("NFC ");
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    /**
     * @tests java.text.Normalizer#isNormalized(CharSequence, Form)
     */
    public void test_isNormalized() throws Exception {
        String src = "\u00c1";
        assertTrue(Normalizer.isNormalized(src, Form.NFC));
        assertFalse(Normalizer.isNormalized(src, Form.NFD));
        assertTrue(Normalizer.isNormalized(src, Form.NFKC));
        assertFalse(Normalizer.isNormalized(src, Form.NFKD));

        src = "\u0041\u0301";
        assertFalse(Normalizer.isNormalized(src, Form.NFC));
        assertTrue(Normalizer.isNormalized(src, Form.NFD));
        assertFalse(Normalizer.isNormalized(src, Form.NFKC));
        assertTrue(Normalizer.isNormalized(src, Form.NFKD));

        src = "\ufb03";
        assertTrue(Normalizer.isNormalized(src, Form.NFC));
        assertTrue(Normalizer.isNormalized(src, Form.NFD));
        assertFalse(Normalizer.isNormalized(src, Form.NFKC));
        assertFalse(Normalizer.isNormalized(src, Form.NFKD));

        src = "\u0066\u0066\u0069";
        assertTrue(Normalizer.isNormalized(src, Form.NFC));
        assertTrue(Normalizer.isNormalized(src, Form.NFD));
        assertTrue(Normalizer.isNormalized(src, Form.NFKC));
        assertTrue(Normalizer.isNormalized(src, Form.NFKD));

        src = "";
        assertTrue(Normalizer.isNormalized(src, Form.NFC));
        assertTrue(Normalizer.isNormalized(src, Form.NFD));
        assertTrue(Normalizer.isNormalized(src, Form.NFKC));
        assertTrue(Normalizer.isNormalized(src, Form.NFKD));
    }

    /**
     * @tests java.text.Normalizer#isNormalized(CharSequence, Form)
     */
    public void test_isNormalized_exception() throws Exception {
        try {
            Normalizer.isNormalized(null, Form.NFC);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }

        try {
            Normalizer.isNormalized("chars", null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    /**
     * @tests java.text.Normalizer#normalize(CharSequence, Form)
     */
    public void test_normalize() throws Exception {
        String src = "\u00c1";
        assertEquals("\u00c1", Normalizer.normalize(src, Form.NFC));
        assertEquals("\u0041\u0301", Normalizer.normalize(src, Form.NFD));
        assertEquals("\u00c1", Normalizer.normalize(src, Form.NFKC));
        assertEquals("\u0041\u0301", Normalizer.normalize(src, Form.NFKD));

        src = "\u0041\u0301";
        assertEquals("\u00c1", Normalizer.normalize(src, Form.NFC));
        assertEquals("\u0041\u0301", Normalizer.normalize(src, Form.NFD));
        assertEquals("\u00c1", Normalizer.normalize(src, Form.NFKC));
        assertEquals("\u0041\u0301", Normalizer.normalize(src, Form.NFKD));

        src = "\ufb03";
        assertEquals("\ufb03", Normalizer.normalize(src, Form.NFC));
        assertEquals("\ufb03", Normalizer.normalize(src, Form.NFD));
        assertEquals("\u0066\u0066\u0069", Normalizer.normalize(src, Form.NFKC));
        assertEquals("\u0066\u0066\u0069", Normalizer.normalize(src, Form.NFKD));

        src = "\u0066\u0066\u0069";
        assertEquals("\u0066\u0066\u0069", Normalizer.normalize(src, Form.NFC));
        assertEquals("\u0066\u0066\u0069", Normalizer.normalize(src, Form.NFD));
        assertEquals("\u0066\u0066\u0069", Normalizer.normalize(src, Form.NFKC));
        assertEquals("\u0066\u0066\u0069", Normalizer.normalize(src, Form.NFKD));

        src = "";
        assertEquals("", Normalizer.normalize(src, Form.NFC));
        assertEquals("", Normalizer.normalize(src, Form.NFD));
        assertEquals("", Normalizer.normalize(src, Form.NFKC));
        assertEquals("", Normalizer.normalize(src, Form.NFKD));
    }

    /**
     * @tests java.text.Normalizer#normalize(CharSequence, Form)
     */
    public void test_normalize_exception() throws Exception {
        try {
            Normalizer.normalize(null, Form.NFC);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }

        try {
            Normalizer.normalize("chars", null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }
}
