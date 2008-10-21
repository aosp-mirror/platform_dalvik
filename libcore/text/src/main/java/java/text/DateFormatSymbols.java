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
 * DateFormatSymbols holds the Strings used in the formating and parsing of
 * dates and times.
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
     * Constructs a new DateFormatSymbols containing the symbols for the default
     * Locale.
     */
    public DateFormatSymbols() {
        this(Locale.getDefault());
    }

    /**
     * Constructs a new DateFormatSymbols containing the symbols for the
     * specified Locale.
     * 
     * @param locale
     *            the Locale
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
    }

    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
// END android-changed

    /**
     * Compares the specified object to this DateFormatSymbols and answer if
     * they are equal. The object must be an instance of DateFormatSymbols with
     * the same symbols.
     * 
     * @param object
     *            the object to compare with this object
     * @return true if the specified object is equal to this DateFormatSymbols,
     *         false otherwise
     * 
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
     * Returns the array of Strings which represent AM and PM. Use the Calendar
     * constants Calendar.AM and Calendar.PM to index into the array.
     * 
     * @return an array of String
     */
    public String[] getAmPmStrings() {
        return ampms.clone();
    }

    /**
     * Returns the array of Strings which represent BC and AD. Use the Calendar
     * constants GregorianCalendar.BC and GregorianCalendar.AD to index into the
     * array.
     * 
     * @return an array of String
     */
    public String[] getEras() {
        return eras.clone();
    }

    /**
     * Returns the pattern characters used by SimpleDateFormat to specify date
     * and time fields.
     * 
     * @return a String containing the pattern characters
     */
    public String getLocalPatternChars() {
        return localPatternChars;
    }

    /**
     * Returns the array of Strings containing the full names of the months. Use
     * the Calendar constants Calendar.JANUARY, etc. to index into the array.
     * 
     * @return an array of String
     */
    public String[] getMonths() {
        return months.clone();
    }

    /**
     * Returns the array of Strings containing the abbreviated names of the
     * months. Use the Calendar constants Calendar.JANUARY, etc. to index into
     * the array.
     * 
     * @return an array of String
     */
    public String[] getShortMonths() {
        return shortMonths.clone();
    }

    /**
     * Returns the array of Strings containing the abbreviated names of the days
     * of the week. Use the Calendar constants Calendar.SUNDAY, etc. to index
     * into the array.
     * 
     * @return an array of String
     */
    public String[] getShortWeekdays() {
        return shortWeekdays.clone();
    }

    /**
     * Returns the array of Strings containing the full names of the days of the
     * week. Use the Calendar constants Calendar.SUNDAY, etc. to index into the
     * array.
     * 
     * @return an array of String
     */
    public String[] getWeekdays() {
        return weekdays.clone();
    }

    /**
     * Returns the two-dimensional array of Strings containing the names of the
     * timezones. Each element in the array is an array of five Strings, the
     * first is a TimeZone ID, and second and third are the full and abbreviated
     * timezone names for standard time, and the fourth and fifth are the full
     * and abbreviated names for daylight time.
     * 
     * @return a two-dimensional array of String
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
     * Sets the array of Strings which represent AM and PM. Use the Calendar
     * constants Calendar.AM and Calendar.PM to index into the array.
     * 
     * @param data
     *            the array of Strings
     */
    public void setAmPmStrings(String[] data) {
        ampms = data.clone();
    }

    /**
     * Sets the array of Strings which represent BC and AD. Use the Calendar
     * constants GregorianCalendar.BC and GregorianCalendar.AD to index into the
     * array.
     * 
     * @param data
     *            the array of Strings
     */
    public void setEras(String[] data) {
        eras = data.clone();
    }

    /**
     * Sets the pattern characters used by SimpleDateFormat to specify date and
     * time fields.
     * 
     * @param data
     *            the String containing the pattern characters
     * 
     * @exception NullPointerException
     *                when the data is null
     */
    public void setLocalPatternChars(String data) {
        if (data == null) {
            throw new NullPointerException();
        }
        localPatternChars = data;
    }

    /**
     * Sets the array of Strings containing the full names of the months. Use
     * the Calendar constants Calendar.JANUARY, etc. to index into the array.
     * 
     * @param data
     *            the array of Strings
     */
    public void setMonths(String[] data) {
        months = data.clone();
    }

    /**
     * Sets the array of Strings containing the abbreviated names of the months.
     * Use the Calendar constants Calendar.JANUARY, etc. to index into the
     * array.
     * 
     * @param data
     *            the array of Strings
     */
    public void setShortMonths(String[] data) {
        shortMonths = data.clone();
    }

    /**
     * Sets the array of Strings containing the abbreviated names of the days of
     * the week. Use the Calendar constants Calendar.SUNDAY, etc. to index into
     * the array.
     * 
     * @param data
     *            the array of Strings
     */
    public void setShortWeekdays(String[] data) {
        shortWeekdays = data.clone();
    }

    /**
     * Sets the array of Strings containing the full names of the days of the
     * week. Use the Calendar constants Calendar.SUNDAY, etc. to index into the
     * array.
     * 
     * @param data
     *            the array of Strings
     */
    public void setWeekdays(String[] data) {
        weekdays = data.clone();
    }

    /**
     * Sets the two-dimensional array of Strings containing the names of the
     * timezones. Each element in the array is an array of five Strings, the
     * first is a TimeZone ID, and second and third are the full and abbreviated
     * timezone names for standard time, and the fourth and fifth are the full
     * and abbreviated names for daylight time.
     * 
     * @param data
     *            the two-dimensional array of Strings
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
