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
 * The wrapper for the primitive type {@code double}.
 * 
 * @see java.lang.Number
 * @since Android 1.0
 */
public final class Double extends Number implements Comparable<Double> {

    private static final long serialVersionUID = -9172774392245257468L;

    /**
     * The value which the receiver represents.
     */
    private final double value;

    /**
     * Constant for the maximum {@code double} value, (2 - 2<sup>-52</sup>) *
     * 2<sup>1023</sup>.
     * 
     * @since Android 1.0
     */
    public static final double MAX_VALUE = 1.79769313486231570e+308;

    /**
     * Constant for the minimum {@code double} value, 2<sup>-1074</sup>.
     * 
     * @since Android 1.0
     */
    public static final double MIN_VALUE = 5e-324;

    /* 4.94065645841246544e-324 gets rounded to 9.88131e-324 */

    /**
     * Constant for the Not-a-Number (NaN) value of the {@code double} type.
     * 
     * @since Android 1.0
     */
    public static final double NaN = 0.0 / 0.0;

    /**
     * Constant for the Positive Infinity value of the {@code double} type.
     * 
     * @since Android 1.0
     */
    public static final double POSITIVE_INFINITY = 1.0 / 0.0;

    /**
     * Constant for the Negative Infinity value of the {@code double} type.
     * 
     * @since Android 1.0
     */
    public static final double NEGATIVE_INFINITY = -1.0 / 0.0;

    /**
     * The {@link Class} object that represents the primitive type {@code
     * double}.
     * 
     * @since Android 1.0
     */
    @SuppressWarnings("unchecked")
    public static final Class<Double> TYPE = (Class<Double>) new double[0]
            .getClass().getComponentType();

    // Note: This can't be set to "double.class", since *that* is
    // defined to be "java.lang.Double.TYPE";

    /**
     * Constant for the number of bits needed to represent a {@code double} in
     * two's complement form.
     * 
     * @since Android 1.0
     */
    public static final int SIZE = 64;

    /**
     * Constructs a new {@code Double} with the specified primitive double
     * value.
     * 
     * @param value
     *            the primitive double value to store in the new instance.
     * @since Android 1.0
     */
    public Double(double value) {
        this.value = value;
    }

    /**
     * Constructs a new {@code Double} from the specified string.
     * 
     * @param string
     *            the string representation of a double value.
     * @throws NumberFormatException
     *             if {@code string} can not be decoded into a double value.
     * @see #parseDouble(String)
     * @since Android 1.0
     */
    public Double(String string) throws NumberFormatException {
        this(parseDouble(string));
    }

    /**
     * Compares this object to the specified double object to determine their
     * relative order. There are two special cases:
     * <ul>
     * <li>{@code Double.NaN} is equal to {@code Double.NaN} and it is greater
     * than any other double value, including {@code Double.POSITIVE_INFINITY};</li>
     * <li>+0.0d is greater than -0.0d</li>
     * </ul>
     * 
     * @param object
     *            the double object to compare this object to.
     * @return a negative value if the value of this double is less than the
     *         value of {@code object}; 0 if the value of this double and the
     *         value of {@code object} are equal; a positive value if the value
     *         of this double is greater than the value of {@code object}.
     * @see java.lang.Comparable
     * @since Android 1.0
     */
    public int compareTo(Double object) {
        long d1, d2;
        long NaNbits = Double.doubleToLongBits(Double.NaN);
        if ((d1 = Double.doubleToLongBits(value)) == NaNbits) {
            if (Double.doubleToLongBits(object.value) == NaNbits) {
                return 0;
            }
            return 1;
        }
        if ((d2 = Double.doubleToLongBits(object.value)) == NaNbits) {
            return -1;
        }
        if (value == object.value) {
            if (d1 == d2) {
                return 0;
            }
            // check for -0
            return d1 > d2 ? 1 : -1;
        }
        return value > object.value ? 1 : -1;
    }

    @Override
    public byte byteValue() {
        return (byte) value;
    }

    /**
     * Converts the specified double value to a binary representation conforming
     * to the IEEE 754 floating-point double precision bit layout. All
     * <em>Not-a-Number (NaN)</em> values are converted to a single NaN
     * representation ({@code 0x7ff8000000000000L}).
     * 
     * @param value
     *            the double value to convert.
     * @return the IEEE 754 floating-point double precision representation of
     *         {@code value}.
     * @see #doubleToRawLongBits(double)
     * @see #longBitsToDouble(long)
     * @since Android 1.0
     */
    public static native long doubleToLongBits(double value);

    /**
     * Converts the specified double value to a binary representation conforming
     * to the IEEE 754 floating-point double precision bit layout.
     * <em>Not-a-Number (NaN)</em> values are preserved.
     * 
     * @param value
     *            the double value to convert.
     * @return the IEEE 754 floating-point double precision representation of
     *         {@code value}.
     * @see #doubleToLongBits(double)
     * @see #longBitsToDouble(long)
     * @since Android 1.0
     */
    public static native long doubleToRawLongBits(double value);

    /**
     * Gets the primitive value of this double.
     * 
     * @return this object's primitive value.
     * @since Android 1.0
     */
    @Override
    public double doubleValue() {
        return value;
    }

    /**
     * Compares this object with the specified object and indicates if they are
     * equal. In order to be equal, {@code object} must be an instance of
     * {@code Double} and the bit pattern of its double value is the same as
     * this object's.
     * 
     * @param object
     *            the object to compare this double with.
     * @return {@code true} if the specified object is equal to this
     *         {@code Double}; {@code false} otherwise.
     * @since Android 1.0
     */
    @Override
    public boolean equals(Object object) {
        return (object == this)
                || (object instanceof Double)
                && (doubleToLongBits(this.value) == doubleToLongBits(((Double) object).value));
    }

    @Override
    public float floatValue() {
        return (float) value;
    }

    @Override
    public int hashCode() {
        long v = doubleToLongBits(value);
        return (int) (v ^ (v >>> 32));
    }

    @Override
    public int intValue() {
        return (int) value;
    }

    /**
     * Indicates whether this object represents an infinite value.
     * 
     * @return {@code true} if the value of this double is positive or negative
     *         infinity; {@code false} otherwise.
     * @since Android 1.0
     */
    public boolean isInfinite() {
        return isInfinite(value);
    }

    /**
     * Indicates whether the specified double represents an infinite value.
     * 
     * @param d
     *            the double to check.
     * @return {@code true} if the value of {@code d} is positive or negative
     *         infinity; {@code false} otherwise.
     * @since Android 1.0
     */
    public static boolean isInfinite(double d) {
        return (d == POSITIVE_INFINITY) || (d == NEGATIVE_INFINITY);
    }

    /**
     * Indicates whether this object is a <em>Not-a-Number (NaN)</em> value.
     * 
     * @return {@code true} if this double is <em>Not-a-Number</em>;
     *         {@code false} if it is a (potentially infinite) double number.
     * @since Android 1.0
     */
    public boolean isNaN() {
        return isNaN(value);
    }

    /**
     * Indicates whether the specified double is a <em>Not-a-Number (NaN)</em>
     * value.
     * 
     * @param d
     *            the double value to check.
     * @return {@code true} if {@code d} is <em>Not-a-Number</em>;
     *         {@code false} if it is a (potentially infinite) double number.
     * @since Android 1.0
     */
    public static boolean isNaN(double d) {
        return d != d;
    }

    /**
     * Converts the specified IEEE 754 floating-point double precision bit
     * pattern to a Java double value.
     * 
     * @param bits
     *            the IEEE 754 floating-point double precision representation of
     *            a double value.
     * @return the double value converted from {@code bits}.
     * @see #doubleToLongBits(double)
     * @see #doubleToRawLongBits(double)
     * @since Android 1.0
     */
    public static native double longBitsToDouble(long bits);

    @Override
    public long longValue() {
        return (long) value;
    }

    /**
     * Parses the specified string as a double value.
     * 
     * @param string
     *            the string representation of a double value.
     * @return the primitive double value represented by {@code string}.
     * @throws NumberFormatException
     *             if {@code string} is {@code null}, has a length of zero or
     *             can not be parsed as a double value.
     * @since Android 1.0
     */
    public static double parseDouble(String string)
            throws NumberFormatException {
        return org.apache.harmony.luni.util.FloatingPointParser
                .parseDouble(string);
    }

    @Override
    public short shortValue() {
        return (short) value;
    }

    @Override
    public String toString() {
        return Double.toString(value);
    }

    /**
     * Returns a string containing a concise, human-readable description of the
     * specified double value.
     * 
     * @param d
     *             the double to convert to a string.
     * @return a printable representation of {@code d}.
     * @since Android 1.0
     */
    public static String toString(double d) {
        return org.apache.harmony.luni.util.NumberConverter.convert(d);
    }

    /**
     * Parses the specified string as a double value.
     * 
     * @param string
     *            the string representation of a double value.
     * @return a {@code Double} instance containing the double value represented
     *         by {@code string}.
     * @throws NumberFormatException
     *             if {@code string} is {@code null}, has a length of zero or
     *             can not be parsed as a double value.
     * @see #parseDouble(String)
     * @since Android 1.0
     */
    public static Double valueOf(String string) throws NumberFormatException {
        return new Double(parseDouble(string));
    }

    /**
     * Compares the two specified double values. There are two special cases:
     * <ul>
     * <li>{@code Double.NaN} is equal to {@code Double.NaN} and it is greater
     * than any other double value, including {@code Double.POSITIVE_INFINITY};</li>
     * <li>+0.0d is greater than -0.0d</li>
     * </ul>
     * 
     * @param double1
     *            the first value to compare.
     * @param double2
     *            the second value to compare.
     * @return a negative value if {@code double1} is less than {@code double2};
     *         0 if {@code double1} and {@code double2} are equal; a positive
     *         value if {@code double1} is greater than {@code double2}.
     * @since Android 1.0         
     */
    public static int compare(double double1, double double2) {
        long d1, d2;
        long NaNbits = Double.doubleToLongBits(Double.NaN);
        if ((d1 = Double.doubleToLongBits(double1)) == NaNbits) {
            if (Double.doubleToLongBits(double2) == NaNbits) {
                return 0;
            }
            return 1;
        }
        if ((d2 = Double.doubleToLongBits(double2)) == NaNbits) {
            return -1;
        }
        if (double1 == double2) {
            if (d1 == d2) {
                return 0;
            }
            // check for -0
            return d1 > d2 ? 1 : -1;
        }
        return double1 > double2 ? 1 : -1;
    }

    /**
     * Returns a {@code Double} instance for the specified double value.
     * 
     * @param d
     *            the double value to store in the instance.
     * @return a {@code Double} instance containing {@code d}.
     * @since Android 1.0
     */
    public static Double valueOf(double d) {
        return new Double(d);
    }

    /**
     * Converts the specified double into its hexadecimal string representation.
     * 
     * @param d
     *            the double to convert.
     * @return the hexadecimal string representation of {@code d}.
     * @since Android 1.0
     */
    public static String toHexString(double d) {
        /*
         * Reference: http://en.wikipedia.org/wiki/IEEE_754
         */
        if (d != d) {
            return "NaN"; //$NON-NLS-1$
        }
        if (d == POSITIVE_INFINITY) {
            return "Infinity"; //$NON-NLS-1$
        }
        if (d == NEGATIVE_INFINITY) {
            return "-Infinity"; //$NON-NLS-1$
        }

        long bitValue = doubleToLongBits(d);

        boolean negative = (bitValue & 0x8000000000000000L) != 0;
        // mask exponent bits and shift down
        long exponent = (bitValue & 0x7FF0000000000000L) >>> 52;
        // mask significand bits and shift up
        long significand = bitValue & 0x000FFFFFFFFFFFFFL;

        if (exponent == 0 && significand == 0) {
            return (negative ? "-0x0.0p0" : "0x0.0p0"); //$NON-NLS-1$ //$NON-NLS-2$
        }

        StringBuilder hexString = new StringBuilder(10);
        if (negative) {
            hexString.append("-0x"); //$NON-NLS-1$
        } else {
            hexString.append("0x"); //$NON-NLS-1$
        }

        if (exponent == 0) { // denormal (subnormal) value
            hexString.append("0."); //$NON-NLS-1$
            // significand is 52-bits, so there can be 13 hex digits
            int fractionDigits = 13;
            // remove trailing hex zeros, so Integer.toHexString() won't print
            // them
            while ((significand != 0) && ((significand & 0xF) == 0)) {
                significand >>>= 4;
                fractionDigits--;
            }
            // this assumes Integer.toHexString() returns lowercase characters
            String hexSignificand = Long.toHexString(significand);

            // if there are digits left, then insert some '0' chars first
            if (significand != 0 && fractionDigits > hexSignificand.length()) {
                int digitDiff = fractionDigits - hexSignificand.length();
                while (digitDiff-- != 0) {
                    hexString.append('0');
                }
            }
            hexString.append(hexSignificand);
            hexString.append("p-1022"); //$NON-NLS-1$
        } else { // normal value
            hexString.append("1."); //$NON-NLS-1$
            // significand is 52-bits, so there can be 13 hex digits
            int fractionDigits = 13;
            // remove trailing hex zeros, so Integer.toHexString() won't print
            // them
            while ((significand != 0) && ((significand & 0xF) == 0)) {
                significand >>>= 4;
                fractionDigits--;
            }
            // this assumes Integer.toHexString() returns lowercase characters
            String hexSignificand = Long.toHexString(significand);

            // if there are digits left, then insert some '0' chars first
            if (significand != 0 && fractionDigits > hexSignificand.length()) {
                int digitDiff = fractionDigits - hexSignificand.length();
                while (digitDiff-- != 0) {
                    hexString.append('0');
                }
            }

            hexString.append(hexSignificand);
            hexString.append('p');
            // remove exponent's 'bias' and convert to a string
            hexString.append(Long.toString(exponent - 1023));
        }
        return hexString.toString();
    }
}
