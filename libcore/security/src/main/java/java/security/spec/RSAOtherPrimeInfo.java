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
public class RSAOtherPrimeInfo {
    // Prime
    private final BigInteger prime;
    // Prime Exponent
    private final BigInteger primeExponent;
    // CRT Coefficient
    private final BigInteger crtCoefficient;

    /**
     * @com.intel.drl.spec_ref
     */
    public RSAOtherPrimeInfo(BigInteger prime,
            BigInteger primeExponent, BigInteger crtCoefficient) {
        if (prime == null) {
            throw new NullPointerException(Messages.getString("security.83", "prime")); //$NON-NLS-1$ //$NON-NLS-2$
        }
        if (primeExponent == null) {
            throw new NullPointerException(Messages.getString("security.83", "primeExponent")); //$NON-NLS-1$ //$NON-NLS-2$
        }
        if (crtCoefficient == null) {
            throw new NullPointerException(Messages.getString("security.83", "crtCoefficient")); //$NON-NLS-1$ //$NON-NLS-2$
        }
        this.prime = prime;
        this.primeExponent = primeExponent;
        this.crtCoefficient = crtCoefficient;
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public final BigInteger getCrtCoefficient() {
        return crtCoefficient;
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public final BigInteger getPrime() {
        return prime;
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public final BigInteger getExponent() {
        return primeExponent;
    }
}
