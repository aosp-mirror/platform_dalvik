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

    // Creating a NumberFormat is expensive, so we like to reuse them, but we need to be careful
    // because they're mutable.
    public void test_NumberFormat_reuse() throws Exception {
        assertEquals("7.000000 7", String.format("%.6f %d", 7, 7));
    }

    public void test_formatNull() throws Exception {
        // We fast-path %s and %d (with no configuration) but need to make sure we handle the
        // special case of the null argument...
        assertEquals("null", String.format(Locale.US, "%s", null));
        assertEquals("null", String.format(Locale.US, "%d", null));
        // ...without screwing up conversions that don't take an argument.
        assertEquals("%", String.format(Locale.US, "%%"));
    }
}
