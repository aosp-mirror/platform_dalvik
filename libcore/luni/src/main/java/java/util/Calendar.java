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
import java.io.Serializable;

/**
 * Calendar is an abstract class which provides the conversion between Dates and
 * integer calendar fields, such as the month, year or minute. Subclasses of
 * this class implement a specific calendar type, such as the gregorian
 * calendar.
 * 
 * @see Date
 * @see GregorianCalendar
 * @see TimeZone
 */
public abstract class Calendar implements Serializable, Cloneable,
        Comparable<Calendar> {

    private static final long serialVersionUID = -1807547505821590642L;

    /**
     * Set to true when the calendar fields have been set from the time, set to
     * false when a field is changed and the fields must be recomputed.
     */
    protected boolean areFieldsSet;

    /**
     * An integer array of calendar fields.
     */
    protected int[] fields;

    /*
     * A boolean array. Each element indicates if the corresponding field has
     * been set.
     */
    protected boolean[] isSet;

    /**
     * Set to true when the time has been set, set to false when a field is
     * changed and the time must be recomputed.
     */
    protected boolean isTimeSet;

    /**
     * The time in milliseconds since January 1, 1970.
     */
    protected long time;

    transient int lastTimeFieldSet;

    transient int lastDateFieldSet;

    private boolean lenient;

    private int firstDayOfWeek;

    private int minimalDaysInFirstWeek;

    private TimeZone zone;

    public static final int JANUARY = 0, FEBRUARY = 1, MARCH = 2, APRIL = 3,
            MAY = 4, JUNE = 5, JULY = 6, AUGUST = 7, SEPTEMBER = 8,
            OCTOBER = 9, NOVEMBER = 10, DECEMBER = 11, UNDECIMBER = 12,

            SUNDAY = 1, MONDAY = 2, TUESDAY = 3, WEDNESDAY = 4, THURSDAY = 5,
            FRIDAY = 6, SATURDAY = 7;

    public static final int ERA = 0, YEAR = 1, MONTH = 2, WEEK_OF_YEAR = 3,
            WEEK_OF_MONTH = 4, DATE = 5, DAY_OF_MONTH = 5, DAY_OF_YEAR = 6,
            DAY_OF_WEEK = 7, DAY_OF_WEEK_IN_MONTH = 8,

            AM_PM = 9, HOUR = 10, HOUR_OF_DAY = 11, MINUTE = 12, SECOND = 13,
            MILLISECOND = 14, ZONE_OFFSET = 15, DST_OFFSET = 16,

            FIELD_COUNT = 17,

            AM = 0, PM = 1;

    private static String[] fieldNames = { "ERA=", "YEAR=", "MONTH=", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            "WEEK_OF_YEAR=", "WEEK_OF_MONTH=", "DAY_OF_MONTH=", "DAY_OF_YEAR=",  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            "DAY_OF_WEEK=", "DAY_OF_WEEK_IN_MONTH=", "AM_PM=", "HOUR=",   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$ //$NON-NLS-4$
            "HOUR_OF_DAY", "MINUTE=", "SECOND=", "MILLISECOND=", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            "ZONE_OFFSET=", "DST_OFFSET=" }; //$NON-NLS-1$ //$NON-NLS-2$

    /**
     * Initializes this Calendar instance using the default TimeZone and Locale.
     * 
     */
    protected Calendar() {
        this(TimeZone.getDefault(), Locale.getDefault());
    }

    Calendar(TimeZone timezone) {
        fields = new int[FIELD_COUNT];
        isSet = new boolean[FIELD_COUNT];
        areFieldsSet = isTimeSet = false;
        setLenient(true);
        setTimeZone(timezone);
    }

    /**
     * Initializes this Calendar instance using the specified TimeZone and
     * Locale.
     * 
     * @param timezone
     *            the timezone
     * @param locale
     *            the locale
     */
    protected Calendar(TimeZone timezone, Locale locale) {
        this(timezone);
        ResourceBundle bundle = Locale.getBundle("Locale", locale); //$NON-NLS-1$
        setFirstDayOfWeek(((Integer) bundle.getObject("First_Day")).intValue()); //$NON-NLS-1$
        setMinimalDaysInFirstWeek(((Integer) bundle.getObject("Minimal_Days")) //$NON-NLS-1$
                .intValue());
    }

    /**
     * Adds the specified amount to a Calendar field.
     * 
     * @param field
     *            the Calendar field to modify
     * @param value
     *            the amount to add to the field
     * 
     * @exception IllegalArgumentException
     *                when the specified field is DST_OFFSET or ZONE_OFFSET.
     */
    abstract public void add(int field, int value);

    /**
     * Returns if the Date specified by this Calendar instance is after the Date
     * specified by the parameter. The comparison is not dependent on the
     * time zones of the Calendars.
     * 
     * @param calendar
     *            the Calendar instance to compare
     * @return true when this Calendar is after calendar, false otherwise
     * 
     * @exception IllegalArgumentException
     *                when the time is not set and the time cannot be computed
     *                from the current field values
     */
    public boolean after(Object calendar) {
        if (!(calendar instanceof Calendar)) {
            return false;
        }
        return getTimeInMillis() > ((Calendar) calendar).getTimeInMillis();
    }

    /**
     * Returns if the Date specified by this Calendar instance is before the
     * Date specified by the parameter. The comparison is not dependent on the
     * time zones of the Calendars.
     * 
     * @param calendar
     *            the Calendar instance to compare
     * @return true when this Calendar is before calendar, false otherwise
     * 
     * @exception IllegalArgumentException
     *                when the time is not set and the time cannot be computed
     *                from the current field values
     */
    public boolean before(Object calendar) {
        if (!(calendar instanceof Calendar)) {
            return false;
        }
        return getTimeInMillis() < ((Calendar) calendar).getTimeInMillis();
    }

    /**
     * Clears all of the fields of this Calendar. All fields are initialized to
     * zero.
     * 
     */
    public final void clear() {
        for (int i = 0; i < FIELD_COUNT; i++) {
            fields[i] = 0;
            isSet[i] = false;
        }
        areFieldsSet = isTimeSet = false;
    }

    /**
     * Clears the specified field to zero.
     * 
     * @param field
     *            the field to clear
     */
    public final void clear(int field) {
        fields[field] = 0;
        isSet[field] = false;
        areFieldsSet = isTimeSet = false;
    }

    /**
     * Returns a new Calendar with the same properties.
     * 
     * @return a shallow copy of this Calendar
     * 
     * @see java.lang.Cloneable
     */
    @Override
    public Object clone() {
        try {
            Calendar clone = (Calendar) super.clone();
            clone.fields = fields.clone();
            clone.isSet = isSet.clone();
            clone.zone = (TimeZone) zone.clone();
            return clone;
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    /**
     * Computes the time from the fields if the time has not already been set.
     * Computes the fields from the time if the fields are not already set.
     * 
     * @exception IllegalArgumentException
     *                when the time is not set and the time cannot be computed
     *                from the current field values
     */
    protected void complete() {
        if (!isTimeSet) {
            computeTime();
            isTimeSet = true;
        }
        if (!areFieldsSet) {
            computeFields();
            areFieldsSet = true;
        }
    }

    /**
     * Computes the Calendar fields from the time.
     * 
     */
    protected abstract void computeFields();

    /**
     * Computes the time from the Calendar fields.
     * 
     * @exception IllegalArgumentException
     *                when the time cannot be computed from the current field
     *                values
     */
    protected abstract void computeTime();

    /**
     * Compares the specified object to this Calendar and answer if they are
     * equal. The object must be an instance of Calendar and have the same
     * properties.
     * 
     * @param object
     *            the object to compare with this object
     * @return true if the specified object is equal to this Calendar, false
     *         otherwise
     */
    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof Calendar)) {
            return false;
        }
        Calendar cal = (Calendar) object;
        return getTimeInMillis() == cal.getTimeInMillis()
                && isLenient() == cal.isLenient()
                && getFirstDayOfWeek() == cal.getFirstDayOfWeek()
                && getMinimalDaysInFirstWeek() == cal
                        .getMinimalDaysInFirstWeek()
                && getTimeZone().equals(cal.getTimeZone());
    }

    /**
     * Gets the value of the specified field after computing the field values
     * from the time if required.
     * 
     * @param field
     *            the field
     * @return the value of the specified field
     * 
     * @exception IllegalArgumentException
     *                when the fields are not set, the time is not set, and the
     *                time cannot be computed from the current field values
     */
    public int get(int field) {
        complete();
        return fields[field];
    }

    /**
     * Gets the maximum value of the specified field for the current date.
     * 
     * @param field
     *            the field
     * @return the maximum value of the specified field
     */
    public int getActualMaximum(int field) {
        int value, next;
        if (getMaximum(field) == (next = getLeastMaximum(field))) {
            return next;
        }
        complete();
        long orgTime = time;
        set(field, next);
        do {
            value = next;
            roll(field, true);
            next = get(field);
        } while (next > value);
        time = orgTime;
        areFieldsSet = false;
        return value;
    }

    /**
     * Gets the minimum value of the specified field for the current date.
     * 
     * @param field
     *            the field
     * @return the minimum value of the specified field
     */
    public int getActualMinimum(int field) {
        int value, next;
        if (getMinimum(field) == (next = getGreatestMinimum(field))) {
            return next;
        }
        complete();
        long orgTime = time;
        set(field, next);
        do {
            value = next;
            roll(field, false);
            next = get(field);
        } while (next < value);
        time = orgTime;
        areFieldsSet = false;
        return value;
    }

    /**
     * Gets the list of installed Locales which support Calendar.
     * 
     * @return an array of Locale
     */
    public static synchronized Locale[] getAvailableLocales() {
        return Locale.getAvailableLocales();
    }

    /**
     * Gets the first day of the week for this Calendar.
     * 
     * @return a Calendar day of the week
     */
    public int getFirstDayOfWeek() {
        return firstDayOfWeek;
    }

    /**
     * Gets the greatest minimum value of the specified field.
     * 
     * @param field
     *            the field
     * @return the greatest minimum value of the specified field
     */
    abstract public int getGreatestMinimum(int field);

    /**
     * Constructs a new instance of the Calendar subclass appropriate for the
     * default Locale.
     * 
     * @return a Calendar subclass instance set to the current date and time in
     *         the default timezone
     */
    public static synchronized Calendar getInstance() {
        return new GregorianCalendar();
    }

    /**
     * Constructs a new instance of the Calendar subclass appropriate for the
     * specified Locale.
     * 
     * @param locale
     *            the locale to use
     * @return a Calendar subclass instance set to the current date and time
     */
    public static synchronized Calendar getInstance(Locale locale) {
        return new GregorianCalendar(locale);
    }

    /**
     * Constructs a new instance of the Calendar subclass appropriate for the
     * default Locale, using the specified TimeZone.
     * 
     * @param timezone
     *            the timezone to use
     * @return a Calendar subclass instance set to the current date and time in
     *         the specified timezone
     */
    public static synchronized Calendar getInstance(TimeZone timezone) {
        return new GregorianCalendar(timezone);
    }

    /**
     * Constructs a new instance of the Calendar subclass appropriate for the
     * specified Locale.
     * 
     * @param timezone
     *            the timezone to use
     * @param locale
     *            the locale to use
     * @return a Calendar subclass instance set to the current date and time in
     *         the specified timezone
     */
    public static synchronized Calendar getInstance(TimeZone timezone,
            Locale locale) {
        return new GregorianCalendar(timezone, locale);
    }

    /**
     * Gets the smallest maximum value of the specified field.
     * 
     * @param field
     *            the field
     * @return the smallest maximum value of the specified field
     */
    abstract public int getLeastMaximum(int field);

    /**
     * Gets the greatest maximum value of the specified field.
     * 
     * @param field
     *            the field
     * @return the greatest maximum value of the specified field
     */
    abstract public int getMaximum(int field);

    /**
     * Gets the minimal days in the first week of the year.
     * 
     * @return the minimal days in the first week of the year
     */
    public int getMinimalDaysInFirstWeek() {
        return minimalDaysInFirstWeek;
    }

    /**
     * Gets the smallest minimum value of the specified field.
     * 
     * @param field
     *            the field
     * @return the smallest minimum value of the specified field
     */
    abstract public int getMinimum(int field);

    /**
     * Gets the time of this Calendar as a Date object.
     * 
     * @return a new Date initialized to the time of this Calendar
     * 
     * @exception IllegalArgumentException
     *                when the time is not set and the time cannot be computed
     *                from the current field values
     */
    public final Date getTime() {
        return new Date(getTimeInMillis());
    }

    /**
     * Computes the time from the fields if required and returns the time.
     * 
     * @return the time of this Calendar
     * 
     * @exception IllegalArgumentException
     *                when the time is not set and the time cannot be computed
     *                from the current field values
     */
    public long getTimeInMillis() {
        if (!isTimeSet) {
            computeTime();
            isTimeSet = true;
        }
        return time;
    }

    /**
     * Gets the timezone of this Calendar.
     * 
     * @return the timezone used by this Calendar
     */
    public TimeZone getTimeZone() {
        return zone;
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
        return (isLenient() ? 1237 : 1231) + getFirstDayOfWeek()
                + getMinimalDaysInFirstWeek() + getTimeZone().hashCode();
    }

    /**
     * Gets the value of the specified field without recomputing.
     * 
     * @param field
     *            the field
     * @return the value of the specified field
     */
    protected final int internalGet(int field) {
        return fields[field];
    }

    /**
     * Returns if this Calendar accepts field values which are outside the valid
     * range for the field.
     * 
     * @return true if this Calendar is lenient, false otherwise
     */
    public boolean isLenient() {
        return lenient;
    }

    /**
     * Returns if the specified field is set.
     * 
     * @param field
     *            a calendar field
     * @return true if the specified field is set, false otherwise
     */
    public final boolean isSet(int field) {
        return isSet[field];
    }

    /**
     * Adds the specified amount the specified field and wrap the value of the
     * field when it goes beyond the maximum or minimum value for the current
     * date. Other fields will be adjusted as required to maintain a consistent
     * date.
     * 
     * @param field
     *            the field to roll
     * @param value
     *            the amount to add
     */
    public void roll(int field, int value) {
        boolean increment = value >= 0;
        int count = increment ? value : -value;
        for (int i = 0; i < count; i++) {
            roll(field, increment);
        }
    }

    /**
     * Increment or decrement the specified field and wrap the value of the
     * field when it goes beyond the maximum or minimum value for the current
     * date. Other fields will be adjusted as required to maintain a consistent
     * date.
     * 
     * @param field
     *            the field to roll
     * @param increment
     *            true to increment the field, false to decrement
     */
    abstract public void roll(int field, boolean increment);

    /**
     * Sets a field to the specified value.
     * 
     * @param field
     *            the Calendar field to modify
     * @param value
     *            the value
     */
    public void set(int field, int value) {
        fields[field] = value;
        isSet[field] = true;
        areFieldsSet = isTimeSet = false;
        if (field > MONTH && field < AM_PM) {
            lastDateFieldSet = field;
        }
        if (field == HOUR || field == HOUR_OF_DAY) {
            lastTimeFieldSet = field;
        }
        if (field == AM_PM) {
            lastTimeFieldSet = HOUR;
        }
    }

    /**
     * Sets the year, month and day of the month fields.
     * 
     * @param year
     *            the year
     * @param month
     *            the month
     * @param day
     *            the day of the month
     */
    public final void set(int year, int month, int day) {
        set(YEAR, year);
        set(MONTH, month);
        set(DATE, day);
    }

    /**
     * Sets the year, month, day of the month, hour of day and minute fields.
     * 
     * @param year
     *            the year
     * @param month
     *            the month
     * @param day
     *            the day of the month
     * @param hourOfDay
     *            the hour of day
     * @param minute
     *            the minute
     */
    public final void set(int year, int month, int day, int hourOfDay,
            int minute) {
        set(year, month, day);
        set(HOUR_OF_DAY, hourOfDay);
        set(MINUTE, minute);
    }

    /**
     * Sets the year, month, day of the month, hour of day, minute and second
     * fields.
     * 
     * @param year
     *            the year
     * @param month
     *            the month
     * @param day
     *            the day of the month
     * @param hourOfDay
     *            the hour of day
     * @param minute
     *            the minute
     * @param second
     *            the second
     */
    public final void set(int year, int month, int day, int hourOfDay,
            int minute, int second) {
        set(year, month, day, hourOfDay, minute);
        set(SECOND, second);
    }

    /**
     * Sets the first day of the week for this Calendar.
     * 
     * @param value
     *            a Calendar day of the week
     */
    public void setFirstDayOfWeek(int value) {
        firstDayOfWeek = value;
    }

    /**
     * Sets this Calendar to accept field values which are outside the valid
     * range for the field.
     * 
     * @param value
     *            a boolean value
     */
    public void setLenient(boolean value) {
        lenient = value;
    }

    /**
     * Sets the minimal days in the first week of the year.
     * 
     * @param value
     *            the minimal days in the first week of the year
     */
    public void setMinimalDaysInFirstWeek(int value) {
        minimalDaysInFirstWeek = value;
    }

    /**
     * Sets the time of this Calendar.
     * 
     * @param date
     *            a Date object
     */
    public final void setTime(Date date) {
        setTimeInMillis(date.getTime());
    }

    /**
     * Sets the time of this Calendar.
     * 
     * @param milliseconds
     *            the time as the number of milliseconds since Jan. 1, 1970
     */
    public void setTimeInMillis(long milliseconds) {
        time = milliseconds;
        isTimeSet = true;
        areFieldsSet = false;
        complete();
    }

    /**
     * Sets the timezone used by this Calendar.
     * 
     * @param timezone
     *            a TimeZone
     */
    public void setTimeZone(TimeZone timezone) {
        zone = timezone;
    }

    /**
     * Returns the string representation of this Calendar.
     * 
     * @return the string representation of this Calendar
     */
    @Override
    public String toString() {
        StringBuffer result = new StringBuffer(getClass().getName() + "[time=" //$NON-NLS-1$
                + (isTimeSet ? String.valueOf(time) : "?") + ",areFieldsSet="  //$NON-NLS-1$//$NON-NLS-2$
                + areFieldsSet +
                // ",areAllFieldsSet=" + areAllFieldsSet +
                ",lenient=" + lenient + ",zone=" + zone + ",firstDayOfWeek=" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                + firstDayOfWeek + ",minimalDaysInFirstWeek=" //$NON-NLS-1$
                + minimalDaysInFirstWeek);
        for (int i = 0; i < FIELD_COUNT; i++) {
            result.append(',');
            result.append(fieldNames[i]);
            result.append('=');
            if (isSet[i]) {
                result.append(fields[i]);
            } else {
                result.append('?');
            }
        }
        result.append(']');
        return result.toString();
    }

    /**
     * Compares the times of the two Calendars, which represent the milliseconds
     * from the January 1, 1970 00:00:00.000 GMT (Gregorian).
     * 
     * @param anotherCalendar
     *            another calendar that is compared with.
     * @return 0 if the times of the two calendar are equal, -1 if the time of
     *         this calendar is before the other one, 1 if the time of this
     *         calendar is after the other one.
     * @throws NullPointerException
     *             if the argument of calendar is null.
     * @throws IllegalArgumentException
     *             if the argument of the calendar does not include a valid time
     *             value.
     */
    public int compareTo(Calendar anotherCalendar) {
        if (null == anotherCalendar) {
            throw new NullPointerException();
        }
        long timeInMillis = getTimeInMillis();
        long anotherTimeInMillis = anotherCalendar.getTimeInMillis();
        if (timeInMillis > anotherTimeInMillis) {
            return 1;
        }
        if (timeInMillis == anotherTimeInMillis) {
            return 0;
        }
        return -1;
    }

    private static final ObjectStreamField[] serialPersistentFields = {
            new ObjectStreamField("areFieldsSet", Boolean.TYPE), //$NON-NLS-1$
            new ObjectStreamField("fields", int[].class), //$NON-NLS-1$
            new ObjectStreamField("firstDayOfWeek", Integer.TYPE), //$NON-NLS-1$
            new ObjectStreamField("isSet", boolean[].class), //$NON-NLS-1$
            new ObjectStreamField("isTimeSet", Boolean.TYPE), //$NON-NLS-1$
            new ObjectStreamField("lenient", Boolean.TYPE), //$NON-NLS-1$
            new ObjectStreamField("minimalDaysInFirstWeek", Integer.TYPE), //$NON-NLS-1$
            new ObjectStreamField("nextStamp", Integer.TYPE), //$NON-NLS-1$
            new ObjectStreamField("serialVersionOnStream", Integer.TYPE), //$NON-NLS-1$
            new ObjectStreamField("time", Long.TYPE), //$NON-NLS-1$
            new ObjectStreamField("zone", TimeZone.class), }; //$NON-NLS-1$

    private void writeObject(ObjectOutputStream stream) throws IOException {
        complete();
        ObjectOutputStream.PutField putFields = stream.putFields();
        putFields.put("areFieldsSet", areFieldsSet); //$NON-NLS-1$
        putFields.put("fields", this.fields); //$NON-NLS-1$
        putFields.put("firstDayOfWeek", firstDayOfWeek); //$NON-NLS-1$
        putFields.put("isSet", isSet); //$NON-NLS-1$
        putFields.put("isTimeSet", isTimeSet); //$NON-NLS-1$
        putFields.put("lenient", lenient); //$NON-NLS-1$
        putFields.put("minimalDaysInFirstWeek", minimalDaysInFirstWeek); //$NON-NLS-1$
        putFields.put("nextStamp", 2 /* MINIMUM_USER_STAMP */); //$NON-NLS-1$
        putFields.put("serialVersionOnStream", 1); //$NON-NLS-1$
        putFields.put("time", time); //$NON-NLS-1$
        putFields.put("zone", zone); //$NON-NLS-1$
        stream.writeFields();
    }

    private void readObject(ObjectInputStream stream) throws IOException,
            ClassNotFoundException {
        ObjectInputStream.GetField readFields = stream.readFields();
        areFieldsSet = readFields.get("areFieldsSet", false); //$NON-NLS-1$
        this.fields = (int[]) readFields.get("fields", null); //$NON-NLS-1$
        firstDayOfWeek = readFields.get("firstDayOfWeek", Calendar.SUNDAY); //$NON-NLS-1$
        isSet = (boolean[]) readFields.get("isSet", null); //$NON-NLS-1$
        isTimeSet = readFields.get("isTimeSet", false); //$NON-NLS-1$
        lenient = readFields.get("lenient", true); //$NON-NLS-1$
        minimalDaysInFirstWeek = readFields.get("minimalDaysInFirstWeek", 1); //$NON-NLS-1$
        time = readFields.get("time", 0L); //$NON-NLS-1$
        zone = (TimeZone) readFields.get("zone", null); //$NON-NLS-1$
    }
}
