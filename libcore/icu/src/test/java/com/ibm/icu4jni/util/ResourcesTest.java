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

package com.ibm.icu4jni.util;

import java.util.Locale;

public class ResourcesTest extends junit.framework.TestCase {
    public void test_getISOLanguages() throws Exception {
        // Check that corrupting our array doesn't affect other callers.
        assertNotNull(Resources.getISOLanguages()[0]);
        Resources.getISOLanguages()[0] = null;
        assertNotNull(Resources.getISOLanguages()[0]);
    }

    public void test_getISOCountries() throws Exception {
        // Check that corrupting our array doesn't affect other callers.
        assertNotNull(Resources.getISOCountries()[0]);
        Resources.getISOCountries()[0] = null;
        assertNotNull(Resources.getISOCountries()[0]);
    }

    public void test_getAvailableLocales() throws Exception {
        // Check that corrupting our array doesn't affect other callers.
        assertNotNull(Resources.getAvailableLocales()[0]);
        Resources.getAvailableLocales()[0] = null;
        assertNotNull(Resources.getAvailableLocales()[0]);
    }

    public void test_getKnownTimezones() throws Exception {
        // Check that corrupting our array doesn't affect other callers.
        assertNotNull(Resources.getKnownTimezones()[0]);
        Resources.getKnownTimezones()[0] = null;
        assertNotNull(Resources.getKnownTimezones()[0]);
    }

    public void test_getDisplayTimeZones() throws Exception {
        // Check that corrupting our array doesn't affect other callers.
        assertNotNull(Resources.getDisplayTimeZones(null)[0]);
        Resources.getDisplayTimeZones(null)[0] = null;
        assertNotNull(Resources.getDisplayTimeZones(null)[0]);
        // getDisplayTimezones actually returns a String[][] rather than a String[].
        assertNotNull(Resources.getDisplayTimeZones(null)[0][0]);
        Resources.getDisplayTimeZones(null)[0][0] = null;
        assertNotNull(Resources.getDisplayTimeZones(null)[0][0]);
    }

    public void test_localeFromString() throws Exception {
        // localeFromString is pretty lenient. Some of these can't be round-tripped
        // through Locale.toString.
        assertEquals(Locale.ENGLISH, Resources.localeFromString("en"));
        assertEquals(Locale.ENGLISH, Resources.localeFromString("en_"));
        assertEquals(Locale.ENGLISH, Resources.localeFromString("en__"));
        assertEquals(Locale.US, Resources.localeFromString("en_US"));
        assertEquals(Locale.US, Resources.localeFromString("en_US_"));
        assertEquals(new Locale("", "US", ""), Resources.localeFromString("_US"));
        assertEquals(new Locale("", "US", ""), Resources.localeFromString("_US_"));
        assertEquals(new Locale("", "", "POSIX"), Resources.localeFromString("__POSIX"));
        assertEquals(new Locale("aa", "BB", "CC"), Resources.localeFromString("aa_BB_CC"));
    }
}
