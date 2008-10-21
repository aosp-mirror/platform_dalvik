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

package tests.api.javax.net.ssl;

import java.io.IOException;
import java.net.ServerSocket;
import java.security.Principal;
import java.security.cert.Certificate;

import javax.net.ssl.HandshakeCompletedEvent;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import junit.framework.TestCase;


/**
 * Tests for <code>HandshakeCompletedEvent</code> class constructors and methods.
 * 
 */
public class HandshakeCompletedEventTest extends TestCase {

    int port;

    ServerSocket ss;

    SSLSocket soc;

    boolean noFreePort = false;
    boolean noSocket = false;

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        SSLSocketFactory sf = (SSLSocketFactory) SSLSocketFactory.getDefault();
        try {
            ss = new ServerSocket(0);
            port = ss.getLocalPort();
        } catch (Exception e) {
            e.printStackTrace();
            noFreePort = true;
            return;
        }
        try {
            soc = (SSLSocket) sf.createSocket("localhost", port);
        } catch (IOException e) {
            noSocket = true;
        }

    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
        if (ss != null) {
            ss.close();
        }
        if (soc != null) {
            soc.close();
        }
    }

    public final void test_Constructor() {
    	if (noFreePort || noSocket) {
            return;
        }
    	SSLSession ses = new MySSLSession();
        HandshakeCompletedEvent event = new HandshakeCompletedEvent(soc, ses);
        if (!ses.equals(event.getSession())) {
            fail("incorrect session");
        }
        if (!soc.equals(event.getSocket())) {
            fail("incorrect socket");
        }
    }

    public final void test_getCipherSuite() {
        if (noFreePort || noSocket) {
            return;
        }
        SSLSession ses = new MySSLSession();

        HandshakeCompletedEvent event = new HandshakeCompletedEvent(soc, ses);
        String name = event.getCipherSuite();
        String name_ses = ses.getCipherSuite();
        if (name == null && name_ses != null) {
            fail("incorrect null CipherCuite");
        }
        if (!name.equals(name_ses)) {
            fail("incorrect CipherCuite");
        }
    }

    /**
     * @tests javax.net.ssl.HandshakeCompletedEvent#getLocalPrincipal()
     */
    public void test_getLocalPrincipal() {
        if (noFreePort || noSocket) return;
        SSLSession ses = new MySSLSession();
        HandshakeCompletedEvent event = new HandshakeCompletedEvent(soc, ses);
        Principal lp     = event.getLocalPrincipal();
        Principal ses_lp = ses.getLocalPrincipal(); 
        assertEquals("Incorrect value of local principal",
        		lp, ses_lp);
    }
    
    public final void test_getLocalCertificates() {
    	if (noFreePort || noSocket) {
            return;
        }
    	SSLSession ses = new MySSLSession();
        HandshakeCompletedEvent event = new HandshakeCompletedEvent(soc, ses);

        Certificate[] certs = event.getLocalCertificates();
        Certificate[] ses_certs = ses.getLocalCertificates();
        if (certs == null && ses_certs == null) {
            return;
        }
        if (certs == null || ses_certs == null) {
            fail("incorrect LocalCertificates");
        }
        for (int i = 0; i < certs.length; i++) {
            if (certs[i] != ses_certs[i]) {
                fail("incorrect LocalCertificates");
            }
        }
    }

    public final void test_getPeerCertificateChain() {
    	if (noFreePort || noSocket) {
            return;
        }
    	SSLSession ses = new MySSLSession();
        HandshakeCompletedEvent event = new HandshakeCompletedEvent(soc, ses);
        try {
            event.getPeerCertificateChain();
            fail("No excpected SSLPeerUnverifiedException");
        } catch (SSLPeerUnverifiedException e) {
        }
    }

    public final void test_getPeerCertificates() {
    	if (noFreePort || noSocket) {
            return;
        }
    	SSLSession ses = new MySSLSession();
        HandshakeCompletedEvent event = new HandshakeCompletedEvent(soc, ses);
        try {
            event.getPeerCertificates();
            fail("No excpected SSLPeerUnverifiedException");
        } catch (SSLPeerUnverifiedException e) {
        }
    }

    /**
     * @tests javax.net.ssl.HandshakeCompletedEvent#getPeerPrincipal()
     */
    public void test_getPeerPrincipal()
        throws SSLPeerUnverifiedException {
        if (noFreePort || noSocket) return;
        SSLSession ses = new MySSLSession();
        HandshakeCompletedEvent event = new HandshakeCompletedEvent(soc, ses);
        Principal pp     = event.getPeerPrincipal();
        Principal ses_pp = ses.getPeerPrincipal(); 
        assertEquals("Incorrect value of peer principal",
        		pp, ses_pp);
    }

    /**
     * @tests javax.net.ssl.HandshakeCompletedEvent#getSession()
     */
    public void test_getSession() {
        if (noFreePort || noSocket) return;
        SSLSession ses = new MySSLSession();
        HandshakeCompletedEvent event = new HandshakeCompletedEvent(soc, ses);
        SSLSession s = event.getSession();
        assertEquals("Incorrect value of session", s, ses);
        assertNull("Session value is not null",
        		new HandshakeCompletedEvent(soc, null).getSession());
    }

    /**
     * @tests javax.net.ssl.HandshakeCompletedEvent#getSocket()
     */
    public void test_getSocket() {
        if (noFreePort || noSocket) return;
        SSLSession ses = new MySSLSession();
        HandshakeCompletedEvent event = new HandshakeCompletedEvent(soc, ses);
        SSLSocket socket = event.getSocket();
        assertEquals("Incorrect value of socket", socket, soc);
    }
}
