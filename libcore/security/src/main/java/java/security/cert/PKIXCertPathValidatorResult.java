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

import java.security.PublicKey;

import org.apache.harmony.security.internal.nls.Messages;

/**
 * @com.intel.drl.spec_ref
 * 
 */
public class PKIXCertPathValidatorResult implements CertPathValidatorResult {
    // A trust anchor used during validation of certification path
    private final TrustAnchor trustAnchor;
    // Valid policy tree resulting from PKIX
    // certification path validation algorithm
    private final PolicyNode policyTree;
    // Public key of the subject (target) certificate
    private final PublicKey subjectPublicKey;

    /**
     * @com.intel.drl.spec_ref
     */
    public PKIXCertPathValidatorResult(TrustAnchor trustAnchor,
            PolicyNode policyTree, PublicKey subjectPublicKey) {
        this.trustAnchor = trustAnchor;
        this.policyTree = policyTree;
        this.subjectPublicKey = subjectPublicKey;
        if (this.trustAnchor == null) {
            throw new NullPointerException(Messages.getString("security.64")); //$NON-NLS-1$
        }
        if (this.subjectPublicKey == null) {
            throw new NullPointerException(
                    Messages.getString("security.65")); //$NON-NLS-1$
        }
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public PolicyNode getPolicyTree() {
        return policyTree;
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public PublicKey getPublicKey() {
        return subjectPublicKey;
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public TrustAnchor getTrustAnchor() {
        return trustAnchor;
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            // Actually, the exception will not be thrown out.
            throw new Error(e);
        }
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public String toString() {
        StringBuffer sb = new StringBuffer(super.toString());
        sb.append(": [\n Trust Anchor: "); //$NON-NLS-1$
        sb.append(trustAnchor.toString());
        sb.append("\n Policy Tree: "); //$NON-NLS-1$
        sb.append(policyTree == null ? "no valid policy tree\n" //$NON-NLS-1$
                                     : policyTree.toString());
        sb.append("\n Subject Public Key: "); //$NON-NLS-1$
        sb.append(subjectPublicKey.toString());
        sb.append("\n]"); //$NON-NLS-1$
        return sb.toString();
    }
}
