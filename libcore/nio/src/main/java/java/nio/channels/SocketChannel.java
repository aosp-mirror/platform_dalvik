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
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.spi.AbstractSelectableChannel;
import java.nio.channels.spi.SelectorProvider;

import org.apache.harmony.luni.platform.Platform;

/**
 * A SocketChannel is a selectable channel for part abstraction of stream
 * connecting socket. The <code>socket</code> method of this class can return
 * the related <code>Socket</code> instance, which can handle the socket.
 * <p>
 * A socket channel is open but not connected when created by <code>open</code>
 * method. After connected by calling the <code>connect</code> method, it will
 * keep connected before closed. The connection is non-blocking that the
 * <code>connect</code> method is for the initial connection and following
 * <code>finishConnect</code> method is for the final steps of connection. The
 * <code>isConnectionPending</code> method can tell the connection is blocked
 * or not; the <code>isConnected</code> method can tell the socket is
 * connected finally or not.
 * </p>
 * <p>
 * The shut down operation can be independent and asynchronous for input and
 * output. The <code>shutdownInput</code> method is for input, and can make
 * the following read operation fail as end of stream. If the input is shut down
 * and another thread is pending in read operation, the read will end without
 * effect and return end of stream. The <code>shutdownOutput</code> method is
 * for output, and can make the following write operation throwing a
 * <code>ClosedChannelException</code>. If the output is shut down and
 * another is pending in a write operation, an
 * <code>AsynchronousCloseException</code> will thrown to the pending thread.
 * </p>
 * <p>
 * Socket channels are thread-safe, no more than one thread can read or write at
 * given time. The <code>connect</code> and <code>finishConnect</code>
 * methods are concurrent each other, when they are processing, other read and
 * write will block.
 * </p>
 */
public abstract class SocketChannel extends AbstractSelectableChannel implements
        ByteChannel, ScatteringByteChannel, GatheringByteChannel {

    static {
        Platform.getNetworkSystem().oneTimeInitialization(true);
    }
    
    /**
     * Constructor for this class.
     * 
     * @param selectorProvider
     *            A instance of SelectorProvider
     */
    protected SocketChannel(SelectorProvider selectorProvider) {
        super(selectorProvider);
    }

    /**
     * Create a open and not-connected socket channel.
     * <p>
     * This channel is got by <code>openSocketChannel</code> method of the
     * default <code>SelectorProvider </code> instance.
     * </p>
     * 
     * @return The new created channel which is open but not-connected.
     * @throws IOException
     *             If some IO problem occurs.
     */
    public static SocketChannel open() throws IOException {
        return SelectorProvider.provider().openSocketChannel();
    }

    /**
     * Create a socket channel and connect it to a socket address.
     * <p>
     * This method perform just as <code>open</code> method following by the
     * <code>connect</code> method.
     * </p>
     * 
     * @param address
     *            The socket address to be connected.
     * @return The new opened channel.
     * @throws AsynchronousCloseException
     *             If the channel is closed by another thread while this method
     *             is in operation.
     * @throws ClosedByInterruptException
     *             If another thread interrupts the calling thread while the
     *             operation is in progress. The calling thread will have the
     *             interrupt state set, and the channel will be closed.
     * @throws UnresolvedAddressException
     *             If the address is not resolved.
     * @throws UnsupportedAddressTypeException
     *             If the address type is not supported.
     * @throws SecurityException
     *             If there is a security manager, and the address is not
     *             permitted to access.
     * @throws IOException
     *             Some other IO error occurred.
     * 
     */
    public static SocketChannel open(SocketAddress address) throws IOException {
        SocketChannel socketChannel = open();
        if (null != socketChannel) {
            socketChannel.connect(address);
        }
        return socketChannel;
    }

    /**
     * Get the valid operations of this channel. Socket channels support
     * connect, read and write operation, so this method returns (
     * <code>SelectionKey.OP_CONNECT</code> |
     * <code>SelectionKey.OP_READ</code> | <code>SelectionKey.OP_WRITE</code> ).
     * 
     * @see java.nio.channels.SelectableChannel#validOps()
     * @return Valid operations in bit-set.
     */
    public final int validOps() {
        return (SelectionKey.OP_CONNECT | SelectionKey.OP_READ | SelectionKey.OP_WRITE);
    }

    /**
     * Return the related socket of this channel, which won't declare public
     * methods that not declared in <code>Socket</code>.
     * 
     * @return The related Socket instance.
     */
    public abstract Socket socket();

    /**
     * Answer whether this channel's socket is connected or not.
     * 
     * @return <code>true</code> for this channel's socket is connected;
     *         <code>false</code> otherwise.
     */
    public abstract boolean isConnected();

    /**
     * Answer whether this channel's socket is in connecting or not.
     * 
     * @return <code>true</code> for the connection is initiated but not
     *         finished; <code>false</code> otherwise.
     */
    public abstract boolean isConnectionPending();

    /**
     * Connect the socket to remote address.
     * <p>
     * If the channel is blocking, this method will suspend before connection
     * finished or an I/O exception. If the channel is non-blocking, this method
     * will return <code>true</code> if the connection is finished at once or
     * return <code>false</code> and the connection must wait
     * <code>finishConnect</code> to finished otherwise.
     * </p>
     * <p>
     * This method can be called at any moment, and can block other read and
     * write operations while connecting.
     * </p>
     * <p>
     * This method just execute the same security checks as the connect method
     * of the <code>Socket</code> class.
     * </p>
     * 
     * @param address
     *            The address to be connected.
     * @return <code>true</code> if connection is finished,<code>false</code>
     *         otherwise.
     * @throws AlreadyConnectedException
     *             If the channel is connected already.
     * @throws ConnectionPendingException
     *             A non-blocking connecting is doing on this channel.
     * @throws ClosedChannelException
     *             If the channel is already closed.
     * @throws AsynchronousCloseException
     *             If the channel is closed by another thread while this method
     *             is in operation.
     * @throws ClosedByInterruptException
     *             If another thread interrupts the calling thread while the
     *             operation is in progress. The calling thread will have the
     *             interrupt state set, and the channel will be closed.
     * @throws UnresolvedAddressException
     *             If the address is not resolved.
     * @throws UnsupportedAddressTypeException
     *             If the address type is not supported.
     * @throws SecurityException
     *             If there is a security manager, and the address is not
     *             permitted to access.
     * @throws IOException
     *             Some other IO error occurred.
     * 
     */
    public abstract boolean connect(SocketAddress address) throws IOException;

    /**
     * Complete the connection.
     * <p>
     * This method is used when the channel is connectable to finish the
     * connection, and the connectable status of a channel means the channel is
     * after initiating in non-blocking mode and calling its
     * <code>connect</code> method. It will throw related
     * <code>IOException</code> if the connection failed.
     * </p>
     * <p>
     * This method will return <code>true</code> if the connection is finished
     * already, and return <code>false</code> if the channel is non-blocking
     * and the connection is not finished yet.
     * </p>
     * <p>
     * If the channel is in blocking mode, this method will suspend, and return
     * <code>true</code> for connection finished or throw some exception
     * otherwise. The channel will be closed if the connection failed and this
     * method thrown some exception.
     * </p>
     * <p>
     * This method can be called at any moment, and can block other read and
     * write operations while connecting.
     * </p>
     * 
     * @return <code>true</code> if the connection is successfully finished,
     *         <code>false</code> otherwise.
     * @throws NoConnectionPendingException
     *             If the channel is not connected and the connection is not
     *             initiated.
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
    public abstract boolean finishConnect() throws IOException;

    /**
     * Reads bytes from the channel into the given buffer.
     * <p>
     * The maximum number of bytes that will be read is the
     * <code>remaining()</code> number of bytes in the buffer when the method
     * invoked. The bytes will be read into the buffer starting at the buffer's
     * <code>position</code>.
     * </p>
     * <p>
     * The call may block if other threads are also attempting to read on the
     * same channel.
     * </p>
     * <p>
     * Upon completion, the buffer's <code>position()</code> is updated to the
     * end of the bytes that were read. The buffer's <code>limit()</code> is
     * unmodified.
     * </p>
     * 
     * @see java.nio.channels.ReadableByteChannel#read(java.nio.ByteBuffer)
     * @param target
     *            The byte buffer to receive the bytes.
     * @return The number of bytes actually read.
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
     * Reads bytes from the channel into a subset of the given buffers.
     * <p>
     * This method attempts to read all of the <code>remaining()</code> bytes
     * from <code>length</code> byte buffers, in order, starting at
     * <code>targets[offset]</code>. The number of bytes actually read is
     * returned.
     * </p>
     * <p>
     * If a read operation is in progress, subsequent threads will block until
     * the read is completed, and will then contend for the ability to read.
     * </p>
     * 
     * @see java.nio.channels.ScatteringByteChannel#read(java.nio.ByteBuffer[],
     *      int, int)
     * @param targets
     *            the array of byte buffers into which the bytes will be read.
     * @param offset
     *            the index of the first buffer to read.
     * @param length
     *            the maximum number of buffers to read.
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
    public abstract long read(ByteBuffer[] targets, int offset, int length)
            throws IOException;

    /**
     * Reads bytes from the channel into all the given buffers.
     * <p>
     * This method is equivalent to:
     * 
     * <pre>
     * read(targets, 0, targets.length);
     * </pre>
     * 
     * @see java.nio.channels.ScatteringByteChannel#read(java.nio.ByteBuffer[])
     * @param targets
     *            the array of byte buffers to receive the bytes being read.
     * @return the number of bytes actually read.
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
    public synchronized final long read(ByteBuffer[] targets)
            throws IOException {
        return read(targets, 0, targets.length);
    }

    /**
     * Writes bytes from the given buffer to the channel.
     * <p>
     * The maximum number of bytes that will be written is the
     * <code>remaining()</code> number of bytes in the buffer when the method
     * invoked. The bytes will be written from the buffer starting at the
     * buffer's <code>position</code>.
     * </p>
     * <p>
     * The call may block if other threads are also attempting to write on the
     * same channel.
     * </p>
     * <p>
     * Upon completion, the buffer's <code>position()</code> is updated to the
     * end of the bytes that were written. The buffer's <code>limit()</code>
     * is unmodified.
     * </p>
     * 
     * @see java.nio.channels.WritableByteChannel#write(java.nio.ByteBuffer)
     * @param source
     *            the byte buffer containing the bytes to be written.
     * @return the number of bytes actually written.
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
     * Writes a subset of the given bytes from the buffers to the channel.
     * <p>
     * This method attempts to write all of the <code>remaining()</code> bytes
     * from <code>length</code> byte buffers, in order, starting at
     * <code>sources[offset]</code>. The number of bytes actually written is
     * returned.
     * </p>
     * <p>
     * If a write operation is in progress, subsequent threads will block until
     * the write is completed, and will then contend for the ability to write.
     * </p>
     * 
     * @see java.nio.channels.GatheringByteChannel#write(java.nio.ByteBuffer[],
     *      int, int)
     * @param sources
     *            the array of byte buffers containing the source of remaining
     *            bytes that will be attempted to be written.
     * @param offset
     *            the index of the first buffer to write.
     * @param length
     *            the number of buffers to write.
     * @return the number of bytes actually written.
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
     * @see java.nio.channels.GatheringByteChannel#write(java.nio.ByteBuffer[])
     * @param sources
     *            the buffers containing bytes to be written.
     * @return the number of bytes actually written.
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
