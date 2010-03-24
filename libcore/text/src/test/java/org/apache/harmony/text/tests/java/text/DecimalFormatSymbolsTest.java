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

import tests.support.Support_Locale;
import dalvik.annotation.AndroidOnly;
import dalvik.annotation.KnownFailure;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestTargetNew;

import junit.framework.TestCase;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;

@TestTargetClass(DecimalFormatSymbols.class) 
public class DecimalFormatSymbolsTest extends TestCase {

    DecimalFormatSymbols dfs;

    DecimalFormatSymbols dfsUS;

    /**
     * @tests java.text.DecimalFormatSymbols#DecimalFormatSymbols() Test of
     *        method java.text.DecimalFormatSymbols#DecimalFormatSymbols().
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "DecimalFormatSymbols",
        args = {}
    )
    public void test_Constructor() {
        // Test for method java.text.DecimalFormatSymbols()
        try {
            new DecimalFormatSymbols();
        } catch (Exception e) {
            fail("Unexpected exception " + e.toString());
        }
    }

    /**
     * @tests java.text.DecimalFormatSymbols#DecimalFormatSymbols(java.util.Locale)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "DecimalFormatSymbols",
        args = {java.util.Locale.class}
    )
    public void test_ConstructorLjava_util_Locale() {
        DecimalFormatSymbols dfs = new DecimalFormatSymbols(new Locale("en",
                "us"));
        assertEquals("Returned incorrect symbols", '%', dfs.getPercent());
        
        try {
            new DecimalFormatSymbols(null);
            fail("NullPointerException was not thrown.");
        } catch(NullPointerException npe) {
            //expected  
        }
    }

    /**
     * @tests java.text.DecimalFormatSymbols#getAvailableLocales()
     */
    public void test_getAvailableLocales_no_provider() throws Exception {
        Locale[] locales = DecimalFormatSymbols.getAvailableLocales();
        assertNotNull(locales);
        // must contain Locale.US
        boolean flag = false;
        for (Locale locale : locales) {
            if (locale.equals(Locale.US)) {
                flag = true;
                break;
            }
        }
        assertTrue(flag);
    }

    /**
     * @tests java.text.DecimalFormatSymbols#getInstance()
     */
    public void test_getInstance() {
        assertEquals(new DecimalFormatSymbols(), DecimalFormatSymbols.getInstance());
        assertEquals(new DecimalFormatSymbols(Locale.getDefault()),
                DecimalFormatSymbols.getInstance());

        assertNotSame(DecimalFormatSymbols.getInstance(), DecimalFormatSymbols.getInstance());
    }

    /**
     * @tests java.text.DecimalFormatSymbols#getInstance(Locale)
     */
    public void test_getInstanceLjava_util_Locale() {
        try {
            DecimalFormatSymbols.getInstance(null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }

        assertEquals(new DecimalFormatSymbols(Locale.GERMANY), DecimalFormatSymbols
                .getInstance(Locale.GERMANY));

        Locale locale = new Locale("not exist language", "not exist country");
        DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(locale);
        assertNotNull(symbols);
        // BEGIN android-removed: this test is wrong, and confuses default locale with root locale.
        // assertEquals(DecimalFormatSymbols.getInstance(), symbols);
        // END android-removed
    }

    /**
     * @tests java.text.DecimalFormatSymbols#clone() Test of method
     *        java.text.DecimalFormatSymbols#clone(). Case 1: Compare of
     *        internal variables of cloned objects. Case 2: Compare of clones.
     *        Case 3: Change the content of the clone.
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "clone",
        args = {}
    )
    public void test_clone() {
        try {
            // case 1: Compare of internal variables of cloned objects
            DecimalFormatSymbols fs = new DecimalFormatSymbols(Locale.US);
            DecimalFormatSymbols fsc = (DecimalFormatSymbols) fs.clone();
            assertEquals(fs.getCurrency(), fsc.getCurrency());

            // case 2: Compare of clones
            fs = new DecimalFormatSymbols();
            DecimalFormatSymbols fsc2 = (DecimalFormatSymbols) (fs.clone());
            // make sure the objects are equal
            assertTrue("Object's clone isn't equal!", fs.equals(fsc2));

            // case 3:
            // change the content of the clone and make sure it's not equal
            // anymore
            // verifies that it's data is now distinct from the original
            fs.setNaN("not-a-number");
            assertTrue("Object's changed clone should not be equal!", !fs
                    .equals(fsc2));
        } catch (Exception e) {
            fail("Unexpected exception " + e.toString());
        }
    }

    /**
     * @tests java.text.DecimalFormatSymbols#equals(java.lang.Object)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "equals",
        args = {java.lang.Object.class}
    )
    public void test_equalsLjava_lang_Object() {
        assertTrue("Equal objects returned false", dfs.equals(dfs.clone()));
        dfs.setDigit('B');
        assertTrue("Un-Equal objects returned true", !dfs
                .equals(new DecimalFormatSymbols()));

    }

    /**
     * @tests java.text.DecimalFormatSymbols#getCurrency()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getCurrency",
        args = {}
    )
    public void test_getCurrency() {
        Locale csCzLocale = new Locale("cs", "CZ");
        Locale czLocale = new Locale("", "CZ");
        Locale csLocale = new Locale("cs", "");
        Locale deLocale = new Locale("de", "AT");
        Locale[] requiredLocales = {Locale.US, csCzLocale, czLocale, csLocale, deLocale};
        if (!Support_Locale.areLocalesAvailable(requiredLocales)) {
            // locale dependent test, bug 1943269
            return;
        }
        Currency currency = Currency.getInstance("USD");
        assertEquals("Returned incorrect currency",
                dfsUS.getCurrency(), currency);

        // use cs_CZ instead
        //Currency currK = Currency.getInstance("KRW");
        Currency currC = Currency.getInstance("CZK");
        Currency currX = Currency.getInstance("XXX");
        Currency currE = Currency.getInstance("EUR");
        // Currency currF = Currency.getInstance("FRF");


        DecimalFormatSymbols dfs1 = new DecimalFormatSymbols(csCzLocale);
        assertTrue("Test1: Returned incorrect currency",
                dfs1.getCurrency() == currC);
        assertEquals("Test1: Returned incorrect currencySymbol", "K\u010d", dfs1
                .getCurrencySymbol());
        assertEquals("Test1: Returned incorrect intlCurrencySymbol", "CZK",
                dfs1.getInternationalCurrencySymbol());


        dfs1 = new DecimalFormatSymbols(czLocale);
        assertTrue("Test2: Returned incorrect currency",
                dfs1.getCurrency() == currC);
        // BEGIN android-changed
        // ICU4.2.1, the currency symbol for CZK is changed from CZK to K\u010d in en.txt.
        assertEquals("Test2: Returned incorrect currencySymbol", "K\u010d", dfs1
                .getCurrencySymbol());
        // END android-changed
        assertEquals("Test2: Returned incorrect intlCurrencySymbol", "CZK",
                dfs1.getInternationalCurrencySymbol());

        dfs1 = new DecimalFormatSymbols(csLocale);
        assertEquals("Test3: Returned incorrect currency",
                currX, dfs1.getCurrency());
        assertEquals("Test3: Returned incorrect currencySymbol", "\u00a4", dfs1
                .getCurrencySymbol());
        assertEquals("Test3: Returned incorrect intlCurrencySymbol", "XXX",
                dfs1.getInternationalCurrencySymbol());

        dfs1 = new DecimalFormatSymbols(deLocale);
        assertTrue("Test4: Returned incorrect currency",
                dfs1.getCurrency() == currE);
        assertEquals("Test4: Returned incorrect currencySymbol", "\u20ac", dfs1
                .getCurrencySymbol());
        assertEquals("Test4: Returned incorrect intlCurrencySymbol", "EUR",
                dfs1.getInternationalCurrencySymbol());

        // RI fails these tests since it doesn't have the PREEURO variant
        // dfs1 = new DecimalFormatSymbols(new Locale("fr", "FR","PREEURO"));
        // assertTrue("Test5: Returned incorrect currency", dfs1.getCurrency()
        // == currF);
        // assertTrue("Test5: Returned incorrect currencySymbol",
        // dfs1.getCurrencySymbol().equals("F"));
        // assertTrue("Test5: Returned incorrect intlCurrencySymbol",
        // dfs1.getInternationalCurrencySymbol().equals("FRF"));
    }

    /**
     * @tests java.text.DecimalFormatSymbols#getCurrencySymbol()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getCurrencySymbol",
        args = {}
    )
    public void test_getCurrencySymbol() {
        assertEquals("Returned incorrect currencySymbol", "$", dfsUS
                .getCurrencySymbol());
    }

    /**
     * @tests java.text.DecimalFormatSymbols#getDecimalSeparator()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getDecimalSeparator",
        args = {}
    )
    public void test_getDecimalSeparator() {
        dfs.setDecimalSeparator('*');
        assertEquals("Returned incorrect DecimalSeparator symbol", '*', dfs
                .getDecimalSeparator());
    }

    /**
     * @tests java.text.DecimalFormatSymbols#getDigit()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getDigit",
        args = {}
    )
    public void test_getDigit() {
        dfs.setDigit('*');
        assertEquals("Returned incorrect Digit symbol", '*', dfs.getDigit());
    }

    /**
     * @tests java.text.DecimalFormatSymbols#getGroupingSeparator()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getGroupingSeparator",
        args = {}
    )
    public void test_getGroupingSeparator() {
        dfs.setGroupingSeparator('*');
        assertEquals("Returned incorrect GroupingSeparator symbol", '*', dfs
                .getGroupingSeparator());
    }

    /**
     * @tests java.text.DecimalFormatSymbols#getInfinity()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getInfinity",
        args = {}
    )
    public void test_getInfinity() {
        dfs.setInfinity("&");
        assertTrue("Returned incorrect Infinity symbol",
                dfs.getInfinity() == "&");
    }

    /**
     * @tests java.text.DecimalFormatSymbols#getInternationalCurrencySymbol()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getInternationalCurrencySymbol",
        args = {}
    )
    public void test_getInternationalCurrencySymbol() {
        assertEquals("Returned incorrect InternationalCurrencySymbol", "USD",
                dfsUS.getInternationalCurrencySymbol());
    }

    /**
     * @tests java.text.DecimalFormatSymbols#getMinusSign()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getMinusSign",
        args = {}
    )
    public void test_getMinusSign() {
        dfs.setMinusSign('&');
        assertEquals("Returned incorrect MinusSign symbol", '&', dfs
                .getMinusSign());
    }

    /**
     * @tests java.text.DecimalFormatSymbols#getMonetaryDecimalSeparator() Test
     *        of method
     *        java.text.DecimalFormatSymbols#getMonetaryDecimalSeparator().
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getMonetaryDecimalSeparator",
        args = {}
    )
    public void test_getMonetaryDecimalSeparator() {
        try {
            dfs.setMonetaryDecimalSeparator(',');
            assertEquals("Returned incorrect MonetaryDecimalSeparator symbol",
                    ',', dfs.getMonetaryDecimalSeparator());
        } catch (Exception e) {
            fail("Unexpected exception " + e.toString());
        }
    }

    /**
     * @tests java.text.DecimalFormatSymbols#getNaN()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getNaN",
        args = {}
    )
    public void test_getNaN() {
        dfs.setNaN("NAN!!");
        assertEquals("Returned incorrect nan symbol", "NAN!!", dfs.getNaN());
    }

    /**
     * @tests java.text.DecimalFormatSymbols#getPatternSeparator()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getPatternSeparator",
        args = {}
    )
    public void test_getPatternSeparator() {
        dfs.setPatternSeparator('X');
        assertEquals("Returned incorrect PatternSeparator symbol", 'X', dfs
                .getPatternSeparator());
    }

    /**
     * @tests java.text.DecimalFormatSymbols#getPercent()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getPercent",
        args = {}
    )
    public void test_getPercent() {
        dfs.setPercent('*');
        assertEquals("Returned incorrect Percent symbol", '*', dfs.getPercent());
    }

    /**
     * @tests java.text.DecimalFormatSymbols#getPerMill()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getPerMill",
        args = {}
    )
    public void test_getPerMill() {
        dfs.setPerMill('#');
        assertEquals("Returned incorrect PerMill symbol", '#', dfs.getPerMill());
    }

    /**
     * @tests java.text.DecimalFormatSymbols#getZeroDigit()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getZeroDigit",
        args = {}
    )
    public void test_getZeroDigit() {
        dfs.setZeroDigit('*');
        assertEquals("Returned incorrect ZeroDigit symbol", '*', dfs
                .getZeroDigit());
    }

    /**
     * @tests java.text.DecimalFormatSymbols#hashCode() Test of method
     *        java.text.DecimalFormatSymbols#hashCode().
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "hashCode",
        args = {}
    )
    @AndroidOnly("Succeeds against Android.")
    public void test_hashCode() {
        try {
            DecimalFormatSymbols dfs1 = new DecimalFormatSymbols();
            DecimalFormatSymbols dfs2 = (DecimalFormatSymbols) dfs1.clone();
            assertTrue("Hash codes of equal object are equal", dfs2
                    .hashCode() == dfs1.hashCode());
            dfs1.setInfinity("infinity_infinity");
            assertTrue("Hash codes of non-equal objects are equal", dfs2
                    .hashCode() != dfs1.hashCode());
        } catch (Exception e) {
            fail("Unexpected exception " + e.toString());
        }
    }

    /**
     * @tests java.text.DecimalFormatSymbols#setCurrency(java.util.Currency)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setCurrency",
        args = {java.util.Currency.class}
    )
    public void test_setCurrencyLjava_util_Currency() {
        Locale locale = Locale.CANADA;
        DecimalFormatSymbols dfs = ((DecimalFormat) NumberFormat
                .getCurrencyInstance(locale)).getDecimalFormatSymbols();

        try {
            dfs.setCurrency(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
        }

        Currency currency = Currency.getInstance("JPY");
        dfs.setCurrency(currency);

        assertTrue("Returned incorrect currency", currency == dfs.getCurrency());
        assertEquals("Returned incorrect currency symbol", currency.getSymbol(
                locale), dfs.getCurrencySymbol());
        assertTrue("Returned incorrect international currency symbol", currency
                .getCurrencyCode().equals(dfs.getInternationalCurrencySymbol()));
    }

    /**
     * @tests java.text.DecimalFormatSymbols#setCurrencySymbol(java.lang.String)
     *        Test of method
     *        java.text.DecimalFormatSymbols#setCurrencySymbol(java.lang.String).
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setCurrencySymbol",
        args = {java.lang.String.class}
    )
    public void test_setCurrencySymbolLjava_lang_String() {
        try {
            dfs.setCurrencySymbol("$");
            assertEquals("Returned incorrect CurrencySymbol symbol", "$", dfs
                    .getCurrencySymbol());
        } catch (Exception e) {
            fail("Unexpected exception " + e.toString());
        }
    }

    /**
     * @tests java.text.DecimalFormatSymbols#setDecimalSeparator(char)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setDecimalSeparator",
        args = {char.class}
    )
    public void test_setDecimalSeparatorC() {
        dfs.setDecimalSeparator('*');
        assertEquals("Returned incorrect DecimalSeparator symbol", '*', dfs
                .getDecimalSeparator());
    }

    /**
     * @tests java.text.DecimalFormatSymbols#setDigit(char)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setDigit",
        args = {char.class}
    )
    public void test_setDigitC() {
        dfs.setDigit('*');
        assertEquals("Returned incorrect Digit symbol", '*', dfs.getDigit());
    }

    /**
     * @tests java.text.DecimalFormatSymbols#setGroupingSeparator(char)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setGroupingSeparator",
        args = {char.class}
    )
    public void test_setGroupingSeparatorC() {
        dfs.setGroupingSeparator('*');
        assertEquals("Returned incorrect GroupingSeparator symbol", '*', dfs
                .getGroupingSeparator());
    }

    /**
     * @tests java.text.DecimalFormatSymbols#setInfinity(java.lang.String)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setInfinity",
        args = {java.lang.String.class}
    )
    public void test_setInfinityLjava_lang_String() {
        dfs.setInfinity("&");
        assertTrue("Returned incorrect Infinity symbol",
                dfs.getInfinity() == "&");
    }

    /**
     * @tests java.text.DecimalFormatSymbols#setInternationalCurrencySymbol(java.lang.String)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setInternationalCurrencySymbol",
        args = {java.lang.String.class}
    )
    public void test_setInternationalCurrencySymbolLjava_lang_String() {
        Locale locale = Locale.CANADA;
        DecimalFormatSymbols dfs = ((DecimalFormat) NumberFormat
                .getCurrencyInstance(locale)).getDecimalFormatSymbols();
        Currency currency = Currency.getInstance("JPY");
        dfs.setInternationalCurrencySymbol(currency.getCurrencyCode());

        assertTrue("Test1: Returned incorrect currency", currency == dfs
                .getCurrency());
        assertEquals("Test1: Returned incorrect currency symbol", currency
                .getSymbol(locale), dfs.getCurrencySymbol());
        assertTrue("Test1: Returned incorrect international currency symbol",
                currency.getCurrencyCode().equals(
                        dfs.getInternationalCurrencySymbol()));

        String symbol = dfs.getCurrencySymbol();
        dfs.setInternationalCurrencySymbol("bogus");
        assertNull("Test2: Returned incorrect currency", dfs.getCurrency());
        assertTrue("Test2: Returned incorrect currency symbol", dfs
                .getCurrencySymbol().equals(symbol));
        assertEquals("Test2: Returned incorrect international currency symbol",
                "bogus", dfs.getInternationalCurrencySymbol());
    }

    /**
     * @tests java.text.DecimalFormatSymbols#setMinusSign(char)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setMinusSign",
        args = {char.class}
    )
    public void test_setMinusSignC() {
        dfs.setMinusSign('&');
        assertEquals("Returned incorrect MinusSign symbol", '&', dfs
                .getMinusSign());
    }

    /**
     * @tests java.text.DecimalFormatSymbols#setMonetaryDecimalSeparator(char)
     *        Test of method
     *        java.text.DecimalFormatSymbols#setMonetaryDecimalSeparator(char).
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setMonetaryDecimalSeparator",
        args = {char.class}
    )
    public void test_setMonetaryDecimalSeparatorC() {
        try {
            dfs.setMonetaryDecimalSeparator('#');
            assertEquals("Returned incorrect MonetaryDecimalSeparator symbol",
                    '#', dfs.getMonetaryDecimalSeparator());
        } catch (Exception e) {
            fail("Unexpected exception " + e.toString());
        }
    }

    /**
     * @tests java.text.DecimalFormatSymbols#setNaN(java.lang.String)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setNaN",
        args = {java.lang.String.class}
    )
    public void test_setNaNLjava_lang_String() {
        dfs.setNaN("NAN!!");
        assertEquals("Returned incorrect nan symbol", "NAN!!", dfs.getNaN());
    }

    /**
     * @tests java.text.DecimalFormatSymbols#setPatternSeparator(char)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setPatternSeparator",
        args = {char.class}
    )
    public void test_setPatternSeparatorC() {
        dfs.setPatternSeparator('X');
        assertEquals("Returned incorrect PatternSeparator symbol", 'X', dfs
                .getPatternSeparator());
    }

    /**
     * @tests java.text.DecimalFormatSymbols#setPercent(char)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setPercent",
        args = {char.class}
    )
    public void test_setPercentC() {
        dfs.setPercent('*');
        assertEquals("Returned incorrect Percent symbol", '*', dfs.getPercent());
    }

    /**
     * @tests java.text.DecimalFormatSymbols#setPerMill(char)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setPerMill",
        args = {char.class}
    )
    public void test_setPerMillC() {
        dfs.setPerMill('#');
        assertEquals("Returned incorrect PerMill symbol", '#', dfs.getPerMill());
    }

    /**
     * @tests java.text.DecimalFormatSymbols#setZeroDigit(char)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setZeroDigit",
        args = {char.class}
    )
    public void test_setZeroDigitC() {
        dfs.setZeroDigit('*');
        assertEquals("Set incorrect ZeroDigit symbol", '*', dfs.getZeroDigit());
    }

    /**
     * Sets up the fixture, for example, open a network connection. This method
     * is called before a test is executed.
     */
    protected void setUp() {
        dfs = new DecimalFormatSymbols();
        dfsUS = new DecimalFormatSymbols(new Locale("en", "us"));
    }

    /**
     * Tears down the fixture, for example, close a network connection. This
     * method is called after a test is executed.
     */
    protected void tearDown() {
    }

    // Test serialization mechanism of DecimalFormatSymbols
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Checks serialization mechanism.",
        method = "!SerializationSelf",
        args = {}
    )
    public void test_serialization() throws Exception {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.FRANCE);
        Currency currency = symbols.getCurrency();
        assertNotNull(currency);

        // serialize
        ByteArrayOutputStream byteOStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOStream = new ObjectOutputStream(byteOStream);
        objectOStream.writeObject(symbols);

        // and deserialize
        ObjectInputStream objectIStream = new ObjectInputStream(
                new ByteArrayInputStream(byteOStream.toByteArray()));
        DecimalFormatSymbols symbolsD = (DecimalFormatSymbols) objectIStream
                .readObject();

        // The associated currency will not persist
        currency = symbolsD.getCurrency();
        assertNotNull(currency);
    }

    // Use RI to write DecimalFormatSymbols out, use Harmony to read
    // DecimalFormatSymbols in. The read symbol will be equal with those
    // instantiated inside Harmony.

    // This assertion will not come into existence the other way around. This is
    // probably caused by different serialization mechanism used by RI and
    // Harmony.
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Make sure all fields have non default values.",
        method = "!SerializationGolden",
        args = {}
    )
    public void test_RIHarmony_compatible() throws Exception {
        ObjectInputStream i = null;
        try {
            DecimalFormatSymbols symbols = new DecimalFormatSymbols(
                    Locale.FRANCE);
            i = new ObjectInputStream(
                    getClass()
                            .getClassLoader()
                            .getResourceAsStream(
                    "serialization/java/text/DecimalFormatSymbols.ser"));
            DecimalFormatSymbols symbolsD = (DecimalFormatSymbols) i
                    .readObject();
            assertEquals(symbols, symbolsD);
        } catch(NullPointerException e) {
            assertNotNull("Failed to load /serialization/java/text/" +
                    "DecimalFormatSymbols.ser", i);
        } finally {
            try {
                if (i != null) {
                    i.close();
                }
            } catch (Exception e) {
            }
        }
        assertDecimalFormatSymbolsRIFrance(dfs);
    }
    
    static void assertDecimalFormatSymbolsRIFrance(DecimalFormatSymbols dfs) {
        // Values based on Java 1.5 RI DecimalFormatSymbols for Locale.FRANCE
        /*
         * currency = [EUR]
         * currencySymbol = [U+20ac] // EURO SIGN
         * decimalSeparator = [,][U+002c]
         * digit = [#][U+0023]
         * groupingSeparator = [U+00a0] // NON-BREAKING SPACE
         * infinity = [U+221e] // INFINITY
         * internationalCurrencySymbol = [EUR]
         * minusSign = [-][U+002d]
         * monetaryDecimalSeparator = [,][U+002c]
         * naN = [U+fffd] // REPLACEMENT CHARACTER
         * patternSeparator = [;][U+003b]
         * perMill = [U+2030] // PER MILLE
         * percent = [%][U+0025]
         * zeroDigit = [0][U+0030]
         */
        assertEquals("EUR", dfs.getCurrency().getCurrencyCode());
        assertEquals("\u20AC", dfs.getCurrencySymbol());
        assertEquals(',', dfs.getDecimalSeparator());
        assertEquals('#', dfs.getDigit());
        assertEquals('\u00a0', dfs.getGroupingSeparator());
        assertEquals("\u221e", dfs.getInfinity());
        assertEquals("EUR", dfs.getInternationalCurrencySymbol());
        assertEquals('-', dfs.getMinusSign());
        assertEquals(',', dfs.getMonetaryDecimalSeparator());
        // RI's default NaN is U+FFFD, Harmony's is based on ICU
        // This suggests an RI bug, assuming that non-UTF8 bytes are UTF8 and
        // getting a conversion failure.
        assertEquals("\uFFFD", dfs.getNaN());
        assertEquals('\u003b', dfs.getPatternSeparator());
        assertEquals('\u2030', dfs.getPerMill());
        assertEquals('%', dfs.getPercent());
        assertEquals('0', dfs.getZeroDigit());
    }
}
