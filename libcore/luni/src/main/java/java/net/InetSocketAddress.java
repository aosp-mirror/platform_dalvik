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
import java.io.ObjectInputStream;

public class InetSocketAddress extends SocketAddress {

    private static final long serialVersionUID = 5076001401234631237L;

    private String hostname;

    private InetAddress addr;

    private int port;

    public InetSocketAddress(int port) {
        this((InetAddress) null, port);
    }

    public InetSocketAddress(InetAddress address, int port) {
        if (port < 0 || port > 65535) {
            throw new IllegalArgumentException();
        }
        if (address == null) {
            addr = InetAddress.ANY;
        } else {
            addr = address;
        }
        hostname = addr.getHostName();
        this.port = port;
    }

    public InetSocketAddress(String host, int port) {
        this(host, port, true);
    }

    /*
     * Internal contructor for InetSocketAddress(String, int) and
     * createUnresolved(String, int);
     */
    InetSocketAddress(String host, int port, boolean needResolved) {
        if (host == null || port < 0 || port > 65535) {
            throw new IllegalArgumentException();
        }
        hostname = host;
        this.port = port;
        if (needResolved) {
            try {
                addr = InetAddress.getByName(hostname);
                hostname = null;
            } catch (UnknownHostException e) {
                // Ignored
            }
        } else {
            addr = null;
        }
    }

    /**
     * Creats an <code>InetSocketAddress</code> without trying to resolve
     * hostname into an InetAddress. The address field is marked as unresolved.
     * 
     * @param host
     * @param port
     * @return an <code>InetSocketAddress</code> instance.
     * @throws IllegalArgumentException
     *             if host is null or the port is not in the range between 0 and
     *             65535.
     */
    public static InetSocketAddress createUnresolved(String host, int port) {
        return new InetSocketAddress(host, port, false);
    }

    public final int getPort() {
        return port;
    }

    public final InetAddress getAddress() {
        return addr;
    }

    public final String getHostName() {
        return (null != addr) ? addr.getHostName() : hostname;
    }

    public final boolean isUnresolved() {
        return addr == null;
    }

    @Override
    public String toString() {
        String host;
        if (addr != null) {
            host = addr.toString();
        } else {
            host = hostname;
        }
        return host + ":" + port; //$NON-NLS-1$
    }

    @Override
    public final boolean equals(Object socketAddr) {
        if (this == socketAddr) {
            return true;
        }
        if (!(socketAddr instanceof InetSocketAddress)) {
            return false;
        }
        InetSocketAddress iSockAddr = (InetSocketAddress) socketAddr;

        // check the ports as we always need to do this
        if (port != iSockAddr.port) {
            return false;
        }

        // we only use the hostnames in the comparison if the addrs were not
        // resolved
        if ((addr == null) && (iSockAddr.addr == null)) {
            return hostname.equals(iSockAddr.hostname);
        }

        // addrs were resolved so use them for the comparison
        if (addr == null) {
            // if we are here we know iSockAddr is not null so just return
            // false
            return false;
        }
        return addr.equals(iSockAddr.addr);
    }

    @Override
    public final int hashCode() {
        if (addr == null) {
            return hostname.hashCode() + port;
        }
        return addr.hashCode() + port;
    }

    private void readObject(ObjectInputStream stream) throws IOException,
            ClassNotFoundException {
        stream.defaultReadObject();
    }
}
