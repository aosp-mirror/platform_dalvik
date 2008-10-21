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

import org.apache.harmony.luni.platform.PlatformAddress;

/**
 * Helper class for operations on direct ByteBuffer
 * 
 * @see java.nio.ByteBuffer
 */
class DirectByteBuffers {

    /**
     * Explicitly frees the memory used by the given direct byte buffer.
     * <p>
     * If the memory is known to already have been freed then this is a no-op.
     * Once the memory has been freed then operations requiring access to the
     * memory will throw an <code>IllegalStateException</code>.
     * </p>
     * <p>
     * Note this is is possible that the memory is freed by code that reaches
     * into the address and explicitly frees it 'beneith' us -- this is bad
     * form.
     * </p>
     * 
     * @param directBuffer
     *            the direct byte buffer memory to free
     * @throws IllegalArgumentException
     *             if the buffer is <code>null</code> or is not a
     *             <em>direct</em> byte buffer.
     */
    public static void free(ByteBuffer directBuffer) {
        if ((directBuffer == null) || (!directBuffer.isDirect())) {
            throw new IllegalArgumentException();
        }
        DirectByteBuffer buf = (DirectByteBuffer) directBuffer;
        buf.free();
    }

    /**
     * Returns the platform address of the start of this buffer instance.
     * <em>You must not attempt to free the returned address!!</em> It may not
     * be an address that was explicitly malloc'ed (i.e. if this buffer is the
     * result of a split); and it may be memory shared by multiple buffers.
     * <p>
     * If you can guarantee that you want to free the underlying memory call the
     * #free() method on this instance -- generally applications will rely on
     * the garbage collector to autofree this memory.
     * </p>
     * 
     * @param directBuffer
     *            the direct byte buffer
     * @return the effective address of the start of the buffer.
     * @throws IllegalStateException
     *             if this buffer address is known to have been freed
     *             previously.
     */
    public static PlatformAddress getEffectiveAddress(ByteBuffer directBuffer) {
        return toDirectBuffer(directBuffer).getEffectiveAddress();
    }

    private static DirectByteBuffer toDirectBuffer(ByteBuffer directBuffer) {
        if ((directBuffer == null) || (!directBuffer.isDirect())) {
            throw new IllegalArgumentException();
        }
        return (DirectByteBuffer) directBuffer;
    }

}
