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

package java.nio.channels;


import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * The interface to channels that can write a set of buffers in a single
 * operation.
 * <p>
 * The corresponding interface for reads is called
 * <code>ScatteringByteChannel</code>.
 * 
 */
public interface GatheringByteChannel extends WritableByteChannel {

    /**
     * Writes bytes from all the given buffers to the channel.
     * <p>
     * This method is equivalent to:
     * 
     * <pre>
     * write(buffers, 0, buffers.length);
     * </pre>
     * 
     * </p>
     * 
     * @param buffers
     *            the buffers containing bytes to be written.
     * @return the number of bytes actually written.
     * @throws ClosedChannelException
     *             if the channel is closed.
     * @throws NonWritableChannelException
     *             if the channel is open, but not in a mode that permits
     *             writing.
     * @throws ClosedByInterruptException
     *             if the thread is interrupted in its IO operation by another
     *             thread closing the channel.
     * @throws AsynchronousCloseException
     *             if the write is interrupted by another thread sending an
     *             explicit interrupt.
     * @throws IOException
     *             if some other type of exception occurs. Details are in the
     *             message.
     */
    public long write(ByteBuffer[] buffers) throws IOException;

    /**
     * Writes a subset of the given bytes from the buffers to the channel.
     * <p>
     * This method attempts to write all of the <code>remaining()</code> bytes
     * from <code>length</code> byte buffers, in order, starting at
     * <code>buffers[offset]</code>. The number of bytes actually written is
     * returned.
     * </p>
     * <p>
     * If a write operation is in progress, subsequent threads will block until
     * the write is completed, and will then contend for the ability to write.
     * </p>
     * 
     * @param buffers
     *            the array of byte buffers containing the source of remaining
     *            bytes that will be attempted to be written.
     * @param offset
     *            the index of the first buffer to write.
     * @param length
     *            the number of buffers to write.
     * @return the number of bytes actually written.
     * @throws IndexOutOfBoundsException
     *             if offset < 0 or > buffers.length; or length < 0 or >
     *             buffers.length - offset.
     * @throws NonWritableChannelException
     *             if the channel was not opened for writing.
     * @throws ClosedChannelException
     *             the channel is currently closed.
     * @throws AsynchronousCloseException
     *             the channel was closed by another thread while the write was
     *             underway.
     * @throws ClosedByInterruptException
     *             the thread was interrupted by another thread while the write
     *             was underway.
     * @throws IOException
     *             if some other type of exception occurs. Details are in the
     *             message.
     */
    public long write(ByteBuffer[] buffers, int offset, int length)
            throws IOException;
}
