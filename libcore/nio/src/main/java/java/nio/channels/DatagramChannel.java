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

package java.nio.channels;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.spi.AbstractSelectableChannel;
import java.nio.channels.spi.SelectorProvider;

import org.apache.harmony.luni.platform.Platform;

/**
 * A DatagramChannel is a selectable channel for part abstraction of datagram
 * socket. The <code>socket</code> method of this class can return the related
 * <code>DatagramSocket</code> instance, which can handle the socket.
 * <p>
 * A datagram channel is open but not connected when created by
 * <code>open</code> method. After connected, it will keep the connected
 * status before disconnecting or closing. The benefit of a connected channel is
 * the reduced effort of security checks during send and receive. When invoking
 * <code>read</code> or <code>write</code>, a connected channel is
 * required.
 * </p>
 * <p>
 * Datagram channels are thread-safe, no more than one thread can read or write
 * at given time.
 * </p>
 * 
 */
public abstract class DatagramChannel extends AbstractSelectableChannel
        implements ByteChannel, ScatteringByteChannel, GatheringByteChannel {

    static {
        Platform.getNetworkSystem().oneTimeInitialization(true);
    }
    
    /**
     * Constructor for this class.
     * 
     * @param selectorProvider
     *            A instance of SelectorProvider
     */
    protected DatagramChannel(SelectorProvider selectorProvider) {
        super(selectorProvider);
    }

    /**
     * Create a open and not-connected datagram channel.
     * <p>
     * This channel is got by <code>openDatagramChannel</code> method of the
     * default <code>SelectorProvider </code> instance.
     * </p>
     * 
     * @return The new created channel which is open but not-connected.
     * @throws IOException
     *             If some IO problem occurs.
     */
    public static DatagramChannel open() throws IOException {
        return SelectorProvider.provider().openDatagramChannel();
    }

    /**
     * Get the valid operations of this channel. Datagram channels support read
     * and write operation, so this method returns (
     * <code>SelectionKey.OP_READ</code> | <code>SelectionKey.OP_WRITE</code> ).
     * 
     * @see java.nio.channels.SelectableChannel#validOps()
     * @return Valid operations in bit-set.
     */
    public final int validOps() {
        return (SelectionKey.OP_READ | SelectionKey.OP_WRITE);
    }

    /**
     * Return the related datagram socket of this channel, which won't declare
     * public methods that not declared in <code>DatagramSocket</code>.
     * 
     * @return The related DatagramSocket instance.
     */
    public abstract DatagramSocket socket();

    /**
     * Answer whether this channel's socket is connected or not.
     * 
     * @return <code>true</code> for this channel's socket is connected;
     *         <code>false</code> otherwise.
     */
    public abstract boolean isConnected();

    /**
     * Connect the socket of this channel to a remote address, which is the only
     * communication peer of getting and sending datagrams after connected.
     * <p>
     * This method can be called at any moment, and won't affect the processing
     * read and write operation. The connect status won't changed before
     * disconnected and closed.
     * </p>
     * <p>
     * This method just execute the same security checks as the connect method
     * of the <code>DatagramSocket</code> class.
     * </p>
     * 
     * @param address
     *            The address to be connected.
     * @return This channel.
     * @throws ClosedChannelException
     *             If the channel is already closed.
     * @throws AsynchronousCloseException
     *             If the channel is closed by another thread while this method
     *             is in operation.
     * @throws ClosedByInterruptException
     *             If another thread interrupts the calling thread while the
     *             operation is in progress. The calling thread will have the
     *             interrupt state set, and the channel will be closed.
     * @throws SecurityException
     *             If there is a security manager, and the address is not
     *             permitted to access.
     * @throws IOException
     *             Some other IO error occurred.
     * 
     */
    public abstract DatagramChannel connect(SocketAddress address)
            throws IOException;

    /**
     * Disconnect the socket of this channel, which is connected before for
     * getting and sending datagrams.
     * <p>
     * This method can be called at any moment, and won't affect the processing
     * read and write operation. It won't has any effect if the socket is not
     * connected or the channel is closed.
     * </p>
     * 
     * @return This channel.
     * @throws IOException
     *             Some other IO error occurred.
     */
    public abstract DatagramChannel disconnect() throws IOException;

    /**
     * Get a datagram from this channel.
     * <p>
     * This method transfers the datagram from the channel into the target byte
     * buffer and return the address of the datagram, if the datagram is
     * available or will be available as this channel is in blocking mode. This
     * method returns <code>null</code> if the datagram is not available now
     * and the channel is in non-blocking mode. The transfer start at the
     * current position of the buffer, and the residual part of the datagram
     * will be ignored if there is no efficient remaining in the buffer to store
     * the datagram.
     * </p>
     * <p>
     * This method can be called at any moment, and will block if there is
     * another thread started a read operation on the channel.
     * </p>
     * <p>
     * This method just execute the same security checks as the receive method
     * of the <code>DatagramSocket</code> class.
     * </p>
     * 
     * @param target
     *            The byte buffer to store the received datagram.
     * @return Address of the datagram if the transfer is performed, or null if
     *         the channel is in non-blocking mode and the datagram are
     *         unavailable.
     * @throws ClosedChannelException
     *             If the channel is already closed.
     * @throws AsynchronousCloseException
     *             If the channel is closed by another thread while this method
     *             is in operation.
     * @throws ClosedByInterruptException
     *             If another thread interrupts the calling thread while the
     *             operation is in progress. The calling thread will have the
     *             interrupt state set, and the channel will be closed.
     * @throws SecurityException
     *             If there is a security manager, and the address is not
     *             permitted to access.
     * @throws IOException
     *             Some other IO error occurred.
     * 
     */
    public abstract SocketAddress receive(ByteBuffer target) throws IOException;

    /**
     * Sends out a datagram by the channel.
     * <p>
     * The precondition of sending is that whether the channel is in blocking
     * mode and enough byte buffer space will be available, or the channel is in
     * non-blocking mode and byte buffer space is enough. The transfer action is
     * just like a regular write operation.
     * </p>
     * <p>
     * This method can be called at any moment, and will block if there is
     * another thread started a read operation on the channel.
     * </p>
     * <p>
     * This method just execute the same security checks as the send method of
     * the <code>DatagramSocket</code> class.
     * </p>
     * 
     * @param source
     *            The byte buffer with the datagram to be sent.
     * @param address
     *            The address to be sent.
     * @return The number of sent bytes. If this method is called, it returns
     *         the number of bytes that remaining in the byte buffer. If the
     *         channel is in non-blocking mode and no enough space for the
     *         datagram in the buffer, it may returns zero.
     * @throws ClosedChannelException
     *             If the channel is already closed.
     * @throws AsynchronousCloseException
     *             If the channel is closed by another thread while this method
     *             is in operation.
     * @throws ClosedByInterruptException
     *             If another thread interrupts the calling thread while the
     *             operation is in progress. The calling thread will have the
     *             interrupt state set, and the channel will be closed.
     * @throws SecurityException
     *             If there is a security manager, and the address is not
     *             permitted to access.
     * @throws IOException
     *             Some other IO error occurred.
     * 
     */
    public abstract int send(ByteBuffer source, SocketAddress address)
            throws IOException;

    /**
     * Reads datagram from the channel into the byte buffer.
     * <p>
     * The precondition of calling this method is that the channel is connected
     * and the coming datagram is from the connected address. If the buffer is
     * not enough to store the datagram, the residual part of the datagram is
     * ignored. Otherwise, this method has the same behavior as the read method
     * in the <code>ReadableByteChannel</code> interface.
     * </p>
     * 
     * @see java.nio.channels.ReadableByteChannel#read(java.nio.ByteBuffer)
     * @param target
     *            The byte buffer to store the received datagram.
     * @return Non-negative number as the number of bytes read, or -1 as the
     *         read operation reaches the end of stream.
     * @throws NotYetConnectedException
     *             If the channel is not connected yet.
     * @throws ClosedChannelException
     *             If the channel is already closed.
     * @throws AsynchronousCloseException
     *             If the channel is closed by another thread while this method
     *             is in operation.
     * @throws ClosedByInterruptException
     *             If another thread interrupts the calling thread while the
     *             operation is in progress. The calling thread will have the
     *             interrupt state set, and the channel will be closed.
     * @throws IOException
     *             Some other IO error occurred.
     * 
     */
    public abstract int read(ByteBuffer target) throws IOException;

    /**
     * Reads datagram from the channel into the byte buffer.
     * <p>
     * The precondition of calling this method is that the channel is connected
     * and the coming datagram is from the connected address. If the buffer is
     * not enough to store the datagram, the residual part of the datagram is
     * ignored. Otherwise, this method has the same behavior as the read method
     * in the <code>ScatteringByteChannel</code> interface.
     * </p>
     * 
     * @see java.nio.channels.ScatteringByteChannel#read(java.nio.ByteBuffer[],
     *      int, int)
     * @param targets
     *            The byte buffers to store the received datagram.
     * @param offset
     *            A non-negative offset in the array of buffer, pointing to the
     *            starting buffer to store the byte transferred, must no larger
     *            than targets.length.
     * @param length
     *            A non-negative length to indicate the maximum number of byte
     *            to be read, must no larger than targets.length - offset.
     * @return Non-negative number as the number of bytes read, or -1 as the
     *         read operation reaches the end of stream.
     * @throws NotYetConnectedException
     *             If the channel is not connected yet.
     * @throws ClosedChannelException
     *             If the channel is already closed.
     * @throws AsynchronousCloseException
     *             If the channel is closed by another thread while this method
     *             is in operation.
     * @throws ClosedByInterruptException
     *             If another thread interrupts the calling thread while the
     *             operation is in progress. The calling thread will have the
     *             interrupt state set, and the channel will be closed.
     * @throws IOException
     *             Some other IO error occurred.
     */
    public abstract long read(ByteBuffer[] targets, int offset, int length)
            throws IOException;

    /**
     * Reads datagram from the channel into the byte buffer.
     * <p>
     * The precondition of calling this method is that the channel is connected
     * and the coming datagram is from the connected address. If the buffer is
     * not enough to store the datagram, the residual part of the datagram is
     * ignored. Otherwise, this method has the same behavior as the read method
     * in the <code>ScatteringByteChannel</code> interface.
     * </p>
     * 
     * @see java.nio.channels.ScatteringByteChannel#read(java.nio.ByteBuffer[])
     * @param targets
     *            The byte buffers to store the received datagram.
     * @return Non-negative number as the number of bytes read, or -1 as the
     *         read operation reaches the end of stream.
     * @throws NotYetConnectedException
     *             If the channel is not connected yet.
     * @throws ClosedChannelException
     *             If the channel is already closed.
     * @throws AsynchronousCloseException
     *             If the channel is closed by another thread while this method
     *             is in operation.
     * @throws ClosedByInterruptException
     *             If another thread interrupts the calling thread while the
     *             operation is in progress. The calling thread will have the
     *             interrupt state set, and the channel will be closed.
     * @throws IOException
     *             Some other IO error occurred.
     */
    public synchronized final long read(ByteBuffer[] targets)
            throws IOException {
        return read(targets, 0, targets.length);
    }

    /**
     * Write datagram from the byte buffer into the channel.
     * <p>
     * The precondition of calling this method is that the channel is connected
     * and the datagram is sent to the connected address. Otherwise, this method
     * has the same behavior as the write method in the
     * <code>WritableByteChannel</code> interface.
     * </p>
     * 
     * @see java.nio.channels.WritableByteChannel#write(java.nio.ByteBuffer)
     * @param source
     *            The byte buffer as the source of the datagram.
     * @return Non-negative number of bytes written.
     * @throws NotYetConnectedException
     *             If the channel is not connected yet.
     * @throws ClosedChannelException
     *             If the channel is already closed.
     * @throws AsynchronousCloseException
     *             If the channel is closed by another thread while this method
     *             is in operation.
     * @throws ClosedByInterruptException
     *             If another thread interrupts the calling thread while the
     *             operation is in progress. The calling thread will have the
     *             interrupt state set, and the channel will be closed.
     * @throws IOException
     *             Some other IO error occurred.
     * 
     */
    public abstract int write(ByteBuffer source) throws IOException;

    /**
     * Write datagram from the byte buffer into the channel.
     * <p>
     * The precondition of calling this method is that the channel is connected
     * and the datagram is sent to the connected address. Otherwise, this method
     * has the same behavior as the write method in the
     * <code>GatheringByteChannel</code> interface.
     * 
     * @see java.nio.channels.GatheringByteChannel#write(java.nio.ByteBuffer[],
     *      int, int)
     * @param sources
     *            The byte buffers as the source of the datagram.
     * @param offset
     *            A non-negative offset in the array of buffer, pointing to the
     *            starting buffer to be retrieved, must no larger than
     *            sources.length.
     * @param length
     *            A non-negative length to indicate the maximum number of byte
     *            to be written, must no larger than sources.length - offset.
     * @return The number of written bytes. If this method is called, it returns
     *         the number of bytes that remaining in the byte buffer. If the
     *         channel is in non-blocking mode and no enough space for the
     *         datagram in the buffer, it may returns zero.
     * @throws NotYetConnectedException
     *             If the channel is not connected yet.
     * @throws ClosedChannelException
     *             If the channel is already closed.
     * @throws AsynchronousCloseException
     *             If the channel is closed by another thread while this method
     *             is in operation.
     * @throws ClosedByInterruptException
     *             If another thread interrupts the calling thread while the
     *             operation is in progress. The calling thread will have the
     *             interrupt state set, and the channel will be closed.
     * @throws IOException
     *             Some other IO error occurred.
     * 
     */
    public abstract long write(ByteBuffer[] sources, int offset, int length)
            throws IOException;

    /**
     * Write datagram from the byte buffer into the channel.
     * <p>
     * The precondition of calling this method is that the channel is connected
     * and the datagram is sent to the connected address. Otherwise, this method
     * has the same behavior as the write method in the
     * <code>GatheringByteChannel</code> interface.
     * 
     * @see java.nio.channels.GatheringByteChannel#write(java.nio.ByteBuffer[])
     * @param sources
     *            The byte buffers as the source of the datagram.
     * @return The number of written bytes. If this method is called, it returns
     *         the number of bytes that remaining in the byte buffer. If the
     *         channel is in non-blocking mode and no enough space for the
     *         datagram in the buffer, it may returns zero.
     * @throws NotYetConnectedException
     *             If the channel is not connected yet.
     * @throws ClosedChannelException
     *             If the channel is already closed.
     * @throws AsynchronousCloseException
     *             If the channel is closed by another thread while this method
     *             is in operation.
     * @throws ClosedByInterruptException
     *             If another thread interrupts the calling thread while the
     *             operation is in progress. The calling thread will have the
     *             interrupt state set, and the channel will be closed.
     * @throws IOException
     *             Some other IO error occurred.
     * 
     */
    public synchronized final long write(ByteBuffer[] sources)
            throws IOException {
        return write(sources, 0, sources.length);
    }
}
