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
 * <p>Byte is the wrapper for the primitive type <code>byte</code>.</p>
 * @since 1.1
 */
public final class Byte extends Number implements Comparable<Byte> {    
    private static final long serialVersionUID = -7183698231559129828L;

    /**
     * The value which the receiver represents.
     */
    private final byte value;

    /**
     * <p>
     * Constant for the maximum <code>byte</code> value, 2<sup>7</sup>-1.
     * </p>
     */
    public static final byte MAX_VALUE = (byte) 0x7F;

    /**
     * <p>
     * Constant for the minimum <code>byte</code> value, -2<sup>7</sup>.
     * </p>
     */
    public static final byte MIN_VALUE = (byte) 0x80;
    
    /**
     * <p>
     * Constant for the number of bits to represent a <code>byte</code> in
     * two's compliment form.
     * </p>
     * 
     * @since 1.5
     */
    public static final int SIZE = 8;

    /**
     * The java.lang.Class that represents this class.
     */
    @SuppressWarnings("unchecked")
    public static final Class<Byte> TYPE = (Class<Byte>) new byte[0].getClass()
            .getComponentType();

    // Note: This can't be set to "byte.class", since *that* is
    // defined to be "java.lang.Byte.TYPE";
    
    /**
     * <p>
     * A cache of instances used by {@link #valueOf(byte)} and auto-boxing.
     * </p>
     */
    private static final Byte[] CACHE = new Byte[256];

    /**
     * Constructs a new instance of the receiver which represents the byte
     * valued argument.
     * 
     * @param value
     *            the byte to store in the new instance.
     */
    public Byte(byte value) {
        this.value = value;
    }

    /**
     * Constructs a new instance of this class given a string.
     * 
     * @param string
     *            a string representation of a single byte quantity.
     * @throws NumberFormatException
     *             if the argument could not be parsed as a byte quantity.
     */
    public Byte(String string) throws NumberFormatException {
        this(parseByte(string));
    }

    /**
     * Returns the byte value which the receiver represents
     * 
     * @return byte the value of the receiver.
     */
    @Override
    public byte byteValue() {
        return value;
    }

    /**
     * <p>
     * Compares this <code>Byte</code> to the <code>Byte</code> passed. If
     * this instance's value is equal to the value of the instance passed, then
     * 0 is returned. If this instance's value is less than the value of the
     * instance passed, then a negative value is returned. If this instance's
     * value is greater than the value of the instance passed, then a positive
     * value is returned.
     * </p>
     * 
     * @param object The instance to compare to.
     * @throws NullPointerException if <code>object</code> is
     *         <code>null</code>.
     * @since 1.2
     */
    public int compareTo(Byte object) {
        return value > object.value ? 1 : (value < object.value ? -1 : 0);
    }

    /**
     * Parses the string argument as if it was a byte value and returns the
     * result. It is an error if the received string does not contain a
     * representation of a single byte quantity. The string may be a hexadecimal
     * ("0x..."), octal ("0..."), or decimal ("...") representation of a byte.
     * 
     * @param string
     *            a string representation of a single byte quantity.
     * @return Byte the value represented by the argument
     * @throws NumberFormatException
     *             if the argument could not be parsed as a byte quantity.
     */
    public static Byte decode(String string) throws NumberFormatException {
        int intValue = Integer.decode(string).intValue();
        byte result = (byte) intValue;
        if (result == intValue) {
            return valueOf(result);
        }
        throw new NumberFormatException();
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
     * In this case, the argument must also be a Byte, and the receiver and
     * argument must represent the same byte value.
     * 
     * @param object
     *            the object to compare with this object
     * @return <code>true</code> if the object is the same as this object
     *         <code>false</code> if it is different from this object
     * @see #hashCode
     */
    @Override
    public boolean equals(Object object) {
        return (object == this) || (object instanceof Byte)
                && (value == ((Byte) object).value);
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
        return value;
    }

    /**
     * Returns the int value which the receiver represents
     * 
     * @return int the value of the receiver.
     */
    @Override
    public int intValue() {
        return value;
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
     * Parses the string argument as if it was a byte value and returns the
     * result. Throws NumberFormatException if the string does not represent a
     * single byte quantity.
     * 
     * @param string
     *            a string representation of a single byte quantity.
     * @return byte the value represented by the argument
     * @throws NumberFormatException
     *             if the argument could not be parsed as a byte quantity.
     */
    public static byte parseByte(String string) throws NumberFormatException {
        int intValue = Integer.parseInt(string);
        byte result = (byte) intValue;
        if (result == intValue) {
            return result;
        }
        throw new NumberFormatException();
    }

    /**
     * Parses the string argument as if it was a byte value and returns the
     * result. Throws NumberFormatException if the string does not represent a
     * single byte quantity. The second argument specifies the radix to use when
     * parsing the value.
     * 
     * @param string
     *            a string representation of a single byte quantity.
     * @param radix
     *            the radix to use when parsing.
     * @return byte the value represented by the argument
     * @throws NumberFormatException
     *             if the argument could not be parsed as a byte quantity.
     */
    public static byte parseByte(String string, int radix)
            throws NumberFormatException {
        int intValue = Integer.parseInt(string, radix);
        byte result = (byte) intValue;
        if (result == intValue) {
            return result;
        }
        throw new NumberFormatException();
    }

    /**
     * Returns the short value which the receiver represents
     * 
     * @return short the value of the receiver.
     */
    @Override
    public short shortValue() {
        return value;
    }

    /**
     * Returns a string containing a concise, human-readable description of the
     * receiver.
     * 
     * @return a printable representation for the receiver.
     */
    @Override
    public String toString() {
        return Integer.toString(value);
    }

    /**
     * Returns a string containing a concise, human-readable description of the
     * argument.
     * 
     * @param value
     *            byte the byte to convert.
     * @return String a printable representation for the byte.
     */
    public static String toString(byte value) {
        return Integer.toString(value);
    }

    /**
     * Parses the string argument as if it was a byte value and returns a Byte
     * representing the result. Throws NumberFormatException if the string
     * cannot be parsed as a byte quantity.
     * 
     * @param string
     *            a string representation of a single byte quantity.
     * @return Byte the value represented by the argument
     * @throws NumberFormatException
     *             if the argument could not be parsed as a byte quantity.
     */
    public static Byte valueOf(String string) throws NumberFormatException {
        return valueOf(parseByte(string));
    }

    /**
     * Parses the string argument as if it was a byte value and returns a Byte
     * representing the result. Throws NumberFormatException if the string
     * cannot be parsed as a byte quantity. The second argument specifies the
     * radix to use when parsing the value.
     * 
     * @param string
     *            a string representation of a single byte quantity.
     * @param radix
     *            the radix to use when parsing.
     * @return Byte the value represented by the argument
     * @throws NumberFormatException
     *             if the argument could not be parsed as a byte quantity.
     */
    public static Byte valueOf(String string, int radix)
            throws NumberFormatException {
        return valueOf(parseByte(string, radix));
    }
    
    /**
     * <p>Returns a <code>Byte</code> instance for the <code>byte</code> value passed.
     * This method is preferred over the constructor, as this method may maintain a cache
     * of instances.</p>
     * @param b The byte value.
     * @return A <code>Byte</code> instance.
     * @since 1.5
     */
    public static Byte valueOf(byte b) {
        synchronized (CACHE) {
            int idx = b - MIN_VALUE;
            Byte result = CACHE[idx];
            return (result == null ? CACHE[idx] = new Byte(b) : result);
        }
    }
}
