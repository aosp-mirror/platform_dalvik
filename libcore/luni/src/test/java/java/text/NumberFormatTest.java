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

public class NumberFormatTest extends junit.framework.TestCase {
    // NumberFormat.format(Object, StringBuffer, FieldPosition) guarantees it calls doubleValue for
    // custom Number subclasses.
    public void test_custom_Number_gets_longValue() throws Exception {
        class MyNumber extends Number {
            public byte byteValue() { throw new UnsupportedOperationException(); }
            public double doubleValue() { return 123; }
            public float floatValue() { throw new UnsupportedOperationException(); }
            public int intValue() { throw new UnsupportedOperationException(); }
            public long longValue() { throw new UnsupportedOperationException(); }
            public short shortValue() { throw new UnsupportedOperationException(); }
            public String toString() { throw new UnsupportedOperationException(); }
        }
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
        assertEquals("123", nf.format(new MyNumber()));
    }

    // NumberFormat.format(Object, StringBuffer, FieldPosition) guarantees it calls longValue for
    // any BigInteger with a bitLength strictly less than 64.
    public void test_small_BigInteger_gets_longValue() throws Exception {
        class MyNumberFormat extends NumberFormat {
            public StringBuffer format(double value, StringBuffer b, FieldPosition f) {
                b.append("double");
                return b;
            }
            public StringBuffer format(long value, StringBuffer b, FieldPosition f) {
                b.append("long");
                return b;
            }
            public Number parse(String string, ParsePosition p) {
                throw new UnsupportedOperationException();
            }
        }
        NumberFormat nf = new MyNumberFormat();
        assertEquals("long", nf.format(BigInteger.valueOf(Long.MAX_VALUE)));
        assertEquals("double", nf.format(BigInteger.valueOf(Long.MAX_VALUE).add(BigInteger.ONE)));
        assertEquals("long", nf.format(BigInteger.valueOf(Long.MIN_VALUE)));
        assertEquals("double", nf.format(BigInteger.valueOf(Long.MIN_VALUE).subtract(BigInteger.ONE)));
    }

    public void test_getIntegerInstance_ar() throws Exception {
        NumberFormat numberFormat = NumberFormat.getNumberInstance(new Locale("ar"));
        assertEquals("#,##0.###;#,##0.###-", ((DecimalFormat) numberFormat).toPattern());
        NumberFormat integerFormat = NumberFormat.getIntegerInstance(new Locale("ar"));
        assertEquals("#,##0;#,##0-", ((DecimalFormat) integerFormat).toPattern());
    }

    public void test_numberLocalization() throws Exception {
        Locale arabic = new Locale("ar");
        NumberFormat nf = NumberFormat.getNumberInstance(arabic);
        assertEquals('\u0660', new DecimalFormatSymbols(arabic).getZeroDigit());
        assertEquals("\u0661\u066c\u0662\u0663\u0664\u066c\u0665\u0666\u0667\u066c\u0668\u0669\u0660",
                nf.format(1234567890));
    }
}
