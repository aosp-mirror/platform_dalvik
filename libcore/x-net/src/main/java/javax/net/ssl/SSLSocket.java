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

package javax.net.ssl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * @com.intel.drl.spec_ref
 * 
 */
public abstract class SSLSocket extends Socket {
    protected SSLSocket() {
        super();
    }

    protected SSLSocket(String host, int port) throws IOException,
            UnknownHostException {
        super(host, port);
    }

    protected SSLSocket(InetAddress address, int port) throws IOException {
        super(address, port);
    }

    protected SSLSocket(String host, int port, InetAddress clientAddress,
            int clientPort) throws IOException, UnknownHostException {
        super(host, port, clientAddress, clientPort);
    }

    protected SSLSocket(InetAddress address, int port,
            InetAddress clientAddress, int clientPort) throws IOException {
        super(address, port, clientAddress, clientPort);
    }
    
    public abstract String[] getSupportedCipherSuites();
    public abstract String[] getEnabledCipherSuites();
    public abstract void setEnabledCipherSuites(String[] suites);
    public abstract String[] getSupportedProtocols();
    public abstract String[] getEnabledProtocols();
    public abstract void setEnabledProtocols(String[] protocols);
    public abstract SSLSession getSession();
    public abstract void addHandshakeCompletedListener(HandshakeCompletedListener listener);
    public abstract void removeHandshakeCompletedListener(HandshakeCompletedListener listener);
    public abstract void startHandshake() throws IOException;
    public abstract void setUseClientMode(boolean mode);
    public abstract boolean getUseClientMode();
    public abstract void setNeedClientAuth(boolean need);
    public abstract boolean getNeedClientAuth();
    public abstract void setWantClientAuth(boolean want);
    public abstract boolean getWantClientAuth();
    public abstract void setEnableSessionCreation(boolean flag);
    public abstract boolean getEnableSessionCreation();
    
}