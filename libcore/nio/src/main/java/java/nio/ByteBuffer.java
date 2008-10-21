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


import org.apache.harmony.luni.platform.Endianness;

/**
 * A buffer of <code>byte</code>s.
 * <p>
 * A byte buffer can be created in either of the following ways:
 * <ul>
 * <li>{@link #allocate(int) Allocate} a new byte array and create a buffer
 * based on it;</li>
 * <li>{@link #allocateDirect(int) Allocate} a memory block and create a direct
 * buffer based on it;</li>
 * <li>{@link #wrap(byte[]) Wrap} an existing byte array to create a new
 * buffer.</li>
 * </ul>
 * </p>
 *
 */
public abstract class ByteBuffer extends Buffer implements Comparable<ByteBuffer> {

    /**
     * Creates a byte buffer based on a new allocated byte array.
     *
     * @param capacity
     *            The capacity of the new buffer
     * @return The created byte buffer
     * @throws IllegalArgumentException
     *             If <code>capacity</code> is less than zero
     */
    public static ByteBuffer allocate(int capacity) {
        if (capacity < 0) {
            throw new IllegalArgumentException();
        }
        return BufferFactory.newByteBuffer(capacity);
    }

    /**
     * Creates a direct byte buffer based on a new allocated memory block.
     *
     * @param capacity
     *            The capacity of the new buffer
     * @return The created byte buffer
     * @throws IllegalArgumentException
     *             If <code>capacity</code> is less than zero
     */
    public static ByteBuffer allocateDirect(int capacity) {
        if (capacity < 0) {
            throw new IllegalArgumentException();
        }
        return BufferFactory.newDirectByteBuffer(capacity);
    }

    /**
     * Creates a new byte buffer by wrapping the given byte array.
     * <p>
     * Calling this method has the same effect as
     * <code>wrap(array, 0, array.length)</code>.</p>
     *
     * @param array     The byte array which the new buffer will be based on
     * @return The created byte buffer
     */
    public static ByteBuffer wrap(byte[] array) {
        return BufferFactory.newByteBuffer(array);
    }

    /**
     * Creates new a byte buffer by wrapping the given byte array.
     * <p>
     * The new buffer's position will be <code>start</code>, limit will be
     * <code>start + len</code>, capacity will be the length of the array.
     * </p>
     *
     * @param array
     *            The byte array which the new buffer will be based on
     * @param start
     *            The start index, must be no less than zero and no greater than
     *            <code>array.length</code>
     * @param len
     *            The length, must be no less than zero and no greater than
     *            <code>array.length - start</code>
     * @return The created byte buffer
     * @exception IndexOutOfBoundsException
     *                If either <code>start</code> or <code>len</code> is
     *                invalid
     */
    public static ByteBuffer wrap(byte[] array, int start, int len) {
        int length = array.length;
        if ((start < 0) || (len < 0) || ((long) start + (long) len > length)) {
            throw new IndexOutOfBoundsException();
        }

        ByteBuffer buf = BufferFactory.newByteBuffer(array);
        buf.position = start;
        buf.limit = start + len;

        return buf;
    }

    /**
     * The byte order of this buffer, default is <code>BIG_ENDIAN</code>.
     */
    Endianness order = Endianness.BIG_ENDIAN;

    /**
     * Constructs a <code>ByteBuffer</code> with given capacity.
     *
     * @param capacity  The capacity of the buffer
     */
    ByteBuffer(int capacity) {
        super(capacity);
        // BEGIN android-added
        _elementSizeShift = 0;
        // END android-added
    }

    /**
     * Returns the byte array which this buffer is based on, if there's one.
     *
     * @return The byte array which this buffer is based on
     * @exception ReadOnlyBufferException
     *                If this buffer is based on a readonly array
     * @exception UnsupportedOperationException
     *                If this buffer is not based on an array
     */
    public final byte[] array() {
        return protectedArray();
    }

    /**
     * Returns the offset of the byte array which this buffer is based on, if
     * there's one.
     * <p>
     * The offset is the index of the array corresponds to the zero position of
     * the buffer.
     * </p>
     *
     * @return The offset of the byte array which this buffer is based on
     * @exception ReadOnlyBufferException
     *                If this buffer is based on a readonly array
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
     * Returns a char buffer which is based on the remaining content of
     * this byte buffer.
     * <p>
     * The new buffer's position is zero, its limit and capacity is
     * the number of remaining bytes divided by two, and its mark is not set.
     * The new buffer's readonly property and byte order are same as this
     * buffer. The new buffer is direct, if this byte buffer is direct.</p>
     * <p>
     * The new buffer shares content with this buffer, which means
     * either buffer's change of content will be visible to the other.
     * The two buffer's position, limit and mark are independent.</p>
     *
     * @return A char buffer which is based on the content of
     * this byte buffer.
     */
    public abstract CharBuffer asCharBuffer();

    /**
     * Returns a double buffer which is based on the remaining content of
     * this byte buffer.
     * <p>
     * The new buffer's position is zero, its limit and capacity is
     * the number of remaining bytes divided by two, and its mark is not set.
     * The new buffer's readonly property and byte order are same as this
     * buffer. The new buffer is direct, if this byte buffer is direct.</p>
     * <p>
     * The new buffer shares content with this buffer, which means
     * either buffer's change of content will be visible to the other.
     * The two buffer's position, limit and mark are independent.</p>
     *
     * @return A double buffer which is based on the content of
     * this byte buffer.
     */
    public abstract DoubleBuffer asDoubleBuffer();

    /**
     * Returns a float buffer which is based on the remaining content of
     * this byte buffer.
     * <p>
     * The new buffer's position is zero, its limit and capacity is
     * the number of remaining bytes divided by two, and its mark is not set.
     * The new buffer's readonly property and byte order are same as this
     * buffer. The new buffer is direct, if this byte buffer is direct.</p>
     * <p>
     * The new buffer shares content with this buffer, which means
     * either buffer's change of content will be visible to the other.
     * The two buffer's position, limit and mark are independent.</p>
     *
     * @return A float buffer which is based on the content of
     * this byte buffer.
     */
    public abstract FloatBuffer asFloatBuffer();

    /**
     * Returns a int buffer which is based on the remaining content of
     * this byte buffer.
     * <p>
     * The new buffer's position is zero, its limit and capacity is
     * the number of remaining bytes divided by two, and its mark is not set.
     * The new buffer's readonly property and byte order are same as this
     * buffer. The new buffer is direct, if this byte buffer is direct.</p>
     * <p>
     * The new buffer shares content with this buffer, which means
     * either buffer's change of content will be visible to the other.
     * The two buffer's position, limit and mark are independent.</p>
     *
     * @return A int buffer which is based on the content of
     * this byte buffer.
     */
    public abstract IntBuffer asIntBuffer();

    /**
     * Returns a long buffer which is based on the remaining content of
     * this byte buffer.
     * <p>
     * The new buffer's position is zero, its limit and capacity is
     * the number of remaining bytes divided by two, and its mark is not set.
     * The new buffer's readonly property and byte order are same as this
     * buffer. The new buffer is direct, if this byte buffer is direct.</p>
     * <p>
     * The new buffer shares content with this buffer, which means
     * either buffer's change of content will be visible to the other.
     * The two buffer's position, limit and mark are independent.</p>
     *
     * @return A long buffer which is based on the content of
     * this byte buffer.
     */
    public abstract LongBuffer asLongBuffer();

    /**
     * Returns a readonly buffer that shares content with this buffer.
     * <p>
     * The returned buffer is guaranteed to be a new instance, even this
     * buffer is readonly itself. The new buffer's position, limit, capacity
     * and mark are the same as this buffer.</p>
     * <p>
     * The new buffer shares content with this buffer, which means
     * this buffer's change of content will be visible to the new buffer.
     * The two buffer's position, limit and mark are independent.</p>
     *
     * @return A readonly version of this buffer.
     */
    public abstract ByteBuffer asReadOnlyBuffer();

    /**
     * Returns a short buffer which is based on the remaining content of
     * this byte buffer.
     * <p>
     * The new buffer's position is zero, its limit and capacity is
     * the number of remaining bytes divided by two, and its mark is not set.
     * The new buffer's readonly property and byte order are same as this
     * buffer. The new buffer is direct, if this byte buffer is direct.</p>
     * <p>
     * The new buffer shares content with this buffer, which means
     * either buffer's change of content will be visible to the other.
     * The two buffer's position, limit and mark are independent.</p>
     *
     * @return A short buffer which is based on the content of
     * this byte buffer.
     */
    public abstract ShortBuffer asShortBuffer();

    /**
     * Compacts this byte buffer.
     * <p>
     * The remaining <code>byte</code>s will be moved to the head of the
     * buffer, staring from position zero. Then the position is set to
     * <code>remaining()</code>; the limit is set to capacity; the mark is
     * cleared.
     * </p>
     *
     * @return This buffer
     * @exception ReadOnlyBufferException
     *                If no changes may be made to the contents of this buffer
     */
    public abstract ByteBuffer compact();

    /**
     * Compare the remaining <code>byte</code>s of this buffer to another
     * byte buffer's remaining <code>byte</code>s.
     *
     * @param otherBuffer
     *            Another byte buffer
     * @return a negative value if this is less than <code>other</code>; 0 if
     *         this equals to <code>other</code>; a positive value if this is
     *         greater than <code>other</code>
     * @exception ClassCastException
     *                If <code>other</code> is not a byte buffer
     */
    public int compareTo(ByteBuffer otherBuffer) {
        int compareRemaining = (remaining() < otherBuffer.remaining()) ? remaining()
                : otherBuffer.remaining();
        int thisPos = position;
        int otherPos = otherBuffer.position;
        byte thisByte, otherByte;
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
     * The duplicated buffer's position, limit, capacity and mark are the
     * same as this buffer. The duplicated buffer's readonly property and
     * byte order are same as this buffer too.</p>
     * <p>
     * The new buffer shares content with this buffer, which means
     * either buffer's change of content will be visible to the other.
     * The two buffer's position, limit and mark are independent.</p>
     *
     * @return A duplicated buffer that shares content with this buffer.
     */
    public abstract ByteBuffer duplicate();

    /**
     * Tests whether this byte buffer equals to another object.
     * <p>
     * If <code>other</code> is not a byte buffer, then false is returned.</p>
     * <p>
     * Two byte buffers are equals if, and only if, their remaining
     * <code>byte</code>s are exactly the same. Position, limit, capacity and
     * mark are not considered.</p>
     *
     * @param other the object to compare against
     * @return Whether this byte buffer equals to another object.
     */
    public boolean equals(Object other) {
        if (!(other instanceof ByteBuffer)) {
            return false;
        }
        ByteBuffer otherBuffer = (ByteBuffer) other;

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
     * Returns the byte at the current position and increase the position by 1.
     *
     * @return The byte at the current position.
     * @exception BufferUnderflowException
     *                If the position is equal or greater than limit
     */
    public abstract byte get();

    /**
     * Reads <code>byte</code>s from the current position into the specified
     * byte array and increase the position by the number of <code>byte</code>s
     * read.
     * <p>
     * Calling this method has the same effect as
     * <code>get(dest, 0, dest.length)</code>.
     * </p>
     *
     * @param dest
     *            The destination byte array
     * @return This buffer
     * @exception BufferUnderflowException
     *                if <code>dest.length</code> is greater than
     *                <code>remaining()</code>
     */
    public ByteBuffer get(byte[] dest) {
        return get(dest, 0, dest.length);
    }

    /**
     * Reads <code>byte</code>s from the current position into the specified
     * byte array, starting from the specified offset, and increase the position
     * by the number of <code>byte</code>s read.
     *
     * @param dest
     *            The target byte array
     * @param off
     *            The offset of the byte array, must be no less than zero and no
     *            greater than <code>dest.length</code>
     * @param len
     *            The number of <code>byte</code>s to read, must be no less
     *            than zero and no greater than <code>dest.length - off</code>
     * @return This buffer
     * @exception IndexOutOfBoundsException
     *                If either <code>off</code> or <code>len</code> is
     *                invalid
     * @exception BufferUnderflowException
     *                If <code>len</code> is greater than
     *                <code>remaining()</code>
     */
    public ByteBuffer get(byte[] dest, int off, int len) {
        int length = dest.length;
        if ((off < 0 ) || (len < 0) || ((long)off + (long)len > length)) {
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
     * Returns a byte at the specified index, and the position is not changed.
     *
     * @param index     The index, must be no less than zero and less than limit
     * @return A byte at the specified index.
     * @exception IndexOutOfBoundsException If index is invalid
     */
    public abstract byte get(int index);

    /**
     * Returns the char at the current position and increase the position by 2.
     * <p>
     * The 2 bytes start from the current position are composed into a char
     * according to current byte order and returned. The position increases by
     * 2.
     * </p>
     *
     * @return The char at the current position.
     * @exception BufferUnderflowException
     *                If the position is greater than <code>limit - 2</code>
     */
    public abstract char getChar();

    /**
     * Returns the char at the specified index.
     * <p>
     * The 2 bytes start from the specified index are composed into a char
     * according to current byte order and returned. The position is not
     * changed.
     * </p>
     *
     * @param index
     *            The index, must be no less than zero and equal or less than
     *            <code>limit - 2</code>
     * @return The char at the specified index.
     * @exception IndexOutOfBoundsException
     *                If <code>index</code> is invalid
     */
    public abstract char getChar(int index);

    /**
     * Returns the double at the current position and increase the position by
     * 8.
     * <p>
     * The 8 bytes start from the current position are composed into a double
     * according to current byte order and returned. The position increases by
     * 8.
     * </p>
     *
     * @return The double at the current position.
     * @exception BufferUnderflowException
     *                If the position is greater than <code>limit - 8</code>
     */
    public abstract double getDouble();

    /**
     * Returns the double at the specified index.
     * <p>
     * The 8 bytes start from the specified index are composed into a double
     * according to current byte order and returned. The position is not
     * changed.
     * </p>
     *
     * @param index
     *            The index, must be no less than zero and equal or less than
     *            <code>limit - 8</code>
     * @return The double at the specified index.
     * @exception IndexOutOfBoundsException
     *                If <code>index</code> is invalid
     */
    public abstract double getDouble(int index);

    /**
     * Returns the float at the current position and increase the position by 4.
     * <p>
     * The 4 bytes start from the current position are composed into a float
     * according to current byte order and returned. The position increases by
     * 4.
     * </p>
     *
     * @return The float at the current position.
     * @exception BufferUnderflowException
     *                If the position is greater than <code>limit - 4</code>
     */
    public abstract float getFloat();

    /**
     * Returns the float at the specified index.
     * <p>
     * The 4 bytes start from the specified index are composed into a float
     * according to current byte order and returned. The position is not
     * changed.
     * </p>
     *
     * @param index
     *            The index, must be no less than zero and equal or less than
     *            <code>limit - 4</code>
     * @return The float at the specified index.
     * @exception IndexOutOfBoundsException
     *                If <code>index</code> is invalid
     */
    public abstract float getFloat(int index);

    /**
     * Returns the int at the current position and increase the position by 4.
     * <p>
     * The 4 bytes start from the current position are composed into a int
     * according to current byte order and returned.
     * The position increases by 4.</p>
     *
     * @return The int at the current position.
     * @exception BufferUnderflowException If the position is greater than <code>limit - 4</code>
     */
    public abstract int getInt();

    /**
     * Returns the int at the specified index.
     * <p>
     * The 4 bytes start from the specified index are composed into a int
     * according to current byte order and returned. The position is not
     * changed.
     * </p>
     *
     * @param index
     *            The index, must be no less than zero and equal or less than
     *            <code>limit - 4</code>
     * @return The int at the specified index.
     * @exception IndexOutOfBoundsException
     *                If <code>index</code> is invalid
     */
    public abstract int getInt(int index);

    /**
     * Returns the long at the current position and increase the position by 8.
     * <p>
     * The 8 bytes start from the current position are composed into a long
     * according to current byte order and returned. The position increases by
     * 8.
     * </p>
     *
     * @return The long at the current position.
     * @exception BufferUnderflowException
     *                If the position is greater than <code>limit - 8</code>
     */
    public abstract long getLong();

    /**
     * Returns the long at the specified index.
     * <p>
     * The 8 bytes start from the specified index are composed into a long
     * according to current byte order and returned. The position is not
     * changed.
     * </p>
     *
     * @param index
     *            The index, must be no less than zero and equal or less than
     *            <code>limit - 8</code>
     * @return The long at the specified index.
     * @exception IndexOutOfBoundsException
     *                If <code>index</code> is invalid
     */
    public abstract long getLong(int index);

    /**
     * Returns the short at the current position and increase the position by 2.
     * <p>
     * The 2 bytes start from the current position are composed into a short
     * according to current byte order and returned.
     * The position increases by 2.</p>
     *
     * @return The short at the current position.
     * @exception BufferUnderflowException If the position is greater than <code>limit - 2</code>
     */
    public abstract short getShort();

    /**
     * Returns the short at the specified index.
     * <p>
     * The 2 bytes start from the specified index are composed into a short
     * according to current byte order and returned. The position is not
     * changed.
     * </p>
     *
     * @param index
     *            The index, must be no less than zero and equal or less than
     *            <code>limit - 2</code>
     * @return The short at the specified index.
     * @exception IndexOutOfBoundsException
     *                If <code>index</code> is invalid
     */
    public abstract short getShort(int index);

    /**
     * Returns whether this buffer is based on a byte array and is read/write.
     * <p>
     * If this buffer is readonly, then false is returned.</p>
     *
     * @return Whether this buffer is based on a byte array and is read/write.
     */
    public final boolean hasArray() {
        return protectedHasArray();
    }

    /**
     * Hash code is calculated from the remaining <code>byte</code>s.
     * <p>
     * Position, limit, capacity and mark don't affect the hash code.</p>
     *
     * @return The hash code calculated from the remaining <code>byte</code>s.
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
     * A byte buffer is direct, if it is based on a byte buffer and the byte
     * buffer is direct.
     * </p>
     *
     * @return True if this buffer is direct.
     */
    public abstract boolean isDirect();

    /**
     * Returns the byte order used by this buffer when converting
     * <code>byte</code>s from/to other primitive types.
     * <p>
     * The default byte order of byte buffer is always BIG_ENDIAN.</p>
     *
     * @return The byte order used by this buffer when converting
     * <code>byte</code>s from/to other primitive types.
     */
    public final ByteOrder order() {
        return order == Endianness.BIG_ENDIAN ? ByteOrder.BIG_ENDIAN
                : ByteOrder.LITTLE_ENDIAN;
    }

    /**
     * Sets the byte order of this buffer.
     *
     * @param byteOrder
     *            The byte order to set. If <code>null</code> then the order
     *            will be {@link ByteOrder#LITTLE_ENDIAN LITTLE_ENDIAN}.
     * @return This buffer
     * @see ByteOrder
     */
    public final ByteBuffer order(ByteOrder byteOrder) {
        return orderImpl(byteOrder);
    }

    ByteBuffer orderImpl(ByteOrder byteOrder) {
        order = byteOrder == ByteOrder.BIG_ENDIAN ? Endianness.BIG_ENDIAN
                : Endianness.LITTLE_ENDIAN;
        return this;
    }

    /**
     * Child class implements this method to realize <code>array()</code>.
     *
     * @return see <code>array()</code>
     */
    abstract byte[] protectedArray();

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
     * Writes the given byte to the current position and increase the position
     * by 1.
     *
     * @param b
     *            The byte to write
     * @return This buffer
     * @exception BufferOverflowException
     *                If position is equal or greater than limit
     * @exception ReadOnlyBufferException
     *                If no changes may be made to the contents of this buffer
     */
    public abstract ByteBuffer put(byte b);

    /**
     * Writes <code>byte</code>s in the given byte array to the current
     * position and increase the position by the number of <code>byte</code>s
     * written.
     * <p>
     * Calling this method has the same effect as
     * <code>put(src, 0, src.length)</code>.
     * </p>
     *
     * @param src
     *            The source byte array
     * @return This buffer
     * @exception BufferOverflowException
     *                If <code>remaining()</code> is less than
     *                <code>src.length</code>
     * @exception ReadOnlyBufferException
     *                If no changes may be made to the contents of this buffer
     */
    public final ByteBuffer put(byte[] src) {
        return put(src, 0, src.length);
    }

    /**
     * Writes <code>byte</code>s in the given byte array, starting from the
     * specified offset, to the current position and increase the position by
     * the number of <code>byte</code>s written.
     *
     * @param src
     *            The source byte array
     * @param off
     *            The offset of byte array, must be no less than zero and no
     *            greater than <code>src.length</code>
     * @param len
     *            The number of <code>byte</code>s to write, must be no less
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
    public ByteBuffer put(byte[] src, int off, int len) {
        int length = src.length;
        if ((off < 0 ) || (len < 0) || ((long)off + (long)len > length)) {
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
     * Writes all the remaining <code>byte</code>s of the <code>src</code>
     * byte buffer to this buffer's current position, and increase both buffers'
     * position by the number of <code>byte</code>s copied.
     *
     * @param src
     *            The source byte buffer
     * @return This buffer
     * @exception BufferOverflowException
     *                If <code>src.remaining()</code> is greater than this
     *                buffer's <code>remaining()</code>
     * @exception IllegalArgumentException
     *                If <code>src</code> is this buffer
     * @exception ReadOnlyBufferException
     *                If no changes may be made to the contents of this buffer
     */
    public ByteBuffer put(ByteBuffer src) {
        if (src == this) {
            throw new IllegalArgumentException();
        }
        if (src.remaining() > remaining()) {
            throw new BufferOverflowException();
        }
        byte[] contents = new byte[src.remaining()];
        src.get(contents);
        put(contents);
        return this;
    }

    /**
     * Write a byte to the specified index of this buffer and the position is
     * not changed.
     *
     * @param index
     *            The index, must be no less than zero and less than the limit
     * @param b
     *            The byte to write
     * @return This buffer
     * @exception IndexOutOfBoundsException
     *                If <code>index</code> is invalid
     * @exception ReadOnlyBufferException
     *                If no changes may be made to the contents of this buffer
     */
    public abstract ByteBuffer put(int index, byte b);

    /**
     * Writes the given char to the current position and increase the position
     * by 2.
     * <p>
     * The char is converted to bytes using the current byte order.
     * </p>
     *
     * @param value
     *            The char to write
     * @return This buffer
     * @exception BufferOverflowException
     *                If position is greater than <code>limit - 2</code>
     * @exception ReadOnlyBufferException
     *                If no changes may be made to the contents of this buffer
     */
    public abstract ByteBuffer putChar(char value);

    /**
     * Write a char to the specified index of this buffer.
     * <p>
     * The char is converted to bytes using the current byte order. The position
     * is not changed.
     * </p>
     *
     * @param index
     *            The index, must be no less than zero and equal or less than
     *            <code>limit - 2</code>
     * @param value
     *            The char to write
     * @return This buffer
     * @exception IndexOutOfBoundsException
     *                If <code>index</code> is invalid
     * @exception ReadOnlyBufferException
     *                If no changes may be made to the contents of this buffer
     */
    public abstract ByteBuffer putChar(int index, char value);

    /**
     * Writes the given double to the current position and increase the position
     * by 8.
     * <p>
     * The double is converted to bytes using the current byte order.
     * </p>
     *
     * @param value
     *            The double to write
     * @return This buffer
     * @exception BufferOverflowException
     *                If position is greater than <code>limit - 8</code>
     * @exception ReadOnlyBufferException
     *                If no changes may be made to the contents of this buffer
     */
    public abstract ByteBuffer putDouble(double value);

    /**
     * Write a double to the specified index of this buffer.
     * <p>
     * The double is converted to bytes using the current byte order. The
     * position is not changed.
     * </p>
     *
     * @param index
     *            The index, must be no less than zero and equal or less than
     *            <code>limit - 8</code>
     * @param value
     *            The double to write
     * @return This buffer
     * @exception IndexOutOfBoundsException
     *                If <code>index</code> is invalid
     * @exception ReadOnlyBufferException
     *                If no changes may be made to the contents of this buffer
     */
    public abstract ByteBuffer putDouble(int index, double value);

    /**
     * Writes the given float to the current position and increase the position
     * by 4.
     * <p>
     * The float is converted to bytes using the current byte order.
     * </p>
     *
     * @param value
     *            The float to write
     * @return This buffer
     * @exception BufferOverflowException
     *                If position is greater than <code>limit - 4</code>
     * @exception ReadOnlyBufferException
     *                If no changes may be made to the contents of this buffer
     */
    public abstract ByteBuffer putFloat(float value);

    /**
     * Write a float to the specified index of this buffer.
     * <p>
     * The float is converted to bytes using the current byte order. The
     * position is not changed.
     * </p>
     *
     * @param index
     *            The index, must be no less than zero and equal or less than
     *            <code>limit - 4</code>
     * @param value
     *            The float to write
     * @return This buffer
     * @exception IndexOutOfBoundsException
     *                If <code>index</code> is invalid
     * @exception ReadOnlyBufferException
     *                If no changes may be made to the contents of this buffer
     */
    public abstract ByteBuffer putFloat(int index, float value);

    /**
     * Writes the given int to the current position and increase the position by
     * 4.
     * <p>
     * The int is converted to bytes using the current byte order.
     * </p>
     *
     * @param value
     *            The int to write
     * @return This buffer
     * @exception BufferOverflowException
     *                If position is greater than <code>limit - 4</code>
     * @exception ReadOnlyBufferException
     *                If no changes may be made to the contents of this buffer
     */
    public abstract ByteBuffer putInt(int value);

    /**
     * Write a int to the specified index of this buffer.
     * <p>
     * The int is converted to bytes using the current byte order. The position
     * is not changed.
     * </p>
     *
     * @param index
     *            The index, must be no less than zero and equal or less than
     *            <code>limit - 4</code>
     * @param value
     *            The int to write
     * @return This buffer
     * @exception IndexOutOfBoundsException
     *                If <code>index</code> is invalid
     * @exception ReadOnlyBufferException
     *                If no changes may be made to the contents of this buffer
     */
    public abstract ByteBuffer putInt(int index, int value);

    /**
     * Writes the given long to the current position and increase the position
     * by 8.
     * <p>
     * The long is converted to bytes using the current byte order.
     * </p>
     *
     * @param value
     *            The long to write
     * @return This buffer
     * @exception BufferOverflowException
     *                If position is greater than <code>limit - 8</code>
     * @exception ReadOnlyBufferException
     *                If no changes may be made to the contents of this buffer
     */
    public abstract ByteBuffer putLong(long value);

    /**
     * Write a long to the specified index of this buffer.
     * <p>
     * The long is converted to bytes using the current byte order. The position
     * is not changed.
     * </p>
     *
     * @param index
     *            The index, must be no less than zero and equal or less than
     *            <code>limit - 8</code>
     * @param value
     *            The long to write
     * @return This buffer
     * @exception IndexOutOfBoundsException
     *                If <code>index</code> is invalid
     * @exception ReadOnlyBufferException
     *                If no changes may be made to the contents of this buffer
     */
    public abstract ByteBuffer putLong(int index, long value);

    /**
     * Writes the given short to the current position and increase the position
     * by 2.
     * <p>
     * The short is converted to bytes using the current byte order.
     * </p>
     *
     * @param value
     *            The short to write
     * @return This buffer
     * @exception BufferOverflowException
     *                If position is greater than <code>limit - 2</code>
     * @exception ReadOnlyBufferException
     *                If no changes may be made to the contents of this buffer
     */
    public abstract ByteBuffer putShort(short value);

    /**
     * Write a short to the specified index of this buffer.
     * <p>
     * The short is converted to bytes using the current byte order. The
     * position is not changed.
     * </p>
     *
     * @param index
     *            The index, must be no less than zero and equal or less than
     *            <code>limit - 2</code>
     * @param value
     *            The short to write
     * @return This buffer
     * @exception IndexOutOfBoundsException
     *                If <code>index</code> is invalid
     * @exception ReadOnlyBufferException
     *                If no changes may be made to the contents of this buffer
     */
    public abstract ByteBuffer putShort(int index, short value);

    /**
     * Returns a sliced buffer that shares content with this buffer.
     * <p>
     * The sliced buffer's capacity will be this buffer's
     * <code>remaining()</code>, and its zero position will correspond to
     * this buffer's current position. The new buffer's position will be
     * 0, limit will be its capacity, and its mark is unset. The new buffer's
     * readonly property and byte order are same as this buffer.</p>
     * <p>
     * The new buffer shares content with this buffer, which means
     * either buffer's change of content will be visible to the other.
     * The two buffer's position, limit and mark are independent.</p>
     *
     * @return A sliced buffer that shares content with this buffer.
     */
    public abstract ByteBuffer slice();

    /**
     * Returns a string represents the state of this byte buffer.
     *
     * @return A string represents the state of this byte buffer.
     */
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append(getClass().getName());
        buf.append(", status: capacity="); //$NON-NLS-1$
        buf.append(capacity());
        buf.append(" position="); //$NON-NLS-1$
        buf.append(position());
        buf.append(" limit="); //$NON-NLS-1$
        buf.append(limit());
        return buf.toString();
    }
}
