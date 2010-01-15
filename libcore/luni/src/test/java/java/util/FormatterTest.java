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

package java.util;

import junit.framework.Test;
import junit.framework.TestSuite;

public class FormatterTest extends junit.framework.TestCase {
    // http://b/2301938
    public void test_uppercaseConversions() throws Exception {
        // In most locales, the upper-case equivalent of "i" is "I".
        assertEquals("JAKOB ARJOUNI", String.format(Locale.US, "%S", "jakob arjouni"));
        // In Turkish-language locales, there's a dotted capital "i".
        assertEquals("JAKOB ARJOUN\u0130", String.format(new Locale("tr", "TR"), "%S", "jakob arjouni"));
    }

    public void test_formatNull() throws Exception {
        assertEquals("null", String.format(Locale.US, "%s", null));
    }
}
