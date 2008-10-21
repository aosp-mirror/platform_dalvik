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
* @author Vera Y. Petrashkova
* @version $Revision$
*/

package java.security.interfaces;

import java.math.BigInteger;
import java.security.spec.RSAOtherPrimeInfo;

/**
 * @com.intel.drl.spec_ref
 * 
 */
public interface RSAMultiPrimePrivateCrtKey extends RSAPrivateKey {
    /**
     * @com.intel.drl.spec_ref
     */
    public static final long serialVersionUID = 618058533534628008L;

    /**
     * @com.intel.drl.spec_ref
     */
    public BigInteger getCrtCoefficient();

    /**
     * @com.intel.drl.spec_ref
     */
    public RSAOtherPrimeInfo[] getOtherPrimeInfo();

    /**
     * @com.intel.drl.spec_ref
     */
    public BigInteger getPrimeP();

    /**
     * @com.intel.drl.spec_ref
     */
    public BigInteger getPrimeQ();

    /**
     * @com.intel.drl.spec_ref
     */
    public BigInteger getPrimeExponentP();

    /**
     * @com.intel.drl.spec_ref
     */
    public BigInteger getPrimeExponentQ();

    /**
     * @com.intel.drl.spec_ref
     */
    public BigInteger getPublicExponent();
}