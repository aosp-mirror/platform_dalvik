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
import java.net.URL;
import java.security.Principal;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;

import org.apache.harmony.security.tests.support.cert.TestUtils;

import junit.framework.TestCase;



/**
 * Tests for <code>HttpsURLConnection</code> class constructors and methods.
 * 
 */
@TestTargetClass(HttpsURLConnection.class) 
public class HttpsURLConnectionTest extends TestCase {
    
    /**
     * @tests javax.net.ssl.HttpsURLConnection#HttpsURLConnection(java_net_URL) 
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "HttpsURLConnection",
        args = {java.net.URL.class}
    )
    public final void test_Constructor() {
        try {
            MyHttpsURLConnection huc = new MyHttpsURLConnection(new URL("https://www.fortify.net/"));
        } catch (Exception e) {
            fail("Unexpected exception: " + e.toString());
        }
        try {
            MyHttpsURLConnection huc = new MyHttpsURLConnection(null);
        } catch (Exception e) {
            fail("Unexpected exception " + e.toString());
        }
    }
    
    /**
     * @tests javax.net.ssl.HttpsURLConnection#getCipherSuite() 
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getCipherSuite",
        args = {}
    )
    public final void test_getCipherSuite() {
        try {
            URL url = new URL("https://localhost:55555");
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            try {
                connection.getCipherSuite();
                fail("IllegalStateException wasn't thrown");
            } catch (IllegalStateException ise) {
                //expected
            }
        } catch (Exception e) {
            fail("Unexpected exception " + e + " for exception case");
        }
        
        try {
            HttpsURLConnection con = new MyHttpsURLConnection(new URL("https://www.fortify.net/"));
            assertEquals("CipherSuite", con.getCipherSuite());
        } catch (Exception e) {
            fail("Unexpected exception " + e);
        }
    }
    
    /**
     * @tests javax.net.ssl.HttpsURLConnection#getLocalCertificates() 
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getLocalCertificates",
        args = {}
    )
    public final void test_getLocalCertificates() {
        try {
            URL url = new URL("https://localhost:55555");
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            try {
                connection.getLocalCertificates();
                fail("IllegalStateException wasn't thrown");
            } catch (IllegalStateException ise) {
                //expected
            }
        } catch (Exception e) {
            fail("Unexpected exception " + e + " for exception case");
        }
        
        try {
            HttpsURLConnection con = new MyHttpsURLConnection(new URL("https://www.fortify.net/"), "X.508");
            assertNull(con.getLocalCertificates());
            con = new MyHttpsURLConnection(new URL("https://www.fortify.net/"), "X.509");
            Certificate[] cert = con.getLocalCertificates();
            assertNotNull(cert);
            assertEquals(1, cert.length);
        } catch (Exception e) {
            fail("Unexpected exception " + e);
        }
    }

    /**
     * @tests javax.net.ssl.HttpsURLConnection#getDefaultHostnameVerifier() 
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getDefaultHostnameVerifier",
        args = {}
    )
    public final void test_getDefaultHostnameVerifier() {
        HostnameVerifier verifyer =
            HttpsURLConnection.getDefaultHostnameVerifier();
        assertNotNull("Default hostname verifyer is null", verifyer);
    }
    
    /**
     * @tests javax.net.ssl.HttpsURLConnection#getDefaultSSLSocketFactory() 
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getDefaultSSLSocketFactory",
        args = {}
    )
    public final void test_getDefaultSSLSocketFactory() {
        SSLSocketFactory sf = HttpsURLConnection.getDefaultSSLSocketFactory();
        if (!sf.equals(SSLSocketFactory.getDefault())) {
            fail("incorrect DefaultSSLSocketFactory");
        }
    }
    
    /**
     * @tests javax.net.ssl.HttpsURLConnection#getHostnameVerifier()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getHostnameVerifier",
        args = {}
    )
    public final void test_getHostnameVerifier()
        throws Exception {
        HttpsURLConnection con = new MyHttpsURLConnection(
                new URL("https://www.fortify.net/"));
        HostnameVerifier verifyer = con.getHostnameVerifier();
        assertNotNull("Hostname verifyer is null", verifyer);
        assertEquals("Incorrect value of hostname verirfyer", 
                HttpsURLConnection.getDefaultHostnameVerifier(), verifyer);
    }

    /**
     * @tests javax.net.ssl.HttpsURLConnection#getLocalPrincipal()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getLocalPrincipal",
        args = {}
    )
    public final void test_getLocalPrincipal() {
        try {
            URL url = new URL("https://localhost:55555");
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            try {
                connection.getLocalPrincipal();
                fail("IllegalStateException wasn't thrown");
            } catch (IllegalStateException ise) {
                //expected
            }
        } catch (Exception e) {
            fail("Unexpected exception " + e + " for exception case");
        }
        
        try {
            HttpsURLConnection con = new MyHttpsURLConnection(new URL("https://www.fortify.net/"), "X.508");
            assertNull(con.getLocalPrincipal());
            con = new MyHttpsURLConnection(new URL("https://www.fortify.net/"), "X.509");
            assertNotNull("Local principal is null", con.getLocalPrincipal());
        } catch (Exception e) {
            fail("Unexpected exception " + e);
        }
    }

    /**
     * @tests javax.net.ssl.HttpsURLConnection#getPeerPrincipal()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getPeerPrincipal",
        args = {}
    )
    public final void test_getPeerPrincipal() throws Exception {
        try {
            URL url = new URL("https://localhost:55555");
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            try {
                connection.getPeerPrincipal();
                fail("IllegalStateException wasn't thrown");
            } catch (IllegalStateException ise) {
                //expected
            }
        } catch (Exception e) {
            fail("Unexpected exception " + e + " for exception case");
        }
        HttpsURLConnection con = new MyHttpsURLConnection(new URL("https://www.fortify.net/"), "X.508");
        try {
            Principal p = con.getPeerPrincipal();
            fail("SSLPeerUnverifiedException wasn't thrown");
        } catch (SSLPeerUnverifiedException e) {
            //expected
        }
        
        con = new MyHttpsURLConnection(new URL("https://www.fortify.net/"), "X.509");
        try {
            Principal p = con.getPeerPrincipal();
            assertNotNull(p);
        } catch (Exception e) {
            fail("Unexpected exception " + e);
        }
    }
    
    /**
     * @tests javax.net.ssl.HttpsURLConnection#getServerCertificates()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getServerCertificates",
        args = {}
    )
    public final void test_getServerCertificates() throws Exception {
        try {
            URL url = new URL("https://localhost:55555");
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            try {
                connection.getServerCertificates();
                fail("IllegalStateException wasn't thrown");
            } catch (IllegalStateException ise) {
                //expected
            }
        } catch (Exception e) {
            fail("Unexpected exception " + e + " for exception case");
        }
        
        HttpsURLConnection con = new MyHttpsURLConnection(new URL("https://www.fortify.net/"), "X.508");
        try {
            Certificate[] cert = con.getServerCertificates();
            fail("SSLPeerUnverifiedException wasn't thrown");
        } catch (SSLPeerUnverifiedException e) {
            //expected
        }
        
        con = new MyHttpsURLConnection(new URL("https://www.fortify.net/"), "X.509");
        try {
            Certificate[] cert = con.getServerCertificates();
            assertNotNull(cert);
            assertEquals(1, cert.length);
        } catch (Exception e) {
            fail("Unexpected exception " + e);
        }
    }

    /**
     * @tests javax.net.ssl.HttpsURLConnection#getSSLSocketFactory()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getSSLSocketFactory",
        args = {}
    )
    public final void test_getSSLSocketFactory() {
        HttpsURLConnection con = new MyHttpsURLConnection(null);
        SSLSocketFactory sf = con.getSSLSocketFactory();
        if (!sf.equals(SSLSocketFactory.getDefault())) {
            fail("incorrect DefaultSSLSocketFactory");
        }
    }

    /**
     * @tests javax.net.ssl.HttpsURLConnection#setDefaultHostnameVerifier()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setDefaultHostnameVerifier",
        args = {javax.net.ssl.HostnameVerifier.class}
    )
    public final void test_setDefaultHostnameVerifier() {
        try {
            HttpsURLConnection.setDefaultHostnameVerifier(null);
            fail("No expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
        try {
            myHostnameVerifier hnv = new myHostnameVerifier();
            HttpsURLConnection.setDefaultHostnameVerifier(hnv);
        } catch (Exception e) {
            fail("Unexpected exception " + e);
        }
    }

    /**
     * @tests javax.net.ssl.HttpsURLConnection#setHostnameVerifier()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setHostnameVerifier",
        args = {javax.net.ssl.HostnameVerifier.class}
    )
    public final void test_setHostnameVerifier() {
        HttpsURLConnection con = new MyHttpsURLConnection(null);
        try {
            con.setHostnameVerifier(null);
            fail("No expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
        try {
            myHostnameVerifier hnv = new myHostnameVerifier();
            con.setHostnameVerifier(hnv);
        } catch (Exception e) {
            fail("Unexpected exception " + e);
        }
    }

    /**
     * @tests javax.net.ssl.HttpsURLConnection#setDefaultSSLSocketFactory()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setDefaultSSLSocketFactory",
        args = {javax.net.ssl.SSLSocketFactory.class}
    )
    public final void test_setDefaultSSLSocketFactory() {
        try {
            HttpsURLConnection.setDefaultSSLSocketFactory(null);
            fail("No expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
        try {
            SSLSocketFactory ssf = (SSLSocketFactory) SSLSocketFactory
                    .getDefault();
            HttpsURLConnection.setDefaultSSLSocketFactory(ssf);
        } catch (Exception e) {
            fail("Unexpected exception " + e);
        }
    }

    /**
     * @tests javax.net.ssl.HttpsURLConnection#setSSLSocketFactory()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setSSLSocketFactory",
        args = {javax.net.ssl.SSLSocketFactory.class}
    )
    public final void test_setSSLSocketFactory() {
        HttpsURLConnection con = new MyHttpsURLConnection(null);
        try {
            con.setSSLSocketFactory(null);
            fail("No expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
        try {
            SSLSocketFactory ssf = (SSLSocketFactory) SSLSocketFactory
                    .getDefault();
            con.setSSLSocketFactory(ssf);
        } catch (Exception e) {
            fail("Unexpected exception " + e);
        }
    }
}

class MyHttpsURLConnection extends javax.net.ssl.HttpsURLConnection {
    
    private String typeDone;
    
    public MyHttpsURLConnection(URL url) {
        super(url);
    }
    
    public MyHttpsURLConnection(URL url, String type) {
        super(url);
        typeDone = type;
    }

    /*
     * @see javax.net.ssl.HttpsURLConnection#getCipherSuite()
     */
    public String getCipherSuite() {
        return "CipherSuite";
    }

    /*
     * @see javax.net.ssl.HttpsURLConnection#getLocalCertificates()
     */
    public Certificate[] getLocalCertificates() {
        Certificate cert = null;
        try {
            CertificateFactory cf = CertificateFactory.getInstance(typeDone);
            byte[] barr = TestUtils.getX509Certificate_v1();
            ByteArrayInputStream bis = new ByteArrayInputStream(barr);
            cert = cf.generateCertificate(bis);
        } catch (CertificateException se) {
            cert = null;
        }
        return cert == null ? null : new Certificate[]{cert};
    }
    
    /*
     * @see javax.net.ssl.HttpsURLConnection#getServerCertificates()
     */
    public Certificate[] getServerCertificates() throws SSLPeerUnverifiedException {
        Certificate cert = null;
        try {
            CertificateFactory cf = CertificateFactory.getInstance(typeDone);
            byte[] barr = TestUtils.getX509Certificate_v3();
            ByteArrayInputStream bis = new ByteArrayInputStream(barr);
            cert = cf.generateCertificate(bis);
        } catch (CertificateException se) {
            throw new SSLPeerUnverifiedException("No server's end-entity certificate");
        }
        return cert == null ? null : new Certificate[]{cert};
    }

    /*
     * @see java.net.HttpURLConnection#disconnect()
     */
    public void disconnect() {
    }

    /*
     * @see java.net.HttpURLConnection#usingProxy()
     */
    public boolean usingProxy() {
        return false;
    }

    public void connect() {
    }

}

class myHostnameVerifier implements HostnameVerifier {
    
    myHostnameVerifier() {
    }
    
    public boolean verify(String hostname, SSLSession session) {
        if (hostname == session.getPeerHost()) {
            return true;
        } else return false;
    }
}

