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
import java.util.Iterator;
import java.util.List;

import org.apache.harmony.security.internal.nls.Messages;

/**
 * An immutable certificate path that can be validated. All certificates in the
 * path are of the same type (i.e., X509).
 * 
 * A <code>CertPath</code> can be represented as a byte array in at least one
 * supported encoding when serialized.
 * 
 * When a <code>List</code> of the certificates is obtained it must be
 * immutable.
 * 
 * A <code>CertPath</code> must be thread-safe without requiring coordinated
 * access.
 */
public abstract class CertPath implements Serializable {
    /**
     * @com.intel.drl.spec_ref
     */
    private static final long serialVersionUID = 6068470306649138683L;
    // Standard name of the type of certificates in this path
    private final String type;

    /**
     * @com.intel.drl.spec_ref
     */
    protected CertPath(String type) {
        this.type = type;
    }

    /**
     * Returns the type of <code>Certificate</code> in the
     * <code>CertPath</code>
     * 
     * @return <code>Certificate</code> type
     */
    public String getType() {
        return type;
    }

    /**
     * Returns true if <code>Certificate</code>s in the list are the same
     * type and the lists are equal (and by implication the certificates
     * contained within are the same).
     * 
     * @param other
     *            <code>CertPath</code> to be compared for equality
     */
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other instanceof CertPath) {
            CertPath o = (CertPath)other;
            if (getType().equals(o.getType())) {
                if (getCertificates().equals(o.getCertificates())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Overrides Object.hashCode() Defined as: hashCode = 31 *
     * path.getType().hashCode() + path.getCertificates().hashCode();
     * 
     * @return hash code for CertPath object
     */
    public int hashCode() {
        int hash = getType().hashCode();
        hash = hash*31 + getCertificates().hashCode();
        return hash;
    }

    /**
     * Returns a <code>String</code> representation of the
     * <code>CertPath</code>
     * <code>Certificate</code>s. It is the result of
     * calling <code>toString</code> on all <code>Certificate</code>s in
     * the <code>List</code>. <code>Certificate</code>s
     * 
     * @return string representation of <code>CertPath</code>
     */
    public String toString() {
        StringBuffer sb = new StringBuffer(getType());
        sb.append(" Cert Path, len="); //$NON-NLS-1$
        sb.append(getCertificates().size());
        sb.append(": [\n"); //$NON-NLS-1$
        int n=1;
        for (Iterator i=getCertificates().iterator();
                      i.hasNext(); n++) {
            sb.append("---------------certificate "); //$NON-NLS-1$
            sb.append(n);
            sb.append("---------------\n"); //$NON-NLS-1$
            sb.append(((Certificate)i.next()).toString());
        }
        sb.append("\n]"); //$NON-NLS-1$
        return sb.toString();
    }

    /**
     * Returns an immutable List of the <code>Certificate</code>s contained
     * in the <code>CertPath</code>.
     * 
     * @return list of <code>Certificate</code>s in the <code>CertPath</code>
     */
    public abstract List<? extends Certificate> getCertificates();

    /**
     * Returns an encoding of the <code>CertPath</code> using the default
     * encoding
     * 
     * @return default encoding of the <code>CertPath</code>
     * @throws CertificateEncodingException
     */
    public abstract byte[] getEncoded()
        throws CertificateEncodingException;

    /**
     * Returns an encoding of the <code>CertPath</code> using the specified
     * encoding
     * 
     * @param encoding
     *            encoding that should be generated
     * @return default encoding of the <code>CertPath</code>
     * @throws CertificateEncodingException
     */
    public abstract byte[] getEncoded(String encoding)
        throws CertificateEncodingException;

    /**
     * Return an <code>Iterator</code> over the supported encodings for a
     * representation of the certificate path.
     * 
     * @return <code>Iterator</code> over supported encodings (as
     *         <code>String</code>s)
     */
    public abstract Iterator<String> getEncodings();

    /**
     * @com.intel.drl.spec_ref
     */
    protected Object writeReplace() throws ObjectStreamException {
        try {
            return new CertPathRep(getType(), getEncoded());
        } catch (CertificateEncodingException e) {
            throw new NotSerializableException (
                    Messages.getString("security.66", e)); //$NON-NLS-1$
        }
    }

    /**
     * @com.intel.drl.spec_ref
     */
    protected static class CertPathRep implements Serializable {
        /**
         * @com.intel.drl.spec_ref
         */
        private static final long serialVersionUID = 3015633072427920915L;
        // Standard name of the type of certificates in this path
        private final String type;
        // cert path data
        private final byte[] data;

        // Force default serialization to use writeUnshared/readUnshared
        // for cert path data
        private static final ObjectStreamField[] serialPersistentFields = {
             new ObjectStreamField("type", String.class), //$NON-NLS-1$
             new ObjectStreamField("data", byte[].class, true) //$NON-NLS-1$
        };

        /**
         * @com.intel.drl.spec_ref
         */
        protected CertPathRep(String type, byte[] data) {
            this.type = type;
            this.data = data;
        }

        /**
         * @com.intel.drl.spec_ref
         */
        protected Object readResolve() throws ObjectStreamException {
            try {
                CertificateFactory cf = CertificateFactory.getInstance(type);
                return cf.generateCertPath(new ByteArrayInputStream(data));
            } catch (Throwable t) {
                throw new NotSerializableException(
                        Messages.getString("security.67", t)); //$NON-NLS-1$
            }
        }
    }
}
