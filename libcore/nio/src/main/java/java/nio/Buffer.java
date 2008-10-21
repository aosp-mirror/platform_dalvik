/* Licensed to the Apache Software Foundation (ASF) under one or more
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

package java.nio;


/**
 * A buffer is a list of elements of a specific primitive type.
 * <p>
 * A buffer can be described by following properties:
 * <ul>
 * <li>Capacity, is the number of elements a buffer can hold. Capacity is no
 * less than zero and never changes.</li>
 * <li>Position, is a cursor of this buffer. Elements are read or write at the
 * position if you do not specify an index explicitly. Position is no less than
 * zero and no greater than the limit.</li>
 * <li>Limit controls the scope of accessible elements. You can only read or
 * write elements from index zero to <code>limit - 1</code>. Accessing
 * elements out of the scope will cause exception. Limit is no less than zero
 * and no greater than capacity.</li>
 * <li>Mark, is used to remember the current position, so that you can reset
 * the position later. Mark is no less than zero and no greater than position.</li>
 * <li>A buffer can be readonly or read-write. Trying to modify the elements of
 * a readonly buffer will cause <code>ReadOnlyBufferException</code>, while
 * changing the position, limit and mark of a readonly buffer is OK.</li>
 * <li>A buffer can be direct or indirect. A direct buffer will try its best to
 * take advantage of native memory APIs and it may not stay in java heap, thus
 * not affected by GC.</li>
 * </ul>
 * </p>
 * <p>
 * Buffers are not thread-safe. If concurrent access to a buffer instance is
 * required, then the callers are responsible to take care of the
 * synchronization issues.
 * </p>
 *
 */
public abstract class Buffer {

    /**
     * <code>UNSET_MARK</code> means the mark has not been set.
     */
    final static int UNSET_MARK = -1;

    /**
     * The capacity of this buffer, which never change.
     */
    final int capacity;

    /**
     * <code>limit - 1</code> is the last element that can be read or write.
     * Limit must be no less than zero and no greater than <code>capacity</code>.
     */
    int limit;

    /**
     * Mark is the position will be set when <code>reset()</code> is called.
     * Mark is not set by default. Mark is always no less than zero and no
     * greater than <code>position</code>.
     */
    int mark = UNSET_MARK;

    /**
     * The current position of this buffer. Position is always no less than zero
     * and no greater than <code>limit</code>.
     */
    int position = 0;

    // BEGIN android-added
    /**
     * The log base 2 of the element size of this buffer.  Each typed subclass
     * (ByteBuffer, CharBuffer, etc.) is responsible for initializing this
     * value.  The value is used by native code to avoid the need for costly
     * 'instanceof' tests.
     *
     */
    int _elementSizeShift;

    /**
     * Returns the array associated with this buffer, or null if none exists.
     * Each typed subclass (ByteBuffer, CharBuffer, etc.) overrides this method
     * to call its array() method with appropriate checks.
     *
     * @return a primitive array or null
     */
    Object _array() {
        return null;
    }

    /**
     * Returns the offset into the backing array, if one exists, otherwise 0.
     * Each typed subclass (ByteBuffer, CharBuffer, etc.) overrides this method
     * to call its arrayOffset() method with appropriate checks.
     *
     * @return the array offset, or 0
     */
    int _arrayOffset() {
        return 0;
    }
    // END android-added

    /**
     * Construct a buffer with the specified capacity.
     *
     * @param capacity
     *            The capacity of this buffer
     */
    Buffer(int capacity) {
        super();
        if (capacity < 0) {
            throw new IllegalArgumentException();
        }
        this.capacity = this.limit = capacity;
    }

    /**
     * Returns the capacity of this buffer.
     *
     * @return The number of elements that are contained in this buffer.
     */
    public final int capacity() {
        return capacity;
    }

    /**
     * Clears this buffer.
     * <p>
     * While the content of this buffer is not changed the following internal
     * changes take place : the current position is reset back to the start of the buffer,
     * the value of the buffer limit is made equal to the capacity and mark is unset.
     * </p>
     *
     * @return This buffer
     */
    public final Buffer clear() {
        position = 0;
        mark = UNSET_MARK;
        limit = capacity;
        return this;
    }

    /**
     * Flips this buffer.
     * <p>
     * The limit is set to the current position, then the position is set to
     * zero, and the mark is cleared.
     * </p>
     * <p>
     * The content of this buffer is not changed.
     * </p>
     *
     * @return This buffer
     */
    public final Buffer flip() {
        limit = position;
        position = 0;
        mark = UNSET_MARK;
        return this;
    }

    /**
     * Returns true if there are remaining element(s) in this buffer.
     * <p>
     * Or more precisely, returns <code>position &lt; limit</code>.
     * </p>
     *
     * @return True if there are remaining element(s) in this buffer.
     */
    public final boolean hasRemaining() {
        return position < limit;
    }

    /**
     * Returns whether this buffer is readonly or not.
     *
     * @return Whether this buffer is readonly or not.
     */
    public abstract boolean isReadOnly();

    /**
     * Returns the limit of this buffer.
     *
     * @return The limit of this buffer.
     */
    public final int limit() {
        return limit;
    }

    /**
     * Sets the limit of this buffer.
     * <p>
     * If the current position in the buffer is in excess of
     * <code>newLimit</code> then, on returning from this call, it will have
     * been adjusted to be equivalent to <code>newLimit</code>. If the mark
     * is set and is greater than the new limit, then it is cleared.
     * </p>
     *
     * @param newLimit
     *            The new limit, must be no less than zero and no greater than
     *            capacity
     * @return This buffer
     * @exception IllegalArgumentException
     *                If <code>newLimit</code> is invalid.
     */
    public final Buffer limit(int newLimit) {
        if (newLimit < 0 || newLimit > capacity) {
            throw new IllegalArgumentException();
        }

        limit = newLimit;
        if (position > newLimit) {
            position = newLimit;
        }
        if ((mark != UNSET_MARK) && (mark > newLimit)) {
            mark = UNSET_MARK;
        }
        return this;
    }

    /**
     * Mark the current position, so that the position may return to this point
     * later by calling <code>reset()</code>.
     *
     * @return This buffer
     */
    public final Buffer mark() {
        mark = position;
        return this;
    }

    /**
     * Returns the position of this buffer.
     *
     * @return The value of this buffer's current position.
     */
    public final int position() {
        return position;
    }

    /**
     * Sets the position of this buffer.
     * <p>
     * If the mark is set and is greater than the new position, then it is
     * cleared.
     * </p>
     *
     * @param newPosition
     *            The new position, must be no less than zero and no greater
     *            than limit
     * @return This buffer
     * @exception IllegalArgumentException
     *                If <code>newPosition</code> is invalid
     */
    public final Buffer position(int newPosition) {
        if (newPosition < 0 || newPosition > limit) {
            throw new IllegalArgumentException();
        }

        position = newPosition;
        if ((mark != UNSET_MARK) && (mark > position)) {
            mark = UNSET_MARK;
        }
        return this;
    }

    /**
     * Returns the number of remaining elements in this buffer.
     * <p>
     * Or more precisely, returns <code>limit - position</code>.
     * </p>
     *
     * @return The number of remaining elements in this buffer.
     */
    public final int remaining() {
        return limit - position;
    }

    /**
     * Reset the position of this buffer to the <code>mark</code>.
     *
     * @return This buffer
     * @exception InvalidMarkException
     *                If the mark is not set
     */
    public final Buffer reset() {
        if (mark == UNSET_MARK) {
            throw new InvalidMarkException();
        }
        position = mark;
        return this;
    }

    /**
     * Rewinds this buffer.
     * <p>
     * The position is set to zero, and the mark is cleared.
     * </p>
     * <p>
     * The content of this buffer is not changed.
     * </p>
     *
     * @return This buffer
     */
    public final Buffer rewind() {
        position = 0;
        mark = UNSET_MARK;
        return this;
    }
}
