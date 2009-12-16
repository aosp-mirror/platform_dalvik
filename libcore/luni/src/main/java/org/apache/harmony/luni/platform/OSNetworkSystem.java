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

// BEGIN android-note
// Address length was changed from long to int for performance reasons.
// Harmony implements INetworkSystem's methods with native methods; Android
// implements them with Java that call through to native wrappers.
// TODO: change the native code to eliminate the wrappers
// END android-note

package org.apache.harmony.luni.platform;

import java.io.FileDescriptor;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketImpl;
import java.net.UnknownHostException;
import java.nio.channels.Channel;
// BEGIN android-removed
// import java.nio.channels.SelectableChannel;
// END android-removed

/**
 * This wraps native code that implements the INetworkSystem interface.
 */
final class OSNetworkSystem implements INetworkSystem {

    private static final int ERRORCODE_SOCKET_TIMEOUT = -209;
    private static final int ERRORCODE_SOCKET_INTERRUPTED = -208;

    private static final int INETADDR_REACHABLE = 0;

    // private static boolean isNetworkInited = false; android-removed

    private static OSNetworkSystem singleton = new OSNetworkSystem();

    /**
     * Answers the unique instance of the OSNetworkSystem.
     *
     * @return the network system interface instance
     */
    public static OSNetworkSystem getOSNetworkSystem() {
        return singleton;
    }

    // Can not be instantiated.
    private OSNetworkSystem() {
        super();
    }

    public void accept(FileDescriptor fdServer, SocketImpl newSocket,
            FileDescriptor fdnewSocket, int timeout) throws IOException {
        acceptSocketImpl(fdServer, newSocket, fdnewSocket, timeout);
    }

    static native void acceptSocketImpl(FileDescriptor fdServer,
            SocketImpl newSocket, FileDescriptor fdnewSocket, int timeout)
            throws IOException;

    // BEGIN android-removed
    // public void acceptStreamSocket(FileDescriptor fdServer,
    //         SocketImpl newSocket, FileDescriptor fdnewSocket, int timeout)
    //         throws IOException {
    //     acceptStreamSocketImpl(fdServer, newSocket, fdnewSocket, timeout);
    // }

    // static native void acceptStreamSocketImpl(FileDescriptor fdServer,
    //         SocketImpl newSocket, FileDescriptor fdnewSocket, int timeout)
    //         throws IOException;
    // END android-removed

    public int availableStream(FileDescriptor fd) throws SocketException {
        return availableStreamImpl(fd);
    }

    static native int availableStreamImpl(FileDescriptor aFD) throws SocketException;

    /**
     * Associates a local address with a socket.
     *
     * @param fd
     *            the socket descriptor
     * @param port
     *            the port number
     * @param inetAddress
     *            address to bind
     * @throws SocketException
     *             thrown if bind operation fails
     */
    public void bind(FileDescriptor fd, InetAddress inetAddress, int port) throws SocketException {
        socketBindImpl(fd, port, inetAddress);
    }

    static native void socketBindImpl(FileDescriptor aFD, int port, InetAddress inetAddress) throws SocketException;

    // BEGIN android-changed (removed unused return value and useless native method)
    public void connect(FileDescriptor fd, int trafficClass,
            InetAddress inetAddress, int port) throws IOException{
        connectStreamWithTimeoutSocketImpl(fd, port, 0, trafficClass, inetAddress);
    }
    // END android-changed

    public void connectDatagram(FileDescriptor fd, int port,
            int trafficClass, InetAddress inetAddress) throws SocketException {
        connectDatagramImpl2(fd, port, trafficClass, inetAddress);
    }

    static native void connectDatagramImpl2(FileDescriptor aFD, int port,
            int trafficClass, InetAddress inetAddress) throws SocketException;

    public void connectStreamWithTimeoutSocket(FileDescriptor aFD,
            int aport, int timeout, int trafficClass, InetAddress inetAddress)
            throws IOException {
        connectStreamWithTimeoutSocketImpl(aFD, aport, timeout, trafficClass,
                inetAddress);
    }

    static native void connectStreamWithTimeoutSocketImpl(FileDescriptor aFD,
            int aport, int timeout, int trafficClass, InetAddress inetAddress)
            throws IOException;

    // BEGIN android-changed
    // changed context from Long to byte[]
    public int connectWithTimeout(FileDescriptor fd, int timeout,
            int trafficClass, InetAddress inetAddress, int port, int step,
            byte[] context) throws IOException {
        return connectWithTimeoutSocketImpl(fd, timeout, trafficClass,
                inetAddress, port, step, context);
    }

    static native int connectWithTimeoutSocketImpl(FileDescriptor aFD,
            int timeout, int trafficClass, InetAddress hostname, int port, int step,
            byte[] context);
    // END android-changed

    public void createDatagramSocket(FileDescriptor fd,
            boolean preferIPv4Stack) throws SocketException {
        createDatagramSocketImpl(fd, preferIPv4Stack);
    }

    /*
    * Allocate a datagram socket in the IP stack. The socket is associated with
    * the <code>aFD</code>.
    *
    * @param aFD the FileDescriptor to associate with the socket @param
    * preferIPv4Stack IP stack preference if underlying platform is V4/V6
    * @exception SocketException upon an allocation error
    */
    static native void createDatagramSocketImpl(FileDescriptor aFD,
            boolean preferIPv4Stack) throws SocketException;

    public void createServerStreamSocket(FileDescriptor fd,
            boolean preferIPv4Stack) throws SocketException {
        createServerStreamSocketImpl(fd, preferIPv4Stack);
    }

    /*
     * Answer the result of attempting to create a server stream socket in the
     * IP stack. Any special options required for server sockets will be set by
     * this method.
     *
     * @param aFD the socket FileDescriptor @param preferIPv4Stack if use IPV4
     * @exception SocketException if an error occurs while creating the socket
     */
    static native void createServerStreamSocketImpl(FileDescriptor aFD,
            boolean preferIPv4Stack) throws SocketException;

    public void createStreamSocket(FileDescriptor fd,
            boolean preferIPv4Stack) throws SocketException {
        createStreamSocketImpl(fd, preferIPv4Stack);
    }

    static native void createStreamSocketImpl(FileDescriptor aFD,
            boolean preferIPv4Stack) throws SocketException;

    /**
     * Disconnect the socket to a port and address
     *a
     * @param fd
     *            the FileDescriptor associated with the socket
     *
     * @throws SocketException
     *             if the disconnect fails
     */
    public void disconnectDatagram(FileDescriptor fd)
            throws SocketException {
        disconnectDatagramImpl(fd);
    }

    static native void disconnectDatagramImpl(FileDescriptor aFD)
            throws SocketException;

    public InetAddress getHostByAddr(byte[] ipAddress)
            throws UnknownHostException {
        // BEGIN android-changed
        // Wallpaper fix for http://b/1851257. This  is a layering violation,
        // but at least the method has the right return type.
        // TODO: Fix the socket code to remove this method altogether.
        return InetAddress.getByAddress(ipAddress);
        // END android-changed
    }
    // BEGIN android-removed
    // static native InetAddress getHostByAddrImpl(byte[] addr)
    //         throws UnknownHostException;
    // END android-removed

    // BEGIN android-removed
    public InetAddress getHostByName(String hostName,
            boolean preferIPv6Addresses) throws UnknownHostException {
        // BEGIN android-changed
        // Wallpaper fix for http://b/1851257.
        return InetAddress.getByName(hostName);
        // END android-changed
    }

    // BEGIN android-removed
    // static native InetAddress getHostByNameImpl(String addr,
    //         boolean preferIPv6Addresses) throws UnknownHostException;
    // END android-removed

    public int getSocketFlags() {
        return getSocketFlagsImpl();
    }

    public native String byteArrayToIpString(byte[] address)
            throws UnknownHostException;

    public native byte[] ipStringToByteArray(String address)
            throws UnknownHostException;

    static native int getSocketFlagsImpl();

    public InetAddress getSocketLocalAddress(FileDescriptor fd,
            boolean preferIPv6Addresses) {
        return getSocketLocalAddressImpl(fd, preferIPv6Addresses);
    }
    static native InetAddress getSocketLocalAddressImpl(FileDescriptor aFD,
            boolean preferIPv6Addresses);

    /**
     * Query the IP stack for the local port to which this socket is bound.
     *
     * @param aFD
     *            the socket descriptor
     * @param preferIPv6Addresses
     *            address preference for nodes that support both IPv4 and IPv6
     * @return the local port to which the socket is bound
     */
    public int getSocketLocalPort(FileDescriptor aFD,
            boolean preferIPv6Addresses) {
        return getSocketLocalPortImpl(aFD, preferIPv6Addresses);
    }

    static native int getSocketLocalPortImpl(FileDescriptor aFD,
            boolean preferIPv6Addresses);

    /**
     * Query the IP stack for the nominated socket option.
     *
     * @param fd
     *            the socket descriptor
     * @param opt
     *            the socket option type
     * @return the nominated socket option value
     * @throws SocketException
     *             if the option is invalid
     */
    public Object getSocketOption(FileDescriptor fd, int opt)
            throws SocketException {
        return getSocketOptionImpl(fd, opt);
    }

    static native Object getSocketOptionImpl(FileDescriptor aFD, int opt)
            throws SocketException;

    public native Channel inheritedChannel();

    // BEGIN android-removed
    // public boolean isReachableByICMP(final InetAddress dest,
    //         InetAddress source, final int ttl, final int timeout) {
    //     return INETADDR_REACHABLE == isReachableByICMPImpl(dest, source, ttl,
    //     timeout);
    // }

    // native int isReachableByICMPImpl(InetAddress addr,
    //         InetAddress local, int ttl, int timeout);
    // END android-removed

    public void listenStreamSocket(FileDescriptor aFD, int backlog)
            throws SocketException {
        listenStreamSocketImpl(aFD, backlog);
    }

    static native void listenStreamSocketImpl(FileDescriptor aFD, int backlog)
            throws SocketException;

    // BEGIN android-removed: we do this statically, when we start the VM.
    // public void oneTimeInitialization(boolean jcl_supports_ipv6);
    // native void oneTimeInitializationImpl(boolean jcl_supports_ipv6);
    // END android-removed

    /**
     * Peek on the socket, update <code>sender</code> address and answer the
     * sender port.
     *
     * @param fd
     *            the socket FileDescriptor
     * @param sender
     *            an InetAddress, to be updated with the sender's address
     * @param receiveTimeout
     *            the maximum length of time the socket should block, reading
     * @return the sender port
     *
     * @throws IOException
     *             upon an read error or timeout
     */
    public int peekDatagram(FileDescriptor fd, InetAddress sender,
            int receiveTimeout) throws IOException {
        return peekDatagramImpl(fd, sender, receiveTimeout);
    }

    static native int peekDatagramImpl(FileDescriptor aFD,
            InetAddress sender, int receiveTimeout) throws IOException;

    /**
     * Read available bytes from the given file descriptor into a byte array.
     *
     * The read has an optional timeout parameter, which if non-zero is the
     * length of time that the read will wait on a select call to see if any
     * bytes are available for reading. If the timeout expires the method
     * returns zero to indicate no bytes were read.
     *
     * @param fd
     *            the socket file descriptor to read
     * @param data
     *            the byte array in which to store the results
     * @param offset
     *            the offset into the byte array in which to start reading the
     *            results
     * @param count
     *            the maximum number of bytes to read
     * @param timeout
     *            the length of time to wait for the bytes, in milliseconds; or
     *            zero to indicate no timeout applied. When there is no timeout
     *            applied the read may block based upon socket options.
     * @return number of bytes read, or zero if there were no bytes available
     *         before the timeout occurred, or -1 to indicate the socket is
     *         closed
     * @throws IOException
     *             if an underlying socket exception occurred
     */
    public int read(FileDescriptor fd, byte[] data, int offset, int count,
            int timeout) throws IOException {
        // BEGIN android-added safety!
        if (offset < 0 || count < 0 || offset > data.length - count) {
            throw new IllegalArgumentException("data.length=" + data.length + " offset=" + offset +
                    " count=" + count);
        }
        // END android-added
        return readSocketImpl(fd, data, offset, count, timeout);
    }

    static native int readSocketImpl(FileDescriptor aFD, byte[] data,
            int offset, int count, int timeout) throws IOException;

    /**
     * Read available bytes from the given file descriptor into OS memory at a
     * given address.
     *
     * @param fd
     *            the socket file descriptor to read
     * @param address
     *            the address of the memory in which to store the results
     * @param count
     *            the maximum number of bytes to read
     * @param timeout
     *            the length of time to wait for the bytes, in milliseconds
     * @return number of bytes read, or zero if there were no bytes available
     *         before the timeout occurred, or -1 to indicate the socket is
     *         closed
     * @throws IOException
     *             if an underlying socket exception occurred
     */
    public int readDirect(FileDescriptor fd, int address, int count,
            int timeout) throws IOException {
        return readSocketDirectImpl(fd, address, count, timeout);
    }

    static native int readSocketDirectImpl(FileDescriptor aFD, int address, int count,
            int timeout) throws IOException;

    /**
     * Receive data on the socket into the specified buffer. The packet fields
     * <code>data</code> & <code>length</code> are passed in addition to
     * <code>packet</code> to eliminate the JNI field access calls.
     *
     * @param fd
     *            the socket FileDescriptor
     * @param packet
     *            the DatagramPacket to receive into
     * @param data
     *            the data buffer of the packet
     * @param offset
     *            the offset in the data buffer
     * @param length
     *            the length of the data buffer in the packet
     * @param receiveTimeout
     *            the maximum length of time the socket should block, reading
     * @param peek
     *            indicates to peek at the data
     * @return number of data received
     * @throws IOException
     *             upon an read error or timeout
     */
    public int receiveDatagram(FileDescriptor fd, DatagramPacket packet,
            byte[] data, int offset, int length, int receiveTimeout,
            boolean peek) throws IOException {
        return receiveDatagramImpl(fd, packet, data, offset, length,
                receiveTimeout, peek);
    }

    static native int receiveDatagramImpl(FileDescriptor aFD,
            DatagramPacket packet, byte[] data, int offset, int length,
            int receiveTimeout, boolean peek) throws IOException;

    public int receiveDatagramDirect(FileDescriptor fd,
            DatagramPacket packet, int address, int offset, int length,
            int receiveTimeout, boolean peek) throws IOException {
        return receiveDatagramDirectImpl(fd, packet, address, offset, length,
                receiveTimeout, peek);
    }

    static native int receiveDatagramDirectImpl(FileDescriptor aFD,
            DatagramPacket packet, int address, int offset, int length,
            int receiveTimeout, boolean peek) throws IOException;

    /**
     * Receive data on the connected socket into the specified buffer. The
     * packet fields <code>data</code> and <code>length</code> are passed in
     * addition to <code>packet</code> to eliminate the JNI field access calls.
     *
     * @param fd
     *            the socket FileDescriptor
     * @param packet
     *            the DatagramPacket to receive into
     * @param data
     *            the data buffer of the packet
     * @param offset
     *            the offset in the data buffer
     * @param length
     *            the length of the data buffer in the packet
     * @param receiveTimeout
     *            the maximum length of time the socket should block, reading
     * @param peek
     *            indicates to peek at the data
     * @return number of data received
     * @throws IOException
     *             upon an read error or timeout
     */
    public int recvConnectedDatagram(FileDescriptor fd,
            DatagramPacket packet, byte[] data, int offset, int length,
            int receiveTimeout, boolean peek) throws IOException {
        return recvConnectedDatagramImpl(fd, packet, data, offset, length,
                receiveTimeout, peek);
    }

    static native int recvConnectedDatagramImpl(FileDescriptor aFD,
            DatagramPacket packet, byte[] data, int offset, int length,
            int receiveTimeout, boolean peek) throws IOException;

    public int recvConnectedDatagramDirect(FileDescriptor aFD, DatagramPacket packet, int address,
            int offset, int length, int receiveTimeout, boolean peek)
            throws IOException {
        return recvConnectedDatagramDirectImpl(aFD, packet, address, offset, length, receiveTimeout, peek);
    }

    static native int recvConnectedDatagramDirectImpl(FileDescriptor aFD,
            DatagramPacket packet, int address, int offset, int length,
            int receiveTimeout, boolean peek) throws IOException;


    public boolean select(FileDescriptor[] readFDs, FileDescriptor[] writeFDs,
            int numReadable, int numWritable, long timeout, int[] flags)
            throws SocketException {
        if (numReadable < 0 || numWritable < 0) {
            throw new IllegalArgumentException();
        }

        int total = numReadable + numWritable;
        if (total == 0) {
            return true;
        }

        assert validateFDs(readFDs, writeFDs, numReadable, numWritable) : "Invalid file descriptor arrays"; //$NON-NLS-1$

        // BEGIN android-changed: handle errors in native code
        return selectImpl(readFDs, writeFDs, numReadable, numWritable, flags, timeout);
        // END android-changed
    }

    // BEGIN android-changed: return type (we throw in native code, with descriptive errors)
    static native boolean selectImpl(FileDescriptor[] readfd,
            FileDescriptor[] writefd, int cread, int cwirte, int[] flags,
            long timeout);
    // END android-changed

    /**
     * Send the <code>data</code> to the address and port to which the was
     * connected and <code>port</code>.
     *
     * @param fd
     *            the socket FileDescriptor
     * @param data
     *            the data buffer of the packet
     * @param offset
     *            the offset in the data buffer
     * @param length
     *            the length of the data buffer in the packet
     * @param bindToDevice
     *            not used, current kept in case needed as was the case for
     *            sendDatagramImpl
     * @return number of data send
     * @throws IOException
     *             upon an read error or timeout
     */
    public int sendConnectedDatagram(FileDescriptor fd, byte[] data,
            int offset, int length, boolean bindToDevice) throws IOException {
        return sendConnectedDatagramImpl(fd, data, offset, length, bindToDevice);
    }

    static native int sendConnectedDatagramImpl(FileDescriptor fd,
            byte[] data, int offset, int length, boolean bindToDevice)
            throws IOException;

    public int sendConnectedDatagramDirect(FileDescriptor fd,
            int address, int offset, int length, boolean bindToDevice)
            throws IOException {
        return sendConnectedDatagramDirectImpl(fd, address, offset, length, bindToDevice);
    }
    static native int sendConnectedDatagramDirectImpl(FileDescriptor fd,
            int address, int offset, int length, boolean bindToDevice)
            throws IOException;

    /**
     * Send the <code>data</code> to the nominated target <code>address</code>
     * and <code>port</code>. These values are derived from the DatagramPacket
     * to reduce the field calls within JNI.
     *
     * @param fd
     *            the socket FileDescriptor
     * @param data
     *            the data buffer of the packet
     * @param offset
     *            the offset in the data buffer
     * @param length
     *            the length of the data buffer in the packet
     * @param port
     *            the target host port
     * @param bindToDevice
     *            if bind to device
     * @param trafficClass
     *            the traffic class to be used when the datagram is sent
     * @param inetAddress
     *            address to connect to.
     * @return number of data send
     *
     * @throws IOException
     *             upon an read error or timeout
     */
    public int sendDatagram(FileDescriptor fd, byte[] data, int offset,
            int length, int port, boolean bindToDevice, int trafficClass,
            InetAddress inetAddress) throws IOException {
        return sendDatagramImpl(fd, data, offset, length, port, bindToDevice,
                trafficClass, inetAddress);
    }

    static native int sendDatagramImpl(FileDescriptor fd, byte[] data, int offset,
            int length, int port, boolean bindToDevice, int trafficClass,
            InetAddress inetAddress) throws IOException;

    public int sendDatagram2(FileDescriptor fd, byte[] data, int offset,
            int length, int port, InetAddress inetAddress) throws IOException {
        return sendDatagramImpl2(fd, data, offset, length, port, inetAddress);
    }

    static native int sendDatagramImpl2(FileDescriptor fd, byte[] data,
            int offset, int length, int port, InetAddress inetAddress) throws IOException;


    public int sendDatagramDirect(FileDescriptor fd, int address,
            int offset, int length, int port, boolean bindToDevice,
            int trafficClass, InetAddress inetAddress) throws IOException {
        return sendDatagramDirectImpl(fd, address, offset, length, port, bindToDevice,
                trafficClass, inetAddress);
    }

    static native int sendDatagramDirectImpl(FileDescriptor fd, int address,
            int offset, int length, int port, boolean bindToDevice,
            int trafficClass, InetAddress inetAddress) throws IOException;

    public void sendUrgentData(FileDescriptor fd, byte value) {
        sendUrgentDataImpl(fd, value);
    }

    static native void sendUrgentDataImpl(FileDescriptor fd, byte value);

    public void setInetAddress(InetAddress sender, byte[] address) {
        setInetAddressImpl(sender, address);
    }

    native void setInetAddressImpl(InetAddress sender, byte[] address);

    public void setNonBlocking(FileDescriptor fd, boolean block)
            throws IOException {
        setNonBlockingImpl(fd, block);
    }

    static native void setNonBlockingImpl(FileDescriptor aFD, boolean block);

    /**
     * Set the nominated socket option in the IP stack.
     *
     * @param aFD
     *            the socket descriptor @param opt the option selector @param
     *            optVal the nominated option value
     *
     * @throws SocketException
     *             if the option is invalid or cannot be set
     */
    public void setSocketOption(FileDescriptor aFD, int opt,
            Object optVal) throws SocketException {
        setSocketOptionImpl(aFD, opt, optVal);
    }

    static native void setSocketOptionImpl(FileDescriptor aFD, int opt,
            Object optVal) throws SocketException;

    public void shutdownInput(FileDescriptor descriptor) throws IOException {
        shutdownInputImpl(descriptor);
    }

    private native void shutdownInputImpl(FileDescriptor descriptor)
            throws IOException;

    public void shutdownOutput(FileDescriptor fd) throws IOException {
        shutdownOutputImpl(fd);
    }

    private native void shutdownOutputImpl(FileDescriptor descriptor)
            throws IOException;
    /**
     * Close the socket in the IP stack.
     *
     * @param fd
     *            the socket descriptor
     */
    public void socketClose(FileDescriptor fd) throws IOException {
        socketCloseImpl(fd);
    }

    static native void socketCloseImpl(FileDescriptor fD);

    public boolean supportsUrgentData(FileDescriptor fd) {
        return supportsUrgentDataImpl(fd);
    }

    static native boolean supportsUrgentDataImpl(FileDescriptor fd);

    /*
    * Used to check if the file descriptor arrays are valid before passing them
    * into the select native call.
    */
    private boolean validateFDs(FileDescriptor[] readFDs,
            FileDescriptor[] writeFDs) {
        for (FileDescriptor fd : readFDs) {
            // Also checks fd not null
            if (!fd.valid()) {
                return false;
            }
        }
        for (FileDescriptor fd : writeFDs) {
            if (!fd.valid()) {
                return false;
            }
        }
        return true;
    }

    private boolean validateFDs(FileDescriptor[] readFDs,
            FileDescriptor[] writeFDs, int countRead, int countWrite) {
        for (int i = 0; i < countRead; ++i) {
            // Also checks fd not null
            if (!readFDs[i].valid()) {
                return false;
            }
        }
        for (int i = 0; i < countWrite; ++i) {
            if (!writeFDs[i].valid()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Write bytes from a byte array to a socket.
     *
     * @param fd
     *            the socket on which to write the bytes
     * @param data
     *            the array containing the bytes to be written
     * @param offset
     *            the offset in the byte array from which to take the bytes
     * @param count
     *            the maximum number of bytes to be written. Callers are trusted
     *            not to send values of length+count that are larger than
     *            data.length
     * @return the actual number of bytes written, which will be between zero
     *         and count
     * @throws IOException
     *             if there is an underlying socket problem
     */
    public int write(FileDescriptor fd, byte[] data, int offset, int count)
            throws IOException {
        return writeSocketImpl(fd, data, offset, count);
    }

    static native int writeSocketImpl(FileDescriptor fd, byte[] data, int offset,
            int count) throws IOException;


    /**
     * Write bytes from the given address to a socket.
     *
     * @param fd
     *            the socket on which to write the bytes
     * @param address
     *            the start address of the bytes to be written
     * @param count
     *            the maximum number of bytes to be written
     * @return the actual number of bytes written, which will be between zero
     *         and count
     * @throws IOException
     *             if there is an underlying socket problem
     */
    public int writeDirect(FileDescriptor fd, int address, int offset, int count)
            throws IOException {
        return writeSocketDirectImpl(fd, address, offset, count);
    }

    static native int writeSocketDirectImpl(FileDescriptor fd, int address, int offset, int count)
            throws IOException;

    // BEGIN android-removed
    // /**
    //  * Write given buffers to a socket. The given buffers is a Object array, the
    //  * element of array must be direct buffer or a byte array to be written.
    //  *
    //  * @param fd
    //  *            the socket on which to write the bytes
    //  * @param buffers
    //  *            the element of array must be direct buffer or a byte array to
    //  *            be written
    //  * @param offsets
    //  *            the index of the first byte to be write
    //  * @param counts
    //  *            the maximum number of bytes to be written
    //  * @param length
    //  *            the size of buffer array
    //  * @return the actual number of bytes written
    //  * @throws IOException
    //  *             if there is an underlying socket problem
    //  */
    // public native int writev(FileDescriptor fd, Object[] buffers,
    //         int[] offsets, int[] counts, int length) throws IOException;
    // END android-removed
}
