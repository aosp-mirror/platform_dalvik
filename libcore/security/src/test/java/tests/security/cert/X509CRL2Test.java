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

import junit.framework.TestCase;

import java.io.InputStream;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Principal;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.cert.CRLException;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509CRL;
import java.security.cert.X509CRLEntry;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import tests.support.resource.Support_Resources;

@TestTargetClass(X509CRL.class)
public class X509CRL2Test extends TestCase {

    private X509Certificate pemCert = null;

    protected void setUp() throws Exception {

        InputStream is = Support_Resources
                .getResourceStream("hyts_certificate_PEM.txt");

        CertificateFactory certFact = CertificateFactory.getInstance("X509");
        pemCert = (X509Certificate) certFact.generateCertificate(is);
    }

    /**
     * @tests java.security.cert.X509CRL#getExtensionValue(java.lang.String)
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
    public void _test_getExtensionValueLjava_lang_String() {
        if (pemCert != null) {
            Vector<String> extensionOids = new Vector<String>();
            extensionOids.addAll(pemCert.getCriticalExtensionOIDs());
            extensionOids.addAll(pemCert.getNonCriticalExtensionOIDs());
            Iterator i = extensionOids.iterator();
            while (i.hasNext()) {
                String oid = (String) i.next();
                byte[] value = pemCert.getExtensionValue(oid);
                if (value != null && value.length > 0) {
                    // check that it is an encoded as a OCTET STRING
                    assertTrue("The extension value for the oid " + oid
                            + " was not encoded as an OCTET STRING",
                            value[0] == 0x04);
                }
            }// end while
        } else {
            fail("Unable to obtain X509Certificate");
        }
    }

    /**
     * @tests java.security.cert.X509CRL#X509CRL()
     */
    @TestInfo(
      level = TestLevel.COMPLETE,
      purpose = "",
      targets = {
        @TestTarget(
          methodName = "X509CRL",
          methodArgs = {}
        )
    })
    public void _test_X509CRL() {
        MyX509CRL crl = new MyX509CRL();
        assertEquals("X.509", crl.getType());
    }

    class MyX509CRL extends X509CRL {

        public MyX509CRL() {
            super();
        }

        @Override
        public byte[] getEncoded() throws CRLException {
            return null;
        }

        @Override
        public Principal getIssuerDN() {
            return null;
        }

        @Override
        public Date getNextUpdate() {
            return null;
        }

        @Override
        public X509CRLEntry getRevokedCertificate(BigInteger serialNumber) {
            return null;
        }

        @Override
        public Set<? extends X509CRLEntry> getRevokedCertificates() {
            return null;
        }

        @Override
        public String getSigAlgName() {
            return null;
        }

        @Override
        public String getSigAlgOID() {
            return null;
        }

        @Override
        public byte[] getSigAlgParams() {
            return null;
        }

        @Override
        public byte[] getSignature() {
            return null;
        }

        @Override
        public byte[] getTBSCertList() throws CRLException {
            return null;
        }

        @Override
        public Date getThisUpdate() {
            return null;
        }

        @Override
        public int getVersion() {
            return 0;
        }

        @Override
        public void verify(PublicKey key) throws CRLException,
                NoSuchAlgorithmException, InvalidKeyException,
                NoSuchProviderException, SignatureException {
        }

        @Override
        public void verify(PublicKey key, String sigProvider)
                throws CRLException, NoSuchAlgorithmException,
                InvalidKeyException, NoSuchProviderException,
                SignatureException {
        }

        @Override
        public boolean isRevoked(Certificate cert) {
            return false;
        }

        @Override
        public String toString() {
            return null;
        }

        public Set<String> getCriticalExtensionOIDs() {
            return null;
        }

        public byte[] getExtensionValue(String oid) {
            return null;
        }

        public Set<String> getNonCriticalExtensionOIDs() {
            return null;
        }

        public boolean hasUnsupportedCriticalExtension() {
            return false;
        }
    }

}
