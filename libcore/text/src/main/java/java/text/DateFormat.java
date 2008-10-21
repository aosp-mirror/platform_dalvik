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

package java.text;

import java.io.InvalidObjectException;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.TimeZone;

import org.apache.harmony.text.internal.nls.Messages;

/**
 * DateFormat is the abstract superclass of formats which format and parse
 * Dates.
 */
public abstract class DateFormat extends Format {

    private static final long serialVersionUID = 7218322306649953788L;

    /**
     * The calendar that this <code>DateFormat</code> uses to format a number 
     * representing a date.
     */
    protected Calendar calendar;

    /**
     * The number format used to format a number.
     */
    protected NumberFormat numberFormat;

    /**
     * Format style constant.
     */
    public final static int DEFAULT = 2;

    /**
     * Format style constant.
     */
    public final static int FULL = 0;

    /**
     * Format style constant.
     */
    public final static int LONG = 1;

    /**
     * Format style constant.
     */
    public final static int MEDIUM = 2;

    /**
     * Format style constant.
     */
    public final static int SHORT = 3;

    /**
     * Field constant.
     */
    public final static int ERA_FIELD = 0;

    /**
     * Field constant.
     */
    public final static int YEAR_FIELD = 1;

    /**
     * Field constant.
     */
    public final static int MONTH_FIELD = 2;

    /**
     * Field constant.
     */
    public final static int DATE_FIELD = 3;

    /**
     * Field constant.
     */
    public final static int HOUR_OF_DAY1_FIELD = 4;

    /**
     * Field constant.
     */
    public final static int HOUR_OF_DAY0_FIELD = 5;

    /**
     * Field constant.
     */
    public final static int MINUTE_FIELD = 6;

    /**
     * Field constant.
     */
    public final static int SECOND_FIELD = 7;

    /**
     * Field constant.
     */
    public final static int MILLISECOND_FIELD = 8;

    /**
     * Field constant.
     */
    public final static int DAY_OF_WEEK_FIELD = 9;

    /**
     * Field constant.
     */
    public final static int DAY_OF_YEAR_FIELD = 10;

    /**
     * Field constant.
     */
    public final static int DAY_OF_WEEK_IN_MONTH_FIELD = 11;

    /**
     * Field constant.
     */
    public final static int WEEK_OF_YEAR_FIELD = 12;

    /**
     * Field constant.
     */
    public final static int WEEK_OF_MONTH_FIELD = 13;

    /**
     * Field constant.
     */
    public final static int AM_PM_FIELD = 14;

    /**
     * Field constant.
     */
    public final static int HOUR1_FIELD = 15;

    /**
     * Field constant.
     */
    public final static int HOUR0_FIELD = 16;

    /**
     * Field constant.
     */
    public final static int TIMEZONE_FIELD = 17;

    /**
     * Constructs a new instance of DateFormat.
     * 
     */
    protected DateFormat() {
    }

    /**
     * Returns a new instance of DateFormat with the same properties.
     * 
     * @return a shallow copy of this DateFormat
     * 
     * @see java.lang.Cloneable
     */
    @Override
    public Object clone() {
        DateFormat clone = (DateFormat) super.clone();
        clone.calendar = (Calendar) calendar.clone();
        clone.numberFormat = (NumberFormat) numberFormat.clone();
        return clone;
    }

    /**
     * Compares the specified object to this DateFormat and answer if they are
     * equal. The object must be an instance of DateFormat with the same
     * properties.
     * 
     * @param object
     *            the object to compare with this object
     * @return true if the specified object is equal to this DateFormat, false
     *         otherwise
     * 
     * @see #hashCode
     */
    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof DateFormat)) {
            return false;
        }
        DateFormat dateFormat = (DateFormat) object;
        return numberFormat.equals(dateFormat.numberFormat)
                && calendar.getTimeZone().equals(
                        dateFormat.calendar.getTimeZone())
                && calendar.getFirstDayOfWeek() == dateFormat.calendar
                        .getFirstDayOfWeek()
                && calendar.getMinimalDaysInFirstWeek() == dateFormat.calendar
                        .getMinimalDaysInFirstWeek()
                && calendar.isLenient() == dateFormat.calendar.isLenient();
    }

    /**
     * Formats the specified object into the specified StringBuffer using the
     * rules of this DateFormat. If the field specified by the FieldPosition is
     * formatted, set the begin and end index of the formatted field in the
     * FieldPosition.
     * 
     * @param object
     *            the object to format, must be a Date or a Number. If the
     *            object is a Number, a Date is constructed using the
     *            <code>longValue()</code> of the Number.
     * @param buffer
     *            the StringBuffer
     * @param field
     *            the FieldPosition
     * @return the StringBuffer parameter <code>buffer</code>
     * 
     * @exception IllegalArgumentException
     *                when the object is not a Date or a Number
     */
    @Override
    public final StringBuffer format(Object object, StringBuffer buffer,
            FieldPosition field) {
        if (object instanceof Date) {
            return format((Date) object, buffer, field);
        }
        if (object instanceof Number) {
            return format(new Date(((Number) object).longValue()), buffer,
                    field);
        }
        throw new IllegalArgumentException();
    }

    /**
     * Formats the specified Date using the rules of this DateFormat.
     * 
     * @param date
     *            the Date to format
     * @return the formatted String
     */
    public final String format(Date date) {
        return format(date, new StringBuffer(), new FieldPosition(0))
                .toString();
    }

    /**
     * Formats the specified Date into the specified StringBuffer using the
     * rules of this DateFormat. If the field specified by the FieldPosition is
     * formatted, set the begin and end index of the formatted field in the
     * FieldPosition.
     * 
     * @param date
     *            the Date to format
     * @param buffer
     *            the StringBuffer
     * @param field
     *            the FieldPosition
     * @return the StringBuffer parameter <code>buffer</code>
     */
    public abstract StringBuffer format(Date date, StringBuffer buffer,
            FieldPosition field);

    /**
     * Gets the list of installed Locales which support DateFormat.
     * 
     * @return an array of Locale
     */
    public static Locale[] getAvailableLocales() {
        return Locale.getAvailableLocales();
    }

    /**
     * Returns the Calendar used by this DateFormat.
     * 
     * @return a Calendar
     */
    public Calendar getCalendar() {
        return calendar;
    }

    /**
     * Returns a DateFormat instance for formatting and parsing dates in the
     * DEFAULT style for the default Locale.
     * 
     * @return a DateFormat
     */
    public final static DateFormat getDateInstance() {
        return getDateInstance(DEFAULT);
    }

    /**
     * Returns a DateFormat instance for formatting and parsing dates in the
     * specified style for the default Locale.
     * 
     * @param style
     *            one of SHORT, MEDIUM, LONG, FULL, or DEFAULT
     * @return a DateFormat
     */
    public final static DateFormat getDateInstance(int style) {
        checkDateStyle(style);
        return getDateInstance(style, Locale.getDefault());
    }

    /**
     * Returns a DateFormat instance for formatting and parsing dates in the
     * specified style for the specified Locale.
     * 
     * @param style
     *            one of SHORT, MEDIUM, LONG, FULL, or DEFAULT
     * @param locale
     *            the Locale
     * @return a DateFormat
     */
    public final static DateFormat getDateInstance(int style, Locale locale) {
        checkDateStyle(style);
        ResourceBundle bundle = getBundle(locale);
        String pattern = bundle.getString("Date_" + getStyleName(style)); //$NON-NLS-1$
        return new SimpleDateFormat(pattern, locale);
    }

    /**
     * Returns a DateFormat instance for formatting and parsing dates and times
     * in the DEFAULT style for the default Locale.
     * 
     * @return a DateFormat
     */
    public final static DateFormat getDateTimeInstance() {
        return getDateTimeInstance(DEFAULT, DEFAULT);
    }

    /**
     * Returns a <code>DateFormat</code> instance for the formatting and
     * parsing of both dates and times in the manner appropriate to the default
     * Locale.
     * 
     * @param dateStyle
     *            one of SHORT, MEDIUM, LONG, FULL, or DEFAULT
     * @param timeStyle
     *            one of SHORT, MEDIUM, LONG, FULL, or DEFAULT
     * @return a DateFormat
     */
    public final static DateFormat getDateTimeInstance(int dateStyle,
            int timeStyle) {
        checkTimeStyle(timeStyle);
        checkDateStyle(dateStyle);
        return getDateTimeInstance(dateStyle, timeStyle, Locale.getDefault());
    }

    /**
     * Returns a DateFormat instance for formatting and parsing dates and times
     * in the specified styles for the specified Locale.
     * 
     * @param dateStyle
     *            one of SHORT, MEDIUM, LONG, FULL, or DEFAULT
     * @param timeStyle
     *            one of SHORT, MEDIUM, LONG, FULL, or DEFAULT
     * @param locale
     *            the Locale
     * @return a DateFormat
     */
    public final static DateFormat getDateTimeInstance(int dateStyle,
            int timeStyle, Locale locale) {
        checkTimeStyle(timeStyle);
        checkDateStyle(dateStyle);
        ResourceBundle bundle = getBundle(locale);
        String pattern = bundle.getString("Date_" + getStyleName(dateStyle)) //$NON-NLS-1$
                + " " + bundle.getString("Time_" + getStyleName(timeStyle)); //$NON-NLS-1$ //$NON-NLS-2$
        return new SimpleDateFormat(pattern, locale);
    }

    /**
     * Returns a DateFormat instance for formatting and parsing dates and times
     * in the SHORT style for the default Locale.
     * 
     * @return a DateFormat
     */
    public final static DateFormat getInstance() {
        return getDateTimeInstance(SHORT, SHORT);
    }

    /**
     * Returns the NumberFormat used by this DateFormat.
     * 
     * @return a NumberFormat
     */
    public NumberFormat getNumberFormat() {
        return numberFormat;
    }

    static String getStyleName(int style) {
        String styleName;
        switch (style) {
            case SHORT:
                styleName = "SHORT"; //$NON-NLS-1$
                break;
            case MEDIUM:
                styleName = "MEDIUM"; //$NON-NLS-1$
                break;
            case LONG:
                styleName = "LONG"; //$NON-NLS-1$
                break;
            case FULL:
                styleName = "FULL"; //$NON-NLS-1$
                break;
            default:
                styleName = ""; //$NON-NLS-1$
        }
        return styleName;
    }

    /**
     * Returns a DateFormat instance for formatting and parsing times in the
     * DEFAULT style for the default Locale.
     * 
     * @return a DateFormat
     */
    public final static DateFormat getTimeInstance() {
        return getTimeInstance(DEFAULT);
    }

    /**
     * Returns a DateFormat instance for formatting and parsing times in the
     * specified style for the default Locale.
     * 
     * @param style
     *            one of SHORT, MEDIUM, LONG, FULL, or DEFAULT
     * @return a DateFormat
     */
    public final static DateFormat getTimeInstance(int style) {
        checkTimeStyle(style);
        return getTimeInstance(style, Locale.getDefault());
    }

    /**
     * Returns a DateFormat instance for formatting and parsing times in the
     * specified style for the specified Locale.
     * 
     * @param style
     *            one of SHORT, MEDIUM, LONG, FULL, or DEFAULT
     * @param locale
     *            the Locale
     * @return a DateFormat
     */
    public final static DateFormat getTimeInstance(int style, Locale locale) {
        checkTimeStyle(style);
        ResourceBundle bundle = getBundle(locale);
        String pattern = bundle.getString("Time_" + getStyleName(style)); //$NON-NLS-1$
        return new SimpleDateFormat(pattern, locale);
    }

    /**
     * Returns the TimeZone of the Calendar used by this DateFormat.
     * 
     * @return a TimeZone
     */
    public TimeZone getTimeZone() {
        return calendar.getTimeZone();
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
        return calendar.getFirstDayOfWeek()
                + calendar.getMinimalDaysInFirstWeek()
                + calendar.getTimeZone().hashCode()
                + (calendar.isLenient() ? 1231 : 1237)
                + numberFormat.hashCode();
    }

    /**
     * Returns if the Calendar used by this DateFormat is lenient.
     * 
     * @return true when the Calendar is lenient, false otherwise
     */
    public boolean isLenient() {
        return calendar.isLenient();
    }

    /**
     * Parse a Date from the specified String using the rules of this
     * DateFormat.
     * 
     * @param string
     *            the String to parse
     * @return the Date resulting from the parse
     * 
     * @exception ParseException
     *                when an error occurs during parsing
     */
    public Date parse(String string) throws ParseException {
        ParsePosition position = new ParsePosition(0);
        Date date = parse(string, position);
        if (position.getErrorIndex() != -1 || position.getIndex() == 0) {
            // text.19=Unparseable date: {0}
            throw new ParseException(
                    Messages.getString("text.19", string), position.getErrorIndex()); //$NON-NLS-1$
        }
        return date;
    }

    /**
     * Parse a Date from the specified String starting at the index specified by
     * the ParsePosition. If the string is successfully parsed, the index of the
     * ParsePosition is updated to the index following the parsed text.
     * 
     * @param string
     *            the String to parse
     * @param position
     *            the ParsePosition, updated on return with the index following
     *            the parsed text, or on error the index is unchanged and the
     *            error index is set to the index where the error occurred
     * @return the Date resulting from the parse, or null if there is an error
     */
    public abstract Date parse(String string, ParsePosition position);

    /**
     * Parse a Date from the specified String starting at the index specified by
     * the ParsePosition. If the string is successfully parsed, the index of the
     * ParsePosition is updated to the index following the parsed text.
     * 
     * @param string
     *            the String to parse
     * @param position
     *            the ParsePosition, updated on return with the index following
     *            the parsed text, or on error the index is unchanged and the
     *            error index is set to the index where the error occurred
     * @return the Date resulting from the parse, or null if there is an error
     */
    @Override
    public Object parseObject(String string, ParsePosition position) {
        return parse(string, position);
    }

    /**
     * Sets the Calendar used by this DateFormat.
     * 
     * @param cal
     *            the Calendar
     */
    public void setCalendar(Calendar cal) {
        calendar = cal;
    }

    /**
     * Sets if the Calendar used by this DateFormat is lenient.
     * 
     * @param value
     *            true to set the Calendar to be lenient, false otherwise
     */
    public void setLenient(boolean value) {
        calendar.setLenient(value);
    }

    /**
     * Sets the NumberFormat used by this DateFormat.
     * 
     * @param format
     *            the NumberFormat
     */
    public void setNumberFormat(NumberFormat format) {
        numberFormat = format;
    }

    /**
     * Sets the TimeZone of the Calendar used by this DateFormat.
     * 
     * @param timezone
     *            the TimeZone
     */
    public void setTimeZone(TimeZone timezone) {
        calendar.setTimeZone(timezone);
    }

    /**
     * The instances of this inner class are used as attribute keys and values
     * in AttributedCharacterIterator that
     * SimpleDateFormat.formatToCharacterIterator() method returns.
     * <p>
     * There is no public constructor to this class, the only instances are the
     * constants defined here.
     * <p>
     */
    public static class Field extends Format.Field {

        private static final long serialVersionUID = 7441350119349544720L;

        private static Hashtable<Integer, Field> table = new Hashtable<Integer, Field>();

        /**
         * Marks the era part of a date.
         */
        public final static Field ERA = new Field("era", Calendar.ERA); //$NON-NLS-1$

        /**
         * Marks the year part of a date.
         */
        public final static Field YEAR = new Field("year", Calendar.YEAR); //$NON-NLS-1$

        /**
         * Marks the month part of a date.
         */
        public final static Field MONTH = new Field("month", Calendar.MONTH); //$NON-NLS-1$

        /**
         * Marks the hour of the day part of a date (0-11).
         */
        public final static Field HOUR_OF_DAY0 = new Field("hour of day", //$NON-NLS-1$
                Calendar.HOUR_OF_DAY);

        /**
         * Marks the hour of the day part of a date (1-12).
         */
        public final static Field HOUR_OF_DAY1 = new Field("hour of day 1", -1); //$NON-NLS-1$

        /**
         * Marks the minute part of a time.
         */
        public final static Field MINUTE = new Field("minute", Calendar.MINUTE); //$NON-NLS-1$

        /**
         * Marks the second part of a time.
         */
        public final static Field SECOND = new Field("second", Calendar.SECOND); //$NON-NLS-1$

        /**
         * Marks the millisecond part of a time.
         */
        public final static Field MILLISECOND = new Field("millisecond", //$NON-NLS-1$
                Calendar.MILLISECOND);

        /**
         * Marks the day of the week part of a date.
         */
        public final static Field DAY_OF_WEEK = new Field("day of week", //$NON-NLS-1$
                Calendar.DAY_OF_WEEK);

        /**
         * Marks the day of the month part of a date.
         */
        public final static Field DAY_OF_MONTH = new Field("day of month", //$NON-NLS-1$
                Calendar.DAY_OF_MONTH);

        /**
         * Marks the day of the year part of a date.
         */
        public final static Field DAY_OF_YEAR = new Field("day of year", //$NON-NLS-1$
                Calendar.DAY_OF_YEAR);

        /**
         * Marks the day of the week in the month part of a date.
         */
        public final static Field DAY_OF_WEEK_IN_MONTH = new Field(
                "day of week in month", Calendar.DAY_OF_WEEK_IN_MONTH); //$NON-NLS-1$

        /**
         * Marks the week of the year part of a date.
         */
        public final static Field WEEK_OF_YEAR = new Field("week of year", //$NON-NLS-1$
                Calendar.WEEK_OF_YEAR);

        /**
         * Marks the week of the month part of a date.
         */
        public final static Field WEEK_OF_MONTH = new Field("week of month", //$NON-NLS-1$
                Calendar.WEEK_OF_MONTH);

        /**
         * Marks the time indicator part of a date.
         */
        public final static Field AM_PM = new Field("am pm", Calendar.AM_PM); //$NON-NLS-1$

        /**
         * Marks the hour part of a date (0-11).
         */
        public final static Field HOUR0 = new Field("hour", Calendar.HOUR); //$NON-NLS-1$

        /**
         * Marks the hour part of a date (1-12).
         */
        public final static Field HOUR1 = new Field("hour 1", -1); //$NON-NLS-1$

        /**
         * Marks the time zone part of a date.
         */
        public final static Field TIME_ZONE = new Field("time zone", -1); //$NON-NLS-1$

        /**
         * The Calendar field that this Field represents.
         */
        private int calendarField = -1;

        /**
         * Constructs a new instance of DateFormat.Field with the given
         * fieldName and calendar field.
         * @param fieldName The field name.
         * @param calendarField the calender field type of the field.
         */
        protected Field(String fieldName, int calendarField) {
            super(fieldName);
            this.calendarField = calendarField;
            if (calendarField != -1
                    && table.get(new Integer(calendarField)) == null) {
                table.put(new Integer(calendarField), this);
            }
        }

        /**
         * Returns the Calendar field this Field represents
         * 
         * @return int calendar field
         */
        public int getCalendarField() {
            return calendarField;
        }

        /**
         * Returns the DateFormat.Field instance for the given calendar field
         * 
         * @param calendarField
         *            a calendar field constant
         * @return null if there is no Field for this calendar field
         */
        public static Field ofCalendarField(int calendarField) {
            if (calendarField < 0 || calendarField >= Calendar.FIELD_COUNT) {
                throw new IllegalArgumentException();
            }

            return table.get(new Integer(calendarField));
        }

        /**
         * Serialization method resolve instances to the constant
         * DateFormat.Field values
         */
        @Override
        protected Object readResolve() throws InvalidObjectException {
            if (calendarField != -1) {
                try {
                    Field result = ofCalendarField(calendarField);
                    if (result != null && this.equals(result)) {
                        return result;
                    }
                } catch (IllegalArgumentException e) {
                    // text.02=Unknown attribute
                    throw new InvalidObjectException(Messages
                            .getString("text.02")); //$NON-NLS-1$
                }
            } else {
                if (this.equals(TIME_ZONE)) {
                    return TIME_ZONE;
                }
                if (this.equals(HOUR1)) {
                    return HOUR1;
                }
                if (this.equals(HOUR_OF_DAY1)) {
                    return HOUR_OF_DAY1;
                }
            }
            // text.02=Unknown attribute
            throw new InvalidObjectException(Messages.getString("text.02")); //$NON-NLS-1$
        }
    }

    private static void checkDateStyle(int style) {
        if (!(style == SHORT || style == MEDIUM || style == LONG
                || style == FULL || style == DEFAULT)) {
            // text.0E=Illegal date style: {0}
            throw new IllegalArgumentException(Messages.getString(
                    "text.0E", style)); //$NON-NLS-1$
        }
    }

    private static void checkTimeStyle(int style) {
        if (!(style == SHORT || style == MEDIUM || style == LONG
                || style == FULL || style == DEFAULT)) {
            // text.0F=Illegal time style: {0}
            throw new IllegalArgumentException(Messages.getString(
                    "text.0F", style)); //$NON-NLS-1$
        }
    }
}
