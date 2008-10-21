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
* @author Vera Y. Petrashkova
* @version $Revision$
*/

package java.security.cert;

import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.Security;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.harmony.security.fortress.Engine;
import org.apache.harmony.security.internal.nls.Messages;


/**
 * This class provides the functionality of a certificate factory algorithm.
 */

public class CertificateFactory {

    // Store CertificateFactory service name
    private static final String SERVICE = "CertificateFactory"; //$NON-NLS-1$

    // Used to access common engine functionality
    private static Engine engine = new Engine(SERVICE);

    // Store used provider
    private final Provider provider;

    // Store used CertificateFactorySpi implementation
    private final CertificateFactorySpi spiImpl;

    // Store used type
    private final String type;

    /**
     * @com.intel.drl.spec_ref
     *  
     */
    protected CertificateFactory(CertificateFactorySpi certFacSpi,
            Provider provider, String type) {
        this.provider = provider;
        this.type = type;
        this.spiImpl = certFacSpi;
    }

    /**
     * Returns a new CertificateFactory of the given type.
     * 
     * @param type
     *            java.lang.String Type of certificate desired
     * @return CertificateFactory a concrete implementation for the certificate
     *         type desired.
     * 
     * @exception CertificateException
     *                If the type cannot be found
     *
     * @exception NullPointerException
     *                If the type is null
     */
    public static final CertificateFactory getInstance(String type)
            throws CertificateException {
        if (type == null) {
            throw new NullPointerException(Messages.getString("security.07")); //$NON-NLS-1$
        }
        try {
            synchronized (engine) {
                engine.getInstance(type, null);
                return new CertificateFactory((CertificateFactorySpi) engine.spi,
                        engine.provider, type);
            }
        } catch (NoSuchAlgorithmException e) {
            throw new CertificateException(e);
        }
    }

    /**
     * @com.intel.drl.spec_ref
     * 
     * throws NullPointerException if algorithm is null (instead of
     * CertificateException as in 1.4 release)
     */
    public static final CertificateFactory getInstance(String type,
            String provider) throws CertificateException,
            NoSuchProviderException {
        if ((provider == null) || (provider.length() == 0)) {
            throw new IllegalArgumentException(Messages.getString("security.02")); //$NON-NLS-1$
        }
        Provider impProvider = Security.getProvider(provider);
        if (impProvider == null) {
            throw new NoSuchProviderException(provider);
        }
        return getInstance(type, impProvider);
    }

    /**
     * Returns a new CertificateFactory of the given type.
     * 
     * @param type
     *            java.lang.String Type of certificate desired
     * @param provider
     *            java.security.Provider Provider which has to implement the
     *            algorithm
     * @return CertificateFactory a concrete implementation for the certificate
     *         type desired.
     * 
     * @exception CertificateException
     *                If the type cannot be found
     *
     * @exception NullPointerException
     *                If algorithm is null
     */
    public static final CertificateFactory getInstance(String type,
            Provider provider) throws CertificateException {
        if (provider == null) {
            throw new IllegalArgumentException(Messages.getString("security.04")); //$NON-NLS-1$
        }
        if (type == null) {
            throw new NullPointerException(Messages.getString("security.07")); //$NON-NLS-1$
        }
        try {
            synchronized (engine) {
                engine.getInstance(type, provider, null);
                return new CertificateFactory((CertificateFactorySpi) engine.spi,
                        provider, type);
            }
        } catch (NoSuchAlgorithmException e) {
            throw new CertificateException(e.getMessage());
        }
    }

    /**
     * Returns the Provider of the certificate factory represented by the
     * receiver.
     * 
     * @return Provider an instance of a subclass of java.security.Provider
     */
    public final Provider getProvider() {
        return provider;
    }

    /**
     * Returns the Certificate type
     * 
     * @return String type of certificate being used
     */
    public final String getType() {
        return type;
    }

    /**
     * Generates and initializes a Certificate from data from the
     * provided input stream.
     * 
     * @param inStream
     *            InputStream Stream from where data is read to create the
     *            Certificate
     * 
     * @return Certificate an initialized Certificate
     * @exception CertificateException
     *                if parsing problems are detected
     */
    public final Certificate generateCertificate(InputStream inStream)
            throws CertificateException {
        return spiImpl.engineGenerateCertificate(inStream);
    }

    /**
     * Returns an Iterator over the supported CertPath encodings (as Strings).
     * The first element is the default encoding.
     * 
     * @return Iterator Iterator over supported CertPath encodings (as Strings)
     */
    public final Iterator<String> getCertPathEncodings() {
        return spiImpl.engineGetCertPathEncodings();
    }

    /**
     * Generates a <code>CertPath</code> from data from the provided
     * <code>InputStream</code>. The default encoding is assumed.
     * 
     * @param inStream
     *            InputStream with PKCS7 or PkiPath encoded data
     * 
     * @return CertPath a CertPath initialized from the provided data
     * 
     * @throws CertificateException
     *             if parsing problems are detected
     */
    public final CertPath generateCertPath(InputStream inStream)
            throws CertificateException {
        Iterator it = getCertPathEncodings();
        if (!it.hasNext()) {
            throw new CertificateException(Messages.getString("security.74")); //$NON-NLS-1$
        }
        return spiImpl.engineGenerateCertPath(inStream, (String) it.next());
    }

    /**
     * Generates a <code>CertPath</code> from data from the provided
     * <code>InputStream</code>. The encoding is that specified by the
     * encoding parameter.
     * 
     * @param inStream
     *            InputStream containing certificate path data in specified
     *            encoding
     * @param encoding
     *            encoding of the data in the input stream
     * 
     * @return CertPath a CertPath initialized from the provided data
     * 
     * @throws CertificateException
     *             if parsing problems are detected
     * @throws UnsupportedOperationException
     *             if the provider does not implement this method
     */
    public final CertPath generateCertPath(InputStream inStream, String encoding)
            throws CertificateException {
        return spiImpl.engineGenerateCertPath(inStream, encoding);
    }

    /**
     * Generates a <code>CertPath</code> from the provided List of
     * Certificates. The encoding is the default encoding.
     * 
     * @param certificates
     *            List containing certificates in a format supported by the
     *            CertificateFactory
     * 
     * @return CertPath a CertPath initialized from the provided data
     * 
     * @throws CertificateException
     *             if parsing problems are detected
     * @throws UnsupportedOperationException
     *             if the provider does not implement this method
     */
    public final CertPath generateCertPath(List<? extends Certificate> certificates)
            throws CertificateException {
        return spiImpl.engineGenerateCertPath(certificates);
    }

    /**
     * Generates and initializes a collection of Certificates from
     * data from the provided input stream.
     * 
     * @param inStream
     *            InputStream Stream from where data is read to create the
     *            Certificates
     * 
     * @return Collection an initialized collection of Certificates
     * @exception CertificateException
     *                if parsing problems are detected
     */
    public final Collection<? extends Certificate> generateCertificates(InputStream inStream)
            throws CertificateException {
        return spiImpl.engineGenerateCertificates(inStream);
    }

    /**
     * Generates and initializes a Certificate Revocation List from data from
     * the provided input stream.
     * 
     * @param inStream
     *            InputStream Stream from where data is read to create the CRL
     * 
     * @return CRL an initialized Certificate Revocation List
     * @exception CRLException
     *                if parsing problems are detected
     */
    public final CRL generateCRL(InputStream inStream) throws CRLException {
        return spiImpl.engineGenerateCRL(inStream);
    }

    /**
     * Generates and initializes a collection of Certificate Revocation List
     * from data from the provided input stream.
     * 
     * @param inStream
     *            InputStream Stream from where data is read to create the CRLs
     * 
     * @return Collection an initialized collection of Certificate Revocation
     *         List
     * @exception CRLException
     *                if parsing problems are detected
     * 
     */
    public final Collection<? extends CRL> generateCRLs(InputStream inStream)
            throws CRLException {
        return spiImpl.engineGenerateCRLs(inStream);
    }
}
