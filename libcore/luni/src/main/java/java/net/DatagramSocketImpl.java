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

package java.net;

import java.io.FileDescriptor;
import java.io.IOException;

import org.apache.harmony.luni.net.NetUtil;
import org.apache.harmony.luni.platform.Platform;

/**
 * The abstract superclass of datagram & multicast socket implementations.
 */
public abstract class DatagramSocketImpl implements SocketOptions {

    protected FileDescriptor fd;

    protected int localPort;

    /**
     * Constructs an unbound datagram socket implementation.
     */
    public DatagramSocketImpl() {
        localPort = -1;
    }

    /**
     * Bind the datagram socket to the nominated localhost/port. Sockets must be
     * bound prior to attempting to send or receive data.
     * 
     * @param port
     *            the port on the localhost to bind
     * @param addr
     *            the address on the multihomed localhost to bind
     * 
     * @exception SocketException
     *                if an error occurred during bind, such as if the port was
     *                already bound
     */
    protected abstract void bind(int port, InetAddress addr)
            throws SocketException;

    /**
     * Close the socket.
     */
    protected abstract void close();

    /**
     * This method allocates the socket descriptor in the underlying operating
     * system.
     */
    protected abstract void create() throws SocketException;

    /**
     * Answer the FileDescriptor, which will be invalid if the socket is closed
     * or not bound.
     * 
     * @return FileDescriptor the socket file descriptor
     */
    protected FileDescriptor getFileDescriptor() {
        return fd;
    }

    /**
     * Answer the local address to which the socket is bound.
     * 
     * @return InetAddress the local address to which the socket is bound.
     */
    InetAddress getLocalAddress() {
        return Platform.getNetworkSystem().getSocketLocalAddress(fd,
                NetUtil.preferIPv6Addresses());
    }

    /**
     * Answer the local port. If the socket was bound to any available port, as
     * flagged by a <code>localPort</code> value of -1, query the IP stack.
     * 
     * @return int the local port to which the socket is bound.
     */
    protected int getLocalPort() {
        return localPort;
    }

    /**
     * Answer the nominated socket option.
     * 
     * @param optID
     *            the socket option to retrieve
     * @return Object the option value
     * @exception SocketException
     *                thrown if an error occurs while accessing the option
     */
    public abstract Object getOption(int optID) throws SocketException;

    /**
     * Answer the time-to-live (TTL) for multicast packets sent on this socket.
     * 
     * @return java.net.InetAddress
     * @throws IOException
     *             The exception description.
     * @deprecated Replaced by {@link #getTimeToLive}
     * @see #getTimeToLive()
     */
    @Deprecated
    protected abstract byte getTTL() throws IOException;

    /**
     * Answer the time-to-live (TTL) for multicast packets sent on this socket.
     * 
     * @return int
     * @throws IOException
     *             The exception description.
     */
    protected abstract int getTimeToLive() throws IOException;

    /**
     * Add this socket to the multicast group. A socket must join a group before
     * data may be received. A socket may be a member of multiple groups but may
     * join any group once.
     * 
     * @param addr
     *            the multicast group to be joined
     * @throws IOException may be thrown while joining a group
     */
    protected abstract void join(InetAddress addr) throws IOException;

    /**
     * Add this socket to the multicast group. A socket must join a group before
     * data may be received. A socket may be a member of multiple groups but may
     * join any group once.
     * 
     * @param addr
     *            the multicast group to be joined
     * @param netInterface
     *            the network interface on which the addresses should be dropped
     * @throws IOException may be thrown while joining a group
     */
    protected abstract void joinGroup(SocketAddress addr,
            NetworkInterface netInterface) throws IOException;

    /**
     * Remove the socket from the multicast group.
     * 
     * @param addr
     *            the multicast group to be left
     * @throws IOException May be thrown while leaving the group
     */
    protected abstract void leave(InetAddress addr) throws IOException;

    /**
     * Remove the socket from the multicast group.
     * 
     * @param addr
     *            the multicast group to be left
     * @param netInterface
     *            the network interface on which the addresses should be dropped
     * @throws IOException May be thrown while leaving the group
     */
    protected abstract void leaveGroup(SocketAddress addr,
            NetworkInterface netInterface) throws IOException;

    /**
     * Peek at the incoming packet to this socket and answer the sender's
     * address into <code>sender</code>. The method will block until a packet
     * is received or timeout expires and returns the sender's port.
     * 
     * @exception IOException
     *                if a read error or timeout occurs
     */
    protected abstract int peek(InetAddress sender) throws IOException;

    /**
     * Receive data into the supplied datagram packet. This call will block
     * until either data is received or, if a timeout is set, the timeout
     * expires. If the timeout expires, the InterruptedIOException is thrown.
     * 
     * @exception IOException
     *                if a read error or timeout occurs
     */
    protected abstract void receive(DatagramPacket pack) throws IOException;

    /**
     * Sends the supplied datagram packet. The packet contains the destination
     * host & port.
     * 
     * @param pack
     *            DatagramPacket to send
     * 
     * @exception IOException
     *                if a write error occurs
     */
    protected abstract void send(DatagramPacket pack) throws IOException;

    /**
     * Set the nominated socket option.
     * 
     * @param optID
     *            the socket option to set
     * @param val
     *            the option value
     * @exception SocketException
     *                thrown if an error occurs while setting the option
     */
    public abstract void setOption(int optID, Object val)
            throws SocketException;

    /**
     * Set the time-to-live (TTL) for multicast packets sent on this socket.
     * 
     * @param ttl
     *            the time-to-live, 0 &lt; ttl &lt;= 255
     * @throws IOException The exception thrown while setting the TTL
     */
    protected abstract void setTimeToLive(int ttl) throws IOException;

    /**
     * Set the time-to-live (TTL) for multicast packets sent on this socket.
     * 
     * @param ttl
     *            the time-to-live, 0 &lt; ttl &lt;= 255
     * @throws IOException The exception thrown while setting the TTL
     * @deprecated Replaced by {@link #setTimeToLive}
     * @see #setTimeToLive(int)
     */
    @Deprecated
    protected abstract void setTTL(byte ttl) throws IOException;

    /**
     * Connect the socket to the specified remote address and port.
     * 
     * @param inetAddr
     *            the remote address
     * @param port
     *            the remote port
     * 
     * @exception SocketException
     *                possibly thrown, if the datagram socket cannot be
     *                connected to the specified remote address and port
     */
    protected void connect(InetAddress inetAddr, int port)
            throws SocketException {
        // do nothing
    }

    /**
     * Disconnect the socket from the remote address and port.
     */
    protected void disconnect() {
        // do nothing
    }

    /**
     * Receive data into the supplied datagram packet by peeking. The data is
     * not removed and will be received by another peekData() or receive() call.
     * 
     * This call will block until either data is received or, if a timeout is
     * set, the timeout expires.
     * 
     * @param pack
     *            the DatagramPacket used to store the data
     * 
     * @return the port the packet was received from
     * 
     * @exception IOException
     *                if an error occurs
     */
    protected abstract int peekData(DatagramPacket pack) throws IOException;
}
