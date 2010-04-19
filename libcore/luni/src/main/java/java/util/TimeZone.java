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

import com.ibm.icu4jni.util.ICU;
import java.io.Serializable;
import org.apache.harmony.luni.internal.util.ZoneInfo;
import org.apache.harmony.luni.internal.util.ZoneInfoDB;

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
     * The short display name style, such as {@code PDT}. Requests for this
     * style may yield GMT offsets like {@code GMT-08:00}.
     */
    public static final int SHORT = 0;

    /**
     * The long display name style, such as {@code Pacific Daylight Time}.
     * Requests for this style may yield GMT offsets like {@code GMT-08:00}.
     */
    public static final int LONG = 1;

    static final TimeZone GMT = new SimpleTimeZone(0, "GMT"); // Greenwich Mean Time

    private static TimeZone defaultTimeZone;

    private String ID;

    public TimeZone() {}

    /**
     * Returns a new time zone with the same ID, raw offset, and daylight
     * savings time rules as this time zone.
     */
    @Override public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Returns the system's installed time zone IDs. Any of these IDs can be
     * passed to {@link #getTimeZone} to lookup the corresponding time zone
     * instance.
     */
    public static synchronized String[] getAvailableIDs() {
        return ZoneInfoDB.getAvailableIDs();
    }

    /**
     * Returns the IDs of the time zones whose offset from UTC is {@code
     * offsetMillis}. Any of these IDs can be passed to {@link #getTimeZone} to
     * lookup the corresponding time zone instance.
     *
     * @return a possibly-empty array.
     */
    public static synchronized String[] getAvailableIDs(int offsetMillis) {
        return ZoneInfoDB.getAvailableIDs(offsetMillis);
    }

    /**
     * Returns the user's preferred time zone. This may have been overridden for
     * this process with {@link #setDefault}.
     *
     * <p>Since the user's time zone changes dynamically, avoid caching this
     * value. Instead, use this method to look it up for each use.
     */
    public static synchronized TimeZone getDefault() {
        if (defaultTimeZone == null) {
            defaultTimeZone = ZoneInfoDB.getSystemDefault();
        }
        return (TimeZone) defaultTimeZone.clone();
    }

    /**
     * Equivalent to {@code getDisplayName(false, TimeZone.LONG, Locale.getDefault())}.
     * <a href="../util/Locale.html#default_locale">Be wary of the default locale</a>.
     */
    public final String getDisplayName() {
        return getDisplayName(false, LONG, Locale.getDefault());
    }

    /**
     * Equivalent to {@code getDisplayName(false, TimeZone.LONG, locale)}.
     */
    public final String getDisplayName(Locale locale) {
        return getDisplayName(false, LONG, locale);
    }

    /**
     * Equivalent to {@code getDisplayName(daylightTime, style, Locale.getDefault())}.
     * <a href="../util/Locale.html#default_locale">Be wary of the default locale</a>.
     */
    public final String getDisplayName(boolean daylightTime, int style) {
        return getDisplayName(daylightTime, style, Locale.getDefault());
    }

    /**
     * Returns the {@link #SHORT short} or {@link #LONG long} name of this time
     * zone with either standard or daylight time, as written in {@code locale}.
     * If the name is not available, the result is in the format
     * {@code GMT[+-]hh:mm}.
     *
     * @param daylightTime true for daylight time, false for standard time.
     * @param style either {@link TimeZone#LONG} or {@link TimeZone#SHORT}.
     * @param locale the display locale.
     */
    public String getDisplayName(boolean daylightTime, int style, Locale locale) {
        if (style != SHORT && style != LONG) {
            throw new IllegalArgumentException();
        }

        boolean useDaylight = daylightTime && useDaylightTime();

        String result = ICU.getDisplayTimeZone(getID(), daylightTime, style,
                locale.toString());
        if (result != null) {
            return result;
        }

        int offset = getRawOffset();
        if (useDaylight && this instanceof SimpleTimeZone) {
            offset += getDSTSavings();
        }
        offset /= 60000;
        char sign = '+';
        if (offset < 0) {
            sign = '-';
            offset = -offset;
        }
        StringBuilder builder = new StringBuilder(9);
        builder.append("GMT");
        builder.append(sign);
        appendNumber(builder, 2, offset / 60);
        builder.append(':');
        appendNumber(builder, 2, offset % 60);
        return builder.toString();
    }

    private void appendNumber(StringBuilder builder, int count, int value) {
        String string = Integer.toString(value);
        for (int i = 0; i < count - string.length(); i++) {
            builder.append('0');
        }
        builder.append(string);
    }

    /**
     * Returns the ID of this {@code TimeZone}, such as
     * {@code America/Los_Angeles}, {@code GMT-08:00} or {@code UTC}.
     */
    public String getID() {
        return ID;
    }

    /**
     * Returns the daylight savings offset in milliseconds for this time zone.
     * The base implementation returns {@code 3600000} (1 hour) for time zones
     * that use daylight savings time and {@code 0} for timezones that do not.
     * Subclasses should override this method for other daylight savings
     * offsets.
     *
     * <p>Note that this method doesn't tell you whether or not to apply the
     * offset: you need to call {@code inDaylightTime} for the specific time
     * you're interested in. If this method returns a non-zero offset, that only
     * tells you that this {@code TimeZone} sometimes observes daylight savings.
     */
    public int getDSTSavings() {
        return useDaylightTime() ? 3600000 : 0;
    }

    /**
     * Returns the offset in milliseconds from UTC for this time zone at {@code
     * time}. The offset includes daylight savings time if the specified
     * date is within the daylight savings time period.
     *
     * @param time the date in milliseconds since January 1, 1970 00:00:00 UTC
     */
    public int getOffset(long time) {
        if (inDaylightTime(new Date(time))) {
            return getRawOffset() + getDSTSavings();
        }
        return getRawOffset();
    }

    /**
     * Returns this time zone's offset in milliseconds from UTC at the specified
     * date and time. The offset includes daylight savings time if the date
     * and time is within the daylight savings time period.
     *
     * <p>This method is intended to be used by {@link Calendar} to compute
     * {@link Calendar#DST_OFFSET} and {@link Calendar#ZONE_OFFSET}. Application
     * code should have no reason to call this method directly. Each parameter
     * is interpreted in the same way as the corresponding {@code Calendar}
     * field. Refer to {@link Calendar} for specific definitions of this
     * method's parameters.
     */
    public abstract int getOffset(int era, int year, int month, int day,
            int dayOfWeek, int timeOfDayMillis);

    /**
     * Returns the offset in milliseconds from UTC of this time zone's standard
     * time.
     */
    public abstract int getRawOffset();

    /**
     * Returns a time zone whose ID is {@code id}. Time zone IDs are typically
     * named by geographic identifiers like {@code America/Los_Angeles} or GMT
     * offsets like {@code GMT-8:00}. Three letter IDs like {@code PST} are
     * supported but should not be used because they is often ambiguous.
     *
     * @return a time zone with the specified ID, or {@code GMT} if the ID
     *     is not recognized and cannot be parsed.
     */
    public static synchronized TimeZone getTimeZone(String id) {
        TimeZone zone = ZoneInfo.getTimeZone(id);
        if (zone != null) {
            return (TimeZone) zone.clone();
        }

        if (!id.startsWith("GMT") || id.length() <= 3) {
            return (TimeZone) GMT.clone();
        }
        char sign = id.charAt(3);
        if (sign != '+' && sign != '-') {
            return (TimeZone) GMT.clone();
        }
        int[] position = new int[1];
        String formattedName = formatTimeZoneName(id, 4);
        int hour = parseNumber(formattedName, 4, position);
        if (hour < 0 || hour > 23) {
            return (TimeZone) GMT.clone();
        }
        int index = position[0];
        if (index == -1) {
            return (TimeZone) GMT.clone();
        }
        int raw = hour * 3600000;
        if (index < formattedName.length() && formattedName.charAt(index) == ':') {
            int minute = parseNumber(formattedName, index + 1, position);
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
     * Returns true if {@code timeZone} has the same rules as this time zone.
     *
     * <p>The base implementation returns true if both time zones have the same
     * raw offset.
     */
    public boolean hasSameRules(TimeZone timeZone) {
        if (timeZone == null) {
            return false;
        }
        return getRawOffset() == timeZone.getRawOffset();
    }

    /**
     * Returns true if {@code time} is in a daylight savings time period for
     * this time zone.
     */
    public abstract boolean inDaylightTime(Date time);

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
     * Overrides the default time zone for the current process only.
     *
     * <p><strong>Warning</strong>: avoid using this method to use a custom time
     * zone in your process. This value may be cleared or overwritten at any
     * time, which can cause unexpected behavior. Instead, manually supply a
     * custom time zone as needed.
     *
     * @param timeZone a custom time zone, or {@code null} to set the default to
     *     the user's preferred value.
     */
    public static synchronized void setDefault(TimeZone timeZone) {
        defaultTimeZone = timeZone != null ? (TimeZone) timeZone.clone() : null;
    }

    /**
     * Sets the ID of this {@code TimeZone}.
     */
    public void setID(String id) {
        if (id == null) {
            throw new NullPointerException();
        }
        ID = id;
    }

    /**
     * Sets the offset in milliseconds from UTC of this time zone's standard
     * time.
     */
    public abstract void setRawOffset(int offsetMillis);

    /**
     * Returns true if this time zone has a daylight savings time period. More
     * specifically, this method returns true to indicate that there's at least
     * one known future transition to or from daylight savings. This means that,
     * say, Taiwan will return false because its historical use of daylight
     * savings doesn't count. A hypothetical country that has never observed
     * daylight savings before but plans to start next year would return true.
     *
     * <p>If this method returns true, that only tells you that this
     * {@code TimeZone} sometimes observes daylight savings. You need to call
     * {@code inDaylightTime} to find out whether daylight savings is in effect.
     */
    public abstract boolean useDaylightTime();
}
