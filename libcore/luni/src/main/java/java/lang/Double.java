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
 * Double is the wrapper for the primitive type <code>double</code>.
 * </p>
 * 
 * @see java.lang.Number
 * @since 1.0
 */
public final class Double extends Number implements Comparable<Double> {

    private static final long serialVersionUID = -9172774392245257468L;

    /**
     * The value which the receiver represents.
     */
    private final double value;

    /**
     * <p>
     * Constant for the maximum <code>double</code> value, (2 - 2<sup>-52/sup>) *
     * 2<sup>1023</sup>.
     * </p>
     */
    public static final double MAX_VALUE = 1.79769313486231570e+308;

    /**
     * <p>
     * Constant for the minimum <code>double</code> value, 2<sup>-1074</sup>.
     * </p>
     */
    public static final double MIN_VALUE = 5e-324;

    /* 4.94065645841246544e-324 gets rounded to 9.88131e-324 */

    /**
     * <p>
     * Constant for the Not-a-Number (NaN) value of the <code>double</code>
     * type.
     * </p>
     */
    public static final double NaN = 0.0 / 0.0;

    /**
     * <p>
     * Constant for the Positive Infinity value of the <code>double</code>
     * type.
     * </p>
     */
    public static final double POSITIVE_INFINITY = 1.0 / 0.0;

    /**
     * <p>
     * Constant for the Negative Infinity value of the <code>double</code>
     * type.
     * </p>
     */
    public static final double NEGATIVE_INFINITY = -1.0 / 0.0;

    /**
     * The java.lang.Class that represents this class.
     * 
     * @since 1.1
     */
    @SuppressWarnings("unchecked")
    public static final Class<Double> TYPE = (Class<Double>) new double[0]
            .getClass().getComponentType();

    // Note: This can't be set to "double.class", since *that* is
    // defined to be "java.lang.Double.TYPE";

    /**
     * <p>
     * Constant for the number of bits to represent a <code>double</code> in
     * two's compliment form.
     * </p>
     * 
     * @since 1.5
     */
    public static final int SIZE = 64;

    /**
     * Constructs a new instance of the receiver which represents the double
     * valued argument.
     * 
     * @param value
     *            the double to store in the new instance.
     */
    public Double(double value) {
        this.value = value;
    }

    /**
     * Constructs a new instance of this class given a string.
     * 
     * @param string
     *            a string representation of a double quantity.
     * @exception NumberFormatException
     *                if the argument could not be parsed as a double quantity.
     */
    public Double(String string) throws NumberFormatException {
        this(parseDouble(string));
    }

    /**
     * Compares the receiver with the Double parameter. NaN is equal to NaN, and
     * is greater than other double values. 0d is greater than -0d.
     * 
     * @param object
     *            the Double to compare to the receiver
     * 
     * @return Returns greater than zero when this.doubleValue() is greater than
     *         object.doubleValue(), zero when this.doubleValue() equals
     *         object.doubleValue(), and less than zero when this.doubleValue()
     *         is less than object.doubleValue()
     * 
     * @throws NullPointerException
     *             if <code>object</code> is <code>null</code>.
     * @since 1.2
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
     * Returns the binary representation of the argument, as a long.
     * 
     * @param value
     *            The double value to convert
     * @return the bits of the double.
     */
    public static native long doubleToLongBits(double value);

    /**
     * Returns the binary representation of the argument, as a long.
     * 
     * @param value
     *            The double value to convert
     * @return the bits of the double.
     */
    public static native long doubleToRawLongBits(double value);

    /**
     * Returns the receiver's value as a double.
     * 
     * @return the receiver's value
     */
    @Override
    public double doubleValue() {
        return value;
    }

    /**
     * Compares the argument to the receiver, and returns true if they represent
     * the <em>same</em> object using a class specific comparison. For
     * Doubles, the check verifies that the receiver's value's bit pattern
     * matches the bit pattern of the argument, which must also be a Double.
     * 
     * @param object
     *            the object to compare with this object
     * @return <code>true</code> if the object is the same as this object
     *         <code>false</code> if it is different from this object
     * @see #hashCode
     */
    @Override
    public boolean equals(Object object) {
        return (object == this)
                || (object instanceof Double)
                && (doubleToLongBits(this.value) == doubleToLongBits(((Double) object).value));
    }

    /**
     * Returns the float value which the receiver represents
     * 
     * @return float the value of the receiver.
     */
    @Override
    public float floatValue() {
        return (float) value;
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
        long v = doubleToLongBits(value);
        return (int) (v ^ (v >>> 32));
    }

    /**
     * Returns the receiver's value as an integer.
     * 
     * @return the receiver's value as an integer
     */
    @Override
    public int intValue() {
        return (int) value;
    }

    /**
     * Returns true if the receiver represents an infinite quantity, and false
     * otherwise.
     * 
     * @return <code>true</code> if the argument is positive or negative
     *         infinity <code>false</code> if it is not an infinite value
     */
    public boolean isInfinite() {
        return isInfinite(value);
    }

    /**
     * Returns true if the argument represents an infinite quantity, and false
     * otherwise.
     * 
     * @param d
     *            value to check for infinitness.
     * @return <code>true</code> if the argument is positive or negative
     *         infinity <code>false</code> if it is not an infinite value
     */
    public static boolean isInfinite(double d) {
        return (d == POSITIVE_INFINITY) || (d == NEGATIVE_INFINITY);
    }

    /**
     * Returns true if the receiver does not represent a valid float quantity.
     * 
     * @return <code>true</code> if the argument is Not A Number
     *         <code>false</code> if it is a (potentially infinite) float
     *         number
     */
    public boolean isNaN() {
        return isNaN(value);
    }

    /**
     * Returns true if the argument does not represent a valid double quantity.
     * 
     * @param d
     *            value to check for numberness.
     * @return <code>true</code> if the argument is Not A Number
     *         <code>false</code> if it is a (potentially infinite) double
     *         number
     */
    public static boolean isNaN(double d) {
        return d != d;
    }

    /**
     * Returns a double built from the binary representation given in the
     * argument.
     * 
     * @param bits
     *            the bits of the double
     * @return the double which matches the bits
     */
    public static native double longBitsToDouble(long bits);

    /**
     * Returns the long value which the receiver represents
     * 
     * @return long the value of the receiver.
     */
    @Override
    public long longValue() {
        return (long) value;
    }

    /**
     * Returns the double which matches the passed in string.
     * NumberFormatException is thrown if the string does not represent a valid
     * double.
     * 
     * @param string
     *            the value to convert
     * @return a double which would print as the argument
     */
    public static double parseDouble(String string)
            throws NumberFormatException {
        return org.apache.harmony.luni.util.FloatingPointParser
                .parseDouble(string);
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
     * Returns a string containing a concise, human-readable description of the
     * receiver.
     * 
     * @return a printable representation for the receiver.
     */
    @Override
    public String toString() {
        return Double.toString(value);
    }

    /**
     * Returns a string containing a printable representation of the argument.
     * 
     * @param d
     *            the double to print
     * @return a printable representation of the argument.
     */
    public static String toString(double d) {
        return org.apache.harmony.luni.util.NumberConverter.convert(d);
    }

    /**
     * Returns the double which matches the passed in string.
     * NumberFormatException is thrown if the string does not represent a valid
     * double.
     * 
     * @param string
     *            the value to convert
     * @return a double which would print as the argument
     */
    public static Double valueOf(String string) throws NumberFormatException {
        return new Double(parseDouble(string));
    }

    /**
     * Compares the two doubles. NaN is equal to NaN, and is greater than other
     * double values. 0d is greater than -0d.
     * 
     * @param double1
     *            the first value to compare
     * @param double2
     *            the second value to compare
     * 
     * @return Returns greater than zero when double1 is greater than double2,
     *         zero when double1 equals double2, and less than zero when double1
     *         is less than double2
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
     * <p>
     * Returns a <code>Double</code> instance for the <code>double</code>
     * value passed. This method is preferred over the constructor, as this
     * method may maintain a cache of instances.
     * </p>
     * 
     * @param d
     *            The double value.
     * @return A <code>Double</code> instance.
     * @since 1.5
     */
    public static Double valueOf(double d) {
        return new Double(d);
    }

    /**
     * <p>
     * Converts a <code>double</code> into a hexadecimal string
     * representation.
     * </p>
     * 
     * @param d
     *            The <code>double</code> to convert.
     * @return The hexadecimal string representation of <code>f</code>.
     * @since 1.5
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
