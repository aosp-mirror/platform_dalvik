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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.harmony.security.internal.nls.Messages;

/**
 * This class is a Service Provider Interface (therefore the Spi suffix) for
 * certificate factories to be supplied by providers.
 */

public abstract class CertificateFactorySpi {

    /**
     * Constructs a new instance of this class.
     */
    public CertificateFactorySpi() {
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
    public abstract Certificate engineGenerateCertificate(InputStream inStream)
            throws CertificateException;

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
    public abstract Collection<? extends Certificate> 
        engineGenerateCertificates(InputStream inStream) throws CertificateException;

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
    public abstract CRL engineGenerateCRL(InputStream inStream)
            throws CRLException;

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
     */
    public abstract Collection<? extends CRL> 
        engineGenerateCRLs(InputStream inStream) throws CRLException;

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
    public CertPath engineGenerateCertPath(InputStream inStream)
            throws CertificateException {
        throw new UnsupportedOperationException(
                Messages.getString("security.70")); //$NON-NLS-1$
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
    public CertPath engineGenerateCertPath(InputStream inStream, String encoding)
            throws CertificateException {
        throw new UnsupportedOperationException(
                Messages.getString("security.71")); //$NON-NLS-1$
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
    public CertPath engineGenerateCertPath(List<? extends Certificate>  certificates) 
            throws CertificateException {
        throw new UnsupportedOperationException(
                Messages.getString("security.72")); //$NON-NLS-1$
    }

    /**
     * Returns an Iterator over the supported CertPath encodings (as Strings).
     * The first element is the default encoding.
     * 
     * @return Iterator Iterator over supported CertPath encodings (as Strings)
     */
    public Iterator<String> engineGetCertPathEncodings() {
        throw new UnsupportedOperationException(
                Messages.getString("security.73")); //$NON-NLS-1$
    }
}
