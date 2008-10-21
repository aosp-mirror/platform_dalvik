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
public class RSAPrivateKeySpec implements KeySpec {    
    // Modulus
    private final BigInteger modulus;
    // Private Exponent
    private final BigInteger privateExponent;

    /**
     * @com.intel.drl.spec_ref
     */
    public RSAPrivateKeySpec(BigInteger modulus, BigInteger privateExponent) {
        this.modulus = modulus;
        this.privateExponent = privateExponent;
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public BigInteger getModulus() {
        return modulus;
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public BigInteger getPrivateExponent() {
        return privateExponent;
    }
}
