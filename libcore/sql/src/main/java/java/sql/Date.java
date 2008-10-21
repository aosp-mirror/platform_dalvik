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

/**
 * A Date class which can consume and produce dates in SQL Date format.
 * <p>
 * The SQL date format represents a date as yyyy-mm-dd. Note that this date
 * format only deals with year, month and day values. There are no values for
 * hours, minutes, seconds.
 * <p>
 * This contrasts with regular java.util.Date values, which include time values
 * for hours, minutes, seconds, milliseconds.
 * <p>
 * Time points are handled as millisecond values - milliseconds since the epoch,
 * January 1st 1970, 00:00:00.000 GMT. Time values passed to the java.sql.Date
 * class are "normalized" to the time 00:00:00.000 GMT on the date implied by
 * the time value.
 */
public class Date extends java.util.Date {

    private static final long serialVersionUID = 1511598038487230103L;

    /**
     * @deprecated Please use the constructor {@link #Date(long)} Constructs a Date
     *             object corresponding to the supplied Year, Month and Day.
     * @param theYear
     *            the year, specified as the year minus 1900. Must be in the
     *            range 0 to 8099.
     * @param theMonth
     *            the month, specified as a number with 0 = January. Must be in
     *            the range 0 to 11.
     * @param theDay
     *            the day in the month. Must be in the range 1 to 31.
     */
    @SuppressWarnings("deprecation")
    @Deprecated
    public Date(int theYear, int theMonth, int theDay) {
        super(theYear, theMonth, theDay);
    }

    /**
     * Creates a Date which corresponds to the day implied by the supplied
     * theDate milliseconds time value.
     * 
     * @param theDate -
     *            a time value in milliseconds since the epoch - January 1 1970
     *            00:00:00 GMT. The time value (hours, minutes, seconds,
     *            milliseconds) stored in the Date object is adjusted to
     *            correspond to 00:00:00 GMT on the day implied by the supplied
     *            time value.
     */
    public Date(long theDate) {
        super(normalizeTime(theDate));
    }

    /**
     * @deprecated This method is deprecated and must not be used. SQL Date
     *             values do not have an hours component.
     * @return does not return
     * @throws IllegalArgumentException
     *             if this method is called
     */
    @SuppressWarnings("deprecation")
    @Deprecated
    @Override
    public int getHours() {
        throw new IllegalArgumentException();
    }

    /**
     * @deprecated This method is deprecated and must not be used. SQL Date
     *             values do not have a minutes component.
     * @return does not return
     * @throws IllegalArgumentException
     *             if this method is called
     */
    @SuppressWarnings("deprecation")
    @Deprecated
    @Override
    public int getMinutes() {
        throw new IllegalArgumentException();
    }

    /**
     * @deprecated This method is deprecated and must not be used. SQL Date
     *             values do not have a seconds component.
     * @return does not return
     * @throws IllegalArgumentException
     *             if this method is called
     */
    @SuppressWarnings("deprecation")
    @Deprecated
    @Override
    public int getSeconds() {
        throw new IllegalArgumentException();
    }

    /**
     * @deprecated This method is deprecated and must not be used. SQL Date
     *             values do not have an hours component.
     * @param theHours
     *            the number of hours to set
     * @throws IllegalArgumentException
     *             if this method is called
     */
    @SuppressWarnings("deprecation")
    @Deprecated
    @Override
    public void setHours(int theHours) {
        throw new IllegalArgumentException();
    }

    /**
     * @deprecated This method is deprecated and must not be used. SQL Date
     *             values do not have a minutes component.
     * @param theMinutes
     *            the number of minutes to set
     * @throws IllegalArgumentException
     *             if this method is called
     */
    @SuppressWarnings("deprecation")
    @Deprecated
    @Override
    public void setMinutes(int theMinutes) {
        throw new IllegalArgumentException();
    }

    /**
     * @deprecated This method is deprecated and must not be used. SQL Date
     *             values do not have a seconds component.
     * @param theSeconds
     *            the number of seconds to set
     * @throws IllegalArgumentException
     *             if this method is called
     */
    @SuppressWarnings("deprecation")
    @Deprecated
    @Override
    public void setSeconds(int theSeconds) {
        throw new IllegalArgumentException();
    }

    /**
     * Sets this date to a date supplied as a milliseconds value. The date is
     * set based on the supplied time value after removing any time elements
     * finer than a day, based on zero GMT for that day.
     * 
     * @param theTime
     *            the time in milliseconds since the Epoch
     */
    @Override
    public void setTime(long theTime) {
        /*
         * Store the Date based on the supplied time after removing any time
         * elements finer than the day based on zero GMT
         */
        super.setTime(normalizeTime(theTime));
    }

    /**
     * Produces a string representation of the Date in SQL format
     * 
     * @return a string representation of the Date in SQL format - "yyyy-mm-dd".
     */
    @Override
    public String toString() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd"); //$NON-NLS-1$
        return dateFormat.format(this);
    }

    /**
     * Creates a Date from a string representation of a date in SQL format.
     * 
     * @param dateString
     *            the string representation of a date in SQL format -
     *            "yyyy-mm-dd".
     * @return the Date object
     * @throws IllegalArgumentException
     *             if the format of the supplied string does not match the SQL
     *             format.
     */
    public static Date valueOf(String dateString) {
        if (dateString == null) {
            throw new IllegalArgumentException();
        }
        int firstIndex = dateString.indexOf('-');
        int secondIndex = dateString.indexOf('-', firstIndex + 1);
        // secondIndex == -1 means none or only one separator '-' has been
        // found.
        // The string is separated into three parts by two separator characters,
        // if the first or the third part is null string, we should throw
        // IllegalArgumentException to follow RI
        if (secondIndex == -1 || firstIndex == 0
                || secondIndex + 1 == dateString.length()) {
            throw new IllegalArgumentException();
        }
        // parse each part of the string
        int year = Integer.parseInt(dateString.substring(0, firstIndex));
        int month = Integer.parseInt(dateString.substring(firstIndex + 1,
                secondIndex));
        int day = Integer.parseInt(dateString.substring(secondIndex + 1,
                dateString.length()));
        return new Date(year - 1900, month - 1, day);
    }

    /*
     * Private method which normalizes a Time value, removing all low
     * significance digits corresponding to milliseconds, seconds, minutes and
     * hours, so that the returned Time value corresponds to 00:00:00 GMT on a
     * particular day.
     */
    private static long normalizeTime(long theTime) {
        return theTime;
    }
}
