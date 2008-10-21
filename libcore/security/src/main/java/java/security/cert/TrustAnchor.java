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

import java.io.IOException;
import java.security.PublicKey;

import javax.security.auth.x500.X500Principal;

import org.apache.harmony.security.internal.nls.Messages;
import org.apache.harmony.security.utils.Array;
import org.apache.harmony.security.x509.NameConstraints;



/**
 * @com.intel.drl.spec_ref
 * 
 */
public class TrustAnchor {
    // Most trusted CA as a X500Principal
    private final X500Principal caPrincipal;
    // Most trusted CA name
    private final String caName;
    // Most trusted CA public key
    private final PublicKey caPublicKey;
    // Most trusted CA certificate
    private final X509Certificate trustedCert;
    // Name constraints extension
    private final byte[] nameConstraints;

    /**
     * @com.intel.drl.spec_ref
     */
    public TrustAnchor(X509Certificate trustedCert, byte[] nameConstraints) {
        if (trustedCert == null) {
            throw new NullPointerException(Messages.getString("security.5C")); //$NON-NLS-1$
        }
        this.trustedCert = trustedCert;
        // copy nameConstraints if not null
        if (nameConstraints != null) {
            this.nameConstraints = new byte[nameConstraints.length];
            System.arraycopy(nameConstraints, 0,
                    this.nameConstraints, 0, this.nameConstraints.length);
            processNameConstraints();
        } else {
            this.nameConstraints = null;
        }
        this.caName = null;
        this.caPrincipal = null;
        this.caPublicKey = null;
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public TrustAnchor(String caName, PublicKey caPublicKey,
            byte[] nameConstraints) {
        if (caName == null) {
            throw new NullPointerException(Messages.getString("security.5D")); //$NON-NLS-1$
        }
        this.caName = caName;
        if (caPublicKey == null) {
            throw new NullPointerException(Messages.getString("security.5E")); //$NON-NLS-1$
        }
        this.caPublicKey = caPublicKey;
        // copy nameConstraints if not null
        if (nameConstraints != null) {
            this.nameConstraints = new byte[nameConstraints.length];
            System.arraycopy(nameConstraints, 0,
                    this.nameConstraints, 0, this.nameConstraints.length);
            processNameConstraints();
        } else {
            this.nameConstraints = null;
        }

        this.trustedCert = null;

        // X500Principal checks caName validity
        if (caName.length() == 0) {
            throw new IllegalArgumentException(
                    Messages.getString("security.5F")); //$NON-NLS-1$
        }
        this.caPrincipal = new X500Principal(this.caName);
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public TrustAnchor(X500Principal caPrincipal,
            PublicKey caPublicKey, byte[] nameConstraints) {
        if (caPrincipal == null) {
            throw new NullPointerException(Messages.getString("security.60")); //$NON-NLS-1$
        }
        this.caPrincipal = caPrincipal;
        if (caPublicKey == null) {
            throw new NullPointerException(Messages.getString("security.5E")); //$NON-NLS-1$
        }
        this.caPublicKey = caPublicKey;
        // copy nameConstraints if not null
        if (nameConstraints != null) {
            this.nameConstraints = new byte[nameConstraints.length];
            System.arraycopy(nameConstraints, 0,
                    this.nameConstraints, 0, this.nameConstraints.length);
            processNameConstraints();
        } else {
            this.nameConstraints = null;
        }

        this.trustedCert = null;
        this.caName = caPrincipal.getName();
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public final byte[] getNameConstraints() {
        if (nameConstraints == null) {
            return null;
        }
        byte[] ret = new byte[nameConstraints.length];
            System.arraycopy(nameConstraints, 0,
                    ret, 0, nameConstraints.length);
        return ret;
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public final X509Certificate getTrustedCert() {
        return trustedCert;
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public final X500Principal getCA() {
        return caPrincipal;
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public final String getCAName() {
        return caName;
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public final PublicKey getCAPublicKey() {
        return caPublicKey;
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public String toString() {
        StringBuffer sb = new StringBuffer("TrustAnchor: [\n"); //$NON-NLS-1$
        if (trustedCert != null) {
            sb.append("Trusted CA certificate: "); //$NON-NLS-1$
            sb.append(trustedCert);
            sb.append("\n"); //$NON-NLS-1$
        }
        if (caPrincipal != null) {
            sb.append("Trusted CA Name: "); //$NON-NLS-1$
            sb.append(caPrincipal);
            sb.append("\n"); //$NON-NLS-1$
        }
        if (caPublicKey != null) {
            sb.append("Trusted CA Public Key: "); //$NON-NLS-1$
            sb.append(caPublicKey);
            sb.append("\n"); //$NON-NLS-1$
        }
        // FIXME if needed:
        if (nameConstraints != null) {
            sb.append("Name Constraints:\n"); //$NON-NLS-1$
            sb.append(Array.toString(nameConstraints, "    ")); //$NON-NLS-1$
        }
        sb.append("\n]"); //$NON-NLS-1$
        return sb.toString();
    }

    //
    // Private stuff
    //

    // Decodes and checks NameConstraints structure.
    // Throws IllegalArgumentException if NameConstraints
    // encoding is invalid.
    private void processNameConstraints() {
        try {
            // decode and check nameConstraints
            NameConstraints.ASN1.decode(nameConstraints);
        } catch (IOException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }
}
