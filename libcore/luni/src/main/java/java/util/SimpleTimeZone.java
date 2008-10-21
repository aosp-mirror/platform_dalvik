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
import java.io.ObjectStreamField;

import org.apache.harmony.luni.util.Msg;

/**
 * SimpleTimeZone represents a local time zone and its daylight savings time
 * rules for the gregorian calendar.
 * 
 * @see Calendar
 * @see TimeZone
 */
public class SimpleTimeZone extends TimeZone {
    
    private static final long serialVersionUID = -403250971215465050L;

    private int rawOffset;

    private int startYear, startMonth, startDay, startDayOfWeek, startTime;

    private int endMonth, endDay, endDayOfWeek, endTime;

    private int startMode, endMode;

    private static final int DOM_MODE = 1, DOW_IN_MONTH_MODE = 2,
            DOW_GE_DOM_MODE = 3, DOW_LE_DOM_MODE = 4;

    /* Constant for representing start or end time in GMT time mode. */
    public static final int UTC_TIME = 2;

    /*
     * Constant for representing start or end time in standard local time mode,
     * based on timezone's raw offset from GMT, does not include Daylight
     * savings.
     */
    public static final int STANDARD_TIME = 1;

    /*
     * Constant for representing start or end time in local wall clock time
     * mode, based on timezone's adjusted offset from GMT, it does include
     * Daylight savings.
     */
    public static final int WALL_TIME = 0;

    private boolean useDaylight;

    private GregorianCalendar daylightSavings;

    private int dstSavings = 3600000;

    /**
     * Constructs a new SimpleTimeZone using the specified offset for standard
     * time from GMT and the specified time zone ID.
     * 
     * @param offset
     *            the offset from GMT of standard time in milliseconds
     * @param name
     *            the time zone ID
     */
    public SimpleTimeZone(int offset, String name) {
        setID(name);
        rawOffset = offset;
    }

    /**
     * Constructs a new SimpleTimeZone using the specified offset for standard
     * time from GMT, the specified time zone ID and the rules for daylight
     * savings time.
     * 
     * @param offset
     *            the offset from GMT of standard time in milliseconds
     * @param name
     *            the time zone ID
     * @param startMonth
     *            the Calendar month in which daylight savings time starts
     * @param startDay
     *            the occurrence of the day of the week on which daylight
     *            savings time starts
     * @param startDayOfWeek
     *            the Calendar day of the week on which daylight savings time
     *            starts
     * @param startTime
     *            the time of day in milliseconds on which daylight savings time
     *            starts
     * @param endMonth
     *            the Calendar month in which daylight savings time ends
     * @param endDay
     *            the occurrence of the day of the week on which daylight
     *            savings time ends
     * @param endDayOfWeek
     *            the Calendar day of the week on which daylight savings time
     *            ends
     * @param endTime
     *            the time of day in milliseconds standard time on which
     *            daylight savings time ends
     */
    public SimpleTimeZone(int offset, String name, int startMonth,
            int startDay, int startDayOfWeek, int startTime, int endMonth,
            int endDay, int endDayOfWeek, int endTime) {
        this(offset, name, startMonth, startDay, startDayOfWeek, startTime,
                endMonth, endDay, endDayOfWeek, endTime, 3600000);
    }

    /**
     * Constructs a new SimpleTimeZone using the specified offset for standard
     * time from GMT, the specified time zone ID and the rules for daylight
     * savings time.
     * 
     * @param offset
     *            the offset from GMT of standard time in milliseconds
     * @param name
     *            the time zone ID
     * @param startMonth
     *            the Calendar month in which daylight savings time starts
     * @param startDay
     *            the occurrence of the day of the week on which daylight
     *            savings time starts
     * @param startDayOfWeek
     *            the Calendar day of the week on which daylight savings time
     *            starts
     * @param startTime
     *            the time of day in milliseconds on which daylight savings time
     *            starts
     * @param endMonth
     *            the Calendar month in which daylight savings time ends
     * @param endDay
     *            the occurrence of the day of the week on which daylight
     *            savings time ends
     * @param endDayOfWeek
     *            the Calendar day of the week on which daylight savings time
     *            ends
     * @param endTime
     *            the time of day in milliseconds standard time on which
     *            daylight savings time ends
     * @param daylightSavings
     *            the daylight savings time difference in milliseconds
     */
    public SimpleTimeZone(int offset, String name, int startMonth,
            int startDay, int startDayOfWeek, int startTime, int endMonth,
            int endDay, int endDayOfWeek, int endTime, int daylightSavings) {
        this(offset, name);
        if (daylightSavings <= 0) {
            throw new IllegalArgumentException(Msg.getString("K00e9", daylightSavings)); //$NON-NLS-1$
        }
        dstSavings = daylightSavings;

        setStartRule(startMonth, startDay, startDayOfWeek, startTime);
        setEndRule(endMonth, endDay, endDayOfWeek, endTime);
    }

    /**
     * Constructs a new SimpleTimeZone using the specified offset for standard
     * time from GMT, the specified time zone ID, the rules for daylight savings
     * time, and the modes indicating UTC, standard, or wall time.
     * 
     * @param offset
     *            the offset from GMT of standard time in milliseconds
     * @param name
     *            the time zone ID
     * @param startMonth
     *            the Calendar month in which daylight savings time starts
     * @param startDay
     *            the occurrence of the day of the week on which daylight
     *            savings time starts
     * @param startDayOfWeek
     *            the Calendar day of the week on which daylight savings time
     *            starts
     * @param startTime
     *            the time of day in milliseconds on which daylight savings time
     *            starts
     * @param startTimeMode
     *            the mode (UTC, standard, or wall time) of the start time value
     * @param endMonth
     *            the Calendar month in which daylight savings time ends
     * @param endDay
     *            the occurrence of the day of the week on which daylight
     *            savings time ends
     * @param endDayOfWeek
     *            the Calendar day of the week on which daylight savings time
     *            ends
     * @param endTime
     *            the time of day in milliseconds standard time on which
     *            daylight savings time ends
     * @param endTimeMode
     *            the mode (UTC, standard, or wall time) of the end time value
     * @param daylightSavings
     *            the daylight savings time difference in milliseconds
     */
    public SimpleTimeZone(int offset, String name, int startMonth,
            int startDay, int startDayOfWeek, int startTime, int startTimeMode,
            int endMonth, int endDay, int endDayOfWeek, int endTime,
            int endTimeMode, int daylightSavings) {

        this(offset, name, startMonth, startDay, startDayOfWeek, startTime,
                endMonth, endDay, endDayOfWeek, endTime, daylightSavings);
        startMode = startTimeMode;
        endMode = endTimeMode;
    }

    /**
     * Returns a new SimpleTimeZone with the same ID, rawOffset and daylight
     * savings time rules as this SimpleTimeZone.
     * 
     * @return a shallow copy of this SimpleTimeZone
     * 
     * @see java.lang.Cloneable
     */
    @Override
    public Object clone() {
        SimpleTimeZone zone = (SimpleTimeZone) super.clone();
        if (daylightSavings != null) {
            zone.daylightSavings = (GregorianCalendar) daylightSavings.clone();
        }
        return zone;
    }

    /**
     * Compares the specified object to this SimpleTimeZone and answer if they
     * are equal. The object must be an instance of SimpleTimeZone and have the
     * same properties.
     * 
     * @param object
     *            the object to compare with this object
     * @return true if the specified object is equal to this SimpleTimeZone,
     *         false otherwise
     * 
     * @see #hashCode
     */
    @Override
    public boolean equals(Object object) {
        if (!(object instanceof SimpleTimeZone)) {
            return false;
        }
        SimpleTimeZone tz = (SimpleTimeZone) object;
        return getID().equals(tz.getID())
                && rawOffset == tz.rawOffset
                && useDaylight == tz.useDaylight
                && (!useDaylight || (startYear == tz.startYear
                        && startMonth == tz.startMonth
                        && startDay == tz.startDay && startMode == tz.startMode
                        && startDayOfWeek == tz.startDayOfWeek
                        && startTime == tz.startTime && endMonth == tz.endMonth
                        && endDay == tz.endDay
                        && endDayOfWeek == tz.endDayOfWeek
                        && endTime == tz.endTime && endMode == tz.endMode && dstSavings == tz.dstSavings));
    }

    /**
     * Gets the daylight savings offset in milliseconds for this SimpleTimeZone.
     * 
     * If this SimpleTimezone does not observe daylight savings, returns 0.
     * 
     * @return the daylight savings offset in milliseconds
     */
    @Override
    public int getDSTSavings() {
        if (!useDaylight) {
            return 0;
        }
        return dstSavings;
    }

    /**
     * Gets the offset from GMT of this SimpleTimeZone for the specified date
     * and time. The offset includes daylight savings time if the specified date
     * and time are within the daylight savings time period.
     * 
     * @param era
     *            the GregorianCalendar era, either GregorianCalendar.BC or
     *            GregorianCalendar.AD
     * @param year
     *            the year
     * @param month
     *            the Calendar month
     * @param day
     *            the day of the month
     * @param dayOfWeek
     *            the Calendar day of the week
     * @param time
     *            the time of day in milliseconds
     * @return the offset from GMT in milliseconds
     */
    @Override
    public int getOffset(int era, int year, int month, int day, int dayOfWeek,
            int time) {
        if (era != GregorianCalendar.BC && era != GregorianCalendar.AD) {
            throw new IllegalArgumentException(Msg.getString("K00ea", era)); //$NON-NLS-1$
        }
        checkRange(month, dayOfWeek, time);
        if (month != Calendar.FEBRUARY || day != 29 || !isLeapYear(year)) {
            checkDay(month, day);
        }

        if (!useDaylightTime() || era != GregorianCalendar.AD
                || year < startYear) {
            return rawOffset;
        }
        if (endMonth < startMonth) {
            if (month > endMonth && month < startMonth) {
                return rawOffset;
            }
        } else {
            if (month < startMonth || month > endMonth) {
                return rawOffset;
            }
        }

        int ruleDay = 0, daysInMonth, firstDayOfMonth = mod7(dayOfWeek - day);
        if (month == startMonth) {
            switch (startMode) {
            case DOM_MODE:
                ruleDay = startDay;
                break;
            case DOW_IN_MONTH_MODE:
                if (startDay >= 0) {
                    ruleDay = mod7(startDayOfWeek - firstDayOfMonth) + 1
                            + (startDay - 1) * 7;
                } else {
                    daysInMonth = GregorianCalendar.DaysInMonth[startMonth];
                    if (startMonth == Calendar.FEBRUARY && isLeapYear(year)) {
                        daysInMonth += 1;
                    }
                    ruleDay = daysInMonth
                            + 1
                            + mod7(startDayOfWeek
                                    - (firstDayOfMonth + daysInMonth))
                            + startDay * 7;
                }
                break;
            case DOW_GE_DOM_MODE:
                ruleDay = startDay
                        + mod7(startDayOfWeek
                                - (firstDayOfMonth + startDay - 1));
                break;
            case DOW_LE_DOM_MODE:
                ruleDay = startDay
                        + mod7(startDayOfWeek
                                - (firstDayOfMonth + startDay - 1));
                if (ruleDay != startDay) {
                    ruleDay -= 7;
                }
                break;
            }
            if (ruleDay > day || ruleDay == day && time < startTime) {
                return rawOffset;
            }
        }

        int ruleTime = endTime - dstSavings;
        int nextMonth = (month + 1) % 12;
        if (month == endMonth || (ruleTime < 0 && nextMonth == endMonth)) {
            switch (endMode) {
            case DOM_MODE:
                ruleDay = endDay;
                break;
            case DOW_IN_MONTH_MODE:
                if (endDay >= 0) {
                    ruleDay = mod7(endDayOfWeek - firstDayOfMonth) + 1
                            + (endDay - 1) * 7;
                } else {
                    daysInMonth = GregorianCalendar.DaysInMonth[endMonth];
                    if (endMonth == Calendar.FEBRUARY && isLeapYear(year)) {
                        daysInMonth++;
                    }
                    ruleDay = daysInMonth
                            + 1
                            + mod7(endDayOfWeek
                                    - (firstDayOfMonth + daysInMonth)) + endDay
                            * 7;
                }
                break;
            case DOW_GE_DOM_MODE:
                ruleDay = endDay
                        + mod7(endDayOfWeek - (firstDayOfMonth + endDay - 1));
                break;
            case DOW_LE_DOM_MODE:
                ruleDay = endDay
                        + mod7(endDayOfWeek - (firstDayOfMonth + endDay - 1));
                if (ruleDay != endDay) {
                    ruleDay -= 7;
                }
                break;
            }

            int ruleMonth = endMonth;
            if (ruleTime < 0) {
                int changeDays = 1 - (ruleTime / 86400000);
                ruleTime = (ruleTime % 86400000) + 86400000;
                ruleDay -= changeDays;
                if (ruleDay <= 0) {
                    if (--ruleMonth < Calendar.JANUARY) {
                        ruleMonth = Calendar.DECEMBER;
                    }
                    ruleDay += GregorianCalendar.DaysInMonth[ruleMonth];
                    if (ruleMonth == Calendar.FEBRUARY && isLeapYear(year)) {
                        ruleDay++;
                    }
                }
            }

            if (month == ruleMonth) {
                if (ruleDay < day || ruleDay == day && time >= ruleTime) {
                    return rawOffset;
                }
            } else if (nextMonth != ruleMonth) {
                return rawOffset;
            }
        }
        return rawOffset + dstSavings;
    }

    /**
     * Gets the offset from GMT of this SimpleTimeZone for the specified date.
     * The offset includes daylight savings time if the specified date is within
     * the daylight savings time period.
     * 
     * @param time
     *            the date in milliseconds since January 1, 1970 00:00:00 GMT
     * @return the offset from GMT in milliseconds
     */
    @Override
    public int getOffset(long time) {
        if (!useDaylightTime()) {
            return rawOffset;
        }
        if (daylightSavings == null) {
            daylightSavings = new GregorianCalendar(this);
        }
        return daylightSavings.getOffset(time + rawOffset);
    }

    /**
     * Gets the offset for standard time from GMT for this SimpleTimeZone.
     * 
     * @return the offset from GMT of standard time in milliseconds
     */
    @Override
    public int getRawOffset() {
        return rawOffset;
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
    public synchronized int hashCode() {
        int hashCode = getID().hashCode() + rawOffset;
        if (useDaylight) {
            hashCode += startYear + startMonth + startDay + startDayOfWeek
                    + startTime + startMode + endMonth + endDay + endDayOfWeek
                    + endTime + endMode + dstSavings;
        }
        return hashCode;
    }

    /**
     * Returns if the specified TimeZone has the same raw offset and daylight
     * savings time rules as this SimpleTimeZone.
     * 
     * @param zone
     *            a TimeZone
     * @return true when the TimeZones have the same raw offset and daylight
     *         savings time rules, false otherwise
     */
    @Override
    public boolean hasSameRules(TimeZone zone) {
        if (!(zone instanceof SimpleTimeZone)) {
            return false;
        }
        SimpleTimeZone tz = (SimpleTimeZone) zone;
        if (useDaylight != tz.useDaylight) {
            return false;
        }
        if (!useDaylight) {
            return rawOffset == tz.rawOffset;
        }
        return rawOffset == tz.rawOffset && dstSavings == tz.dstSavings
                && startYear == tz.startYear && startMonth == tz.startMonth
                && startDay == tz.startDay && startMode == tz.startMode
                && startDayOfWeek == tz.startDayOfWeek
                && startTime == tz.startTime && endMonth == tz.endMonth
                && endDay == tz.endDay && endDayOfWeek == tz.endDayOfWeek
                && endTime == tz.endTime && endMode == tz.endMode;
    }

    /**
     * Returns if the specified Date is in the daylight savings time period for
     * this SimpleTimeZone.
     * 
     * @param time
     *            a Date
     * @return true when the Date is in the daylight savings time period, false
     *         otherwise
     */
    @Override
    public boolean inDaylightTime(Date time) {
        // check for null pointer
        long millis = time.getTime();
        if (!useDaylightTime()) {
            return false;
        }
        if (daylightSavings == null) {
            daylightSavings = new GregorianCalendar(this);
        }
        return daylightSavings.getOffset(millis + rawOffset) != rawOffset;
    }

    private boolean isLeapYear(int year) {
        if (year > 1582) {
            return year % 4 == 0 && (year % 100 != 0 || year % 400 == 0);
        }
        return year % 4 == 0;
    }

    private int mod7(int num1) {
        int rem = num1 % 7;
        return (num1 < 0 && rem < 0) ? 7 + rem : rem;
    }

    /**
     * Sets the daylight savings offset in milliseconds for this SimpleTimeZone.
     * 
     * @param milliseconds
     *            the daylight savings offset in milliseconds
     */
    public void setDSTSavings(int milliseconds) {
        if (milliseconds > 0) {
            dstSavings = milliseconds;
        } else {
            throw new IllegalArgumentException();
        }
    }

    private void checkRange(int month, int dayOfWeek, int time) {
        if (month < Calendar.JANUARY || month > Calendar.DECEMBER) {
            throw new IllegalArgumentException(Msg.getString("K00e5", month)); //$NON-NLS-1$
        }
        if (dayOfWeek < Calendar.SUNDAY || dayOfWeek > Calendar.SATURDAY) {
            throw new IllegalArgumentException(Msg.getString("K00e7", dayOfWeek)); //$NON-NLS-1$
        }
        if (time < 0 || time >= 24 * 3600000) {
            throw new IllegalArgumentException(Msg.getString("K00e8", time)); //$NON-NLS-1$
        }
    }

    private void checkDay(int month, int day) {
        if (day <= 0 || day > GregorianCalendar.DaysInMonth[month]) {
            throw new IllegalArgumentException(Msg.getString("K00e6", day)); //$NON-NLS-1$
        }
    }

    private void setEndMode() {
        if (endDayOfWeek == 0) {
            endMode = DOM_MODE;
        } else if (endDayOfWeek < 0) {
            endDayOfWeek = -endDayOfWeek;
            if (endDay < 0) {
                endDay = -endDay;
                endMode = DOW_LE_DOM_MODE;
            } else {
                endMode = DOW_GE_DOM_MODE;
            }
        } else {
            endMode = DOW_IN_MONTH_MODE;
        }
        useDaylight = startDay != 0 && endDay != 0;
        if (endDay != 0) {
            checkRange(endMonth, endMode == DOM_MODE ? 1 : endDayOfWeek,
                    endTime);
            if (endMode != DOW_IN_MONTH_MODE) {
                checkDay(endMonth, endDay);
            } else {
                if (endDay < -5 || endDay > 5) {
                    throw new IllegalArgumentException(Msg.getString("K00f8", endDay)); //$NON-NLS-1$
                }
            }
        }
        if (endMode != DOM_MODE) {
            endDayOfWeek--;
        }
    }

    /**
     * Sets the rule which specifies the end of daylight savings time.
     * 
     * @param month
     *            the Calendar month in which daylight savings time ends
     * @param dayOfMonth
     *            the Calendar day of the month on which daylight savings time
     *            ends
     * @param time
     *            the time of day in milliseconds standard time on which
     *            daylight savings time ends
     */
    public void setEndRule(int month, int dayOfMonth, int time) {
        endMonth = month;
        endDay = dayOfMonth;
        endDayOfWeek = 0; // Initialize this value for hasSameRules()
        endTime = time;
        setEndMode();
    }

    /**
     * Sets the rule which specifies the end of daylight savings time.
     * 
     * @param month
     *            the Calendar month in which daylight savings time ends
     * @param day
     *            the occurrence of the day of the week on which daylight
     *            savings time ends
     * @param dayOfWeek
     *            the Calendar day of the week on which daylight savings time
     *            ends
     * @param time
     *            the time of day in milliseconds standard time on which
     *            daylight savings time ends
     */
    public void setEndRule(int month, int day, int dayOfWeek, int time) {
        endMonth = month;
        endDay = day;
        endDayOfWeek = dayOfWeek;
        endTime = time;
        setEndMode();
    }

    /**
     * Sets the rule which specifies the end of daylight savings time.
     * 
     * @param month
     *            the Calendar month in which daylight savings time ends
     * @param day
     *            the Calendar day of the month
     * @param dayOfWeek
     *            the Calendar day of the week on which daylight savings time
     *            ends
     * @param time
     *            the time of day in milliseconds on which daylight savings time
     *            ends
     * @param after
     *            selects the day after or before the day of month
     */
    public void setEndRule(int month, int day, int dayOfWeek, int time,
            boolean after) {
        endMonth = month;
        endDay = after ? day : -day;
        endDayOfWeek = -dayOfWeek;
        endTime = time;
        setEndMode();
    }

    /**
     * Sets the offset for standard time from GMT for this SimpleTimeZone.
     * 
     * @param offset
     *            the offset from GMT of standard time in milliseconds
     */
    @Override
    public void setRawOffset(int offset) {
        rawOffset = offset;
    }

    private void setStartMode() {
        if (startDayOfWeek == 0) {
            startMode = DOM_MODE;
        } else if (startDayOfWeek < 0) {
            startDayOfWeek = -startDayOfWeek;
            if (startDay < 0) {
                startDay = -startDay;
                startMode = DOW_LE_DOM_MODE;
            } else {
                startMode = DOW_GE_DOM_MODE;
            }
        } else {
            startMode = DOW_IN_MONTH_MODE;
        }
        useDaylight = startDay != 0 && endDay != 0;
        if (startDay != 0) {
            checkRange(startMonth, startMode == DOM_MODE ? 1 : startDayOfWeek,
                    startTime);
            if (startMode != DOW_IN_MONTH_MODE) {
                checkDay(startMonth, startDay);
            } else {
                if (startDay < -5 || startDay > 5) {
                    throw new IllegalArgumentException(Msg.getString("K00f8", startDay)); //$NON-NLS-1$
                }
            }
        }
        if (startMode != DOM_MODE) {
            startDayOfWeek--;
        }
    }

    /**
     * Sets the rule which specifies the start of daylight savings time.
     * 
     * @param month
     *            the Calendar month in which daylight savings time starts
     * @param dayOfMonth
     *            the Calendar day of the month on which daylight savings time
     *            starts
     * @param time
     *            the time of day in milliseconds on which daylight savings time
     *            starts
     */
    public void setStartRule(int month, int dayOfMonth, int time) {
        startMonth = month;
        startDay = dayOfMonth;
        startDayOfWeek = 0; // Initialize this value for hasSameRules()
        startTime = time;
        setStartMode();
    }

    /**
     * Sets the rule which specifies the start of daylight savings time.
     * 
     * @param month
     *            the Calendar month in which daylight savings time starts
     * @param day
     *            the occurrence of the day of the week on which daylight
     *            savings time starts
     * @param dayOfWeek
     *            the Calendar day of the week on which daylight savings time
     *            starts
     * @param time
     *            the time of day in milliseconds on which daylight savings time
     *            starts
     */
    public void setStartRule(int month, int day, int dayOfWeek, int time) {
        startMonth = month;
        startDay = day;
        startDayOfWeek = dayOfWeek;
        startTime = time;
        setStartMode();
    }

    /**
     * Sets the rule which specifies the start of daylight savings time.
     * 
     * @param month
     *            the Calendar month in which daylight savings time starts
     * @param day
     *            the Calendar day of the month
     * @param dayOfWeek
     *            the Calendar day of the week on which daylight savings time
     *            starts
     * @param time
     *            the time of day in milliseconds on which daylight savings time
     *            starts
     * @param after
     *            selects the day after or before the day of month
     */
    public void setStartRule(int month, int day, int dayOfWeek, int time,
            boolean after) {
        startMonth = month;
        startDay = after ? day : -day;
        startDayOfWeek = -dayOfWeek;
        startTime = time;
        setStartMode();
    }

    /**
     * Sets the starting year for daylight savings time in this SimpleTimeZone.
     * Years before this start year will always be in standard time.
     * 
     * @param year
     *            the starting year
     */
    public void setStartYear(int year) {
        startYear = year;
        useDaylight = true;
    }

    /**
     * Returns the string representation of this SimpleTimeZone.
     * 
     * @return the string representation of this SimpleTimeZone
     */
    @Override
    public String toString() {
        return getClass().getName()
                + "[id=" //$NON-NLS-1$
                + getID()
                + ",offset=" //$NON-NLS-1$
                + rawOffset
                + ",dstSavings=" //$NON-NLS-1$
                + dstSavings
                + ",useDaylight=" //$NON-NLS-1$
                + useDaylight
                + ",startYear=" //$NON-NLS-1$
                + startYear
                + ",startMode=" //$NON-NLS-1$
                + startMode
                + ",startMonth=" //$NON-NLS-1$
                + startMonth
                + ",startDay=" //$NON-NLS-1$
                + startDay
                + ",startDayOfWeek=" //$NON-NLS-1$
                + (useDaylight && (startMode != DOM_MODE) ? startDayOfWeek + 1
                        : 0) + ",startTime=" + startTime + ",endMode=" //$NON-NLS-1$ //$NON-NLS-2$
                + endMode + ",endMonth=" + endMonth + ",endDay=" + endDay //$NON-NLS-1$ //$NON-NLS-2$
                + ",endDayOfWeek=" //$NON-NLS-1$
                + (useDaylight && (endMode != DOM_MODE) ? endDayOfWeek + 1 : 0)
                + ",endTime=" + endTime + "]";  //$NON-NLS-1$//$NON-NLS-2$
    }

    /**
     * Returns if this TimeZone has a daylight savings time period.
     * 
     * @return true if this time zone has a daylight savings time period, false
     *         otherwise
     */
    @Override
    public boolean useDaylightTime() {
        return useDaylight;
    }

    private static final ObjectStreamField[] serialPersistentFields = {
            new ObjectStreamField("dstSavings", Integer.TYPE), //$NON-NLS-1$
            new ObjectStreamField("endDay", Integer.TYPE), //$NON-NLS-1$
            new ObjectStreamField("endDayOfWeek", Integer.TYPE), //$NON-NLS-1$
            new ObjectStreamField("endMode", Integer.TYPE), //$NON-NLS-1$
            new ObjectStreamField("endMonth", Integer.TYPE), //$NON-NLS-1$
            new ObjectStreamField("endTime", Integer.TYPE), //$NON-NLS-1$
            new ObjectStreamField("monthLength", byte[].class), //$NON-NLS-1$
            new ObjectStreamField("rawOffset", Integer.TYPE), //$NON-NLS-1$
            new ObjectStreamField("serialVersionOnStream", Integer.TYPE), //$NON-NLS-1$
            new ObjectStreamField("startDay", Integer.TYPE), //$NON-NLS-1$
            new ObjectStreamField("startDayOfWeek", Integer.TYPE), //$NON-NLS-1$
            new ObjectStreamField("startMode", Integer.TYPE), //$NON-NLS-1$
            new ObjectStreamField("startMonth", Integer.TYPE), //$NON-NLS-1$
            new ObjectStreamField("startTime", Integer.TYPE), //$NON-NLS-1$
            new ObjectStreamField("startYear", Integer.TYPE), //$NON-NLS-1$
            new ObjectStreamField("useDaylight", Boolean.TYPE), }; //$NON-NLS-1$

    private void writeObject(ObjectOutputStream stream) throws IOException {
        int sEndDay = endDay, sEndDayOfWeek = endDayOfWeek + 1, sStartDay = startDay, sStartDayOfWeek = startDayOfWeek + 1;
        if (useDaylight
                && (startMode != DOW_IN_MONTH_MODE || endMode != DOW_IN_MONTH_MODE)) {
            Calendar cal = new GregorianCalendar(this);
            if (endMode != DOW_IN_MONTH_MODE) {
                cal.set(Calendar.MONTH, endMonth);
                cal.set(Calendar.DATE, endDay);
                sEndDay = cal.get(Calendar.DAY_OF_WEEK_IN_MONTH);
                if (endMode == DOM_MODE) {
                    sEndDayOfWeek = cal.getFirstDayOfWeek();
                }
            }
            if (startMode != DOW_IN_MONTH_MODE) {
                cal.set(Calendar.MONTH, startMonth);
                cal.set(Calendar.DATE, startDay);
                sStartDay = cal.get(Calendar.DAY_OF_WEEK_IN_MONTH);
                if (startMode == DOM_MODE) {
                    sStartDayOfWeek = cal.getFirstDayOfWeek();
                }
            }
        }
        ObjectOutputStream.PutField fields = stream.putFields();
        fields.put("dstSavings", dstSavings); //$NON-NLS-1$
        fields.put("endDay", sEndDay); //$NON-NLS-1$
        fields.put("endDayOfWeek", sEndDayOfWeek); //$NON-NLS-1$
        fields.put("endMode", endMode); //$NON-NLS-1$
        fields.put("endMonth", endMonth); //$NON-NLS-1$
        fields.put("endTime", endTime); //$NON-NLS-1$
        fields.put("monthLength", GregorianCalendar.DaysInMonth); //$NON-NLS-1$
        fields.put("rawOffset", rawOffset); //$NON-NLS-1$
        fields.put("serialVersionOnStream", 1); //$NON-NLS-1$
        fields.put("startDay", sStartDay); //$NON-NLS-1$
        fields.put("startDayOfWeek", sStartDayOfWeek); //$NON-NLS-1$
        fields.put("startMode", startMode); //$NON-NLS-1$
        fields.put("startMonth", startMonth); //$NON-NLS-1$
        fields.put("startTime", startTime); //$NON-NLS-1$
        fields.put("startYear", startYear); //$NON-NLS-1$
        fields.put("useDaylight", useDaylight); //$NON-NLS-1$
        stream.writeFields();
        stream.writeInt(4);
        byte[] values = new byte[4];
        values[0] = (byte) startDay;
        values[1] = (byte) (startMode == DOM_MODE ? 0 : startDayOfWeek + 1);
        values[2] = (byte) endDay;
        values[3] = (byte) (endMode == DOM_MODE ? 0 : endDayOfWeek + 1);
        stream.write(values);
    }

    private void readObject(ObjectInputStream stream) throws IOException,
            ClassNotFoundException {
        ObjectInputStream.GetField fields = stream.readFields();
        rawOffset = fields.get("rawOffset", 0); //$NON-NLS-1$
        useDaylight = fields.get("useDaylight", false); //$NON-NLS-1$
        if (useDaylight) {
            endMonth = fields.get("endMonth", 0); //$NON-NLS-1$
            endTime = fields.get("endTime", 0); //$NON-NLS-1$
            startMonth = fields.get("startMonth", 0); //$NON-NLS-1$
            startTime = fields.get("startTime", 0); //$NON-NLS-1$
            startYear = fields.get("startYear", 0); //$NON-NLS-1$
        }
        if (fields.get("serialVersionOnStream", 0) == 0) { //$NON-NLS-1$
            if (useDaylight) {
                startMode = endMode = DOW_IN_MONTH_MODE;
                endDay = fields.get("endDay", 0); //$NON-NLS-1$
                endDayOfWeek = fields.get("endDayOfWeek", 0) - 1; //$NON-NLS-1$
                startDay = fields.get("startDay", 0); //$NON-NLS-1$
                startDayOfWeek = fields.get("startDayOfWeek", 0) - 1; //$NON-NLS-1$
            }
        } else {
            dstSavings = fields.get("dstSavings", 0); //$NON-NLS-1$
            if (useDaylight) {
                endMode = fields.get("endMode", 0); //$NON-NLS-1$
                startMode = fields.get("startMode", 0); //$NON-NLS-1$
                int length = stream.readInt();
                byte[] values = new byte[length];
                stream.readFully(values);
                if (length >= 4) {
                    startDay = values[0];
                    startDayOfWeek = values[1];
                    if (startMode != DOM_MODE) {
                        startDayOfWeek--;
                    }
                    endDay = values[2];
                    endDayOfWeek = values[3];
                    if (endMode != DOM_MODE) {
                        endDayOfWeek--;
                    }
                }
            }
        }
    }

}
