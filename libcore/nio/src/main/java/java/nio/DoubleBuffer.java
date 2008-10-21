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

/**
 * A buffer of <code>double</code>s.
 * <p>
 * A double buffer can be created in either of the following ways:
 * <ul>
 * <li>{@link #allocate(int) Allocate} a new double array and create a buffer
 * based on it;</li>
 * <li>{@link #wrap(double[]) Wrap} an existing double array to create a new
 * buffer;</li>
 * <li>Use
 * {@link java.nio.ByteBuffer#asDoubleBuffer() ByteBuffer.asDoubleBuffer} to
 * create a double buffer based on a byte buffer.</li>
 * </ul>
 * </p>
 */
public abstract class DoubleBuffer extends Buffer implements Comparable<DoubleBuffer> {

    /**
     * Creates a double buffer based on a new allocated double array.
     *
     * @param capacity  The capacity of the new buffer
     * @return The created double buffer
     * @throws IllegalArgumentException If <code>capacity</code> is less than zero
     */
    public static DoubleBuffer allocate(int capacity) {
        if (capacity < 0) {
            throw new IllegalArgumentException();
        }
        return BufferFactory.newDoubleBuffer(capacity);
    }

    /**
     * Creates a new double buffer by wrapping the given double array.
     * <p>
     * Calling this method has the same effect as
     * <code>wrap(array, 0, array.length)</code>.</p>
     *
     * @param array     The double array which the new buffer will be based on
     * @return The created double buffer
     */
    public static DoubleBuffer wrap(double[] array) {
        return wrap(array, 0, array.length);
    }

    /**
     * Creates new a double buffer by wrapping the given double array.
     * <p>
     * The new buffer's position will be <code>start</code>, limit will be
     * <code>start + len</code>, capacity will be the length of the array.
     * </p>
     *
     * @param array
     *            The double array which the new buffer will be based on
     * @param start
     *            The start index, must be no less than zero and no greater than
     *            <code>array.length</code>
     * @param len
     *            The length, must be no less than zero and no greater than
     *            <code>array.length - start</code>
     * @return The created double buffer
     * @exception IndexOutOfBoundsException
     *                If either <code>start</code> or <code>len</code> is
     *                invalid
     */
    public static DoubleBuffer wrap(double[] array, int start, int len) {
        int length = array.length;
        if (start < 0 || len < 0 || (long)start + (long)len > length) {
            throw new IndexOutOfBoundsException();
        }

        DoubleBuffer buf = BufferFactory.newDoubleBuffer(array);
        buf.position = start;
        buf.limit = start + len;

        return buf;
    }

    /**
     * Constructs a <code>DoubleBuffer</code> with given capacity.
     *
     * @param capacity  The capacity of the buffer
     */
    DoubleBuffer(int capacity) {
        super(capacity);
        // BEGIN android-added
        _elementSizeShift = 3;
        // END android-added
    }

    /**
     * Returns the double array which this buffer is based on, if there's one.
     *
     * @return The double array which this buffer is based on
     * @exception ReadOnlyBufferException
     *                If this buffer is based on an array, but it is readonly
     * @exception UnsupportedOperationException
     *                If this buffer is not based on an array
     */
    public final double[] array() {
        return protectedArray();
    }

    /**
     * Returns the offset of the double array which this buffer is based on, if
     * there's one.
     * <p>
     * The offset is the index of the array corresponds to the zero position of
     * the buffer.
     * </p>
     *
     * @return The offset of the double array which this buffer is based on
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
    public abstract DoubleBuffer asReadOnlyBuffer();

    /**
     * Compacts this double buffer.
     * <p>
     * The remaining <code>double</code>s will be moved to the head of the
     * buffer, staring from position zero. Then the position is set to
     * <code>remaining()</code>; the limit is set to capacity; the mark is
     * cleared.
     * </p>
     *
     * @return This buffer
     * @exception ReadOnlyBufferException
     *                If no changes may be made to the contents of this buffer
     */
    public abstract DoubleBuffer compact();

    /**
     * Compare the remaining <code>double</code>s of this buffer to another
     * double buffer's remaining <code>double</code>s.
     *
     * @param otherBuffer
     *            Another double buffer
     * @return a negative value if this is less than <code>other</code>; 0 if
     *         this equals to <code>other</code>; a positive value if this is
     *         greater than <code>other</code>
     * @exception ClassCastException
     *                If <code>other</code> is not a double buffer
     */
    public int compareTo(DoubleBuffer otherBuffer) {
        int compareRemaining = (remaining() < otherBuffer.remaining()) ? remaining()
                : otherBuffer.remaining();
        int thisPos = position;
        int otherPos = otherBuffer.position;
        double thisByte, otherByte;
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
    public abstract DoubleBuffer duplicate();

    /**
     * Tests whether this double buffer equals to another object.
     * <p>
     * If <code>other</code> is not a double buffer, then false is returned.</p>
     * <p>
     * Two double buffers are equals if, and only if, their remaining
     * <code>double</code>s are exactly the same. Position, limit, capacity and
     * mark are not considered.</p>
     *
     * @param other the object to be compared against
     * @return Whether this double buffer equals to another object.
     */
    public boolean equals(Object other) {
        if (!(other instanceof DoubleBuffer)) {
            return false;
        }
        DoubleBuffer otherBuffer = (DoubleBuffer) other;

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
     * Returns the double at the current position and increase the position by 1.
     *
     * @return The double at the current position.
     * @exception BufferUnderflowException If the position is equal or greater than limit
     */
    public abstract double get();

    /**
     * Reads <code>double</code>s from the current position into the
     * specified double array and increase the position by the number of
     * <code>double</code>s read.
     * <p>
     * Calling this method has the same effect as
     * <code>get(dest, 0, dest.length)</code>.
     * </p>
     *
     * @param dest
     *            The destination double array
     * @return This buffer
     * @exception BufferUnderflowException
     *                if <code>dest.length</code> is greater than
     *                <code>remaining()</code>
     */
    public DoubleBuffer get(double[] dest) {
        return get(dest, 0, dest.length);
    }

    /**
     * Reads <code>double</code>s from the current position into the
     * specified double array, starting from the specified offset, and increase
     * the position by the number of <code>double</code>s read.
     *
     * @param dest
     *            The target double array
     * @param off
     *            The offset of the double array, must be no less than zero and
     *            no greater than <code>dest.length</code>
     * @param len
     *            The number of <code>double</code>s to read, must be no less
     *            than zero and no greater than <code>dest.length - off</code>
     * @return This buffer
     * @exception IndexOutOfBoundsException
     *                If either <code>off</code> or <code>len</code> is
     *                invalid
     * @exception BufferUnderflowException
     *                If <code>len</code> is greater than
     *                <code>remaining()</code>
     */
    public DoubleBuffer get(double[] dest, int off, int len) {
        int length = dest.length;
        if (off < 0 || len < 0 || (long)off + (long)len > length) {
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
     * Returns a double at the specified index, and the position is not changed.
     *
     * @param index     The index, must be no less than zero and less than limit
     * @return A double at the specified index.
     * @exception IndexOutOfBoundsException If index is invalid
     */
    public abstract double get(int index);

    /**
     * Returns whether this buffer is based on a double array and is read/write.
     * <p>
     * If this buffer is readonly, then false is returned.</p>
     *
     * @return Whether this buffer is based on a double array and is read/write.
     */
    public final boolean hasArray() {
        return protectedHasArray();
    }

    /**
     * Hash code is calculated from the remaining <code>double</code>s.
     * <p>
     * Position, limit, capacity and mark don't affect the hash code.</p>
     *
     * @return The hash code calculated from the remaining <code>double</code>s.
     */
    public int hashCode() {
        int myPosition = position;
        int hash = 0;
        long l;
        while (myPosition < limit) {
            l = Double.doubleToLongBits(get(myPosition++));
            hash = hash + ((int) l) ^ ((int) (l >> 32));
        }
        return hash;
    }

    /**
     * Returns true if this buffer is direct.
     * <p>
     * A direct buffer will try its best to take advantage of native memory
     * APIs and it may not stay in java heap, thus not affected by GC.</p>
     * <p>
     * A double buffer is direct, if it is based on a byte buffer and the byte
     * buffer is direct.</p>
     *
     * @return True if this buffer is direct.
     */
    public abstract boolean isDirect();

    /**
     * Returns the byte order used by this buffer when converting
     * <code>double</code>s from/to <code>byte</code>s.
     * <p>
     * If this buffer is not based on a byte buffer, then always return the
     * platform's native byte order.</p>
     *
     * @return The byte order used by this buffer when converting
     * <code>double</code>s from/to <code>byte</code>s.
     */
    public abstract ByteOrder order();

    /**
     * Child class implements this method to realize <code>array()</code>.
     *
     * @return see <code>array()</code>
     */
    abstract double[] protectedArray();

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
     * Writes the given double to the current position and increase the position
     * by 1.
     *
     * @param d
     *            The double to write
     * @return This buffer
     * @exception BufferOverflowException
     *                If position is equal or greater than limit
     * @exception ReadOnlyBufferException
     *                If no changes may be made to the contents of this buffer
     */
    public abstract DoubleBuffer put(double d);

    /**
     * Writes <code>double</code>s in the given double array to the current
     * position and increase the position by the number of <code>double</code>s
     * written.
     * <p>
     * Calling this method has the same effect as
     * <code>put(src, 0, src.length)</code>.
     * </p>
     *
     * @param src
     *            The source double array
     * @return This buffer
     * @exception BufferOverflowException
     *                If <code>remaining()</code> is less than
     *                <code>src.length</code>
     * @exception ReadOnlyBufferException
     *                If no changes may be made to the contents of this buffer
     */
    public final DoubleBuffer put(double[] src) {
        return put(src, 0, src.length);
    }

    /**
     * Writes <code>double</code>s in the given double array, starting from
     * the specified offset, to the current position and increase the position
     * by the number of <code>double</code>s written.
     *
     * @param src
     *            The source double array
     * @param off
     *            The offset of double array, must be no less than zero and no
     *            greater than <code>src.length</code>
     * @param len
     *            The number of <code>double</code>s to write, must be no
     *            less than zero and no greater than
     *            <code>src.length - off</code>
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
    public DoubleBuffer put(double[] src, int off, int len) {
        int length = src.length;
        if (off < 0 || len < 0 || (long)off + (long)len > length) {
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
     * Writes all the remaining <code>double</code>s of the <code>src</code>
     * double buffer to this buffer's current position, and increase both
     * buffers' position by the number of <code>double</code>s copied.
     *
     * @param src
     *            The source double buffer
     * @return This buffer
     * @exception BufferOverflowException
     *                If <code>src.remaining()</code> is greater than this
     *                buffer's <code>remaining()</code>
     * @exception IllegalArgumentException
     *                If <code>src</code> is this buffer
     * @exception ReadOnlyBufferException
     *                If no changes may be made to the contents of this buffer
     */
    public DoubleBuffer put(DoubleBuffer src) {
        if (src == this) {
            throw new IllegalArgumentException();
        }
        if (src.remaining() > remaining()) {
            throw new BufferOverflowException();
        }
        double[] doubles = new double[src.remaining()];
        src.get(doubles);
        put(doubles);
        return this;
    }

    /**
     * Write a double to the specified index of this buffer and the position is
     * not changed.
     *
     * @param index
     *            The index, must be no less than zero and less than the limit
     * @param d
     *            The double to write
     * @return This buffer
     * @exception IndexOutOfBoundsException
     *                If index is invalid
     * @exception ReadOnlyBufferException
     *                If no changes may be made to the contents of this buffer
     */
    public abstract DoubleBuffer put(int index, double d);

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
    public abstract DoubleBuffer slice();

    /**
     * Returns a string represents the state of this double buffer.
     *
     * @return A string represents the state of this double buffer.
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
