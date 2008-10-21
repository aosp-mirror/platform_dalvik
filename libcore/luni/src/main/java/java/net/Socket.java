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
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.SocketChannel;
import java.security.AccessController;

import org.apache.harmony.luni.net.NetUtil;
//import android.net.SocketImplProvider;
import org.apache.harmony.luni.net.SocketImplProvider;
import org.apache.harmony.luni.platform.Platform;
import org.apache.harmony.luni.util.Msg;
import org.apache.harmony.luni.util.PriviAction;

/**
 * This class represents sockets to be used in connection-oriented (streaming)
 * protocols.
 */
public class Socket {

    SocketImpl impl;

    static SocketImplFactory factory;

    private volatile boolean isCreated = false;

    private boolean isBound = false;

    private boolean isConnected = false;

    private boolean isClosed = false;

    private boolean isInputShutdown = false;

    private boolean isOutputShutdown = false;

    private static class ConnectLock {
    }

    private Object connectLock = new ConnectLock();

    private Proxy proxy;

    static final int MULTICAST_IF = 1;

    static final int MULTICAST_TTL = 2;

    static final int TCP_NODELAY = 4;

    static final int FLAG_SHUTDOWN = 8;
   
    static {
        Platform.getNetworkSystem().oneTimeInitialization(true);
    }

    /**
     * Construct a connection-oriented Socket. The Socket is created in the
     * <code>factory</code> if declared, or otherwise of the default type.
     * 
     * @see SocketImplFactory
     */
    public Socket() {
        impl = factory != null ? factory.createSocketImpl()
                : SocketImplProvider.getSocketImpl();
    }

    /**
     * Constructs a connection-oriented Socket with specified <code>proxy</code>.
     * 
     * Method <code>checkConnect</code> is called if a security manager
     * exists, and the proxy host address and port number are passed as
     * parameters.
     * 
     * @param proxy
     *            the specified proxy for this Socket.
     * @throws IllegalArgumentException
     *             if the proxy is null or of an invalid type.
     * @throws SecurityException
     *             if a security manager exists and it denies the permission to
     *             connect to proxy.
     */
    public Socket(Proxy proxy) {
        if (null == proxy || Proxy.Type.HTTP == proxy.type()) {
            // KA023=Proxy is null or invalid type
            throw new IllegalArgumentException(Msg.getString("KA023")); //$NON-NLS-1$
        }
        InetSocketAddress address = (InetSocketAddress) proxy.address();
        if (null != address) {
            InetAddress addr = address.getAddress();
            String host;
            if (null != addr) {
                host = addr.getHostAddress();
            } else {
                host = address.getHostName();
            }
            int port = address.getPort();
            checkConnectPermission(host, port);
        }
        impl = factory != null ? factory.createSocketImpl()
                : SocketImplProvider.getSocketImpl(proxy);
        this.proxy = proxy;
    }

    /**
     * Construct a stream socket connected to the nominated destination
     * host/port. By default, the socket binds it to any available port on the
     * default localhost.
     * 
     * @param dstName
     *            the destination host to connect to
     * @param dstPort
     *            the port on the destination host to connect to
     * 
     * @throws UnknownHostException
     *             if the host cannot be resolved
     * @throws IOException
     *             if an error occurs while instantiating the socket
     */
    public Socket(String dstName, int dstPort) throws UnknownHostException,
            IOException {
        this();
        InetAddress dstAddress = InetAddress.getByName(dstName);
        checkDestination(dstAddress, dstPort);
        startupSocket(dstAddress, dstPort, null, 0, true);
    }

    /**
     * Construct a stream socket connected to the nominated destination
     * host/port. The socket is bound it to the nominated localAddress/port.
     * 
     * @param dstName
     *            the destination host to connect to
     * @param dstPort
     *            the port on the destination host to connect to
     * @param localAddress
     *            the local host address to bind to
     * @param localPort
     *            the local port to bind to
     * 
     * @throws UnknownHostException
     *             if the host cannot be resolved
     * @throws IOException
     *             if an error occurs while instantiating the socket
     */
    public Socket(String dstName, int dstPort, InetAddress localAddress,
            int localPort) throws IOException {
        this();
        InetAddress dstAddress = InetAddress.getByName(dstName);
        checkDestination(dstAddress, dstPort);
        startupSocket(dstAddress, dstPort, localAddress, localPort, true);
    }

    /**
     * Answer a new socket. This constructor is deprecated.
     * 
     * @param hostName
     *            the host name
     * @param port
     *            the port on the host
     * @param streaming
     *            if true, answer a stream socket, else answer a a datagram
     *            socket.
     * 
     * @throws UnknownHostException
     *             if the host cannot be resolved
     * @throws IOException
     *             if an error occurs while instantiating the socket
     * 
     * @deprecated As of JDK 1.1, replaced by Socket
     * @see #Socket(String,int)
     */
    @Deprecated
    public Socket(String hostName, int port, boolean streaming)
            throws IOException {
        this();
        InetAddress host = InetAddress.getByName(hostName);
        checkDestination(host, port);
        startupSocket(host, port, null, 0, streaming);
    }

    /**
     * Construct a stream socket connected to the nominated destination host
     * address/port. By default, the socket binds it to any available port on
     * the default localhost.
     * 
     * @param dstAddress
     *            the destination host address to connect to
     * @param dstPort
     *            the port on the destination host to connect to
     * 
     * @throws IOException
     *             if an error occurs while instantiating the socket
     */
    public Socket(InetAddress dstAddress, int dstPort) throws IOException {
        this();
        checkDestination(dstAddress, dstPort);
        startupSocket(dstAddress, dstPort, null, 0, true);
    }

    /**
     * Construct a stream socket connected to the nominated destination host
     * address/port. The socket is bound it to the nominated localAddress/port.
     * 
     * @param dstAddress
     *            the destination host address to connect to
     * @param dstPort
     *            the port on the destination host to connect to
     * @param localAddress
     *            the local host address to bind to
     * @param localPort
     *            the local port to bind to
     * 
     * @throws IOException
     *             if an error occurs while instantiating the socket
     */
    public Socket(InetAddress dstAddress, int dstPort,
            InetAddress localAddress, int localPort) throws IOException {
        this();
        checkDestination(dstAddress, dstPort);
        startupSocket(dstAddress, dstPort, localAddress, localPort, true);
    }

    /**
     * Answer a new socket. This constructor is deprecated.
     * 
     * @param addr
     *            the internet address
     * @param port
     *            the port on the host
     * @param streaming
     *            if true, answer a stream socket, else answer a datagram
     *            socket.
     * 
     * @throws UnknownHostException
     *             if the host cannot be resolved
     * @throws IOException
     *             if an error occurs while instantiating the socket
     * 
     * @deprecated As of JDK 1.1, replaced by Socket
     * @see #Socket(InetAddress,int)
     */
    @Deprecated
    public Socket(InetAddress addr, int port, boolean streaming)
            throws IOException {
        this();
        checkDestination(addr, port);
        startupSocket(addr, port, null, 0, streaming);
    }

    /**
     * Creates an unconnected socket, wrapping the <code>socketImpl</code>
     * argument.
     * 
     * @param anImpl
     *            the socket to wrap
     * 
     * @throws SocketException
     *             if an error occurs assigning the implementation
     */
    protected Socket(SocketImpl anImpl) throws SocketException {
        impl = anImpl;
    }

    /**
     * Check the connection destination satisfies the security policy and is in
     * the valid port range.
     * 
     * @param destAddr
     *            the destination host address
     * @param dstPort
     *            the port on the destination host
     */
    void checkDestination(InetAddress destAddr, int dstPort) {
        if (dstPort < 0 || dstPort > 65535) {
            throw new IllegalArgumentException(Msg.getString("K0032")); //$NON-NLS-1$
        }
        checkConnectPermission(destAddr.getHostName(), dstPort);
    }

    /*
     * Checks the connection destination satisfies the security policy.
     * 
     * @param hostname the destination hostname @param dstPort the port on the
     * destination host
     */
    private void checkConnectPermission(String hostname, int dstPort) {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkConnect(hostname, dstPort);
        }
    }

    /**
     * Close the socket. It is not valid to use the socket thereafter.
     * 
     * @throws IOException
     *             if an error occurs during the close
     */
    public synchronized void close() throws IOException {
        isClosed = true;
        impl.close();
    }

    /**
     * Returns an {@link InetAddress} instance representing the address this
     * socket has connected to.
     * 
     * @return if this socket is connected, the address it is connected to. A
     *         <code>null</code> return signifies no connection has been made.
     */
    public InetAddress getInetAddress() {
        if (!isConnected()) {
            return null;
        }
        return impl.getInetAddress();
    }

    /**
     * Answer the socket input stream, to read byte data off the socket. Note,
     * multiple input streams may be created on a single socket.
     * 
     * @return a byte oriented read stream for this socket
     * 
     * @throws IOException
     *             if an error occurs creating the stream
     * 
     * @see org.apache.harmony.luni.net.SocketInputStream
     */
    public InputStream getInputStream() throws IOException {
        checkClosedAndCreate(false);
        if (isInputShutdown()) {
            throw new SocketException(Msg.getString("K0321")); //$NON-NLS-1$
        }
        return impl.getInputStream();
    }

    /**
     * Answer the SO_KEEPALIVE option for this socket.
     * 
     * @return the socket SO_KEEPALIVE option setting
     * 
     * @throws SocketException
     *             if an error occurs on the option access
     */
    public boolean getKeepAlive() throws SocketException {
        checkClosedAndCreate(true);
        return ((Boolean) impl.getOption(SocketOptions.SO_KEEPALIVE))
                .booleanValue();
    }

    /**
     * Returns an {@link InetAddress} instance representing the <i>local</i>
     * address this socket is bound to.
     * 
     * @return the local address that this socket has bound to
     */
    public InetAddress getLocalAddress() {
        if (!isBound()) {
            return InetAddress.ANY;
        }
        return Platform.getNetworkSystem().getSocketLocalAddress(impl.fd,
                InetAddress.preferIPv6Addresses());
    }

    /**
     * Answer the local port to which the socket is bound.
     * 
     * @return the local port to which the socket is bound
     */
    public int getLocalPort() {
        if (!isBound()) {
            return -1;
        }
        return impl.getLocalPort();
    }

    /**
     * Answer the socket output stream, for writing byte data on the socket.
     * Note, multiplie output streams may be created on a single socket.
     * 
     * @return OutputStream a byte oriented write stream for this socket
     * 
     * @throws IOException
     *             if an error occurs creating the stream
     * 
     * @see org.apache.harmony.luni.net.SocketOutputStream
     */
    public OutputStream getOutputStream() throws IOException {
        checkClosedAndCreate(false);
        if (isOutputShutdown()) {
            throw new SocketException(Msg.getString("KA00f")); //$NON-NLS-1$
        }
        return impl.getOutputStream();
    }

    /**
     * Returns the number of the remote port this socket is connected to.
     * 
     * @return int the remote port number that this socket has connected to. A
     *         return of <code>0</code> (zero) indicates that there is no
     *         connection in place.
     */
    public int getPort() {
        if (!isConnected()) {
            return 0;
        }
        return impl.getPort();
    }

    /**
     * Answer the linger-on-close timeout for this socket (the SO_LINGER value).
     * 
     * @return this socket's SO_LINGER value. A value of <code>-1</code> will
     *         be returned if the option is not enabled.
     * 
     * @throws SocketException
     *             if an error occurs on querying this property
     */
    public int getSoLinger() throws SocketException {
        checkClosedAndCreate(true);
        return ((Integer) impl.getOption(SocketOptions.SO_LINGER)).intValue();
    }

    /**
     * Answer the socket receive buffer size (SO_RCVBUF).
     * 
     * @return socket receive buffer size
     * 
     * @throws SocketException
     *             if an error occurs on the option access
     */
    public synchronized int getReceiveBufferSize() throws SocketException {
        checkClosedAndCreate(true);
        return ((Integer) impl.getOption(SocketOptions.SO_RCVBUF)).intValue();
    }

    /**
     * Answer the socket send buffer size (SO_SNDBUF).
     * 
     * @return socket send buffer size
     * 
     * @throws SocketException
     *             if an error occurs on the option access
     */
    public synchronized int getSendBufferSize() throws SocketException {
        checkClosedAndCreate(true);
        return ((Integer) impl.getOption(SocketOptions.SO_SNDBUF)).intValue();
    }

    /**
     * Answer the socket read timeout. The SO_TIMEOUT option, a value of 0
     * indicates it is disabled and a read operation will block indefinitely
     * waiting for data.
     * 
     * @return the socket read timeout
     * 
     * @throws SocketException
     *             if an error occurs on the option access
     */
    public synchronized int getSoTimeout() throws SocketException {
        checkClosedAndCreate(true);
        return ((Integer) impl.getOption(SocketOptions.SO_TIMEOUT)).intValue();
    }

    /**
     * Answer true if the socket is using Nagle's algorithm. The TCP_NODELAY
     * option setting.
     * 
     * @return the socket TCP_NODELAY option setting
     * 
     * @throws SocketException
     *             if an error occurs on the option access
     */
    public boolean getTcpNoDelay() throws SocketException {
        checkClosedAndCreate(true);
        return ((Boolean) impl.getOption(SocketOptions.TCP_NODELAY))
                .booleanValue();
    }

    /**
     * Set the SO_KEEPALIVE option for this socket.
     * 
     * @param value
     *            the socket SO_KEEPALIVE option setting
     * 
     * @throws SocketException
     *             if an error occurs setting the option
     */
    public void setKeepAlive(boolean value) throws SocketException {
        if (impl != null) {
            checkClosedAndCreate(true);
            impl.setOption(SocketOptions.SO_KEEPALIVE, value ? Boolean.TRUE
                    : Boolean.FALSE);
        }
    }

    /**
     * Specifies the application's socket implementation factory. This may only
     * be executed the once over the lifetime of the application.
     * 
     * @param fac
     *            the socket factory to set
     * @exception IOException
     *                thrown if the factory has already been set
     */
    public static synchronized void setSocketImplFactory(SocketImplFactory fac)
            throws IOException {
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
     * Set the socket send buffer size.
     * 
     * @param size
     *            the buffer size, in bytes
     * 
     * @throws SocketException
     *             if an error occurs while setting the size or the size is
     *             invalid.
     */
    public synchronized void setSendBufferSize(int size) throws SocketException {
        checkClosedAndCreate(true);
        if (size < 1) {
            throw new IllegalArgumentException(Msg.getString("K0035")); //$NON-NLS-1$
        }
        impl.setOption(SocketOptions.SO_SNDBUF, Integer.valueOf(size));
    }

    /**
     * Set the socket receive buffer size.
     * 
     * @param size
     *            the buffer size, in bytes
     * 
     * @throws SocketException
     *             tf an error occurs while setting the size or the size is
     *             invalid.
     */
    public synchronized void setReceiveBufferSize(int size)
            throws SocketException {
        checkClosedAndCreate(true);
        if (size < 1) {
            throw new IllegalArgumentException(Msg.getString("K0035")); //$NON-NLS-1$
        }
        impl.setOption(SocketOptions.SO_RCVBUF, Integer.valueOf(size));
    }

    /**
     * Set the SO_LINGER option, with the specified time, in seconds. The
     * SO_LINGER option is silently limited to 65535 seconds.
     * 
     * @param on
     *            if linger is enabled
     * @param timeout
     *            the linger timeout value, in seconds
     * 
     * @throws SocketException
     *             if an error occurs setting the option
     */
    public void setSoLinger(boolean on, int timeout) throws SocketException {
        checkClosedAndCreate(true);
        if (on && timeout < 0) {
            throw new IllegalArgumentException(Msg.getString("K0045")); //$NON-NLS-1$
        }
        // BEGIN android-changed
        /*
         * The spec indicates that the right way to turn off an option
         * is to pass Boolean.FALSE, so that's what we do here.
         */
        if (on) {
            if (timeout > 65535) {
                timeout = 65535;
            }
            impl.setOption(SocketOptions.SO_LINGER, Integer.valueOf(timeout));
        } else {
            impl.setOption(SocketOptions.SO_LINGER, Boolean.FALSE);
        }
        // END android-changed
    }

    /**
     * Set the read timeout on this socket. The SO_TIMEOUT option, is specified
     * in milliseconds. The read operation will block indefinitely for a zero
     * value.
     * 
     * @param timeout
     *            the read timeout value
     * 
     * @throws SocketException
     *             if an error occurs setting the option
     */
    public synchronized void setSoTimeout(int timeout) throws SocketException {
        checkClosedAndCreate(true);
        if (timeout < 0) {
            throw new IllegalArgumentException(Msg.getString("K0036")); //$NON-NLS-1$
        }
        impl.setOption(SocketOptions.SO_TIMEOUT, Integer.valueOf(timeout));
    }

    /**
     * Set whether the socket is to use Nagle's algorithm. The TCP_NODELAY
     * option setting.
     * 
     * @param on
     *            the socket TCP_NODELAY option setting
     * 
     * @throws SocketException
     *             if an error occurs setting the option
     */
    public void setTcpNoDelay(boolean on) throws SocketException {
        checkClosedAndCreate(true);
        impl.setOption(SocketOptions.TCP_NODELAY, Boolean.valueOf(on));
    }

    /**
     * Creates a stream socket, binds it to the nominated local address/port,
     * then connects it to the nominated destination address/port.
     * 
     * @param dstAddress
     *            the destination host address
     * @param dstPort
     *            the port on the destination host
     * @param localAddress
     *            the address on the local machine to bind
     * @param localPort
     *            the port on the local machine to bind
     * 
     * @throws IOException
     *             thrown if a error occurs during the bind or connect
     *             operations
     */
    void startupSocket(InetAddress dstAddress, int dstPort,
            InetAddress localAddress, int localPort, boolean streaming)
            throws IOException {

        if (localPort < 0 || localPort > 65535) {
            throw new IllegalArgumentException(Msg.getString("K0046")); //$NON-NLS-1$
        }

        InetAddress addr = localAddress == null ? InetAddress.ANY
                : localAddress;
        synchronized (this) {
            impl.create(streaming);
            isCreated = true;
            try {
                if (!streaming || !NetUtil.usingSocks(proxy)) {
                    impl.bind(addr, localPort);
                }
                isBound = true;
                impl.connect(dstAddress, dstPort);
                isConnected = true;
            } catch (IOException e) {
                impl.close();
                throw e;
            }
        }
    }

    /**
     * Returns a string containing a concise, human-readable description of the
     * socket.
     * 
     * @return the description
     */
    @Override
    public String toString() {
        if (!isConnected()) {
            return "Socket[unconnected]"; //$NON-NLS-1$
        }
        return impl.toString();
    }

    /**
     * Shutdown the input portion of the socket.
     * 
     * @throws IOException
     *             if an error occurs while closing the socket input
     * @throws SocketException
     *             if the socket is closed
     */
    public void shutdownInput() throws IOException {
        if (isInputShutdown()) {
            throw new SocketException(Msg.getString("K0321")); //$NON-NLS-1$
        }
        checkClosedAndCreate(false);
        impl.shutdownInput();
        isInputShutdown = true;
    }

    /**
     * Shutdown the output portion of the socket.
     * 
     * @throws IOException
     *             if an error occurs while closing the socket output
     * @throws SocketException
     *             if the socket is closed
     */
    public void shutdownOutput() throws IOException {
        if (isOutputShutdown()) {
            throw new SocketException(Msg.getString("KA00f")); //$NON-NLS-1$
        }
        checkClosedAndCreate(false);
        impl.shutdownOutput();
        isOutputShutdown = true;
    }

    /**
     * Check if the socket is closed, and throw an exception. Otherwise create
     * the underlying SocketImpl.
     * 
     * @throws SocketException
     *             if the socket is closed
     */
    private void checkClosedAndCreate(boolean create) throws SocketException {
        if (isClosed()) {
            throw new SocketException(Msg.getString("K003d")); //$NON-NLS-1$
        }
        if (!create) {
            if (!isConnected()) {
                throw new SocketException(Msg.getString("K0320")); //$NON-NLS-1$
                // a connected socket must be created
            }

            /*
             * return directly to fix a possible bug, if !create, should return
             * here
             */
            return;
        }
        if (isCreated) {
            return;
        }
        synchronized (this) {
            if (isCreated) {
                return;
            }
            try {
                impl.create(true);
            } catch (SocketException e) {
                throw e;
            } catch (IOException e) {
                throw new SocketException(e.toString());
            }
            isCreated = true;
        }
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
     * Return if the socket is closed.
     * 
     * @return <code>true</code> if the socket is closed, <code>false</code>
     *         otherwise.
     */
    public boolean isClosed() {
        return isClosed;
    }

    /**
     * Bind the Socket to the nominated local host/port.
     * 
     * @param localAddr
     *            the local machine address and port to bind on
     * 
     * @throws IllegalArgumentException
     *             if the SocketAddress is not supported
     * @throws IOException
     *             if the socket is already bound, or a problem occurs during
     *             the bind
     */
    public void bind(SocketAddress localAddr) throws IOException {
        checkClosedAndCreate(true);
        if (isBound()) {
            throw new BindException(Msg.getString("K0315")); //$NON-NLS-1$
        }
        
        int port = 0;
        InetAddress addr = InetAddress.ANY;
        if (localAddr != null) {
            if (!(localAddr instanceof InetSocketAddress)) {
                throw new IllegalArgumentException(Msg.getString(
                        "K0316", localAddr.getClass())); //$NON-NLS-1$
            }
            InetSocketAddress inetAddr = (InetSocketAddress) localAddr;
            if ((addr = inetAddr.getAddress()) == null) {
                throw new SocketException(Msg.getString(
                        "K0317", inetAddr.getHostName())); //$NON-NLS-1$
            }
            port = inetAddr.getPort();
        }

        synchronized (this) {
            try {
                impl.bind(addr, port);
                isBound = true;
            } catch (IOException e) {
                impl.close();
                throw e;
            }
        }
    }

    /**
     * Connect the Socket to the host/port specified by the SocketAddress.
     * 
     * @param remoteAddr
     *            the remote machine address and port to connect to
     * 
     * @throws IllegalArgumentException
     *             if the SocketAddress is not supported
     * @throws IOException
     *             if the socket is already connected, or a problem occurs
     *             during the connect
     */
    public void connect(SocketAddress remoteAddr) throws IOException {
        connect(remoteAddr, 0);
    }

    /**
     * Connect the Socket to the host/port specified by the SocketAddress with a
     * specified timeout.
     * 
     * @param remoteAddr
     *            the remote machine address and port to connect to
     * @param timeout
     *            the millisecond timeout value, the connect will block
     *            indefinitely for a zero value.
     * 
     * @throws IllegalArgumentException
     *             if the timeout is negative, or the SocketAddress is not
     *             supported
     * @throws IOException
     *             if the socket is already connected, or a problem occurs
     *             during the connect
     */
    public void connect(SocketAddress remoteAddr, int timeout)
            throws IOException {
        checkClosedAndCreate(true);
        if (timeout < 0) {
            throw new IllegalArgumentException(Msg.getString("K0036")); //$NON-NLS-1$
        }
        if (isConnected()) {
            throw new SocketException(Msg.getString("K0079")); //$NON-NLS-1$
        }
        if (remoteAddr == null) {
            throw new IllegalArgumentException(Msg.getString("K0318")); //$NON-NLS-1$
        }

        if (!(remoteAddr instanceof InetSocketAddress)) {
            throw new IllegalArgumentException(Msg.getString(
                    "K0316", remoteAddr.getClass())); //$NON-NLS-1$
        }
        InetSocketAddress inetAddr = (InetSocketAddress) remoteAddr;
        InetAddress addr;
        if ((addr = inetAddr.getAddress()) == null) {
            throw new UnknownHostException(Msg.getString("K0317", remoteAddr));//$NON-NLS-1$
        }
        int port = inetAddr.getPort();

        checkDestination(addr, port);
        synchronized (connectLock) {
            try {
                if (!isBound()) {
                    // socket allready created at this point by earlier call or
                    // checkClosedAndCreate this caused us to lose socket
                    // options on create
                    // impl.create(true);
                    if (!NetUtil.usingSocks(proxy)) {
                        impl.bind(InetAddress.ANY, 0);
                    }
                    isBound = true;
                }
                impl.connect(remoteAddr, timeout);
                isConnected = true;
            } catch (IOException e) {
                impl.close();
                throw e;
            }
        }
    }

    /**
     * Return if {@link #shutdownInput} has been called.
     * 
     * @return <code>true</code> if <code>shutdownInput</code> has been
     *         called, <code>false</code> otherwise.
     */
    public boolean isInputShutdown() {
        return isInputShutdown;
    }

    /**
     * Return if {@link #shutdownOutput} has been called.
     * 
     * @return <code>true</code> if <code>shutdownOutput</code> has been
     *         called, <code>false</code> otherwise.
     */
    public boolean isOutputShutdown() {
        return isOutputShutdown;
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
        checkClosedAndCreate(true);
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
        checkClosedAndCreate(true);
        return ((Boolean) impl.getOption(SocketOptions.SO_REUSEADDR))
                .booleanValue();
    }

    /**
     * Set the SO_OOBINLINE socket option. When this option is enabled, out of
     * band data is recieved in the normal data stream.
     * 
     * @param oobinline
     *            the socket SO_OOBINLINE option setting
     * 
     * @throws SocketException
     *             if the socket is closed or the option is invalid.
     */
    public void setOOBInline(boolean oobinline) throws SocketException {
        checkClosedAndCreate(true);
        impl.setOption(SocketOptions.SO_OOBINLINE, oobinline ? Boolean.TRUE
                : Boolean.FALSE);
    }

    /**
     * Get the state of the SO_OOBINLINE socket option.
     * 
     * @return <code>true</code> if the SO_OOBINLINE is enabled,
     *         <code>false</code> otherwise.
     * 
     * @throws SocketException
     *             if the socket is closed or the option is invalid.
     */
    public boolean getOOBInline() throws SocketException {
        checkClosedAndCreate(true);
        return ((Boolean) impl.getOption(SocketOptions.SO_OOBINLINE))
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
        checkClosedAndCreate(true);
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
        checkClosedAndCreate(true);
        return ((Number) impl.getOption(SocketOptions.IP_TOS)).intValue();
    }

    /**
     * Send the single byte of urgent data on the socket.
     * 
     * @param value
     *            the byte of urgent data
     * 
     * @exception IOException
     *                when an error occurs sending urgent data
     */
    public void sendUrgentData(int value) throws IOException {
        if (!impl.supportsUrgentData()) {
            throw new SocketException(Msg.getString("K0333")); //$NON-NLS-1$
        }
        impl.sendUrgentData(value);
    }

    /**
     * Set the appropriate flags for a Socket created by ServerSocket.accept().
     * 
     * @see ServerSocket#implAccept
     */
    void accepted() {
        isCreated = isBound = isConnected = true;
    }

    static boolean preferIPv4Stack() {
        String result = AccessController.doPrivileged(new PriviAction<String>(
                "java.net.preferIPv4Stack")); //$NON-NLS-1$
        return "true".equals(result); //$NON-NLS-1$
    }

    /**
     * if Socket is created by a SocketChannel, returns the related
     * SocketChannel
     * 
     * @return the related SocketChannel
     */
    public SocketChannel getChannel() {
        return null;
    }

    /**
     * sets performance preference for connectionTime,latency and bandwidth
     * 
     * @param connectionTime
     *            the importance of connect time
     * @param latency
     *            the importance of latency
     * @param bandwidth
     *            the importance of bandwidth
     */
    public void setPerformancePreferences(int connectionTime, int latency,
            int bandwidth) {
        // Our socket implementation only provides one protocol: TCP/IP, so
        // we do nothing for this method
    }
}
