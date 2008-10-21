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

import org.apache.harmony.security.asn1.ObjectIdentifier;
import org.apache.harmony.security.internal.nls.Messages;
import org.apache.harmony.security.utils.Array;


/**
 * @com.intel.drl.spec_ref
 * 
 */
public class PolicyQualifierInfo {
    // This PolicyQualifierInfo DER encoding
    private final byte[] encoded;
    // This PolicyQualifierInfo policy qualifier id -
    // OID represented as String containing non-negative integers
    // separated by periods
    private final String policyQualifierId;
    // DER encoding of the policy qualifier - part of encoded
    private final byte[] policyQualifier;

    /**
     * @com.intel.drl.spec_ref
     */
    public PolicyQualifierInfo(byte[] encoded) throws IOException {
        if (encoded == null) {
            throw new NullPointerException(Messages.getString("security.0A")); //$NON-NLS-1$
        }
        if (encoded.length == 0) {
            throw new IOException(Messages.getString("security.69")); //$NON-NLS-1$
        }
        this.encoded = new byte[encoded.length];
        System.arraycopy(encoded, 0, this.encoded, 0, this.encoded.length);
        
        // DER Decoding:
        Object[] decoded = (Object[]) org.apache.harmony.security.x509.PolicyQualifierInfo.ASN1
                .decode(this.encoded);
        policyQualifierId = ObjectIdentifier.toString((int[]) decoded[0]);
        policyQualifier = (byte[]) decoded[1];
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public final byte[] getEncoded() {
        byte[] ret = new byte[encoded.length];
        System.arraycopy(encoded, 0, ret, 0, encoded.length);
        return ret;
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public final String getPolicyQualifierId() {
        return policyQualifierId;
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public final byte[] getPolicyQualifier() {
        if (policyQualifier == null) {
            return null;
        }
        byte[] ret = new byte[policyQualifier.length];
        System.arraycopy(policyQualifier, 0, ret, 0, policyQualifier.length);
        return ret;
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public String toString() {
        StringBuffer sb =
            new StringBuffer("PolicyQualifierInfo: [\npolicyQualifierId: "); //$NON-NLS-1$
        sb.append(policyQualifierId);
        sb.append("\npolicyQualifier: \n"); //$NON-NLS-1$
        sb.append(Array.toString(policyQualifier, " ")); //$NON-NLS-1$
        sb.append("]"); //$NON-NLS-1$
        return sb.toString();
    }
}
