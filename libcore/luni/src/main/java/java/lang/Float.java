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
 * Float is the wrapper for the primitive type <code>float</code>.
 * </p>
 * 
 * @see java.lang.Number
 * @since 1.0
 */
public final class Float extends Number implements Comparable<Float> {

    private static final long serialVersionUID = -2671257302660747028L;

    /**
     * The value which the receiver represents.
     */
    private final float value;

    /**
     * <p>
     * Constant for the maximum <code>float</code> value, (2 - 2<sup>-23</sup>) *
     * 2<sup>127</sup>.
     * </p>
     */
    public static final float MAX_VALUE = 3.40282346638528860e+38f;

    /**
     * <p>
     * Constant for the minimum <code>float</code> value, 2<sup>-149</sup>.
     * </p>
     */
    public static final float MIN_VALUE = 1.40129846432481707e-45f;

    /**
     * <p>
     * Constant for the Not-a-Number (NaN) value of the <code>float</code>
     * type.
     * </p>
     */
    public static final float NaN = 0.0f / 0.0f;

    /**
     * <p>
     * Constant for the Positive Infinity value of the <code>float</code>
     * type.
     * </p>
     */
    public static final float POSITIVE_INFINITY = 1.0f / 0.0f;

    /**
     * <p>
     * Constant for the Negative Infinity value of the <code>float</code>
     * type.
     * </p>
     */
    public static final float NEGATIVE_INFINITY = -1.0f / 0.0f;

    /**
     * The java.lang.Class that represents this class.
     * 
     * @since 1.1
     */
    @SuppressWarnings("unchecked")
    public static final Class<Float> TYPE = (Class<Float>) new float[0]
            .getClass().getComponentType();

    // Note: This can't be set to "float.class", since *that* is
    // defined to be "java.lang.Float.TYPE";

    /**
     * <p>
     * Constant for the number of bits to represent a <code>float</code> in
     * two's compliment form.
     * </p>
     * 
     * @since 1.5
     */
    public static final int SIZE = 32;

    /**
     * Constructs a new instance of the receiver which represents the float
     * valued argument.
     * 
     * @param value
     *            the float to store in the new instance.
     */
    public Float(float value) {
        this.value = value;
    }

    /**
     * Constructs a new instance of the receiver which represents the double
     * valued argument.
     * 
     * @param value
     *            the double to store in the new instance.
     */
    public Float(double value) {
        this.value = (float) value;
    }

    /**
     * Constructs a new instance of this class given a string.
     * 
     * @param string
     *            a string representation of a float quantity.
     * @exception NumberFormatException
     *                if the argument could not be parsed as a float quantity.
     */
    public Float(String string) throws NumberFormatException {
        this(parseFloat(string));
    }

    /**
     * Compares the receiver with the Float parameter. NaN is equal to NaN, and
     * is greater than other float values. 0f is greater than -0f.
     * 
     * @param object
     *            the Float to compare to the receiver
     * 
     * @return Returns greater than zero when this.floatValue() is greater than
     *         object.floatValue(), zero when this.floatValue() equals
     *         object.floatValue(), and less than zero when this.floatValue() is
     *         less than object.floatValue()
     * @throws NullPointerException
     *             if <code>object</code> is <code>null</code>.
     * @since 1.2
     */
    public int compareTo(Float object) {
        int f1, f2;
        int NaNbits = Float.floatToIntBits(Float.NaN);
        if ((f1 = Float.floatToIntBits(value)) == NaNbits) {
            if (Float.floatToIntBits(object.value) == NaNbits) {
                return 0;
            }
            return 1;
        }
        if ((f2 = Float.floatToIntBits(object.value)) == NaNbits) {
            return -1;
        }
        if (value == object.value) {
            if (f1 == f2) {
                return 0;
            }
            // check for -0
            return f1 > f2 ? 1 : -1;
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
     * the <em>same</em> object using a class specific comparison. For Floats,
     * the check verifies that the receiver's value's bit pattern matches the
     * bit pattern of the argument, which must also be a Float.
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
                || (object instanceof Float)
                && (floatToIntBits(this.value) == floatToIntBits(((Float) object).value));
    }

    /**
     * Returns the binary representation of the argument, as an int.
     * 
     * @param value
     *            The float value to convert
     * @return the bits of the float.
     */
    public static native int floatToIntBits(float value);

    /**
     * Returns the binary representation of the argument, as an int.
     * 
     * @param value
     *            The float value to convert
     * @return the bits of the float.
     */
    public static native int floatToRawIntBits(float value);

    /**
     * Returns the receiver's value as a float.
     * 
     * @return the receiver's value
     */
    @Override
    public float floatValue() {
        return value;
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
        return floatToIntBits(value);
    }

    /**
     * Returns a float built from the binary representation given in the
     * argument.
     * 
     * @param bits
     *            the bits of the float
     * @return the float which matches the bits
     */
    public static native float intBitsToFloat(int bits);

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
     * @param f
     *            value to check for infinitness.
     * @return <code>true</code> if the argument is positive or negative
     *         infinity <code>false</code> if it is not an infinite value
     */
    public static boolean isInfinite(float f) {
        return (f == POSITIVE_INFINITY) || (f == NEGATIVE_INFINITY);
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
     * Returns true if the argument does not represent a valid float quantity.
     * 
     * @param f
     *            value to check for numberness.
     * @return <code>true</code> if the argument is Not A Number
     *         <code>false</code> if it is a (potentially infinite) float
     *         number
     */
    public static boolean isNaN(float f) {
        return f != f;
    }

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
     * Returns the float which matches the passed in string.
     * NumberFormatException is thrown if the string does not represent a valid
     * float.
     * 
     * @param string
     *            the value to convert
     * @return a float which would print as the argument
     * @see #valueOf(String)
     * @since 1.2
     */
    public static float parseFloat(String string) throws NumberFormatException {
        return org.apache.harmony.luni.util.FloatingPointParser
                .parseFloat(string);
    }

    /**
     * Returns the short value which the receiver represents
     * 
     * @return short the value of the receiver.
     * @since 1.1
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
        return Float.toString(value);
    }

    /**
     * Returns a string containing a printable representation of the argument.
     * 
     * @param f
     *            the float to print
     * @return a printable representation of the argument.
     */
    public static String toString(float f) {
        return org.apache.harmony.luni.util.NumberConverter.convert(f);
    }

    /**
     * Returns the float which matches the passed in string.
     * NumberFormatException is thrown if the string does not represent a valid
     * float.
     * 
     * @param string
     *            the value to convert
     * @return a float which would print as the argument
     */
    public static Float valueOf(String string) throws NumberFormatException {
        return valueOf(parseFloat(string));
    }

    /**
     * Compares the two floats. NaN is equal to NaN, and is greater than other
     * float values. 0f is greater than -0f.
     * 
     * @param float1
     *            the first value to compare
     * @param float2
     *            the second value to compare
     * 
     * @return Returns greater than zero when float1 is greater than float2,
     *         zero when float1 equals float2, and less than zero when float1 is
     *         less than float2
     * @since 1.4
     */
    public static int compare(float float1, float float2) {
        int f1, f2;
        int NaNbits = Float.floatToIntBits(Float.NaN);
        if ((f1 = Float.floatToIntBits(float1)) == NaNbits) {
            if (Float.floatToIntBits(float2) == NaNbits) {
                return 0;
            }
            return 1;
        }
        if ((f2 = Float.floatToIntBits(float2)) == NaNbits) {
            return -1;
        }
        if (float1 == float2) {
            if (f1 == f2) {
                return 0;
            }
            // check for -0
            return f1 > f2 ? 1 : -1;
        }
        return float1 > float2 ? 1 : -1;
    }

    /**
     * <p>
     * Returns a <code>Float</code> instance for the <code>float</code>
     * value passed. This method is preferred over the constructor, as this
     * method may maintain a cache of instances.
     * </p>
     * 
     * @param f
     *            The float value.
     * @return A <code>Float</code> instance.
     * @since 1.5
     */
    public static Float valueOf(float f) {
        return new Float(f);
    }

    /**
     * <p>
     * Converts a <code>float</code> into a hexadecimal string representation.
     * </p>
     * 
     * @param f
     *            The <code>float</code> to convert.
     * @return The hexadecimal string representation of <code>f</code>.
     * @since 1.5
     */
    public static String toHexString(float f) {
        /*
         * Reference: http://en.wikipedia.org/wiki/IEEE_754
         */
        if (f != f) {
            return "NaN"; //$NON-NLS-1$
        }
        if (f == POSITIVE_INFINITY) {
            return "Infinity"; //$NON-NLS-1$
        }
        if (f == NEGATIVE_INFINITY) {
            return "-Infinity"; //$NON-NLS-1$
        }

        int bitValue = floatToIntBits(f);

        boolean negative = (bitValue & 0x80000000) != 0;
        // mask exponent bits and shift down
        int exponent = (bitValue & 0x7f800000) >>> 23;
        // mask significand bits and shift up
        // significand is 23-bits, so we shift to treat it like 24-bits
        int significand = (bitValue & 0x007FFFFF) << 1;

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
            // significand is 23-bits, so there can be 6 hex digits
            int fractionDigits = 6;
            // remove trailing hex zeros, so Integer.toHexString() won't print
            // them
            while ((significand != 0) && ((significand & 0xF) == 0)) {
                significand >>>= 4;
                fractionDigits--;
            }
            // this assumes Integer.toHexString() returns lowercase characters
            String hexSignificand = Integer.toHexString(significand);

            // if there are digits left, then insert some '0' chars first
            if (significand != 0 && fractionDigits > hexSignificand.length()) {
                int digitDiff = fractionDigits - hexSignificand.length();
                while (digitDiff-- != 0) {
                    hexString.append('0');
                }
            }
            hexString.append(hexSignificand);
            hexString.append("p-126"); //$NON-NLS-1$
        } else { // normal value
            hexString.append("1."); //$NON-NLS-1$
            // significand is 23-bits, so there can be 6 hex digits
            int fractionDigits = 6;
            // remove trailing hex zeros, so Integer.toHexString() won't print
            // them
            while ((significand != 0) && ((significand & 0xF) == 0)) {
                significand >>>= 4;
                fractionDigits--;
            }
            // this assumes Integer.toHexString() returns lowercase characters
            String hexSignificand = Integer.toHexString(significand);

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
            hexString.append(Integer.toString(exponent - 127));
        }
        return hexString.toString();
    }
}
