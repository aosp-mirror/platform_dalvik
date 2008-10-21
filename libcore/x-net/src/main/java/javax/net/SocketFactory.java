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

/**
* @author Boris V. Kuznetsov
* @version $Revision$
*/

package javax.net;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * @com.intel.drl.spec_ref
 * 
 */
public abstract class SocketFactory {

    static SocketFactory defaultFactory;
    
    protected SocketFactory() {
    }

    public static synchronized SocketFactory getDefault() {
        if (defaultFactory == null) {
            defaultFactory = new DefaultSocketFactory();
        }
        return defaultFactory;
    }

    public Socket createSocket() throws IOException {
        // follow RI's behavior 
        throw new SocketException("Unconnected sockets not implemented");
    }

    public abstract Socket createSocket(String host, int port)
            throws IOException, UnknownHostException;

    public abstract Socket createSocket(String host, int port,
            InetAddress localHost, int localPort) throws IOException,
            UnknownHostException;

    public abstract Socket createSocket(InetAddress host, int port)
            throws IOException;

    public abstract Socket createSocket(InetAddress address, int port,
            InetAddress localAddress, int localPort) throws IOException;
}
