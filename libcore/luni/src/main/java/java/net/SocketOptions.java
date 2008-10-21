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

// BEGIN android-note
// Added a comment to SO_TIMEOUT, below.
// END android-note

/**
 * Defines the protocol to get & set Socket options.
 */
public interface SocketOptions {

    public static final int SO_LINGER = 128;

    /**
     * Timeout for blocking operation. The argument value is specified
     * in milliseconds.
     */
    public static final int SO_TIMEOUT = 4102;

    public static final int TCP_NODELAY = 1;

    // For 5 and 6 see MulticastSocket

    // For 7 see PlainDatagramSocketImpl
    
    public static final int IP_MULTICAST_IF = 16;

    public static final int SO_BINDADDR = 15;

    public static final int SO_REUSEADDR = 4;

    // 10 not currently used
    
    public static final int SO_SNDBUF = 4097;

    public static final int SO_RCVBUF = 4098;

    // For 13, see DatagramSocket
    
    public static final int SO_KEEPALIVE = 8;

    public static final int IP_TOS = 3;

    public static final int IP_MULTICAST_LOOP = 18;

    public static final int SO_BROADCAST = 32;

    public static final int SO_OOBINLINE = 4099;

    public static final int IP_MULTICAST_IF2 = 31;

    /**
     * Answer the declared socket option.
     * 
     * @return Object the option value
     * @param optID
     *            the option identifier
     * @exception SocketException
     *                thrown if an error occurs getting the option
     */
    public Object getOption(int optID) throws SocketException;

    /**
     * Set the declared socket option to the value.
     * 
     * @param optID
     *            the option identifier
     * @param val
     *            the option value to be set
     * @exception SocketException
     *                thrown if an error occurs setting the option
     */
    public void setOption(int optID, Object val) throws SocketException;
}
