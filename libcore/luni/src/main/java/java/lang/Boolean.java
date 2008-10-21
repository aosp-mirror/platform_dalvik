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

import java.io.Serializable;

/**
 * <p>
 * Boolean is the wrapper for the primitive type <code>boolean</code>.
 * </p>
 * 
 * @since 1.0
 */
public final class Boolean implements Serializable, Comparable<Boolean> {

    private static final long serialVersionUID = -3665804199014368530L;

    /**
     * The boolean value of the receiver.
     */
    private final boolean value;

    /**
     * The java.lang.Class that represents this class.
     */
    @SuppressWarnings("unchecked")
    public static final Class<Boolean> TYPE = (Class<Boolean>) new boolean[0]
            .getClass().getComponentType();

    // Note: This can't be set to "boolean.class", since *that* is
    // defined to be "java.lang.Boolean.TYPE";

    /**
     * The instance of the receiver which represents truth.
     */
    public static final Boolean TRUE = new Boolean(true);

    /**
     * The instance of the receiver which represents falsehood.
     */
    public static final Boolean FALSE = new Boolean(false);

    /**
     * Constructs a new instance of this class given a string. If the string is
     * equal to "true" using a non-case sensitive comparison, the result will be
     * a Boolean representing true, otherwise it will be a Boolean representing
     * false.
     * 
     * @param string
     *            The name of the desired boolean.
     */
    public Boolean(String string) {
        this(parseBoolean(string));
    }

    /**
     * Constructs a new instance of this class given true or false.
     * 
     * @param value
     *            true or false.
     */
    public Boolean(boolean value) {
        this.value = value;
    }

    /**
     * Returns true if the receiver represents true and false if the receiver
     * represents false.
     * 
     * @return true or false.
     */
    public boolean booleanValue() {
        return value;
    }

    /**
     * Compares the argument to the receiver, and returns true if they represent
     * the <em>same</em> object using a class specific comparison.
     * <p>
     * In this case, the argument must also be a Boolean, and the receiver and
     * argument must represent the same boolean value (i.e. both true or both
     * false).
     * 
     * @param o
     *            the object to compare with this object
     * @return <code>true</code> if the object is the same as this object
     *         <code>false</code> if it is different from this object
     * 
     * @see #hashCode
     */
    @Override
    public boolean equals(Object o) {
        return (o == this)
                || ((o instanceof Boolean) && (value == ((Boolean) o).value));
    }

    /**
     * <p>
     * Compares this <code>Boolean</code> to another <code>Boolean</code>.
     * If this instance has the same value as the instance passed, then
     * <code>0</code> is returned. If this instance is <code>true</code> and
     * the instance passed is <code>false</code>, then a positive value is
     * returned. If this instance is <code>false</code> and the instance
     * passed is <code>true</code>, then a negative value is returned.
     * </p>
     * 
     * @param that
     *            The instance to compare to.
     * @throws java.lang.NullPointerException
     *             if <code>that</code> is <code>null</code>.
     * @since 1.5
     * @see java.lang.Comparable
     */
    public int compareTo(Boolean that) {
        if (that == null) {
            throw new NullPointerException();
        }

        if (this.value == that.value) {
            return 0;
        }

        return this.value ? 1 : -1;
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
        return value ? 1231 : 1237;
    }

    /**
     * Returns a string containing a concise, human-readable description of the
     * receiver.
     * 
     * @return a printable representation for the receiver.
     */
    @Override
    public String toString() {
        return String.valueOf(value);
    }

    /**
     * Returns true if the system property described by the argument equal to
     * "true" using case insensitive comparison, and false otherwise.
     * 
     * @param string
     *            The name of the desired boolean.
     * @return The boolean value.
     */
    public static boolean getBoolean(String string) {
        if (string == null || string.length() == 0) {
            return false;
        }
        return (parseBoolean(System.getProperty(string)));
    }

    /**
     * <p>
     * Parses the string as a <code>boolean</code>. If the string is not
     * <code>null</code> and is equal to <code>"true"</code>, regardless
     * case, then <code>true</code> is returned, otherwise <code>false</code>.
     * </p>
     * 
     * @param s
     *            The string to parse.
     * @return A boolean value.
     * @since 1.5
     */
    public static boolean parseBoolean(String s) {
        return "true".equalsIgnoreCase(s); //$NON-NLS-1$
    }

    /**
     * Converts the specified boolean to its string representation. When the
     * boolean is true answer <code>"true"</code>, otherwise answer
     * <code>"false"</code>.
     * 
     * @param value
     *            the boolean
     * @return the boolean converted to a string
     */
    public static String toString(boolean value) {
        return String.valueOf(value);
    }

    /**
     * Returns a Boolean representing true if the argument is equal to "true"
     * using case insensitive comparison, and a Boolean representing false
     * otherwise.
     * 
     * @param string
     *            The name of the desired boolean.
     * @return the boolean value.
     */
    public static Boolean valueOf(String string) {
        return parseBoolean(string) ? Boolean.TRUE : Boolean.FALSE;
    }

    /**
     * Returns Boolean.TRUE if the argument is equal to "true" using case
     * insensitive comparison, and Boolean.FALSE representing false otherwise.
     * 
     * @param b
     *            the boolean value.
     * @return Boolean.TRUE or Boolean.FALSE Global true/false objects.
     */
    public static Boolean valueOf(boolean b) {
        return b ? Boolean.TRUE : Boolean.FALSE;
    }
}
