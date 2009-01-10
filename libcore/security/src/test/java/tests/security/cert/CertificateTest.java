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
* @author Vladimir N. Molotkov
* @version $Revision$
*/

package tests.security.cert;



import dalvik.annotation.BrokenTest;
import dalvik.annotation.KnownFailure;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestTargets;

import junit.framework.TestCase;

import java.io.ByteArrayInputStream;
import java.io.ObjectStreamException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.PublicKey;
import java.security.Security;
import java.security.SignatureException;
import java.security.cert.CertPath;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;

import org.apache.harmony.security.tests.support.cert.MyCertificate;
import org.apache.harmony.security.tests.support.cert.MyFailingCertificate;
import org.apache.harmony.security.tests.support.cert.TestUtils;
import org.apache.harmony.testframework.serialization.SerializationTest;

import tests.api.javax.security.cert.X509CertificateTest.MyModifiablePublicKey;


/**
 * Tests for <code>Certificate</code> fields and methods
 * 
 */
@TestTargetClass(Certificate.class)
public class CertificateTest extends TestCase {
    /**
     * Meaningless cert encoding just for testing purposes
     */
    private static final byte[] testEncoding = new byte[] { (byte) 1, (byte) 2,
            (byte) 3, (byte) 4, (byte) 5 };

    /**
     * Constructor for CertificateTest.
     * @param name
     */
    public CertificateTest(String name) {
        super(name);
    }

    //
    // Tests
    //

    /**
     * Test for <code>Certificate(String type)</code> method<br>
     */
    @TestTargetNew(
        level = TestLevel.SUFFICIENT,
        notes = "",
        method = "Certificate",
        args = {java.lang.String.class}
    )
    public final void testCertificate() {
        try {
            Certificate c1 = new MyCertificate("TEST_TYPE", testEncoding);
            assertTrue(Arrays.equals(testEncoding, c1.getEncoded()));
            assertEquals("TEST", c1.getPublicKey().getAlgorithm());
            assertTrue(Arrays.equals(
                    new byte[] { (byte) 1, (byte) 2, (byte) 3 }, c1
                            .getPublicKey().getEncoded()));
            assertEquals("TEST_FORMAT", c1.getPublicKey().getFormat());
            assertEquals("TEST_TYPE", c1.getType());
        } catch (CertificateEncodingException e) {
            fail("Unexpected CertificateEncodingException " + e.getMessage());
        }  
    }

    /**
     * Test for <code>hashCode()</code> method<br>
     * Assertion: returns hash of the <code>Certificate</code> instance
     * @throws CertificateEncodingException 
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "hashCode",
        args = {}
    )
    public final void testHashCode() throws CertificateEncodingException {
        Certificate c1 = new MyCertificate("TEST_TYPE", testEncoding);
        Certificate c2 = new MyCertificate("TEST_TYPE", testEncoding);

        assertTrue(c1.hashCode() == c2.hashCode());

        assertFalse(c1.hashCode() == new MyCertificate("TEST_TYPE", cert
                .getEncoded()).hashCode());
        assertFalse(c1.hashCode() == cert.hashCode());
    }

    /**
     * Test for <code>hashCode()</code> method<br>
     * Assertion: hash code of equal objects should be the same
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "hashCode",
        args = {}
    )
    public final void testHashCodeEqualsObject() {
        Certificate c1 = new MyCertificate("TEST_TYPE", testEncoding);
        Certificate c2 = new MyCertificate("TEST_TYPE", testEncoding);

        assertTrue((c1.hashCode() == c2.hashCode()) && c1.equals(c2));
        assertFalse(cert.equals(c1));
    }


    /**
     * Test for <code>getType()</code> method<br>
     * Assertion: returns this certificate type 
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getType",
        args = {}
    )
    public final void testGetType() {
        Certificate c1 = new MyCertificate("TEST_TYPE", testEncoding);
        assertEquals("TEST_TYPE", c1.getType());
    }

    /**
     * Test #1 for <code>equals(Object)</code> method<br>
     * Assertion: object equals to itself 
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies positive case.",
        method = "equals",
        args = {java.lang.Object.class}
    )
    public final void testEqualsObject01() {
        Certificate c1 = new MyCertificate("TEST_TYPE", testEncoding);
        assertTrue(c1.equals(c1));
    }

    /**
     * Test for <code>equals(Object)</code> method<br>
     * Assertion: object equals to other <code>Certificate</code>
     * instance with the same state
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies positive case.",
        method = "equals",
        args = {java.lang.Object.class}
    )    
    public final void testEqualsObject02() {
        Certificate c1 = new MyCertificate("TEST_TYPE", testEncoding);
        Certificate c2 = new MyCertificate("TEST_TYPE", testEncoding);
        assertTrue(c1.equals(c2) && c2.equals(c1));
    }

    /**
     * Test for <code>equals(Object)</code> method<br>
     * Assertion: object not equals to <code>null</code>
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies equals method with null as a parameter.",
        method = "equals",
        args = {java.lang.Object.class}
    )
    public final void testEqualsObject03() {
        Certificate c1 = new MyCertificate("TEST_TYPE", testEncoding);
        assertFalse(c1.equals(null));
    }

    /**
     * Test for <code>equals(Object)</code> method<br>
     * Assertion: object not equals to other which is not
     * instance of <code>Certificate</code>
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies negative case.",
        method = "equals",
        args = {java.lang.Object.class}
    )
    public final void testEqualsObject04() {
        Certificate c1 = new MyCertificate("TEST_TYPE", testEncoding);
        assertFalse(c1.equals("TEST_TYPE"));
    }

    //
    // the following tests just call methods
    // that are abstract in <code>Certificate</code>
    // (So they just like signature tests)
    //

    /**
     * This test just calls <code>getEncoded()</code> method<br>
     * @throws CertificateException 
     */
    @TestTargetNew(
        level = TestLevel.SUFFICIENT,
        notes = "Can not verify CertificateEncodingException. indirectly tested",
        method = "getEncoded",
        args = {}
    )
    @KnownFailure("Assertion does not evaluate to true... Works in javax.Certificate")
    public final void testGetEncoded() throws CertificateException {
        Certificate c1 = new MyCertificate("TEST_TYPE", testEncoding);
        assertNotNull(c1.getEncoded());
        
        assertTrue(Arrays.equals(TestUtils.rootCert.getBytes(),cert.getEncoded()));
        
        byte[] b = rootCert.getBytes();
        
        b[4] = (byte) 200;
        
        try {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        ByteArrayInputStream stream = new ByteArrayInputStream(b);
        cert = cf.generateCertificate(stream);
        } catch (CertificateException e) {
            //ok
        }
    }

    /**
     * This test just calls <code>verify(PublicKey)</code> method<br>
     * 
     * @throws InvalidKeyException
     * @throws CertificateException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchProviderException
     * @throws SignatureException
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies only null as a parameter.",
        method = "verify",
        args = {java.security.PublicKey.class}
    )
    public final void testVerifyPublicKey()
        throws InvalidKeyException,
               CertificateException,
               NoSuchAlgorithmException,
               NoSuchProviderException,
               SignatureException {
        Certificate c1 = new MyCertificate("TEST_TYPE", testEncoding);
        c1.verify(null);
    }

    /**
     * This test just calls <code>verify(PublicKey,String)</code> method<br>
     *
     * @throws InvalidKeyException
     * @throws CertificateException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchProviderException
     * @throws SignatureException
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies only null as parameters.",
        method = "verify",
        args = {java.security.PublicKey.class, java.lang.String.class}
    )
    public final void testVerifyPublicKeyString()
        throws InvalidKeyException,
               CertificateException,
               NoSuchAlgorithmException,
               NoSuchProviderException,
               SignatureException {
        Certificate c1 = new MyCertificate("TEST_TYPE", testEncoding);
        c1.verify(null, null);
    }

    /**
     * This test just calls <code>toString()</code> method<br>
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "toString",
        args = {}
    )
    public final void testToString() {
        Certificate c1 = new MyCertificate("TEST_TYPE", testEncoding);
        c1.toString();
    }

    /**
     * This test just calls <code>testGetPublicKey()</code> method<br>
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getPublicKey",
        args = {}
    )
    public final void testGetPublicKey() {
        Certificate c1 = new MyCertificate("TEST_TYPE", testEncoding);
        c1.getPublicKey();
    }

    /**
     * This test just calls <code>writeReplace()</code> method<br>
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Doesn't verify ObjectStreamException.",
        method = "writeReplace",
        args = {}
    )
    public final void testWriteReplace() {
        MyCertificate c1 = new MyCertificate("TEST_TYPE", testEncoding);
        
        try {
            Object obj = c1.writeReplace();
            assertTrue(obj.toString().contains(
                    "java.security.cert.Certificate$CertificateRep"));
        } catch (ObjectStreamException e) {
            fail("Unexpected ObjectStreamException " + e.getMessage());
        }
    }
    
    /**
     * Certificate:
    Data:
        Version: 3 (0x2)
        Serial Number: 0 (0x0)
        Signature Algorithm: sha1WithRSAEncryption
        Issuer: C=AN, ST=Android, O=Android, OU=Android, CN=Android/emailAddress=android
        Validity
            Not Before: Dec  9 16:35:30 2008 GMT
            Not After : Dec  9 16:35:30 2011 GMT
        Subject: C=AN, ST=Android, O=Android, OU=Android, CN=Android/emailAddress=android
        Subject Public Key Info:
            Public Key Algorithm: rsaEncryption
            RSA Public Key: (1024 bit)
                Modulus (1024 bit):
                    00:c5:fb:5e:68:37:82:1d:58:ed:cb:31:8c:08:7f:
                    51:31:4c:68:40:8c:4d:07:a1:0e:18:36:02:6b:89:
                    92:c1:cf:88:1e:cf:00:22:00:8c:37:e8:6a:76:94:
                    71:53:81:78:e1:48:94:fa:16:61:93:eb:a0:ee:62:
                    9d:6a:d2:2c:b8:77:9d:c9:36:d5:d9:1c:eb:26:3c:
                    43:66:4d:7b:1c:1d:c7:a1:37:66:e2:84:54:d3:ed:
                    21:dd:01:1c:ec:9b:0c:1e:35:e9:37:15:9d:2b:78:
                    a8:3b:11:3a:ee:c2:de:55:44:4c:bd:40:8d:e5:52:
                    b0:fc:53:33:73:4a:e5:d0:df
                Exponent: 65537 (0x10001)
        X509v3 extensions:
            X509v3 Subject Key Identifier: 
                4B:E3:22:14:AD:0A:14:46:B7:52:31:8B:AB:9E:5A:62:F3:98:37:80
            X509v3 Authority Key Identifier: 
                keyid:4B:E3:22:14:AD:0A:14:46:B7:52:31:8B:AB:9E:5A:62:F3:98:37:80
                DirName:/C=AN/ST=Android/O=Android/OU=Android/CN=Android/emailAddress=android
                serial:00

            X509v3 Basic Constraints: 
                CA:TRUE
    Signature Algorithm: sha1WithRSAEncryption
        72:4f:12:8a:4e:61:b2:9a:ba:58:17:0b:55:96:f5:66:1c:a8:
        ba:d1:0f:8b:9b:2d:ab:a8:00:ac:7f:99:7d:f6:0f:d7:85:eb:
        75:4b:e5:42:37:71:46:b1:4a:b0:1b:17:e4:f9:7c:9f:bd:20:
        75:35:9f:27:8e:07:95:e8:34:bd:ab:e4:10:5f:a3:7b:4c:56:
        69:d4:d0:f1:e9:74:15:2d:7f:77:f0:38:77:eb:8a:99:f3:a9:
        88:f0:63:58:07:b9:5a:61:f8:ff:11:e7:06:a1:d1:f8:85:fb:
        99:1c:f5:cb:77:86:36:cd:43:37:99:09:c2:9a:d8:f2:28:05:
        06:0c

     */
    public static final String rootCert = "-----BEGIN CERTIFICATE-----\n" + 
    "MIIDGzCCAoSgAwIBAgIBADANBgkqhkiG9w0BAQUFADBtMQswCQYDVQQGEwJBTjEQ\n" + 
    "MA4GA1UECBMHQW5kcm9pZDEQMA4GA1UEChMHQW5kcm9pZDEQMA4GA1UECxMHQW5k\n" + 
    "cm9pZDEQMA4GA1UEAxMHQW5kcm9pZDEWMBQGCSqGSIb3DQEJARYHYW5kcm9pZDAe\n" + 
    "Fw0wODEyMDkxNjM1MzBaFw0xMTEyMDkxNjM1MzBaMG0xCzAJBgNVBAYTAkFOMRAw\n" + 
    "DgYDVQQIEwdBbmRyb2lkMRAwDgYDVQQKEwdBbmRyb2lkMRAwDgYDVQQLEwdBbmRy\n" + 
    "b2lkMRAwDgYDVQQDEwdBbmRyb2lkMRYwFAYJKoZIhvcNAQkBFgdhbmRyb2lkMIGf\n" + 
    "MA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDF+15oN4IdWO3LMYwIf1ExTGhAjE0H\n" + 
    "oQ4YNgJriZLBz4gezwAiAIw36Gp2lHFTgXjhSJT6FmGT66DuYp1q0iy4d53JNtXZ\n" + 
    "HOsmPENmTXscHcehN2bihFTT7SHdARzsmwweNek3FZ0reKg7ETruwt5VREy9QI3l\n" + 
    "UrD8UzNzSuXQ3wIDAQABo4HKMIHHMB0GA1UdDgQWBBRL4yIUrQoURrdSMYurnlpi\n" + 
    "85g3gDCBlwYDVR0jBIGPMIGMgBRL4yIUrQoURrdSMYurnlpi85g3gKFxpG8wbTEL\n" + 
    "MAkGA1UEBhMCQU4xEDAOBgNVBAgTB0FuZHJvaWQxEDAOBgNVBAoTB0FuZHJvaWQx\n" + 
    "EDAOBgNVBAsTB0FuZHJvaWQxEDAOBgNVBAMTB0FuZHJvaWQxFjAUBgkqhkiG9w0B\n" + 
    "CQEWB2FuZHJvaWSCAQAwDAYDVR0TBAUwAwEB/zANBgkqhkiG9w0BAQUFAAOBgQBy\n" + 
    "TxKKTmGymrpYFwtVlvVmHKi60Q+Lmy2rqACsf5l99g/Xhet1S+VCN3FGsUqwGxfk\n" + 
    "+XyfvSB1NZ8njgeV6DS9q+QQX6N7TFZp1NDx6XQVLX938Dh364qZ86mI8GNYB7la\n" + 
    "Yfj/EecGodH4hfuZHPXLd4Y2zUM3mQnCmtjyKAUGDA==\n" + 
    "-----END CERTIFICATE-----";
    
public class MyModifiablePublicKey implements PublicKey {
        
        private PublicKey key;
        private boolean modifiedAlgo;
        private String algo;
        private String format;
        private boolean modifiedFormat;
        private boolean modifiedEncoding;
        private byte[] encoding;
        
        public MyModifiablePublicKey(PublicKey k) {
            super();
            this.key = k;
        }

        public String getAlgorithm() {
            if (modifiedAlgo) {
                return algo;
            } else {
                return key.getAlgorithm();
            }
        }

        public String getFormat() {
            if (modifiedFormat) {
                return this.format;
            } else {
                return key.getFormat();
            }
            
        }

        public byte[] getEncoded() {
            if (modifiedEncoding) {
                return this.encoding;
            } else {
                return key.getEncoded();
            }
            
        }

        public long getSerVerUID() {
            return key.serialVersionUID;
        }
        
        public void setAlgorithm(String myAlgo) {
            modifiedAlgo = true;
            this.algo = myAlgo;
        }
        
        public void setFormat(String myFormat) {
            modifiedFormat = true;
            format = myFormat;
        }
        
        public void setEncoding(byte[] myEncoded) {
            modifiedEncoding = true;
            encoding = myEncoded;
        }
    }
    
    private Certificate cert;

    private Provider wrongProvider;

    private Provider useFulProvider;
   
    public void setUp() throws Exception {
        super.setUp();
        TestUtils.initCertPathSSCertChain();
        cert = TestUtils.rootCertificateSS;
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        wrongProvider = cf.getProvider();
        useFulProvider = Security.getProviders("Signature.sha1WithRSAEncryption")[0];
    }
    
    /**
     * This test just calls <code>verify(PublicKey,String)</code> method<br>
     *
     * @throws InvalidKeyException
     * @throws CertificateException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchProviderException
     * @throws SignatureException
     */
    @TestTargetNew(
        level = TestLevel.SUFFICIENT,
        notes = "Test fails: ClassCastException when SignatureException is expected",
        method = "verify",
        args = {java.security.PublicKey.class, java.lang.String.class}
    )
    @BrokenTest("Test fails: ClassCastException when SignatureException is expected")
    public final void testVerifyPublicKeyString2() throws InvalidKeyException,
            CertificateException, NoSuchAlgorithmException,
            NoSuchProviderException, SignatureException {

        // real test
        cert.verify(cert.getPublicKey(), useFulProvider.getName());

        // Exception tests

        try {
            cert.verify(cert.getPublicKey(), "UnknownProvider");
        } catch (NoSuchProviderException e) {
            // ok
        }

        Security.removeProvider(wrongProvider.getName());

        try {
            cert.verify(cert.getPublicKey(), wrongProvider.getName());
        } catch (NoSuchAlgorithmException e) {
            // ok
        }

        Security.addProvider(wrongProvider);
        /*
        PublicKey k = cert.getPublicKey();
        MyModifiablePublicKey tamperedKey = new MyModifiablePublicKey(k);
        tamperedKey.setAlgorithm("wrongAlgo");

        try {
            cert.verify(tamperedKey, provs[0].getName());
        } catch (SignatureException e) {
            // ok
        }
        
        try {
            cert.verify(c1.getPublicKey(), provs[0].getName());
        } catch (InvalidKeyException e) {
            // ok
        }
        */
    }
    
    /**
     * This test just calls <code>verify(PublicKey)</code> method<br>
     * 
     * @throws InvalidKeyException
     * @throws CertificateException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchProviderException
     * @throws SignatureException
     */
    @TestTargetNew(
        level = TestLevel.SUFFICIENT,
        notes = "Test fails: ClassCastException when InvalidKeyException is expected." +
                "",
        method = "verify",
        args = {java.security.PublicKey.class}
    )
    @BrokenTest("ClassCastException")
    public final void testVerifyPublicKey2() throws InvalidKeyException,
            CertificateException, NoSuchAlgorithmException,
            NoSuchProviderException, SignatureException {
        
        Certificate c1 = new MyCertificate("TEST_TYPE", testEncoding);
        c1.verify(null);

        cert.verify(cert.getPublicKey());
        
        PublicKey k = cert.getPublicKey();

        MyModifiablePublicKey changedEncoding = new MyModifiablePublicKey(k);
        changedEncoding
                .setEncoding(new byte[cert.getEncoded().length - 1]);
        
        try {
            cert.verify(c1.getPublicKey());
            fail("expected InvalidKeyException");
        } catch (InvalidKeyException e) {
            // ok
        }

        try {
            cert.verify(changedEncoding);
            fail("Exception expected");
        } catch (Exception e) {
            // ok
        }
        
        MyModifiablePublicKey changedAlgo = new MyModifiablePublicKey(k);
        changedAlgo.setAlgorithm("MD5withBla");
        
        try {
            cert.verify(changedAlgo);
            fail("Exception expected");
        } catch (SignatureException e) {
            // ok
        }
        
    }
    
    /**
     * This test just calls <code>writeReplace()</code> method<br>
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "writeReplace",
        args = {}
    )
    public final void testWriteReplace2() {
        MyCertificate c1 = new MyFailingCertificate("TEST_TYPE", testEncoding);
        
        try {
            Object obj = c1.writeReplace();
        } catch (ObjectStreamException e) {
            //ok
        }
    }
    
    /**
     * @tests serialization/deserialization compatibility.
     */
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Verifies serialization/deserialization compatibility. And tests default constructor",
            method = "!SerializationSelf",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "writeReplace",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "Certificate.CertificateRep.readResolve",
            args = {}
        )
    })
    public void testSerializationSelf() throws Exception {
        TestUtils.initCertPathSSCertChain();

        SerializationTest.verifySelf(TestUtils.rootCertificateSS);
    }

    /**
     * @tests serialization/deserialization compatibility with RI.
     */
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Verifies serialization/deserialization compatibility.",
            method = "!SerializationGolden",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "writeReplace",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "Certificate.CertificateRep.readResolve",
            args = {}
        )
    })
    public void testSerializationCompatibility() throws Exception {
        //create test file (once)
//        SerializationTest.createGoldenFile("device/dalvik/libcore/security/src/test/resources/serialization", this, TestUtils.rootCertificateSS);
        TestUtils.initCertPathSSCertChain();

        SerializationTest.verifyGolden(this, TestUtils.rootCertificateSS);
    }
}
