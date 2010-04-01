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

import com.ibm.icu4jni.util.LocaleData;
import com.ibm.icu4jni.util.Resources;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Locale;

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
 * <p>
 * If you decide to create a date/time formatter with a specific format pattern
 * for a specific locale, you can do so with:
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
 * <p>
 * New {@code DateFormatSymbols} subclasses may be added to support
 * {@code SimpleDateFormat} for date/time formatting for additional locales.
 *
 * @see DateFormat
 * @see SimpleDateFormat
 */
public class DateFormatSymbols implements Serializable, Cloneable {

    private static final long serialVersionUID = -5987973545549424702L;

    private String localPatternChars;

    String[] ampms, eras, months, shortMonths, shortWeekdays, weekdays;

    // Localized display names.
    String[][] zoneStrings;
    // Has the user called setZoneStrings?
    transient boolean customZoneStrings;

    // BEGIN android-removed
    // transient private com.ibm.icu4jni.text.DateFormatSymbols icuSymbols;
    // END android-removed

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
     */
    public DateFormatSymbols(Locale locale) {
        this.locale = locale;
        // BEGIN android-changed
        this.localPatternChars = SimpleDateFormat.patternChars;
        LocaleData localeData = com.ibm.icu4jni.util.Resources.getLocaleData(locale);
        this.ampms = localeData.amPm;
        this.eras = localeData.eras;
        this.months = localeData.longMonthNames;
        this.shortMonths = localeData.shortMonthNames;
        this.weekdays = localeData.longWeekdayNames;
        this.shortWeekdays = localeData.shortWeekdayNames;
        // END android-changed
    }

    /**
     * Returns a new {@code DateFormatSymbols} instance for the default locale.
     *
     * @return an instance of {@code DateFormatSymbols}
     * @since 1.6
     * @hide
     */
    public static final DateFormatSymbols getInstance() {
        return getInstance(Locale.getDefault());
    }

    /**
     * Returns a new {@code DateFormatSymbols} for the given locale.
     *
     * @param locale the locale
     * @return an instance of {@code DateFormatSymbols}
     * @throws NullPointerException if {@code locale == null}
     * @since 1.6
     * @hide
     */
    public static final DateFormatSymbols getInstance(Locale locale) {
        if (locale == null) {
            throw new NullPointerException();
        }
        return new DateFormatSymbols(locale);
    }

    /**
     * Returns an array of locales for which custom {@code DateFormatSymbols} instances
     * are available.
     * @since 1.6
     * @hide
     */
    public static Locale[] getAvailableLocales() {
        return Resources.getAvailableDateFormatSymbolsLocales();
    }

    private void writeObject(ObjectOutputStream oos) throws IOException {
        // BEGIN android-changed
        internalZoneStrings();
        // END android-changed
        oos.defaultWriteObject();
    }

    // BEGIN android-removed
    // DateFormatSymbols(Locale locale,
    //         com.ibm.icu4jni.text.DateFormatSymbols icuSymbols) {
    //
    //     this.icuSymbols = icuSymbols;
    //     localPatternChars = icuSymbols.getLocalPatternChars();
    //     ampms = icuSymbols.getAmPmStrings();
    //     eras = icuSymbols.getEras();
    //     months = icuSymbols.getMonths();
    //     shortMonths = icuSymbols.getShortMonths();
    //     shortWeekdays = icuSymbols.getShortWeekdays();
    //     weekdays = icuSymbols.getWeekdays();
    // }
    // END android-removed

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
     */
    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof DateFormatSymbols)) {
            return false;
        }
        DateFormatSymbols rhs = (DateFormatSymbols) object;
        return localPatternChars.equals(rhs.localPatternChars) &&
                Arrays.equals(ampms, rhs.ampms) &&
                Arrays.equals(eras, rhs.eras) &&
                Arrays.equals(months, rhs.months) &&
                Arrays.equals(shortMonths, rhs.shortMonths) &&
                Arrays.equals(shortWeekdays, rhs.shortWeekdays) &&
                Arrays.equals(weekdays, rhs.weekdays) &&
                timeZoneStringsEqual(this, rhs);
    }

    private static boolean timeZoneStringsEqual(DateFormatSymbols lhs, DateFormatSymbols rhs) {
        // Quick check that may keep us from having to load the zone strings.
        // Note that different locales may have the same strings, so the opposite check isn't valid.
        if (lhs.zoneStrings == null && rhs.zoneStrings == null && lhs.locale.equals(rhs.locale)) {
            return true;
        }
        // Make sure zone strings are loaded, then check.
        return Arrays.deepEquals(lhs.internalZoneStrings(), rhs.internalZoneStrings());
    }

    @Override
    public String toString() {
        // 'locale' isn't part of the externally-visible state.
        // 'zoneStrings' is so large, we just print a representative value.
        return getClass().getName() +
                "[amPmStrings=" + Arrays.toString(ampms) +
                ",customZoneStrings=" + customZoneStrings +
                ",eras=" + Arrays.toString(eras) +
                ",localPatternChars=" + new String(localPatternChars) +
                ",months=" + Arrays.toString(months) +
                ",shortMonths=" + Arrays.toString(shortMonths) +
                ",shortWeekdays=" + Arrays.toString(shortWeekdays) +
                ",weekdays=" + Arrays.toString(weekdays) +
                ",zoneStrings=[" + Arrays.toString(internalZoneStrings()[0]) + "...]" +
                "]";
    }

    /**
     * Returns the array of strings which represent AM and PM. Use the
     * {@link java.util.Calendar} constants {@code Calendar.AM} and
     * {@code Calendar.PM} as indices for the array.
     * 
     * @return an array of strings.
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
     */
    public String[] getEras() {
        return eras.clone();
    }

    /**
     * Returns the pattern characters used by {@link SimpleDateFormat} to
     * specify date and time fields.
     * 
     * @return a string containing the pattern characters.
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
     */
    public String[][] getZoneStrings() {
        // BEGIN android-changed
        return Resources.clone2dStringArray(internalZoneStrings());
        // END android-changed
    }

    @Override
    public int hashCode() {
        // BEGIN android-changed
        String[][] zoneStrings = internalZoneStrings();
        // END android-changed
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
        for (String[] element : zoneStrings) {
            for (int j = 0; j < element.length; j++) {
                if (element[j] != null) {
                    hashCode += element[j].hashCode();
                }
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
     * @throws NullPointerException
     *            if {@code data} is null
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
     */
    public void setZoneStrings(String[][] data) {
        zoneStrings = Resources.clone2dStringArray(data);
        customZoneStrings = true;
    }
}
