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

import java.io.Serializable;
import java.security.Principal;
import java.security.cert.Certificate;
import javax.security.cert.X509Certificate;
import java.util.EventObject;

/**
 * @com.intel.drl.spec_ref
 * 
 */
public class HandshakeCompletedEvent extends EventObject implements
        Serializable {

    /**
     * @serial
     * The 5.0 spec. doesn't declare this serialVersionUID field
     * In order to be compatible it is explicitly declared here
     */
    private static final long serialVersionUID = 7914963744257769778L;

    private transient SSLSession session;

    public HandshakeCompletedEvent(SSLSocket sock, SSLSession s) {
        super(sock);
        session = s;
    }

    public SSLSession getSession() {
        return session;
    }

    public String getCipherSuite() {
        return session.getCipherSuite();
    }

    public Certificate[] getLocalCertificates() {
        return session.getLocalCertificates();
    }

    public Certificate[] getPeerCertificates()
            throws SSLPeerUnverifiedException {
        return session.getPeerCertificates();
    }

    public X509Certificate[] getPeerCertificateChain()
            throws SSLPeerUnverifiedException {
        return session.getPeerCertificateChain();
    }

    public Principal getPeerPrincipal() throws SSLPeerUnverifiedException {
        return session.getPeerPrincipal();
    }
    
    public Principal getLocalPrincipal() {
        return session.getLocalPrincipal();
    }
    
    public SSLSocket getSocket() {
        return (SSLSocket)this.source;
    }

}