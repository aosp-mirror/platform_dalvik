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
 * The interface to channels that can read a set of buffers in a single
 * operation.
 * <p>
 * The corresponding interface for writes is called
 * <code>GatheringByteChannel</code>.
 * 
 */
public interface ScatteringByteChannel extends ReadableByteChannel {

    /**
     * Reads bytes from the channel into all the given buffers.
     * <p>
     * This method is equivalent to:
     * 
     * <pre>
     * read(buffers, 0, buffers.length);
     * </pre>
     * 
     * </p>
     * 
     * @param buffers
     *            the array of byte buffers to receive the bytes being read.
     * @return the number of bytes actually read.
     * @throws ClosedChannelException
     *             if the channel is closed.
     * @throws NonReadableChannelException
     *             if the channel is open, but not in a mode that permits
     *             reading.
     * @throws ClosedByInterruptException
     *             if the thread is interrupted in its IO operation by another
     *             thread closing the channel.
     * @throws AsynchronousCloseException
     *             if the read is interrupted by another thread sending an
     *             explicit interrupt.
     * @throws IOException
     *             if some other type of exception occurs. Details are in the
     *             message.
     */
    public long read(ByteBuffer[] buffers) throws IOException;

    /**
     * Reads bytes from the channel into a subset of the given buffers.
     * <p>
     * This method attempts to read all of the <code>remaining()</code> bytes
     * from <code>length</code> byte buffers, in order, starting at
     * <code>buffers[offset]</code>. The number of bytes actually read is
     * returned.
     * </p>
     * <p>
     * If a read operation is in progress, subsequent threads will block until
     * the read is completed, and will then contend for the ability to read.
     * </p>
     * 
     * @param buffers
     *            the array of byte buffers into which the bytes will be read.
     * @param offset
     *            the index of the first buffer to read.
     * @param length
     *            the maximum number of buffers to read.
     * @return the number of bytes actually read.
     * @throws IndexOutOfBoundsException
     *             if offset < 0 or > buffers.length; or length < 0 or >
     *             buffers.length - offset.
     * @throws NonReadableChannelException
     *             if the channel was not opened for reading.
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
    public long read(ByteBuffer[] buffers, int offset, int length)
            throws IOException;

}
