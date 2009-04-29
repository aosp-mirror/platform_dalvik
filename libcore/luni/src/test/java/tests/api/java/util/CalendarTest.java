/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package tests.api.java.util;

import dalvik.annotation.KnownFailure;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargets;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetClass; 

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

@TestTargetClass(Calendar.class) 
public class CalendarTest extends junit.framework.TestCase {
    
    Locale defaultLocale;

    /**
     * @tests java.util.Calendar#set(int, int)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "set",
        args = {int.class, int.class}
    )
    public void test_setII() {
        // Test for correct result defined by the last set field
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("EST"));

        cal.clear();
        cal.set(Calendar.YEAR, 2002);
        assertTrue("Incorrect result 0: " + cal.getTime().getTime(), cal
                .getTime().getTime() == 1009861200000L);

        cal.clear();
        cal.set(Calendar.YEAR, 2002);
        cal.set(Calendar.MONTH, Calendar.MARCH);
        assertTrue("Incorrect result 0a: " + cal.getTime(), cal.getTime()
                .getTime() == 1014958800000L);

        cal.clear();
        cal.set(Calendar.YEAR, 2002);
        cal.set(Calendar.DATE, 24);
        assertTrue("Incorrect result 0b: " + cal.getTime(), cal.getTime()
                .getTime() == 1011848400000L);

        cal.set(Calendar.MONTH, Calendar.OCTOBER);
        cal.set(Calendar.DATE, 31);
        cal.set(Calendar.MONTH, Calendar.NOVEMBER);
        cal.set(Calendar.DATE, 26);
        assertTrue("Incorrect month: " + cal.get(Calendar.MONTH), cal
                .get(Calendar.MONTH) == Calendar.NOVEMBER);

        int dow = cal.get(Calendar.DAY_OF_WEEK);
        cal.set(Calendar.DATE, 27);
        assertTrue("Incorrect DAY_OF_WEEK: " + cal.get(Calendar.DAY_OF_WEEK)
                + " expected: " + dow, cal.get(Calendar.DAY_OF_WEEK) != dow);

        cal.clear();
        cal.set(Calendar.YEAR, 2002);
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        assertTrue("Incorrect result 0c1: " + cal.getTime().getTime(), cal
                .getTime().getTime() == 1010379600000L);

        cal.clear();
        cal.set(Calendar.YEAR, 2002);
        cal.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY);
        assertTrue("Incorrect result 0c2: " + cal.getTime().getTime(), cal
                .getTime().getTime() == 1009861200000L);

        cal.clear();
        cal.set(Calendar.YEAR, 2002);
        cal.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY);
        assertTrue("Incorrect result 0c3: " + cal.getTime(), cal.getTime()
                .getTime() == 1010034000000L);

        cal.clear();
        cal.set(Calendar.YEAR, 2002);
        cal.set(Calendar.WEEK_OF_MONTH, 2);
        assertTrue("Incorrect result 0d: " + cal.getTime(), cal.getTime()
                .getTime() == 1010293200000L);

        cal.clear();
        cal.set(Calendar.YEAR, 2002);
        cal.set(Calendar.DAY_OF_WEEK_IN_MONTH, 2);
        assertTrue("Incorrect result 0e: " + cal.getTime(), cal.getTime()
                .getTime() == 1010898000000L);

        cal.clear();
        cal.set(Calendar.YEAR, 2002);
        cal.set(Calendar.WEEK_OF_YEAR, 11);
        assertTrue("Incorrect result 0f: " + cal.getTime(), cal.getTime()
                .getTime() == 1015736400000L);

        cal.clear();
        cal.set(Calendar.YEAR, 2002);
        cal.set(Calendar.DATE, 24);
        cal.set(Calendar.WEEK_OF_YEAR, 11);
        assertTrue("Incorrect result 0g: " + cal.getTime(), cal.getTime()
                .getTime() == 1011848400000L);

        cal.clear();
        cal.set(Calendar.YEAR, 2002);
        cal.get(Calendar.WEEK_OF_YEAR); // Force fields to compute
        cal.set(Calendar.WEEK_OF_YEAR, 11);
        assertTrue("Incorrect result 0h: " + cal.getTime(), cal.getTime()
                .getTime() == 1015909200000L);

        // WEEK_OF_YEAR has priority over MONTH/DATE
        cal.clear();
        cal.set(Calendar.YEAR, 2002);
        cal.set(Calendar.DAY_OF_YEAR, 170);
        cal.set(Calendar.WEEK_OF_YEAR, 11);
        cal.set(Calendar.MONTH, Calendar.JANUARY);
        cal.set(Calendar.DATE, 5);
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        assertTrue("Incorrect result 1: " + cal.getTime(), cal.getTime()
                .getTime() == 1015822800000L);

        // WEEK_OF_YEAR has priority over MONTH/DATE
        cal.clear();
        cal.set(Calendar.YEAR, 2002);
        cal.set(Calendar.WEEK_OF_YEAR, 11);
        cal.set(Calendar.MONTH, Calendar.JANUARY);
        cal.set(Calendar.DATE, 5);
        cal.set(Calendar.DAY_OF_YEAR, 170);
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        assertTrue("Incorrect result 1a: " + cal.getTime(), cal.getTime()
                .getTime() == 1015822800000L);

        // DAY_OF_WEEK has no effect when other fields not set
        cal.clear();
        cal.set(Calendar.YEAR, 2002);
        cal.set(Calendar.MONTH, Calendar.MARCH);
        cal.set(Calendar.DATE, 11);
        cal.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY);
        assertTrue("Incorrect result 1b: " + cal.getTime(), cal.getTime()
                .getTime() == 1015822800000L);

        // WEEK_OF_MONTH has priority
        cal.clear();
        cal.set(Calendar.YEAR, 2002);
        cal.set(Calendar.WEEK_OF_YEAR, 12);
        cal.set(Calendar.DAY_OF_WEEK_IN_MONTH, 1);
        cal.set(Calendar.WEEK_OF_MONTH, 3);
        cal.set(Calendar.MONTH, Calendar.MARCH);
        cal.set(Calendar.DATE, 5);
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        assertTrue("Incorrect result 2: " + cal.getTime(), cal.getTime()
                .getTime() == 1015822800000L);

        // DAY_OF_WEEK_IN_MONTH has priority over WEEK_OF_YEAR
        cal.clear();
        cal.set(Calendar.YEAR, 2002);
        cal.set(Calendar.WEEK_OF_YEAR, 12);
        cal.set(Calendar.DAY_OF_WEEK_IN_MONTH, 2);
        cal.set(Calendar.MONTH, Calendar.MARCH);
        cal.set(Calendar.DATE, 5);
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        assertTrue("Incorrect result 3: " + cal.getTime(), cal.getTime()
                .getTime() == 1015822800000L);

        // WEEK_OF_MONTH has priority, MONTH not set
        cal.clear();
        cal.set(Calendar.YEAR, 2002);
        cal.set(Calendar.WEEK_OF_YEAR, 12);
        cal.set(Calendar.DAY_OF_WEEK_IN_MONTH, 1);
        cal.set(Calendar.WEEK_OF_MONTH, 3);
        cal.set(Calendar.DATE, 25);
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        assertTrue("Incorrect result 4: " + cal.getTime(), cal.getTime()
                .getTime() == 1010984400000L);

        // WEEK_OF_YEAR has priority when MONTH set last and DAY_OF_WEEK set
        cal.clear();
        cal.set(Calendar.YEAR, 2002);
        cal.set(Calendar.WEEK_OF_YEAR, 11);
        cal.set(Calendar.DATE, 25);
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        cal.set(Calendar.MONTH, Calendar.JANUARY);
        assertTrue("Incorrect result 5: " + cal.getTime(), cal.getTime()
                .getTime() == 1015822800000L);

        // Use MONTH/DATE when WEEK_OF_YEAR set but not DAY_OF_WEEK
        cal.clear();
        cal.set(Calendar.YEAR, 2002);
        cal.set(Calendar.WEEK_OF_YEAR, 12);
        cal.set(Calendar.DATE, 11);
        cal.set(Calendar.MONTH, Calendar.MARCH);
        assertTrue("Incorrect result 5a: " + cal.getTime(), cal.getTime()
                .getTime() == 1015822800000L);

        // Use MONTH/DATE when DAY_OF_WEEK is not set
        cal.clear();
        cal.set(Calendar.YEAR, 2002);
        cal.set(Calendar.WEEK_OF_YEAR, 12);
        cal.set(Calendar.DATE, 11);
        cal.set(Calendar.WEEK_OF_MONTH, 1);
        cal.set(Calendar.MONTH, Calendar.MARCH);
        assertTrue("Incorrect result 5b: " + cal.getTime(), cal.getTime()
                .getTime() == 1015822800000L);

        // WEEK_OF_MONTH has priority
        cal.clear();
        cal.set(Calendar.YEAR, 2002);
        cal.set(Calendar.WEEK_OF_YEAR, 12);
        cal.set(Calendar.DATE, 5);
        cal.set(Calendar.WEEK_OF_MONTH, 3);
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        cal.set(Calendar.MONTH, Calendar.MARCH);
        assertTrue("Incorrect result 5c: " + cal.getTime(), cal.getTime()
                .getTime() == 1015822800000L);

        // DATE has priority when set last
        cal.clear();
        cal.set(Calendar.YEAR, 2002);
        cal.set(Calendar.WEEK_OF_YEAR, 12);
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        cal.set(Calendar.MONTH, Calendar.MARCH);
        cal.set(Calendar.DATE, 11);
        assertTrue("Incorrect result 6: " + cal.getTime(), cal.getTime()
                .getTime() == 1015822800000L);

        // DATE has priority when set last, MONTH not set
        cal.clear();
        cal.set(Calendar.YEAR, 2002);
        cal.set(Calendar.WEEK_OF_YEAR, 12);
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        cal.set(Calendar.DATE, 14);
        assertTrue("Incorrect result 7: " + cal.getTime(), cal.getTime()
                .getTime() == 1010984400000L);

        // DAY_OF_YEAR has priority when MONTH set last and DATE not set
        cal.clear();
        cal.set(Calendar.YEAR, 2002);
        cal.set(Calendar.DAY_OF_YEAR, 70);
        cal.set(Calendar.MONTH, Calendar.JANUARY);
        assertTrue("Incorrect result 8: " + cal.getTime(), cal.getTime()
                .getTime() == 1015822800000L);

        // DAY/MONTH has priority when DATE set after DAY_OF_YEAR
        cal.clear();
        cal.set(Calendar.YEAR, 2002);
        cal.set(Calendar.DAY_OF_YEAR, 170);
        cal.set(Calendar.DATE, 11);
        cal.set(Calendar.MONTH, Calendar.MARCH);
        assertTrue("Incorrect result 8a: " + cal.getTime(), cal.getTime()
                .getTime() == 1015822800000L);

        // DAY_OF_YEAR has priority when set after DATE
        cal.clear();
        cal.set(Calendar.YEAR, 2002);
        cal.set(Calendar.DATE, 15);
        cal.set(Calendar.DAY_OF_YEAR, 70);
        cal.set(Calendar.MONTH, Calendar.JANUARY);
        assertTrue("Incorrect result 8b: " + cal.getTime(), cal.getTime()
                .getTime() == 1015822800000L);

        // DATE has priority when set last
        cal.clear();
        cal.set(Calendar.YEAR, 2002);
        cal.set(Calendar.DAY_OF_YEAR, 70);
        cal.set(Calendar.DATE, 14);
        assertTrue("Incorrect result 9: " + cal.getTime(), cal.getTime()
                .getTime() == 1010984400000L);

        // DATE has priority when set last
        cal.clear();
        cal.set(Calendar.YEAR, 2002);
        cal.set(Calendar.WEEK_OF_YEAR, 15);
        cal.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY);
        cal.set(Calendar.DATE, 14);
        assertTrue("Incorrect result 9a: " + cal.getTime(), cal.getTime()
                .getTime() == 1010984400000L);

        cal.clear();
        cal.set(Calendar.YEAR, 2002);
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        cal.set(Calendar.DATE, 14);
        cal.set(Calendar.WEEK_OF_YEAR, 11);
        assertTrue("Incorrect result 9b: " + cal.getTime(), cal.getTime()
                .getTime() == 1015822800000L);

        cal.clear();
        cal.set(Calendar.YEAR, 2002);
        cal.set(Calendar.DATE, 14);
        cal.set(Calendar.WEEK_OF_YEAR, 11);
        assertTrue("Incorrect result 9c: " + cal.getTime(), cal.getTime()
                .getTime() == 1010984400000L);

        cal.clear();
        cal.set(Calendar.YEAR, 2002);
        cal.set(Calendar.WEEK_OF_MONTH, 1);
        cal.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY);
        cal.set(Calendar.MONTH, Calendar.MARCH);
        cal.set(Calendar.DATE, 11);
        assertTrue("Incorrect result 9d: " + cal.getTime(), cal.getTime()
                .getTime() == 1015822800000L);

        // DAY_OF_YEAR has priority when DAY_OF_MONTH set last and other fields
        // not set
        cal.clear();
        cal.set(Calendar.YEAR, 2002);
        cal.set(Calendar.DAY_OF_YEAR, 70);
        cal.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY);
        assertTrue("Incorrect result 10: " + cal.getTime(), cal.getTime()
                .getTime() == 1015822800000L);

        // MONTH/DATE has priority when DAY_OF_WEEK_IN_MONTH set last but
        // DAY_OF_WEEK not set
        cal.clear();
        cal.set(Calendar.YEAR, 2002);
        cal.set(Calendar.DATE, 11);
        cal.set(Calendar.MONTH, Calendar.MARCH);
        cal.set(Calendar.DAY_OF_WEEK_IN_MONTH, 1);
        assertTrue("Incorrect result 11: " + cal.getTime(), cal.getTime()
                .getTime() == 1015822800000L);

        // MONTH/DATE has priority when WEEK_OF_YEAR set last but DAY_OF_WEEK
        // not set
        cal.clear();
        cal.set(Calendar.YEAR, 2002);
        cal.set(Calendar.DATE, 11);
        cal.set(Calendar.MONTH, Calendar.MARCH);
        cal.set(Calendar.WEEK_OF_YEAR, 15);
        assertTrue("Incorrect result 12: " + cal.getTime(), cal.getTime()
                .getTime() == 1015822800000L);

        // MONTH/DATE has priority when WEEK_OF_MONTH set last but DAY_OF_WEEK
        // not set
        cal.clear();
        cal.set(Calendar.YEAR, 2002);
        cal.set(Calendar.DATE, 11);
        cal.set(Calendar.MONTH, Calendar.MARCH);
        cal.set(Calendar.WEEK_OF_MONTH, 1);
        assertTrue("Incorrect result 13: " + cal.getTime(), cal.getTime()
                .getTime() == 1015822800000L);

        // Ensure last date field set is reset after computing
        cal.clear();
        cal.set(Calendar.YEAR, 2002);
        cal.set(Calendar.DAY_OF_YEAR, 111);
        cal.get(Calendar.YEAR);
        cal.set(Calendar.MONTH, Calendar.MARCH);
        cal.set(Calendar.AM_PM, Calendar.AM);
        assertTrue("Incorrect result 14: " + cal.getTime(), cal.getTime()
                .getTime() == 1016686800000L);

        int hour = cal.get(Calendar.HOUR);
        cal.set(Calendar.HOUR, hour);
        cal.set(Calendar.AM_PM, Calendar.PM);
        assertEquals("AM_PM not changed", Calendar.PM, cal.get(Calendar.AM_PM));
        // setting AM_PM without HOUR should not have any affect
        cal.set(Calendar.AM_PM, Calendar.AM);
        assertEquals("AM_PM was changed 1",
                Calendar.AM, cal.get(Calendar.AM_PM));
        int hourOfDay = cal.get(Calendar.HOUR_OF_DAY);
        hour = cal.get(Calendar.HOUR);
        cal.set(Calendar.AM_PM, Calendar.PM);
        assertEquals("AM_PM was changed 2",
                Calendar.PM, cal.get(Calendar.AM_PM));
        assertEquals(hour, cal.get(Calendar.HOUR));
        assertEquals(hourOfDay + 12, cal.get(Calendar.HOUR_OF_DAY));
        
        // regression test for Harmony-2122
        cal = Calendar.getInstance();
        int oldValue = cal.get(Calendar.AM_PM);
        int newValue = (oldValue == Calendar.AM) ? Calendar.PM : Calendar.AM;
        cal.set(Calendar.AM_PM, newValue);
        newValue = cal.get(Calendar.AM_PM);
        assertTrue(newValue != oldValue);
        
        cal.setLenient(false);
        
        try {
            cal.set(-1, 3);
            fail("ArrayIndexOutOfBoundsException expected");
        } catch (ArrayIndexOutOfBoundsException e) {
            //expected
        }
        
        try {
            cal.set(Calendar.FIELD_COUNT + 1, 3);
            fail("ArrayIndexOutOfBoundsException expected");
        } catch (ArrayIndexOutOfBoundsException e) {
            //expected
        }
    }

    /**
     * @tests java.util.Calendar#setTime(java.util.Date)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setTime",
        args = {java.util.Date.class}
    )
    public void test_setTimeLjava_util_Date() {
        Calendar cal = Calendar.getInstance();
        // Use millisecond time for testing in Core
        cal.setTime(new Date(884581200000L)); // (98, Calendar.JANUARY, 12)
        assertEquals("incorrect millis", 884581200000L, cal.getTime().getTime());
        cal.setTimeZone(TimeZone.getTimeZone("EST"));
        cal.setTime(new Date(943506000000L)); // (99, Calendar.NOVEMBER, 25)
        assertTrue("incorrect fields", cal.get(Calendar.YEAR) == 1999
                && cal.get(Calendar.MONTH) == Calendar.NOVEMBER
                && cal.get(Calendar.DATE) == 25);
    }

    /**
     * @tests java.util.Calendar#compareTo(Calendar)
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies NullPointerException.",
        method = "compareTo",
        args = {java.util.Calendar.class}
    )
    public void test_compareToLjava_util_Calendar_null() {
        Calendar cal = Calendar.getInstance();
        try {
            cal.compareTo(null);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    /**
     * @tests java.util.Calendar#compareTo(Calendar)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "compareTo",
        args = {java.util.Calendar.class}
    )
    public void test_compareToLjava_util_Calendar() {
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(1997, 12, 13, 23, 57);

        Calendar anotherCal = Calendar.getInstance();
        anotherCal.clear();
        anotherCal.set(1997, 12, 13, 23, 57);
        assertEquals(0, cal.compareTo(anotherCal));

        anotherCal = Calendar.getInstance();
        anotherCal.clear();
        anotherCal.set(1997, 11, 13, 24, 57);
        assertEquals(1, cal.compareTo(anotherCal));

        anotherCal = Calendar.getInstance();
        anotherCal.clear();
        anotherCal.set(1997, 12, 13, 23, 58);
        assertEquals(-1, cal.compareTo(anotherCal));
        
        try {
            cal.compareTo(null);
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
            //expected
        }

        MockCalendar mc = new MockCalendar();

        try {
            cal.compareTo(mc);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
            //expected
        }
    }

    /**
     * @tests java.util.Calendar#clone()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "clone",
        args = {}
    )
    public void test_clone() {
        // Regression for HARMONY-475
        Calendar cal = Calendar.getInstance();
        cal.set(2006, 5, 6, 11, 35);
        Calendar anotherCal = (Calendar) cal.clone();
        // should be deep clone
        assertNotSame("getTimeZone", cal.getTimeZone(), anotherCal
                .getTimeZone());
    }

    /**
     * @tests java.util.Calendar#getTimeInMillis()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getTimeInMillis",
        args = {}
    )
    public void test_getTimeInMillis() {
        Calendar cal = Calendar.getInstance();

        int year = Integer.MIN_VALUE + 71;
        cal.setTimeZone(TimeZone.getTimeZone("GMT"));;
        cal.set(Calendar.YEAR, year + 1900);
        cal.set(Calendar.MONTH, Calendar.JANUARY);
        cal.set(Calendar.DATE, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        assertEquals(6017546357372606464L, cal.getTimeInMillis());
    }

    /**
     * @tests {@link java.util.Calendar#getActualMaximum(int)}
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getActualMaximum",
        args = {int.class}
    )
    public void test_getActualMaximum_I() {
        Calendar c = new MockCalendar();
        assertEquals("should be equal to 0", 0, c.getActualMaximum(0));
    }
    
    /**
     * @tests {@link java.util.Calendar#getActualMinimum(int)}
     */
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getActualMinimum",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "Calendar",
            args = {}
        )
    })
    public void test_getActualMinimum_I() {
        Calendar c = new MockCalendar();
        assertEquals("should be equal to 0", 0, c.getActualMinimum(0));
    }

    private class MockCalendar extends Calendar {

        public MockCalendar() {
            super();
        }

        public MockCalendar(TimeZone default1, Locale germany) {
            super(default1, germany);
        }

        @Override
        public void add(int field, int value) {
        }

        @Override
        protected void computeFields() {
        }

        @Override
        protected void computeTime() {
            throw new IllegalArgumentException();
        }

        @Override
        public int getGreatestMinimum(int field) {
            return 0;
        }

        @Override
        public int getLeastMaximum(int field) {
            return 0;
        }

        @Override
        public int getMaximum(int field) {
            return 0;
        }

        @Override
        public int getMinimum(int field) {
            return 0;
        }

        @Override
        public void roll(int field, boolean increment) {
        }
    }
    
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "Calendar",
        args = {java.util.TimeZone.class, java.util.Locale.class}
    )
    public void test_ConstructorLjava_utilTimeZoneLjava_util_Locale() {
        assertNotNull(new MockCalendar(TimeZone.getDefault(), Locale.GERMANY));
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test calls dummy implementation of abstract method.",
        method = "add",
        args = {int.class, int.class}
    )
    public void test_addII() {
        MockCalendar mc = new MockCalendar();
        
        mc.add(Calendar.DAY_OF_YEAR, 7);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "after",
        args = {java.lang.Object.class}
    )
    public void test_afterLjava_lang_Object() {
        MockCalendar mcBefore = new MockCalendar();
        MockCalendar mc       = new MockCalendar();
        MockCalendar mcAfter  = new MockCalendar();
        MockCalendar mcSame   = new MockCalendar();

        mcBefore.setTimeInMillis(1000);
        mc.setTimeInMillis(10000);
        mcAfter.setTimeInMillis(100000);
        mcSame.setTimeInMillis(10000);
        
        assertTrue(mc.after(mcBefore));
        assertFalse(mc.after(mcAfter));
        assertFalse(mc.after(mcSame));
        assertFalse(mc.after(mc));
        assertFalse(mc.after(new String()));
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "before",
        args = {java.lang.Object.class}
    )
    public void test_beforeLjava_lang_Object() {
        MockCalendar mcBefore = new MockCalendar();
        MockCalendar mc       = new MockCalendar();
        MockCalendar mcAfter  = new MockCalendar();
        MockCalendar mcSame   = new MockCalendar();
    
        mcBefore.setTimeInMillis(1000);
        mc.setTimeInMillis(10000);
        mcAfter.setTimeInMillis(100000);
        mcSame.setTimeInMillis(10000);
        
        assertFalse(mc.before(mcBefore));
        assertTrue(mc.before(mcAfter));
        assertFalse(mc.before(mcSame));
        assertFalse(mc.before(mc));
        assertFalse(mc.before(new String()));
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "clear",
        args = {}
    )
    public void test_clear() {
        MockCalendar mc1 = new MockCalendar();
        MockCalendar mc2 = new MockCalendar();
        
        assertTrue(mc1.toString().equals(mc2.toString()));
        mc1.set(2008, Calendar.SEPTEMBER, 23, 18, 0, 0);
        assertFalse(mc1.toString().equals(mc2.toString()));
        mc1.clear();
        assertTrue(mc1.toString().equals(mc2.toString()));
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "clear",
        args = {int.class}
    )
    public void test_clearI() {
        MockCalendar mc1 = new MockCalendar();
        MockCalendar mc2 = new MockCalendar();
        
        assertTrue(mc1.toString().equals(mc2.toString()));
        mc1.set(2008, Calendar.SEPTEMBER, 23, 18, 0, 0);
        assertFalse(mc1.toString().equals(mc2.toString()));
        mc1.clear(Calendar.YEAR);
        mc1.clear(Calendar.MONTH);
        mc1.clear(Calendar.DAY_OF_MONTH);
        mc1.clear(Calendar.HOUR_OF_DAY);
        mc1.clear(Calendar.MINUTE);
        mc1.clear(Calendar.SECOND);
        mc1.clear(Calendar.MILLISECOND);
        assertTrue(mc1.toString().equals(mc2.toString()));
    }

    class Mock_Calendar extends Calendar {
        boolean flagComplete = false;
        @Override
        public void add(int field, int amount) {
            
        }

        @Override
        protected void computeFields() {
            this.set(MONTH, this.internalGet(MONTH)%12);
        }

        @Override
        protected void computeTime() {
        }

        @Override
        public int getGreatestMinimum(int field) {
            return 0;
        }

        @Override
        public int getLeastMaximum(int field) {
            return 0;
        }

        @Override
        public int getMaximum(int field) {
            return 0;
        }

        @Override
        public int getMinimum(int field) {
            return 0;
        }

        @Override
        public void roll(int field, boolean up) {
        }
        
        @Override
        public void complete() {
            computeTime();
            computeFields();
            flagComplete = true;
        }
        
        public boolean isCompleted () {
            return flagComplete;
        }

        public int internalGetField(int field) {
            return super.internalGet(field);
        }
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "complete",
        args = {}
    )
    public void test_complete() {
        Mock_Calendar cal = new Mock_Calendar();
        
        assertFalse(cal.isCompleted());
        cal.setTimeInMillis(1000);
        cal.get(Calendar.MONTH);
        assertTrue(cal.isCompleted());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "computeFields",
        args = {}
    )
    public void test_computeFields() {
        Calendar cal1 = Calendar.getInstance(TimeZone.getTimeZone("GMT+2"), defaultLocale);
        Calendar cal2 = Calendar.getInstance(TimeZone.getTimeZone("GMT+2"), defaultLocale);
        
        cal1.setTimeInMillis(1222185600225L);
        cal2.set(2008, Calendar.SEPTEMBER, 23, 18, 0, 0);
        assertFalse(cal1.toString().equals(cal2.toString()));
        cal1.get(Calendar.YEAR);
        cal2.getTimeInMillis();
        cal1.set(Calendar.MILLISECOND, 0);
        cal2.set(Calendar.MILLISECOND, 0);
        // tests fails in this line.
        assertTrue(cal1.toString().equals(cal2.toString()));
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "equals",
        args = {java.lang.Object.class}
    )
    public void test_equals() {
        Calendar cal1 = Calendar.getInstance(TimeZone.getTimeZone("GMT+2"), defaultLocale);
        Calendar cal2 = Calendar.getInstance(TimeZone.getTimeZone("GMT+2"), defaultLocale);
        
        cal1.setTimeInMillis(1222185600225L);
        cal2.set(2008, Calendar.SEPTEMBER, 23, 18, 0, 0);
        assertFalse(cal1.equals(cal2));
        cal1.get(Calendar.YEAR);
        cal2.getTimeInMillis();
        cal1.set(Calendar.MILLISECOND, 0);
        cal2.set(Calendar.MILLISECOND, 0);
        // tests fails on following line.
        assertTrue(cal1.equals(cal2));
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "get",
        args = {int.class}
    )
    public void test_getI() {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+2"), defaultLocale);
        
        cal.setTimeInMillis(1222185600225L);
        assertEquals(cal.get(Calendar.ERA), 1);
        assertEquals(cal.get(Calendar.YEAR), 2008);
        assertEquals(cal.get(Calendar.MONTH), Calendar.SEPTEMBER);
        assertEquals(cal.get(Calendar.DAY_OF_MONTH), 23);
        // Following line returns wrong value. Behavior uncompatible with RI.
        assertEquals(cal.get(Calendar.HOUR_OF_DAY), 18);
        assertEquals(cal.get(Calendar.MINUTE), 0);
        
        try {
            cal.get(-1);
            fail("ArrayIndexOutOfBoundsException expected");
        } catch (ArrayIndexOutOfBoundsException e) {
            //expected
        }

        try {
            cal.get(Calendar.FIELD_COUNT + 1);
            fail("ArrayIndexOutOfBoundsException expected");
        } catch (ArrayIndexOutOfBoundsException e) {
            //expected
        }
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getAvailableLocales",
        args = {}
    )
    public void test_getAvailableLocales() {
        assertNotNull(Calendar.getAvailableLocales());
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getFirstDayOfWeek",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getInstance",
            args = {}
        )
    })
    @KnownFailure("Some locales were removed last minute in cupcake")
    public void test_getFirstDayOfWeek() {
        Calendar cal = Calendar.getInstance();

        assertEquals(Calendar.SUNDAY, cal.getFirstDayOfWeek());
        Locale.setDefault(Locale.FRANCE);
        cal = Calendar.getInstance();
        assertEquals(Calendar.MONDAY, cal.getFirstDayOfWeek());
        Locale.setDefault(Locale.US);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getInstance",
        args = {java.util.Locale.class}
    )
    @KnownFailure("Some locales were removed last minute in cupcake")
    public void test_getInstanceLjava_util_Locale() {
        Calendar cal1 = Calendar.getInstance(Locale.FRANCE);
        Locale.setDefault(Locale.FRANCE);
        Calendar cal2 = Calendar.getInstance();
        assertSame(cal1.getFirstDayOfWeek(), cal2.getFirstDayOfWeek());
        Locale.setDefault(Locale.US);
        cal2 = Calendar.getInstance();
        assertNotSame(cal1.getFirstDayOfWeek(), cal2.getFirstDayOfWeek());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getInstance",
        args = {java.util.TimeZone.class}
    )
    public void test_get_InstanceLjava_util_TimeZone() {
        Calendar cal1 = Calendar.getInstance(TimeZone.getTimeZone("GMT-6"));
        Calendar cal2 = Calendar.getInstance(TimeZone.getTimeZone("GMT+1"));
        assertNotSame(cal1.getTimeZone().getRawOffset(), cal2.getTimeZone().getRawOffset());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getInstance",
        args = {java.util.TimeZone.class, java.util.Locale.class}
    )
    @KnownFailure("Some locales were removed last minute in cupcake")
    public void test_getInstanceLjava_util_TimeZoneLjava_util_Locale() {
        Calendar cal1 = Calendar.getInstance(TimeZone.getTimeZone("GMT-6"), Locale.FRANCE);
        Locale.setDefault(Locale.FRANCE);
        Calendar cal2 = Calendar.getInstance(TimeZone.getTimeZone("GMT+1"));
        assertSame(cal1.getFirstDayOfWeek(), cal2.getFirstDayOfWeek());
        assertNotSame(cal1.getTimeZone().getRawOffset(), cal2.getTimeZone().getRawOffset());
        Locale.setDefault(Locale.US);
        cal2 = Calendar.getInstance(TimeZone.getTimeZone("GMT+1"));
        assertNotSame(cal1.getFirstDayOfWeek(), cal2.getFirstDayOfWeek());
        assertNotSame(cal1.getTimeZone().getRawOffset(), cal2.getTimeZone().getRawOffset());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getMinimalDaysInFirstWeek",
        args = {}
    )
    @KnownFailure("Some locales were removed last minute in cupcake")
    public void test_getMinimalDaysInFirstWeek() {
        Calendar cal = Calendar.getInstance();
        assertTrue(cal.getMinimalDaysInFirstWeek()==1);
        Locale.setDefault(Locale.FRANCE);
        cal = Calendar.getInstance();
        assertTrue(cal.getMinimalDaysInFirstWeek()==4);
        Locale.setDefault(Locale.US);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getTime",
        args = {}
    )
    public void test_getTime() {
        Calendar cal = Calendar.getInstance();
        Date d = new Date(1222185600225L);
        
        cal.setTimeInMillis(1222185600225L);
        assertEquals(d.getTime(), cal.getTimeInMillis());
        assertEquals(d, cal.getTime());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getTimeZone",
        args = {}
    )
    public void test_getTimeZone() {
        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(TimeZone.getTimeZone("GMT-6"));
        
        assertEquals(TimeZone.getTimeZone("GMT-6"), cal.getTimeZone());
        cal = Calendar.getInstance(TimeZone.getTimeZone("GMT-8"));
        assertEquals(TimeZone.getTimeZone("GMT-8"), cal.getTimeZone());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "hashCode",
        args = {}
    )
    @KnownFailure("Some locales were removed last minute in cupcake")
    public void test_hashCode() {
        Calendar cal1 = Calendar.getInstance();
        Locale.setDefault(Locale.FRANCE);
        Calendar cal2 = Calendar.getInstance();
        Locale.setDefault(Locale.US);
        assertTrue(cal1.hashCode() != cal2.hashCode());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "internalGet",
        args = {int.class}
    )
    public void test_internalGet() {
        Mock_Calendar mc = new Mock_Calendar();
        assertEquals(0, mc.internalGetField(Calendar.MONTH));
        mc.set(Calendar.MONTH, 35);
        assertEquals(35, mc.internalGetField(Calendar.MONTH));
        assertEquals(11, mc.get(Calendar.MONTH));
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "isLenient",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "setLenient",
            args = {boolean.class}
        )
    })
    public void test_isLenient() {
        Calendar cal = Calendar.getInstance();
        assertTrue(cal.isLenient());
        cal.set(Calendar.MONTH, 35);
        cal.get(Calendar.MONTH);
        cal.setLenient(false);
        cal.set(Calendar.MONTH, 35);
        try {
            cal.get(Calendar.MONTH);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
            //expected
        }
        assertFalse(cal.isLenient());
        cal.setLenient(true);
        cal.set(Calendar.MONTH, 35);
        cal.get(Calendar.MONTH);
        assertTrue(cal.isLenient());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "isSet",
        args = {int.class}
    )
    public void test_isSet() {
        Calendar cal = Calendar.getInstance();
        cal.clear();
        assertFalse(cal.isSet(Calendar.MONTH));
        cal.set(Calendar.MONTH, 35);
        assertTrue(cal.isSet(Calendar.MONTH));
        assertFalse(cal.isSet(Calendar.YEAR));
        cal.get(Calendar.MONTH);
        assertTrue(cal.isSet(Calendar.YEAR));
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "roll",
        args = {int.class, int.class}
    )
    public void test_rollII() {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+2"), defaultLocale);
        
        cal.setTimeInMillis(1222185600225L);
        cal.roll(Calendar.DAY_OF_MONTH, 200);
        assertEquals(cal.get(Calendar.ERA), 1);
        assertEquals(cal.get(Calendar.YEAR), 2008);
        assertEquals(cal.get(Calendar.MONTH), Calendar.SEPTEMBER);
        assertEquals(cal.get(Calendar.DAY_OF_MONTH), 13);
        // Following line returns wrong value. Behavior uncompatible with RI.
        assertEquals(cal.get(Calendar.HOUR_OF_DAY), 18);
        assertEquals(cal.get(Calendar.MINUTE), 0);
        cal.roll(Calendar.DAY_OF_MONTH, -200);
        assertEquals(cal.get(Calendar.ERA), 1);
        assertEquals(cal.get(Calendar.YEAR), 2008);
        assertEquals(cal.get(Calendar.MONTH), Calendar.SEPTEMBER);
        assertEquals(cal.get(Calendar.DAY_OF_MONTH), 23);
        assertEquals(cal.get(Calendar.HOUR_OF_DAY), 18);
        assertEquals(cal.get(Calendar.MINUTE), 0);
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "set",
            args = {int.class, int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "setTimeInMillis",
            args = {long.class}
        )
    })
    public void test_setIII() {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+2"), defaultLocale);
        
        cal.setTimeInMillis(1222185600225L);
        assertEquals(1222185600225L, cal.getTimeInMillis());
        assertEquals(cal.get(Calendar.YEAR), 2008);
        assertEquals(cal.get(Calendar.MONTH), Calendar.SEPTEMBER);
        assertEquals(cal.get(Calendar.DAY_OF_MONTH), 23);
        assertEquals(cal.get(Calendar.SECOND), 0);
        
        cal.set(1970, Calendar.JANUARY, 1);
        assertEquals(cal.get(Calendar.ERA), 1);
        // Following line returns wrong value. Behavior uncompatible with RI.
        assertEquals(cal.get(Calendar.HOUR_OF_DAY), 18);
        assertEquals(cal.get(Calendar.MINUTE), 0);
        assertEquals(cal.get(Calendar.SECOND), 0);

        assertEquals(cal.get(Calendar.YEAR), 1970);
        assertEquals(cal.get(Calendar.MONTH), 0);
        assertEquals(cal.get(Calendar.DAY_OF_MONTH), 1);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "set",
        args = {int.class, int.class, int.class, int.class, int.class}
    )
    public void test_setIIIII() {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+2"), defaultLocale);
        
        cal.setTimeInMillis(1222185600225L);
        assertEquals(cal.get(Calendar.YEAR), 2008);
        assertEquals(cal.get(Calendar.MONTH), Calendar.SEPTEMBER);
        assertEquals(cal.get(Calendar.DAY_OF_MONTH), 23);
        // Following line returns wrong value. Behavior uncompatible with RI.
        assertEquals(cal.get(Calendar.HOUR_OF_DAY), 18);
        assertEquals(cal.get(Calendar.MINUTE), 0);
        assertEquals(cal.get(Calendar.SECOND), 0);
        
        cal.set(1970, Calendar.JANUARY, 1, 0, 10);
        assertEquals(cal.get(Calendar.ERA), 1);
        assertEquals(cal.get(Calendar.SECOND), 0);
    
        assertEquals(cal.get(Calendar.YEAR), 1970);
        assertEquals(cal.get(Calendar.MONTH), 0);
        assertEquals(cal.get(Calendar.DAY_OF_MONTH), 1);
        assertEquals(cal.get(Calendar.HOUR_OF_DAY), 0);
        assertEquals(cal.get(Calendar.MINUTE), 10);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "set",
        args = {int.class, int.class, int.class, int.class, int.class, int.class}
    )
    public void test_setIIIIII() {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+2"), defaultLocale);
        
        cal.setTimeInMillis(1222185600225L);
        assertEquals(cal.get(Calendar.YEAR), 2008);
        assertEquals(cal.get(Calendar.MONTH), Calendar.SEPTEMBER);
        assertEquals(cal.get(Calendar.DAY_OF_MONTH), 23);
        // Following line returns wrong value. Behavior uncompatible with RI.
        assertEquals(cal.get(Calendar.HOUR_OF_DAY), 18);
        assertEquals(cal.get(Calendar.MINUTE), 0);
        assertEquals(cal.get(Calendar.SECOND), 0);
        
        cal.set(1970, Calendar.JANUARY, 1, 0, 10, 33);
        assertEquals(cal.get(Calendar.ERA), 1);
    
        assertEquals(cal.get(Calendar.YEAR), 1970);
        assertEquals(cal.get(Calendar.MONTH), 0);
        assertEquals(cal.get(Calendar.DAY_OF_MONTH), 1);
        assertEquals(cal.get(Calendar.HOUR_OF_DAY), 0);
        assertEquals(cal.get(Calendar.MINUTE), 10);
        assertEquals(cal.get(Calendar.SECOND), 33);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setFirstDayOfWeek",
        args = {int.class}
    )
    public void test_setFirstDayOfWeekI() {
        Calendar cal = Calendar.getInstance();
        
        for (int i = 0; i < 10; i++) {
            cal.setFirstDayOfWeek(i);
            assertEquals(i, cal.getFirstDayOfWeek());
        }
        cal.setLenient(false);
        cal.setFirstDayOfWeek(10);
        cal.setFirstDayOfWeek(-10);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setMinimalDaysInFirstWeek",
        args = {int.class}
    )
    public void test_setMinimalDaysInFirstWeekI() {
        Calendar cal = Calendar.getInstance();
        
        for (int i = 0; i < 10; i++) {
            cal.setMinimalDaysInFirstWeek(i);
            assertEquals(i, cal.getMinimalDaysInFirstWeek());
        }
        cal.setLenient(false);
        cal.setMinimalDaysInFirstWeek(10);
        cal.setMinimalDaysInFirstWeek(-10);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setTimeZone",
        args = {java.util.TimeZone.class}
    )
    public void test_setTimeZoneLjava_util_TimeZone() {
        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(TimeZone.getTimeZone("GMT-6"));
        assertEquals(TimeZone.getTimeZone("GMT-6"), cal.getTimeZone());
        cal = Calendar.getInstance(TimeZone.getTimeZone("GMT-8"));
        cal.setTimeZone(TimeZone.getTimeZone("GMT-6"));
        assertEquals(TimeZone.getTimeZone("GMT-6"), cal.getTimeZone());
        
        cal.setTimeZone(null);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "toString",
        args = {}
    )
    public void test_toString() {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTimeZone(TimeZone.getTimeZone("GMT-6"));
        cal2.setTimeZone(TimeZone.getTimeZone("GMT-8"));
        cal1.set(Calendar.MILLISECOND, 0);
        cal2.set(Calendar.MILLISECOND, 0);
        assertFalse(cal1.toString().equals(cal2.toString()));
        cal1.setTimeZone(TimeZone.getTimeZone("GMT-8"));
        assertTrue(cal1.toString().equals(cal2.toString()));
    }
    
    @TestTargetNew(
        level = TestLevel.ADDITIONAL,
        method = "get",
        args = {int.class}
    )
    public void test_EdgeCases() {
        Locale.setDefault(Locale.US);
        Calendar c = Calendar.getInstance();
        
        c.setTimeInMillis(Long.MAX_VALUE);
        
        assertEquals(292278994, c.get(Calendar.YEAR));
        assertEquals(Calendar.AUGUST, c.get(Calendar.MONTH));
        assertEquals(17, c.get(Calendar.DAY_OF_MONTH));
        assertEquals(Calendar.SUNDAY, c.get(Calendar.DAY_OF_WEEK));
        assertEquals(7, c.get(Calendar.HOUR_OF_DAY));
        assertEquals(12, c.get(Calendar.MINUTE));
        assertEquals(55, c.get(Calendar.SECOND));
    }

    protected void setUp() {
        defaultLocale = Locale.getDefault();
        Locale.setDefault(Locale.US);
    }

    protected void tearDown() {
        Locale.setDefault(defaultLocale);
    }
}
