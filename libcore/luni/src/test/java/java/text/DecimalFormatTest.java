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

package java.text;

import junit.framework.Test;
import junit.framework.TestSuite;

import java.math.BigInteger;
import java.util.Locale;

public class DecimalFormatTest extends junit.framework.TestCase {
    // Android fails this test, truncating to 127 digits.
    public void test_setMaximumIntegerDigits() throws Exception {
        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.US);
        numberFormat.setGroupingUsed(false);
        numberFormat.setMinimumIntegerDigits(400);
        // The RI's documentation suggests that the int should be formatted to 309 characters --
        // a magic number they don't explain -- but the BigInteger should be formatted to the 400
        // characters we asked for. In practice, the RI uses 309 in both cases.
        assertEquals(309, numberFormat.format(123).length());
        assertEquals(309, numberFormat.format(BigInteger.valueOf(123)).length());
    }
}
