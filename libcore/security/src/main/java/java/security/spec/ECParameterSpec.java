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
public class ECParameterSpec implements AlgorithmParameterSpec {
    // Elliptic curve for which this is parameter
    private final EllipticCurve curve;
    // Distinguished point on the elliptic curve called generator or base point
    private final ECPoint generator;
    // Order of the generator
    private final BigInteger order;
    // Cofactor
    private final int cofactor;

    /**
     * @com.intel.drl.spec_ref
     */
    public ECParameterSpec(EllipticCurve curve, ECPoint generator,
            BigInteger order, int cofactor) {
        this.curve = curve;
        this.generator = generator;
        this.order = order;
        this.cofactor = cofactor;
        // throw NullPointerException if curve, generator or order is null
        if (this.curve == null) {
            throw new NullPointerException(Messages.getString("security.83", "curve")); //$NON-NLS-1$ //$NON-NLS-2$
        }
        if (this.generator == null) {
            throw new NullPointerException(Messages.getString("security.83", "generator")); //$NON-NLS-1$ //$NON-NLS-2$
        }
        if (this.order == null) {
            throw new NullPointerException(Messages.getString("security.83", "order")); //$NON-NLS-1$ //$NON-NLS-2$
        }
        // throw IllegalArgumentException if order or cofactor is not positive
        if (!(this.order.compareTo(BigInteger.ZERO) > 0)) {
            throw new
            IllegalArgumentException(Messages.getString("security.86", "order")); //$NON-NLS-1$ //$NON-NLS-2$
        }
        if (!(this.cofactor > 0)) {
            throw new
            IllegalArgumentException(Messages.getString("security.86", "cofactor")); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public int getCofactor() {
        return cofactor;
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public EllipticCurve getCurve() {
        return curve;
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public ECPoint getGenerator() {
        return generator;
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public BigInteger getOrder() {
        return order;
    }
}
