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

import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import junit.framework.Test;

public class DateFormatTest extends junit.framework.TestCase {

    private class MockDateFormat extends DateFormat {

        private static final long serialVersionUID = 1L;

        public MockDateFormat() {
            super();
        }

        @Override
        public Date parse(String source, ParsePosition pos) {
            // it is a fake
            return null;
        }

        @Override
        public StringBuffer format(Date date, StringBuffer toAppendTo,
                FieldPosition fieldPosition) {
            // it is a fake
            return null;
        }
    }

    /**
     * @tests java.text.DateFormat#DateFormat() Test of method
     *        java.text.DateFormat#DateFormat().
     */
    public void test_Constructor() {
        try {
            new MockDateFormat();
        } catch (Exception e) {
            fail("Unexpected exception " + e.toString());
        }
    }

    /**
     * @tests java.text.DateFormat#equals(java.lang.Object obj) Test of
     *        java.text.DateFormat#equals(java.lang.Object obj).
     */
    public void test_equalsLjava_lang_Object() {
        try {
            DateFormat format = DateFormat.getInstance();
            DateFormat clone = (DateFormat) format.clone();
            assertTrue("Clone and parent are not equaled", format.equals(clone));
            assertTrue("Clone is equal to other object", !clone
                    .equals(DateFormat.getTimeInstance()));
            format.setCalendar(Calendar.getInstance());
            assertTrue("Clone and parent are not equaled", format.equals(clone));
        } catch (Exception e) {
            fail("Unexpected exception " + e.toString());
        }
    }

    /**
     * @tests java.text.DateFormat#format(java.util.Date) Test of method
     *        java.text.DateFormat#format(java.util.Date).
     */
    public void test_formatLjava_util_Date() {
        try {
            DateFormat format = DateFormat.getDateTimeInstance(
                    DateFormat.SHORT, DateFormat.SHORT, Locale.US);
            Date current = new Date();
            String dtf = format.format(current);
            SimpleDateFormat sdf = new SimpleDateFormat("M/d/yy h:mm a");
            assertTrue("Incorrect date format", sdf.format(current).equals(dtf));
        } catch (Exception e) {
            fail("Unexpected exception " + e.toString());
        }
    }

    /**
     * @tests java.text.DateFormat#format(Object, StringBuffer, FieldPosition)
     *        Test of method java.text.DateFormat#format(Object, StringBuffer,
     *        FieldPosition)
     */
    public void test_formatLjava_lang_ObjectLjava_lang_StringBufferLjava_text_FieldPosition() {
        try {
            DateFormat format = DateFormat.getDateTimeInstance(
                    DateFormat.SHORT, DateFormat.SHORT, Locale.US);
            Date current = new Date();
            StringBuffer toAppend = new StringBuffer();
            FieldPosition fp = new FieldPosition(DateFormat.YEAR_FIELD);
            StringBuffer sb = format.format(current, toAppend, fp);
            SimpleDateFormat sdf = new SimpleDateFormat("M/d/yy h:mm a");
            assertTrue("Incorrect date format", sdf.format(current).equals(
                    sb.toString()));
            assertTrue("Incorrect beginIndex of filed position", fp
                    .getBeginIndex() == sb.lastIndexOf("/") + 1);
            assertTrue("Incorrect endIndex of filed position",
                    fp.getEndIndex() == sb.lastIndexOf("/") + 3);
        } catch (Exception e) {
            fail("Unexpected exception " + e.toString());
        }
    }

    /**
     * @tests java.text.DateFormat#clone()
     */
    public void test_clone() {
        DateFormat format = DateFormat.getInstance();
        DateFormat clone = (DateFormat) format.clone();
        assertTrue("Clone not equal", format.equals(clone));
        clone.getNumberFormat().setMinimumFractionDigits(123);
        assertTrue("Clone shares NumberFormat", !format.equals(clone));
    }

    /**
     * @tests java.text.DateFormat#getAvailableLocales()
     */
    public void test_getAvailableLocales() {
        Locale[] locales = DateFormat.getAvailableLocales();
        assertTrue("No locales", locales.length > 0);
        boolean english = false, german = false;
        for (int i = locales.length; --i >= 0;) {
            if (locales[i].equals(Locale.ENGLISH))
                english = true;
            if (locales[i].equals(Locale.GERMAN))
                german = true;
            DateFormat f1 = DateFormat.getDateTimeInstance(DateFormat.SHORT,
                    DateFormat.SHORT, locales[i]);
            assertTrue("Doesn't work",
                    f1.format(new Date()).getClass() == String.class);
        }
        assertTrue("Missing locales", english && german);
    }

    /**
     * @tests java.text.DateFormat#getCalendar()
     */
    public void test_getCalendar() {
        DateFormat format = DateFormat.getInstance();
        Calendar cal1 = format.getCalendar();
        Calendar cal2 = format.getCalendar();
        assertTrue("Calendars not identical", cal1 == cal2);
    }

    /**
     * @tests java.text.DateFormat#getDateInstance()
     */
    public void test_getDateInstance() {
        SimpleDateFormat f2 = (SimpleDateFormat) DateFormat.getDateInstance();
        assertTrue("Wrong class", f2.getClass() == SimpleDateFormat.class);
        assertTrue("Wrong default", f2.equals(DateFormat.getDateInstance(
                DateFormat.DEFAULT, Locale.getDefault())));
        assertTrue("Wrong symbols", f2.getDateFormatSymbols().equals(
                new DateFormatSymbols()));
        assertTrue("Doesn't work",
                f2.format(new Date()).getClass() == String.class);
    }

    /**
     * @tests java.text.DateFormat#getDateInstance(int)
     */
    public void test_getDateInstanceI() {
        assertTrue("Default not medium",
                DateFormat.DEFAULT == DateFormat.MEDIUM);

        SimpleDateFormat f2 = (SimpleDateFormat) DateFormat
                .getDateInstance(DateFormat.SHORT);
        assertTrue("Wrong class1", f2.getClass() == SimpleDateFormat.class);
        assertTrue("Wrong default1", f2.equals(DateFormat.getDateInstance(
                DateFormat.SHORT, Locale.getDefault())));
        assertTrue("Wrong symbols1", f2.getDateFormatSymbols().equals(
                new DateFormatSymbols()));
        assertTrue("Doesn't work1",
                f2.format(new Date()).getClass() == String.class);

        f2 = (SimpleDateFormat) DateFormat.getDateInstance(DateFormat.MEDIUM);
        assertTrue("Wrong class2", f2.getClass() == SimpleDateFormat.class);
        assertTrue("Wrong default2", f2.equals(DateFormat.getDateInstance(
                DateFormat.MEDIUM, Locale.getDefault())));
        assertTrue("Wrong symbols2", f2.getDateFormatSymbols().equals(
                new DateFormatSymbols()));
        assertTrue("Doesn't work2",
                f2.format(new Date()).getClass() == String.class);

        f2 = (SimpleDateFormat) DateFormat.getDateInstance(DateFormat.LONG);
        assertTrue("Wrong class3", f2.getClass() == SimpleDateFormat.class);
        assertTrue("Wrong default3", f2.equals(DateFormat.getDateInstance(
                DateFormat.LONG, Locale.getDefault())));
        assertTrue("Wrong symbols3", f2.getDateFormatSymbols().equals(
                new DateFormatSymbols()));
        assertTrue("Doesn't work3",
                f2.format(new Date()).getClass() == String.class);

        f2 = (SimpleDateFormat) DateFormat.getDateInstance(DateFormat.FULL);
        assertTrue("Wrong class4", f2.getClass() == SimpleDateFormat.class);
        assertTrue("Wrong default4", f2.equals(DateFormat.getDateInstance(
                DateFormat.FULL, Locale.getDefault())));
        assertTrue("Wrong symbols4", f2.getDateFormatSymbols().equals(
                new DateFormatSymbols()));
        assertTrue("Doesn't work4",
                f2.format(new Date()).getClass() == String.class);

        // regression test for HARMONY-940
        try {
            DateFormat.getDateInstance(77);
            fail("Should throw IAE");
        } catch (IllegalArgumentException iae) {
            // expected
        }
    }

    /**
     * @tests java.text.DateFormat#getDateInstance(int, java.util.Locale)
     */
    public void test_getDateInstanceILjava_util_Locale() {
        SimpleDateFormat f2 = (SimpleDateFormat) DateFormat.getDateInstance(
                DateFormat.SHORT, Locale.GERMAN);
        assertTrue("Wrong class", f2.getClass() == SimpleDateFormat.class);
        assertTrue("Wrong symbols", f2.getDateFormatSymbols().equals(
                new DateFormatSymbols(Locale.GERMAN)));
        assertTrue("Doesn't work",
                f2.format(new Date()).getClass() == String.class);

        f2 = (SimpleDateFormat) DateFormat.getDateInstance(DateFormat.MEDIUM,
                Locale.GERMAN);
        assertTrue("Wrong class", f2.getClass() == SimpleDateFormat.class);
        assertTrue("Wrong symbols", f2.getDateFormatSymbols().equals(
                new DateFormatSymbols(Locale.GERMAN)));
        assertTrue("Doesn't work",
                f2.format(new Date()).getClass() == String.class);

        f2 = (SimpleDateFormat) DateFormat.getDateInstance(DateFormat.LONG,
                Locale.GERMAN);
        assertTrue("Wrong class", f2.getClass() == SimpleDateFormat.class);
        assertTrue("Wrong symbols", f2.getDateFormatSymbols().equals(
                new DateFormatSymbols(Locale.GERMAN)));
        assertTrue("Doesn't work",
                f2.format(new Date()).getClass() == String.class);

        f2 = (SimpleDateFormat) DateFormat.getDateInstance(DateFormat.FULL,
                Locale.GERMAN);
        assertTrue("Wrong class", f2.getClass() == SimpleDateFormat.class);
        assertTrue("Wrong symbols", f2.getDateFormatSymbols().equals(
                new DateFormatSymbols(Locale.GERMAN)));
        assertTrue("Doesn't work",
                f2.format(new Date()).getClass() == String.class);

        // regression test for HARMONY-940
        try {
            DateFormat.getDateInstance(77, Locale.GERMAN);
            fail("Should throw IAE");
        } catch (IllegalArgumentException iae) {
            // expected
        }
    }

    /**
     * @tests java.text.DateFormat#getDateTimeInstance()
     */
    public void test_getDateTimeInstance() {
        SimpleDateFormat f2 = (SimpleDateFormat) DateFormat
                .getDateTimeInstance();
        assertTrue("Wrong class", f2.getClass() == SimpleDateFormat.class);
        assertTrue("Wrong default", f2.equals(DateFormat.getDateTimeInstance(
                DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.getDefault())));
        assertTrue("Wrong symbols", f2.getDateFormatSymbols().equals(
                new DateFormatSymbols()));
        assertTrue("Doesn't work",
                f2.format(new Date()).getClass() == String.class);
    }

    private void testDateTime(int dStyle, int tStyle) {
        SimpleDateFormat f2 = (SimpleDateFormat) DateFormat
                .getDateTimeInstance(dStyle, tStyle);
        assertTrue("Wrong class", f2.getClass() == SimpleDateFormat.class);
        SimpleDateFormat date = (SimpleDateFormat) DateFormat.getDateInstance(
                dStyle, Locale.getDefault());
        SimpleDateFormat time = (SimpleDateFormat) DateFormat.getTimeInstance(
                tStyle, Locale.getDefault());
        assertTrue("Wrong default", f2.toPattern().equals(
                date.toPattern() + " " + time.toPattern()));
        assertTrue("Wrong symbols", f2.getDateFormatSymbols().equals(
                new DateFormatSymbols()));
        assertTrue("Doesn't work",
                f2.format(new Date()).getClass() == String.class);
    }

    /**
     * @tests java.text.DateFormat#getDateTimeInstance(int, int)
     */
    public void test_getDateTimeInstanceII() {
        testDateTime(DateFormat.SHORT, DateFormat.SHORT);
        testDateTime(DateFormat.SHORT, DateFormat.MEDIUM);
        testDateTime(DateFormat.SHORT, DateFormat.LONG);
        testDateTime(DateFormat.SHORT, DateFormat.FULL);

        testDateTime(DateFormat.MEDIUM, DateFormat.SHORT);
        testDateTime(DateFormat.MEDIUM, DateFormat.MEDIUM);
        testDateTime(DateFormat.MEDIUM, DateFormat.LONG);
        testDateTime(DateFormat.MEDIUM, DateFormat.FULL);

        testDateTime(DateFormat.LONG, DateFormat.SHORT);
        testDateTime(DateFormat.LONG, DateFormat.MEDIUM);
        testDateTime(DateFormat.LONG, DateFormat.LONG);
        testDateTime(DateFormat.LONG, DateFormat.FULL);

        testDateTime(DateFormat.FULL, DateFormat.SHORT);
        testDateTime(DateFormat.FULL, DateFormat.MEDIUM);
        testDateTime(DateFormat.FULL, DateFormat.LONG);
        testDateTime(DateFormat.FULL, DateFormat.FULL);

        // regression test for HARMONY-940
        try {
            DateFormat.getDateTimeInstance(77, 66);
            fail("Should throw IAE");
        } catch (IllegalArgumentException iae) {
            // expected
        }
    }

    private void testDateTimeLocale(int dStyle, int tStyle) {
        SimpleDateFormat f2 = (SimpleDateFormat) DateFormat
                .getDateTimeInstance(dStyle, tStyle, Locale.GERMAN);
        assertTrue("Wrong class", f2.getClass() == SimpleDateFormat.class);
        SimpleDateFormat date = (SimpleDateFormat) DateFormat.getDateInstance(
                dStyle, Locale.GERMAN);
        SimpleDateFormat time = (SimpleDateFormat) DateFormat.getTimeInstance(
                tStyle, Locale.GERMAN);
        assertTrue("Wrong default", f2.toPattern().equals(
                date.toPattern() + " " + time.toPattern()));
        assertTrue("Wrong symbols", f2.getDateFormatSymbols().equals(
                new DateFormatSymbols(Locale.GERMAN)));
        assertTrue("Doesn't work",
                f2.format(new Date()).getClass() == String.class);
    }

    /**
     * @tests java.text.DateFormat#getDateTimeInstance(int, int,
     *        java.util.Locale)
     */
    public void test_getDateTimeInstanceIILjava_util_Locale() {
        testDateTimeLocale(DateFormat.SHORT, DateFormat.SHORT);
        testDateTimeLocale(DateFormat.SHORT, DateFormat.MEDIUM);
        testDateTimeLocale(DateFormat.SHORT, DateFormat.LONG);
        testDateTimeLocale(DateFormat.SHORT, DateFormat.FULL);

        testDateTimeLocale(DateFormat.MEDIUM, DateFormat.SHORT);
        testDateTimeLocale(DateFormat.MEDIUM, DateFormat.MEDIUM);
        testDateTimeLocale(DateFormat.MEDIUM, DateFormat.LONG);
        testDateTimeLocale(DateFormat.MEDIUM, DateFormat.FULL);

        testDateTimeLocale(DateFormat.LONG, DateFormat.SHORT);
        testDateTimeLocale(DateFormat.LONG, DateFormat.MEDIUM);
        testDateTimeLocale(DateFormat.LONG, DateFormat.LONG);
        testDateTimeLocale(DateFormat.LONG, DateFormat.FULL);

        testDateTimeLocale(DateFormat.FULL, DateFormat.SHORT);
        testDateTimeLocale(DateFormat.FULL, DateFormat.MEDIUM);
        testDateTimeLocale(DateFormat.FULL, DateFormat.LONG);
        testDateTimeLocale(DateFormat.FULL, DateFormat.FULL);

        // regression test for HARMONY-940
        try {
            DateFormat.getDateTimeInstance(77, 66, Locale.GERMAN);
            fail("Should throw IAE");
        } catch (IllegalArgumentException iae) {
            // expected
        }
    }

    /**
     * @tests java.text.DateFormat#getInstance()
     */
    public void test_getInstance() {
        SimpleDateFormat f2 = (SimpleDateFormat) DateFormat.getInstance();
        assertTrue("Wrong class", f2.getClass() == SimpleDateFormat.class);
        assertTrue("Wrong default", f2.equals(DateFormat.getDateTimeInstance(
                DateFormat.SHORT, DateFormat.SHORT, Locale.getDefault())));
        assertTrue("Wrong symbols", f2.getDateFormatSymbols().equals(
                new DateFormatSymbols()));
        assertTrue("Doesn't work",
                f2.format(new Date()).getClass() == String.class);
    }

    /**
     * @tests java.text.DateFormat#getNumberFormat()
     */
    public void test_getNumberFormat() {
        DateFormat format = DateFormat.getInstance();
        NumberFormat nf1 = format.getNumberFormat();
        NumberFormat nf2 = format.getNumberFormat();
        assertTrue("NumberFormats not identical", nf1 == nf2);
    }

    /**
     * @tests java.text.DateFormat#getTimeInstance()
     */
    public void test_getTimeInstance() {
        SimpleDateFormat f2 = (SimpleDateFormat) DateFormat.getTimeInstance();
        assertTrue("Wrong class", f2.getClass() == SimpleDateFormat.class);
        assertTrue("Wrong default", f2.equals(DateFormat.getTimeInstance(
                DateFormat.DEFAULT, Locale.getDefault())));
        assertTrue("Wrong symbols", f2.getDateFormatSymbols().equals(
                new DateFormatSymbols()));
        assertTrue("Doesn't work",
                f2.format(new Date()).getClass() == String.class);
    }

    /**
     * @tests java.text.DateFormat#getTimeInstance(int)
     */
    public void test_getTimeInstanceI() {
        SimpleDateFormat f2 = (SimpleDateFormat) DateFormat
                .getTimeInstance(DateFormat.SHORT);
        assertTrue("Wrong class1", f2.getClass() == SimpleDateFormat.class);
        assertTrue("Wrong default1", f2.equals(DateFormat.getTimeInstance(
                DateFormat.SHORT, Locale.getDefault())));
        assertTrue("Wrong symbols1", f2.getDateFormatSymbols().equals(
                new DateFormatSymbols()));
        assertTrue("Doesn't work1",
                f2.format(new Date()).getClass() == String.class);

        f2 = (SimpleDateFormat) DateFormat.getTimeInstance(DateFormat.MEDIUM);
        assertTrue("Wrong class2", f2.getClass() == SimpleDateFormat.class);
        assertTrue("Wrong default2", f2.equals(DateFormat.getTimeInstance(
                DateFormat.MEDIUM, Locale.getDefault())));
        assertTrue("Wrong symbols2", f2.getDateFormatSymbols().equals(
                new DateFormatSymbols()));
        assertTrue("Doesn't work2",
                f2.format(new Date()).getClass() == String.class);

        f2 = (SimpleDateFormat) DateFormat.getTimeInstance(DateFormat.LONG);
        assertTrue("Wrong class3", f2.getClass() == SimpleDateFormat.class);
        assertTrue("Wrong default3", f2.equals(DateFormat.getTimeInstance(
                DateFormat.LONG, Locale.getDefault())));
        assertTrue("Wrong symbols3", f2.getDateFormatSymbols().equals(
                new DateFormatSymbols()));
        assertTrue("Doesn't work3",
                f2.format(new Date()).getClass() == String.class);

        f2 = (SimpleDateFormat) DateFormat.getTimeInstance(DateFormat.FULL);
        assertTrue("Wrong class4", f2.getClass() == SimpleDateFormat.class);
        assertTrue("Wrong default4", f2.equals(DateFormat.getTimeInstance(
                DateFormat.FULL, Locale.getDefault())));
        assertTrue("Wrong symbols4", f2.getDateFormatSymbols().equals(
                new DateFormatSymbols()));
        assertTrue("Doesn't work4",
                f2.format(new Date()).getClass() == String.class);

        // regression test for HARMONY-940
        try {
            DateFormat.getTimeInstance(77);
            fail("Should throw IAE");
        } catch (IllegalArgumentException iae) {
            // expected
        }
    }

    /**
     * @tests java.text.DateFormat#getTimeInstance(int, java.util.Locale)
     */
    public void test_getTimeInstanceILjava_util_Locale() {
        SimpleDateFormat f2 = (SimpleDateFormat) DateFormat.getTimeInstance(
                DateFormat.SHORT, Locale.GERMAN);
        assertTrue("Wrong class", f2.getClass() == SimpleDateFormat.class);
        assertTrue("Wrong symbols", f2.getDateFormatSymbols().equals(
                new DateFormatSymbols(Locale.GERMAN)));
        assertTrue("Doesn't work",
                f2.format(new Date()).getClass() == String.class);

        f2 = (SimpleDateFormat) DateFormat.getTimeInstance(DateFormat.MEDIUM,
                Locale.GERMAN);
        assertTrue("Wrong class", f2.getClass() == SimpleDateFormat.class);
        assertTrue("Wrong symbols", f2.getDateFormatSymbols().equals(
                new DateFormatSymbols(Locale.GERMAN)));
        assertTrue("Doesn't work",
                f2.format(new Date()).getClass() == String.class);

        f2 = (SimpleDateFormat) DateFormat.getTimeInstance(DateFormat.LONG,
                Locale.GERMAN);
        assertTrue("Wrong class", f2.getClass() == SimpleDateFormat.class);
        assertTrue("Wrong symbols", f2.getDateFormatSymbols().equals(
                new DateFormatSymbols(Locale.GERMAN)));
        assertTrue("Doesn't work",
                f2.format(new Date()).getClass() == String.class);

        f2 = (SimpleDateFormat) DateFormat.getTimeInstance(DateFormat.FULL,
                Locale.GERMAN);
        assertTrue("Wrong class", f2.getClass() == SimpleDateFormat.class);
        assertTrue("Wrong symbols", f2.getDateFormatSymbols().equals(
                new DateFormatSymbols(Locale.GERMAN)));
        assertTrue("Doesn't work",
                f2.format(new Date()).getClass() == String.class);

        try {
            DateFormat.getTimeInstance(77, Locale.GERMAN);
            fail("Should throw IAE");
        } catch (IllegalArgumentException iae) {
            // expected
        }
    }

    /**
     * @tests java.text.DateFormat#getTimeZone() Test of method
     *        java.text.DateFormat#getTimeZone().
     */
    public void test_getTimeZone() {
        try {
            DateFormat format = DateFormat.getInstance();
            TimeZone   tz     = format.getTimeZone();
            //if(1 == 1)
            //    throw new Exception(tz.getClass().getName());
            // We know we are not sun.util so:
            assertFalse("Incorrect zone info", tz.getClass().getName().equals(
                    "sun.util.calendar.ZoneInfo"));
            assertTrue("Incorrect time zone", tz.equals(format.getCalendar()
                    .getTimeZone()));
        } catch (Exception e) {
            fail("Unexpected exception " + e.toString());
        }
    }

    /**
     * @tests java.text.DateFormat#hashCode() Test of method
     *        java.text.DateFormat#hashCode().
     */
    public void test_hashCode() {
        try {
            DateFormat df1 = DateFormat.getInstance();
            DateFormat df2 = (DateFormat) df1.clone();
            assertTrue("Hash codes of clones are not equal",
                    df1.hashCode() == df2.hashCode());
            assertTrue("Hash codes of different objects are the same", df1
                    .hashCode() != DateFormat.getDateInstance().hashCode());
        } catch (Exception e) {
            fail("Unexpected exception " + e.toString());
        }
    }

    /**
     * @tests java.text.DateFormat#isLenient() Test of method
     *        java.text.DateFormat#isLenient().
     */
    public void test_isLenient() {
        DateFormat df = DateFormat.getInstance();
        Calendar c = df.getCalendar();
        if (df.isLenient()) {
            try {
                c.set(Calendar.DAY_OF_MONTH, 32);
                c.get(Calendar.DAY_OF_MONTH);
            } catch (Exception e) {
                fail("Unexpected exception " + e.toString());
            }
            c.setLenient(false);
            try {
                c.set(Calendar.DAY_OF_MONTH, 32);
                c.get(Calendar.DAY_OF_MONTH);
                fail("Expected IllegalArgumentException was not thrown");
            } catch (IllegalArgumentException e) {
                // expected
            } catch (Exception e) {
                fail("Unexpected exception " + e.toString());
            }
        } else {
            try {
                c.set(Calendar.DAY_OF_MONTH, 32);
                c.get(Calendar.DAY_OF_MONTH);
                fail("Expected IllegalArgumentException was not thrown");
            } catch (IllegalArgumentException e) {
                // expected
            } catch (Exception e) {
                fail("Unexpected exception " + e.toString());
            }
            c.setLenient(true);
            try {
                c.set(Calendar.DAY_OF_MONTH, 32);
                c.get(Calendar.DAY_OF_MONTH);
            } catch (Exception e) {
                fail("Unexpected exception " + e.toString());
            }
        }
    }

    /**
     * @tests java.text.DateFormat#setCalendar(java.util.Calendar)
     */
    public void test_setCalendarLjava_util_Calendar() {
        DateFormat format = DateFormat.getInstance();
        Calendar cal = Calendar.getInstance();
        format.setCalendar(cal);
        assertTrue("Not identical Calendar", cal == format.getCalendar());
    }

    /**
     * @tests java.text.DateFormat#setNumberFormat(java.text.NumberFormat)
     */
    public void test_setNumberFormatLjava_text_NumberFormat() {
        DateFormat format = DateFormat.getInstance();
        NumberFormat f1 = NumberFormat.getInstance();
        format.setNumberFormat(f1);
        assertTrue("Not identical NumberFormat", f1 == format.getNumberFormat());
    }

    /**
     * @tests java.text.DateFormat#parse(String)
     */
    public void test_parse_LString() {
        DateFormat format = DateFormat.getInstance();
        try {
            format.parse("not a Date");
            fail("should throw ParseException first");
        } catch (ParseException e) {
            assertNotNull(e.getMessage());
        }
    }

    /**
     * @tests java.text.DateFormat#parseObject(String, ParsePosition) Test of
     *        method java.text.DateFormat#parseObject(String, ParsePosition).
     *        Case 1: Try to parse correct data string. Case 2: Try to parse
     *        partialy correct data string. Case 3: Try to use argument
     *        ParsePosition as null.
     */
    public void test_parseObjectLjava_lang_StringLjava_text_ParsePosition() {
        DateFormat df = DateFormat.getInstance();
        try {
            // case 1: Try to parse correct data string.
            Date current = new Date();
            ParsePosition pp = new ParsePosition(0);
            int parseIndex = pp.getIndex();
            Date result = (Date) df.parseObject(df.format(current), pp);
            assertTrue("Parse operation return null", result != null);
            assertTrue("ParseIndex is incorrect", pp.getIndex() != parseIndex);

            // case 2: Try to parse partially correct data string.
            pp.setIndex(0);
            char[] cur = df.format(current).toCharArray();
            cur[cur.length / 2] = 'Z';
            String partialCorrect = new String(cur);
            result = (Date) df.parseObject(partialCorrect, pp);
            assertTrue("Parse operation return not-null", result == null);
            assertTrue("ParseIndex is incorrect", pp.getIndex() == 0);
            assertTrue("ParseErrorIndex is incorrect",
                    pp.getErrorIndex() == cur.length / 2);

            // case 3: Try to use argument ParsePosition as null.
            try {
                df.parseObject(df.format(current), null);
                fail("Expected NullPointerException was not thrown");
            } catch (NullPointerException e) {
                // expected
            }
        } catch (Exception e) {
            fail("Unexpected exception " + e.toString());
        }
    }

    /**
     * @tests java.text.DateFormat#setLenient(boolean) Test of method
     *        java.text.DateFormat#setLenient(boolean).
     */
    public void test_setLenientZ() {
        DateFormat df = DateFormat.getInstance();
        Calendar c = df.getCalendar();
        try {
            c.setLenient(true);
            try {
                c.set(Calendar.DAY_OF_MONTH, 32);
                c.get(Calendar.DAY_OF_MONTH);
            } catch (Exception e) {
                fail("Unexpected exception " + e.toString());
            }
            c.setLenient(false);
            try {
                c.set(Calendar.DAY_OF_MONTH, 32);
                c.get(Calendar.DAY_OF_MONTH);
                fail("Expected IllegalArgumentException was not thrown");
            } catch (IllegalArgumentException e) {
                // expected
            } catch (Exception e) {
                fail("Unexpected exception " + e.toString());
            }
        } catch (Exception e) {
            fail("Uexpected exception " + e.toString());
        }
    }

    /**
     * @tests java.text.DateFormat#setTimeZone(TimeZone) Test of method
     *        java.text.DateFormat#setTimeZone(TimeZone).
     */
    public void test_setTimeZoneLjava_util_TimeZone() {
        try {
            DateFormat format = DateFormat.getInstance();
            TimeZone tz = TimeZone.getTimeZone("America/Los_Angeles");
            format.setTimeZone(tz);
            assertTrue("TimeZone is set incorrectly", tz.equals(format
                    .getTimeZone()));
        } catch (Exception e) {
            fail("Unexpected exception " + e.toString());
        }
    }
}
