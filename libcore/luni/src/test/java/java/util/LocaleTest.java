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

package java.util;

import junit.framework.Test;
import junit.framework.TestSuite;

public class LocaleTest extends junit.framework.TestCase {
    public void test_getDisplayName() throws Exception {
        // http://b/2611311; if there's no display language/country/variant, use the raw codes.
        Locale weird = new Locale("AaBbCc", "DdEeFf", "GgHhIi");
        assertEquals("aabbcc", weird.getLanguage());
        assertEquals("", weird.getDisplayLanguage());
        assertEquals("DDEEFF", weird.getCountry());
        assertEquals("", weird.getDisplayCountry());
        assertEquals("GgHhIi", weird.getVariant());
        assertEquals("", weird.getDisplayVariant());
        assertEquals("aabbcc (DDEEFF,GgHhIi)", weird.getDisplayName());
    }
}
