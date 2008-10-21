/* 
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package java.text;

/**
 * StringCharacterIterator is an implementation of CharacterIterator for
 * Strings.
 */
public final class StringCharacterIterator implements CharacterIterator {

    String string;

    int start, end, offset;

    /**
     * Constructs a new StringCharacterIterator on the specified String. The
     * begin and current indexes are set to the beginning of the String, the end
     * index is set to the length of the String.
     * 
     * @param value
     *            the new source String to iterate
     */
    public StringCharacterIterator(String value) {
        string = value;
        start = offset = 0;
        end = string.length();
    }

    /**
     * Constructs a new StringCharacterIterator on the specified String with the
     * current index set to the specified value. The begin index is set to the
     * beginning of the String, the end index is set to the length of the String.
     * 
     * @param value
     *            the new source String to iterate
     * @param location
     *            the current index
     * 
     * @exception IllegalArgumentException
     *                when the current index is less than zero or greater than
     *                the length of the String
     */
    public StringCharacterIterator(String value, int location) {
        string = value;
        start = 0;
        end = string.length();
        if (location < 0 || location > end) {
            throw new IllegalArgumentException();
        }
        offset = location;
    }

    /**
     * Constructs a new StringCharacterIterator on the specified String with the
     * begin, end and current index set to the specified values.
     * 
     * @param value
     *            the new source String to iterate
     * @param start
     *            the index of the first character to iterate
     * @param end
     *            the index one past the last character to iterate
     * @param location
     *            the current index
     * 
     * @exception IllegalArgumentException
     *                when the begin index is less than zero, the end index is
     *                greater than the String length, the begin index is greater
     *                than the end index, the current index is less than the
     *                begin index or greater than the end index
     */
    public StringCharacterIterator(String value, int start, int end,
            int location) {
        string = value;
        if (start < 0 || end > string.length() || start > end
                || location < start || location > end) {
            throw new IllegalArgumentException();
        }
        this.start = start;
        this.end = end;
        offset = location;
    }

    /**
     * Returns a new StringCharacterIterator with the same source String, begin,
     * end, and current index as this StringCharacterIterator.
     * 
     * @return a shallow copy of this StringCharacterIterator
     * 
     * @see java.lang.Cloneable
     */
    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    /**
     * Returns the character at the current index in the source String.
     * 
     * @return the current character, or DONE if the current index is past the
     *         end
     */
    public char current() {
        if (offset == end) {
            return DONE;
        }
        return string.charAt(offset);
    }

    /**
     * Compares the specified object to this StringCharacterIterator and answer
     * if they are equal. The object must be a StringCharacterIterator iterating
     * over the same sequence of characters with the same index.
     * 
     * @param object
     *            the object to compare with this object
     * @return true if the specified object is equal to this
     *         StringCharacterIterator, false otherwise
     * 
     * @see #hashCode
     */
    @Override
    public boolean equals(Object object) {
        if (!(object instanceof StringCharacterIterator)) {
            return false;
        }
        StringCharacterIterator it = (StringCharacterIterator) object;
        return string.equals(it.string) && start == it.start && end == it.end
                && offset == it.offset;
    }

    /**
     * Sets the current position to the begin index and returns the character at
     * the begin index.
     * 
     * @return the character at the begin index
     */
    public char first() {
        if (start == end) {
            return DONE;
        }
        offset = start;
        return string.charAt(offset);
    }

    /**
     * Returns the begin index in the source String.
     * 
     * @return the index of the first character to iterate
     */
    public int getBeginIndex() {
        return start;
    }

    /**
     * Returns the end index in the source String.
     * 
     * @return the index one past the last character to iterate
     */
    public int getEndIndex() {
        return end;
    }

    /**
     * Returns the current index in the source String.
     * 
     * @return the current index
     */
    public int getIndex() {
        return offset;
    }

    /**
     * Returns an integer hash code for the receiver. Objects which are equal
     * answer the same value for this method.
     * 
     * @return the receiver's hash
     * 
     * @see #equals
     */
    @Override
    public int hashCode() {
        return string.hashCode() + start + end + offset;
    }

    /**
     * Sets the current position to the end index - 1 and returns the character
     * at the current position.
     * 
     * @return the character before the end index
     */
    public char last() {
        if (start == end) {
            return DONE;
        }
        offset = end - 1;
        return string.charAt(offset);
    }

    /**
     * Increments the current index and returns the character at the new index.
     * 
     * @return the character at the next index, or DONE if the next index is
     *         past the end
     */
    public char next() {
        if (offset >= (end - 1)) {
            offset = end;
            return DONE;
        }
        return string.charAt(++offset);
    }

    /**
     * Decrements the current index and returns the character at the new index.
     * 
     * @return the character at the previous index, or DONE if the previous
     *         index is past the beginning
     */
    public char previous() {
        if (offset == start) {
            return DONE;
        }
        return string.charAt(--offset);
    }

    /**
     * Sets the current index in the source String.
     * 
     * @return the character at the new index, or DONE if the index is past the
     *         end
     * 
     * @exception IllegalArgumentException
     *                when the new index is less than the begin index or greater
     *                than the end index
     */
    public char setIndex(int location) {
        if (location < start || location > end) {
            throw new IllegalArgumentException();
        }
        offset = location;
        if (offset == end) {
            return DONE;
        }
        return string.charAt(offset);
    }

    /**
     * Sets the source String to iterate. The begin and end positions are set to
     * the start and end of this String.
     * 
     * @param value
     *            the new source String
     */
    public void setText(String value) {
        string = value;
        start = offset = 0;
        end = value.length();
    }
}
