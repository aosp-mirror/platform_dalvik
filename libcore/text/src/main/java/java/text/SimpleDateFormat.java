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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamField;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.SimpleTimeZone;
import java.util.TimeZone;
import java.util.Vector;

import org.apache.harmony.text.internal.nls.Messages;

/**
 * SimpleDateFormat is used to format and parse Gregorian calendar dates and
 * times based on a pattern of date and time fields. Each date and time field is
 * specified in the pattern by a specific character. The characters used can be
 * either localized or non-localized. For some fields, which have both numeric
 * and text representations or abbreviated as well as full names, the number of
 * grouped characters specifies how the field is formatted or parsed.
 */
public class SimpleDateFormat extends DateFormat {

    private static final long serialVersionUID = 4774881970558875024L;

    private static final String patternChars = "GyMdkHmsSEDFwWahKzZ"; //$NON-NLS-1$

    private String pattern;

    private DateFormatSymbols formatData;

    transient private int creationYear;

    private Date defaultCenturyStart;

    /**
     * Constructs a new SimpleDateFormat for formatting and parsing dates and
     * times in the SHORT style for the default Locale.
     */
    public SimpleDateFormat() {
        this(Locale.getDefault());
        pattern = defaultPattern();
        formatData = new DateFormatSymbols(Locale.getDefault());
    }

    /**
     * Constructs a new SimpleDateFormat using the specified non-localized
     * pattern and the DateFormatSymbols and Calendar for the default Locale.
     * 
     * @param pattern
     *            the pattern
     * 
     * @exception NullPointerException
     *                if a <code>null</code> value of <code>pattern</code>
     *                is supplied.
     * @exception IllegalArgumentException
     *                if <code>pattern</code> is not considered to be useable
     *                by this formatter.
     */
    public SimpleDateFormat(String pattern) {
        this(pattern, Locale.getDefault());
    }

    /**
     * Constructs a new SimpleDateFormat using the specified non-localized
     * pattern and DateFormatSymbols and the Calendar for the default Locale.
     * 
     * @param template
     *            the pattern
     * @param value
     *            the DateFormatSymbols
     * 
     * @exception NullPointerException
     *                if the pattern is null
     * @exception IllegalArgumentException
     *                if the pattern is invalid
     */
    public SimpleDateFormat(String template, DateFormatSymbols value) {
        this(Locale.getDefault());
        validatePattern(template);
        pattern = template;
        formatData = (DateFormatSymbols) value.clone();
    }

    /**
     * Constructs a new SimpleDateFormat using the specified non-localized
     * pattern and the DateFormatSymbols and Calendar for the specified Locale.
     * 
     * @param template
     *            the pattern
     * @param locale
     *            the Locale
     * 
     * @exception NullPointerException
     *                if the pattern is null
     * @exception IllegalArgumentException
     *                if the pattern is invalid
     */
    public SimpleDateFormat(String template, Locale locale) {
        this(locale);
        validatePattern(template);
        pattern = template;
        formatData = new DateFormatSymbols(locale);
    }

    private SimpleDateFormat(Locale locale) {
        numberFormat = NumberFormat.getInstance(locale);
        numberFormat.setParseIntegerOnly(true);
        numberFormat.setGroupingUsed(false);
        calendar = new GregorianCalendar(locale);
        calendar.add(Calendar.YEAR, -80);
        creationYear = calendar.get(Calendar.YEAR);
        defaultCenturyStart = calendar.getTime();
    }

    private void append(StringBuffer buffer, FieldPosition position,
            Vector<FieldPosition> fields, char format, int count) {
        int field = -1;
        int index = patternChars.indexOf(format);
        if (index == -1) {
            // text.03=Unknown pattern character - '{0}'
            throw new IllegalArgumentException(Messages.getString(
                    "text.03", format)); //$NON-NLS-1$
        }

        int beginPosition = buffer.length();
        Field dateFormatField = null;

        switch (index) {
            case ERA_FIELD:
                dateFormatField = Field.ERA;
                buffer.append(formatData.eras[calendar.get(Calendar.ERA)]);
                break;
            case YEAR_FIELD:
                dateFormatField = Field.YEAR;
                int year = calendar.get(Calendar.YEAR);
                if (count < 4) {
                    appendNumber(buffer, 2, year %= 100);
                } else {
                    appendNumber(buffer, count, year);
                }
                break;
            case MONTH_FIELD:
                dateFormatField = Field.MONTH;
                int month = calendar.get(Calendar.MONTH);
                if (count <= 2) {
                    appendNumber(buffer, count, month + 1);
                } else if (count == 3) {
                    buffer.append(formatData.shortMonths[month]);
                } else {
                    buffer.append(formatData.months[month]);
                }
                break;
            case DATE_FIELD:
                dateFormatField = Field.DAY_OF_MONTH;
                field = Calendar.DATE;
                break;
            case HOUR_OF_DAY1_FIELD: // k
                dateFormatField = Field.HOUR_OF_DAY1;
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                appendNumber(buffer, count, hour == 0 ? 24 : hour);
                break;
            case HOUR_OF_DAY0_FIELD: // H
                dateFormatField = Field.HOUR_OF_DAY0;
                field = Calendar.HOUR_OF_DAY;
                break;
            case MINUTE_FIELD:
                dateFormatField = Field.MINUTE;
                field = Calendar.MINUTE;
                break;
            case SECOND_FIELD:
                dateFormatField = Field.SECOND;
                field = Calendar.SECOND;
                break;
            case MILLISECOND_FIELD:
                dateFormatField = Field.MILLISECOND;
                int value = calendar.get(Calendar.MILLISECOND);
                appendNumber(buffer, count, value);
                break;
            case DAY_OF_WEEK_FIELD:
                dateFormatField = Field.DAY_OF_WEEK;
                int day = calendar.get(Calendar.DAY_OF_WEEK);
                if (count < 4) {
                    buffer.append(formatData.shortWeekdays[day]);
                } else {
                    buffer.append(formatData.weekdays[day]);
                }
                break;
            case DAY_OF_YEAR_FIELD:
                dateFormatField = Field.DAY_OF_YEAR;
                field = Calendar.DAY_OF_YEAR;
                break;
            case DAY_OF_WEEK_IN_MONTH_FIELD:
                dateFormatField = Field.DAY_OF_WEEK_IN_MONTH;
                field = Calendar.DAY_OF_WEEK_IN_MONTH;
                break;
            case WEEK_OF_YEAR_FIELD:
                dateFormatField = Field.WEEK_OF_YEAR;
                field = Calendar.WEEK_OF_YEAR;
                break;
            case WEEK_OF_MONTH_FIELD:
                dateFormatField = Field.WEEK_OF_MONTH;
                field = Calendar.WEEK_OF_MONTH;
                break;
            case AM_PM_FIELD:
                dateFormatField = Field.AM_PM;
                buffer.append(formatData.ampms[calendar.get(Calendar.AM_PM)]);
                break;
            case HOUR1_FIELD: // h
                dateFormatField = Field.HOUR1;
                hour = calendar.get(Calendar.HOUR);
                appendNumber(buffer, count, hour == 0 ? 12 : hour);
                break;
            case HOUR0_FIELD: // K
                dateFormatField = Field.HOUR0;
                field = Calendar.HOUR;
                break;
            case TIMEZONE_FIELD: // z
                dateFormatField = Field.TIME_ZONE;
                appendTimeZone(buffer, count, true);
                break;
            case (TIMEZONE_FIELD + 1): // Z
                dateFormatField = Field.TIME_ZONE;
                appendTimeZone(buffer, count, false);
                break;
        }
        if (field != -1) {
            appendNumber(buffer, count, calendar.get(field));
        }

        if (fields != null) {
            position = new FieldPosition(dateFormatField);
            position.setBeginIndex(beginPosition);
            position.setEndIndex(buffer.length());
            fields.add(position);
        } else {
            // Set to the first occurrence
            if ((position.getFieldAttribute() == dateFormatField || (position
                    .getFieldAttribute() == null && position.getField() == index))
                    && position.getEndIndex() == 0) {
                position.setBeginIndex(beginPosition);
                position.setEndIndex(buffer.length());
            }
        }
    }

    private void appendTimeZone(StringBuffer buffer, int count,
            boolean generalTimezone) {
        // cannot call TimeZone.getDisplayName() because it would not use
        // the DateFormatSymbols of this SimpleDateFormat

        if (generalTimezone) {
            String id = calendar.getTimeZone().getID();
// BEGIN android-changed
            String[][] zones = formatData.internalZoneStrings();
// END android-changed
            String[] zone = null;
            for (String[] element : zones) {
                if (id.equals(element[0])) {
                    zone = element;
                    break;
                }
            }
            if (zone == null) {
                int offset = calendar.get(Calendar.ZONE_OFFSET)
                        + calendar.get(Calendar.DST_OFFSET);
                char sign = '+';
                if (offset < 0) {
                    sign = '-';
                    offset = -offset;
                }
                buffer.append("GMT"); //$NON-NLS-1$
                buffer.append(sign);
                appendNumber(buffer, 2, offset / 3600000);
                buffer.append(':');
                appendNumber(buffer, 2, (offset % 3600000) / 60000);
            } else {
                int daylight = calendar.get(Calendar.DST_OFFSET) == 0 ? 0 : 2;
                if (count < 4) {
                    buffer.append(zone[2 + daylight]);
                } else {
                    buffer.append(zone[1 + daylight]);
                }
            }
        } else {
            int offset = calendar.get(Calendar.ZONE_OFFSET)
                    + calendar.get(Calendar.DST_OFFSET);
            char sign = '+';
            if (offset < 0) {
                sign = '-';
                offset = -offset;
            }
            buffer.append(sign);
            appendNumber(buffer, 2, offset / 3600000);
            appendNumber(buffer, 2, (offset % 3600000) / 60000);
        }
    }

    private void appendNumber(StringBuffer buffer, int count, int value) {
        int minimumIntegerDigits = numberFormat.getMinimumIntegerDigits();
        numberFormat.setMinimumIntegerDigits(count);
        numberFormat.format(new Integer(value), buffer, new FieldPosition(0));
        numberFormat.setMinimumIntegerDigits(minimumIntegerDigits);
    }

    /**
     * Changes the pattern of this SimpleDateFormat to the specified pattern
     * which uses localized pattern characters.
     * 
     * @param template
     *            the localized pattern
     */
    public void applyLocalizedPattern(String template) {
        pattern = convertPattern(template, formatData.getLocalPatternChars(),
                patternChars, true);
    }

    /**
     * Changes the pattern of this SimpleDateFormat to the specified pattern
     * which uses non-localized pattern characters.
     * 
     * @param template
     *            the non-localized pattern
     * 
     * @exception NullPointerException
     *                if the pattern is null
     * @exception IllegalArgumentException
     *                if the pattern is invalid
     */
    public void applyPattern(String template) {
        validatePattern(template);
        pattern = template;
    }

    /**
     * Returns a new SimpleDateFormat with the same pattern and properties as
     * this SimpleDateFormat.
     * 
     * @return a shallow copy of this SimpleDateFormat
     * 
     * @see java.lang.Cloneable
     */
    @Override
    public Object clone() {
        SimpleDateFormat clone = (SimpleDateFormat) super.clone();
        clone.formatData = (DateFormatSymbols) formatData.clone();
        clone.defaultCenturyStart = new Date(defaultCenturyStart.getTime());
        return clone;
    }

    private static String defaultPattern() {
        ResourceBundle bundle = getBundle(Locale.getDefault());
        String styleName = getStyleName(SHORT);
        return bundle.getString("Date_" + styleName) + " " //$NON-NLS-1$ //$NON-NLS-2$
                + bundle.getString("Time_" + styleName); //$NON-NLS-1$
    }

    /**
     * Compares the specified object to this SimpleDateFormat and answer if they
     * are equal. The object must be an instance of SimpleDateFormat and have
     * the same DateFormat properties, pattern, DateFormatSymbols, and creation
     * year.
     * 
     * @param object
     *            the object to compare with this object
     * @return true if the specified object is equal to this SimpleDateFormat,
     *         false otherwise
     * 
     * @see #hashCode
     */
    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof SimpleDateFormat)) {
            return false;
        }
        SimpleDateFormat simple = (SimpleDateFormat) object;
        return super.equals(object) && pattern.equals(simple.pattern)
                && formatData.equals(simple.formatData);
    }

    private Date error(ParsePosition position, int offset, TimeZone zone) {
        position.setErrorIndex(offset);
        calendar.setTimeZone(zone);
        return null;
    }

    /**
     * Formats the specified object using the rules of this SimpleDateFormat and
     * returns an AttributedCharacterIterator with the formatted Date and
     * attributes.
     * 
     * @param object
     *            the object to format
     * @return an AttributedCharacterIterator with the formatted date and
     *         attributes
     * 
     * @exception NullPointerException
     *                when the object is null
     * @exception IllegalArgumentException
     *                when the object cannot be formatted by this Format
     */
    @Override
    public AttributedCharacterIterator formatToCharacterIterator(Object object) {
        if (object == null) {
            throw new NullPointerException();
        }
        if (object instanceof Date) {
            return formatToCharacterIteratorImpl((Date) object);
        }
        if (object instanceof Number) {
            return formatToCharacterIteratorImpl(new Date(((Number) object)
                    .longValue()));
        }
        throw new IllegalArgumentException();
    }

    private AttributedCharacterIterator formatToCharacterIteratorImpl(Date date) {
        StringBuffer buffer = new StringBuffer();
        Vector<FieldPosition> fields = new Vector<FieldPosition>();

        // format the date, and find fields
        formatImpl(date, buffer, null, fields);

        // create and AttributedString with the formatted buffer
        AttributedString as = new AttributedString(buffer.toString());

        // add DateFormat field attributes to the AttributedString
        for (int i = 0; i < fields.size(); i++) {
            FieldPosition pos = fields.elementAt(i);
            Format.Field attribute = pos.getFieldAttribute();
            as.addAttribute(attribute, attribute, pos.getBeginIndex(), pos
                    .getEndIndex());
        }

        // return the CharacterIterator from AttributedString
        return as.getIterator();
    }

    /**
     * Formats the specified Date into the specified StringBuffer using the
     * pattern of this SimpleDateFormat. If the field specified by the
     * FieldPosition is formatted, set the begin and end index of the formatted
     * field in the FieldPosition.
     * 
     * @param date
     *            the Date to format
     * @param buffer
     *            the StringBuffer
     * @param field
     *            the FieldPosition
     * @return the StringBuffer parameter <code>buffer</code>
     * 
     * @exception IllegalArgumentException
     *                when there are invalid characters in the pattern
     */
    @Override
    public StringBuffer format(Date date, StringBuffer buffer,
            FieldPosition field) {
        return formatImpl(date, buffer, field, null);
    }

    /**
     * Validate the format character.
     * 
     * @param format
     *            the format character
     * 
     * @throws IllegalArgumentException
     *             when the format character is invalid
     */
    private void validateFormat(char format) {
        int index = patternChars.indexOf(format);
        if (index == -1) {
            // text.03=Unknown pattern character - '{0}'
            throw new IllegalArgumentException(Messages.getString(
                    "text.03", format)); //$NON-NLS-1$
        }
    }

    /**
     * Validate the pattern.
     * 
     * @param template
     *            the pattern to validate.
     * 
     * @throws NullPointerException
     *             if the pattern is null
     * @throws IllegalArgumentException
     *             if the pattern is invalid
     */
    private void validatePattern(String template) {
        boolean quote = false;
        int next, last = -1, count = 0;

        final int patternLength = template.length();
        for (int i = 0; i < patternLength; i++) {
            next = (template.charAt(i));
            if (next == '\'') {
                if (count > 0) {
                    validateFormat((char) last);
                    count = 0;
                }
                if (last == next) {
                    last = -1;
                } else {
                    last = next;
                }
                quote = !quote;
                continue;
            }
            if (!quote
                    && (last == next || (next >= 'a' && next <= 'z') || (next >= 'A' && next <= 'Z'))) {
                if (last == next) {
                    count++;
                } else {
                    if (count > 0) {
                        validateFormat((char) last);
                    }
                    last = next;
                    count = 1;
                }
            } else {
                if (count > 0) {
                    validateFormat((char) last);
                    count = 0;
                }
                last = -1;
            }
        }
        if (count > 0) {
            validateFormat((char) last);
        }

        if (quote) {
            // text.04=Unterminated quote {0}
            throw new IllegalArgumentException(Messages.getString("text.04")); //$NON-NLS-1$
        }

    }

    /**
     * Formats the date.
     * <p>
     * If the FieldPosition <code>field</code> is not null, and the field
     * specified by this FieldPosition is formatted, set the begin and end index
     * of the formatted field in the FieldPosition.
     * <p>
     * If the Vector <code>fields</code> is not null, find fields of this
     * date, set FieldPositions with these fields, and add them to the fields
     * vector.
     * 
     * @param date
     *            Date to Format
     * @param buffer
     *            StringBuffer to store the resulting formatted String
     * @param field
     *            FieldPosition to set begin and end index of the field
     *            specified, if it is part of the format for this date
     * @param fields
     *            Vector used to store the FieldPositions for each field in this
     *            date
     * 
     * @return the formatted Date
     * 
     * @exception IllegalArgumentException
     *                when the object cannot be formatted by this Format
     */
    private StringBuffer formatImpl(Date date, StringBuffer buffer,
            FieldPosition field, Vector<FieldPosition> fields) {

        boolean quote = false;
        int next, last = -1, count = 0;
        calendar.setTime(date);
        if (field != null) {
            field.clear();
        }

        final int patternLength = pattern.length();
        for (int i = 0; i < patternLength; i++) {
            next = (pattern.charAt(i));
            if (next == '\'') {
                if (count > 0) {
                    append(buffer, field, fields, (char) last, count);
                    count = 0;
                }
                if (last == next) {
                    buffer.append('\'');
                    last = -1;
                } else {
                    last = next;
                }
                quote = !quote;
                continue;
            }
            if (!quote
                    && (last == next || (next >= 'a' && next <= 'z') || (next >= 'A' && next <= 'Z'))) {
                if (last == next) {
                    count++;
                } else {
                    if (count > 0) {
                        append(buffer, field, fields, (char) last, count);
                    }
                    last = next;
                    count = 1;
                }
            } else {
                if (count > 0) {
                    append(buffer, field, fields, (char) last, count);
                    count = 0;
                }
                last = -1;
                buffer.append((char) next);
            }
        }
        if (count > 0) {
            append(buffer, field, fields, (char) last, count);
        }
        return buffer;
    }

    /**
     * Returns the Date which is the start of the one hundred year period for
     * two digits year values.
     * 
     * @return a Date
     */
    public Date get2DigitYearStart() {
        return defaultCenturyStart;
    }

    /**
     * Returns the DateFormatSymbols used by this SimpleDateFormat.
     * 
     * @return a DateFormatSymbols
     */
    public DateFormatSymbols getDateFormatSymbols() {
        // Return a clone so the arrays in the ResourceBundle are not modified
        return (DateFormatSymbols) formatData.clone();
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
        return super.hashCode() + pattern.hashCode() + formatData.hashCode()
                + creationYear;
    }

    private int parse(String string, int offset, char format, int count) {
        int index = patternChars.indexOf(format);
        if (index == -1) {
            // text.03=Unknown pattern character - '{0}'
            throw new IllegalArgumentException(Messages.getString(
                    "text.03", format)); //$NON-NLS-1$
        }
        int field = -1;
        int absolute = 0;
        if (count < 0) {
            count = -count;
            absolute = count;
        }
        switch (index) {
            case ERA_FIELD:
                return parseText(string, offset, formatData.eras, Calendar.ERA);
            case YEAR_FIELD:
                if (count >= 3) {
                    field = Calendar.YEAR;
                } else {
                    ParsePosition position = new ParsePosition(offset);
                    Number result = parseNumber(absolute, string, position);
                    if (result == null) {
                        return -position.getErrorIndex() - 1;
                    }
                    int year = result.intValue();
                    // A two digit year must be exactly two digits, i.e. 01
                    if ((position.getIndex() - offset) == 2 && year >= 0) {
                        year += creationYear / 100 * 100;
                        if (year < creationYear) {
                            year += 100;
                        }
                    }
                    calendar.set(Calendar.YEAR, year);
                    return position.getIndex();
                }
                break;
            case MONTH_FIELD:
                if (count <= 2) {
                    return parseNumber(absolute, string, offset,
                            Calendar.MONTH, -1);
                }
                index = parseText(string, offset, formatData.months,
                        Calendar.MONTH);
                if (index < 0) {
                    return parseText(string, offset, formatData.shortMonths,
                            Calendar.MONTH);
                }
                return index;
            case DATE_FIELD:
                field = Calendar.DATE;
                break;
            case HOUR_OF_DAY1_FIELD:
                ParsePosition position = new ParsePosition(offset);
                Number result = parseNumber(absolute, string, position);
                if (result == null) {
                    return -position.getErrorIndex() - 1;
                }
                int hour = result.intValue();
                if (hour == 24) {
                    hour = 0;
                }
                calendar.set(Calendar.HOUR_OF_DAY, hour);
                return position.getIndex();
            case HOUR_OF_DAY0_FIELD:
                field = Calendar.HOUR_OF_DAY;
                break;
            case MINUTE_FIELD:
                field = Calendar.MINUTE;
                break;
            case SECOND_FIELD:
                field = Calendar.SECOND;
                break;
            case MILLISECOND_FIELD:
                field = Calendar.MILLISECOND;
                break;
            case DAY_OF_WEEK_FIELD:
                index = parseText(string, offset, formatData.weekdays,
                        Calendar.DAY_OF_WEEK);
                if (index < 0) {
                    return parseText(string, offset, formatData.shortWeekdays,
                            Calendar.DAY_OF_WEEK);
                }
                return index;
            case DAY_OF_YEAR_FIELD:
                field = Calendar.DAY_OF_YEAR;
                break;
            case DAY_OF_WEEK_IN_MONTH_FIELD:
                field = Calendar.DAY_OF_WEEK_IN_MONTH;
                break;
            case WEEK_OF_YEAR_FIELD:
                field = Calendar.WEEK_OF_YEAR;
                break;
            case WEEK_OF_MONTH_FIELD:
                field = Calendar.WEEK_OF_MONTH;
                break;
            case AM_PM_FIELD:
                return parseText(string, offset, formatData.ampms,
                        Calendar.AM_PM);
            case HOUR1_FIELD:
                position = new ParsePosition(offset);
                result = parseNumber(absolute, string, position);
                if (result == null) {
                    return -position.getErrorIndex() - 1;
                }
                hour = result.intValue();
                if (hour == 12) {
                    hour = 0;
                }
                calendar.set(Calendar.HOUR, hour);
                return position.getIndex();
            case HOUR0_FIELD:
                field = Calendar.HOUR;
                break;
            case TIMEZONE_FIELD:
                return parseTimeZone(string, offset);
            case (TIMEZONE_FIELD + 1):
                return parseTimeZone(string, offset);
        }
        if (field != -1) {
            return parseNumber(absolute, string, offset, field, 0);
        }
        return offset;
    }

    /**
     * Parse a Date from the specified String starting at the index specified by
     * the ParsePosition. If the string is successfully parsed, the index of the
     * ParsePosition is updated to the index following the parsed text.
     * 
     * @param string
     *            the String to parse according to the pattern of this
     *            SimpleDateFormat
     * @param position
     *            the ParsePosition, updated on return with the index following
     *            the parsed text, or on error the index is unchanged and the
     *            error index is set to the index where the error occurred
     * @return the Date resulting from the parse, or null if there is an error
     * 
     * @exception IllegalArgumentException
     *                when there are invalid characters in the pattern
     */
    @Override
    public Date parse(String string, ParsePosition position) {
        boolean quote = false;
        int next, last = -1, count = 0, offset = position.getIndex();
        int length = string.length();
        calendar.clear();
        TimeZone zone = calendar.getTimeZone();
        final int patternLength = pattern.length();
        for (int i = 0; i < patternLength; i++) {
            next = pattern.charAt(i);
            if (next == '\'') {
                if (count > 0) {
                    if ((offset = parse(string, offset, (char) last, count)) < 0) {
                        return error(position, -offset - 1, zone);
                    }
                    count = 0;
                }
                if (last == next) {
                    if (offset >= length || string.charAt(offset) != '\'') {
                        return error(position, offset, zone);
                    }
                    offset++;
                    last = -1;
                } else {
                    last = next;
                }
                quote = !quote;
                continue;
            }
            if (!quote
                    && (last == next || (next >= 'a' && next <= 'z') || (next >= 'A' && next <= 'Z'))) {
                if (last == next) {
                    count++;
                } else {
                    if (count > 0) {
                        if ((offset = parse(string, offset, (char) last, -count)) < 0) {
                            return error(position, -offset - 1, zone);
                        }
                    }
                    last = next;
                    count = 1;
                }
            } else {
                if (count > 0) {
                    if ((offset = parse(string, offset, (char) last, count)) < 0) {
                        return error(position, -offset - 1, zone);
                    }
                    count = 0;
                }
                last = -1;
                if (offset >= length || string.charAt(offset) != next) {
                    return error(position, offset, zone);
                }
                offset++;
            }
        }
        if (count > 0) {
            if ((offset = parse(string, offset, (char) last, count)) < 0) {
                return error(position, -offset - 1, zone);
            }
        }
        Date date;
        try {
            date = calendar.getTime();
        } catch (IllegalArgumentException e) {
            return error(position, offset, zone);
        }
        position.setIndex(offset);
        calendar.setTimeZone(zone);
        return date;
    }

    private Number parseNumber(int max, String string, ParsePosition position) {
        int digit, length = string.length(), result = 0;
        int index = position.getIndex();
        if (max > 0 && max < length - index) {
            length = index + max;
        }
        while (index < length
                && (string.charAt(index) == ' ' || string.charAt(index) == '\t')) {
            index++;
        }
        if (max == 0) {
            position.setIndex(index);
            return numberFormat.parse(string, position);
        }

        while (index < length
                && (digit = Character.digit(string.charAt(index), 10)) != -1) {
            index++;
            result = result * 10 + digit;
        }
        if (index == position.getIndex()) {
            position.setErrorIndex(index);
            return null;
        }
        position.setIndex(index);
        return new Integer(result);
    }

    private int parseNumber(int max, String string, int offset, int field,
            int skew) {
        ParsePosition position = new ParsePosition(offset);
        Number result = parseNumber(max, string, position);
        if (result == null) {
            return -position.getErrorIndex() - 1;
        }
        calendar.set(field, result.intValue() + skew);
        return position.getIndex();
    }

    private int parseText(String string, int offset, String[] text, int field) {
        int found = -1;
        for (int i = 0; i < text.length; i++) {
            if (text[i].length() == 0) {
                continue;
            }
            if (string
                    .regionMatches(true, offset, text[i], 0, text[i].length())) {
                // Search for the longest match, in case some fields are subsets
                if (found == -1 || text[i].length() > text[found].length()) {
                    found = i;
                }
            }
        }
        if (found != -1) {
            calendar.set(field, found);
            return offset + text[found].length();
        }
        return -offset - 1;
    }

    private int parseTimeZone(String string, int offset) {
// BEGIN android-changed
        String[][] zones = formatData.internalZoneStrings();
// END android-changed
        boolean foundGMT = string.regionMatches(offset, "GMT", 0, 3); //$NON-NLS-1$
        if (foundGMT) {
            offset += 3;
        }
        char sign;
        if (offset < string.length()
                && ((sign = string.charAt(offset)) == '+' || sign == '-')) {
            ParsePosition position = new ParsePosition(offset + 1);
            Number result = numberFormat.parse(string, position);
            if (result == null) {
                return -position.getErrorIndex() - 1;
            }
            int hour = result.intValue();
            int raw = hour * 3600000;
            int index = position.getIndex();
            if (index < string.length() && string.charAt(index) == ':') {
                position.setIndex(index + 1);
                result = numberFormat.parse(string, position);
                if (result == null) {
                    return -position.getErrorIndex() - 1;
                }
                int minute = result.intValue();
                raw += minute * 60000;
            } else if (hour >= 24) {
                raw = (hour / 100 * 3600000) + (hour % 100 * 60000);
            }
            if (sign == '-') {
                raw = -raw;
            }
            calendar.setTimeZone(new SimpleTimeZone(raw, "")); //$NON-NLS-1$
            return position.getIndex();
        }
        if (foundGMT) {
            calendar.setTimeZone(TimeZone.getTimeZone("GMT")); //$NON-NLS-1$
            return offset;
        }
        for (String[] element : zones) {
            for (int j = 1; j < 5; j++) {
                if (string.regionMatches(true, offset, element[j], 0,
                        element[j].length())) {
                    TimeZone zone = TimeZone.getTimeZone(element[0]);
                    if (zone == null) {
                        return -offset - 1;
                    }
                    int raw = zone.getRawOffset();
                    if (j >= 3 && zone.useDaylightTime()) {
                        raw += 3600000;
                    }
                    calendar.setTimeZone(new SimpleTimeZone(raw, "")); //$NON-NLS-1$
                    return offset + element[j].length();
                }
            }
        }
        return -offset - 1;
    }

    /**
     * Sets the Date which is the start of the one hundred year period for two
     * digits year values.
     * 
     * @param date
     *            the Date
     */
    public void set2DigitYearStart(Date date) {
        defaultCenturyStart = date;
        Calendar cal = new GregorianCalendar();
        cal.setTime(date);
        creationYear = cal.get(Calendar.YEAR);
    }

    /**
     * Sets the DateFormatSymbols used by this SimpleDateFormat.
     * 
     * @param value
     *            the DateFormatSymbols
     */
    public void setDateFormatSymbols(DateFormatSymbols value) {
        formatData = (DateFormatSymbols) value.clone();
    }

    /**
     * Returns the pattern of this SimpleDateFormat using localized pattern
     * characters.
     * 
     * @return the localized pattern
     */
    public String toLocalizedPattern() {
        return convertPattern(pattern, patternChars, formatData
                .getLocalPatternChars(), false);
    }

    /**
     * Returns the pattern of this SimpleDateFormat using non-localized pattern
     * characters.
     * 
     * @return the non-localized pattern
     */
    public String toPattern() {
        return pattern;
    }

    private static final ObjectStreamField[] serialPersistentFields = {
            new ObjectStreamField("defaultCenturyStart", Date.class), //$NON-NLS-1$
            new ObjectStreamField("formatData", DateFormatSymbols.class), //$NON-NLS-1$
            new ObjectStreamField("pattern", String.class), //$NON-NLS-1$
            new ObjectStreamField("serialVersionOnStream", Integer.TYPE), }; //$NON-NLS-1$

    private void writeObject(ObjectOutputStream stream) throws IOException {
        ObjectOutputStream.PutField fields = stream.putFields();
        fields.put("defaultCenturyStart", defaultCenturyStart); //$NON-NLS-1$
        fields.put("formatData", formatData); //$NON-NLS-1$
        fields.put("pattern", pattern); //$NON-NLS-1$
        fields.put("serialVersionOnStream", 1); //$NON-NLS-1$
        stream.writeFields();
    }

    private void readObject(ObjectInputStream stream) throws IOException,
            ClassNotFoundException {
        ObjectInputStream.GetField fields = stream.readFields();
        int version = fields.get("serialVersionOnStream", 0); //$NON-NLS-1$
        Date date;
        if (version > 0) {
            date = (Date) fields.get("defaultCenturyStart", new Date()); //$NON-NLS-1$
        } else {
            date = new Date();
        }
        set2DigitYearStart(date);
        formatData = (DateFormatSymbols) fields.get("formatData", null); //$NON-NLS-1$
        pattern = (String) fields.get("pattern", ""); //$NON-NLS-1$ //$NON-NLS-2$
    }
}
