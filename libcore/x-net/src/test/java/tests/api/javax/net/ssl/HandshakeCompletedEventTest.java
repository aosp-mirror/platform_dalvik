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

import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetNew;

import java.io.ByteArrayInputStream;
import java.security.cert.Certificate;

import javax.net.ssl.HandshakeCompletedEvent;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.security.cert.X509Certificate;

import junit.framework.TestCase;

import org.apache.harmony.xnet.tests.support.mySSLSession;
import org.apache.harmony.xnet.tests.support.mySSLSocket;


/**
 * Tests for <code>HandshakeCompletedEvent</code> class constructors and methods.
 * 
 */
@TestTargetClass(HandshakeCompletedEvent.class) 
public class HandshakeCompletedEventTest extends TestCase {
    
    String certificate = "-----BEGIN CERTIFICATE-----\n"
        + "MIICZTCCAdICBQL3AAC2MA0GCSqGSIb3DQEBAgUAMF8xCzAJBgNVBAYTAlVTMSAw\n"
        + "HgYDVQQKExdSU0EgRGF0YSBTZWN1cml0eSwgSW5jLjEuMCwGA1UECxMlU2VjdXJl\n"
        + "IFNlcnZlciBDZXJ0aWZpY2F0aW9uIEF1dGhvcml0eTAeFw05NzAyMjAwMDAwMDBa\n"
        + "Fw05ODAyMjAyMzU5NTlaMIGWMQswCQYDVQQGEwJVUzETMBEGA1UECBMKQ2FsaWZv\n"
        + "cm5pYTESMBAGA1UEBxMJUGFsbyBBbHRvMR8wHQYDVQQKExZTdW4gTWljcm9zeXN0\n"
        + "ZW1zLCBJbmMuMSEwHwYDVQQLExhUZXN0IGFuZCBFdmFsdWF0aW9uIE9ubHkxGjAY\n"
        + "BgNVBAMTEWFyZ29uLmVuZy5zdW4uY29tMIGfMA0GCSqGSIb3DQEBAQUAA4GNADCB\n"
        + "iQKBgQCofmdY+PiUWN01FOzEewf+GaG+lFf132UpzATmYJkA4AEA/juW7jSi+LJk\n"
        + "wJKi5GO4RyZoyimAL/5yIWDV6l1KlvxyKslr0REhMBaD/3Z3EsLTTEf5gVrQS6sT\n"
        + "WMoSZAyzB39kFfsB6oUXNtV8+UKKxSxKbxvhQn267PeCz5VX2QIDAQABMA0GCSqG\n"
        + "SIb3DQEBAgUAA34AXl3at6luiV/7I9MN5CXYoPJYI8Bcdc1hBagJvTMcmlqL2uOZ\n"
        + "H9T5hNMEL9Tk6aI7yZPXcw/xI2K6pOR/FrMp0UwJmdxX7ljV6ZtUZf7pY492UqwC\n"
        + "1777XQ9UEZyrKJvF5ntleeO0ayBqLGVKCWzWZX9YsXCpv47FNLZbupE=\n"
        + "-----END CERTIFICATE-----\n";


    /**
     * @tests javax.net.ssl.HandshakeCompletedEvent#HandshakeCompletedEvent(SSLSocket sock, SSLSession s) 
     */
    @TestTargetNew(
        level = TestLevel.SUFFICIENT,
        notes = "Exceptions for null/incorrect parameters are not provided",
        method = "HandshakeCompletedEvent",
        args = {javax.net.ssl.SSLSocket.class, javax.net.ssl.SSLSession.class}
    )
    public final void test_Constructor() {
        mySSLSession session = new mySSLSession("localhost", 1080, null);
        mySSLSocket socket = new mySSLSocket();
        try {
            HandshakeCompletedEvent event = new HandshakeCompletedEvent(socket, session);
            assertNotNull(event);
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }
        try {
            HandshakeCompletedEvent event = new HandshakeCompletedEvent(null, null);
            fail("Any exception wasn't thrown for null parameters");
        } catch (Exception e) {
            //expected
        }
    }
    
    /**
     * @tests javax.net.ssl.HandshakeCompletedEvent#getCipherSuite() 
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getCipherSuite",
        args = {}
    )
    public final void test_getCipherSuite() {
        mySSLSession session = new mySSLSession("localhost", 1080, null);
        mySSLSocket socket = new mySSLSocket();
        HandshakeCompletedEvent event = new HandshakeCompletedEvent(socket, session);
        try {
            String name = event.getCipherSuite();
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }
    }
    
    /**
     * @tests javax.net.ssl.HandshakeCompletedEvent#getLocalCertificates() 
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getLocalCertificates",
        args = {}
    )
    public final void test_getLocalCertificates() {
        mySSLSession session = new mySSLSession("localhost", 1080, null);
        mySSLSocket socket = new mySSLSocket();
        HandshakeCompletedEvent event = new HandshakeCompletedEvent(socket, session);
        try {
            assertNull(event.getLocalCertificates());
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }
    }
    
    /**
     * @tests javax.net.ssl.HandshakeCompletedEvent#getLocalPrincipal() 
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getLocalPrincipal",
        args = {}
    )
    public final void test_getLocalPrincipal() {
        mySSLSession session = new mySSLSession("localhost", 1080, null);
        mySSLSocket socket = new mySSLSocket();
        HandshakeCompletedEvent event = new HandshakeCompletedEvent(socket, session);
        try {
            assertNull(event.getLocalPrincipal());
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }
    }
    
    /**
     * @tests javax.net.ssl.HandshakeCompletedEvent#getPeerCertificateChain() 
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getPeerCertificateChain",
        args = {}
    )
    public final void test_getPeerCertificateChain() {
        ByteArrayInputStream bis = new ByteArrayInputStream(certificate.getBytes());
        mySSLSession session = new mySSLSession(null);
        mySSLSocket socket = new mySSLSocket();
        HandshakeCompletedEvent event = new HandshakeCompletedEvent(socket, session);
        try {
            X509Certificate[] res = event.getPeerCertificateChain();
            fail("SSLPeerUnverifiedException wasn't thrown");
        } catch (SSLPeerUnverifiedException spue) {
            //expected
        }
        
        try {
            X509Certificate xc = X509Certificate.getInstance(bis);
            X509Certificate[] xcs = {xc};
            session = new mySSLSession(xcs);
            event = new HandshakeCompletedEvent(socket, session);
        } catch (Exception e) {
            fail(e + " was thrown for configuration");
        }
        try {
            X509Certificate[] res = event.getPeerCertificateChain();
            assertEquals(res.length, 1);
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }
    }
    
    /**
     * @tests javax.net.ssl.HandshakeCompletedEvent#getPeerCertificates() 
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getPeerCertificates",
        args = {}
    )
    public final void test_getPeerCertificates() {
        mySSLSession session = new mySSLSession("localhost", 1080, null);
        mySSLSocket socket = new mySSLSocket();
        HandshakeCompletedEvent event = new HandshakeCompletedEvent(socket, session);
        try {
            Certificate[] res = event.getPeerCertificates();
            fail("SSLPeerUnverifiedException wasn't thrown");
        } catch (SSLPeerUnverifiedException spue) {
            //expected
        }
        
        session = new mySSLSession(null);
        event = new HandshakeCompletedEvent(socket, session);
        try {
            Certificate[] res = event.getPeerCertificates();
            assertEquals(res.length, 3);
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }
    }
    
    /**
     * @tests javax.net.ssl.HandshakeCompletedEvent#getPeerPrincipal() 
     */
    @TestTargetNew(
        level = TestLevel.SUFFICIENT,
        notes = "",
        method = "getPeerPrincipal",
        args = {}
    )
    public final void test_getPeerPrincipal() {
        mySSLSession session = new mySSLSession("localhost", 1080, null);
        mySSLSocket socket = new mySSLSocket();
        HandshakeCompletedEvent event = new HandshakeCompletedEvent(socket, session);
        try {
            assertNull(event.getPeerPrincipal());
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }
    }
    
    /**
     * @tests javax.net.ssl.HandshakeCompletedEvent#getSession() 
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getSession",
        args = {}
    )
    public final void test_getSession() {
        mySSLSession session = new mySSLSession("localhost", 1080, null);
        mySSLSocket socket = new mySSLSocket();
        HandshakeCompletedEvent event = new HandshakeCompletedEvent(socket, session);
        try {
            SSLSession ss = event.getSession();
            assertNotNull(ss);
            assertEquals(session, ss);
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }
    }
    
    /**
     * @tests javax.net.ssl.HandshakeCompletedEvent#getSocket() 
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getSocket",
        args = {}
    )
    public final void test_getSocket() {
        mySSLSession session = new mySSLSession("localhost", 1080, null);
        mySSLSocket socket = new mySSLSocket();
        HandshakeCompletedEvent event = new HandshakeCompletedEvent(socket, session);
        try {
            SSLSocket ss = event.getSocket();
            assertNotNull(ss);
            assertEquals(socket, ss);
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }
    }
}
