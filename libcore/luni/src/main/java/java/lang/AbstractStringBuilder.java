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

import java.io.InvalidObjectException;
import java.util.Arrays;

import org.apache.harmony.luni.util.Msg;

/**
 * <p>
 * A modifiable {@link CharSequence sequence of characters} for use in creating
 * and modifying Strings. This class is intended as a base class for
 * {@link StringBuffer} and {@link StringBuilder}.
 * </p>
 *
 * @see StringBuffer
 * @see StringBuilder
 * @since Android 1.0
 */
abstract class AbstractStringBuilder {

    static final int INITIAL_CAPACITY = 16;

    private char[] value;

    private int count;

    private boolean shared;

    /*
     * Returns the character array.
     */
    final char[] getValue() {
        return value;
    }

    /*
     * Returns the underlying buffer and sets the shared flag.
     */
    final char[] shareValue() {
        shared = true;
        return value;
    }

    /*
     * Restores internal state after deserialization.
     */
    final void set(char[] val, int len) throws InvalidObjectException {
        if (val == null) {
            val = new char[0];
        }
        if (val.length < len) {
            throw new InvalidObjectException(Msg.getString("K0199")); //$NON-NLS-1$
        }

        shared = false;
        value = val;
        count = len;
    }

    AbstractStringBuilder() {
        value = new char[INITIAL_CAPACITY];
    }

    AbstractStringBuilder(int capacity) {
        if (capacity < 0) {
            throw new NegativeArraySizeException();
        }
        value = new char[capacity];
    }

    AbstractStringBuilder(String string) {
        count = string.length();
        shared = false;
        value = new char[count + INITIAL_CAPACITY];
        // BEGIN android-changed
        string._getChars(0, count, value, 0);
        // END android-changed
    }

    private void enlargeBuffer(int min) {
        int twice = (value.length << 1) + 2;
        char[] newData = new char[min > twice ? min : twice];
        System.arraycopy(value, 0, newData, 0, count);
        value = newData;
        shared = false;
    }

    final void appendNull() {
        int newSize = count + 4;
        if (newSize > value.length) {
            enlargeBuffer(newSize);
        } else if (shared) {
            value = value.clone();
            shared = false;
        }
        value[count++] = 'n';
        value[count++] = 'u';
        value[count++] = 'l';
        value[count++] = 'l';
    }

    final void append0(char chars[]) {
        int newSize = count + chars.length;
        if (newSize > value.length) {
            enlargeBuffer(newSize);
        } else if (shared) {
            value = value.clone();
            shared = false;
        }
        System.arraycopy(chars, 0, value, count, chars.length);
        count = newSize;
    }

    final void append0(char chars[], int start, int length) {
        if (chars == null) {
            throw new NullPointerException();
        }
        // start + length could overflow, start/length maybe MaxInt
        if (start >= 0 && 0 <= length && length <= chars.length - start) {
            int newSize = count + length;
            if (newSize > value.length) {
                enlargeBuffer(newSize);
            } else if (shared) {
                value = value.clone();
                shared = false;
            }
            System.arraycopy(chars, start, value, count, length);
            count = newSize;
        } else {
            throw new ArrayIndexOutOfBoundsException();
        }
    }

    final void append0(char ch) {
        if (count == value.length) {
            enlargeBuffer(count + 1);
        }
        if (shared) {
            value = value.clone();
            shared = false;
        }
        value[count++] = ch;
    }

    final void append0(String string) {
        if (string == null) {
            appendNull();
            return;
        }
        int adding = string.length();
        int newSize = count + adding;
        if (newSize > value.length) {
            enlargeBuffer(newSize);
        } else if (shared) {
            value = value.clone();
            shared = false;
        }
        // BEGIN android-changed
        string._getChars(0, adding, value, count);
        // END android-changed
        count = newSize;
    }

    final void append0(CharSequence s, int start, int end) {
        if (s == null) {
            s = "null"; //$NON-NLS-1$
        }
        if (start < 0 || end < 0 || start > end || end > s.length()) {
            throw new IndexOutOfBoundsException();
        }

        // BEGIN android-changed
        int adding = end - start;
        int newSize = count + adding;
        if (newSize > value.length) {
            enlargeBuffer(newSize);
        } else if (shared) {
            value = value.clone();
            shared = false;
        }

        if (s instanceof String) {
            // BEGIN android-changed
            ((String) s)._getChars(start, end, value, count);
            // END android-changed
        } else {
            int j = count; // Destination index.
            for (int i = start; i < end; i++) {
                value[j++] = s.charAt(i);
            }
        }

        this.count = newSize;
        // END android-changed
    }

    /**
     * Returns the number of characters that can be held without growing.
     * 
     * @return the capacity
     * @see #ensureCapacity
     * @see #length
     */
    public int capacity() {
        return value.length;
    }

    /**
     * Retrieves the character at the {@code index}.
     * 
     * @param index
     *            the index of the character to retrieve.
     * @return the char value.
     * @throws IndexOutOfBoundsException
     *             if {@code index} is negative or greater than or equal to the
     *             current {@link #length()}.
     */
    public char charAt(int index) {
        if (index < 0 || index >= count) {
            throw new StringIndexOutOfBoundsException(index);
        }
        return value[index];
    }

    final void delete0(int start, int end) {
        if (start >= 0) {
            if (end > count) {
                end = count;
            }
            if (end == start) {
                return;
            }
            if (end > start) {
                int length = count - end;
                if (length > 0) {
                    if (!shared) {
                        System.arraycopy(value, end, value, start, length);
                    } else {
                        char[] newData = new char[value.length];
                        System.arraycopy(value, 0, newData, 0, start);
                        System.arraycopy(value, end, newData, start, length);
                        value = newData;
                        shared = false;
                    }
                }
                count -= end - start;
                return;
            }
        }
        throw new StringIndexOutOfBoundsException();
    }

    final void deleteCharAt0(int location) {
        if (0 > location || location >= count) {
            throw new StringIndexOutOfBoundsException(location);
        }
        int length = count - location - 1;
        if (length > 0) {
            if (!shared) {
                System.arraycopy(value, location + 1, value, location, length);
            } else {
                char[] newData = new char[value.length];
                System.arraycopy(value, 0, newData, 0, location);
                System
                        .arraycopy(value, location + 1, newData, location,
                                length);
                value = newData;
                shared = false;
            }
        }
        count--;
    }

    /**
     * Ensures that this object has a minimum capacity available before
     * requiring the internal buffer to be enlarged. The general policy of this
     * method is that if the {@code minimumCapacity} is larger than the current
     * {@link #capacity()}, then the capacity will be increased to the largest
     * value of either the {@code minimumCapacity} or the current capacity
     * multiplied by two plus two. Although this is the general policy, there is
     * no guarantee that the capacity will change.
     * 
     * @param min
     *            the new minimum capacity to set.
     */
    public void ensureCapacity(int min) {
        if (min > value.length) {
            enlargeBuffer(min);
        }
    }

    /**
     * Copies the requested sequence of characters to the {@code char[]} passed
     * starting at {@code destStart}.
     * 
     * @param start
     *            the inclusive start index of the characters to copy.
     * @param end
     *            the exclusive end index of the characters to copy.
     * @param dest
     *            the {@code char[]} to copy the characters to.
     * @param destStart
     *            the inclusive start index of {@code dest} to begin copying to.
     * @throws IndexOutOfBoundsException
     *             if the {@code start} is negative, the {@code destStart} is
     *             negative, the {@code start} is greater than {@code end}, the
     *             {@code end} is greater than the current {@link #length()} or
     *             {@code destStart + end - begin} is greater than
     *             {@code dest.length}.
     */
    public void getChars(int start, int end, char[] dest, int destStart) {
        if (start > count || end > count || start > end) {
            throw new StringIndexOutOfBoundsException();
        }
        System.arraycopy(value, start, dest, destStart, end - start);
    }

    final void insert0(int index, char[] chars) {
        if (0 > index || index > count) {
            throw new StringIndexOutOfBoundsException(index);
        }
        if (chars.length != 0) {
            move(chars.length, index);
            System.arraycopy(chars, 0, value, index, chars.length);
            count += chars.length;
        }
    }

    final void insert0(int index, char chars[], int start, int length) {
        if (0 <= index && index <= count) {
            // start + length could overflow, start/length maybe MaxInt
            if (start >= 0 && 0 <= length && length <= chars.length - start) {
                if (length != 0) {
                    move(length, index);
                    System.arraycopy(chars, start, value, index, length);
                    count += length;
                }
                return;
            }
            throw new StringIndexOutOfBoundsException("offset " + start
                    + ", len " + length + ", array.length " + chars.length);
        }
        throw new StringIndexOutOfBoundsException(index);
    }

    final void insert0(int index, char ch) {
        if (0 > index || index > count) {
            // RI compatible exception type
            throw new ArrayIndexOutOfBoundsException(index);
        }
        move(1, index);
        value[index] = ch;
        count++;
    }

    final void insert0(int index, String string) {
        if (0 <= index && index <= count) {
            if (string == null) {
                string = "null"; //$NON-NLS-1$
            }
            int min = string.length();
            if (min != 0) {
                move(min, index);
                // BEGIN android-changed
                string._getChars(0, min, value, index);
                // END android-changed
                count += min;
            }
        } else {
            throw new StringIndexOutOfBoundsException(index);
        }
    }

    final void insert0(int index, CharSequence s, int start, int end) {
        if (s == null) {
            s = "null"; //$NON-NLS-1$
        }
        if (index < 0 || index > count || start < 0 || end < 0 || start > end
                || end > s.length()) {
            throw new IndexOutOfBoundsException();
        }
        insert0(index, s.subSequence(start, end).toString());
    }

    /**
     * The current length.
     * 
     * @return the number of characters contained in this instance.
     */
    public int length() {
        return count;
    }

    private void move(int size, int index) {
        int newSize;
        if (value.length - count >= size) {
            if (!shared) {
                System.arraycopy(value, index, value, index + size, count
                        - index); // index == count case is no-op
                return;
            }
            newSize = value.length;
        } else {
            int a = count + size, b = (value.length << 1) + 2;
            newSize = a > b ? a : b;
        }

        char[] newData = new char[newSize];
        System.arraycopy(value, 0, newData, 0, index);
        // index == count case is no-op
        System.arraycopy(value, index, newData, index + size, count - index);
        value = newData;
        shared = false;
    }

    final void replace0(int start, int end, String string) {
        if (start >= 0) {
            if (end > count) {
                end = count;
            }
            if (end > start) {
                int stringLength = string.length();
                int diff = end - start - stringLength;
                if (diff > 0) { // replacing with fewer characters
                    if (!shared) {
                        // index == count case is no-op
                        System.arraycopy(value, end, value, start
                                + stringLength, count - end);
                    } else {
                        char[] newData = new char[value.length];
                        System.arraycopy(value, 0, newData, 0, start);
                        // index == count case is no-op
                        System.arraycopy(value, end, newData, start
                                + stringLength, count - end);
                        value = newData;
                        shared = false;
                    }
                } else if (diff < 0) {
                    // replacing with more characters...need some room
                    move(-diff, end);
                } else if (shared) {
                    value = value.clone();
                    shared = false;
                }
                // BEGIN android-changed
                string._getChars(0, stringLength, value, start);
                // END android-changed
                count -= diff;
                return;
            }
            if (start == end) {
                if (string == null) {
                    throw new NullPointerException();
                }
                insert0(start, string);
                return;
            }
        }
        throw new StringIndexOutOfBoundsException();
    }

    final void reverse0() {
        if (count < 2) {
            return;
        }
        if (!shared) {
            for (int i = 0, end = count, mid = count / 2; i < mid; i++) {
                char temp = value[--end];
                value[end] = value[i];
                value[i] = temp;
            }
        } else {
            char[] newData = new char[value.length];
            for (int i = 0, end = count; i < count; i++) {
                newData[--end] = value[i];
            }
            value = newData;
            shared = false;
        }
    }

    /**
     * Sets the character at the {@code index}.
     * 
     * @param index
     *            the zero-based index of the character to replace.
     * @param ch
     *            the character to set.
     * @throws IndexOutOfBoundsException
     *             if {@code index} is negative or greater than or equal to the
     *             current {@link #length()}.
     */
    public void setCharAt(int index, char ch) {
        if (0 > index || index >= count) {
            throw new StringIndexOutOfBoundsException(index);
        }
        if (shared) {
            value = value.clone();
            shared = false;
        }
        value[index] = ch;
    }

    /**
     * Sets the current length to a new value. If the new length is larger than
     * the current length, then the new characters at the end of this object
     * will contain the {@code char} value of {@code \u0000}.
     * 
     * @param length
     *            the new length of this StringBuffer.
     * @exception IndexOutOfBoundsException
     *                if {@code length < 0}.
     * @see #length
     */
    public void setLength(int length) {
        if (length < 0) {
            throw new StringIndexOutOfBoundsException(length);
        }
        if (count < length) {
            if (length > value.length) {
                enlargeBuffer(length);
            } else {
                if (shared) {
                    char[] newData = new char[value.length];
                    System.arraycopy(value, 0, newData, 0, count);
                    value = newData;
                    shared = false;
                } else {
                    Arrays.fill(value, count, length, (char) 0);
                }
            }
        }
        count = length;
    }

    /**
     * Returns the String value of the subsequence from the {@code start} index
     * to the current end.
     * 
     * @param start
     *            the inclusive start index to begin the subsequence.
     * @return a String containing the subsequence.
     * @throws StringIndexOutOfBoundsException
     *             if {@code start} is negative or greater than the current
     *             {@link #length()}.
     */
    public String substring(int start) {
        if (0 <= start && start <= count) {
            if (start == count) {
                return ""; //$NON-NLS-1$
            }

            shared = true;
            return new String(start, count - start, value);
        }
        throw new StringIndexOutOfBoundsException(start);
    }

    /**
     * Returns the String value of the subsequence from the {@code start} index
     * to the {@code end} index.
     * 
     * @param start
     *            the inclusive start index to begin the subsequence.
     * @param end
     *            the exclusive end index to end the subsequence.
     * @return a String containing the subsequence.
     * @throws StringIndexOutOfBoundsException
     *             if {@code start} is negative, greater than {@code end} or if
     *             {@code end} is greater than the current {@link #length()}.
     */
    public String substring(int start, int end) {
        if (0 <= start && start <= end && end <= count) {
            if (start == end) {
                return ""; //$NON-NLS-1$
            }

            shared = true;
            return new String(value, start, end - start);
        }
        throw new StringIndexOutOfBoundsException();
    }

    /**
     * Returns the current String representation.
     * 
     * @return a String containing the characters in this instance.
     */
    @Override
    public String toString() {
        if (count == 0) {
            return ""; //$NON-NLS-1$
        }

        if (count >= 256 && count <= (value.length >> 1)) {
            return new String(value, 0, count);
        }
        shared = true;
        return new String(0, count, value);
    }

    /**
     * Returns a {@code CharSequence} of the subsequence from the {@code start}
     * index to the {@code end} index.
     * 
     * @param start
     *            the inclusive start index to begin the subsequence.
     * @param end
     *            the exclusive end index to end the subsequence.
     * @return a CharSequence containing the subsequence.
     * @throws IndexOutOfBoundsException
     *             if {@code start} is negative, greater than {@code end} or if
     *             {@code end} is greater than the current {@link #length()}.
     */
    public CharSequence subSequence(int start, int end) {
        return substring(start, end);
    }

    /**
     * Searches for the first index of the specified character. The search for
     * the character starts at the beginning and moves towards the end.
     * 
     * @param string
     *            the string to find.
     * @return the index of the specified character, -1 if the character isn't
     *         found.
     * @see #lastIndexOf(String)
     */
    public int indexOf(String string) {
        return indexOf(string, 0);
    }

    /**
     * Searches for the index of the specified character. The search for the
     * character starts at the specified offset and moves towards the end.
     * 
     * @param subString
     *            the string to find.
     * @param start
     *            the starting offset.
     * @return the index of the specified character, -1 if the character isn't
     *         found
     * @see #lastIndexOf(String,int)
     */
    public int indexOf(String subString, int start) {
        if (start < 0) {
            start = 0;
        }
        int subCount = subString.length();
        if (subCount > 0) {
            if (subCount + start > count) {
                return -1;
            }
            // TODO optimize charAt to direct array access
            char firstChar = subString.charAt(0);
            while (true) {
                int i = start;
                boolean found = false;
                for (; i < count; i++) {
                    if (value[i] == firstChar) {
                        found = true;
                        break;
                    }
                }
                if (!found || subCount + i > count) {
                    return -1; // handles subCount > count || start >= count
                }
                int o1 = i, o2 = 0;
                while (++o2 < subCount && value[++o1] == subString.charAt(o2)) {
                    // Intentionally empty
                }
                if (o2 == subCount) {
                    return i;
                }
                start = i + 1;
            }
        }
        return (start < count || start == 0) ? start : count;
    }

    /**
     * Searches for the last index of the specified character. The search for
     * the character starts at the end and moves towards the beginning.
     * 
     * @param string
     *            the string to find.
     * @return the index of the specified character, -1 if the character isn't
     *         found.
     * @see String#lastIndexOf(String)
     */
    public int lastIndexOf(String string) {
        return lastIndexOf(string, count);
    }

    /**
     * Searches for the index of the specified character. The search for the
     * character starts at the specified offset and moves towards the beginning.
     * 
     * @param subString
     *            the string to find.
     * @param start
     *            the starting offset.
     * @return the index of the specified character, -1 if the character isn't
     *         found.
     * @see String#lastIndexOf(String,int)
     */
    public int lastIndexOf(String subString, int start) {
        int subCount = subString.length();
        if (subCount <= count && start >= 0) {
            if (subCount > 0) {
                if (start > count - subCount) {
                    start = count - subCount; // count and subCount are both
                }
                // >= 1
                // TODO optimize charAt to direct array access
                char firstChar = subString.charAt(0);
                while (true) {
                    int i = start;
                    boolean found = false;
                    for (; i >= 0; --i) {
                        if (value[i] == firstChar) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        return -1;
                    }
                    int o1 = i, o2 = 0;
                    while (++o2 < subCount
                            && value[++o1] == subString.charAt(o2)) {
                        // Intentionally empty
                    }
                    if (o2 == subCount) {
                        return i;
                    }
                    start = i - 1;
                }
            }
            return start < count ? start : count;
        }
        return -1;
    }

    /**
     * Trims off any extra capacity beyond the current length. Note, this method
     * is NOT guaranteed to change the capacity of this object.
     */
    public void trimToSize() {
        if (count < value.length) {
            char[] newValue = new char[count];
            System.arraycopy(value, 0, newValue, 0, count);
            value = newValue;
            shared = false;
        }
    }

    /**
     * Retrieves the Unicode code point value at the {@code index}.
     * 
     * @param index
     *            the index to the {@code char} code unit.
     * @return the Unicode code point value.
     * @throws IndexOutOfBoundsException
     *             if {@code index} is negative or greater than or equal to
     *             {@link #length()}.
     * @see Character
     * @see Character#codePointAt(char[], int, int)
     */
    public int codePointAt(int index) {
        if (index < 0 || index >= count) {
            throw new StringIndexOutOfBoundsException(index);
        }
        return Character.codePointAt(value, index, count);
    }

    /**
     * Retrieves the Unicode code point value that precedes the {@code index}.
     * 
     * @param index
     *            the index to the {@code char} code unit within this object.
     * @return the Unicode code point value.
     * @throws IndexOutOfBoundsException
     *             if {@code index} is less than 1 or greater than
     *             {@link #length()}.
     * @see Character
     * @see Character#codePointBefore(char[], int)
     */
    public int codePointBefore(int index) {
        if (index < 1 || index > count) {
            throw new StringIndexOutOfBoundsException(index);
        }
        return Character.codePointBefore(value, index);
    }

    /**
     * <p>
     * Calculates the number of Unicode code points between {@code beginIndex}
     * and {@code endIndex}.
     * </p>
     * 
     * @param beginIndex
     *            the inclusive beginning index of the subsequence.
     * @param endIndex
     *            the exclusive end index of the subsequence.
     * @return the number of Unicode code points in the subsequence.
     * @throws IndexOutOfBoundsException
     *             if {@code beginIndex} is negative or greater than
     *             {@code endIndex} or {@code endIndex} is greater than
     *             {@link #length()}.
     * @see Character
     * @see Character#codePointCount(char[], int, int)
     */
    public int codePointCount(int beginIndex, int endIndex) {
        if (beginIndex < 0 || endIndex > count || beginIndex > endIndex) {
            throw new StringIndexOutOfBoundsException();
        }
        return Character.codePointCount(value, beginIndex, endIndex
                - beginIndex);
    }

    /**
     * <p>
     * Returns the index that is offset {@code codePointOffset} code points from
     * {@code index}.
     * </p>
     * 
     * @param index
     *            the index to calculate the offset from.
     * @param codePointOffset
     *            the number of code points to count.
     * @return the index that is {@code codePointOffset} code points away from
     *         index.
     * @throws IndexOutOfBoundsException
     *             if {@code index} is negative or greater than
     *             {@link #length()} or if there aren't enough code points
     *             before or after {@code index} to match
     *             {@code codePointOffset}.
     * @see Character
     * @see Character#offsetByCodePoints(char[], int, int, int, int)
     */
    public int offsetByCodePoints(int index, int codePointOffset) {
        return Character.offsetByCodePoints(value, 0, count, index,
                codePointOffset);
    }
}
