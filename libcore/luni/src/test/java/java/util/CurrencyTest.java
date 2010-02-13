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

public class CurrencyTest extends junit.framework.TestCase {
    // Regression test to ensure that Currency.getSymbol(Locale) returns the
    // currency code if ICU doesn't have a localization of the symbol. The
    // harmony Currency tests don't test this, and their DecimalFormat tests
    // only test it as a side-effect, and in a way that only detected my
    // specific mistake of returning null (returning "stinky" would have
    // passed).
    public void test_getSymbol_fallback() throws Exception {
        // This assumes that AED never becomes a currency important enough to
        // Canada that Canadians give it a localized (to Canada) symbol.
        assertEquals("AED", Currency.getInstance("AED").getSymbol(Locale.CANADA));
    }

    // Regression test to ensure that Currency.getInstance(String) throws if
    // given an invalid ISO currency code.
    public void test_getInstance_illegal_currency_code() throws Exception {
        Currency.getInstance("USD");
        try {
            Currency.getInstance("BOGO-DOLLARS");
            fail("expected IllegalArgumentException for invalid ISO currency code");
        } catch (IllegalArgumentException expected) {
        }
    }
}
