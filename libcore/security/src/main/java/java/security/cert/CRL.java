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

/**
 * This class represents Certificate Revocation Lists (CRLs). They are used to
 * indicate that a given Certificate has expired already.
 * 
 * @see CertificateFactory
 */
public abstract class CRL {
    // The CRL type
    private final String type;

    /**
     * @com.intel.drl.spec_ref
     */
    protected CRL(String type) {
        this.type = type;
    }

    /**
     * Returns the type of this CRL.
     * 
     * @return String the type of this CRL.
     */
    public final String getType() {
        return type;
    }

    /**
     * Returns if a given Certificate has been revoked or not.
     * 
     * @param cert
     *            Certificate The Certificate to test
     * 
     * @return true if the certificate has been revoked false if the certificate
     *         has not been revoked yet
     */
    public abstract boolean isRevoked(Certificate cert);

    /**
     * Returns a string containing a concise, human-readable description of the
     * receiver.
     * 
     * @return a printable representation for the receiver.
     */
    public abstract String toString();
}
