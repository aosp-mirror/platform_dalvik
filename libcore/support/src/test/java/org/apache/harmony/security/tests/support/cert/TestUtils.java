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

package org.apache.harmony.security.tests.support.cert;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.CollectionCertStoreParameters;
import java.security.cert.PKIXCertPathChecker;
import java.security.cert.PolicyNode;
import java.security.cert.TrustAnchor;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import tests.support.resource.Support_Resources;

/**
 * java.security.cert test utilities
 * 
 */
public class TestUtils {
    // Certificate type used during testing
    private static final String certType = "X.509";
    // Key store type used during testing
    private static final String keyStoreType = "BKS";
    // The file name prefix to load keystore from
    private static final String keyStoreFileName = "test." + keyStoreType
            + ".ks";
    //
    // The file name suffixes to load keystore from
    //  *.ks1 - keystore containing untrusted certificates only
    //  *.ks2 - keystore containing trusted certificates only
    //  *.ks3 - keystore containing both trusted and untrusted certificates
    //
    public static final int UNTRUSTED = 1;
    public static final int TRUSTED = 2;
    public static final int TRUSTED_AND_UNTRUSTED = 3;
    //
    // Common passwords for all test keystores
    //
    private final static char[] storepass =
        new char[] {'s','t','o','r','e','p','w','d'};

    /**
     * Creates <code>TrustAnchor</code> instance
     * constructed using self signed test certificate
     *
     * @return <code>TrustAnchor</code> instance
     * @throws CertificateException
     * @throws FileNotFoundException
     */
    public static TrustAnchor getTrustAnchor() {
        CertificateFactory cf = null;
        try {
            cf = CertificateFactory.getInstance(certType);
        } catch (CertificateException e) {
            // requested cert type is not available in the
            // default provider package or any of the other provider packages
            // that were searched
            throw new RuntimeException(e);
        }
        BufferedInputStream bis = null;
        try {
            bis = new BufferedInputStream(new ByteArrayInputStream(
                    getEncodedX509Certificate()));
            X509Certificate c1 = (X509Certificate)cf.generateCertificate(bis);

            return new TrustAnchor(c1, null);
        } catch (Exception e) {
            // all failures are fatal
            throw new RuntimeException(e);
        } finally {
            if (bis != null) {
                try {
                    bis.close() ;
                } catch (IOException ign) {}
            }
        }
    }

    /**
     * Creates <code>Set</code> of <code>TrustAnchor</code>s
     * containing single element (self signed test certificate).
     * @return Returns <code>Set</code> of <code>TrustAnchor</code>s
     */
    public static Set getTrustAnchorSet() {
        TrustAnchor ta = getTrustAnchor();
        if (ta == null) {
            return null;
        }
        HashSet<TrustAnchor> set = new HashSet<TrustAnchor>();
        if (!set.add(ta)) {
            throw new RuntimeException("Could not create trust anchor set");
        }
        return set;
    }

    /**
     * Creates test <code>KeyStore</code> instance
     * 
     * @param initialize
     *  Do not initialize returned <code>KeyStore</code> if false
     * 
     * @param testKeyStoreType 
     *  this parameter ignored if <code>initialize</code> is false;
     *  The following types supported:<br>
     *  1 - <code>KeyStore</code> with untrusted certificates only<br>
     *  2 - <code>KeyStore</code> with trusted certificates only<br>
     *  3 - <code>KeyStore</code> with both trusted and untrusted certificates
     * 
     * @return Returns test <code>KeyStore</code> instance
     */
    public static KeyStore getKeyStore(boolean initialize,
            int testKeyStoreType) {
        BufferedInputStream bis = null;
        try {
            KeyStore ks = KeyStore.getInstance(keyStoreType);
            if (initialize) {
                String fileName = keyStoreFileName + testKeyStoreType;
                ks.load(Support_Resources.getResourceStream(fileName),
                        storepass);
            }
            return ks;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (initialize && bis != null) {
                try {
                    bis.close();
                } catch (IOException ign) {}
            }
        }
    }

    /**
     * Creates <code>List</code> of <code>CollectionCertStores</code>
     *
     * @return The list created
     * 
     * @throws InvalidAlgorithmParameterException
     * @throws NoSuchAlgorithmException
     */
    public static List getCollectionCertStoresList()
        throws InvalidAlgorithmParameterException,
               NoSuchAlgorithmException {
        CertStore cs = CertStore.getInstance("Collection",
                new CollectionCertStoreParameters());
        ArrayList<CertStore> l = new ArrayList<CertStore>();
        if (!l.add(cs)) {
            throw new RuntimeException("Could not create cert stores list");
        }
        return l;
    }

    /**
     * Creates stub implementation of the <code>PKIXCertPathChecker</code>
     *
     * @return Stub implementation of the <code>PKIXCertPathChecker</code>
     */
    public static PKIXCertPathChecker getTestCertPathChecker() {
        // stub implementation for testing purposes only
        return new PKIXCertPathChecker() {
            private boolean forward = false;

            public void check(Certificate arg0, Collection arg1)
                    throws CertPathValidatorException {
            }

            public Set getSupportedExtensions() {
                return null;
            }

            public void init(boolean arg0) throws CertPathValidatorException {
                forward = arg0;
            }

            public boolean isForwardCheckingSupported() {
                // just to check this checker state
                return forward;
            }
        };
    }

    /**
     * Creates policy tree stub containing two <code>PolicyNode</code>s
     * for testing purposes
     *
     * @return root <code>PolicyNode</code> of the policy tree
     */
    public static PolicyNode getPolicyTree() {
        return new PolicyNode() {
            final PolicyNode parent = this;
            public int getDepth() {
                // parent
                return 0;
            }

            public boolean isCritical() {
                return false;
            }

            public String getValidPolicy() {
                return null;
            }

            public PolicyNode getParent() {
                return null;
            }

            public Iterator getChildren() {
                PolicyNode child = new PolicyNode() {
                    public int getDepth() {
                        // child
                        return 1;
                    }

                    public boolean isCritical() {
                        return false;
                    }

                    public String getValidPolicy() {
                        return null;
                    }

                    public PolicyNode getParent() {
                        return parent;
                    }

                    public Iterator getChildren() {
                        return null;
                    }

                    public Set getExpectedPolicies() {
                        return null;
                    }

                    public Set getPolicyQualifiers() {
                        return null;
                    }
                };
                HashSet<PolicyNode> s = new HashSet<PolicyNode>();
                s.add(child);
                return s.iterator();
            }

            public Set getExpectedPolicies() {
                return null;
            }

            public Set getPolicyQualifiers() {
                return null;
            }
        };
    }
    // X.509 encoded certificate
    private static final String ENCODED_X509_CERTIFICATE = "-----BEGIN CERTIFICATE-----\n"
            + "MIIDHTCCAtsCBEFT72swCwYHKoZIzjgEAwUAMHQxCzAJBgNVBAYTAlJVMQwwCgYDVQQIEwNOU08x\n"
            + "FDASBgNVBAcTC05vdm9zaWJpcnNrMQ4wDAYDVQQKEwVJbnRlbDEVMBMGA1UECxMMRFJMIFNlY3Vy\n"
            + "aXR5MRowGAYDVQQDExFWbGFkaW1pciBNb2xvdGtvdjAeFw0wNDA5MjQwOTU2NTlaFw0wNjA1MTcw\n"
            + "OTU2NTlaMHQxCzAJBgNVBAYTAlJVMQwwCgYDVQQIEwNOU08xFDASBgNVBAcTC05vdm9zaWJpcnNr\n"
            + "MQ4wDAYDVQQKEwVJbnRlbDEVMBMGA1UECxMMRFJMIFNlY3VyaXR5MRowGAYDVQQDExFWbGFkaW1p\n"
            + "ciBNb2xvdGtvdjCCAbgwggEsBgcqhkjOOAQBMIIBHwKBgQD9f1OBHXUSKVLfSpwu7OTn9hG3Ujzv\n"
            + "RADDHj+AtlEmaUVdQCJR+1k9jVj6v8X1ujD2y5tVbNeBO4AdNG/yZmC3a5lQpaSfn+gEexAiwk+7\n"
            + "qdf+t8Yb+DtX58aophUPBPuD9tPFHsMCNVQTWhaRMvZ1864rYdcq7/IiAxmd0UgBxwIVAJdgUI8V\n"
            + "IwvMspK5gqLrhAvwWBz1AoGBAPfhoIXWmz3ey7yrXDa4V7l5lK+7+jrqgvlXTAs9B4JnUVlXjrrU\n"
            + "WU/mcQcQgYC0SRZxI+hMKBYTt88JMozIpuE8FnqLVHyNKOCjrh4rs6Z1kW6jfwv6ITVi8ftiegEk\n"
            + "O8yk8b6oUZCJqIPf4VrlnwaSi2ZegHtVJWQBTDv+z0kqA4GFAAKBgQDiNmj9jgWu1ILYqYWcUhNN\n"
            + "8CjjRitf80yWP/s/565wZz3anb2w72jum63mdShDko9eOOOd1hiVuiBnNhSL7D6JfIYBJvNXr1av\n"
            + "Gw583BBv12OBgg0eAW/GRWBn2Ak2JjsoBc5x2c1HAEufakep7T6RoC+n3lqbKPKyHWVdfqQ9KTAL\n"
            + "BgcqhkjOOAQDBQADLwAwLAIUaRS3C9dXcMbrOAhmidFBr7oMvH0CFEC3LUwfLJX5gY8P6uxpkPx3\n"
            + "JDSM\n" + "-----END CERTIFICATE-----\n";

    public static byte[] getEncodedX509Certificate() {
        return ENCODED_X509_CERTIFICATE.getBytes();
    }
    
    /**
     * Returns X.509 certificate encoding corresponding to version v1.
     * 
     * Certificate encoding was created by hands according to X.509 Certificate
     * ASN.1 notation. The certificate encoding has the following encoded
     * field values:<br> 
     * - version: 1<br>
     * - serialNumber: 5<br>
     * - issuer: CN=Z<br>
     * - notBefore: 13 Dec 1999 14:15:16<br>
     * - notAfter: 01 Jan 2000 00:00:00<br>
     * - subject: CN=Y<br>
     * 
     * @return X.509 certificate encoding corresponding to version v1.
     */
    public static byte[] getX509Certificate_v1() {
        return new byte[] {
        // Certificate: SEQUENCE
            0x30, 0x6B,

            //
            // TBSCertificate: SEQUENCE {
            //
            0x30, 0x5C,

            // version: [0] EXPLICIT Version DEFAULT v1
            (byte) 0xA0, 0x03, 0x02, 0x01, 0x00,

            // serialNumber: CertificateSerialNumber
            0x02, 0x01, 0x05,

            // signature: AlgorithmIdentifier
            0x30, 0x07, // SEQUENCE
            0x06, 0x02, 0x03, 0x05,//OID
            0x01, 0x01, 0x07, //ANY

            //issuer: Name
            0x30, 0x0C, 0x31, 0x0A, 0x30, 0x08, 0x06, 0x03, 0x55, 0x04, 0x03,
            0x13, 0x01, 0x5A, // CN=Z

            //validity: Validity
            0x30, 0x1E, // SEQUENCE
            // notBefore: UTCTime
            0x17, 0x0D, 0x39, 0x39, 0x31, 0x32, 0x31, 0x33, 0x31, 0x34, 0x31,
            0x35, 0x31, 0x36, 0x5A, // 13 Dec 1999 14:15:16
            // notAfter:  UTCTime
            0x17, 0x0D, 0x30, 0x30, 0x30, 0x31, 0x30, 0x31, 0x30, 0x30, 0x30,
            0x30, 0x30, 0x30, 0x5A, // 01 Jan 2000 00:00:00

            //subject: Name
            0x30, 0x0C, 0x31, 0x0A, 0x30, 0x08, 0x06, 0x03, 0x55, 0x04, 0x03,
            0x13, 0x01, 0x59, // CN=Y
            //SubjectPublicKeyInfo  ::=  SEQUENCE  {
            //    algorithm            AlgorithmIdentifier,
            //    subjectPublicKey     BIT STRING  }
            0x30, 0x0D, // SEQUENCE
            0x30, 0x07, // SEQUENCE
            0x06, 0x02, 0x03, 0x05,//OID
            0x01, 0x01, 0x07, //ANY
            0x03, 0x02, 0x00, 0x01, // subjectPublicKey

            // issuerUniqueID - missed
            // subjectUniqueID - missed
            // extensions - missed

            // } end TBSCertificate

            //
            // signatureAlgorithm: AlgorithmIdentifier
            //
            0x30, 0x07, // SEQUENCE
            0x06, 0x02, 0x03, 0x05,//OID
            0x01, 0x01, 0x07, //ANY

            //
            // signature: BIT STRING  
            //
            0x03, 0x02, 0x00, 0x01 };
    }

    /**
     * Returns X.509 certificate encoding corresponding to version v3.
     * 
     * Certificate encoding was created by hands according to X.509 Certificate
     * ASN.1 notation. The certificate encoding has the following encoded
     * field values:<br> 
     * - version: 3<br>
     * - serialNumber: 5<br>
     * - issuer: CN=Z<br>
     * - notBefore: 13 Dec 1999 14:15:16<br>
     * - notAfter: 01 Jan 2000 00:00:00<br>
     * - subject: CN=Y<br>
     * - extensions:
     *       1) AuthorityKeyIdentifier(OID=2.5.29.35): no values in it(empty sequence) 
     * 
     * @return X.509 certificate encoding corresponding to version v3.
     */
    public static byte[] getX509Certificate_v3() {
        return new byte[] {
        // Certificate: SEQUENCE
            0x30, 0x7D,

            //
            // TBSCertificate: SEQUENCE {
            //
            0x30, 0x6E,

            // version: [0] EXPLICIT Version DEFAULT v1
            (byte) 0xA0, 0x03, 0x02, 0x01, 0x02,

            // serialNumber: CertificateSerialNumber
            0x02, 0x01, 0x05,

            // signature: AlgorithmIdentifier
            0x30, 0x07, // SEQUENCE
            0x06, 0x02, 0x03, 0x05,//OID
            0x01, 0x01, 0x07, //ANY

            //issuer: Name
            0x30, 0x0C, 0x31, 0x0A, 0x30, 0x08, 0x06, 0x03, 0x55, 0x04, 0x03,
            0x13, 0x01, 0x5A, // CN=Z

            //validity: Validity
            0x30, 0x1E, // SEQUENCE
            // notBefore: UTCTime
            0x17, 0x0D, 0x39, 0x39, 0x31, 0x32, 0x31, 0x33, 0x31, 0x34, 0x31,
            0x35, 0x31, 0x36, 0x5A, // 13 Dec 1999 14:15:16
            // notAfter:  UTCTime
            0x17, 0x0D, 0x30, 0x30, 0x30, 0x31, 0x30, 0x31, 0x30, 0x30, 0x30,
            0x30, 0x30, 0x30, 0x5A, // 01 Jan 2000 00:00:00

            //subject: Name
            0x30, 0x0C, 0x31, 0x0A, 0x30, 0x08, 0x06, 0x03, 0x55, 0x04, 0x03,
            0x13, 0x01, 0x59, // CN=Y
            //SubjectPublicKeyInfo  ::=  SEQUENCE  {
            //    algorithm            AlgorithmIdentifier,
            //    subjectPublicKey     BIT STRING  }
            0x30, 0x0D, // SEQUENCE
            0x30, 0x07, // SEQUENCE
            0x06, 0x02, 0x03, 0x05,//OID
            0x01, 0x01, 0x07, //ANY
            0x03, 0x02, 0x00, 0x01, // subjectPublicKey

            // issuerUniqueID - missed
            // subjectUniqueID - missed
            // extensions : [3]  EXPLICIT Extensions OPTIONAL
            (byte) 0xA3, 0x10,
            // Extensions  ::=  SEQUENCE SIZE (1..MAX) OF Extension
            0x30, 0x0E,
            // Extension  ::=  SEQUENCE  {
            // extnID      OBJECT IDENTIFIER,
            // critical    BOOLEAN DEFAULT FALSE,
            // extnValue   OCTET STRING  }

            // 1) AuthorityKeyIdentifier extension (see HARMONY-3384)
            0x30, 0x0C,
            0x06, 0x03, 0x55, 0x1D, 0x23, // OID = 2.5.29.35
            0x01, 0x01, 0x00, // critical = FALSE
            0x04, 0x02, 0x30, 0x00, // extnValue: MUST be empty sequence
            // missed: keyIdentifier
            // missed: authorityCertIssuer
            // missed" authorityCertSerialNumber

            // } end TBSCertificate

            //
            // signatureAlgorithm: AlgorithmIdentifier
            //
            0x30, 0x07, // SEQUENCE
            0x06, 0x02, 0x03, 0x05,//OID
            0x01, 0x01, 0x07, //ANY

            //
            // signature: BIT STRING  
            //
            0x03, 0x02, 0x00, 0x01 };
    }

    /**
     * Returns X.509 CRL encoding corresponding to version v1.
     * 
     * CRL encoding was created by hands according to X.509 CRL ASN.1
     * notation. The CRL encoding has the following encoded field values:<br> 
     * - version: 1<br>
     * - issuer: CN=Z<br>
     * - thisUpdate: 01 Jan 2001 01:02:03<br>
     * 
     * @return X.509 CRL encoding corresponding to version v1.
     */
    public static byte[] getX509CRL_v1() {
        return new byte[] {
                //CertificateList: SEQUENCE
                0x30, 0x35, 
                
                // TBSCertList: SEQUENCE  
                0x30, 0x27,
                // Version: INTEGER OPTIONAL
                // 0x02, 0x01, 0x01, - missed here cause it is v1
                // signature: AlgorithmIdentifier
                0x30, 0x06, // SEQUENCE
                0x06, 0x01, 0x01, // OID
                0x01, 0x01, 0x11, // ANY
                // issuer: Name                   
                0x30, 0x0C, 0x31, 0x0A, 0x30, 0x08, 0x06, 0x03, 0x55, 0x04,
                0x03, 0x13, 0x01, 0x5A, // CN=Z 
                // thisUpdate: ChoiceOfTime
                // GeneralizedTime: 01 Jan 2001 01:02:03
                0x18, 0x0F, 0x32, 0x30, 0x30, 0x31, 0x30, 0x31, 0x30, 0x31,
                0x30, 0x31, 0x30, 0x32, 0x30, 0x33, 0x5A,
                
                // nextUpdate - missed
                // revokedCertificates - missed
                // crlExtensions - missed
                
                // signatureAlgorithm: AlgorithmIdentifier
                0x30, 0x06, // SEQUENCE
                0x06, 0x01, 0x01, //OID
                0x01, 0x01, 0x11, //ANY
                // signature: BIT STRING  
                0x03, 0x02, 0x00, 0x01 };
    }
}
