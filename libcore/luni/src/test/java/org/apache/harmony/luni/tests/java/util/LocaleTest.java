/* Licensed to the Apache Software Foundation (ASF) under one or more
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

package org.apache.harmony.luni.tests.java.util;

import java.util.Locale;

import junit.framework.TestCase;

public class LocaleTest extends TestCase {

    /**
     * @tests java.util.Locale#getAvailableLocales()
     */
    public void test_getAvailableLocales() {
        Locale[] locales = Locale.getAvailableLocales();
        // Assumes that there will be a decent number of locales
        // BEGIN android-changed
        // this assumption is wrong. Android has a reduced locale repository.
        // was >100, now it's >10
        assertTrue("Assert 0: Cannot find locales", locales.length > 10);
        // END android-changed
    }
}
