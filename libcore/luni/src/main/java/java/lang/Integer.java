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

// BEGIN android-note
// Reimiplemented toString, bit-twiddling, etc. Faster and cleaner.
// BEGIN android-note

package java.lang;

/**
 * The wrapper for the primitive type {@code int}.
 * <p>
 * Implementation note: The "bit twiddling" methods in this class use techniques
 * described in <a href="http://www.hackersdelight.org/">Henry S. Warren,
 * Jr.'s Hacker's Delight, (Addison Wesley, 2002)</a> and <a href=
 * "http://graphics.stanford.edu/~seander/bithacks.html">Sean Anderson's
 * Bit Twiddling Hacks.</a>
 *
 * @see java.lang.Long
 * @since 1.0
 */
public final class Integer extends Number implements Comparable<Integer> {

    private static final long serialVersionUID = 1360826667806852920L;

    /**
     * The int value represented by this Integer
     */
    private final int value;

    /**
     * Constant for the maximum {@code int} value, 2<sup>31</sup>-1.
     */
    public static final int MAX_VALUE = 0x7FFFFFFF;

    /**
     * Constant for the minimum {@code int} value, -2<sup>31</sup>.
     */
    public static final int MIN_VALUE = 0x80000000;

    /**
     * Constant for the number of bits needed to represent an {@code int} in
     * two's complement form.
     *
     * @since 1.5
     */
    public static final int SIZE = 32;

    /**
     * These tables are used to special-case toString computation for
     * small values.  This serves three purposes: it reduces memory usage;
     * it increases performance for small values; and it decreases the
     * number of comparisons required to do the length computation.
     * Elements of this table are lazily initialized on first use.
     * No locking is necessary, i.e., we use the non-volatile, racy
     * single-check idiom.
     */
    private static final String[] SMALL_NONNEGATIVE_VALUES = new String[100];
    private static final String[] SMALL_NEGATIVE_VALUES = new String[100];

    /** TENS[i] contains the tens digit of the number i, 0 <= i <= 99. */
    static final char[] TENS = {
        '0', '0', '0', '0', '0', '0', '0', '0', '0', '0',
        '1', '1', '1', '1', '1', '1', '1', '1', '1', '1',
        '2', '2', '2', '2', '2', '2', '2', '2', '2', '2',
        '3', '3', '3', '3', '3', '3', '3', '3', '3', '3',
        '4', '4', '4', '4', '4', '4', '4', '4', '4', '4',
        '5', '5', '5', '5', '5', '5', '5', '5', '5', '5',
        '6', '6', '6', '6', '6', '6', '6', '6', '6', '6',
        '7', '7', '7', '7', '7', '7', '7', '7', '7', '7',
        '8', '8', '8', '8', '8', '8', '8', '8', '8', '8',
        '9', '9', '9', '9', '9', '9', '9', '9', '9', '9'
    };

    /** Ones [i] contains the tens digit of the number i, 0 <= i <= 99. */
    static final char[] ONES = {
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
    };

    /**
     * The digits for all supported radices.
     */
    static final char[] DIGITS = {
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j',
        'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't',
        'u', 'v', 'w', 'x', 'y', 'z'
    };

    /**
     * Table for Seal's algorithm for Number of Trailing Zeros. Hacker's Delight
     * online, Figure 5-18 (http://www.hackersdelight.org/revisions.pdf)
     * The entries whose value is -1 are never referenced.
     */
    private static final byte NTZ_TABLE[] = {
        32,  0,  1, 12,  2,  6, -1, 13,   3, -1,  7, -1, -1, -1, -1, 14,
        10,  4, -1, -1,  8, -1, -1, 25,  -1, -1, -1, -1, -1, 21, 27, 15,
        31, 11,  5, -1, -1, -1, -1, -1,   9, -1, -1, 24, -1, -1, 20, 26,
        30, -1, -1, -1, -1, 23, -1, 19,  29, -1, 22, 18, 28, 17, 16, -1
    };

    /**
     * The {@link Class} object that represents the primitive type {@code int}.
     */
    @SuppressWarnings("unchecked")
    public static final Class<Integer> TYPE
            = (Class<Integer>) int[].class.getComponentType();
    // Note: Integer.TYPE can't be set to "int.class", since *that* is
    // defined to be "java.lang.Integer.TYPE";

    /**
     * Constructs a new {@code Integer} with the specified primitive integer
     * value.
     *
     * @param value
     *            the primitive integer value to store in the new instance.
     */
    public Integer(int value) {
        this.value = value;
    }

    /**
     * Constructs a new {@code Integer} from the specified string.
     *
     * @param string
     *            the string representation of an integer value.
     * @throws NumberFormatException
     *             if {@code string} can not be decoded into an integer value.
     * @see #parseInt(String)
     */
    public Integer(String string) throws NumberFormatException {
        this(parseInt(string));
    }

    @Override
    public byte byteValue() {
        return (byte) value;
    }

    /**
     * Compares this object to the specified integer object to determine their
     * relative order.
     *
     * @param object
     *            the integer object to compare this object to.
     * @return a negative value if the value of this integer is less than the
     *         value of {@code object}; 0 if the value of this integer and the
     *         value of {@code object} are equal; a positive value if the value
     *         of this integer is greater than the value of {@code object}.
     * @see java.lang.Comparable
     * @since 1.2
     */
    public int compareTo(Integer object) {
        int thisValue = value;
        int thatValue = object.value;
        return thisValue < thatValue ? -1 : (thisValue == thatValue ? 0 : 1);
    }

    /**
     * Parses the specified string and returns a {@code Integer} instance if the
     * string can be decoded into an integer value. The string may be an
     * optional minus sign "-" followed by a hexadecimal ("0x..." or "#..."),
     * octal ("0..."), or decimal ("...") representation of an integer.
     *
     * @param string
     *            a string representation of an integer value.
     * @return an {@code Integer} containing the value represented by
     *         {@code string}.
     * @throws NumberFormatException
     *             if {@code string} can not be parsed as an integer value.
     */
    public static Integer decode(String string) throws NumberFormatException {
        int length = string.length(), i = 0;
        if (length == 0) {
            // BEGIN android-changed
            throw new NumberFormatException("unable to parse '"+string+"' as integer");
            // END android-changed
        }
        char firstDigit = string.charAt(i);
        boolean negative = firstDigit == '-';
        if (negative) {
            if (length == 1) {
                // BEGIN android-changed
                throw new NumberFormatException("unable to parse '"+string+"' as integer");
                // END android-changed
            }
            firstDigit = string.charAt(++i);
        }

        int base = 10;
        if (firstDigit == '0') {
            if (++i == length) {
                return valueOf(0);
            }
            if ((firstDigit = string.charAt(i)) == 'x' || firstDigit == 'X') {
                if (++i == length) {
                    // BEGIN android-changed
                    throw new NumberFormatException("unable to parse '"+string+"' as integer");
                    // END android-changed
                }
                base = 16;
            } else {
                base = 8;
            }
        } else if (firstDigit == '#') {
            if (++i == length) {
                // BEGIN android-changed
                throw new NumberFormatException("unable to parse '"+string+"' as integer");
                // END android-changed
            }
            base = 16;
        }

        int result = parse(string, i, base, negative);
        return valueOf(result);
    }

    @Override
    public double doubleValue() {
        return value;
    }

    /**
     * Compares this instance with the specified object and indicates if they
     * are equal. In order to be equal, {@code o} must be an instance of
     * {@code Integer} and have the same integer value as this object.
     *
     * @param o
     *            the object to compare this integer with.
     * @return {@code true} if the specified object is equal to this
     *         {@code Integer}; {@code false} otherwise.
     */
    @Override
    public boolean equals(Object o) {
        return o instanceof Integer && ((Integer) o).value == value;
    }

    @Override
    public float floatValue() {
        return value;
    }

    /**
     * Returns the {@code Integer} value of the system property identified by
     * {@code string}. Returns {@code null} if {@code string} is {@code null}
     * or empty, if the property can not be found or if its value can not be
     * parsed as an integer.
     *
     * @param string
     *            the name of the requested system property.
     * @return the requested property's value as an {@code Integer} or
     *         {@code null}.
     */
    public static Integer getInteger(String string) {
        if (string == null || string.length() == 0) {
            return null;
        }
        String prop = System.getProperty(string);
        if (prop == null) {
            return null;
        }
        try {
            return decode(prop);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    /**
     * Returns the {@code Integer} value of the system property identified by
     * {@code string}. Returns the specified default value if {@code string} is
     * {@code null} or empty, if the property can not be found or if its value
     * can not be parsed as an integer.
     *
     * @param string
     *            the name of the requested system property.
     * @param defaultValue
     *            the default value that is returned if there is no integer
     *            system property with the requested name.
     * @return the requested property's value as an {@code Integer} or the
     *         default value.
     */
    public static Integer getInteger(String string, int defaultValue) {
        if (string == null || string.length() == 0) {
            return valueOf(defaultValue);
        }
        String prop = System.getProperty(string);
        if (prop == null) {
            return valueOf(defaultValue);
        }
        try {
            return decode(prop);
        } catch (NumberFormatException ex) {
            return valueOf(defaultValue);
        }
    }

    /**
     * Returns the {@code Integer} value of the system property identified by
     * {@code string}. Returns the specified default value if {@code string} is
     * {@code null} or empty, if the property can not be found or if its value
     * can not be parsed as an integer.
     *
     * @param string
     *            the name of the requested system property.
     * @param defaultValue
     *            the default value that is returned if there is no integer
     *            system property with the requested name.
     * @return the requested property's value as an {@code Integer} or the
     *         default value.
     */
    public static Integer getInteger(String string, Integer defaultValue) {
        if (string == null || string.length() == 0) {
            return defaultValue;
        }
        String prop = System.getProperty(string);
        if (prop == null) {
            return defaultValue;
        }
        try {
            return decode(prop);
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    @Override
    public int hashCode() {
        return value;
    }

    /**
     * Gets the primitive value of this int.
     *
     * @return this object's primitive value.
     */
    @Override
    public int intValue() {
        return value;
    }

    @Override
    public long longValue() {
        return value;
    }

    /**
     * Parses the specified string as a signed decimal integer value. The ASCII
     * character \u002d ('-') is recognized as the minus sign.
     *
     * @param string
     *            the string representation of an integer value.
     * @return the primitive integer value represented by {@code string}.
     * @throws NumberFormatException
     *             if {@code string} is {@code null}, has a length of zero or
     *             can not be parsed as an integer value.
     */
    public static int parseInt(String string) throws NumberFormatException {
        return parseInt(string, 10);
    }

    /**
     * Parses the specified string as a signed integer value using the specified
     * radix. The ASCII character \u002d ('-') is recognized as the minus sign.
     *
     * @param string
     *            the string representation of an integer value.
     * @param radix
     *            the radix to use when parsing.
     * @return the primitive integer value represented by {@code string} using
     *         {@code radix}.
     * @throws NumberFormatException
     *             if {@code string} is {@code null} or has a length of zero,
     *             {@code radix < Character.MIN_RADIX},
     *             {@code radix > Character.MAX_RADIX}, or if {@code string}
     *             can not be parsed as an integer value.
     */
    public static int parseInt(String string, int radix)
            throws NumberFormatException {
        if (string == null || radix < Character.MIN_RADIX
                || radix > Character.MAX_RADIX) {
            // BEGIN android-changed
            throw new NumberFormatException("unable to parse '"+string+"' as integer");
            // END android-changed
        }
        int length = string.length(), i = 0;
        if (length == 0) {
            // BEGIN android-changed
            throw new NumberFormatException("unable to parse '"+string+"' as integer");
            // END android-changed
        }
        boolean negative = string.charAt(i) == '-';
        if (negative && ++i == length) {
            // BEGIN android-changed
            throw new NumberFormatException("unable to parse '"+string+"' as integer");
            // END android-changed
        }

        return parse(string, i, radix, negative);
    }

    private static int parse(String string, int offset, int radix,
            boolean negative) throws NumberFormatException {
        int max = Integer.MIN_VALUE / radix;
        int result = 0, length = string.length();
        while (offset < length) {
            int digit = Character.digit(string.charAt(offset++), radix);
            if (digit == -1) {
                // BEGIN android-changed
                throw new NumberFormatException("unable to parse '"+string+"' as integer");
                // END android-changed
            }
            if (max > result) {
                // BEGIN android-changed
                throw new NumberFormatException("unable to parse '"+string+"' as integer");
                // END android-changed
            }
            int next = result * radix - digit;
            if (next > result) {
                // BEGIN android-changed
                throw new NumberFormatException("unable to parse '"+string+"' as integer");
                // END android-changed
            }
            result = next;
        }
        if (!negative) {
            result = -result;
            if (result < 0) {
                // BEGIN android-changed
                throw new NumberFormatException("unable to parse '"+string+"' as integer");
                // END android-changed
            }
        }
        return result;
    }

    @Override
    public short shortValue() {
        return (short) value;
    }

    /**
     * Converts the specified integer into its binary string representation. The
     * returned string is a concatenation of '0' and '1' characters.
     *
     * @param i
     *            the integer to convert.
     * @return the binary string representation of {@code i}.
     */
    public static String toBinaryString(int i) {
        int bufLen = 32;  // Max number of binary digits in an int
        char[] buf = new char[bufLen];
        int cursor = bufLen;

        do {
            buf[--cursor] = (char) ((i & 1) + '0');
        }  while ((i >>>= 1) != 0);

        return new String(cursor, bufLen - cursor, buf);
    }

    /**
     * Converts the specified integer into its hexadecimal string
     * representation. The returned string is a concatenation of characters from
     * '0' to '9' and 'a' to 'f'.
     *
     * @param i
     *            the integer to convert.
     * @return the hexadecimal string representation of {@code i}.
     */
    public static String toHexString(int i) {
        int bufLen = 8;  // Max number of hex digits in an int
        char[] buf = new char[bufLen];
        int cursor = bufLen;

        do {
            buf[--cursor] = DIGITS[i & 0xF];
        } while ((i >>>= 4) != 0);

        return new String(cursor, bufLen - cursor, buf);
    }

    /**
     * Converts the specified integer into its octal string representation. The
     * returned string is a concatenation of characters from '0' to '7'.
     *
     * @param i
     *            the integer to convert.
     * @return the octal string representation of {@code i}.
     */
    public static String toOctalString(int i) {
        int bufLen = 11;  // Max number of octal digits in an int
        char[] buf = new char[bufLen];
        int cursor = bufLen;

        do {
            buf[--cursor] = (char) ((i & 7) + '0');
        } while ((i >>>= 3) != 0);

        return new String(cursor, bufLen - cursor, buf);
    }

    @Override
    public String toString() {
        return Integer.toString(value);
    }

    /**
     * Converts the specified integer into its decimal string representation.
     * The returned string is a concatenation of a minus sign if the number is
     * negative and characters from '0' to '9'.
     *
     * @param i
     *            the integer to convert.
     * @return the decimal string representation of {@code i}.
     */
    public static String toString(int i) {
        boolean negative = false;
        if (i < 0) {
            negative = true;
            i = -i;
            if (i < 100) {
                if (i < 0) // If -n is still negative, n is Integer.MIN_VALUE
                    return "-2147483648";
                String result = SMALL_NEGATIVE_VALUES[i];
                if (result == null) {
                    SMALL_NEGATIVE_VALUES[i] = result =
                            i < 10 ? stringOf('-', ONES[i])
                                    : stringOf('-', TENS[i], ONES[i]);
                }
                return result;
            }
        } else {
            if (i < 100) {
                String result = SMALL_NONNEGATIVE_VALUES[i];
                if (result == null) {
                    SMALL_NONNEGATIVE_VALUES[i] = result =
                        i < 10 ? stringOf(ONES[i]) : stringOf(TENS[i], ONES[i]);
                }
                return result;
            }
        }

        int bufLen = 11; // Max number of chars in result
        char[] buf = new char[bufLen];
        int cursor = bufLen;

        // Calculate digits two-at-a-time till remaining digits fit in 16 bits
        while (i >= (1 << 16)) {
            // Compute q = n/100 and r = n % 100 as per "Hacker's Delight" 10-8
            int q = (int) ((0x51EB851FL * i) >>> 37);
            // BEGIN android-changed
            int r = i - ((q << 6) + (q << 5) + (q << 2));  // int r = n - 100*q;
            // END android-changed

            buf[--cursor] = ONES[r];
            buf[--cursor] = TENS[r];
            i = q;
        }

        // Calculate remaining digits one-at-a-time for performance
        while (i != 0) {
            // Compute q = n/10 and r = n % 10 as per "Hacker's Delight" 10-8
            int q = (0xCCCD * i) >>> 19;
            // BEGIN android-changed
            int r = i - ((q << 3) + (q << 1));  // int r = n - 10 * q;
            // END android-changed

            buf[--cursor] = (char) (r + '0');
            i = q;
        }

        if (negative)
            buf[--cursor] = '-';

        return new String(cursor, bufLen - cursor, buf);
    }

    /**
     * Returns a string composed of the specified characters. Note that the
     * autoboxing does *not* result in an extra copy of the char array: we are
     * using a package-private string constructor that uses incorporates the
     * "autoboxing array" into the new string.
     */
    private static String stringOf(char... args) {
        return new String(0, args.length, args);
    }

    /**
     * Converts the specified integer into a string representation based on the
     * specified radix. The returned string is a concatenation of a minus sign
     * if the number is negative and characters from '0' to '9' and 'a' to 'z',
     * depending on the radix. If {@code radix} is not in the interval defined
     * by {@code Character.MIN_RADIX} and {@code Character.MAX_RADIX} then 10 is
     * used as the base for the conversion.
     *
     * @param i
     *            the integer to convert.
     * @param radix
     *            the base to use for the conversion.
     * @return the string representation of {@code i}.
     */
    public static String toString(int i, int radix) {
        if (radix < Character.MIN_RADIX || radix > Character.MAX_RADIX) {
            radix = 10;
        }
        if (radix == 10) {
            return toString(i);
        }

        /*
         * If i is positive, negate it. This is the opposite of what one might
         * expect. It is necessary because the range of the negative values is
         * strictly larger than that of the positive values: there is no
         * positive value corresponding to Integer.MIN_VALUE.
         */
        boolean negative = false;
        if (i < 0) {
            negative = true;
        } else {
            i = -i;
        }

        int bufLen = radix < 8 ? 33 : 12;  // Max chars in result (conservative)
        char[] buf = new char[bufLen];
        int cursor = bufLen;

        do {
            int q = i / radix;
            buf[--cursor] = DIGITS[radix * q - i];
            i = q;
        } while (i != 0);

        if (negative) {
            buf[--cursor] = '-';
        }

        return new String(cursor, bufLen - cursor, buf);
    }

    /**
     * Parses the specified string as a signed decimal integer value.
     *
     * @param string
     *            the string representation of an integer value.
     * @return an {@code Integer} instance containing the integer value
     *         represented by {@code string}.
     * @throws NumberFormatException
     *             if {@code string} is {@code null}, has a length of zero or
     *             can not be parsed as an integer value.
     * @see #parseInt(String)
     */
    public static Integer valueOf(String string) throws NumberFormatException {
        return valueOf(parseInt(string));
    }

    /**
     * Parses the specified string as a signed integer value using the specified
     * radix.
     *
     * @param string
     *            the string representation of an integer value.
     * @param radix
     *            the radix to use when parsing.
     * @return an {@code Integer} instance containing the integer value
     *         represented by {@code string} using {@code radix}.
     * @throws NumberFormatException
     *             if {@code string} is {@code null} or has a length of zero,
     *             {@code radix < Character.MIN_RADIX},
     *             {@code radix > Character.MAX_RADIX}, or if {@code string}
     *             can not be parsed as an integer value.
     * @see #parseInt(String, int)
     */
    public static Integer valueOf(String string, int radix)
            throws NumberFormatException {
        return valueOf(parseInt(string, radix));
    }

    /**
     * Determines the highest (leftmost) bit of the specified integer that is 1
     * and returns the bit mask value for that bit. This is also referred to as
     * the Most Significant 1 Bit. Returns zero if the specified integer is
     * zero.
     *
     * @param i
     *            the integer to examine.
     * @return the bit mask indicating the highest 1 bit in {@code i}.
     * @since 1.5
     */
    public static int highestOneBit(int i) {
        // Hacker's Delight, Figure 3-1
        i |= (i >> 1);
        i |= (i >> 2);
        i |= (i >> 4);
        i |= (i >> 8);
        i |= (i >> 16);
        return i - (i >>> 1);
    }

    /**
     * Determines the lowest (rightmost) bit of the specified integer that is 1
     * and returns the bit mask value for that bit. This is also referred
     * to as the Least Significant 1 Bit. Returns zero if the specified integer
     * is zero.
     *
     * @param i
     *            the integer to examine.
     * @return the bit mask indicating the lowest 1 bit in {@code i}.
     * @since 1.5
     */
    public static int lowestOneBit(int i) {
        return i & -i;
    }

    /**
     * Determines the number of leading zeros in the specified integer prior to
     * the {@link #highestOneBit(int) highest one bit}.
     *
     * @param i
     *            the integer to examine.
     * @return the number of leading zeros in {@code i}.
     * @since 1.5
     */
    public static int numberOfLeadingZeros(int i) {
        // Hacker's Delight, Figure 5-6
        if (i <= 0) {
            return (~i >> 26) & 32;
        }
        int n = 1;
        if (i >> 16 == 0) {
            n +=  16;
            i <<= 16;
        }
        if (i >> 24 == 0) {
            n +=  8;
            i <<= 8;
        }
        if (i >> 28 == 0) {
            n +=  4;
            i <<= 4;
        }
        if (i >> 30 == 0) {
            n +=  2;
            i <<= 2;
        }
        return n - (i >>> 31);
    }

    /**
     * Determines the number of trailing zeros in the specified integer after
     * the {@link #lowestOneBit(int) lowest one bit}.
     *
     * @param i
     *            the integer to examine.
     * @return the number of trailing zeros in {@code i}.
     * @since 1.5
     */
    public static int numberOfTrailingZeros(int i) {
        // Seal's algorithm - Hacker's Delight 5-18
        // BEGIN android-changed - Harmony version should be one-liner in comment below
        i &= -i;
        i = (i <<  4) + i;    // x *= 17
        i = (i <<  6) + i;    // x *= 65
        i = (i << 16) - i;    // x *= 65535
        return NTZ_TABLE[i >>> 26]; // NTZ_TABLE[((i & -i) * 0x0450FBAF) >>> 26]
        // END android-changed
    }

    /**
     * Counts the number of 1 bits in the specified integer; this is also
     * referred to as population count.
     *
     * @param i
     *            the integer to examine.
     * @return the number of 1 bits in {@code i}.
     * @since 1.5
     */
    public static int bitCount(int i) {
        // Hacker's Delight, Figure 5-2
        i -= (i >> 1) & 0x55555555;
        i = (i & 0x33333333) + ((i >> 2) & 0x33333333);
        i = ((i >> 4) + i) & 0x0F0F0F0F;
        i += i >> 8;
        i += i >> 16;
        return i & 0x0000003F;
    }

    /**
     * Rotates the bits of the specified integer to the left by the specified
     * number of bits.
     *
     * @param i
     *            the integer value to rotate left.
     * @param distance
     *            the number of bits to rotate.
     * @return the rotated value.
     * @since 1.5
     */
    public static int rotateLeft(int i, int distance) {
        // Shift distances are mod 32 (JLS3 15.19), so we needn't mask -distance
        return (i << distance) | (i >>> -distance);
    }

    /**
     * Rotates the bits of the specified integer to the right by the specified
     * number of bits.
     *
     * @param i
     *            the integer value to rotate right.
     * @param distance
     *            the number of bits to rotate.
     * @return the rotated value.
     * @since 1.5
     */
    public static int rotateRight(int i, int distance) {
        // Shift distances are mod 32 (JLS3 15.19), so we needn't mask -distance
        return (i >>> distance) | (i << -distance);
    }

    /**
     * Reverses the order of the bytes of the specified integer.
     *
     * @param i
     *            the integer value for which to reverse the byte order.
     * @return the reversed value.
     * @since 1.5
     */
    public static int reverseBytes(int i) {
        // Hacker's Delight 7-1, with minor tweak from Veldmeijer
        // http://graphics.stanford.edu/~seander/bithacks.html
        i =    ((i >>>  8) & 0x00FF00FF) | ((i & 0x00FF00FF) <<  8);
        return ( i >>> 16              ) | ( i               << 16);
    }

    /**
     * Reverses the order of the bits of the specified integer.
     *
     * @param i
     *            the integer value for which to reverse the bit order.
     * @return the reversed value.
     * @since 1.5
     */
    public static int reverse(int i) {
        // Hacker's Delight 7-1, with minor tweak from Veldmeijer
        // http://graphics.stanford.edu/~seander/bithacks.html
        i =    ((i >>>  1) & 0x55555555) | ((i & 0x55555555) <<  1);
        i =    ((i >>>  2) & 0x33333333) | ((i & 0x33333333) <<  2);
        i =    ((i >>>  4) & 0x0F0F0F0F) | ((i & 0x0F0F0F0F) <<  4);
        i =    ((i >>>  8) & 0x00FF00FF) | ((i & 0x00FF00FF) <<  8);
        return ((i >>> 16)             ) | ((i             ) << 16);
    }

    /**
     * Returns the value of the {@code signum} function for the specified
     * integer.
     *
     * @param i
     *            the integer value to check.
     * @return -1 if {@code i} is negative, 1 if {@code i} is positive, 0 if
     *         {@code i} is zero.
     * @since 1.5
     */
    public static int signum(int i) {
        return (i >> 31) | (-i >>> 31); // Hacker's delight 2-7
    }

    /**
     * Returns a {@code Integer} instance for the specified integer value.
     * <p>
     * If it is not necessary to get a new {@code Integer} instance, it is
     * recommended to use this method instead of the constructor, since it
     * maintains a cache of instances which may result in better performance.
     *
     * @param i
     *            the integer value to store in the instance.
     * @return a {@code Integer} instance containing {@code i}.
     * @since 1.5
     */
    public static Integer valueOf(int i) {
        return  i >= 128 || i < -128 ? new Integer(i) : SMALL_VALUES[i + 128];
    }

    /**
     * A cache of instances used by {@link Integer#valueOf(int)} and auto-boxing
     */
    private static final Integer[] SMALL_VALUES = new Integer[256];

    static {
        for(int i = -128; i < 128; i++) {
            SMALL_VALUES[i + 128] = new Integer(i);
        }
    }
}
