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

/**
 * @com.intel.drl.spec_ref
 * 
 */
public class RSAPrivateCrtKeySpec extends RSAPrivateKeySpec {    
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

    /**
     * @com.intel.drl.spec_ref
     */
    public RSAPrivateCrtKeySpec(BigInteger modulus,
                                BigInteger publicExponent,
                                BigInteger privateExponent,
                                BigInteger primeP,
                                BigInteger primeQ,
                                BigInteger primeExponentP,
                                BigInteger primeExponentQ,
                                BigInteger crtCoefficient) {

        super(modulus, privateExponent);

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
