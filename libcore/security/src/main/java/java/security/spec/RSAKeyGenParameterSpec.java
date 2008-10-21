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
public class RSAKeyGenParameterSpec implements AlgorithmParameterSpec {    
    /**
     * @com.intel.drl.spec_ref
     */
    public static final BigInteger F0 = BigInteger.valueOf(3L);
    /**
     * @com.intel.drl.spec_ref
     */
    public static final BigInteger F4 = BigInteger.valueOf(65537L);

    // Key size
    private final int keysize;
    // Public Exponent
    private final BigInteger publicExponent;

    /**
     * @com.intel.drl.spec_ref
     */
    public RSAKeyGenParameterSpec(int keysize, BigInteger publicExponent) {
        this.keysize = keysize;
        this.publicExponent = publicExponent;
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public int getKeysize() {
        return keysize;
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public BigInteger getPublicExponent() {
        return publicExponent;
    }
}
