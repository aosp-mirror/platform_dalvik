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

import java.io.IOException;
import java.nio.channels.DatagramChannel;

//import android.net.SocketImplProvider;
import org.apache.harmony.luni.net.SocketImplProvider;
import org.apache.harmony.luni.platform.Platform;

import org.apache.harmony.luni.util.Msg;

/**
 * This class models a socket for sending & receiving datagram packets.
 * 
 * @see DatagramPacket
 */
public class DatagramSocket {

    DatagramSocketImpl impl;

    InetAddress address;

    int port = -1;

    static DatagramSocketImplFactory factory;

    boolean isBound = false;

    private boolean isConnected = false;

    private boolean isClosed = false;

    private static class Lock {
    }

    static {
        Platform.getNetworkSystem().oneTimeInitialization(true);
    }
    
    private Object lock = new Lock();

    /**
     * Constructs a datagram socket, bound to any available port on the
     * localhost.
     * 
     * @throws SocketException
     *             if a problem occurs creating or binding the socket
     */
    public DatagramSocket() throws SocketException {
        this(0);
    }

    /**
     * Returns a datagram socket, bound to the nominated port on the localhost.
     * 
     * @param aPort
     *            the port to bind on the localhost
     * 
     * @throws SocketException
     *             if a problem occurs creating or binding the socket
     */
    public DatagramSocket(int aPort) throws SocketException {
        super();
        checkListen(aPort);
        createSocket(aPort, InetAddress.ANY);
    }

    /**
     * Constructs a datagram socket, bound to the nominated localhost/port.
     * 
     * @param aPort
     *            the port on the localhost to bind
     * @param addr
     *            the address on the multihomed localhost to bind
     * 
     * @throws SocketException
     *             if a problem occurs creating or binding the socket
     */
    public DatagramSocket(int aPort, InetAddress addr) throws SocketException {
        super();
        checkListen(aPort);
        createSocket(aPort, null == addr ? InetAddress.ANY : addr);
    }

    /**
     * Sent prior to attempting to bind the socket, check that the port is
     * within the valid port range and verify with the security manager that the
     * port may be bound by the current context.
     * 
     * @param aPort
     *            the port on the localhost that is to be bound
     */
    void checkListen(int aPort) {
        if (aPort < 0 || aPort > 65535) {
            throw new IllegalArgumentException(Msg.getString("K0325", aPort)); //$NON-NLS-1$
        }
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkListen(aPort);
        }
    }

    /**
     * Close the socket.
     */
    // In the documentation jdk1.1.7a/guide/net/miscNet.html, this method is
    // noted as not being synchronized.
    public void close() {
        isClosed = true;
        impl.close();
    }

    /**
     * Connect the datagram socket to a remote host and port. The host and port
     * are validated, thereafter the only validation on send() and receive() is
     * that the packet address/port matches the connected target.
     * 
     * @param anAddress
     *            the target address
     * @param aPort
     *            the target port
     */
    public void connect(InetAddress anAddress, int aPort) {
        if (anAddress == null || aPort < 0 || aPort > 65535) {
            throw new IllegalArgumentException(Msg.getString("K0032")); //$NON-NLS-1$
        }

        synchronized (lock) {
            if (isClosed()) {
                return;
            }
            try {
                checkClosedAndBind(true);
            } catch (SocketException e) {
                // Ignored
            }

            SecurityManager security = System.getSecurityManager();
            if (security != null) {
                if (anAddress.isMulticastAddress()) {
                    security.checkMulticast(anAddress);
                } else {
                    security.checkConnect(anAddress.getHostName(), aPort);
                }
            }

            try {
                impl.connect(anAddress, aPort);
            } catch (SocketException e) {
                // not connected at the native level just do what we did before
            }
            address = anAddress;
            port = aPort;
            isConnected = true;
        }
    }

    /**
     * 'Disconnect' the datagram socket from a remote host and port. This method
     * may be called on an unconnected socket.
     */
    public void disconnect() {
        if (isClosed() || !isConnected()) {
            return;
        }
        impl.disconnect();
        address = null;
        port = -1;
        isConnected = false;
    }

    synchronized void createSocket(int aPort, InetAddress addr)
            throws SocketException {
        impl = factory != null ? factory.createDatagramSocketImpl()
                : SocketImplProvider.getDatagramSocketImpl();
        impl.create();
        try {
            impl.bind(aPort, addr);
            isBound = true;
        } catch (SocketException e) {
            close();
            throw e;
        }
    }

    /**
     * Returns an {@link InetAddress} instance representing the address this
     * socket has connected to.
     * 
     * @return if this socket is connected, the address it is connected to. A
     *         <code>null</code> return signifies no connection has been made.
     */
    public InetAddress getInetAddress() {
        return address;
    }

    /**
     * Returns an {@link InetAddress} instance representing the <i>local</i>
     * address this socket is bound to.
     * 
     * @return the local address to which the socket is bound
     */
    public InetAddress getLocalAddress() {
        if (isClosed()) {
            return null;
        }
        if (!isBound()) {
            return InetAddress.ANY;
        }
        InetAddress anAddr = impl.getLocalAddress();
        try {
            SecurityManager security = System.getSecurityManager();
            if (security != null) {
                security.checkConnect(anAddr.getHostName(), -1);
            }
        } catch (SecurityException e) {
            return InetAddress.ANY;
        }
        return anAddr;
    }

    /**
     * Answer the local port to which the socket is bound.
     * 
     * @return int local port to which the socket is bound
     */
    public int getLocalPort() {
        if (isClosed()) {
            return -1;
        }
        if (!isBound()) {
            return 0;
        }
        return impl.getLocalPort();
    }

    /**
     * Returns the number of the remote port this socket is connected to.
     * 
     * @return int the remote port number that this socket has connected to. A
     *         return of <code>-1</code> indicates that there is no connection
     *         in place.
     */
    public int getPort() {
        return port;
    }

    /**
     * Returns whether this socket is multicast.
     * 
     * @return Always returns false.
     */
    boolean isMulticastSocket() {
        return false;
    }

    /**
     * Answer the socket receive buffer size (SO_RCVBUF).
     * 
     * @return int socket receive buffer size
     * 
     * @exception SocketException
     *                when an error occurs
     */
    public synchronized int getReceiveBufferSize() throws SocketException {
        checkClosedAndBind(false);
        return ((Integer) impl.getOption(SocketOptions.SO_RCVBUF)).intValue();
    }

    /**
     * Answer the socket send buffer size (SO_SNDBUF).
     * 
     * @return int socket send buffer size
     * 
     * @exception SocketException
     *                when an error occurs
     */
    public synchronized int getSendBufferSize() throws SocketException {
        checkClosedAndBind(false);
        return ((Integer) impl.getOption(SocketOptions.SO_SNDBUF)).intValue();
    }

    /**
     * Answer the socket receive timeout (SO_RCVTIMEOUT), in milliseconds. Zero
     * implies the timeout is disabled.
     * 
     * @return int socket receive timeout
     * 
     * @exception SocketException
     *                when an error occurs
     */
    public synchronized int getSoTimeout() throws SocketException {
        checkClosedAndBind(false);
        return ((Integer) impl.getOption(SocketOptions.SO_TIMEOUT)).intValue();
    }

    /**
     * Receive on this socket into the packet argument. This method blocks until
     * a packet is received or, if a timeout has been defined, the timeout
     * period expires. If this is a connected socket, the packet host/port are
     * compared to the connection host/port otherwise the security manager if
     * present is queried whether the packet host/port is acceptable. Any
     * packets from unacceptable origins will be silently discarded. The packet
     * fields are set according to the data received. If the received data is
     * longer than the packet buffer, it is truncated.
     * 
     * @param pack
     *            the DatagramPacket to receive data into
     * 
     * @exception java.io.IOException
     *                If a receive error occurs.
     */
    public synchronized void receive(DatagramPacket pack) throws IOException {
        checkClosedAndBind(true);

        boolean secure = true;

        InetAddress senderAddr = null;

        int senderPort = 0;
        DatagramPacket tempPack = new DatagramPacket(new byte[1], 1);
        boolean copy = false;

        SecurityManager security = System.getSecurityManager();
        if (address != null || security != null) { // The socket is connected
            // Check pack before peeking
            if (pack == null) {
                throw new NullPointerException();
            }
            secure = false;
            while (!secure) {
                copy = false;
                try {
                    senderPort = impl.peekData(tempPack);
                    senderAddr = tempPack.getAddress();
                } catch (SocketException e) {
                    if (e.getMessage().equals(
                            "The socket does not support the operation")) { //$NON-NLS-1$
                        tempPack = new DatagramPacket(new byte[pack.length],
                                pack.getLength());
                        impl.receive(tempPack);
                        senderAddr = tempPack.getAddress();
                        senderPort = tempPack.getPort();
                        copy = true;
                    } else {
                        throw e;
                    }
                }
                if (address == null) {
                    try {
                        security.checkAccept(senderAddr.getHostName(),
                                senderPort);
                        if (!copy) {
                            secure = true;
                        }
                        break;
                    } catch (SecurityException e) {
                        if (!copy) {
                            if (tempPack == null) {
                                tempPack = new DatagramPacket(
                                        new byte[pack.length], pack.length);
                            }
                            impl.receive(tempPack);
                        }
                    }
                } else if (port == senderPort && address.equals(senderAddr)) {
                    if (!copy) {
                        secure = true;
                    }
                    break;
                } else if (!copy) {
                    if (tempPack == null) {
                        tempPack = new DatagramPacket(new byte[pack.length],
                                pack.length);
                    }
                    impl.receive(tempPack);
                }
            }
        }
        if (copy) {
            System.arraycopy(tempPack.getData(), 0, pack.getData(), pack
                    .getOffset(), tempPack.getLength());
            pack.setLength(tempPack.getLength());
            pack.setAddress(tempPack.getAddress());
            pack.setPort(tempPack.getPort());
        }
        if (secure) {
            impl.receive(pack);
        }
    }

    /**
     * Send the packet on this socket. The packet must satisfy the security
     * policy before it may be sent.
     * 
     * @param pack
     *            the DatagramPacket to send
     * 
     * @exception java.io.IOException
     *                If a send error occurs.
     */
    public void send(DatagramPacket pack) throws IOException {
        checkClosedAndBind(true);

        InetAddress packAddr = pack.getAddress();
        if (address != null) { // The socket is connected
            if (packAddr != null) {
                if (!address.equals(packAddr) || port != pack.getPort()) {
                    throw new IllegalArgumentException(Msg.getString("K0034")); //$NON-NLS-1$
                }
            } else {
                pack.setAddress(address);
                pack.setPort(port);
            }
        } else {
            // not connected so the target address is not allowed to be null
            if (packAddr == null) {
                if (pack.port == -1) {
                    // KA019 Destination address is null
                    throw new NullPointerException(Msg.getString("KA019")); //$NON-NLS-1$
                }
                return;
            }
            SecurityManager security = System.getSecurityManager();
            if (security != null) {
                if (packAddr.isMulticastAddress()) {
                    security.checkMulticast(packAddr);
                } else {
                    security.checkConnect(packAddr.getHostName(), pack
                            .getPort());
                }
            }
        }
        impl.send(pack);
    }

    /**
     * Set the socket send buffer size.
     * 
     * @param size
     *            the buffer size, in bytes. Must be at least one byte.
     * 
     * @exception java.net.SocketException
     *                If an error occurs while setting the size or the size is
     *                invalid.
     */
    public synchronized void setSendBufferSize(int size) throws SocketException {
        if (size < 1) {
            throw new IllegalArgumentException(Msg.getString("K0035")); //$NON-NLS-1$
        }
        checkClosedAndBind(false);
        impl.setOption(SocketOptions.SO_SNDBUF, Integer.valueOf(size));
    }

    /**
     * Set the socket receive buffer size.
     * 
     * @param size
     *            the buffer size, in bytes. Must be at least one byte.
     * 
     * @exception java.net.SocketException
     *                If an error occurs while setting the size or the size is
     *                invalid.
     */
    public synchronized void setReceiveBufferSize(int size)
            throws SocketException {
        if (size < 1) {
            throw new IllegalArgumentException(Msg.getString("K0035")); //$NON-NLS-1$
        }
        checkClosedAndBind(false);
        impl.setOption(SocketOptions.SO_RCVBUF, Integer.valueOf(size));
    }

    /**
     * Set the SO_RCVTIMEOUT to <code>timeout</code>, in milliseconds. The
     * receive timeout defines the period a socket will block waiting to receive
     * data, before throwing an InterruptedIOException.
     * 
     * @param timeout
     *            the timeout period, in milliseconds
     * 
     * @exception java.net.SocketException
     *                If an error occurs while setting the timeout or the period
     *                is invalid.
     */
    public synchronized void setSoTimeout(int timeout) throws SocketException {
        if (timeout < 0) {
            throw new IllegalArgumentException(Msg.getString("K0036")); //$NON-NLS-1$
        }
        checkClosedAndBind(false);
        impl.setOption(SocketOptions.SO_TIMEOUT, Integer.valueOf(timeout));
    }

    /**
     * Specifies the application's socket implementation factory. This may only
     * be invoked once over the lifetime of the application.
     * 
     * @param fac
     *            the socket factory to set
     * @exception IOException
     *                thrown if the factory has already been set
     */
    public static synchronized void setDatagramSocketImplFactory(
            DatagramSocketImplFactory fac) throws IOException {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkSetFactory();
        }
        if (factory != null) {
            throw new SocketException(Msg.getString("K0044")); //$NON-NLS-1$
        }
        factory = fac;
    }

    /**
     * Constructs a DatagramSocket using the specified DatagramSocketImpl. The
     * DatagramSocket is not bound.
     * 
     * @param socketImpl
     *            the DatagramSocketImpl to use
     */
    protected DatagramSocket(DatagramSocketImpl socketImpl) {
        if (socketImpl == null) {
            throw new NullPointerException();
        }
        impl = socketImpl;
    }

    /**
     * Constructs a DatagramSocket bound to the host/port specified by the
     * SocketAddress, or an unbound DatagramSocket if the SocketAddress is null.
     * 
     * @param localAddr
     *            the local machine address and port to bind to
     * 
     * @throws IllegalArgumentException
     *             if the SocketAddress is not supported
     * @throws SocketException
     *             if a problem occurs creating or binding the socket
     */
    public DatagramSocket(SocketAddress localAddr) throws SocketException {
        if (localAddr != null) {
            if (!(localAddr instanceof InetSocketAddress)) {
                throw new IllegalArgumentException(Msg.getString(
                        "K0316", localAddr.getClass())); //$NON-NLS-1$
            }
            checkListen(((InetSocketAddress) localAddr).getPort());
        }
        impl = factory != null ? factory.createDatagramSocketImpl()
                : SocketImplProvider.getDatagramSocketImpl();
        impl.create();
        if (localAddr != null) {
            try {
                bind(localAddr);
            } catch (SocketException e) {
                close();
                throw e;
            }
        }
        // SocketOptions.SO_BROADCAST is set by default for DatagramSocket
        setBroadcast(true);
    }

    void checkClosedAndBind(boolean bind) throws SocketException {
        if (isClosed()) {
            throw new SocketException(Msg.getString("K003d")); //$NON-NLS-1$
        }
        if (bind && !isBound()) {
            checkListen(0);
            impl.bind(0, InetAddress.ANY);
            isBound = true;
        }
    }

    /**
     * Bind the DatagramSocket to the nominated local host/port.
     * 
     * @param localAddr
     *            the local machine address and port to bind on
     * 
     * @throws IllegalArgumentException
     *             if the SocketAddress is not supported
     * @throws SocketException
     *             if the socket is already bound, or a problem occurs during
     *             the bind
     */
    public void bind(SocketAddress localAddr) throws SocketException {
        checkClosedAndBind(false);
        int localPort = 0;
        InetAddress addr = InetAddress.ANY;
        if (localAddr != null) {
            if (!(localAddr instanceof InetSocketAddress)) {
                throw new IllegalArgumentException(Msg.getString(
                        "K0316", localAddr.getClass())); //$NON-NLS-1$
            }
            InetSocketAddress inetAddr = (InetSocketAddress) localAddr;
            addr = inetAddr.getAddress();
            if (addr == null) {
                throw new SocketException(Msg.getString(
                        "K0317", inetAddr.getHostName())); //$NON-NLS-1$
            }
            localPort = inetAddr.getPort();
            checkListen(localPort);
        }
        impl.bind(localPort, addr);
        isBound = true;
    }

    /**
     * Connect the datagram socket to a remote host and port. The host and port
     * are validated, thereafter the only validation on send() and receive() is
     * that the packet address/port matches the connected target.
     * 
     * @param remoteAddr
     *            the target address and port
     * 
     * @exception SocketException
     *                if a problem occurs during the connect
     */
    public void connect(SocketAddress remoteAddr) throws SocketException {
        if (remoteAddr == null) {
            throw new IllegalArgumentException(Msg.getString("K0318")); //$NON-NLS-1$
        }

        if (!(remoteAddr instanceof InetSocketAddress)) {
            throw new IllegalArgumentException(Msg.getString(
                    "K0316", remoteAddr.getClass())); //$NON-NLS-1$
        }

        InetSocketAddress inetAddr = (InetSocketAddress) remoteAddr;
        if (inetAddr.getAddress() == null) {
            throw new SocketException(Msg.getString(
                    "K0317", inetAddr.getHostName())); //$NON-NLS-1$
        }

        synchronized (lock) {
            // make sure the socket is open
            checkClosedAndBind(true);

            SecurityManager security = System.getSecurityManager();
            if (security != null) {
                if (inetAddr.getAddress().isMulticastAddress()) {
                    security.checkMulticast(inetAddr.getAddress());
                } else {
                    security.checkConnect(inetAddr.getAddress().getHostName(),
                            inetAddr.getPort());
                }
            }

            // now try to do the connection at the native level. To be
            // compatible for the case when the address is inaddr_any we just
            // eat the exception an act as if we are connected at the java level
            try {
                impl.connect(inetAddr.getAddress(), inetAddr.getPort());
            } catch (Exception e) {
                // not connected at the native level just do what we did before
            }

            // if we get here then we connected ok
            address = inetAddr.getAddress();
            port = inetAddr.getPort();
            isConnected = true;
        }
    }

    /**
     * Return if the socket is bound to a local address and port.
     * 
     * @return <code>true</code> if the socket is bound to a local address,
     *         <code>false</code> otherwise.
     */
    public boolean isBound() {
        return isBound;
    }

    /**
     * Return if the socket is connected.
     * 
     * @return <code>true</code> if the socket is connected,
     *         <code>false</code> otherwise.
     */
    public boolean isConnected() {
        return isConnected;
    }

    /**
     * Answer the remote SocketAddress for this socket, or null if the socket is
     * not connected.
     * 
     * @return the remote socket address
     */
    public SocketAddress getRemoteSocketAddress() {
        if (!isConnected()) {
            return null;
        }
        return new InetSocketAddress(getInetAddress(), getPort());
    }

    /**
     * Answer the local SocketAddress for this socket, or null if the socket is
     * not bound.
     * <p>
     * This is useful on multihomed hosts.
     * 
     * @return the local socket address
     */
    public SocketAddress getLocalSocketAddress() {
        if (!isBound()) {
            return null;
        }
        return new InetSocketAddress(getLocalAddress(), getLocalPort());
    }

    /**
     * Set the SO_REUSEADDR socket option.
     * 
     * @param reuse
     *            the socket SO_REUSEADDR option setting
     * 
     * @throws SocketException
     *             if the socket is closed or the option is invalid.
     */
    public void setReuseAddress(boolean reuse) throws SocketException {
        checkClosedAndBind(false);
        impl.setOption(SocketOptions.SO_REUSEADDR, reuse ? Boolean.TRUE
                : Boolean.FALSE);
    }

    /**
     * Get the state of the SO_REUSEADDR socket option.
     * 
     * @return <code>true</code> if the SO_REUSEADDR is enabled,
     *         <code>false</code> otherwise.
     * 
     * @throws SocketException
     *             if the socket is closed or the option is invalid.
     */
    public boolean getReuseAddress() throws SocketException {
        checkClosedAndBind(false);
        return ((Boolean) impl.getOption(SocketOptions.SO_REUSEADDR))
                .booleanValue();
    }

    /**
     * Set the SO_BROADCAST socket option.
     * 
     * @param broadcast
     *            the socket SO_BROADCAST option setting
     * 
     * @throws SocketException
     *             if the socket is closed or the option is invalid.
     */
    public void setBroadcast(boolean broadcast) throws SocketException {
        checkClosedAndBind(false);
        impl.setOption(SocketOptions.SO_BROADCAST, broadcast ? Boolean.TRUE
                : Boolean.FALSE);
    }

    /**
     * Get the state of the SO_BROADCAST socket option.
     * 
     * @return <code>true</code> if the SO_BROADCAST is enabled,
     *         <code>false</code> otherwise.
     * 
     * @throws SocketException
     *             if the socket is closed or the option is invalid.
     */
    public boolean getBroadcast() throws SocketException {
        checkClosedAndBind(false);
        return ((Boolean) impl.getOption(SocketOptions.SO_BROADCAST))
                .booleanValue();
    }

    /**
     * Set the IP_TOS socket option.
     * 
     * @param value
     *            the socket IP_TOS setting
     * 
     * @throws SocketException
     *             if the socket is closed or the option is invalid.
     */
    public void setTrafficClass(int value) throws SocketException {
        checkClosedAndBind(false);
        if (value < 0 || value > 255) {
            throw new IllegalArgumentException();
        }
        impl.setOption(SocketOptions.IP_TOS, Integer.valueOf(value));
    }

    /**
     * Get the IP_TOS socket option.
     * 
     * @return the IP_TOS socket option value
     * 
     * @throws SocketException
     *             if the option is invalid
     */
    public int getTrafficClass() throws SocketException {
        checkClosedAndBind(false);
        return ((Number) impl.getOption(SocketOptions.IP_TOS)).intValue();
    }

    /**
     * Return if the socket is closed.
     * 
     * @return <code>true</code> if the socket is closed, <code>false</code>
     *         otherwise.
     */
    public boolean isClosed() {
        return isClosed;
    }

    /**
     * if DatagramSocket is created by a DatagramChannel, returns the related
     * DatagramChannel
     * 
     * @return the related DatagramChannel if any
     */
    public DatagramChannel getChannel() {
        return null;
    }
}
