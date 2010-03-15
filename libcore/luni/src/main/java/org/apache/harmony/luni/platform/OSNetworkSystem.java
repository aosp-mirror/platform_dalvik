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

    private static OSNetworkSystem singleton = new OSNetworkSystem();

    public static OSNetworkSystem getOSNetworkSystem() {
        return singleton;
    }

    private OSNetworkSystem() {
    }

    public native void accept(FileDescriptor fdServer, SocketImpl newSocket,
            FileDescriptor fdnewSocket, int timeout) throws IOException;

    public native void bind(FileDescriptor fd, InetAddress inetAddress, int port) throws SocketException;

    // BEGIN android-changed (removed unused return value and useless native method)
    public void connect(FileDescriptor fd, int trafficClass,
            InetAddress inetAddress, int port) throws IOException{
        connectStreamWithTimeoutSocket(fd, port, 0, trafficClass, inetAddress);
    }
    // END android-changed

    public native void connectDatagram(FileDescriptor fd, int port,
            int trafficClass, InetAddress inetAddress) throws SocketException;

    public native void connectStreamWithTimeoutSocket(FileDescriptor fd,
            int port, int timeout, int trafficClass, InetAddress inetAddress)
            throws IOException;

    // BEGIN android-changed: changed context from Long to byte[]
    public native int connectWithTimeout(FileDescriptor fd, int timeout,
            int trafficClass, InetAddress inetAddress, int port, int step,
            byte[] context) throws IOException;
    // END android-changed

    // TODO: preferIPv4Stack is ignored.
    public native void createDatagramSocket(FileDescriptor fd, boolean preferIPv4Stack)
            throws SocketException;

    // TODO: preferIPv4Stack is ignored.
    public native void createServerStreamSocket(FileDescriptor fd, boolean preferIPv4Stack)
            throws SocketException;

    // TODO: preferIPv4Stack is ignored.
    public native void createStreamSocket(FileDescriptor fd, boolean preferIPv4Stack)
            throws SocketException;

    public native void disconnectDatagram(FileDescriptor fd) throws SocketException;

    // TODO: rewrite callers to skip the middleman.
    public InetAddress getHostByAddr(byte[] ipAddress) throws UnknownHostException {
        // BEGIN android-changed
        // Wallpaper fix for http://b/1851257. This  is a layering violation,
        // but at least the method has the right return type.
        // TODO: Fix the socket code to remove this method altogether.
        return InetAddress.getByAddress(ipAddress);
        // END android-changed
    }

    // TODO: rewrite callers to skip the middleman.
    public InetAddress getHostByName(String hostName) throws UnknownHostException {
        return InetAddress.getByName(hostName);
    }

    public native String byteArrayToIpString(byte[] address)
            throws UnknownHostException;

    public native byte[] ipStringToByteArray(String address)
            throws UnknownHostException;

    public native InetAddress getSocketLocalAddress(FileDescriptor fd);

    public native int getSocketLocalPort(FileDescriptor fd);

    public native Object getSocketOption(FileDescriptor fd, int opt) throws SocketException;

    public Channel inheritedChannel() {
        // Android never has stdin/stdout connected to a socket.
        return null;
    }

    public native void listenStreamSocket(FileDescriptor fd, int backlog) throws SocketException;

    public native int peekDatagram(FileDescriptor fd, InetAddress sender, int receiveTimeout)
            throws IOException;

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

    public native int readDirect(FileDescriptor fd, int address, int count, int timeout)
            throws IOException;

    public native int receiveDatagram(FileDescriptor fd, DatagramPacket packet,
            byte[] data, int offset, int length, int receiveTimeout,
            boolean peek) throws IOException;

    public native int receiveDatagramDirect(FileDescriptor fd,
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
    public native int recvConnectedDatagram(FileDescriptor fd,
            DatagramPacket packet, byte[] data, int offset, int length,
            int receiveTimeout, boolean peek) throws IOException;

    public native int recvConnectedDatagramDirect(FileDescriptor fd,
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

        // BEGIN android-changed: handle errors in native code
        return selectImpl(readFDs, writeFDs, numReadable, numWritable, flags, timeout);
        // END android-changed
    }

    // BEGIN android-changed: return type (we throw in native code, with descriptive errors)
    static native boolean selectImpl(FileDescriptor[] readfd,
            FileDescriptor[] writefd, int cread, int cwirte, int[] flags,
            long timeout);
    // END android-changed

    // TODO: bindToDevice is unused.
    public native int sendConnectedDatagram(FileDescriptor fd, byte[] data,
            int offset, int length, boolean bindToDevice) throws IOException;

    // TODO: bindToDevice is unused.
    public native int sendConnectedDatagramDirect(FileDescriptor fd,
            int address, int offset, int length, boolean bindToDevice)
            throws IOException;

    // TODO: bindToDevice is unused.
    public native int sendDatagram(FileDescriptor fd, byte[] data, int offset,
            int length, int port, boolean bindToDevice, int trafficClass,
            InetAddress inetAddress) throws IOException;

    public native int sendDatagram2(FileDescriptor fd, byte[] data, int offset,
            int length, int port, InetAddress inetAddress) throws IOException;

    // TODO: bindToDevice is unused.
    public native int sendDatagramDirect(FileDescriptor fd, int address,
            int offset, int length, int port, boolean bindToDevice,
            int trafficClass, InetAddress inetAddress) throws IOException;

    public native void sendUrgentData(FileDescriptor fd, byte value);

    public native void setInetAddress(InetAddress sender, byte[] address);

    public native void setNonBlocking(FileDescriptor fd, boolean block) throws IOException;

    public native void setSocketOption(FileDescriptor fd, int opt, Object optVal)
            throws SocketException;

    public native void shutdownInput(FileDescriptor fd) throws IOException;

    public native void shutdownOutput(FileDescriptor fd) throws IOException;

    public native void socketClose(FileDescriptor fd) throws IOException;

    public native boolean supportsUrgentData(FileDescriptor fd);

    public native int write(FileDescriptor fd, byte[] data, int offset, int count)
            throws IOException;

    public native int writeDirect(FileDescriptor fd, int address, int offset, int count)
            throws IOException;
}
