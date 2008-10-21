/* Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.harmony.luni.net;

import java.io.FileDescriptor;
import java.net.DatagramSocketImpl;
import java.net.InetAddress;
import java.net.Proxy;
import java.net.SocketImpl;

public class SocketImplProvider {

    public static SocketImpl getSocketImpl() {
        return new PlainSocketImpl2();
    }

    /**
     * gets a SocketImpl with specified proxy.
     */
    public static SocketImpl getSocketImpl(Proxy proxy) {
        return new PlainSocketImpl2(proxy);
    }

    public static SocketImpl getSocketImpl(FileDescriptor fd, int localport, InetAddress addr,
            int port) {
        return new PlainSocketImpl2(fd, localport, addr, port);
    }

    public static SocketImpl getServerSocketImpl() {
        return new PlainServerSocketImpl();
    }

    public static SocketImpl getServerSocketImpl(FileDescriptor fd) {
        return new PlainServerSocketImpl(fd);
    }

    public static DatagramSocketImpl getDatagramSocketImpl() {
        return new PlainDatagramSocketImpl();
    }

    public static DatagramSocketImpl getMulticastSocketImpl() {
        return new PlainMulticastSocketImpl();
    }

    public static DatagramSocketImpl getDatagramSocketImpl(FileDescriptor fd, int localPort) {
        return new PlainDatagramSocketImpl(fd, localPort);
    }

}
