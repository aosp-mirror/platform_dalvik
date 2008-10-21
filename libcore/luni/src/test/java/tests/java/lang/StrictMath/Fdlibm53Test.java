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

package tests.java.lang.StrictMath;

import junit.framework.TestCase;

public class Fdlibm53Test extends TestCase {
    public void test_pow() {
        assertTrue(Double.longBitsToDouble(-4610068591539890326L) == StrictMath.pow(-1.0000000000000002e+00,4.5035996273704970e+15));
        assertTrue(Double.longBitsToDouble(4601023824101950163L) == StrictMath.pow(-9.9999999999999978e-01,4.035996273704970e+15));
    }

    public void test_tan(){
        assertTrue(Double.longBitsToDouble(4850236541654588678L) == StrictMath.tan( 1.7765241907548024E+269));
    }
}
