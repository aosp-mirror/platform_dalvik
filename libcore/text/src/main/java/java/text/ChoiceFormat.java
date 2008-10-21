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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * ChoiceFormat is used to associate strings with ranges of double values. The
 * strings and ranges are either specified using arrays or with a pattern which
 * is parsed to determine the Strings and ranges.
 */

public class ChoiceFormat extends NumberFormat {

    private static final long serialVersionUID = 1795184449645032964L;

    private double[] choiceLimits;

    private String[] choiceFormats;

    /**
     * Constructs a new ChoiceFormat with the specified ranges and associated
     * strings.
     * 
     * @param limits
     *            an array of double, the ranges are greater or equal to the
     *            value in lower index up to less than the value in the next
     *            higher index. The bounds of the lowest and highest indexes are
     *            negative and positive infinity.
     * @param formats
     *            the strings associated with the ranges. The lower bound of the
     *            associated range is at the same index as the string.
     */
    public ChoiceFormat(double[] limits, String[] formats) {
        setChoices(limits, formats);
    }

    /**
     * Constructs a new ChoiceFormat with the strings and ranges parsed from the
     * specified pattern.
     * 
     * @param template
     *            the pattern of strings and ranges
     * 
     * @exception IllegalArgumentException
     *                then an error occurs parsing the pattern
     */
    public ChoiceFormat(String template) {
        applyPattern(template);
    }

    /**
     * Parses the pattern to determine new strings and ranges for this
     * ChoiceFormat.
     * 
     * @param template
     *            the pattern of strings and ranges
     * 
     * @exception IllegalArgumentException
     *                then an error occurs parsing the pattern
     */
    public void applyPattern(String template) {
        double[] limits = new double[5];
        List<String> formats = new ArrayList<String>();
        int length = template.length(), limitCount = 0, index = 0;
        StringBuffer buffer = new StringBuffer();
        NumberFormat format = NumberFormat.getInstance(Locale.US);
        ParsePosition position = new ParsePosition(0);
        while (true) {
            index = skipWhitespace(template, index);
            if (index >= length) {
                if (limitCount == limits.length) {
                    choiceLimits = limits;
                } else {
                    choiceLimits = new double[limitCount];
                    System.arraycopy(limits, 0, choiceLimits, 0, limitCount);
                }
                choiceFormats = new String[formats.size()];
                for (int i = 0; i < formats.size(); i++) {
                    choiceFormats[i] = formats.get(i);
                }
                return;
            }

            position.setIndex(index);
            Number value = format.parse(template, position);
            index = skipWhitespace(template, position.getIndex());
            if (position.getErrorIndex() != -1 || index >= length) {
                // Fix Harmony 540
                choiceLimits = new double[0];
                choiceFormats = new String[0];
                return;
            }
            char ch = template.charAt(index++);
            if (limitCount == limits.length) {
                double[] newLimits = new double[limitCount * 2];
                System.arraycopy(limits, 0, newLimits, 0, limitCount);
                limits = newLimits;
            }
            double next;
            switch (ch) {
                case '#':
                case '\u2264':
                    next = value.doubleValue();
                    break;
                case '<':
                    next = nextDouble(value.doubleValue());
                    break;
                default:
                    throw new IllegalArgumentException();
            }
            if (limitCount > 0 && next <= limits[limitCount - 1]) {
                throw new IllegalArgumentException();
            }
            buffer.setLength(0);
            position.setIndex(index);
            upTo(template, position, buffer, '|');
            index = position.getIndex();
            limits[limitCount++] = next;
            formats.add(buffer.toString());
        }
    }

    /**
     * Returns a new instance of ChoiceFormat with the same ranges and strings
     * as this ChoiceFormat.
     * 
     * @return a shallow copy of this ChoiceFormat
     * 
     * @see java.lang.Cloneable
     */
    @Override
    public Object clone() {
        ChoiceFormat clone = (ChoiceFormat) super.clone();
        clone.choiceLimits = choiceLimits.clone();
        clone.choiceFormats = choiceFormats.clone();
        return clone;
    }

    /**
     * Compares the specified object to this ChoiceFormat and answer if they are
     * equal. The object must be an instance of ChoiceFormat and have the same
     * limits and formats.
     * 
     * @param object
     *            the object to compare with this object
     * @return true if the specified object is equal to this ChoiceFormat, false
     *         otherwise
     * 
     * @see #hashCode
     */
    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof ChoiceFormat)) {
            return false;
        }
        ChoiceFormat choice = (ChoiceFormat) object;
        return Arrays.equals(choiceLimits, choice.choiceLimits)
                && Arrays.equals(choiceFormats, choice.choiceFormats);
    }

    /**
     * Appends to the specified StringBuffer the string associated with the
     * range in which the specified double value fits.
     * 
     * @param value
     *            the double to format
     * @param buffer
     *            the StringBuffer
     * @param field
     *            a FieldPosition which is ignored
     * @return the StringBuffer parameter <code>buffer</code>
     */
    @Override
    public StringBuffer format(double value, StringBuffer buffer,
            FieldPosition field) {
        for (int i = choiceLimits.length - 1; i >= 0; i--) {
            if (choiceLimits[i] <= value) {
                return buffer.append(choiceFormats[i]);
            }
        }
        return choiceFormats.length == 0 ? buffer : buffer
                .append(choiceFormats[0]);
    }

    /**
     * Appends to the specified StringBuffer the string associated with the
     * range in which the specified long value fits.
     * 
     * @param value
     *            the long to format
     * @param buffer
     *            the StringBuffer
     * @param field
     *            a FieldPosition which is ignored
     * @return the StringBuffer parameter <code>buffer</code>
     */
    @Override
    public StringBuffer format(long value, StringBuffer buffer,
            FieldPosition field) {
        return format((double) value, buffer, field);
    }

    /**
     * Returns the Strings associated with the ranges of this ChoiceFormat.
     * 
     * @return an array of String
     */
    public Object[] getFormats() {
        return choiceFormats;
    }

    /**
     * Returns the ranges of this ChoiceFormat.
     * 
     * @return an array of double, the ranges are greater or equal to the value
     *         in lower index up to less than the value in the next higher
     *         index. The bounds of the lowest and highest indexes are negative
     *         and positive infinity.
     */
    public double[] getLimits() {
        return choiceLimits;
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
        for (int i = 0; i < choiceLimits.length; i++) {
            long v = Double.doubleToLongBits(choiceLimits[i]);
            hashCode += (int) (v ^ (v >>> 32)) + choiceFormats[i].hashCode();
        }
        return hashCode;
    }

    /**
     * Returns the double value which is closest to the specified double but
     * larger.
     * 
     * @param value
     *            a double value
     * @return the next larger double value
     */
    public static final double nextDouble(double value) {
        if (value == Double.POSITIVE_INFINITY) {
            return value;
        }
        long bits;
        // Handle -0.0
        if (value == 0) {
            bits = 0;
        } else {
            bits = Double.doubleToLongBits(value);
        }
        return Double.longBitsToDouble(value < 0 ? bits - 1 : bits + 1);
    }

    /**
     * Returns the double value which is closest to the specified double but
     * either larger or smaller as specified.
     * 
     * @param value
     *            a double value
     * @param increment
     *            true to get a larger value, false to get a smaller value
     * @return the next larger or smaller double value
     */
    public static double nextDouble(double value, boolean increment) {
        return increment ? nextDouble(value) : previousDouble(value);
    }

    /**
     * Parse a Double from the specified String starting at the index specified
     * by the ParsePosition. The String is compared to the strings of this
     * ChoiceFormat and if a match occurs, the answer is the lower bound of the
     * corresponding range. If the string is successfully parsed, the index of
     * the ParsePosition is updated to the index following the parsed text.
     * 
     * @param string
     *            the String to parse
     * @param position
     *            the ParsePosition, updated on return with the index following
     *            the parsed text, or on error the index is unchanged and the
     *            error index is set to the index where the error occurred
     * @return a Double resulting from the parse, or Double.NaN if there is an
     *         error
     */
    @Override
    public Number parse(String string, ParsePosition position) {
        int offset = position.getIndex();
        for (int i = 0; i < choiceFormats.length; i++) {
            if (string.startsWith(choiceFormats[i], offset)) {
                position.setIndex(offset + choiceFormats[i].length());
                return new Double(choiceLimits[i]);
            }
        }
        position.setErrorIndex(offset);
        return new Double(Double.NaN);
    }

    /**
     * Returns the double value which is closest to the specified double but
     * smaller.
     * 
     * @param value
     *            a double value
     * @return the next smaller double value
     */
    public static final double previousDouble(double value) {
        if (value == Double.NEGATIVE_INFINITY) {
            return value;
        }
        long bits;
        // Handle 0.0
        if (value == 0) {
            bits = 0x8000000000000000L;
        } else {
            bits = Double.doubleToLongBits(value);
        }
        return Double.longBitsToDouble(value <= 0 ? bits + 1 : bits - 1);
    }

    /**
     * Sets the ranges and associated strings of this ChoiceFormat.
     * 
     * @param limits
     *            an array of double, the ranges are greater or equal to the
     *            value in lower index up to less than the value in the next
     *            higher index. The bounds of the lowest and highest indexes are
     *            negative and positive infinity.
     * @param formats
     *            the strings associated with the ranges. The lower bound of the
     *            range is at the same index as the string.
     */
    public void setChoices(double[] limits, String[] formats) {
        if (limits.length != formats.length) {
            throw new IllegalArgumentException();
        }
        choiceLimits = limits;
        choiceFormats = formats;
    }

    private int skipWhitespace(String string, int index) {
        int length = string.length();
        while (index < length && Character.isWhitespace(string.charAt(index))) {
            index++;
        }
        return index;
    }

    /**
     * Returns the pattern of this ChoiceFormat which specified the ranges and
     * their associated strings.
     * 
     * @return the pattern
     */
    public String toPattern() {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < choiceLimits.length; i++) {
            if (i != 0) {
                buffer.append('|');
            }
            String previous = String.valueOf(previousDouble(choiceLimits[i]));
            String limit = String.valueOf(choiceLimits[i]);
            if (previous.length() < limit.length()) {
                buffer.append(previous);
                buffer.append('<');
            } else {
                buffer.append(limit);
                buffer.append('#');
            }
            boolean quote = (choiceFormats[i].indexOf('|') != -1);
            if (quote) {
                buffer.append('\'');
            }
            buffer.append(choiceFormats[i]);
            if (quote) {
                buffer.append('\'');
            }
        }
        return buffer.toString();
    }
}
