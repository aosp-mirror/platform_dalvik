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
import dalvik.annotation.TestTargets;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetNew;

import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSessionBindingListener;
import javax.net.ssl.SSLSessionBindingEvent;

import java.io.ByteArrayInputStream;
import java.security.cert.Certificate;
import javax.security.cert.X509Certificate;

import junit.framework.TestCase;

import org.apache.harmony.xnet.tests.support.mySSLSession;

/**
 * Tests for SSLSession class
 * 
 */
@TestTargetClass(SSLSession.class) 
public class SSLSessionTest extends TestCase {
    
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
     * @tests javax.net.ssl.SSLSession#getPeerHost()
     * @tests javax.net.ssl.SSLSession#getPeerPort()
     */
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getPeerHost",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getPeerPort",
            args = {}
        )
    })
    public void test_getPeerHost() {
        mySSLSession s = new mySSLSession("localhost", 1080, null);
        try {
            assertEquals(s.getPeerHost(), "localhost");
            assertEquals(s.getPeerPort(), 1080);
        } catch (Exception ex) {
            fail("Unexpected exception " + ex);
        }
    }
    
    /**
     * @tests javax.net.ssl.SSLSession#invalidate()
     * @tests javax.net.ssl.SSLSession#isValid()
     */
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "invalidate",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "isValid",
            args = {}
        )
    })
    public void test_invalidate() {
        mySSLSession s = new mySSLSession("localhost", 1080, null);
        try {
            assertFalse(s.isValid());
            s.invalidate();
            assertTrue(s.isValid());
        } catch (Exception ex) {
            fail("Unexpected exception " + ex);
        }
    }
    
    /**
     * @tests javax.net.ssl.SSLSession#getPeerPrincipal()
     */
    @TestTargetNew(
        level = TestLevel.SUFFICIENT,
        notes = "Exception wasn't implemented in the interface's class",
        method = "getPeerPrincipal",
        args = {}
    )
    public void test_getPeerPrincipal() {
        mySSLSession s = new mySSLSession("localhost", 1080, null);
        try {
            assertNull(s.getPeerPrincipal());
        } catch (Exception ex) {
            fail("Unexpected exception " + ex);
        }
    }
    
    /**
     * @tests javax.net.ssl.SSLSession#getApplicationBufferSize()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getApplicationBufferSize",
        args = {}
    )
    public void test_getApplicationBufferSize() {
        mySSLSession s = new mySSLSession("localhost", 1080, null);
        try {
            assertEquals(s.getApplicationBufferSize(), 1234567);
        } catch (Exception ex) {
            fail("Unexpected exception " + ex);
        }
    }
    
    /**
     * @tests javax.net.ssl.SSLSession#getCipherSuite()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getCipherSuite",
        args = {}
    )
    public void test_getCipherSuite() {
        mySSLSession s = new mySSLSession("localhost", 1080, null);
        try {
            assertEquals(s.getCipherSuite(), "SuiteName");
        } catch (Exception ex) {
            fail("Unexpected exception " + ex);
        }
    }
    
    /**
     * @tests javax.net.ssl.SSLSession#getCreationTime()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getCreationTime",
        args = {}
    )
    public void test_getCreationTime() {
        mySSLSession s = new mySSLSession("localhost", 1080, null);
        try {
            assertEquals(s.getCreationTime(), 1000l);
        } catch (Exception ex) {
            fail("Unexpected exception " + ex);
        }
    }
    
    /**
     * @tests javax.net.ssl.SSLSession#getId()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getId",
        args = {}
    )
    public void test_getId() {
        byte[] data = {5, 10, 15};
        mySSLSession s = new mySSLSession("localhost", 1080, data);
        try {
            assertEquals(s.getId(), data);
        } catch (Exception ex) {
            fail("Unexpected exception " + ex);
        }
    }
    
    /**
     * @tests javax.net.ssl.SSLSession#getLastAccessedTime()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getLastAccessedTime",
        args = {}
    )
    public void test_getLastAccessedTime() {
        mySSLSession s = new mySSLSession("localhost", 1080, null);
        try {
            assertEquals(s.getLastAccessedTime(), 2000l);
        } catch (Exception ex) {
            fail("Unexpected exception " + ex);
        }
    }
    
    /**
     * @tests javax.net.ssl.SSLSession#getLocalCertificates()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getLocalCertificates",
        args = {}
    )
    public void test_getLocalCertificates() {
        mySSLSession s = new mySSLSession("localhost", 1080, null);
        try {
            assertNull(s.getLocalCertificates());
        } catch (Exception ex) {
            fail("Unexpected exception " + ex);
        }
    }
    
    /**
     * @tests javax.net.ssl.SSLSession#getLocalPrincipal()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getLocalPrincipal",
        args = {}
    )
    public void test_getLocalPrincipal() {
        mySSLSession s = new mySSLSession("localhost", 1080, null);
        try {
            assertNull(s.getLocalPrincipal());
        } catch (Exception ex) {
            fail("Unexpected exception " + ex);
        }
    }
    
    /**
     * @tests javax.net.ssl.SSLSession#getPacketBufferSize()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getPacketBufferSize",
        args = {}
    )
    public void test_getPacketBufferSize() {
        mySSLSession s = new mySSLSession("localhost", 1080, null);
        try {
            assertEquals(s.getPacketBufferSize(), 12345);
        } catch (Exception ex) {
            fail("Unexpected exception " + ex);
        }
    }
    
    /**
     * @tests javax.net.ssl.SSLSession#getPeerCertificates()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getPeerCertificates",
        args = {}
    )
    public void test_getPeerCertificates() {
        mySSLSession s = new mySSLSession("localhost", 1080, null);
        try {
            Certificate[] res = s.getPeerCertificates();
            fail("SSLPeerUnverifiedException wasn't thrown");
        } catch (SSLPeerUnverifiedException pue) {
            //expected
        }
        s = new mySSLSession(null);
        try {
            Certificate[] res = s.getPeerCertificates();
            assertEquals(res.length, 3);
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }
    }
    
    /**
     * @tests javax.net.ssl.SSLSession#getPeerCertificateChain()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getPeerCertificateChain",
        args = {}
    )
    public void test_getPeerCertificateChain() {
        ByteArrayInputStream bis = new ByteArrayInputStream(certificate.getBytes());
        mySSLSession s = new mySSLSession("localhost", 1080, null);
        try {
            X509Certificate[] resN = s.getPeerCertificateChain();
            fail("SSLPeerUnverifiedException wasn't thrown");
        } catch (SSLPeerUnverifiedException pue) {
            //expected
        }
        try {
            X509Certificate xc = X509Certificate.getInstance(bis);
            X509Certificate[] xcs = {xc};
            s = new mySSLSession(xcs);
        } catch (Exception e) {
            fail(e + " was thrown for configuration");
        }
        try {
            X509Certificate[] res = s.getPeerCertificateChain();
            assertEquals(res.length, 1);
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }
    }
    
    /**
     * @tests javax.net.ssl.SSLSession#getProtocol()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getProtocol",
        args = {}
    )
    public void test_getProtocol() {
        mySSLSession s = new mySSLSession("localhost", 1080, null);
        try {
            assertEquals(s.getProtocol(), "ProtocolName");
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }
    }
    
    /**
     * @tests javax.net.ssl.SSLSession#getSessionContext()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getSessionContext",
        args = {}
    )
    public void test_getSessionContext() {
        mySSLSession s = new mySSLSession("localhost", 1080, null);
        try {
            assertNull(s.getSessionContext());
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }
    }
    
    /**
     * @tests javax.net.ssl.SSLSession#putValue(String name, Object value)
     * @tests javax.net.ssl.SSLSession#removeValue(String name)
     * @tests javax.net.ssl.SSLSession#getValueNames()
     */
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "putValue",
            args = {String.class, Object.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "removeValue",
            args = {String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getValueNames",
            args = {}
        )
    })
    public void test_putValue() {
        mySSLSession s = new mySSLSession("localhost", 1080, null);
        mySSLSessionBindingListener sbl = new mySSLSessionBindingListener();
        try {
            assertNotNull(s.getValueNames());
            assertEquals(s.getValueNames().length, 0);
            s.putValue("Name_01", sbl);
            s.putValue("Name_02", sbl);
            s.putValue("Name_03", sbl);
            assertEquals(s.getValueNames().length, 3);
            s.removeValue("Name_01");
            assertEquals(s.getValueNames().length, 2);
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }
        
        try {
            s.putValue(null, null);
            fail("IllegalArgumentException wasn't thrown");
        } catch (IllegalArgumentException iae) {
            //expected
        }
        try {
            s.putValue("ABC", null);
            fail("IllegalArgumentException wasn't thrown");
        } catch (IllegalArgumentException iae) {
            //expected
        }
        try {
            s.putValue(null, sbl);
            fail("IllegalArgumentException wasn't thrown");
        } catch (IllegalArgumentException iae) {
            //expected
        }
        
        try {
            s.removeValue(null);
            fail("IllegalArgumentException wasn't thrown");
        } catch (IllegalArgumentException iae) {
            //expected
        }
    }
    
    /**
     * @tests javax.net.ssl.SSLSession#getValue(String name)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getValue",
        args = {String.class}
    )
    public void test_getValue() {
        mySSLSession s = new mySSLSession("localhost", 1080, null);
        mySSLSessionBindingListener sbl = new mySSLSessionBindingListener();
        
        try {
            s.getValue(null);
            fail("IllegalArgumentException wasn't thrown");
        } catch (IllegalArgumentException iae) {
            //expected
        }
        
        try {
            s.putValue("Name", sbl);
            Object obj = s.getValue("Name");
            assertTrue(obj instanceof SSLSessionBindingListener);
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }
    }
    
    public class mySSLSessionBindingListener implements SSLSessionBindingListener {
        mySSLSessionBindingListener() {
        }
        public void valueBound(SSLSessionBindingEvent event) {}
        public void valueUnbound(SSLSessionBindingEvent event) {}
    }


}
