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
* @author Alexander Y. Kleymenov
* @version $Revision$
*/

package java.security.cert;

import java.io.ByteArrayInputStream;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Principal;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.cert.CRL;
import java.security.cert.CRLException;
import java.security.cert.X509CRLEntry;
import java.security.cert.X509Extension;
import java.util.Arrays;
import java.util.Date;
import java.util.Set;
import javax.security.auth.x500.X500Principal;

import org.apache.harmony.security.internal.nls.Messages;

/**
 * @com.intel.drl.spec_ref
 */
public abstract class X509CRL extends CRL implements X509Extension {

    /**
     * @com.intel.drl.spec_ref
     */
    protected X509CRL() {
        super("X.509"); //$NON-NLS-1$
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof X509CRL)) {
            return false;
        }
        X509CRL obj = (X509CRL) other;
        try {
            return Arrays.equals(getEncoded(), obj.getEncoded());
        } catch (CRLException e) {
            return false;
        }
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public int hashCode() {
        try {
            int res = 0;
            byte[] array = getEncoded();
            for (int i=0; i<array.length; i++) {
                res += array[i] & 0xFF;
            }
            return res;
        } catch (CRLException e) {
            return 0;
        }
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public abstract byte[] getEncoded() throws CRLException;


    /**
     * @com.intel.drl.spec_ref
     */
    public abstract void verify(PublicKey key)
                     throws CRLException, NoSuchAlgorithmException,
                            InvalidKeyException, NoSuchProviderException,
                            SignatureException;

    /**
     * @com.intel.drl.spec_ref
     */
    public abstract void verify(PublicKey key, String sigProvider)
                     throws CRLException, NoSuchAlgorithmException,
                            InvalidKeyException, NoSuchProviderException,
                            SignatureException;

    /**
     * @com.intel.drl.spec_ref
     */
    public abstract int getVersion();

    /**
     * @com.intel.drl.spec_ref
     */
    public abstract Principal getIssuerDN();

    /**
     * @com.intel.drl.spec_ref
     */
    public X500Principal getIssuerX500Principal() {
        try {
            // TODO if there is no X.509 certificate provider installed
            // should we try to access Harmony X509CRLImpl via classForName?
            CertificateFactory factory = CertificateFactory
                    .getInstance("X.509"); //$NON-NLS-1$

            X509CRL crl = (X509CRL) factory
                    .generateCRL(new ByteArrayInputStream(getEncoded()));

            return crl.getIssuerX500Principal();

        } catch (Exception e) {
            throw new RuntimeException(Messages.getString("security.59"), e); //$NON-NLS-1$
        }
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public abstract Date getThisUpdate();

    /**
     * @com.intel.drl.spec_ref
     */
    public abstract Date getNextUpdate();

    /**
     * @com.intel.drl.spec_ref
     */
    public abstract X509CRLEntry getRevokedCertificate(BigInteger serialNumber);

    /**
     * @com.intel.drl.spec_ref
     */
    public X509CRLEntry getRevokedCertificate(X509Certificate certificate) {
        if (certificate == null) {
            throw new NullPointerException();
        }
        return getRevokedCertificate(certificate.getSerialNumber());
    }
        
    /**
     * @com.intel.drl.spec_ref
     */
    public abstract Set<? extends X509CRLEntry> getRevokedCertificates();

    /**
     * @com.intel.drl.spec_ref
     */
    public abstract byte[] getTBSCertList() throws CRLException;

    /**
     * @com.intel.drl.spec_ref
     */
    public abstract byte[] getSignature();

    /**
     * @com.intel.drl.spec_ref
     */
    public abstract String getSigAlgName();

    /**
     * @com.intel.drl.spec_ref
     */
    public abstract String getSigAlgOID();

    /**
     * @com.intel.drl.spec_ref
     */
    public abstract byte[] getSigAlgParams();
}

