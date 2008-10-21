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

package java.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;

import org.apache.harmony.luni.internal.nls.Messages;

/**
 * Date represents a specific moment in time, to the millisecond.
 * 
 * @see System#currentTimeMillis
 * @see Calendar
 * @see GregorianCalendar
 * @see SimpleTimeZone
 * @see TimeZone
 */
public class Date implements Serializable, Cloneable, Comparable<Date> {

    private static final long serialVersionUID = 7523967970034938905L;

    // Used by parse()
    private static int creationYear = new Date().getYear();

    private transient long milliseconds;

    /**
     * Initializes this Date instance to the current date and time.
     * 
     */
    public Date() {
        this(System.currentTimeMillis());
    }

    /**
     * Constructs a new Date initialized to midnight in the default TimeZone on
     * the specified date.
     * 
     * @param year
     *            the year, 0 is 1900
     * @param month
     *            the month, 0 - 11
     * @param day
     *            the day of the month, 1 - 31
     * 
     * @deprecated use {@link GregorianCalendar#GregorianCalendar(int, int, int)}
     */
    @Deprecated
    public Date(int year, int month, int day) {
        GregorianCalendar cal = new GregorianCalendar(false);
        cal.set(1900 + year, month, day);
        milliseconds = cal.getTimeInMillis();
    }

    /**
     * Constructs a new Date initialized to the specified date and time in the
     * default TimeZone.
     * 
     * @param year
     *            the year, 0 is 1900
     * @param month
     *            the month, 0 - 11
     * @param day
     *            the day of the month, 1 - 31
     * @param hour
     *            the hour of day, 0 - 23
     * @param minute
     *            the minute of the hour, 0 - 59
     * 
     * @deprecated use {@link GregorianCalendar#GregorianCalendar(int, int, int, int, int)}
     */
    @Deprecated
    public Date(int year, int month, int day, int hour, int minute) {
        GregorianCalendar cal = new GregorianCalendar(false);
        cal.set(1900 + year, month, day, hour, minute);
        milliseconds = cal.getTimeInMillis();
    }

    /**
     * Constructs a new Date initialized to the specified date and time in the
     * default TimeZone.
     * 
     * @param year
     *            the year, 0 is 1900
     * @param month
     *            the month, 0 - 11
     * @param day
     *            the day of the month, 1 - 31
     * @param hour
     *            the hour of day, 0 - 23
     * @param minute
     *            the minute of the hour, 0 - 59
     * @param second
     *            the second of the minute, 0 - 59
     * 
     * @deprecated use {@link GregorianCalendar#GregorianCalendar(int, int, int, int, int, int)}
     */
    @Deprecated
    public Date(int year, int month, int day, int hour, int minute, int second) {
        GregorianCalendar cal = new GregorianCalendar(false);
        cal.set(1900 + year, month, day, hour, minute, second);
        milliseconds = cal.getTimeInMillis();
    }

    /**
     * Initializes this Date instance using the specified millisecond value. The
     * value is the number of milliseconds since Jan. 1, 1970 GMT.
     * 
     * @param milliseconds
     *            the number of milliseconds since Jan. 1, 1970 GMT
     */
    public Date(long milliseconds) {
        this.setTime(milliseconds);
    }

    /**
     * Constructs a new Date initialized to the date and time parsed from the
     * specified String.
     * 
     * @param string
     *            the String to parse
     * 
     * @deprecated use {@link DateFormat}
     */
    @Deprecated
    public Date(String string) {
        milliseconds = parse(string);
    }

    /**
     * Returns if this Date is after the specified Date.
     * 
     * @param date
     *            a Date instance to compare
     * @return true if this Date is after the specified Date, false otherwise
     */
    public boolean after(Date date) {
        return milliseconds > date.milliseconds;
    }

    /**
     * Boolean indication of whether or not this <code>Date</code> occurs
     * earlier than the <code>Date</code> argument.
     * 
     * @param date
     *            a Date instance to compare
     * @return <code>true</code> if this <code>Date</code> occurs earlier
     *         than <code>date</code>, otherwise <code>false</code>
     */
    public boolean before(Date date) {
        return milliseconds < date.milliseconds;
    }

    /**
     * Returns a new Date with the same millisecond value as this Date.
     * 
     * @return a shallow copy of this Date
     * 
     * @see java.lang.Cloneable
     */
    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    /**
     * Compare the receiver to the specified Date to determine the relative
     * ordering.
     * 
     * @param date
     *            a Date
     * @return an int < 0 if this Date is less than the specified Date, 0 if
     *         they are equal, and > 0 if this Date is greater
     */
    public int compareTo(Date date) {
        if (milliseconds < date.milliseconds) {
            return -1;
        }
        if (milliseconds == date.milliseconds) {
            return 0;
        }
        return 1;
    }

    /**
     * Compares the specified object to this Date and answer if they are equal.
     * The object must be an instance of Date and have the same millisecond
     * value.
     * 
     * @param object
     *            the object to compare with this object
     * @return true if the specified object is equal to this Date, false
     *         otherwise
     * 
     * @see #hashCode
     */
    @Override
    public boolean equals(Object object) {
        return (object == this) || (object instanceof Date)
                && (milliseconds == ((Date) object).milliseconds);
    }

    /**
     * Returns the gregorian calendar day of the month for this Date object.
     * 
     * @return the day of the month
     * 
     * @deprecated use Calendar.get(Calendar.DATE)
     */
    @Deprecated
    public int getDate() {
        return new GregorianCalendar(milliseconds).get(Calendar.DATE);
    }

    /**
     * Returns the gregorian calendar day of the week for this Date object.
     * 
     * @return the day of the week
     * 
     * @deprecated use Calendar.get(Calendar.DAY_OF_WEEK)
     */
    @Deprecated
    public int getDay() {
        return new GregorianCalendar(milliseconds).get(Calendar.DAY_OF_WEEK) - 1;
    }

    /**
     * Returns the gregorian calendar hour of the day for this Date object.
     * 
     * @return the hour of the day
     * 
     * @deprecated use Calendar.get(Calendar.HOUR_OF_DAY)
     */
    @Deprecated
    public int getHours() {
        return new GregorianCalendar(milliseconds).get(Calendar.HOUR_OF_DAY);
    }

    /**
     * Returns the gregorian calendar minute of the hour for this Date object.
     * 
     * @return the minutes
     * 
     * @deprecated use Calendar.get(Calendar.MINUTE)
     */
    @Deprecated
    public int getMinutes() {
        return new GregorianCalendar(milliseconds).get(Calendar.MINUTE);
    }

    /**
     * Returns the gregorian calendar month for this Date object.
     * 
     * @return the month
     * 
     * @deprecated use Calendar.get(Calendar.MONTH)
     */
    @Deprecated
    public int getMonth() {
        return new GregorianCalendar(milliseconds).get(Calendar.MONTH);
    }

    /**
     * Returns the gregorian calendar second of the minute for this Date object.
     * 
     * @return the seconds
     * 
     * @deprecated use Calendar.get(Calendar.SECOND)
     */
    @Deprecated
    public int getSeconds() {
        return new GregorianCalendar(milliseconds).get(Calendar.SECOND);
    }

    /**
     * Returns this Date as a millisecond value. The value is the number of
     * milliseconds since Jan. 1, 1970 GMT.
     * 
     * @return the number of milliseconds since Jan. 1, 1970 GMT.
     */
    public long getTime() {
        return milliseconds;
    }

    /**
     * Returns the timezone offset in minutes of the default TimeZone.
     * 
     * @return the timezone offset in minutes of the default TimeZone
     * 
     * @deprecated use
     *             <code>(Calendar.get(Calendar.ZONE_OFFSET) + Calendar.get(Calendar.DST_OFFSET)) / 60000</code>
     */
    @Deprecated
    public int getTimezoneOffset() {
        GregorianCalendar cal = new GregorianCalendar(milliseconds);
        return -(cal.get(Calendar.ZONE_OFFSET) + cal.get(Calendar.DST_OFFSET)) / 60000;
    }

    /**
     * Returns the gregorian calendar year since 1900 for this Date object.
     * 
     * @return the year - 1900
     * 
     * @deprecated use <code>Calendar.get(Calendar.YEAR) - 1900</code>
     */
    @Deprecated
    public int getYear() {
        return new GregorianCalendar(milliseconds).get(Calendar.YEAR) - 1900;
    }

    /**
     * Returns an integer hash code for the receiver. Objects which are equal
     * answer the same value for this method.
     * 
     * @return the receiver's hash
     * 
     * @see #equals
     */
    @Override
    public int hashCode() {
        return (int) (milliseconds >>> 32) ^ (int) milliseconds;
    }

    private static int parse(String string, String[] array) {
        for (int i = 0, alength = array.length, slength = string.length(); i < alength; i++) {
            if (string.regionMatches(true, 0, array[i], 0, slength)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Returns the millisecond value of the date and time parsed from the
     * specified String. Many date/time formats are recognized, including IETF
     * standard syntax, i.e. Tue, 22 Jun 1999 12:16:00 GMT-0500
     * 
     * @param string
     *            the String to parse
     * @return the millisecond value parsed from the String
     * 
     * @deprecated use {@link DateFormat}
     */
    @Deprecated
    public static long parse(String string) {

        if (string == null) {
            // luni.06=The string argument is null
            throw new IllegalArgumentException(Messages.getString("luni.06")); //$NON-NLS-1$
        }

        char sign = 0;
        int commentLevel = 0;
        int offset = 0, length = string.length(), state = 0;
        int year = -1, month = -1, date = -1;
        int hour = -1, minute = -1, second = -1, zoneOffset = 0;
        boolean zone = false;
        final int PAD = 0, LETTERS = 1, NUMBERS = 2;
        StringBuffer buffer = new StringBuffer();

        while (offset <= length) {
            char next = offset < length ? string.charAt(offset) : '\r';
            offset++;

            if (next == '(') {
                commentLevel++;
            }
            if (commentLevel > 0) {
                if (next == ')') {
                    commentLevel--;
                }
                if (commentLevel == 0) {
                    next = ' ';
                } else {
                    continue;
                }
            }

            int nextState = PAD;
            if ('a' <= next && next <= 'z' || 'A' <= next && next <= 'Z') {
                nextState = LETTERS;
            } else if ('0' <= next && next <= '9') {
                nextState = NUMBERS;
            } else if (!Character.isSpace(next) && ",+-:/".indexOf(next) == -1) { //$NON-NLS-1$
                throw new IllegalArgumentException();
            }

            if (state == NUMBERS && nextState != NUMBERS) {
                int digit = Integer.parseInt(buffer.toString());
                buffer.setLength(0);
                if (sign == '+' || sign == '-') {
                    if (zoneOffset == 0) {
                        zone = true;
                        zoneOffset = sign == '-' ? -digit : digit;
                        sign = 0;
                    } else {
                        throw new IllegalArgumentException();
                    }
                } else if (digit >= 70) {
                    if (year == -1
                            && (Character.isSpace(next) || next == ','
                                    || next == '/' || next == '\r')) {
                        year = digit;
                    } else {
                        throw new IllegalArgumentException();
                    }
                } else if (next == ':') {
                    if (hour == -1) {
                        hour = digit;
                    } else if (minute == -1) {
                        minute = digit;
                    } else {
                        throw new IllegalArgumentException();
                    }
                } else if (next == '/') {
                    if (month == -1) {
                        month = digit - 1;
                    } else if (date == -1) {
                        date = digit;
                    } else {
                        throw new IllegalArgumentException();
                    }
                } else if (Character.isSpace(next) || next == ','
                        || next == '-' || next == '\r') {
                    if (hour != -1 && minute == -1) {
                        minute = digit;
                    } else if (minute != -1 && second == -1) {
                        second = digit;
                    } else if (date == -1) {
                        date = digit;
                    } else if (year == -1) {
                        year = digit;
                    } else {
                        throw new IllegalArgumentException();
                    }
                } else if (year == -1 && month != -1 && date != -1) {
                    year = digit;
                } else {
                    throw new IllegalArgumentException();
                }
            } else if (state == LETTERS && nextState != LETTERS) {
                String text = buffer.toString().toUpperCase();
                buffer.setLength(0);
                if (text.length() == 1) {
                    throw new IllegalArgumentException();
                }
                if (text.equals("AM")) { //$NON-NLS-1$
                    if (hour == 12) {
                        hour = 0;
                    } else if (hour < 1 || hour > 12) {
                        throw new IllegalArgumentException();
                    }
                } else if (text.equals("PM")) { //$NON-NLS-1$
                    if (hour == 12) {
                        hour = 0;
                    } else if (hour < 1 || hour > 12) {
                        throw new IllegalArgumentException();
                    }
                    hour += 12;
                } else {
                    DateFormatSymbols symbols = new DateFormatSymbols(Locale.US);
                    String[] weekdays = symbols.getWeekdays(), months = symbols
                            .getMonths();
                    int value;
                    if (parse(text, weekdays) != -1) {/* empty */
                    } else if (month == -1
                            && (month = parse(text, months)) != -1) {/* empty */
                    } else if (text.equals("GMT") || text.equals("UT") //$NON-NLS-1$ //$NON-NLS-2$
                            || text.equals("UTC")) { //$NON-NLS-1$
                        zone = true;
                        zoneOffset = 0;
                    } else if ((value = zone(text)) != 0) {
                        zone = true;
                        zoneOffset = value;
                    } else {
                        throw new IllegalArgumentException();
                    }
                }
            }

            if (next == '+' || (year != -1 && next == '-')) {
                sign = next;
            } else if (!Character.isSpace(next) && next != ','
                    && nextState != NUMBERS) {
                sign = 0;
            }

            if (nextState == LETTERS || nextState == NUMBERS) {
                buffer.append(next);
            }
            state = nextState;
        }

        if (year != -1 && month != -1 && date != -1) {
            if (hour == -1) {
                hour = 0;
            }
            if (minute == -1) {
                minute = 0;
            }
            if (second == -1) {
                second = 0;
            }
            if (year < (creationYear - 80)) {
                year += 2000;
            } else if (year < 100) {
                year += 1900;
            }
            if (zone) {
                if (zoneOffset >= 24 || zoneOffset <= -24) {
                    hour -= zoneOffset / 100;
                    minute -= zoneOffset % 100;
                } else {
                    hour -= zoneOffset;
                }
                return UTC(year - 1900, month, date, hour, minute, second);
            }
            return new Date(year - 1900, month, date, hour, minute, second)
                    .getTime();
        }
        throw new IllegalArgumentException();
    }

    /**
     * Sets the gregorian calendar day of the month for this Date object.
     * 
     * @param day
     *            the day of the month
     * 
     * @deprecated use Calendar.set(Calendar.DATE, day)
     */
    @Deprecated
    public void setDate(int day) {
        GregorianCalendar cal = new GregorianCalendar(milliseconds);
        cal.set(Calendar.DATE, day);
        milliseconds = cal.getTimeInMillis();
    }

    /**
     * Sets the gregorian calendar hour of the day for this Date object.
     * 
     * @param hour
     *            the hour of the day
     * 
     * @deprecated use Calendar.set(Calendar.HOUR_OF_DAY, hour)
     */
    @Deprecated
    public void setHours(int hour) {
        GregorianCalendar cal = new GregorianCalendar(milliseconds);
        cal.set(Calendar.HOUR_OF_DAY, hour);
        milliseconds = cal.getTimeInMillis();
    }

    /**
     * Sets the gregorian calendar minute of the hour for this Date object.
     * 
     * @param minute
     *            the minutes
     * 
     * @deprecated use Calendar.set(Calendar.MINUTE, minute)
     */
    @Deprecated
    public void setMinutes(int minute) {
        GregorianCalendar cal = new GregorianCalendar(milliseconds);
        cal.set(Calendar.MINUTE, minute);
        milliseconds = cal.getTimeInMillis();
    }

    /**
     * Sets the gregorian calendar month for this Date object.
     * 
     * @param month
     *            the month
     * 
     * @deprecated use Calendar.set(Calendar.MONTH, month)
     */
    @Deprecated
    public void setMonth(int month) {
        GregorianCalendar cal = new GregorianCalendar(milliseconds);
        cal.set(Calendar.MONTH, month);
        milliseconds = cal.getTimeInMillis();
    }

    /**
     * Sets the gregorian calendar second of the minute for this Date object.
     * 
     * @param second
     *            the seconds
     * 
     * @deprecated use Calendar.set(Calendar.SECOND, second)
     */
    @Deprecated
    public void setSeconds(int second) {
        GregorianCalendar cal = new GregorianCalendar(milliseconds);
        cal.set(Calendar.SECOND, second);
        milliseconds = cal.getTimeInMillis();
    }

    /**
     * Sets this Date to the specified millisecond value. The value is the
     * number of milliseconds since Jan. 1, 1970 GMT.
     * 
     * @param milliseconds
     *            the number of milliseconds since Jan. 1, 1970 GMT.
     */
    public void setTime(long milliseconds) {
        this.milliseconds = milliseconds;
    }

    /**
     * Sets the gregorian calendar year since 1900 for this Date object.
     * 
     * @param year
     *            the year since 1900
     * 
     * @deprecated use Calendar.set(Calendar.YEAR, year + 1900)
     */
    @Deprecated
    public void setYear(int year) {
        GregorianCalendar cal = new GregorianCalendar(milliseconds);
        cal.set(Calendar.YEAR, year + 1900);
        milliseconds = cal.getTimeInMillis();
    }

    /**
     * Returns the string representation of this Date in GMT in the format: 22
     * Jun 1999 13:02:00 GMT
     * 
     * @return the string representation of this Date in GMT
     * 
     * @deprecated use {@link DateFormat}
     */
    @Deprecated
    public String toGMTString() {
        SimpleDateFormat format1 = new SimpleDateFormat("d MMM ", Locale.US); //$NON-NLS-1$
        SimpleDateFormat format2 = new SimpleDateFormat(
                " HH:mm:ss 'GMT'", Locale.US); //$NON-NLS-1$
        TimeZone gmtZone = TimeZone.getTimeZone("GMT"); //$NON-NLS-1$
        format1.setTimeZone(gmtZone);
        format2.setTimeZone(gmtZone);
        GregorianCalendar gc = new GregorianCalendar(gmtZone);
        gc.setTimeInMillis(milliseconds);
        return format1.format(this) + gc.get(Calendar.YEAR)
                + format2.format(this);
    }

    /**
     * Returns the string representation of this Date for the current Locale.
     * 
     * @return the string representation of this Date for the current Locale
     * 
     * @deprecated use {@link DateFormat}
     */
    @Deprecated
    public String toLocaleString() {
        return DateFormat.getDateTimeInstance().format(this);
    }

    /**
     * Returns the string representation of this Date in the format: Tue Jun 22
     * 13:07:00 GMT 1999
     * 
     * @return the string representation of this Date
     */
    @Override
    public String toString() {
        return new SimpleDateFormat("E MMM dd HH:mm:ss z ", Locale.US) //$NON-NLS-1$
                .format(this)
                + new GregorianCalendar(milliseconds).get(Calendar.YEAR);
    }

    /**
     * Returns the millisecond value of the specified date and time in GMT.
     * 
     * @param year
     *            the year, 0 is 1900
     * @param month
     *            the month, 0 - 11
     * @param day
     *            the day of the month, 1 - 31
     * @param hour
     *            the hour of day, 0 - 23
     * @param minute
     *            the minute of the hour, 0 - 59
     * @param second
     *            the second of the minute, 0 - 59
     * @return long
     * 
     * @deprecated use: <code>
     *  Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
     *  cal.set(year + 1900, month, day, hour, minute, second);
     *  cal.getTime().getTime();</code>
     */
    @Deprecated
    public static long UTC(int year, int month, int day, int hour, int minute,
            int second) {
        GregorianCalendar cal = new GregorianCalendar(false);
        cal.setTimeZone(TimeZone.getTimeZone("GMT")); //$NON-NLS-1$
        cal.set(1900 + year, month, day, hour, minute, second);
        return cal.getTimeInMillis();
    }

    private static int zone(String text) {
        if (text.equals("EST")) { //$NON-NLS-1$
            return -5;
        }
        if (text.equals("EDT")) { //$NON-NLS-1$
            return -4;
        }
        if (text.equals("CST")) { //$NON-NLS-1$
            return -6;
        }
        if (text.equals("CDT")) { //$NON-NLS-1$
            return -5;
        }
        if (text.equals("MST")) { //$NON-NLS-1$
            return -7;
        }
        if (text.equals("MDT")) { //$NON-NLS-1$
            return -6;
        }
        if (text.equals("PST")) { //$NON-NLS-1$
            return -8;
        }
        if (text.equals("PDT")) { //$NON-NLS-1$
            return -7;
        }
        return 0;
    }

    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.defaultWriteObject();
        stream.writeLong(getTime());
    }

    private void readObject(ObjectInputStream stream) throws IOException,
            ClassNotFoundException {
        stream.defaultReadObject();
        setTime(stream.readLong());
    }
}
