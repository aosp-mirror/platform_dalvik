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

package org.apache.harmony.sql.tests.java.sql;

import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestTargets;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetNew;

import java.sql.Timestamp;
import java.util.Date;
import java.util.TimeZone;

import org.apache.harmony.testframework.serialization.SerializationTest;
import junit.framework.TestCase;

@TestTargetClass(Timestamp.class)
/**
 * JUnit Testcase for the java.sql.Timestamp class
 * 
 */

public class TimestampTest extends TestCase {

    static class MockTimestamp extends Timestamp{
        private String holiday;

        public MockTimestamp(long theTime) {
            super(theTime);
            holiday = "Christmas";
        }

        // Constructor should not call this public API,
        // since it may be overrided to use variables uninitialized.
        public void setTime(long theTime){
            super.setTime(theTime);
            holiday.hashCode();
        }
    }

    static long TIME_TEST1 = 38720231; // 10:45:20.231 GMT

    static long TIME_TEST2 = 80279000; // 22:17:59.000 GMT

    static long TIME_TEST3 = -38720691; // 13:14:39.309 GMT

    static long TIME_COMPARE = 123498845;

    static long TIME_EARLY = -2347889122L;// A time well before the Epoch

    static long TIME_LATE = 2347889122L; // A time well after the Epoch

    static String STRING_TEST1 = "1970-01-01 10:45:20.231"; // "1970-01-01
                                                            // 10:45:20.231000000";

    static String STRING_TEST2 = "1970-01-01 22:17:59.0"; // "1970-01-01
                                                            // 22:17:59.000000000";

    static String STRING_TEST3 = "1969-12-31 13:14:39.309"; // "1969-12-31
                                                            // 13:14:39.309000000";

    static String STRING_INVALID1 = "ABCDEFGHI";

    static String STRING_INVALID2 = "233104";

    static String STRING_INVALID3 = "21-43-48";

    // A timepoint in the correct format but with numeric values out of range
    // ...this is accepted despite being a crazy date specification
    // ...it is treated as the correct format date 3000-06-08 12:40:06.875 !!
    static String STRING_OUTRANGE = "2999-15-99 35:99:66.875";

    static long[] TIME_ARRAY = { TIME_TEST1, TIME_TEST2, TIME_TEST3 };

    static int[] YEAR_ARRAY = { 70, 70, 69 };

    static int[] MONTH_ARRAY = { 0, 0, 11 };

    static int[] DATE_ARRAY = { 1, 1, 31 };

    static int[] HOURS_ARRAY = { 10, 22, 13 };

    static int[] MINUTES_ARRAY = { 45, 17, 14 };

    static int[] SECONDS_ARRAY = { 20, 59, 39 };

    static int[] NANOS_ARRAY = { 231000000, 000000000, 309000000 };

    static int[] NANOS_ARRAY2 = { 137891990, 635665198, 109985421 };

    static String[] STRING_NANOS_ARRAY = { "1970-01-01 10:45:20.13789199",
            "1970-01-01 22:17:59.635665198", "1969-12-31 13:14:39.109985421" };

    static String[] STRING_GMT_ARRAY = { STRING_TEST1, STRING_TEST2,
            STRING_TEST3 };

    static String[] STRING_LA_ARRAY = { "02:45:20", "14:17:59", "05:14:40" };

    static String[] STRING_JP_ARRAY = { "19:45:20", "07:17:59", "22:14:40" };

    static String[] INVALID_STRINGS = { STRING_INVALID1, STRING_INVALID2,
            STRING_INVALID3 };

    // Timezones
    static String TZ_LONDON = "GMT"; // GMT (!) PS London != GMT (?!?)

    static String TZ_PACIFIC = "America/Los_Angeles"; // GMT - 8

    static String TZ_JAPAN = "Asia/Tokyo"; // GMT + 9

    static String[] TIMEZONES = { TZ_LONDON, TZ_PACIFIC, TZ_JAPAN };

    static String[][] STRING_ARRAYS = { STRING_GMT_ARRAY, STRING_LA_ARRAY,
            STRING_JP_ARRAY };

    /*
     * Constructor test
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Incorrect parameter checking missed",
        method = "Timestamp",
        args = {long.class}
    )
    public void testTimestamplong() {
        Timestamp theTimestamp = new Timestamp(TIME_TEST1);

        // The Timestamp should have been created
        assertNotNull(theTimestamp);

        Timestamp mockTimestamp = new MockTimestamp(TIME_TEST1);
        assertNotNull(mockTimestamp);
    } // end method testTimestamplong

    /*
     * Constructor test
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "Timestamp",
        args = {int.class, int.class, int.class, int.class, int.class, int.class, int.class}
    )
    @SuppressWarnings("deprecation")
    public void testTimestampintintintintintintint() {
        int[][] valid = { { 99, 2, 14, 17, 52, 3, 213577212 }, // 0 valid
                { 0, 0, 1, 0, 0, 0, 0 }, // 1 valid
                { 106, 11, 31, 23, 59, 59, 999999999 }, // 2 valid
                { 106, 11, 31, 23, 59, 61, 999999999 }, // 5 Seconds out of
                                                        // range
                { 106, 11, 31, 23, 59, -1, 999999999 }, // 6 Seconds out of
                                                        // range
                { 106, 11, 31, 23, 61, 59, 999999999 }, // 7 Minutes out of
                                                        // range
                { 106, 11, 31, 23, -1, 59, 999999999 }, // 8 Minutes out of
                                                        // range
                { 106, 11, 31, 25, 59, 59, 999999999 }, // 9 Hours out of range
                { 106, 11, 31, -1, 59, 59, 999999999 }, // 10 Hours out of range
                { 106, 11, 35, 23, 59, 59, 999999999 }, // 11 Days out of range
                { 106, 11, -1, 23, 59, 59, 999999999 }, // 12 Days out of range
                { 106, 15, 31, 23, 59, 59, 999999999 }, // 13 Months out of
                                                        // range
                { 106, -1, 31, 23, 59, 59, 999999999 }, // 14 Months out of
                                                        // range
                { -10, 11, 31, 23, 59, 59, 999999999 }, // 15 valid - Years
                                                        // negative
        };

        for (int[] element : valid) {
            Timestamp theTimestamp = new Timestamp(element[0],
                    element[1], element[2], element[3],
                    element[4], element[5], element[6]);
            assertNotNull("Timestamp not generated: ", theTimestamp);
        } // end for
        
        int[][] invalid = {
                { 106, 11, 31, 23, 59, 59, 1999999999 }, 
                { 106, 11, 31, 23, 59, 59, -999999999 },
        };
        for (int[] element : invalid) {
            try {
                new Timestamp(element[0],
                        element[1], element[2], element[3],
                        element[4], element[5], element[6]);
                fail("Should throw IllegalArgumentException");
            } catch (IllegalArgumentException e) {
                // expected
            }
        }

    } // end method testTimestampintintintintintintint

    /*
     * Method test for setTime
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setTime",
        args = {long.class}
    )
    public void testSetTimelong() {
        // First set the timezone to GMT
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));

        Timestamp theTimestamp = new Timestamp(TIME_TEST1);

        for (int i = 0; i < TIME_ARRAY.length; i++) {
            theTimestamp.setTime(TIME_ARRAY[i]);

            assertEquals(TIME_ARRAY[i], theTimestamp.getTime());
            assertEquals(NANOS_ARRAY[i], theTimestamp.getNanos());
        } // end for

    } // end method testsetTimelong

    /*
     * Method test for getTime
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getTime",
        args = {}
    )
    public void testGetTime() {
        // First set the timezone to GMT
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));

        for (long element : TIME_ARRAY) {
            Timestamp theTimestamp = new Timestamp(element);
            assertEquals(element, theTimestamp.getTime());
        } // end for

    } // end method testgetTime

    /*
     * Method test for getYear
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Deprecation",
        method = "getYear",
        args = {}
    )
    @SuppressWarnings("deprecation")
    public void testGetYear() {
        for (int i = 0; i < TIME_ARRAY.length; i++) {
            Timestamp theTimestamp = new Timestamp(TIME_ARRAY[i]);
            assertEquals(YEAR_ARRAY[i], theTimestamp.getYear());
        } // end for

    } // end method testgetYear

    /*
     * Method test for getMonth
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Deprecation",
        method = "getMonth",
        args = {}
    )
    @SuppressWarnings("deprecation")
    public void testGetMonth() {
        for (int i = 0; i < TIME_ARRAY.length; i++) {
            Timestamp theTimestamp = new Timestamp(TIME_ARRAY[i]);
            assertEquals(MONTH_ARRAY[i], theTimestamp.getMonth());
        } // end for

    } // end method testgetMonth

    /*
     * Method test for getDate
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Deprecation",
        method = "getDate",
        args = {}
    )
    @SuppressWarnings("deprecation")
    public void testGetDate() {
    	TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
        for (int i = 0; i < TIME_ARRAY.length; i++) {
            Timestamp theTimestamp = new Timestamp(TIME_ARRAY[i]);
            assertEquals(DATE_ARRAY[i], theTimestamp.getDate());
        } // end for

    } // end method testgetDate

    /*
     * Method test for getHours
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Deprecation",
        method = "getHours",
        args = {}
    )
    @SuppressWarnings("deprecation")
    public void testGetHours() {
    	TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
        for (int i = 0; i < TIME_ARRAY.length; i++) {
            Timestamp theTimestamp = new Timestamp(TIME_ARRAY[i]);
            assertEquals(HOURS_ARRAY[i], theTimestamp.getHours());
        } // end for

    } // end method testgetHours

    /*
     * Method test for getMinutes
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Deprecation",
        method = "getMinutes",
        args = {}
    )
    @SuppressWarnings("deprecation")
    public void testGetMinutes() {
        for (int i = 0; i < TIME_ARRAY.length; i++) {
            Timestamp theTimestamp = new Timestamp(TIME_ARRAY[i]);
            assertEquals(MINUTES_ARRAY[i], theTimestamp.getMinutes());
        } // end for

    } // end method testgetMinutes

    /*
     * Method test for getSeconds
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Deprecation",
        method = "getSeconds",
        args = {}
    )
    @SuppressWarnings("deprecation")
    public void testGetSeconds() {
        for (int i = 0; i < TIME_ARRAY.length; i++) {
            Timestamp theTimestamp = new Timestamp(TIME_ARRAY[i]);
            assertEquals(SECONDS_ARRAY[i], theTimestamp.getSeconds());
        } // end for

    } // end method testgetSeconds

    /*
     * Method test for valueOf
     */
    static String theExceptionMessage = "Timestamp format must be yyyy-mm-dd hh:mm:ss.fffffffff";

    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "valueOf",
        args = {java.lang.String.class}
    )
    public void testValueOfString() {
    	TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
        for (int i = 0; i < TIME_ARRAY.length; i++) {
            Timestamp theTimestamp = new Timestamp(TIME_ARRAY[i]);
            Timestamp theTimestamp2 = Timestamp.valueOf(STRING_GMT_ARRAY[i]);
            assertEquals(theTimestamp, theTimestamp2);
        } // end for

        // Test for a string in correct format but with number values out of
        // range
        Timestamp theTimestamp = Timestamp.valueOf(STRING_OUTRANGE);
        assertNotNull(theTimestamp);
        /*
         * System.out.println("testValueOfString: outrange timestamp: " +
         * theTimestamp.toString() );
         */

        for (String element : INVALID_STRINGS) {
            try {
                Timestamp.valueOf(element);
                fail("Should throw IllegalArgumentException.");
            } catch (IllegalArgumentException e) {
                // expected
            } // end try

        } // end for

    } // end method testvalueOfString

    /*
     * Method test for valueOf
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "valueOf",
        args = {java.lang.String.class}
    )
    public void testValueOfString1() {
    	TimeZone.setDefault(TimeZone.getTimeZone("GMT"));

        Timestamp theReturn;

        theReturn = Timestamp.valueOf("1970-01-01 10:45:20.231");
        assertEquals("Wrong result for time test", 38720231,
                theReturn.getTime());
        assertEquals("Wrong result for nanos test", 231000000,
                theReturn.getNanos());

        theReturn = Timestamp.valueOf("1970-01-01 10:45:20.231987654");
        assertEquals("Wrong result for time test", 38720231,
                theReturn.getTime());
        assertEquals("Wrong result for nanos test", 231987654,
                theReturn.getNanos());

        theReturn = Timestamp.valueOf("1970-01-01 22:17:59.0");
        assertEquals("Wrong result for time test", 80279000,
                theReturn.getTime());
        assertEquals("Wrong result for nanos test", 0,
                theReturn.getNanos());

        theReturn = Timestamp.valueOf("1969-12-31 13:14:39.309");
        assertEquals("Wrong result for time test", -38720691,
                theReturn.getTime());
        assertEquals("Wrong result for nanos test", 309000000,
                theReturn.getNanos());

        theReturn = Timestamp.valueOf("1970-01-01 10:45:20");
        assertEquals("Wrong result for time test", 38720000,
                theReturn.getTime());
        assertEquals("Wrong result for nanos test", 0,
                theReturn.getNanos());
        
        String[] invalid = {
                null,
                "ABCDEFGHI", 
                "233104", "1970-01-01 22:17:59.",
                "1970-01-01 10:45:20.231987654690645322",
                "1970-01-01 10:45:20&231987654",
                "1970-01-01 10:45:20.-31987654",
                "1970-01-01 10:45:20.ABCD87654", 
                "21-43-48",
        };
        for (String element : invalid) {
            try {
                theReturn = Timestamp.valueOf(element);
                fail("Should throw IllegalArgumentException for " + element);
            } catch (IllegalArgumentException e) {
                //expected
            }
        }

        // Regression test for HARMONY-5506
        String date = "1970-01-01 22:17:59.0                 ";
        Timestamp t = Timestamp.valueOf(date);
        assertEquals(80279000,t.getTime());

    } // end method testValueOfString

    public void testValueOf_IAE() {
        try {
            java.sql.Timestamp.valueOf("2008-12-22 15:00:01.");
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }

        try {
            // bug of RI 5, passed on RI 6
            java.sql.Timestamp.valueOf("178548938-12-22 15:00:01.000000001");
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }

        try {
            java.sql.Timestamp.valueOf("2008-12-22 15:00:01.0000000011");
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    /*
     * Method test for toString
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "toString",
        args = {}
    )
    public void testToString() {
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));

        for (int i = 0; i < TIME_ARRAY.length; i++) {
            Timestamp theTimestamp = new Timestamp(TIME_ARRAY[i]);
            assertEquals("Wrong conversion for test " + i, STRING_GMT_ARRAY[i],
                    theTimestamp.toString());
        } // end for

		Timestamp t1 = new Timestamp(Long.MIN_VALUE);
		assertEquals("292278994-08-17 07:12:55.192", t1.toString()); //$NON-NLS-1$

		Timestamp t2 = new Timestamp(Long.MIN_VALUE + 1);
		assertEquals("292278994-08-17 07:12:55.193", t2.toString()); //$NON-NLS-1$

		Timestamp t3 = new Timestamp(Long.MIN_VALUE + 807);
		assertEquals("292278994-08-17 07:12:55.999", t3.toString()); //$NON-NLS-1$

		Timestamp t4 = new Timestamp(Long.MIN_VALUE + 808);
		assertEquals("292269055-12-02 16:47:05.0", t4.toString()); //$NON-NLS-1$
    } // end method testtoString

    private void testToString(String timeZone, long[] theTimeStamps, String[] theTimeStampStrings) {
    	TimeZone.setDefault(TimeZone.getTimeZone(timeZone));
        for (int i = 0; i < TIME_ARRAY.length; i++) {
            Timestamp theTimestamp = new Timestamp(theTimeStamps[i]);
            assertEquals(theTimeStampStrings[i], theTimestamp.toString());
        } // end for

    }

    /*
     * Method test for getNanos
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getNanos",
        args = {}
    )
    public void testGetNanos() {
        for (int i = 0; i < TIME_ARRAY.length; i++) {
            Timestamp theTimestamp = new Timestamp(TIME_ARRAY[i]);
            assertEquals("Wrong conversion for test " + i, NANOS_ARRAY[i],
                    theTimestamp.getNanos());
        } // end for

    } // end method testgetNanos

    /*
     * Method test for setNanos
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setNanos",
        args = {int.class}
    )
    public void testSetNanosint() {
    	TimeZone.setDefault(TimeZone.getTimeZone("GMT"));

        int[] NANOS_INVALID = { -137891990, 1635665198, -1 };
        for (int i = 0; i < TIME_ARRAY.length; i++) {
            Timestamp theTimestamp = new Timestamp(TIME_ARRAY[i]);

            theTimestamp.setNanos(NANOS_ARRAY2[i]);

            assertEquals("Wrong conversion for test " + i, NANOS_ARRAY2[i],
                    theTimestamp.getNanos());
            // Also check that these Timestamps with detailed nanos values
            // convert to
            // strings correctly
            assertEquals("Wrong conversion for test " + i,
                    STRING_NANOS_ARRAY[i], theTimestamp.toString());
        } // end for

        for (int i = 0; i < NANOS_INVALID.length; i++) {
            Timestamp theTimestamp = new Timestamp(TIME_ARRAY[i]);
            int originalNanos = theTimestamp.getNanos();
            try {
                theTimestamp.setNanos(NANOS_INVALID[i]);
                fail("Should throw IllegalArgumentException");
            } catch (IllegalArgumentException e) {
                //expected
            } // end try

            assertEquals(originalNanos, theTimestamp.getNanos());
        } // end for

    } // end method testsetNanosint

    /*
     * Method test for equals
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "equals",
        args = {java.sql.Timestamp.class}
    )
    public void testEqualsTimestamp() {
        for (long element : TIME_ARRAY) {
            Timestamp theTimestamp = new Timestamp(element);
            Timestamp theTimestamp2 = new Timestamp(element);

            assertTrue(theTimestamp.equals(theTimestamp2));
        } // end for

        Timestamp theTest = new Timestamp(TIME_COMPARE);

        for (long element : TIME_ARRAY) {
            Timestamp theTimestamp = new Timestamp(element);
            assertFalse(theTimestamp.equals(theTest));
        } // end for
        
        // Regression for HARMONY-526
        assertFalse(new Timestamp(0).equals((Timestamp) null));
    } // end method testequalsTimestamp

    /*
     * Method test for equals
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "equals",
        args = {java.lang.Object.class}
    )
    public void testEqualsObject() {
        for (long element : TIME_ARRAY) {
            Timestamp theTimestamp = new Timestamp(element);

            Object theTimestamp2 = new Timestamp(element);

            assertTrue(theTimestamp.equals(theTimestamp2));
        } // end for

        Object theTest = new Timestamp(TIME_COMPARE);

        for (long element : TIME_ARRAY) {
            Timestamp theTimestamp = new Timestamp(element);

            assertFalse(theTimestamp.equals(theTest));
        } // end for

        Object nastyTest = new String("Test ");
        Timestamp theTimestamp = new Timestamp(TIME_ARRAY[1]);
        assertFalse(theTimestamp.equals(nastyTest));

        // Regression for HARMONY-526
        assertFalse(new Timestamp(0).equals((Object) null));
    } // end method testequalsObject

    /*
     * Method test for before
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "before",
        args = {java.sql.Timestamp.class}
    )
    public void testBeforeTimestamp() {
        Timestamp theTest = new Timestamp(TIME_LATE);

        for (long element : TIME_ARRAY) {
            Timestamp theTimestamp = new Timestamp(element);

            assertTrue(theTimestamp.before(theTest));
        } // end for

        theTest = new Timestamp(TIME_EARLY);

        for (long element : TIME_ARRAY) {
            Timestamp theTimestamp = new Timestamp(element);

            assertFalse(theTimestamp.before(theTest));
        } // end for

        for (long element : TIME_ARRAY) {
            theTest = new Timestamp(element);
            Timestamp theTimestamp = new Timestamp(element);

            assertFalse(theTimestamp.before(theTest));
            theTest.setNanos(theTest.getNanos() + 1);
            assertTrue(theTimestamp.before(theTest));
        } // end for

    } // end method testbeforeTimestamp

    /*
     * Method test for after
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "after",
        args = {java.sql.Timestamp.class}
    )
    public void testAfterTimestamp() {
        Timestamp theTest = new Timestamp(TIME_LATE);

        for (long element : TIME_ARRAY) {
            Timestamp theTimestamp = new Timestamp(element);

            assertFalse(theTimestamp.after(theTest));
        } // end for

        theTest = new Timestamp(TIME_EARLY);

        for (long element : TIME_ARRAY) {
            Timestamp theTimestamp = new Timestamp(element);

            assertTrue(theTimestamp.after(theTest));
        } // end for

        for (long element : TIME_ARRAY) {
            theTest = new Timestamp(element);
            Timestamp theTimestamp = new Timestamp(element);

            assertFalse(theTimestamp.after(theTest));
            theTimestamp.setNanos(theTimestamp.getNanos() + 1);
            assertTrue(theTimestamp.after(theTest));
        } // end for

    } // end method testafterTimestamp

    /*
     * Method test for compareTo
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "compareTo",
        args = {java.sql.Timestamp.class}
    )
    @SuppressWarnings("deprecation")
    public void testCompareToTimestamp() {
        Timestamp theTest = new Timestamp(TIME_EARLY);
        Timestamp theTest2 = new Timestamp(TIME_LATE);

        for (long element : TIME_ARRAY) {
            Timestamp theTimestamp = new Timestamp(element);
            Timestamp theTimestamp2 = new Timestamp(element);

            assertTrue(theTimestamp.compareTo(theTest) > 0);
            assertTrue(theTimestamp.compareTo(theTest2) < 0);
            assertEquals(0, theTimestamp.compareTo(theTimestamp2));
        } // end for
        
        Timestamp t1 = new Timestamp(-1L);
        Timestamp t2 = new Timestamp(-1L);
        
        t1.setTime(Long.MIN_VALUE);
        t2.setDate(Integer.MIN_VALUE);
        assertEquals(1, t1.compareTo(t2));
        assertEquals(-1, t2.compareTo(t1));

        t1.setTime(Long.MAX_VALUE);
        t2.setTime(Long.MAX_VALUE - 1);
        assertEquals(1, t1.compareTo(t2));
        assertEquals(-1, t2.compareTo(t1));

        t1.setTime(Integer.MAX_VALUE);
        t2.setTime(Integer.MAX_VALUE);
        assertEquals(0, t1.compareTo(t2));
        assertEquals(0, t2.compareTo(t1));

    } // end method testcompareToTimestamp

    /**
     * @tests java.sql.Timestamp#compareTo(java.util.Date)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "compareTo",
        args = {java.util.Date.class}
    )
    public void testCompareToDate() {
        Date theTest = new Timestamp(TIME_EARLY);
        Date theTest2 = new Timestamp(TIME_LATE);

        for (long element : TIME_ARRAY) {
            Timestamp theTimestamp = new Timestamp(element);
            Date theTimestamp2 = new Timestamp(element);

            assertTrue(theTimestamp.compareTo(theTest) > 0);
            assertTrue(theTimestamp.compareTo(theTest2) < 0);
            assertEquals(0, theTimestamp.compareTo(theTimestamp2));
        } // end for

        Date nastyTest = new Date();
        Timestamp theTimestamp = new Timestamp(TIME_ARRAY[1]);
        try {
            theTimestamp.compareTo(nastyTest);
            // It throws ClassCastException in JDK 1.5.0_06 but in 1.5.0_07 it
            // does not throw the expected exception.
            fail("testCompareToObject: Did not get expected ClassCastException");
        } catch (ClassCastException e) {
            // Should get here
            /*
             * System.out.println("testCompareToObject: ClassCastException as
             * expected"); System.out.println("Exception message: " +
             * e.getMessage());
             */
        } // end try

    } // end method testcompareToObject
    
    /**
     * @tests serialization/deserialization compatibility.
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Serialization test",
        method = "!SerializationSelf",
        args = {}
    )
    public void testSerializationSelf() throws Exception {
        Timestamp object = new Timestamp(100L);
        SerializationTest.verifySelf(object);
    }

    /**
     * @tests serialization/deserialization compatibility with RI.
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Serialization test",
        method = "!SerializationGolden",
        args = {}
    )
    public void testSerializationCompatibility() throws Exception {
        Timestamp object = new Timestamp(100L);
        SerializationTest.verifyGolden(this, object);
    }
    
    /**
     * @tests java.sql.Timestamp#toString()
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "toString",
        args = {}
    )
    public void test_toString() {

        Timestamp t1 = new Timestamp(Long.MIN_VALUE);
        assertEquals("292278994-08-17 07:12:55.192", t1.toString()); //$NON-NLS-1$

        Timestamp t2 = new Timestamp(Long.MIN_VALUE + 1);
        assertEquals("292278994-08-17 07:12:55.193", t2.toString()); //$NON-NLS-1$

        Timestamp t3 = new Timestamp(Long.MIN_VALUE + 807);
        assertEquals("292278994-08-17 07:12:55.999", t3.toString()); //$NON-NLS-1$

        Timestamp t4 = new Timestamp(Long.MIN_VALUE + 808);
        assertEquals("292269055-12-02 16:47:05.0", t4.toString()); //$NON-NLS-1$
    }

    // Reset defualt timezone
    TimeZone defaultTimeZone = TimeZone.getDefault();

    protected void tearDown() {
        TimeZone.setDefault(defaultTimeZone);
    }
} // end class TimestampTest
