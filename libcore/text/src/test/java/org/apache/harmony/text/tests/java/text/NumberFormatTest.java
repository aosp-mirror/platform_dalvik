/* 
 * Licensed to the Apache Software Foundation (ASF) under one or more
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
package org.apache.harmony.text.tests.java.text;

import java.text.ChoiceFormat;
import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Currency;
import java.util.Locale;
import junit.framework.TestCase;

public class NumberFormatTest extends TestCase {

    /**
     * @tests java.text.NumberFormat#format(java.lang.Object,
     *        java.lang.StringBuffer, java.text.FieldPosition)
     */
    public void test_formatLjava_lang_ObjectLjava_lang_StringBufferLjava_text_FieldPosition() {
        FieldPosition pos;
        StringBuffer out;
        DecimalFormat format = (DecimalFormat) NumberFormat
                .getInstance(Locale.US);

        pos = new FieldPosition(0);
        out = format.format(new Long(Long.MAX_VALUE), new StringBuffer(), pos);
        assertEquals("Wrong result L1: " + out, "9,223,372,036,854,775,807",
                out.toString());

        pos = new FieldPosition(0);
        out = format.format(new Long(Long.MIN_VALUE), new StringBuffer(), pos);
        assertEquals("Wrong result L2: " + out, "-9,223,372,036,854,775,808",
                out.toString());

        pos = new FieldPosition(0);
        out = format.format(new java.math.BigInteger(String
                .valueOf(Long.MAX_VALUE)), new StringBuffer(), pos);
        assertEquals("Wrong result BI1: " + out, "9,223,372,036,854,775,807",
                out.toString());

        pos = new FieldPosition(0);
        out = format.format(new java.math.BigInteger(String
                .valueOf(Long.MIN_VALUE)), new StringBuffer(), pos);
        assertEquals("Wrong result BI2: " + out, "-9,223,372,036,854,775,808",
                out.toString());

        java.math.BigInteger big;
        pos = new FieldPosition(0);
        big = new java.math.BigInteger(String.valueOf(Long.MAX_VALUE))
                .add(new java.math.BigInteger("1"));
        out = format.format(big, new StringBuffer(), pos);
        assertEquals("Wrong result BI3: " + out, "9,223,372,036,854,775,808",
                out.toString());

        pos = new FieldPosition(0);
        big = new java.math.BigInteger(String.valueOf(Long.MIN_VALUE))
                .add(new java.math.BigInteger("-1"));
        out = format.format(big, new StringBuffer(), pos);
        assertEquals("Wrong result BI4: " + out, "-9,223,372,036,854,775,809",
                out.toString());

        pos = new FieldPosition(0);
        out = format.format(new java.math.BigDecimal("51.348"),
                new StringBuffer(), pos);
        assertEquals("Wrong result BD1: " + out, "51.348", out.toString());

        pos = new FieldPosition(0);
        out = format.format(new java.math.BigDecimal("51"), new StringBuffer(),
                pos);
        assertEquals("Wrong result BD2: " + out, "51", out.toString());

    }

    /**
     * @tests java.text.NumberFormat#getIntegerInstance()
     */
    public void test_getIntegerInstance() throws ParseException {
        // Test for method java.text.NumberFormat getIntegerInstance()
        Locale origLocale = Locale.getDefault();
        Locale.setDefault(Locale.US);

        DecimalFormat format = (DecimalFormat) NumberFormat
                .getIntegerInstance();

        assertEquals(
                "Test1: NumberFormat.getIntegerInstance().toPattern() returned wrong pattern",
                "#,##0", format.toPattern());
        assertEquals(
                "Test2: NumberFormat.getIntegerInstance().format(35.76) returned wrong value",
                "36", format.format(35.76));
        assertEquals(
                "Test3: NumberFormat.getIntegerInstance().parse(\"35.76\") returned wrong number",
                new Long(35), format.parse("35.76"));
        assertEquals(
                "Test4: NumberFormat.getIntegerInstance().parseObject(\"35.76\") returned wrong number",
                new Long(35), format.parseObject("35.76"));
        Locale.setDefault(origLocale);
    }

    /**
     * @tests java.text.NumberFormat#getIntegerInstance(java.util.Locale)
     */
    public void test_getIntegerInstanceLjava_util_Locale()
            throws ParseException {
        // Test for method java.text.NumberFormat
        // getIntegerInstance(java.util.Locale)
        Locale usLocale = Locale.US;
        // BEGIN android-changed
        // use de_CH instead
        // Locale arLocale = new Locale("ar", "AE");
        Locale chLocale = new Locale("de", "CH");
        // END android-changed

        DecimalFormat format = (DecimalFormat) NumberFormat
                .getIntegerInstance(usLocale);
        assertEquals(
                "Test1: NumberFormat.getIntegerInstance().toPattern() returned wrong pattern",
                "#,##0", format.toPattern());
        assertEquals(
                "Test2: NumberFormat.getIntegerInstance().format(-35.76) returned wrong value",
                "-36", format.format(-35.76));
        assertEquals(
                "Test3: NumberFormat.getIntegerInstance().parse(\"-36\") returned wrong number",
                new Long(-36), format.parse("-36"));
        assertEquals(
                "Test4: NumberFormat.getIntegerInstance().parseObject(\"-36\") returned wrong number",
                new Long(-36), format.parseObject("-36"));
        assertEquals(
                "Test5: NumberFormat.getIntegerInstance().getMaximumFractionDigits() returned wrong value",
                0, format.getMaximumFractionDigits());
        assertTrue(
                "Test6: NumberFormat.getIntegerInstance().isParseIntegerOnly() returned wrong value",
                format.isParseIntegerOnly());

        // try with a locale that has a different integer pattern
        // BEGIN android-changed
        // use de_CH instead
        // format = (DecimalFormat) NumberFormat.getIntegerInstance(arLocale);
        format = (DecimalFormat) NumberFormat.getIntegerInstance(chLocale);
        assertEquals(
                "Test7: NumberFormat.getIntegerInstance(new Locale(\"de\", \"CH\")).toPattern() returned wrong pattern",
                "#,##0", format.toPattern());
        assertEquals(
                "Test8: NumberFormat.getIntegerInstance(new Locale(\"de\", \"CH\")).format(-35.76) returned wrong value",
                "-36", format.format(-35.76));
        assertEquals(
                "Test9: NumberFormat.getIntegerInstance(new Locale(\"de\", \"CH\")).parse(\"-36-\") returned wrong number",
                new Long(-36), format.parse("-36"));
        assertEquals(
                "Test10: NumberFormat.getIntegerInstance(new Locale(\"de\", \"CH\")).parseObject(\"36-\") returned wrong number",
                new Long(-36), format.parseObject("-36"));

        assertEquals(
                "Test11: NumberFormat.getIntegerInstance(new Locale(\"de\", \"CH\")).getMaximumFractionDigits() returned wrong value",
                0, format.getMaximumFractionDigits());
        assertTrue(
                "Test12: NumberFormat.getIntegerInstance(new Locale(\"de\", \"CH\")).isParseIntegerOnly() returned wrong value",
                format.isParseIntegerOnly());
        // use de_CH instead
        /*assertEquals(
                "Test7: NumberFormat.getIntegerInstance(new Locale(\"ar\", \"AE\")).toPattern() returned wrong pattern",
                "#,##0;#,##0-", format.toPattern());
        assertEquals(
                "Test8: NumberFormat.getIntegerInstance(new Locale(\"ar\", \"AE\")).format(-35.76) returned wrong value",
                "36-", format.format(-35.76));
        assertEquals(
                "Test9: NumberFormat.getIntegerInstance(new Locale(\"ar\", \"AE\")).parse(\"-36-\") returned wrong number",
                new Long(-36), format.parse("36-"));
        assertEquals(
                "Test10: NumberFormat.getIntegerInstance(new Locale(\"ar\", \"AE\")).parseObject(\"36-\") returned wrong number",
                new Long(-36), format.parseObject("36-"));

        assertEquals(
                "Test11: NumberFormat.getIntegerInstance(new Locale(\"ar\", \"AE\")).getMaximumFractionDigits() returned wrong value",
                0, format.getMaximumFractionDigits());
        assertTrue(
                "Test12: NumberFormat.getIntegerInstance(new Locale(\"ar\", \"AE\")).isParseIntegerOnly() returned wrong value",
                format.isParseIntegerOnly());*/
        // END android-changed
    }

    /**
     * @tests java.text.NumberFormat#getCurrency()
     */
    public void test_getCurrency() {
        // Test for method java.util.Currency getCurrency()

        // a subclass that supports currency formatting
        Currency currH = Currency.getInstance("HUF");
        NumberFormat format = NumberFormat.getInstance(new Locale("hu", "HU"));
        assertSame("Returned incorrect currency", currH, format.getCurrency());

        // a subclass that doesn't support currency formatting
        ChoiceFormat cformat = new ChoiceFormat(
                "0#Less than one|1#one|1<Between one and two|2<Greater than two");
        try {
            ((NumberFormat) cformat).getCurrency();
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
        }
    }

    /**
     * @tests java.text.NumberFormat#setMaximumIntegerDigits()
     */
    public void test_setMaximumIntegerDigits() {
        NumberFormat format = NumberFormat.getInstance();
        format.setMaximumIntegerDigits(2);
        assertEquals("Wrong result: case 1", "23", format.format(123));

        format.setMaximumIntegerDigits(Integer.MIN_VALUE);
        assertEquals("Wrong result: case 2", "0", format.format(123));
    }

    /**
     * @tests java.text.NumberFormat#setCurrency(java.util.Currency)
     */
    public void test_setCurrencyLjava_util_Currency() {
        // Test for method void setCurrency(java.util.Currency)
        // a subclass that supports currency formatting
        Currency currA = Currency.getInstance("ARS");
        NumberFormat format = NumberFormat.getInstance(new Locale("hu", "HU"));
        format.setCurrency(currA);
        assertSame("Returned incorrect currency", currA, format.getCurrency());

        // a subclass that doesn't support currency formatting
        ChoiceFormat cformat = new ChoiceFormat(
                "0#Less than one|1#one|1<Between one and two|2<Greater than two");
        try {
            ((NumberFormat) cformat).setCurrency(currA);
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
        }
    }

    /**
     * @tests java.text.NumberFormat#parseObject(java.lang.String,
     *        java.text.ParsePosition)
     */
    public void test_parseObjectLjava_lang_StringLjava_text_ParsePosition() {
        // regression test for HARMONY-1003
        assertNull(NumberFormat.getInstance().parseObject("0",
                new ParsePosition(-1)));

        // Regression for HARMONY-1685
        try {
            NumberFormat.getInstance().parseObject("test", null);
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
            // expected
        }
    }

    /**
     * @tests java.text.NumberFormat#clone()
     */
    public void test_clone() {

        int max_digits = 100;
        NumberFormat nf1 = NumberFormat.getInstance();
        nf1.setMaximumIntegerDigits(max_digits);

        NumberFormat nf2 = (NumberFormat) nf1.clone();
        NumberFormat nf3 = (NumberFormat) nf1.clone();

        assertTrue("Clonned object is not equal to object", nf2.equals(nf1));
        assertTrue("Two clonned objects are not equal", nf2.equals(nf3));

        assertTrue("Max digits value is incorrect for clonned object", nf2
                .getMaximumIntegerDigits() == max_digits);

        nf1.setMaximumIntegerDigits(10);
        assertTrue(
                "Max digits value is incorrect for clonned object after changing this value for object",
                nf2.getMaximumIntegerDigits() == max_digits);
    }

    /**
     * @tests java.text.NumberFormat#equals(Object)
     */
    public void test_equals() {

        NumberFormat nf1 = NumberFormat.getInstance();
        NumberFormat nf2 = NumberFormat.getInstance();

        assertTrue("Objects are not equal", nf1.equals(nf2));
        assertTrue("THe same Objects are not equal", nf1.equals(nf1));

        nf2.setMaximumIntegerDigits(100);
        assertFalse("Different NumberFormat are equal", nf1.equals(nf2));

        nf2.setMaximumIntegerDigits(nf1.getMaximumIntegerDigits());
        assertTrue("THe same Objects are not equal", nf1.equals(nf2));

        nf1 = NumberFormat.getIntegerInstance();
        nf2 = NumberFormat.getIntegerInstance(Locale.CHINA);
        assertFalse("Different NumberFormat are equal", nf1.equals(nf2));

        assertFalse("Object is equal null", nf1.equals(null));
    }

    /**
     * @tests java.text.NumberFormat#format(double)
     */
    public void test_formatLdouble() {
        // BEGIN android-changed
        NumberFormat nf1 = NumberFormat.getInstance(Locale.US);
        // use de_CH instead
        // NumberFormat nf2 = NumberFormat.getInstance(new Locale("ar", "AR"));
        NumberFormat nf2 = NumberFormat.getInstance(new Locale("de", "CH"));

        String out = nf1.format(1234567890.0123456789);
        assertEquals("Wrong result for double : " + out, "1,234,567,890.012",
                out.toString());

        out = nf1.format(-1234567890.0123456789);
        assertEquals("Wrong result for double : " + out, "-1,234,567,890.012",
                out.toString());

        out = nf2.format(-1234567890.0123456789);
        // use de_CH instead
        // assertEquals("Wrong result for double : " + out, "1,234,567,890.012-",
        //         out.toString());
        assertEquals("Wrong result for double : " + out, "-1'234'567'890.012",
                out.toString());

        out = nf1.format(1.0001);
        assertEquals("Wrong result for for double: " + out, "1", out.toString());

        out = nf1.format(5.0);
        assertEquals("Wrong result for for double: " + out, "5", out.toString());
        // END android-changed
    }

    /**
     * @tests java.text.NumberFormat#format(long)
     */
    public void test_formatLlong() {
        // BEGIN android-changed
        NumberFormat nf1 = NumberFormat.getInstance(Locale.US);
        // use de_CH instead
        // NumberFormat nf2 = NumberFormat.getInstance(Locale.CANADA_FRENCH);
        NumberFormat nf2 = NumberFormat.getInstance(new Locale("de", "CH"));

        String out = nf1.format(Long.MAX_VALUE);
        assertEquals("Wrong result for double : " + out,
                "9,223,372,036,854,775,807", out.toString());

        out = nf1.format(Long.MIN_VALUE);
        assertEquals("Wrong result for double : " + out,
                "-9,223,372,036,854,775,808", out.toString());

        out = nf2.format(-1234567890);
        // use de_CH instead
        // assertEquals("Wrong result for double : " + out, "-1 234 567 890", out
        //         .toString());
        assertEquals("Wrong result for double : " + out, "-1'234'567'890", out
                .toString());

        // the Locale data of icu uses \uc2a0
        out = nf1.format(1);
        assertEquals("Wrong result for for double: " + out, "1", out.toString());

        out = nf1.format(0);
        assertEquals("Wrong result for for double: " + out, "0", out.toString());
        // END android-changed
    }

    /**
     * @tests java.text.NumberFormat#getAvailableLocales()
     */
    public void test_getAvailableLocales() {

        Locale[] l = NumberFormat.getAvailableLocales();
        assertFalse("returned Locale array is null", l == null);
        assertTrue("returned Locale length <= 0", l.length > 0);
        Locale[] resl = Locale.getAvailableLocales();
        assertTrue("returned Locale arrays are different",
                l.length == resl.length);
        boolean isUS = false;
        for (int i = 0; i < resl.length; i++) {
            assertEquals("elements " + i + " are not equal: ", resl[i], l[i]);
            if (l[i].equals(Locale.US))
                isUS = true;
        }
        assertTrue("there is no Locale.US", isUS);
    }

    /**
     * @tests java.text.NumberFormat#getCurrencyInstance()
     */
    public void test_getCurrencyInstance() {

        NumberFormat format = NumberFormat.getCurrencyInstance();

        assertNotSame("Instance is null", null, format);
        assertTrue("Object is not instance of NumberFormat",
                format instanceof NumberFormat);

        assertEquals(
                "Test1: NumberFormat.getCurrencyInstance().format(35.76) returned wrong value",
                "$35.76", format.format(35.76));
        assertEquals(
                "Test2: NumberFormat.getCurrencyInstance().format(123456.789) returned wrong value",
                "$123,456.79", format.format(123456.789));
        assertEquals(
                "Test3: NumberFormat.getCurrencyInstance().format(0.1) returned wrong value",
                "$0.10", format.format(0.1));
        assertEquals(
                "Test4: NumberFormat.getCurrencyInstance().format(0.999) returned wrong value",
                "$1.00", format.format(0.999));
    }

    /**
     * @tests java.text.NumberFormat#getCurrencyInstance(java.util.Locale)
     */
    public void test_getCurrencyInstanceLjava_util_Locale() {
        // BEGIN android-changed
        Locale usLocale = Locale.US;
        // use de_AT instead
        // Locale mkLocale = new Locale("mk", "MK");
        Locale atLocale = new Locale("de", "AT");

        NumberFormat format = NumberFormat.getCurrencyInstance(usLocale);

        assertNotSame("Instance is null", null, format);
        assertTrue("Object is not instance of NumberFormat",
                format instanceof NumberFormat);

        assertEquals(
                "Test1: NumberFormat.getCurrencyInstance(Locale.US).format(35.76) returned wrong value",
                "$35.76", format.format(35.76));
        assertEquals(
                "Test2: NumberFormat.getCurrencyInstance(Locale.US).format(123456.789) returned wrong value",
                "$123,456.79", format.format(123456.789));
        assertEquals(
                "Test3: NumberFormat.getCurrencyInstance(Locale.US).format(0.1) returned wrong value",
                "$0.10", format.format(0.1));
        assertEquals(
                "Test4: NumberFormat.getCurrencyInstance(Locale.US).format(0.999) returned wrong value",
                "$1.00", format.format(0.999));

        // use de_AT instead
        // format = NumberFormat.getCurrencyInstance(mkLocale);
        format = NumberFormat.getCurrencyInstance(atLocale);
        
        assertEquals(
                "Test5: NumberFormat.getCurrencyInstance(new Locale(\"de\", \"AT\")).format(35.76) returned wrong value",
                "\u20ac 35,76", format.format(35.76));
        assertEquals(
                "Test6: NumberFormat.getCurrencyInstance(new Locale(\"de\", \"AT\")).format(123456.789) returned wrong value",
                "\u20ac 123.456,79", format.format(123456.789));
        assertEquals(
                "Test7: NumberFormat.getCurrencyInstance(new Locale(\"de\", \"AT\")).format(0.1) returned wrong value",
                "\u20ac 0,10", format.format(0.1));
        assertEquals(
                "Test8: NumberFormat.getCurrencyInstance(new Locale(\"de\", \"AT\")).format(0.999) returned wrong value",
                "\u20ac 1,00", format.format(0.999));
        // use de_AT instead
        /*assertEquals(
                "Test5: NumberFormat.getCurrencyInstance(new Locale(\"mk\", \"MK\")).format(35.76) returned wrong value",
                "Den 35,76", format.format(35.76));
        assertEquals(
                "Test6: NumberFormat.getCurrencyInstance(new Locale(\"mk\", \"MK\")).format(123456.789) returned wrong value",
                "Den 123.456,79", format.format(123456.789));
        assertEquals(
                "Test7: NumberFormat.getCurrencyInstance(new Locale(\"mk\", \"MK\")).format(0.1) returned wrong value",
                "Den 0,1", format.format(0.1));
        assertEquals(
                "Test8: NumberFormat.getCurrencyInstance(new Locale(\"mk\", \"MK\")).format(0.999) returned wrong value",
                "Den 1", format.format(0.999));*/
        try {
            NumberFormat.getCurrencyInstance(null);
            fail("java.lang.NullPointerException is not thrown");
        } catch (java.lang.NullPointerException npe) {
            // expested
        }
        // END android-changed
    }

    /**
     * @tests java.text.NumberFormat#getInstance()
     */
    public void test_getInstance() {
        Locale.setDefault(Locale.US);
        NumberFormat format = NumberFormat.getInstance();

        assertNotSame("Instance is null", null, format);
        assertTrue("Object is not instance of NumberFormat",
                format instanceof NumberFormat);

        assertEquals(
                "Test1: NumberFormat.getInstance().format(1234567890.0987654321) returned wrong value",
                "1,234,567,890.099", format.format(1234567890.0987654321));
        assertEquals(
                "Test2: ((DecimalFormat) NumberFormat.getInstance()).toPattern returned wrong value",
                "#,##0.###", ((DecimalFormat) format).toPattern());
        assertEquals(
                "Test3: NumberFormat.getInstance().format(123456789) returned wrong value",
                "123,456,789", format.format(123456789));
    }

    /**
     * @tests java.text.NumberFormat#getInstance(Locale)
     */
    public void test_getInstanceLjava_util_Locale() {
        // BEGIN android-changed
        Locale.setDefault(Locale.US);
        // use de_CH instead
        // NumberFormat format = NumberFormat.getInstance(new Locale("ar", "AR"));
        NumberFormat format = NumberFormat.getInstance(new Locale("de", "CH"));

        assertNotSame("Instance is null", null, format);
        assertTrue("Object is not instance of NumberFormat",
                format instanceof NumberFormat);

        assertEquals(
                "Test1: NumberFormat.getInstance().format(1234567890.0987654321) returned wrong value",
                "1'234'567'890.099", format.format(1234567890.0987654321));
        assertEquals(
                "Test2: ((DecimalFormat) NumberFormat.getInstance()).toPattern returned wrong value",
                "#,##0.###", ((DecimalFormat) format).toPattern());
        assertEquals(
                "Test3: NumberFormat.getInstance().format(123456789) returned wrong value",
                "123'456'789", format.format(123456789));
        // use de_CH instead
        /*assertEquals(
                "Test1: NumberFormat.getInstance().format(1234567890.0987654321) returned wrong value",
                "1,234,567,890.099", format.format(1234567890.0987654321));
        assertEquals(
                "Test2: ((DecimalFormat) NumberFormat.getInstance()).toPattern returned wrong value",
                "#,##0.###;#,##0.###-", ((DecimalFormat) format).toPattern());
        assertEquals(
                "Test3: NumberFormat.getInstance().format(123456789) returned wrong value",
                "123,456,789", format.format(123456789));*/
        try {
            NumberFormat.getInstance(null);
            fail("java.lang.NullPointerException is not thrown");
        } catch (java.lang.NullPointerException npe) {
            // expested
        }
        // END android-changed
    }

    /**
     * @tests java.text.NumberFormat#getNumberInstance()
     */
    public void test_getNumberInstance() {
        Locale.setDefault(Locale.US);
        NumberFormat format = NumberFormat.getNumberInstance();

        assertNotSame("Instance is null", null, format);
        assertTrue("Object is not instance of NumberFormat",
                format instanceof NumberFormat);

        assertEquals(
                "Test1: NumberFormat.getNumberInstance().format(1234567890.0987654321) returned wrong value",
                "1,234,567,890.099", format.format(1234567890.0987654321));
        assertEquals(
                "Test2: ((DecimalFormat) NumberFormat.getNumberInstance()).toPattern returned wrong value",
                "#,##0.###", ((DecimalFormat) format).toPattern());
        assertEquals(
                "Test3: NumberFormat.getNumberInstance().format(123456789) returned wrong value",
                "123,456,789", format.format(123456789));
    }

    /**
     * @tests java.text.NumberFormat#getNumberInstance(Locale)
     */
    public void test_getNumberInstanceLjava_util_Locale() {
        // BEGIN android-changed
        Locale.setDefault(Locale.US);
        // use de_CH instead
        NumberFormat format = NumberFormat.getNumberInstance(new Locale("de",
                "CH"));
        // NumberFormat format = NumberFormat.getNumberInstance(new Locale("ar",
        //         "AR"));

        assertNotSame("Instance is null", null, format);
        assertTrue("Object is not instance of NumberFormat",
                format instanceof NumberFormat);

        assertEquals(
                "Test1: NumberFormat.getNumberInstance().format(-1234567890.0987654321) returned wrong value",
                "-1'234'567'890.099", format.format(-1234567890.0987654321));
        assertEquals(
                "Test2: ((DecimalFormat) NumberFormat.getNumberInstance()).toPattern returned wrong value",
                "#,##0.###", ((DecimalFormat) format).toPattern());
        assertEquals(
                "Test3: NumberFormat.getNumberInstance().format(123456789) returned wrong value",
                "123'456'789", format.format(123456789));
        // use de_CH instead
        /*assertEquals(
                "Test1: NumberFormat.getNumberInstance().format(-1234567890.0987654321) returned wrong value",
                "1,234,567,890.099-", format.format(-1234567890.0987654321));
        assertEquals(
                "Test2: ((DecimalFormat) NumberFormat.getNumberInstance()).toPattern returned wrong value",
                "#,##0.###;#,##0.###-", ((DecimalFormat) format).toPattern());
        assertEquals(
                "Test3: NumberFormat.getNumberInstance().format(123456789) returned wrong value",
                "123,456,789", format.format(123456789));*/
        try {
            NumberFormat.getInstance(null);
            fail("java.lang.NullPointerException is not thrown");
        } catch (java.lang.NullPointerException npe) {
            // expested
        }
        // END android-changed
    }

    /**
     * @tests java.text.NumberFormat#getPercentInstance()
     */
    public void test_getPercentInstance() {
        Locale.setDefault(Locale.US);
        NumberFormat format = NumberFormat.getPercentInstance();

        assertNotSame("Instance is null", null, format);
        assertTrue("Object is not instance of NumberFormat",
                format instanceof NumberFormat);

        assertEquals(
                "Test1: NumberFormat.getPercentInstance().format(1234567890.0987654321) returned wrong value",
                "123,456,789,010%", format.format(1234567890.0987654321));
        assertEquals(
                "Test2: ((DecimalFormat) NumberFormat.getPercentInstance()).toPattern returned wrong value",
                "#,##0%", ((DecimalFormat) format).toPattern());
        assertEquals(
                "Test3: NumberFormat.getPercentInstance().format(123456789) returned wrong value",
                "12,345,678,900%", format.format(123456789));
    }

    /**
     * @tests java.text.NumberFormat#getPercentInstance(Locale)
     */
    public void test_getPercentInstanceLjava_util_Locale() {
        Locale.setDefault(Locale.US);
        NumberFormat format = NumberFormat.getPercentInstance(new Locale("ar",
                "AR"));

        assertNotSame("Instance is null", null, format);
        assertTrue("Object is not instance of NumberFormat",
                format instanceof NumberFormat);

        assertEquals(
                "Test1: NumberFormat.getPercentInstance().format(1234567890.0987654321) returned wrong value",
                "123,456,789,010%", format.format(1234567890.0987654321));
        assertEquals(
                "Test2: ((DecimalFormat) NumberFormat.getPercentInstance()).toPattern returned wrong value",
                "#,##0%", ((DecimalFormat) format).toPattern());
        assertEquals(
                "Test3: NumberFormat.getPercentInstance().format(123456789) returned wrong value",
                "12,345,678,900%", format.format(123456789));
        try {
            NumberFormat.getInstance(null);
            fail("java.lang.NullPointerException is not thrown");
        } catch (java.lang.NullPointerException npe) {
            // expested
        }
    }

    /**
     * @tests java.text.NumberFormat#getMaximumFractionDigits()
     */
    public void test_getMaximumFractionDigits() {
        NumberFormat nf1 = NumberFormat.getInstance();

        nf1.setMaximumFractionDigits(Integer.MAX_VALUE);
        int result = nf1.getMaximumFractionDigits();
        assertTrue("getMaximumFractionDigits returns " + result
                + " instead of: " + Integer.MAX_VALUE,
                result == Integer.MAX_VALUE);

        nf1.setMaximumFractionDigits(0);
        result = nf1.getMaximumFractionDigits();
        assertTrue("getMaximumFractionDigits returns " + result
                + " instead of 0", result == 0);

        nf1.setMinimumFractionDigits(Integer.MAX_VALUE);
        result = nf1.getMaximumFractionDigits();
        assertTrue("getMaximumFractionDigits returns " + result
                + " instead of Integer.MAX_VALUE", result == Integer.MAX_VALUE);
    }

    /**
     * @tests java.text.NumberFormat#getMinimumFractionDigits()
     */
    public void test_getMinimumFractionDigits() {
        NumberFormat nf1 = NumberFormat.getInstance();
        nf1.setMinimumFractionDigits(Integer.MAX_VALUE);
        int result = nf1.getMinimumFractionDigits();
        assertTrue("getMinimumFractionDigits returns " + result
                + " instead of: " + Integer.MAX_VALUE,
                result == Integer.MAX_VALUE);

        nf1.setMaximumFractionDigits(0);
        result = nf1.getMinimumFractionDigits();
        assertTrue("getMinimumFractionDigits returns " + result
                + " instead of 0", result == 0);

        nf1.setMinimumFractionDigits(52);
        result = nf1.getMinimumFractionDigits();
        assertTrue("getMinimumFractionDigits returns " + result
                + " instead of 52", result == 52);
    }

    /**
     * @tests java.text.NumberFormat#getMaximumIntegerDigits()
     */
    public void test_getMaximumIntegerDigits() {
        NumberFormat nf1 = NumberFormat.getInstance();
        nf1.setMaximumIntegerDigits(Integer.MAX_VALUE);
        int result = nf1.getMaximumIntegerDigits();
        assertTrue("getMaximumIntegerDigits returns " + result
                + " instead of: " + Integer.MAX_VALUE,
                result == Integer.MAX_VALUE);

        nf1.setMaximumIntegerDigits(0);
        result = nf1.getMaximumIntegerDigits();
        assertTrue("getMaximumIntegerDigits returns " + result
                + " instead of 0", result == 0);

        nf1.setMinimumIntegerDigits(Integer.MAX_VALUE);
        result = nf1.getMaximumIntegerDigits();
        assertTrue("getMaximumIntegerigits returns " + result
                + " instead of Integer.MAX_VALUE", result == Integer.MAX_VALUE);
    }

    /**
     * @tests java.text.NumberFormat#getMinimumIntegerDigits()
     */
    public void test_getMinimumIntegernDigits() {
        NumberFormat nf1 = NumberFormat.getInstance();
        nf1.setMinimumIntegerDigits(Integer.MAX_VALUE);
        int result = nf1.getMinimumIntegerDigits();
        assertTrue("getMinimumIntegerDigits returns " + result
                + " instead of: " + Integer.MAX_VALUE,
                result == Integer.MAX_VALUE);

        nf1.setMaximumIntegerDigits(0);
        result = nf1.getMinimumIntegerDigits();
        assertTrue("getMinimumIntegerDigits returns " + result
                + " instead of 0", result == 0);

        nf1.setMinimumIntegerDigits(0x12034);
        result = nf1.getMinimumIntegerDigits();
        assertTrue("getMinimumIntegerDigits returns " + result
                + " instead of 5148", result == 73780);
    }

    /**
     * @tests java.text.NumberFormat#hashCode()
     */
    public void test_hashCode() {

        NumberFormat nf1 = NumberFormat.getInstance();
        NumberFormat nf11 = NumberFormat.getInstance();
        NumberFormat nf2 = NumberFormat.getInstance(Locale.US);
        NumberFormat nf3 = NumberFormat.getPercentInstance();
        NumberFormat nf4 = NumberFormat.getCurrencyInstance();
        NumberFormat nf5 = NumberFormat
                .getNumberInstance(new Locale("mk", "MK"));
        NumberFormat nf6 = NumberFormat.getInstance(Locale.US);

        assertTrue("Hash codes are not equal: case 1", nf1.hashCode() == nf2
                .hashCode());
        assertTrue("Hash codes are not equal: case 2", nf1.hashCode() == nf11
                .hashCode());
        assertTrue("Hash codes are not equal: case 3", nf1.hashCode() == nf3
                .hashCode());
        assertFalse("Hash codes are equal: case 4", nf3.hashCode() == nf4
                .hashCode());
        assertFalse("Hash codes are equal: case 5", nf4.hashCode() == nf5
                .hashCode());
        assertTrue("Hash codes are not equal: case 6", nf5.hashCode() == nf6
                .hashCode());

        nf1.setMaximumFractionDigits(0);
        assertTrue("Hash codes are not equal: case 7", nf1.hashCode() == nf11
                .hashCode());
    }

    /**
     * @tests java.text.NumberFormat#isGroupingUsed()
     */
    public void test_isGroupingUsed() {
        NumberFormat nf1 = NumberFormat.getInstance();
        assertTrue("grouping is not used for NumberFormat.getInstance", nf1
                .isGroupingUsed());

        nf1.setGroupingUsed(false);
        assertFalse(
                "grouping is used for NumberFormat.getInstance after setting false",
                nf1.isGroupingUsed());

        nf1.setGroupingUsed(true);
        assertTrue(
                "grouping is not used for NumberFormat.getInstance after setting true",
                nf1.isGroupingUsed());
    }

    /**
     * @tests java.text.NumberFormat#setGroupingUsed(boolean)
     */
    public void test_setGroupingUsed() {
        NumberFormat nf1 = NumberFormat.getInstance(Locale.US);
        nf1.setGroupingUsed(false);

        assertEquals("grouping is not used for 1234567890.1", "1234567890.1",
                nf1.format(1234567890.1));

        assertEquals("grouping is not used for -1234567890.1", "-1234567890.1",
                nf1.format(-1234567890.1));

        nf1.setGroupingUsed(false);

        assertEquals("grouping is not used for 1234567890.1", "1234567890.1",
                nf1.format(1234567890.1));

        assertEquals("grouping is not used for -1234567890.1", "-1234567890.1",
                nf1.format(-1234567890.1));

        nf1.setGroupingUsed(true);

        assertEquals("grouping is not used for 1234567890.1",
                "1,234,567,890.1", nf1.format(1234567890.1));

        assertEquals("grouping is not used for -1234567890.1",
                "-1,234,567,890.1", nf1.format(-1234567890.1));

        NumberFormat nf2 = NumberFormat.getPercentInstance(new Locale("ar",
                "AR"));
        nf2.setGroupingUsed(false);
        assertEquals(
                "Locale(\"ar\", \"AR\"): grouping is not used for 1234567890.1",
                "123456789010%", nf2.format(1234567890.1));

        assertEquals(
                "Locale(\"ar\", \"AR\"): grouping is not used for -1234567890.1",
                "-123456789010%", nf2.format(-1234567890.1));
        assertEquals("grouping is not used for 1234567890.1",
                "1,234,567,890.1", nf1.format(1234567890.1));

        nf2.setGroupingUsed(true);
        assertEquals(
                "Locale(\"ar\", \"AR\"): grouping is not used for 1234567890.1",
                "123,456,789,010%", nf2.format(1234567890.1));

        assertEquals(
                "Locale(\"ar\", \"AR\"): grouping is not used for -1234567890.1",
                "-123,456,789,010%", nf2.format(-1234567890.1));

        nf2.setGroupingUsed(true);
        assertEquals(
                "Locale(\"ar\", \"AR\"): grouping is not used for 1234567890.1",
                "123,456,789,010%", nf2.format(1234567890.1));

        assertEquals(
                "Locale(\"ar\", \"AR\"): grouping is not used for -1234567890.1",
                "-123,456,789,010%", nf2.format(-1234567890.1));
    }

    /**
     * @tests java.text.NumberFormat#isParseIntegerOnly()
     */
    public void test_isParseIntegerOnly() {
        NumberFormat nf1 = NumberFormat.getInstance();
        assertTrue("ParseIntegerOnly is not used for NumberFormat.getInstance",
                nf1.isGroupingUsed());

        nf1.setParseIntegerOnly(false);
        assertFalse(
                "ParseIntegerOnly is used for NumberFormat.getInstance after setting false",
                nf1.isParseIntegerOnly());

        nf1.setParseIntegerOnly(true);
        assertTrue(
                "ParseIntegerOnly is not used for NumberFormat.getInstance after setting true",
                nf1.isParseIntegerOnly());
    }

    /**
     * @tests java.text.NumberFormat#setParseIntegerOnly(boolean)
     */
    public void test_setParseIntegerOnly() {
        NumberFormat nf1 = NumberFormat.getInstance(Locale.US);
        nf1.setParseIntegerOnly(true);

        assertEquals("ParseIntegerOnly is not used for 1234567890.1",
                "1,234,567,890.1", nf1.format(1234567890.1));
        assertEquals("ParseIntegerOnly is not used for -1234567890.1",
                "-1,234,567,890.1", nf1.format(-1234567890.1));
        assertEquals("ParseIntegerOnly is not used for -1234567890.",
                "-1,234,567,890", nf1.format(-1234567890.));

        nf1.setParseIntegerOnly(false);

        assertEquals("ParseIntegerOnly is not used for 1234567890.1",
                "1,234,567,890.1", nf1.format(1234567890.1));
        assertEquals("ParseIntegerOnly is not used for -1234567890.1",
                "-1,234,567,890.1", nf1.format(-1234567890.1));
        assertEquals("ParseIntegerOnly is not used for -1234567890.",
                "-1,234,567,890", nf1.format(-1234567890.));
    }

    /**
     * @tests java.text.NumberFormat#setMaximumFractionDigits(int)
     */
    public void test_setMaximumFractionDigits() {
        NumberFormat nf1 = NumberFormat.getInstance(Locale.US);
        nf1.setMaximumFractionDigits(Integer.MAX_VALUE);
        int result = nf1.getMaximumFractionDigits();
        assertTrue("setMaximumFractionDigits set " + result
                + " instead of Integer.MAX_VALUE", result == Integer.MAX_VALUE);
        nf1.setMaximumFractionDigits(0);
        result = nf1.getMaximumFractionDigits();
        assertTrue("setMaximumFractionDigits set " + result + " instead of 0",
                result == 0);
        assertEquals("format of 1234567890.0987654321 returns incorrect value",
                "1,234,567,890", nf1.format(1234567890.0987654321));
        nf1.setMaximumFractionDigits(5);
        result = nf1.getMaximumFractionDigits();
        assertTrue("setMaximumFractionDigits set " + result + " instead of 5",
                result == 5);
        assertEquals(
                "format of 1234567890.0987654321 returns incorrect value with MaximumFractionDigits = 5",
                "1,234,567,890.09877", nf1.format(1234567890.0987654321));
        assertEquals(
                "format of -1234567890 returns incorrect value with MaximumFractionDigits = 5",
                "-1,234,567,890", nf1.format(-1234567890));
        nf1.setMaximumFractionDigits(Integer.MIN_VALUE);
        result = nf1.getMaximumFractionDigits();
        assertTrue("setMaximumFractionDigits set " + result
                + " instead of Integer.MIN_VALUE", result == 0);
        assertEquals(
                "format of 1234567890.0987654321 returns incorrect value with MaximumFractionDigits = 5",
                "1,234,567,890", nf1.format(1234567890.0987654321));
    }

    /**
     * @tests java.text.NumberFormat#setMinimumFractionDigits(int)
     */
    public void test_setMinimumFractionDigits() {

        NumberFormat nf1 = NumberFormat.getInstance(Locale.US);
        nf1.setMinimumFractionDigits(Integer.MAX_VALUE);
        int result = nf1.getMinimumFractionDigits();
        assertTrue("setMinimumFractionDigits set " + result
                + " instead of Integer.MAX_VALUE", result == Integer.MAX_VALUE);
        nf1.setMinimumFractionDigits(0);
        result = nf1.getMinimumFractionDigits();
        assertTrue("setMinimumFractionDigits set " + result + " instead of 0",
                result == 0);
        nf1.setMinimumFractionDigits(5);
        result = nf1.getMinimumFractionDigits();
        assertTrue("setMinimumFractionDigits set " + result + " instead of 5",
                result == 5);
        assertEquals(
                "format of 1234567890.0987654321 returns incorrect value with MinimumFractionDigits = 5",
                "1,234,567,890.09000", nf1.format(1234567890.09));
        assertEquals(
                "format of -1234567890 returns incorrect value with MinimumFractionDigits = 5",
                "-1,234,567,890.00000", nf1.format(-1234567890));
        nf1.setMinimumFractionDigits(Integer.MIN_VALUE);
        result = nf1.getMinimumFractionDigits();
        assertTrue("setMinimumFractionDigits set " + result
                + " instead of Integer.MIN_VALUE", result == 0);
        assertEquals(
                "format of 1234567890.098 returns incorrect value with MinimumFractionDigits = 5",
                "1,234,567,890.098", nf1.format(1234567890.098));
    }

    /**
     * @tests java.text.NumberFormat#setMinimumIntegerDigits(int)
     */
    public void test_setMinimumIntegerDigits() {

        NumberFormat nf1 = NumberFormat.getInstance(Locale.US);
        nf1.setMinimumIntegerDigits(Integer.MAX_VALUE);
        int result = nf1.getMinimumIntegerDigits();
        assertTrue("setMinimumIntegerDigits set " + result
                + " instead of Integer.MAX_VALUE", result == Integer.MAX_VALUE);
        nf1.setMinimumIntegerDigits(0);
        result = nf1.getMinimumIntegerDigits();
        assertTrue("setMinimumIntegerDigits set " + result + " instead of 0",
                result == 0);
        nf1.setMinimumIntegerDigits(5);
        result = nf1.getMinimumIntegerDigits();
        assertTrue("setMinimumIntegerDigits set " + result + " instead of 5",
                result == 5);
        assertEquals(
                "format of 123.09 returns incorrect value with MinimumIntegerDigits = 5",
                "00,123.09", nf1.format(123.09));
        assertEquals(
                "format of -123 returns incorrect value with MinimumIntegerDigits = 5",
                "-00,123", nf1.format(-123));
        nf1.setMinimumIntegerDigits(Integer.MIN_VALUE);
        result = nf1.getMinimumIntegerDigits();
        assertTrue("setMinimumIntegerDigits set " + result
                + " instead of Integer.MIN_VALUE", result == 0);
    }

    /**
     * @tests java.text.NumberFormat#parse(String)
     */
    public void test_parseLjava_lang_String() {
        NumberFormat nf1 = NumberFormat.getInstance();
        try {
            assertEquals(
                    "Test1: NumberFormat.getInstance().parse(\"1234567890.1\") returned wrong number",
                    new Double(1234567890.1), nf1.parse("1234567890.1"));
        } catch (java.text.ParseException pe) {
            fail("java.text.ParseException is thrown for 1234567890.1");
        }

        try {
            assertEquals(
                    "Test2: NumberFormat.getInstance().parse(\"-1234567890.1\") returned wrong number",
                    new Double(-1234567890.1), nf1.parse("-1,234,567,890.1"));
        } catch (java.text.ParseException pe) {
            fail("java.text.ParseException is thrown for -1,234,567,890.1");
        }

        try {
            nf1.parse("@1,234,567,8901");
            fail("java.text.ParseException is not thrown for 1,234,567,890z1");
        } catch (java.text.ParseException pe) {
            // expected
        }

        nf1 = NumberFormat.getPercentInstance();
        try {
            assertEquals(
                    "Test3: NumberFormat.getPercentInstance().parse(\"-123%\") returned wrong number",
                    new Double(-1.23), nf1.parse("-123%"));
        } catch (java.text.ParseException pe) {
            fail("java.text.ParseException is thrown for -123%");
        }

        nf1 = NumberFormat.getCurrencyInstance();
        try {
            assertEquals(
                    "Test4: NumberFormat.getCurrencyInstance().parse(\"$123\") returned wrong number",
                    new Long(123), nf1.parse("$123"));
        } catch (java.text.ParseException pe) {
            fail("java.text.ParseException is thrown for $123");
        }

        try {
            assertEquals(
                    "Test4: NumberFormat.getCurrencyInstance().parse(\"$123abc\") returned wrong number",
                    new Long(123), nf1.parse("$123abc"));
        } catch (java.text.ParseException pe) {
            fail("java.text.ParseException is thrown for $123");
        }

        nf1 = NumberFormat.getIntegerInstance();
        try {
            assertEquals(
                    "Test5: NumberFormat.getIntegerInstance().parse(\"-123.123\") returned wrong number",
                    nf1.parseObject("-123.123"), nf1.parse("-123.123"));
        } catch (java.text.ParseException pe) {
            fail("java.text.ParseException is thrown for $123");
        }
    }

    /**
     * @tests java.text.NumberFormat#NumberFormat()
     */
    public void test_constructor() {
        MyNumberFormat mf = new MyNumberFormat();
        assertFalse("Greated NumberFormat object is null", mf == null);
        assertTrue(
                "Greated NumberFormat object is not instance of NumberFormat",
                mf instanceof NumberFormat);
    }

    class MyNumberFormat extends NumberFormat {
        static final long serialVersionUID = 1L;

        public MyNumberFormat() {
            super();
        }

        public StringBuffer format(double number, StringBuffer toAppendTo,
                FieldPosition pos) {

            return new StringBuffer();
        }

        public Number parse(String source, ParsePosition parsePosition) {

            return new Double(0);
        }

        public StringBuffer format(long number, StringBuffer toAppendTo,
                FieldPosition pos) {
            return new StringBuffer();
        }

    }
}
