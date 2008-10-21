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

import java.sql.Time;
import java.util.TimeZone;

import junit.framework.TestCase;

/**
 * JUnit Testcase for the java.sql.Time class
 * 
 */
public class TimeTest extends TestCase {

    static long TIME_TEST1 = 38720000; // 10:45:20 GMT

    static long TIME_TEST2 = 80279000; // 22:17:59 GMT

    static long TIME_TEST3 = -38720000; // 13:14:40 GMT

    static String STRING_TEST1 = "10:45:20";

    static String STRING_TEST2 = "22:17:59";

    static String STRING_TEST3 = "13:14:40";

    static String STRING_INVALID1 = "ABCDEFGHI";

    static String STRING_INVALID2 = "233104";

    static String STRING_INVALID3 = "21-43-48";

    static String STRING_OUTRANGE = "35:99:66";

    static long[] TIME_ARRAY = { TIME_TEST1, TIME_TEST2, TIME_TEST3 };

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

    @SuppressWarnings("deprecation")
    public void testTimeintintint() {
        Time theTime = new Time(10, 45, 20);

        // The date should have been created
        assertNotNull(theTime);
    } // end method testTimeintintint()

    public void testTime() {
        Time theTime = new Time(TIME_TEST1);

        // The date should have been created
        assertNotNull(theTime);
    } // end method testTime()

    public void testToString() {
        // Loop through the timezones testing the String conversion for each
        for (int i = 0; i < TIME_ARRAY.length; i++) {
            testToString(TIMEZONES[i], TIME_ARRAY, STRING_ARRAYS[i]);
        } // end for

    } // end method test

    private void testToString(String timeZone, long[] theTimes,
            String[] theTimeStrings) {
        // Set the timezone
        TimeZone.setDefault(TimeZone.getTimeZone(timeZone));

        for (int i = 0; i < theTimes.length; i++) {
            // Create the Time object
            Time theTime = new Time(theTimes[i]);
            // Convert to a time string ... and compare
            String JDBCString = theTime.toString();
            assertEquals(theTimeStrings[i], JDBCString);
        } // end for

    } // end testToString( String, long[], String[] )

    /*
     * Method test for valueOf
     */
    public void testValueOfString() {
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));

        
        Time[] theReturns = { new Time(38720000), new Time(80279000),
                new Time(47680000)};
        String[] validTime = { "10:45:20", "22:17:59", "13:14:40", };
        String[] invalidTime = { null, "ABCDEFGHI", "233104", "21-43-48" };
        
        for (int i = 0; i < validTime.length; i++) {
            Time theReturn = Time.valueOf(validTime[i]);
                assertEquals(theReturns[i], theReturn);
        } // end for
        
        for (String element : invalidTime) {
            try {
                Time.valueOf(element);
                fail("Should throw IllegalArgumentException");
            } catch (IllegalArgumentException e) {
                //expected
            }
        }

    } // end method testValueOfString

    public void testSetTime() {
        // Ensure that the timezone is set to GMT
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));

        Time theTime = new Time(TIME_TEST1);
        assertEquals(STRING_TEST1, theTime.toString());

        theTime.setTime(TIME_TEST2);
        assertEquals(STRING_TEST2, theTime.toString());
    } // end method testSetTime()

    @SuppressWarnings("deprecation")
    public void testSetDate() {
        Time theTime = new Time(TIME_TEST1);

        try {
            theTime.setDate(10);
            fail("Should throw IllegalArgumentException.");
        } catch (IllegalArgumentException e) {
            //expected
        } // end try
    } // end method testSetDate()

    @SuppressWarnings("deprecation")
    public void testSetMonth() {
        Time theTime = new Time(TIME_TEST1);

        try {
            theTime.setMonth(2);
            fail("Should throw IllegalArgumentException.");
        } catch (IllegalArgumentException e) {
            //expected
        } // end try
    } // end method testSetMonth()

    @SuppressWarnings("deprecation")
    public void testSetYear() {
        Time theTime = new Time(TIME_TEST1);

        try {
            theTime.setYear(99);
            fail("Should throw IllegalArgumentException.");
        } catch (IllegalArgumentException e) {
            //expected
        } // end try
    } // end method testSetYear()

    @SuppressWarnings("deprecation")
    public void testGetDate() {
        Time theTime = new Time(TIME_TEST1);

        try {
            theTime.getDate();
            fail("Should throw IllegalArgumentException.");
        } catch (IllegalArgumentException e) {
            //expected
        } // end try
    } // end method test

    @SuppressWarnings("deprecation")
    public void testGetDay() {
        Time theTime = new Time(TIME_TEST1);

        try {
            theTime.getDay();
            fail("Should throw IllegalArgumentException.");
        } catch (IllegalArgumentException e) {
            //expected
        } // end try
    } // end method test

    @SuppressWarnings("deprecation")
    public void testGetMonth() {
        Time theTime = new Time(TIME_TEST1);

        try {
            theTime.getMonth();
            fail("Should throw IllegalArgumentException.");
        } catch (IllegalArgumentException e) {
            //expected
        } // end try
    } // end method test

    @SuppressWarnings("deprecation")
    public void testGetYear() {
        Time theTime = new Time(TIME_TEST1);

        try {
            theTime.getYear();
            fail("Should throw IllegalArgumentException.");
        } catch (IllegalArgumentException e) {
            //expected
        } // end try
    } // end method test
    
    /**
     * @tests java.sql.Time#valueOf(String )
     */
     public void test_valueOf_IllegalArgumentException() {
            try{
                Time.valueOf("15:43:12:34");
                fail("should throw NumberFormatException");
            } catch (NumberFormatException e) {
                //expected
            }
            
            try{
                Time.valueOf(":10:07:01");
                fail("should throw IllegalArgumentException");
            } catch (IllegalArgumentException e) {
                //expected
            }
            
            try{
                Time.valueOf("::01");
                fail("should throw IllegalArgumentException");
            } catch (IllegalArgumentException e) {
                //expected
            }
            
            try{
                Time.valueOf("11::");
                fail("should throw IllegalArgumentException");
            } catch (IllegalArgumentException e) {
                //expected
            }
            
            try{
                Time.valueOf(":01:");
                fail("should throw IllegalArgumentException");
            } catch (IllegalArgumentException e) {
                //expected
            }
            
            try{
                Time.valueOf(":10:w2:01");
                fail("should throw IllegalArgumentException");
            } catch (IllegalArgumentException e) {
                //expected
            }
        
            try{
                Time.valueOf("07:w2:");
                fail("should throw IllegalArgumentException");
            } catch (IllegalArgumentException e) {
                //expected
            }
            
            try{
                Time.valueOf("17:w2:w2");
                fail("should throw NumberFormatException");
            } catch (NumberFormatException e) {
                //expected
            }
            
            try{
                Time.valueOf("16::01");
                fail("should throw NumberFormatException");
            } catch (NumberFormatException e) {
                //expected
            }
        }
} // end class TimeTest

