/*
 * Copyright (C) 2009 The Android Open Source Project
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

package java.lang;

import junit.framework.Test;
import junit.framework.TestSuite;

public class FloatTest extends junit.framework.TestCase {
    public void test_valueOf_String1() throws Exception {
        // This threw OutOfMemoryException.
        // http://code.google.com/p/android/issues/detail?id=4185
        assertEquals(2358.166016f, Float.valueOf("2358.166016"));
    }
    public void test_valueOf_String2() throws Exception {
        // This threw OutOfMemoryException.
        // http://code.google.com/p/android/issues/detail?id=3156
        assertEquals(-2.14748365E9f, Float.valueOf(String.valueOf(Integer.MIN_VALUE)));
    }
}
