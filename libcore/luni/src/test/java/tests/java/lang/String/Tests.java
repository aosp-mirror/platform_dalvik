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

import dalvik.annotation.TestInfo;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTarget;
import dalvik.annotation.TestTargetClass;

import junit.framework.TestCase;

/**
 * Tests for the class {@link String}.
 */
@TestTargetClass(String.class) 
public class Tests extends TestCase {
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "Doesn't check NullPointerException.",
      targets = {
        @TestTarget(
          methodName = "contains",
          methodArgs = {java.lang.CharSequence.class}
        )
    })
    public void test_contains() {
        assertTrue("aabc".contains("abc"));
        assertTrue("abcd".contains("abc"));
        assertFalse("abcd".contains("cba"));
    }
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "Verifies positive functionality.",
      targets = {
        @TestTarget(
          methodName = "charAt",
          methodArgs = {int.class}
        )
    })
    public void test_charAt() {
        assertTrue("abcd".charAt(0) == 'a');
        assertTrue("abcd".charAt(3) == 'd');
    }
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "Doesn't check specific cases.",
      targets = {
        @TestTarget(
          methodName = "startsWith",
          methodArgs = {java.lang.String.class}
        )
    })
    public void test_StartsWith() {
        assertTrue("abcd".startsWith("abc"));
        assertFalse("abcd".startsWith("aabc"));
    }
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "Doesn't check specific cases.",
      targets = {
        @TestTarget(
          methodName = "endsWith",
          methodArgs = {java.lang.String.class}
        )
    })
    public void test_EndsWith() {
        assertTrue("abcd".endsWith("bcd"));
        assertFalse("abcd".endsWith("bcde"));
    }
    @TestInfo(
      level = TestLevel.TODO,
      purpose = "Verifies nothing.",
      targets = {
        @TestTarget(
          methodName = "!Constants",
          methodArgs = {}
        )
    })
    public void test_CASE_INSENSITIVE_ORDER() {
        String  s1 = "ABCDEFG";
        String  s2 = "abcdefg";
        
        assertTrue(String.CASE_INSENSITIVE_ORDER.compare(s1, s2) == 0);
    }
}
