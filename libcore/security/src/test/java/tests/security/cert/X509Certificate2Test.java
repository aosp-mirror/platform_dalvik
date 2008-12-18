/* 
 * Licensed to the Apache Software Foundation (ASF) under one or more
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

package tests.security.cert;

import dalvik.annotation.TestInfo;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTarget;
import dalvik.annotation.TestTargetClass;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Principal;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import javax.security.auth.x500.X500Principal;

import org.apache.harmony.security.tests.support.cert.TestUtils;
import tests.support.resource.Support_Resources;

@TestTargetClass(X509Certificate.class)
public class X509Certificate2Test extends junit.framework.TestCase {

    /**
     * @tests java.security.cert.X509Certificate#getExtensionValue(java.lang.String)
     */
    @TestInfo(
      level = TestLevel.COMPLETE,
      purpose = "",
      targets = {
        @TestTarget(
          methodName = "getExtensionValue",
          methodArgs = {String.class}
        )
    })
    public void _test_getExtensionValueLjava_lang_String() throws Exception {

        InputStream is = Support_Resources
                .getResourceStream("hyts_certificate_PEM.txt");

        CertificateFactory certFact = CertificateFactory.getInstance("X509");
        X509Certificate pemCert = (X509Certificate) certFact
                .generateCertificate(is);

        Vector<String> extensionOids = new Vector<String>();
        extensionOids.addAll(pemCert.getCriticalExtensionOIDs());
        extensionOids.addAll(pemCert.getNonCriticalExtensionOIDs());
        Iterator i = extensionOids.iterator();
        while (i.hasNext()) {
            String oid = (String) i.next();
            byte[] value = pemCert.getExtensionValue(oid);
            if (value != null && value.length > 0) {
                // check that it is an encoded as a OCTET STRING
                assertEquals("The extension value for the oid " + oid
                        + " was not encoded as an OCTET STRING", 0x04, value[0]);
            }
        }
    }

    /**
     * Test for X.509 Certificate provider
     */
    @TestInfo(
      level = TestLevel.COMPLETE,
      purpose = "",
      targets = {
        @TestTarget(
          methodName = "toString",
          methodArgs = {}
        )
    })
    public void test_toString() throws Exception {

        // Regression for HARMONY-3384
        CertificateFactory certFact = CertificateFactory.getInstance("X509");
        X509Certificate pemCert = (X509Certificate) certFact
                .generateCertificate(new ByteArrayInputStream(TestUtils
                        .getX509Certificate_v3()));

        // extension value is empty sequence
        byte[] extnValue = pemCert.getExtensionValue("2.5.29.35");
        assertTrue(Arrays.equals(new byte[] { 0x04, 0x02, 0x30, 0x00 },
                extnValue));
        assertNotNull(pemCert.toString());
        // End regression for HARMONY-3384
    }

    /**
     * @tests java.security.cert.X509Certificate#X509Certificate()
     */
    @TestInfo(
      level = TestLevel.COMPLETE,
      purpose = "",
      targets = {
        @TestTarget(
          methodName = "X509Certificate",
          methodArgs = {}
        )
    })
    public void test_X509Certificate() {
        MyX509Certificate s = null;
        try {
            s = new MyX509Certificate();
        } catch (Exception e) {
            fail("Unexpected exception " + e.getMessage());
        }
        assertEquals("X.509", s.getType());
    }

    // Base64 encoded form of ASN.1 DER encoded X.509 Certificate
    // (see RFC 3280 at http://www.ietf.org/rfc/rfc3280.txt)
    // (generated by using of classes from
    // org.apache.harmony.security.x509 package)
    static String base64cert = "MIIByzCCATagAwIBAgICAiswCwYJKoZIhvcNAQEFMB0xGzAZBgNVBAoT"
            + "EkNlcnRpZmljYXRlIElzc3VlcjAeFw0wNjA0MjYwNjI4MjJaFw0zMzAz"
            + "MDExNjQ0MDlaMB0xGzAZBgNVBAoTEkNlcnRpZmljYXRlIElzc3VlcjCB"
            + "nzANBgkqhkiG9w0BAQEFAAOBjQAwgYkCgYEAkLGLsPdSPDMyP1OUOKu"
            + "U3cvbNK5RGaQ3bXc5aDjvApx43BcaoXgt6YD/5yXz0OsIooj5yA37bY"
            + "JGcVrvFD5FMPdDd3vjNPQOep0MzG4CdbkaZde5SigPabOMQYS4oUyLBx"
            + "W3LGG0mUODe5AGGqtqXU0GlKg4K2je6cCtookCUCAwEAAaMeMBwwGgYD"
            + "VR0RAQH/BBAwDoEMcmZjQDgyMi5OYW1lMAsGCSqGSIb3DQEBBQOBgQBZ"
            + "pVXj01dOpqnZErUQb50j8lJD1dIaz1eJTvJCSadj7ziV1VtnnapI07c"
            + "XEa7ONzcHQTYTG10poHfOK/a0BaULF3GlctDESilwQYbW5BdfpAlZpbH"
            + "AFLcUDh6Eq50kc0A/anh/j3mgBNuvbIMo7hHNnZB6k/prswm2BszyLD"
            + "yw==";

    // Base64 encoded form of ASN.1 DER encoded X.509 CRL
    // (see RFC 3280 at http://www.ietf.org/rfc/rfc3280.txt)
    // (generated by using of classes from
    // org.apache.harmony.security.x509 package)
    static String base64crl = "MIHXMIGXAgEBMAkGByqGSM44BAMwFTETMBEGA1UEChMKQ1JMIElzc3Vl"
            + "chcNMDYwNDI3MDYxMzQ1WhcNMDYwNDI3MDYxNTI1WjBBMD8CAgIrFw0w"
            + "NjA0MjcwNjEzNDZaMCowCgYDVR0VBAMKAQEwHAYDVR0YBBUYEzIwMDYw"
            + "NDI3MDYxMzQ1LjQ2OFqgDzANMAsGA1UdFAQEBAQEBDAJBgcqhkjOOAQD"
            + "AzAAMC0CFQCk0t0DTyu82QpajbBlxX9uXvUDSgIUSBN4g+xTEeexs/0k"
            + "9AkjBhjF0Es=";

    // has stub implementation for abstract methods
    private static class MyX509Certificate extends X509Certificate {

        private static final long serialVersionUID = -7196694072296607007L;

        public void checkValidity() throws CertificateExpiredException,
                CertificateNotYetValidException {
        }

        public void checkValidity(Date date)
                throws CertificateExpiredException,
                CertificateNotYetValidException {
        }

        public int getVersion() {
            return 3;
        }

        public BigInteger getSerialNumber() {
            return null;
        }

        public Principal getIssuerDN() {
            return null;
        }

        public Principal getSubjectDN() {
            return null;
        }

        public Date getNotBefore() {
            return null;
        }

        public Date getNotAfter() {
            return null;
        }

        public byte[] getTBSCertificate() throws CertificateEncodingException {
            return null;
        }

        public byte[] getSignature() {
            return null;
        }

        public String getSigAlgName() {
            return null;
        }

        public String getSigAlgOID() {
            return null;
        }

        public byte[] getSigAlgParams() {
            return null;
        }

        public boolean[] getIssuerUniqueID() {
            return null;
        }

        public boolean[] getSubjectUniqueID() {
            return null;
        }

        public boolean[] getKeyUsage() {
            return null;
        }

        public int getBasicConstraints() {
            return 0;
        }

        public void verify(PublicKey key) throws CertificateException,
                NoSuchAlgorithmException, InvalidKeyException,
                NoSuchProviderException, SignatureException {
        }

        public void verify(PublicKey key, String sigProvider)
                throws CertificateException, NoSuchAlgorithmException,
                InvalidKeyException, NoSuchProviderException,
                SignatureException {
        }

        public String toString() {
            return "";
        }

        public PublicKey getPublicKey() {
            return null;
        }

        public byte[] getEncoded() throws CertificateEncodingException {
            return null;
        }

        public Set<String> getNonCriticalExtensionOIDs() {
            return null;
        }

        public Set<String> getCriticalExtensionOIDs() {
            return null;
        }

        public byte[] getExtensionValue(String oid) {
            return null;
        }

        public boolean hasUnsupportedCriticalExtension() {
            return false;
        }
    }

    /**
     * @tests java.security.cert.X509Certificate#getType()
     */
    @TestInfo(
      level = TestLevel.COMPLETE,
      purpose = "",
      targets = {
        @TestTarget(
          methodName = "getType",
          methodArgs = {}
        )
    })
    public void testGetType() {
        assertEquals("X.509", new MyX509Certificate().getType());
    }

    /**
     * @tests java.security.cert.X509Certificate#getIssuerX500Principal()
     */
    @TestInfo(
      level = TestLevel.COMPLETE,
      purpose = "",
      targets = {
        @TestTarget(
          methodName = "getIssuerX500Principal",
          methodArgs = {}
        )
    })
    public void testGetIssuerX500Principal() {
        // return valid encoding
        MyX509Certificate cert = new MyX509Certificate() {
            private static final long serialVersionUID = 638659908323741165L;

            public byte[] getEncoded() {
                return TestUtils.getX509Certificate_v1();
            };
        };

        assertEquals(new X500Principal("CN=Z"), cert.getIssuerX500Principal());
    }

    /**
     * @tests java.security.cert.X509Certificate#getSubjectX500Principal()
     */
    @TestInfo(
      level = TestLevel.COMPLETE,
      purpose = "",
      targets = {
        @TestTarget(
          methodName = "getSubjectX500Principal",
          methodArgs = {}
        )
    })
    public void testGetSubjectX500Principal() {
        // return valid encoding
        MyX509Certificate cert = new MyX509Certificate() {
            private static final long serialVersionUID = -3625913637413840694L;

            public byte[] getEncoded() {
                return TestUtils.getX509Certificate_v1();
            };
        };

        assertEquals(new X500Principal("CN=Y"), cert.getSubjectX500Principal());
    }

    /**
     * @tests java.security.cert.X509Certificate#getExtendedKeyUsage()
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "Doesn't verify CertificateParsingException.",
      targets = {
        @TestTarget(
          methodName = "getExtendedKeyUsage",
          methodArgs = {}
        )
    })
    public void testGetExtendedKeyUsage() throws CertificateParsingException {
        assertNull(new MyX509Certificate().getExtendedKeyUsage());
    }

    /**
     * @tests java.security.cert.X509Certificate#getSubjectAlternativeNames()
     */
    @TestInfo(
      level = TestLevel.COMPLETE,
      purpose = "",
      targets = {
        @TestTarget(
          methodName = "getSubjectAlternativeNames",
          methodArgs = {}
        )
    })
    public void testGetSubjectAlternativeNames()
            throws CertificateParsingException {

        assertNull(new MyX509Certificate().getSubjectAlternativeNames());
    }

    /**
     * @tests java.security.cert.X509Certificate#getIssuerAlternativeNames()
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "Doesn't verify CertificateParsingException.",
      targets = {
        @TestTarget(
          methodName = "getIssuerAlternativeNames",
          methodArgs = {}
        )
    })
    public void testGetIssuerAlternativeNames()
            throws CertificateParsingException {

        assertNull(new MyX509Certificate().getIssuerAlternativeNames());
    }

}
