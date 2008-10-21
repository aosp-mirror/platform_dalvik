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
* @author Alexander V. Astapchuk
* @version $Revision$
*/

package java.security;

import java.io.Serializable;
import java.security.cert.CertPath;

import org.apache.harmony.security.internal.nls.Messages;

/**
 * @com.intel.drl.spec_ref 
 */

public final class CodeSigner implements Serializable {

    /**
     * @com.intel.drl.spec_ref 
     */
    private static final long serialVersionUID = 6819288105193937581L;

    /**
     * @com.intel.drl.spec_ref 
     */
    private CertPath signerCertPath;

    /**
     * @com.intel.drl.spec_ref 
     */
    private Timestamp timestamp;

    // Cached hash code value
    private transient int hash;

    /**
     * @com.intel.drl.spec_ref 
     */
    public CodeSigner(CertPath signerCertPath, Timestamp timestamp) {
        if (signerCertPath == null) {
            throw new NullPointerException(Messages.getString("security.10")); //$NON-NLS-1$
        }
        this.signerCertPath = signerCertPath;
        this.timestamp = timestamp;
    }

    /**
     * @com.intel.drl.spec_ref 
     */
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof CodeSigner) {
            CodeSigner that = (CodeSigner) obj;
            if (!signerCertPath.equals(that.signerCertPath)) {
                return false;
            }
            return timestamp == null ? that.timestamp == null : timestamp
                    .equals(that.timestamp);
        }
        return false;
    }

    /**
     * @com.intel.drl.spec_ref 
     */
    public CertPath getSignerCertPath() {
        return signerCertPath;
    }

    /**
     * @com.intel.drl.spec_ref 
     */
    public Timestamp getTimestamp() {
        return timestamp;
    }

    /**
     * @com.intel.drl.spec_ref 
     */
    public int hashCode() {
        if (hash == 0) {
            hash = signerCertPath.hashCode()
                    ^ (timestamp == null ? 0 : timestamp.hashCode());
        }
        return hash;
    }

    /**
     * @com.intel.drl.spec_ref 
     */
    public String toString() {
        // There is no any special reason for '256' here, it's taken abruptly
        // FIXME: 1.5 StringBuffer => StringBuilder
        StringBuffer buf = new StringBuffer(256);
        // The javadoc says nothing, and the others implementations behavior seems as 
        // dumping only the first certificate. Well, let's do the same.
        buf.append("CodeSigner [").append(signerCertPath.getCertificates().get(0)); //$NON-NLS-1$
        if( timestamp != null ) {
            buf.append("; ").append(timestamp); //$NON-NLS-1$
        }
        buf.append("]"); //$NON-NLS-1$
        return buf.toString();
    }
}