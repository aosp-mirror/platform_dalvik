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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * <p>
 * A modifiable {@link CharSequence sequence of characters} for use in creating
 * and modifying Strings. This class is intended as a direct replacement of
 * {@link java.lang.StringBuffer} for non-concurrent use; unlike
 * <code>StringBuffer</code> this class is not synchronized for thread safety.
 * </p>
 *
 * <p>
 * The majority of the modification methods on this class return
 * <code>StringBuilder</code>, so that, like <code>StringBuffer</code>s,
 * they can be used in chaining method calls together. For example,
 * <code>new StringBuilder("One should ").append("always strive ").append("to achieve Harmony")</code>.
 * </p>
 *
 * @see java.lang.CharSequence
 * @see java.lang.Appendable
 * @see java.lang.StringBuffer
 * @see java.lang.String
 *
 * @since 1.5
 */
public final class StringBuilder extends AbstractStringBuilder implements
        Appendable, CharSequence, Serializable {

    private static final long serialVersionUID = 4383685877147921099L;

    /**
     * <p>
     * Constructs an instance with an initial capacity of <code>16</code>.
     * </p>
     *
     * @see #capacity()
     */
    public StringBuilder() {
        super();
    }

    /**
     * <p>
     * Constructs an instance with a specified capacity.
     * </p>
     *
     * @param capacity The initial capacity to use.
     *
     * @throws NegativeArraySizeException if the <code>capacity</code>
     *         parameter is <code>null</code>.
     *
     * @see #capacity()
     */
    public StringBuilder(int capacity) {
        super(capacity);
    }

    /**
     * <p>
     * Constructs an instance that's populated by a {@link CharSequence}. The
     * capacity of the new builder will be the length of the
     * <code>CharSequence</code> plus 16.
     * </p>
     *
     * @param seq The <code>CharSequence</code> to copy into the builder.
     * @throws NullPointerException if the <code>seq</code> parameter is
     *         <code>null</code>.
     */
    public StringBuilder(CharSequence seq) {
        super(seq.toString());
    }

    /**
     * <p>
     * Constructs an instance that's populated by a {@link String}. The
     * capacity of the new builder will be the length of the
     * <code>String</code> plus 16.
     * </p>
     *
     * @param str The <code>String</code> to copy into the builder.
     * @throws NullPointerException if the <code>str</code> parameter is
     *         <code>null</code>.
     */
    public StringBuilder(String str) {
        super(str);
    }

    /**
     * <p>
     * Appends the String representation of the <code>boolean</code> value
     * passed. The <code>boolean</code> value is converted to a String
     * according to the rule defined by {@link String#valueOf(boolean)}.
     * </p>
     *
     * @param b The <code>boolean</code> value to append to this object.
     * @return A reference to this object.
     *
     * @see String#valueOf(boolean)
     */
    public StringBuilder append(boolean b) {
        append0(b ? "true" : "false"); //$NON-NLS-1$ //$NON-NLS-2$
        return this;
    }

    /**
     * <p>
     * Appends the String representation of the <code>char</code> value
     * passed. The <code>char</code> value is converted to a String according
     * to the rule defined by {@link String#valueOf(char)}.
     * </p>
     *
     * @param c The <code>char</code> value to append to this object.
     * @return A reference to this object.
     *
     * @see String#valueOf(char)
     */
    public StringBuilder append(char c) {
        append0(c);
        return this;
    }

    /**
     * <p>
     * Appends the String representation of the <code>int</code> value passed.
     * The <code>int</code> value is converted to a String according to the
     * rule defined by {@link String#valueOf(int)}.
     * </p>
     *
     * @param i The <code>int</code> value to append to this object.
     * @return A reference to this object.
     *
     * @see String#valueOf(int)
     */
    public StringBuilder append(int i) {
        append0(Integer.toString(i));
        return this;
    }

    /**
     * <p>
     * Appends the String representation of the <code>long</code> value
     * passed. The <code>long</code> value is converted to a String according
     * to the rule defined by {@link String#valueOf(long)}.
     * </p>
     *
     * @param lng The <code>long</code> value to append to this object.
     * @return A reference to this object.
     *
     * @see String#valueOf(long)
     */
    public StringBuilder append(long lng) {
        append0(Long.toString(lng));
        return this;
    }

    /**
     * <p>
     * Appends the String representation of the <code>float</code> value
     * passed. The <code>float</code> value is converted to a String according
     * to the rule defined by {@link String#valueOf(float)}.
     * </p>
     *
     * @param f The <code>float</code> value to append to this object.
     * @return A reference to this object.
     *
     * @see String#valueOf(float)
     */
    public StringBuilder append(float f) {
        append0(Float.toString(f));
        return this;
    }

    /**
     * <p>
     * Appends the String representation of the <code>double</code> value
     * passed. The <code>double</code> value is converted to a String
     * according to the rule defined by {@link String#valueOf(double)}.
     * </p>
     *
     * @param d The <code>double</code> value to append to this object.
     * @return A reference to this object.
     *
     * @see String#valueOf(double)
     */
    public StringBuilder append(double d) {
        append0(Double.toString(d));
        return this;
    }

    /**
     * <p>
     * Appends the String representation of the <code>Object</code> value
     * passed. The <code>Object</code> value is converted to a String
     * according to the rule defined by {@link String#valueOf(Object)}.
     * </p>
     *
     * @param obj The <code>Object</code> value to append to this object.
     * @return A reference to this object.
     *
     * @see String#valueOf(Object)
     */
    public StringBuilder append(Object obj) {
        if (obj == null) {
            appendNull();
        } else {
            append0(obj.toString());
        }
        return this;
    }

    /**
     * <p>
     * Appends the contents of the String. If the String passed is
     * <code>null</code>, then the String <code>"null"</code> is appended.
     * </p>
     *
     * @param str The String to append to this object.
     * @return A reference to this object.
     */
    public StringBuilder append(String str) {
        append0(str);
        return this;
    }

    /**
     * <p>
     * Appends the contents of the StringBuffer. If the StringBuffer passed is
     * <code>null</code>, then the StringBuffer <code>"null"</code> is
     * appended.
     * </p>
     *
     * @param sb The StringBuffer to append to this object.
     * @return A reference to this object.
     */
    public StringBuilder append(StringBuffer sb) {
        if (sb == null) {
            appendNull();
        } else {
            append0(sb.getValue(), 0, sb.length());
        }
        return this;
    }

    /**
     * <p>
     * Appends the String representation of the <code>char[]</code> value
     * passed. The <code>char[]</code> value is converted to a String
     * according to the rule defined by {@link String#valueOf(char[])}.
     * </p>
     *
     * @param ch  The <code>char[]</code> value to append to this object.
     * @return A reference to this object.
     *
     * @see String#valueOf(char[])
     */
    public StringBuilder append(char[] ch) {
        append0(ch);
        return this;
    }

    /**
     * <p>
     * Appends the String representation of the subset of the
     * <code>char[]</code> value passed. The <code>char[]</code> value is
     * converted to a String according to the rule defined by
     * {@link String#valueOf(char[],int,int)}.
     * </p>
     *
     * @param str The <code>char[]</code> value to append to this object.
     * @param offset The inclusive offset index to begin copying from the
     *        <code>str</code> parameter.
     * @param len The number of character to copy from the <code>str</code>
     *        parameter.
     * @return A reference to this object.
     *
     * @see String#valueOf(char[],int,int)
     */
    public StringBuilder append(char[] str, int offset, int len) {
        append0(str, offset, len);
        return this;
    }

    /**
     * <p>
     * Appends the String representation of the <code>CharSequence</code>
     * value passed. If the <code>CharSequence</code> is <code>null</code>,
     * then the String <code>"null"</code> is appended.
     * </p>
     *
     * @param csq The <code>CharSequence</code> value to append to this
     *        object.
     * @return A reference to this object.
     */
    public StringBuilder append(CharSequence csq) {
        if (csq == null) {
            appendNull();
        } else {
            append0(csq.toString());
        }
        return this;
    }

    /**
     * <p>
     * Appends the String representation of the subsequence of the
     * <code>CharSequence</code> value passed. If the
     * <code>CharSequence</code> is <code>null</code>, then the String
     * <code>"null"</code> is used to extract the subsequence from.
     * </p>
     *
     * @param csq The <code>CharSequence</code> value to append to this
     *        object.
     * @param start The beginning index of the subsequence.
     * @param end The ending index of the subsequence.
     * @return A reference to this object.
     */
    public StringBuilder append(CharSequence csq, int start, int end) {
        append0(csq, start, end);
        return this;
    }

    /**
     * <p>
     * Appends the encoded Unicode code point to this object. The code point is
     * converted to a <code>char[]</code> as defined by
     * {@link Character#toChars(int)}.
     * </p>
     *
     * @param codePoint The Unicode code point to encode and append.
     * @return A reference to this object.
     * @see Character#toChars(int)
     */
    public StringBuilder appendCodePoint(int codePoint) {
        append0(Character.toChars(codePoint));
        return this;
    }

    /**
     * <p>
     * Deletes a sequence of characters within this object, shifts any remaining
     * characters to the left and adjusts the {@link #length()} of this object.
     * </p>
     *
     * @param start The inclusive start index to begin deletion.
     * @param end The exclusive end index to stop deletion.
     * @return A reference to this object.
     * @throws StringIndexOutOfBoundsException if <code>start</code> is less
     *         than zero, greater than the current length or greater than
     *         <code>end</code>.
     */
    public StringBuilder delete(int start, int end) {
        delete0(start, end);
        return this;
    }

    /**
     * <p>
     * Deletes a single character within this object, shifts any remaining
     * characters to the left and adjusts the {@link #length()} of this object.
     * </p>
     *
     * @param index The index of the character to delete.
     * @return A reference to this object.
     * @throws StringIndexOutOfBoundsException if <code>index</code> is less
     *         than zero or is greater than or equal to the current length.
     */
    public StringBuilder deleteCharAt(int index) {
        deleteCharAt0(index);
        return this;
    }

    /**
     * <p>
     * Inserts the String representation of the <code>boolean</code> value
     * passed into this object at the <code>offset</code> passed. The
     * <code>boolean</code> value is converted to a String according to the
     * rule defined by {@link String#valueOf(boolean)}.
     * </p>
     *
     * @param offset The index of this object to insert the value.
     * @param b The <code>boolean</code> value to insert into this object.
     * @return A reference to this object.
     *
     * @throws StringIndexOutOfBoundsException if <code>offset</code> is
     *         negative or greater than the current {@link #length()}.
     *
     * @see String#valueOf(boolean)
     */
    public StringBuilder insert(int offset, boolean b) {
        insert0(offset, b ? "true" : "false"); //$NON-NLS-1$ //$NON-NLS-2$
        return this;
    }

    /**
     * <p>
     * Inserts the String representation of the <code>char</code> value passed
     * into this object at the <code>offset</code> passed. The
     * <code>char</code> value is converted to a String according to the rule
     * defined by {@link String#valueOf(char)}.
     * </p>
     *
     * @param offset The index of this object to insert the value.
     * @param c The <code>char</code> value to insert into this object.
     * @return A reference to this object.
     *
     * @throws ArrayIndexOutOfBoundsException if <code>offset</code> is
     *         negative or greater than the current {@link #length()}.
     *
     * @see String#valueOf(char)
     */
    public StringBuilder insert(int offset, char c) {
        insert0(offset, c);
        return this;
    }

    /**
     * <p>
     * Inserts the String representation of the <code>int</code> value passed
     * into this object at the <code>offset</code> passed. The
     * <code>int</code> value is converted to a String according to the rule
     * defined by {@link String#valueOf(int)}.
     * </p>
     *
     * @param offset The index of this object to insert the value.
     * @param i The <code>int</code> value to insert into this object.
     * @return A reference to this object.
     *
     * @throws StringIndexOutOfBoundsException if <code>offset</code> is
     *         negative or greater than the current {@link #length()}.
     *
     * @see String#valueOf(int)
     */
    public StringBuilder insert(int offset, int i) {
        insert0(offset, Integer.toString(i));
        return this;
    }

    /**
     * <p>
     * Inserts the String representation of the <code>long</code> value passed
     * into this object at the <code>offset</code> passed. The
     * <code>long</code> value is converted to a String according to the rule
     * defined by {@link String#valueOf(long)}.
     * </p>
     *
     * @param offset The index of this object to insert the value.
     * @param l The <code>long</code> value to insert into this object.
     * @return A reference to this object.
     *
     * @throws StringIndexOutOfBoundsException if <code>offset</code> is
     *         negative or greater than the current {@link #length()}.
     *
     * @see String#valueOf(long)
     */
    public StringBuilder insert(int offset, long l) {
        insert0(offset, Long.toString(l));
        return this;
    }

    /**
     * <p>
     * Inserts the String representation of the <code>float</code> value
     * passed into this object at the <code>offset</code> passed. The
     * <code>float</code> value is converted to a String according to the rule
     * defined by {@link String#valueOf(float)}.
     * </p>
     *
     * @param offset The index of this object to insert the value.
     * @param f The <code>float</code> value to insert into this object.
     * @return A reference to this object.
     *
     * @throws StringIndexOutOfBoundsException if <code>offset</code> is
     *         negative or greater than the current {@link #length()}.
     *
     * @see String#valueOf(float)
     */
    public StringBuilder insert(int offset, float f) {
        insert0(offset, Float.toString(f));
        return this;
    }

    /**
     * <p>
     * Inserts the String representation of the <code>double</code> value
     * passed into this object at the <code>offset</code> passed. The
     * <code>double</code> value is converted to a String according to the
     * rule defined by {@link String#valueOf(double)}.
     * </p>
     *
     * @param offset The index of this object to insert the value.
     * @param d The <code>double</code> value to insert into this object.
     * @return A reference to this object.
     *
     * @throws StringIndexOutOfBoundsException if <code>offset</code> is
     *         negative or greater than the current {@link #length()}.
     *
     * @see String#valueOf(double)
     */
    public StringBuilder insert(int offset, double d) {
        insert0(offset, Double.toString(d));
        return this;
    }

    /**
     * <p>
     * Inserts the String representation of the <code>Object</code> value
     * passed into this object at the <code>offset</code> passed. The
     * <code>Object</code> value is converted to a String according to the
     * rule defined by {@link String#valueOf(Object)}.
     * </p>
     *
     * @param offset The index of this object to insert the value.
     * @param obj The <code>Object</code> value to insert into this object.
     * @return A reference to this object.
     *
     * @throws StringIndexOutOfBoundsException if <code>offset</code> is
     *         negative or greater than the current {@link #length()}.
     *
     * @see String#valueOf(Object)
     */
    public StringBuilder insert(int offset, Object obj) {
        insert0(offset, obj == null ? "null" : obj.toString()); //$NON-NLS-1$
        return this;
    }

    /**
     * <p>
     * Inserts the String value passed into this object at the
     * <code>offset</code> passed. If the String parameter is null, then the
     * String <code>"null"</code> is inserted.
     * </p>
     *
     * @param offset The index of this object to insert the value.
     * @param str The String to insert into this object.
     * @return A reference to this object.
     *
     * @throws StringIndexOutOfBoundsException if <code>offset</code> is
     *         negative or greater than the current {@link #length()}.
     */
    public StringBuilder insert(int offset, String str) {
        insert0(offset, str);
        return this;
    }

    /**
     * <p>
     * Inserts the String representation of the <code>char[]</code> value
     * passed into this object at the <code>offset</code> passed. The
     * <code>char[]</code> value is converted to a String according to the
     * rule defined by {@link String#valueOf(char[])}.
     * </p>
     *
     * @param offset The index of this object to insert the value.
     * @param ch The <code>char[]</code> value to insert into this object.
     * @return A reference to this object.
     *
     * @throws StringIndexOutOfBoundsException if <code>offset</code> is
     *         negative or greater than the current {@link #length()}.
     *
     * @see String#valueOf(char[])
     */
    public StringBuilder insert(int offset, char[] ch) {
        insert0(offset, ch);
        return this;
    }

    /**
     * <p>
     * Inserts the String representation of the subsequence of the
     * <code>char[]</code> value passed into this object at the
     * <code>offset</code> passed. The <code>char[]</code> value is
     * converted to a String according to the rule defined by
     * {@link String#valueOf(char[],int,int)}.
     * </p>
     *
     * @param offset The index of this object to insert the value.
     * @param str The <code>char[]</code> value to insert into this object.
     * @param strOffset The inclusive index of the <code>str</code> parameter
     *        to start copying from.
     * @param strLen The number of characters to copy from the <code>str</code>
     *        parameter.
     * @return A reference to this object.
     *
     * @throws StringIndexOutOfBoundsException if <code>offset</code> is
     *         negative or greater than the current {@link #length()}.
     *
     * @see String#valueOf(char[],int,int)
     */
    public StringBuilder insert(int offset, char[] str, int strOffset,
            int strLen) {
        insert0(offset, str, strOffset, strLen);
        return this;
    }

    /**
     * <p>
     * Inserts the String representation of the <code>CharSequence</code>
     * value passed into this object at the <code>offset</code> passed. The
     * <code>CharSequence</code> value is converted to a String as defined by
     * {@link CharSequence#toString()}. If the <code>CharSequence</code> is
     * <code>null</code>, then the String <code>"null"</code> is inserted.
     * </p>
     *
     * @param offset The index of this object to insert the value.
     * @param s The <code>CharSequence</code> value to insert into this
     *        object.
     * @return A reference to this object.
     *
     * @throws IndexOutOfBoundsException if <code>offset</code> is negative or
     *         greater than the current {@link #length()}.
     *
     * @see CharSequence#toString()
     */
    public StringBuilder insert(int offset, CharSequence s) {
        insert0(offset, s == null ? "null" : s.toString()); //$NON-NLS-1$
        return this;
    }

    /**
     * <p>
     * Inserts the String representation of the subsequence of the
     * <code>CharSequence</code> value passed into this object at the
     * <code>offset</code> passed. The <code>CharSequence</code> value is
     * converted to a String as defined by
     * {@link CharSequence#subSequence(int, int)}. If the
     * <code>CharSequence</code> is <code>null</code>, then the String
     * <code>"null"</code> is used to determine the subsequence.
     * </p>
     *
     * @param offset The index of this object to insert the value.
     * @param s The <code>CharSequence</code> value to insert into this
     *        object.
     * @param start The start of the subsequence of the <code>s</code>
     *        parameter.
     * @param end The end of the subsequence of the <code>s</code> parameter.
     * @return A reference to this object.
     *
     * @throws IndexOutOfBoundsException if <code>offset</code> is negative or
     *         greater than the current {@link #length()}.
     *
     * @see CharSequence#subSequence(int, int)
     */
    public StringBuilder insert(int offset, CharSequence s, int start, int end) {
        insert0(offset, s, start, end);
        return this;
    }

    /**
     * <p>
     * Replaces the indicated subsequence of this object with the String passed.
     * If the String passed is longer or shorter than the subsequence, then this
     * object will be adjusted appropriately.
     * </p>
     *
     * @param start The inclusive start index of the sequence to replace in this
     *        object.
     * @param end The exclusive end index of the sequence to replace in this
     *        object.
     * @param str The String to replace the subsequence.
     * @return A reference to this object.
     * @throws StringIndexOutOfBoundsException if <code>start</code> is
     *         negative, greater than the current {@link #length()} or greater
     *         than <code>end</code>.
     * @throws NullPointerException if the <code>str</code> parameter is
     *         <code>null</code>.
     */
    public StringBuilder replace(int start, int end, String str) {
        replace0(start, end, str);
        return this;
    }

    /**
     * <p>
     * Reverses the contents of this object.
     * </p>
     *
     * @return A reference to this object.
     */
    public StringBuilder reverse() {
        reverse0();
        return this;
    }

    /**
     * Returns the contents of this StringBuilder.
     *
     * @return a String containing the characters in this StringBuilder
     */
    @Override
    public String toString() {
        /* Note: This method is required to workaround a compiler bug
         * in the RI javac (at least in 1.5.0_06) that will generate a
         * reference to the non-public AbstractStringBuilder if we don't
         * override it here.
         */
        return super.toString();
    }

    /**
     * <p>
     * Reads the state of a <code>StringBuilder</code> from the passed stream
     * and restores it to this instance.
     * </p>
     *
     * @param in The stream to read the state from.
     * @throws IOException if the stream throws it during the read.
     * @throws ClassNotFoundException if the stream throws it during the read.
     */
    private void readObject(ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        in.defaultReadObject();
        int count = in.readInt();
        char[] value = (char[]) in.readObject();
        set(value, count);
    }

    /**
     * <p>
     * Writes the state of this object to the stream passed.
     * </p>
     *
     * @param out The stream to write the state to.
     * @throws IOException if the stream throws it during the write.
     * @serialData <code>int</code> - The length of this object.
     *             <code>char[]</code> - The buffer from this object, which
     *             may be larger than the length field.
     */
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        out.writeInt(length());
        out.writeObject(getValue());
    }
}
