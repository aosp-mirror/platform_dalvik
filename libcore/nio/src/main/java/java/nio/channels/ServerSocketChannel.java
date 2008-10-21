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
import java.net.ServerSocket;
import java.nio.channels.spi.AbstractSelectableChannel;
import java.nio.channels.spi.SelectorProvider;

/**
 * A ServerSocketChannel is a partly abstracted stream-oriented listening socket
 * which is selectable. Binding and manipulation of socket options can only be done
 * through the associated <code>ServerSocket</code> object, returned by calling 
 * socket method. ServerSocketChannels can not be constructed for a pre-existing 
 * server socket, nor can it be assigned a SocketImpl.
 * <p> 
 * A Server-Socket channel is open but not bound when created by
 * <code>open</code> method. (Calling <code>accept</code> before bound will cause a
 * <code>NotYetBoundException</code>). It can be bound by calling the bind method 
 * of a related <code>ServerSocket</code> instance.</p>  
 */
public abstract class ServerSocketChannel extends AbstractSelectableChannel {

    /**
     * Construct a new instance for ServerSocketChannel
     * @param selectorProvider
     *            An instance of SelectorProvider
     */

    protected ServerSocketChannel(SelectorProvider selectorProvider) {
        super(selectorProvider);
    }

    /**
     * Create an open and unbound server-socket channel.
     * <p>
     * This channel is got by calling <code>openServerSocketChannel</code>
     * method of the default <code>SelectorProvider </code> instance.
     * </p> 
     * 
     * @return The new created channel which is open but unbound.
     * @throws IOException
     *             If some IO problem occurs.
     */
    public static ServerSocketChannel open() throws IOException {
        return SelectorProvider.provider().openServerSocketChannel();
    }

    /**
     * Get the valid operations of this channel. Server-socket channels support
     * accepting operation.Currently the only supported operation is OP_ACCEPT.
     * It always returns <code>SelectionKey.OP_ACCEPT</code>.
     * 
     * @see java.nio.channels.SelectableChannel#validOps()
     * @return Valid operations in bit-set.
     */
    public final int validOps() {
        return SelectionKey.OP_ACCEPT;
    }

    /**
     * Return the related server-socket of this channel. 
     * All public methods declared in returned object should be declared in <code>ServerSocket</code>.
     * 
     * @return The related ServerSocket instance.
     */
    public abstract ServerSocket socket();

    /**
     * Accepts a connection to this socket.
     * <p>
     * It returns null when the channel is non-blocking and no connections available, otherwise it
     * blocks indefinitely until a new connection is available or an I/O error occurs.
     * The returned channel will be in blocking mode any way. 
     * </p>
     * 
     * <p>
     * This method just execute the same security checks as the accept method of
     * the <code>ServerSocket</code> class.
     * </p>
     * 
     * @return The accepted SocketChannel instance, or null as the channel is
     *         non-blocking and no connections available.
     * @throws ClosedChannelException
     *             If the channel is already closed.
     * @throws AsynchronousCloseException
     *             If the channel is closed by another thread while this method
     *             is in operation.
     * @throws ClosedByInterruptException
     *             If another thread interrupts the calling thread while the
     *             operation is in progress. The calling thread will have the
     *             interrupt state set, and the channel will be closed.
     * @throws NotYetBoundException
     *             If the socket has not yet been bound.
     * @throws SecurityException
     *             If there is a security manager, and the new connection is not
     *             permitted to access.
     * @throws IOException
     *             Some other IO error occurred.
     * 
     */
    public abstract SocketChannel accept() throws IOException;

}
