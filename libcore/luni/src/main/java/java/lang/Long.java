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

package java.lang;

/**
 * <p>
 * Long is the wrapper for the primitive type <code>long</code>.
 * </p>
 * 
 * <p>
 * As with the specification, this implementation relied on code laid out in <a
 * href="http://www.hackersdelight.org/">Henry S. Warren, Jr.'s Hacker's
 * Delight, (Addison Wesley, 2002)</a> as well as <a
 * href="http://aggregate.org/MAGIC/">The Aggregate's Magic Algorithms</a>.
 * </p>
 * 
 * @see java.lang.Number
 * @since 1.0
 */
public final class Long extends Number implements Comparable<Long> {

    private static final long serialVersionUID = 4290774380558885855L;

    /**
     * The value which the receiver represents.
     */
    private final long value;

    /**
     * <p>
     * Constant for the maximum <code>long</code> value, 2<sup>63</sup>-1.
     * </p>
     */
    public static final long MAX_VALUE = 0x7FFFFFFFFFFFFFFFL;

    /**
     * <p>
     * Constant for the minimum <code>long</code> value, -2<sup>31</sup>.
     * </p>
     */
    public static final long MIN_VALUE = 0x8000000000000000L;

    /**
     * The java.lang.Class that represents this class.
     */
    @SuppressWarnings("unchecked")
    public static final Class<Long> TYPE = (Class<Long>) new long[0].getClass()
            .getComponentType();

    // Note: This can't be set to "long.class", since *that* is
    // defined to be "java.lang.Long.TYPE";

    /**
     * <p>
     * Constant for the number of bits to represent a <code>long</code> in
     * two's compliment form.
     * </p>
     * 
     * @since 1.5
     */
    public static final int SIZE = 64;


    /**
     * Constructs a new instance of the receiver which represents the long
     * valued argument.
     * 
     * @param value
     *            the long to store in the new instance.
     */
    public Long(long value) {
        this.value = value;
    }

    /**
     * Constructs a new instance of this class given a string.
     * 
     * @param string
     *            a string representation of an long quantity.
     * @exception NumberFormatException
     *                if the argument could not be parsed as a long quantity.
     */
    public Long(String string) throws NumberFormatException {
        this(parseLong(string));
    }

    /**
     * Returns the byte value which the receiver represents
     * 
     * @return byte the value of the receiver.
     */
    @Override
    public byte byteValue() {
        return (byte) value;
    }

    /**
     * <p>
     * Compares this <code>Long</code> to the <code>Long</code> passed. If
     * this instance's value is equal to the value of the instance passed, then
     * 0 is returned. If this instance's value is less than the value of the
     * instance passed, then a negative value is returned. If this instance's
     * value is greater than the value of the instance passed, then a positive
     * value is returned.
     * </p>
     * 
     * @param object
     *            The instance to compare to.
     * @throws NullPointerException
     *             if <code>object</code> is <code>null</code>.
     * @since 1.2
     */
    public int compareTo(Long object) {
        return value > object.value ? 1 : (value < object.value ? -1 : 0);
    }

    /**
     * Parses the string argument as if it was a long value and returns the
     * result. Throws NumberFormatException if the string does not represent a
     * long quantity. The string may be a hexadecimal ("0x..."), octal ("0..."),
     * or decimal ("...") representation of a long.
     * 
     * @param string
     *            a string representation of an long quantity.
     * @return Long the value represented by the argument
     * @exception NumberFormatException
     *                if the argument could not be parsed as an long quantity.
     */
    public static Long decode(String string) throws NumberFormatException {
        int length = string.length(), i = 0;
        if (length == 0) {
            throw new NumberFormatException();
        }
        char firstDigit = string.charAt(i);
        boolean negative = firstDigit == '-';
        if (negative) {
            if (length == 1) {
                throw new NumberFormatException(string);
            }
            firstDigit = string.charAt(++i);
        }

        int base = 10;
        if (firstDigit == '0') {
            if (++i == length) {
                return valueOf(0L);
            }
            if ((firstDigit = string.charAt(i)) == 'x' || firstDigit == 'X') {
                if (i == length) {
                    throw new NumberFormatException(string);
                }
                i++;
                base = 16;
            } else {
                base = 8;
            }
        } else if (firstDigit == '#') {
            if (i == length) {
                throw new NumberFormatException(string);
            }
            i++;
            base = 16;
        }

        long result = parse(string, i, base, negative);
        return valueOf(result);
    }

    /**
     * Returns the double value which the receiver represents
     * 
     * @return double the value of the receiver.
     */
    @Override
    public double doubleValue() {
        return value;
    }

    /**
     * Compares the argument to the receiver, and returns true if they represent
     * the <em>same</em> object using a class specific comparison.
     * <p>
     * In this case, the argument must also be an Long, and the receiver and
     * argument must represent the same long value.
     * 
     * @param o
     *            the object to compare with this object
     * @return <code>true</code> if the object is the same as this object
     *         <code>false</code> if it is different from this object
     * @see #hashCode
     */
    @Override
    public boolean equals(Object o) {
        return (o instanceof Long)
                && (value == ((Long) o).value);
    }

    /**
     * Returns the float value which the receiver represents
     * 
     * @return float the value of the receiver.
     */
    @Override
    public float floatValue() {
        return value;
    }

    /**
     * Returns a Long representing the long value of the property named by the
     * argument. If the property could not be found, or its value could not be
     * parsed as a long, answer null.
     * 
     * @param string
     *            The name of the desired integer property.
     * @return Long A Long representing the value of the property.
     */
    public static Long getLong(String string) {
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
     * Returns a Long representing the long value of the property named by the
     * argument. If the property could not be found, or its value could not be
     * parsed as a long, answer a Long representing the second argument.
     * 
     * @param string
     *            The name of the desired long property.
     * @return Long An Long representing the value of the property.
     */
    public static Long getLong(String string, long defaultValue) {
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
     * Returns an Long representing the long value of the property named by the
     * argument. If the property could not be found, or its value could not be
     * parsed as an long, answer the second argument.
     * 
     * @param string
     *            The name of the desired long property.
     * @return Long An Long representing the value of the property.
     */
    public static Long getLong(String string, Long defaultValue) {
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

    /**
     * Returns an integer hash code for the receiver. Any two objects which
     * answer <code>true</code> when passed to <code>equals</code> must
     * answer the same value for this method.
     * 
     * @return the receiver's hash
     * 
     * @see #equals
     */
    @Override
    public int hashCode() {
        return (int) (value ^ (value >>> 32));
    }

    /**
     * Returns the int value which the receiver represents
     * 
     * @return int the value of the receiver.
     */
    @Override
    public int intValue() {
        return (int) value;
    }

    /**
     * Returns the long value which the receiver represents
     * 
     * @return long the value of the receiver.
     */
    @Override
    public long longValue() {
        return value;
    }

    /**
     * Parses the string argument as if it was a long value and returns the
     * result. Throws NumberFormatException if the string does not represent a
     * long quantity.
     * 
     * @param string
     *            a string representation of a long quantity.
     * @return long the value represented by the argument
     * @exception NumberFormatException
     *                if the argument could not be parsed as a long quantity.
     */
    public static long parseLong(String string) throws NumberFormatException {
        return parseLong(string, 10);
    }

    /**
     * Parses the string argument as if it was an long value and returns the
     * result. Throws NumberFormatException if the string does not represent an
     * long quantity. The second argument specifies the radix to use when
     * parsing the value.
     * 
     * @param string
     *            a string representation of an long quantity.
     * @param radix
     *            the base to use for conversion.
     * @return long the value represented by the argument
     * @exception NumberFormatException
     *                if the argument could not be parsed as an long quantity.
     */
    public static long parseLong(String string, int radix)
            throws NumberFormatException {
        if (string == null || radix < Character.MIN_RADIX
                || radix > Character.MAX_RADIX) {
            throw new NumberFormatException();
        }
        int length = string.length(), i = 0;
        if (length == 0) {
            throw new NumberFormatException(string);
        }
        boolean negative = string.charAt(i) == '-';
        if (negative && ++i == length) {
            throw new NumberFormatException(string);
        }

        return parse(string, i, radix, negative);
    }

    private static long parse(String string, int offset, int radix,
            boolean negative) {
        long max = Long.MIN_VALUE / radix;
        long result = 0, length = string.length();
        while (offset < length) {
            int digit = Character.digit(string.charAt(offset++), radix);
            if (digit == -1) {
                throw new NumberFormatException(string);
            }
            if (max > result) {
                throw new NumberFormatException(string);
            }
            long next = result * radix - digit;
            if (next > result) {
                throw new NumberFormatException(string);
            }
            result = next;
        }
        if (!negative) {
            result = -result;
            if (result < 0) {
                throw new NumberFormatException(string);
            }
        }
        return result;
    }

    /**
     * Returns the short value which the receiver represents
     * 
     * @return short the value of the receiver.
     */
    @Override
    public short shortValue() {
        return (short) value;
    }

    /**
     * Returns a string containing '0' and '1' characters which describe the
     * binary representation of the argument.
     * 
     * @param l
     *            a long to get the binary representation of
     * @return String the binary representation of the argument
     */
    public static String toBinaryString(long l) {
        int count = 1;
        long j = l;

        if (l < 0) {
            count = 64;
        } else {
            while ((j >>= 1) != 0) {
                count++;
            }
        }

        char[] buffer = new char[count];
        do {
            buffer[--count] = (char) ((l & 1) + '0');
            l >>= 1;
        } while (count > 0);
        return new String(0, buffer.length, buffer);
    }

    /**
     * Returns a string containing characters in the range 0..7, a..f which
     * describe the hexadecimal representation of the argument.
     * 
     * @param l
     *            a long to get the hex representation of
     * @return String the hex representation of the argument
     */
    public static String toHexString(long l) {
        int count = 1;
        long j = l;

        if (l < 0) {
            count = 16;
        } else {
            while ((j >>= 4) != 0) {
                count++;
            }
        }

        char[] buffer = new char[count];
        do {
            int t = (int) (l & 15);
            if (t > 9) {
                t = t - 10 + 'a';
            } else {
                t += '0';
            }
            buffer[--count] = (char) t;
            l >>= 4;
        } while (count > 0);
        return new String(0, buffer.length, buffer);
    }

    /**
     * Returns a string containing characters in the range 0..7 which describe
     * the octal representation of the argument.
     * 
     * @param l
     *            a long to get the octal representation of
     * @return String the octal representation of the argument
     */
    public static String toOctalString(long l) {
        int count = 1;
        long j = l;

        if (l < 0) {
            count = 22;
        } else {
            while ((j >>>= 3) != 0) {
                count++;
            }
        }

        char[] buffer = new char[count];
        do {
            buffer[--count] = (char) ((l & 7) + '0');
            l >>>= 3;
        } while (count > 0);
        return new String(0, buffer.length, buffer);
    }

    /**
     * Returns a string containing a concise, human-readable description of the
     * receiver.
     * 
     * @return a printable representation for the receiver.
     */
    @Override
    public String toString() {
        return Long.toString(value);
    }

    /**
     * Returns a string containing characters in the range 0..9 which describe
     * the decimal representation of the argument.
     * 
     * @param l
     *            a long to get the representation of
     * @return String the representation of the argument
     */
    public static String toString(long l) {
        return toString(l, 10);
    }

    /**
     * Returns a string containing characters in the range 0..9, a..z (depending
     * on the radix) which describe the representation of the argument in that
     * radix.
     * 
     * @param l
     *            a long to get the representation of
     * @param radix
     *            the base to use for conversion.
     * @return String the representation of the argument
     */
    public static String toString(long l, int radix) {
        if (radix < Character.MIN_RADIX || radix > Character.MAX_RADIX) {
            radix = 10;
        }
        if (l == 0) {
            return "0"; //$NON-NLS-1$
        }

        int count = 2;
        long j = l;
        boolean negative = l < 0;
        if (!negative) {
            count = 1;
            j = -l;
        }
        while ((l /= radix) != 0) {
            count++;
        }

        char[] buffer = new char[count];
        do {
            int ch = 0 - (int) (j % radix);
            if (ch > 9) {
                ch = ch - 10 + 'a';
            } else {
                ch += '0';
            }
            buffer[--count] = (char) ch;
        } while ((j /= radix) != 0);
        if (negative) {
            buffer[0] = '-';
        }
        return new String(0, buffer.length, buffer);
    }

    /**
     * Parses the string argument as if it was an long value and returns the
     * result. Throws NumberFormatException if the string does not represent an
     * long quantity.
     * 
     * @param string
     *            a string representation of an long quantity.
     * @return Long the value represented by the argument
     * @exception NumberFormatException
     *                if the argument could not be parsed as an long quantity.
     */
    public static Long valueOf(String string) throws NumberFormatException {
        return valueOf(parseLong(string));
    }

    /**
     * Parses the string argument as if it was an long value and returns the
     * result. Throws NumberFormatException if the string does not represent an
     * long quantity. The second argument specifies the radix to use when
     * parsing the value.
     * 
     * @param string
     *            a string representation of an long quantity.
     * @param radix
     *            the base to use for conversion.
     * @return Long the value represented by the argument
     * @exception NumberFormatException
     *                if the argument could not be parsed as an long quantity.
     */
    public static Long valueOf(String string, int radix)
            throws NumberFormatException {
        return valueOf(parseLong(string, radix));
    }

    /**
     * <p>
     * Determines the highest (leftmost) bit that is 1 and returns the value
     * that is the bit mask for that bit. This is sometimes referred to as the
     * Most Significant 1 Bit.
     * </p>
     * 
     * @param lng
     *            The <code>long</code> to interrogate.
     * @return The bit mask indicating the highest 1 bit.
     * @since 1.5
     */
    public static long highestOneBit(long lng) {
        lng |= (lng >> 1);
        lng |= (lng >> 2);
        lng |= (lng >> 4);
        lng |= (lng >> 8);
        lng |= (lng >> 16);
        lng |= (lng >> 32);
        return (lng & ~(lng >>> 1));
    }

    /**
     * <p>
     * Determines the lowest (rightmost) bit that is 1 and returns the value
     * that is the bit mask for that bit. This is sometimes referred to as the
     * Least Significant 1 Bit.
     * </p>
     * 
     * @param lng
     *            The <code>long</code> to interrogate.
     * @return The bit mask indicating the lowest 1 bit.
     * @since 1.5
     */
    public static long lowestOneBit(long lng) {
        return (lng & (-lng));
    }

    /**
     * <p>
     * Determines the number of leading zeros in the <code>long</code> passed
     * prior to the {@link #highestOneBit(long) highest one bit}.
     * </p>
     * 
     * @param lng
     *            The <code>long</code> to process.
     * @return The number of leading zeros.
     * @since 1.5
     */
    public static int numberOfLeadingZeros(long lng) {
        lng |= lng >> 1;
        lng |= lng >> 2;
        lng |= lng >> 4;
        lng |= lng >> 8;
        lng |= lng >> 16;
        lng |= lng >> 32;
        return bitCount(~lng);
    }

    /**
     * <p>
     * Determines the number of trailing zeros in the <code>long</code> passed
     * after the {@link #lowestOneBit(long) lowest one bit}.
     * </p>
     * 
     * @param lng
     *            The <code>long</code> to process.
     * @return The number of trailing zeros.
     * @since 1.5
     */
    public static int numberOfTrailingZeros(long lng) {
        return bitCount((lng & -lng) - 1);
    }

    /**
     * <p>
     * Counts the number of 1 bits in the <code>long</code> value passed; this
     * is sometimes referred to as a population count.
     * </p>
     * 
     * @param lng
     *            The <code>long</code> value to process.
     * @return The number of 1 bits.
     * @since 1.5
     */
    public static int bitCount(long lng) {
        lng = (lng & 0x5555555555555555L) + ((lng >> 1) & 0x5555555555555555L);
        lng = (lng & 0x3333333333333333L) + ((lng >> 2) & 0x3333333333333333L);
        // adjust for 64-bit integer
        int i = (int) ((lng >>> 32) + lng);
        i = (i & 0x0F0F0F0F) + ((i >> 4) & 0x0F0F0F0F);
        i = (i & 0x00FF00FF) + ((i >> 8) & 0x00FF00FF);
        i = (i & 0x0000FFFF) + ((i >> 16) & 0x0000FFFF);
        return i;
    }

    /**
     * <p>
     * Rotates the bits of <code>lng</code> to the left by the
     * <code>distance</code> bits.
     * </p>
     * 
     * @param lng
     *            The <code>long</code> value to rotate left.
     * @param distance
     *            The number of bits to rotate.
     * @return The rotated value.
     * @since 1.5
     */
    public static long rotateLeft(long lng, int distance) {
        if (distance == 0) {
            return lng;
        }
        /*
         * According to JLS3, 15.19, the right operand of a shift is always
         * implicitly masked with 0x3F, which the negation of 'distance' is
         * taking advantage of.
         */
        return ((lng << distance) | (lng >>> (-distance)));
    }

    /**
     * <p>
     * Rotates the bits of <code>lng</code> to the right by the
     * <code>distance</code> bits.
     * </p>
     * 
     * @param lng
     *            The <code>long</code> value to rotate right.
     * @param distance
     *            The number of bits to rotate.
     * @return The rotated value.
     * @since 1.5
     */
    public static long rotateRight(long lng, int distance) {
        if (distance == 0) {
            return lng;
        }
        /*
         * According to JLS3, 15.19, the right operand of a shift is always
         * implicitly masked with 0x3F, which the negation of 'distance' is
         * taking advantage of.
         */
        return ((lng >>> distance) | (lng << (-distance)));
    }

    /**
     * <p>
     * Reverses the bytes of a <code>long</code>.
     * </p>
     * 
     * @param lng
     *            The <code>long</code> to reverse.
     * @return The reversed value.
     * @since 1.5
     */
    public static long reverseBytes(long lng) {
        long b7 = lng >>> 56;
        long b6 = (lng >>> 40) & 0xFF00L;
        long b5 = (lng >>> 24) & 0xFF0000L;
        long b4 = (lng >>> 8) & 0xFF000000L;
        long b3 = (lng & 0xFF000000L) << 8;
        long b2 = (lng & 0xFF0000L) << 24;
        long b1 = (lng & 0xFF00L) << 40;
        long b0 = lng << 56;
        return (b0 | b1 | b2 | b3 | b4 | b5 | b6 | b7);
    }

    /**
     * <p>
     * Reverses the bytes of a <code>long</code>.
     * </p>
     * 
     * @param lng
     *            The <code>long</code> to reverse.
     * @return The reversed value.
     * @since 1.5
     */
    public static long reverse(long lng) {
        // From Hacker's Delight, 7-1, Figure 7-1
        lng = (lng & 0x5555555555555555L) << 1 | (lng >> 1)
                & 0x5555555555555555L;
        lng = (lng & 0x3333333333333333L) << 2 | (lng >> 2)
                & 0x3333333333333333L;
        lng = (lng & 0x0F0F0F0F0F0F0F0FL) << 4 | (lng >> 4)
                & 0x0F0F0F0F0F0F0F0FL;
        return reverseBytes(lng);
    }

    /**
     * <p>
     * The <code>signum</code> function for <code>long</code> values. This
     * method returns -1 for negative values, 1 for positive values and 0 for
     * the value 0.
     * </p>
     * 
     * @param lng
     *            The <code>long</code> value.
     * @return -1 if negative, 1 if positive otherwise 0.
     * @since 1.5
     */
    public static int signum(long lng) {
        return (lng == 0 ? 0 : (lng < 0 ? -1 : 1));
    }

    /**
     * <p>
     * Returns a <code>Long</code> instance for the <code>long</code> value
     * passed. This method is preferred over the constructor, as this method may
     * maintain a cache of instances.
     * </p>
     * 
     * @param lng
     *            The long value.
     * @return A <code>Long</code> instance.
     * @since 1.5
     */
    public static Long valueOf(long lng) {
        if (lng < -128 || lng > 127) {
            return new Long(lng);
        }
        return valueOfCache.CACHE[128+(int)lng];
    }

    static class valueOfCache {
        /**
         * <p>
         * A cache of instances used by {@link Long#valueOf(long)} and auto-boxing.
         * </p>
         */
        static final Long[] CACHE = new Long[256];

        static {
            for(int i=-128; i<=127; i++) {
                CACHE[i+128] = new Long(i);
            }
        }
    }

}
