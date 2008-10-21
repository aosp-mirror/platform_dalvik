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

//import android.net.SocketImplProvider;
import org.apache.harmony.luni.net.SocketImplProvider;
import org.apache.harmony.luni.platform.Platform;

import org.apache.harmony.luni.util.Msg;

/**
 * ServerSocket create connections between 'host' and 'client' machines. The
 * ServerSocket listens on a well known port and upon a connection request,
 * instantiates a 'host' sockets, which carries on future communication with the
 * requesting 'client' socket, so that the server socket may continue listening
 * for connection requests. They are passive objects, having no execution thread
 * of their own to listen on.
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
     * Construct a ServerSocket, which is not bound to any port. The default
     * number of pending connections may be backlogged.
     * 
     * @see Socket
     */
    public ServerSocket() throws IOException {
        impl = factory != null ? factory.createSocketImpl()
                : SocketImplProvider.getServerSocketImpl();
    }

    protected ServerSocket(SocketImpl impl) {
        this.impl = impl;
    }

    /**
     * Construct a ServerSocket, bound to the nominated port on the default
     * localhost. The default number of pending connections may be backlogged.
     * 
     * @param aport
     *            the port number to listen for connection requests on
     * @see Socket
     */
    public ServerSocket(int aport) throws IOException {
        this(aport, defaultBacklog(), InetAddress.ANY);
    }

    /**
     * Construct a ServerSocket, bound to the nominated port on the default
     * localhost. The number of pending connections that may be backlogged is a
     * specified.
     * 
     * @param aport
     *            the port number to listen for connection requests on
     * @param backlog
     *            the number of pending connection requests, before requests are
     *            rejected
     * @see Socket
     */
    public ServerSocket(int aport, int backlog) throws IOException {
        this(aport, backlog, InetAddress.ANY);
    }

    /**
     * Construct a ServerSocket, bound to the nominated local host/port. The
     * number of pending connections that may be backlogged is a specified.
     * 
     * @param aport
     *            the port number to listen for connection requests on
     * @param localAddr
     *            the local machine address to bind on
     * @param backlog
     *            the number of pending connection requests, before requests are
     *            rejected
     * @see Socket
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
     * Retrieve the first connection request and answer the 'host' socket that
     * will conduct further communications with the requesting 'client' socket.
     * 
     * @return Socket the 'host' socket
     * @exception IOException
     *                if an error occurs while instantiating the 'host' socket
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
     * Check whether the server may listen for connection requests on
     * <code>aport</code>. Throw an exception if the port is outside the
     * valid range or does not satisfy the security policy.
     * 
     * @param aPort
     *            the candidate port to listen on
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
     * Close this server socket. Any attempt to connect to this socket
     * thereafter will fail.
     */
    public void close() throws IOException {
        isClosed = true;
        impl.close();
    }

    /**
     * Answer the default number of pending connections on a server socket.
     * 
     * @return int the default number of pending connection requests
     */
    static int defaultBacklog() {
        return 50;
    }

    /**
     * Answer the local IP address for this server socket. Return null if the
     * socket is not bound. This is useful on multihomed hosts.
     * 
     * @return InetAddress the local address
     */
    public InetAddress getInetAddress() {
        if (!isBound()) {
            return null;
        }
        return impl.getInetAddress();
    }

    /**
     * Answer the local port for this server socket. Return -1 if the socket is
     * not bound.
     * 
     * @return int the local port the server is listening on
     */
    public int getLocalPort() {
        if (!isBound()) {
            return -1;
        }
        return impl.getLocalPort();
    }

    /**
     * Answer the time-out period of this server socket. This is the time the
     * server will wait listening for connections, before exiting.
     * 
     * @return int the listening timeout
     * @exception SocketException
     *                thrown if option cannot be retrieved
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
     * Invoke the server socket implementation to accept a connection on the
     * newly created <code>aSocket</code>.
     * 
     * @param aSocket
     *            the concrete socketImpl to accept the connection request on
     * @exception IOException
     *                thrown if connection cannot be accepted
     */
    protected final void implAccept(Socket aSocket) throws IOException {
        impl.accept(aSocket.impl);
        aSocket.accepted();
    }

    /**
     * Set the server socket implementation factory. This method may only be
     * invoked with sufficient security and only once during the application
     * lifetime.
     * 
     * @param aFactory
     *            the streaming socket factory to be used for further socket
     *            instantiations
     * @exception IOException
     *                thrown if the factory is already set
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
     * Set the listen time-out period for this server socket.
     * 
     * @param timeout
     *            the time to wait for a connection request
     * @exception SocketException
     *                thrown if an error occurs during setting the option
     */
    public synchronized void setSoTimeout(int timeout) throws SocketException {
        checkClosedAndCreate(true);
        if (timeout < 0) {
            throw new IllegalArgumentException(Msg.getString("K0036")); //$NON-NLS-1$
        }
        impl.setOption(SocketOptions.SO_TIMEOUT, Integer.valueOf(timeout));
    }

    /**
     * Returns a string containing a concise, human-readable description of the
     * server socket. The <code>port</code> field is reported a zero, as there
     * is no connection formed to the server.
     * 
     * @return String the description
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
     * Bind the ServerSocket to the nominated local host/port. The default
     * number of pending connections may be backlogged.
     * 
     * @param localAddr
     *            the local machine address and port to bind on
     * 
     * @exception IllegalArgumentException
     *                if the SocketAddress is not supported
     * @exception IOException
     *                if the socket is already bound, or a problem occurs during
     *                the bind
     */
    public void bind(SocketAddress localAddr) throws IOException {
        bind(localAddr, defaultBacklog());
    }

    /**
     * Bind the ServerSocket to the nominated local host/port. The number of
     * pending connections that may be backlogged is a specified.
     * 
     * @param localAddr
     *            the local machine address and port to bind on
     * @param backlog
     *            the number of pending connection requests, before requests are
     *            rejected
     * 
     * @exception IllegalArgumentException
     *                if the SocketAddress is not supported
     * @exception IOException
     *                if the socket is already bound, or a problem occurs during
     *                the bind
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
     * Answer the local SocketAddress for this server socket, or null if the
     * socket is not bound. This is useful on multihomed hosts.
     */
    public SocketAddress getLocalSocketAddress() {
        if (!isBound()) {
            return null;
        }
        return new InetSocketAddress(getInetAddress(), getLocalPort());
    }

    /**
     * Return if the server socket is bound to a local address and port.
     */
    public boolean isBound() {
        return isBound;
    }

    /**
     * Return if the server socket is closed.
     */
    public boolean isClosed() {
        return isClosed;
    }

    /**
     * Check if the socket is closed, and throw an exception.
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
     * Set the SO_REUSEADDR socket option.
     * 
     * @param reuse
     *            the socket SO_REUSEADDR option setting
     */
    public void setReuseAddress(boolean reuse) throws SocketException {
        checkClosedAndCreate(true);
        impl.setOption(SocketOptions.SO_REUSEADDR, reuse ? Boolean.TRUE
                : Boolean.FALSE);
    }

    /**
     * Get the state of the SO_REUSEADDR socket option.
     */
    public boolean getReuseAddress() throws SocketException {
        checkClosedAndCreate(true);
        return ((Boolean) impl.getOption(SocketOptions.SO_REUSEADDR))
                .booleanValue();
    }

    /**
     * Set the socket receive buffer size.
     * 
     * @param size
     *            the buffer size, in bytes
     * 
     * @exception java.net.SocketException
     *                If an error occurs while setting the size or the size is
     *                invalid.
     */
    public void setReceiveBufferSize(int size) throws SocketException {
        checkClosedAndCreate(true);
        if (size < 1) {
            throw new IllegalArgumentException(Msg.getString("K0035")); //$NON-NLS-1$
        }
        impl.setOption(SocketOptions.SO_RCVBUF, Integer.valueOf(size));
    }

    /**
     * Answer the socket receive buffer size (SO_RCVBUF).
     * 
     * @return int socket receive buffer size
     */
    public int getReceiveBufferSize() throws SocketException {
        checkClosedAndCreate(true);
        return ((Integer) impl.getOption(SocketOptions.SO_RCVBUF)).intValue();
    }

    /**
     * if ServerSocket is created by a ServerSocketChannel, returns the related
     * ServerSocketChannel
     * 
     * @return the related ServerSocketChannel if any
     */
    public ServerSocketChannel getChannel() {
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
        // Our socket implementation only provide one protocol: TCP/IP, so
        // we do nothing for this method
    }
}
