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

package java.sql;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Java representation of an SQL TIME value. Provides functions to aid
 * generation and interpretation of JDBC escape format for time values.
 * 
 */
public class Time extends Date {

    private static final long serialVersionUID = 8397324403548013681L;

    /**
     * @deprecated Please use the constructor {@link #Time(long)} Constructs a Time
     *             object using the supplied values for Hour, Minute and Second.
     *             The Year, Month and Day elements of the Time object are set
     *             to 1970, January, 1 reflecting the Epoch (Time in
     *             milliseconds = 0).
     *             <p>
     *             Any attempt to access the Year, Month or Day elements of a
     *             Time object will result in an IllegalArgumentException.
     *             <p>
     *             Result is undefined if any argument is out of bounds.
     * @param theHour
     *            a value from 0 - 23
     * @param theMinute
     *            a value from 0 - 59
     * @param theSecond
     *            a value from 0 - 59
     */
    @SuppressWarnings("deprecation")
    @Deprecated
    public Time(int theHour, int theMinute, int theSecond) {
        super(70, 0, 1, theHour, theMinute, theSecond);
    }

    /**
     * Constructs a Time object using a supplied time specified in milliseconds
     * 
     * @param theTime
     *            a Time specified in milliseconds since the Epoch (January 1st
     *            1970, 00:00:00.000)
     */
    public Time(long theTime) {
        super(theTime);
    }

    /**
     * @deprecated This method is deprecated and must not be used. An SQL Time
     *             object does not have a Date component.
     * @return does not return
     * @throws IllegalArgumentException
     *             if this method is called
     */
    @SuppressWarnings("deprecation")
    @Deprecated
    @Override
    public int getDate() {
        throw new IllegalArgumentException();
    }

    /**
     * @deprecated This method is deprecated and must not be used. An SQL Time
     *             object does not have a Day component.
     * @return does not return
     * @throws IllegalArgumentException
     *             if this method is called
     */
    @SuppressWarnings("deprecation")
    @Deprecated
    @Override
    public int getDay() {
        throw new IllegalArgumentException();
    }

    /**
     * @deprecated This method is deprecated and must not be used. An SQL Time
     *             object does not have a Month component.
     * @return does not return
     * @throws IllegalArgumentException
     *             if this method is called
     */
    @SuppressWarnings("deprecation")
    @Deprecated
    @Override
    public int getMonth() {
        throw new IllegalArgumentException();
    }

    /**
     * @deprecated This method is deprecated and must not be used. An SQL Time
     *             object does not have a Year component.
     * @return does not return
     * @throws IllegalArgumentException
     *             if this method is called
     */
    @SuppressWarnings("deprecation")
    @Deprecated
    @Override
    public int getYear() {
        throw new IllegalArgumentException();
    }

    /**
     * @deprecated This method is deprecated and must not be used. An SQL Time
     *             object does not have a Date component.
     * @throws IllegalArgumentException
     *             if this method is called
     */
    @SuppressWarnings("deprecation")
    @Deprecated
    @Override
    public void setDate(int i) {
        throw new IllegalArgumentException();
    }

    /**
     * @deprecated This method is deprecated and must not be used. An SQL Time
     *             object does not have a Month component.
     * @throws IllegalArgumentException
     *             if this method is called
     */
    @SuppressWarnings("deprecation")
    @Deprecated
    @Override
    public void setMonth(int i) {
        throw new IllegalArgumentException();
    }

    /**
     * @deprecated This method is deprecated and must not be used. An SQL Time
     *             object does not have a Year component.
     * @throws IllegalArgumentException
     *             if this method is called
     */
    @SuppressWarnings("deprecation")
    @Deprecated
    @Override
    public void setYear(int i) {
        throw new IllegalArgumentException();
    }

    /**
     * Sets the time for this Time object to the supplied milliseconds value.
     * 
     * @param time
     *            A time value expressed as milliseconds since the Epoch.
     *            Negative values are milliseconds before the Epoch. The Epoch
     *            is January 1 1970, 00:00:00.000
     */
    @Override
    public void setTime(long time) {
        super.setTime(time);
    }

    /**
     * Formats the Time as a String in JDBC escape format: hh:mm:ss
     * 
     * @return A String representing the Time value in JDBC escape format:
     *         HH:mm:ss
     */
    @Override
    public String toString() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss"); //$NON-NLS-1$
        return dateFormat.format(this);
    }

    /**
     * Creates a Time object from a String holding a time represented in JDBC
     * escape format: hh:mm:ss.
     * <p>
     * An exception occurs if the input string is not in the form of a time in
     * JDBC escape format.
     * 
     * @param timeString
     *            A String representing the time value in JDBC escape format:
     *            hh:mm:ss
     * @return The Time object set to a time corresponding to the given time
     * @throws IllegalArgumentException
     *             if the supplied time string is not in JDBC escape format.
     */
    public static Time valueOf(String timeString) {
        if (timeString == null) {
            throw new IllegalArgumentException();
        }
        int firstIndex = timeString.indexOf(':');
        int secondIndex = timeString.indexOf(':', firstIndex + 1);
        // secondIndex == -1 means none or only one separator '-' has been found.
        // The string is separated into three parts by two separator characters,
        // if the first or the third part is null string, we should throw
        // IllegalArgumentException to follow RI
        if (secondIndex == -1|| firstIndex == 0 || secondIndex + 1 == timeString.length()) {
            throw new IllegalArgumentException();
        }
        // parse each part of the string
        int hour = Integer.parseInt(timeString.substring(0, firstIndex));
        int minute = Integer.parseInt(timeString.substring(firstIndex + 1, secondIndex));
        int second = Integer.parseInt(timeString.substring(secondIndex + 1, timeString
                .length()));
        return new Time(hour, minute, second);
    }
}
