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
import java.util.Date;
import java.security.cert.CertPath;

import org.apache.harmony.security.internal.nls.Messages;

/**
 * @com.intel.drl.spec_ref 
 */

public final class Timestamp implements Serializable {

    /**
     * @com.intel.drl.spec_ref 
     */
    private static final long serialVersionUID = -5502683707821851294L;

    private Date timestamp;

    private CertPath signerCertPath;

    // Cached hash
    private transient int hash;

    /**
     * @com.intel.drl.spec_ref 
     */
    public Timestamp(Date timestamp, CertPath signerCertPath) {
        if (timestamp == null) {
            throw new NullPointerException(Messages.getString("security.0F")); //$NON-NLS-1$
        }
        if (signerCertPath == null) {
            throw new NullPointerException(Messages.getString("security.10")); //$NON-NLS-1$
        }
        // Clone timestamp to prevent modifications
        this.timestamp = new Date(timestamp.getTime());
        this.signerCertPath = signerCertPath;
    }

    /**
     * @com.intel.drl.spec_ref 
     */
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof Timestamp) {
            Timestamp that = (Timestamp) obj;
            return timestamp.equals(that.timestamp)
                    && signerCertPath.equals(that.signerCertPath);
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
    public Date getTimestamp() {
        return timestamp;
    }

    /**
     * @com.intel.drl.spec_ref 
     */
    public int hashCode() {
        if (hash == 0) {
            hash = timestamp.hashCode() ^ signerCertPath.hashCode();
        }
        return hash;
    }

    /**
     * @com.intel.drl.spec_ref 
     */
    public String toString() {
        StringBuffer buf = new StringBuffer(256);
        // Dump only the first certificate
        buf.append("Timestamp [").append(timestamp).append(" certPath="); //$NON-NLS-1$ //$NON-NLS-2$
        buf.append(signerCertPath.getCertificates().get(0)).append("]"); //$NON-NLS-1$
        return buf.toString();
    }
}