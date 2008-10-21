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

import org.apache.harmony.luni.util.Msg;

/**
 * This class models a datagram packet to be sent or received. The
 * DatagramPacket(byte[], int, InetAddress, int) constructor is used for packets
 * to be sent, while the DatagramPacket(byte[], int) constructor is used for
 * received packets.
 * 
 * @see DatagramSocket
 */
public final class DatagramPacket {

    byte[] data;

    int length;

    InetAddress address;

    int port = -1; // The default port number is -1

    int offset = 0;

    /**
     * Constructs a new <code>DatagramPacket</code> suitable for receiving
     * datagram packets of length up to <code>length</code>.
     * 
     * @param data
     *            byte array to store the read characters
     * @param length
     *            length of the data buffer
     */
    public DatagramPacket(byte[] data, int length) {
        this(data, 0, length);
    }

    /**
     * Constructs a new <code>DatagramPacket</code> suitable for receiving
     * datagram packets of length up to <code>length</code>, with an offset
     * into the buffer <code>offset</code>.
     * 
     * @param data
     *            byte array to store the read characters
     * @param offset
     *            the offset into the byte array
     * @param length
     *            length of the data buffer
     */
    public DatagramPacket(byte[] data, int offset, int length) {
        super();
        setData(data, offset, length);
    }

    /**
     * Constructs a new <code>DatagramPacket</code> suitable for sending
     * packets to the nominated host/port. The <code>length</code> must be
     * less than or equal to the size of <code>data</code>.
     * 
     * @param data
     *            byte array to store the read characters
     * @param offset
     *            the offset in to read/write from
     * @param length
     *            length of the data buffer
     * @param host
     *            address of the target host
     * @param aPort
     *            target host port
     */

    public DatagramPacket(byte[] data, int offset, int length,
            InetAddress host, int aPort) {
        this(data, offset, length);
        setPort(aPort);
        address = host;
    }

    /**
     * Constructs a new <code>DatagramPacket</code> suitable for sending
     * packets to the nominated host/port. The <code>length</code> must be
     * less than or equal to the size of <code>data</code>.
     * 
     * @param data
     *            byte array to store the read characters
     * @param length
     *            length of the data buffer
     * @param host
     *            address of the target host
     * @param port
     *            target host port
     */
    public DatagramPacket(byte[] data, int length, InetAddress host, int port) {
        this(data, 0, length, host, port);
    }

    /**
     * Answer the IP address of the machine that is the target or sender of this
     * datagram.
     * 
     * @return InetAddress the target host address
     */
    public synchronized InetAddress getAddress() {
        return address;
    }

    /**
     * Answer the data sent or received in this datagram.
     * 
     * @return byte[] the data sent/received
     */
    public synchronized byte[] getData() {
        return data;
    }

    /**
     * Answer the length of the data sent or received in this datagram.
     * 
     * @return int the length of the sent/received data
     */
    public synchronized int getLength() {
        return length;
    }

    /**
     * Answer the offset of the data sent or received in this datagram buffer.
     * 
     * @return int the offset of the start of the sent/received data
     */
    public synchronized int getOffset() {
        return offset;
    }

    /**
     * Answer the port number of the target or sender machine of this datagram.
     * 
     * @return int for received packets, the sender address and for sent
     *         packets, the target host
     */
    public synchronized int getPort() {
        return port;
    }

    /**
     * Set the IP address of the machine that is the target of this datagram.
     * 
     * @param addr
     *            the target host address
     */
    public synchronized void setAddress(InetAddress addr) {
        address = addr;
    }

    /**
     * Set the data buffer for this datagram.
     * 
     * @param buf
     *            the data to be sent
     * @param anOffset
     *            the offset into the data
     * @param aLength
     *            the length of the data to be sent
     */
    public synchronized void setData(byte[] buf, int anOffset, int aLength) {
        if (0 > anOffset || anOffset > buf.length || 0 > aLength
                || aLength > buf.length - anOffset) {
            throw new IllegalArgumentException(Msg.getString("K002f")); //$NON-NLS-1$
        }
        data = buf;
        offset = anOffset;
        length = aLength;
    }

    /**
     * Set the data sent in this datagram.
     * 
     * @param buf
     *            the data to be sent
     */
    public synchronized void setData(byte[] buf) {
        length = buf.length; // This will check for null
        data = buf;
        offset = 0;
    }

    /**
     * Set the length of the data sent in this datagram.
     * 
     * @param len
     *            the length of the data to be sent
     */
    public synchronized void setLength(int len) {
        if (0 > len || offset + len > data.length) {
            throw new IllegalArgumentException(Msg.getString("K002f")); //$NON-NLS-1$
        }
        length = len;
    }

    /**
     * Set the port number of the target machine of this datagram.
     * 
     * @param aPort
     *            the target host port
     */
    public synchronized void setPort(int aPort) {
        if (aPort < 0 || aPort > 65535) {
            throw new IllegalArgumentException(Msg.getString("K0325", aPort)); //$NON-NLS-1$
        }
        port = aPort;
    }

    /**
     * Constructs a new <code>DatagramPacket</code> suitable for sending
     * packets to the nominated host/port. The <code>length</code> must be
     * less than or equal to the size of <code>data</code>.
     * 
     * @param data
     *            byte array to store the read characters
     * @param length
     *            length of the data buffer
     * @param sockAddr
     *            the machine address and port
     */
    public DatagramPacket(byte[] data, int length, SocketAddress sockAddr)
            throws SocketException {
        this(data, 0, length);
        setSocketAddress(sockAddr);
    }

    /**
     * Constructs a new <code>DatagramPacket</code> suitable for sending
     * packets to the nominated host/port. The <code>length</code> must be
     * less than or equal to the size of <code>data</code>.
     * 
     * @param data
     *            byte array to store the read characters
     * @param offset
     *            the offset in to read/write from
     * @param length
     *            length of the data buffer
     * @param sockAddr
     *            the machine address and port
     */
    public DatagramPacket(byte[] data, int offset, int length,
            SocketAddress sockAddr) throws SocketException {
        this(data, offset, length);
        setSocketAddress(sockAddr);
    }

    /**
     * Answer the SocketAddress for this packet.
     */
    public synchronized SocketAddress getSocketAddress() {
        return new InetSocketAddress(getAddress(), getPort());
    }

    /**
     * Set the SocketAddress for this packet.
     * 
     * @param sockAddr
     *            the machine address and port
     */
    public synchronized void setSocketAddress(SocketAddress sockAddr) {
        if (!(sockAddr instanceof InetSocketAddress)) {
            throw new IllegalArgumentException(Msg.getString(
                    "K0316", sockAddr == null ? null : sockAddr.getClass())); //$NON-NLS-1$
        }
        InetSocketAddress inetAddr = (InetSocketAddress) sockAddr;
        port = inetAddr.getPort();
        address = inetAddr.getAddress();
    }
}
