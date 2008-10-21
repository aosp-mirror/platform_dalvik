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

package javax.security.cert;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.math.BigInteger;
import java.security.AccessController;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Principal;
import java.security.PublicKey;
import java.security.Security;
import java.security.SignatureException;
import java.security.cert.CertificateFactory;
import java.util.Date;
import javax.security.cert.Certificate;
import javax.security.cert.CertificateEncodingException;
import javax.security.cert.CertificateException;
import javax.security.cert.CertificateExpiredException;
import javax.security.cert.CertificateNotYetValidException;

import org.apache.harmony.security.internal.nls.Messages;

/**
 * @com.intel.drl.spec_ref
 */
public abstract class X509Certificate extends Certificate {

    private static Constructor constructor;
    
    static {
        try {
            String classname = (String) AccessController.doPrivileged(
                new java.security.PrivilegedAction() {
                    public Object run() {
                        return Security.getProperty("cert.provider.x509v1"); //$NON-NLS-1$
                    }
                }
            );
            Class cl = Class.forName(classname);
            constructor =
                cl.getConstructor(new Class[] {InputStream.class});
        } catch (Throwable e) {
        }
    }
    
    /**
     * @com.intel.drl.spec_ref
     */
    public X509Certificate() {
        super();
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public static final X509Certificate getInstance(InputStream inStream)
                                             throws CertificateException {
        if (inStream == null) {
            throw new CertificateException(Messages.getString("security.87")); //$NON-NLS-1$
        }
        if (constructor != null) {
            try {
                return (X509Certificate) 
                    constructor.newInstance(new Object[] {inStream});
            } catch (Throwable e) {
                throw new CertificateException(e.getMessage());
            }
        }

        final java.security.cert.X509Certificate cert;
        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509"); //$NON-NLS-1$
            cert = (java.security.cert.X509Certificate)
                                            cf.generateCertificate(inStream);
        } catch (java.security.cert.CertificateException e) {
            throw new CertificateException(e.getMessage());
        }

        return new X509Certificate() {

            public byte[] getEncoded() throws CertificateEncodingException {
                try {
                    return cert.getEncoded();
                } catch (java.security.cert.CertificateEncodingException e) {
                    throw new CertificateEncodingException(e.getMessage());
                }
            }

            public void verify(PublicKey key) throws CertificateException,
                                NoSuchAlgorithmException, InvalidKeyException,
                                NoSuchProviderException, SignatureException {
                try {
                    cert.verify(key);
                } catch (java.security.cert.CertificateException e) {
                    throw new CertificateException(e.getMessage());
                }
            }

            public void verify(PublicKey key, String sigProvider)
                            throws CertificateException,
                                NoSuchAlgorithmException, InvalidKeyException,
                                NoSuchProviderException, SignatureException {
                try {
                    cert.verify(key, sigProvider);
                } catch (java.security.cert.CertificateException e) {
                    throw new CertificateException(e.getMessage());
                }
            }

            public String toString() {
                return cert.toString();
            }

            public PublicKey getPublicKey() {
                return cert.getPublicKey();
            }

            public void checkValidity() throws CertificateExpiredException,
                                   CertificateNotYetValidException {
                try {
                    cert.checkValidity();
                } catch (java.security.cert.CertificateNotYetValidException e) {
                    throw new CertificateNotYetValidException(e.getMessage());
                } catch (java.security.cert.CertificateExpiredException e) {
                    throw new CertificateExpiredException(e.getMessage());
                }
            }

            public void checkValidity(Date date) 
                            throws CertificateExpiredException,
                                   CertificateNotYetValidException {
                try {
                    cert.checkValidity(date);
                } catch (java.security.cert.CertificateNotYetValidException e) {
                    throw new CertificateNotYetValidException(e.getMessage());
                } catch (java.security.cert.CertificateExpiredException e) {
                    throw new CertificateExpiredException(e.getMessage());
                }
            }

            public int getVersion() {
                return 2;
            }

            public BigInteger getSerialNumber() {
                return cert.getSerialNumber();
            }

            public Principal getIssuerDN() {
                return cert.getIssuerDN();
            }

            public Principal getSubjectDN() {
                return cert.getSubjectDN();
            }

            public Date getNotBefore() {
                return cert.getNotBefore();
            }

            public Date getNotAfter() {
                return cert.getNotAfter();
            }

            public String getSigAlgName() {
                return cert.getSigAlgName();
            }

            public String getSigAlgOID() {
                return cert.getSigAlgOID();
            }

            public byte[] getSigAlgParams() {
                return cert.getSigAlgParams();
            }
        };
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public static final X509Certificate getInstance(byte[] certData)
                                             throws CertificateException {
        if (certData == null) {
            throw new CertificateException(Messages.getString("security.88")); //$NON-NLS-1$
        }
        ByteArrayInputStream bais = new ByteArrayInputStream(certData);
        return getInstance(bais);
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public abstract void checkValidity()
            throws CertificateExpiredException, CertificateNotYetValidException;


    /**
     * @com.intel.drl.spec_ref
     */
    public abstract void checkValidity(Date date)
            throws CertificateExpiredException, CertificateNotYetValidException;

    /**
     * @com.intel.drl.spec_ref
     */
    public abstract int getVersion();

    /**
     * @com.intel.drl.spec_ref
     */
    public abstract BigInteger getSerialNumber();

    /**
     * @com.intel.drl.spec_ref
     */
    public abstract Principal getIssuerDN();

    /**
     * @com.intel.drl.spec_ref
     */
    public abstract Principal getSubjectDN();

    /**
     * @com.intel.drl.spec_ref
     */
    public abstract Date getNotBefore();

    /**
     * @com.intel.drl.spec_ref
     */
    public abstract Date getNotAfter();

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

