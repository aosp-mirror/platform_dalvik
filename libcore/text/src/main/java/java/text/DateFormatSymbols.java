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
/**
*******************************************************************************
* Copyright (C) 1996-2007, International Business Machines Corporation and    *
* others. All Rights Reserved.                                                *
*******************************************************************************
*/

// BEGIN android-note
// The class javadoc and some of the method descriptions are copied from ICU4J
// source files. Changes have been made to the copied descriptions.
// The icu license header was added to this file. 
// END android-note

package java.text;

import java.io.Serializable;
// BEGIN android-added
import java.io.IOException;
import java.io.ObjectOutputStream;
// END android-added
import java.util.Arrays;
import java.util.Locale;
import java.util.ResourceBundle;

// BEGIN android-added
import com.ibm.icu4jni.util.Resources;
// END android-added
/**
 * Encapsulates localizable date-time formatting data, such as the names of the
 * months, the names of the days of the week, and the time zone data.
 * {@code DateFormat} and {@code SimpleDateFormat} both use
 * {@code DateFormatSymbols} to encapsulate this information.
 * <p>
 * Typically you shouldn't use {@code DateFormatSymbols} directly. Rather, you
 * are encouraged to create a date/time formatter with the {@code DateFormat}
 * class's factory methods: {@code getTimeInstance}, {@code getDateInstance},
 * or {@code getDateTimeInstance}. These methods automatically create a
 * {@code DateFormatSymbols} for the formatter so that you don't have to. After
 * the formatter is created, you may modify its format pattern using the
 * {@code setPattern} method. For more information about creating formatters
 * using {@code DateFormat}'s factory methods, see {@link DateFormat}.
 * </p>
 * <p>
 * If you decide to create a date/time formatter with a specific format pattern
 * for a specific locale, you can do so with:
 * </p>
 * <blockquote>
 * 
 * <pre>
 * new SimpleDateFormat(aPattern, new DateFormatSymbols(aLocale)).
 * </pre>
 * 
 * </blockquote>
 * <p>
 * {@code DateFormatSymbols} objects can be cloned. When you obtain a
 * {@code DateFormatSymbols} object, feel free to modify the date/time
 * formatting data. For instance, you can replace the localized date/time format
 * pattern characters with the ones that you feel easy to remember or you can
 * change the representative cities to your favorite ones.
 * </p>
 * <p>
 * New {@code DateFormatSymbols} subclasses may be added to support
 * {@code SimpleDateFormat} for date/time formatting for additional locales.
 * </p>
 * 
 * @see DateFormat
 * @see SimpleDateFormat
 * @since Android 1.0
 */
public class DateFormatSymbols implements Serializable, Cloneable {

    private static final long serialVersionUID = -5987973545549424702L;

    private String localPatternChars;

    String[] ampms, eras, months, shortMonths, shortWeekdays, weekdays;

    String[][] zoneStrings;

// BEGIN android-added
    /**
     * Locale, necessary to lazily load time zone strings. We force the time
     * zone names to load upon serialization, so this will never be needed
     * post deserialization.
     */
    transient final Locale locale;

    /**
     * Gets zone strings, initializing them if necessary. Does not create
     * a defensive copy, so make sure you do so before exposing the returned
     * arrays to clients.
     */
    synchronized String[][] internalZoneStrings() {
        if (zoneStrings == null) {
            zoneStrings = Resources.getDisplayTimeZones(locale.toString());
        }
        return zoneStrings;       
    }
// END android-added

    /**
     * Constructs a new {@code DateFormatSymbols} instance containing the
     * symbols for the default locale.
     * 
     * @since Android 1.0
     */
    public DateFormatSymbols() {
        this(Locale.getDefault());
    }

    /**
     * Constructs a new {@code DateFormatSymbols} instance containing the
     * symbols for the specified locale.
     * 
     * @param locale
     *            the locale.
     * @since Android 1.0
     */
    public DateFormatSymbols(Locale locale) {
        ResourceBundle bundle = Format.getBundle(locale);
        localPatternChars = bundle.getString("LocalPatternChars"); //$NON-NLS-1$
        ampms = bundle.getStringArray("ampm"); //$NON-NLS-1$
        eras = bundle.getStringArray("eras"); //$NON-NLS-1$
        months = bundle.getStringArray("months"); //$NON-NLS-1$
        shortMonths = bundle.getStringArray("shortMonths"); //$NON-NLS-1$
        shortWeekdays = bundle.getStringArray("shortWeekdays"); //$NON-NLS-1$
        weekdays = bundle.getStringArray("weekdays"); //$NON-NLS-1$
        // BEGIN android-changed
        // zoneStrings = (String[][]) bundle.getObject("timezones"); //$NON-NLS-1$
        this.locale = locale;
        // END android-changed
    }

    @Override
    public Object clone() {
        // BEGIN android-changed
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
        // END android-changed
    }

    /**
     * Compares this object with the specified object and indicates if they are
     * equal.
     * 
     * @param object
     *            the object to compare with this object.
     * @return {@code true} if {@code object} is an instance of
     *         {@code DateFormatSymbols} and has the same symbols as this
     *         object, {@code false} otherwise.
     * @see #hashCode
     * @since Android 1.0
     */
    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof DateFormatSymbols)) {
            return false;
        }
        DateFormatSymbols obj = (DateFormatSymbols) object;
        if (!localPatternChars.equals(obj.localPatternChars)) {
            return false;
        }
        if (!Arrays.equals(ampms, obj.ampms)) {
            return false;
        }
        if (!Arrays.equals(eras, obj.eras)) {
            return false;
        }
        if (!Arrays.equals(months, obj.months)) {
            return false;
        }
        if (!Arrays.equals(shortMonths, obj.shortMonths)) {
            return false;
        }
        if (!Arrays.equals(shortWeekdays, obj.shortWeekdays)) {
            return false;
        }
        if (!Arrays.equals(weekdays, obj.weekdays)) {
            return false;
        }
        // BEGIN android-changed
        // Quick check that may keep us from having to load the zone strings.
        if (zoneStrings == null && obj.zoneStrings == null
                    && !locale.equals(obj.locale)) {
            return false;
        }
        // Make sure zone strings are loaded.
        internalZoneStrings();
        obj.internalZoneStrings();
        // END android-changed
        if (zoneStrings.length != obj.zoneStrings.length) {
            return false;
        }
        for (String[] element : zoneStrings) {
            if (element.length != element.length) {
                return false;
            }
            for (int j = 0; j < element.length; j++) {
                if (element[j] != element[j]
                        && !(element[j].equals(element[j]))) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Returns the array of strings which represent AM and PM. Use the
     * {@link java.util.Calendar} constants {@code Calendar.AM} and
     * {@code Calendar.PM} as indices for the array.
     * 
     * @return an array of strings.
     * @since Android 1.0
     */
    public String[] getAmPmStrings() {
        return ampms.clone();
    }

    /**
     * Returns the array of strings which represent BC and AD. Use the
     * {@link java.util.Calendar} constants {@code GregorianCalendar.BC} and
     * {@code GregorianCalendar.AD} as indices for the array.
     * 
     * @return an array of strings.
     * @since Android 1.0
     */
    public String[] getEras() {
        return eras.clone();
    }

    /**
     * Returns the pattern characters used by {@link SimpleDateFormat} to
     * specify date and time fields.
     * 
     * @return a string containing the pattern characters.
     * @since Android 1.0
     */
    public String getLocalPatternChars() {
        return localPatternChars;
    }

    /**
     * Returns the array of strings containing the full names of the months. Use
     * the {@link java.util.Calendar} constants {@code Calendar.JANUARY} etc. as
     * indices for the array.
     * 
     * @return an array of strings.
     * @since Android 1.0
     */
    public String[] getMonths() {
        return months.clone();
    }

    /**
     * Returns the array of strings containing the abbreviated names of the
     * months. Use the {@link java.util.Calendar} constants
     * {@code Calendar.JANUARY} etc. as indices for the array.
     * 
     * @return an array of strings.
     * @since Android 1.0
     */
    public String[] getShortMonths() {
        return shortMonths.clone();
    }

    /**
     * Returns the array of strings containing the abbreviated names of the days
     * of the week. Use the {@link java.util.Calendar} constants
     * {@code Calendar.SUNDAY} etc. as indices for the array.
     * 
     * @return an array of strings.
     * @since Android 1.0
     */
    public String[] getShortWeekdays() {
        return shortWeekdays.clone();
    }

    /**
     * Returns the array of strings containing the full names of the days of the
     * week. Use the {@link java.util.Calendar} constants
     * {@code Calendar.SUNDAY} etc. as indices for the array.
     * 
     * @return an array of strings.
     * @since Android 1.0
     */
    public String[] getWeekdays() {
        return weekdays.clone();
    }

    /**
     * Returns the two-dimensional array of strings containing the names of the
     * time zones. Each element in the array is an array of five strings, the
     * first is a TimeZone ID, the second and third are the full and abbreviated
     * time zone names for standard time, and the fourth and fifth are the full
     * and abbreviated names for daylight time.
     * 
     * @return a two-dimensional array of strings.
     * @since Android 1.0
     */
    public String[][] getZoneStrings() {
        // BEGIN android-added
        String[][] zoneStrings = internalZoneStrings();
        // END android-added
        String[][] clone = new String[zoneStrings.length][];
        for (int i = zoneStrings.length; --i >= 0;) {
            clone[i] = zoneStrings[i].clone();
        }
        return clone;
    }

    @Override
    public int hashCode() {
        int hashCode;
        hashCode = localPatternChars.hashCode();
        for (String element : ampms) {
            hashCode += element.hashCode();
        }
        for (String element : eras) {
            hashCode += element.hashCode();
        }
        for (String element : months) {
            hashCode += element.hashCode();
        }
        for (String element : shortMonths) {
            hashCode += element.hashCode();
        }
        for (String element : shortWeekdays) {
            hashCode += element.hashCode();
        }
        for (String element : weekdays) {
            hashCode += element.hashCode();
        }
        // BEGIN android-added
        String[][] zoneStrings = internalZoneStrings();
        // END android-added
        for (String[] element : zoneStrings) {
            for (int j = 0; j < element.length; j++) {
                hashCode += element[j].hashCode();
            }
        }
        return hashCode;
    }

    /**
     * Sets the array of strings which represent AM and PM. Use the
     * {@link java.util.Calendar} constants {@code Calendar.AM} and
     * {@code Calendar.PM} as indices for the array.
     * 
     * @param data
     *            the array of strings for AM and PM.
     * @since Android 1.0
     */
    public void setAmPmStrings(String[] data) {
        ampms = data.clone();
    }

    /**
     * Sets the array of Strings which represent BC and AD. Use the
     * {@link java.util.Calendar} constants {@code GregorianCalendar.BC} and
     * {@code GregorianCalendar.AD} as indices for the array.
     * 
     * @param data
     *            the array of strings for BC and AD.
     * @since Android 1.0
     */
    public void setEras(String[] data) {
        eras = data.clone();
    }

    /**
     * Sets the pattern characters used by {@link SimpleDateFormat} to specify
     * date and time fields.
     * 
     * @param data
     *            the string containing the pattern characters.
     * @since Android 1.0
     */
    public void setLocalPatternChars(String data) {
        if (data == null) {
            throw new NullPointerException();
        }
        localPatternChars = data;
    }

    /**
     * Sets the array of strings containing the full names of the months. Use
     * the {@link java.util.Calendar} constants {@code Calendar.JANUARY} etc. as
     * indices for the array.
     * 
     * @param data
     *            the array of strings.
     * @since Android 1.0
     */
    public void setMonths(String[] data) {
        months = data.clone();
    }

    /**
     * Sets the array of strings containing the abbreviated names of the months.
     * Use the {@link java.util.Calendar} constants {@code Calendar.JANUARY}
     * etc. as indices for the array.
     * 
     * @param data
     *            the array of strings.
     * @since Android 1.0
     */
    public void setShortMonths(String[] data) {
        shortMonths = data.clone();
    }

    /**
     * Sets the array of strings containing the abbreviated names of the days of
     * the week. Use the {@link java.util.Calendar} constants
     * {@code Calendar.SUNDAY} etc. as indices for the array.
     * 
     * @param data
     *            the array of strings.
     * @since Android 1.0
     */
    public void setShortWeekdays(String[] data) {
        shortWeekdays = data.clone();
    }

    /**
     * Sets the array of strings containing the full names of the days of the
     * week. Use the {@link java.util.Calendar} constants
     * {@code Calendar.SUNDAY} etc. as indices for the array.
     * 
     * @param data
     *            the array of strings.
     * @since Android 1.0
     */
    public void setWeekdays(String[] data) {
        weekdays = data.clone();
    }

    /**
     * Sets the two-dimensional array of strings containing the names of the
     * time zones. Each element in the array is an array of five strings, the
     * first is a TimeZone ID, and second and third are the full and abbreviated
     * time zone names for standard time, and the fourth and fifth are the full
     * and abbreviated names for daylight time.
     * 
     * @param data
     *            the two-dimensional array of strings.
     * @since Android 1.0
     */
    public void setZoneStrings(String[][] data) {
        zoneStrings = data.clone();
    }

    // BEGIN android-added
    private void writeObject(ObjectOutputStream out)
                  throws IOException {
        // Ensure internal zone strings are initialized to ensure backward
        // compatibility.
        internalZoneStrings();

        out.defaultWriteObject();
    }
    // END android-added
}
