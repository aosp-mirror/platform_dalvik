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

package java.nio;

import java.io.IOException;

/**
 * A buffer of <code>char</code>s.
 * <p>
 * A char buffer can be created in either of the following ways:
 * <ul>
 * <li>{@link #allocate(int) Allocate} a new char array and create a buffer
 * based on it;</li>
 * <li>{@link #wrap(char[]) Wrap} an existing char array to create a new
 * buffer;</li>
 * <li>{@link #wrap(CharSequence) Wrap} an existing char sequence to create a
 * new buffer;</li>
 * <li>Use {@link java.nio.ByteBuffer#asCharBuffer() ByteBuffer.asCharBuffer}
 * to create a char buffer based on a byte buffer.</li>
 * </ul>
 * </p>
 *
 */
public abstract class CharBuffer extends Buffer implements Comparable<CharBuffer>,
        CharSequence, Appendable, Readable {

    /**
     * Creates a char buffer based on a new allocated char array.
     *
     * @param capacity
     *            The capacity of the new buffer
     * @return The created char buffer
     * @throws IllegalArgumentException
     *             If <code>capacity</code> is less than zero
     */
    public static CharBuffer allocate(int capacity) {
        if (capacity < 0) {
            throw new IllegalArgumentException();
        }
        return BufferFactory.newCharBuffer(capacity);
    }

    /**
     * Creates a new char buffer by wrapping the given char array.
     * <p>
     * Calling this method has the same effect as
     * <code>wrap(array, 0, array.length)</code>.
     * </p>
     *
     * @param array
     *            The char array which the new buffer will be based on
     * @return The created char buffer
     */
    public static CharBuffer wrap(char[] array) {
        return wrap(array, 0, array.length);
    }

    /**
     * Creates new a char buffer by wrapping the given char array.
     * <p>
     * The new buffer's position will be <code>start</code>, limit will be
     * <code>start + len</code>, capacity will be the length of the array.
     * </p>
     *
     * @param array
     *            The char array which the new buffer will be based on
     * @param start
     *            The start index, must be no less than zero and no greater than
     *            <code>array.length</code>
     * @param len
     *            The length, must be no less than zero and no greater than
     *            <code>array.length - start</code>
     * @return The created char buffer
     * @exception IndexOutOfBoundsException
     *                If either <code>start</code> or <code>len</code> is
     *                invalid
     */
    public static CharBuffer wrap(char[] array, int start, int len) {
        int length = array.length;
        if ((start < 0) || (len < 0)
                || (long) start + (long) len > length) {
            throw new IndexOutOfBoundsException();
        }

        CharBuffer buf = BufferFactory.newCharBuffer(array);
        buf.position = start;
        buf.limit = start + len;

        return buf;
    }

    /**
     * Creates a new char buffer by wrapping the given char sequence.
     * <p>
     * Calling this method has the same effect as
     * <code>wrap(chseq, 0, chseq.length())</code>.
     * </p>
     *
     * @param chseq
     *            The char sequence which the new buffer will be based on
     * @return The created char buffer
     */
    public static CharBuffer wrap(CharSequence chseq) {
        return BufferFactory.newCharBuffer(chseq);
    }

    /**
     * Creates a new char buffer by wrapping the given char sequence.
     * <p>
     * The new buffer's position will be <code>start</code>, limit will be
     * <code>end</code>, capacity will be the length of the char sequence.
     * The new buffer is readonly.
     * </p>
     *
     * @param chseq
     *            The char sequence which the new buffer will be based on
     * @param start
     *            The start index, must be no less than zero and no greater than
     *            <code>chseq.length()</code>
     * @param end
     *            The end index, must be no less than <code>start</code> and
     *            no greater than <code>chseq.length()</code>
     * @return The created char buffer
     * @exception IndexOutOfBoundsException
     *                If either <code>start</code> or <code>end</code> is
     *                invalid
     */
    public static CharBuffer wrap(CharSequence chseq, int start, int end) {
        if (chseq == null) {
            throw new NullPointerException();
        }
        if (start < 0 || end < start || end > chseq.length()) {
            throw new IndexOutOfBoundsException();
        }

        CharBuffer result = BufferFactory.newCharBuffer(chseq);
        result.position = start;
        result.limit = end;
        return result;
    }

    /**
     * Constructs a <code>CharBuffer</code> with given capacity.
     *
     * @param capacity
     *            The capacity of the buffer
     */
    CharBuffer(int capacity) {
        super(capacity);
        // BEGIN android-added
        _elementSizeShift = 1;
        // END android-added
    }

    /**
     * Returns the char array which this buffer is based on, if there's one.
     *
     * @return The char array which this buffer is based on
     * @exception ReadOnlyBufferException
     *                If this buffer is based on an array, but it is readonly
     * @exception UnsupportedOperationException
     *                If this buffer is not based on an array
     */
    public final char[] array() {
        return protectedArray();
    }

    /**
     * Returns the offset of the char array which this buffer is based on, if
     * there's one.
     * <p>
     * The offset is the index of the array corresponds to the zero position of
     * the buffer.
     * </p>
     *
     * @return The offset of the char array which this buffer is based on
     * @exception ReadOnlyBufferException
     *                If this buffer is based on an array, but it is readonly
     * @exception UnsupportedOperationException
     *                If this buffer is not based on an array
     */
    public final int arrayOffset() {
        return protectedArrayOffset();
    }

    // BEGIN android-added
    @Override Object _array() {
        if (hasArray()) {
            return array();
        }
        return null;
    }

    @Override int _arrayOffset() {
        if (hasArray()) {
            return arrayOffset();
        }
        return 0;
    }
    // END android-added

    /**
     * Returns a readonly buffer that shares content with this buffer.
     * <p>
     * The returned buffer is guaranteed to be a new instance, even this buffer
     * is readonly itself. The new buffer's position, limit, capacity and mark
     * are the same as this buffer.
     * </p>
     * <p>
     * The new buffer shares content with this buffer, which means this buffer's
     * change of content will be visible to the new buffer. The two buffer's
     * position, limit and mark are independent.
     * </p>
     *
     * @return A readonly version of this buffer.
     */
    public abstract CharBuffer asReadOnlyBuffer();

    /**
     * Returns the character located at the specified index in the buffer. The
     * index value is referenced from the current buffer position.
     *
     * @param index
     *            The index referenced from the current buffer position. It must
     *            not be less than zero but less than the value obtained from a
     *            call to <code>remaining()</code>
     * @return the character located at the specified index (referenced from the
     *         current position) in the buffer.
     * @exception IndexOutOfBoundsException
     *                If the index is invalid
     */
    public final char charAt(int index) {
        if (index < 0 || index >= remaining()) {
            throw new IndexOutOfBoundsException();
        }
        return get(position + index);
    }

    /**
     * Compacts this char buffer.
     * <p>
     * The remaining <code>char</code>s will be moved to the head of the
     * buffer, staring from position zero. Then the position is set to
     * <code>remaining()</code>; the limit is set to capacity; the mark is
     * cleared.
     * </p>
     *
     * @return This buffer
     * @exception ReadOnlyBufferException
     *                If no changes may be made to the contents of this buffer
     */
    public abstract CharBuffer compact();

    /**
     * Compare the remaining <code>char</code>s of this buffer to another
     * char buffer's remaining <code>char</code>s.
     *
     * @param otherBuffer
     *            Another char buffer
     * @return a negative value if this is less than <code>other</code>; 0 if
     *         this equals to <code>other</code>; a positive value if this is
     *         greater than <code>other</code>
     * @exception ClassCastException
     *                If <code>other</code> is not a char buffer
     */
    public int compareTo(CharBuffer otherBuffer) {
        int compareRemaining = (remaining() < otherBuffer.remaining()) ? remaining()
                : otherBuffer.remaining();
        int thisPos = position;
        int otherPos = otherBuffer.position;
        char thisByte, otherByte;
        while (compareRemaining > 0) {
            thisByte = get(thisPos);
            otherByte = otherBuffer.get(otherPos);
            if (thisByte != otherByte) {
                return thisByte < otherByte ? -1 : 1;
            }
            thisPos++;
            otherPos++;
            compareRemaining--;
        }
        return remaining() - otherBuffer.remaining();
    }

    /**
     * Returns a duplicated buffer that shares content with this buffer.
     * <p>
     * The duplicated buffer's position, limit, capacity and mark are the same
     * as this buffer. The duplicated buffer's readonly property and byte order
     * are same as this buffer too.
     * </p>
     * <p>
     * The new buffer shares content with this buffer, which means either
     * buffer's change of content will be visible to the other. The two buffer's
     * position, limit and mark are independent.
     * </p>
     *
     * @return A duplicated buffer that shares content with this buffer.
     */
    public abstract CharBuffer duplicate();

    /**
     * Tests whether this char buffer equals to another object.
     * <p>
     * If <code>other</code> is not a char buffer, then false is returned.
     * </p>
     * <p>
     * Two char buffers are equals if, and only if, their remaining
     * <code>char</code>s are exactly the same. Position, limit, capacity and
     * mark are not considered.
     * </p>
     *
     * @param other
     *            the object to be compared against
     * @return Whether this char buffer equals to another object.
     */
    public boolean equals(Object other) {
        if (!(other instanceof CharBuffer)) {
            return false;
        }
        CharBuffer otherBuffer = (CharBuffer) other;

        if (remaining() != otherBuffer.remaining()) {
            return false;
        }

        int myPosition = position;
        int otherPosition = otherBuffer.position;
        boolean equalSoFar = true;
        while (equalSoFar && (myPosition < limit)) {
            equalSoFar = get(myPosition++) == otherBuffer.get(otherPosition++);
        }

        return equalSoFar;
    }

    /**
     * Returns the char at the current position and increase the position by 1.
     *
     * @return The char at the current position.
     * @exception BufferUnderflowException
     *                If the position is equal or greater than limit
     */
    public abstract char get();

    /**
     * Reads <code>char</code>s from the current position into the specified
     * char array and increase the position by the number of <code>char</code>s
     * read.
     * <p>
     * Calling this method has the same effect as
     * <code>get(dest, 0, dest.length)</code>.
     * </p>
     *
     * @param dest
     *            The destination char array
     * @return This buffer
     * @exception BufferUnderflowException
     *                if <code>dest.length</code> is greater than
     *                <code>remaining()</code>
     */
    public CharBuffer get(char[] dest) {
        return get(dest, 0, dest.length);
    }

    /**
     * Reads <code>char</code>s from the current position into the specified
     * char array, starting from the specified offset, and increase the position
     * by the number of <code>char</code>s read.
     *
     * @param dest
     *            The target char array
     * @param off
     *            The offset of the char array, must be no less than zero and no
     *            greater than <code>dest.length</code>
     * @param len
     *            The number of <code>char</code>s to read, must be no less
     *            than zero and no greater than <code>dest.length - off</code>
     * @return This buffer
     * @exception IndexOutOfBoundsException
     *                If either <code>off</code> or <code>len</code> is
     *                invalid
     * @exception BufferUnderflowException
     *                If <code>len</code> is greater than
     *                <code>remaining()</code>
     */
    public CharBuffer get(char[] dest, int off, int len) {
        int length = dest.length;
        if ((off < 0 ) || (len < 0) || (long)off + (long)len > length) {
            throw new IndexOutOfBoundsException();
        }

        if (len > remaining()) {
            throw new BufferUnderflowException();
        }
        for (int i = off; i < off + len; i++) {
            dest[i] = get();
        }
        return this;
    }

    /**
     * Returns a char at the specified index, and the position is not changed.
     *
     * @param index
     *            The index, must be no less than zero and less than limit
     * @return A char at the specified index.
     * @exception IndexOutOfBoundsException
     *                If index is invalid
     */
    public abstract char get(int index);

    /**
     * Returns whether this buffer is based on a char array and is read/write.
     * <p>
     * If this buffer is readonly, then false is returned.
     * </p>
     *
     * @return Whether this buffer is based on a char array and is read/write.
     */
    public final boolean hasArray() {
        return protectedHasArray();
    }

    /**
     * Hash code is calculated from the remaining <code>char</code>s.
     * <p>
     * Position, limit, capacity and mark don't affect the hash code.
     * </p>
     *
     * @return The hash code calculated from the remaining <code>char</code>s.
     */
    public int hashCode() {
        int myPosition = position;
        int hash = 0;
        while (myPosition < limit) {
            hash = hash + get(myPosition++);
        }
        return hash;
    }

    /**
     * Returns true if this buffer is direct.
     * <p>
     * A direct buffer will try its best to take advantage of native memory APIs
     * and it may not stay in java heap, thus not affected by GC.
     * </p>
     * <p>
     * A char buffer is direct, if it is based on a byte buffer and the byte
     * buffer is direct.
     * </p>
     *
     * @return True if this buffer is direct.
     */
    public abstract boolean isDirect();

    /**
     * Returns the number of remaining <code>char</code>s.
     *
     * @return The number of remaining <code>char</code>s.
     */
    public final int length() {
        return remaining();
    }

    /**
     * Returns the byte order used by this buffer when converting
     * <code>char</code>s from/to <code>byte</code>s.
     * <p>
     * If this buffer is not based on a byte buffer, then always return the
     * platform's native byte order.
     * </p>
     *
     * @return The byte order used by this buffer when converting
     *         <code>char</code>s from/to <code>byte</code>s.
     */
    public abstract ByteOrder order();

    /**
     * Child class implements this method to realize <code>array()</code>.
     *
     * @return see <code>array()</code>
     */
    abstract char[] protectedArray();

    /**
     * Child class implements this method to realize <code>arrayOffset()</code>.
     *
     * @return see <code>arrayOffset()</code>
     */
    abstract int protectedArrayOffset();

    /**
     * Child class implements this method to realize <code>hasArray()</code>.
     *
     * @return see <code>hasArray()</code>
     */
    abstract boolean protectedHasArray();

    /**
     * Writes the given char to the current position and increase the position
     * by 1.
     *
     * @param c
     *            The char to write
     * @return This buffer
     * @exception BufferOverflowException
     *                If position is equal or greater than limit
     * @exception ReadOnlyBufferException
     *                If no changes may be made to the contents of this buffer
     */
    public abstract CharBuffer put(char c);

    /**
     * Writes <code>char</code>s in the given char array to the current
     * position and increase the position by the number of <code>char</code>s
     * written.
     * <p>
     * Calling this method has the same effect as
     * <code>put(src, 0, src.length)</code>.
     * </p>
     *
     * @param src
     *            The source char array
     * @return This buffer
     * @exception BufferOverflowException
     *                If <code>remaining()</code> is less than
     *                <code>src.length</code>
     * @exception ReadOnlyBufferException
     *                If no changes may be made to the contents of this buffer
     */
    public final CharBuffer put(char[] src) {
        return put(src, 0, src.length);
    }

    /**
     * Writes <code>char</code>s in the given char array, starting from the
     * specified offset, to the current position and increase the position by
     * the number of <code>char</code>s written.
     *
     * @param src
     *            The source char array
     * @param off
     *            The offset of char array, must be no less than zero and no
     *            greater than <code>src.length</code>
     * @param len
     *            The number of <code>char</code>s to write, must be no less
     *            than zero and no greater than <code>src.length - off</code>
     * @return This buffer
     * @exception BufferOverflowException
     *                If <code>remaining()</code> is less than
     *                <code>len</code>
     * @exception IndexOutOfBoundsException
     *                If either <code>off</code> or <code>len</code> is
     *                invalid
     * @exception ReadOnlyBufferException
     *                If no changes may be made to the contents of this buffer
     */
    public CharBuffer put(char[] src, int off, int len) {
        int length = src.length;
        if ((off < 0 ) || (len < 0) || (long)off + (long)len > length) {
            throw new IndexOutOfBoundsException();
        }

        if (len > remaining()) {
            throw new BufferOverflowException();
        }
        for (int i = off; i < off + len; i++) {
            put(src[i]);
        }
        return this;
    }

    /**
     * Writes all the remaining <code>char</code>s of the <code>src</code>
     * char buffer to this buffer's current position, and increase both buffers'
     * position by the number of <code>char</code>s copied.
     *
     * @param src
     *            The source char buffer
     * @return This buffer
     * @exception BufferOverflowException
     *                If <code>src.remaining()</code> is greater than this
     *                buffer's <code>remaining()</code>
     * @exception IllegalArgumentException
     *                If <code>src</code> is this buffer
     * @exception ReadOnlyBufferException
     *                If no changes may be made to the contents of this buffer
     */
    public CharBuffer put(CharBuffer src) {
        if (src == this) {
            throw new IllegalArgumentException();
        }
        if (src.remaining() > remaining()) {
            throw new BufferOverflowException();
        }

        char[] contents = new char[src.remaining()];
        src.get(contents);
        put(contents);
        return this;
    }

    /**
     * Write a char to the specified index of this buffer and the position is
     * not changed.
     *
     * @param index
     *            The index, must be no less than zero and less than the limit
     * @param c
     *            The char to write
     * @return This buffer
     * @exception IndexOutOfBoundsException
     *                If index is invalid
     * @exception ReadOnlyBufferException
     *                If no changes may be made to the contents of this buffer
     */
    public abstract CharBuffer put(int index, char c);

    /**
     * Write all <code>char</code>s of the give string to the current
     * position of this buffer, and increase the position by the length of
     * string.
     * <p>
     * Calling this method has the same effect as
     * <code>put(str, 0, str.length())</code>.
     * </p>
     *
     * @param str
     *            The string to write
     * @return This buffer
     * @exception BufferOverflowException
     *                If <code>remaining()</code> is less than the length of
     *                string
     * @exception ReadOnlyBufferException
     *                If no changes may be made to the contents of this buffer
     */
    public final CharBuffer put(String str) {
        return put(str, 0, str.length());
    }

    /**
     * Write <code>char</code>s of the given string to the current position
     * of this buffer, and increase the position by the number of
     * <code>char</code>s written.
     *
     * @param str
     *            The string to write
     * @param start
     *            The first char to write, must be no less than zero and no
     *            greater than <code>str.length()</code>
     * @param end
     *            The last char to write (excluding), must be less than
     *            <code>start</code> and no greater than
     *            <code>str.length()</code>
     * @return This buffer
     * @exception BufferOverflowException
     *                If <code>remaining</code> is less than
     *                <code>end - start</code>
     * @exception IndexOutOfBoundsException
     *                If either <code>start</code> or <code>end</code> is
     *                invalid
     * @exception ReadOnlyBufferException
     *                If no changes may be made to the contents of this buffer
     */
    public CharBuffer put(String str, int start, int end) {
        int length = str.length();
        if (start < 0 || end < start || end > length) {
            throw new IndexOutOfBoundsException();
        }

        if (end - start > remaining()) {
            throw new BufferOverflowException();
        }
        for (int i = start; i < end; i++) {
            put(str.charAt(i));
        }
        return this;
    }

    /**
     * Returns a sliced buffer that shares content with this buffer.
     * <p>
     * The sliced buffer's capacity will be this buffer's
     * <code>remaining()</code>, and its zero position will correspond to
     * this buffer's current position. The new buffer's position will be 0,
     * limit will be its capacity, and its mark is unset. The new buffer's
     * readonly property and byte order are same as this buffer.
     * </p>
     * <p>
     * The new buffer shares content with this buffer, which means either
     * buffer's change of content will be visible to the other. The two buffer's
     * position, limit and mark are independent.
     * </p>
     *
     * @return A sliced buffer that shares content with this buffer.
     */
    public abstract CharBuffer slice();

    /**
     * Returns a new char buffer represents a sub-sequence of this buffer's
     * current remaining content.
     * <p>
     * The new buffer's position will be <code>position() + start</code>,
     * limit will be <code>position() + end</code>, capacity will be same as
     * this buffer. The new buffer's readonly property and byte order are same
     * as this buffer.
     * </p>
     * <p>
     * The new buffer shares content with this buffer, which means either
     * buffer's change of content will be visible to the other. The two buffer's
     * position, limit and mark are independent.
     * </p>
     *
     * @param start
     *            The start index of the sub-sequence, referenced from the
     *            current buffer position. Must not be less than zero and not
     *            greater than the value obtained from a call to
     *            <code>remaining()</code>.
     * @param end
     *            The end index of the sub-sequence, referenced from the current
     *            buffer position. Must not be less than <code>start</code>
     *            and not be greater than the value obtained from a call to
     *            <code>remaining()</code>
     * @return A new char buffer represents a sub-sequence of this buffer's
     *         current remaining content.
     * @exception IndexOutOfBoundsException
     *                If either <code>start</code> or <code>end</code> is
     *                invalid
     */
    public abstract CharSequence subSequence(int start, int end);

    /**
     * Returns a string represents the current remaining <code>char</code>s
     * of this buffer.
     *
     * @return A string represents the current remaining <code>char</code>s
     *         of this buffer.
     */
    public String toString() {
        StringBuffer strbuf = new StringBuffer();
        for (int i = position; i < limit; i++) {
            strbuf.append(get(i));
        }
        return strbuf.toString();
    }

    /**
     * @see Appendable#append(char)
     */
    public CharBuffer append(char c){
        return put(c);
    }

    /**
     * @see Appendable#append(CharSequence)
     */
    public CharBuffer append(CharSequence csq){
        if (csq != null) {
            return put(csq.toString());
        }
        return put("null"); //$NON-NLS-1$
    }

    /**
     * @see Appendable#append(CharSequence, int, int)
     */
    public CharBuffer append(CharSequence csq, int start, int end){
        if (csq == null) {
            csq = "null"; //$NON-NLS-1$
        }
        CharSequence cs = csq.subSequence(start, end);
        if (cs.length() > 0) {
            return put(cs.toString());
        }
        return this;
    }

    /**
     * @see Readable#read(CharBuffer)
     */
    public int read(CharBuffer target) throws IOException {
        if(target == this){
            throw new IllegalArgumentException();
        }
        if (remaining() == 0) {
            return target.remaining()==0?0:-1;
        }
        int result = Math.min(target.remaining(), remaining());
        char[] chars = new char[result];
        get(chars);
        target.put(chars);
        return result;
    }
}
