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
import java.io.ObjectStreamField;
import java.io.Serializable;

/**
 * StringBuffer is a variable size contiguous indexable array of characters. The
 * length of the StringBuffer is the number of characters it contains. The
 * capacity of the StringBuffer is the number of characters it can hold.
 * <p>
 * Characters may be inserted at any position up to the length of the
 * StringBuffer, increasing the length of the StringBuffer. Characters at any
 * position in the StringBuffer may be replaced, which does not affect the
 * StringBuffer length.
 * <p>
 * The capacity of a StringBuffer may be specified when the StringBuffer is
 * created. If the capacity of the StringBuffer is exceeded, the capacity is
 * increased.
 * 
 * @see String
 * @see StringBuilder
 * @since 1.0
 */
public final class StringBuffer extends AbstractStringBuilder implements
        Appendable, Serializable, CharSequence {

    private static final long serialVersionUID = 3388685877147921107L;

    private static final ObjectStreamField serialPersistentFields[] = {
            new ObjectStreamField("count", int.class), //$NON-NLS-1$
            new ObjectStreamField("shared", boolean.class), //$NON-NLS-1$
            new ObjectStreamField("value", char[].class), }; //$NON-NLS-1$

    /**
     * Constructs a new StringBuffer using the default capacity.
     */
    public StringBuffer() {
        super();
    }

    /**
     * Constructs a new StringBuffer using the specified capacity.
     * 
     * @param capacity
     *            the initial capacity
     */
    public StringBuffer(int capacity) {
        super(capacity);
    }

    /**
     * Constructs a new StringBuffer containing the characters in the specified
     * string and the default capacity.
     * 
     * @param string
     *            the string content with which to initialize the new
     *            <code>StringBuffer</code> instance
     * @throws NullPointerException
     *             on supplying a <code>null</code> value of
     *             <code>string</code>
     */
    public StringBuffer(String string) {
        super(string);
    }

    /**
     * <p>
     * Constructs a StringBuffer and initializes it with the characters in the
     * <code>CharSequence</code>.
     * </p>
     * 
     * @param cs
     *            The <code>CharSequence</code> to initialize the instance.
     * @throws NullPointerException
     *             if the <code>cs</code> parameter is <code>null</code>.
     * @since 1.5
     */
    public StringBuffer(CharSequence cs) {
        super(cs.toString());
    }

    /**
     * Adds the string representation of the specified boolean to the end of
     * this StringBuffer.
     * 
     * @param b
     *            the boolean
     * @return this StringBuffer
     */
    public StringBuffer append(boolean b) {
        return append(b ? "true" : "false"); //$NON-NLS-1$//$NON-NLS-2$
    }

    /**
     * Adds the specified character to the end of this StringBuffer.
     * 
     * @param ch
     *            a character
     * @return this StringBuffer
     */
    public synchronized StringBuffer append(char ch) {
        append0(ch);
        return this;
    }

    /**
     * Adds the string representation of the specified double to the end of this
     * StringBuffer.
     * 
     * @param d
     *            the double
     * @return this StringBuffer
     */
    public StringBuffer append(double d) {
        return append(Double.toString(d));
    }

    /**
     * Adds the string representation of the specified float to the end of this
     * StringBuffer.
     * 
     * @param f
     *            the float
     * @return this StringBuffer
     */
    public StringBuffer append(float f) {
        return append(Float.toString(f));
    }

    /**
     * Adds the string representation of the specified integer to the end of
     * this StringBuffer.
     * 
     * @param i 
     *            the integer
     * @return this StringBuffer
     */
    public StringBuffer append(int i) {
        return append(Integer.toString(i));
    }

    /**
     * Adds the string representation of the specified long to the end of this
     * StringBuffer.
     * 
     * @param l
     *            the long
     * @return this StringBuffer
     */
    public StringBuffer append(long l) {
        return append(Long.toString(l));
    }

    /**
     * Adds the string representation of the specified object to the end of this
     * StringBuffer.
     * 
     * @param obj
     *            the object
     * @return this StringBuffer
     */
    public synchronized StringBuffer append(Object obj) {
        if (obj == null) {
            appendNull();
        } else {
            append0(obj.toString());
        }
        return this;
    }

    /**
     * Adds the specified string to the end of this StringBuffer.
     * 
     * @param string
     *            the string
     * @return this StringBuffer
     */
    public synchronized StringBuffer append(String string) {
        append0(string);
        return this;
    }

    /**
     * Adds the specified StringBuffer to the end of this StringBuffer.
     * 
     * @param sb
     *            the StringBuffer
     * @return this StringBuffer
     * 
     * @since 1.4
     */
    public synchronized StringBuffer append(StringBuffer sb) {
        if (sb == null) {
            appendNull();
        } else {
            synchronized (sb) {
                append0(sb.getValue(), 0, sb.length());
            }
        }
        return this;
    }

    /**
     * Adds the character array to the end of this StringBuffer.
     * 
     * @param chars
     *            the character array
     * @return this StringBuffer
     * 
     * @throws NullPointerException
     *             when chars is null
     */
    public synchronized StringBuffer append(char chars[]) {
        append0(chars);
        return this;
    }

    /**
     * Adds the specified sequence of characters to the end of this
     * StringBuffer.
     * 
     * @param chars
     *            a character array
     * @param start
     *            the starting offset
     * @param length
     *            the number of characters
     * @return this StringBuffer
     * 
     * @throws ArrayIndexOutOfBoundsException
     *             when <code>length < 0, start < 0</code> or
     *             <code>start + length > chars.length</code>
     * @throws NullPointerException
     *             when chars is null
     */
    public synchronized StringBuffer append(char chars[], int start, int length) {
        append0(chars, start, length);
        return this;
    }

    /**
     * <p>
     * Appends the <code>CharSequence</code> to this buffer. If the
     * <code>CharSequence</code> is <code>null</code>, then the string
     * <code>"null"</code> is appended.
     * </p>
     * 
     * @param s
     *            The <code>CharSequence</code> to append.
     * @return A reference to this object.
     * @since 1.5
     */
    public synchronized StringBuffer append(CharSequence s) {
        if (s == null) {
            appendNull();
        } else {
            append0(s.toString());
        }
        return this;
    }

    /**
     * <p>
     * Appends the subsequence of the <code>CharSequence</code> to this
     * buffer. If the <code>CharSequence</code> is <code>null</code>, then
     * the string <code>"null"</code> is used to extract a subsequence.
     * </p>
     * 
     * @param s
     *            The <code>CharSequence</code> to append.
     * @param start
     *            The inclusive start index of the subsequence of the
     *            <code>CharSequence</code>.
     * @param end
     *            The exclusive end index of the subsequence of the
     *            <code>CharSequence</code>.
     * @return A reference to this object.
     * @since 1.5
     * @throws IndexOutOfBoundsException
     *             if <code>start</code> or <code>end</code> are negative,
     *             <code>start</code> is greater than <code>end</code> or
     *             <code>end</code> is greater than the length of
     *             <code>s</code>.
     */
    public synchronized StringBuffer append(CharSequence s, int start, int end) {
        append0(s, start, end);
        return this;
    }

    /**
     * <p>
     * Appends the encoded Unicode code point to this object. The code point is
     * converted to a <code>char[]</code> as defined by
     * {@link Character#toChars(int)}.
     * </p>
     * 
     * @param codePoint
     *            The Unicode code point to encode and append.
     * @return A reference to this object.
     * @see Character#toChars(int)
     * @since 1.5
     */
    public StringBuffer appendCodePoint(int codePoint) {
        return append(Character.toChars(codePoint));
    }

    /**
     * Returns the character at the specified offset in this StringBuffer.
     * 
     * @param index
     *            the zero-based index in this StringBuffer
     * @return the character at the index
     * 
     * @throws IndexOutOfBoundsException
     *             when <code>index < 0</code> or
     *             <code>index >= length()</code>
     */
    @Override
    public synchronized char charAt(int index) {
        return super.charAt(index);
    }

    /**
     * <p>
     * Retrieves the Unicode code point value at the <code>index</code>.
     * </p>
     * 
     * @param index
     *            The index to the <code>char</code> code unit within this
     *            object.
     * @return The Unicode code point value.
     * @throws IndexOutOfBoundsException
     *             if <code>index</code> is negative or greater than or equal
     *             to {@link #length()}.
     * @see Character
     * @see Character#codePointAt(char[], int, int)
     * @since 1.5
     */
    @Override
    public synchronized int codePointAt(int index) {
        return super.codePointAt(index);
    }

    /**
     * <p>
     * Retrieves the Unicode code point value that precedes the
     * <code>index</code>.
     * </p>
     * 
     * @param index
     *            The index to the <code>char</code> code unit within this
     *            object.
     * @return The Unicode code point value.
     * @throws IndexOutOfBoundsException
     *             if <code>index</code> is less than 1 or greater than
     *             {@link #length()}.
     * @see Character
     * @see Character#codePointBefore(char[], int, int)
     * @since 1.5
     */
    @Override
    public synchronized int codePointBefore(int index) {
        return super.codePointBefore(index);
    }

    /**
     * <p>
     * Calculates the number of Unicode code points between
     * <code>beginIndex</code> and <code>endIndex</code>.
     * </p>
     * 
     * @param beginIndex
     *            The inclusive beginning index of the subsequence.
     * @param endIndex
     *            The exclusive end index of the subsequence.
     * @return The number of Unicode code points in the subsequence.
     * @throws IndexOutOfBoundsException
     *             if <code>beginIndex</code> is negative or greater than
     *             <code>endIndex</code> or <code>endIndex</code> is greater
     *             than {@link #length()}.
     * @since 1.5
     */
    @Override
    public synchronized int codePointCount(int beginIndex, int endIndex) {
        return super.codePointCount(beginIndex, endIndex);
    }

    /**
     * Deletes a range of characters.
     * 
     * @param start
     *            the offset of the first character
     * @param end
     *            the offset one past the last character
     * @return this StringBuffer
     * 
     * @throws StringIndexOutOfBoundsException
     *             when <code>start < 0, start > end</code> or
     *             <code>end > length()</code>
     */
    public synchronized StringBuffer delete(int start, int end) {
        delete0(start, end);
        return this;
    }

    /**
     * Deletes a single character
     * 
     * @param location
     *            the offset of the character to delete
     * @return this StringBuffer
     * 
     * @throws StringIndexOutOfBoundsException
     *             when <code>location < 0</code> or
     *             <code>location >= length()</code>
     */
    public synchronized StringBuffer deleteCharAt(int location) {
        deleteCharAt0(location);
        return this;
    }

    /**
     * Ensures that this StringBuffer can hold the specified number of
     * characters without growing.
     * 
     * @param min
     *            the minimum number of elements that this StringBuffer will
     *            hold before growing
     */
    @Override
    public synchronized void ensureCapacity(int min) {
        super.ensureCapacity(min);
    }

    /**
     * Copies the specified characters in this StringBuffer to the character
     * array starting at the specified offset in the character array.
     * 
     * @param start
     *            the starting offset of characters to copy
     * @param end
     *            the ending offset of characters to copy
     * @param buffer
     *            the destination character array
     * @param idx
     *            the starting offset in the character array
     * 
     * @throws IndexOutOfBoundsException
     *             when <code>start < 0, end > length(),
     *              start > end, index < 0, end - start > buffer.length - index</code>
     * @throws NullPointerException
     *             when buffer is null
     */
    @Override
    public synchronized void getChars(int start, int end, char[] buffer, int idx) {
        super.getChars(start, end, buffer, idx);
    }

    /**
     * Searches in this StringBuffer for the index of the specified character.
     * The search for the character starts at the specified offset and moves
     * towards the end.
     * 
     * @param subString
     *            the string to find
     * @param start
     *            the starting offset
     * @return the index in this StringBuffer of the specified character, -1 if
     *         the character isn't found
     * 
     * @see #lastIndexOf(String,int)
     * 
     * @since 1.4
     */
    @Override
    public synchronized int indexOf(String subString, int start) {
        return super.indexOf(subString, start);
    }

    /**
     * Inserts the character at the specified offset in this StringBuffer.
     * 
     * @param index
     *            the index at which to insert
     * @param ch
     *            the character to insert
     * @return this StringBuffer
     * 
     * @throws ArrayIndexOutOfBoundsException
     *             when <code>index < 0</code> or
     *             <code>index > length()</code>
     */
    public synchronized StringBuffer insert(int index, char ch) {
        insert0(index, ch);
        return this;
    }

    /**
     * Inserts the string representation of the specified boolean at the
     * specified offset in this StringBuffer.
     * 
     * @param index
     *            the index at which to insert
     * @param b
     *            the boolean to insert
     * @return this StringBuffer
     * 
     * @throws StringIndexOutOfBoundsException
     *             when <code>index < 0</code> or
     *             <code>index > length()</code>
     */
    public StringBuffer insert(int index, boolean b) {
        return insert(index, b ? "true" : "false"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * Inserts the string representation of the specified integer at the
     * specified offset in this StringBuffer.
     * 
     * @param index
     *            the index at which to insert
     * @param i
     *            the integer to insert
     * @return this StringBuffer
     * 
     * @throws StringIndexOutOfBoundsException
     *             when <code>index < 0</code> or
     *             <code>index > length()</code>
     */
    public StringBuffer insert(int index, int i) {
        return insert(index, Integer.toString(i));
    }

    /**
     * Inserts the string representation of the specified long at the specified
     * offset in this StringBuffer.
     * 
     * @param index
     *            the index at which to insert
     * @param l
     *            the long to insert
     * @return this StringBuffer
     * 
     * @throws StringIndexOutOfBoundsException
     *             when <code>index < 0</code> or
     *             <code>index > length()</code>
     */
    public StringBuffer insert(int index, long l) {
        return insert(index, Long.toString(l));
    }

    /**
     * Inserts the string representation of the specified double at the
     * specified offset in this StringBuffer.
     * 
     * @param index
     *            the index at which to insert
     * @param d
     *            the double to insert
     * @return this StringBuffer
     * 
     * @throws StringIndexOutOfBoundsException
     *             when <code>index < 0</code> or
     *             <code>index > length()</code>
     */
    public StringBuffer insert(int index, double d) {
        return insert(index, Double.toString(d));
    }

    /**
     * Inserts the string representation of the specified float at the specified
     * offset in this StringBuffer.
     * 
     * @param index
     *            the index at which to insert
     * @param f
     *            the float to insert
     * @return this StringBuffer
     * 
     * @throws StringIndexOutOfBoundsException
     *             when <code>index < 0</code> or
     *             <code>index > length()</code>
     */
    public StringBuffer insert(int index, float f) {
        return insert(index, Float.toString(f));
    }

    /**
     * Inserts the string representation of the specified object at the
     * specified offset in this StringBuffer.
     * 
     * @param index
     *            the index at which to insert
     * @param obj
     *            the object to insert
     * @return this StringBuffer
     * 
     * @throws StringIndexOutOfBoundsException
     *             when <code>index < 0</code> or
     *             <code>index > length()</code>
     */
    public StringBuffer insert(int index, Object obj) {
        return insert(index, obj == null ? "null" : obj.toString()); //$NON-NLS-1$
    }

    /**
     * Inserts the string at the specified offset in this StringBuffer.
     * 
     * @param index
     *            the index at which to insert
     * @param string
     *            the string to insert
     * @return this StringBuffer
     * 
     * @throws StringIndexOutOfBoundsException
     *             when <code>index < 0</code> or
     *             <code>index > length()</code>
     */
    public synchronized StringBuffer insert(int index, String string) {
        insert0(index, string);
        return this;
    }

    /**
     * Inserts the character array at the specified offset in this StringBuffer.
     * 
     * @param index
     *            the index at which to insert
     * @param chars
     *            the character array to insert
     * @return this StringBuffer
     * 
     * @throws StringIndexOutOfBoundsException
     *             when <code>index < 0</code> or
     *             <code>index > length()</code>
     * @throws NullPointerException
     *             when chars is null
     */
    public synchronized StringBuffer insert(int index, char[] chars) {
        insert0(index, chars);
        return this;
    }

    /**
     * Inserts the specified sequence of characters at the specified offset in
     * this StringBuffer.
     * 
     * @param index
     *            the index at which to insert
     * @param chars
     *            a character array
     * @param start
     *            the starting offset
     * @param length
     *            the number of characters
     * @return this StringBuffer
     * 
     * @throws StringIndexOutOfBoundsException
     *             when <code>length < 0, start < 0,</code>
     *              <code>start + length > chars.length, index < 0</code>
     *             or <code>index > length()</code>
     * @throws NullPointerException
     *             when chars is null
     */
    public synchronized StringBuffer insert(int index, char chars[], int start,
            int length) {
        insert0(index, chars, start, length);
        return this;
    }

    /**
     * <p>
     * Inserts the <code>CharSequence</code> into this buffer at the
     * <code>index</code>. If <code>CharSequence</code> is
     * <code>null</code>, then the string <code>"null"</code> is inserted.
     * </p>
     * 
     * @param index
     *            The index of this buffer to insert the sequence.
     * @param s
     *            The <code>CharSequence</code> to insert.
     * @return A reference to this object.
     * @since 1.5
     * @throws IndexOutOfBoundsException
     *             if the index is invalid.
     */
    public synchronized StringBuffer insert(int index, CharSequence s) {
        insert0(index, s == null ? "null" : s.toString()); //$NON-NLS-1$
        return this;
    }

    /**
     * <p>
     * Inserts the <code>CharSequence</code> into this buffer at the
     * <code>index</code>. If <code>CharSequence</code> is
     * <code>null</code>, then the string <code>"null"</code> is inserted.
     * </p>
     * 
     * @param index
     *            The index of this buffer to insert the sequence.
     * @param s
     *            The <code>CharSequence</code> to insert.
     * @param start
     *            The inclusive start index of the subsequence of the
     *            <code>CharSequence</code>.
     * @param end
     *            The exclusive end index of the subsequence of the
     *            <code>CharSequence</code>.
     * @return A reference to this object.
     * @since 1.5
     * @throws IndexOutOfBoundsException
     *             if <code>index</code> is negative or greater than the
     *             current length, <code>start</code> or <code>end</code>
     *             are negative, <code>start</code> is greater than
     *             <code>end</code> or <code>end</code> is greater than the
     *             length of <code>s</code>.
     */
    public synchronized StringBuffer insert(int index, CharSequence s,
            int start, int end) {
        insert0(index, s, start, end);
        return this;
    }

    /**
     * Searches in this StringBuffer for the index of the specified character.
     * The search for the character starts at the specified offset and moves
     * towards the beginning.
     * 
     * @param subString
     *            the string to find
     * @param start
     *            the starting offset
     * @return the index in this StringBuffer of the specified character, -1 if
     *         the character isn't found
     * 
     * @see #indexOf(String,int)
     * 
     * @since 1.4
     */
    @Override
    public synchronized int lastIndexOf(String subString, int start) {
        return super.lastIndexOf(subString, start);
    }

    /**
     * <p>
     * Returns the index within this object that is offset from
     * <code>index</code> by <code>codePointOffset</code> code points.
     * </p>
     * 
     * @param index
     *            The index within this object to calculate the offset from.
     * @param codePointOffset
     *            The number of code points to count.
     * @return The index within this object that is the offset.
     * @throws IndexOutOfBoundsException
     *             if <code>index</code> is negative or greater than
     *             {@link #length()} or if there aren't enough code points
     *             before or after <code>index</code> to match
     *             <code>codePointOffset</code>.
     * @since 1.5
     */
    @Override
    public synchronized int offsetByCodePoints(int index, int codePointOffset) {
        return super.offsetByCodePoints(index, codePointOffset);
    }

    /**
     * Replace a range of characters with the characters in the specified
     * String.
     * 
     * @param start
     *            the offset of the first character
     * @param end
     *            the offset one past the last character
     * @param string
     *            a String
     * @return this StringBuffer
     * 
     * @throws StringIndexOutOfBoundsException
     *             when <code>start < 0</code> or <code>start > end</code>
     */
    public synchronized StringBuffer replace(int start, int end, String string) {
        replace0(start, end, string);
        return this;
    }

    /**
     * Reverses the order of characters in this StringBuffer.
     * 
     * @return this StringBuffer
     */
    public synchronized StringBuffer reverse() {
        reverse0();
        return this;
    }

    /**
     * Sets the character at the specified offset in this StringBuffer.
     * 
     * @param index
     *            the zero-based index in this StringBuffer
     * @param ch
     *            the character
     * 
     * @throws IndexOutOfBoundsException
     *             when <code>index < 0</code> or
     *             <code>index >= length()</code>
     */
    @Override
    public synchronized void setCharAt(int index, char ch) {
        super.setCharAt(index, ch);
    }

    /**
     * Sets the length of this StringBuffer to the specified length. If there
     * are more than length characters in this StringBuffer, the characters at
     * end are lost. If there are less than length characters in the
     * StringBuffer, the additional characters are set to <code>\\u0000</code>.
     * 
     * @param length
     *            the new length of this StringBuffer
     * 
     * @throws IndexOutOfBoundsException
     *             when <code>length < 0</code>
     * 
     * @see #length()
     */
    @Override
    public synchronized void setLength(int length) {
        super.setLength(length);
    }

    /**
     * Copies a range of characters into a new String.
     * 
     * @param start
     *            the offset of the first character
     * @param end
     *            the offset one past the last character
     * @return a new String containing the characters from start to end - 1
     * 
     * @throws IndexOutOfBoundsException
     *             when <code>start < 0, start > end</code> or
     *             <code>end > length()</code>
     * 
     * @since 1.4
     */
    @Override
    public synchronized CharSequence subSequence(int start, int end) {
        return super.substring(start, end);
    }

    /**
     * Copies a range of characters into a new String.
     * 
     * @param start
     *            the offset of the first character
     * @return a new String containing the characters from start to the end of
     *         the string
     * 
     * @throws StringIndexOutOfBoundsException
     *             when <code>start < 0</code> or
     *             <code>start > length()</code>
     */
    @Override
    public synchronized String substring(int start) {
        return super.substring(start);
    }

    /**
     * Copies a range of characters into a new String.
     * 
     * @param start
     *            the offset of the first character
     * @param end
     *            the offset one past the last character
     * @return a new String containing the characters from start to end - 1
     * 
     * @throws StringIndexOutOfBoundsException
     *             when <code>start < 0, start > end</code> or
     *             <code>end > length()</code>
     */
    @Override
    public synchronized String substring(int start, int end) {
        return super.substring(start, end);
    }

    /**
     * Returns the contents of this StringBuffer.
     * 
     * @return a String containing the characters in this StringBuffer
     */
    @Override
    public synchronized String toString() {
        return super.toString();
    }

    /**
     * <p>
     * Trims the storage capacity of this buffer down to the size of the current
     * character sequence. Execution of this method may change the results
     * returned by the {@link #capacity()} method, but this is not required.
     * </p>
     * 
     * @since 1.5
     */
    @Override
    public synchronized void trimToSize() {
        super.trimToSize();
    }

    private synchronized void writeObject(ObjectOutputStream out)
            throws IOException {
        ObjectOutputStream.PutField fields = out.putFields();
        fields.put("count", length()); //$NON-NLS-1$
        fields.put("shared", false); //$NON-NLS-1$
        fields.put("value", getValue()); //$NON-NLS-1$
        out.writeFields();
    }

    private void readObject(ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        ObjectInputStream.GetField fields = in.readFields();
        int count = fields.get("count", 0); //$NON-NLS-1$
        char[] value = (char[]) fields.get("value", null); //$NON-NLS-1$
        set(value, count);
    }
}
