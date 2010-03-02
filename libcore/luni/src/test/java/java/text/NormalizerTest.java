/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package java.text;

public class NormalizerTest extends junit.framework.TestCase {
    public void testNormalize() {
        String src;
        String dst;
        String target;

        src = new String(new char[] {0x03D3, 0x03D4, 0x1E9B});
        // Should already be canonical composed
        dst = Normalizer.normalize(src, Normalizer.Form.NFC);
        assertTrue(src.equals(dst));

        // Composed to canonical decomposed
        target = new String(new char[] {0x03D2, 0x0301, 0x03D2, 0x0308, 0x017F, 0x0307});
        dst = Normalizer.normalize(src, Normalizer.Form.NFD);
        assertTrue(target.equals(dst));

        // Composed to compatibility composed
        target = new String(new char[] {0x038E, 0x3AB, 0x1E61});
        dst = Normalizer.normalize(src, Normalizer.Form.NFKC);
        assertTrue(target.equals(dst));

        // Composed to compatibility decomposed
        target = new String(new char[] {0x03A5, 0x0301, 0x03A5, 0x0308, 0x0073, 0x0307});
        dst = Normalizer.normalize(src, Normalizer.Form.NFKD);
        assertTrue(target.equals(dst));

        // Decomposed to canonical composed
        src = new String(new char[] {0x0065, 0x0301});
        target = new String(new char[] {0x00E9});
        dst = Normalizer.normalize(src, Normalizer.Form.NFC);
        assertTrue(target.equals(dst));

        // Decomposed to compatibility composed
        src = new String(new char[] {0x1E9B, 0x0323});
        target = new String(new char[] {0x1E69});
        dst = Normalizer.normalize(src, Normalizer.Form.NFKC);
        assertTrue(target.equals(dst));

        try {
            Normalizer.normalize(null, Normalizer.Form.NFC);
            fail("Did not throw error on null argument");
        } catch (NullPointerException e) {
            // pass
        }
    }

    public void testIsNormalized() {
        String target;

        target = new String(new char[] {0x03D3, 0x03D4, 0x1E9B});
        assertTrue(Normalizer.isNormalized(target, Normalizer.Form.NFC));
        assertFalse(Normalizer.isNormalized(target, Normalizer.Form.NFD));
        assertFalse(Normalizer.isNormalized(target, Normalizer.Form.NFKC));
        assertFalse(Normalizer.isNormalized(target, Normalizer.Form.NFKD));

        target = new String(new char[] {0x03D2, 0x0301, 0x03D2, 0x0308, 0x017F, 0x0307});
        assertFalse(Normalizer.isNormalized(target, Normalizer.Form.NFC));
        assertTrue(Normalizer.isNormalized(target, Normalizer.Form.NFD));
        assertFalse(Normalizer.isNormalized(target, Normalizer.Form.NFKC));
        assertFalse(Normalizer.isNormalized(target, Normalizer.Form.NFKD));

        target = new String(new char[] {0x038E, 0x03AB, 0x1E61});
        assertTrue(Normalizer.isNormalized(target, Normalizer.Form.NFC));
        assertFalse(Normalizer.isNormalized(target, Normalizer.Form.NFD));
        assertTrue(Normalizer.isNormalized(target, Normalizer.Form.NFKC));
        assertFalse(Normalizer.isNormalized(target, Normalizer.Form.NFKD));

        target = new String(new char[] {0x03A5, 0x0301, 0x03A5, 0x0308, 0x0073, 0x0307});
        assertFalse(Normalizer.isNormalized(target, Normalizer.Form.NFC));
        assertTrue(Normalizer.isNormalized(target, Normalizer.Form.NFD));
        assertFalse(Normalizer.isNormalized(target, Normalizer.Form.NFKC));
        assertTrue(Normalizer.isNormalized(target, Normalizer.Form.NFKD));

        try {
            Normalizer.isNormalized(null, Normalizer.Form.NFC);
            fail("Did not throw NullPointerException on null argument");
        } catch (NullPointerException e) {
            // pass
        }
    }
}
