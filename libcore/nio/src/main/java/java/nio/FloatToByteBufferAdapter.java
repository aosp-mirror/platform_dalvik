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

// BEGIN android-added
// copied from newer version of harmony
import org.apache.harmony.nio.internal.DirectBuffer;
import org.apache.harmony.luni.platform.PlatformAddress;
// END android-added

/**
 * This class wraps a byte buffer to be a float buffer.
 * <p>
 * Implementation notice:
 * <ul>
 * <li>After a byte buffer instance is wrapped, it becomes privately owned by
 * the adapter. It must NOT be accessed outside the adapter any more.</li>
 * <li>The byte buffer's position and limit are NOT linked with the adapter.
 * The adapter extends Buffer, thus has its own position and limit.</li>
 * </ul>
 * </p>
 * 
 */
// BEGIN android-changed
// copied from newer version of harmony
final class FloatToByteBufferAdapter extends FloatBuffer implements DirectBuffer {
// END android-changed

    static FloatBuffer wrap(ByteBuffer byteBuffer) {
        return new FloatToByteBufferAdapter(byteBuffer.slice());
    }

    private final ByteBuffer byteBuffer;

    FloatToByteBufferAdapter(ByteBuffer byteBuffer) {
        super((byteBuffer.capacity() >> 2));
        this.byteBuffer = byteBuffer;
        this.byteBuffer.clear();
    }

    // BEGIN android-added
    // copied from newer version of harmony
    public int getByteCapacity() {
        if (byteBuffer instanceof DirectBuffer) {
            return ((DirectBuffer)byteBuffer).getByteCapacity();
        } else {
            assert false : byteBuffer;
            return -1;
        }            
    }

    public PlatformAddress getEffectiveAddress() {
        if (byteBuffer instanceof DirectBuffer) {
            return ((DirectBuffer)byteBuffer).getEffectiveAddress();
        } else {
            assert false : byteBuffer;
            return null;
        }
    }

    public PlatformAddress getBaseAddress() {
        if (byteBuffer instanceof DirectBuffer) {
            return ((DirectBuffer)byteBuffer).getBaseAddress();
        } else {
            assert false : byteBuffer;
            return null;
        }
    }
            
    public boolean isAddressValid() {
        if (byteBuffer instanceof DirectBuffer) {
            return ((DirectBuffer)byteBuffer).isAddressValid();
        } else {
            assert false : byteBuffer;
            return false;
        }
    }

    public void addressValidityCheck() {
        if (byteBuffer instanceof DirectBuffer) {
            ((DirectBuffer)byteBuffer).addressValidityCheck();
        } else {
            assert false : byteBuffer;
        }
    }
        
    public void free() {
        if (byteBuffer instanceof DirectBuffer) {
            ((DirectBuffer)byteBuffer).free();
        } else {
            assert false : byteBuffer;
        }   
    }
    // END android-added

    public FloatBuffer asReadOnlyBuffer() {
        FloatToByteBufferAdapter buf = new FloatToByteBufferAdapter(byteBuffer
                .asReadOnlyBuffer());
        buf.limit = limit;
        buf.position = position;
        buf.mark = mark;
        return buf;
    }

    public FloatBuffer compact() {
        if (byteBuffer.isReadOnly()) {
            throw new ReadOnlyBufferException();
        }
        byteBuffer.limit(limit << 2);
        byteBuffer.position(position << 2);
        byteBuffer.compact();
        byteBuffer.clear();
        position = limit - position;
        limit = capacity;
        mark = UNSET_MARK;
        return this;
    }

    public FloatBuffer duplicate() {
        FloatToByteBufferAdapter buf = new FloatToByteBufferAdapter(byteBuffer
                .duplicate());
        buf.limit = limit;
        buf.position = position;
        buf.mark = mark;
        return buf;
    }

    public float get() {
        if (position == limit) {
            throw new BufferUnderflowException();
        }
        return byteBuffer.getFloat(position++ << 2);
    }

    public float get(int index) {
        if (index < 0 || index >= limit) {
            throw new IndexOutOfBoundsException();
        }
        return byteBuffer.getFloat(index << 2);
    }

    public boolean isDirect() {
        return byteBuffer.isDirect();
    }

    public boolean isReadOnly() {
        return byteBuffer.isReadOnly();
    }

    public ByteOrder order() {
        return byteBuffer.order();
    }

    protected float[] protectedArray() {
        throw new UnsupportedOperationException();
    }

    protected int protectedArrayOffset() {
        throw new UnsupportedOperationException();
    }

    protected boolean protectedHasArray() {
        return false;
    }

    public FloatBuffer put(float c) {
        if (position == limit) {
            throw new BufferOverflowException();
        }
        byteBuffer.putFloat(position++ << 2, c);
        return this;
    }

    public FloatBuffer put(int index, float c) {
        if (index < 0 || index >= limit) {
            throw new IndexOutOfBoundsException();
        }
        byteBuffer.putFloat(index << 2, c);
        return this;
    }

    public FloatBuffer slice() {
        byteBuffer.limit(limit << 2);
        byteBuffer.position(position << 2);
        FloatBuffer result = new FloatToByteBufferAdapter(byteBuffer.slice());
        byteBuffer.clear();
        return result;
    }

}
