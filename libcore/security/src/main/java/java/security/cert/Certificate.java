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

package java.security.cert;

import java.io.ByteArrayInputStream;
import java.io.NotSerializableException;
import java.io.ObjectStreamException;
import java.io.ObjectStreamField;
import java.io.Serializable;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.SignatureException;
import java.util.Arrays;

import org.apache.harmony.security.internal.nls.Messages;

/**
 * Abstract class to represent identity certificates. It represents a way to
 * verify the binding of a Principal and its public key. Examples are X.509,
 * PGP, and SDSI.
 */
public abstract class Certificate implements Serializable {
    /**
     * @com.intel.drl.spec_ref
     */
    private static final long serialVersionUID = -3585440601605666277L;

    // The standard name of the certificate type
    private final String type;

    /**
     * @com.intel.drl.spec_ref
     */
    protected Certificate(String type) {
        this.type = type;
    }

    /**
     * Returns the certificate type represented by the receiver.
     * 
     * @return the certificate type represented by the receiver.
     */
    public final String getType() {
        return type;
    }

    /**
     * Compares the argument to the receiver, and returns true if they represent
     * the <em>same</em> object using a class specific comparison. The
     * implementation in Object returns true only if the argument is the exact
     * same object as the receiver (==).
     * 
     * @param other
     *            the object to compare with this object
     * @return <code>true</code> if the object is the same as this object
     *         <code>false</code> if it is different from this object
     * @see #hashCode
     */
    public boolean equals(Object other) {
        // obj equal to itself
        if (this == other) {
            return true;
        }
        if (other instanceof Certificate) {
            try {
                // check that encoded forms match
                return Arrays.equals(this.getEncoded(),
                        ((Certificate)other).getEncoded());
            } catch (CertificateEncodingException e) {
                throw new RuntimeException(e);
            }
        }
        return false;
    }

    /**
     * Returns an integer hash code for the receiver. Any two objects which
     * answer <code>true</code> when passed to <code>equals</code> must
     * answer the same value for this method.
     * 
     * @return the receiver's hash
     * 
     * @see #equals
     */
    public int hashCode() {
        try {
            byte[] encoded = getEncoded();
            int hash = 0;
            for (int i=0; i<encoded.length; i++) {
                hash += i*encoded[i];
            }
            return hash;
        } catch (CertificateEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns the encoded representation for this certificate.
     * 
     * @return the encoded representation for this certificate.
     */
    public abstract byte[] getEncoded() throws CertificateEncodingException;

    /**
     * Verifies that this certificate was signed with the given public key.
     * 
     * @param key
     *            PublicKey public key for which verification should be
     *            performed.
     * 
     * @exception CertificateException
     *                if encoding errors are detected
     * @exception NoSuchAlgorithmException
     *                if an unsupported algorithm is detected
     * @exception InvalidKeyException
     *                if an invalid key is detected
     * @exception NoSuchProviderException
     *                if there is no default provider
     * @exception SignatureException
     *                if signature errors are detected
     */
    public abstract void verify(PublicKey key)
        throws CertificateException,
               NoSuchAlgorithmException,
               InvalidKeyException,
               NoSuchProviderException,
               SignatureException;

    /**
     * Verifies that this certificate was signed with the given public key. Uses
     * the signature algorithm given by the provider.
     * 
     * @param key
     *            PublicKey public key for which verification should be
     *            performed.
     * @param sigProvider
     *            String the name of the signature provider.
     * 
     * @exception CertificateException
     *                if encoding errors are detected
     * @exception NoSuchAlgorithmException
     *                if an unsupported algorithm is detected
     * @exception InvalidKeyException
     *                if an invalid key is detected
     * @exception NoSuchProviderException
     *                if there is no default provider
     * @exception SignatureException
     *                if signature errors are detected
     */
    public abstract void verify(PublicKey key, String sigProvider)
        throws CertificateException,
               NoSuchAlgorithmException,
               InvalidKeyException,
               NoSuchProviderException,
               SignatureException;

    /**
     * Returns a string containing a concise, human-readable description of the
     * receiver.
     * 
     * @return a printable representation for the receiver.
     */
    public abstract String toString();

    /**
     * Returns the public key corresponding to this certificate.
     * 
     * @return the public key corresponding to this certificate.
     */
    public abstract PublicKey getPublicKey();

    /**
     * @com.intel.drl.spec_ref
     */
    protected Object writeReplace() throws ObjectStreamException {
        try {
            return new CertificateRep(getType(), getEncoded());
        } catch (CertificateEncodingException e) {  
            throw new NotSerializableException (
                    Messages.getString("security.66", e)); //$NON-NLS-1$
        }
    }

    /**
     * @com.intel.drl.spec_ref
     * 
     */
    protected static class CertificateRep implements Serializable {
        /**
         * @com.intel.drl.spec_ref
         */
        private static final long serialVersionUID = -8563758940495660020L;
        // The standard name of the certificate type
        private final String type;
        // The certificate data
        private final byte[] data;

        // Force default serialization to use writeUnshared/readUnshared
        // for the certificate data
        private static final ObjectStreamField[] serialPersistentFields = {
             new ObjectStreamField("type", String.class), //$NON-NLS-1$
             new ObjectStreamField("data", byte[].class, true) //$NON-NLS-1$
        };

        /**
         * @com.intel.drl.spec_ref
         */
        protected CertificateRep(String type, byte[] data) {
            this.type = type;
            this.data = data;
        }

        /**
         * @com.intel.drl.spec_ref
         */
        protected Object readResolve() throws ObjectStreamException {
            try {
                CertificateFactory cf = CertificateFactory.getInstance(type);
                return cf.generateCertificate(new ByteArrayInputStream(data));
            } catch (Throwable t) {
                throw new NotSerializableException(
                        Messages.getString("security.68", t)); //$NON-NLS-1$
            }
        }
    }
}
