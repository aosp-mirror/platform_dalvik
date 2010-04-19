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

package java.math;

import junit.framework.Test;
import junit.framework.TestSuite;

import java.math.BigInteger;
import java.util.Locale;

public class BigIntegerTest extends junit.framework.TestCase {
    // http://code.google.com/p/android/issues/detail?id=7036
    public void test_invalidBigIntegerStringConversions() {
        // Check we don't disallow related reasonable strings...
        new BigInteger("1", 10);
        new BigInteger("1a", 16);
        new BigInteger("-1", 10);
        new BigInteger("-1a", 16);
        // This is allowed from Java 7 on.
        new BigInteger("+1");
        // Now check the invalid cases...
        try {
            new BigInteger("-a"); // no digits from other bases.
            fail();
        } catch (NumberFormatException expected) {
        }
        try {
            new BigInteger("-1a"); // no trailing digits from other bases.
            fail();
        } catch (NumberFormatException expected) {
        }
        try {
            new BigInteger("-1 hello"); // no trailing junk at all.
            fail();
        } catch (NumberFormatException expected) {
        }
        try {
            new BigInteger("--1"); // only one sign.
            fail();
        } catch (NumberFormatException expected) {
        }
        try {
            new BigInteger(""); // at least one digit.
            fail();
        } catch (NumberFormatException expected) {
        }
        try {
            new BigInteger("-"); // at least one digit, even after a sign.
            fail();
        } catch (NumberFormatException expected) {
        }
    }
}
