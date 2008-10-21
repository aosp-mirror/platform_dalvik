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

import org.apache.harmony.luni.platform.PlatformAddress;
import org.apache.harmony.nio.internal.DirectBuffer;


class MappedByteBufferAdapter extends MappedByteBuffer implements DirectBuffer {

    private static final int CHAR_SIZE = 2;

    private static final int SHORT_SIZE = 2;

    private static final int INTEGER_SIZE = 4;

    private static final int LONG_SIZE = 8;

    private static final int FLOAT_SIZE = 4;

    private static final int DOUBLE_SIZE = 8;
    
    public MappedByteBufferAdapter(ByteBuffer buffer) {
        super(buffer);
    }

    public MappedByteBufferAdapter(PlatformAddress addr, int capa, int offset,
            int mode) {
        super(addr, capa, offset, mode);
    }

    public CharBuffer asCharBuffer() {
        return this.wrapped.asCharBuffer();
    }

    public DoubleBuffer asDoubleBuffer() {
        return this.wrapped.asDoubleBuffer();
    }

    public FloatBuffer asFloatBuffer() {
        return this.wrapped.asFloatBuffer();
    }

    public IntBuffer asIntBuffer() {
        return this.wrapped.asIntBuffer();
    }

    public LongBuffer asLongBuffer() {
        return this.wrapped.asLongBuffer();
    }

    public ByteBuffer asReadOnlyBuffer() {
        MappedByteBufferAdapter buf = new MappedByteBufferAdapter(this.wrapped
                .asReadOnlyBuffer());
        buf.limit = this.limit;
        buf.position = this.position;
        buf.mark = this.mark;
        return buf;
    }

    public ShortBuffer asShortBuffer() {
        return this.wrapped.asShortBuffer();
    }

    public ByteBuffer compact() {
        if (this.wrapped.isReadOnly()) {
            throw new ReadOnlyBufferException();
        }
        this.wrapped.limit(this.limit);
        this.wrapped.position(this.position);
        this.wrapped.compact();
        this.wrapped.clear();
        this.position = this.limit - this.position;
        this.limit = this.capacity;
        this.mark = UNSET_MARK;
        return this;
    }

    public ByteBuffer duplicate() {
        MappedByteBufferAdapter buf = new MappedByteBufferAdapter(this.wrapped
                .duplicate());
        buf.limit = this.limit;
        buf.position = this.position;
        buf.mark = this.mark;
        return buf;
    }

    public byte get() {
        if (this.position == this.limit) {
            throw new BufferUnderflowException();
        }
        return this.wrapped.get(this.position++);
    }

    public byte get(int index) {
        return this.wrapped.get(index);
    }

    public char getChar() {
        int newPosition = this.position + CHAR_SIZE;
        if (newPosition > this.limit) {
            throw new BufferUnderflowException();
        }
        char result = this.wrapped.getChar(this.position);
        this.position = newPosition;
        return result;
    }

    public char getChar(int index) {
        return this.wrapped.getChar(index);
    }

    public double getDouble() {
        int newPosition = this.position + DOUBLE_SIZE;
        if (newPosition > this.limit) {
            throw new BufferUnderflowException();
        }
        double result = this.wrapped.getDouble(this.position);
        this.position = newPosition;
        return result;
    }

    public double getDouble(int index) {
        return this.wrapped.getDouble(index);
    }

    public PlatformAddress getEffectiveAddress() {
        return ((DirectBuffer) this.wrapped).getEffectiveAddress();
    }

    public float getFloat() {
        int newPosition = this.position + FLOAT_SIZE;
        if (newPosition > this.limit) {
            throw new BufferUnderflowException();
        }
        float result = this.wrapped.getFloat(this.position);
        this.position = newPosition;
        return result;
    }

    public float getFloat(int index) {
        return this.wrapped.getFloat(index);
    }

    public int getInt() {
        int newPosition = this.position + INTEGER_SIZE;
        if (newPosition > this.limit) {
            throw new BufferUnderflowException();
        }
        int result = this.wrapped.getInt(this.position);
        this.position = newPosition;
        return result;
    }

    public int getInt(int index) {
        return this.wrapped.getInt(index);
    }

    public long getLong() {
        int newPosition = this.position + LONG_SIZE;
        if (newPosition > this.limit) {
            throw new BufferUnderflowException();
        }
        long result = this.wrapped.getLong(this.position);
        this.position = newPosition;
        return result;
    }

    public long getLong(int index) {
        return this.wrapped.getLong(index);
    }

    public short getShort() {
        int newPosition = this.position + SHORT_SIZE;
        if (newPosition > this.limit) {
            throw new BufferUnderflowException();
        }
        short result = this.wrapped.getShort(this.position);
        this.position = newPosition;
        return result;
    }

    public short getShort(int index) {
        return this.wrapped.getShort(index);
    }

    public boolean isDirect() {
        return true;
    }

    public boolean isReadOnly() {
        return this.wrapped.isReadOnly();
    }

    ByteBuffer orderImpl(ByteOrder byteOrder) {
        super.orderImpl(byteOrder);
        return this.wrapped.order(byteOrder);
    }

    public ByteBuffer put(byte b) {
        if (this.position == this.limit) {
            throw new BufferOverflowException();
        }
        this.wrapped.put(this.position++, b);
        return this;
    }

    public ByteBuffer put(byte[] src, int off, int len) {
        this.wrapped.position(this.position);
        this.wrapped.put(src, off, len);
        this.position += len;
        return this;
    }

    public ByteBuffer put(int index, byte b) {
        this.wrapped.put(index, b);
        return this;
    }

    public ByteBuffer putChar(char value) {
        int newPosition = this.position + CHAR_SIZE;
        if (newPosition > this.limit) {
            throw new BufferOverflowException();
        }
        this.wrapped.putChar(this.position, value);
        this.position = newPosition;
        return this;
    }

    public ByteBuffer putChar(int index, char value) {
        this.wrapped.putChar(index, value);
        return this;
    }

    public ByteBuffer putDouble(double value) {
        int newPosition = this.position + DOUBLE_SIZE;
        if (newPosition > this.limit) {
            throw new BufferOverflowException();
        }
        this.wrapped.putDouble(this.position, value);
        this.position = newPosition;
        return this;
    }

    public ByteBuffer putDouble(int index, double value) {
        this.wrapped.putDouble(index, value);
        return this;
    }

    public ByteBuffer putFloat(float value) {
        int newPosition = this.position + FLOAT_SIZE;
        if (newPosition > this.limit) {
            throw new BufferOverflowException();
        }
        this.wrapped.putFloat(this.position, value);
        this.position = newPosition;
        return this;
    }

    public ByteBuffer putFloat(int index, float value) {
        this.wrapped.putFloat(index, value);
        return this;
    }

    public ByteBuffer putInt(int index, int value) {
        this.wrapped.putInt(index, value);
        return this;
    }

    public ByteBuffer putInt(int value) {
        int newPosition = this.position + INTEGER_SIZE;
        if (newPosition > this.limit) {
            throw new BufferOverflowException();
        }
        this.wrapped.putInt(this.position, value);
        this.position = newPosition;
        return this;
    }

    public ByteBuffer putLong(int index, long value) {
        this.wrapped.putLong(index, value);
        return this;
    }

    public ByteBuffer putLong(long value) {
        int newPosition = this.position + LONG_SIZE;
        if (newPosition > this.limit) {
            throw new BufferOverflowException();
        }
        this.wrapped.putLong(this.position, value);
        this.position = newPosition;
        return this;
    }

    public ByteBuffer putShort(int index, short value) {
        this.wrapped.putShort(index, value);
        return this;
    }

    public ByteBuffer putShort(short value) {
        int newPosition = this.position + SHORT_SIZE;
        if (newPosition > this.limit) {
            throw new BufferOverflowException();
        }
        this.wrapped.putShort(this.position, value);
        this.position = newPosition;
        return this;
    }

    public ByteBuffer slice() {
        this.wrapped.limit(this.limit);
        this.wrapped.position(this.position);
        MappedByteBufferAdapter result = new MappedByteBufferAdapter(
                this.wrapped.slice());
        this.wrapped.clear();
        return result;
    }

    byte[] protectedArray() {
        return this.wrapped.protectedArray();
    }

    int protectedArrayOffset() {
        return this.wrapped.protectedArrayOffset();
    }

    boolean protectedHasArray() {
        return this.wrapped.protectedHasArray();
    }

    public PlatformAddress getBaseAddress() {
        return this.wrapped.getBaseAddress();
    }

    public boolean isAddressValid() {
        return this.wrapped.isAddressValid();
    }

    public void addressValidityCheck() {
        this.wrapped.addressValidityCheck();
    }

    public void free() {
        this.wrapped.free();
    }

    // BEGIN android-added
    // copied from newer version of harmony        
    public int getByteCapacity() {
        return wrapped.getByteCapacity();
    }
    // END android-added
}
