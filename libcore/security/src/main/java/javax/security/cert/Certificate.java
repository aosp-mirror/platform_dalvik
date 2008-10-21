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

package javax.security.cert;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.SignatureException;
import java.util.Arrays;
import javax.security.cert.CertificateEncodingException;
import javax.security.cert.CertificateException;

/**
 * @com.intel.drl.spec_ref
 */
public abstract class Certificate {

    /**
     * @com.intel.drl.spec_ref
     */
    public Certificate() {}

    /**
     * @com.intel.drl.spec_ref
     */
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Certificate)) {
            return false;
        }
        Certificate object = (Certificate) obj;
        try {
            return Arrays.equals(getEncoded(), object.getEncoded());
        } catch (CertificateEncodingException e) {
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
                res += array[i];
            }
        } catch (CertificateEncodingException e) {
        }
        return res;
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public abstract byte[] getEncoded()
            throws CertificateEncodingException;

    /**
     * @com.intel.drl.spec_ref
     */
    public abstract void verify(PublicKey key)
            throws CertificateException, NoSuchAlgorithmException,
                   InvalidKeyException, NoSuchProviderException,
                   SignatureException;

    /**
     * @com.intel.drl.spec_ref
     */
    public abstract void verify(PublicKey key, String sigProvider)
            throws CertificateException, NoSuchAlgorithmException, 
                   InvalidKeyException, NoSuchProviderException,
                   SignatureException;

    /**
     * @com.intel.drl.spec_ref
     */
    public abstract String toString();

    /**
     * @com.intel.drl.spec_ref
     */
    public abstract PublicKey getPublicKey();
}

