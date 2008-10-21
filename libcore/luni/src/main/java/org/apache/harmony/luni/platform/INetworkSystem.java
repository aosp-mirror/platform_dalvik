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

package org.apache.harmony.luni.platform;

import java.io.FileDescriptor;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketImpl;
import java.net.UnknownHostException;
import java.nio.channels.Channel;
import java.nio.channels.SelectableChannel;

/*
 * The interface for network methods.
 */
public interface INetworkSystem {

    // -----------------------------------------------
    // Class Const
    // -----------------------------------------------

    /*
     * Socket connect Step start
     */
    public final int SOCKET_CONNECT_STEP_START = 0;

    /*
     * Socket connect Step check
     */
    public final int SOCKET_CONNECT_STEP_CHECK = 1;

    // -----------------------------------------------
    // Methods
    // -----------------------------------------------

    /*
     * socket accept
     */
    public void accept(FileDescriptor fdServer, SocketImpl newSocket,
            FileDescriptor fdnewSocket, int timeout) throws IOException;

    public void bind(FileDescriptor aFD, int port, InetAddress inetAddress)
            throws SocketException;

    public boolean bind2(FileDescriptor aFD, int port, boolean bindToDevice,
            InetAddress inetAddress) throws SocketException;

    public void createSocket(FileDescriptor fd, boolean preferIPv4Stack)
            throws IOException;

    public int read(FileDescriptor aFD, byte[] data, int offset, int count,
            int timeout) throws IOException;
    
    public int readDirect(FileDescriptor aFD, int address, int offset, int count,
            int timeout) throws IOException;

    public int write(FileDescriptor fd, byte[] data, int offset, int count)
            throws IOException;
    
    public int writeDirect(FileDescriptor fd, int address, int offset, int count)
            throws IOException;

    public void setNonBlocking(FileDescriptor aFD, boolean block)
            throws IOException;

    public int connect(FileDescriptor aFD, int trafficClass,
            InetAddress inetAddress, int port) throws IOException;

    // BEGIN android-changed
    public int connectWithTimeout(FileDescriptor aFD, int timeout,
            int trafficClass, InetAddress hostname, int port, int step,
            byte[] context) throws IOException;
    // END android-changed

    public int sendDatagram(FileDescriptor fd, byte[] data, int offset,
            int length, int port, boolean bindToDevice, int trafficClass,
            InetAddress inetAddress) throws IOException;
    
    public int sendDatagramDirect(FileDescriptor fd, int address, int offset,
            int length, int port, boolean bindToDevice, int trafficClass,
            InetAddress inetAddress) throws IOException;

    public int receiveDatagram(FileDescriptor aFD, DatagramPacket packet,
            byte[] data, int offset, int length, int receiveTimeout,
            boolean peek) throws IOException;
    
    public int receiveDatagramDirect(FileDescriptor aFD, DatagramPacket packet,
            int address, int offset, int length, int receiveTimeout,
            boolean peek) throws IOException;

    public int recvConnectedDatagram(FileDescriptor aFD, DatagramPacket packet,
            byte[] data, int offset, int length, int receiveTimeout,
            boolean peek) throws IOException;
    
    public int recvConnectedDatagramDirect(FileDescriptor aFD,
            DatagramPacket packet, int address, int offset, int length,
            int receiveTimeout, boolean peek) throws IOException;
    
    public int peekDatagram(FileDescriptor aFD, InetAddress sender,
            int receiveTimeout) throws IOException;

    public int sendConnectedDatagram(FileDescriptor fd, byte[] data,
            int offset, int length, boolean bindToDevice) throws IOException;
    
    public int sendConnectedDatagramDirect(FileDescriptor fd, int address,
            int offset, int length, boolean bindToDevice) throws IOException;

    public void disconnectDatagram(FileDescriptor aFD) throws SocketException;

    public void createDatagramSocket(FileDescriptor aFD, boolean preferIPv4Stack)
            throws SocketException;

    public void connectDatagram(FileDescriptor aFD, int port, int trafficClass,
            InetAddress inetAddress) throws SocketException;

    public void createMulticastSocket(FileDescriptor aFD,
            boolean preferIPv4Stack) throws SocketException;

    public void createServerStreamSocket(FileDescriptor aFD,
            boolean preferIPv4Stack) throws SocketException;

    public int receiveStream(FileDescriptor aFD, byte[] data, int offset,
            int count, int timeout) throws IOException;

    public int sendStream(FileDescriptor fd, byte[] data, int offset, int count)
            throws IOException;

    public void shutdownInput(FileDescriptor descriptor) throws IOException;

    public void shutdownOutput(FileDescriptor descriptor) throws IOException;

    public boolean supportsUrgentData(FileDescriptor fd);

    public void sendUrgentData(FileDescriptor fd, byte value);

    public int availableStream(FileDescriptor aFD) throws SocketException;

    // BEGIN android-removed
    // public void acceptStreamSocket(FileDescriptor fdServer,
    //         SocketImpl newSocket, FileDescriptor fdnewSocket, int timeout)
    //         throws IOException;
    // 
    // public void createStreamSocket(FileDescriptor aFD, boolean preferIPv4Stack)
    //         throws SocketException;
    // END android-removed

    public void listenStreamSocket(FileDescriptor aFD, int backlog)
            throws SocketException;

    public void connectStreamWithTimeoutSocket(FileDescriptor aFD, int aport,
            int timeout, int trafficClass, InetAddress inetAddress)
            throws IOException;

    public int sendDatagram2(FileDescriptor fd, byte[] data, int offset,
            int length, int port, InetAddress inetAddress) throws IOException;

    public InetAddress getSocketLocalAddress(FileDescriptor aFD,
            boolean preferIPv6Addresses);

    public int[] select(FileDescriptor[] readFDs,
            FileDescriptor[] writeFDs, long timeout)
            throws SocketException;

    /*
     * Query the IP stack for the local port to which this socket is bound.
     * 
     * @param aFD the socket descriptor @param preferIPv6Addresses address
     * preference for nodes that support both IPv4 and IPv6 @return int the
     * local port to which the socket is bound
     */
    public int getSocketLocalPort(FileDescriptor aFD,
            boolean preferIPv6Addresses);

    /*
     * Query the IP stack for the nominated socket option.
     * 
     * @param aFD the socket descriptor @param opt the socket option type
     * @return the nominated socket option value
     * 
     * @throws SocketException if the option is invalid
     */
    public Object getSocketOption(FileDescriptor aFD, int opt)
            throws SocketException;

    /*
     * Set the nominated socket option in the IP stack.
     * 
     * @param aFD the socket descriptor @param opt the option selector @param
     * optVal the nominated option value
     * 
     * @throws SocketException if the option is invalid or cannot be set
     */
    public void setSocketOption(FileDescriptor aFD, int opt, Object optVal)
            throws SocketException;

    public int getSocketFlags();

    /*
     * Close the socket in the IP stack.
     * 
     * @param aFD the socket descriptor
     */
    public void socketClose(FileDescriptor aFD) throws IOException;

    public InetAddress getHostByAddr(byte[] addr) throws UnknownHostException;

    public InetAddress getHostByName(String addr, boolean preferIPv6Addresses)
            throws UnknownHostException;

    public void setInetAddress(InetAddress sender, byte[] address);
    
    // BEGIN android-removed
    // public boolean isReachableByICMP(InetAddress dest,InetAddress source,int ttl,int timeout);
    // END android-removed
    
    public Channel inheritedChannel();
    
    public void oneTimeInitialization(boolean jcl_supports_ipv6);
}
