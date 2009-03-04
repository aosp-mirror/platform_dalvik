/*
 * Copyright (C) 2007 The Android Open Source Project
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

package tests.java.lang.String;

import dalvik.annotation.TestTargets;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargetClass;

import junit.framework.TestCase;

/**
 * Tests for the class {@link String}.
 */
@TestTargetClass(String.class) 
public class Tests extends TestCase {
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Doesn't check NullPointerException.",
        method = "contains",
        args = {java.lang.CharSequence.class}
    )
    public void test_contains() {
        assertTrue("aabc".contains("abc"));
        assertTrue("abcd".contains("abc"));
        assertFalse("abcd".contains("cba"));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Verifies positive functionality.",
        method = "charAt",
        args = {int.class}
    )
    public void test_charAt() {
        assertTrue("abcd".charAt(0) == 'a');
        assertTrue("abcd".charAt(3) == 'd');
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Doesn't check specific cases.",
        method = "startsWith",
        args = {java.lang.String.class}
    )
    public void test_StartsWith() {
        assertTrue("abcd".startsWith("abc"));
        assertFalse("abcd".startsWith("aabc"));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Doesn't check specific cases.",
        method = "endsWith",
        args = {java.lang.String.class}
    )
    public void test_EndsWith() {
        assertTrue("abcd".endsWith("bcd"));
        assertFalse("abcd".endsWith("bcde"));
    }
    @TestTargetNew(
        level = TestLevel.TODO,
        notes = "Verifies nothing.",
        method = "!Constants",
        args = {}
    )
    public void test_CASE_INSENSITIVE_ORDER() {
        String  s1 = "ABCDEFG";
        String  s2 = "abcdefg";
        
        assertTrue(String.CASE_INSENSITIVE_ORDER.compare(s1, s2) == 0);
    }
}
