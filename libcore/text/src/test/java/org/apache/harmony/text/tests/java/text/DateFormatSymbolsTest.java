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

import java.text.DateFormatSymbols;
import java.util.Arrays;
import java.util.Locale;

public class DateFormatSymbolsTest extends junit.framework.TestCase {

    private DateFormatSymbols dfs;

    /**
     * @tests java.text.DateFormatSymbols#DateFormatSymbols()
     */
    public void test_Constructor() {
        // Test for method java.text.DateFormatSymbols()
        // Used in tests
        try {
            new DateFormatSymbols();
        } catch (Exception e) {
            fail("Constructor failed.");
        }
    }

    /**
     * @tests java.text.DateFormatSymbols#DateFormatSymbols(java.util.Locale)
     */
    public void test_ConstructorLjava_util_Locale() {
        // Test for method java.text.DateFormatSymbols(java.util.Locale)
        try {
            new DateFormatSymbols(new Locale("en", "us"));
        } catch (Exception e) {
            fail("Constructor failed.");
        }
    }

    /**
     * @tests java.text.DateFormatSymbols#clone()
     */
    public void test_clone() {
        // Test for method java.lang.Object java.text.DateFormatSymbols.clone()
        DateFormatSymbols symbols = new DateFormatSymbols();
        DateFormatSymbols clone = (DateFormatSymbols) symbols.clone();
        assertTrue("Not equal", symbols.equals(clone));
    }

    /**
     * @tests java.text.DateFormatSymbols#equals(java.lang.Object)
     */
    public void test_equalsLjava_lang_Object() {
        // Test for method boolean
        // java.text.DateFormatSymbols.equals(java.lang.Object)
        assertTrue("Equal object returned true", dfs.equals(dfs.clone()));
        dfs.setLocalPatternChars("KKKKKKKKK");
        assertTrue("Un-Equal objects returned false", !dfs
                .equals(new DateFormatSymbols()));
    }

    /**
     * @tests java.text.DateFormatSymbols#getAmPmStrings()
     */
    public void test_getAmPmStrings() {
        // Test for method java.lang.String []
        // java.text.DateFormatSymbols.getAmPmStrings()
        String[] retVal = dfs.getAmPmStrings();
        String[] val = { "AM", "PM" };
        if (retVal.length != val.length)
            fail("Returned wrong array");
        for (int i = 0; i < val.length; i++)
            assertTrue("Array values do not match", retVal[i].equals(val[i]));
    }

    /**
     * @tests java.text.DateFormatSymbols#getEras()
     */
    public void test_getEras() {
        // Test for method java.lang.String []
        // java.text.DateFormatSymbols.getEras()
        String[] retVal = dfs.getEras();
        String[] val = { "BC", "AD" };
        if (retVal.length != val.length)
            fail("Returned wrong array");
        for (int i = 0; i < val.length; i++)
            assertTrue("Array values do not match", retVal[i].equals(val[i]));
    }

    /**
     * @tests java.text.DateFormatSymbols#getLocalPatternChars()
     */
    public void test_getLocalPatternChars() {
        // Test for method java.lang.String
        // java.text.DateFormatSymbols.getLocalPatternChars()
        String retVal = dfs.getLocalPatternChars();

        String val = "GyMdkHmsSEDFwWahKzZ";

        assertTrue("Returned incorrect pattern string", retVal.equals(val));
    }

    /**
     * @tests java.text.DateFormatSymbols#getMonths()
     */
    public void test_getMonths() {
        // Test for method java.lang.String []
        // java.text.DateFormatSymbols.getMonths()
        String[] retVal = dfs.getMonths();
        String[] val = { "January", "February", "March", "April", "May",
                "June", "July", "August", "September", "October", "November",
                "December", "" };
        if (retVal.length != val.length)
            fail("Returned wrong array: " + retVal.length);
        for (int i = 0; i < val.length; i++)
            assertTrue("Array values do not match", retVal[i].equals(val[i]));
    }

    /**
     * @tests java.text.DateFormatSymbols#getShortMonths()
     */
    public void test_getShortMonths() {
        // Test for method java.lang.String []
        // java.text.DateFormatSymbols.getShortMonths()
        String[] retVal = dfs.getShortMonths();
        String[] val = { "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul",
                "Aug", "Sep", "Oct", "Nov", "Dec", "" };
        if (retVal.length != val.length)
            fail("Returned wrong array");
        for (int i = 0; i < val.length; i++)
            assertTrue("Array values do not match", retVal[i].equals(val[i]));
    }

    /**
     * @tests java.text.DateFormatSymbols#getShortWeekdays()
     */
    public void test_getShortWeekdays() {
        // Test for method java.lang.String []
        // java.text.DateFormatSymbols.getShortWeekdays()
        String[] retVal = dfs.getShortWeekdays();
        String[] val = { "", "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat" };
        if (retVal.length != val.length)
            fail("Returned wrong array");
        for (int i = 0; i < val.length; i++)
            assertTrue("Array values do not match", retVal[i].equals(val[i]));
    }

    /**
     * @tests java.text.DateFormatSymbols#getWeekdays()
     */
    public void test_getWeekdays() {
        // Test for method java.lang.String []
        // java.text.DateFormatSymbols.getWeekdays()
        String[] retVal = dfs.getWeekdays();
        String[] val = { "", "Sunday", "Monday", "Tuesday", "Wednesday",
                "Thursday", "Friday", "Saturday" };
        if (retVal.length != val.length)
            fail("Returned wrong array");
        for (int i = 0; i < val.length; i++)
            assertTrue("Array values do not match", retVal[i].equals(val[i]));
    }

    /**
     * @tests java.text.DateFormatSymbols#getZoneStrings()
     */
    public void test_getZoneStrings() {
        // Test for method java.lang.String [][]
        // java.text.DateFormatSymbols.getZoneStrings()
        String[][] val = { { "XX" }, { "YY" } };
        dfs.setZoneStrings(val);
        String[][] retVal = dfs.getZoneStrings();
        if (retVal.length != val.length)
            fail("Returned wrong array");
        for (int i = 0; i < val.length; i++)
            assertTrue("Failed to set strings", Arrays
                    .equals(retVal[i], val[i]));
    }

    /**
     * @tests java.text.DateFormatSymbols#hashCode()
     */
    public void test_hashCode() {
        // Test for method int java.text.DateFormatSymbols.hashCode()
        int hc1 = dfs.hashCode();
        int hc2 = dfs.hashCode();
        assertTrue("hashCode() returned inconsistent number : " + hc1 + " - "
                + hc2, hc1 == hc2);

        assertTrue("hashCode() returns different values for equal() objects",
                dfs.hashCode() == dfs.clone().hashCode());
    }

    /**
     * @tests java.text.DateFormatSymbols#setAmPmStrings(java.lang.String[])
     */
    public void test_setAmPmStrings$Ljava_lang_String() {
        // Test for method void
        // java.text.DateFormatSymbols.setAmPmStrings(java.lang.String [])
        String[] val = { "XX", "YY" };
        dfs.setAmPmStrings(val);
        String[] retVal = dfs.getAmPmStrings();
        if (retVal.length != val.length)
            fail("Returned wrong array");
        for (int i = 0; i < val.length; i++)
            assertTrue("Failed to set strings", retVal[i].equals(val[i]));
    }

    /**
     * @tests java.text.DateFormatSymbols#setEras(java.lang.String[])
     */
    public void test_setEras$Ljava_lang_String() {
        // Test for method void
        // java.text.DateFormatSymbols.setEras(java.lang.String [])
        String[] val = { "XX", "YY" };
        dfs.setEras(val);
        String[] retVal = dfs.getEras();
        if (retVal.length != val.length)
            fail("Returned wrong array");
        for (int i = 0; i < val.length; i++)
            assertTrue("Failed to set strings", retVal[i].equals(val[i]));
    }

    /**
     * @tests java.text.DateFormatSymbols#setLocalPatternChars(java.lang.String)
     */
    public void test_setLocalPatternCharsLjava_lang_String() {
        // Test for method void
        // java.text.DateFormatSymbols.setLocalPatternChars(java.lang.String)
        dfs.setLocalPatternChars("GyMZZkHmsSEHHFwWahKz");
        String retVal = dfs.getLocalPatternChars();
        String val = "GyMZZkHmsSEHHFwWahKz";
        assertTrue("Returned incorrect pattern string", retVal.equals(val));

        try {
            // Regression for HARMONY-466
            new DateFormatSymbols().setLocalPatternChars(null);
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
            // expected
        }
    }

    /**
     * @tests java.text.DateFormatSymbols#setMonths(java.lang.String[])
     */
    public void test_setMonths$Ljava_lang_String() {
        // Test for method void
        // java.text.DateFormatSymbols.setMonths(java.lang.String [])
        String[] val = { "XX", "YY" };
        dfs.setMonths(val);
        String[] retVal = dfs.getMonths();
        assertTrue("Return is identical", retVal != dfs.getMonths());
        if (retVal.length != val.length)
            fail("Returned wrong array");
        for (int i = 0; i < val.length; i++)
            assertTrue("Failed to set strings", retVal[i].equals(val[i]));
    }

    /**
     * @tests java.text.DateFormatSymbols#setShortMonths(java.lang.String[])
     */
    public void test_setShortMonths$Ljava_lang_String() {
        // Test for method void
        // java.text.DateFormatSymbols.setShortMonths(java.lang.String [])
        String[] val = { "XX", "YY" };
        dfs.setShortMonths(val);
        String[] retVal = dfs.getShortMonths();
        assertTrue("Return is identical", retVal != dfs.getShortMonths());
        if (retVal.length != val.length)
            fail("Returned wrong array");
        for (int i = 0; i < val.length; i++)
            assertTrue("Failed to set strings", retVal[i].equals(val[i]));
    }

    /**
     * @tests java.text.DateFormatSymbols#setShortWeekdays(java.lang.String[])
     */
    public void test_setShortWeekdays$Ljava_lang_String() {
        // Test for method void
        // java.text.DateFormatSymbols.setShortWeekdays(java.lang.String [])
        String[] val = { "XX", "YY" };
        dfs.setShortWeekdays(val);
        String[] retVal = dfs.getShortWeekdays();
        assertTrue("Return is identical", retVal != dfs.getShortWeekdays());
        if (retVal.length != val.length)
            fail("Returned wrong array");
        for (int i = 0; i < val.length; i++)
            assertTrue("Failed to set strings", retVal[i].equals(val[i]));
    }

    /**
     * @tests java.text.DateFormatSymbols#setWeekdays(java.lang.String[])
     */
    public void test_setWeekdays$Ljava_lang_String() {
        // Test for method void
        // java.text.DateFormatSymbols.setWeekdays(java.lang.String [])
        String[] val = { "XX", "YY" };
        dfs.setWeekdays(val);
        String[] retVal = dfs.getWeekdays();
        assertTrue("Return is identical", retVal != dfs.getWeekdays());
        if (retVal.length != val.length)
            fail("Returned wrong array");
        for (int i = 0; i < val.length; i++)
            assertTrue("Failed to set strings", retVal[i].equals(val[i]));
    }

    /**
     * @tests java.text.DateFormatSymbols#setZoneStrings(java.lang.String[][])
     */
    public void test_setZoneStrings$$Ljava_lang_String() {
        // Test for method void
        // java.text.DateFormatSymbols.setZoneStrings(java.lang.String [][])
        String[][] val = { { "XX" }, { "YY" } };
        dfs.setZoneStrings(val);
        String[][] retVal = dfs.getZoneStrings();
        assertTrue("get returns identical", retVal != dfs.getZoneStrings());
        assertTrue("get[0] returns identical", retVal[0] != dfs
                .getZoneStrings()[0]);
        assertTrue("get returned identical", retVal != val);
        if (retVal.length != val.length)
            fail("Returned wrong array");
        for (int i = 0; i < val.length; i++)
            assertTrue("Failed to set strings: " + retVal[i], Arrays.equals(
                    retVal[i], val[i]));
    }

    /**
     * Sets up the fixture, for example, open a network connection. This method
     * is called before a test is executed.
     */
    protected void setUp() {
        dfs = new DateFormatSymbols(new Locale("en", "us"));
    }

    /**
     * Tears down the fixture, for example, close a network connection. This
     * method is called after a test is executed.
     */
    protected void tearDown() {
    }
}
