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
* @author Alexander Y. Kleymenov
* @version $Revision$
*/

package java.security.cert;

import java.math.BigInteger;
import java.security.cert.CRLException;
import java.security.cert.X509CRLEntry;
import java.security.cert.X509Extension;
import java.util.Arrays;
import java.util.Date;
import javax.security.auth.x500.X500Principal;

/**
 * @com.intel.drl.spec_ref
 */
public abstract class X509CRLEntry implements X509Extension {

    /**
     * @com.intel.drl.spec_ref
     */
    public X509CRLEntry() {}

    /**
     * @com.intel.drl.spec_ref
     */
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof X509CRLEntry)) {
            return false;
        }
        X509CRLEntry obj = (X509CRLEntry) other;
        try {
            return Arrays.equals(getEncoded(), obj.getEncoded());
        } catch (CRLException e) {
            return false;
        }
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public int hashCode() {
        int res = 0;
        try {
            byte[] array = getEncoded();
            for (int i=0; i<array.length; i++) {
                res += array[i] & 0xFF;
            }
        } catch (CRLException e) {
        }
        return res;
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public abstract byte[] getEncoded() throws CRLException;

    /**
     * @com.intel.drl.spec_ref
     */
    public abstract BigInteger getSerialNumber();

    public X500Principal getCertificateIssuer() {
        return null;
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public abstract Date getRevocationDate();

    /**
     * @com.intel.drl.spec_ref
     */
    public abstract boolean hasExtensions();

    /**
     * @com.intel.drl.spec_ref
     */
    public abstract String toString();
}

