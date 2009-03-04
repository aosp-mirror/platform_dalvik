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

package tests.security.cert;

import dalvik.annotation.BrokenTest;

import dalvik.annotation.KnownFailure;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargetClass;

import junit.framework.TestCase;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Principal;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.cert.CertPath;
import java.security.cert.CertPathBuilder;
import java.security.cert.CertPathBuilderException;
import java.security.cert.CertStore;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.CertificateParsingException;
import java.security.cert.CollectionCertStoreParameters;
import java.security.cert.PKIXBuilderParameters;
import java.security.cert.PKIXCertPathBuilderResult;
import java.security.cert.TrustAnchor;
import java.security.cert.X509CRL;
import java.security.cert.X509CertSelector;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.security.auth.x500.X500Principal;


import org.apache.harmony.security.provider.cert.X509CertImpl;
import org.apache.harmony.security.tests.support.cert.MyCRL;
import org.apache.harmony.security.tests.support.cert.TestUtils;
import org.apache.harmony.security.tests.support.TestKeyPair;
import org.apache.harmony.security.asn1.ASN1Boolean;
import org.apache.harmony.security.asn1.ASN1Integer;
import org.apache.harmony.security.asn1.ASN1OctetString;
import org.apache.harmony.security.asn1.ASN1Oid;
import org.apache.harmony.security.asn1.ASN1Sequence;
import org.apache.harmony.security.asn1.ASN1Type;
import org.apache.harmony.security.x501.Name;
import org.apache.harmony.security.x509.CertificatePolicies;
import org.apache.harmony.security.x509.GeneralName;
import org.apache.harmony.security.x509.GeneralNames;
import org.apache.harmony.security.x509.GeneralSubtree;
import org.apache.harmony.security.x509.GeneralSubtrees;
import org.apache.harmony.security.x509.NameConstraints;
import org.apache.harmony.security.x509.ORAddress;
import org.apache.harmony.security.x509.OtherName;
import org.apache.harmony.security.x509.PolicyInformation;
import org.apache.harmony.security.x509.PrivateKeyUsagePeriod;

/**
 * X509CertSelectorTest
 */
@TestTargetClass(X509CertSelector.class)
public class X509CertSelectorTest extends TestCase {

    /**
     * @tests java.security.cert.X509CertSelector#addSubjectAlternativeName(int, byte[])
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "IOException checking missed",
        method = "addSubjectAlternativeName",
        args = {int.class, byte[].class}
    )
    public void test_addSubjectAlternativeNameLintLbyte_array() throws IOException {
        // Regression for HARMONY-2487
        int[] types = { 0, 1, 2, 3, 4, 5, 6, 7, 8 };
        for (int i = 0; i < types.length; i++) {
            try {
                new X509CertSelector().addSubjectAlternativeName(types[i],
                        (byte[]) null);
                fail("No expected NullPointerException for type: " + i);
            } catch (NullPointerException e) {
            }
        }
    }

    /**
     * @tests java.security.cert.X509CertSelector#addSubjectAlternativeName(int, String)
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Verifies IOException.",
        method = "addSubjectAlternativeName",
        args = {int.class, java.lang.String.class}
    )
    public void test_addSubjectAlternativeNameLintLjava_lang_String() {
        // Regression for HARMONY-727
        int[] types = { 0, 2, 3, 4, 5, 6, 7, 8 };
        for (int i = 0; i < types.length; i++) {
            try {
                new X509CertSelector().addSubjectAlternativeName(types[i],
                        "0xDFRF");
                fail("IOException expected");
            } catch (IOException e) {
            }
        }
    }

    /**
     * @tests java.security.cert.X509CertSelector#addPathToName(int, byte[])
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Verifies NullPointerException.",
        method = "addPathToName",
        args = {int.class, byte[].class}
    )
    public void test_addPathToNameLintLbyte_array() throws IOException {
        // Regression for HARMONY-2487
        int[] types = { 0, 1, 2, 3, 4, 5, 6, 7, 8 };
        for (int i = 0; i < types.length; i++) {
            try {
                new X509CertSelector().addPathToName(types[i], (byte[]) null);
                fail("No expected NullPointerException for type: " + i);
            } catch (NullPointerException e) {
            }
        }
    }

    /**
     * @tests java.security.cert.X509CertSelector#addPathToName(int, String)
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Verifies IOException.",
        method = "addPathToName",
        args = {int.class, java.lang.String.class}
    )
    public void test_addPathToNameLintLjava_lang_String() {
        // Regression for HARMONY-724
        for (int type = 0; type <= 8; type++) {
            try {
                new X509CertSelector().addPathToName(type, (String) null);
                fail("IOException expected!");
            } catch (IOException ioe) {
                // expected
            }
        }
        
        
    }
    
    /**
     * @tests java.security.cert.X509CertSelector#X509CertSelector()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "X509CertSelector",
        args = {}
    )
    public void test_X509CertSelector() {
        X509CertSelector selector = null;
        try {
            selector = new X509CertSelector();
        } catch (Exception e) {
            fail("Unexpected exception " + e.getMessage());
        }
        assertEquals(-1, selector.getBasicConstraints());
        assertTrue(selector.getMatchAllSubjectAltNames());
    }

    /**
     * @tests java.security.cert.X509CertSelector#clone()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "clone",
        args = {}
    )
    public void test_clone() throws Exception {
        X509CertSelector selector = new X509CertSelector();
        X509CertSelector selector1 = (X509CertSelector) selector.clone();

        assertEquals(selector.getMatchAllSubjectAltNames(), selector1
                .getMatchAllSubjectAltNames());
        assertEquals(selector.getAuthorityKeyIdentifier(), selector1
                .getAuthorityKeyIdentifier());
        assertEquals(selector.getBasicConstraints(), selector1
                .getBasicConstraints());
        assertEquals(selector.getCertificate(), selector1.getCertificate());
        assertEquals(selector.getCertificateValid(), selector1
                .getCertificateValid());
        assertEquals(selector.getExtendedKeyUsage(), selector1
                .getExtendedKeyUsage());
        assertEquals(selector.getIssuer(), selector1.getIssuer());
        assertEquals(selector.getIssuerAsBytes(), selector1.getIssuerAsBytes());
        assertEquals(selector.getIssuerAsString(), selector1
                .getIssuerAsString());
        assertEquals(selector.getKeyUsage(), selector1.getKeyUsage());
        assertEquals(selector.getNameConstraints(), selector1
                .getNameConstraints());
        assertEquals(selector.getPathToNames(), selector1.getPathToNames());
        assertEquals(selector.getPolicy(), selector1.getPolicy());
        assertEquals(selector.getPrivateKeyValid(), selector1
                .getPrivateKeyValid());
        assertEquals(selector.getSerialNumber(), selector1.getSerialNumber());
        assertEquals(selector.getSubject(), selector1.getSubject());
        assertEquals(selector.getSubjectAlternativeNames(), selector1
                .getSubjectAlternativeNames());
        assertEquals(selector.getSubjectAsBytes(), selector1
                .getSubjectAsBytes());
        assertEquals(selector.getSubjectAsString(), selector1
                .getSubjectAsString());
        assertEquals(selector.getSubjectKeyIdentifier(), selector1
                .getSubjectKeyIdentifier());
        assertEquals(selector.getSubjectPublicKey(), selector1
                .getSubjectPublicKey());
        assertEquals(selector.getSubjectPublicKeyAlgID(), selector1
                .getSubjectPublicKeyAlgID());

        selector = null;
        try {
            selector.clone();
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
            // expected
        }
    }

    /**
     * @tests java.security.cert.X509CertSelector#getAuthorityKeyIdentifier()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getAuthorityKeyIdentifier",
        args = {}
    )
    public void test_getAuthorityKeyIdentifier() {
        byte[] akid1 = new byte[] { 4, 5, 1, 2, 3, 4, 5 }; // random value
        byte[] akid2 = new byte[] { 4, 5, 5, 4, 3, 2, 1 }; // random value
        X509CertSelector selector = new X509CertSelector();

        assertNull("Selector should return null", selector
                .getAuthorityKeyIdentifier());
        selector.setAuthorityKeyIdentifier(akid1);
        assertTrue("The returned keyID should be equal to specified", Arrays
                .equals(akid1, selector.getAuthorityKeyIdentifier()));
        assertTrue("The returned keyID should be equal to specified", Arrays
                .equals(akid1, selector.getAuthorityKeyIdentifier()));
        assertFalse("The returned keyID should differ", Arrays.equals(akid2,
                selector.getAuthorityKeyIdentifier()));
    }

    /**
     * @tests java.security.cert.X509CertSelector#getBasicConstraints()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getBasicConstraints",
        args = {}
    )
    public void test_getBasicConstraints() {
        X509CertSelector selector = new X509CertSelector();
        int[] validValues = { 2, 1, 0, 1, 2, 3, 10, 20 };
        for (int i = 0; i < validValues.length; i++) {
            selector.setBasicConstraints(validValues[i]);
            assertEquals(validValues[i], selector.getBasicConstraints());
        }
    }

    /**
     * @tests java.security.cert.X509CertSelector#getCertificate()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getCertificate",
        args = {}
    )
    public void test_getCertificate() throws CertificateException {
        X509CertSelector selector = new X509CertSelector();
        CertificateFactory certFact = CertificateFactory.getInstance("X509");
        X509Certificate cert1 = (X509Certificate) certFact
                .generateCertificate(new ByteArrayInputStream(TestUtils
                        .getX509Certificate_v3()));

        X509Certificate cert2 = (X509Certificate) certFact
                .generateCertificate(new ByteArrayInputStream(TestUtils
                        .getX509Certificate_v1()));

        selector.setCertificate(cert1);
        assertEquals(cert1, selector.getCertificate());

        selector.setCertificate(cert2);
        assertEquals(cert2, selector.getCertificate());

        selector.setCertificate(null);
        assertNull(selector.getCertificate());
    }

    /**
     * @tests java.security.cert.X509CertSelector#getCertificateValid()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getCertificateValid",
        args = {}
    )
    public void test_getCertificateValid() {
        Date date1 = new Date(100);
        Date date2 = new Date(200);
        Date date3 = Calendar.getInstance().getTime();
        X509CertSelector selector = new X509CertSelector();

        assertNull("Selector should return null", selector
                .getCertificateValid());
        selector.setCertificateValid(date1);
        assertTrue("The returned date should be equal to specified", date1
                .equals(selector.getCertificateValid()));
        selector.getCertificateValid().setTime(200);
        assertTrue("The returned date should be equal to specified", date1
                .equals(selector.getCertificateValid()));
        assertFalse("The returned date should differ", date2.equals(selector
                .getCertificateValid()));
        selector.setCertificateValid(date3);
        assertTrue("The returned date should be equal to specified", date3
                .equals(selector.getCertificateValid()));
        selector.setCertificateValid(null);
        assertNull(selector.getCertificateValid());
    }

    /**
     * @tests java.security.cert.X509CertSelector#getExtendedKeyUsage()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getExtendedKeyUsage",
        args = {}
    )
    public void test_getExtendedKeyUsage() {
        HashSet<String> ku = new HashSet<String>(Arrays
                .asList(new String[] { "1.3.6.1.5.5.7.3.1",
                        "1.3.6.1.5.5.7.3.2", "1.3.6.1.5.5.7.3.3",
                        "1.3.6.1.5.5.7.3.4", "1.3.6.1.5.5.7.3.8",
                        "1.3.6.1.5.5.7.3.9", "1.3.6.1.5.5.7.3.5",
                        "1.3.6.1.5.5.7.3.6", "1.3.6.1.5.5.7.3.7" }));
        X509CertSelector selector = new X509CertSelector();

        assertNull("Selector should return null", selector
                .getExtendedKeyUsage());
        try {
            selector.setExtendedKeyUsage(ku);
        } catch (IOException e) {
            fail("Unexpected IOException was thrown.");
        }
        assertTrue(
                "The returned extendedKeyUsage should be equal to specified",
                ku.equals(selector.getExtendedKeyUsage()));
        try {
            selector.getExtendedKeyUsage().add("KRIBLEGRABLI");
            fail("The returned Set should be immutable.");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    /**
     * @tests java.security.cert.X509CertSelector#getIssuer()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getIssuer",
        args = {}
    )
    public void test_getIssuer() {
        X500Principal iss1 = new X500Principal("O=First Org.");
        X500Principal iss2 = new X500Principal("O=Second Org.");
        X509CertSelector selector = new X509CertSelector();

        assertNull("Selector should return null", selector.getIssuer());
        selector.setIssuer(iss1);
        assertEquals("The returned issuer should be equal to specified", iss1,
                selector.getIssuer());
        assertFalse("The returned issuer should differ", iss2.equals(selector
                .getIssuer()));
    }

    /**
     * @tests java.security.cert.X509CertSelector#getIssuerAsBytes()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getIssuerAsBytes",
        args = {}
    )
    public void test_getIssuerAsBytes() {
        byte[] name1 = new byte[]
        // manually obtained DER encoding of "O=First Org." issuer name;
        { 48, 21, 49, 19, 48, 17, 6, 3, 85, 4, 10, 19, 10, 70, 105, 114, 115,
                116, 32, 79, 114, 103, 46 };

        byte[] name2 = new byte[]
        // manually obtained DER encoding of "O=Second Org." issuer name;
        { 48, 22, 49, 20, 48, 18, 6, 3, 85, 4, 10, 19, 11, 83, 101, 99, 111,
                110, 100, 32, 79, 114, 103, 46 };
        X500Principal iss1 = new X500Principal(name1);
        X500Principal iss2 = new X500Principal(name2);
        X509CertSelector selector = new X509CertSelector();

        try {
            assertNull("Selector should return null", selector
                    .getIssuerAsBytes());
            selector.setIssuer(iss1);
            assertTrue("The returned issuer should be equal to specified",
                    Arrays.equals(name1, selector.getIssuerAsBytes()));
            assertFalse("The returned issuer should differ", name2
                    .equals(selector.getIssuerAsBytes()));
            selector.setIssuer(iss2);
            assertTrue("The returned issuer should be equal to specified",
                    Arrays.equals(name2, selector.getIssuerAsBytes()));
        } catch (IOException e) {
            fail("Unexpected IOException was thrown.");
        }
    }

    /**
     * @tests java.security.cert.X509CertSelector#getIssuerAsString()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getIssuerAsString",
        args = {}
    )
    public void test_getIssuerAsString() {
        String name1 = "O=First Org.";
        String name2 = "O=Second Org.";
        X500Principal iss1 = new X500Principal(name1);
        X500Principal iss2 = new X500Principal(name2);
        X509CertSelector selector = new X509CertSelector();

        assertNull("Selector should return null", selector.getIssuerAsString());
        selector.setIssuer(iss1);
        assertEquals("The returned issuer should be equal to specified", name1,
                selector.getIssuerAsString());
        assertFalse("The returned issuer should differ", name2.equals(selector
                .getIssuerAsString()));
        selector.setIssuer(iss2);
        assertEquals("The returned issuer should be equal to specified", name2,
                selector.getIssuerAsString());
    }

    /**
     * @tests java.security.cert.X509CertSelector#getKeyUsage()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getKeyUsage",
        args = {}
    )
    public void test_getKeyUsage() {
        boolean[] ku = new boolean[] { true, false, true, false, true, false,
                true, false, true };
        X509CertSelector selector = new X509CertSelector();

        assertNull("Selector should return null", selector.getKeyUsage());
        selector.setKeyUsage(ku);
        assertTrue("The returned date should be equal to specified", Arrays
                .equals(ku, selector.getKeyUsage()));
        boolean[] result = selector.getKeyUsage();
        result[0] = !result[0];
        assertTrue("The returned keyUsage should be equal to specified", Arrays
                .equals(ku, selector.getKeyUsage()));
    }

    /**
     * @tests java.security.cert.X509CertSelector#getMatchAllSubjectAltNames()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getMatchAllSubjectAltNames",
        args = {}
    )
    public void test_getMatchAllSubjectAltNames() {
        X509CertSelector selector = new X509CertSelector();
        assertTrue("The matchAllNames initially should be true", selector
                .getMatchAllSubjectAltNames());
        selector.setMatchAllSubjectAltNames(false);
        assertFalse("The value should be false", selector
                .getMatchAllSubjectAltNames());
    }

    /**
     * @tests java.security.cert.X509CertSelector#getNameConstraints()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getNameConstraints",
        args = {}
    )
    public void test_getNameConstraints() throws IOException {
        GeneralName[] name_constraints = new GeneralName[] {
                new GeneralName(1, "822.Name"),
                new GeneralName(1, "rfc@822.Name"),
                new GeneralName(2, "Name.org"),
                new GeneralName(2, "dNS.Name.org"),

                new GeneralName(6, "http://Resource.Id"),
                new GeneralName(6, "http://uniform.Resource.Id"),
                new GeneralName(7, "1.1.1.1"),

                new GeneralName(new byte[] { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                        1, 1, 1, 1, 1 }), };

        X509CertSelector selector = new X509CertSelector();

        for (int i = 0; i < name_constraints.length; i++) {
            GeneralSubtree subtree = new GeneralSubtree(name_constraints[i]);
            GeneralSubtrees subtrees = new GeneralSubtrees();
            subtrees.addSubtree(subtree);
            NameConstraints constraints = new NameConstraints(subtrees,
                    subtrees);
            selector.setNameConstraints(constraints.getEncoded());
            assertTrue(Arrays.equals(constraints.getEncoded(), selector
                    .getNameConstraints()));
        }
    }

    /**
     * @tests java.security.cert.X509CertSelector#getPathToNames()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getPathToNames",
        args = {}
    )
    public void test_getPathToNames() {
        try {
            GeneralName san0 = new GeneralName(new OtherName("1.2.3.4.5",
                    new byte[] { 1, 2, 0, 1 }));
            GeneralName san1 = new GeneralName(1, "rfc@822.Name");
            GeneralName san2 = new GeneralName(2, "dNSName");
            GeneralName san3 = new GeneralName(new ORAddress());
            GeneralName san4 = new GeneralName(new Name("O=Organization"));
            GeneralName san6 = new GeneralName(6, "http://uniform.Resource.Id");
            GeneralName san7 = new GeneralName(7, "1.1.1.1");
            GeneralName san8 = new GeneralName(8, "1.2.3.4444.55555");

            GeneralNames sans1 = new GeneralNames();
            sans1.addName(san0);
            sans1.addName(san1);
            sans1.addName(san2);
            sans1.addName(san3);
            sans1.addName(san4);
            sans1.addName(san6);
            sans1.addName(san7);
            sans1.addName(san8);
            GeneralNames sans2 = new GeneralNames();
            sans2.addName(san0);

            TestCert cert1 = new TestCert(sans1);
            TestCert cert2 = new TestCert(sans2);
            X509CertSelector selector = new X509CertSelector();
            selector.setMatchAllSubjectAltNames(true);

            selector.setPathToNames(null);
            assertTrue("Any certificate should match in the case of null "
                    + "subjectAlternativeNames criteria.", selector
                    .match(cert1)
                    && selector.match(cert2));

            Collection<List<?>> sans = sans1.getPairsList();

            selector.setPathToNames(sans);

            Collection<List<?>> col = selector.getPathToNames();
            Iterator<List<?>> i = col.iterator();
            while (i.hasNext()) {
                Object o = i.next();
                if (!(o instanceof List)) {
                    fail("expected a List");
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
            fail("Unexpected IOException was thrown.");
        }
    }

    /**
     * @tests java.security.cert.X509CertSelector#getPolicy()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getPolicy",
        args = {}
    )
    public void test_getPolicy() throws IOException {
        String[] policies1 = new String[] { "1.3.6.1.5.5.7.3.1",
                "1.3.6.1.5.5.7.3.2", "1.3.6.1.5.5.7.3.3", "1.3.6.1.5.5.7.3.4",
                "1.3.6.1.5.5.7.3.8", "1.3.6.1.5.5.7.3.9", "1.3.6.1.5.5.7.3.5",
                "1.3.6.1.5.5.7.3.6", "1.3.6.1.5.5.7.3.7" };

        String[] policies2 = new String[] { "1.3.6.7.3.1" };

        HashSet<String> p1 = new HashSet<String>(Arrays.asList(policies1));
        HashSet<String> p2 = new HashSet<String>(Arrays.asList(policies2));

        X509CertSelector selector = new X509CertSelector();

        selector.setPolicy(null);
        assertNull(selector.getPolicy());

        selector.setPolicy(p1);
        assertEquals("The returned date should be equal to specified", p1, selector.getPolicy());
        
        selector.setPolicy(p2);
        assertEquals("The returned date should be equal to specified", p2, selector.getPolicy());
    }

    /**
     * @tests java.security.cert.X509CertSelector#getPrivateKeyValid()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getPrivateKeyValid",
        args = {}
    )
    public void test_getPrivateKeyValid() {
        Date date1 = new Date(100);
        Date date2 = new Date(200);
        X509CertSelector selector = new X509CertSelector();

        assertNull("Selector should return null", selector.getPrivateKeyValid());
        selector.setPrivateKeyValid(date1);
        assertTrue("The returned date should be equal to specified", date1
                .equals(selector.getPrivateKeyValid()));
        selector.getPrivateKeyValid().setTime(200);
        assertTrue("The returned date should be equal to specified", date1
                .equals(selector.getPrivateKeyValid()));
        assertFalse("The returned date should differ", date2.equals(selector
                .getPrivateKeyValid()));
    }

    /**
     * @tests java.security.cert.X509CertSelector#getSerialNumber()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getSerialNumber",
        args = {}
    )
    public void test_getSerialNumber() {
        BigInteger ser1 = new BigInteger("10000");
        BigInteger ser2 = new BigInteger("10001");
        X509CertSelector selector = new X509CertSelector();

        assertNull("Selector should return null", selector.getSerialNumber());
        selector.setSerialNumber(ser1);
        assertEquals("The returned serial number should be equal to specified",
                ser1, selector.getSerialNumber());
        assertFalse("The returned serial number should differ", ser2
                .equals(selector.getSerialNumber()));
    }

    /**
     * @tests java.security.cert.X509CertSelector#getSubject()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getSubject",
        args = {}
    )
    public void test_getSubject() {
        X500Principal sub1 = new X500Principal("O=First Org.");
        X500Principal sub2 = new X500Principal("O=Second Org.");
        X509CertSelector selector = new X509CertSelector();

        assertNull("Selector should return null", selector.getSubject());
        selector.setSubject(sub1);
        assertEquals("The returned subject should be equal to specified", sub1,
                selector.getSubject());
        assertFalse("The returned subject should differ", sub2.equals(selector
                .getSubject()));
    }

    /**
     * @tests java.security.cert.X509CertSelector#getSubjectAlternativeNames()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getSubjectAlternativeNames",
        args = {}
    )
    public void test_getSubjectAlternativeNames() {
        try {
            GeneralName san1 = new GeneralName(1, "rfc@822.Name");
            GeneralName san2 = new GeneralName(2, "dNSName");

            GeneralNames sans = new GeneralNames();
            sans.addName(san1);
            sans.addName(san2);

            TestCert cert_1 = new TestCert(sans);
            X509CertSelector selector = new X509CertSelector();

            assertNull("Selector should return null", selector
                    .getSubjectAlternativeNames());

            selector.setSubjectAlternativeNames(sans.getPairsList());
            assertTrue("The certificate should match the selection criteria.",
                    selector.match(cert_1));
            selector.getSubjectAlternativeNames().clear();
            assertTrue("The modification of initialization object "
                    + "should not affect the modification "
                    + "of internal object.", selector.match(cert_1));
        } catch (IOException e) {
            e.printStackTrace();
            fail("Unexpected IOException was thrown.");
        }
    }

    /**
     * @tests java.security.cert.X509CertSelector#getSubjectAsBytes()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getSubjectAsBytes",
        args = {}
    )
    public void test_getSubjectAsBytes() {
        byte[] name1 = new byte[]
        // manually obtained DER encoding of "O=First Org." issuer name;
        { 48, 21, 49, 19, 48, 17, 6, 3, 85, 4, 10, 19, 10, 70, 105, 114, 115,
                116, 32, 79, 114, 103, 46 };
        byte[] name2 = new byte[]
        // manually obtained DER encoding of "O=Second Org." issuer name;
        { 48, 22, 49, 20, 48, 18, 6, 3, 85, 4, 10, 19, 11, 83, 101, 99, 111,
                110, 100, 32, 79, 114, 103, 46 };

        X500Principal sub1 = new X500Principal(name1);
        X500Principal sub2 = new X500Principal(name2);
        X509CertSelector selector = new X509CertSelector();

        try {
            assertNull("Selector should return null", selector
                    .getSubjectAsBytes());
            selector.setSubject(sub1);
            assertTrue("The returned issuer should be equal to specified",
                    Arrays.equals(name1, selector.getSubjectAsBytes()));
            assertFalse("The returned issuer should differ", name2
                    .equals(selector.getSubjectAsBytes()));
            selector.setSubject(sub2);
            assertTrue("The returned issuer should be equal to specified",
                    Arrays.equals(name2, selector.getSubjectAsBytes()));
        } catch (IOException e) {
            fail("Unexpected IOException was thrown.");
        }
    }

    /**
     * @tests java.security.cert.X509CertSelector#getSubjectAsString()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getSubjectAsString",
        args = {}
    )
    public void test_getSubjectAsString() {
        String name1 = "O=First Org.";
        String name2 = "O=Second Org.";
        X500Principal sub1 = new X500Principal(name1);
        X500Principal sub2 = new X500Principal(name2);
        X509CertSelector selector = new X509CertSelector();

        assertNull("Selector should return null", selector.getSubjectAsString());
        selector.setSubject(sub1);
        assertEquals("The returned subject should be equal to specified",
                name1, selector.getSubjectAsString());
        assertFalse("The returned subject should differ", name2.equals(selector
                .getSubjectAsString()));
        selector.setSubject(sub2);
        assertEquals("The returned subject should be equal to specified",
                name2, selector.getSubjectAsString());
    }

    /**
     * @tests java.security.cert.X509CertSelector#getSubjectKeyIdentifier()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getSubjectKeyIdentifier",
        args = {}
    )
    public void test_getSubjectKeyIdentifier() {
        byte[] skid1 = new byte[] { 1, 2, 3, 4, 5 }; // random value
        byte[] skid2 = new byte[] { 4, 5, 5, 4, 3, 2, 1 }; // random value
        X509CertSelector selector = new X509CertSelector();

        assertNull("Selector should return null", selector
                .getSubjectKeyIdentifier());
        selector.setSubjectKeyIdentifier(skid1);
        assertTrue("The returned keyID should be equal to specified", Arrays
                .equals(skid1, selector.getSubjectKeyIdentifier()));
        selector.getSubjectKeyIdentifier()[0]++;
        assertTrue("The returned keyID should be equal to specified", Arrays
                .equals(skid1, selector.getSubjectKeyIdentifier()));
        assertFalse("The returned keyID should differ", Arrays.equals(skid2,
                selector.getSubjectKeyIdentifier()));
    }

    /**
     * @tests java.security.cert.X509CertSelector#getSubjectPublicKey()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getSubjectPublicKey",
        args = {}
    )
    public void test_getSubjectPublicKey() throws Exception {

        // SubjectPublicKeyInfo ::= SEQUENCE {
        // algorithm AlgorithmIdentifier,
        // subjectPublicKey BIT STRING }
        byte[] enc = { 0x30, 0x0E, // SEQUENCE
                0x30, 0x07, // SEQUENCE
                0x06, 0x02, 0x03, 0x05,// OID
                0x01, 0x01, 0x07, // ANY
                0x03, 0x03, 0x01, 0x01, 0x06, // subjectPublicKey
        };

        X509CertSelector selector = new X509CertSelector();

        selector.setSubjectPublicKey(enc);
        PublicKey key = selector.getSubjectPublicKey();
        assertEquals("0.3.5", key.getAlgorithm());
        assertEquals("X.509", key.getFormat());
        assertTrue(Arrays.equals(enc, key.getEncoded()));
        assertNotNull(key.toString());

        key = new MyPublicKey();

        selector.setSubjectPublicKey(key);
        PublicKey keyActual = selector.getSubjectPublicKey();
        assertEquals(key, keyActual);
        assertEquals(key.getAlgorithm(), keyActual.getAlgorithm());
    }

    /**
     * @tests java.security.cert.X509CertSelector#getSubjectPublicKeyAlgID()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getSubjectPublicKeyAlgID",
        args = {}
    )
    public void test_getSubjectPublicKeyAlgID() {

        X509CertSelector selector = new X509CertSelector();
        String[] validOIDs = { "0.0.20", "1.25.0", "2.0.39", "0.2.10", "1.35.15",
                "2.17.89" };

        assertNull("Selector should return null", selector
                .getSubjectPublicKeyAlgID());

        for (int i = 0; i < validOIDs.length; i++) {
            try {
                selector.setSubjectPublicKeyAlgID(validOIDs[i]);
                assertEquals(validOIDs[i], selector.getSubjectPublicKeyAlgID());
            } catch (IOException e) {
                System.out.println("t = " + e.getMessage());
                //fail("Unexpected exception " + e.getMessage());
            }
        }

        String pkaid1 = "1.2.840.113549.1.1.1"; // RSA encryption (source:
        // http://asn1.elibel.tm.fr)
        String pkaid2 = "1.2.840.113549.1.1.2"; // MD2 with RSA encryption
        // (source:
        // http://asn1.elibel.tm.fr)

        try {
            selector.setSubjectPublicKeyAlgID(pkaid1);
        } catch (IOException e) {
            fail("Unexpected IOException was thrown.");
        }
        assertTrue("The returned oid should be equal to specified", pkaid1
                .equals(selector.getSubjectPublicKeyAlgID()));
        assertFalse("The returned oid should differ", pkaid2.equals(selector
                .getSubjectPublicKeyAlgID()));
    }

    /**
     * @tests java.security.cert.X509CertSelector#match(java.security.cert.Certificate)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "match",
        args = {java.security.cert.Certificate.class}
    )
    public void test_matchLjava_security_cert_Certificate()
            throws CertificateException {
        X509CertSelector selector = new X509CertSelector();
        assertFalse(selector.match(null));

        CertificateFactory certFact = CertificateFactory.getInstance("X509");
        X509Certificate cert1 = (X509Certificate) certFact
                .generateCertificate(new ByteArrayInputStream(TestUtils
                        .getX509Certificate_v3()));

        X509Certificate cert2 = (X509Certificate) certFact
                .generateCertificate(new ByteArrayInputStream(TestUtils
                        .getX509Certificate_v1()));

        selector.setCertificate(cert1);
        assertTrue(selector.match(cert1));
        assertFalse(selector.match(cert2));

        selector.setCertificate(cert2);
        assertFalse(selector.match(cert1));
        assertTrue(selector.match(cert2));
    }

    /**
     * @tests java.security.cert.X509CertSelector#setAuthorityKeyIdentifier(byte[])
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setAuthorityKeyIdentifier",
        args = {byte[].class}
    )
    public void test_setAuthorityKeyIdentifierLB$() throws CertificateException {
        X509CertSelector selector = new X509CertSelector();

        byte[] akid1 = new byte[] { 1, 2, 3, 4, 5 }; // random value
        byte[] akid2 = new byte[] { 5, 4, 3, 2, 1 }; // random value
        TestCert cert1 = new TestCert(akid1);
        TestCert cert2 = new TestCert(akid2);

        selector.setAuthorityKeyIdentifier(null);
        assertTrue("The certificate should match the selection criteria.",
                selector.match(cert1));
        assertTrue("The certificate should match the selection criteria.",
                selector.match(cert2));
        assertNull(selector.getAuthorityKeyIdentifier());

        selector.setAuthorityKeyIdentifier(akid1);
        assertTrue("The certificate should not match the selection criteria.",
                selector.match(cert1));
        assertFalse("The certificate should not match the selection criteria.",
                selector.match(cert2));
        selector.setAuthorityKeyIdentifier(akid2);
        assertFalse("The certificate should not match the selection criteria.",
                selector.match(cert1));
        assertTrue("The certificate should not match the selection criteria.",
                selector.match(cert2));

        akid2[0]++;
        assertTrue("The certificate should match the selection criteria.",
                selector.match(cert2));
    }

    /**
     * @tests java.security.cert.X509CertSelector#setBasicConstraints(int)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setBasicConstraints",
        args = {int.class}
    )
    public void test_setBasicConstraintsLint() {
        X509CertSelector selector = new X509CertSelector();
        int[] invalidValues = { -3, -4, -5, 1000000000 };
        for (int i = 0; i < invalidValues.length; i++) {
            try {
                selector.setBasicConstraints(-3);
                fail("IllegalArgumentException expected");
            } catch (IllegalArgumentException e) {
                // expected
            }
        }

        int[] validValues = { -2, -1, 0, 1, 2, 3, 10, 20 };
        for (int i = 0; i < validValues.length; i++) {
            selector.setBasicConstraints(validValues[i]);
            assertEquals(validValues[i], selector.getBasicConstraints());
        }
    }

    /**
     * @tests java.security.cert.X509CertSelector#setCertificate(java.security.cert.Certificate)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setCertificate",
        args = {java.security.cert.X509Certificate.class}
    )
    public void test_setCertificateLjava_security_cert_X509Certificate()
            throws CertificateException {

        TestCert cert1 = new TestCert("same certificate");
        TestCert cert2 = new TestCert("other certificate");
        X509CertSelector selector = new X509CertSelector();

        selector.setCertificate(null);
        assertTrue("Any certificates should match in the case of null "
                + "certificateEquals criteria.", selector.match(cert1)
                && selector.match(cert2));
        selector.setCertificate(cert1);
        assertTrue("The certificate should match the selection criteria.",
                selector.match(cert1));
        assertFalse("The certificate should not match the selection criteria.",
                selector.match(cert2));
        selector.setCertificate(cert2);
        assertTrue("The certificate should match the selection criteria.",
                selector.match(cert2));
        selector.setCertificate(null);
        assertNull(selector.getCertificate());
    }

    /**
     * @tests java.security.cert.X509CertSelector#setCertificateValid(java.util.Date)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setCertificateValid",
        args = {java.util.Date.class}
    )
    public void test_setCertificateValidLjava_util_Date()
            throws CertificateException {
        X509CertSelector selector = new X509CertSelector();

        Date date1 = new Date(100);
        Date date2 = new Date(200);
        TestCert cert1 = new TestCert(date1);
        TestCert cert2 = new TestCert(date2);

        selector.setCertificateValid(null);
        assertNull(selector.getCertificateValid());
        selector.setCertificateValid(date1);
        assertTrue("The certificate should match the selection criteria.",
                selector.match(cert1));
        assertFalse("The certificate should not match the selection criteria.",
                selector.match(cert2));
        selector.setCertificateValid(date2);
        date2.setTime(300);
        assertTrue("The certificate should match the selection criteria.",
                selector.match(cert2));
    }

    /**
     * @tests java.security.cert.X509CertSelector#setExtendedKeyUsage(Set<String>)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setExtendedKeyUsage",
        args = {java.util.Set.class}
    )
    public void test_setExtendedKeyUsageLjava_util_Set()
            throws CertificateException {
        HashSet<String> ku1 = new HashSet<String>(Arrays
                .asList(new String[] { "1.3.6.1.5.5.7.3.1",
                        "1.3.6.1.5.5.7.3.2", "1.3.6.1.5.5.7.3.3",
                        "1.3.6.1.5.5.7.3.4", "1.3.6.1.5.5.7.3.8",
                        "1.3.6.1.5.5.7.3.9", "1.3.6.1.5.5.7.3.5",
                        "1.3.6.1.5.5.7.3.6", "1.3.6.1.5.5.7.3.7" }));
        HashSet<String> ku2 = new HashSet<String>(Arrays.asList(new String[] {
                "1.3.6.1.5.5.7.3.1", "1.3.6.1.5.5.7.3.2", "1.3.6.1.5.5.7.3.3",
                "1.3.6.1.5.5.7.3.4", "1.3.6.1.5.5.7.3.8", "1.3.6.1.5.5.7.3.9",
                "1.3.6.1.5.5.7.3.5", "1.3.6.1.5.5.7.3.6" }));
        TestCert cert1 = new TestCert(ku1);
        TestCert cert2 = new TestCert(ku2);

        X509CertSelector selector = new X509CertSelector();

        try {
            selector.setExtendedKeyUsage(null);
        } catch (IOException e) {
            fail("Unexpected IOException was thrown.");
        }
        assertTrue("Any certificate should match in the case of null "
                + "extendedKeyUsage criteria.", selector.match(cert1)
                && selector.match(cert2));
        try {
            selector.setExtendedKeyUsage(ku1);
        } catch (IOException e) {
            fail("Unexpected IOException was thrown.");
        }
        assertEquals(ku1, selector.getExtendedKeyUsage());

        try {
            selector.setExtendedKeyUsage(ku2);
        } catch (IOException e) {
            fail("Unexpected IOException was thrown.");
        }
        assertEquals(ku2, selector.getExtendedKeyUsage());
    }

    /**
     * @tests java.security.cert.X509CertSelector#setIssuer(byte[])
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setIssuer",
        args = {byte[].class}
    )
    public void test_setIssuerLB$() throws CertificateException {
        byte[] name1 = new byte[]
        // manually obtained DER encoding of "O=First Org." issuer name;
        { 48, 21, 49, 19, 48, 17, 6, 3, 85, 4, 10, 19, 10, 70, 105, 114, 115,
                116, 32, 79, 114, 103, 46 };
        byte[] name2 = new byte[]
        // manually obtained DER encoding of "O=Second Org." issuer name;
        { 48, 22, 49, 20, 48, 18, 6, 3, 85, 4, 10, 19, 11, 83, 101, 99, 111,
                110, 100, 32, 79, 114, 103, 46 };
        X500Principal iss1 = new X500Principal(name1);
        X500Principal iss2 = new X500Principal(name2);
        TestCert cert1 = new TestCert(iss1);
        TestCert cert2 = new TestCert(iss2);

        X509CertSelector selector = new X509CertSelector();

        try {
            selector.setIssuer((byte[]) null);
        } catch (IOException e) {
            fail("Unexpected IOException was thrown.");
        }
        assertTrue("Any certificates should match "
                + "in the case of null issuer criteria.", selector.match(cert1)
                && selector.match(cert2));
        try {
            selector.setIssuer(name1);
        } catch (IOException e) {
            fail("Unexpected IOException was thrown.");
        }
        assertTrue("The certificate should match the selection criteria.",
                selector.match(cert1));
        assertFalse("The certificate should not match the selection criteria.",
                selector.match(cert2));
        try {
            selector.setIssuer(name2);
        } catch (IOException e) {
            fail("Unexpected IOException was thrown.");
        }
        assertTrue("The certificate should match the selection criteria.",
                selector.match(cert2));
    }

    /**
     * @tests java.security.cert.X509CertSelector#setIssuer(java.lang.String)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setIssuer",
        args = {java.lang.String.class}
    )
    public void test_setIssuerLjava_lang_String() throws CertificateException {

        String name1 = "O=First Org.";
        String name2 = "O=Second Org.";
        X500Principal iss1 = new X500Principal(name1);
        X500Principal iss2 = new X500Principal(name2);
        TestCert cert1 = new TestCert(iss1);
        TestCert cert2 = new TestCert(iss2);

        X509CertSelector selector = new X509CertSelector();

        try {
            selector.setIssuer((String) null);
        } catch (IOException e) {
            fail("Unexpected IOException was thrown.");
        }
        assertTrue("Any certificates should match "
                + "in the case of null issuer criteria.", selector.match(cert1)
                && selector.match(cert2));
        try {
            selector.setIssuer(name1);
        } catch (IOException e) {
            fail("Unexpected IOException was thrown.");
        }
        assertTrue("The certificate should match the selection criteria.",
                selector.match(cert1));
        assertFalse("The certificate should not match the selection criteria.",
                selector.match(cert2));
        try {
            selector.setIssuer(name2);
        } catch (IOException e) {
            fail("Unexpected IOException was thrown.");
        }
        assertTrue("The certificate should match the selection criteria.",
                selector.match(cert2));
    }

    /**
     * @tests java.security.cert.X509CertSelector#setIssuer(javax.security.auth.x500.X500Principal)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setIssuer",
        args = {javax.security.auth.x500.X500Principal.class}
    )
    public void test_setIssuerLjavax_security_auth_x500_X500Principal()
            throws CertificateException {
        X500Principal iss1 = new X500Principal("O=First Org.");
        X500Principal iss2 = new X500Principal("O=Second Org.");
        TestCert cert1 = new TestCert(iss1);
        TestCert cert2 = new TestCert(iss2);
        X509CertSelector selector = new X509CertSelector();

        selector.setIssuer((X500Principal) null);
        assertTrue("Any certificates should match "
                + "in the case of null issuer criteria.", selector.match(cert1)
                && selector.match(cert2));
        selector.setIssuer(iss1);
        assertTrue("The certificate should match the selection criteria.",
                selector.match(cert1));
        assertFalse("The certificate should not match the selection criteria.",
                selector.match(cert2));
        selector.setIssuer(iss2);
        assertTrue("The certificate should match the selection criteria.",
                selector.match(cert2));
    }

    /**
     * @tests java.security.cert.X509CertSelector#setKeyUsage(boolean)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setKeyUsage",
        args = {boolean[].class}
    )
    public void test_setKeyUsageZ() throws CertificateException {
        boolean[] ku1 = new boolean[] { true, true, true, true, true, true,
                true, true, true };
        // decipherOnly is disallowed
        boolean[] ku2 = new boolean[] { true, true, true, true, true, true,
                true, true, false };
        TestCert cert1 = new TestCert(ku1);
        TestCert cert2 = new TestCert(ku2);
        TestCert cert3 = new TestCert((boolean[]) null);

        X509CertSelector selector = new X509CertSelector();

        selector.setKeyUsage(null);
        assertTrue("Any certificate should match in the case of null "
                + "keyUsage criteria.", selector.match(cert1)
                && selector.match(cert2));
        selector.setKeyUsage(ku1);
        assertTrue("The certificate should match the selection criteria.",
                selector.match(cert1));
        assertFalse("The certificate should not match the selection criteria.",
                selector.match(cert2));
        assertTrue("The certificate which does not have a keyUsage extension "
                + "implicitly allows all keyUsage values.", selector
                .match(cert3));
        selector.setKeyUsage(ku2);
        ku2[0] = !ku2[0];
        assertTrue("The certificate should match the selection criteria.",
                selector.match(cert2));
    }

    /**
     * @tests java.security.cert.X509CertSelector#setMatchAllSubjectAltNames(boolean)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setMatchAllSubjectAltNames",
        args = {boolean.class}
    )
    public void test_setMatchAllSubjectAltNamesZ() {
        TestCert cert = new TestCert();
        X509CertSelector selector = new X509CertSelector();

        assertTrue(selector.match(cert));

        assertFalse(selector.match(null));
    }

    /**
     * @tests java.security.cert.X509CertSelector#setNameConstraints(byte[]
     *        bytes)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setNameConstraints",
        args = {byte[].class}
    )
    public void test_setNameConstraintsLB$() throws IOException {
        GeneralName[] name_constraints = new GeneralName[] {
                new GeneralName(1, "822.Name"),
                new GeneralName(1, "rfc@822.Name"),
                new GeneralName(2, "Name.org"),
                new GeneralName(2, "dNS.Name.org"),

                new GeneralName(6, "http://Resource.Id"),
                new GeneralName(6, "http://uniform.Resource.Id"),
                new GeneralName(7, "1.1.1.1"),

                new GeneralName(new byte[] { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                        1, 1, 1, 1, 1 }), };

        X509CertSelector selector = new X509CertSelector();

        for (int i = 0; i < name_constraints.length; i++) {
            GeneralSubtree subtree = new GeneralSubtree(name_constraints[i]);
            GeneralSubtrees subtrees = new GeneralSubtrees();
            subtrees.addSubtree(subtree);
            NameConstraints constraints = new NameConstraints(subtrees,
                    subtrees);
            selector.setNameConstraints(constraints.getEncoded());
            assertTrue(Arrays.equals(constraints.getEncoded(), selector
                    .getNameConstraints()));
        }
    }

    /**
     * @tests java.security.cert.X509CertSelector#setPathToNames(Collection<List<?>>)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setPathToNames",
        args = {java.util.Collection.class}
    )
    public void test_setPathToNamesLjava_util_Collection() {
        try {
            GeneralName san0 = new GeneralName(new OtherName("1.2.3.4.5",
                    new byte[] { 1, 2, 0, 1 }));
            GeneralName san1 = new GeneralName(1, "rfc@822.Name");
            GeneralName san2 = new GeneralName(2, "dNSName");
            GeneralName san3 = new GeneralName(new ORAddress());
            GeneralName san4 = new GeneralName(new Name("O=Organization"));
            GeneralName san6 = new GeneralName(6, "http://uniform.Resource.Id");
            GeneralName san7 = new GeneralName(7, "1.1.1.1");
            GeneralName san8 = new GeneralName(8, "1.2.3.4444.55555");

            GeneralNames sans1 = new GeneralNames();
            sans1.addName(san0);
            sans1.addName(san1);
            sans1.addName(san2);
            sans1.addName(san3);
            sans1.addName(san4);
            sans1.addName(san6);
            sans1.addName(san7);
            sans1.addName(san8);
            GeneralNames sans2 = new GeneralNames();
            sans2.addName(san0);

            TestCert cert1 = new TestCert(sans1);
            TestCert cert2 = new TestCert(sans2);
            X509CertSelector selector = new X509CertSelector();
            selector.setMatchAllSubjectAltNames(true);

            selector.setPathToNames(null);
            assertTrue("Any certificate should match in the case of null "
                    + "subjectAlternativeNames criteria.", selector
                    .match(cert1)
                    && selector.match(cert2));

            Collection<List<?>> sans = sans1.getPairsList();

            selector.setPathToNames(sans);

            Collection<List<?>> col = selector.getPathToNames();
            Iterator<List<?>> i = col.iterator();
            while (i.hasNext()) {
                Object o = i.next();
                if (!(o instanceof List)) {
                    fail("expected a List");
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
            fail("Unexpected IOException was thrown.");
        }
    }

    /**
     * @tests java.security.cert.X509CertSelector#setPolicy(Set<String>)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setPolicy",
        args = {java.util.Set.class}
    )
    public void test_setPolicyLjava_util_Set() throws IOException {
        String[] policies1 = new String[] { "1.3.6.1.5.5.7.3.1",
                "1.3.6.1.5.5.7.3.2", "1.3.6.1.5.5.7.3.3", "1.3.6.1.5.5.7.3.4",
                "1.3.6.1.5.5.7.3.8", "1.3.6.1.5.5.7.3.9", "1.3.6.1.5.5.7.3.5",
                "1.3.6.1.5.5.7.3.6", "1.3.6.1.5.5.7.3.7" };

        String[] policies2 = new String[] { "1.3.6.7.3.1" };

        HashSet<String> p1 = new HashSet<String>(Arrays.asList(policies1));
        HashSet<String> p2 = new HashSet<String>(Arrays.asList(policies2));

        X509CertSelector selector = new X509CertSelector();

        TestCert cert1 = new TestCert(policies1);
        TestCert cert2 = new TestCert(policies2);

        selector.setPolicy(null);
        assertTrue("Any certificate should match in the case of null "
                + "privateKeyValid criteria.", selector.match(cert1)
                && selector.match(cert2));

        selector.setPolicy(p1);
        assertTrue("The certificate should match the selection criteria.",
                selector.match(cert1));
        assertFalse("The certificate should not match the selection criteria.",
                selector.match(cert2));
        
        selector.setPolicy(p2);
        assertFalse("The certificate should not match the selection criteria.",
                selector.match(cert1));
        assertTrue("The certificate should match the selection criteria.",
                selector.match(cert2));
    }

    /**
     * @tests java.security.cert.X509CertSelector#setPrivateKeyValid(java.util.Date)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setPrivateKeyValid",
        args = {java.util.Date.class}
    )
    public void test_setPrivateKeyValidLjava_util_Date()
            throws CertificateException {
        Date date1 = new Date(100000000);
        Date date2 = new Date(200000000);
        Date date3 = new Date(300000000);
        Date date4 = new Date(150000000);
        Date date5 = new Date(250000000);
        TestCert cert1 = new TestCert(date1, date2);
        TestCert cert2 = new TestCert(date2, date3);

        X509CertSelector selector = new X509CertSelector();

        selector.setPrivateKeyValid(null);
        assertTrue("Any certificate should match in the case of null "
                + "privateKeyValid criteria.", selector.match(cert1)
                && selector.match(cert2));
        selector.setPrivateKeyValid(date4);
        assertTrue("The certificate should match the selection criteria.",
                selector.match(cert1));
        assertFalse("The certificate should not match the selection criteria.",
                selector.match(cert2));
        selector.setPrivateKeyValid(date5);
        date5.setTime(date4.getTime());
        assertTrue("The certificate should match the selection criteria.",
                selector.match(cert2));
    }

    /**
     * @tests java.security.cert.X509CertSelector#setSerialNumber(java.math.BigInteger)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setSerialNumber",
        args = {java.math.BigInteger.class}
    )
    public void test_setSerialNumberLjava_math_BigInteger()
            throws CertificateException {
        BigInteger ser1 = new BigInteger("10000");
        BigInteger ser2 = new BigInteger("10001");
        TestCert cert1 = new TestCert(ser1);
        TestCert cert2 = new TestCert(ser2);
        X509CertSelector selector = new X509CertSelector();

        selector.setSerialNumber(null);
        assertTrue("Any certificate should match in the case of null "
                + "serialNumber criteria.", selector.match(cert1)
                && selector.match(cert2));
        selector.setSerialNumber(ser1);
        assertTrue("The certificate should match the selection criteria.",
                selector.match(cert1));
        assertFalse("The certificate should not match the selection criteria.",
                selector.match(cert2));
        selector.setSerialNumber(ser2);
        assertTrue("The certificate should match the selection criteria.",
                selector.match(cert2));
    }

    /**
     * @tests java.security.cert.X509CertSelector#setSubject(byte[])
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setSubject",
        args = {byte[].class}
    )
    public void test_setSubjectLB$() throws CertificateException {
        byte[] name1 = new byte[]
        // manually obtained DER encoding of "O=First Org." issuer name;
        { 48, 21, 49, 19, 48, 17, 6, 3, 85, 4, 10, 19, 10, 70, 105, 114, 115,
                116, 32, 79, 114, 103, 46 };
        byte[] name2 = new byte[]
        // manually obtained DER encoding of "O=Second Org." issuer name;
        { 48, 22, 49, 20, 48, 18, 6, 3, 85, 4, 10, 19, 11, 83, 101, 99, 111,
                110, 100, 32, 79, 114, 103, 46 };
        X500Principal sub1 = new X500Principal(name1);
        X500Principal sub2 = new X500Principal(name2);
        TestCert cert1 = new TestCert(sub1);
        TestCert cert2 = new TestCert(sub2);

        X509CertSelector selector = new X509CertSelector();

        try {
            selector.setSubject((byte[]) null);
        } catch (IOException e) {
            fail("Unexpected IOException was thrown.");
        }
        assertTrue("Any certificates should match "
                + "in the case of null issuer criteria.", selector.match(cert1)
                && selector.match(cert2));
        try {
            selector.setSubject(name1);
        } catch (IOException e) {
            fail("Unexpected IOException was thrown.");
        }
        assertTrue("The certificate should match the selection criteria.",
                selector.match(cert1));
        assertFalse("The certificate should not match the selection criteria.",
                selector.match(cert2));
        try {
            selector.setSubject(name2);
        } catch (IOException e) {
            fail("Unexpected IOException was thrown.");
        }
        assertTrue("The certificate should match the selection criteria.",
                selector.match(cert2));
    }

    /**
     * @tests java.security.cert.X509CertSelector#setSubject(java.lang.String)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setSubject",
        args = {java.lang.String.class}
    )
    public void test_setSubjectLjava_lang_String() throws CertificateException {
        String name1 = "O=First Org.";
        String name2 = "O=Second Org.";
        X500Principal sub1 = new X500Principal(name1);
        X500Principal sub2 = new X500Principal(name2);
        TestCert cert1 = new TestCert(sub1);
        TestCert cert2 = new TestCert(sub2);
        X509CertSelector selector = new X509CertSelector();

        try {
            selector.setSubject((String) null);
        } catch (IOException e) {
            fail("Unexpected IOException was thrown.");
        }
        assertTrue("Any certificates should match "
                + "in the case of null subject criteria.", selector
                .match(cert1)
                && selector.match(cert2));
        try {
            selector.setSubject(name1);
        } catch (IOException e) {
            fail("Unexpected IOException was thrown.");
        }
        assertTrue("The certificate should match the selection criteria.",
                selector.match(cert1));
        assertFalse("The certificate should not match the selection criteria.",
                selector.match(cert2));
        try {
            selector.setSubject(name2);
        } catch (IOException e) {
            fail("Unexpected IOException was thrown.");
        }
        assertTrue("The certificate should match the selection criteria.",
                selector.match(cert2));
    }

    /**
     * @tests java.security.cert.X509CertSelector#setSubject(javax.security.auth.x500.X500Principal)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setSubject",
        args = {javax.security.auth.x500.X500Principal.class}
    )
    public void test_setSubjectLjavax_security_auth_x500_X500Principal()
            throws CertificateException {
        X500Principal sub1 = new X500Principal("O=First Org.");
        X500Principal sub2 = new X500Principal("O=Second Org.");
        TestCert cert1 = new TestCert(sub1);
        TestCert cert2 = new TestCert(sub2);
        X509CertSelector selector = new X509CertSelector();

        selector.setSubject((X500Principal) null);
        assertTrue("Any certificates should match "
                + "in the case of null subjcet criteria.", selector
                .match(cert1)
                && selector.match(cert2));
        selector.setSubject(sub1);
        assertTrue("The certificate should match the selection criteria.",
                selector.match(cert1));
        assertFalse("The certificate should not match the selection criteria.",
                selector.match(cert2));
        selector.setSubject(sub2);
        assertTrue("The certificate should match the selection criteria.",
                selector.match(cert2));
    }

    /**
     * @tests java.security.cert.X509CertSelector#setSubjectAlternativeNames(Collection<List<?>>)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setSubjectAlternativeNames",
        args = {java.util.Collection.class}
    )
    public void test_setSubjectAlternativeNamesLjava_util_Collection() {

        try {
            GeneralName san0 = new GeneralName(new OtherName("1.2.3.4.5",
                    new byte[] { 1, 2, 0, 1 }));
            GeneralName san1 = new GeneralName(1, "rfc@822.Name");
            GeneralName san2 = new GeneralName(2, "dNSName");
            GeneralName san3 = new GeneralName(new ORAddress());
            GeneralName san4 = new GeneralName(new Name("O=Organization"));
            GeneralName san6 = new GeneralName(6, "http://uniform.Resource.Id");
            GeneralName san7 = new GeneralName(7, "1.1.1.1");
            GeneralName san8 = new GeneralName(8, "1.2.3.4444.55555");

            GeneralNames sans1 = new GeneralNames();
            sans1.addName(san0);
            sans1.addName(san1);
            sans1.addName(san2);
            sans1.addName(san3);
            sans1.addName(san4);
            sans1.addName(san6);
            sans1.addName(san7);
            sans1.addName(san8);
            GeneralNames sans2 = new GeneralNames();
            sans2.addName(san0);

            TestCert cert1 = new TestCert(sans1);
            TestCert cert2 = new TestCert(sans2);
            X509CertSelector selector = new X509CertSelector();
            selector.setMatchAllSubjectAltNames(true);

            selector.setSubjectAlternativeNames(null);
            assertTrue("Any certificate should match in the case of null "
                    + "subjectAlternativeNames criteria.", selector
                    .match(cert1)
                    && selector.match(cert2));

            Collection<List<?>> sans = sans1.getPairsList();

            selector.setSubjectAlternativeNames(sans);

            Collection<List<?>> col = selector.getSubjectAlternativeNames();
            Iterator<List<?>> i = col.iterator();
            while (i.hasNext()) {
                Object o = i.next();
                if (!(o instanceof List)) {
                    fail("expected a List");
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
            fail("Unexpected IOException was thrown.");
        }
    }
    
    /**
     * @tests java.security.cert.X509CertSelector#setSubjectKeyIdentifier(byte[])
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setSubjectKeyIdentifier",
        args = {byte[].class}
    )
    public void test_setSubjectKeyIdentifierLB$() throws CertificateException {
        byte[] skid1 = new byte[] { 1, 2, 3, 4, 5 }; // random value
        byte[] skid2 = new byte[] { 5, 4, 3, 2, 1 }; // random value
        TestCert cert1 = new TestCert(skid1);
        TestCert cert2 = new TestCert(skid2);
        X509CertSelector selector = new X509CertSelector();

        selector.setSubjectKeyIdentifier(null);
        assertTrue("Any certificate should match in the case of null "
                + "serialNumber criteria.", selector.match(cert1)
                && selector.match(cert2));
        selector.setSubjectKeyIdentifier(skid1);
        assertTrue("The certificate should match the selection criteria.",
                selector.match(cert1));
        assertFalse("The certificate should not match the selection criteria.",
                selector.match(cert2));
        selector.setSubjectKeyIdentifier(skid2);
        skid2[0]++;
        assertTrue("The certificate should match the selection criteria.",
                selector.match(cert2));
    }

    /**
     * @tests java.security.cert.X509CertSelector#setSubjectPublicKey(byte[])
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setSubjectPublicKey",
        args = {byte[].class}
    )
    public void test_setSubjectPublicKeyLB$() throws Exception {

        //SubjectPublicKeyInfo  ::=  SEQUENCE  {
        //    algorithm            AlgorithmIdentifier,
        //    subjectPublicKey     BIT STRING  }
        byte[] enc = { 0x30, 0x0E, // SEQUENCE
                0x30, 0x07, // SEQUENCE
                0x06, 0x02, 0x03, 0x05,//OID
                0x01, 0x01, 0x07, //ANY
                0x03, 0x03, 0x01, 0x01, 0x06, // subjectPublicKey
        };

        X509CertSelector selector = new X509CertSelector();

        selector.setSubjectPublicKey(enc);
        PublicKey key = selector.getSubjectPublicKey();
        assertEquals("0.3.5", key.getAlgorithm());
        assertEquals("X.509", key.getFormat());
        assertTrue(Arrays.equals(enc, key.getEncoded()));
        assertNotNull(key.toString());
    }

    /**
     * @tests java.security.cert.X509CertSelector#setSubjectPublicKey(java.security.PublicKey key)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setSubjectPublicKey",
        args = {java.security.PublicKey.class}
    )
    public void test_setSubjectPublicKeyLjava_security_PublicKey()
            throws CertificateException {
        PublicKey pkey1 = null;
        PublicKey pkey2 = null;
        try {
            pkey1 = new TestKeyPair("RSA").getPublic();
            pkey2 = new TestKeyPair("DSA").getPublic();
        } catch (Exception e) {
            fail("Unexpected Exception was thrown: " + e.getMessage());
        }

        TestCert cert1 = new TestCert(pkey1);
        TestCert cert2 = new TestCert(pkey2);
        X509CertSelector selector = new X509CertSelector();

        selector.setSubjectPublicKey((PublicKey) null);
        assertTrue("Any certificate should match in the case of null "
                + "subjectPublicKey criteria.", selector.match(cert1)
                && selector.match(cert2));
        selector.setSubjectPublicKey(pkey1);
        assertTrue("The certificate should match the selection criteria.",
                selector.match(cert1));
        assertFalse("The certificate should not match the selection criteria.",
                selector.match(cert2));
        selector.setSubjectPublicKey(pkey2);
        assertTrue("The certificate should match the selection criteria.",
                selector.match(cert2));
    }

    /**
     * @tests java.security.cert.X509CertSelector#setSubjectPublicKeyAlgID(java.lang.String)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setSubjectPublicKeyAlgID",
        args = {java.lang.String.class}
    )
    public void test_setSubjectPublicKeyAlgIDLjava_lang_String()
            throws CertificateException {

        X509CertSelector selector = new X509CertSelector();
        String pkaid1 = "1.2.840.113549.1.1.1"; // RSA (source:
        // http://asn1.elibel.tm.fr)
        String pkaid2 = "1.2.840.10040.4.1"; // DSA (source:
        // http://asn1.elibel.tm.fr)
        PublicKey pkey1;
        PublicKey pkey2;
        try {
            pkey1 = new TestKeyPair("RSA").getPublic();
            pkey2 = new TestKeyPair("DSA").getPublic();
        } catch (Exception e) {
            e.printStackTrace();
            fail("Unexpected Exception was thrown: " + e.getMessage());
            return;
        }
        TestCert cert1 = new TestCert(pkey1);
        TestCert cert2 = new TestCert(pkey2);

        try {
            selector.setSubjectPublicKeyAlgID(null);
        } catch (IOException e) {

            fail("Unexpected IOException was thrown.");
        }
        assertTrue("Any certificate should match in the case of null "
                + "subjectPublicKeyAlgID criteria.", selector.match(cert1)
                && selector.match(cert2));

        String[] validOIDs = { "0.0.20", "1.25.0", "2.0.39", "0.2.10", "1.35.15",
                "2.17.89", "2.5.29.16", "2.5.29.17", "2.5.29.30", "2.5.29.32",
                "2.5.29.37" };

        for (int i = 0; i < validOIDs.length; i++) {
            try {
                selector.setSubjectPublicKeyAlgID(validOIDs[i]);
                assertEquals(validOIDs[i], selector.getSubjectPublicKeyAlgID());
            } catch (IOException e) {
                fail("Unexpected exception " + e.getMessage());
            }
        }
        
        String[] invalidOIDs = { "0.20", "1.25", "2.39", "3.10"};
        for (int i = 0; i < invalidOIDs.length; i++) {
            try {
                selector.setSubjectPublicKeyAlgID(invalidOIDs[i]);
                fail("IOException wasn't thrown for " + invalidOIDs[i]);
            } catch (IOException e) {
            }
        }

        try {
            selector.setSubjectPublicKeyAlgID(pkaid1);
        } catch (IOException e) {
            fail("Unexpected IOException was thrown.");
        }
        assertTrue("The certificate should match the selection criteria.",
                selector.match(cert1));
        assertFalse("The certificate should not match the selection criteria.",
                selector.match(cert2));
        try {
            selector.setSubjectPublicKeyAlgID(pkaid2);
        } catch (IOException e) {
            fail("Unexpected IOException was thrown.");
        }
        assertTrue("The certificate should match the selection criteria.",
                selector.match(cert2));
    }

    /**
     * @tests java.security.cert.X509CertSelector#toString()
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "toString",
        args = {}
    )
    public void test_toString() {
        X509CertSelector selector = new X509CertSelector();
        assertNotNull(selector.toString());
    }

    public class MyPublicKey implements PublicKey {
        private static final long serialVersionUID = 2899528375354645752L;

        public MyPublicKey() {
            super();
        }

        public String getAlgorithm() {
            return "PublicKey";
        }

        public String getFormat() {
            return "Format";
        }

        public byte[] getEncoded() {
            return new byte[0];
        }

        public long getSerVerUID() {
            return serialVersionUID;
        }
    }

    private class TestCert extends X509Certificate {

        private static final long serialVersionUID = 176676115254260405L;

        /* Stuff fields */
        protected String equalCriteria = null; // to simplify method equals()

        protected BigInteger serialNumber = null;

        protected X500Principal issuer = null;

        protected X500Principal subject = null;

        protected byte[] keyIdentifier = null;

        protected Date date = null;

        protected Date notBefore = null;

        protected Date notAfter = null;

        protected PublicKey key = null;

        protected boolean[] keyUsage = null;

        protected List<String> extKeyUsage = null;

        protected int pathLen = 1;

        protected GeneralNames sans = null;

        protected byte[] encoding = null;

        protected String[] policies = null;

        protected Collection<List<?>> collection = null;

        protected NameConstraints nameConstraints = null;

        /* Stuff methods */
        public TestCert() {
        }

        public TestCert(GeneralNames sans) {
            setSubjectAlternativeNames(sans);
        }

        public TestCert(NameConstraints nameConstraints) {
            this.nameConstraints = nameConstraints;
        }

        public TestCert(Collection<List<?>> collection) {
            setCollection(collection);
        }

        public TestCert(String equalCriteria) {
            setEqualCriteria(equalCriteria);
        }

        public TestCert(String[] policies) {
            setPolicies(policies);
        }

        public TestCert(BigInteger serial) {
            setSerialNumber(serial);
        }

        public TestCert(X500Principal principal) {
            setIssuer(principal);
            setSubject(principal);
        }

        public TestCert(byte[] array) {
            setKeyIdentifier(array);
        }

        public TestCert(Date date) {
            setDate(date);
        }

        public TestCert(Date notBefore, Date notAfter) {
            setPeriod(notBefore, notAfter);
        }

        public TestCert(PublicKey key) {
            setPublicKey(key);
        }

        public TestCert(boolean[] keyUsage) {
            setKeyUsage(keyUsage);
        }

        public TestCert(Set<String> extKeyUsage) {
            setExtendedKeyUsage(extKeyUsage);
        }

        public TestCert(int pathLen) {
            this.pathLen = pathLen;
        }

        public void setSubjectAlternativeNames(GeneralNames sans) {
            this.sans = sans;
        }

        public void setCollection(Collection<List<?>> collection) {
            this.collection = collection;
        }

        public void setPolicies(String[] policies) {
            this.policies = policies;
        }

        public void setExtendedKeyUsage(Set<String> extKeyUsage) {
            this.extKeyUsage = (extKeyUsage == null) ? null : new ArrayList<String>(
                    extKeyUsage);
        }

        public void setKeyUsage(boolean[] keyUsage) {
            this.keyUsage = (keyUsage == null) ? null : (boolean[]) keyUsage
                    .clone();
        }

        public void setPublicKey(PublicKey key) {
            this.key = key;
        }

        public void setPeriod(Date notBefore, Date notAfter) {
            this.notBefore = notBefore;
            this.notAfter = notAfter;
        }

        public void setSerialNumber(BigInteger serial) {
            this.serialNumber = serial;
        }

        public void setEqualCriteria(String equalCriteria) {
            this.equalCriteria = equalCriteria;
        }

        public void setIssuer(X500Principal issuer) {
            this.issuer = issuer;
        }

        public void setSubject(X500Principal subject) {
            this.subject = subject;
        }

        public void setKeyIdentifier(byte[] subjectKeyID) {
            this.keyIdentifier = (byte[]) subjectKeyID.clone();
        }

        public void setDate(Date date) {
            this.date = new Date(date.getTime());
        }

        public void setEncoding(byte[] encoding) {
            this.encoding = encoding;
        }

        /* Method implementations */
        public boolean equals(Object cert) {
            if (cert == null) {
                return false;
            }
            if ((equalCriteria == null)
                    || (((TestCert) cert).equalCriteria == null)) {
                return false;
            } else {
                return equalCriteria.equals(((TestCert) cert).equalCriteria);
            }
        }

        public String toString() {
            if (equalCriteria != null) {
                return equalCriteria;
            }
            return "";
        }

        public void checkValidity() throws CertificateExpiredException,
                CertificateNotYetValidException {
        }

        public void checkValidity(Date date)
                throws CertificateExpiredException,
                CertificateNotYetValidException {
            if (this.date == null) {
                throw new CertificateExpiredException();
            }
            int result = this.date.compareTo(date);
            if (result > 0) {
                throw new CertificateExpiredException();
            }
            if (result < 0) {
                throw new CertificateNotYetValidException();
            }
        }

        public int getVersion() {
            return 3;
        }

        public BigInteger getSerialNumber() {
            return (serialNumber == null) ? new BigInteger("1111")
                    : serialNumber;
        }

        public Principal getIssuerDN() {
            return issuer;
        }

        public X500Principal getIssuerX500Principal() {
            return issuer;
        }

        public Principal getSubjectDN() {
            return subject;
        }

        public X500Principal getSubjectX500Principal() {
            return subject;
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
            return keyUsage;
        }

        public List<String> getExtendedKeyUsage()
                throws CertificateParsingException {
            return extKeyUsage;
        }

        public int getBasicConstraints() {
            return pathLen;
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

        public PublicKey getPublicKey() {
            return key;
        }

        public byte[] getEncoded() throws CertificateEncodingException {
            return encoding;
        }

        public Set<String> getNonCriticalExtensionOIDs() {
            return null;
        }

        public Set<String> getCriticalExtensionOIDs() {
            return null;
        }

        public byte[] getExtensionValue(String oid) {

            if (("2.5.29.14".equals(oid)) || ("2.5.29.35".equals(oid))) {
                // Extension value is represented as an OctetString
                return ASN1OctetString.getInstance().encode(keyIdentifier);
            }
            if ("2.5.29.16".equals(oid)) {
                PrivateKeyUsagePeriod pkup = new PrivateKeyUsagePeriod(
                        notBefore, notAfter);
                byte[] encoded = pkup.getEncoded();
                return ASN1OctetString.getInstance().encode(encoded);
            }
            if (("2.5.29.37".equals(oid)) && (extKeyUsage != null)) {
                ASN1Oid[] oa = new ASN1Oid[extKeyUsage.size()];
                String[] val = new String[extKeyUsage.size()];
                Iterator it = extKeyUsage.iterator();
                int id = 0;
                while (it.hasNext()) {
                    oa[id] = ASN1Oid.getInstanceForString();
                    val[id++] = (String) it.next();
                }
                return ASN1OctetString.getInstance().encode(
                        new ASN1Sequence(oa).encode(val));
            }
            if ("2.5.29.19".equals(oid)) {
                return ASN1OctetString.getInstance().encode(
                        new ASN1Sequence(new ASN1Type[] {
                                ASN1Boolean.getInstance(),
                                ASN1Integer.getInstance() })
                                .encode(new Object[] {
                                        new Boolean(pathLen != 1),
                                        BigInteger.valueOf(pathLen)
                                                .toByteArray() }));
            }
            if ("2.5.29.17".equals(oid) && (sans != null)) {
                if (sans.getNames() == null) {
                    return null;
                }
                return ASN1OctetString.getInstance().encode(
                        GeneralNames.ASN1.encode(sans));
            }
            if ("2.5.29.32".equals(oid) && (policies != null)
                    && (policies.length > 0)) {
                // Certificate Policies Extension (as specified in rfc 3280)
                CertificatePolicies certificatePolicies = new CertificatePolicies();
                for (int i = 0; i < policies.length; i++) {
                    PolicyInformation policyInformation = new PolicyInformation(
                            policies[i]);
                    certificatePolicies.addPolicyInformation(policyInformation);
                }
                return ASN1OctetString.getInstance().encode(
                        certificatePolicies.getEncoded());
            }
            if ("2.5.29.30".equals(oid) && (nameConstraints != null)) { //
                // Name
                // Constraints
                // Extension
                // (as
                // specified
                // in
                // rfc
                // 3280)
                return ASN1OctetString.getInstance().encode(
                        nameConstraints.getEncoded());
            }

            return null;
        }

        public boolean hasUnsupportedCriticalExtension() {
            return false;
        }
        
    }
    
    public X509Certificate rootCertificate;

    public X509Certificate endCertificate;

    public MyCRL crl;

    private X509CertSelector theCertSelector;

    private CertPathBuilder builder;
    
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
    
    public static final String rootPrivateKey =
         "-----BEGIN RSA PRIVATE KEY-----\n" + 
         "Proc-Type: 4,ENCRYPTED\n" + 
         "DEK-Info: DES-EDE3-CBC,D9682F66FDA316E5\n" + 
         "\n" + 
         "8lGaQPlUZ/iHhdldB//xfNUrZ3RAkBthzKg+n9HBJsjztXXAZ40NGYZmgvpgnfmr\n" + 
         "7ZJxHxYHFc3GAmBBk9v+/dA8E5yWJa71roffWMQUuFNfGzHhGTOxvNC04W7yAajs\n" + 
         "CPuyI+xnAAo73F7NVTiqX3NVgu4bB8RVxJyToMe4M289oh93YvxWQ4buVTf0ErJ8\n" + 
         "Yc8+0ugpfXjGfRhL36qj6B1CcV7NMdXAVExrGlTf0TWT9wVbiROk4XaoaFuWh17h\n" + 
         "11NEDjsKQ8T4M9kRdC+tKfST8sLik1Pq6jRLIKeX8GQd7tV1IWVZ3KcQBJwu9zLq\n" + 
         "Hi0GTSF7IWCdwXjDyniMQiSbkmHNP+OnVyhaqew5Ooh0uOEQq/KWFewXg7B3VMr0\n" + 
         "l6U8sBX9ODGeW0wVdNopvl17udCkV0xm3S+MRZDnZiTlAXwKx/a/gyf5R5XYp3S0\n" + 
         "0eqrfy2o6Ax4hRkwcNJ2KMeLQNIiYYWKABQj5/i4TYZV6npCIXOnQEkXa9DmqyUE\n" + 
         "qB7eFj5FcXeqQ8ERmsLveWArsLDn2NNPdv5EaKIs2lrvwoKYeYF7hrKNpifq+QqS\n" + 
         "u1kN+KHjibcF42EAUozNVmkHsW8VqlywAs4MsMwxU0D57cVGWycuSedraKhc0D6j\n" + 
         "a4pQOWWY3ZMLoAA1ZmHG9cjDPqcJt0rqk5AhSBRmGVUccfkP7dk9KyJQizro87LI\n" + 
         "u7zWwMIqTfmlhyfAP0AWjrt/bMN9heGByVA55xkyCdSEVaC5gsIfmGpNy4u+wbZ9\n" + 
         "rSWVuTfAbjW0n0FW+CDS1LgdjXNkeAP2Uvc1QgVRCPdA23WniLFFJQ==\n" + 
         "-----END RSA PRIVATE KEY-----";
    
    /**
     * Certificate:
    Data:
        Version: 3 (0x2)
        Serial Number: 1 (0x1)
        Signature Algorithm: sha1WithRSAEncryption
        Issuer: C=AN, ST=Android, O=Android, OU=Android, CN=Android/emailAddress=android
        Validity
            Not Before: Dec  9 16:40:35 2008 GMT
            Not After : Dec  9 16:40:35 2009 GMT
        Subject: C=AN, ST=Android, L=Android, O=Android, OU=Android, CN=Android Certificate/emailAddress=android
        Subject Public Key Info:
            Public Key Algorithm: rsaEncryption
            RSA Public Key: (1024 bit)
                Modulus (1024 bit):
                    00:b8:e3:de:c7:a9:40:47:2c:2a:6f:f5:2a:f4:cd:
                    f2:2d:40:fa:15:3f:1c:37:66:73:a5:67:4d:5b:a0:
                    b6:b1:dd:dc:bf:01:c7:e2:c1:48:1a:8f:1c:ce:ec:
                    b0:a2:55:29:9a:1b:3a:6e:cc:7b:d7:65:ae:0b:05:
                    34:03:8a:af:db:f0:dc:01:80:92:03:b4:13:e5:d6:
                    fd:79:66:7f:c3:1a:62:d5:5e:3d:c0:19:a4:42:15:
                    47:19:e6:f0:c8:b7:e2:7b:82:a2:c7:3d:df:ac:8c:
                    d5:bc:39:b8:e5:93:ac:3f:af:30:b7:cc:00:a8:00:
                    f3:38:23:b0:97:0e:92:b1:1b
                Exponent: 65537 (0x10001)
        X509v3 extensions:
            X509v3 Basic Constraints: 
                CA:FALSE
            Netscape Comment: 
                OpenSSL Generated Certificate
            X509v3 Subject Key Identifier: 
                88:4D:EC:16:26:A7:76:F5:26:43:BC:34:99:DF:D5:EA:7B:F8:5F:DE
            X509v3 Authority Key Identifier: 
                keyid:4B:E3:22:14:AD:0A:14:46:B7:52:31:8B:AB:9E:5A:62:F3:98:37:80

    Signature Algorithm: sha1WithRSAEncryption
        55:73:95:e6:4c:40:fc:fd:52:8a:5f:83:15:49:73:ca:f3:d8:
        5f:bb:d6:f5:2e:90:e6:7f:c3:7d:4d:27:d3:45:c6:53:9b:aa:
        e3:32:99:40:b3:a9:d3:14:7d:d5:e6:a7:70:95:30:6e:dc:8c:
        7b:48:e1:98:d1:65:7a:eb:bf:b0:5c:cd:c2:eb:31:5e:b6:e9:
        df:56:95:bc:eb:79:74:27:5b:6d:c8:55:63:09:d3:f9:e2:40:
        ba:b4:a2:c7:2c:cb:b1:3a:c2:d8:0c:21:31:ee:68:7e:97:ce:
        98:22:2e:c6:cf:f0:1a:11:04:ca:9a:06:de:98:48:85:ac:6c:
        6f:98
     */
    public static final String  endCert = 
        "-----BEGIN CERTIFICATE-----\n" + 
        "MIIC6jCCAlOgAwIBAgIBATANBgkqhkiG9w0BAQUFADBtMQswCQYDVQQGEwJBTjEQ\n" + 
        "MA4GA1UECBMHQW5kcm9pZDEQMA4GA1UEChMHQW5kcm9pZDEQMA4GA1UECxMHQW5k\n" + 
        "cm9pZDEQMA4GA1UEAxMHQW5kcm9pZDEWMBQGCSqGSIb3DQEJARYHYW5kcm9pZDAe\n" + 
        "Fw0wODEyMDkxNjQwMzVaFw0wOTEyMDkxNjQwMzVaMIGLMQswCQYDVQQGEwJBTjEQ\n" + 
        "MA4GA1UECBMHQW5kcm9pZDEQMA4GA1UEBxMHQW5kcm9pZDEQMA4GA1UEChMHQW5k\n" + 
        "cm9pZDEQMA4GA1UECxMHQW5kcm9pZDEcMBoGA1UEAxMTQW5kcm9pZCBDZXJ0aWZp\n" + 
        "Y2F0ZTEWMBQGCSqGSIb3DQEJARYHYW5kcm9pZDCBnzANBgkqhkiG9w0BAQEFAAOB\n" + 
        "jQAwgYkCgYEAuOPex6lARywqb/Uq9M3yLUD6FT8cN2ZzpWdNW6C2sd3cvwHH4sFI\n" + 
        "Go8czuywolUpmhs6bsx712WuCwU0A4qv2/DcAYCSA7QT5db9eWZ/wxpi1V49wBmk\n" + 
        "QhVHGebwyLfie4Kixz3frIzVvDm45ZOsP68wt8wAqADzOCOwlw6SsRsCAwEAAaN7\n" + 
        "MHkwCQYDVR0TBAIwADAsBglghkgBhvhCAQ0EHxYdT3BlblNTTCBHZW5lcmF0ZWQg\n" + 
        "Q2VydGlmaWNhdGUwHQYDVR0OBBYEFIhN7BYmp3b1JkO8NJnf1ep7+F/eMB8GA1Ud\n" + 
        "IwQYMBaAFEvjIhStChRGt1Ixi6ueWmLzmDeAMA0GCSqGSIb3DQEBBQUAA4GBAFVz\n" + 
        "leZMQPz9UopfgxVJc8rz2F+71vUukOZ/w31NJ9NFxlObquMymUCzqdMUfdXmp3CV\n" + 
        "MG7cjHtI4ZjRZXrrv7BczcLrMV626d9WlbzreXQnW23IVWMJ0/niQLq0oscsy7E6\n" + 
        "wtgMITHuaH6XzpgiLsbP8BoRBMqaBt6YSIWsbG+Y\n" + 
        "-----END CERTIFICATE-----";
    
    public static final String endPrivateKey =
        "-----BEGIN RSA PRIVATE KEY-----\n" + 
        "Proc-Type: 4,ENCRYPTED\n" + 
        "DEK-Info: DES-EDE3-CBC,E20AAB000D1D90B1\n" + 
        "\n" + 
        "cWrCb6eHuwb6/gnbX12Va47qSpFW0j99Lq2eEj0fqLdlwA6+KvD3/U+Nj4ldaAQ4\n" + 
        "rYryQv0MJu/kT9z/mJbBI4NwunX/9vXttyuh8s07sv8AqdHCylYR9miz61Q0LkLR\n" + 
        "9H9D8NWMgMnuVhlj+NUXlkF+Jfriu5xkIqeYDhN8c3/AMawQoNdW/pWmgz0BfFIP\n" + 
        "DUxszfXHx5mfSMoRdC2YZGlFdsONSO7s14Ayz8+pKD0PzSARXtTEJ5+mELCnhFsw\n" + 
        "R7zYYwD+9WjL702bjYQxwRS5Sk1Z/VAxLFfjdtlUFSi6VLGIG+jUnM1RF91KtJY1\n" + 
        "bJOQrlHw9/wyH75y9sXUrVpil4qH9shILHgu4A0VaL7IpIFjWS9vPY7SvwqRlbk7\n" + 
        "QPhxoIpiNzjzjEa7PG6nSqy8mRzJP0OLWzRUoMWJn6ntf+oj7CzaaIgFrrwRGOCQ\n" + 
        "BYibTTMZ/paxKDvZ9Lcl8a6uRvi2II2/F63bPcTcILsKDsBdQp93Evanw1QKXdGi\n" + 
        "jb4b0Y1LYZM0jl7z2TSBZ27HyHKp4jMQP9q9mujEKInjzSB+gsRGfP6++OilrR2U\n" + 
        "Y7kN2o/ufnPHltel0pUWOHr45IyK8zowgXWtKVl9U+VRwr2thGbdqkRGk55KjJK4\n" + 
        "Q+OfwvIKHgvn/4cN/BGIA/52eyY//bTFk6ePGY2vlQK4mvB7MeSxtxoCGxdCYQru\n" + 
        "wI28rOHyQ1cdx141yxlKVSIcxBVZHm8sfh9PHeKMKuaOgc8kfx+Qh8IghFHyJ+yg\n" + 
        "PboNF9/PiM/glaaBzY2OKTYQKY6LiTetZiI6RdLE7Y+SFwG7Wwo5dg==\n" + 
        "-----END RSA PRIVATE KEY-----";
    
    private void setupEnvironment() throws Exception {
        // create certificates and CRLs
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        ByteArrayInputStream bi = new ByteArrayInputStream(rootCert.getBytes());
        rootCertificate = (X509Certificate) cf.generateCertificate(bi);
        bi = new ByteArrayInputStream(endCert.getBytes());
        endCertificate = (X509Certificate) cf.generateCertificate(bi);

        BigInteger revokedSerialNumber = BigInteger.valueOf(1);
        crl = new MyCRL("X.509");
//        X509CRL rootCRL = X509CRL;
//        X509CRL interCRL = X509CRLExample.createCRL(interCert, interPair
//                .getPrivate(), revokedSerialNumber);

        // create CertStore to support path building
        List<Object> list = new ArrayList<Object>();

        list.add(rootCertificate);
        list.add(endCertificate);

//        CollectionCertStoreParameters params = new CollectionCertStoreParameters(
//                list);
//        CertStore store = CertStore.getInstance("Collection", params);
//
        theCertSelector = new X509CertSelector();
        theCertSelector.setCertificate(endCertificate);
        theCertSelector.setIssuer(endCertificate.getIssuerX500Principal()
                .getEncoded());
        
     // build the path
        builder = CertPathBuilder.getInstance("PKIX");

    }
    
    private CertPath buildCertPath() throws InvalidAlgorithmParameterException {
        PKIXCertPathBuilderResult result = null;
        PKIXBuilderParameters buildParams = new PKIXBuilderParameters(
                Collections.singleton(new TrustAnchor(rootCertificate, null)),
                theCertSelector);
        try {
        result = (PKIXCertPathBuilderResult) builder
        .build(buildParams);
        } catch(CertPathBuilderException e) {
            return null;
        }
        return result.getCertPath();
    }
    
    /**
     * @tests java.security.cert.X509CertSelector#addPathToName(int, byte[])
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies Exception",
        method = "addPathToName",
        args = {int.class, byte[].class}
    )
    public void test_addPathToNameLintLbyte_array2() throws Exception {
        TestUtils.initCertPathSSCertChain();
        setupEnvironment();
        GeneralName name = new GeneralName(1, "822.Name");
        assertNotNull(name.getEncoded());
        byte[] b = new byte[name.getEncoded().length];
        b = name.getEncoded();
        b[name.getEncoded().length-3] = (byte) 200;
        
        try {
            theCertSelector.addPathToName(1, b);
        } catch (IOException e) {
            // ok
        }
        
        theCertSelector.setPathToNames(null);
        
        theCertSelector.addPathToName(1, name.getEncodedName());
        assertNotNull(theCertSelector.getPathToNames());
        CertPath p = buildCertPath();
        assertNull(p);
        
        theCertSelector.setPathToNames(null);
        
//        name = new GeneralName(new Name("O=Android"));
//        theCertSelector.addPathToName(4, endCertificate.getSubjectDN().getName());
        theCertSelector.addPathToName(4, TestUtils.rootCertificateSS.getIssuerX500Principal().getEncoded());
        assertNotNull(theCertSelector.getPathToNames());
        p = TestUtils.buildCertPathSSCertChain();
        assertNotNull(p);
    }
    
    /**
     * @tests java.security.cert.X509CertSelector#addPathToName(int, String)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies IOException.",
        method = "addPathToName",
        args = {int.class, java.lang.String.class}
    )
    public void test_addPathToNameLintLjava_lang_String2() throws Exception {
        setupEnvironment();
        
        GeneralName name = new GeneralName(1, "822.Name");
        assertNotNull(name.getEncoded());
        byte[] b = new byte[name.getEncoded().length];
        b = name.getEncoded();
        b[name.getEncoded().length-3] = (byte) 200;
        
        try {
        theCertSelector.addPathToName(1, new String(b));
        } catch (IOException e) {
            // ok
        }
        
        theCertSelector.setPathToNames(null);
        
        theCertSelector.addPathToName(1, new String(name.getEncodedName()));
        assertNotNull(theCertSelector.getPathToNames());
        
        CertPath p = buildCertPath();
        assertNull(p);
        
        theCertSelector.setPathToNames(null);
        theCertSelector.addPathToName(1, rootCertificate.getIssuerX500Principal().getName());
        assertNotNull(theCertSelector.getPathToNames());
        //p = buildCertPath();
        //assertNotNull(p);
    }
    
    /**
     * @tests java.security.cert.X509CertSelector#addSubjectAlternativeName(int, byte[])
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "IOException checking missed",
        method = "addSubjectAlternativeName",
        args = {int.class, byte[].class}
    )
    public void test_addSubjectAlternativeNameLintLbyte_array2()
            throws Exception {
        
      
        GeneralName san0 = new GeneralName(new OtherName("1.2.3.4.5",
                new byte[] {1, 2, 0, 1}));
        GeneralName san1 = new GeneralName(1, "rfc@822.Name");
        GeneralName san2 = new GeneralName(2, "dNSName");

        GeneralNames sans1 = new GeneralNames();
        sans1.addName(san0);
        sans1.addName(san1);
        sans1.addName(san2);

        X509CertSelector selector = new X509CertSelector();

        selector.addSubjectAlternativeName(0, san0.getEncodedName());
        selector.addSubjectAlternativeName(1, san1.getEncodedName());
        selector.addSubjectAlternativeName(2, san2.getEncodedName());

        GeneralNames sans2 = new GeneralNames();
        sans2.addName(san0);

        TestCert cert1 = new TestCert(sans1);
        TestCert cert2 = new TestCert(sans2);

        assertTrue(selector.match(cert1));
        assertFalse(selector.match(cert2));
        
        selector.setSubjectAlternativeNames(null);

        GeneralName name = new GeneralName(new Name("O=Android"));
        try {
            selector.addSubjectAlternativeName(0, name.getEncodedName());
        } catch (IOException e) {
            // ok
        }

    }

    /**
     * @tests java.security.cert.X509CertSelector#addSubjectAlternativeName(int, String)
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "addSubjectAlternativeName",
        args = {int.class, java.lang.String.class}
    )
    public void test_addSubjectAlternativeNameLintLjava_lang_String2() throws Exception{
        GeneralName san6 = new GeneralName(6, "http://uniform.Resource.Id");
        GeneralName san2 = new GeneralName(2, "dNSName");

        GeneralNames sans1 = new GeneralNames();
        sans1.addName(san6);
        sans1.addName(san2);

        X509CertSelector selector = new X509CertSelector();
        
        selector.addSubjectAlternativeName(6, "http://uniform.Resource.Id");
        selector.addSubjectAlternativeName(2, "dNSName");

        GeneralNames sans2 = new GeneralNames();
        sans2.addName(san2);

        TestCert cert1 = new TestCert(sans1);
        TestCert cert2 = new TestCert(sans2);

        assertTrue(selector.match(cert1));
        assertFalse(selector.match(cert2));
        
        selector.setSubjectAlternativeNames(null);

        GeneralName name = new GeneralName(new Name("O=Android"));
        try {
            selector.addSubjectAlternativeName(0, (name.toString()));
        } catch (IOException e) {
            // ok
        }

    }
}
