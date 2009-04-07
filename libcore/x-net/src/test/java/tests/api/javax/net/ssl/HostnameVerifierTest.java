/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership. The ASF
 * licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package tests.api.javax.net.ssl;

import dalvik.annotation.AndroidOnly;
import dalvik.annotation.BrokenTest;
import dalvik.annotation.KnownFailure;
import dalvik.annotation.SideEffect;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestTargetNew;

import junit.framework.TestCase;

import org.apache.harmony.xnet.tests.support.mySSLSession;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;


/**
 * Tests for <code>HostnameVerifier</code> class constructors and methods.
 * 
 */
@TestTargetClass(HostnameVerifier.class)
public class HostnameVerifierTest extends TestCase implements
        CertificatesToPlayWith {

    /**
     * @tests javax.net.ssl.HostnameVerifier#verify(String hostname, SSLSession
     *        session)
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "verify",
        args = {String.class, SSLSession.class}
    )
    @SideEffect("the DefaultHostnameVerifier is set in some other tests, therefore we need isolation")
    public final void test_verify() {
        mySSLSession session = new mySSLSession("localhost", 1080, null);
        HostnameVerifier hv = HttpsURLConnection.getDefaultHostnameVerifier();
        try {
            assertFalse(hv.verify("localhost", session));
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }
    }

    // copied and modified from apache http client test suite.
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "verify",
        args = {String.class, SSLSession.class}
    )
    @AndroidOnly("DefaultHostnameVerifier on RI is weird and cannot be tested this way.")
    @SideEffect("the DefaultHostnameVerifier is set in some other tests, therefore we need isolation")
    public void testVerify() throws Exception {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        InputStream in;
        X509Certificate x509;
        in = new ByteArrayInputStream(X509_FOO);
        x509 = (X509Certificate) cf.generateCertificate(in);
        mySSLSession session = new mySSLSession(new X509Certificate[] {x509});

        HostnameVerifier verifier = HttpsURLConnection
                .getDefaultHostnameVerifier();

        assertTrue(verifier.verify("foo.com", session));
        assertFalse(verifier.verify("a.foo.com", session));
        assertFalse(verifier.verify("bar.com", session));

        in = new ByteArrayInputStream(X509_HANAKO);
        x509 = (X509Certificate) cf.generateCertificate(in);
        session = new mySSLSession(new X509Certificate[] {x509});
        assertTrue(verifier.verify("\u82b1\u5b50.co.jp", session));
        assertFalse(verifier.verify("a.\u82b1\u5b50.co.jp", session));

        in = new ByteArrayInputStream(X509_FOO_BAR);
        x509 = (X509Certificate) cf.generateCertificate(in);
        session = new mySSLSession(new X509Certificate[] {x509});
        assertTrue(verifier.verify("foo.com", session));
        assertFalse(verifier.verify("a.foo.com", session));
        assertTrue(verifier.verify("bar.com", session));
        assertFalse(verifier.verify("a.bar.com", session));

        in = new ByteArrayInputStream(X509_FOO_BAR_HANAKO);
        x509 = (X509Certificate) cf.generateCertificate(in);
        session = new mySSLSession(new X509Certificate[] {x509});
        assertTrue(verifier.verify("foo.com", session));
        assertFalse(verifier.verify("a.foo.com", session));
        // these checks test alternative subjects. The test data contains an
        // alternative subject starting with a japanese kanji character. This is
        // not supported by Android because the underlying implementation from
        // harmony follows the definition from rfc 1034 page 10 for alternative
        // subject names. This causes the code to drop all alternative subjects.
        // assertTrue(verifier.verify("bar.com", session));
        // assertFalse(verifier.verify("a.bar.com", session));
        // assertFalse(verifier.verify("a.\u82b1\u5b50.co.jp", session));

        in = new ByteArrayInputStream(X509_NO_CNS_FOO);
        x509 = (X509Certificate) cf.generateCertificate(in);
        session = new mySSLSession(new X509Certificate[] {x509});
        assertTrue(verifier.verify("foo.com", session));
        assertFalse(verifier.verify("a.foo.com", session));

        in = new ByteArrayInputStream(X509_NO_CNS_FOO);
        x509 = (X509Certificate) cf.generateCertificate(in);
        session = new mySSLSession(new X509Certificate[] {x509});
        assertTrue(verifier.verify("foo.com", session));
        assertFalse(verifier.verify("a.foo.com", session));

        in = new ByteArrayInputStream(X509_THREE_CNS_FOO_BAR_HANAKO);
        x509 = (X509Certificate) cf.generateCertificate(in);
        session = new mySSLSession(new X509Certificate[] {x509});
        assertFalse(verifier.verify("foo.com", session));
        assertFalse(verifier.verify("a.foo.com", session));
        assertFalse(verifier.verify("bar.com", session));
        assertFalse(verifier.verify("a.bar.com", session));
        assertTrue(verifier.verify("\u82b1\u5b50.co.jp", session));
        assertFalse(verifier.verify("a.\u82b1\u5b50.co.jp", session));

        in = new ByteArrayInputStream(X509_WILD_FOO);
        x509 = (X509Certificate) cf.generateCertificate(in);
        session = new mySSLSession(new X509Certificate[] {x509});
        assertFalse(verifier.verify("foo.com", session));
        assertTrue(verifier.verify("www.foo.com", session));
        assertTrue(verifier.verify("\u82b1\u5b50.foo.com", session));
        assertTrue(verifier.verify("a.b.foo.com", session));

        in = new ByteArrayInputStream(X509_WILD_CO_JP);
        x509 = (X509Certificate) cf.generateCertificate(in);
        session = new mySSLSession(new X509Certificate[] {x509});
        // Silly test because no-one would ever be able to lookup an IP address
        // using "*.co.jp".
        assertTrue(verifier.verify("*.co.jp", session));
        assertFalse(verifier.verify("foo.co.jp", session));
        assertFalse(verifier.verify("\u82b1\u5b50.co.jp", session));

        in = new ByteArrayInputStream(X509_WILD_FOO_BAR_HANAKO);
        x509 = (X509Certificate) cf.generateCertificate(in);
        session = new mySSLSession(new X509Certificate[] {x509});
        // try the foo.com variations
        assertFalse(verifier.verify("foo.com", session));
        assertTrue(verifier.verify("www.foo.com", session));
        assertTrue(verifier.verify("\u82b1\u5b50.foo.com", session));
        assertTrue(verifier.verify("a.b.foo.com", session));
        // these checks test alternative subjects. The test data contains an
        // alternative subject starting with a japanese kanji character. This is
        // not supported by Android because the underlying implementation from
        // harmony follows the definition from rfc 1034 page 10 for alternative
        // subject names. This causes the code to drop all alternative subjects.
        // assertFalse(verifier.verify("bar.com", session));
        // assertTrue(verifier.verify("www.bar.com", session));
        // assertTrue(verifier.verify("\u82b1\u5b50.bar.com", session));
        // assertTrue(verifier.verify("a.b.bar.com", session));
    }

    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "verify",
        args = {String.class, SSLSession.class}
    )
    @AndroidOnly("DefaultHostnameVerifier on RI is weird and cannot be tested this way.")
    @KnownFailure("DefaultHostnameVerifier is broken on Android, fixed in donutburger")
    @SideEffect("the DefaultHostnameVerifier is set in some other tests, therefore we need isolation")
    public void testSubjectAlt() throws Exception {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        InputStream in = new ByteArrayInputStream(X509_MULTIPLE_SUBJECT_ALT);
        X509Certificate x509 = (X509Certificate) cf.generateCertificate(in);
        mySSLSession session = new mySSLSession(new X509Certificate[] {x509});

        HostnameVerifier verifier = HttpsURLConnection
                .getDefaultHostnameVerifier();

        // Whitespace differences between RI and Android are ignored by
        // replacing ", " with ","
        assertEquals(
                "CN=localhost,OU=Unknown,O=Unknown,L=Unknown,ST=Unknown,C=CH",
                x509.getSubjectDN().getName().replace(", ", ","));

        assertTrue(verifier.verify("localhost", session));
        assertTrue(verifier.verify("localhost.localdomain", session));
        assertTrue(verifier.verify("127.0.0.1", session));

        assertFalse(verifier.verify("local.host", session));
        assertFalse(verifier.verify("127.0.0.2", session));

    }
}
