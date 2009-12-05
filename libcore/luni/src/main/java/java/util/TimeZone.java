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

import java.io.Serializable;

// BEGIN android-added
import org.apache.harmony.luni.internal.util.ZoneInfo;
import org.apache.harmony.luni.internal.util.ZoneInfoDB;
import com.ibm.icu4jni.util.Resources;
// END android-added

/**
 * {@code TimeZone} represents a time zone offset, taking into account
 * daylight savings.
 * <p>
 * Typically, you get a {@code TimeZone} using {@code getDefault}
 * which creates a {@code TimeZone} based on the time zone where the
 * program is running. For example, for a program running in Japan,
 * {@code getDefault} creates a {@code TimeZone} object based on
 * Japanese Standard Time.
 * <p>
 * You can also get a {@code TimeZone} using {@code getTimeZone}
 * along with a time zone ID. For instance, the time zone ID for the U.S.
 * Pacific Time zone is "America/Los_Angeles". So, you can get a U.S. Pacific
 * Time {@code TimeZone} object with the following: <blockquote>
 *
 * <pre>
 * TimeZone tz = TimeZone.getTimeZone(&quot;America/Los_Angeles&quot;);
 * </pre>
 *
 * </blockquote> You can use the {@code getAvailableIDs} method to iterate
 * through all the supported time zone IDs. You can then choose a supported ID
 * to get a {@code TimeZone}. If the time zone you want is not
 * represented by one of the supported IDs, then you can create a custom time
 * zone ID with the following syntax: <blockquote>
 *
 * <pre>
 * GMT[+|-]hh[[:]mm]
 * </pre>
 *
 * </blockquote> For example, you might specify GMT+14:00 as a custom time zone
 * ID. The {@code TimeZone} that is returned when you specify a custom
 * time zone ID does not include daylight savings time.
 * <p>
 * For compatibility with JDK 1.1.x, some other three-letter time zone IDs (such
 * as "PST", "CTT", "AST") are also supported. However, <strong>their use is
 * deprecated</strong> because the same abbreviation is often used for multiple
 * time zones (for example, "CST" could be U.S. "Central Standard Time" and
 * "China Standard Time"), and the Java platform can then only recognize one of
 * them.
 * <p>
 * Please note the type returned by factory methods, i.e. {@code getDefault()}
 * and {@code getTimeZone(String)}, is implementation dependent, so it may
 * introduce serialization incompatibility issues between different
 * implementations. Android returns instances of {@link SimpleTimeZone} so that
 * the bytes serialized by Android can be deserialized successfully on other
 * implementations, but the reverse compatibility cannot be guaranteed.
 *
 * @see GregorianCalendar
 * @see SimpleTimeZone
 */
public abstract class TimeZone implements Serializable, Cloneable {
    private static final long serialVersionUID = 3581463369166924961L;

    /**
     * The SHORT display name style.
     */
    public static final int SHORT = 0;

    /**
     * The LONG display name style.
     */
    public static final int LONG = 1;

    // BEGIN android-removed
    // private static HashMap<String, TimeZone> AvailableZones;
    // END android-removed

    private static TimeZone Default;

    static TimeZone GMT = new SimpleTimeZone(0, "GMT"); // Greenwich Mean Time

    private String ID;

    // BEGIN android-removed
    // private com.ibm.icu.util.TimeZone icuTimeZone = null;
    //
    // private static void initializeAvailable() {
    //     TimeZone[] zones = TimeZones.getTimeZones();
    //     AvailableZones = new HashMap<String, TimeZone>(
    //             (zones.length + 1) * 4 / 3);
    //     AvailableZones.put(GMT.getID(), GMT);
    //     for (int i = 0; i < zones.length; i++) {
    //         AvailableZones.put(zones[i].getID(), zones[i]);
    //     }
    //}
    //
    // private static boolean isAvailableIDInICU(String name) {
    //     String[] availableIDs = com.ibm.icu.util.TimeZone.getAvailableIDs();
    //     for (int i = 0; i < availableIDs.length; i++) {
    //         if (availableIDs[i].equals(name)) {
    //             return true;
    //         }
    //     }
    //     return false;
    // }
    //
    // private static void appendAvailableZones(String name) {
    //     com.ibm.icu.util.TimeZone icuTZ = com.ibm.icu.util.TimeZone
    //             .getTimeZone(name);
    //     int raw = icuTZ.getRawOffset();
    //     TimeZone zone = new SimpleTimeZone(raw, name);
    //     AvailableZones.put(name, zone);
    // }
    // END android-removed

    /**
     * Constructs a new instance of this class.
     */
    public TimeZone() {
    }

    /**
     * Returns a new {@code TimeZone} with the same ID, {@code rawOffset} and daylight savings
     * time rules as this {@code TimeZone}.
     *
     * @return a shallow copy of this {@code TimeZone}.
     * @see java.lang.Cloneable
     */
    @Override
    public Object clone() {
        try {
            TimeZone zone = (TimeZone) super.clone();
            return zone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e); // android-changed
        }
    }

    /**
     * Gets the available time zone IDs. Any one of these IDs can be passed to
     * {@code get()} to create the corresponding {@code TimeZone} instance.
     *
     * @return an array of time zone ID strings.
     */
    public static synchronized String[] getAvailableIDs() {
        // BEGIN android-changed
        // return com.ibm.icu.util.TimeZone.getAvailableIDs();
        return ZoneInfoDB.getAvailableIDs();
        // END android-changed
    }

    /**
     * Gets the available time zone IDs which match the specified offset from
     * GMT. Any one of these IDs can be passed to {@code get()} to create the corresponding
     * {@code TimeZone} instance.
     *
     * @param offset
     *            the offset from GMT in milliseconds.
     * @return an array of time zone ID strings.
     */
    public static synchronized String[] getAvailableIDs(int offset) {
        // BEGIN android-changed
        // String[] availableIDs = com.ibm.icu.util.TimeZone.getAvailableIDs();
        // int count = 0;
        // int length = availableIDs.length;
        // String[] all = new String[length];
        // for (int i = 0; i < length; i++) {
        //     com.ibm.icu.util.TimeZone tz = com.ibm.icu.util.TimeZone
        //             .getTimeZone(availableIDs[i]);
        //     if (tz.getRawOffset() == offset) {
        //         all[count++] = tz.getID();
        //     }
        // }
        // String[] answer = new String[count];
        // System.arraycopy(all, 0, answer, 0, count);
        // return answer;
        return ZoneInfoDB.getAvailableIDs(offset);
        // END android-changed
    }

    /**
     * Gets the default time zone.
     *
     * @return the default time zone.
     */
    public static synchronized TimeZone getDefault() {
        if (Default == null) {
            // BEGIN android-changed
            // setDefault(null);
            Default = ZoneInfoDB.getDefault();
            // END android-changed
        }
        return (TimeZone) Default.clone();
    }

    /**
     * Gets the LONG name for this {@code TimeZone} for the default {@code Locale} in standard
     * time. If the name is not available, the result is in the format
     * {@code GMT[+-]hh:mm}.
     *
     * @return the {@code TimeZone} name.
     */
    public final String getDisplayName() {
        return getDisplayName(false, LONG, Locale.getDefault());
    }

    /**
     * Gets the LONG name for this {@code TimeZone} for the specified {@code Locale} in standard
     * time. If the name is not available, the result is in the format
     * {@code GMT[+-]hh:mm}.
     *
     * @param locale
     *            the {@code Locale}.
     * @return the {@code TimeZone} name.
     */
    public final String getDisplayName(Locale locale) {
        return getDisplayName(false, LONG, locale);
    }

    /**
     * Gets the specified style of name ({@code LONG} or {@code SHORT}) for this {@code TimeZone} for
     * the default {@code Locale} in either standard or daylight time as specified. If
     * the name is not available, the result is in the format {@code GMT[+-]hh:mm}.
     *
     * @param daylightTime
     *            {@code true} for daylight time, {@code false} for standard
     *            time.
     * @param style
     *            either {@code LONG} or {@code SHORT}.
     * @return the {@code TimeZone} name.
     */
    public final String getDisplayName(boolean daylightTime, int style) {
        return getDisplayName(daylightTime, style, Locale.getDefault());
    }

    /**
     * Gets the specified style of name ({@code LONG} or {@code SHORT}) for this {@code TimeZone} for
     * the specified {@code Locale} in either standard or daylight time as specified. If
     * the name is not available, the result is in the format {@code GMT[+-]hh:mm}.
     *
     * @param daylightTime
     *            {@code true} for daylight time, {@code false} for standard
     *            time.
     * @param style
     *            either LONG or SHORT.
     * @param locale
     *            either {@code LONG} or {@code SHORT}.
     * @return the {@code TimeZone} name.
     */
    public String getDisplayName(boolean daylightTime, int style, Locale locale) {
        // BEGIN android-changed
        // if(icuTimeZone == null || !ID.equals(icuTimeZone.getID())){
        //     icuTimeZone = com.ibm.icu.util.TimeZone.getTimeZone(ID);
        // }
        // return icuTimeZone.getDisplayName(
        //         daylightTime, style, locale);
        if (style == SHORT || style == LONG) {
            boolean useDaylight = daylightTime && useDaylightTime();

            String result = Resources.getDisplayTimeZone(getID(), daylightTime, style, locale.toString());
            if (result != null) {
                return result;
            }

            int offset = getRawOffset();
            if (useDaylight && this instanceof SimpleTimeZone) {
                offset += ((SimpleTimeZone) this).getDSTSavings();
            }
            offset /= 60000;
            char sign = '+';
            if (offset < 0) {
                sign = '-';
                offset = -offset;
            }
            StringBuffer buffer = new StringBuffer(9);
            buffer.append("GMT");
            buffer.append(sign);
            appendNumber(buffer, 2, offset / 60);
            buffer.append(':');
            appendNumber(buffer, 2, offset % 60);
            return buffer.toString();
        }
        throw new IllegalArgumentException();
        // END android-changed
    }

    // BEGIN android-added
    private void appendNumber(StringBuffer buffer, int count, int value) {
        String string = Integer.toString(value);
        if (count > string.length()) {
            for (int i = 0; i < count - string.length(); i++) {
                buffer.append('0');
            }
        }
        buffer.append(string);
    }
    // END android-added

    /**
     * Gets the ID of this {@code TimeZone}.
     *
     * @return the time zone ID string.
     */
    public String getID() {
        return ID;
    }

    /**
     * Gets the daylight savings offset in milliseconds for this {@code TimeZone}.
     * <p>
     * This implementation returns 3600000 (1 hour), or 0 if the time zone does
     * not observe daylight savings.
     * <p>
     * Subclasses may override to return daylight savings values other than 1
     * hour.
     * <p>
     * Note that this method doesn't tell you whether or not to apply the offset: you
     * need to call {@code inDaylightTime} for the specific time you're interested in.
     * If this method returns a non-zero offset, that only tells you that this
     * {@code TimeZone} sometimes observes daylight savings.
     *
     * @return the daylight savings offset in milliseconds if this {@code TimeZone}
     *         observes daylight savings, zero otherwise.
     */
    public int getDSTSavings() {
        if (useDaylightTime()) {
            return 3600000;
        }
        return 0;
    }

    /**
     * Gets the offset from GMT of this {@code TimeZone} for the specified date. The
     * offset includes daylight savings time if the specified date is within the
     * daylight savings time period.
     *
     * @param time
     *            the date in milliseconds since January 1, 1970 00:00:00 GMT
     * @return the offset from GMT in milliseconds.
     */
    public int getOffset(long time) {
        if (inDaylightTime(new Date(time))) {
            return getRawOffset() + getDSTSavings();
        }
        return getRawOffset();
    }

    /**
     * Gets the offset from GMT of this {@code TimeZone} for the specified date and
     * time. The offset includes daylight savings time if the specified date and
     * time are within the daylight savings time period.
     *
     * @param era
     *            the {@code GregorianCalendar} era, either {@code GregorianCalendar.BC} or
     *            {@code GregorianCalendar.AD}.
     * @param year
     *            the year.
     * @param month
     *            the {@code Calendar} month.
     * @param day
     *            the day of the month.
     * @param dayOfWeek
     *            the {@code Calendar} day of the week.
     * @param time
     *            the time of day in milliseconds.
     * @return the offset from GMT in milliseconds.
     */
    abstract public int getOffset(int era, int year, int month, int day,
            int dayOfWeek, int time);

    /**
     * Gets the offset for standard time from GMT for this {@code TimeZone}.
     *
     * @return the offset from GMT in milliseconds.
     */
    abstract public int getRawOffset();

    /**
     * Gets the {@code TimeZone} with the specified ID.
     *
     * @param name
     *            a time zone string ID.
     * @return the {@code TimeZone} with the specified ID or null if no {@code TimeZone} with
     *         the specified ID exists.
     */
    public static synchronized TimeZone getTimeZone(String name) {
        // BEGIN android-changed
        // if (AvailableZones == null) {
        //     initializeAvailable();
        // }
        //
        // TimeZone zone = AvailableZones.get(name);
        // if(zone == null && isAvailableIDInICU(name)){
        //     appendAvailableZones(name);
        //     zone = AvailableZones.get(name);
        // }
        TimeZone zone = ZoneInfo.getTimeZone(name);
        // END android-changed
        if (zone == null) {
            if (name.startsWith("GMT") && name.length() > 3) {
                char sign = name.charAt(3);
                if (sign == '+' || sign == '-') {
                    int[] position = new int[1];
                    String formattedName = formatTimeZoneName(name, 4);
                    int hour = parseNumber(formattedName, 4, position);
                    if (hour < 0 || hour > 23) {
                        return (TimeZone) GMT.clone();
                    }
                    int index = position[0];
                    if (index != -1) {
                        int raw = hour * 3600000;
                        if (index < formattedName.length()
                                && formattedName.charAt(index) == ':') {
                            int minute = parseNumber(formattedName, index + 1,
                                    position);
                            if (position[0] == -1 || minute < 0 || minute > 59) {
                                return (TimeZone) GMT.clone();
                            }
                            raw += minute * 60000;
                        } else if (hour >= 30 || index > 6) {
                            raw = (hour / 100 * 3600000) + (hour % 100 * 60000);
                        }
                        if (sign == '-') {
                            raw = -raw;
                        }
                        return new SimpleTimeZone(raw, formattedName);
                    }
                }
            }
            zone = GMT;
        }
        return (TimeZone) zone.clone();
    }

    private static String formatTimeZoneName(String name, int offset) {
        StringBuilder buf = new StringBuilder();
        int index = offset, length = name.length();
        buf.append(name.substring(0, offset));

        while (index < length) {
            if (Character.digit(name.charAt(index), 10) != -1) {
                buf.append(name.charAt(index));
                if ((length - (index + 1)) == 2) {
                    buf.append(':');
                }
            } else if (name.charAt(index) == ':') {
                buf.append(':');
            }
            index++;
        }

        if (buf.toString().indexOf(":") == -1) {
            buf.append(':');
            buf.append("00");
        }

        if (buf.toString().indexOf(":") == 5) {
            buf.insert(4, '0');
        }

        return buf.toString();
    }

    /**
     * Returns whether the specified {@code TimeZone} has the same raw offset as this
     * {@code TimeZone}.
     *
     * @param zone
     *            a {@code TimeZone}.
     * @return {@code true} when the {@code TimeZone} have the same raw offset, {@code false}
     *         otherwise.
     */
    public boolean hasSameRules(TimeZone zone) {
        if (zone == null) {
            return false;
        }
        return getRawOffset() == zone.getRawOffset();
    }

    /**
     * Returns whether the specified {@code Date} is in the daylight savings time period for
     * this {@code TimeZone}.
     *
     * @param time
     *            a {@code Date}.
     * @return {@code true} when the {@code Date} is in the daylight savings time period, {@code false}
     *         otherwise.
     */
    abstract public boolean inDaylightTime(Date time);

    private static int parseNumber(String string, int offset, int[] position) {
        int index = offset, length = string.length(), digit, result = 0;
        while (index < length
                && (digit = Character.digit(string.charAt(index), 10)) != -1) {
            index++;
            result = result * 10 + digit;
        }
        position[0] = index == offset ? -1 : index;
        return result;
    }

    /**
     * Sets the default time zone. If passed {@code null}, then the next
     * time {@link #getDefault} is called, the default time zone will be
     * determined. This behavior is slightly different than the canonical
     * description of this method, but it follows the spirit of it.
     *
     * @param timezone
     *            a {@code TimeZone} object.
     */
    public static synchronized void setDefault(TimeZone timezone) {
        // BEGIN android-removed
        // if (timezone != null) {
        //     setICUDefaultTimeZone(timezone);
        //     Default = timezone;
        //     return;
        // }
        //
        // String zone = AccessController.doPrivileged(new PriviAction<String>(
        //         "user.timezone"));
        //
        // // sometimes DRLVM incorrectly adds "\n" to the end of timezone ID
        // if (zone != null && zone.contains("\n")) {
        //     zone = zone.substring(0, zone.indexOf("\n"));
        // }
        //
        // // if property user.timezone is not set, we call the native method
        // // getCustomTimeZone
        // if (zone == null || zone.length() == 0) {
        //     int[] tzinfo = new int[10];
        //     boolean[] isCustomTimeZone = new boolean[1];
        //
        //     String zoneId = getCustomTimeZone(tzinfo, isCustomTimeZone);
        //
        //     // if returned TimeZone is a user customized TimeZone
        //     if (isCustomTimeZone[0]) {
        //         // build a new SimpleTimeZone
        //         switch (tzinfo[1]) {
        //         case 0:
        //             // does not observe DST
        //             Default = new SimpleTimeZone(tzinfo[0], zoneId);
        //             break;
        //         default:
        //             // observes DST
        //             Default = new SimpleTimeZone(tzinfo[0], zoneId, tzinfo[5],
        //                     tzinfo[4], tzinfo[3], tzinfo[2], tzinfo[9],
        //                     tzinfo[8], tzinfo[7], tzinfo[6], tzinfo[1]);
        //         }
        //     } else {
        //         // get TimeZone
        //         Default = getTimeZone(zoneId);
        //     }
        // } else {
        //     // if property user.timezone is set in command line (with -D option)
        //     Default = getTimeZone(zone);
        // }
        // setICUDefaultTimeZone(Default);
        // END android-removed

        // BEGIN android-added
        Default = timezone;

        // TODO Not sure if this is spec-compliant. Shouldn't be persistent.
        ZoneInfoDB.setDefault(timezone);
        // END android-added
    }

    // BEGIN android-removed
    // private static void setICUDefaultTimeZone(TimeZone timezone) {
    //     final com.ibm.icu.util.TimeZone icuTZ = com.ibm.icu.util.TimeZone
    //             .getTimeZone(timezone.getID());
    //
    //     AccessController
    //             .doPrivileged(new PrivilegedAction<java.lang.reflect.Field>() {
    //                 public java.lang.reflect.Field run() {
    //                     java.lang.reflect.Field field = null;
    //                     try {
    //                         field = com.ibm.icu.util.TimeZone.class
    //                                 .getDeclaredField("defaultZone");
    //                         field.setAccessible(true);
    //                         field.set("defaultZone", icuTZ);
    //                     } catch (Exception e) {
    //                         return null;
    //                     }
    //                     return field;
    //                 }
    //             });
    // }
    // END android-removed

    /**
     * Sets the ID of this {@code TimeZone}.
     *
     * @param name
     *            a string which is the time zone ID.
     */
    public void setID(String name) {
        if (name == null) {
            throw new NullPointerException();
        }
        ID = name;
    }

    /**
     * Sets the offset for standard time from GMT for this {@code TimeZone}.
     *
     * @param offset
     *            the offset from GMT in milliseconds.
     */
    abstract public void setRawOffset(int offset);

    /**
     * Returns whether this {@code TimeZone} has a daylight savings time period.
     * 
     * <p>If this method returns true, that only tells you that this
     * {@code TimeZone} sometimes observes daylight savings. You need to call
     * {@code inDaylightTime} to find out whether daylight savings is in effect.
     *
     * <p>More specifically, this method returns true to indicate that there's
     * at least one known future transition to or from daylight savings. This
     * means that, say, Taiwan will return false because its historical use of
     * daylight savings doesn't count. A hypothetical country that has never
     * observed daylight savings before but plans to start next year would return
     * true.
     * 
     * @return {@code true} if this {@code TimeZone} has a daylight savings time period, {@code false}
     *         otherwise.
     */
    abstract public boolean useDaylightTime();

    /**
     * Gets the name and the details of the user-selected TimeZone on the
     * device.
     *
     * @param tzinfo
     *            int array of 10 elements to be filled with the TimeZone
     *            information. Once filled, the contents of the array are
     *            formatted as follows: tzinfo[0] -> the timezone offset;
     *            tzinfo[1] -> the dst adjustment; tzinfo[2] -> the dst start
     *            hour; tzinfo[3] -> the dst start day of week; tzinfo[4] -> the
     *            dst start week of month; tzinfo[5] -> the dst start month;
     *            tzinfo[6] -> the dst end hour; tzinfo[7] -> the dst end day of
     *            week; tzinfo[8] -> the dst end week of month; tzinfo[9] -> the
     *            dst end month;
     * @param isCustomTimeZone
     *            boolean array of size 1 that indicates if a timezone match is
     *            found
     * @return the name of the TimeZone or null if error occurs in native
     *         method.
     */
    private static native String getCustomTimeZone(int[] tzinfo,
            boolean[] isCustomTimeZone);
}
