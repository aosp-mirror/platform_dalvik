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
import java.nio.channels.ServerSocketChannel;

import org.apache.harmony.luni.net.SocketImplProvider;
import org.apache.harmony.luni.platform.Platform;

import org.apache.harmony.luni.util.Msg;

/**
 * This class represents a server-side socket that waits for incoming client
 * connections. A {@code ServerSocket} handles the requests and sends back an
 * appropriate reply. The actual tasks that a server socket must accomplish are
 * implemented by an internal {@code SocketImpl} instance.
 * 
 * @since Android 1.0
 */
public class ServerSocket {

    SocketImpl impl;

    static SocketImplFactory factory;

    private volatile boolean isCreated;

    private boolean isBound;

    private boolean isClosed;
    
    static {
        Platform.getNetworkSystem().oneTimeInitialization(true);
    }

    /**
     * Constructs a new {@code ServerSocket} instance which is not bound to any
     * port. The default number of pending connections may be backlogged.
     * 
     * @throws IOException
     *             if an error occurs while creating the server socket.
     * @since Android 1.0
     */
    public ServerSocket() throws IOException {
        impl = factory != null ? factory.createSocketImpl()
                : SocketImplProvider.getServerSocketImpl();
    }

    /**
     * Unspecified constructor.
     *
     * Warning: this function is technically part of API#1.
     * Hiding it for API#2 broke source compatibility.
     * Removing it entirely would theoretically break binary compatibility,
     *     and would be better done with some visibility over the extent
     *     of the compatibility breakage (expected to be non-existent).
     *
     * @hide
     */
    protected ServerSocket(SocketImpl impl) {
        this.impl = impl;
    }

    /**
     * Constructs a new {@code ServerSocket} instance bound to the nominated
     * port on the localhost. The default number of pending connections may be
     * backlogged. If {@code aport} is 0 a free port is assigned to the socket.
     * 
     * @param aport
     *            the port number to listen for connection requests on.
     * @throws IOException
     *             if an error occurs while creating the server socket.
     * @since Android 1.0
     */
    public ServerSocket(int aport) throws IOException {
        this(aport, defaultBacklog(), InetAddress.ANY);
    }

    /**
     * Constructs a new {@code ServerSocket} instance bound to the nominated
     * port on the localhost. The number of pending connections that may be
     * backlogged is specified by {@code backlog}. If {@code aport} is 0 a free
     * port is assigned to the socket.
     * 
     * @param aport
     *            the port number to listen for connection requests on.
     * @param backlog
     *            the number of pending connection requests, before requests
     *            will be rejected.
     * @throws IOException
     *             if an error occurs while creating the server socket.
     * @since Android 1.0
     */
    public ServerSocket(int aport, int backlog) throws IOException {
        this(aport, backlog, InetAddress.ANY);
    }

    /**
     * Constructs a new {@code ServerSocket} instance bound to the nominated
     * local host address and port. The number of pending connections that may
     * be backlogged is specified by {@code backlog}. If {@code aport} is 0 a
     * free port is assigned to the socket.
     * 
     * @param aport
     *            the port number to listen for connection requests on.
     * @param localAddr
     *            the local machine address to bind on.
     * @param backlog
     *            the number of pending connection requests, before requests
     *            will be rejected.
     * @throws IOException
     *             if an error occurs while creating the server socket.
     * @since Android 1.0
     */
    public ServerSocket(int aport, int backlog, InetAddress localAddr)
            throws IOException {
        super();
        checkListen(aport);
        impl = factory != null ? factory.createSocketImpl()
                : SocketImplProvider.getServerSocketImpl();
        InetAddress addr = localAddr == null ? InetAddress.ANY : localAddr;

        synchronized (this) {
            impl.create(true);
            isCreated = true;
            try {
                impl.bind(addr, aport);
                isBound = true;
                impl.listen(backlog > 0 ? backlog : defaultBacklog());
            } catch (IOException e) {
                close();
                throw e;
            }
        }
    }

    /**
     * Waits for an incoming request and blocks until the connection is opened.
     * This method returns a socket object representing the just opened
     * connection.
     * 
     * @return the connection representing socket.
     * @throws IOException
     *             if an error occurs while accepting a new connection.
     * @since Android 1.0
     */
    public Socket accept() throws IOException {
        checkClosedAndCreate(false);
        if (!isBound()) {
            throw new SocketException(Msg.getString("K031f")); //$NON-NLS-1$
        }

        Socket aSocket = new Socket();
        try {
            synchronized (this) {
                implAccept(aSocket);
            }
            SecurityManager security = System.getSecurityManager();
            if (security != null) {
                security.checkAccept(aSocket.getInetAddress().getHostAddress(),
                        aSocket.getPort());
            }
        } catch (SecurityException e) {
            aSocket.close();
            throw e;
        } catch (IOException e) {
            aSocket.close();
            throw e;
        }
        return aSocket;
    }

    /**
     * Checks whether the server may listen for connection requests on {@code
     * aport}. Throws an exception if the port is outside the valid range
     * {@code 0 <= aport <= 65535 }or does not satisfy the security policy.
     * 
     * @param aPort
     *            the candidate port to listen on.
     * @since Android 1.0
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
     * Closes this server socket and its implementation. Any attempt to connect
     * to this socket thereafter will fail.
     * 
     * @throws IOException
     *             if an error occurs while closing this socket.
     * @since Android 1.0
     */
    public void close() throws IOException {
        isClosed = true;
        impl.close();
    }

    /**
     * Answer the default number of pending connections on a server socket. If
     * the backlog value maximum is reached, any subsequent incoming request is
     * rejected.
     * 
     * @return int the default number of pending connection requests
     */
    static int defaultBacklog() {
        return 50;
    }

    /**
     * Gets the local IP address of this server socket or {@code null} if the
     * socket is unbound. This is useful for multihomed hosts.
     * 
     * @return the local address of this server socket.
     * @since Android 1.0
     */
    public InetAddress getInetAddress() {
        if (!isBound()) {
            return null;
        }
        return impl.getInetAddress();
    }

    /**
     * Gets the local port of this server socket or {@code -1} if the socket is
     * unbound.
     * 
     * @return the local port this server is listening on.
     * @since Android 1.0
     */
    public int getLocalPort() {
        if (!isBound()) {
            return -1;
        }
        return impl.getLocalPort();
    }

    /**
     * Gets the timeout period of this server socket. This is the time the
     * server will wait listening for accepted connections before exiting.
     * 
     * @return the listening timeout value of this server socket.
     * @throws IOException
     *             if the option cannot be retrieved.
     * @since Android 1.0
     */
    public synchronized int getSoTimeout() throws IOException {
        if (!isCreated) {
            synchronized (this) {
                if (!isCreated) {
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
        }
        return ((Integer) impl.getOption(SocketOptions.SO_TIMEOUT)).intValue();
    }

    /**
     * Invokes the server socket implementation to accept a connection on the
     * given socket {@code aSocket}.
     * 
     * @param aSocket
     *            the concrete {@code SocketImpl} to accept the connection
     *            request on.
     * @throws IOException
     *             if the connection cannot be accepted.
     * @since Android 1.0
     */
    protected final void implAccept(Socket aSocket) throws IOException {
        impl.accept(aSocket.impl);
        aSocket.accepted();
    }

    /**
     * Sets the server socket implementation factory of this instance. This
     * method may only be invoked with sufficient security privilege and only
     * once during the application lifetime.
     * 
     * @param aFactory
     *            the streaming socket factory to be used for further socket
     *            instantiations.
     * @throws IOException
     *             if the factory could not be set or is already set.
     * @since Android 1.0
     */
    public static synchronized void setSocketFactory(SocketImplFactory aFactory)
            throws IOException {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkSetFactory();
        }
        if (factory != null) {
            throw new SocketException(Msg.getString("K0042")); //$NON-NLS-1$
        }
        factory = aFactory;
    }

    /**
     * Sets the timeout period of this server socket. This is the time the
     * server will wait listening for accepted connections before exiting. This
     * value must be a positive number.
     * 
     * @param timeout
     *            the listening timeout value of this server socket.
     * @throws SocketException
     *             if an error occurs while setting the option.
     * @since Android 1.0
     */
    public synchronized void setSoTimeout(int timeout) throws SocketException {
        checkClosedAndCreate(true);
        if (timeout < 0) {
            throw new IllegalArgumentException(Msg.getString("K0036")); //$NON-NLS-1$
        }
        impl.setOption(SocketOptions.SO_TIMEOUT, Integer.valueOf(timeout));
    }

    /**
     * Returns a textual representation of this server socket including the
     * address, port and the state. The port field is set to {@code 0} if there
     * is no connection to the server socket.
     * 
     * @return the textual socket representation.
     * @since Android 1.0
     */
    @Override
    public String toString() {
        StringBuffer result = new StringBuffer(64);
        result.append("ServerSocket["); //$NON-NLS-1$
        if (!isBound()) {
            return result.append("unbound]").toString(); //$NON-NLS-1$
        }
        return result.append("addr=") //$NON-NLS-1$
                .append(getInetAddress().getHostName()).append("/") //$NON-NLS-1$
                .append(getInetAddress().getHostAddress()).append(
                        ",port=0,localport=") //$NON-NLS-1$
                .append(getLocalPort()).append("]") //$NON-NLS-1$
                .toString();
    }

    /**
     * Binds this server socket to the given local socket address. The default
     * number of pending connections may be backlogged. If the {@code localAddr}
     * is set to {@code null} the socket will be bound to an available local
     * address on any free port of the system.
     * 
     * @param localAddr
     *            the local address and port to bind on.
     * @throws IllegalArgumentException
     *             if the {@code SocketAddress} is not supported.
     * @throws IOException
     *             if the socket is already bound or a problem occurs during
     *             binding.
     * @since Android 1.0
     */
    public void bind(SocketAddress localAddr) throws IOException {
        bind(localAddr, defaultBacklog());
    }

    /**
     * Binds this server socket to the given local socket address. If the
     * {@code localAddr} is set to {@code null} the socket will be bound to an
     * available local address on any free port of the system. The value for
     * {@code backlog} must e greater than {@code 0} otherwise the default value
     * will be used.
     * 
     * @param localAddr
     *            the local machine address and port to bind on.
     * @param backlog
     *            the number of pending connection requests, before requests
     *            will be rejected.
     * @throws IllegalArgumentException
     *             if the {@code SocketAddress} is not supported.
     * @throws IOException
     *             if the socket is already bound or a problem occurs during
     *             binding.
     * @since Android 1.0
     */
    public void bind(SocketAddress localAddr, int backlog) throws IOException {
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
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkListen(port);
        }

        synchronized (this) {
            try {
                impl.bind(addr, port);
                isBound = true;
                impl.listen(backlog > 0 ? backlog : defaultBacklog());
            } catch (IOException e) {
                close();
                throw e;
            }
        }
    }

    /**
     * Gets the local socket address of this server socket or {@code null} if
     * the socket is unbound. This is useful on multihomed hosts.
     * 
     * @return the local socket address and port this socket is bound to.
     * @since Android 1.0
     */
    public SocketAddress getLocalSocketAddress() {
        if (!isBound()) {
            return null;
        }
        return new InetSocketAddress(getInetAddress(), getLocalPort());
    }

    /**
     * Returns whether this server socket is bound to a local address and port
     * or not.
     * 
     * @return {@code true} if this socket is bound, {@code false} otherwise.
     * @since Android 1.0
     */
    public boolean isBound() {
        return isBound;
    }

    /**
     * Returns whether this server socket is closed or not.
     * 
     * @return {@code true} if this socket is closed, {@code false} otherwise.
     * @since Android 1.0
     */
    public boolean isClosed() {
        return isClosed;
    }

    /**
     * Checks whether the socket is closed, and throws an exception.
     */
    private void checkClosedAndCreate(boolean create) throws SocketException {
        if (isClosed()) {
            throw new SocketException(Msg.getString("K003d")); //$NON-NLS-1$
        }

        if (!create || isCreated) {
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
     * Sets the value for the socket option {@code SocketOptions.SO_REUSEADDR}.
     * 
     * @param reuse
     *            the socket option setting.
     * @throws SocketException
     *             if an error occurs while setting the option value.
     * @since Android 1.0
     */
    public void setReuseAddress(boolean reuse) throws SocketException {
        checkClosedAndCreate(true);
        impl.setOption(SocketOptions.SO_REUSEADDR, reuse ? Boolean.TRUE
                : Boolean.FALSE);
    }

    /**
     * Gets the value of the socket option {@code SocketOptions.SO_REUSEADDR}.
     * 
     * @return {@code true} if the option is enabled, {@code false} otherwise.
     * @throws SocketException
     *             if an error occurs while reading the option value.
     * @since Android 1.0
     */
    public boolean getReuseAddress() throws SocketException {
        checkClosedAndCreate(true);
        return ((Boolean) impl.getOption(SocketOptions.SO_REUSEADDR))
                .booleanValue();
    }

    /**
     * Sets the server socket receive buffer size {@code
     * SocketOptions.SO_RCVBUF}.
     * 
     * @param size
     *            the buffer size in bytes.
     * @throws SocketException
     *             if an error occurs while setting the size or the size is
     *             invalid.
     * @since Android 1.0
     */
    public void setReceiveBufferSize(int size) throws SocketException {
        checkClosedAndCreate(true);
        if (size < 1) {
            throw new IllegalArgumentException(Msg.getString("K0035")); //$NON-NLS-1$
        }
        impl.setOption(SocketOptions.SO_RCVBUF, Integer.valueOf(size));
    }

    /**
     * Gets the value for the receive buffer size socket option {@code
     * SocketOptions.SO_RCVBUF}.
     * 
     * @return the receive buffer size of this socket.
     * @throws SocketException
     *             if an error occurs while reading the option value.
     * @since Android 1.0
     */
    public int getReceiveBufferSize() throws SocketException {
        checkClosedAndCreate(true);
        return ((Integer) impl.getOption(SocketOptions.SO_RCVBUF)).intValue();
    }

    /**
     * Gets the related channel if this instance was created by a
     * {@code ServerSocketChannel}. The current implementation returns always {@code
     * null}.
     * 
     * @return the related {@code ServerSocketChannel} if any.
     * @since Android 1.0
     */
    public ServerSocketChannel getChannel() {
        return null;
    }

    /**
     * Sets performance preferences for connection time, latency and bandwidth.
     * <p>
     * This method does currently nothing.
     * </p>
     * 
     * @param connectionTime
     *            the value representing the importance of a short connecting
     *            time.
     * @param latency
     *            the value representing the importance of low latency.
     * @param bandwidth
     *            the value representing the importance of high bandwidth.
     * @since Android 1.0
     */
    public void setPerformancePreferences(int connectionTime, int latency,
            int bandwidth) {
        // Our socket implementation only provide one protocol: TCP/IP, so
        // we do nothing for this method
    }
}
