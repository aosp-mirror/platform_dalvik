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

package java.security.spec;

import java.math.BigInteger;

import org.apache.harmony.security.internal.nls.Messages;

/**
 * @com.intel.drl.spec_ref
 * 
 */
public class RSAMultiPrimePrivateCrtKeySpec extends RSAPrivateKeySpec {
    // Public Exponent
    private final BigInteger publicExponent;
    // Prime P
    private final BigInteger primeP;
    // Prime Q
    private final BigInteger primeQ;
    // Prime Exponent P
    private final BigInteger primeExponentP;
    // Prime Exponent Q
    private final BigInteger primeExponentQ;
    // CRT Coefficient
    private final BigInteger crtCoefficient;
    // Other Prime Info
    private final RSAOtherPrimeInfo[] otherPrimeInfo;

    /**
     * @com.intel.drl.spec_ref
     */
    public RSAMultiPrimePrivateCrtKeySpec(
            BigInteger modulus,
            BigInteger publicExponent,
            BigInteger privateExponent,
            BigInteger primeP,
            BigInteger primeQ,
            BigInteger primeExponentP,
            BigInteger primeExponentQ,
            BigInteger crtCoefficient,
            RSAOtherPrimeInfo[] otherPrimeInfo) {

        super(modulus, privateExponent);

        // Perform checks specified
        if (modulus == null) {
            throw new NullPointerException(Messages.getString("security.83", "modulus")); //$NON-NLS-1$ //$NON-NLS-2$
        }
        if (privateExponent == null) {
            throw new NullPointerException(Messages.getString("security.83", "privateExponent")); //$NON-NLS-1$ //$NON-NLS-2$
        }
        if (publicExponent == null) {
            throw new NullPointerException(Messages.getString("security.83", "publicExponent")); //$NON-NLS-1$ //$NON-NLS-2$
        }
        if (primeP == null) {
            throw new NullPointerException(Messages.getString("security.83", "primeP")); //$NON-NLS-1$ //$NON-NLS-2$
        }
        if (primeQ == null) {
            throw new NullPointerException(Messages.getString("security.83", "primeQ")); //$NON-NLS-1$ //$NON-NLS-2$
        }
        if (primeExponentP == null) {
            throw new NullPointerException(Messages.getString("security.83", "primeExponentP")); //$NON-NLS-1$ //$NON-NLS-2$
        }
        if (primeExponentQ == null) {
            throw new NullPointerException(Messages.getString("security.83", "primeExponentQ")); //$NON-NLS-1$ //$NON-NLS-2$
        }
        if (crtCoefficient == null) {
            throw new NullPointerException(Messages.getString("security.83", "crtCoefficient")); //$NON-NLS-1$ //$NON-NLS-2$
        }

        if (otherPrimeInfo != null) {
            if (otherPrimeInfo.length == 0) {
                throw new IllegalArgumentException(
                Messages.getString("security.85")); //$NON-NLS-1$
            }
            // Clone array to prevent subsequent modification
            this.otherPrimeInfo = new RSAOtherPrimeInfo[otherPrimeInfo.length];
            System.arraycopy(otherPrimeInfo, 0,
                    this.otherPrimeInfo, 0, this.otherPrimeInfo.length);
        } else {
            this.otherPrimeInfo = null;
        }
        this.publicExponent = publicExponent;
        this.primeP = primeP;
        this.primeQ = primeQ;
        this.primeExponentP = primeExponentP;
        this.primeExponentQ = primeExponentQ;
        this.crtCoefficient = crtCoefficient;
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public BigInteger getCrtCoefficient() {
        return crtCoefficient;
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public RSAOtherPrimeInfo[] getOtherPrimeInfo() {
        // Clone array (if not null) to prevent subsequent modification
        if (otherPrimeInfo == null) {
            return null;
        } else {
            RSAOtherPrimeInfo[] ret =
                new RSAOtherPrimeInfo[otherPrimeInfo.length];
            System.arraycopy(otherPrimeInfo, 0, ret, 0, ret.length);
            return ret;
        }
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public BigInteger getPrimeExponentP() {
        return primeExponentP;
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public BigInteger getPrimeExponentQ() {
        return primeExponentQ;
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public BigInteger getPrimeP() {
        return primeP;
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public BigInteger getPrimeQ() {
        return primeQ;
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public BigInteger getPublicExponent() {
        return publicExponent;
    }
}
