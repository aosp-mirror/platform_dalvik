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
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamField;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.Vector;

import org.apache.harmony.text.internal.nls.Messages;

/**
 * MessageFormat is used to format and parse arguments based on a pattern. The
 * pattern specifies how each argument will be formatted and concatenated with
 * other text to produce the formatted output.
 */
public class MessageFormat extends Format {

    private static final long serialVersionUID = 6479157306784022952L;

    private Locale locale = Locale.getDefault();

    transient private String[] strings;

    private int[] argumentNumbers;

    private Format[] formats;

    private int maxOffset;

    transient private int maxArgumentIndex;

    /**
     * Constructs a new MessageFormat using the specified pattern and the
     * specified Locale for Formats.
     * 
     * @param template
     *            the pattern
     * @param locale
     *            the locale
     * 
     * @exception IllegalArgumentException
     *                when the pattern cannot be parsed
     */
    public MessageFormat(String template, Locale locale) {
        this.locale = locale;
        applyPattern(template);
    }

    /**
     * Constructs a new MessageFormat using the specified pattern and the
     * default Locale for Formats.
     * 
     * @param template
     *            the pattern
     * 
     * @exception IllegalArgumentException
     *                when the pattern cannot be parsed
     */
    public MessageFormat(String template) {
        applyPattern(template);
    }

    /**
     * Changes this MessageFormat to use the specified pattern.
     * 
     * @param template
     *            the pattern
     * 
     * @exception IllegalArgumentException
     *                when the pattern cannot be parsed
     */
    public void applyPattern(String template) {
        int length = template.length();
        StringBuffer buffer = new StringBuffer();
        ParsePosition position = new ParsePosition(0);
        Vector<String> localStrings = new Vector<String>();
        int argCount = 0;
        int[] args = new int[10];
        int maxArg = -1;
        Vector<Format> localFormats = new Vector<Format>();
        while (position.getIndex() < length) {
            if (Format.upTo(template, position, buffer, '{')) {
                byte arg;
                int offset = position.getIndex();
                if (offset >= length
                        || (arg = (byte) Character.digit(template
                                .charAt(offset++), 10)) == -1) {
                    // text.19=Invalid argument number
                    throw new IllegalArgumentException(Messages
                            .getString("text.19")); //$NON-NLS-1$
                }
                position.setIndex(offset);
                localFormats.addElement(parseVariable(template, position));
                if (argCount >= args.length) {
                    int[] newArgs = new int[args.length * 2];
                    System.arraycopy(args, 0, newArgs, 0, args.length);
                    args = newArgs;
                }
                args[argCount++] = arg;
                if (arg > maxArg) {
                    maxArg = arg;
                }
            }
            localStrings.addElement(buffer.toString());
            buffer.setLength(0);
        }
        this.strings = new String[localStrings.size()];
        for (int i = 0; i < localStrings.size(); i++) {
            this.strings[i] = localStrings.elementAt(i);
        }
        argumentNumbers = args;
        this.formats = new Format[argCount];
        for (int i = 0; i < argCount; i++) {
            this.formats[i] = localFormats.elementAt(i);
        }
        maxOffset = argCount - 1;
        maxArgumentIndex = maxArg;
    }

    /**
     * Returns a new instance of MessageFormat with the same pattern and Formats
     * as this MessageFormat.
     * 
     * @return a shallow copy of this MessageFormat
     * 
     * @see java.lang.Cloneable
     */
    @Override
    public Object clone() {
        MessageFormat clone = (MessageFormat) super.clone();
        Format[] array = new Format[formats.length];
        for (int i = formats.length; --i >= 0;) {
            if (formats[i] != null) {
                array[i] = (Format) formats[i].clone();
            }
        }
        clone.formats = array;
        return clone;
    }

    /**
     * Compares the specified object to this MessageFormat and answer if they
     * are equal. The object must be an instance of MessageFormat and have the
     * same pattern.
     * 
     * @param object
     *            the object to compare with this object
     * @return true if the specified object is equal to this MessageFormat,
     *         false otherwise
     * 
     * @see #hashCode
     */
    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof MessageFormat)) {
            return false;
        }
        MessageFormat format = (MessageFormat) object;
        if (maxOffset != format.maxOffset) {
            return false;
        }
        // Must use a loop since the lengths may be different due
        // to serialization cross-loading
        for (int i = 0; i <= maxOffset; i++) {
            if (argumentNumbers[i] != format.argumentNumbers[i]) {
                return false;
            }
        }
        return locale.equals(format.locale)
                && Arrays.equals(strings, format.strings)
                && Arrays.equals(formats, format.formats);
    }

    /**
     * Formats the specified object using the rules of this MessageFormat and
     * returns an AttributedCharacterIterator with the formatted message and
     * attributes. The AttributedCharacterIterator returned also includes the
     * attributes from the formats of this MessageFormat.
     * 
     * @param object
     *            the object to format
     * @return an AttributedCharacterIterator with the formatted message and
     *         attributes
     * 
     * @exception IllegalArgumentException
     *                when the arguments in the object array cannot be formatted
     *                by this Format
     */
    @Override
    public AttributedCharacterIterator formatToCharacterIterator(Object object) {
        if (object == null) {
            throw new NullPointerException();
        }

        StringBuffer buffer = new StringBuffer();
        Vector<FieldContainer> fields = new Vector<FieldContainer>();

        // format the message, and find fields
        formatImpl((Object[]) object, buffer, new FieldPosition(0), fields);

        // create an AttributedString with the formatted buffer
        AttributedString as = new AttributedString(buffer.toString());

        // add MessageFormat field attributes and values to the AttributedString
        for (int i = 0; i < fields.size(); i++) {
            FieldContainer fc = fields.elementAt(i);
            as.addAttribute(fc.attribute, fc.value, fc.start, fc.end);
        }

        // return the CharacterIterator from AttributedString
        return as.getIterator();
    }

    /**
     * Formats the Object arguments into the specified StringBuffer using the
     * pattern of this MessageFormat.
     * <p>
     * If Field Attribute of the FieldPosition supplied is
     * MessageFormat.Field.ARGUMENT, then begin and end index of this field
     * position is set to the location of the first occurrence of a message
     * format argument. Otherwise the FieldPosition is ignored
     * <p>
     * 
     * @param objects
     *            the array of Objects to format
     * @param buffer
     *            the StringBuffer
     * @param field
     *            a FieldPosition.
     * 
     * @return the StringBuffer parameter <code>buffer</code>
     */
    public final StringBuffer format(Object[] objects, StringBuffer buffer,
            FieldPosition field) {
        return formatImpl(objects, buffer, field, null);
    }

    private StringBuffer formatImpl(Object[] objects, StringBuffer buffer,
            FieldPosition position, Vector<FieldContainer> fields) {
        FieldPosition passedField = new FieldPosition(0);
        for (int i = 0; i <= maxOffset; i++) {
            buffer.append(strings[i]);
            int begin = buffer.length();
            Object arg;
            if (objects != null && argumentNumbers[i] < objects.length) {
                arg = objects[argumentNumbers[i]];
            } else {
                buffer.append('{');
                buffer.append(argumentNumbers[i]);
                buffer.append('}');
                handleArgumentField(begin, buffer.length(), argumentNumbers[i],
                        position, fields);
                continue;
            }
            Format format = formats[i];
            if (format == null || arg == null) {
                if (arg instanceof Number) {
                    format = NumberFormat.getInstance();
                } else if (arg instanceof Date) {
                    format = DateFormat.getInstance();
                } else {
                    buffer.append(arg);
                    handleArgumentField(begin, buffer.length(),
                            argumentNumbers[i], position, fields);
                    continue;
                }
            }
            if (format instanceof ChoiceFormat) {
                String result = format.format(arg);
                MessageFormat mf = new MessageFormat(result);
                mf.setLocale(locale);
                mf.format(objects, buffer, passedField);
                handleArgumentField(begin, buffer.length(), argumentNumbers[i],
                        position, fields);
                handleformat(format, arg, begin, fields);
            } else {
                format.format(arg, buffer, passedField);
                handleArgumentField(begin, buffer.length(), argumentNumbers[i],
                        position, fields);
                handleformat(format, arg, begin, fields);
            }
        }
        if (maxOffset + 1 < strings.length) {
            buffer.append(strings[maxOffset + 1]);
        }
        return buffer;
    }

    /**
     * Adds a new FieldContainer with MessageFormat.Field.ARGUMENT field,
     * argnumber, begin and end index to the fields vector, or sets the
     * position's begin and end index if it has MessageFormat.Field.ARGUMENT as
     * its field attribute.
     * 
     * @param begin
     * @param end
     * @param argnumber
     * @param position
     * @param fields
     */
    private void handleArgumentField(int begin, int end, int argnumber,
            FieldPosition position, Vector<FieldContainer> fields) {
        if (fields != null) {
            fields.add(new FieldContainer(begin, end, Field.ARGUMENT,
                    new Integer(argnumber)));
        } else {
            if (position != null
                    && position.getFieldAttribute() == Field.ARGUMENT
                    && position.getEndIndex() == 0) {
                position.setBeginIndex(begin);
                position.setEndIndex(end);
            }
        }
    }

    /**
     * An inner class to store attributes, values, start and end indices.
     * Instances of this inner class are used as elements for the fields vector
     */
    private static class FieldContainer {
        int start, end;

        AttributedCharacterIterator.Attribute attribute;

        Object value;

        public FieldContainer(int start, int end,
                AttributedCharacterIterator.Attribute attribute, Object value) {
            this.start = start;
            this.end = end;
            this.attribute = attribute;
            this.value = value;
        }
    }

    /**
     * If fields vector is not null, find and add the fields of this format to
     * the fields vector by iterating through its AttributedCharacterIterator
     * 
     * @param format
     *            the format to find fields for
     * @param arg
     *            object to format
     * @param begin
     *            the index where the string this format has formatted begins
     * @param fields
     *            fields vector, each entry in this vector are of type
     *            FieldContainer.
     */
    private void handleformat(Format format, Object arg, int begin,
            Vector<FieldContainer> fields) {
        if (fields != null) {
            AttributedCharacterIterator iterator = format
                    .formatToCharacterIterator(arg);
            while (iterator.getIndex() != iterator.getEndIndex()) {
                int start = iterator.getRunStart();
                int end = iterator.getRunLimit();

                Iterator<?> it = iterator.getAttributes().keySet().iterator();
                while (it.hasNext()) {
                    AttributedCharacterIterator.Attribute attribute = (AttributedCharacterIterator.Attribute) it
                            .next();
                    Object value = iterator.getAttribute(attribute);
                    fields.add(new FieldContainer(begin + start, begin + end,
                            attribute, value));
                }
                iterator.setIndex(end);
            }
        }
    }

    /**
     * Formats the specified object into the specified StringBuffer using the
     * pattern of this MessageFormat.
     * 
     * @param object
     *            the object to format, must be an array of Object
     * @param buffer
     *            the StringBuffer
     * @param field
     *            a FieldPosition which is ignored
     * @return the StringBuffer parameter <code>buffer</code>
     * 
     * @exception ClassCastException
     *                when <code>object</code> is not an array of Object
     */
    @Override
    public final StringBuffer format(Object object, StringBuffer buffer,
            FieldPosition field) {
        return format((Object[]) object, buffer, field);
    }

    /**
     * Formats the Object arguments using the specified MessageFormat pattern.
     * 
     * @param template
     *            the pattern
     * @param objects
     *            the array of Objects to format
     * @return the formatted result
     * 
     * @exception IllegalArgumentException
     *                when the pattern cannot be parsed
     */
    public static String format(String template, Object... objects) {
        return new MessageFormat(template).format(objects);
    }

    /**
     * Returns the Formats of this MessageFormat.
     * 
     * @return an array of Format
     */
    public Format[] getFormats() {
        return formats.clone();
    }

    /**
     * Returns the formats used for each argument index. If an argument is
     * placed more than once in the pattern string, than returns the format of
     * the last one.
     * 
     * @return an array of formats, ordered by argument index
     */
    public Format[] getFormatsByArgumentIndex() {
        Format[] answer = new Format[maxArgumentIndex + 1];
        for (int i = 0; i < maxOffset + 1; i++) {
            answer[argumentNumbers[i]] = formats[i];
        }
        return answer;
    }

    /**
     * Sets the format used for argument at index <code>argIndex</code>to
     * <code>format</code>
     * 
     * @param argIndex
     * @param format
     */
    public void setFormatByArgumentIndex(int argIndex, Format format) {
        for (int i = 0; i < maxOffset + 1; i++) {
            if (argumentNumbers[i] == argIndex) {
                formats[i] = format;
            }
        }
    }

    /**
     * Sets the formats used for each argument <code>The formats</code> array
     * elements should be in the order of the argument indices.
     * 
     * @param formats
     */
    public void setFormatsByArgumentIndex(Format[] formats) {
        for (int j = 0; j < formats.length; j++) {
            for (int i = 0; i < maxOffset + 1; i++) {
                if (argumentNumbers[i] == j) {
                    this.formats[i] = formats[j];
                }
            }
        }
    }

    /**
     * Returns the Locale used when creating Formats.
     * 
     * @return the Locale used to create Formats
     */
    public Locale getLocale() {
        return locale;
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
        int hashCode = 0;
        for (int i = 0; i <= maxOffset; i++) {
            hashCode += argumentNumbers[i] + strings[i].hashCode();
            if (formats[i] != null) {
                hashCode += formats[i].hashCode();
            }
        }
        if (maxOffset + 1 < strings.length) {
            hashCode += strings[maxOffset + 1].hashCode();
        }
        if (locale != null) {
            return hashCode + locale.hashCode();
        }
        return hashCode;
    }

    /**
     * Parse the message arguments from the specified String using the rules of
     * this MessageFormat.
     * 
     * @param string
     *            the String to parse
     * @return the array of Object arguments resulting from the parse
     * 
     * @exception ParseException
     *                when an error occurs during parsing
     */
    public Object[] parse(String string) throws ParseException {
        ParsePosition position = new ParsePosition(0);
        Object[] result = parse(string, position);
        if (position.getErrorIndex() != -1 || position.getIndex() == 0) {
            throw new ParseException(null, position.getErrorIndex());
        }
        return result;
    }

    /**
     * Parse the message argument from the specified String starting at the
     * index specified by the ParsePosition. If the string is successfully
     * parsed, the index of the ParsePosition is updated to the index following
     * the parsed text.
     * 
     * @param string
     *            the String to parse
     * @param position
     *            the ParsePosition, updated on return with the index following
     *            the parsed text, or on error the index is unchanged and the
     *            error index is set to the index where the error occurred
     * @return the array of Object arguments resulting from the parse, or null
     *         if there is an error
     */
    public Object[] parse(String string, ParsePosition position) {
        if (string == null) {
            return new Object[0];
        }
        ParsePosition internalPos = new ParsePosition(0);
        int offset = position.getIndex();
        Object[] result = new Object[maxArgumentIndex + 1];
        for (int i = 0; i <= maxOffset; i++) {
            String sub = strings[i];
            if (!string.startsWith(sub, offset)) {
                position.setErrorIndex(offset);
                return null;
            }
            offset += sub.length();
            Object parse;
            Format format = formats[i];
            if (format == null) {
                if (i + 1 < strings.length) {
                    int next = string.indexOf(strings[i + 1], offset);
                    if (next == -1) {
                        position.setErrorIndex(offset);
                        return null;
                    }
                    parse = string.substring(offset, next);
                    offset = next;
                } else {
                    parse = string.substring(offset);
                    offset = string.length();
                }
            } else {
                internalPos.setIndex(offset);
                parse = format.parseObject(string, internalPos);
                if (internalPos.getErrorIndex() != -1) {
                    position.setErrorIndex(offset);
                    return null;
                }
                offset = internalPos.getIndex();
            }
            result[argumentNumbers[i]] = parse;
        }
        if (maxOffset + 1 < strings.length) {
            String sub = strings[maxOffset + 1];
            if (!string.startsWith(sub, offset)) {
                position.setErrorIndex(offset);
                return null;
            }
            offset += sub.length();
        }
        position.setIndex(offset);
        return result;
    }

    /**
     * Parse the message argument from the specified String starting at the
     * index specified by the ParsePosition. If the string is successfully
     * parsed, the index of the ParsePosition is updated to the index following
     * the parsed text.
     * 
     * @param string
     *            the String to parse
     * @param position
     *            the ParsePosition, updated on return with the index following
     *            the parsed text, or on error the index is unchanged and the
     *            error index is set to the index where the error occurred
     * @return the array of Object arguments resulting from the parse, or null
     *         if there is an error
     */
    @Override
    public Object parseObject(String string, ParsePosition position) {
        return parse(string, position);
    }

    private int match(String string, ParsePosition position, boolean last,
            String[] tokens) {
        int length = string.length(), offset = position.getIndex(), token = -1;
        while (offset < length && Character.isWhitespace(string.charAt(offset))) {
            offset++;
        }
        for (int i = tokens.length; --i >= 0;) {
            if (string.regionMatches(true, offset, tokens[i], 0, tokens[i]
                    .length())) {
                token = i;
                break;
            }
        }
        if (token == -1) {
            return -1;
        }
        offset += tokens[token].length();
        while (offset < length && Character.isWhitespace(string.charAt(offset))) {
            offset++;
        }
        char ch;
        if (offset < length
                && ((ch = string.charAt(offset)) == '}' || (!last && ch == ','))) {
            position.setIndex(offset + 1);
            return token;
        }
        return -1;
    }

    private Format parseVariable(String string, ParsePosition position) {
        int length = string.length(), offset = position.getIndex();
        char ch;
        if (offset >= length
                || ((ch = string.charAt(offset++)) != '}' && ch != ',')) {
            // text.15=Missing element format
            throw new IllegalArgumentException(Messages.getString("text.15")); //$NON-NLS-1$
        }
        position.setIndex(offset);
        if (ch == '}') {
            return null;
        }
        int type = match(string, position, false, new String[] { "time", //$NON-NLS-1$
                "date", "number", "choice" }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        if (type == -1) {
            // text.16=Unknown element format
            throw new IllegalArgumentException(Messages.getString("text.16")); //$NON-NLS-1$
        }
        StringBuffer buffer = new StringBuffer();
        ch = string.charAt(position.getIndex() - 1);
        switch (type) {
            case 0: // time
            case 1: // date
                if (ch == '}') {
                    return type == 1 ? DateFormat.getDateInstance(
                            DateFormat.DEFAULT, locale) : DateFormat
                            .getTimeInstance(DateFormat.DEFAULT, locale);
                }
                int dateStyle = match(string, position, true, new String[] {
                        "full", "long", "medium", "short" }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                if (dateStyle == -1) {
                    Format.upToWithQuotes(string, position, buffer, '}', '{');
                    return new SimpleDateFormat(buffer.toString(), locale);
                }
                switch (dateStyle) {
                    case 0:
                        dateStyle = DateFormat.FULL;
                        break;
                    case 1:
                        dateStyle = DateFormat.LONG;
                        break;
                    case 2:
                        dateStyle = DateFormat.MEDIUM;
                        break;
                    case 3:
                        dateStyle = DateFormat.SHORT;
                        break;
                }
                return type == 1 ? DateFormat
                        .getDateInstance(dateStyle, locale) : DateFormat
                        .getTimeInstance(dateStyle, locale);
            case 2: // number
                if (ch == '}') {
                    return NumberFormat.getInstance();
                }
                int numberStyle = match(string, position, true, new String[] {
                        "currency", "percent", "integer" }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                if (numberStyle == -1) {
                    Format.upToWithQuotes(string, position, buffer, '}', '{');
                    return new DecimalFormat(buffer.toString(),
                            new DecimalFormatSymbols(locale));
                }
                switch (numberStyle) {
                    case 0: // currency
                        return NumberFormat.getCurrencyInstance(locale);
                    case 1: // percent
                        return NumberFormat.getPercentInstance(locale);
                }
                return NumberFormat.getIntegerInstance(locale);
        }
        // choice
        try {
            Format.upToWithQuotes(string, position, buffer, '}', '{');
        } catch (IllegalArgumentException e) {
            // ignored
        }
        return new ChoiceFormat(buffer.toString());
    }

    /**
     * Sets the specified Format used by this MessageFormat.
     * 
     * @param offset
     *            the format to change
     * @param format
     *            the Format
     */
    public void setFormat(int offset, Format format) {
        formats[offset] = format;
    }

    /**
     * Sets the Formats used by this MessageFormat.
     * 
     * @param formats
     *            an array of Format
     */
    public void setFormats(Format[] formats) {
        int min = this.formats.length;
        if (formats.length < min) {
            min = formats.length;
        }
        for (int i = 0; i < min; i++) {
            this.formats[i] = formats[i];
        }
    }

    /**
     * Sets the Locale to use when creating Formats.
     * 
     * @param locale
     *            the Locale
     */
    public void setLocale(Locale locale) {
        this.locale = locale;
        for (int i = 0; i <= maxOffset; i++) {
            Format format = formats[i];
            // BEGIN android-removed
            //if (format instanceof DecimalFormat) {
            //     formats[i] = new DecimalFormat(((DecimalFormat) format)
            //             .toPattern(), new DecimalFormatSymbols(locale));                
            //} else if (format instanceof SimpleDateFormat) {
            //     formats[i] = new SimpleDateFormat(((SimpleDateFormat) format)
            //             .toPattern(), locale);
            //}
            // END android-removed
            // BEGIN android-added
            // java specification undefined for null argument, change into 
            // a more tolerant implementation
            if (format instanceof DecimalFormat) {
                try {
                    formats[i] = new DecimalFormat(((DecimalFormat) format)
                            .toPattern(), new DecimalFormatSymbols(locale));
                } catch (NullPointerException npe){
                    formats[i] = null;
                }
            } else if (format instanceof SimpleDateFormat) {
                try {
                    formats[i] = new SimpleDateFormat(((SimpleDateFormat) format)
                            .toPattern(), locale);
                } catch (NullPointerException npe) {
                    formats[i] = null;
                }
            }
            // END android-added
        }
    }

    private String decodeDecimalFormat(StringBuffer buffer, Format format) {
        buffer.append(",number"); //$NON-NLS-1$
        if (format.equals(NumberFormat.getNumberInstance(locale))) {
            // Empty block
        } else if (format.equals(NumberFormat.getIntegerInstance(locale))) {
            buffer.append(",integer"); //$NON-NLS-1$
        } else if (format.equals(NumberFormat.getCurrencyInstance(locale))) {
            buffer.append(",currency"); //$NON-NLS-1$
        } else if (format.equals(NumberFormat.getPercentInstance(locale))) {
            buffer.append(",percent"); //$NON-NLS-1$
        } else {
            buffer.append(',');
            return ((DecimalFormat) format).toPattern();
        }
        return null;
    }

    private String decodeSimpleDateFormat(StringBuffer buffer, Format format) {
        if (format.equals(DateFormat
                .getTimeInstance(DateFormat.DEFAULT, locale))) {
            buffer.append(",time"); //$NON-NLS-1$
        } else if (format.equals(DateFormat.getDateInstance(DateFormat.DEFAULT,
                locale))) {
            buffer.append(",date"); //$NON-NLS-1$
        } else if (format.equals(DateFormat.getTimeInstance(DateFormat.SHORT,
                locale))) {
            buffer.append(",time,short"); //$NON-NLS-1$
        } else if (format.equals(DateFormat.getDateInstance(DateFormat.SHORT,
                locale))) {
            buffer.append(",date,short"); //$NON-NLS-1$
        } else if (format.equals(DateFormat.getTimeInstance(DateFormat.LONG,
                locale))) {
            buffer.append(",time,long"); //$NON-NLS-1$
        } else if (format.equals(DateFormat.getDateInstance(DateFormat.LONG,
                locale))) {
            buffer.append(",date,long"); //$NON-NLS-1$
        } else if (format.equals(DateFormat.getTimeInstance(DateFormat.FULL,
                locale))) {
            buffer.append(",time,full"); //$NON-NLS-1$
        } else if (format.equals(DateFormat.getDateInstance(DateFormat.FULL,
                locale))) {
            buffer.append(",date,full"); //$NON-NLS-1$
        } else {
            buffer.append(",date,"); //$NON-NLS-1$
            return ((SimpleDateFormat) format).toPattern();
        }
        return null;
    }

    /**
     * Returns the pattern of this MessageFormat.
     * 
     * @return the pattern
     */
    public String toPattern() {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i <= maxOffset; i++) {
            appendQuoted(buffer, strings[i]);
            buffer.append('{');
            buffer.append(argumentNumbers[i]);
            Format format = formats[i];
            String pattern = null;
            if (format instanceof ChoiceFormat) {
                buffer.append(",choice,"); //$NON-NLS-1$
                pattern = ((ChoiceFormat) format).toPattern();
            } else if (format instanceof DecimalFormat) {
                pattern = decodeDecimalFormat(buffer, format);
            } else if (format instanceof SimpleDateFormat) {
                pattern = decodeSimpleDateFormat(buffer, format);
            } else if (format != null) {
                // text.17=Unknown format
                throw new IllegalArgumentException(Messages
                        .getString("text.17")); //$NON-NLS-1$
            }
            if (pattern != null) {
                boolean quote = false;
                int index = 0, length = pattern.length(), count = 0;
                while (index < length) {
                    char ch = pattern.charAt(index++);
                    if (ch == '\'') {
                        quote = !quote;
                    }
                    if (!quote) {
                        if (ch == '{') {
                            count++;
                        }
                        if (ch == '}') {
                            if (count > 0) {
                                count--;
                            } else {
                                buffer.append("'}"); //$NON-NLS-1$
                                ch = '\'';
                            }
                        }
                    }
                    buffer.append(ch);
                }
            }
            buffer.append('}');
        }
        if (maxOffset + 1 < strings.length) {
            appendQuoted(buffer, strings[maxOffset + 1]);
        }
        return buffer.toString();
    }

    private void appendQuoted(StringBuffer buffer, String string) {
        int length = string.length();
        for (int i = 0; i < length; i++) {
            char ch = string.charAt(i);
            if (ch == '{' || ch == '}') {
                buffer.append('\'');
                buffer.append(ch);
                buffer.append('\'');
            } else {
                buffer.append(ch);
            }
        }
    }

    private static final ObjectStreamField[] serialPersistentFields = {
            new ObjectStreamField("argumentNumbers", int[].class), //$NON-NLS-1$
            new ObjectStreamField("formats", Format[].class), //$NON-NLS-1$
            new ObjectStreamField("locale", Locale.class), //$NON-NLS-1$
            new ObjectStreamField("maxOffset", Integer.TYPE), //$NON-NLS-1$
            new ObjectStreamField("offsets", int[].class), //$NON-NLS-1$
            new ObjectStreamField("pattern", String.class), }; //$NON-NLS-1$

    private void writeObject(ObjectOutputStream stream) throws IOException {
        ObjectOutputStream.PutField fields = stream.putFields();
        fields.put("argumentNumbers", argumentNumbers); //$NON-NLS-1$
        Format[] compatibleFormats = formats;
        fields.put("formats", compatibleFormats); //$NON-NLS-1$
        fields.put("locale", locale); //$NON-NLS-1$
        fields.put("maxOffset", maxOffset); //$NON-NLS-1$
        int offset = 0;
        int offsetsLength = maxOffset + 1;
        int[] offsets = new int[offsetsLength];
        StringBuffer pattern = new StringBuffer();
        for (int i = 0; i <= maxOffset; i++) {
            offset += strings[i].length();
            offsets[i] = offset;
            pattern.append(strings[i]);
        }
        if (maxOffset + 1 < strings.length) {
            pattern.append(strings[maxOffset + 1]);
        }
        fields.put("offsets", offsets); //$NON-NLS-1$
        fields.put("pattern", pattern.toString()); //$NON-NLS-1$
        stream.writeFields();
    }

    private void readObject(ObjectInputStream stream) throws IOException,
            ClassNotFoundException {
        ObjectInputStream.GetField fields = stream.readFields();
        argumentNumbers = (int[]) fields.get("argumentNumbers", null); //$NON-NLS-1$
        formats = (Format[]) fields.get("formats", null); //$NON-NLS-1$
        locale = (Locale) fields.get("locale", null); //$NON-NLS-1$
        maxOffset = fields.get("maxOffset", 0); //$NON-NLS-1$
        int[] offsets = (int[]) fields.get("offsets", null); //$NON-NLS-1$
        String pattern = (String) fields.get("pattern", null); //$NON-NLS-1$
        int length;
        if (maxOffset < 0) {
            length = pattern.length() > 0 ? 1 : 0;
        } else {
            length = maxOffset
                    + (offsets[maxOffset] == pattern.length() ? 1 : 2);
        }
        strings = new String[length];
        int last = 0;
        for (int i = 0; i <= maxOffset; i++) {
            strings[i] = pattern.substring(last, offsets[i]);
            last = offsets[i];
        }
        if (maxOffset + 1 < strings.length) {
            strings[strings.length - 1] = pattern.substring(last, pattern
                    .length());
        }
    }

    /**
     * The instances of this inner class are used as attribute keys in
     * AttributedCharacterIterator that
     * MessageFormat.formatToCharacterIterator() method returns.
     * <p>
     * There is no public constructor to this class, the only instances are the
     * constants defined here.
     */
    public static class Field extends Format.Field {

        private static final long serialVersionUID = 7899943957617360810L;

        /**
         * This constant stands for the message argument.
         */
        public static final Field ARGUMENT = new Field("message argument field"); //$NON-NLS-1$

        /**
         * Constructs a new instance of MessageFormat.Field with the given field
         * name.
         * @param fieldName The field name.
         */
        protected Field(String fieldName) {
            super(fieldName);
        }

        /**
         * serialization method resolve instances to the constant
         * MessageFormat.Field values
         */
        @Override
        protected Object readResolve() throws InvalidObjectException {
            String name = this.getName();
            if (name == null) {
                // text.18=Not a valid {0}, subclass should override
                // readResolve()
                throw new InvalidObjectException(Messages.getString(
                        "text.18", "MessageFormat.Field")); //$NON-NLS-1$ //$NON-NLS-2$
            }

            if (name.equals(ARGUMENT.getName())) {
                return ARGUMENT;
            }
            // text.18=Not a valid {0}, subclass should override readResolve()
            throw new InvalidObjectException(Messages.getString(
                    "text.18", "MessageFormat.Field")); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

}
